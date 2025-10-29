package server.data;

import java.util.ArrayList;
import java.util.List;

public class RedisList extends RedisObject {
    private final List<String> values;

    public RedisList(long expiryTimeMillis) {
        super(expiryTimeMillis);
        this.values = new ArrayList<>();
    }

    public List<String> getValues() {
        return values;
    }

    public int append(String value) {
        values.add(value);
        return values.size();
    }
}
