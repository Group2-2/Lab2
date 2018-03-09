package server.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * This class describe message in chat. It contain information about sender and message
 */
public class Message implements Serializable {
    private String sender;
    private String text;

    public Message(String sender, String text) {
        this.sender = sender;
        this.text = text;
    }

    /**
     * Method for get text of message
     * @return text of message
     */
    public String getText() {
        return text;
    }

    /**
     * Method for get sender login of message
     * @return sender login
     */
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
