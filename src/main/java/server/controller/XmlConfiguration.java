package server.controller;
import server.model.*;

import com.thoughtworks.xstream.XStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.*;
import java.util.List;
public class XmlConfiguration {

    private XmlConfiguration() {
    }

    private static XmlConfiguration instance = new XmlConfiguration();

    public static XmlConfiguration getInstance() {
        return instance;
    }

    public String configuration(Command command) {
        Document document = getXML(command.getValue());
            NodeList nodes = document.getElementsByTagName("command");
            Element element = (Element) nodes.item(0);
            String type = element.getAttribute("type");
            switch (type) {
                case "all_users": {
                    return getListUsers();
                }
                case "online_users": {
                    return getOnlineListUsers();
                }
                case "сhats": {
                    return getChats();
                }
                case "get_messages": {
                    Long id = Long.parseLong(element.getAttribute("chat_id"));
                    return getMessages(id);
                }
                case "ban": {
                    String login = element.getAttribute("user");
                    Configuration.getInstance().ban(login);
                    return "<command type=\"ban\" result = \"ACCEPTED\"/>";
                }
                case "login" : {
                    String login = element.getAttribute("login");
                    String password = element.getAttribute("password");
                    String name = Configuration.getInstance().getUserName(login);
                    if(Configuration.getInstance().login(new User(login, password))){
                            Configuration.getInstance().setOnlineStatus(login, true);
                            return String.format("<command type=\"login\" result = \"ACCEPTED\" name= \"%s\" />", name);
                    }else{
                        return "<command type=\"login\" result = \"NOTACCEPTED\"/>";
                    }
                }
                case "registration": {
                    String login = element.getAttribute("login");
                    String password = element.getAttribute("password");
                    String name = element.getAttribute("name");
                   if(Configuration.getInstance().register(new User(login, password, name, true, false))) {
                       return "<command type=\"registration\" result = \"NOTACCEPTED\" />";
                   }else {
                       return "<command type=\"registration\" result = \"ACCEPTED\" />";
                   }
                }
                case "newChatID": {
                    long id = Configuration.getInstance().createChat();
                    return String.format("<command type=\"newChatID\" chat_id=\"%s\" />", id);
                }
            }
        return "";
    }

    private String getMessages(long id) {
        XStream xstream = new XStream();
        xstream.alias("message", Message.class);
        xstream.useAttributeFor(Message.class, "sender");
        xstream.useAttributeFor(Message.class, "text");
        xstream.aliasField("sender", Message.class, "sender");
        xstream.aliasField("text", Message.class, "text");
        xstream.alias("messages", List.class);
        List<Message>list = Configuration.getInstance().getMessages(id);
        return xstream.toXML(list);
    }

    private String getChats() {
        XStream xstream = new XStream();
        xstream.alias("chats", List.class);
        xstream.alias("chat", Long.class);
        List<Long> list = Configuration.getInstance().getChats();
        return xstream.toXML(list);
    }

    private String getListUsers() {
        XStream xstream = new XStream();
        xstream.alias("users", List.class);
        xstream.alias("user", String.class);
        final List<String> list = Configuration.getInstance().getListUsers();
        return xstream.toXML(list);
    }

    private String getOnlineListUsers() {
        XStream xstream = new XStream();
        xstream.alias("users", List.class);
        xstream.alias("user", String.class);
        final List<String> list = Configuration.getInstance().getOnlineListUsers();
        return xstream.toXML(list);
    }

    private Document getXML(String value){
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
