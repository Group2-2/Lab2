package server.model;

import com.thoughtworks.xstream.XStream;
import org.apache.log4j.Logger;
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

/**
 * Class for parse xml command and configuration server
 * Class is singletone
 */
public class XmlConfiguration {

    private XmlConfiguration() {
    }

    private static XmlConfiguration instance = new XmlConfiguration();
    private static Model model = ModelImpl.getInstance();
    private static final Logger logger = Logger.getLogger(XmlConfiguration.class);

    public static XmlConfiguration getInstance() {
        return instance;
    }

    /**
     * Method for configuration server
     * @param command xml command
     * @return answer from server
     */
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
            case "chats": {
                String login = element.getAttribute("sender");
                return getChats(login);
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
                return String.format("<command type=\"ban\" user = \"%s\" result = \"ACCEPTED\"/>", login);
            }
            case "unban": {
                String login = element.getAttribute("user");
                model.unban(login);
                return String.format("<command type=\"unban\" user = \"%s\" result = \"ACCEPTED\"/>", login);
            }
            case "login" : {
                String login = element.getAttribute("login");
                String password = element.getAttribute("password");
                if(model.login(new User(login, password, ""))){
                    String name = model.getUserName(login);
                    return String.format("<command type=\"login\" result=\"ACCEPTED\" name=\"%s\" isAdmin=\"%s\" isInBan=\"%s\" />",
                            name, model.isAdmin(login), model.isInBan(login));
                } else {
                    return "<command type=\"login\" result = \"NOTACCEPTED\"/>";
                }
            }
            case "registration": {
                String login = element.getAttribute("login");
                String password = element.getAttribute("password");
                String name = element.getAttribute("name");
                if(!model.register(new User(login, password, name))) {
                    return "<command type=\"registration\" result =\"NOTACCEPTED\" />";
                } else {
                    return String.format("<command type=\"registration\" name=\"%s\" result=\"ACCEPTED\" />", name);
                }
            }
            case "newChatID": {
                String login = element.getAttribute("sender");
                long id = model.createChat();
                model.addToChat(login, id);
                return String.format("<command type=\"newChatID\" chat_id=\"%s\" user = \"%s\" />", id, login);
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
                logger.warn("Command not found " + command);
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
        return xstream.toXML(list).replaceAll("\\n", " ")
                .replaceAll("message", String.format("message chat_id=\"%s\"", "" + id));
    }

    private static String getChats(String login) {
        XStream xstream = new XStream();
        xstream.alias("chats", List.class);
        xstream.alias("chat", Long.class);
        List<Long> list = model.getChats(login);
        return xstream.toXML(list).replaceAll("\\n", " ");
    }

    private static String listUserToXml(List<String> list) {
        XStream xstream = new XStream();
        xstream.alias("users", List.class);
        xstream.alias("user", String.class);
        return xstream.toXML(list).replaceAll("\\n", " ");
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
