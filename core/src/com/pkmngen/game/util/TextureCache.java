package com.pkmngen.game.util;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pkmngen.game.Game;


/**
 * Cache textures based on filehandles.
 * 
 * Also, if a thread outside the gdx thread wants to initialize a texture,
 * this will post the initialization (ie new Texture(...)) to the gdx
 * thread if that thread uses TextureCache.get(<texture info>).
 */
public class TextureCache {
    public static HashMap<FileHandle, com.badlogic.gdx.graphics.Texture> textMap = new HashMap<FileHandle, com.badlogic.gdx.graphics.Texture>();
//    // Textures with their colors inverted. Used for effects requiring inverse colors.
    public static HashMap<Texture, com.badlogic.gdx.graphics.Texture[]> effectsTextMap = new HashMap<Texture, com.badlogic.gdx.graphics.Texture[]>();
    public static HashMap<Texture, Color[]> colorsTextMap = new HashMap<Texture, Color[]>();
    public static Texture currTexture;

    public static HashMap<String, com.badlogic.gdx.graphics.Texture> eggTextures = new HashMap<String, com.badlogic.gdx.graphics.Texture>();
    public static SpriteProxy maleSymbol;
    public static SpriteProxy femaleSymbol;
    static {
        Texture text = new Texture(Gdx.files.internal("male_symbol1.png"));
        TextureCache.maleSymbol = new SpriteProxy(Color.WHITE, text, 0, 0, 8, 8);
        text = new Texture(Gdx.files.internal("female_symbol1.png"));
        TextureCache.femaleSymbol = new SpriteProxy(Color.WHITE, text, 0, 0, 8, 8);
//        System.out.println(TextureCache.femaleSymbol.color1);
//        System.out.println(TextureCache.femaleSymbol.color2);
    }

    
    /**
     * If this is being called from outside the main thread, post the operation (new Texture())
     * to the main thread and use that. Used when loading the map in a separate thread.
     */
    public static com.badlogic.gdx.graphics.Texture get(final Pixmap pixmap) {
        if (Thread.currentThread() != Game.staticGame.gameThread) {
            Runnable runnable = new Runnable() {
                public void run() {
                    try {
                        TextureCache.currTexture = new Texture(pixmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    synchronized (this) {
                        this.notify();
                    }
                }
            };
            Gdx.app.postRunnable(runnable);
            try {
                synchronized (runnable) {
                    runnable.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else {
            TextureCache.currTexture = new Texture(pixmap);
        }
        return TextureCache.currTexture;
    }

    public static com.badlogic.gdx.graphics.Texture get(final FileHandle file) {
        if (!TextureCache.textMap.containsKey(file)) {
            if (Thread.currentThread() != Game.staticGame.gameThread) {
                Runnable runnable = new Runnable() {
                    public void run() {
                        try {
                            TextureCache.textMap.put(file, new com.badlogic.gdx.graphics.Texture(file));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        synchronized (this) {
                            this.notify();
                        }
                    }
                };
                Gdx.app.postRunnable(runnable);
                try {
                    synchronized (runnable) {
                        runnable.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else {
                TextureCache.textMap.put(file, new com.badlogic.gdx.graphics.Texture(file));
            }
        }
        return TextureCache.textMap.get(file);
    }
}
