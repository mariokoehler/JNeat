package de.mkoehler.neat.examples.memory;

import de.mkoehler.neat.config.NEATConfig;
import de.mkoehler.neat.core.Genome;
import de.mkoehler.neat.core.VisualizerFrame;
import de.mkoehler.neat.evolution.FitnessEvaluator;
import de.mkoehler.neat.evolution.GoalEvaluator;
import de.mkoehler.neat.evolution.Population;
import de.mkoehler.neat.network.NeuralNetwork;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

// Main Application Class
public class MainMemoryChallenge {

    public static void main(String[] args) {
        // --- Setup visualization windows ---
        VisualizerFrame topologyVisualizer = new VisualizerFrame("Live Champion Topology");
        topologyVisualizer.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JFrame simFrame = new JFrame("Memory Challenge Simulation");
        MemoryVisualizer simVisualizer = new MemoryVisualizer();
        simFrame.add(simVisualizer);
        simFrame.pack();
        simFrame.setLocationByPlatform(true);
        simFrame.setVisible(true);
        simFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Genome seedGenome = null;
        if (args.length == 2 && args[0].equalsIgnoreCase("--genome")) {
            try {
                String content = Files.readString(Paths.get(args[1]));
                seedGenome = Genome.fromJsonString(content);
                System.out.println("Successfully loaded seed genome from: " + args[1]);
            } catch (IOException e) {
                System.err.println("Error loading genome file: " + e.getMessage());
                return;
            }
        }

        // 1. Configure NEAT for the Memory Challenge
        NEATConfig config = NEATConfig.builder()
                .inputNodes(6)
                .outputNodes(2)
                .populationSize(250)
                .allowRecurrent(true)
                .compatibilityThreshold(4.0)
                .build();

        // 2. Create the evaluators
        FitnessEvaluator evaluator = new MemoryEvaluator(config);
        GoalEvaluator goalEvaluator = new MemoryGoalEvaluator(config);

        // 3. Create a population
        Population population = new Population(config, evaluator);

        if (seedGenome != null) {
            System.out.println("Seeding initial population from loaded genome...");
            population.getGenomes().clear();
            population.getGenomes().add(seedGenome); // Add the original
            Random random = new Random();
            for (int i = 1; i < config.getPopulationSize(); i++) {
                Genome mutant = seedGenome.copy();
                // Mutate heavily to create initial diversity around the solution
                mutant.mutate(config, population.getInnovationTracker(), random);
                mutant.mutate(config, population.getInnovationTracker(), random);
                population.getGenomes().add(mutant);
            }
        }

        // 4. Evolve
        Genome allTimeBest = null;
        if (seedGenome != null) {
            allTimeBest = seedGenome.copy();
        }

        // Define our safety-net interval
        final int GOAL_CHECK_INTERVAL = 10;

        for (int i = 0; i < 1000; i++) {
            population.evolve();
            Genome bestOfGen = Objects.requireNonNull(population.getBestGenome());

            System.out.printf("Generation: %d, Best Fitness: %.2f, Species: %d%n",
                    population.getGeneration(), bestOfGen.getFitness(), population.getSpeciesCount());

            boolean newBestFitnessFound = (allTimeBest == null || bestOfGen.getFitness() > allTimeBest.getFitness());

            // Trigger a check if we have a new fitness champion OR it's a periodic safety-net check
            if (newBestFitnessFound || (population.getGeneration() % GOAL_CHECK_INTERVAL == 0)) {

                if (newBestFitnessFound) {
                    System.out.println("New all-time best fitness found! Checking if it meets the goal...");
                    allTimeBest = bestOfGen.copy(); // Tentatively update our best candidate
                    topologyVisualizer.updateVisuals(allTimeBest);
                }

                // Run the expensive goal check on the current generation's champion
                if (goalEvaluator.isGoalMet(bestOfGen)) {
                    System.out.println("\nSUCCESS! Goal achieved by champion of generation " + population.getGeneration());
                    // The one that met the goal is the true best, even if its fitness score is lower
                    allTimeBest = bestOfGen.copy();
                    break; // End the evolution
                } else if (newBestFitnessFound) {
                    // Only log this part if it was a fitness-based check that failed
                    System.out.println("...Goal not met yet, continuing evolution.");
                }
            }
        }


        if (allTimeBest != null) {
            Genome prunedBest = allTimeBest.getAsPruned();
            System.out.println("\n--- Pruned Final Genome Topology ---");
            System.out.println(prunedBest.getTopologyString());

            saveGenomeToFile(prunedBest); // Save the clean version

            System.out.println("\n--- Demonstrating All-Time Best Genome ---");
            // We can demonstrate either one, but the pruned one is functionally identical and cleaner.
            topologyVisualizer.updateVisuals(prunedBest);
            demonstrate(prunedBest, config, simVisualizer);

        } else {
            System.out.println("No viable genome was evolved.");
        }

    }

    private static void saveGenomeToFile(Genome genome) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = "best_genome_" + timestamp + ".json";
        try {
            String json = genome.toJsonString();
            Path path = Paths.get(filename);
            Files.writeString(path, json);
            System.out.println("Successfully saved best genome to: " + path.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error saving genome to file: " + e.getMessage());
        }
    }

    private static void demonstrate(Genome genome, NEATConfig config, MemoryVisualizer visualizer) {
        NeuralNetwork net = NeuralNetwork.create(genome, config);
        MemoryEnvironment env = new MemoryEnvironment();
        for (int i = 0; i < MemoryEvaluator.MAX_STEPS; i++) {
            if (env.taskComplete) break;

            double[] state = env.getState();
            double[] output = net.activate(state, 3);
            env.update(output, i);
            visualizer.updateEnvironment(env);

            try { Thread.sleep(30); } catch (InterruptedException e) { break; }
        }
        System.out.println("Demonstration finished.");
    }
}