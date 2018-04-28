package controller;

import com.sun.org.apache.xpath.internal.operations.Mod;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import model.*;
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
                if (message == null){
                    isWork = false;
                    Server.getInstance().deleteUser(this);
                }
                if (message != null && !message.equals("")) {

                    String login = checkNewUser(message);
                    if (!login.equals("")){
                        Server.getInstance().setUser(login, this);
                //        Server.getInstance().sendToChat(Long.parseLong("0"),XmlConfiguration.getInstance().configuration(message),this);
                    }
                    String response = XmlConfiguration.getInstance().configuration(message);
                    System.out.println(message);
                    if(response.contains("NOTACCEPTED")) {
                        Server.getInstance().deleteUser(this);
                    }
                    send(response);
                    ModelImpl.getInstance().save();
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
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
                send("<test></test>");
                String message = reader.readLine(); //just try
                if (message == null){
                    isWork = false;
                    Server.getInstance().deleteUser(this);
                }
                return true;
            } catch (Exception e) {
                stopConnection();
               return false;
            }
    }

    /**
     * stops current connection
     */
    public void stopConnection() {
        isWork = false;
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
                break;
            case "registration":
                if(!ModelImpl.getInstance().existUser(element.getAttribute("login"))){
                    return element.getAttribute("login");
                }
                break;
            case "login" :
                return element.getAttribute("login");
            case "addMessage":
            case "addToChat":
                long id = Long.parseLong(element.getAttribute("chat_id"));
                Server.getInstance().sendToChat(id,command, this);
                break;
            case "setOnlineStatus":
                boolean isOnline = Boolean.parseBoolean(element.getAttribute("isOnline"));
                ModelImpl.getInstance().setOnlineStatus(element.getAttribute("user"), isOnline);
                Server.getInstance().sendToChat(Long.parseLong("0"),command, this);
            case "newChatID":/*дальше по имплементации*/
            default :
                break;
        }
        return "";
    }
    public String configuration(String command) {
        String type = ModelImpl.getInstance().getTypeOfTheCommand(command);
        switch (type) {
            case "all_users": {
                return XmlConfiguration.getInstance().listUserToXml(ModelImpl.getInstance().getListUsers());
            }
            case "online_users": {
                return  XmlConfiguration.getInstance().listUserToXml(ModelImpl.getInstance().getOnlineListUsers());
            }
            case "chats": {
                String login = ModelImpl.getInstance().getSender(command);
                return XmlConfiguration.getInstance().getChats(login);
            }
            case "get_messages": {
                long id = ModelImpl.getInstance().getChatId(command);
                return XmlConfiguration.getInstance().getMessages(id);
            }
            case "get_chat_users": {
                long id = ModelImpl.getInstance().getChatId(command);
                return XmlConfiguration.getInstance().listUserToXml(ModelImpl.getInstance().getChatUsers(id));
            }
            case "ban":
            case "unban":
            {
                String login = ModelImpl.getInstance().getUserFromMessage(command);
                if(type.equals("ban")) {
                    ModelImpl.getInstance().ban(login);
                }else{
                    ModelImpl.getInstance().unban(login);
                }
                return ModelImpl.getInstance().result(type, login, true);
            }
            case "login" : {
                String login = ModelImpl.getInstance().getLogin(command);
                String password = ModelImpl.getInstance().getPass(command);
                if(ModelImpl.getInstance().login(new User(login, password, ""))){
                    String name = ModelImpl.getInstance().getUserName(login);
                    return ModelImpl.getInstance().resultForLoginRegister(type,name, ModelImpl.getInstance().isAdmin(login), ModelImpl.getInstance().isInBan(login), true);
                } else {
                    return ModelImpl.getInstance().resultForLoginRegister(type, null,false,false,false);

                }
            }
            case "registration": {
                String login = ModelImpl.getInstance().getLogin(command);
                String password = ModelImpl.getInstance().getPass(command);
                String name = ModelImpl.getInstance().getNameCommand(command);
                if(!ModelImpl.getInstance().register(new User(login, password, name))) {
                    return "<command type=\"registration\" result =\"NOTACCEPTED\" />";
                } else {
                    return String.format("<command type=\"registration\" name=\"%s\" result=\"ACCEPTED\" />", name);
                }
            }
            case "newChatID": {
                String login = ModelImpl.getInstance().getSender(command);
                long id = ModelImpl.getInstance().createChat();
                ModelImpl.getInstance().addToChat(login, id);
                return String.format("<command type=\"newChatID\" chat_id=\"%s\" user = \"%s\" />", id, login);
            }
            case "addToChat": {
                String login = ModelImpl.getInstance().getLogin(command);
                long id = ModelImpl.getInstance().getChatId(command);
                ModelImpl.getInstance().addToChat(login, id);
                return command;
            }
            case "addMessage": {
                String login = ModelImpl.getInstance().getSender(command);
                long id = ModelImpl.getInstance().getChatId(command);
                String text = ModelImpl.getInstance().getTextAttr(command);
                ModelImpl.getInstance().addMessage(id, new Message(login, text));
                return command;
            }
            case "setOnlineStatus": {
                boolean online = ModelImpl.getInstance().getOnlineStatus(command);
                String login = ModelImpl.getInstance().getUserFromMessage(command);
                ModelImpl.getInstance().setOnlineStatus(login, online);
                return command;
            }
            case "createAdmin": {
                String login = ModelImpl.getInstance().getUserFromMessage(command);
                ModelImpl.getInstance().createAdmin(login);
                return command;
            }
            case "deleteAdmin": {
                String login = ModelImpl.getInstance().getUserFromMessage(command);
                ModelImpl.getInstance().deleteAdmin(login);
                return command;
            }
            case "isInBan": {
                String login = ModelImpl.getInstance().getUserFromMessage(command);
                ModelImpl.getInstance().deleteAdmin(login);
                return String.format("<command type=\"isInBan\" isInBan=\"%s\" />", ModelImpl.getInstance().isInBan(login)) ;
            }
            case "getUserName": {
                String login = ModelImpl.getInstance().getUserFromMessage(command);
                return String.format("<command type=\"getUserName\" name=\"%s\" />", ModelImpl.getInstance().getUserName(login)) ;
            }
            default: {
                logger.warn("Command not found " + command);
                return command;
            }

        }
    }
}

