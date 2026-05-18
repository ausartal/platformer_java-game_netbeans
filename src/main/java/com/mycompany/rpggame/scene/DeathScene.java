package com.mycompany.rpggame.scene;

import com.mycompany.rpggame.input.KeyHandler;

import javax.swing.*;
import java.awt.*;

public class DeathScene implements Scene {
    private SceneManager manager;
    private JPanel panel;
    private KeyHandler key;
    private long enterTime;

    public DeathScene(SceneManager m, JPanel panel, KeyHandler key) {
        this.manager = m;
        this.panel = panel;
        this.key = key;
    }

    @Override
    public void onEnter() { enterTime = System.currentTimeMillis(); }

    @Override
    public void onExit() { }

    @Override
    public void update() {
        if (key.isPressed(java.awt.event.KeyEvent.VK_R)) manager.startGame();
        if (key.isPressed(java.awt.event.KeyEvent.VK_M)) manager.backToMenu();
    }

    @Override
    public void render(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillRect(0,0,panel.getWidth(), panel.getHeight());
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("You Died", 420, 240);
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.drawString("Press R to Retry or M to Menu", 400, 300);
    }
}
