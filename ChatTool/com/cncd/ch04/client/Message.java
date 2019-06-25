package com.cncd.ch04.client;

public class Message {
	String msg;
	int fromPort;
	int toPort;
	String toPor;
	
	Message(String message, int fp, String tp){
		msg = message;
		fromPort = fp;
		toPor = tp;
	}
}
