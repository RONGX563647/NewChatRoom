package client;

import client.ui.UIComponentFactory;
import common.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Map;

import client.network.NetworkManager;
import client.ui.ChatMainUI;
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
        JPanel loginPanel = UIComponentFactory.createRoundedPanel(Color.WHITE, 30);
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
        serverIpField = UIComponentFactory.createStyledTextField();
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
        loginAccountField = UIComponentFactory.createStyledTextField();
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
        loginPwdField = UIComponentFactory.createStyledPasswordField();
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

        JButton loginBtn = UIComponentFactory.createStyledButton("登录");
        JButton toRegisterBtn = UIComponentFactory.createStyledButton("去注册");
        JButton findPwdBtn = UIComponentFactory.createStyledButton("找回密码");

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

        confirmBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String account = accountField.getText().trim();
                if (account.isEmpty()) {
                    JOptionPane.showMessageDialog(findPwdDialog, "账号不能为空！");
                    return;
                }
                try {
                    if (networkManager == null || !networkManager.isConnected()) {
                        networkManager = new NetworkManager(ChatClient.this, serverIp);
                        if (!networkManager.connectToServer()) {
                            JOptionPane.showMessageDialog(findPwdDialog, "连接服务器失败！");
                            return;
                        }
                    }

                    Message findMsg = new Message(Message.Type.FIND_PASSWORD, account, "");
                    networkManager.sendMessage(findMsg);
                    
                    Message response = networkManager.receiveMessage();
                    JOptionPane.showMessageDialog(findPwdDialog, response.getContent());

                    if (response.getContent().contains("验证通过")) {
                        findPwdDialog.dispose();
                        showResetPasswordDialog(account);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(findPwdDialog, "验证账号失败：" + ex.getMessage());
                    resetSocket();
                    ex.printStackTrace();
                }
            }
        });

        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                findPwdDialog.dispose();
            }
        });

        findPwdDialog.setVisible(true);
    }

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

        resetBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newPassword = new String(newPwdField.getPassword()).trim();
                if (newPassword.isEmpty()) {
                    JOptionPane.showMessageDialog(resetPwdDialog, "新密码不能为空！");
                    return;
                }
                try {
                    Message resetMsg = new Message(Message.Type.RESET_PASSWORD, account, "");
                    resetMsg.setPassword(newPassword);
                    networkManager.sendMessage(resetMsg);

                    Message response = networkManager.receiveMessage();
                    JOptionPane.showMessageDialog(resetPwdDialog, response.getContent());

                    if (response.getContent().contains("成功")) {
                        resetPwdDialog.dispose();
                        loginPwdField.setText("");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(resetPwdDialog, "重置密码失败：" + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetPwdDialog.dispose();
            }
        });

        resetPwdDialog.setVisible(true);
    }

    public void showRegisterDialog() {
        JDialog registerDialog = new JDialog(loginRegisterFrame, "用户注册", true);
        registerDialog.setSize(400, 300);
        registerDialog.setLocationRelativeTo(loginRegisterFrame);
        registerDialog.setResizable(false);
        registerDialog.getContentPane().setBackground(new Color(240, 248, 255));

        JPanel registerPanel = UIComponentFactory.createRoundedPanel(Color.WHITE, 20);
        registerPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel accountLabel = new JLabel("账号：");
        accountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        registerPanel.add(accountLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        registerAccountField = UIComponentFactory.createStyledTextField();
        registerPanel.add(registerAccountField, gbc);

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
        registerPwdField = UIComponentFactory.createStyledPasswordField();
        registerPanel.add(registerPwdField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        JButton registerBtn = UIComponentFactory.createStyledButton("注册");
        JButton cancelBtn = UIComponentFactory.createStyledButton("取消");
        btnPanel.add(registerBtn);
        btnPanel.add(Box.createHorizontalStrut(15));
        btnPanel.add(cancelBtn);
        registerPanel.add(btnPanel, gbc);

        registerDialog.add(registerPanel);

        registerBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String account = registerAccountField.getText().trim();
                String password = new String(registerPwdField.getPassword()).trim();
                if (account.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(registerDialog, "账号和密码不能为空！");
                    return;
                }
                register(account, password);
                registerDialog.dispose();
            }
        });

        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerDialog.dispose();
            }
        });

        registerDialog.setVisible(true);
    }

    private void register(String account, String password) {
        try {
            if (networkManager == null || !networkManager.isConnected()) {
                networkManager = new NetworkManager(this, serverIp);
                if (!networkManager.connectToServer()) {
                    JOptionPane.showMessageDialog(loginRegisterFrame, "连接服务器失败！");
                    return;
                }
            }

            Message registerMsg = new Message(Message.Type.REGISTER, account, "");
            registerMsg.setPassword(password);
            networkManager.sendMessage(registerMsg);

            Message response = networkManager.receiveMessage();
            JOptionPane.showMessageDialog(loginRegisterFrame, response.getContent());

            if (response.getContent().contains("成功")) {
                registerAccountField.setText("");
                registerPwdField.setText("");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(loginRegisterFrame, "注册失败：" + e.getMessage());
            resetSocket();
            e.printStackTrace();
        }
    }

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
            chatManager.sendCreateGroupRequest(groupName);
        }
    }

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
            chatManager.sendSearchGroupRequest(groupId);
        }
    }

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
            chatManager.sendJoinGroupRequest(groupId);
        }
    }

    private ChatMainUI chatMainUI;
    private MessageHandler messageHandler;

    private client.managers.ChatManager chatManager;
    private client.managers.FileManager fileManager;
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
        windowManager = new client.managers.WindowManager(this);
        
        setTitle("Java Socket 聊天室(账号：" + username + ")");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
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
