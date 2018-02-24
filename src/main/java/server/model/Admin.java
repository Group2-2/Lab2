package server.model;

public class Admin extends User {
    public Admin(String login, String password, String name) {
        super(login, password, name);
    }

    @Override
    public String toString() {
        return "Admin{ "+super.toString()+" }";
    }
}
