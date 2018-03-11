package server.model;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class contains information about user
 */
public class User implements Serializable {
    private String name;
    private String login;
    private String password;
    private boolean online= false;
    private boolean admin = false;
    private List<Long> chats = new ArrayList<>();

    private static final Logger logger = Logger.getLogger(User.class);


    public User(String login, String password, String name) {
        this.name = name;
        this.login = login;
        this.password = password;

        if (name.trim().equals("")) {
            logger.warn("Name of user is empty");
        }
        if (login.trim().equals("")) {
            logger.warn("Login of user is empty");
        }
        if (password.trim().equals("")) {
            logger.warn("Password of user is empty");
        }
    }

    /**
     * Get chats what is available for user
     * @return list of chat id's
     */
    public List<Long> getChats() {
        return chats;
    }

    /**
     * Get user name
     * @return user name
     */
    public String getName() {
        return name;
    }

    /**
     * Get user login
     * @return user login
     */
    public String getLogin() {
        return login;
    }

    /**
     * Set user login
     * @param login new login
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * Check online status
     * @return online status
     */
    public boolean isOnline() {
        return online;
    }

    /**
     * Set online status
     * @param online online status
     */
    public void setOnline(boolean online) {
        this.online = online;
    }

    /**
     * Check admin status
     * @return admin status
     */
    public boolean isAdmin() {
        return admin;
    }

    /**
     * Set admin status
     * @param admin admin status
     */
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, login, password);
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", online=" + online +
                ", admin=" + admin +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return user.getLogin().equals(login);
    }
}
