package ru.kuzmina.server.chat.auth;

import java.sql.*;
import java.util.Set;

public class AuthService {
    public static final String CONNECTION_STRING = "jdbc:sqlite:/Users/mariakuzmina/Documents/GBCourses/Java/NetworkChat/NetworkChatServer/src/main/java/ru/kuzmina/server/userdb/NetworkChatDB.db";

    private static Connection connection;
    private static Statement statement;

    private static void connectDB() throws SQLException {
        connection = DriverManager.getConnection(CONNECTION_STRING);
        statement = connection.createStatement();
    }

    private static void closeConnection() {
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
            connectDB();
            userName = getUserNameFromDB(login, password);
        } catch (SQLException e) {
            System.err.println("Failed to connect to Users database");
            e.printStackTrace();
        } finally {
            closeConnection();
        }
        return userName;
    }

    private static String getUserNameFromDB(String login, String password) throws SQLException {
        String sqlText = String.format("SELECT username FROM Users WHERE login LIKE '%s' AND password LIKE '%s'", login, password);
        ResultSet resultSet = statement.executeQuery(sqlText);
        if (resultSet.next()){
            return resultSet.getString("username");
        }
        return null;
    }

}
