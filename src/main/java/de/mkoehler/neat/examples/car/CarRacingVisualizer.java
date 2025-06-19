// File: src/main/java/de/mkoehler/neat/examples/car/CarRacingVisualizer.java
package de.mkoehler.neat.examples.car;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;

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

        // Draw track
        g2d.setColor(Color.DARK_GRAY);
        g2d.fill(track.getOuterBoundary());
        g2d.setColor(getBackground());
        g2d.fill(track.getInnerBoundary());
        g2d.setColor(Color.GRAY);
        g2d.draw(track.getOuterBoundary());
        g2d.draw(track.getInnerBoundary());

        // Draw checkpoints
        g2d.setColor(new Color(0, 255, 0, 80));
        for (var checkpoint : track.getCheckpoints()) {
            g2d.fill(checkpoint);
        }

        // Draw sensor rays
        g2d.setStroke(new BasicStroke(1f));
        for(int i = 0; i < car.sensorRays.size(); i++) {
            Line2D ray = car.sensorRays.get(i);
            // Color ray based on distance (red=close, yellow=far)
            float intensity = car.sensorReadings.get(i).floatValue();
            g2d.setColor(new Color(1.0f, intensity, 0, 0.6f));
            g2d.draw(ray);
        }

        // Draw car
        g2d.setColor(car.isCrashed ? Color.RED : Color.CYAN);
        Shape carShape = car.getTransformedShape();
        g2d.fill(carShape);
    }
}