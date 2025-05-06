package com.example.demo.apps.controller;

import com.example.demo.apps.config.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

  @PostMapping("/login")
  public ResponseEntity<Map<String, String>> login(@RequestParam String username, @RequestParam String password) {
    if ("user".equals(username) && "password".equals(password)) {
      String token = JwtUtil.generateToken(username);
      return ResponseEntity.ok(Map.of("token", token));
    }
    return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
  }
}
