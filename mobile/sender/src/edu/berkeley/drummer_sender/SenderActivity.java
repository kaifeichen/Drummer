package edu.berkeley.drummer_sender;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class SenderActivity extends Activity {
	private boolean mBusy = false;
	private AudioSendThread mSendThread;
	private final int mSampleRate = 44100; // Hz
	private final double mMaxDuration = 10000; // in ms, to avoid long noisy
	private final short mMaxAmp = 0x7FFF; // 32767
	private final double mStartPhase = 0;
	// UI objects
	private EditText mAmpRatioText;
	private EditText mStartFreqText;
	private EditText mStopFreqText;
	private EditText mDurationText;
	private EditText mSendItvlText;
	private Spinner mSignalSpinner;
	private Spinner mWinSpinner;
	private Button mStartButton;
	private Button mStopButton;

	private class AudioSendThread extends Thread {
		private volatile boolean mRun = true;
		private final Handler mHandler;
		private final int mMinBufSize;
		private final AudioTrack mAudioTrack;
		// TODO make two input box/ two slider for these two frequency
		private final double mAmpRatio = Double.parseDouble(mAmpRatioText
				.getText().toString());
		private final int mStartFreq = Integer.parseInt(mStartFreqText
				.getText().toString());
		private final int mStopFreq = Integer.parseInt(mStopFreqText.getText()
				.toString());
		private final int mSendItvl = Integer.parseInt(mSendItvlText.getText()
				.toString());
		private final String mSignal = mSignalSpinner.getSelectedItem()
				.toString();
		private final String mWindow = mWinSpinner.getSelectedItem().toString();
		private final short mSamples[];

		private final Runnable mSend = new Runnable() {
			@Override
			public void run() {
				if (mRun) {
					mAudioTrack.play();
					mAudioTrack.write(mSamples, 0, mSamples.length);
					mAudioTrack.stop(); // blocking until all data is played
					mHandler.postDelayed(mSend, mSendItvl);
				} else {
					mAudioTrack.release();
				}
			}
		};

		public AudioSendThread() {
			mHandler = new Handler();

			// TODO buffsize and audioTrack should be created in onCreate
			mMinBufSize = AudioTrack.getMinBufferSize(mSampleRate,
					AudioFormat.CHANNEL_OUT_MONO,
					AudioFormat.ENCODING_PCM_16BIT);
			mAudioTrack = new AudioTrack(AudioManager.STREAM_ALARM,
					mSampleRate, AudioFormat.CHANNEL_OUT_MONO,
					AudioFormat.ENCODING_PCM_16BIT, mMinBufSize,
					AudioTrack.MODE_STREAM);
			double duration = 0;
			final CharSequence durationText = mDurationText.getText();
			if (durationText != null && durationText.toString().length() > 0) {
				duration = Double.parseDouble(durationText.toString()) / 1000.0;
			}
			mSamples = getSamples(duration);
		}

		@Override
		public void run() {
			setPriority(Thread.MAX_PRIORITY);
			mHandler.postDelayed(mSend, 0);
		}

		public void terminate() {
			mRun = false;
			mAudioTrack.pause();
		}

		private final short[] getSamples(final double duration) {
			final int sampleNum = (int) (duration * mSampleRate);
			final int bufSize = sampleNum < mMinBufSize ? mMinBufSize
					: sampleNum;

			// initial values in arrays are 0 in java
			final short samples[] = new short[bufSize];
			double time = 0.0;
			double val = 0;
			for (int i = 0; i < sampleNum; i++) {
				if (mSignal.equals("Linear Chirp")) {
					val = linearChirp(time, mStartFreq, mStartPhase, mStopFreq,
							duration);
				} else if (mSignal.equals("Log Chirp")) {
					val = logChirp(time, mStartFreq, mStartPhase, mStopFreq,
							duration);
				}

				if (mWindow.equals("Cosine")) {
					val = val * cosWindow(i, sampleNum);
				} else if (mWindow.equals("Blackman-Harris")) {
					val = val * bhWindow(i, sampleNum);
				}
				samples[i] = (short) (mAmpRatio * mMaxAmp * val);
				time += 1.0 / mSampleRate;
			}

			return samples;
		}

		// Linear chirp: x(t) = sin(phase0 + 2*pi*(f0*t + k/2*t^2))
		private double linearChirp(final double time, final double startFreq,
				final double startPhase, final double stopFreq,
				final double timeSpan) {
			final double k = (stopFreq - startFreq) / timeSpan;
			final double phase = startPhase + 2. * Math.PI
					* (startFreq * time + k / 2 * time * time);
			return Math.sin(phase);
		}

		// logarithmic chirp:
		// x(t) = sin(phase0 + 2*pi*f0*T/ln(f1/f0)*(e^(t/T*ln(f1/f0)) - 1))
		private double logChirp(final double time, final double startFreq,
				final double startPhase, final double stopFreq,
				final double timeSpan) {
			final double phase = startPhase
					+ 2.
					* Math.PI
					* startFreq
					* timeSpan
					/ Math.log(stopFreq / startFreq)
					* (Math.exp(time / timeSpan
							* Math.log(stopFreq / startFreq)) - 1);
			return Math.sin(phase);
		}

		// Cosine window
		// http://en.wikipedia.org/wiki/Window_function#Cosine_window
		private double cosWindow(final int n, final int N) {
			final double val = Math.sin(Math.PI * n / (N - 1));
			return val;
		}

		// Blackman-Harris window
		// http://en.wikipedia.org/wiki/Window_function#Blackman.E2.80.93Harris_window
		private double bhWindow(final int n, final int N) {
			final double a0 = 0.35875;
			final double a1 = 0.48829;
			final double a2 = 0.14128;
			final double a3 = 0.01168;
			final double val = a0 - a1 * Math.cos(2 * Math.PI * n / (N - 1))
					+ a2 * Math.cos(4 * Math.PI * n / (N - 1)) - a3
					* Math.cos(6 * Math.PI * n / (N - 1));
			return val;
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
		mAmpRatioText = (EditText) findViewById(R.id.amp_ratio_text);
		mStartFreqText = (EditText) findViewById(R.id.start_freq_text);
		mStopFreqText = (EditText) findViewById(R.id.stop_freq_text);
		mDurationText = (EditText) findViewById(R.id.duration_text);
		mSendItvlText = (EditText) findViewById(R.id.send_itvl_text);
		mStartButton = (Button) findViewById(R.id.start_chirp_button);
		mStopButton = (Button) findViewById(R.id.stop_chirp_button);
		mSignalSpinner = (Spinner) findViewById(R.id.signal_spinner);
		mWinSpinner = (Spinner) findViewById(R.id.window_spinner);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.signal_array,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSignalSpinner.setAdapter(adapter);

		adapter = ArrayAdapter.createFromResource(this, R.array.window_array,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mWinSpinner.setAdapter(adapter);

		mStartButton.setOnClickListener(mStartListener);
		mStopButton.setOnClickListener(mStopListener);
		mStartButton.setEnabled(true);
		mStopButton.setEnabled(false);
	}

	@Override
	public void onPause() {
		stop();
		super.onPause();
	}

	private void start() {
		mStartButton.setEnabled(false);
		mStopButton.setEnabled(true);

		mBusy = true;

		mSendThread = new AudioSendThread();

		// cap the send duration to avoid long noise
		double sendDuration = Double.parseDouble(mDurationText.getText()
				.toString());
		if (sendDuration > mMaxDuration) {
			sendDuration = mMaxDuration;
			mDurationText.setText(Double.toString(sendDuration));
		}

		mSendThread.start();
	}

	public void stop() {
		if (mBusy == true) {
			mSendThread.terminate();
			try {
				mSendThread.join();
			} catch (final InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mSendThread = null;

			mBusy = false;

			mStartButton.setEnabled(true);
			mStopButton.setEnabled(false);
		}
	}
}