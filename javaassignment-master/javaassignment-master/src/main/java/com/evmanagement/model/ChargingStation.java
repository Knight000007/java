package com.evmanagement.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class ChargingStation implements Serializable {
    private static final long serialVersionUID = 110048258295059597L;
    
    private String stationId;
    private String name;
    private String location;
    private double latitude;
    private double longitude;
    private boolean isAvailable;
    private double chargingRate; // kW
    private double pricePerKWh;
    private List<String> reviews;
    private List<Integer> ratings;
    private Map<String, Object> amenities;
    private List<ChargingPort> chargingPorts;
    private Map<LocalDateTime, Integer> occupancyHistory;
    private OperatingHours operatingHours;
    private MaintenanceSchedule maintenance;
    private PaymentOptions paymentOptions;
    private EmergencyContact emergencyContact;

    public ChargingStation(String stationId, String name, String location, 
                          double latitude, double longitude, double chargingRate, 
                          double pricePerKWh) {
        this.stationId = stationId;
        this.name = name;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isAvailable = true;
        this.chargingRate = chargingRate;
        this.pricePerKWh = pricePerKWh;
        this.reviews = new ArrayList<>();
        this.ratings = new ArrayList<>();
        this.amenities = new HashMap<>();
        this.chargingPorts = new ArrayList<>();
        this.occupancyHistory = new HashMap<>();
        initializeDefaultAmenities();
        initializeDefaultPorts();
    }

    private void initializeDefaultAmenities() {
        amenities.put("wifi", true);
        amenities.put("restroom", true);
        amenities.put("parking", true);
        amenities.put("cafe", false);
        amenities.put("shopping", false);
        amenities.put("waiting_area", true);
    }

    private void initializeDefaultPorts() {
        chargingPorts.add(new ChargingPort("P1", ChargingType.DC_FAST, 50.0));
        chargingPorts.add(new ChargingPort("P2", ChargingType.AC_LEVEL_2, 7.2));
    }

    public void updateAvailability(boolean available) {
        this.isAvailable = available;
        saveStationData();
    }

    public void updatePricing(double newPrice) {
        this.pricePerKWh = newPrice;
        saveStationData();
    }

    public void addChargingPort(ChargingPort port) {
        chargingPorts.add(port);
        saveStationData();
    }

    public void removeChargingPort(String portId) {
        chargingPorts.removeIf(port -> port.getId().equals(portId));
        saveStationData();
    }

    public void setOperatingHours(OperatingHours hours) {
        this.operatingHours = hours;
        saveStationData();
    }

    public void scheduleMaintenanceWindow(LocalDateTime start, LocalDateTime end) {
        if (maintenance == null) {
            maintenance = new MaintenanceSchedule();
        }
        maintenance.addMaintenanceWindow(start, end);
        saveStationData();
    }

    public void setPaymentOptions(PaymentOptions options) {
        this.paymentOptions = options;
        saveStationData();
    }

    public void setEmergencyContact(EmergencyContact contact) {
        this.emergencyContact = contact;
        saveStationData();
    }

    public void updateOccupancy(LocalDateTime timestamp, int occupancy) {
        occupancyHistory.put(timestamp, occupancy);
        if (occupancyHistory.size() > 1000) { // Limit history size
            LocalDateTime oldest = occupancyHistory.keySet().stream().min(LocalDateTime::compareTo).get();
            occupancyHistory.remove(oldest);
        }
        saveStationData();
    }

    public void addReview(String review, int rating) {
        reviews.add(review);
        ratings.add(rating);
        saveStationData();
    }

    public double getAverageRating() {
        if (ratings.isEmpty()) return 0.0;
        return ratings.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }

    public double calculateChargingTime(double requiredKWh) {
        // Find fastest available charging port
        return chargingPorts.stream()
            .filter(ChargingPort::isAvailable)
            .mapToDouble(port -> requiredKWh / port.getPower())
            .min()
            .orElse(requiredKWh / chargingRate);
    }

    public double calculateChargingCost(double kWhCharged) {
        return kWhCharged * pricePerKWh;
    }

    private void saveStationData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream("stations/" + stationId + ".dat"))) {
            oos.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Getters
    public String getStationId() { return stationId; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public boolean isAvailable() { return isAvailable; }
    public double getChargingRate() { return chargingRate; }
    public double getPricePerKWh() { return pricePerKWh; }
    public List<String> getReviews() { return new ArrayList<>(reviews); }
    public List<Integer> getRatings() { return new ArrayList<>(ratings); }
    public Map<String, Object> getAmenities() { return new HashMap<>(amenities); }
    public List<ChargingPort> getChargingPorts() { return new ArrayList<>(chargingPorts); }
    public OperatingHours getOperatingHours() { return operatingHours; }
    public MaintenanceSchedule getMaintenance() { return maintenance; }
    public PaymentOptions getPaymentOptions() { return paymentOptions; }
    public EmergencyContact getEmergencyContact() { return emergencyContact; }

    public static class ChargingPort implements Serializable {
        private String id;
        private ChargingType type;
        private double power;
        private boolean available;
        private String connectorType;

        public ChargingPort(String id, ChargingType type, double power) {
            this.id = id;
            this.type = type;
            this.power = power;
            this.available = true;
            this.connectorType = "Type 2"; // Default
        }

        public String getId() { return id; }
        public ChargingType getType() { return type; }
        public double getPower() { return power; }
        public boolean isAvailable() { return available; }
        public String getConnectorType() { return connectorType; }

        public void setAvailable(boolean available) {
            this.available = available;
        }
    }

    public enum ChargingType {
        AC_LEVEL_1(3.7),
        AC_LEVEL_2(7.2),
        DC_FAST(50.0),
        DC_SUPER_FAST(150.0);

        private final double maxPower;

        ChargingType(double maxPower) {
            this.maxPower = maxPower;
        }

        public double getMaxPower() {
            return maxPower;
        }
    }

    public static class OperatingHours implements Serializable {
        private Map<DayOfWeek, TimeRange> schedule;

        public OperatingHours() {
            this.schedule = new HashMap<>();
        }

        public void setHours(DayOfWeek day, LocalTime open, LocalTime close) {
            schedule.put(day, new TimeRange(open, close));
        }

        public boolean isOpen(LocalDateTime dateTime) {
            TimeRange range = schedule.get(dateTime.getDayOfWeek());
            if (range == null) return false;
            LocalTime time = dateTime.toLocalTime();
            return !time.isBefore(range.open) && !time.isAfter(range.close);
        }

        private static class TimeRange implements Serializable {
            private LocalTime open;
            private LocalTime close;

            public TimeRange(LocalTime open, LocalTime close) {
                this.open = open;
                this.close = close;
            }
        }
    }

    public static class MaintenanceSchedule implements Serializable {
        private List<MaintenanceWindow> windows;

        public MaintenanceSchedule() {
            this.windows = new ArrayList<>();
        }

        public void addMaintenanceWindow(LocalDateTime start, LocalDateTime end) {
            windows.add(new MaintenanceWindow(start, end));
        }

        public boolean isUnderMaintenance(LocalDateTime dateTime) {
            return windows.stream()
                .anyMatch(window -> window.includes(dateTime));
        }

        private static class MaintenanceWindow implements Serializable {
            private LocalDateTime start;
            private LocalDateTime end;

            public MaintenanceWindow(LocalDateTime start, LocalDateTime end) {
                this.start = start;
                this.end = end;
            }

            public boolean includes(LocalDateTime dateTime) {
                return !dateTime.isBefore(start) && !dateTime.isAfter(end);
            }
        }
    }

    public static class PaymentOptions implements Serializable {
        private boolean creditCard;
        private boolean debitCard;
        private boolean mobilePay;
        private boolean rfidCard;
        private List<String> supportedPaymentApps;

        public PaymentOptions() {
            this.supportedPaymentApps = new ArrayList<>();
        }

        public void enableCreditCard() { this.creditCard = true; }
        public void enableDebitCard() { this.debitCard = true; }
        public void enableMobilePay() { this.mobilePay = true; }
        public void enableRfidCard() { this.rfidCard = true; }
        public void addPaymentApp(String app) { this.supportedPaymentApps.add(app); }

        public boolean acceptsCreditCard() { return creditCard; }
        public boolean acceptsDebitCard() { return debitCard; }
        public boolean acceptsMobilePay() { return mobilePay; }
        public boolean acceptsRfidCard() { return rfidCard; }
        public List<String> getSupportedPaymentApps() { return new ArrayList<>(supportedPaymentApps); }
    }

    public static class EmergencyContact implements Serializable {
        private String name;
        private String phone;
        private String email;
        private boolean available24x7;

        public EmergencyContact(String name, String phone, String email, boolean available24x7) {
            this.name = name;
            this.phone = phone;
            this.email = email;
            this.available24x7 = available24x7;
        }

        public String getName() { return name; }
        public String getPhone() { return phone; }
        public String getEmail() { return email; }
        public boolean isAvailable24x7() { return available24x7; }
    }
} 