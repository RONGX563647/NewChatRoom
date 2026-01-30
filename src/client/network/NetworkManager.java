package client.network;

import client.ChatClient;
import common.Message;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * 网络通信管理类
 * 负责处理客户端与服务器之间的网络通信
 */
public class NetworkManager {
    private ChatClient chatClient;
    private String serverIp;
    private static final int SERVER_PORT = 8888;

    // Socket相关
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    public NetworkManager(ChatClient chatClient, String serverIp) {
        this.chatClient = chatClient;
        this.serverIp = serverIp;
    }

    /**
     * 连接到服务器
     */
    public boolean connectToServer() {
        try {
            if (socket == null || socket.isClosed() || !socket.isConnected()) {
                socket = new Socket(serverIp, SERVER_PORT);
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());
                return true;
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(chatClient, "连接服务器失败：" + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 发送消息到服务器
     */
    public void sendMessage(Message message) {
        try {
            if (oos != null) {
                oos.writeObject(message);
                oos.flush();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(chatClient, "发送消息失败：" + e.getMessage());
            e.printStackTrace();
            resetSocket();
        }
    }

    /**
     * 接收来自服务器的消息
     */
    public Message receiveMessage() {
        try {
            if (ois != null) {
                return (Message) ois.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(chatClient, "接收消息失败：" + e.getMessage());
            e.printStackTrace();
            resetSocket();
        }
        return null;
    }

    /**
     * 获取输出流
     */
    public ObjectOutputStream getOutputStream() {
        return oos;
    }

    /**
     * 获取输入流
     */
    public ObjectInputStream getInputStream() {
        return ois;
    }

    /**
     * 关闭网络连接
     */
    public void closeConnection() {
        try {
            if (ois != null) ois.close();
            if (oos != null) oos.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 重置Socket连接
     */
    public void resetSocket() {
        try {
            if (ois != null) ois.close();
            if (oos != null) oos.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket = null;
        oos = null;
        ois = null;
    }

    /**
     * 检查是否已连接到服务器
     */
    public boolean isConnected() {
        return socket != null && !socket.isClosed() && socket.isConnected();
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }
}