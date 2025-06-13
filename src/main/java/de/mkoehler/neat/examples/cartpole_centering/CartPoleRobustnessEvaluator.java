package de.mkoehler.neat.examples.cartpole_centering;

import de.mkoehler.neat.config.NEATConfig;
import de.mkoehler.neat.core.Genome;
import de.mkoehler.neat.evolution.FitnessEvaluator;
import de.mkoehler.neat.network.NeuralNetwork;
import de.mkoehler.neat.examples.cartpole.CartPoleEnvironment;

import java.util.List;
import java.util.Random;

/**
 * The most advanced evaluator for the Cart-Pole problem.
 * <p>
 * This evaluator not only rewards centering but also actively introduces random
 * disturbances during the simulation. This forces the evolution of truly robust
 * controllers that can recover from unexpected events.
 * </p>
 */
public class CartPoleRobustnessEvaluator implements FitnessEvaluator {

    public static final int MAX_TIME_STEPS = 5000;
    private static final double X_THRESHOLD = 2.4;

    // --- New parameters for robustness training ---
    private static final double DISTURBANCE_PROBABILITY = 0.01; // 1% chance of a kick each step
    private static final double DISTURBANCE_MAGNITUDE = 0.8;    // How strong the kick is

    private final Random random = new Random();

    private final NEATConfig config;

    public CartPoleRobustnessEvaluator(NEATConfig config) {
        this.config = config;
    }

    @Override
    public void evaluatePopulation(List<Genome> population) {
        for (Genome genome : population) {
            NeuralNetwork net;
            try {
                net = NeuralNetwork.create(genome, config);
            } catch (IllegalStateException e) {
                genome.setFitness(0);
                continue;
            }

            CartPoleEnvironment env = new CartPoleEnvironment();
            double totalFitness = 0.0;

            for (int i = 0; i < MAX_TIME_STEPS; i++) {
                if (env.isDone()) {
                    break;
                }

                // --- NEW: Apply a random disturbance ---
                if (random.nextDouble() < DISTURBANCE_PROBABILITY) {
                    double impulse = (random.nextDouble() - 0.5) * DISTURBANCE_MAGNITUDE;
                    env.applyDisturbance(impulse);
                }
                // ------------------------------------

                double[] state = env.getState();
                double[] output = net.activate(state);
                int action = (output[0] < 0.5) ? 0 : 1;
                env.update(action);

                double x_position = state[0];
                double normalizedDistance = Math.abs(x_position / X_THRESHOLD);
                double stepScore = 1.0 - normalizedDistance;
                totalFitness += stepScore;
            }

            genome.setFitness(totalFitness);
        }
    }
}