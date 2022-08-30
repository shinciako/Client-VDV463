package com.vdv463.client;

import com.vdv463.client.communication.Message;
import com.vdv463.client.communication.MessageHandler;
import pl.com.ekoenergetyka.vdv463.messages.ProvideChargingInformationResponse;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.websocket.pojo.PojoEndpointClient;
import org.springframework.web.bind.annotation.RestController;
import javax.websocket.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@Slf4j
@ClientEndpoint
@NoArgsConstructor
public class WebSocketClient {
    public Session session;
    WebSocketClient.ConnectionConfig wsConfig;

    private final List<MessageHandler> messageHandlers = new ArrayList<>();

    {
        List<String> protocols = new ArrayList<>(List.of("v1.463.vdv.de"));
        wsConfig = WebSocketClient.ConnectionConfig.builder()
                .tls(false)
                .server("dev.eos-dev.ekoenergetyka.com.pl/api/se/vdv463/ws")
                .subprotocols(protocols)
                .build();
    }

    public void sendCall(Message message) {
        try {
            if (session.isOpen()) {
                session.getBasicRemote().sendText(message.serialize());
                log.info(message.serialize());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Data
    @Builder
    public static class ConnectionConfig {
        private boolean tls;
        private String server;
        private List<String> subprotocols;

    }

    public void connect(ConnectionConfig config) throws Exception {
        WebSocketClient.connect(this, config);
    }

    public static void connect(WebSocketClient webSocketClient, ConnectionConfig config) throws Exception {
        var protocol = config.tls ? "wss" : "ws";
        var endpointURI = URI.create(protocol + "://" + config.server);
        webSocketClient.session = getSession(webSocketClient, endpointURI, config);
    }

    private static Session getSession(WebSocketClient webSocketClient, URI endpointURI, ConnectionConfig connectionConfig) throws Exception {
        var container = ContainerProvider.getWebSocketContainer();
        var clientConfig = createClientConfig(connectionConfig);
        return container.connectToServer(new PojoEndpointClient(webSocketClient, new ArrayList<>()), clientConfig, endpointURI);
    }

    private static ClientEndpointConfig createClientConfig(ConnectionConfig connectionConfig) {
        var builder = ClientEndpointConfig.Builder.create();
        return builder.decoders(new ArrayList<>()).encoders(new ArrayList<>())
                .preferredSubprotocols(connectionConfig.subprotocols).build();
    }

    public void close() throws IOException {
        session.close();
    }

    @OnOpen
    public void onOpen(Session session) {
        log.info("Connected. Id - {}", session.getId());
    }

    @OnMessage
    public void onMessage(String payload, Session session) {
        log.info("Message {} from session {}", payload, session.getId());
        Message message = Message.parse(payload);
        if (Objects.equals(message.getAction(), "BootNotification")) {
            notifyEveryone(message);
        } else if (Objects.equals(message.getAction(), "ProvideChargingInformation")) {
            notifyEveryone(message);
            Message mes = Message.builder()
                    .type(Message.Type.CONFIRMATION)
                    .source("BMS")
                    .presystemId(message.getPresystemId())
                    .timeStamp(ZonedDateTime.now())
                    .id(UUID.randomUUID().toString())
                    .action("ProvideChargingInformation")
                    .objectPayload(new ProvideChargingInformationResponse())
                    .build();
            sendCall(mes);
            notifyEveryone(mes);
        } else
            notifyEveryone(message);
    }

    @OnClose
    public void onClose(CloseReason closeReason) {
        log.info("Session id {} closed", session.getId());
        log.info("{}", closeReason);
    }

    public void attach(MessageHandler messageHandler) {
        messageHandlers.add(messageHandler);
    }

    public void notifyEveryone(Message message) {
        for (MessageHandler messageHandler : messageHandlers) {
            messageHandler.onMessage(message);
        }
    }
}








