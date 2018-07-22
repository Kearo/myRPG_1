package world;

public class Tile {
	public static Tile tiles[] = new Tile[255];
	public static byte not = 0;
	private static String folder = "worldtiles/";
	
	public static final Tile grass = new Tile(folder + "grass");
	public static final Tile dirt = new Tile(folder + "dirt").setSolid();
	
	private byte id;
	private boolean solid;
	private String texture;
	
	public Tile(String texture){
		this.id	= not;
		not++;
		this.texture = texture;
		this.solid = false;
		if(tiles[id] != null){
			throw new IllegalStateException("Tiles at: [" + id + "] is already used!");
		}
		tiles[id] = this;
	}

	public Tile setSolid(){
		this.solid = true;
		return this;
	}
	
	public boolean isSolid(){
		return solid;
	}
	
	public byte getId() {
		return id;
	}

	public String getTexture() {
		return texture;
	}
}
