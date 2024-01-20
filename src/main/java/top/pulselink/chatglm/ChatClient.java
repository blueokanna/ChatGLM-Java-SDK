package top.pulselink.chatglm;

import java.util.Scanner;
import static top.pulselink.chatglm.ConstantValue.*;

public class ChatClient {

    private static APIKeys apiKeys;
    private static String jwtToken;
    private String ResponseMessage;

    public ChatClient(String APIs) {
        apiKeys = APIKeys.getInstance(APIs);
        jwtToken = new CustomJWT(apiKeys.getUserId(), apiKeys.getUserSecret(), main_algorithm).createJWT();
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

    public static void main(String[] args) {
        final String apiKeyss = "xxxxxxxxxxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxx"; //Change your own API key,You can find it from https://open.bigmodel.cn/usercenter/apikeys

        Scanner scan = new Scanner(System.in); //Entering Content with Scanner

        while (scan.hasNext()) {
            String userInput = scan.nextLine();
            ChatClient chats = new ChatClient(apiKeyss);      //Initial ChatClient (Instantiation)
            chats.AsyncInvoke(userInput);                     //Assign the question you entered to the synchronised request
            System.out.print(chats.getResponseMessage());  //Print out ChatGLM's response
            System.out.println();
        }
    }
}
