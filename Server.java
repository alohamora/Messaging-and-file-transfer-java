import java.io.* ;
import java.net.* ;
import java.util.*;
import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime;   

public class Server {
    static String IP_ADDR = "127.0.0.1";
    static int PORT = 6969;
    static int max_clients = 10;
    public static String SERVER_FOLDER = "ServerDir/";
    public static Vector<String> Users;
    public static Vector<Socket> clientSockets;
    public static Map<String, Group> clientGroupMapping;
    public static Vector<Group> groups;
    static ServerSocket serverSocket;
    public static void main(String[] args) {
        StringBuffer sb = new StringBuffer();
        Users = new Vector<String>(max_clients);
        clientSockets = new Vector<Socket>(max_clients);
        groups = new Vector<Group>(max_clients);
        clientGroupMapping = new HashMap<String, Group>();
        try{
            serverSocket = new ServerSocket(PORT);
            Runtime.getRuntime().addShutdownHook(new ShutDownThread());
            System.out.println(sb.append(get_time_string() + "Server running on port: ").append(PORT));
            sb.delete(0, sb.length());
            File server = new File(SERVER_FOLDER);
            if(!server.exists())    server.mkdir();
        }
        catch(IOException e){
            sb.append("Could not create server socket-> ").append(e.toString());
            sb.delete(0, sb.length());
        }
        while(true){
            try{
                Socket ClientSock = serverSocket.accept();
                if(Server.check_username_add_user(ClientSock)){
                    ClientThread client = new ClientThread(ClientSock);
                    client.start();
                }
            }
            catch(IOException e){
                sb.append("Could not accept client-> ").append(e.toString());
                System.out.println(get_time_string() + sb);
                sb.delete(0, sb.length());
            }
        }
    }

    public static boolean check_username_add_user(Socket ClientSock){
        try{
            DataOutputStream dout = new DataOutputStream(ClientSock.getOutputStream());
            if(Users.size() == max_clients){
                dout.writeUTF(get_time_string() + "Could not login client...Maximum capacity reached for server");
                System.out.println(get_time_string() + "Could not login client...Maximum capacity reached for server");
                ClientSock.close();
                return false;
            }
            else{
                DataInputStream clientInput = new DataInputStream(ClientSock.getInputStream());
                String username = clientInput.readUTF();
                if(Users.contains(username)){
                    dout.writeUTF(get_time_string() + "Could not login user " + username + "...a client with same username is connected");
                    System.out.println(get_time_string() + "Could not login user " + username + "...a client with same username is connected");
                    ClientSock.close();
                    return false;
                }
                else{
                    String foldername = SERVER_FOLDER + username;
                    File userFolder = new File(foldername);
                    userFolder.mkdir();
                    Users.add(username);
                    clientSockets.add(ClientSock);
                    System.out.println(get_time_string() + username + " connected to server");
                }
            }
        }
        catch(IOException e){
            System.out.println(e.toString());
        }
        return true;
    }
    public static synchronized void delete_user(String username, Socket clientSock){
        System.out.println(get_time_string() + username + " disconnected");
        if(clientGroupMapping.containsKey(username)){
            int groupLen = clientGroupMapping.get(username).remove_user(username, clientSock, false);
            if(groupLen == 0){
                groups.remove(clientGroupMapping.get(username));
                System.out.println(get_time_string() + "Removed group " + clientGroupMapping.get(username).groupName);
            }
            clientGroupMapping.remove(username);
        }
        try{
            clientSock.close();
        }
        catch(IOException e){
            System.out.println(e.toString());
        }
        Users.remove(username);
        clientSockets.remove(clientSock);
    }

    public static synchronized void create_group(String groupName, String username, Socket clientSock){
        try{
            DataOutputStream dout = new DataOutputStream(clientSock.getOutputStream());
            for(int i=0;i<groups.size();i++){
                if(groups.get(i).groupName.equals(groupName)){
                    dout.writeUTF(get_time_string() + "Group already created");
                    return ;
                }
            }
            dout.writeUTF(get_time_string() + "Created group");
            Group groupObj = new Group(groupName, username, clientSock);
            groups.add(groupObj);
            clientGroupMapping.put(username, groupObj);
        }
        catch(IOException e){
            System.out.println(e.toString());
        }
    }

    public static synchronized void join_group(String groupName, String username, Socket clientSock){
        try{
            DataOutputStream dout = new DataOutputStream(clientSock.getOutputStream());
            if(clientGroupMapping.containsKey(username)){
                clientGroupMapping.get(username).remove_user(username, clientSock, false);
            }
            for(int i=0;i<groups.size();i++){
                if(groups.get(i).groupName.equals(groupName)){
                    groups.get(i).add_user(username, clientSock);
                    clientGroupMapping.put(username, groups.get(i));
                    return ;
                }
            }
            dout.writeUTF(get_time_string() + "No group with the given name");
        }
        catch(IOException e){
            System.out.println(e.toString());
        }
    }

    public static synchronized void leave_group(String groupName, String username, Socket clientSock){
        try{
            DataOutputStream dout = new DataOutputStream(clientSock.getOutputStream());
            for(int i=0;i<groups.size();i++){
                if(groups.get(i).groupName.equals(groupName)){
                    if(groups.get(i).remove_user(username, clientSock, true) == 0){
                        System.out.println(get_time_string() + "Removed group " + groups.get(i).groupName);
                        groups.remove(groups.get(i));
                    }
                    clientGroupMapping.remove(username);
                    return ;
                }
            }
                dout.writeUTF(get_time_string() + "No group with the given name");
        }
        catch(IOException e){
            System.out.println(e.toString());
        }
    }

    public static void list_groups(Socket clientSock){
        try{
            DataOutputStream dout = new DataOutputStream(clientSock.getOutputStream());   
            if(groups.size() == 0){
                dout.writeUTF(get_time_string() + "No groups available");
            }
            else{
                StringBuffer sb = new StringBuffer();
                sb.append("Available groups: ");
                for(int i=0;i<groups.size();i++){
                    sb.append(groups.get(i).groupName);
                    if(i != groups.size() - 1)  sb.append(", ");
                }
                dout.writeUTF(sb.toString());
            }
        }
        catch(IOException e){
            System.out.println(e.toString());
        }
    }

    public static void share_msg(String username, Socket clientSock, StringTokenizer st){
        try{
            DataOutputStream dout = new DataOutputStream(clientSock.getOutputStream());   
            if(!clientGroupMapping.containsKey(username)){
                dout.writeUTF(get_time_string() + "User is not part of any group");
            }
            else{
                clientGroupMapping.get(username).share_msg(username, st);
            }
        }
        catch(IOException e){
            System.out.println(e.toString());
        }
    }

    public static void show_details(String groupName, Socket clientSock){
        try{
            DataOutputStream dout = new DataOutputStream(clientSock.getOutputStream());
            for(int i=0;i<groups.size();i++){
                if(groups.get(i).groupName.equals(groupName)){
                    groups.get(i).show_details(clientSock);
                    return ;
                }
            }
            dout.writeUTF(get_time_string() + "No group with the given name");
        }
        catch(IOException e){
            System.out.println(e.toString());
        }
    }

    public static String get_time_string(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");  
        LocalDateTime now = LocalDateTime.now();  
        return "[SERVER][" + dtf.format(now) + "]: ";
    }
}

class ShutDownThread extends Thread{
    public void run(){
        System.out.println("Server shutting down... :)");
    }
}