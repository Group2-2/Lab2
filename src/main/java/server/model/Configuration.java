package server.model;

import com.thoughtworks.xstream.XStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class Configuration {

    private Map<String, List<Message>> chats;
    private Map<String, List<User>> group;
    private List<User> listUsers;
    private Admin admin = null;
    private static XStream xstream = new XStream();

    private Configuration() {
        admin = read(FilePath.ADMIN.getPath());
        if (admin == null) {
            admin = new Admin("admin", "pass", "1");
        }
        chats = read(FilePath.CHATS.getPath());
        if (chats == null) {
            chats = new Hashtable<>();
            writeObject(chats, FilePath.CHATS.getPath());
        }
        listUsers = read(FilePath.LIST_USER.getPath());
        if (listUsers == null) {
            listUsers = new ArrayList<User>();
            writeObject(listUsers, FilePath.LIST_USER.getPath());
        }
    }

    private static Configuration instance = new Configuration();

    public static Configuration getInstance() {
        return instance;
    }

    public String configuration(Command command) {
        String value = command.getValue();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(value)));
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
                case "get_message": {
                    String id = element.getAttribute("chat_id");
                    return getMessages(id);
                }
                case "ban": {
                    String id = element.getAttribute("user");
                    listUsers.forEach(user -> {
                        //if (user.getLogin().equals(id))

                    });
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void addMessage(String id, Message message) {
        if (!chats.containsKey(id)) {
            chats.put(id, new ArrayList<>());
        }
        chats.get(id).add(message);
    }

    public void save() {
        writeObject(chats, FilePath.CHATS.getPath());
        writeObject(listUsers, FilePath.LIST_USER.getPath());
    }

    private static <T> T read(String path) {
        T result = null;
        try {
            FileInputStream fis = new FileInputStream(path);
            ObjectInputStream ois = new ObjectInputStream(fis);
            result = (T) ois.readObject();
            ois.close();
        } catch (IOException e) {
            e.printStackTrace();
            result = null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static void writeObject(Object obj, String path) {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(obj);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String getMessages(String id) {
        if (!chats.containsKey(id)) {
            chats.put(id, new ArrayList<>());
        }
        return xstream.toXML(chats.get(id));
    }

    private String getChats() {
        List<String> list = new ArrayList<>();
        chats.forEach((key, value) -> list.add(key));
        return xstream.toXML(list);
    }

    private String getListUsers() {
        final List<String> list = new ArrayList<>();
        listUsers.forEach(item -> list.add(item.getName()));
        return xstream.toXML(listUsers);
    }

    private String getOnlineListUsers() {
        final List<String> list = new ArrayList<>();
        listUsers.forEach(item -> {
            if (item.isOnline())
                list.add(item.getName());
        });
        return xstream.toXML(listUsers);
    }
}
