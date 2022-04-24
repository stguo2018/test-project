package com.ews.stguo.testproject.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
@RestController
public class TestWebClientController {

    @PostMapping(path = "/webcliet/test")
    public ResponseEntity<String> test(@RequestBody Map<String, String> contents) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 30000; i++) {
            sb.append(UUID.randomUUID().toString());
        }
        return ResponseEntity.ok("received-" + sb.toString());
    }

    @GetMapping(path = "/webcliet/test")
    public ResponseEntity<String> test() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 30000; i++) {
            sb.append(UUID.randomUUID().toString());
        }
        return ResponseEntity.ok("received-" + sb.toString());
    }

}
