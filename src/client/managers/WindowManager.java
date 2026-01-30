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
