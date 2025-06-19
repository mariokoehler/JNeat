// File: src/main/java/de/mkoehler/neat/examples/car/Track.java
package de.mkoehler.neat.examples.car;

import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines the geometry of the race track, including boundaries and checkpoints.
 */
public class Track {

    private final Path2D outerBoundary;
    private final Path2D innerBoundary;
    private final Path2D fullTrackShape;
    private final List<Line2D> checkpoints; // CHANGED from Rectangle2D to Line2D
    private final Point2D startPosition;
    private final double startAngle;

    public Track() {
        this.outerBoundary = new Path2D.Double();
        this.innerBoundary = new Path2D.Double();

        // Define a sample track shape
        outerBoundary.moveTo(100, 100);
        outerBoundary.curveTo(300, 50, 600, 150, 700, 300);
        outerBoundary.curveTo(800, 500, 600, 600, 400, 550);
        outerBoundary.curveTo(200, 500, 50, 400, 100, 100);
        outerBoundary.closePath();

        innerBoundary.moveTo(200, 200);
        innerBoundary.curveTo(350, 150, 550, 250, 600, 350);
        innerBoundary.curveTo(650, 450, 500, 500, 400, 450);
        innerBoundary.curveTo(250, 400, 150, 300, 200, 200);
        innerBoundary.closePath();

        // Create a single path for easy collision checks (contains)
        this.fullTrackShape = new Path2D.Double(outerBoundary);
        this.fullTrackShape.append(innerBoundary, false);

        // --- START OF FIX: Procedurally generate checkpoints ---
        this.checkpoints = generateCheckpoints(10); // Generate 10 checkpoints
        // --- END OF FIX ---

        // Start position and angle for the car
        this.startPosition = new Point2D.Double(150, 150);
        this.startAngle = 0; // Pointing right
    }

    // UPDATED getter
    public List<Line2D> getCheckpoints() { return checkpoints; }

    // --- NEW METHOD to generate checkpoints ---
    private List<Line2D> generateCheckpoints(int count) {
        List<Line2D> generated = new ArrayList<>();
        List<Point2D> outerPoints = getPointsFromPath(outerBoundary);
        List<Point2D> innerPoints = getPointsFromPath(innerBoundary);

        if (outerPoints.isEmpty() || innerPoints.isEmpty()) {
            return generated;
        }

        int step = outerPoints.size() / count;
        for (int i = 0; i < count; i++) {
            Point2D outerPoint = outerPoints.get(i * step);
            Point2D closestInnerPoint = findClosestPoint(outerPoint, innerPoints);
            generated.add(new Line2D.Double(outerPoint, closestInnerPoint));
        }
        return generated;
    }

    private List<Point2D> getPointsFromPath(Path2D path) {
        List<Point2D> points = new ArrayList<>();
        // A FlatteningPathIterator approximates curves with straight line segments
        PathIterator pi = path.getPathIterator(null, 1.0); // 1.0 is flatness
        double[] coords = new double[6];
        while (!pi.isDone()) {
            int type = pi.currentSegment(coords);
            if (type == PathIterator.SEG_LINETO || type == PathIterator.SEG_MOVETO) {
                points.add(new Point2D.Double(coords[0], coords[1]));
            }
            pi.next();
        }
        return points;
    }

    private Point2D findClosestPoint(Point2D from, List<Point2D> toPoints) {
        Point2D closest = null;
        double minDistanceSq = Double.POSITIVE_INFINITY;
        for (Point2D p : toPoints) {
            double distSq = from.distanceSq(p);
            if (distSq < minDistanceSq) {
                minDistanceSq = distSq;
                closest = p;
            }
        }
        return closest;
    }

    // --- Unchanged methods below ---
    public Path2D getOuterBoundary() { return outerBoundary; }
    public Path2D getInnerBoundary() { return innerBoundary; }
    public Point2D getStartPosition() { return startPosition; }
    public double getStartAngle() { return startAngle; }
    public boolean contains(Point2D point) {
        return fullTrackShape.contains(point) && !innerBoundary.contains(point);
    }
}