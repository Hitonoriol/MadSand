package hitonoriol.madsand.map;

import java.util.ArrayList;

public class CropGrowthStageContainer extends ArrayList<CropGrowthStage> {
	public static int MAX_STAGES = 4;

	public CropGrowthStageContainer() {
		super(MAX_STAGES);
	}

	public int getStageObject(int stage) { // Get Crop MapObject to display on map during the <stage>
		return super.get(stage).objectId;
	}

	public int getStageLength(int stage) { // Get <growth stage> duration in ticks
		int duration = 0;

		for (int i = 0; i <= stage; ++i)
			duration += super.get(i).stageLength;

		return duration;
	}
}
