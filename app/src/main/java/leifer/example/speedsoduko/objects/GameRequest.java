package leifer.example.speedsoduko.objects;

public class GameRequest {
    private User userSender;
    private User userReceived;
    private String key;
    private boolean accepted;

    public GameRequest() {
    }

    public GameRequest(User userSender,User userReceived) {
        this.userSender = userSender;
        this.userReceived = userReceived;
        this.key = userSender.getUserName()+":>:"+userReceived.getUserName();
        this.accepted = false;
    }

    public User getUserSender() {
        return userSender;
    }

    public void setUserSender(User userSender) {
        this.userSender = userSender;
    }

    public User getUserReceived() {
        return userReceived;
    }

    public void setUserReceived(User userReceived) {
        this.userReceived = userReceived;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
}
