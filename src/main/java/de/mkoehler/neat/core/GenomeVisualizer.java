package de.mkoehler.neat.core;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

public class GenomeVisualizer extends JPanel {

    private Genome genomeToDraw;
    private final Map<Integer, Point> nodePositions = new HashMap<>();
    // Memoization cache for calculated depths
    private final Map<Integer, Integer> nodeDepths = new HashMap<>();

    public GenomeVisualizer() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(400, 400));
    }

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

    private void calculateNodePositions() {
        // ... (This part of the method remains the same) ...
        int width = getWidth();
        int height = getHeight();
        int margin = 50;

        List<NodeGene> inputs = new ArrayList<>();
        List<NodeGene> outputs = new ArrayList<>();
        List<NodeGene> hiddens = new ArrayList<>();
        for (NodeGene n : genomeToDraw.getNodes().values()) {
            if (n.type() == NodeType.INPUT) inputs.add(n);
            else if (n.type() == NodeType.OUTPUT) outputs.add(n);
            else hiddens.add(n);
        }

        for (int i = 0; i < inputs.size(); i++) {
            double y = inputs.size() == 1 ? height / 2.0 : margin + i * (height - 2.0 * margin) / (inputs.size() - 1);
            nodePositions.put(inputs.get(i).id(), new Point(margin, (int) y));
        }

        // --- The change is here: Call the new, safe depth calculation ---
        for (NodeGene n : outputs) calculateNodeDepth(n.id(), new HashSet<>());
        for (NodeGene n : hiddens) calculateNodeDepth(n.id(), new HashSet<>());

        // ... (The rest of the method for positioning nodes in layers remains the same) ...
        Map<Integer, List<NodeGene>> layers = new HashMap<>();
        int maxDepth = 0;
        for (NodeGene hiddenNode : hiddens) {
            int depth = nodeDepths.getOrDefault(hiddenNode.id(), 1);
            layers.computeIfAbsent(depth, k -> new ArrayList<>()).add(hiddenNode);
            if (depth > maxDepth) maxDepth = depth;
        }

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

        for (int i = 0; i < outputs.size(); i++) {
            double y = outputs.size() == 1 ? height / 2.0 : margin + i * (height - 2.0 * margin) / (outputs.size() - 1);
            nodePositions.put(outputs.get(i).id(), new Point(width - margin, (int) y));
        }
    }

    // ==================================================================
    // === NEW, CYCLE-SAFE RECURSIVE METHOD =============================
    // ==================================================================
    /**
     * Recursively calculates node depth, using a path set to prevent infinite loops from cycles.
     * @param nodeId The ID of the node to calculate the depth for.
     * @param path A set of node IDs in the current recursive call chain.
     * @return The calculated depth of the node.
     */
    private int calculateNodeDepth(int nodeId, Set<Integer> path) {
        // 1. Check memoization cache first for performance
        if (nodeDepths.containsKey(nodeId)) {
            return nodeDepths.get(nodeId);
        }

        // 2. Check for cycles. If we're already in the path, stop.
        if (path.contains(nodeId)) {
            return 0; // Return a default depth to break the cycle
        }

        NodeGene node = genomeToDraw.getNodes().get(nodeId);
        if (node.type() == NodeType.INPUT) {
            nodeDepths.put(nodeId, 0);
            return 0;
        }

        // Add current node to the path for this recursive branch
        path.add(nodeId);

        int maxDepth = 0;
        for (ConnectionGene conn : genomeToDraw.getConnections().values()) {
            if (conn.isEnabled() && conn.getOutNodeId() == nodeId) {
                maxDepth = Math.max(maxDepth, calculateNodeDepth(conn.getInNodeId(), path));
            }
        }

        // IMPORTANT: Remove current node from the path before returning.
        // This allows other branches of the recursion to traverse it.
        path.remove(nodeId);

        int finalDepth = maxDepth + 1;
        nodeDepths.put(nodeId, finalDepth); // Store result in cache
        return finalDepth;
    }

    // ... (drawConnections and drawNodes methods remain unchanged) ...
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
                case INPUT -> g2d.setColor(new Color(0x1E88E5));
                case OUTPUT -> g2d.setColor(new Color(0xE53935));
                case HIDDEN -> g2d.setColor(new Color(0x6D4C41));
            }
            g2d.fillOval(p.x - 7, p.y - 7, 14, 14);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(p.x - 7, p.y - 7, 14, 14);
        }
    }
}