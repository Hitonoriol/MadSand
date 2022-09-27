package hitonoriol.madsand.map.object;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.TimeDependent;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.gamecontent.Items;
import hitonoriol.madsand.gamecontent.Objects;
import hitonoriol.madsand.map.CropGrowthStage;
import hitonoriol.madsand.map.CropGrowthStageContainer;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.util.Utils;

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
		growthStages = Items.all().getCropStages(id);
		this.plantTime = plantTime;
		objId = growthStages.getStageObject(curStage);
		initProperties(Objects.all().get(objId));
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
		if (isFullyGrown())
			setGrowthStage(LAST_STAGE);
		else if (getTimeSincePlanted() >= growthStages.getStageLength(curStage))
			setGrowthStage(curStage + 1);
	}

	private void setGrowthStage(int stage) {
		if (stage > LAST_STAGE)
			return;

		curStage = stage;
		id = objId = growthStages.getStageObject(stage);
		Utils.dbg("%s has grown: stage %d/%d", getName(), curStage, LAST_STAGE);
		if (curStage == LAST_STAGE)
			MadSand.world().exec(map -> {
				Pair position = getPosition();
				if (position.isEmpty()) {
					map.getTimeScheduler().remove(this);
					Utils.dbg("{%X} Crop {%s} was not found on the map, removing...",
							map.getTimeScheduler().hashCode(),
							this);
					return;
				}
				Utils.dbg("%s has reached its final growth stage: replacing object at %s", getName(), position);
				map.delObject(position);
				map.addObject(position, id);
			});
	}

	public long getTimeSincePlanted() {
		return MadSand.world().currentActionTick() - plantTime;
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
	
	@Override
	public boolean isEmpty() {
		return super.isEmpty() || this == Map.nullCrop;
	}
}