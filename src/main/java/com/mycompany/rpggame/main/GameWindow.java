package com.mycompany.rpggame.main;

import javax.swing.*;
import java.awt.*;

public class GameWindow extends JFrame {

    private GamePanel panel;
    private final int width, height;

    public GameWindow(String title, int width, int height) {
        super(title);
        this.width = width;
        this.height = height;

        // Basic window config
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        setUndecorated(true);                       
        setExtendedState(JFrame.MAXIMIZED_BOTH);   

        // GamePanel
        panel = new GamePanel(width, height);

        // Tempel GamePanel di tengah 
        add(panel, BorderLayout.CENTER);
        pack();

        setLocationRelativeTo(null);
    }

    public void start() {
        setVisible(true);
        panel.startGameLoop();
    }
}
