package com.example.sse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
public class SSEController {

    private Channel channel;

    public SSEController(Channel channel) {
        this.channel = channel;
    }

    @GetMapping(value = "/channel")
    public ResponseEntity<Flux<ServerSentEvent<String>>> channel() {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_EVENT_STREAM_VALUE).body(channel.createSubscriber());
    }

    @PostMapping("/send")
    public void sendStringDownTube(@RequestParam String string) {
        channel.sendString(string);
    }

    @PostMapping("/close")
    public void closeAll(@RequestParam String uuid) {
        channel.closeConnection(uuid);
    }
}
