package net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server extends Thread {
	private ServerSocket sckServer;
	private Socket sckClient;
	private LinkedList<Client> clients = new LinkedList<Client>();
	private LinkedList<ClientWorker> workers = new LinkedList<ClientWorker>();
	private int port = 11000;
	private int maxWorkers = 1;

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
		getClientWorker();
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
		try {
			while (true) {
				sckClient = sckServer.accept();
				clients.add(new Client(sckClient));
				ClientWorker cw = getClientWorker();
				if (cw != null) {
					synchronized (cw) {
						pushClients(cw);
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
		clients.clear();
	}

	private ClientWorker startClientWorker() {
		ClientWorker cw = new ClientWorker(workers.size() + 1, this);
		workers.add(cw);
		return cw;
	}

	private ClientWorker getClientWorker() {
		for (ClientWorker currentClientWorker : this.workers) {
			if (currentClientWorker.getState() == Thread.State.TIMED_WAITING)
				return currentClientWorker;
		}
		if (workers.size() < this.maxWorkers)
			return startClientWorker();
		return null;
	}

	@SuppressWarnings("unused")
	private void shutdownClientWorker(ClientWorker cw) {
		workers.remove(cw);
		cw.shutdown();
	}
}