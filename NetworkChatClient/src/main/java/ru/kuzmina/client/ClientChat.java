package ru.kuzmina.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.kuzmina.client.controllers.AuthController;
import ru.kuzmina.client.controllers.ClientController;
import ru.kuzmina.client.history.HistoryHandler;

import java.io.IOException;

public class ClientChat extends Application {
    public static ClientChat INSTANCE;

    private FXMLLoader chatWindowLoader;
    private FXMLLoader authDialogLoader;
    private FXMLLoader changeNameLoader;
    private Stage primaryStage;
    private Stage authStage;
    private Stage changeNameStage;

    private static String userName;

    private HistoryHandler historyHandler;

    @Override
    public void start(Stage stage) throws IOException {
        this.primaryStage = stage;
        initViews();
        getChatStage().show();
        getAuthStage().show();

        getAuthController().initializeMessageHandler();
    }

    @Override
    public void init() {
        INSTANCE = this;
    }

    public void initViews() throws IOException {
        initChatWindow();
        initAuthDialog();
        initChangeNameDialog();
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

    private void initChangeNameDialog() throws IOException {
        changeNameLoader = new FXMLLoader(getClass().getResource("changeUserName-template.fxml"));
        Parent changeNameDialog = changeNameLoader.load();
        changeNameStage = new Stage();
        changeNameStage.initOwner(primaryStage);
        changeNameStage.initModality(Modality.WINDOW_MODAL);
        changeNameStage.setScene(new Scene(changeNameDialog));
    }
    private AuthController getAuthController(){
        return authDialogLoader.getController();
    }

    private ClientController getChatController(){
        return chatWindowLoader.getController();
    }

    public void switchToMainChatWindow(String userName) {
        this.userName = userName;
        getChatStage().setTitle(userName);
        getChatController().initializeMessageHandler();
        getAuthController().close();
        getAuthStage().close();
        try {
            historyHandler = new HistoryHandler(userName);
            getChatController().loadChatHistory();
        } catch (IOException e) {
            System.err.println("Failed to open history file ");
            e.printStackTrace();
        }

    }

    public Stage getAuthStage() {
        return authStage;
    }

    public Stage getChatStage() {
        return this.primaryStage;
    }

    public Stage getChangeNameStage() {
        return changeNameStage;
    }

    public static String getUserName() {
        return userName;
    }

    public HistoryHandler getHistoryHandler() {
        return historyHandler;
    }

    public static void main(String[] args) {
        Application.launch();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        historyHandler.close();
    }
}