package top.pulselink.chatglm;

public class ChatClient {

    private static APIKeys apiKeys;
    private static String jwtToken;
    private static final String algorithm = "HmacSHA256";
    private String ResponseMessage;

    public ChatClient(String APIs) {
        apiKeys = APIKeys.getInstance(APIs);
        jwtToken = new CustomJWT(apiKeys.getUserId(), apiKeys.getUserSecret(), algorithm).createJWT();
    }

    public void SSEInvoke(String userInput) {
        ReceiveSSEInvokeOnlyText receiveInvokeModel = new ReceiveSSEInvokeOnlyText(jwtToken, userInput);
        try {
            ResponseMessage = receiveInvokeModel.getResponseMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void SyncInvoke(String userInput) {
        ReceiveInvokeModelOnlyText receiveInvokeModel = new ReceiveInvokeModelOnlyText(jwtToken, userInput);
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
            e.getMessage();
        }
    }

    public String getResponseMessage() {
        return ResponseMessage;
    }
}
