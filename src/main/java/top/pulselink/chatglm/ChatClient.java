package top.pulselink.chatglm;

public class ChatClient {

    private static APIKeys apiKeys;
    private static String jwtToken;
    private static String algorithm = "HmacSHA256";
    private String ResponseMessage;

    public ChatClient(String APIs) {
        apiKeys = APIKeys.getInstance(APIs);
        jwtToken = new CustomJWT(apiKeys.getUserId(), apiKeys.getUserSecret(), algorithm).createJWT();
    }

    public void SyncInvoke(String userInput) {
        ReceiveInvokeModelOnlyText receiveInvokeModel = new ReceiveInvokeModelOnlyText(jwtToken, userInput);
        try {
            ResponseMessage = receiveInvokeModel.getResponseMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void SyncInvoke(String userInput, String selection) {
        ReceiveInvokeModelOnlyText receiveInvokeModel = new ReceiveInvokeModelOnlyText(jwtToken, userInput, selection);
        try {
            ResponseMessage = receiveInvokeModel.getResponseMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void AsyncInvoke(String userInput) {
        ReceiveAsyncInvokeOnlyText asyncInvokeOnlyText = new ReceiveAsyncInvokeOnlyText(jwtToken, userInput);
        try {
            ResponseMessage = asyncInvokeOnlyText.getReponse();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void AsyncInvoke(String userInput, String selection) {
        ReceiveAsyncInvokeOnlyText asyncInvokeOnlyText = new ReceiveAsyncInvokeOnlyText(jwtToken, userInput, selection);
        try {
            ResponseMessage = asyncInvokeOnlyText.getReponse();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void SSEInvoke(String userInput, String selection) {
        ReceiveSSEInvokeOnlyText SSEInvokeOnlyText = new ReceiveSSEInvokeOnlyText(jwtToken, userInput, selection);
        try {
            ResponseMessage = SSEInvokeOnlyText.getGetElement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getResponseMessage() {
        return ResponseMessage;
    }
}
