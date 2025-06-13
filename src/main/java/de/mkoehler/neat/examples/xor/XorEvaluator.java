package de.mkoehler.neat.examples.xor;

import de.mkoehler.neat.config.NEATConfig;
import de.mkoehler.neat.core.Genome;
import de.mkoehler.neat.evolution.FitnessEvaluator;
import de.mkoehler.neat.network.NeuralNetwork;

import java.util.List;

public class XorEvaluator implements FitnessEvaluator {
    private final double[][] inputs = {{0, 0}, {0, 1}, {1, 0}, {1, 1}};
    private final double[][] outputs = {{0}, {1}, {1}, {0}};

    private final NEATConfig config;

    public XorEvaluator(NEATConfig config) {
        this.config = config;
    }

    @Override
    public void evaluatePopulation(List<Genome> population) {
        for (Genome genome : population) {
            NeuralNetwork net;
            try {
                // This is where the exception occurs
                net = NeuralNetwork.create(genome, config);
            } catch (IllegalStateException e) {
                System.err.println("--- FATAL: Cycle detected in genome ---");
                System.err.println(genome.getTopologyString());
                System.err.println("------------------------------------");
                // Re-throw the exception to halt execution
                throw e;
            }

            double error = 0.0;
            for (int i = 0; i < inputs.length; i++) {
                double[] networkOutput = net.activate(inputs[i]);
                error += Math.pow(outputs[i][0] - networkOutput[0], 2);
            }
            double fitness = 4.0 - error;
            genome.setFitness(fitness);
        }
    }
}