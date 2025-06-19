// File: src/main/java/de/mkoehler/neat/examples/car/CarRacingEvaluator.java
package de.mkoehler.neat.examples.car;

import de.mkoehler.neat.config.NEATConfig;
import de.mkoehler.neat.core.Genome;
import de.mkoehler.neat.evolution.FitnessEvaluator;
import de.mkoehler.neat.network.NeuralNetwork;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class CarRacingEvaluator implements FitnessEvaluator {

    public static final int MAX_TIME_STEPS = 2000;
    private static final int CHECKPOINT_REWARD = 1000;

    private final NEATConfig config;
    private final Track track;

    public CarRacingEvaluator(NEATConfig config, Track track) {
        this.config = config;
        this.track = track;
    }

    @Override
    public void evaluatePopulation(List<Genome> population) {
        for (Genome genome : population) {
            NeuralNetwork net;
            try {
                net = NeuralNetwork.create(genome, config);
            } catch (Exception e) {
                genome.setFitness(0);
                continue;
            }

            Car car = new Car(track.getStartPosition(), track.getStartAngle(), config.getInputNodes());
            int nextCheckpointIndex = 0;
            double fitness = 0;
            int timeSinceLastCheckpoint = 0;

            for (int step = 0; step < MAX_TIME_STEPS; step++) {
                if (car.isCrashed || timeSinceLastCheckpoint > 300) { // Timeout if stuck
                    break;
                }

                // Get network inputs from car sensors
                car.castRays(track);
                double[] inputs = car.sensorReadings.stream().mapToDouble(d -> d).toArray();

                // Activate network and get outputs
                double[] output = net.activate(inputs);
                double steering = output[0] * 2 - 1; // Map [0,1] to [-1,1]
                double acceleration = output[1];      // Map [0,1] to [0,1]

                // Update car state
                car.update(steering, acceleration, track);

                // Update fitness
                fitness += car.speed * 0.1; // Small reward for speed
                timeSinceLastCheckpoint++;

                // Check for checkpoint
                Rectangle2D nextCheckpoint = track.getCheckpoints().get(nextCheckpointIndex);
                if (nextCheckpoint.contains(car.position)) {
                    fitness += CHECKPOINT_REWARD;
                    nextCheckpointIndex = (nextCheckpointIndex + 1) % track.getCheckpoints().size();
                    timeSinceLastCheckpoint = 0; // Reset timer
                }
            }

            // Add a final bonus for distance to the next checkpoint
            double dist = car.position.distance(track.getCheckpoints().get(nextCheckpointIndex).getCenterX(), track.getCheckpoints().get(nextCheckpointIndex).getCenterY());
            fitness += CHECKPOINT_REWARD - dist;

            genome.setFitness(Math.max(0, fitness));
        }
    }
}