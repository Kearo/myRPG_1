package basicGameObjects;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import assets.Assets;
import game.AudioManager;
import game.GameLogicManager;
import mechanics.ErrorLog;
import render.Camera;
import render.Shader;
import render.Texture;
import render.Transform;
import world.World;

public class Mainplayer extends Players {
	private List<String> actionsToDo;
	private boolean skill1 = false;//
	private Inventory inventory;
	private Equipment equipment;
	private boolean moveCheck = false;
	private Vector2f moveReminder;
	private boolean onlineMode;
	private static Texture tex;
	
	private List<Enemy> elist;
	private List<Players> plist;

	private static InetAddress ip;

	public static void initTex() {
		tex = new Texture("player/" + "sword1_diagonal2.png");
		// tex = new Texture("player/" + "test.png");
		try {
			ip = InetAddress.getByName("localhost");
		} catch (UnknownHostException e) {
			ErrorLog.writeError("mPlayer", e);
			e.printStackTrace();
		}
	}

	public Mainplayer(World world, String id, float posX, float posY) {
		super(id, posX, posY, ip, false, world);
		transform = new Transform();
		this.id = id;

		transform.pos.set(new Vector3f(posX, posY, 0));
		transform.scale.sub(0.5f, 0.5f, 0);
		actionsToDo = new ArrayList<String>();
		this.worldX = world.getWidth();
		this.worldY = world.getHeight();
		initPlayer();
		moveReminder = new Vector2f(0, 0);
		onlineMode = GameLogicManager.getMode();
	}

	public void initPlayer() {
		setHp(100);
		setMana(100000000);
		inventory = new Inventory();
		equipment = new Equipment();
		direction = new Vector2f(0, 0);
	}

	@Override
	public void render(Shader shader, Camera camera, World world) {
		if (tex != null) {
			Matrix4f target = camera.getProjection();
			target.mul(world.getWorldMatrix());

			shader.bind();
			shader.setUniform("sampler", 0);
			shader.setUniform("projection", transform.getProjection(target));
			tex.bind(0);
			Assets.getModel().render();
		}

	}

	public void update(Camera camera, World world) {
		if (!onlineMode) {
			offlineMode(camera, world);
		} else {
			onlineMode(camera, world);
		}
	}

	private void onlineMode(Camera camera, World world) {
		Vector2f movement = new Vector2f();
		for (int i = 0; i < actionsToDo.size(); i++) {
			switch (actionsToDo.get(i)) {
			case "playerMoveLeft":
				if (transform.pos.x > -1 + transform.scale.x + speed * 1.1f) {
					movement.add(-speed, 0);
					moveCheck = true;
				}
				break;
			case "playerMoveRight":
				if (transform.pos.x < worldX * 2 - speed * 1.1f - (transform.scale.x * 3)) {
					movement.add(speed, 0);
					moveCheck = true;
				}
				break;
			case "playerMoveUp":
				if (transform.pos.y < 1 - transform.scale.y - speed * 1.1f) {
					movement.add(0, speed);
					moveCheck = true;
				}
				break;
			case "playerMoveDown":
				if (transform.pos.y > -worldY * 2 + speed * 1.1f + (transform.scale.y * 3)) {
					movement.add(0, -speed);
					moveCheck = true;
				}
				break;
			case "attackMove":
				useSkill(world);
				break;
			case "pickUpItem":
				checkForItem();
				break;

			}
		}
		if (!(moveReminder.x == movement.x && moveReminder.y == movement.y)) {
			if (movement.x != 0 || movement.y != 0) {
				world.manageUDPOutput(new String("move/" + id + "/" + movement.x + "/" + movement.y + "/"),
						"server");
			} else {
				if (moveCheck) {
					moveCheck = false;
					world.manageUDPOutput(
							new String("move/" + id + "/" + movement.x + "/" + movement.y + "/"), "server");
				}
			}
		}
		if (moveReminder.x != movement.x) {
			moveReminder.x = movement.x;
		}
		if (moveReminder.y != movement.y) {
			moveReminder.y = movement.y;
		}
		move(direction);
		actionsToDo.clear();
		regenerations();
		checkSpellCD();
		camera.getPosition().lerp(transform.pos.mul(-world.getScale(), new Vector3f()), 1.0f); // last
		// number
		// controlls
	}

	private void offlineMode(Camera camera, World world) {
		Vector2f movement = new Vector2f();
		for (int i = 0; i < actionsToDo.size(); i++) {
			switch (actionsToDo.get(i)) {
			case "playerMoveLeft":
				if (transform.pos.x > -1 + transform.scale.x + speed * 1.1f) {
					if (!checkCollision(-speed, 0, world)) {
						movement.add(-speed, 0);
						moveCheck = true;
					}
				}
				break;
			case "playerMoveRight":
				if (transform.pos.x < worldX * 2 - speed * 1.1f - (transform.scale.x * 3)) {
					if (!checkCollision(speed, 0, world)) {
						movement.add(speed, 0);
						moveCheck = true;
					}
				}
				break;
			case "playerMoveUp":
				if (transform.pos.y < 1 - transform.scale.y - speed * 1.1f) {
					if (!checkCollision(0, speed, world)) {
						movement.add(0, speed);
						moveCheck = true;
					}
				}
				break;
			case "playerMoveDown":
				if (transform.pos.y > -worldY * 2 + speed * 1.1f + (transform.scale.y * 3)) {
					if (!checkCollision(0, -speed, world)) {
						movement.add(0, -speed);
						moveCheck = true;
					}
				}
				break;
			case "attackMove":
				if (!skillLock1) {
					useSkill(world);
				}
				break;
			case "pickUpItem":
				checkForItem();
				break;

			}
		}
		move(movement);
		actionsToDo.clear();
		checkSpellCD();
		regenerations();
		camera.getPosition().lerp(transform.pos.mul(-world.getScale(), new Vector3f()), 1.0f); // last
																								// number
																								// controlls
	}

	// @Override //todoLater
	// public void hit(String hitID){
	//
	// }

	private void useSkill(World world) { // todo in relation to screensize
		if (onlineMode) {
			float a = (world.getScreenX() - world.getScale() * 2) / 2;
			float b = (world.getScreenY() - world.getScale() * 2) / 2;// need
																		// screenY
																		// weil
																		// viewY
																		// zu
																		// verzeht
																		// durch
																		// umwandlungen

			int xCam = (int) (world.getWidth() * world.getScale() * 2 + a);
			int yCam = (int) (world.getHeight() * world.getScale() * 2 + b);

			float xWorld = world.getHeight() * 2;
			float yWorld = world.getHeight() * 2;

			skillLock1 = true;
			Skill s = new Skill(transform.pos.x, transform.pos.y, world, id, false);
			// int listPos = world.addSkill(transform.pos.x,
			// transform.pos.y, id);
			// pos of mouse relative to the view;
			float x = world.getMouseX() / (world.getWidth() / 2) - s.getTransform().scale.x;
			float y = -(world.getMouseY() / (world.getHeight() / 2)) + s.getTransform().scale.y;
			// translate mouseposition to destinacionpoint in world
			float x2 = xWorld - ((xCam + world.getCamera().getPosition().x) / (world.getWidth() / 2))
					- s.getTransform().scale.x;
			float y2 = -yWorld + ((yCam - world.getCamera().getPosition().y) / (world.getHeight() / 2))
					+ s.getTransform().scale.y;

			float x3 = (x2 + x);
			float y3 = (y2 + y);
			// cal direction
			x = x3 - (transform.pos.x + transform.scale.x);
			y = y3 - (transform.pos.y - transform.scale.y);
			// normalize
			float lenght = (float) Math.sqrt(x * x + y * y);
			x = x / lenght;
			y = y / lenght;
			// System.out.println(world.getCamera().getPosition().x);
			skillcd = System.nanoTime();
			world.manageUDPOutput(new String("attack/" + id + "/" + "0" + "/" + x + "/" + y + "/"), "server");
		} else {
			if (mana > Skill.manaCost) {
				mana -= Skill.manaCost;
				if (!skillLock1) {
					// 4704 = 4096 + 608
					// 4424
					// xcam = width * scale*2 + camposX*-1
					// ycam = hight * - camposY
					float a = (world.getScreenX() - world.getScale() * 2) / 2;
					float b = (world.getScreenY() - world.getScale() * 2) / 2;// need
																				// screenY
																				// weil
																				// viewY
																				// zu
																				// verzeht
																				// durch
																				// umwandlungen

					int xCam = (int) (world.getWidth() * world.getScale() * 2 + a);
					int yCam = (int) (world.getHeight() * world.getScale() * 2 + b);

					float xWorld = world.getHeight() * 2;
					float yWorld = world.getHeight() * 2;

					skillLock1 = true;
					Skill s = new Skill(transform.pos.x, transform.pos.y, world, id, false);
					// int listPos = world.addSkill(transform.pos.x,
					// transform.pos.y, id);
					// pos of mouse relative to the view;
					float x = world.getMouseX() / (world.getWidth() / 2) - s.getTransform().scale.x;
					float y = -(world.getMouseY() / (world.getHeight() / 2)) + s.getTransform().scale.y;
					// translate mouseposition to destinacionpoint in world
					float x2 = xWorld - ((xCam + world.getCamera().getPosition().x) / (world.getWidth() / 2))
							- s.getTransform().scale.x;
					float y2 = -yWorld + ((yCam - world.getCamera().getPosition().y) / (world.getHeight() / 2))
							+ s.getTransform().scale.y;

					float x3 = (x2 + x);
					float y3 = (y2 + y);
					// cal direction
					x = x3 - (transform.pos.x + transform.scale.x);
					y = y3 - (transform.pos.y - transform.scale.y);
					// normalize
					float lenght = (float) Math.sqrt(x * x + y * y);
					x = x / lenght;
					y = y / lenght;
					// System.out.println(world.getCamera().getPosition().x);
					s.setDirection(x, y);
					world.addSkill(s);
					skillcd = System.nanoTime();
				}
			}
		}
	}

	private boolean checkCollision(float dx, float dy, World world) { // Worldtile
																		// and
																		// Enemy
		float x = transform.pos.x + transform.scale.x;
		float y = transform.pos.y - transform.scale.y;
		float ix = x + 0;
		float iy = y + 0;

		if (dx > 0) { // left- 0 right+
			ix = (int) (transform.pos.x + transform.scale.x + dx + 1);
		}
		if (dx < 0) {
			ix = (int) (transform.pos.x - transform.scale.x - dx + 1);
		}

		if (dy > 0) { // top+ 0 down-
			iy = (int) (transform.pos.y + transform.scale.y + dy - 1);
		}
		if (dy < 0) {
			iy = (int) (transform.pos.y - transform.scale.y - dy - 1);
		}
		iy = Math.abs(iy);

		if (world.getTile((int) ix / 2, (int) iy / 2).isSolid()) {
			return true;
		}
		// corners
		ix = x + 0;
		iy = y + 0;
		if (dx > 0) { // right
			ix = (int) Math.round(transform.pos.x + transform.scale.x * 2 + dx);
			iy = (int) Math.round(transform.pos.y - transform.scale.y * 2);
		}
		if (dx < 0) { // left
			ix = (int) Math.round(transform.pos.x + dx);
			iy = (int) Math.round(transform.pos.y - transform.scale.y * 2);
		}

		if (dy > 0) { // top
			ix = (int) Math.round(transform.pos.x + transform.scale.x * 2);
			iy = (int) Math.round(transform.pos.y + dy);
		}
		if (dy < 0) { // down
			ix = (int) Math.round(transform.pos.x + transform.scale.x * 2);
			iy = (int) Math.round(transform.pos.y - transform.scale.y * 2 + dy);
		}
		// ix = Math.abs(ix);
		iy = Math.abs(iy);

		if (world.getTile((int) ix / 2, (int) iy / 2).isSolid()) {
			return true;
		}

		elist = world.getEnemyList();
		plist = world.getPlayersList();
		if (elist.size() > 0) {
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
		}

		if (plist.size() > 0) {
			// System.out.println(plist.size());
			for (int i = 0; i < plist.size(); i++) {
				Transform hitTransform;
				hitTransform = plist.get(i).getTransform();
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
		}
		return false;
	}

	public void dmg(int dmg) {
		setHp(getHp() - dmg);
	}

	private void move(Vector2f direction) {
		transform.pos.add(new Vector3f(direction, 0));
		AudioManager.setListenerPos(transform.pos.x, transform.pos.y, 0);
	}

	public void setActions(String input) {
		actionsToDo.add(input);
	}

	private void pickUpItem(Item item) {
		inventory.addItem(item);
	}

	public void equipItem(Item item) {
		equipment.equipItem(item);
		;
	}

	private void checkForItem() {
		Item f = new Item();
		pickUpItem(f);
	}

	public Transform getTransfrom() {
		return transform;
	}

	public boolean getSkillactive() {
		return skill1;
	}

	public void setSkillactive() {
		skill1 = false;
	}

	public String getID() {
		return id;
	}

	public int getHp() {
		return hp;
	}

	public void setHp(int hp) {
		this.hp = hp;
	}

	public void setMana(int mana) {
		this.mana = mana;
	}
}
