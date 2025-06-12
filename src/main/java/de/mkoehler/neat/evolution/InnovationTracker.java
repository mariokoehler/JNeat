package de.mkoehler.neat.evolution;

import java.util.HashMap;
import java.util.Map;

public class InnovationTracker {
    private int nextNodeId;
    private int nextInnovationNumber;
    private Map<String, Integer> connectionInnovations;

    public InnovationTracker(int inputNodes, int outputNodes) {
        this.nextNodeId = inputNodes + outputNodes;
        this.nextInnovationNumber = 0;
        this.connectionInnovations = new HashMap<>();
    }

    public synchronized int getInnovationNumber(int in, int out) {
        String key = in + "->" + out;
        if (connectionInnovations.containsKey(key)) {
            return connectionInnovations.get(key);
        }
        int innovation = nextInnovationNumber++;
        connectionInnovations.put(key, innovation);
        return innovation;
    }

    public synchronized int getNodeId() {
        return nextNodeId++;
    }

    public void resetForNextGeneration() {
        // Clear per-generation cache to ensure same structural mutations in one gen get same innovation
        connectionInnovations.clear();
    }
}