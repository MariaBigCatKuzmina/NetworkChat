package ru.kuzmina.client.model;

import ru.kuzmina.clientserver.Command;

public interface ReadCommandListener {
    void processReceivedCommand(Command command);
}
