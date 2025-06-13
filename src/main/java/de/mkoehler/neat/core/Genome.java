package de.mkoehler.neat.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import de.mkoehler.neat.config.NEATConfig;
import de.mkoehler.neat.evolution.InnovationTracker;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

@Getter
@ToString
public class Genome {
    private final Map<Integer, NodeGene> nodes;
    @Setter
    private Map<Integer, ConnectionGene> connections;
    @Setter
    private double fitness = 0.0;
    @Setter
    private double adjustedFitness = 0.0;

    public Genome() {
        this.nodes = new HashMap<>();
        this.connections = new TreeMap<>(); // TreeMap ensures connections are sorted by innovation number
    }

    public Genome copy() {
        Genome newGenome = new Genome();
        for (NodeGene node : this.nodes.values()) {
            newGenome.addNodeGene(node.copy());
        }
        for (ConnectionGene connection : this.connections.values()) {
            newGenome.addConnectionGene(connection.copy());
        }
        newGenome.setFitness(this.fitness);
        newGenome.setAdjustedFitness(this.adjustedFitness);
        return newGenome;
    }

    public static Genome crossover(Genome parent1, Genome parent2, Random random) {
        Genome child = new Genome();
        Genome moreFitParent = parent1.getFitness() >= parent2.getFitness() ? parent1 : parent2;
        Genome lessFitParent = parent1.getFitness() < parent2.getFitness() ? parent1 : parent2;

        // Inherit all nodes from the more fit parent
        for (NodeGene nodeGene : moreFitParent.getNodes().values()) {
            child.addNodeGene(nodeGene.copy());
        }

        // Inherit connections
        for (ConnectionGene connectionGene1 : moreFitParent.getConnections().values()) {
            ConnectionGene connectionGene2 = lessFitParent.getConnections().get(connectionGene1.getInnovationNumber());

            if (connectionGene2 != null) { // Matching gene
                ConnectionGene childGene = random.nextBoolean() ? connectionGene1.copy() : connectionGene2.copy();
                child.addConnectionGene(childGene);
            } else { // Disjoint or excess gene from the more fit parent
                child.addConnectionGene(connectionGene1.copy());
            }
        }

        return child;
    }

    public void mutate(NEATConfig config, InnovationTracker innovationTracker, Random random) {
        if (random.nextDouble() < config.mutateWeightRate) {
            mutateWeight(config, random);
        }
        if (random.nextDouble() < config.addConnectionRate) {
            mutateAddConnection(config, innovationTracker, random);
        }
        if (random.nextDouble() < config.addNodeRate) {
            mutateAddNode(config, innovationTracker, random);
        }
        if (random.nextDouble() < config.toggleEnableRate) {
            mutateToggleEnable(random);
        }
    }

    private void mutateWeight(NEATConfig config, Random random) {
        if (connections.isEmpty()) return;
        List<ConnectionGene> connectionList = new ArrayList<>(connections.values());
        ConnectionGene gene = connectionList.get(random.nextInt(connectionList.size()));
        if (random.nextDouble() < config.weightShiftRate) {
            gene.setWeight(gene.getWeight() + (random.nextDouble() * 2 - 1) * config.weightShiftStrength);
        } else {
            gene.setWeight(random.nextDouble() * config.newConnectionWeightRange * 2 - config.newConnectionWeightRange);
        }
    }

    private void mutateAddConnection(NEATConfig config, InnovationTracker innovationTracker, Random random) {
        List<NodeGene> possibleInputs = new ArrayList<>();
        List<NodeGene> possibleOutputs = new ArrayList<>();

        for(NodeGene node : nodes.values()){
            possibleInputs.add(node);
            if(node.type() != NodeType.INPUT){
                possibleOutputs.add(node);
            }
        }

        if (possibleInputs.isEmpty() || possibleOutputs.isEmpty()) return;

        // Use the configurable number of attempts
        for (int i = 0; i < config.addConnectionAttempts; i++) {
            NodeGene node1 = possibleInputs.get(random.nextInt(possibleInputs.size()));
            NodeGene node2 = possibleOutputs.get(random.nextInt(possibleOutputs.size()));

            if (node1.id() == node2.id() || connectionExists(node1.id(), node2.id())) {
                continue;
            }

            if (!config.allowRecurrent && isPathExists(node2.id(), node1.id())) {
                continue;
            }
            // Use the configurable weight range
            double weight = random.nextDouble() * config.newConnectionWeightRange * 2 - config.newConnectionWeightRange;
            int innovation = innovationTracker.getInnovationNumber(node1.id(), node2.id());
            addConnectionGene(new ConnectionGene(node1.id(), node2.id(), weight, true, innovation));
            return;
        }
    }

    /**
     * Checks if a path exists from a start node to an end node in the genome's graph.
     * This is used to prevent the creation of cycles during mutation.
     *
     * IMPORTANT: This check ignores the 'enabled' status of connections, as a disabled
     * connection could be re-enabled later, creating a cycle. It checks the pure topology.
     *
     * @param startNodeId The ID of the node to start the search from.
     * @param endNodeId   The ID of the node to search for.
     * @return True if a path exists, false otherwise.
     */
    private boolean isPathExists(int startNodeId, int endNodeId) {
        if (startNodeId == endNodeId) {
            return true;
        }

        Queue<Integer> queue = new LinkedList<>();
        Set<Integer> visited = new HashSet<>();

        queue.add(startNodeId);
        visited.add(startNodeId);

        while (!queue.isEmpty()) {
            int currentNodeId = queue.poll();

            // Find all outgoing connections from the current node, regardless of enabled status
            for (ConnectionGene connection : connections.values()) {
                if (connection.getInNodeId() == currentNodeId) {
                    int nextNodeId = connection.getOutNodeId();

                    if (nextNodeId == endNodeId) {
                        return true; // Found a path to the end node
                    }

                    if (!visited.contains(nextNodeId)) {
                        visited.add(nextNodeId);
                        queue.add(nextNodeId);
                    }
                }
            }
        }

        return false; // No path found
    }

    private boolean connectionExists(int inNodeId, int outNodeId) {
        return connections.values().stream()
                .anyMatch(c -> c.getInNodeId() == inNodeId && c.getOutNodeId() == outNodeId);
    }

    private void mutateAddNode(NEATConfig config, InnovationTracker innovationTracker, Random random) {
        if (connections.isEmpty()) return;

        List<ConnectionGene> enabledConnections = connections.values().stream()
                .filter(ConnectionGene::isEnabled).toList();
        if (enabledConnections.isEmpty()) return;

        ConnectionGene oldConnection = enabledConnections.get(random.nextInt(enabledConnections.size()));
        oldConnection.setEnabled(false);

        int newNodeId = innovationTracker.getNodeId();
        addNodeGene(new NodeGene(newNodeId, NodeType.HIDDEN));

        int in1 = innovationTracker.getInnovationNumber(oldConnection.getInNodeId(), newNodeId);
        int in2 = innovationTracker.getInnovationNumber(newNodeId, oldConnection.getOutNodeId());

        addConnectionGene(new ConnectionGene(oldConnection.getInNodeId(), newNodeId, config.addNodeNewLinkWeight, true, in1));
        addConnectionGene(new ConnectionGene(newNodeId, oldConnection.getOutNodeId(), oldConnection.getWeight(), true, in2));
    }

    private void mutateToggleEnable(Random random) {
        if (connections.isEmpty()) return;
        List<ConnectionGene> connectionList = new ArrayList<>(connections.values());
        ConnectionGene gene = connectionList.get(random.nextInt(connectionList.size()));
        gene.setEnabled(!gene.isEnabled());
    }

    public static double getCompatibilityDistance(Genome g1, Genome g2, NEATConfig config) {
        int excessGenes = 0;
        int disjointGenes = 0;
        double weightDiff = 0;
        int matchingGenes = 0;

        Iterator<ConnectionGene> it1 = g1.connections.values().iterator();
        Iterator<ConnectionGene> it2 = g2.connections.values().iterator();

        ConnectionGene gene1 = it1.hasNext() ? it1.next() : null;
        ConnectionGene gene2 = it2.hasNext() ? it2.next() : null;

        while (gene1 != null || gene2 != null) {
            if (gene1 == null) {
                excessGenes++;
                gene2 = it2.hasNext() ? it2.next() : null;
                continue;
            }
            if (gene2 == null) {
                excessGenes++;
                gene1 = it1.hasNext() ? it1.next() : null;
                continue;
            }

            int innov1 = gene1.getInnovationNumber();
            int innov2 = gene2.getInnovationNumber();

            if (innov1 == innov2) { // Matching
                matchingGenes++;
                weightDiff += Math.abs(gene1.getWeight() - gene2.getWeight());
                gene1 = it1.hasNext() ? it1.next() : null;
                gene2 = it2.hasNext() ? it2.next() : null;
            } else if (innov1 < innov2) { // Disjoint g1
                disjointGenes++;
                gene1 = it1.hasNext() ? it1.next() : null;
            } else { // Disjoint g2
                disjointGenes++;
                gene2 = it2.hasNext() ? it2.next() : null;
            }
        }

        int N = Math.max(g1.connections.size(), g2.connections.size());
        if (N < 20) N = 1; // Avoid over-penalizing small genomes

        return (config.C1_EXCESS * excessGenes / N) +
                (config.C2_DISJOINT * disjointGenes / N) +
                (config.C3_WEIGHTS * (matchingGenes > 0 ? weightDiff / matchingGenes : 0));
    }

    public void addNodeGene(NodeGene node) {
        nodes.put(node.id(), node);
    }

    public void addConnectionGene(ConnectionGene connection) {
        connections.put(connection.getInnovationNumber(), connection);
    }

    /**
     * Generates a string representation of the genome's topology for debugging.
     * @return A string detailing nodes and connections.
     */
    @JsonIgnore
    public String getTopologyString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Nodes:\n");
        nodes.values().stream()
                .sorted(Comparator.comparingInt(NodeGene::id))
                .forEach(n -> sb.append(String.format("  Node %d: %s\n", n.id(), n.type())));

        sb.append("\nConnections (Innovation, In -> Out, Weight, Enabled):\n");
        connections.values().stream()
                .sorted(Comparator.comparingInt(ConnectionGene::getInnovationNumber))
                .forEach(c -> sb.append(String.format("  Innov %d: %d -> %d, w=%.3f, %s\n",
                        c.getInnovationNumber(), c.getInNodeId(), c.getOutNodeId(), c.getWeight(), c.isEnabled() ? "E" : "D")));
        return sb.toString();
    }

    /**
     * Serializes the current Genome object into a pretty-printed JSON string.
     * @return A JSON string representation of the genome.
     */
    public String toJsonString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            // Use a "pretty printer" to make the JSON human-readable
            ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
            return writer.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            // In a real application, you might use a proper logger here
            e.printStackTrace();
            // Re-throw as a runtime exception to avoid forcing callers to handle it
            throw new RuntimeException("Error serializing Genome to JSON", e);
        }
    }

    /**
     * Deserializes a Genome object from a JSON string.
     * @param json The JSON string to parse.
     * @return A new Genome instance.
     */
    public static Genome fromJsonString(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, Genome.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Error deserializing Genome from JSON", e);
        }
    }

    /**
     * Creates a new, pruned version of this genome.
     * <p>
     * This method removes all disabled connections and any nodes that become
     * disconnected as a result. The returned genome represents the minimal
     * functional topology of the current one.
     * </p>
     * @return A new, clean Genome instance with only active components.
     */
    @JsonIgnore
    public Genome getAsPruned() {
        Genome prunedGenome = new Genome();
        prunedGenome.nodes.clear(); // Start with no nodes

        // 1. Collect all enabled connections.
        Map<Integer, ConnectionGene> enabledConnections = new HashMap<>();
        for (ConnectionGene conn : this.connections.values()) {
            if (conn.isEnabled()) {
                enabledConnections.put(conn.getInnovationNumber(), conn.copy());
            }
        }

        if (enabledConnections.isEmpty()) {
            // If there are no enabled connections, we might just have I/O nodes.
            // Add them back so the genome isn't empty.
            for (NodeGene node : this.nodes.values()) {
                if (node.type() == NodeType.INPUT || node.type() == NodeType.OUTPUT) {
                    prunedGenome.addNodeGene(node.copy());
                }
            }
            return prunedGenome;
        }

        // 2. Identify all nodes that are part of an enabled connection.
        Set<Integer> activeNodeIds = new HashSet<>();
        for (ConnectionGene conn : enabledConnections.values()) {
            activeNodeIds.add(conn.getInNodeId());
            activeNodeIds.add(conn.getOutNodeId());
        }

        // 3. Add only the active nodes to the new genome.
        for (int nodeId : activeNodeIds) {
            prunedGenome.addNodeGene(this.nodes.get(nodeId).copy());
        }

        // 4. Add the enabled connections to the new genome.
        prunedGenome.setConnections(enabledConnections);

        // Set fitness for reference, though it's not strictly necessary for the pruned version.
        prunedGenome.setFitness(this.fitness);

        return prunedGenome;
    }

}