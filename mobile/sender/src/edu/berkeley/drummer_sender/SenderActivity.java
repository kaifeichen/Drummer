package edu.berkeley.drummer_sender;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SenderActivity extends Activity {
	private boolean mRunning = false;
	private AudioSendThread mSendThread;
	private final int mSampleRate = 44100;
	private EditText mAmpRatioText;
	private EditText mStartFreqText;
	private EditText mStopFreqText;
	private EditText mDurationText;
	private EditText mSendItvlText;
	private Button mStartButton;
	private Button mStopButton;
	private final double maxDuration = 100; // in millisecond
	private final int maxAmp = 32767;

	private class AudioSendThread extends Thread {
		private volatile boolean mRun = true;
		private final Handler handler;
		private final int mBufSize;
		private final AudioTrack mAudioTrack;
		// TODO make two input box/ two slider for these two frequency
		final int mStartFreq = Integer.parseInt(mStartFreqText.getText()
				.toString());
		final int mStopFreq = Integer.parseInt(mStopFreqText.getText()
				.toString());
		final int mSendItvl = Integer.parseInt(mSendItvlText.getText()
				.toString());
		private final short mSamples[];

		private final Runnable mSend = new Runnable() {
			@Override
			public void run() {
				if (mRun) {
					mAudioTrack.play();
					mAudioTrack.write(mSamples, 0, mSamples.length);
					mAudioTrack.stop(); // blocking until all data is played
					handler.postDelayed(mSend, mSendItvl);
				} else {
					mAudioTrack.release();
				}
			}
		};

		public AudioSendThread() {
			handler = new Handler();

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
			final int amp = (int) (Double.parseDouble(mAmpRatioText.getText()
					.toString()) * maxAmp);
			mSamples = genSamples(duration, mStartFreq, mStopFreq, mBufSize,
					mSampleRate, amp);
		}

		@Override
		public void run() {
			setPriority(Thread.MAX_PRIORITY);
			handler.postDelayed(mSend, 0);
		}

		public void terminate() {
			mRun = false;
		}

		private final short[] genSamples(final double duration,
				final double startFreq, final double stopFreq,
				final int buffsize, final int sampleRate, final int maxAmp) {
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
			// TODO check duration is not 0
			k = Math.pow(stopFreq / startFreq, 1 / duration);

			// TODO make this iterate over all timestamps and call function to
			// get
			// the value

			final short[] array = {0, 38, 77, 83, 35, -61, -165, -209, -141,
					39, 259, 400, 350, 82, -311, -635, -685, -360, 245, 857,
					1133, 843, 27, -971, -1639, -1545, -594, 850, 2094, 2426,
					1512, -359, -2337, -3375, -2777, -617, 2177, 4212, 4303,
					2141, -1426, -4696, -5909, -4177, -61, 4558, 7321, 6577,
					2338, -3554, -8203, -9068, -5328, 1514, 8206, 11271, 8802,
					1597, -7028, -12747, -12384, -5650, 4481, 13063, 15584,
					10326, -552, -11864, -17858, -15138, -4559, 8954, 18692,
					19486, 10446, -4352, -17690, -22727, -16514, -1679, 14652,
					24281, 22061, 8640, -9633, -23718, -26362, -15846, 2959,
					20845, 28785, 22509, 4794, -15751, -28882, -27847, -12870,
					8816, 26472, 31194, 20423, -670, -21676, -32101, -26638,
					-7881, 14910, 30401, 30842, 15971, -6837, -26241, -32597,
					-22779, -1724, 20057, 31756, 27648, 9915, -12515, -28481,
					-30164, -16940, 4414, 23208, 30203, 22181, 3421, -16580,
					-27932, -25260, -10254, 9349, 23770, 26074, 15520, -2272,
					-18311, -24787, -18885, -3986, 12238, 21785, 20269, 8932,
					-6220, -17598, -19826, -12283, 832, 12819, 17894, 13979,
					3509, -8019, -14931, -14156, -6574, 3675, 11431, 13104,
					8320, -123, -7860, -11197, -8866, -2455, 4598, 8833, 8447,
					4036, -1906, -6376, -7358, -4717, -83, 4115, 5905, 4683,
					1358, -2240, -4360, -4156, -1998, 844, 2931, 3360, 2140,
					70, -1749, -2487, -1945, -566, 872, 1678, 1568, 740, -296,
					-1018, -1137, -703, -23, 538, 739, 556, 154, -232, -424,
					-377, -167, 66, 208, 216, 123, 2, -80, -98, -64, -14, 19};

			for (int j = 0; j < sampleNum; j++) {
				for (int i = 0; i < buffsize; i++) {
					if ((j == sampleNum - 1) && i > remain - 1) {
						samples[j * buffsize + i] = (short) 0;
					} else {
						samples[j * buffsize + i] = (short) (maxAmp / 32767.0 * array[j
								* buffsize + i]);
						// samples[j * buffsize + i] = (short) (maxAmp *
						// Math.sin(twopi
						// * startFreq * (Math.pow(k, phase) - 1) /
						// Math.log(k)));
						phase += 1.0 / sampleRate;
					}

				}
			}

			return samples;
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

		mStartButton.setOnClickListener(mStartListener);
		mStopButton.setOnClickListener(mStopListener);
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

		mSendThread = new AudioSendThread();

		// cap the send duration to avoid long noise
		double sendDuration = Double.parseDouble(mDurationText.getText()
				.toString());
		if (sendDuration > maxDuration) {
			sendDuration = maxDuration;
			mDurationText.setText(Double.toString(sendDuration));
		}

		mSendThread.start();
	}

	public void stop() {
		if (mRunning == true) {
			mSendThread.terminate();
			try {
				mSendThread.join();
			} catch (final InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mSendThread = null;

			mRunning = false;

			mStartButton.setEnabled(true);
			mStopButton.setEnabled(false);
		}
	}
}