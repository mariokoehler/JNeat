package de.mkoehler.neat.examples.memory;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;

// Visualizer Panel Class
class MemoryVisualizer extends JPanel {
    private MemoryEnvironment env;

    public MemoryVisualizer() {
        setPreferredSize(new Dimension(500, 500));
        setBackground(Color.DARK_GRAY);
    }

    public void updateEnvironment(MemoryEnvironment env) {
        this.env = env;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (env == null) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw Food (Green)
        g2d.setColor(Color.GREEN);
        Point2D.Double foodPos = env.foodPos;
        g2d.fillOval((int) foodPos.x - 10, (int) foodPos.y - 10, 20, 20);

        // Draw Goal (Blue), only if active
        if (env.goalActive) {
            g2d.setColor(Color.CYAN);
            Point2D.Double goalPos = env.goalPos;
            g2d.fillOval((int) goalPos.x - 10, (int) goalPos.y - 10, 20, 20);
        }

        // Draw Agent (White)
        g2d.setColor(Color.WHITE);
        Point2D.Double agentPos = env.agentPos;
        g2d.fillOval((int) agentPos.x - 8, (int) agentPos.y - 8, 16, 16);
    }
}
