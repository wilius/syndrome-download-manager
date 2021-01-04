package xdman.util;

import com.fasterxml.jackson.core.JsonProcessingException;

public class JsonUtil {
    private JsonUtil() {
    }

    public static byte[] writeValues(Object o) {
        try {
            return ObjectMapperFactory.instance.writeValueAsBytes(0);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
