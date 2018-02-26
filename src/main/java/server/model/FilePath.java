package server.model;

public enum FilePath {
    ADMIN("admin"),
    CHATS("chats"),
    Groups("groups"),
    LIST_USER("list_users");

    private String path;

    FilePath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
