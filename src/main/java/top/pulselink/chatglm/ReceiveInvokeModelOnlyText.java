package top.pulselink.chatglm;

import java.util.concurrent.CompletableFuture;

public class ReceiveInvokeModelOnlyText {

    private static final String chat_pro = "chatglm_pro/invoke";
    private static final String chat_std = "chatglm_std/invoke";
    private static final String chat_lite = "chatglm_lite/invoke";
    private String responseMessage, ReponseMessage;
    private String DefaultUrl = "https://open.bigmodel.cn/api/paas/v3/model-api/chatglm_std/invoke";
    private String InvokeapiUrl = "https://open.bigmodel.cn/api/paas/v3/model-api/";

    public ReceiveInvokeModelOnlyText(String token, String message) {
        sendRequestAndWait(token, message, DefaultUrl);
    }

    public ReceiveInvokeModelOnlyText(String token, String message, String selection) {
        String selections = null;
        switch (selection) {
            case "Pro" ->
                selections = chat_pro;
            case "Standard" ->
                selections = chat_std;
            case "Lite" ->
                selections = chat_lite;
            default -> {
            }
        }
        InvokeapiUrl = InvokeapiUrl + selections;
        sendRequestAndWait(token, message, InvokeapiUrl);
    }

    private void sendRequestAndWait(String token, String message, String apiUrl) {
        InvokeModel invokeModel = new InvokeModel();
        CompletableFuture<String> result = invokeModel.HTTPServer(token, message, apiUrl);
        result.thenAccept(response -> {
            ReponseMessage = response;
            responseMessage = invokeModel.getContentMessage();
        }).join();
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public String getTrueResponseMessage() {
        return ReponseMessage;
    }
}
