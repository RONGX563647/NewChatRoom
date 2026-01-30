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
