package server.model;

import java.util.ArrayList;
import java.util.List;

public class Admin extends User {
    private List<User> listBan;

    public Admin(String login, String password, String name) {
        super(login, password, name);
        listBan = new ArrayList<>();
    }

    public List<User> getListBan() {
        return listBan;
    }

    @Override
    public String toString() {
        return "Admin{ "+super.toString()+" }";
    }
}
