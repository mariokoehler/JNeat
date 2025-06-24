// File: src/main/java/de/mkoehler/neat/examples/soccer/SoccerVisualizer.java
package de.mkoehler.neat.examples.soccer;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

public class SoccerVisualizer extends JPanel {

    private SoccerEnvironment env;

    public SoccerVisualizer() {
        setPreferredSize(new Dimension(SoccerEnvironment.FIELD_WIDTH, SoccerEnvironment.FIELD_HEIGHT));
        setBackground(new Color(0x00, 0x64, 0x00)); // Dark green
    }

    public void updateEnvironment(SoccerEnvironment env) {
        this.env = env;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (env == null) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw goals
        g2d.setColor(Color.WHITE);
        g2d.fill(env.goal1);
        g2d.fill(env.goal2);

        // Draw Player 1
        g2d.setColor(Color.BLUE);
        drawPlayer(g2d, env.player1);

        // Draw Player 2
        g2d.setColor(Color.RED);
        drawPlayer(g2d, env.player2);

        // Draw Ball
        g2d.setColor(Color.WHITE);
        g2d.fill(new Ellipse2D.Double(
                env.ball.position.x - SoccerBall.RADIUS,
                env.ball.position.y - SoccerBall.RADIUS,
                SoccerBall.RADIUS * 2,
                SoccerBall.RADIUS * 2
        ));
    }

    private void drawPlayer(Graphics2D g2d, SoccerPlayer player) {
        g2d.fill(new Ellipse2D.Double(
                player.position.x - SoccerPlayer.RADIUS,
                player.position.y - SoccerPlayer.RADIUS,
                SoccerPlayer.RADIUS * 2,
                SoccerPlayer.RADIUS * 2
        ));

        // Draw a line to indicate direction
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(
                (int) player.position.x,
                (int) player.position.y,
                (int) (player.position.x + Math.cos(player.angle) * (SoccerPlayer.RADIUS + 5)),
                (int) (player.position.y + Math.sin(player.angle) * (SoccerPlayer.RADIUS + 5))
        );
        g2d.setStroke(new BasicStroke(1));
    }
}