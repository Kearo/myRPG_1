package basicGameObjects;

public class Equipment {
	private Item head;
	private Item body;
	private Item hands;
	private Item pants;
	private Item boots;
	
	private Item righthand;
	private Item lefthand;
	
	
	
	public Equipment(){
		
	}
	
	public Equipment(Item[] items){
		for(int i = 0; i < items.length; i++){
			
		}
	}
	
	public void equipItem(Item item){
		
	}
	
	public Item getHead() {
		return head;
	}

	public void setHead(Item head) {
		this.head = head;
	}

	public Item getBody() {
		return body;
	}

	public void setBody(Item body) {
		this.body = body;
	}

	public Item getHands() {
		return hands;
	}

	public void setHands(Item hands) {
		this.hands = hands;
	}

	public Item getPants() {
		return pants;
	}

	public void setPants(Item pants) {
		this.pants = pants;
	}

	public Item getBoots() {
		return boots;
	}

	public void setBoots(Item boots) {
		this.boots = boots;
	}

	public Item getRighthand() {
		return righthand;
	}

	public void setRighthand(Item righthand) {
		this.righthand = righthand;
	}

	public Item getLefthand() {
		return lefthand;
	}

	public void setLefthand(Item lefthand) {
		this.lefthand = lefthand;
	}

}
