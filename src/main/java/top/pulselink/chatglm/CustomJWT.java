package top.pulselink.chatglm;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

public final class CustomJWT {

    private final String secret;
    private final String algorithm;
    private final String header;
    private final String payload;
    private final String ntpServer = "ntp.aliyun.com";
    private final int timeout = 5000;
    private long lastServerTime = 0;
    private long lastUpdateTime = 0;

    public CustomJWT(String userID, String userSecret, String algorithm) {
        this.algorithm = algorithm;
        this.secret = userSecret;
        this.header = "{\"alg\":\"HS256\",\"typ\":\"JWT\",\"sign_type\":\"SIGN\"}";
        this.payload = JWTPayload(userID);
    }

    protected String createJWT() {
        String encodedHeader = encodeBase64Url(header.getBytes());
        String encodedPayload = encodeBase64Url(payload.getBytes());
        String toSign = encodedHeader + "." + encodedPayload;

        byte[] signatureBytes = generateSignature(toSign, secret, algorithm);
        String calculatedSignature = encodeBase64Url(signatureBytes);
        return toSign + "." + calculatedSignature;
    }

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

    protected String JWTPayload(String userID) {
        long timeNow = receiveTime();
        long expTime = timeNow * 10;
        return String.format("{\"api_key\":\"%s\",\"exp\":%d,\"timestamp\":%d}", userID, expTime, timeNow);
    }

    private byte[] generateSignature(String data, String secret, String algorithm) {
        try {
            Mac hmac = Mac.getInstance(algorithm);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), algorithm);
            hmac.init(secretKeySpec);
            return hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String encodeBase64Url(byte[] data) {
        String base64url = Base64.getUrlEncoder().withoutPadding().encodeToString(data)
                .replace("+", "-")
                .replace("/", "_");
        return base64url;
    }

    protected long receiveTime() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime < 60000) {
            return lastServerTime;
        } else {
            try {
                NTPUDPClient timeClient = new NTPUDPClient();
                timeClient.setDefaultTimeout(timeout);
                InetAddress inetAddress = InetAddress.getByName(ntpServer);
                TimeInfo timeInfo = timeClient.getTime(inetAddress);
                long serverTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();
                lastServerTime = serverTime;
                lastUpdateTime = currentTime;
                return serverTime;
            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch NTP time", e);
            }
        }

    }
}
