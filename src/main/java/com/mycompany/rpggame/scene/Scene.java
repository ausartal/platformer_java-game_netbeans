package com.mycompany.rpggame.scene;

import java.awt.*;

public interface Scene {
    void onEnter();
    void onExit();
    void update();
    void render(Graphics2D g);
}
