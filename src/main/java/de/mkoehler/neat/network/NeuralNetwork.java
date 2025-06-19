package de.mkoehler.neat.network;

import de.mkoehler.neat.config.NEATConfig;
import de.mkoehler.neat.core.ConnectionGene;
import de.mkoehler.neat.core.Genome;
import de.mkoehler.neat.core.NodeGene;
import de.mkoehler.neat.core.NodeType;

import java.util.*;

public class NeuralNetwork {

    private final List<Neuron> neurons; // Used for recurrent activation
    private final List<Neuron> neuronsInOrder; // Used for fast feed-forward activation

    private final List<Neuron> inputNeurons;
    private final List<Neuron> outputNeurons;
    private final boolean isRecurrent;

    private NeuralNetwork(List<Neuron> allNeurons, List<Neuron> sortedNeurons, List<Neuron> inputNeurons, List<Neuron> outputNeurons, boolean isRecurrent) {
        this.neurons = allNeurons;
        this.neuronsInOrder = sortedNeurons;
        this.inputNeurons = inputNeurons;
        this.outputNeurons = outputNeurons;
        this.isRecurrent = isRecurrent;
    }

    /**
     * Creates a NeuralNetwork from a given genome and configuration.
     * It will prepare for either feed-forward or recurrent activation based on the config.
     *
     * @param genome The genome to decode.
     * @param config The NEAT configuration, which specifies if recurrence is allowed.
     * @return A ready-to-use NeuralNetwork instance.
     */
    public static NeuralNetwork create(Genome genome, NEATConfig config) {
        Map<Integer, Neuron> neuronMap = new HashMap<>();
        List<Neuron> inputs = new ArrayList<>();
        List<Neuron> outputs = new ArrayList<>();
        List<Neuron> allNeurons = new ArrayList<>();

        for (NodeGene nodeGene : genome.getNodes().values()) {
            Neuron neuron = new Neuron(nodeGene.id(), nodeGene.type(), ActivationFunction.SIGMOID);
            neuronMap.put(nodeGene.id(), neuron);
            allNeurons.add(neuron);
            if (nodeGene.type() == NodeType.INPUT) inputs.add(neuron);
            if (nodeGene.type() == NodeType.OUTPUT) outputs.add(neuron);
        }

        for (ConnectionGene conn : genome.getConnections().values()) {
            if (conn.isEnabled()) {
                Neuron from = neuronMap.get(conn.getInNodeId());
                Neuron to = neuronMap.get(conn.getOutNodeId());
                if (from != null && to != null) {
                    to.addInputSynapse(new Synapse(from, conn.getWeight()));
                }
            }
        }

        List<Neuron> sortedNeurons = null;
        if (!config.isAllowRecurrent()) {
            // If feed-forward, we use the high-performance topological sort
            sortedNeurons = topologicalSort(neuronMap, genome);
        }

        return new NeuralNetwork(allNeurons, sortedNeurons, inputs, outputs, config.isAllowRecurrent());
    }

    /**
     * Activates the network with a default number of steps.
     * 1 step for feed-forward, 2 steps for recurrent.
     *
     * @param inputValues The input values for the network.
     * @return The output values from the network's output neurons.
     */
    public double[] activate(double[] inputValues) {
        int defaultSteps = this.isRecurrent ? 2 : 1;
        return activate(inputValues, defaultSteps);
    }

    /**
     * Activates the network over a specified number of time steps, choosing the correct method.
     *
     * @param inputValues The input values for the network.
     * @param timeSteps   The number of iterations to propagate signals. Ignored for feed-forward networks (always 1).
     * @return The final output values from the output neurons.
     */
    public double[] activate(double[] inputValues, int timeSteps) {
        if (inputValues.length != inputNeurons.size()) {
            throw new IllegalArgumentException("Incorrect number of inputs. Expected: " + inputNeurons.size() + ", Got: " + inputValues.length);
        }

        if (this.isRecurrent) {
            return activateRecurrent(inputValues, timeSteps);
        } else {
            return activateFeedForward(inputValues);
        }
    }

    private double[] activateFeedForward(double[] inputValues) {
        if (inputValues.length != inputNeurons.size()) {
            throw new IllegalArgumentException("Incorrect number of inputs.");
        }
        for (int i = 0; i < inputNeurons.size(); i++) {
            inputNeurons.get(i).setOutputValue(inputValues[i]);
        }
        for (Neuron neuron : neuronsInOrder) {
            if (neuron.getType() != NodeType.INPUT) {
                neuron.calculate();
            }
        }
        return outputNeurons.stream().mapToDouble(Neuron::getOutputValue).toArray();
    }

    private double[] activateRecurrent(double[] inputValues, int timeSteps) {
        if (inputValues.length != inputNeurons.size()) {
            throw new IllegalArgumentException("Incorrect number of inputs.");
        }
        for (Neuron n : neurons) n.setOutputValue(0.0);

        for (int step = 0; step < timeSteps; step++) {
            Map<Integer, Double> previousStepValues = new HashMap<>();
            for (Neuron n : neurons) {
                previousStepValues.put(n.getId(), n.getOutputValue());
            }

            for (int i = 0; i < inputNeurons.size(); i++) {
                inputNeurons.get(i).setOutputValue(inputValues[i]);
            }

            for (Neuron n : neurons) {
                if (n.getType() != NodeType.INPUT) {
                    double sum = 0;
                    for (Synapse s : n.getInputSynapses()) {
                        double fromValue = previousStepValues.getOrDefault(s.getFromNeuron().getId(), 0.0);
                        sum += fromValue * s.getWeight();
                    }
                    n.setOutputValue(ActivationFunction.SIGMOID.applyAsDouble(sum));
                }
            }
        }
        return outputNeurons.stream().mapToDouble(Neuron::getOutputValue).toArray();
    }

    /**
     * Re-introduced topological sort (Kahn's Algorithm) for feed-forward networks.
     */
    private static List<Neuron> topologicalSort(Map<Integer, Neuron> neuronMap, Genome genome) {
        Map<Neuron, Integer> inDegree = new HashMap<>();
        Map<Neuron, List<Neuron>> adjacencyList = new HashMap<>();

        for (Neuron n : neuronMap.values()) {
            inDegree.put(n, 0);
            adjacencyList.put(n, new ArrayList<>());
        }

        for (ConnectionGene conn : genome.getConnections().values()) {
            if (conn.isEnabled()) {
                Neuron from = neuronMap.get(conn.getInNodeId());
                Neuron to = neuronMap.get(conn.getOutNodeId());
                if (from != null && to != null) {
                    adjacencyList.get(from).add(to);
                    inDegree.put(to, inDegree.get(to) + 1);
                }
            }
        }

        Queue<Neuron> queue = new LinkedList<>();
        for (Neuron n : neuronMap.values()) {
            if (inDegree.get(n) == 0) {
                queue.add(n);
            }
        }

        List<Neuron> sortedList = new ArrayList<>();
        while (!queue.isEmpty()) {
            Neuron u = queue.poll();
            sortedList.add(u);
            for (Neuron v : adjacencyList.get(u)) {
                inDegree.put(v, inDegree.get(v) - 1);
                if (inDegree.get(v) == 0) {
                    queue.add(v);
                }
            }
        }

        if (sortedList.size() != neuronMap.size()) {
            throw new IllegalStateException("A cycle was detected in a network that was supposed to be feed-forward.");
        }
        return sortedList;
    }
}