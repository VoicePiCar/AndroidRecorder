package unal.informacion.teoria.recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class AudioRecorder {
    private static final String LOG_TAG = "AudioRecordTest";

    private AudioRecord audioInput = null;
    private int format;
    private int sampleSize;
    private boolean recording;
    List audioBuffer;

    public AudioRecorder(int format, int sampleSize) {
        this.format = format;
        this.sampleSize = sampleSize;
    }

    /**
     * Start recording
     */
    public void startRecording() {
        this.recording = true;
        int channel_config = AudioFormat.CHANNEL_IN_MONO;
        int bufferSize = AudioRecord.getMinBufferSize(sampleSize, channel_config, format);
        audioInput = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, sampleSize, channel_config, format, bufferSize);

        if (audioInput.getState() == AudioRecord.STATE_INITIALIZED) {

            audioInput.startRecording();
            short[] audioInBuffer = new short[bufferSize];
            audioBuffer = new ArrayList<>();
            while (recording && audioInput != null) {
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
    public List stopRecording() {
        this.recording = false;
        if (audioInput.getState() == AudioRecord.STATE_INITIALIZED) {
            audioInput.stop();
        }

        audioInput.release();
        audioInput = null;
        return audioBuffer;
    }

}
