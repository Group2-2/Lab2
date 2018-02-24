package client.view;

import client.controller.*;

/**
 * Created by SviatoslavHavrilo on 17.02.2018.
 */
public class PrivateChatView extends GeneralChatView{


    public PrivateChatView(ClientController controller) {
        super(controller, "Private chat room");
        removeprivateChatButton();
}
}
