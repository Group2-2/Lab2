package server.model;

public enum FilePath {
    BAN_LIST("ban_list"),
    CHATS("chats"),
    GROUPS("groups"),
    LIST_USER("list_users"),;

    private String path;

    FilePath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
