package hitonoriol.madsand.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.entities.inventory.Inventory;
import hitonoriol.madsand.entities.inventory.item.CombatEquipment;
import hitonoriol.madsand.entities.inventory.item.Item;

public class Equipment {
	private HashMap<EquipSlot, Item> equipped = new HashMap<>();
	private float equipmentWeight = 0;
	private PlayerStats stats;

	public Equipment(PlayerStats stats) {
		this.stats = stats;
	}

	public boolean equip(Item item) {
		EquipSlot slot = item.getEquipSlot();
		if (equipped.containsKey(slot))
			equipped.remove(slot);

		equipped.put(slot, item);

		Gui.overlay.equipmentSidebar.equipItem(slot, item);
		equipmentWeight += item.weight;
		return true;
	}

	public boolean equip(CombatEquipment item) {
		equip((Item) item);
		stats.applyBonus(item);
		return true;
	}

	public Item previousEquipment(Item item) {
		return getItem(item.getEquipSlot());
	}

	public boolean unEquip(EquipSlot slot) {
		if (!equipped.containsKey(slot))
			return false;

		Item item = getItem(slot);
		if (item.is(CombatEquipment.class)) {
			CombatEquipment equipment = (CombatEquipment) item;
			equipmentWeight -= equipment.weight;
			stats.removeBonus(equipment);
		}

		Gui.overlay.equipmentSidebar.equipItem(slot, Item.nullItem);

		return equipped.remove(slot) != null;
	}

	public void unEquipAll() {
		for (EquipSlot slot : EquipSlot.values())
			unEquip(slot);
	}

	public boolean unEquip(Item item) {
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
			list.add(inventory.items.indexOf(getItem(slot)));

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
