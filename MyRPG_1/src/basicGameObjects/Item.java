package basicGameObjects;

public class Item {
	protected int id;
	protected String name;
	protected int requiredLevel;
	protected int itemLevel;
	
	
	public Item(){
		
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	protected void calculateItemLevel(){
		
	}
	
	public void render(){
		
	}
}
