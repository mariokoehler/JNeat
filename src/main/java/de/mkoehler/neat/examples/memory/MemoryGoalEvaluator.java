package de.mkoehler.neat.examples.memory;

import de.mkoehler.neat.config.NEATConfig;
import de.mkoehler.neat.core.Genome;
import de.mkoehler.neat.evolution.GoalEvaluator;
import de.mkoehler.neat.network.NeuralNetwork;

public class MemoryGoalEvaluator implements GoalEvaluator {

    private final NEATConfig config;

    public MemoryGoalEvaluator(NEATConfig config) {
        this.config = config;
    }

    @Override
    public boolean isGoalMet(Genome genome) {
        // We run a fresh, clean simulation to check for success.
        NeuralNetwork net;
        try {
            net = NeuralNetwork.create(genome, config);
        } catch (Exception e) {
            // An invalid genome cannot meet the goal.
            return false;
        }

        MemoryEnvironment env = new MemoryEnvironment();
        for (int i = 0; i < MemoryEvaluator.MAX_STEPS; i++) {
            if (env.taskComplete) {
                // The agent successfully completed both stages.
                return true;
            }

            double[] state = env.getState();
            double[] output = net.activate(state, 3);
            env.update(output, i);
        }

        // If the loop finishes and the task was not complete, the goal was not met.
        return env.taskComplete;
    }
}
