package de.mkoehler.neat.evolution;

import de.mkoehler.neat.core.Genome;

/**
 * A functional interface for determining if a given genome has met the
 * ultimate goal of a problem.
 * <p>
 * This provides a definitive, binary success condition that is separate from
 * the more nuanced fitness function used to guide evolution. It helps prevent
 * "reward hacking" where an agent gets a high fitness score without actually
 * solving the core task.
 * </p>
 */
@FunctionalInterface
public interface GoalEvaluator {

    /**
     * Evaluates a single genome against the true success criteria.
     *
     * @param genome The genome to evaluate.
     * @return {@code true} if the genome successfully solves the problem, {@code false} otherwise.
     */
    boolean isGoalMet(Genome genome);
}