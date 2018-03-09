package client.view;

import client.controller.*;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by SviatoslavHavrilo on 17.02.2018.
 */
public class PrivateChatView extends GeneralChatView {

    public PrivateChatView(ClientControllerImpl controller) {
        super(controller, "Private chat room");
        removePrivateChatButton();
        addCloseListener();
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setOnlineUsersList(controller.getOnlineUsersList());
    }

    public void addCloseListener() {
        this.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent event) {
                controller.leavePrivateChat(getChat_id());
            }
        });
    }

    public void closeFrame() {
        this.setVisible(false);
        this.dispose();
    }
}
