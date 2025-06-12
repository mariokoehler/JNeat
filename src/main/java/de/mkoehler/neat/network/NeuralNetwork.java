package de.mkoehler.neat.network;

import de.mkoehler.neat.core.ConnectionGene;
import de.mkoehler.neat.core.Genome;
import de.mkoehler.neat.core.NodeGene;
import de.mkoehler.neat.core.NodeType;

import java.util.*;

public class NeuralNetwork {
    private final List<Neuron> inputNeurons;
    private final List<Neuron> outputNeurons;
    private final List<Neuron> neuronsInOrder; // This will now be a topologically sorted list

    private NeuralNetwork(List<Neuron> inputNeurons, List<Neuron> outputNeurons, List<Neuron> neuronsInOrder) {
        this.inputNeurons = inputNeurons;
        this.outputNeurons = outputNeurons;
        this.neuronsInOrder = neuronsInOrder;
    }

    public static NeuralNetwork create(Genome genome) {
        Map<Integer, Neuron> neuronMap = new HashMap<>();
        List<Neuron> inputs = new ArrayList<>();
        List<Neuron> outputs = new ArrayList<>();

        // 1. Create all neuron objects
        for (NodeGene nodeGene : genome.getNodes().values()) {
            Neuron neuron = new Neuron(nodeGene.id(), nodeGene.type(), ActivationFunction.SIGMOID);
            neuronMap.put(nodeGene.id(), neuron);
            if (nodeGene.type() == NodeType.INPUT) inputs.add(neuron);
            if (nodeGene.type() == NodeType.OUTPUT) outputs.add(neuron);
        }

        // 2. Create synapses and build the graph structure for sorting
        Map<Neuron, List<Neuron>> adjacencyList = new HashMap<>();
        Map<Neuron, Integer> inDegree = new HashMap<>();

        for (Neuron n : neuronMap.values()) {
            adjacencyList.put(n, new ArrayList<>());
            inDegree.put(n, 0);
        }

        for (ConnectionGene conn : genome.getConnections().values()) {
            if (conn.isEnabled()) {
                Neuron from = neuronMap.get(conn.getInNodeId());
                Neuron to = neuronMap.get(conn.getOutNodeId());
                if (from != null && to != null) {
                    // Add synapse for activation
                    to.addInputSynapse(new Synapse(from, conn.getWeight()));
                    // Add edge to adjacency list for sorting
                    adjacencyList.get(from).add(to);
                    // Increment in-degree for the 'to' neuron
                    inDegree.put(to, inDegree.get(to) + 1);
                }
            }
        }

        // 3. Perform a proper topological sort
        List<Neuron> sortedNeurons = topologicalSort(neuronMap, adjacencyList, inDegree);

        return new NeuralNetwork(inputs, outputs, sortedNeurons);
    }

    public double[] activate(double[] inputValues) {
        if (inputValues.length != inputNeurons.size()) {
            throw new IllegalArgumentException("Incorrect number of inputs. Expected: " + inputNeurons.size() + ", Got: " + inputValues.length);
        }

        // Reset all non-input neuron values
        for(Neuron n : neuronsInOrder){
            if(n.getType() != NodeType.INPUT){
                n.setOutputValue(0.0);
            }
        }

        // Set input values
        for (int i = 0; i < inputNeurons.size(); i++) {
            inputNeurons.get(i).setOutputValue(inputValues[i]);
        }

        // Activate neurons in topological order
        for (Neuron neuron : neuronsInOrder) {
            // Input neurons are already set, so we only need to calculate for others.
            if (neuron.getType() != NodeType.INPUT) {
                neuron.calculate();
            }
        }

        return outputNeurons.stream().mapToDouble(Neuron::getOutputValue).toArray();
    }

    private static List<Neuron> topologicalSort(Map<Integer, Neuron> neuronMap, Map<Neuron, List<Neuron>> adjacencyList, Map<Neuron, Integer> inDegree) {
        Queue<Neuron> queue = new LinkedList<>();
        // Initialize the queue with all nodes having an in-degree of 0
        for (Neuron n : neuronMap.values()) {
            if (inDegree.get(n) == 0) {
                queue.add(n);
            }
        }

        List<Neuron> sortedList = new ArrayList<>();
        while (!queue.isEmpty()) {
            Neuron u = queue.poll();
            sortedList.add(u);

            // For each neighbor of u, decrement its in-degree
            if (adjacencyList.containsKey(u)) {
                for (Neuron v : adjacencyList.get(u)) {
                    inDegree.put(v, inDegree.get(v) - 1);
                    // If in-degree becomes 0, add it to the queue
                    if (inDegree.get(v) == 0) {
                        queue.add(v);
                    }
                }
            }
        }

        // If the sorted list doesn't contain all the nodes, it means the graph has a cycle.
        // This is a critical error for a feed-forward network, as it cannot be activated.
        if (sortedList.size() != neuronMap.size()) {
            throw new IllegalStateException("A cycle was detected in the neural network graph. Cannot create a valid phenotype.");
        }

        return sortedList;
    }

}