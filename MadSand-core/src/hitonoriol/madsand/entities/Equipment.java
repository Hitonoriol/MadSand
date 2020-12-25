package hitonoriol.madsand.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.entities.inventory.Inventory;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.enums.EquipSlot;

public class Equipment {
	private HashMap<EquipSlot, Item> equipped = new HashMap<>();
	private float equipmentWeight = 0;
	private PlayerStats stats;

	public Equipment(PlayerStats stats) {
		this.stats = stats;
	}

	boolean equip(EquipSlot slot, Item item) {
		if (equipped.containsKey(slot))
			equipped.remove(slot);

		equipped.put(slot, item);

		Gui.overlay.equipmentSidebar.equipItem(slot, item);

		equipmentWeight += item.weight;
		stats.applyBonus(item);
		return true;
	}

	public boolean equip(Item item) {
		EquipSlot slot = EquipSlot.slotByType(item.type);

		if (slot == null)
			return false;

		return equip(slot, item);
	}

	public Item previousEquipment(Item item) {
		return getItem(EquipSlot.slotByType(item.type));
	}

	public boolean unEquip(EquipSlot slot) {
		if (!equipped.containsKey(slot))
			return false;

		Item item = equipped.get(slot);
		equipmentWeight -= item.weight;
		stats.removeBonus(item);

		Gui.overlay.equipmentSidebar.equipItem(slot, Item.nullItem);

		return equipped.remove(slot) != null;
	}

	public void unEquipAll() {
		for (EquipSlot slot : EquipSlot.values())
			unEquip(slot);
	}

	public boolean unEquip(Item item) {
		return unEquip(EquipSlot.slotByType(item.type));
	}

	public Item getItem(EquipSlot slot) {
		return equipped.getOrDefault(slot, Item.nullItem);
	}

	public Item getHand() {
		return getItem(EquipSlot.MainHand);
	}

	public void setHand(Item item) {
		unEquip(EquipSlot.MainHand);
		equip(EquipSlot.MainHand, item);
	}

	public boolean itemEquipped(Item item) {
		EquipSlot slot = EquipSlot.slotByType(item.type);
		return (getItem(slot) == item);
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
		for (Entry<EquipSlot, Item> entry : equipped.entrySet())
			if (!apply)
				stats.removeBonus(entry.getValue());
			else
				stats.applyBonus(entry.getValue());
	}

	public void refreshUI() {
		for (Entry<EquipSlot, Item> entry : equipped.entrySet())
			Gui.overlay.equipmentSidebar.equipItem(entry.getKey(), entry.getValue());
	}
}
