package com.example.nanowars;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class OrientationDetector implements SensorEventListener {

	private SensorManager manager;
	private Sensor accelerometer;

	private Float rotation;

	public OrientationDetector(Context context) {
		manager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
		
		accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		rotation = 0.0f;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER:
			synchronized (rotation) {
				rotation = (float)Math.toDegrees(Math.atan2((double)event.values[0], (double)event.values[1]));
			}
			break;
		}
	}
	
	public float getRotation() {
		synchronized (rotation) {
			return rotation;
		}
	}

	public void pause() {
		manager.unregisterListener(this);
	}

	public void resume() {
		manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
	}
	
}
