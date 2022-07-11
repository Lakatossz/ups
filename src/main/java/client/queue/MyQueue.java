package client.queue;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Trida fronty s pouzitim mutexu.
 */
public class MyQueue {

    /**
     * Fronta pro prijem zprav.
     */
    public Queue<String> queue;

    /**
     * Vytvori frontu.
     */
    public MyQueue() {
        queue = new LinkedList<>();
    }

    /**
     * Prida zpravu do fronty.
     */
    public synchronized void add(String message) {
        queue.add(message);
        notify();
    }

    /**
     * Odebere zpravu z fronty.
     */
    public synchronized String remove() {
        while(queue.isEmpty()) {
            try {
                wait();
                break;
            } catch (InterruptedException e) {
                System.out.println("Vyber prvku z fronty - cekani.");
            }
        }

        if(queue.isEmpty()) {
            notify();
            return null;
        } else {
            String message = queue.remove();
            notify();
            return message;
        }
    }
}
