package unal.informacion.teoria.recorder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;
import io.socket.client.Socket;
import io.socket.client.IO;
import io.socket.emitter.Emitter;


import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    private Socket socket;

    /**
     * Activate test connection button
     *
     * @param view: Android view
     */
    public void testConnection(View view) {

        EditText input = (EditText) findViewById(R.id.inputRobotIP);

        try {
            socket = IO.socket("http://" + input.getText()+":81");
        } catch (URISyntaxException e) {
            Toast.makeText(getApplicationContext(), "URI error", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
        }
		
		socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

			@Override
			public void call(Object... args) {
				 Toast.makeText(getApplicationContext(), "Correct connection", Toast.LENGTH_SHORT).show();
			}

		}).on(Socket.EVENT_CONNECT_ERROR , new Emitter.Listener() {

			@Override
			public void call(Object... args) {
				Toast.makeText(getApplicationContext(), "Sorry, we're unable to connect to the robot", Toast.LENGTH_SHORT).show();
			}

		});
		
		socket.connect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
