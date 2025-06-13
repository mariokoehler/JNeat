package de.mkoehler.neat.examples.cartpole_centering;

import de.mkoehler.neat.config.NEATConfig;
import de.mkoehler.neat.core.Genome;
import de.mkoehler.neat.core.VisualizerFrame;
import de.mkoehler.neat.evolution.FitnessEvaluator;
import de.mkoehler.neat.evolution.Population;
import de.mkoehler.neat.examples.cartpole.CartPoleEnvironment;
import de.mkoehler.neat.network.NeuralNetwork;

import javax.swing.*;

public class MainCartPoleCentering {

    private static VisualizerFrame liveChampionVisualizer;
    private static CartPoleVisualizer interactiveVisualizer;
    private static CartPoleEnvironment demonstrationEnv;

    public MainCartPoleCentering() {
        setupInteractiveVisualizer();
    }

    public static void main(String[] args) {
        // --- Create window for LIVE champion view during evolution ---
        liveChampionVisualizer = new VisualizerFrame("Live Champion Topology");
        liveChampionVisualizer.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Don't exit app on close

        MainCartPoleCentering app = new MainCartPoleCentering();
        app.runEvolution();
    }

    public void runEvolution() {
        // ... (NEAT configuration and population setup is the same)
        NEATConfig config = new NEATConfig(4, 1);
        config.populationSize = 250;
        config.compatibilityThreshold = 3.5;
        FitnessEvaluator evaluator = new CartPoleRobustnessEvaluator(config);
        Population population = new Population(config, evaluator);

        Genome bestGenome = null;
        Genome allTimeBest = null;
        int generations = 500;
        for (int i = 0; i < generations; i++) {
            population.evolve();
            bestGenome = population.getBestGenome();

            // Keep track of the best genome found so far
            if (allTimeBest == null || bestGenome.getFitness() > allTimeBest.getFitness()) {
                allTimeBest = bestGenome.copy();
            }

            // --- Update the live visualizer ---
            liveChampionVisualizer.updateVisuals(bestGenome);

            System.out.printf("Generation: %d, Best Fitness (score): %.2f, Species: %d%n",
                    population.getGeneration(), bestGenome.getFitness(), population.getSpeciesCount());

            if (bestGenome.getFitness() > CartPoleCenteringEvaluator.MAX_TIME_STEPS * 0.95) {
                break;
            }
        }

        System.out.println("\n--- Demonstrating All-Time Best Genome ---");
        System.out.println("Click to the left or right of the cart to apply a disturbance!");
        System.out.println(allTimeBest.getTopologyString());

        // --- Create a NEW window for the final winner ---
        VisualizerFrame finalWinnerVisualizer = new VisualizerFrame("Final Winning Genome");
        finalWinnerVisualizer.updateVisuals(allTimeBest);

        demonstrateBestGenome(allTimeBest, config);
    }

    private void setupInteractiveVisualizer() {
        interactiveVisualizer = new CartPoleVisualizer(this);
        JFrame frame = new JFrame("NEAT Cart-Pole Interactive Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(interactiveVisualizer);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void handleDisturbance(double impulse) {
        if (demonstrationEnv != null) {
            demonstrationEnv.applyDisturbance(impulse);
        }
    }

    private static void demonstrateBestGenome(Genome bestGenome, NEATConfig config) {
        // ... (This method is the same as before, but uses interactiveVisualizer)
        NeuralNetwork bestNetwork = NeuralNetwork.create(bestGenome, config);
        demonstrationEnv = new CartPoleEnvironment();
        for (int i = 0; i < CartPoleCenteringEvaluator.MAX_TIME_STEPS; i++) {
            if (demonstrationEnv.isDone()) break;
            double[] state = demonstrationEnv.getState();
            double[] output = bestNetwork.activate(state);
            int action = (output[0] < 0.5) ? 0 : 1;
            demonstrationEnv.update(action);
            interactiveVisualizer.updateState(state[0], state[2]);
            try { Thread.sleep(20); } catch (InterruptedException e) { break; }
        }
        System.out.println("\nDemonstration finished.");
        demonstrationEnv = null;
    }
}