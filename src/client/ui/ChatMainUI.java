package client.ui;

import client.ChatClient;
import common.Group;
import common.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * 聊天主界面组件类
 * 负责创建和管理聊天主界面的UI组件
 */
public class ChatMainUI {
    private ChatClient chatClient;
    
    // 界面组件
    private JTextArea chatArea;      // 聊天内容显示区
    private JTextField inputField;   // 消息输入框
    private JComboBox<String> chatTypeBox; // 聊天类型选择（私聊/群聊）
    private JComboBox<String> targetBox; // 私聊：用户名 | 群聊：群ID（显示群名）
    private JList<String> userList;  // 在线用户列表
    private DefaultListModel<String> userListModel; // 用户列表数据模型
    private JButton fileBtn; // 新增：选择文件按钮
    private JButton shakeBtn; // 新增：窗口抖动按钮
    
    // 群聊相关组件
    private DefaultListModel<String> groupListModel; // 群列表显示模型（显示群名）
    private JList<String> groupList;
    private JPanel groupPanel; // 群列表面板
    private JComboBox<String> fontSelectBox; // 字体选择下拉框
    private Font currentChatFont; // 当前选中的聊天字体

    public ChatMainUI(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 初始化聊天主界面
     */
    public void initChatUI(String username) {
        chatClient.setTitle("Java 聊天室");
        chatClient.setSize(900, 600);
        chatClient.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chatClient.setLocationRelativeTo(null);

        currentChatFont = new Font("微软雅黑", Font.PLAIN, 14);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(280, 0));
        leftPanel.setBackground(new Color(245, 245, 245));

        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBackground(new Color(245, 245, 245));
        userPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel userTitleLabel = new JLabel("在线用户");
        userTitleLabel.setFont(new Font("微软雅黑", Font.BOLD, 13));
        userTitleLabel.setForeground(new Color(51, 51, 51));
        userPanel.add(userTitleLabel, BorderLayout.NORTH);

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setBackground(Color.WHITE);
        userList.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        userList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setFont(new Font("微软雅黑", Font.PLAIN, 13));
                if (isSelected) {
                    label.setBackground(new Color(7, 193, 96));
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(new Color(51, 51, 51));
                }
                label.setOpaque(true);
                return label;
            }
        });

        userList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 1) {
                    String selectedUser = userList.getSelectedValue();
                    if (selectedUser != null && !selectedUser.equals(username)) {
                        chatTypeBox.setSelectedItem("私聊");
                        targetBox.removeAllItems();
                        targetBox.addItem(selectedUser);
                    }
                }
            }
        });
        userPanel.add(new JScrollPane(userList), BorderLayout.CENTER);

        groupPanel = new JPanel(new BorderLayout());
        groupPanel.setBackground(new Color(245, 245, 245));
        groupPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel groupTitleLabel = new JLabel("群聊列表");
        groupTitleLabel.setFont(new Font("微软雅黑", Font.BOLD, 13));
        groupTitleLabel.setForeground(new Color(51, 51, 51));
        groupPanel.add(groupTitleLabel, BorderLayout.NORTH);

        groupListModel = new DefaultListModel<>();
        groupList = new JList<>(groupListModel);
        groupList.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        groupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        groupList.setBackground(Color.WHITE);
        groupList.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        groupList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setFont(new Font("微软雅黑", Font.PLAIN, 13));
                if (isSelected) {
                    label.setBackground(new Color(7, 193, 96));
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(new Color(51, 51, 51));
                }
                label.setOpaque(true);
                return label;
            }
        });

        groupList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 1) {
                    String selectedGroupName = groupList.getSelectedValue();
                    if (selectedGroupName == null || selectedGroupName.isEmpty()) {
                        JOptionPane.showMessageDialog(chatClient, "请选择有效的群聊！");
                        return;
                    }
                    String groupId = chatClient.getGroupNameToIdMap().get(selectedGroupName);
                    if (groupId == null || groupId.isEmpty()) {
                        JOptionPane.showMessageDialog(chatClient, "该群聊的ID不存在，请刷新！");
                        return;
                    }
                    chatTypeBox.setSelectedItem("群聊");
                    targetBox.removeAllItems();
                    targetBox.addItem(selectedGroupName);
                    targetBox.putClientProperty("groupId", groupId);
                }
            }
        });
        groupPanel.add(new JScrollPane(groupList), BorderLayout.CENTER);

        JPanel groupBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        groupBtnPanel.setBackground(new Color(245, 245, 245));
        JButton createGroupBtn = new JButton("创建");
        createGroupBtn.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        createGroupBtn.setBackground(new Color(7, 193, 96));
        createGroupBtn.setForeground(Color.WHITE);
        createGroupBtn.setBorderPainted(false);
        createGroupBtn.setFocusPainted(false);
        createGroupBtn.setPreferredSize(new Dimension(60, 28));

        JButton searchGroupBtn = new JButton("查找");
        searchGroupBtn.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        searchGroupBtn.setBackground(new Color(7, 193, 96));
        searchGroupBtn.setForeground(Color.WHITE);
        searchGroupBtn.setBorderPainted(false);
        searchGroupBtn.setFocusPainted(false);
        searchGroupBtn.setPreferredSize(new Dimension(60, 28));

        JButton joinGroupBtn = new JButton("加入");
        joinGroupBtn.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        joinGroupBtn.setBackground(new Color(7, 193, 96));
        joinGroupBtn.setForeground(Color.WHITE);
        joinGroupBtn.setBorderPainted(false);
        joinGroupBtn.setFocusPainted(false);
        joinGroupBtn.setPreferredSize(new Dimension(60, 28));

        groupBtnPanel.add(createGroupBtn);
        groupBtnPanel.add(searchGroupBtn);
        groupBtnPanel.add(joinGroupBtn);
        groupPanel.add(groupBtnPanel, BorderLayout.SOUTH);

        leftPanel.add(userPanel, BorderLayout.NORTH);
        leftPanel.add(groupPanel, BorderLayout.CENTER);

        JPanel chatMainPanel = new JPanel(new BorderLayout());
        chatMainPanel.setBackground(Color.WHITE);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(250, 250, 250));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        topPanel.setPreferredSize(new Dimension(0, 50));

        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        typePanel.setBackground(new Color(250, 250, 250));

        JLabel typeLabel = new JLabel("聊天类型：");
        typeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        typeLabel.setForeground(new Color(102, 102, 102));
        typePanel.add(typeLabel);

        chatTypeBox = new JComboBox<>(new String[]{"群聊", "私聊"});
        chatTypeBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        chatTypeBox.setBackground(Color.WHITE);
        typePanel.add(chatTypeBox);

        JLabel targetLabel = new JLabel("目标：");
        targetLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        targetLabel.setForeground(new Color(102, 102, 102));
        typePanel.add(targetLabel);

        targetBox = new JComboBox<>();
        targetBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        targetBox.setBackground(Color.WHITE);
        typePanel.add(targetBox);

        JLabel fontLabel = new JLabel("字体：");
        fontLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        fontLabel.setForeground(new Color(102, 102, 102));
        typePanel.add(fontLabel);

        fontSelectBox = new JComboBox<>(new String[]{"微软雅黑", "宋体"});
        fontSelectBox.setSelectedItem("微软雅黑");
        fontSelectBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        fontSelectBox.setBackground(Color.WHITE);
        fontSelectBox.setPreferredSize(new Dimension(80, 25));
        fontSelectBox.addActionListener(e -> {
            String selectedFontName = (String) fontSelectBox.getSelectedItem();
            currentChatFont = new Font(selectedFontName, Font.PLAIN, 14);
            chatArea.setFont(currentChatFont);
        });
        typePanel.add(fontSelectBox);

        topPanel.add(typePanel, BorderLayout.WEST);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        chatArea.setBackground(Color.WHITE);
        chatArea.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(250, 250, 250));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        bottomPanel.setPreferredSize(new Dimension(0, 80));

        inputField = new JTextField();
        inputField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        inputField.setBackground(Color.WHITE);
        inputField.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setBackground(new Color(250, 250, 250));

        JButton sendBtn = new JButton("发送");
        sendBtn.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        sendBtn.setBackground(new Color(7, 193, 96));
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setBorderPainted(false);
        sendBtn.setFocusPainted(false);
        sendBtn.setPreferredSize(new Dimension(70, 32));

        fileBtn = new JButton("文件");
        fileBtn.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        fileBtn.setBackground(new Color(7, 193, 96));
        fileBtn.setForeground(Color.WHITE);
        fileBtn.setBorderPainted(false);
        fileBtn.setFocusPainted(false);
        fileBtn.setPreferredSize(new Dimension(70, 32));

        shakeBtn = new JButton("抖动");
        shakeBtn.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        shakeBtn.setBackground(new Color(7, 193, 96));
        shakeBtn.setForeground(Color.WHITE);
        shakeBtn.setBorderPainted(false);
        shakeBtn.setFocusPainted(false);
        shakeBtn.setPreferredSize(new Dimension(70, 32));

        JButton screenshotBtn = new JButton("截图");
        screenshotBtn.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        screenshotBtn.setBackground(new Color(7, 193, 96));
        screenshotBtn.setForeground(Color.WHITE);
        screenshotBtn.setBorderPainted(false);
        screenshotBtn.setFocusPainted(false);
        screenshotBtn.setPreferredSize(new Dimension(70, 32));

        btnPanel.add(sendBtn);
        btnPanel.add(fileBtn);
        btnPanel.add(screenshotBtn);
        btnPanel.add(shakeBtn);

        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(btnPanel, BorderLayout.EAST);

        chatMainPanel.add(topPanel, BorderLayout.NORTH);
        chatMainPanel.add(scrollPane, BorderLayout.CENTER);
        chatMainPanel.add(bottomPanel, BorderLayout.SOUTH);

        chatClient.add(leftPanel, BorderLayout.WEST);
        chatClient.add(chatMainPanel, BorderLayout.CENTER);

        sendBtn.addActionListener(e -> chatClient.sendMessage());
        inputField.addActionListener(e -> chatClient.sendMessage());
        fileBtn.addActionListener(e -> chatClient.selectFile());
        shakeBtn.addActionListener(e -> chatClient.sendShakeMessage());
        createGroupBtn.addActionListener(e -> chatClient.showCreateGroupDialog());
        searchGroupBtn.addActionListener(e -> chatClient.showSearchGroupDialog());
        joinGroupBtn.addActionListener(e -> chatClient.showJoinGroupDialog());
        screenshotBtn.addActionListener(e -> chatClient.handleScreenshot());

        chatTypeBox.addActionListener(e -> {
            if (chatTypeBox.getSelectedItem().equals("群聊")) {
                targetBox.removeAllItems();
                for (int i = 0; i < groupListModel.size(); i++) {
                    targetBox.addItem(groupListModel.getElementAt(i));
                }
            } else {
                targetBox.removeAllItems();
                for (int i = 0; i < userListModel.size(); i++) {
                    String user = userListModel.getElementAt(i);
                    if (!user.equals(username)) {
                        targetBox.addItem(user);
                    }
                }
            }
        });

        chatArea.setFont(currentChatFont);
    }

    // Getter方法
    public JTextArea getChatArea() {
        return chatArea;
    }

    public JTextField getInputField() {
        return inputField;
    }

    public JComboBox<String> getChatTypeBox() {
        return chatTypeBox;
    }

    public JComboBox<String> getTargetBox() {
        return targetBox;
    }

    public JList<String> getUserList() {
        return userList;
    }

    public DefaultListModel<String> getUserListModel() {
        return userListModel;
    }

    public JButton getFileBtn() {
        return fileBtn;
    }

    public JButton getShakeBtn() {
        return shakeBtn;
    }

    public DefaultListModel<String> getGroupListModel() {
        return groupListModel;
    }

    public JList<String> getGroupList() {
        return groupList;
    }

    public JPanel getGroupPanel() {
        return groupPanel;
    }

    public JComboBox<String> getFontSelectBox() {
        return fontSelectBox;
    }

    public Font getCurrentChatFont() {
        return currentChatFont;
    }

    /**
     * 更新在线用户列表UI
     */
    public void updateUserList(List<String> onlineUsers) {
        // 注意：Swing组件更新必须在事件调度线程中执行
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (String user : onlineUsers) {
                userListModel.addElement(user);
            }
        });
    }

    /**
     * 更新群列表（线程安全）
     */
    public void updateGroupList(List<Group> groupList) {
        SwingUtilities.invokeLater(() -> {
            groupListModel.clear();
            chatClient.getGroupIdToNameMap().clear();
            chatClient.getGroupNameToIdMap().clear();

            for (Group group : groupList) {
                groupListModel.addElement(group.getGroupName());
                chatClient.getGroupIdToNameMap().put(group.getGroupId(), group.getGroupName());
                chatClient.getGroupNameToIdMap().put(group.getGroupName(), group.getGroupId());
            }

            // 若当前是群聊模式，更新目标下拉框
            if (chatTypeBox.getSelectedItem().equals("群聊")) {
                targetBox.removeAllItems();
                for (int i = 0; i < groupListModel.size(); i++) {
                    targetBox.addItem(groupListModel.getElementAt(i));
                }
            }
        });
    }
}