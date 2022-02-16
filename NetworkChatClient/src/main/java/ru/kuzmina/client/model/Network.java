package ru.kuzmina.client.model;

import ru.kuzmina.clientserver.Command;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Network {

    private final List<ReadCommandListener> listeners = new CopyOnWriteArrayList<>();

    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 8189;

    private final int port;
    private final String host;
    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    private static Network INSTANCE;
    private boolean isConnected;

    private final ExecutorService executorService;

    public static Network getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Network();
        }
        return INSTANCE;
    }

    private Network(int port, String host) {
        this.port = port;
        this.host = host;
        executorService = Executors.newSingleThreadExecutor();
    }

    private Network() {
        this(SERVER_PORT, SERVER_HOST);
    }

    public boolean connect() {
        try {
            this.socket = new Socket(this.host, this.port);
            this.outputStream = new ObjectOutputStream(socket.getOutputStream());
            this.inputStream = new ObjectInputStream(socket.getInputStream());
            //readMessageProcess =
            startReadMessageProcess();
            this.isConnected = true;
            return true;
        } catch (IOException e) {
            System.err.println("Ошибка подключения к серверу");
            e.printStackTrace();
            return false;
        }
    }

    public void sendMessage(String message) throws IOException {
        sendCommand(Command.publicMessageCommand(message));
    }

    private void sendCommand(Command command) throws IOException {
        try {
            outputStream.writeObject(command);
        } catch (IOException e) {
            System.err.println("Не удалось отправить сообщение");
            throw e;
        }
    }

    public void sendPrivateMessage(String recipient, String message) throws IOException {
        sendCommand(Command.privateMessageCommand(recipient, message));
    }

    public void sendAuthMessage(String login, String password) throws IOException {
        sendCommand(Command.authCommand(login, password));
    }

    public void sendChangeUserNameMessage(String oldName, String newName, String password) throws IOException {
        sendCommand(Command.changeUserNameCommand(oldName, newName, password));
    }

    public void startReadMessageProcess() {
         executorService.execute(() -> {
            while (true) {
                try {
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    Command command = readCommand();
                    for (ReadCommandListener commandListener : listeners) {
                        commandListener.processReceivedCommand(command);
                    }
                } catch (IOException e) {
                    System.err.println("Не удалось получить команду");
                    e.printStackTrace();
                    close();
                    break;
                }
            }
        });
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

    public ReadCommandListener addReadMessageListener(ReadCommandListener listener) {
        listeners.add(listener);
        return listener;
    }

    public void removeReadMessageListener(ReadCommandListener listener) {
        listeners.remove(listener);
    }

    public void close() {
        try {
            isConnected = false;
            inputStream.close();
            outputStream.close();
            socket.close();
            executorService.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return isConnected;
    }
}
