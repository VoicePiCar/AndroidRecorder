package unal.informacion.teoria.recorder;

import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import java.net.URISyntaxException;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "AudioRecordTest";

    // options for Audiorecorder and Audiotrack
    private int format = AudioFormat.ENCODING_PCM_16BIT;
    private int sampleSize = 8000;

    // recorder variable instantiations
    private AudioRecorder audioInput;
    private AudioTrack audioOutput = null;
    private boolean recording = true;
    private boolean playing = true;

    // Audio buffer
    private List<Short> audioBuffer;

    // connection variables
    private Socket socket;
    private String serverIP;

    // User
    private User user;

    /**
     * Process voice and sends command to server
     */
    private void processCommand() {

        FFT commandFFT = new FFT(audioBuffer);
        commandFFT = FFT.ditfft2(commandFFT);
        double[] magnitude = commandFFT.magnitude();
        String command = "";

        for (String key : user.getCommands().keySet()) {

            double result = SignalProcessing.jaccardCoefficient(magnitude,
                    user.getCommand(key));

            if (result > 0.5) {
                command = key;
                break;
            }
        }

        if (!command.isEmpty()) {
            sendCommand(command);
            Toast.makeText(getApplicationContext(), command, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), R.string.commandNotFound, Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Start or stop the recording
     */
    private void onRecord() {

        Thread recordThread = new Thread(new Runnable() {

            @Override
            public void run() {
                audioInput.startRecording();
            }
        });
        recordThread.start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        audioBuffer = audioInput.stopRecording();
        processCommand();
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
                audioOutput.setNotificationMarkerPosition(audioBuffer.size());
                audioOutput.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
                    @Override
                    public void onMarkerReached(AudioTrack track) {
                        Button button = (Button) findViewById(R.id.player);
                        assert button != null;
                        button.setText(R.string.start_play);
                        playing = !playing;
                    }

                    @Override
                    public void onPeriodicNotification(AudioTrack track) {
                    }
                });
                audioOutput.play();

                short[] audioData = new short[audioBuffer.size()];
                for (int i = 0; i < audioData.length; i++) {
                    audioData[i] = audioBuffer.get(i);
                }

                audioOutput.write(audioData, 0, audioData.length);
            }
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
     * Activate recording button
     *
     * @param view: Android view
     */
    public void recordClick(View view) {
        if (playing) {
            onRecord();
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
        audioInput = new AudioRecorder(format, sampleSize);
        user = new User();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (settings.contains("user")) {
            String nameUser = settings.getString("user", null);
            String jsonUser = settings.getString(nameUser + "cmd", null);

            Gson gson = new Gson();
            user = (gson.fromJson(jsonUser, user.getClass()));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (audioInput != null) {
            audioInput = null;
        }

        if (audioOutput != null) {
            audioOutput.release();
            audioOutput = null;
        }
    }
}
