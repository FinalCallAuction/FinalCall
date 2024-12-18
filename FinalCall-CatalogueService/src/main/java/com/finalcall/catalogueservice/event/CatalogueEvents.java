package com.finalcall.catalogueservice.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.web.socket.WebSocketSession;

public class CatalogueEvents {
    public static class ItemCreatedEvent extends ApplicationEvent {
        private final Long itemId;
        private final Object itemData;

        public ItemCreatedEvent(Object source, Long itemId, Object itemData) {
            super(source);
            this.itemId = itemId;
            this.itemData = itemData;
        }

        public Long getItemId() { return itemId; }
        public Object getItemData() { return itemData; }
    }

    public static class WebSocketEvent extends ApplicationEvent {
        private final String type;
        private final Object payload;
        private final WebSocketSession session;
        private final Long itemId;

        public WebSocketEvent(Object source, String type, Object payload, 
                            WebSocketSession session, Long itemId) {
            super(source);
            this.type = type;
            this.payload = payload;
            this.session = session;
            this.itemId = itemId;
        }

        public String getType() { return type; }
        public Object getPayload() { return payload; }
        public WebSocketSession getSession() { return session; }
        public Long getItemId() { return itemId; }
    }
}