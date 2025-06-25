package de.mkoehler.neat.evolution;

import de.mkoehler.neat.core.ConnectionGene;
import de.mkoehler.neat.core.Genome;
import de.mkoehler.neat.core.NodeGene;

import java.util.HashMap;
import java.util.List;
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

    /**
     * Scans a population of genomes (typically after loading from a file)
     * and updates the tracker's internal counters to avoid reusing existing
     * node IDs and innovation numbers.
     *
     * @param population The list of genomes to scan.
     */
    public synchronized void primeFromPopulation(List<Genome> population) {
        System.out.println("Priming InnovationTracker from existing population...");
        int maxNodeId = -1;
        int maxInnovation = -1;

        for (Genome genome : population) {
            for (NodeGene node : genome.getNodes().values()) {
                if (node.id() > maxNodeId) {
                    maxNodeId = node.id();
                }
            }
            for (ConnectionGene conn : genome.getConnections().values()) {
                if (conn.getInnovationNumber() > maxInnovation) {
                    maxInnovation = conn.getInnovationNumber();
                }
            }
        }

        // Set the next available ID to be one greater than the highest found
        if (maxNodeId > this.nextNodeId) {
            this.nextNodeId = maxNodeId + 1;
        }
        if (maxInnovation > this.nextInnovationNumber) {
            this.nextInnovationNumber = maxInnovation + 1;
        }
        System.out.printf("Tracker primed. Next Node ID: %d, Next Innovation: %d%n", this.nextNodeId, this.nextInnovationNumber);
    }
}