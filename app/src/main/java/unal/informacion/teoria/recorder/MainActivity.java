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
    private String serverIP;

    /**
     * Activate test connection button
     *
     * @param view: Android view
     */
    public void testConnection(View view) {

        EditText input = (EditText) findViewById(R.id.inputRobotIP);
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
