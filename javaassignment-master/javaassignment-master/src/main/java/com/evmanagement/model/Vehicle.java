package com.evmanagement.model;

import java.io.Serializable;
import java.time.LocalDate;

public class Vehicle implements Serializable {
    private String vehicleId;
    private String model;
    private String manufacturer;
    private int year;
    private String licensePlate;
    private String color;
    private double batteryCapacity; // in kWh
    private double currentCharge; // in percentage
    private double range; // in kilometers
    private double efficiency; // kWh per 100km
    private LocalDate lastServiceDate;
    private int totalMileage;
    private String ownerUserId;

    public Vehicle(String vehicleId, String model, String manufacturer, int year, 
                  String licensePlate, String color, double batteryCapacity, double efficiency) {
        this.vehicleId = vehicleId;
        this.model = model;
        this.manufacturer = manufacturer;
        this.year = year;
        this.licensePlate = licensePlate;
        this.color = color;
        this.batteryCapacity = batteryCapacity;
        this.efficiency = efficiency;
        this.currentCharge = 100.0; // Default to full charge
        this.range = calculateRange();
        this.lastServiceDate = LocalDate.now();
        this.totalMileage = 0;
    }

    private double calculateRange() {
        return (batteryCapacity * currentCharge / 100.0) / (efficiency / 100.0);
    }

    // Getters and Setters
    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    
    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public double getBatteryCapacity() { return batteryCapacity; }
    public void setBatteryCapacity(double batteryCapacity) { 
        this.batteryCapacity = batteryCapacity;
        this.range = calculateRange();
    }
    
    public double getCurrentCharge() { return currentCharge; }
    public void setCurrentCharge(double currentCharge) { 
        this.currentCharge = currentCharge;
        this.range = calculateRange();
    }
    
    public double getRange() { return range; }
    
    public double getEfficiency() { return efficiency; }
    public void setEfficiency(double efficiency) { 
        this.efficiency = efficiency;
        this.range = calculateRange();
    }
    
    public LocalDate getLastServiceDate() { return lastServiceDate; }
    public void setLastServiceDate(LocalDate lastServiceDate) { this.lastServiceDate = lastServiceDate; }
    
    public int getTotalMileage() { return totalMileage; }
    public void setTotalMileage(int totalMileage) { this.totalMileage = totalMileage; }
    
    public String getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(String ownerUserId) { this.ownerUserId = ownerUserId; }
    
    @Override
    public String toString() {
        return String.format("%s %s (%d) - %s", manufacturer, model, year, licensePlate);
    }
} 