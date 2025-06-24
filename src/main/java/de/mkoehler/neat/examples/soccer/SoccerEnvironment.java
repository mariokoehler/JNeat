// File: src/main/java/de/mkoehler/neat/examples/soccer/SoccerEnvironment.java
package de.mkoehler.neat.examples.soccer;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;
/**
 * Manages the entire state of a 1-vs-1 soccer match.
 * Handles physics, collisions, and provides sensory data for the agents.
 */
public class SoccerEnvironment {

    // Field dimensions
    public static final int FIELD_WIDTH = 800;
    public static final int FIELD_HEIGHT = 500;
    public static final int GOAL_WIDTH = 150;
    private static final int GOAL_DEPTH = 20;

    public SoccerPlayer player1;
    public SoccerPlayer player2;
    public SoccerBall ball;

    public final Rectangle2D goal1; // Team 0's goal (left)
    public final Rectangle2D goal2; // Team 1's goal (right)

    public int score1 = 0;
    public int score2 = 0;

    private final Random random = new Random();

    public SoccerEnvironment() {
        goal1 = new Rectangle2D.Double(0, (FIELD_HEIGHT - GOAL_WIDTH) / 2.0, GOAL_DEPTH, GOAL_WIDTH);
        goal2 = new Rectangle2D.Double(FIELD_WIDTH - GOAL_DEPTH, (FIELD_HEIGHT - GOAL_WIDTH) / 2.0, GOAL_DEPTH, GOAL_WIDTH);
        reset();
    }

    /**
     * Resets the players and ball to their starting positions.
     */
    public void reset() {
        player1 = new SoccerPlayer(FIELD_WIDTH * 0.25, FIELD_HEIGHT / 2.0, 0, 0);
        player2 = new SoccerPlayer(FIELD_WIDTH * 0.75, FIELD_HEIGHT / 2.0, Math.PI, 1);

        // Add vertical variance to the ball's starting position.
        // A range of +/- 50 pixels from the center is a good start.
        double yVariance = (random.nextDouble() * 100.0) - 50.0;
        double startY = (FIELD_HEIGHT / 2.0) + yVariance;

        ball = new SoccerBall(FIELD_WIDTH / 2.0, startY);

        score1 = 0;
        score2 = 0;
    }

    /**
     * Generates the sensory input array for a given player.
     * @param player The player for whom to generate inputs.
     * @return A double array of normalized sensory data.
     */
    public double[] getInputsFor(SoccerPlayer player) {
        SoccerPlayer opponent = (player == player1) ? player2 : player1;
        Rectangle2D ownGoal = (player.team == 0) ? goal1 : goal2;
        Rectangle2D opponentGoal = (player.team == 0) ? goal2 : goal1;

        // Get world-coordinate delta vectors (same as before)
        Point2D.Double toBall_world = new Point2D.Double(ball.position.x - player.position.x, ball.position.y - player.position.y);
        Point2D.Double toOwnGoal_world = new Point2D.Double(getCenter(ownGoal).x - player.position.x, getCenter(ownGoal).y - player.position.y);
        Point2D.Double toOpponentGoal_world = new Point2D.Double(getCenter(opponentGoal).x - player.position.x, getCenter(opponentGoal).y - player.position.y);
        Point2D.Double toOpponent_world = new Point2D.Double(opponent.position.x - player.position.x, opponent.position.y - player.position.y);

        // Rotate these vectors to be relative to the player's current angle.
        // This transforms them from world-space to player-space (egocentric).
        Point2D.Double toBall_local = rotateVector(toBall_world, -player.angle);
        Point2D.Double toOwnGoal_local = rotateVector(toOwnGoal_world, -player.angle);
        Point2D.Double toOpponentGoal_local = rotateVector(toOpponentGoal_world, -player.angle);
        Point2D.Double toOpponent_local = rotateVector(toOpponent_world, -player.angle);

        // Also rotate the player's own velocity vector, so it "sees" its momentum
        // relative to the direction it's facing (e.g., am I strafing left or moving forward?).
        Point2D.Double velocity_local = rotateVector(player.velocity, -player.angle);

        // Normalize the transformed vectors
        double maxDist = FIELD_WIDTH; // Use a single large normalizer
        double maxSpeed = SoccerPlayer.MAX_SPEED;

        // Also normalize the ball's velocity
        double ballVelX = ball.velocity.x / maxSpeed;
        double ballVelY = ball.velocity.y / maxSpeed;

        return new double[]{
                toBall_local.x / maxDist, toBall_local.y / maxDist,
                toOwnGoal_local.x / maxDist, toOwnGoal_local.y / maxDist,
                toOpponentGoal_local.x / maxDist, toOpponentGoal_local.y / maxDist,
                toOpponent_local.x / maxDist, toOpponent_local.y / maxDist,
                velocity_local.x / maxSpeed, velocity_local.y / maxSpeed,
                ballVelX, ballVelY
        };

    }

    /**
     * A new helper method to rotate a 2D vector.
     * @param vector The vector (as a Point2D) to rotate.
     * @param angleRad The angle in radians to rotate by.
     * @return The new, rotated vector.
     */
    private Point2D.Double rotateVector(Point2D.Double vector, double angleRad) {
        double cosA = Math.cos(angleRad);
        double sinA = Math.sin(angleRad);
        double newX = vector.x * cosA - vector.y * sinA;
        double newY = vector.x * sinA + vector.y * cosA;
        return new Point2D.Double(newX, newY);
    }

    private Point2D.Double normalize(Point2D.Double target, Point2D.Double source, double max_dist) {
        return new Point2D.Double(
                (target.x - source.x) / max_dist,
                (target.y - source.y) / max_dist
        );
    }

    public Point2D.Double getCenter(Rectangle2D rect) {
        return new Point2D.Double(rect.getCenterX(), rect.getCenterY());
    }

    private void handleCollisions() {
        // Player-Wall
        collideWithWalls(player1);
        collideWithWalls(player2);

        // Ball-Wall (but NOT in front of the goal)
        boolean inLeftGoalArea = ball.position.y > goal1.getY() && ball.position.y < goal1.getY() + goal1.getHeight();
        boolean inRightGoalArea = ball.position.y > goal2.getY() && ball.position.y < goal2.getY() + goal2.getHeight();

        if ((ball.position.x < SoccerBall.RADIUS && !inLeftGoalArea) || (ball.position.x > FIELD_WIDTH - SoccerBall.RADIUS && !inRightGoalArea)) {
            ball.velocity.x *= -0.8;
            ball.position.x = Math.max(SoccerBall.RADIUS, Math.min(FIELD_WIDTH - SoccerBall.RADIUS, ball.position.x));
        }
        if (ball.position.y < SoccerBall.RADIUS || ball.position.y > FIELD_HEIGHT - SoccerBall.RADIUS) {
            ball.velocity.y *= -0.8;
            ball.position.y = Math.max(SoccerBall.RADIUS, Math.min(FIELD_HEIGHT - SoccerBall.RADIUS, ball.position.y));
        }

        // Player-Ball
        collidePlayerBall(player1);
        collidePlayerBall(player2);

        // Player-Player
        double distSq = player1.position.distanceSq(player2.position);
        double min_dist = SoccerPlayer.RADIUS * 2;
        if (distSq < min_dist * min_dist) {
            resolveCollision(player1, player2);
        }
    }

    public void kick(SoccerPlayer player) {
        double distSq = player.position.distanceSq(ball.position);
        double kick_radius = SoccerPlayer.RADIUS + SoccerBall.RADIUS + 5; // Allow kick from slightly further away
        if(distSq < kick_radius * kick_radius) {
            double angleToBall = Math.atan2(ball.position.y - player.position.y, ball.position.x - player.position.x);
            ball.velocity.x += Math.cos(angleToBall) * 8.0;
            ball.velocity.y += Math.sin(angleToBall) * 8.0;
        }
    }

    private void collideWithWalls(SoccerPlayer p) {
        if (p.position.x < SoccerPlayer.RADIUS) { p.position.x = SoccerPlayer.RADIUS; p.velocity.x *= -0.5; }
        if (p.position.x > FIELD_WIDTH - SoccerPlayer.RADIUS) { p.position.x = FIELD_WIDTH - SoccerPlayer.RADIUS; p.velocity.x *= -0.5; }
        if (p.position.y < SoccerPlayer.RADIUS) { p.position.y = SoccerPlayer.RADIUS; p.velocity.y *= -0.5; }
        if (p.position.y > FIELD_HEIGHT - SoccerPlayer.RADIUS) { p.position.y = FIELD_HEIGHT - SoccerPlayer.RADIUS; p.velocity.y *= -0.5; }
    }

    private void collidePlayerBall(SoccerPlayer p) {
        double distSq = p.position.distanceSq(ball.position);
        double min_dist = SoccerPlayer.RADIUS + SoccerBall.RADIUS;
        if (distSq < min_dist * min_dist) {
            // Basic collision response
            ball.velocity.x += p.velocity.x * 0.4;
            ball.velocity.y += p.velocity.y * 0.4;
            resolveCollision(p, ball);
        }
    }

    private void resolveCollision(SoccerPlayer p1, SoccerPlayer p2) {
        // Simple push-apart logic for two players
        double dx = p2.position.x - p1.position.x;
        double dy = p2.position.y - p1.position.y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        double overlap = (SoccerPlayer.RADIUS * 2) - dist;
        if(overlap > 0) {
            double pushX = (dx / dist) * overlap * 0.5;
            double pushY = (dy / dist) * overlap * 0.5;
            p1.position.x -= pushX; p1.position.y -= pushY;
            p2.position.x += pushX; p2.position.y += pushY;
        }
    }

    private void resolveCollision(SoccerPlayer p, SoccerBall b) {
        // Push-apart logic for player and ball
        double dx = b.position.x - p.position.x;
        double dy = b.position.y - p.position.y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        double overlap = (SoccerPlayer.RADIUS + SoccerBall.RADIUS) - dist;
        if(overlap > 0) {
            double pushX = (dx / dist) * overlap;
            double pushY = (dy / dist) * overlap;
            b.position.x += pushX; b.position.y += pushY;
        }
    }

    private boolean checkGoal() {
        // A goal is scored if the ball *completely crosses* the goal line.
        if (ball.position.x < GOAL_DEPTH && goal1.contains(ball.position)) {
            score2++;
            reset();
            return true;
        }
        if (ball.position.x > FIELD_WIDTH - GOAL_DEPTH && goal2.contains(ball.position)) {
            score1++;
            reset();
            return true;
        }
        return false;
    }

    /**
     * Applies the actions from the neural networks to the players.
     * This should be called before `runPhysicsStep()`.
     */
    public void applyActions(double[] outputs1, double[] outputs2) {
        // Player 1 (left)
        double turn1 = outputs1[0];
        double thrust1 = (outputs1[1] + 1) / 2.0; // Tanh output is [-1, 1]
        player1.update(turn1, thrust1);
        if (outputs1.length > 2 && (outputs1[2] + 1) / 2.0 > 0.7) {
            kick(player1);
        }

        // Player 2 (right)
        double turn2 = outputs2[0];
        double thrust2 = (outputs2[1] + 1) / 2.0;
        player2.update(turn2, thrust2);
        if (outputs2.length > 2 && (outputs2[2] + 1) / 2.0 > 0.7) {
            kick(player2);
        }
    }

    /**
     * Runs one physics step: updates positions, handles all collisions, and checks for goals.
     * This should be called after `applyActions()`.
     * @return True if a goal was scored, false otherwise.
     */
    public boolean runPhysicsStep() {

        // 1. Update positions based on current velocities for ALL entities.
        player1.position.x += player1.velocity.x;
        player1.position.y += player1.velocity.y;
        player2.position.x += player2.velocity.x;
        player2.position.y += player2.velocity.y;
        ball.update(); // ball.update() already handles this internally

        // 2. Handle all collisions, which may modify positions and velocities.
        handleCollisions();

        // 3. Apply drag to players after all movement and collisions for this step.
        player1.velocity.x *= 0.96;
        player1.velocity.y *= 0.96;
        player2.velocity.x *= 0.96;
        player2.velocity.y *= 0.96;

        return checkGoal();
    }

}