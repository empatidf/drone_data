package com.dji.uxsdkdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.dji.uxsdkdemo.model.LocationModel;

public class UDPActivity extends AppCompatActivity {

    private String targetIp = "192.168.137.1";
    private int port = 14550;
    private int timeInterval = 100;

    private Handler customHandler;

    UdpClientHandler udpClientHandler;
    UdpClientThread udpClientThread;

    LocationModel locationModel;

    EditText editTextIP;
    EditText editTextPort;
    public static TextView textViewStatus;
    Button buttonConnect;
    Button resetConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        udpClientHandler = new UdpClientHandler(this);
        setContentView(R.layout.second_activity);

        Intent intent = getIntent();
        locationModel = intent.getParcelableExtra("dji"); //if it's a string you stored.

        customHandler = new android.os.Handler();

        editTextIP = findViewById(R.id.edit_text_udp_ip);
        editTextPort = findViewById(R.id.edit_text_udp_port);
        textViewStatus = findViewById(R.id.text_view_udp_status);
        buttonConnect = findViewById(R.id.button_udp_connect);
        resetConnect = findViewById(R.id.button_udp_reset);

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customHandler.postDelayed(updateTimerThread, timeInterval);
            }
        });

        resetConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textViewStatus.setText("");
                customHandler.removeCallbacks(updateTimerThread);
            }
        });
    }

    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            sendDataToUDP();
            customHandler.postDelayed(this, timeInterval);
        }
    };

    private void sendDataToUDP() {

        udpClientThread = new UdpClientThread(
                editTextIP.getText().toString(),
                Integer.parseInt(editTextPort.getText().toString()),
                udpClientHandler,
                locationModel
        );
        udpClientThread.start();
    }

    private void clientEnd(){
        udpClientThread = null;
    }

    public static class UdpClientHandler extends Handler {
        public static final int UPDATE_STATE = 0;
        public static final int UPDATE_MSG = 1;
        public static final int UPDATE_END = 2;
        private UDPActivity parent;

        public UdpClientHandler(UDPActivity parent) {
            super();
            this.parent = parent;
        }

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case UPDATE_STATE:
                    textViewStatus.setText(msg.obj.toString());
                    break;
                case UPDATE_MSG:
                    break;
                case UPDATE_END:
                    parent.clientEnd();
                    break;
                default:
                    super.handleMessage(msg);
            }

        }
    }

}


