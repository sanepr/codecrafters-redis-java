package server.data;

public abstract class RedisObject {
    private final long expiryTimeMillis; // 0 = no expiry

    protected RedisObject(long expiryTimeMillis) {
        this.expiryTimeMillis = expiryTimeMillis;
    }

    public boolean isExpired() {
        return expiryTimeMillis > 0 && System.currentTimeMillis() > expiryTimeMillis;
    }
}
