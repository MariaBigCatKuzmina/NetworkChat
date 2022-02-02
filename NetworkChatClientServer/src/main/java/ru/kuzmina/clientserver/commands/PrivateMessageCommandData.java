package ru.kuzmina.clientserver.commands;

import java.io.Serializable;

public class PrivateMessageCommandData implements Serializable {

    private final String reciever;
    private final String message;

    public PrivateMessageCommandData(String reciever, String message) {
        this.reciever = reciever;
        this.message = message;
    }

    public String getReciever() {
        return reciever;
    }

    public String getMessage() {
        return message;
    }
}
