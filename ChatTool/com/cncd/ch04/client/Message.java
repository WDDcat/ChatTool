package com.cncd.ch04.client;

public class Message {
	String message;
	String fromPort;
	String toPort;
	
	Message(String msg, String fp, String tp){
		message = msg;
		fromPort = fp;
		toPort = tp;
	}
}
