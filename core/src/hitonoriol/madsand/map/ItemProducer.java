package hitonoriol.madsand.map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.gamecontent.Items;
import hitonoriol.madsand.gamecontent.Objects;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class ItemProducer {
	private int id;
	private int lvl = 0, maxLvl;

	private float upgradeProductionMultiplier = 0.1f;
	private float upgradeRequirementMultiplier = 0.25f;

	private float upgradeRequirement; // quantity of upgradeMaterial required for upgrade

	private float productionRate = 0; // amount of product to produce per realTimeTick
	private float consumptionRate = 0; // amount of "fuel" material to consume per realTimeTick

	private float consumableMaterialStorage = 0; // amount of "fuel" material currently in station's storage
	private float productStorage = 0; // amount of product currently accumulated by the stations's storage
	private float maxProductStorage;

	private int producedMaterial; // Item id -- item produced by the station
	private int consumedMaterial; // Item id -- item required for station upgrade & as "fuel"

	public ItemProducer(int id) {
		this.id = id;
		loadProperties();
	}

	public ItemProducer() {
		this.id = 0;
	}

	private void loadProperties() {
		ItemProducer properties = Objects.all().itemProducers().get(this.id);
		producedMaterial = properties.producedMaterial;
		consumedMaterial = properties.consumedMaterial;

		maxLvl = properties.maxLvl;
		upgradeRequirement = properties.upgradeRequirement;
		upgradeProductionMultiplier = properties.upgradeProductionMultiplier;
		upgradeRequirementMultiplier = properties.upgradeRequirementMultiplier;
		maxProductStorage = properties.maxProductStorage;

		productionRate = properties.productionRate;
		consumptionRate = properties.consumptionRate;
	}

	// Called every realTimeTick
	public void produce() {
		if (!canProduce())
			return;

		productStorage += productionRate;
		consumableMaterialStorage -= consumptionRate;
	}

	public boolean hasFreeStorage() {
		return productStorage < maxProductStorage;
	}

	public boolean hasRawMaterial() {
		return consumableMaterialStorage - consumptionRate >= 0;
	}
	
	public boolean hasProduct() {
		return productStorage >= 1;
	}

	public boolean isEndless() {
		return consumptionRate <= 0;
	}

	public boolean canProduce() {
		return hasFreeStorage() && hasRawMaterial();
	}

	public boolean upgrade() {
		if (lvl >= maxLvl || consumableMaterialStorage < upgradeRequirement)
			return false;

		consumableMaterialStorage -= upgradeRequirement;
		productionRate += (float) (++lvl) * upgradeProductionMultiplier;
		upgradeRequirement += upgradeRequirement * upgradeRequirementMultiplier;
		return true;
	}

	public void addRawMaterial(int quantity) {
		consumableMaterialStorage += quantity;
	}

	@JsonIgnore
	public Item getProduct(int quantity) {
		if (quantity <= 0)
			return Item.nullItem;

		if (productStorage - quantity < 0)
			return getProduct((int) productStorage);

		productStorage -= quantity;
		return Item.create(producedMaterial, quantity);
	}

	@JsonIgnore
	public Item getProduct() {
		return getProduct((int) productStorage);
	}

	@JsonIgnore
	public String getConsumableName() {
		return Items.all().getName(consumedMaterial);
	}

	@JsonIgnore
	public String getProductName() {
		return Items.all().getName(producedMaterial);
	}

	public int getId() {
		return id;
	}

	public float getConsumedMaterialStorage() {
		return consumableMaterialStorage;
	}

	public float getProductStorage() {
		return productStorage;
	}

	public float getMaxProductStorage() {
		return maxProductStorage;
	}

	public int getConsumedMaterialId() {
		return consumedMaterial;
	}

	public int getProductId() {
		return producedMaterial;
	}

	public float getProductionRate() {
		return productionRate;
	}

	public float getConsumptionRate() {
		return consumptionRate;
	}

	public int getLvl() {
		return lvl;
	}

	public int getMaxLvl() {
		return maxLvl;
	}

	public float getUpgradeRequirement() {
		return upgradeRequirement;
	}
}
