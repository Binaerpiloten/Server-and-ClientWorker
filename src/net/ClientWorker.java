package net;

import java.io.IOException;
import java.net.SocketException;
import java.util.LinkedList;

public class ClientWorker extends Thread {
	private Server server;
	private int id;
	private int inputTimeout = 000;
	private int wakeupTime = 20000;
	LinkedList<Client> clients = new LinkedList<Client>();

	public ClientWorker(int id, Server server) {
		this.server = server;
		this.id = id;
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
		while (clients.size() == 0) {
			if (server.isAlive()) {
				System.out.println(this.id + ": Server running. Waiting.");
				wait(this.wakeupTime);
			} else {
				System.out.println(this.id + ": Server not running. Exit.");
				this.interrupt();
				break;
			}
		}
	}

	private void processClients() {

		while (clients.size() != 0) {
			Client currentClient = clients.removeFirst();
			System.out.println(this.id + ": Processing! (" + clients.size()
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
		System.out.println(this.id + ": Client done.");
	}

	private void ProcessPacket(Client client, String rawPacket) {
		// TODO: Place code here.
		client.write(true);
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
