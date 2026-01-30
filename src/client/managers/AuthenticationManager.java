package client.managers;

import client.ChatClient;
import common.Message;

import javax.swing.*;

/**
 * 认证管理类
 * 负责处理登录、注册、找回密码等认证相关功能
 */
public class AuthenticationManager {
    private final ChatClient chatClient;

    public AuthenticationManager(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 登录
     */
    public void login(String account, String password) {
        try {
            if (!chatClient.getNetworkManager().connectToServer()) {
                return;
            }

            Message loginMsg = new Message(Message.Type.LOGIN, account, "");
            loginMsg.setPassword(password);
            chatClient.getNetworkManager().sendMessage(loginMsg);

            Message response = chatClient.getNetworkManager().receiveMessage();
            if (response.getContent().contains("成功")) {
                chatClient.setUsername(account);
                chatClient.getLoginRegisterFrame().dispose();
                chatClient.initChatUI();
                chatClient.setVisible(true);
                chatClient.getChatArea().append("【系统消息】" + response.getContent() + "\n");

                Message getUsersMsg = new Message(Message.Type.GET_ONLINE_USERS, account, "", "");
                chatClient.getNetworkManager().sendMessage(getUsersMsg);

                chatClient.startMessageListener();
            } else {
                JOptionPane.showMessageDialog(chatClient.getLoginRegisterFrame(), response.getContent());
                chatClient.getLoginPwdField().setText("");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(chatClient.getLoginRegisterFrame(), "登录失败：" + e.getMessage());
            e.printStackTrace();
            chatClient.resetSocket();
            System.exit(1);
        }
    }

    /**
     * 注册
     */
    public void register(String account, String password) {
        try {
            if (!chatClient.getNetworkManager().connectToServer()) {
                JOptionPane.showMessageDialog(chatClient.getLoginRegisterFrame(), "连接服务器失败！");
                return;
            }

            Message registerMsg = new Message(Message.Type.REGISTER, account, "");
            registerMsg.setPassword(password);
            chatClient.getNetworkManager().sendMessage(registerMsg);

            Message response = chatClient.getNetworkManager().receiveMessage();
            JOptionPane.showMessageDialog(chatClient.getLoginRegisterFrame(), response.getContent());

            if (response.getContent().contains("成功")) {
                chatClient.getRegisterAccountField().setText("");
                chatClient.getRegisterPwdField().setText("");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(chatClient.getLoginRegisterFrame(), "注册失败：" + e.getMessage());
            chatClient.resetSocket();
            e.printStackTrace();
        }
    }

    /**
     * 找回密码
     */
    public void findPassword(String account) {
        try {
            if (!chatClient.getNetworkManager().connectToServer()) {
                JOptionPane.showMessageDialog(chatClient.getLoginRegisterFrame(), "连接服务器失败！");
                return;
            }

            Message findMsg = new Message(Message.Type.FIND_PASSWORD, account, "");
            chatClient.getNetworkManager().sendMessage(findMsg);

            Message response = chatClient.getNetworkManager().receiveMessage();
            JOptionPane.showMessageDialog(chatClient.getLoginRegisterFrame(), response.getContent());

            if (response.getContent().contains("验证通过")) {
                resetPassword(account);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(chatClient.getLoginRegisterFrame(), "验证账号失败：" + ex.getMessage());
            chatClient.resetSocket();
            ex.printStackTrace();
        }
    }

    /**
     * 重置密码
     */
    public void resetPassword(String account) {
        JPasswordField newPwdField = new JPasswordField();
        int result = JOptionPane.showConfirmDialog(
                chatClient.getLoginRegisterFrame(),
                new Object[]{"请输入新密码：", newPwdField},
                "重置密码",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            String newPassword = new String(newPwdField.getPassword()).trim();
            if (newPassword.isEmpty()) {
                JOptionPane.showMessageDialog(chatClient.getLoginRegisterFrame(), "新密码不能为空！");
                return;
            }

            try {
                Message resetMsg = new Message(Message.Type.RESET_PASSWORD, account, "");
                resetMsg.setPassword(newPassword);
                chatClient.getNetworkManager().sendMessage(resetMsg);

                Message response = chatClient.getNetworkManager().receiveMessage();
                JOptionPane.showMessageDialog(chatClient.getLoginRegisterFrame(), response.getContent());

                if (response.getContent().contains("成功")) {
                    chatClient.getLoginPwdField().setText("");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(chatClient.getLoginRegisterFrame(), "重置密码失败：" + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}
