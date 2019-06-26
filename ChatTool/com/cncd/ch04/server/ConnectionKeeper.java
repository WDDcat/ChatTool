package com.cncd.ch04.server;
import java.io.*;
import java.net.*;
import java.util.*;
public class ConnectionKeeper {
	public ArrayList<Friend> friends;
    private LinkedList clientList;
    private CommandParser cp;
    public ConnectionKeeper(CommandParser parser) {
        this.cp = parser;
        clientList = new LinkedList();
        friends = new ArrayList<Friend>();
    }
    public int locateFriends(String s) {
    	for(int i=0;i<friends.size();i++) {
    		if(s.equals(friends.get(i).nick)) return i;
    	}
    	return -1;
    }
    
    public boolean checkFriends(String s) {
    	for(int i=0;i<friends.size();i++) {
    		if(s.equals(friends.get(i).nick))return true;
    	}
    	return false;
    }
    
    public void addFriends(String s,String sb) {
    	if(!checkFriends(s))friends.add(new Friend(s));
    	for(int i=0;i<friends.size();i++) {
    		if(s.equals(friends.get(i).nick)) {
    			friends.get(i).friend.add(sb);
    		}
    	}
    }
    public void sendFriend(ConnectedClient c) {
    	if(checkFriends(c.nick)) {
    		int t = locateFriends(c.nick);
    		for(int i=0;i<friends.get(t).friend.size();i++) {
    			String p = friends.get(t).friend.get(i)+":friendApply:"+c.nick;
    			c.sendMessage(p);
//    			System.out.println(p);
    		}
    	}
    }
    public void add(Socket s) {
        MainServer.connects++;
        clientList.addLast(new ConnectedClient(s, this));
//        ConnectedClient c = (ConnectedClient)clientList.getLast();
//        sendFriend(c);
        
    }
    public void remove(ConnectedClient cc) {
        clientList.remove(cc);
        cc = null;
    }
    public LinkedList users() {
        return clientList;
    }
    public void runCommand(ConnectedClient cc, String str) {
        cp.runCommand(cc, str);
    }
    public void sendTo(ConnectedClient sender, String user, String msg) {
        boolean found = false;
        for(int i =0;i<clientList.size();i++) {
            ConnectedClient receiver = (ConnectedClient)(clientList.get(i));
            if(user.equalsIgnoreCase(receiver.nick)) {
                receiver.sendMessage(msg);
                found = true;
                i = clientList.size()+5; // Stop the loop.
            }
        }
        if(!found) {
            sender.sendMessage("Unable to find user " + user);
        }
    }
    public void broadcast(String msg) {
        for(int i =0;i<clientList.size();i++) {
            ConnectedClient cc = (ConnectedClient)(clientList.get(i));
            cc.sendMessage(msg);
        }
    }
}
