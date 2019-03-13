package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class Server {
	private static final String EXIT = "<EXIT>";
	private static final String GET_LIST = "<GET_LIST>";
	private static final String JOIN_ROOM = "<JOIN_ROOM>";
	private static final String LEAVE_ROOM = "<LEAVE_ROOM>";
	private static final String DEFAULT_ROOM = "default";

	private List<Connection> connections = Collections.synchronizedList(new ArrayList<>());

	private ServerSocket serverSocket;

	SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	Date date;

	public static void main(String[] args) {
		new Server(8284);
	}

	Server(int port) {
		try {
			serverSocket = new ServerSocket(port);
			Scanner scanner = new Scanner(System.in);
			while (true) {
				System.out.println("Ожидание нового соединения");
				Socket socket = serverSocket.accept();
				connections.add(new Connection(socket));
				System.out.println("Подключен новый клиент");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeAll();
		}
	}

	private void closeAll() {
		try {
			serverSocket.close();
			synchronized (connections) {
				for (Connection connection : connections) {
					connection.close();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private class Connection extends Thread {

//		Потоки ввода/вывода
		private BufferedReader in;
		private PrintWriter out;

//		Данные о подключенном клиенте
		private String nickName;
		private Socket socket;

//		Данные о чат-комнате
		private String roomName = DEFAULT_ROOM;

		public Connection(Socket socket) {
			this.socket = socket;
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);
				start();
			} catch (IOException e) {
				e.printStackTrace();
				close();
			}
		}

		public void run() {
			try {
				date = new Date();
				nickName = in.readLine();
				synchronized (connections) {
					for (Connection connection : connections) {
						connection.out.println(dateFormat.format(date) + " " + this.nickName + " has joined");
					}
				}

				String str = "";
				while (!str.equals(EXIT)) {
					str = in.readLine();

					switch (str) {
//					Вывод списка
					case GET_LIST:
						this.out.println("Chat list:");
						synchronized (connections) {
							for (Connection connection : connections) {
								this.out.println(connection.getNickname());
							}
						}
						break;
//					Подключение к чат-комнате
					case JOIN_ROOM:
						roomName = in.readLine();
						print("Joined the room " + roomName);
						break;
//					Отключение из чат-комнаты
					case LEAVE_ROOM:
						print("Left the room");
						roomName = DEFAULT_ROOM;
						break;
					default:
						print(str);
						break;
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				close();
				connections.remove(this);
			}
		}

		public boolean isRoommate(String hisRoom) {
			if (roomName.equals(hisRoom))
				return true;
			return false;
		}

		public void print(String str) {
			synchronized (connections) {
				for (Connection connection : connections) {
					if (connection.isRoommate(roomName))
						connection.out.println("<" + this.nickName + ">:" + str);
				}
			}
		}

		public String getNickname() {
			return nickName;
		}

		private void close() {
			try {
				date = new Date();
				print("has left at: " + dateFormat.format(date));
				socket.close();
				in.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
