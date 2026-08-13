package com.toy.cnr.api.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckApi {

    @GetMapping("/elb-health")
    public String health() {
        return "OK";
    }

    // for debug
    @GetMapping("/version")
    public String version() {
        return "1.0.1";
    }
}
