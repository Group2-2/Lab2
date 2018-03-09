package server.model;


/**
 * Enum contains path to file
 */
public enum FilePath {
    BAN_LIST("ban_list"),
    CHATS("chats"),
    GROUPS("groups"),
    LIST_USER("list_users");

    private String path;

    FilePath(String path) {
        this.path = path;
    }

    /**
     * Get path to file
     * @return path to file
     */
    public String getPath() {
        return path;
    }
}
