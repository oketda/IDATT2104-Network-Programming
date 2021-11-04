/**
 * SocketTjener.java  - "Programmering i Java", 4.utgave - 2009-07-01
 *
 * Programmet �pner en socket og venter p� at en klient skal ta kontakt.
 * Programmet leser tekster som klienten sender over, og returnerer disse.
 */

import java.io.*;
import java.net.*;

class SocketTjener {
    public static void main(String[] args) throws IOException {
        final int PORTNR = 1250;

        ServerSocket tjener = new ServerSocket(PORTNR);
        System.out.println("Logg for tjenersiden. N� venter vi...");
        Socket forbindelse = tjener.accept();  // venter inntil noen tar kontakt

        /* �pner str�mmer for kommunikasjon med klientprogrammet */
        InputStreamReader leseforbindelse
                = new InputStreamReader(forbindelse.getInputStream());
        BufferedReader leseren = new BufferedReader(leseforbindelse);
        PrintWriter skriveren = new PrintWriter(forbindelse.getOutputStream(), true);

        /* Sender innledning til klienten */
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

            skriveren.println("For å avlsutte. Skriv linjeskift.");
            enLinje = leseren.readLine();
        }


        /* Mottar data fra klienten */
        /*String enLinje = leseren.readLine();  // mottar en linje med tekst
        while (enLinje != null) {  // forbindelsen p� klientsiden er lukket
            System.out.println("En klient skrev: " + enLinje);
            skriveren.println("Du skrev: " + enLinje);  // sender svar til klienten
            enLinje = leseren.readLine();
        }*/

        /* Lukker forbindelsen */
        leseren.close();
        skriveren.close();
        forbindelse.close();
    }
}

/* Utskrift p� tjenersiden:
Logg for tjenersiden. N� venter vi...
En klient skrev: Hallo, dette er en pr�ve.
En klient skrev: Og det fungerer utmerket.
*/
