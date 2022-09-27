package hitonoriol.madsand.world.worldgen;

import static hitonoriol.madsand.resources.Resources.loader;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;

import hitonoriol.madsand.Enumerable;

public class WorldGenPreset implements Enumerable {
	private final static String DEF_DIR = "worldgen/";

	public String biomeName;
	public double settlementProbability = 10;
	public double ownedByFactionProbability = 12.5;

	// Biome tile & object sets
	public OverworldPreset overworld = new OverworldPreset();

	// Lake generator params & lake tiles
	public LakePreset lake = loader().load(DEF_DIR + "lake_defaults.json", LakePreset.class);

	// default cave tile & object
	public CavePreset cave = loader().load(DEF_DIR + "cave_defaults.json", CavePreset.class);

	//Dungeon params
	public int dungeonProbability; // Probability to generate dungeon instead of normal cave
	public DungeonPreset dungeon = loader().load(DEF_DIR + "dungeon_defaults.json", DungeonPreset.class);

	public String postGenScript;

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

	@JsonSetter("dungeon")
	private void setDungeon(JsonNode json) {
		dungeon = loader().update(dungeon, json);
	}

	@JsonSetter("lake")
	private void setLake(JsonNode json) {
		lake = loader().update(lake, json);
	}

	@JsonSetter("cave")
	private void setCave(JsonNode json) {
		cave = loader().update(cave, json);
	}

	@Override
	public void setId(int id) {}

	@Override
	public int id() {
		return 0;
	}

	@Override
	public String name() {
		return biomeName;
	}
}