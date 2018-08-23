package basicGameObjects;

import org.joml.Vector2f;

import render.Transform;
import world.World;

public abstract class BasisObject {
	protected Transform transform;
	protected Vector2f direction;
	protected World world;
	protected String id;
	
	public Transform getTransform() {
		return transform;
	}
	
	public Vector2f getDirection() {
		return direction;
	}
	
	public World getWorld(){
		return world;
	}
	
	public String getID() {
		return id;
	}
}
