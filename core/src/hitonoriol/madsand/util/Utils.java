package hitonoriol.madsand.util;

import static hitonoriol.madsand.resources.Resources.ERR_FILE;
import static hitonoriol.madsand.resources.Resources.LINEBREAK;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.math3.random.RandomDataGenerator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.dialog.GameTextSubstitutor;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.util.Functional.SafeRunnable;
import me.xdrop.jrand.JRand;

public class Utils {
	private static boolean printTimestamp = true;
	static NumberFormat numberFormatter = NumberFormat.getInstance(Locale.US);
	public static Random random = new Random();
	static {
		numberFormatter.setMinimumFractionDigits(0);
		numberFormatter.setRoundingMode(RoundingMode.HALF_UP);
	}

	public static void enableTimestampOutput() {
		printTimestamp = true;
	}

	public static void disableTimestampOutput() {
		printTimestamp = false;
	}

	public static String str(int val) {
		return Integer.toString(val);
	}

	public static int val(String str) {
		return Integer.parseInt(str);
	}

	public static String fileBaseName(String fileName) {
		return fileName.split("\\.", 2)[0];
	}

	public static ArrayList<Integer> parseList(String str) {
		StringTokenizer list = new StringTokenizer(str, ",");
		ArrayList<Integer> ret = new ArrayList<>();

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

	private static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");

	public static String now(SimpleDateFormat format) {
		return format.format(Calendar.getInstance().getTime());
	}

	public static void out(String arg) {
		if (Globals.silentMode)
			return;

		if (printTimestamp)
			arg = "[" + now(dateFormat) + "] " + arg;

		System.out.print(arg + "\n");
	}

	public static void out(String arg, Object... args) {
		out(String.format(arg, args));
	}

	public static void dbg(String arg) {
		if (Globals.debugMode)
			out("* " + arg);
	}

	public static void dbg(String arg, Object... args) {
		dbg(String.format(arg, args));
	}

	public static void out() {
		if (!Globals.silentMode)
			System.out.println();
	}

	public static void die(String... msg) {
		out("Seems like some fatal error has occured.");
		if (msg.length > 0) {
			for (String m : msg)
				out(m);
		}
		Gdx.app.exit();
	}

	private static final int MAX_ERRORS = 5;
	private static long ERR_INTERVAL = 200;
	private static long errors = 0, lastError = 0;

	public static void panic(String msg) {
		long timeDelta = System.currentTimeMillis() - lastError;
		lastError = System.currentTimeMillis();
		if (timeDelta > ERR_INTERVAL)
			errors -= timeDelta / ERR_INTERVAL;
		if (errors < 0)
			errors = 0;
		++errors;

		if (errors > MAX_ERRORS)
			die("Too many errors");

		Gui.drawOkDialog("Panic", "Oops, something went horribly wrong.").fillScreen()
				.newLine()
				.appendText("You can continue playing. If you notice more strange behavior, relaunch the game.")
				.newLine(2)
				.appendText("Here's some useless info (this will also be saved to " + ERR_FILE + "):")
				.newLine()
				.appendText(msg);
	}

	public static void tryTo(SafeRunnable action) {
		Functional.tryTo(action, exception -> {
			panic(ExceptionUtils.getStackTrace(exception));
			exception.printStackTrace();
		});
	}

	public static String round(double num) {
		return round(num, 2);
	}

	public static String round(double num, int n) {
		numberFormatter.setMaximumFractionDigits(n);
		return numberFormatter.format(num);
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

	public static <T> T randElement(Set<T> set) {
		return randElement(set, 0);
	}

	public static <T> T randElement(Set<T> set, int offset) {
		return set.stream()
				.skip(rand(0 + offset, set.size() - 1))
				.findFirst().orElse(null);
	}

	public static <T> T randElement(List<T> list) {
		if (list.size() == 1)
			return list.get(0);

		return list.get(random.nextInt(list.size()));
	}

	public static <T> T randElement(T arr[]) {
		return randElement(Arrays.asList(arr));
	}

	public static double randPercent(Random random) {
		return random.nextDouble() * 100.0;
	}

	public static double randPercent(RandomDataGenerator random) {
		return random.nextUniform(0, 100, true);
	}

	public static double randPercent() {
		return randPercent(random);
	}

	public static boolean percentRoll(Random random, double percent) {
		return percentRoll(randPercent(random), percent);
	}

	public static boolean percentRoll(RandomDataGenerator random, double percent) {
		return percentRoll(randPercent(random), percent);
	}

	public static boolean percentRoll(double percent) {
		return percentRoll(random, percent);
	}

	public static boolean percentRoll(double rollResult, double percent) {
		return rollResult < percent;
	}

	public static float nextFloat(RandomDataGenerator random) {
		return (float) random.nextUniform(0, 1, true);
	}

	public static Color randomColor(RandomDataGenerator random) {
		return new Color(nextFloat(random), nextFloat(random), nextFloat(random), 1);
	}

	public static long now() {
		return Instant.now().getEpochSecond();
	}

	final static int MAX_SYLLABLES = 4;

	public static String randWord() {
		return JRand.word().syllables(1, MAX_SYLLABLES).capitalize().gen();
	}

	public final static int H_DAY = 24;
	public final static int S_MINUTE = 60, M_HOUR = S_MINUTE;
	public final static int S_HOUR = 3600;
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

	public static <T> String getPackageName(Class<T> clazz) {
		String fullName = clazz.getName();
		return fullName.substring(0, fullName.lastIndexOf("."));
	}

	public static int largestDivisor(int n) {
		if (n % 2 == 0)
			return n / 2;

		final int sqrtn = (int) Math.sqrt(n);
		for (int i = 3; i <= sqrtn; i += 2) {
			if (n % i == 0)
				return n / i;
		}
		return 1;
	}

	public static double log(double value, double base) {
		if (value < 1)
			return 0;
		return Math.log(value) / Math.log(base);
	}

	public static boolean builderEquals(StringBuilder builder, String contents) {
		return builder.length() == contents.length() && builder.indexOf(contents) == 0;
	}

	public static StringBuilder clearBuilder(StringBuilder sb) {
		sb.setLength(0);
		return sb;
	}

	public static StringBuilder newLine(StringBuilder sb) {
		return sb.append(LINEBREAK);
	}

	public static int val(boolean bool) {
		return bool ? 1 : 0;
	}

	public static boolean bool(int val) {
		return (val == 1);
	}
}
