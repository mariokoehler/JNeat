// File: src/main/java/de/mkoehler/neat/examples/soccer/SoccerBall.java
package de.mkoehler.neat.examples.soccer;

import java.awt.geom.Point2D;

/**
 * Represents the ball in the soccer game.
 */
public class SoccerBall {
    public Point2D.Double position;
    public Point2D.Double velocity;

    private static final double DRAG = 0.98;
    public static final double RADIUS = 10.0;

    public SoccerBall(double x, double y) {
        this.position = new Point2D.Double(x, y);
        this.velocity = new Point2D.Double(0, 0);
    }

    public void update() {
        this.position.x += this.velocity.x;
        this.position.y += this.velocity.y;
        this.velocity.x *= DRAG;
        this.velocity.y *= DRAG;
    }
}