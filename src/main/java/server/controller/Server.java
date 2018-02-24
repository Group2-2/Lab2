package server.controller;

import server.model.User;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Hashtable;
import java.util.Map;

public class Server implements Runnable {
    private ServerSocket serverSocket;
    private int port;

    private Map<User, Connection> users;
    private static Server instance = new Server(12345);

    private Server(int port) {
        users = new Hashtable<User, Connection>();
        this.port = port;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {

    }

    public int getPort() {
        return port;
    }

    public static Server getInstance() {
        return instance;
    }

    public Map<User, Connection> getUsers() {
        return users;
    }
}
