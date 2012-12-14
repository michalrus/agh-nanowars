package com.example.nanowars.model;

public class Shot {
	public int load;
	public Cell from, to;
	public double x, y, radius, distanceLeft, moveOverlap;
	public boolean finished = false, started = false;
	public Cell.Type type;

	protected static int RADIUS_K = 3;
	protected static int RADIUS_L = 10;
	protected static double RADIUS_AT_K = 0.04166666666666666666666666666667;
	protected static double RADIUS_AT_L = 0.07291666666666666666666666666667;

	private static double MOVE_VELOCITY = 0.01041666666666666666666666666667 / 100; // fraction of display per [ms]
	private static double MOVE_OVERLAP = 0.6; // fraction of shot radius

	public Shot(Cell from, Cell to, int load) {
		this.from = from;
		this.to = to;
		this.load = load;
		x = from.x;
		y = from.y;
		type = from.type;
		
		double a = (RADIUS_AT_L - RADIUS_AT_K) / (RADIUS_L - RADIUS_K);
		double b = RADIUS_AT_K - RADIUS_K * a;
		radius = a * load + b;
		moveOverlap = radius * (1 - MOVE_OVERLAP);
	}
	
	private void computeDistanceLeft () {
		distanceLeft = Game.distance(x, y, to.x, to.y);
	}
	
	private void moveCloserBy (double distance) {
		x += distance * (to.x - x) / distanceLeft;
		y += distance * (to.y - y) / distanceLeft;
	}

	public void update(long dt) {
		if (finished)
			return;

		computeDistanceLeft();
		double step;
		
		if (!started) {
			step = from.radius + moveOverlap; // do not start exactly at source cell's center
			started = true;
		} else
			step = MOVE_VELOCITY * dt;
		
		if (step > distanceLeft - (to.radius + moveOverlap)) {
			finished = true;
			to.getShotAtBy(this);
		}
		else
			moveCloserBy(step);
	}
}
