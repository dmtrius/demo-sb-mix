package com.example.demo.apps;

import lombok.Data;

public class App1 {
    void main() throws InterruptedException {
        Counter counter = new Counter();
        Thread t1 = new T1(counter);
        Thread t2 = new T1(counter);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println(counter.getCounter());

        Integer counter2 = 0;
        Thread t3 = new T2(counter2);
        Thread t4 = new T2(counter2);
        t3.start();
        t4.start();
        t3.join();
        t4.join();
        System.out.println(counter2);
    }
}
@Data
class Counter {
    private Integer counter = 0;
}

class T1 extends Thread {
    private final Counter counter;

    public T1(Counter counter) {
        this.counter = counter;
    }

    @Override
    public void run() {
        this.increment();
    }

    private synchronized void increment() {
        this.counter.setCounter(this.counter.getCounter() + 1000);
    }
}

 class T2 extends Thread {
     private Integer counter;

     public T2(Integer counter) {
         this.counter = counter;
     }

     @Override
     public void run() {
         this.increment();
     }

     private synchronized void increment() {
         counter = counter + 1000;
     }
 }