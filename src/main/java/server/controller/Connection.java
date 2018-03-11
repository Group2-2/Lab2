package server.controller;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import server.model.*;
import java.io.*;
import java.net.*;

/**
 * Class wraps current connection
 * Fully communicates with the user, receives the message and sends back
 */
public class Connection implements Runnable {

    private static final Logger logger = Logger.getLogger(Connection.class);

    final private Socket socket;
    private PrintWriter writer;
    /**
     * parameter of working this thread, stopped, when it is false
     */
    private boolean isWork = true;

    public Connection(Socket socket) {
        this.socket = socket;
        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
            //writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            logger.warn("connectionNewInstanse", e);
        }
    }

    @Override
    public void run() {
        while (isWork) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message = reader.readLine();
                System.out.println(message);
                if (message != null && !message.equals("")) {
                    String response = XmlConfiguration.getInstance().configuration(message);

                    String login = checkNewUser(message);
                    if(!login.equals("")){
                        Server.getInstance().setUser(login, this);
                //        Server.getInstance().sendToChat(Long.parseLong("0"),XmlConfiguration.getInstance().configuration(message),this);
                    }
                    send(response);
                }
            } catch (IOException e) {
                logger.warn("readlineEx",e);
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
            logger.warn("Close", e);
        }

    }

    /**
     * send message ro this user
     * @param message
     */
    public void send(String message) {
        writer.flush();
        writer.println(message);
        writer.flush();
        System.out.println(message);
    }

    /**
     * @return false if connection crushed and stopped the thread
     */
    public boolean checkConnection() {
            try {
                send("<test></test>");
                return true;
            } catch (Exception e) {
               isWork = false;
               return false;
            }
    }

    /**
     * check login and register, check chat_id for newMessage and chatConfiguration
     * @param command
     * @return login if user login or register
     */
    private String checkNewUser(String command) {
        Document document = XmlConfiguration.newDocument(command);
        NodeList nodes = document.getElementsByTagName("command");
        Element element = (Element) nodes.item(0);
        String type = element.getAttribute("type");
        switch (type) {
            case "ban":
            case "unban":
                Server.getInstance().sendToChat(Long.parseLong("0"),command, this);
                return "";
            case "registration":
            case "login" :
                return element.getAttribute("login");
            case "addMessage":
            case "addToChat":
                long id = Long.parseLong(element.getAttribute("chat_id"));
                Server.getInstance().sendToChat(id,command, this);
                return "";
            case "setOnlineStatus":
                boolean isOnline = Boolean.parseBoolean(element.getAttribute("isOnline"));
                ModelImpl.getInstance().setOnlineStatus(element.getAttribute("user"), isOnline);
                Server.getInstance().sendToChat(Long.parseLong("0"),command, this);
            case "newChatID":/*дальше по имплементации*/
            default :
                return "";
        }
    }
}

