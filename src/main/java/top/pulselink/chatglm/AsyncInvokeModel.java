package top.pulselink.chatglm;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AsyncInvokeModel {

    private String getMessage = "";
    private String TaskID = "";

    public CompletableFuture<String> asyncRequest(String token, String input, String url, String checkUrl) {
        return asyncInvokeRequestMethod(token, input, url)
                .thenCompose(taskId -> waitForTaskToComplete(token, checkUrl))
                .thenApply(responseData -> processTaskStatus(responseData))
                .exceptionally(ex -> "HTTP request failed with status code: " + ex.getMessage());
    }

    private CompletableFuture<String> asyncInvokeRequestMethod(String token, String message, String apiUrl) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json;charset=UTF-8")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString("{\"prompt\":\"" + message + "\"}"))
                .build();

        return HttpClient.newHttpClient()
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenCompose(response -> {
                    if (response.statusCode() == 200) {
                        processResponseData(response.body());
                        return CompletableFuture.completedFuture(response.body());
                    } else {

                        JsonObject errorResponse = JsonParser.parseString(response.body()).getAsJsonObject();
                        if (errorResponse.has("code") && errorResponse.has("msg")) {
                            int code = errorResponse.get("code").getAsInt();
                            String msg = errorResponse.get("msg").getAsString();
                            throw new RuntimeException("HTTP request failure, Code: " + code + ", Message: " + msg);
                        } else {
                            return CompletableFuture.failedFuture(new RuntimeException("HTTP request failure, Code: " + response.statusCode()));
                        }
                    }
                });
    }

    private CompletableFuture<String> asyncInvokeGetMethod(String token, String checkUrl) {
        return HttpClient.newHttpClient()
                .sendAsync(HttpRequest.newBuilder()
                        .uri(URI.create(checkUrl + TaskID))
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .header("Authorization", "Bearer " + token)
                        .GET()
                        .build(), HttpResponse.BodyHandlers.ofString())
                .thenCompose(response -> {
                    if (response.statusCode() == 200) {
                        //System.out.println(checkUrl + TaskID);
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
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    private boolean isTaskComplete(String taskStatus) {
        JsonObject taskStatusJson = JsonParser.parseString(taskStatus).getAsJsonObject();
        if (taskStatusJson.has("data")) {
            JsonObject data = taskStatusJson.getAsJsonObject("data");
            if (data.has("task_status")) {
                String status = data.get("task_status").getAsString();
                return "SUCCESS".equalsIgnoreCase(status);
            }
        }
        return false;
    }

    private void processResponseData(String responseData) {
        JsonObject jsonResponse = JsonParser.parseString(responseData).getAsJsonObject();

        if (jsonResponse.has("data")) {
            JsonObject data = jsonResponse.getAsJsonObject("data");
            if (data.has("task_id")) {
                String taskId = data.get("task_id").getAsString()
                        .replace("\"", "")
                        .replace("\\n\\n", "\n");
                this.TaskID = taskId;

            }
        } else {
            System.out.println("Response does not contain 'data' field");
        }
    }

    private String processTaskStatus(String responseData) {
        try {
            JsonObject jsonResponse = JsonParser.parseString(responseData).getAsJsonObject();
            if (jsonResponse.has("data")) {
                JsonObject data = jsonResponse.getAsJsonObject("data");
                if (data.has("choices")) {
                    JsonArray choices = data.getAsJsonArray("choices");
                    if (!choices.isEmpty()) {
                        JsonObject choice = choices.get(0).getAsJsonObject();
                        if (choice.has("content")) {
                            String message = choice.get("content").getAsString()
                                    .replaceAll("\"", "")
                                    .replace("\\", "")
                                    .replace("\\n\\n", "\n");
                            message = convertUnicodeEmojis(message);
                            getMessage = message;
                        }
                    }
                }
            }
        } catch (Exception e) {
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

    public String getTaskID() {
        return TaskID;
    }

    public String getContentMessage() {
        return getMessage.replace("\\n", "\n");
    }
}
