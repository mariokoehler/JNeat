package de.mkoehler.neat.examples.xor;

import de.mkoehler.neat.config.NEATConfig;
import de.mkoehler.neat.core.Genome;
import de.mkoehler.neat.evolution.Population;
import de.mkoehler.neat.network.NeuralNetwork;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        // 1. Configure NEAT
        NEATConfig config = NEATConfig.builder()
                .inputNodes(2)
                .outputNodes(1)
                .build();

        // 2. Create a fitness evaluator
        XorEvaluator evaluator = new XorEvaluator(config);

        // 3. Create a population
        Population population = new Population(config, evaluator);

        // 4. Evolve
        Genome bestGenome = null;
        int generations = 20000;
        for (int i = 0; i < generations; i++) {
            population.evolve();
            bestGenome = population.getBestGenome();
            System.out.printf("Generation: %d, Best Fitness: %.4f, Species: %d%n",
                    population.getGeneration(),
                    bestGenome.getFitness(),
                    population.getSpeciesCount());

            // Stop if a solution is found
            if (bestGenome.getFitness() > 3.9) {
                System.out.println("\nSolution found!");
                break;
            }
        }

        // 5. Test the best genome
        System.out.println("\n--- Testing Best Genome ---");
        NeuralNetwork bestNetwork = NeuralNetwork.create(bestGenome, config);
        testNetwork(bestNetwork, new double[]{0, 0}, 0); // Expected output for {0,0} is 0
        testNetwork(bestNetwork, new double[]{0, 1}, 1); // Expected output for {0,1} is 1
        testNetwork(bestNetwork, new double[]{1, 0}, 1); // Expected output for {1,0} is 1
        testNetwork(bestNetwork, new double[]{1, 1}, 0); // Expected output for {1,1} is 0
    }

    private static void testNetwork(NeuralNetwork net, double[] inputs, int expectedOutput) {
        double[] output = net.activate(inputs);
        int roundedOutput = (int) Math.round(output[0]);

        System.out.printf("Input: %s -> Raw Output: [%.4f], Rounded: %d (Expected: %d) -> %s%n",
                Arrays.toString(inputs),
                output[0],
                roundedOutput,
                expectedOutput,
                (roundedOutput == expectedOutput ? "CORRECT" : "WRONG"));
    }
}