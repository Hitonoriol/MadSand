package hitonoriol.madsand.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;

public class Main {
	static final String VERSION = Main.class.getPackage().getImplementationVersion();
	static Launcher launcher = new Launcher();

	public static void main(String[] args) {
		launcher.checkForUpdates();
	}

	public static String readLine(String name) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(name));
			String line = br.readLine();
			br.close();
			return line;
		} catch (Exception e) {
			e.printStackTrace();
			return "error";
		}
	}

	public static void writeFile(File file, String text) {
		try {
			PrintWriter pw = new PrintWriter(file);
			pw.print(text);
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
