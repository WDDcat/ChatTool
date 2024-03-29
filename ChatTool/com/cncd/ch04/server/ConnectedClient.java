package com.cncd.ch04.server;
import java.io.*;
import java.net.*;
import java.util.*;

import com.cncd.ch04.client.ClientKernel;
public class ConnectedClient {
    private ConnectionKeeper ck;
    public String nick;
    public Date connectedTime;
    public String ipNumber;
    public int portNumber;
    public boolean verifyedBoolean = false;
    public int verifyedCount = 0;
    public String tmpNick = "";
    private ServerMsgSender msgSend;
    private ServerMsgListener msgList;
    private Socket sock;
    public boolean printMsg = false;
    public ConnectedClient(Socket sock, ConnectionKeeper ck) {
        this.ck = ck;
        ipNumber = sock.getInetAddress().getHostAddress();
        portNumber = sock.getPort();
        this.sock = sock;
        msgSend = new ServerMsgSender(this.sock, this);
        msgList = new ServerMsgListener(this.sock, this);
        nick = "" + portNumber;
    }
    public ConnectionKeeper getConnectionKeeper() {
            return ck;
    }
    public String getNick() {
            return nick;
    }
    public void sendMessage(String str) {
        msgSend.addMessage(str);
    }
    public void sendTo(String user, String msg) {
        ck.sendTo(this, user, msg);
    }
    public void broadcastMessage(String str) {
        if(!isSpam(str)) ck.broadcast(str);
    }
    public void dropClient() {
        msgList.closeConnection();
        msgSend.closeConnection();
        ck.remove(this);
    }
    public void runCommand(String str) {
        if(str.charAt(0)==0xFD) {
            String str1 = str.substring(1);
            ck.runCommand(this, str1);
        }
    }
    private boolean isSpam(String str) {
        return false;
    }
    public static void main(String arg[]) {
        MainServer ms = new MainServer(1984);
    }
    public void whoAmI() {
        String str = "<br>Connected Port: " + portNumber + "<br>" +
                     "Nick: " + nick + "<br>";
        sendMessage(str);
    }
}
class ServerMsgSender extends Thread {
	private int num = 0;
	private int flag = 0;
    private Socket sock;
    private LinkedList msgList;
    private ConnectedClient cc;
    private boolean running = true;
    public ServerMsgSender(Socket sock, ConnectedClient cc) {
        this.sock = sock;
        this.cc = cc;
        collectInfo();
        msgList = new LinkedList();
        start();
    }
    public synchronized void addMessage(String str) {
        if(cc.printMsg) System.out.println("MsgSender.addMessage: " +str);
        msgList.addLast(str);
    }
    private void collectInfo() {
    }
    public void run() {
        try {
            PrintWriter output = new PrintWriter(new OutputStreamWriter(sock.getOutputStream(),"UTF-8"),true);
            while(running) {
                while(msgList.size()>0) {
                    String toSend = (String)(msgList.removeFirst());
                    output.println(toSend + MainServer.MSGENDCHAR);
                    System.out.println(">>Ssend:" + toSend + MainServer.MSGENDCHAR);
                    if(cc.printMsg) System.out.println("MsgSender.run: Sending: " + toSend);
                    sleep(10);
                }
                
                
                if(num != cc.getConnectionKeeper().users().size()) flag = 0;
                if(flag < 3) {
                	cc.runCommand("" + ClientKernel.COMMAND + "users");
                	flag++;
                }
                if(flag == 2) {
                	cc.getConnectionKeeper().sendFriend((ConnectedClient)cc.getConnectionKeeper().users().getLast());
                }
                num = cc.getConnectionKeeper().users().size();
                sleep(10);
            }
        } catch(Exception e) {
            String msg = e.getMessage();
            if(msg.startsWith(MainServer.DISCONNECTED) ||
                msg.startsWith(MainServer.DISCONNECTED_CLIENT)) {
                System.out.println("MsgSender.run Client disconnected nick: " + cc.nick);
                cc.dropClient();
            } else {
                System.out.println("MsgSender.run: Msg: " + msg);
                e.printStackTrace();
                cc.dropClient();
            }
        }
    }
    public void closeConnection() {
        running = false;
    }
}
class ServerMsgListener extends Thread {
    private LinkedList msgList;
    private Socket sock;
    private ConnectedClient cc;
    private boolean running = true;
    public ServerMsgListener(Socket s, ConnectedClient cc) {
        msgList = new LinkedList();
        sock = s;
        this.cc = cc;
        start();
    }
    public void closeConnection() {
        running = false;
    }
    public void run() {
        try {
//            BufferedInputStream buffIn = new BufferedInputStream(sock.getInputStream());
//            DataInputStream dataIn = new DataInputStream(buffIn);
            BufferedReader input = new BufferedReader(new InputStreamReader(sock.getInputStream(), "UTF-8"));
            while(running) {
                int c;
                boolean didRun = false;
                boolean isCommand = false;
                sleep(10);
                String strBuff = input.readLine();
                if(strBuff.length() > 0) {
                	if(!didRun) didRun = true;
                	if(strBuff.charAt(0) == 0xFD)	isCommand = true;
                	strBuff = strBuff.substring(0, strBuff.length() - 1);
                }
                System.out.println(">>Sreceive:" + strBuff);
                if(cc.verifyedCount>0 && !cc.verifyedBoolean && !isCommand) {
                    cc.verifyedCount--;
                    if(cc.verifyedCount==1) {
                        cc.sendMessage("You have failed to verify your nick");
                        cc.nick = "" + cc.portNumber;
                        cc.sendMessage("Your nick is " + cc.nick);
                    } else {
                        cc.sendMessage("type: \"/verify &lt;password&gt\" to verify your nick");
                    }
                }
                if(didRun) {
                	String toSend = "" + cc.nick + ":" + strBuff.toString();
                	String[] ff = toSend.toString().split(":");
                	if(ff.length == 3 && ff[1].equals("friendApply")) {
                		cc.getConnectionKeeper().addFriends(ff[0],ff[2]);
                		cc.getConnectionKeeper().addFriends(ff[2],ff[0]);
                	}
                    if(cc.printMsg) System.out.println("MsgListenet.run Sending msg: " + toSend);
                    if(!isCommand) cc.broadcastMessage(toSend);
                    else cc.runCommand(strBuff.toString());
                }
            }
        } catch(SocketException se) {
            if(se.getMessage().startsWith("Connection reset"))
                cc.dropClient();
        } catch(Exception e) {
            e.printStackTrace();
            cc.dropClient();
        }
    }
}

