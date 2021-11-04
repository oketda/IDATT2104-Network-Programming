import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ThreadTest extends Thread{

    BufferedReader leseren;
    PrintWriter skriveren;
    Socket forbindelse;

    public ThreadTest(BufferedReader leseren, PrintWriter skriveren, Socket forbindelse){
        this.leseren = leseren;
        this.skriveren = skriveren;
        this.forbindelse = forbindelse;

    }


    public void run() {
        try {
            Scanner leserFraKommandovindu = new Scanner(System.in);
            skriveren.println("Hei, du har kontakt med tjenersiden!");
            skriveren.println("For å avlsutte. Skriv linjeskift.");
            String enLinje = leseren.readLine();
            int tall1;
            int tall2;
            String metode;

            while (enLinje != null) {
                skriveren.println("Skriv første tallet");
                enLinje = leseren.readLine();
                tall1 = Integer.parseInt(enLinje);

                skriveren.println("Vil du summere eller subtrahere? skriv + eller -");
                enLinje = leseren.readLine();
                metode = enLinje;

                skriveren.println("Skriv det andre tallet");
                enLinje = leseren.readLine();
                tall2 = Integer.parseInt(enLinje);

                if (metode.equals("+")) {
                    skriveren.println(tall1 + " + " + tall2 + " = " + (tall1 + tall2));
                } else if (metode.equals("-")) {
                    skriveren.println(tall1 + " - " + tall2 + " = " + (tall1 - tall2));
                }

                skriveren.println("For å avlsutte. Skriv linjeskift, ellers skriv noe annet");
                enLinje = leseren.readLine();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

            try{
                leseren.close();
                skriveren.close();
                forbindelse.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ThreadSocketServer{

    public static void main(String[] args) throws IOException {
        final int PORTNR = 1250;

        Scanner leserFraKommandovindu = new Scanner(System.in);
        System.out.println("Hvor mange threads?");
        int antallThreads = leserFraKommandovindu.nextInt();
        ServerSocket server = new ServerSocket(PORTNR);

        for (int i = 0; i < antallThreads; i++) {
            Socket forbindelse = null;
            try {
                System.out.println(i+1 + " venter på klient");
                forbindelse = server.accept();
                InputStreamReader leseforbindelse
                        = new InputStreamReader(forbindelse.getInputStream());
                BufferedReader leseren = new BufferedReader(leseforbindelse);
                PrintWriter skriveren = new PrintWriter(forbindelse.getOutputStream(), true);
                System.out.println("test1");
                Thread thread = new ThreadTest(leseren, skriveren, forbindelse);
                System.out.println("Test2");
                thread.start();
                System.out.println("test3");
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        server.close();
    }
}
