package controller;

import org.apache.log4j.Logger;
import model.*;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Class wraps current connection
 * Fully communicates with the user, receives the message and sends back
 */
    class Connection implements Runnable {

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
            logger.warn("connectionNewInstance", e);
        }
    }

    @Override
    public void run() {
        while (isWork) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message = reader.readLine();
                if (message == null){
                    stopConnection();
                    Server.getInstance().deleteUser(this);
                }
                if (message != null && !message.equals("")) {
                    String response = configuration(message);
                    System.out.println(message);
                    send(response);
                    ModelImpl.getInstance().save();
                }
            } catch (IOException e) {
                stopConnection();
                logger.warn("readlineEx from user, while thread running",e);
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
            logger.warn("Close in thread", e);
        }
        writer.close();
    }

    /**
     * send message ro this user
     * @param message - sended message
     */
    public void send(String message) {
        writer.flush();
        writer.println(message);
        writer.flush();
        System.out.println(message);
    }

    /**
     * Method to check the connection
     * @return false if connection crushed and stopped the thread
     */
    public boolean checkConnection() {
        if (!isWork) {
            Server.getInstance().deleteUser(this);
            return true;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            logger.warn("ex in BufferedReader(new InputStreamReader(socket.getInputStream()))", e);
        }
        try {
          send("<test></test>");
          String message = reader.readLine(); //just try
            if (message == null) {
                isWork = false;
                Server.getInstance().deleteUser(this);
            }
            return true;
        } catch (NullPointerException | IOException e) {
            stopConnection();
        }
        try {
            reader.close();
        } catch (IOException | NullPointerException e ) {
            logger.debug(e);
        }
        return false;
    }

    /**
     * stops current connection
     */
    public void stopConnection() {
        isWork = false;
    }
   /* /**
     * check login and register, check chat_id for newMessage and chatConfiguration
     * @param command - accepted command
     * @return login if user login or register
     */
  /*  private String checkNewUser(String command) {
        Document document = XmlConfiguration.getInstance().newDocument(command);
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
                break;
            default :
                break;
        }
        return "";
    }
    */

    /**
     * Methot that get user command and correctly processes:
     * manages user profiles, adds connections to the map,
     * sends the necessary commands to the required chats
     * @param command - accepted command
     * @return string, that sends to user back
     */
    private String configuration(String command) {
        Map<String, Object> map;
        String type = XmlConfiguration.getTypeOfTheCommand(command);
        switch (type) {
            case "all_users": {
                return XmlConfiguration.listUserToXml(ModelImpl.getInstance().getListUsers(), "users");
            }
            case "online_users": {
                return  XmlConfiguration.listUserToXml(ModelImpl.getInstance().getOnlineListUsers(), "onlineUsers");
            }
            case "chats": {
                String login = XmlConfiguration.getInstance().getSender(command);
                return XmlConfiguration.getChats(login);
            }
            case "get_messages": {
                long id = XmlConfiguration.getInstance().getChatId(command);
                return XmlConfiguration.getMessages(id);
            }
            case "get_chat_users": {
                long id = XmlConfiguration.getInstance().getChatId(command);
                return XmlConfiguration.listUserToXml(ModelImpl.getInstance().getChatUsers(id), "users");
            }
            case "ban": {
                Server.getInstance().sendToChat(Long.parseLong("0"), command, this);
                String login = XmlConfiguration.getInstance().getUserFromMessage(command);
                ModelImpl.getInstance().ban(login);
                map = new HashMap<>();
                map.put("login", login);
                map.put("result", "ACCEPTED");
                return XmlConfiguration.getInstance().command(type, map);
            }
            case "unban": {
                Server.getInstance().sendToChat(Long.parseLong("0"), command, this);
                String login = XmlConfiguration.getInstance().getUserFromMessage(command);
                ModelImpl.getInstance().unban(login);
                map = new HashMap<>();
                map.put("login", login);
                map.put("result", "ACCEPTED");
                return XmlConfiguration.getInstance().command(type, map);
            }
            case "login" : {
                String login = XmlConfiguration.getInstance().getLogin(command);
                String password = XmlConfiguration.getInstance().getPassword(command);
                map = new HashMap<>();
                if (ModelImpl.getInstance().login(new User(login, password, ""))) {
                    map.put("isAdmin", ModelImpl.getInstance().isAdmin(login));
                    map.put("isInBan", ModelImpl.getInstance().isInBan(login));
                    map.put("result", "ACCEPTED");
                    Server.getInstance().setUser(login, this);
                } else {
                    map.put("result", "NOTACCEPTED");
                }
                return XmlConfiguration.getInstance().command(type, map);
            }
            case "registration": {
                String login = XmlConfiguration.getInstance().getLogin(command);
                String password = XmlConfiguration.getInstance().getPassword(command);
                map = new HashMap<>();
                if(!ModelImpl.getInstance().register(new User(login, password))) {
                    map.put("result", "NOTACCEPTED");
                } else {
                    map.put("result", "ACCEPTED");
                    Server.getInstance().setUser(login, this);
                }
                return XmlConfiguration.getInstance().command(type,map);
            }
            case "newChatID": {
                String login = XmlConfiguration.getInstance().getSender(command);
                long id = ModelImpl.getInstance().createChat();
                ModelImpl.getInstance().addToChat(login, id);
                map = new HashMap<>();
                map.put("chat_id", id);
                map.put("user", login);
                return XmlConfiguration.getInstance().command(type,map);
            }
            case "addToChat": {
                String login = XmlConfiguration.getInstance().getLogin(command);
                long id = XmlConfiguration.getInstance().getChatId(command);
                ModelImpl.getInstance().addToChat(login, id);
                Server.getInstance().sendToChat(id,command, this);
                return command;
            }

            case "addMessage": {
                String login = XmlConfiguration.getInstance().getSender(command);
                long id = XmlConfiguration.getInstance().getChatId(command);
                String text = XmlConfiguration.getInstance().getText(command);
                ModelImpl.getInstance().addMessage(id, new Message(login, text));
                return command;
            }
            case "setOnlineStatus": {
                boolean online = XmlConfiguration.getInstance().getOnlineStatus(command);
                String login = XmlConfiguration.getInstance().getUserFromMessage(command);
                ModelImpl.getInstance().setOnlineStatus(login, online);
                Server.getInstance().sendToChat(Long.parseLong("0"),command, this);
                return command;
            }
            case "createAdmin": {
                String login = XmlConfiguration.getInstance().getUserFromMessage(command);
                ModelImpl.getInstance().createAdmin(login);
                return command;
            }
            case "deleteAdmin": {
                String login = XmlConfiguration.getInstance().getUserFromMessage(command);
                ModelImpl.getInstance().deleteAdmin(login);
                return command;
            }
            case "isInBan": {
                String login = XmlConfiguration.getInstance().getUserFromMessage(command);
                ModelImpl.getInstance().deleteAdmin(login);
                map = new HashMap<>();
                map.put("isInBan", ModelImpl.getInstance().isInBan(login));
                return XmlConfiguration.getInstance().command(type,map);
            }
            case "getUserName": {
                String login = XmlConfiguration.getInstance().getUserFromMessage(command);
                map = new HashMap<>();
                map.put("name", ModelImpl.getInstance().getUserName(login));
                return XmlConfiguration.getInstance().command(type,map);
            }
            case "getBanList":
                return XmlConfiguration.listUserToXml(ModelImpl.getInstance().getBanList(), "banList");
            case "deleteUser":
                String login = XmlConfiguration.getInstance().getLogin(command);
                map = new HashMap<>();
                ModelImpl.getInstance().deleteUser(login);
                map.put("result", "ACCEPTED");
                return XmlConfiguration.getInstance().command(type, map);
            case "changePassword":
                String log = XmlConfiguration.getInstance().getLogin(command);
                String pass = XmlConfiguration.getInstance().getPassword(command);
                map = new HashMap<>();
                map.put("result", "ACCEPTED");
                ModelImpl.getInstance().changePassword(log,pass);
                return XmlConfiguration.getInstance().command(type, map);
            default:
                logger.warn("Command not found " + command);
                return command;
        }

    }
}

