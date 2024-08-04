package com.example.lanconv2;

import androidx.appcompat.app.AppCompatActivity;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

import android.os.Bundle;

public class ClientActivity extends AppCompatActivity {

    private static final String TAG = "ClientActivity";
    private TextView textViewChat;
    private EditText editTextMessage;
    private EditText editTextServerIp;
    private EditText editTextClientName;
    private Button buttonSend;
    private Button buttonConnect;
    private Socket socket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private String clientName;
    private String serverName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        textViewChat = findViewById(R.id.textViewChat);
        editTextMessage = findViewById(R.id.editTextMessage);
        editTextClientName = findViewById(R.id.editTextClientName);
        buttonSend = findViewById(R.id.buttonSend);
        editTextServerIp = findViewById(R.id.editTextServerIp);
        buttonConnect = findViewById(R.id.buttonConnect);

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String serverIp = editTextServerIp.getText().toString();
                clientName = editTextClientName.getText().toString();
                if (serverIp.isEmpty() || clientName.isEmpty()) {
                    return;
                }
                new Thread(new ClientThread(serverIp)).start();
            }
        });
        
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editTextMessage.getText().toString();
                if (!message.isEmpty() && socket != null && socket.isConnected()) {
                    new SendMessageTask().execute(message);
                }else{
                    editTextMessage.setText("");
                }
                editTextMessage.setText("");
            }
        });
    }


    private class ClientThread implements Runnable {
        private String serverIp;

        ClientThread(String serverIp) {
            this.serverIp = serverIp;
        }

        @Override
        public void run() {
            try {
                socket = new Socket(serverIp, 22222);
                Log.d(TAG, "Client Connected...");
                runOnUiThread(() -> textViewChat.append("Client Connected...\n"));

                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectInputStream = new ObjectInputStream(socket.getInputStream());

                objectOutputStream.writeObject(clientName);
                serverName = (String) objectInputStream.readObject();
                runOnUiThread(() -> textViewChat.append(serverName + " Connected...\n"));

                new Thread(new ReceiveMessageTask()).start();
            } catch (IOException | ClassNotFoundException e) {
                Log.e(TAG, "Error in ClientThread", e);
                runOnUiThread(() -> textViewChat.append("Failed to connect to server...\n"));
            }
        }
    }

    private class ReceiveMessageTask implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    String message = (String) objectInputStream.readObject();
                    runOnUiThread(() -> textViewChat.append(serverName + ": " + message + "\n"));
                }
            } catch (IOException | ClassNotFoundException e) {
                Log.e(TAG, "Error in ReceiveMessageTask", e);
            }
        }
    }

    private class SendMessageTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... messages) {
            try {
                if (objectOutputStream != null) {
                    objectOutputStream.writeObject(messages[0]);
                    runOnUiThread(() -> textViewChat.append(clientName + ": " + messages[0] + "\n"));
                } else {
                    Log.e(TAG, "ObjectOutputStream is null");
                }
            } catch (IOException e) {
                Log.e(TAG, "Error in SendMessageTask", e);
            }
            return null;
        }
    }
}