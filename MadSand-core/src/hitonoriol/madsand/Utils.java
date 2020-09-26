package hitonoriol.madsand;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Random;

public class Utils {
	public static boolean debugMode = true;
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

	public static void out(String arg) {
		if (!debugMode)
			return;

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		System.out.print("[" + sdf.format(cal.getTime()) + "] " + arg + "\n");

	}

	public static void die(String... msg) {
		out("Seems like some fatal error occured. Check " + MadSand.ERRFILE + " for details.");
		if (msg.length > 0) {
			for (String m : msg) {
				out(m);
			}
		}
		System.exit(-1);
	}

	public static double round(double curWeight) {
		return (Math.round(curWeight * 100) / 100.00);
	}

	public static int rand(int min, int max) {
		return random.nextInt((max - min) + 1) + min;
	}

	public static int rand(int max) {
		if (max < 1)
			max = 1;

		return random.nextInt(max);
	}

	public static int randElement(ArrayList<Integer> list) {
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

	public static int val(boolean bool) {
		return bool ? 1 : 0;
	}

	public static boolean bool(int val) {
		return (val == 1);
	}
}
