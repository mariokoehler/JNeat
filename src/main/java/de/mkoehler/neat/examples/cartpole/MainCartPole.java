package de.mkoehler.neat.examples.cartpole;

import de.mkoehler.neat.config.NEATConfig;
import de.mkoehler.neat.core.Genome;
import de.mkoehler.neat.evolution.FitnessEvaluator;
import de.mkoehler.neat.evolution.Population;
import de.mkoehler.neat.network.NeuralNetwork;

public class MainCartPole {

    public static void main(String[] args) {
        // 1. Configure NEAT for the Cart-Pole problem (4 inputs, 1 output)
        NEATConfig config = new NEATConfig(4, 1);
        config.populationSize = 200; // A slightly larger population can be helpful

        // 2. Create our custom fitness evaluator
        FitnessEvaluator evaluator = new CartPoleEvaluator(config);

        // 3. Create a population
        Population population = new Population(config, evaluator);

        // 4. Evolve
        Genome bestGenome = null;
        int generations = 300;
        for (int i = 0; i < generations; i++) {
            population.evolve();
            bestGenome = population.getBestGenome();

            System.out.printf("Generation: %d, Best Fitness (steps): %.0f, Species: %d%n",
                    population.getGeneration(),
                    bestGenome.getFitness(),
                    population.getSpeciesCount());

            // Stop if a solution is found (survives for the maximum time)
            if (bestGenome.getFitness() >= CartPoleEvaluator.MAX_TIME_STEPS) {
                System.out.printf("\nSolution found in generation %d!%n", population.getGeneration());
                break;
            }
        }

        // 5. Demonstrate the best genome's performance
        System.out.println("\n--- Demonstrating Best Genome ---");
        System.out.println(bestGenome.getTopologyString());
        demonstrateBestGenome(bestGenome, config);
    }

    /**
     * Runs a simulation with the best network and prints its state to the console.
     */
    private static void demonstrateBestGenome(Genome bestGenome, NEATConfig config) {
        NeuralNetwork bestNetwork = NeuralNetwork.create(bestGenome, config);
        CartPoleEnvironment env = new CartPoleEnvironment();

        for (int i = 0; i < CartPoleEvaluator.MAX_TIME_STEPS; i++) {
            if (env.isDone()) {
                System.out.printf("Demonstration failed after %d steps.%n", i);
                return;
            }

            double[] state = env.getState();
            double[] output = bestNetwork.activate(state);
            int action = (output[0] < 0.5) ? 0 : 1;

            env.update(action);

            // Print a simple representation of the state
            System.out.printf("\rStep: %d, Pos: %+.2f, Angle: %+.2f, Action: %s",
                    i, state[0], state[2], (action == 1 ? "RIGHT" : "LEFT "));

            try {
                // Slow down the simulation to make it watchable
                Thread.sleep(30);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.printf("\nDemonstration successful! Survived for %d steps.%n", CartPoleEvaluator.MAX_TIME_STEPS);
    }
}