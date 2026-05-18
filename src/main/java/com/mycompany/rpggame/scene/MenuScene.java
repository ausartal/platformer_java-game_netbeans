package com.mycompany.rpggame.scene;

import com.mycompany.rpggame.input.KeyHandler;

import javax.swing.*;
import java.awt.*;

public class MenuScene implements Scene {

    private SceneManager manager;
    private JPanel panel;
    private KeyHandler key;

    // Blinking text animation
    private float alpha = 1.0f;
    private boolean fade = false;

    public MenuScene(SceneManager m, JPanel panel, KeyHandler key) {
        this.manager = m;
        this.panel = panel;
        this.key = key;
    }

    @Override
    public void onEnter() { }

    @Override
    public void onExit() { }

    @Override
    public void update() {

        // ENTER to start
        if (key.isPressed(java.awt.event.KeyEvent.VK_ENTER)) {
            manager.startGame();
        }

        // Blinking effect
        if (fade) {
            alpha -= 0.02f;
            if (alpha <= 0.2f) fade = false;
        } else {
            alpha += 0.02f;
            if (alpha >= 1.0f) fade = true;
        }
    }

    @Override
    public void render(Graphics2D g) {

        int w = panel.getWidth();
        int h = panel.getHeight();

        // Background (simple dark gradient) 
        GradientPaint bg = new GradientPaint(
                0, 0, new Color(15, 15, 25),
                0, h, new Color(5, 5, 10)
        );
        g.setPaint(bg);
        g.fillRect(0, 0, w, h);

        // === Title (centered) ===
        String title = "Knightfall: Endless Arena";
        g.setFont(new Font("Serif", Font.BOLD, 58));

        FontMetrics fm = g.getFontMetrics();
        int titleX = (w - fm.stringWidth(title)) / 2;
        int titleY = h / 2 - 60;

        // Title shadow
        g.setColor(new Color(0, 0, 0, 150));
        g.drawString(title, titleX + 4, titleY + 4);

        // Title pure white
        g.setColor(Color.WHITE);
        g.drawString(title, titleX, titleY);

        // === "Press Enter" centered & blinking ===
        String enterText = "Press ENTER to start";
        g.setFont(new Font("SansSerif", Font.PLAIN, 28));
        fm = g.getFontMetrics();
        int enterX = (w - fm.stringWidth(enterText)) / 2;

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.setColor(Color.WHITE);
        g.drawString(enterText, enterX, titleY + 50);

        // Reset opacity
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        // Controls Info 
        String controls = "Move: A/D | Jump: W/Space | Attack: J/Z | Dash: Shift";
        g.setFont(new Font("SansSerif", Font.PLAIN, 18));
        fm = g.getFontMetrics();

        int ctrlX = (w - fm.stringWidth(controls)) / 2;
        int ctrlY = h - 40; // bottom with spacing

        g.setColor(Color.WHITE);
        g.drawString(controls, ctrlX, ctrlY);
    }
}
