package de.mkoehler.neat.network;

public class Synapse {
    private final Neuron fromNeuron;
    private final double weight;

    public Synapse(Neuron fromNeuron, double weight) {
        this.fromNeuron = fromNeuron;
        this.weight = weight;
    }

    public double getWeightedValue() {
        return fromNeuron.getOutputValue() * weight;
    }
}