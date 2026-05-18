package game.entity;

import java.awt.*;
import java.util.Random;

public class Boss {
	public double x, y;
	public int width = 80, height = 96;
	public int hp = 400;
	public Rectangle hitbox = new Rectangle();
	public Rectangle attackHitbox = new Rectangle();
	public int attackDamage = 18;
	private double velX = 0, velY = 0;
	private final double gravity = 0.6;
	private final double walkSpeed = 1.2;
	private final double dashSpeed = 5.2;
	private final double jumpStrength = -10;
	private long lastAttackTime = 0;
	private final long baseAttackCooldown = 900;
	private long attackCooldown = baseAttackCooldown;
	private boolean facingRight = true;
	private final int maxHp = 400;
	private boolean phase2 = false;
	private State state = State.CHASE;
	private long stateEnteredAt = System.currentTimeMillis();
	private final long attackWindup = 250;
	private final Random rnd = new Random();

	private enum State { IDLE, CHASE, ATTACK_PREP, ATTACKING, RECOVER }

	public Boss(double x, double y) {
		this.x = x;
		this.y = y;
		hitbox.setBounds((int)x, (int)y, width, height);
		attackHitbox.setBounds((int)x, (int)y, width, height);
		state = State.CHASE;
		stateEnteredAt = System.currentTimeMillis();
	}

	private boolean hasHitPlayer = false;

	public void update(Player player) {
		velY += gravity;
		if (velY > 12) velY = 12;

		double playerCenterX = player.x + player.width/2.0;
		double dist = Math.abs((x + width/2.0) - playerCenterX);

		if (!phase2 && hp < maxHp * 0.45) {
			phase2 = true;
			attackCooldown = (long)(baseAttackCooldown * 0.7);
		}

		long now = System.currentTimeMillis();

		switch (state) {
			case IDLE:
				if (now - stateEnteredAt > 800 + rnd.nextInt(1200)) {
					state = State.CHASE;
					stateEnteredAt = now;
				}
				break;
			case CHASE:
				if (dist > 160) moveToward(playerCenterX);
				else if (dist > 60) moveToward(playerCenterX);
				else stopMoving();

				if (canAttack() && dist < 180 && rnd.nextDouble() < (phase2 ? 0.06 : 0.02)) {
					state = State.ATTACK_PREP;
					stateEnteredAt = now;
				}
				break;
			case ATTACK_PREP:
				if (now - stateEnteredAt >= attackWindup) {
					performMelee(playerCenterX > x);
					state = State.ATTACKING;
					stateEnteredAt = now;
					hasHitPlayer = false;
				}
				break;
			case ATTACKING:
				// Cek tabrakan serangan boss ke player, hanya sekali per serangan
				if (!hasHitPlayer && attackHitbox.intersects(player.hitbox) && player.isAlive()) {
					player.takeDamage(attackDamage);
					hasHitPlayer = true;
				}
				if (now - stateEnteredAt > 350 + (phase2 ? -120 : 0)) {
					state = State.RECOVER;
					stateEnteredAt = now;
				}
				break;
			case RECOVER:
				if (now - stateEnteredAt > 400) {
					state = State.CHASE;
					stateEnteredAt = now;
				}
				break;
		}

		x += velX;
		y += velY;
		facingRight = velX >= 0;
		hitbox.setBounds((int)x, (int)y, width, height);
	}

	public void moveToward(double px) {
		double center = x + width / 2.0;
		if (Math.abs(px - center) < 4) { velX = 0; return; }
		velX = (px > center) ? walkSpeed : -walkSpeed;
		facingRight = velX >= 0;
	}

	public void stopMoving() { velX = 0; }

	public boolean canAttack() {
		return (System.currentTimeMillis() - lastAttackTime) >= attackCooldown;
	}

	public void performMelee(boolean directionRight) {
		double r = rnd.nextDouble();
		if (phase2) r += 0.2;

		if (r < 0.15) {
			doDashAttack(directionRight);
		} else if (r < 0.6) {
			doMeleeCombo(directionRight);
		} else {
			doHopSlam(directionRight);
		}

		lastAttackTime = System.currentTimeMillis();
	}

	private void doMeleeCombo(boolean dirRight) {
		attackDamage = phase2 ? 30 : 18;
		int w = 80, h = 40;
		int ax = (int)(x + (dirRight ? width : -w));
		int ay = (int)(y + height/2 - h/2);
		attackHitbox.setBounds(ax, ay, w, h);
		velX = dirRight ? 1.8 : -1.8;
	}

	private void doDashAttack(boolean dirRight) {
		attackDamage = phase2 ? 40 : 26;
		int w = 120, h = 48;
		int ax = (int)(x + (dirRight ? width : -w));
		int ay = (int)(y + height/2 - h/2);
		attackHitbox.setBounds(ax, ay, w, h);
		velX = dirRight ? dashSpeed : -dashSpeed;
	}

	private void doHopSlam(boolean dirRight) {
		attackDamage = phase2 ? 28 : 20;
		velY = jumpStrength * 0.9;
		int w = 100, h = 50;
		int ax = (int)(x - (w-width)/2.0);
		int ay = (int)(y + height);
		attackHitbox.setBounds(ax, ay, w, h);
		velX = dirRight ? 0.6 : -0.6;
	}

	public void takeDamage(int dmg) {
		hp -= dmg;
		if (hp < 0) hp = 0;
		velX = (rnd.nextBoolean() ? -1.5 : 1.5);
		state = State.RECOVER;
		stateEnteredAt = System.currentTimeMillis();
	}

	public boolean isAlive() { return hp > 0; }

	public void render(Graphics2D g) {
		g.setColor(new Color(200, 50, 80));
		g.fillRect((int)x, (int)y, width, height);
		// Optional: draw attack hitbox for debug
		g.setColor(new Color(255,255,255,80));
		g.drawRect(attackHitbox.x, attackHitbox.y, attackHitbox.width, attackHitbox.height);
	}
}