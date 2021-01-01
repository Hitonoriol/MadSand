package hitonoriol.madsand.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import hitonoriol.madsand.world.worldgen.WorldGenPreset;

public class WorldGenProp {

	public static WorldGenPreset nullPreset = new WorldGenPreset();
	public static HashMap<Integer, WorldGenPreset> biomes = new HashMap<>();
	public static List<String> encounters = new ArrayList<>();

	public static WorldGenPreset getBiome(int biomeId) {
		return biomes.getOrDefault(biomeId, nullPreset);
	}

}
