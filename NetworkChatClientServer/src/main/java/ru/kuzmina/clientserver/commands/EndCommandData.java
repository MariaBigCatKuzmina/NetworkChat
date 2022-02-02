package ru.kuzmina.clientserver.commands;

import java.io.Serializable;

public class EndCommandData implements Serializable {
    private final String userName;

    public EndCommandData(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }
}
