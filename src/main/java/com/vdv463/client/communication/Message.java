package com.vdv463.client.communication;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.internal.StringUtil;


import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Data
@Builder
public class Message {

    public static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        ;
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    public static Message parse(String payload) {
        var messageBuilder = Message.builder();
        try {
            List<Object> parsed = MAPPER.readValue(payload, MAPPER.getTypeFactory().constructCollectionType(List.class, Object.class));
            if (parsed.size() == 7) {
                messageBuilder.type(checkMessageType(parsed));
                messageBuilder.source(getString(parsed, 1));
                messageBuilder.presystemId(getString(parsed, 2));
                messageBuilder.timeStamp(getZonedDateTime(parsed, 3));
                messageBuilder.id(getString(parsed, 4));
                messageBuilder.action(getString(parsed, 5));
                messageBuilder.payload(getMap(parsed, 6));
            }
        } catch (JsonProcessingException ex) {
            log.warn("Cannot parse payload '{}'.", payload, ex);
        }
        return messageBuilder.build();
    }

    private static String getString(List<Object> parsed, int i) {
        var object = parsed.get(i);
        if (object instanceof String) {
            return (String) object;
        }
        return null;
    }

    private static ZonedDateTime getZonedDateTime(List<Object> parsed, int i) {
        var object = parsed.get(i);
        if (object instanceof String) {
            try {
                return ZonedDateTime.parse((CharSequence) object);
            } catch (DateTimeParseException e) {
                log.warn("Cannot parse timestamp from {}", object);
            }
        }
        return null;
    }

    private static Map<String, Object> getMap(List<Object> parsed, int i) {
        var map = parsed.get(i);
        if (map == null) {
            return new HashMap<>();
        }
        return (Map<String, Object>) map;
    }

    private static Type checkMessageType(List<Object> parsed) {
        return parsed.get(0) instanceof Integer ? Type.getByNumber((Integer) parsed.get(0)) : null;
    }


    @RequiredArgsConstructor
    public enum Type {
        REQUEST(1),
        CONFIRMATION(2),
        ERROR(3);

        @Getter
        private final int messageTypeNumber;

        private static final Map<Integer, Type> messageTypesByNumber = initMessageTypesByNumber();

        private static Map<Integer, Type> initMessageTypesByNumber() {
            var messageTypesByNumber = new HashMap<Integer, Type>();
            for (var messageType : Type.values()) {
                messageTypesByNumber.put(messageType.getMessageTypeNumber(), messageType);
            }
            return messageTypesByNumber;
        }

        public static Type getByNumber(final int messageTypeNumber) {
            return messageTypesByNumber.get(messageTypeNumber);
        }

    }

    private Type type;
    private String source;
    private String presystemId;
    private ZonedDateTime timeStamp;
    private String id;
    private String action;
    private Map<String, Object> payload;
    private Object objectPayload;


    public String getId() {
        return id;
    }

    public final boolean isValid() {
        return type != null &&
                !StringUtil.isBlank(source) &&
                !StringUtil.isBlank(presystemId) &&
                timeStamp != null &&
                !StringUtil.isBlank(id) &&
                !StringUtil.isBlank(action) &&
                payload != null;
    }

    public final Map<String, Object> getPayload() {
        return payload;
    }

    public <T> T getPayload(Class<T> payloadClass) {
        return MAPPER.convertValue(payload, payloadClass);
    }

    public final String serialize() {
        var arrayNode = MAPPER.createArrayNode();
        arrayNode.add(type.getMessageTypeNumber());
        arrayNode.add(source);
        arrayNode.add(presystemId);
        arrayNode.add(MAPPER.valueToTree(timeStamp));
        arrayNode.add(id);
        arrayNode.add(action);
        var payloadNode = MAPPER.createObjectNode();
        if (objectPayload != null) {
            payloadNode = MAPPER.valueToTree(objectPayload);
        } else if (payload != null) {
            payloadNode = MAPPER.valueToTree(payload);
        }
        arrayNode.add(payloadNode);
        return arrayNode.toString();
    }
}