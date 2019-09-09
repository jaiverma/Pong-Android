package com.jai.pong;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class NetServer extends Thread {
    private Paddle paddle;
    private Paddle paddleMod;
    private DatagramSocket socket;
    private int port;
    private float lastHeight;

    public NetServer(Paddle paddle, Paddle paddleMod, int port) {
        this.paddle = paddle;
        this.paddleMod = paddleMod;
        this.lastHeight = 0;
        try {
            this.port = port;
            this.socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            if (paddle.height == null) {
//                Log.i("hathi-data-server", "still null");
                continue;
            }
            byte[] data = new byte[Float.BYTES];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // receive height from client and set it to other player's height
            float height = ByteBuffer.wrap(packet.getData()).getFloat();
            paddleMod.move(height);
            // send our height
            sendData(ByteBuffer.allocate(Float.BYTES).putFloat(paddle.height).array(),
                    packet.getAddress(),
                    packet.getPort());
//            Log.i("hathi-data-server", String.valueOf(height));
        }
    }

    public void sendData(byte[] data, InetAddress ipAddress, int port) {
        DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
