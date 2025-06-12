package de.mkoehler.neat.evolution;

import de.mkoehler.neat.core.Genome;
import java.util.List;

@FunctionalInterface
public interface FitnessEvaluator {
    void evaluatePopulation(List<Genome> population);
}
