package client;

import client.ClientFrame;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Client extends Thread implements Closeable {

	private static final String EXIT = "<EXIT>";

	private Socket socket;
	private ClientFrame frame;
	private String nickName;

//	Потоки ввода/вывода
	private BufferedReader in;
	private PrintWriter out;

//	Данные об адресе сервера и порте
	InetAddress ip;
	int port;

	boolean stoped = true;

	public Client(ClientFrame frame, InetAddress ip, int port) {
		this.frame = frame;
		this.ip = ip;
		this.port = port;
	}

	public void setStoped() {
		stoped = true;
	}

	public void setNickname(String str) {
		nickName = str;
	}

	public void run() {
		try {
			socket = new Socket(ip, port);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			stoped = false;

//			Представляемся серверу
			out.println(nickName);

			while (!stoped) {
				try {
				String str = in.readLine();
				frame.print(str);
				}catch(IOException e) {
					frame.print("No answer form server. Try to restart.");
					e.printStackTrace();
					break;
				}
			}
		} catch (IOException e) {
			frame.print(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			frame.print(e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				frame.setEnableActions(false);
				close();
			} catch (IOException e) {
				frame.print(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public void send(String str) {
		out.println(str);
	}

	@Override
	public void close() throws IOException {
		send(EXIT);
		in.close();
		out.close();
		socket.close();
	}

}
