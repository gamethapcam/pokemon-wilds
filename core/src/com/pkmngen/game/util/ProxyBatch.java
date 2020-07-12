package com.pkmngen.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * TODO: remove if unused.
 * Supports headless mode by ignoring all calls to draw() etc when disabled.
 * Also supports 'yield' actions, ie Threaded Actions, by posting all draw()
 * calls to the Gdx thread (that way Actions can run in separate Thread).
 */
public class ProxyBatch extends SpriteBatch {
    boolean disabled = false;

    public ProxyBatch() {}

    @Override
    public void draw(final Texture texture, final float x, final float y, final float width, final float height) {
        Runnable runnable = new Runnable() {
            public void run() {
                if (!disabled) {
                    ProxyBatch.super.draw(texture, x, y, width, height);
                }
            }
        };
        Gdx.app.postRunnable(runnable);
        // TODO: don't think it needs to wait here.
    }
}
