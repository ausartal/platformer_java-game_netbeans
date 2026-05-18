package com.mycompany.rpggame.graphics;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class Sprite {
    public static BufferedImage load(String name) {
        try {
            InputStream is = Sprite.class.getResourceAsStream("/res/" + name);
            if (is == null) {
                is = Sprite.class.getClassLoader().getResourceAsStream("res/" + name);
            }
            if (is == null) return null;
            return ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
