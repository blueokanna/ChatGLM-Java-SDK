package top.pulselink.chatglm;

public class ReceiveSSEInvokeOnlyText {

    private String getElements;
    private String DefaultUrl = "https://open.bigmodel.cn/api/paas/v3/model-api/chatglm_std/sse-invoke";

    public ReceiveSSEInvokeOnlyText(String token, String message) {
        SSEInvokeSendRequest(token, message, DefaultUrl);
    }

    private void SSEInvokeSendRequest(String token, String message, String urls) {
        try {
            SSEInvokeModel sseInvoke = new SSEInvokeModel();
            sseInvoke.SSEinvokeRequestMethod(token, message, urls);
            getElements = sseInvoke.resultQueue.take();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public String getGetElement() {
        return getElements;
    }
}
