package webapp;

import org.mindrot.jbcrypt.BCrypt;

public class User {
    private String username;
    private byte[] password;
    private String avatar;
    private int balance;

    public User(String username, String unhashedPassword) {
        this.username = username;
        this.password = BCrypt.hashpw(unhashedPassword, BCrypt.gensalt()).getBytes();
    }

    public User() {

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public byte[] getPassword() {
        return password;
    }

    public void setPasswordAndHash(String unhashedPassword) {
        this.password = BCrypt.hashpw(unhashedPassword, BCrypt.gensalt()).getBytes();
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }
}
