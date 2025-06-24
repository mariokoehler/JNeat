// File: src/main/java/de/mkoehler/neat/examples/soccer/SensorVisualizerPanel.java
package de.mkoehler.neat.examples.soccer;

import javax.swing.*;
import java.awt.*;

/**
 * A dedicated panel for visualizing the neural network inputs (sensors) of a soccer player.
 */
public class SensorVisualizerPanel extends JPanel {

    private double[] inputs;
    private final String playerName;
    private final Color playerColor;

    // Labels for each sensor to make the display understandable
    private static final String[] INPUT_LABELS = {
            "Ball DX", "Ball DY",
            "Own Goal DX", "Own Goal DY",
            "Opp Goal DX", "Opp Goal DY",
            "Opponent DX", "Opponent DY",
            "Velocity X", "Velocity Y",
            "Ball Vel X", "Ball Vel Y"
    };

    public SensorVisualizerPanel(String playerName, Color playerColor) {
        this.playerName = playerName;
        this.playerColor = playerColor;
        this.setPreferredSize(new Dimension(150, SoccerEnvironment.FIELD_HEIGHT));
        this.setBackground(Color.DARK_GRAY);
    }

    /**
     * Updates the sensor data to be displayed.
     * @param inputs The array of input values for the player's neural network.
     */
    public void updateInputs(double[] inputs) {
        this.inputs = inputs;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw Player Name Title
        g2d.setColor(playerColor);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString(playerName, 10, 20);

        if (inputs == null || inputs.length == 0) {
            return;
        }

        int barWidth = getWidth() - 20;
        int barSpacing = 35;
        int topMargin = 50;

        for (int i = 0; i < inputs.length; i++) {
            double value = inputs[i]; // Value is in range [-1, 1]
            int y = topMargin + i * barSpacing;

            // Draw Label
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            g2d.drawString(INPUT_LABELS[i], 10, y - 5);

            // Draw background of the bar
            g2d.setColor(Color.GRAY.darker());
            g2d.fillRect(10, y, barWidth, 15);

            // Draw the actual value bar
            // Positive values go right (green), negative values go left (red)
            int zeroPoint = 10 + barWidth / 2;
            if (value > 0) {
                g2d.setColor(Color.GREEN);
                int length = (int) (value * (barWidth / 2.0));
                g2d.fillRect(zeroPoint, y, length, 15);
            } else {
                g2d.setColor(Color.RED);
                int length = (int) (-value * (barWidth / 2.0));
                g2d.fillRect(zeroPoint - length, y, length, 15);
            }

            // Draw center line
            g2d.setColor(Color.WHITE.darker());
            g2d.drawLine(zeroPoint, y, zeroPoint, y + 15);
        }
    }
}