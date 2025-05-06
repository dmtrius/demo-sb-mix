package com.example.demo.apps;

import java.util.ArrayList;
import java.util.List;

public class App7 {

  void main() {
    System.out.println(getRowByIndex(6));
  }

  private static List<Integer> getRowByIndex(int rowIndex) {
    List<Integer> row = new ArrayList<>();
    for (int i = 0; i <= rowIndex; i++) {
      row.addFirst(1);
      for (int j = 1; j < row.size() - 1; j++) {
        row.set(j, row.get(j) + row.get(j + 1));
      }
    }
    return row;
  }
}
/*
0: 1
1: 1 1
2: 1 2 1
3: 1 3 3 1
4: 1 4 6 4 1
5: 1 5 10 10 5 1
6: 1 6 15 20 15 6 1
 */
