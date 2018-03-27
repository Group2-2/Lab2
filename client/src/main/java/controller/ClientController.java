package controller;

/**
 * Created by SviatoslavHavrilo on 03.03.2018.
 */
public interface ClientController {

    /**
     *Start new chat application
     */
    void run();

    /**
     * get in out streams
     * @return boolean successful connected
     */
    boolean connectServer();

    /**
     * prepare command check user login-password
     * @param login
     * @param password
     * @return
     */
    boolean validateUser(String login, String password);

    /**
     * prepare command register new user
     * @param login
     * @param nickName
     * @param password
     * @return success
     */
    boolean registerNewUser(String login, String nickName, String password);

    /**
     * close main chat window
     */
    void exitChat();

    /**
     * prepare massage for output stream
     * @param message
     * @param chatID
     * @return success
     */
    boolean sendMessage(String message, String chatID);

    /**
     * prepare command banned user
     * @param User
     * @return command is sent
     */
    boolean banUser(String User);

    /**
     * prepare command unbanned user
     * @param User
     * @return command is sent
     */
    boolean unBanUser(String User);

    /**
     * sent massage/command to output stream
     * @param xmlText
     * @return command is sent
     */
    boolean sendXMLString(String xmlText);


}
