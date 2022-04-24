package com.ews.stguo.testproject.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
@RestController
public class SimpleController {

    @GetMapping(path = "/simple/tryGetDelay")
    public ResponseEntity<String> tryGetDelay(@RequestParam("delay") long delay) {
        try {
            TimeUnit.MILLISECONDS.sleep(delay);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
        }
        return ResponseEntity.ok("OK");
    }

}
