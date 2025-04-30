package com.example.demo.apps;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.IntStream;

@Slf4j
public class BatchWebClient {

  private final WebClient webClient;

  public BatchWebClient() {
    this.webClient = WebClient.builder()
        .baseUrl("https://jsonplaceholder.typicode.com")
        .build();
  }

  public void fetchPostsInBatches(int totalPosts, int batchSize) {
    List<Integer> postIds = IntStream.rangeClosed(1, totalPosts).boxed().toList();

    Flux.fromIterable(postIds)
        .flatMap(this::fetchPostById, batchSize)
        .doOnNext(System.out::println)
        .blockLast();
  }

  private Mono<Post> fetchPostById(int postId) {
    return webClient.get()
        .uri("/posts/{id}", postId)
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(Post.class)
        .onErrorResume(e -> {
          log.info("Failed to fetch post {}: {}", postId, e.getMessage());
          return Mono.empty();
        });
  }

  public static void main(String[] args) {
    int totalPosts = 10;
    int batchSize = 3;

    BatchWebClient client = new BatchWebClient();
    client.fetchPostsInBatches(totalPosts, batchSize);
  }
}

record Post(
    Integer userId,
    Integer id,
    String title,
    String body
){};
