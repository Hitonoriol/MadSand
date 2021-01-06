package hitonoriol.madsand.dialog;

import java.util.HashMap;

import org.apache.commons.text.StringSubstitutor;

import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import me.xdrop.jrand.JRand;
import me.xdrop.jrand.generators.person.FirstnameGenerator;
import me.xdrop.jrand.model.person.Gender;

public class GameTextSubstitutor {
	public static final String DELIM_L = "{", DELIM_R = "}";

	static final String RAND_NAME = "RANDOM_NAME";
	static final String RAND_NAME_M = "RANDOM_NAME_M", RAND_NAME_F = "RANDOM_NAME_F";

	public static final String QUEST_ITEM_REWARD = "ITEM_REWARD", QUEST_EXP_REWARD = "EXP_REWARD";
	public static final String QUEST_ITEM_OBJECTIVE = "ITEM_OBJECTIVE", QUEST_KILL_OBJECTIVE = "KILL_OBJECTIVE";
	public static final String PLAYER_NAME = "PLAYER";
	public static final String LINEBREAK = "br";

	private HashMap<String, String> globalConstants = new HashMap<>();
	StringSubstitutor substitutor = new StringSubstitutor(globalConstants, DELIM_L, DELIM_R);

	private static GameTextSubstitutor instance = new GameTextSubstitutor();

	private GameTextSubstitutor() {

	}

	public static void init() {
		instance.globalConstants.put(LINEBREAK, Resources.LINEBREAK);
	}

	public static void add(String var, String subText) {
		Utils.out("Substitutor: Setting " + DELIM_L + var + DELIM_R + " to \"" + subText + "\"");
		instance.globalConstants.put(var, subText);
	}

	public static String replace(String str) {
		instance.createRandomEntries(str);
		return instance.substitutor.replace(str);
	}

	private void createRandomEntries(String str) {
		if (str.contains(RAND_NAME)) {
			String token = RAND_NAME;
			FirstnameGenerator nameGen;

			if (str.contains(RAND_NAME_M)) {
				nameGen = JRand.firstname().gender(Gender.MALE);
				token = RAND_NAME_M;
			} else if (str.contains(RAND_NAME_F)) {
				nameGen = JRand.firstname().gender(Gender.FEMALE);
				token = RAND_NAME_F;
			} else
				nameGen = JRand.firstname();

			add(token, nameGen.gen());
		}
	}

	public static String replace(String str, HashMap<String, String> additionalReplacements) {
		return StringSubstitutor.replace(replace(str), additionalReplacements);
	}
}
