package client.managers;

import client.ChatClient;
import client.utils.FileUtils;
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

            JOptionPane.showMessageDialog(chatClient,
                    "已选择文件：" + selectedFile.getName() + "\n文件大小：" + FileUtils.formatFileSize(fileSize));
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
