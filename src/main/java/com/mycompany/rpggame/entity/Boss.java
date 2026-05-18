package com.mycompany.rpggame.entity;

import com.mycompany.rpggame.graphics.Animation;
import com.mycompany.rpggame.graphics.Sprite;
import com.mycompany.rpggame.world.TileMap;
import com.mycompany.rpggame.combat.Attack;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Boss extends Entity {

    private final TileMap map;

    // movement
    private double velX = 0;
    private double velY = 0;
    private final double gravity = 0.6;
    private final double walkSpeed = 1.2;
    private final double dashSpeed = 5.2;
    private final double jumpStrength = -10;

    // attack core
    private Rectangle meleeBox = new Rectangle();
    private int attackDamage = 18;

    private long lastAttackTime = 0;
    private final long baseAttackCooldown = 900;
    private long attackCooldown = baseAttackCooldown;

    private boolean attackSpawned = false;
    private long attackSpawnOffset = 120;
    private long attackFinishTime = 350;

    // boss phases
    private final int maxHp;
    private boolean phase2 = false;

    // teleport + multidash phase2
    private boolean invisible = false;
    private boolean teleported = false;
    private long teleportStart = 0;

    private int dashCount = 0;
    private final int MAX_DASHES = 3;

    // attacks from boss
    public List<Attack> spawnedAttacks = new ArrayList<>();

    // states
    private enum State {
        IDLE, CHASE, ATTACK_PREP, ATTACKING, RECOVER,
        TELEPORT, MULTIDASH
    }

    private State state = State.IDLE;
    private long stateEnteredAt = System.currentTimeMillis();

    private final long attackWindup = 250;
    private final Random rnd = new Random();

    // animation
    private Animation animIdle, animWalk, animAttack, animJump, animHurt, animDeath;
    private BufferedImage currentFrame;

    private boolean facingRight = true;

    public Boss(double x, double y, TileMap map) {
        super(x, y, 80, 96, 600);
        this.map = map;
        this.maxHp = this.hp;

        // load sprites
        animIdle = new Animation(Sprite.load("boss_idle.png"), 10);
        animWalk = new Animation(Sprite.load("boss_walk.png"), 8);
        animAttack = new Animation(Sprite.load("boss_squatAttack.png"), 8);
        animJump = new Animation(Sprite.load("boss_jump.png"), 8);
        animHurt = new Animation(Sprite.load("boss_hurt.png"), 6);
        animDeath = new Animation(Sprite.load("boss_death.png"), 8);

        enterState(State.CHASE);
    }

    @Override
    public void update() {}

    private void enterState(State s) {
        state = s;
        stateEnteredAt = System.currentTimeMillis();
    }

    private void facePlayer(Player p) {
        facingRight = (p.x + p.width/2.0) > (x + width/2.0);
    }

    private boolean canAttack() {
        return System.currentTimeMillis() - lastAttackTime >= attackCooldown;
    }

    // ─────────────────────────────────────
    //             PHASE 2 EXTRAS
    // ─────────────────────────────────────

    private void enterTeleport(Player player) {
        enterState(State.TELEPORT);
        teleported = false;
        invisible = true;
        teleportStart = System.currentTimeMillis();
        velX = 0;
    }

    private void handleTeleport(Player player) {
        long t = System.currentTimeMillis() - teleportStart;

        // disappear for 250ms then appear behind
        if (!teleported && t > 250) {
            boolean toRight = player.x < x;

            x = player.x + (toRight ? 60 : -60);
            y = player.y;

            teleported = true;
            invisible = false;

            dashCount = 0;
            enterMultiDash(player);
        }
    }

    private void enterMultiDash(Player player) {
        enterState(State.MULTIDASH);
        dashCount = 0;
        performDashToward(player);
    }

    private void performDashToward(Player player) {
        boolean toRight = player.x > x;
        facingRight = toRight;

        attackDamage = 40;

        int w = 130, h = 52;
        int ax = (int)(x + (toRight ? width : -w));
        int ay = (int)(y + height/2 - h/2);

        meleeBox.setBounds(ax, ay, w, h);
        velX = toRight ? dashSpeed * 1.6 : -dashSpeed * 1.6;

        attackSpawnOffset = 100;
        attackFinishTime = 230;

        attackSpawned = false;
    }

    // ─────────────────────────────────────
    //           NORMAL ATTACKS
    // ─────────────────────────────────────

    private void doMeleeCombo(boolean toRight) {
        attackDamage = phase2 ? 30 : 18;

        int w = 90, h = 48;
        int ax = (int)(x + (toRight ? width : -w));
        int ay = (int)(y + height/2 - h/2);

        meleeBox.setBounds(ax, ay, w, h);
        velX = toRight ? 1.8 : -1.8;

        attackSpawnOffset = 120;
        attackFinishTime = 350;
    }

    private void doDashAttack(boolean toRight) {
        attackDamage = phase2 ? 36 : 26;

        int w = 130, h = 52;
        int ax = (int)(x + (toRight ? width : -w));
        int ay = (int)(y + height/2 - h/2);

        meleeBox.setBounds(ax, ay, w, h);
        velX = toRight ? dashSpeed : -dashSpeed;

        attackSpawnOffset = 120;
        attackFinishTime = 500;
    }

    private void doHopSlam(boolean toRight) {
        attackDamage = phase2 ? 28 : 20;

        velY = jumpStrength;

        int w = 100, h = 60;
        int ax = (int)(x - (w - width)/2.0);
        int ay = (int)(y + height);

        meleeBox.setBounds(ax, ay, w, h);

        velX = toRight ? 0.6 : -0.6;

        attackSpawnOffset = 160;
        attackFinishTime = 600;
    }

    private void spawnProjectile(boolean toRight) {
        int size = 20;
        int sx = (int)(x + (toRight ? width + 4 : -size - 4));
        int sy = (int)(y + height/2 - size/2);

        Rectangle box = new Rectangle(sx, sy, size, size);
        Attack a = new Attack(box, phase2 ? 14 : 9, 1200);
        spawnedAttacks.add(a);
    }

    private void spawnMeleeAttack() {
        Rectangle r = new Rectangle(meleeBox);
        Attack a = new Attack(r, attackDamage, attackFinishTime - attackSpawnOffset);
        spawnedAttacks.add(a);
    }

    // ─────────────────────────────────────
    //               MAIN UPDATE
    // ─────────────────────────────────────

    public void update(Player player) {

        velY += gravity;
        if (velY > 12) velY = 12;

        long now = System.currentTimeMillis();

        double playerCenter = player.x + player.width/2.0;
        double dist = Math.abs((x + width/2.0) - playerCenter);

        // enter phase 2
        if (!phase2 && hp < maxHp * 0.40) {
            phase2 = true;
            attackCooldown = (long)(baseAttackCooldown * 0.6);
        }

        // phase 2 teleport trigger
        if (phase2 && state == State.CHASE && dist < 180 && rnd.nextDouble() < 0.008) {
            enterTeleport(player);
        }

        switch(state) {

            case IDLE:
                animIdle.update();
                if (now - stateEnteredAt > 700) enterState(State.CHASE);
                break;

            case CHASE:
                animWalk.update();
                facePlayer(player);

                // follow
                if (dist > 60) velX = facingRight ? walkSpeed : -walkSpeed;
                else velX = 0;

                // projectile
                if (dist < 240 && rnd.nextDouble() < (phase2 ? 0.02 : 0.008)) {
                    spawnProjectile(facingRight);
                }

                // melee
                if (canAttack() && dist < 160) {
                    enterState(State.ATTACK_PREP);
                    animAttack.reset();
                }
                break;

            case ATTACK_PREP:
                velX = 0;
                animAttack.update();
                facePlayer(player);

                if (now - stateEnteredAt >= attackWindup) performMelee(player);
                break;

            case ATTACKING:
                animAttack.update();
                facePlayer(player);

                long t = now - stateEnteredAt;

                if (!attackSpawned && t >= attackSpawnOffset) {
                    spawnMeleeAttack();
                    attackSpawned = true;
                }

                if (t > attackFinishTime) {
                    enterState(State.RECOVER);
                }
                break;

            case RECOVER:
                animIdle.update();
                if (now - stateEnteredAt > 300) enterState(State.CHASE);
                break;

            case TELEPORT:
                handleTeleport(player);
                break;

            case MULTIDASH:
                animAttack.update();

                long td = now - stateEnteredAt;

                if (!attackSpawned && td >= attackSpawnOffset) {
                    spawnMeleeAttack();
                    attackSpawned = true;
                }

                if (td > attackFinishTime) {
                    dashCount++;
                    if (dashCount < MAX_DASHES) {
                        performDashToward(player);
                        stateEnteredAt = System.currentTimeMillis();
                    } else {
                        velX = 0;
                        enterState(State.RECOVER);
                    }
                }
                break;
        }

        // collisions
        double nextX = x + velX;
        double nextY = y + velY;

        if (velX > 0) {
            if (map.isSolidAt(nextX + width, y + height - 2)) velX = 0;
        } else if (velX < 0) {
            if (map.isSolidAt(nextX, y + height - 2)) velX = 0;
        }
        x += velX;

        if (velY > 0) {
            int foot = (int)(nextY + height);
            if (map.isSolidAt(x + 10, foot)) velY = 0;
        }
        y += velY;

        hitbox.setBounds((int)x,(int)y,width,height);

        // select sprite frame
        if (!isAlive()) {
            animDeath.update();
            currentFrame = animDeath.getFrame();
        } else switch(state) {
            case IDLE: currentFrame = animIdle.getFrame(); break;
            case CHASE: currentFrame = animWalk.getFrame(); break;
            case ATTACK_PREP:
            case ATTACKING:
            case MULTIDASH: currentFrame = animAttack.getFrame(); break;
            case RECOVER: currentFrame = animIdle.getFrame(); break;
            case TELEPORT: currentFrame = animIdle.getFrame(); break;
        }
    }

    private void performMelee(Player p) {
        attackSpawned = false;

        facePlayer(p);
        boolean toRight = facingRight;

        double r = rnd.nextDouble();
        if (phase2) r += 0.2;

        if (r < 0.15)      doDashAttack(toRight);
        else if (r < 0.6) doMeleeCombo(toRight);
        else              doHopSlam(toRight);

        lastAttackTime = System.currentTimeMillis();
        enterState(State.ATTACKING);
        animAttack.reset();
    }

    @Override
    public void takeDamage(int dmg) {
        super.takeDamage(dmg);

        animHurt.reset();
        velX = rnd.nextBoolean() ? -1.5 : 1.5;

        enterState(State.RECOVER);
    }

    @Override
    public void render(Graphics2D g) {
        if (!invisible && currentFrame != null) {

            if (!facingRight)
                g.drawImage(currentFrame, (int)x + width, (int)y, -width, height, null);
            else
                g.drawImage(currentFrame, (int)x, (int)y, width, height, null);
        }

        // DEBUG: draw hitboxes
        for (Attack a : spawnedAttacks) {
            Rectangle r = a.getHitbox();
            g.setColor(new Color(180,180,255,120));
            g.fillRect(r.x, r.y, r.width, r.height);
        }
    }

    public List<Attack> pullSpawnedAttacks() {
        List<Attack> out = new ArrayList<>(spawnedAttacks);
        spawnedAttacks.clear();
        return out;
    }
}
