package io;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

public class KeySettings {
	private static int numberOfKeys = 6;
	private static Map<Integer, String> keys = new HashMap<Integer, String>();
	private static boolean isInit = false;


	private static int playerMoveLeft = GLFW.GLFW_KEY_A;
	private static int playerMoveRight = GLFW.GLFW_KEY_D;
	private static int playerMoveUp = GLFW.GLFW_KEY_W;
	private static int playerMoveDown = GLFW.GLFW_KEY_S;
	
	private static int attackMove = GLFW.GLFW_MOUSE_BUTTON_1;
		
	private static int pickUpItem = GLFW.GLFW_KEY_F;
	
	
	public static void setAttackMove(int attackMove) {
		KeySettings.attackMove = attackMove;
	}

	public static void setMoveLeft(int playerMoveLeft) {
		KeySettings.playerMoveLeft = playerMoveLeft;
	}

	public static void setMoveRight(int playerMoveRight) {
		KeySettings.playerMoveRight = playerMoveRight;
	}

	public static void setMoveUp(int playerMoveUp) {
		KeySettings.playerMoveUp = playerMoveUp;
	}

	public static void setMoveDown(int playerMoveDown) {
		KeySettings.playerMoveDown = playerMoveDown;
	}
	
	public static void setPickUpItem(int pickUpItem) {
		KeySettings.pickUpItem = pickUpItem;
	}
	

	public static String getAlocatedKey(int pressedKey){
		if(isInit){
			return keys.get(pressedKey);
		}else{
			return null;
		}		
	}
	
	public static void setKeySettings(){
		keys.put(playerMoveLeft, "playerMoveLeft");
		keys.put(playerMoveRight, "playerMoveRight");
		keys.put(playerMoveUp, "playerMoveUp");
		keys.put(playerMoveDown, "playerMoveDown");
		
		keys.put(attackMove, "attackMove");
		
		keys.put(pickUpItem, "pickUpItem");
		
		isInit = true;
	}
	
	public static int getNumberOfKeys(){
		return numberOfKeys;
	}
	
}
