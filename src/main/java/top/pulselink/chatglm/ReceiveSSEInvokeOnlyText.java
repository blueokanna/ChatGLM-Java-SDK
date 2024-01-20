package top.pulselink.chatglm;

import java.util.concurrent.CompletableFuture;

public class ReceiveSSEInvokeOnlyText {

    private String responseSSEMessage = null;
    private final String defaultUrl = "https://open.bigmodel.cn/api/paas/v4/chat/completions";

    public ReceiveSSEInvokeOnlyText(String token, String message) {
        receiveSSEInvoke(token, message, defaultUrl);
    }

    private void receiveSSEInvoke(String token, String message, String url) {

        SSEInvokeModel sseInvoke = new SSEInvokeModel();
        CompletableFuture<String> result = sseInvoke.SSERequest(token, message, url);

        result.thenAccept(response -> {
            responseSSEMessage = sseInvoke.getContentMessage();
        }).exceptionally(ex -> {
            System.err.println("Error: " + ex.getMessage());
            return null;
        });
        result.join();
    }

    public String getResponseMessage() {
        return responseSSEMessage;
    }
}
