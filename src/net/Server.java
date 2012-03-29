package net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server extends Thread {
	private ServerSocket sckServer;
	private Socket sckClient;
	private LinkedList<Client> clients = new LinkedList<Client>();
	private int port = 11000;

	ExecutorService executor = Executors.newCachedThreadPool();

	public Server() {
		this.init();
	}

	public Server(int port) {
		this.port = port;
		this.init();
	}

	private void init() {
		try {
			sckServer = new ServerSocket(this.port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		start();
	}

	public int getPort() {
		return port;
	}

	public void close() {
		try {
			sckServer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		final ClientWorker cw = new ClientWorker(this);
		cw.start();
		try {
			while (true) {
				sckClient = sckServer.accept();
				System.out.println("Incoming Connection from "
						+ sckClient.getInetAddress());
				clients.add(new Client(sckClient));
				System.out.println(cw.getState());
				synchronized (cw) {
					if (cw.getState() == Thread.State.WAITING) {
						pushClients(cw);
						System.out.println("PUSH!");
						cw.notify();
					}
				}
			}
		} catch (IOException e) {
			System.err.println("Socket error.");
		}
	}
	
	public void pushClients(ClientWorker cw) {
		cw.addClients(clients);
		System.out.println(clients.size() + " clients received.");
		clients.clear();
	}
}