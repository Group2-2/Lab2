package model;

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
import java.util.Map;

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
    /*public String configuration(String command) {
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
*/
    public static String getMessages(long id) {
        XStream xstream = new XStream();
        xstream.alias("message", Message.class);
        xstream.useAttributeFor(Message.class, "sender");
        xstream.useAttributeFor(Message.class, "text");
        xstream.aliasField("sender", Message.class, "sender");
        xstream.aliasField("text", Message.class, "text");
        xstream.alias("messages", List.class);
        List<Message>list = model.getMessages(id);
        String s =  xstream.toXML(list).replaceAll("\\n", " ")
                .replaceAll("message ", String.format("message chat_id=\"%s\" ", "" + id));
        System.out.println(s);
        return s;
    }

    public static String getChats(String login) {
        XStream xstream = new XStream();
        xstream.alias("chats", List.class);
        xstream.alias("chat", Long.class);
        List<Long> list = model.getChats(login);
        return xstream.toXML(list).replaceAll("\\n", " ");
    }

    public static String listUserToXml(List<String> list, String aliasList) {
        XStream xstream = new XStream();
        xstream.alias(aliasList, List.class);
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

    private static String getValue(String command, String attribute) {
        Document document = newDocument(command);
        NodeList nodes = document.getElementsByTagName("command");
        Element element = (Element) nodes.item(0);
        return element.getAttribute(attribute);
    }

    /**
     * Method for get type of command
     * @param command command from user
     * @return type of command
     */
    public static String getTypeOfTheCommand(String command) {
        return getValue(command, "type");
    }

    /**
     * Method for get sender of command
     * @param command command from user
     * @return sender of command
     */
    public String getSender(String command) {
        return getValue(command, "sender");
    }

    /**
     * Method for get chat id of command
     * @param command command from user
     * @return chat id of command
     */
    public Long getChatId(String command) {
        return Long.parseLong(getValue(command, "chat_id"));
    }

    /**
     * Method for get user of command
     * @param command command from user
     * @return user of command
     */
    public String getUserFromMessage(String command) {
        return getValue(command, "user");
    }

    /**
     * Method for get password of command
     * @param command command from user
     * @return password of command
     */
    public String getPassword(String command) {
        return getValue(command, "password");
    }

    /**
     * Method for get login of command
     * @param command command from user
     * @return login of command
     */
    public String getLogin(String command) {
        return getValue(command, "login");
    }

    /**
     * Method for get name of command
     * @param command command from user
     * @return name of command
     */
    public String getName(String command) {
        return getValue(command, "name");
    }

    /**
     * Method for online status from command
     * @param command command from user
     * @return online status from command
     */
    public boolean getOnlineStatus(String command) {
        return Boolean.parseBoolean(getValue(command, "isOnline"));
    }

    /**
     * Method for get text from command
     * @param command command from user
     * @return text of command
     */
    public String getText(String command) {
        return getValue(command, "text");
    }

    /**
     * Method for create response answer
     * @param command name of command
     * @param attributes dictionary of attribute
     * @return new xml response
     */
    public String command(String command, Map<String, Object> attributes) {
        StringBuilder builder = new StringBuilder();
        String value = "";
        if (attributes != null) {
            for (Map.Entry<String, Object> item: attributes.entrySet()) {
                String buf = String.format("%s=\"%s\"", item.getKey(), item.getValue());
                builder.append(buf).append(" ");
            }
            value = builder.toString().trim();
        }

        return String.format("<command type=\"%s\" %s />", command, value);
    }
}
