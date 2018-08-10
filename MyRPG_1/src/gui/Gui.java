package gui;


import org.joml.Matrix4f;

import assets.Assets;
import io.Window;
import render.Camera;
import render.Shader;
import render.TileSheet;

public class Gui{
	private int fps = 0;
	private	Camera camera;
	private static TileSheet sheet;
	private static boolean init = true;
	private float width;
	private float height;
	private float scale = 64;	
	
	public static void initGui(){
		sheet = new TileSheet("TileSheets/Gui/test.png", 3);
	}
	
	public Gui(Window window ){
		width = window.getWidth()/scale - 1;
		height = window.getHeight()/scale - 1;
		camera = new Camera(window.getWidth(), window.getHeight());
	}
	
	public void update(){
		
	}
	
	public void drawString(float width, float height, int fps){
		//font.drawString(width, height, ""+fps);
	}
	
	public void resizeCamera(Window window){
		camera.setProjection(window.getWidth(), window.getHeight());
	}
	
	public void render(Shader shader){	
		//drawString(width, height, fps);
		
		Matrix4f mat = new Matrix4f();
		camera.getProjection().scale(32, mat);
		mat.translate(-width, height, 0);
		
		shader.bind();
		shader.setUniform("sampler", 0);
		shader.setUniform("projection", mat);
		sheet.bindTile(shader, 3);
		
		Assets.getModel().render();
	}
	
	public void setFPS(int fps){
		this.fps = fps;
	}
	
	public int getFPS(){
		return fps;
	}
	
	public boolean getInit(){
		return init;
	}
}