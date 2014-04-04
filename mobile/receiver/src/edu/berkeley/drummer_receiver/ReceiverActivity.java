package edu.berkeley.drummer_receiver;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class ReceiverActivity extends Activity {
	private boolean mRunning = false;
	private File mAudioDir;
	private BufferedOutputStream mAudioOS;
	private AudioRecordThread mRecordThread;
	private final int mSampleRate = 44100;
	private Button mStartButton;
	private Button mStopButton;
	private IMU mIMU;

	private class AudioRecordThread extends Thread {
		private volatile boolean mRun = true;
		private final AudioRecord mRecorder;
		private final int mBufSize;

		public AudioRecordThread() {
			// TODO buffsize and mRecorder should be created in onCreate
			mBufSize = AudioRecord
					.getMinBufferSize(mSampleRate, AudioFormat.CHANNEL_IN_MONO,
							AudioFormat.ENCODING_PCM_16BIT);
			mRecorder = new AudioRecord(MediaRecorder.AudioSource.CAMCORDER,
					mSampleRate, AudioFormat.CHANNEL_IN_MONO,
					AudioFormat.ENCODING_PCM_16BIT, mBufSize);
		}

		@Override
		public void run() {
			setPriority(Thread.MAX_PRIORITY);
			try {
				mRecorder.startRecording();
				while (mRun == true) {
					final byte[] buffer = new byte[mBufSize];
					// blocking read, which returns when
					// buffer.length bytes are recorded
					mRecorder.read(buffer, 0, buffer.length); // Bytes
					System.out.print(buffer);
					mAudioOS.write(buffer);
				}
				mRecorder.stop();
				mRecorder.release();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}

		public void terminate() {
			mRun = false;
		}
	}

	private final OnClickListener mStartListener = new OnClickListener() {

		@Override
		public void onClick(final View v) {
			start();
		}
	};

	private final OnClickListener mStopListener = new OnClickListener() {

		@Override
		public void onClick(final View v) {
			stop();
		}
	};

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mStartButton = (Button) this.findViewById(R.id.start_imu_button);
		mStopButton = (Button) this.findViewById(R.id.stop_imu_button);

		mStartButton.setOnClickListener(mStartListener);
		mStopButton.setOnClickListener(mStopListener);

		mStartButton.setEnabled(true);
		mStopButton.setEnabled(false);

		// This will get the SD Card directory and create a folder named
		// Drummer in it.
		final File sdCard = Environment.getExternalStorageDirectory();
		mAudioDir = new File(sdCard.getAbsolutePath() + "/Drummer/Audio");
		mAudioDir.mkdirs();

		mIMU = new IMU(this);
	}

	@Override
	public void onPause() {
		stop();
		super.onPause();
	}

	private void start() {
		mStartButton.setEnabled(false);
		mStopButton.setEnabled(true);

		mRunning = true;

		try {
			final String audioFileName = Long.toString(System
					.currentTimeMillis()) + ".txt";
			final File linearAccFile = new File(mAudioDir, audioFileName);
			mAudioOS = new BufferedOutputStream(new FileOutputStream(
					linearAccFile));
		} catch (final FileNotFoundException e) {
			Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
		}

		mRecordThread = new AudioRecordThread();
		mRecordThread.start();

		mIMU.start();
	}

	public void stop() {
		if (mRunning == true) {
			mIMU.stop();

			mRecordThread.terminate();
			try {
				mRecordThread.join();
			} catch (final InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mRecordThread = null;

			try {
				if (mAudioOS != null) {
					mAudioOS.flush();
					mAudioOS.close();
					mAudioOS = null;
				}
			} catch (final IOException e) {
				Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
			}

			mRunning = false;

			mStartButton.setEnabled(true);
			mStopButton.setEnabled(false);
		}
	}
}