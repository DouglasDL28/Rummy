package main;

import java.io.*;
import java.net.*;
import java.util.*;
import java.io.IOException;

/*import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
*/

public class Client {

    final static int ServerPort = 1234;
    public static volatile boolean isTurn = false;

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

        // read username from user input
        System.out.println("Ingrese el nombre de usuario: ");
        String newName = nameScanner.nextLine();

        // scanner for incoming messages from server
        Scanner scn = new Scanner(System.in);

        // sendMessage thread
        Thread sendMessage = new Thread(() -> {
            while (true) {

                // read the message to deliver to client
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
                        case "4" ->
                                isTurn = false;
                        // player cards
                        case "CARDS" -> {
                            System.out.println("Your current cards are: ");

                            String stringCards = payload[1].replace("[", "");
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
                        case "MELDS" ->
                                System.out.println();
                        // table top card
                        case "TOP" ->
                                System.out.println("Card on top: \n" + "|   " + payload[1] + "   |");
                        // information
                        default ->
                            System.out.println(response);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
/*
        Thread GUI = new Thread(() -> {
            DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();
            Screen screen = null;

            try {

                Terminal terminal = defaultTerminalFactory.createTerminal();
                screen = new TerminalScreen(terminal);
                TerminalSize terminalSize = screen.getTerminalSize();

                screen.startScreen();

                screen.setCursorPosition(new TerminalPosition(1, terminalSize.getRows()));

                while(true) {
                    KeyStroke keyStroke = screen.pollInput();
                    if (keyStroke != null && (keyStroke.getKeyType() == KeyType.Escape || keyStroke.getKeyType() == KeyType.EOF)) {
                        break;
                    }

                    String sizeLabel = "          Chat";

                    TerminalPosition labelBoxTopLeft = new TerminalPosition(terminalSize.getColumns() - 24, 0);
                    TerminalSize labelBoxSize = new TerminalSize(24, terminalSize.getRows());
                    TerminalPosition labelBoxTopRightCorner = labelBoxTopLeft.withRelativeColumn(labelBoxSize.getColumns() - 1);

                    TextGraphics textGraphics = screen.newTextGraphics();

                    textGraphics.fillRectangle(labelBoxTopLeft, labelBoxSize, ' ');

                    textGraphics.drawLine(
                            labelBoxTopLeft.withRelativeColumn(1),
                            labelBoxTopLeft.withRelativeColumn(labelBoxSize.getColumns() - 1),
                            Symbols.DOUBLE_LINE_HORIZONTAL);
                    textGraphics.drawLine(
                            labelBoxTopLeft.withRelativeRow(2).withRelativeColumn(1),
                            labelBoxTopLeft.withRelativeRow(2).withRelativeColumn(labelBoxSize.getColumns() - 1),
                            Symbols.DOUBLE_LINE_HORIZONTAL);
                    textGraphics.drawLine(
                            labelBoxTopLeft.withRelativeRow(3).withRelativeColumn(0),
                            labelBoxTopLeft.withRelativeRow(labelBoxSize.getRows()).withRelativeColumn(0),
                            Symbols.DOUBLE_LINE_VERTICAL
                    );
                    textGraphics.drawLine(
                            labelBoxTopLeft.withRelativeRow(3).withRelativeColumn(labelBoxSize.getColumns() - 1),
                            labelBoxTopLeft.withRelativeRow(labelBoxSize.getRows()).withRelativeColumn(labelBoxSize.getColumns() - 1),
                            Symbols.DOUBLE_LINE_VERTICAL
                    );

                    textGraphics.setCharacter(labelBoxTopLeft, Symbols.DOUBLE_LINE_TOP_LEFT_CORNER);
                    textGraphics.setCharacter(labelBoxTopLeft.withRelativeRow(1), Symbols.DOUBLE_LINE_VERTICAL);
                    textGraphics.setCharacter(labelBoxTopLeft.withRelativeRow(2), Symbols.DOUBLE_LINE_T_RIGHT);
                    textGraphics.setCharacter(labelBoxTopRightCorner, Symbols.DOUBLE_LINE_TOP_RIGHT_CORNER);
                    textGraphics.setCharacter(labelBoxTopRightCorner.withRelativeRow(1), Symbols.DOUBLE_LINE_VERTICAL);
                    textGraphics.setCharacter(labelBoxTopRightCorner.withRelativeRow(2), Symbols.DOUBLE_LINE_T_LEFT);

                    textGraphics.putString(labelBoxTopLeft.withRelative(1, 1), sizeLabel);

                    for(String message: messages){
                        textGraphics.putString(labelBoxTopLeft.withRelative(1, 1), ">>" + message);
                    }

                    screen.refresh();
                    Thread.yield();
                }
            } catch(IOException exc) {
                exc.printStackTrace();
            }
        });*/

        // send username to table
        dos.writeUTF("0~" + newName);

        System.out.println("Para enviar mensajes ingrese m~<mensaje>\n");

        // start thread for listening and sending messages
        sendMessage.start();
        readMessage.start();
        //GUI.start();
    }
}