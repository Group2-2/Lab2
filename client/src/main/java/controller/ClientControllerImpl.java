package controller;

import view.*;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

public class ClientControllerImpl implements ClientController {
    private static final Logger logger = Logger.getLogger(ClientControllerImpl.class);
    private int port;
    private String serverAddress;
    private Socket socket;
    private ArrayList<String> onlineUsers = new ArrayList<>();
    private ArrayList<String> chatsListInForm = new ArrayList<>();
    private BufferedReader in;
    private PrintWriter out;
    private ArrayList<String> banUsers = new ArrayList<>();
    private ArrayList<String> allUsers = new ArrayList<>();
    private String currentUser;
    private String currentUserPassword;
    private boolean isAdmin;
    private boolean isBanned;
    private boolean isConnected;
    private GeneralChatView generalChatView;
    private AdminView adminView;
    private LoginView loginView;
    private LinkedHashMap<String, PrivateChatView> privateChatsList = new LinkedHashMap<>();
    private static String mainChatID = "0";
    private static final String configPath = "configConnection.xml";
    private OnlineUsersView allUsersView;

    /**
     * @param args args
     */
    public static void main(String[] args) {
        ClientControllerImpl client = new ClientControllerImpl();
        client.run();
    }

    /**
     * Start new chat application.
     */
    public void run() {
        isConnected = connectServer();
        if (isConnected) {
            loginView = new LoginView(this);
            readEnterToChat();
            /*//test
            String msg = "<command type=\"createAdmin\" user = \"admin\"/>";
            sendXMLString(msg);
            //*/

            if (isAdmin()) {
                generalChatView = new AdminView(this);
                generalChatView.setTitle(getCurrentUser().concat(": ADMIN_General chat"));
            } else {
                generalChatView = new GeneralChatView(this, getCurrentUser().concat(": Main chat"));
                generalChatView.blockBanedUser(isBanned);
            }
            logger.info("Client start working");
            readInputStream();

        } else {
            JOptionPane.showMessageDialog(null, "Server not found! Connection settings in file: " + configPath);
            exitApp();
            logger.error("Server not found!");
        }
    }

    /**
     * read input stream user login in application.
     */
    public void readEnterToChat() {
        while (isConnected) {
            String line = null;
            try {
                line = in.readLine();
                if (line.contains("<test></test>")) {
                    continue;
                }
                Document document = getXML(line);
                NodeList nodes = document.getElementsByTagName("command");
                Element element = (Element) nodes.item(0);
                String type = element.getAttribute("type");
                String result = element.getAttribute("result");
                if (type.equals("login")) {
                    if (result.equals("ACCEPTED")) {
                        boolean isAdminString = Boolean.parseBoolean(element.getAttribute("isAdmin"));
                        setAdmin(isAdminString);
                        isBanned = Boolean.parseBoolean(element.getAttribute("isInBan"));
                        break;
                    } else if (result.equals("NOTACCEPTED")) {
                        JOptionPane.showMessageDialog(null, "Incorrect login/password");
                        loginView = new LoginView(this);
                    }
                } else if (type.equals("registration")) {
                    if (result.equals("ACCEPTED")) {
                        boolean isAdminString = Boolean.parseBoolean(element.getAttribute("isAdmin"));
                        setAdmin(isAdminString);
                        break;
                    } else if (result.equals("NOTACCEPTED")) {
                        JOptionPane.showMessageDialog(null, "Login is alredy used!");
                        loginView = new LoginView(this);
                    }
                }
            } catch (IOException e) {
                logger.error("Failed to read input stream for access!");
                e.printStackTrace();
                exitApp();
            }
        }
    }

    /**
     * get in out streams.
     *
     * @return boolean successful connected
     */
    public boolean connectServer() {
        try {
           if (!readConfig()) {
               JOptionPane.showMessageDialog(null, "Delete file or change settings in file: " + configPath);
               logger.error("File with connection config cannot read!");
               return false;
           }
            socket = new Socket(getServerAddress(), getPort());
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            logger.info("Connected: " + socket);
        } catch (UnknownHostException e) {
            logger.error("Host unknown: ", e);
            return false;
        } catch (IOException e) {
            logger.error("Failed to read connection config ", e);
            return false;
        }
        return true;
    }


    /**
     * start reading input stream massages.
     */
    public void readInputStream() {
        boolean varGetOnlineUsers = false;
        boolean varSendOnlines = false;
        boolean varLoadMessages = false;
        boolean varSetChatList = false;
        boolean varSetBanList = false;
        try {
            getOnlineUsers();
            while (isConnected) {
                if (varGetOnlineUsers && !varLoadMessages) {
                    getMassagesInChat(mainChatID);
                }
                if (varGetOnlineUsers && varLoadMessages && !varSendOnlines) {
                    sendOnline("true");
                    varSendOnlines = true;
                }
                if (varGetOnlineUsers && varLoadMessages && varSendOnlines && !varSetChatList) {
                    getChatsList();
                }
                if (varGetOnlineUsers && varLoadMessages && varSendOnlines && varSetChatList && isAdmin() && !varSetBanList) {
                    getBanList();
                }
                String line = in.readLine();
                if (line.contains("<test></test>")) {
                    continue;
                }
                System.out.println("Get in line " + line);
                if (line.equals("</messages>") || line.equals("<messages/>")) {
                    varLoadMessages = true;
                    continue;
                }
                if (line.equals("</onlineUsers>") || line.equals("<onlineUsers/>")) {
                    varGetOnlineUsers = true;
                    continue;
                }

                Document document = getXML(line);
                NodeList nodes = document.getElementsByTagName("command");
                Element element = (Element) nodes.item(0);
                if (element == null) {
                    if (document.getDocumentElement().getNodeName().equals("onlineUsers")) {
                        setOnlineUsers(line);
                        varGetOnlineUsers = true;
                    }
                    if (document.getDocumentElement().getNodeName().equals("users")) {
                        setAllUsers(line);
                    }
                    if (document.getDocumentElement().getNodeName().equals("messages")) {
                        setAllMassages(line);
                        varLoadMessages = true;
                    }
                    if (document.getDocumentElement().getNodeName().equals("chats")) {
                        setChats(line);
                        varSetChatList = true;
                    }
                    if (document.getDocumentElement().getNodeName().equals("banList")) {
                        setBanList(line);
                        varSetBanList = true;
                    }
                    continue;
                }
                String type = element.getAttribute("type");

                switch (type) {
                    case "setOnlineStatus": {
                        String login = element.getAttribute("user");
                        boolean online = Boolean.parseBoolean(element.getAttribute("isOnline"));
                        changeOnlineUsers(login, online);
                        break;
                    }
                    case "ban": {
                        String login = element.getAttribute("login");
                        banUserConfirm(login);
                        break;
                    }
                    case "unban": {
                        String login = element.getAttribute("login");
                        unBanUserConfirm(login);
                        break;
                    }
                    case "newChatID": {
                        String chatId = element.getAttribute("chat_id");
                        String login = element.getAttribute("user");
                        openPrivateChat(login, chatId, false);
                        break;
                    }
                    case "addToChat": {
                        String chatId = element.getAttribute("chat_id");
                        String login = element.getAttribute("login");
                        openPrivateChat(login, chatId, true);
                        break;
                    }
                    case "addMessage": {
                        String sender = element.getAttribute("sender");
                        String chatId = element.getAttribute("chat_id");
                        String text = element.getAttribute("text");
                        getMessages(chatId, text, sender);
                        break;
                    }
                    case "isInBan": {
                        String login = element.getAttribute("user");
                        break;
                    }
                    case "deleteUser": {
                        String result = element.getAttribute("result");
                        if (result.equals("ACCEPTED")) {
                            JOptionPane.showMessageDialog(null, "User has been deleted!");
                        }
                        break;
                    }
                    case "changePassword": {
                        String result = element.getAttribute("result");
                        if (result.equals("ACCEPTED")) {
                            JOptionPane.showMessageDialog(null, "Password has been changed!");
                        }
                        break;
                    }
                    case "stop": {
                        Object[] options = {"OK", "CANCEL"};
                        int n = JOptionPane
                                .showOptionDialog(null, "Chat server has been stoped! Close application?",
                                        "Confirmation", JOptionPane.OK_CANCEL_OPTION,
                                        JOptionPane.QUESTION_MESSAGE, null, options,
                                        options[0]);
                        if (n == 0) {
                            exitChat();
                            System.exit(2);
                        }
                        break;
                    }
                    case "restart": {
                        restartClient();
                        break;                                            
                    }
                    default:
                        logger.trace("Command type not found " + type);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Ошибка при получении сообщения!");
            exitApp();
        }
    }

    /**
     * After server restart try to reconnect to server.
     */
    private void restartClient() {
        exitApp();
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.error("Wait for restart failed ", e);
        }
        isConnected = connectServer();
        if (isConnected) {
            validateUser(getCurrentUser(), getCurrentUserPassword());
            readEnterToChat();
            //System.out.println("Server has been restart!");
            getMessages(mainChatID, "Server has been restart!", "SERVER");
            logger.info("Client and Server has been restart!");
        } else {
            logger.error("Не удалось переподключится к серверу!");
            Object[] options = {"OK", "CANCEL"};
            int n = JOptionPane
                    .showOptionDialog(null, "Chat server has been restart! But reconnection failed( Close application?",
                            "Confirmation", JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE, null, options,
                            options[0]);
            if (n == 0) {
                exitChat();
                System.exit(2);
            }
        }
    }

    /**
     * @param login login
     * @param chatId chatId
     * @param addTo start new or add to exist chat
     * @return chat is opened
     */
    public boolean openPrivateChat(String login, String chatId, boolean addTo) {
        if (login.equals(getCurrentUser()) && (!chatsListInForm.contains(chatId))) {
            chatsListInForm.add(chatId);
            generalChatView.setPrivateChatsList(chatsListInForm);
        }
        if (addTo && login.equals(getCurrentUser()) && (!privateChatsList.containsKey(chatId))) {
            int dialogButton = JOptionPane.YES_NO_OPTION;
            int dialogResult = JOptionPane.showConfirmDialog(null, "Open private chat window?", "Join private chat", dialogButton);
            if (dialogResult == 0) {
                openPrivateChatWindow(chatId);
            }
        }
        if (!addTo && login.equals(getCurrentUser()) && (!privateChatsList.containsKey(chatId))) {
            openPrivateChatWindow(chatId);
            addToPrivateChatSelect(chatId);
        }
        return true;
    }

    /**
     * open private chat window.
     * * @param chatId chatId
     */
    public void openPrivateChatWindow(String chatId) {
        PrivateChatView privateChatView = new PrivateChatView(this);
        privateChatView.setTitle(getCurrentUser().concat(": Private chat room"));
        privateChatView.setChat_id(chatId);
        getMassagesInChat(chatId);
        privateChatsList.put(chatId, privateChatView);
        privateChatView.setPrivateChatsList(chatsListInForm);
    }

    /**
     * close main chat window.
     */
    public void exitChat() {
        sendMessage("@ Has left chat", mainChatID);
        sendOnline("false");
        exitApp();
    }

    /**
     * close in out streams.
     */
    public void exitApp() {
        try {
            isConnected = false;
            in.close();
            out.close();
            socket.close();
            logger.info("Closed resources and exit application");
        } catch (Exception e) {
            logger.error("Application exit failed!");
        }
    }

    /**
     * close private chat window.
     *
     * @param chatId chatId
     */
    public void leavePrivateChat(String chatId) {
        if (privateChatsList.containsKey(chatId)) {
            sendMessage("@ Has left chat", chatId);
            privateChatsList.remove(chatId);
        }
    }

    /**
     * close and leave private chat.
     *
     * @param chatId chatId
     */
    public void leaveForeverPrivateChat(String chatId) {
        leavePrivateChat(chatId);
        if (chatsListInForm.contains(chatId)) {
            chatsListInForm.remove(chatId);
            generalChatView.setPrivateChatsList(chatsListInForm);
        }
        String msg = String.format("<command type=\"leaveChat\" chat_id=\"%1$s\" login = \"%2$s\" />", chatId, getCurrentUser());
        sendXMLString(msg);
    }


    /**
     * print input massage into wright window.
     *
     * @param chatId chatId
     * @param text text
     * @param sender sender
     */
    public synchronized void getMessages(String chatId, String text, String sender) {
        String massage = sender.concat(": ").concat(text);
        if (chatId.equals(mainChatID)) {
            generalChatView.printNewMassage(massage);
        } else if (privateChatsList.containsKey(chatId)) {
            PrivateChatView privateChatView = privateChatsList.get(chatId);
            privateChatView.printNewMassage(massage);
        }
    }

    /**
     * input stream user is banned.
     *
     * @param login login
     */
    private void banUserConfirm(String login) {
        if (login.equals(getCurrentUser())) {
            JOptionPane.showMessageDialog(null, "Admine has banned you");
            isBanned = true;
            generalChatView.blockBanedUser(isBanned);
            logger.info("Admine has banned you");
        }
        if (isAdmin()) {
            if (!banUsers.contains(login)) {
                banUsers.add(login);
            }
            generalChatView.setBannedList(banUsers);
            sendMessage("@ADMIN has banned ".concat(login), mainChatID);
        }
    }

    /**
     * input stream user is unbanned.
     *
     * @param login login
     */
    private void unBanUserConfirm(String login) {
        if (login.equals(getCurrentUser())) {
            JOptionPane.showMessageDialog(null, "Admine has unbanned you");
            isBanned = false;
            generalChatView.blockBanedUser(isBanned);
            logger.info("Admine has unbanned you");
        }
        if (isAdmin()) {
            if (banUsers.contains(login)) {
                banUsers.remove(login);
            }
            generalChatView.setBannedList(banUsers);
            sendMessage("@ADMIN has Unbanned ".concat(login), mainChatID);
        }
    }

    /**
     * getter current user login.
     *
     * @return currentUser
     */
    public String getCurrentUser() {
        return currentUser;
    }

    /**
     * set current user login
     *
     * @param currentUser currentUser
     */
    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    /**
     * getter current user password.
     *
     * @return currentUser
     */
    public String getCurrentUserPassword() {
        return currentUserPassword;
    }

    /**
     * set current user password.
     *
     * @param currentUserPassword currentUserPassword
     */
    public void setCurrentUserPassword(String currentUserPassword) {
        this.currentUserPassword = currentUserPassword;
    }
    /**
     * @return isAdmin
     */
    public boolean isAdmin() {
        return isAdmin;
    }

    /**
     * @param admin admin
     */
    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    /**
     * @return mainChatID.
     */
    public static String getMainChatID() {
        return mainChatID;
    }

    /**
     * prepare massage for output stream.
     *
     * @param message message
     * @param chatId  chatId
     * @return success Message is sent
     */
    public boolean sendMessage(String message, String chatId) {
        //<command type="addMessage" sender="my_nick" chat_id = "0" text ="dsfaf"/>
        String msg = String.format("<command type=\"addMessage\" sender=\"%1$s\" chat_id = \"%2$s\" text =\"%3$s\"/>", getCurrentUser(), chatId, message.replaceAll("\\n", " "));
        return (sendXMLString(msg));
    }

    /**
     * prepare command register new user
     *
     * @param login login
     * @param password password
     * @return success Massage is sent
     */
    public boolean registerNewUser(String login, String password) {
        //<addMessage sender = * chat_id = * text = ***/>
        String msg = String.format("<command type=\"registration\" login=\"%1$s\" password =\"%2$s\"/>", login, password);
        setCurrentUser(login);
        setCurrentUserPassword(password);
        return (sendXMLString(msg));
    }

    /**
     * prepare command user is online.
     *
     * @param isOnline isOnline
     */
    public void sendOnline(String isOnline) {
        //<addMessage sender = * chat_id = * text = ***/>
        String msg = String.format("<command type=\"setOnlineStatus\" user=\"%1$s\" isOnline = \"%2$s\"/>", getCurrentUser(), isOnline);
        sendXMLString(msg);
    }

    /**
     * prepare command get chats list.
     */
    public void getChatsList() {
        //<addMessage sender = * chat_id = * text = ***/>
        String msg = String.format("<command type=\"chats\" sender=\"%1$s\"/>", getCurrentUser());
        sendXMLString(msg);
    }

    /**
     * prepare command check user login-password.
     *
     * @param login login
     * @param password password
     * @return Massage is sent
     */
    public boolean validateUser(String login, String password) {
        //<command type="login" login="log1" password ="pass1"/>
        String msg = String.format("<command type=\"login\" login=\"%1$s\" password =\"%2$s\"/>", login, password);
        setCurrentUser(login);
        setCurrentUserPassword(password);
        return (sendXMLString(msg));
    }

    /**
     * prepare command generate new chat ID.
     *
     * @return Massage is sent
     */
    public boolean createPrivateChat() {
        //<command type="newChatID" sender = "sender"/>
        String msg = String.format("<command type=\"newChatID\" sender = \"%s\"/>", getCurrentUser());
        return (sendXMLString(msg));
    }

    /**
     * prepare command add user to private chat window.
     *
     * @param login login
     * @param chatId chatId
     */
    public void addToPrivateChat(String login, String chatId) {
        //<command type="addToChat" chat_id = "0" user = "***" />
        String msg = String.format("<command type=\"addToChat\" chat_id = \"%s\" login = \"%s\"/>", chatId, login);
        sendXMLString(msg);
    }

    /**
     * prepare command banned user.
     *
     * @param banedUser banedUser
     * @return command is sent
     */
    public boolean banUser(String banedUser) {
        //<command type="ban" user = "***"></command>
        String msg = String.format("<command type=\"ban\" user = \"%s\"/>", banedUser);
        return (sendXMLString(msg));
    }

    /**
     * prepare command unbanned user.
     *
     * @param unBanUser user login
     * @return command is sent
     */
    public boolean unBanUser(String unBanUser) {
        //<command type="unban" user = "***"></command>
        String msg = String.format("<command type=\"unban\" user = \"%s\"/>", unBanUser);
        return (sendXMLString(msg));
    }

    /**
     * prepare command get chats.
     *
     * @return command is sent
     */
    public boolean getChatList() {
        //<command type="сhats" sender = "***"></command>
        String msg = String.format("<command type=\"сhats\" sender = \"%s\"/>", getCurrentUser());
        return (sendXMLString(msg));
    }

    /**
     * prepare command get all online users.
     */
    public void getOnlineUsers() {
        //<command type="online_users"></command>
        String msg = "<command type=\"online_users\"/>";
        sendXMLString(msg);
    }

    /**
     * prepare command get messages in chat.
     * @param chatId chatId
     */
    public void getMassagesInChat(String chatId) {
        String msg = String.format("<command type=\"get_messages\" chat_id=\"%s\"/>", chatId);
        sendXMLString(msg);
    }

    /**
     * prepare command get ban list.
     */
    private void getBanList() {
        String msg = String.format("<command type=\"getBanList\"/>");
        sendXMLString(msg);
    }

    /**
     * sent massage/command to output stream.
     *
     * @param xmlText xml string
     * @return command is sent
     */
    public boolean sendXMLString(String xmlText) {
        System.out.println("OUT " + xmlText);
        out.println(xmlText); //test
        return true;
    }

    /**
     * @return onlineUsers list
     */
    public ArrayList<String> getOnlineUsersList() {
        return onlineUsers;
    }


    /**
     * parse input xml and set online users to frames.
     *
     * @param line xml string
     */
    public void setOnlineUsers(String line) {
        //<online_users>   <user>qwerty</user> </online_users>
        setUsersList(line, onlineUsers);
        generalChatView.setOnlineUsersList(onlineUsers);
    }

    /**
     * parse input xml and set all users list.
     *
     * @param line xml string
     */
    public void setAllUsers(String line) {
        //<users>   <user>qwerty</user> </users>
        setUsersList(line, allUsers);
    }

    /**
     * parse input xml and set all users list.
     *
     * @param line xml string
     */
    public void setBanList(String line) {
        //<banList>   <user>qwerty</user> </banList>
        setUsersList(line, banUsers);
        generalChatView.setBannedList(banUsers);
    }

    /**
     * Common parse input xml and set in list users.
     *
     * @param line xml string
     * @param usersList ArrayList with users
     */
    public void setUsersList(String line, ArrayList<String> usersList) {
        //<banList>   <user>qwerty</user> </banList>
        usersList.clear();
        Document document = getXML(line);
        NodeList users = document.getElementsByTagName("user");
        for (int i = 0; i < users.getLength(); i++) {
            Node node = users.item(i);
            if (node.getNodeName().equals("user")) {
                Element element = (Element) node;
                String nicknameVar = element.getTextContent();
                usersList.add(nicknameVar);
            }
        }
    }


    /**
     * parse input xml and set private chats.
     *
     * @param line xml string
     */
    public void setChats(String line) {
        //<chats>   <long>0</long>   <long>1</long> </chats>
        chatsListInForm.clear();
        Document document = getXML(line);
        NodeList chatIDlong = document.getElementsByTagName("long");
        for (int i = 0; i < chatIDlong.getLength(); i++) {
            Node node = chatIDlong.item(i);
            if (node.getNodeName().equals("long")) {
                Element element = (Element) node;
                String chatStringID = element.getTextContent();
                if (chatStringID.equals("0")) continue;
                if (!chatsListInForm.contains(chatStringID)) {
                    chatsListInForm.add(chatStringID);
                }
            }
        }
        generalChatView.setPrivateChatsList(chatsListInForm);
    }

    /**
     * parse input xml and set massages
     *
     * @param line xml string
     */
    public void setAllMassages(String line) {
        //<messages>   <message chat_id="0" sender="q" text="ff"/>
        Document document = getXML(line);
        NodeList messageList = document.getElementsByTagName("message");
        for (int i = 0; i < messageList.getLength(); i++) {
            Node node = messageList.item(i);
            if (node.getNodeName().equals("message")) {
                Element element = (Element) node;
                String sender = element.getAttribute("sender");
                String text = element.getAttribute("text");
                String chatId = element.getAttribute("chat_id");
                getMessages(chatId, text, sender);
            }
        }
    }

    /**
     * add/remove user from online list.
     *
     * @param login login
     * @param online online
     */
    public void changeOnlineUsers(String login, boolean online) {
        if (online) {
            if (!onlineUsers.contains(login)) onlineUsers.add(login);
            if (login.equals(currentUser) && !isAdmin()) sendMessage("@ Join chat", mainChatID);
            if (login.equals(currentUser) && isAdmin()) sendMessage("I AM ADMIN! I am in chat now!", mainChatID);
        } else {
            if (onlineUsers.contains(login)) onlineUsers.remove(login);
        }
        generalChatView.setOnlineUsersList(onlineUsers);
    }

    /**
     * open window to chose users to add.
     *
     * @param chatId chatId
     */
    public void addToPrivateChatSelect(String chatId) {
        OnlineUsersView onlineUsersView =
                new OnlineUsersView(this, "Select user to add to chat", "addToPrivateChat", chatId);
    }

    /**
     * open window to chose user to unban
     *
     * @param chatId chatId
     */
    public void unBanUserSelect(String chatId) {
        OnlineUsersView bannedUsersView = new OnlineUsersView(this, "Select user to unban", "unBanUser", chatId);
        bannedUsersView.setOnlineUsersList(banUsers);
    }

    /**
     * open window to chose user to ban
     *
     * @param chatId chatId
     */
    public void banUserSelect(String chatId) {
        OnlineUsersView onlineUsersView = new OnlineUsersView(this, "Select user to ban", "banUser", chatId);
    }

    /**
     * parse string with XML.
     *
     * @param value stringXML
     * @return document XML
     */

    private Document getXML(String value) {
        value = value.replaceAll("\\n", " ");
        Document document = null;
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(new InputSource(new StringReader(value)));
            document.getDocumentElement().normalize();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
            logger.error("Failed to read input XML", e);
        }
        return document;
    }

    /**
     * getter port.
     *
     * @return int port
     */
    public int getPort() {
        return port;
    }

    /**
     * set port.
     *
     * @param port port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * getter serverAddress
     *
     * @return String serverAddress
     */
    private String getServerAddress() {
        return serverAddress;
    }

    /**
     * set serverAddress.
     *
     * @param serverAddress serverAddress
     */
    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    /**
     * read port and server address from xml file or create new file.
     *
     * @return read config file successfully
     */
    private boolean readConfig() {
        String varPort = "12345";
        String varServerAddress = "localhost";
        boolean readSuccess = false;
        try {
            if (new File(configPath).exists()) {
                File file = new File(configPath);
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                Document document = documentBuilder.parse(file);
                document.getDocumentElement().normalize();
                NodeList config = document.getElementsByTagName("config");
                for (int i = 0; i < config.getLength(); i++) {
                    Node node = config.item(i);
                    if (node.getNodeName().equals("config")) {
                        Element element = (Element) node;
                        varPort = element.getElementsByTagName("PORT").item(0).getTextContent();
                        varServerAddress = element.getElementsByTagName("serverAddress").item(0).getTextContent();

                        setPort(Integer.parseInt(varPort));
                        setServerAddress(varServerAddress);
                        logger.info("File with config read without problems!");
                        readSuccess = true;
                    }
                }
            } else {
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document doc = docBuilder.newDocument();
                Element rootElement = doc.createElement("config");
                doc.appendChild(rootElement);
                Element portElement = doc.createElement("PORT");
                portElement.appendChild(doc.createTextNode(varPort));
                rootElement.appendChild(portElement);
                Element serverAddressElement = doc.createElement("serverAddress");
                serverAddressElement.appendChild(doc.createTextNode(varServerAddress));
                rootElement.appendChild(serverAddressElement);

                // write the content into xml file
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(new File(configPath));

                transformer.transform(source, result);

                setPort(Integer.parseInt(varPort));
                setServerAddress(varServerAddress);
                JOptionPane.showMessageDialog(null, "File with configuration saved! You can change settings in file: " + configPath);
                logger.info("File with config saved!");
                readSuccess = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("IO exception in reading file with config!");
        } catch (TransformerException e) {
            e.printStackTrace();
            logger.error("Transformer exception in reading file with config!");
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            logger.error("Parser exception in reading file with config!");
        } catch (SAXException e) {
            e.printStackTrace();
            logger.error("SAX exception in reading file with config!");
        } finally {
            return readSuccess;
        }
    }

    /**
     * prepare command get all users.
     */
    public void getAllUsers() {
        //<command type="online_users"></command>
        String msg = "<command type=\"all_users\"/>";
        sendXMLString(msg);
    }

    /**
     * open window to chose user to delete.
     *
     * @param chatId chatId
     */
    public void deleteUserSelect(String chatId) {
        getAllUsers();
        allUsersView = new OnlineUsersView(this, "Select user to delete", "deleteUser", chatId);
        allUsersView.setOnlineUsersList(allUsers);
    }

    /**
     * prepare command delete user.
     *
     * @param deleteUser deleteUser
     * @return command is sent
     */
    public boolean deleteUser(String deleteUser) {
        //<command type="deleteUser" login = "***"></command>
        String msg = String.format("<command type=\"deleteUser\" login = \"%s\"/>", deleteUser);
        return (sendXMLString(msg));
    }

    /**
     * open change password window.
     *
     * @param userLogin login
     */
    public void changePassWindow(String userLogin) {
        ChangePassView changePassView = new ChangePassView(this, userLogin);
    }

    /**
     * prepare command delete user.
     *
     * @param login login
     * @param password password
     */
    public void changePassword(String login, String password) {
        //<command type="deleteUser" login = "***"></command>
        String msg = String.format("<command type=\"changePassword\" login = \"%1$s\"  password =\"%2$s\" />", login, password);
        sendXMLString(msg);
        setCurrentUserPassword(password);
    }

    /**
     * open window to chose user to edit password.
     *
     * @param chatId chatId
     */
    public void editUserSelect(String chatId) {
        getAllUsers();
        allUsersView = new OnlineUsersView(this, "Select user to change password", "editUser", chatId);
        allUsersView.setOnlineUsersList(allUsers);
    }

}


