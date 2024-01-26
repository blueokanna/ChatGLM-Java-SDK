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

public class AsyncInvokeModel {

    private String getMessage = "";
    private String ID = "";
    private String jsonRequestBody;
    private final HistoryMessage messages = new HistoryMessage();

    public CompletableFuture<String> asyncRequest(String token, String input, String url, String checkUrl) {
        return asyncInvokeRequestMethod(token, input, url)
                .thenCompose(taskId -> waitForTaskToComplete(token, checkUrl))
                .thenApply(responseData -> processTaskStatus(responseData, input))
                .exceptionally(ex -> "HTTP request failed with status code: " + ex.getMessage());
    }

    private CompletableFuture<String> asyncInvokeRequestMethod(String token, String message, String apiUrl) {

        jsonRequestBody = String.format("{\"model\":\"%s\", \"messages\":[{\"role\":\"%s\",\"content\":\"%s\"},%s], \"stream\":false,\"temperture\":%f,\"top_p\":%f}",
                Language_Model, system_role, system_content, lastMessages(message), temp_float, top_p_float);

        //System.out.println(jsonRequestBody);  //Debug
        //System.out.println(messages.addHistoryToFile(user_role, message)); //Debug
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
                        processResponseData(response.body());
                        return CompletableFuture.completedFuture(response.body());
                    } else {
                        JsonObject errorResponse = JsonParser.parseString(response.body()).getAsJsonObject();
                        if (errorResponse.has("id") && errorResponse.has("task_status")) {
                            int code = errorResponse.get("id").getAsInt();
                            String status = errorResponse.get("task_status").getAsString();
                            throw new RuntimeException("HTTP request failure, Your request id is: " + code + ", Status: " + status);
                        } else {
                            return CompletableFuture.failedFuture(new RuntimeException("HTTP request failure, Code: " + response.statusCode()));
                        }
                    }
                });

    }

    private CompletableFuture<String> asyncInvokeGetMethod(String token, String checkUrl) {
        return HttpClient.newHttpClient()
                .sendAsync(HttpRequest.newBuilder()
                        .uri(URI.create(checkUrl + ID))
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .header("Authorization", "Bearer " + token)
                        .GET()
                        .build(), HttpResponse.BodyHandlers.ofString())
                .thenCompose(response -> {
                    if (response.statusCode() == 200) {
                        return CompletableFuture.completedFuture(response.body());
                    } else {
                        return CompletableFuture.failedFuture(new RuntimeException("HTTP request failure, Code: " + response.statusCode()));
                    }
                });
    }

    private CompletableFuture<String> waitForTaskToComplete(String token, String checkUrl) {
        return CompletableFuture.supplyAsync(() -> {
            while (true) {
                String taskStatus = asyncInvokeGetMethod(token, checkUrl).join();
                if (isTaskComplete(taskStatus)) {
                    return taskStatus;
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    private boolean isTaskComplete(String taskStatus) {
        JsonObject taskStatusJson = JsonParser.parseString(taskStatus).getAsJsonObject();

        if (taskStatusJson.has("task_status")) {
            String status = taskStatusJson.get("task_status").getAsString();
            return "SUCCESS".equalsIgnoreCase(status);
        }
        return false;
    }

    private void processResponseData(String responseData) {
        JsonObject jsonResponse = JsonParser.parseString(responseData).getAsJsonObject();
        if (jsonResponse.has("id")) {
            String taskId = jsonResponse.get("id").getAsString()
                    .replace("\"", "")
                    .replace("\\n\\n", "\n");
            this.ID = taskId;

        }
    }

    private String processTaskStatus(String responseData, String userMessage) {
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

    public String getTaskID() {
        return ID;
    }

    public String getContentMessage() {
        return getMessage;
    }
}
