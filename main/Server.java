package main;
import java.io.*;
import java.util.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


/** Server class */
public class Server {
 
    public static void main(String[] args) throws IOException {
        // server is listening on port 1234
        ServerSocket ss = new ServerSocket(1234);
         
        Socket s;

        GameMaster gm = new GameMaster();
         
        // running infinite loop for getting client request
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
                ClientHandler mtch = new ClientHandler(s, "client", dis, dos);

                // Create a new Thread with this object.
                Thread t = new Thread(mtch);

                System.out.println("Adding this client to active client list");

                // start the thread.
                t.start();

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
            player.handler.sendMessage("Game is starting now!");

            Table newTable = new Table(playersWaiting);
            Thread t = new Thread(newTable);

            tables.add(newTable);
            t.start();

            playersWaiting = new ArrayList<>();
        } else{
            player.handler.sendMessage("Game is starting soon...");
        }
        return true;
    }
}

class Table implements Runnable {
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
        ArrayList<Integer> cards = new ArrayList<>(){{
            add(1);
            add(2);
            add(3);
            add(4);
            add(5);
            add(6);
            add(7);
            add(8);
            add(9);
            add(10);
            add(11);
            add(12);
            add(13);
        }};
        for(Integer card: cards){
            for(String suit: suits){
                this.cards.add(new Card(suit, card));
            }
        }
        Random rand = new Random();
        int startIndex = rand.nextInt(this.players.size());
        this.currentPlayer = startIndex;
    }

    public boolean startGame(){
        this.isGameGoingOn = false;
        this.hasWinner = false;
        for (Player player:this.players){
            player.assignTable(this);
            player.receiveCards(pickCards(7));
        }
        this.topCard = this.pickCard();
        return true;
    }

    public boolean mainLoop(){
        while (!hasWinner){
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

    public boolean checkWinner(Player player) {
        if (player.cards.size() <= 0)
            return true;

        return false;
    }

    public void gameOver(Player winner) {
        for (Player player : this.players) {
            player.handler.sendMessage(winner.name + " es el ganador!");
        }
        this.hasWinner = true;
    }

    @Override
    public void run() {

        this.startGame();

        while(!this.hasWinner){
            Player currentPlayer = this.players.get(this.currentPlayer);
            currentPlayer.handler.sendMessage("2~"); // start turn
            currentPlayer.startTurn(topCard, this.pickCard());

            while(!currentPlayer.play()) {
                if (checkWinner(currentPlayer)) {
                    this.gameOver(currentPlayer);
                    break;
                }
            }

            currentPlayer.endTurn();

            if (checkWinner(currentPlayer)) {
                this.gameOver(currentPlayer);
                break;
            }

            currentPlayer.handler.sendMessage("4~"); // end turn

            this.currentPlayer = (this.currentPlayer + 1) % this.players.size();
        }
    }
}

class Player {
    public ClientHandler handler;
    public ArrayList<Card> cards;
    public Table table;
    public boolean canPlay;
    public boolean waitingForInput;
    public volatile String lastInput;
    public String name;

    public Player(ClientHandler handler) {
        this.handler = handler;
        this.cards = new ArrayList<>();
        this.canPlay = false;
        this.waitingForInput = false;
        this.lastInput = null;
    }

    public boolean setName(String newName){
        this.name = newName;
        return true;
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

        this.handler.sendCurrentHand();

        this.handler.sendMessage("TOP~" + topCard);
        this.handler.sendMessage("\nEnter '1' to take top card \nAny other input will give a random card from the pile...");
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
        return true;
    }

    public boolean areCardsConsecutive(List<Card> cardsToCheck){
        //this.handler.sendMessage("Checking if consecutive");
        List<Card> sortedList = cardsToCheck.stream().sorted().collect(Collectors.toList());
        //this.handler.sendMessage("Cards to check " + sortedList.toString());
        //this.handler.sendMessage("Length to check " + Integer.toString(sortedList.size()));
        for(int i=1; i<sortedList.size();i++){
            if(sortedList.get(i).getValue() - sortedList.get(i-1).getValue() != 1 || !sortedList.get(i).getSymbol().equals(sortedList.get(i - 1).getSymbol())){
                return false;
            }
        }
        return true;
    }

    public boolean areCardsTheSame(List<Card> cardsToCheck){
        //this.handler.sendMessage("Checking if same");
        for(int i=1; i<cardsToCheck.size();i++){
            if(!(cardsToCheck.get(i).getValue() == cardsToCheck.get(i-1).getValue())){
                return false;
            }
        }
        return true;
    }

    public boolean isNewMeldValid(List<Card> cardsToCheck){
        if(cardsToCheck.size() < 3){
            return false;
        }
        return this.areCardsConsecutive(cardsToCheck) || this.areCardsTheSame(cardsToCheck);
    }

    public boolean canAddCardsToMeld(Melds meldToAdd, List<Card> cardsToAdd){
        List<Card> sortedMeld = meldToAdd.cards.stream().sorted().collect(Collectors.toList());
        for(int i = 0; i < cardsToAdd.size(); i++){
            Card cardToCheck = cardsToAdd.get(i);
            if (((cardToCheck.getValue() - Collections.max(sortedMeld).getValue() == 1) || (cardToCheck.getValue() - Collections.min(sortedMeld).getValue() == -1)) && cardToCheck.getSymbol().equals(meldToAdd.cards.get(0).getSymbol())){
                return false;
            }
            if(!cardToCheck.getSymbol().equals(sortedMeld.get(0).getSymbol())){
                return false;
            }
        }
        return true;
    }

    public boolean play(){
        this.handler.sendCurrentHand();
        this.handler.sendMessage(
            "Choose an option: \n" +
            "1. Create new meld \n" +
            "2. Add card(s) to existing meld \n" +
            "3. End turn"
        );

        this.lastInput = null;

        while (true){
            if(this.lastInput != null){
                System.out.println("Received " + this.lastInput);
                break;
            }
        }

        switch (this.lastInput) {
            case ("1") -> {
                this.lastInput = null;
                this.handler.sendMessage("Enter the cards, separated by commas: ");
                while (true){
                    if(this.lastInput != null){
                        System.out.println("Received: " + this.lastInput);
                        break;
                    }
                }
                List<Card> cardsToMeld = Arrays.stream(lastInput.split(",")).map(s -> this.cards.get(Integer.parseInt(s))).collect(Collectors.toList());
                this.lastInput = null;
                this.handler.sendMessage(cardsToMeld.toString());
                if(this.isNewMeldValid(cardsToMeld)){
                    this.handler.sendMessage("\nValid meld!");
                    this.table.melds.add(new Melds((ArrayList<Card>) cardsToMeld));
                    this.removeCards((ArrayList<Card>) cardsToMeld);

                    if (this.cards.size() <= 0) // end turn if no cards left
                        return true;
                } else {
                    this.handler.sendMessage("Meld is not valid, try again or end turn");
                }
                return false;
            }
            case ("2") -> {
                this.lastInput = null;
                this.handler.sendMessage("Enter the meld's index: ");
                while (true){
                    if(this.lastInput != null){
                        System.out.println("Received: " + this.lastInput);
                        break;
                    }
                }
                String meldId = this.lastInput;
                this.lastInput = null;
                this.handler.sendMessage("Enter cards to add: ");
                while (true){
                    if(this.lastInput != null){
                        System.out.println("Received: " + this.lastInput);
                        break;
                    }
                }
                List<Card> cardsToMeld = Arrays.stream(lastInput.split(",")).map(s -> this.cards.get(Integer.parseInt(s))).collect(Collectors.toList());
                if(this.canAddCardsToMeld(this.table.melds.get(Integer.parseInt(meldId)), cardsToMeld)){
                    this.table.melds.get(Integer.parseInt(meldId)).addCards((ArrayList<Card>) cardsToMeld);
                    this.removeCards((ArrayList<Card>) cardsToMeld);

                    if (this.cards.size() <= 0) // end turn if no cards left
                        return true;
                }
                this.lastInput = null;
                return false;
            }
            case ("3") -> {
                this.lastInput = null;
                return true;
            }
        }
        return true;
    }

    public Card endTurn(){
        this.handler.sendMessage("Enter the card's index");
        while (true){
            if (this.lastInput != null){
                System.out.println("Received" + this.lastInput);
                break;
            }
        }
        Card cardToDiscard = this.cards.get(Integer.parseInt(this.lastInput));
        this.table.placeCardAtTheTop(cardToDiscard);
        this.cards.remove(cardToDiscard);

        this.lastInput = null;
        this.canPlay = false;

        return cardToDiscard;
    }

    public boolean receiveCards(ArrayList<Card> cards){
        this.cards.addAll(cards);
        return true;
    }

    public boolean removeCards(ArrayList<Card> cardsToRemove){
        try{
            this.cards.removeAll(cardsToRemove);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public boolean receiveCard(Card card){
        this.cards.add(card);
        return true;
    }
}

class Card implements Comparable<Card>{
    public String suit;
    public int value;

    public Card(String suit, int value) {
        this.suit = suit;
        this.value = value;
    }

    public String getSuit() {
        return this.suit;
    }

    public int getValue() {
        return this.value;
    }

    public String getAsciiSuit(){
        switch (this.suit){
            case("HEARTS") -> {
                return "❤";
            }
            case("SPADES") -> {
                return "♠";
            }
            case("DIAMONDS") -> {
                return "♦";
            }
            case("CLUBS") -> {
                return "♣";
            }
        }
        return "";
    }

    public String getSymbol(){
        switch (this.value){
            case(1) -> {
                return "A";
            }
            case(2) -> {
                return "2";
            }
            case(3) -> {
                return "3";
            }
            case(4) -> {
                return "4";
            }
            case(5) -> {
                return "5";
            }
            case(6) -> {
                return "6";
            }
            case(7) -> {
                return "7";
            }
            case(8) -> {
                return "8";
            }
            case(9) -> {
                return "9";
            }
            case(10) -> {
                return "10";
            }
            case(11) -> {
                return "J";
            }
            case(12) -> {
                return "Q";
            }
            case(13) -> {
                return "K";
            }
        }
        return "";
    }

    @Override
    public String toString() {
        return this.getAsciiSuit() + this.getSymbol();
    }

    @Override
    public int compareTo(Card c){
        return this.getValue() - c.getValue();
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

    public boolean addCards(ArrayList<Card> newCards){
        this.cards.addAll(newCards);
        return true;
    }

    @Override
    public String toString() {
        return this.cards.toString();
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
        } catch (Exception e){
            return false;
        }
    }

    public boolean sendCurrentHand(){
        this.sendMessage("MELDS~" + this.player.table.melds);
        this.sendMessage("CARDS~" + this.player.cards);
        return true;
    }

    public boolean broadCastMessage(String message){
        ArrayList<Player> playerInTables = this.player.table.players;
        try{
            for(Player currentPlayer:playerInTables){
                currentPlayer.handler.dos.writeUTF(this.player.name + " " + message);
            }
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public boolean sendTopCard(){
        this.sendMessage("Currently the top card is" + this.player.table.topCard);
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

                System.out.println("Received this " + received);
                String[] request = received.split("~");

                switch (request[0]) {
                    case "0": // name
                        this.player.setName(request[1]);
                        break;
                    case "1": // message
                        this.broadCastMessage(request[1]);
                        break;
                    case "3": // play
                        this.player.lastInput = request[1];
                        break;
                
                    default:
                        break;
                }

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