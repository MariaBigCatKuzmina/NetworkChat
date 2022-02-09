package ru.kuzmina.client.controllers;


import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import ru.kuzmina.client.ClientChat;
import ru.kuzmina.client.dialogs.Dialogs;
import ru.kuzmina.client.model.Network;
import ru.kuzmina.client.model.ReadCommandListener;
import ru.kuzmina.clientserver.Command;

import ru.kuzmina.clientserver.commands.AuthOkCommandData;
import ru.kuzmina.clientserver.commands.ClientMessageCommandData;
import ru.kuzmina.clientserver.commands.ErrorCommandData;
import ru.kuzmina.clientserver.commands.UpdateUserListCommandData;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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
        StringBuilder sb = new StringBuilder();

        String messageHeader;
        messageHeader = sender != null ? sender : "no sender";
        messageHeader = recipient != null && sender != null ? messageHeader + " -> " + recipient : messageHeader + ": ";

        sb.append(DateFormat.getDateTimeInstance().format(new Date()));
        sb.append(System.lineSeparator());
        sb.append(messageHeader);
        sb.append(System.lineSeparator());
        sb.append(message);
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        chatArea.appendText(sb.toString());
        try {
            ClientChat.INSTANCE.getHistoryHandler().writeIntoFile(sb.toString());
        } catch (IOException e) {
            Platform.runLater(() -> {
                Dialogs.HistoryFileError.WRITE_FILE.show();
            });
            e.printStackTrace();
        }
        messageField.setFocusTraversable(true);
        messageField.clear();
    }

    public void loadChatHistory() throws IOException {
        List<String> lastHistory = new ArrayList<> (ClientChat.INSTANCE.getHistoryHandler().readLastNEntries(100));
        if (lastHistory != null) {
            Iterator<String> itr = lastHistory.iterator();
            while (itr.hasNext()) {
                chatArea.appendText(itr.next());
                chatArea.appendText(System.lineSeparator());
            }
            chatArea.appendText(System.lineSeparator());
        }
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