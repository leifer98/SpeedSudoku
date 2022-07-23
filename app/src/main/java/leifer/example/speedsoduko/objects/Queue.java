package leifer.example.speedsoduko.objects;

import java.io.Serializable;

public class Queue implements Serializable {
    private String key;
    private User user;

    public Queue() {
    }

    public Queue(User user) {
        this.key = null;
        this.user = user;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Queue that = (Queue) o;

        return user.equals(that.user);
    }

    @Override
    public int hashCode() {
        return user.hashCode();
    }
}
