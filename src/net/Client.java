package net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
	private final Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	
	public Client(Socket socket) {
		this.socket = socket;
		try {
			this.in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			this.out = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void write(Object s) {
			out.println(s);
	}
	
	public String read() {
		try {
			return in.readLine();
		} catch (IOException e) {
			return null;
		}
	}
	
	public BufferedReader getInputStream() {
		return in;
	}
	
	public PrintWriter getOutputStream() {
		return out;
	}
	
	public Socket getSocket() {
		return socket;
	}

}
