package server;

import common.Group;
import common.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天室服务端
 */
public class ChatServer {
    // 端口号
    private static final int PORT = 8888;
    // 保存用户连接：用户名 -> 输出流（用于发送消息）
    private static Map<String, ObjectOutputStream> userMap = new ConcurrentHashMap<>();
    private static Map<String, Group> groupMap = new ConcurrentHashMap<>(); // 群ID→群实体
    private static Map<String, String> userAuthMap = new ConcurrentHashMap<>(); // 新增：用户认证信息（账号→密码）

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("聊天室服务端已启动，端口：" + PORT);

            // 初始化默认群组
            // 初始化默认群（生成UUID作为群ID）
            Group defaultGroup = new Group(UUID.randomUUID().toString(), "默认群");
            groupMap.put(defaultGroup.getGroupId(), defaultGroup);

            // 循环接收客户端连接
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("新客户端连接：" + clientSocket);
                // 为每个客户端创建独立线程处理
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 新增：获取在线用户列表
    private static List<String> getOnlineUsers() {
        return new ArrayList<>(userMap.keySet());
    }

    // 新增：获取所有群列表
    private static List<Group> getAllGroups() {
        return new ArrayList<>(groupMap.values());
    }

    // 新增：推送在线用户列表给所有客户端
    private static void broadcastOnlineUsers() throws IOException {
        List<String> onlineUsers = getOnlineUsers();
        Message usersMsg = new Message(Message.Type.ONLINE_USERS, "服务器");
        usersMsg.setOnlineUsers(onlineUsers); // 设置在线用户列表
        for (ObjectOutputStream oos : userMap.values()) {
            oos.writeObject(usersMsg);
            oos.flush();
        }
    }

    // 新增：推送群列表给所有客户端
    private static void broadcastGroupList() throws IOException {
        List<Group> groupList = getAllGroups();
        Message groupMsg = new Message(Message.Type.GROUP_LIST, "服务器");
        groupMsg.setGroupList(groupList); // 设置群列表
        for (ObjectOutputStream oos : userMap.values()) {
            oos.writeObject(groupMsg);
            oos.flush();
        }
    }

    /**
     * 客户端处理线程
     */
    static class ClientHandler extends Thread {
        private Socket socket;
        private ObjectInputStream ois;
        private ObjectOutputStream oos;
        private String username; // 当前连接的用户名

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                // 初始化流（注意：先创建输出流，再创建输入流）
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());

                // 处理客户端消息
                Message message;
                while ((message = (Message) ois.readObject()) != null) {
                    switch (message.getType()) {
                        case REGISTER: // 新增：处理注册请求
                            handleRegister(message);
                            break;
                        case LOGIN: // 改造：登录需校验密码
                            handleLoginWithAuth(message);
                            break;
                        // 新增：处理找回密码（验证账号）
                        case FIND_PASSWORD:
                            handleFindPassword(message);
                            break;
                        // 新增：处理重置密码
                        case RESET_PASSWORD:
                            handleResetPassword(message);
                            break;
                        case PRIVATE_CHAT:
                            handlePrivateChat(message);
                            break;
                        case GROUP_CHAT:
                            handleGroupChat(message);
                            break;
                        case GET_ONLINE_USERS: // 新增：处理获取在线用户请求
                            sendOnlineUsers();
                            break;
                        // 新增：处理私聊文件
                        case FILE_PRIVATE:
                            handlePrivateFile(message);
                            break;
                        // 新增：处理群聊文件
                        case FILE_GROUP:
                            handleGroupFile(message);
                            break;
                        // 新增：处理窗口抖动消息
                        case SHAKE:
                            handleShake(message);
                            break;
                        // 新增：处理创建群聊
                        case CREATE_GROUP:
                            handleCreateGroup(message);
                            break;
                        // 新增：处理查找群聊
                        case SEARCH_GROUP:
                            handleSearchGroup(message);
                            break;
                        // 新增：处理加入群聊
                        case JOIN_GROUP:
                            handleJoinGroup(message);
                            break;
                        default:
                            System.out.println("未知消息类型：" + message.getType());
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                // 客户端断开连接
                handleDisconnect();
            }
        }

        // ===== 2. 新增 handleFindPassword 方法（验证账号是否存在） =====
        private void handleFindPassword(Message message) throws IOException {
            String account = message.getSender(); // 找回密码的账号

            // 校验账号是否为空
            if (account == null || account.isEmpty()) {
                Message response = new Message(Message.Type.FIND_PASSWORD_RESPONSE, "服务器", account, "账号不能为空！");
                oos.writeObject(response);
                oos.flush();
                return;
            }

            // 校验账号是否存在
            if (userAuthMap.containsKey(account)) {
                Message response = new Message(Message.Type.FIND_PASSWORD_RESPONSE, "服务器", account, "账号验证通过！请输入新密码");
                oos.writeObject(response);
                oos.flush();
            } else {
                Message response = new Message(Message.Type.FIND_PASSWORD_RESPONSE, "服务器", account, "账号不存在，请检查！");
                oos.writeObject(response);
                oos.flush();
            }
        }

        // ===== 3. 新增 handleResetPassword 方法（重置密码） =====
        private void handleResetPassword(Message message) throws IOException {
            String account = message.getSender(); // 要重置的账号
            String newPassword = message.getPassword(); // 新密码

            // 校验账号和新密码是否为空
            if (account == null || account.isEmpty() || newPassword == null || newPassword.isEmpty()) {
                Message response = new Message(Message.Type.RESET_PASSWORD_RESPONSE, "服务器", account, "账号或新密码不能为空！");
                oos.writeObject(response);
                oos.flush();
                return;
            }

            // 校验账号是否存在
            if (!userAuthMap.containsKey(account)) {
                Message response = new Message(Message.Type.RESET_PASSWORD_RESPONSE, "服务器", account, "账号不存在，重置失败！");
                oos.writeObject(response);
                oos.flush();
                return;
            }

            // 重置密码（更新userAuthMap）
            userAuthMap.put(account, newPassword);
            Message response = new Message(Message.Type.RESET_PASSWORD_RESPONSE, "服务器", account, "密码重置成功！请使用新密码登录");
            oos.writeObject(response);
            oos.flush();
            System.out.println("用户 " + account + " 密码重置成功");
        }

        // ========== 新增：处理注册请求 ==========
        private void handleRegister(Message message) throws IOException {
            String account = message.getSender(); // 注册账号
            String password = message.getPassword(); // 注册密码

            // 校验账号是否为空
            if (account == null || account.isEmpty() || password == null || password.isEmpty()) {
                Message response = new Message(Message.Type.REGISTER_RESPONSE, "服务器", account, "账号或密码不能为空！");
                oos.writeObject(response);
                oos.flush();
                return;
            }

            // 校验账号是否已存在
            if (userAuthMap.containsKey(account)) {
                Message response = new Message(Message.Type.REGISTER_RESPONSE, "服务器", account, "账号已存在，请更换！");
                oos.writeObject(response);
                oos.flush();
            } else {
                // 注册成功，保存账号密码
                userAuthMap.put(account, password);
                Message response = new Message(Message.Type.REGISTER_RESPONSE, "服务器", account, "注册成功！请返回登录");
                oos.writeObject(response);
                oos.flush();
                System.out.println("用户 " + account + " 注册成功");
            }
        }

        /**
         * 处理用户登录
         */
        private void handleLoginWithAuth(Message message) throws IOException {
            String account = message.getSender();// 登录账号
            String password = message.getPassword(); // 登录密码
            // 1. 校验账号是否注册
            if (!userAuthMap.containsKey(account)) {
                Message response = new Message(Message.Type.LOGIN, "服务器", account, "账号未注册，请先注册！");
                oos.writeObject(response);
                oos.flush();
                //socket.close();
                return;
            }

            // 2. 校验密码是否正确
            if (!userAuthMap.get(account).equals(password)) {
                Message response = new Message(Message.Type.LOGIN, "服务器", account, "密码错误，请重新输入！");
                oos.writeObject(response);
                oos.flush();
                //socket.close();
                return;
            }

            // 3. 校验是否已登录
            if (userMap.containsKey(account)) {
                Message response = new Message(Message.Type.LOGIN, "服务器", account, "账号已登录，请勿重复登录！");
                oos.writeObject(response);
                oos.flush();
                //socket.close();
                return;
            }

            // 保存用户连接
            username = account;
            userMap.put(username, oos);
            // 将用户加入默认群
            Group defaultGroup = groupMap.values().stream().filter(g -> g.getGroupName().equals("默认群")).findFirst().get();
            defaultGroup.addMember(username);

            // 登录成功响应
            oos.writeObject(new Message(Message.Type.LOGIN, "服务器", username, "登录成功！"));
            oos.flush();

            // 广播用户上线通知
            broadcastOnlineNotify(username);
            // 新增：推送最新的在线用户列表给所有客户端
            broadcastOnlineUsers();
            broadcastGroupList(); // 新增：推送群列表


            System.out.println(username + " 登录成功，当前在线人数：" + userMap.size());
        }

        /**
         * 处理私聊消息
         */
        private void handlePrivateChat(Message message) throws IOException {
            String receiver = message.getReceiver();
            String content = message.getContent();

            // 获取接收者的输出流
            ObjectOutputStream receiverOos = userMap.get(receiver);
            if (receiverOos != null) {
                // 转发私聊消息
                receiverOos.writeObject(new Message(
                        Message.Type.PRIVATE_CHAT,
                        message.getSender(),
                        receiver,
                        content
                ));
                receiverOos.flush();
            } else {
                // 接收者不在线
                oos.writeObject(new Message(
                        Message.Type.PRIVATE_CHAT,
                        "服务器",
                        username,
                        "用户 " + receiver + " 不在线！"
                ));
                oos.flush();
            }
        }

        /**
         * 处理群聊消息
         */
        private void handleGroupChat(Message message) throws IOException {
            String groupId = message.getReceiver();
            String content = message.getContent();

            // 获取群组内的所有成员
            Group group = groupMap.get(groupId);
            if (group == null) {
                oos.writeObject(new Message(
                        Message.Type.GROUP_CHAT,
                        "服务器",
                        username,
                        "群组 " + group + " 不存在！"
                ));
                oos.flush();
                return;
            }

            // 向群组内所有成员转发消息（排除发送者自己）
            for (String member : group.getMembers()) {
                if (!member.equals(username)) {
                    ObjectOutputStream memberOos = userMap.get(member);
                    if (memberOos != null) {
                        memberOos.writeObject(new Message(
                                Message.Type.GROUP_CHAT,
                                message.getSender(),
                                groupId,
                                content
                        ));
                        memberOos.flush();
                    }
                }
            }
        }

        // 新增：发送在线用户列表给当前客户端
        private void sendOnlineUsers() throws IOException {
            List<String> onlineUsers = getOnlineUsers();
            Message usersMsg = new Message(Message.Type.ONLINE_USERS, "服务器");
            usersMsg.setOnlineUsers(onlineUsers); // 设置在线用户列表
            oos.writeObject(usersMsg);
            oos.flush();
        }

        /**
         * 处理客户端断开连接
         */
        private void handleDisconnect() {
            try {
                if (username != null && userMap.containsKey(username)) {
                    // 移除用户连接
                    userMap.remove(username);
                    // 从所有群中移除该用户
                    for (Group group : groupMap.values()) {
                        group.removeMember(username);
                    }
                    // 广播用户下线通知
                    broadcastOfflineNotify(username);
                    // 新增：推送最新的在线用户列表
                    broadcastOnlineUsers();
                    broadcastGroupList(); // 推送更新后的群列表
                    System.out.println(username + " 已下线，当前在线人数：" + userMap.size());
                }
                // 关闭资源
                if (ois != null) ois.close();
                if (oos != null) oos.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * 广播用户上线通知
         */
        private void broadcastOnlineNotify(String username) throws IOException {
            Message notifyMsg = new Message(
                    Message.Type.ONLINE_NOTIFY,
                    "服务器",
                    "",
                    username + " 已上线！"
            );
            // 发送给所有在线用户
            for (ObjectOutputStream oos : userMap.values()) {
                oos.writeObject(notifyMsg);
                oos.flush();
            }
        }

        /**
         * 广播用户下线通知
         */
        private void broadcastOfflineNotify(String username) throws IOException {
            Message notifyMsg = new Message(
                    Message.Type.OFFLINE_NOTIFY,
                    "服务器",
                    "",
                    username + " 已下线！"
            );
            // 发送给所有在线用户
            for (ObjectOutputStream oos : userMap.values()) {
                oos.writeObject(notifyMsg);
                oos.flush();
            }
        }

        // 新增：处理私聊文件转发
        private void handlePrivateFile(Message message) throws IOException {
            String receiver = message.getReceiver();
            ObjectOutputStream receiverOos = userMap.get(receiver);

            if (receiverOos != null) {
                // 直接转发文件消息给目标用户
                receiverOos.writeObject(message);
                receiverOos.flush();
                System.out.println(username + " 发送文件[" + message.getFileName() + "]给" + receiver);
            } else {
                // 接收者不在线，反馈给发送者
                oos.writeObject(new Message(
                        Message.Type.PRIVATE_CHAT,
                        "服务器",
                        username,
                        "用户 " + receiver + " 不在线，文件发送失败！"
                ));
                oos.flush();
            }
        }

        // 新增：处理群聊文件转发
        private void handleGroupFile(Message message) throws IOException {
            String groupName = message.getReceiver();
            Group group = groupMap.get(groupName);

            if (group == null) {
                oos.writeObject(new Message(
                        Message.Type.GROUP_CHAT,
                        "服务器",
                        username,
                        "群组 " + groupName + " 不存在，文件发送失败！"
                ));
                oos.flush();
                return;
            }

            // 转发文件给群内所有成员（排除发送者）
            for (String member : group.getMembers()) {
                if (!member.equals(username)) {
                    ObjectOutputStream memberOos = userMap.get(member);
                    if (memberOos != null) {
                        memberOos.writeObject(message);
                        memberOos.flush();
                    }
                }
            }
            System.out.println(username + " 向群[" + groupName + "]发送文件[" + message.getFileName() + "]");
        }

        // 新增：处理窗口抖动消息
        private void handleShake(Message message) throws IOException {
            String target = message.getReceiver();
            // 判断是私聊抖动（目标是用户名）还是群聊抖动（目标是群组名）
            if (userMap.containsKey(target)) {
                // 私聊抖动：转发给指定用户
                ObjectOutputStream targetOos = userMap.get(target);
                if (targetOos != null) {
                    targetOos.writeObject(message);
                    targetOos.flush();
                    System.out.println(username + " 向" + target + "发送窗口抖动");
                } else {
                    // 目标用户不在线，反馈给发送者
                    oos.writeObject(new Message(
                            Message.Type.PRIVATE_CHAT,
                            "服务器",
                            username,
                            "用户 " + target + " 不在线，抖动发送失败！"
                    ));
                    oos.flush();
                }
            } else if (groupMap.containsKey(target)) {
                Group group = groupMap.get(target);
                // 群聊抖动：转发给群内所有成员（排除发送者）
                for (String member : group.getMembers()) {
                    if (!member.equals(username)) {
                        ObjectOutputStream memberOos = userMap.get(member);
                        if (memberOos != null) {
                            memberOos.writeObject(message);
                            memberOos.flush();
                        }
                    }
                }
                System.out.println(username + " 向群[" + target + "]发送窗口抖动");
            } else {
                // 目标不存在（非用户/非群组）
                oos.writeObject(new Message(
                        Message.Type.PRIVATE_CHAT,
                        "服务器",
                        username,
                        "目标 " + target + " 不存在，抖动发送失败！"
                ));
                oos.flush();
            }
        }

        private void handleCreateGroup(Message message) throws IOException {
            String groupName = message.getGroupName();
            if (groupName == null || groupName.isEmpty()) {
                oos.writeObject(new Message(Message.Type.CREATE_GROUP, "服务器", username, "群名称不能为空！"));
                oos.flush();
                return;
            }

            // 生成唯一群ID，创建群实体
            String groupId = UUID.randomUUID().toString();
            Group newGroup = new Group(groupId, groupName);
            newGroup.addMember(username); // 建立者自动加入
            groupMap.put(groupId, newGroup);

            // 响应创建成功（返回群ID和群名）
            oos.writeObject(new Message(Message.Type.CREATE_GROUP, "服务器", username, "群聊创建成功！群ID：" + groupId + "，群名：" + groupName));
            oos.flush();

            // 推送最新群列表给所有客户端
            broadcastGroupList();
            System.out.println(username + " 创建群聊：" + groupName + "（群ID：" + groupId + "）");
        }

        /**
         * 处理查找群聊请求（按群ID）
         */
        private void handleSearchGroup(Message message) throws IOException {
            String groupId = message.getGroupId();
            Group group = groupMap.get(groupId);

            if (group == null) {
                oos.writeObject(new Message(Message.Type.SEARCH_GROUP, "服务器", username, "未找到群ID为" + groupId + "的群聊！"));
                oos.flush();
            } else {
                oos.writeObject(new Message(Message.Type.SEARCH_GROUP, "服务器", username, "找到群聊：" + group.getGroupName() + "（群ID：" + groupId + "）"));
                oos.flush();
            }
        }

        /**
         * 处理加入群聊请求
         */
        private void handleJoinGroup(Message message) throws IOException {
            String groupId = message.getGroupId();
            Group group = groupMap.get(groupId);

            if (group == null) {
                oos.writeObject(new Message(Message.Type.JOIN_GROUP, "服务器", username, "群ID不存在，加入失败！"));
                oos.flush();
                return;
            }

            if (group.getMembers().contains(username)) {
                oos.writeObject(new Message(Message.Type.JOIN_GROUP, "服务器", username, "你已加入该群聊，无需重复加入！"));
                oos.flush();
                return;
            }

            // 加入群聊
            group.addMember(username);
            oos.writeObject(new Message(Message.Type.JOIN_GROUP, "服务器", username, "成功加入群聊：" + group.getGroupName() + "（群ID：" + groupId + "）"));
            oos.flush();

            // 推送最新群列表
            broadcastGroupList();
            System.out.println(username + " 加入群聊：" + group.getGroupName() + "（群ID：" + groupId + "）");
        }
    }
}
