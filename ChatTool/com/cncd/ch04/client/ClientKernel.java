package com.cncd.ch04.client;
import java.io.*;
import java.net.*;
import java.util.*;
public class ClientKernel {
    public static final char MSGENDCHAR = 0xff;
    public static final char EXIT = 0xFE;
    public static final char NICK = 0xFD;
    public static final char COMMAND = 0xFD;
    
    private String serverAd;
    private int port;
    private Socket sock;
    private boolean isConnected = false;
    private boolean dropMe = false;
    private LinkedList clients;
    public String nick;
    public boolean printMsg = true;
    private ClientMsgSender cms;
    private ClientMsgListener cml;
    public ArrayList<String> friends;
    public ArrayList<String> AllUsers;
    /** Creates a new instance of ClientKernel */
    public ClientKernel(String server, int port) {
    	this.friends = new ArrayList<String>();
    	this.AllUsers = new ArrayList<String>();
        this.port = port;
        serverAd = server;
        clients = new LinkedList();
        connect();
        if(isConnected) {
            cms = new ClientMsgSender(this, sock);
            cml = new ClientMsgListener(this, sock);
        }
    }
    public int addFriend(String s) {
    	for(int i=0; i<this.friends.size();i++) {
    		if(this.friends.get(i).equals(s))
    			return 0;
    	}
    	this.friends.add(s);
    	return 1;
//    	System.out.println("susccefull");
    }
    public boolean checkFriendOnline(String s,ArrayList<String> u) {
    	for(int j=0;j<u.size();j++) {
    		if(s.equals(u.get(j)))
    			return true;
    	}
    	return false;
    }
    public void connect() {
        try {
            sock = new Socket(serverAd, port);
            isConnected = true;
        } catch(IOException ioe ) {
            ioe.printStackTrace();
        }
    }
    public int getPort() {
        return port;
    }
    public boolean setNick(String nick) {
    	this.nick = nick;
        sendMessage("" + ClientKernel.COMMAND + "nick " + nick);
        return true;
    }
    public int getLocalPort() {
        return sock.getLocalPort();
    }
    public void dropMe() {
        System.out.println("Drop ME!!!");
        cms.drop();
        cml.drop();
        dropMe = true;
        while(cml.hasStoped() && cms.hasStoped()) pause(5);
    }
    public void sendMessage(String str) {
        if(!dropMe) {
            if(str.charAt(0) == '/')
                cms.addMessage("" + ClientKernel.COMMAND + str.substring(1) );
            else cms.addMessage(str);
        }
    }
    public void addClient(ChatClient c) {
        clients.add(c);
    }
    public void removeClient(ChatClient c) {
        clients.remove(c);
    }
    public void pause(int time) {
        try {
            Thread.sleep(time);
        } catch(Exception e) {}
    }
    public synchronized void storeMsg(String str) {
        Object[] client = clients.toArray();
        for(int i=0;i<client.length;i++)
            ((ChatClient)(client[i])).addMsg(str);
    }
    
    public synchronized void refreshUsers(ArrayList<String> str) {
        Object[] client = clients.toArray();
        for(int i=0;i<client.length;i++) {
//        	System.out.println("i am in" + i);
            ((ChatClient)(client[i])).refreshUsers(str);
        }
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    public static void main(String args[]) {
        new ClientKernel("localhost", 1984);
    }
}
class ClientMsgSender extends Thread {
    private Socket s;
    private ClientKernel ck;
    private LinkedList msgList;
    private boolean running = true;
    private boolean hasStoped = false;
    public ClientMsgSender(ClientKernel ck, Socket s) {
        this.ck = ck;
        this.s  = s;
        msgList = new LinkedList();
        start();
    }
    public synchronized void addMessage(String msg) {
        msgList.addLast(msg);
    }
    public void drop() {
        running = false;
    }
    public boolean hasStoped() {
        return hasStoped;
    }
    public void run() {
        try {
            PrintWriter output = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), "UTF-8"), true);
            while(running) {
            	while(msgList.size()>0) {
            		String msg = ((String)(msgList.removeFirst()));
            		output.println(msg + ClientKernel.MSGENDCHAR);
            	}
            	sleep(10);
            }
        } catch(Exception ioe) {
            ioe.printStackTrace();
        } finally {
            hasStoped = true;
        }
    }
}
class ClientMsgListener extends Thread{
    private ClientKernel ck;
    private Socket s;
    private boolean running = true;
    private boolean hasStoped = false;
    public ClientMsgListener(ClientKernel ck, Socket s) {
        this.ck = ck;
        this.s  = s;
        start();
    }
    public void drop() {
        running = false;
    }
    public boolean hasStoped() {
        return hasStoped;
    }
    public void run() {
        try {
        		BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
                while(running) {
                	String strBuff = input.readLine();
                	if(strBuff.length() > 0) {
                		strBuff = strBuff.substring(0, strBuff.length() - 1);
                	}
                    
                    String friendApp = strBuff.toString();
                    String[] a = friendApp.split(":");
                    if(a.length == 3 && a[1].equals("friendApply") && a[2].equals(ck.nick)) {
                    	ck.addFriend(a[0]);
                    }
                    if(strBuff.toString().charAt(0) == '/' && strBuff.toString().substring(1,8).equals("refresh")) {
                    	ArrayList<String> list = new ArrayList<>();
                    	ck.AllUsers.clear();
                    	String buffer = strBuff.toString().substring(8);
//                    	System.out.println("buffer=" + buffer);
                    	String name = "";
                    	while(buffer.length() > 0) {
                    		if(buffer.charAt(0) == ' ') {
                    			list.add(name);
                    			ck.AllUsers.add(name);
                    			name = "";
                    		}
                    		else {
                    			name += buffer.charAt(0);
                    		}
                    		buffer = buffer.substring(1);
                    	}
                    	ck.refreshUsers(list);
                    }
                    else {
                    	String buffer = strBuff.toString();
                    	String[] split = buffer.split("/");
                    	String receiver = split[split.length - 1];
                    	if(receiver.equals(ck.nick) || receiver.equals("ChatServer"))
                    		ck.storeMsg(split[0]);
                    }
                }
                stop();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        } finally {
            hasStoped = true;
        }
    }
}
