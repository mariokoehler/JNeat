package de.mkoehler.neat.core;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class ConnectionGene {

    private final int inNodeId;
    private final int outNodeId;
    private final int innovationNumber;

    @Setter
    private double weight;
    @Setter
    private boolean enabled;

    public ConnectionGene(int inNodeId, int outNodeId, double weight, boolean enabled, int innovationNumber) {
        this.inNodeId = inNodeId;
        this.outNodeId = outNodeId;
        this.weight = weight;
        this.enabled = enabled;
        this.innovationNumber = innovationNumber;
    }

    public ConnectionGene copy() {
        return new ConnectionGene(inNodeId, outNodeId, weight, enabled, innovationNumber);
    }

}
