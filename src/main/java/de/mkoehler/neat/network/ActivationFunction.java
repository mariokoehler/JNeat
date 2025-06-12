package de.mkoehler.neat.network;

import java.util.function.DoubleUnaryOperator;

public class ActivationFunction {
    public static final DoubleUnaryOperator SIGMOID = x -> 1 / (1 + Math.exp(-4.9 * x));
}