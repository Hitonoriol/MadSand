package hitonoriol.madsand.world;

import java.util.HashMap;

import hitonoriol.madsand.entities.inventory.Inventory;
import hitonoriol.madsand.enums.WorkerType;

public class Settlement {
	public String name;
	public Inventory warehouse = new Inventory(DEF_MAX_WEIGHT);
	public HashMap<WorkerType, Integer> workers = new HashMap<>();
	
	private static float DEF_MAX_WEIGHT = 50;
}
