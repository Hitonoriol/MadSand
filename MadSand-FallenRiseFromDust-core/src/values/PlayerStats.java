package values;

public class PlayerStats {
	public static int hand = 0;
	public static int accur = 2; // ACCUR
	public static int blood = 200; // CONSTITUTION*10
	public static int maxblood = 200;
	public static int atk = 3;// ATK
	public static int luck = 1; //LUCK
	public static int dexterity = 1; //DEX
	public static int intelligence = 1; //INT
	public static float stamina = 50.0F; // STAMINA*5
	public static float maxstamina = 50.0F;
	public static int[] def = new int[3];

	public static int[] rest = {-1,-1,-1,-1};

	public static int helmet = 0;
	public static int cplate = 0;
	public static int shield = 0;

	public static int lvl = 0;
	public static int exp = 0;
	public static int requiredexp = 100;

	public static int[] woodcutterskill = { 1, 0, 50 };
	public static int[] miningskill = { 1, 0, 50 };
	public static int[] survivalskill = { 1, 0, 65 };
	public static int[] harvestskill = { 1, 0, 35 };
	public static int[] craftingskill = { 1, 0, 30 };

	
}