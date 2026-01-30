package server;

import server.broadcast.BroadcastService;
import server.handlers.ClientHandler;
import server.handlers.MessageHandler;
import server.managers.GroupManager;
import server.managers.OnlineUserManager;
import server.managers.UserManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 聊天室服务端
 */
public class ChatServer {
    private static final int PORT = 8888;

    private final UserManager userManager;
    private final OnlineUserManager onlineUserManager;
    private final GroupManager groupManager;
    private final BroadcastService broadcastService;
    private final MessageHandler messageHandler;

    public ChatServer() {
        this.userManager = new UserManager();
        this.onlineUserManager = new OnlineUserManager();
        this.groupManager = new GroupManager();
        this.broadcastService = new BroadcastService(onlineUserManager, groupManager);
        this.messageHandler = new MessageHandler(onlineUserManager, groupManager, broadcastService);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("聊天室服务端已启动，端口：" + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("新客户端连接：" + clientSocket);

                ClientHandler clientHandler = new ClientHandler(
                        clientSocket,
                        userManager,
                        onlineUserManager,
                        groupManager,
                        broadcastService,
                        messageHandler
                );
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }
}
