package com.example.nanowars.model;

public class Cell {
	protected static long INCREASE_LOAD_EVERY = 1500; // [ms]
	
	protected static int RADIUS_K = 10;
	protected static int RADIUS_L = 50;
	protected static double RADIUS_AT_K = 0.08333333333333333333333333333333;
	protected static double RADIUS_AT_L = 0.16666666666666666666666666666667;

	public int capacity, load;

	public double x, y, radius;

	public enum Type {
		HUMAN, ENEMY, NEUTRAL
	}
	
	public boolean selected;
	public Type type;

	protected long nextIncreaseIn;
	
	protected Game game;

	public Cell(Game game, Type type, int capacity, int load, double x, double y) {
		this.game = game;
		this.type = type;
		this.capacity = capacity;
		this.load = load;
		nextIncreaseIn = INCREASE_LOAD_EVERY;
		selected = false;
		
		this.x = x;
		this.y = y;

		double a = (RADIUS_AT_L - RADIUS_AT_K) / (RADIUS_L - RADIUS_K);
		double b = RADIUS_AT_K - RADIUS_K * a;
		radius = a * capacity + b;
	}

	public void handleActionTap() {
		game.chooseCell(this);
	}

	public void update(long dt) {
		if (type == Type.NEUTRAL)
			return;

		nextIncreaseIn -= dt;
		if (nextIncreaseIn <= 0 && load < capacity) {
			load++;
			nextIncreaseIn = INCREASE_LOAD_EVERY;
		}
	}

	public void shootAt (Cell target) {
		if (load > 1) {
			int shotLoad = Math.round(load / 2);
			load -= shotLoad;
			game.addShot(this, target, shotLoad);
		}
	}
	
	public void getShotAtBy(Shot shot) {
		if (shot.type == type) {
			// do³adowanie
			load = Math.min(load + shot.load, capacity);
		}
		else {
			// atak
			load -= shot.load;
			if (load < 0) {
				type = shot.type;
				load = Math.min(-load, capacity);
			}
			nextIncreaseIn = INCREASE_LOAD_EVERY;
		}
	}
}
