package leifer.example.speedsoduko.objects;

import java.time.LocalDateTime;

public class Message {
    private String message;
    private User userFrom, userTo;
    private LocalDateTime timeSent = LocalDateTime.now();

    public Message() {    }

    public Message(String message,User userFrom,User userTo) {
        this.message = message;
        this.userFrom = userFrom;
        this.userTo = userTo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getUserFrom() {
        return userFrom;
    }

    public void setUserFrom(User userFrom) {
        this.userFrom = userFrom;
    }

    public User getUserTo() {
        return userTo;
    }

    public void setUserTo(User userTo) {
        this.userTo = userTo;
    }

    public LocalDateTime getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(LocalDateTime timeSent) {
        this.timeSent = timeSent;
    }
}
