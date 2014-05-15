package edu.berkeley.drummer_receiver;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class ReceiverActivity extends Activity {
    private boolean mRunning = false;
    private File mAudioDir;
    private File mReportDir;
    private BufferedOutputStream mAudioOS;
    private BufferedOutputStream mReportOS;
    private AudioRecordThread mRecordThread;
    private final int mSampleRate = 44100;
    private Button mStartButton;
    private Button mStopButton;
    private Button mReportDoorButton;
    private Button mReportCornerButton;
    private IMU mIMU;

    private class AudioRecordThread extends Thread {
        private volatile boolean mRun = true;
        private final AudioRecord mRecorder;
        private final int mBufSize;

        public AudioRecordThread() {
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
            setPriority(Thread.MAX_PRIORITY);
            try {
                mRecorder.startRecording();
                while (mRun == true) {
                    final byte[] buffer = new byte[mBufSize];
                    // blocking read, which returns when
                    // buffer.length bytes are recorded
                    mRecorder.read(buffer, 0, buffer.length); // Bytes
                    System.out.print(buffer);
                    mAudioOS.write(buffer);
                }
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

    private final OnClickListener mReportListener = new OnClickListener() {

        @Override
        public void onClick(final View v) {
            final int buttonId = v.getId();
            try {
                String data;
                if (buttonId == R.id.report_door_button) {
                    data = System.currentTimeMillis() + " ";
                    data += System.nanoTime() + " ";
                    data += "Door\n";
                    mReportOS.write(data.getBytes());
                } else if (buttonId == R.id.report_corner_button) {
                    data = System.currentTimeMillis() + " ";
                    data += System.nanoTime() + " ";
                    data += "Corner\n";
                    mReportOS.write(data.getBytes());
                }
            } catch (final IOException e) {
                Toast.makeText(ReceiverActivity.this, e.toString(),
                        Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStartButton = (Button) this.findViewById(R.id.start_imu_button);
        mStopButton = (Button) this.findViewById(R.id.stop_imu_button);
        mReportDoorButton = (Button) this.findViewById(R.id.report_door_button);
        mReportCornerButton = (Button) this
                .findViewById(R.id.report_corner_button);

        mStartButton.setOnClickListener(mStartListener);
        mStopButton.setOnClickListener(mStopListener);
        mReportDoorButton.setOnClickListener(mReportListener);
        mReportCornerButton.setOnClickListener(mReportListener);

        mStartButton.setEnabled(true);
        mStopButton.setEnabled(false);
        mReportDoorButton.setEnabled(false);
        mReportCornerButton.setEnabled(false);

        // This will get the SD Card directory and create a folder named
        // Drummer in it.
        final File sdCard = Environment.getExternalStorageDirectory();
        mAudioDir = new File(sdCard.getAbsolutePath() + "/Drummer/Audio");
        mAudioDir.mkdirs();
        mReportDir = new File(sdCard.getAbsolutePath() + "/Drummer/Report");
        mReportDir.mkdirs();

        mIMU = new IMU(this);
    }
    @Override
    public void onPause() {
        stop();
        super.onPause();
    }

    private void start() {
        mStartButton.setEnabled(false);
        mStopButton.setEnabled(true);
        mReportDoorButton.setEnabled(true);
        mReportCornerButton.setEnabled(true);

        mRunning = true;

        try {
            final String audioFileName = Long.toString(System
                    .currentTimeMillis()) + ".txt";
            final File audioFile = new File(mAudioDir, audioFileName);
            mAudioOS = new BufferedOutputStream(new FileOutputStream(audioFile));

            final String reportFileName = Long.toString(System
                    .currentTimeMillis()) + ".txt";
            final File reportFile = new File(mReportDir, reportFileName);
            mReportOS = new BufferedOutputStream(new FileOutputStream(
                    reportFile));
        } catch (final FileNotFoundException e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }

        mRecordThread = new AudioRecordThread();
        mRecordThread.start();

        mIMU.start();
    }

    public void stop() {
        if (mRunning == true) {
            mIMU.stop();

            mRecordThread.terminate();
            try {
                mRecordThread.join();
            } catch (final InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mRecordThread = null;

            try {
                if (mAudioOS != null) {
                    mAudioOS.flush();
                    mAudioOS.close();
                    mAudioOS = null;
                }
                if (mReportOS != null) {
                    mReportOS.flush();
                    mReportOS.close();
                    mReportOS = null;
                }
            } catch (final IOException e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            }

            mRunning = false;

            mStartButton.setEnabled(true);
            mStopButton.setEnabled(false);
            mReportDoorButton.setEnabled(false);
            mReportCornerButton.setEnabled(false);
        }
    }
}