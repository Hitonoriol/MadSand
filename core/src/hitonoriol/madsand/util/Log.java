package hitonoriol.madsand.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Log {
	private static final SimpleDateFormat accurateDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH'h'mm'm'ss's'SSS");
	public static final String OUT_FILE = String
		.format("MadSand-%s.log", accurateDateFormat.format(Calendar.getInstance().getTime()));

	public static DateFormat getAccurateDateFormat() {
		return accurateDateFormat;
	}
}
