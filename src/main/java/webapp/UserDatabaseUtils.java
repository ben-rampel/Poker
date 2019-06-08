package webapp;

import static webapp.constants.*;

import java.sql.*;

public class UserDatabaseUtils {

    public static boolean login(String username, String password) {
        boolean goodLogin = false;

        ResultSet resultSet;
        try (Connection connection = DriverManager.getConnection(connectionUrl);) {
            Statement statement = connection.createStatement();
            String loginQuery = "DECLARE\t@responseMessage nvarchar(250)\n" +
                    "\n" +
                    "EXEC\tdbo.uspLogin\n" +
                    "\t\t@pLoginName = N'" + username + "',\n" +
                    "\t\t@pPassword = N'" + password + "',\n" +
                    "\t\t@responseMessage = @responseMessage OUTPUT\n" +
                    "\n" +
                    "SELECT\t@responseMessage as N'@responseMessage'\n";

            resultSet = statement.executeQuery(loginQuery);
            resultSet.next();
            if (resultSet.getString("@responseMessage").equals("SUCCESS")) {
                goodLogin = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return goodLogin;
    }

    public static void register(String username, String password, String discordID) {
        try (Connection connection = DriverManager.getConnection(connectionUrl);) {
            Statement statement = connection.createStatement();
            String registerQuery = "DECLARE @responseMessage NVARCHAR(250)\n" +
                    "\n" +
                    "EXEC dbo.uspAddUser\n" +
                    "          @pLogin = N'" + username + "',\n" +
                    "          @pPassword = N'" + password + "',\n" +
                    "          @pDiscordUUID = N'" + discordID + "',\n" +
                    "          @pChips = N'250',\n" +
                    "          @responseMessage=@responseMessage OUTPUT\n";
            statement.executeQuery(registerQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void withdraw(String username, int amount) {
        try (Connection connection = DriverManager.getConnection(connectionUrl);) {
            Statement statement = connection.createStatement();
            String withdrawQuery =
                    "UPDATE [dbo].[user]" + "\n" +
                            "SET [chips] = [chips] - " + amount + "\n" +
                            "WHERE [name] = '" + username + "';";
            statement.executeUpdate(withdrawQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deposit(String username, int amount) {
        try (Connection connection = DriverManager.getConnection(connectionUrl);) {
            Statement statement = connection.createStatement();
            String depositQuery =
                    "UPDATE [dbo].[user]" + "\n" +
                            "SET [chips] = [chips] + " + amount + "\n" +
                            "WHERE [name] = '" + username + "';";
            statement.executeUpdate(depositQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getBalance(String username) {
        int balance = -1;
        try (Connection connection = DriverManager.getConnection(connectionUrl);) {
            Statement statement = connection.createStatement();
            String query = "SELECT TOP 1 [chips] FROM [dbo].[user] WHERE [name] = '" + username + "'";
            ResultSet result = statement.executeQuery(query);
            result.next();
            balance = result.getInt("chips");
        } catch (SQLException e) {
            String message = e.getMessage();
            if (message.equals("The result set has no current row.")) {
                throw new IllegalArgumentException();
            } else {
                e.printStackTrace();
            }
        }
        return balance;
    }

    public static void main(String[] args) {
        deposit("Ben", 3000);
        deposit("toyotasupra2002", 3000);
    }

}
