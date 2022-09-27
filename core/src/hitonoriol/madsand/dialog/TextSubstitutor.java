package hitonoriol.madsand.dialog;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.apache.commons.text.StringSubstitutor;

import hitonoriol.madsand.Enumerable;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.gamecontent.Items;
import hitonoriol.madsand.gamecontent.Objects;
import hitonoriol.madsand.gamecontent.Tiles;
import hitonoriol.madsand.lua.Lua;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.util.Strings;
import hitonoriol.madsand.util.Utils;
import me.xdrop.jrand.JRand;
import me.xdrop.jrand.model.person.Gender;

public class TextSubstitutor {
	public static final String L = "{", R = "}";

	static final String RAND_NAME = "RANDOM_NAME";
	static final String RAND_NAME_M = "RANDOM_NAME_M", RAND_NAME_F = "RANDOM_NAME_F";

	public static final String QUEST_ITEM_REWARD = "ITEM_REWARD", QUEST_EXP_REWARD = "EXP_REWARD";
	public static final String QUEST_ITEM_OBJECTIVE = "ITEM_OBJECTIVE", QUEST_KILL_OBJECTIVE = "KILL_OBJECTIVE";
	public static final String LINEBREAK = "br", TAB = "TAB";
	private static Pattern luaPattern = Pattern.compile("\\$\\((.*)\\)"); // $(lua_expression)
	private static Pattern idPattern = Pattern.compile("\\@\\((item|object|tile)\\:(.*?)\\)"); // @(<item/object/tile>:partial_name_str)

	public static final String INDENT_FLAG = "--indent";

	private Map<String, String> values = new HashMap<>();
	private Map<String, Supplier<String>> dynamicMap = new HashMap<>();
	private StringSubstitutor substitutor = new StringSubstitutor(values, L, R);

	private static TextSubstitutor instance = new TextSubstitutor();
	static {
		add(LINEBREAK, Resources.LINEBREAK);
		add(TAB, Resources.Tab);
		add("PAR", Resources.LINEBREAK + Resources.Tab);
		add("PLAYER", () -> MadSand.player().getName());
		add(RAND_NAME, () -> JRand.firstname().gen());
		add(RAND_NAME_M, () -> JRand.firstname().gender(Gender.MALE).gen());
		add(RAND_NAME_F, () -> JRand.firstname().gender(Gender.FEMALE).gen());
	}

	private TextSubstitutor() {}

	public static void add(String var, String subText) {
		Utils.dbg("Substitutor: Setting " + L + var + R + " to \"" + subText + "\"");
		instance.values.put(var, subText);
	}

	public static void add(String var, Supplier<String> subSupplier) {
		instance.dynamicMap.put(L + var + R, subSupplier);
	}

	public static String replace(String str) {
		StringBuilder sb = new StringBuilder(str);
		replaceDynamic(sb);
		parsePatterns(sb);
		applyFlags(sb);
		return instance.substitutor.replace(sb);
	}

	private static boolean flagExists(StringBuilder sb, String flag) {
		int idx = sb.indexOf(flag);
		if (idx == -1)
			return false;
		sb.delete(idx, idx + flag.length());
		return true;
	}

	private static void applyFlags(StringBuilder sb) {
		if (flagExists(sb, INDENT_FLAG)) {
			for (int idx = sb.indexOf(Resources.LINEBREAK); idx != -1; idx = sb.indexOf(Resources.LINEBREAK, idx + 1))
				sb.insert(idx + 1, Resources.Tab);
		}
	}

	private static void replaceDynamic(StringBuilder sb) {
		instance.dynamicMap.forEach((var, supplier) -> {
			int start;
			while ((start = sb.indexOf(var)) != -1) {
				sb.replace(start, start + var.length(), supplier.get());
			}
		});
	}

	private static void parsePatterns(StringBuilder sb) {
		/* Replace lua expressions with their evaluation result */
		Strings.parseRegex(sb, luaPattern, matcher -> {
			String exprResult = Lua.execute("return " + matcher.group(1) + ";").tojstring();
			sb.replace(matcher.start(), matcher.end(), exprResult);
		});

		/* Replace Item / MapObject / Tile names with their id's */
		Strings.parseRegex(sb, idPattern, matcher -> {
			Map<Integer, ? extends Enumerable> map;
			String type = matcher.group(1);
			if (type.equals("item"))
				map = Items.all().get();
			else if (type.equals("object"))
				map = Objects.all().get();
			else
				map = Tiles.all().get();

			sb.replace(matcher.start(), matcher.end(), Utils.str(Enumerable.findId(map, matcher.group(2))));
		});
	}
}
