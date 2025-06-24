package de.mkoehler.neat.evolution;

import de.mkoehler.neat.config.NEATConfig;
import de.mkoehler.neat.core.ConnectionGene;
import de.mkoehler.neat.core.Genome;
import de.mkoehler.neat.core.NodeGene;
import de.mkoehler.neat.core.NodeType;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

public class Population {
    private final NEATConfig config;
    @Getter
    private final InnovationTracker innovationTracker;
    private final FitnessEvaluator evaluator;
    @Getter
    private List<Genome> genomes;
    private List<Species> species;
    private int generation = 0;
    private final Random random = new Random();

    public Population(NEATConfig config, FitnessEvaluator evaluator) {
        this.config = config;
        this.evaluator = evaluator;
        this.innovationTracker = new InnovationTracker(config.getInputNodes(), config.getOutputNodes());
        this.genomes = new ArrayList<>();
        this.species = new ArrayList<>();
        initializePopulation();
    }

    private void initializePopulation() {
        for (int i = 0; i < config.getPopulationSize(); i++) {
            Genome genome = new Genome();
            // Create input and output nodes
            for (int j = 0; j < config.getInputNodes(); j++) {
                genome.addNodeGene(new NodeGene(j, NodeType.INPUT));
            }
            for (int j = 0; j < config.getOutputNodes(); j++) {
                genome.addNodeGene(new NodeGene(config.getInputNodes() + j, NodeType.OUTPUT));
            }

            if (config.isStartWithFullyConnectedTopology()) {
                // Connect every input node to every output node
                for (int in = 0; in < config.getInputNodes(); in++) {
                    for (int out = 0; out < config.getOutputNodes(); out++) {
                        int outNodeId = config.getInputNodes() + out;
                        int innovation = innovationTracker.getInnovationNumber(in, outNodeId);
                        // Start with small random weights
                        double weight = (random.nextDouble() * 2 - 1) * 0.5;
                        genome.addConnectionGene(new ConnectionGene(in, outNodeId, weight, true, innovation));
                    }
                }
            } else {
                // The traditional NEAT approach: start with one random connection
                int inNode = random.nextInt(config.getInputNodes());
                int outNode = config.getInputNodes() + random.nextInt(config.getOutputNodes());
                int innovation = innovationTracker.getInnovationNumber(inNode, outNode);
                genome.addConnectionGene(new ConnectionGene(inNode, outNode, random.nextDouble() * 2 - 1, true, innovation));
            }

            genomes.add(genome);
        }
    }

    public void evolve() {
        evaluator.evaluatePopulation(genomes);
        speciate();
        updateAndCullSpecies();
        reproduce();
        generation++;
        innovationTracker.resetForNextGeneration();
    }

    private void speciate() {
        for (Species s : species) {
            s.reset(random);
        }
        for (Genome g : genomes) {
            boolean foundSpecies = false;
            for (Species s : species) {
                if (Genome.getCompatibilityDistance(g, s.getRepresentative(), config) < config.getCompatibilityThreshold()) {
                    s.addMember(g);
                    foundSpecies = true;
                    break;
                }
            }
            if (!foundSpecies) {
                species.add(new Species(g));
            }
        }
        species.removeIf(s -> s.getMembers().isEmpty());
    }

    private void updateAndCullSpecies() {
        double totalAverageFitness = 0;
        for (Species s : species) {
            s.calculateAdjustedFitness();
            s.updateStagnation();
            totalAverageFitness += s.getAverageFitness();
        }

        // Cull stagnant species
        if(species.size() > 1) {
            species.removeIf(s -> s.getGenerationsWithoutImprovement() > config.getSpeciesStagnationLimit());
        }
    }

    private void reproduce() {
        double totalAdjustedFitnessSum = species.stream()
                .flatMap(s -> s.getMembers().stream())
                .mapToDouble(Genome::getAdjustedFitness)
                .sum();

        List<Genome> nextGeneration = new ArrayList<>();
        for (Species s : species) {
            double speciesAdjustedFitnessSum = s.getMembers().stream().mapToDouble(Genome::getAdjustedFitness).sum();
            int offspringCount = (int) Math.round((speciesAdjustedFitnessSum / totalAdjustedFitnessSum) * config.getPopulationSize());

            if(offspringCount > 0){
                nextGeneration.addAll(s.generateOffspring(offspringCount, config, innovationTracker, random));
            }
        }

        // Refill if rounding caused a shortfall
        while (nextGeneration.size() < config.getPopulationSize()) {
            if (!species.isEmpty()) {
                Species s = species.get(random.nextInt(species.size()));
                nextGeneration.addAll(s.generateOffspring(1, config, innovationTracker, random));
            } else {
                // Catastrophic extinction, re-initialize
                initializePopulation();
                return;
            }
        }

        genomes = nextGeneration.stream().limit(config.getPopulationSize()).collect(Collectors.toList());
    }

    public Genome getBestGenome() {
        return genomes.stream().max(Comparator.comparingDouble(Genome::getFitness)).orElse(null);
    }
    public int getGeneration() { return generation; }
    public int getSpeciesCount() { return species.size(); }
}