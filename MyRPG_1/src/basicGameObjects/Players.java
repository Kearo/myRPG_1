package basicGameObjects;

import java.net.InetAddress;

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

public class Players {

	// private String name;
	// private int level;
	protected int hp;
	protected String id;
	protected int mana;
	protected int manaReg = 10;
	protected int maxMana = 1000000;
	protected long regCd = 0;
	protected float regTime = 5;
	protected boolean skillLock1 = false;
	protected long skillcd = 0;
	protected float cd = 2f;
	protected int worldX, worldY;
	protected float speed = 0.2f; // 0.2f
	protected Transform transform;
	private static Texture tex;
	protected Vector2f direction;
	protected boolean delete = false;
	protected boolean gotHit = false;
	protected String hitID;
	protected String hitInvokerID;
	protected InetAddress ip;
	protected boolean serverSide;
	protected boolean moved = false;
	protected Skill[] skillBar;
	protected World world;
	protected Vector2f serverPos_interpolation;
	protected boolean interpolation = false;
	protected int walkCounter = 0;
	protected float walkCounterAddX, walkCounterAddY;

	public static void initTex() {
		tex = new Texture("player/" + "sword1_diagonal.png");
	}

	public Players(String id, float posX, float posY, InetAddress ip, boolean serverSide, World world) {
		transform = new Transform();
		transform.pos.set(posX, posY, 0);
		skillBar = new Skill[5];
		skillBar[0] = new Skill(0, 0, world, id, serverSide);
		this.id = id;
		this.ip = ip;
		this.serverSide = serverSide;
		this.world = world;
		serverPos_interpolation = new Vector2f(0, 0);
		initPlayer();
	}

	public void initPlayer() {
		setHp(100);
		direction = new Vector2f(0, 0);
	}

	public void update() {
		if (serverSide) {
			if (!Collision.checkCollisionPlayers(this, world)) {
				move();
			} else {
				direction.x = 0;
				direction.y = 0;
				world.addToBuffer(new String("move/player/" + id + "/" + direction.x + "/" + direction.y + "/"
						+ transform.pos.x + "/" + transform.pos.y + "/"));
			}

		} else {
			move();
		}
		regenerations();
		checkSpellCD();
	}

	protected void regenerations() {
		long l = System.nanoTime();
		long l1 = (l - regCd);
		l1 = l1 / 1000000000;
		if (l1 > regTime) {
			if (mana < (maxMana - manaReg)) {
				mana += manaReg;
			} else {
				// mana = maxMana;
			}
			regCd = System.nanoTime();
		}
	}

	protected void checkSpellCD() {
		long l = System.nanoTime();
		long l1 = l - skillcd;
		l1 = l1 / 100000000;
		if (l1 > cd)
			skillLock1 = false;
	}

	public void hit(String hitID, String hitInvokerID) {
		gotHit = false;
		System.out.println("hit by skill: " + hitID + " from " + hitInvokerID);
	}

	public void death() {
		delete = true;
	}

	public void useSkill(World world, int skillBarNumber, float dx, float dy) {
		world.addSkill(skillBar[skillBarNumber].getID(), transform.pos.x, transform.pos.y, id, dx, dy);
	}

	public void useSkill(World world, Skill s) {// later form skillCreation
		// Skill s = new Skill(transform.pos.x, transform.pos.y, world,
		// skillBar[skillBarNumber].getID(), serverSide);
		// s.setDirection(dx, dy);
		// world.addSkill(s);
	}

	public void dmg(int dmg) {
		setHp(getHp() - dmg);
	}

	private void move() {
		if (direction.x != 0 || direction.y != 0) {
			moved = true;
		} else {
			moved = false;
		}
		if (world.getOnline() && !serverSide) {
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
					System.out.println(transform.pos.x + "  " +transform.pos.y);
				}
			}
		}
		if (moved) {
			transform.pos.add(new Vector3f(direction, 0));
		}
	}

	public void setDirection(float dx, float dy, World world) {
		direction.x = dx;
		direction.y = dy;
	}

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

	public void setInterpolatation(float x, float y) {
		serverPos_interpolation.set(x, y);
		interpolation = true;
	}

	public Transform getTransform() {
		return transform;
	}

	public Vector2f getDirection() {
		return direction;
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

	public void setDelete() {
		delete = true;
	}

	public boolean getDelete() {
		return delete;
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

	public void setHitInvokerID(String hitInvokerID) {
		this.hitInvokerID = hitInvokerID;
	}

	public String getHitInvokerID() {
		return hitInvokerID;
	}

	public String getHitID() {
		return hitID;
	}

	public void setIP(InetAddress ip) {
		this.ip = ip;
	}

	public InetAddress getIp() {
		return ip;
	}

	public boolean getMoved() {
		return moved;
	}

	public void setMoved() {
		moved = false;
	}

	public float getSpeed() {
		return speed;
	}
}
