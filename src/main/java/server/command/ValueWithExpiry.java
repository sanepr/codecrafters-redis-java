package server.command;

public class ValueWithExpiry {
    private final String value;
    private final long expiryTimeMillis; // 0 = no expiry

    public ValueWithExpiry(String value, long expiryTimeMillis) {
        this.value = value;
        this.expiryTimeMillis = expiryTimeMillis;
    }

    public String getValue() {
        return value;
    }

    public boolean isExpired() {
        return expiryTimeMillis > 0 && System.currentTimeMillis() > expiryTimeMillis;
    }
}
