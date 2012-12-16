package com.example.nanowars;

import java.util.HashMap;
import java.util.LinkedList;
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

	private OrientationDetector orientationDetector;
	private MainThread thread;
	private Vibrator vibrator;

	// don't touch!
	// freaking ConcurrentLinkeyQueue implementation error
	// last ACTION_MOVE is read as ACTION_UP if we use one queue for all events
	private ConcurrentLinkedQueue<MotionEvent> downEvents, upEvents, moveEvents;

	public Game game;

	private HashMap<Cell.Type, Bitmap> bitmapCell, bitmapShot;
	private Bitmap bitmapBackground;

	private Paint linePaint;

	private static double TEXT_SIZE = 0.04166666666666666666666666666667;

	private static int DEVICE_ROTATION_NUM_EL_MEAN = 40;
	private LinkedList<Float> deviceRotations;
	private int deviceRotationsSize;
	private float deviceRotation;

	public MainGamePanel(Context context) {
		super(context);

		vibrator = (Vibrator) context
				.getSystemService(Context.VIBRATOR_SERVICE);
		
		orientationDetector = new OrientationDetector(context);

		downEvents = new ConcurrentLinkedQueue<MotionEvent>();
		upEvents = new ConcurrentLinkedQueue<MotionEvent>();
		moveEvents = new ConcurrentLinkedQueue<MotionEvent>();
		
		deviceRotations = new LinkedList<Float>();
		deviceRotationsSize = 0;
		deviceRotation = 0.0f;

		getHolder().addCallback(this);
		setFocusable(true);

		thread = null;
		game = null;
	}

	public void pause() {
		orientationDetector.pause();
		thread.die();
		waitForMainThreadDeath();
	}

	public void resume() {
		if (thread != null || game == null)
			return;
		orientationDetector.resume();
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

		bitmapCell.put(Cell.Type.HUMAN,
				BitmapFactory.decodeResource(getResources(), R.drawable.human));
		bitmapCell.put(Cell.Type.ENEMY,
				BitmapFactory.decodeResource(getResources(), R.drawable.enemy));
		bitmapCell.put(Cell.Type.NEUTRAL, BitmapFactory.decodeResource(
				getResources(), R.drawable.neutral));

		bitmapShot.put(Cell.Type.HUMAN, BitmapFactory.decodeResource(
				getResources(), R.drawable.human_shot));
		bitmapShot.put(Cell.Type.ENEMY, BitmapFactory.decodeResource(
				getResources(), R.drawable.enemy_shot));

		linePaint = new Paint();
		linePaint.setColor(Color.WHITE);

		game = new Game(1.0 * getHeight() / getWidth(), vibrator);
		game.initCells();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		waitForMainThreadDeath();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downEvents.add(event);
			break;
		case MotionEvent.ACTION_MOVE:
			moveEvents.add(event);
			break;
		case MotionEvent.ACTION_UP:
			upEvents.add(event);
			break;
		}

		return true;
	}

	private double getEventX(MotionEvent event) {
		return 1.0 * event.getX() / getWidth();
	}

	private double getEventY(MotionEvent event) {
		return 1.0 * event.getY() / getWidth(); // YES! By WIDTH! -,-
	}

	public void processUserInput() {
		for (;;) {
			MotionEvent event = downEvents.poll();
			if (event == null)
				break;
			game.handleActionDown(getEventX(event), getEventY(event));
		}

		for (;;) {
			MotionEvent event = moveEvents.poll();
			if (event == null)
				break;
			game.handleActionMove(getEventX(event), getEventY(event));
		}

		for (;;) {
			MotionEvent event = upEvents.poll();
			if (event == null)
				break;
			game.handleActionUp(getEventX(event), getEventY(event));
		}
	}

	protected void render(Canvas canvas) {
		canvas.drawColor(Color.BLACK);

		drawBackground(canvas);

		drawLine(canvas);

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

		int rW = t(cell.radius * 2);
		int rH = rW;
		int rX = t(cell.x - cell.radius);
		int rY = t(cell.y - cell.radius);

		Rect rs = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		Rect rd = new Rect(rX, rY, rX + rW, rY + rH);

		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setTextSize(t(TEXT_SIZE));
		paint.setTextAlign(Paint.Align.CENTER);

		canvas.save();
		canvas.rotate(deviceRotation, t(cell.x), t(cell.y));
		canvas.drawBitmap(bitmap, rs, rd, null);
		canvas.drawText(cell.load + "/" + cell.capacity, t(cell.x), t(cell.y
				+ 0.3 * TEXT_SIZE), paint);
		canvas.restore();
	}

	private void drawShot(Canvas canvas, Shot shot) {
		Bitmap bitmap = bitmapShot.get(shot.type);

		int rW = t(shot.radius * 2);
		int rH = rW;
		int rX = t(shot.x - shot.radius);
		int rY = t(shot.y - shot.radius);

		Rect rs = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		Rect rd = new Rect(rX, rY, rX + rW, rY + rH);

		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setTextSize(t(TEXT_SIZE));
		paint.setTextAlign(Paint.Align.CENTER);

		canvas.save();
		canvas.rotate(deviceRotation, t(shot.x), t(shot.y));
		canvas.drawBitmap(bitmap, rs, rd, null);
		canvas.drawText("" + shot.load, t(shot.x), t(shot.y + 0.3 * TEXT_SIZE),
				paint);
		canvas.restore();
	}

	private void drawLine(Canvas canvas) {
		Cell source = game.getSourceCell();
		Cell destination = game.getDestinationCell();

		if (source != null) {
			// if user selected a source cell
			double toX, toY;

			if (destination != null) {
				// if a connection has been made
				toX = destination.x;
				toY = destination.y;
			} else {
				toX = game.lineEndX;
				toY = game.lineEndY;
			}

			canvas.drawLine(t(source.x), t(source.y), t(toX), t(toY), linePaint);
		}
	}

	public void processSensors() {
		float angle = orientationDetector.getRotation();
		
		deviceRotationsSize++;
		deviceRotations.add(angle);
		
		while (deviceRotationsSize > DEVICE_ROTATION_NUM_EL_MEAN) {
			deviceRotations.removeFirst();
			deviceRotationsSize--;
		}
		
		float sum = 0.0f;
		for (Float el : deviceRotations) {
			sum += el;
		}
		
		deviceRotation = sum / deviceRotationsSize;
	}
}
