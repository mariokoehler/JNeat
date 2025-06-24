// File: src/main/java/de/mkoehler/neat/examples/soccer/SoccerEvaluator.java
package de.mkoehler.neat.examples.soccer;

import de.mkoehler.neat.config.NEATConfig;
import de.mkoehler.neat.core.Genome;
import de.mkoehler.neat.evolution.FitnessEvaluator;
import de.mkoehler.neat.network.NeuralNetwork;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SoccerEvaluator implements FitnessEvaluator {

    private static final int MAX_GAME_STEPS = 1500; // Each game lasts up to 1500 steps
    private static final int GAMES_PER_GENOME = 5;  // Each genome plays 5 games

    private final NEATConfig config;
    private final Random random = new Random();

    private record Matchup(Genome player1, Genome player2) {}
    private record FitnessUpdate(Genome genome, double fitness) {}

    public SoccerEvaluator(NEATConfig config) {
        this.config = config;
    }

    @Override
    public void evaluatePopulation(List<Genome> population) {

        // 1. Prepare all the matchups that need to be played this generation.
        List<Matchup> matchups = new ArrayList<>();
        for (int i = 0; i < population.size(); i++) {
            for (int k = 0; k < GAMES_PER_GENOME; k++) {
                int opponentIndex;
                do {
                    opponentIndex = random.nextInt(population.size());
                } while (opponentIndex == i);
                matchups.add(new Matchup(population.get(i), population.get(opponentIndex)));
            }
        }

        // 2. Play all games in parallel and collect the fitness updates.
        //    The stream processing is the only part that runs concurrently.
        Map<Genome, Double> totalFitnessScores = matchups.parallelStream()
                .flatMap(matchup -> {
                    // Play one game
                    MatchResult result = playGame(matchup.player1(), matchup.player2());
                    // Return a stream of two updates, one for each player
                    return Stream.of(
                            new FitnessUpdate(matchup.player1(), result.player1Fitness),
                            new FitnessUpdate(matchup.player2(), result.player2Fitness)
                    );
                })
                .collect(Collectors.groupingBy(
                        FitnessUpdate::genome,
                        Collectors.summingDouble(FitnessUpdate::fitness)
                ));

        // 3. Update the fitness of each genome in a final, safe, single-threaded loop.
        population.forEach(genome -> {
            // Get the total accumulated score from the map, or 0 if the genome didn't play.
            double totalFitness = totalFitnessScores.getOrDefault(genome, 0.0);
            genome.setFitness(Math.max(0, totalFitness));
        });

    }

    /**
     * Simulates a single match between two genomes and calculates their performance.
     */
    private MatchResult playGame(Genome g1, Genome g2) {

        // Randomly assign genomes to player slots to ensure they learn both sides.
        Genome leftPlayerGenome;
        Genome rightPlayerGenome;
        if (random.nextBoolean()) {
            leftPlayerGenome = g1;
            rightPlayerGenome = g2;
        } else {
            leftPlayerGenome = g2;
            rightPlayerGenome = g1;
        }

        NeuralNetwork net1 = NeuralNetwork.create(leftPlayerGenome, config);  // Player 1 is always on the left
        NeuralNetwork net2 = NeuralNetwork.create(rightPlayerGenome, config); // Player 2 is always on the right

        SoccerEnvironment env = new SoccerEnvironment();

        double fitness1 = 0;
        double fitness2 = 0;

        for (int step = 0; step < MAX_GAME_STEPS; step++) {

            // 1. Get sensory inputs based on the CURRENT state of the world.
            double[] inputs1 = env.getInputsFor(env.player1);
            double[] inputs2 = env.getInputsFor(env.player2);

            // 2. Activate networks to get the desired actions for this frame.
            double[] outputs1 = net1.activate(inputs1);
            double[] outputs2 = net2.activate(inputs2);

            // 3. Apply these actions to the players (changes their intent/velocity).
            env.applyActions(outputs1, outputs2);

            // 4. Run the physics simulation for one step (moves players, resolves collisions).
            boolean goalScored = env.runPhysicsStep();

            // --- Granular Fitness Calculation ---
            fitness1 += calculateStepFitness(env, env.player1);
            fitness2 += calculateStepFitness(env, env.player2);

            // Update environment physics and check for goals
            if (goalScored) {
                // The checkGoal() method in env already updated scores and reset positions
                // Now we apply a large fitness bonus/penalty
                if (env.score1 > 0) { // Player 1 scored
                    fitness1 += 1000;
                    fitness2 -= 500;
                } else if (env.score2 > 0) { // Player 2 scored
                    fitness2 += 1000;
                    fitness1 -= 500;
                }
                env.score1 = 0; // Reset for next potential goal in same match
                env.score2 = 0;
            }
        }

        if (leftPlayerGenome == g1) {
            // g1 was on the left (player 1), g2 was on the right (player 2)
            return new MatchResult(fitness1, fitness2);
        } else {
            // g2 was on the left (player 1), g1 was on the right (player 2)
            return new MatchResult(fitness2, fitness1);
        }
    }

    private double calculateStepFitness(SoccerEnvironment env, SoccerPlayer player) {
        double stepFitness = 0;
        Point2D opponentGoalCenter = (player.team == 0) ? env.getCenter(env.goal2) : env.getCenter(env.goal1);

        // Reward for being close to the ball
        double distToBall = player.position.distance(env.ball.position);
        stepFitness += (SoccerEnvironment.FIELD_WIDTH - distToBall) * 0.001; // Small reward

        // Reward for the ball being close to the opponent's goal
        double ballDistToGoal = env.ball.position.distance(opponentGoalCenter);
        stepFitness += (SoccerEnvironment.FIELD_WIDTH - ballDistToGoal) * 0.002; // Slightly higher reward

        return stepFitness;
    }

    /** A simple data class to hold the results of a single match. */
    private static class MatchResult {
        final double player1Fitness;
        final double player2Fitness;

        MatchResult(double f1, double f2) {
            this.player1Fitness = f1;
            this.player2Fitness = f2;
        }
    }
}