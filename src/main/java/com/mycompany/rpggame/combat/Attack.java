package com.mycompany.rpggame.combat;

import java.awt.*;

public class Attack {
    public Rectangle hitbox;
    public int damage;
    public long startTime;
    public long duration;

    public Attack(Rectangle hitbox, int damage, long duration) {
        this.hitbox = new Rectangle(hitbox);
        this.damage = damage;
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
    }

    public boolean isActive() {
        return System.currentTimeMillis() - startTime < duration;
    }

    public Rectangle getHitbox() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
