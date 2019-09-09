package org.isegodin.spring.security.template.app.controller;

import org.isegodin.spring.security.template.app.data.TempData;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("isAuthenticated()")
    public TempData getData(@RequestParam String name) {
        return TempData.builder()
                .name(name)
                .timestamp(System.currentTimeMillis())
                .build();
    }


}
