package model;

import java.util.List;

/**
 * Model interface.
 */
public interface Model {
    /**
     * Method for get list of all users.
     * @return list of all users
     */
    List<String> getListUsers();

    /**
     * Method for get list of online users.
     * @return list of online users
     */
    List<String> getOnlineListUsers();

    /**
     * Method for add message for chat.
     * @param id id of chat
     * @param message message for adding
     */
    void addMessage(long id, Message message);

    /**
     * Method for save all changes in server.
     */
    void save();

    /**
     * Method for get all messages from chat.
     * @param id id of chat
     * @return list of chat messages
     */
    List<Message> getMessages(long id);

    /**
     * Method for register user on server.
     * @param user user for registration
     * @return if the user has successfully registered return true else return false
     */
    boolean register(User user);

    /**
     * Method for login user on server.
     * @param user user for login
     * @return if the user has successfully authorized return true else return false
     */
    boolean login(User user);

    /**
     * Method for get user name by login.
     * @param login login of user
     * @return name of user
     */
    String getUserName(String login);

    /**
     * Method for get all users from chat.
     * @param id id of chat
     * @return list of chat users
     */
    List<String> getChatUsers(long id);

    /**
     * Method for create new chat.
     * @return id of new chat
     */
    long createChat();

    /**
     * Method for add new user to chat by login.
     * @param login new user in chat
     * @param id id of chat
     */
    void addToChat(String login, long id);

    /**
     * Method for return all chat id what are available for user.
     * @param login login of users
     * @return list with chat id
     */
    List<Long> getChats(String login);

    /**
     * Method for ban user.
     * @param login login of user
     */
    void ban(String login);

    /**
     * Method for unban user.
     * @param login login of user
     */
    void unban(String login);

    /**
     * Method for change online status of user.
     * @param login login of user
     * @param online online status of user
     */
    void setOnlineStatus(String login, boolean online);

    /**
     * Method for create admin.
     * @param login login of user
     */
    void createAdmin(String login);

    /**
     * Method for delete admin.
     * @param login login of user
     */
    void deleteAdmin(String login);

    /**
     * Method for check admin status in user.
     * @param login login of user
     * @return admin status
     */
    boolean isAdmin(String login);

    /**
     * Method for check ban status in user.
     * @param login login of user
     * @return ban status
     */
    boolean isInBan(String login);

    /**
     * Method for check online status in user.
     * @param login login of user
     * @return online status
     */
    boolean isOnline(String login);

    /**
     * Method for delete user from base.
     * @param login login of user
     */
    void  deleteUser(String login);

    /**
     * Method for check exist of user.
     * @param login login of user
     * @return if user exist return true else false
     */
    boolean existUser(String login);


    /**
     * Method for get ban list.
     * @return ban list
     */
    List<String> getBanList();

    /**
     * Method for change password.
     * @param login login of user
     * @param password new password
     */
    void changePassword(String login, String password);

    /**
     * Method for leave user from chat
     * @param login login of user
     * @param chatId id of chat
     */
    void leaveChat(String login, Long chatId);

}
