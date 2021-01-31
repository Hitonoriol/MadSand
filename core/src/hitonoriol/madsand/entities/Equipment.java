package hitonoriol.madsand.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.entities.inventory.Inventory;
import hitonoriol.madsand.entities.inventory.item.AbstractEquipment;
import hitonoriol.madsand.entities.inventory.item.CombatEquipment;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.inventory.item.Tool;

public class Equipment {
	private HashMap<EquipSlot, AbstractEquipment> equipped = new HashMap<>();
	private float equipmentWeight = 0;
	private PlayerStats stats;

	public Equipment(PlayerStats stats) {
		this.stats = stats;
	}

	public boolean equip(AbstractEquipment item) {
		EquipSlot slot = item.getEquipSlot();
		if (equipped.containsKey(slot))
			equipped.remove(slot);

		equipped.put(slot, item);

		Gui.overlay.equipmentSidebar.equipItem(slot, item);
		equipmentWeight += item.weight;
		return true;
	}

	public boolean equip(CombatEquipment item) {
		equip((AbstractEquipment) item);
		stats.applyBonus(item);
		return true;
	}

	public AbstractEquipment previousEquipment(AbstractEquipment item) {
		return getItem(item.getEquipSlot());
	}

	public boolean unEquip(EquipSlot slot) {
		if (!equipped.containsKey(slot))
			return false;

		AbstractEquipment item = equipped.get(slot);
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

	public AbstractEquipment getItem(EquipSlot slot) {
		return equipped.getOrDefault(slot, Item.nullItem);
	}

	public Item getHand() {
		return getItem(EquipSlot.MainHand);
	}

	public void setHand(Item item) {
		unEquip(EquipSlot.MainHand);
		equip(EquipSlot.MainHand, item);
	}

	private boolean itemEquipped(Item item, boolean allowRegularItems) {
		EquipSlot slot = allowRegularItems ? EquipSlot.slotByTypeAll(item.type) : EquipSlot.slotByType(item.type);
		return (getItem(slot) == item);
	}

	public boolean itemEquipped(Item item) {
		return itemEquipped(item, false);
	}

	public boolean itemEquippedOrHeld(Item item) {
		return itemEquipped(item, true);
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
