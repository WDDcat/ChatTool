package com.cncd.ch04.server;

import java.util.ArrayList;

public class Friend {
	public String nick;
	public ArrayList<String> friend;
	
	Friend(String s){
		this.nick = s;
		friend = new ArrayList<String>();
	}
}
