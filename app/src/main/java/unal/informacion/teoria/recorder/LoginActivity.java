package unal.informacion.teoria.recorder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * A login screen that offers login via voice.
 */
public class LoginActivity extends AppCompatActivity {

    // Record commands
    private boolean recordedName = false;
    private AudioRecorder audioInput;
    private User user = null;
    private ArrayList<String> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        users = new ArrayList<>();
        audioInput = new AudioRecorder(AudioFormat.ENCODING_PCM_16BIT, 8000);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (settings.contains("users")) {
            String jsonUsers = settings.getString("users", null);
            Gson gson = new Gson();
            users = gson.fromJson(jsonUsers, users.getClass());
        }

    }

    /**
     * Activate register button
     *
     * @param view: Android view
     */
    public void registerClick(View view) {

        showRegisterForm(View.VISIBLE);
        EditText userName = (EditText) findViewById(R.id.userName);

        if (user == null) {
            user = new User();
        }

        assert userName != null;
        if (user.getCommands().size() == 4 && recordedName && userName.getText().length() > 0) {
            user.setName(userName.getText().toString());
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = settings.edit();
            Gson gson = new Gson();
            users.add(user.getName());
            editor.putString("users", gson.toJson(users));
            editor.putString(user.getName(), gson.toJson(user.getAudioName()));
            editor.putString(user.getName() + "cmd", gson.toJson(user));
            editor.apply();

            Toast.makeText(getApplicationContext(), R.string.correctRegister, Toast.LENGTH_SHORT).show();
            user = new User();
            recordedName = false;
            showRegisterForm(View.INVISIBLE);
        } else
            Toast.makeText(getApplicationContext(), R.string.registerInstr, Toast.LENGTH_SHORT).show();
    }

    private void showRegisterForm(int state) {
        TextView lbName = (TextView) findViewById(R.id.labelName);
        assert lbName != null;
        lbName.setVisibility(state);
        EditText userName = (EditText) findViewById(R.id.userName);
        assert userName != null;
        userName.setVisibility(state);
        TableLayout recordButtons = (TableLayout) findViewById(R.id.commandsLayout);
        assert recordButtons != null;
        recordButtons.setVisibility(state);
        Button mRecordNameButton = (Button) findViewById(R.id.recordName);
        assert mRecordNameButton != null;
        mRecordNameButton.setVisibility(state);
    }

    /**
     * Record commands to register user
     *
     * @param view: Android view
     */
    public void recordCommands(View view) {
        String cmd = view.getTag().toString();
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

        FFT cmdFFT = new FFT(audioInput.stopRecording());
        cmdFFT = FFT.ditfft2(cmdFFT);
        user.setCommand(cmd, cmdFFT.magnitude());
        Toast.makeText(getApplicationContext(), R.string.finishRecording, Toast.LENGTH_SHORT).show();
    }


    /**
     * Record name to register user
     *
     * @param view: Android view
     */
    public void recordName(View view) {

        recordedName = true;

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

        FFT nameFFT = new FFT(audioInput.stopRecording());
        nameFFT = FFT.ditfft2(nameFFT);
        user.setAudioName(nameFFT.magnitude());
        Toast.makeText(getApplicationContext(), R.string.finishRecording, Toast.LENGTH_SHORT).show();
    }


    /**
     * Attempts to sign in
     * If there are errors, no actual login attempt is made.
     *
     * @param view: Android view
     */
    public void attemptLogin(View view) {

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

        FFT nameFFT = new FFT(audioInput.stopRecording());
        nameFFT = FFT.ditfft2(nameFFT);
        double[] nameFFTMagnitude = nameFFT.magnitude();
        boolean logIn = false;

        double max = -1;
        String similarUser = "";

        for (String user : users) {

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            if (settings.contains(user)) {
                String jsonUser = settings.getString(user, null);
                Gson gson = new Gson();
                double[] audioUser = new double[20];
                audioUser = gson.fromJson(jsonUser, audioUser.getClass());

                double result = SignalProcessing.jaccardCoefficient(nameFFTMagnitude, audioUser);
                if (result > max) {
                    logIn = true;
                    max = result;
                    similarUser = user;
                }
            }
        }
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        settings.edit().putString("user", similarUser).apply();

        if (logIn) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else
            Toast.makeText(getApplicationContext(), R.string.error_sign_in, Toast.LENGTH_SHORT).show();

    }

}



