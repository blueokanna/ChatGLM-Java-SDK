# 智谱 AI 大模型自定义 ChatGLM-Java-SDK - [English Doc](https://github.com/AstralQuanta/ChatGLM-Java-SDK/blob/main/README.md)
>
> 此项目是由 **Java** 的 **JDK17** 的长期版本开发
----
## ⚠️请注意😟！原本 **0.0.1** 的已经不再适用了，最后一个全新版本是 **0.0.3**

**Java Maven Dependency (BlueChatGLM)调用**
> 请使用 **Java Maven** 调用这个库✔️，**Java Ant** 使用这个似乎会报错❌

```
<dependency>
  <groupId>top.pulselink</groupId>
  <artifactId>bluechatglm</artifactId>
  <version>0.0.3</version>
</dependency>
```

## 1.使用 NTP 服务器时间

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

----

## 2. 易于使用的 SDK

**本项目唯一一个不变的量：`algorithm = HmacSHA256`**

### 2.1 调用并使用 Maven 库
>
> 相对于很多人来说，使用这个 **SDK** 的难度较低🤩。以下的三个示例是使用 **Scanner** 输入你的问题，控制台将输出 **ChatGLM** 回答：

调用**SSE请求**，示例代码如下 `（此示例对英文输出比较友好，中文输出有问题）`：

```
public class Main{
    public static void main(String[] args) {
        String apiKeyss = "Your_API_Key"; //替换成自己的 API Key

        Scanner scan = new Scanner(System.in); //利用 Scanner 输入内容
        while (scan.hasNext()) {
             String userInput = scan.nextLine();
             ChatClient chats = new ChatClient(apiKeyss);      //初始 ChatClient （实例化）
             chats.SSEInvoke(userInput);                    //将你输入的问题赋值给 SSE 请求的
             System.out.println(chats.getResponseMessage()); //打印出 ChatGLM 的回答内容
        }
    }
}
```

调用**异步请求**，示例代码如下：

```
public class Main{
    public static void main(String[] args) {
        String apiKeyss = "Your_API_Key"; //替换成自己的 API Key

        Scanner scan = new Scanner(System.in); //利用 Scanner 输入内容
        while (scan.hasNext()) {
             String userInput = scan.nextLine();
             ChatClient chats = new ChatClient(apiKeyss);      //初始 ChatClient （实例化）
             chats.AsyncInvoke(userInput);                    //将你输入的问题赋值给异步请求的
             System.out.println(chats.getResponseMessage()); //打印出 ChatGLM 的回答内容
        }
    }
}
```

调用**同步请求**，示例代码如下：

```
public class Main{
    public static void main(String[] args) {
        String apiKeyss = "Your_API_Key"; //替换成自己的 API Key

        Scanner scan = new Scanner(System.in); //利用 Scanner 输入内容
        while (scan.hasNext()) {
             String userInput = scan.nextLine();
             ChatClient chats = new ChatClient(apiKeyss);      //初始 ChatClient （实例化）
             chats.SyncInvoke(userInput);                    //将你输入的问题赋值给同步请求的
             System.out.println(chats.getResponseMessage()); //打印出 ChatGLM 的回答内容
        }
    }
}
```

### 2.2 资深开发者👨🏼‍💻

对于资深开发者，目前此版本只是做了一个较为简单的开发，还有 `temperature` , `top_p` , `incremental` , `return_type` 等参数没有添加到这一次的开发。后期的话这边也会跟进开发的脚步，当然这边也是非常希望其他开发者对本项目提供技术支持！在这里先感谢各位了！

----

## 3.项目介绍

### **CustomJWT** 是对于这个项目的自定制而写的，后期会继续开发，拓展这个项目

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

在**同步请求**和**SSE请求**中使用的请求方式如下（在**Header**里面）：

```
connection.setRequestMethod("POST");
connection.setRequestProperty("Accept", "application/json");        //同步请求
//connection.setRequestProperty("Accept", "text/event-stream");    //SSE请求
connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
connection.setRequestProperty("Authorization", "Bearer " + token);
```

使用**gson**的库，让**Payload**写入**JSON**里面

```
JsonObject payloadMessage = new JsonObject();
payloadMessage.addProperty("prompt", message); //添加属性
```

> 一般来说 prompt -> message 就可以了，如果想要添加其他部分的属性这里也可以添加，比如添加关于**temperature**，**top_p**：

```
payloadMessage.addProperty("temperature", 0.6);
payloadMessage.addProperty("top_p", 0.7);
```

#### SSE 流式传输模型（SSEInvokeModel：目前不完善，存在一定的BUG，不推荐使用）

这里使用的是一个对每一 **SSE 流式** 的生成，一般获取得到的内容包含`：event`,`request_id`以及`data`。对于`add`后的数据，拼接在一起，这里使用队列的方法排序：

```
//队列： BlockingQueue<String> resultQueue = new ArrayBlockingQueue<>(2000);
//将你的'Content' 数据加入队列：resultQueue.offer(dataBuilder.toString());

/*
分割（data：Content）
*/
            String[] pair = keyValue.split(":");
            if (pair.length == 2) {
                String key = pair[0].trim();
                String value = pair[1].trim();
                eventData.addProperty(key, value);
            }
```

对于 `meta`来说，这个是可以后期添加的，代码示例如下：

```
        if (line.startsWith("data: ")) {  // (line = reader.readLine()) != null 传入
            String data = line.substring(6).trim();
            JsonObject eventData = parseEventData(data);

            String eventType = eventData.has("event") ? eventData.get("event").getAsString() : null;
            String eventDataString = eventData.has("data") ? eventData.get("data").getAsString() : null;

            if ("add".equals(eventType)) {          //add事件
                System.out.println("Add Event: " + eventDataString);
            } else if ("error".equals(eventType) || "interrupted".equals(eventType)) {
                System.out.println("Error or Interrupted Event: " + eventDataString);
            } else if ("finish".equals(eventType)) {
                System.out.println("Finish Event: " + eventDataString);

                if (eventData.has("meta")) {        // meta数据
                    JsonObject meta = eventData.getAsJsonObject("meta");
                    System.out.println("Meta: " + meta.toString());

                    if (meta.has("usage")) {         //使用量（Token数量）的输出
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

#### 异步请求传输模型（AsyncInvokeModel：推荐使用，速度快）

这里采用的是`HTTPRequest`方法，来接收消息：

```
HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Accept", "application/json")  //请求头
                .header("Content-Type", "application/json;charset=UTF-8")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString("{\"prompt\":\"" + message + "\"}"))  //Payload 部分 -> 对应用户输入的信息
                .build();
```

整体使用的是异步发送信息，这样的好处是可以减少线程阻塞，这里的`code`和`msg`是获取错误消息。当你得到一个`request_id` 的时候，再进行查询

```
                    if (response.statusCode() == 200) {      //当得到响应值是 200 的时候，输出一个异步请求的接口相应参数
                        processResponseData(response.body());
                        return CompletableFuture.completedFuture(response.body());
                    } else {

                        JsonObject errorResponse = JsonParser.parseString(response.body()).getAsJsonObject(); //不是 200，则放回错误的信息
                        if (errorResponse.has("code") && errorResponse.has("msg")) {
                            int code = errorResponse.get("code").getAsInt();
                            String msg = errorResponse.get("msg").getAsString();
                            throw new RuntimeException("HTTP request failure, Code: " + code + ", Message: " + msg);
                        } else {
                            return CompletableFuture.failedFuture(new RuntimeException("HTTP request failure, Code: " + response.statusCode()));
                        }
                    }        
```

当你得到需要的**Task_id**的时候，进行**GET**请求查询(部分代码)：

```
                .....略 .sendAsync(HttpRequest.newBuilder()
                        .uri(URI.create(checkUrl + TaskID)) //添加Taskid到查询地址
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .header("Authorization", "Bearer " + token)
                        .GET()
                        .build(), HttpResponse.BodyHandlers.ofString())
                .thenCompose(response -> {......略
                )};
```

最后通过**JSON**的提取，提取代码示例为：

```
JsonObject jsonResponse = JsonParser.parseString(responseData).getAsJsonObject();
            if (jsonResponse.has("data")) {
                JsonObject data = jsonResponse.getAsJsonObject("data");   //data 里面获取
                if (data.has("choices")) {
                    JsonArray choices = data.getAsJsonArray("choices");  //choices里面获取
                    if (!choices.isEmpty()) {
                        JsonObject choice = choices.get(0).getAsJsonObject(); //从第 0 个开始
                        if (choice.has("content")) {
                            String message = choice.get("content").getAsString()
                                    .replaceAll("\"", "")
                                    .replace("\\", "")
                                    .replace("\\n\\n", "\n");
                            message = convertUnicodeEmojis(message);
                            getMessage = message;                      //赋值message，提供外部链接
                        }
                    }
                }
            }
```

#### 同步请求传输模型（InvokeModel：推荐使用，速度较快）

相对于**SSE流式**来说，这个**同步请求**还算不错,运行的时候不会出现字符缺失的 **BUG**，速度相比于**异步请求**也不差，同步的缺点就是请求量过大可能会阻塞线程（`单线程`）

这里直接说明关于处理信息这一块，这一块就是解析**JSON**也没有其他的东西了，示例代码：

```
if (isJsonResponse(connection)) {
            JsonObject jsonResponse = JsonParser.parseString(responseData).getAsJsonObject();
            if (jsonResponse.has("data")) {  
                JsonObject data = jsonResponse.getAsJsonObject("data");      //data 里面获取
                if (data.has("choices")) {
                    JsonArray choices = data.getAsJsonArray("choices");      //choices里面获取
                    for (int i = 0; i < choices.size(); i++) {            //从第 0 个开始
                        JsonObject choice = choices.get(i).getAsJsonObject();
                        if (choice.has("content")) {
                            String Message = choice.get("content").getAsString();    //Content 内容获取
                            Message = Message.replaceAll("\"", "");
                            Message = Message.replace("\\n\\n", "\n");
                            Message = Message.replace("\\", "");
                            Message = convertUnicodeEmojis(Message);  
                            contentMessage = Message;                //赋值message，提供外部链接
                        }
                    }
                }
            }
        }
```

> 总体下来，介绍本项目三种请求方式应该还是相对简单，目前的 **BUG** 也只能尽量去修🥳，也希望各路大神的对这个项目的支援！再次感谢🎉！
---

## 4.结语
>
> 谢谢你打开我的项目，这是一个第三方开发的 ChatGLM SDK 开发项目，我也在尝试开发和更新这个项目，官方开发肯定比我个人开发要完善很多，当然我个人也会继续坚持开发下去，当使用效率的时候 官方比官方时间更好，我认为这个项目我认为这个项目是一次成功的学习经历。 我会不断更新这个项目。 也希望越来越多的人一起参与🚀 谢谢你们看到最后！😆👏

----
**最后的最后感恩 gson 的 jar 包开发人员以及 Apache 的 jar 包开发人员**👩‍💻👨‍💻
