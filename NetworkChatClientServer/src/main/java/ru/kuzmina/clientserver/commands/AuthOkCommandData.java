package ru.kuzmina.clientserver.commands;

import java.io.Serializable;

public class AuthOkCommandData implements Serializable {
    public AuthOkCommandData(String userName) {
        this.userName = userName;
    }

    private final String userName;

    public String getUserName() {
        return userName;
    }
}
