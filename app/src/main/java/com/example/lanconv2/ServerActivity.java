package com.example.lanconv2;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerActivity extends AppCompatActivity {
    private static final String TAG = "ServerActivity";
    private TextView textViewChat;
    private EditText editTextMessage;
    private EditText editTextServerName;
    private Button buttonSend;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private String serverName;
    private String clientName;
    TextView serverIP;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        textViewChat = findViewById(R.id.textViewChat);
        editTextMessage = findViewById(R.id.editTextMessage);
        editTextServerName = findViewById(R.id.editTextServerName);
        buttonSend = findViewById(R.id.buttonSend);
        serverIP = findViewById(R.id.serverIP);

        Bundle bundle = getIntent().getExtras();
        String value1 = bundle.getString("tag1");
        serverIP.setText("Your Server IP: "+value1);

        new Thread(new ServerThread()).start();

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editTextMessage.getText().toString();
                if (!message.isEmpty()) {
                    new SendMessageTask().execute(message);
                }else{
                    Toast.makeText(ServerActivity.this,"Something went wrong!",Toast.LENGTH_SHORT).show();
                    editTextMessage.setText("");
                }
                editTextMessage.setText("");
            }
        });
    }

    private class ServerThread implements Runnable {
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(22222);
                Log.d(TAG, "Server Started...");
                runOnUiThread(() -> textViewChat.append("Server Started...\n"));

                clientSocket = serverSocket.accept();
                Log.d(TAG, "Client Connected...");
                runOnUiThread(() -> textViewChat.append("Client Connected...\n"));

                objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                objectInputStream = new ObjectInputStream(clientSocket.getInputStream());

                serverName = editTextServerName.getText().toString();
                if (serverName.isEmpty()) {
                    serverName = "Server";
                }
                objectOutputStream.writeObject(serverName);

                clientName = (String) objectInputStream.readObject();
                runOnUiThread(() -> textViewChat.append(clientName + " Connected...\n"));

                new Thread(new ReceiveMessageTask()).start();
            } catch (IOException | ClassNotFoundException e) {
                Log.e(TAG, "Error in ServerThread", e);
            }
        }
    }

    private class ReceiveMessageTask implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    String message = (String) objectInputStream.readObject();
                    runOnUiThread(() -> textViewChat.append(clientName + ": " + message + "\n"));
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
                    runOnUiThread(() -> textViewChat.append(serverName + ": " + messages[0] + "\n"));
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