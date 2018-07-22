package game;

import java.io.FileNotFoundException;

import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;

import audio.AudioMaster;
import audio.Source;
import mechanics.ErrorLog;

public class AudioManager {
	private static Source musicPlayer;

	public static void init() {
		AudioMaster.init();
		AudioMaster.setListenerData(0, 0, 0);
		AL10.alDistanceModel(AL11.AL_LINEAR_DISTANCE);
		musicPlayer = new Source();
		musicPlayer.stop();
	}

	public static void playSoundEffect(String soundName, float x, float y, float z) {
		Source s = new Source();
		s.setPosition(x, y, z);
		s.setLooping(false);
		s.setVolume(0.4f);
		try {
			s.play(AudioMaster.loadSound(soundName));
		} catch (FileNotFoundException e) {
			ErrorLog.writeError("audioManager_playEffect", e);
			e.printStackTrace();
		}
	}

	public static void playMusic(String soundName, float x, float y, float z) {
		try {
			musicPlayer.play(AudioMaster.loadSound("music/"+ soundName));
		} catch (FileNotFoundException e) {
			ErrorLog.writeError("audioManager:music", e);
			e.printStackTrace();
		}
		musicPlayer.setPosition(x, y, z);
		musicPlayer = new Source();
		musicPlayer.setLooping(true);
		musicPlayer.setVolume(0.4f);
	}

	public static void stopMusic() {
		musicPlayer.stop();
	}
	
	public static void setListenerPos(float x, float y, float z){
		AudioMaster.setListenerData(x, y, z);
		if(musicPlayer.isPlaying()){
			musicPlayer.setPosition(x, y, z);
		}
	}
}
