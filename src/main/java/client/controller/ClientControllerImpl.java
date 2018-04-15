package client.controller;

        import client.view.*;
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
    private ArrayList<String> chatsListInForm = new ArrayList<>();
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
    private Thread thread;

    /**
     * @param args
     * @throws IOException
     * @throws SAXException
     */
    public static void main(String[] args) {
        ClientControllerImpl client = new ClientControllerImpl();
        client.run();
    }

    /**
     * Start new chat application
     */
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
          /*  //test
            String msg = "<command type=\"createAdmin\" user = \"Sviat\"/>";
            sendXMLString(msg);
            //*/

            if (isAdmin()) {
                generalChatView = new AdminView(this);
            } else {
                generalChatView = new GeneralChatView(this, "Main chat");
                generalChatView.blockBanedUser(isBanned);
            }

            readInputStream();

            try {
                in.close();
                out.close();
            } catch (Exception e) {
                logger.info("Потоки не были закрыты!");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Server not found!");
        }
    }

    /**
     * get in out streams
     *
     * @return boolean successful connected
     */
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


    /**
     * start reading input stream massages
     */
    public void readInputStream() {
        boolean varGetOnlineUsers = false;
        boolean varSendOnlines = false;
        boolean varLoadMessages = false;
        boolean varSetChatList = false;
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
                String line = in.readLine();
                System.out.println("Get in line " + line);
                if (line.equals("</messages>") || line.equals("<messages/>")) {
                    line = in.readLine();
                    varLoadMessages = true;
                }
                Document document = getXML(line);
                NodeList nodes = document.getElementsByTagName("command");
                Element element = (Element) nodes.item(0);
                if (element == null) {
                    if (document.getDocumentElement().getNodeName().equals("users")) {
                        setOnlineUsers(line);
                        varGetOnlineUsers = true;
                    }
                    if (document.getDocumentElement().getNodeName().equals("messages")) {
                        setAllMassages(line);
                        varLoadMessages = true;
                    }
                    if (document.getDocumentElement().getNodeName().equals("chats")) {
                        setChats(line);
                        varSetChatList = true;
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
                        String login = element.getAttribute("user");
                        banUserConfirm(login);
                        break;
                    }
                    case "unban": {
                        String login = element.getAttribute("user");
                        unBanUserConfirm(login);
                        break;
                    }
                    case "newChatID": {
                        String chat_id = element.getAttribute("chat_id");
                        String login = element.getAttribute("user");
                        openPrivateChat(login, chat_id);
                        break;
                    }
                    case "addToChat": {
                        String chat_id = element.getAttribute("chat_id");
                        String login = element.getAttribute("login");
                        openPrivateChat(login, chat_id);
                        break;
                    }
                    case "addMessage": {
                        String sender = element.getAttribute("sender");
                        String chat_id = element.getAttribute("chat_id");
                        String text = element.getAttribute("text");
                        getMessages(chat_id, text, sender);
                        break;
                    }
                    case "isInBan": {
                        String login = element.getAttribute("user");
                        break;
                    }
                }
            }
        } catch (IOException e) {
            logger.info("Ошибка при получении сообщения!");
            e.printStackTrace();
        }
    }

    /**
     * @param login
     * @param password
     */
    public void openRegistrationView(String login, String password) {
        RegistrationView registrationView = new RegistrationView(this);
        registrationView.setLoginPassword(login, password);
    }

    /**
     * @param login
     * @param chat_id
     * @return
     */
    public boolean openPrivateChat(String login, String chat_id) {
        if (login.equals(getCurrentUser()) && (!chatsListInForm.contains(chat_id))) {
            chatsListInForm.add(chat_id);
            generalChatView.setPrivateChatsList(chatsListInForm);
        }
        if (login.equals(getCurrentUser()) && (!privateChatsList.containsKey(chat_id))) {
            PrivateChatView privateChatView = new PrivateChatView(this);
            privateChatView.setChat_id(chat_id);
            // sendMessage("@ Join chat", chat_id);
            getMassagesInChat(chat_id);
            privateChatsList.put(chat_id, privateChatView);
            privateChatView.setPrivateChatsList(chatsListInForm);
        }
        return true;
    }

    /**
     * close main chat window
     */
    public void exitChat() {
        sendOnline("false");
        exitApp();
    }

    /**
     * close in out streams
     */
    public void exitApp() {
        try {
            isConnected = false;
            in.close();
            out.close();
            thread.interrupt();
        } catch (Exception e) {
            logger.error("Chat exit failed! ", e);
        }
    }

    /**
     * close private chat window
     *
     * @param chat_id
     */
    public void leavePrivateChat(String chat_id) {
        if (privateChatsList.containsKey(chat_id)) {
            sendMessage("@ Has left chat", chat_id);
            privateChatsList.remove(chat_id);
        }
    }

    /**
     * print input massage into wright window
     *
     * @param chat_id
     * @param text
     * @param sender
     */
    public synchronized void getMessages(String chat_id, String text, String sender) {
        String massage = sender.concat(": ").concat(text);
        if (chat_id.equals(mainChatID)) {
            generalChatView.printNewMassage(massage);
        } else if (privateChatsList.containsKey(chat_id)) {
            PrivateChatView privateChatView = privateChatsList.get(chat_id);
            privateChatView.printNewMassage(massage);
        }
    }

    /**
     * input stream user is banned
     *
     * @param login
     */
    private void banUserConfirm(String login) {
        if (login.equals(getCurrentUser())) {
            isBanned = true;
            generalChatView.blockBanedUser(isBanned);
        }
        sendMessage("@ADMIN has banned ".concat(login), mainChatID);
    }

    /**
     * input stream user is unbanned
     *
     * @param login
     */
    private void unBanUserConfirm(String login) {
        if (login.equals(getCurrentUser())) {
            isBanned = false;
            generalChatView.blockBanedUser(isBanned);
        }
        sendMessage("@ADMIN has Unbanned ".concat(login), mainChatID);
    }

    /**
     * getter current user login
     *
     * @return currentUser
     */
    public String getCurrentUser() {
        return currentUser;
    }

    /**
     * set current user login
     *
     * @param currentUser
     */
    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    /**
     * @return isAdmin
     */
    public boolean isAdmin() {
        return isAdmin;
    }

    /**
     * @param admin
     */
    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    /**
     * @return mainChatID
     */
    public static String getMainChatID() {
        return mainChatID;
    }

    /**
     * prepare massage for output stream
     *
     * @param message
     * @param chatID
     * @return success
     */
    public boolean sendMessage(String message, String chatID) {
        //<command type="addMessage" sender="my_nick" chat_id = "0" text ="dsfaf"/>
        String msg = String.format("<command type=\"addMessage\" sender=\"%1$s\" chat_id = \"%2$s\" text =\"%3$s\"/>", getCurrentUser(), chatID, message.replaceAll("\\n", " "));
        return (sendXMLString(msg));
    }

    /**
     * prepare command register new user
     *
     * @param login
     * @param nickName
     * @param password
     * @return success
     */
    public boolean registerNewUser(String login, String nickName, String password) {
        //<addMessage sender = * chat_id = * text = ***/>
        String msg = String.format("<command type=\"registration\" login=\"%1$s\" name = \"%2$s\" password =\"%3$s\"/>", login, nickName, password);
        setCurrentUser(login);
        return (sendXMLString(msg));
    }

    /**
     * prepare command user is online
     *
     * @param isOnline
     */
    public void sendOnline(String isOnline) {
        //<addMessage sender = * chat_id = * text = ***/>
        String msg = String.format("<command type=\"setOnlineStatus\" user=\"%1$s\" isOnline = \"%2$s\"/>", getCurrentUser(), isOnline);
        sendXMLString(msg);
    }

    /**
     * prepare command get chats list
     */
    public void getChatsList() {
        //<addMessage sender = * chat_id = * text = ***/>
        String msg = String.format("<command type=\"chats\" sender=\"%1$s\"/>", getCurrentUser());
        sendXMLString(msg);
    }

    /**
     * prepare command check user login-password
     *
     * @param login
     * @param password
     * @return
     */
    public boolean validateUser(String login, String password) {
        //<command type="login" login="log1" password ="pass1"/>
        String msg = String.format("<command type=\"login\" login=\"%1$s\" password =\"%2$s\"/>", login, password);
        setCurrentUser(login);
        return (sendXMLString(msg));
    }

    /**
     * prepare command generate new chat ID
     *
     * @return
     */
    public boolean createPrivateChat() {
        //<command type="newChatID" sender = "sender"/>
        String msg = String.format("<command type=\"newChatID\" sender = \"%s\"/>", getCurrentUser());
        return (sendXMLString(msg));
    }

    /**
     * prepare command add user to private chat window
     *
     * @param login
     * @param chat_id
     */
    public void addToPrivateChat(String login, String chat_id) {
        //<command type="addToChat" chat_id = "0" user = "***" />
        String msg = String.format("<command type=\"addToChat\" chat_id = \"%s\" login = \"%s\"/>", chat_id, login);
        sendXMLString(msg);
    }

    /**
     * prepare command banned user
     *
     * @param banedUser
     * @return command is sent
     */
    public boolean banUser(String banedUser) {
        //<command type="ban" user = "***"></command>
        String msg = String.format("<command type=\"ban\" user = \"%s\"/>", banedUser);
        return (sendXMLString(msg));
    }

    /**
     * prepare command unbanned user
     *
     * @param unBanUser
     * @return command is sent
     */
    public boolean unBanUser(String unBanUser) {
        //<command type="unban" user = "***"></command>
        String msg = String.format("<command type=\"unban\" user = \"%s\"/>", unBanUser);
        return (sendXMLString(msg));
    }

    /**
     * prepare command get chats
     *
     * @return command is sent
     */
    public boolean getChatList() {
        //<command type="сhats" sender = "***"></command>
        String msg = String.format("<command type=\"сhats\" sender = \"%s\"/>", getCurrentUser());
        return (sendXMLString(msg));
    }

    /**
     * prepare command get all online users
     */
    public void getOnlineUsers() {
        //<command type="online_users"></command>
        String msg = "<command type=\"online_users\"/>";
        sendXMLString(msg);
    }

    /**
     * prepare command get messages in chat
     */
    public void getMassagesInChat(String ChatID) {
        String msg = String.format("<command type=\"get_messages\" chat_id=\"%s\"/>", ChatID);
        sendXMLString(msg);
    }

    /**
     * sent massage/command to output stream
     *
     * @param xmlText
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
     * parse input xml and set online users to frames
     *
     * @param line
     */
    public void setOnlineUsers(String line) {
        //<users>   <user>qwerty</user> </users>
        onlineUsers.clear();
        Document document = getXML(line);
        NodeList users = document.getElementsByTagName("user");
        for (int i = 0; i < users.getLength(); i++) {
            Node node = users.item(i);
            if (node.getNodeName().equals("user")) {
                Element element = (Element) node;
                String nicknameVar = element.getTextContent();
                onlineUsers.add(nicknameVar);
            }
        }
        generalChatView.setOnlineUsersList(onlineUsers);
    }

    /**
     * parse input xml and set private chats
     *
     * @param line
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
     * @param line
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
                String chat_id = element.getAttribute("chat_id");
                getMessages(chat_id, text, sender);
            }
        }
    }

    /**
     * add/remove user from online list
     *
     * @param login
     * @param online
     */
    public void changeOnlineUsers(String login, boolean online) {
        if (online) {
            if (!onlineUsers.contains(login)) onlineUsers.add(login);
            if (login.equals(currentUser)) sendMessage("@ Join chat", mainChatID);
        } else {
            if (onlineUsers.contains(login)) onlineUsers.remove(login);
            if (login.equals(currentUser)) sendMessage("@ Has left chat", mainChatID);
        }
        generalChatView.setOnlineUsersList(onlineUsers);
    }

    /**
     * open window to chose users to add
     *
     * @param chat_id
     */
    public void addToPrivateChatSelect(String chat_id) {
        OnlineUsersView OnlineUsersView = new OnlineUsersView(this, "Select user to add to chat", "addToPrivateChat", chat_id);
    }

    /**
     * open window to chose user to unban
     *
     * @param chat_id
     */
    public void unBanUserSelect(String chat_id) {
        OnlineUsersView OnlineUsersView = new OnlineUsersView(this, "Select user to unban", "unBanUser", chat_id);
    }

    /**
     * open window to chose user to ban
     *
     * @param chat_id
     */
    public void banUserSelect(String chat_id) {
        OnlineUsersView OnlineUsersView = new OnlineUsersView(this, "Select user to ban", "banUser", chat_id);
    }

    /**
     * parse string with XML
     *
     * @param value
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
        }
        return document;
    }
}


