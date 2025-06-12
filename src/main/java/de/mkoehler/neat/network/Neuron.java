package de.mkoehler.neat.network;

import de.mkoehler.neat.core.NodeType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

public class Neuron {
    @Getter
    private final int id;
    @Getter
    private final NodeType type;
    private final List<Synapse> inputSynapses = new ArrayList<>();
    @Setter
    @Getter
    private double outputValue = 0.0;
    private final DoubleUnaryOperator activationFunction;

    public Neuron(int id, NodeType type, DoubleUnaryOperator activationFunction) {
        this.id = id;
        this.type = type;
        this.activationFunction = activationFunction;
    }

    public void calculate() {
        if (type == NodeType.INPUT) {
            return; // Input value is set directly
        }
        double sum = inputSynapses.stream().mapToDouble(Synapse::getWeightedValue).sum();
        this.outputValue = activationFunction.applyAsDouble(sum);
    }

    public void addInputSynapse(Synapse synapse) { this.inputSynapses.add(synapse); }
}