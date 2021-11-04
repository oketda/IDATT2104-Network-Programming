import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class WebTjener {

    public static void main(String[] args) throws IOException {
        final int PORTNR = 1250;

        try{
            ServerSocket tjener = new ServerSocket(PORTNR);
            System.out.println("Logg for tjenersiden. N� venter vi...");
            Socket forbindelse = tjener.accept();

            InputStreamReader leseforbindelse
                    = new InputStreamReader(forbindelse.getInputStream());
            BufferedReader leseren = new BufferedReader(leseforbindelse);
            PrintWriter skriveren = new PrintWriter(forbindelse.getOutputStream(), true);

            String header = "";
            String linje = leseren.readLine();

            while (!linje.equals("")){
                header += "<li>"+ linje + "</li>\n";
                linje = leseren.readLine();
            }
            skriveren.println("HTTP/1.0 200 OK");
            skriveren.println("Content-Type: text/html; charset=utf-8");

            skriveren.println("<HTML><BODY>");
            skriveren.println("");
            skriveren.println("<H1> Hilsen. Du har koblet deg opp til min enkle web-tjener </h1>");
            skriveren.println("<ul>");
            skriveren.println(header);
            skriveren.println("</ul></body></html>");

            leseren.close();
            skriveren.close();
            forbindelse.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
