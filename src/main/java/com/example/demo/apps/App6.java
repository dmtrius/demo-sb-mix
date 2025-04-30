package com.example.demo.apps;

import java.util.ArrayList;
import java.util.List;

public class App6 {

  void main() {
//    Deck deck = init();
//    System.out.println(deck.getCards().size());
//    deck.getCards().forEach(System.out::println);
//    System.out.println("Shuffled deck:");
//    Deck shuffled = shuffle(deck);
//    shuffled.getCards().forEach(System.out::println);
  }

  private Deck shuffle(Deck deck) {
    List<Card> shuffled = new ArrayList<>();
    while(shuffled.size() < deck.getCards().size()) {
      int randomIndex = (int) (Math.random() * deck.getCards().size());
      Card card = deck.getCards().get(randomIndex);
      if (!shuffled.contains(card)) {
        shuffled.add(card);
      }
    }
    return new Deck(shuffled);
  }

  private static Deck init() {
    Deck deck = new Deck();
    List<String> suites = List.of("Hearts", "Spades", "Diamonds", "Clubs");
    List<String> cardNames = List.of("Jack", "Queen", "King", "Ace");
    List<Card> cards = new ArrayList<>();
    suites.forEach(suite -> {
      for (int i = 2; i <= 14; ++i) {
        if (i <= 10) {
          cards.add(new Card(i, String.valueOf(i), suite));
        } else {
          int finalI = i;
          cardNames.forEach(name -> {
            cards.add(new Card(finalI, name, suite));
          });
        }
      }
    });
    deck.setCards(cards);
    return deck;
  }
}

/**
 * Deck:
 * "Heart:
 * Card:
 * 2: "2"
 * "Spades":
 * Card:
 * 2: "2"
 */

class Deck {
  private List<Card> cards = new ArrayList<>();

  public Deck() {
  }

  public Deck(List<Card> cards) {
    this.cards = cards;
  }

  public List<Card> getCards() {
    return cards;
  }

  public void setCards(List<Card> cards) {
    this.cards = cards;
  }
}

record Card(
  Integer value,
  String name,
  String suite
){}
