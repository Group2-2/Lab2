package server.model;

import com.thoughtworks.xstream.XStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.security.auth.login.Configuration;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.List;

public class XmlConfiguration {

    private XmlConfiguration() {
    }

    private static XmlConfiguration instance = new XmlConfiguration();
    private static ModelImpl model = ModelImpl.getInstance();

    public static XmlConfiguration getInstance() {
        return instance;
    }

    public String configuration(String command) {
        Document document = newDocument(command);
        NodeList nodes = document.getElementsByTagName("command");
        Element element = (Element) nodes.item(0);
        String type = element.getAttribute("type");
        switch (type) {
            case "all_users": {
                return listUserToXml(model.getListUsers());
            }
            case "online_users": {
                return listUserToXml(model.getOnlineListUsers());
            }
            case "сhats": {
                return getChats();
            }
            case "get_messages": {
                long id = Long.parseLong(element.getAttribute("chat_id"));
                return getMessages(id);
            }
            case "get_chat_users": {
                long id = Long.parseLong(element.getAttribute("chat_id"));
                return listUserToXml(model.getChatUsers(id));
            }
            case "ban": {
                String login = element.getAttribute("user");
                model.ban(login);
                return "<command type=\"ban\" result = \"ACCEPTED\"/>"; //return
            }
            case "unban": {
                String login = element.getAttribute("user");
                model.unban(login);
                return "<command type=\"unban\" result = \"ACCEPTED\"/>"; //return
            }
            case "login" : {
                String login = element.getAttribute("login");
                String password = element.getAttribute("password");
                String name = model.getUserName(login);
                if(model.login(new User(login, password, name, true, false))){
                    return String.format("<command type=\"login\" result = \"ACCEPTED\" name= \"%s\" isAdmin = \"%s\" />", name, model.isAdmin(login));
                } else {
                    return "<command type=\"login\" result = \"NOTACCEPTED\"/>";
                }
            }
            case "registration": {
                String login = element.getAttribute("login");
                String password = element.getAttribute("password");
                String name = element.getAttribute("name");
                if(!model.register(new User(login, password, name, true, false))) {
                    return "<command type=\"registration\" result = \"NOTACCEPTED\" />";
                } else {
                    return String.format("<command type=\"registration\" isAdmin = \"%s\" result = \"ACCEPTED\" />", model.isAdmin(login));
                }
            }
            case "newChatID": {
                String login = element.getAttribute("sender");
                long id = model.createChat();
                model.addToChat(login, id);
                return String.format("<command type=\"newChatID\" chat_id=\"%s\" />", id);
            }
            case "addToChat": {
                String login = element.getAttribute("login");
                long id = Long.parseLong(element.getAttribute("chat_id"));
                model.addToChat(login, id);
                return command;
            }
            case "addMessage": {
                String login = element.getAttribute("sender");
                long id = Long.parseLong(element.getAttribute("chat_id"));
                String text = element.getAttribute("text");
                model.addMessage(id, new Message(login, text));
                return command;
            }
            case "setOnlineStatus": {
                boolean online = Boolean.parseBoolean(element.getAttribute("isOnline"));
                String login = element.getAttribute("user");
                model.setOnlineStatus(login, online);
                return command;
            }
            case "createAdmin": {
                String login = element.getAttribute("user");
                model.createAdmin(login);
                return command;
            }
            case "deleteAdmin": {
                String login = element.getAttribute("user");
                model.deleteAdmin(login);
                return command;
            }
            case "isInBan": {
                String login = element.getAttribute("user");
                model.deleteAdmin(login);
                return String.format("<command type=\"isInBan\" isInBan=\"%s\" />", model.isInBan(login)) ;
            }
            case "getUserName": {
                String login = element.getAttribute("user");
                return String.format("<command type=\"getUserName\" name=\"%s\" />", model.getUserName(login)) ;
            }
            default: {
                return command;
            }

        }
    }

    private static String getMessages(long id) {
        XStream xstream = new XStream();
        xstream.alias("message", Message.class);
        xstream.useAttributeFor(Message.class, "sender");
        xstream.useAttributeFor(Message.class, "text");
        xstream.aliasField("sender", Message.class, "sender");
        xstream.aliasField("text", Message.class, "text");
        xstream.alias("messages", List.class);
        List<Message>list = model.getMessages(id);
        return xstream.toXML(list);
    }

    private static String getChats() {
        XStream xstream = new XStream();
        xstream.alias("chats", List.class);
        xstream.alias("chat", Long.class);
        List<Long> list = model.getChats();
        return xstream.toXML(list);
    }

    private static String listUserToXml(List<String> list) {
        XStream xstream = new XStream();
        xstream.alias("users", List.class);
        xstream.alias("user", String.class);
        return xstream.toXML(list);
    }

    public static Document newDocument(String value){
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
