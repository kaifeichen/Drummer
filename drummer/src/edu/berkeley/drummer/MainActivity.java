package edu.berkeley.drummer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity {
    Thread t;
    int sr = 44100;
    long S0;
    boolean isRunning = true;
    SeekBar fSlider;
    TextView Viewer;
    Button Send;
    EditText Sendtime;
    double sliderval;
    double maxDuration = 50;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fSlider = (SeekBar) findViewById(R.id.frequency);
        Viewer = (TextView) findViewById(R.id.frequencytext);
        Send = (Button) findViewById(R.id.send_button);
        Sendtime = (EditText) findViewById(R.id.timemilisec);

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
                t = new Thread() {
                    @SuppressLint("NewApi")
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
                            if (!Sendtime.getText().toString().isEmpty()) {
                                S0 = Double.parseDouble(Sendtime.getText()
                                        .toString()) / 1000.0;
                            }
                        }

                        final short amp = 32000;
                        final double twopi = 8. * Math.atan(1.);
                        double ph = 0.0;
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
                        final short samples[] = new short[buffsize * sample];

                        for (int j = 0; j < sample; j++) {
                            for (int i = 0; i < buffsize; i++) {
                                if ((j == sample - 1) && i > remain - 1) {
                                    samples[j * buffsize + i] = (short) 0;
                                } else {
                                    samples[j * buffsize + i] = (short) (amp * Math
                                            .sin(ph));
                                }
                                ph += twopi * fr / sr;
                            }
                        }
                        audioTrack.play();
                        audioTrack.write(samples, 0, buffsize * sample);
                        audioTrack.stop();
                        audioTrack.release();
                    }
                };

                double duration = Double.parseDouble(Sendtime.getText()
                        .toString());

                if (duration > maxDuration) {
                    duration = maxDuration;
                    Sendtime.setText(Double.toString(duration));
                }
                t.start();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        try {
            t.join();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        t = null;
    }
}
