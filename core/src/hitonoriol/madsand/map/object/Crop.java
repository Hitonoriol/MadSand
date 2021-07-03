package hitonoriol.madsand.map.object;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.TimeDependent;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.map.CropGrowthStage;
import hitonoriol.madsand.map.CropGrowthStageContainer;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.properties.ObjectProp;

public class Crop extends MapObject implements TimeDependent {
	static final int STAGE_COUNT = 4, LAST_STAGE = STAGE_COUNT - 1;
	CropGrowthStageContainer growthStages;
	int totalGrowthTime = 0;
	public int curStage = 0;
	public long plantTime;
	int objId, itemId;

	public Crop(int id, long plantTime) {
		super();
		itemId = id;
		growthStages = ItemProp.getCropStages(id);
		this.plantTime = plantTime;
		objId = growthStages.getStageObject(curStage);
		initProperties(ObjectProp.getObject(objId));
	}

	public Crop(int id, long plantTime, int stage) {
		this(id, plantTime);
		curStage = stage;
		objId = growthStages.getStageObject(curStage);
	}

	public Crop() {
		itemId = 0;
	}

	@Override
	public void update() {
		if ((curStage + 1) >= STAGE_COUNT)
			return;

		if (isFullyGrown())
			setGrowthStage(LAST_STAGE);

		if (getTimeSincePlanted() >= growthStages.getStageLength(curStage))
			setGrowthStage(curStage + 1);
	}

	private void setGrowthStage(int stage) {
		if (stage > LAST_STAGE)
			return;

		curStage = stage;
		id = objId = growthStages.getStageObject(stage);
		if (curStage == LAST_STAGE)
			MadSand.world().exec(map -> {
				Pair position = getPosition();
				map.delObject(position);
				map.addObject(position, id);
			});
	}

	public long getTimeSincePlanted() {
		return MadSand.world().currentRealtimeTick() - plantTime;
	}

	public int getGrowthTime() {
		if (totalGrowthTime > 0)
			return totalGrowthTime;

		int time = 0;
		for (CropGrowthStage stageLen : growthStages)
			time += stageLen.stageLength;

		totalGrowthTime = time;
		return time;
	}

	public long getHarvestTime() {
		return getGrowthTime() - getTimeSincePlanted();
	}

	public int getSeedsId() {
		return itemId;
	}

	public boolean isFullyGrown() {
		return getHarvestTime() <= 0;
	}
}