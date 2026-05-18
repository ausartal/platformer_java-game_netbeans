package com.mycompany.rpggame.world;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Tile {
    public static final int SIZE = 42;
    public BufferedImage sprite;
    public boolean solid;

    public Tile(BufferedImage sprite, boolean solid) {
        this.sprite = sprite;
        this.solid = solid;
    }

    public void render(java.awt.Graphics2D g, int x, int y) {
        if (sprite != null) g.drawImage(sprite, x, y, SIZE, SIZE, null);
    }
}
