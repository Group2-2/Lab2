package client.controller;

import client.view.*;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


public class ClientControllerImpl implements ClientController {
    private static final Logger logger = Logger.getLogger(ClientControllerImpl.class);
    private int PORT = 12345;
    private String serverAddress = "localhost";
    private Socket socket;
    private ArrayList<String> onlineUsers = new ArrayList<>();
    private BufferedReader in;
    private PrintWriter out;
    private List<String> banUsers;
    private String currentUser;
    private boolean isAdmin;
    private boolean isBanned;
    private boolean isConnected;
    private GeneralChatView generalChatView;
    private AdminView adminView;
    private LoginView loginView;
    private RegistrationView registrationView;
    private LinkedHashMap<String, PrivateChatView> privateChatsList = new LinkedHashMap<>();
    private static String mainChatID = "0";

    public static void main(String[] args) throws IOException, SAXException {
        ClientControllerImpl client = new ClientControllerImpl();
        client.run();
    }

    public void run() {
        isConnected = connectServer();
        if (isConnected) {
            loginView = new LoginView(this);
            while (isConnected) {
                String line = null;
                try {
                    line = in.readLine();
                    Document document = getXML(line);
                    NodeList nodes = document.getElementsByTagName("command");
                    Element element = (Element) nodes.item(0);
                    String type = element.getAttribute("type");
                    String result = element.getAttribute("result");
                    if (type.equals("login")) {
                        if (result.equals("ACCEPTED")) {
                            String userName = element.getAttribute("name");
                            setCurrentUser(userName);
                            boolean isAdminString = Boolean.parseBoolean(element.getAttribute("isAdmin"));
                            setAdmin(isAdminString);
                            isBanned = Boolean.parseBoolean(element.getAttribute("isInBan"));
                            break;
                        } else if (result.equals("NOTACCEPTED")) {
                            loginView = new LoginView(this);
                        }
                    } else if (type.equals("registration")) {
                        if (result.equals("ACCEPTED")) {
                            boolean isAdminString = Boolean.parseBoolean(element.getAttribute("isAdmin"));
                            setAdmin(isAdminString);
                            break;
                        } else if (result.equals("NOTACCEPTED")) {
                            RegistrationView registrationView = new RegistrationView(this);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    isConnected = false;
                    try {
                        in.close();
                        out.close();
                    } catch (Exception ex) {
                        logger.info("Потоки не были закрыты!");
                    }
                }
            }
            Thread thread = new Thread(new ReadMessage(in, this));
            thread.start();

            if (isAdmin()) {
                adminView = new AdminView(this);
            } else {
                generalChatView = new GeneralChatView(this, "Main chat");
                generalChatView.setOnlineUsersList(getOnlineUserslist());
                generalChatView.blockBanedUser(isBanned);
            }
            sendOnline("true");
            sendMessage("@ Join chat", mainChatID);

            while (isConnected) {

            }
            thread.interrupt();
            try {
                in.close();
                out.close();
            } catch (Exception e) {
                logger.info("Потоки не были закрыты!");
            }
        }else{
            JOptionPane.showMessageDialog(null, "Server not found!");
        }
    }

    public boolean connectServer() {
        try {
            socket = new Socket(serverAddress, PORT);
            logger.info("Connected: " + socket);
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (UnknownHostException e) {
            logger.error("Host unknown: ", e);
            return false;
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public ClientControllerImpl() {
 /*       loginView = new LoginView(this);

        generalChatView = new GeneralChatView(this, "Main chat");
        generalChatView.setOnlineUsersList(getOnlineUserslist());
        getChatList();

        adminView = new AdminView(this);
        PrivateChatView PrivateChatView = new PrivateChatView(this);*/
        /*PrivateChatView PrivateChatView = new PrivateChatView(this);
        adminView = new AdminView(this);
        RegistrationView registrationView = new RegistrationView(this);
        OnlineUsersView OnlineUsersView = new OnlineUsersView(this, "Users", "sdf");*/
    }

    public void openRegistrationView(String login, String password) {
        RegistrationView registrationView = new RegistrationView(this);
        registrationView.setLoginPassword(login, password);
    }

    public boolean openPrivateChat(String login, String chat_id) {
        if (login.equals(getCurrentUser()) && (!privateChatsList.containsKey(chat_id))) {
            PrivateChatView privateChatView = new PrivateChatView(this);
            privateChatView.setChat_id(chat_id);
            sendMessage("@ Join chat", chat_id);
            privateChatsList.put(chat_id, privateChatView);
            return true;
        }
        return true;
    }

    public void exitChat() {
        sendMessage("@ Has left chat", mainChatID);
        sendOnline("false");
        exitApp();
    }

    public void exitApp() {
        try {
            isConnected = false;
            in.close();
            out.close();
        } catch (Exception e) {
            logger.error("Chat exit failed! ", e);
        }
    }

    public void leavePrivateChat(String chat_id) {
        if (privateChatsList.containsKey(chat_id)) {
            sendMessage("@ Has left chat", chat_id);
            privateChatsList.remove(chat_id);
        }
    }

    public synchronized void getMessages(String chat_id, String text, String sender) {
        String massage = sender.concat(": ").concat(text);
        if (chat_id.equals(mainChatID)) {
            generalChatView.printNewMassage(massage);
        } else if (privateChatsList.containsKey(chat_id)) {
            PrivateChatView privateChatView = privateChatsList.get(chat_id);
            privateChatView.printNewMassage(massage);
        }
    }

    private void banUserConfirm(String login) {
        String massage = "@ADMIN has banned ".concat(login);
        generalChatView.printNewMassage(massage);
        if (login.equals(getCurrentUser())) {
            isBanned = true;
            generalChatView.blockBanedUser(isBanned);
        }
    }

    private void unBanUserConfirm(String login) {
        String massage = "@ADMIN has UNbanned ".concat(login);
        generalChatView.printNewMassage(massage);
        if (login.equals(getCurrentUser())) {
            isBanned = false;
            generalChatView.blockBanedUser(isBanned);
        }
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public static String getMainChatID() {
        return mainChatID;
    }

    public boolean sendMessage(String message, String chatID) {
        //<command type="addMessage" sender="my_nick" chat_id = "0" text ="dsfaf"/>
        String msg = String.format("<command type=\"addMessage\" sender=\"%1$s\" chat_id = \"%2$s\" text =\"%3$s\"/>", getCurrentUser(),chatID, message);
        return (sendXMLString(msg));
    }

    public boolean registerNewUser(String login, String nickName, String password) {
        //<addMessage sender = * chat_id = * text = ***/>
        String msg = String.format("<command type=\"registration\" login=\"%1$s\" name = \"%2$s\" password =\"%3$s\"/>", login, nickName, password);
        setCurrentUser(login);
        return (sendXMLString(msg));
    }

    public void sendOnline(String isOnline) {
        //<addMessage sender = * chat_id = * text = ***/>
        String msg = String.format("<command type=\"setOnlineStatus\" user=\"%1$s\" isOnline = \"%2$s\"/>", getCurrentUser(), isOnline);
        sendXMLString(msg);
    }

    public boolean validateUser(String login, String password) {
        //<command type="login" login="log1" password ="pass1"/>
        String msg = String.format("<command type=\"login\" login=\"%1$s\" password =\"%2$s\"/>", login, password);
        setCurrentUser(login);
        return (sendXMLString(msg));
    }

    public boolean createPrivateChat() {
        //<command type="newChatID" sender = "sender"/>
        String msg = String.format("<command type=\"newChatID\" sender = \"%s\"/>", getCurrentUser());
        return (sendXMLString(msg));
    }

    public void addToPrivateChat(String login, String chat_id) {
        //<command type="addToChat" chat_id = "0" user = "***" />
        String msg = String.format("<command type=\"addToChat\" chat_id = \"%s\" user = \"%s\"/>", chat_id, login);
        sendXMLString(msg);
    }

    public boolean banUser(String banedUser) {
        //<command type="ban" user = "***"></command>
        String msg = String.format("<command type=\"ban\" user = \"%s\"/>", banedUser);
        return (sendXMLString(msg));
    }

    public boolean unBanUser(String unBanUser) {
        //<command type="unban" user = "***"></command>
        String msg = String.format("<command type=\"unban\" user = \"%s\"/>", unBanUser);
        return (sendXMLString(msg));
    }

    public boolean getChatList() {
        //<command type="сhats" sender = "***"></command>
        String msg = String.format("<command type=\"сhats\" sender = \"%s\"/>", getCurrentUser());
        return (sendXMLString(msg));
    }

    public boolean sendXMLString(String xmlText) {
        System.out.println(xmlText);
        out.println(xmlText); //test
        return true;
    }


    public ArrayList<String> getOnlineUserslist() {
        onlineUsers.clear();
        onlineUsers.add("Marta");
        onlineUsers.add("Anna");
        onlineUsers.add("Kate");
        onlineUsers.add("Lili");

        //<command type="online_users"></command>
        String msg = "<command type=\"online_users\"/>";
        System.out.println(msg);
        sendXMLString(msg);

        return onlineUsers;
    }

    public void addToPrivateChatSelect(String chat_id) {
        OnlineUsersView OnlineUsersView = new OnlineUsersView(this, "Select user to add to chat", "addToPrivateChat", chat_id);
    }

    public void unBanUserSelect(String chat_id) {
        OnlineUsersView OnlineUsersView = new OnlineUsersView(this, "Select user to unban", "unBanUser", chat_id);
    }

    public void banUserSelect(String chat_id) {
        OnlineUsersView OnlineUsersView = new OnlineUsersView(this, "Select user to ban", "banUser", chat_id);
    }


    private Document getXML(String value) {
        Document document = null;
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(new InputSource(new StringReader(value)));
            document.getDocumentElement().normalize();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        return document;
    }

    private class ReadMessage implements Runnable {
        private boolean stoped;
        BufferedReader in;
        private ClientControllerImpl controller;


        public ReadMessage(BufferedReader in, ClientControllerImpl controller) {
            this.in = in;
            this.controller = controller;
        }

        public void setStop() {
            stoped = true;
        }

        public void run() {
            try {
                while (true) {
                    String line = in.readLine();
                    Document document = getXML(line);
                    NodeList nodes = document.getElementsByTagName("command");
                    Element element = (Element) nodes.item(0);
                    String type = element.getAttribute("type");

                    switch (type) {
                        case "all_users": {
                            //
                        }
                        case "online_users": {
                            //
                        }
                        case "сhats": {
                            //
                        }
                        case "get_messages": {
                            //
                        }
                        case "get_chat_users": {
                            //
                        }
                        case "ban": {
                            String login = element.getAttribute("user");
                            controller.banUserConfirm(login);
                            //
                        }
                        case "unban": {
                            String login = element.getAttribute("user");
                            controller.unBanUserConfirm(login);
                            //
                        }
                        case "newChatID": {
                            String chat_id = element.getAttribute("chat_id");
                            String login = element.getAttribute("sender");
                            openPrivateChat(login, chat_id);
                        }
                        case "addToChat": {
                            String chat_id = element.getAttribute("chat_id");
                            String login = element.getAttribute("login");
                            openPrivateChat(login, chat_id);
                        }
                        case "addMessage": {
                            String sender = element.getAttribute("sender");
                            String chat_id = element.getAttribute("chat_id");
                            String text = element.getAttribute("text");
                            controller.getMessages(chat_id, text, sender);
                        }
                        case "isInBan": {
                            String login = element.getAttribute("user");
                            //
                        }
                    }
                }
            } catch (IOException e) {
                logger.info("Ошибка при получении сообщения!");
                e.printStackTrace();
            }
        }
    }


}

