package ru.kuzmina.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ru.kuzmina.client.ClientChat;
import ru.kuzmina.client.dialogs.Dialogs;
import ru.kuzmina.client.model.Network;

import java.io.IOException;

public class ChangeUserNameController {
    @FXML public TextField newName;
    @FXML public PasswordField password;

    @FXML
    public void changeUserName(ActionEvent actionEvent) throws IOException {
        Network network = Network.getInstance();
        String oldUserName = ClientChat.INSTANCE.getUserName();
        String newUserName = newName.getText();
        String pwd = password.getText();
        if (!oldUserName.equals(newUserName)) {
            if (!newUserName.isBlank() && !pwd.isBlank()) {
                if (network.isConnected()) {
                    network.sendChangeUserNameMessage(oldUserName, newUserName, pwd);
                } else {
                    Dialogs.NetworkError.SERVER_CONNECT.show();
                }
                cancel(null);
            } else {
                Dialogs.AuthErrors.EMPTY_CREDENTIALS.show("Имя пользователя и пароль не должны быть пустыми");
            }
        }
    }
    @FXML
    public void cancel(ActionEvent actionEvent) {
        newName.setText(null);
        password.setText(null);
        ClientChat.INSTANCE.getChangeNameStage().close();
    }
}
