import java.io.* ;
import java.net.* ;
import java.util.*;

public class ClientThread extends Thread{
    Socket clientSock;
    String username;
    ClientThread(Socket sock){
        clientSock = sock;
        username = Server.Users.get(Server.clientSockets.indexOf(clientSock));
    }

    public void run(){
        try{
            DataInputStream clientInput = new DataInputStream(clientSock.getInputStream());
            while(true){
                String cmd = clientInput.readUTF();
                execute_command(cmd);
            }
        }
        catch(IOException e){
            Server.delete_user(username, clientSock);
        }
    }
    public void execute_command(String cmd){
        StringTokenizer st = new StringTokenizer(cmd);
        String cmd_option = st.nextToken();
        if(cmd_option.equals("create_group")){
            Server.create_group(st.nextToken(), username, clientSock);
        }
        else if(cmd_option.equals("join_group")){
            Server.join_group(st.nextToken(), username, clientSock);
        }
        else if(cmd_option.equals("leave_group")){
            Server.leave_group(st.nextToken(), username, clientSock);
        }
        else if(cmd_option.equals("list_groups")){
            Server.list_groups(clientSock);
        }
        else if(cmd_option.equals("share_msg")){
            Server.share_msg(username, clientSock, st);
        }
        else if(cmd_option.equals("create_folder")){
            create_folder(st.nextToken());
        }
    }

    public void create_folder(String foldername){
        try{
            DataOutputStream clientOutput = new DataOutputStream(clientSock.getOutputStream());
            File folder = new File(Server.SERVER_FOLDER + username + "/" + foldername);
            if (!folder.exists()){
                if(folder.mkdir())  clientOutput.writeUTF(Server.get_time_string() + "Folder created");
                else    clientOutput.writeUTF(Server.get_time_string() + "Could not create folder");
            }
            else{
                clientOutput.writeUTF(Server.get_time_string() + "Folder already present on Server");
            }
        }
        catch(IOException e){
            System.out.println(e.toString());
        }
    }
}