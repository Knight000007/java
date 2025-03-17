package com.evmanagement.util;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import com.evmanagement.model.Analytics;

import java.awt.Color;
import java.awt.Font;
import java.time.LocalDate;
import java.util.Map;

public class ChartGenerator {
    
    public ChartPanel createEnvironmentalImpactChart(Analytics analytics) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        System.out.printf("Creating energy usage chart", analytics);
        if (analytics != null) {
            Map<String, Double> impact = analytics.getEnvironmentalImpact();
            dataset.addValue(impact.get("carbonSaved"), "CO2 Saved", "Carbon");
            dataset.addValue(impact.get("treesEquivalent"), "Trees Equivalent", "Trees");
            dataset.addValue(impact.get("energySaved"), "Energy Saved", "Energy");
        } else {
            // Add placeholder data
            dataset.addValue(0, "CO2 Saved", "Carbon");
            dataset.addValue(0, "Trees Equivalent", "Trees");
            dataset.addValue(0, "Energy Saved", "Energy");
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
            "Environmental Impact",
            "Metric",
            "Value",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        
        customizeChart(chart);
        return new ChartPanel(chart);
    }
    
    public ChartPanel createEnergyUsageChart(Analytics analytics) {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        TimeSeries series = new TimeSeries("Energy Usage");
        
        if (analytics != null) {
            Map<String, Double> usage = analytics.getUsageStats();
            LocalDate date = LocalDate.now().minusDays(30);
            
            for (int i = 0; i < 30; i++) {
                series.add(new Day(date.getDayOfMonth(), 
                                 date.getMonthValue(), 
                                 date.getYear()),
                         usage.getOrDefault("day" + i, 0.0));
                date = date.plusDays(1);
            }
        }
        
        dataset.addSeries(series);
        
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            "Energy Usage Over Time",
            "Date",
            "Energy (kWh)",
            dataset,
            true,
            true,
            false
        );
        
        customizeChart(chart);
        return new ChartPanel(chart);
    }
    
    public ChartPanel createCostAnalysisChart(Analytics analytics) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        if (analytics != null) {
            Map<String, Double> costs = analytics.getCostSavings();
            dataset.addValue(costs.get("evCost"), "EV", "Monthly");
            dataset.addValue(costs.get("petrolCost"), "Petrol", "Monthly");
            dataset.addValue(costs.get("evCostYearly"), "EV", "Yearly");
            dataset.addValue(costs.get("petrolCostYearly"), "Petrol", "Yearly");
        } else {
            // Add placeholder data
            dataset.addValue(0, "EV", "Monthly");
            dataset.addValue(0, "Petrol", "Monthly");
            dataset.addValue(0, "EV", "Yearly");
            dataset.addValue(0, "Petrol", "Yearly");
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
            "Cost Comparison: EV vs Traditional Fuel",
            "Period",
            "Cost (Rs)",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        
        customizeChart(chart);
        return new ChartPanel(chart);
    }
    
    private void customizeChart(JFreeChart chart) {
        // Customize chart appearance
        chart.setBackgroundPaint(Color.WHITE);
        
        // Customize title
        chart.getTitle().setFont(new Font("Arial", Font.BOLD, 16));
        
        // Customize legend
        chart.getLegend().setItemFont(new Font("Arial", Font.PLAIN, 12));
        
        // Customize plot
        chart.getPlot().setBackgroundPaint(Color.WHITE);
        chart.getPlot().setOutlinePaint(Color.BLACK);
    }
}
