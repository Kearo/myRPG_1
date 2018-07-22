package io;

import static org.lwjgl.glfw.GLFW.*;


import org.lwjgl.glfw.GLFWErrorCallback;
import static org.lwjgl.glfw.Callbacks.*;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowSizeCallback;

public class Window {
	private long win;
	private int width, height;
	private boolean fullscreen;
	private boolean hasResized;
	private GLFWWindowSizeCallback windowSizeCallback;
	private Input input;
	
	public static void setCallbacks(){
		glfwSetErrorCallback(new GLFWErrorCallback() {
			@Override
			public void invoke(int error, long description) {
				throw new IllegalStateException(GLFWErrorCallback.getDescription(description));
			}		
		});
	}
	
	private void setLocalCallbacks(){
		windowSizeCallback = new GLFWWindowSizeCallback(){
			@Override
			public void invoke(long argWindow, int argWidth, int argHeight) {
				width = argWidth;
				height = argHeight;
				hasResized = true;
			}	
		};
		glfwSetWindowSizeCallback(win, windowSizeCallback);
	}
	
	public Window(int x, int y, String title){
		setSize(x, y);
		setFullscreen(false);
		hasResized = false;
		createWindow(title);
		input = new Input(win);
	}
	
	public void createWindow(String title){
		win = glfwCreateWindow(width, height, title, fullscreen ? glfwGetPrimaryMonitor() : 0, 0);	//test if fullscreen = true,, if yes use glfw... else 0
		
		if(win == 0){
			throw new IllegalStateException("Window Failed");
		}
		
		if(!fullscreen){
			GLFWVidMode vid = glfwGetVideoMode(glfwGetPrimaryMonitor());
			glfwSetWindowPos(win, (vid.width() - width) / 2, (vid.height() - height) / 2);
			
			glfwShowWindow(win);
		}
		glfwMakeContextCurrent(win);
		
		
		setLocalCallbacks();
	}
	
	public void cleanUp(){
		glfwFreeCallbacks(win);
	}
	
	public boolean shouldClose(){
		return glfwWindowShouldClose(win);
	}
	
	public void swapBuffers(){
		glfwSwapBuffers(win);
	}
	
	public void setSize(int width, int height){
		this.width = width;
		this.height	= height;
	}
	
	public void setFullscreen(boolean fullscreen){
		this.fullscreen = fullscreen;
	}
	
	public void update(){
		hasResized = false;
		//input.update();
		glfwPollEvents();
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight(){
		return height;
	}
	
	public boolean hasResized(){
		return hasResized;
	}
	
	public boolean isFullscreen(){
		return fullscreen;
	}
	
	public long getWindow(){
		return win;
	}
	
	public Input getInput(){
		return input;
	}
	
}
