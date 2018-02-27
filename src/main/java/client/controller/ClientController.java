package client.controller;

import client.view.*;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

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


public class ClientController extends Thread {
    private static final Logger logger = Logger.getLogger(ClientController.class);
    private int PORT = 1506;
    private String hostName = "localhost";
    private Socket connect;
    private ArrayList<String> onlineUsers = new ArrayList<>();
    private InputStream in;
    private OutputStream out;
    private List<String> banUsers;
    private String currentUser;
    private boolean isConnected;
    private GeneralChatView generalChatView;
    private AdminView adminView;
    private LinkedHashMap<Integer, PrivateChatView> privateChatsList;


    public ClientController() {
        //LoginView LoginView = new LoginView(this);
        generalChatView = new GeneralChatView(this, "Main chat");
        PrivateChatView PrivateChatView = new PrivateChatView(this);
        adminView = new AdminView(this);
        //OnlineUsersView OnlineUsersView = new OnlineUsersView(this, "Users");
    }

    public void openRegistrationView(String login, String password){
        RegistrationView registrationView = new RegistrationView(this);
        registrationView.setLoginPassword(login, password);
    }

    public boolean validateUser(String login, String password){
        System.out.println("User validation "+" "+ login +" "+ password);

        try {
            Document doc = newDocXML();
            Element command = doc.createElement("command");
            command.setAttribute("type", "login");
            command.setAttribute("login", login);
            command.setAttribute("password", password);
            doc.appendChild(command);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
            System.out.println(output);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean newUserRegistration(String login, String nickName, String password){
        System.out.println("User reg "+" "+ nickName +" "+ password);
        try {
            Document doc = newDocXML();
            Element command = doc.createElement("command");
            command.setAttribute("type", "registration");
            command.setAttribute("login", login);
            command.setAttribute("name", nickName);
            command.setAttribute("password", password);
            doc.appendChild(command);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
            System.out.println(output);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        return true;
    }


    private Document newDocXML() throws ParserConfigurationException {
        DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder icBuilder;
        icBuilder = icFactory.newDocumentBuilder();
        Document doc = icBuilder.newDocument();
        return doc;
    }

    public boolean connectToServer() {
        try {
            connect = new Socket(hostName, PORT);
            logger.info("Connected: " + connect);

        } catch (UnknownHostException e) {
            logger.error("Host unknown: ",e);
            return false;
        } catch (IOException e) {

            return false;
        }

        return true;

    }

    public void exitChat() {
        try {

        } catch (Exception e) {

            logger.error("Exception close: ",e);}

    }

    public synchronized void  getMessages() {

    }

    public String getCurrentUser() {
        currentUser = "my_nick";
        return currentUser;
    }

    public boolean sendMessage(String message, String chatID) {
        //<message sender = *** chat = *** text = ***/>
        System.out.println("New massage general chat "+ message);
        try {
            Document doc = newDocXML();
            Element command = doc.createElement("message");
            command.setAttribute("sender", getCurrentUser());
            command.setAttribute("chat", chatID);
            command.setAttribute("text", message);
            doc.appendChild(command);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
            System.out.println(output);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        generalChatView.printNewMassage(getCurrentUser()+": "+message);

        return true;
    }

    public boolean banUser(String banedUser) {
        //<command type="ban" user = "***"></command>
        System.out.println("ban "+ banedUser);
        try {
            Document doc = newDocXML();
            Element command = doc.createElement("command");
            command.setAttribute("type", "ban");
            command.setAttribute("user", banedUser);
            doc.appendChild(command);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
            System.out.println(output);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        return true;

    }

    public boolean unBanUser(String unBanUser) {
        //<command type="unban" user = "***"></command>
        System.out.println("unBanUser  "+ unBanUser);
        try {
            Document doc = newDocXML();
            Element command = doc.createElement("command");
            command.setAttribute("type", "unban");
            command.setAttribute("user", unBanUser);
            doc.appendChild(command);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
            System.out.println(output);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        return true;

    }



    @Override
    public void run() {
        //while (true) {
          //  if (close) return;
           // getMessage();

        //}
    }


    public void createView() {

    }

    public ArrayList<String> getOnlineUserslist() {
        onlineUsers.clear();
        onlineUsers.add("Marta");
        onlineUsers.add("Anna");
        onlineUsers.add("Kate");
        onlineUsers.add("Lili");
//<command type="online_users"></command>
        try {
            Document doc = newDocXML();
            Element command = doc.createElement("command");
            command.setAttribute("type", "online_users");
            doc.appendChild(command);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
            System.out.println(output);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        return onlineUsers;
    }

    public void createPrivateChatSelect() {
        OnlineUsersView OnlineUsersView = new OnlineUsersView(this, "Select users to chat", "CreatePrivateChat");
    }
    public void unBanUserSelect() {
        OnlineUsersView OnlineUsersView = new OnlineUsersView(this, "Select user to unban", "CreatePrivateChat");
    }
    public void banUserSelect() {
        OnlineUsersView OnlineUsersView = new OnlineUsersView(this, "Select user to ban", "CreatePrivateChat");
    }

    public static void main(String[] args) throws IOException, SAXException {
        ClientController client = new ClientController();
       // client.createView();
       // client.run();
    }


}

