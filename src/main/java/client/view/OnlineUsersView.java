package client.view;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import client.controller.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Created by SviatoslavHavrilo on 17.02.2018.
 */
public class OnlineUsersView extends JFrame {
    private JList onlineUsersList;
    private JButton selectUsersButton;
    private JButton cancelButton;
    private JPanel mainPanel;
    private ClientController controller;
    private String command;
    private DefaultListModel listModel;

    public OnlineUsersView(ClientController controller, String titile, String command) {
        super(titile);
        this.controller = controller;
        this.command = command;
        createGUI();
        ArrayList<String> arrList = controller.getOnlineUserslist();
        setOnlineUsersList(arrList);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }

    private void createGUI() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        final JScrollPane scrollPane1 = new JScrollPane();
        mainPanel.add(scrollPane1, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        onlineUsersList = new JList();
        listModel = new DefaultListModel();
        onlineUsersList.setModel(listModel);
        scrollPane1.setViewportView(onlineUsersList);
        selectUsersButton = new JButton();
        selectUsersButton.setText("Select users");
        mainPanel.add(selectUsersButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        mainPanel.add(cancelButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        this.add(mainPanel);
        setButtonListeners();
    }

    public void setButtonListeners() {
        selectUsersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });
    }

    public void setOnlineUsersList(ArrayList<String> arrList){
        for (String userName: arrList) {
            listModel.addElement(userName);
        }
    }

}
