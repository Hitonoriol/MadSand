package ru.bernarder.fallenrisefromdust.map;

import java.util.Vector;

import ru.bernarder.fallenrisefromdust.MadSand;
import ru.bernarder.fallenrisefromdust.properties.CropProp;

public class Crop {
	static final int STAGE_COUNT = 4;
	Vector<Integer> stages;
	Vector<Integer> stagelen;
	public int curStage = 0;
	public long plantTime;
	public int id;
	int objId;

	public Crop(int id, long plantTime) {
		this.id = id;
		stages = CropProp.stages.get(id);
		this.plantTime = plantTime;
		objId = stages.get(curStage);
		stagelen = CropProp.stagelen.get(id);
	}

	public Crop(int id, long plantTime, int stage) {
		this(id, plantTime);
		curStage = stage;
		objId = stages.get(curStage);
	}

	public Crop() {
		id = 0;
	}

	boolean upd() {
		if ((curStage + 1) < STAGE_COUNT && MadSand.world.globalTick - plantTime >= stagelen.get(curStage)) {
			objId = stages.get(++curStage);
			return true;
		}
		return false;
	}
}