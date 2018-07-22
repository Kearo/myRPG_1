package assets;

import render.Model;

public class Assets {
	private static Model model;
	
	public static Model getModel(){
		return model;
	}
	
	public static void initAsset(){
		float[] vertices = new float[]{
				-1, 1, 0, //top left				0
				1, 1, 0, //top right				1
				1, -1, 0,	//bottom right 			2		
				-1, -1, 0,	//bottom left			3
		};
		
		float[] texture = new float[]{
				0,0,
				1,0,
				1,1,			
				0,1,
		};
		
		int[] indicies = new int[]{
			0,1,2,
			2,3,0
		};
		
		model = new Model(vertices, texture, indicies);
	}
	
	public static void deletAsset(){
		model = null;
	}
}
