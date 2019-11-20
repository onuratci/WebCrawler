package com.jeejava.server;

public class ShutDown extends Thread {

	private final MyServer server;

	public ShutDown(MyServer server){
		this.server = server;
	}

	@Override
	public void run() {
		MyServer.shutDown(server);
	}
}
