// File: src/main/java/de/mkoehler/neat/examples/car/Track.java
package de.mkoehler.neat.examples.car;

import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines the geometry of the race track, including boundaries and checkpoints.
 * This version is constructed from clean, pre-defined SVG path data for maximum
 * stability and easy editing via external tools.
 */
public class Track {

    private final Path2D outerBoundary;
    private final Path2D innerBoundary;
    private final Path2D fullTrackShape;
    private final List<Line2D> checkpoints;
    private final Point2D startPosition;
    private final double startAngle;

    public Track() {
        // --- 1. PASTE SVG PATH DATA FROM YOUR EDITOR HERE ---

        // This data defines a complex, stable "pretzel" track.
        String outerPathData = "M 88 38 C 126 17 183 17 231 16 C 284 17 338 19 393 25 C 471 39.3333 550 44 627 68 C 670 91 708 122 736 163 C 753 199 775 238 773 277 C 768 321 741 379 706 398 C 664 420 594 443 544 420 C 497 398 449 347 434 295 C 423 254 422 183 401 172 C 376 159 339 154 318 167 C 300 185 281 209 279 236 C 282 262 304 302 315 330 C 323.6667 357.6667 353 387 341 413 C 319 436 275 450 241 443 C 191.3333 426.3333 121 428 92 393 C 57 347 43 275 43 210 C 42 154 47 100 65 56 Z";
        String innerPathData = "M 125 85 C 160 78 193.6667 77 228 73 C 279 70 334 75 386 81 C 458 90 530 108 600 127 C 623 138 647 162 667 186 C 684 208 695 245 699 274 C 699 293 681 316 666 331 C 641 340 597 337 569 326 C 548 316 535 279.3333 518 256 C 503.3333 220 495 174 474 148 C 450 128 418 121 389 110 C 356 103 318 107 285 118 C 258 133 233 193 219 230 C 209 262 235.6667 298.6667 244 333 C 248 343 254 357 250 369 C 247 376 240 380 233 379 C 210 382 176 361 154 345 C 130 316 110 252 98 202 C 98 165 107.3333 131.3333 112 96 Z";

        // --- 2. Construct the track from the path data ---
        this.outerBoundary = createPathFromSvg(outerPathData);
        this.innerBoundary = createPathFromSvg(innerPathData);

        // This method of creating the track shape is stable.
        this.fullTrackShape = new Path2D.Double(outerBoundary);
        this.fullTrackShape.append(innerBoundary, false);

        // --- 3. Generate checkpoints (this part is still robust) ---
        this.checkpoints = generateCheckpoints(12);

        // --- 4. Define a safe start position and angle ---
        this.startPosition = new Point2D.Double(133, 53);
        this.startAngle = 0.3;
    }

    /**
     * A helper to parse an SVG path data string and create a Path2D object.
     * This is a simplified parser that only handles M, C, and Z commands.
     */
    private Path2D.Double createPathFromSvg(String svgData) {
        Path2D.Double path = new Path2D.Double();
        String[] commands = svgData.split(" (?=[a-zA-Z])"); // Split by letter commands

        for (String command : commands) {
            String op = command.substring(0, 1);
            String[] parts = command.substring(1).trim().split("[ ,]+");

            try {
                if (op.equalsIgnoreCase("M")) {
                    path.moveTo(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
                } else if (op.equalsIgnoreCase("C")) {
                    path.curveTo(
                            Double.parseDouble(parts[0]), Double.parseDouble(parts[1]),
                            Double.parseDouble(parts[2]), Double.parseDouble(parts[3]),
                            Double.parseDouble(parts[4]), Double.parseDouble(parts[5])
                    );
                } else if (op.equalsIgnoreCase("Z")) {
                    path.closePath();
                }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                System.err.println("Error parsing SVG path command: " + command);
            }
        }
        return path;
    }

    // --- The rest of the class uses the same reliable helpers as before ---

    private List<Line2D> generateCheckpoints(int count) {
        List<Line2D> generated = new ArrayList<>();
        List<Point2D> outerPoints = getPointsFromPath(outerBoundary);
        List<Point2D> innerPoints = getPointsFromPath(innerBoundary);

        if (outerPoints.isEmpty() || innerPoints.isEmpty()) {
            return generated;
        }

        int step = outerPoints.size() / count;
        for (int i = 0; i < count; i++) {
            Point2D outerPoint = outerPoints.get((i * step + step / 2) % outerPoints.size());
            Point2D closestInnerPoint = findClosestPoint(outerPoint, innerPoints);
            generated.add(new Line2D.Double(outerPoint, closestInnerPoint));
        }
        return generated;
    }

    private List<Point2D> getPointsFromPath(Path2D path) {
        List<Point2D> points = new ArrayList<>();
        PathIterator pi = path.getPathIterator(null, 1.0);
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

    public List<Line2D> getCheckpoints() { return checkpoints; }
    public Path2D getOuterBoundary() { return outerBoundary; }
    public Path2D getInnerBoundary() { return innerBoundary; }
    public Point2D getStartPosition() { return startPosition; }
    public double getStartAngle() { return startAngle; }

    public boolean contains(Point2D point) {
        return fullTrackShape.contains(point) && !innerBoundary.contains(point);
    }
}