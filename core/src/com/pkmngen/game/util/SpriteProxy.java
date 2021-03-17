package com.pkmngen.game.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.Batch;


/**
 * I guess this works.
 * Used for 'inverse color' effects, or generally effects that replace
 * sprite colors using the sprite's palette.
 * 
 * TODO: I could have just set a shader before and after the draw() call
 * instead of creating a new texture for each effect. Oh well, maybe
 * improve this in the future.
 */
public class SpriteProxy extends com.badlogic.gdx.graphics.g2d.Sprite {
    public static boolean inverseColors = false;

    // Used by some attacks, like Toxic
    // effect1 - replace second color with black
    // effect2 - replace first color with second color (and second color with black)
    // Note: this depends on first color being the lightest, and second being darker/est.
    // I checked and I think all palettes from the vgc use this convention.
    public static boolean darkenColors1 = false;
    public static boolean darkenColors2 = false;
    public static boolean darkenColors3 = false;
    // Used by confuse ray
    public static boolean confuseRayColors1 = false;
    public static boolean confuseRayColors2 = false;
    
    // Done on a per-sprite basis (TODO: same with the other stuff? not sure)
    public boolean lightenColors1 = false;
    public boolean lightenColors2 = false;
    public static boolean lightenAllColors1 = false;
    public static boolean lightenAllColors2 = false;

    // Need to be able to manually set, like in the case of health bar.
    // This is basically the palette
    public Color color1 = null;
    public Color color2 = new Color();
    public Color black = Color.BLACK;

    Texture originalTexture;
    Texture inverseTexture;
    Texture darkenTexture1;
    Texture darkenTexture2;
    Texture darkenTexture3;
    Texture lightenTexture1;
    Texture lightenTexture2;
    Texture confuseRayTexture1;
    Texture confuseRayTexture2;

    @Override
    public void draw(Batch batch) {
        Texture texture = this.getTexture();
        // Check for inverse colors effect
        if (SpriteProxy.inverseColors) {
            this.setTexture(this.inverseTexture);
            texture = this.inverseTexture;
        }
        else if (!SpriteProxy.inverseColors && texture == this.inverseTexture) {
            this.setTexture(this.originalTexture);
            texture = this.originalTexture;
        }
        // Check for darken colors effect (1)
        if (SpriteProxy.darkenColors1 && texture == this.originalTexture) {
            this.setTexture(this.darkenTexture1);
            texture = this.darkenTexture1;
        }
        else if (!SpriteProxy.darkenColors1 && texture == this.darkenTexture1) {
            this.setTexture(this.originalTexture);
            texture = this.originalTexture;
        }
        // Check for darken colors effect (2)
        if (SpriteProxy.darkenColors2 && texture == this.originalTexture) {
            this.setTexture(this.darkenTexture2);
            texture = this.darkenTexture2;
        }
        else if (!SpriteProxy.darkenColors2 && texture == this.darkenTexture2) {
            this.setTexture(this.originalTexture);
            texture = this.originalTexture;
        }
        // Check for darken colors effect (3)
        if (SpriteProxy.darkenColors3 && texture == this.originalTexture) {
            this.setTexture(this.darkenTexture3);
            texture = this.darkenTexture3;
        }
        else if (!SpriteProxy.darkenColors3 && texture == this.darkenTexture3) {
            this.setTexture(this.originalTexture);
            texture = this.originalTexture;
        }

        // Check for lighten all colors effect (1)
        if (SpriteProxy.lightenAllColors1 && texture == this.originalTexture) {
            this.setTexture(this.lightenTexture1);
            texture = this.lightenTexture1;
        }
        else if (!SpriteProxy.lightenAllColors1 && texture == this.lightenTexture1) {
            this.setTexture(this.originalTexture);
            texture = this.originalTexture;
        }
        // Check for lighten all colors effect (2)
        if (SpriteProxy.lightenAllColors2 && texture == this.originalTexture) {
            this.setTexture(this.lightenTexture2);
            texture = this.lightenTexture2;
        }
        else if (!SpriteProxy.lightenAllColors2 && texture == this.lightenTexture2) {
            this.setTexture(this.originalTexture);
            texture = this.originalTexture;
        }
        
        // Check for lighten colors effect (1)
        if (this.lightenColors1 && texture == this.originalTexture) {
            this.setTexture(this.lightenTexture1);
            texture = this.lightenTexture1;
        }
        else if (!this.lightenColors1 && !SpriteProxy.lightenAllColors1 && texture == this.lightenTexture1) {
            this.setTexture(this.originalTexture);
            texture = this.originalTexture;
        }
        // Check for lighten colors effect (2)
        if (this.lightenColors2 && texture == this.originalTexture) {
            this.setTexture(this.lightenTexture2);
            texture = this.lightenTexture2;
        }
        else if (!this.lightenColors2 && !SpriteProxy.lightenAllColors2 && texture == this.lightenTexture2) {
            this.setTexture(this.originalTexture);
            texture = this.originalTexture;
        }
        // Check for confuse ray colors effect (1)
        if (SpriteProxy.confuseRayColors1 && texture == this.originalTexture) {
            this.setTexture(this.confuseRayTexture1);
            texture = this.confuseRayTexture1;
        }
        else if (!SpriteProxy.confuseRayColors1 && texture == this.confuseRayTexture1) {
            this.setTexture(this.originalTexture);
            texture = this.originalTexture;
        }
        // Check for confuse ray colors effect (2)
        if (SpriteProxy.confuseRayColors2 && texture == this.originalTexture) {
            this.setTexture(this.confuseRayTexture2);
            texture = this.confuseRayTexture2;
        }
        else if (!SpriteProxy.confuseRayColors2 && texture == this.confuseRayTexture2) {
            this.setTexture(this.originalTexture);
            texture = this.originalTexture;
        }
        super.draw(batch);
    }

    public SpriteProxy(Texture texture, int srcX, int srcY, int srcWidth, int srcHeight) {
        this(null, texture, srcX, srcY, srcWidth, srcHeight);
    }
    
    public SpriteProxy(Color color1, Texture texture, int srcX, int srcY, int srcWidth, int srcHeight) {
        this(color1, new Color(), texture, srcX, srcY, srcWidth, srcHeight);
    }

    public SpriteProxy(Color color1, Color color2, Texture texture, int srcX, int srcY, int srcWidth, int srcHeight) {
        this(Color.BLACK, color1, new Color(), texture, srcX, srcY, srcWidth, srcHeight);
    }

    public SpriteProxy(Color black, Color color1, Color color2, Texture texture, int srcX, int srcY, int srcWidth, int srcHeight) {
        super(texture, srcX, srcY, srcWidth, srcHeight);
        this.color1 = color1;
        this.color2 = color2;  // TODO: doesn't work.
        this.black = black;
        this.originalTexture = texture;
        // TODO: can't set color1/color2 before here.

        // Save inverse color texture (and others)
        if (!TextureCache.effectsTextMap.containsKey(texture)) {
            // Save inverse, darken1, and darken2 here (for now, more may come later)
            Texture[] textures = new Texture[8];
            TextureData temp = texture.getTextureData();
            if (!temp.isPrepared()) {
                temp.prepare();
            }
            Pixmap currPixmap = temp.consumePixmap();
            Pixmap newPixmap = new Pixmap(texture.getWidth(), texture.getHeight(), Pixmap.Format.RGBA8888);
            newPixmap.setColor(new Color(0f, 0f, 0f, 0f));
            newPixmap.fill();
            Color color;
            for (int i=0, j=0; j < texture.getHeight(); i++) {
                if (i > texture.getWidth()) {
                    i=-1;
                    j++;
                    continue;
                }
                color = new Color(currPixmap.getPixel(i, j));
                if (!(color.r == 0f && color.g == 251.0f/255f && color.b == 0f) && !(color.r == 0f && color.g == 0f && color.b == 0f) && !(color.r == 1f && color.g == 1f && color.b == 1f)) {
                    if (this.color1 == null) {
                        this.color1 = color;
                        continue;
                    }
                    if (!(color.r == this.color1.r && color.g == this.color1.g && color.b == this.color1.b)) {
                        this.color2 = color;
                        break;
                    }
                }
            }
            // Swap colors so that lightest color is first (required by darken1 and darken2 effects)
            if (this.color1 != null && this.color2 != null && (this.color1.r + this.color1.g + this.color1.b) < (this.color2.r + this.color2.g + this.color2.b)) {
                Color tempColor = this.color1;
                this.color1 = this.color2;
                this.color2 = tempColor;
            }
            // cache color1 and color2
            Color[] colors = new Color[2];
            colors[0] = this.color1;
            colors[1] = this.color2;
            TextureCache.colorsTextMap.put(texture, colors);
            // Inverse colors texture
            for (int i=0, j=0; j < texture.getHeight(); i++) {
                if (i > texture.getWidth()) {
                    i=-1;
                    j++;
                    continue;
                }
                color = new Color(currPixmap.getPixel(i, j));
//                if (color.a < 1f) {
//                    // DrawEnemyHealth bgsprite has transparent area that is black.
//                    // Need it to 
////                    color = new Color(0f, 0f, 0f, 0f);
//                }
//                else 
                if (color.a == 0f) {
                    color = new Color(0f, 0f, 0f, 0f);
                }
                else if ((color.r == 0f && color.g == 0f && color.b == 0f)) {
                    color = Color.WHITE;
                }
                else if ((color.r == 1f && color.g == 1f && color.b == 1f)) {
                    color = this.black;
                }
                // Some backgrounds aren't 'true' white.
                else if ((color.r == 1f && color.g == 251.0f/255f && color.b == 1f)) {
                    color = this.black;
                }
                else if (color.r == this.color1.r && color.g == this.color1.g && color.b == this.color1.b) {
                    color = this.color2;
                }
                else if (color.r == this.color2.r && color.g == this.color2.g && color.b == this.color2.b) {
                    color = this.color1;
                }
                if (color.a != 0f) {
                    newPixmap.drawPixel(i, j, Color.rgba8888(color.r, color.g, color.b, 1f));
                }
//                if (color.a == 1f) {
//                }
//                else {
//                    newPixmap.drawPixel(i, j, Color.rgba8888(1f, 1f, 1f, 0f));
//                }
            }
            textures[0] = TextureCache.get(newPixmap);  // inverse texture
            // Save darken1 texture
            // Replace all instances of this.color2 with black
            newPixmap = new Pixmap(texture.getWidth(), texture.getHeight(), Pixmap.Format.RGBA8888);
            newPixmap.setColor(new Color(0f, 0f, 0f, 0f));
            newPixmap.fill();
            for (int i=0, j=0; j < texture.getHeight(); i++) {
                if (i > texture.getWidth()) {
                    i=-1;
                    j++;
                    continue;
                }
                color = new Color(currPixmap.getPixel(i, j));
                if (color.a == 0f) {
                    color = new Color(0f, 0f, 0f, 0f);
                }
                else if ((color.r == 0f && color.g == 0f && color.b == 0f)) {
                }
                else if ((color.r == 1f && color.g == 1f && color.b == 1f)) {
                }
                // Some backgrounds aren't 'true' white.
                else if ((color.r == 1f && color.g == 251.0f/255f && color.b == 1f)) {
                }
                else if (this.color1 != null && color.r == this.color2.r && color.g == this.color2.g && color.b == this.color2.b) {
                    color = this.black;
                }
                if (color.a != 0f) {
                    newPixmap.drawPixel(i, j, Color.rgba8888(color.r, color.g, color.b, 1f));
                }
            }
            textures[1] = TextureCache.get(newPixmap);  // darken1 texture

            // Save darken2 texture
            // Replace all instances of this.color1 with this.color2
            // Replace all instances of this.color2 with black
            newPixmap = new Pixmap(texture.getWidth(), texture.getHeight(), Pixmap.Format.RGBA8888);
            newPixmap.setColor(new Color(0f, 0f, 0f, 0f));
            newPixmap.fill();
            for (int i=0, j=0; j < texture.getHeight(); i++) {
                if (i > texture.getWidth()) {
                    i=-1;
                    j++;
                    continue;
                }
                color = new Color(currPixmap.getPixel(i, j));
                if (color.a == 0f) {
                    color = new Color(0f, 0f, 0f, 0f);
                }
                else if ((color.r == 0f && color.g == 0f && color.b == 0f)) {
                }
                else if ((color.r == 1f && color.g == 1f && color.b == 1f)) {
                }
                // Some backgrounds aren't 'true' white.
                else if ((color.r == 1f && color.g == 251.0f/255f && color.b == 1f)) {
                }
                else if (this.color1 != null && color.r == this.color2.r && color.g == this.color2.g && color.b == this.color2.b) {
                    color = this.black;
                }
                else if (this.color1 != null && color.r == this.color1.r && color.g == this.color1.g && color.b == this.color1.b) {
                    color = this.color2;
                }
                if (color.a != 0f) {
                    newPixmap.drawPixel(i, j, Color.rgba8888(color.r, color.g, color.b, 1f));
                }
            }
            textures[2] = TextureCache.get(newPixmap);  // darken2 texture

            // Save lighten1 texture
            // Replace all instances of black with this.color2
            // Replace all instances of this.color1 with white
            // Replace all instances of this.color2 with this.color1
            newPixmap = new Pixmap(texture.getWidth(), texture.getHeight(), Pixmap.Format.RGBA8888);
            newPixmap.setColor(new Color(0f, 0f, 0f, 0f));
            newPixmap.fill();
            for (int i=0, j=0; j < texture.getHeight(); i++) {
                if (i > texture.getWidth()) {
                    i=-1;
                    j++;
                    continue;
                }
                color = new Color(currPixmap.getPixel(i, j));
                if (color.a == 0f) {
                    color = new Color(0f, 0f, 0f, 0f);
                }
                else if ((color.r == 0f && color.g == 0f && color.b == 0f)) {
                    color = this.color2;
                }
                else if ((color.r == 1f && color.g == 1f && color.b == 1f)) {
                }
                // Some backgrounds aren't 'true' white.
                else if ((color.r == 1f && color.g == 251.0f/255f && color.b == 1f)) {
                }
                else if (this.color1 != null && color.r == this.color2.r && color.g == this.color2.g && color.b == this.color2.b) {
                    color = this.color1;
                }
                else if (this.color1 != null && color.r == this.color1.r && color.g == this.color1.g && color.b == this.color1.b) {
                    color = Color.WHITE;
                }
                if (color.a != 0f) {
                    newPixmap.drawPixel(i, j, Color.rgba8888(color.r, color.g, color.b, 1f));
                }
            }
            textures[3] = TextureCache.get(newPixmap);  // lighten1 texture

            // Save lighten2 texture
            // Replace all instances of black with this.color1
            // Replace all instances of this.color1 with white
            // Replace all instances of this.color2 with white
            newPixmap = new Pixmap(texture.getWidth(), texture.getHeight(), Pixmap.Format.RGBA8888);
            newPixmap.setColor(new Color(0f, 0f, 0f, 0f));
            newPixmap.fill();
            for (int i=0, j=0; j < texture.getHeight(); i++) {
                if (i > texture.getWidth()) {
                    i=-1;
                    j++;
                    continue;
                }
                color = new Color(currPixmap.getPixel(i, j));
                if (color.a == 0f) {
                    color = new Color(0f, 0f, 0f, 0f);
                }
                else if ((color.r == 0f && color.g == 0f && color.b == 0f)) {
                    color = this.color1;
                }
                else if ((color.r == 1f && color.g == 1f && color.b == 1f)) {
                }
                // Some backgrounds aren't 'true' white.
                else if ((color.r == 1f && color.g == 251.0f/255f && color.b == 1f)) {
                }
                else if (this.color1 != null && color.r == this.color2.r && color.g == this.color2.g && color.b == this.color2.b) {
                    color = Color.WHITE;
                }
                else if (this.color1 != null && color.r == this.color1.r && color.g == this.color1.g && color.b == this.color1.b) {
                    color = Color.WHITE;
                }
                if (color.a != 0f) {
                    newPixmap.drawPixel(i, j, Color.rgba8888(color.r, color.g, color.b, 1f));
                }
            }
            textures[4] = TextureCache.get(newPixmap);  // lighten2 texture

            // Save confuseRay1 texture
            // color2 same, color1->white, black->color1, white->black
            newPixmap = new Pixmap(texture.getWidth(), texture.getHeight(), Pixmap.Format.RGBA8888);
            newPixmap.setColor(new Color(0f, 0f, 0f, 0f));
            newPixmap.fill();
            for (int i=0, j=0; j < texture.getHeight(); i++) {
                if (i > texture.getWidth()) {
                    i=-1;
                    j++;
                    continue;
                }
                color = new Color(currPixmap.getPixel(i, j));
                if (color.a == 0f) {
                    color = new Color(0f, 0f, 0f, 0f);
                }
                else if ((color.r == 0f && color.g == 0f && color.b == 0f)) {
                    color = this.color1;
                }
                else if ((color.r == 1f && color.g == 1f && color.b == 1f)) {
                    color = this.black;
                }
                // Some backgrounds aren't 'true' white.
                else if ((color.r == 1f && color.g == 251.0f/255f && color.b == 1f)) {
                    color = this.black;
                }
                else if (this.color1 != null && color.r == this.color2.r && color.g == this.color2.g && color.b == this.color2.b) {
                }
                else if (this.color1 != null && color.r == this.color1.r && color.g == this.color1.g && color.b == this.color1.b) {
                    color = Color.WHITE;
                }
                if (color.a != 0f) {
                    newPixmap.drawPixel(i, j, Color.rgba8888(color.r, color.g, color.b, 1f));
                }
            }
            textures[5] = TextureCache.get(newPixmap);

            // Save confuseRay2 texture
            // color1 same, color2->white, black->color2, white->black
            newPixmap = new Pixmap(texture.getWidth(), texture.getHeight(), Pixmap.Format.RGBA8888);
            newPixmap.setColor(new Color(0f, 0f, 0f, 0f));
            newPixmap.fill();
            for (int i=0, j=0; j < texture.getHeight(); i++) {
                if (i > texture.getWidth()) {
                    i=-1;
                    j++;
                    continue;
                }
                color = new Color(currPixmap.getPixel(i, j));
                if (color.a == 0f) {
                    color = new Color(0f, 0f, 0f, 0f);
                }
                else if ((color.r == 0f && color.g == 0f && color.b == 0f)) {
                    color = this.color2;
                }
                else if ((color.r == 1f && color.g == 1f && color.b == 1f)) {
                    color = this.black;
                }
                // Some backgrounds aren't 'true' white.
                else if ((color.r == 1f && color.g == 251.0f/255f && color.b == 1f)) {
                    color = this.black;
                }
                else if (this.color1 != null && color.r == this.color2.r && color.g == this.color2.g && color.b == this.color2.b) {
                    color = Color.WHITE;
                }
                else if (this.color1 != null && color.r == this.color1.r && color.g == this.color1.g && color.b == this.color1.b) {
                }
                if (color.a != 0f) {
                    newPixmap.drawPixel(i, j, Color.rgba8888(color.r, color.g, color.b, 1f));
                }
            }
            textures[6] = TextureCache.get(newPixmap);

            // Save darken3 texture
            // everything exc white->black
            newPixmap = new Pixmap(texture.getWidth(), texture.getHeight(), Pixmap.Format.RGBA8888);
            newPixmap.setColor(new Color(0f, 0f, 0f, 0f));
            newPixmap.fill();
            for (int i=0, j=0; j < texture.getHeight(); i++) {
                if (i > texture.getWidth()) {
                    i=-1;
                    j++;
                    continue;
                }
                color = new Color(currPixmap.getPixel(i, j));
                if (color.a == 0f) {
                    color = new Color(0f, 0f, 0f, 0f);
                }
                else if ((color.r == 0f && color.g == 0f && color.b == 0f)) {
                }
                else if ((color.r == 1f && color.g == 1f && color.b == 1f)) {
                }
                // Some backgrounds aren't 'true' white.
                else if ((color.r == 1f && color.g == 251.0f/255f && color.b == 1f)) {
                }
                else if (this.color1 != null && color.r == this.color2.r && color.g == this.color2.g && color.b == this.color2.b) {
                    color = this.black;
                }
                else if (this.color1 != null && color.r == this.color1.r && color.g == this.color1.g && color.b == this.color1.b) {
                    color = this.black;
                }
                if (color.a != 0f) {
                    newPixmap.drawPixel(i, j, Color.rgba8888(color.r, color.g, color.b, 1f));
                }
            }
            textures[7] = TextureCache.get(newPixmap);  // darken3 texture

            TextureCache.effectsTextMap.put(texture, textures);
        }
        else {
            this.color1 = TextureCache.colorsTextMap.get(texture)[0];
            this.color2 = TextureCache.colorsTextMap.get(texture)[1];
        }
        this.inverseTexture = TextureCache.effectsTextMap.get(texture)[0];
        this.darkenTexture1 = TextureCache.effectsTextMap.get(texture)[1];
        this.darkenTexture2 = TextureCache.effectsTextMap.get(texture)[2];
        this.lightenTexture1 = TextureCache.effectsTextMap.get(texture)[3];
        this.lightenTexture2 = TextureCache.effectsTextMap.get(texture)[4];
        // Used by Confuse Ray attack
        this.confuseRayTexture1 = TextureCache.effectsTextMap.get(texture)[5];
        this.confuseRayTexture2 = TextureCache.effectsTextMap.get(texture)[6];
        this.darkenTexture3 = TextureCache.effectsTextMap.get(texture)[7];
        
    }
}
