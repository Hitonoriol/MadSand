package hitonoriol.madsand.map;

public class CropGrowthStage {
	public int stageLength;
	public int objectId;

	public CropGrowthStage(int stageLength, int objectId) {
		this.stageLength = stageLength;
		this.objectId = objectId;
	}

	public CropGrowthStage() {
		this(0, 0);
	}
}
