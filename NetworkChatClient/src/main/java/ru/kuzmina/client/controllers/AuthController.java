package ru.kuzmina.client.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ru.kuzmina.client.ClientChat;
import ru.kuzmina.client.dialogs.Dialogs;
import ru.kuzmina.client.model.Network;
import ru.kuzmina.client.model.ReadCommandListener;
import ru.kuzmina.clientserver.Command;
import ru.kuzmina.clientserver.commands.AuthOkCommandData;
import ru.kuzmina.clientserver.commands.ErrorCommandData;

import java.io.IOException;

public class AuthController {

    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button authButton;
    private ReadCommandListener readCommandListener;


    @FXML
    public void executeAuth(ActionEvent actionEvent) {
        String login = loginField.getText();
        String password = passwordField.getText();
        if (login == null || login.isBlank() || password == null || password.isBlank()) {
            Dialogs.AuthErrors.EMPTY_CREDENTIALS.show();
            return;
        }
        if (!hasConnectedToServer()) {
            Dialogs.NetworkError.SERVER_CONNECT.show();
        }
        try {
            Network.getInstance().sendAuthMessage(login, password);
        } catch (IOException e) {
            Dialogs.NetworkError.SEND_MESSAGE.show();
            e.printStackTrace();
        }
    }

    private boolean hasConnectedToServer() {
        Network network = Network.getInstance();
        return network.isConnected() || network.connect();
    }

    public void initializeMessageHandler() {
        readCommandListener = getNetwork().addReadMessageListener(new ReadCommandListener() {
            @Override
            public void processReceivedCommand(Command command) {
                switch (command.getType()) {
                    case AUTH_OK: {
                        Platform.runLater(() -> {
                            ClientChat.INSTANCE.switchToMainChatWindow(((AuthOkCommandData) command.getData()).getUserName());
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


    private Network getNetwork() {
        return Network.getInstance();
    }

    public void close() {
        getNetwork().removeReadMessageListener(readCommandListener);
    }
}
