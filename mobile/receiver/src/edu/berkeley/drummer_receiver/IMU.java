package edu.berkeley.drummer_receiver;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

public class IMU {
	private final Activity mActivity;
	private final File mLinearAccDir;
	private final File mRotationDir;
	private BufferedOutputStream mLinearAccOS;
	private BufferedOutputStream mRotationOS;
	private final SensorManager mSensorManager;
	private final Sensor mLinearAcc;
	private final Sensor mRotation;

	private final SensorEventListener mLinearAccListener = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSensorChanged(final SensorEvent event) {
			try {
				String data = System.currentTimeMillis() + " ";
				data += System.nanoTime() + " ";
				data += event.timestamp + " ";
				data += event.values[0] + " ";
				data += event.values[1] + " ";
				data += event.values[2] + "\n";
				mLinearAccOS.write(data.getBytes());
			} catch (final IOException e) {
				Toast.makeText(mActivity, e.toString(), Toast.LENGTH_LONG)
						.show();
			}
		}
	};

	private final SensorEventListener mRotationListener = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSensorChanged(final SensorEvent event) {
			try {
				String data = System.currentTimeMillis() + " ";
				data += System.nanoTime() + " ";
				data += event.timestamp + " ";
				data += event.values[0] + " ";
				data += event.values[1] + " ";
				data += event.values[2] + "\n";
				mRotationOS.write(data.getBytes());
			} catch (final IOException e) {
				Toast.makeText(mActivity, e.toString(), Toast.LENGTH_LONG)
						.show();
			}
		}
	};

	@SuppressLint("InlinedApi")
	public IMU(final Activity activity) {
		mActivity = activity;

		final File sdCard = Environment.getExternalStorageDirectory();
		mLinearAccDir = new File(sdCard.getAbsolutePath()
				+ "/Drummer/LinearAcc");
		mLinearAccDir.mkdirs();
		mRotationDir = new File(sdCard.getAbsolutePath() + "/Drummer/Rotation");
		mRotationDir.mkdirs();

		mSensorManager = (SensorManager) mActivity
				.getSystemService(Context.SENSOR_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			mLinearAcc = mSensorManager
					.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		} else {
			mLinearAcc = null;
			Toast.makeText(mActivity, "No Linear Accelerometer",
					Toast.LENGTH_LONG).show();
		}
		mRotation = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
	}

	public void start() {
		try {
			final String linearAccFileName = Long.toString(System
					.currentTimeMillis()) + ".txt";
			final File linearAccFile = new File(mLinearAccDir,
					linearAccFileName);
			mLinearAccOS = new BufferedOutputStream(new FileOutputStream(
					linearAccFile));

			final String rotationFileName = Long.toString(System
					.currentTimeMillis()) + ".txt";
			final File rotationFile = new File(mRotationDir, rotationFileName);
			mRotationOS = new BufferedOutputStream(new FileOutputStream(
					rotationFile));
		} catch (final FileNotFoundException e) {
			Toast.makeText(mActivity, e.toString(), Toast.LENGTH_LONG).show();
		}

		mSensorManager.registerListener(mLinearAccListener, mLinearAcc,
				SensorManager.SENSOR_DELAY_FASTEST);
		mSensorManager.registerListener(mRotationListener, mRotation,
				SensorManager.SENSOR_DELAY_FASTEST);
	}

	public void stop() {
		mSensorManager.unregisterListener(mLinearAccListener);
		mSensorManager.unregisterListener(mRotationListener);

		try {
			if (mLinearAccOS != null) {
				mLinearAccOS.flush();
				mLinearAccOS.close();
				mLinearAccOS = null;
			}
			if (mRotationOS != null) {
				mRotationOS.flush();
				mRotationOS.close();
				mRotationOS = null;
			}
		} catch (final IOException e) {
			Toast.makeText(mActivity, e.toString(), Toast.LENGTH_LONG).show();
		}
	}
}
