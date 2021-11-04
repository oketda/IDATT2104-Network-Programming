import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PrimeNumberFinder extends Thread{

    public int threadNR;
    public int numberOfThreads;
    public int from;
    public int to;
    public static List<Integer> primeNumbers = Collections.synchronizedList(new ArrayList<Integer>());
    public static boolean wait = true;

    public PrimeNumberFinder(int threadNR, int numberOfThreads, int from, int to){
        this.threadNR = threadNR;
        this.numberOfThreads = numberOfThreads;
        this.from = from;
        this.to = to;
    }

    public void run(){

        boolean prime = true;

        for (int i = from+threadNR; i <= to; i = i+numberOfThreads) {
            for (int j = 2; j < i; j++) {
                if (i % j == 0) {
                    prime = false;
                }
                if (j + 1 >= i && prime == true) {
                    primeNumbers.add(i);
                }
            }
            prime = true;
        }

        System.out.println("Thread " + threadNR + " exiting...");
        stopWaiting();
    }

    public void stopWaiting(){
        Client.finishedThreads.incrementAndGet();
        if (Client.finishedThreads.intValue() == numberOfThreads){
            wait = false;
        }
    }
}

class Client {

    public static AtomicInteger finishedThreads = new AtomicInteger();

    public static void main(String[] args) throws InterruptedException {

        Scanner scn = new Scanner(System.in);

        System.out.println("How many threads do you want?");
        int threads = scn.nextInt();
        System.out.println("From what number?");
        int from = scn.nextInt();
        System.out.println("To what number?");
        int to = scn.nextInt();

        for (int i = 0; i < threads; i++) {
            PrimeNumberFinder pnf = new PrimeNumberFinder(i, threads, from, to);
            Thread thread = new Thread(pnf);
            thread.start();
            thread.join();
        }

        //Spin lock while waiting for threads to finnish
        while (PrimeNumberFinder.wait == true){
            TimeUnit.SECONDS.sleep(1);
        }

        //Sorting the prime numbers
        Collections.sort(PrimeNumberFinder.primeNumbers);

        System.out.println("\nList of prime numbers from " + from + " to " + to + ":");
        for (int i = 0; i < PrimeNumberFinder.primeNumbers.size(); i++) {
            System.out.println(PrimeNumberFinder.primeNumbers.get(i));
        }
    }
}
