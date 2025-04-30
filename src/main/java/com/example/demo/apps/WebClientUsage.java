package com.example.demo.apps;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

public class WebClientUsage {
  private final WebClient webClient;

  public WebClientUsage() {
    this.webClient = WebClient.builder()
        .baseUrl("https://jsonplaceholder.typicode.com")
        .build();
  }

  void main() {
    var webClientUsage = new WebClientUsage();
    webClientUsage.webClientExample();
  }

  private void webClientExample() {
    WebClient client = WebClient.create();
    Mono<String> responseMono = client.get()
        .uri("https://jsonplaceholder.typicode.com/posts/1")
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(String.class);

    String result = responseMono.block();
    System.out.println("Response: " + result);
  }

  public String getPostById(int id) {
    return webClient.get()
        .uri("/posts/{id}", id)
        .retrieve()
        .bodyToMono(String.class)
        .block();
  }

  public String getPostsByUserId(int userId) {
    return webClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/posts")
            .queryParam("userId", userId)
            .build())
        .retrieve()
        .bodyToMono(String.class)
        .block();
  }

  public String createPost(MyRequest request) {
    return webClient.post()
        .uri("/posts")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .retrieve()
        .bodyToMono(String.class)
        .block();
  }

  public String updatePost(int id, MyRequest request) {
    return webClient.put()
        .uri("/posts/{id}", id)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .retrieve()
        .bodyToMono(String.class)
        .block();
  }

  public void deletePost(int id) {
    webClient.delete()
        .uri("/posts/{id}", id)
        .retrieve()
        .bodyToMono(Void.class)
        .block();
  }

  public String getWithErrorHandling(int id) {
    return webClient.get()
        .uri("/posts/{id}", id)
        .retrieve()
        .onStatus(HttpStatusCode::is4xxClientError, response ->
            Mono.error(new RuntimeException("Client error: " + response.statusCode())))
        .onStatus(HttpStatusCode::is5xxServerError, response ->
            Mono.error(new RuntimeException("Server error: " + response.statusCode())))
        .bodyToMono(String.class)
        .onErrorResume(ex -> {
          if (ex instanceof WebClientResponseException) {
            System.out.println("WebClient error: " + ex.getMessage());
          }
          return Mono.just("Fallback content");
        })
        .block();
  }
}

@NoArgsConstructor
@AllArgsConstructor
@Data
class MyRequest {
  private String title;
  private String body;
  private int userId;
}
