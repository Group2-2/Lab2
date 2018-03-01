package server.model;

import java.util.Objects;

public class Message {
    private String sender;
    private String text;

    public Message(String sender, String text) {
        this.sender = sender;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String getSender() {
        return sender;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(sender, message.sender) &&
                Objects.equals(text, message.text);
    }

    @Override
    public int hashCode() {

        return Objects.hash(sender, text);
    }

    @Override
    public String toString() {
        return "Message{" +
                "sender=" + sender +
                ", text='" + text + '\'' +
                '}';
    }
}
