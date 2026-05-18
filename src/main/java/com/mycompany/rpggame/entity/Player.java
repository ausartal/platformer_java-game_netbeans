package com.mycompany.rpggame.entity;

import com.mycompany.rpggame.graphics.Animation;
import com.mycompany.rpggame.graphics.Sprite;
import com.mycompany.rpggame.input.KeyHandler;
import com.mycompany.rpggame.world.TileMap;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Player extends Entity {
    public double x,y;
    private double velX = 0, velY = 0;
    private final double speed = 3.0;
    private final double jumpStrength = -10;
    private final double gravity = 0.6;
    private boolean onGround = false;
    private boolean canDash = true;
    private long dashTime = 0;
    private final long dashCooldown = 800; // ms
    private final double dashSpeed = 8;
    private final KeyHandler key;
    private boolean attacking = false;
    private long attackTimer = 0;

    private Animation animIdle, animRun, animAttack, animJump, animHurt, animDeath;
    private BufferedImage currentFrame;
    private TileMap map;

    public int attackDamage = 10;
    public Rectangle attackHitbox = new Rectangle();

    public Player(double x, double y, KeyHandler key, TileMap map) {
        super(x, y, 48, 48, 200);
        this.x = x; this.y = y;
        this.key = key;
        this.map = map;
        animIdle = new Animation(Sprite.load("Player_Idle.png"), 10);
        animRun = new Animation(Sprite.load("Player_Run.png"), 6);
        animAttack = new Animation(Sprite.load("Player_Attack.png"), 6);
        animJump = new Animation(Sprite.load("Player_Jump.png"), 8);
        animHurt = new Animation(Sprite.load("Player_Hurt.png"), 8);
        animDeath = new Animation(Sprite.load("Player_Death.png"), 8);
    }

    @Override
    public void update() {
        boolean left = key.isPressed(java.awt.event.KeyEvent.VK_A) || key.isPressed(java.awt.event.KeyEvent.VK_LEFT);
        boolean right = key.isPressed(java.awt.event.KeyEvent.VK_D) || key.isPressed(java.awt.event.KeyEvent.VK_RIGHT);
        boolean jump = key.isPressed(java.awt.event.KeyEvent.VK_W) || key.isPressed(java.awt.event.KeyEvent.VK_SPACE);
        boolean dash = key.isPressed(java.awt.event.KeyEvent.VK_SHIFT);
        boolean attack = key.isPressed(java.awt.event.KeyEvent.VK_J) || key.isPressed(java.awt.event.KeyEvent.VK_Z);

        if (left) { velX = -speed; facingRight = false; }
        else if (right) { velX = speed; facingRight = true; }
        else velX = 0;

        if (dash && canDash) {
            canDash = false;
            dashTime = System.currentTimeMillis();
            velX = facingRight ? dashSpeed : -dashSpeed;
        }
        if (!canDash && System.currentTimeMillis() - dashTime > dashCooldown) {
            canDash = true;
        }

        if (jump && onGround) {
            velY = jumpStrength;
            onGround = false;
        }

        // apply gravity
        velY += gravity;
        if (velY > 12) velY = 12;

        // horizontal collision check using tilemap
        double nextX = x + velX;
        if (velX > 0) {
            if (map.isSolidAt(nextX + width, y + height - 1) || map.isSolidAt(nextX + width, y + 1)) {
                velX = 0;
            }
        } else if (velX < 0) {
            if (map.isSolidAt(nextX, y + height - 1) || map.isSolidAt(nextX, y + 1)) {
                velX = 0;
            }
        }

        // vertical collision
        double nextY = y + velY;
        if (velY > 0) {
            if (map.isSolidAt(x + 5, nextY + height) || map.isSolidAt(x + width - 5, nextY + height)) {
                velY = 0;
                onGround = true;
                // snap to tile top
                //flooring
//                y = Math.floor((nextY + height) / TileMap.TILE_SIZE) * TileMap.TILE_SIZE - height;
            }
        } else if (velY < 0) {
            if (map.isSolidAt(x + 5, nextY) || map.isSolidAt(x + width - 5, nextY)) {
                velY = 0;
            }
        }

        x += velX;
        y += velY;

        // attack handling
        if (attack && !attacking) {
            attacking = true;
            attackTimer = System.currentTimeMillis();
            animAttack.reset();
        }
        if (attacking) {
            animAttack.update();
            if (System.currentTimeMillis() - attackTimer > 300) {
                attacking = false;
            }
        }

        // animation selection
        if (!isAlive()) {
            animDeath.update(); currentFrame = animDeath.getFrame();
        } else if (attacking) currentFrame = animAttack.getFrame();
        else if (!onGround) { animJump.update(); currentFrame = animJump.getFrame(); }
        else if (velX != 0) { animRun.update(); currentFrame = animRun.getFrame(); }
        else { animIdle.update(); currentFrame = animIdle.getFrame(); }

        hitbox.setBounds((int)x, (int)y, width, height);
        int aw = 40, ah = 28;
        if (facingRight) attackHitbox.setBounds((int)x + width, (int)y + 10, aw, ah);
        else attackHitbox.setBounds((int)x - aw, (int)y + 10, aw, ah);
    }

    @Override
    public void render(Graphics2D g) {
        if (currentFrame != null) {
            int drawX = (int)x;
            int drawY = (int)y;
            if (!facingRight) {
                g.drawImage(currentFrame, drawX + width, drawY, -width, height, null);
            } else {
                g.drawImage(currentFrame, drawX, drawY, width, height, null);
            }
        } else {
            g.setColor(Color.BLUE);
            g.fillRect((int)x, (int)y, width, height);
        }
    }

    public boolean isAttacking() { return attacking; }
}
