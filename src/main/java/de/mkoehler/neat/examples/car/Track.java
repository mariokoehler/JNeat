// File: src/main/java/de/mkoehler/neat/examples/car/Track.java
package de.mkoehler.neat.examples.car;

import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines the geometry of the race track, including boundaries and checkpoints.
 */
public class Track {

    private final Path2D outerBoundary;
    private final Path2D innerBoundary;
    private final Path2D fullTrackShape;
    private final List<Rectangle2D> checkpoints;
    private final Point2D startPosition;
    private final double startAngle;

    public Track() {
        this.outerBoundary = new Path2D.Double();
        this.innerBoundary = new Path2D.Double();
        this.checkpoints = new ArrayList<>();

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

        // Define checkpoints
        checkpoints.add(new Rectangle(350, 50, 20, 150));
        checkpoints.add(new Rectangle(650, 200, 100, 20));
        checkpoints.add(new Rectangle(600, 450, 20, 150));
        checkpoints.add(new Rectangle(200, 500, 150, 20));
        checkpoints.add(new Rectangle(100, 200, 20, 150));

        // Start position and angle for the car
        this.startPosition = new Point2D.Double(150, 150);
        this.startAngle = 0; // Pointing right
    }

    public Path2D getOuterBoundary() { return outerBoundary; }
    public Path2D getInnerBoundary() { return innerBoundary; }
    public List<Rectangle2D> getCheckpoints() { return checkpoints; }
    public Point2D getStartPosition() { return startPosition; }
    public double getStartAngle() { return startAngle; }

    /**
     * Checks if a given point is within the track boundaries.
     * @param point The point to check.
     * @return true if the point is on the track, false otherwise.
     */
    public boolean contains(Point2D point) {
        return fullTrackShape.contains(point) && !innerBoundary.contains(point);
    }
}