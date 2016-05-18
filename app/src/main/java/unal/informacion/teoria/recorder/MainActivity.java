package unal.informacion.teoria.recorder;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // recorder variable instantiations
    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private boolean recording = true;
    private boolean playing = true;

    /**
     * Start or stop the recording
     *
     * @param start: true if will record, false otherwise
     */
    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    /**
     * Start or stop the reproduction
     *
     * @param start: true if will play, false otherwise
     */
    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    /**
     * Start playing the recording
     */
    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    playClick(getWindow().getDecorView().getRootView());
                }
            });

            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    /**
     * Stop playing the recording
     */
    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    /**
     * Start recording
     */
    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    /**
     * Stop recording
     */
    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    /**
     * Activate recording button
     *
     * @param view: Android view
     */
    public void recordClick(View view) {
        if (playing) {
            onRecord(recording);
            Button button = (Button) findViewById(R.id.rec);
            if (button != null) {
                if (recording)
                    button.setText(R.string.stop_rec);
                else
                    button.setText(R.string.start_rec);
                recording = !recording;
            } else
                Toast.makeText(getApplicationContext(), "Error: not existing button", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(getApplicationContext(), R.string.wait_play, Toast.LENGTH_SHORT).show();
    }

    /**
     * Activate reproducer button
     *
     * @param view: Android view
     */
    public void playClick(View view) {
        if (recording) {
            onPlay(playing);
            Button button = (Button) findViewById(R.id.player);
            if (button != null) {
                if (playing)
                    button.setText(R.string.stop_play);
                else
                    button.setText(R.string.start_play);
                playing = !playing;
            } else
                Toast.makeText(getApplicationContext(), "Error: not existing button", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(getApplicationContext(), R.string.wait_rec, Toast.LENGTH_SHORT).show();
    }

    public MainActivity() {
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }
}
