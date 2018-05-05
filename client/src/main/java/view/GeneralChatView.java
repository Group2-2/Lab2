package view;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import controller.*;
import controller.ClientControllerImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Created by SviatoslavHavrilo on 23.02.2018.
 */
public class GeneralChatView extends JFrame {
    protected JTextArea chatArea;
    protected JList onlineUsersList;
    protected JList privateChatsList;
    protected JButton sendMessageButton;
    protected JTextArea newMassegeArea;
    protected JButton privateChatButton;
    protected JButton openPrivateChatButton;
    protected ClientControllerImpl controller;
    protected JPanel mainPanel;
    protected JButton addNewUserButton;
    protected JButton editMyPasswordButton;
    protected DefaultListModel listModel;
    protected DefaultListModel listModelChats;
    private String chat_id;

    /**
     * Create new main chat frame
     * @param controller controller
     * @param titile titile
     */
    public GeneralChatView(ClientControllerImpl controller, String titile) {
        super(titile);
        this.controller = controller;
        setChat_id(controller.getMainChatID());
        createGUI();
        addCloseListener();
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); //(EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }

    /**
     * Constructor for extended frames
     * @param titile
     */
    public GeneralChatView(String titile) {
        super(titile);
        setChat_id(controller.getMainChatID());
    }

    /**
     * Getter chat_id
     * @return string chat_id
     */
    public String getChat_id() {
        return chat_id;
    }

    /**
     * setter chat_id
     * @param chatId chatId
     */
    public void setChat_id(String chatId) {
        this.chat_id = chatId;
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void createGUI() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(6, 3, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.setBorder(BorderFactory.createTitledBorder("Online users"));
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setBackground(new Color(-2366220));
        mainPanel.add(scrollPane1, new GridConstraints(0, 1, 4, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        chatArea = new JTextArea();
        chatArea.setBackground(new Color(-2366220));
        chatArea.setLineWrap(true);
        chatArea.setText("");
        scrollPane1.setViewportView(chatArea);
        final JScrollPane scrollPane2 = new JScrollPane();
        mainPanel.add(scrollPane2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        onlineUsersList = new JList();
        onlineUsersList.setEnabled(true);
        listModel = new DefaultListModel();
        onlineUsersList.setModel(listModel);
        onlineUsersList.setLayoutOrientation(1);
        scrollPane2.setViewportView(onlineUsersList);
        newMassegeArea = new JTextArea();
        mainPanel.add(newMassegeArea, new GridConstraints(4, 1, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, 50), null, 0, false));
        final JScrollPane scrollPane3 = new JScrollPane();
        mainPanel.add(scrollPane3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        privateChatsList = new JList();
        privateChatsList.setLayoutOrientation(1);
        listModelChats = new DefaultListModel();
        privateChatsList.setModel(listModelChats);
        scrollPane3.setViewportView(privateChatsList);
        final JLabel label1 = new JLabel();
        label1.setText("Private chats");
        mainPanel.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        privateChatButton = new JButton();
        privateChatButton.setText("Start new private chat");
        mainPanel.add(privateChatButton, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        openPrivateChatButton = new JButton();
        openPrivateChatButton.setText("Open private chat");
        mainPanel.add(openPrivateChatButton, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sendMessageButton = new JButton();
        sendMessageButton.setText("Send message");
        mainPanel.add(sendMessageButton, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editMyPasswordButton = new JButton();
        editMyPasswordButton.setText("Edit my password");
        mainPanel.add(editMyPasswordButton, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        this.add(mainPanel);
        setButtonListeners();
    }

    public void addCloseListener() {
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                Object[] options = {"Yes", "No"};
                int n = JOptionPane
                        .showOptionDialog(event.getWindow(), "Close application?",
                                "Confirmation", JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE, null, options,
                                options[0]);
                if (n == 0) {
                    controller.exitChat();
                    System.exit(2);
                }
            }
        });
    }

    /**
     * Change private chat button into add new user to chat
     */
    public void removePrivateChatButton() {
        mainPanel.remove(privateChatButton);
        mainPanel.remove(privateChatButton);
        mainPanel.remove(privateChatButton);
        addNewUserButton = new JButton();
        addNewUserButton.setText("Add new friend to chat");
        mainPanel.add(addNewUserButton, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        addNewUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.addToPrivateChatSelect(getChat_id());
            }
        });
    }

    /**
     * set button listeners
     */
    public void setButtonListeners() {
        privateChatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.createPrivateChat();
            }
        });
        sendMessageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (newMassegeArea.getText() != null && !newMassegeArea.getText().trim().equals("")) {
                    controller.sendMessage(newMassegeArea.getText(), getChat_id());
                    newMassegeArea.setText("");
                }
            }
        });
        openPrivateChatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (privateChatsList.getSelectedValue() != null) {
                    String val = privateChatsList.getSelectedValue().toString();
                    if (val != null && !val.trim().equals("")) {
                        controller.addToPrivateChat(controller.getCurrentUser(), val);
                    }
                }
            }
        });
        privateChatsList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                if (evt.getClickCount() == 2) {
                    String val = privateChatsList.getSelectedValue().toString();
                    if (val != null && !val.trim().equals("")) {
                        controller.addToPrivateChat(controller.getCurrentUser(), val);
                    }
                }
            }
        });
        editMyPasswordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.changePassWindow(controller.getCurrentUser());
            }
        });
    }

    /**
     * Set list of online users
     * @param arrList
     */
    public void setOnlineUsersList(ArrayList<String> arrList) {
        listModel.clear();
        for (String userName : arrList) {
            listModel.addElement(userName);
        }
    }

    /**
     * add new massage in text area
     * @param massage
     */
    public void printNewMassage(String massage) {
        chatArea.append(massage + "\n");
    }

    /**
     * Banned user enabled send massages
     * @param isBaned
     */
    public void blockBanedUser(boolean isBaned) {
        boolean block = !isBaned;
        sendMessageButton.setEnabled(block);
        newMassegeArea.setEnabled(block);
    }

    public void setPrivateChatsList(ArrayList<String> arrList) {
        listModelChats.clear();
        for (String chatID : arrList) {
            listModelChats.addElement(chatID);
        }
    }

    /**
     * Set list of banned users
     * @param arrList
     */
    public void setBannedList(ArrayList<String> arrList) {
    }
}
