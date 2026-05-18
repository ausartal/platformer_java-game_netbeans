package com.mycompany.rpggame.utils;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;

public class Sound {
    public static void play(String resourcePath) {
        try (InputStream is = Sound.class.getResourceAsStream("/res/" + resourcePath)) {
            if (is == null) return;
            AudioInputStream ais = AudioSystem.getAudioInputStream(is);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            // ignore
        }
    }
}
