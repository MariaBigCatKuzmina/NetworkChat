package ru.kuzmina.client.controllers;


import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.kuzmina.client.ClientChat;
import ru.kuzmina.client.model.Network;
import ru.kuzmina.client.model.ReadCommandListener;
import ru.kuzmina.clientserver.Command;
import ru.kuzmina.clientserver.CommandType;
import ru.kuzmina.clientserver.commands.ClientMessageCommandData;
import ru.kuzmina.clientserver.commands.UpdateUserListCommandData;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class ClientController {


    private ClientChat application;

    @FXML
    public ListView<String> userList;
    @FXML
    public TextArea chatArea;
    @FXML
    private TextField messageField;
    @FXML
    private Button sendButton;


    public void sendMessage() {

        String message = messageField.getText().trim();
        if (message.isEmpty()) {
            messageField.clear();
            return;
        }

        String recipient = null;
        if (!userList.getSelectionModel().isEmpty()) {
            recipient = userList.getSelectionModel().getSelectedItem();
        }
        try {
            if (recipient != null) {
                Network.getInstance().sendPrivateMessage(recipient, message);
            } else {
                Network.getInstance().sendMessage(message);
            }

        } catch (IOException e) {
            application.showErrorDialog("Ошибка передачи данных по сети");
            e.printStackTrace();
        }
        appendMessageToChat("Я", recipient, message);
    }

    private void appendMessageToChat(String sender, String recipient, String message) {
        chatArea.appendText(DateFormat.getDateTimeInstance().format(new Date()));
        chatArea.appendText(System.lineSeparator());

        String messageHeader;
        messageHeader = sender != null ? sender : "no sender";
        messageHeader = recipient != null && sender != null ? messageHeader + " -> " + recipient : messageHeader + ": ";

        chatArea.appendText(messageHeader);
        chatArea.appendText(System.lineSeparator());
        chatArea.appendText(message);
        chatArea.appendText(System.lineSeparator());
        chatArea.appendText(System.lineSeparator());
        messageField.setFocusTraversable(true);
        messageField.clear();
    }

    public void onClose() {
        ((Stage) sendButton.getScene().getWindow()).close();
    }

//    public void setApplication(ClientChat application) {
//        this.application = application;
//    }

    public void initializeMessageHandler() {
        Network.getInstance().addReadMessageListener(new ReadCommandListener() {
            @Override
            public void processReceivedCommand(Command command) {
                if (command.getType() == CommandType.CLIENT_MESSAGE) {
                    ClientMessageCommandData data = (ClientMessageCommandData) command.getData();
                    appendMessageToChat(data.getSender(), null, data.getMessage());
                } else if (command.getType() == CommandType.UPDATE_USER_LIST) {
                    UpdateUserListCommandData data = (UpdateUserListCommandData) command.getData();
                    Platform.runLater(() -> {
                        userList.setItems(FXCollections.observableList(data.getUsers()));
                    });
                }
            }
        });
    }
}