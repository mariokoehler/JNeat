// File: src/main/java/de/mkoehler/neat/examples/car/CarRacingEvaluator.java
package de.mkoehler.neat.examples.car;

import de.mkoehler.neat.config.NEATConfig;
import de.mkoehler.neat.core.Genome;
import de.mkoehler.neat.evolution.FitnessEvaluator;
import de.mkoehler.neat.network.NeuralNetwork;
import java.awt.geom.Line2D; // <-- ADD THIS IMPORT
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
                if (car.isCrashed || timeSinceLastCheckpoint > 300) {
                    break;
                }

                car.castRays(track);
                double[] inputs = car.sensorReadings.stream().mapToDouble(d -> d).toArray();

                double[] output = net.activate(inputs);
                double steering = output[0] * 2 - 1;
                double acceleration = output[1];

                car.update(steering, acceleration, track);

                fitness += car.speed * 0.1;
                timeSinceLastCheckpoint++;

                Line2D nextCheckpoint = track.getCheckpoints().get(nextCheckpointIndex);
                Line2D carMovement = new Line2D.Double(car.previousPosition, car.position);

                if (nextCheckpoint.intersectsLine(carMovement)) {
                    fitness += CHECKPOINT_REWARD;
                    nextCheckpointIndex = (nextCheckpointIndex + 1) % track.getCheckpoints().size();
                    timeSinceLastCheckpoint = 0;
                }
            }

            // Use distance from car's final position to the next checkpoint line
            double dist = track.getCheckpoints().get(nextCheckpointIndex).ptSegDist(car.position);
            fitness += (CHECKPOINT_REWARD / 2.0) - dist; // Use a smaller bonus here

            genome.setFitness(Math.max(0, fitness));
        }
    }
}