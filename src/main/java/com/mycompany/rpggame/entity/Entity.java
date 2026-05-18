package com.mycompany.rpggame.entity;

import java.awt.*;

public abstract class Entity {
    public double x;
    public double y;
    public int width;
    protected int height;
    public int hp;
    protected boolean facingRight = true;
    public Rectangle hitbox;

    public Entity(double x, double y, int w, int h, int hp) {
        this.x = x; this.y = y; this.width = w; this.height = h; this.hp = hp;
        hitbox = new Rectangle((int)x, (int)y, w, h);
    }

    public abstract void update();
    public abstract void render(Graphics2D g);

    public boolean isAlive() { return hp > 0; }

    public void takeDamage(int dmg) {
        hp -= dmg;
        if (hp < 0) hp = 0;
    }

    public Rectangle getHitbox() {
        hitbox.setBounds((int)x, (int)y, width, height);
        return hitbox;
    }
}
