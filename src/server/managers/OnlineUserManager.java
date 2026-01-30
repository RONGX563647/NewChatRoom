package server.managers;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 在线用户管理类
 * 负责管理在线用户及其输出流
 */
public class OnlineUserManager {
    private final Map<String, ObjectOutputStream> userMap;

    public OnlineUserManager() {
        this.userMap = new ConcurrentHashMap<>();
    }

    /**
     * 添加在线用户
     */
    public void addUser(String username, ObjectOutputStream oos) {
        userMap.put(username, oos);
    }

    /**
     * 移除在线用户
     */
    public void removeUser(String username) {
        userMap.remove(username);
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(String username) {
        return userMap.containsKey(username);
    }

    /**
     * 获取用户的输出流
     */
    public ObjectOutputStream getUserOutputStream(String username) {
        return userMap.get(username);
    }

    /**
     * 获取所有在线用户名
     */
    public List<String> getOnlineUsers() {
        return new ArrayList<>(userMap.keySet());
    }

    /**
     * 获取所有在线用户的输出流
     */
    public List<ObjectOutputStream> getAllOutputStreams() {
        return new ArrayList<>(userMap.values());
    }

    /**
     * 获取在线用户数量
     */
    public int getOnlineUserCount() {
        return userMap.size();
    }

    /**
     * 发送消息给指定用户
     */
    public void sendMessageToUser(String username, Object message) throws IOException {
        ObjectOutputStream oos = userMap.get(username);
        if (oos != null) {
            oos.writeObject(message);
            oos.flush();
        }
    }
}
