package de.mkoehler.neat.config;

public class NEATConfig {
    // Population and Speciation
    public int populationSize = 150;
    public double compatibilityThreshold = 3.0;
    public double C1_EXCESS = 1.0;
    public double C2_DISJOINT = 1.0;
    public double C3_WEIGHTS = 0.4;
    public int speciesStagnationLimit = 15;
    public double speciesElitism = 0.05; // Keep the top 5% of a species

    // Mutation Rates
    public double crossoverRate = 0.75; // Chance for a child to be from crossover
    public double mutateWeightRate = 0.8;
    public double weightShiftRate = 0.9; // 90% of weight mutations are shifts, 10% are random
    public double weightShiftStrength = 0.1;
    public double addConnectionRate = 0.05;
    public double addNodeRate = 0.03;
    public double toggleEnableRate = 0.01;

    /** The number of attempts to find a valid new connection during mutation. */
    public int addConnectionAttempts = 20;

    /** The range for a new connection's weight. A value of 2.0 means weights will be in [-2.0, 2.0]. */
    public double newConnectionWeightRange = 2.0;

    /** The weight of the new connection leading into a newly added node. Usually 1.0. */
    public double addNodeNewLinkWeight = 1.0;

    // Network
    public int inputNodes;
    public int outputNodes;

    public NEATConfig(int inputNodes, int outputNodes) {
        this.inputNodes = inputNodes;
        this.outputNodes = outputNodes;
    }
}