package client;

import client.handler.MessageHandler;
import client.network.NetworkManager;
import common.Message;

import javax.swing.*;

public class MessageListener extends Thread {
    private final NetworkManager networkManager;
    private final MessageHandler messageHandler;
    private final JTextArea chatArea;

    public MessageListener(NetworkManager networkManager, MessageHandler messageHandler, JTextArea chatArea) {
        this.networkManager = networkManager;
        this.messageHandler = messageHandler;
        this.chatArea = chatArea;
    }

    @Override
    public void run() {
        try {
            Message message;
            while (true) {
                message = networkManager.receiveMessage();
                if (message != null) {
                    messageHandler.handleMessage(message);
                }
            }
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                chatArea.append("【系统消息】与服务器断开连接！\n");
            });
            e.printStackTrace();
        }
    }
}
