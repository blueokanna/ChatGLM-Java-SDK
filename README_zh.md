# 智谱 AI 大模型自定义 ChatGLM4-Java-SDK - [English Doc](https://github.com/AstralQuanta/ChatGLM-Java-SDK/blob/main/README.md)
>
> 此项目是由 **Java** 的 **JDK17** 的长期版本开发
----
## ⚠️请注意😟！原本 **0.0.1** 的已经不再适用了，最后一个全新版本是 **0.1.1**

**Java Maven Dependency (BlueChatGLM)调用**
```
<dependency>
  <groupId>top.pulselink</groupId>
  <artifactId>bluechatglm</artifactId>
  <version>0.1.1</version>
</dependency>
```

**Java Gradle (BlueChatGLM)调用**
```
implementation group: 'top.pulselink', name: 'bluechatglm', version: '0.1.1'
```

**Java sbt (BlueChatGLM)调用**
```
libraryDependencies += "top.pulselink" % "bluechatglm" % "0.1.1"
```


## 1. Utils 工具

### 1.1 NTP 网络时间服务器

它通过互联网或局域网上的时间服务器来提供高精度，高安全的时间信息，确保所有设备都使用相同的时间是关键的。这里的应用是对于 `JWT` 验证使用

```
//获取网络时间协议服务器（NTP Server）

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
### 1.2 保存 API 密钥

保存 **API** 密钥并将其存储在调用 `chatglm_api_key` txt 文件的本地文件中：

```
    private static String loadApiKey() {                    //加载 API 密钥
        try (BufferedReader reader = new BufferedReader(new FileReader(API_KEY_FILE))) {
            return reader.readLine();
        } catch (IOException e) {
            return null; // If the file doesn't exist or an error occurs, return null
        }
    }

    private static void saveApiKey(String apiKey) {           //保存 API 密钥
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(API_KEY_FILE))) {
            writer.write(apiKey);
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }
    }
```

----

## 2. 易于使用的 SDK

### 2.1 调用并使用 Maven 库
>
> 相对于很多人来说，使用这个 **SDK** 的难度较低🤩。以下的三个示例是使用 **Scanner** 输入你的问题，控制台将输出 **ChatGLM** 回答：

调用**SSE请求**，示例代码如下 (目前已解决无法输入中文等问题，可以正常使用)：

```
public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String apiKeyss = loadApiKey();                          //加载 API 密钥

        if (apiKeyss == null) {                                  //如果不存在文件或者密钥为空，则需要输入密钥
            System.out.println("Enter your API key:");
            apiKeyss = scanner.nextLine();
            saveApiKey(apiKeyss);
        }

        while (scanner.hasNext()) {
            String userInput = scanner.nextLine();

             ChatClient chats = new ChatClient(apiKeyss);      //初始 ChatClient （实例化）
             chats.SSEInvoke(userInput);                    //将你输入的问题赋值给流式请求的
             System.out.print(chats.getResponseMessage()); //打印出 ChatGLM 的回答内容
            System.out.println();
        }
    }
```

调用**异步请求**，示例代码如下：

```
public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String apiKeyss = loadApiKey();                          //加载 API 密钥

        if (apiKeyss == null) {                                  //如果不存在文件或者密钥为空，则需要输入密钥
            System.out.println("Enter your API key:");
            apiKeyss = scanner.nextLine();
            saveApiKey(apiKeyss);
        }
        while (scanner.hasNext()) {
            String userInput = scanner.nextLine();

             ChatClient chats = new ChatClient(apiKeyss);      //初始 ChatClient （实例化）
             chats.AsyncInvoke(userInput);                    //将你输入的问题赋值给异步请求的
             System.out.print(chats.getResponseMessage()); //打印出 ChatGLM 的回答内容
            System.out.println();
        }
    }
```

调用**同步请求**，示例代码如下：

```
public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String apiKeyss = loadApiKey();                          //加载 API 密钥

        if (apiKeyss == null) {                                  //如果不存在文件或者密钥为空，则需要输入密钥
            System.out.println("Enter your API key:");
            apiKeyss = scanner.nextLine();
            saveApiKey(apiKeyss);
        }
        while (scanner.hasNext()) {
            String userInput = scanner.nextLine();

             ChatClient chats = new ChatClient(apiKeyss);      //初始 ChatClient （实例化）
             chats.SyncInvoke(userInput);                    //将你输入的问题赋值给同步请求的
             System.out.print(chats.getResponseMessage()); //打印出 ChatGLM 的回答内容
            System.out.println();
        }
    }
```

### 2.2 资深开发者👨🏼‍💻

**对于资深开发者，我们会后续跟进开发工作，目前的版本是ChatGLM-4的语言模型版本，并且已经解决了SSE中文输入看不懂的问题，当然我们也希望其他的 开发商为本项目提供技术支持！ 先感谢您！**

----

## 3.项目介绍

### **CustomJWT** 是对于这个项目的自定制而写的，后期会继续开发更新，拓展这个项目

根据 **JWT.io** 这个网站进行了解以及原理的学习，对于这个项目的**JWT** 验证，**Java**实现起来还是较容易实现的，其中使用的部分是 `Base64Url` 而不是常规的 `Base64`

**编码 Base64Url** 使用的编辑如下：

```
private String encodeBase64Url(byte[] data) {
        String base64url = Base64.getUrlEncoder().withoutPadding().encodeToString(data)；  //将输入的内容转换成 Base64Url
        return base64url;             //返回 base64url
    }
```

----
创建 **JWT**，实现 **Header** 验证：

```
protected String createJWT() {
        String encodedHeader = encodeBase64Url(header.getBytes());
        String encodedPayload = encodeBase64Url(payload.getBytes());
        String toSign = encodedHeader + "." + encodedPayload;

        byte[] signatureBytes = generateSignature(toSign, secret, algorithm);
        String calculatedSignature = encodeBase64Url(signatureBytes);
        return toSign + "." + calculatedSignature;
    }
```

----
验证 **JWT** 签名部分是否与输出的结果一致：

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

### 请求调用🌐

在**同步请求**和**SSE请求**中使用的请求方式如下：


```
HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json;charset=UTF-8")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody))
                .build();
```

使用 **POST** 方法制作 **jsonRequestBody** ，如下文所示（同步方法的 `Stream 为 false`）：

```
String jsonRequestBody = String.format("{\"model\":\"%s\", \"messages\":[{\"role\":\"%s\",\"content\":\"%s\"},{\"role\":\"%s\",\"content\":\"%s\"}], \"stream\":false,\"temperture\":%f,\"top_p\":%f}", Language_Model, system_role, system_content, user_role, message, temp_float, top_p_float);
```


#### SSE 流式传输模型（可以正常使用！完美支持）

这里我们将使用 **concurrent.Flow** 方法来解决SSE流处理的问题：

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
**并在此处调用并接收 chatglm-4 消息：**
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

#### 异步请求传输模型（AsyncInvokeModel：推荐使用，速度快）

这里采用的是`HTTPRequest`方法，来接收消息：

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

整体使用的是异步发送信息，这样的好处是可以减少线程阻塞，这里的`code`和`status`是获取错误消息。当你得到一个`request_id` 的时候，再进行查询

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

当你得到需要的**Task_id**的时候，进行**GET**请求查询(部分代码)：

```
                .....略 .sendAsync(HttpRequest.newBuilder()
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

最后通过**JSON**的提取，提取代码示例为：

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

#### 同步请求传输模型（InvokeModel：推荐使用，速度较快）

**同步请求**还算不错,运行的时候一般情况下都还算快，当然同步的缺点就是请求量过大可能会阻塞线程（`单线程`）

这里直接说明关于处理信息这一块，这一块就是解析**JSON**也没有其他的东西了，示例代码：

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

> 总体下来，介绍本项目三种请求方式应该还是相对简单，如果有任何问题，可以在 **Issue** 处发起讨论🥳，也希望各路大神的对这个项目的支援！再次感谢🎉！
---

## 4.结语
>
> 谢谢你打开我的项目，这是一个第三方开发的 ChatGLM SDK 开发项目，我也在尝试开发和更新这个项目，官方开发肯定比我个人开发要完善很多，当然我个人也会继续坚持开发下去，当使用效率的时候 官方比官方时间更好，我认为这个项目我认为这个项目是一次成功的学习经历。 我会不断更新这个项目。 也希望越来越多的人一起参与🚀 谢谢你们看到最后！😆👏

----
**最后的最后感恩 gson 的 jar 包开发人员**👩‍💻👨‍💻
