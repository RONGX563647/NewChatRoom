package client.ui;

import client.ChatClient;

import javax.swing.*;
import java.awt.*;

/**
 * 登录注册界面组件类
 * 负责创建和管理登录注册相关的UI组件
 */
public class LoginRegisterUI {
    private ChatClient chatClient;
    
    // 登录注册界面组件
    private JFrame loginRegisterFrame;
    private JTextField loginAccountField;
    private JPasswordField loginPwdField;
    private JTextField registerAccountField;
    private JPasswordField registerPwdField;
    private JTextField serverIpField;
    
    public LoginRegisterUI(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 创建登录注册主界面
     */
    public void createLoginRegisterFrame() {
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
        JPanel loginPanel = client.ui.UIComponentFactory.createRoundedPanel(Color.WHITE, 30);
        loginPanel.setLayout(new GridBagLayout()); // 精准布局
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // 控件间距
        gbc.anchor = GridBagConstraints.CENTER;

        // 服务器IP标签 + 输入框
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel ipLabel = new JLabel("服务器IP：");
        ipLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        loginPanel.add(ipLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        serverIpField = client.ui.UIComponentFactory.createStyledTextField(); // 复用美化的输入框
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
        loginAccountField = client.ui.UIComponentFactory.createStyledTextField();
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
        loginPwdField = client.ui.UIComponentFactory.createStyledPasswordField();
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

        JButton loginBtn = client.ui.UIComponentFactory.createStyledButton("登录");
        JButton toRegisterBtn = client.ui.UIComponentFactory.createStyledButton("去注册");
        JButton findPwdBtn = client.ui.UIComponentFactory.createStyledButton("找回密码");

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

        // 为按钮添加事件监听器
        setupEventListeners(loginBtn, toRegisterBtn, findPwdBtn);

        loginRegisterFrame.setVisible(true);
    }

    /**
     * 为登录注册界面的按钮设置事件监听器
     */
    private void setupEventListeners(JButton loginBtn, JButton toRegisterBtn, JButton findPwdBtn) {
        // 登录按钮事件
        loginBtn.addActionListener(e -> {
            String serverIp = serverIpField.getText().trim();
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
            
            // 调用主类的登录方法
            chatClient.login(account, password, serverIp);
        });

        // 去注册按钮事件
        toRegisterBtn.addActionListener(e -> chatClient.showRegisterDialog());

        // 找回密码按钮点击事件
        findPwdBtn.addActionListener(e -> chatClient.showFindPasswordDialog());
    }

    // Getter方法
    public JFrame getLoginRegisterFrame() {
        return loginRegisterFrame;
    }

    public JTextField getLoginAccountField() {
        return loginAccountField;
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

    public JTextField getServerIpField() {
        return serverIpField;
    }
}