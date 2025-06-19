// File: src/main/java/de/mkoehler/neat/examples/car/CarRacingVisualizer.java
package de.mkoehler.neat.examples.car;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class CarRacingVisualizer extends JPanel {

    private Track track;
    private Car car;

    public CarRacingVisualizer() {
        setPreferredSize(new Dimension(800, 700));
        setBackground(new Color(0x333333));
    }

    public void updateState(Track track, Car car) {
        this.track = track;
        this.car = car;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (track == null || car == null) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw track (unchanged)
        g2d.setColor(Color.DARK_GRAY);
        g2d.fill(track.getOuterBoundary());
        g2d.setColor(getBackground());
        g2d.fill(track.getInnerBoundary());
        g2d.setColor(Color.GRAY);
        g2d.draw(track.getOuterBoundary());
        g2d.draw(track.getInnerBoundary());

        g2d.setColor(new Color(0, 255, 0, 150)); // Green and slightly transparent
        g2d.setStroke(new BasicStroke(3f));
        for (var checkpoint : track.getCheckpoints()) {
            g2d.draw(checkpoint);
        }

        // Draw sensor rays
        g2d.setStroke(new BasicStroke(1f));
        for (int i = 0; i < car.sensorIntersectionPoints.size(); i++) {
            Point2D endPoint = car.sensorIntersectionPoints.get(i);
            Line2D visibleRay = new Line2D.Double(car.position, endPoint);

            // Color ray based on distance (red=close, yellow=far)
            // We use the original sensorReadings for color, as it's already normalized
            float intensity = (1.0f - car.sensorReadings.get(i).floatValue());
            g2d.setColor(new Color(1.0f, intensity, 0, 0.7f));
            g2d.draw(visibleRay);
        }

        // Draw car (unchanged)
        g2d.setColor(car.isCrashed ? Color.RED : Color.CYAN);
        Shape carShape = car.getTransformedShape();
        g2d.fill(carShape);
    }

}