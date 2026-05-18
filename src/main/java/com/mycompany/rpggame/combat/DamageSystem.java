package com.mycompany.rpggame.combat;

import com.mycompany.rpggame.entity.Entity;

import java.awt.*;

public class DamageSystem {
    public static void checkAndApply(Attack atk, Entity target) {
        if (!atk.isActive()) return;
        Rectangle tbox = target.getHitbox();
        if (atk.hitbox.intersects(tbox)) {
            target.takeDamage(atk.damage);
            atk.startTime = System.currentTimeMillis() - atk.duration - 1;
        }
    }
}
