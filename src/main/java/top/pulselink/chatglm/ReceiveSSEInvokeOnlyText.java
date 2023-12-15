package top.pulselink.chatglm;

public class ReceiveSSEInvokeOnlyText {

    private String responseMessage = null;
    private final String defaultUrl = "https://open.bigmodel.cn/api/paas/v3/model-api/chatglm_turbo/sse-invoke";

    public ReceiveSSEInvokeOnlyText(String token, String message) {
        receiveSSEInvoke(token, message, defaultUrl);
    }

    private void receiveSSEInvoke(String token, String message, String url) {
        try {
            SSEInvokeModel sseInvoke = new SSEInvokeModel();
            sseInvoke.SSEinvokeRequestMethod(token, message, url);
            responseMessage = sseInvoke.resultQueue.take();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            ex.printStackTrace();
        }
    }

    public String getResponseMessage() {
        return responseMessage;
    }
}
