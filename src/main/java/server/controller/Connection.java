package server.controller;

import java.net.Socket;

public class Connection implements Runnable {

    private Socket socket;

    public Connection(Socket socket) {
        this.socket = socket;
    }

    public void run() {

    }

    public void send(String message) {

    }
}
