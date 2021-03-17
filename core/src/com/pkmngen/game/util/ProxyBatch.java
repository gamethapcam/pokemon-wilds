package com.pkmngen.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.pkmngen.game.util.SpriteProxy;

/**
 * TODO: remove if unused.
 * TODO: Supports headless mode by ignoring all calls to draw() etc when disabled.
 * Also supports 'yield' actions, ie Threaded Actions, by posting all draw()
 *  calls to the Gdx thread (that way Actions can run in separate Thread).
 */
public class ProxyBatch extends SpriteBatch {
    boolean disabled = false;

    public ProxyBatch() {
        super();
    }

    public void draw(SpriteProxy sprite, float x, float y) {
        Texture texture = sprite.getTexture();
        if (SpriteProxy.inverseColors) {
            sprite.setTexture(sprite.inverseTexture);
            texture = sprite.inverseTexture;
        }
        else if (!SpriteProxy.inverseColors && texture == sprite.inverseTexture) {
            sprite.setTexture(sprite.originalTexture);
            texture = sprite.originalTexture;
        }

        // Check for darken colors effect (1)
        if (SpriteProxy.darkenColors1 && texture == sprite.originalTexture) {
            sprite.setTexture(sprite.darkenTexture1);
            texture = sprite.darkenTexture1;
        }
        else if (!SpriteProxy.darkenColors1 && texture == sprite.darkenTexture1) {
            sprite.setTexture(sprite.originalTexture);
            texture = sprite.originalTexture;
        }
        // Check for darken colors effect (2)
        if (SpriteProxy.darkenColors2 && texture == sprite.originalTexture) {
            sprite.setTexture(sprite.darkenTexture2);
            texture = sprite.darkenTexture2;
        }
        else if (!SpriteProxy.darkenColors2 && texture == sprite.darkenTexture2) {
            sprite.setTexture(sprite.originalTexture);
            texture = sprite.originalTexture;
        }
        // Check for darken colors effect (3)
        if (SpriteProxy.darkenColors3 && texture == sprite.originalTexture) {
            sprite.setTexture(sprite.darkenTexture3);
            texture = sprite.darkenTexture3;
        }
        else if (!SpriteProxy.darkenColors3 && texture == sprite.darkenTexture3) {
            sprite.setTexture(sprite.originalTexture);
            texture = sprite.originalTexture;
        }

        // Check for confuse ray colors effect (1)
        if (SpriteProxy.confuseRayColors1 && texture == sprite.originalTexture) {
            sprite.setTexture(sprite.confuseRayTexture1);
            texture = sprite.confuseRayTexture1;
        }
        else if (!SpriteProxy.confuseRayColors1 && texture == sprite.confuseRayTexture1) {
            sprite.setTexture(sprite.originalTexture);
            texture = sprite.originalTexture;
        }
        // Check for confuse ray colors effect (2)
        if (SpriteProxy.confuseRayColors2 && texture == sprite.originalTexture) {
            sprite.setTexture(sprite.confuseRayTexture2);
            texture = sprite.confuseRayTexture2;
        }
        else if (!SpriteProxy.confuseRayColors2 && texture == sprite.confuseRayTexture2) {
            sprite.setTexture(sprite.originalTexture);
            texture = sprite.originalTexture;
        }
        super.draw(sprite, x, y);
    }

//    @Override
//    public void draw(final Texture texture, final float x, final float y, final float width, final float height) {
//        Runnable runnable = new Runnable() {
//            public void run() {
//                if (!disabled) {
//                    ProxyBatch.super.draw(texture, x, y, width, height);
//                }
//            }
//        };
//        Gdx.app.postRunnable(runnable);
//        // TODO: don't think it needs to wait here.
//        
//        // Handle inverted colors effect
//    }
}
