package ru.bernarder.fallenrisefromdust.desktop;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import ru.bernarder.fallenrisefromdust.Gui;
import ru.bernarder.fallenrisefromdust.MadSand;

public class Launcher {
	public static void main(String[] args) throws Exception {
		if (args[0].equals("fromUpdater")) {
			System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream("MadSandOutput.txt")), true));
			int radius = 13, wsize = 100;
			if (new File("MadSand_Saves/lastrend.dat").exists())
				radius = (Integer.parseInt(Gui.getExternal("lastrend.dat")));
			if (new File("MadSand_Saves/lastworld.dat").exists())
				wsize = (Integer.parseInt(Gui.getExternal("lastworld.dat")));
			MadSand.setParams(radius, wsize);
			MadSand.setWorldName("My world");
			MadSand.setTime(60);
			MadSand.setName("Charlie");
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
}
