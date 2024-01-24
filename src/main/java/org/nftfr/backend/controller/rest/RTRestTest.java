package org.nftfr.backend.controller.rest;

import org.nftfr.backend.application.RealTimeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@CrossOrigin(value = "*")
@RequestMapping("/rt")
public class RTRestTest {
    static private Double myInt = 10.0;

    @GetMapping("/test")
    public SseEmitter test() {
        SseEmitter emitter = new SseEmitter(5 * 1000L);
        RealTimeService.registerEmitter("test_id", emitter);
        return emitter;
    }

    @GetMapping("/test2")
    @ResponseStatus(HttpStatus.OK)
    public void test2() {
        myInt += 10.0;
        RealTimeService.pushUpdate("test_id", myInt);
    }
}
