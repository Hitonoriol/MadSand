package hitonoriol.madsand.map;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.properties.ItemProp;

public class Crop {
	static final int STAGE_COUNT = 4;
	CropGrowthStageContainer growthStages;
	public int curStage = 0;
	public long plantTime;
	public int id;
	int objId;

	public Crop(int id, long plantTime) {
		this.id = id;
		growthStages = ItemProp.getCropStages(id);
		this.plantTime = plantTime;
		objId = growthStages.getStageObject(curStage);
	}

	public Crop(int id, long plantTime, int stage) {
		this(id, plantTime);
		curStage = stage;
		objId = growthStages.getStageObject(curStage);
	}

	public Crop() {
		id = 0;
	}

	boolean upd() {
		if ((curStage + 1) >= STAGE_COUNT)
			return false;

		if (MadSand.world.globalTick - plantTime >= growthStages.getStageLength(curStage))
			objId = growthStages.getStageObject(++curStage);

		return true;
	}
}