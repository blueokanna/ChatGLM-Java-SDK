package top.pulselink.chatglm;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HistoryMessage {

    private Gson gson;
    private String historyFilePath;

    public HistoryMessage() {
        this.gson = new Gson();
        this.historyFilePath = ConstantValue.HISTORY_FILE;

        createHistoryFileIfNotExists();
    }

    private void createHistoryFileIfNotExists() {
        Path filePath = Paths.get(historyFilePath);
        if (!Files.exists(filePath)) {
            try {
                Files.createFile(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String addHistoryToFile(String role, String content, String... additionalParameters) {
        String json = createJson(role, content, additionalParameters);

        try (FileWriter fileWriter = new FileWriter(historyFilePath, true);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {

            bufferedWriter.write(json);
            bufferedWriter.write(",");
            bufferedWriter.newLine();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return json;
    }

    private String createJson(String role, String content, String... additionalParameters) {
        JsonObject historys = new JsonObject();
        historys.addProperty("role", role);
        historys.addProperty("content", content);
        for (int i = 0; i < additionalParameters.length; i += 2) {
            historys.addProperty(additionalParameters[i], additionalParameters[i + 1]);
        }
        return gson.toJson(historys);
    }

    public String loadHistoryFromFile() {
        StringBuilder historyJson = new StringBuilder();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(historyFilePath))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                historyJson.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return historyJson.toString();
    }
}
