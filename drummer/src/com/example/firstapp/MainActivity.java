package com.example.firstapp;

import com.example.firstapp.R;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fSlider = (SeekBar) findViewById(R.id.frequency);
        Viewer = (TextView) findViewById(R.id.frequencytext);
        Send = (Button) findViewById(R.id.send_button);
        Sendtime = (EditText) findViewById(R.id.timemilisec);      
        
        OnSeekBarChangeListener listener = new OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) { }
            public void onStartTrackingTouch(SeekBar seekBar) { }
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) sliderval = progress / (double)seekBar.getMax();
                Viewer.setText(Double.toString(2000 + 20000*sliderval));
                
            }
        };
        
        fSlider.setOnSeekBarChangeListener(listener);
        
        Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	t = new Thread() {
                	@SuppressLint("NewApi") @Override     	  
                    public void run() {

                        setPriority(Thread.MAX_PRIORITY);

                        int buffsize = AudioTrack.getMinBufferSize(sr, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

                        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sr, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, buffsize, AudioTrack.MODE_STREAM);
                        long S0;
                        if (Sendtime.getText().toString().isEmpty()){
                        	S0=0;
                        }
                        else{
                        	S0= Long.parseLong(Sendtime.getText().toString());
                        }
                        
                        short samples[] = new short[buffsize];
                        int amp = 10000;
                        double twopi = 8.*Math.atan(1.);
                        double fr = 2000.f;
                        double ph = 0.0;

                        audioTrack.play();
                        long startTime=System.currentTimeMillis();
                        while(System.currentTimeMillis()-startTime<S0){
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
        });
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