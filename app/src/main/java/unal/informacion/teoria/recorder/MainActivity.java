package unal.informacion.teoria.recorder;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import android.widget.EditText;
import io.socket.client.Socket;
import io.socket.client.IO;
import io.socket.emitter.Emitter;

import java.net.URISyntaxException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    // recorder variable instantiations
    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private boolean recording = true;
    private boolean playing = true;

    // connection variables
    private Socket socket;
    private String serverIP;

    /**
     * Cast an audio file to a byte array
     *
     * @return an empty array if fails and the audio byte array otherwise
     */
    private byte[] audioToArray() {
        try {
            FileInputStream inputStream = new FileInputStream(mFileName);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            while (inputStream.available() > 0) {
                outputStream.write(inputStream.read());
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

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
        byte[] bytes = audioToArray();
        System.out.println(Arrays.toString(bytes));
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

    /**
     * Activate test connection button
     *
     * @param view: Android view
     */
    public void testConnection(View view) {

        EditText input = (EditText) findViewById(R.id.inputRobotIP);
        assert input != null;
        if (!input.getText().toString().equals(serverIP)) {
            serverIP = input.getText().toString().trim();
            newConnection();
        }

        if (socket.connected()) {
            Toast.makeText(getApplicationContext(), R.string.connected,
                    Toast.LENGTH_SHORT).show();
        } else {
            socket.connect();
        }
    }

    /**
     * Send command to server
     *
     * @param command: String
     */
    public void sendCommand(String command) {

        if (serverIP == null || !socket.connected()) {
            Toast.makeText(getApplicationContext(), R.string.disconnected,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (socket.connected()) {
            socket.emit("command", command);
        }
    }

    /**
     * Start a new SocketIO connection
     */
    public void newConnection() {

        try {
            socket = IO.socket("http://" + serverIP + ":81");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), R.string.connected,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                socket.disconnect();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), R.string.connectionError,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
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
