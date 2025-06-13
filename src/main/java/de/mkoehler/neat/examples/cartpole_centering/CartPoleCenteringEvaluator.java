package de.mkoehler.neat.examples.cartpole_centering;

import de.mkoehler.neat.config.NEATConfig;
import de.mkoehler.neat.core.Genome;
import de.mkoehler.neat.evolution.FitnessEvaluator;
import de.mkoehler.neat.network.NeuralNetwork;
import de.mkoehler.neat.examples.cartpole.CartPoleEnvironment; // We reuse the environment

import java.util.List;

/**
 * A more advanced FitnessEvaluator for the Cart-Pole problem.
 * <p>
 * This evaluator teaches the agent not just to balance the pole, but to do so
 * while keeping the cart close to the center of the track. This introduces a
 * trade-off, making the problem significantly harder.
 * </p>
 * <p>
 * Fitness is calculated as the sum of scores from each time step. The score for
 * a single step is 1.0 (for surviving) minus a penalty for being far from the center.
 * This provides a continuous incentive to stay centered.
 * </p>
 */
public class CartPoleCenteringEvaluator implements FitnessEvaluator {

    public static final int MAX_TIME_STEPS = 5000;
    private static final double X_THRESHOLD = 2.4; // Must match the value in the environment

    private final NEATConfig config;

    public CartPoleCenteringEvaluator(NEATConfig config) {
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

                double[] state = env.getState();
                double[] output = net.activate(state);
                int action = (output[0] < 0.5) ? 0 : 1;
                env.update(action);

                // Calculate the score for this step
                double x_position = state[0];
                double normalizedDistance = Math.abs(x_position / X_THRESHOLD);
                // The score is higher when the cart is closer to the center.
                double stepScore = 1.0 - normalizedDistance;
                totalFitness += stepScore;
            }

            genome.setFitness(totalFitness);
        }
    }
}