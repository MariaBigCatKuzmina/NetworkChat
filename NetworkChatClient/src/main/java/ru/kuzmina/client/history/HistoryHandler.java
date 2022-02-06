package ru.kuzmina.client.history;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryHandler {
    private final String historyDirectory;
    private BufferedWriter fileWriter;

    public void writeIntoFile(String message) throws IOException {
        fileWriter.write(message);
        fileWriter.flush();
    }

    public List<String> readLastHundredEntries() throws IOException {
        String l;
        List<String> fileLines = new ArrayList<>();
        try (BufferedReader fileReader = new BufferedReader(new FileReader(historyDirectory))) {
            while ((l = fileReader.readLine()) != null) {
                fileLines.add(l);
            }
            if (!fileLines.isEmpty()) {
                int arrayListLength = fileLines.size() - 1;
                int firstElement = (arrayListLength <= 100) ? 0 : arrayListLength - 100;
                return  fileLines.subList(firstElement, arrayListLength);
            }
            return null;
        }
    }

    public void close() throws IOException {
        if (fileWriter != null) {
            fileWriter.close();
        }
    }

    public HistoryHandler(String userName) throws IOException {
        this.historyDirectory = String.format("src/main/java/ru/kuzmina/client/history/history_%s.txt", userName);
        fileWriter = new BufferedWriter(new FileWriter(historyDirectory, true));
    }

    public String getHistoryDirectory() {
        return historyDirectory;
    }
}
