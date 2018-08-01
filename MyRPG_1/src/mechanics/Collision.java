package mechanics;

import java.util.List;

import basicGameObjects.Enemy;
import basicGameObjects.Players;
import render.Transform;
import world.World;

public class Collision {

	public static boolean checkCollisionPlayers(Players p, World world) {
		if(true){
			return false;
		}
		
		List<Enemy> elist;
		List<Players> plist;
		
		float dx = p.getDirection().x;
		float dy = p.getDirection().y;
		float x = p.getTransform().pos.x + p.getTransform().scale.x;
		float y = p.getTransform().pos.y - p.getTransform().scale.y;
		float ix = x + 0;
		float iy = y + 0;

		if (dx > 0) { // left- 0 right+
			ix = (int) (x + dx + 1);
		}
		if (dx < 0) {
			ix = (int) (x - dx + 1);
		}

		if (dy > 0) { // top+ 0 down-
			iy = (int) (y + dy - 1);
		}
		if (dy < 0) {
			iy = (int) (y - dy - 1);
		}
		iy = Math.abs(iy);

		if (world.getTile((int) ix / 2, (int) iy / 2).isSolid()) {
			return true;
		}
		// corners
		ix = x + 0;
		iy = y + 0;
		if (dx > 0) { // right
			ix = (int) Math.round(x * 2 + dx);
			iy = (int) Math.round(y * 2);
		}
		if (dx < 0) { // left
			ix = (int) Math.round(p.getTransform().pos.x + dx);
			iy = (int) Math.round(y * 2);
		}

		if (dy > 0) { // top
			ix = (int) Math.round(x * 2);
			iy = (int) Math.round(p.getTransform().pos.y + dy);
		}
		if (dy < 0) { // down
			ix = (int) Math.round(x * 2);
			iy = (int) Math.round(y * 2 + dy);
		}
		// ix = Math.abs(ix);
		iy = Math.abs(iy);

//		if (world.getTile((int) ix / 2, (int) iy / 2).isSolid()) {
//			return true;
//		}

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
					if (xCol <= radHitX + p.getTransform().scale.x + dx && yCol <= radHitY + p.getTransform().scale.y) {
						return true;
					}
				}
				if (xSide > 0) {
					if (xCol <= radHitX + p.getTransform().scale.x - dx && yCol <= radHitY + p.getTransform().scale.y) {
						return true;
					}
				}
				if (ySide < 0) {
					if (xCol <= radHitX + p.getTransform().scale.x && yCol <= radHitY + p.getTransform().scale.y + dy) {
						return true;
					}
				}
				if (ySide > 0) {
					if (xCol <= radHitX + p.getTransform().scale.x && yCol <= radHitY + p.getTransform().scale.y - dy) {
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
					if (xCol <= radHitX + p.getTransform().scale.x + dx && yCol <= radHitY + p.getTransform().scale.y) {
						return true;
					}
				}
				if (xSide > 0) {
					if (xCol <= radHitX + p.getTransform().scale.x - dx && yCol <= radHitY + p.getTransform().scale.y) {
						return true;
					}
				}
				if (ySide < 0) {
					if (xCol <= radHitX + p.getTransform().scale.x && yCol <= radHitY +p.getTransform().scale.y + dy) {
						return true;
					}
				}
				if (ySide > 0) {
					if (xCol <= radHitX + p.getTransform().scale.x && yCol <= radHitY + p.getTransform().scale.y - dy) {
						return true;
					}
				}
			}
		}
		return false;

	}
}
