package hitonoriol.madsand.entities;

import hitonoriol.madsand.entities.inventory.item.Item;

public interface StatAction {
	public void _die();

	public void _damage(int amt);

	public void _heal(int amt);

	public void _changeStamina(float by);

	public Item _getItem(int id);
}
