package com.pkmngen.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.pkmngen.game.Game;

public class DesktopLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

        int scale = 3; // scale viewport
        config.width = scale * 160;
        config.height = scale * 144;

        new LwjglApplication(new Game(), config);
    }
}