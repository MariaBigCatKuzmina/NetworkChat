package ru.kuzmina.server.chat.auth;

import java.sql.*;

public class AuthService {
    public static final String CONNECTION_STRING = "jdbc:sqlite:NetworkChatDB.db";

    private Connection connection;
    private Statement statement;

    private void connect() throws SQLException {
        connection = DriverManager.getConnection(CONNECTION_STRING);
        statement = connection.createStatement();
    }

    private void disconnect() {
        try {
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Failed to close connection to User database");
            e.printStackTrace();
        }
    }

    public String getUserNameByLoginAndPassword(String login, String password) {
        String userName = null;
        try {
            connect();
            userName = getUserNameFromDB(login, password);
        } catch (SQLException e) {
            System.err.println("Failed to connect to Users database");
            e.printStackTrace();
        } finally {
            disconnect();
        }
        return userName;
    }

    private String getUserNameFromDB(String login, String password) throws SQLException {
        String sqlText = String.format("SELECT username FROM Users WHERE login LIKE '%s' AND password LIKE '%s'", login, password);
        ResultSet resultSet = statement.executeQuery(sqlText);
        if (resultSet.next()) {
            return resultSet.getString("username");
        }
        return null;
    }

    public int updateUserName(String oldUserName, String newUserName, String password){
        String qslText = String.format("UPDATE Users SET UserName = '%s' WHERE username LIKE '%s' AND password LIKE '%s'", newUserName, oldUserName, password);
        try {
            connect();
            return statement.executeUpdate(qslText);
        } catch (SQLException e) {
            System.err.println("Failed to connect to Users database");
            e.printStackTrace();
        } finally {
            disconnect();
        }
        return 0;
    }


}
