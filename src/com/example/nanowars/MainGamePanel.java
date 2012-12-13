package com.example.nanowars;

import java.util.HashMap;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.example.nanowars.model.Cell;
import com.example.nanowars.model.Game;
import com.example.nanowars.model.Shot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainGamePanel extends SurfaceView implements
		SurfaceHolder.Callback {

	private MainThread thread;
	Vibrator vibrator;
	Context context;

	private ConcurrentLinkedQueue<MotionEvent> motionEvents;

	public Game game;

	private Map<Cell.Type, Bitmap> bitmapCell, bitmapShot;
	private Bitmap bitmapBackground, bitmapSelected;
	
	private static double TEXT_SIZE = 0.04166666666666666666666666666667;

	public MainGamePanel(Context context) {
		super(context);
		this.context = context;

		vibrator = (Vibrator) context
				.getSystemService(Context.VIBRATOR_SERVICE);
		motionEvents = new ConcurrentLinkedQueue<MotionEvent>();

		getHolder().addCallback(this);
		setFocusable(true);

		thread = null;
		game = null;
	}

	public void pause() {
		thread.die();
		waitForMainThreadDeath();
	}

	public void resume() {
		if (thread != null || game == null)
			return;
		thread = new MainThread(getHolder(), this);
		thread.start();
	}

	private void waitForMainThreadDeath() {
		if (thread == null)
			return;

		boolean retry = true;
		while (retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {
			}
		}

		thread = null;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	public void surfaceCreated(SurfaceHolder holder) {
		if (game == null)
			init();

		resume();
	}

	private void init() {
		bitmapCell = new HashMap<Cell.Type, Bitmap>();
		bitmapShot = new HashMap<Cell.Type, Bitmap>();

		bitmapBackground = BitmapFactory.decodeResource(getResources(),
				R.drawable.background);

		bitmapSelected = BitmapFactory.decodeResource(getResources(),
				R.drawable.human_selected);

		bitmapCell.put(Cell.Type.HUMAN,
				BitmapFactory.decodeResource(getResources(), R.drawable.human));
		bitmapCell.put(Cell.Type.ENEMY,
				BitmapFactory.decodeResource(getResources(), R.drawable.enemy));
		bitmapCell.put(Cell.Type.NEUTRAL, BitmapFactory.decodeResource(
				getResources(), R.drawable.neutral));

		bitmapShot.put(Cell.Type.HUMAN,
				BitmapFactory.decodeResource(getResources(), R.drawable.human_shot));
		bitmapShot.put(Cell.Type.ENEMY,
				BitmapFactory.decodeResource(getResources(), R.drawable.enemy_shot));

		game = new Game(1.0 * getHeight() / getWidth());
		game.initCells();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		waitForMainThreadDeath();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		motionEvents.add(event);
		return super.onTouchEvent(event);
	}

	public void processUserInput() {
		for (;;) {
			MotionEvent event = motionEvents.poll();
			if (event == null)
				break;

			if (event.getAction() != MotionEvent.ACTION_DOWN)
				return;

			double eX = 1.0 * event.getX() / getWidth();
			double eY = 1.0 * event.getY() / getWidth(); // YES! By WIDTH! -,-

			for (Cell cell : game.cells) {
				double d = Game.distance(eX, eY, cell.x, cell.y);
				if (d <= cell.radius) {
					cell.handleActionTap();
					vibrator.vibrate(40);
					break;
				}
			}
		}
	}

	protected void render(Canvas canvas) {
		canvas.drawColor(Color.BLACK);

		drawBackground(canvas);

		for (Cell cell : game.cells)
			drawCell(canvas, cell);
		for (Shot shot : game.shots)
			drawShot(canvas, shot);
	}

	private void drawBackground(Canvas canvas) {
		Bitmap bitmap = bitmapBackground;

		float scaleW = 1.0f * getWidth() / bitmap.getWidth();
		float scaleH = 1.0f * getHeight() / bitmap.getHeight();

		scaleW = Math.max(scaleW, scaleH);
		scaleH = scaleW;

		int rW = Math.round(bitmap.getWidth() * scaleW);
		int rH = Math.round(bitmap.getHeight() * scaleH);
		int rX = (getWidth() - rW) / 2;
		int rY = (getHeight() - rH) / 2;

		Rect rs = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		Rect rd = new Rect(rX, rY, rX + rW, rY + rH);

		canvas.drawBitmap(bitmap, rs, rd, null);
	}

	/**
	 * Translates fractional dimensions to real pixels.
	 * 
	 * @param in
	 *            Fraction of this SurfaceView width.
	 * @return Real pixel of that fraction.
	 */
	private int t(double in) {
		return (int) Math.round(in * getWidth());
	}

	private void drawCell(Canvas canvas, Cell cell) {
		Bitmap bitmap = bitmapCell.get(cell.type);

		if (cell.type == Cell.Type.HUMAN && cell.selected)
			bitmap = bitmapSelected;

		int rW = t(cell.radius * 2);
		int rH = rW;
		int rX = t(cell.x - cell.radius);
		int rY = t(cell.y - cell.radius);

		Rect rs = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		Rect rd = new Rect(rX, rY, rX + rW, rY + rH);

		canvas.drawBitmap(bitmap, rs, rd, null);

		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setTextSize(t(TEXT_SIZE));
		paint.setTextAlign(Paint.Align.CENTER);

		canvas.drawText(cell.load + "/" + cell.capacity, t(cell.x),
				t(cell.y + 0.3 * TEXT_SIZE), paint);
	}

	private void drawShot(Canvas canvas, Shot shot) {
		Bitmap bitmap = bitmapShot.get(shot.type);

		int rW = t(shot.radius * 2);
		int rH = rW;
		int rX = t(shot.x - shot.radius);
		int rY = t(shot.y - shot.radius);

		Rect rs = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		Rect rd = new Rect(rX, rY, rX + rW, rY + rH);

		canvas.drawBitmap(bitmap, rs, rd, null);

		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setTextSize(t(TEXT_SIZE));
		paint.setTextAlign(Paint.Align.CENTER);

		canvas.drawText("" + shot.load, t(shot.x), t(shot.y + 0.3 * TEXT_SIZE), paint);
	}
}
