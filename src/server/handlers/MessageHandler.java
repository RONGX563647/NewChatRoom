package server.handlers;

import common.Group;
import common.Message;
import server.broadcast.BroadcastService;
import server.managers.GroupManager;
import server.managers.OnlineUserManager;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * 消息处理器类
 * 负责处理各种类型的消息（私聊、群聊、文件、抖动等）
 */
public class MessageHandler {
    private final OnlineUserManager onlineUserManager;
    private final GroupManager groupManager;
    private final BroadcastService broadcastService;

    public MessageHandler(OnlineUserManager onlineUserManager,
                       GroupManager groupManager,
                       BroadcastService broadcastService) {
        this.onlineUserManager = onlineUserManager;
        this.groupManager = groupManager;
        this.broadcastService = broadcastService;
    }

    /**
     * 处理私聊消息
     */
    public void handlePrivateChat(Message message, ObjectOutputStream senderOos, String sender) throws IOException {
        String receiver = message.getReceiver();
        String content = message.getContent();

        ObjectOutputStream receiverOos = onlineUserManager.getUserOutputStream(receiver);
        if (receiverOos != null) {
            receiverOos.writeObject(new Message(
                    Message.Type.PRIVATE_CHAT,
                    message.getSender(),
                    receiver,
                    content
            ));
            receiverOos.flush();
        } else {
            senderOos.writeObject(new Message(
                    Message.Type.PRIVATE_CHAT,
                    "服务器",
                    sender,
                    "用户 " + receiver + " 不在线！"
            ));
            senderOos.flush();
        }
    }

    /**
     * 处理群聊消息
     */
    public void handleGroupChat(Message message, ObjectOutputStream senderOos, String sender) throws IOException {
        String groupId = message.getReceiver();
        String content = message.getContent();

        Group group = groupManager.getGroupById(groupId);
        if (group == null) {
            senderOos.writeObject(new Message(
                    Message.Type.GROUP_CHAT,
                    "服务器",
                    sender,
                    "群组 " + groupId + " 不存在！"
            ));
            senderOos.flush();
            return;
        }

        for (String member : group.getMembers()) {
            if (!member.equals(sender)) {
                ObjectOutputStream memberOos = onlineUserManager.getUserOutputStream(member);
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

    /**
     * 处理私聊文件
     */
    public void handlePrivateFile(Message message, ObjectOutputStream senderOos, String sender) throws IOException {
        String receiver = message.getReceiver();
        ObjectOutputStream receiverOos = onlineUserManager.getUserOutputStream(receiver);

        if (receiverOos != null) {
            receiverOos.writeObject(message);
            receiverOos.flush();
            System.out.println(sender + " 发送文件[" + message.getFileName() + "]给" + receiver);
        } else {
            senderOos.writeObject(new Message(
                    Message.Type.PRIVATE_CHAT,
                    "服务器",
                    sender,
                    "用户 " + receiver + " 不在线，文件发送失败！"
            ));
            senderOos.flush();
        }
    }

    /**
     * 处理群聊文件
     */
    public void handleGroupFile(Message message, ObjectOutputStream senderOos, String sender) throws IOException {
        String groupName = message.getReceiver();
        Group group = groupManager.getGroupByName(groupName);

        if (group == null) {
            senderOos.writeObject(new Message(
                    Message.Type.GROUP_CHAT,
                    "服务器",
                    sender,
                    "群组 " + groupName + " 不存在，文件发送失败！"
            ));
            senderOos.flush();
            return;
        }

        for (String member : group.getMembers()) {
            if (!member.equals(sender)) {
                ObjectOutputStream memberOos = onlineUserManager.getUserOutputStream(member);
                if (memberOos != null) {
                    memberOos.writeObject(message);
                    memberOos.flush();
                }
            }
        }
        System.out.println(sender + " 向群[" + groupName + "]发送文件[" + message.getFileName() + "]");
    }

    /**
     * 处理窗口抖动
     */
    public void handleShake(Message message, String sender) throws IOException {
        String target = message.getReceiver();

        if (onlineUserManager.isUserOnline(target)) {
            ObjectOutputStream targetOos = onlineUserManager.getUserOutputStream(target);
            if (targetOos != null) {
                targetOos.writeObject(message);
                targetOos.flush();
                System.out.println(sender + " 向" + target + "发送窗口抖动");
            }
        } else {
            Group group = groupManager.getGroupByName(target);
            if (group != null) {
                for (String member : group.getMembers()) {
                    if (!member.equals(sender)) {
                        ObjectOutputStream memberOos = onlineUserManager.getUserOutputStream(member);
                        if (memberOos != null) {
                            memberOos.writeObject(message);
                            memberOos.flush();
                        }
                    }
                }
                System.out.println(sender + " 向群[" + target + "]发送窗口抖动");
            }
        }
    }

    /**
     * 处理创建群聊
     */
    public void handleCreateGroup(Message message, ObjectOutputStream oos, String sender) throws IOException {
        String groupName = message.getContent();
        Group group = groupManager.createGroup(groupName);
        group.addMember(sender);

        oos.writeObject(new Message(
                Message.Type.GROUP_CHAT,
                "服务器",
                sender,
                "创建群组[" + groupName + "]成功！群ID：" + group.getGroupId()
        ));
        oos.flush();

        broadcastService.broadcastGroupList();
        System.out.println(sender + " 创建群组[" + groupName + "]");
    }

    /**
     * 处理查找群聊
     */
    public void handleSearchGroup(Message message, ObjectOutputStream oos) throws IOException {
        String keyword = message.getContent();
        List<Group> groups = groupManager.searchGroups(keyword);

        StringBuilder result = new StringBuilder();
        if (groups.isEmpty()) {
            result.append("未找到包含[").append(keyword).append("]的群组");
        } else {
            result.append("找到以下群组：\n");
            for (Group group : groups) {
                result.append("- ").append(group.getGroupName())
                        .append(" (ID: ").append(group.getGroupId()).append(")\n");
            }
        }

        oos.writeObject(new Message(
                Message.Type.GROUP_CHAT,
                "服务器",
                "",
                result.toString()
        ));
        oos.flush();
    }

    /**
     * 处理加入群聊
     */
    public void handleJoinGroup(Message message, ObjectOutputStream oos, String sender) throws IOException {
        String groupId = message.getReceiver();

        if (groupManager.joinGroup(groupId, sender)) {
            Group group = groupManager.getGroupById(groupId);
            oos.writeObject(new Message(
                    Message.Type.GROUP_CHAT,
                    "服务器",
                    sender,
                    "成功加入群组[" + group.getGroupName() + "]！"
            ));
            oos.flush();
            System.out.println(sender + " 加入群组[" + group.getGroupName() + "]");
        } else {
            oos.writeObject(new Message(
                    Message.Type.GROUP_CHAT,
                    "服务器",
                    sender,
                    "加入群组失败，群组不存在！"
            ));
            oos.flush();
        }
    }
}
