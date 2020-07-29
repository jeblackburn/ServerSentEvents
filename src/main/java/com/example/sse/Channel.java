package com.example.sse;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

public interface Channel {
    Flux<ServerSentEvent<String>> createSubscriber();
    void sendString(String string);

    void closeConnection(String guid);
}
