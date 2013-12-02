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
                        double S0;
                        if (Sendtime.getText().toString().isEmpty()){
                            S0=0.0;
                        }
                        else{
                            S0= Double.parseDouble(Sendtime.getText().toString())/1000.0;
                        }
                        
                        
                        int amp = 32000;
                        double twopi = 8.*Math.atan(1.);
                        double ph = 0.0;
                        double fr =  2000 + 20000*sliderval;  
                        double count = S0*sr; 
                        int sample=1;
                        int remain=0;
                        if (count<buffsize){
                            remain=(int)count;
                        }else{
                            sample=(int) (count/buffsize);
                            remain=(int) (count-sample*buffsize);
                        }
                        short samples[] = new short[buffsize*sample];
                        audioTrack.play();
         
                        for(int j=0; j<sample;j++){
                            for(int i=0; i < buffsize; i++){
                                samples[j*buffsize+i] = (short) (amp*Math.sin(ph));
                                ph += twopi*fr/sr;
                                if ((j==sample-1) && i>remain-1){
                                    samples[j*buffsize+i]=(short)0;
                                }
                            }
                        }
                        audioTrack.write(samples, 0, buffsize*sample);
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
