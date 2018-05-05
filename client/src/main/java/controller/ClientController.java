package controller;

/**
 * Created by SviatoslavHavrilo on 03.03.2018.
 */
public interface ClientController {

    /**
     *Start new chat application.
     */
    void run();

    /**
     * get in out streams.
     * @return boolean successful connected
     */
    boolean connectServer();

    /**
     * prepare command check user login-password.
     * @param login login
     * @param password password
     * @return command has been sent
     */
    boolean validateUser(String login, String password);

    /**
     * prepare command register new user.
     * @param login login
     * @param nickName name
     * @param password password
     * @return success Massage is sent
     */
    boolean registerNewUser(String login, String nickName, String password);

    /**
     * close main chat window.
     */
    void exitChat();

    /**
     * prepare massage for output stream.
     * @param message message
     * @param chatId chatId
     * @return success Massage is sent
     */
    boolean sendMessage(String message, String chatId);

    /**
     * prepare command banned user.
     * @param user user login
     * @return command is sent
     */
    boolean banUser(String user);

    /**
     * prepare command unbanned user.
     * @param user login
     * @return command is sent
     */
    boolean unBanUser(String user);

    /**
     * sent massage/command to output stream.
     * @param xmlText string xml
     * @return command is sent
     */
    boolean sendXMLString(String xmlText);

    /**
     * command delete user.
     *
     * @param deleteUser login
     * @return command is sent
     */
    boolean deleteUser(String deleteUser);

    /**
     * command delete user.
     *
     * @param login login
     * @param password password
     */
    void changePassword(String login, String password);

}
