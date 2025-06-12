package de.mkoehler.neat.core;

public record NodeGene(int id, NodeType type) {
    public NodeGene copy() {
        return new NodeGene(this.id, this.type);
    }
}
