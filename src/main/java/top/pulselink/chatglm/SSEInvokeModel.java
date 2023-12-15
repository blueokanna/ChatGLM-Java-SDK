package top.pulselink.chatglm;

import com.google.gson.JsonObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class SSEInvokeModel {

    protected BlockingQueue<String> resultQueue;

    public SSEInvokeModel() {
        resultQueue = new ArrayBlockingQueue<>(2000);
    }

    public void SSEinvokeRequestMethod(String token, String message, String weburl) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(weburl);
            connection = (HttpURLConnection) url.openConnection();
            setupConnection(token, message, connection);

            JsonObject payloadMessage = createPayload(message);

            sendData(connection, payloadMessage);

            receiveData(connection);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void setupConnection(String token, String message, HttpURLConnection connection) throws IOException {
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept", "text/event-stream");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setDoInput(true);
        connection.setDoOutput(true);
    }

    private JsonObject createPayload(String message) {
        JsonObject payloadMessage = new JsonObject();
        payloadMessage.addProperty("prompt", message);
        payloadMessage.addProperty("temperature", 0.95);
        payloadMessage.addProperty("top_p", 0.7);
        return payloadMessage;
    }

    private void sendData(HttpURLConnection connection, JsonObject payloadMessage) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8))) {
            writer.write(payloadMessage.toString());
            writer.flush();
        }
    }

    private void receiveData(HttpURLConnection connection) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder dataBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data: ")) {
                    String[] parts = line.split("\\:");
                    String data = parts[1].trim();
                    dataBuilder.append(data).append(" ");
                }
            }
            resultQueue.offer(dataBuilder.toString());
        }
    }
}
