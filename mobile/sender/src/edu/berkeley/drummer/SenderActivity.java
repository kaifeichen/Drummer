package edu.berkeley.drummer;

import java.io.File;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SenderActivity extends Activity
		implements
			View.OnClickListener,
			OnSeekBarChangeListener {
	private AudioSendThread mSendThread;
	private final int mSampleRate = 44100;
	private SeekBar mFreqBar;
	private TextView mTextView;
	private Button mSendButton;
	private EditText mDurationText;
	private double sliderval;
	private final double maxDuration = 100; // in millisecond
	private final int maxAmp = 32000;

	private class AudioSendThread extends Thread {
		private final int mBufSize;
		private final AudioTrack mAudioTrack;
		// TODO make two input box/ two slider for these two freq
		final int startfreq = 5000;
		final int endFreq = 5100;
		private final short mSamples[];

		public AudioSendThread() {
			// TODO buffsize and audioTrack should be created in onCreate
			mBufSize = AudioTrack.getMinBufferSize(mSampleRate,
					AudioFormat.CHANNEL_OUT_MONO,
					AudioFormat.ENCODING_PCM_16BIT);
			mAudioTrack = new AudioTrack(AudioManager.STREAM_ALARM,
					mSampleRate, AudioFormat.CHANNEL_OUT_MONO,
					AudioFormat.ENCODING_PCM_16BIT, mBufSize,
					AudioTrack.MODE_STREAM);
			double duration = 0;
			final Editable durationText = mDurationText.getText();
			if (durationText != null && durationText.toString().length() > 0) {
				duration = Double.parseDouble(durationText.toString()) / 1000.0;
			}

			mSamples = genSamples(duration, startfreq, endFreq, mBufSize,
					mSampleRate, maxAmp);
		}

		@Override
		public void run() {
			setPriority(Thread.MAX_PRIORITY);
			mAudioTrack.play();
			mAudioTrack.write(mSamples, 0, mSamples.length);
			mAudioTrack.stop();
			mAudioTrack.release();
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
	}

	private static final short[] genSamples(final double duration,
			final double startFreq, final double endFreq, final int buffsize,
			final int sampleRate, final int maxAmp) {
		final double count = duration * sampleRate;
		int sampleNum = 1;
		int remain = 0;
		if (count < buffsize) {
			remain = (int) count;
		} else {
			sampleNum = (int) (count / buffsize);
			remain = (int) (count - sampleNum * buffsize);
		}

		final double twopi = 2. * Math.PI;
		double phase = 0.0;

		final short samples[] = new short[buffsize * sampleNum];
		// TODO put the equation in comment
		double k = 1;
		k = Math.pow(endFreq / startFreq, 1 / duration);

		// TODO make this iterate over all timestamps and call function to get
		// the value
		for (int j = 0; j < sampleNum; j++) {
			for (int i = 0; i < buffsize; i++) {
				samples[j * buffsize + i] = (short) (maxAmp * Math.sin(twopi
						* startFreq * (Math.pow(k, phase) - 1) / Math.log(k)));
				phase += 1.0 / sampleRate;

				if ((j == sampleNum - 1) && i > remain - 1) {
					samples[j * buffsize + i] = (short) 0;
				}
			}
		}

		return samples;
	}

	@Override
	public void onClick(final View v) {
		mSendThread = new AudioSendThread();

		double sendDuration = Double.parseDouble(mDurationText.getText()
				.toString());

		if (sendDuration > maxDuration) {
			sendDuration = maxDuration;
			mDurationText.setText(Double.toString(sendDuration));
		}

		mSendThread.start();
		try {
			mSendThread.join();
		} catch (final InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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