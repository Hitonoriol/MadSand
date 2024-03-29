package hitonoriol.madsand.util;

import static java.lang.System.getProperty;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.math.RoundingMode;
import java.text.DateFormat;
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

import hitonoriol.madsand.dialog.TextSubstitutor;
import hitonoriol.madsand.gamecontent.Globals;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.util.Functional.SafeRunnable;
import me.xdrop.jrand.JRand;

public class Utils {
	private static boolean printTimestamp = true;
	static NumberFormat numberFormatter = NumberFormat.getInstance(Locale.US);
	public static final Random random = new Random();
	public static final RandomDataGenerator dataGen = new RandomDataGenerator();
	static {
		numberFormatter.setMinimumFractionDigits(0);
		numberFormatter.setRoundingMode(RoundingMode.HALF_UP);
	}

	public static String colorizeText(String text, Color color) {
		var str = color.toString();
		return "[#" + str.substring(0, str.length() - 2) + "]" + text + "[]";
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
		var list = new StringTokenizer(str, ",");
		var ret = new ArrayList<Integer>();

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

	public static String now(DateFormat format) {
		return format.format(Calendar.getInstance().getTime());
	}

	public static double toSeconds(long millis) {
		return millis / 1000d;
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

	public static void die(Throwable e) {
		Gui.doLater(Gdx.app::exit); // Schedule an exit in case if the exception is caught
		throw new RuntimeException(e);
	}

	public static void die(String msg) {
		die(new Exception(msg));
	}

	public static void die() {
		die("Seems like some fatal error has occured.");
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
			.appendText("Here's some useless info (this will also be saved to " + Log.OUT_FILE + "):")
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

		var time = "";
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
		return TextSubstitutor.L + varName + TextSubstitutor.R;
	}

	public static <T> String getPackageName(Class<T> clazz) {
		var fullName = clazz.getName();
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

	public static int val(boolean bool) {
		return bool ? 1 : 0;
	}

	public static boolean bool(int val) {
		return (val == 1);
	}

	private static double toMB(double bytes) {
		return bytes / 0x100000;
	}

	private static double toMB(MemoryUsage usage) {
		return toMB(usage.getUsed());
	}

	public static String memoryUsageString() {
		var runtime = Runtime.getRuntime();
		double nonHeapMem = toMB(ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage());
		double totalMem = toMB(runtime.totalMemory()), freeMem = toMB(runtime.freeMemory());
		double memInUse = totalMem - freeMem;
		return String.format(
			"Memory allocated: %.2fMB (%.2fMB used), off-heap: %.2fMB",
			totalMem, memInUse, nonHeapMem
		);
	}

	public static void printMemoryInfo() {
		out(memoryUsageString());
	}

	public static void printSystemInfo() {
		out(
			"Using Java %s on %s (%s)",
			getProperty("java.version"), getProperty("os.name"), getProperty("os.arch")
		);
	}
}
