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
        // 窗口设置
        chatClient.setTitle("Java Socket 聊天室(账号：" + username + ")");
        chatClient.setSize(800, 500);
        chatClient.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chatClient.setLocationRelativeTo(null);

        // ========== 初始化字体（默认宋体） ==========
        currentChatFont = new Font("宋体", Font.PLAIN, 14); // 默认字体：宋体、常规、14号

        // ========== 左侧：在线用户列表面板 + 群列表 ==========
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(200, 0));

        // 在线用户列表面板
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBorder(BorderFactory.createTitledBorder("在线用户"));

        // 初始化用户列表模型和组件
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setFont(new Font("宋体", Font.PLAIN, 14));
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 新增：点击用户列表项，自动选择私聊目标
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

        // 新增：群列表
        groupPanel = new JPanel(new BorderLayout());
        groupPanel.setBorder(BorderFactory.createTitledBorder("所有群聊"));
        groupListModel = new DefaultListModel<>();
        groupList = new JList<>(groupListModel);
        groupList.setFont(new Font("宋体", Font.PLAIN, 14));
        groupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // 点击群列表，切换为该群聊
        groupList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 1) {
                    String selectedGroupName = groupList.getSelectedValue();
                    // 空值校验（关键）
                    if (selectedGroupName == null || selectedGroupName.isEmpty()) {
                        JOptionPane.showMessageDialog(chatClient, "请选择有效的群聊！");
                        return;
                    }
                    // 从映射中获取群ID（核心）
                    String groupId = chatClient.getGroupNameToIdMap().get(selectedGroupName);
                    if (groupId == null || groupId.isEmpty()) {
                        JOptionPane.showMessageDialog(chatClient, "该群聊的ID不存在，请刷新！");
                        return;
                    }
                    // 切换为群聊模式，并设置下拉框
                    chatTypeBox.setSelectedItem("群聊");
                    targetBox.removeAllItems();
                    targetBox.addItem(selectedGroupName);
                    // 存储群ID到下拉框的ClientProperty（关键）
                    targetBox.putClientProperty("groupId", groupId);
                }
            }
        });
        groupPanel.add(new JScrollPane(groupList), BorderLayout.CENTER);

        // 新增：群操作按钮（创建群、查找群、加入群）
        JPanel groupBtnPanel = new JPanel();
        JButton createGroupBtn = new JButton("创建群聊");
        JButton searchGroupBtn = new JButton("查找群聊");
        JButton joinGroupBtn = new JButton("加入群聊");
        groupBtnPanel.add(createGroupBtn);
        groupBtnPanel.add(searchGroupBtn);
        groupBtnPanel.add(joinGroupBtn);
        groupPanel.add(groupBtnPanel, BorderLayout.SOUTH);

        // 组装左侧面板
        leftPanel.add(userPanel, BorderLayout.NORTH);
        leftPanel.add(groupPanel, BorderLayout.CENTER);


        // ========== 右侧：聊天主面板 ==========
        JPanel chatMainPanel = new JPanel(new BorderLayout());

        // 顶部面板：选择聊天类型和目标
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5)); // 流式布局，间距10

        // 新增：字体选择下拉框
        topPanel.add(new JLabel("聊天字体："));
        fontSelectBox = new JComboBox<>(new String[]{"宋体", "黑体"}); // 至少两个字体
        fontSelectBox.setSelectedItem("宋体"); // 默认选中宋体
        fontSelectBox.setPreferredSize(new Dimension(100, 25)); // 固定宽度，美观
        // 字体选择事件监听（核心）
        fontSelectBox.addActionListener(e -> {
            // 获取选中的字体名称
            String selectedFontName = (String) fontSelectBox.getSelectedItem();
            // 更新当前字体
            currentChatFont = new Font(selectedFontName, Font.PLAIN, 14);
            // 立即修改聊天区域的字体（所有内容都会生效）
            chatArea.setFont(currentChatFont);
            // 提示（可选，可删除）
            JOptionPane.showMessageDialog(chatClient, "聊天字体已切换为：" + selectedFontName);
        });
        topPanel.add(fontSelectBox);

        chatTypeBox = new JComboBox<>(new String[]{"群聊", "私聊"});
        targetBox = new JComboBox<>();
        topPanel.add(new JLabel("聊天类型："));
        topPanel.add(chatTypeBox);
        topPanel.add(new JLabel("目标："));
        topPanel.add(targetBox);

        // 中间面板：聊天内容显示
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("宋体", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(chatArea);

        // 底部面板：输入+按钮（发送、文件、抖动）
        JPanel bottomPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        // 按钮面板：新增抖动按钮
        JPanel btnPanel = new JPanel();
        JButton sendBtn = new JButton("发送");
        fileBtn = new JButton("选择文件"); // 新增文件按钮
        shakeBtn = new JButton("窗口抖动"); // 新增抖动按钮
        // 新增：截图发送按钮
        JButton screenshotBtn = new JButton("截图发送");
        btnPanel.add(sendBtn);
        btnPanel.add(fileBtn);
        btnPanel.add(screenshotBtn); // 添加截图按钮
        btnPanel.add(shakeBtn); // 添加抖动按钮
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(btnPanel, BorderLayout.EAST);

        // 组装右侧聊天面板
        chatMainPanel.add(topPanel, BorderLayout.NORTH);
        chatMainPanel.add(scrollPane, BorderLayout.CENTER);
        chatMainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // ========== 整体布局：左（用户列表）+ 右（聊天面板） ==========
        chatClient.add(leftPanel, BorderLayout.WEST);
        chatClient.add(chatMainPanel, BorderLayout.CENTER);

        // 发送按钮事件
        sendBtn.addActionListener(e -> chatClient.sendMessage());

        // 回车发送消息
        inputField.addActionListener(e -> chatClient.sendMessage());

        // 新增：文件按钮点击事件
        fileBtn.addActionListener(e -> chatClient.selectFile());

        // 新增：抖动按钮点击事件
        shakeBtn.addActionListener(e -> chatClient.sendShakeMessage());

        // 新增：群操作按钮事件
        createGroupBtn.addActionListener(e -> chatClient.showCreateGroupDialog());
        searchGroupBtn.addActionListener(e -> chatClient.showSearchGroupDialog());
        joinGroupBtn.addActionListener(e -> chatClient.showJoinGroupDialog());

        // 新增：截图按钮点击事件
        screenshotBtn.addActionListener(e -> chatClient.handleScreenshot());

        // 聊天类型切换事件
        chatTypeBox.addActionListener(e -> {
            if (chatTypeBox.getSelectedItem().equals("群聊")) {
                targetBox.removeAllItems();
                for (int i = 0; i < groupListModel.size(); i++) {
                    targetBox.addItem(groupListModel.getElementAt(i));
                }
            } else {
                // 切换私聊时，显示在线用户列表（排除自己）
                targetBox.removeAllItems();
                for (int i = 0; i < userListModel.size(); i++) {
                    String user = userListModel.getElementAt(i);
                    if (!user.equals(username)) {
                        targetBox.addItem(user);
                    }
                }
            }
        });
        
        // 设置初始字体
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