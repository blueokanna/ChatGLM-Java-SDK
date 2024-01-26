package top.pulselink.chatglm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import static top.pulselink.chatglm.ConstantValue.*;

public class ChatClient{

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

    private static String loadApiKey() {                    //Load API Key
        try (BufferedReader reader = new BufferedReader(new FileReader(API_KEY_FILE))) {
            return reader.readLine();
        } catch (IOException e) {
            return null; // If the file doesn't exist or an error occurs, return null
        }
    }

    private static void saveApiKey(String apiKey) {         //Save API Key
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(API_KEY_FILE))) {
            writer.write(apiKey);
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }
    }

    public String getResponseMessage() {
        return ResponseMessage;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String apiKeyss = loadApiKey();

        if (apiKeyss == null) {
            System.out.println("请输入你的 API 密钥:");
            apiKeyss = scanner.nextLine();
            saveApiKey(apiKeyss);
        }
        System.out.print("请输入对话:\n你: ");
        while (scanner.hasNext()) {
            ChatClient chats = new ChatClient(apiKeyss);      //Initial ChatClient (Instantiation)
            String userInput = scanner.nextLine();

            chats.SSEInvoke(userInput);                     //Assign the question you entered to the synchronised request
            System.out.print("莉莉娅: " + chats.getResponseMessage());  //Print out ChatGLM's response
            System.out.println("\n");
            System.out.print("你: ");
        }
    }

}
