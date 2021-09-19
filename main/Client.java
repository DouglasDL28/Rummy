package main;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Scanner;

import main.Stanza;
  
public class Client {

    final static int ServerPort = 1234;
  
    public static void main(String args[]) throws UnknownHostException, IOException {

        Scanner scn = new Scanner(System.in);
          
        // getting localhost ip
        InetAddress ip = InetAddress.getByName("localhost");
          
        // establish the connection
        Socket s = new Socket(ip, ServerPort);
          
        // obtaining input and out streams
        DataInputStream dis = new DataInputStream(s.getInputStream());
        DataOutputStream dos = new DataOutputStream(s.getOutputStream());
  
        // sendMessage thread
        Thread sendMessage = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    // read the message to deliver.
                    Stanza.Builder stanza = Stanza.newBuilder();
                    Stanza build = stanza.build();
                      
                    try {
                        // write on the output stream
                        build.writeDelimitedTo(dos);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
          
        // readMessage thread
        Thread readMessage = new Thread(new Runnable() {
            @Override
            public void run() {
  
                while (true) {
                    try {
                        Stanza stanza = null;

                        // read the message sent to this client
                        while ((stanza = Stanza.parseDelimitedFrom(dis)) != null){
                            System.out.println(stanza.toString());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
  
        sendMessage.start();
        readMessage.start();
  
    }
}