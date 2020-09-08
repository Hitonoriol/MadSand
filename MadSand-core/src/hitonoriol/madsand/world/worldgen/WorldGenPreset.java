package hitonoriol.madsand.world.worldgen;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class WorldGenPreset {
	public String biomeName;

	// Biome tile & object sets
	public OverworldPreset overworld = new OverworldPreset();

	// Lake generator params & lake tiles
	public LakePreset lake = new LakePreset();

	// default cave tile & object
	public CavePreset cave = new CavePreset();

	//Dungeon params
	public int dungeonProbability; // Probability to generate dungeon instead of normal cave
	public DungeonPreset dungeon = new DungeonPreset();

	@JsonIgnore
	public int getDefaultTile() {
		return overworld.defaultTile;
	}

	@JsonIgnore
	public ArrayList<RollList> getBiomeTiles() {
		return overworld.tiles;
	}

	@JsonIgnore
	public ArrayList<RollList> getBiomeObjects() {
		return overworld.objects;
	}

	@JsonIgnore
	public LakePreset getBiomeLake() {
		return lake;
	}

	@JsonIgnore
	public CavePreset getBiomeUnderworld() {
		return cave;
	}

	@JsonIgnore
	public DungeonPreset getBiomeDungeon() {
		return dungeon;
	}
	
	@JsonIgnore
	public CavePreset getBiomeCave() {
		return cave;
	}
}