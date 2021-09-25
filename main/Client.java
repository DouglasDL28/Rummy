package main;

import java.io.*;
import java.net.*;
import java.util.*;
import java.io.IOException;


public class Client {

    final static int ServerPort = 1234;
    public static volatile boolean isTurn = false;

    public static String rules = """
            
            Rules:
            * You will be dealt 7 cards from the deck, and the goal is to be the first person to use them all.
            * Each round, you´ll pick between grabbing the top card of the common pile, or being dealt a random one from the rest of the deck.
            * After that, you can use your cards by arranging new melds on the table or adding your cards to a meld previously created by you or another player.
            * Your turn ends when you can´t/don´t want to do anything in the table, and return a card to be placed on top of the common pile.
            
            There are two types of melds: 3 or more cards with the same number or a succesion of cards from the same suit.
            Example: \u27643, \u27644, \u27645 is a valid meld, as well as \u2764J, \u2660J, \u2666J, while \u27643, \u26664, \u27645 and \u2764J, \u2660J, \u2666Q aren´t.
            
            If you want to chat with the other players in the room, simply type '/m <message>'
            You can always return to check the rules and commands by typing '/h'
            """
            ;

    public static String welcome = """
             ______    __   __  __   __  __   __  __   __
            |    _ |  |  | |  ||  |_|  ||  |_|  ||  | |  |
            |   | ||  |  | |  ||       ||       ||  |_|  |
            |   |_||_ |  |_|  ||       ||       ||       |
            |    __  ||       ||       ||       ||_     _|
            |   |  | ||       || ||_|| || ||_|| |  |   |
            |___|  |_||_______||_|   |_||_|   |_|  |___|
            
            Welcome to the Rummy console implementation!
            
            This game is simple and fun to play with friends, or other people online.
            """
            ;

    public static void main(String[] args) throws IOException {
        // getting localhost ip
        InetAddress ip = InetAddress.getByName("localhost");

        // establish the connection
        Socket s = new Socket(ip, ServerPort);

        // obtaining input and out streams
        DataInputStream dis = new DataInputStream(s.getInputStream());
        DataOutputStream dos = new DataOutputStream(s.getOutputStream());

        // name scanner
        Scanner nameScanner = new Scanner(System.in);

        // welcome page
        System.out.println(welcome + rules);

        // read username from user input
        System.out.println("Enter your username: ");
        String newName = nameScanner.nextLine();

        // scanner for incoming messages from server
        Scanner scn = new Scanner(System.in);

        // sendMessage thread
        Thread sendMessage = new Thread(() -> {
            while (true) {

                // read the message to deliver to client
                String msg = scn.nextLine();

                if (isTurn){
                    if(!msg.equals("")) msg = "3~" + msg;
                } else {
                    String[] command = msg.split(" ", 2);

                    switch (command[0]){
                        case "/m" -> msg = "1~" + command[1];
                        case "/h" -> System.out.println(rules);
                        case "" -> {
                            //pass
                        }
                        default -> System.out.println("Sorry, command '" + command[0] + "' not recognized.");
                    }
                }

                try {
                    // write on the output stream
                    dos.writeUTF(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
          
        // readMessage thread
        Thread readMessage = new Thread(() -> {

            while (true) {
                try {
                    // read the message sent to this client
                    String response = dis.readUTF();

                    String[] payload = response.split("~");

                    switch (payload[0]) {
                        // message
                        case "1" ->
                                System.out.println(payload[1]);
                        // turn
                        case "2" -> {
                            System.out.println("\nIt´s your turn, pick a play!");
                            isTurn = true;
                        }
                        // end turn
                        case "4" -> {
                            System.out.println("\nWaiting for other players to end their turn...");
                            isTurn = false;
                        }
                        case "5" -> {
                            switch(payload[1]){
                                // player cards
                                case "CARDS" -> {
                                    System.out.println("\nYour current cards are: ");

                                    String stringCards = payload[2].replace("[", "");
                                    stringCards = stringCards.replace("]", "");
                                    stringCards = stringCards.replace(" ", "");

                                    String[] cards = stringCards.split(",");

                                    for(String card: cards){
                                        System.out.print("|   " + card + "   |  ");
                                    }
                                    System.out.println();

                                    for(int i = 0; i < cards.length; i++){
                                        if (cards[i].length() == 3) {
                                            System.out.print("      " + i + "      ");
                                        } else {
                                            System.out.print("     " + i + "      ");
                                        }
                                    }
                                    System.out.println("\n");
                                }
                                // table melds
                                case "MELDS" -> {
                                    System.out.println("\nThe current melds are: ");
                                    System.out.println(payload[2]);
                                }
                                // table top card
                                case "TOP" ->
                                        System.out.println("Card on top: \n" + "|   " + payload[2] + "   |");
                            }
                        }
                        // information
                        default ->
                            System.out.println(response);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // send username to table
        dos.writeUTF("0~" + newName);

        // start thread for listening and sending messages
        sendMessage.start();
        readMessage.start();
    }
}