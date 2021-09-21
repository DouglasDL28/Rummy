package main;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class Client {

    final static int ServerPort = 1234;
  
    public static void main(String args[]) throws UnknownHostException, IOException {
        //Ingrese nombre
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
                    String msg = scn.nextLine();
                      
                    try {
                        // write on the output stream
                        dos.writeUTF(msg);
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
                        // read the message sent to this client
                        /*
                        * Mensaje: 0~Mensaje
                        * Jugadas:
                        *   Jalar o devolver carta: 1~0~Índice de carta
                        *
                        * */
                        String msg = dis.readUTF();
                        System.out.println(msg);
                    } catch (IOException e) {
  
                        e.printStackTrace();
                    }
                }
            }
        });

        Scanner nameScanner = new Scanner(System.in);
        System.out.println("Favor ingresar el nombre de usuario ");
        String newName = nameScanner.nextLine();
        System.out.println("Para enviar mensajes únicamente es necesario ingresar m~Mensaje");
//        dos.writeUTF("3~name");
        sendMessage.start();
        readMessage.start();
        dos.writeUTF("3~" + newName);
    }
}