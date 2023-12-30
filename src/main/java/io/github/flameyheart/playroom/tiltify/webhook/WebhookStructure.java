package io.github.flameyheart.playroom.tiltify.webhook;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.UUID;

public class WebhookStructure {
    public abstract static class Webhook {}
    public static class Money {
        public String currency;
        public float value;
    }
    public static class RewardClaim {
        public UUID id;
        public UUID rewardId;
        public int quantity;
        public String customQuestion;
    }
    public static class Meta {
        public String attemptedAt;
        public EventType eventType;
        public UUID id;
        public UUID subscriptionSourceId;
        public String subscriptionSourceType;
    }
    @JsonDeserialize(using = EventType.EventTypeDeserializer.class)
    public static class EventType {
        public String access;
        public String method;
        public String type;

        public EventType(String access, String method, String type) {
            this.access = access;
            this.method = method;
            this.type = type;
        }

        public static class EventTypeDeserializer extends StdDeserializer<EventType> {
            protected EventTypeDeserializer() {
                super(EventType.class);
            }

            @Override
            public EventType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String string = p.getValueAsString();
                String[] split = string.split(":");
                return new EventType(split[0], split[1], split[2]);
            }
        }
    }
}
