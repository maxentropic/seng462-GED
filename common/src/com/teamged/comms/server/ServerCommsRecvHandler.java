package com.teamged.comms.server;

import com.teamged.comms.ServerMessage;
import com.teamged.comms.internal.CommsManager;
import com.teamged.comms.internal.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by DanielF on 2016-03-18.
 */
public class ServerCommsRecvHandler implements Runnable {
    private final Socket socket;

    public ServerCommsRecvHandler(Socket socket) {
        this.socket = socket;
        System.out.println("Communication listener has connected a receiver to client on port " + socket.getLocalPort());
    }

    @Override
    public void run() {
        String request;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            while (true) {
                request = in.readLine();
                Message msg = Message.fromCommunication(request);
                if (msg != null) {
                    CommsManager.putNextServerRequest(new ServerMessage(msg));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}