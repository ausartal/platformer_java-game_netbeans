package com.mycompany.rpggame.world;

import java.awt.*;

public class TileMap {

    public static double TILE_SIZE = 32;
    public Tile[][] tiles;
    public int rows, cols;
    public int offsetY = 350;

    public TileMap(int[][] layout) {
        rows = layout.length;
        cols = layout[0].length;
        tiles = new Tile[rows][cols];

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                int id = layout[y][x];

                if (id == 1) {
                    tiles[y][x] = new Tile(null, true);   // solid
                } else {
                    tiles[y][x] = new Tile(null, false);  // kosong
                }
            }
        }
    }

    public void render(Graphics2D g) {

        int screenW = g.getClipBounds().width;
        int screenH = g.getClipBounds().height;

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {

                Tile t = tiles[y][x];
                if (!t.solid) continue;

                int tileX = x * Tile.SIZE;
                int tileY = y * Tile.SIZE + offsetY;
                int tileH = screenH - tileY;

                // BAGIAN GRADIENT 
                int purpleHeight = (int) (tileH * 0.30);  // 20% ungu

                GradientPaint grad = new GradientPaint(
                        tileX, tileY,
                        new Color(80, 0, 80, 200),    // ungu gelap
                        tileX, tileY + purpleHeight,
                        new Color(0, 0, 0, 255)       // transition ke hitam
                );

                // Render gradient 20% atas
                g.setPaint(grad);
                g.fillRect(tileX, tileY, Tile.SIZE, purpleHeight);

                // BAGIAN HITAM PADAT (80%) ---
                g.setColor(Color.BLACK);
                g.fillRect(tileX, tileY + purpleHeight, Tile.SIZE, tileH - purpleHeight);
            }
        }
    }

    public boolean isSolidAt(double wx, double wy) {
        wy -= offsetY;

        int tx = (int)(wx / Tile.SIZE);
        int ty = (int)(wy / Tile.SIZE);

        if (tx < 0 || ty < 0 || tx >= cols || ty >= rows)
            return false;

        return tiles[ty][tx].solid;
    }
}
    