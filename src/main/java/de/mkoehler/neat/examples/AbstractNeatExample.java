// File: src/main/java/de/mkoehler/neat/examples/AbstractNeatExample.java
package de.mkoehler.neat.examples;

import de.mkoehler.neat.config.NEATConfig;
import de.mkoehler.neat.core.Genome;
import de.mkoehler.neat.core.VisualizerFrame;
import de.mkoehler.neat.evolution.FitnessEvaluator;
import de.mkoehler.neat.evolution.GoalEvaluator;
import de.mkoehler.neat.evolution.Population;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

/**
 * An abstract base class for running NEAT experiments.
 * <p>
 * This class handles the common boilerplate for a NEAT run, including:
 * <ul>
 *   <li>The main evolution loop.</li>
 *   <li>Command-line argument parsing for a seed genome.</li>
 *   <li>Tracking the best genome over all generations.</li>
 *   <li>Periodic checking against a "goal condition".</li>
 *   <li>Saving the final, best genome to a file.</li>
 *   <li>Displaying the live topology of the generation champion.</li>
 * </ul>
 * <p>
 * To create a new experiment, extend this class and implement the abstract methods
 * to provide the problem-specific configuration, evaluators, and demonstration logic.
 */
public abstract class AbstractNeatExample {

    protected VisualizerFrame topologyVisualizer;
    protected Genome allTimeBest;

    // --- Abstract Methods (Must be implemented by subclasses) ---

    /**
     * Creates the NEAT configuration specific to the problem.
     *
     * @return A configured NEATConfig instance.
     */
    protected abstract NEATConfig createNeatConfig();

    /**
     * Creates the fitness evaluator for the problem. This is the core
     * function that assigns a fitness score to each genome.
     *
     * @param config The NEAT configuration.
     * @return An implementation of FitnessEvaluator.
     */
    protected abstract FitnessEvaluator createFitnessEvaluator(NEATConfig config);

    /**
     * Runs a final demonstration of the winning genome. This could involve
     * a GUI, console output, or any other form of visualization.
     *
     * @param bestGenome The genome that won the evolutionary run.
     * @param config     The NEAT configuration.
     */
    protected abstract void demonstrate(Genome bestGenome, NEATConfig config);

    // --- Overridable "Hook" Methods (Optional for subclasses) ---

    /**
     * Creates a GoalEvaluator for the problem. A GoalEvaluator provides a definitive,
     * binary "solved/not solved" check, which can be more reliable than fitness alone.
     *
     * @param config The NEAT configuration.
     * @return An implementation of GoalEvaluator, or null if not used.
     */
    protected GoalEvaluator createGoalEvaluator(NEATConfig config) {
        return null; // Default: no goal evaluator
    }

    /**
     * Sets up any problem-specific visualizers before evolution begins.
     * The generic topology visualizer is already handled by the base class.
     */
    protected void setupProblemSpecificVisualizers() {
        // Default: do nothing
    }

    /**
     * A hook that is called at the end of each generation. The default implementation
     * prints progress to the console.
     *
     * @param population The current state of the population.
     * @param bestOfGen  The best genome from the completed generation.
     */
    protected void onGenerationComplete(Population population, Genome bestOfGen) {
        System.out.printf("Generation: %d, Best Fitness: %.2f, Species: %d%n",
                population.getGeneration(), bestOfGen.getFitness(), population.getSpeciesCount());
    }

    /**
     * A hook called when a new all-time best genome (by fitness) is found.
     * The default implementation updates the topology visualizer.
     *
     * @param newBest The new best genome.
     */
    protected void onNewAllTimeBest(Genome newBest) {
        System.out.println("New all-time best fitness found! Checking if it meets the goal...");
        this.topologyVisualizer.updateVisuals(newBest);
    }

    /**
     * Determines whether the final best genome should be pruned before demonstration.
     * Pruning removes disabled connections and unused nodes.
     * <p>
     * Override and return {@code false} for problems where the absolute identity
     * and count of input/output nodes is semantically important (e.g., sensor arrays).
     *
     * @return {@code true} to prune the genome, {@code false} to use it as-is.
     */
    protected boolean shouldPruneFinalGenome() {
        return true; // Default behavior is to prune for cleaner topology.
    }

    /**
     * The main entry point to start the evolution process.
     *
     * @param args Command-line arguments. Use "--genome <filepath>" to seed the run.
     */
    public void run(String[] args, int maxGenerations, int goalCheckInterval) {
        // 1. Setup
        setupVisualizers();
        Genome seedGenome = loadSeedGenomeFromArgs(args);
        NEATConfig config = createNeatConfig();
        FitnessEvaluator fitnessEvaluator = createFitnessEvaluator(config);
        GoalEvaluator goalEvaluator = createGoalEvaluator(config);
        Population population = new Population(config, fitnessEvaluator);
        seedPopulation(population, seedGenome, config);

        if (seedGenome != null) {
            allTimeBest = seedGenome.copy();
        }

        // 2. Evolution Loop
        for (int i = 0; i < maxGenerations; i++) {
            population.evolve();
            Genome bestOfGen = Objects.requireNonNull(population.getBestGenome());

            onGenerationComplete(population, bestOfGen);

            boolean newBestFitnessFound = (allTimeBest == null || bestOfGen.getFitness() > allTimeBest.getFitness());

            if (newBestFitnessFound) {
                allTimeBest = bestOfGen.copy();
                onNewAllTimeBest(allTimeBest);
            }

            // Periodically check if the goal is met, or check immediately if we have a new best
            if (goalEvaluator != null && (newBestFitnessFound || population.getGeneration() % goalCheckInterval == 0)) {
                if (goalEvaluator.isGoalMet(bestOfGen)) {
                    System.out.println("\nSUCCESS! Goal achieved by champion of generation " + population.getGeneration());
                    allTimeBest = bestOfGen.copy(); // Ensure the actual winner is the all-time best
                    break;
                } else if (newBestFitnessFound) {
                    System.out.println("...Goal not met yet, continuing evolution.");
                }
            }
        }

        // 3. Post-Evolution
        if (allTimeBest != null) {
            Genome finalGenome = allTimeBest;
            if (shouldPruneFinalGenome()) {
                finalGenome = allTimeBest.getAsPruned();
                System.out.println("\n--- Pruned Final Genome Topology ---");
            } else {
                System.out.println("\n--- Final Genome Topology (Unpruned) ---");
            }
            System.out.println(finalGenome.getTopologyString());

            saveGenomeToFile(finalGenome); // Save the version we are about to demonstrate
            topologyVisualizer.updateVisuals(finalGenome);

            System.out.println("\n--- Demonstrating All-Time Best Genome ---");
            demonstrate(finalGenome, config); // Pass the final (maybe-pruned) genome
        } else {
            System.out.println("No viable genome was evolved or found.");
        }
    }

    private void setupVisualizers() {
        this.topologyVisualizer = new VisualizerFrame("Live Champion Topology");
        this.topologyVisualizer.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setupProblemSpecificVisualizers(); // Call hook for subclasses
    }

    private void seedPopulation(Population population, Genome seedGenome, NEATConfig config) {
        if (seedGenome == null) return;

        System.out.println("Seeding initial population from loaded genome...");
        population.getGenomes().clear();
        population.getGenomes().add(seedGenome.copy()); // Add the original
        Random random = new Random();
        for (int i = 1; i < config.getPopulationSize(); i++) {
            Genome mutant = seedGenome.copy();
            // Mutate heavily to create initial diversity
            mutant.mutate(config, population.getInnovationTracker(), random);
            mutant.mutate(config, population.getInnovationTracker(), random);
            population.getGenomes().add(mutant);
        }
    }

    private Genome loadSeedGenomeFromArgs(String[] args) {
        if (args.length == 2 && args[0].equalsIgnoreCase("--genome")) {
            try {
                String content = Files.readString(Paths.get(args[1]));
                System.out.println("Successfully loaded seed genome from: " + args[1]);
                return Genome.fromJsonString(content);
            } catch (IOException e) {
                System.err.println("Error loading genome file: " + e.getMessage());
                System.exit(1);
            }
        }
        return null;
    }

    private void saveGenomeToFile(Genome genome) {
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
}