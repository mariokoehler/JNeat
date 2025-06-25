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
     * Provides a problem-specific prefix for the saved genome filename.
     * @return A string to be used at the start of the filename (e.g., "carRacing").
     */
    protected abstract String getGenomeFilePrefix();

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
    protected void onNewAllTimeBest(Genome newBest, NEATConfig config) {
        System.out.println("New all-time best fitness found!");
        this.topologyVisualizer.updateVisuals(newBest);

        if (config.isSaveEveryChampion()) {
            System.out.println("Saving new champion to file...");
            saveGenomeToFile(newBest);
        }
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
        return false; // Default behavior is to prune for cleaner topology.
    }

    /**
     * The main entry point to start the evolution process.
     *
     * @param args Command-line arguments. Use "--genome <filepath>" to seed the run.
     */
    public void run(String[] args, int maxGenerations, int goalCheckInterval) {
        // Check for --demo flag first. If present, run in demo-only mode.
        if (args.length == 2 && args[0].equalsIgnoreCase("--demo")) {
            runDemonstrationOnly(args[1]);
            return; // End execution after demonstration
        }

        // If not in demo mode, proceed with the normal evolution run.
        runEvolution(args, maxGenerations, goalCheckInterval);

    }

    /**
     * Loads a genome and immediately runs the demonstration, skipping evolution.
     * @param genomeFilePath The path to the genome JSON file.
     */
    private void runDemonstrationOnly(String genomeFilePath) {
        System.out.println("--- Running in Demonstration-Only Mode ---");

        // 1. Load the genome
        Genome demoGenome = loadGenomeFromFile(genomeFilePath);
        if (demoGenome == null) {
            System.err.println("Could not run demonstration. Exiting.");
            return;
        }

        // 2. Setup the necessary components for demonstration
        NEATConfig config = createNeatConfig();
        setupVisualizers();

        // 3. Update topology visualizer with the loaded genome
        System.out.println("\n--- Loaded Genome Topology ---");
        System.out.println(demoGenome.getTopologyString());
        topologyVisualizer.updateVisuals(demoGenome);

        // 4. Run the demonstration
        System.out.println("\n--- Starting Demonstration ---");
        demonstrate(demoGenome, config);
    }

    /**
     * Executes the standard evolution process.
     */
    private void runEvolution(String[] args, int maxGenerations, int goalCheckInterval) {
        // This method now contains the original logic from the run() method.

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
            population.getInnovationTracker().primeFromPopulation(population.getGenomes());
            System.out.println("Initializing visualizer with seed genome topology.");
            topologyVisualizer.updateVisuals(allTimeBest);
        }

        // 2. Evolution Loop
        for (int i = 0; i < maxGenerations; i++) {
            population.evolve();
            Genome bestOfGen = Objects.requireNonNull(population.getBestGenome());

            onGenerationComplete(population, bestOfGen);

            boolean newBestFitnessFound = (allTimeBest == null || bestOfGen.getFitness() > allTimeBest.getFitness());

            if (newBestFitnessFound) {
                allTimeBest = bestOfGen.copy();
                onNewAllTimeBest(allTimeBest, config);
            }

            if (goalEvaluator != null && (newBestFitnessFound || population.getGeneration() % goalCheckInterval == 0)) {
                if (goalEvaluator.isGoalMet(bestOfGen)) {
                    System.out.println("\nSUCCESS! Goal achieved by champion of generation " + population.getGeneration());
                    allTimeBest = bestOfGen.copy();
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

            saveGenomeToFile(finalGenome);
            topologyVisualizer.updateVisuals(finalGenome);

            System.out.println("\n--- Demonstrating All-Time Best Genome ---");
            demonstrate(finalGenome, config);
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

    /**
     * Renamed this method for clarity. It specifically handles the --genome flag for seeding.
     */
    private Genome loadSeedGenomeFromArgs(String[] args) {
        if (args.length == 2 && args[0].equalsIgnoreCase("--genome")) {
            return loadGenomeFromFile(args[1]);
        }
        return null;
    }

    /**
     * New helper method to load a genome from a file, used by both demo and seed modes.
     */
    private Genome loadGenomeFromFile(String filePath) {
        try {
            String content = Files.readString(Paths.get(filePath));
            System.out.println("Successfully loaded genome from: " + filePath);
            return Genome.fromJsonString(content);
        } catch (IOException e) {
            System.err.println("Error loading genome file: " + e.getMessage());
            return null;
        }
    }
    private void saveGenomeToFile(Genome genome) {
        // 1. Get the prefix from the concrete example class
        String prefix = getGenomeFilePrefix();

        // 2. Get the rounded fitness value
        long fitness = Math.round(genome.getFitness());

        // 3. Get the timestamp
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        // 4. Assemble the new filename
        String filename = String.format("%s_fitness-%d_%s.json", prefix, fitness, timestamp);

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