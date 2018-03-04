package client.view;


import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import client.controller.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class AdminView extends GeneralChatView {
/*    private JTextArea chatArea;
    private JList onlineUsersList;
    private JButton sendMessageButton;
    private JTextArea newMassegeArea;
    private JButton privateChatButton;
    private JPanel mainPanel;*/
    private JButton banUserButton;
    private JButton unbanUserButton;
    private JButton editUserInformationButton;
    private JList bannedUsersList;
  /*  private ClientController controller;
    private DefaultListModel listModel;*/

    public AdminView(ClientControllerImpl controller) {
        super("ADMIN_General chat");
        this.setTitle("ADMIN_General chat");
        this.controller = controller;
        createAdminGUI();
        ArrayList<String> arrList = controller.getOnlineUserslist();
        setOnlineUsersList(arrList);
        super.setButtonListeners();
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }

    private void createAdminGUI() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(6, 10, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.setBackground(new Color(-3747873));
        final JScrollPane scrollPane1 = new JScrollPane();
        mainPanel.add(scrollPane1, new GridConstraints(1, 2, 2, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setEnabled(true);
        chatArea.setLineWrap(false);
        chatArea.setMinimumSize(new Dimension(50, 50));
        chatArea.setPreferredSize(new Dimension(50, 300));
        chatArea.setText("");
        chatArea.setWrapStyleWord(true);
        scrollPane1.setViewportView(chatArea);
        final JLabel label1 = new JLabel();
        label1.setText(" ");
        mainPanel.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Online");
        mainPanel.add(label2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText(" ");
        mainPanel.add(label3, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Banned users");
        mainPanel.add(label4, new GridConstraints(0, 8, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        bannedUsersList = new JList();
        final DefaultListModel defaultListModel1 = new DefaultListModel();
        bannedUsersList.setModel(defaultListModel1);
        mainPanel.add(bannedUsersList, new GridConstraints(1, 8, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        unbanUserButton = new JButton();
        unbanUserButton.setText("Unban user");
        mainPanel.add(unbanUserButton, new GridConstraints(2, 8, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        banUserButton = new JButton();
        banUserButton.setText("Ban user");
        mainPanel.add(banUserButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        onlineUsersList = new JList();
        onlineUsersList.setBackground(new Color(-3747873));
        listModel = new DefaultListModel();
        onlineUsersList.setModel(listModel);
        onlineUsersList.setToolTipText("");
        mainPanel.add(onlineUsersList, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(150, 50), new Dimension(150, -1), 0, false));
        newMassegeArea = new JTextArea();
        newMassegeArea.setLineWrap(true);
        newMassegeArea.setRows(1);
        newMassegeArea.setText("");
        newMassegeArea.setWrapStyleWord(true);
        mainPanel.add(newMassegeArea, new GridConstraints(3, 2, 2, 3, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(150, 100), null, 0, false));
        privateChatButton = new JButton();
        privateChatButton.setText("Private chat");
        mainPanel.add(privateChatButton, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sendMessageButton = new JButton();
        sendMessageButton.setText("Send message");
        mainPanel.add(sendMessageButton, new GridConstraints(4, 8, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
       /* editUserInformationButton = new JButton();
        editUserInformationButton.setText("Edit user information");
        mainPanel.add(editUserInformationButton, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
*/
        this.add(mainPanel);
        setAdminButtonListeners();

    }

    public void setAdminButtonListeners() {
        banUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.banUserSelect(getChat_id());
            }
        });

        unbanUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.unBanUserSelect(getChat_id());
            }
        });
    }
    }
