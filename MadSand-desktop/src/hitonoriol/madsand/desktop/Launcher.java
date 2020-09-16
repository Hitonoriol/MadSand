package hitonoriol.madsand.desktop;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import hitonoriol.madsand.MadSand;

public class Launcher {
	public static void main(String[] args) throws Exception {
		System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream("MadSandOutput.txt")), true));
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.resizable = false;
		
		config.addIcon("icon-256.png", FileType.Internal);
        config.addIcon("icon-64.png", FileType.Internal);
        config.addIcon("icon-32.png", FileType.Internal);
        
		// config.setFromDisplayMode(LwjglApplicationConfiguration.getDesktopDisplayMode());
        
		config.vSyncEnabled = true;
		config.width = 1280;
		config.height = 720;
		config.foregroundFPS = 60;
		config.fullscreen = false;
		
		new com.badlogic.gdx.backends.lwjgl.LwjglApplication(new MadSand(), config);
	}
}
