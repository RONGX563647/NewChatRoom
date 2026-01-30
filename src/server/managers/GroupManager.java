package server.managers;

import common.Group;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 群组管理类
 * 负责群组的创建、查找、成员管理等功能
 */
public class GroupManager {
    private final Map<String, Group> groupMap;

    public GroupManager() {
        this.groupMap = new ConcurrentHashMap<>();
        initializeDefaultGroup();
    }

    /**
     * 初始化默认群组
     */
    private void initializeDefaultGroup() {
        Group defaultGroup = new Group(UUID.randomUUID().toString(), "默认群");
        groupMap.put(defaultGroup.getGroupId(), defaultGroup);
    }

    /**
     * 创建新群组
     */
    public Group createGroup(String groupName) {
        Group group = new Group(UUID.randomUUID().toString(), groupName);
        groupMap.put(group.getGroupId(), group);
        return group;
    }

    /**
     * 根据群ID获取群组
     */
    public Group getGroupById(String groupId) {
        return groupMap.get(groupId);
    }

    /**
     * 根据群名获取群组
     */
    public Group getGroupByName(String groupName) {
        return groupMap.values().stream()
                .filter(g -> g.getGroupName().equals(groupName))
                .findFirst()
                .orElse(null);
    }

    /**
     * 搜索包含关键字的群组
     */
    public List<Group> searchGroups(String keyword) {
        List<Group> result = new ArrayList<>();
        for (Group group : groupMap.values()) {
            if (group.getGroupName().contains(keyword)) {
                result.add(group);
            }
        }
        return result;
    }

    /**
     * 获取所有群组
     */
    public List<Group> getAllGroups() {
        return new ArrayList<>(groupMap.values());
    }

    /**
     * 用户加入群组
     */
    public boolean joinGroup(String groupId, String username) {
        Group group = groupMap.get(groupId);
        if (group != null) {
            group.addMember(username);
            return true;
        }
        return false;
    }

    /**
     * 用户离开群组
     */
    public void leaveGroup(String groupId, String username) {
        Group group = groupMap.get(groupId);
        if (group != null) {
            group.removeMember(username);
        }
    }

    /**
     * 从所有群组中移除用户
     */
    public void removeUserFromAllGroups(String username) {
        for (Group group : groupMap.values()) {
            group.removeMember(username);
        }
    }

    /**
     * 获取群组的所有成员
     */
    public List<String> getGroupMembers(String groupId) {
        Group group = groupMap.get(groupId);
        return group != null ? group.getMembers() : new ArrayList<>();
    }

    /**
     * 获取默认群组
     */
    public Group getDefaultGroup() {
        return groupMap.values().stream()
                .filter(g -> g.getGroupName().equals("默认群"))
                .findFirst()
                .orElse(null);
    }
}
