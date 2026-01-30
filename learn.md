# Java Socket 聊天室 - 从0开始开发教程

## 项目概述

这是一个基于 Java Socket 的网络聊天室应用，采用 C/S（客户端/服务端）架构，支持以下功能：

- 用户注册/登录/找回密码
- 私聊和群聊
- 文件传输（私聊和群聊）
- 窗口抖动功能
- 在线用户列表实时更新
- 群组管理（创建、搜索、加入）
- 自定义聊天字体

## 技术栈

- **Java SE** - 核心编程语言
- **Java Swing** - GUI界面开发
- **Java Socket** - 网络通信
- **Java IO** - 数据传输
- **多线程** - 并发处理

## 项目结构

```
NewChatRoom/
├── src/
│   ├── common/                 # 公共类（客户端和服务端共享）
│   │   ├── Message.java        # 消息实体类
│   │   └── Group.java          # 群组实体类
│   ├── server/                 # 服务端代码
│   │   ├── ChatServer.java     # 服务端主类
│   │   ├── core/
│   │   │   └── ServerManager.java
│   │   ├── handlers/
│   │   │   ├── ClientHandler.java
│   │   │   └── MessageHandler.java
│   │   ├── managers/
│   │   │   ├── UserManager.java
│   │   │   └── GroupManager.java
│   │   └── broadcast/
│   │       └── BroadcastManager.java
│   └── client/                 # 客户端代码
│       ├── ChatClient.java     # 客户端主类
│       ├── network/
│       │   └── NetworkManager.java
│       ├── handler/
│       │   └── MessageHandler.java
│       └── ui/
│           ├── ChatMainUI.java
│           ├── LoginRegisterUI.java
│           └── UIComponentFactory.java
```

---

## 第一阶段：基础架构搭建

### 1.1 创建公共消息类

首先创建 `common/Message.java`，这是客户端和服务端通信的基础：

```java
package common;
import java.io.Serializable;
import java.util.List;

/**
 * 消息实体类，用于客户端和服务端之间的通信
 */
public class Message implements Serializable {

    // 消息类型枚举
    public enum Type {
        LOGIN,              // 登录
        PRIVATE_CHAT,       // 私聊
        GROUP_CHAT,         // 群聊
        ONLINE_NOTIFY,      // 上线通知
        OFFLINE_NOTIFY,     // 下线通知
        GET_ONLINE_USERS,   // 获取在线用户
        ONLINE_USERS,       // 在线用户列表
        FILE_PRIVATE,       // 私聊文件
        FILE_GROUP,         // 群聊文件
        SHAKE,              // 窗口抖动
        CREATE_GROUP,       // 创建群组
        SEARCH_GROUP,       // 搜索群组
        JOIN_GROUP,         // 加入群组
        GROUP_LIST,         // 群组列表
        REGISTER,           // 注册
        REGISTER_RESPONSE,  // 注册响应
        FIND_PASSWORD,      // 找回密码
        FIND_PASSWORD_RESPONSE,
        RESET_PASSWORD,     // 重置密码
        RESET_PASSWORD_RESPONSE
    }

    private Type type;        // 消息类型
    private String sender;    // 发送者
    private String receiver;  // 接收者
    private String content;   // 消息内容
    private List<String> onlineUsers;  // 在线用户列表
    
    // 文件传输相关字段
    private String fileName;
    private long fileSize;
    private byte[] fileData;
    
    // 群聊相关字段
    private String groupId;
    private String groupName;
    private List<Group> groupList;
    
    private String password;  // 密码字段

    // 构造函数
    public Message(Type type, String sender, String receiver, String content) {
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
    }

    public Message(Type type, String sender) {
        this.type = type;
        this.sender = sender;
        this.content = "";
    }

    // Getter 和 Setter 方法...
}
```

**关键点说明：**
- 实现 `Serializable` 接口，使对象可以通过网络传输
- 使用枚举定义消息类型，便于管理和扩展
- 包含多种构造函数，适应不同场景

### 1.2 创建群组实体类

创建 `common/Group.java`：

```java
package common;

import java.util.ArrayList;
import java.util.List;

public class Group implements java.io.Serializable {
    private String groupId;      // 唯一群ID
    private String groupName;    // 群名称
    private List<String> members; // 群成员列表

    public Group(String groupId, String groupName) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.members = new ArrayList<>();
    }

    // 添加群成员
    public void addMember(String username) {
        if (!members.contains(username)) {
            members.add(username);
        }
    }

    // 移除群成员
    public void removeMember(String username) {
        members.remove(username);
    }

    // Getter & Setter...
}
```

---

## 第二阶段：服务端开发

### 2.1 基础服务端

创建 `server/ChatServer.java`：

```java
package server;

import common.Group;
import common.Message;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private static final int PORT = 8888;
    
    // 保存用户连接：用户名 -> 输出流
    private static Map<String, ObjectOutputStream> userMap = new ConcurrentHashMap<>();
    private static Map<String, Group> groupMap = new ConcurrentHashMap<>();
    private static Map<String, String> userAuthMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("聊天室服务端已启动，端口：" + PORT);

            // 初始化默认群组
            Group defaultGroup = new Group(UUID.randomUUID().toString(), "默认群");
            groupMap.put(defaultGroup.getGroupId(), defaultGroup);

            // 循环接收客户端连接
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("新客户端连接：" + clientSocket);
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 获取在线用户列表
    private static List<String> getOnlineUsers() {
        return new ArrayList<>(userMap.keySet());
    }

    // 推送在线用户列表给所有客户端
    private static void broadcastOnlineUsers() throws IOException {
        List<String> onlineUsers = getOnlineUsers();
        Message usersMsg = new Message(Message.Type.ONLINE_USERS, "服务器");
        usersMsg.setOnlineUsers(onlineUsers);
        for (ObjectOutputStream oos : userMap.values()) {
            oos.writeObject(usersMsg);
            oos.flush();
        }
    }
}
```

### 2.2 客户端处理线程

在 `ChatServer.java` 中添加内部类：

```java
/**
 * 客户端处理线程
 */
static class ClientHandler extends Thread {
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private String username;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // 初始化流（注意顺序：先输出后输入）
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            // 处理客户端消息
            Message message;
            while ((message = (Message) ois.readObject()) != null) {
                handleMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 客户端断开连接处理
            if (username != null) {
                userMap.remove(username);
                try {
                    broadcastOnlineUsers();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleMessage(Message message) throws IOException {
        switch (message.getType()) {
            case LOGIN:
                handleLogin(message);
                break;
            case PRIVATE_CHAT:
                handlePrivateChat(message);
                break;
            case GROUP_CHAT:
                handleGroupChat(message);
                break;
            // 其他消息类型处理...
        }
    }

    private void handleLogin(Message message) throws IOException {
        username = message.getSender();
        userMap.put(username, oos);
        
        // 发送登录成功响应
        Message response = new Message(
            Message.Type.LOGIN, 
            "服务器", 
            username, 
            "登录成功"
        );
        oos.writeObject(response);
        oos.flush();
        
        // 广播在线用户列表
        broadcastOnlineUsers();
    }

    private void handlePrivateChat(Message message) throws IOException {
        ObjectOutputStream targetOos = userMap.get(message.getReceiver());
        if (targetOos != null) {
            targetOos.writeObject(message);
            targetOos.flush();
        }
    }

    private void handleGroupChat(Message message) throws IOException {
        Group group = groupMap.get(message.getGroupId());
        if (group != null) {
            for (String member : group.getMembers()) {
                ObjectOutputStream memberOos = userMap.get(member);
                if (memberOos != null) {
                    memberOos.writeObject(message);
                    memberOos.flush();
                }
            }
        }
    }
}
```

---

## 第三阶段：客户端开发

### 3.1 网络管理器

创建 `client/network/NetworkManager.java`：

```java
package client.network;

import common.Message;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class NetworkManager {
    private String serverIp;
    private static final int SERVER_PORT = 8888;
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    public NetworkManager(String serverIp) {
        this.serverIp = serverIp;
    }

    // 连接到服务器
    public boolean connectToServer() {
        try {
            socket = new Socket(serverIp, SERVER_PORT);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, 
                "连接服务器失败：" + e.getMessage());
            return false;
        }
    }

    // 发送消息
    public void sendMessage(Message message) {
        try {
            if (oos != null) {
                oos.writeObject(message);
                oos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 接收消息
    public Message receiveMessage() {
        try {
            if (ois != null) {
                return (Message) ois.readObject();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 关闭连接
    public void closeConnection() {
        try {
            if (ois != null) ois.close();
            if (oos != null) oos.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

### 3.2 UI组件工厂

创建 `client/ui/UIComponentFactory.java`：

```java
package client.ui;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;

public class UIComponentFactory {

    // 创建美化样式的按钮
    public static JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        button.setBackground(new Color(64, 158, 255));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 圆角效果
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING, 
                    RenderingHints.VALUE_ANTIALIAS_ON
                );
                g2.setColor(c.getBackground());
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 8, 8);
                super.paint(g2, c);
                g2.dispose();
            }
        });
        return button;
    }

    // 创建美化的输入框
    public static JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        field.setBorder(new RoundBorder(8, new Color(204, 204, 204)));
        field.setBackground(Color.WHITE);
        return field;
    }

    // 创建圆角面板
    public static JPanel createRoundedPanel(Color bgColor, int padding) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING, 
                    RenderingHints.VALUE_ANTIALIAS_ON
                );
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createEmptyBorder(
            padding, padding, padding, padding
        ));
        panel.setOpaque(false);
        return panel;
    }
}

// 圆角边框类
class RoundBorder extends AbstractBorder {
    private int radius;
    private Color color;

    public RoundBorder(int radius, Color color) {
        this.radius = radius;
        this.color = color;
    }

    @Override
    public void paintBorder(Component c, Graphics g, 
                          int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING, 
            RenderingHints.VALUE_ANTIALIAS_ON
        );
        g2.setColor(color);
        g2.drawRoundRect(x, y, width-1, height-1, radius, radius);
        g2.dispose();
    }
}
```

### 3.3 登录注册界面

创建 `client/ui/LoginRegisterUI.java`：

```java
package client.ui;

import client.ChatClient;
import common.Message;

import javax.swing.*;
import java.awt.*;

public class LoginRegisterUI {
    private ChatClient chatClient;
    private JFrame loginFrame;
    private JTextField accountField;
    private JPasswordField passwordField;
    private JTextField serverIpField;

    public LoginRegisterUI(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public void show() {
        loginFrame = new JFrame("Java 聊天室 - 登录");
        loginFrame.setSize(500, 400);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setLocationRelativeTo(null);
        loginFrame.getContentPane().setBackground(new Color(240, 248, 255));

        // 标题
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Java 聊天室");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(64, 158, 255));
        titlePanel.add(titleLabel);

        // 登录面板
        JPanel loginPanel = UIComponentFactory.createRoundedPanel(
            Color.WHITE, 30
        );
        loginPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // 服务器IP
        gbc.gridx = 0; gbc.gridy = 0;
        loginPanel.add(new JLabel("服务器IP："), gbc);
        gbc.gridx = 1;
        serverIpField = UIComponentFactory.createStyledTextField();
        serverIpField.setText("127.0.0.1");
        loginPanel.add(serverIpField, gbc);

        // 账号
        gbc.gridx = 0; gbc.gridy = 1;
        loginPanel.add(new JLabel("账号："), gbc);
        gbc.gridx = 1;
        accountField = UIComponentFactory.createStyledTextField();
        loginPanel.add(accountField, gbc);

        // 密码
        gbc.gridx = 0; gbc.gridy = 2;
        loginPanel.add(new JLabel("密码："), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        loginPanel.add(passwordField, gbc);

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        
        JButton loginBtn = UIComponentFactory.createStyledButton("登录");
        loginBtn.addActionListener(e -> doLogin());
        
        JButton registerBtn = UIComponentFactory.createStyledButton("注册");
        registerBtn.addActionListener(e -> doRegister());
        
        buttonPanel.add(loginBtn);
        buttonPanel.add(registerBtn);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        loginPanel.add(buttonPanel, gbc);

        // 组装界面
        loginFrame.setLayout(new BorderLayout());
        loginFrame.add(titlePanel, BorderLayout.NORTH);
        loginFrame.add(loginPanel, BorderLayout.CENTER);
        loginFrame.setVisible(true);
    }

    private void doLogin() {
        String account = accountField.getText();
        String password = new String(passwordField.getPassword());
        String serverIp = serverIpField.getText();

        if (account.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(loginFrame, "请输入账号和密码");
            return;
        }

        chatClient.login(account, password, serverIp);
    }

    private void doRegister() {
        // 注册逻辑...
    }
}
```

### 3.4 客户端主类

创建 `client/ChatClient.java`：

```java
package client;

import client.network.NetworkManager;
import client.ui.LoginRegisterUI;
import client.ui.ChatMainUI;
import common.Message;

import javax.swing.*;
import java.awt.*;

public class ChatClient extends JFrame {
    private NetworkManager networkManager;
    private String username;
    private LoginRegisterUI loginUI;
    private ChatMainUI chatUI;

    public ChatClient() {
        // 显示登录界面
        loginUI = new LoginRegisterUI(this);
        loginUI.show();
    }

    // 登录方法
    public void login(String account, String password, String serverIp) {
        networkManager = new NetworkManager(serverIp);
        
        if (!networkManager.connectToServer()) {
            return;
        }

        // 发送登录消息
        Message loginMsg = new Message(
            Message.Type.LOGIN, 
            account, 
            "", 
            ""
        );
        loginMsg.setPassword(password);
        networkManager.sendMessage(loginMsg);

        // 接收响应
        Message response = networkManager.receiveMessage();
        if (response != null && response.getContent().contains("成功")) {
            this.username = account;
            // 关闭登录界面，显示聊天界面
            SwingUtilities.invokeLater(() -> {
                loginUI.close();
                chatUI = new ChatMainUI(this);
                chatUI.show(username);
                startMessageListener();
            });
        } else {
            JOptionPane.showMessageDialog(null, "登录失败");
        }
    }

    // 启动消息监听线程
    private void startMessageListener() {
        new Thread(() -> {
            while (true) {
                Message message = networkManager.receiveMessage();
                if (message != null) {
                    chatUI.handleMessage(message);
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatClient());
    }
}
```

---

## 第四阶段：功能扩展

### 4.1 文件传输功能

**发送文件：**

```java
// 选择文件并发送
private void sendFile() {
    JFileChooser fileChooser = new JFileChooser();
    int result = fileChooser.showOpenDialog(this);
    
    if (result == JFileChooser.APPROVE_OPTION) {
        File file = fileChooser.getSelectedFile();
        
        try {
            byte[] fileData = Files.readAllBytes(file.toPath());
            String target = (String) targetBox.getSelectedItem();
            
            Message fileMsg = new Message(
                isPrivateChat() ? Message.Type.FILE_PRIVATE : Message.Type.FILE_GROUP,
                username,
                target,
                file.getName(),
                file.length(),
                fileData
            );
            
            networkManager.sendMessage(fileMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

**接收文件：**

```java
private void handleFileMessage(Message message) {
    int option = JOptionPane.showConfirmDialog(this,
        "收到文件：" + message.getFileName() + "\n是否保存？",
        "文件传输",
        JOptionPane.YES_NO_OPTION
    );
    
    if (option == JOptionPane.YES_OPTION) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(message.getFileName()));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Files.write(fileChooser.getSelectedFile().toPath(), 
                          message.getFileData());
                JOptionPane.showMessageDialog(this, "文件保存成功");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
```

### 4.2 窗口抖动功能

```java
// 发送抖动消息
private void sendShake() {
    String target = (String) targetBox.getSelectedItem();
    Message shakeMsg = new Message(Message.Type.SHAKE, username, target, "");
    networkManager.sendMessage(shakeMsg);
}

// 执行窗口抖动
private void performShake() {
    if (isShaking) return;
    isShaking = true;
    
    final Point original = getLocation();
    final Timer timer = new Timer();
    
    timer.schedule(new TimerTask() {
        int count = 0;
        @Override
        public void run() {
            if (count >= 10) {
                setLocation(original);
                isShaking = false;
                timer.cancel();
                return;
            }
            int offsetX = (int) (Math.random() * 10 - 5);
            int offsetY = (int) (Math.random() * 10 - 5);
            setLocation(original.x + offsetX, original.y + offsetY);
            count++;
        }
    }, 0, 50);
}
```

### 4.3 群组管理功能

**创建群组：**

```java
private void createGroup() {
    String groupName = JOptionPane.showInputDialog(this, "请输入群名称：");
    if (groupName != null && !groupName.isEmpty()) {
        Message msg = new Message(Message.Type.CREATE_GROUP, username, "", groupName);
        networkManager.sendMessage(msg);
    }
}
```

**服务端处理创建群组：**

```java
private void handleCreateGroup(Message message) throws IOException {
    String groupId = UUID.randomUUID().toString();
    Group newGroup = new Group(groupId, message.getContent());
    newGroup.addMember(message.getSender());
    groupMap.put(groupId, newGroup);
    
    // 广播群列表更新
    broadcastGroupList();
}
```

---

## 第五阶段：运行和测试

### 5.1 编译运行

1. **编译项目：**
```bash
# 创建输出目录
mkdir -p out/production/NewChatRoom

# 编译所有Java文件
javac -d out/production/NewChatRoom src/common/*.java src/server/*.java src/client/**/*.java
```

2. **启动服务端：**
```bash
cd out/production/NewChatRoom
java server.ChatServer
```

3. **启动客户端：**
```bash
cd out/production/NewChatRoom
java client.ChatClient
```

### 5.2 使用IntelliJ IDEA运行

1. 打开项目文件夹
2. 右键 `ChatServer.java` → Run
3. 右键 `ChatClient.java` → Run（可运行多个实例）

---

## 关键技术点总结

### 1. Socket通信流程

```
服务端：ServerSocket → accept() → Socket → 输入/输出流
客户端：Socket → connect() → 输入/输出流
```

### 2. 多线程处理

- 服务端为每个客户端创建独立线程
- 客户端需要独立线程监听服务器消息
- 使用 `SwingUtilities.invokeLater()` 更新UI

### 3. 对象序列化

- 所有消息类必须实现 `Serializable`
- 使用 `ObjectInputStream/ObjectOutputStream` 传输对象

### 4. 线程安全

- 使用 `ConcurrentHashMap` 存储用户连接
- 避免多线程同时修改共享数据

### 5. Swing UI更新

- 所有UI更新必须在事件调度线程执行
- 使用 `SwingUtilities.invokeLater()` 或 `invokeAndWait()`

---

## 扩展建议

1. **数据库集成** - 使用MySQL存储用户信息和聊天记录
2. **文件断点续传** - 大文件分片传输
3. **图片和表情** - 支持富文本消息
4. **语音/视频通话** - 集成WebRTC
5. **移动端适配** - 开发Android客户端
6. **加密传输** - 使用SSL/TLS加密通信

---

## 常见问题解决

### Q1: 连接被拒绝
- 检查服务端是否启动
- 检查防火墙设置
- 确认IP地址和端口正确

### Q2: 对象传输失败
- 确认类实现 `Serializable`
- 检查类路径一致
- 确认流未关闭

### Q3: UI卡顿
- 耗时操作放入后台线程
- 使用 `SwingWorker` 处理异步任务

---

希望这个教程能帮助你理解并开发出自己的Java聊天室应用！
