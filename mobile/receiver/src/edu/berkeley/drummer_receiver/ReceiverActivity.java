package edu.berkeley.drummer_receiver;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ReceiverActivity extends Activity implements View.OnClickListener {
	private AudioRecordThread mRecordThread;
	private final int mSampleRate = 44100;
	private EditText mDurationText;
	private Button mRecvButton;
	private Handler mHandler;
	private IMU mIMU;

	private class AudioRecordThread extends Thread {
		private volatile boolean mRun = true;
		private final AudioRecord mRecorder;
		private BufferedOutputStream mOutStream;
		private final int mBufSize;

		public AudioRecordThread() {
			final File sdCard = Environment.getExternalStorageDirectory();
			final File directory = new File(sdCard.getAbsolutePath()
					+ "/Drummer");
			final String fileName = Long.toString(System.currentTimeMillis())
					+ ".dat";
			final File file = new File(directory, fileName);

			try {
				mOutStream = new BufferedOutputStream(
						new FileOutputStream(file));
			} catch (final FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

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
			// setPriority(Thread.MAX_PRIORITY);
			try {
				mRecorder.startRecording();
				while (mRun == true) {
					final byte[] buffer = new byte[mBufSize];
					// blocking read, which returns when
					// buffer.length bytes are recorded
					mRecorder.read(buffer, 0, buffer.length); // Bytes
					System.out.print(buffer);
					mOutStream.write(buffer);
				}
				mOutStream.flush();
				mOutStream.close();
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

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mRecvButton = (Button) findViewById(R.id.recv_button);
		mDurationText = (EditText) findViewById(R.id.recv_duration_text);
		mHandler = new Handler();
		mRecvButton.setOnClickListener(this);

		// This will get the SD Card directory and create a folder named
		// Drummer in it.
		final File sdCard = Environment.getExternalStorageDirectory();
		final File directory = new File(sdCard.getAbsolutePath() + "/Drummer");
		directory.mkdirs();

		mIMU = new IMU(this);
	}

	@Override
	public void onPause() {
		mIMU.stop();
		if (mRecordThread != null) {
			mRecordThread.terminate();
			try {
				mRecordThread.join();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
		super.onPause();
	}

	private final Runnable mPauseTask = new Runnable() {
		@Override
		public void run() {
			mRecordThread.terminate();
			try {
				mRecordThread.join();
			} catch (final InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mRecordThread = null;
			mHandler.removeCallbacks(mPauseTask);
			mRecvButton.setEnabled(true);
		}
	};

	@Override
	public void onClick(final View v) {
		mRecvButton.setEnabled(false);
		mRecordThread = new AudioRecordThread();

		final long recvDuration = Long.parseLong(mDurationText.getText()
				.toString());

		mRecordThread.start();
		mHandler.postDelayed(mPauseTask, recvDuration);
	}
}