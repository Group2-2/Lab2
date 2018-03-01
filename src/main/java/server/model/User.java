package server.model;

import java.util.Objects;

public class User {
    private String name;
    private String login;
    private String password;
    private boolean online;
    private boolean admin;

    public User(String name, String login, String password, boolean online, boolean admin) {
        this.name = name;
        this.login = login;
        this.password = password;
        this.online = online;
        this.admin = admin;
    }

    public User(String login,String password) {
        this.login = login;
        this.password = password;
        online = false;
        admin = false;
    }

    public User(String name, String login, boolean admin) {
        this.name = name;
        this.login = login;
        this.admin = admin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public boolean isAdmin() {
        return admin;
    }

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
