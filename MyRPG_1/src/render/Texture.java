package render;

import static org.lwjgl.opengl.GL11.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;

import mechanics.ErrorLog;

import static org.lwjgl.opengl.GL13.*;

public class Texture {
	private int id;
	private int width;
	private int height;
	
	public Texture(String filename){
		BufferedImage image;
		try{
			image = ImageIO.read(new File("./textures/" + filename));
			width = image.getWidth();
			height = image.getHeight();
			int[] raw_pixels = new int[width * height - 4];
			raw_pixels = image.getRGB(0, 0, width, height, null, 0, width);
			ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * 4);
			for(int i = 0; i < width; i++){
				for(int j = 0; j< height; j++){
					int pixel = raw_pixels[i*width + j];
					pixels.put((byte)((pixel >> 16) & 0xFF)); 	//red
					pixels.put((byte)((pixel >> 8) & 0xFF));		//green
					pixels.put((byte)((pixel) & 0xFF));			//blue
					pixels.put((byte)((pixel >> 24) & 0xFF));		//alpha
				}
			}
			
			pixels.flip();
			id = glGenTextures();
			glBindTexture(GL_TEXTURE_2D, id);
			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
		}catch(IOException e){
			e.printStackTrace();
			ErrorLog.writeError("texture_constr.", e);
		}
	}
	
	protected void finalize() throws Throwable{
		glDeleteTextures(id);
		super.finalize();
	}
	
	public void bind(int sampler){
		if(sampler >= 0 && sampler <= 31){
			glActiveTexture(GL_TEXTURE0 + sampler);
			glBindTexture(GL_TEXTURE_2D, id);
		}
	}
	
	public void unBind(int sampler){
		if(sampler >= 0 && sampler <= 31){
			glBindTexture(GL_TEXTURE_2D, id);
			glBindTexture(0,0);
		}
	}
	
}
