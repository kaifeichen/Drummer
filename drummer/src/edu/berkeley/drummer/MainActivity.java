package edu.berkeley.drummer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONArray;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity {
	private AudioSendThread t;
	private AudioRecordThread s;
	int sr = 44100;
	long S0;
	boolean isRunning = true;
	SeekBar fSlider;
	TextView Viewer;
	Button Send;
	EditText Sendtime;
	double sliderval;
	double maxDuration = 50;
	private Handler mHandler;

	private class AudioSendThread extends Thread {
		@Override
		public void run() {

			setPriority(Thread.MAX_PRIORITY);

			final int buffsize = AudioTrack.getMinBufferSize(sr,
					AudioFormat.CHANNEL_OUT_MONO,
					AudioFormat.ENCODING_PCM_16BIT);
			final AudioTrack audioTrack = new AudioTrack(
					AudioManager.STREAM_ALARM, sr,
					AudioFormat.CHANNEL_OUT_MONO,
					AudioFormat.ENCODING_PCM_16BIT, buffsize,
					AudioTrack.MODE_STREAM);
			double S0 = 0;
			if (Sendtime.getText() != null) {
				if (Sendtime.getText().toString().length() > 0) {
					S0 = Double.parseDouble(Sendtime.getText().toString()) / 1000.0;
				}
			}

			final double fr = 0 + 22050 * sliderval;
			final double count = S0 * sr;
			int sample = 1;
			int remain = 0;
			if (count < buffsize) {
				remain = (int) count;
			} else {
				sample = (int) (count / buffsize);
				remain = (int) (count - sample * buffsize);
			}
			audioTrack.play();
			int start_fr = 1000;
            int end_fr = 2000;
            short samples[] = sample(S0, start_fr, end_fr, buffsize, sample, remain);
			audioTrack.write(samples, 0, buffsize * sample);
			audioTrack.stop();
			audioTrack.release();
		}
	}

	private class AudioRecordThread extends Thread {
		private volatile boolean mRun = true;
		private AudioRecord mRecorder;
		private BufferedOutputStream os = null;

		@Override
		public void run() {
			final File sdCard = Environment.getExternalStorageDirectory();
			final File directory = new File(sdCard.getAbsolutePath()
					+ "/Drummer");
			final String fileName = Long.toString(System.currentTimeMillis())
					+ ".pcm";
			final File file = new File(directory, fileName);
			try {
				os = new BufferedOutputStream(new FileOutputStream(file));
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				final int bufferSize = AudioRecord.getMinBufferSize(44100,
						AudioFormat.CHANNEL_IN_MONO,
						AudioFormat.ENCODING_PCM_16BIT);
				System.out.println(bufferSize);
				mRecorder = new AudioRecord(
						MediaRecorder.AudioSource.CAMCORDER, 44100,
						AudioFormat.CHANNEL_IN_MONO,
						AudioFormat.ENCODING_PCM_16BIT, bufferSize);

				mRecorder.startRecording();

				while (mRun == true) {
					final byte[] buffer = new byte[bufferSize];
					// blocking read, which returns when
					// buffer.length bytes are recorded
					mRecorder.read(buffer, 0, buffer.length); // Bytes

					os.write(buffer);
				}
				os.flush();
				os.close();
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
		fSlider = (SeekBar) findViewById(R.id.frequency);
		Viewer = (TextView) findViewById(R.id.frequencytext);
		Send = (Button) findViewById(R.id.send_button);
		Sendtime = (EditText) findViewById(R.id.timemilisec);
		mHandler = new Handler();

		final OnSeekBarChangeListener listener = new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(final SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(final SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(final SeekBar seekBar,
					final int progress, final boolean fromUser) {
				if (fromUser)
					sliderval = progress / (double) seekBar.getMax();
				Viewer.setText(Double.toString(0 + 22050 * sliderval));

			}
		};

		fSlider.setOnSeekBarChangeListener(listener);

		Send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				Send.setEnabled(false);
				t = new AudioSendThread();
				s = new AudioRecordThread();

				double sendDuration = Double.parseDouble(Sendtime.getText()
						.toString());

				if (sendDuration > maxDuration) {
					sendDuration = maxDuration;
					Sendtime.setText(Double.toString(sendDuration));
				}

				long recvDuration = 1000;

				// This will get the SD Card directory and create a folder named
				// MyFiles in it.
				final File sdCard = Environment.getExternalStorageDirectory();
				final File directory = new File(sdCard.getAbsolutePath()
						+ "/Drummer");
				directory.mkdirs();


				t.start();
				try {
					t.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				s.start();
				mHandler.postDelayed(mPauseTask, recvDuration);
			}
		});

	}
	
    private short[] sample(double S0, double start_fr, double end_fr, int buffsize, int sample, int remain){
        int amp = 32000;
        double twopi = 8.*Math.atan(1.);
        double ph = 0.0;
        double fr =  2000 + 20000*sliderval;  


    	short samples[] = new short[buffsize*sample];
    	double k = 1;
    	k = Math.pow(end_fr/start_fr, 1/S0);
    	
    	for(int j=0; j<sample;j++){
            for(int i=0; i < buffsize; i++){
                samples[j * buffsize + i] = (short) (amp * Math.sin(twopi*start_fr*(Math.pow(k, ph)-1)/Math.log(k)));
                System.out.println(samples[j*buffsize+i]);
                ph += 1.0/sr;

                if ((j==sample-1) && i>remain-1){
                    samples[j*buffsize+i]=(short)0;
                }
            }
        }
    	
    	
    	return samples;
    }

	private final Runnable mPauseTask = new Runnable() {
		@Override
		public void run() {
			s.terminate();
			try {
				s.join();
			} catch (final InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mHandler.removeCallbacks(mPauseTask);
			Send.setEnabled(true);
		}
	};

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		isRunning = false;
		s.interrupt();
		try {
			t.join();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		t = null;
	}
}