package de.mkoehler.neat.network;

import de.mkoehler.neat.core.NodeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NeuronTest {

    /**
     * <h3>Test Scenario: Neuron Calculation with Multiple Inputs</h3>
     * <p>
     * This test verifies that a non-input neuron correctly calculates its output value.
     * </p>
     * <b>Steps:</b>
     * <ol>
     *   <li>Create two source neurons and set their output values.</li>
     *   <li>Create a target neuron of type {@code HIDDEN}.</li>
     *   <li>Add two synapses to the target neuron, connecting it to the source neurons with defined weights.</li>
     *   <li>Call the {@code calculate()} method on the target neuron.</li>
     * </ol>
     * <b>Expected Outcome:</b>
     * <p>
     * The target neuron's output value should be the result of applying the sigmoid activation
     * function to the sum of the weighted inputs from its synapses.
     * </p>
     * <p>
     * Calculation: {@code sum = (source1.output * weight1) + (source2.output * weight2)}
     * <br>
     * {@code sum = (1.0 * 0.5) + (0.5 * -0.8) = 0.5 - 0.4 = 0.1}
     * <br>
     * {@code finalOutput = sigmoid(0.1) ≈ 0.621}
     * </p>
     */
    @Test
    @DisplayName("Neuron should correctly calculate its output from multiple weighted inputs")
    void testNeuronCalculation() {
        // Arrange: Create source neurons
        Neuron source1 = new Neuron(10, NodeType.INPUT, ActivationFunction.SIGMOID);
        source1.setOutputValue(1.0);
        Neuron source2 = new Neuron(11, NodeType.INPUT, ActivationFunction.SIGMOID);
        source2.setOutputValue(0.5);

        // Arrange: Create target neuron and connect synapses
        Neuron target = new Neuron(12, NodeType.HIDDEN, ActivationFunction.SIGMOID);
        target.addInputSynapse(new Synapse(source1, 0.5));
        target.addInputSynapse(new Synapse(source2, -0.8));

        // Act
        target.calculate();

        // Assert
        double expectedSum = (1.0 * 0.5) + (0.5 * -0.8); // 0.1
        double expectedOutput = ActivationFunction.SIGMOID.applyAsDouble(expectedSum);
        assertEquals(expectedOutput, target.getOutputValue(), 0.001,
                "Neuron calculation did not match expected value.");
    }

    /**
     * <h3>Test Scenario: Input Neuron Calculation</h3>
     * <p>
     * This test ensures that calling {@code calculate()} on an {@code INPUT} neuron
     * has no effect on its output value.
     * </p>
     * <b>Steps:</b>
     * <ol>
     *   <li>Create a neuron of type {@code INPUT}.</li>
     *   <li>Set its output value to a specific number.</li>
     *   <li>Call the {@code calculate()} method.</li>
     * </ol>
     * <b>Expected Outcome:</b>
     * <p>
     * The neuron's output value should remain unchanged, as input values are set externally
     * and are not calculated.
     * </p>
     */
    @Test
    @DisplayName("Calling calculate() on an INPUT neuron should not change its value")
    void testInputNeuronNoOpCalculate() {
        // Arrange
        Neuron inputNeuron = new Neuron(0, NodeType.INPUT, ActivationFunction.SIGMOID);
        inputNeuron.setOutputValue(0.75);

        // Act
        inputNeuron.calculate();

        // Assert
        assertEquals(0.75, inputNeuron.getOutputValue(),
                "Input neuron's value should not change after calling calculate().");
    }
}