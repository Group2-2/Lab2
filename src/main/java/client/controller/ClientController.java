package client.controller;

/**
 * Created by SviatoslavHavrilo on 03.03.2018.
 */
public interface ClientController {

    boolean connectServer();

    boolean validateUser(String login, String password);

    boolean registerNewUser(String login, String nickName, String password);

    void exitChat();

    boolean sendMessage(String message, String chatID);

    boolean banUser(String User);

    boolean unBanUser(String User);

    void run();


}
