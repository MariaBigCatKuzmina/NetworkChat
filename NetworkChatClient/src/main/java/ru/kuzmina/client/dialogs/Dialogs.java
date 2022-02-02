package ru.kuzmina.client.dialogs;

import javafx.scene.control.Alert;
import ru.kuzmina.client.ClientChat;

public class Dialogs {
    public enum AuthErrors {
        EMPTY_CREDENTIALS("Логин и пароль не должны быть пустыми"),
        INVALID_CREDENTIALS("Логин и пароль неверны");

        private static final String TITLE = "Ошибка аутентификации";
        private static final String TYPE = TITLE;
        private final String message;

        AuthErrors(String message) {
            this.message = message;
        }

        public void show() {
            showDialog(Alert.AlertType.ERROR, TITLE, TYPE, message);
        }

        public void show(String errorMessage) {
            showDialog(Alert.AlertType.ERROR, TITLE, TYPE, errorMessage);
        }
    }

    public enum NetworkError {
        SEND_MESSAGE("Не удалось отправить сообщение"),
        SERVER_CONNECT("Не удалось установить соединение с сервером");

        private static final String TITLE = "Ошибка сети";
        private static final String TYPE = "Ошибка передачи данных по сети";
        private final String message;

        NetworkError(String message) {
            this.message = message;
        }

        public void show() {
            showDialog(Alert.AlertType.ERROR, TITLE, TYPE, message);
        }

    }

    private static  void showDialog(Alert.AlertType dialogType, String title, String type, String message){
        Alert alert = new Alert(dialogType);
        alert.initOwner(ClientChat.INSTANCE.getChatStage());
        alert.setTitle(title);
        alert.setHeaderText(type);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
