package top.pulselink.chatglm;

import java.util.concurrent.CompletableFuture;


public class ReceiveAsyncInvokeOnlyText {

    private static final String chat_pro = "chatglm_pro/async-invoke";
    private static final String chat_std = "chatglm_std/async-invoke";
    private static final String chat_lite = "chatglm_lite/async-invoke";
    private String ReponseMessage;
    private String DefaultUrl = "https://open.bigmodel.cn/api/paas/v3/model-api/chatglm_std/async-invoke";
    private String AsyncInvokeapiUrl = "https://open.bigmodel.cn/api/paas/v3/model-api/";
    private String AsyncInvokeCheckUrl = "https://open.bigmodel.cn/api/paas/v3/model-api/-/async-invoke/";

    public ReceiveAsyncInvokeOnlyText(String token, String message) {
        sendRequestAndWait(token, message, DefaultUrl);
    }

    public ReceiveAsyncInvokeOnlyText(String token, String message, String selection) {
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
        AsyncInvokeapiUrl = AsyncInvokeapiUrl + selections;
        sendRequestAndWait(token, message, AsyncInvokeapiUrl);
    }

    private void sendRequestAndWait(String token, String message, String urls) {
        AsyncInvokeModel asyncInvokeModel = new AsyncInvokeModel();
        CompletableFuture<String> result = asyncInvokeModel.asyncRequest(token, message, urls, AsyncInvokeCheckUrl);

        result.thenAccept(response -> {
            ReponseMessage = asyncInvokeModel.getContentMessage();
        }).exceptionally(ex -> {
            System.err.println("Error: " + ex.getMessage());
            return null;
        });
        result.join();
    }

    public String getReponse() {
        return ReponseMessage;
    }
}