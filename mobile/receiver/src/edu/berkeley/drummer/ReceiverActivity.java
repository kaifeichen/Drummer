package edu.berkeley.drummer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class ReceiverActivity extends Activity
		implements
			View.OnClickListener,
			OnSeekBarChangeListener {
	private AudioRecordThread mRecordThread;
	private final int mSampleRate = 44100;
	private SeekBar mFreqBar;
	private TextView mTextView;
	private Button mSendButton;
	private EditText mDurationText;
	private double sliderval;
	private final double maxDuration = 100; // in millisecond
	private final int maxAmp = 32000;
	private Handler mHandler;


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
		mFreqBar = (SeekBar) findViewById(R.id.freq_bar);
		mTextView = (TextView) findViewById(R.id.freq_text);
		mSendButton = (Button) findViewById(R.id.send_button);
		mDurationText = (EditText) findViewById(R.id.duration_text);
		mHandler = new Handler();

		mFreqBar.setOnSeekBarChangeListener(this);
		mSendButton.setOnClickListener(this);

		// This will get the SD Card directory and create a folder named
		// Drummer in it.
		final File sdCard = Environment.getExternalStorageDirectory();
		final File directory = new File(sdCard.getAbsolutePath() + "/Drummer");
		directory.mkdirs();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mRecordThread.terminate();
		try {
			mRecordThread.join();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
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
			mHandler.removeCallbacks(mPauseTask);
			mSendButton.setEnabled(true);
		}
	};

	@Override
	public void onClick(final View v) {
		mSendButton.setEnabled(false);
		mRecordThread = new AudioRecordThread();

		double sendDuration = Double.parseDouble(mDurationText.getText()
				.toString());

		if (sendDuration > maxDuration) {
			sendDuration = maxDuration;
			mDurationText.setText(Double.toString(sendDuration));
		}

		final long recvDuration = 3000;

		mRecordThread.start();
		mHandler.postDelayed(mPauseTask, recvDuration);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onProgressChanged(final SeekBar seekBar, final int progress,
			final boolean fromUser) {
		if (fromUser) {
			sliderval = progress / (double) seekBar.getMax();
		}
		mTextView.setText(Double.toString(0 + 22050 * sliderval));

	}

	@Override
	public void onStartTrackingTouch(final SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(final SeekBar seekBar) {
		// TODO Auto-generated method stub

	}
}