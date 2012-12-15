package com.example.nanowars.model;

import java.util.Vector;

import android.os.Vibrator;

public class Game {
	public Vector<Cell> cells;
	public Vector<Shot> shots;
	public AI ai;

	public double aspectRatio; // of display

	private Vibrator vibrator;

	private Cell sourceCell, destinationCell, fingerAtCell, tappedDownCell;
	public double lineEndX, lineEndY;

	private static double BABY_SHOT_DISTANCE = 0.04166666666666666666666666666667;

	public static double distance(double x1, double y1, double x2, double y2) {
		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}

	public Game(double aspectRatio, Vibrator vibrator) {
		this.aspectRatio = aspectRatio;
		ai = new AI(this);
		this.vibrator = vibrator;

		sourceCell = null;
		destinationCell = null;
		fingerAtCell = null;
		tappedDownCell = null;
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
		removeInvalidConnections();
		
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
	
	/**
	 * Removes connections from cells that changed {@link Cell#Type} after choosing them as {@link #sourceCell}.
	 */
	private void removeInvalidConnections() {
		if (sourceCell != null && sourceCell.type != Cell.Type.HUMAN) {
			sourceCell = null;
			destinationCell = null;
			tappedDownCell = null;
		}
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

		if (touched != null
				&& (touched == destinationCell || touched.type == Cell.Type.HUMAN)) {
			vibrator.vibrate(40);
		}

		if (sourceCell != null && destinationCell != null
				&& touched != destinationCell) {
			// cancel a connection
			sourceCell = null;
			destinationCell = null;
			tappedDownCell = null;
		}

		tappedDownCell = touched; // even if it's null
	}

	public void handleActionMove(double x, double y) {
		Cell touched = getCellAt(x, y);

		if (tappedDownCell != null && touched != tappedDownCell
				&& tappedDownCell.type == Cell.Type.HUMAN) {
			sourceCell = tappedDownCell;
			destinationCell = null;
			tappedDownCell = null;
		}

		if (sourceCell != null && destinationCell == null) {
			if (touched != null) {
				if (touched != fingerAtCell && touched != sourceCell)
					vibrator.vibrate(40);

				lineEndX = touched.x;
				lineEndY = touched.y;
			} else {
				lineEndX = x;
				lineEndY = y;
			}
		}

		fingerAtCell = touched; // even if it's null!
	}

	public void handleActionUp(double x, double y) {
		Cell touched = getCellAt(x, y);

		if (touched == null) {
			// cancel a connection
			sourceCell = null;
			destinationCell = null;
			tappedDownCell = null;
		} else if (sourceCell != null) {
			if (destinationCell == null)
				// are we making a connection?
				destinationCell = touched;
			if (destinationCell == touched)
				sourceCell.shootAt(destinationCell);
		}

		fingerAtCell = null; // mr. obvious

		lineEndX = x;
		lineEndY = y;
	}

	public void addShot(Cell from, Cell to, int load) {
		if (from.lastShot == null
				|| from.lastShot.finished
				|| from.lastShot.to != to
				|| distance(from.x, from.y, from.lastShot.x, from.lastShot.y) > (from.radius
						+ from.lastShot.radius + BABY_SHOT_DISTANCE)) {
			Shot shot = new Shot(from, to, load);
			shots.add(shot);
			from.lastShot = shot;
		} else
			from.lastShot.addLoad(load);
	}

	public Cell getSourceCell() {
		return sourceCell;
	}

	public Cell getDestinationCell() {
		return destinationCell;
	}
}
