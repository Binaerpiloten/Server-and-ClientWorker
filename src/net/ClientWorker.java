package net;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.LinkedList;

public class ClientWorker extends Thread {
	private Server server;
	private int wakeuptime = 10000;
	private int inputTimeout = 2000;
	LinkedList<Client> clients = new LinkedList<Client>();

	public ClientWorker(Server server) {
		this.server = server;
	}

	public void run() {
		while (true) {
			try {
				update();
				if (this.isInterrupted() == false)
					processClients();
				else
					break;
			} catch (InterruptedException e) {
				System.out.println("Wake up!");
			} catch (SocketTimeoutException e) {
				System.out.println("Socket timeout.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void addClients(LinkedList<Client> sckClients) {
		this.clients.addAll(sckClients);
	}

	private synchronized void update() throws InterruptedException {
		while (clients.size() == 0) {
			server.pullClients(this);
			if (clients.size() == 0) {
				System.out.println("Nothing to do.");
				if (server.isAlive()) {
					System.out.println("Server running. Waiting.");
					wait(this.wakeuptime);
				} else {
					System.out.println("Server not running. Exit.");
					this.interrupt();
					break;
				}
			}
		}
	}

	private void processClients() throws InterruptedException, IOException {

		Client currentClient = clients.removeFirst();
		currentClient.getSocket().setSoTimeout(inputTimeout);
		System.out.println("Processing! (" + clients.size()
				+ " Clients in Queue)");
		processCurrentClient(currentClient);
		try {
			currentClient.getSocket().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void processCurrentClient(Client client) throws IOException {
		while (true) {
			String rawPacket = client.read();
			if (rawPacket != null) {
				ProcessPacket(client, rawPacket);
			} else {
				client.getSocket().close();
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

	public int getWakeuptime() {
		return this.wakeuptime;
	}

	public void setWakeuptime(int wakeuptime) {
		this.wakeuptime = wakeuptime;
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
