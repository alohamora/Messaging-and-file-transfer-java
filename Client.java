import java.io.*;
import java.net.*;

public class Client {
    static String IP_ADDR = "127.0.0.1";
    static int PORT = 6969;
    static Socket clientSock;
    static String username;
    static DataOutputStream clientOutput;
    static DataInputStream clientInput;
    public static void main(String[] args) throws IOException, UnknownHostException{
        try{
            String input;
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            StringBuffer sb = new StringBuffer();
            username = Client.getUsername(args);
            clientSock = new Socket(IP_ADDR, PORT);
            clientOutput = new DataOutputStream(clientSock.getOutputStream());
            clientInput = new DataInputStream(clientSock.getInputStream());
            Thread messageHandler = new Thread(new MessageHandler(clientInput));
            messageHandler.start();
            clientOutput.writeUTF(username);
            while(true){
                try{
                    input = bufferedReader.readLine();
                    if(input.length() > 0){
                        clientOutput.writeUTF(input);
                        sb.delete(0, sb.length());
                    }
                }
                catch(Exception e){
                    System.out.println(e.toString());
                }
            }
        }
        catch(ConnectException e){
            System.out.println("Could not connect to server");
            System.exit(0);
        }        
    }
    public static String getUsername(String[] args){
        if(args.length == 0){
            System.out.println("No username passed");
            System.exit(0);
        }
        return args[0];
    }
}

class MessageHandler implements Runnable{
    DataInputStream server;
    MessageHandler(DataInputStream clientInput){
        server = clientInput;
    }
    @Override
    public void run() {
        try{
            String serverOutput = new String();
            while(true){
                serverOutput = server.readUTF();
                System.out.println(serverOutput);
            }
        }
        catch(IOException e){
            System.out.println("Connection to server lost");
            System.exit(0);
        }
    }
}