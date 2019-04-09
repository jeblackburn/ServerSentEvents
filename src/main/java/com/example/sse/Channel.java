package com.example.sse;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface Channel {

    Flux<ServerSentEvent<String>> giveTube();

    void sendString(String string);

    void authenticate(UUID uuid);
}
