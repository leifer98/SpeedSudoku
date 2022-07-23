package leifer.example.speedsoduko.objects;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private String userID, userKey, userName, userEmail;
    private int userMMR;
    private boolean userManager;
    private GameRecord lastGameRecord;
    private List<Friend> friends = new ArrayList<Friend>();

    public User() {}

    public User(String userID,String userKey,String userName,String userEmail,int userMMR,boolean userManager) {
        this.userID = userID;
        this.userKey = userKey;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userMMR = userMMR;
        this.userManager = userManager;
        this.lastGameRecord = null;
        this.friends = null;
    }


    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public int getUserMMR() {
        return userMMR;
    }

    public void setUserMMR(int userMMR) {
        this.userMMR = userMMR;
    }

    public boolean isUserManager() {
        return userManager;
    }

    public void setUserManager(boolean userManager) {
        this.userManager = userManager;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public GameRecord getLastGameRecord() {
        return lastGameRecord;
    }

    public void setLastGameRecord(GameRecord lastGameRecord) {
        this.lastGameRecord = lastGameRecord;
    }

    public List<Friend> getFriends() {
        return friends;
    }

    public void setFriends(List<Friend> friends) {
        this.friends = friends;
    }

    public void addFriend(Friend friend) {
        this.friends.add(friend);
    }

    public void removeFriend(Friend friend) {
        this.friends.remove(friend);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (!userKey.equals(user.userKey)) return false;
        return userEmail.equals(user.userEmail);
    }

    public List<List<Integer>> convertToList(int[][] oldList) {
        List<List<Integer>> list = new ArrayList<List<Integer>>(9);

        for (int i = 0; i < 9; i++) {
            List<Integer> temp = new ArrayList<Integer>(9);
            for (int j = 0; j < 9; j++) {
                temp.add(j,oldList[i][j]);
            }
            list.add(i,temp);
        }

        return list;
    }

    public int[][] convertToMatrices(List<List<Integer>> oldList) {
        int[][] list = new int[9][9];

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                list[i][j] = oldList.get(i).get(j);
            }
        }

        return list;
    }
}
