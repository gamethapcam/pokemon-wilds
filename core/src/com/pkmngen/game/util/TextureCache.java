package com.pkmngen.game.util;

import java.util.HashMap;

import com.badlogic.gdx.files.FileHandle;

/*
 * Cache textures based on filehandles.
 */
public class TextureCache {
    
    public static HashMap<FileHandle, com.badlogic.gdx.graphics.Texture> textMap = new HashMap<FileHandle, com.badlogic.gdx.graphics.Texture>();

    public static com.badlogic.gdx.graphics.Texture get(FileHandle file) {
        if (!TextureCache.textMap.containsKey(file)) {
            TextureCache.textMap.put(file, new com.badlogic.gdx.graphics.Texture(file));
        }
        return TextureCache.textMap.get(file);
    }
}
