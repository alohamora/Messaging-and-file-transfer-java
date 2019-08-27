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
        if(cmd_option.equals("create_group") && st.countTokens() > 0){
            Server.create_group(st.nextToken(), username, clientSock);
        }
        else if(cmd_option.equals("join_group") && st.countTokens() > 0){
            Server.join_group(st.nextToken(), username, clientSock);
        }
        else if(cmd_option.equals("leave_group") && st.countTokens() > 0){
            Server.leave_group(st.nextToken(), username, clientSock);
        }
        else if(cmd_option.equals("list_groups")){
            Server.list_groups(clientSock);
        }
        else if(cmd_option.equals("share_msg") && st.countTokens() > 1){
            String groupname = st.nextToken();
            Server.share_msg(username, groupname, clientSock, st);
        }
        else if(cmd_option.equals("list_details") && st.countTokens() > 0){
            Server.show_details(st.nextToken(), clientSock);
        }
        else if(cmd_option.equals("create_folder") && st.countTokens() > 0){
            create_folder(st.nextToken());
        }
        else if(cmd_option.equals("move_file") && st.countTokens() > 1){
            String source = st.nextToken();
            String dest = st.nextToken();
            move_file(source, dest);
        }
        else{
            try{
                DataOutputStream clientOutput = new DataOutputStream(clientSock.getOutputStream());
                clientOutput.writeUTF(Server.get_time_string() + "Invalid command format passed");
            }
            catch(IOException e){
                System.out.println(e.toString());
            }   
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

    public void move_file(String source, String dest){
        try{
            DataOutputStream clientOutput = new DataOutputStream(clientSock.getOutputStream());
            File source_file = new File(Server.SERVER_FOLDER + username + "/" + source);
            File dest_file = new File(Server.SERVER_FOLDER + username + "/" + dest);
            if(source_file.isFile() && (! dest_file.isDirectory() )){
                if(source_file.renameTo(dest_file)){
                    source_file.delete();
                    clientOutput.writeUTF(Server.get_time_string() + "File moved successfully");
                }
                else    clientOutput.writeUTF(Server.get_time_string() + "Could not move file");
            }
            else if(source_file.isDirectory()){
                clientOutput.writeUTF(Server.get_time_string() + "Given source path is a folder");
            }
            else if(!source_file.exists()){
                clientOutput.writeUTF(Server.get_time_string() + "Given source path does not exists");
            }
            else if(dest_file.isDirectory()){
                clientOutput.writeUTF(Server.get_time_string() + "Given destination path is an existing folder");
            }
        }
        catch(IOException e){
            System.out.println(e.toString());
        }
    }
}