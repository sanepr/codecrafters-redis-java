package server.data;

public class RedisString extends RedisObject {
    private final String value;

    public RedisString(String value, long expiryTimeMillis) {
        super(expiryTimeMillis);
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
