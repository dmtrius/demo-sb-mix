package com.example.demo.apps.controller.v2;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RequestMapping(path = "/api/v2/", produces = "application/json")
@RestController("v2HomeController")
public class HomeController {
  @GetMapping("/home")
  public ResponseEntity<Map<String, String>> home() {
    Instant now = Instant.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    String formattedDate = LocalDateTime.ofInstant(now, ZoneId.systemDefault()).format(formatter);
    return ResponseEntity.ok(Map.of("time_v2", formattedDate));
  }
}
