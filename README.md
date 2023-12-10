# Third Party Customisation ChatGLM-Java-SDK
>
> ChatGLM-Java-SDK, a Java-based open interface for customised spectral macromodels, developed by **Java** in the long term version of **JDK17**.
----
## ⚠️Caution😟！The original **0.0.1** is no longer available, the official address of its call has been changed, it is not possible to use **0.0.1** version, please move to **0.0.2** version as soon as possible.

**Java Maven Dependency (BlueChatGLM)**
> Please use **Java Maven** Library✔️. **Java Ant** Using this seems to give an error.❌

```
<dependency>
  <groupId>top.pulselink</groupId>
  <artifactId>bluechatglm</artifactId>
  <version>0.0.2</version>
</dependency>
```

## 1.Using NTP Server Time

It provides highly accurate and secure time information via time servers on the Internet or LAN, and it is critical to ensure that all devices use the same time. The application here is for `JWT` authentication using the

```
//Get Network Time Protocol Server（NTP Server）

    protected long receiveTime() {
        long currentTime = System.currentTimeMillis(); //Gets the millisecond timestamp of the current system.
        if (currentTime - lastUpdateTime < 60000) { //If the time difference is less than 60 seconds, return the last time obtained from the NTP server
            return lastServerTime;
        } else {
            try {
                NTPUDPClient timeClient = new NTPUDPClient(); //Create an NTPUDPClient object to communicate with the NTP server.
                timeClient.setDefaultTimeout(timeout);
                InetAddress inetAddress = InetAddress.getByName(ntpServer);  //Get the IP address of the NTP server using the provided ntpServer string.
                TimeInfo timeInfo = timeClient.getTime(inetAddress);  //Extracting server time information
                long serverTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();
                lastServerTime = serverTime; //Stores the newly acquired server time in the lastServerTime variable for future use.
                lastUpdateTime = currentTime;  //Stores the current time in the lastUpdateTime variable for future comparison.
                return serverTime; //Returns the time obtained from the NTP server
            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch NTP time", e);
            }
        }
    }
```

----

## 2. Easy-to-use SDK

**The only constant quantity in this project：`algorithm = HmacSHA256`**

### 2.1 Calling and Using the Maven Library
>
> Using this project **SDK** is less difficult 🤩. The following three examples use **Scanner** to enter your question and the console will output **ChatGLM** to answer it：

Call **SSE request**, the sample code is as follows `(This example is more friendly to English output, Chinese output has problems)`:

```
public class Main{
    public static void main(String[] args) {
        String apiKeyss = "Your_API_Key"; //Replace the API Key with your own

        Scanner scan = new Scanner(System.in); //Entering Content with Scanner
        while (scan.hasNext()) {
             String userInput = scan.nextLine();
             ChatClient chats = new ChatClient(apiKeyss);      //Initial ChatClient (Instantiation)
             chats.SSEInvoke(userInput);                       //Assign the question you entered to the SSE request
             System.out.println(chats.getResponseMessage());   //Print out ChatGLM's response
        }
    }
}
```

Call **asynchronous request**, sample code is as follows:

```
public class Main{
    public static void main(String[] args) {
        String apiKeyss = "Your_API_Key"; //Replace the API Key with your own

        Scanner scan = new Scanner(System.in); //Entering Content with Scanner
        while (scan.hasNext()) {
             String userInput = scan.nextLine();
             ChatClient chats = new ChatClient(apiKeyss);      //Initial ChatClient (Instantiation)
             chats.AsyncInvoke(userInput);                    //Assign the question you entered to the asynchronous request
             System.out.println(chats.getResponseMessage());  //Print out ChatGLM's response
        }
    }
}
```

Call **synchronisation request**, sample code is as follows:

```
public class Main{
    public static void main(String[] args) {
        String apiKeyss = "Your_API_Key"; //Replace the API Key with your own

        Scanner scan = new Scanner(System.in); //Entering Content with Scanner
        while (scan.hasNext()) {
             String userInput = scan.nextLine();
             ChatClient chats = new ChatClient(apiKeyss);      //Initial ChatClient (Instantiation)
             chats.SyncInvoke(userInput);                     //Assign the question you entered to the synchronised request
             System.out.println(chats.getResponseMessage());  //Print out ChatGLM's response
        }
    }
}
```

### 2.2 Senior Developer👨🏼‍💻

For senior developers, this version is only a simple development, there are `temperature`, `top_p`, `incremental`, `return_type` and other parameters have not been added to this development. We will follow up with the development in the future, and of course we would like to ask other developers to provide technical support for this project! Thank you in advance!

----

## 3.Project Description

### **CustomJWT** is for the project's self-customisation and write, later will continue to develop and expand the project!

According to **JWT.io** this website for understanding and principle of learning, for this project of **JWT** validation, **Java** implementation is easier to achieve, which uses the part of the `Base64Url` instead of the conventional `Base64`.

**Encoding Base64Url** used by the editor is as follows:

```
private String encodeBase64Url(byte[] data) {
        String base64url = Base64.getUrlEncoder().withoutPadding().encodeToString(data) //convert the input to Base64Url
                .replace("+", "-")  //The plus sign here needs to be replaced with -.
                .replace("/", "_"); //replace the slash here with a _.
        return base64url; // return base64url
    }
```

----
Creates **JWT** that implements **Header** validation:

```
protected String createJWT() {
        String encodedHeader = encodeBase64Url(header.getBytes());
        String encodedPayload = encodeBase64Url(payload.getBytes()); String encodedPayload = encodeBase64Url(payload.getBytes());
        String toSign = encodedHeader + "." + encodedPayload.

        
        String calculatedSignature = encodeBase64Url(signatureBytes); return toSign + ".".
        return toSign + "." + calculatedSignature; }
    }
```

----
Verify that the **JWT** signature section matches the output:

```
protected boolean verifyJWT(String jwt) {
        jwt = jwt.trim();

        String[] parts = jwt.split("\\.");
        if (parts.length != 3) {
            return false;
        }

        String encodedHeader = parts[0];
        String encodedPayload = parts[1];
        String signature = parts[2];

        String toVerify = encodedHeader + "." + encodedPayload;
        byte[] calculatedSignatureBytes = generateSignature(toVerify, secret, algorithm);
        String calculatedSignature = encodeBase64Url(calculatedSignatureBytes);

        return calculatedSignature.equals(signature);
    }
 
```

### request a calling🌐

The request methods used in **Synchronous Request** and **SSE Request** are as follows (inside **Header**):

```
connection.setRequestMethod("POST");
connection.setRequestProperty("Accept", "application/json");        //synchronisation request
//connection.setRequestProperty("Accept", "text/event-stream");    //SSE request
connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
connection.setRequestProperty("Authorization", "Bearer " + token);
```

Use the **gson** library to make **Payload** write to **JSON**.

```
JsonObject payloadMessage = new JsonObject();
payloadMessage.addProperty("prompt", message); //Adding Properties
```

> Generally prompt -> message is enough, if you want to add other parts of the attributes can also be added here, such as adding about **temperature**, **top_p**:

```
payloadMessage.addProperty("temperature", 0.6);
payloadMessage.addProperty("top_p", 0.7);
```

#### SSE Streaming Transfer Model (SSEInvokeModel: currently imperfect, with some bugs, not recommended)

What is used here is a per **SSE streaming** generation, which generally fetches the resulting content containing `:event` , `request_id` and `data`. For the data after `add`, it is spliced together and here it is sorted using the queue method:

```
//Queue: BlockingQueue<String> resultQueue = new ArrayBlockingQueue<>(2000);
//Add your 'Content' data to the queue: resultQueue.offer(dataBuilder.toString());

/*
data：Content）
*/
            String[] pair = keyValue.split(":");
            if (pair.length == 2) {
                String key = pair[0].trim();
                String value = pair[1].trim();
                eventData.addProperty(key, value);
            }
```

For `meta`, you can add it later, the code example is as follows:

```
        if (line.startsWith("data: ")) {  // (line = reader.readLine()) != null
            String data = line.substring(6).trim();
            JsonObject eventData = parseEventData(data);

            String eventType = eventData.has("event") ? eventData.get("event").getAsString() : null;
            String eventDataString = eventData.has("data") ? eventData.get("data").getAsString() : null;

            if ("add".equals(eventType)) {          //add event
                System.out.println("Add Event: " + eventDataString);
            } else if ("error".equals(eventType) || "interrupted".equals(eventType)) {
                System.out.println("Error or Interrupted Event: " + eventDataString);
            } else if ("finish".equals(eventType)) {
                System.out.println("Finish Event: " + eventDataString);

                if (eventData.has("meta")) {        // meta data
                    JsonObject meta = eventData.getAsJsonObject("meta");
                    System.out.println("Meta: " + meta.toString());

                    if (meta.has("usage")) {         //Output of usage (number of Token)
                        JsonObject usage = meta.getAsJsonObject("usage");
                        System.out.println("Usage: " + usage.toString());
                        System.out.println("Prompt Tokens: " + usage.get("prompt_tokens").getAsInt());
                        System.out.println("Completion Tokens: " + usage.get("completion_tokens").getAsInt());
                        System.out.println("Total Tokens: " + usage.get("total_tokens").getAsInt());
                    }
                }
            }
        }
```

#### Asynchronous Request Transfer Model (AsyncInvokeModel: recommended)

The `HTTPRequest` method is used here to receive the message:

```
HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Accept", "application/json")  //request header
                .header("Content-Type", "application/json;charset=UTF-8")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString("{\"prompt\":\"" + message + "\"}"))  //Payload section -> corresponds to the information entered by the user
                .build();
```

The overall use is to send messages asynchronously, which has the advantage of reducing thread blocking, where `code` and `msg` are getting error messages. When you get a `request_id`, then query the

```
                    if (response.statusCode() == 200) {      //When the response value is 200, output the corresponding parameters of the interface for an asynchronous request.
                        processResponseData(response.body());
                        return CompletableFuture.completedFuture(response.body());
                    } else {

                        JsonObject errorResponse = JsonParser.parseString(response.body()).getAsJsonObject(); //is not 200, an error message is returned
                        if (errorResponse.has("code") && errorResponse.has("msg")) {
                            int code = errorResponse.get("code").getAsInt();
                            String msg = errorResponse.get("msg").getAsString();
                            throw new RuntimeException("HTTP request failure, Code: " + code + ", Message: " + msg);
                        } else {
                            return CompletableFuture.failedFuture(new RuntimeException("HTTP request failure, Code: " + response.statusCode()));
                        }
                    }        
```

When you get the **Task_id** you need, make a **GET** request query (part of the code):

```
                ..... .sendAsync(HttpRequest.newBuilder()
                        .uri(URI.create(checkUrl + TaskID)) //Add Taskid to the query address
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .header("Authorization", "Bearer " + token)
                        .GET()
                        .build(), HttpResponse.BodyHandlers.ofString())
                .thenCompose(response -> {......
                )};
```

Finally the extraction by **JSON**, the sample extraction code is:

```
JsonObject jsonResponse = JsonParser.parseString(responseData).getAsJsonObject();
            if (jsonResponse.has("data")) {
                JsonObject data = jsonResponse.getAsJsonObject("data");   //Getting inside data
                if (data.has("choices")) {
                    JsonArray choices = data.getAsJsonArray("choices");  //choices to get the message
                    if (!choices.isEmpty()) {
                        JsonObject choice = choices.get(0).getAsJsonObject(); //Starting from 0
                        if (choice.has("content")) {
                            String message = choice.get("content").getAsString()
                                    .replaceAll("\"", "")
                                    .replace("\\", "")
                                    .replace("\\n\\n", "\n");
                            message = convertUnicodeEmojis(message);
                            getMessage = message;                      //Assign message, provide external link
                        }
                    }
                }
            }
```

#### Synchronised request transfer model (InvokeModel: recommended)

Compared to **SSE streaming**, this **synchronous request** is quite good, running without missing characters **BUG**, speed compared to **asynchronous request** is not bad, the disadvantage of synchronous is that the amount of requests is too large may block the thread (`single-threaded`)

Here directly on the handling of information on this piece, this piece is parsing **JSON ** there is nothing else, sample code:

```
if (isJsonResponse(connection)) {
            JsonObject jsonResponse = JsonParser.parseString(responseData).getAsJsonObject();
            if (jsonResponse.has("data")) {  
                JsonObject data = jsonResponse.getAsJsonObject("data");      
                if (data.has("choices")) {
                    JsonArray choices = data.getAsJsonArray("choices");      
                    for (int i = 0; i < choices.size(); i++) {           
                        JsonObject choice = choices.get(i).getAsJsonObject();
                        if (choice.has("content")) {
                            String Message = choice.get("content").getAsString(); 
                            Message = Message.replaceAll("\"", "");
                            Message = Message.replace("\\n\\n", "\n");
                            Message = Message.replace("\\", "");
                            Message = convertUnicodeEmojis(Message);  
                            contentMessage = Message;
                        }
                    }
                }
            }
        }
```

> Overall down, the introduction of this project three ways to request should still be relatively simple, the current **BUG** can only try to fix 🥳, but also hope that all the gods of the support of this project! Thanks again 🎉!
---

## 4.Conclusion
>
> Thank you for opening my project, although I'm not very good at writing, but I'm also trying to develop this project, when you ask me why I do not use the official project, I want to say that in fact, this is also in the challenge of self (repeated building wheels), the official development of the official development is certainly a lot more than my personal development of the perfection of my personal development, but I'll continue to adhere to it, when the use of the efficiency of the official better than the official time, I think this project I consider this project a successful learning experience. I will keep updating this project. Also I hope more and more people will participate together 🚀 Thanks for seeing it to the end! 😆👏

----
**Last thanks to the jar developers of gson and the jar developers of Apache** 👩‍💻👨‍💻
