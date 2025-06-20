package de.mkoehler.neat.config;

import de.mkoehler.neat.network.ActivationFunction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.function.DoubleUnaryOperator;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class NEATConfig {

    // Population and Speciation
    @Builder.Default private int populationSize = 150;
    @Builder.Default private double compatibilityThreshold = 3.0;
    @Builder.Default private double C1_EXCESS = 1.0;
    @Builder.Default private double C2_DISJOINT = 1.0;
    @Builder.Default private double C3_WEIGHTS = 0.4;
    @Builder.Default private int speciesStagnationLimit = 15;
    @Builder.Default private double speciesElitism = 0.05; // Keep the top 5% of a species

    // Mutation Rates
    @Builder.Default private double crossoverRate = 0.75; // Chance for a child to be from crossover
    @Builder.Default private double mutateWeightRate = 0.8;
    @Builder.Default private double weightShiftRate = 0.9; // 90% of weight mutations are shifts, 10% are random
    @Builder.Default private double weightShiftStrength = 0.1;
    @Builder.Default private double addConnectionRate = 0.05;
    @Builder.Default private double addNodeRate = 0.03;
    @Builder.Default private double toggleEnableRate = 0.01;

    /** The number of attempts to find a valid new connection during mutation. */
    @Builder.Default private int addConnectionAttempts = 20;

    /** The range for a new connection's weight. A value of 2.0 means weights will be in [-2.0, 2.0]. */
    @Builder.Default private double newConnectionWeightRange = 2.0;

    /** The weight of the new connection leading into a newly added node. Usually 1.0. */
    @Builder.Default private double addNodeNewLinkWeight = 1.0;

    /** If true, allows the creation of recurrent connections during mutation. */
    @Builder.Default private boolean allowRecurrent = false;

    // Network
    @Builder.Default private int inputNodes = 2;
    @Builder.Default private int outputNodes = 1;

    /**
     * The activation function to be used by all hidden neurons.
     * Defaults to the traditional {@link ActivationFunction#SIGMOID}.
     */
    @Builder.Default
    private DoubleUnaryOperator hiddenActivationFunction = ActivationFunction.SIGMOID;

    /**
     * The activation function to be used by all output neurons.
     * Defaults to the traditional {@link ActivationFunction#SIGMOID}.
     */
    @Builder.Default
    private DoubleUnaryOperator outputActivationFunction = ActivationFunction.SIGMOID;

}