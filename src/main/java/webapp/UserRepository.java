package webapp;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserRepository {
    Connection DBConn;

    @Autowired
    public UserRepository(String[] databaseParameters){
        try {
            DBConn = DriverManager.getConnection(databaseParameters[0], databaseParameters[1], databaseParameters[2]);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<User> allUsers() throws SQLException {
        ResultSet resultSet;
        Statement statement = DBConn.createStatement();
        List<User> users = new ArrayList<>();
        String selectQuery = "SELECT USERNAME,PASSWORD,AVATAR,BALANCE\n" +
                "FROM users";
        resultSet = statement.executeQuery(selectQuery);
        while(resultSet.next()){
            User u = new User();
            u.setUsername(resultSet.getString(1));
            u.setPassword(resultSet.getBytes(2));
            u.setAvatar(resultSet.getString(3));
            u.setBalance(resultSet.getInt(4));
            users.add(u);
        }
        return users;
    }

    public void addUser(User u) throws SQLException {
        for(User a : allUsers()){
            if(a.getUsername().equals(u.getUsername())) throw new RuntimeException("Username in use");
        }
        Statement statement = DBConn.createStatement();
        String insertQuery = "INSERT INTO `brampelpoker`.`users`\n(`USERNAME`,`PASSWORD`,`BALANCE`)\n" +
                             "VALUES\n" +
                             String.format("('%s','%s',%d);",u.getUsername(),new String(u.getPassword()),u.getBalance());
    }

    public void editUser(User u) throws SQLException {
        Statement statement = DBConn.createStatement();
        String insertQuery = "UPDATE `users`" +
                String.format("\nSET `BALANCE` = %d\n", u.getBalance()) +
                String.format("WHERE `USERNAME` = '%s' ", u.getUsername());
        statement.executeUpdate(insertQuery);
    }

    public User getUser(String username){
        try {
            for(User a : allUsers()){
                if(a.getUsername().equals(username)) return a;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean login(String username, String password){
        User u = getUser(username);
        if(u == null) return false;
        return BCrypt.checkpw(password, new String(u.getPassword()));
    }

    public boolean register(String username, String password){
        User u = new User(username,password);
        u.setBalance(250);
        try {
            addUser(u);
            return true;
        } catch(Exception e){
            System.out.println(e.getMessage());
            return false;
        }
    }

    public int getBalance(String username){
        User u = getUser(username);
        if(u == null) return -1;
        return u.getBalance();
    }

    public void withdraw(String username, int amount){
        User u = getUser(username);
        if(u == null || u.getBalance() < amount) return;
        u.setBalance(u.getBalance()-amount);
        try {
            editUser(u);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deposit(String username, int amount){
        User u = getUser(username);
        if(u == null) return;
        u.setBalance(u.getBalance()+amount);
        try {
            editUser(u);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
