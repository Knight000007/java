package com.evmanagement.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class Analytics implements Serializable {
    private static final long serialVersionUID = -2507850679237932194L;
    
    private User user;
    private Map<String, Double> environmentalImpact;
    private Map<String, Double> costSavings;
    private Map<String, Double> usageStats;
    private Map<String, Double> efficiencyMetrics;
    private Map<String, Double> chargingStats;
    private List<Route> tripHistory;
    private Map<YearMonth, Map<String, Double>> annualStats;

    public Analytics(User user) {
        this.user = user;
        this.environmentalImpact = new HashMap<>();
        this.costSavings = new HashMap<>();
        this.usageStats = new HashMap<>();
        this.efficiencyMetrics = new HashMap<>();
        this.chargingStats = new HashMap<>();
        this.tripHistory = new ArrayList<>();
        this.annualStats = new HashMap<>();
        initializeData();
    }

    private void initializeData() {
        // Initialize environmental impact metrics
        environmentalImpact.put("carbonSaved", 0.0);
        environmentalImpact.put("treesEquivalent", 0.0);
        environmentalImpact.put("energySaved", 0.0);
        environmentalImpact.put("householdDaysEquivalent", 0.0);

        // Initialize cost savings metrics
        costSavings.put("evCost", 0.0);
        costSavings.put("petrolCost", 0.0);
        costSavings.put("evCostYearly", 0.0);
        costSavings.put("petrolCostYearly", 0.0);

        // Initialize usage statistics
        usageStats.put("totalTrips", 0.0);
        usageStats.put("totalDistance", 0.0);
        usageStats.put("avgTripLength", 0.0);
        usageStats.put("totalEnergy", 0.0);

        // Initialize efficiency metrics
        efficiencyMetrics.put("avgEnergy", 0.0);
        efficiencyMetrics.put("bestEnergy", 0.0);
        efficiencyMetrics.put("improvement", 0.0);

        // Initialize charging statistics
        chargingStats.put("avgTime", 0.0);
        chargingStats.put("sessions", 0.0);
        chargingStats.put("totalCharged", 0.0);
    }

    public void addTrip(Route trip) {
        tripHistory.add(trip);
        updateMetrics(trip);
    }

    private void updateMetrics(Route trip) {
        // Update usage stats
        usageStats.put("totalTrips", usageStats.get("totalTrips") + 1);
        usageStats.put("totalDistance", usageStats.get("totalDistance") + trip.getDistance());
        usageStats.put("totalEnergy", usageStats.get("totalEnergy") + trip.getEstimatedEnergy());
        usageStats.put("avgTripLength", usageStats.get("totalDistance") / usageStats.get("totalTrips"));

        // Update efficiency metrics
        double energyPerKm = trip.getEstimatedEnergy() / trip.getDistance();
        efficiencyMetrics.put("avgEnergy", energyPerKm);
        if (energyPerKm < efficiencyMetrics.get("bestEnergy") || efficiencyMetrics.get("bestEnergy") == 0.0) {
            efficiencyMetrics.put("bestEnergy", energyPerKm);
        }
        efficiencyMetrics.put("improvement", 
            (efficiencyMetrics.get("avgEnergy") - efficiencyMetrics.get("bestEnergy")) 
            / efficiencyMetrics.get("avgEnergy") * 100);

        // Update environmental impact
        double carbonSaved = trip.getDistance() * 0.2; // Approximate CO2 savings in kg
        environmentalImpact.put("carbonSaved", 
            environmentalImpact.get("carbonSaved") + carbonSaved);
        environmentalImpact.put("treesEquivalent", 
            environmentalImpact.get("carbonSaved") / 21.7); // One tree absorbs about 21.7 kg CO2 per year
        environmentalImpact.put("energySaved", 
            environmentalImpact.get("energySaved") + trip.getEstimatedEnergy());

        // Update cost savings
        double evCost = trip.getEstimatedEnergy() * 15; // Assuming 15 Rs per kWh
        double petrolCost = trip.getDistance() * 0.1 * 180; // Assuming 0.1L/km and 180 Rs per liter
        costSavings.put("evCost", costSavings.get("evCost") + evCost);
        costSavings.put("petrolCost", costSavings.get("petrolCost") + petrolCost);
        costSavings.put("evCostYearly", costSavings.get("evCost") * 12);
        costSavings.put("petrolCostYearly", costSavings.get("petrolCost") * 12);

        // Update charging stats if charging stops were used
        List<ChargingStation> chargingStops = trip.getChargingStops();
        if (!chargingStops.isEmpty()) {
            chargingStats.put("sessions", chargingStats.get("sessions") + chargingStops.size());
            double totalChargingTime = chargingStops.stream()
                .mapToDouble(station -> station.calculateChargingTime(trip.getVehicle().getBatteryCapacity() * 0.8))
                .sum();
            chargingStats.put("avgTime", 
                (chargingStats.get("avgTime") * (chargingStats.get("sessions") - 1) + totalChargingTime) 
                / chargingStats.get("sessions"));
        }

        // Update annual stats
        YearMonth yearMonth = YearMonth.from(LocalDateTime.now());
        Map<String, Double> monthStats = annualStats.computeIfAbsent(yearMonth, k -> new HashMap<>());
        monthStats.put("distance", trip.getDistance());
        monthStats.put("energy", trip.getEstimatedEnergy());
        monthStats.put("carbonSaved", carbonSaved);
        monthStats.put("costSaved", petrolCost - evCost);
    }

    public void refreshData() {
        initializeData();
        for (Route trip : tripHistory) {
            updateMetrics(trip);
        }
    }

    // Getters
    public Map<String, Double> getEnvironmentalImpact() {
        return new HashMap<>(environmentalImpact);
    }

    public Map<String, Double> getCostSavings() {
        return new HashMap<>(costSavings);
    }

    public Map<String, Double> getUsageStats() {
        return new HashMap<>(usageStats);
    }

    public Map<String, Double> getEfficiencyMetrics() {
        return new HashMap<>(efficiencyMetrics);
    }

    public Map<String, Double> getChargingStats() {
        return new HashMap<>(chargingStats);
    }

    public Map<YearMonth, Map<String, Double>> getAnnualStats(int year) {
        Map<YearMonth, Map<String, Double>> yearStats = new HashMap<>();
        annualStats.forEach((ym, stats) -> {
            if (ym.getYear() == year) {
                yearStats.put(ym, new HashMap<>(stats));
            }
        });
        return yearStats;
    }

    public double getSpeedEfficiency() {
        return efficiencyMetrics.getOrDefault("speedEfficiency", 80.0);
    }

    public double getBrakingEfficiency() {
        return efficiencyMetrics.getOrDefault("brakingEfficiency", 80.0);
    }

    public double getRangeUtilization() {
        return efficiencyMetrics.getOrDefault("rangeUtilization", 80.0);
    }

    public double getChargingEfficiency() {
        return efficiencyMetrics.getOrDefault("chargingEfficiency", 80.0);
    }

    public double calculateSustainabilityImpact(double distance, double evEfficiency, 
            double fuelEfficiency, double fuelPrice, double electricityTariff) {
        // Calculate CO2 emissions for petrol vehicle (kg CO2/km)
        double petrolCO2 = distance * fuelEfficiency * 2.31; // 2.31 kg CO2 per liter of petrol

        // Calculate CO2 emissions for EV (kg CO2/kWh)
        double evCO2 = distance * evEfficiency * 0.85; // 0.85 kg CO2 per kWh (Nepal's grid mix)

        // Calculate CO2 savings
        double co2Savings = petrolCO2 - evCO2;
        
        // Update environmental impact metrics
        environmentalImpact.put("carbonSaved", environmentalImpact.get("carbonSaved") + co2Savings);
        
        return co2Savings;
    }
} 