package com.simon.skyneuralnetusage.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.simon.skyneuralnetusage.Main;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Neural Network Usage Example";
		config.width = 426;
		config.height = 900;
		
		new LwjglApplication(new Main(), config);
	}
}
