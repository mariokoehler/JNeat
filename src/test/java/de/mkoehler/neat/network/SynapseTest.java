package de.mkoehler.neat.network;

import de.mkoehler.neat.core.NodeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SynapseTest {

    /**
     * <h3>Test Scenario: Get Weighted Value</h3>
     * <p>
     * This test verifies that a synapse correctly computes its weighted output value.
     * </p>
     * <b>Steps:</b>
     * <ol>
     *   <li>Create a source neuron and set its output value.</li>
     *   <li>Create a synapse with a specific weight, originating from the source neuron.</li>
     *   <li>Call the {@code getWeightedValue()} method on the synapse.</li>
     * </ol>
     * <b>Expected Outcome:</b>
     * <p>
     * The returned value should be the product of the source neuron's output value
     * and the synapse's weight.
     * </p>
     * <p>
     * Calculation: {@code 0.8 * 0.5 = 0.4}
     * </p>
     */
    @Test
    @DisplayName("Synapse should return the source neuron's output multiplied by its weight")
    void testGetWeightedValue() {
        // Arrange
        Neuron fromNeuron = new Neuron(0, NodeType.INPUT, ActivationFunction.SIGMOID);
        fromNeuron.setOutputValue(0.8);
        Synapse synapse = new Synapse(fromNeuron, 0.5);

        // Act
        double weightedValue = synapse.getWeightedValue();

        // Assert
        assertEquals(0.4, weightedValue, 0.001,
                "Synapse did not return the correct weighted value.");
    }
}