package server.controller;

import org.apache.log4j.Logger;
import server.model.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Creates new Connections on demand
 * Checks for crush connections
 * Supports console for admin control
 * @see server.controller.ServerController
 * @see server.controller.Connection
 */
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
     * @see Server#checkOnline
     * @param port port
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
        new Thread(this::consoleStart).start();
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
    public void setUser(String login, Connection connection) {
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

    /**
     * The main method, starts the Thread
     * @param args args
     */
    public static void main(String[] args) {
            new Thread(instance).start();
    }

    @Override
    public void sendToChat(Long chatId, String text, Connection current) {
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
    private void checkOnline() {
        while (!Thread.interrupted()) {
            Iterator<Map.Entry<String, Connection>> entries = users.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, Connection> entry = entries.next();
                if (!entry.getValue().checkConnection()) {
                    ModelImpl.getInstance().setOnlineStatus(entry.getKey(), false);
                    if (entries.hasNext()) {
                        entries.next();
                    }
                    entries.remove();
                }
            }
            try {
                Thread.sleep(60000); // 1 minute
            } catch (InterruptedException e) {
                logger.warn("CheckOnline, ", e);
            }
        }
    }
    public void deleteUser(Connection conn){
        Iterator<Map.Entry<String, Connection>> entries = users.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, Connection> entry = entries.next();
            if (conn == entry.getValue()) {
                ModelImpl.getInstance().setOnlineStatus(entry.getKey(), false);
                if (entries.hasNext()) {
                    entries.next();
                }
                entries.remove();
            }
        }
    }
    /**
     * The method for Admin Console Configuration - run() in Thread
     */
    private void consoleStart() {
        while (true) {
            consoleMenu();
        }
    }

    /**
     * gives us a list of users by status
     * @see Server#consoleChangeUser(String)
     */
    private void consoleMenu() {
        int count = 4;
        System.out.println("0 --- STOP SERVER");
        System.out.println("1 --- get all users");
        System.out.println("2 --- get online users");
        System.out.println("3 --- get ban users");
        System.out.println("4 --- get unban users");
        int a;
        while (true) {
            a = consoleInputIndex();
            if(a < 0 || a > count) {
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
                if(consoleInputIndex() == 1){
                    System.exit(0);
                }
                break;
            case 1:
                list = ModelImpl.getInstance().getListUsers();
                consoleShowUsers(list);
                break;
            case 2:
                list = ModelImpl.getInstance().getOnlineListUsers();
                consoleShowUsers(list);
                break;
            case 3:
                list = ModelImpl.getInstance().getListUsers();
                list.forEach(string -> {
                    if (ModelImpl.getInstance().isInBan(string)) list1.add(string);
                });
                consoleShowUsers(list1);
                    break;
            case 4:
                list = ModelImpl.getInstance().getListUsers();
                list.forEach(string -> {
                    if (!ModelImpl.getInstance().isInBan(string)) list1.add(string);
                });
                consoleShowUsers(list1);
                break;
            default:
                    System.out.println("smth wrong");
                    break;
        }
    }

    /** called in consoleMenu
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
                String s = (String) list.get(i-1);
                System.out.println(i + ": " + s + " - isBan: " + ModelImpl.getInstance().isInBan(s)
                        + ", isAdmin: " + ModelImpl.getInstance().isAdmin(s)
                        + ", online: " + ModelImpl.getInstance().isOnline(s));
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
            if (a == 0) return;
            consoleChangeUser((String) list.get(a - 1));
        }
     }

    /**
     * show all user information, gives the opportunity to change BAN/UNBAN, isAdmin/no called in
     * consoleShowUsers
     * @param login of current user
     * @see Server#consoleShowUsers(List)
     * */
     private void consoleChangeUser(String login) {
        while (true) {
            System.out.println(login + " - isBan: " + ModelImpl.getInstance().isInBan(login)
                    + ", isAdmin: " + ModelImpl.getInstance().isAdmin(login) + ", online: "
                    + ModelImpl.getInstance().isOnline(login));
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
                    return;
                case 1:
                    if (ModelImpl.getInstance().isInBan(login)) {
                        ModelImpl.getInstance().unban(login);
                    } else {
                        ModelImpl.getInstance().ban(login);
                    }
                    ModelImpl.getInstance().save();
                    break;
                case 2:
                    if (!ModelImpl.getInstance().isAdmin(login)){
                        ModelImpl.getInstance().createAdmin(login);
                    } else {
                        ModelImpl.getInstance().deleteAdmin(login);
                    }
                    ModelImpl.getInstance().save();
                    break;
                default:
                    System.out.println("smth wrong");
                    break;
            }
        }
     }

    /**
     * method for menu, user has to enter int value above 0
     * @return int value that user entered, -1 - if user entered smth wrong
     */
    private static int consoleInputIndex() {
        System.out.println("Enter your choise:");
        Scanner sc = new Scanner(System.in);
        int  n;
        try {
            n = sc.nextInt();
        } catch (Exception e) {
            return -1;
        }
        return n;
    }
}
