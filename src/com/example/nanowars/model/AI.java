package com.example.nanowars.model;

public class AI {
	private Game game;

	private static long ACTION_TIME_SPAN_MIN = 1000; // [ms]
	private static long ACTION_TIME_SPAN_MAX = 3000; // [ms]

	private long nextActionIn;

	public AI(Game game) {
		this.game = game;
		nextActionIn = ACTION_TIME_SPAN_MAX;
	}

	public void update(long dt) {
		nextActionIn -= dt;
		
		if (nextActionIn <= 0) {
			Cell from = null;
			Cell to = null;
			
			for (Cell c : game.cells) {
				if (c.type != Cell.Type.ENEMY && (to == null || to.load > c.load))
					to = c;
				else if (c.type == Cell.Type.ENEMY && (from == null || from.load < c.load))
					from = c;
			}

			if (from != null && to != null)
				from.shootAt(to);
			
			nextActionIn = Math.round(Math.random()
					* (ACTION_TIME_SPAN_MAX - ACTION_TIME_SPAN_MIN)
					+ ACTION_TIME_SPAN_MIN);
		}
	}
}
