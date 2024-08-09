package com.example.lanconv2;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button createServer,enterServer;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createServer = findViewById(R.id.createServer);
        enterServer = findViewById(R.id.enterServer);

        WifiManager ip = (WifiManager)      getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = ip.getConnectionInfo();
        int cIp = wifiInfo.getIpAddress();
        String cIP = Formatter.formatIpAddress(cIp);

        createServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ServerActivity.class);
                intent.putExtra("tag1",cIP);
                startActivity(intent);
            }
        });
        enterServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ClientActivity.class);
                intent.putExtra("tag1",cIP);
                startActivity(intent);
            }
        });


    }

}