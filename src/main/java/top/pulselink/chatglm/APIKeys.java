package top.pulselink.chatglm;

public class APIKeys {

    private final String userId;
    private final String userSecret;
    private static APIKeys instance;

    private APIKeys(String userId, String userSecret) {
        this.userId = userId;
        this.userSecret = userSecret;
    }

    public static APIKeys getInstance(String api) {
        if (instance == null) {
            String[] parts = api.trim().split("\\.");
            if (parts.length == 2) {
                instance = new APIKeys(parts[0], parts[1]);
            } else {
                throw new IllegalArgumentException("Your API Key is Invalid");
            }
        }
        return instance;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserSecret() {
        return userSecret;
    }
}
