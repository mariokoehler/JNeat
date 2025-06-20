// File: src/main/java/de/mkoehler/neat/network/Neuron.java
package de.mkoehler.neat.network;

import de.mkoehler.neat.core.NodeType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

/**
 * Represents a single neuron (or node) within the neural network phenotype.
 * <p>
 * A neuron holds a list of its incoming connections (synapses), its current
 * output value, and the activation function it uses to process its inputs.
 * The behavior of a neuron differs based on its {@link NodeType}:
 * <ul>
 *   <li><b>INPUT:</b> Its output value is set directly from the network's external inputs.
 *       It does not perform any calculations.</li>
 *   <li><b>HIDDEN:</b> It calculates its output by summing the weighted values of its
 *       incoming synapses and passing the result through an activation function.</li>
 *   <li><b>OUTPUT:</b> Behaves like a hidden neuron, but its final output value is
 *       part of the network's overall result.</li>
 * </ul>
 */
@Getter
public class Neuron {

    /**
     * The unique identifier for this neuron, corresponding to the
     * {@code NodeGene} ID from the genome.
     * @return The integer ID of the neuron.
     */
    private final int id;

    /**
     * The type of the neuron (INPUT, HIDDEN, or OUTPUT), which dictates its behavior.
     * @return The {@link NodeType} of the neuron.
     */
    private final NodeType type;

    /**
     * A list of all incoming synapses connecting to this neuron. This list is
     * empty for {@code INPUT} neurons.
     * @return A list of {@link Synapse} objects.
     */
    private final List<Synapse> inputSynapses = new ArrayList<>();

    /**
     * The current output value of this neuron. This value is the result of the
     * last activation calculation or is set directly for input neurons.
     * @param outputValue The new output value to set.
     * @return The current output value.
     */
    @Setter
    private double outputValue = 0.0;

    /** The activation function used to process the summed input. */
    private final DoubleUnaryOperator activationFunction;

    /**
     * Constructs a new Neuron.
     *
     * @param id The unique identifier for the neuron.
     * @param type The {@link NodeType} (e.g., INPUT, HIDDEN, OUTPUT).
     * @param activationFunction The mathematical function (e.g., sigmoid) to apply to the summed inputs.
     */
    public Neuron(int id, NodeType type, DoubleUnaryOperator activationFunction) {
        this.id = id;
        this.type = type;
        this.activationFunction = activationFunction;
    }

    /**
     * Calculates the neuron's output value based on its incoming synapses.
     * <p>
     * This method sums the weighted output of all connected input neurons
     * ({@code synapse.getWeightedValue()}) and then applies the neuron's
     * activation function to the sum.
     * <br>
     * <b>Note:</b> This method has no effect if the neuron's type is {@code INPUT},
     * as input values are set externally.
     */
    public void calculate() {
        if (type == NodeType.INPUT) {
            return; // Input value is set directly by the network
        }
        double sum = inputSynapses.stream().mapToDouble(Synapse::getWeightedValue).sum();
        this.outputValue = activationFunction.applyAsDouble(sum);
    }

    /**
     * Adds an incoming connection to this neuron.
     *
     * @param synapse The {@link Synapse} to add, which contains a reference
     *                to the source neuron and the connection weight.
     */
    public void addInputSynapse(Synapse synapse) {
        this.inputSynapses.add(synapse);
    }
}