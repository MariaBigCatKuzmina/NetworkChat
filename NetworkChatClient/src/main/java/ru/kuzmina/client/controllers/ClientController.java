package ru.kuzmina.client.controllers;


import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import ru.kuzmina.client.ClientChat;
import ru.kuzmina.client.dialogs.Dialogs;
import ru.kuzmina.client.model.Network;
import ru.kuzmina.client.model.ReadCommandListener;
import ru.kuzmina.clientserver.Command;
import ru.kuzmina.clientserver.CommandType;
import ru.kuzmina.clientserver.commands.AuthOkCommandData;
import ru.kuzmina.clientserver.commands.ClientMessageCommandData;
import ru.kuzmina.clientserver.commands.ErrorCommandData;
import ru.kuzmina.clientserver.commands.UpdateUserListCommandData;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;


public class ClientController {

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
            Dialogs.NetworkError.SEND_MESSAGE.show();
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
        ClientChat.INSTANCE.getChatStage().close();
    }


    public void initializeMessageHandler() {
        Network.getInstance().addReadMessageListener(new ReadCommandListener() {
            @Override
            public void processReceivedCommand(Command command) {
                switch (command.getType()) {
                    case AUTH_OK: {
                        Platform.runLater(() -> {
                            ClientChat.INSTANCE.getChatStage().setTitle(((AuthOkCommandData) command.getData()).getUserName());

                        });
                        break;
                    }
                    case CLIENT_MESSAGE: {
                        ClientMessageCommandData data = (ClientMessageCommandData) command.getData();
                        appendMessageToChat(data.getSender(), null, data.getMessage());
                        break;
                    }
                    case UPDATE_USER_LIST: {
                        UpdateUserListCommandData data = (UpdateUserListCommandData) command.getData();
                        Platform.runLater(() -> {
                            userList.setItems(FXCollections.observableList(data.getUsers()));
                        });
                        break;
                    }
                    case ERROR: {
                        Platform.runLater(() -> {
                            Dialogs.AuthErrors.INVALID_CREDENTIALS.show(((ErrorCommandData) command.getData()).getErrorMessage());
                        });
                        break;
                    }
                }
            }
        });
    }

    public void changeUserName(ActionEvent actionEvent) {
        ClientChat.INSTANCE.getChangeNameStage().show();
    }
}