package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

import mechanics.ErrorLog;



public class UDPServer implements Runnable{
	private DatagramSocket serverSocket;
	private GameServer gs;
	private boolean running = false;
	private byte[] buffer = new byte[1024];
	
	public UDPServer(String sAddress, int port, GameServer gs){
		this.gs = gs;
		try {
			serverSocket = new DatagramSocket(null);
			InetSocketAddress address = new InetSocketAddress(sAddress, port);
			serverSocket.setReuseAddress(true);
			serverSocket.bind(address);	
		//	System.out.println(serverSocket.getLocalAddress());
//			System.out.println(serverSocket.getInetAddress());
//			System.out.println(serverSocket.getLocalSocketAddress());
		} catch (SocketException e) {
			e.printStackTrace();
			ErrorLog.writeError("udp_cons.", e);
		}
		Thread udpServer = new Thread(this, "udp");
		running = true;
		udpServer.start();
	}


	@Override
	public void run() {
		while(running){
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			try {
				serverSocket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
				ErrorLog.writeError("udp_recieve", e);
			}
			manageInput(packet);
			
		}
		closeUDP();
	}
	
	private void manageInput(DatagramPacket packet){
		gs.manageUDPInput(packet);
	}
	
	public void send(DatagramPacket packet){
//		InetAddress address = null;
//		try {
//			address = InetAddress.getByName(sAddress);
//		} catch (UnknownHostException e) {
//			e.printStackTrace();
//		}
		
		//DatagramPacket spacket = new DatagramPacket(data, data.length, address, port);
		try {
			serverSocket.send(packet);
		//	System.out.println(packet.getPort());
		} catch (IOException e) {
			e.printStackTrace();
			ErrorLog.writeError("udp_input", e);
		}
	}
	
	private void closeUDP(){
		serverSocket.close();
	}

}
