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
    /**
     * login - current connection of online users
     */
    private Map<String, Connection> users; //login/connection
    private static Server instance = new Server(12345);

    /**
     * initializes port, map, ServerSocket and started new Thread with checkOnline method
     * @see server.controller.Server#checkOnline
     * @param port
     */

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

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public Map<String, Connection> getUsers() {
        return users;
    }

    public static Server getInstance() {
        return instance;
    }

    @Override
    public void setUser(String login, Connection connection){
        ModelImpl.getInstance().save();
        users.put(login, connection);
    }

    @Override
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

    public static void main(String[] args){
            new Thread(instance).start();
    }

    @Override
    public void sendToChat(Long chatId, String text, Connection current){
        List list = ModelImpl.getInstance().getChatUsers(chatId);
        users.forEach((login, connection) -> {
            if(list.contains(login) && connection != current) {
                connection.send(text);
            }
        } );
        ModelImpl.getInstance().save();
    }

    /**
     * method for thread, every SomePeriodOfTIme checks all users, was connection crush or no
     */
    private void checkOnline(){
        while(!Thread.interrupted()){
            users.forEach((login, connection) -> {
                if(!connection.checkConnection()){
                    users.remove(login);
                    ModelImpl.getInstance().setOnlineStatus(login, false);
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
