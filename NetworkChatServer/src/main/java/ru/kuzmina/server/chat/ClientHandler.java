package ru.kuzmina.server.chat;

import ru.kuzmina.clientserver.Command;
import ru.kuzmina.clientserver.CommandType;
import ru.kuzmina.clientserver.commands.AuthCommandData;
import ru.kuzmina.clientserver.commands.ChangeUserNameCommandData;
import ru.kuzmina.clientserver.commands.PrivateMessageCommandData;
import ru.kuzmina.clientserver.commands.PublicMessageCommandData;

import java.io.*;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//Класс отвечающий за клиентское соединение
public class ClientHandler {

    private final MyServer server;
    private final Socket clientSocket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private String userName;

    private ExecutorService executorService;


    public ClientHandler(MyServer myServer, Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.server = myServer;
        executorService = Executors.newFixedThreadPool(1);
    }

    public void handle() throws IOException {
        inputStream = new ObjectInputStream(clientSocket.getInputStream());
        outputStream = new ObjectOutputStream(clientSocket.getOutputStream());

        executorService.execute(() -> {
            try {
                authenticate();
                readMessages();
            } catch (IOException e) {
                System.err.println("Failed to read incoming message from client");
                e.printStackTrace();
            } finally {
                try {
                    closeConnection();
                } catch (IOException e) {
                    System.err.println("Failed to close connection");
                    e.printStackTrace();
                }
            }
        });
    }

    private void authenticate() throws IOException {
        Timer closeConnectionTimer = getCloseConnectionTimer();

        while (true) {
            Command command = readCommand();
            if (command == null) {
                continue;
            }
            if (command.getType() == CommandType.AUTH) {
                AuthCommandData data = (AuthCommandData) command.getData();
                String login = data.getLogin();
                String password = data.getPassword();

                String userName = server.getAuthService().getUserNameByLoginAndPassword(login, password);
                if (userName == null) {
                    sendCommand(Command.errorCommand("Некорректный логин и пароль"));
                } else if (!server.isClientConnected(userName)) {
                    this.userName = userName;
                    sendCommand(Command.authOkCommand(userName));
                    server.subscribe(this);
                    closeConnectionTimer.cancel();
                    return;
                } else {
                    sendCommand(Command.errorCommand("Пользователь с таким логином и паролем уже подключен"));
                }
            }
        }
    }

    private Timer getCloseConnectionTimer() {
        Timer closeConnectionTimer = new Timer();
        TimerTask closeConnectionTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    System.out.println("Connection closed on timeout");
                    sendCommand(Command.errorCommand("Соединение с сервером прервано по таймауту./nПерезапустите приложение и попробуйте еще раз "));
                    closeConnection();
                } catch (IOException e) {
                    System.err.println("Failed to close connection");
                    e.printStackTrace();
                }
            }
        };
        closeConnectionTimer.schedule(closeConnectionTask, 120000);
        return closeConnectionTimer;
    }

    public void sendCommand(Command command) throws IOException {
        outputStream.writeObject(command);
    }

    private Command readCommand() throws IOException {
        Command command = null;
        try {
            command = (Command) inputStream.readObject();
         } catch (ClassNotFoundException e) {
            System.err.println("Failed to read a command class");
            e.printStackTrace();
        }
        return command;
    }

    private void readMessages() throws IOException {
        while (true) {
            Command command = readCommand();
            if (command == null) {
                continue;
            }
            switch (command.getType()) {
                case END:
                    return;
                case PRIVATE_MESSAGE: {
                    PrivateMessageCommandData privateCommand = (PrivateMessageCommandData) command.getData();
                    String recipientName = privateCommand.getReciever();
                    String message = privateCommand.getMessage();
                    server.sendPrivateMessage(this, recipientName, message);
                    break;
                }
                case PUBLIC_MESSAGE: {
                    PublicMessageCommandData publicCommand = (PublicMessageCommandData) command.getData();
                    server.broadcastMessage(publicCommand.getMessage(), this);
                    break;
                }
                case CHANGE_USERNAME: {
                    ChangeUserNameCommandData commandData = (ChangeUserNameCommandData) command.getData();
                    if (server.getAuthService().updateUserName(commandData.getOldUserName(), commandData.getNewUserName(),commandData.getPassword()) > 0) {
                        userName = commandData.getNewUserName();
                        server.notifyClientUserListUpdated();
                        sendCommand(Command.authOkCommand(userName));
                    } else {
                        sendCommand(Command.errorCommand("Неверный пароль"));
                    }
                    break;
                }
            }
        }
    }

    private void closeConnection() throws IOException {
        server.unsubscribe(this);
        inputStream.close();
        outputStream.close();
        clientSocket.close();
        executorService.shutdown();
    }

    public String getUserName() {
        return userName;
    }

}
