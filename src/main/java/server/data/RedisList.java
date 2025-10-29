package server.data;

import java.util.LinkedList;

public class RedisList extends RedisObject {
    private final LinkedList<String> values;

    public RedisList(long expiryTimeMillis) {
        super(expiryTimeMillis);
        this.values = new LinkedList<>();
    }

    public LinkedList<String> getValues() {
        return values;
    }

    public int append(String value) {
        values.add(value);
        return values.size();
    }
}
