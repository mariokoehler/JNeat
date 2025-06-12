package de.mkoehler.neat.core;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A JPanel that visualizes the topology of a NEAT Genome.
 * It replicates the layered layout approach where nodes are positioned
 * based on their depth from the input layer.
 */
public class GenomeVisualizer extends JPanel {

    private Genome genomeToDraw;
    private final Map<Integer, Point> nodePositions = new HashMap<>();
    private final Map<Integer, Integer> nodeDepths = new HashMap<>();

    public GenomeVisualizer() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(400, 400));
    }

    /**
     * Sets the genome to be visualized and triggers a repaint.
     * @param genome The genome to display.
     */
    public void updateGenome(Genome genome) {
        this.genomeToDraw = genome;
        this.nodePositions.clear();
        this.nodeDepths.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (genomeToDraw == null || genomeToDraw.getNodes().isEmpty()) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        calculateNodePositions();
        drawConnections(g2d);
        drawNodes(g2d);
    }

    /**
     * Calculates the (x, y) coordinates for each node in the genome.
     * This method implements the layered layout algorithm.
     */
    private void calculateNodePositions() {
        int width = getWidth();
        int height = getHeight();
        int margin = 50;

        // Group nodes by type
        List<NodeGene> inputs = new ArrayList<>();
        List<NodeGene> outputs = new ArrayList<>();
        List<NodeGene> hiddens = new ArrayList<>();
        for (NodeGene n : genomeToDraw.getNodes().values()) {
            if (n.type() == NodeType.INPUT) inputs.add(n);
            else if (n.type() == NodeType.OUTPUT) outputs.add(n);
            else hiddens.add(n);
        }

        // 1. Position input nodes on the left
        for (int i = 0; i < inputs.size(); i++) {
            double y = inputs.size() == 1 ? height / 2.0 : margin + i * (height - 2.0 * margin) / (inputs.size() - 1);
            nodePositions.put(inputs.get(i).id(), new Point(margin, (int) y));
        }

        // 2. Calculate depth of all other nodes
        for (NodeGene n : outputs) calculateNodeDepth(n.id());
        for (NodeGene n : hiddens) calculateNodeDepth(n.id());

        // Group hidden nodes by layer (depth)
        Map<Integer, List<NodeGene>> layers = new HashMap<>();
        int maxDepth = 0;
        for (NodeGene hiddenNode : hiddens) {
            int depth = nodeDepths.getOrDefault(hiddenNode.id(), 1);
            layers.computeIfAbsent(depth, k -> new ArrayList<>()).add(hiddenNode);
            if (depth > maxDepth) maxDepth = depth;
        }

        // 3. Position hidden nodes in their layers
        int numLayers = layers.keySet().size();
        List<Integer> sortedDepths = new ArrayList<>(layers.keySet());
        Collections.sort(sortedDepths);

        for(int i = 0; i < sortedDepths.size(); i++) {
            int depth = sortedDepths.get(i);
            List<NodeGene> layerNodes = layers.get(depth);
            double x = margin + (i + 1) * (width - 2.0 * margin) / (numLayers + 1);
            for (int j = 0; j < layerNodes.size(); j++) {
                double y = layerNodes.size() == 1 ? height / 2.0 : margin + j * (height - 2.0 * margin) / (layerNodes.size() - 1);
                nodePositions.put(layerNodes.get(j).id(), new Point((int)x, (int)y));
            }
        }

        // 4. Position output nodes on the right
        for (int i = 0; i < outputs.size(); i++) {
            double y = outputs.size() == 1 ? height / 2.0 : margin + i * (height - 2.0 * margin) / (outputs.size() - 1);
            nodePositions.put(outputs.get(i).id(), new Point(width - margin, (int) y));
        }
    }

    /**
     * Recursively calculates the depth of a node (longest path from an input).
     * Uses memoization to avoid redundant calculations.
     */
    private int calculateNodeDepth(int nodeId) {
        if (nodeDepths.containsKey(nodeId)) {
            return nodeDepths.get(nodeId);
        }
        NodeGene node = genomeToDraw.getNodes().get(nodeId);
        if (node.type() == NodeType.INPUT) {
            nodeDepths.put(nodeId, 0);
            return 0;
        }

        int maxDepth = -1;
        for (ConnectionGene conn : genomeToDraw.getConnections().values()) {
            if (conn.isEnabled() && conn.getOutNodeId() == nodeId) {
                maxDepth = Math.max(maxDepth, calculateNodeDepth(conn.getInNodeId()));
            }
        }

        int finalDepth = maxDepth + 1;
        nodeDepths.put(nodeId, finalDepth);
        return finalDepth;
    }

    private void drawConnections(Graphics2D g2d) {
        for (ConnectionGene conn : genomeToDraw.getConnections().values()) {
            if (!conn.isEnabled()) continue;
            Point from = nodePositions.get(conn.getInNodeId());
            Point to = nodePositions.get(conn.getOutNodeId());
            if (from != null && to != null) {
                g2d.setColor(conn.getWeight() > 0 ? new Color(0, 180, 0, 180) : new Color(220, 0, 0, 180));
                g2d.setStroke(new BasicStroke(Math.min(5f, (float) Math.abs(conn.getWeight()) * 2f)));
                g2d.drawLine(from.x, from.y, to.x, to.y);
            }
        }
    }

    private void drawNodes(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(1.5f));
        for (Map.Entry<Integer, Point> entry : nodePositions.entrySet()) {
            int nodeId = entry.getKey();
            Point p = entry.getValue();
            NodeGene node = genomeToDraw.getNodes().get(nodeId);

            switch (node.type()) {
                case INPUT -> g2d.setColor(new Color(0x1E88E5));  // Blue
                case OUTPUT -> g2d.setColor(new Color(0xE53935)); // Red
                case HIDDEN -> g2d.setColor(new Color(0x6D4C41)); // Brown
            }
            g2d.fillOval(p.x - 7, p.y - 7, 14, 14);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(p.x - 7, p.y - 7, 14, 14);
        }
    }
}