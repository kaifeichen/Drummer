package com.example.firstapp;
import com.example.firstapp.R;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity {
    Thread t;
    int sr = 44100;
    boolean isRunning = true;
    SeekBar fSlider;
    TextView Viewer;
    double sliderval;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fSlider = (SeekBar) findViewById(R.id.frequency);
        Viewer = (TextView) findViewById(R.id.frequencytext);
        OnSeekBarChangeListener listener = new OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) { }
            public void onStartTrackingTouch(SeekBar seekBar) { }
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) sliderval = progress / (double)seekBar.getMax();
                Viewer.setText(Double.toString(2000 + 20000*sliderval));

            }
        };

        fSlider.setOnSeekBarChangeListener(listener);

        t = new Thread() {
        	@Override     	  
            public void run() {

                setPriority(Thread.MAX_PRIORITY);

                int buffsize = AudioTrack.getMinBufferSize(sr, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

                AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sr, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, buffsize, AudioTrack.MODE_STREAM);

                short samples[] = new short[buffsize];
                int amp = 10000;
                double twopi = 8.*Math.atan(1.);
                double fr = 2000.f;
                double ph = 0.0;

                audioTrack.play();

                while(isRunning){
                    fr =  2000 + 20000*sliderval;
                    for(int i=0; i < buffsize; i++){
                        samples[i] = (short) (amp*Math.sin(ph));
                        ph += twopi*fr/sr;
                    }
                    audioTrack.write(samples, 0, buffsize);
                }
                audioTrack.stop();
                audioTrack.release();
            }
        };
        t.start();        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void onDestroy(){
        super.onDestroy();
        isRunning = false;
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        t = null;
     }
}