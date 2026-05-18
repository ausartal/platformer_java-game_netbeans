package com.mycompany.rpggame.graphics;

import java.awt.image.BufferedImage;

public class Animation {
    private BufferedImage[] frames;
    private int speed; 
    private int index = 0;
    private int counter = 0;

    public Animation(BufferedImage sheet, int speed) {
        this.speed = speed;
        if (sheet == null) {
            frames = new BufferedImage[0];
            return;
        }
        int fh = sheet.getHeight();
        int frameCount = Math.max(1, sheet.getWidth() / fh);
        frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = sheet.getSubimage(i * fh, 0, fh, fh);
        }
    }

    public void update() {
        if (frames.length == 0) return;
        counter++;
        if (counter >= speed) {
            counter = 0;
            index = (index + 1) % frames.length;
        }
    }

    public java.awt.image.BufferedImage getFrame() {
        if (frames.length == 0) return null;
        return frames[index];
    }

    public void reset() {
        index = 0; counter = 0;
    }
}
