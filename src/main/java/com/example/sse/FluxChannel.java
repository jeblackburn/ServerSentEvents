package com.example.sse;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class FluxChannel implements Channel {

    private List<FluxHolder> sinks;

    public FluxChannel() {
        this.sinks = new ArrayList<>();
    }

    @Override
    public Flux<ServerSentEvent<String>> giveTube() {

        UUID uuid = UUID.randomUUID();

        return Flux.create((FluxSink<ServerSentEvent<String>> fluxSink) -> {

            System.out.println("Callback from Flux");
            sinks.add(new FluxHolder(fluxSink, uuid));

            // force the issue more quickly if the client disconnects
//            Disposable pingConsumer = Flux.interval(Duration.ofSeconds(2)).doOnNext(a -> {
//                System.out.println("Sending a Ping.");
//                fluxSink.next(ServerSentEvent.<String>builder().event("ping " + a).build());
//            }).subscribe();

            fluxSink.onCancel(() -> {
                System.out.println("On Cancel!!!");
                sinks.removeIf(fluxHolder -> fluxHolder.uuid.equals(uuid));
                fluxSink.complete();
//                pingConsumer.dispose();
            } );

            fluxSink.next(ServerSentEvent.<String>builder().event("authenticate").data("{\"uuid\": \"" + uuid + "\"}").build());
        });
    }

    @Override
    public void sendString(final String string) {

        sinks.forEach((FluxHolder sink) -> sink.send(string));
    }

    @Override
    public void authenticate(UUID uuid) {

        System.out.println(sinks.size());

        System.out.println(uuid);

        sinks.stream().filter(it -> it.uuid.equals(uuid)).forEach(FluxHolder::authenticate);
    }
}

class FluxHolder {

    private Boolean authenticated = false;

    UUID uuid;

    private FluxSink<ServerSentEvent<String>> sink;

    public FluxHolder(FluxSink<ServerSentEvent<String>> sink, UUID uuid) {
        this.sink = sink;
        this.uuid = uuid;
    }

    public void authenticate() {

        System.out.println("Authed");
        this.authenticated = true;
    }

    public void send(String string) {

        System.out.println(uuid);

        System.out.println(authenticated);

        if (authenticated) sink.next(ServerSentEvent.<String>builder().event("message").data(string).build());
    }
}
