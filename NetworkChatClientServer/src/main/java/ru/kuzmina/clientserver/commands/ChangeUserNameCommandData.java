package ru.kuzmina.clientserver.commands;

import java.io.Serializable;

public class ChangeUserNameCommandData implements Serializable {
    private final String oldUserName;
    private final String newUserName;
    private final String password;

    public ChangeUserNameCommandData(String oldUserName, String newUserName, String password) {
        this.oldUserName = oldUserName;
        this.newUserName = newUserName;
        this.password = password;
    }

    public String getOldUserName() {
        return oldUserName;
    }

    public String getNewUserName() {
        return newUserName;
    }

    public String getPassword() {
        return password;
    }
}
