package dev.ilkersahin.java.spring.gym.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OpenApiController {
    @GetMapping(value = "/api-docs", produces = "application/x-yaml")
    public ResponseEntity<Resource> getOpenApiYaml() {
        return ResponseEntity.ok(new ClassPathResource("openapi.yaml"));
    }
}
