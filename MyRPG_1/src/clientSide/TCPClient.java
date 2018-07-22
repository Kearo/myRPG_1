package clientSide;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import game.GameLogicManager;
import mechanics.ErrorLog;

public class TCPClient {
	private Socket socket;
	private DataInputStream input;
	private PrintWriter output;
	private boolean connected = false;
	private Thread recieve;
	private byte[] buffer;
	
	public TCPClient(String address, int port){
		connected = connect(address, port);
		if(connected){
			recieve();
		}
	}
	
	private boolean connect(String address, int port){
		try {
			socket = new Socket(address, port);
			setStreams();
			buffer = new byte[1024];
		} catch (IOException e) {
			e.printStackTrace();
			ErrorLog.writeError("clientConnect", e);
			return false;
		}	
		return true;
	}
	
	private void setStreams(){
		try {
			input = new DataInputStream(socket.getInputStream());
			output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream())); 
			output.flush();
		} catch (IOException e) {
			ErrorLog.writeError("clientStreams", e);
			e.printStackTrace();
		} 	
	}
	
	private void recieve(){
		recieve = new Thread(){
			@Override
			public void run(){ 	
				if(!connected){
					disconnect();
				}
				String message = null;
				while(connected){
					try {
						input.read(buffer);
						message = new String(buffer).trim();
						manageInput(message);
					} catch (IOException e) {
						e.printStackTrace();
						ErrorLog.writeError("clientInput", e);
						disconnect();
					}	
				}
			}
		};
		recieve.start();
	}
	
	private void manageInput(String message){
		System.out.println(message);
		if(message.equals("login_success")){
			send("connect_world/");
		}
		if(message.startsWith("worldLogIn")){
			String[] s = message.split("/");
			try {
				GameLogicManager.initOnline(InetAddress.getByName(s[1]), s[2]);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				ErrorLog.writeError("clientMessageInput", e);
			}
		}
		if(message.endsWith("access denied")){
			System.out.println("Wrong ID or PW");
			System.exit(0);
		}
	}
	
	
	public void send(String message){
		output.println(message);
		output.flush();
	}
	
	public void disconnect(){
		try {
			if(connected){
				connected = false;
				input.close();
				output.close();
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
			ErrorLog.writeError("clientDc", e);
		}		
	}
		
}