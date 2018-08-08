package world;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import basicGameObjects.Enemy;
import basicGameObjects.Mainplayer;
import basicGameObjects.Players;
import basicGameObjects.Skill;
import game.GameLogicManager;
import io.Window;
import mechanics.ErrorLog;
import render.Camera;
import render.Shader;
import render.Transform;
import server.GameServerWorld;

public class World {
	private float viewX, viewY, screenY, screenX;
	private int width;
	private int height;
	private int scale;
	private Mainplayer mainPlayer;
	private List<Players> players;
	private List<Enemy> enemys;
	private List<Skill> activeSkills;
	private List<String> input;
	private List<Integer> remover;
	private byte[] tiles;
	private Transform transform;
	private Matrix4f world;
	private Camera camera;
	// private TileSheet worldtiles;
	private static float mouseX, mouseY;
	private boolean online;
	private int globalEnemyCounter = 0;
	private int maxMopsOnMap;
	private int mopRespawnTimer = 100;
	private int fastMopRespawnTimer = 20;
	private int respawnTimer = fastMopRespawnTimer;
	private String mapName;
	private GameServerWorld gsw;
	private boolean serverWorld = false;
	private int port;
	private InetAddress iaServer;
	private int mopIDcounter = 0;
	private int skillIDcounter = 0;
	// private boolean firstConnect = true;
	private List<InetAddress> addresses;
	private List<String> sendBuffer;
	private int sendRadius = 30;

	public World(int winx, int winy, int z) { // offlineMode
		try {
			BufferedImage worldsheet = ImageIO
					.read(new File("./textures/worldtiles/sheets/worlds/" + "worldsheet.png"));
			// worldtiles = new TileSheet("worldtiles/sheets/" +
			// "world_sheet.png", 16);

			online = false;

			width = worldsheet.getWidth();
			height = worldsheet.getHeight();
			scale = 32;

			setMaxMopsOnMap();

			camera = new Camera(winx, winy);

			enemys = new ArrayList<Enemy>();
			activeSkills = new ArrayList<Skill>();
			players = new ArrayList<Players>();
			input = new ArrayList<String>();
			remover = new ArrayList<Integer>();

			this.world = new Matrix4f().setTranslation(new Vector3f(0));
			this.world.scale(scale);

			int[] colorTileSheet = worldsheet.getRGB(0, 0, width, height, null, 0, width);

			tiles = new byte[width * height];

			transform = new Transform();

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int red = (colorTileSheet[x + y * width] >> 16) & 0xFF;
					Tile t;
					try {
						t = Tile.tiles[red];
					} catch (ArrayIndexOutOfBoundsException e) {
						t = null;
						ErrorLog.writeError("world_array", e);
					}
					if (t != null) {
						if (t == Tile.dirt) {
						}
						setTile(t, x, y);
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
			ErrorLog.writeError("world_cons.1", e);
		}
	}

	public World(int winx, int winy, String world, InetAddress ip) {// online
																	// mode,
																	// clientSide
		BufferedImage worldsheet = null;
		try {
			worldsheet = ImageIO.read(new File("./textures/worldtiles/sheets/worlds/" + world));
		} catch (IOException e) {
			e.printStackTrace();
			ErrorLog.writeError("world_imageRead", e);
		}

		online = true;
		iaServer = ip;

		width = worldsheet.getWidth();
		height = worldsheet.getHeight();
		scale = 32;
		mapName = world;

		setMaxMopsOnMap();

		camera = new Camera(winx, winy);
		enemys = new ArrayList<Enemy>();
		activeSkills = new ArrayList<Skill>();
		players = new ArrayList<Players>();
		input = new ArrayList<String>();
		remover = new ArrayList<Integer>();

		this.world = new Matrix4f().setTranslation(new Vector3f(0));
		this.world.scale(scale);

		int[] colorTileSheet = worldsheet.getRGB(0, 0, width, height, null, 0, width);

		tiles = new byte[width * height];

		transform = new Transform();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int red = (colorTileSheet[x + y * width] >> 16) & 0xFF;
				Tile t;
				try {
					t = Tile.tiles[red];
				} catch (ArrayIndexOutOfBoundsException e) {
					t = null;
					ErrorLog.writeError("world_array", e);
				}
				if (t != null) {
					if (t == Tile.dirt) {
					}
					setTile(t, x, y);
				}
			}
		}
	}

	public World(String world, GameServerWorld gsw) {// online mode, serverMap
		BufferedImage worldsheet = null;
		try {
			worldsheet = ImageIO.read(new File("./textures/worldtiles/sheets/worlds/" + world));
		} catch (IOException e) {
			e.printStackTrace();
			ErrorLog.writeError("logIn_imageRead", e);
		}
		this.gsw = gsw;
		online = true;
		mapName = world;
		serverWorld = true;

		width = worldsheet.getWidth();
		height = worldsheet.getHeight();
		scale = 32;

		setMaxMopsOnMap();

		enemys = new ArrayList<Enemy>();
		activeSkills = new ArrayList<Skill>();
		players = new ArrayList<Players>();
		input = new ArrayList<String>();
		remover = new ArrayList<Integer>();

		addresses = new ArrayList<InetAddress>();
		sendBuffer = new ArrayList<String>();

		this.world = new Matrix4f().setTranslation(new Vector3f(0));
		this.world.scale(scale);

		int[] colorTileSheet = worldsheet.getRGB(0, 0, width, height, null, 0, width);

		tiles = new byte[width * height];

		transform = new Transform();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int red = (colorTileSheet[x + y * width] >> 16) & 0xFF;
				Tile t;
				try {
					t = Tile.tiles[red];
				} catch (ArrayIndexOutOfBoundsException e) {
					t = null;
					ErrorLog.writeError("logIn_array", e);
				}
				if (t != null) {
					if (t == Tile.dirt) {
					}
					setTile(t, x, y);
				}
			}
		}
	}

	public void setTile(Tile tile, int x, int y) {
		tiles[x + y * width] = tile.getId();
	}

	public Matrix4f getWorldMatrix() {
		return world;
	}

	public void calculateView(Window window) {
		camera.setProjection(window.getWidth(), window.getHeight());
		screenX = window.getWidth();
		screenY = window.getHeight();
		viewX = (window.getWidth() / (scale * 2) + 2);
		viewY = (window.getHeight() / (scale * 2) + 2);
	}

	public void update() {
		if (online) {
			onlineMode();
		} else {
			offlineMode();
		}
	}

	private void offlineMode() {
		manageInput();
		if (mainPlayer != null) {
			mainPlayer.update(camera, this);
		}
		for (int i = 0; i < activeSkills.size(); i++) {
			activeSkills.get(i).update();
			if (activeSkills.get(i).getDelete()) {
				activeSkills.remove(i);
			}
		}

		for (int i = 0; i < enemys.size(); i++) {
			enemys.get(i).update();
			if (enemys.get(i).getDelete()) {
				enemys.remove(i);
				globalEnemyCounter--;
			}
		}
		if (globalEnemyCounter < maxMopsOnMap && respawnTimer <= 0) {
			addEnemy();
			if (globalEnemyCounter < maxMopsOnMap / 3) {
				respawnTimer = fastMopRespawnTimer;
			} else {
				respawnTimer = mopRespawnTimer;
			}
		} else {
			respawnTimer--;
		}
	}

	private void onlineMode() {
		for (Enemy e : enemys) {
			e.update();
			if (e.getDelete()) {
				remover.add(enemys.indexOf(e));
				globalEnemyCounter--;
			}
		}
		for (int i = remover.size() - 1; i >= 0; i--) {
			enemys.remove(i);
		}
		remover.clear();

		if (serverWorld) {
			if (globalEnemyCounter < maxMopsOnMap && respawnTimer <= 0) {
				// addEnemy();
				if (globalEnemyCounter < maxMopsOnMap / 3) {
					respawnTimer = fastMopRespawnTimer;
				} else {
					respawnTimer = mopRespawnTimer;
				}
			} else {
				respawnTimer--;
			}
		}

		for (Players p : players) {// todo
			p.update();
			if (p.getDelete()) {
				remover.add(activeSkills.indexOf(p));
			}
		}
		for (int i = remover.size() - 1; i >= 0; i--) {
			players.remove(i);
		}
		remover.clear();

		for (Skill s : activeSkills) {
			s.update();
			if (s.getDelete()) {
				remover.add(activeSkills.indexOf(s));
			}
		}
		for (int i = remover.size() - 1; i >= 0; i--) {
			activeSkills.remove(i);
		}
		remover.clear();

		if (!serverWorld) {
			manageInput();
			mainPlayer.update(camera, this);
		}
		if (serverWorld) {
			List<String> tempBuffer = new ArrayList<String>(sendBuffer);
			sendBuffer.clear();
			for (Players p : players) {
				boolean moveIndicator = false;
				if (p.getMoved()) {
					moveIndicator = true;
				}
				float x = p.getTransform().pos.x + p.getTransform().scale.x;
				float y = p.getTransform().pos.y - p.getTransform().scale.y;
				float xdif;
				float ydif;
				float sx, sy;
				if (tempBuffer.size() > 0) {
					for (String s : tempBuffer) {// String same as input
						sx = 0;
						sy = 0;
						xdif = 0;
						ydif = 0;
						String[] q = s.split("/");
						switch (q[0]) {
						case "spawn": // (monster/player)/ID/InetAdress/posX/posY/
							// monster, no inet place
							// statsLAter
							if (q[1].equals("monster")) {
								sx = Float.parseFloat(q[3]);
								sy = Float.parseFloat(q[4]);
							} else {
								sx = Float.parseFloat(q[5]);
								sy = Float.parseFloat(q[6]);
							}
							break;
						case "move": // (monster/player)/ID/directionX/directionY/posX/posY/
							sx = Float.parseFloat(q[5]);
							sy = Float.parseFloat(q[6]);
							break;
						case "turn": // (monster/player)/ID/turnDegree/posX/posY/
							sx = Float.parseFloat(q[4]);
							sy = Float.parseFloat(q[5]);
							break;
						case "attack": // ID/posX/posY/invokerID/directionX/directionY/
							sx = Float.parseFloat(q[2]);
							sy = Float.parseFloat(q[3]);
							break;
						// case "getHit": //
						// (monster/player)/ID/InvokerID/attackID/posX/posY/
						// sx = Integer.parseInt(q[5]);
						// sy = Integer.parseInt(q[6]);
						// break;
						case "despawn": // (monster/player)/ID/poX/posY/
							sx = Integer.parseInt(q[3]);
							sy = Integer.parseInt(q[4]);
							break;
						}

						xdif = x - sx;
						ydif = y - sy;

						xdif = Math.abs(xdif);
						ydif = Math.abs(ydif);
						if (xdif <= sendRadius && ydif <= sendRadius) {
							manageUDPOutput(s, p.getIp().toString());
						}
					}
				}
				if (moveIndicator) {
					y = Math.abs(y);
					for (Players w : players) {
						if (!w.getMoved()) {
							xdif = 0;
							ydif = 0;
							float x2 = w.getTransform().pos.x + w.getTransform().scale.x;
							float y2 = w.getTransform().pos.y - w.getTransform().scale.y;
							y2 = Math.abs(y2);
							xdif = x - x2;
							ydif = y - y2;
							if (xdif <= sendRadius && ydif <= sendRadius) {
								manageUDPOutput("spawn/player/" + p.getID() + "/" + p.getIp() + "/"
										+ p.getTransform().pos.x + "/" + p.getTransform().pos.y + "/",
										p.getIp().toString());
							}
						}
						w.setMoved();
					}
					for (Skill s : activeSkills) {
						if (!s.getMoved()) {
							xdif = 0;
							ydif = 0;
							float x2 = s.getTransform().pos.x + s.getTransform().scale.x;
							float y2 = s.getTransform().pos.y - s.getTransform().scale.y;
							y2 = Math.abs(y2);
							xdif = x - x2;
							ydif = y - y2;
							if (xdif <= sendRadius && ydif <= sendRadius) {
								manageUDPOutput(
										new String("attack/" + s.getID() + "/" + s.getTransform().pos.x + "/"
												+ s.getTransform().pos.y + "/" + s.getInvokerID() + "/"
												+ s.getDirection().x + "/" + s.getDirection().y + "/"),
										p.getIp().toString());
							}
						}
						s.setMoved();
					}
					for (Enemy e : enemys) {
						if (!e.getMoved()) {
							xdif = 0;
							ydif = 0;
							float x2 = e.getTransform().pos.x + e.getTransform().scale.x;
							float y2 = e.getTransform().pos.y - e.getTransform().scale.y;
							y2 = Math.abs(y2);
							xdif = x - x2;
							ydif = y - y2;
							if (xdif <= sendRadius && ydif <= sendRadius) {
								manageUDPOutput("spawn/monster/" + e.getID() + "/" + e.getTransform().pos.x + "/"
										+ e.getTransform().pos.y + "/", p.getIp().toString());
							}
						}
						e.setMoved();
					}
				}
			}
			tempBuffer.clear();
		}
	}

	private void connect(Players mp) {
		float x = mp.getTransform().pos.x + mp.getTransform().scale.x;
		float y = mp.getTransform().pos.y - mp.getTransform().scale.y;
		float xdif = 0;
		float ydif = 0;
		y = Math.abs(y);
		for (Players p : players) {
			xdif = 0;
			ydif = 0;
			float x2 = p.getTransform().pos.x + p.getTransform().scale.x;
			float y2 = p.getTransform().pos.y - p.getTransform().scale.y;
			y2 = Math.abs(y2);
			xdif = x - x2;
			ydif = y - y2;
			if (xdif <= sendRadius && ydif <= sendRadius) {
				manageUDPOutput("spawn/player/" + p.getID() + "/" + p.getIp() + "/" + p.getTransform().pos.x + "/"
						+ p.getTransform().pos.y + "/", mp.getIp().toString());
			}
		}
		for (Enemy e : enemys) {
			xdif = 0;
			ydif = 0;
			float x2 = e.getTransform().pos.x + e.getTransform().scale.x;
			float y2 = e.getTransform().pos.y - e.getTransform().scale.y;
			y2 = Math.abs(y2);
			xdif = x - x2;
			ydif = y - y2;
			if (xdif <= sendRadius && ydif <= sendRadius) {
				manageUDPOutput("spawn/monster/" + e.getID() + "/" + e.getTransform().pos.x + "/"
						+ e.getTransform().pos.y + "/", mp.getIp().toString());
			}
		}
		for (Skill s : activeSkills) {
			xdif = 0;
			ydif = 0;
			float x2 = s.getTransform().pos.x + s.getTransform().scale.x;
			float y2 = s.getTransform().pos.y - s.getTransform().scale.y;
			y2 = Math.abs(y2);
			xdif = x - x2;
			ydif = y - y2;
			if (xdif <= sendRadius && ydif <= sendRadius) {
				manageUDPOutput(
						new String("attack/" + s.getID() + "/" + s.getTransform().pos.x + "/" + s.getTransform().pos.y
								+ "/" + s.getInvokerID() + "/" + s.getDirection().x + "/" + s.getDirection().y + "/"),
						mp.getIp().toString());
			}
		}
	}

	public void manageUDPInput(String input) {
		System.out.println(input);
		String[] distributor = input.split("/");
		if (serverWorld) {
			switch (distributor[0]) {
			case "login":
				try {
					addPlayer(distributor[1], InetAddress.getByName(distributor[2]), Float.parseFloat(distributor[3]),
							Float.parseFloat(distributor[4]));
				} catch (NumberFormatException | UnknownHostException e1) {
					e1.printStackTrace();
					ErrorLog.writeError("world_input_login", e1);
				}
				for (Players p : players) {
					if (p.getID().equals(distributor[1])) {
						connect(p);
						break;
					}
				}
				break;
			case "move": // ID/directionX/directionY/
				for (Players p : players) {
					if (p.getID().equals(distributor[1])) {
						p.setDirection(Float.parseFloat(distributor[2]), Float.parseFloat(distributor[3]), this);
						sendBuffer.add(
								new String("move/player/" + p.getID() + "/" + distributor[2] + "/" + distributor[3])
										+ "/" + p.getTransform().pos.x + "/" + p.getTransform().pos.y + "/");
						break;
					}
				}
				break;
			case "turn": // ID/turnDegree/
				break;
			case "attack": // ID/SkillbarNumber/directionX/directionY/InvokerID/
				for (Players p : players) {
					if (p.getID().equals(distributor[5])) {
						p.useSkill(this, Integer.parseInt(distributor[2]), Float.parseFloat(distributor[3]),
								Float.parseFloat(distributor[4]));
					}
				}
				break;
			case "disconnect": // ID/
				for (Players p : players) {
					if (p.getID().equals(distributor[1])) {
						p.setDelete();
						break;
					}
				}
				break;
			}
		} else {
			switch (distributor[0]) {
			case "spawn": // (monster/player)/ID/InetAdress/posX/posY/
				// Inet not by mosnter
				// statsLAter
				if (distributor[1].equals("monster")) {
					boolean temp = false;
					for (Enemy e : enemys) {
						if (e.getID().equals(distributor[2])) {
							temp = true;
							break;
						}
					}
					if (!temp) {
						addEnemy(distributor[2], Float.parseFloat(distributor[3]), Float.parseFloat(distributor[4]));
						enemys.get(enemys.size()-1).setInterpolatation(Float.parseFloat(distributor[5]), 
								Float.parseFloat(distributor[6]));
					}
				}
				if (distributor[1].equals("player")) {
					try {
						if (!mainPlayer.getID().equals(distributor[2])) {
							boolean temp = false;
							for (Players p : players) {
								if (p.getID().equals(distributor[2])) {
									temp = true;
									break;
								}
							}
							if(!temp){
								addPlayer(distributor[2], InetAddress.getByName(distributor[3]),
										Float.parseFloat(distributor[4]), Float.parseFloat(distributor[5]));
								players.get(players.size()-1).setInterpolatation(Float.parseFloat(distributor[5]),
										Float.parseFloat(distributor[6]));
							}
						}
					} catch (NumberFormatException | UnknownHostException e) {
						e.printStackTrace();
						ErrorLog.writeError("world_spawn", e);
					}
				}
				break;
			case "move": // (monster/player)/ID/directionX/directionY/posX/posY/
				if (distributor[1].equals("player")) {
					for (Players p : players) {
						if (p.getID().equals(distributor[2])) {
							p.setDirection(Float.parseFloat(distributor[3]), Float.parseFloat(distributor[4]), this);
							p.setInterpolatation(Float.parseFloat(distributor[5]), Float.parseFloat(distributor[6]));
							break;
						}
					}
				}
				if (distributor[1].equals("monster")) {
					for (Enemy e : enemys) {
						if (e.getID().equals(distributor[2])) {
							e.setDirection(Float.parseFloat(distributor[3]), Float.parseFloat(distributor[4]));
							e.setInterpolatation(Float.parseFloat(distributor[5]), Float.parseFloat(distributor[6]));
							break;
						}
					}
				}
				break;
			case "turn": // (monster/player)/ID/turnDegree/posX/posY/
				// coming soon
				break;
			case "attack": // ID/posX/posY/invokerID/directionX/directionY/
				boolean temp = false;
				for (Skill s : activeSkills) {
					if (s.getID().equals(distributor[1])) {
						temp = true;
					}
				}
				if (!temp) {
					addSkill(distributor[1], Float.parseFloat(distributor[2]), Float.parseFloat(distributor[3]),
							distributor[4], Float.parseFloat(distributor[5]), Float.parseFloat(distributor[6]));
				}
				break;
			case "getHit": // (monster/player)/ID/InvokerID/attackID/posX/posY/
				if (distributor[1].equals("player")) {
					for (Players p : players) {
						if (p.getID().equals(distributor[2])) {
							p.hit(distributor[4], distributor[3]);
							break;
						}
					}
				}
				if (distributor[1].equals("monster")) {
					for (Enemy e : enemys) {
						if (e.getID().equals(distributor[2])) {
							e.hit(distributor[4], distributor[3]);
							break;
						}
					}
				}
				break;
			case "despawn": // (monster/player)/ID/poX/posY/
				if (distributor[1].equals("player")) {
					for (Players p : players) {
						if (p.getID().equals(distributor[2])) {
							p.setDelete();
							break;
						}
					}
				}
				if (distributor[1].equals("monster")) {
					for (Enemy e : enemys) {
						if (e.getID().equals(distributor[2])) {
							e.setDelete();
							break;
						}
					}
				}
				break;

			case "sendItem":
				break;
			case "getItem":
				break;
			case "equipItem":
				break;
			case "dequipItem":
				break;
			case "useItem":
				break;
			case "throwItemAway":
				break;
			case "interactWithNPC":
				break;
			case "interactWithObject":
				break;
			case "skillInSkillbar":
				break;
			}
		}
	}

	public void manageUDPOutput(String output, String destinationIP) {
		byte[] data = new byte[1024];
		output = this.getMapName() + "/" + output;
		data = output.getBytes();
		InetAddress ip = null;
		if (!destinationIP.equals("clients")) {
			if (destinationIP.equals("server")) {
				try {
					if (iaServer == null) {
						ip = InetAddress.getByName(gsw.getIPofWorld());
					} else {
						ip = iaServer;
					}
					DatagramPacket packet = new DatagramPacket(data, data.length, ip, 8888);
					GameLogicManager.sendUDP(packet);
				} catch (UnknownHostException e) {
					e.printStackTrace();
					ErrorLog.writeError("world_out_ip", e);
				}
			} else {
				try {
					if (destinationIP.contains("/")) {
						destinationIP = destinationIP.replace("/", "");
					}
					ip = InetAddress.getByName(destinationIP);
					DatagramPacket packet = new DatagramPacket(data, data.length, ip, 7777);
					gsw.sendOnput(packet);
				} catch (UnknownHostException e) {
					e.printStackTrace();
					ErrorLog.writeError("world_out_ip", e);
				}
			}
		} else {
			for (int i = 0; i < players.size(); i++) {
				// System.out.println(players.get(i).getIp());
				DatagramPacket packet = new DatagramPacket(data, data.length, players.get(i).getIp(), 7777);
				gsw.sendOnput(packet);
			}
		}
	}

	private void setMaxMopsOnMap() {
		maxMopsOnMap = width * height / 2 / scale;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getPort() {
		return port;
	}

	public void addGameServerWorld(GameServerWorld gsw) {
		this.gsw = gsw;
	}

	public Tile getTile(int x, int y) {
		try {
			return Tile.tiles[tiles[x + y * width]];
		} catch (ArrayIndexOutOfBoundsException e) {
			ErrorLog.writeError("world_tile", e);
			return null;
		}
	}

	public void correctCamera(Window window) {
		Vector3f pos = camera.getPosition();

		int w = -width * scale * 2;
		int h = height * scale * 2;

		if (pos.x > -(window.getWidth() / 2) + scale) {
			pos.x = -(window.getWidth() / 2) + scale;
		}

		if (pos.x < w + (window.getWidth() / 2) + scale) {
			pos.x = w + (window.getWidth() / 2) + scale;
		}

		if (pos.y < (window.getHeight() / 2) - scale) {
			pos.y = (window.getHeight() / 2) - scale;
		}

		if (pos.y > h - (window.getHeight() / 2) - scale) {
			pos.y = h - (window.getHeight() / 2) - scale;
		}
	}

	public void render(TileRenderer render, Shader shader) {
		int posX = (int) camera.getPosition().x / (scale * 2);
		int posY = (int) camera.getPosition().y / (scale * 2);

		for (int i = 0; i < viewX; i++) {
			for (int j = 0; j < viewY + 1; j++) {
				Tile t = getTile(i - posX - ((int) viewX / 2) + 1, j + posY - ((int) viewY / 2));
				if (t != null) {
					render.renderTile(t, i - posX - ((int) viewX / 2) + 1, -j - posY + ((int) viewY / 2), shader, world,
							camera);
				}
				// worldtiles.bindTile(shader, 1, 1);
				// Assets.getModel().render();
			}
		}

		float xLeftCam = (viewX / 2 - 0.5f - camera.getPosition().x / -64) * -2;
		float xRightCam = (viewX / 2 + camera.getPosition().x / -64) * 2;
		float yTopCam = (viewY / 2 - 0.375f + camera.getPosition().y / -64) * 2;
		float yBotCam = -(viewY / 2 - camera.getPosition().y / -64) * 2;

		if (activeSkills.size() > 0) {
			for (int i = 0; i < activeSkills.size(); i++) {
				if (activeSkills.get(i).getTransform().pos.x > xLeftCam
						&& activeSkills.get(i).getTransform().pos.x < xRightCam) {
					if (activeSkills.get(i).getTransform().pos.y < yTopCam
							&& activeSkills.get(i).getTransform().pos.y > yBotCam) {
						activeSkills.get(i).render(shader, camera, this);
					}
				}
			}
		}

		if (enemys.size() > 0) {
			for (int i = 0; i < enemys.size(); i++) {
				if (enemys.get(i).getTransform().pos.x > xLeftCam && enemys.get(i).getTransform().pos.x < xRightCam) {
					if (enemys.get(i).getTransform().pos.y < yTopCam && enemys.get(i).getTransform().pos.y > yBotCam) {
						enemys.get(i).render(shader, camera, this);
					}
				}
			}
		}

		if (players.size() > 0) {
			for (int i = 0; i < players.size(); i++) {
				if (players.get(i).getTransform().pos.x > xLeftCam && players.get(i).getTransform().pos.x < xRightCam) {
					if (players.get(i).getTransform().pos.y < yTopCam
							&& players.get(i).getTransform().pos.y > yBotCam) {
						players.get(i).render(shader, camera, this);
					}
				}
			}
		}
		mainPlayer.render(shader, camera, this);
	}

	private void manageInput() {
		if (input.size() > 0) {
			for (int i = 0; i < input.size(); i++) {
				useKeyInput(input.get(i));
			}
		}
	}

	private void useKeyInput(String input) {
		if (input != null) {
			switch (input.substring(0, 1)) {
			case "p":
				mainPlayer.setActions(input);
				break;
			case "a":
				mainPlayer.setActions(input);
			}
		}
	}

	public void getKeyInput(List<String> input) {
		this.input = input;
	}

	public void addSkill(Skill s) {
		skillIDcounter++;
		activeSkills.add(s);
		if (online && serverWorld) {
			sendBuffer.add(new String("attack/" + skillIDcounter + ":" + s.getID() + ":" + skillIDcounter + "/"
					+ s.getTransform().pos.x + "/" + s.getTransform().pos.y + "/" + s.getInvokerID() + "/"
					+ s.getDirection().x + "/" + s.getDirection().y + "/"));
		}
	}

	public void addSkill(String id, float x, float y, String invokerID, float directionX, float directionY) {// server
		skillIDcounter++;
		Skill s = new Skill(id, x, y, this, invokerID, directionX, directionY, true);
		activeSkills.add(s);
		if (online && serverWorld) {
			sendBuffer.add(new String("attack/" + skillIDcounter + ":" + s.getID() + "/" + x + "/" + y + "/" + invokerID
					+ "/" + directionX + "/" + directionY + "/"));
		}
	}

	public void addPlayer(String id, InetAddress ip, float x, float y) {
		Players p = new Players(id, x, y, ip, true, this);
		players.add(p);
		if (online && serverWorld) {
			sendBuffer.add("spawn/player/" + id + "/" + ip + "/" + x + "/" + y + "/");
		}
	}

	public void addMainPlayer(String id, String address) {
		Random rand = new Random();
		boolean b = true;
		int i1 = 0, i2 = 0;
		while (b) {
			i1 = rand.nextInt(width - 1);
			i2 = -rand.nextInt(height - 1);
			if (i1 < 0) {
				i1 = 0;
			}
			if (i2 > 0) {
				i2 = 0;
			}
			if (!getTile(i1, -i2).isSolid()) {
				b = false;
			}
		}
		mainPlayer = new Mainplayer(this, id, i1 * 2, i2 * 2);
		players.add(mainPlayer);
		camera.getPosition().set(transform.pos.mul(-scale, new Vector3f()));
		if (online) {
			manageUDPOutput(new String("login/" + id + "/" + address + "/" + i1 * 2 + "/" + i2 * 2 + "/"), "server");
		}
	}

	public void addEnemy() {
		Random rn = new Random();
		int x = rn.nextInt(width - 1);// cause scaling
		int y = -rn.nextInt(height - 1);
		if (x < 0) {
			x = 0;
		}
		if (y > 0) {
			y = 0;
		}
		if (!getTile(x, -y).isSolid()) {
			String id = "enemy" + mopIDcounter;
			Enemy e = new Enemy(this, x * 2, y * 2, id, serverWorld);
			enemys.add(e);
			globalEnemyCounter++;
			mopIDcounter++;
			if (online && serverWorld) {
				sendBuffer.add("spawn/monster/" + id + "/" + x * 2 + "/" + y * 2 + "/");
			}
		}
	}

	public void addEnemy(String id, float x, float y) {
		Enemy e = new Enemy(this, x, y, id, serverWorld);
		enemys.add(e);
	}

	public static void initTex() {
		Enemy.initTex();
		Skill.initTex();
		Players.initTex();
		Mainplayer.initTex();
	}

	public void removePlayer(Players p) {
		players.remove(p);
	}

	public List<Skill> getSkillList() {
		return activeSkills;
	}

	public List<Players> getPlayersList() {
		return players;
	}

	public List<Enemy> getEnemyList() {
		return enemys;
	}

	public Mainplayer getMainplayer() {
		return mainPlayer;
	}

	public int getScale() {
		return scale;
	}

	public int getSkillIDcounter() {
		return skillIDcounter;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public float getMouseX() {
		return mouseX;
	}

	public float getMouseY() {
		return mouseY;
	}

	public static void setMouseX(float x) {
		mouseX = x;
	}

	public static void setMouseY(float y) {
		mouseY = y;
	}

	public float getViewX() {
		return viewX;
	}

	public float getViewY() {
		return viewY;
	}

	public Camera getCamera() {
		return camera;
	}

	public float getScreenY() {
		return screenY;
	}

	public float getScreenX() {
		return screenX;
	}

	public boolean getOnline() {
		return online;
	}

	public String getMapName() {
		return mapName;
	}

	public boolean getServerWorld() {
		return serverWorld;
	}

	public List<InetAddress> getAddressList() {
		return addresses;
	}

	public InetAddress getWorldIP() {
		return iaServer;
	}

	public void setWorldIP(InetAddress ip) {
		iaServer = ip;
	}

	public void addToBuffer(String command) {
		sendBuffer.add(command);
	}

	public void addAddress(InetAddress address) {
		addresses.add(address);
	}

	public void removeAddress(InetAddress address) {
		addresses.remove(address);
	}
}
