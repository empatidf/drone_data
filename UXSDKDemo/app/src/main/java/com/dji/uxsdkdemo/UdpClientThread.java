package com.dji.uxsdkdemo;

import android.os.Message;

import com.dji.uxsdkdemo.model.LocationModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import dji.common.flightcontroller.FlightControllerState;

public class UdpClientThread extends Thread{

    String dstAddress;
    int dstPort;
    private boolean running;
    UDPActivity.UdpClientHandler handler;
    LocationModel locationModel;

    DatagramSocket socket;
    InetAddress address;

    public UdpClientThread(String addr, int port, UDPActivity.UdpClientHandler handler, LocationModel locationModel) {
        super();
        dstAddress = addr;
        dstPort = port;
        this.handler = handler;
        this.locationModel = locationModel;
    }

    public void setRunning(boolean running){
        this.running = running;
    }

    private void sendState(String state){
        handler.sendMessage(
                Message.obtain(handler,
                        UDPActivity.UdpClientHandler.UPDATE_STATE, state));
    }

    @Override
    public void run() {
        running = true;

        try {
            socket = new DatagramSocket();
            address = InetAddress.getByName(dstAddress);

            byte[] sendData    = new byte[1024];
            byte[] receiveData = new byte[1024];

            String latitudeText = String.valueOf(locationModel.getLatitude());
            String longitudeText = String.valueOf(locationModel.getLongitude());
            String altitudeText = String.valueOf(locationModel.getAltitude());

            JSONObject json = new JSONObject();
            json.put("latitude", latitudeText);
            json.put("longitude", longitudeText);
            json.put("altitude", altitudeText);

            sendData = json.toString().getBytes();

            DatagramPacket packet =
                    new DatagramPacket(sendData, sendData.length, address, dstPort);
            socket.send(packet);

            sendState("connected");

            // get response
            packet = new DatagramPacket(sendData, sendData.length);

            socket.receive(packet);
            String line = new String(packet.getData(), 0, packet.getLength());

            handler.sendMessage(
                    Message.obtain(handler, UDPActivity.UdpClientHandler.UPDATE_MSG, line));

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if(socket != null){
                socket.close();
                handler.sendEmptyMessage(UDPActivity.UdpClientHandler.UPDATE_END);
            }
        }

    }
}
