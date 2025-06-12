package de.mkoehler.neat.examples.cartpole_centering;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CartPoleVisualizer extends JPanel {
    // ... (Constants like WIDTH, HEIGHT, SCALE remain the same) ...
    private static final int WIDTH = 800;
    private static final int HEIGHT = 400;
    private static final double WORLD_WIDTH = 2.4 * 2;
    private static final double SCALE = WIDTH / WORLD_WIDTH;
    private static final int CART_Y = HEIGHT - 100;

    private double cartX;
    private double poleTheta;
    private final MainCartPoleCentering mainApp; // A reference back to the main class

    public CartPoleVisualizer(MainCartPoleCentering mainApp) {
        this.mainApp = mainApp;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.LIGHT_GRAY);

        // Add a mouse listener to handle clicks
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // We need the screen position of the cart to see if click was left or right
                int cartScreenX = (int) (WIDTH / 2.0 + cartX * SCALE);
                if (e.getX() < cartScreenX) {
                    mainApp.handleDisturbance(-0.5); // Kick to the left (negative impulse)
                } else {
                    mainApp.handleDisturbance(0.5); // Kick to the right (positive impulse)
                }
            }
        });
    }

    public void updateState(double cartX, double poleTheta) {
        this.cartX = cartX;
        this.poleTheta = poleTheta;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        // ... (The paintComponent method is exactly the same as before) ...
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.BLACK);
        g2d.drawLine(0, CART_Y, WIDTH, CART_Y);
        int cartScreenX = (int) (WIDTH / 2.0 + this.cartX * SCALE);
        int cartWidth = (int) (0.5 * SCALE);
        int cartHeight = (int) (0.25 * SCALE);
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(cartScreenX - cartWidth / 2, CART_Y - cartHeight / 2, cartWidth, cartHeight);
        int poleLength = (int) (1.0 * SCALE);
        g2d.setColor(Color.ORANGE);
        g2d.setStroke(new BasicStroke(8));
        var oldTransform = g2d.getTransform();
        g2d.translate(cartScreenX, CART_Y);
        g2d.rotate(this.poleTheta);
        g2d.drawLine(0, 0, 0, -poleLength);
        g2d.setTransform(oldTransform);
    }
}