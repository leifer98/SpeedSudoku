package leifer.example.speedsoduko.objects;

import java.io.Serializable;

public class Friend implements Serializable {
    private String friendName, friendKey, friendStatus;
    private int friendMMR;

    public Friend() {
    }

    public Friend(String friendName,String friendKey,String friendStatus,int friendMMR) {
        this.friendName = friendName;
        this.friendKey = friendKey;
        this.friendStatus = friendStatus;
        this.friendMMR = friendMMR;
    }

    public Friend(User friend,String friendStatus) {
        this.friendName = friend.getUserName();
        this.friendKey = friend.getUserKey();
        this.friendStatus = friendStatus;
        this.friendMMR = friend.getUserMMR();
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public String getFriendKey() {
        return friendKey;
    }

    public void setFriendKey(String friendKey) {
        this.friendKey = friendKey;
    }

    public String getFriendStatus() {
        return friendStatus;
    }

    public void setFriendStatus(String friendStatus) {
        this.friendStatus = friendStatus;
    }

    public int getFriendMMR() {
        return friendMMR;
    }

    public void setFriendMMR(int friendMMR) {
        this.friendMMR = friendMMR;
    }

}
