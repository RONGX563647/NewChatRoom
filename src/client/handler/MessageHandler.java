package client.handler;

import client.ChatClient;
import client.ui.ChatMainUI;
import common.Group;
import common.Message;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 消息处理器类
 * 负责处理从服务器接收到的各种消息类型
 */
public class MessageHandler {
    private ChatClient chatClient;
    private ChatMainUI chatMainUI;

    public MessageHandler(ChatClient chatClient, ChatMainUI chatMainUI) {
        this.chatClient = chatClient;
        this.chatMainUI = chatMainUI;
    }

    /**
     * 处理接收到的消息
     */
    public void handleMessage(Message message) {
        switch (message.getType()) {
            case PRIVATE_CHAT:
                handlePrivateChat(message);
                break;
            case GROUP_CHAT:
                handleGroupChat(message);
                break;
            case ONLINE_NOTIFY:
            case OFFLINE_NOTIFY:
                handleNotification(message);
                break;
            case ONLINE_USERS:
                handleOnlineUsers(message);
                break;
            // 新增：处理私聊文件接收
            case FILE_PRIVATE:
                handlePrivateFile(message);
                break;
            // 新增：处理群聊文件接收
            case FILE_GROUP:
                handleGroupFile(message);
                break;
            // 新增：处理窗口抖动消息
            case SHAKE:
                handleShake(message);
                break;
            // 新增：处理创建/查找/加入群聊响应
            case CREATE_GROUP:
                handleCreateGroupResponse(message);
                break;
            case SEARCH_GROUP:
                handleSearchGroupResponse(message);
                break;
            case JOIN_GROUP:
                handleJoinGroupResponse(message);
                break;
            // 新增：更新群列表
            case GROUP_LIST:
                handleGroupList(message);
                break;
            case FIND_PASSWORD_RESPONSE:
            case RESET_PASSWORD_RESPONSE:
            case REGISTER_RESPONSE:
                handleGeneralResponse(message);
                break;
            default:
                handleUnknownMessage(message);
        }
        // 滚动到最新消息
        SwingUtilities.invokeLater(() -> {
            chatClient.getChatArea().setCaretPosition(chatClient.getChatArea().getText().length());
        });
    }

    /**
     * 处理私聊消息
     */
    private void handlePrivateChat(Message message) {
        SwingUtilities.invokeLater(() -> {
            chatClient.getChatArea().append("【私聊-" + message.getSender() + "】" + message.getContent() + "\n");
        });
    }

    /**
     * 处理群聊消息
     */
    private void handleGroupChat(Message message) {
        SwingUtilities.invokeLater(() -> {
            String groupName = chatClient.getGroupIdToNameMap().get(message.getGroupId());
            if (groupName != null) {
                chatClient.getChatArea().append("【群聊-" + groupName + "】" + message.getSender() + "：" + message.getContent() + "\n");
            } else {
                chatClient.getChatArea().append("【群聊-" + message.getReceiver() + "】" + message.getSender() + "：" + message.getContent() + "\n");
            }
        });
    }

    /**
     * 处理系统通知消息
     */
    private void handleNotification(Message message) {
        SwingUtilities.invokeLater(() -> {
            chatClient.getChatArea().append("【系统消息】" + message.getContent() + "\n");
        });
    }

    /**
     * 处理在线用户列表
     */
    private void handleOnlineUsers(Message message) {
        chatMainUI.updateUserList(message.getOnlineUsers());
    }

    /**
     * 处理私聊文件接收
     */
    private void handlePrivateFile(Message message) {
        SwingUtilities.invokeLater(() -> {
            chatClient.getChatArea().append("【系统消息】" + message.getSender() + " 发送私聊文件：" + 
                message.getFileName() + "（大小：" + formatFileSize(message.getFileSize()) + "）\n");
            saveBytesToFile(message.getFileData(), message.getFileName());
        });
    }

    /**
     * 处理群聊文件接收
     */
    private void handleGroupFile(Message message) {
        SwingUtilities.invokeLater(() -> {
            chatClient.getChatArea().append("【系统消息】" + message.getSender() + " 发送群文件[" + 
                message.getReceiver() + "]：" + message.getFileName() + "（大小：" + 
                formatFileSize(message.getFileSize()) + "）\n");
            saveBytesToFile(message.getFileData(), message.getFileName());
        });
    }

    /**
     * 处理窗口抖动消息
     */
    private void handleShake(Message message) {
        SwingUtilities.invokeLater(() -> {
            chatClient.getChatArea().append("【系统消息】" + message.getSender() + " 向你发送了窗口抖动！\n");
            // 执行窗口抖动
            shakeWindow();
        });
    }

    /**
     * 处理创建群聊响应
     */
    private void handleCreateGroupResponse(Message message) {
        SwingUtilities.invokeLater(() -> {
            chatClient.getChatArea().append("【系统消息】" + message.getContent() + "\n");
        });
    }

    /**
     * 处理查找群聊响应
     */
    private void handleSearchGroupResponse(Message message) {
        SwingUtilities.invokeLater(() -> {
            chatClient.getChatArea().append("【系统消息】" + message.getContent() + "\n");
        });
    }

    /**
     * 处理加入群聊响应
     */
    private void handleJoinGroupResponse(Message message) {
        SwingUtilities.invokeLater(() -> {
            chatClient.getChatArea().append("【系统消息】" + message.getContent() + "\n");
        });
    }

    /**
     * 处理群列表更新
     */
    private void handleGroupList(Message message) {
        chatMainUI.updateGroupList(message.getGroupList());
    }

    /**
     * 处理一般响应消息（注册、找回密码、重置密码等）
     */
    private void handleGeneralResponse(Message message) {
        SwingUtilities.invokeLater(() -> {
            chatClient.getChatArea().append("【系统消息】" + message.getContent() + "\n");
        });
    }

    /**
     * 处理未知消息类型
     */
    private void handleUnknownMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            chatClient.getChatArea().append("【未知消息】" + message.getContent() + "\n");
        });
    }

    /**
     * 保存字节数组为文件
     */
    private void saveBytesToFile(byte[] data, String fileName) {
        // 调用ChatClient中的方法
        chatClient.saveBytesToFile(data, fileName);
    }

    /**
     * 格式化文件大小（字节→KB/MB）
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        }
    }

    /**
     * 执行窗口抖动动画（线程安全）
     */
    private void shakeWindow() {
        // 防止重复抖动
        if (chatClient.isShaking()) {
            return;
        }
        chatClient.setShaking(true);

        // 记录窗口原始位置
        Point originalLocation = chatClient.getLocation();
        int shakeTimes = 8; // 抖动次数
        int shakeOffset = 5; // 抖动偏移量（像素）
        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            int count = 0;
            // 抖动方向：左右上下交替
            int[] dx = {shakeOffset, -shakeOffset, shakeOffset, -shakeOffset, 
                        shakeOffset, -shakeOffset, shakeOffset, -shakeOffset};
            int[] dy = {shakeOffset, -shakeOffset, -shakeOffset, shakeOffset, 
                        shakeOffset, -shakeOffset, -shakeOffset, shakeOffset};

            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    if (count < shakeTimes) {
                        // 移动窗口
                        chatClient.setLocation(originalLocation.x + dx[count], originalLocation.y + dy[count]);
                        count++;
                    } else {
                        // 恢复原始位置，停止抖动
                        chatClient.setLocation(originalLocation);
                        timer.cancel();
                        chatClient.setShaking(false);
                    }
                });
            }
        }, 0, 50); // 每50毫秒抖动一次，共8次（400毫秒）
    }
}