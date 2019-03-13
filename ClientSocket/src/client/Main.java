package client;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import client.ClientFrame;

public class Main {

	public static void main(String[] args) {

		try {
			new ClientFrame(Inet4Address.getLocalHost(), 8284).setVisible(true);
		} catch (UnknownHostException e) {
			System.out.println(e.getMessage());
		}
	}

}
