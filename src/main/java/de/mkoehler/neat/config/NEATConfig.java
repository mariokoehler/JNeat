// File: src/main/java/de/mkoehler/neat/config/NEATConfig.java
package de.mkoehler.neat.config;

import de.mkoehler.neat.network.ActivationFunction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.function.DoubleUnaryOperator;

/**
 * A configuration class for the NEAT algorithm, built using the Builder pattern.
 * <p>
 * This class holds all the tunable parameters that control the evolutionary process,
 * from population and speciation settings to mutation rates and network topology.
 * </p>
 * Example usage:
 * <pre>{@code
 * NEATConfig config = NEATConfig.builder()
 *     .populationSize(200)
 *     .compatibilityThreshold(4.0)
 *     .hiddenActivationFunction(ActivationFunction.LEAKY_RELU)
 *     .build();
 * }</pre>
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class NEATConfig {

    // =========================================================================
    // === Population and Speciation ===========================================
    // =========================================================================

    /**
     * The total number of genomes in the population for each generation.
     * <p>
     * A larger population increases genetic diversity and the chances of finding a
     * solution but significantly increases computation time per generation.
     * </p>
     * <b>Guidance:</b> Typical values range from 150 to 300. For very complex
     * problems, values up to 1000 might be used.
     */
    @Builder.Default private int populationSize = 150;

    /**
     * The compatibility distance threshold (δ_t) for grouping genomes into species.
     * <p>
     * If a genome's distance to a species' representative is less than this
     * threshold, it is placed in that species. A lower value leads to more,
     * smaller species, promoting diversity. A higher value leads to fewer,
     * larger species, promoting faster convergence.
     * </p>
     * <b>Guidance:</b> This value often requires tuning. A good starting point is 3.0 or 4.0.
     */
    @Builder.Default private double compatibilityThreshold = 3.0;

    /**
     * The coefficient (c1) for excess genes in the compatibility distance formula.
     * Excess genes are those with innovation numbers beyond the maximum of the
     * other parent.
     * <p>
     * This parameter controls how much having extra, un-matched genes at the
     * end of a genome contributes to the incompatibility distance.
     * </p>
     * <b>Guidance:</b> Usually kept at 1.0.
     */
    @Builder.Default private double C1_EXCESS = 1.0;

    /**
     * The coefficient (c2) for disjoint genes in the compatibility distance formula.
     * Disjoint genes are those that are not excess but do not have a matching
     * innovation number in the other parent.
     * <p>
     * This parameter controls how much having different genes in the middle of
     * a genome contributes to the incompatibility distance.
     * </p>
     * <b>Guidance:</b> Usually kept at 1.0.
     */
    @Builder.Default private double C2_DISJOINT = 1.0;

    /**
     * The coefficient (c3) for the average weight difference of matching genes.
     * <p>
     * This parameter controls how much the difference in connection weights for
     * shared genes contributes to the incompatibility distance.
     * </p>
     * <b>Guidance:</b> A value between 0.4 and 1.0 is common. Lower values make
     * structural differences more important than weight differences.
     */
    @Builder.Default private double C3_WEIGHTS = 0.4;

    /**
     * The number of generations a species can go without improving its top fitness
     * before it is considered stagnant and removed from the population.
     * <p>
     * This prevents the algorithm from wasting resources on species that have
     * reached a dead end.
     * </p>
     * <b>Guidance:</b> 15 to 20 is a good starting range.
     */
    @Builder.Default private int speciesStagnationLimit = 15;

    /**
     * The percentage of the top-performing genomes in a species that are carried
     * over to the next generation without any mutation.
     * <p>
     * Elitism ensures that the best solutions found so far are not lost due to
     * destructive mutations or crossover.
     * </p>
     * <b>Guidance:</b> A small value like 0.01 (1%) or 0.05 (5%) is typical.
     */
    @Builder.Default private double speciesElitism = 0.05;

    // =========================================================================
    // === Mutation Rates ======================================================
    // =========================================================================

    /**
     * The probability that an offspring will be created via crossover between two
     * parents. If this check fails (1 - crossoverRate), the child will be a
     * direct (mutated) clone of a single parent.
     * <p>
     * <b>Guidance:</b> A high value like 0.75 is common, favoring the mixing of genes.
     */
    @Builder.Default private double crossoverRate = 0.75;

    /**
     * The overall probability that an offspring's connection weights will be mutated.
     * <p>
     * <b>Guidance:</b> This should be high (e.g., 0.8) as weight tuning is the most
     * frequent form of evolution.
     */
    @Builder.Default private double mutateWeightRate = 0.8;

    /**
     * When a weight is mutated, this is the probability that it will be "shifted"
     * (perturbed) rather than being completely replaced with a new random value.
     * <p>
     * The alternative (1 - weightShiftRate) is a complete replacement. Shifting
     * is better for fine-tuning, while replacement allows for more drastic exploration.
     * </p>
     * <b>Guidance:</b> A high value (e.g., 0.9) is standard.
     */
    @Builder.Default private double weightShiftRate = 0.9;

    /**
     * The strength of the perturbation when a weight is shifted. The new weight
     * will be {@code old_weight + (random * 2 - 1) * weightShiftStrength}.
     * <p>
     * <b>Guidance:</b> A small value like 0.1 allows for fine-tuning.
     */
    @Builder.Default private double weightShiftStrength = 0.1;

    /**
     * The probability of adding a new connection (synapse) between two previously
     * unconnected nodes in a single genome mutation.
     * <p>
     * This is a "structural" mutation that increases network complexity.
     * </p>
     * <b>Guidance:</b> Should be low, typically between 0.01 and 0.05.
     */
    @Builder.Default private double addConnectionRate = 0.05;

    /**
     * The probability of adding a new node to the network. This mutation works by
     * "splitting" an existing connection in two, with the new node placed in the middle.
     * <p>
     * This is the primary way NEAT grows the network topology.
     * </p>
     * <b>Guidance:</b> Should be low, typically between 0.01 and 0.03.
     */
    @Builder.Default private double addNodeRate = 0.03;

    /**
     * The probability that an existing connection's "enabled" status will be toggled.
     * This allows the network to experiment with temporarily removing connections
     * without losing the underlying gene.
     * <p>
     * <b>Guidance:</b> Very low, e.g., 0.01, as it can be a highly disruptive mutation.
     */
    @Builder.Default private double toggleEnableRate = 0.01;

    // =========================================================================
    // === Structural Mutation Details =========================================
    // =========================================================================

    /**
     * The number of attempts the mutation algorithm gets to find two valid nodes
     * to form a new connection between.
     * <p>
     * This prevents infinite loops in very dense networks where finding a valid
     * new connection might be difficult.
     * </p>
     */
    @Builder.Default private int addConnectionAttempts = 20;

    /**
     * The range for a new connection's weight, or for a weight that is fully
     * reset. A value of 2.0 means new weights will be in the range [-2.0, 2.0].
     */
    @Builder.Default private double newConnectionWeightRange = 2.0;

    /**
     * When adding a new node by splitting an existing connection, this is the weight
     * of the new connection leading *into* the new node. The original weight is
     * applied to the connection *from* the new node.
     * <p>
     * <b>Guidance:</b> Usually set to 1.0 to ensure the new node initially has minimal
     * impact on the network's behavior until its own weights are tuned.
     */
    @Builder.Default private double addNodeNewLinkWeight = 1.0;

    /**
     * If true, allows the creation of recurrent connections during the
     * "add connection" mutation (i.e., a connection from a later node in the
     * topology back to an earlier one).
     * <p>
     * This is necessary for tasks requiring memory. When enabled, the network
     * will use a slower, multi-step activation process.
     * </p>
     */
    @Builder.Default private boolean allowRecurrent = false;

    // =========================================================================
    // === Network Topology & Activation =======================================
    // =========================================================================

    /** The number of input nodes in the network. Must match the problem's state space. */
    @Builder.Default private int inputNodes = 2;

    /** The number of output nodes in the network. Must match the problem's action space. */
    @Builder.Default private int outputNodes = 1;

    /**
     * If true, the initial population will be created with genomes where every
     * input node is connected to every output node. This is useful for problems
     * where all inputs are expected to be relevant from the start.
     * <p>
     * If false (the default), initial genomes will have a single random connection,
     * following the traditional NEAT approach of starting minimally.
     * </p>
     */
    @Builder.Default private boolean startWithFullyConnectedTopology = false;

    /**
     * The activation function to be used by all hidden neurons.
     * <p>
     * <b>Guidance:</b> {@link ActivationFunction#SIGMOID} is traditional for NEAT.
     * Modern alternatives like {@link ActivationFunction#LEAKY_RELU} or
     * {@link ActivationFunction#TANH} may lead to faster convergence.
     */
    @Builder.Default private DoubleUnaryOperator hiddenActivationFunction = ActivationFunction.SIGMOID;

    /**
     * The activation function to be used by all output neurons.
     * <p>
     * <b>Guidance:</b> The choice depends on the desired output range.
     * <ul>
     *   <li>{@link ActivationFunction#SIGMOID} for an output in [0, 1].</li>
     *   <li>{@link ActivationFunction#TANH} for an output in [-1, 1].</li>
     * </ul>
     */
    @Builder.Default private DoubleUnaryOperator outputActivationFunction = ActivationFunction.SIGMOID;
}