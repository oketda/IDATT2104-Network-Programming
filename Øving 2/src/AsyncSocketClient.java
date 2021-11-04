package asyncsocket;

import java.io.*;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 *
 * @author Tomas Holt, inspired by http://www.baeldung.com/java-nio2-async-socket-channel
 */

public class AsyncSocketClient {

    private AsynchronousSocketChannel client;

    public void init() throws Exception{
        client = AsynchronousSocketChannel.open();
        InetSocketAddress hostAddress = new InetSocketAddress("localhost", 4555);
        Future<Void> future = client.connect(hostAddress);

        future.get();//wait for connection
    }

    public String sendMessage(String message) throws Exception {
        byte[] byteMsg = new String(message).getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(byteMsg);
        Future<Integer> writeResult = client.write(buffer);

        // do some computation
        writeResult.get();//wait for result
        buffer.flip();
        Future<Integer> readResult = client.read(buffer);

        // do some computation
        readResult.get();//wait for result
        String echo = new String(buffer.array()).trim();
        buffer.clear();
        System.out.println(echo);
        return echo;
    }

    public int calculator(int a, int b, String math) throws IOException, ExecutionException, InterruptedException {
        String tallA = Integer.toString(a);
        String tallB = Integer.toString(b);
        String message = tallA + " " + tallB + " " +  math;
        byte[] byteMsg = new String(message).getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(byteMsg);
        Future<Integer> writeResult = client.write(buffer);

        // do some computation
        writeResult.get();//wait for result
        buffer.flip();
        Future<Integer> readResult = client.read(buffer);

        // do some computation
        readResult.get();//wait for result

        int ans = getCalculation(buffer, a, b);
        return ans;
    }

    private int getCalculation(ByteBuffer buffer, int a, int b){
        byte space = 32;
        byte[] riktigByteArray = new byte[0];
        byte[] byteArray = getByteArrayFromByteBuffer(buffer);


        int digitsA = String.valueOf(a).length();
        int digitsB = String.valueOf(b).length();

        byte[] tempByteArray = new byte[byteArray.length-2];
        for (int i = 0; i < tempByteArray.length; i++) {
            tempByteArray[i] = byteArray[i];
        }

        int numberOfSpaces = 0;
        for (int i = 0; i < tempByteArray.length; i++) {
            if (Byte.compare(tempByteArray[i], space) == 0){
                numberOfSpaces++;
            }
        }

        //System.out.println(new String(tempByteArray));

        if (numberOfSpaces == 1){
            for (int i = 0; i <tempByteArray.length; i++) {
                if (Byte.compare(tempByteArray[i], space) == 0){
                    riktigByteArray = new byte[i];
                    for (int j = 0; j < riktigByteArray.length; j++) {
                        riktigByteArray[j] = tempByteArray[j];
                    }
                    break;
                }
            }
        }
        else if (numberOfSpaces == 0){
            if (digitsA <= digitsB){
                riktigByteArray = new byte[tempByteArray.length-digitsA];
            }
            else {
                riktigByteArray = new byte[tempByteArray.length - digitsB];
            }
            for (int i = 0; i < riktigByteArray.length; i++) {
                riktigByteArray[i] = tempByteArray[i];
            }
        }

        String ans = new String(riktigByteArray);
        //System.out.println(ans);
        return Integer.parseInt(ans);
    }

    private static byte[] getByteArrayFromByteBuffer(ByteBuffer byteBuffer) {
        byte[] bytesArray = new byte[byteBuffer.capacity()];
        ((ByteBuffer) byteBuffer.duplicate().clear()).get(bytesArray);
        return bytesArray;
    }

    public void cleanUp() throws IOException{
        client.shutdownInput();
        client.shutdownOutput();
        client.close();
    }

    public static void main(String args[]) throws Exception {
        System.out.println("*Start client");
        AsyncSocketClient client = new AsyncSocketClient();
        client.init();

        Scanner input = new Scanner(System.in);
        String scn = "";
        System.out.println("Write the first number:");
        int a = input.nextInt();
        System.out.println("Write +, -, * or /:");
        String math = input.next();
        System.out.println("Write the second number:");
        int b = input.nextInt();

        int calculated = client.calculator(a, b, math);
        System.out.println("Calculation from server: " + a + math + b + " = " + calculated);

        client.cleanUp();
    }
}
