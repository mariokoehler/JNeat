// File: src/main/java/de/mkoehler/neat/network/Synapse.java
package de.mkoehler.neat.network;

import lombok.Getter;

/**
 * Represents a directed, weighted connection between two {@link Neuron} instances.
 * <p>
 * A Synapse is the physical manifestation of a {@code ConnectionGene} from the
 * genome. It links a 'from' neuron to a 'to' neuron (the 'to' neuron holds
 * the reference to this synapse in its input list).
 * </p>
 * The primary role of a synapse is to transmit a signal, which is the output
 * of its source neuron multiplied by the synapse's weight.
 */
@Getter
public class Synapse {

    /**
     * The source neuron from which this synapse originates.
     * @return The {@link Neuron} instance that provides the input signal for this synapse.
     */
    private final Neuron fromNeuron;

    /**
     * The weight of this connection. This value multiplies the output of the
     * {@code fromNeuron} to determine the signal strength.
     * <p>
     * A positive weight is excitatory, while a negative weight is inhibitory.
     * </p>
     * @return The double value of the connection weight.
     */
    private final double weight;

    /**
     * Constructs a new Synapse.
     *
     * @param fromNeuron The source {@link Neuron} for this connection.
     * @param weight     The connection's weight, which modulates the signal.
     */
    public Synapse(Neuron fromNeuron, double weight) {
        this.fromNeuron = fromNeuron;
        this.weight = weight;
    }

    /**
     * Calculates the value transmitted by this synapse.
     * <p>
     * This is calculated as: {@code fromNeuron.getOutputValue() * this.weight}.
     * </p>
     * @return The weighted output value from the source neuron.
     */
    public double getWeightedValue() {
        return fromNeuron.getOutputValue() * weight;
    }
}