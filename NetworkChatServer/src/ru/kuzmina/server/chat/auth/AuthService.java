package ru.kuzmina.server.chat.auth;

import java.util.Set;

public class AuthService {
    private static Set<User> USER = Set.of(
            new User("login1","pass1", "user1"),
            new User("login2","pass2", "user2"),
            new User("login3","pass3", "user3")
    );

    public String getUserNameByLoginAndPassword(String login, String password){
        User requiredUser = new User(login, password);
        for (User user : USER) {
            if (requiredUser.equals(user)) {
                return user.getUserName();
            }
        }
        return null;
    }




}
