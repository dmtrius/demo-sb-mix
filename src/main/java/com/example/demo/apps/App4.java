package com.example.demo.apps;

import java.util.Arrays;

public class App4 {

    void main() {
        //SERVICE
       int[] a = {4,6,4, 3,2};
        System.out.println((Arrays.toString(c1(a))));
    }

    private static int[] c1(int[] a) {
        for (int i = 0; i <= a.length - 1; i++) {
            for(int j = 0; j<=i-1; j++) {
                if (a[j] < a[j+1]) {
                    int temp = a[j];
                    a[j] = a[j+1];
                    a[j+1] = temp;
                }
            }
        }
    return a;
    }
}
