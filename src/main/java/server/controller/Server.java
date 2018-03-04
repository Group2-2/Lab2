package server.controller;

import org.apache.log4j.Logger;
import server.model.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class Server implements ServerController {
    private static final Logger logger = Logger.getLogger(Server.class);
    private ServerSocket serverSocket;
    private int port;

    private Map<String, Connection> users; //login/connection
    private static Server instance = new Server(12345);

    private Server(int port) {
        users = new Hashtable<>();
        this.port = port;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            logger.error("Server - getInstance", e);
        }
        new Thread(this::checkOnline).start();
    }


    public int getPort() {
        return port;
    }

    public Map<String, Connection> getUsers() {
        return users;
    }

    public static Server getInstance() {
        return instance;
    }

    public void setUser(String login, Connection connection){
        users.put(login, connection);
    }

    public void run() {
        while (true) {
            final Socket socket;
            try {
                socket = serverSocket.accept();
                final Connection connection = new Connection(socket);
                new Thread(connection).start();
            } catch (IOException e) {
                logger.warn("Connection-socket", e);
            }
        }
    }
    public void sendToChat(Long chatId, String text, Connection current){
        List list = ModelImpl.getInstance().getChatUsers(chatId);
        users.forEach((login, connection) -> {
            if(list.contains(login) && connection != current) {
                connection.send(text);
            }
        } );
    }

    private void checkOnline(){
        while(!Thread.interrupted()){
            users.forEach((login, connection) -> {
                if(!connection.checkConnection()){
                    users.remove(login);
                }
            });
            try {
                Thread.sleep(60000); // 1 minute
            } catch (InterruptedException e) {
                logger.warn("CheckOnline, ", e);
            }
        }
    }

}
