package controller;

import model.XmlConfiguration;
import org.apache.log4j.Logger;
import model.ModelImpl;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.*;


/**
 * Creates new Connections on demand.
 * Checks for crush connections
 * Supports console for admin control
 * Class is singletone.
 * @see controller.ServerController
 * @see controller.Connection
 */
public class Server implements ServerController {
    /**
     * logger for this class.
     */
    private static final Logger logger = Logger.getLogger(Server.class);
    /**
     * Used for getting sockets to connection.
     */
    private ServerSocket serverSocket;
    /**
     * current server port.
     */
    private final int port;
    /**
     * when it's true - thread checkOnline works.
     */
    private boolean checkOnlineWork = true;
    /**
     * when it's true - console of the server works.
     */
    private boolean consoleWork = true;
    /**
     * for working current insatanse of the server works.
     */
    private static boolean serverWork = true;
    /**
     * instance of model for control.
     */
    private static final ModelImpl model = ModelImpl.getInstance();
    /**
     * instance for parsing commands.
     */
    private static final XmlConfiguration xml = XmlConfiguration.getInstance();
    /**
     * login - current connection of online users.
     * (login, connection for login)
     */
    private Map<String, Connection> users;
    /**
     * current instance of server.
     */
    private static Server instance;

    /**
     * milliseconds in period of meth that checks online users.
     */
    private final int period  = 60000;
    /**
     * initializes port, map, ServerSocket and started new Thread with checkOnline method.
     * @see Server#checkOnline
     * @param port port
     */

    private Server(int port) {
        users = new Hashtable<>();
        this.port = port;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            logger.error("IOEx in Server - getInstance, ServerSocket: " + serverSocket.toString());
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

    /**
     * for current instance.
     * @return current instance
     */
    public static Server getInstance() {
        return instance;
    }

    @Override
    public void setUser(String login, Connection connection) {
        users.put(login, connection);
        model.save();
    }

    @Override
    public void run() {
        while (serverWork) {
            final Socket socket;
            try {
                socket = serverSocket.accept();
                final Connection connection = new Connection(socket);
                new Thread(connection).start();
            } catch (IOException e) {

                logger.warn("Connection-socket ServerSocket: " + serverSocket.toString());
            }
        }
    }

    /**
     * The main method, starts the Thread.
     * @param args args
     */
    public static void main(String[] args) {
        List<String> list = model.getOnlineListUsers();
        for (String login:list) {
            model.setOnlineStatus(login, false);
        }
            while (true) {
                System.out.print("Enter port: ");
                int myPort = consoleInputIndex();
                if (myPort < 0 || myPort > 65535 || myPort == 8080) {
                    System.out.println("Wrong Port value");
                    continue;
                }
                instance = new Server(myPort);
                break;
            }
            new Thread(instance::consoleStart).start();
            new Thread(instance).start();
    }

    @Override
    public void sendToChat(Long chatId, String text, Connection current) {
        List list = model.getChatUsers(chatId);
        users.forEach((login, connection) -> {
            if (list.contains(login) && !connection.equals(current)) {
                connection.send(text);
            }
        });
       // System.out.println("send to chat " + chatId + " - " + text);
        model.save();
    }

    /**
     * Sends command by connection for special user.
     * @param login - login of special user
     * @param command for this user
     */
    public void sendToUser(String login, String command) {
        if (users.containsKey(login)) {
            users.get(login).send(command);
        }
        model.save();
      //  System.out.println("send to " + login + "- " + command);
    }

    /**
     * Checks crush connections.
     * method for thread, every SomePeriodOfTIme checks all users, was connection crush or no.
     */
    private void checkOnline() {
        while (checkOnlineWork) {
            Iterator<Map.Entry<String, Connection>> entries = users.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, Connection> entry = entries.next();
                if (entry.getValue().checkConnection()) {
                    model.setOnlineStatus(entry.getKey(), false);
                    model.setOnlineStatus(entry.getKey(), false);
                    Map<String, Object> map = new HashMap<>();
                    map.put("user", entry.getKey());
                    map.put("isOnline", false);
                    sendToChat(Long.parseLong("0"), xml.command("setOnlineStatus", map), entry.getValue());
                 //   if (entries.hasNext()) {
                 //       entries.next();
                        entries.remove();
                //    }
                    model.save();
                }
            }
            try {
                Thread.sleep(period); // 1 minute
            } catch (InterruptedException e) {
                logger.warn(e);
            }
        }
    }

    /**
     * deletes user from map and set offline status.
     * @param conn link on the connection to user
     */

    public void deleteUser(Connection conn) {
        Iterator<Map.Entry<String, Connection>> entries = users.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, Connection> entry = entries.next();
            if (conn == entry.getValue()) {
                if (model.existUser(entry.getKey())) {
                    model.setOnlineStatus(entry.getKey(), false);
                    Map<String, Object> map = new HashMap<>();
                    map.put("user", entry.getKey());
                    map.put("isOnline", false);
                    sendToChat(Long.parseLong("0"), xml.command("setOnlineStatus", map), entry.getValue());
                    entry.getValue().stopConnection();
                }
              //  if (entries.hasNext()) {
                    entries.remove();
           //     }
                model.save();
            }
        }
    }
    /**
     * The method for Admin Console Configuration - run() in Thread.
     */
    private void consoleStart() {
        while (consoleWork) {
            consoleMenu();
        }
    }

    /**
     * gives us a list of users by status.
     * @see Server#consoleChangeUser(String)
     */
    private void consoleMenu() {
        int count = 13;
        System.out.println("0 --- EXIT APP");
        if (serverWork) {
            System.out.println("10 --- STOP SERVER");
            System.out.println("12 --- OVERLOAD SERVER");
        } else {
            System.out.println("11 --- START SERVER");
        }
        System.out.println("13 --- show current PORT");
        System.out.println("1 --- get all users");
        System.out.println("2 --- get online users");
        System.out.println("3 --- get ban users");
        System.out.println("4 --- get unban users");
        int a;
        while (true) {
            a = consoleInputIndex();
            if (a < 0 || a > count) {
                System.out.println("wrong");
                continue;
            }
            break;
        }
        List<String> list;
        List<String> list1 = new ArrayList<>();
        switch (a) {
            case 0:
                System.out.print("Are you sure to stop server? Enter 1: ");
                if (consoleInputIndex() == 1) {
                    if (Server.getInstance().serverWork) {
                        stop();
                    }
                    System.exit(0);
                }
                break;
            case 1:
                list = model.getListUsers();
                consoleShowUsers(list);
                break;
            case 2:
                list = model.getOnlineListUsers();
                consoleShowUsers(list);
                break;
            case 3:
                list = model.getListUsers();
                list.forEach(string -> {
                    if (model.isInBan(string)) {
                        list1.add(string);
                    }
                });
                consoleShowUsers(list1);
                    break;
            case 4:
                list = model.getListUsers();
                list.forEach(string -> {
                    if (!model.isInBan(string)) {
                        list1.add(string);
                    }
                });
                consoleShowUsers(list1);
                break;
            case 10:
                if (!Server.getInstance().serverWork) {
                    System.out.println("server was stopped");
                      return;
                }
                System.out.print("Are you sure to stop server? Enter 1: ");
                if (consoleInputIndex() == 1) {
                    sendToChat(Long.parseLong("0"), xml.command("stop", null), null);
                    stop();
                }
                break;
            case 11:
                if (serverWork) {
                    System.out.println("Server was started");
                } else {
                    instance = new Server(port);
                    Server.getInstance().serverWork = true;
                    new Thread(instance).start();
                }
                break;
            case 12:
                if (!Server.getInstance().serverWork) {
                    System.out.println("Server was stopped");
                    return;
                }
                sendToChat(Long.parseLong("0"), xml.command("restart", null), null);
                stop();
                instance = new Server(port);
                Server.getInstance().serverWork = true;
                new Thread(instance).start();
                break;
            case 13:
                System.out.println("current port: " + instance.getPort() + "\n");
                break;
            default:
                    System.out.println("smth wrong");
                    break;
        }
    }

    /**
     *  called in consoleMenu.
     * show users in current list and call consoleChangeUser
     * @see Server#consoleChangeUser(String)
     * @see Server#consoleMenu()
     * @param list current list
     */
     private void consoleShowUsers(List list) {
        while (true) {
            if (list == null || list.size() == 0) {
                System.out.println("List is empty\n");
                return;
            }
            System.out.println("0 - return");
            for (int i = 1; i <= list.size(); i++) {
                String s = (String) list.get(i - 1);
                System.out.println(i + ": " + s + " - isBan: " + model.isInBan(s)
                        + ", isAdmin: " + model.isAdmin(s)
                        + ", online: " + model.isOnline(s));
            }
            System.out.print("The user you want to change. ");
            int a;
            while (true) {
                a = consoleInputIndex();
                if (a > list.size() || a < 0) {
                    System.out.println("wrong");
                    continue;
                }
                break;
            }
            if (a == 0) {
                return;
            }
            if(consoleChangeUser((String) list.get(a - 1))) {
                System.out.println("You changed user setting, upload..");
                return;
            }
        }
     }

    /**
     * show all user information, gives the opportunity to change BAN/UNBAN, isAdmin/no called in.
     * consoleShowUsers
     * @param login of current user
     * @see Server#consoleShowUsers(List)
     * */
     private boolean consoleChangeUser(String login) {
        while (true) {
            System.out.println(login + " - isBan: " + model.isInBan(login)
                    + ", isAdmin: " + model.isAdmin(login) + ", online: "
                    + model.isOnline(login));
            System.out.println("0 --- return");
            System.out.println("1 --- ban/unban");
            System.out.println("2 --- setAdmin/no");
            int a;
            while (true) {
                a = consoleInputIndex();
                if (a < 0 || a > 2) {
                    System.out.print("Wrong. ");
                    continue;
                }
                break;
            }
            switch (a) {
                case 0:
                    return false;
                case 1:
                    Map<String, Object> map = new HashMap<>();
                    map.put("login", login);
                    if (model.isInBan(login)) {
                        sendToChat(Long.parseLong("0"), xml.command("unban", map), null);
                        model.unban(login);
                    } else {
                        sendToChat(Long.parseLong("0"), xml.command("ban", map), null);
                        model.ban(login);
                    }
                    model.save();
                    return true;
                case 2:
                    if (!model.isAdmin(login)) {
                        model.createAdmin(login);
                    } else {
                        model.deleteAdmin(login);
                    }
                    model.save();
                    return true;
                default:
                    System.out.println("smth wrong");
                    return true;
            }
        }
     }

    /**
     * method for menu, user has to enter int value above 0.
     * @return int value that user entered, -1 - if user entered smth wrong
     */
    private static int consoleInputIndex() {
        System.out.print("Enter your choice: ");
        Scanner sc = new Scanner(System.in);
        int  n;
        try {
            n = sc.nextInt();
        } catch (Exception e) {
            return -1;
        }
        return n;
    }

    /**
     * To stop current SERVER and all connections.
     */
    private void stop() {
        Iterator<Map.Entry<String, Connection>> entries = users.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, Connection> entry = entries.next();
            model.setOnlineStatus(entry.getKey(), false);
            entry.getValue().stopConnection();
          //  if (entries.hasNext()) {
          //      entries.next();
                entries.remove();
         //   }
        }
        model.save();
        Server.getInstance().serverWork = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            logger.debug("IOEx in closing, ServerSocket:" + serverSocket.toString());
        }
    }
}
