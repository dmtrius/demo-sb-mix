package com.example.demo.apps;

import java.util.Map;
import java.util.stream.Collectors;

public class App5 {
    void main() {
        String str = "aaabbcdd";
        String uq = str.chars()
                .mapToObj(c -> String.valueOf((char) c))
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
                .entrySet().stream()
                .filter(e -> e.getValue() == 1)
                .findFirst().map(Map.Entry::getKey).get();
        System.out.println(uq);
    }
}
