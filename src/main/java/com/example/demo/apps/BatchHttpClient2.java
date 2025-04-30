package com.example.demo.apps;

import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class BatchHttpClient2 {

  private final HttpClient httpClient;

  public BatchHttpClient2() {
    this.httpClient = HttpClient.newHttpClient();
  }

  public void fetchPostsAsync(int totalPosts, int batchSize) {
    ExecutorService executor = Executors.newFixedThreadPool(batchSize);

    List<Integer> postIds = IntStream.rangeClosed(1, totalPosts).boxed().collect(Collectors.toList());

    for (int i = 0; i < postIds.size(); i += batchSize) {
      int end = Math.min(i + batchSize, postIds.size());
      List<Integer> batch = postIds.subList(i, end);

      List<CompletableFuture<Void>> futures = new ArrayList<>();

      for (int postId : batch) {
        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> fetchPost(postId), executor)
            .thenAccept(response -> {
              log.info("Post ID {}:\n{}\n", postId, response);
            })
            .exceptionally(ex -> {
              log.error("Failed to fetch post {}: {}", postId, ex.getMessage());
              return null;
            });

        futures.add(future);
      }

      // Wait for this batch to complete Vladyslav Pozniak
      CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    executor.shutdown();
  }

  private String fetchPost(int postId) {
    try {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create("https://jsonplaceholder.typicode.com/posts/" + postId))
          .header("Accept", "application/json")
          .GET()
          .build();

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      return response.body();
    } catch (Exception e) {
      throw new RuntimeException("Request failed for postId " + postId, e);
    }
  }

  public static void main(String[] args) {
    int totalPosts = 10;
    int batchSize = 3;

    new BatchHttpClient2().fetchPostsAsync(totalPosts, batchSize);
  }
}
