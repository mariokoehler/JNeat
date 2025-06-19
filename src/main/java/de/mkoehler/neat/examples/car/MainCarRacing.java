// File: src/main/java/de/mkoehler/neat/examples/car/MainCarRacing.java
package de.mkoehler.neat.examples.car;

import de.mkoehler.neat.config.NEATConfig;
import de.mkoehler.neat.core.Genome;
import de.mkoehler.neat.core.NodeType;
import de.mkoehler.neat.examples.AbstractNeatExample;
import de.mkoehler.neat.evolution.FitnessEvaluator;
import de.mkoehler.neat.network.NeuralNetwork;

import javax.swing.*;

public class MainCarRacing extends AbstractNeatExample {

    private CarRacingVisualizer simVisualizer;
    private final Track track = new Track(); // The track is constant for the whole run

    public static void main(String[] args) {
        MainCarRacing challenge = new MainCarRacing();
        challenge.run(args, 10, 25);
    }

    @Override
    protected NEATConfig createNeatConfig() {
        return NEATConfig.builder()
                .inputNodes(5) // 5 sensors
                .outputNodes(2) // steering and acceleration
                .populationSize(200)
                .compatibilityThreshold(4.0)
                .speciesStagnationLimit(20)
                .allowRecurrent(false) // Feed-forward is likely sufficient
                .build();
    }

    @Override
    protected boolean shouldPruneFinalGenome() {
        // For this problem, the number and order of sensors is critical.
        // Pruning would change the meaning of the inputs.
        return false;
    }

    @Override
    protected FitnessEvaluator createFitnessEvaluator(NEATConfig config) {
        return new CarRacingEvaluator(config, this.track);
    }

    @Override
    protected void setupProblemSpecificVisualizers() {
        JFrame simFrame = new JFrame("NEAT Car Racing");
        this.simVisualizer = new CarRacingVisualizer();
        simFrame.add(simVisualizer);
        simFrame.pack();
        simFrame.setLocationByPlatform(true);
        simFrame.setVisible(true);
        simFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    protected void demonstrate(Genome bestGenome, NEATConfig config) {
        NeuralNetwork net = NeuralNetwork.create(bestGenome, config);

        // Don't use the original config. Count the actual inputs in the winner genome.
        int actualInputCount = (int) bestGenome.getNodes().values().stream()
                .filter(node -> node.type() == NodeType.INPUT)
                .count();

        // Create a car with the correct number of sensors for this specific genome.
        Car car = new Car(track.getStartPosition(), track.getStartAngle(), actualInputCount);

        for (int i = 0; i < CarRacingEvaluator.MAX_TIME_STEPS; i++) {
            if (car.isCrashed) break;

            car.castRays(track);
            double[] inputs = car.sensorReadings.stream().mapToDouble(d -> d).toArray();
            double[] output = net.activate(inputs);
            double steering = output[0] * 2 - 1;
            double acceleration = output[1];
            car.update(steering, acceleration, track);

            simVisualizer.updateState(track, car);

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