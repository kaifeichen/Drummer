package edu.berkeley.drummer_sender;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SenderActivity extends Activity implements View.OnClickListener {
	private AudioSendThread mSendThread;
	private final int mSampleRate = 44100;
	private EditText mStartFreqText;
	private EditText mStopFreqText;
	private EditText mDurationText;
	private Button mSendButton;
	private final double maxDuration = 100; // in millisecond
	private final int maxAmp = 32000;

	private class AudioSendThread extends Thread {
		private final int mBufSize;
		private final AudioTrack mAudioTrack;
		// TODO make two input box/ two slider for these two frequency
		final int mStartFreq = Integer.parseInt(mStartFreqText.getText()
				.toString());
		final int mStopFreq = Integer.parseInt(mStopFreqText.getText()
				.toString());
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
			final CharSequence durationText = mDurationText.getText();
			if (durationText != null && durationText.toString().length() > 0) {
				duration = Double.parseDouble(durationText.toString()) / 1000.0;
			}

			mSamples = genSamples(duration, mStartFreq, mStopFreq, mBufSize,
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
		mStartFreqText = (EditText) findViewById(R.id.start_freq_text);
		mStopFreqText = (EditText) findViewById(R.id.stop_freq_text);
		mDurationText = (EditText) findViewById(R.id.duration_text);
		mSendButton = (Button) findViewById(R.id.send_button);

		mSendButton.setOnClickListener(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private static final short[] genSamples(final double duration,
			final double startFreq, final double stopFreq, final int buffsize,
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
		k = Math.pow(stopFreq / startFreq, 1 / duration);

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
}