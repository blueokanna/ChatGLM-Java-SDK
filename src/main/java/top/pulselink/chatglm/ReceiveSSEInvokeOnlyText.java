package top.pulselink.chatglm;

public class ReceiveSSEInvokeOnlyText {

    private static final String chat_pro = "chatglm_pro/sse-invoke";
    private static final String chat_std = "chatglm_std/sse-invoke";
    private static final String chat_lite = "chatglm_lite/sse-invoke";
    private String getElements;
    private String DefaultUrl = "https://open.bigmodel.cn/api/paas/v3/model-api/chatglm_std/sse-invoke";
    private String SSEInvokeapiUrl = "https://open.bigmodel.cn/api/paas/v3/model-api/";

    public ReceiveSSEInvokeOnlyText(String token, String message) {
        SSEInvokeSendRequest(token, message, DefaultUrl);
    }

    public ReceiveSSEInvokeOnlyText(String token, String message, String selection) {
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
        SSEInvokeapiUrl = SSEInvokeapiUrl + selections;
        SSEInvokeSendRequest(token, message, SSEInvokeapiUrl);
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
