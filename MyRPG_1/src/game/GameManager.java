package game;

import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
//import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;
//import static org.lwjgl.opengl.GL11.glClear;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.lwjgl.opengl.GL;

import assets.Asset_circle;
import assets.Assets;
import gui.Gui;
import io.Window;
import mechanics.ErrorLog;
//import render.Camera;
import render.Shader;
import world.TileRenderer;
//import world.World;

public class GameManager {
	private boolean worldIP = false;
	private String ip;
	private static boolean stopped = false;
	private static boolean accessSuccess = false;
	private GameLogicManager logic;
	private static long lastFpsCheck = 0;
	private static int currentFPS = 0;
	private static int totalFrames = 0;

	private static Window window;
	public static Gui gui;
	private Shader shader;
	private Shader shadergui;
	private TileRenderer tilerenderer;

	public GameManager() {
		ErrorLog.initLog();
		getIP();
		init();
	}

	private void getIP() {
		if (worldIP) {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String input;
			input = null;
			System.out.println("Enter new IP!");
			try {
				input = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				ErrorLog.writeError("GM_ip", e);
			}
			ip = input;
		}
	}

	private void init() {
		if (!glfwInit()) {
			System.err.println("failed");
			System.exit(1);
		}
		startWindow();
		initAudio();
		startGame();
		manageWindow();
		glfwTerminate();
	}

	private void initAudio() {
		AudioManager.init();
	}

	private void startGame() {
		logic = new GameLogicManager(worldIP, ip);
		logic.startThread();
	}

	private void startWindow() {
		Window.setCallbacks();
		window = new Window(1280, 720, "MyRPG");
		gui = new Gui(window);

		glfwSwapInterval(1);
		GL.createCapabilities();

		glEnable(GL_BLEND); // transparency by animations
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_TEXTURE_2D);
		
		Assets.initAsset();
		Asset_circle.initAsset();

		shader = new Shader("shader");
		shadergui = new Shader("gui");

		tilerenderer = new TileRenderer();
	}

	private void manageWindow() {
		while (!accessSuccess) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
				ErrorLog.writeError("GM_sleep", e);
			}
		}
		while (!window.shouldClose()) {
			totalFrames++;
			if (System.nanoTime() > lastFpsCheck + 1000000000) {
				lastFpsCheck = System.nanoTime();
				currentFPS = totalFrames;
				totalFrames = 0;
				gui.setFPS(currentFPS);
				// System.out.println("FPS: " + currentFPS);
			}

			if (window.hasResized()) {
				GameLogicManager.setWindow();
				glViewport(0, 0, window.getWidth(), window.getHeight());
			}

			GameLogicManager.render(shader, tilerenderer);

			window.update();

			if (gui.getInit()) {
				gui.update();
				gui.render(shadergui);
			}
			// end
			window.swapBuffers();

		}
		GameLogicManager.setStoppped();
		AudioManager.closeAudio();
	}

	public static void loginSuccess() {
		accessSuccess = true;
	}

	public synchronized static Window getWindow() {
		return window;
	}

	public synchronized static boolean getStopped() {
		return stopped;
	}
}
