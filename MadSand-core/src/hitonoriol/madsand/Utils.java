package hitonoriol.madsand;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import me.xdrop.jrand.JRand;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Random;
import java.util.StringTokenizer;

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
		out("Seems like some fatal error occured. Check " + MadSand.ERRFILE + " for details.");
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
		if (max < min) {
			Utils.out("***bruh momentum: rand max < min, returning min");
			return min;
		}

		return random.nextInt((max - min) + 1) + min;
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

	public static double randPercent() {
		return random.nextDouble() * 100.0;
	}

	public static boolean percentRoll(double percent) {

		if (percent == 0)
			return false;

		return (randPercent() < percent);
	}

	public static long now() {
		return Instant.now().getEpochSecond();
	}

	final static int MAX_SYLLABLES = 4;

	public static String randWord() {
		return JRand.word().syllables(1, MAX_SYLLABLES).capitalize().gen();
	}

	final static float S_MINUTE = 60;
	final static float S_HOUR = 3600;
	final static float S_DAY = 86400;

	public static String timeString(long seconds) {
		if (seconds < S_MINUTE)
			return seconds + " seconds";

		else if (seconds < S_HOUR)
			return round(((float) seconds / S_MINUTE)) + " minutes";

		else if (seconds < 86400)
			return round(((float) seconds / S_HOUR)) + " hours";

		else
			return round(((float) seconds / S_DAY)) + " days";
	}

	public static double log(double value, double base) {
		return Math.log(value) / Math.log(base);
	}

	public static int val(boolean bool) {
		return bool ? 1 : 0;
	}

	public static boolean bool(int val) {
		return (val == 1);
	}
}
