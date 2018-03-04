package server.controller;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import server.model.*;
import java.io.*;
import java.net.*;

public class Connection implements Runnable {

    final private Socket socket;
    private PrintWriter writer;
    private boolean isWork = true;

    public Connection(Socket socket) {
        this.socket = socket;
        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
            //writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (isWork) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message = reader.readLine();
                System.out.println(message);
                if (message != null && !message.equals("")) {
                    String response = XmlConfiguration.getInstance().configuration(message);
                    System.out.println(response);
                    String login = checkNewUser(message);
                    if(!login.equals("")){
                        Server.getInstance().setUser(login, this);
                    }
                    send(response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void send(String message) {
        writer.println(message);
    }

    public boolean checkConnection() {
            try {
                send("<test></test>");
                return true;
            } catch (Exception e) {
                isWork = false;
               return false;
            }
    }
    private String checkNewUser(String command){
        Document document = XmlConfiguration.newDocument(command);
        NodeList nodes = document.getElementsByTagName("command");
        Element element = (Element) nodes.item(0);
        String type = element.getAttribute("type");
        switch (type) {
            case "registration":
            case "login" :
                return element.getAttribute("login");
            case "addMessage":
            case "newChatID":
            case "addToChat":
                long id = Long.parseLong(element.getAttribute("chat_id"));
                Server.getInstance().sendToChat(id,command, this);
                return "";
            default :
                return "";
        }
    }
}

