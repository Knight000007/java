package com.evmanagement.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.painter.Painter;

public class RoutePainter implements Painter<JXMapViewer> {
    private List<GeoPosition> track;
    private Color color = Color.RED;
    private int lineWidth = 4;

    public RoutePainter(List<GeoPosition> track) {
        this.track = new ArrayList<>(track);
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
        g = (Graphics2D) g.create();

        // Convert from geo to world bitmap pixel coordinates
        Rectangle rect = map.getViewportBounds();
        g.translate(-rect.x, -rect.y);

        // Anti-aliasing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Set drawing style
        g.setColor(color);
        g.setStroke(new BasicStroke(lineWidth));

        // Draw the track
        for (int i = 0; i < track.size() - 1; i++) {
            GeoPosition gp1 = track.get(i);
            GeoPosition gp2 = track.get(i + 1);

            Point2D p1 = map.getTileFactory().geoToPixel(gp1, map.getZoom());
            Point2D p2 = map.getTileFactory().geoToPixel(gp2, map.getZoom());

            g.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());
        }

        g.dispose();
    }
} 