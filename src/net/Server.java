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
	private LinkedList<ClientWorker> workers = new LinkedList<ClientWorker>();
	private int port = 11000;
	private int maxWorkers = 1;

	ExecutorService executor = Executors.newCachedThreadPool();

	public Server() {
		this.init();
	}

	public Server(int port) {
		this.port = port;
		this.init();
	}
	
	public Server(int port, int maxWorkers) {
		this.port = port;
		this.maxWorkers = maxWorkers;
		this.init();
	}

	private void init() {
		try {
			sckServer = new ServerSocket(this.port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		start();
		workers.add(new ClientWorker(this));
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
		boolean clientProcessed;
		try {
			while (true) {
				clientProcessed = false;
				sckClient = sckServer.accept();
//				System.out.println("Incoming Connection from " + sckClient.getInetAddress());
				clients.add(new Client(sckClient));
				for (ClientWorker currentWorker : workers) {
					if (currentWorker.getState() == Thread.State.TIMED_WAITING) {
						synchronized (currentWorker) {
							pushClients(currentWorker);
//							System.out.println("PUSH!");
							currentWorker.notify();
							clientProcessed = true;
							break;
						}
					}
				}
				if (!clientProcessed && workers.size() < this.maxWorkers) {
					System.out.println("All ClientWorkers busy - starting new one.");
					ClientWorker newCw = new ClientWorker(this); 
					workers.add(newCw);
				}
			}
		} catch (IOException e) {
			System.err.println("Socket error.");
		}
	}

	public void pushClients(ClientWorker cw) {
		cw.addClients(clients);
//		System.out.println(clients.size() + " clients received.");
		clients.clear();
	}
	
	private void shutdownClientWorker(ClientWorker cw) {
		workers.remove(cw);
		cw.shutdown();
	}
}