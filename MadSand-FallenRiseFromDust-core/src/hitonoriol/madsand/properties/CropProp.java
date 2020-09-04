package hitonoriol.madsand.properties;

import java.util.HashMap;
import java.util.Vector;

public class CropProp {
	public static HashMap<Integer, Vector<Integer>> stages = new HashMap<Integer, Vector<Integer>>();	//object ids by growth stages(0-4)
	public static HashMap<Integer, Vector<Integer>> stagelen = new HashMap<Integer, Vector<Integer>>();	// growth stage length in ticks
}
