package io;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorEnterCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import world.World;

public class Input {
	private long window;
	private List<String> actionList;
	private List<Integer> actionModeList;

	private GLFWKeyCallback keyCall;
	private GLFWCursorPosCallback curPos;
	private GLFWCursorEnterCallback curEnter;
	private GLFWMouseButtonCallback curButton;
		

	public Input(long window) {
		this.window = window;
			
		actionList = new ArrayList<String>();
		actionModeList = new ArrayList<Integer>();
		
		KeySettings.setKeySettings();

		initCallbacks();
	}

	private void initCallbacks() {
		keyCall = new GLFWKeyCallback() {
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
				if((!actionList.contains(KeySettings.getAlocatedKey(key))) && action
						!= GLFW.GLFW_REPEAT){
					actionList.add(KeySettings.getAlocatedKey(key));
					actionModeList.add(action);
				}else{
					if(action == GLFW.GLFW_RELEASE){
						actionList.remove(KeySettings.getAlocatedKey(key));
					}
				}
			}
		};
		GLFW.glfwSetKeyCallback(window, keyCall);
		
		curPos = new GLFWCursorPosCallback() {
			@Override
			public void invoke(long window, double xpos, double ypos) {
				// TODO Auto-generated method stub
				World.setMouseX((float)xpos);
				World.setMouseY((float)ypos);
		//		System.out.println(ypos);
			}
		};
		GLFW.glfwSetCursorPosCallback(window, curPos);
		
		
		curEnter = new GLFWCursorEnterCallback() {
			@Override
			public void invoke(long window, boolean entered) {
				// TODO Auto-generated method stub
			}			
		};
		GLFW.glfwSetCursorEnterCallback(window, curEnter);
		
		
		curButton = new GLFWMouseButtonCallback() {
			@Override
			public void invoke(long window, int button, int action, int mods) {
				// TODO Auto-generated method stub
				if((!actionList.contains(KeySettings.getAlocatedKey(button))) && action
						!= GLFW.GLFW_REPEAT){
					actionList.add(KeySettings.getAlocatedKey(button));
					actionModeList.add(action);
				}else{
					if(action == GLFW.GLFW_RELEASE){
						actionList.remove(KeySettings.getAlocatedKey(button));
					}
				}
				
			}			
		};
		GLFW.glfwSetMouseButtonCallback(window, curButton);	
	}

	
	public List<String> getactionList(){
		return actionList;
	}
	
}
