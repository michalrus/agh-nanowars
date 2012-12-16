package com.example.nanowars;

import java.util.Date;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class MainThread extends Thread {
	private SurfaceHolder surfaceHolder;
	private MainGamePanel gamePanel;
	private boolean alive;

	public MainThread(SurfaceHolder surfaceHolder, MainGamePanel gamePanel) {
		super();
		this.surfaceHolder = surfaceHolder;
		this.gamePanel = gamePanel;
		alive = true;
	}

	public void die() {
		alive = false;
	}

	public void run() {
		long now = 0, then = 0;
		Canvas canvas;

		while (alive) {
			canvas = null;
			try {
				canvas = this.surfaceHolder.lockCanvas();
				synchronized (surfaceHolder) {
					now = new Date().getTime();
					gamePanel.processSensors();
					gamePanel.processUserInput();
					gamePanel.game.update(now - then);
					gamePanel.render(canvas);
					then = now;
				}
			} finally {
				if (canvas != null) {
					surfaceHolder.unlockCanvasAndPost(canvas);
				}
			}
		}
	}
}
