package controller;

import java.util.Map;

/**
 * interface for server controller with main methods.
 */
public interface ServerController extends Runnable {
    /**
     * @return port.
     */
    int getPort();

    /**
     * map with ONLINE users.
     * @return map login of user - connection og this current user
     */
    Map<String, Connection> getUsers();

    /**
     * called, when user login or register (ACCEPTED).
     * @param login of already connected user
     * @param connection current connection of this user
     */
    void setUser(String login, Connection connection);

    /**
     * Send command to all users in chat except this current user.
     * @param chatId - current chat
     * @param text - message
     * @param current user, that sent mess, to avoid duplicates
     */
    void sendToChat(Long chatId, String text, Connection current);

    @Override
    void run();
}
