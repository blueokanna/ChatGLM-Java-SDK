package top.pulselink.chatglm;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            String line;
            StringBuilder response = new StringBuilder();
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

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        
        connection.setDoInput(true);
        connection.setDoOutput(true);

        JsonObject payloadMessage = new JsonObject();
        payloadMessage.addProperty("prompt", message);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] postDataBytes = payloadMessage.toString().getBytes(StandardCharsets.UTF_8);
            os.write(postDataBytes);
        }

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

    private void processResponseData(String responseData, HttpURLConnection connection) {
        //String contentType = connection.getContentType();
        //System.out.println("Content-Type: " + contentType); // 调试信息

        if (isJsonResponse(connection)) {
            JsonObject jsonResponse = JsonParser.parseString(responseData).getAsJsonObject();
            if (jsonResponse.has("data")) {
                JsonObject data = jsonResponse.getAsJsonObject("data");
                if (data.has("choices")) {
                    JsonArray choices = data.getAsJsonArray("choices");
                    for (int i = 0; i < choices.size(); i++) {
                        JsonObject choice = choices.get(i).getAsJsonObject();
                        if (choice.has("content")) {
                            String Message = choice.get("content").getAsString();
                            Message = Message.replaceAll("\"", "");
                            Message = Message.replace("\\n\\n", "\n");
                            Message = Message.replace("\\", "");
                            Message = convertUnicodeEmojis(Message);
                            contentMessage = Message;
                        }
                    }
                }
            }
        } else {
            System.out.println("Response is not in JSON format.");
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

    protected String getContentMessage() {
        return contentMessage.replace("\\n", "\n");
    }
}
