package net;

import java.io.IOException;
import java.net.SocketException;
import java.util.LinkedList;

public class ClientWorker extends Thread {
	private Server server;
	private int inputTimeout = 2000;
	LinkedList<Client> clients = new LinkedList<Client>();

	public ClientWorker(Server server) {
		this.server = server;
		start();
	}

	public void run() {
		while (true) {
			try {
				update();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			if (this.isInterrupted() == false)
				processClients();
			else
				break;
		}
	}

	public void addClients(LinkedList<Client> sckClients) {
		this.clients.addAll(sckClients);
	}

	private synchronized void update() throws InterruptedException {
		pullClients(server);
		System.out.println("PULL!");
		if (clients.size() == 0) {
			System.out.println("Nothing to do.");
			if (server.isAlive()) {
				System.out.println("Server running. Waiting.");
				wait();
			} else {
				System.out.println("Server not running. Exit.");
				this.interrupt();
			}
		}
	}

	private void processClients() {

		while (clients.size() != 0) {
			Client currentClient = clients.removeFirst();
			System.out.println("Processing! (" + clients.size()
					+ " Clients in Queue)");
			try {
				currentClient.getSocket().setSoTimeout(inputTimeout);
			} catch (SocketException e) {
				e.printStackTrace();
			}
			processCurrentClient(currentClient);
			try {
				currentClient.getSocket().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void processCurrentClient(Client client) {
		while (true) {
			String rawPacket = client.read();
			if (rawPacket != null) {
				ProcessPacket(client, rawPacket);
			} else {
				break;
			}
		}
	}

	private void ProcessPacket(Client client, String rawPacket) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {

		}
		System.out.println("Incoming Packet : " + rawPacket);
		client.write(1);
	}

	private void pullClients(Server server) {
		server.pushClients(this);
	}
	
	public synchronized void shutdown() {
		this.interrupt();
		this.notify();
	}

	public int getInputTimeout() {
		return this.inputTimeout;
	}

	public void setInputTimeout(int inputTimeout) {
		this.inputTimeout = inputTimeout;
	}

	public int getClientCount() {
		return clients.size();
	}
}
