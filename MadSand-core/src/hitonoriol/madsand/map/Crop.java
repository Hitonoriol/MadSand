package hitonoriol.madsand.map;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.properties.ItemProp;

public class Crop {
	static final int STAGE_COUNT = 4;
	CropGrowthStageContainer growthStages;
	int totalGrowthTime = 0;
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

		if (getTimeSincePlanted() >= growthStages.getStageLength(curStage))
			objId = growthStages.getStageObject(++curStage);

		return true;
	}

	public long getTimeSincePlanted() {
		return MadSand.world.globalRealtimeTick - plantTime;
	}

	public int getGrowthTime() {
		if (totalGrowthTime > 0)
			return totalGrowthTime;

		int time = 0;
		for (CropGrowthStage stageLen : growthStages) {
			Utils.out("Stage len: " + stageLen.stageLength);
			time += stageLen.stageLength;
		}

		totalGrowthTime = time;
		return time;
	}

	public long getHarvestTime() {
		return getGrowthTime() - getTimeSincePlanted();
	}
}