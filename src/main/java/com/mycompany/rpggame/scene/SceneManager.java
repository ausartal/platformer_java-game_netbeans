    package com.mycompany.rpggame.scene;

    import com.mycompany.rpggame.input.KeyHandler;

    import javax.swing.*;
    import java.awt.*;

    public class SceneManager {
        private JPanel panel;
        private KeyHandler key;
        private Scene current;

        public SceneManager(JPanel panel, KeyHandler key) {
            this.panel = panel;
            this.key = key;
            setScene(new MenuScene(this, panel, key));
        }

        public void setScene(Scene s) {
            if (current != null) current.onExit();
            current = s;
            current.onEnter();
        }

        public void update() {
            if (current != null) current.update();
        }

        public void render(Graphics2D g) {
            if (current != null) current.render(g);
        }

        public void startGame() {
            setScene(new GameScene(this, panel, key));
        }

        public void goToDeath() {
            setScene(new DeathScene(this, panel, key));
        }

        public void backToMenu() {
            setScene(new MenuScene(this, panel, key));
        }
    }
