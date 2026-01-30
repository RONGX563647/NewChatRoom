package client.handler;

import client.ChatClient;
import client.ui.ChatMainUI;
import client.utils.FileUtils;
import client.managers.DataManager;
import common.Message;

import javax.swing.*;

/**
 * 消息处理器类
 * 负责处理从服务器接收到的各种消息类型
 */
public class MessageHandler {
    private ChatClient chatClient;
    private ChatMainUI chatMainUI;
    private DataManager dataManager;

    public MessageHandler(ChatClient chatClient, ChatMainUI chatMainUI) {
        this.chatClient = chatClient;
        this.chatMainUI = chatMainUI;
        this.dataManager = new DataManager(chatClient);
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
                message.getFileName() + "（大小：" + FileUtils.formatFileSize(message.getFileSize()) + "）\n");
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
                FileUtils.formatFileSize(message.getFileSize()) + "）\n");
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
        dataManager.saveBytesToFile(data, fileName);
    }

    private void shakeWindow() {
        chatClient.shakeWindow();
    }
}