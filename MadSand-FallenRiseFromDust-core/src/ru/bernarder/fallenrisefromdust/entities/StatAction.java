package ru.bernarder.fallenrisefromdust.entities;

import ru.bernarder.fallenrisefromdust.entities.inventory.Item;

public interface StatAction {
	public void _die();

	public void _damage(int amt);

	public void _heal(int amt);

	public Item _getItem(int id);
}
