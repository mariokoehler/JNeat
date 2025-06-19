// File: src/main/java/de/mkoehler/neat/examples/memory/MainMemoryChallenge.java
package de.mkoehler.neat.examples.memory;

import de.mkoehler.neat.config.NEATConfig;
import de.mkoehler.neat.core.Genome;
import de.mkoehler.neat.evolution.FitnessEvaluator;
import de.mkoehler.neat.evolution.GoalEvaluator;
import de.mkoehler.neat.examples.AbstractNeatExample;
import de.mkoehler.neat.network.NeuralNetwork;

import javax.swing.*;

public class MainMemoryChallenge extends AbstractNeatExample {

    private MemoryVisualizer simVisualizer;

    public static void main(String[] args) {
        MainMemoryChallenge challenge = new MainMemoryChallenge();
        // Run for 1000 generations with a goal check every 10 generations
        challenge.run(args, 1000, 10);
    }

    @Override
    protected NEATConfig createNeatConfig() {
        return NEATConfig.builder()
                .inputNodes(6)
                .outputNodes(2)
                .populationSize(250)
                .allowRecurrent(true)
                .compatibilityThreshold(4.0)
                .build();
    }

    @Override
    protected FitnessEvaluator createFitnessEvaluator(NEATConfig config) {
        return new MemoryEvaluator(config);
    }

    @Override
    protected GoalEvaluator createGoalEvaluator(NEATConfig config) {
        return new MemoryGoalEvaluator(config);
    }

    @Override
    protected void setupProblemSpecificVisualizers() {
        JFrame simFrame = new JFrame("Memory Challenge Simulation");
        this.simVisualizer = new MemoryVisualizer();
        simFrame.add(simVisualizer);
        simFrame.pack();
        simFrame.setLocationByPlatform(true);
        simFrame.setVisible(true);
        simFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    protected void demonstrate(Genome bestGenome, NEATConfig config) {
        NeuralNetwork net = NeuralNetwork.create(bestGenome, config);
        MemoryEnvironment env = new MemoryEnvironment();
        for (int i = 0; i < MemoryEvaluator.MAX_STEPS; i++) {
            if (env.taskComplete) break;

            double[] state = env.getState();
            // Activate network over several steps to let recurrent signals propagate
            double[] output = net.activate(state, 3);
            env.update(output, i);
            simVisualizer.updateEnvironment(env);

            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("Demonstration finished.");
    }
}