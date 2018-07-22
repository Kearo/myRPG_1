package server;

import java.net.DatagramPacket;

import mechanics.ErrorLog;
import world.World;

public class GameServerWorld implements Runnable{
	private boolean running;
	private World world;
	private int playerCounter;
	private GameServer gs;
	private boolean threadPause;
	private String mapName;

	public GameServerWorld(String worldName, GameServer gs){
		this.world = new World(worldName, this);
		this.gs = gs;
		this.mapName = worldName;
		playerCounter = 0;
		Thread gsw = new Thread(this, worldName);
		running = true;
		threadPause = false;
		gsw.start();
	}
	
	@Override
	public void run() {
		long interval = 20;
		long startTime;
		long endTime;
		
		while(running){
			startTime = System.currentTimeMillis();
			
			world.update();
			
			if(playerCounter == 0){
			//	pauseThread();
			}
			
			endTime = System.currentTimeMillis();
			
			try {
				Thread.sleep(endTime-startTime+interval);
			} catch (InterruptedException e) {
				ErrorLog.writeError("GSW_sleep", e);
				e.printStackTrace();
			}
		}
	}
	
	public void getInput(String input){
		if(threadPause){
			wakeUpThread();
		}
		world.manageUDPInput(input);
	}

	public void sendOnput(DatagramPacket packet){
		gs.manageUDPOutput(packet);
	}
	
	public void addPlayerCounter(){
		playerCounter++;
		if(threadPause){
			wakeUpThread();
		}
	}
	
	public void subPlayerCounter(){
		playerCounter--;
	}
	
	public World getWorld(){
		return world;
	}
	
	public int getPlayerCounter(){
		return playerCounter;
	}
	
	public void pauseThread(){
		threadPause = true;
		//Thread.suspend();
	}
	
	public void wakeUpThread(){
		threadPause = false;
		//Thread.
	}
	
	public String getMapName(){
		return mapName;
	}
	
	public String getIPofWorld(){
		return gs.getAddress();
	}
	
	public void close(){
		running = false;
	}

}
