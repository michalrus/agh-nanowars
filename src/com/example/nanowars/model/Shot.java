package com.example.nanowars.model;

public class Shot {
	public int load;
	public Cell from, to;
	public double x, y, radius;
	public boolean finished = false;
	public Cell.Type type;

	protected static int RADIUS_K = 3;
	protected static int RADIUS_L = 10;
	protected static double RADIUS_AT_K = 0.04166666666666666666666666666667;
	protected static double RADIUS_AT_L = 0.07291666666666666666666666666667;

	private static double MOVE_VELOCITY = 0.01041666666666666666666666666667 / 50; // fraction of display per [ms]
	private static double MOVE_OVERLAP = 0.0;

	public Shot(Cell from, Cell to, int load) {
		this.from = from;
		this.to = to;
		this.load = load;
		this.x = from.x;
		this.y = from.y;
		type = from.type;
		
		double a = (RADIUS_AT_L - RADIUS_AT_K) / (RADIUS_L - RADIUS_K);
		double b = RADIUS_AT_K - RADIUS_K * a;
		radius = a * load + b;
		
		moveBy(from.radius + radius - MOVE_OVERLAP);
	}
	
	private void moveBy (double dist) {
		if (finished)
			return;
		
		double d = Game.distance(x, y, to.x, to.y);
		
		if (d - to.radius - radius < 0 || d - to.radius - radius + MOVE_OVERLAP < dist) {
			finished = true;
			return;
		}

		x += dist * (to.x - x) / d;
		y += dist * (to.y - y) / d;
	}

	public void update(long dt) {
		if (finished)
			return;

		moveBy(MOVE_VELOCITY * dt);
		
		// TODO: kiedy komórki s¹ zbyt blisko jest klops...
		
		if (finished)
			to.getShotAtBy(this);
	}
}
