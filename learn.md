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
- 截图功能

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
│   ├── common/                          # 公共类（客户端和服务端共享）
│   │   ├── Message.java                 # 消息实体类
│   │   └── Group.java                   # 群组实体类
│   ├── server/                           # 服务端代码
│   │   ├── ChatServer.java               # 服务端主类
│   │   ├── handlers/
│   │   │   ├── ClientHandler.java        # 客户端连接处理
│   │   │   └── MessageHandler.java       # 消息处理
│   │   ├── managers/
│   │   │   ├── UserManager.java         # 用户管理
│   │   │   ├── OnlineUserManager.java    # 在线用户管理
│   │   │   └── GroupManager.java        # 群组管理
│   │   └── broadcast/
│   │       └── BroadcastService.java     # 广播服务
│   └── client/                           # 客户端代码
│       ├── ChatClient.java               # 客户端主类
│       ├── MessageListener.java           # 消息监听线程
│       ├── ScreenshotManager.java        # 截图管理
│       ├── UIComponentFactory.java        # UI组件工厂
│       ├── network/
│       │   └── NetworkManager.java     # 网络管理
│       ├── handler/
│       │   └── MessageHandler.java       # 消息处理器
│       ├── managers/
│       │   ├── ChatManager.java         # 聊天管理
│       │   ├── FileManager.java         # 文件管理
│       │   ├── DataManager.java         # 数据管理
│       │   ├── WindowManager.java       # 窗口管理
│       │   └── AuthenticationManager.java # 认证管理
│       └── ui/
│           ├── ChatMainUI.java          # 聊天主界面
│           ├── LoginRegisterUI.java      # 登录注册界面
│           └── UIComponentFactory.java    # UI组件工厂
```

---

## 第一阶段：基础架构搭建

### 1.1 创建公共消息类

创建 `common/Message.java`，这是客户端和服务端通信的基础：

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

    // Getter 和 Setter 方法
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    
    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public List<String> getOnlineUsers() { return onlineUsers; }
    public void setOnlineUsers(List<String> onlineUsers) { this.onlineUsers = onlineUsers; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    
    public byte[] getFileData() { return fileData; }
    public void setFileData(byte[] fileData) { this.fileData = fileData; }
    
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    
    public List<Group> getGroupList() { return groupList; }
    public void setGroupList(List<Group> groupList) { this.groupList = groupList; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
```

**关键点说明：**
- 实现 `Serializable` 接口，使对象可以通过网络传输
- 使用枚举定义消息类型，便于管理和扩展
- 包含多种构造函数，适应不同场景
- 提供完整的getter和setter方法

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

    // Getter 和 Setter
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    
    public List<String> getMembers() { return members; }
    public void setMembers(List<String> members) { this.members = members; }
}
```

---

## 第二阶段：服务端开发

### 2.1 用户管理器

创建 `server/managers/UserManager.java`：

```java
package server.managers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户管理类
 * 负责用户注册、登录验证、密码找回等功能
 */
public class UserManager {
    private final Map<String, String> userAuthMap; // 用户名 -> 密码

    public UserManager() {
        this.userAuthMap = new ConcurrentHashMap<>();
    }

    /**
     * 用户注册
     */
    public boolean register(String username, String password) {
        if (userAuthMap.containsKey(username)) {
            return false; // 用户已存在
        }
        userAuthMap.put(username, password);
        return true;
    }

    /**
     * 验证用户登录
     */
    public boolean authenticate(String username, String password) {
        String storedPassword = userAuthMap.get(username);
        return storedPassword != null && storedPassword.equals(password);
    }

    /**
     * 验证账号是否存在
     */
    public boolean userExists(String username) {
        return userAuthMap.containsKey(username);
    }

    /**
     * 更新密码
     */
    public boolean updatePassword(String username, String newPassword) {
        if (!userAuthMap.containsKey(username)) {
            return false;
        }
        userAuthMap.put(username, newPassword);
        return true;
    }
}
```

### 2.2 在线用户管理器

创建 `server/managers/OnlineUserManager.java`：

```java
package server.managers;

import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 在线用户管理类
 * 负责管理在线用户的连接和状态
 */
public class OnlineUserManager {
    private final Map<String, ObjectOutputStream> userMap; // 用户名 -> 输出流

    public OnlineUserManager() {
        this.userMap = new ConcurrentHashMap<>();
    }

    /**
     * 添加在线用户
     */
    public void addUser(String username, ObjectOutputStream oos) {
        userMap.put(username, oos);
    }

    /**
     * 移除在线用户
     */
    public void removeUser(String username) {
        userMap.remove(username);
    }

    /**
     * 获取用户输出流
     */
    public ObjectOutputStream getUserStream(String username) {
        return userMap.get(username);
    }

    /**
     * 获取在线用户列表
     */
    public List<String> getOnlineUsers() {
        return new ArrayList<>(userMap.keySet());
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(String username) {
        return userMap.containsKey(username);
    }
}
```

### 2.3 群组管理器

创建 `server/managers/GroupManager.java`：

```java
package server.managers;

import common.Group;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 群组管理类
 * 负责群组的创建、查找、成员管理等功能
 */
public class GroupManager {
    private final Map<String, Group> groupMap;

    public GroupManager() {
        this.groupMap = new ConcurrentHashMap<>();
    }

    /**
     * 创建群组
     */
    public Group createGroup(String groupName) {
        String groupId = UUID.randomUUID().toString();
        Group group = new Group(groupId, groupName);
        groupMap.put(groupId, group);
        return group;
    }

    /**
     * 搜索群组
     */
    public List<Group> searchGroups(String keyword) {
        List<Group> result = new ArrayList<>();
        for (Group group : groupMap.values()) {
            if (group.getGroupName().contains(keyword)) {
                result.add(group);
            }
        }
        return result;
    }

    /**
     * 获取群组
     */
    public Group getGroup(String groupId) {
        return groupMap.get(groupId);
    }

    /**
     * 加入群组
     */
    public boolean joinGroup(String groupId, String username) {
        Group group = groupMap.get(groupId);
        if (group != null) {
            group.addMember(username);
            return true;
        }
        return false;
    }

    /**
     * 获取所有群组
     */
    public List<Group> getAllGroups() {
        return new ArrayList<>(groupMap.values());
    }
}
```

### 2.4 广播服务

创建 `server/broadcast/BroadcastService.java`：

```java
package server.broadcast;

import common.Group;
import common.Message;
import server.managers.GroupManager;
import server.managers.OnlineUserManager;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * 广播服务类
 * 负责向客户端广播消息
 */
public class BroadcastService {
    private final OnlineUserManager onlineUserManager;
    private final GroupManager groupManager;

    public BroadcastService(OnlineUserManager onlineUserManager, GroupManager groupManager) {
        this.onlineUserManager = onlineUserManager;
        this.groupManager = groupManager;
    }

    /**
     * 广播消息给所有在线用户
     */
    public void broadcastToAll(Message message) throws IOException {
        for (String username : onlineUserManager.getOnlineUsers()) {
            ObjectOutputStream oos = onlineUserManager.getUserStream(username);
            if (oos != null) {
                oos.writeObject(message);
                oos.flush();
            }
        }
    }

    /**
     * 发送消息给指定用户
     */
    public void sendToUser(String username, Message message) throws IOException {
        ObjectOutputStream oos = onlineUserManager.getUserStream(username);
        if (oos != null) {
            oos.writeObject(message);
            oos.flush();
        }
    }

    /**
     * 广播消息给群组成员
     */
    public void broadcastToGroup(String groupId, Message message) throws IOException {
        Group group = groupManager.getGroup(groupId);
        if (group != null) {
            for (String member : group.getMembers()) {
                sendToUser(member, message);
            }
        }
    }

    /**
     * 广播在线用户列表
     */
    public void broadcastOnlineUsers() throws IOException {
        List<String> onlineUsers = onlineUserManager.getOnlineUsers();
        Message usersMsg = new Message(Message.Type.ONLINE_USERS, "服务器");
        usersMsg.setOnlineUsers(onlineUsers);
        broadcastToAll(usersMsg);
    }

    /**
     * 广播群组列表
     */
    public void broadcastGroupList() throws IOException {
        List<Group> groupList = groupManager.getAllGroups();
        Message groupsMsg = new Message(Message.Type.GROUP_LIST, "服务器");
        groupsMsg.setGroupList(groupList);
        broadcastToAll(groupsMsg);
    }
}
```

### 2.5 消息处理器

创建 `server/handlers/MessageHandler.java`：

```java
package server.handlers;

import common.Group;
import common.Message;
import server.broadcast.BroadcastService;
import server.managers.GroupManager;
import server.managers.OnlineUserManager;
import server.managers.UserManager;

import java.io.IOException;
import java.util.List;

/**
 * 消息处理器类
 * 负责处理从客户端接收到的各种消息类型
 */
public class MessageHandler {
    private final UserManager userManager;
    private final OnlineUserManager onlineUserManager;
    private final GroupManager groupManager;
    private final BroadcastService broadcastService;
    private String currentUsername;

    public MessageHandler(UserManager userManager, OnlineUserManager onlineUserManager, 
                       GroupManager groupManager, BroadcastService broadcastService) {
        this.userManager = userManager;
        this.onlineUserManager = onlineUserManager;
        this.groupManager = groupManager;
        this.broadcastService = broadcastService;
    }

    public void setCurrentUsername(String username) {
        this.currentUsername = username;
    }

    /**
     * 处理消息
     */
    public void handleMessage(Message message) throws IOException {
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
            case FILE_PRIVATE:
                handlePrivateFile(message);
                break;
            case FILE_GROUP:
                handleGroupFile(message);
                break;
            case SHAKE:
                handleShake(message);
                break;
            case CREATE_GROUP:
                handleCreateGroup(message);
                break;
            case SEARCH_GROUP:
                handleSearchGroup(message);
                break;
            case JOIN_GROUP:
                handleJoinGroup(message);
                break;
            case GET_ONLINE_USERS:
                handleGetOnlineUsers();
                break;
            case REGISTER:
                handleRegister(message);
                break;
            case FIND_PASSWORD:
                handleFindPassword(message);
                break;
            case RESET_PASSWORD:
                handleResetPassword(message);
                break;
            default:
                break;
        }
    }

    private void handleLogin(Message message) throws IOException {
        String username = message.getSender();
        String password = message.getPassword();

        if (userManager.authenticate(username, password)) {
            onlineUserManager.addUser(username, 
                onlineUserManager.getUserStream(username));
            
            Message response = new Message(Message.Type.LOGIN, "服务器", 
                username, "登录成功");
            broadcastService.sendToUser(username, response);
            broadcastService.broadcastOnlineUsers();
        } else {
            Message response = new Message(Message.Type.LOGIN, "服务器", 
                username, "登录失败：账号或密码错误");
            broadcastService.sendToUser(username, response);
        }
    }

    private void handlePrivateChat(Message message) throws IOException {
        broadcastService.sendToUser(message.getReceiver(), message);
    }

    private void handleGroupChat(Message message) throws IOException {
        broadcastService.broadcastToGroup(message.getGroupId(), message);
    }

    private void handlePrivateFile(Message message) throws IOException {
        broadcastService.sendToUser(message.getReceiver(), message);
    }

    private void handleGroupFile(Message message) throws IOException {
        broadcastService.broadcastToGroup(message.getGroupId(), message);
    }

    private void handleShake(Message message) throws IOException {
        broadcastService.sendToUser(message.getReceiver(), message);
    }

    private void handleCreateGroup(Message message) throws IOException {
        Group group = groupManager.createGroup(message.getContent());
        broadcastService.broadcastGroupList();
        
        Message response = new Message(Message.Type.CREATE_GROUP, "服务器", 
            currentUsername, "群组创建成功：" + group.getGroupName());
        broadcastService.sendToUser(currentUsername, response);
    }

    private void handleSearchGroup(Message message) throws IOException {
        List<Group> groups = groupManager.searchGroups(message.getContent());
        Message response = new Message(Message.Type.SEARCH_GROUP, "服务器", 
            currentUsername, "");
        response.setGroupList(groups);
        broadcastService.sendToUser(currentUsername, response);
    }

    private void handleJoinGroup(Message message) throws IOException {
        boolean success = groupManager.joinGroup(message.getGroupId(), 
            message.getSender());
        
        if (success) {
            broadcastService.broadcastGroupList();
            Message response = new Message(Message.Type.JOIN_GROUP, "服务器", 
                currentUsername, "加入群组成功");
            broadcastService.sendToUser(currentUsername, response);
        } else {
            Message response = new Message(Message.Type.JOIN_GROUP, "服务器", 
                currentUsername, "加入群组失败：群组不存在");
            broadcastService.sendToUser(currentUsername, response);
        }
    }

    private void handleGetOnlineUsers() throws IOException {
        broadcastService.broadcastOnlineUsers();
    }

    private void handleRegister(Message message) throws IOException {
        String username = message.getSender();
        String password = message.getPassword();

        if (userManager.register(username, password)) {
            Message response = new Message(Message.Type.REGISTER_RESPONSE, 
                "服务器", username, "注册成功");
            broadcastService.sendToUser(username, response);
        } else {
            Message response = new Message(Message.Type.REGISTER_RESPONSE, 
                "服务器", username, "注册失败：用户已存在");
            broadcastService.sendToUser(username, response);
        }
    }

    private void handleFindPassword(Message message) throws IOException {
        String username = message.getSender();
        if (userManager.userExists(username)) {
            Message response = new Message(Message.Type.FIND_PASSWORD_RESPONSE, 
                "服务器", username, "账号验证通过，请重置密码");
            broadcastService.sendToUser(username, response);
        } else {
            Message response = new Message(Message.Type.FIND_PASSWORD_RESPONSE, 
                "服务器", username, "账号不存在");
            broadcastService.sendToUser(username, response);
        }
    }

    private void handleResetPassword(Message message) throws IOException {
        String username = message.getSender();
        String newPassword = message.getPassword();

        if (userManager.updatePassword(username, newPassword)) {
            Message response = new Message(Message.Type.RESET_PASSWORD_RESPONSE, 
                "服务器", username, "密码重置成功");
            broadcastService.sendToUser(username, response);
        } else {
            Message response = new Message(Message.Type.RESET_PASSWORD_RESPONSE, 
                "服务器", username, "密码重置失败：账号不存在");
            broadcastService.sendToUser(username, response);
        }
    }
}
```

### 2.6 客户端处理器

创建 `server/handlers/ClientHandler.java`：

```java
package server.handlers;

import common.Message;
import server.broadcast.BroadcastService;
import server.handlers.MessageHandler;
import server.managers.GroupManager;
import server.managers.OnlineUserManager;
import server.managers.UserManager;

import java.io.*;
import java.net.Socket;

/**
 * 客户端连接处理线程
 */
public class ClientHandler extends Thread {
    private final Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private final MessageHandler messageHandler;

    public ClientHandler(Socket socket, UserManager userManager, 
                      OnlineUserManager onlineUserManager, 
                      GroupManager groupManager, 
                      BroadcastService broadcastService) {
        this.socket = socket;
        this.messageHandler = new MessageHandler(userManager, 
            onlineUserManager, groupManager, broadcastService);
    }

    @Override
    public void run() {
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            Message message;
            while ((message = (Message) ois.readObject()) != null) {
                messageHandler.setCurrentUsername(message.getSender());
                messageHandler.handleMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    private void cleanup() {
        try {
            if (ois != null) ois.close();
            if (oos != null) oos.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ObjectOutputStream getOutputStream() {
        return oos;
    }
}
```

### 2.7 服务端主类

创建 `server/ChatServer.java`：

```java
package server;

import server.broadcast.BroadcastService;
import server.handlers.ClientHandler;
import server.managers.GroupManager;
import server.managers.OnlineUserManager;
import server.managers.UserManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 聊天室服务端
 */
public class ChatServer {
    private static final int PORT = 8888;
    
    private final UserManager userManager;
    private final OnlineUserManager onlineUserManager;
    private final GroupManager groupManager;
    private final BroadcastService broadcastService;

    public ChatServer() {
        this.userManager = new UserManager();
        this.onlineUserManager = new OnlineUserManager();
        this.groupManager = new GroupManager();
        this.broadcastService = new BroadcastService(onlineUserManager, groupManager);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("聊天室服务端已启动，端口：" + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("新客户端连接：" + clientSocket);
                
                ClientHandler clientHandler = new ClientHandler(
                    clientSocket, 
                    userManager, 
                    onlineUserManager, 
                    groupManager, 
                    broadcastService
                );
                
                onlineUserManager.addUser("temp", clientHandler.getOutputStream());
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ChatServer().start();
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

/**
 * 网络管理类
 * 负责与服务器的网络通信
 */
public class NetworkManager {
    private String serverIp;
    private static final int SERVER_PORT = 8888;
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    public NetworkManager(String serverIp) {
        this.serverIp = serverIp;
    }

    /**
     * 连接到服务器
     */
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

    /**
     * 发送消息
     */
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

    /**
     * 接收消息
     */
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

    /**
     * 重置Socket连接
     */
    public void resetSocket() {
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

创建 `client/UIComponentFactory.java`：

```java
package client.ui;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;

/**
 * UI组件工厂类
 * 提供统一的UI组件创建方法，保持界面风格一致
 */
public class UIComponentFactory {

    /**
     * 创建美化样式的按钮
     */
    public JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        button.setBackground(new Color(64, 158, 255));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
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

    /**
     * 创建美化的输入框
     */
    public JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        field.setBorder(new RoundBorder(8, new Color(204, 204, 204)));
        field.setBackground(Color.WHITE);
        return field;
    }

    /**
     * 创建圆角面板
     */
    public JPanel createRoundedPanel(Color bgColor, int padding) {
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

/**
 * 圆角边框类
 */
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

### 3.3 消息监听器

创建 `client/MessageListener.java`：

```java
package client;

import client.handler.MessageHandler;
import client.network.NetworkManager;
import common.Message;

import javax.swing.*;

/**
 * 消息监听线程
 * 负责持续监听服务器发送的消息
 */
public class MessageListener extends Thread {
    private final NetworkManager networkManager;
    private final MessageHandler messageHandler;
    private final JTextArea chatArea;

    public MessageListener(NetworkManager networkManager, MessageHandler messageHandler, 
                       JTextArea chatArea) {
        this.networkManager = networkManager;
        this.messageHandler = messageHandler;
        this.chatArea = chatArea;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Message message = networkManager.receiveMessage();
                if (message != null) {
                    messageHandler.handleMessage(message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### 3.4 消息处理器

创建 `client/handler/MessageHandler.java`：

```java
package client.handler;

import client.ChatClient;
import client.ui.ChatMainUI;
import common.Message;

import javax.swing.*;
import java.awt.Point;

/**
 * 消息处理器类
 * 负责处理从服务器接收到的各种消息类型
 */
public class MessageHandler {
    private final ChatClient chatClient;
    private final ChatMainUI chatMainUI;

    public MessageHandler(ChatClient chatClient, ChatMainUI chatMainUI) {
        this.chatClient = chatClient;
        this.chatMainUI = chatMainUI;
    }

    /**
     * 处理消息
     */
    public void handleMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            switch (message.getType()) {
                case PRIVATE_CHAT:
                    handlePrivateChat(message);
                    break;
                case GROUP_CHAT:
                    handleGroupChat(message);
                    break;
                case FILE_PRIVATE:
                case FILE_GROUP:
                    handleFile(message);
                    break;
                case SHAKE:
                    handleShake(message);
                    break;
                case ONLINE_USERS:
                    handleOnlineUsers(message);
                    break;
                case GROUP_LIST:
                    handleGroupList(message);
                    break;
                case ONLINE_NOTIFY:
                    handleOnlineNotify(message);
                    break;
                case OFFLINE_NOTIFY:
                    handleOfflineNotify(message);
                    break;
                default:
                    break;
            }
        });
    }

    private void handlePrivateChat(Message message) {
        chatMainUI.getChatArea().append(
            "【私聊-" + message.getSender() + "】" + 
            message.getSender() + "：" + message.getContent() + "\n"
        );
    }

    private void handleGroupChat(Message message) {
        chatMainUI.getChatArea().append(
            "【群聊-" + message.getGroupId() + "】" + 
            message.getSender() + "：" + message.getContent() + "\n"
        );
    }

    private void handleFile(Message message) {
        String fileName = message.getFileName();
        long fileSize = message.getFileSize();
        chatMainUI.getChatArea().append(
            "【系统消息】收到文件：" + fileName + 
            "（大小：" + formatFileSize(fileSize) + "）\n"
        );
        
        int confirm = JOptionPane.showConfirmDialog(
            chatMainUI,
            "是否保存文件：" + fileName + "？",
            "接收文件",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            chatClient.saveBytesToFile(message.getFileData(), fileName);
        }
    }

    private void handleShake(Message message) {
        chatMainUI.getChatArea().append(
            "【系统消息】收到来自" + message.getSender() + "的窗口抖动！\n"
        );
        chatClient.shakeWindow();
    }

    private void handleOnlineUsers(Message message) {
        chatClient.updateUserList(message.getOnlineUsers());
    }

    private void handleGroupList(Message message) {
        chatClient.updateGroupList(message.getGroupList());
    }

    private void handleOnlineNotify(Message message) {
        chatMainUI.getChatArea().append(
            "【系统消息】" + message.getSender() + " 上线了\n"
        );
    }

    private void handleOfflineNotify(Message message) {
        chatMainUI.getChatArea().append(
            "【系统消息】" + message.getSender() + " 下线了\n"
        );
    }

    private void shakeWindow() {
        chatClient.shakeWindow();
    }

    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        }
    }
}
```

### 3.5 Manager类

#### 聊天管理器

创建 `client/managers/ChatManager.java`：

```java
package client.managers;

import client.ChatClient;
import common.Message;

import javax.swing.*;

/**
 * 聊天管理类
 * 负责处理聊天消息的发送逻辑
 */
public class ChatManager {
    private final ChatClient chatClient;

    public ChatManager(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 发送聊天消息
     */
    public void sendMessage() {
        String content = chatClient.getInputField().getText().trim();
        if (content.isEmpty()) {
            return;
        }

        try {
            Message.Type type;
            String targetName = (String) chatClient.getTargetBox().getSelectedItem();
            String target = null;

            if (chatClient.getChatTypeBox().getSelectedItem().equals("群聊")) {
                type = Message.Type.GROUP_CHAT;
                target = (String) chatClient.getTargetBox().getClientProperty("groupId");
                if (target == null || target.isEmpty()) {
                    target = chatClient.getGroupNameToIdMap().get(targetName);
                }
                if (target == null || target.isEmpty()) {
                    JOptionPane.showMessageDialog(chatClient, "未找到该群聊的ID，无法发送消息！");
                    return;
                }
            } else {
                type = Message.Type.PRIVATE_CHAT;
                target = targetName;
            }

            Message chatMsg = new Message(type, chatClient.getUsername(), target, content);
            chatClient.getNetworkManager().sendMessage(chatMsg);
            chatClient.getInputField().setText("");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(chatClient, "发送消息失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 发送窗口抖动
     */
    public void sendShake() {
        String targetName = (String) chatClient.getTargetBox().getSelectedItem();
        String target = null;

        if (chatClient.getChatTypeBox().getSelectedItem().equals("群聊")) {
            target = (String) chatClient.getTargetBox().getClientProperty("groupId");
            if (target == null || target.isEmpty()) {
                target = chatClient.getGroupNameToIdMap().get(targetName);
            }
        } else {
            target = targetName;
        }

        if (target == null || target.isEmpty()) {
            JOptionPane.showMessageDialog(chatClient, "请先选择聊天对象！");
            return;
        }

        try {
            Message shakeMsg = new Message(Message.Type.SHAKE, chatClient.getUsername(), target, "");
            chatClient.getNetworkManager().sendMessage(shakeMsg);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(chatClient, "发送抖动失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 发送创建群聊请求
     */
    public void sendCreateGroupRequest(String groupName) {
        try {
            Message createMsg = new Message(Message.Type.CREATE_GROUP, chatClient.getUsername(), "", groupName);
            chatClient.getNetworkManager().sendMessage(createMsg);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(chatClient, "发送创建群聊请求失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 发送查找群聊请求
     */
    public void sendSearchGroupRequest(String keyword) {
        try {
            Message searchMsg = new Message(Message.Type.SEARCH_GROUP, chatClient.getUsername(), "", keyword);
            chatClient.getNetworkManager().sendMessage(searchMsg);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(chatClient, "发送查找群聊请求失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 发送加入群聊请求
     */
    public void sendJoinGroupRequest(String groupId) {
        try {
            Message joinMsg = new Message(Message.Type.JOIN_GROUP, chatClient.getUsername(), groupId, "");
            chatClient.getNetworkManager().sendMessage(joinMsg);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(chatClient, "发送加入群聊请求失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

#### 文件管理器

创建 `client/managers/FileManager.java`：

```java
package client.managers;

import client.ChatClient;
import common.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 文件管理类
 * 负责处理文件选择、发送和截图功能
 */
public class FileManager {
    private final ChatClient chatClient;
    private File selectedFile;

    public FileManager(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    public void setSelectedFile(File file) {
        this.selectedFile = file;
    }

    /**
     * 选择文件
     */
    public void selectFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择要发送的文件");
        int result = fileChooser.showOpenDialog(chatClient);

        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            long fileSize = selectedFile.length();
            String fileSizeStr = formatFileSize(fileSize);

            JOptionPane.showMessageDialog(chatClient,
                    "已选择文件：" + selectedFile.getName() + "\n文件大小：" + fileSizeStr);
        }
    }

    /**
     * 发送文件
     */
    public void sendFile() {
        if (selectedFile == null || !selectedFile.exists()) {
            JOptionPane.showMessageDialog(chatClient, "请先选择文件！");
            return;
        }

        String targetName = (String) chatClient.getTargetBox().getSelectedItem();
        String target = null;
        Message.Type type;

        if (chatClient.getChatTypeBox().getSelectedItem().equals("群聊")) {
            type = Message.Type.FILE_GROUP;
            target = (String) chatClient.getTargetBox().getClientProperty("groupId");
            if (target == null || target.isEmpty()) {
                target = chatClient.getGroupNameToIdMap().get(targetName);
            }
        } else {
            type = Message.Type.FILE_PRIVATE;
            target = targetName;
        }

        if (target == null || target.isEmpty()) {
            JOptionPane.showMessageDialog(chatClient, "请先选择聊天对象！");
            return;
        }

        try {
            FileInputStream fis = new FileInputStream(selectedFile);
            byte[] fileData = new byte[(int) selectedFile.length()];
            fis.read(fileData);
            fis.close();

            Message fileMsg = new Message(type, chatClient.getUsername(), target,
                    selectedFile.getName(), selectedFile.length(), fileData);
            chatClient.getNetworkManager().sendMessage(fileMsg);

            chatClient.getChatArea().append("【系统消息】文件[" + selectedFile.getName() + "]发送成功\n");
            selectedFile = null;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(chatClient, "发送文件失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 截图并发送
     */
    public void captureAndSendScreenshot() {
        try {
            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenshot = robot.createScreenCapture(screenRect);

            File screenshotFile = new File("screenshot_" + System.currentTimeMillis() + ".png");
            javax.imageio.ImageIO.write(screenshot, "png", screenshotFile);

            int confirm = JOptionPane.showConfirmDialog(
                    chatClient,
                    "截图已生成，是否发送给当前选中的" + 
                            (chatClient.getChatTypeBox().getSelectedItem().equals("群聊") ? "群聊" : "好友") + "？",
                    "确认发送截图",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm != JOptionPane.YES_OPTION) {
                screenshotFile.delete();
                return;
            }

            selectedFile = screenshotFile;
            sendFile();

            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    if (screenshotFile.exists()) {
                        screenshotFile.delete();
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(chatClient, "截图发送失败：" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }
}
```

#### 数据管理器

创建 `client/managers/DataManager.java`：

```java
package client.managers;

import client.ChatClient;
import common.Group;

import javax.swing.*;
import java.util.List;

/**
 * 数据管理类
 * 负责更新用户列表、群列表等数据
 */
public class DataManager {
    private final ChatClient chatClient;

    public DataManager(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 更新在线用户列表UI
     */
    public void updateUserList(List<String> onlineUsers) {
        SwingUtilities.invokeLater(() -> {
            chatClient.getUserListModel().clear();
            for (String user : onlineUsers) {
                chatClient.getUserListModel().addElement(user);
            }
        });
    }

    /**
     * 更新群列表
     */
    public void updateGroupList(List<Group> groupList) {
        SwingUtilities.invokeLater(() -> {
            chatClient.getGroupListModel().clear();
            chatClient.getGroupIdToNameMap().clear();
            chatClient.getGroupNameToIdMap().clear();

            for (Group group : groupList) {
                chatClient.getGroupListModel().addElement(group.getGroupName());
                chatClient.getGroupIdToNameMap().put(group.getGroupId(), group.getGroupName());
                chatClient.getGroupNameToIdMap().put(group.getGroupName(), group.getGroupId());
            }

            if (chatClient.getChatTypeBox().getSelectedItem().equals("群聊")) {
                chatClient.getTargetBox().removeAllItems();
                for (int i = 0; i < chatClient.getGroupListModel().size(); i++) {
                    chatClient.getTargetBox().addItem(chatClient.getGroupListModel().getElementAt(i));
                }
            }
        });
    }

    /**
     * 保存字节数组为文件
     */
    public void saveBytesToFile(byte[] data, String fileName) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File(fileName));
        int result = fileChooser.showSaveDialog(chatClient);

        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File saveFile = fileChooser.getSelectedFile();
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(saveFile)) {
                fos.write(data);
                chatClient.getChatArea().append("【系统消息】已接收文件：" + fileName + "，保存至：" + saveFile.getAbsolutePath() + "\n");
            } catch (java.io.IOException e) {
                chatClient.getChatArea().append("【系统消息】文件保存失败：" + e.getMessage() + "\n");
                e.printStackTrace();
            }
        } else {
            chatClient.getChatArea().append("【系统消息】取消保存文件：" + fileName + "\n");
        }
    }
}
```

#### 窗口管理器

创建 `client/managers/WindowManager.java`：

```java
package client.managers;

import client.ChatClient;

import javax.swing.*;
import java.awt.*;

/**
 * 窗口管理类
 * 负责窗口抖动等窗口相关功能
 */
public class WindowManager {
    private final ChatClient chatClient;
    private Point originalLocation;
    private boolean isShaking = false;

    public WindowManager(ChatClient chatClient) {
        this.chatClient = chatClient;
        this.originalLocation = chatClient.getLocation();
    }

    /**
     * 执行窗口抖动
     */
    public void shakeWindow() {
        if (isShaking) {
            return;
        }

        isShaking = true;
        originalLocation = chatClient.getLocation();

        new Timer(10, e -> {
            int offsetX = (int) (Math.random() * 20 - 10);
            int offsetY = (int) (Math.random() * 20 - 10);
            chatClient.setLocation(originalLocation.x + offsetX, originalLocation.y + offsetY);
        }).start();

        new Timer(500, e -> {
            chatClient.setLocation(originalLocation);
            isShaking = false;
        }).start();
    }

    /**
     * 检查是否正在抖动
     */
    public boolean isShaking() {
        return isShaking;
    }

    /**
     * 设置抖动状态
     */
    public void setShaking(boolean shaking) {
        isShaking = shaking;
    }
}
```

#### 认证管理器

创建 `client/managers/AuthenticationManager.java`：

```java
package client.managers;

import client.ChatClient;
import common.Message;

import javax.swing.*;

/**
 * 认证管理类
 * 负责用户注册、登录、密码找回等功能
 */
public class AuthenticationManager {
    private final ChatClient chatClient;

    public AuthenticationManager(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 用户注册
     */
    public void register(String account, String password) {
        try {
            Message registerMsg = new Message(Message.Type.REGISTER, account, "");
            registerMsg.setPassword(password);
            chatClient.getNetworkManager().sendMessage(registerMsg);

            Message response = chatClient.getNetworkManager().receiveMessage();
            JOptionPane.showMessageDialog(chatClient.getLoginRegisterFrame(), response.getContent());

            if (response.getContent().contains("成功")) {
                chatClient.getLoginRegisterFrame().dispose();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(chatClient.getLoginRegisterFrame(), "注册失败：" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * 用户登录
     */
    public void login(String account, String password, String serverIp) {
        try {
            chatClient.setServerIp(serverIp);
            
            if (!chatClient.getNetworkManager().connectToServer()) {
                return;
            }

            Message loginMsg = new Message(Message.Type.LOGIN, account, "");
            loginMsg.setPassword(password);
            chatClient.getNetworkManager().sendMessage(loginMsg);

            Message response = chatClient.getNetworkManager().receiveMessage();
            JOptionPane.showMessageDialog(chatClient.getLoginRegisterFrame(), response.getContent());

            if (response.getContent().contains("成功")) {
                chatClient.setUsername(account);
                chatClient.getLoginRegisterFrame().dispose();
                chatClient.initChatUI();
                chatClient.startMessageListener();

                Message getUsersMsg = new Message(Message.Type.GET_ONLINE_USERS, account, "", "");
                chatClient.getNetworkManager().sendMessage(getUsersMsg);
            } else {
                chatClient.getLoginPwdField().setText("");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(chatClient.getLoginRegisterFrame(), "登录失败：" + ex.getMessage());
            ex.printStackTrace();
            chatClient.resetSocket();
            System.exit(1);
        }
    }

    /**
     * 找回密码
     */
    public void findPassword(String account) {
        try {
            Message findMsg = new Message(Message.Type.FIND_PASSWORD, account, "");
            chatClient.getNetworkManager().sendMessage(findMsg);

            Message response = chatClient.getNetworkManager().receiveMessage();
            JOptionPane.showMessageDialog(chatClient.getLoginRegisterFrame(), response.getContent());

            if (response.getContent().contains("验证通过")) {
                resetPassword(account);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(chatClient.getLoginRegisterFrame(), "验证账号失败：" + ex.getMessage());
            chatClient.resetSocket();
            ex.printStackTrace();
        }
    }

    /**
     * 重置密码
     */
    public void resetPassword(String account) {
        JPasswordField newPwdField = new JPasswordField();
        int result = JOptionPane.showConfirmDialog(
                chatClient.getLoginRegisterFrame(),
                new Object[]{"请输入新密码：", newPwdField},
                "重置密码",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            String newPassword = new String(newPwdField.getPassword()).trim();
            if (newPassword.isEmpty()) {
                JOptionPane.showMessageDialog(chatClient.getLoginRegisterFrame(), "新密码不能为空！");
                return;
            }

            try {
                Message resetMsg = new Message(Message.Type.RESET_PASSWORD, account, "");
                resetMsg.setPassword(newPassword);
                chatClient.getNetworkManager().sendMessage(resetMsg);

                Message response = chatClient.getNetworkManager().receiveMessage();
                JOptionPane.showMessageDialog(chatClient.getLoginRegisterFrame(), response.getContent());

                if (response.getContent().contains("成功")) {
                    chatClient.getLoginPwdField().setText("");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(chatClient.getLoginRegisterFrame(), "重置密码失败：" + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}
```

### 3.6 客户端主类

创建 `client/ChatClient.java`：

```java
package client;

import client.managers.ChatManager;
import client.managers.DataManager;
import client.managers.FileManager;
import client.managers.WindowManager;
import client.network.NetworkManager;
import client.ui.ChatMainUI;
import client.ui.LoginRegisterUI;
import client.ui.UIComponentFactory;
import client.handler.MessageHandler;
import common.Group;
import common.Message;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 聊天室客户端
 */
public class ChatClient extends JFrame {
    private String serverIp = "127.0.0.1";
    private String username;
    
    private NetworkManager networkManager;
    private MessageHandler messageHandler;
    private UIComponentFactory uiComponentFactory;

    private ChatManager chatManager;
    private FileManager fileManager;
    private DataManager dataManager;
    private WindowManager windowManager;

    private JTextArea chatArea;
    private JTextField inputField;
    private JComboBox<String> chatTypeBox;
    private JComboBox<String> targetBox;
    private DefaultListModel<String> userListModel;
    private DefaultListModel<String> groupListModel;
    private Map<String, String> groupIdToNameMap;
    private Map<String, String> groupNameToIdMap;

    private JFrame loginRegisterFrame;
    private JTextField loginAccountField;
    private JPasswordField loginPwdField;
    private JTextField registerAccountField;
    private JPasswordField registerPwdField;

    public ChatClient() {
        uiComponentFactory = new UIComponentFactory();
        showLoginRegisterFrame();
    }

    public void login(String account, String password, String serverIp) {
        this.serverIp = serverIp;
        login(account, password);
    }

    public void login(String account, String password) {
        networkManager = new NetworkManager(serverIp);
        if (!networkManager.connectToServer()) {
            return;
        }

        Message loginMsg = new Message(Message.Type.LOGIN, account, "");
        loginMsg.setPassword(password);
        networkManager.sendMessage(loginMsg);

        Message response = networkManager.receiveMessage();
        if (response != null && response.getContent().contains("成功")) {
            this.username = account;
            SwingUtilities.invokeLater(() -> {
                loginRegisterFrame.dispose();
                initChatUI();
                startMessageListener();
            });
        } else {
            JOptionPane.showMessageDialog(loginRegisterFrame, response.getContent());
            loginPwdField.setText("");
        }
    }

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
        groupIdToNameMap = new java.util.HashMap<>();
        groupNameToIdMap = new java.util.HashMap<>();
        
        chatManager = new ChatManager(this);
        fileManager = new FileManager(this);
        dataManager = new DataManager(this);
        windowManager = new WindowManager(this);
        
        setTitle("Java Socket 聊天室(账号：" + username + ")");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    public void startMessageListener() {
        MessageListener listener = new MessageListener(networkManager, messageHandler, chatArea);
        listener.start();
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

    public void resetSocket() {
        if (networkManager != null) {
            networkManager.resetSocket();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatClient();
            }
        });
    }

    // Getter 方法
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

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }
}
```

---

## 第四阶段：运行和测试

### 4.1 启动服务端

```bash
# 编译服务端
javac -d bin -encoding UTF-8 src/common/*.java src/server/**/*.java

# 运行服务端
java -cp bin server.ChatServer
```

### 4.2 启动客户端

```bash
# 编译客户端
javac -d bin -encoding UTF-8 src/common/*.java src/client/**/*.java

# 运行客户端
java -cp bin client.ChatClient
```

### 4.3 测试功能

1. **注册新用户**：在登录界面点击"注册"按钮
2. **登录系统**：输入账号密码登录
3. **发送私聊消息**：选择私聊模式，选择目标用户，发送消息
4. **创建群聊**：点击"创建群聊"按钮，输入群名
5. **加入群聊**：在群列表中选择群聊加入
6. **发送群聊消息**：选择群聊模式，选择群聊，发送消息
7. **发送文件**：点击"选择文件"按钮，选择文件发送
8. **窗口抖动**：点击"窗口抖动"按钮发送抖动
9. **截图功能**：使用截图功能发送屏幕截图

---

## 项目特点

### 代码结构清晰
- 采用分层架构，职责分离
- Manager类负责业务逻辑
- Handler类负责消息处理
- UI类负责界面展示

### 可扩展性强
- 消息类型使用枚举，易于扩展
- Manager类独立，便于添加新功能
- 网络通信封装，便于更换协议

### 线程安全
- 使用ConcurrentHashMap保证并发安全
- Swing组件更新在事件调度线程中执行
- 消息监听独立线程运行

### 用户体验
- 美化的UI界面
- 圆角按钮和面板
- 实时消息更新
- 文件传输进度提示

---

## 总结

本教程详细介绍了如何从零开始开发一个功能完整的Java Socket聊天室应用。通过本教程，你将学到：

1. **Java Socket网络编程**基础
2. **多线程编程**处理并发
3. **Swing GUI开发**技巧
4. **面向对象设计**原则
5. **项目架构设计**方法

希望这个教程能帮助你理解并开发出自己的Java聊天室应用！
