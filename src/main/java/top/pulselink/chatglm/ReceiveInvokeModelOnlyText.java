package top.pulselink.chatglm;

import java.util.concurrent.CompletableFuture;

public class ReceiveInvokeModelOnlyText {

    private String responseSyncMessage;
    private String DefaultUrl = "https://open.bigmodel.cn/api/paas/v4/chat/completions";

    public ReceiveInvokeModelOnlyText(String token, String message) {
        sendRequestAndWait(token, message, DefaultUrl);
    }

    private void sendRequestAndWait(String token, String message, String apiUrl) {
        InvokeModel invokeModel = new InvokeModel();
        CompletableFuture<String> result = invokeModel.syncRequest(token, message, apiUrl);

        result.thenAccept(response -> {
            responseSyncMessage = invokeModel.getContentMessage();
        }).exceptionally(ex -> {
            System.err.println("Error: " + ex.getMessage());
            return null;
        });
        result.join();

    }

    public String getResponseMessage() {
        return responseSyncMessage;
    }

}
