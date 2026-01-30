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
