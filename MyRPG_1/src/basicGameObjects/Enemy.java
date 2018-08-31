package basicGameObjects;

import java.io.File;
import java.util.List;
import java.util.Random;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import assets.Assets;
import mechanics.Collision;
import render.Camera;
import render.Shader;
import render.Texture;
import render.Transform;
import world.World;

public class Enemy extends BasisObject{
	protected String name;
	protected int level;
	protected int hp, mana;
	protected int expGain;
	protected boolean aggressiv;
	protected float attackRange;
	protected float chaseRange = 5;
	protected boolean inCombat = false;
	protected String hitID;
	protected String hitInvokerID;

	protected int worldX, worldY;
	protected static Texture[] tex = null;
	protected float speed = 0.2f;
	protected boolean delete = false;

	protected List<Enemy> elist;
	protected List<Players> plist;
	protected Item[] lootList;

	protected int moveTicker;
	protected int moveTime = 10;
	protected int stopTickertime = 2000;
	protected int stopTicker = stopTickertime;
	protected boolean haveAggro = false;
	protected Vector2f direction = new Vector2f(0, 0);
	protected boolean gotHit = false;
	protected boolean death = false;
	protected long deathTimer;
	protected int secondsAfterDeathToRemove = 20;
	protected boolean lootGenerated = false;
	protected boolean sendInfo = false;
	protected boolean onlineMode;
	protected boolean serverSide;
	protected boolean moved = false;
	protected Vector2f serverPos_interpolation = new Vector2f(0, 0);

	protected boolean interpolation = false;
	protected int walkCounter = 0;
	protected float walkCounterAddX, walkCounterAddY;
	protected Players chaseTarget;
	
	protected static String dirPath = "./textures/monsters";
	protected int texPos = 1;

	public static void initTex() {
		File dir = new File(dirPath);	
		File[] fileList = dir.listFiles();
		int length = fileList.length;
		for(File f : fileList){
			if(f.isDirectory()){
				length--;
			}
		}
		tex = new Texture[length];
		int counter = 0;
		for(int i = 0; i < fileList.length; i++){
			if(fileList[i].isDirectory()){
				counter++;
				continue;
			}
			tex[i-counter] = new Texture("monsters/" + fileList[i].getName());
		}
	}

	public Enemy(World world, float posX, float posY, String id, boolean serverSide) {
		transform = new Transform();
		this.world = world;
		this.worldX = world.getWidth();
		this.worldY = world.getHeight();
		this.id = id;
		onlineMode = world.getOnline();
	//	transform.scale.add(new Vector3f(-.5f, -0.5f, 0));
		transform.pos.add(new Vector3f(posX, posY, 0));
		lootList = new Item[10];
		this.serverSide = serverSide;
	}

	public void update() {
		if (onlineMode) {
			onlineMode();
		} else {
			offlineMode();
		}
	}

	private void onlineMode() {
		if (!inCombat && !death) {
			move();
		} else {
			if (!death) {
				if (inCombat) {
					chase(chaseTarget);
					attack();
				} else {
					if (aggressiv) {
						chaseTarget = checkForPlayers();
						if (chaseTarget != null) {
							inCombat = true;
						}
					}
				}
			} else {
				if (lootGenerated) {
					long l = System.nanoTime();
					l = (l - deathTimer) / 1000000000;
					if (l >= secondsAfterDeathToRemove) {
						delete = true;
					}
				} else {
					generateLoot();
				}
			}
		}
	}

	private void offlineMode() {
		if (!inCombat && !death) {
			moveOffline();
		} else {
			if (!death) {
				chase(chaseTarget);
				attack();
			} else {
				if (lootGenerated) {
					long l = System.nanoTime();
					l = (l - deathTimer) / 1000000000;
					if (l >= secondsAfterDeathToRemove) {
						delete = true;
					}
				} else {
					generateLoot();
				}
			}
		}
	}

	protected boolean checkCollision(float dx, float dy, World world) {
		if(Collision.checkCollisionMap(this)){
			return true;
		}	
		Object o = Collision.checkCollisionMopsAndPlayers(this);
		if(o != null){
			return true;
		}	
		return false;
	}

	protected void move() {
		if (world.getServerWorld()) {
			if (moveTicker > 0) {
				if (direction.x != 0 || direction.y != 0) {
					moved = true;
				} else {
					moved = false;
				}
				if (!checkCollision(direction.x, direction.y, world)) {
					transform.pos.add(new Vector3f(direction, 0));

				} else {
					moveTicker = 0;
					world.addToBuffer("move/monster/" + id + "/" + direction.x + "/" + direction.y + "/" + transform.pos.x + "/"
							+ transform.pos.y + "/");
				}
				if (moveTicker == 0) {
					stopTicker = stopTickertime;
				}
				moveTicker--;
			} else {
				if (direction.distance(0, 0) > 0) {
					direction.set(0, 0);
					world.addToBuffer("move/monster/" + id + "/" + direction.x + "/" + direction.y + "/" + transform.pos.x + "/"
							+ transform.pos.y + "/");
				}
				if (stopTicker > 0) {
					stopTicker--;
				} else {
					initMovement();
				}
			}
		} else {
			if (interpolation) {
				float x = serverPos_interpolation.x - transform.pos.x;
				float y = serverPos_interpolation.y - transform.pos.y;
				if (Math.abs(x) > 0.5f || Math.abs(y) > 0.5f) {
					System.out.println("tes" + transform.pos.x + "  " + transform.pos.y);
					transform.pos.x = serverPos_interpolation.x;
					transform.pos.y = serverPos_interpolation.y;
				} else {
					if (x != 0 || y != 0) {
						if (walkCounter == 0) {
							walkCounterAddX = x / 10;
							walkCounterAddY = y / 10;
						} else {
							walkCounterAddX = (walkCounterAddX * walkCounter + x) / 10;
							walkCounterAddY = (walkCounterAddY * walkCounter + y) / 10;
						}
						walkCounter = 10;
					}
				}
				interpolation = false;
			} else {
				if (walkCounter > 0) {
					transform.pos.add(new Vector3f(walkCounterAddX, walkCounterAddY, 0));
					walkCounter--;
					System.out.println("RRR" + transform.pos.x + "  " + transform.pos.y);
				}
			}
			transform.pos.add(new Vector3f(direction, 0));
		}
	}

	protected void moveOffline() {
		if (moveTicker > 0) {
			if (direction.x != 0 || direction.y != 0) {
				moved = true;
			} else {
				moved = false;
			}
			if (!checkCollision(direction.x, direction.y, world)) {
				transform.pos.add(new Vector3f(direction, 0));
				moveTicker--;
				if (moveTicker == 0) {
					stopTicker = stopTickertime;
				}
			} else {
				moveTicker--;
				if (moveTicker == 0) {
					stopTicker = stopTickertime;
				}
			}
		} else {
			direction.set(0, 0);
			if (stopTicker > 0)
				stopTicker--;
			else {
				initMovement();
			}
		}
	}

	protected void chase(Players target) {

	}

	protected void attack() {

	}

	protected void generateLoot() {
		// generateItems_add_to_lootList
		lootGenerated = true;
	}

	protected void death() {
		death = true;
		deathTimer = System.nanoTime();
		texPos = 0;
	}

	public void hit(String hitID, String hitInvokerID) {
		inCombat = true;
		System.out.println(id + " enemy " + hitID);
		// getDMG.usw....
		death();
	}

	private Players checkForPlayers() {
		plist = world.getPlayersList();
		float posX = transform.pos.x + transform.scale.x;
		float posY = transform.pos.y - transform.scale.y;
		for (int i = 0; i < plist.size(); i++) {
			Transform hitTransform;
			hitTransform = plist.get(i).getTransform();
			float px = hitTransform.pos.x + hitTransform.scale.x;
			float py = hitTransform.pos.y - hitTransform.scale.y;
			px = posX - px;
			py = posY - py;
			if (px >= chaseRange && py <= chaseRange) {
				inCombat = true;
				return plist.get(i);
			}
		}
		inCombat = false;
		return null;
	}

	public void setDirection(float dx, float dy) {
		direction.x = dx;
		direction.y = dy;
	}

	public void initMovement() {
		if (!haveAggro) {
			Random rand = new Random();
			int directionPointer = rand.nextInt(4);
			moveTicker = moveTime;
			if (directionPointer == 0 && transform.pos.x < worldX * 2 - 2) {// right
				direction.add(speed, 0);
			}
			if (directionPointer == 1 && transform.pos.y > -worldY * 2 - 2) {// down
				direction.add(0, -speed);
			}
			if (directionPointer == 2 && transform.pos.x > 0) {
				direction.add(-speed, 0);
			}
			if (directionPointer == 3 && transform.pos.y < 0) {
				direction.add(0, speed);
			}
			if (world.getOnline()) {
				world.addToBuffer("move/monster/" + id + "/" + direction.x + "/" + direction.y + "/" + transform.pos.x + "/"
						+ transform.pos.y + "/");
				// world.manageUDPOutput(new String("move/monster/" + id + "/" +
				// move.x + "/" + move.y + "/"), "clients");
			}
		}

	}
	
	public void render(Shader shader, Camera camera, World world) {
		if (tex != null) {
			Matrix4f target = camera.getProjection();
			target.mul(world.getWorldMatrix());

			shader.bind();
			shader.setUniform("sampler", 0);
			shader.setUniform("projection", transform.getProjection(target));
			
			tex[texPos].bind(0);
			Assets.getModel().render();
		}

	}

	public Vector2f getDirection() {
		return direction;
	}

	public boolean getDelete() {
		return delete;
	}

	public void setDelete() {
		delete = true;
	}
	
	public boolean getHit() {
		return gotHit;
	}

	public void setHit(boolean hit) {
		gotHit = hit;
	}

	public void setHitID(String hitID) {
		this.hitID = hitID;
	}

	public String getHitID() {
		return hitID;
	}

	public String getHitInvokerID() {
		return hitInvokerID;
	}

	public Item[] getLoot() {
		return lootList;
	}

	public void setLoot(Item[] loot) {
		lootList = loot;
	}

	public boolean getDeath() {
		return death;
	}

	public boolean getsendInfo() {
		return sendInfo;
	}

	public void setsendInfo() {
		sendInfo = false;
	}

	public boolean getMoved() {
		return moved;
	}

	public void setMoved() {
		moved = false;
	}

	public void setInterpolatation(float x, float y) {
		serverPos_interpolation.set(x, y);
		interpolation = true;
	}

}
