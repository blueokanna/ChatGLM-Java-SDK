package top.pulselink.chatglm;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static top.pulselink.chatglm.ConstantValue.*;

public class InvokeModel {

    public String getMessage = "";
    private String jsonRequestBody;
    private final HistoryMessage messages = new HistoryMessage();

    public synchronized CompletableFuture<String> syncRequest(String token, String input, String url) {
        return syncInvokeRequestMethod(token, input, url)
                .thenApply(responseData -> ResponseDataBody(responseData, input))
                .exceptionally(ex -> "HTTP request failed with status code: " + ex.getMessage());
    }

    private CompletableFuture<String> syncInvokeRequestMethod(String token, String message, String apiUrl) {
        jsonRequestBody = String.format("{\"model\":\"%s\", \"messages\":[{\"role\":\"%s\",\"content\":\"%s\"},%s], \"stream\":false,\"temperture\":%f,\"top_p\":%f}",
                Language_Model, system_role, system_content, lastMessages(message), temp_float, top_p_float);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json;charset=UTF-8")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody))
                .build();

        return HttpClient.newHttpClient()
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenCompose(response -> {
                    if (response.statusCode() == 200) {
                        ResponseDataBody(response.body(), message);
                        return CompletableFuture.completedFuture(response.body());
                    } else {
                        JsonObject errorResponse = JsonParser.parseString(response.body()).getAsJsonObject();
                        if (errorResponse.has("created") && errorResponse.has("id")) {
                            int idCode = errorResponse.get("id").getAsInt();
                            int createdCode = errorResponse.get("created").getAsInt();
                            throw new RuntimeException("HTTP request failure, Your request created code is: " + createdCode + ", your id: " + idCode);
                        } else {
                            return CompletableFuture.failedFuture(new RuntimeException("HTTP request failure, Code: " + response.statusCode()));
                        }
                    }
                });
    }

    private String ResponseDataBody(String responseData, String userMessage) {
        try {
            JsonObject jsonResponse = JsonParser.parseString(responseData).getAsJsonObject();

            if (jsonResponse.has("choices")) {
                JsonArray choices = jsonResponse.getAsJsonArray("choices");

                if (!choices.isEmpty()) {
                    JsonObject choice = choices.get(0).getAsJsonObject();

                    if (choice.has("message")) {
                        JsonObject message = choice.getAsJsonObject("message");

                        if (message.has("content")) {
                            String content = message.get("content").getAsString();
                            getMessage = convertUnicodeEmojis(content);
                            getMessage = getMessage.replaceAll("\"", "")
                                    .replaceAll("\\\\n\\\\n", "\n")
                                    .replaceAll("\\\\nn", "\n")
                                    .replaceAll("\\n", "\n")
                                    .replaceAll("\\\\", "")
                                    .replaceAll("\\\\", "");

                            messages.addHistoryToFile(user_role, userMessage);
                            messages.addHistoryToFile(assistant_role, getMessage);
                        }
                    }
                }
            }
        } catch (JsonSyntaxException e) {
            System.out.println("Error processing task status: " + e.getMessage());
        }

        return getMessage;
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

    private String setInputMessage() {
        String message = messages.loadHistoryFromFile();
        if (message != null) {
            return messages.loadHistoryFromFile();
        } else {
            return null;
        }
    }

    private String lastMessages(String userMessage) {
        JsonObject input = new JsonObject();
        input.addProperty("role", user_role);
        input.addProperty("content", userMessage);
        String texts = new Gson().toJson(input);

        String regex = ",(?=\\s*\\})";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(setInputMessage() + texts);
        return matcher.replaceAll("");
    }

    public String getContentMessage() {
        return getMessage;
    }
}
