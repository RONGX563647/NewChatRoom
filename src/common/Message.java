package common;
import java.io.Serializable;
import java.util.List;

/**
 * 消息实体类，用于客户端和服务端之间的通信
 */
public class Message implements Serializable {

    // 消息类型：登录、私聊、群聊、上线通知、下线通知、在线用户列表
    public enum Type {
        LOGIN, PRIVATE_CHAT, GROUP_CHAT, ONLINE_NOTIFY, OFFLINE_NOTIFY,
        GET_ONLINE_USERS, ONLINE_USERS, FILE_PRIVATE, FILE_GROUP, SHAKE,
        CREATE_GROUP, SEARCH_GROUP, JOIN_GROUP, GROUP_LIST,
        REGISTER, REGISTER_RESPONSE,
        FIND_PASSWORD, FIND_PASSWORD_RESPONSE,
        RESET_PASSWORD, RESET_PASSWORD_RESPONSE
    }

    private Type type;        // 消息类型
    private String sender;    // 发送者、用户名
    private String receiver;  // 接收者（私聊用户/群聊组名）
    private String content;   // 消息内容
    private List<String> onlineUsers;

    // 新增：文件传输相关字段
    private String fileName;  // 文件名
    private long fileSize;    // 文件大小（字节）
    private byte[] fileData;  // 文件字节数组

    // 新增：群聊相关字段
    private String groupId;   // 群ID
    private String groupName; // 群名称
    private List<Group> groupList; // 群列表（用于服务器推送）

    private String password;  // 新增：密码字段

    // 构造函数
    public Message(Type type, String sender, String receiver, String content) {
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
    }

    /**
     * 列表类消息构造函数（在线用户列表/群列表）
     * 通用列表构造：通过setter区分是onlineUsers还是groupList
     */
    public Message(Type type, String sender) {
        this.type = type;
        this.sender = sender;
        this.content = "";
    }

    // 新增：文件消息构造函数
    public Message(Type type, String sender, String receiver, String fileName, long fileSize, byte[] fileData) {
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileData = fileData;
    }

    // 新增：抖动消息构造函数（无需内容，仅需类型+发送者+接收者）
    public Message(Type type, String sender, String receiver) {
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.content = "";
    }

    // Getter 和 Setter
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<String> getOnlineUsers() { return onlineUsers; }
    public void setOnlineUsers(List<String> onlineUsers) { this.onlineUsers = onlineUsers; }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public long getFileSize() {
        return fileSize;
    }
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    public byte[] getFileData() {
        return fileData;
    }
    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }
    public String getGroupId() {
        return groupId;
    }
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    public String getGroupName() {
        return groupName;
    }
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    public List<Group> getGroupList() {
        return groupList;
    }
    public void setGroupList(List<Group> groupList) {
        this.groupList = groupList;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}