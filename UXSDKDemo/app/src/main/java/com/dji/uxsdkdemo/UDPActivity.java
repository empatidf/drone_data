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

import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.sdk.base.BaseProduct;
import dji.sdk.products.Aircraft;

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
    public static TextView textViewLatitude;
    public static TextView textViewLongitude;
    public static TextView textViewAltitude;
    Button buttonConnect;
    Button resetConnect;
    FlightControllerState flightControllerState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        udpClientHandler = new UdpClientHandler(this);
        setContentView(R.layout.second_activity);

        flightControllerState = new FlightControllerState();

        Intent intent = getIntent();
        //locationModel = intent.getParcelableExtra("dji"); //if it's a string you stored.

        customHandler = new android.os.Handler();

        editTextIP = findViewById(R.id.edit_text_udp_ip);
        editTextPort = findViewById(R.id.edit_text_udp_port);
        textViewStatus = findViewById(R.id.text_view_udp_status);
        textViewLatitude = findViewById(R.id.text_view_latitude);
        textViewLongitude = findViewById(R.id.text_view_longitude);
        textViewAltitude = findViewById(R.id.text_view_altitude);
        buttonConnect = findViewById(R.id.button_udp_connect);
        resetConnect = findViewById(R.id.button_udp_reset);

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextPort.getText() == null || editTextPort.getText().toString().isEmpty()) {
                    return;
                }
                customHandler.postDelayed(updateTimerThread, timeInterval);
            }
        });

        resetConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textViewStatus.setText("");
                textViewLatitude.setText("");
                textViewLongitude.setText("");
                textViewAltitude.setText("");
                customHandler.removeCallbacks(updateTimerThread);
            }
        });

//        BaseProduct product = DJISampleApplication.getProductInstance();
//        if (product != null && product.isConnected()) {
//            if (product instanceof Aircraft) {
//                mFlightController = ((Aircraft) product).getFlightController();
//            }
//        }
//
//        if (mFlightController != null) {
//            mFlightController.setStateCallback(new FlightControllerState.Callback() {
//
//                @Override
//                public void onUpdate(FlightControllerState djiFlightControllerCurrentState) {
//                    droneLocationLat = djiFlightControllerCurrentState.getAircraftLocation().getLatitude();
//                    droneLocationLng = djiFlightControllerCurrentState.getAircraftLocation().getLongitude();
//                    droneLocationAlt = djiFlightControllerCurrentState.getAircraftLocation().getAltitude();
//                    updateDroneLocation();
//                }
//            });
//        }
//    }
//
//    private void updateDroneLocation() {
//        textViewLatitude.setText(String.valueOf(droneLocationLat));
//        textViewLongitude.setText(String.valueOf(droneLocationLng));
//        textViewAltitude.setText(String.valueOf(droneLocationAlt));
//
//        udpClientThread = new UDPClientThread(
//                editTextIP.getText().toString(),
//                Integer.parseInt(editTextPort.getText().toString()),
//                udpClientHandler,
//                new LocationModel(
//                        droneLocationLat,
//                        droneLocationLng,
//                        droneLocationAlt
//                )
//        );
//        udpClientThread.start();
    }

    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            sendDataToUDP();
            customHandler.postDelayed(this, timeInterval);
        }
    };

    private void sendDataToUDP() {


        LocationCoordinate3D location3d = flightControllerState.getAircraftLocation();
        LocationModel location = new LocationModel(location3d.getLatitude(), location3d.getLongitude(), location3d.getAltitude());

        textViewLatitude.setText(String.valueOf(location3d.getLatitude()));
        textViewLongitude.setText(String.valueOf(location3d.getLongitude()));
        textViewAltitude.setText(String.valueOf(location3d.getAltitude()));

        udpClientThread = new UdpClientThread(
                editTextIP.getText().toString(),
                Integer.parseInt(editTextPort.getText().toString()),
                udpClientHandler,
                location
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


