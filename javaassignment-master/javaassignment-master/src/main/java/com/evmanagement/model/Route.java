package com.evmanagement.model;

import java.io.Serializable;
import java.util.*;
import java.time.LocalDateTime;
import org.jxmapviewer.viewer.GeoPosition;

public class Route implements Serializable {
    private String routeId;
    private String startLocation;
    private String endLocation;
    private double distance; // in kilometers
    private List<ChargingStation> chargingStops;
    private double estimatedEnergy; // in kWh
    private double estimatedTime; // in hours
    private Vehicle vehicle;
    private int harshBrakingEvents;
    private double duration; // actual duration in hours
    private double averageSpeed; // km/h
    private List<DrivingEvent> drivingEvents;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private RouteStatus status;
    private Map<String, Double> metrics;
    private List<GeoPosition> waypoints;
    private Weather weather;
    private double elevation;
    private Map<String, Double> costBreakdown;

    public Route(String routeId, String startLocation, String endLocation, Vehicle vehicle) {
        this.routeId = routeId;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.vehicle = vehicle;
        this.chargingStops = new ArrayList<>();
        this.harshBrakingEvents = 0;
        this.duration = 0.0;
        this.averageSpeed = 0.0;
        this.drivingEvents = new ArrayList<>();
        this.metrics = new HashMap<>();
        this.waypoints = new ArrayList<>();
        this.costBreakdown = new HashMap<>();
        this.status = RouteStatus.PLANNED;
    }

    public void calculateRoute(List<ChargingStation> availableStations, boolean optimizeForTime) {
        // Get coordinates
        GeoPosition start = getGeoPosition(startLocation);
        GeoPosition end = getGeoPosition(endLocation);
        
        // Calculate base route
        this.distance = calculateDistance(
            start.getLatitude(), start.getLongitude(),
            end.getLatitude(), end.getLongitude()
        );
        
        // Initialize route metrics
        double remainingRange = vehicle.getRange();
        double currentLat = start.getLatitude();
        double currentLon = start.getLongitude();
        
        // Clear existing charging stops
        chargingStops.clear();
        waypoints.clear();
        
        // Add start point
        waypoints.add(start);
        
        // Find optimal charging stops
        while (remainingRange < distance) {
            ChargingStation nextStop = findOptimalChargingStop(
                currentLat, currentLon,
                end.getLatitude(), end.getLongitude(),
                remainingRange,
                availableStations,
                optimizeForTime
            );
            
            if (nextStop != null) {
                chargingStops.add(nextStop);
                waypoints.add(new GeoPosition(nextStop.getLatitude(), nextStop.getLongitude()));
                remainingRange = vehicle.getRange();
                currentLat = nextStop.getLatitude();
                currentLon = nextStop.getLongitude();
            } else {
                throw new RuntimeException("No suitable charging station found");
            }
        }
        
        // Add end point
        waypoints.add(end);
        
        // Calculate route metrics
        calculateRouteMetrics();
    }

    private ChargingStation findOptimalChargingStop(
            double currentLat, double currentLon,
            double destLat, double destLon,
            double remainingRange,
            List<ChargingStation> stations,
            boolean optimizeForTime) {
        
        ChargingStation optimalStation = null;
        double optimalScore = Double.MAX_VALUE;
        
        for (ChargingStation station : stations) {
            if (!station.isAvailable()) continue;
            
            double distanceToStation = calculateDistance(
                currentLat, currentLon,
                station.getLatitude(), station.getLongitude()
            );
            
            if (distanceToStation > remainingRange) continue;
            
            double distanceToDestination = calculateDistance(
                station.getLatitude(), station.getLongitude(),
                destLat, destLon
            );
            
            // Calculate score based on optimization preference
            double score;
            if (optimizeForTime) {
                double chargingTime = calculateChargingTime(station);
                score = distanceToStation + (chargingTime * 50.0/3600.0); // Convert hours to km equivalent
            } else {
                score = distanceToStation + (station.getPricePerKWh() * 10.0); // Weight price in score
            }
            
            if (score < optimalScore) {
                optimalScore = score;
                optimalStation = station;
            }
        }
        
        return optimalStation;
    }

    private void calculateRouteMetrics() {
        // Calculate energy requirements
        this.estimatedEnergy = calculateTotalEnergyRequirement();
        
        // Calculate time including charging stops
        this.estimatedTime = calculateTotalTime();
        
        // Calculate costs
        calculateCosts();
        
        // Update metrics
        metrics.put("totalDistance", distance);
        metrics.put("estimatedEnergy", estimatedEnergy);
        metrics.put("estimatedTime", estimatedTime);
        metrics.put("totalCost", costBreakdown.get("total"));
        metrics.put("energyEfficiency", estimatedEnergy / distance);
    }

    private double calculateTotalEnergyRequirement() {
        // Base energy requirement
        double baseEnergy = distance * (vehicle.getEfficiency() / 100.0);
        
        // Add elevation factor (simplified)
        double elevationFactor = 1.0 + (elevation / 1000.0) * 0.1;
        
        // Add weather factor (simplified)
        double weatherFactor = (weather != null) ? weather.getEnergyImpactFactor() : 1.0;
        
        return baseEnergy * elevationFactor * weatherFactor;
    }

    private double calculateTotalTime() {
        double totalTime = 0.0;
        
        // Driving time (assume average speed of 60 km/h)
        totalTime += distance / 60.0;
        
        // Charging time
        for (ChargingStation station : chargingStops) {
            totalTime += calculateChargingTime(station);
        }
        
        return totalTime;
    }

    private double calculateChargingTime(ChargingStation station) {
        // Calculate required charge
        double requiredCharge = vehicle.getBatteryCapacity() * 0.8; // Charge to 80%
        
        // Calculate charging time in hours
        return requiredCharge / station.getChargingRate();
    }

    private void calculateCosts() {
        double energyCost = 0.0;
        double chargingCost = 0.0;
        
        // Calculate energy costs
        for (ChargingStation station : chargingStops) {
            double chargeAmount = vehicle.getBatteryCapacity() * 0.8; // 80% charge
            chargingCost += station.calculateChargingCost(chargeAmount);
        }
        
        // Store cost breakdown
        costBreakdown.put("energy", energyCost);
        costBreakdown.put("charging", chargingCost);
        costBreakdown.put("total", energyCost + chargingCost);
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula for calculating distance between two points
        final int R = 6371; // Earth's radius in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }

    // Simplified location conversion (in real app, would use geocoding service)
    private double getLocationLatitude(String location) {
        // Kathmandu's latitude as default
        return 27.7172;
    }

    private double getLocationLongitude(String location) {
        // Kathmandu's longitude as default
        return 85.3240;
    }

    // Getters
    public String getRouteId() { return routeId; }
    public String getStartLocation() { return startLocation; }
    public String getEndLocation() { return endLocation; }
    public double getDistance() { return distance; }
    public List<ChargingStation> getChargingStops() { 
        return new ArrayList<>(chargingStops); 
    }
    public double getEstimatedEnergy() { return estimatedEnergy; }
    public double getEstimatedTime() { return estimatedTime; }
    public Vehicle getVehicle() { return vehicle; }
    public Map<String, Double> getMetrics() { return new HashMap<>(metrics); }
    public List<GeoPosition> getWaypoints() { return new ArrayList<>(waypoints); }
    public Map<String, Double> getCostBreakdown() { return new HashMap<>(costBreakdown); }
    public RouteStatus getStatus() { return status; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }

    public void addDrivingEvent(DrivingEvent event) {
        drivingEvents.add(event);
        if (event.getType() == DrivingEventType.HARSH_BRAKING) {
            harshBrakingEvents++;
        }
    }

    public void setDuration(double duration) {
        this.duration = duration;
        if (duration > 0) {
            this.averageSpeed = this.distance / duration;
        }
    }

    public int getHarshBrakingEvents() {
        return harshBrakingEvents;
    }

    public double getDuration() {
        return duration;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public List<DrivingEvent> getDrivingEvents() {
        return new ArrayList<>(drivingEvents);
    }

    public enum DrivingEventType {
        HARSH_BRAKING,
        RAPID_ACCELERATION,
        SHARP_TURN,
        CHARGING_START,
        CHARGING_END
    }

    public static class DrivingEvent implements Serializable {
        private DrivingEventType type;
        private double timestamp;
        private Map<String, Double> parameters;

        public DrivingEvent(DrivingEventType type, double timestamp) {
            this.type = type;
            this.timestamp = timestamp;
            this.parameters = new HashMap<>();
        }

        public void addParameter(String key, double value) {
            parameters.put(key, value);
        }

        public DrivingEventType getType() {
            return type;
        }

        public double getTimestamp() {
            return timestamp;
        }

        public Map<String, Double> getParameters() {
            return new HashMap<>(parameters);
        }
    }

    public void setStatus(RouteStatus status) {
        this.status = status;
    }

    public void setWeather(Weather weather) {
        this.weather = weather;
        // Recalculate metrics if route is already planned
        if (status != RouteStatus.PLANNED) {
            calculateRouteMetrics();
        }
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
        // Recalculate metrics if route is already planned
        if (status != RouteStatus.PLANNED) {
            calculateRouteMetrics();
        }
    }

    public enum RouteStatus {
        PLANNED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }

    public static class Weather implements Serializable {
        private String condition; // sunny, rainy, etc.
        private double temperature;
        private double windSpeed;
        
        public Weather(String condition, double temperature, double windSpeed) {
            this.condition = condition;
            this.temperature = temperature;
            this.windSpeed = windSpeed;
        }
        
        public double getEnergyImpactFactor() {
            double factor = 1.0;
            
            // Temperature impact
            if (temperature < 10) {
                factor *= 1.2; // Cold weather increases energy consumption
            } else if (temperature > 30) {
                factor *= 1.15; // Hot weather increases energy consumption
            }
            
            // Wind impact
            factor *= (1.0 + (windSpeed / 100.0)); // Wind speed impact
            
            // Weather condition impact
            switch (condition.toLowerCase()) {
                case "rainy":
                    factor *= 1.1;
                    break;
                case "snowy":
                    factor *= 1.3;
                    break;
                case "windy":
                    factor *= 1.2;
                    break;
            }
            
            return factor;
        }
    }

    private GeoPosition getGeoPosition(String location) {
        // In a real application, this would use a geocoding service
        // For now, return default coordinates for Nepal
        return new GeoPosition(27.7172, 85.3240);
    }
} 