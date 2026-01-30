package client;

import common.Group;
import common.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import client.network.NetworkManager;
import client.ui.ChatMainUI;
import client.ui.LoginRegisterUI;
import client.handler.MessageHandler;

/**
 * 聊天室客户端
 */
public class ChatClient extends JFrame {
    // 网络管理器
    private NetworkManager networkManager;
    
    private String serverIp = "127.0.0.1";
    private String username; // 当前登录用户名/账号
    // 新增：服务器IP输入框
    private JTextField serverIpField;

    // 界面组件
    private JTextArea chatArea;      // 聊天内容显示区
    private JTextField inputField;   // 消息输入框
    private JComboBox<String> chatTypeBox; // 聊天类型选择（私聊/群聊）
    private JComboBox<String> targetBox; // 私聊：用户名 | 群聊：群ID（显示群名）
    private DefaultListModel<String> userListModel; // 用户列表数据模型

    // 新增：群聊相关（存储群ID和群名的映射，便于显示）
    private DefaultListModel<String> groupListModel; // 群列表显示模型（显示群名）
    private Map<String, String> groupIdToNameMap = new java.util.HashMap<>(); // 群ID→群名
    private Map<String, String> groupNameToIdMap = new java.util.HashMap<>(); // 群名→群ID

    // 登录注册界面组件（新增）
    private JFrame loginRegisterFrame; // 登录注册主窗口
    private JTextField loginAccountField; // 登录账号输入框
    private JPasswordField loginPwdField; // 登录密码输入框
    private JTextField registerAccountField; // 注册账号输入框
    private JPasswordField registerPwdField; // 注册密码输入框

    public ChatClient() {
        uiComponentFactory = new UIComponentFactory();
        // 启动时先显示登录注册界面
        showLoginRegisterFrame();
    }

    /**
     * 登录方法（带服务器IP参数）
     */
    public void login(String account, String password, String serverIp) {
        this.serverIp = serverIp;
        login(account, password);
    }

    // Getter方法
    public Map<String, String> getGroupIdToNameMap() {
        return groupIdToNameMap;
    }

    public Map<String, String> getGroupNameToIdMap() {
        return groupNameToIdMap;
    }

    public JComboBox<String> getChatTypeBox() {
        return chatTypeBox;
    }

    public JComboBox<String> getTargetBox() {
        return targetBox;
    }

    public JTextArea getChatArea() {
        return chatArea;
    }

    public JTextField getInputField() {
        return inputField;
    }

    public DefaultListModel<String> getUserListModel() {
        return userListModel;
    }

    public DefaultListModel<String> getGroupListModel() {
        return groupListModel;
    }

    public String getUsername() {
        return username;
    }

    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public JFrame getLoginRegisterFrame() {
        return loginRegisterFrame;
    }

    public JPasswordField getLoginPwdField() {
        return loginPwdField;
    }

    public JTextField getRegisterAccountField() {
        return registerAccountField;
    }

    public JPasswordField getRegisterPwdField() {
        return registerPwdField;
    }

    /**
     * 新增：显示登录注册主界面
     */
    public void showLoginRegisterFrame() {
        loginRegisterFrame = new JFrame("Java 聊天室 - 登录/注册");
        loginRegisterFrame.setSize(500, 450);
        loginRegisterFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginRegisterFrame.setLocationRelativeTo(null);
        loginRegisterFrame.setResizable(false); // 禁止调整窗口大小
        loginRegisterFrame.getContentPane().setBackground(new Color(240, 248, 255)); // 浅蓝背景

        // 顶部标题面板
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Java 聊天室");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(64, 158, 255));
        titlePanel.add(titleLabel);

        // 核心登录面板（美化的圆角面板）
        JPanel loginPanel = uiComponentFactory.createRoundedPanel(Color.WHITE, 30);
        loginPanel.setLayout(new GridBagLayout()); // 精准布局
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // 控件间距
        gbc.anchor = GridBagConstraints.CENTER;

        // ========== 新增：服务器IP标签 + 输入框 ==========
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel ipLabel = new JLabel("服务器IP：");
        ipLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        loginPanel.add(ipLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        serverIpField = uiComponentFactory.createStyledTextField();
        serverIpField.setText("127.0.0.1"); // 默认显示本地IP
        serverIpField.setToolTipText("请输入服务器的IP地址（如192.168.1.100）");
        loginPanel.add(serverIpField, gbc);

        // 账号标签 + 输入框（原有逻辑，gridy改为1）
        gbc.gridx = 0;
        gbc.gridy = 1; // 序号后移，给IP输入框让位
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel accountLabel = new JLabel("账号：");
        accountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        loginPanel.add(accountLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        loginAccountField = uiComponentFactory.createStyledTextField();
        loginPanel.add(loginAccountField, gbc);

        // 密码标签 + 密码框（原有逻辑，gridy改为2）
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel pwdLabel = new JLabel("密码：");
        pwdLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        loginPanel.add(pwdLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        loginPwdField = uiComponentFactory.createStyledPasswordField();
        loginPanel.add(loginPwdField, gbc);

        // 按钮面板（原有逻辑，gridy改为3）
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton loginBtn = uiComponentFactory.createStyledButton("登录");
        JButton toRegisterBtn = uiComponentFactory.createStyledButton("去注册");
        JButton findPwdBtn = uiComponentFactory.createStyledButton("找回密码");

        btnPanel.add(loginBtn);
        btnPanel.add(Box.createHorizontalStrut(15));
        btnPanel.add(toRegisterBtn);
        btnPanel.add(Box.createHorizontalStrut(15));
        btnPanel.add(findPwdBtn);
        loginPanel.add(btnPanel, gbc);

        // 组装主窗口
        loginRegisterFrame.setLayout(new BorderLayout(0, 20));
        loginRegisterFrame.add(titlePanel, BorderLayout.NORTH);
        loginRegisterFrame.add(loginPanel, BorderLayout.CENTER);

        // 新增：找回密码按钮点击事件
        findPwdBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showFindPasswordDialog();
            }
        });

        // 登录按钮事件
        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 1. 校验IP输入
                serverIp = serverIpField.getText().trim();
                if (serverIp.isEmpty()) {
                    JOptionPane.showMessageDialog(loginRegisterFrame, "服务器IP不能为空！");
                    return;
                }

                String account = loginAccountField.getText().trim();
                String password = new String(loginPwdField.getPassword()).trim();
                if (account.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(loginRegisterFrame, "账号和密码不能为空！");
                    return;
                }
                // 连接服务器并登录
                login(account, password);
            }
        });

        // 去注册按钮事件
        toRegisterBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRegisterDialog();
            }
        });

        loginRegisterFrame.setVisible(true);
    }

    /**
     * 第一步：找回密码 - 输入账号验证
     */
    public void showFindPasswordDialog() {
        JDialog findPwdDialog = new JDialog(loginRegisterFrame, "找回密码 - 验证账号", true);
        findPwdDialog.setSize(350, 150);
        findPwdDialog.setLocationRelativeTo(loginRegisterFrame);
        findPwdDialog.setLayout(new GridLayout(2, 2, 10, 10));
        ((JComponent) findPwdDialog.getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField accountField = new JTextField();
        JButton confirmBtn = new JButton("验证账号");
        JButton cancelBtn = new JButton("取消");

        findPwdDialog.add(new JLabel("请输入注册账号："));
        findPwdDialog.add(accountField);
        findPwdDialog.add(confirmBtn);
        findPwdDialog.add(cancelBtn);

        // 验证账号按钮事件
        confirmBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String account = accountField.getText().trim();
                if (account.isEmpty()) {
                    JOptionPane.showMessageDialog(findPwdDialog, "账号不能为空！");
                    return;
                }
                // 发送验证账号请求
                try {
                    // 确保连接服务器
                    if (networkManager == null || !networkManager.isConnected()) {
                        networkManager = new NetworkManager(ChatClient.this, serverIp);
                        if (!networkManager.connectToServer()) {
                            JOptionPane.showMessageDialog(findPwdDialog, "连接服务器失败！");
                            return;
                        }
                    }

                    // 构造验证账号消息
                    Message findMsg = new Message(Message.Type.FIND_PASSWORD, account, "");
                    networkManager.sendMessage(findMsg);
                    
                    // 接收验证响应
                    Message response = networkManager.receiveMessage();
                    JOptionPane.showMessageDialog(findPwdDialog, response.getContent());

                    // 验证通过 → 第二步：输入新密码
                    if (response.getContent().contains("验证通过")) {
                        findPwdDialog.dispose();
                        // 传入验证通过的账号，进入重置密码步骤
                        showResetPasswordDialog(account);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(findPwdDialog, "验证账号失败：" + ex.getMessage());
                    resetSocket(); // 重置Socket
                    ex.printStackTrace();
                }
            }
        });

        // 取消按钮事件
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                findPwdDialog.dispose();
            }
        });

        findPwdDialog.setVisible(true);
    }

    /**
     * 第二步：找回密码 - 输入新密码重置
     */
    private void showResetPasswordDialog(String account) {
        JDialog resetPwdDialog = new JDialog(loginRegisterFrame, "找回密码 - 重置密码", true);
        resetPwdDialog.setSize(350, 150);
        resetPwdDialog.setLocationRelativeTo(loginRegisterFrame);
        resetPwdDialog.setLayout(new GridLayout(2, 2, 10, 10));
        ((JComponent) resetPwdDialog.getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPasswordField newPwdField = new JPasswordField();
        JButton resetBtn = new JButton("重置密码");
        JButton cancelBtn = new JButton("取消");

        resetPwdDialog.add(new JLabel("请输入新密码："));
        resetPwdDialog.add(newPwdField);
        resetPwdDialog.add(resetBtn);
        resetPwdDialog.add(cancelBtn);

        // 重置密码按钮事件
        resetBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newPassword = new String(newPwdField.getPassword()).trim();
                if (newPassword.isEmpty()) {
                    JOptionPane.showMessageDialog(resetPwdDialog, "新密码不能为空！");
                    return;
                }
                // 发送重置密码请求
                try {
                    Message resetMsg = new Message(Message.Type.RESET_PASSWORD, account, "");
                    resetMsg.setPassword(newPassword); // 设置新密码
                    networkManager.sendMessage(resetMsg);

                    // 接收重置响应
                    Message response = networkManager.receiveMessage();
                    JOptionPane.showMessageDialog(resetPwdDialog, response.getContent());

                    // 重置成功 → 关闭对话框，返回登录界面
                    if (response.getContent().contains("成功")) {
                        resetPwdDialog.dispose();
                        // 清空登录界面的密码框
                        loginPwdField.setText("");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(resetPwdDialog, "重置密码失败：" + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

        // 取消按钮事件
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetPwdDialog.dispose();
            }
        });

        resetPwdDialog.setVisible(true);
    }


    /**
     * 新增：显示注册对话框
     */
    public void showRegisterDialog() {
        JDialog registerDialog = new JDialog(loginRegisterFrame, "用户注册", true);
        registerDialog.setSize(400, 300);
        registerDialog.setLocationRelativeTo(loginRegisterFrame);
        registerDialog.setResizable(false);
        registerDialog.getContentPane().setBackground(new Color(240, 248, 255));

        // 注册面板（美化的圆角面板）
        JPanel registerPanel = uiComponentFactory.createRoundedPanel(Color.WHITE, 20);
        registerPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        // 账号标签 + 输入框
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel accountLabel = new JLabel("账号：");
        accountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        registerPanel.add(accountLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        registerAccountField = uiComponentFactory.createStyledTextField();
        registerPanel.add(registerAccountField, gbc);

        // 密码标签 + 密码框
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel pwdLabel = new JLabel("密码：");
        pwdLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        registerPanel.add(pwdLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        registerPwdField = uiComponentFactory.createStyledPasswordField();
        registerPanel.add(registerPwdField, gbc);

        // 按钮面板
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        JButton registerBtn = uiComponentFactory.createStyledButton("注册");
        JButton cancelBtn = uiComponentFactory.createStyledButton("取消");
        btnPanel.add(registerBtn);
        btnPanel.add(Box.createHorizontalStrut(15));
        btnPanel.add(cancelBtn);
        registerPanel.add(btnPanel, gbc);

        // 组装对话框
        registerDialog.add(registerPanel);

        // 注册按钮事件
        registerBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String account = registerAccountField.getText().trim();
                String password = new String(registerPwdField.getPassword()).trim();
                if (account.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(registerDialog, "账号和密码不能为空！");
                    return;
                }
                // 发送注册请求
                register(account, password);
                registerDialog.dispose();
            }
        });

        // 取消按钮事件
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerDialog.dispose();
            }
        });

        registerDialog.setVisible(true);
    }

    /**
     * 新增：发送注册请求
     */
    private void register(String account, String password) {
        try {
            // 确保连接服务器
            if (networkManager == null || !networkManager.isConnected()) {
                networkManager = new NetworkManager(this, serverIp);
                if (!networkManager.connectToServer()) {
                    JOptionPane.showMessageDialog(loginRegisterFrame, "连接服务器失败！");
                    return;
                }
            }

            // 构造注册消息
            Message registerMsg = new Message(Message.Type.REGISTER, account, "");
            registerMsg.setPassword(password);
            networkManager.sendMessage(registerMsg);

            // 接收注册响应
            Message response = networkManager.receiveMessage();
            JOptionPane.showMessageDialog(loginRegisterFrame, response.getContent());

            // 注册成功后清空输入框
            if (response.getContent().contains("成功")) {
                registerAccountField.setText("");
                registerPwdField.setText("");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(loginRegisterFrame, "注册失败：" + e.getMessage());
            resetSocket(); // 重置Socket
            e.printStackTrace();
        }
    }

    // UI组件
    private LoginRegisterUI loginRegisterUI;
    private ChatMainUI chatMainUI;
    private MessageHandler messageHandler;
    private UIComponentFactory uiComponentFactory;

    // Manager组件
    private client.managers.ChatManager chatManager;
    private client.managers.FileManager fileManager;
    private client.managers.DataManager dataManager;
    private client.managers.WindowManager windowManager;
    
    public void initChatUI() {
        chatMainUI = new ChatMainUI(this);
        messageHandler = new MessageHandler(this, chatMainUI);
        
        chatMainUI.initChatUI(username);
        
        chatArea = chatMainUI.getChatArea();
        inputField = chatMainUI.getInputField();
        chatTypeBox = chatMainUI.getChatTypeBox();
        targetBox = chatMainUI.getTargetBox();
        userListModel = chatMainUI.getUserListModel();
        groupListModel = chatMainUI.getGroupListModel();
        
        chatManager = new client.managers.ChatManager(this);
        fileManager = new client.managers.FileManager(this);
        dataManager = new client.managers.DataManager(this);
        windowManager = new client.managers.WindowManager(this);
        
        setTitle("Java Socket 聊天室(账号：" + username + ")");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    // ========== 新增群聊相关对话框 ==========
    /**
     * 显示创建群聊对话框
     */
    public void showCreateGroupDialog() {
        JTextField groupNameField = new JTextField(20);
        JPanel panel = new JPanel();
        panel.add(new JLabel("请输入群聊名称："));
        panel.add(groupNameField);

        int result = JOptionPane.showConfirmDialog(
                this, panel, "创建新群聊", JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            String groupName = groupNameField.getText().trim();
            if (groupName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "群聊名称不能为空！");
                return;
            }
            // 发送创建群聊请求
            sendCreateGroupRequest(groupName);
        }
    }

    /**
     * 显示查找群聊对话框
     */
    public void showSearchGroupDialog() {
        JTextField groupIdField = new JTextField(20);
        JPanel panel = new JPanel();
        panel.add(new JLabel("请输入群聊ID："));
        panel.add(groupIdField);

        int result = JOptionPane.showConfirmDialog(
                this, panel, "查找群聊", JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            String groupId = groupIdField.getText().trim();
            if (groupId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "群聊ID不能为空！");
                return;
            }
            // 发送查找群聊请求
            sendSearchGroupRequest(groupId);
        }
    }

    /**
     * 显示加入群聊对话框
     */
    public void showJoinGroupDialog() {
        JTextField groupIdField = new JTextField(20);
        JPanel panel = new JPanel();
        panel.add(new JLabel("请输入群聊ID："));
        panel.add(groupIdField);

        int result = JOptionPane.showConfirmDialog(
                this, panel, "加入群聊", JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            String groupId = groupIdField.getText().trim();
            if (groupId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "群聊ID不能为空！");
                return;
            }
            // 发送加入群聊请求
            sendJoinGroupRequest(groupId);
        }
    }

    // ========== 新增群聊请求发送方法 ==========
    /**
     * 发送创建群聊请求
     */
    private void sendCreateGroupRequest(String groupName) {
        try {
            Message createMsg = new Message(Message.Type.CREATE_GROUP, username,"");
            createMsg.setGroupName(groupName); // 设置群名
            networkManager.sendMessage(createMsg);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "发送创建群聊请求失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 发送查找群聊请求
     */
    private void sendSearchGroupRequest(String groupId) {
        try {
            Message joinMsg = new Message(Message.Type.JOIN_GROUP, username, "");
            joinMsg.setGroupId(groupId); // 设置群ID
            networkManager.sendMessage(joinMsg);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "发送查找群聊请求失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 发送加入群聊请求
     */
    private void sendJoinGroupRequest(String groupId) {
        try {
            Message joinMsg = new Message(Message.Type.JOIN_GROUP, username, groupId, "");
            networkManager.sendMessage(joinMsg);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "发送加入群聊请求失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 连接服务器
     */
    private void login(String account, String password) {
        try {
            // 初始化网络管理器并连接服务器
            networkManager = new NetworkManager(this, serverIp);
            if (!networkManager.connectToServer()) {
                return; // 连接失败则返回
            }

            // 发送登录消息
            Message loginMsg = new Message(Message.Type.LOGIN, account, "");
            loginMsg.setPassword(password);
            networkManager.sendMessage(loginMsg);

            // 接收登录响应
            Message response = networkManager.receiveMessage();
            if (response.getContent().contains("成功")) {
                username = account;
                // 关闭登录注册窗口，显示聊天界面
                loginRegisterFrame.dispose();
                initChatUI();
                setVisible(true);
                chatArea.append("【系统消息】" + response.getContent() + "\n");

                // 新增：请求在线用户列表
                Message getUsersMsg = new Message(Message.Type.GET_ONLINE_USERS, username, "", "");
                networkManager.sendMessage(getUsersMsg);

                startMessageListener();
            } else {
                JOptionPane.showMessageDialog(loginRegisterFrame, response.getContent());
                // 密码错误时清空密码框
                loginPwdField.setText("");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(loginRegisterFrame, "登录失败：" + e.getMessage());
            e.printStackTrace();
            // 异常时重置Socket（避免残留失效连接）
            resetSocket();
            System.exit(1);
        }
    }

    public void resetSocket() {
        if (networkManager != null) {
            networkManager.resetSocket();
        }
    }

    public void startMessageListener() {
        MessageListener listener = new MessageListener(networkManager, messageHandler, chatArea);
        listener.start();
    }

    public void handleScreenshot() {
        ScreenshotManager screenshotManager = new ScreenshotManager(this);
        screenshotManager.handleScreenshot(fileManager.getSelectedFile(), fileManager::sendFile, chatTypeBox);
    }

    public void sendMessage() {
        chatManager.sendMessage();
    }

    public void selectFile() {
        fileManager.selectFile();
    }

    public void sendFile() {
        fileManager.sendFile();
    }

    public void saveBytesToFile(byte[] data, String fileName) {
        dataManager.saveBytesToFile(data, fileName);
    }

    public void updateUserList(List<String> onlineUsers) {
        dataManager.updateUserList(onlineUsers);
    }

    public void updateGroupList(List<Group> groupList) {
        dataManager.updateGroupList(groupList);
    }

    public void sendShakeMessage() {
        chatManager.sendShake();
    }

    public void shakeWindow() {
        windowManager.shakeWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatClient();
            }
        });
    }
}
