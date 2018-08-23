package basicGameObjects;

import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import assets.Assets;
import game.AudioManager;
import mechanics.Collision;
import render.Camera;
import render.Shader;
import render.Texture;
import render.Transform;
import world.World;

public class Skill extends BasisObject{
	// protected String playerID;
	protected static Texture tex;
	protected static String audio;
	// private int dmg = 10;
	protected static int manaCost = 10;
	protected float traveldistance = 25;
	protected float speed = 0.25f;
	protected boolean delete = false;
	protected Vector2f direction;
	protected boolean gotDirection = false;
	protected List<Skill> slist;
	protected List<Enemy> elist;
	protected List<Players> plist;
	protected int listPos;
	protected String invokerID;
	protected boolean gotHit = false;
	protected String hitID;
	protected String hitInvokerID;
	protected boolean created = false;
	protected boolean onlineMode;
	protected boolean serverSide;
	protected boolean moved = false;
	protected boolean eventAtDestination = false;
	protected Vector2f serverPos_interpolation = new Vector2f(0, 0);
	protected int power = 5;
	protected int walkCounter = 0;
	protected float walkCounterAddX, walkCounterAddY;

	public static void initTex() {
		tex = new Texture("skills/" + "test_skill_circle.png");
		audio = "soundEffects/" + "bounce.wav";
	}

	public Skill(float posx, float posy, World world, String invokerID, boolean serverSide) {
		this.id = invokerID + ":" + id + ":" + world.getSkillIDcounter();
		this.world = world;
		this.invokerID = invokerID;
		transform = new Transform();
		transform.scale.add(new Vector3f(-.7f, -0.7f, 0));
		transform.pos.add(new Vector3f(posx, posy, 0));
		this.onlineMode = world.getOnline();
		this.serverSide = serverSide;
	}

	public Skill(String id, float posx, float posy, World world, String invokerID, float dx, float dy,
			boolean serverSide) {
		this.id = id;
		this.world = world;
		this.invokerID = invokerID;
		transform = new Transform();
		transform.scale.add(new Vector3f(-.7f, -0.7f, 0));
		transform.pos.add(new Vector3f(posx, posy, 0));
		setDirection(dx, dy);
		created = true;
		this.onlineMode = world.getOnline();
		this.serverSide = serverSide;
	}

	public void setDirection(float dx, float dy) {
		direction = new Vector2f(dx, dy);
		gotDirection = true;
	}

	public void update() {
		if (onlineMode) {
			onlineMode();
		} else {
			offlineMode();
		}
	}

	private void onlineMode() {
		if (!gotHit) {
			move();
			if (world.getServerWorld()) {
				 checkCollision();
			}
			if(walkCounter > 0){
				transform.pos.add(new Vector3f(walkCounterAddX, walkCounterAddY, 0));
				walkCounter--;
	//			System.out.println(transform.pos.x + "  " +transform.pos.y);
			}
		}
	}

	private void offlineMode() {
		move();
		checkCollision();
	}

	protected void move() {
		if (gotDirection) {
			if (direction.x != 0 || direction.y != 0) {
				moved = true;
			} else {
				moved = false;
			}
			if (traveldistance > 0) {
				traveldistance = traveldistance - speed;
				transform.pos.add(new Vector3f(direction.x * speed, direction.y * speed, 0));
			} else {
				if (!eventAtDestination) {
					delete = true;
				}
			}
		}
	}

	public void hit(String invokerID, float x, float y, String type) {
		gotHit = true;
		eventByHit(x, y, type);
	}

	public void hit(Object o) {
		float x = 0;
		float y = 0;
		String type = "";
		if (o instanceof Skill) {
			Skill s = ((Skill) o);
			int pow = s.getPower();
			if (power > pow) {
				s.setDelete();
				power = power - pow;
			} else {
				if (power < pow) {
					delete = true;
					s.setPower(pow - power);
				} else {
					s.setDelete();
					delete = true;
				}
			}
			x = s.getTransform().pos.x;
			y = s.getTransform().pos.y;
			type = "skill";
		}
		if (o instanceof Players) {
			Players p = (Players) o;
			p.hit(id, invokerID);
			hitID = p.getID();
			x = p.getTransform().pos.x;
			y = p.getTransform().pos.y;
			type = "player";
		}
		if (o instanceof Enemy) {
			Enemy e = (Enemy) o;
			e.hit(id, invokerID);
			hitID = e.getID();
			x = e.getTransform().pos.x;
			y = e.getTransform().pos.y;
			type = "monster";
		}
		gotHit = true;
		eventByHit(x, y, type);
	}

	public void hit(Mainplayer p, float x, float y, String type) {// offline
		p.hit(id, invokerID);
		gotHit = true;
		eventByHit(x, y, type);
	}

	public void hitWall(float x, float y) {
		gotHit = true;
		eventByHit(x, y, "wall");
	}

	public void eventByHit(float x, float y, String type) {
		if (!world.getServerWorld()) {
			AudioManager.playSoundEffect(audio, transform.pos.x, transform.pos.y, 0);
			float ex = transform.pos.x - x;
			float ey = transform.pos.y - y;
			if (Math.abs(ex) > 0.5f || Math.abs(ey) > 0.5f) {
				System.out.println("tesSkill" + transform.pos.x + "  " + transform.pos.y);
				transform.pos.x = x;
				transform.pos.y = y;
			}else{
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
		} else {
			world.addToBuffer("hit/"+ type + "/" + id + "/" + invokerID + "/" + x + "/" + y + "/");
		}
		delete = true;
	}

	protected void checkCollision() {
		if(Collision.checkCollisionMap(this)){
			hitWall(transform.pos.x, transform.pos.y);
			}	
		Object o = Collision.checkCollisionMopsAndPlayers(this);
		if(o != null){
			hit(o);
		}	
		Skill s = Collision.checkCollisionSkills(this);
		if(s != null){
			hit(s);
		}	
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
	}

	public void setListPos(int pos) {
		listPos = pos;
	}

	public boolean getDelete() {
		return delete;
	}

	public static int getManaCost() {
		return manaCost;
	}

	public String getInvokerID() {
		return invokerID;
	}

	public void setDelete() {
		delete = true;
	}

	public Vector2f getDirection() {
		return direction;
	}

	public void setHit(boolean hit) {
		gotHit = hit;
	}

	public boolean getHit() {
		return gotHit;
	}

	public String getHitID() {
		return hitID;
	}

	public static String getAudio() {
		return audio;
	}

	public void setPos(float x, float y) {
		transform.pos.x = x;
		transform.pos.y = y;
	}

	public boolean getCreated() {
		return created;
	}

	public void setCreated() {
		created = false;
	}

	public boolean getMoved() {
		return moved;
	}

	public void setMoved() {
		moved = false;
	}

	public int getPower() {
		return power;
	}

	public void setPower(int power) {
		this.power = power;
	}
	
	public void setID(String s){
		id = s;
	}
}
