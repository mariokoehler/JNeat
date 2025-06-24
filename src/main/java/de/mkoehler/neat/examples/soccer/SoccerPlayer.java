// File: src/main/java/de/mkoehler/neat/examples/soccer/SoccerPlayer.java
package de.mkoehler.neat.examples.soccer;

import lombok.Getter;

import java.awt.geom.Point2D;

/**
 * Represents a single player agent in the soccer game.
 * Manages its own position, velocity, and physics.
 */
public class SoccerPlayer {

    public Point2D.Double position;
    public Point2D.Double velocity;
    public double angle; // in radians
    public final int team; // 0 for home, 1 for away

    // Physics constants
    public static final double MAX_SPEED = 4.0;
    private static final double THRUST_FORCE = 0.3;
    private static final double TURN_RATE = 0.1;
    private static final double DRAG = 0.96;
    public static final double RADIUS = 15.0;

    public SoccerPlayer(double x, double y, double startAngle, int team) {
        this.position = new Point2D.Double(x, y);
        this.velocity = new Point2D.Double(0, 0);
        this.angle = startAngle;
        this.team = team;
    }

    /**
     * Updates the player's state based on network outputs.
     * @param turn      Value from -1 (left) to 1 (right).
     * @param thrust    Value from 0 (coast) to 1 (full throttle).
     */
    public void update(double turn, double thrust) {
        // Apply rotation
        this.angle += turn * TURN_RATE;

        // Apply thrust
        if (thrust > 0) {
            double forceX = Math.cos(this.angle) * THRUST_FORCE * thrust;
            double forceY = Math.sin(this.angle) * THRUST_FORCE * thrust;
            this.velocity.x += forceX;
            this.velocity.y += forceY;
        }

        // Enforce max speed
        double speed = this.velocity.distance(0, 0);
        if (speed > MAX_SPEED) {
            this.velocity.x = (this.velocity.x / speed) * MAX_SPEED;
            this.velocity.y = (this.velocity.y / speed) * MAX_SPEED;
        }

    }
}