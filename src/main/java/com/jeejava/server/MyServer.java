package com.jeejava.server;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import com.jeejava.server.constant.ServerConstant;
import com.jeejava.server.handler.ServerResourceHandler;
import com.sun.net.httpserver.HttpServer;

public class MyServer implements Runnable {

	private static final Logger LOGGER = Logger.getLogger(MyServer.class.getName());

	private HttpServer httpServer;
	private ExecutorService executor;
	private  String serverHome;
	private  int port;

	public MyServer(String serverHome,int port) {
		this.serverHome = serverHome;
		this.port = port;
	}

	@Override
	public void run() {
		try {
			executor = Executors.newFixedThreadPool(10);

			httpServer = HttpServer.create(new InetSocketAddress(ServerConstant.DEFAULT_HOST, port), 0);
			httpServer.createContext(ServerConstant.FORWARD_SINGLE_SLASH, new ServerResourceHandler(
					serverHome + ServerConstant.FORWARD_SINGLE_SLASH + ServerConstant.WEBAPP_DIR, true, false));
			httpServer.setExecutor(executor);

			LOGGER.info("Starting server...");

			httpServer.start();

			LOGGER.info("Server started => " + ServerConstant.DEFAULT_HOST + ":" + port);

			// Wait here until shutdown is notified
			synchronized (this) {
				try {
					this.wait();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			LOGGER.severe("Error occurred during server starting..." + e);
		}
	}

	static void shutDown(MyServer server) {
		try {
			LOGGER.info("Shutting down server...");
			server.httpServer.stop(0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		synchronized (server) {
			server.notifyAll();
		}
	}

}
