package de.mkoehler.neat.core;

import javax.swing.*;
import java.awt.*;

/**
 * A simple JFrame wrapper for displaying a GenomeVisualizer.
 */
public class VisualizerFrame extends JFrame {
    private final GenomeVisualizer visualizerPanel;

    public VisualizerFrame(String title) {
        super(title);
        visualizerPanel = new GenomeVisualizer();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(visualizerPanel, BorderLayout.CENTER);
        this.pack();
        this.setLocationByPlatform(true);
        this.setVisible(true);
    }

    /**
     * Updates the displayed genome in the visualizer panel.
     * @param genome The new genome to draw.
     */
    public void updateVisuals(Genome genome) {
        if (genome != null) {
            visualizerPanel.updateGenome(genome);
        }
    }
}
