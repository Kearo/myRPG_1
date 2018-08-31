package mechanics;

import java.util.List;

import basicGameObjects.BasisObject;
import basicGameObjects.Enemy;
import basicGameObjects.Players;
import basicGameObjects.Skill;
import render.Transform;

public class Collision {

	public static boolean checkCollisionMap(BasisObject o) {
		float scaleMultiplier = (float) (2 / (Math.sqrt(o.getTransform().scale.x * o.getTransform().scale.x + 
									o.getTransform().scale.y * o.getTransform().scale.x)));
	//	System.out.println(scaleMultiplier + " " + o.getTransform().scale.x);
		float x1 = o.getTransform().pos.x + o.getTransform().scale.x / scaleMultiplier; // corner1
		float x2 = o.getTransform().pos.x + o.getTransform().scale.x * scaleMultiplier;
		float y1 = o.getTransform().pos.y - o.getTransform().scale.y / scaleMultiplier;
		float y2 = o.getTransform().pos.y - o.getTransform().scale.y * scaleMultiplier;

		float ix = 0;
		float iy = 0;

		float dx = o.getDirection().x;
		float dy = o.getDirection().y;
		if (dx > 0) { // right
			ix = x2 + dx;
			iy = y1;
			iy = Math.abs(iy);
			if (o.getWorld().getTile((int) ix / 2, (int) iy / 2).isSolid()) {
				o.setInBetween(((int) ix/2) - x2, ((int) iy/2) + y1);
				return true;
			} else {
				ix = x2 + dx;
				iy = y2;
				iy = Math.abs(iy);
				if (o.getWorld().getTile((int) ix / 2, (int) iy / 2).isSolid()) {
					o.setInBetween(((int) ix/2) - x2, ((int) iy/2) + y2);
					return true;
				}
			}
		}
		if (dx < 0) { // left
			ix = x1 + dx;
			iy = y1;
			iy = Math.abs(iy);
			if (o.getWorld().getTile((int) ix / 2, (int) iy / 2).isSolid()) {
				o.setInBetween(((int)ix/2) - x1, ((int)iy/2) + y1);
				return true;
			} else {
				ix = x1 + dx;
				iy = y2;
				iy = Math.abs(iy);
				if (o.getWorld().getTile((int) ix / 2, (int) iy / 2).isSolid()) {
					o.setInBetween(((int)ix/2) - x1,  ((int) iy/2) + y2);
					return true;
				}
			}
		}

		if (dy > 0) { // top
			ix = x1;
			iy = y1 + dy;
			iy = Math.abs(iy);
			if (o.getWorld().getTile((int) ix / 2, (int) iy / 2).isSolid()) {
				o.setInBetween(((int) ix/2) - x1, ((int) iy/2) + y1);
				return true;
			} else {
				ix = x2;
				iy = y1 + dy;
				iy = Math.abs(iy);
				if (o.getWorld().getTile((int) ix / 2, (int) iy / 2).isSolid()) {
					o.setInBetween(((int) ix/2) - x2, ((int) iy/2) + y1);
					return true;
				}
			}
		}
		if (dy < 0) { // down
			ix = x1;
			iy = y2 + dy;
			iy = Math.abs(iy);
			if (o.getWorld().getTile((int) ix / 2, (int) iy / 2).isSolid()) {
				o.setInBetween(((int) ix/2) - x1, ((int) iy/2) + y2);
				return true;
			} else {
				ix = x2;
				iy = y2 + dy;
				iy = Math.abs(iy);
				if (o.getWorld().getTile((int) ix / 2, (int) iy / 2).isSolid()) {
					o.setInBetween(((int) ix/2) - x2, ((int) iy/2) + y2);
					return true;
				}
			}
		}
		return false;
	}

	public static Skill checkCollisionSkills(BasisObject o) {
		List<Skill> slist;
		slist = o.getWorld().getSkillList();
		float posX = o.getTransform().pos.x + o.getTransform().scale.x;
		float posY = o.getTransform().pos.y - o.getTransform().scale.y;
		float radX = o.getTransform().scale.x + 0f;
		float radY = o.getTransform().scale.y + 0f;
		for (Skill s : slist) {
			if (s.getID() != o.getID()) {
				Transform hitTransform;
				hitTransform = s.getTransform();
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
					return s;
				}
			}

		}
		return null;
	}

	public static BasisObject checkCollisionMopsAndPlayers(BasisObject o) {
		List<Enemy> elist;
		List<Players> plist;
		elist = o.getWorld().getEnemyList();
		plist = o.getWorld().getPlayersList();

		float dx = o.getDirection().x;
		float dy = o.getDirection().y;
		float aX = 0, bX = 0, aY = 0, bY = 0;
		if (dx > 0) {
			bX = dx;
		}
		if (dx < 0) {
			aX = dx;
		}
		if (dy > 0) {
			aY = dy;
		}
		if (dy < 0) {
			bY = dy;
		}
		
		float scaleMultiplier = (float) (2 / (Math.sqrt((o.getTransform().scale.x * o.getTransform().scale.x) + 
				(o.getTransform().scale.y * o.getTransform().scale.x))));

		float x1 = o.getTransform().pos.x + aX; // corner1
		float x2 = o.getTransform().pos.x + o.getTransform().scale.x * scaleMultiplier + bX;
		float y1 = o.getTransform().pos.y + aY;
		float y2 = o.getTransform().pos.y - o.getTransform().scale.y * scaleMultiplier + bY;

		float epMinX_oMaxX, epMinY_oMaxY, oMinX_epMaxX, oMinY_epMaxY;

		for (Enemy e : elist) {
			if (e.getID() != o.getID()) {
				scaleMultiplier = (float) (2 / (Math.sqrt((e.getTransform().scale.x * e.getTransform().scale.x) + 
						(e.getTransform().scale.y * e.getTransform().scale.x))));
				epMinX_oMaxX = e.getTransform().pos.x - x2;
				epMinY_oMaxY = (e.getTransform().pos.y - e.getTransform().scale.y * 2) - y1;
				oMinX_epMaxX = x1 - (e.getTransform().pos.x + e.getTransform().scale.x * 2);
				oMinY_epMaxY = y2 - e.getTransform().pos.y;

				if (epMinX_oMaxX > 0 || epMinY_oMaxY > 0) {
					continue;
				}
				if (oMinX_epMaxX > 0 || oMinY_epMaxY > 0) {
					continue;
				}
				if(o.getInvoker() != null && o.getInvoker().equals(e.getID())){
					continue;
				}
				return e;
			}
		}

		for (Players p : plist) {
			if (p.getID() != o.getID()) {
				scaleMultiplier = (float) (2 / (Math.sqrt((p.getTransform().scale.x * p.getTransform().scale.x) + 
						(p.getTransform().scale.y * p.getTransform().scale.x))));
				epMinX_oMaxX = p.getTransform().pos.x - x2;
				epMinY_oMaxY = (p.getTransform().pos.y - p.getTransform().scale.y * 2) - y1;
				oMinX_epMaxX = x1 - (p.getTransform().pos.x + p.getTransform().scale.x * 2);
				oMinY_epMaxY = y2 - p.getTransform().pos.y;

				if (epMinX_oMaxX > 0 || epMinY_oMaxY > 0) {
					continue;
				}
				if (oMinX_epMaxX > 0 || oMinY_epMaxY > 0) {
					continue;
				}
				if(o.getInvoker() != null && o.getInvoker().equals(p.getID())){
					continue;
				}
				return p;
			}
		}
		return null;
	}
}
