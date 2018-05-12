package model;

import com.thoughtworks.xstream.XStream;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

/**
 * This class is implements of interface Model.
 * This class is singleton
 */

public class ModelImpl implements Model {

    private Map<Long, List<Message>> chats;
    private Map<Long, List<String>> groups;
    private List<User> listUsers;
    private List<String> banList;

    private static ModelImpl instance = new ModelImpl();
    private final Logger logger = Logger.getLogger(ModelImpl.class);


    public static ModelImpl getInstance() {
        return instance;
    }

    private ModelImpl() {

        chats = read(FilePath.CHATS.getPath());
        if (chats == null) {
            chats = new Hashtable<>();
            chats.put(0L, new ArrayList<>());
            writeObject(chats, FilePath.CHATS.getPath());
            logger.warn("chats file is empty");
        }
        groups = read(FilePath.GROUPS.getPath());
        if (groups == null) {
            groups = new Hashtable<>();
            groups.put(0L, new ArrayList<>());
            writeObject(groups, FilePath.GROUPS.getPath());
            logger.warn("groups file is empty");
        }
        listUsers = read(FilePath.LIST_USER.getPath());
        if (listUsers == null) {
            listUsers = new ArrayList<>();
            writeObject(listUsers, FilePath.LIST_USER.getPath());
            logger.warn("listUsers file is empty");
        }
        banList = read(FilePath.BAN_LIST.getPath());
        if (banList == null) {
            banList = new ArrayList<>();
            writeObject(listUsers, FilePath.BAN_LIST.getPath());
            logger.warn("banList file is empty");
        }
    }


    @Override
    public List<String> getListUsers() {
        List<String> list = new ArrayList<>();
        listUsers.forEach(user -> list.add(user.getLogin()));
        return list;
    }

    @Override
    public List<String> getOnlineListUsers() {
        List<String> list = new ArrayList<>();
        listUsers.forEach(user ->  {
            if (user.isOnline())
                list.add(user.getLogin());
        });
        return list;
    }

    @Override
    public void addMessage(long id, Message message) {
        if (!chats.containsKey(id)) {
            chats.put(id, new ArrayList<>());
            groups.put(id, new ArrayList<>());
        }
        chats.get(id).add(message);
    }

    @Override
    public void save() {
        writeObject(chats, FilePath.CHATS.getPath());
        writeObject(listUsers, FilePath.LIST_USER.getPath());
        writeObject(groups, FilePath.GROUPS.getPath());
        writeObject(banList, FilePath.BAN_LIST.getPath());
        //System.out.println();
    }

    @Override
    public List<Message> getMessages(long id) {
        return chats.get(id);
    }

    @Override
    public boolean register(User user) {
        for (User person: listUsers) {
            if (person.equals(user))
                return false;
        }
        listUsers.add(user);
        user.getChats().add(0L);
        groups.get(0L).add(user.getLogin());
        return true;
    }

    @Override
    public boolean login(User user) {
        for (User person: listUsers) {
            if (person.equals(user) && user.getPassword().equals(person.getPassword()))
                return true;
        }
        return false;
    }

    @Override
    public String getUserName(String login) {
        for (User person: listUsers) {
            if (person.getLogin().equals(login)) {
                return person.getName();
            }
        }

        return null;
    }

    @Override
    public List<String> getChatUsers(long id) {
        return groups.get(id);
    }

    @Override
    public long createChat() {
        long id = chats.size();
        chats.put(id, new ArrayList<>());
        groups.put(id, new ArrayList<>());
        return id;
    }

    @Override
    public void addToChat(String login, long id) {
        if (!groups.containsKey(id)) {
            chats.put(id, new ArrayList<>());
            groups.put(id, new ArrayList<>());
        }
        groups.get(id).add(login);
        User user = findByLogin(login);
        user.getChats().add(id);
    }

    @Override
    public List<Long> getChats(String login) {
        User user = findByLogin(login);
        return user.getChats();
    }

    @Override
    public void ban(String login) {
        banList.add(login);
        User user = findByLogin(login);
        user.getChats().remove(0L);
        groups.get(0L).remove(login);
    }

    @Override
    public void unban(String login) {
        banList.remove(login);
        User user = findByLogin(login);
        user.getChats().add(0L);
        groups.get(0L).add(login);
    }

    @Override
    public void setOnlineStatus(String login, boolean online) {
        User user = findByLogin(login);
        user.setOnline(online);
    }

    @Override
    public void createAdmin(String login) {
        User user = findByLogin(login);
        user.setAdmin(true);
    }

    @Override
    public void deleteAdmin(String login) {
        User user = findByLogin(login);
        user.setAdmin(false);
    }

    @Override
    public boolean isAdmin(String login) {
        User user = findByLogin(login);
        return user.isAdmin();
    }

    @Override
    public boolean isInBan(String login) {
        return banList.contains(login);
    }

    @Override
    public boolean isOnline(String login) {
        User user = findByLogin(login);
        return user.isOnline();
    }

    @Override
    public void deleteUser(String login) {
        User user = findByLogin(login);
        listUsers.remove(user);
        banList.remove(login);
        user.getChats().forEach(chat -> chats.get(chat).remove(login));
    }

    @Override
    public boolean existUser(String login) {
        return findByLogin(login) != null;
    }

    @Override
    public List<String> getBanList() {
        return new ArrayList<>(banList);
    }


    @Override
    public void changePassword(String login, String password) {
        User user = findByLogin(login);
        user.setPassword(password);
    }

    private User findByLogin(String login) {
        User user = null;
        for (User person: listUsers) {
            if (person.getLogin().equals(login)) {
                user = person;
            }
        }
        if (user == null)
            logger.error("user with login " + login + " is not exist");
        return user;
    }

    private <T> T read(String path) {
        T result = null;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(path)));
            StringBuilder builder = new StringBuilder();
            String thisLine = "";
            while ((thisLine = bufferedReader.readLine()) != null) {
                builder.append(thisLine).append("\b");
            }
            XStream xstream = new XStream();
            xstream.alias("hashtable", Map.class);
            xstream.alias("entry", Map.Entry.class);
            xstream.alias("list", List.class);
            xstream.alias("chats", List.class);
            result = (T) xstream.fromXML(builder.toString());
            bufferedReader.close();
        } catch (IOException e) {
            logger.debug("File " + path + " not found");
        }
        return result;
    }

    private void writeObject(Object obj, String path) {
        try {
            XStream xstream = new XStream();
            xstream.toXML(obj, new PrintWriter(new File(path)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void leaveChat(String login, Long chatId) {
    }
}
