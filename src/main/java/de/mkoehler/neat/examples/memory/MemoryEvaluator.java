package de.mkoehler.neat.examples.memory;

import de.mkoehler.neat.config.NEATConfig;
import de.mkoehler.neat.core.Genome;
import de.mkoehler.neat.evolution.FitnessEvaluator;
import de.mkoehler.neat.network.NeuralNetwork;

import java.util.List;

public class MemoryEvaluator implements FitnessEvaluator {

    private final NEATConfig config;
    public static final int MAX_STEPS = 800;

    public MemoryEvaluator(NEATConfig config) {
        this.config = config;
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

            MemoryEnvironment env = new MemoryEnvironment();
            for (int i = 0; i < MAX_STEPS; i++) {
                if (env.taskComplete) break;

                double[] state = env.getState();
                // Activate network over several steps to let recurrent signals propagate
                double[] output = net.activate(state, 3);
                env.update(output, i);
            }

            genome.setFitness(calculateFitness(env));
        }
    }

    private double calculateFitness(MemoryEnvironment env) {
        double maxDist = env.agentPos.distance(0,0);
        double fitness = 0;

        // Reward for getting close to the first food item
        double distToFood = env.agentPos.distance(env.foodPos);
        fitness += (maxDist - distToFood);

        if (env.goalActive) {
            // Big bonus for completing stage 1
            fitness += 1000.0;

            // Add a time bonus for eating the food quickly
            fitness += (MAX_STEPS - env.foodEatenTime);

            // Reward for getting close to the goal
            double distToGoal = env.agentPos.distance(env.goalPos);
            fitness += (maxDist - distToGoal);

            if (env.taskComplete) {
                // Huge bonus for completing the entire task
                fitness += 5000.0;
            }
        }

        return Math.max(0, fitness);
    }
}