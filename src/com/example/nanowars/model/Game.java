package com.example.nanowars.model;

import java.util.Vector;

public class Game {
	public Vector<Cell> cells;
	public Vector<Shot> shots;
	public AI ai;

	public double aspectRatio; // of display

	private Cell selectedCell;

	public static double distance(double x1, double y1, double x2, double y2) {
		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}

	public Game(double aspectRatio) {
		this.aspectRatio = aspectRatio;
		selectedCell = null;
		ai = new AI(this);
	}

	public void initCells() {
		cells = new Vector<Cell>();
		shots = new Vector<Shot>();

		cells.add(new Cell(this, Cell.Type.ENEMY, 20, 11, 0.4, 0.4 * aspectRatio));
		cells.add(new Cell(this, Cell.Type.ENEMY, 10, 4, 0.8, 0.8 * aspectRatio));

		cells.add(new Cell(this, Cell.Type.HUMAN, 40, 10, 0.65, 0.1 * aspectRatio));
		cells.add(new Cell(this, Cell.Type.HUMAN, 50, 7, 0.8, 0.4 * aspectRatio));

		cells.add(new Cell(this, Cell.Type.NEUTRAL, 40, 30, 0.3, 0.8 * aspectRatio));
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

	public void addShot(Cell from, Cell to, int load) {
		shots.add(new Shot(from, to, load));
	}

	public void chooseCell(Cell cell) {
		if (cell.type == Cell.Type.HUMAN) {
			if (selectedCell == null) {
				selectedCell = cell;
				cell.selected = true;
			} else if (selectedCell == cell) {
				selectedCell.selected = false;
				selectedCell = null;
			} else {
				selectedCell.shootAt(cell);
				selectedCell.selected = false;
				selectedCell = null;
			}
		} else if (selectedCell != null) {
			selectedCell.shootAt(cell);
			selectedCell.selected = false;
			selectedCell = null;
		}
	}
}
