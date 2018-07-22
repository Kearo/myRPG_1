package clientSide;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

import game.GameLogicManager;
import mechanics.ErrorLog;


public class UDPClient implements Runnable{
	private boolean running = false;
	private DatagramSocket socket;
	 
	private byte[] buffer = new byte[1024];
	 
	    public UDPClient(String sAddress, int port) {
	        try {
	    		socket = new DatagramSocket(null);
				InetSocketAddress address = new InetSocketAddress(sAddress, port);
				socket.setReuseAddress(true);
				socket.bind(address);
			} catch (SocketException e1) {
				ErrorLog.writeError("udp_Constructor", e1);
				e1.printStackTrace();
			}
	        Thread udpClient = new Thread(this, "udpClient");
	        running = true;
	        udpClient.setDaemon(true);
	        udpClient.start();
	    }
	 	  	
		@Override
		public void run() {
			while(running){
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length );
				try {
					socket.receive(packet);
				} catch (IOException e) {
					e.printStackTrace();
					ErrorLog.writeError("udp_run", e);
				}
	            manageInput(packet);
			}
			close();
		} 
		
		private void manageInput(DatagramPacket packet){
			GameLogicManager.getUDP(packet);
		}
		
		public void send(DatagramPacket packet){
			try {
				socket.send(packet);	
			} catch (IOException e) {
				e.printStackTrace();
				ErrorLog.writeError("udp_send", e);
			}
		}
		
	    public void close() {
	    	running = false;
	        socket.close();
	    }
}