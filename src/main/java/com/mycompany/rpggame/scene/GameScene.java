package com.mycompany.rpggame.scene;

import com.mycompany.rpggame.combat.Attack;
import com.mycompany.rpggame.combat.DamageSystem;
import com.mycompany.rpggame.entity.Boss;
import com.mycompany.rpggame.entity.Player;
import com.mycompany.rpggame.input.KeyHandler;
import com.mycompany.rpggame.world.TileMap;

import javax.swing.*;
import java.awt.*;
import java.awt.BasicStroke;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameScene implements Scene {

    private SceneManager manager;
    private JPanel panel;
    private KeyHandler key;
    private Player player;
    private Boss boss;
    private List<Attack> attacks = new ArrayList<>();
    private TileMap map;
    private boolean escHeld = false;

    // Transition
    private boolean entering = true;
    private float enterAlpha = 0f;
    private long enterStartNano = 0L;
    private final long ENTER_DURATION_NS = 600_000_000L;

    // Screen shake
    private float shakeTime = 0f;
    private float shakeDuration = 0f;
    private float shakeIntensity = 0f;
    private int shakeOffsetX = 0;
    private int shakeOffsetY = 0;

    // Damage numbers
    private static class DamageNumber {
        String text;
        float x, y;
        float vy = -0.4f;
        float life = 1.0f;
        Color color;
        DamageNumber(String t, float x, float y, Color c) {
            this.text = t; this.x = x; this.y = y; this.color = c;
        }
    }
    private final List<DamageNumber> dmgNumbers = new ArrayList<>();

    // Pause menu
    private boolean paused = false;
    private int pauseSelection = 0;

    // Death overlay
    private boolean isDead = false;
    private int deathSelection = 0;

    // Combo system
    private int combo = 0;
    private float comboTimer = 0f;
    private final float COMBO_TIMEOUT = 2.0f;

    // Stamina (optional support)
    private boolean hasStaminaMethod = false;
    private Method staminaGetter = null;
    private float localStamina = 100f;
    private final float STAMINA_MAX = 100f;
    private final float STAMINA_COST_ATTACK = 20f;
    private final float STAMINA_REGEN = 12f;

    // Last HP tracking
    private int lastPlayerHP = 0;
    private int lastBossHP = 0;

    private BufferedImage bgCache = null;

    public GameScene(SceneManager m, JPanel panel, KeyHandler key) {
        this.manager = m;
        this.panel = panel;
        this.key = key;
    }

    @Override
    public void onEnter() {

        int[][] layout = {
                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
        };

        map = new TileMap(layout);
        player = new Player(120, map.offsetY - 80, key, map);
        boss = new Boss(700, map.offsetY - 80, map);

        attacks.clear();
        dmgNumbers.clear();
        paused = false;
        isDead = false;

        combo = 0;
        comboTimer = 0f;

        localStamina = STAMINA_MAX;

        lastPlayerHP = player.hp;
        lastBossHP = boss.hp;

        // try stamina method
        try {
            staminaGetter = player.getClass().getMethod("getStamina");
            hasStaminaMethod = true;
        } catch (Exception e) {
            hasStaminaMethod = false;
        }

        entering = true;
        enterAlpha = 0f;
        enterStartNano = System.nanoTime();
    }

    @Override
    public void onExit() {}

    @Override
    public void update() {

        // ENTER TRANSITION
        if (entering) {
            long now = System.nanoTime();
            enterAlpha = Math.min(1f, (float)(now - enterStartNano) / ENTER_DURATION_NS);
            if (enterAlpha >= 1f) entering = false;
        }

        // ESC = Pause toggle
        if (key.isPressed(KeyEvent.VK_ESCAPE)) {
            if (!escHeld && !isDead) {
                paused = !paused;
                pauseSelection = 0;
                escHeld = true;
            }
        } else escHeld = false;

        if (paused || isDead) {
            handleMenuInput();
            updateEffects(1f/60f);
            return;
        }

        // CORE GAME LOGIC
        player.update();
        boss.update(player);
        
        if (player.isAttacking()) {
            attacks.add(new Attack(new Rectangle(player.attackHitbox), player.attackDamage, 140));
        }
        
        List<Attack> bossAtk = boss.pullSpawnedAttacks();
        attacks.addAll(bossAtk);

        // Damage handling
        Iterator<Attack> it = attacks.iterator();
        while (it.hasNext()) {
            Attack a = it.next();

            int pb = boss.hp;
            int pp = player.hp;

            DamageSystem.checkAndApply(a, boss);
            DamageSystem.checkAndApply(a, player);

            if (boss.hp < pb) {
                int dmg = pb - boss.hp;
                spawnDamageNumber(boss.x + boss.width/2f, boss.y - 8, dmg, Color.WHITE);
                triggerShake(6f, 0.3f);
                combo++;
                comboTimer = COMBO_TIMEOUT;
            }

            if (player.hp < pp) {
                int dmg = pp - player.hp;
                spawnDamageNumber(player.x + player.width/2f, player.y - 8, dmg, Color.WHITE);
                triggerShake(9f, 0.4f);
                combo = 0;
                comboTimer = 0f;
            }

            if (!a.isActive()) it.remove();
        }

        // COMBO TIMER
        if (combo > 0) {
            comboTimer -= 1f/60f;
            if (comboTimer <= 0) {
                combo = 0;
                comboTimer = 0f;
            }
        }

        // STAMINA
        if (!hasStaminaMethod) {
            localStamina = Math.min(STAMINA_MAX, localStamina + STAMINA_REGEN * (1f/60f));
        }

        updateEffects(1f/60f);

        // DEATH CHECK
        if (!player.isAlive()) {
            isDead = true;
            paused = false;
            deathSelection = 0;
        }

        if (!boss.isAlive()) manager.backToMenu();
    }

    // Effect

    private void updateEffects(float dt) {
        // Damage numbers
        for (int i = dmgNumbers.size() - 1; i >= 0; i--) {
            DamageNumber d = dmgNumbers.get(i);
            d.y += d.vy;
            d.life -= dt;
            if (d.life <= 0) dmgNumbers.remove(i);
        }

        // Shake
        if (shakeTime > 0f) {
            shakeTime -= dt;
            float t = shakeTime / shakeDuration;
            shakeOffsetX = (int)((Math.random()*2-1) * shakeIntensity * t);
            shakeOffsetY = (int)((Math.random()*2-1) * shakeIntensity * t);
        } else {
            shakeOffsetX = 0;
            shakeOffsetY = 0;
        }
    }

    private void spawnDamageNumber(double wx, double wy, int dmg, Color c) {
        dmgNumbers.add(new DamageNumber("" + dmg, (float)wx, (float)wy, c));
    }

    private void triggerShake(float intensity, float duration) {
        shakeIntensity = intensity;
        shakeDuration = duration;
        shakeTime = duration;
    }

    // Menu Control

    private void handleMenuInput() {
        if (key.isPressed(KeyEvent.VK_UP)) {
            if (isDead) deathSelection = Math.max(0, deathSelection - 1);
            else pauseSelection = Math.max(0, pauseSelection - 1);
        }
        if (key.isPressed(KeyEvent.VK_DOWN)) {
            if (isDead) deathSelection = Math.min(1, deathSelection + 1);
            else pauseSelection = Math.min(2, pauseSelection + 1);
        }
        if (key.isPressed(KeyEvent.VK_ENTER)) {
            if (isDead) {
                if (deathSelection == 0) manager.startGame();
                else manager.backToMenu();
            } else if (paused) {
                if (pauseSelection == 0) paused = false;
                else if (pauseSelection == 1) manager.startGame();
                else manager.backToMenu();
            }
        }
    }

    // Render

    @Override
    public void render(Graphics2D g) {

        int sw = panel.getWidth();
        int sh = panel.getHeight();

        double baseW = 1280;
        double baseH = 720;

        double scale = Math.min(sw/baseW, sh/baseH);
        double offsetX = (sw - baseW * scale) / 2;
        double offsetY = (sh - baseH * scale) / 2;

        g.setColor(Color.BLACK);
        g.fillRect(0,0,sw,sh);

        AffineTransform old = g.getTransform();
        g.translate(offsetX + shakeOffsetX, offsetY + shakeOffsetY);
        g.scale(scale, scale);

        // BACKGROUND
        if (bgCache == null) {
            try {
                bgCache = javax.imageio.ImageIO.read(
                        getClass().getResourceAsStream("/res/graveyard.png")
                );
            } catch (Exception e) { bgCache = null; }
        }

        if (bgCache != null)
            g.drawImage(bgCache, 0, 0, (int)baseW, (int)baseH, null);
        else {
            g.setColor(new Color(10,10,15));
            g.fillRect(0,0,(int)baseW,(int)baseH);
        }

        g.setColor(new Color(0,0,0,110));
        g.fillRect(0,0,(int)baseW,(int)baseH);

        // WORLD RENDER
        map.render(g);
        player.render(g);
        boss.render(g);

        g.setColor(new Color(255,255,255,150));
        for (Attack a : attacks) {
            if (a.isActive()) g.draw(a.getHitbox());
        }

        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        for (DamageNumber d : dmgNumbers) {
            int alpha = (int)(255*Math.max(0,Math.min(1,d.life)));
            g.setColor(new Color(d.color.getRed(), d.color.getGreen(), d.color.getBlue(), alpha));
            g.drawString(d.text, d.x, d.y);
        }

        g.setTransform(old);

        // HP bars
        drawHPBar(g, 20, 20, player.hp, 200, new Color(90,255,120));
        drawHPBar(g, sw - 260, 20, boss.hp, boss.hp + (maxBossHP() - boss.hp), new Color(255,110,110));

        // Stamina
        float stam = localStamina;
        if (hasStaminaMethod && staminaGetter != null) {
            try {
                Object v = staminaGetter.invoke(player);
                if (v instanceof Number) stam = ((Number)v).floatValue();
            } catch (Exception ignored) {}
        }
        drawStaminaBar(g, 20, 48, stam, STAMINA_MAX);

        // Combo
        if (combo > 0) {
            g.setFont(new Font("SansSerif", Font.BOLD, 24));
            String label = "COMBO x" + combo;
            int tw = g.getFontMetrics().stringWidth(label);
            g.setColor(Color.WHITE);
            g.drawString(label, (sw - tw) / 2, 70);
        }

        if (paused) renderPauseMenu(g, sw, sh);
        if (isDead) renderDeathOverlay(g, sw, sh);

        if (entering) {
            int a = (int)(255 * (1 - enterAlpha));
            g.setColor(new Color(0,0,0,a));
            g.fillRect(0,0,sw,sh);
        }
    }

    private void drawHPBar(Graphics2D g, int x, int y, int hp, int max, Color fill) {
        int w = 240;
        int h = 22;

        g.setColor(new Color(0,0,0,150));
        g.fillRoundRect(x,y,w,h,12,12);

        double pct = Math.max(0, Math.min(1.0, hp * 1.0 / max));
        int fillW = (int)(w * pct);

        g.setColor(fill);
        g.fillRoundRect(x,y,fillW,h,12,12);

        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(x,y,w,h,12,12);
    }

    private void drawStaminaBar(Graphics2D g, int x, int y, float stamina, float max) {
        int w = 180;
        int h = 10;

        g.setColor(new Color(255,255,255,40));
        g.fillRoundRect(x,y,w,h,8,8);

        int fillW = (int)(w * Math.max(0,Math.min(1, stamina/max)));

        g.setColor(new Color(190,220,255));
        g.fillRoundRect(x,y,fillW,h,8,8);

        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(1));
        g.drawRoundRect(x,y,w,h,8,8);
    }

    private int maxBossHP() {
        return 600; // same with boss constructor
    }

    // Pause and Death Menu

    private void renderPauseMenu(Graphics2D g, int w, int h) {

        g.setColor(new Color(0,0,0,180));
        g.fillRect(0,0,w,h);

        int bw = 460, bh = 240;
        int bx = (w - bw) / 2;
        int by = (h - bh) / 2;

        g.setColor(new Color(30,30,40));
        g.fillRoundRect(bx,by,bw,bh,14,14);

        g.setColor(new Color(255,255,255,80));
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(bx,by,bw,bh,14,14);

        g.setFont(new Font("Serif", Font.BOLD, 42));
        g.setColor(Color.WHITE);
        String t = "PAUSED";
        int tw = g.getFontMetrics().stringWidth(t);
        g.drawString(t, bx + (bw - tw)/2, by + 60);

        g.setFont(new Font("SansSerif", Font.PLAIN, 24));
        String[] opts = {"Resume","Restart","Back to Menu"};

        for (int i = 0; i < opts.length; i++) {
            int ox = bx + 46;
            int oy = by + 110 + i*40;

            if (i == pauseSelection) {
                g.setColor(Color.WHITE);
                g.fillRoundRect(ox - 18, oy - 20, 300, 32, 10, 10);
                g.setColor(Color.BLACK);
            } else {
                g.setColor(Color.WHITE);
            }

            g.drawString(opts[i], ox, oy);
        }
    }

    private void renderDeathOverlay(Graphics2D g, int w, int h) {
        g.setColor(new Color(0,0,0,200));
        g.fillRect(0,0,w,h);

        g.setFont(new Font("Serif", Font.BOLD, 44));
        g.setColor(Color.WHITE);

        String msg = "YOU DIED";
        int mw = g.getFontMetrics().stringWidth(msg);
        g.drawString(msg, (w - mw)/2, h/2 - 40);

        g.setFont(new Font("SansSerif", Font.PLAIN, 22));
        String[] opts = {"Restart","Back to Menu"};

        for (int i = 0; i < opts.length; i++) {
            int ox = (w - g.getFontMetrics().stringWidth(opts[i]))/2;
            int oy = h/2 + 10 + i*40;

            if (i == deathSelection) {
                g.setColor(Color.WHITE);
                g.drawString("> " + opts[i], ox - 28, oy);
            } else {
                g.setColor(new Color(200,200,200));
                g.drawString(opts[i], ox, oy);
            }
        }
    }
}
