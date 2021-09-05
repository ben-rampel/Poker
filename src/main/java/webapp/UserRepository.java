package webapp;

import org.mindrot.jbcrypt.BCrypt;
import org.omg.CORBA.DynAnyPackage.Invalid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.InvalidNameException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("SqlNoDataSourceInspection")
@Service
public class UserRepository {
    Connection DBConn;

    @Autowired
    public UserRepository(String[] databaseParameters) {
        try {
            DBConn = DriverManager.getConnection(databaseParameters[0], databaseParameters[1], databaseParameters[2]);
            Statement statement = DBConn.createStatement();
            String buildDatabase =
                    "CREATE TABLE IF NOT EXISTS users (" +
                            "username VARCHAR(20) NOT NULL," +
                            "password VARCHAR(255) NOT NULL, " +
                            "avatar VARCHAR(200), " +
                            "balance INT NOT NULL" +
                            ");";
            statement.executeUpdate(buildDatabase);
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
        while (resultSet.next()) {
            try {
                User u = new User();
                u.setUsername(resultSet.getString(1));
                u.setPassword(resultSet.getBytes(2));
                u.setAvatar(resultSet.getString(3));
                u.setBalance(resultSet.getInt(4));
                users.add(u);
            } catch (InvalidNameException e) {
                System.out.println("Attempted to load invalid name from database.");
            }
        }
        return users;
    }

    public void addUser(User u) throws SQLException, InvalidNameException {
        for (User a : allUsers()) {
            if (a.getUsername().equals(u.getUsername())) throw new InvalidNameException("Username in use");
        }
        Statement statement = DBConn.createStatement();
        String insertQuery = "INSERT INTO `users`\n(`USERNAME`,`PASSWORD`,`BALANCE`)\n" +
                "VALUES\n" +
                String.format("('%s','%s',%d);", u.getUsername(), new String(u.getPassword()), u.getBalance());
        statement.executeUpdate(insertQuery);
    }

    public void editUser(User u) throws SQLException {
        Statement statement = DBConn.createStatement();
        String insertQuery = "UPDATE `users`" +
                String.format("\nSET `BALANCE` = %d\n", u.getBalance()) +
                String.format("WHERE `USERNAME` = '%s' ", u.getUsername());
        statement.executeUpdate(insertQuery);
    }

    public User getUser(String username) {
        try {
            for (User a : allUsers()) {
                if (a.getUsername().equals(username)) return a;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean login(String username, String password) {
        User u = getUser(username);
        if (u == null) return false;
        return BCrypt.checkpw(password, new String(u.getPassword()));
    }

    public User register(String username, String password) throws InvalidNameException {
        try {
            User u = new User(username, password);
            u.setBalance(3500);
            addUser(u);
            return u;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public int getBalance(String username) {
        User u = getUser(username);
        if (u == null) return -1;
        return u.getBalance();
    }

    public void withdraw(String username, int amount) {
        User u = getUser(username);
        if (u == null || u.getBalance() < amount) return;
        u.setBalance(u.getBalance() - amount);
        try {
            editUser(u);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deposit(String username, int amount) {
        User u = getUser(username);
        if (u == null) return;
        u.setBalance(u.getBalance() + amount);
        try {
            editUser(u);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
