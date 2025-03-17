package com.evmanagement.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.time.LocalDate;

public class User implements Serializable {
    private static final long serialVersionUID = -1737014625941125425L;
    
    private String userId;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private List<Vehicle> vehicles;
    private List<Route> tripHistory;
    private Map<String, List<Route>> favoriteRoutes;
    private double totalCarbonSaved; // in kg CO2
    private double totalEnergySaved; // in kWh
    private Analytics analytics;
    private Map<String, String> preferences; // Store user preferences
    private LocalDate registrationDate;
    private LocalDate lastLoginDate;

    public User(String userId, String username, String fullName, String email, String phoneNumber) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.vehicles = new ArrayList<>();
        this.tripHistory = new ArrayList<>();
        this.favoriteRoutes = new HashMap<>();
        this.totalCarbonSaved = 0.0;
        this.totalEnergySaved = 0.0;
        this.analytics = new Analytics(this);
        this.preferences = new HashMap<>();
        this.registrationDate = LocalDate.now();
        this.lastLoginDate = LocalDate.now();
    }

    // Enhanced vehicle management
    public void addVehicle(Vehicle vehicle) {
        vehicle.setOwnerUserId(this.userId);
        vehicles.add(vehicle);
        saveUserData();
    }

    public void removeVehicle(Vehicle vehicle) {
        vehicles.remove(vehicle);
        saveUserData();
    }

    public void updateVehicle(Vehicle oldVehicle, Vehicle newVehicle) {
        int index = vehicles.indexOf(oldVehicle);
        if (index != -1) {
            newVehicle.setOwnerUserId(this.userId);
            vehicles.set(index, newVehicle);
            saveUserData();
        }
    }
    
    public void updateVehicle(Vehicle updatedVehicle) {
        // Find the vehicle with the same ID and update it
        for (int i = 0; i < vehicles.size(); i++) {
            if (vehicles.get(i).getVehicleId().equals(updatedVehicle.getVehicleId())) {
                updatedVehicle.setOwnerUserId(this.userId);
                vehicles.set(i, updatedVehicle);
                saveUserData();
                break;
            }
        }
    }

    // Enhanced trip management
    public void addTrip(Route trip) {
        tripHistory.add(trip);
        analytics.addTrip(trip);
        updateEnvironmentalImpact(
            trip.getDistance() * 0.2, // Approximate CO2 savings in kg
            trip.getEstimatedEnergy()
        );
        saveUserData();
    }

    public void addFavoriteRoute(String name, Route route) {
        favoriteRoutes.computeIfAbsent(name, k -> new ArrayList<>()).add(route);
        saveUserData();
    }

    public void removeFavoriteRoute(String name) {
        favoriteRoutes.remove(name);
        saveUserData();
    }

    // User preferences management
    public void setPreference(String key, String value) {
        preferences.put(key, value);
        saveUserData();
    }

    public String getPreference(String key) {
        return preferences.getOrDefault(key, "");
    }

    public void updateLoginDate() {
        this.lastLoginDate = LocalDate.now();
        saveUserData();
    }

    // Profile update methods
    public void updateProfile(String fullName, String email, String phoneNumber) {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        saveUserData();
    }

    // Data persistence
    private void saveUserData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(username + ".dat"))) {
            oos.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public List<Vehicle> getVehicles() { return new ArrayList<>(vehicles); }
    public List<Route> getTripHistory() { return new ArrayList<>(tripHistory); }
    public Map<String, List<Route>> getFavoriteRoutes() { return new HashMap<>(favoriteRoutes); }
    public double getTotalCarbonSaved() { return totalCarbonSaved; }
    public double getTotalEnergySaved() { return totalEnergySaved; }
    public Analytics getAnalytics() { return analytics; }
    public LocalDate getRegistrationDate() { return registrationDate; }
    public LocalDate getLastLoginDate() { return lastLoginDate; }
    public Map<String, String> getPreferences() { return new HashMap<>(preferences); }

    public void updateEnvironmentalImpact(double carbonSaved, double energySaved) {
        this.totalCarbonSaved += carbonSaved;
        this.totalEnergySaved += energySaved;
        saveUserData();
    }
} 

// test