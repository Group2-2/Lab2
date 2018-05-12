package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import model.Message;
import model.ModelImpl;
import model.User;
import model.XmlConfiguration;

import org.apache.log4j.Logger;

/**
* Class wraps current connection.
* Fully communicates with the user, receives the message and sends back.
*/
 class Connection implements Runnable {
    /**
     * logger for class.
     */
    private static final Logger logger = Logger.getLogger(Connection.class);
    /**
     * used to getInputStream.
     */
    private final Socket socket;
    /**
     * current writer.
     */
    private PrintWriter writer;
    /**
     * instance for parsing xml.
     */
    private final XmlConfiguration xml = XmlConfiguration.getInstance();
    /**
     * instance for model configuration.
     */
    private final ModelImpl model = ModelImpl.getInstance();
      /**
     * parameter of working this thread, stopped, when it is false.
     */
    private boolean isWork = true;

    /**
     * Constructor. Init writer.
     * @param socket init current socket
     */
    Connection(Socket socket) {
        this.socket = socket;
        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            logger.warn("IOException when in creating Connection in socket:" + socket.toString());
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
                    System.out.println("get: " + message);
                    String response = configuration(message);
                    if(!"".equals(response)) {
                        send(response);
                    }
                    System.out.println("response: " + message);
                    model.save();
                }
            }
        } catch (IOException e) {
            stopConnection();
            logger.warn("socket: " + socket.toString(), e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                logger.debug("IOEX in closing reader, socket^" + socket.toString());
            }
        }
    }

    /**
     * send message ro this user
     * @param message - sent message
     */
    public void send(String message) {
        writer.flush();
        writer.println(message);
        writer.flush();
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
        try {
            send("<test></test>");
        } catch (NullPointerException e) {
            stopConnection();
            Server.getInstance().deleteUser(this);
            choice = true;
        }
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
            logger.warn("IOEx in closing socket(StopConnection meth), socket" + socket.toString());
        }
        writer.close();
    }

    /**
     * Method that get user command and correctly processes:
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
                Server.getInstance().sendToChat(Long.parseLong("0"), s, this);
                Server.getInstance().sendToUser(login, s);
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
                Server.getInstance().sendToChat(Long.parseLong("0"), s, this);
                return s;
            }
            case "login" : {
                String login = xml.getLogin(command);
                String password = xml.getPassword(command);
                map = new HashMap<>();
                if (!Server.getInstance().getUsers().containsKey(login) && model.login(new User(login, password, ""))) {
                    map.put("isAdmin", model.isAdmin(login));
                    map.put("isInBan", model.isInBan(login));
                    map.put("result", "ACCEPTED");
                    Server.getInstance().setUser(login, this);
                } else {
                    map.put("result", "NOTACCEPTED");
                }
              //  System.out.println();
                return xml.command(type, map);
            }
            case "registration": {
                String login = xml.getLogin(command);
                String password = xml.getPassword(command);
                map = new HashMap<>();
                if (!model.register(new User(login, password))) {
                    map.put("result", "NOTACCEPTED");
                } else {
                    map.put("result", "ACCEPTED");
                    Server.getInstance().setUser(login, this);
                }
                return xml.command(type, map);
            }
            case "newChatID": {
                String login = xml.getSender(command);
                if (model.isInBan(login)) {
                    return command;
                }
                long id = model.createChat();
                model.addToChat(login, id);
                map = new HashMap<>();
                map.put("chat_id", id);
                map.put("user", login);
                return xml.command(type, map);
            }
            case "addToChat": {
                String login = xml.getLogin(command);
                long id = xml.getChatId(command);
                model.addToChat(login, id);
                Server.getInstance().sendToChat(id, command, this);
                return command;
            }

            case "addMessage": {
                String login = xml.getSender(command);
                if (model.isInBan(login)) {
                    return command;
                }
                long id = xml.getChatId(command);
                String text = xml.getText(command);
                model.addMessage(id, new Message(login, text));
                Server.getInstance().sendToChat(id, command, this);
                return command;
            }
            case "setOnlineStatus": {
                boolean online = xml.getOnlineStatus(command);
                String login = xml.getUserFromMessage(command);
                model.setOnlineStatus(login, online);
                if (!online) {
                    Server.getInstance().deleteUser(this);
                } else {
                    Server.getInstance().sendToChat(Long.parseLong("0"), command, this);
                }
                return "";
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
                return xml.command(type, map);
            }
            case "getUserName": {
                String login = xml.getUserFromMessage(command);
                map = new HashMap<>();
                map.put("name", model.getUserName(login));
                return xml.command(type, map);
            }
            case "getBanList":
                return XmlConfiguration.listUserToXml(model.getBanList(), "banList");
            case "deleteUser": {
                String login = xml.getLogin(command);
                map = new HashMap<>();
                model.deleteUser(login);
                map.put("login", login);
                map.put("result", "ACCEPTED");
                String result = xml.command(type, map);
                Server.getInstance().sendToChat(Long.parseLong("0"), result, null);
                return "";
            }
            case "changePassword": {
                String log = xml.getLogin(command);
                String pass = xml.getPassword(command);
                map = new HashMap<>();
                map.put("result", "ACCEPTED");
                model.changePassword(log, pass);
                return xml.command(type, map);
            }
            case "leaveChat": {
                Long chatId = xml.getChatId(command);
                String login = xml.getLogin(command);
                model.leaveChat(login, chatId);
                map = new HashMap<>();
                map.put("login", login);
                map.put("chat_id", chatId);
                return "";
            }
            default:
                logger.trace("Command not found " + command);
                return command;
        }

    }
}

