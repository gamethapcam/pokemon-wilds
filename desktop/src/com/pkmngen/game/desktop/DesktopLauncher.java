package com.pkmngen.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.pkmngen.game.PkmnGen;

public class DesktopLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

        // try 160 x 144 pixels
        int multi_temp = 3; // scale viewport
        config.width = multi_temp * 160; // + 160*2;
        config.height = multi_temp * 144;

        new LwjglApplication(new PkmnGen(), config);
    }
}
