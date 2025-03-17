package com.evmanagement;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.List;
import com.evmanagement.model.*;
import com.evmanagement.dao.*;
import java.io.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultWaypoint;
import java.util.Map;
import com.evmanagement.util.ChartGenerator;
import java.util.UUID;
import java.awt.CardLayout;
import java.time.LocalDateTime;
import java.time.Year;
import com.evmanagement.util.*;
import java.util.stream.Collectors;

public class EVManagementApp extends JFrame {
    private JTabbedPane tabbedPane;
    private JPanel vehiclePanel;
    private JPanel chargingPanel;
    private JPanel routePanel;
    private JPanel analyticsPanel;
    private JPanel profilePanel;
    private JPanel reviewsPanel;
    private JPanel sustainabilityPanel;
    private User currentUser;
    private FileDAO<User> userDAO;
    private ChargingStation currentStation;
    private DefaultListModel<String> stationListModel;
    private JLabel nameLabel;
    private JLabel locationLabel;
    private JLabel ratingLabel;
    private CardLayout analyticsCardLayout;
    private JPanel analyticsCardPanel;
    private JTabbedPane analyticsTabbedPane;

    public EVManagementApp() {
        setTitle("EV Management System - Nepal");
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize DAO and models
        userDAO = new FileDAO<>("users.dat");
        stationListModel = new DefaultListModel<>();

        // Set modern look and feel
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            try {
                // Fallback to system look and feel if Nimbus is not available
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        initComponents();
        showLoginDialog();
    }

    private void initComponents() {
        // Create main tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Initialize panels
        vehiclePanel = createVehiclePanel();
        chargingPanel = createChargingPanel();
        routePanel = createRoutePlanningPanel();
        analyticsPanel = createAnalyticsPanel();
        profilePanel = createProfilePanel();
        reviewsPanel = createReviewsPanel();
        sustainabilityPanel = createSustainabilityPanel();

        // Add icons to tabs
        ImageIcon vehicleIcon = new ImageIcon("src/main/resources/icons/vehicle.png");
        ImageIcon chargingIcon = new ImageIcon("src/main/resources/icons/charging.png");
        ImageIcon routeIcon = new ImageIcon("src/main/resources/icons/route.png");
        ImageIcon analyticsIcon = new ImageIcon("src/main/resources/icons/analytics.png");
        ImageIcon profileIcon = new ImageIcon("src/main/resources/icons/profile.png");
        ImageIcon reviewsIcon = new ImageIcon("src/main/resources/icons/reviews.png");
        ImageIcon sustainabilityIcon = new ImageIcon("src/main/resources/icons/sustainability.png");

        // Add panels to tabbed pane with icons
        tabbedPane.addTab("Vehicle Management", vehicleIcon, vehiclePanel);
        tabbedPane.addTab("Charging Stations", chargingIcon, chargingPanel);
        tabbedPane.addTab("Route Planning", routeIcon, routePanel);
        tabbedPane.addTab("Analytics", analyticsIcon, analyticsPanel);
        tabbedPane.addTab("Profile", profileIcon, profilePanel);
        tabbedPane.addTab("Reviews", reviewsIcon, reviewsPanel);
        tabbedPane.addTab("Sustainability Calculator", sustainabilityIcon, sustainabilityPanel);

        // Add tabbed pane to frame
        add(tabbedPane);
    }

    private void showLoginDialog() {
        JDialog loginDialog = new JDialog(this, "Login", true);
        loginDialog.setSize(300, 150);
        loginDialog.setLocationRelativeTo(this);
        loginDialog.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username field
        gbc.gridx = 0; gbc.gridy = 0;
        loginDialog.add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1;
        JTextField usernameField = new JTextField(15);
        loginDialog.add(usernameField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        loginDialog.add(buttonPanel, gbc);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            User user = loadUserFromFile(username);
            if (user != null) {
                handleSuccessfulLogin(user);
                loginDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(loginDialog,
                    "User not found. Please register first.",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        registerButton.addActionListener(e -> {
            showRegistrationDialog();
            loginDialog.dispose();
        });

        loginDialog.setVisible(true);
    }

    private void showRegistrationDialog() {
        JDialog regDialog = new JDialog(this, "Register", true);
        regDialog.setSize(300, 250);
        regDialog.setLocationRelativeTo(this);
        regDialog.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Form fields
        JTextField usernameField = new JTextField(15);
        JTextField nameField = new JTextField(15);
        JTextField emailField = new JTextField(15);
        JTextField phoneField = new JTextField(15);

        gbc.gridx = 0; gbc.gridy = 0;
        regDialog.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        regDialog.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        regDialog.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        regDialog.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        regDialog.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        regDialog.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        regDialog.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        regDialog.add(phoneField, gbc);

        JButton registerButton = new JButton("Register");
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        regDialog.add(registerButton, gbc);

        registerButton.addActionListener(e -> {
            if (!usernameField.getText().trim().isEmpty()) {
                currentUser = new User(
                    "U" + System.currentTimeMillis(),
                    usernameField.getText().trim(),
                    nameField.getText().trim(),
                    emailField.getText().trim(),
                    phoneField.getText().trim()
                );
                saveUserToFile(currentUser);
                updateProfilePanel();
                regDialog.dispose();
            }
        });

        regDialog.setVisible(true);
    }

    private JPanel createVehiclePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Vehicle list on the left
        JPanel listPanel = new JPanel(new BorderLayout());
        DefaultListModel<Vehicle> vehicleListModel = new DefaultListModel<>();
        loadVehiclesFromFile(vehicleListModel);
        
        // Configure the JList for vehicles with proper renderer and selection mode
        JList<Vehicle> vehicleList = new JList<>(vehicleListModel);
        vehicleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        vehicleList.setFixedCellHeight(30); // Make list items bigger for easier selection
        
        // Print initial number of vehicles for debugging
        System.out.println("Initial vehicle list size: " + vehicleListModel.size());
        for (int i = 0; i < vehicleListModel.size(); i++) {
            System.out.println("Vehicle " + i + ": " + vehicleListModel.get(i).getModel());
        }
        
        vehicleList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Vehicle) {
                    Vehicle vehicle = (Vehicle) value;
                    String displayText = vehicle.getModel() + " (" + vehicle.getManufacturer() + ") - " + vehicle.getLicensePlate();
                    Component c = super.getListCellRendererComponent(list, displayText, index, isSelected, cellHasFocus);
                    // Make text larger for better readability
                    ((JLabel)c).setFont(new Font("Arial", Font.PLAIN, 14));
                    return c;
                } else {
                    return super.getListCellRendererComponent(list, "Invalid vehicle", index, isSelected, cellHasFocus);
                }
            }
        });
        
        // Add selection listener to show details when a vehicle is selected
        vehicleList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Vehicle selectedVehicle = vehicleList.getSelectedValue();
                System.out.println("Vehicle selected: " + (selectedVehicle != null ? 
                    selectedVehicle.getModel() : "none") + 
                    ", index: " + vehicleList.getSelectedIndex());
            }
        });
        
        // Set a preferred size for better visibility
        JScrollPane scrollPane = new JScrollPane(vehicleList);
        scrollPane.setPreferredSize(new Dimension(300, 300));
        listPanel.add(scrollPane, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add Vehicle");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        listPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Vehicle details on the right
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Add components to main panel
        panel.add(listPanel, BorderLayout.WEST);
        panel.add(detailsPanel, BorderLayout.CENTER);

        // Add Vehicle Button Action
        addButton.addActionListener(e -> showAddVehicleDialog(vehicleListModel));
        
        // Edit Vehicle Button Action
        editButton.addActionListener(e -> {
            try {
                Vehicle selectedVehicle = vehicleList.getSelectedValue();
                System.out.println("Edit button clicked, selected vehicle: " + 
                                  (selectedVehicle != null ? selectedVehicle.getModel() : "none"));
                
                if (selectedVehicle != null) {
                    showEditVehicleDialog(vehicleListModel, selectedVehicle, vehicleList.getSelectedIndex());
                } else {
                    JOptionPane.showMessageDialog(this, "Please select a vehicle to edit", "No Selection", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                    "Error editing vehicle: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Delete Vehicle Button Action
        deleteButton.addActionListener(e -> {
            try {
                Vehicle selectedVehicle = vehicleList.getSelectedValue();
                System.out.println("Delete button clicked, selected vehicle: " + 
                                  (selectedVehicle != null ? selectedVehicle.getModel() : "none"));
                
                if (selectedVehicle != null) {
                    int confirm = JOptionPane.showConfirmDialog(this, 
                        "Are you sure you want to delete this vehicle?\n" + selectedVehicle.getModel(),
                        "Confirm Deletion", 
                        JOptionPane.YES_NO_OPTION);
                        
                    if (confirm == JOptionPane.YES_OPTION) {
                        // Remove from user's vehicles if user exists
                        if (currentUser != null) {
                            currentUser.removeVehicle(selectedVehicle);
                            saveUserToFile(currentUser);
                            System.out.println("Removed vehicle from user");
                        }
                        
                        // Remove from list model and save
                        vehicleListModel.removeElement(selectedVehicle);
                        saveVehiclesToFile(vehicleListModel);
                        System.out.println("Removed vehicle from list model and saved to file");
                        
                        // Update vehicle combo boxes elsewhere in the app
                        updateVehicleComboBoxes();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Please select a vehicle to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                    "Error deleting vehicle: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    private void loadVehiclesFromFile(DefaultListModel<Vehicle> listModel) {
        listModel.clear(); // Clear existing items first
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("vehicles.dat"))) {
            List<Vehicle> vehicles = (List<Vehicle>) ois.readObject();
            vehicles.forEach(listModel::addElement);
            System.out.println("Loaded " + vehicles.size() + " vehicles from file");
        } catch (IOException | ClassNotFoundException e) {
            // If file doesn't exist or has problem, just log and continue with empty list
            System.out.println("No existing vehicles file found or error reading it: " + e.getMessage());
        }
    }

    private void showAddVehicleDialog(DefaultListModel<Vehicle> listModel) {
        JDialog dialog = new JDialog(this, "Add Vehicle", true);
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField modelField = new JTextField(15);
        JTextField batteryField = new JTextField(15);
        JTextField efficiencyField = new JTextField(15);

        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Model:"), gbc);
        gbc.gridx = 1;
        dialog.add(modelField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Battery (kWh):"), gbc);
        gbc.gridx = 1;
        dialog.add(batteryField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Efficiency (Wh/km):"), gbc);
        gbc.gridx = 1;
        dialog.add(efficiencyField, gbc);

        JButton saveButton = new JButton("Save");
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        dialog.add(saveButton, gbc);

        saveButton.addActionListener(e -> {
            try {
                Vehicle vehicle = new Vehicle(
                    "V" + System.currentTimeMillis(),
                    modelField.getText(),
                    "Generic",
                    2023,
                    "BA 1 CH " + System.currentTimeMillis(),
                    "Black",
                    Double.parseDouble(batteryField.getText()),
                    Double.parseDouble(efficiencyField.getText())
                );
                addVehicle(vehicle);
                listModel.addElement(vehicle);
                saveVehiclesToFile(listModel);
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Please enter valid numbers for battery capacity and efficiency",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    private void saveVehiclesToFile(DefaultListModel<Vehicle> listModel) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("vehicles.dat"))) {
            List<Vehicle> vehicles = new ArrayList<>();
            for (int i = 0; i < listModel.size(); i++) {
                vehicles.add(listModel.get(i));
            }
            oos.writeObject(vehicles);
            System.out.println("Saved " + vehicles.size() + " vehicles to file");
        } catch (IOException e) {
            System.err.println("Error saving vehicles to file: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Failed to save vehicles: " + e.getMessage(),
                "Save Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addVehicle(Vehicle vehicle) {
        if (currentUser != null) {
            currentUser.addVehicle(vehicle);
            saveUserToFile(currentUser);
            
            // Update vehicle list in route planning panel
            updateVehicleComboBoxes();
        }
    }
    
    private void updateVehicleComboBoxes() {
        // Update vehicle list in route planning panel
        JPanel inputPanel = (JPanel) routePanel.getComponent(0);
        for (Component comp : inputPanel.getComponents()) {
            if (comp instanceof JComboBox) {
                @SuppressWarnings("unchecked")
                JComboBox<String> vehicleComboBox = (JComboBox<String>) comp;
                updateVehicleComboBox(vehicleComboBox);
                break;
            }
        }
    }
    
    private void showEditVehicleDialog(DefaultListModel<Vehicle> listModel, Vehicle vehicle, int index) {
        JDialog dialog = new JDialog(this, "Edit Vehicle", true);
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField modelField = new JTextField(vehicle.getModel(), 15);
        JTextField batteryField = new JTextField(String.valueOf(vehicle.getBatteryCapacity()), 15);
        JTextField efficiencyField = new JTextField(String.valueOf(vehicle.getEfficiency()), 15);

        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Model:"), gbc);
        gbc.gridx = 1;
        dialog.add(modelField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Battery (kWh):"), gbc);
        gbc.gridx = 1;
        dialog.add(batteryField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Efficiency (Wh/km):"), gbc);
        gbc.gridx = 1;
        dialog.add(efficiencyField, gbc);

        JButton saveButton = new JButton("Save Changes");
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        dialog.add(saveButton, gbc);

        saveButton.addActionListener(e -> {
            try {
                // Update vehicle properties but keep the ID
                String vehicleId = vehicle.getVehicleId();
                String licensePlate = vehicle.getLicensePlate();
                
                // Create updated vehicle with the same ID
                Vehicle updatedVehicle = new Vehicle(
                    vehicleId,
                    modelField.getText(),
                    vehicle.getManufacturer(),
                    vehicle.getYear(),
                    licensePlate,
                    vehicle.getColor(),
                    Double.parseDouble(batteryField.getText()),
                    Double.parseDouble(efficiencyField.getText())
                );
                
                // Replace in list model
                listModel.set(index, updatedVehicle);
                
                // Update in user's vehicles if user exists
                if (currentUser != null) {
                    currentUser.updateVehicle(updatedVehicle);
                    saveUserToFile(currentUser);
                }
                
                // Save to file
                saveVehiclesToFile(listModel);
                
                // Update vehicle combo boxes elsewhere in the app
                updateVehicleComboBoxes();
                
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Please enter valid numbers for battery capacity and efficiency",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    private JPanel createChargingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Map integration
        JXMapViewer mapViewer = new JXMapViewer();
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        mapViewer.setTileFactory(tileFactory);

        // Set initial map position to Nepal
        GeoPosition nepal = new GeoPosition(28.3949, 84.1240);
        mapViewer.setZoom(7);
        mapViewer.setAddressLocation(nepal);

        // Initialize station list model and load stations
        stationListModel = new DefaultListModel<>();
        List<ChargingStation> stations = loadStationsFromFiles();
        for (ChargingStation station : stations) {
            stationListModel.addElement(station.getName());
        }

        // Create waypoints for all stations
        Set<Waypoint> waypoints = new HashSet<>();
        for (ChargingStation station : stations) {
            waypoints.add(new DefaultWaypoint(
                new GeoPosition(station.getLatitude(), station.getLongitude())
            ));
        }

        // Configure waypoint painter
        WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<>();
        waypointPainter.setWaypoints(waypoints);

        // Add map interactions
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                GeoPosition clicked = mapViewer.convertPointToGeoPosition(p);
                ChargingStation nearest = findNearestStation(clicked.getLatitude(), clicked.getLongitude());
                if (nearest != null) {
                    currentStation = nearest;
                    updateStationDetails(nearest);
                    // Center map on selected station with animation
                    GeoPosition stationPos = new GeoPosition(nearest.getLatitude(), nearest.getLongitude());
                    mapViewer.setAddressLocation(stationPos);
                    mapViewer.setZoom(4);
                }
            }
        };

        // Add map controls
        mapViewer.addMouseListener(mouseAdapter);
        mapViewer.addMouseListener(new PanMouseInputListener(mapViewer));
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCenter(mapViewer));
        mapViewer.addKeyListener(new PanKeyListener(mapViewer));
        mapViewer.addMouseListener(new CenterMapListener(mapViewer));

        // Set up painters
        List<Painter<JXMapViewer>> painters = new ArrayList<>();
        painters.add(waypointPainter);
        CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
        mapViewer.setOverlayPainter(painter);

        // Add map to panel
        panel.add(mapViewer, BorderLayout.CENTER);

        // Create station details panel
        JPanel detailsPanel = new JPanel(new BorderLayout(5, 5));
        detailsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Create station list
        JList<String> stationList = new JList<>(stationListModel);
        stationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stationList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedName = stationList.getSelectedValue();
                ChargingStation selected = findChargingStationByName(selectedName, stations);
                if (selected != null) {
                    currentStation = selected;
                    updateStationDetails(selected);
                    // Center map on selected station
                    GeoPosition stationPos = new GeoPosition(
                        selected.getLatitude(), 
                        selected.getLongitude()
                    );
                    mapViewer.setAddressLocation(stationPos);
                    mapViewer.setZoom(4);
                }
            }
        });

        // Create station info panel
        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        nameLabel = new JLabel();
        locationLabel = new JLabel();
        ratingLabel = new JLabel();
        infoPanel.add(nameLabel);
        infoPanel.add(locationLabel);
        infoPanel.add(ratingLabel);

        // Add components to details panel
        JButton addStationButton = new JButton("Add Station");
        addStationButton.addActionListener(e -> showAddStationDialog(mapViewer, waypointPainter));
        
        detailsPanel.add(addStationButton, BorderLayout.NORTH);
        detailsPanel.add(new JScrollPane(stationList), BorderLayout.CENTER);
        detailsPanel.add(infoPanel, BorderLayout.SOUTH);

        // Set preferred size for details panel
        detailsPanel.setPreferredSize(new Dimension(250, panel.getHeight()));
        
        panel.add(detailsPanel, BorderLayout.EAST);

        return panel;
    }

    private void showAddStationDialog(JXMapViewer mapViewer, WaypointPainter<Waypoint> waypointPainter) {
        JDialog dialog = new JDialog(this, "Add Charging Station", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(20);
        JTextField locationField = new JTextField(20);
        JTextField latitudeField = new JTextField(10);
        JTextField longitudeField = new JTextField(10);

        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        dialog.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Location:"), gbc);
        gbc.gridx = 1;
        dialog.add(locationField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Latitude:"), gbc);
        gbc.gridx = 1;
        dialog.add(latitudeField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Longitude:"), gbc);
        gbc.gridx = 1;
        dialog.add(longitudeField, gbc);

        JButton addButton = new JButton("Add Station");
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        dialog.add(addButton, gbc);

        addButton.addActionListener(e -> {
            try {
                String name = nameField.getText();
                String location = locationField.getText();
                double latitude = Double.parseDouble(latitudeField.getText());
                double longitude = Double.parseDouble(longitudeField.getText());
                
                ChargingStation newStation = new ChargingStation(
                    "S" + System.currentTimeMillis(),
                    name, location, latitude, longitude, 50.0, 10.0
                );
                
                // Save station to file
                saveStationToFile(newStation);
                
                // Update station list model
                stationListModel.addElement(name);
                
                // Add new waypoint to map
                Set<Waypoint> waypoints = new HashSet<>(waypointPainter.getWaypoints());
                waypoints.add(new DefaultWaypoint(new GeoPosition(latitude, longitude)));
                waypointPainter.setWaypoints(waypoints);
                
                // Refresh map
                mapViewer.repaint();
                
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Please enter valid numbers for latitude and longitude",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    private ChargingStation findChargingStationByName(String name, List<ChargingStation> stations) {
        if (name == null) return null;
        return stations.stream()
            .filter(station -> name.equals(station.getName()))
            .findFirst()
            .orElse(null);
    }

    private List<ChargingStation> loadStationsFromFiles() {
        List<ChargingStation> stations = new ArrayList<>();
        File stationsDir = new File("stations");
        
        if (stationsDir.exists() && stationsDir.isDirectory()) {
            File[] files = stationsDir.listFiles((dir, name) -> name.endsWith(".dat"));
            if (files != null) {
                for (File file : files) {
                    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                        ChargingStation station = (ChargingStation) ois.readObject();
                        stations.add(station);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
        return stations;
    }

    private void updateStationListModel(DefaultListModel<String> model) {
        model.clear();
        List<ChargingStation> stations = loadStationsFromFiles();
        for (ChargingStation station : stations) {
            model.addElement(station.getName());
        }
    }

    private void updateStationDetails(ChargingStation station) {
        if (station != null) {
            nameLabel.setText("Name: " + station.getName());
            locationLabel.setText("Location: " + station.getLocation());
            ratingLabel.setText(String.format("Rating: %.1f", station.getAverageRating()));
        }
    }

    private ChargingStation findNearestStation(double lat, double lon) {
        List<ChargingStation> stations = loadStationsFromFiles();
        ChargingStation nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (ChargingStation station : stations) {
            double distance = calculateDistance(
                lat, lon, 
                station.getLatitude(), 
                station.getLongitude()
            );
            if (distance < minDistance) {
                minDistance = distance;
                nearest = station;
            }
        }
        return nearest;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula
        final int R = 6371; // Earth's radius in kilometers
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private void updateVehicleComboBox(JComboBox<String> comboBox) {
        // Clear the combo box first
        comboBox.removeAllItems();
        if (currentUser != null) {
            for (Vehicle vehicle : currentUser.getVehicles()) {
                comboBox.addItem(vehicle.getModel());
            }
        }
    }

    private JPanel createRoutePlanningPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create input panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Vehicle Selection
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Select Vehicle:"), gbc);
        
        JComboBox<String> vehicleComboBox = new JComboBox<>();
        updateVehicleComboBox(vehicleComboBox);
        gbc.gridx = 1; gbc.gridy = 0;
        inputPanel.add(vehicleComboBox, gbc);

        // Start coordinates
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Start Latitude:"), gbc);
        JTextField startLatField = new JTextField(10);
        gbc.gridx = 1;
        inputPanel.add(startLatField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Start Longitude:"), gbc);
        JTextField startLonField = new JTextField(10);
        gbc.gridx = 1;
        inputPanel.add(startLonField, gbc);

        // End coordinates
        gbc.gridx = 0; gbc.gridy = 3;
        inputPanel.add(new JLabel("End Latitude:"), gbc);
        JTextField endLatField = new JTextField(10);
        gbc.gridx = 1;
        inputPanel.add(endLatField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        inputPanel.add(new JLabel("End Longitude:"), gbc);
        JTextField endLonField = new JTextField(10);
        gbc.gridx = 1;
        inputPanel.add(endLonField, gbc);

        // Plan Route button
        JButton planButton = new JButton("Plan Route");
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        inputPanel.add(planButton, gbc);

        // Results panel
        JPanel resultsPanel = new JPanel(new BorderLayout());
        JTextArea routeDetails = new JTextArea(10, 40);
        routeDetails.setEditable(false);
        resultsPanel.add(new JScrollPane(routeDetails), BorderLayout.CENTER);

        // Map panel
        JXMapViewer mapViewer = new JXMapViewer();
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        mapViewer.setTileFactory(tileFactory);
        mapViewer.setZoom(7);
        mapViewer.setAddressLocation(new GeoPosition(27.7172, 85.3240)); // Default to Nepal

        planButton.addActionListener(e -> {
            if (currentUser == null) {
                JOptionPane.showMessageDialog(panel, 
                    "Please log in to use route planning features.", 
                    "Login Required", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                double startLat = Double.parseDouble(startLatField.getText());
                double startLon = Double.parseDouble(startLonField.getText());
                double endLat = Double.parseDouble(endLatField.getText());
                double endLon = Double.parseDouble(endLonField.getText());

                // Get selected vehicle
                String selectedModel = (String) vehicleComboBox.getSelectedItem();
                Vehicle selectedVehicle = currentUser.getVehicles().stream()
                    .filter(v -> v.getModel().equals(selectedModel))
                    .findFirst()
                    .orElse(null);

                if (selectedVehicle == null) {
                    JOptionPane.showMessageDialog(panel, 
                        "Please select a vehicle first.", 
                        "Vehicle Required", 
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Create route
                Route route = new Route(
                    UUID.randomUUID().toString(),
                    String.format("%.4f,%.4f", startLat, startLon),
                    String.format("%.4f,%.4f", endLat, endLon),
                    selectedVehicle
                );

                // Calculate route with available charging stations
                List<ChargingStation> stations = loadStationsFromFiles();
                route.calculateRoute(stations, true);

                // Calculate total distance using Haversine formula
                double totalDistance = calculateDistance(startLat, startLon, endLat, endLon);
                
                // Find all stations near the route path
                List<ChargingStation> nearbyStations = findStationsNearRoute(
                    startLat, startLon, endLat, endLon, stations, 20.0 // 20km radius
                );
                
                // Calculate distances to charging stops
                List<ChargingStation> chargingStops = route.getChargingStops();
                if (!chargingStops.isEmpty()) {
                    // Distance to first charging stop
                    totalDistance = calculateDistance(startLat, startLon, 
                        chargingStops.get(0).getLatitude(), 
                        chargingStops.get(0).getLongitude());
                    
                    // Distance between charging stops
                    for (int i = 0; i < chargingStops.size() - 1; i++) {
                        ChargingStation current = chargingStops.get(i);
                        ChargingStation next = chargingStops.get(i + 1);
                        totalDistance += calculateDistance(
                            current.getLatitude(), current.getLongitude(),
                            next.getLatitude(), next.getLongitude()
                        );
                    }
                    
                    // Distance from last charging stop to destination
                    ChargingStation lastStop = chargingStops.get(chargingStops.size() - 1);
                    totalDistance += calculateDistance(
                        lastStop.getLatitude(), lastStop.getLongitude(),
                        endLat, endLon
                    );
                }

                // Update map
                Set<GeoPosition> waypoints = new HashSet<>();
                waypoints.add(new GeoPosition(startLat, startLon));
                waypoints.add(new GeoPosition(endLat, endLon));
                
                // Add all nearby stations to markers (with different color for non-stop stations)
                List<Waypoint> markers = new ArrayList<>();
                for (ChargingStation station : nearbyStations) {
                    GeoPosition stationPos = new GeoPosition(
                        station.getLatitude(), 
                        station.getLongitude()
                    );
                    waypoints.add(stationPos);
                    // Use custom waypoint to differentiate between stops and nearby stations
                    if (chargingStops.contains(station)) {
                        markers.add(new ColorWaypoint(stationPos, Color.RED)); // Stops are red
                    } else {
                        markers.add(new ColorWaypoint(stationPos, Color.BLUE)); // Other stations are blue
                    }
                }

                // Create a track from waypoints
                List<GeoPosition> track = new ArrayList<>(waypoints);
                
                // Create custom painter for different colored waypoints
                WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<Waypoint>() {
                    @Override
                    protected void doPaint(Graphics2D g, JXMapViewer map, int width, int height) {
                        for (Waypoint waypoint : getWaypoints()) {
                            if (waypoint instanceof ColorWaypoint) {
                                ColorWaypoint colorWaypoint = (ColorWaypoint) waypoint;
                                Point2D point = map.getTileFactory().geoToPixel(
                                    colorWaypoint.getPosition(), map.getZoom());
                                
                                int x = (int) point.getX() - map.getViewportBounds().x;
                                int y = (int) point.getY() - map.getViewportBounds().y;
                                
                                g.setColor(colorWaypoint.getColor());
                                g.fillOval(x - 5, y - 5, 10, 10);
                                g.setColor(Color.BLACK);
                                g.drawOval(x - 5, y - 5, 10, 10);
                            }
                        }
                    }
                };
                
                waypointPainter.setWaypoints(new HashSet<>(markers));
                mapViewer.setOverlayPainter(new CompoundPainter<>(
                    new RoutePainter(track),
                    waypointPainter
                ));

                // Zoom to fit route
                mapViewer.zoomToBestFit(new HashSet<>(track), 0.7);

                // Update route details with accurate distances
                StringBuilder details = new StringBuilder();
                details.append(String.format("Total Distance: %.2f km%n", totalDistance));
                details.append(String.format("Estimated Time: %.2f hours%n", totalDistance / 60.0));
                details.append(String.format("Energy Required: %.2f kWh%n", 
                    totalDistance * (selectedVehicle.getEfficiency())));
                details.append("\nRequired Charging Stops:\n");
                
                if (chargingStops.isEmpty()) {
                    details.append("No charging stops needed\n");
                } else {
                    for (int i = 0; i < chargingStops.size(); i++) {
                        ChargingStation station = chargingStops.get(i);
                        double distanceFromStart = 0;
                        if (i == 0) {
                            distanceFromStart = calculateDistance(startLat, startLon,
                                station.getLatitude(), station.getLongitude());
                        } else {
                            ChargingStation prevStation = chargingStops.get(i - 1);
                            distanceFromStart = calculateDistance(
                                prevStation.getLatitude(), prevStation.getLongitude(),
                                station.getLatitude(), station.getLongitude()
                            );
                        }
                        details.append(String.format("- %s (%s) - %.2f km from previous point%n", 
                            station.getName(), 
                            station.getLocation(),
                            distanceFromStart
                        ));
                    }
                }
                
                details.append("\nOther Nearby Charging Stations:\n");
                List<ChargingStation> otherStations = nearbyStations.stream()
                    .filter(station -> !chargingStops.contains(station))
                    .collect(Collectors.toList());
                    
                if (otherStations.isEmpty()) {
                    details.append("No other stations within 20km of route\n");
                } else {
                    for (ChargingStation station : otherStations) {
                        details.append(String.format("- %s (%s)%n", 
                            station.getName(), 
                            station.getLocation()
                        ));
                    }
                }
                
                routeDetails.setText(details.toString());

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel,
                    "Please enter valid coordinates.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel,
                    "Error calculating route: " + ex.getMessage(),
                    "Route Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        // Layout
        panel.add(inputPanel, BorderLayout.WEST);
        panel.add(mapViewer, BorderLayout.CENTER);
        panel.add(resultsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private List<ChargingStation> findStationsNearRoute(
            double startLat, double startLon, 
            double endLat, double endLon, 
            List<ChargingStation> stations,
            double maxDistance) {
        
        List<ChargingStation> nearbyStations = new ArrayList<>();
        
        // Calculate route vector
        double routeDistance = calculateDistance(startLat, startLon, endLat, endLon);
        double vectorX = endLat - startLat;
        double vectorY = endLon - startLon;
        
        for (ChargingStation station : stations) {
            // Calculate perpendicular distance from station to route line
            double stationLat = station.getLatitude();
            double stationLon = station.getLongitude();
            
            // Use cross product to find perpendicular distance
            double crossProduct = Math.abs(
                vectorX * (stationLon - startLon) - 
                vectorY * (stationLat - startLat)
            );
            double perpendicularDistance = crossProduct / routeDistance;
            
            // Check if station is within maxDistance km of route
            if (perpendicularDistance <= maxDistance) {
                // Also check if station is reasonably close to the route segment
                double distanceFromStart = calculateDistance(startLat, startLon, stationLat, stationLon);
                double distanceFromEnd = calculateDistance(endLat, endLon, stationLat, stationLon);
                
                if (distanceFromStart <= routeDistance + maxDistance && 
                    distanceFromEnd <= routeDistance + maxDistance) {
                    nearbyStations.add(station);
                }
            }
        }
        
        return nearbyStations;
    }
    
    // Custom Waypoint class for different colors
    private static class ColorWaypoint extends DefaultWaypoint {
        private final Color color;
        
        public ColorWaypoint(GeoPosition pos, Color color) {
            super(pos);
            this.color = color;
        }
        
        public Color getColor() {
            return color;
        }
    }

    private JPanel createAnalyticsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create a card layout to switch between login message and analytics content
        CardLayout cardLayout = new CardLayout();
        JPanel cardPanel = new JPanel(cardLayout);
        
        // Login required message panel
        JPanel loginMessagePanel = new JPanel(new GridBagLayout());
        JLabel loginMessage = new JLabel("Please log in to view analytics");
        loginMessage.setFont(new Font("Arial", Font.BOLD, 16));
        loginMessagePanel.add(loginMessage);
        
        // Analytics content panel
        JPanel analyticsContent = new JPanel(new BorderLayout());
        
        // Add both panels to the card layout
        cardPanel.add(loginMessagePanel, "login");
        cardPanel.add(analyticsContent, "content");
        
        // Create tabbed pane for different analytics sections
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Environmental Impact Panel
        JPanel environmentalPanel = new JPanel(new BorderLayout());
        System.out.println(currentUser);
        ChartPanel environmentalChart = new ChartGenerator().createEnvironmentalImpactChart(
            currentUser != null ? currentUser.getAnalytics() : null
        );
        environmentalPanel.add(environmentalChart, BorderLayout.CENTER);
        tabbedPane.addTab("Environmental Impact", environmentalPanel);

        // Energy Usage Panel
        JPanel energyPanel = new JPanel(new BorderLayout());
       
        ChartPanel energyChart = new ChartGenerator().createEnergyUsageChart(
            currentUser != null ? currentUser.getAnalytics() : null
        );
        energyPanel.add(energyChart, BorderLayout.CENTER);
        tabbedPane.addTab("Energy Usage", energyPanel);

        // Cost Analysis Panel
        JPanel costPanel = new JPanel(new BorderLayout());
        ChartPanel costChart = new ChartGenerator().createCostAnalysisChart(
            currentUser != null ? currentUser.getAnalytics() : null
        );
        costPanel.add(costChart, BorderLayout.CENTER);
        tabbedPane.addTab("Cost Analysis", costPanel);
        
        // Add refresh button
        JButton refreshButton = new JButton("Refresh Analytics");
        refreshButton.addActionListener(e -> {
            if (currentUser != null) {
                updateAnalyticsPanels();
            }
        });
        
        // Add components to analytics content
        analyticsContent.add(tabbedPane, BorderLayout.CENTER);
        analyticsContent.add(refreshButton, BorderLayout.SOUTH);
        
        // Add card panel to main panel
        panel.add(cardPanel, BorderLayout.CENTER);
        
        // Show appropriate panel based on login status
        if (currentUser != null) {
            cardLayout.show(cardPanel, "content");
        } else {
            cardLayout.show(cardPanel, "login");
        }
        
        // Store references for later updates
        this.analyticsCardLayout = cardLayout;
        this.analyticsCardPanel = cardPanel;
        this.analyticsTabbedPane = tabbedPane;

        return panel;
    }

    private void updateAnalyticsPanels() {
        if (this.currentUser == null || analyticsTabbedPane == null) return;
        
        // Update each chart
        for (int i = 0; i < analyticsTabbedPane.getTabCount(); i++) {
            Component comp = analyticsTabbedPane.getComponentAt(i);
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                Component chart = panel.getComponent(0);
                if (chart instanceof ChartPanel) {
                    ChartPanel chartPanel = (ChartPanel) chart;
                    switch (i) {
                        case 0: // Environmental Impact
                            chartPanel.setChart(new ChartGenerator()
                                .createEnvironmentalImpactChart(this.currentUser.getAnalytics())
                                .getChart());
                            break;
                        case 1: // Energy Usage
                            chartPanel.setChart(new ChartGenerator()
                                .createEnergyUsageChart(this.currentUser.getAnalytics())
                                .getChart());
                            break;
                        case 2: // Cost Analysis
                            chartPanel.setChart(new ChartGenerator()
                                .createCostAnalysisChart(this.currentUser.getAnalytics())
                                .getChart());
                            break;
                    }
                }
            }
        }
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // User Info Panel
        JPanel userInfoPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        userInfoPanel.setBorder(BorderFactory.createTitledBorder("User Information"));
        
        JLabel usernameLabel = new JLabel("Username: ");
        JLabel nameLabel = new JLabel("Name: ");
        JLabel emailLabel = new JLabel("Email: ");
        JLabel phoneLabel = new JLabel("Phone: ");
        
        JLabel usernameValue = new JLabel();
        JLabel nameValue = new JLabel();
        JLabel emailValue = new JLabel();
        JLabel phoneValue = new JLabel();
        
        userInfoPanel.add(usernameLabel);
        userInfoPanel.add(usernameValue);
        userInfoPanel.add(nameLabel);
        userInfoPanel.add(nameValue);
        userInfoPanel.add(emailLabel);
        userInfoPanel.add(emailValue);
        userInfoPanel.add(phoneLabel);
        userInfoPanel.add(phoneValue);
        
        // Vehicle List Panel
        JPanel vehiclePanel = new JPanel(new BorderLayout(5, 5));
        vehiclePanel.setBorder(BorderFactory.createTitledBorder("Your Vehicles"));
        DefaultListModel<String> vehicleListModel = new DefaultListModel<>();
        JList<String> vehicleList = new JList<>(vehicleListModel);
        
        if (currentUser != null) {
            usernameValue.setText(this.currentUser.getUsername());
            nameValue.setText(this.currentUser.getFullName());
            emailValue.setText(this.currentUser.getEmail());
            phoneValue.setText(this.currentUser.getPhoneNumber());
            
            for (Vehicle vehicle : this.currentUser.getVehicles()) {
                vehicleListModel.addElement(vehicle.getModel());
            }
        }
        
        vehiclePanel.add(new JScrollPane(vehicleList), BorderLayout.CENTER);
        
        // Add panels to main panel
        panel.add(userInfoPanel, BorderLayout.NORTH);
        panel.add(vehiclePanel, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createReviewsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create station selection combo box
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> stationComboBox = new JComboBox<>();
        JList<String> reviewsList = new JList<>();
        
        updateStationComboBox(stationComboBox);
        stationComboBox.addActionListener(e -> {
            String selectedName = (String) stationComboBox.getSelectedItem();
            if (selectedName != null) {
                List<ChargingStation> stations = loadStationsFromFiles();
                currentStation = stations.stream()
                    .filter(s -> s.getName().equals(selectedName))
                    .findFirst()
                    .orElse(null);
                if (currentStation != null) {
                    reviewsList.setListData(currentStation.getReviews().toArray(new String[0]));
                }
            }
        });
        selectionPanel.add(new JLabel("Select Station: "));
        selectionPanel.add(stationComboBox);
        panel.add(selectionPanel, BorderLayout.NORTH);
        
        JTextArea reviewInput = new JTextArea(5, 20);
        JSpinner ratingInput = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
        JButton submitReviewButton = new JButton("Submit Review");

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        inputPanel.add(new JLabel("Review:"));
        inputPanel.add(new JScrollPane(reviewInput));
        inputPanel.add(new JLabel("Rating:"));
        inputPanel.add(ratingInput);

        submitReviewButton.addActionListener(e -> {
            if (currentStation == null) {
                JOptionPane.showMessageDialog(panel,
                    "Please select a charging station first",
                    "No Station Selected",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String review = reviewInput.getText();
            int rating = (int) ratingInput.getValue();
            currentStation.addReview(review, rating);
            saveStationReviewsToFile(currentStation);
            reviewsList.setListData(currentStation.getReviews().toArray(new String[0]));
            reviewInput.setText("");
            ratingInput.setValue(1);
            
            // Update station details to show new rating
            updateStationDetails(currentStation);
        });

        panel.add(inputPanel, BorderLayout.CENTER);
        panel.add(submitReviewButton, BorderLayout.EAST);
        panel.add(new JScrollPane(reviewsList), BorderLayout.SOUTH);
        return panel;
    }

    private void updateStationComboBox(JComboBox<String> comboBox) {
        comboBox.removeAllItems();
        List<ChargingStation> stations = loadStationsFromFiles();
        for (ChargingStation station : stations) {
            comboBox.addItem(station.getName());
        }
    }

    private void saveStationReviewsToFile(ChargingStation station) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(station.getStationId() + "_reviews.dat"))) {
            oos.writeObject(station.getReviews());
            oos.writeObject(station.getRatings());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JPanel createSustainabilityPanel() {
        JPanel panel = new JPanel(new GridLayout(7, 2, 5, 5));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JTextField distanceInput = new JTextField();
        JTextField evEfficiencyInput = new JTextField();
        JTextField fuelEfficiencyInput = new JTextField();
        JTextField fuelPriceInput = new JTextField();
        JTextField electricityTariffInput = new JTextField();
        JButton calculateButton = new JButton("Calculate");
        JLabel resultLabel = new JLabel();

        calculateButton.addActionListener(e -> {
            try {
                double distance = Double.parseDouble(distanceInput.getText());
                double evEfficiency = Double.parseDouble(evEfficiencyInput.getText());
                double fuelEfficiency = Double.parseDouble(fuelEfficiencyInput.getText());
                double fuelPrice = Double.parseDouble(fuelPriceInput.getText());
                double electricityTariff = Double.parseDouble(electricityTariffInput.getText());
                double co2Savings = currentUser.getAnalytics().calculateSustainabilityImpact(distance, evEfficiency, fuelEfficiency, fuelPrice, electricityTariff);
                resultLabel.setText(String.format("CO2 Savings: %.2f kg", co2Savings));
            } catch (NumberFormatException ex) {
                resultLabel.setText("Invalid input");
            }
        });

        panel.add(new JLabel("Distance (km):"));
        panel.add(distanceInput);
        panel.add(new JLabel("EV Efficiency (km/kWh):"));
        panel.add(evEfficiencyInput);
        panel.add(new JLabel("Fuel Efficiency (km/l):"));
        panel.add(fuelEfficiencyInput);
        panel.add(new JLabel("Fuel Price (per liter):"));
        panel.add(fuelPriceInput);
        panel.add(new JLabel("Electricity Tariff (per kWh):"));
        panel.add(electricityTariffInput);
        panel.add(calculateButton);
        panel.add(resultLabel);
        return panel;
    }

    private void updateProfilePanel() {
        if (currentUser == null) return;

        profilePanel.removeAll();

        // User info section
        JPanel infoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        infoPanel.add(new JLabel("Username: " + currentUser.getUsername()), gbc);
        gbc.gridy++;
        infoPanel.add(new JLabel("Full Name: " + currentUser.getFullName()), gbc);
        gbc.gridy++;
        infoPanel.add(new JLabel("Email: " + currentUser.getEmail()), gbc);
        gbc.gridy++;
        infoPanel.add(new JLabel("Phone: " + currentUser.getPhoneNumber()), gbc);

        profilePanel.add(infoPanel, BorderLayout.NORTH);
        profilePanel.revalidate();
        profilePanel.repaint();
    }

    private User loadUserFromFile(String username) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(username + ".dat"))) {
            return (User) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveUserToFile(User user) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(user.getUsername() + ".dat"))) {
            oos.writeObject(user);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveStationToFile(ChargingStation station) {
        File dir = new File("stations");
        if (!dir.exists()) dir.mkdir();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(dir, station.getStationId() + ".dat")))) {
            oos.writeObject(station);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleSuccessfulLogin(User user) {
        this.currentUser = user;
        
        // Update vehicle list in route planning panel
        JPanel inputPanel = (JPanel) routePanel.getComponent(0);
        for (Component comp : inputPanel.getComponents()) {
            if (comp instanceof JComboBox) {
                @SuppressWarnings("unchecked")
                JComboBox<String> vehicleComboBox = (JComboBox<String>) comp;
                updateVehicleComboBox(vehicleComboBox);
                break;
            }
        }
        
        // Show analytics content
        analyticsCardLayout.show(analyticsCardPanel, "content");
        
        // Refresh analytics data
        updateAnalyticsPanels();
        
        // Update profile panel
        updateProfilePanel();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EVManagementApp app = new EVManagementApp();
            app.setVisible(true);
        });
    }
} 