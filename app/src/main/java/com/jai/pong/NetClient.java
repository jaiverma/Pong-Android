package com.jai.pong;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class NetClient extends Thread {
    private Paddle paddle;
    private Paddle paddleMod;
    private DatagramSocket socket;
    private InetAddress ipAddress;
    private int port;
    private float lastHeight;

    public NetClient(Paddle paddle, Paddle paddleMod, String ipAddress, int port) {
        this.paddle = paddle;
        this.paddleMod = paddleMod;
        this.lastHeight = 0;
        try {
            this.ipAddress = InetAddress.getByName(ipAddress);
            this.port = port;
            this.socket = new DatagramSocket();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            if (paddle.height == null)
                continue;
            // send our height
            byte[] data = ByteBuffer.allocate(Float.BYTES).putFloat(paddle.height).array();
//            Log.i("hathi-zzz", "Sending data " + data.toString());
            sendData(data);
            DatagramPacket packet = new DatagramPacket(data, data.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // receive server's height and set it to other player's height
            byte[] recv = packet.getData();
            byte[] height_b = new byte[Float.BYTES];
            System.arraycopy(recv, 0, height_b, 0, Float.BYTES);
            float height = ByteBuffer.wrap(height_b).getFloat();
            paddleMod.move(height);
//            Log.i("hathi-data-client", String.valueOf(height));
        }
    }

    public void sendData(byte[] data) {
        DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
