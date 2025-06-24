// File: src/main/java/de/mkoehler/neat/examples/soccer/MainSoccer.java
package de.mkoehler.neat.examples.soccer;

import de.mkoehler.neat.config.NEATConfig;
import de.mkoehler.neat.core.Genome;
import de.mkoehler.neat.examples.AbstractNeatExample;
import de.mkoehler.neat.evolution.FitnessEvaluator;
import de.mkoehler.neat.network.ActivationFunction;
import de.mkoehler.neat.network.NeuralNetwork;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class MainSoccer extends AbstractNeatExample {

    private SoccerVisualizer simVisualizer;
    private final Random random = new Random();

    private SensorVisualizerPanel player1SensorPanel;
    private SensorVisualizerPanel player2SensorPanel;

    public static void main(String[] args) {
        MainSoccer challenge = new MainSoccer();
        challenge.run(args, 1000, 10); // Run for 500 generations
    }

    @Override
    protected String getGenomeFilePrefix() {
        return "soccer";
    }

    @Override
    protected NEATConfig createNeatConfig() {
        return NEATConfig.builder()
                .inputNodes(12)
                .outputNodes(3) // Turn, Thrust, Kick
                .populationSize(100) // Keep population smaller due to expensive evaluation
                .allowRecurrent(false)
                .hiddenActivationFunction(ActivationFunction.TANH)
                .outputActivationFunction(ActivationFunction.TANH) // Good for [-1, 1] turn output
                .startWithFullyConnectedTopology(true)
                .build();
    }

    @Override
    protected FitnessEvaluator createFitnessEvaluator(NEATConfig config) {
        return new SoccerEvaluator(config);
    }

    @Override
    protected void setupProblemSpecificVisualizers() {
        JFrame simFrame = new JFrame("NEAT Soccer");
        simFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Use a BorderLayout to arrange multiple panels
        simFrame.setLayout(new BorderLayout());

        // Create the panels
        this.simVisualizer = new SoccerVisualizer();
        this.player1SensorPanel = new SensorVisualizerPanel("Player 1 (Blue)", Color.CYAN);
        this.player2SensorPanel = new SensorVisualizerPanel("Player 2 (Red)", Color.PINK);

        // Add panels to the frame
        simFrame.add(simVisualizer, BorderLayout.CENTER);
        simFrame.add(player1SensorPanel, BorderLayout.WEST);
        simFrame.add(player2SensorPanel, BorderLayout.EAST);

        simFrame.pack();
        simFrame.setLocationByPlatform(true);
        simFrame.setVisible(true);
    }

    // Correct version of MainSoccer.demonstrate()
    @Override
    protected void demonstrate(Genome bestGenome, NEATConfig config) {
        System.out.println("Demonstrating champion (BLUE) vs a copy of itself (RED).");

        NeuralNetwork net1 = NeuralNetwork.create(bestGenome, config);
        NeuralNetwork net2 = NeuralNetwork.create(bestGenome, config);
        SoccerEnvironment env = new SoccerEnvironment();

        for (int i = 0; i < 3000; i++) {
            // 1. Sense
            double[] inputs1 = env.getInputsFor(env.player1);
            double[] inputs2 = env.getInputsFor(env.player2);
            player1SensorPanel.updateInputs(inputs1);
            player2SensorPanel.updateInputs(inputs2);

            // 2. Think
            double[] outputs1 = net1.activate(inputs1);
            double[] outputs2 = net2.activate(inputs2);

            // 3. Act (apply intent)
            env.applyActions(outputs1, outputs2);

            // 4. Simulate (move, collide, score)
            env.runPhysicsStep();

            // 5. Visualize
            simVisualizer.updateEnvironment(env);

            try { Thread.sleep(20); } catch (InterruptedException e) { break; }
        }
        System.out.println("Demonstration finished.");
    }

}