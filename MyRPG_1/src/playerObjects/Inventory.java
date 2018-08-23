package playerObjects;

import basicGameObjects.Item;

public class Inventory {
	private Item[] inven;
	private int freeSlot;
	
	public Inventory(){
		inven = new Item[25];
		freeSlot = 0;
	}
	
	public Inventory(Item[] items){
		this.inven = items;
		checkFreeSlot();
	}
	
	public void addItem(Item item){
		if(freeSlot > -1){
			inven[freeSlot] = item;
		}
		checkFreeSlot();
	}
	
	public void removeItem(Item item){
		for(int i = 0; i < inven.length; i++){
			if(inven[i] == item){
				inven[i] = null;
			}
		}
		checkFreeSlot();
	}
	
	private void checkFreeSlot(){
		freeSlot = -1;
		for(int i = 0; i < inven.length; i++){
			if(inven[i] == null){
				freeSlot = i;
			}
		}
	}
	
	public void render(){
		
	}
}
