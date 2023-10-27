package top.pulselink.chatglm;

import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
        try {
            URL url = new URL(weburl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "text/event-stream");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Authorization", "Bearer " + token);

            connection.setDoInput(true);
            connection.setDoOutput(true);

            JsonObject payloadMessage = new JsonObject();
            payloadMessage.addProperty("prompt", message);

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8))) {
                writer.write(payloadMessage.toString());
                writer.flush();
            } catch (IOException ex) {
                ex.getMessage();
            }

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
            } finally {
                connection.disconnect();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
