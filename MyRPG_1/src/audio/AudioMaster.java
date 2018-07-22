package audio;

import java.io.BufferedInputStream;
 
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import audio.WaveData;
import mechanics.ErrorLog;


public class AudioMaster {
	private static long device, context;
	private static ALCCapabilities deviceCaps;
	private static IntBuffer contextAttribList;
	private static List<Integer> buffers = new ArrayList<Integer>();
	
	public static void init(){
		device = ALC10.alcOpenDevice((ByteBuffer)null);
		
		deviceCaps = ALC.createCapabilities(device);
		
		contextAttribList = BufferUtils.createIntBuffer(16);
		
		context = ALC10.alcCreateContext(device, contextAttribList);
		
		if(!ALC10.alcMakeContextCurrent(context)) {
			try {
				throw new Exception("Failed to make context current");
			} catch (Exception e) {
				ErrorLog.writeError("audio", e);
				e.printStackTrace();
			}	
		}
		AL.createCapabilities(deviceCaps);
	}
	
	public static void setListenerData(float x, float y, float z){
		AL10.alListener3f(AL10.AL_POSITION, x, y, z);
		AL10.alListener3f(AL10.AL_VELOCITY, 0, 0, 0);
	}
	
	public static int loadSound(String file) throws FileNotFoundException{//wav needs to be mono
		  File ffile = new File("./audio/" + file);
		  int buffer = AL10.alGenBuffers();
		  buffers.add(buffer);
		  BufferedInputStream is = new BufferedInputStream(new FileInputStream(ffile));
		  WaveData waveFile = WaveData.create(is);
		  AL10.alBufferData(buffer, waveFile.format, waveFile.data, waveFile.samplerate);
		  waveFile.dispose();
		  return buffer;
		 }
	
	public static void cleanUp(){
		for(int buffer : buffers){
			AL10.alDeleteBuffers(buffer);
		}
		ALC10.alcDestroyContext(context);
		ALC10.alcCloseDevice(device);
		ALC.destroy();
	}
}
