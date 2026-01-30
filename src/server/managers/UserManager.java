package server.managers;

import common.Message;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户认证管理类
 * 负责用户注册、登录验证、密码重置等功能
 */
public class UserManager {
    private final Map<String, String> userAuthMap;

    public UserManager() {
        this.userAuthMap = new ConcurrentHashMap<>();
    }

    /**
     * 检查账号是否存在
     */
    public boolean accountExists(String account) {
        return userAuthMap.containsKey(account);
    }

    /**
     * 验证密码是否正确
     */
    public boolean verifyPassword(String account, String password) {
        String storedPassword = userAuthMap.get(account);
        return storedPassword != null && storedPassword.equals(password);
    }

    /**
     * 注册新用户
     */
    public boolean registerUser(String account, String password) {
        if (accountExists(account)) {
            return false;
        }
        userAuthMap.put(account, password);
        return true;
    }

    /**
     * 重置密码
     */
    public boolean resetPassword(String account, String newPassword) {
        if (!accountExists(account)) {
            return false;
        }
        userAuthMap.put(account, newPassword);
        return true;
    }

    /**
     * 处理注册请求
     */
    public void handleRegister(Message message, ObjectOutputStream oos) throws IOException {
        String account = message.getSender();
        String password = message.getPassword();

        if (account == null || account.isEmpty() || password == null || password.isEmpty()) {
            Message response = new Message(Message.Type.REGISTER_RESPONSE, "服务器", account, "账号或密码不能为空！");
            oos.writeObject(response);
            oos.flush();
            return;
        }

        if (registerUser(account, password)) {
            Message response = new Message(Message.Type.REGISTER_RESPONSE, "服务器", account, "注册成功！请返回登录");
            oos.writeObject(response);
            oos.flush();
            System.out.println("用户 " + account + " 注册成功");
        } else {
            Message response = new Message(Message.Type.REGISTER_RESPONSE, "服务器", account, "账号已存在，请更换！");
            oos.writeObject(response);
            oos.flush();
        }
    }

    /**
     * 处理找回密码请求
     */
    public void handleFindPassword(Message message, ObjectOutputStream oos) throws IOException {
        String account = message.getSender();

        if (account == null || account.isEmpty()) {
            Message response = new Message(Message.Type.FIND_PASSWORD_RESPONSE, "服务器", account, "账号不能为空！");
            oos.writeObject(response);
            oos.flush();
            return;
        }

        if (accountExists(account)) {
            Message response = new Message(Message.Type.FIND_PASSWORD_RESPONSE, "服务器", account, "账号验证通过！请输入新密码");
            oos.writeObject(response);
            oos.flush();
        } else {
            Message response = new Message(Message.Type.FIND_PASSWORD_RESPONSE, "服务器", account, "账号不存在，请检查！");
            oos.writeObject(response);
            oos.flush();
        }
    }

    /**
     * 处理重置密码请求
     */
    public void handleResetPassword(Message message, ObjectOutputStream oos) throws IOException {
        String account = message.getSender();
        String newPassword = message.getPassword();

        if (account == null || account.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            Message response = new Message(Message.Type.RESET_PASSWORD_RESPONSE, "服务器", account, "账号或新密码不能为空！");
            oos.writeObject(response);
            oos.flush();
            return;
        }

        if (resetPassword(account, newPassword)) {
            Message response = new Message(Message.Type.RESET_PASSWORD_RESPONSE, "服务器", account, "密码重置成功！请使用新密码登录");
            oos.writeObject(response);
            oos.flush();
            System.out.println("用户 " + account + " 密码重置成功");
        } else {
            Message response = new Message(Message.Type.RESET_PASSWORD_RESPONSE, "服务器", account, "账号不存在，重置失败！");
            oos.writeObject(response);
            oos.flush();
        }
    }
}
