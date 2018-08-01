package game;

import java.net.DatagramPacket;
import java.net.InetAddress;

import clientSide.TCPClient;
import clientSide.UDPClient;
import gui.Gui;
import io.Window;
import mechanics.ErrorLog;
import render.Shader;
import world.TileRenderer;
import world.World;

public class GameLogicManager implements Runnable{
	private static boolean running = false;
	private static boolean online = true;
	private static World world;
	private static UDPClient udp;
	private static TCPClient tcp;
	private static String playerName = "test2";
	private static String pw = "1234";
	private static String address = "192.168.123.27";//192.168.123.27
	private Window window;
	
	public GameLogicManager(boolean worldIP, String ip){
		if(worldIP){
			address = ip;
		}
		window = GameManager.getWindow();
		Gui.initGui();
		init();
	}
	
	public void startThread(){
		Thread logic = new Thread(this, "logic");
		logic.start();
	}
	
	private static void init(){
		if(online){
			World.initTex();	
			tcp = new TCPClient(address, 8889);
			
			tcp.send("login/"+playerName+"/"+pw);
		}else{
			World.initTex();
			world = new World(GameManager.getWindow().getWidth(), GameManager.getWindow().getHeight(), 0);
			world.calculateView(GameManager.getWindow());
			world.addMainPlayer(playerName, "local");
			GameManager.loginSuccess();
			running = true;
		}
	}	
	
	public static void initOnline(InetAddress worldIP, String worldName ){
		udp = new UDPClient(address, 7777);
		world = new World(GameManager.getWindow().getWidth(), GameManager.getWindow().getHeight(), worldName, worldIP);	
		world.calculateView(GameManager.getWindow());
		world.addMainPlayer(playerName, address);
		
		GameManager.loginSuccess();
		running = true;
	}
	
	
	@Override
	public void run() {
		long interval = 20;
		long startTime;
		long endTime;
		
		while(!running){
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
				ErrorLog.writeError("GLM_sleep", e);
			}
		}
		while(running){
			startTime = System.currentTimeMillis();
			
			world.getKeyInput(window.getInput().getactionList());
			world.update();
			world.correctCamera(GameManager.getWindow());
			
			endTime = System.currentTimeMillis();
			try {
				Thread.sleep(endTime-startTime+interval);
			} catch (InterruptedException e) {
				e.printStackTrace();
				ErrorLog.writeError("GLM_sleep2", e);
			}
		}
		if(online){
			udp.close();
			tcp.disconnect();
		}
	}
	
	public static void getUDP(DatagramPacket packet){
		String input = new String(packet.getData(), 0, packet.getLength());
		String[] s = input.split("/", 2);
		input = s[1];
		world.manageUDPInput(input);
	}
	
	public static void sendUDP(DatagramPacket packet){
		udp.send(packet);
	}
	
	public static void setStoppped(){
		running = false;
	}
	
	public static void setWindow(){
		world.calculateView(GameManager.getWindow());
	//	world.correctCamera(GameManager.getWindow());
	}
	
	public static int getScale(){
		return world.getScale();
	}
	
	public static void render(Shader shader, TileRenderer render){
		world.render(render, shader);
	}
	
	public static boolean getMode(){
		return online;
	}
	
}
