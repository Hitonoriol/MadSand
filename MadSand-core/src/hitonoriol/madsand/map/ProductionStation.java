package hitonoriol.madsand.map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.properties.ObjectProp;

public class ProductionStation {
	public int id;
	public int lvl = 0, maxLvl;

	public float upgradeProductionMultiplier = 0.1f;
	public float upgradeRequirementMultiplier = 0.25f;

	public float upgradeRequirement; // quantity of upgradeMaterial required for upgrade

	public float productionRate = 0; // amount of product to produce per realTimeTick
	public float consumptionRate = 0; // amount of "fuel" material to consume per realTimeTick

	public float consumableMaterialStorage = 0; // amount of "fuel" material currently in station's storage
	public float productStorage = 0; // amount of product currently accumulated by the stations's storage
	public float maxProductStorage;

	public int producedMaterial; // Item id -- item produced by the station
	public int consumedMaterial; // Item id -- item required for station upgrade & as "fuel"

	public ProductionStation(int id) {
		this.id = id;
		loadProperties();
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

	public void addConsumableItem(int quantity) {
		consumableMaterialStorage += quantity;
	}

	@JsonIgnore
	public Item getProduct(int quantity) {
		if (productStorage - quantity < 0)
			return Item.nullItem;

		productStorage -= quantity;
		return new Item(producedMaterial, quantity);
	}
}
