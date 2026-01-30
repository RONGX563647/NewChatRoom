package common;

import java.util.ArrayList;
import java.util.List;

/**
 * 群聊实体类：封装群ID、群名称、成员列表
 */
public class Group implements java.io.Serializable {
    private String groupId;      // 唯一群ID（UUID生成）
    private String groupName;    // 群名称
    private List<String> members; // 群成员列表

    public Group(String groupId, String groupName) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.members = new ArrayList<>();
    }

    // Getter & Setter
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public List<String> getMembers() { return members; }
    public void setMembers(List<String> members) { this.members = members; }

    // 新增：添加群成员
    public void addMember(String username) {
        if (!members.contains(username)) {
            members.add(username);
        }
    }

    // 新增：移除群成员
    public void removeMember(String username) {
        members.remove(username);
    }
}
