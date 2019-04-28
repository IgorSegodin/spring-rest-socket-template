package org.isegodin.spring.rest.socket.template.controller;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author isegodin
 */
@RequestMapping("/test")
@RestController
public class TempController {

    @GetMapping("/data")
    public TempData getData(@RequestParam String name) {
        return TempData.builder()
                .name(name)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    @Data
    @Builder
    private static class TempData {
        private String name;
        private long timestamp;
    }
}
