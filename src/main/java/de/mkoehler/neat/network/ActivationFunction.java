// File: src/main/java/de/mkoehler/neat/network/ActivationFunction.java
package de.mkoehler.neat.network;

import java.util.function.DoubleUnaryOperator;

/**
 * A utility class that holds common activation functions used in neural networks.
 * <p>
 * Activation functions are mathematical equations that determine the output of a
 * neural network node given a set of inputs. They introduce non-linearity into
 * the network, which is crucial for learning complex patterns.
 * </p>
 * <p>
 * Each function is implemented as a {@link DoubleUnaryOperator}, a functional
 * interface that takes a single {@code double} argument and returns a
 * {@code double} result. This allows them to be passed around and used cleanly.
 * </p>
 */
public class ActivationFunction {

    /**
     * The standard logistic sigmoid function, scaled for NEAT.
     * <p>
     * This function maps any real-valued number into a range between 0 and 1.
     * It is defined as: {@code f(x) = 1 / (1 + e^(-4.9 * x))}.
     * The multiplier of 4.9 is a common practice in NEAT implementations to
     * steepen the curve, which can help accelerate learning.
     * </p>
     * <ul>
     *   <li>Range: (0, 1)</li>
     *   <li>Centered at: 0.5</li>
     * </ul>
     */
    public static final DoubleUnaryOperator SIGMOID = x -> 1 / (1 + Math.exp(-4.9 * x));

    /**
     * The Hyperbolic Tangent (Tanh) function.
     * <p>
     * Tanh is similar to the sigmoid function but maps inputs to a range
     * between -1 and 1. Because its output is zero-centered, it often leads
     * to faster convergence in training compared to sigmoid.
     * </p>
     * <ul>
     *   <li>Range: (-1, 1)</li>
     *   <li>Centered at: 0.0</li>
     * </ul>
     */
    public static final DoubleUnaryOperator TANH = Math::tanh;

    /**
     * The Rectified Linear Unit (ReLU) function.
     * <p>
     * ReLU is one of the most popular activation functions in modern deep learning.
     * It is computationally very efficient and helps mitigate the vanishing
     * gradient problem. It is defined as {@code f(x) = max(0, x)}.
     * </p>
     * <ul>
     *   <li>Range: [0, ∞)</li>
     *   <li>Behavior: Returns the input if it's positive, otherwise returns 0.</li>
     * </ul>
     */
    public static final DoubleUnaryOperator RELU = x -> Math.max(0, x);

    /**
     * The Leaky Rectified Linear Unit (Leaky ReLU) function.
     * <p>
     * Leaky ReLU is a variant of ReLU that allows a small, non-zero gradient
     * for negative inputs, preventing "dying ReLU" problems where neurons can
     * become permanently inactive. It is defined as {@code f(x) = x} if
     * {@code x > 0}, and {@code f(x) = 0.01 * x} otherwise.
     * </p>
     * <ul>
     *   <li>Range: (-∞, ∞)</li>
     *   <li>Behavior: Acts like ReLU for positive inputs but has a small
     *       positive slope for negative inputs.</li>
     * </ul>
     */
    public static final DoubleUnaryOperator LEAKY_RELU = x -> x > 0 ? x : 0.01 * x;


    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ActivationFunction() {
        // This class should not be instantiated.
    }
}