package hitonoriol.madsand;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import hitonoriol.madsand.dialog.GameTextSubstitutor;
import me.xdrop.jrand.JRand;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Random;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {
	public static boolean debugMode = false;
	public static SpriteBatch batch;

	public static Random random = new Random();

	public static void init() {
		try {
			Resources.init();
			batch = new SpriteBatch();
		} catch (Exception e) {
			die("Exception on init: " + ExceptionUtils.getStackTrace(e));
		}
	}

	public static String str(int val) {
		return Integer.toString(val);
	}

	public static int val(String str) {
		return Integer.parseInt(str);
	}

	public static ArrayList<Integer> parseList(String str) {
		StringTokenizer list = new StringTokenizer(str, ",");
		ArrayList<Integer> ret = new ArrayList<Integer>();

		while (list.hasMoreTokens())
			ret.add(val(list.nextToken()));

		return ret;
	}

	public static int oneOf(String stringList) {
		return randElement(parseList(stringList));
	}

	public static String oneOfStrings(String stringList) {
		return randElement(Stream.of(stringList.split(",", -1)).collect(Collectors.toList()));
	}

	static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

	public static void out(String arg) {
		if (!debugMode)
			return;

		System.out.print("[" + sdf.format(Calendar.getInstance().getTime()) + "] " + arg + "\n");
	}

	public static void out() {
		System.out.println();
	}

	public static void die(String... msg) {
		out("Seems like some fatal error occured. Check " + Resources.ERR_FILE + " for details.");
		if (msg.length > 0) {
			for (String m : msg)
				out(m);
		}
		System.exit(-1);
	}

	public static double round(double num) {
		return (Math.round(num * 100) / 100.00);
	}

	public static int rand(int min, int max) {
		if (max <= min)
			return min;

		return random.nextInt((max - min) + 1) + min;
	}

	public static <T extends Enum<?>> T pick(Class<T> clazz) {
		int i = random.nextInt(clazz.getEnumConstants().length);
		return clazz.getEnumConstants()[i];
	}

	public static int signRand(int min, int max) {
		int num = rand(min, max);
		return random.nextBoolean() ? num : -num;
	}

	public static int rand(int max) {
		if (max < 1)
			max = 1;

		return random.nextInt(max);
	}

	public static <T> T randElement(List<T> list) {
		if (list.size() == 1)
			return list.get(0);

		return list.get(random.nextInt(list.size()));
	}

	public static <T> T randElement(T arr[]) {
		return randElement(Arrays.asList(arr));
	}

	public static double randPercent() {
		return random.nextDouble() * 100.0;
	}

	public static boolean percentRoll(double percent) {

		if (percent == 0)
			return false;

		return (randPercent() < percent);
	}

	public static boolean percentRoll(double rollResult, double percent) {
		return rollResult < percent;
	}

	public static long now() {
		return Instant.now().getEpochSecond();
	}

	final static int MAX_SYLLABLES = 4;

	public static String randWord() {
		return JRand.word().syllables(1, MAX_SYLLABLES).capitalize().gen();
	}

	final static int S_MINUTE = 60;
	final static int S_HOUR = 3600;
	final static int S_DAY = 86400;
	final static String TIME_DELIM = ":";

	public static String timeString(long seconds, boolean verbose) {
		int hours = (int) (seconds / S_HOUR);
		int minutes = (int) ((seconds % S_HOUR) / S_MINUTE);
		int secs = (int) (seconds % S_MINUTE);

		if (!verbose)
			return hours + TIME_DELIM + minutes + TIME_DELIM + secs;

		String time = "";
		if (minutes > 0)
			time = minutes + " minutes ";
		if (hours > 0)
			time = hours + " hours " + time;
		if (secs > 0 || time.length() == 0)
			time += secs + " seconds";

		return time.trim();
	}

	public static String timeString(long seconds) {
		return timeString(seconds, true);
	}

	public static String subsName(String varName) {
		return GameTextSubstitutor.DELIM_L + varName + GameTextSubstitutor.DELIM_R;
	}

	public static double log(double value, double base) {
		if (value < 1)
			return 0;
		return Math.log(value) / Math.log(base);
	}

	public static int val(boolean bool) {
		return bool ? 1 : 0;
	}

	public static boolean bool(int val) {
		return (val == 1);
	}
}
