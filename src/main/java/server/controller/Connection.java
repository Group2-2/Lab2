package server.controller;

import server.model.User;
import java.io.*;
import java.net.*;

public class Connection implements Runnable {

    private Socket socket;
    private PrintWriter writer;
    private User user = null;
    private boolean isWork = true;

    public Connection(Socket socket) {
        this.socket = socket;
        try {
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(this::checkOnline).start();
    }

    public void run() {
        while (isWork) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void send(String message) {
        writer.println(message);
    }

    private void checkOnline() {
        while (isWork) {
            try {
                send("<test></test>");
            } catch (Exception e) {
                if (user != null)
                    Server.getInstance().getUsers().remove(user);
                isWork = false;
            }
        }
    }
}
