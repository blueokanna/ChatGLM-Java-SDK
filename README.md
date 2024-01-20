# Zhipu AI Large Model Custom ChatGLM4-Java-SDK - [中文文档](https://github.com/AstralQuanta/ChatGLM-Java-SDK/blob/main/README_zh.md)
>
> ChatGLM4-Java-SDK, a Java-based open interface for customised spectral macromodels, developed by **Java** in the long term version of **JDK17**.
----
## ⚠️Caution😟！The original **0.0.1** is no longer available! The Latest Version is 0.1.0.

**Java Maven Dependency (BlueChatGLM)**
```
<dependency>
  <groupId>top.pulselink</groupId>
  <artifactId>bluechatglm</artifactId>
  <version>0.1.0</version>
</dependency>
```

**Java Gradle (BlueChatGLM)**
```
implementation group: 'top.pulselink', name: 'bluechatglm', version: '0.1.0'
```

**Java sbt (BlueChatGLM)**
```
libraryDependencies += "top.pulselink" % "bluechatglm" % "0.1.0"
```

## 1.Using NTP Server Time

It provides highly accurate and secure time information via time servers on the Internet or LAN, and it is critical to ensure that all devices use the same time. The application here is for `JWT` authentication using the

```
//Get Network Time Protocol Server（NTP Server）

    private long getNTPTime() throws IOException {
        int port = 123;
        InetAddress address = InetAddress.getByName("ntp.aliyun.com");

        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] buf = new byte[48];
            buf[0] = 0x1B;
            DatagramPacket requestPacket = new DatagramPacket(buf, buf.length, address, port);
            socket.send(requestPacket);

            DatagramPacket responsePacket = new DatagramPacket(new byte[48], 48);
            socket.receive(responsePacket);

            byte[] rawData = responsePacket.getData();
            long secondsSince1900 = 0;
            for (int i = 40; i <= 43; i++) {
                secondsSince1900 = (secondsSince1900 << 8) | (rawData[i] & 0xff);
            }
            return (secondsSince1900 - 2208988800L) * 1000;
        }
    }
```

----

## 2. Easy-to-use SDK

### 2.1 Calling and Using the Maven Library
>
> Using this project **SDK** is less difficult 🤩. The following three examples use **Scanner** to enter your question and the console will output **ChatGLM** to answer it：

Call **SSE request**, the sample code is as follows:

```
public class Main{
    public static void main(String[] args) {
        String apiKeyss = //Replace your own API key,You can find it from https://open.bigmodel.cn/usercenter/apikeys

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
        String apiKeyss = "Your_API_Key"; //Replace your own API key,You can find it from https://open.bigmodel.cn/usercenter/apikeys

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
        String apiKeyss = "Your_API_Key"; //Replace your own API key,You can find it from https://open.bigmodel.cn/usercenter/apikeys

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

**For senior developers, we will follow up the development work in the future, the current version is the language model version of ChatGLM-4, and has solved the problem of SSE Chinese input can not be read, of course, we also hope that other developers to provide technical support for this project! Thank you in advance!**

----

## 3.Project Description

### **CustomJWT** is for the project's self-customisation and write, later will continue to develop and expand the project!

According to **JWT.io** this website for understanding and principle of learning, for this project of **JWT** validation, **Java** implementation is easier to achieve, which uses the part of the `Base64Url` instead of the conventional `Base64`.

**Encoding Base64Url** used by the editor is as follows:

```
private String encodeBase64Url(byte[] data) {
        String base64url = Base64.getUrlEncoder().withoutPadding().encodeToString(data) //convert the input to Base64Url
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
HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json;charset=UTF-8")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody))
                .build();
```

Make **jsonRequestBody** that using **POST** method which context like below (Synchronous with `false of Stream`):

```
String jsonRequestBody = String.format("{\"model\":\"%s\", \"messages\":[{\"role\":\"%s\",\"content\":\"%s\"},{\"role\":\"%s\",\"content\":\"%s\"}], \"stream\":false,\"temperture\":%f,\"top_p\":%f}", Language_Model, system_role, system_content, user_role, message, temp_float, top_p_float);
```

#### SSE Streaming Transfer Model

Here we will use the **concurrent.Flow** method to solve the problem of SSE stream processing:

```
public class SSESubscriber implements Flow.Subscriber<String> {

        private Flow.Subscription subscription;

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            subscription.request(1);
        }

        @Override
        public void onNext(String item) {
            if (item.startsWith("data: ")) {
                String jsonData = item.substring("data: ".length());
                //System.out.println("Received SSE item: " + jsonData); //Debug

                if (!jsonData.equals("[DONE]")) {
                    responseDataBody(jsonData.replaceAll("Invalid JSON format: \\[\"DONE\"\\]", ""));
                }
            }
            subscription.request(1);
        }

        @Override
        public void onError(Throwable throwable) {
            System.out.println("Error in SSESubscriber: " + throwable.getMessage());
        }

        @Override
        public void onComplete() {
            //System.out.println("SSESubscriber completed");
        }
    }
```
**And the calling to receive chatglm-4 message here:**
```
try (JsonReader jsonReader = new JsonReader(new StringReader(responseData))) {
            jsonReader.setLenient(true);
            JsonElement jsonElement = JsonParser.parseReader(jsonReader);

            if (jsonElement.isJsonObject()) {
                JsonObject jsonResponse = jsonElement.getAsJsonObject();

                if (jsonResponse.has("choices")) {
                    JsonArray choices = jsonResponse.getAsJsonArray("choices");

                    if (!choices.isEmpty()) {
                        JsonObject choice = choices.get(0).getAsJsonObject();

                        if (choice.has("delta")) {
                            JsonObject delta = choice.getAsJsonObject("delta");

                            if (delta.has("content")) {
                                String content = delta.get("content").getAsString();
                                getMessage = convertUnicodeEmojis(content);
                                getMessage = getMessage.replaceAll("\"", "")
                                        .replaceAll("\\\\n\\\\n", "\n")
                                        .replaceAll("\\\\nn", "\n")
                                        .replaceAll("\\n", "\n")
                                        .replaceAll("\\\\", "")
                                        .replaceAll("\\\\", "");

                                for (char c : getMessage.toCharArray()) {
                                    charQueue.offer(c);
                                }

                                while (!charQueue.isEmpty()) {
                                    queueResult.append(charQueue.poll());
                                }
                            }
                        }
                    }
                }
            } else {
                System.out.println("Invalid JSON format: " + jsonElement);
            }
        } catch (IOException e) {
            System.out.println("Error reading JSON: " + e.getMessage());
        }
```


#### Asynchronous Request Transfer Model

The `HTTPRequest` method is used here to receive the message:

```
String jsonRequestBody = String.format("{\"model\":\"%s\", \"messages\":[{\"role\":\"%s\",\"content\":\"%s\"},{\"role\":\"%s\",\"content\":\"%s\"}],\"temperture\":%f,\"top_p\":%f}",
                Language_Model, system_role, system_content, user_role, message, temp_float, top_p_float);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json;charset=UTF-8")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody))
                .build();
```

The overall use is to send messages asynchronously, which has the advantage of reducing thread blocking, where `code` and `status` are getting error messages. When you get a `request_id`, then query the

```
                    if (response.statusCode() == 200) {      //When the response value is 200, output the corresponding parameters of the interface for an asynchronous request.
                        processResponseData(response.body());
                        return CompletableFuture.completedFuture(response.body());
                    } else {
                        JsonObject errorResponse = JsonParser.parseString(response.body()).getAsJsonObject();
                        if (errorResponse.has("id") && errorResponse.has("task_status")) {
                            int code = errorResponse.get("id").getAsInt();
                            String status = errorResponse.get("task_status").getAsString();
                            throw new RuntimeException("HTTP request failure, Your request id is: " + code + ", Status: " + status);
                        } else {
                            return CompletableFuture.failedFuture(new RuntimeException("HTTP request failure, Code: " + response.statusCode()));
                        }
                    }
                });
```

When you get the **Task_id** you need, make a **GET** request query (part of the code):

```
                ..... .sendAsync(HttpRequest.newBuilder()
                        .uri(URI.create(checkUrl + ID))
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .header("Authorization", "Bearer " + token)
                        .GET()
                        .build(), HttpResponse.BodyHandlers.ofString())
                .thenCompose(response -> {
                    if (response.statusCode() == 200) {
                        return CompletableFuture.completedFuture(response.body());
                    } else {
                        return CompletableFuture.failedFuture(new RuntimeException("HTTP request failure, Code: " + response.statusCode()));
                    }
                });
```

Finally the extraction by **JSON**, the sample extraction code is:

```
try {
            JsonObject jsonResponse = JsonParser.parseString(responseData).getAsJsonObject();

            if (jsonResponse.has("choices")) {
                JsonArray choices = jsonResponse.getAsJsonArray("choices");

                if (!choices.isEmpty()) {
                    JsonObject choice = choices.get(0).getAsJsonObject();

                    if (choice.has("message")) {
                        JsonObject message = choice.getAsJsonObject("message");

                        if (message.has("content")) {
                            String content = message.get("content").getAsString();
                            getMessage = convertUnicodeEmojis(content);
                            getMessage = getMessage.replaceAll("\"", "")
                                    .replaceAll("\\\\n\\\\n", "\n")
                                    .replaceAll("\\\\nn", "\n")
                                    .replaceAll("\\n", "\n")
                                    .replaceAll("\\\\", "")
                                    .replaceAll("\\\\", "");
                        }
                    }
                }
            }
        } catch (JsonSyntaxException e) {
            System.out.println("Error processing task status: " + e.getMessage());
        }
```

#### Synchronised request transfer model

**synchronous request** is quite good, speed compared to **asynchronous request** is not bad, the disadvantage of synchronous is that the amount of requests is too large may block the thread (`single-threaded`)

Here directly on the handling of information on this piece, this piece is parsing **JSON ** there is nothing else, sample code:

```
 try {
            JsonObject jsonResponse = JsonParser.parseString(responseData).getAsJsonObject();

            if (jsonResponse.has("choices")) {
                JsonArray choices = jsonResponse.getAsJsonArray("choices");

                if (!choices.isEmpty()) {
                    JsonObject choice = choices.get(0).getAsJsonObject();

                    if (choice.has("message")) {
                        JsonObject message = choice.getAsJsonObject("message");

                        if (message.has("content")) {
                            String content = message.get("content").getAsString();
                            getMessage = convertUnicodeEmojis(content);
                            getMessage = getMessage.replaceAll("\"", "")
                                    .replaceAll("\\\\n\\\\n", "\n")
                                    .replaceAll("\\\\nn", "\n")
                                    .replaceAll("\\n", "\n")
                                    .replaceAll("\\\\", "")
                                    .replaceAll("\\\\", "");
                        }
                    }
                }
            }
        } catch (JsonSyntaxException e) {
            System.out.println("Error processing task status: " + e.getMessage());
        }
```

> Overall down, the introduction of this project three ways to request should still be relatively simple, the current **BUG** can only try to fix 🥳, but also hope that all the gods of the support of this project! Thanks again 🎉!
---

## 4.Conclusion
>
> Thank you for opening my project, this is a third party development ChatGLM SDK development project, I am also trying to develop and update this project, the official development is certainly much more perfect than my personal development, of course, I personally will continue to insist on the development of the time when the use of the efficiency of the official better than the official time, I think that this project I think that this project is a successful learning experience. I will continue to update this project. I also hope that more and more people will participate together 🚀 Thank you for seeing it to the end!😆👏

----
Last thanks to the jar developers of **gson** 👩‍💻👨‍💻
