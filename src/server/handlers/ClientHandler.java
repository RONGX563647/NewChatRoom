package server.handlers;

import common.Group;
import common.Message;
import server.broadcast.BroadcastService;
import server.managers.GroupManager;
import server.managers.OnlineUserManager;
import server.managers.UserManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * 客户端处理线程
 * 负责处理单个客户端的所有请求
 */
public class ClientHandler extends Thread {
    private final Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private String username;

    private final UserManager userManager;
    private final OnlineUserManager onlineUserManager;
    private final GroupManager groupManager;
    private final BroadcastService broadcastService;
    private final MessageHandler messageHandler;

    public ClientHandler(Socket socket,
                      UserManager userManager,
                      OnlineUserManager onlineUserManager,
                      GroupManager groupManager,
                      BroadcastService broadcastService,
                      MessageHandler messageHandler) {
        this.socket = socket;
        this.userManager = userManager;
        this.onlineUserManager = onlineUserManager;
        this.groupManager = groupManager;
        this.broadcastService = broadcastService;
        this.messageHandler = messageHandler;
    }

    @Override
    public void run() {
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            Message message;
            while ((message = (Message) ois.readObject()) != null) {
                switch (message.getType()) {
                    case REGISTER:
                        userManager.handleRegister(message, oos);
                        break;
                    case LOGIN:
                        handleLoginWithAuth(message);
                        break;
                    case FIND_PASSWORD:
                        userManager.handleFindPassword(message, oos);
                        break;
                    case RESET_PASSWORD:
                        userManager.handleResetPassword(message, oos);
                        break;
                    case PRIVATE_CHAT:
                        messageHandler.handlePrivateChat(message, oos, username);
                        break;
                    case GROUP_CHAT:
                        messageHandler.handleGroupChat(message, oos, username);
                        break;
                    case GET_ONLINE_USERS:
                        broadcastService.sendOnlineUsersToUser(username);
                        break;
                    case FILE_PRIVATE:
                        messageHandler.handlePrivateFile(message, oos, username);
                        break;
                    case FILE_GROUP:
                        messageHandler.handleGroupFile(message, oos, username);
                        break;
                    case SHAKE:
                        messageHandler.handleShake(message, username);
                        break;
                    case CREATE_GROUP:
                        messageHandler.handleCreateGroup(message, oos, username);
                        break;
                    case SEARCH_GROUP:
                        messageHandler.handleSearchGroup(message, oos);
                        break;
                    case JOIN_GROUP:
                        messageHandler.handleJoinGroup(message, oos, username);
                        break;
                    default:
                        System.out.println("未知消息类型：" + message.getType());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            handleDisconnect();
        }
    }

    /**
     * 处理用户登录
     */
    private void handleLoginWithAuth(Message message) throws IOException {
        String account = message.getSender();
        String password = message.getPassword();

        if (!userManager.accountExists(account)) {
            Message response = new Message(Message.Type.LOGIN, "服务器", account, "账号未注册，请先注册！");
            oos.writeObject(response);
            oos.flush();
            return;
        }

        if (!userManager.verifyPassword(account, password)) {
            Message response = new Message(Message.Type.LOGIN, "服务器", account, "密码错误，请重新输入！");
            oos.writeObject(response);
            oos.flush();
            return;
        }

        if (onlineUserManager.isUserOnline(account)) {
            Message response = new Message(Message.Type.LOGIN, "服务器", account, "账号已登录，请勿重复登录！");
            oos.writeObject(response);
            oos.flush();
            return;
        }

        username = account;
        onlineUserManager.addUser(username, oos);

        Group defaultGroup = groupManager.getDefaultGroup();
        if (defaultGroup != null) {
            defaultGroup.addMember(username);
        }

        oos.writeObject(new Message(Message.Type.LOGIN, "服务器", username, "登录成功！"));
        oos.flush();

        broadcastService.broadcastOnlineNotify(username);
        broadcastService.broadcastOnlineUsers();
        broadcastService.broadcastGroupList();

        System.out.println(username + " 登录成功，当前在线人数：" + onlineUserManager.getOnlineUserCount());
    }

    /**
     * 处理客户端断开连接
     */
    private void handleDisconnect() {
        try {
            if (username != null && onlineUserManager.isUserOnline(username)) {
                onlineUserManager.removeUser(username);
                groupManager.removeUserFromAllGroups(username);

                broadcastService.broadcastOfflineNotify(username);
                broadcastService.broadcastOnlineUsers();
                broadcastService.broadcastGroupList();

                System.out.println(username + " 已下线，当前在线人数：" + onlineUserManager.getOnlineUserCount());
            }

            if (ois != null) ois.close();
            if (oos != null) oos.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
