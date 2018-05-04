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

    private XmlConfiguration xml = XmlConfiguration.getInstance();
    private ModelImpl model = ModelImpl.getInstance();
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
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (isWork) {
                String message = reader.readLine();
                if (message == null) {
                    stopConnection();
                    Server.getInstance().deleteUser(this);
                }
                if (message != null && !message.equals("")) {
                    String response = configuration(message);
                    System.out.println(message);
                    send(response);
                    model.save();
                }
            }
        } catch (IOException e) {
            stopConnection();
            logger.warn("readlineEx from user, while thread running",e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                logger.debug(e);
            }
        }
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
        boolean choice = false;
        if (!isWork) {
            Server.getInstance().deleteUser(this);
            return true;
        }
       // BufferedReader reader = null;
        try {
        /*    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));*/
            send("<test></test>");
         /*   String message = reader.readLine(); //just try
            if (message == null) {
                isWork = false;
                Server.getInstance().deleteUser(this);
                 choice = true;
            }*/

        } catch (NullPointerException /*| IOException*/ e) {
            stopConnection();
            Server.getInstance().deleteUser(this);
            choice = true;
        }/* finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                logger.debug(e);
            }
        }*/
        return choice;
    }

    /**
     * stops current connection
     */
    public void stopConnection() {
        isWork = false;
        try {
            socket.close();
        } catch (IOException e) {
            logger.warn("Close in thread", e);
        }
        writer.close();
    }
   /* /**
     * check login and register, check chat_id for newMessage and chatConfiguration
     * @param command - accepted command
     * @return login if user login or register
     */
  /*  private String checkNewUser(String command) {
        Document document = xml.newDocument(command);
        NodeList nodes = document.getElementsByTagName("command");
        Element element = (Element) nodes.item(0);
        String type = element.getAttribute("type");
        switch (type) {
            case "ban":
            case "unban":
                Server.getInstance().sendToChat(Long.parseLong("0"),command, this);
                break;
            case "registration":
                if(!model.existUser(element.getAttribute("login"))){
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
                model.setOnlineStatus(element.getAttribute("user"), isOnline);
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
                return XmlConfiguration.listUserToXml(model.getListUsers(), "users");
            }
            case "online_users": {
                return  XmlConfiguration.listUserToXml(model.getOnlineListUsers(), "onlineUsers");
            }
            case "chats": {
                String login = xml.getSender(command);
                return XmlConfiguration.getChats(login);
            }
            case "get_messages": {
                long id = xml.getChatId(command);
                return XmlConfiguration.getMessages(id);
            }
            case "get_chat_users": {
                long id = xml.getChatId(command);
                return XmlConfiguration.listUserToXml(model.getChatUsers(id), "users");
            }
            case "ban": {
                Server.getInstance().sendToChat(Long.parseLong("0"), command, this);
                String login = xml.getUserFromMessage(command);
                model.ban(login);
                map = new HashMap<>();
                map.put("login", login);
                map.put("result", "ACCEPTED");
                String s = xml.command(type, map);
                Server.getInstance().sendToChat(Long.parseLong("0"), s , this);
                return s;
            }
            case "unban": {
                Server.getInstance().sendToChat(Long.parseLong("0"), command, this);
                String login = xml.getUserFromMessage(command);
                model.unban(login);
                map = new HashMap<>();
                map.put("login", login);
                map.put("result", "ACCEPTED");
                String s = xml.command(type, map);
                Server.getInstance().sendToChat(Long.parseLong("0"), s , this);
                return s;
            }
            case "login" : {
                String login = xml.getLogin(command);
                String password = xml.getPassword(command);
                map = new HashMap<>();
                if (model.login(new User(login, password, ""))) {
                    map.put("isAdmin", model.isAdmin(login));
                    map.put("isInBan", model.isInBan(login));
                    map.put("result", "ACCEPTED");
                    Server.getInstance().setUser(login, this);
                } else {
                    map.put("result", "NOTACCEPTED");
                }
                return xml.command(type, map);
            }
            case "registration": {
                String login = xml.getLogin(command);
                String password = xml.getPassword(command);
                map = new HashMap<>();
                if(!model.register(new User(login, password))) {
                    map.put("result", "NOTACCEPTED");
                } else {
                    map.put("result", "ACCEPTED");
                    Server.getInstance().setUser(login, this);
                }
                return xml.command(type,map);
            }
            case "newChatID": {
                String login = xml.getSender(command);
                if(model.isInBan(login)) {
                    return command;
                }
                long id = model.createChat();
                model.addToChat(login, id);
                map = new HashMap<>();
                map.put("chat_id", id);
                map.put("user", login);
                return xml.command(type,map);
            }
            case "addToChat": {
                String login = xml.getLogin(command);
                long id = xml.getChatId(command);
                model.addToChat(login, id);
                Server.getInstance().sendToChat(id,command, this);
                return command;
            }

            case "addMessage": {
                String login = xml.getSender(command);
                if(model.isInBan(login)) {
                    return command;
                }
                long id = xml.getChatId(command);
                String text = xml.getText(command);
                model.addMessage(id, new Message(login, text));
                Server.getInstance().sendToChat(id,command, this);
                return command;
            }
            case "setOnlineStatus": {
                boolean online = xml.getOnlineStatus(command);
                String login = xml.getUserFromMessage(command);
                model.setOnlineStatus(login, online);
                Server.getInstance().sendToChat(Long.parseLong("0"),command, this);
                return command;
            }
            case "createAdmin": {
                String login = xml.getUserFromMessage(command);
                model.createAdmin(login);
                return command;
            }
            case "deleteAdmin": {
                String login = xml.getUserFromMessage(command);
                model.deleteAdmin(login);
                return command;
            }
            case "isInBan": {
                String login = xml.getUserFromMessage(command);
                model.deleteAdmin(login);
                map = new HashMap<>();
                map.put("isInBan", model.isInBan(login));
                return xml.command(type,map);
            }
            case "getUserName": {
                String login = xml.getUserFromMessage(command);
                map = new HashMap<>();
                map.put("name", model.getUserName(login));
                return xml.command(type,map);
            }
            case "getBanList":
                return XmlConfiguration.listUserToXml(model.getBanList(), "banList");
            case "deleteUser":
                String login = xml.getLogin(command);
                map = new HashMap<>();
                model.deleteUser(login);
                map.put("result", "ACCEPTED");
                return xml.command(type, map);
            case "changePassword":
                String log = xml.getLogin(command);
                String pass = xml.getPassword(command);
                map = new HashMap<>();
                map.put("result", "ACCEPTED");
                model.changePassword(log,pass);
                return xml.command(type, map);
            default:
                logger.warn("Command not found " + command);
                return command;
        }

    }
}

