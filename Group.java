import java.io.* ;
import java.net.* ;
import java.util.*;

class Group {
    String groupName;
    Vector<String> members;
    Vector<Socket> memberSockets;

    Group(String name, String username, Socket clientSocket){
        members = new Vector<String>();
        memberSockets = new Vector<Socket>();
        groupName = name;
        members.add(username);
        memberSockets.add(clientSocket);
        System.out.println(Server.get_time_string() + "Created group " + groupName);
    }

    public void add_user(String username, Socket clientSocket){
        try {
            DataOutputStream dout = new DataOutputStream(clientSocket.getOutputStream());
            if(!members.contains(username)){
                members.add(username);
                memberSockets.add(clientSocket);
                dout.writeUTF(Server.get_time_string() + "User added in group " + groupName);
            }
            else{
                dout.writeUTF(Server.get_time_string() + "User already joined in this group " + groupName);
            }
        }
        catch(IOException e){
            System.out.println(e.toString());
        }
    }
    public int remove_user(String username, Socket clientSocket, boolean write_output){
        try{
            if(members.contains(username)){
                members.remove(username);
                memberSockets.remove(clientSocket);
                if(write_output){
                    DataOutputStream dout = new DataOutputStream(clientSocket.getOutputStream());
                    dout.writeUTF(Server.get_time_string() + "User removed from group " + groupName);
                }
            }
            else{
                if(write_output){
                    DataOutputStream dout = new DataOutputStream(clientSocket.getOutputStream());
                    dout.writeUTF(Server.get_time_string() + "User not present in group " + groupName);
                }
            }
        }
        catch(IOException e){
            System.out.println(e.toString());
        }
        return members.size();
    }
    public void share_msg(String sender, StringTokenizer st){
        try{
            StringBuilder br = new StringBuilder();
            while(st.hasMoreTokens()){
                String msg = st.nextToken();
                br.append(msg).append(" ");
            }
            for(int i=0;i<members.size();i++){
                DataOutputStream dout = new DataOutputStream(memberSockets.get(i).getOutputStream());
                if(!members.get(i).equals(sender)){

                    dout.writeUTF("[" + groupName + "][" + sender + "]: " + br.toString());
                }
                else{
                    dout.writeUTF(Server.get_time_string() + "Message shared in group " + groupName);
                }
            }
        }
        catch(IOException e){
            System.out.println(e.toString());
        }
    }
}