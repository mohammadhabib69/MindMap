package com.mindmap.export;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonUtil {

    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.registerModule(new JavaTimeModule());
        // Configure to not fail on unknown properties for forward compatibility
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // Write dates as ISO-8601 strings, not timestamp arrays
        MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        // Pretty print output
        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static ObjectMapper getMapper() {
        return MAPPER;
    }
}
