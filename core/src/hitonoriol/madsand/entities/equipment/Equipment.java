package hitonoriol.madsand.entities.equipment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.entities.PlayerStats;
import hitonoriol.madsand.entities.inventory.Inventory;
import hitonoriol.madsand.entities.inventory.item.AbstractEquipment;
import hitonoriol.madsand.entities.inventory.item.CombatEquipment;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.gui.Gui;

public class Equipment {
	private HashMap<EquipSlot, Item> equipped = new HashMap<>();
	private float equipmentWeight = 0;
	private PlayerStats stats;

	public Equipment(PlayerStats stats) {
		this.stats = stats;
	}

	public boolean equip(Item item) {
		if (itemEquipped(item))
			return false;
		
		if (AbstractEquipment.isCursed(previouslyEquipped(item)))
			return false;

		EquipSlot slot = item.getEquipSlot();
		equipped.put(slot, item);
		Gui.overlay.equipmentSidebar.equipItem(slot, item);
		equipmentWeight += item.weight;
		return true;
	}

	public boolean equip(CombatEquipment item) {
		if (equip((Item) item)) {
			stats.applyBonus(item);
			return true;
		}

		return false;
	}

	public Item previouslyEquipped(Item newItem) {
		return getItem(newItem.getEquipSlot());
	}

	public boolean unEquip(EquipSlot slot) {
		if (!equipped.containsKey(slot))
			return false;

		Item item = getItem(slot);
		item.as(CombatEquipment.class).ifPresent(equipment -> stats.removeBonus(equipment));
		equipmentWeight -= item.weight;
		Gui.overlay.equipmentSidebar.equipItem(slot, Item.nullItem);
		return equipped.remove(slot) != null;
	}

	public void unEquipAll() {
		for (EquipSlot slot : EquipSlot.values)
			unEquip(slot);
	}

	public boolean unEquip(Item item) {
		if (AbstractEquipment.isCursed(item))
			return false;

		return unEquip(item.getEquipSlot());
	}

	public Item getItem(EquipSlot slot) {
		return equipped.getOrDefault(slot, Item.nullItem);
	}

	public boolean itemEquipped(Item item) {
		return (getItem(item.getEquipSlot()) == item);
	}

	@JsonIgnore
	public float getTotalWeight() {
		return equipmentWeight;
	}

	public ArrayList<Integer> getIndexList(Inventory inventory) { //For serializer
		ArrayList<Integer> list = new ArrayList<>();

		for (EquipSlot slot : EquipSlot.values())
			list.add(inventory.getItems().indexOf(getItem(slot)));

		return list;
	}

	public void setStatBonus(boolean apply) {
		for (Entry<EquipSlot, Item> entry : equipped.entrySet()) {
			entry.getValue().as(CombatEquipment.class).ifPresent(equipment -> {
				if (!apply)
					stats.removeBonus(equipment);
				else
					stats.applyBonus(equipment);
			});
		}
	}

	public void refreshUI() {
		for (Entry<EquipSlot, Item> entry : equipped.entrySet())
			Gui.overlay.equipmentSidebar.equipItem(entry.getKey(), entry.getValue());
	}
}
