package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import mechanics.ErrorLog;


public class GameServer implements Runnable {
	private UDPServer udpServer;
	private LogInServer logInServer;
	private List<GameServerWorld> worlds;
	private Map<String, SocketAddress> loggedInIDs;
	ExecutorService executor;
	private boolean running;
	private Thread gs;
	private String address;
	

	public GameServer(String address) {	
		this.address = address;
		ErrorLog.initLog();
		initTCP_UDP(address);
		initWorlds();
		initExecuter();
		
		running = true;
		
		gs = new Thread(this, "GameServer");
		gs.start();
	}

	private void initWorlds() {
		worlds = new ArrayList<GameServerWorld>();
		loggedInIDs = new HashMap<String, SocketAddress>();
		
		File directory = new File("./textures/worldtiles/sheets/worlds");
		File[] f = directory.listFiles();
		for (int i = 0; i < f.length; i++) {
			GameServerWorld gsw = new GameServerWorld(f[i].getName(), this);
			worlds.add(gsw);
		}
	}
	
	private void initExecuter(){
		executor = Executors.newCachedThreadPool();
	}
	
	private void initTCP_UDP(String address){
		udpServer = new UDPServer(address, 8888, this);
		logInServer = new LogInServer(address, 8889, this);
	}

	public void execute(GameServerWorld gsw) {// todo
		// System.out.println(gsw.getMapName() + " " + gsw.getPlayerCounter());
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
			ErrorLog.writeError("GS_execute", e);
		}
	}
	
	public String getWorlds(){
		return worlds.get(0).getMapName();
	}
	
	public boolean addPlayer(String loginID, SocketAddress sa){
		if(loggedInIDs.containsKey(loginID)){
			alreadyConnected(loginID);
			return false;
		}else{
			loggedInIDs.put(loginID, sa);
			return true;
		}
	}
	
	public void removePlayer(String loginID, SocketAddress sa){
		loggedInIDs.remove(loginID);
		logInServer.disconnectClient(sa);
	}
	
	public void alreadyConnected(String loginID){
		logInServer.disconnectClient(loggedInIDs.get(loginID));
		loggedInIDs.remove(loginID);
	}

	public void manageUDPInput(DatagramPacket packet) {
		String input = new String(packet.getData(), 0 , packet.getLength());
		String[] toWorld = input.split("/");
		for (int i = 0; i < worlds.size(); i++) {
			if (worlds.get(i).getMapName().equals(toWorld[0])) {
				String[] s = input.split("/", 2);
				input = s[1];
				worlds.get(i).getInput(input);
			}
		}
	}

	public void manageUDPOutput(DatagramPacket packet) {
		udpServer.send(packet);
	}
	
	public void worldToClientsMessage(String message){
		logInServer.sendToAll(message);
	}
	
	public String getAddress(){
		return address;
	}

	@Override
	public void run() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String input;
		while (running) {
			input = null;
			System.out.println("Enter new command!");
			try {
				input = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				ErrorLog.writeError("GS_input", e);
			}
			switch (input) {
			case "exit":
				System.out.println("Close Programm");
				System.exit(0);
				break;
			}
			System.out.println();
		}
	}
}
