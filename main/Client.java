package main;
import java.beans.ConstructorProperties;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    final static int ServerPort = 1234;
    public static volatile boolean isTurn = false;
    
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

                    if (isTurn){
                        msg = "3~" + msg;
                    } else {
                        msg = "1~" + msg;
                    }
 
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
                        String response = dis.readUTF();

                        System.out.println("Received this " + response);
                        String[] payload = response.split("~");

                        switch (payload[0]) {
                            case "1": // message
                                System.out.println(payload[1]);
                                break;

                            case "2": // turn
                                isTurn = true;
                                break;

                            case "4": // end turn
                                isTurn = false;
                                break;
                        
                            default:
                                break;
                        }

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
        dos.writeUTF("0~" + newName);
    }
}