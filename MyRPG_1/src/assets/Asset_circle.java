package assets;

import render.Model;

public class Asset_circle {
	private static Model model;
	
	public static Model getModel(){
		return model;
	}
	
	public static void initAsset(){
		float radius = 1;
		int numberOfSides = 180;
	    int numberOfVertices = numberOfSides + 2;
	    
	    float twicePi = (float) (2.0f * Math.PI);
	    
	    float circleVerticesX[] = new float[numberOfVertices];
	    float circleVerticesY[] = new float[numberOfVertices];
	    float circleVerticesZ[] = new float[numberOfVertices];
	    
	    circleVerticesX[0] = 0;
	    circleVerticesY[0] = 0;
	    circleVerticesZ[0] = 0;
	    
	    for ( int i = 1; i < numberOfVertices; i++ )
	    {
	        circleVerticesX[i] = (float) (0 + ( radius * Math.cos( i *  twicePi / numberOfSides ) ));
	        circleVerticesY[i] = (float) (0 + ( radius * Math.sin( i * twicePi / numberOfSides ) ));
	        circleVerticesZ[i] = 0;
	    }
	    
		float allCircleVertices[] = new float[numberOfVertices * 3];
	    
	    for ( int i = 0; i < numberOfVertices; i++ )
	    {
	        allCircleVertices[i * 3] = circleVerticesX[i];
	        allCircleVertices[( i * 3 ) + 1] = circleVerticesY[i];
	        allCircleVertices[( i * 3 ) + 2] = circleVerticesZ[i];
	    }
	    
	    
		float[] vertices = new float[]{
							
		};
		vertices = allCircleVertices;
		
		float[] texture = new float[]{
				0,0,
				1,0,
				1,1,			
				0,1,
		};
	
		int[] indicies = new int[numberOfVertices*3];
		//	0,1,2,
		//	2,3,0
		
		
		for(int i = 0; i < numberOfVertices; i++){
			indicies[i*3] = 0;
			indicies[i*3 + 1] = i+1;
			indicies[i*3 + 2] = i+2;
		}
		
		model = new Model(vertices, texture, indicies);
	}
	
	public static void deletAsset(){
		model = null;
	}
}