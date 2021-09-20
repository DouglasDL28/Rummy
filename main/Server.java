package main;
import java.io.*;
import java.util.*;
import java.net.*;
import java.util.concurrent.*;
 
// Server class
public class Server {
    
    // Vector to store active clients
    static Vector<ClientHandler> ar = new Vector<>();
     
    // counter for clients
    static int i = 0;
 
    public static void main(String[] args) throws IOException {
        // server is listening on port 1234
        ServerSocket ss = new ServerSocket(1234);
         
        Socket s;

        GameMaster gm = new GameMaster();
         
        // running infinite loop for getting
        // client request
        while (true) {

            try {

                // Accept the incoming request
                s = ss.accept();

                System.out.println("New client request received : " + s);

                // obtain input and output streams
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                System.out.println("Creating a new handler for this client...");

                // Create a new handler object for handling this request.
                ClientHandler mtch = new ClientHandler(s,"client " + i, dis, dos);

                // Create a new Thread with this object.
                Thread t = new Thread(mtch);

                System.out.println("Adding this client to active client list");

                // add this client to active clients list
                ar.add(mtch);

                // start the thread.
                t.start();

                // increment i for new client.
                // i is used for naming only, and can be replaced
                // by any naming scheme
                i++;
                Player newPlayer = new Player(mtch);
                mtch.addPlayer(newPlayer);
                gm.addPlayer(newPlayer);
            } catch (Exception e) {
                System.out.println("Socket exception " + e);
                ss.close();
                e.printStackTrace();
                break;
            }
        }
    }
}

class GameMaster {
    public volatile ArrayList<Table> tables;
    public ArrayList<Player> playersWaiting;

    public GameMaster() {
        tables = new ArrayList<>();
        playersWaiting = new ArrayList<>();
    }

    public boolean addPlayer(Player player){
        playersWaiting.add(player);
        if(playersWaiting.size() == 2){
            player.handler.sendMessage("Game is starting now");
            Table newTable = new Table(playersWaiting);
            tables.add(newTable);
            playersWaiting=new ArrayList<>();
        }else{
            player.handler.sendMessage("Game is starting soon");
        }
        return true;
    }
}

class Table {
    public ArrayList<Card> cards;
    public ArrayList<Player> players;
    public ArrayList<Melds> melds;
    public int currentPlayer;
    public boolean isGameGoingOn;
    public boolean hasWinner;
    public Card topCard;

    public Table(ArrayList<Player> players) {
        this.players = players;
        this.melds = new ArrayList<>();
        this.cards = new ArrayList<>();
        ArrayList<String> suits = new ArrayList<>(){{
            add("HEARTS");
            add("CLUBS");
            add("DIAMONDS");
            add("SPADES");
        }};
        ArrayList<String> cards = new ArrayList<>(){{
            add("2");
            add("3");
            add("4");
            add("5");
            add("6");
            add("7");
            add("8");
            add("9");
            add("10");
            add("J");
            add("Q");
            add("K");
            add("A");
        }};
        for(String card: cards){
            for(String suit: suits){
                this.cards.add(new Card(suit, card));
            }
        }
        Random rand = new Random();
        int startIndex = rand.nextInt(this.players.size());
        this.currentPlayer = startIndex;
        this.startGame();
    }

    public boolean startGame(){
        this.isGameGoingOn = false;
        this.hasWinner = false;
        for (Player player:this.players){
            player.assignTable(this);
            player.receiveCards(pickCards(7));
        }
        this.topCard = this.pickCard();
        this.mainLoop();
        return true;
    }

    public boolean mainLoop(){
        while (!hasWinner){
            Player currentPlayer = this.players.get(this.currentPlayer);
            currentPlayer.startTurn(topCard, this.pickCard());
            currentPlayer.endTurn();
//            currentPlayer.play();
            this.currentPlayer = (this.currentPlayer + 1)%this.players.size();
        }
        return true;
    }

    public ArrayList<Card> pickCards(int amountOfCards){
        ArrayList<Card> cards = new ArrayList<>();
        for(int i = 0; i < amountOfCards; i++){
            cards.add(pickCard());
        }
        return cards;
    }

    public boolean receiveCard(Card card){
        this.cards.add(card);
        return true;
    }

    public Card pickCard(){
        Random rand = new Random();
        return cards.remove(rand.nextInt(cards.size()));
    }

    public boolean placeCardAtTheTop(Card card){
        this.topCard = card;
        return true;
    }
}

class Player {
    public ClientHandler handler;
    public ArrayList<Card> cards;
    public Table table;
    public boolean canPlay;
    public boolean waitingForInput;
    public volatile String lastInput;

    public Player(ClientHandler handler) {
        this.handler = handler;
        this.cards = new ArrayList<>();
        this.canPlay = false;
        this.waitingForInput = false;
        this.lastInput = null;
    }

    public boolean waitForInput(){
        this.waitingForInput = true;
        return true;
    }

    public boolean receiveInput(String newInput){
        this.lastInput = newInput;
        this.waitingForInput = false;
        return true;
    }

    public boolean assignTable(Table table){
        this.table = table;
        return true;
    }

    public boolean checkLastInput(){
        return this.handler.lastInput == null;
    }

    public boolean startTurn(Card topCard, Card randomCard){
        this.canPlay = true;
        Scanner startTurnScanner = new Scanner(System.in);
        this.handler.sendMessage("La carta actual es " + topCard);
        this.handler.sendMessage("Ingrese 1 para seleccionar la carta superior \n Cualquier otro ingreso selecciona una carta al azar");
        while (true){
            if(lastInput != null){
                System.out.println("Received " + this.lastInput);
                break;
            }
        }
        if (this.lastInput.equals("1")) {
            this.cards.add(topCard);
        } else {
            this.cards.add(randomCard);
        }
        this.lastInput = null;
        this.handler.sendCurrentHand();
        return true;
    }

    public boolean play(){
        Scanner playScanner = new Scanner(System.in);
        this.handler.sendMessage("Ingrese 1 para crear un nuevo juego \n Ingrese 2 para agregar cartas a un juego existente \n Ingrese 3 para terminar su turno");
        String turnChoice = playScanner.nextLine();
        switch (turnChoice) {
            case ("1") -> {
                this.handler.sendMessage("Ingrese las cartas a agregar por su índice y separados por coma");
            }
            case ("2") -> {
                this.handler.sendMessage("Ingrese el juego a agregar ");
                this.handler.sendMessage("Ingrese las cartas a agregar ");
            }
            case ("3") -> {
                this.endTurn();
                return false;
            }
        }
        return true;
    }

    public Card endTurn(){
        this.handler.sendMessage("Ingrese el valor de carta a regresar");
        while (true){
            if (lastInput != null){
                System.out.println("Received" + this.lastInput);
                break;
            }
        }
        Card cardToDiscard = this.cards.get(Integer.parseInt(this.lastInput));
        this.table.placeCardAtTheTop(cardToDiscard);
        this.cards.remove(cardToDiscard);
        this.lastInput = null;
        this.canPlay = false;
        this.handler.sendCurrentHand();
        return cardToDiscard;
    }

    public boolean receiveCards(ArrayList<Card> cards){
        this.cards.addAll(cards);
        this.handler.sendMessage("Received cards boi " + this.cards.toString());
        return true;
    }

    public boolean receiveCard(Card card){
        this.cards.add(card);
        return true;
    }
}

class Card{
    public String suit;
    public String value;

    public Card(String suit, String value) {
        this.suit = suit;
        this.value = value;
    }

    public String getSuit() {
        return suit;
    }

    public String getValue() {
        return value;
    }

    public String getAscii(String suit){
        switch (suit){
            case("HEARTS") -> {
                return "❤";
            }
            case("SPADES") -> {
                return "♠";
            }
            case("DIAMONDS") -> {
                return "\u2666";
            }
            case("CLUBS") -> {
                return "\u2663";
            }
        }
        return "";
    }

    @Override
    public String toString() {
        return getAscii(this.suit) + value;
    }
}

class Melds {
    public ArrayList<Card> cards;
    public Melds(ArrayList<Card> cards) {
        this.cards = cards;
    }
    public boolean addCardToMeld(Card card){
        this.cards.add(card);
        return true;
    }
}

// ClientHandler class
class ClientHandler implements Runnable {
    Scanner scn = new Scanner(System.in);
    private String name;
    final DataInputStream dis;
    final DataOutputStream dos;
    Socket s;
    boolean isloggedin;
    Player player;
    String lastInput;
     
    // constructor
    public ClientHandler(Socket s, String name, DataInputStream dis, DataOutputStream dos) {
        this.dis = dis;
        this.dos = dos;
        this.name = name;
        this.s = s;
        this.isloggedin=true;
        this.lastInput = null;
    }

    public boolean addPlayer(Player newPlayer){
        this.player = newPlayer;
        return true;
    }

    public boolean sendMessage(String message){
        try {
            this.dos.writeUTF(message);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public boolean sendCurrentHand(){
        this.sendMessage("Your current cards are" + this.player.cards);
        return true;
    }

    @Override
    public void run() {
        String received;
        while (true) {
            try {
                // receive the string
                received = dis.readUTF();

                if(received.equals("logout")) {
                    System.out.println("Closed socket");
                    this.isloggedin=false;
                    this.s.close();
                    break;
                }
                System.out.println("Received");
                if(this.player.canPlay){
                    System.out.println("Received");
                    this.player.lastInput = received;
                }else{
                    this.sendMessage("Favor espere su turno");
                }
                // break the string into message and recipient part
//                StringTokenizer st = new StringTokenizer(received, "#");
//                String MsgToSend = st.nextToken();
//                String recipient = st.nextToken();
 
                // search for the recipient in the connected devices list.
                // ar is the vector storing client of active users
                /*for (ClientHandler mc : Server.ar) {
                    // if the recipient is found, write on its
                    // output stream
                    System.out.println("mc " + mc.name);
                    mc.dos.writeUTF(this.name+" : " + received);
                }*/
            } catch (IOException e) {
                System.out.println("Error");
                e.printStackTrace();
            }
             
        }
        try {
            // closing resources
            this.dis.close();
            this.dos.close();
             
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}