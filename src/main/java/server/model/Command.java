package server.model;

import java.util.Objects;

public class Command {
    private String value;
    private User sender;

    public Command(User sender, String value) {
        this.sender = sender;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public User getSender() {
        return sender;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Command command = (Command) o;
        return Objects.equals(value, command.value) &&
                Objects.equals(sender, command.sender);
    }

    @Override
    public int hashCode() {

        return Objects.hash(value, sender);
    }

    @Override
    public String toString() {
        return "Command{" +
                "value='" + value + '\'' +
                ", sender=" + sender +
                '}';
    }
}
