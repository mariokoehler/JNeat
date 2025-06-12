package de.mkoehler.neat.evolution;

import de.mkoehler.neat.config.NEATConfig;
import de.mkoehler.neat.core.Genome;

import java.util.*;

public class Species {
    private static int nextId = 0;
    private final int id;
    private final List<Genome> members = new ArrayList<>();
    private Genome representative;
    private double topFitness = 0.0;
    private int generationsWithoutImprovement = 0;

    public Species(Genome representative) {
        this.id = nextId++;
        this.representative = representative;
        addMember(representative);
    }

    public boolean addMember(Genome genome) {
        members.add(genome);
        return true;
    }

    public List<Genome> generateOffspring(int offspringCount, NEATConfig config, InnovationTracker tracker, Random random) {
        List<Genome> offspring = new ArrayList<>();
        if (offspringCount == 0) return offspring;

        // Elitism: carry over the best genome of the species
        int eliteCount = (int) Math.ceil(members.size() * config.speciesElitism);
        if (eliteCount > offspringCount) eliteCount = offspringCount;

        members.sort(Comparator.comparingDouble(Genome::getFitness).reversed()); // Best fitness first
        for (int i = 0; i < eliteCount; i++) {
            offspring.add(members.get(i)); // Note: this is a reference, not a copy. It will be part of the next gen.
        }

        for (int i = 0; i < offspringCount - eliteCount; i++) {
            Genome child;
            if (random.nextDouble() < config.crossoverRate && members.size() > 1) {
                Genome parent1 = selectParent(random);
                Genome parent2 = selectParent(random);
                child = Genome.crossover(parent1, parent2, random);
            } else {
                child = selectParent(random).copy(); // Create a copy to mutate
            }
            child.mutate(config, tracker, random);
            offspring.add(child);
        }

        return offspring;
    }

    private Genome selectParent(Random random) {
        // Simple tournament selection could be used here, for now, just random selection
        return members.get(random.nextInt(members.size()));
    }

    public void calculateAdjustedFitness() {
        for (Genome g : members) {
            g.setAdjustedFitness(g.getFitness() / members.size());
        }
    }

    public void updateStagnation() {
        double currentTopFitness = members.stream().mapToDouble(Genome::getFitness).max().orElse(0);
        if (currentTopFitness > this.topFitness) {
            this.topFitness = currentTopFitness;
            this.generationsWithoutImprovement = 0;
        } else {
            this.generationsWithoutImprovement++;
        }
    }

    public void reset(Random random) {
        if (!members.isEmpty()) {
            this.representative = members.get(random.nextInt(members.size()));
        }
        members.clear();
        topFitness = 0.0;
    }

    public List<Genome> getMembers() { return members; }
    public Genome getRepresentative() { return representative; }
    public int getGenerationsWithoutImprovement() { return generationsWithoutImprovement; }
    public double getAverageFitness() { return members.stream().mapToDouble(Genome::getFitness).average().orElse(0.0); }
}