package com.example.demo.apps.controller.v1;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RequestMapping(path = "/api/v1/", produces = "application/json")
@RestController("v1HomeController")
public class HomeController {
  @GetMapping("/home")
  public ResponseEntity<Map<String, LocalDateTime>> home() {
    return ResponseEntity.ok(Map.of("time_v1", LocalDateTime.now()));
  }
}
