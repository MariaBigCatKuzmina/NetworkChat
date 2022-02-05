package ru.kuzmina.server.chat;

import ru.kuzmina.clientserver.Command;
import ru.kuzmina.server.chat.auth.AuthService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MyServer {

    private final List<ClientHandler> clients = new ArrayList<>();
    private AuthService authService;

    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server has been started");
            authService = new AuthService();
            while (true) {
                waitAndProcessClientConnection(serverSocket);
            }
        } catch (IOException e) {
            System.err.println("Failed to bind port " + port);
            e.printStackTrace();
        }
    }

    private void waitAndProcessClientConnection(ServerSocket serverSocket) throws IOException {
        System.out.println("Waiting for new connections");
        Socket clientSocket = serverSocket.accept();
        System.out.println("Client has been connected");
        ClientHandler clientHandler = new ClientHandler(this, clientSocket);
        clientHandler.handle();
    }

    public synchronized void broadcastMessage(String message, ClientHandler sender) throws IOException {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendCommand(Command.clientMessageCommand(sender.getUserName(),message));
            }
        }
    }

    public synchronized void sendPrivateMessage(ClientHandler sender, String recipientName, String message) throws IOException {
        for (ClientHandler client : clients) {
            if (client.getUserName().equals(recipientName) && client != sender){
                client.sendCommand(Command.clientMessageCommand(sender.getUserName(), message));
                return;
            }
        }
    }

    public synchronized boolean isClientConnected (String userName) {
        for (ClientHandler clientItm : this.clients) {
            if (clientItm.getUserName().equals(userName)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void subscribe(ClientHandler client) throws IOException {
        this.clients.add(client);
        notifyClientUserListUpdated();
    }

    public synchronized void unsubscribe(ClientHandler client) throws IOException {
        this.clients.remove(client);
        notifyClientUserListUpdated();
    }

    public AuthService getAuthService() {
        return authService;
    }

    public void notifyClientUserListUpdated() throws IOException {
        List<String> connectedUsers = new ArrayList<>();
        for (ClientHandler client : clients) {
            connectedUsers.add(client.getUserName());
        }
        for (ClientHandler client : clients) {
            client.sendCommand(Command.updateUserListCommand(connectedUsers));
        }
    }

}
