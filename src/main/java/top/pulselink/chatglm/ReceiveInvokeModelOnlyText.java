package top.pulselink.chatglm;

import java.util.concurrent.CompletableFuture;

public class ReceiveInvokeModelOnlyText {


    private String responseMessage;
    private String DefaultUrl = "https://open.bigmodel.cn/api/paas/v3/model-api/chatglm_turbo/invoke";

    public ReceiveInvokeModelOnlyText(String token, String message) {
        sendRequestAndWait(token, message, DefaultUrl);
    }

    private void sendRequestAndWait(String token, String message, String apiUrl) {
        InvokeModel invokeModel = new InvokeModel();
        CompletableFuture<String> result = invokeModel.HTTPServer(token, message, apiUrl);
        result.thenAccept(response -> {
            responseMessage = invokeModel.getContentMessage();
        }).join();
    }

    public String getResponseMessage() {
        return responseMessage;
    }

}
