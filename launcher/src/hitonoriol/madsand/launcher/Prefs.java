package hitonoriol.madsand.launcher;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import org.hildan.fxgson.FxGson;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class Prefs {
	public LinkedList<GameVersionEntry> gameVersions = new LinkedList<>();
	public SimpleStringProperty vmArgs = new SimpleStringProperty(GameLauncher.DEFAULT_VM_ARGS);
	public SimpleStringProperty gameArgs = new SimpleStringProperty("");
	public SimpleBooleanProperty showConsole = new SimpleBooleanProperty(false);
	public SimpleBooleanProperty autoCheckForUpdates = new SimpleBooleanProperty(true);

	private static Prefs instance;
	private static final Gson gson = FxGson.coreBuilder().setPrettyPrinting().create();
	private static final File prefsFile = new File("prefs.json");

	private Prefs() {}

	public static void load() {
		if (prefsFile.exists()) {
			try (FileReader reader = new FileReader(prefsFile)) {
				instance = gson.fromJson(new JsonReader(reader), Prefs.class);

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else
			instance = new Prefs();
	}

	public static void save() {
		try (FileWriter writer = new FileWriter(prefsFile)) {
			gson.toJson(instance, writer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Prefs values() {
		return instance;
	}
}
