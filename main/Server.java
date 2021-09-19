package main;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.net.*;
 
// Server class
public class Server {

    // Matrix for session management
    // Rows indicate session and columns session's clients
    static CopyOnWriteArrayList<CopyOnWriteArrayList<ClientHandler>> sessions = new CopyOnWriteArrayList<>();
     
    // counter for sessions
    static int sessionId = 0;
 
    public static void main(String[] args) throws IOException {
        // server is listening on port 1234
        ServerSocket ss = new ServerSocket(1234);
         
        Socket s;
         
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
                ClientHandler mtch = new ClientHandler(s, sessionId, dis, dos);

                // Create a new Thread with this object.
                Thread t = new Thread(mtch);
                
                System.out.println("Adding this client to session");

                if (sessions.size() > 0 && sessions.size() >= sessionId && sessions.get(sessionId).size() < 4) {
                    sessions.get(sessionId).add(mtch); // add new player to  existing session
                } else {
                    CopyOnWriteArrayList<ClientHandler> currSession = new CopyOnWriteArrayList<>(); // create new session
                    currSession.add(mtch); // add player to new session
                    sessions.add(currSession);
                    sessionId++; // increment sessionId for new session.
                }
                
                // start the thread.
                t.start();
                
                
            } catch (Exception e) {
                ss.close();
                e.printStackTrace();
            }
        }
    }
}
 
// ClientHandler class
class ClientHandler implements Runnable {
    Scanner scn = new Scanner(System.in);
    private int sessionId;
    final DataInputStream dis;
    final DataOutputStream dos;
    Socket s;
    boolean isloggedin;
     
    // constructor
    public ClientHandler(Socket s, int sessionId, DataInputStream dis, DataOutputStream dos) {
        this.dis = dis;
        this.dos = dos;
        this.sessionId = sessionId;
        this.s = s;
        this.isloggedin=true;
    }
 
    @Override
    public void run() {
 
        String received;
        while (true) {
            try {
                // receive the string
                received = dis.readUTF();
                 
                System.out.println(received);
                 
                if(received.equals("logout")) {
                    this.isloggedin=false;
                    this.s.close();
                    break;
                }
                 
                // break the string into message and recipient part
                StringTokenizer st = new StringTokenizer(received, "#");
                String MsgToSend = st.nextToken();
                String recipient = st.nextToken();
 
                // TODO: manejar por sesiones en lugar de por clientes individuales
                // search for the recipient in the connected devices list.
                // ar is the vector storing client of active users
                for (ClientHandler mc : Server.ar) {
                    // if the recipient is found, write on its
                    // output stream
                    if (mc.name.equals(recipient) && mc.isloggedin==true) {
                        mc.dos.writeUTF(this.name+" : "+MsgToSend);
                        break;
                    }
                }
            } catch (IOException e) {
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