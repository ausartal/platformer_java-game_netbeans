package com.mycompany.rpggame.main;

import com.mycompany.rpggame.input.KeyHandler;
import com.mycompany.rpggame.scene.SceneManager;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {
    public static final int SCALE = 1;
    private int width, height;
    private Thread gameThread;
    private boolean running = false;
    private SceneManager sceneManager;
    private KeyHandler keyHandler;

    public GamePanel(int width, int height) {
        this.width = width;
        this.height = height;

        setPreferredSize(new Dimension(width * SCALE, height * SCALE));
        setFocusable(true);
        requestFocusInWindow();

        keyHandler = new KeyHandler();
        addKeyListener(keyHandler);

        sceneManager = new SceneManager(this, keyHandler);
    }

    public void startGameLoop() {
        if (running) return;
        running = true;

        gameThread = new Thread(() -> {
            final double FPS = 60.0;
            final double nsPerFrame = 1_000_000_000.0 / FPS;

            long lastTime = System.nanoTime();
            double delta = 0;

            while (running) {
                long now = System.nanoTime();
                delta += (now - lastTime) / nsPerFrame;
                lastTime = now;

                if (delta >= 1) {
                    update();
                    repaint();
                    delta--;
                }

                try { Thread.sleep(1); } catch (Exception ignored) {}
            }
        }, "GameLoopThread");

        gameThread.start();
    }

    private void update() {
        sceneManager.update();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        sceneManager.render(g2);

        g2.dispose();
    }
    @Override
    public Dimension getPreferredSize() {
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    return new Dimension(screen.width, screen.height);
    }
}


