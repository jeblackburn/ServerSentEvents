package com.example.sse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RestController
public class SSEController {

    private Channel channel;

    public SSEController(Channel channel) {
        this.channel = channel;
    }

    @GetMapping(value = "/channel")
    public ResponseEntity<Flux<ServerSentEvent<String>>> channel() {

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_EVENT_STREAM_VALUE).body(channel.giveTube());
    }

    @PostMapping("/send")
    public void sendStringDownTube(@RequestParam String string) {
        System.out.println("string = " + string);
        channel.sendString(string);
    }

    @PostMapping("/authenticate")
    public void authenticate(@RequestParam String token, @RequestParam UUID uuid) {

        if (token.equals("good")) channel.authenticate(uuid);
    }
}
