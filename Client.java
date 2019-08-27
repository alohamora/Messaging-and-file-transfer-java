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
                        if(input.contains("upload"))    upload_file(input, clientOutput);
                        else    clientOutput.writeUTF(input);
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
    static long check_file_size(String input){
        String[] tokens = input.split(" ");
        if(tokens.length > 1){
            File file = new File(tokens[1]);
            if(file.isFile())   return file.length();
            else    System.out.println("File not present or not a regular file");
        }
        else{
            System.out.println("Incorrect File upload command format");
        }
        return -1;
    }

    static byte[] read_file(String input, long fileSize){
        try{
            String[] tokens = input.split(" ");      
            File file = new File(tokens[1]);
            InputStream in = new BufferedInputStream(new FileInputStream(file));
            byte[] buf = new byte[(int)file.length()];
            in.read(buf);
            in.close();
            return buf;
        }
        catch(IOException e){
            System.out.println(e.toString());
        }
        return new byte[0];
    }

    static void upload_file(String input, DataOutput clientOutput){
        try{
            int fileSize = (int)check_file_size(input);
            if(fileSize >= 0){
                clientOutput.writeUTF(input + " " + String.valueOf(fileSize));
                String[] tokens = input.split(" ");
                File file = new File(tokens[1]);
                InputStream in = new BufferedInputStream(new FileInputStream(file));
                while(fileSize > 0){
                    byte[] buf = new byte[Integer.min(fileSize, 1000)];
                    in.read(buf, 0, Integer.min(fileSize, 1000));
                    clientOutput.write(buf, 0, Integer.min(fileSize, 1000));
                    fileSize -= 1000;
                }
                in.close();
            }
        }
        catch(Exception e){
            e.printStackTrace();
            System.out.println("could not upload file from client");
        }
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