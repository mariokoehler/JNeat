package de.mkoehler.neat.examples.memory;

import java.awt.geom.Point2D;
import java.util.Random;

/**
 * Manages the state for the two-stage memory challenge.
 * 1. Agent must find the Food.
 * 2. Only after finding the Food, the Goal becomes the new target.
 */
public class MemoryEnvironment {

    public final Point2D.Double agentPos;
    public final Point2D.Double foodPos;
    public final Point2D.Double goalPos;

    private Point2D.Double agentVel;
    private final Random random = new Random();

    // Environment state
    public boolean goalActive = false;
    public boolean taskComplete = false;
    public int foodEatenTime = -1;

    // Simulation constants
    private static final double MAX_SPEED = 3.0;
    private static final double ARENA_WIDTH = 500;
    private static final double ARENA_HEIGHT = 500;
    private static final double FOOD_RADIUS = 10.0;
    private static final double BORDER_MARGIN = 50; // Don't spawn items too close to the edge

    public MemoryEnvironment() {
        // 1. Agent always starts in the center. No more lucky starts.
        this.agentPos = new Point2D.Double(ARENA_WIDTH / 2, ARENA_HEIGHT / 2);
        this.agentVel = new Point2D.Double(0, 0);

        // 2. Place food and goal in opposite quadrants to ensure a challenge.
        double centerX = ARENA_WIDTH / 2;
        double centerY = ARENA_HEIGHT / 2;
        double spawnableWidth = centerX - BORDER_MARGIN;
        double spawnableHeight = centerY - BORDER_MARGIN;

        // Determine quadrants for food and goal randomly.
        boolean foodInTopLeft = random.nextBoolean();
        boolean foodUsesXFirst = random.nextBoolean(); // Vary placement within the quadrant

        double foodX, foodY, goalX, goalY;

        if (foodInTopLeft) {
            // Food in top-left quadrant, Goal in bottom-right
            foodX = centerX - (random.nextDouble() * spawnableWidth);
            foodY = centerY - (random.nextDouble() * spawnableHeight);
            goalX = centerX + (random.nextDouble() * spawnableWidth);
            goalY = centerY + (random.nextDouble() * spawnableHeight);
        } else {
            // Food in top-right quadrant, Goal in bottom-left
            foodX = centerX + (random.nextDouble() * spawnableWidth);
            foodY = centerY - (random.nextDouble() * spawnableHeight);
            goalX = centerX - (random.nextDouble() * spawnableWidth);
            goalY = centerY + (random.nextDouble() * spawnableHeight);
        }

        // Randomly swap goal and food positions for more variety
        if (random.nextBoolean()){
            this.foodPos = new Point2D.Double(foodX, foodY);
            this.goalPos = new Point2D.Double(goalX, goalY);
        } else {
            this.foodPos = new Point2D.Double(goalX, goalY);
            this.goalPos = new Point2D.Double(foodX, foodY);
        }
    }

    /**
     * Provides the sensory input for the neural network.
     * Crucially, it hides the goal's location until the food has been eaten.
     * @return A double array of the agent's sensory inputs.
     */
    public double[] getState() {
        // Normalize all inputs to be roughly in the [-1, 1] range
        double foodDx = (foodPos.x - agentPos.x) / ARENA_WIDTH;
        double foodDy = (foodPos.y - agentPos.y) / ARENA_HEIGHT;

        double goalDx = 0;
        double goalDy = 0;
        if (goalActive) {
            goalDx = (goalPos.x - agentPos.x) / ARENA_WIDTH;
            goalDy = (goalPos.y - agentPos.y) / ARENA_HEIGHT;
        }

        double velX = agentVel.x / MAX_SPEED;
        double velY = agentVel.y / MAX_SPEED;

        return new double[]{foodDx, foodDy, goalDx, goalDy, velX, velY};
    }

    /**
     * Updates the agent's position and the environment state based on network output.
     * @param output A double array from the network, representing force in x and y directions.
     * @param currentStep The current time step of the simulation.
     */
    public void update(double[] output, int currentStep) {
        if (taskComplete) return;

        // Apply force from network output
        agentVel.x += (output[0] * 2 - 1); // Map [0,1] to [-1,1]
        agentVel.y += (output[1] * 2 - 1);

        // Enforce max speed
        double speed = agentVel.distance(0, 0);
        if (speed > MAX_SPEED) {
            agentVel.x = (agentVel.x / speed) * MAX_SPEED;
            agentVel.y = (agentVel.y / speed) * MAX_SPEED;
        }

        // Update position
        agentPos.x += agentVel.x;
        agentPos.y += agentVel.y;

        // Wall collision
        if (agentPos.x < 0 || agentPos.x > ARENA_WIDTH) { agentVel.x *= -0.5; agentPos.x = Math.max(0, Math.min(ARENA_WIDTH, agentPos.x)); }
        if (agentPos.y < 0 || agentPos.y > ARENA_HEIGHT) { agentVel.y *= -0.5; agentPos.y = Math.max(0, Math.min(ARENA_HEIGHT, agentPos.y)); }

        // Check for task progression
        if (!goalActive) {
            if (agentPos.distance(foodPos) < FOOD_RADIUS) {
                goalActive = true;
                foodEatenTime = currentStep;
            }
        } else {
            if (agentPos.distance(goalPos) < FOOD_RADIUS) {
                taskComplete = true;
            }
        }
    }
}