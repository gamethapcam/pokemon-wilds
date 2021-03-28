package com.pkmngen.game.desktop;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.pkmngen.game.Game;

public class DesktopLauncher {
    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        int scale = 3;  // Scale viewport
        config.width = scale * 160;
        config.height = scale * 144;
        config.title = "Pokémon Wilds";
        config.addIcon("icon-256.png", FileType.Internal);
        config.addIcon("icon-64.png", FileType.Internal);
        config.addIcon("icon-32.png", FileType.Internal);
        new LwjglApplication(new Game(new String[] {"dev"}), config);
    }
}