package server.model;

public class Command extends Node {
    private String value;
    public Command(User sender) {
        super(sender);
    }
}
