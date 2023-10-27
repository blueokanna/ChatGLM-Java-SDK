package top.pulselink.chatglm;

import java.util.concurrent.CompletableFuture;


public class ReceiveAsyncInvokeOnlyText {

    private String ReponseMessage;
    private String DefaultUrl = "https://open.bigmodel.cn/api/paas/v3/model-api/chatglm_turbo/async-invoke";
    private String AsyncInvokeCheckUrl = "https://open.bigmodel.cn/api/paas/v3/model-api/-/async-invoke/";

    public ReceiveAsyncInvokeOnlyText(String token, String message) {
        sendRequestAndWait(token, message, DefaultUrl);
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