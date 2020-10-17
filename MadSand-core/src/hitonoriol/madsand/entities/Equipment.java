package hitonoriol.madsand.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.enums.EquipSlot;

public class Equipment {
	private HashMap<EquipSlot, Item> equipped = new HashMap<>();
	private float equipmentWeight = 0;
	private Stats stats;
	private boolean isPlayer = false;

	public Equipment(Stats stats) {
		this.stats = stats;
	}

	public Equipment(Stats stats, boolean isPlayer) {
		this(stats);
		this.isPlayer = isPlayer;
	}

	public boolean getIsPlayer() {
		return isPlayer;
	}

	public void setIsPlayer(boolean isPlayer) {
		this.isPlayer = isPlayer;
	}

	private boolean equip(EquipSlot slot, Item item) {
		if (equipped.containsKey(slot))
			equipped.remove(slot);

		equipped.put(slot, item);

		if (isPlayer)
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

		if (isPlayer)
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

	public ArrayList<String> getUidList() { //For serializer
		EquipSlot slots[] = EquipSlot.values();
		int len = slots.length;
		ArrayList<String> list = new ArrayList<>(len);
		Item item;

		for (int i = 0; i < len - 1; ++i) {
			item = getItem(EquipSlot.getByNumber(i));
			list.add(item.uid);
		}

		list.add(getHand().id + "");

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
		if (!isPlayer)
			return;

		for (Entry<EquipSlot, Item> entry : equipped.entrySet())
			Gui.overlay.equipmentSidebar.equipItem(entry.getKey(), entry.getValue());

	}
}
