package com.pkmngen.game.util;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.pkmngen.game.Game;

/**
 * Cache textures based on filehandles.
 */
public class TextureCache {
    
    public static HashMap<FileHandle, com.badlogic.gdx.graphics.Texture> textMap = new HashMap<FileHandle, com.badlogic.gdx.graphics.Texture>();
    public static Texture currTexture;
    
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
