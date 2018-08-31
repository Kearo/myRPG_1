package basicGameObjects;

import org.joml.Vector2f;

import render.Texture;
import render.Transform;
import world.World;

public abstract class BasisObject {
	protected Transform transform;
	protected Vector2f direction;
	protected World world;
	protected String id;
	protected boolean inBetweenDistance = false;
	protected Vector2f inBetween;
	protected String invokerID = null;
	protected static Texture tex[] = null;
	
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
	
	public void setInBetween(float x, float y){
		inBetweenDistance = true;
		inBetween = new Vector2f(x,y);
	}
	
	public String getInvoker(){
		return invokerID;
	}
}
