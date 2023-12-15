package top.pulselink.chatglm;

import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.CompletableFuture;

public class InvokeModel {

    private String contentMessage = "";

    public CompletableFuture<String> HTTPServer(String token, String message, String url) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return invokeMethod(token, message, url);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("HTTP request failed.");
            }
        });
    }

    private String readResponseData(HttpURLConnection connection) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    private boolean isJsonResponse(HttpURLConnection connection) {
        String contentType = connection.getContentType();
        return contentType != null && contentType.toLowerCase().contains("application/json");
    }

    private String invokeMethod(String token, String message, String apiUrl) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        setupConnectionProperties(connection, token);

        sendPayloadData(connection, message);

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            String responseData = readResponseData(connection);
            processResponseData(responseData, connection);
            return responseData;
        } else {
            System.out.println("HTTP request failure, Code: " + responseCode);
            return "HTTP request failure, Code: " + responseCode;
        }
    }

    private void setupConnectionProperties(HttpURLConnection connection, String token) throws ProtocolException {
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setDoInput(true);
        connection.setDoOutput(true);
    }

    private void sendPayloadData(HttpURLConnection connection, String message) throws IOException {
        JsonObject payloadMessage = new JsonObject();
        payloadMessage.addProperty("prompt", message);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] postDataBytes = payloadMessage.toString().getBytes(StandardCharsets.UTF_8);
            os.write(postDataBytes);
        }
    }

    private void processResponseData(String responseData, HttpURLConnection connection) {
        if (isJsonResponse(connection)) {
            JsonObject jsonResponse = JsonParser.parseString(responseData).getAsJsonObject();
            extractContentFromJson(jsonResponse);
        } else {
            System.out.println("Response is not in JSON format.");
        }
    }

    private void extractContentFromJson(JsonObject jsonResponse) {
        if (jsonResponse.has("data")) {
            JsonObject data = jsonResponse.getAsJsonObject("data");
            if (data.has("choices")) {
                JsonArray choices = data.getAsJsonArray("choices");
                processChoices(choices);
            }
        }
    }

    private void processChoices(JsonArray choices) {
        for (int i = 0; i < choices.size(); i++) {
            JsonObject choice = choices.get(i).getAsJsonObject();
            extractContent(choice);
        }
    }

    private void extractContent(JsonObject choice) {
        if (choice.has("content")) {
            String message = choice.get("content").getAsString()
                    .replaceAll("\"", "")
                    .replaceAll("\\\\n\\\\n", "\n")
                    .replaceAll("\\\\nn", "\n")
                    .replaceAll("\\n", "\n")
                    .replaceAll("\\\\", "");
            contentMessage = convertUnicodeEmojis(message);
        }
    }

    private String convertUnicodeEmojis(String input) {
        String regex = "\\\\u[0-9a-fA-F]{4}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String emoji = Character.toString((char) Integer.parseInt(matcher.group().substring(2), 16));
            matcher.appendReplacement(result, emoji);
        }
        matcher.appendTail(result);
        return result.toString();
    }

    public String getContentMessage() {
        return contentMessage;
    }
}
