package game.entity;

import java.awt.*;

public class Player {
	public double x, y;
	public int width = 48, height = 48;
	public int hp = 200;
	public Rectangle hitbox = new Rectangle();

	public Player(double x, double y) {
		this.x = x;
		this.y = y;
		hitbox.setBounds((int)x, (int)y, width, height);
	}

	public void takeDamage(int dmg) {
		hp -= dmg;
		if (hp < 0) hp = 0;
	}

	public boolean isAlive() { return hp > 0; }

	// ...tambahkan update dan render sesuai kebutuhan game kamu...
}