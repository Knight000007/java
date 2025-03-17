package com.evmanagement.dao;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileDAO<T> {
    private final String filePath;

    public FileDAO(String filePath) {
        this.filePath = filePath;
    }

    public void saveToFile(List<T> items) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(filePath))) {
            oos.writeObject(new ArrayList<>(items));
        }
    }

    @SuppressWarnings("unchecked")
    public List<T> loadFromFile() throws IOException, ClassNotFoundException {
        File file = new File(filePath);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(filePath))) {
            return (List<T>) ois.readObject();
        }
    }

    public void saveToTextFile(List<String> lines) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(filePath))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    public List<String> loadFromTextFile() throws IOException {
        List<String> lines = new ArrayList<>();
        File file = new File(filePath);
        
        if (!file.exists()) {
            return lines;
        }

        try (BufferedReader reader = new BufferedReader(
                new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }
} 