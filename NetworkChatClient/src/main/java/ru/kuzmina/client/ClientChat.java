package ru.kuzmina.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.kuzmina.client.controllers.AuthController;
import ru.kuzmina.client.controllers.ClientController;

import java.io.IOException;


public class ClientChat extends Application {
    public static ClientChat INSTANCE;

    private FXMLLoader chatWindowLoader;
    private FXMLLoader authDialogLoader;
    private Stage primaryStage;
    private Stage authStage;

    @Override
    public void start(Stage stage) throws IOException {
        this.primaryStage = stage;
        initViews();
        getChatStage().show();
        getAuthStage().show();

        getAuthController().initializeMessageHandler();
    }

    @Override
    public void init() throws Exception {
        INSTANCE = this;
    }

    public void initViews() throws IOException {
        initChatWindow();
        initAuthDialog();
    }

    private void initAuthDialog() throws IOException {
        authDialogLoader = new FXMLLoader();
        authDialogLoader.setLocation(getClass().getResource("authDialog-template.fxml"));
        Parent authDialogPanel = authDialogLoader.load();
        authStage = new Stage();
        authStage.initOwner(primaryStage);
        authStage.initModality(Modality.WINDOW_MODAL);
        authStage.setScene(new Scene(authDialogPanel));
    }

    private void initChatWindow() throws IOException {
        chatWindowLoader = new FXMLLoader();
        chatWindowLoader.setLocation(getClass().getResource("chat-template.fxml"));
        Parent root = chatWindowLoader.load();
        this.primaryStage.setScene(new Scene(root));

    }

    private AuthController getAuthController(){
        return authDialogLoader.getController();
    }

    private ClientController getChatController(){
        return chatWindowLoader.getController();
    }


    public void switchToMainChatWindow(String userName) {
        getChatStage().setTitle(userName);
        getChatController().initializeMessageHandler();
        getAuthController().close();
        getAuthStage().close();
    }

    public void showErrorDialog(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setContentText(errorMessage);
        alert.showAndWait();
    }

    public Stage getAuthStage() {
        return authStage;
    }

    public static void main(String[] args) {
        Application.launch();
    }

    public Stage getChatStage() {
        return this.primaryStage;
    }


}