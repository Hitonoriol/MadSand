package hitonoriol.madsand.map;

import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.properties.ObjectProp;

public class ProductionStation {
	int id;
	int lvl = 0, maxLvl;

	float upgradeProductionMultiplier = 0.1f;
	float upgradeRequirementMultiplier = 0.25f;

	int upgradeRequirement; // quantity of upgradeMaterial required for upgrade

	float productionRate = 0; // amount of product to produce per realTimeTick
	float consumptionRate = 0; // amount of "fuel" material to consume per realTimeTick

	float consumableMaterialStorage = 0; // amount of "fuel" material currently in station's storage
	float productStorage = 0; // amount of product currently accumulated by the stations's storage
	float maxProductStorage;

	int producedMaterial; // Item id -- item produced by the station
	int consumedMaterial; // Item id -- item required for station upgrade & as "fuel"

	public ProductionStation(int id) {
		this.id = id;
		loadProperties();
		upgrade();
	}

	public ProductionStation() {
		this.id = 0;
	}

	private void loadProperties() {
		ProductionStation properties = ObjectProp.productionStations.get(this.id);
		this.producedMaterial = properties.producedMaterial;
		this.consumedMaterial = properties.consumedMaterial;

		this.maxLvl = properties.maxLvl;
		this.upgradeRequirement = properties.upgradeRequirement;
		this.upgradeProductionMultiplier = properties.upgradeProductionMultiplier;
		this.upgradeRequirementMultiplier = properties.upgradeRequirementMultiplier;
		this.maxProductStorage = properties.maxProductStorage;

		this.productionRate = properties.productionRate;
		this.consumptionRate = properties.consumptionRate;

	}

	// Called every realTimeTick
	public void produce() {
		if (productStorage >= maxProductStorage)
			return;
		if (consumableMaterialStorage - consumptionRate < 0)
			return;

		productStorage += productionRate;
		consumableMaterialStorage -= consumptionRate;
	}

	public boolean upgrade() {
		if (lvl >= maxLvl || consumableMaterialStorage < upgradeRequirement)
			return false;

		consumableMaterialStorage -= upgradeRequirement;
		productionRate += (float) (++lvl) * upgradeProductionMultiplier;
		upgradeRequirement += upgradeRequirement * upgradeRequirementMultiplier;
		return true;
	}

	public void addConsumableItem(Item item) {
		if (item.id != consumedMaterial)
			return;

		consumableMaterialStorage += item.quantity;
	}

	public Item getProduct(int quantity) {
		if (productStorage - quantity < 0)
			return Item.nullItem;

		productStorage -= quantity;
		return new Item(producedMaterial, quantity);
	}
}
