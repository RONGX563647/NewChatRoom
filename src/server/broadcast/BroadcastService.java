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
 * 负责向所有在线用户广播消息
 */
public class BroadcastService {
    private final OnlineUserManager onlineUserManager;
    private final GroupManager groupManager;

    public BroadcastService(OnlineUserManager onlineUserManager, GroupManager groupManager) {
        this.onlineUserManager = onlineUserManager;
        this.groupManager = groupManager;
    }

    /**
     * 广播用户上线通知
     */
    public void broadcastOnlineNotify(String username) throws IOException {
        Message notifyMsg = new Message(
                Message.Type.ONLINE_NOTIFY,
                "服务器",
                "",
                username + " 已上线！"
        );
        broadcastToAll(notifyMsg);
    }

    /**
     * 广播用户下线通知
     */
    public void broadcastOfflineNotify(String username) throws IOException {
        Message notifyMsg = new Message(
                Message.Type.OFFLINE_NOTIFY,
                "服务器",
                "",
                username + " 已下线！"
        );
        broadcastToAll(notifyMsg);
    }

    /**
     * 广播在线用户列表给所有客户端
     */
    public void broadcastOnlineUsers() throws IOException {
        List<String> onlineUsers = onlineUserManager.getOnlineUsers();
        Message usersMsg = new Message(Message.Type.ONLINE_USERS, "服务器");
        usersMsg.setOnlineUsers(onlineUsers);
        broadcastToAll(usersMsg);
    }

    /**
     * 广播群列表给所有客户端
     */
    public void broadcastGroupList() throws IOException {
        List<Group> groupList = groupManager.getAllGroups();
        Message groupMsg = new Message(Message.Type.GROUP_LIST, "服务器");
        groupMsg.setGroupList(groupList);
        broadcastToAll(groupMsg);
    }

    /**
     * 发送在线用户列表给指定用户
     */
    public void sendOnlineUsersToUser(String username) throws IOException {
        List<String> onlineUsers = onlineUserManager.getOnlineUsers();
        Message usersMsg = new Message(Message.Type.ONLINE_USERS, "服务器");
        usersMsg.setOnlineUsers(onlineUsers);
        onlineUserManager.sendMessageToUser(username, usersMsg);
    }

    /**
     * 向所有在线用户广播消息
     */
    private void broadcastToAll(Message message) throws IOException {
        for (ObjectOutputStream oos : onlineUserManager.getAllOutputStreams()) {
            oos.writeObject(message);
            oos.flush();
        }
    }
}
