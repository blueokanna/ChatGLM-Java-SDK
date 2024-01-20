package top.pulselink.chatglm;

import java.util.concurrent.CompletableFuture;

public class ReceiveAsyncInvokeOnlyText {

    private String responseAsyncMessage;
    private String DefaultUrl = "https://open.bigmodel.cn/api/paas/v4/async/chat/completions";
    private String AsyncInvokeCheckUrl = "https://open.bigmodel.cn/api/paas/v4/async-result/";

    public ReceiveAsyncInvokeOnlyText(String token, String message) {
        sendRequestAndWait(token, message, DefaultUrl);
    }

    private void sendRequestAndWait(String token, String message, String urls) {

        AsyncInvokeModel asyncInvokeModel = new AsyncInvokeModel();
        CompletableFuture<String> result = asyncInvokeModel.asyncRequest(token, message, urls, AsyncInvokeCheckUrl);
        result.thenAccept(response -> {
            responseAsyncMessage = asyncInvokeModel.getContentMessage();
        }).exceptionally(ex -> {
            System.err.println("Error: " + ex.getMessage());
            return null;
        });
        result.join();
    }

    public String getReponse() {
        return responseAsyncMessage;
    }
}
