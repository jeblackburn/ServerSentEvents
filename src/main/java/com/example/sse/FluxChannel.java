package com.example.sse;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FluxChannel implements Channel {

    private Map<UUID, FluxSink<ServerSentEvent<String>>> sinksByGuid = new ConcurrentHashMap<>();

    @Override
    public Flux<ServerSentEvent<String>> createSubscriber() {

        UUID uuid = UUID.randomUUID();

        return Flux.create((FluxSink<ServerSentEvent<String>> fluxSink) -> {
            System.out.println("Callback from Flux for " + uuid);
            sinksByGuid.put(uuid, fluxSink);

            // force the issue more quickly if the client disconnects
            Disposable pingConsumer = Flux.interval(Duration.ofSeconds(2)).doOnNext(a -> {
                System.out.printf("Sending a Ping to %d consumers: %s\n", sinksByGuid.size(), uuid);
                fluxSink.next(ServerSentEvent.<String>builder().event("ping " + a).build());
            }).subscribe();

            fluxSink.onCancel(() -> {
                System.out.println("Closed: " + uuid);
                sinksByGuid.remove(uuid);
                fluxSink.complete();
                pingConsumer.dispose();
            });

            fluxSink.next(ServerSentEvent.<String>builder().event("authenticate").data("{\"uuid\": \"" + uuid + "\"}").build());

            this.sinksByGuid.put(uuid, fluxSink);
        });
    }

    @Override
    public void sendString(final String string) {
        System.out.println("Publishing a message to " + sinksByGuid.size() + " subscribers.");
        sinksByGuid.values().forEach(sink -> sink.next(ServerSentEvent.<String>builder().event("message").data(string).build()));
    }

    @Override
    public void closeConnection(String guid) {
        UUID uuid = UUID.fromString(guid);
        if(sinksByGuid.containsKey(uuid)) {
            sinksByGuid.get(uuid).complete();
        }
        sinksByGuid.remove(uuid);
    }
}