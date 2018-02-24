package server.model;

import java.util.Objects;

abstract public class Node {
    private User sender;

    public Node(User sender) {
        this.sender = sender;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(sender, node.sender);
    }

    @Override
    public int hashCode() {

        return Objects.hash(sender);
    }

    @Override
    public String toString() {
        return "Node{" +
                "sender=" + sender +
                '}';
    }
}
