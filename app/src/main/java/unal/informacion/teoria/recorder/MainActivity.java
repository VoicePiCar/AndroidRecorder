package unal.informacion.teoria.recorder;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder.AudioSource;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {


    private static final String LOG_TAG = "AudioRecordTest";

    // options for Audiorecorder and Audiotrack
    private int format = AudioFormat.ENCODING_PCM_16BIT;
    private int sampleSize = 44100;

    // recorder variable instantiations
    private AudioRecord audioInput = null;
    private AudioTrack audioOutput = null;
    private boolean recording = true;
    private boolean playing = true;

    // Audio buffer
    private List<Short> audioBuffer;

    // connection variables
    private Socket socket;
    private String serverIP;

    /**
     * Start or stop the recording
     *
     * @param start: true if will record, false otherwise
     */
    private void onRecord(boolean start) {
        if (start) {
            Thread recordThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    startRecording();
                }
            });

            recordThread.start();
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
            Thread playThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    startPlaying();
                }
            });

            playThread.start();
        } else {
            stopPlaying();
        }
    }

    /**
     * Start playing the recording
     */
    private void startPlaying() {

        int channel_config = AudioFormat.CHANNEL_OUT_MONO;
        int bufferSize = AudioTrack.getMinBufferSize(sampleSize, channel_config, format);
        audioOutput = new AudioTrack(AudioManager.STREAM_MUSIC, sampleSize, channel_config,
                format, bufferSize, AudioTrack.MODE_STREAM);

        if (audioOutput.getState() == AudioTrack.STATE_INITIALIZED) {

            if (audioBuffer != null) {
                audioOutput.play();

                short[] audioData = new short[audioBuffer.size()];
                for (int i = 0; i < audioData.length; i++) {
                    audioData[i] = audioBuffer.get(i);
                }

                audioOutput.write(audioData, 0, audioData.length);
            }

            // TODO when the recording stops playing change the state of the button

        } else {
            Log.e(LOG_TAG, "audioTrack init failed");
        }
    }

    /**
     * Stop playing the recording
     */
    private void stopPlaying() {
        if (audioOutput.getState() == AudioTrack.STATE_INITIALIZED) {
            audioOutput.stop();
        }
        audioOutput.release();
        audioOutput = null;
    }

    /**
     * Start recording
     */
    private void startRecording() {

        int channel_config = AudioFormat.CHANNEL_IN_MONO;
        int bufferSize = AudioRecord.getMinBufferSize(sampleSize, channel_config, format);
        audioInput = new AudioRecord(AudioSource.VOICE_RECOGNITION, sampleSize, channel_config, format, bufferSize);

        if (audioInput.getState() == AudioRecord.STATE_INITIALIZED) {

            audioInput.startRecording();
            short[] audioInBuffer = new short[bufferSize];
            audioBuffer = new ArrayList<>();
            while (!recording) {
                int numShorts = audioInput.read(audioInBuffer, 0, bufferSize);

                for (int i = 0; i < numShorts; i++) {
                    audioBuffer.add(audioInBuffer[i]);
                }
            }
        } else {
            Log.e(LOG_TAG, "audioRecord init failed");
        }
    }

    /**
     * Stop recording
     */
    private void stopRecording() {
        if (audioInput.getState() == AudioRecord.STATE_INITIALIZED) {
            audioInput.stop();
        }

        audioInput.release();
        audioInput = null;
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
        if (Patterns.IP_ADDRESS.matcher(input.getText()).matches()) {
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
        } else
            Toast.makeText(getApplicationContext(), R.string.invalid_ip, Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (audioInput != null) {
            audioInput.release();
            audioInput = null;
        }

        if (audioOutput != null) {
            audioOutput.release();
            audioOutput = null;
        }
    }
}
