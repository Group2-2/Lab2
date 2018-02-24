package client.controller;

import client.view.*;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


public class ClientController extends Thread {
    private static final Logger logger = Logger.getLogger(ClientController.class);
    private int PORT = 12345;
    private String hostName = "localhost";
    private Socket connect;
    private List<String> onlineUsers = new ArrayList<>();
    private InputStream in;
    private OutputStream out;
    private List<String> banUsers;

    private boolean isConnected;


    public ClientController() {
        LoginView LoginView = new LoginView(this);
        GeneralChatView GeneralChatView = new GeneralChatView(this, "Main chat");
        PrivateChatView PrivateChatView = new PrivateChatView(this);
        AdminView AdminView = new AdminView(this);
        OnlineUsersView OnlineUsersView = new OnlineUsersView(this, "Users");
    }

    public boolean validateUser(String nickName, String password){
        System.out.println("User validation "+" "+ nickName +" "+ password);
        return true;
    }

    public boolean newUserRegistration(String nickName, String password){
        System.out.println("User reg "+" "+ nickName +" "+ password);
        return true;
    }


    public boolean connectToServer() {
        try {
            connect = new Socket(hostName, PORT);
            logger.info("Connected: " + connect);

        } catch (UnknownHostException e) {
            logger.error("Host unknown: ",e);
            return false;
        } catch (IOException e) {

            return false;
        }

        return true;

    }

    public void exitChat() {


    }

    public synchronized void  getMessages() {

    }


    public void sendMessage(String xml, String message) {

    }

    public boolean banUser(String banUser) {
return true;
    }

    public boolean unBanUser(String unBanUser) {
        return true;
    }



    @Override
    public void run() {
        //while (true) {

        //}
    }


    public void createLoginView() {

    }

    public static void main(String[] args) throws IOException, SAXException {
        ClientController client = new ClientController();
        client.createLoginView();
        client.run();
    }
}

