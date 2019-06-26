package com.cncd.ch04.client;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
public class ChatClient extends JFrame implements KeyListener, ActionListener, FocusListener {
    public static final String appName = "Chat Tool";
    public static final String serverText = "127.0.0.1";
    public static final String portText = "3500";
    public static final String nickText = "YourName";
    JDialog d;
    JPanel northPanel, southPanel, centerPanel;
    JTextField txtHost, txtPort, msgWindow, txtNick;
    JButton buttonConnect, buttonSend, buttonAllUsers, buttonFriends,buttonAddFriend;
    JScrollPane sc;
    JList<String> ls;
    Vector<String> users;
    ClientKernel ck;
    ClientHistory historyWindow;
    private String lastMsg = "";
    public int flag = 0;
    /** Creates a new instance of Class */
    public ChatClient() {
        uiInit();
        txtHost.setText("127.0.0.1");
        txtPort.setText("3500");
    }
    public void uiInit() {
        setLayout(new BorderLayout());
        //创建North
        northPanel = new JPanel(new GridLayout(0,2));
        northPanel.add(new JLabel("Host address:"));
        northPanel.add(txtHost = new JTextField(ChatClient.serverText));
        northPanel.add(new JLabel("Port:"));
        northPanel.add(txtPort = new JTextField(ChatClient.portText));
        northPanel.add(new JLabel("Nick:"));
        northPanel.add(txtNick = new JTextField(ChatClient.nickText));
        northPanel.add(buttonAllUsers = new JButton("All Users"));
        northPanel.add(buttonAddFriend = new JButton("ADD"));
        northPanel.add(buttonFriends = new JButton("Friends"));
        northPanel.add(buttonConnect = new JButton("Connect"));
        buttonConnect.addActionListener(this);
        buttonAllUsers.addActionListener(this);
        buttonFriends.addActionListener(this);
        buttonAddFriend.addActionListener(this);
        txtHost.addKeyListener(this);
        txtHost.addFocusListener(this);
        txtNick.addFocusListener(this);
        txtNick.addKeyListener(this);
        txtPort.addKeyListener(this);
        txtPort.addFocusListener(this);
        buttonConnect.addKeyListener(this);
        this.add(northPanel, BorderLayout.NORTH);
        
        //创建Sourth
        southPanel = new JPanel();
        southPanel.add(msgWindow = new JTextField(20));
        southPanel.add(buttonSend = new JButton("Send"));
        buttonSend.addActionListener(this);
        msgWindow.addKeyListener(this);
        add(southPanel, BorderLayout.SOUTH);
        
        //创建Center
        centerPanel = new JPanel(new GridLayout(0,2));
        users = new Vector<>();
        ls = new JList(users);
        ls.setBorder(BorderFactory.createTitledBorder("Users"));
        users.add("ChatServer");
        ls.setSelectedIndex(0);
        centerPanel.add(ls);
        ls.addFocusListener(this);
        historyWindow = new ClientHistory();
        sc = new JScrollPane(historyWindow);
        sc.setAutoscrolls(true);
        centerPanel.add(sc);
        this.add(centerPanel, BorderLayout.CENTER);
        
//        //创建弹窗
//        d = new JDialog();
    }
    public void addFriend() {
    	if(!ck.nick.equals(users.get(ls.getLeadSelectionIndex()).toString())) {
    		ck.addFriend(users.get(ls.getLeadSelectionIndex()).toString());
        }
    }
    public void refreshUsers(ArrayList<String> u) {
    	if(this.flag == 0) {
    	users.clear();
    	users.add("ChatServer");
    	for(int i = 0; i < u.size(); i++) {
    		users.add(u.get(i));
    	}
    	ls.setListData(users);
    	ls.revalidate();
    	ls.repaint();
    	ls.setSelectedIndex(0); 
    	}
    	else {
    	users.clear();
    	users.add("Online Friend");
    	for(int i = 0; i < ck.friends.size(); i++) {
        	if(ck.checkFriendOnline(ck.friends.get(i),u))users.add(ck.friends.get(i));
//        	System.out.println(ck.friends.get(i));
        }
    	users.add("Offline Friend");
    	for(int i = 0; i < ck.friends.size(); i++) {
        	if(!ck.checkFriendOnline(ck.friends.get(i),u))users.add(ck.friends.get(i));
        }
//    	for(int i=0;i<users.size();i++) System.out.println(users.get(i));
    	ls.setListData(users);
        ls.revalidate();
        ls.repaint();
        ls.setSelectedIndex(0);
    	}
    }
    public static void main(String args[]) {
        ChatClient client = new ChatClient();
        client.setTitle(client.appName);
        client.setSize(450, 500);
        client.setLocation(100,100);
        client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.setVisible(true);
        client.msgWindow.requestFocus();
    }
    public void addMsg(String str) {
    	String[] spliter = str.split(":");
    	if(spliter[0].equals(ck.nick)) {
    		historyWindow.addText("<font color=\"#00ff00\">" + spliter[1]);
    	}
    	else {
    		historyWindow.addText(str);
    	}
    }
    private void connect() {
        try {
            if(ck!=null) ck.dropMe();
            ck = new ClientKernel(txtHost.getText(), Integer.parseInt(txtPort.getText()));
            ck.setNick(txtNick.getText());
            if(ck.isConnected()) {
                ck.addClient(this);
                addMsg("<font color=\"#0000ff\">connected! Local Port:" + ck.getLocalPort() + "</font>");
            } else {
                addMsg("<font color=\"#ff0000\">connect failed！</font>");
            }
        } catch(Exception e) { e.printStackTrace(); }
    }
    private void send() {
        String toSend = msgWindow.getText();
        if(!ck.nick.equals(users.get(ls.getLeadSelectionIndex()).toString())) {
//        	Message msg = new Message(toSend, ck.nick, users.get(ls.getLeadSelectionIndex()).toString());
        	String msg = toSend + " /" + users.get(ls.getLeadSelectionIndex()).toString();
        	ck.sendMessage(msg);
        	lastMsg = toSend;
        }
        msgWindow.setText("");
    }
    public void sendFriend() {
    	String FriendNick = users.get(ls.getLeadSelectionIndex()).toString();
    	String msg = "friendApply:" + FriendNick;
    	ck.sendMessage(msg);
    }
    public void keyPressed(KeyEvent e) {
    }
    public void keyReleased(KeyEvent e) {
        if(e.getSource() == msgWindow && e.getKeyCode() == KeyEvent.VK_UP) msgWindow.setText(lastMsg);
    }
    public void keyTyped(KeyEvent e) {
        if(e.getKeyChar() ==KeyEvent.VK_ENTER) {
            if(e.getSource() == msgWindow) send();
            if(e.getSource() == txtNick) { connect(); msgWindow.requestFocus(); }
            if(e.getSource() == txtHost) txtPort.requestFocus();
            if(e.getSource() == txtPort) txtNick.requestFocus();
        }
    }
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==buttonConnect) connect();
        if(e.getSource()==buttonSend) send();
        if(e.getSource()==buttonAllUsers) {this.flag = 0;
        this.refreshUsers(ck.AllUsers);}
        if(e.getSource()==buttonAddFriend) {addFriend();
        sendFriend();} 
        if(e.getSource()==buttonFriends) {this.flag = 1;
        this.refreshUsers(ck.AllUsers);};
    }
    public void focusGained(FocusEvent e) {
        if(e.getSource()==txtHost && txtHost.getText().equals(ChatClient.serverText)) txtHost.setText("");
        if(e.getSource()==txtPort && txtPort.getText().equals(ChatClient.portText)) txtPort.setText("");
        if(e.getSource()==txtNick && txtNick.getText().equals(ChatClient.nickText)) txtNick.setText("");
    }
    public void focusLost(FocusEvent e) {
       if(e.getSource()==txtPort && txtPort.getText().equals("")) txtPort.setText(ChatClient.portText);
       if(e.getSource()==txtHost && txtHost.getText().equals("")) txtHost.setText(ChatClient.serverText);
       if(e.getSource()==txtNick && txtNick.getText().equals(ChatClient.nickText)) 
                                                            txtNick.setText(ChatClient.nickText);
    }
    class ClientHistory extends JEditorPane {
        public ClientHistory() {
            super("text/html", "" + ChatClient.appName);
            setEditable(false);
            setAutoscrolls(true);
        }
        public void addText(String str) {
            String html = getText();
            int end = html.lastIndexOf("</body>");
            String startStr = html.substring(0, end);
            String endStr = html.substring(end, html.length());
            String newHtml = startStr + "<br>" + str + endStr;
            setText(newHtml);
            setSelectionStart(newHtml.length()-1);
            setSelectionEnd(newHtml.length());
         }
        public void clear() {
            setText("");
        }
    }
    
}

