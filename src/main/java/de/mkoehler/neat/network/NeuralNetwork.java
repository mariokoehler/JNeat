// File: src/main/java/de/mkoehler/neat/network/NeuralNetwork.java
package de.mkoehler.neat.network;

import de.mkoehler.neat.config.NEATConfig;
import de.mkoehler.neat.core.ConnectionGene;
import de.mkoehler.neat.core.Genome;
import de.mkoehler.neat.core.NodeGene;
import de.mkoehler.neat.core.NodeType;

import java.util.*;
import java.util.function.DoubleUnaryOperator;

/**
 * Represents the phenotype of a {@link Genome}, a functional neural network.
 * <p>
 * This class translates the genetic blueprint of nodes and connections from a
 * {@code Genome} into a network of {@link Neuron} and {@link Synapse} objects
 * that can be activated to process input and produce output.
 * </p>
 * The network can be constructed in one of two modes, determined by the
 * {@link NEATConfig#isAllowRecurrent()} setting:
 * <ul>
 *   <li><b>Feed-Forward:</b> If recurrence is disallowed, the network is
 *       topologically sorted. This allows for highly efficient, single-pass
 *       activation. An exception is thrown if the genome describes a cycle.</li>
 *   <li><b>Recurrent (RNN):</b> If recurrence is allowed, the network can contain
 *       cycles. Activation is performed over multiple time steps to allow signals
 *       to propagate through recurrent connections, giving the network a form of
 *       short-term memory.</li>
 * </ul>
 * Use the static factory method {@link #create(Genome, NEATConfig)} to construct an instance.
 */
public class NeuralNetwork {

    /** A list of all neurons in the network, used for recurrent activation. */
    private final List<Neuron> neurons;

    /**
     * A topologically sorted list of neurons, used for efficient feed-forward activation.
     * This list is {@code null} for recurrent networks.
     */
    private final List<Neuron> neuronsInOrder;

    /** A dedicated list of the network's input neurons for easy access. */
    private final List<Neuron> inputNeurons;

    /** A dedicated list of the network's output neurons for easy access. */
    private final List<Neuron> outputNeurons;

    /** A flag indicating whether this network supports recurrent connections. */
    private final boolean isRecurrent;

    /**
     * Private constructor to enforce creation via the static factory method.
     */
    private NeuralNetwork(List<Neuron> allNeurons, List<Neuron> sortedNeurons, List<Neuron> inputNeurons, List<Neuron> outputNeurons, boolean isRecurrent) {
        this.neurons = allNeurons;
        this.neuronsInOrder = sortedNeurons;
        this.inputNeurons = inputNeurons;
        this.outputNeurons = outputNeurons;
        this.isRecurrent = isRecurrent;
    }

    /**
     * Creates a {@code NeuralNetwork} instance from a given genome and configuration.
     * <p>
     * This factory method decodes the genome's genes into a network of neurons
     * and synapses. Based on the {@code allowRecurrent} flag in the config, it
     * will either prepare for fast feed-forward activation by topologically
     * sorting the neurons or set up for multi-step recurrent activation.
     * </p>
     *
     * @param genome The genome specifying the network's structure.
     * @param config The NEAT configuration, which determines if recurrence is allowed.
     * @return A ready-to-use {@code NeuralNetwork} instance.
     * @throws IllegalStateException if {@code config.isAllowRecurrent()} is false and the genome contains a cycle.
     */
    public static NeuralNetwork create(Genome genome, NEATConfig config) {
        Map<Integer, Neuron> neuronMap = new HashMap<>();
        List<Neuron> inputs = new ArrayList<>();
        List<Neuron> outputs = new ArrayList<>();
        List<Neuron> allNeurons = new ArrayList<>();

        // Create all neuron objects, assigning activation functions based on type
        for (NodeGene nodeGene : genome.getNodes().values()) {
            DoubleUnaryOperator activationFunction;
            switch (nodeGene.type()) {
                case HIDDEN -> activationFunction = config.getHiddenActivationFunction();
                case OUTPUT -> activationFunction = config.getOutputActivationFunction();
                case INPUT -> activationFunction = null; // Input neurons don't have one
                default -> throw new IllegalStateException("Unexpected value: " + nodeGene.type());
            }

            Neuron neuron = new Neuron(nodeGene.id(), nodeGene.type(), activationFunction);
            neuronMap.put(nodeGene.id(), neuron);
            allNeurons.add(neuron);
            if (nodeGene.type() == NodeType.INPUT) inputs.add(neuron);
            if (nodeGene.type() == NodeType.OUTPUT) outputs.add(neuron);
        }

        // Create synapses from enabled connection genes
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
            // For feed-forward networks, perform a topological sort for performance.
            // This also serves as a validation step to detect cycles.
            sortedNeurons = topologicalSort(neuronMap, genome);
        }

        return new NeuralNetwork(allNeurons, sortedNeurons, inputs, outputs, config.isAllowRecurrent());
    }

    /**
     * Activates the network with a default number of steps.
     * <p>
     * This is a convenience method that calls {@link #activate(double[], int)}.
     * The number of steps defaults to:
     * <ul>
     *   <li><b>1 step</b> for a feed-forward network.</li>
     *   <li><b>2 steps</b> for a recurrent network (to allow signals to pass through at least one recurrent loop).</li>
     * </ul>
     *
     * @param inputValues The input values for the network. Must match the number of input nodes.
     * @return The output values from the network's output neurons.
     * @throws IllegalArgumentException if the number of input values does not match the network's input layer.
     */
    public double[] activate(double[] inputValues) {
        int defaultSteps = this.isRecurrent ? 2 : 1;
        return activate(inputValues, defaultSteps);
    }

    /**
     * Activates the network over a specified number of time steps.
     * <p>
     * This method selects the appropriate activation strategy based on whether the
     * network is recurrent.
     * </p>
     * @param inputValues The input values for the network.
     * @param timeSteps   The number of iterations to propagate signals. For feed-forward
     *                    networks, this value is ignored and always treated as 1. For
     *                    recurrent networks, a value > 1 is needed to leverage memory.
     * @return The final output values from the output neurons.
     * @throws IllegalArgumentException if the number of input values does not match the network's input layer.
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

    /**
     * Performs a single-pass activation for a feed-forward network.
     * It processes neurons in their topologically sorted order.
     */
    private double[] activateFeedForward(double[] inputValues) {
        // Set input neuron values
        for (int i = 0; i < inputNeurons.size(); i++) {
            inputNeurons.get(i).setOutputValue(inputValues[i]);
        }

        // Calculate output for all other neurons in sorted order
        for (Neuron neuron : neuronsInOrder) {
            if (neuron.getType() != NodeType.INPUT) {
                neuron.calculate();
            }
        }

        // Collect results from output neurons
        return outputNeurons.stream().mapToDouble(Neuron::getOutputValue).toArray();
    }

    /**
     * Performs a multi-step activation for a recurrent network.
     * The state of the network from the previous step is used as input for the current step.
     */
    private double[] activateRecurrent(double[] inputValues, int timeSteps) {
        // Reset all neuron outputs before starting
        for (Neuron n : neurons) n.setOutputValue(0.0);

        for (int step = 0; step < timeSteps; step++) {
            // Store the output values from the *previous* step
            Map<Integer, Double> previousStepValues = new HashMap<>();
            for (Neuron n : neurons) {
                previousStepValues.put(n.getId(), n.getOutputValue());
            }

            // Clamp input neurons to their new values for this step
            for (int i = 0; i < inputNeurons.size(); i++) {
                inputNeurons.get(i).setOutputValue(inputValues[i]);
            }

            // Calculate new outputs for hidden and output neurons
            for (Neuron n : neurons) {
                if (n.getType() != NodeType.INPUT) {
                    double sum = 0;
                    for (Synapse s : n.getInputSynapses()) {
                        // Use the output value from the previous step for calculations
                        double fromValue = previousStepValues.getOrDefault(s.getFromNeuron().getId(), 0.0);
                        sum += fromValue * s.getWeight();
                    }
                    n.setOutputValue(ActivationFunction.SIGMOID.applyAsDouble(sum));
                }
            }
        }

        // Collect final results from output neurons
        return outputNeurons.stream().mapToDouble(Neuron::getOutputValue).toArray();
    }

    /**
     * Sorts the neurons of a feed-forward network topologically using Kahn's Algorithm.
     * This ensures that when we activate the network, each neuron's inputs have
     * already been calculated.
     * @param neuronMap A map of neuron IDs to Neuron objects.
     * @param genome    The genome, used to find connections.
     * @return A new list of {@link Neuron} objects in topological order.
     * @throws IllegalStateException if a cycle is detected in the graph.
     */
    private static List<Neuron> topologicalSort(Map<Integer, Neuron> neuronMap, Genome genome) {
        Map<Neuron, Integer> inDegree = new HashMap<>();
        Map<Neuron, List<Neuron>> adjacencyList = new HashMap<>();

        for (Neuron n : neuronMap.values()) {
            inDegree.put(n, 0);
            adjacencyList.put(n, new ArrayList<>());
        }

        // Build adjacency list and calculate in-degrees
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

        // Initialize queue with all nodes that have an in-degree of 0 (e.g., input neurons)
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

            // For each neighbor, decrement its in-degree and add to queue if it becomes 0
            for (Neuron v : adjacencyList.get(u)) {
                inDegree.put(v, inDegree.get(v) - 1);
                if (inDegree.get(v) == 0) {
                    queue.add(v);
                }
            }
        }

        // If the sorted list doesn't contain all neurons, a cycle must exist.
        if (sortedList.size() != neuronMap.size()) {
            throw new IllegalStateException("A cycle was detected in a network that was supposed to be feed-forward.");
        }
        return sortedList;
    }
}