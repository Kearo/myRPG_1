package basicGameObjects;

import java.util.List;
import java.util.Random;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import assets.Assets;
import render.Camera;
import render.Shader;
import render.Texture;
import render.Transform;
import world.World;

public class Enemy {
	protected String name;
	protected int level;
	protected int hp, mana;
	protected int expGain;
	protected boolean aggressiv;
	protected float attackRange;
	protected float chaseRange = 5;
	protected boolean inCombat = false;
	protected String id;
	protected String hitID;
	protected String hitInvokerID;

	protected int worldX, worldY;
	protected World world;
	protected static Texture tex;
	protected static Texture deathtex;
	protected Transform transform;
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
	protected Vector2f move = new Vector2f(0, 0);
	protected Vector2f rememberMove = new Vector2f(0, 0);
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
	
	protected int walkCounter = 0;
	protected float walkCounterAddX, walkCounterAddY;

	public static void initTex() {
		tex = new Texture("monsters/" + "enemy_test.png");
		deathtex = new Texture("monsters/" + "enemy_death_test.png");
	}

	public Enemy(World world, float posX, float posY, String id, boolean serverSide) {
		transform = new Transform();
		this.world = world;
		this.worldX = world.getWidth();
		this.worldY = world.getHeight();
		this.id = id;
		onlineMode = world.getOnline();
		transform.scale.add(new Vector3f(-.5f, -0.5f, 0));
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

	}

	private void offlineMode() {
		if (!inCombat && !death) {
			moveOffline();
		} else {
			if (!death) {
				chase();
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
		float x = transform.pos.x + transform.scale.x;
		float y = transform.pos.y - transform.scale.y;
		float ix = x + 0;
		float iy = y + 0;

		if (dx > 0) { // left- 0 right+
			ix = (int) (transform.pos.x + transform.scale.x + dx * 1.1f + 1);
		}
		if (dx < 0) {
			ix = (int) (transform.pos.x - transform.scale.x - dx * 1.1f + 1);
		}

		if (dy > 0) { // top+ 0 down-
			iy = (int) (transform.pos.y + transform.scale.y + dy * 1.1f - 1);
		}
		if (dy < 0) {
			iy = (int) (transform.pos.y - transform.scale.y - dy * 1.1f - 1);
		}
		iy = Math.abs(iy);
		if (iy >= 128)
			iy = 127;
		if (iy < 0)
			iy = 0;
		if (ix < 0)
			ix = 0;
		if (ix >= 128)
			ix = 127;
		if (world.getTile((int) ix / 2, (int) iy / 2).isSolid()) {
			return true;
		}

		ix = x + 0;
		iy = y + 0;
		if (dx > 0) { // right
			ix = (int) Math.round(transform.pos.x + transform.scale.x * 2 + dx * 1.1f);
			iy = (int) Math.round(transform.pos.y - transform.scale.y * 2);
		}
		if (dx < 0) { // left
			ix = (int) Math.round(transform.pos.x + dx * 1.1f);
			iy = (int) Math.round(transform.pos.y - transform.scale.y * 2);
		}

		if (dy > 0) { // top
			ix = (int) Math.round(transform.pos.x + transform.scale.x * 2);
			iy = (int) Math.round(transform.pos.y + dy * 1.1f);
		}
		if (dy < 0) { // down
			ix = (int) Math.round(transform.pos.x + transform.scale.x * 2);
			iy = (int) Math.round(transform.pos.y - transform.scale.y * 2 + dy * 1.1f);
		}
		// ix = Math.abs(ix);
		iy = Math.abs(iy);
		if (iy >= 128)
			iy = 127;
		if (iy < 0)
			iy = 0;
		if (ix < 0)
			ix = 0;
		if (ix >= 128)
			ix = 127;
		if (world.getTile((int) ix / 2, (int) (iy) / 2).isSolid()) {
			return true;
		}

		elist = world.getEnemyList();
		plist = world.getPlayersList();
		for (int i = 0; i < elist.size(); i++) {
			Transform hitTransform;
			hitTransform = elist.get(i).getTransform();
			float xHit = hitTransform.pos.x + hitTransform.scale.x;
			float yHit = hitTransform.pos.y - hitTransform.scale.y;
			float radHitX = hitTransform.scale.x;
			float radHitY = hitTransform.scale.y;
			float xCol;
			float yCol;

			xCol = x - xHit;
			yCol = y - yHit;
			float xSide = xCol;
			float ySide = yCol;
			xCol = Math.abs(xCol);
			yCol = Math.abs(yCol);

			if (xSide < 0) {
				if (xCol <= radHitX + transform.scale.x + dx && yCol <= radHitY + transform.scale.y) {
					return true;
				}
			}
			if (xSide > 0) {
				if (xCol <= radHitX + transform.scale.x - dx && yCol <= radHitY + transform.scale.y) {
					return true;
				}
			}
			if (ySide < 0) {
				if (xCol <= radHitX + transform.scale.x && yCol <= radHitY + transform.scale.y + dy) {
					return true;
				}
			}
			if (ySide > 0) {
				if (xCol <= radHitX + transform.scale.x && yCol <= radHitY + transform.scale.y - dy) {
					return true;
				}
			}
		}

		for (int i = 0; i < plist.size(); i++) {
			Transform hitTransform;
			hitTransform = elist.get(i).getTransform();
			float xHit = hitTransform.pos.x + hitTransform.scale.x;
			float yHit = hitTransform.pos.y - hitTransform.scale.y;
			float radHitX = hitTransform.scale.x;
			float radHitY = hitTransform.scale.y;
			float xCol;
			float yCol;

			xCol = x - xHit;
			yCol = y - yHit;
			float xSide = xCol;
			float ySide = yCol;
			xCol = Math.abs(xCol);
			yCol = Math.abs(yCol);

			if (xSide < 0) {
				if (xCol <= radHitX + transform.scale.x + dx && yCol <= radHitY + transform.scale.y) {

					return true;
				}
			}
			if (xSide > 0) {
				if (xCol <= radHitX + transform.scale.x - dx && yCol <= radHitY + transform.scale.y) {
					return true;
				}
			}
			if (ySide < 0) {
				if (xCol <= radHitX + transform.scale.x && yCol <= radHitY + transform.scale.y + dy) {
					return true;
				}
			}
			if (ySide > 0) {
				if (xCol <= radHitX + transform.scale.x && yCol <= radHitY + transform.scale.y - dy) {
					return true;
				}
			}
		}

		return false;
	}

	protected void move() {
		if (moveTicker > 0) {
			if (move.x != 0 || move.y != 0) {
				moved = true;
			} else {
				moved = false;
			}
			if (!checkCollision(move.x, move.y, world)) {
				transform.pos.add(new Vector3f(move, 0));
				moveTicker--;
				if (moveTicker == 0) {
					stopTicker = stopTickertime;
				}
			} else {
				if (world.getServerWorld()) {
					move = new Vector2f(0, 0);
					world.manageUDPOutput(new String("move/monster/" + id + "/" + move.x + "/" + move.y + "/"),
							"clients");
				} else {
					moveTicker--;
					if (moveTicker == 0) {
						stopTicker = stopTickertime;
					}
				}
			}
		} else {
			move.set(0, 0);
			if (world.getServerWorld()) {
				world.manageUDPOutput(new String("move/monster/" + id + "/" + move.x + "/" + move.y + "/"), "clients");
			}
			if (stopTicker > 0)
				stopTicker--;
			else {
				initMovement();
			}
		}
	}

	protected void moveOffline() {
		if (moveTicker > 0) {
			if (move.x != 0 || move.y != 0) {
				moved = true;
			} else {
				moved = false;
			}
			if (!checkCollision(move.x, move.y, world)) {
				transform.pos.add(new Vector3f(move, 0));
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
			move.set(0, 0);
			if (stopTicker > 0)
				stopTicker--;
			else {
				initMovement();
			}
		}
	}

	protected void chase() {

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
	}

	public void hit(String hitID, String hitInvokerID) {
		inCombat = true;
		System.out.println(id + " " + hitID);
		// getDMG.usw....
		death();
	}

	private void checkForPlayers() {
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
				chase();
				inCombat = true;
				return;
			}
		}
		inCombat = false;
	}

	public void setDirection(float dx, float dy) {
		move.x = dx;
		move.y = dy;
	}

	public void initMovement() {
		if (!haveAggro) {
			Random rand = new Random();
			int direction = rand.nextInt(4);
			moveTicker = moveTime;
			if (direction == 0 && transform.pos.x < worldX * 2 - 2) {// right
				move.add(speed, 0);
			}
			if (direction == 1 && transform.pos.y > -worldY * 2 - 2) {// down
				move.add(0, -speed);
			}
			if (direction == 2 && transform.pos.x > 0) {
				move.add(-speed, 0);
			}
			if (direction == 3 && transform.pos.y < 0) {
				move.add(0, speed);
			}
			if (world.getOnline()) {
				world.manageUDPOutput(new String("move/monster/" + id + "/" + move.x + "/" + move.y + "/"), "clients");
			}
		}

	}

	public Transform getTransform() {
		return transform;
	}

	public Vector2f getDirection() {
		return move;
	}

	public boolean getDelete() {
		return delete;
	}

	public void setDelete() {
		delete = true;
	}

	public String getID() {
		return id;
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
	}

	public void render(Shader shader, Camera camera, World world) {
		if (tex != null && deathtex != null) {
			Matrix4f target = camera.getProjection();
			target.mul(world.getWorldMatrix());

			shader.bind();
			shader.setUniform("sampler", 0);
			shader.setUniform("projection", transform.getProjection(target));
			if (death) {
				deathtex.bind(0);
			} else {
				tex.bind(0);
			}
			Assets.getModel().render();
		}

	}
}
