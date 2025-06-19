// File: src/main/java/de/mkoehler/neat/examples/car/Car.java
package de.mkoehler.neat.examples.car;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the car agent, including its physics, sensors, and state.
 */
public class Car {

    // --- Car properties ---
    public Point2D.Double position;
    public double angle; // in radians
    public double speed;
    public boolean isCrashed = false;
    private final Rectangle2D.Double shape;

    // --- Sensor properties ---
    private final int numSensors;
    private final double sensorAngleSpread;
    private final double sensorRange;
    public final List<Double> sensorReadings;
    public final List<Line2D> sensorRays; // For visualization

    public Car(Point2D startPos, double startAngle, int numSensors) {
        this.position = new Point2D.Double(startPos.getX(), startPos.getY());
        this.angle = startAngle;
        this.speed = 0;
        this.shape = new Rectangle2D.Double(-10, -5, 20, 10); // Car shape relative to its center

        this.numSensors = numSensors;
        this.sensorAngleSpread = Math.PI * 0.9; // 162 degrees
        this.sensorRange = 150.0;
        this.sensorReadings = new ArrayList<>(numSensors);
        this.sensorRays = new ArrayList<>(numSensors);
        for(int i = 0; i < numSensors; i++) {
            sensorReadings.add(1.0); // Default reading (no obstacle)
            sensorRays.add(new Line2D.Double());
        }
    }

    /**
     * Updates the car's state based on network outputs and checks for collisions.
     */
    public void update(double steering, double acceleration, Track track) {
        if (isCrashed) return;

        // --- Physics Update ---
        this.speed += acceleration;
        this.speed *= 0.98; // Apply some drag
        if (this.speed > 5.0) this.speed = 5.0; // Max speed
        if (this.speed < -2.0) this.speed = -2.0; // Max reverse speed

        if (speed != 0) {
            this.angle += steering * (speed / 5.0); // Steering is more effective at higher speeds
        }

        this.position.x += this.speed * Math.cos(this.angle);
        this.position.y += this.speed * Math.sin(this.angle);

        // --- Collision Check ---
        if (!track.contains(this.position)) {
            this.isCrashed = true;
        }
    }

    /**
     * Casts sensor rays and updates the sensor readings.
     */
    public void castRays(Track track) {
        if (isCrashed) return;

        for (int i = 0; i < numSensors; i++) {
            double sensorAngle;
            if (numSensors > 1) {
                sensorAngle = this.angle - (sensorAngleSpread / 2) + (sensorAngleSpread * i / (numSensors - 1));
            } else {
                sensorAngle = this.angle;
            }

            double endX = this.position.x + sensorRange * Math.cos(sensorAngle);
            double endY = this.position.y + sensorRange * Math.sin(sensorAngle);
            Line2D.Double ray = new Line2D.Double(this.position.x, this.position.y, endX, endY);
            sensorRays.set(i, ray);

            // Find the closest intersection with track boundaries
            Point2D closestIntersection = findIntersection(ray, track);
            double distance = (closestIntersection != null) ? this.position.distance(closestIntersection) : sensorRange;

            // Normalize the reading: 1.0 means no obstacle, 0.0 means obstacle is very close
            sensorReadings.set(i, 1.0 - (distance / sensorRange));
        }
    }

    /**
     * Helper to find the closest intersection point of a ray with the track.
     */
    private Point2D findIntersection(Line2D ray, Track track) {
        Point2D closest = null;
        double minDistance = Double.POSITIVE_INFINITY;

        // Check against both inner and outer boundaries
        for (Path2D boundary : List.of(track.getInnerBoundary(), track.getOuterBoundary())) {
            PathIterator pi = boundary.getPathIterator(null);
            double[] coords = new double[6];
            Point2D.Double start = new Point2D.Double();
            Point2D.Double current = new Point2D.Double();

            while (!pi.isDone()) {
                int type = pi.currentSegment(coords);
                if (type == PathIterator.SEG_MOVETO) {
                    start.setLocation(coords[0], coords[1]);
                    current.setLocation(coords[0], coords[1]);
                } else if (type == PathIterator.SEG_LINETO) {
                    Line2D.Double segment = new Line2D.Double(current, new Point2D.Double(coords[0], coords[1]));
                    if (ray.intersectsLine(segment)) {
                        Point2D intersection = getIntersectionPoint(ray, segment);
                        if (intersection != null) {
                            double dist = ray.getP1().distance(intersection);
                            if (dist < minDistance) {
                                minDistance = dist;
                                closest = intersection;
                            }
                        }
                    }
                    current.setLocation(coords[0], coords[1]);
                } else if (type == PathIterator.SEG_CLOSE) {
                    Line2D.Double segment = new Line2D.Double(current, start);
                    if (ray.intersectsLine(segment)) {
                        Point2D intersection = getIntersectionPoint(ray, segment);
                        if (intersection != null) {
                            double dist = ray.getP1().distance(intersection);
                            if (dist < minDistance) {
                                minDistance = dist;
                                closest = intersection;
                            }
                        }
                    }
                }
                pi.next();
            }
        }
        return closest;
    }

    // Standard line-line intersection algorithm
    private Point2D getIntersectionPoint(Line2D line1, Line2D line2) {
        double x1 = line1.getX1(), y1 = line1.getY1(), x2 = line1.getX2(), y2 = line1.getY2();
        double x3 = line2.getX1(), y3 = line2.getY1(), x4 = line2.getX2(), y4 = line2.getY2();

        double den = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (den == 0) return null; // Parallel

        double t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / den;
        double u = -((x1 - x2) * (y1 - y3) - (y1 - y2) * (x1 - x3)) / den;

        if (t >= 0 && t <= 1 && u >= 0 && u <= 1) {
            return new Point2D.Double(x1 + t * (x2 - x1), y1 + t * (y2 - y1));
        }
        return null; // No intersection within segments
    }


    public Shape getTransformedShape() {
        AffineTransform at = new AffineTransform();
        at.translate(position.getX(), position.getY());
        at.rotate(angle);
        return at.createTransformedShape(this.shape);
    }
}