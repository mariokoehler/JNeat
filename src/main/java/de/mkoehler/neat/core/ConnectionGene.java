package de.mkoehler.neat.core;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@NoArgsConstructor
public class ConnectionGene {

    private int inNodeId;
    private int outNodeId;
    private int innovationNumber;

    private double weight;
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
