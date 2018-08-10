package basicGameObjects;

import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import assets.Asset_circle;
import game.AudioManager;
import render.Camera;
import render.Shader;
import render.Texture;
import render.Transform;
import world.World;

public class Skill {
	// protected String playerID;
	protected static Texture tex;
	protected static String audio;
	// private int dmg = 10;
	protected static int manaCost = 10;
	protected float traveldistance = 25;
	protected float speed = 0.25f;
	protected boolean delete = false;
	protected Transform transform;
	protected Vector2f direction;
	protected boolean gotDirection = false;
	protected World world;
	protected List<Skill> slist;
	protected List<Enemy> elist;
	protected List<Players> plist;
	protected int listPos;
	protected String id = "skill";
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
		tex = new Texture("skills/" + "test_skill.png");
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
				// checkCollision();
			}
			if(walkCounter > 0){
				transform.pos.add(new Vector3f(walkCounterAddX, walkCounterAddY, 0));
				walkCounter--;
				System.out.println(transform.pos.x + "  " +transform.pos.y);
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

	public void hit(String hitID, String invokerID, float x, float y, String type) {
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

	public void hitWall(float x, float y, String type) {
		gotHit = true;
		eventByHit(x, y, type);
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
			world.addToBuffer(type + "/" + id + "/" + invokerID + "/" + x + "/" + y + "/");
		}
		delete = true;
	}

	protected void checkCollision() {
		boolean hit = false;

		float posX = transform.pos.x + transform.scale.x;
		float posY = transform.pos.y - transform.scale.y;
		float radX = transform.scale.x + 0f;
		float radY = transform.scale.y + 0f;

		float ix = posX + 0;
		float iy = posY + 0;

		float dx = direction.x;
		float dy = direction.y;

		ix = posX + 0;
		iy = posY + 0;

		if (dx > 0) { // right
			ix = (int) Math.round(transform.pos.x + transform.scale.x + dx + 1.5f);
			iy = (int) Math.round(transform.pos.y - transform.scale.y);
		}
		if (dx < 0) { // left
			ix = (int) Math.round(transform.pos.x + dx);
			iy = (int) Math.round(transform.pos.y - transform.scale.y);
		}

		if (dy > 0) { // top
			ix = (int) Math.round(transform.pos.x + transform.scale.x);
			iy = (int) Math.round(transform.pos.y + dy - 1.5f);
		}
		if (dy < 0) { // down
			ix = (int) Math.round(transform.pos.x + transform.scale.x);
			iy = (int) Math.round(transform.pos.y - transform.scale.y + dy + 0.5f);
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
		if (world.getTile((int) ix / 2, (int) (iy) / 2).isSolid()) {
			hitWall(ix, iy, "wall");
			hit = true;
		}

		if (!hit) {
			slist = world.getSkillList();
			if (slist.size() > 0) {
				for (int i = 0; i < slist.size(); i++) {
					if (slist.get(i).getID() != id) {
						Transform hitTransform;
						hitTransform = slist.get(i).getTransform();
						float px = hitTransform.pos.x + hitTransform.scale.x;
						float py = hitTransform.pos.y - hitTransform.scale.y;
						float rx = hitTransform.scale.x + 0;
						float ry = hitTransform.scale.y + 0;

						float pxx = posX - px;
						float pyy = posY - py;
						float radxx = radX + rx;
						float radyy = radY + ry;

						if (pxx < 0) {
							pxx = -pxx;
						}
						if (pyy < 0) {
							pyy = -pyy;
						}

						if (pxx <= radxx && pyy <= radyy) {
							hit = true;
						}
					}
					if (hit) {
						hitID = slist.get(i).getID();
						hit(slist.get(i));
						break;
					}
				}

			}

		}
		if (!hit) {
			plist = world.getPlayersList();
			if (plist.size() > 0) {
				for (int i = 0; i < plist.size(); i++) {
					Transform hitTransform;
					hitTransform = plist.get(i).getTransform();
					float px = hitTransform.pos.x + hitTransform.scale.x;
					float py = hitTransform.pos.y - hitTransform.scale.y;
					float rx = hitTransform.scale.x + 0f;
					float ry = hitTransform.scale.y + 0f;

					float pxx = posX - px;
					float pyy = posY - py;
					float radxx = radX + rx;
					float radyy = radY + ry;

					pxx = Math.abs(pxx);
					pyy = Math.abs(pyy);

					if (pxx <= radxx && pyy <= radyy) {
						hit = true;
					}

					px = hitTransform.pos.x;
					py = hitTransform.pos.y;
					if (Math.abs(posX - px) < radX * 1.3f && Math.abs(posY - py) < radY * 1.3f) {
						hit = true;
					}
					px += hitTransform.scale.x * 2;
					if (Math.abs(posX - px) < radX * 0.7f && Math.abs(posY - py) < radY * 0.7f) {
						hit = true;
					}
					py -= hitTransform.scale.y * 2;
					if (Math.abs(posX - px) < radX * 1.3f && Math.abs(posY - py) < radY * 1.3f) {
						hit = true;
					}
					px -= hitTransform.scale.x * 2;
					if (Math.abs(posX - px) < radX * 0.7f && Math.abs(posY - py) < radY * 0.7f) {
						hit = true;
					}

					if (hit) {
						hitID = plist.get(i).getID();
						hit(plist.get(i));
						break;
					}
				}
			}
		}

		if (!hit) {
			elist = world.getEnemyList();
			if (elist.size() > 0) {
				for (int i = 0; i < elist.size(); i++) {
					Transform hitTransform;
					hitTransform = elist.get(i).getTransform();
					float px = hitTransform.pos.x + hitTransform.scale.x;
					float py = hitTransform.pos.y - hitTransform.scale.y;
					float rx = hitTransform.scale.x + 0f;
					float ry = hitTransform.scale.y + 0f;

					float pxx = posX - px;
					float pyy = posY - py;
					float radxx = radX + rx;
					float radyy = radY + ry;

					pxx = Math.abs(pxx);
					pyy = Math.abs(pyy);

					if (pxx <= radxx && pyy <= radyy) {
						hit = true;
					}

					px = hitTransform.pos.x;
					py = hitTransform.pos.y;
					if (Math.abs(posX - px) < radX * 1.3f && Math.abs(posY - py) < radY * 1.3f) {
						hit = true;
					}
					px += hitTransform.scale.x * 2;
					if (Math.abs(posX - px) < radX * 0.7f && Math.abs(posY - py) < radY * 0.7f) {
						hit = true;
					}
					py -= hitTransform.scale.y * 2;
					if (Math.abs(posX - px) < radX * 1.3f && Math.abs(posY - py) < radY * 1.3f) {
						hit = true;
					}
					px -= hitTransform.scale.x * 2;
					if (Math.abs(posX - px) < radX * 0.7f && Math.abs(posY - py) < radY * 0.7f) {
						hit = true;
					}

					if (hit) {
						if (!elist.get(i).getDeath()) {
							hitID = elist.get(i).getID();
							hit(elist.get(i));
							break;
						} else {
							hit = false;
						}

					}
				}
			}
		}

		if (!hit) {
			if (!invokerID.equals(world.getMainplayer().getID())) {
				Transform hitTransform;
				hitTransform = world.getMainplayer().getTransfrom();
				float px = hitTransform.pos.x + hitTransform.scale.x;
				float py = hitTransform.pos.y - hitTransform.scale.y;
				float rx = hitTransform.scale.x + 0;
				float ry = hitTransform.scale.y + 0;

				float pxx = posX - px;
				float pyy = posY - py;
				float radxx = radX + rx;
				float radyy = radY + ry;

				pxx = Math.abs(pxx);
				pyy = Math.abs(pyy);

				if (pxx <= radxx && pyy <= radyy) {
					hit = true;
				}

				px -= hitTransform.scale.x;
				py += hitTransform.scale.y;
				if (Math.abs(posX - px) < radX * 1.3f && Math.abs(posY - py) < radY * 1.3f) {
					hit = true;
				}
				px += hitTransform.scale.x * 2;
				if (Math.abs(posX - px) < radX * 0.7f && Math.abs(posY - py) < radY * 0.7f) {
					hit = true;
				}
				py -= hitTransform.scale.y * 2;
				if (Math.abs(posX - px) < radX * 1.3f && Math.abs(posY - py) < radY * 1.3f) {
					hit = true;
				}
				px -= hitTransform.scale.x * 2;
				if (Math.abs(posX - px) < radX * 0.7f && Math.abs(posY - py) < radY * 0.7f) {
					hit = true;
				}

				if (hit) {
					hit(world.getMainplayer());
				}
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

			tex.bind(0);
			Asset_circle.getModel().render();
		}
	}

	public void setInterpolatation(float x, float y) {
		serverPos_interpolation.set(x, y);
	}

	public void setListPos(int pos) {
		listPos = pos;
	}

	public Transform getTransform() {
		return transform;
	}

	public boolean getDelete() {
		return delete;
	}

	public static int getManaCost() {
		return manaCost;
	}

	public String getID() {
		return id;
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
}
