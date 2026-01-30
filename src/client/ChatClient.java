package client;

import common.Group;
import common.Message;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.image.BufferedImage;
import java.io.*;

import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.Timer;
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
    private JList<String> userList;  // 在线用户列表
    private DefaultListModel<String> userListModel; // 用户列表数据模型
    private JButton fileBtn; // 新增：选择文件按钮
    private File selectedFile; // 新增：选中的文件
    private JButton shakeBtn; // 新增：窗口抖动按钮

    // 窗口抖动相关：记录原始位置，避免重复抖动
    private Point originalLocation;
    private boolean isShaking = false;

    // 新增：群聊相关（存储群ID和群名的映射，便于显示）
    private DefaultListModel<String> groupListModel; // 群列表显示模型（显示群名）
    private JList<String> groupList;
    private JPanel groupPanel; // 群列表面板
    private Map<String, String> groupIdToNameMap = new java.util.HashMap<>(); // 群ID→群名
    private Map<String, String> groupNameToIdMap = new java.util.HashMap<>(); // 群名→群ID

    // 登录注册界面组件（新增）
    private JFrame loginRegisterFrame; // 登录注册主窗口
    private JTextField loginAccountField; // 登录账号输入框
    private JPasswordField loginPwdField; // 登录密码输入框
    private JTextField registerAccountField; // 注册账号输入框
    private JPasswordField registerPwdField; // 注册密码输入框

    // 字体相关（新增）
    private JComboBox<String> fontSelectBox; // 字体选择下拉框
    private Font currentChatFont; // 当前选中的聊天字体

    public ChatClient() {
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

    public boolean isShaking() {
        return isShaking;
    }

    public void setShaking(boolean shaking) {
        isShaking = shaking;
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
        JPanel loginPanel = createRoundedPanel(Color.WHITE, 30);
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
        serverIpField = createStyledTextField(); // 复用美化的输入框
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
        loginAccountField = createStyledTextField();
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
        loginPwdField = createStyledPasswordField();
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

        JButton loginBtn = createStyledButton("登录");
        JButton toRegisterBtn = createStyledButton("去注册");
        JButton findPwdBtn = createStyledButton("找回密码");

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
        JPanel registerPanel = createRoundedPanel(Color.WHITE, 20);
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
        registerAccountField = createStyledTextField();
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
        registerPwdField = createStyledPasswordField();
        registerPanel.add(registerPwdField, gbc);

        // 按钮面板
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        JButton registerBtn = createStyledButton("注册");
        JButton cancelBtn = createStyledButton("取消");
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
    
    /**
     * 初始化主界面
     */
    private void initChatUI() {
        // 创建UI组件实例
        chatMainUI = new ChatMainUI(this);
        messageHandler = new MessageHandler(this, chatMainUI);
        
        // 使用UI组件初始化界面
        chatMainUI.initChatUI(username);
        
        // 设置界面组件引用
        chatArea = chatMainUI.getChatArea();
        inputField = chatMainUI.getInputField();
        chatTypeBox = chatMainUI.getChatTypeBox();
        targetBox = chatMainUI.getTargetBox();
        userList = chatMainUI.getUserList();
        userListModel = chatMainUI.getUserListModel();
        fileBtn = chatMainUI.getFileBtn();
        shakeBtn = chatMainUI.getShakeBtn();
        groupListModel = chatMainUI.getGroupListModel();
        groupList = chatMainUI.getGroupList();
        groupPanel = chatMainUI.getGroupPanel();
        fontSelectBox = chatMainUI.getFontSelectBox();
        currentChatFont = chatMainUI.getCurrentChatFont();
        
        // 窗口设置
        setTitle("Java Socket 聊天室(账号：" + username + ")");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    /**
     * 截取整个屏幕并保存为临时PNG文件
     * @return 生成的临时截图文件，失败返回null
     */
    private File captureFullScreen() {
        try {
            // 1. 创建Robot对象（用于截图）
            Robot robot = new Robot();

            // 2. 获取屏幕尺寸（多屏也能覆盖）
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

            // 3. 截取屏幕
            BufferedImage screenImage = robot.createScreenCapture(screenRect);

            // 4. 创建临时文件（唯一文件名，避免冲突）
            String tempFileName = "screenshot_" + UUID.randomUUID() + ".png";
            File tempFile = new File(System.getProperty("java.io.tmpdir"), tempFileName);

            // 5. 保存截图为PNG格式
            ImageIO.write(screenImage, "PNG", tempFile);

            JOptionPane.showMessageDialog(this, "截图成功！临时文件路径：" + tempFile.getAbsolutePath());
            return tempFile;

        } catch (AWTException ex) {
            JOptionPane.showMessageDialog(this, "截图权限不足！请检查是否允许程序访问屏幕。");
            ex.printStackTrace();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "保存截图失败：" + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
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

                // 启动线程监听服务器消息
                new MessageListener().start();
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

    // 新增：重置Socket的辅助方法（客户端）
    private void resetSocket() {
        if (networkManager != null) {
            networkManager.resetSocket();
        }
    }

    /**
     * 发送消息
     */
    public void sendMessage() {
        String content = inputField.getText().trim();
        if (content.isEmpty()) {
            return;
        }

        try {
            Message.Type type;
            String targetName = (String) targetBox.getSelectedItem();
            String target = null; // 最终发送给服务端的目标（群ID/用户名）

            if (chatTypeBox.getSelectedItem().equals("群聊")) {
                type = Message.Type.GROUP_CHAT;
                // 优先从ClientProperty获取群ID（更可靠）
                target = (String) targetBox.getClientProperty("groupId");
                // 兜底：从映射中获取
                if (target == null || target.isEmpty()) {
                    target = groupNameToIdMap.get(targetName);
                }
                // 空值校验（关键）
                if (target == null || target.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "未找到该群聊的ID，无法发送消息！");
                    return;
                }
            } else {
                type = Message.Type.PRIVATE_CHAT;
                target = targetName; // 私聊直接用用户名
            }

            // 发送消息
            Message message = new Message(type, username, target, content);
            networkManager.sendMessage(message);

            // 本地显示发送的消息
            if (type == Message.Type.GROUP_CHAT) {
                chatArea.append("【群聊-" + target + "】我：" + content + "\n");
            } else {
                chatArea.append("【私聊-" + target + "】我：" + content + "\n");
            }

            // 清空输入框
            inputField.setText("");
        } catch (Exception   e) {
            JOptionPane.showMessageDialog(this, "发送消息失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    // 新增：选择文件
    public void selectFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            chatArea.append("【系统消息】已选择文件：" + selectedFile.getName() + "（大小：" + formatFileSize(selectedFile.length()) + "）\n");
            // 选择文件后，弹出确认发送对话框
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "是否发送文件：" + selectedFile.getName() + " 到 " + targetBox.getSelectedItem() + "？",
                    "确认发送文件",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                sendFile(); // 发送选中的文件
            }
        }
    }

    // 新增：发送文件
    private void sendFile() {
        if (selectedFile == null || !selectedFile.exists()) {
            JOptionPane.showMessageDialog(this, "请先选择有效的文件！");
            return;
        }

        try {
            // 读取文件为字节数组（简易版：适合小文件，大文件需分块）
            byte[] fileData = readFileToBytes(selectedFile);
            if (fileData == null) {
                chatArea.append("【系统消息】文件读取失败！\n");
                return;
            }

            // 确定消息类型（私聊文件/群聊文件）
            Message.Type type;
            String targetName = (String) targetBox.getSelectedItem();
            String target = null;

            if (chatTypeBox.getSelectedItem().equals("群聊")) {
                type = Message.Type.FILE_GROUP;
                target = (String) targetBox.getClientProperty("groupId");
                if (target == null || target.isEmpty()) {
                    target = groupNameToIdMap.get(targetName);
                }
                if (target == null || target.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "未找到该群聊的ID，无法发送文件！");
                    return;
                }
            } else {
                type = Message.Type.FILE_PRIVATE;
                target = targetName;
            }

            // 构造文件消息并发送
            Message fileMsg = new Message(
                    type,
                    username,
                    target,
                    selectedFile.getName(),
                    selectedFile.length(),
                    fileData
            );
            networkManager.sendMessage(fileMsg);

            chatArea.append("【系统消息】文件 " + selectedFile.getName() + " 发送成功！\n");
            selectedFile = null; // 发送后清空选中的文件
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "发送文件失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    // 新增：读取文件为字节数组
    private byte[] readFileToBytes(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024 * 8]; // 8KB缓冲区
            int len;
            while ((len = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 重写：保存字节数组为文件
    public void saveBytesToFile(byte[] data, String fileName) {
        // 选择保存路径（默认当前目录）
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(fileName));
        int result = fileChooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File saveFile = fileChooser.getSelectedFile();
            try (FileOutputStream fos = new FileOutputStream(saveFile)) {
                fos.write(data);
                chatArea.append("【系统消息】已接收文件：" + fileName + "，保存至：" + saveFile.getAbsolutePath() + "\n");
            } catch (IOException e) {
                chatArea.append("【系统消息】文件保存失败：" + e.getMessage() + "\n");
                e.printStackTrace();
            }
        } else {
            chatArea.append("【系统消息】取消保存文件：" + fileName + "\n");
        }
    }

    // 新增：格式化文件大小（字节→KB/MB）
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        }
    }

    // 新增：更新在线用户列表UI
    private void updateUserList(List<String> onlineUsers) {
        // 注意：Swing组件更新必须在事件调度线程中执行
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (String user : onlineUsers) {
                userListModel.addElement(user);
            }
        });
    }
    // ========== 新增：更新群列表（线程安全） ==========
    private void updateGroupList(List<Group> groupList) {
        SwingUtilities.invokeLater(() -> {
            groupListModel.clear();
            groupIdToNameMap.clear      ();
            groupNameToIdMap.clear();

            for (Group group : groupList) {
                groupListModel.addElement(group.getGroupName());
                groupIdToNameMap.put(group.getGroupId(), group.getGroupName());
                groupNameToIdMap.put(group.getGroupName(), group.getGroupId());
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

    // 新增：发送窗口抖动消息
    public void sendShakeMessage() {
        String targetName = (String) targetBox.getSelectedItem();
        if (targetName == null || targetName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请选择聊天目标！");
            return;
        }

        try {
            String target = null;
            if (chatTypeBox.getSelectedItem().equals("群聊")) {
                target = (String) targetBox.getClientProperty("groupId");
                if (target == null || target.isEmpty()) {
                    target = groupNameToIdMap.get(targetName);
                }
                if (target == null || target.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "未找到该群聊的ID，无法发送抖动！");
                    return;
                }
            } else {
                target = targetName;
            }

            Message shakeMsg = new Message(Message.Type.SHAKE, username, target);
            networkManager.sendMessage(shakeMsg);

            // 本地提示
            if (chatTypeBox.getSelectedItem().equals("群聊")) {
                chatArea.append("【系统消息】已向群[" + target + "]发送窗口抖动！\n");
            } else {
                chatArea.append("【系统消息】已向" + target + "发送窗口抖动！\n");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "发送抖动失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    // 新增：执行窗口抖动动画（线程安全）
    private void shakeWindow() {
        // 防止重复抖动
        if (isShaking) {
            return;
        }
        isShaking = true;

        // 记录窗口原始位置
        originalLocation = this.getLocation();
        int shakeTimes = 8; // 抖动次数
        int shakeOffset = 5; // 抖动偏移量（像素）
        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            int count = 0;
            // 抖动方向：左右上下交替
            int[] dx = {shakeOffset, -shakeOffset, shakeOffset, -shakeOffset, shakeOffset, -shakeOffset, shakeOffset, -shakeOffset};
            int[] dy = {shakeOffset, -shakeOffset, -shakeOffset, shakeOffset, shakeOffset, -shakeOffset, -shakeOffset, shakeOffset};

            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    if (count < shakeTimes) {
                        // 移动窗口
                        setLocation(originalLocation.x + dx[count], originalLocation.y + dy[count]);
                        count++;
                    } else {
                        // 恢复原始位置，停止抖动
                        setLocation(originalLocation);
                        timer.cancel();
                        isShaking = false;
                    }
                });
            }
        }, 0, 50); // 每50毫秒抖动一次，共8次（400毫秒）
    }


    /**
     * 美化按钮：圆角、背景色、悬停效果
     */
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        // 设置按钮样式
        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        button.setBackground(new Color(64, 158, 255)); // 蓝色主色调
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20)); // 内边距
        button.setFocusPainted(false); // 去掉焦点框
        button.setBorderPainted(false); // 去掉边框
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // 手型光标
        // 圆角设置（通过设置按钮的形状）
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void installUI(JComponent c) {
                super.installUI(c);
                c.setOpaque(false);
                ((AbstractButton) c).setContentAreaFilled(false);
            }
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // 绘制圆角背景
                g2.setColor(c.getBackground());
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 8, 8);
                // 悬停效果
                if (c instanceof AbstractButton && ((AbstractButton) c).getModel().isRollover()) {
                    g2.setColor(new Color(84, 172, 255));
                    g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 8, 8);
                }
                super.paint(g2, c);
                g2.dispose();
            }
        });
        return button;
    }
    /**
     * 创建美化的输入框：圆角、边框
     */
    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        // 设置自定义圆角边框
        field.setBorder(new RoundBorder(8, new Color(204, 204, 204)));
        field.setOpaque(true); // 确保背景不透明，显示边框效果
        field.setBackground(Color.WHITE);
        return field;
    }
    /**
     * 美化密码框：复用自定义圆角边框
     */
    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        field.setBorder(new RoundBorder(8, new Color(204, 204, 204)));
        field.setOpaque(true);
        field.setBackground(Color.WHITE);
        return field;
    }
    /**
     * 创建圆角面板（带背景色和内边距）
     */
    private JPanel createRoundedPanel(Color bgColor, int padding) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
        panel.setOpaque(false); // 透明背景，显示圆角
        return panel;
    }

    /**
     * 自定义圆角边框（替代重写UI的方式）
     */
    private class RoundBorder extends javax.swing.border.AbstractBorder {
        private int radius; // 圆角半径
        private Color borderColor;

        public RoundBorder(int radius, Color borderColor) {
            this.radius = radius;
            this.borderColor = borderColor;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // 绘制圆角边框
            g2.setColor(borderColor);
            g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(8, 10, 8, 10); // 内边距，和之前保持一致
        }
    }

    /**
     * 监听服务器消息的线程
     */
    class MessageListener extends Thread {
        @Override
        public void run() {
            try {
                Message message;
                while (true) { // 持续监听消息
                    message = networkManager.receiveMessage();
                    if (message != null) {
                        // 使用消息处理器处理消息
                        messageHandler.handleMessage(message);
                    }
                }
            } catch (Exception e) { // 捕获所有异常
                SwingUtilities.invokeLater(() -> {
                    chatArea.append("【系统消息】与服务器断开连接！\n");
                });
                e.printStackTrace();
            }
        }
    }

    /**
     * 处理截图功能
     */
    public void handleScreenshot() {
        try {
            // 1. 截取整个屏幕
            File screenshotFile = captureFullScreen();
            if (screenshotFile == null || !screenshotFile.exists()) {
                JOptionPane.showMessageDialog(this, "截图失败！");
                return;
            }

            // 2. 确认发送（可选，提升体验）
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "截图已生成，是否发送给当前选中的" + (chatTypeBox.getSelectedItem().equals("群聊") ? "群聊" : "好友") + "？",
                    "确认发送截图",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) {
                // 用户取消发送，删除临时文件
                screenshotFile.delete();
                return;
            }

            // 3. 复用文件发送逻辑（关键：把截图文件赋值给selectedFile，调用sendFile）
            selectedFile = screenshotFile;
            sendFile();

            // 4. 发送完成后异步删除临时文件（避免占用空间）
            new Thread(() -> {
                try {
                    // 延迟1秒删除（确保文件已发送完成）
                    Thread.sleep(1000);
                    if (screenshotFile.exists()) {
                        screenshotFile.delete();
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "截图发送失败：" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Swing界面需在事件调度线程中运行
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatClient();
            }
        });
    }
}
