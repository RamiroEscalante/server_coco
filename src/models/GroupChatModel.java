package Models;

public class GroupChatModel {
    int groupId;
    int chatterId;
    String groupName;

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getChatterId() {
        return chatterId;
    }

    public void setChatterId(int chatterId) {
        this.chatterId = chatterId;
    }
    
    public String getGroupName() {
        return groupName;
    }

    public void getGroupName(String groupName) {
        this.groupName = groupName;
    }
}
