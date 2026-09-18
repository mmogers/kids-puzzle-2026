package lv.marmog.androidpuzzlegame.model;


public class User {
    private int usernameId;
    private String username;

    public User() {
    }

    @Override
    public String toString() {
        return this.username;
    }

    public int getUsernameId() {
        return this.usernameId;
    }

    public void setUsernameId(int usernameId) {
        this.usernameId = usernameId;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
