package com.example.nanowars.model;

import java.util.Vector;

import android.os.Vibrator;

public class Game {
	public Vector<Cell> cells;
	public Vector<Shot> shots;
	public AI ai;

	public double aspectRatio; // of display

	private Vibrator vibrator;

	private Cell sourceCell, fingerAtCell;
	public double fingerAtX, fingerAtY;

	public static double distance(double x1, double y1, double x2, double y2) {
		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}

	public Game(double aspectRatio, Vibrator vibrator) {
		this.aspectRatio = aspectRatio;
		ai = new AI(this);
		this.vibrator = vibrator;

		sourceCell = null;
	}

	public void initCells() {
		cells = new Vector<Cell>();
		shots = new Vector<Shot>();

		cells.add(new Cell(this, Cell.Type.ENEMY, 20, 11, 0.3,
				0.4 * aspectRatio));
		cells.add(new Cell(this, Cell.Type.ENEMY, 10, 4, 0.8, 0.8 * aspectRatio));

		cells.add(new Cell(this, Cell.Type.HUMAN, 40, 10, 0.65,
				0.1 * aspectRatio));
		cells.add(new Cell(this, Cell.Type.HUMAN, 50, 7, 0.8, 0.4 * aspectRatio));

		cells.add(new Cell(this, Cell.Type.NEUTRAL, 40, 30, 0.3,
				0.8 * aspectRatio));
	}

	public void update(long dt) {
		ai.update(dt);

		for (Cell cell : cells)
			cell.update(dt);

		Vector<Shot> finishedShots = new Vector<Shot>();
		for (Shot shot : shots)
			if (!shot.finished)
				shot.update(dt);
			else
				finishedShots.add(shot);
		if (!finishedShots.isEmpty())
			for (Shot shot : finishedShots)
				shots.remove(shot);
	}

	private Cell getCellAt(double x, double y) {
		Cell touched = null;

		for (Cell cell : cells) {
			double d = distance(x, y, cell.x, cell.y);
			if (d <= cell.radius) {
				touched = cell;
				break;
			}
		}

		return touched;
	}

	public void handleActionDown(double x, double y) {
		Cell touched = getCellAt(x, y);

		if (touched != null && touched.type == Cell.Type.HUMAN) {
			// we're choosing an attacker cell
			sourceCell = touched;
			vibrator.vibrate(40);
		} else
			sourceCell = null;

		fingerAtCell = sourceCell;
		fingerAtX = x;
		fingerAtY = y;
	}

	public void handleActionUp(double x, double y) {
		Cell touched = getCellAt(x, y);

		if (touched != null && sourceCell != null)
			sourceCell.shootAt(touched);

		sourceCell = null;
		fingerAtCell = null;

		fingerAtX = x;
		fingerAtY = y;
	}

	public void handleActionMove(double x, double y) {
		Cell touched = getCellAt(x, y);

		if (touched != null && sourceCell != null && touched != fingerAtCell && touched != sourceCell) {
			// they're hovering over a new cell with their own cell selected
			vibrator.vibrate(40);
		}

		fingerAtCell = touched; // even if it's null!

		// to draw a line
		fingerAtX = x;
		fingerAtY = y;
	}

	public void addShot(Cell from, Cell to, int load) {
		shots.add(new Shot(from, to, load));
	}

	public Cell getSourceCell() {
		return sourceCell;
	}
}
