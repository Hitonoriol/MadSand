package hitonoriol.madsand.entities;

public class LootItemEntry {
	public int id;
	public int maxQuantity;

	public LootItemEntry(int id, int maxQuantity) {
		this.id = id;
		this.maxQuantity = maxQuantity;
	}
	
	public LootItemEntry() {
		this(0,0);
	}
}