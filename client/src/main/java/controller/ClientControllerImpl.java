package controller;

import view.*;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
import java.util.*;
import java.util.concurrent.TimeUnit;
import model.XmlConfiguration;


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
    private final XmlConfiguration xml = XmlConfiguration.getInstance();

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
                String type = XmlConfiguration.getTypeOfTheCommand(line);
                String result = XmlConfiguration.getAttributeResult(line);
                if (type.equals("login")) {
                    if (result.equals("ACCEPTED")) {
                        boolean isAdminString = Boolean.parseBoolean(XmlConfiguration.getValue(line,"isAdmin"));
                        setAdmin(isAdminString);
                        isBanned = Boolean.parseBoolean(XmlConfiguration.getValue(line,"isInBan"));
                        break;
                    } else if (result.equals("NOTACCEPTED")) {
                        JOptionPane.showMessageDialog(null, "Incorrect login/password");
                        loginView = new LoginView(this);
                    }
                } else if (type.equals("registration")) {
                    if (result.equals("ACCEPTED")) {
                        boolean isAdminString = Boolean.parseBoolean(XmlConfiguration.getValue(line,"isAdmin"));
                        setAdmin(isAdminString);
                        break;
                    } else if (result.equals("NOTACCEPTED")) {
                        JOptionPane.showMessageDialog(null, "Login is alredy used!");
                        loginView = new LoginView(this);
                    }
                }
            } catch (IOException e) {
                logger.error("Failed to read input stream for access!");
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
                if (line.equals("</messages>") || line.equals("<messages/>")) {
                    varLoadMessages = true;
                    continue;
                }
                if (line.equals("</onlineUsers>") || line.equals("<onlineUsers/>")) {
                    varGetOnlineUsers = true;
                    continue;
                }

                String nodeName = xml.getNodeNameFromXML(line);
                Element element = xml.getElementFromXML(line);
                if (element == null) {
                    if (nodeName.equals("onlineUsers")) {
                        setOnlineUsers(line);
                        varGetOnlineUsers = true;
                    }
                    if (nodeName.equals("users")) {
                        setAllUsers(line);
                    }
                    if (nodeName.equals("messages")) {
                        setAllMassages(line);
                        varLoadMessages = true;
                    }
                    if (nodeName.equals("chats")) {
                        setChats(line);
                        varSetChatList = true;
                    }
                    if (nodeName.equals("banList")) {
                        setBanList(line);
                        varSetBanList = true;
                    }
                    continue;
                }

                String type = XmlConfiguration.getTypeOfTheCommand(line);

                switch (type) {
                    case "setOnlineStatus": {
                        String login = xml.getUserFromMessage(line);
                        boolean online = xml.getOnlineStatus(line);
                        changeOnlineUsers(login, online);
                        break;
                    }
                    case "ban": {
                        String login = xml.getLogin(line);
                        banUserConfirm(login);
                        break;
                    }
                    case "unban": {
                        String login = xml.getLogin(line);
                        unBanUserConfirm(login);
                        break;
                    }
                    case "newChatID": {
                        String chatId = XmlConfiguration.getValue(line,"chat_id");
                        String login = xml.getUserFromMessage(line);
                        openPrivateChat(login, chatId, false);
                        break;
                    }
                    case "addToChat": {
                        String chatId = XmlConfiguration.getValue(line,"chat_id");
                        String login = xml.getLogin(line);
                        openPrivateChat(login, chatId, true);
                        break;
                    }
                    case "addMessage": {
                        String sender = xml.getSender(line);
                        String chatId = XmlConfiguration.getValue(line,"chat_id");
                        String text = xml.getText(line);
                        getMessages(chatId, text, sender);
                        break;
                    }
                    case "isInBan": {
                        String login = xml.getUserFromMessage(line);
                        break;
                    }
                    case "deleteUser": {
                        String result = XmlConfiguration.getAttributeResult(line);
                        if (result.equals("ACCEPTED")) {
                            String login = xml.getLogin(line);
                            deleteUserConfirm(login);
                            break;
                        }
                        break;
                    }
                    case "changePassword": {
                        String result = XmlConfiguration.getAttributeResult(line);
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
           logger.error("Wait for restart failed ", e);
        }
        isConnected = connectServer();
        if (isConnected) {
            validateUser(getCurrentUser(), getCurrentUserPassword());
            readEnterToChat();
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
     *  user is deleted.
     *
     * @param login login
     */
    private void deleteUserConfirm(String login) {
        if (login.equals(getCurrentUser())) {
            logger.info("Admin delete you from chat");
            Object[] options = {"OK", "CANCEL"};
            int n = JOptionPane
                    .showOptionDialog(null, "Admin delete you from chat! Application will be closed",
                            "Confirmation", JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE, null, options,
                            options[0]);

                exitChat();
                System.exit(2);

        }
        if (isAdmin()) {
            JOptionPane.showMessageDialog(null, "User has been deleted!");
            sendMessage("@ADMIN has delete ".concat(login), mainChatID);
        }
        changeOnlineUsers(login, false);
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
        xml.setUserListFromXML(line, onlineUsers);
        generalChatView.setOnlineUsersList(onlineUsers);
    }

    /**
     * parse input xml and set all users list.
     *
     * @param line xml string
     */
    public void setAllUsers(String line) {
        //<users>   <user>qwerty</user> </users>
        xml.setUserListFromXML(line, allUsers);
    }

    /**
     * parse input xml and set all users list.
     *
     * @param line xml string
     */
    public void setBanList(String line) {
        //<banList>   <user>qwerty</user> </banList>
        xml.setUserListFromXML(line, banUsers);
        generalChatView.setBannedList(banUsers);
    }

    /**
     * parse input xml and set private chats.
     *
     * @param line xml string
     */
    public void setChats(String line) {
        //<chats>   <long>0</long>   <long>1</long> </chats>
        chatsListInForm.clear();
        xml.setChatsListFromXML(line, chatsListInForm);
        generalChatView.setPrivateChatsList(chatsListInForm);
    }

    /**
     * parse input xml and set massages
     *
     * @param line xml string
     */
    public void setAllMassages(String line) {
        //<messages>   <message chat_id="0" sender="q" text="ff"/>
        ArrayList<HashMap<String,String>> listMassages = xml.getAllMassages(line);
        for ( HashMap<String, String> map:listMassages) {
            Set<Map.Entry<String, String>> set = map.entrySet();
            String chatId = "", text = "", sender = "";
                for (Map.Entry<String, String> me : set) {
                    if(me.getKey().equals("chatId")) chatId = me.getValue();
                    if(me.getKey().equals("sender")) sender = me.getValue();
                    if(me.getKey().equals("text")) text = me.getValue();
                }
            getMessages(chatId, text, sender);
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
            logger.error("IO exception in reading file with config!");
        } catch (TransformerException e) {
            logger.error("Transformer exception in reading file with config!");
        } catch (ParserConfigurationException e) {
            logger.error("Parser exception in reading file with config!");
        } catch (SAXException e) {
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


