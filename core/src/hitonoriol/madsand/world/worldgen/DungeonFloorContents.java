package hitonoriol.madsand.world.worldgen;

public class DungeonFloorContents {
	public int fromFloor; // if floor equals or greater than fromFloor
	public DungeonContents contents;

	public DungeonFloorContents(int fromFloor, DungeonContents contents) {
		this.fromFloor = fromFloor;
		this.contents = contents;
	}

	public DungeonFloorContents() {
		this(0, null);
	}
}
