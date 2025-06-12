package de.mkoehler.neat.network;

import de.mkoehler.neat.core.ConnectionGene;
import de.mkoehler.neat.core.Genome;
import de.mkoehler.neat.core.NodeGene;
import de.mkoehler.neat.core.NodeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NeuralNetworkTest {

    private Genome simpleGenome;

    @BeforeEach
    void setUp() {
        // A minimal genome: 2 inputs (0,1), 1 output (2), one connection from 0->2
        simpleGenome = new Genome();
        simpleGenome.addNodeGene(new NodeGene(0, NodeType.INPUT));
        simpleGenome.addNodeGene(new NodeGene(1, NodeType.INPUT));
        simpleGenome.addNodeGene(new NodeGene(2, NodeType.OUTPUT));
        simpleGenome.addConnectionGene(new ConnectionGene(0, 2, 0.5, true, 0));
    }

    /**
     * <h3>Test Scenario: Basic Network Creation</h3>
     * <p>
     * This test verifies that the {@code create} factory method can successfully build
     * a {@code NeuralNetwork} instance from a simple, valid genome without throwing errors.
     * </p>
     * <b>Steps:</b>
     * <ol>
     *   <li>Use a pre-configured simple genome.</li>
     *   <li>Call {@code NeuralNetwork.create(genome)}.</li>
     * </ol>
     * <b>Expected Outcome:</b>
     * <p>
     * The method should return a non-null {@code NeuralNetwork} object.
     * </p>
     */
    @Test
    @DisplayName("Should create a minimal network from a simple genome")
    void testCreateMinimalNetwork() {
        // Act & Assert
        assertNotNull(NeuralNetwork.create(simpleGenome),
                "Network creation should not fail for a valid minimal genome.");
    }

    /**
     * <h3>Test Scenario: Basic Network Activation</h3>
     * <p>
     * This test checks the feed-forward activation of the minimal network.
     * </p>
     * <b>Steps:</b>
     * <ol>
     *   <li>Create a network from the simple genome (input 0 -> output 2, weight 0.5).</li>
     *   <li>Call {@code activate} with input values {@code [1.0, 0.0]}.</li>
     * </ol>
     * <b>Expected Outcome:</b>
     * <p>
     * The output neuron should receive a summed input of {@code 1.0 * 0.5 = 0.5}.
     * The final output should be {@code sigmoid(0.5)}.
     * </p>
     */
    @Test
    @DisplayName("Should activate a minimal network correctly")
    void testActivateMinimalNetwork() {
        // Arrange
        NeuralNetwork net = NeuralNetwork.create(simpleGenome);

        // Act
        double[] output = net.activate(new double[]{1.0, 0.0});

        // Assert
        double expectedOutput = ActivationFunction.SIGMOID.applyAsDouble(0.5);
        assertEquals(1, output.length);
        assertEquals(expectedOutput, output[0], 0.001);
    }

    /**
     * <h3>Test Scenario: Activation with a Hidden Node</h3>
     * <p>
     * This test validates the correctness of the topological sort and activation flow
     * in a network with a hidden layer.
     * </p>
     * <b>Steps:</b>
     * <ol>
     *   <li>Create a genome with a hidden node (0 -> 3 -> 2).</li>
     *   <li>Create the network from this genome.</li>
     *   <li>Call {@code activate} with input {@code [1.0, 0.0]}.</li>
     * </ol>
     * <b>Expected Outcome:</b>
     * <p>
     * The computation should flow correctly from the input node, through the hidden node,
     * to the output node, with activation functions applied at each step.
     * </p>
     * <p>
     * Hidden Node 3 input: {@code 1.0 * 0.5 = 0.5} -> Hidden Output: {@code sigmoid(0.5)}
     * <br>
     * Output Node 2 input: {@code sigmoid(0.5) * 0.8} -> Final Output: {@code sigmoid(sigmoid(0.5) * 0.8)}
     * </p>
     */
    @Test
    @DisplayName("Should activate a network with a hidden node correctly")
    void testActivateNetworkWithHiddenNode() {
        // Arrange
        Genome hiddenNodeGenome = new Genome();
        hiddenNodeGenome.addNodeGene(new NodeGene(0, NodeType.INPUT));
        hiddenNodeGenome.addNodeGene(new NodeGene(1, NodeType.INPUT));
        hiddenNodeGenome.addNodeGene(new NodeGene(2, NodeType.OUTPUT));
        hiddenNodeGenome.addNodeGene(new NodeGene(3, NodeType.HIDDEN));
        hiddenNodeGenome.addConnectionGene(new ConnectionGene(0, 3, 0.5, true, 0));
        hiddenNodeGenome.addConnectionGene(new ConnectionGene(3, 2, 0.8, true, 1));
        NeuralNetwork net = NeuralNetwork.create(hiddenNodeGenome);

        // Act
        double[] output = net.activate(new double[]{1.0, 0.0});

        // Assert
        double hiddenNodeOutput = ActivationFunction.SIGMOID.applyAsDouble(1.0 * 0.5);
        double finalOutput = ActivationFunction.SIGMOID.applyAsDouble(hiddenNodeOutput * 0.8);
        assertEquals(finalOutput, output[0], 0.001);
    }

    /**
     * <h3>Test Scenario: Cycle Detection in Network Graph</h3>
     * <p>
     * This test ensures that the network creation process identifies cycles in the
     * genome's topology and throws an appropriate exception. A network with a cycle
     * cannot be topologically sorted and thus cannot be activated in a feed-forward manner.
     * </p>
     * <b>Steps:</b>
     * <ol>
     *   <li>Create a genome with a cycle (e.g., node 3 -> 4, node 4 -> 3).</li>
     *   <li>Attempt to create a network from this genome.</li>
     * </ol>
     * <b>Expected Outcome:</b>
     * <p>
     * The call to {@code NeuralNetwork.create()} should throw an {@code IllegalStateException}.
     * </p>
     */
    @Test
    @DisplayName("Should throw IllegalStateException when genome describes a cycle")
    void testCreateNetworkWithCycle() {
        // Arrange
        Genome cycleGenome = new Genome();
        cycleGenome.addNodeGene(new NodeGene(0, NodeType.INPUT));
        cycleGenome.addNodeGene(new NodeGene(1, NodeType.OUTPUT));
        cycleGenome.addNodeGene(new NodeGene(3, NodeType.HIDDEN));
        cycleGenome.addNodeGene(new NodeGene(4, NodeType.HIDDEN));
        cycleGenome.addConnectionGene(new ConnectionGene(3, 4, 1.0, true, 0));
        cycleGenome.addConnectionGene(new ConnectionGene(4, 3, 1.0, true, 1)); // This creates the cycle
        cycleGenome.addConnectionGene(new ConnectionGene(0, 3, 1.0, true, 2));
        cycleGenome.addConnectionGene(new ConnectionGene(4, 1, 1.0, true, 3));


        // Act & Assert
        assertThrows(IllegalStateException.class,
                () -> NeuralNetwork.create(cycleGenome),
                "Should throw an exception for a network with a cycle.");
    }

    /**
     * <h3>Test Scenario: Mismatched Input Size</h3>
     * <p>
     * This test verifies that the {@code activate} method performs input validation.
     * </p>
     * <b>Steps:</b>
     * <ol>
     *   <li>Create a valid network with 2 input nodes.</li>
     *   <li>Call {@code activate} with an array of size 1.</li>
     * </ol>
     * <b>Expected Outcome:</b>
     * <p>
     * The call should throw an {@code IllegalArgumentException}.
     * </p>
     */
    @Test
    @DisplayName("Activate should throw IllegalArgumentException for incorrect input size")
    void testActivateWithIncorrectInputSize() {
        // Arrange
        NeuralNetwork net = NeuralNetwork.create(simpleGenome); // This network expects 2 inputs
        double[] badInput = {1.0};

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> net.activate(badInput),
                "Should throw an exception for mismatched input array size.");
    }
}