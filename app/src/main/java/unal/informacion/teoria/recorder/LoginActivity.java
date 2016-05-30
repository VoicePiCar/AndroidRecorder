package unal.informacion.teoria.recorder;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

import static android.Manifest.permission.LOCATION_HARDWARE;
import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via voice.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };

    // UI references.
    private View mProgressView;

    // Record commands
    private boolean recordedCommands = false;
    private boolean recordedName = false;
    private AudioRecorder audioInput;
    private User user = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        audioInput = new AudioRecorder(AudioFormat.ENCODING_PCM_16BIT, 8000);

        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Activate register button
     *
     * @param view: Android view
     */
    public void registerClick(View view) {

        TextView lbName = (TextView) findViewById(R.id.labelName);
        lbName.setVisibility(View.VISIBLE);
        EditText userName = (EditText) findViewById(R.id.userName);
        userName.setVisibility(View.VISIBLE);
        TableLayout recordButtons = (TableLayout) findViewById(R.id.commandsLayout);
        recordButtons.setVisibility(View.VISIBLE);
        Button mRecordNameButton = (Button) findViewById(R.id.recordName);
        mRecordNameButton.setVisibility(View.VISIBLE);

        if (user == null) {
            user = new User();
        }

        if (recordedCommands && recordedName && userName.getText().length() > 0) {
            user.setName(userName.getText().toString());
        } else
            Toast.makeText(getApplicationContext(), R.string.registerInstr, Toast.LENGTH_SHORT).show();
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
        cmdFFT.ditfft2(cmdFFT);
        user.setCommand(cmd, cmdFFT.magnitude());
        Toast.makeText(getApplicationContext(), R.string.finishRecording, Toast.LENGTH_SHORT).show();
    }


    /**
     * Record name to register user
     *
     * @param view: Android view
     */
    public void recordName(View view) {

        Thread recordThread = new Thread(new Runnable() {

            @Override
            public void run() {
                audioInput.startRecording();
            }
        });
        recordThread.start();

        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        FFT nameFFT = new FFT(audioInput.stopRecording());
        nameFFT.ditfft2(nameFFT);
        user.setAudioName(nameFFT.magnitude());
        Toast.makeText(getApplicationContext(), R.string.finishRecording, Toast.LENGTH_SHORT).show();
    }


    /**
     * Attempts to sign in
     * If there are errors, no actual login attempt is made.
     */
    private void attemptLogin() {

        boolean cancel = false;
        View focusView = null;


        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            //mAuthTask = new UserLoginTask(email, password);
            // mAuthTask.execute((Void) null);
        }
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}



