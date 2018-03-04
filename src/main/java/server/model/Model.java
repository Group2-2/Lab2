package server.model;

import java.util.List;

public interface Model {
    List<String> getListUsers();
    List<String> getOnlineListUsers();
    void addMessage(long id, Message message);
    void save();
    List<Message> getMessages(long id);
    boolean register(User user);
    boolean login(User user);
    String getUserName(String login);
    List<String> getChatUsers(long id);
    long createChat();
    void addToChat(String login, long id);
    List<Long> getChats();
    void ban(String login);
    void unban(String login);
    void setOnlineStatus(String login, boolean online);
    void createAdmin(String login);
    void deleteAdmin(String login);
    boolean isAdmin(String login);
    boolean isInBan(String login);
}
