package de.mkoehler.neat.examples.cartpole;

import java.util.Random;

/**
 * Simulates the classic Cart-Pole balancing problem.
 * This class handles the physics and state of the environment,
 * completely separate from the NEAT algorithm.
 */
public class CartPoleEnvironment {

    // --- Physics Constants ---
    private static final double GRAVITY = 9.8;
    private static final double MASS_CART = 1.0;
    private static final double MASS_POLE = 0.1;
    private static final double TOTAL_MASS = MASS_POLE + MASS_CART;
    private static final double POLE_HALF_LENGTH = 0.5; // Actually half the pole's length
    private static final double POLE_MASS_LENGTH = MASS_POLE * POLE_HALF_LENGTH;
    private static final double FORCE_MAG = 10.0;
    private static final double TAU = 0.02; // Seconds between state updates

    // --- Failure Conditions ---
    private static final double X_THRESHOLD = 2.4; // Cart position limit
    private static final double THETA_THRESHOLD_RADIANS = 12 * 2 * Math.PI / 360; // Pole angle limit

    // --- State Variables ---
    private double x;              // Cart Position
    private double x_dot;          // Cart Velocity
    private double theta;          // Pole Angle (radians)
    private double theta_dot;      // Pole Angular Velocity

    private final Random random = new Random();

    /**
     * Initializes the environment with a random starting state near equilibrium.
     */
    public CartPoleEnvironment() {
        // Start with small random values to make the problem non-deterministic
        this.x = random.nextDouble() * 0.1 - 0.05;
        this.x_dot = random.nextDouble() * 0.1 - 0.05;
        this.theta = random.nextDouble() * 0.1 - 0.05;
        this.theta_dot = random.nextDouble() * 0.1 - 0.05;
    }

    /**
     * Updates the environment state by one time step based on the applied action.
     * @param action 0 to push left, 1 to push right.
     */
    public void update(int action) {
        double force = (action == 1) ? FORCE_MAG : -FORCE_MAG;

        double cosTheta = Math.cos(theta);
        double sinTheta = Math.sin(theta);

        // Physics equations from the classic paper by Barto, Sutton, and Anderson
        double temp = (force + POLE_MASS_LENGTH * theta_dot * theta_dot * sinTheta) / TOTAL_MASS;
        double theta_acc = (GRAVITY * sinTheta - cosTheta * temp) /
                (POLE_HALF_LENGTH * (4.0 / 3.0 - MASS_POLE * cosTheta * cosTheta / TOTAL_MASS));
        double x_acc = temp - POLE_MASS_LENGTH * theta_acc * cosTheta / TOTAL_MASS;

        // Update state using Euler integration
        x += TAU * x_dot;
        x_dot += TAU * x_acc;
        theta += TAU * theta_dot;
        theta_dot += TAU * theta_acc;
    }

    /**
     * Returns the current state of the environment.
     * @return A double array containing [cart position, cart velocity, pole angle, pole angular velocity].
     */
    public double[] getState() {
        return new double[]{x, x_dot, theta, theta_dot};
    }

    /**
     * Checks if the simulation has reached a terminal state (failure).
     * @return True if the pole has fallen or the cart has gone off the track.
     */
    public boolean isDone() {
        return Math.abs(x) > X_THRESHOLD || Math.abs(theta) > THETA_THRESHOLD_RADIANS;
    }

    /**
     * Applies an external force impulse to the pole's angular velocity.
     * This can be used to test the stability of a controller.
     * @param impulse The amount of velocity to add (can be positive or negative).
     */
    public void applyDisturbance(double impulse) {
        this.theta_dot += impulse;
    }

}