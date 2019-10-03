package ru.bernarder.fallenrisefromdust.desktop;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import ru.bernarder.fallenrisefromdust.MadSand;

public class Launcher {
	public static void main(String[] args) throws Exception {
		System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream("MadSandOutput.txt")), true));
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.resizable = false;
		// config.setFromDisplayMode(LwjglApplicationConfiguration.getDesktopDisplayMode());
		config.vSyncEnabled = true;
		config.width = 1280;
		config.height = 720;
		config.foregroundFPS = 60;
		config.fullscreen = false;
		new com.badlogic.gdx.backends.lwjgl.LwjglApplication(new MadSand(), config);
	}
}
