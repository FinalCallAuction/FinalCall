/**
 * A basic WebSocket handler that echoes received messages.
 */
package com.finalcall.auctionservice.websocket;

import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

public class WebSockHandler extends TextWebSocketHandler {

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws IOException {

        String receivedMessage = (String) message.getPayload();
        // Process the message and send a response if needed
        session.sendMessage(new TextMessage("Received: " + receivedMessage));
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {

    }

}