package de.mkoehler.neat.examples.cartpole;

import de.mkoehler.neat.core.Genome;
import de.mkoehler.neat.evolution.FitnessEvaluator;
import de.mkoehler.neat.network.NeuralNetwork;

import java.util.List;

/**
 * A FitnessEvaluator for the Cart-Pole balancing problem.
 * Fitness is determined by how many time steps a genome's neural network
 * can keep the pole balanced.
 */
public class CartPoleEvaluator implements FitnessEvaluator {

    public static final int MAX_TIME_STEPS = 5000;

    @Override
    public void evaluatePopulation(List<Genome> population) {
        for (Genome genome : population) {
            NeuralNetwork net;
            try {
                net = NeuralNetwork.create(genome);
            } catch (IllegalStateException e) {
                // This genome describes an invalid (cyclic) network
                System.err.println("Skipping evaluation of genome with invalid topology: " + e.getMessage());
                genome.setFitness(0);
                continue;
            }

            CartPoleEnvironment env = new CartPoleEnvironment();
            int timeStepsSurvived = 0;

            for (int i = 0; i < MAX_TIME_STEPS; i++) {
                if (env.isDone()) {
                    break;
                }

                double[] state = env.getState();
                double[] output = net.activate(state);
                int action = (output[0] < 0.5) ? 0 : 1;

                env.update(action);
                timeStepsSurvived++;
            }

            genome.setFitness(timeStepsSurvived);
        }
    }
}