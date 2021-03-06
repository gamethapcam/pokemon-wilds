package com.pkmngen.game;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;
import com.esotericsoftware.kryo.io.Output;
import com.pkmngen.game.Player.CaughtFishAnim;
import com.pkmngen.game.Player.Craft;
import com.pkmngen.game.Player.Emote;
import com.pkmngen.game.Player.Flying;
import com.pkmngen.game.Pokemon.Generation;
import com.pkmngen.game.SpecialBattleMewtwo.RocksEffect1;
import com.pkmngen.game.SpecialBattleMewtwo.RocksEffect2;
import com.pkmngen.game.util.Direction;
import com.pkmngen.game.util.LinkedMusic;
import com.pkmngen.game.util.SpriteProxy;
import com.pkmngen.game.util.TextureCache;

//import box2dLight.PointLight;

// grass -
// part of player's head is above grass,
// part is beneath
// just use two draw actions
// likely need to mark tile as 'grass',
// for wild encounters (not positive tho)
class DrawMap extends Action {
    public int layer = 140;
    Sprite blankSprite;
    Sprite belowBridgeSprite;
    Pixmap pixels;
    Texture texture;
    Vector3 startPos;
    Vector3 endPos;
    Vector3 worldCoordsTL;
    Vector3 worldCoordsBR;
    Tile tile;
    Sprite spritePart;
    Sprite zSprite;
    int zsTimer = 0;
    float done1 = 0f;
    float done2 = 0f;
    float done3 = 0f;

    public DrawMap(Game game) {
        this.pixels = new Pixmap(Gdx.files.internal("tiles/blank2.png"));
        this.texture = TextureCache.get(this.pixels);
        this.blankSprite = new Sprite(TextureCache.get(Gdx.files.internal("tiles/blank2.png")), 0, 0, 16, 16);
        this.zSprite = new Sprite(TextureCache.get(Gdx.files.internal("tiles/zs1.png")), 0, 0, 16, 16);
        this.belowBridgeSprite = new Sprite(TextureCache.get(Gdx.files.internal("tiles/bridge1_lower.png")), 0, 0, 16, 16);
    }

    public int getLayer() {
        return this.layer;
    }

    @Override
    public void step(Game game) {
        
        // Draw pending tiles to the minimap
        // Adjust i < val depending on framerate hit.
        for (int i=0; !game.map.minimapQueue.isEmpty() && i < 2; i++) {
//        if (!game.map.minimapQueue.isEmpty()) {
            game.map.minimapQueue.remove(0).updateMiniMap(game);
        }

        // TODO: if player is in battle, don't do any of this

        // TODO: debug, remove
//        if (Gdx.input.isKeyPressed(Input.Keys.P)) {
//            System.out.println("Camera unproject");
//            System.out.println(java.time.LocalTime.now());
//        }
//        System.out.println(game.currScreen.x);
//        System.out.println(game.currScreen.y);

        // draw every sprite in the map
//        for (Tile tile : new ArrayList<Tile>(game.map.tiles.values())) {
        // TODO: this screws up when screen resizes
        // trying to draw tl to br of screen, so that sprites layer on top of each other
        worldCoordsTL = game.cam.unproject(new Vector3(-256, 0, 0f));
        worldCoordsBR = game.cam.unproject(new Vector3(game.currScreen.x, game.currScreen.y+128+64, 0f));
        worldCoordsTL.x = (int)worldCoordsTL.x - (int)worldCoordsTL.x % 16;
        worldCoordsTL.y = (int)worldCoordsTL.y - (int)worldCoordsTL.y % 16;
        worldCoordsBR.x = (int)worldCoordsBR.x - (int)worldCoordsBR.x % 16;
        worldCoordsBR.y = (int)worldCoordsBR.y - (int)worldCoordsBR.y % 16;
//        System.out.println(game.viewport.getScreenWidth());
//        System.out.println(game.viewport.getScreenHeight());
        this.startPos = worldCoordsTL; // new Vector3(worldCoordsTL.x, worldCoordsTL.y, 0f);
        this.endPos = worldCoordsBR; // new Vector3(worldCoordsBR.x, worldCoordsBR.y, 0f);

//        int numTiles = 0;  // TODO: remove
        // TODO: debug, remove
//        if (Gdx.input.isKeyPressed(Input.Keys.P)) {
//            System.out.println("Start draw tiles");
//            System.out.println(java.time.LocalTime.now());
//        }
        // Used for triggering egg sound effects
        this.done1 = 0f;
        this.done2 = 0f;
        this.done3 = 0f;

        for (Vector2 currPos = new Vector2(startPos.x, startPos.y); currPos.y > endPos.y;) {
            tile = game.map.tiles.get(currPos);
            currPos.x += 16;
            if (currPos.x > endPos.x) {
                currPos.x = startPos.x;
                currPos.y -= 16;
            }
            if (tile == null) {
                continue;
            }
            // debug, remove
//            numTiles++;
//          for (Tile tile : game.map.tiles.values()) {
//            // Don't draw sprites if not in camera view
//            // note - when zoomed out, game will lag
            if (!game.cam.frustum.pointInFrustum(tile.position.x, tile.position.y-32, game.cam.position.z) &&
                    !game.cam.frustum.pointInFrustum(tile.position.x+tile.sprite.getWidth(), tile.position.y+tile.sprite.getHeight()+64, game.cam.position.z) &&
                    !game.cam.frustum.pointInFrustum(tile.position.x+tile.sprite.getWidth(), tile.position.y-32, game.cam.position.z) &&
                    !game.cam.frustum.pointInFrustum(tile.position.x, tile.position.y+tile.sprite.getHeight()+64, game.cam.position.z)) {
               continue;
            }

            // Attempt to cull pokemon draw actions
            // TODO: just have a Map.DrawLower() that will draw all lowers of game.pokemon, player etc
            if (game.map.pokemon.containsKey(tile.position)) {
                game.map.pokemon.get(tile.position).drawThisFrame = true;
            }

            // TODO: this attempted to create one bg sprite out of map tiles, couldn't get pixmap resizing to work (ie it just
            // remained 16x16 at all times
            // removing from map tiles removed collision data
            // if I were to do this in the future, just put this login in the Tile() constructor
            // draw to game.map.bgsprite on construction.
//            Texture text = tile.sprite.getTexture();
//            if (!text.getTextureData().isPrepared()) {
//                text.getTextureData().prepare();
//            }
//            Pixmap pixmap = text.getTextureData().consumePixmap();
//            this.pixels.drawPixmap(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight(),
//                                   (int)tile.position.x, (int)tile.position.y,
//                                   (int)tile.sprite.getWidth(), (int)tile.sprite.getHeight());
////            this.texture.draw(pixmap, (int)tile.position.x, (int)tile.position.y);
//
//            if (tile.overSprite != null) {
//                text = tile.overSprite.getTexture();
//                if (!text.getTextureData().isPrepared()) {
//                    text.getTextureData().prepare();
//                }
//                pixmap = text.getTextureData().consumePixmap();
////                this.texture.draw(pixmap, (int)tile.position.x, (int)tile.position.y);
//                this.pixels.drawPixmap(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight(),
//                                       (int)tile.position.x, (int)tile.position.y,
//                                       (int)tile.sprite.getWidth(), (int)tile.sprite.getHeight());
//            }
//            game.map.tiles.remove(tile.position);
//            this.texture = TextureCache.get(this.pixels);

            if (tile.sprite instanceof SpriteProxy) {
                game.mapBatch.draw((SpriteProxy)tile.sprite, tile.sprite.getX(), tile.sprite.getY());
            }
            else {
                game.mapBatch.draw(tile.sprite, tile.sprite.getX(), tile.sprite.getY());
            }

            if (tile.drawUpperBelowPlayer) {
                game.mapBatch.draw(tile.overSprite, tile.overSprite.getX(), tile.overSprite.getY());
            }
            else if (tile.belowBridge) {
                game.mapBatch.draw(this.belowBridgeSprite, tile.position.x, tile.position.y);
            }

            // Play the closest/loudest egg hop sound effects
            if (game.playerCanMove &&
                game.map.pokemon.containsKey(tile.position)) {
//                game.map.pokemon.get(currPos).name.equals("egg") &&  // TODO: remove
//                game.map.pokemon.get(currPos).mapTiles == game.map.tiles) {
                Pokemon pokemon = game.map.pokemon.get(tile.position);
                // TODO: doing in drawmap
                // Play sounds for nearby pokemon eggs
                if (pokemon.standingAction != null &&
                    pokemon.isEgg &&
                    pokemon.mapTiles == game.map.tiles &&
                    Pokemon.Standing.class.isInstance(pokemon.standingAction)) {
                    Pokemon.Standing standingAction = ((Pokemon.Standing)pokemon.standingAction);
                    // As egg gets farther away, decrease the volume of the sound effect.
                    float volume = 1f - (tile.position.dst2(game.player.position.x, game.player.position.y)/52480f);
                    if (standingAction.danceCounter == 0 && volume > this.done1) {
//                        game.insertAction(new PlaySound("ledge2", 1f - (dist/52480f), null));
                        done1 = volume;
                    }
                    else if (standingAction.danceCounter == 16 && volume > this.done2) {
//                        game.insertAction(new PlaySound("ledge2", 1f - (dist/52480f), null));
                        done2 = volume;
                    }
                    else if (standingAction.danceCounter == 70 +20 && volume > this.done3) {
//                        game.insertAction(new PlaySound("headbutt1", 1f - (dist/52480f), null));
                        done3 = volume;
                    }
                }
            }
            // tile.sprite.draw(game.batch);

//            // oversprite is often ledges
//            if (tile.overSprite != null) {
//                game.mapBatch.draw(tile.overSprite, tile.overSprite.getX(), tile.overSprite.getY());
//                // tile.overSprite.draw(game.batch); // doesn't allow
//                // coloring via batch // TODO - remove
//            }

            /*
             * I regret everything, shouldn't have tried to do this at all.
             * TODO: remove if unused.
             */
//            // if this is same position as player, draw player
//            if (game.player.position.y >= currPos.y-16 && game.player.position.y < currPos.y+16 && game.player.position.x >= currPos.x-32 && game.player.position.x < currPos.x) {
//                // draw player lower
//                if (!game.player.isSleeping) {
//                    this.spritePart = new Sprite(game.player.currSprite);
//                    this.spritePart.setRegionY(8);
//                    this.spritePart.setRegionHeight(8);
//                    game.batch.draw(this.spritePart, game.player.position.x, game.player.position.y+4);
//                }
//                // draw grass over lower part of player
//                if (tile.attrs.get("grass") == true) {
//                    game.batch.draw(tile.overSprite, tile.sprite.getX(), tile.sprite.getY());
//                }
//                // TODO: shouldn't be doing this here, need to refactor map draw action
//                if (tile.nameUpper.contains("sleeping_bag")) {
//                    tile.overSprite.draw(game.batch);
//                }
//
//                // draw player upper
//                if (game.player.isSleeping) {
//                    if (this.zsTimer < 64) {
//                        this.zSprite.setPosition(game.player.position.x+8, game.player.position.y+18);
//                    }
//                    else {
//                        this.zSprite.setPosition(game.player.position.x+16, game.player.position.y+18);
//                    }
//                    this.zsTimer++;
//                    if (this.zsTimer >= 128) {
//                        this.zsTimer = 0;
//                    }
//                    this.zSprite.draw(game.batch);
//                }
//                else {
//                    // TODO: this is broken
//                    // draw building tile if building
//                    if (game.player.isBuilding) {
//                        // get direction facing
//                        Vector2 pos = new Vector2(0,0);
//                        if (game.player.dirFacing == "right") {
//                            pos = new Vector2(game.player.position.cpy().add(16,0));
//                        }
//                        else if (game.player.dirFacing == "left") {
//                            pos = new Vector2(game.player.position.cpy().add(-16,0));
//                        }
//                        else if (game.player.dirFacing == "up") {
//                            pos = new Vector2(game.player.position.cpy().add(0,16));
//                        }
//                        else if (game.player.dirFacing == "down") {
//                            pos = new Vector2(game.player.position.cpy().add(0,-16));
//                        }
//                        // get game.player.currBuildTile and draw it at position
//                        Sprite sprite = new Sprite(game.player.currBuildTile.sprite);
//                        sprite.setAlpha(.8f);
//                        sprite.setPosition(pos.x, pos.y);
//                        Tile nextTile = game.map.tiles.get(pos);
//                        if (nextTile != null && nextTile.attrs.get("solid")) {
//                            sprite.setColor(1f, .7f, .7f, .8f);
//                        }
//                        boolean requirementsMet = true;
//                        for (String reqName : game.player.buildTileRequirements.get(game.player.currBuildTile.name).keySet()) {
//                            if (!game.player.itemsDict.containsKey(reqName)) {
//                                requirementsMet = false;
//                                break;
//                            }
//                            int playerOwns = game.player.itemsDict.get(reqName);
//                            if (playerOwns < game.player.buildTileRequirements.get(game.player.currBuildTile.name).get(reqName)) {
//                                requirementsMet = false;
//                                break;
//                            }
//                        }
//                        if (!requirementsMet) {
//                            sprite.setColor(1f, .7f, .7f, .8f);
//                        }
//                        sprite.draw(game.batch);
//                        if (game.player.currBuildTile.overSprite != null) {
//                            sprite = new Sprite(game.player.currBuildTile.overSprite);
//                            sprite.setAlpha(.8f);
//                            sprite.setPosition(pos.x, pos.y);
//                            if (nextTile != null && nextTile.attrs.get("solid")) {
//                                sprite.setColor(1f, .7f, .7f, .8f);
//                            }
//                            sprite.draw(game.batch);
//                        }
//                    }
//                    this.spritePart = new Sprite(game.player.currSprite);
//                    this.spritePart.setRegionY(0);
//                    this.spritePart.setRegionHeight(8);
//                    game.batch.draw(this.spritePart, game.player.position.x, game.player.position.y+12);
//                    game.player.currSprite.setPosition(game.player.position.x, game.player.position.y);  // this needs to be set to detect collision
//                }
//            }
        }
        // Egg hop sound effects
        if (this.done1 > 0f) {
            game.insertAction(new PlaySound("ledge2", this.done1, null));
        }
        if (this.done2 > 0f) {
            game.insertAction(new PlaySound("ledge2", this.done2, null));
        }
        if (this.done3 > 0f) {
            game.insertAction(new PlaySound("headbutt1", this.done3, null));
        }
        // Draw other players
        for (Player player : game.players.values()) {
            if (player.network.tiles != game.map.tiles) {
                continue;
            }
            if (player.isSleeping) {
                player.zSprite.draw(game.mapBatch);
                game.mapBatch.draw(player.sleepingSprite, player.position.x, player.position.y);
            }
            else {
                // TODO: could check for player in frustum, not checking for now
                player.currSprite.setPosition(player.position.x, player.position.y+4);
//                game.batch.draw(player.currSprite, player.position.x, player.position.y);
                player.currSprite.draw(game.mapBatch);
            }
        }

        // TODO: debug, remove
//        if (Gdx.input.isKeyPressed(Input.Keys.P)) {
//            System.out.println("Drew tiles");
//            System.out.println(numTiles);
//            System.out.println(java.time.LocalTime.now());
//        }
//        game.batch.draw(this.texture, 5, 5);
    }
}

// action is separate because grass in on different layer
class DrawMapGrass extends Action {
    public int layer = 120;
    Sprite blankSprite;
    Pixmap pixels;
    Texture texture;
    Vector3 startPos;
    Vector3 endPos;
    Vector3 worldCoordsTL;
    Vector3 worldCoordsBR;
    Sprite fossilSprite;
    Tile tile;

    public DrawMapGrass(Game game) {
//        this.fossilSprite = new Sprite(TextureCache.get(Gdx.files.internal("tiles/fossil2.png")), 0, 0, 16, 16);
        this.fossilSprite = new Sprite(TextureCache.get(Gdx.files.internal("tiles/fossil1.png")), 0, 0, 16, 16);
    }

    public int getLayer() {
        return this.layer;
    }

    @Override
    public void step(Game game) {
        worldCoordsTL = game.cam.unproject(new Vector3(-128, 0, 0f));
        worldCoordsBR = game.cam.unproject(new Vector3(game.currScreen.x, game.currScreen.y+128, 0f));
        worldCoordsTL.x = (int)worldCoordsTL.x - (int)worldCoordsTL.x % 16;
        worldCoordsTL.y = (int)worldCoordsTL.y - (int)worldCoordsTL.y % 16;
        worldCoordsBR.x = (int)worldCoordsBR.x - (int)worldCoordsBR.x % 16;
        worldCoordsBR.y = (int)worldCoordsBR.y - (int)worldCoordsBR.y % 16;
        this.startPos = worldCoordsTL; // new Vector3(worldCoordsTL.x, worldCoordsTL.y, 0f);
        this.endPos = worldCoordsBR; // new Vector3(worldCoordsBR.x, worldCoordsBR.y, 0f);

        // TODO - bug i can't fix where grass sprites lag when moving

        // game.cam.update(); // doesn't work
        // game.batch.setProjectionMatrix(game.cam.combined);

        // draw every sprite in the map
//        for (Tile tile : game.map.tiles.values()) {
        for (Vector2 currPos = new Vector2(startPos.x, startPos.y); currPos.y > endPos.y;) {
            tile = game.map.tiles.get(currPos);
            currPos.x += 16;
            if (currPos.x > endPos.x) {
                currPos.x = startPos.x;
                currPos.y -= 16;
            }
            if (tile == null) {
                continue;
            }
            // Don't draw sprites if not in camera view
            // note - when zoomed out, game will lag
            if (!game.cam.frustum.pointInFrustum(tile.position.x, tile.position.y, game.cam.position.z) &&
                    !game.cam.frustum.pointInFrustum(tile.position.x+tile.sprite.getWidth()+16, tile.position.y+tile.sprite.getHeight()+16, game.cam.position.z) &&
                    !game.cam.frustum.pointInFrustum(tile.position.x+tile.sprite.getWidth()+16, tile.position.y, game.cam.position.z) &&
                    !game.cam.frustum.pointInFrustum(tile.position.x, tile.position.y+tile.sprite.getHeight()+16, game.cam.position.z)) {
               continue;
            }
            if (tile.attrs.get("grass") && tile.overSprite != null) {
                // tile.sprite.draw(game.batch); doesn't allow coloring
                game.mapBatch.draw(tile.overSprite, tile.sprite.getX(), tile.sprite.getY());
            }

            // TODO: shouldn't be doing this here, need to refactor map draw action
            if (tile.nameUpper.contains("sleeping_bag")) {
                tile.overSprite.draw(game.mapBatch);
            }

            if (tile.overSprite != null && !tile.drawUpperBelowPlayer) {
                if (tile.nameUpper.equals("campfire1") && game.mapBatch.getColor().r < 0.2f) {
                    Color tempColor = game.mapBatch.getColor();
                    game.mapBatch.setColor(new Color(0.2f, 0.2f, 0.2f, 1f));
                    game.mapBatch.draw(tile.overSprite, tile.overSprite.getX(), tile.overSprite.getY());
                    game.mapBatch.setColor(tempColor);
                }
                else {
                    game.mapBatch.draw(tile.overSprite, tile.overSprite.getX(), tile.overSprite.getY());
                }
            }

            if (tile.hasItem != null) {
                
                if (tile.hasItem.contains("fossil") || tile.hasItem.equals("old amber")) {
                    if (tile.hasItem.equals("helix fossil")) {
                        this.fossilSprite.setRegion(0, 16*1, 16, 16);
                    }
                    else if (tile.hasItem.equals("dome fossil")) {
                        this.fossilSprite.setRegion(0, 16*2, 16, 16);
                    }
                    else if (tile.hasItem.equals("old amber")) {
                        this.fossilSprite.setRegion(0, 0, 16, 16);
                    }
                    else if (tile.hasItem.equals("root fossil")) {
                        this.fossilSprite.setRegion(0, 16*3, 16, 16);
                    }
                    else if (tile.hasItem.equals("claw fossil")) {
                        this.fossilSprite.setRegion(0, 16*4, 16, 16);
                    }
                    else if (tile.hasItem.equals("shield fossil")) {
                        this.fossilSprite.setRegion(0, 16*5, 16, 16);
                    }
                    else if (tile.hasItem.equals("skull fossil")) {
                        this.fossilSprite.setRegion(0, 16*6, 16, 16);
                    }
                    game.mapBatch.draw(this.fossilSprite, tile.position.x, tile.position.y);
                }
                else if (tile.hasItem.contains("stone")) {
                    game.mapBatch.draw(TextureCache.get(Gdx.files.internal("tiles/pokeball1.png")), tile.position.x, tile.position.y);
                }
            }
        }
    }
}

/**
 * Draw tops of some trees over the player.
 */
class DrawMapTrees extends Action {
    public int layer = 110;
    Vector3 startPos;
    Vector3 endPos;
    Vector3 worldCoordsTL;
    Vector3 worldCoordsBR;
    Tile tile;
    public DrawMapTrees(Game game) {}

    public int getLayer() {
        return this.layer;
    }
    @Override
    public void step(Game game) {
        // TODO: remove (test)
//        if (game.map.tiles != game.map.overworldTiles) {
//            return;
//        }

        worldCoordsTL = game.cam.unproject(new Vector3(-256, 0, 0f));
        worldCoordsBR = game.cam.unproject(new Vector3(game.currScreen.x, game.currScreen.y+128, 0f));
        worldCoordsTL.x = (int)worldCoordsTL.x - (int)worldCoordsTL.x % 16;
        worldCoordsTL.y = (int)worldCoordsTL.y - (int)worldCoordsTL.y % 16;
        worldCoordsBR.x = (int)worldCoordsBR.x - (int)worldCoordsBR.x % 16;
        worldCoordsBR.y = (int)worldCoordsBR.y - (int)worldCoordsBR.y % 16;
        this.startPos = worldCoordsTL;
        this.endPos = worldCoordsBR;
        for (Vector2 currPos = new Vector2(startPos.x, startPos.y); currPos.y > endPos.y;) {
            tile = game.map.tiles.get(currPos);
            currPos.x += 16;
            if (currPos.x > endPos.x) {
                currPos.x = startPos.x;
                currPos.y -= 16;
            }
            if (tile == null) {
                continue;
            }
            if (!game.cam.frustum.pointInFrustum(tile.position.x, tile.position.y, game.cam.position.z) &&
                !game.cam.frustum.pointInFrustum(tile.position.x+tile.sprite.getWidth(), tile.position.y+tile.sprite.getHeight(), game.cam.position.z) &&
                !game.cam.frustum.pointInFrustum(tile.position.x+tile.sprite.getWidth(), tile.position.y, game.cam.position.z) &&
                !game.cam.frustum.pointInFrustum(tile.position.x, tile.position.y+tile.sprite.getHeight(), game.cam.position.z)) {
                continue;
            }
            // If this tree is supposed to be behind the player, don't re-draw it.
            if (tile.position.y > game.player.position.y) {
                continue;
            }

            // Still checking overSprite != null for safety
            if (tile.overSprite != null && tile.drawAsTree) {
                // TODO: test if that works for drawing the top part
                game.mapBatch.draw(tile.overSprite.getTexture(),
                                   tile.overSprite.getX(), tile.overSprite.getY()+16,
                                   0, 0, 16, (int)tile.overSprite.getHeight()-16);
            }
        }
    }
}

// debug for drawing lab floor 1 bg
class DrawSpecialMewtwoBg extends Action {
    public int layer = 141;

    Sprite bgSprite;

    public DrawSpecialMewtwoBg() {
        Texture text = TextureCache.get(Gdx.files.internal("lab1_fl1.png"));
        this.bgSprite = new Sprite(text, 0, 0, 479, 448);

        this.bgSprite.setPosition(-80 +1, -242 +1 +16*4);
    }

    public int getLayer() {
        return this.layer;
    }

    @Override
    public void step(Game game) {
        this.bgSprite.draw(game.mapBatch);
    }

}

class EnteiTile extends Tile {
    public EnteiTile(Vector2 pos) {
        super("entei_overw1", pos);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onPressA(Game game) {
        game.playerCanMove = false;

        // TODO - player can't move when returning from this
        game.battle.oppPokemon = new Pokemon("Entei", 50);

        Action encounterAction = new SplitAction(
                                     new BattleIntro(
                                     new BattleIntroAnim1(
                                     new SplitAction(
                                         new DrawBattle(game),
                                     new BattleAnimPositionPlayers(game,
                                     new PlaySound(game.battle.oppPokemon.specie.name,
                                     new DisplayText(game, "Wild "+ game.battle.oppPokemon.nickname.toUpperCase()+ " appeared!",
                                                     null, null,
                                     new WaitFrames(game, 39,
                                     game.player.adrenaline > 0 ?
                                         new DisplayText(game, "" + game.player.name + " has ADRENALINE " + Integer.toString(game.player.adrenaline) + "!",
                                                         null, null,
                                         new PrintAngryEating(game,
                                         new DrawBattleMenuSafariZone(game, null)))
                                     :
                                         new PrintAngryEating(game,
                                         new DrawBattleMenuSafariZone(game, null))
                                      ))))))),
                                  new WaitFrames(game, 10,
                                  new BattleIntroMusic(
                                  new WaitFrames(game, 100,
                                  new DoneWithDemo(game)))));

        game.insertAction(new DisplayText(game, "GROWL!!", "Entei", null,
                          // new PlayerCanMove(game,
                          encounterAction));
    }
}


class LightningFlash extends Action {
    Sprite sprite;
    int timer = 0;
    Color color;

    public LightningFlash(Action nextAction) {
        this.nextAction = nextAction;
        Texture text1 = TextureCache.get(Gdx.files.internal("battle/intro_frame6.png"));
        this.sprite = new Sprite(text1);
        this.sprite.setPosition(0,0);
    }

    public String getCamera() {return "gui";}

    @Override
    public void step(Game game) {
//        if (this.timer % 4 < 2) {  // TODO: remove, too much strobe
        if (this.timer % 14 < 7) {
            this.color = game.uiBatch.getColor();
            game.uiBatch.setColor(.5f, .5f, .5f, 1f);
            for (int i = -1; i < 2; i+= 1) {
                for (int j = -1; j < 2; j+= 1) {
                    game.uiBatch.draw(this.sprite, 160*i, 144*j);
                }
            }
            game.uiBatch.setColor(this.color);
        }

//        if (this.timer < 80) {  // TODO: remove, too much strobe
        if (this.timer < 14) {
            
        }
        else {
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
        }
        this.timer++;
    }
    
}

/**
 * TODO: remove this and use the teleport anim.
 */
class EscapeRope extends Action {
    Sprite sprite;
    Sprite sprite2;
    public int layer = 114;  // TODO: check
    int timer = 0;
    int slow = 3;

    public EscapeRope(Action nextAction) {
        this.nextAction = nextAction;
        // fade out from white anim
        Texture text1 = TextureCache.get(Gdx.files.internal("battle/intro_frame6.png"));
        this.sprite = new Sprite(text1);
        this.sprite.setPosition(0, 0);
        this.sprite2 = new Sprite(text1);
        this.sprite.setPosition(-144, 0);
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        if (this.timer < 2*slow) {
            if (this.timer == 0) {
                game.insertAction(new PlaySound("enter1", null));
            }
        }
        else if (this.timer < 4*slow) {
            this.sprite.draw(game.uiBatch, .25f);
            this.sprite2.draw(game.uiBatch, .25f);
        }
        else if (this.timer < 6*slow) {
            this.sprite.draw(game.uiBatch, .50f);
            this.sprite2.draw(game.uiBatch, .50f);
        }
        else if (this.timer < 22*slow) {
            if (this.timer == 6*slow) {
                game.map.interiorTilesIndex=100;
                game.map.tiles = game.map.overworldTiles;
                game.player.dirFacing = "down";
                game.player.currSprite = game.player.standingSprites.get(game.player.dirFacing);

                // Player buildtile stuff
                // TODO: this may need moved
                // TODO: test and remove if unused
//                if (game.map.tiles == game.map.overworldTiles) {
//                    game.player.buildTiles = game.player.outdoorBuildTiles;
//                }
//                else {
//                    game.player.buildTiles = game.player.indoorBuildTiles;
//                }
//                while (game.player.buildTileIndex > 0 && game.player.buildTileIndex >= game.player.buildTiles.size()) {
//                    game.player.buildTileIndex--;
//                }
//                game.player.currBuildTile = game.player.buildTiles.get(game.player.buildTileIndex);
                //
                Gdx.gl.glClearColor(0, 0, 0, 1);
                if (!game.map.timeOfDay.equals("night")) {
                    game.mapBatch.setColor(new Color(1f, 1f, 1f, 1f));
                }
                else {
                    // Is night, so set to night color
                    game.mapBatch.setColor(new Color(0.08f, 0.08f, 0.3f, 1.0f));
                    
                }
            }
            this.sprite.draw(game.uiBatch, 1f);
            this.sprite2.draw(game.uiBatch, 1f);
        }
        else if (this.timer < 24*slow) {
            if (this.timer == 22*slow) {
                game.insertAction(new PlaySound("exit1", null));
            }
            this.sprite.draw(game.uiBatch, .75f);
            this.sprite2.draw(game.uiBatch, .75f);
        }
        else if (this.timer < 26*slow) {
            this.sprite.draw(game.uiBatch, .50f);
            this.sprite2.draw(game.uiBatch, .50f);
        }
        else if (this.timer < 28*slow) {
            this.sprite.draw(game.uiBatch, .25f);
            this.sprite2.draw(game.uiBatch, .25f);
        }
        else {
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
        }
        this.timer++;
    }
}


// TODO: shader method
class EnterBuilding extends Action {
    Sprite sprite;
    String action;
    Action nextAction;
    Map<Vector2, Tile> whichTiles;

    public int layer = 114;  // TODO: check
    int timer = 0;

    int slow = 1;  // TODO: remove, use some sort of into anim;
    
    int interiorTilesIndex = 0;

    public EnterBuilding(Game game, Action nextAction) {
        this(game, "enter", nextAction);
    }

    public EnterBuilding(Game game, String action, Action nextAction) {
        this(game, action, null, nextAction);
    }

    public EnterBuilding(Game game, String action, int slow, Action nextAction) {
        this(game, action, null, nextAction);
        this.slow = slow;
    }
 
    public EnterBuilding(Game game, String action, Map<Vector2, Tile> whichTiles, int interiorTilesIndex, Action nextAction) {
        this(game, action, whichTiles, nextAction);
        this.interiorTilesIndex = interiorTilesIndex;
    }
    
    public EnterBuilding(Game game, String action, Map<Vector2, Tile> whichTiles, Action nextAction) {
        this.whichTiles = whichTiles;
        this.nextAction = nextAction;
        this.action = action;
        // fade out from white anim
        Texture text1 = TextureCache.get(Gdx.files.internal("battle/intro_frame6.png"));
        this.sprite = new Sprite(text1);
        this.sprite.setPosition(0,0);
        this.interiorTilesIndex = game.map.interiorTilesIndex;
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void firstStep(Game game) {
        if (this.slow != 1) {
            for (int i=0; i < 12*this.slow; i++) {
                this.step(game);
            }
        }
    }

    @Override
    public void step(Game game) {
        if (this.timer < 2*slow) {
            if (this.timer == 0 && this.action.equals("enter") || this.action.equals("exit")) {
                game.insertAction(new PlaySound(this.action+"1", null));
            }
        }
        else if (this.timer < 4*slow) {
            this.sprite.draw(game.uiBatch, .25f);
        }
        else if (this.timer < 6*slow) {
            this.sprite.draw(game.uiBatch, .50f);
        }
        else if (this.timer < 12*slow) {
            if (this.timer == 6*slow) {
                
                // TODO: test
                Tile tile = game.map.overworldTiles.get(game.player.position);
                if (tile != null && tile.name.equals("cave1_door1")) {
                    if (this.action.equals("enter")) {
                        game.map.interiorTilesIndex=90;
                    }
                    else {
                        game.map.interiorTilesIndex=100;
                    }
                }
                
                if (this.whichTiles != null) {
                    game.map.tiles = this.whichTiles;
                }
                else if (this.action.equals("enter")) {
                    game.map.tiles = game.map.interiorTiles.get(game.map.interiorTilesIndex);
                }
                else if (this.action.equals("exit")){
                    game.map.tiles = game.map.overworldTiles;
                }

                // Player buildtile stuff
                // TODO: user Player.updateBuildTiles()
//                if (game.map.tiles == game.map.overworldTiles) {
//                    if (game.player.hmPokemon != null && game.player.hmPokemon.specie.name.equals("smeargle")) {
//                        game.player.buildTiles = game.player.smeargleBuildTiles;
//                    }
//                    else {
//                        game.player.buildTiles = game.player.outdoorBuildTiles;
//                    }
//                }
//                else {
//                    if (game.player.hmPokemon != null && game.player.hmPokemon.specie.name.equals("smeargle")) {
//                        game.player.buildTiles = game.player.smeargleInteriorTiles;
//                    }
//                    else {
//                        game.player.buildTiles = game.player.indoorBuildTiles;
//                    }
//                }
//                while (game.player.buildTileIndex > 0 && game.player.buildTileIndex >= game.player.buildTiles.size()) {
//                    game.player.buildTileIndex--;
//                }
//                game.player.currBuildTile = game.player.buildTiles.get(game.player.buildTileIndex);
                game.player.updateBuildTiles(game);

                if (this.action.equals("exit")) {
                    Gdx.gl.glClearColor(1, 1, 1, 1);
                }
                else {
                    Gdx.gl.glClearColor(0, 0, 0, 1);
                }


                // Fade music if required
                tile = game.map.tiles.get(game.player.position);
                if (tile != null) {
                    Route newRoute = tile.routeBelongsTo;
                    if (newRoute != null && !newRoute.name.equals(game.map.currRoute.name) && (newRoute.isDungeon != game.map.currRoute.isDungeon)) {

                        // set flag in controller
                        // if going back to overworld, then what?
                        // handle batch shading separately
                        game.musicController.fadeToDungeon = true;

                        // TODO: fix
                        //  - need to resume overworld music.
//                        String nextMusicName = newRoute.getNextMusic(false);
//                        Action nextMusic = new FadeMusic("currMusic", "out", "", .025f,
//                                           new WaitFrames(game, 10,
//                                           null));
//                        if (newRoute.name.contains("pkmnmansion")) {
//                            game.mapBatch.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
//                            if (!game.loadedMusic.containsKey(nextMusicName)) {
//                                Music temp = new LinkedMusic("music/"+nextMusicName, "");
//                                temp.setVolume(0.1f);
//                                game.loadedMusic.put(nextMusicName, temp);
//                            }
//                            game.loadedMusic.get(nextMusicName).stop();
//                            nextMusic.append(// TODO: this didn't really work, still doesn't loop
//                                             new FadeMusic(nextMusicName, "in", "", .2f, true, 1f, null,
//                                             new CallMethod(game.loadedMusic.get(nextMusicName), "setLooping", new Object[]{true},
//                                             null)));
//                        }
//                        else {
//                            game.mapBatch.setColor(new Color(1f, 1f, 1f, 1f));
//                            nextMusic.append(new FadeMusic(nextMusicName, "in", "", .2f, true, 1f, null, null));///game.musicCompletionListener, null));
//                        }
//                        game.insertAction(nextMusic);
//                        nextMusic.step(game);
//                        game.fadeMusicAction = nextMusic;
                        game.map.currRoute = newRoute;
                    }
                    // Used for desert music transition atm
                    // TODO: I think this is for doors, not when game is loaded (?)
                    else if (newRoute != null && newRoute.type().equals("desert") && !game.map.timeOfDay.equals("night")) {
                        game.musicController.fadeToDungeon = true;
                        game.map.currRoute = newRoute;
                    }
                    // If player starts in graveyard, fade to correct music when game loads
                    else if (newRoute != null && newRoute.type().equals("graveyard") && !game.map.timeOfDay.equals("night")) {
                        // This had to be separate from batch color change below,
                        // was causing music issues.
                        game.musicController.fadeToDungeon = true;
                        game.map.currRoute = newRoute;
                        FogEffect.active = true;
                        FogEffect.refresh = true;
                    }

                    if (newRoute != null && newRoute.name.contains("pkmnmansion")) {
                        // TODO: test
                        game.mapBatch.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));

                        // TODO: remove
//                        game.mapBatch.setColor(new Color(0.1f, 0.1f, 0.1f, 1f));
//                        System.out.println("game.mapBatch.getPackedColor()");
//                        System.out.println(game.mapBatch.getPackedColor());
//                        System.out.println(new Color(0.1f, 0.1f, 0.1f, 1f).toFloatBits());
                    }
                    else if (newRoute != null && newRoute.name.equals("fossil_lab1")) {
                        game.mapBatch.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
                    }
                    else if (newRoute != null && newRoute.name.equals("ruins1_inner")) {
//                        game.mapBatch.setColor(new Color(0.04f, 0.04f, 0.1f, 1.0f));
//                        game.mapBatch.setColor(new Color(0.02f, 0.02f, 0.05f, 1.0f));  // Somewhat hard to see
//                        game.mapBatch.setColor(new Color(0.01f, 0.01f, 0.04f, 1.0f));  // Very hard to see
                        System.out.println(this.interiorTilesIndex);
                        if (this.interiorTilesIndex >= 99) {
                            game.mapBatch.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
                        }
                        else if (this.interiorTilesIndex == 98){
                            game.mapBatch.setColor(new Color(0.3f, 0.3f, 0.6f, 1.0f));
                        }
                        else if (this.interiorTilesIndex == 97){
                            game.mapBatch.setColor(new Color(0.04f, 0.04f, 0.1f, 1.0f));
                        }
                        else if (this.interiorTilesIndex == 96){
                            game.mapBatch.setColor(new Color(0.01f, 0.01f, 0.04f, 1.0f));  // Very hard to see
//                            game.mapBatch.setColor(new Color(0.02f, 0.02f, 0.05f, 1.0f));  // Somewhat hard to see
                        }
                        else {
                            game.mapBatch.setColor(new Color(0.02f, 0.02f, 0.05f, 1.0f));  // Somewhat hard to see
                        }
                    }
                    else if (newRoute != null && newRoute.name.equals("regi_cave1")) {
//                        game.mapBatch.setColor(new Color(0.08f, 0.08f, 0.3f, 1.0f));
//                        game.mapBatch.setColor(new Color(0.2f, 0.2f, 0.5f, 1.0f));  // TODO: re-enable?
                    }
                    else if (newRoute != null && newRoute.type().equals("graveyard") && !game.map.timeOfDay.equals("night")) {
                        game.mapBatch.setColor(new Color(.9f, .9f, 1f, 1f));
                    }
                    else if (!game.map.timeOfDay.equals("night")) {
                        // TODO: remove
//                        if (newRoute != null && newRoute.name.contains("pkmnmansion")) {
//                            game.mapBatch.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
//                        }
//                        else {
                            game.mapBatch.setColor(new Color(1f, 1f, 1f, 1f));
//                        }
                            
                        // TODO: debug, remove
//                        game.mapBatch.setColor(new Color(.7f, .7f, 1f, 1f));
                    }
                    else {
                        // Is night, so set to night color
                        game.mapBatch.setColor(new Color(0.08f, 0.08f, 0.3f, 1.0f));
                        
//                        // This is a lighter version of night used for recording video
//                        game.mapBatch.setColor(new Color(0.3f, 0.3f, 0.7f, 1.0f));  // TODO: debug, remove
                    }
                }
            }
            this.sprite.draw(game.uiBatch, 1f);
        }
        else if (this.timer < 14*slow) {
            this.sprite.draw(game.uiBatch, .75f);
        }
        else if (this.timer < 16*slow) {
            this.sprite.draw(game.uiBatch, .50f);
        }
        else if (this.timer < 18*slow) {
            this.sprite.draw(game.uiBatch, .25f);
        }
        else {
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
        }
        this.timer++;
    }
}

class MegaGengarTile extends Tile {
    public MegaGengarTile(Vector2 pos) {
        super("mega_gengar_overworld1", pos);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onPressA(Game game) {
        game.playerCanMove = false;

        game.insertAction(new SpecialBattleMegaGengar(game));
    }
}

/**
 * TODO: Trying this out.
 * TODO: remove, unused (I think)
 */
class DrawCampfireAuras extends Action {
    public int layer = 110;
    Vector3 startPos;
    Vector3 endPos;
    Vector3 worldCoordsTL;
    Vector3 worldCoordsBR;
    Tile tile;
    Sprite[] campfireSprites = new Sprite[2];
    int campfireTimer = 0;

    public DrawCampfireAuras(Game game) {
        Texture text = TextureCache.get(Gdx.files.internal("fire_mask1.png"));
        this.campfireSprites[0] = new Sprite(text, 0,  0, 160, 144);
        text = TextureCache.get(Gdx.files.internal("fire_mask2.png"));
        this.campfireSprites[1] = new Sprite(text, 0, 0, 160, 144);
    }

    public int getLayer() {
        return this.layer;
    }

    @Override
    public void step(Game game) {
        if (game.map == null || game.map.tiles == null) {
            return;
        }
        worldCoordsTL = game.cam.unproject(new Vector3(-256, -256, 0f));
        worldCoordsBR = game.cam.unproject(new Vector3(game.currScreen.x+256, game.currScreen.y+256, 0f));
        worldCoordsTL.x = (int)worldCoordsTL.x - (int)worldCoordsTL.x % 16;
        worldCoordsTL.y = (int)worldCoordsTL.y - (int)worldCoordsTL.y % 16;
        worldCoordsBR.x = (int)worldCoordsBR.x - (int)worldCoordsBR.x % 16;
        worldCoordsBR.y = (int)worldCoordsBR.y - (int)worldCoordsBR.y % 16;
        this.startPos = worldCoordsTL;
        this.endPos = worldCoordsBR;
        for (Vector2 currPos = new Vector2(startPos.x, startPos.y); currPos.y > endPos.y;) {
            tile = game.map.tiles.get(currPos);
            currPos.x += 16;
            if (currPos.x > endPos.x) {
                currPos.x = startPos.x;
                currPos.y -= 16;
            }
            if (tile == null) {
                continue;
            }
            // Draw campfire sprites to batch.
            if (tile.nameUpper.equals("campfire1")) {
                Sprite currSprite = this.campfireSprites[1];
                if (this.campfireTimer % 20 < 10) {
                    currSprite = this.campfireSprites[0];
                }
                for (int i=0; i < 1; i++) {
                    game.lightingBatch.draw(currSprite,
                                            tile.position.x +8 -(currSprite.getWidth()/2f),
                                            tile.position.y +8 -(currSprite.getHeight()/2f));
                }
            }
        }
        if (this.campfireTimer < 79) {
            this.campfireTimer++;
        }
        else {
            this.campfireTimer = 0;
        }
    }
}

/**
 * Moves water back and forth.
 * 
 * TODO: bug where grass tiles move out of sync with normal,
 *       probably because drawmap is before a camera update, drawGrass is after
 * TODO: this is doubling as animating campfire.
 * TODO: probably rename to 'AnimateMap' or something, b/c I 
 *       also want it to move flowers.
 */
class MoveWater extends Action {
    public int layer = 109;
    ArrayList<Vector2> positions;
    Vector2 position;
    ArrayList<Integer> repeats;
//    Texture texture;
    Vector3 startPos;
    Vector3 endPos;
    Vector3 worldCoordsTL;
    Vector3 worldCoordsBR;
    Tile tile;
    Sprite[] campfireSprites;
    Sprite[] torchSprites = new Sprite[2];
    int campfireTimer = 0;
    int avatarTimer = 0;
    int waterTimer = 0;
    int regiCaveTimer = 0;
    public static int regiTimer2 = 0;
    public boolean justFlipped = false;
    int regiCaveInterval = 40; //60;
//    PointLight pointLight;  // TODO: remove
    Pixmap pixmap;
    // Sep pixmap for roaming and riding mons,
    // Needed for optimization
    Pixmap roamingPixmap;  
    Texture roamingTexture;
    Pixmap firePixmap1;
    Pixmap firePixmap2;
    Pixmap torchPixmap1;
    Pixmap torchPixmap2;
    int timer = 0;
    Vector2 prevPos = new Vector2();
    public ArrayList<Tile> placeWater = new ArrayList<Tile>();

    public MoveWater(Game game) {
        this.positions = new ArrayList<Vector2>();
        this.resetVars();
        Texture text = TextureCache.get(Gdx.files.internal("tiles/campfire1.png"));
        this.campfireSprites = new Sprite[4];
        this.campfireSprites[0] = new Sprite(text, 0,  0, 16, 20);
        this.campfireSprites[1] = new Sprite(text, 16, 0, 16, 20);
//        text = TextureCache.get(Gdx.files.internal("fire_mask1.png"));
//        this.campfireSprites[2] = new Sprite(text, 0,  0, 160, 144);
//        text = TextureCache.get(Gdx.files.internal("fire_mask2.png"));
//        this.campfireSprites[3] = new Sprite(text, 0, 0, 160, 144);

        text = TextureCache.get(Gdx.files.internal("tiles/torch_sheet1.png"));
        this.torchSprites[0] = new Sprite(text, 0,  60, 16, 20);
        this.torchSprites[1] = new Sprite(text, 16, 60, 16, 20);
        text = TextureCache.get(Gdx.files.internal("torch_mask1.png"));
        TextureData temp = text.getTextureData();
        if (!temp.isPrepared()) {
            temp.prepare();
        }
        this.torchPixmap1 = temp.consumePixmap();
        text = TextureCache.get(Gdx.files.internal("torch_mask2.png"));
        temp = text.getTextureData();
        if (!temp.isPrepared()) {
            temp.prepare();
        }
        this.torchPixmap2 = temp.consumePixmap();

        //
        text = TextureCache.get(Gdx.files.internal("fire_mask3.png"));
        this.campfireSprites[2] = new Sprite(text, 0,  0, 160, 144);
        temp = text.getTextureData();
        if (!temp.isPrepared()) {
            temp.prepare();
        }
        this.firePixmap1 = temp.consumePixmap();
        text = TextureCache.get(Gdx.files.internal("fire_mask4.png"));
        this.campfireSprites[3] = new Sprite(text, 0, 0, 160, 144);
        temp = text.getTextureData();
        if (!temp.isPrepared()) {
            temp.prepare();
        }
        this.firePixmap2 = temp.consumePixmap();

//        this.pointLight = new PointLight(game.rayHandler, 20, new Color(.3f,.2f,.1f,1), 2, -0, 0);
//        this.pointLight = new PointLight(game.rayHandler, 8, new Color(1f, .9f, .7f, 1), 5f, -0, 0);
//        this.pointLight = new PointLight(game.rayHandler, 16, new Color(.8f, .7f, .6f, 1), 5f, -0, 0);
//        this.pointLight.setPosition(0f, 0f);

//        this.pixmap = new Pixmap(160*3, 144*3, Pixmap.Format.RGBA8888);
        this.pixmap = new Pixmap(160*4, 144*3, Pixmap.Format.RGBA8888);
//        this.pixmap.setColor(new Color(0f, 0f, 0f, 1f));  // previous behavior when this was the only pixmap
        this.pixmap.setColor(new Color(0f, 0f, 0f, 0f));

//        this.roamingPixmap = new Pixmap(160*3, 144*3, Pixmap.Format.RGBA8888);
        this.roamingPixmap = new Pixmap(160*4, 144*3, Pixmap.Format.RGBA8888);
        this.roamingPixmap.setColor(new Color(0f, 0f, 0f, 1f));
        this.roamingTexture = TextureCache.get(this.roamingPixmap);
    }

    public int getLayer() {
        return this.layer;
    }

    public void resetVars() {
        this.position = new Vector2(0, 0);
        this.positions.add(new Vector2(0, 0));
        this.positions.add(new Vector2(1, 0));
        this.positions.add(new Vector2(2, 0));
        this.positions.add(new Vector2(3, 0));
        this.positions.add(new Vector2(2, 0));
        this.positions.add(new Vector2(1, 0));

        this.repeats = new ArrayList<Integer>();
        this.repeats.add(20 - 1); // 20 frames each
        this.repeats.add(20 - 1); // 20 frames each
        this.repeats.add(20 - 1); // 20 frames each
        this.repeats.add(20 - 1); // 20 frames each
        this.repeats.add(20 - 1); // 20 frames each
        this.repeats.add(20 - 1); // 20 frames each
    }

    @Override
    public void step(Game game) {
        // TODO: battle still lags pretty badly even with this optimization
        //       probably need to not draw overworld at all while in battle.
        // Possible that there could be a way to only change the mask when
        // a new campfire tile is built.
        // TODO: this optimization is broken for roaming pokemon and field move
        //       fire-type pokemon.
        this.roamingPixmap.fill();
        if (this.campfireTimer % 10 == 0) {
            this.pixmap.fill();
//            this.prevPos.set(game.player.position.x +8 -240, game.player.position.y +8 -216);
            this.prevPos.set(game.player.position.x, game.player.position.y);
        }
        // TODO: remove
//        Sprite currMask = this.campfireSprites[3];
//        if (this.campfireTimer % 20 < 10) {
//          currMask = this.campfireSprites[2];
//        }
//        TextureData temp = currMask.getTexture().getTextureData();
//        if (!temp.isPrepared()) {
//            temp.prepare();
//        }
//        Pixmap currPixmap = temp.consumePixmap();
        Pixmap currPixmap = this.firePixmap2;
        Pixmap currTorchPixmap = this.torchPixmap2;
        if (this.campfireTimer % 20 < 10) {
            currPixmap = this.firePixmap1;
            currTorchPixmap = this.torchPixmap1;
        }
        worldCoordsTL = game.cam.unproject(new Vector3(-256, -256, 0f));
        worldCoordsBR = game.cam.unproject(new Vector3(game.currScreen.x+256, game.currScreen.y+256, 0f));
        worldCoordsTL.x = (int)worldCoordsTL.x - (int)worldCoordsTL.x % 16;
        worldCoordsTL.y = (int)worldCoordsTL.y - (int)worldCoordsTL.y % 16;
        worldCoordsBR.x = (int)worldCoordsBR.x - (int)worldCoordsBR.x % 16;
        worldCoordsBR.y = (int)worldCoordsBR.y - (int)worldCoordsBR.y % 16;
        this.startPos = worldCoordsTL; // new Vector3(worldCoordsTL.x, worldCoordsTL.y, 0f);
        this.endPos = worldCoordsBR; // new Vector3(worldCoordsBR.x, worldCoordsBR.y, 0f);

        if (positions.isEmpty()) {
            this.resetVars();
        }
        this.position = positions.get(0);
        for (Vector2 currPos = new Vector2(startPos.x, startPos.y); currPos.y > endPos.y;) {
            tile = game.map.tiles.get(currPos);
            currPos.x += 16;
            if (currPos.x > endPos.x) {
                currPos.x = startPos.x;
                currPos.y -= 16;
            }
            if (tile == null) {
                continue;
            }
//            if (!game.cam.frustum.pointInFrustum(tile.position.x, tile.position.y, game.cam.position.z) &&
//                    !game.cam.frustum.pointInFrustum(tile.position.x+tile.sprite.getWidth(), tile.position.y+tile.sprite.getHeight(), game.cam.position.z) &&
//                    !game.cam.frustum.pointInFrustum(tile.position.x+tile.sprite.getWidth(), tile.position.y, game.cam.position.z) &&
//                    !game.cam.frustum.pointInFrustum(tile.position.x, tile.position.y+tile.sprite.getHeight(), game.cam.position.z)) {
//               continue;
//            }
            // Texture text = new
            // Texture(Gdx.files.internal("tiles/water1_2.png"));
            if (tile.attrs.get("water")) {
                // tile.sprite.setPosition(tile.sprite.getX()+position.x,
                // tile.sprite.getY()+position.y);
                // tile.overSprite.draw(game.batch);
                tile.sprite.setRegionX((int)this.position.x);
                tile.sprite.setRegionWidth((int)tile.sprite.getWidth());
            }
            else if (tile.name.contains("flower")) {
//            else if (tile.is(Tile.Type.FLOWER)) {
                if (this.timer == 0) {
                    tile.sprite.setRegion(0, 0, 16, 16);
                }
                else if (this.timer == 26) {
                    tile.sprite.setRegion(16, 0, 16, 16);
                }
            }
            // Animate campfires
            // Also animate campfire aura around fire type pokemon walking around.
            Pokemon pokemon = game.map.pokemon.get(tile.position);
            if (tile.nameUpper.equals("campfire1")) {
//            if (tile.is(Tile.Type.CAMPFIRE)) {
                if (this.campfireTimer == 0) {
                    Sprite newSprite = new Sprite(this.campfireSprites[0]);
                    newSprite.setPosition(tile.overSprite.getX(), tile.overSprite.getY());
                    tile.overSprite = newSprite;
                }
                else if (this.campfireTimer == 40) {
                    Sprite newSprite = new Sprite(this.campfireSprites[1]);
                    newSprite.setPosition(tile.overSprite.getX(), tile.overSprite.getY());
                    tile.overSprite = newSprite;
                }
                // Attempt at fixing campfire aura issue using a pixmap
                // Issue was that pixel 'lightness' would keep increasing until
                // everything was white and hard to see.
                if (this.campfireTimer % 10 == 0) {
                    if (game.mapBatch.getColor().r < .5f) {
                        int xPos = (int)(tile.position.x -80 -(game.player.position.x -320));  // -240
                        int yPos = (int)(296 -(tile.position.y +8 -72 -(game.player.position.y -216)));
                        this.pixmap.drawPixmap(currPixmap, xPos, yPos);
                    }
                }
            }
            else if (tile.isTorch || (tile.items != null && tile.items.containsKey("torch"))) {
                tile.isTorch = true;
                Sprite newSprite;
                if (this.campfireTimer < 40) {
                    newSprite = new Sprite(this.torchSprites[0]);
                }
                else {
                    newSprite = new Sprite(this.torchSprites[1]);
                }
                // Apply the campfire aura
                Color color = game.mapBatch.getColor();
                if (game.mapBatch.getColor().r < .5f) {
                    game.mapBatch.setColor(0.2f, 0.2f, 0.2f, 1f);
                    if (this.campfireTimer % 10 == 0) {
                        int xPos = (int)(tile.position.x -80 -(game.player.position.x -320));
                        int yPos = (int)(296 -(tile.position.y +8 -72 -(game.player.position.y -216)));
                        this.pixmap.drawPixmap(currTorchPixmap, xPos +40, yPos +36);
                    }
                }
                int offsetX = 0;
//                if (tile.nameUpper.equals("fence1")) {
//                    offsetX = -4;
//                }
                game.mapBatch.draw(newSprite, tile.position.x +offsetX, tile.position.y+4 +2);
                game.mapBatch.setColor(color);
            }
            else if (tile.name.equals("cave1_regi3")) {
                if (game.battle.drawAction != null) {
                    // TODO: remove
//                    tile.sprite.setPosition(tile.position.x-8, tile.position.y-9);
//                    tile.sprite.setRegion(0, 0, 48, 48);
//                    if (this.regiTimer2 % 80 == 0) {
//                        tile.sprite.setRegion(160, 0, 32, 32);
//                    }
//                    else if (this.regiTimer2 % 80 == 40) {
//                        tile.sprite.setRegion(128, 0, 32, 32);
//                    }
//                    tile.sprite.setRegion(160, 0, 32, 32);
                    // The commented stuff here would make regigigas dance when you
                    // interacted with him.
                    if (this.regiTimer2 == 0) {
                        tile.sprite.setRegion(160, 0, 32, 32);
//                        tile.sprite.setRegion(432, 0, 48, 48);
//                        tile.sprite.setPosition(tile.position.x-8, tile.position.y-9);
                    }
//                    else if (this.regiTimer2 == 15) {
//                        tile.sprite.setRegion(384, 0, 48, 48);
//                        tile.sprite.setPosition(tile.position.x-8, tile.position.y-9);
//                    }
                    this.justFlipped = true;
                }
                else {
                    if (this.justFlipped) { // annoying work-around
                        this.justFlipped = false;
                        tile.sprite.setPosition(tile.position.x, tile.position.y);
                        tile.sprite.setRegion(128, 0, 32, 32);
                    }
                    if (this.regiTimer2 == 0) {
                        tile.sprite.setRegion(160, 0, 32, 32);
                    }
                    else if (this.regiTimer2 == 4) {
                        tile.sprite.setRegion(128, 0, 32, 32);
                    }
                    else if (this.regiTimer2 == 8) {
                        tile.sprite.setRegion(160, 0, 32, 32);
                    }
                    else if (this.regiTimer2 == 12) {
                        tile.sprite.setRegion(128, 0, 32, 32);
                    }
                    else if (this.regiTimer2 == 16) {
                        tile.sprite.setRegion(160, 0, 32, 32);
                    }
                    else if (this.regiTimer2 == 20) {
                        tile.sprite.setRegion(128, 0, 32, 32);
                    }
                    else if (this.regiTimer2 == 36 +16) {
                        tile.sprite.setRegion(192, 0, 32, 32);
                    }
                    else if (this.regiTimer2 == 40 +16) {
                        tile.sprite.setRegion(224, 0, 32, 32);
                    }
                    else if (this.regiTimer2 == 44 +16) {
                        tile.sprite.setRegion(256, 0, 32, 32);
                    }
                    else if (this.regiTimer2 == 48 +16) {
                        tile.sprite.setRegion(288, 0, 32, 32);
                    }
                    else if (this.regiTimer2 == 52 +16) {
                        tile.sprite.setRegion(128, 0, 32, 32);
                    }
                }
                
                // TODO: remove
//                else if (this.regiTimer2 == 0) {
//                    tile.sprite.setRegion(0, 0, 48, 48);
//                    tile.sprite.setPosition(tile.position.x-8, tile.position.y-9);
//                }
//                else if (this.regiTimer2 == 15) {
//                    tile.sprite.setRegion(48, 0, 48, 48);
//                    tile.sprite.setPosition(tile.position.x-8, tile.position.y-9);
//                }
            }
            else if (tile.nameUpper.contains("revived_")) {
//            else if (tile.is(Tile.Type.REVIVED)) {
                String name = tile.nameUpper.split("_")[1];
                if (!Specie.species.containsKey(name)) {
                    Specie.species.put(name, new Specie(name));
                }
                int index = 0;
                if (this.avatarTimer < 60) {
                    index = 1;
                }
                game.mapBatch.draw(Specie.species.get(name).avatarSprites.get(index), tile.position.x+8, tile.position.y+8);
            }
            // TODO: make generic? (somehow would have to know to trigger campfire aura)
            else if (tile.nameUpper.equals("volcarona")) {
//            else if (tile.is(Tile.Type.VOLCARONA)) {
                // Campfire aura
                if (this.campfireTimer % 10 == 0) {
                    if (game.mapBatch.getColor().r < .5f) {
                        int xPos = (int)(tile.position.x -80 -(game.player.position.x -320));
                        int yPos = (int)(296 -(tile.position.y +8 -72 -(game.player.position.y -216)));
                        this.pixmap.drawPixmap(currPixmap, xPos, yPos);
                    }
                }
                String name = tile.nameUpper;
                if (!Specie.species.containsKey(name)) {
                    Specie.species.put(name, new Specie(name));
                }
                int index = 0;
                if (this.avatarTimer < 60) {
                    index = 1;
                }
                game.mapBatch.draw(Specie.species.get(name).avatarSprites.get(index), tile.position.x, tile.position.y+4);
            }
            else if (tile.nameUpper.equals("spiritomb")) {
                String name = tile.nameUpper;
                if (!Specie.species.containsKey(name)) {
                    Specie.species.put(name, new Specie(name));
                }
                int index = 0;
                if (this.avatarTimer < 60) {
                    index = 1;
                }
                game.mapBatch.draw(Specie.species.get(name).avatarSprites.get(index), tile.position.x, tile.position.y+4);
            }
            else if (this.waterTimer == 0 && (tile.nameUpper.contains("hole") && !tile.nameUpper.contains("water"))) {
                Vector2[] positions = new Vector2[]{new Vector2(0, -16), new Vector2(0, 16),
                                                    new Vector2(-16, 0), new Vector2(16, 0)};
                for (Vector2 position : positions) {
                    Tile currTile = game.map.tiles.get(tile.position.cpy().add(position));
                    if (currTile == null) {
                        continue;
                    }
                    if (currTile.name.contains("water") || currTile.nameUpper.contains("water")) {
                        this.placeWater.add(tile);
                        break;
                    }
                }
            }
            // TODO: is this used?
            // TODO: I think this added 'round edges' to holes. Remove if not using.
//            else if (tile.nameUpper.contains("hole1_water")) {
//                tile.overSprite.setTexture(Tile.dynamicTexts.get(tile.nameUpper)[(int)this.position.x]);
//            }

            if (pokemon != null &&
                pokemon.mapTiles == game.map.tiles &&
                pokemon.hms.contains("FLASH") &&
                game.mapBatch.getColor().r < .5f) {
                int xPos = (int)(pokemon.position.x -80 -(game.player.position.x -320));
                int yPos = (int)(296 -(pokemon.position.y +8 -72 -(game.player.position.y -216)));
//                    this.pixmap.drawPixmap(currPixmap, xPos, yPos);
                this.roamingPixmap.drawPixmap(currPixmap, xPos, yPos);
            }
        }
        if (game.mapBatch.getColor().r < .5f &&
            game.player.hmPokemon != null &&
            game.player.hmPokemon.currOwSprite != null &&
            game.player.hmPokemon.hms.contains("FLASH")) {
//            Pokemon pokemon = game.player.hmPokemon;  // TODO: remove
            Vector2 position = game.player.hmPokemon.position;
            // TODO: use game.player.currFieldMove instead
            // If pokemon is the one doing the field move,
            // then draw the fire aura at the player's position.
            if (!game.player.currFieldMove.equals("")) {
                position = game.player.position;
            }
            int xPos = (int)(position.x -80 -(game.player.position.x -320));  // -240
            int yPos = (int)(296 -(position.y +8 -72 -(game.player.position.y -216)));
//                this.pixmap.drawPixmap(currPixmap, xPos, yPos);
            this.roamingPixmap.drawPixmap(currPixmap, xPos, yPos);
        }
        if (game.mapBatch.getColor().r < 1f) {
            Color tempColor = game.mapBatch.getColor();
            game.mapBatch.setColor(new Color(1f, 1f, 1f, 1f));
            int temp1 = game.mapBatch.getBlendSrcFunc();
            int temp2 = game.mapBatch.getBlendDstFunc();
            game.mapBatch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_SRC_ALPHA);
            
            // Campfire pixmap needs to be drawn onto the roaming pixmap
            this.roamingPixmap.drawPixmap(this.pixmap, (int)(this.prevPos.x - game.player.position.x), (int)(game.player.position.y-this.prevPos.y));
            
//            Texture texture = new Texture(this.pixmap);
//            Texture texture = new Texture(this.roamingPixmap);
            this.roamingTexture.draw(this.roamingPixmap, 0, 0);
            // TODO: diff levels of exposure depending on batch color
            game.mapBatch.draw(this.roamingTexture, game.player.position.x +8 -320, game.player.position.y +8 -216);  // -240
//            game.mapBatch.draw(texture, this.prevPos.x, this.prevPos.y);
            if (tempColor.r < 0.5f) {
                game.mapBatch.draw(this.roamingTexture, game.player.position.x +8 -320, game.player.position.y +8 -216);
//                game.mapBatch.draw(texture, this.prevPos.x, this.prevPos.y);
            }
            if (tempColor.r < 0.1f) {
                game.mapBatch.draw(this.roamingTexture, game.player.position.x +8 -320, game.player.position.y +8 -216);
//                game.mapBatch.draw(texture, this.prevPos.x, this.prevPos.y);
            }
            game.mapBatch.setBlendFunction(temp1, temp2);
            game.mapBatch.setColor(tempColor);
//            texture.dispose();
        }
        if (this.campfireTimer < 79) {
            this.campfireTimer++;
        }
        else {
            this.campfireTimer = 0;
        }
        if (this.avatarTimer < 120) {
            this.avatarTimer++;
        }
        else {
            this.avatarTimer = 0;
        }
        if (this.waterTimer < 60) {
            this.waterTimer++;
        }
        else {
            this.waterTimer = 0;
        }
        if (game.battle.drawAction != null && this.regiTimer2 < 30) {
            this.regiTimer2++;
        }
        else if (game.battle.drawAction == null && this.regiTimer2 < 160) {
            this.regiTimer2++;
        }
        else {
            this.regiTimer2 = 0;
        }
        if (game.map.currRoute != null &&
            game.battle.drawAction == null &&  // TODO: might be kind of cool tho.
            game.map.currRoute.name.equals("regi_cave1")) {

//            if (game.battle.drawAction != null) {
                game.uiBatch.setColor(new Color(1f, 1f, 1f, 1.0f));
                game.mapBatch.setColor(new Color(0.6f, 0.6f, 0.6f, 1.0f));
//            }
//            else 
            if (this.regiCaveTimer < this.regiCaveInterval*1) {
//                SpriteProxy.darkenColors1 = true;
//                SpriteProxy.darkenColors2 = false;
                game.uiBatch.setColor(new Color(0.7f, 0.7f, 0.8f, 1.0f));
                game.mapBatch.setColor(new Color(0.5f, 0.5f, 0.8f, 1.0f));
            }
            else if (this.regiCaveTimer < this.regiCaveInterval*2) {
//                SpriteProxy.darkenColors1 = false;
//                SpriteProxy.darkenColors2 = true;
                game.uiBatch.setColor(new Color(0.6f, 0.6f, 0.7f, 1.0f));
                game.mapBatch.setColor(new Color(0.4f, 0.4f, 0.7f, 1.0f));
            }
            else if (this.regiCaveTimer < this.regiCaveInterval*3) {
//                SpriteProxy.darkenColors1 = true;
//                SpriteProxy.darkenColors2 = false;
                game.uiBatch.setColor(new Color(0.7f, 0.7f, 0.8f, 1.0f));
                game.mapBatch.setColor(new Color(0.30f, 0.30f, 0.60f, 1.0f));
            }
            else if (this.regiCaveTimer < this.regiCaveInterval*4) {
//                SpriteProxy.darkenColors1 = false;
//                SpriteProxy.darkenColors2 = true;
                game.uiBatch.setColor(new Color(0.6f, 0.6f, 0.7f, 1.0f));
                game.mapBatch.setColor(new Color(0.20f, 0.20f, 0.50f, 1.0f));
            }
            else if (this.regiCaveTimer < this.regiCaveInterval*5) {
//                SpriteProxy.darkenColors1 = true;
//                SpriteProxy.darkenColors2 = false;
                game.uiBatch.setColor(new Color(0.7f, 0.7f, 0.8f, 1.0f));
                game.mapBatch.setColor(new Color(0.30f, 0.30f, 0.60f, 1.0f));
            }
            else {
//                SpriteProxy.darkenColors1 = false;
//                SpriteProxy.darkenColors2 = true;
                game.uiBatch.setColor(new Color(0.6f, 0.6f, 0.7f, 1.0f));
                game.mapBatch.setColor(new Color(0.4f, 0.4f, 0.7f, 1.0f));
            }

            if (this.regiCaveTimer < this.regiCaveInterval*6) {
                this.regiCaveTimer++;
            }
            else {
                this.regiCaveTimer = 0;
            }
        }
        // TODO: might screw other effects up in the future.
        else {
            game.uiBatch.setColor(new Color(1f, 1f, 1f, 1.0f));
        }

        if (!this.placeWater.isEmpty()) {

            for (Tile tile : this.placeWater) {
                //          Tile newTile = new Tile("water2", tile.position.cpy(), true, tile.routeBelongsTo);
                //          newTile.items = tile.items;
                //          newTile.hasItem = tile.hasItem;
                //          newTile.hasItemAmount = tile.hasItemAmount;
                //          game.map.tiles.put(tile.position.cpy(), newTile);
                
                // Try to preserve route and biome this way
                // TODO: refactor after making tile.init() available
//                tile.name = "water2";
//                tile.nameUpper = "";
//                tile.overSprite = null;
//                Texture playerText = TextureCache.get(Gdx.files.internal("tiles/water2.png"));
//                tile.sprite = new Sprite(playerText, 0, 0, 16, 16);
//                tile.sprite.setPosition(tile.position.x, tile.position.y);
//                tile.attrs.put("solid", true);
//                tile.attrs.put("water", true);

                // This was the 'hole that looks like water' stuff
                // TODO: don't remove this block until sure not using
//                String[] names = tile.nameUpper.split("_");
//                String ending = "_default";
//                if (names.length > 1) {
//                    ending = "_" + names[names.length-1];
//                }
//                System.out.println(tile.nameUpper);
//                System.out.println(names[0]+"_water"+ending);
////                Tile newTile = new Tile(tile.name, names[0]+"_water"+ending, tile.position.cpy(), true, tile.routeBelongsTo);
//                Tile newTile = new Tile("sand3", names[0]+"_water"+ending, tile.position.cpy(), true, tile.routeBelongsTo);
//                newTile.items = tile.items;
//                newTile.hasItem = tile.hasItem;
//                newTile.hasItemAmount = tile.hasItemAmount;
//                newTile.biome = tile.biome;
//                game.map.tiles.put(tile.position.cpy(), newTile);

                tile.name = "water5";
                tile.nameUpper = "";
                tile.overSprite = null;
                tile.init(tile.name, tile.nameUpper, tile.position, true, tile.routeBelongsTo);
                
                // TODO: not doing atm. May revisit.
                // Surround left/right/up/down with sand
//                Vector2[] positions = new Vector2[]{new Vector2(0, -16), new Vector2(0, 16),
//                                                    new Vector2(-16, 0), new Vector2(16, 0)};
//                for (Vector2 position : positions) {
//                    Tile currTile = game.map.tiles.get(tile.position.cpy().add(position));
//                    if (currTile == null) {
//                        continue;
//                    }
//                    if (!currTile.attrs.get("solid") && 
//                        !currTile.name.contains("sand") && 
//                        !currTile.name.contains("bridge")) {
//                        currTile.name = "sand1";
//                        currTile.init(currTile.name, currTile.nameUpper, currTile.position, true, currTile.routeBelongsTo);
//                    }
//                }
            }
            game.insertAction(new PlaySound("sand1", .5f, true, null));
            this.placeWater.clear();
        }
        
        // repeat sprite/pos for current object for 'frames[0]' number of
        // frames.
        if (this.repeats.get(0) > 0) {
            this.repeats.set(0, this.repeats.get(0) - 1);
        }
        else {
            // since position is relative, only update once each time period
            // this.position = this.position.add(positions.get(0));
            positions.remove(0);
            repeats.remove(0);
        }
        this.timer++;
        if (this.timer >= 52) {
            this.timer = 0;
        }
    }

}

public class PkmnMap {

    // it looks like .equals() is called here, so this method is valid.
    // ie, passing a new vector to search for a tile is fine
    public Map<Vector2, Tile> overworldTiles = new HashMap<Vector2, Tile>();
    public Map<Vector2, Tile> tiles = overworldTiles;
    public ArrayList<HashMap<Vector2, Tile>> interiorTiles = new ArrayList<HashMap<Vector2, Tile>>();
    public int interiorTilesIndex = 100;

    // locations of all pokemon currently located on the map
    Map<Vector2, Pokemon> pokemon = new HashMap<Vector2, Pokemon>();

    Vector2 bottomLeft = null;
    Vector2 topRight = null;

    // use this to drop the tops of trees over the player
    //  hopefully makes drawing take less time
//    Map<Vector2, Tile> trees = new HashMap<Vector2, Tile>();

    // Used to know where to spawn new players
    ArrayList<Vector2> edges = new ArrayList<Vector2>();
    // Routes on map
    ArrayList<Route> routes;
    // debating whether I should just make tiles have references
    // to route objects, and not have currRoute here
    public Route currRoute;
    String currBiome = "";
    // needed for wild encounters etc
    Random rand;
    String timeOfDay = "day";  // used by cycleDayNight
    String id;  // needed for saving to file
//    TrainerTipsTile unownSpawn;
    ArrayList<String> unownUsed = new ArrayList<String>();
    {
        char[] textArray = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        for (int i=0; i < textArray.length; i++) {
            unownUsed.add(String.valueOf(textArray[i]));
        }
        unownUsed.add("!");
        unownUsed.add("qmark");
    }

    public Pixmap minimap;
    // Tiles queued to be drawn to the minimap
    public ArrayList<Tile> minimapQueue = new ArrayList<Tile>();
    
    //
    public ShadeEffect shadeEffect;

    // TODO: test
//    Synced.HashMap<Vector2, Tile> testTiles = new Synced.HashMap<Vector2, Tile>("game.map.tiles");

    public PkmnMap(String mapName) {
        this.id = mapName;
        Vector2 pos;
        this.rand = new Random();

        // init interior tiles
        // I couldn't figure out how to make this a normal array
        // higher numbers == higher layers in y direction
        for (int i=0; i < 100; i++) {
            this.interiorTiles.add(null);
        }
        this.interiorTiles.add(new HashMap<Vector2, Tile>());
        this.interiorTiles.add(new HashMap<Vector2, Tile>());

        if (mapName == "default") {
            /*
             *
             * // for now, just load this manually for (int i = 0; i < 10; i++) {
             * for (int j = 0; j < 10; j++) { pos = new Vector2(i*16, j*16);
             * this.tiles.put(pos, new Tile("ground1", pos)); } }
             *
             * for (int i = 0; i < 10; i++) { pos = new Vector2(i*16, 10*16);
             * this.tiles.put(pos, new Tile("block1", pos));
             *
             * pos = new Vector2(i*16, -1*16); this.tiles.put(pos, new
             * Tile("block1", pos)); }
             *
             * for (int j = 0; j < 8; j++) { pos = new Vector2(-1*16, j*16);
             * this.tiles.put(pos, new Tile("block1", pos));
             *
             * pos = new Vector2(16*16, j*16); this.tiles.put(pos, new
             * Tile("block1", pos)); }
             *
             * // grass in middle for (int i = 4; i < 6; i++) { for (int j = 4; j
             * < 6; j++) { // hopefully will overwrite? pos = new Vector2(i*16,
             * j*16); this.tiles.put(pos, new Tile("grass1", pos)); } }
             *
             * // ledge pos = new Vector2(4*16, 2*16); this.tiles.put(pos, new
             * Tile("ledge1_down", pos)); // ledge pos = new Vector2(3*16, 3*16);
             * this.tiles.put(pos, new Tile("ledge1_right", pos)); // ledge pos =
             * new Vector2(4*16, 4*16); this.tiles.put(pos, new
             * Tile("ledge1_left", pos));
             */

            // pos = new Vector2(16, 16);
            // this.tiles.put(pos, new Tile("ground2", pos));

            // set route
            // in future, need tiles to change route
            // this.currRoute = new Route("Route 1", 20);
            this.currRoute = new Route("Demo_SteelRoute", 20); // handled in
                                                                // other actions

        }

        else if (mapName == "Demo_Legendary1") {
            for (int i = -3; i < 8; i++) {
                pos = new Vector2(-32, i * 32 - 16);
                this.tiles.put(pos.cpy(), new Tile("tree_large1", pos.cpy()));
                this.tiles.put(pos.cpy().add(16, 0), new Tile(
                        "tree_large1_noSprite", pos.cpy().add(16, 0)));
                this.tiles.put(pos.cpy().add(0, 16), new Tile(
                        "tree_large1_noSprite", pos.cpy().add(0, 16)));
                this.tiles.put(pos.cpy().add(16, 16), new Tile(
                        "tree_large1_noSprite", pos.cpy().add(16, 16)));
                pos = new Vector2(-64, i * 32 - 16);
                this.tiles.put(pos.cpy(), new Tile("tree_large1", pos.cpy()));
                this.tiles.put(pos.cpy().add(16, 0), new Tile(
                        "tree_large1_noSprite", pos.cpy().add(16, 0)));
                this.tiles.put(pos.cpy().add(0, 16), new Tile(
                        "tree_large1_noSprite", pos.cpy().add(0, 16)));
                this.tiles.put(pos.cpy().add(16, 16), new Tile(
                        "tree_large1_noSprite", pos.cpy().add(16, 16)));
                pos = new Vector2(16, i * 32 - 16);
                this.tiles.put(pos.cpy(), new Tile("tree_large1", pos.cpy()));
                this.tiles.put(pos.cpy().add(16, 0), new Tile(
                        "tree_large1_noSprite", pos.cpy().add(16, 0)));
                this.tiles.put(pos.cpy().add(0, 16), new Tile(
                        "tree_large1_noSprite", pos.cpy().add(0, 16)));
                this.tiles.put(pos.cpy().add(16, 16), new Tile(
                        "tree_large1_noSprite", pos.cpy().add(16, 16)));
                pos = new Vector2(48, i * 32 - 16);
                this.tiles.put(pos.cpy(), new Tile("tree_large1", pos.cpy()));
                this.tiles.put(pos.cpy().add(16, 0), new Tile(
                        "tree_large1_noSprite", pos.cpy().add(16, 0)));
                this.tiles.put(pos.cpy().add(0, 16), new Tile(
                        "tree_large1_noSprite", pos.cpy().add(0, 16)));
                this.tiles.put(pos.cpy().add(16, 16), new Tile(
                        "tree_large1_noSprite", pos.cpy().add(16, 16)));
                pos = new Vector2(80, i * 32 - 16);
                this.tiles.put(pos.cpy(), new Tile("tree_large1", pos.cpy()));
                this.tiles.put(pos.cpy().add(16, 0), new Tile(
                        "tree_large1_noSprite", pos.cpy().add(16, 0)));
                this.tiles.put(pos.cpy().add(0, 16), new Tile(
                        "tree_large1_noSprite", pos.cpy().add(0, 16)));
                this.tiles.put(pos.cpy().add(16, 16), new Tile(
                        "tree_large1_noSprite", pos.cpy().add(16, 16)));
            }

            pos = new Vector2(0, -16);
            this.tiles.put(pos, new Tile("block1", pos));
            pos = new Vector2(0, 32 * 6 - 16);
            this.tiles.put(pos, new Tile("block1", pos));

            pos = new Vector2(0, 32 * 5);

            // choose randomly between raikou, entei and suicune as legendary
            // pkmn to appear
            int randNum = this.rand.nextInt(3); // 0,1,2
            if (randNum == 0) {
                this.tiles.put(pos, new RaikouTile(pos));
            } else if (randNum == 1) {
                this.tiles.put(pos, new EnteiTile(pos));
            } else {
                this.tiles.put(pos, new SuicuneTile(pos));
            }

            this.currRoute = new Route("Route 1", 20);
        }

        else if (mapName.equals("SpecialMewtwo")) {
//            for (int i = -3; i < 8; i++) {
//                pos = new Vector2(-32, i * 32 - 16);
//                this.tiles.put(pos.cpy(), new Tile("tree_large1", pos.cpy()));
//                this.tiles.put(pos.cpy().add(16, 0), new Tile(
//                        "tree_large1_noSprite", pos.cpy().add(16, 0)));
//                this.tiles.put(pos.cpy().add(0, 16), new Tile(
//                        "tree_large1_noSprite", pos.cpy().add(0, 16)));
//                this.tiles.put(pos.cpy().add(16, 16), new Tile(
//                        "tree_large1_noSprite", pos.cpy().add(16, 16)));
//                pos = new Vector2(-64, i * 32 - 16);
//                this.tiles.put(pos.cpy(), new Tile("tree_large1", pos.cpy()));
//                this.tiles.put(pos.cpy().add(16, 0), new Tile(
//                        "tree_large1_noSprite", pos.cpy().add(16, 0)));
//                this.tiles.put(pos.cpy().add(0, 16), new Tile(
//                        "tree_large1_noSprite", pos.cpy().add(0, 16)));
//                this.tiles.put(pos.cpy().add(16, 16), new Tile(
//                        "tree_large1_noSprite", pos.cpy().add(16, 16)));
//                pos = new Vector2(16, i * 32 - 16);
//                this.tiles.put(pos.cpy(), new Tile("tree_large1", pos.cpy()));
//                this.tiles.put(pos.cpy().add(16, 0), new Tile(
//                        "tree_large1_noSprite", pos.cpy().add(16, 0)));
//                this.tiles.put(pos.cpy().add(0, 16), new Tile(
//                        "tree_large1_noSprite", pos.cpy().add(0, 16)));
//                this.tiles.put(pos.cpy().add(16, 16), new Tile(
//                        "tree_large1_noSprite", pos.cpy().add(16, 16)));
//                pos = new Vector2(48, i * 32 - 16);
//                this.tiles.put(pos.cpy(), new Tile("tree_large1", pos.cpy()));
//                this.tiles.put(pos.cpy().add(16, 0), new Tile(
//                        "tree_large1_noSprite", pos.cpy().add(16, 0)));
//                this.tiles.put(pos.cpy().add(0, 16), new Tile(
//                        "tree_large1_noSprite", pos.cpy().add(0, 16)));
//                this.tiles.put(pos.cpy().add(16, 16), new Tile(
//                        "tree_large1_noSprite", pos.cpy().add(16, 16)));
//                pos = new Vector2(80, i * 32 - 16);
//                this.tiles.put(pos.cpy(), new Tile("tree_large1", pos.cpy()));
//                this.tiles.put(pos.cpy().add(16, 0), new Tile(
//                        "tree_large1_noSprite", pos.cpy().add(16, 0)));
//                this.tiles.put(pos.cpy().add(0, 16), new Tile(
//                        "tree_large1_noSprite", pos.cpy().add(0, 16)));
//                this.tiles.put(pos.cpy().add(16, 16), new Tile(
//                        "tree_large1_noSprite", pos.cpy().add(16, 16)));
//            }
//
//            pos = new Vector2(0, -16);
//            this.tiles.put(pos, new Tile("block1", pos));
//            pos = new Vector2(0, 32 * 6 - 16);
//            this.tiles.put(pos, new Tile("block1", pos));

//            pos = new Vector2(0, 32 * 5);
            pos = new Vector2(0, 16 * 5);

            // tile to trigger special mewtwo battle
//            this.tiles.put(pos, new SpecialMewtwoTile(pos));
            this.tiles.put(pos.cpy(), new Tile("blank", "mewtwo_overworld", pos.cpy(), true, null));

            this.currRoute = new Route("Route 1", 20);
        }
        else {
            this.currRoute = new Route("Demo_SteelRoute", 20);
        }
    }

    /**
     * Fix surrounding tiles based on the tile just added
     * Ex: fence tile touching another fence above and below
     * 
     * TODO: remove the isoverworld flag (refactor)
     */
    public void adjustSurroundingTiles(Tile currTile) {
        this.adjustSurroundingTiles(currTile, this.tiles);
    }
    
    public void adjustSurroundingTiles(Tile currTile, Map<Vector2, Tile> currTiles) {
        this.adjustSurroundingTiles(currTile, currTiles, currTiles == this.overworldTiles);
    }

    public void adjustSurroundingTiles(Tile currTile, Map<Vector2, Tile> currTiles, boolean isOverworld) {
        Vector2 pos = new Vector2();
        Tile tile;
        for (int i = -16; i < 17; i+=16) {
            for (int j = -16; j < 17; j+=16) {
                pos.set(currTile.position.cpy().add(i, j));
                tile = currTiles.get(pos);
                if (tile == null) {
                    continue;
                }
                this.adjustTile(tile, currTiles, isOverworld);
            }
        }
    }

    public void adjustTile(Tile currTile, Map<Vector2, Tile> currTiles) {
        this.adjustTile(currTile, currTiles, currTiles == this.overworldTiles);
    }

    /**
     * Find which sides surrounded on. Replace overSprite based on that.
     * Ex: fence surrounded by 4 fence posts becomes a 4-cross fence.
     */
    public void adjustTile(Tile currTile, Map<Vector2, Tile> currTiles, boolean isOverworld) {
        Tile up = currTiles.get(currTile.position.cpy().add(0, 16));
        Tile down = currTiles.get(currTile.position.cpy().add(0, -16));
        Tile right = currTiles.get(currTile.position.cpy().add(16, 0));
        Tile left = currTiles.get(currTile.position.cpy().add(-16, 0));
//        String path = "tiles/";
//        if (currTile.name.contains("house")) {
//            path = "tiles/buildings/";
//        }
        String[] tokens = currTile.nameUpper.split("_");
        String name;
        name = tokens[0];
        for (int i = 1; i < tokens.length-1; i++) {
            name += "_" + tokens[i];
        }
//        String name = currTile.nameUpper.split("_", 2)[0];
//        String name = currTile.nameUpper.split("_")[0];
//        System.out.println(name);
        boolean touchUp = up != null && up.nameUpper.contains(name);
        boolean touchDown = down != null && down.nameUpper.contains(name);
        boolean touchRight = right != null && right.nameUpper.contains(name);
        boolean touchLeft = left != null && left.nameUpper.contains(name);
        String ext = "_";
        HashMap<Vector2, Tile> interiorTiles = this.interiorTiles.get(this.interiorTilesIndex);

        if (isOverworld) {

            // Ledge stuff
            if (up != null && up.nameUpper.equals("ledges3_E") && 
                right != null && right.nameUpper.equals("ledges3_N")) {
                Tile newTile = new Tile(currTile.name, "ledges3_NE", 
                                        currTile.position.cpy(), true,
                                        currTile.routeBelongsTo);
                newTile.items = currTile.items;
                newTile.hasItem = currTile.hasItem;
                newTile.hasItemAmount = currTile.hasItemAmount;
                currTiles.put(currTile.position.cpy(), newTile);
            }
            else if (up != null && up.nameUpper.equals("ledges3_W") && 
                     left != null && left.nameUpper.equals("ledges3_N")) {
                Tile newTile = new Tile(currTile.name, "ledges3_NW", 
                                        currTile.position.cpy(), true,
                                        currTile.routeBelongsTo);
                newTile.items = currTile.items;
                newTile.hasItem = currTile.hasItem;
                newTile.hasItemAmount = currTile.hasItemAmount;
                currTiles.put(currTile.position.cpy(), newTile);
            }
            else if (down != null && down.nameUpper.equals("ledges3_W") && 
                     left != null && left.nameUpper.equals("ledges3_S")) {
               Tile newTile = new Tile(currTile.name, "ledges3_SW", 
                                       currTile.position.cpy(), true,
                                       currTile.routeBelongsTo);
               newTile.items = currTile.items;
               newTile.hasItem = currTile.hasItem;
               newTile.hasItemAmount = currTile.hasItemAmount;
               currTiles.put(currTile.position.cpy(), newTile);
           }
            else if (down != null && down.nameUpper.equals("ledges3_E") && 
                     right != null && right.nameUpper.equals("ledges3_S")) {
                Tile newTile = new Tile(currTile.name, "ledges3_SE", 
                                        currTile.position.cpy(), true,
                                        currTile.routeBelongsTo);
                newTile.items = currTile.items;
                newTile.hasItem = currTile.hasItem;
                newTile.hasItemAmount = currTile.hasItemAmount;
                currTiles.put(currTile.position.cpy(), newTile);
            }
            
            if (currTile.nameUpper.contains("picture")) {
                return;
            }
            else if (currTile.nameUpper.contains("house_plant")) {
                return;
            }
            else if (currTile.nameUpper.contains("house_gym")) {
                return;
            }
//            else if (currTile.nameUpper.contains("window")) {
//                return;
//            }
            else if (currTile.nameUpper.contains("hole")) {
                // TODO: this is clunky
                touchUp = touchUp || (up != null && (up.name.contains("hole") || up.nameUpper.contains("hole")));
                touchDown = touchDown || (down != null && (down.name.contains("hole") || down.nameUpper.contains("hole")));
                touchRight = touchRight || (right != null && (right.name.contains("hole") || right.nameUpper.contains("hole")));
                touchLeft = touchLeft || (left != null && (left.name.contains("hole") || left.nameUpper.contains("hole")));
                
                touchUp = touchUp || (up != null && (up.name.contains("water") || up.nameUpper.contains("water")));
                touchDown = touchDown || (down != null && (down.name.contains("water") || down.nameUpper.contains("water")));
                touchRight = touchRight || (right != null && (right.name.contains("water") || right.nameUpper.contains("water")));
                touchLeft = touchLeft || (left != null && (left.name.contains("water") || left.nameUpper.contains("water")));
                
                // This is an attempt to make holes look better when touching diagonally
                // 1 == NE, 2 == SE, 3 == SW, 4 == SW
                Tile NE = currTiles.get(currTile.position.cpy().add(16, 16));
                Tile SE = currTiles.get(currTile.position.cpy().add(16, -16));
                Tile SW = currTiles.get(currTile.position.cpy().add(-16, -16));
                Tile NW = currTiles.get(currTile.position.cpy().add(-16, 16));
                boolean touchNE = (NE != null && (NE.name.contains("water") || NE.nameUpper.contains("water") || NE.nameUpper.contains("hole")));
                boolean touchSE = (SE != null && (SE.name.contains("water") || SE.nameUpper.contains("water") || SE.nameUpper.contains("hole")));
                boolean touchSW = (SW != null && (SW.name.contains("water") || SW.nameUpper.contains("water") || SW.nameUpper.contains("hole")));
                boolean touchNW = (NW != null && (NW.name.contains("water") || NW.nameUpper.contains("water") || NW.nameUpper.contains("hole")));

                if (touchUp) {
                    ext += "N";
                }
                if (touchDown) {
                    ext += "S";
                }
                if (touchRight) {
                    ext += "E";
                }
                if (touchLeft) {
                    ext += "W";
                }
                if (ext.equals("_")) {
                    ext = "";
                }
                else {
                    if (touchNE && touchUp && touchRight) {
                        ext += "[NE]";
                    }
                    if (touchSE && touchDown && touchRight) {
                        ext += "[SE]";
                    }
                    if (touchSW && touchDown && touchLeft) {
                        ext += "[SW]";
                    }
                    if (touchNW && touchUp && touchLeft) {
                        ext += "[NW]";
                    }
                }
                Tile newTile = new Tile(currTile.name, name+ext, 
                                        currTile.position.cpy(), true, currTile.routeBelongsTo);
                newTile.items = currTile.items;
                newTile.hasItem = currTile.hasItem;  // TODO: this needs to be done everywhere
                // ideally just replace name and nameUpper and re-call tile init().
                newTile.hasItemAmount = currTile.hasItemAmount;
                currTiles.put(currTile.position.cpy(), newTile);
            }
            else if (currTile.nameUpper.contains("fence") && !currTile.nameUpper.contains("house")) {
                if (up == null || down == null || right == null || left == null) {
                    return;
                }
                // Bend fences towards houses?
                if (down.nameUpper.contains("house")) {  // && !down.nameUpper.contains("roof")
                    touchDown = true;
                }
                if (left.nameUpper.contains("house") && !left.nameUpper.contains("roof")) {
                    touchLeft = true;
                }
                if (right.nameUpper.contains("house") && !right.nameUpper.contains("roof")) {
                    touchRight = true;
                }
                // Only applies to non-desert fence atm
                if (!currTile.nameUpper.contains("fence2")) {
                    // bend fences towards houses?
                    if (up.nameUpper.contains("house")) { //  && !up.nameUpper.contains("roof")
                        touchUp = true;
                    }
                }
                if (!touchUp && !touchDown && !touchLeft && !touchRight) {
                    return;
                }
                if (touchUp) {
                    ext += "N";
                }
                if (touchDown) {
                    ext += "S";
                }
                if (touchRight) {
                    ext += "E";
                }
                if (touchLeft) {
                    ext += "W";
                }
                if (touchLeft && touchRight && !touchUp && !touchDown) {
                    ext = "";
                }
                if ((touchUp || touchDown) && !touchLeft && !touchRight) {
                    ext = "_NS";
                }
                if (ext.length() == 2) {
                    ext = "";
                }
                Tile newTile = new Tile(currTile.name, name+ext, 
                                        currTile.position.cpy(), true, currTile.routeBelongsTo);
                newTile.items = currTile.items;  // TODO: problems? idk
                currTiles.put(currTile.position.cpy(), newTile);
        //        text = TextureCache.get(Gdx.files.internal(path+currTile.nameUpper+ext+".png"));
        //        Vector2 pos = new Vector2(currTile.overSprite.getX(), currTile.overSprite.getY());
        //        currTile.overSprite = new Sprite(text, 0, 0, 16, 16);
        //        currTile.overSprite.setPosition(pos.x, pos.y);
            }
            else if (currTile.nameUpper.contains("house") &&
                     !currTile.nameUpper.contains("roof") &&
                     !currTile.nameUpper.contains("door")) {
                if (currTile.nameUpper.equals("house5_middle1") && down.nameUpper.contains("fence1") &&
                    !(touchUp ^ touchDown)) {
                    Tile newTile = new Tile(currTile.name, "house5_middle1_fence1", 
                                            currTile.position.cpy(), true, currTile.routeBelongsTo);
                    newTile.items = currTile.items;
                    currTiles.put(currTile.position.cpy(), newTile);
                    // Add to interiors
                    // TODO: test
                    if (!interiorTiles.containsKey(currTile.position)) {
                        Tile interiorTile = new Tile("house5_floor1", currTile.position.cpy());
                        interiorTiles.put(currTile.position.cpy(), interiorTile);
                    }
                    return;
                }

                // TODO: unify this with house5
                if (currTile.nameUpper.contains("house6")) {
//                    name = currTile.nameUpper.split("_")[0];
//                    System.out.println(name);
                    touchUp = up != null && up.nameUpper.contains(name) && !up.nameUpper.contains("roof");
                    touchDown = down != null && down.nameUpper.contains(name) && !down.nameUpper.contains("roof");
                    touchRight = right != null && right.nameUpper.contains(name) && !right.nameUpper.contains("roof");
                    touchLeft = left != null && left.nameUpper.contains(name) && !left.nameUpper.contains("roof");

//                    if (!touchLeft && !touchRight) {
//                        return;
//                    }
                    if (touchUp) {
                        ext += "N";
                    }
                    if (touchDown) {
                        ext += "S";
                    }
                    if (touchRight) {
                        ext += "E";
                    }
                    if (touchLeft) {
                        ext += "W";
                    }
                    if (touchLeft && touchRight && !touchUp && !touchDown) {
                        ext = "_NEW";
                    }
                    if (ext.length() <= 2) {
                        ext += "N";
                    }
                    Tile newTile = new Tile(currTile.name, name+ext, 
                                            currTile.position.cpy(), true, currTile.routeBelongsTo);
                    newTile.items = currTile.items;
                    currTiles.put(currTile.position.cpy(), newTile);

                    // Add to interiors if nothing there currently
                    if (!interiorTiles.containsKey(currTile.position)) {
                        Tile interiorTile = new Tile("house5_floor1", currTile.position.cpy());
                        interiorTiles.put(currTile.position.cpy(), interiorTile);
                    }
                    return;
                }
                else if (currTile.nameUpper.contains("house7")) {
                    if (touchUp) {
                        ext += "N";
                    }
                    if (touchDown) {
                        ext += "S";
                    }
                    if (touchRight) {
                        ext += "E";
                    }
                    if (touchLeft) {
                        ext += "W";
                    }
                    if (ext.length() < 2) {
                        ext += "S";
                    }
                    System.out.println(name+ext);
                    System.out.println("");
                    Tile newTile = new Tile(currTile.name, name+ext, 
                                            currTile.position.cpy(), true,
                                            currTile.routeBelongsTo);
                    newTile.items = currTile.items;
                    currTiles.put(currTile.position.cpy(), newTile);

                    // Add to interiors if nothing there currently
                    if (!interiorTiles.containsKey(currTile.position)) {
                        Tile interiorTile = new Tile("house5_floor1", currTile.position.cpy());
                        interiorTiles.put(currTile.position.cpy(), interiorTile);
                    }
                    return;
                }
                
                if (!touchLeft && !touchRight) {
                    return;
                }
                if (touchRight && touchLeft) {
                    ext += "middle1";
                }
                else if (touchRight && touchDown) {
                    ext += "E";
                }
                else if (touchLeft && touchDown) {
                    ext += "W";
                }
                else if (touchRight) {
                    ext += "left1";
                }
                else if (touchLeft) {
                    ext += "right1";
                }
                Tile newTile = new Tile(currTile.name, name+ext, 
                                        currTile.position.cpy(), true, currTile.routeBelongsTo);
                newTile.items = currTile.items;
                currTiles.put(currTile.position.cpy(), newTile);

                // Add to interiors if nothing there currently
                if (!interiorTiles.containsKey(currTile.position)) {
                    Tile interiorTile = new Tile("house5_floor1", currTile.position.cpy());
                    interiorTiles.put(currTile.position.cpy(), interiorTile);
                }
            }
            else if (currTile.nameUpper.contains("house") &&
                     currTile.nameUpper.contains("roof")) {
                if (!touchUp && !touchDown && !touchLeft && !touchRight) {
                    return;
                }
                if (touchUp) {
                    ext += "N";
                }
                if (touchDown) {
                    ext += "S";
                }
                if (touchRight) {
                    ext += "E";
                }
                if (touchLeft) {
                    ext += "W";
                }
                if (touchLeft && touchRight && !touchUp && !touchDown) {
                    ext = "_middle1";
                }
                if ((touchUp || touchDown) && !touchLeft && !touchRight) {
                    ext = "_NSEW";
                }
                if (ext.length() == 2) {
                    ext = "";
                    if (touchLeft) {
                        ext = "_right1";
                    }
                    else if (touchRight) {
                        ext = "_left1";
                    }
                }

                // TODO: clean this stuff up
                if (currTile.nameUpper.contains("house7")) {
                    ext = "_";
                    if (touchUp) {
                        ext += "N";
                    }
                    if (touchDown) {
                        ext += "S";
                    }
                    if (touchRight) {
                        ext += "E";
                    }
                    if (touchLeft) {
                        ext += "W";
                    }
                }
                
                
                Tile newTile = new Tile(currTile.name, name+ext, 
                                        currTile.position.cpy(), true,
                                        currTile.routeBelongsTo);
                newTile.items = currTile.items;
                currTiles.put(currTile.position.cpy(), newTile);
                // Add to interiors
//                HashMap<Vector2, Tile> currInterior = game.map.interiorTiles.get(game.map.interiorTilesIndex);
                // Allowed to overwrite house back walls.
                if (!interiorTiles.containsKey(currTile.position) ||
                    interiorTiles.get(currTile.position).name.contains("wall")) {
                    Tile interiorTile = new Tile("house5_floor1", currTile.position.cpy());
                    interiorTiles.put(currTile.position.cpy(), interiorTile);
                }
                if (!interiorTiles.containsKey(up.position) || interiorTiles.get(up.position) == null) {
                    interiorTiles.put(up.position.cpy(), new Tile("house5_wall1", up.position.cpy()));
                }
            }
            else if (currTile.nameUpper.contains("door")) {
                if (right != null && left != null) {
                    // Handle case where player puts a door on the roof, which should
                    // make a 'back door' (red carpet on back of house that acts as door).
                    if ((left.nameUpper.contains("roof") || left.nameUpper.contains("door")) &&
                        (right.nameUpper.contains("roof") || right.nameUpper.contains("door")) &&
                        !(left.nameUpper.contains("door") && right.nameUpper.contains("door"))) {
                        String name2 = "house5_roof_middle1";
                        if (currTile.nameUpper.contains("house6")) {
                            name2 = "house6_roof_middle1";
                        }
                        else if (currTile.nameUpper.contains("house7")) {
                            name2 = "house7_roof_S";
                        }
                        Tile newTile =  new Tile(currTile.name, name2, 
                                                 currTile.position.cpy(), true,
                                                 currTile.routeBelongsTo);
                        newTile.items = currTile.items;
                        currTiles.put(currTile.position.cpy(), newTile);
                        this.adjustSurroundingTiles(currTile, currTiles);
                        if (!up.attrs.get("solid")) {
                            currTiles.put(up.position.cpy(),
                                               new Tile("rug2", "", up.position.cpy(), true, up.routeBelongsTo));
                            // Add to interiors
                            if (!interiorTiles.containsKey(up.position) ||
                                interiorTiles.get(up.position).name.contains("wall")) {
                                Tile interiorTile = new Tile("house5_door1", up.position.cpy());
                                interiorTiles.put(up.position.cpy(), interiorTile);
                            }
                        }
                        // Remove indoor rug if it exists
                        if (interiorTiles.containsKey(currTile.position) &&
                            interiorTiles.get(currTile.position).name.contains("rug")) {
                            Tile interiorTile = new Tile("house5_floor1", currTile.position.cpy());
                            interiorTiles.put(currTile.position.cpy(), interiorTile);
                        }
                        return;
                    }
                    // If player builds next to fences, turn the door into a 'gate'
                    if ((left.nameUpper.contains("fence") || left.nameUpper.contains("door")) &&
                        (right.nameUpper.contains("fence") || right.nameUpper.contains("door"))) {
                        String fenceName = "fence1";
                        if (name.contains("house6")) {
                            fenceName = "fence2";
                        }
                        Tile newTile =  new Tile(currTile.name, fenceName+"gate1", 
                                                 currTile.position.cpy(), true,
                                                 currTile.routeBelongsTo);
                        newTile.items = currTile.items;
                        currTiles.put(currTile.position.cpy(), newTile);
                        this.adjustSurroundingTiles(currTile, currTiles);
                        return;
                    }
                }
                if (up != null && down != null) {
                    if ((up.nameUpper.contains("fence") || up.nameUpper.contains("door")) &&
                            (down.nameUpper.contains("fence") || down.nameUpper.contains("door"))) {
                            String fenceName = "fence1";
                            if (name.contains("house6")) {
                                fenceName = "fence2";
                            }
                            Tile newTile =  new Tile(currTile.name, fenceName+"gate1_NS",
                                                     currTile.position.cpy(), true,
                                                     currTile.routeBelongsTo);
                            newTile.items = currTile.items;
                            currTiles.put(currTile.position.cpy(), newTile);
                            this.adjustSurroundingTiles(currTile, currTiles);
                            return;
                        }
                        // Side doors
                        if ((up.nameUpper.contains("roof") || up.nameUpper.contains("E") || up.nameUpper.contains("W") || up.nameUpper.contains("left") || up.nameUpper.contains("right") || up.nameUpper.contains("door")) &&
                            (down.nameUpper.contains("roof") || down.nameUpper.contains("E") || down.nameUpper.contains("W") || down.nameUpper.contains("left") || down.nameUpper.contains("right") || down.nameUpper.contains("door")) &&
                            !(up.nameUpper.contains("door") && down.nameUpper.contains("door"))) {
                            //
                            int roofCount = 0;
                            if (right != null && right.nameUpper.contains("roof")) {
                                roofCount++;
                            }
                            if (left != null && left.nameUpper.contains("roof")) {
                                roofCount++;
                            }
                            String name2 = "house5_middle1";
                            if (roofCount > 0) {
                                name2 = "house5_roof_middle1";
                            }
                            if (name.contains("house6")) {
                                name2 = "house6_NS";
                                if (roofCount > 0) {
                                    name2 = "house6_roof_NS";
                                }
                            }
                            else if (name.contains("house7")) {
                                name2 = "house7_wall_NS";
                                if (roofCount > 0) {
                                    name2 = "house7_roof_NS";
                                }
                            }
                            Tile newTile =  new Tile(currTile.name, name2, 
                                                     currTile.position.cpy(), true,
                                                     currTile.routeBelongsTo);
                            newTile.items = currTile.items;
                            this.tiles.put(currTile.position.cpy(), newTile);
                            this.adjustSurroundingTiles(currTile);
                            //
                            if (!left.attrs.get("solid")) {
                                this.tiles.put(left.position.cpy(), new Tile("rug2_right", "", left.position.cpy(), true, left.routeBelongsTo));
                                // Add to interiors
                                Tile interiorTile = interiorTiles.get(currTile.position);
                                if (interiorTile != null && interiorTile.name.contains("floor")) {
                                    interiorTile = new Tile("rug2_left", currTile.position.cpy());
                                    interiorTiles.put(currTile.position.cpy(), interiorTile);
                                }
                            }
                            else if (!right.attrs.get("solid")) {
                                this.tiles.put(right.position.cpy(), new Tile("rug2_left", "", right.position.cpy(), true, right.routeBelongsTo));
                                // Add to interiors
                                Tile interiorTile = interiorTiles.get(currTile.position);
                                if (interiorTile != null && interiorTile.name.contains("floor")) {
                                    interiorTile = new Tile("rug2_right", currTile.position.cpy());
                                    interiorTiles.put(currTile.position.cpy(), interiorTile);
                                }
                            }
                            return;
                        }
                }
                
                // Add to interiors
                if (!interiorTiles.containsKey(currTile.position)) {
                    Tile interiorTile = new Tile("house5_floor_rug1", currTile.position.cpy());
                    interiorTiles.put(currTile.position.cpy(), interiorTile);
                }
            }
        }
        else {
            
            // TODO: tables for now
            if (currTile.nameUpper.contains("table")) {
                if (!touchUp && !touchDown && !touchLeft && !touchRight) {
                    return;
                }
                if (touchUp) {
                    ext += "N";
                }
                if (touchDown) {
                    ext += "S";
                }
                if (touchRight) {
                    ext += "E";
                }
                if (touchLeft) {
                    ext += "W";
                }
                if (ext.equals("_E")) {
                    ext = "_left1";
                }
                else if (ext.equals("_W")) {
                    ext = "_right1";
                }
                else if (ext.equals("_EW")) {
                    ext = "_middle1";
                }
                else if (ext.equals("_NS")) {
                    ext = "_middle2";
                }
                else if (ext.equals("_N")) {
                    ext = "_down1";
                }
                else if (ext.equals("_S")) {
                    ext = "_up1";
                }
                Tile newTile = new Tile(currTile.name, name+ext, 
                                        currTile.position.cpy(), true,
                                        currTile.routeBelongsTo);
                newTile.items = currTile.items;
                currTiles.put(currTile.position.cpy(), newTile);
            }

            else if (currTile.nameUpper.contains("interiorwall")) {
                if (!touchUp && !touchDown && !touchLeft && !touchRight) {
                    return;
                }
                // TODO: only adjust after connecting something to the wall.
                if (touchUp || up.name.contains("wall")) {
                    ext += "N";
                }
                if (touchDown) {
                    ext += "S";
                }
                if (touchRight || right == null) {
                    ext += "E";
                }
                if (touchLeft || left == null) {
                    ext += "W";
                }
                Tile newTile = new Tile(currTile.name, name+ext, 
                                        currTile.position.cpy(), true,
                                        currTile.routeBelongsTo);
                newTile.items = currTile.items;
                currTiles.put(currTile.position.cpy(), newTile);
            }

            // TODO: move to exteriors, maybe
            else if (currTile.name.contains("ruins1")) {
                
                if (currTile.name.contains("pillar")) {
                    return;
                }

                // TODO: have to use name instead of nameUpper
                tokens = currTile.name.split("_");
                name = tokens[0];
                for (int i = 1; i < tokens.length-1; i++) {
                    name += "_" + tokens[i];
                }

                touchUp = up != null && up.name.contains(name);
                touchDown = down != null && down.name.contains(name);
                touchRight = right != null && right.name.contains(name);
                touchLeft = left != null && left.name.contains(name);

                if (touchUp) {
                    ext += "N";
                }
                if (touchDown) {
                    ext += "S";
                }
                if (touchRight) {
                    ext += "E";
                }
                if (touchLeft) {
                    ext += "W";
                }
                Tile newTile = new Tile(name+ext, currTile.position.cpy(), true, currTile.routeBelongsTo);
                newTile.items = currTile.items;
                currTiles.put(currTile.position.cpy(), newTile);
            }
        }
    }

    public void loadFromFile(Game game) {
        // If map exists as file, load it
//        System.out.println(this.id);
        try {
            // TODO: tile Route is not saved here.
            // Should be able to serialize route pokemon and save. Although might be ineffecient to save all
            //  pokemon details.
            InputStream inputStream = new InflaterInputStream(new FileInputStream(this.id + ".sav"));
            com.esotericsoftware.kryo.io.Input input = new com.esotericsoftware.kryo.io.Input(inputStream);
//            com.esotericsoftware.kryo.io.Input input = new com.esotericsoftware.kryo.io.Input(new FileInputStream(this.id + ".sav"));  // uncompressed
            
            Network.SaveData saveData = game.server.getKryo().readObject(input, Network.SaveData.class);
//            Network.SaveData saveData = Network.SaveData.get(game, input);
            input.close();
            this.tiles.clear();
            this.bottomLeft = new Vector2();
            this.topRight = new Vector2();
            // Load map tiles
            HashMap<String, Route> loadedRoutes = new HashMap<String, Route>();
            for (Network.TileData tileData : saveData.mapTiles.tiles) {
                // store unique routes as hashmap ClassID->Route
                if (tileData.routeBelongsTo != null && !loadedRoutes.containsKey(tileData.routeBelongsTo)) {
                    loadedRoutes.put(tileData.routeBelongsTo, new Route(saveData.mapTiles.routes.get(tileData.routeBelongsTo)));
                }
                Route tempRoute = loadedRoutes.get(tileData.routeBelongsTo);
                Tile newTile = Tile.get(tileData, tempRoute);
                this.tiles.put(tileData.pos.cpy(), newTile);

                // Load burrowed trapinch
                if (newTile.items != null && newTile.items.containsKey("trapinch")) {
                    Pokemon trapinch = new Pokemon("trapinch", 22, Pokemon.Generation.CRYSTAL);
                    trapinch.isTrapping = true;
                    trapinch.position = newTile.position.cpy();
                    // TODO: test, assuming overworld for now.
                    trapinch.mapTiles = game.map.tiles;
                    game.insertAction(trapinch.new Burrowed());
                }

                if (this.topRight.x < tileData.pos.x) {
                    this.topRight.x = tileData.pos.x;
                }
                if (this.topRight.y < tileData.pos.y) {
                    this.topRight.y = tileData.pos.y;
                }
                if (this.bottomLeft.x > tileData.pos.x) {
                    this.bottomLeft.x = tileData.pos.x;
                }
                if (this.bottomLeft.y > tileData.pos.y) {
                    this.bottomLeft.y = tileData.pos.y;
                }
            }
            // Load interior tiles
            this.interiorTiles.clear();
            for (HashMap<Vector2, Network.TileData> tileDatas : saveData.mapTiles.interiorTiles) {
                HashMap<Vector2, Tile> tiles = null;
                if (tileDatas != null) {
                    tiles = new HashMap<Vector2, Tile>();
                    for (Network.TileData tileData : tileDatas.values()) {
                        if (tileData.routeBelongsTo == null && tileData.tileName.contains("pkmnmansion") && !tileData.tileName.contains("stairs")) {
//                            System.out.println(tileData.tileName);
//                            System.out.println(tileData.routeBelongsTo);
                            // TODO: eventually remove this 'else' block.
                            // pkmnmansion didn't save interior tiles. this is a temp fix to load based on route name
                            Route route = null;
                            tileData.routeBelongsTo = "pkmnmansion_temp";
                            if (!loadedRoutes.containsKey(tileData.routeBelongsTo)) {
                                route = new Route("pkmnmansion1", 30);  // won't vary level but that's okay
                                loadedRoutes.put(tileData.routeBelongsTo, route);
                            }
                        }
                        if (tileData.routeBelongsTo != null && !loadedRoutes.containsKey(tileData.routeBelongsTo)) {
                            loadedRoutes.put(tileData.routeBelongsTo, new Route(saveData.mapTiles.routes.get(tileData.routeBelongsTo)));
                        }
                        Route tempRoute = loadedRoutes.get(tileData.routeBelongsTo);
                        Tile newTile = Tile.get(tileData, tempRoute);
                        tiles.put(newTile.position.cpy(), newTile);

                    }
                }
                this.interiorTiles.add(tiles);
            }
            // Load misc map-related values
            this.interiorTilesIndex = saveData.mapTiles.interiorTilesIndex;
            this.timeOfDay = saveData.mapTiles.timeOfDay;
            CycleDayNight.dayTimer = saveData.mapTiles.dayTimer;
            this.edges = saveData.mapTiles.edges;
            // Load players
            for (Network.PlayerDataBase playerData : saveData.players) {
//                Player player = new Player(Network.PlayerData.get(playerData));  // TODO: remove if unused
                Player player = new Player(playerData); 
                player.type = Player.Type.REMOTE;  // TODO: store in playerData?
                game.players.put(playerData.id, player);
            }
            // Load game.player
//            game.player = new Player(Network.PlayerData.get(saveData.playerData));  // TODO: remove if unused
            game.player = new Player(saveData.playerData);
            for (Pokemon pokemon : game.player.pokemon) {
                // not sure what to do; game.player doesn't exist before
                // the line above
                pokemon.previousOwner = game.player;
            }
            game.cam.position.set(game.player.position.x+16, game.player.position.y, 0);
            game.player.type = Player.Type.LOCAL;
            if (saveData.playerData.isInterior) {
                game.map.tiles = game.map.interiorTiles.get(game.map.interiorTilesIndex);
            }
            if (game.player.currFieldMove.equals("FLY")) {
                // TODO: this is a hack
                PlayerStanding standingAction = null;
                for (Action action : game.actionStack) {
                    if (PlayerStanding.class.isInstance(action)) {
                        standingAction = (PlayerStanding)action;
                        break;
                    }
                }
                game.actionStack.remove(standingAction);
                game.insertAction(game.player.new Flying(game.player.pokemon.get(saveData.playerData.flyingIndex), false, null));
                game.cam.translate(0f, 16f, 0f);
            }
            // Load overworld pokemon
            game.map.pokemon.clear();
            for (Vector2 pos : saveData.overworldPokemon.keySet()) {
                Network.PokemonDataBase pokemonData = saveData.overworldPokemon.get(pos);
                Pokemon pokemon = new Pokemon(pokemonData);
//                System.out.println(pokemon.mapTiles == game.map.overworldTiles);
                pokemon.position = pos.cpy();
                game.map.pokemon.put(pos, pokemon);
                game.insertAction(pokemon.new Standing());
            }
            

            // TODO: remove
            // This will hack data from other saves into current save
//            inputStream = new InflaterInputStream(new FileInputStream("Pogtopia-charmeleon.sav"));
//            input = new com.esotericsoftware.kryo.io.Input(inputStream);
//            saveData = game.server.getKryo().readObject(input, Network.SaveData.class);
//            input.close();
//            // Load game.player
//            Player oldPlayer = new Player(saveData.playerData);
//            Pokemon oldMon = oldPlayer.pokemon.get(0);
//            oldMon.previousOwner = game.player;
//            game.player.pokemon.add(oldMon);

            //TODO: remove if unused
//            Network.MapTiles mapTiles = game.server.getKryo().readObject(input, Network.MapTiles.class);
//            input.close();
//            this.tiles.clear();
//            HashMap<String, Route> loadedRoutes = new HashMap<String, Route>();
//            for (Network.TileData tileData : mapTiles.tiles) {
//                // store unique routes as hashmap ClassID->Route
//                if (tileData.routeBelongsTo != null && !loadedRoutes.containsKey(tileData.routeBelongsTo)) {
//                    loadedRoutes.put(tileData.routeBelongsTo, new Route(mapTiles.routes.get(tileData.routeBelongsTo)));
//                }
//                Route tempRoute = loadedRoutes.get(tileData.routeBelongsTo);
//                this.tiles.put(tileData.pos.cpy(), Tile.get(tileData, tempRoute));
//            }
//            // Load interior tiles
//            this.interiorTiles.clear();
//            for (HashMap<Vector2, Network.TileData> tileDatas : mapTiles.interiorTiles) {
//                HashMap<Vector2, Tile> tiles = null;
//                if (tileDatas != null) {
//                    tiles = new HashMap<Vector2, Tile>();
//                    for (Network.TileData tileData : tileDatas.values()) {
//                        Tile newTile = Tile.get(tileData, null);
//                        tiles.put(newTile.position.cpy(), newTile);
//                    }
//                }
//                this.interiorTiles.add(tiles);
//            }
//            // Load misc map-related values
//            this.interiorTilesIndex = mapTiles.interiorTilesIndex;
//            this.timeOfDay = mapTiles.timeOfDay;
//            CycleDayNight.dayTimer = mapTiles.dayTimer;
//            this.edges = mapTiles.edges;
//
//            // Load players
//            inputStream = new InflaterInputStream(new FileInputStream(this.id + ".players.sav"));
//            input = new com.esotericsoftware.kryo.io.Input(inputStream);
//            ArrayList<Network.PlayerData> players = game.server.getKryo().readObject(input, ArrayList.class);
//            input.close();
//            for (Network.PlayerData playerData : players) {
//                Player player = new Player(playerData);
//                player.type = Player.Type.REMOTE;  // TODO: store in playerData?
//                game.players.put(playerData.id, player);
//            }
//            // Load game.player
//            inputStream = new InflaterInputStream(new FileInputStream(this.id + ".player.sav"));
//            input = new com.esotericsoftware.kryo.io.Input(inputStream);
//            Network.PlayerData playerData = game.server.getKryo().readObject(input, Network.PlayerData.class);
//            input.close();
//            game.player = new Player(playerData);
//            game.cam.position.set(game.player.position.x+16, game.player.position.y, 0);
//            game.player.type = Player.Type.LOCAL;
//            if (playerData.isInterior) {
//                game.map.tiles = game.map.interiorTiles.get(game.map.interiorTilesIndex);
//            }
//            // if this is a server, put player in game.players
//            // this will convert a local save file to a 'hosted' save file
//            //  it won't convert the other way tho.
////            if (game.type == Game.Type.SERVER && ) {
////
////            }
            FileHandle file = Gdx.files.local(this.id + ".png");
            if (file.exists()) {
                this.minimap = new Pixmap(file);
            }
            else {
                // TODO: update this section
                // Save minimap
                int width = (int)(Game.staticGame.map.topRight.x - Game.staticGame.map.bottomLeft.x)/8;
                int height = (int)(Game.staticGame.map.topRight.y - Game.staticGame.map.bottomLeft.y)/8;
                Game.staticGame.map.minimap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
                Game.staticGame.map.minimap.setColor(0, 0, 0, 1);
                Game.staticGame.map.minimap.fill();
                Vector2 startPos = game.player.position.cpy().add(-8*16, -8*16);
                startPos.x = (int)startPos.x - (int)startPos.x % 16;
                startPos.y = (int)startPos.y - (int)startPos.y % 16;
                Vector2 endPos = game.player.position.cpy().add(8*16, 8*16);
                endPos.x = (int)endPos.x - (int)endPos.x % 16;
                endPos.y = (int)endPos.y - (int)endPos.y % 16;
                for (Vector2 currPos = new Vector2(startPos.x, startPos.y); currPos.y < endPos.y;) {
                    Tile tile = Game.staticGame.map.tiles.get(currPos);
                    currPos.x += 16;
                    if (currPos.x > endPos.x) {
                        currPos.x = startPos.x;
                        currPos.y += 16;
                    }
                    if (tile == null) {
                        continue;
                    }
                    tile.updateMiniMap(game);
                }
                //
            }

        } catch (FileNotFoundException e) {
            System.out.println("No save file found for map: " + this.id);
        }
    }
    
    /**
     * Change the map batch color after a period of time.
     */
    public class ShadeEffect extends Action {
        int timer = 0;
        Color color;
        String type;
        
        public ShadeEffect(String type) {
            this(type, 30);
        }
        
        public ShadeEffect(String type, int timer) {
            this.color = new Color(1f, 1f, 1f, 1f);
            if (type.equals("graveyard")) {
//                this.color = new Color(.8f, .8f, 1f, 1f);
                this.color = new Color(.9f, .9f, 1f, 1f);
//                this.color = new Color(1f, 1f, 1f, 1f);
            }
            this.type = type;
            this.timer = timer;
        }

        @Override
        public void firstStep(Game game) {
            game.actionStack.remove(PkmnMap.this.shadeEffect);
            PkmnMap.this.shadeEffect = this;
        }

        @Override
        public void step(Game game) {
            this.timer--;
            if (this.timer < 0) {
                game.mapBatch.setColor(this.color);
                if (this.type.equals("graveyard")) {
                    FogEffect.active = true;
                    FogEffect.refresh = true;
                }
                else {
                    FogEffect.active = false;
                }
                game.actionStack.remove(this);
            }
        }
    }

    /*
     * TODO: for large maps, will likely need to split into multiple files corresponding to
     * different map regions. Then save regions periodically. Ideally only when cpu cycles
     * are available.
     */
    public static class PeriodicSave extends Action {
        float timeDelta = 60;
//        float saveInterval = 10; // TODO: debug, was 60  // Every minute for now
        float saveInterval = 60; // TODO: debug, was 60  // Every minute for now
        
        public static int timeSinceLastSave = 0;

        // TODO: map id's?
        OutputStream outputStream;
        Output output;

        public PeriodicSave(Game game) {
            // Ends up under ...\pokemon_world_gen\Workspace\desktop\<game.map.id>.sav
            // DeflaterOutputStream <- output compression.
//            this.outputStream = new DeflaterOutputStream(new FileOutputStream(game.map.id + ".sav"));
        }

        public int getLayer() { return 500;}

        @Override
        public void step(Game game) {
            PeriodicSave.timeSinceLastSave = 0;
            // TODO: this only works for server atm, because I'm using game.server.getKryo() below.
            // Could change to also use game.server.getKryo() if I need for client.
            if (game.type == Game.Type.CLIENT) {
                return;
            }
            this.timeDelta += Gdx.graphics.getDeltaTime();
            if (this.timeDelta >= this.saveInterval) {
                this.timeDelta = 0f;
                System.out.println("Backing up previous save file...");
                try {
                    // First time game is saved, this path won't exist
                    Files.copy(Paths.get(game.map.id + ".sav"),
                               Paths.get(game.map.id + ".sav.backup"),
                               StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Saving game to file...");
                try {
                    this.outputStream = new DeflaterOutputStream(new FileOutputStream(game.map.id + ".sav"));
//                    this.output = new Output(new FileOutputStream(game.map.id + ".sav"));  // uncompressed
                    this.output = new Output(this.outputStream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                game.server.getKryo().writeObject(this.output, new Network.SaveData(game));
                this.output.close();
                System.out.println("Done.");
                
                // Save minimap to file
                FileHandle file = new FileHandle(game.map.id+".png");
                PixmapIO.writePNG(file, game.map.minimap);
            }
        }
    }

}

class RaikouTile extends Tile {
    public RaikouTile(Vector2 pos) {
        super("raikou_overw1", pos);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onPressA(Game game) {
        game.playerCanMove = false;

        // TODO - player can't move when returning from this
        game.battle.oppPokemon = new Pokemon("Raikou", 50);

        Action encounterAction = new SplitAction(
                new BattleIntro(
                        new BattleIntroAnim1(
                                new SplitAction(
                                        new DrawBattle(game),
                                        new BattleAnimPositionPlayers(
                                                game,
                                                new PlaySound(
                                                        game.battle.oppPokemon.specie.name,
                                                        new DisplayText(
                                                                game,
                                                                "Wild "
                                                                        + game.battle.oppPokemon.nickname
                                                                                .toUpperCase()
                                                                        + " appeared!",
                                                                null,
                                                                null,
                                                                new WaitFrames(
                                                                        game,
                                                                        39,
                                                                        // demo
                                                                        // code
                                                                        // -
                                                                        // wildly
                                                                        // confusing,
                                                                        // but i
                                                                        // don't
                                                                        // want
                                                                        // to
                                                                        // write
                                                                        // another
                                                                        // if
                                                                        // statement
                                                                        game.player.adrenaline > 0 ? new DisplayText(
                                                                                game,
                                                                                ""
                                                                                        + game.player.name
                                                                                        + " has ADRENALINE "
                                                                                        + Integer
                                                                                                .toString(game.player.adrenaline)
                                                                                        + "!",
                                                                                null,
                                                                                null,
                                                                                new PrintAngryEating(
                                                                                        game, // for
                                                                                                // demo
                                                                                                // mode,
                                                                                                // normally
                                                                                                // left
                                                                                                // out
                                                                                        new DrawBattleMenuSafariZone(
                                                                                                game,
                                                                                                null)))
                                                                                : new PrintAngryEating(
                                                                                        game, // for
                                                                                                // demo
                                                                                                // mode,
                                                                                                // normally
                                                                                                // left
                                                                                                // out
                                                                                        new DrawBattleMenuSafariZone(
                                                                                                game,
                                                                                                null))
                                                                //
                                                                ))))))),
                new WaitFrames(game, 10, new BattleIntroMusic(new WaitFrames(
                        game, 100, new DoneWithDemo(game)))));

        game.insertAction(new DisplayText(game, "GROWL!!",
                "Raikou", null,
                // new PlayerCanMove(game,
                encounterAction));
    }
}

class Route {
    String name;
    int level;

    // TODO: not positive that this will be permanently removed.
    // TODO: probably rename to storedPokemon
//    ArrayList<Pokemon> pokemon = new ArrayList<Pokemon>();

    // Still used for headbutt and rock smash
    ArrayList<Pokemon> storedPokemon = new ArrayList<Pokemon>();

//    ArrayList<String> allowedPokemon = null;

    // Try this for now?
    Music music;  // This is still used
    // TODO: remove
//    ArrayList<String> musics = new ArrayList<String>();
    int musicsIndex = 0;

    boolean isDungeon = false;  // some routes require music transition. ie, cave, pokemon mansion

    // Pokemon list to pick from when encountering Pokemon
    // Multiple entries to increase encounter rate
    public static HashMap<String, ArrayList<String>> allowedPokemon = new HashMap<String, ArrayList<String>>();
    // Lazy-loaded in an attempt to save memory
    public static HashMap<String, ArrayList<String>> musics = new HashMap<String, ArrayList<String>>();

    /**
     * NOTE: not doing an encounter table like the games currently. Each 
     * pokemon has an equal chance of being encountered in Wilds. If switching
     * to using an encounter table in the future, do something like:
     * 
     * HashMap<String, Integer> pokemon = HashMap<String, Integer>;  // Integers must add up to 256
     * pokemon.put("bulbasaur", 32);  // 1/8 encounter rate, 32/256 == 1/8
     * pokemon.put("gible", 8);       // 1/32 encounter rate, 8/256 == 1/32
     * ...
     * 
     * Then in checkWildEncounter:
     * 
     * int counter = 0;
     * int randomNum = Game.rand.nextInt(256);
     * for (String name : pokemon.keySet()) {
     *     counter += currRoute.pokemon.get(name);
     *     if (randomNum < counter) {
     *         pokemon = new Pokemon(name, ...);
     *         break;
     *     }
     * }
     * 
     */

    /**
     * Constructor for loading from file or loading over network.
     */
    public Route(Network.RouteData routeData) {
//        this.rand = new Random();  // TODO: delete, should just use single random num generator which can be seeded.
        this.name = routeData.name;
        this.level = routeData.level;
        // TODO: remove, lookup up via static HashMap now
//        this.allowedPokemon = new ArrayList<String>(routeData.allowedPokemon);  
        // TODO: remove, lookup up via static HashMap now
//        this.musics = new ArrayList<String>(routeData.musics);
        this.musicsIndex = routeData.musicsIndex;
        // Still used for headbutt spawns
        for (Network.PokemonDataBase pokemonData : routeData.pokemon) {
            this.storedPokemon.add(new Pokemon(pokemonData));
        }
        
        this.init();
    }

    public Route(String name, int level) {
        this.name = name;
        this.level = level;
        this.init();
        // TODO: remove
//        this.genPokemon(256);
    }
    
    public void init() {
        if (this.name.equals("pkmnmansion1")) {
            this.isDungeon = true;
        }
        if (this.name.equals("fossil_lab1")) {
            this.isDungeon = true;
        }
        if (this.name.equals("regi_cave1")) {
            this.isDungeon = true;
        }
        if (this.name.equals("ruins1_inner")) {
            this.isDungeon = true;
        }
        // TODO: remove
//        if (this.name.equals("desert1") ||
//            this.name.equals("ruins1_outer") ||
//            this.name.equals("oasis1") ||
//            this.name.equals("sand_pit1")) {
//        }
    }
    
    // Currently only used for transitioning music between routes.
    public String type() {
        if (this.name.equals("desert1") ||
            this.name.equals("ruins1_outer") ||
            this.name.equals("oasis1") ||
            this.name.equals("sand_pit1")) {
            return "desert";
        }
        if (this.name.equals("graveyard1")) {
            return "graveyard";
        }
        return "";
    }

    
    public ArrayList<String> musics() {
        if (!Route.musics.containsKey(this.name)) {
            ArrayList<String> musics = new ArrayList<String>();
            if (this.name.equals("ruins1_inner")) {
                musics.add("relic_castle2");
                musics.add("relic_castle2");
            }
            else if (this.name.equals("fossil_lab1")) {
                musics.add("silence1");
                musics.add("silence1");
            }
            else if (this.name.equals("graveyard1")) {
//                musics.add("silence1");
//                musics.add("silence1");
                musics.add("old_chateau3");
                musics.add("old_chateau3");
            }
            else if (this.name.equals("pkmnmansion1")) {
                musics.add("pkmnmansion");
                musics.add("pkmnmansion");
            }
            else if (this.name.equals("regi_cave1")) {
                musics.add("sealed_chamber2");
                musics.add("sealed_chamber2");
            }
            else if (this.name.equals("desert1") ||
                     this.name.equals("ruins1_outer") ||
                     this.name.equals("oasis1") ||
                     this.name.equals("sand_pit1")) {
                musics.add("route_111-2");
                musics.add("route_111-2");
            }
            else {
                // TODO: possibly different per-route
//              musics.add("nature1_render");  // TODO: this is being removed or replaced by something
              musics.add("pokemon_tcg_gym1");
              musics.add("overw3");
              musics.add("route_42");
              musics.add("national_park1");
              musics.add("viridian_forest_gs");
              musics.add("route_3_gs");
              musics.add("route_3_rb");
              musics.add("route_1");
              musics.add("route_idk1");
//              musics.add("littleroot_town");  // TODO: doesnt really fit
              // TODO: victory road theme thing
            }
            Route.musics.put(this.name, musics);
        }
        return Route.musics.get(this.name);
    }
    
    
    public ArrayList<String> allowedPokemon() {
        if (!Route.allowedPokemon.containsKey(this.name)) {
            ArrayList<String> allowedPokemon = new ArrayList<String>();
            if (this.name.equals("mountain1")) {
                allowedPokemon.add("rhydon");
                allowedPokemon.add("rhyhorn");
                allowedPokemon.add("onix");
                allowedPokemon.add("machop");
                allowedPokemon.add("machoke");
//                allowedPokemon.add("solrock");  // TODO: permissions
                // lunatone, zubat?
                allowedPokemon.add("cubone");
                allowedPokemon.add("phanpy");
                allowedPokemon.add("skarmory");  // <- fly animation doesn't look great for now, need ow sprites.
                allowedPokemon.add("whismur");   // nuuk
                allowedPokemon.add("makuhita");  // nuuk
                // TODO: remove
//                this.music = Gdx.audio.newMusic(Gdx.files.internal("route1_1.ogg"));
//                this.music.setLooping(true);
//                this.music.setVolume(.3f);
            }
            else if (this.name.equals("deep_forest")) {
                // TODO: this is just an idea
                // zubat, houndour, murkrow, nidorana/o, scyther, pinsir, stantler, tangela, teddiursa, weepinballowedPokemon    // absol, growlithe, vulpix,
                allowedPokemon.add("zubat");
                allowedPokemon.add("golbat");
                allowedPokemon.add("houndour");  // houndoom never spawns (probably level related)
                //              allowedPokemon.add("stantler");  // TODO: enable when decent overworld sprite
                allowedPokemon.add("murkrow");
                allowedPokemon.add("nidorina");
                allowedPokemon.add("nidorino");
                allowedPokemon.add("scyther");
                allowedPokemon.add("pinsir");
                allowedPokemon.add("growlithe");
                allowedPokemon.add("vulpix");
                allowedPokemon.add("breloom");  // nuuk
                allowedPokemon.add("sableye");  // nuuk
//                allowedPokemon.add("litwick");  // Goose // nuuk  TODO: remove, silph scope now
                allowedPokemon.add("ralts");  // nuuk
                allowedPokemon.add("gastly");
            }
            else if (this.name.equals("forest1")) {
                allowedPokemon.add("oddish");
                allowedPokemon.add("gloom");
                allowedPokemon.add("pidgey");
                allowedPokemon.add("pidgeotto");
                allowedPokemon.add("spearow");
//                allowedPokemon.add("swablu");  // TODO: permissions
                allowedPokemon.add("taillow");  // nuuk
                allowedPokemon.add("hoppip");
//                allowedPokemon.add("machop");
                allowedPokemon.add("bulbasaur");
                allowedPokemon.add("charmander");
                allowedPokemon.add("chikorita");
                allowedPokemon.add("paras");
                allowedPokemon.add("pikachu");
                allowedPokemon.add("weedle");
                allowedPokemon.add("caterpie");
                allowedPokemon.add("spinarak");
                allowedPokemon.add("ledyba");
                allowedPokemon.add("hoothoot");
                allowedPokemon.add("mankey");
                allowedPokemon.add("shroomish");
                allowedPokemon.add("combee");  // nuuk
                //
//                allowedPokemon.add("girafarig");  // TODO: enable when decent overworld sprite

                // TODO: test with these removed
                // TODO: make sure there are still roaming bulbasaur
//                for (int i = 0; i < 2; i++) {
//                    allowedPokemon.add("bulbasaur");
//                    allowedPokemon.add("charmander");
//                    allowedPokemon.add("chikorita");
//                }


                // TODO: remove
//                this.music = Gdx.audio.newMusic(Gdx.files.internal("route1_1.ogg"));
//                this.music.setLooping(true);
//                this.music.setVolume(.3f);
            }
            else if (this.name.equals("savanna1")) {
//                allowedPokemon.add("tauros");  // TODO: enable when decent overworld sprite
                allowedPokemon.add("ponyta"); 
                allowedPokemon.add("miltank");
                for (int i = 0; i < 2; i++) {
                    allowedPokemon.add("drowzee");
//                    allowedPokemon.add("abra");
                    allowedPokemon.add("oddish");
                    allowedPokemon.add("chikorita");
                    allowedPokemon.add("bulbasaur");
                    allowedPokemon.add("hoppip");
                    allowedPokemon.add("pidgey");
                    allowedPokemon.add("taillow");  // nuuk and prism
                    allowedPokemon.add("charmander");
                    allowedPokemon.add("cyndaquil");
                    allowedPokemon.add("mareep");
                    allowedPokemon.add("ekans");
                    allowedPokemon.add("doduo");
                    allowedPokemon.add("sentret");
                    allowedPokemon.add("rattata");
                    allowedPokemon.add("yanma");
                    allowedPokemon.add("poochyena");  // nuuk
                    allowedPokemon.add("eevee");
                    allowedPokemon.add("cutiefly");
                    allowedPokemon.add("shinx");  // prism
                }
                // feels like it's getting too diluted
//                allowedPokemon.add("snubbul");
//                allowedPokemon.add("meowth");
                // nidoran, murkro, natu, 

                // TODO: might need savanna2 biome, which has it's own dedicated blotch area
//                allowedPokemon.add("kangaskan");  // only if high-level?
//                allowedPokemon.add("scyther");  // only if high-level?
//                allowedPokemon.add("pinsir");  // only if high-level?
//                allowedPokemon.add("lickitung");  // only if high-level?
//                allowedPokemon.add("drowzee");
//                allowedPokemon.add("eevee");  // TODO: after evolutionary stones added
            }
            else if (this.name.equals("beach1")) {
                allowedPokemon.add("squirtle");
                allowedPokemon.add("krabby");  // TODO: probably remove, rock smash spawn now.
                allowedPokemon.add("totodile");  // this guy never spawns, no idea why
                allowedPokemon.add("shellder");  // TODO: probably remove, rock smash spawn now.
                allowedPokemon.add("wooper");  // this guy never spawns no idea why
                allowedPokemon.add("staryu");  // TODO: probabloy remove, rock smash spawn now.
                allowedPokemon.add("marill");  // this guy never spawns as Azumarill
                allowedPokemon.add("slowpoke");
                allowedPokemon.add("psyduck");
                //
                allowedPokemon.add("wingull");  // nuuk
                //
                allowedPokemon.add("corphish");  // sir-feralipogchamp discord
                //
            }
            else if (this.name.equals("desert1")) {
                // TODO: these are just some ideas
//                allowedPokemon.add("gible");  // TODO: was prism, remove
//                allowedPokemon.add("gabite");
                // TODO: would be sweet if fully evolved forms were hard to escape from and catch
                //  ie, they were scary to run into.
                //  probably just make them high level.
//                allowedPokemon.add("garchomp");   // high-level
//                allowedPokemon.add("cacnea");
//                allowedPokemon.add("cacturne");

//                allowedPokemon.add("sandshrew");
//                allowedPokemon.add("sandslash");
                allowedPokemon.add("kangaskhan");
                allowedPokemon.add("cubone");
//                allowedPokemon.add("diglett");  // TODO: didnt care for the way this looked.
                allowedPokemon.add("numel");
                allowedPokemon.add("camerupt");
                allowedPokemon.add("drapion");

//                allowedPokemon.add("marowak");

//                allowedPokemon.add("rhyhorn");  // TODO: remove
//                allowedPokemon.add("shieldon");  // TODO: was prism, remove
//                allowedPokemon.add("bastiodon");  // high-level
//                allowedPokemon.add("skorupi");  // TODO: was prism, remove
//                allowedPokemon.add("drapion");    // high-level
//                allowedPokemon.add("trapinch");  // TODO: was prism, remove
//                allowedPokemon.add("vibrava");
            }
            else if (this.name.equals("graveyard1")) {
                allowedPokemon.add("gastly");
                allowedPokemon.add("litwick");
                allowedPokemon.add("murkrow");
                allowedPokemon.add("drifloon");
                allowedPokemon.add("misdreavus");
                allowedPokemon.add("zubat");
                // TODO
                allowedPokemon.add("chingling");
                allowedPokemon.add("chimecho");
                allowedPokemon.add("cubone");
                allowedPokemon.add("houndour");
                allowedPokemon.add("hoothoot");
                allowedPokemon.add("spinarak");
                allowedPokemon.add("absol");
                allowedPokemon.add("duskull");
            }
            // 
            else if (this.name.equals("sand_pit1")) {
                allowedPokemon.add("diglett");
                allowedPokemon.add("sandile");
                // Gible? Gabite?
                // Hippowdon line
                
            }
            else if (this.name.equals("oasis1")) {
                allowedPokemon.add("surskit");
                allowedPokemon.add("poliwag");
                allowedPokemon.add("lotad");
                allowedPokemon.add("lombre");
                allowedPokemon.add("zigzagoon");  // TODO: need ow sprite
                allowedPokemon.add("sandshrew");
                allowedPokemon.add("exeggcute");
            }
            else if (this.name.equals("ruins1_outer")) {
                allowedPokemon.add("sigilyph");
                allowedPokemon.add("natu");
                allowedPokemon.add("nosepass");
                allowedPokemon.add("beldum");
                allowedPokemon.add("metang");
                allowedPokemon.add("smeargle");
                allowedPokemon.add("solrock");
                allowedPokemon.add("bronzor");
            }
            else if (this.name.equals("ruins1_inner")) {
                // TODO
                allowedPokemon.add("lunatone");  // TODO: day/night spawns
//                allowedPokemon.add("chingling");
                allowedPokemon.add("elgyem");
                allowedPokemon.add("duskull");
                allowedPokemon.add("sandile");
                allowedPokemon.add("sandshrew");
                // Baltoy/Claydol
                // Beldum/Metang/Metagross
                // Klink/Klang/Klinklang
                // Elgyem/Beheeyem
                // Golett/Golurk
                // Deino/Zweilous/Hydreigon
                // Chingling/Chimeco
                // relic castle spawns: sandline, sandshrew, yamask, onix, claydol
            }
            else if (this.name.equals("snow1")) {
                allowedPokemon.add("larvitar");
                allowedPokemon.add("sneasel");
//                allowedPokemon.add("weavile");
//                allowedPokemon.add("snorunt");
//                allowedPokemon.add("glalie");
                allowedPokemon.add("aron");
//                allowedPokemon.add("lairon");
                allowedPokemon.add("swinub");
                allowedPokemon.add("piloswine");
                allowedPokemon.add("jynx");
                allowedPokemon.add("delibird");  // TODO: make sure roaming spawns aren't wonky after adding
                // TODO: remove
//                this.music = Gdx.audio.newMusic(Gdx.files.internal("route1_1.ogg"));
//                this.music.setLooping(true);
//                this.music.setVolume(.3f);
            }
            else if (this.name.equals("fossil_lab1")) {
                // 
            }
            else if (this.name.equals("pkmnmansion1")) {
                allowedPokemon.add("magmar");
                allowedPokemon.add("grimer");
                allowedPokemon.add("rattata");
                allowedPokemon.add("koffing");
                allowedPokemon.add("vulpix");
                allowedPokemon.add("growlithe");
                allowedPokemon.add("ponyta");
                allowedPokemon.add("ditto");
            }
            else if (this.name.equals("regi_cave1")) {
                allowedPokemon.clear();  // no wild spawns
            }
            else if (this.name.equals("oasis_pond1")) {
                allowedPokemon.add("poliwag");
                allowedPokemon.add("goldeen");
                allowedPokemon.add("feebas");  // super rod
                allowedPokemon.add("remoraid");  // super rod
            }
            else if (this.name.equals("sea1")) { 
                // Relicanth, Wailmer, Chinchou, Mantine likely dive/surf spawns
                allowedPokemon.add("tentacool");
                allowedPokemon.add("magikarp");
                allowedPokemon.add("corsola");  // good rod
                allowedPokemon.add("horsea");  // good rod
                allowedPokemon.add("qwilfish");  // super rod

                // Debatable, these are all rock smash spawns
//                allowedPokemon.add("shellder");
//                allowedPokemon.add("krabby");
//                allowedPokemon.add("staryu");
            }
            // Rivers made by the player.
            else if (this.name.equals("river1")) {
                allowedPokemon.add("magikarp");
                allowedPokemon.add("poliwag");
                allowedPokemon.add("goldeen");
                allowedPokemon.add("remoraid");  // super rod
            }
            // TODO: this was just for fun. Potentially remove.
            else if (this.name.equals("sand_fishing1")) {
                allowedPokemon.add("trapinch");
                allowedPokemon.add("trapinch");
                allowedPokemon.add("sandile");
                allowedPokemon.add("sandile");
                allowedPokemon.add("gible");  // super rod
            }
            // TODO: remove
            else if (this.name.equals("rock_smash1")) {
                allowedPokemon.add("geodude");
                allowedPokemon.add("geodude");
                allowedPokemon.add("shuckle");
            }
            else if (this.name.equals("")) {
                allowedPokemon.clear();
                // TODO: remove
//                this.music = Gdx.audio.newMusic(Gdx.files.internal("route1_1.ogg"));
//                this.music.setLooping(true);
//                this.music.setVolume(.3f);
            }
            else {
                allowedPokemon.clear();
                // TODO: remove
//                this.pokemon.add(new Pokemon("kangaskhan", 22, Pokemon.Generation.CRYSTAL));
//                this.pokemon.add(new Pokemon("togekiss", 22, Pokemon.Generation.CRYSTAL));
//                this.pokemon.add(new Pokemon("rhydon", 22));
//                this.pokemon.add(new Pokemon("rhyhorn", 22));
//                this.pokemon.add(new Pokemon("onix", 22));
//                this.pokemon.add(new Pokemon("machop", 22));
                allowedPokemon.add("mamoswine");
                allowedPokemon.add("weavile");
                allowedPokemon.add("magmortar");
                allowedPokemon.add("electivire");
                allowedPokemon.add("metagross");
                // TODO: remove
//                this.music = Gdx.audio.newMusic(Gdx.files.internal("route1_1.ogg"));
//                this.music.setLooping(true);
//                this.music.setVolume(.3f);
            }
            Route.allowedPokemon.put(this.name, allowedPokemon);
        }
        return Route.allowedPokemon.get(this.name);
    }

    // get next music. if random == true, get random one that isn't same as current
    public String getNextMusic(boolean random) {
//        // For now, do nature sounds between each song
//        if (this.musicsIndex != 0) {
//            this.musicsIndex = 0;
//            return this.musics.get(this.musicsIndex);
//        }

        // TODO: this doesn't work given the above code
        int nextIndex = 0;
        if (random) {
            nextIndex = this.musicsIndex;
            while (nextIndex == this.musicsIndex) {  // || nextIndex == 0  // TODO: remove
                nextIndex = Game.staticGame.map.rand.nextInt(this.musics().size());
            }
        }
        else {
            nextIndex = (this.musicsIndex + 1) % this.musics().size();
        }
        this.musicsIndex = nextIndex;
        return this.musics().get(this.musicsIndex);
    }
    
    public ArrayList<String> goodRodPokemon() {
        ArrayList<String> pokemon = new ArrayList<String>();
        pokemon.add("corsola");
        pokemon.add("horsea");
        return pokemon;
    }

    public ArrayList<String> superRodPokemon() {
        ArrayList<String> pokemon = new ArrayList<String>();
        pokemon.add("feebas");
        pokemon.add("remoraid");
        pokemon.add("qwilfish");
        //
        pokemon.add("gible");
        return pokemon;
    }

    /**
     * Day-only pokemon
     */
    public ArrayList<String> dayPokemon() {
        ArrayList<String> dayPokemon = new ArrayList<String>();
        // TODO: probably if-block for different routes in the future.
        // TODO: a couple of people on discord never gave me their day/night list(s)
        dayPokemon.add("ledyba");
        dayPokemon.add("ledian");
        dayPokemon.add("sunkern");
        dayPokemon.add("sunflora");
        dayPokemon.add("spearow");
        dayPokemon.add("fearow");
        dayPokemon.add("pidgey");
        dayPokemon.add("pidgeotto");
        dayPokemon.add("pidgeot");
        dayPokemon.add("ponyta");
        dayPokemon.add("rapidash");
        dayPokemon.add("growlithe");
        dayPokemon.add("arcanine");
        dayPokemon.add("doduo");
        dayPokemon.add("dodrio");
        dayPokemon.add("hoppip");
        dayPokemon.add("skiploom");
        dayPokemon.add("jumpluff");
        dayPokemon.add("miltank");
        dayPokemon.add("tauros");
        dayPokemon.add("solrock");
        dayPokemon.add("sentret");
        dayPokemon.add("furret");
        dayPokemon.add("bellossom");
        dayPokemon.add("mawile");
        dayPokemon.add("yanma");
        return dayPokemon;
    }

    /**
     * Night-only pokemon
     */
    public ArrayList<String> nightPokemon() {
        ArrayList<String> nightPokemon = new ArrayList<String>();
        nightPokemon.add("hoothoot");
        nightPokemon.add("noctowl");
        nightPokemon.add("spinarak");
        nightPokemon.add("ariados");
        nightPokemon.add("houndour");
        nightPokemon.add("houndoom");
        nightPokemon.add("poochyena");
        nightPokemon.add("mightyena");
        nightPokemon.add("oddish");
        nightPokemon.add("gloom");
        nightPokemon.add("vileplume");
        nightPokemon.add("murkrow");
        nightPokemon.add("honchkrow");
        nightPokemon.add("sneasel");
        nightPokemon.add("zubat");
        nightPokemon.add("golbat");
        nightPokemon.add("crobat");
        nightPokemon.add("lunatone");
        nightPokemon.add("cleffa");
        nightPokemon.add("clefairy");
        nightPokemon.add("clefable");
        nightPokemon.add("igglybuff");
        nightPokemon.add("jigglypuff");
        nightPokemon.add("wigglytuff");
        nightPokemon.add("sableye");
        nightPokemon.add("gastly");
        nightPokemon.add("haunter");
        nightPokemon.add("gengar");
        return nightPokemon;
    }
}

//class SpecialMewtwoTile extends Tile {
////    public static boolean isPresent = true;
//    
//    public SpecialMewtwoTile(Vector2 pos) {
//        super("mewtwo_overw1", pos);
//    }
//
//    @Override
//    public void onPressA(Game game) {
//        if (!this.nameUpper.contains("hidden")) {
//            game.playerCanMove = false;
//            SpecialMewtwo1 mewtwo = new SpecialMewtwo1(50, this);
//            game.battle.oppPokemon = mewtwo;
//            Action fadeMusic = new FadeMusic("currMusic", "out", "pause", 0.025f, null);
//            game.insertAction(new SplitAction(fadeMusic,
//                              new WaitFrames(game, 20,
//                              new DisplayText(game, "...", null, false, true,
//                              new WaitFrames(game, 100,
//                              new SpecialBattleMewtwo(game, mewtwo))))));
//            game.fadeMusicAction = fadeMusic;
//        }
//    }
//}

// action to generate mountain

// TODO - in future, will not handle these as tiles but as
// overworld pkmn. need easy way to access tile and overworld
// pkmn functions all at once
class SuicuneTile extends Tile {
    public SuicuneTile(Vector2 pos) {
        super("suicune_overw1", pos);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onPressA(Game game) {
        game.playerCanMove = false;

        // TODO - player can't move when returning from this
        game.battle.oppPokemon = new Pokemon("Suicune", 50);

        Action encounterAction = new SplitAction(
                new BattleIntro(
                        new BattleIntroAnim1(
                                new SplitAction(
                                        new DrawBattle(game),
                                        new BattleAnimPositionPlayers(
                                                game,
                                                new PlaySound(
                                                        game.battle.oppPokemon.specie.name,
                                                        new DisplayText(
                                                                game,
                                                                "Wild "
                                                                        + game.battle.oppPokemon.nickname
                                                                                .toUpperCase()
                                                                        + " appeared!",
                                                                null,
                                                                null,
                                                                new WaitFrames(
                                                                        game,
                                                                        39,
                                                                        // demo
                                                                        // code
                                                                        // -
                                                                        // wildly
                                                                        // confusing,
                                                                        // but i
                                                                        // don't
                                                                        // want
                                                                        // to
                                                                        // write
                                                                        // another
                                                                        // if
                                                                        // statement
                                                                        game.player.adrenaline > 0 ? new DisplayText(
                                                                                game,
                                                                                ""
                                                                                        + game.player.name
                                                                                        + " has ADRENALINE "
                                                                                        + Integer
                                                                                                .toString(game.player.adrenaline)
                                                                                        + "!",
                                                                                null,
                                                                                null,
                                                                                new PrintAngryEating(
                                                                                        game, // for
                                                                                                // demo
                                                                                                // mode,
                                                                                                // normally
                                                                                                // left
                                                                                                // out
                                                                                        new DrawBattleMenuSafariZone(
                                                                                                game,
                                                                                                null)))
                                                                                : new PrintAngryEating(
                                                                                        game, // for
                                                                                                // demo
                                                                                                // mode,
                                                                                                // normally
                                                                                                // left
                                                                                                // out
                                                                                        new DrawBattleMenuSafariZone(
                                                                                                game,
                                                                                                null))
                                                                //
                                                                ))))))),
                new WaitFrames(game, 10, new BattleIntroMusic(new WaitFrames(
                        game, 100, new DoneWithDemo(game)))));

        game.insertAction(new DisplayText(game, "GROWL!!",
                "Suicune", null,
                // new PlayerCanMove(game,
                encounterAction));
    }
}

class Tile {
    // Idea: name() property that will get Tile's enum name, and convert it to string.

    Map<String, Boolean> attrs;

    // temp fix to ledge direction thing
    // can't think of another place to put this.
    String ledgeDir;
    public Vector2 position;
    public Sprite sprite;
    public Sprite overSprite; // ledges
    // If not null, has item on this square that can be pick up by pressing A on the tile
    public String hasItem;
    public int hasItemAmount = 0;

    // route that this tile belongs to
    // used to signal movement to new routes
    Route routeBelongsTo;
    // This is flagged by MoveWater in an attempt to speed up
    // computation.
    public boolean isTorch = false;
    // if true, need to draw bridge supports over this tile
    // flagged by DrawMap (?)
    public boolean belowBridge = false;
    // Cached value to tell game to draw this over the player
    public boolean drawAsTree = false;
    // Cached value to draw overSprite below player
    // Ex: stairs, stool, cracked ground, door.
    public boolean drawUpperBelowPlayer = false;

    // this is just my idea for tiles that do something when player walks over
    // it,
    // like grass, warp tiles.
    // would probly rely on setting a game.map.player.canMove flag to false.
    // likely that playerMove function would call onWalkOver when player steps
    // onto new tile
    // tall grass wild encounter is currently in-lined in playerMoving action

    // if you need to see what type of tile this is, use name variable
    public String name;
    // Needed to store which object is above the 'terrain'. Like a tree, rock, house piece, etc.
    // This must be stored because it needs to be sent over the network, and saved locally.
    public String nameUpper = "";

    String biome = "";

    // Used by hidden switches (so it knows which door tiles to flip)
    // I'm storing Vector2 so I can serialize these values. Unfortunately the switch
    // tile will need to be smart enough to know if it should flip switches on same
    // layer, layer below, above, etc.
    //
    // This has to be initialized manually. Defaulting to null to save space.
    ArrayList<Vector2> doorTiles;

    // For when you collect items from this tile
    HashMap<String, Integer> items = null; // TODO: trying to improve perf new HashMap<String, Integer>();
    
    // TODO: experimental
    // hole name -> texture
    public static HashMap<String, Texture[]> dynamicTexts = new HashMap<String, Texture[]>();

    // Only used during map generation
    public boolean isBottomMtnLayer = false;

    // TODO: this didn't really increase performance, remove probably
//    public static enum Type {
//        FLOWER,
//        CAMPFIRE,
//        REVIVED,
//        VOLCARONA,
//    }
//    public static HashMap<String, ArrayList<Type>> types = new HashMap<String, ArrayList<Type>>();
//    public boolean is(Type type) {
//        if (!Tile.types.containsKey(this.name+this.nameUpper)) {
//            ArrayList<Type> types = new ArrayList<Type>();
//            if (this.name.contains("flower")) {
//                types.add(Type.FLOWER);
//            }
//            if (this.nameUpper.equals("campfire1")) {
//                types.add(Type.CAMPFIRE);
//            }
//            if (this.nameUpper.contains("revived_")) {
//                types.add(Type.REVIVED);
//            }
//            if (this.nameUpper.equals("volcarona")) {
//                types.add(Type.VOLCARONA);
//            }
//            Tile.types.put(this.name+this.nameUpper, types);
//        }
//        return Tile.types.get(this.name+this.nameUpper).contains(type);
//    }

    /**
     * Api intending to reduce memory usage.
     */
    public HashMap<String, Integer> items() {
        if (this.items == null) {
            this.items = new HashMap<String, Integer>();

            // Init items here based on tile name and nameUpper
            if (this.name.equals("grass2")) {
                this.items.put("grass", 1);
                // TODO: test
                if (Game.rand.nextInt(256) < 64) {  // 1/4 chance atm
                    this.items.put("miracle seed", 1);
                }
            }
            else if (this.name.equals("grass3")) {
                this.items.put("grass", 1);
            }
            else if (this.name.equals("grass4")) {
                this.items.put("grass", 2);
            }
            else if (this.name.equals("grass_sand1")) {
                this.items.put("grass", 1);
            }
            else if (this.name.equals("grass_sand3")) {
                this.items.put("grass", 2);
            }
            else if (this.name.equals("grass_graveyard1")) {
                this.items.put("grass", 2);
            }
            else if (this.name.equals("tree5")) {
                this.items.put("log", 2);
                // TODO: might drop coconut in the future.
            }
            else if (this.nameUpper.contains("bush2_color")) {
                this.items.put("log", 1);
                String[] items = {"black apricorn", "blue apricorn", "green apricorn", "pink apricorn",
                                  "red apricorn", "white apricorn", "yellow apricorn"};
                if (Game.staticGame.map.rand.nextInt(2) == 0) {
                    this.items.put(items[Game.staticGame.map.rand.nextInt(items.length)], 1);
                }
            }
            else if (this.nameUpper.equals("tree2")) {
                this.items.put("log", 2);
                String[] items = {"black apricorn", "blue apricorn", "green apricorn", "pink apricorn",
                                  "red apricorn", "white apricorn", "yellow apricorn"};
                if (Game.staticGame.map.rand.nextInt(2) == 0) {
                    this.items.put(items[Game.staticGame.map.rand.nextInt(items.length)], 2);
                }
            }
            else if (this.nameUpper.equals("tree4")) {
                this.items.put("log", 2);
                String[] items = {"black apricorn", "blue apricorn", "green apricorn", "pink apricorn",
                                  "red apricorn", "white apricorn", "yellow apricorn"};
                if (Game.staticGame.map.rand.nextInt(2) == 0) {
                    this.items.put(items[Game.staticGame.map.rand.nextInt(items.length)], 2);
                }
            }
            else if (this.nameUpper.equals("grass_planted")) {
                this.items.put("miracle seed", 1);
            }
            if (!this.nameUpper.equals("")) {
                if (!this.nameUpper.contains("floor") &&
                    !this.nameUpper.contains("pokeball") &&
                    !this.nameUpper.contains("bush") &&
                    !this.nameUpper.equals("pokemon_mansion_key") &&
                    !this.nameUpper.contains("REGI") &&
                    !this.nameUpper.contains("tree") &&
                    !this.nameUpper.contains("rock") &&
                    !this.nameUpper.equals("grass_planted") &&
                    !this.nameUpper.contains("campfire") &&
                    !this.nameUpper.contains("fence") &&
                    !this.nameUpper.contains("house") &&
                    !this.nameUpper.contains("gravestone")) {
                    this.items.put("grass", 1);
                    this.items.put("log", 1);
                }
                if (this.nameUpper.equals("rock1") ||
                    this.nameUpper.equals("rock1_color")) {
                    this.items.put("hard stone", 1);
                    if (Game.rand.nextInt(10) == 0) {
                        this.items.put("moon stone", 1);
                    }
                }
                else if (this.nameUpper.equals("gravestone2")) {
                    this.items.put("hard stone", 1);
                    this.items.put("life force", 1);
                }
            }
        }
        return this.items;
    }

    /**
     * Factory method. Useful when loading from file, and need to create Tile subclasses for some Tiles.
     * @see TrainerTipsTile
     * 
     * TODO: I kind of regret doing it this way. Should have just made an 'if' block in Tile's onPressA,
     * added TrainerTipsTile fields here and called it good.
     */
    public static Tile get(Network.TileData tileData, Route routeBelongsTo) {
        if (tileData.tileNameUpper.equals("sign1")) {
            return new TrainerTipsTile(tileData.pos.cpy(), routeBelongsTo, tileData.isUnown, tileData.message);
        }
        Tile tile = new Tile(tileData.tileName, tileData.tileNameUpper, tileData.pos.cpy(), true, routeBelongsTo);
        tile.items = tileData.items;
        tile.hasItem = tileData.hasItem;
        tile.hasItemAmount = tileData.hasItemAmount;
        tile.doorTiles = tileData.doorTiles;
        tile.biome = tileData.biome;
        return tile;
    }

    public Tile(String tileName, String nameUpper, Vector2 pos) {
        this(tileName, nameUpper, pos, false);
    }

    public Tile(String tileName, String nameUpper, Vector2 pos, boolean color) {
        this(tileName, nameUpper, pos, color, null);
    }
    public Tile(String tileName, String nameUpper, Vector2 pos, boolean color, Route routeBelongsTo) {
        this.init(tileName, nameUpper, pos, color, routeBelongsTo);
    }

    public void init() {
        this.init(this.name, this.nameUpper, this.position, true, this.routeBelongsTo);
    }
    
    /**
     * TODO: test
     */
    public void init(String tileName, String nameUpper, Vector2 pos, boolean color, Route routeBelongsTo) {
        // initialize attributes of tile
        this.attrs = new HashMap<String, Boolean>();
        this.attrs.put("solid", false);
        this.attrs.put("ledge", false);
        this.attrs.put("water", false);
        this.attrs.put("grass", false);
        this.attrs.put("tree",  false);
        this.attrs.put("smashable",  false);
        this.belowBridge = false;

        this.name = tileName;
        this.nameUpper = nameUpper;  // object above the terrain, ie house, tree, rock etc.

        // this.attrs.put("qmark", false); // TODO - delete if unused// tile that
        // is available to be changed to another tile later

        this.position = pos;
        this.routeBelongsTo = routeBelongsTo;

        if (tileName.equals("ground1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("ground1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.equals("ground2")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("ground2.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.equals("block1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("block1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true); // block is solid
        } else if (tileName.equals("grass1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/blank2.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(Gdx.files.internal("grass1.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("grass", true);

            // this.sprite.setColor(new Color(.1f, .1f, .1f, 1f)); // debug
        } else if (tileName.equals("grass_planted")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/grass_planted.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.equals("grass2")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/grass2_under.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(Gdx.files.internal("tiles/grass2_over.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("grass", true);
            this.attrs.put("cuttable", true);
            // TODO: remove
//            this.items.put("grass", 1);
//            // TODO: test
//            if (Game.rand.nextInt(256) < 64) {  // 1/4 chance atm
//                this.items.put("miracle seed", 1);
//            }
            // this.sprite.setColor(new Color(.1f, .1f, .1f, 1f)); // debug
        } else if (tileName.equals("grass3")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/grass3_under.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(Gdx.files.internal("tiles/grass3_over.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("grass", true);
            this.attrs.put("cuttable", true);
//            this.items.put("grass", 1);
        } else if (tileName.equals("grass4")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/green1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(Gdx.files.internal("tiles/grass4_over.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("grass", true);
            this.attrs.put("cuttable", true);
//            this.items.put("grass", 1);
        } else if (tileName.equals("grass_sand1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/sand1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(Gdx.files.internal("tiles/grass2_over.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("grass", true);
            this.attrs.put("cuttable", true);
//            this.items.put("grass", 1);
        } else if (tileName.equals("flower1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/flower1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.equals("ground3")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/ground3.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);

        } else if (tileName.equals("mountain1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/mountain1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.equals("mountain2")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/mountain2.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.equals("mountain3")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/mountain3.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.contains("mountain")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/"+tileName+".png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            if (tileName.contains("left")) {
                this.attrs.put("ledge", true);
                this.ledgeDir = "left";
            }
            else if (tileName.contains("right")) {
                this.attrs.put("ledge", true);
                this.ledgeDir = "right";
            }
            else if (tileName.contains("top")) {
                this.attrs.put("ledge", true);
                this.ledgeDir = "up";
            }
            else if (tileName.contains("bottom")) {
                this.attrs.put("ledge", true);
                this.ledgeDir = "down";
            }
            else if (!tileName.contains("inner")) {
                this.attrs.put("solid", true);
            }
        }
        else if (tileName.contains("cave1") || tileName.contains("cave2")) {
            if (tileName.equals("cave1_regi1")) {
                Texture playerText = TextureCache.get(Gdx.files.internal("tiles/cave1/"+tileName+".png"));
                this.sprite = new SpriteProxy(new Color(56.0f/255f, 56.0f/255f, 56.0f/255f, 1f),
                                              new Color(192.0f/255f, 144.0f/255f, 56.0f/255f, 1f),
                                              new Color(160.0f/255f, 120.0f/255f, 24.0f/255f, 1f),
                                              playerText, 0, 0, 16, 16);
                this.sprite = new SpriteProxy(playerText, 0, 0, 32, 32);
            }
            else if (tileName.equals("cave1_regi2") || tileName.equals("cave1_regi3")) {
                Texture playerText = TextureCache.get(Gdx.files.internal("tiles/cave1/cave1_regi1.png"));
                this.sprite = new SpriteProxy(playerText, 32*4, 0, 32, 32);
            }
//            else if (tileName.equals("cave1_regi5")) {
//                
//            }
//            else if (tileName.equals("cave1_regi3")) {
//                Texture texture = TextureCache.get(Gdx.files.internal("tiles/cave1/regi3.png"));
//                this.sprite = new Sprite(texture, 0, 0, 48, 48);
//                this.sprite.setPosition(this.position.x-8, this.position.y-9);
//            }
            else {
                Texture playerText = TextureCache.get(Gdx.files.internal("tiles/cave1/"+tileName+".png"));
                this.sprite = new SpriteProxy(new Color(56.0f/255f, 56.0f/255f, 56.0f/255f, 1f),
                                              new Color(192.0f/255f, 144.0f/255f, 56.0f/255f, 1f),
                                              new Color(160.0f/255f, 120.0f/255f, 24.0f/255f, 1f),
                                              playerText, 0, 0, 16, 16);
            }
            
            if (tileName.contains("up")) {
                this.attrs.put("ledge", true);
                this.ledgeDir = "up";
            }
            else if (!tileName.contains("floor") &&
                !tileName.contains("ramp") &&
                !tileName.contains("door")) {
                this.attrs.put("solid", true);
            }
            if (this.name.equals("cave1_regipedistal1")) {
                this.overSprite = this.sprite;
                Texture playerText = TextureCache.get(Gdx.files.internal("tiles/cave1/cave1_up1.png"));
                this.sprite = new SpriteProxy(new Color(56.0f/255f, 56.0f/255f, 56.0f/255f, 1f),
                                              new Color(192.0f/255f, 144.0f/255f, 56.0f/255f, 1f),
                                              new Color(160.0f/255f, 120.0f/255f, 24.0f/255f, 1f),
                                              playerText, 0, 0, 16, 16);
            }
        }
        else if (tileName.equals("tree_large1")) { //
            Texture playerText;
            if (color) {
                playerText = TextureCache.get(Gdx.files.internal("tiles/tree_large1_color.png"));
            }
            else {
                playerText = TextureCache.get(Gdx.files.internal("tiles/tree_large1.png"));
            }
            this.sprite = new Sprite(playerText, 0, 0, 32, 32);
            this.attrs.put("solid", true);
        } else if (tileName.equals("tree_large1_noSprite")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/blank.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        }

        else if (tileName.equals("ledge1_down")) {
            Texture playerText;
            if (color) {
                playerText = TextureCache.get(Gdx.files.internal("ledge1_down_color.png"));
            }
            else {
                playerText = TextureCache.get(Gdx.files.internal("ledge1_down.png"));
            }
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("ledge", true);
            this.ledgeDir = "down";

            // this.sprite.setColor(new Color(1f, 1f, 1f, 1f)); // debug
        } 
        else if (tileName.contains("ledge2")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/"+tileName+".png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("ledge", true);
//            this.ledgeDir = "down";  // TODO
        }
        else if (tileName.equals("ledge1_left")) {
            Texture playerText;
            if (color) {
                playerText = TextureCache.get(Gdx.files.internal("ledge1_left_color.png"));
            }
            else {
                playerText = TextureCache.get(Gdx.files.internal("ledge1_left.png"));
            }
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("ledge", true);
            this.ledgeDir = "left";

            // this.sprite.setColor(new Color(.1f, .1f, .1f, 1f)); // debug
        } else if (tileName.equals("ledge1_right")) {
            Texture playerText;
            if (color) {
                playerText = TextureCache.get( Gdx.files.internal("ledge1_right_color.png"));
            }
            else {
                playerText = TextureCache.get( Gdx.files.internal("ledge1_right.png"));
            }
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("ledge", true);
            this.ledgeDir = "right";
        } else if (tileName.equals("ground2_top")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("ground2_top.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("ledge", true);
        } else if (tileName.equals("ledge1_corner_bl")) {
            Texture playerText;
            if (color) {
                playerText = TextureCache.get(Gdx.files.internal("ledge1_corner_bl_color.png"));
            }
            else {
                playerText = TextureCache.get(Gdx.files.internal("ledge1_corner_bl.png"));
            }
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        } else if (tileName.equals("ledge1_corner_br")) {
            Texture playerText;
            if (color) {
                playerText = TextureCache.get(Gdx.files.internal("ledge1_corner_br_color.png"));
            }
            else {
                playerText = TextureCache.get(Gdx.files.internal("ledge1_corner_br.png"));
            }
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        } else if (tileName.equals("ledge2_corner_tl")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("ledge2_corner_tl.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        } else if (tileName.equals("ledge2_corner_tr")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("ledge2_corner_tr.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        }

        else if (tileName.equals("ledge_grass_ramp")) {
            Texture playerText;
            if (color) {
                playerText = TextureCache.get(Gdx.files.internal("tiles/ledge_grass_ramp_color.png"));
            }
            else {
                playerText = TextureCache.get(Gdx.files.internal("tiles/ledge_grass_ramp.png"));
            }
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.equals("ledge_grass_safari_up")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/ledge_grass_safari_up.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("ledge", true); // TODO - need ledgeDir variable?
            this.ledgeDir = "up";
        } else if (tileName.equals("ledge_grass_down")) {
            Texture playerText;
            if (color) {
                playerText = TextureCache.get(Gdx.files.internal("tiles/ledge1_down_color.png"));
            }
            else {
                playerText = TextureCache.get(Gdx.files.internal("tiles/ledge_grass_down.png"));
            }
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("ledge", true);
            this.ledgeDir = "down";
        } else if (tileName.equals("ledge_grass_left")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/ledge_grass_left.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("ledge", true);
            this.ledgeDir = "left";
        } else if (tileName.equals("ledge_grass_right")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/ledge_grass_right.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("ledge", true);
            this.ledgeDir = "right";
        } else if (tileName.equals("ledge_grass_inside_tl")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/ledge_grass_inside_tl.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        } else if (tileName.equals("ledge_grass_inside_tr")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/ledge_grass_inside_tr.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        }

        // water
        else if (tileName.equals("water1")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/water1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
            this.attrs.put("water", true);
        } else if (tileName.equals("water1_ledge1_left")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/water1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
            this.attrs.put("water", true);
            playerText = TextureCache.get(
                    Gdx.files.internal("tiles/water1_ledge1_left.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.equals("water1_ledge1_right")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/water1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
            this.attrs.put("water", true);
            playerText = TextureCache.get(
                    Gdx.files.internal("tiles/water1_ledge1_right.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.equals("water1_ledge1_tl")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/water1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
            this.attrs.put("water", true);
            playerText = TextureCache.get(
                    Gdx.files.internal("tiles/water1_ledge1_tl.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.equals("water1_ledge1_top")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/water1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
            this.attrs.put("water", true);
            playerText = TextureCache.get(
                    Gdx.files.internal("tiles/water1_ledge1_top.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.equals("water1_ledge1_tr")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/water1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
            this.attrs.put("water", true);
            playerText = TextureCache.get(
                    Gdx.files.internal("tiles/water1_ledge1_tr.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
        }

        else if (tileName.equals("grass_short2")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/grass_short2.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.equals("grass_short3")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/grass_short3.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.equals("warp1_greyed")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/warp1_greyed.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.equals("warp1")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/warp1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        }

        // overw pokemon
        else if (tileName.equals("raikou_overw1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("ground1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(
                    Gdx.files.internal("pokemon/raikou_overw1.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        } else if (tileName.equals("entei_overw1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("ground1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(
                    Gdx.files.internal("pokemon/entei_overw1.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        } else if (tileName.equals("suicune_overw1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("ground1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(
                    Gdx.files.internal("pokemon/suicune_overw1.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        // TODO: remove
//        } else if (tileName.equals("mewtwo_overw1")) {
////            Texture playerText = TextureCache.get(Gdx.files.internal("ground1.png"));
//            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/blank.png"));
//            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
//            playerText = TextureCache.get(Gdx.files.internal("tiles/mewtwo_overworld.png"));
//            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
//            this.attrs.put("solid", true);
        } else if (tileName.equals("mega_gengar_overworld1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/blank.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(
                    Gdx.files.internal("pokemon/mgengar_overworld1.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        } else if (tileName.equals("solid")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/qmark_tile1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            // this.attrs.put("qmark", true); // TODO - remove if unused
            this.attrs.put("solid", true); // solid in case I need to put
                                            // foliage near 'solid' objects
        } else if (tileName.equals("bush1")) {
            Texture playerText;
            if (color) {
                playerText = TextureCache.get(Gdx.files.internal("tiles/bush2_color.png"));
                this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
                this.nameUpper = "bush2_color";
            }
            else {
                playerText = TextureCache.get(Gdx.files.internal("tiles/bush1.png"));
            }
            this.name = "green1";
            playerText = TextureCache.get(Gdx.files.internal("tiles/green1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
            this.attrs.put("cuttable", true);
            this.attrs.put("headbuttable", true);
//            bush2_color
//            this.items.put("log", 1);
//            String[] items = {"black apricorn", "blue apricorn", "green apricorn", "pink apricorn",
//                              "red apricorn", "white apricorn", "yellow apricorn"};
//            if (Game.staticGame.map.rand.nextInt(2) == 0) {
//                this.items.put(items[Game.staticGame.map.rand.nextInt(items.length)], 1);
//            }
        } else if (tileName.equals("bush2")) {
            Texture playerText;
            playerText = TextureCache.get(Gdx.files.internal("tiles/bush2_color.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(Gdx.files.internal("tiles/green1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        } else if (tileName.equals("tree_small1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/tree_small1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        } else if (tileName.equals("rock1")) {
            // TODO: remove
//            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/rock1.png"));
//            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
            // TODO: test
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/sand1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.name = "sand1";
            this.nameUpper = "rock1";
        } else if (tileName.equals("rock2")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/rock2.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        } else if (tileName.equals("rock3")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/rock3.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        } else if (tileName.equals("rock4")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/rock4.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.equals("rock5")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/sand1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.name = "sand1";
            this.nameUpper = tileName;
        } else if (tileName.equals("sand1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/sand1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.equals("sand2")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/sand2.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.equals("sand3") || tileName.equals("sand3_desertEdge")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/sand3.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.equals("path1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/path1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        // grass-like ground
        } else if (tileName.contains("green")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/"+tileName+".png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.contains("flower")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/"+tileName+".png"));
//            this.sprite = new Sprite(playerText, 0, 0, 32, 16);  // TODO: test
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.contains("snow")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/"+tileName+".png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        // tall tree
        } else if (tileName.equals("tree1")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/tree1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 32);
            this.attrs.put("solid", true);
        // tall tree - one sprite over, one under
        } else if (tileName.equals("tree2")) {
//            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/tree3_under.png"));
//            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
//            playerText = TextureCache.get(Gdx.files.internal("tiles/tree3_over.png"));
//            this.overSprite = new Sprite(playerText, 0, 0, 16, 32);
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/green1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(Gdx.files.internal("tiles/tree2.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 32);
            this.name = "green1";
            this.nameUpper = "tree2";
//            this.items.put("log", 2);
//            String[] items = {"black apricorn", "blue apricorn", "green apricorn", "pink apricorn",
//                              "red apricorn", "white apricorn", "yellow apricorn"};
//            if (Game.staticGame.map.rand.nextInt(2) == 0) {
//                this.items.put(items[Game.staticGame.map.rand.nextInt(items.length)], 2);
//            }
            this.attrs.put("solid", true);
//            this.attrs.put("tree", true);  // TODO: remove
            this.attrs.put("cuttable", true);
            this.attrs.put("headbuttable", true);
        } else if (tileName.equals("tree4")) {
            // TODO: remove
//            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/tree4_under.png"));
//            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
//            playerText = TextureCache.get(Gdx.files.internal("tiles/tree4_over.png"));
//            this.overSprite = new Sprite(playerText, 0, 0, 16, 32);
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/snow1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(Gdx.files.internal("tiles/tree4.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 32);
            this.name = "snow1";
            this.nameUpper = "tree4";
//            this.items.put("log", 2);
//            String[] items = {"black apricorn", "blue apricorn", "green apricorn", "pink apricorn",
//                              "red apricorn", "white apricorn", "yellow apricorn"};
//            if (Game.staticGame.map.rand.nextInt(2) == 0) {
//                this.items.put(items[Game.staticGame.map.rand.nextInt(items.length)], 2);
//            }
            this.attrs.put("solid", true);
//            this.attrs.put("tree", true);  // TODO: remove
            this.attrs.put("headbuttable", true);
        } else if (tileName.equals("tree5")) {
            //          Texture playerText = TextureCache.get(Gdx.files.internal("tiles/tree5_under.png"));
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/green1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            //          playerText = TextureCache.get(Gdx.files.internal("tiles/tree5_over.png"));
            playerText = TextureCache.get(Gdx.files.internal("tiles/tree6.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 32);
            this.attrs.put("solid", true);
            this.attrs.put("tree", true);
            this.attrs.put("headbuttable", true);
            this.attrs.put("cuttable", true);

        // TODO: not used anywhere afaik except tile editor
        } else if (tileName.equals("tree6")) {
            // TODO: this is just for tileEditor
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/tree6.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 32);
            this.attrs.put("solid", true);
            this.attrs.put("tree", true);
            this.attrs.put("headbuttable", true);
        } else if (tileName.equals("tree7")) {
            // TODO: this is just for tileEditor
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/tree7.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 32);
            this.attrs.put("solid", true);
            this.attrs.put("tree", true);
            this.attrs.put("headbuttable", true);
        } else if (tileName.equals("aloe_large1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/aloe_large1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 32, 32);
            this.attrs.put("solid", true);
        } else if (tileName.contains("cactus")) {
//            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/sand1.png"));
//            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/desert4.png")); 
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/desert6.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(Gdx.files.internal("tiles/"+tileName+".png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
            if (tileName.equals("cactus2") || tileName.equals("cactus3") || tileName.equals("cactus9")) {
                this.overSprite = new Sprite(playerText, 0, 0, 16, 32);
            }
            if (tileName.equals("cactus10")) {
                this.overSprite = new Sprite(playerText, 0, 0, 32, 16);
            }
            if (!tileName.equals("cactus10") && !tileName.equals("cactus7") && !tileName.equals("cactus8") && !tileName.equals("cactus9")) {
//                this.name = "sand1";
                this.name = "desert6";
                this.nameUpper = tileName;
            }
            this.attrs.put("solid", true);
            this.attrs.put("headbuttable", true);

        } else if (tileName.contains("ruins")) {
            // TODO: remove
//            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/"+tileName+".png"));
//            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            if (tileName.contains("pillar")) {
                Texture playerText = TextureCache.get(Gdx.files.internal("tiles/desert4.png"));
                this.sprite = new Sprite(playerText, 0, 0, 16, 16);
                playerText = TextureCache.get(Gdx.files.internal("tiles/"+tileName+".png"));
                this.overSprite = new Sprite(playerText, 0, 0, 16, 32);
                this.attrs.put("solid", true);  // TODO: not working
                this.drawAsTree = true;
            }
            else if (tileName.contains("floor")) {
                Texture playerText = TextureCache.get(Gdx.files.internal("tiles/"+tileName+".png"));
                this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            }
            else if (tileName.contains("picture")) {
                Texture playerText = TextureCache.get(Gdx.files.internal("tiles/"+tileName+".png"));
                this.sprite = new Sprite(playerText, 0, 0, 48, 32);
                this.attrs.put("solid", true);
            }
            else if (tileName.contains("path")) {
                Texture playerText = TextureCache.get(Gdx.files.internal("tiles/"+tileName+".png"));
                this.sprite = new Sprite(playerText, 0, 0, 48, 16);
            }
            else if (tileName.contains("wall")) {
                Texture playerText = TextureCache.get(Gdx.files.internal("tiles/"+tileName+".png"));
                this.sprite = new Sprite(playerText, 0, 0, 16, 16);
                this.attrs.put("solid", true);
            }
            else {
                Texture text = TextureCache.get(Gdx.files.internal("tiles/ruins1_all.png"));
                int offsetX = 8;
                int offsetY = 8;
                if (this.name.contains("E")) {
                    offsetX -= 8;
                }
                if (this.name.contains("W")) {
                    offsetX += 8;
                }
                if (this.name.contains("N")) {
                    offsetY += 8;
                }
                if (this.name.contains("S")) {
                    offsetY -= 8;
                }
                this.sprite = new Sprite(text, offsetX, offsetY, 16, 16);
            }
        } else if (tileName.equals("tree_plant1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/green1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(Gdx.files.internal("tiles/buildings/plant1.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 32);
            this.attrs.put("solid", true);
            this.attrs.put("tree", true);
        } else if (tileName.equals("pkmnmansion_statue1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/buildings/pkmnmansion_floor1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(Gdx.files.internal("tiles/buildings/pkmnmansion_statue1.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 32);
            this.attrs.put("solid", true);
        } else if (tileName.equals("pkmnmansion_struct1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/buildings/pkmnmansion_floor1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(Gdx.files.internal("tiles/buildings/pkmnmansion_struct1.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 32, 32);
            this.attrs.put("solid", true);
        } else if (tileName.equals("pkmnmansion_shelf1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/buildings/pkmnmansion_floor1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(Gdx.files.internal("tiles/buildings/pkmnmansion_shelf1.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 32);
            this.attrs.put("solid", true);
        } else if (tileName.equals("pkmnmansion_shelf1_NS")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/buildings/pkmnmansion_floor1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(Gdx.files.internal("tiles/buildings/pkmnmansion_shelf1_NS.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 32);
            this.attrs.put("solid", true);
        // colored water
        } else if (tileName.equals("water2")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/water2.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
            this.attrs.put("water", true);
        }
        else if (tileName.equals("water3")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/water3.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
            this.attrs.put("water", true);
        }
        else if (tileName.equals("water5")) {
            // TODO: ended up not going with this.
            // Could revisit in the future.
//            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/water5.png"));
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/water2.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
            this.attrs.put("water", true);
        }
        else if (tileName.equals("black1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/blank3.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        }
        else if (tileName.equals("blank1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/blank.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        }
        else if (tileName.equals("campfire1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/campfire1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 20);
            this.attrs.put("solid", true);
        }
        else if (tileName.equals("fence1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/fence1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 20);  // TODO: why 20?
            this.attrs.put("solid", true);
        }
        else if (tileName.equals("fence2")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/fence2.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        }
        // TODO: unify water2 and water5?
        // TODO: the bridge stuff is very convoluted b/c
        //       it's currently storing water type in the name.
        //       'bridge' needs to stay in nameLower tho b/c
        //       want to be able to build buildings on it.
        else if (tileName.equals("bridge1_water2_lower")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/water2.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("water", true);
            this.attrs.put("solid", true);
            this.belowBridge = true;  // Draw bridge supports over this tile
        }
        else if (tileName.equals("bridge1_water5_lower")) {
//            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/water5.png"));
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/water2.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("water", true);
            this.attrs.put("solid", true);
            this.belowBridge = true;  // Draw bridge supports over this tile
        }
        // Has to be after previous two checks
        else if (tileName.contains("bridge")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/bridge1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("cuttable", true);
        }
        else if (tileName.equals("sleeping_bag1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/sleeping_bag1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 24, 16);
        }
        else if (tileName.equals("house_bed1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/buildings/house_bed1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 32);
            this.attrs.put("solid", true);
        }
        else if (tileName.equals("house_plant1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/buildings/house_plant1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 32);
            this.attrs.put("solid", true);
        }
        else if (tileName.equals("house_plant2")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/buildings/house_plant2.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 32);
            this.attrs.put("solid", true);
        }
        else if (tileName.equals("house_gym1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/buildings/house_gym1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 32);
            this.attrs.put("solid", true);
        }
        else if (tileName.equals("house_wardrobe1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/buildings/house_wardrobe1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 32);
            this.attrs.put("solid", true);
        }
        else if (tileName.contains("house_vanity")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/buildings/"+tileName+".png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 32);
            this.attrs.put("solid", true);
        }
        else if (tileName.contains("house_couch1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/buildings/"+tileName+".png"));
            this.sprite = new Sprite(playerText, 0, 0, 32, 16);
            this.attrs.put("solid", true);
        }
        else if (tileName.contains("house_couch")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/buildings/"+tileName+".png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 32);
            this.attrs.put("solid", true);
        }
        else if (tileName.equals("house_shelf1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/buildings/house_shelf1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 32);
            this.attrs.put("solid", true);
        }
        else if (tileName.equals("house_stool1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/buildings/house_stool1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        }
        else if (tileName.equals("torch1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/torch_sheet1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 20);
        }
        else if (tileName.contains("desert")) {
            if (tileName.equals("desert2_trapinch_spawn")) {
                tileName = "desert2";
            }
            // TODO: test
            if (tileName.equals("desert4_isGrass")) {
                tileName = "desert4";
                this.attrs.put("grass", true);
            }
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/"+tileName+".png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            
            if (tileName.equals("desert2")) {
                this.attrs.put("grass", true);
                this.overSprite = this.sprite;
            }

        }
        else if (tileName.equals("grass_sand2")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/desert1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(Gdx.files.internal("tiles/grass2_over.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("grass", true);
            this.attrs.put("cuttable", true);
        }
        else if (tileName.equals("grass_sand3")) {
//            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/desert4.png"));
//            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/sand1.png"));
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/desert6.png"));
            
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(Gdx.files.internal("tiles/grass5_over.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("grass", true);
            this.attrs.put("cuttable", true);
        } 
        else if (tileName.equals("grass_graveyard1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/green11.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(Gdx.files.internal("tiles/grass6_over.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("grass", true);
            this.attrs.put("cuttable", true);
        }
        else if (tileName.contains("house6")) {
            // Don't do any work here; buildings always have something ground-related as
            // the lower name.
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/blank3.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.nameUpper = tileName;
        }
        else if (tileName.contains("house7")) {
            // Don't do any work here; buildings always have something ground-related as
            // the lower name.
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/blank3.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.nameUpper = tileName;
        }
        else if (tileName.contains("potted")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/blank.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.nameUpper = tileName;
        }
        else if (tileName.contains("hole1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/hole1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        }
        else if (tileName.equals("building1_pokecenter1_right")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/blank.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        }
        else if (tileName.equals("building1_pokecenter1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/buildings/building1_pokecenter1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 32, 32);
            this.attrs.put("solid", true);
        }
        else if (tileName.equals("building1_machine2")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/buildings/building1_machine2.png"));
            this.sprite = new Sprite(playerText, 0, 0, 96, 32);
            this.attrs.put("solid", true);
        }
        else if (tileName.equals("pedistal1")) {
//            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/pedistal1.png"));
//            this.sprite = new Sprite(playerText, 0, 0, 16, 32);
//            this.attrs.put("solid", true);
            this.nameUpper = name;
        }
        else if (tileName.equals("statue1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/statue1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 32);
            this.attrs.put("solid", true);
        }
        else if (tileName.contains("gate1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/"+tileName+".png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        }
        else if (tileName.equals("building1_fossilreviver1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/blank.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.nameUpper = tileName;
        }
        else if (tileName.equals("interiorwall1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/blank.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.nameUpper = tileName;
        }
        else if (tileName.contains("gravestone")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/blank.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.nameUpper = tileName;
        }
        else if (tileName.contains("sign_")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/blank.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.nameUpper = tileName;
        }
        else if (tileName.contains("ledges3")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/blank.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.nameUpper = tileName;
        }
        else {
            // just load from image file
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/buildings/"+tileName+".png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            if (!tileName.contains("door") && !tileName.contains("floor") && 
                !tileName.contains("rug") && !tileName.contains("__off") &&
                !tileName.contains("carpet") && !tileName.contains("cables")) {
                this.attrs.put("solid", true);
            }
        }
        if (tileName.contains("pkmnmansion_floor")) {
            this.attrs.put("grass", true);
        }
        
        // TODO: refactors
        //  - just load file based on tile name. Names can probably contain slashes.
        //  - remove references above to setting overSprite, and just have whatever calls Tile() pass in nameUpper.
        //  - remove attrs, just do booleans isCuttable, isLedge, etc.
        // TODO: it would be even better if this.name was just in the format "lower:upper".
        //  - that way code can just check tile.name once without checking both. Also keeps backwards compatibility.

        // if there is an 'upper' (above terrain' object, load it and set oversprite to it.
        if (!this.nameUpper.equals("")){
            // Load from image file based on the name
            Texture text;
            if (this.nameUpper.contains("REGI")) {
                text = TextureCache.get(Gdx.files.internal("tiles/cave1/overworld2.png"));
                if (this.nameUpper.equals("REGIDRAGO")) {
                    this.overSprite = new Sprite(text, 16, 0, 16, 16);
                }
                else if (this.nameUpper.equals("REGIELEKI")) {
                    this.overSprite = new Sprite(text, 32, 0, 16, 16);
                }
                else if (this.nameUpper.equals("REGICE")) {
                    this.overSprite = new Sprite(text, 48, 0, 16, 16);
                }
                else if (this.nameUpper.equals("REGIROCK")) {
                    this.overSprite = new Sprite(text, 64, 0, 16, 16);
                }
                else if (this.nameUpper.equals("REGISTEEL")) {
                    this.overSprite = new Sprite(text, 80, 0, 16, 16);
                }
            }
            else if (this.nameUpper.contains("ledges3")) {
                text = TextureCache.get(Gdx.files.internal("tiles/ledges3.png"));
                int offsetX = 8;
                int offsetY = 8;
                if (this.nameUpper.contains("E")) {
                    offsetX -= 8;
                }
                if (this.nameUpper.contains("W")) {
                    offsetX += 8;
                }
                if (this.nameUpper.contains("N")) {
                    offsetY += 8;
                }
                if (this.nameUpper.contains("S")) {
                    offsetY -= 8;
                }
                this.overSprite = new Sprite(text, offsetX, offsetY, 16, 16);
            }
            else if (this.nameUpper.contains("house6")) {
                // TODO: move to fn
                String name = "house6";
                if (this.nameUpper.contains("roof")) {
                    name += "_roof";
                }
                if (this.nameUpper.contains("door") ||
                        this.nameUpper.contains("left") ||
                        this.nameUpper.contains("middle") ||
                        this.nameUpper.contains("right")) {
                    text = TextureCache.get(Gdx.files.internal("tiles/buildings/"+this.nameUpper+".png"));
                    this.overSprite = new Sprite(text, 0, 0, 16, 16);
                }
                else {
                    text = TextureCache.get(Gdx.files.internal("tiles/buildings/"+name+"_all.png"));
                    int offsetX = 8;
                    int offsetY = 8;
                    if (this.nameUpper.contains("E")) {
                        offsetX -= 8;
                    }
                    if (this.nameUpper.contains("W")) {
                        offsetX += 8;
                    }
                    if (this.nameUpper.contains("N")) {
                        offsetY += 8;
                    }
                    if (this.nameUpper.contains("S")) {
                        offsetY -= 8;
                    }
                    this.overSprite = new Sprite(text, offsetX, offsetY, 16, 16);
                }
//                this.attrs.put("solid", true);
//                this.attrs.put("cuttable", true);
            }
            else if (this.nameUpper.contains("house7_roof")) {
                text = TextureCache.get(Gdx.files.internal("tiles/house7_all.png"));
                this.overSprite = new Sprite(text, 16, 16, 16, 16);
                String[] exts = this.nameUpper.split("_");
                if (exts.length > 1) {
                    String ext = exts[exts.length-1];
                    if (ext.equals("E")) {
                        this.overSprite.setRegion(0, 0, 16, 16);
                    }
                    else if (ext.equals("EW")) {
                        this.overSprite.setRegion(16, 0, 16, 16);
                    }
                    else if (ext.equals("SE")) {
                        this.overSprite.setRegion(64, 0, 16, 16);
                    }
                    else if (ext.equals("W")) {
                        this.overSprite.setRegion(48, 0, 16, 16);
                    }
                    else if (ext.equals("SW")) {
                        this.overSprite.setRegion(96, 0, 16, 16);
                    }
                    else if (ext.equals("NE")) {
                        this.overSprite.setRegion(64, 16, 16, 16);
                    }
                    else if (ext.equals("NEW") || ext.equals("N")) {
                        this.overSprite.setRegion(80, 16, 16, 16);
                    }
                    else if (ext.equals("NW")) {
                        this.overSprite.setRegion(96, 16, 16, 16);
                    }
                    else if (ext.equals("NSE")) {
                        this.overSprite.setRegion(112, 0, 16, 16);
                    }
                    else if (ext.equals("S") || ext.equals("SEW")) {
                        this.overSprite.setRegion(80, 0, 16, 16);
                    }
                    else if (ext.equals("NSEW")) {
                        this.overSprite.setRegion(128, 0, 16, 16);
                    }
                    else if (ext.equals("NSW")) {
                        this.overSprite.setRegion(144, 0, 16, 16);
                    }
                }
            }
            else if (this.nameUpper.contains("house7")) {
                text = TextureCache.get(Gdx.files.internal("tiles/house7_all.png"));
                this.overSprite = new Sprite(text, 176, 16, 16, 16);
                String[] exts = this.nameUpper.split("_");
                if (exts.length > 1) {
                    String ext = exts[exts.length-1];
                    if (this.nameUpper.contains("door")) {
                        this.overSprite.setRegion(208, 16, 16, 16);
                    }
//                    else if (this.nameUpper.contains("window")) {
//                        this.overSprite.setRegion(208, 0, 16, 16);
//                    }
                    else if (ext.equals("E")) {
                        this.overSprite.setRegion(0, 16, 16, 16);
                    }
                    else if (ext.equals("SE")) {
                        this.overSprite.setRegion(16, 16, 16, 16);
                    }
                    else if (ext.equals("W")) {
                        this.overSprite.setRegion(48, 16, 16, 16);
                    }
                    else if (ext.equals("SW")) {
                        this.overSprite.setRegion(32, 16, 16, 16);
                    }
                    else if (ext.equals("NE")) {
                        this.overSprite.setRegion(160, 16, 16, 16);
                    }
                    else if (ext.equals("NEW") || ext.equals("N")) {
                        this.overSprite.setRegion(176, 16, 16, 16);
                    }
                    else if (ext.equals("NW")) {
                        this.overSprite.setRegion(192, 16, 16, 16);
                    }
                    else if (ext.equals("NSE")) {
                        this.overSprite.setRegion(160, 0, 16, 16);
                    }
                    else if (ext.equals("NSEW")) {
                        this.overSprite.setRegion(176, 0, 16, 16);
                    }
                    else if (ext.equals("S") || ext.equals("SEW")) {
                        this.overSprite.setRegion(32, 0, 16, 16);
                    }
                    else if (ext.equals("NSW")) {
                        this.overSprite.setRegion(192, 0, 16, 16);
                    }
                    
                }

                if (this.nameUpper.contains("window")) {
                    // TODO: test
                    if (!Tile.dynamicTexts.containsKey(this.nameUpper)) {
                        Texture[] textures = new Texture[1];
                        TextureData temp = text.getTextureData();
                        if (!temp.isPrepared()) {
                            temp.prepare();
                        }
                        Pixmap oldPixmap = temp.consumePixmap();
                        Pixmap newPixmap = new Pixmap(16, 16, Format.RGBA8888);
                        int left, bottom;
                        left = this.overSprite.getRegionX();
                        bottom = this.overSprite.getRegionY();
//                        regionWidth = this.overSprite.getRegionWidth();
//                        regionHeight = this.overSprite.getRegionHeight();

                        text = TextureCache.get(Gdx.files.internal("tiles/windows1.png"));
                        temp = text.getTextureData();
                        if (!temp.isPrepared()) {
                            temp.prepare();
                        }
                        Pixmap currPixmap = temp.consumePixmap();
                        int color2;
                        for (int m=0; m < newPixmap.getWidth(); m++) {
                            for (int n=0; n < newPixmap.getHeight(); n++) {
                                if (n < 8) {
                                    if (m < 8 && !this.nameUpper.contains("E")) {
                                        color2 = currPixmap.getPixel(m, n);
                                    }
                                    else if (m >= 8 && !this.nameUpper.contains("W")) {
                                        color2 = currPixmap.getPixel(m, n);
                                    }
                                    else if (this.nameUpper.contains("EW")) {
                                        color2 = currPixmap.getPixel(m, n);
                                    }
                                    else {
                                        color2 = oldPixmap.getPixel(left+m, bottom+n);
                                    }
                                }
                                else {
                                    color2 = oldPixmap.getPixel(left+m, bottom+n);
                                }
                                newPixmap.drawPixel(m, n, color2);
                            }
                        }
                        textures[0] = new Texture(newPixmap);
                        Tile.dynamicTexts.put(this.nameUpper, textures);
                    }
                    text = Tile.dynamicTexts.get(this.nameUpper)[0];
                    this.overSprite = new Sprite(text, 0, 0, 16, 16);
                }
                
            }
            else if (this.nameUpper.contains("house") && !this.nameUpper.contains("|")) {
                text = TextureCache.get(Gdx.files.internal("tiles/buildings/"+this.nameUpper+".png"));
                this.overSprite = new Sprite(text, 0, 0, 16, 16);
            }
            else if (this.nameUpper.equals("pedistal1") ||
                     this.nameUpper.equals("ruins_statue1") ||
                     this.nameUpper.equals("statue1")) {
                text = TextureCache.get(Gdx.files.internal("tiles/"+this.nameUpper+".png"));
                this.overSprite = new Sprite(text, 0, 0, 16, 32);
                this.drawAsTree = true;
            }
            else if (this.nameUpper.contains("building1_fossilreviver1")) {
                text = TextureCache.get(Gdx.files.internal("tiles/buildings/building1_pc1.png"));
//                text = TextureCache.get(Gdx.files.internal("tiles/buildings/building1_pc2.png"));
                this.overSprite = new Sprite(text, 0, 0, 16, 24);
            }
            else if (this.nameUpper.equals("solid")) {
                // No sprite, just invisible solid block
                text = TextureCache.get(Gdx.files.internal("tiles/blank3.png"));
            }
            else if (this.nameUpper.equals("onpress_above")) {
                // No sprite, just invisible solid block
                text = TextureCache.get(Gdx.files.internal("tiles/blank3.png"));
            }
            else if (this.nameUpper.equals("onpress_left")) {
                // No sprite, just invisible solid block
                text = TextureCache.get(Gdx.files.internal("tiles/blank3.png"));
            }
            else if (this.nameUpper.contains("revived")) {
                text = TextureCache.get(Gdx.files.internal("tiles/blank3.png"));
            }
            // TODO: some way to flag that a pokemon is on this tile
            else if (this.nameUpper.contains("volcarona")) {
                text = TextureCache.get(Gdx.files.internal("tiles/blank3.png"));
            }
            else if (this.nameUpper.contains("spiritomb")) {
                text = TextureCache.get(Gdx.files.internal("tiles/blank3.png"));
            }
            else if (this.nameUpper.contains("hole1_water")) {
                if (!Tile.dynamicTexts.containsKey(this.nameUpper)) {
                    String[] names = this.nameUpper.split("_");
                    String ending = "";
                    if (names.length > 2) {
                        if (!names[names.length-1].equals("default")) {
                            ending = "_"+names[names.length-1];
                        }
                    }
                    Texture[] textures = new Texture[4];
                    for (int k = 0; k < 4; k++) {
                        // Draw pixmap over prevTexture
                        text = TextureCache.get(Gdx.files.internal("tiles/hole1"+ending+".png"));
                        TextureData temp = text.getTextureData();
                        if (!temp.isPrepared()) {
                            temp.prepare();
                        }
                        Pixmap newPixmap = temp.consumePixmap();
                        int height = text.getHeight();
                        int width = text.getWidth();

                        text = TextureCache.get(Gdx.files.internal("tiles/water2.png"));
                        temp = text.getTextureData();
                        if (!temp.isPrepared()) {
                            temp.prepare();
                        }
                        Pixmap currPixmap = temp.consumePixmap();
                        for (int i=0, j=0; j < height; i++) {
                            if (i > width) {
                                i=-1;
                                j++;
                                continue;
                            }
                            Color color3 = new Color(newPixmap.getPixel(i, j));
                            boolean isBlack = color3.r == 56f/255f && color3.g == 56f/255f && color3.b == 56f/255f;
                            if (color3.a == 0) {  // || isBlack
                                continue;
                            }
                            Color color2 = new Color(currPixmap.getPixel(i+k, j));
//                            newPixmap.drawPixel(i, j, Color.rgba8888((color2.r+color3.r)/2, (color2.g+color3.g)/2, (color2.b+color3.b)/2, color2.a));
                            newPixmap.drawPixel(i, j, Color.rgba8888(color2.r, color2.g, color2.b, color2.a));
                        }
                        textures[k] = TextureCache.get(newPixmap);
                    }
                    // Pre-cache that frame with the colors swapped
                    Tile.dynamicTexts.put(this.nameUpper, textures);
                }
                text = Tile.dynamicTexts.get(this.nameUpper)[0];
                this.overSprite = new Sprite(text, 0, 0, 16, 16);
            }
            else if (this.nameUpper.contains("hole1")) {
                if (!Tile.dynamicTexts.containsKey(this.nameUpper)) {
                    String name = new String(this.nameUpper);
                    name = name.replace("[NE]", "");
                    name = name.replace("[SE]", "");
                    name = name.replace("[SW]", "");
                    name = name.replace("[NW]", "");
                    Texture[] textures = new Texture[1];
                    // Draw pixmap over prevTexture
                    text = TextureCache.get(Gdx.files.internal("tiles/"+name+".png"));
                    TextureData temp = text.getTextureData();
                    if (!temp.isPrepared()) {
                        temp.prepare();
                    }
                    Pixmap newPixmap = temp.consumePixmap();
                    int height = text.getHeight();
                    int width = text.getWidth();
                    for (int i=0, j=0; j < height; i++) {
                        if (i > width) {
                            i=-1;
                            j++;
                            continue;
                        }
                        if (j < 10) {
                            if (i < 8) {
                                if (this.nameUpper.contains("[NW]")) {
                                    newPixmap.drawPixel(i, j, Color.rgba8888(160f/255f, 120f/255f, 24f/255f, 1f));
                                }
                            }
                            else {
                                if (this.nameUpper.contains("[NE]")) {
                                    newPixmap.drawPixel(i, j, Color.rgba8888(160f/255f, 120f/255f, 24f/255f, 1f));
                                }
                            }
                        }
                        else {
                            if (i < 8) {
                                if (this.nameUpper.contains("[SW]")) {
                                    newPixmap.drawPixel(i, j, Color.rgba8888(160f/255f, 120f/255f, 24f/255f, 1f));
                                }
                            }
                            else {
                                if (this.nameUpper.contains("[SE]")) {
                                    newPixmap.drawPixel(i, j, Color.rgba8888(160f/255f, 120f/255f, 24f/255f, 1f));
                                }
                            }
                        }
                    }
                    textures[0] = TextureCache.get(newPixmap);
                    // Pre-cache that frame with the colors swapped
                    Tile.dynamicTexts.put(this.nameUpper, textures);
                }
//                text = TextureCache.get(Gdx.files.internal("tiles/"+this.nameUpper+".png"));
                text = Tile.dynamicTexts.get(this.nameUpper)[0];
                this.overSprite = new Sprite(text, 0, 0, 16, 16);
            }
            else if (this.nameUpper.contains("interiorwall1")) {
                if (!Tile.dynamicTexts.containsKey(this.nameUpper)) {
                    Texture[] textures = new Texture[1];
                    // Draw pixmap over prevTexture
                    text = TextureCache.get(Gdx.files.internal("tiles/buildings/interiorwall1.png"));
                    TextureData temp = text.getTextureData();
                    if (!temp.isPrepared()) {
                        temp.prepare();
                    }
                    Pixmap currPixmap = temp.consumePixmap();
                    int width = 16;
                    int height = 32;
                    Pixmap newPixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
                    Vector2 bl = new Vector2(0, 0);
                    Vector2 tr = new Vector2(8, 8);
                    if (this.nameUpper.contains("N")) {
                        bl.add(0, 8);
                    }
                    if (this.nameUpper.contains("S")) {
                        tr.add(0, -8);
                    }
                    if (this.nameUpper.contains("E")) {
                        tr.add(-8, 0);
                    }
                    if (this.nameUpper.contains("W")) {
                        bl.add(8, 0);
                    }
                    int offsetX=0, offsetY=0;
                    int color3;
                    for (int i=0, j=0; j < 16; i++) {
                        if (i > width) {
                            i=-1;
                            j++;
                            continue;
                        }
                        if (i < 8) {
                            offsetX = (int)bl.x;
                        }
                        else {
                            offsetX = (int)tr.x;
                        }
                        if (j < 8) {
                            offsetY = (int)bl.y;
                        }
                        else {
                            offsetY = (int)tr.y;
                        }
                        color3 = currPixmap.getPixel(i+offsetX, j+offsetY);
                        newPixmap.drawPixel(i, j, color3);
                    }
                    // Draw wall on lower portion
                    text = TextureCache.get(Gdx.files.internal("tiles/buildings/house5_wall1.png"));
                    temp = text.getTextureData();
                    if (!temp.isPrepared()) {
                        temp.prepare();
                    }
                    currPixmap = temp.consumePixmap();
                    for (int i=0, j=0; j < 16; i++) {
                        if (i > width) {
                            i=-1;
                            j++;
                            continue;
                        }
                        color3 = currPixmap.getPixel(i, j);
                        newPixmap.drawPixel(i, j+16, color3);
                    }
                    // TODO: not ideal, hacky
                    String[] names = this.nameUpper.split("\\|");
                    if (names.length > 1) {
                        String name = names[1];
                        text = TextureCache.get(Gdx.files.internal("tiles/buildings/"+name+".png"));
                        temp = text.getTextureData();
                        if (!temp.isPrepared()) {
                            temp.prepare();
                        }
                        currPixmap = temp.consumePixmap();
                        for (int i=0, j=0; j < 16; i++) {
                            if (i > width) {
                                i=-1;
                                j++;
                                continue;
                            }
                            color3 = currPixmap.getPixel(i, j);
                            newPixmap.drawPixel(i, j+16, color3);
                        }
                    }
                    textures[0] = TextureCache.get(newPixmap);
                    Tile.dynamicTexts.put(this.nameUpper, textures);
                }
                text = Tile.dynamicTexts.get(this.nameUpper)[0];
                this.overSprite = new Sprite(text, 0, 0, 16, 32);
            }
            else if (this.nameUpper.contains("_cracked")) {
                text = TextureCache.get(Gdx.files.internal("tiles/desert4_cracked.png"));
                this.overSprite = new Sprite(text, 0, 0, 16, 16);
            }
            else if (this.nameUpper.equals("tree_large1")) {
                text = TextureCache.get(Gdx.files.internal("tiles/tree_large1_color.png"));
                this.overSprite = new Sprite(text, 0, 0, 32, 32);
            }
            else if (this.nameUpper.contains("pokedoll1")) {
                text = TextureCache.get(Gdx.files.internal("tiles/pokedoll1.png"));
                this.overSprite = new Sprite(text, 0, 0, 16, 16);
            }
            else {
                text = TextureCache.get(Gdx.files.internal("tiles/"+this.nameUpper+".png"));
                this.overSprite = new Sprite(text, 0, 0, 16, 16);
            }
            if (this.nameUpper.equals("house_couch1")) {
                this.overSprite = new Sprite(text, 0, 0, 32, 16);
            }
            else if (this.nameUpper.contains("house_couch")) {
                this.overSprite = new Sprite(text, 0, 0, 16, 32);
            }
            if (this.nameUpper.equals("house_bed1") ||
                this.nameUpper.contains("tree2") ||
                this.nameUpper.contains("tree4") ||
                this.nameUpper.contains("tree6") ||
                this.nameUpper.contains("tree7") ||
                this.nameUpper.contains("house_plant1") ||
                this.nameUpper.contains("house_plant2") ||
                this.nameUpper.contains("house_gym1") ||
                this.nameUpper.contains("house_wardrobe1") ||
                this.nameUpper.contains("house_vanity") ||
                this.nameUpper.contains("cactus2") ||
                this.nameUpper.contains("cactus3") ||
                this.nameUpper.contains("cactus9") ||
                this.nameUpper.contains("house_shelf1")) {
                this.overSprite = new Sprite(text, 0, 0, 16, 32);
            }
            if (this.nameUpper.equals("cactus10")) {
                this.overSprite = new Sprite(text, 0, 0, 32, 16);
            }
            if (this.nameUpper.equals("aloe_large1")) {
                this.overSprite = new Sprite(text, 0, 0, 32, 32);
            }
            if (!this.nameUpper.contains("door") &&
                !this.nameUpper.contains("floor") &&
                !this.nameUpper.contains("tree_planted") &&
                !this.nameUpper.contains("stool") &&
                !this.nameUpper.contains("gate") &&
                !this.nameUpper.equals("grass_planted") &&
                !this.nameUpper.contains("_cracked")) {
                this.attrs.put("solid", true);
            }

            if (this.nameUpper.contains("cactus1") ||
                this.nameUpper.contains("cactus2")) {
                this.attrs.put("headbuttable", true);
            }

            // TODO: do inclusive, not exclusive
            // every time I add something with a new nameUpper,
            // it is cuttable by default
            if (!this.nameUpper.contains("floor") &&
                !this.nameUpper.contains("pokeball") &&
                !this.nameUpper.contains("bush") &&
                !this.nameUpper.equals("pokemon_mansion_key") &&
                !this.nameUpper.contains("REGI") &&
                !this.nameUpper.contains("tree") &&
                !this.nameUpper.contains("rock") &&
                !this.nameUpper.equals("aloe_large1") &&
                !this.nameUpper.contains("onpress") &&
                !this.nameUpper.equals("solid")) {
                this.attrs.put("cuttable", true);
            }
            if (this.nameUpper.contains("bush2_color") ||
                this.nameUpper.equals("tree2") ||
                this.nameUpper.equals("tree6") ||
                this.nameUpper.equals("tree7") ||
                this.nameUpper.equals("tree4")) {
                this.attrs.put("headbuttable", true);
                this.attrs.put("cuttable", true);
//                this.items.put("log", 1);  // TODO: remove
            }
            if (this.nameUpper.equals("pokeball1")) {
                this.attrs.put("solid", true);
                this.hasItem = "pok? ball";
                this.hasItemAmount = 1;
            }
            else if (this.nameUpper.equals("ultraball1")) {
                this.attrs.put("solid", true);
                this.hasItem = "ultra ball";
                this.hasItemAmount = 1;
            }
            if (this.nameUpper.equals("pokemon_mansion_key")) {
                this.attrs.put("solid", true);
                this.hasItem = "secret key";
                this.hasItemAmount = 1;
            }
            if (this.nameUpper.contains("stairs")) {
                this.attrs.put("solid", false);
                this.attrs.put("grass", false);
            }
            if (this.nameUpper.equals("mewtwo_overworld_hidden")) {
                this.attrs.put("solid", false);
            }
            if (this.nameUpper.equals("sign1")) {
                this.attrs.put("cuttable", false);  // ruins unown spawns otherwise
            }
            if (this.nameUpper.equals("rock1") ||
                this.nameUpper.equals("rock1_color") ||
                this.nameUpper.equals("rock5") ||
                this.nameUpper.contains("gravestone")) {
                this.attrs.put("smashable", true);
                this.attrs.put("cuttable", false);  // not sure why but cuttable can be true here.
            }
            
            // TODO: test
            if (this.nameUpper.contains("fence")) {
                if (this.nameUpper.equals("fence1") ||
                    this.nameUpper.equals("fence2")) {
                    this.attrs.put("ledge", true);
                    this.ledgeDir = "down";
                }
                else if (this.nameUpper.equals("fence1_NS") ||
                         this.nameUpper.equals("fence2_NS")) {
                    this.attrs.put("ledge", true);
                    this.ledgeDir = "left";
                }
            }

            if (this.nameUpper.contains("stairs") ||
                this.nameUpper.contains("door") ||
                this.nameUpper.contains("cracked") ||
                this.nameUpper.equals("grass_planted") ||
                this.nameUpper.contains("stool")) {
                this.drawUpperBelowPlayer = true;
            }
                    
        }
        if (this.overSprite != null &&
            !this.nameUpper.contains("tree_planted") && 
            (this.name.contains("tree") || this.nameUpper.contains("tree") ||
             this.nameUpper.contains("house_gym") || this.nameUpper.contains("house_plant") ||
             this.nameUpper.equals("cactus2") ||
             this.nameUpper.equals("building1_fossilreviver1") ||
             this.nameUpper.contains("house_shelf") ||
             this.nameUpper.contains("interiorwall") ||
             this.nameUpper.contains("house_wardrobe"))) {
            this.drawAsTree = true;
        }

        this.sprite.setPosition(pos.x, pos.y);
        if (this.overSprite != null) {
            this.overSprite.setPosition(pos.x, pos.y);

            if (this.nameUpper.equals("rock1_color")) {
                this.overSprite.setPosition(pos.x, pos.y+4);
            }
        }
    }

    public Tile(String tileName, Vector2 pos) {
        this(tileName, "", pos);
    }

    public Tile(String tileName, Vector2 pos, boolean color) {
        this(tileName, "", pos, color, null);
    }

    public Tile(String tileName, Vector2 pos, boolean color, Route routeBelongsTo) {
        this(tileName, "", pos, color, routeBelongsTo);
    }
    
    /**
     * Used to know whether or not player can set the text on this tile.
     */
    public boolean isSign() {
        return this.nameUpper.equals("gravestone3") ||
               this.nameUpper.equals("sign_built1") ||
               this.nameUpper.equals("sign_desert1");
    }
    
    /**
     * Add this Tile's color data to the minimap.
     * 
     * This method needs to get decent performance.
     */
    public void updateMiniMap(Game game) {
        // Some tiles don't get drawn to minimap.
        if (this.name.equals("tree_large1_noSprite") ||
            this.name.equals("blank1")) {
            return;
        }

        TextureData temp;
        int left, bottom, regionWidth, regionHeight;
        if (this.overSprite != null) {
            temp = this.overSprite.getTexture().getTextureData();
            left = this.overSprite.getRegionX();
            bottom = this.overSprite.getRegionY();
            regionWidth = this.overSprite.getRegionWidth();
            regionHeight = this.overSprite.getRegionHeight();
        }
        else {
            temp = this.sprite.getTexture().getTextureData();
            left = this.sprite.getRegionX();
            bottom = this.sprite.getRegionY();
            regionWidth = this.sprite.getRegionWidth();
            regionHeight = this.sprite.getRegionHeight();
        }
        if (!temp.isPrepared()) {
            temp.prepare();
        }

//        if (this.name.equals("tree_large1")) {
//            System.out.println(regionWidth);
//            System.out.println(regionHeight);
//        }
                
        Pixmap currPixmap = temp.consumePixmap();
        int offset = 7;
        if (this.name.contains("grass")) {
            offset = 4;
        }
        else if (this.name.equals("tree_large1")) {
            offset = 6;
        }
        int x = (int)(this.position.x-game.map.bottomLeft.x)/8;
        int y = (int)(game.map.topRight.y-this.position.y)/8;
        int color;
//        int color = currPixmap.getPixel(0, 0);
//        game.map.minimap.drawPixel(x, y, color);
//        if (currPixmap.getWidth() > 16) {
//            game.map.minimap.drawPixel(x+2, y, color);
//            if (currPixmap.getHeight() > 16) {
//                game.map.minimap.drawPixel(x, y+2, color);
//            }
//        }
//        if (currPixmap.getHeight() > 16) {
//            game.map.minimap.drawPixel(x, y+2, color);
//        }
//        color = currPixmap.getPixel(currPixmap.getWidth()-1, 0);
//        game.map.minimap.drawPixel(x+1, y, color);
//        color = currPixmap.getPixel(0, currPixmap.getHeight()-1);
//        game.map.minimap.drawPixel(x, y+1, color);
//        color = currPixmap.getPixel(currPixmap.getWidth()-1, currPixmap.getHeight()-1);
//        game.map.minimap.drawPixel(x+1, y+1, color);

        // I suspect the perf for this isn't good.
        int offsetY = ((regionHeight/16)-1)*2;
        for (int m=0; m*16 < regionWidth; m++) {
            for (int n=0; n*16 < regionHeight; n++) {
                color = currPixmap.getPixel((m)*16+offset+left, (n)*16+offset+bottom);
                Game.staticGame.map.minimap.drawPixel(x+(m*2), y+(n*2)-offsetY, color);
                color = currPixmap.getPixel((m+1)*16-1-offset+left, (n)*16+offset+bottom);
                Game.staticGame.map.minimap.drawPixel(x+(m*2)+1, y+(n*2)-offsetY, color);
                color = currPixmap.getPixel((m)*16+offset+left, (n+1)*16-1-offset+bottom);
                Game.staticGame.map.minimap.drawPixel(x+(m*2), y+(n*2)+1-offsetY, color);
                color = currPixmap.getPixel((m+1)*16-1-offset+left, (n+1)*16-1-offset+bottom);
                Game.staticGame.map.minimap.drawPixel(x+(m*2)+1, y+(n*2)+1-offsetY, color);
            }
        }
    }

    /**
     * Remove upper part of tile and put pokeball in inventory.
     * @param player
     */
    public void pickUpItem(Player player) {
        if (!this.nameUpper.contains("hole")) {
            this.overSprite = null;
            this.nameUpper = "";
            this.attrs.put("solid", false);
        }
        int amount = this.hasItemAmount;
        if (player.itemsDict.containsKey(this.hasItem)) {
            amount += player.itemsDict.get(this.hasItem);
        }
        player.itemsDict.put(this.hasItem, amount);
        this.hasItem = null;
        this.hasItemAmount = 0;
    }

    public class PutTile extends Action {
        Tile tile;
        Vector2 pos;

        public PutTile(Tile tile, Vector2 pos, Action nextAction) {
            this.tile = tile;
            this.pos = pos;
            this.nextAction = nextAction;
        }

        @Override
        public void step(Game game) {
            game.map.overworldTiles.put(this.pos.cpy(), this.tile);
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
        }
    }

    /**
     * 
     */
    public class Moving extends Action {
        int timer = 0;
        Tile target;
        Vector2 currPos;
        Vector2 offset = new Vector2();
        Sprite overSprite;

        public Moving(Tile target, Vector2 position, Action nextAction) {
            this.target = target;
            this.offset = position.cpy().sub(Tile.this.position);
            this.offset.x /= 16;
            this.offset.y /= 16;
            this.nextAction = nextAction;
        }

        @Override
        public void firstStep(Game game) {
            this.overSprite = Tile.this.overSprite;
            Tile.this.drawUpperBelowPlayer = false;
            Tile.this.overSprite = null;
            this.currPos = Tile.this.position.cpy();
        }

        @Override
        public void step(Game game) {
            this.currPos.add(this.offset);
            game.mapBatch.draw(this.overSprite, this.currPos.x, this.currPos.y);
            //
            this.timer++;
            if (this.timer >= 16) {
                this.target.nameUpper = Tile.this.nameUpper;
                this.target.init();
                Tile.this.nameUpper = "";
                Tile.this.init();
                game.actionStack.remove(this);
                game.insertAction(this.nextAction);
            }
        }
    }

    public void onPressA(Game game) {
        // signs store text in this.hasItem
        if (this.hasItem != null && !this.isSign()) {
            if (game.type == Game.Type.CLIENT) {
                game.client.sendTCP(new Network.PickupItem(game.player.network.id, game.player.dirFacing));
            }
            else {
                game.playerCanMove = false;
                String number = "a";
                String plural = "";
                if (this.hasItemAmount > 1) {
                    number = String.valueOf(this.hasItemAmount);
                    plural = "S";
                    if (this.hasItem.endsWith("s")) {
                        plural = "ES";
                    }
                }
                game.insertAction(new DisplayText(game, "Found "+number+" "+this.hasItem.toUpperCase()+plural+"!", "fanfare1.ogg", null,
                                  new WaitFrames(game, 6,
                                  new SetField(game, "playerCanMove", true,
                                  null))));
                this.pickUpItem(game.player);
            }
        }
        else if (game.map.pokemon.containsKey(this.position) &&
                 game.map.pokemon.get(this.position).mapTiles == game.map.tiles) {
            Pokemon pokemon = game.map.pokemon.get(this.position);
            if (game.type == Game.Type.CLIENT) {
                // Server will send a PausePokemon back if this succeeds.
                game.client.sendTCP(new Network.PausePokemon(game.player.network.id,
                                                             pokemon.position,
                                                             true));
                return;
            }
            Action nextAction = new Action() {
                public String getCamera() {
                    return "gui";
                }
            };
            game.playerCanMove = false;

            // Special case for the zen-darmanitan interaction
            // TODO: special name or something for zen mode?
            if (pokemon.specie.name.equals("darmanitanzen") && pokemon.previousOwner == null) {
//                game.battle.oppPokemon = pokemon;  // Have to do b/c of Battle.getIntroAction() init stuff

                nextAction.append(new DisplayText(game, "A statue of an ancient Pokemon.", null, null, null));
                if (game.player.itemsDict.containsKey("ragecandybar")) {
                    nextAction.append(new DisplayText(game, "Give it a RageCandyBar?", null, true, false,
                                      new DrawYesNoMenu(null,
                                          new DisplayText.Clear(game,
                                          new WaitFrames(game, 6,
                                          game.player.new RemoveFromInventory("ragecandybar", 1,
                                          new DisplayText(game, "The statue responded to the RageCandyBar...", null, null,
                                          new DisplayText(game, "The awakened DARMANITAN attacked!", null, null,
                                          new SetField(game.battle, "oppPokemon", pokemon,
                                          new CallMethod(game.player, "setCurrPokemon", new Object[]{},
                                          new SetField(game.musicController, "startBattle", "wild",
                                          new Battle.GetIntroAction(null))))))))),
//                                          new CallStaticMethod(Battle.class, "getIntroAction", new Object[]{game}, null))))))))),
//                                          Battle.getIntroAction(game))))))))),
                                      new DisplayText.Clear(game,
                                      new WaitFrames(game, 6,
                                      new SetField(game, "playerCanMove", true, 
                                      new SetField(pokemon, "canMove", true,
                                      null)))))));
                }
                else {
                    nextAction.append(new WaitFrames(game, 10,
                                        new SetField(game, "playerCanMove", true,
                                      null)));
                }
                game.insertAction(nextAction);
                return;
            }

//            game.map.pokemon.get(this.position)
            // TODO: play animation
            pokemon.canMove = false;
            String oppDir = "down";  // TODO: need a map for this.
            if (game.player.dirFacing.equals("up")) {
                oppDir = "down";
            }
            else if (game.player.dirFacing.equals("down")) {
                oppDir = "up";
            }
            else if (game.player.dirFacing.equals("right")) {
                oppDir = "left";
            }
            else if (game.player.dirFacing.equals("left")) {
                oppDir = "right";
            }
            boolean isBaseSpecies = true;
            if (Pokemon.baseSpecies.get(pokemon.specie.name) != null) {
                isBaseSpecies = Pokemon.baseSpecies.get(pokemon.specie.name).equals(pokemon.specie.name);
            }
            // If player is attacking
            if (game.player.currFieldMove.equals("ATTACK") ||
                (game.player.currFieldMove.equals("RIDE") && game.player.hmPokemon.types.contains("DARK"))) {
                if (isBaseSpecies) {
                    pokemon.happiness = 0;
                }
                // Enter battle with pokemon
                game.battle.oppPokemon = pokemon;
                game.player.setCurrPokemon();
                game.insertAction(new SetField(pokemon, "dirFacing", oppDir,
                                  new WaitFrames(game, 20,
                                  new SplitAction(pokemon.new Emote("!", null),
                                  new WaitFrames(game, 20,
                                  new PlaySound(pokemon,
                                  new SetField(game.musicController, "startBattle", "wild",
                                  Battle.getIntroAction(game))))))));
                return;
            }
            if (pokemon.previousOwner == game.player && pokemon.hasItem != null) {
                nextAction.append(new DisplayText(game, pokemon.nickname.toUpperCase()+" looks like it' holding something...", null, false, true,
                                  new DisplayText(game, pokemon.nickname.toUpperCase()+" gave you "+pokemon.hasItem.toUpperCase()+"!", "fanfare1.ogg", false, true,
                                  null)));
                if (!game.player.alreadyDoneHarvestables.contains(pokemon.hasItem)) {
                    if (pokemon.hasItem.contains("manure")) {
                        nextAction.append(new DisplayText(game, "It smells pretty bad... surely it will come in handy!", null, false, true, null));
                    }
                    else if (pokemon.hasItem.contains("wool")) {
                        nextAction.append(new DisplayText(game, "Mmm... feels soft and warm.", null, false, true, null));
                    }
                    else if (pokemon.hasItem.contains("hard shell")) {
                        nextAction.append(new DisplayText(game, "It' a piece of it' shell... feels nice and sturdy.", null, false, true, null));
                    }
                    game.player.alreadyDoneHarvestables.add(pokemon.hasItem);
                }
                nextAction.append(new SetField(pokemon, "harvestTimer", 0,  // Order matters
                                  new SetField(pokemon, "hasItem", null,
                                  new WaitFrames(game, 10,
                                  new SetField(game, "playerCanMove", true, 
                                  new SetField(pokemon, "canMove", true,
                                  null))))));
                int amount = 1;
                if (game.player.itemsDict.containsKey(pokemon.hasItem)) {
                    amount = game.player.itemsDict.get(pokemon.hasItem)+1;
                }
                game.player.itemsDict.put(pokemon.hasItem, amount);
                game.insertAction(nextAction);
                return;
            }
            // If this is a wild evolved form, then it will aggro player if
            // interacted with twice in a row.
            boolean shouldAggro = Pokemon.aggroAnyway.contains(pokemon.specie.name) || (!isBaseSpecies && !Pokemon.dontAggro.contains(pokemon.specie.name));
            if (pokemon.previousOwner != game.player && !pokemon.isEgg && shouldAggro) {
                if (!pokemon.interactedWith) {
                    nextAction = new SetField(pokemon, "dirFacing", oppDir,
                                 new WaitFrames(game, 20,
                                 new SplitAction(pokemon.new Emote("skull", null),
                                 new WaitFrames(game, 20,
                                 new PlaySound(pokemon,
                                 null)))));
                    nextAction.append(new DisplayText(game, pokemon.nickname.toUpperCase()+" seems aggressive... it may attack if provoked!", null, false, true, null));
                    pokemon.interactedWith = true;
                    nextAction.append(new WaitFrames(game, 10,
                                      new SetField(game, "playerCanMove", true,
                                      new SetField(pokemon, "canMove", true, null))));
                }
                else {
                    nextAction = new SetField(pokemon, "dirFacing", oppDir,
                                 new WaitFrames(game, 20,
                                 new SplitAction(pokemon.new Emote("!", null),
                                 new WaitFrames(game, 20,
                                 new PlaySound(pokemon,
                                 new WaitFrames(game, 10,
                                 new SetField(game, "playerCanMove", true,
                                 new SetField(pokemon, "canMove", true,
                                 new SetField(pokemon, "aggroPlayer", true,
                                 null)))))))));
                }
                game.insertAction(nextAction);
                return;
            }

            Action emote;
            // If it's an egg, display the '...' emote
            if (pokemon.isEgg) {
                emote = pokemon.new Emote("...", null);
            }
            else if (pokemon.previousOwner != game.player) {
                emote = pokemon.new Emote("happy", null);
            }
            else if (!pokemon.inHabitat) {
                emote = pokemon.new Emote("uncomfortable", null);
            }
            else if (pokemon.loveInterest != null) {
                emote = pokemon.new Emote("heart", null);
            }
            else {
                emote = pokemon.new Emote("happy", null);
            }
            nextAction = new SetField(pokemon, "dirFacing", oppDir,
                         new WaitFrames(game, 20,
                         new SplitAction(emote,
                         new WaitFrames(game, 20,
                         new PlaySound(pokemon,
                         null)))));

            if (pokemon.isEgg) {
                String[] huhs = new String[]{"Neat!", "Hey!", "Look!", "Wow!", "Huh?", "Hmm..."};
                nextAction.append(//new WaitFrames(game, 60,  // use if no cry for egg
                                  new DisplayText(game, huhs[Game.rand.nextInt(huhs.length)]+" A POK?MON egg!", null, false, true,
                                  null));
            }
            else if (pokemon.previousOwner != game.player) {
                nextAction.append(new DisplayText(game, pokemon.nickname.toUpperCase()+" seems friendly. ", null, false, true, null));
            } 
            // if pokemon is indoors, it will say it's happy but no items
            else if (pokemon.mapTiles != game.map.overworldTiles) {
                nextAction.append(new DisplayText(game, pokemon.nickname.toUpperCase()+" is enjoying itself.", null, false, true, null));
            }
            else if (!pokemon.inHabitat) {
                nextAction.append(new DisplayText(game, pokemon.nickname.toUpperCase()+" seems uncomfortable in this environment. ", null, false, true, null));
            }
            else if (pokemon.loveInterest != null) {
                nextAction.append(new DisplayText(game, pokemon.nickname.toUpperCase()+" seems interested in "+pokemon.loveInterest.nickname.toUpperCase()+".", null, false, true, null));
            }
            else {
                nextAction.append(new DisplayText(game, pokemon.nickname.toUpperCase()+" seems happy. ", null, false, true, null));
            }
//                              new DisplayText(game, "Put "+pokemon.name.toUpperCase()+" in it' POK?BALL?", null, true, false,
            String text = pokemon.isEgg ? "Pick it up?" : "Add "+pokemon.nickname.toUpperCase()+" to your party?";
            nextAction.append(new DisplayText(game, text, null, true, false,
                              new DrawYesNoMenu(null,
                                  new DisplayText.Clear(game,
                                  new WaitFrames(game, 6,
                                  pokemon.new AddToInventory(
                                  new SetField(game, "playerCanMove", true,
                                  new SetField(pokemon, "canMove", true,
                                  null))))),
                              new DisplayText.Clear(game,
                              new WaitFrames(game, 6,
//                              new WaitFrames(game, 10,  // TODO: test to avoid textbox mayhem
                              new SetField(game, "playerCanMove", true, 
                              new SetField(pokemon, "canMove", true,
                              null)))))));
            game.insertAction(nextAction);
        }

        // Spiritomb stuff
        else if (this.nameUpper.equals("odd_keystone2")) {
            game.playerCanMove = false;
            int amountLeft = 107-this.hasItemAmount;
            String text = "A voice whispers from within... "+String.valueOf(amountLeft)+"... bring "+String.valueOf(amountLeft)+"...";
            if (amountLeft == 69) {
                text = "A voice whispers from within... bring 69... Nice...";
            }
            Action nextAction = new DisplayText(game, text, null, null, null);
            //
            if (game.player.itemsDict.containsKey("life force")) {
                //
                // Variable input version
                Craft craft = new Craft("life force", 1);
                craft.requirements.add(new Craft("life force", 1));
                // TODO: there needs to be a better way to do this.
                class ChoiceAction extends Action {
                    Player.Craft craft;
                    public ChoiceAction(Player.Craft craft, Action nextAction) {
                        this.craft = craft;
                        this.nextAction = nextAction;
                    };
                    @Override
                    public void step(Game game) {
                        Tile.this.hasItemAmount += this.craft.amount;
                        // Prevent player from using too much
                        if (Tile.this.hasItemAmount > 107) {
                            this.craft.amount -= Tile.this.hasItemAmount -107;
                        }
                        //
                        if (Tile.this.hasItemAmount >= 107) {
                            this.nextAction = new WaitFrames(game, 20,
                                              new SetField(Tile.this, "nameUpper", "spiritomb",
                                              new CallMethod(Tile.this, "init", new Object[]{},
                                              new PokemonFrame(new Pokemon("spiritomb", 2, Pokemon.Generation.CRYSTAL, false, false),
                                              this.nextAction))));
                        }
                        if (this.craft.amount == 69) {
                            this.nextAction = new DisplayText(game, "69... Nice...", null, null,
                                              this.nextAction);
                        }
                        game.actionStack.remove(this);
                        game.insertAction(game.player.new RemoveFromInventory(this.craft.name, this.craft.amount,
                                          new DisplayText(game, "The spirits entered into the keystone...", "attacks/hypnosis_player_gsc/sound.ogg", null,
                                          this.nextAction)));
                    }
                };
                nextAction.append(new DisplayText(game, "Use life force?", null, true, false,
                                  new SelectAmount(craft,
                                      new DisplayText.Clear(game,
                                      new WaitFrames(game, 10,
                                      new SetField(game, "playerCanMove", true,
                                      null))),
                                  new DisplayText.Clear(game,
                                  new WaitFrames(game, 6,
                                  new ChoiceAction(craft,
                                  new WaitFrames(game, 10,
                                  new SetField(game, "playerCanMove", true,
                                  null))))))));

              // This version didn't have variable input
//            if (game.player.itemsDict.containsKey("life force") && game.player.itemsDict.get("life force") >= 107) {
//                nextAction.append(new DisplayText(game, "Use life force?", null, true, false,
//                                  new DrawYesNoMenu(null,
//                                      new DisplayText.Clear(game,
//                                      new WaitFrames(game, 6,
//                                      game.player.new RemoveFromInventory("life force", 107,
//                                      new DisplayText(game, "The spirits entered into the keystone...", "attacks/hypnosis_player_gsc/sound.ogg", null,
//                                      new WaitFrames(game, 20,
//                                      // Immediate encounter version
////                                      new SetField(game.battle, "oppPokemon", new Pokemon("spiritomb", 25, Pokemon.Generation.CRYSTAL),
////                                      new CallMethod(game.player, "setCurrPokemon", new Object[]{},
////                                      new SetField(game.musicController, "startBattle", "wild",
////                                      new Battle.GetIntroAction(null)))))))),
//                                      new SetField(this, "nameUpper", "spiritomb",
//                                      new CallMethod(this, "init", new Object[]{},
//                                      new PokemonFrame(new Pokemon("spiritomb", 2, Pokemon.Generation.CRYSTAL, false, false),
//                                      new WaitFrames(game, 6,
//                                      new SetField(game, "playerCanMove", true,
//                                      null)))))))))),
//                                      //
//                                  new DisplayText.Clear(game,
//                                  new WaitFrames(game, 10,
//                                  new SetField(game, "playerCanMove", true, 
//                                  null))))));
            }
            else {
                nextAction.append(new WaitFrames(game, 10,
                                  new SetField(game, "playerCanMove", true,
                                  null)));
            }
            game.insertAction(nextAction);
        }
        //
        else if (this.isSign() && game.player.dirFacing.equals("up")) {

            game.playerCanMove = false;
            if (this.hasItem == null) {

                /**
                 * TODO: move somewhere
                 */
                class SetSignText extends Action {
                    Texture bgTexture;
                    Color prevColor;
                    int avatarAnimCounter = 0;
                    HashMap<Integer, Character> alphanumericKeys = new HashMap<Integer, Character>();
                    HashMap<Integer, Character> alphanumericKeysShift = new HashMap<Integer, Character>();
                    ArrayList<Character> text = new ArrayList<Character>();
                    int backspaceTimer = 0;
                    public boolean done = false;
                    public boolean disabled = false;
                    public SetSignText(Action nextAction) {
                        this.bgTexture = TextureCache.get(Gdx.files.internal("battle/battle_bg3.png"));

                        char[] textArray = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
                        for (int i=0; i < textArray.length; i++) {
                            this.alphanumericKeys.put(Input.Keys.valueOf(String.valueOf(textArray[i])),
                                                      String.valueOf(textArray[i]).toLowerCase().charAt(0));
                            this.alphanumericKeysShift.put(Input.Keys.valueOf(String.valueOf(textArray[i])),textArray[i]);
                        }
                        textArray = "1234567890".toCharArray();
                        for (int i=0; i < textArray.length; i++) {
                            this.alphanumericKeys.put(Input.Keys.valueOf(String.valueOf(textArray[i])), textArray[i]);
                            this.alphanumericKeysShift.put(Input.Keys.valueOf(String.valueOf(textArray[i])), textArray[i]);
                        }
                        this.alphanumericKeys.put(Input.Keys.PERIOD, '.');
                        this.alphanumericKeysShift.put(Input.Keys.SLASH, '?');
                        this.alphanumericKeysShift.put(Input.Keys.NUM_1, '!');
                        
                        this.nextAction = nextAction;
                    };
                    public String getCamera() {
                        return "gui";
                    }
                    public int getLayer() {
                        return 107;
                    }
                    @Override
                    public void firstStep(Game game) {
                        game.insertAction(new DisplayText(game, "Press Enter to set text", null, true, false, null));
                    }
                    @Override
                    public void step(Game game) {
                        this.prevColor = game.mapBatch.getColor();
                        game.uiBatch.setColor(1f, 1f, 1f, 1f);
//                        for (i = -1; i < 1; i+= 1) {
//                            for (j = -1; j < 1; j+= 1) {
//                                game.uiBatch.draw(this.bgTexture, 256*i, 256*j);
//                            }
//                        }
                        game.uiBatch.draw(this.bgTexture, -8, -8);
                        game.uiBatch.setColor(this.prevColor);
                        
                        Sprite letterSprite;
                        // Draw text
                        int i = 0;
                        int j = 0;
                        for (int k=0; k < this.text.size(); k++) {
                            char character = this.text.get(k);
                            if (character == ' ' && k+1 < this.text.size()) {
                                int length = 1;
                                char nextChar = this.text.get(k+length);
                                while (nextChar != ' ' && k+length < this.text.size()) {
                                    nextChar = this.text.get(k+length++);
                                }
                                if (i+length > 20 && j < 5) {
                                    i = -1;
                                    j++;
                                }
                            }
                            letterSprite = game.textDict.get(character);
                            letterSprite.setPosition(8*i, 128 -16*j);
                            letterSprite.draw(game.uiBatch, 1f);
                            i++;
                            if (i > 20) {
                                i = 0;
                                j++;
                            }
                        }
                        // Draw cursor
                        if (this.avatarAnimCounter >= 12) {
                            letterSprite = game.textDict.get('_');
                            game.uiBatch.draw(letterSprite, 8*i, 128 -16*j);
                        }

                        if (this.done) {
                            game.actionStack.remove(this);
                        }
                        if (this.disabled) {
                            return;
                        }

                        // Accept input
                        if (Gdx.input.isKeyPressed(Input.Keys.BACKSPACE)) {
                            if (this.backspaceTimer < 30) {
                                this.backspaceTimer++;
                            }
                        }
                        else {
                            this.backspaceTimer = 0;
                        }
                        if ((this.backspaceTimer >= 30 || Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) && this.text.size() > 0) {
                            this.text.remove(this.text.size()-1);
                        }
                        //
                        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                            this.disabled = true;
                            String allText = "";
                            for (char c : this.text) {
                                allText += c;
                            }
                            game.insertAction(new DisplayText.Clear(game,
                                              new WaitFrames(game, 3,
                                              new DisplayText(game, "Keep changes?", null, true, false,
                                              new DrawYesNoMenu(null,
                                                  new DisplayText.Clear(game,
                                                  new WaitFrames(game, 3,
                                                  new SetField(Tile.this, "hasItem", allText,
                                                  new WaitFrames(game, 10,
                                                  new SetField(this, "done", true,
                                                  new SetField(game, "playerCanMove", true,
                                                  null)))))),
                                              new DisplayText.Clear(game,
                                              new WaitFrames(game, 10,
                                              new SetField(this, "done", true,
                                              new SetField(game, "playerCanMove", true,
                                              null)))))))));
                        }

                        if (i >= 20 && j >= 5) {
                            return;
                        }
                        
                        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                            this.text.add(' ');
                        }
                        //
                        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                            for (Integer key : this.alphanumericKeysShift.keySet()) {
                                if (Gdx.input.isKeyJustPressed(key)) {
                                    this.text.add(this.alphanumericKeysShift.get(key));
                                }
                            }
                        }
                        else {
                            for (Integer key : this.alphanumericKeys.keySet()) {
                                if (Gdx.input.isKeyJustPressed(key)) {
                                    this.text.add(this.alphanumericKeys.get(key));
                                }
                            }
                        }


                        this.avatarAnimCounter--;
                        if (this.avatarAnimCounter <= 0) {
                            this.avatarAnimCounter = 24;
                        }
                    }
                    
                }

                Action nextAction = new SetSignText(
                                    new WaitFrames(game, 10,
                                      new SetField(game, "playerCanMove", true,
                                      null)));
                game.insertAction(nextAction);
                
            }
            else {
                Action nextAction = new DisplayText(game, this.hasItem, null, null,
                                    new WaitFrames(game, 10,
                                      new SetField(game, "playerCanMove", true,
                                      null)));
                game.insertAction(nextAction);
            }
        }
                
        else if (this.nameUpper.equals("gravestone2") && game.player.dirFacing.equals("up")) {
            game.playerCanMove = false;
            Action nextAction = new DisplayText(game, "The text is too faded to make out...", null, null,
                                new WaitFrames(game, 10,
                                  new SetField(game, "playerCanMove", true,
                                  null)));
            Tile down = game.map.tiles.get(this.position.cpy().add(0, -32));
            Tile right = game.map.tiles.get(this.position.cpy().add(16, -16));
            if (Game.rand.nextInt(128) == 0 &&
                ((down != null && !down.attrs.get("solid")) || (right != null && !right.attrs.get("solid")))) {
//            if (true) {  // debug
                class DrawMysteryNPC extends Action {
                    Texture texture;
                    Vector2 position;
                    public boolean stop = false;
                    public DrawMysteryNPC(Action nextAction) {
                        this.texture = TextureCache.get(Gdx.files.internal("npc1.png"));
                        this.nextAction = nextAction;
                    };
                    public String getCamera() {
                        return "map";
                    }
                    public int getLayer() {
                        return 114;
                    }
                    @Override
                    public void firstStep(Game game) {
                        game.insertAction(this.nextAction);
                        Vector3 worldCoords = game.cam.unproject(new Vector3(0f, 0f, 0f));
                        // -16 b/c will draw starting at top of screen
                        this.position = new Vector2(worldCoords.x, worldCoords.y -16);
                    }
                    @Override
                    public void step(Game game) {
                        game.mapBatch.draw(this.texture, this.position.x, this.position.y+1);
                        if (this.stop) {
                            game.actionStack.remove(this);
                        }
                    }
                };
                game.playerCanMove = true;
                game.player.acceptInput = false;
                DrawMysteryNPC drawMysteryNPC = new DrawMysteryNPC(null);
                drawMysteryNPC.append(new DisplayText(game, "The text is too faded to make out...", null, null,
                                      null));
                if (down != null && !down.attrs.get("solid")) {
                    drawMysteryNPC.append(new SetField(game.player, "dirFacing", "down",
                                          new SetField(game.player, "currSprite", game.player.standingSprites.get("down"),
                                          null)));
                }
                else {
                    drawMysteryNPC.append(new SetField(game.player, "dirFacing", "right",
                    new SetField(game.player, "currSprite", game.player.standingSprites.get("right"),
                    null)));
                }
                drawMysteryNPC.append(new PlayerMoving(game, game.player, false,
                                      new WaitFrames(game, 10,
                                      new SetField(game.player, "acceptInput", true,
                                      new SetField(drawMysteryNPC, "stop", true,
                                      null)))));
                nextAction = drawMysteryNPC;
            }
            game.insertAction(nextAction);
        }
        else if (this.name.equals("cave1_regi5")) {
            // Just copy what the tile next to it will do.
            Tile leftTile = game.map.tiles.get(this.position.cpy().add(-16, 0));
            if (leftTile != null) {
                leftTile.onPressA(game);
            }
        }
        // Regigigas while he is standing
        // If all regis are caught, then enter battle.
        else if (this.name.equals("cave1_regi2")) {
            game.playerCanMove = false;
            Action nextAction;
            nextAction = new DisplayText(game, "...", null, false, true,  //was - It stands silently in place
                         new WaitFrames(game, 6,
                         new SetField(game, "playerCanMove", true,
                         null)));
            game.insertAction(nextAction);
        }
        else if (this.name.equals("cave1_regi3")) {
            // Dancing sprite
//            Texture texture = TextureCache.get(Gdx.files.internal("tiles/cave1/regi3.png"));
//            this.sprite = new Sprite(texture, 0, 0, 48, 48);
//            this.sprite.setPosition(this.position.x-8, this.position.y-9);
            MoveWater.regiTimer2 = 0;
            // Battle intro animation and text
            game.playerCanMove = false;
            game.battle.oppPokemon = new Pokemon("regigigas", 50);
            // This is used to remove him from the map if caught.
            game.battle.oppPokemon.position = this.position.cpy();
            game.player.setCurrPokemon();
            game.insertAction(//new WaitFrames(game, 40,
                              new DisplayText(game, "...Zut zutt!", "pokemon/cries/486.ogg", null,
                              new SetField(game.musicController, "startBattle", "regi_battle1",
                              new SplitAction(
                                  new CallMethod(game.uiBatch, "setColor", new Object[]{new Color(1f, 1f, 1f, 1.0f)}, null),
                              new CallMethod(game.mapBatch, "setColor", new Object[]{new Color(0.5f, 0.5f, 0.5f, 1.0f)},
                              new SplitAction(
                                  new RegigigasIntroAnim.BattleIntro(null),
    //                                              new WaitFrames(game, 240,
                              new WaitFrames(game, 230,  // lines up the intro frame better with the music.
//                              new SplitAction(
//                                  new WaitFrames(game, 120,
//                                  new SplitAction(
//                                      new RegigigasIntroAnim.RocksEffect1(),
//                                      new RegigigasIntroAnim.RocksEffect2())),
                              Battle.getIntroAction(game))))))));
        }
        // Regi legendary pokemon is on this tile
        else if (this.nameUpper.contains("REGI")) {
            Pokemon regi = new Pokemon(this.nameUpper.toLowerCase(), 40);
            // TODO: need text for their cry
            game.playerCanMove = false;
            game.battle.oppPokemon = regi;
            game.player.setCurrPokemon();
            String cryText = "";
            if (this.nameUpper.equals("REGIELEKI")) {
                cryText = "Zizi zizizi.";
            }
            else if (this.nameUpper.equals("REGIDRAGO")) {
                cryText = "Zagd.";
            }
            else if (this.nameUpper.equals("REGISTEEL")) {
                cryText = "Ji-ji-ze-ji-zoh.";
            }
            else if (this.nameUpper.equals("REGIROCK")) {
                cryText = "Zaza zari za...";
            }
            else if (this.nameUpper.equals("REGICE")) {
                cryText = "Jakiih!";
            }

//            game.uiBatch.setColor(new Color(1f, 1f, 1f, 1.0f));
//            game.mapBatch.setColor(new Color(0.6f, 0.6f, 0.6f, 1.0f));
            
            game.insertAction(new DisplayText(game, cryText, "pokemon/cries/" + regi.dexNumber + ".ogg", null,
                              new SetField(game.musicController, "startBattle", "regi_battle1",
                              new SplitAction(
                                  new CallMethod(game.uiBatch, "setColor", new Object[]{new Color(1f, 1f, 1f, 1.0f)}, null),
                              new CallMethod(game.mapBatch, "setColor", new Object[]{new Color(0.5f, 0.5f, 0.5f, 1.0f)},
                              new SplitAction(
                                  new RegigigasIntroAnim.BattleIntro(null),
//                              new WaitFrames(game, 240,
                              new WaitFrames(game, 230,  // lines up the intro frame better with the music.
                              Battle.getIntroAction(game))))))));
        }
        // This has to be after regi legendary encounter check b/c on same tile.
        else if (this.name.equals("cave1_regipedistal1")) {
            game.playerCanMove = false;
            game.player.isCrafting = true;
            // Fill regicrafts with whatever is available
            game.player.regiCrafts.clear();
            if (this.items().containsKey("REGISTEEL")) {
                Craft craft = new Craft("REGISTEEL", 1);
                craft.requirements.add(new Craft("metal coat", 70));
                craft.requirements.add(new Craft("spell tag", 1));
                game.player.regiCrafts.add(craft);
            }
            if (this.items().containsKey("REGIROCK")) {
                Craft craft = new Craft("REGIROCK", 1);
                craft.requirements.add(new Craft("hard stone", 70));
                craft.requirements.add(new Craft("spell tag", 1));
                game.player.regiCrafts.add(craft);
            }
            if (this.items().containsKey("REGICE")) {
                Craft craft = new Craft("REGICE", 1);
                craft.requirements.add(new Craft("nevermeltice", 70));
                craft.requirements.add(new Craft("spell tag", 1));
                game.player.regiCrafts.add(craft);
            }
            if (this.items().containsKey("REGIDRAGO")) {
                Craft craft = new Craft("REGIDRAGO", 1);
                craft.requirements.add(new Craft("dragon scale", 70));
                craft.requirements.add(new Craft("dragon fang", 2));
                craft.requirements.add(new Craft("spell tag", 1));
                game.player.regiCrafts.add(craft);
            }
            if (this.items().containsKey("REGIELEKI")) {
                Craft craft = new Craft("REGIELEKI", 1);
                craft.requirements.add(new Craft("magnet", 70));
                craft.requirements.add(new Craft("binding band", 2));
                craft.requirements.add(new Craft("spell tag", 1));
                game.player.regiCrafts.add(craft);
            }
            game.insertAction(new DrawCraftsMenu.Intro(null, 9,
                              new DrawCraftsMenu(game, game.player.regiCrafts,
                              new SetField(game, "playerCanMove", true,
                              new SetField(game.player, "isCrafting", false,
                              null)))));
        }
        else if (this.name.equals("building1_pokecenter1_right")) {
            // Just copy what the tile next to it will do.
            Tile leftTile = game.map.tiles.get(this.position.cpy().add(-16, 0));
            if (leftTile != null) {
                leftTile.onPressA(game);
            }
        }
        // This has to be after regi legendary encounter check b/c on same tile.
        else if (this.name.equals("building1_fossilreviver1") && game.player.dirFacing.equals("up")) {
            game.playerCanMove = false;
            boolean foundRevivedMon = false;
            for (Tile tile : game.map.tiles.values()) {
                if (tile.nameUpper.contains("revived")) {
                    foundRevivedMon = true;
                    break;
                }
            }
            // Don't allow use while a mon is on the revive machine thing
            if (foundRevivedMon) {
                Action nextAction = new DisplayText(game, "It' not responding...", null, false, true,
                                                    new WaitFrames(game, 6, 
                                                    new SetField(game, "playerCanMove", true,
                                                    null)));
                               game.insertAction(nextAction);
                               return;
            }
            // Requires POWER field move to use
            if (game.player.hmPokemon == null || !game.player.currFieldMove.equals("POWER")) {
                Action nextAction = new DisplayText(game, "No power... an ELECTRIC type could get it running.", null, false, true,
                                     new WaitFrames(game, 6, 
                                     new SetField(game, "playerCanMove", true,
                                     null)));
                game.insertAction(nextAction);
                return;
            }
            game.player.isCrafting = true;
            Action nextAction = new FossilMachinePowerUp(false,
                                new PlaySound("pc_on1",
                                new DisplayText(game, game.player.hmPokemon.nickname.toUpperCase()+" powered up the machine!", null, false, true,
                                new DrawCraftsMenu.Intro(null, 9,
                                new DrawCraftsMenu(game, game.player.fossilCrafts,
                                new FossilMachinePowerUp(true,
                                new PlaySound("pc_off1",
                                new SetField(game, "playerCanMove", true,
                                new SetField(game.player, "isCrafting", false,
                                null)))))))));
            game.insertAction(nextAction);
        }
        else if (this.nameUpper.contains("revived_")) { 
            game.playerCanMove = false;
            // Scale level of wild mon
            int averageLevel = 0;
            int numberPokemon = 0;
            for (Pokemon mon : game.player.pokemon) {
                // Eggs don't count towards level scaling
                if (mon.isEgg) {
                    continue;
                }
                averageLevel += mon.level;
                numberPokemon++;
            }
            // This shouldn't happen, but just in case.
            if (numberPokemon <= 0) {
                System.out.println("WARNING: this should never happen. Might want to look into it.");
                numberPokemon = 1;
            }
            averageLevel = averageLevel/numberPokemon;
            averageLevel = averageLevel -3 + Game.rand.nextInt(3);
            if (averageLevel > 50) {
                averageLevel = 50;
            }

            String name = this.nameUpper.split("_")[1].toLowerCase();
            Pokemon pokemon = new Pokemon(name, averageLevel, Pokemon.Generation.CRYSTAL);
            //
            if (game.player.currFieldMove.equals("CHARM") && game.player.hmPokemon != null) {
                if (game.player.pokemon.size() >= 6) {
                    game.insertAction(new DisplayText.Clear(game,
                                      new WaitFrames(game, 3,
                                      new PlaySound("error1",
                                      new DisplayText(game, "Not enough room in your party!", null, null,
                                      new WaitFrames(game, 6,
                                      new SetField(game, "playerCanMove", true, null)))))));
                    return;
                }
                Action newAction = //new DisplayText(game, pokemon.nickname.toUpperCase()+" is charmed by "+game.player.hmPokemon.nickname.toUpperCase()+"!", null, null,
                                   new DisplayText(game, game.player.name+" received "+pokemon.nickname.toUpperCase()+"!", "fanfare1.ogg", null,
                                   new SetField(game, "playerCanMove", true, null));
                this.nameUpper = "";
                game.player.pokemon.add(pokemon);
                pokemon.previousOwner = game.player;
                game.insertAction(newAction);
                return;
            }

            game.playerCanMove = false;
            game.battle.oppPokemon = pokemon;
            pokemon.onTile = this;
            game.player.setCurrPokemon();
//            pokemon.position = this.position.cpy().add(8, 8);  // TODO: not working
//            game.insertAction(pokemon.new Emote("!", null));  // TODO: not working
            game.insertAction(new DisplayText(game, pokemon.nickname.toUpperCase()+" attacked!", "pokemon/cries/" + pokemon.dexNumber + ".ogg", null,
                              //new PlaySound(pokemon,
                              new WaitFrames(game, 10,
                              new SetField(game.musicController, "startBattle", "wild",
                              Battle.getIntroAction(game)))));
        }
        // Tile for Volcarona battle
        // TODO: make this generic-ish
        // Some things are specific, like battle theme, 'cry' text, etc.
        else if (this.nameUpper.equals("volcarona")) {
            String name = this.nameUpper;
            // TODO: it's level 70 in black/white, might change at some point
            // Larvesta evolves at level 59, so 60 for now.
            Pokemon pokemon = new Pokemon(name, 60, Pokemon.Generation.CRYSTAL);
            game.playerCanMove = false;
            game.battle.oppPokemon = pokemon;
            pokemon.onTile = this;
            game.player.setCurrPokemon();
            game.insertAction(new DisplayText(game, "Vraahhbrbrbr!", "pokemon/cries/" + pokemon.dexNumber + ".ogg", null,
                              new WaitFrames(game, 10,
                              new SetField(game.musicController, "startBattle", "bw_legendary_theme3",
                              new WaitFrames(game, 80,
                              Battle.getIntroAction(game))))));
        }
        else if (this.nameUpper.equals("spiritomb")) {
            String name = this.nameUpper;
            Pokemon pokemon = new Pokemon(name, 25, Pokemon.Generation.CRYSTAL);
            game.playerCanMove = false;
            game.battle.oppPokemon = pokemon;
            pokemon.onTile = this;
            game.player.setCurrPokemon();
            game.insertAction(new DisplayText(game, "Yulaaah!", "pokemon/cries/" + pokemon.dexNumber + ".ogg", null,
                              new WaitFrames(game, 10,
                              new SetField(game.musicController, "startBattle", "wild",
                              Battle.getIntroAction(game)))));
        }
        
        else if (this.nameUpper.equals("pokedoll1")) {
            // TODO: this could be handled via hasItem, maybe.
            String name = "pok? doll";
            game.playerCanMove = false;
            // SplitAction part is just to simulate delay present in console games
            game.insertAction(new SplitAction(
                                  new WaitFrames(game, 15,
                                  new SetField(this, "nameUpper", "",
                                  new SetField(this, "overSprite", null,
                                  new CallMethod(this, "init", new Object[] {},
                                  null)))),
                              new DisplayText(game, "Found a "+name.toUpperCase()+"!", "fanfare1.ogg", null,
                              new WaitFrames(game, 6,
                              new SetField(game, "playerCanMove", true,
                              null)))));
            int amount = 1;
            if (game.player.itemsDict.containsKey(name)) {
                amount += game.player.itemsDict.get(name);
            }
            game.player.itemsDict.put(name, amount);
            // TODO: remove
//            this.nameUpper = "";
//            this.overSprite = null;
//            this.init();
        }
        else if (this.nameUpper.equals("pokedoll1_banette") ||
                 this.nameUpper.equals("pokedoll1_mimikyu")) {
            String name = "banette";
            if (this.nameUpper.equals("pokedoll1_mimikyu")) {
                name = "mimikyu";
            }
            // ! Pop up, then text
            game.playerCanMove = false;
            game.battle.oppPokemon = new Pokemon(name, 24, Pokemon.Generation.CRYSTAL);
            game.battle.oppPokemon.mapTiles = game.map.overworldTiles;
            game.battle.oppPokemon.position = this.position.cpy();
            game.battle.oppPokemon.canMove = false;
            game.battle.oppPokemon.aggroPlayer = true;
            game.player.setCurrPokemon();
            // TODO/idea: play the ding noise
            game.insertAction(new SplitAction(
                                  new PlaySound("ledge2", null),
                              game.player.new Emote("!",
                              new DisplayText(game, "The doll became animated... and attacked!", null, null,
                              new SplitAction(
                                  new WaitFrames(game, 400,
                                  new SetField(this, "nameUpper", "",
                                  new SetField(this, "overSprite", null,
                                  new CallMethod(this, "init", new Object[] {}, 
                                  game.battle.oppPokemon.new Standing())))),
                              new WaitFrames(game, 10,
                              new SetField(game.musicController, "startBattle", "wild",
                              Battle.getIntroAction(game))))))));
        }

        // Pokemon mansion (dungeon) door
        else if (this.nameUpper.contains("house_wardrobe")) {
            game.playerCanMove = false;
            if (!game.player.currFieldMove.equals("")) {
                game.insertAction(new PlaySound("error1",
                                  new DisplayText(game, "Can? use this while using a Field Move.", null, null,
                                  new WaitFrames(game, 6,
                                  new SetField(game, "playerCanMove", true, null)))));
                return;
            }
            Action nextAction = new DisplayText(game, "Arrow left or right to change clothes color.", null, true, true,
                                new ChangePlayerColor(
                                new DisplayText.Clear(game,
                                new WaitFrames(game, 6,
                                new SetField(game, "playerCanMove", true, null)))));
            game.insertAction(nextAction);
        }

        // Vanity furniture
        else if (this.nameUpper.contains("house_vanity")) {
            game.playerCanMove = false;
            if (!game.player.currFieldMove.equals("")) {
                game.insertAction(new PlaySound("error1",
                                  new DisplayText(game, "Can? use this while using a Field Move.", null, null,
                                  new WaitFrames(game, 6,
                                  new SetField(game, "playerCanMove", true, null)))));
                return;
            }
            Action nextAction = new DisplayText(game, "Arrow left or right to change appearance.", null, true, true,
                                new ChangePlayerCharacter(
                                new DisplayText.Clear(game,
                                new WaitFrames(game, 6,
                                new SetField(game, "playerCanMove", true, null)))));
            game.insertAction(nextAction);
        }

        // Pokemon mansion (dungeon) door
        else if (this.name.contains("pkmnmansion_ext_locked")) {
            game.playerCanMove = false;
            Action nextAction;
            if (!game.player.itemsDict.containsKey("secret key")) {
                nextAction = new DisplayText(game, "It' locked...", null, false, true,
                             new WaitFrames(game, 3, 
                             new SetField(game, "playerCanMove", true,
                             null)));
            }
            else {
                nextAction = new DisplayText(game, "It' locked...", null, false, true,
                             new DisplayText(game, "Open using the SECRET KEY?", null, true, false,
                             new DrawYesNoMenu(null,
                                 new DisplayText.Clear(game,
                                 new WaitFrames(game, 3,
                                 game.player.new RemoveFromInventory("secret key", 1,
                                 new Tile.PutTile(new Tile("pkmnmansion_ext_door", this.position.cpy(), true, this.routeBelongsTo), this.position.cpy(),
                                 new DisplayText(game, "The door opened!", "fanfare1.ogg", null,
                                 new SetField(game, "playerCanMove", true,
                                 null)))))),
                             new DisplayText.Clear(game,
                             new WaitFrames(game, 3,
                             new SetField(game, "playerCanMove", true,
                             null))))));
            }
            game.insertAction(nextAction);
        }
        // Secret switch text
        else if (this.name.equals("pkmnmansion_statue1") && this.doorTiles != null && game.player.dirFacing.equals("up")) {
            game.playerCanMove = false;
            ArrayList<Tile> flipDoors = new ArrayList<Tile>();
            for (Vector2 pos : this.doorTiles) {
//                game.map.interiorTiles.get(game.map.interiorTilesIndex).get(pos).flipDoorTile();
                flipDoors.add(game.map.interiorTiles.get(game.map.interiorTilesIndex-1).get(pos));
            }
            Action nextAction = new DisplayText(game, "A hidden switch! Press it?", null, true, false,
                                new DrawYesNoMenu(null,
                                    new DisplayText.Clear(game,
                                    new WaitFrames(game, 20,
                                    new PlaySound("enter1",
                                    new Tile.FlipDoorTile(flipDoors,
                                    new SetField(game, "playerCanMove", true,
                                    null))))),
                                new DisplayText.Clear(game,
                                new WaitFrames(game, 3,
                                new SetField(game, "playerCanMove", true,
                                null)))));
            game.insertAction(nextAction);
        }
        else if (this.nameUpper.contains("bed")) {
            game.playerCanMove = false;
            Action nextAction = new DisplayText(game, "Do you want to sleep?", null, true, false,
                                                new DrawYesNoMenu(null,
                                                    new DisplayText.Clear(game,
                                                    new WaitFrames(game, 20,
                                                    new SetField(game.player, "sleepingDir", game.player.facingPos(),
                                                    new SetField(game.player, "isSleeping", true,
                                                    new SetField(game, "playerCanMove", true,
                                                    null))))),
                                                new DisplayText.Clear(game,
                                                new WaitFrames(game, 3,
                                                new SetField(game, "playerCanMove", true,
                                                null)))));
            game.insertAction(nextAction);
        }
        else if (this.nameUpper.contains("onpress_above")) {
            Tile down = game.map.tiles.get(this.position.cpy().add(0, -16));
            String name = this.name;
            String nameUpper = this.nameUpper;
            this.name = down.name;
            this.nameUpper = down.nameUpper;
            this.onPressA(game);
            this.name = name;
            this.nameUpper = nameUpper;
        }
        else if (this.nameUpper.contains("onpress_left")) {
            Tile left = game.map.tiles.get(this.position.cpy().add(-16, 0));
            String name = this.name;
            String nameUpper = this.nameUpper;
            this.name = left.name;
            this.nameUpper = left.nameUpper;
            this.onPressA(game);
            this.name = name;
            this.nameUpper = nameUpper;
        }
        else if (this.nameUpper.contains("house_couch")) {
            game.playerCanMove = false;
            game.insertAction(new WaitFrames(game, 30,
                              game.player.new Sitting(this)));
        }
        else if (this.nameUpper.equals("house_window1")) {
            game.insertAction(new PlaySound("seed1", null));
            if (this.overSprite.getRegionX() == 0) {
                this.overSprite.setRegion(16, 0, 16, 16);
            }
            else if (this.overSprite.getRegionX() == 16) {
                this.overSprite.setRegion(32, 0, 16, 16);
            }
            else {
                this.overSprite.setRegion(0, 0, 16, 16);
            }
        }
        else if (this.nameUpper.equals("house_window2")) {
            game.insertAction(new PlaySound("seed1", null));
            if (this.overSprite.getRegionX() == 0) {
                this.overSprite.setRegion(16, 0, 16, 16);
            }
            else {
                this.overSprite.setRegion(0, 0, 16, 16);
            }
        }
        else if (this.nameUpper.contains("_plush")) {
            String name = "";
            String[] names = this.nameUpper.split("_plush");
            if (names.length > 1) {
                name = names[1];
            }
            game.playerCanMove = false;
            game.insertAction(new DisplayText(game, "A "+name.toUpperCase()+" doll. Cute!", null, null,
                              new WaitFrames(game, 3, 
                              new SetField(game, "playerCanMove", true,
                              null))));
        }
        else if (this.nameUpper.equals("mewtwo_overworld")) {
            game.playerCanMove = false;
            SpecialMewtwo1 mewtwo = new SpecialMewtwo1(50, this);
            game.battle.oppPokemon = mewtwo;
            // TODO: remove
//            for (Pokemon currPokemon : game.player.pokemon) {
//                if (currPokemon.currentStats.get("hp") > 0) {
//                    game.player.currPokemon = currPokemon;
//                    break;
//                }
//            }
            // The first Pokemon the player sends out in battle should have > 0 hp.
            game.player.setCurrPokemon();
            game.musicController.inBattle = true;  // Enables battle fadeout, etc
//            Action fadeMusic = new FadeMusic("currMusic", "out", "pause", 0.025f, 
//                               new CallMethod(game.currMusic, "setVolume", new Object[]{1f}, null));
            Action fadeMusic = new FadeMusic(game.currMusic, -0.025f, null);
            game.insertAction(new SplitAction(fadeMusic,
                              new WaitFrames(game, 20,
                              new DisplayText(game, "...", null, false, true,
                              new WaitFrames(game, 100,
                              new SpecialBattleMewtwo(game, mewtwo))))));
//            game.fadeMusicAction = fadeMusic;
        }
        // TODO: feedback was that this was annoying
        // TODO: optionally enable/disable based on feedback
//        else if (this.nameUpper.contains("bush") || (this.name.contains("grass") && !this.name.contains("ledge"))) {  // && !this.name.contains("large")
//            game.playerCanMove = false;
//            game.insertAction(new DisplayText(game, "A Grass-type POK?MON can CUT this.", null, null,
//                              new WaitFrames(game, 3, 
//                              new SetField(game, "playerCanMove", true,
//                              null))));
//        }
        else if (this.items().containsKey("torch")) {
            game.playerCanMove = false;
            game.insertAction(new DisplayText(game, "Remove torch?", null, true, false,
                              new DrawYesNoMenu(null,
                                  new DisplayText.Clear(game,
                                  new WaitFrames(game, 3,
                                  new PickupItem(this.items(), "torch",
                                  new SetField(this, "isTorch", false,  // this caches whether tile is torch or not
                                  new SetField(game, "playerCanMove", true,
                                  null))))),
                              new DisplayText.Clear(game,
                              new WaitFrames(game, 3, 
                              new SetField(game, "playerCanMove", true,
                              null))))));
        }
        else if (this.nameUpper.contains("rock1_color")) {
            game.playerCanMove = false;
            game.insertAction(new DisplayText(game, "A Rock-type POK?MON can break this using ROCK SMASH.", null, null,
                              new WaitFrames(game, 3, 
                              new SetField(game, "playerCanMove", true,
                              null))));
        }
        else if ((this.name.contains("water") || this.name.equals("desert2")) &&
                 game.player.currFieldMove.equals("") &&  // No Hm, or a Pokemon is following.
                 game.player.currRod.contains("rod")) {
            String rodName = game.player.currRod;
            game.playerCanMove = false;
            String dirFacing = game.player.dirFacing;
            Sprite fishingSprite = new Sprite(game.player.fishingSprites);
            if (dirFacing.equals("up")) {
                fishingSprite.setRegion(0, 0, 16, 24);
            }
            else if (dirFacing.equals("down")) {
                fishingSprite.setRegion(16, 0, 16, 24);
            }
            else if (dirFacing.equals("left")) {
                fishingSprite.setRegion(32, 0, 24, 16);
                fishingSprite.flip(true, false);
            }
            else if (dirFacing.equals("right")) {
                fishingSprite.setRegion(32, 0, 24, 16);
            }
            // Play the 'potion' sound effect
            Action nextAction = new PlaySound("rod1",
                                new SplitAction(
                                    new SetField(game.player, "isFishing", true, null), 
                                new SetField(game.player, "currSprite", fishingSprite,
                                null)));
            //
            if (Game.rand.nextInt(2) == 0) {
                // Get the encountered Pokemon.
                Route route = new Route("", 2);
                if (this.routeBelongsTo != null && this.routeBelongsTo.name.equals("oasis1")) {
                    route.name = "oasis_pond1";
                }
                else if (this.name.equals("water2")) {
                    route.name = "sea1";
                }
                // TODO: potentially remove depending on feedback
                else if (this.name.equals("desert2")) {
                    route.name = "sand_fishing1";
                }
                // TODO: river fish should depend on the biome river is built through
                // Don't have enough river mons for that currently, tho (I think)
                // Maybe if river is in sand/beach, it contains shellder etc? idk.
                else {
                    route.name = "river1";
                }
                ArrayList<String> eligiblePokemon = new ArrayList<String>(route.allowedPokemon());
                // TODO: debug, remove
                for (String name : eligiblePokemon) {
                    System.out.println(name);
                }
                if (rodName.equals("old rod") || rodName.equals("good rod")) {
                    for (String name : route.superRodPokemon()) {
                        eligiblePokemon.remove(name);
                    }
                }
                if (rodName.equals("old rod")) {
                    for (String name : route.goodRodPokemon()) {
                        eligiblePokemon.remove(name);
                    }
                }
                // TODO: debug, remove
                for (String name : eligiblePokemon) {
                    System.out.println(name);
                }
                String name = eligiblePokemon.get(Game.rand.nextInt(eligiblePokemon.size()));
                // Level based on rod
                int level = 10;
                if (rodName.equals("good rod")) {
                    level = 20;
                }
                else if (rodName.equals("super rod")) {
                    level = 30;
                }
                // Evo the Pokemon if able (based on level requirement)
                Pokemon pokemon = new Pokemon(name, level, Pokemon.Generation.CRYSTAL);
                String evolveTo = null;
                int timesEvolved = 0;
                Map<String, String> evos;
                boolean failed = false;
                while (!failed) {
                    failed = true;
                    evos = Specie.gen2Evos.get(pokemon.specie.name);
                    for (String evo : evos.keySet()) {
                        try {
                            int evoLevel = Integer.valueOf(evo);
                            if (evoLevel <= pokemon.level && Game.rand.nextInt(256) >= 128) {
                                evolveTo = evos.get(evo);
                                pokemon.evolveTo(evolveTo);
                                timesEvolved++;
                                failed = false;
                                break;
                            }
                        }
                        catch (NumberFormatException e) {
                            // Item-based or other type of evo, so just do it regardless of requirement
                            if (Game.rand.nextInt(256) >= 192) {
                                evolveTo = evos.get(evo);
                                pokemon.evolveTo(evolveTo);
                                timesEvolved++;
                                failed = false;
                                break;
                            }
                        }
                    }
                }
                String[] huhs = new String[] {"Whoa!             ", "Oh!               ",
                                              "Huh?              ", "What!?            "};
                String huh = huhs[Game.rand.nextInt(huhs.length)];
                game.player.setCurrPokemon();
                game.battle.oppPokemon = pokemon;
                nextAction.append(new WaitFrames(game, 120, //  90,
                                  game.player.new CaughtFishAnim(
                                  game.player.new Emote("!",
                                  new DisplayText(game, huh+"A bite!", null, null,
                                  new WaitFrames(game, 30,
                                    new SplitAction(
                                        new SetField(game.player, "isFishing", false, null),
                                    new SetField(game.player, "currSprite", game.player.standingSprites.get(dirFacing), 
                                    new SetField(game.musicController, "startBattle", "wild",
                                  Battle.getIntroAction(game, true))))))))));
//                game.insertAction(new WaitFrames(game, 600,
//                                  new SetField(game.player, "isFishing", false,
//                                  null)));
            }
            else {
                String[] huhs = new String[] {"!", "..."};
                String huh = huhs[Game.rand.nextInt(huhs.length)];
                nextAction.append(new WaitFrames(game, 120,
                                  new DisplayText(game, "Not even a nibble"+huh, null, null,
                                    new SplitAction(
                                      new SetField(game.player, "isFishing", false, null), 
                                  new SetField(game, "playerCanMove", true,
                                  new WaitFrames(game, 30,
                                  null))))));
            }
            game.insertAction(nextAction);
        }
    }

    public static class FlipDoorTile extends Action {
        ArrayList<Tile> flipDoors;
        
        @Override
        public void step(Game game) {
            for (Tile tile : this.flipDoors) {
                tile.flipDoorTile();
            }
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
        }
        
        public FlipDoorTile(ArrayList<Tile> flipDoors, Action nextAction) {
            this.flipDoors = flipDoors;
            this.nextAction = nextAction;
        }
    }

    public void flipDoorTile() {
        String[] tokens = this.name.split("__");
        if (tokens.length < 2) {
            System.out.println("somethings wrong.");
            return;
        }
        String name = tokens[0];
        String onOff = tokens[1];
        if (onOff.equals("on")) {
            name += "__off";
            this.attrs.put("solid", false);
        }
        else {
            name += "__on";
            this.attrs.put("solid", true);
        }
        this.name = name;
        Texture text = TextureCache.get(Gdx.files.internal("tiles/buildings/"+name+".png"));
        this.sprite = new Sprite(text, 0, 0, 16, 16);
        this.sprite.setPosition(this.position.x, this.position.y);
    }

    public void onWalkOver() {}
}

/**
 * Sign that displays 'trainer tips' text to player.
 */
class TrainerTipsTile extends Tile {
    public static ArrayList<String> messages = new ArrayList<String>();
    static {
        TrainerTipsTile.initMessages();
    }
    // TODO: hint about foresight making ghosts catchable, once that's done.
    String message = "";
    // TODO: current idea is there is always 1 unown sign per map. will need post-processing in 
    // genforest2 to set one to isUnown=true
    boolean isUnown;

    public TrainerTipsTile(Vector2 pos, Route route) {
        // TODO: post-processing to set one tile to be unown tile.
        // probably should be limited to large maps, or something, otherwise puzzle might be too easy.
        this(pos, route, false, "");
    }

    public TrainerTipsTile(Vector2 pos, Route route, boolean isUnown, String message) {
        super("sand1", "sign1", pos, true, route);
        this.isUnown = isUnown;
        this.message = message;
        if (this.message.equals("")) {
            // the point of grabbing a message and discarding is to try to reduce the number of repeat messages
            // located close together.
            if (TrainerTipsTile.messages.size() <= 0) {
                TrainerTipsTile.initMessages();
            }
            this.message = TrainerTipsTile.messages.get(Game.staticGame.map.rand.nextInt(TrainerTipsTile.messages.size()));
            TrainerTipsTile.messages.remove(this.message);
        }
    }

    public static void initMessages() {
        TrainerTipsTile.messages.clear();
        TrainerTipsTile.messages.add("Stand still while holding X to stop using a Field Move.");
        TrainerTipsTile.messages.add("You can craft POK?BALLS out of Apricorns at a campfire.");
        TrainerTipsTile.messages.add("If you white out during battle, you will return to the last place you used a sleeping bag.");
        TrainerTipsTile.messages.add("Using your sleeping bag will slowly restore your party' hp.");
//        TrainerTipsTile.messages.add("Ghosts will chase you at night, but remember! A campfire will deter them.");
        TrainerTipsTile.messages.add("Ghosts may appear in the woods at night. A campfire will ward them off.");
        TrainerTipsTile.messages.add("Use CUT on trees and tall grass to get building materials.");
        TrainerTipsTile.messages.add("POK?MON will lay EGGS when located near a compatible mate.");
//        TrainerTipsTile.messages.add("Build fences to prevent your pokemon from running away when you let them out of their POK?BALL.");
        TrainerTipsTile.messages.add("Stand still while holding X to stop using a Field Move.");
        TrainerTipsTile.messages.add("Build fences to prevent your POK?MON from wandering off when you let them out of their POK?BALL.");
        TrainerTipsTile.messages.add("You can build a door between two roof tiles to build a back door to your house.");
        TrainerTipsTile.messages.add("Sleeping indoors will restore hp twice as fast as sleeping outdoors.");
        TrainerTipsTile.messages.add("Use CUT on buildings to remove them.");
        TrainerTipsTile.messages.add("Your POK?MON will be happier if fenced in and located near a shelter.");
        TrainerTipsTile.messages.add("POK?MON are happier when located in their natural habitat. If they are happy enough, they may give you items!");
        TrainerTipsTile.messages.add("Sleeping in a bed will over time cure your POK?MON' status conditions.");
        TrainerTipsTile.messages.add("Be wary of taking EGGS near wild POK?MON. Angry parents may attack!");
        TrainerTipsTile.messages.add("Use the escape rope to return to the nearest location on the shore.");
        TrainerTipsTile.messages.add("You can plant Miracle Seeds and Apricorns to grow grass and trees.");
        TrainerTipsTile.messages.add("You can build a gate by building a door between two fences.");
    }

    @Override
    public void onPressA(Game game) {
        game.playerCanMove = false;
        if (this.isUnown) {
            game.musicController.unownMusic = true;
            DisplayText.unownText = true;  // makes text look glitchy
//            game.map.unownSpawn = this;  // TODO: probably don't need this anymore.
            game.currMusic.pause();
            if (!game.loadedMusic.containsKey("unown1")) {
                game.loadedMusic.put("unown1", Gdx.audio.newMusic(Gdx.files.internal("music/unown1.ogg")));
            }
//            Music music = Gdx.audio.newMusic(Gdx.files.internal("music/unown1.ogg"));  // TODO: remove
            game.currMusic = game.loadedMusic.get("unown1");
            game.currMusic.stop();
            game.currMusic.setLooping(true);
            game.currMusic.setVolume(1f);
            game.currMusic.play();
            this.isUnown = false;
        }
        game.insertAction(new DisplayText(game, "TRAINER TIPS!    ", null, null,
                          new DisplayText.Clear(game,
                          new DisplayText(game, this.message, null, null,
                          new WaitFrames(game, 3,  // This fixes issue where final A press will re-trigger text.
                          new SetField(game, "playerCanMove", true,
                          null))))));
    }
}

class RegigigasOutroAnim extends Action {
    int timer = 0;
    int phase = 0;
    Tile regiTile;  // Tile that regigigas is on.
    Tile pedistalTile;
    Music soundEffect;
    Music soundEffect2;
    
    public RegigigasOutroAnim(Action nextAction) {
        this.nextAction = nextAction;
    }

    @Override
    public void firstStep(Game game) {
        for (Tile tile : game.map.tiles.values()) {
            if (tile.name.equals("cave1_regi2")) {
                this.regiTile = tile;
                break;
            }
        }
        // TODO: check that I call dispose on this later.
//        this.soundEffect = Gdx.audio.newMusic(Gdx.files.internal("eq3.ogg"));
        // TODO: dispose
        this.soundEffect2 = Gdx.audio.newMusic(Gdx.files.internal("sounds/splash1.ogg"));
        this.soundEffect2.setLooping(false);
        this.soundEffect2.setVolume(0.3f);

        this.soundEffect = Gdx.audio.newMusic(Gdx.files.internal("sounds/stomp1.ogg"));
        this.soundEffect.setLooping(true);
        this.soundEffect.setVolume(0.7f);
    }

    @Override
    public void step(Game game) {
        // EQ animation with sound effect
        // regi fade in slowly
        if (this.phase == 0) {
            if (this.timer == 0) {
                this.soundEffect.play();
            }
            int step = 40;
//            int step = 60;
            if (this.timer < step*8) {
                if (this.timer % 4 == 0) {
                    game.cam.translate(0, 2);
                    game.player.position.add(0, 2);
                    this.regiTile.sprite.translateY(2);
                }
                else if (this.timer % 4 == 2) {
                    game.cam.translate(0, -2);
                    game.player.position.add(0, -2);
                    this.regiTile.sprite.translateY(-2);
                }
            }
            if (this.timer == step*2) {
                this.regiTile.sprite.setRegion(96, 0, 32, 32);
//                this.soundEffect2.play();
            }
            else if (this.timer == step*4) {
                this.regiTile.sprite.setRegion(64, 0, 32, 32);
//                this.soundEffect2.play();
            }
            else if (this.timer == step*6) {
                this.regiTile.sprite.setRegion(32, 0, 32, 32);
//                this.soundEffect2.play();
            }
            else if (this.timer == step*8) {
                this.regiTile.sprite.setRegion(0, 0, 32, 32);
//                this.soundEffect2.play();
//                game.cam.translate(0, -2);
//                game.player.position.add(0, -2);
//                this.regiTile.sprite.translateY(-2);
                this.soundEffect.stop();
            }
            else if (this.timer == step*9) {
                this.phase = 1;
                this.timer = 0;
            }
        }
        else if (this.phase == 1) {
            this.regiTile.name = "cave1_regi1";
            this.soundEffect.dispose();
            this.soundEffect2.stop();
            this.soundEffect2.dispose();
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
        }
        this.timer++;
    }
}


class PokemonFrame extends Action {
    int timer = 0;
    Pokemon pokemon;
    Sprite bg;
    public boolean isDone = false;

    public PokemonFrame(Pokemon pokemon, Action nextAction) {
        this.pokemon = pokemon;
        this.nextAction = nextAction;
        Texture text = new Texture(Gdx.files.internal("menu/frame1.png"));  // TODO: un-ref
        this.bg = new Sprite(text, 0, 0, 16*10, 16*9);
    }
    
    @Override
    public String getCamera() {
        return "gui";
    }

    @Override
    public int getLayer() {
        return 0;
    }

    @Override
    public void firstStep(Game game) {
        game.battle.oppPokemon = this.pokemon;
        game.insertAction(new WaitFrames(game, 4, new PlaySound(this.pokemon, null)));
        game.insertAction(new PokemonIntroAnim(
                          new WaitFrames(game, 30,
                          new SetField(this, "isDone", true, null))));
    }

    @Override
    public void step(Game game) {
        game.uiBatch.draw(this.bg, 0, 0);
        Sprite sprite = new Sprite(this.pokemon.sprite);
        game.uiBatch.draw(sprite, 84-(int)(this.pokemon.sprite.getWidth()/2), 48);
        if (this.isDone) {
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
        }
    }
}


class FossilMachinePowerUp extends Action {
    int timer = 0;
    ArrayList<Tile> buildingTiles = new ArrayList<Tile>();
    Music soundEffect;
    boolean outro;

    public FossilMachinePowerUp(boolean outro, Action nextAction) {
        this.outro = outro;
        this.nextAction = nextAction;
    }

    @Override
    public void firstStep(Game game) {
        for (Tile tile : game.map.tiles.values()) {
            if (tile.name.contains("building1") && !tile.name.contains("building1_machine")) {
                this.buildingTiles.add(tile);
            }
        }
        
        if (this.outro) {
            for (Tile tile : this.buildingTiles) {
                tile.sprite.setRegion(0, 0, tile.sprite.getHeight(), 16);
            }
            game.mapBatch.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
            return;
        }

//        this.soundEffect = Gdx.audio.newMusic(Gdx.files.internal("para1.ogg"));
//        this.soundEffect.setLooping(false);
//        this.soundEffect.setVolume(0.7f);
//        this.soundEffect.stop();
        game.loadedMusic.put("para1", Gdx.audio.newMusic(Gdx.files.internal("sounds/para1.ogg")));
    }

    @Override
    public void step(Game game) {
        for (Tile tile : this.buildingTiles) {
            if (this.timer < 50) {
                if (this.timer % 16 == 0) {
                    tile.sprite.setRegion(0, 0, (int)tile.sprite.getHeight(), (int)tile.sprite.getHeight());
                    if (tile.overSprite != null) {
                        tile.overSprite.setRegion(0, 0, (int)tile.sprite.getHeight(), (int)tile.overSprite.getHeight());
                    }
                    game.mapBatch.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
                }
                if (this.timer % 16 == 13) {
                    game.insertAction(new PlaySound("para1", .7f, true, null));
                    tile.sprite.setRegion((int)tile.sprite.getHeight(), 0, (int)tile.sprite.getHeight(), (int)tile.sprite.getHeight());
                    if (tile.overSprite != null) {
                        tile.overSprite.setRegion((int)tile.sprite.getHeight(), 0, (int)tile.sprite.getHeight(), (int)tile.overSprite.getHeight());
                    }
                    game.mapBatch.setColor(new Color(1f, 1f, 1f, 1f));
                }
            }
            else if (this.timer < 90) {
                if (this.timer % 4 == 0) {
                    tile.sprite.setRegion(0, 0, (int)tile.sprite.getHeight(), (int)tile.sprite.getHeight());
                    if (tile.overSprite != null) {
                        tile.overSprite.setRegion(0, 0, (int)tile.sprite.getHeight(), (int)tile.overSprite.getHeight());
                    }
                    game.mapBatch.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
                }
                if (this.timer % 4 == 2) {
                    tile.sprite.setRegion((int)tile.sprite.getHeight(), 0, (int)tile.sprite.getHeight(), (int)tile.sprite.getHeight());
                    if (tile.overSprite != null) {
                        tile.overSprite.setRegion((int)tile.sprite.getHeight(), 0, (int)tile.sprite.getHeight(), (int)tile.overSprite.getHeight());
                    }
                    game.mapBatch.setColor(new Color(1f, 1f, 1f, 1f));
                }
            }
        }

        if (this.timer >= 70) {
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
        }

        this.timer++;
    }
    

    static class LightFlicker extends Action {
        int timer = 0;
        int phase = 0;

        ArrayList<Tile> buildingTiles = new ArrayList<Tile>();

        public LightFlicker(Action nextAction) {
            this.nextAction = nextAction;
        }

        @Override
        public void firstStep(Game game) {

            for (Tile tile : game.map.tiles.values()) {
                if (tile.name.contains("building1") && !tile.name.contains("building1_machine")) {
                    this.buildingTiles.add(tile);
                }
            }
        }
        
        @Override
        public String getCamera() {
            return "map";
        }

        @Override
        public int getLayer() {
            return 0;
        }

        @Override
        public void step(Game game) {
            
            if (this.phase == 0) {
                if (this.timer < 24) {
                    if (this.timer % 24 == 0) {

                        for (Tile tile : this.buildingTiles) {
                            tile.sprite.setRegion(0, 0, (int)tile.sprite.getHeight(), (int)tile.sprite.getHeight());
                            if (tile.overSprite != null) {
                                tile.overSprite.setRegion(0, 0, (int)tile.sprite.getHeight(), (int)tile.overSprite.getHeight());
                            }
                        }
                        game.mapBatch.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
                    }
                    else if (this.timer % 24 == 12) {

                        for (Tile tile : this.buildingTiles) {
                            tile.sprite.setRegion((int)tile.sprite.getHeight(), 0, (int)tile.sprite.getHeight(), (int)tile.sprite.getHeight());
                            if (tile.overSprite != null) {
                                tile.overSprite.setRegion((int)tile.sprite.getHeight(), 0, (int)tile.sprite.getHeight(), (int)tile.overSprite.getHeight());
                            }
                        }
                        game.mapBatch.setColor(new Color(1f, 1f, 1f, 1f));
                    }
                }
                else {
                    this.phase++;
                    this.timer = 0;
                }
            }
            if (this.phase == 1) {
                if (this.timer < 40) {
                    if (this.timer % 40 == 0) {
                        for (Tile tile : this.buildingTiles) {
                            tile.sprite.setRegion(0, 0, (int)tile.sprite.getHeight(), (int)tile.sprite.getHeight());
                            if (tile.overSprite != null) {
                                tile.overSprite.setRegion(0, 0, (int)tile.sprite.getHeight(), (int)tile.overSprite.getHeight());
                            }
                        }
                        game.mapBatch.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
                    }
                    else if (this.timer % 40 == 20) {
                        for (Tile tile : this.buildingTiles) {
                            tile.sprite.setRegion((int)tile.sprite.getHeight(), 0, (int)tile.sprite.getHeight(), (int)tile.sprite.getHeight());
                            if (tile.overSprite != null) {
                                tile.overSprite.setRegion((int)tile.sprite.getHeight(), 0, (int)tile.sprite.getHeight(), (int)tile.overSprite.getHeight());
                            }
                        }
                        game.mapBatch.setColor(new Color(1f, 1f, 1f, 1f));
                    }
                }
                else {
                    this.phase++;
                    this.timer = 0;
                }
            }
            if (this.phase == 2) {
                if (this.timer < 48) {
                    if (this.timer % 48 == 0) {
                        for (Tile tile : this.buildingTiles) {
                            tile.sprite.setRegion(0, 0, (int)tile.sprite.getHeight(), (int)tile.sprite.getHeight());
                            if (tile.overSprite != null) {
                                tile.overSprite.setRegion(0, 0, (int)tile.sprite.getHeight(), (int)tile.overSprite.getHeight());
                            }
                        }
                        game.mapBatch.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
                    }
                    else if (this.timer % 48 == 24) {
                        for (Tile tile : this.buildingTiles) {
                            tile.sprite.setRegion((int)tile.sprite.getHeight(), 0, (int)tile.sprite.getHeight(), (int)tile.sprite.getHeight());
                            if (tile.overSprite != null) {
                                tile.overSprite.setRegion((int)tile.sprite.getHeight(), 0, (int)tile.sprite.getHeight(), (int)tile.overSprite.getHeight());
                            }
                        }
                        game.mapBatch.setColor(new Color(1f, 1f, 1f, 1f));
                    }
                }
                else {
                    this.phase++;
                    this.timer = 0;
                }
            }
            if (this.phase == 3) {
                if (this.timer < 48) {
                    if (this.timer % 48 == 0) {
                        for (Tile tile : this.buildingTiles) {
                            tile.sprite.setRegion(0, 0, (int)tile.sprite.getHeight(), (int)tile.sprite.getHeight());
                            if (tile.overSprite != null) {
                                tile.overSprite.setRegion(0, 0, (int)tile.sprite.getHeight(), (int)tile.overSprite.getHeight());
                            }
                        }
                        game.mapBatch.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
                    }
                    else if (this.timer % 48 == 24) {
                        for (Tile tile : this.buildingTiles) {
                            tile.sprite.setRegion((int)tile.sprite.getHeight(), 0, (int)tile.sprite.getHeight(), (int)tile.sprite.getHeight());
                            if (tile.overSprite != null) {
                                tile.overSprite.setRegion((int)tile.sprite.getHeight(), 0, (int)tile.sprite.getHeight(), (int)tile.overSprite.getHeight());
                            }
                        }
                        game.mapBatch.setColor(new Color(1f, 1f, 1f, 1f));
                    }
                }
                else {
                    this.phase++;
                    this.timer = 0;
                }
            }
            if (this.phase == 4) {
                if (this.timer < 56) {
                    if (this.timer % 56 == 0) {
                        for (Tile tile : this.buildingTiles) {
                            tile.sprite.setRegion(0, 0, (int)tile.sprite.getHeight(), (int)tile.sprite.getHeight());
                            if (tile.overSprite != null) {
                                tile.overSprite.setRegion(0, 0, (int)tile.sprite.getHeight(), (int)tile.overSprite.getHeight());
                            }
                        }
                        game.mapBatch.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
                    }
                    else if (this.timer % 56 == 28) {
                        for (Tile tile : this.buildingTiles) {
                            tile.sprite.setRegion((int)tile.sprite.getHeight(), 0, (int)tile.sprite.getHeight(), (int)tile.sprite.getHeight());
                            if (tile.overSprite != null) {
                                tile.overSprite.setRegion((int)tile.sprite.getHeight(), 0, (int)tile.sprite.getHeight(), (int)tile.overSprite.getHeight());
                            }
                        }
                        game.mapBatch.setColor(new Color(1f, 1f, 1f, 1f));
                    }
                }
                else {
                    this.phase++;
                    this.timer = 0;
                }
            }
            if (this.phase == 5) {
                game.actionStack.remove(this);
                game.insertAction(this.nextAction);
            }
            
            this.timer++;
        }
        
    }


    static class DoRevive extends Action {
        int timer = 0;
        int phase = 0;
        Tile machineTile;
        String pokemonName;
        Sprite fossilSprite;
        Music soundEffect;
        int offsetY = 0;
        int regionOffsetY = 0;

        public DoRevive(String pokemonName, Action nextAction) {
            this.pokemonName = pokemonName;
//            this.fossilSprite = new Sprite(TextureCache.get(Gdx.files.internal("tiles/fossil2.png")), 0, 0, 16, 16);
            this.fossilSprite = new Sprite(TextureCache.get(Gdx.files.internal("tiles/fossil1.png")), 0, 0, 16, 16);
            this.nextAction = nextAction;

            if (pokemonName.equals("OMANYTE")) {
                this.regionOffsetY = 16*1;
            }
            else if (pokemonName.equals("KABUTO")) {
                this.regionOffsetY = 16*2;
            }
            else if (pokemonName.equals("AERODACTYL")) {
                this.regionOffsetY = 0;
            }
            else if (pokemonName.equals("LILEEP")) {
                this.regionOffsetY = 16*3;
            }
            else if (pokemonName.equals("ANORITH")) {
                this.regionOffsetY = 16*4;
            }
            else if (pokemonName.equals("SHIELDON")) {
                this.regionOffsetY = 16*5;
            }
            else if (pokemonName.equals("CRANIDOS")) {
                this.regionOffsetY = 16*6;
            }
            this.fossilSprite.setRegion(0, this.regionOffsetY, 16, 16);
        }

        @Override
        public void firstStep(Game game) {
            for (Tile tile : game.map.tiles.values()) {
                if (tile.name.equals("building1_pokecenter1")) {
                    this.machineTile = tile;
                    break;
                }
            }

//            this.soundEffect = Gdx.audio.newMusic(Gdx.files.internal("stomp1.ogg"));
            this.soundEffect = Gdx.audio.newMusic(Gdx.files.internal("sounds/machine1.ogg"));
            this.soundEffect.setLooping(true);
            this.soundEffect.setVolume(0.7f);
            

        }
        
        @Override
        public String getCamera() {
            return "map";
        }

        @Override
        public int getLayer() {
            return 0;
        }

        @Override
        public void step(Game game) {

            if (this.phase == 0) {
                if (this.timer == 120) {
                    this.phase++;
                    this.timer = 0;
                }
            }
            else if (this.phase == 1) {

                if (this.timer == 1) {
                    this.soundEffect.play();
                }

                if (this.timer < 120*3 +2) {
                    if (this.timer % 4 == 0) {
                        game.cam.translate(0, 2);
                        game.player.position.add(0, 2);
                        if (game.player.hmPokemon != null) {
                            game.player.hmPokemon.position.add(0, 2);
                        }
                    }
                    else if (this.timer % 4 == 1) {
                        this.offsetY = 0;
                    }
                    else if (this.timer % 4 == 2) {
                        game.cam.translate(0, -2);
                        game.player.position.add(0, -2);
                        if (game.player.hmPokemon != null) {
                            game.player.hmPokemon.position.add(0, -2);
                        }
                    }
                    else if (this.timer % 4 == 3) {
                        this.offsetY = -2;
                    }
                    if (this.timer % 8 == 0) {
                        this.fossilSprite.setRegion(0, this.regionOffsetY, 16, 16);
                    }
                    else if (this.timer % 8 == 4) {
                        this.fossilSprite.setRegion(16, this.regionOffsetY, 16, 16);
                    }
                    if (this.timer % 16 == 0) {
                        game.insertAction(new PlaySound("teleport1", .5f, true, null));
                    }
                }
                else {
//                    game.playerCanMove = true;
//                    game.player.isCrafting = false;
//                    game.player.hmPokemon.dirFacing = "left";
                    this.soundEffect.stop();
                    this.soundEffect.dispose();
                    this.machineTile.nameUpper = "revived_"+this.pokemonName;
                    game.actionStack.remove(this);
                    game.insertAction(this.nextAction);
//                    game.insertAction(new PlaySound("dingdong1", .5f, false,
//                                      new FossilMachinePowerUp(true, null)));

                    // TODO: ideally this would be shiny if mon is shiny
                    Pokemon tempPokemon = new Pokemon(this.pokemonName.toLowerCase(), 2, Pokemon.Generation.CRYSTAL, false, false);
                    game.insertAction(new WaitFrames(game, 90,
                                      new PokemonFrame(tempPokemon,
                                      new WaitFrames(game, 20,
                                      new SplitAction(
                                          new LightFlicker(null),                
                                      new PlaySound("heal1_downshift",
                                      new SplitAction(
                                          new PlaySound("stomp1", null),
                                      new FossilMachinePowerUp(true,
                                      new SetField(game, "playerCanMove", true,
                                      new SetField(game.player, "isCrafting", false,
                                      new SetField(game.player.hmPokemon, "dirFacing", "left",
                                      null)))))))))));
        
                }
            }

            game.mapBatch.draw(this.fossilSprite,
                               this.machineTile.position.x+8,
                               this.machineTile.position.y+8+this.offsetY);  // TODO: offsetY not working
            this.timer++;
        }
    }
}


class RegigigasIntroAnim extends Action {
    int timer = 0;
    int phase = 0;
    Tile regiTile;  // Tile that regigigas is on.
    Tile pedistalTile;
    Music soundEffect;
    Music soundEffect2;
    Sprite lightningSprite;
    String dirFacing = "left";
    String regiName;
    int regionNum = 0;
    
    public RegigigasIntroAnim(String regiName, String dirFacing, Action nextAction) {
        this.regiName = regiName;
        this.dirFacing = dirFacing;
        this.nextAction = nextAction;
        Texture text = TextureCache.get(Gdx.files.internal("lightning1.png"));
        this.lightningSprite = new Sprite(text, 0, 0, 160, 144);
    }

    @Override
    public void firstStep(Game game) {
        for (Tile tile : game.map.tiles.values()) {
            if (tile.name.equals("cave1_regi1")) {
                this.regiTile = tile;
                break;
            }
        }
        for (Tile tile : game.map.tiles.values()) {
            if (tile.name.contains("cave1_regipedistal1")) {
                this.pedistalTile = tile;
                break;
            }
        }
        // TODO: check that I call dispose on this later.
//        this.soundEffect = Gdx.audio.newMusic(Gdx.files.internal("eq3.ogg"));
        // TODO: dispose
        this.soundEffect2 = Gdx.audio.newMusic(Gdx.files.internal("sounds/splash1.ogg"));
        this.soundEffect2.setLooping(false);
        this.soundEffect2.setVolume(0.3f);

        this.soundEffect = Gdx.audio.newMusic(Gdx.files.internal("sounds/stomp1.ogg"));
        this.soundEffect.setLooping(true);
        this.soundEffect.setVolume(0.7f);
    }

    @Override
    public void step(Game game) {
        // EQ animation with sound effect
        // regi fade in slowly
        if (this.phase == 0) {
            if (this.timer == 0) {
                this.soundEffect.play();
            }
            int step = 60;
            if (this.timer < step*8) {
                if (this.timer % 4 == 0) {
                    game.cam.translate(0, 2);
                    game.player.position.add(0, 2);
                    this.regiTile.sprite.translateY(2);
                }
                else if (this.timer % 4 == 2) {
                    game.cam.translate(0, -2);
                    game.player.position.add(0, -2);
                    this.regiTile.sprite.translateY(-2);
                }
            }
            if (this.timer == step*2) {
                this.regiTile.sprite.setRegionX(32);
                this.regiTile.sprite.setRegionWidth(32);
                this.soundEffect2.play();
            }
            else if (this.timer == step*4) {
                this.regiTile.sprite.setRegionX(64);
                this.regiTile.sprite.setRegionWidth(32);
                this.soundEffect2.play();
            }
            else if (this.timer == step*6) {
                this.regiTile.sprite.setRegionX(96);
                this.regiTile.sprite.setRegionWidth(32);
                this.soundEffect2.play();
            }
            else if (this.timer == step*8) {
                this.regiTile.sprite.setRegionX(128);
                this.regiTile.sprite.setRegionWidth(32);
//                this.soundEffect2.play();
//                game.cam.translate(0, -2);
//                game.player.position.add(0, -2);
//                this.regiTile.sprite.translateY(-2);
                this.soundEffect.stop();
            }
            else if (this.timer == step*10) {
                this.phase = 1;
                this.timer = 0;
            }
        }
        else if (this.phase == 1) {
            if (this.timer == 1) {
                this.soundEffect.stop();
                this.soundEffect.dispose();
//                this.soundEffect = Gdx.audio.newMusic(Gdx.files.internal("gigas_noises12.ogg"));
                this.soundEffect = Gdx.audio.newMusic(Gdx.files.internal("sounds/gigas_noises14.ogg"));
                this.soundEffect.setVolume(1f);
                this.soundEffect.play();
            }
            
            if (this.timer < 36) {
                if (this.timer < 18) {
                    if (this.timer % 4 == 0) {
                        this.regiTile.sprite.setRegion(192, 0, 32, 32);
                    }
                    else if (this.timer % 4 == 2) {
                        this.regiTile.sprite.setRegion(128, 0, 32, 32);
                    }
                }
                else if (this.timer < 36) {
                    if (this.timer % 6 == 0) {
                        this.regiTile.sprite.setRegion(192, 0, 32, 32);
                    }
                    else if (this.timer % 6 == 3) {
                        this.regiTile.sprite.setRegion(128, 0, 32, 32);
                    }
                }
            }
            else if (this.timer < 70) {
                
            }
            else if (this.timer < 70 +36) {
                if (this.timer == 70) {
                    this.soundEffect.stop();
                    this.soundEffect.play();
                }
                if (this.timer < 70 +18) {
                    if (this.timer % 4 == 0) {
                        this.regiTile.sprite.setRegion(224, 0, 32, 32);
                    }
                    else if (this.timer % 4 == 2) {
                        this.regiTile.sprite.setRegion(128, 0, 32, 32);
                    }
                }
                else if (this.timer < 70 +36) {
                    if (this.timer % 6 == 0) {
                        this.regiTile.sprite.setRegion(224, 0, 32, 32);
                    }
                    else if (this.timer % 6 == 3) {
                        this.regiTile.sprite.setRegion(128, 0, 32, 32);
                    }
                }
            }
            else if (this.timer < 110) {
                
            }
            else if (this.timer < 110 +36) {
                if (this.timer == 110) {
                    this.soundEffect.stop();
                    this.soundEffect.play();
                }
                if (this.timer < 110 +18) {
                    if (this.timer % 4 == 0) {
                        this.regiTile.sprite.setRegion(288, 0, 32, 32);
                    }
                    else if (this.timer % 4 == 2) {
                        this.regiTile.sprite.setRegion(128, 0, 32, 32);
                    }
                }
                else if (this.timer < 110 +36) {
                    if (this.timer % 6 == 0) {
                        this.regiTile.sprite.setRegion(288, 0, 32, 32);
                    }
                    else if (this.timer % 6 == 3) {
                        this.regiTile.sprite.setRegion(128, 0, 32, 32);
                    }
                }
            }
            else if (this.timer < 150) {
                
            }
            else if (this.timer < 150 +36) {
                if (this.timer == 150) {
//                    this.soundEffect = Gdx.audio.newMusic(Gdx.files.internal("sounds/gigas_noises15.ogg"));
//                    this.soundEffect.setVolume(1f);
                    this.soundEffect.stop();
                    this.soundEffect.play();
                }
                if (this.timer < 150 +18) {
                    if (this.timer % 4 == 0) {
                        this.regiTile.sprite.setRegion(256, 0, 32, 32);
                    }
                    else if (this.timer % 4 == 2) {
                        this.regiTile.sprite.setRegion(128, 0, 32, 32);
                    }
                }
                else if (this.timer < 150 +36) {
                    if (this.timer % 6 == 0) {
                        this.regiTile.sprite.setRegion(256, 0, 32, 32);
                    }
                    else if (this.timer % 6 == 3) {
                        this.regiTile.sprite.setRegion(128, 0, 32, 32);
                    }
                }
            }
            else if (this.timer < 220) {
                
            }
            else {
                this.phase=4;
                this.timer = 0;
            }
        }
        // TODO: remove, unused
        else if (this.phase == 1) {
            if (this.timer == 20) {
                this.soundEffect.stop();
                this.soundEffect.dispose();
                this.soundEffect = Gdx.audio.newMusic(Gdx.files.internal("sounds/gigas_noises2.ogg"));
                this.soundEffect.setVolume(1f);
                this.soundEffect.play();
                this.soundEffect.pause();
                this.soundEffect.setPosition(5);
            }
            if (this.timer < 120) {
                
            }
            else if (this.timer < 180) {
                if (this.timer == 120) {
                    this.soundEffect.play();
                }
                if (this.timer % 6 == 0) {
                    this.regiTile.sprite.setRegionX(160);
                    this.regiTile.sprite.setRegionWidth(32);
                }
                else if (this.timer % 6 == 3) {
                    this.regiTile.sprite.setRegionX(128);
                    this.regiTile.sprite.setRegionWidth(32);
                }
            }
            else if (this.timer < 240) {
                
            }
            else {
                this.soundEffect.stop();
                this.phase++;
                this.timer = 0;
            }
        }
        // TODO: not using this one
        else if (this.phase == 1) {
            if (this.timer == 2) {
                this.soundEffect.stop();
                this.soundEffect.dispose();
            }
            if (this.timer == 20) {
                this.soundEffect = Gdx.audio.newMusic(Gdx.files.internal("sounds/gigas_noises7.ogg"));
                this.soundEffect.setVolume(1f);
            }
            if (this.timer < 80) {
            
            }
            else if (this.timer < 140) {
                if (this.timer % 4 == 0) {
                    this.regiTile.sprite.setRegionX(128);
                    this.regiTile.sprite.setRegionWidth(32);
                    this.soundEffect.stop();
                    this.soundEffect.play();
                }
                else if (this.timer % 4 == 3) {
                    this.regiTile.sprite.setRegionX(160);
                    this.regiTile.sprite.setRegionWidth(32);
                }
            }
            else if (this.timer < 200) {
                
            }
            else {
                this.phase++;
                this.timer = 0;
            }
        }
        // TODO: unused
        else if (this.phase == 2 || this.phase == 3) {
            if (this.timer == 2) {
                this.soundEffect.stop();
                this.soundEffect.dispose();
            }
            if (this.timer == 20) {
                this.soundEffect = Gdx.audio.newMusic(Gdx.files.internal("sounds/gigas_noises9.ogg"));
                this.soundEffect.setVolume(1f);
                this.soundEffect.play();
                this.regiTile.sprite.setRegion(192, 0, 32, 32);
            }
            else if (this.timer == 24) {
                this.regiTile.sprite.setRegion(224, 0, 32, 32);
            }
            else if (this.timer == 28) {
                this.regiTile.sprite.setRegion(256, 0, 32, 32);
            }
            else if (this.timer == 32) {
                this.regiTile.sprite.setRegion(288, 0, 32, 32);
            }
            else if (this.timer == 36) {
                this.regiTile.sprite.setRegion(128, 0, 32, 32);
            }
            else if (this.timer == 100) {
                this.phase++;
                this.timer = 0;
            }
        }
        // Flash eyes and then leave on
        // play lightning strike
        // play lightning strike flashes
        // play lightning srike sound
        else if (this.phase == 4) {

            if (this.timer < 30) {
            }
            else if (this.timer < 60) {
                if (this.timer % 6 == 0) {
                    this.regiTile.sprite.setRegion(128, 0, 32, 32);
                }
                else if (this.timer % 6 == 3) {
                    this.regiTile.sprite.setRegion(160, 0, 32, 32);
                }
            }
            else if (this.timer < 80) {
            
            }
            else if (this.timer < 160) {
                if (this.timer % 4 == 0) {
                    game.cam.translate(0, 2);
                    game.player.position.add(0, 2);
                    this.regiTile.sprite.translateY(2);
                }
                else if (this.timer % 4 == 2) {
                    game.cam.translate(0, -2);
                    game.player.position.add(0, -2);
                    this.regiTile.sprite.translateY(-2);
                }
            }
            else if (this.timer < 220) {
                if (this.timer % 6 == 0) {
                    this.pedistalTile.overSprite.setRegion(0, 0, 16, 16);
                }
                else if (this.timer % 6 == 3) {
                    this.pedistalTile.overSprite.setRegion(this.regionNum, 0, 16, 16);
                }
            }
//            else if (this.timer == 180) {
//                game.cam.translate(0, -2);
//                game.player.position.add(0, -2);
//                this.regiTile.sprite.translateY(-2);
//            }

            if (this.timer == 1) {
                this.lightningSprite.setRegion(160*4, 0, 160, 144);
                this.soundEffect.stop();
                this.soundEffect.dispose();
                this.soundEffect2.stop();
                this.soundEffect2.dispose();
                if (this.regiName.equals("REGIDRAGO")) {
                    this.regionNum = 16;
                }
                else if (this.regiName.equals("REGIELEKI")) {
                    this.regionNum = 32;
                }
                else if (this.regiName.equals("REGICE")) {
                    this.regionNum = 48;
                }
                else if (this.regiName.equals("REGIROCK")) {
                    this.regionNum = 64;
                }
                else if (this.regiName.equals("REGISTEEL")) {
                    this.regionNum = 80;
                }
            }
            else if (this.timer == 60) {
                this.soundEffect = Gdx.audio.newMusic(Gdx.files.internal("attacks/thunderpunch_player_gsc/sound.ogg"));
                this.soundEffect.setVolume(1f);
                this.soundEffect.play();
                SpriteProxy.inverseColors = true;
                this.lightningSprite.setRegion(160*0, 0, 160, 144);
                game.insertAction(new LightningFlash(null));
            }
            else if (this.timer == 62) {
                SpriteProxy.inverseColors = false;
                this.lightningSprite.setRegion(160*1, 0, 160, 144);
            }
            else if (this.timer == 64) {
                SpriteProxy.inverseColors = true;
                this.lightningSprite.setRegion(160*2, 0, 160, 144);
            }
            else if (this.timer == 68) {
                SpriteProxy.inverseColors = false;
                this.lightningSprite.setRegion(160*3, 0, 160, 144);
            }
            else if (this.timer == 80) {
                this.lightningSprite.setRegion(160*4, 0, 160, 144);
            }
            else if (this.timer == 160) {
                this.regiTile.sprite.setRegion(128, 0, 32, 32);
                this.soundEffect2 = Gdx.audio.newMusic(Gdx.files.internal("sounds/hit.ogg"));
                this.soundEffect2.setVolume(1f);
                this.soundEffect2.play();
                String knockBackDir = "left";
                if (this.dirFacing.equals("right")) {
                    knockBackDir = "left";
                }
                else if (this.dirFacing.equals("up")) {
                    knockBackDir = "down";
                }
                game.player.dirFacing = this.dirFacing;
                game.player.currSprite = game.player.standingSprites.get(game.player.dirFacing);
                game.insertAction(new PlayerKnockedBack(game, game.player, knockBackDir));
                

                this.regiTile.name = "cave1_regi2";
                this.pedistalTile.nameUpper = this.regiName;  // TODO: enable depending
                Texture text = TextureCache.get(Gdx.files.internal("tiles/cave1/overworld2.png"));
                this.pedistalTile.overSprite = new Sprite(text, this.regionNum, 0, 16, 16);
                // TODO: remove once DrawMap does game.mapBatch.draw(overSprite) instead
                this.pedistalTile.overSprite.setPosition(this.pedistalTile.position.x, this.pedistalTile.position.y);
                
            }
            else if (this.timer == 190) {
                
                game.playerCanMove = true;
            }
            else if (this.timer == 230) {
                this.soundEffect.stop();
                this.soundEffect.dispose();
                this.soundEffect2.stop();
                this.soundEffect2.dispose();
                game.actionStack.remove(this);
            }
            game.mapBatch.draw(this.lightningSprite, this.regiTile.position.x -110, this.regiTile.position.y -94);
        }
        this.timer++;
    }

    static class RocksEffect1 extends Action {
//        public static int velocityX = 0;
//        public static boolean shouldMoveX = false;
        public static float velocityX = 2;
        public static boolean shouldMoveX = true;

        public static boolean shouldMoveY = true;

        public int layer = 111;
        Sprite textboxSprite;
        Sprite[] sprites = new Sprite[20];
        // flip-flop between these two velocities
        int[] velocities = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        int[] velocities2 = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        int velocity = 1;
        int whichVelocity = 0;
        Random rand = new Random();
        boolean firstStep = true;

        public RocksEffect1() {
            Texture text = new Texture(Gdx.files.internal("battle/battle_bg4.png"));
            this.textboxSprite = new Sprite(text, 0, 0, 176, 160);
            this.textboxSprite.setPosition(-8, -8);

            text = new Texture(Gdx.files.internal("battle/rock2.png"));
            this.sprites[0] = new Sprite(text, 0*32, 0, 32, 32);
            this.sprites[0].setColor(1f, 1f, 1f, 1f);
            this.sprites[1] = new Sprite(text, 1*32, 0, 32, 32);
//            this.sprites[2] = new Sprite(text, 2*32, 0, 32, 32);
//            this.sprites[3] = new Sprite(text, 5*32, 0, 32, 32);
            this.sprites[2] = new Sprite(text, 2*32, 0, 32, 32);
            this.sprites[3] = new Sprite(text, 5*32, 0, 32, 32);
            this.sprites[4] = new Sprite(text, 6*32, 0, 32, 32);
            this.sprites[5] = new Sprite(text, 1*32, 0, 32, 32);
//            this.sprites[6] = new Sprite(text, 2*32, 0, 32, 32);
//            this.sprites[7] = new Sprite(text, 5*32, 0, 32, 32);
            this.sprites[6] = new Sprite(text, 2*32, 0, 32, 32);
            this.sprites[7] = new Sprite(text, 5*32, 0, 32, 32);
            this.sprites[8] = new Sprite(text, 6*32, 0, 32, 32);
            this.sprites[9] = new Sprite(text, 0*32, 0, 32, 32);
            this.sprites[10] = new Sprite(text, 1*32, 0, 32, 32);
            this.sprites[11] = new Sprite(text, 2*32, 0, 32, 32);
            this.sprites[12] = new Sprite(text, 5*32, 0, 32, 32);
            this.sprites[13] = new Sprite(text, 6*32, 0, 32, 32);
            this.sprites[14] = new Sprite(text, 1*32, 0, 32, 32);
            this.sprites[15] = new Sprite(text, 2*32, 0, 32, 32);
            this.sprites[16] = new Sprite(text, 5*32, 0, 32, 32);
            this.sprites[17] = new Sprite(text, 6*32, 0, 32, 32);
            this.sprites[18] = new Sprite(text, 0*32, 0, 32, 32);
            this.sprites[19] = new Sprite(text, 0*32, 0, 32, 32);

            for (int i=0; i < 20; i++) {
                this.sprites[i].setPosition(rand.nextInt(160+32)-32, rand.nextInt(144) - 144);
                this.sprites[i].setRotation(rand.nextInt(4) * 90);

                this.velocities[i] = 2; //rand.nextInt(2) + 1;
                this.velocities2[i] = this.velocities[i] - 1; // -1 +rand.nextInt(2);
            }
        }
        public String getCamera() {return "gui";}

        public int getLayer(){return this.layer;}

        @Override
        public void step(Game game) {
            if (this.firstStep) {
                RegigigasIntroAnim.RocksEffect2.drawRocks = true;
                this.firstStep = false;
            }

            for (int i=0; i < 20; i++) {
                if (this.whichVelocity == 0) {
                    this.velocity = this.velocities[i];
                }
                else {
                    this.velocity = this.velocities2[i];
                }
                if (!RocksEffect1.shouldMoveY) {
                    this.velocity = 0;
                }

                RocksEffect1.velocityX = 0;
//                if (this.sprites[i].getY() > 30) {
//                    RocksEffect1.velocityX = 100f/this.sprites[i].getY();
//                }
                this.sprites[i].setPosition(this.sprites[i].getX() + RocksEffect1.velocityX,
                                            this.sprites[i].getY() + this.velocity);
                if (this.sprites[i].getY() > 144) {
                    this.sprites[i].setPosition(rand.nextInt(160+32)-32, rand.nextInt(144) - 144);
//                    this.sprites[i].setRotation(rand.nextInt(4) * 90);
//                    this.velocities[i] = rand.nextInt(2) + 1;
//                    this.velocities2[i] = this.velocities[i] -1 +rand.nextInt(2);
                }
//                if (this.sprites[i].getX() < 0) {
//                    this.sprites[i].setPosition(160, rand.nextInt(144) - 32);
//                }
                if (this.sprites[i].getX() > 160) {
                    this.sprites[i].setPosition(-32, this.sprites[i].getY());
                }
                this.sprites[i].draw(game.uiBatch);

            }
            this.whichVelocity = (this.whichVelocity + 1) % 2;

            // need textbox over animation
            this.textboxSprite.draw(game.uiBatch);

            if (game.battle.drawAction == null) {
                game.actionStack.remove(this);
                return;
            }
        }
    }

    static class RocksEffect2 extends Action {
        public static int velocityX = 0;
        public static boolean shouldMoveX = false;
        public static boolean shouldMoveY = true;
        static boolean drawRocks = false;
        public int layer = 131;
        Sprite bgSprite;
        Sprite bgSprite2;
        Sprite[] sprites = new Sprite[10];
        // flip-flop between these two velocities
        int[] velocities = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        int[] velocities2 = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        int velocity = 1;
        int whichVelocity = 0;
        Random rand = new Random();
        float[] bg_values = new float[]{0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 08f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 08f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.9f, 0.9f, 0.9f, 0.9f, 0.9f, 0.9f,
                                        0.95f, 0.95f, 0.95f, 0.95f, 0.95f, 0.95f,
                                        1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
                                        1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
                                        1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
                                        1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
                                        1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
                                        1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
                                        1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
                                        1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
                                        0.95f, 0.95f, 0.95f, 0.95f, 0.95f, 0.95f,
                                        0.9f, 0.9f, 0.9f, 0.9f, 0.9f, 0.9f,};
        int bg_values_idx = 0;
        float curr_value = 0f;

        public RocksEffect2() {
            Texture text = new Texture(Gdx.files.internal("battle/battle_bg3.png"));
            this.bgSprite = new Sprite(text, 0, 0, 176, 160);
            this.bgSprite.setPosition(-8, -8);

            text = new Texture(Gdx.files.internal("battle/battle_bg5.png"));
            this.bgSprite2 = new Sprite(text, 0, 0, 176, 160);
            this.bgSprite2.setPosition(-8, -8);

            text = new Texture(Gdx.files.internal("battle/rock1.png"));
            this.sprites[0] = new Sprite(text, 0*32, 0, 32, 32);
            this.sprites[0].setColor(0f, 0f, 0f, 1f);
            this.sprites[1] = new Sprite(text, 1*32, 0, 32, 32);
            this.sprites[1].setColor(0f, 0f, 0f, 1f);
            this.sprites[2] = new Sprite(text, 2*32, 0, 32, 32);
            this.sprites[2].setColor(0f, 0f, 0f, 1f);
            this.sprites[3] = new Sprite(text, 5*32, 0, 32, 32);
            this.sprites[3].setColor(0f, 0f, 0f, 1f);
            this.sprites[4] = new Sprite(text, 0*32, 0, 32, 32);
            this.sprites[4].setColor(0f, 0f, 0f, 1f);
            this.sprites[5] = new Sprite(text, 1*32, 0, 32, 32);
            this.sprites[5].setColor(0f, 0f, 0f, 1f);
            this.sprites[6] = new Sprite(text, 2*32, 0, 32, 32);
            this.sprites[6].setColor(0f, 0f, 0f, 1f);
            this.sprites[7] = new Sprite(text, 5*32, 0, 32, 32);
            this.sprites[7].setColor(0f, 0f, 0f, 1f);
            this.sprites[8] = new Sprite(text, 0*32, 0, 32, 32);
            this.sprites[8].setColor(0f, 0f, 0f, 1f);
            this.sprites[9] = new Sprite(text, 0*32, 0, 32, 32);
            this.sprites[9].setColor(0f, 0f, 0f, 1f);

            for (int i=0; i < 10; i++) {
                this.sprites[i].setPosition(rand.nextInt(160-32), rand.nextInt(46) + (144-46-32-46));
                this.velocities[i] = rand.nextInt(5) + 4;
                this.velocities2[i] = this.velocities[i];
                this.sprites[i].setRotation(rand.nextInt(4) * 90);
            }
        }

        public String getCamera() {return "gui";}

        public int getLayer(){return this.layer;}

        @Override
        public void step(Game game) {
            // didn't end up liking this
//            // need bg under the animation
//            this.curr_value = this.bg_values[this.bg_values_idx];
////            System.out.println(curr_value);
//            this.bg_values_idx += 1;
//            if (this.bg_values_idx >= this.bg_values.length) {
//                this.bg_values_idx = 0;
//            }
//
//            this.bgSprite.setColor(this.curr_value, this.curr_value, this.curr_value, 1f);
//            this.bgSprite2.setColor(this.curr_value, this.curr_value, this.curr_value, 1f);
            this.bgSprite.draw(game.uiBatch);
//            game.floatingBatch.draw(this.bgSprite, 0, 0);

            if (RocksEffect2.drawRocks) {
                for (int i=0; i < 10; i++) {
                    if (this.velocities2[i] <= 0) {
                        this.velocity = 1;
                        this.velocities2[i] = this.velocities[i];
                    }
                    else {
                        this.velocity = 0;
                    }
                    this.velocities2[i]--;

                    if (!RocksEffect2.shouldMoveY) {
                        this.velocity = 0;
                    }

                    this.sprites[i].setPosition(this.sprites[i].getX() + RocksEffect2.velocityX,
                                                this.sprites[i].getY() + this.velocity);
                    if (this.sprites[i].getY() > 144) {
                        this.sprites[i].setPosition(rand.nextInt(160-32), rand.nextInt(46) + (144-46-32-46));
                        this.sprites[i].setRotation(rand.nextInt(4) * 90);
                        this.velocities[i] = rand.nextInt(5) + 4;
                        this.velocities2[i] = this.velocities[i];
                    }
                    if (this.sprites[i].getX() < 0) {
                        this.sprites[i].setPosition(160, rand.nextInt(144-32));
                        this.sprites[i].setRotation(rand.nextInt(4) * 90);
                    }
                    this.sprites[i].draw(game.uiBatch);

                }
                this.whichVelocity = (this.whichVelocity + 1) % 2;
                this.bgSprite2.draw(game.uiBatch);
            }

            if (game.battle.drawAction == null) {
                game.actionStack.remove(this);
                return;
            }
        }
    }

    public static class BattleIntro extends Action {
        public int timer = 0;
        public Sprite sprite;

        public BattleIntro(Action nextAction) {
            this.nextAction = nextAction;
            Texture text = TextureCache.get(Gdx.files.internal("tiles/cave1/regi_eye1.png"));
            this.sprite = new Sprite(text, 0, 0, 16, 16);
        }
        
        @Override
        public void firstStep(Game game) {}
        
        @Override
        public String getCamera() {
            return "gui";
        }
        
        @Override
        public int getLayer() {
            return 140;
        }

        @Override
        public void step(Game game) {
            if (this.timer > 0) {
                float alpha = 1f;
                if (this.timer < 2) {
                    alpha = 0.3f;
                }
                else if (this.timer < 4) {
                    alpha = 0.6f;
                }
//                game.uiBatch.draw(this.sprite, this.regiTile.position.x-32+16, this.regiTile.position.y-64-16);
                this.sprite.setAlpha(alpha);
                this.sprite.setPosition(72-32+8, 80-64-8);
                this.sprite.draw(game.uiBatch);
//                game.uiBatch.draw(this.sprite, 72-32+8, 80-64-8);
            }
            if (this.timer > 70) {
                float alpha = 1f;
                if (this.timer < 70 +2) {
                    alpha = 0.3f;
                }
                else if (this.timer < 70 +4) {
                    alpha = 0.6f;
                }
                this.sprite.setAlpha(alpha);
                this.sprite.setPosition(72-64+8, 80-16-8);
                this.sprite.draw(game.uiBatch);
//                game.uiBatch.draw(this.sprite, 72-64+8, 80-16-8);
            }
            if (this.timer > 140) {
                float alpha = 1f;
                if (this.timer < 140 +2) {
                    alpha = 0.3f;
                }
                else if (this.timer < 140 +4) {
                    alpha = 0.6f;
                }
                this.sprite.setAlpha(alpha);
                this.sprite.setPosition(72-32+8, 80+32-8);
                this.sprite.draw(game.uiBatch);
//                game.uiBatch.draw(this.sprite, 72-32+8, 80+32-8);
            }
            if (this.timer > 190-6) {
                float alpha = 1f;
                if (this.timer < 190-6 +2) {
                    alpha = 0.3f;
                }
                else if (this.timer < 190-6 +4) {
                    alpha = 0.6f;
                }
                this.sprite.setAlpha(alpha);
                game.uiBatch.draw(this.sprite, 72+16+8, 80+32-8);
            }
            if (this.timer > 230-6-6) {
                float alpha = 1f;
                if (this.timer < 230-6-6 +2) {
                    alpha = 0.3f;
                }
                else if (this.timer < 230-6-6 +4) {
                    alpha = 0.6f;
                }
                this.sprite.setAlpha(alpha);
                this.sprite.setPosition(72+48+8, 80-16-8);
                this.sprite.draw(game.uiBatch);
//                game.uiBatch.draw(this.sprite, 72+48+8, 80-16-8);
            }
            if (this.timer > 260-10-6) {
                float alpha = 1f;
                if (this.timer < 260-10-6 +2) {
                    alpha = 0.3f;
                }
                else if (this.timer < 260-10-6 +4) {
                    alpha = 0.6f;
                }
                this.sprite.setAlpha(alpha);
                this.sprite.setPosition(72+16+8, 80-64-8);
                this.sprite.draw(game.uiBatch);
//                game.uiBatch.draw(this.sprite, 72+16+8, 80-64-8);
            }
//            if (this.timer > 330) {
//                float alpha = 1f;
//                if (this.timer < 2) {
//                    alpha = 0.3f;
//                }
//                else if (this.timer < 4) {
//                    alpha = 0.6f;
//                }
//                game.mapBatch.draw(this.sprite, this.regiTile.position.x+32, this.regiTile.position.y-32);
//            }

            if (this.timer >= 370) {
                game.actionStack.remove(this);
                game.insertAction(this.nextAction);
            }

            this.timer++;
        }
    }
}


class PutTile extends Action {
    public int layer = 0;
    public Map<Vector2, Tile> tiles;
    public Tile tile;

    public PutTile(Game game, Tile tile, Action nextAction) {
        this.tile = tile;
        this.tiles = game.map.tiles;
        this.nextAction = nextAction;
    }
    public String getCamera() {return "map";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        this.tiles.put(this.tile.position.cpy(), this.tile);
        game.actionStack.remove(this);
        game.insertAction(this.nextAction);
    }
}

class OverworldAnimation extends Action {
    public int layer = 0;
    Vector2 position;
    String name;
    HashMap<Integer, String> metadata = new HashMap<Integer, String>();
    Music sound;
    int frameNum = 1;
    int timer = 2;
    Texture currText;
    Sprite currFrame;
    boolean flip = false;
    boolean firstStep = true;
    

    public OverworldAnimation(Game game, String name, Vector2 position, boolean flip, Action nextAction) {
        this.name = name.toLowerCase().replace(' ', '_');
        this.position = position;
        this.flip = flip;
        this.nextAction = nextAction;
    }
    public String getCamera() {return "map";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        if (this.firstStep) {
            // I did the Psychic

            // Load metadata for each frame
            // ex: player_healthbar_gone -> means to make player's healthbar transparent during this frame
            try {
                FileHandle file = Gdx.files.internal("attacks/" + this.name + "/metadata.out");
                Reader reader = file.reader();
                BufferedReader br = new BufferedReader(reader);
                String line;
//                    int lineNum = 0;
                while ((line = br.readLine()) != null)   {
                    int frameNum = Integer.valueOf(line.split(", ")[0]);
                    String properties = line.split(", ")[1];
                    this.metadata.put(frameNum, properties);
//                        lineNum++;
                }
                reader.close();
                // load sound to play and play it
                if (!this.name.contains("evolve")) {
                    this.sound = Gdx.audio.newMusic(Gdx.files.internal("attacks/" + this.name + "/sound.ogg"));
                    this.sound.play();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GdxRuntimeException e) {
                // pass, this animation will just be skipped
            }
            this.firstStep = false;
            // TODO: test
            // Pre-cache all frames in the attack animation
            for (int i=0; ; i++) {
                FileHandle filehandle = Gdx.files.internal("attacks/" + this.name + "/output/frame-" + String.format("%03d", i) + ".png");
                if (!filehandle.exists()) {
                    break;
                }
                // This is just for pre-caching
                TextureCache.get(filehandle);
            }
        }
//        int frame = this.frameNum - (this.frameNum % 2) +1;
        // If next frame doesn't exist in animation, return
        FileHandle filehandle = Gdx.files.internal("attacks/" + this.name + "/output/frame-" + String.format("%03d", this.frameNum) + ".png");
        if (!filehandle.exists()) {  // TODO: > 6 is unique to dig anim
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
            return;
        }
        // Draw current frame
        this.currText = TextureCache.get(filehandle);
        this.currFrame = new Sprite(this.currText, 0, 0, 160, 144);
        if (this.flip) {
            this.currFrame.flip(true, false);
        }
        game.mapBatch.draw(this.currFrame, this.position.x, this.position.y);

        // Handle metadata
        if (this.metadata.containsKey(this.frameNum)) {
            String properties = this.metadata.get(this.frameNum);
            // TODO: nothing atm
        }
        this.timer--;
        if (this.timer <= 0) {
            this.timer = 2;
            this.frameNum++;
        }
    }
}

class FogEffect extends Action {
    public static boolean active = false;
    public static boolean refresh = true;
    Texture fogTexture1;
    Texture fogTexture2;
    Texture shadeTexture1;
    Texture shadeTexture2;
    Pixmap shadePixmap1;
    Pixmap shadePixmap2;
    ArrayList<Vector2> fogPositions1 = new ArrayList<Vector2>();
    ArrayList<Vector2> fogPositions2 = new ArrayList<Vector2>();
    Vector2 startPos;
    Vector2 endPos;
    int timer = 0;
//    Vector3 worldCoordsTL;
//    Vector3 worldCoordsBR;
    Color prevColor;
    Vector2 currOffset = new Vector2();
    Pixmap pixmap;
    Texture texture;
    Vector2 prevPos = new Vector2();
    
    public FogEffect() {
        this.fogTexture1 = TextureCache.get(Gdx.files.internal("fog1.png"));
        this.fogTexture2 = TextureCache.get(Gdx.files.internal("fog2.png"));
//        this.shadeTexture1 = TextureCache.get(Gdx.files.internal("shade3.png"));
//        this.shadeTexture2 = TextureCache.get(Gdx.files.internal("shade4.png"));
        this.shadeTexture1 = TextureCache.get(Gdx.files.internal("shade5.png"));
        this.shadeTexture2 = TextureCache.get(Gdx.files.internal("shade6.png"));
        TextureData temp = this.shadeTexture1.getTextureData();
        if (!temp.isPrepared()) {
            temp.prepare();
        }
        this.shadePixmap1 = temp.consumePixmap();
        temp = this.shadeTexture2.getTextureData();
        if (!temp.isPrepared()) {
            temp.prepare();
        }
        this.shadePixmap2 = temp.consumePixmap();

        this.pixmap = new Pixmap(160*4, 144*3, Pixmap.Format.RGBA8888);
        this.pixmap.setColor(new Color(1f, 1f, 1f, 0f));
        this.texture = TextureCache.get(this.pixmap);
    }
    
    @Override
    public String getCamera() {
        return "map";
    }

    @Override
    public int getLayer() {
        return 0;
    }

    @Override
    public void firstStep(Game game) {
//        game.insertAction(this.new LightenScreen());
    }


    @Override
    public void step(Game game) {
        if (!FogEffect.active ||
            game.map.timeOfDay.equals("night") ||
            game.map.tiles != game.map.overworldTiles) {
            return;
        }
        
        // TODO: trying out a 'deep forest' overlay

        // TODO: probably a better way
        if (FogEffect.refresh) {
            this.pixmap.fill();
            this.prevPos.set(game.player.position.x, game.player.position.y);
            FogEffect.refresh = false;
            this.fogPositions1.clear();
            this.fogPositions2.clear();
            //
            this.startPos = game.player.position.cpy().add(-64*6, -64*5);
            this.startPos.x -= this.startPos.x % 16;
            this.startPos.y -= this.startPos.y % 16;
            this.endPos = game.player.position.cpy().add(64*6, 64*5);
            this.endPos.x -= this.endPos.x % 16;
            this.endPos.y -= this.endPos.y % 16;
            int scale = 12;  // 16 looked pretty good too
            Vector2 rotate;
            int offsetX, offsetY, offset1, offset2;
            //
            for (Vector2 currPos = this.startPos.cpy(); currPos.y <= this.endPos.y;) {
                // 
                if (currPos.x/4 % 2 == currPos.y/8 % 2) {
//                if (currPos.x/8 % 2 == 0) {
                    rotate = currPos.cpy().sub(this.currOffset).rotate(0);
                    offsetY = ((int)Math.abs(rotate.y/scale) %14) -7;
                    offsetX = (((int)Math.abs(rotate.x/scale)) %14) -7;  //  - rotate.y/scale
                    offsetX = Math.abs(offsetX);
                    offsetY = Math.abs(offsetY);
                    offset1 = offsetX +offsetY;
                    offsetY = Math.abs(((int)Math.abs(rotate.y/scale) %20) -10);
                    offsetX = Math.abs((((int)Math.abs(rotate.x/scale)) %20) -10);
                    offset2 = offsetX +offsetY;
                    offset2 = offset2/5;
                    if (offset1*offset2 > 7) {
                        this.fogPositions1.add(currPos.cpy());
                    }
                    else if (offset1*offset2 > 4) {
                        this.fogPositions2.add(currPos.cpy());
                    }
                }

                currPos.x += 4;
                if (currPos.x > endPos.x) {
                    currPos.x = startPos.x;
                    currPos.y += 8;
                }
            }

            for (Vector2 currPos : this.fogPositions1) {
                this.pixmap.drawPixmap(this.shadePixmap1, (int)(currPos.x -this.startPos.x), (int)(currPos.y -this.startPos.y));
            }
            for (Vector2 currPos : this.fogPositions2) {
                this.pixmap.drawPixmap(this.shadePixmap2, (int)(currPos.x -this.startPos.x), (int)(currPos.y -this.startPos.y));
            }
        }
        //
        Color tempColor = game.mapBatch.getColor();
        game.mapBatch.setColor(new Color(1f, 1f, 1f, .4f));
        this.texture.draw(this.pixmap, 0, 0);
        game.mapBatch.draw(this.texture, this.prevPos.x +8 -320, this.prevPos.y +8 -216);
        game.mapBatch.setColor(tempColor);
    }

    public void oldStep(Game game) {
        if (!FogEffect.active ||
            game.map.timeOfDay.equals("night") ||
            game.map.tiles != game.map.overworldTiles) {
            return;
        }

        // TODO: probably a better way
        if (FogEffect.refresh) {
            FogEffect.refresh = false;
            this.fogPositions1.clear();
            this.fogPositions2.clear();
            //
            this.startPos = game.player.position.cpy().add(-64*6, -64*4);
            this.startPos.x -= this.startPos.x % 16;
            this.startPos.y -= this.startPos.y % 16;
            this.endPos = game.player.position.cpy().add(64*6, 64*4);
            this.endPos.x -= this.endPos.x % 16;
            this.endPos.y -= this.endPos.y % 16;
            int scale = 16;
            Vector2 rotate;
            int offsetX, offsetY, offset1, offset2;
            //
            for (Vector2 currPos = this.startPos.cpy(); currPos.y <= this.endPos.y;) {
                // 
                if (currPos.x/8 % 2 == currPos.y/16 % 2) {
                    rotate = currPos.cpy().sub(this.currOffset).rotate(0);
                    offsetY = ((int)Math.abs(rotate.y/scale) %14) -7;
                    offsetX = (((int)Math.abs(rotate.x/scale - rotate.y/scale)) %14) -7;
                    offsetX = Math.abs(offsetX);
                    offsetY = Math.abs(offsetY);
                    offset1 = offsetX +offsetY;
                    offsetY = Math.abs(((int)Math.abs(rotate.y/scale) %20) -10);
                    offsetX = Math.abs((((int)Math.abs(rotate.x/scale)) %20) -10);
                    offset2 = offsetX +offsetY;
                    offset2 = offset2/5;
//                    if (offset1*offset2 > 10) {
//                        if (Game.rand.nextInt(3) == 0) {
//                            this.fogPositions2.add(currPos.cpy().add(Game.rand.nextInt(3)-1, Game.rand.nextInt(3)-1));
//                        }
//                        else {
//                            this.fogPositions1.add(currPos.cpy().add(Game.rand.nextInt(3)-1, Game.rand.nextInt(3)-1));
//                        }
//                    }
//                    else 
                    if (offset1*offset2 > 9) {
                        this.fogPositions2.add(currPos.cpy().add((Game.rand.nextInt(3)-1), (Game.rand.nextInt(3)-1)));
                    }
                    if (offset1*offset2 > 11) {
                        this.fogPositions1.add(currPos.cpy().add((Game.rand.nextInt(3)-1), (Game.rand.nextInt(3)-1)));
                    }
                    if (offset1*offset2 > 13) {
//                        if (Game.rand.nextInt(3) == 0) {
                            this.fogPositions1.add(currPos.cpy().add((Game.rand.nextInt(3)-1), (Game.rand.nextInt(3)-1)));
//                            this.fogPositions1.add(currPos.cpy().add((Game.rand.nextInt(3)-1), (Game.rand.nextInt(5)-2)));
//                            offset2 = 3;
//                            if (Game.rand.nextBoolean()) {
//                                offset2 = -3;
//                            }
                            this.fogPositions1.add(currPos.cpy().add((Game.rand.nextInt(3)-1), 4));
                            
//                            this.fogPositions1.add(currPos.cpy());
//                        }
//                        else {
//                            this.fogPositions2.add(currPos.cpy().add(Game.rand.nextInt(3)-1, Game.rand.nextInt(3)-1));
//                        }
                    }
                }

                currPos.x += 4;
                if (currPos.x > endPos.x) {
                    currPos.x = startPos.x;
                    currPos.y += 8;
                }
            }
        }

        this.prevColor = game.mapBatch.getColor();
//        game.mapBatch.setColor(1f, 1f, 1f, .3f);
        game.mapBatch.setColor(1f, 1f, 1f, .2f);
        this.timer++;
        for (Vector2 currPos : this.fogPositions1) {
            game.mapBatch.draw(this.fogTexture1, currPos.x +this.currOffset.x, currPos.y +this.currOffset.y);
        }
        for (Vector2 currPos : this.fogPositions2) {
            game.mapBatch.draw(this.fogTexture2, currPos.x +this.currOffset.x, currPos.y +this.currOffset.y);
        }
        if (this.timer >= 151) {
            this.currOffset.add(-2, 0);
            this.timer = 0;
        }
        game.mapBatch.setColor(this.prevColor);
    }
    
    
    /**
     * Could add movement. Looked pretty good.
     */
    public void coolCloudsEffect(Game game) {
        if (!FogEffect.active ||
            game.map.timeOfDay.equals("night") ||
            game.map.tiles != game.map.overworldTiles) {
            return;
        }
        
        // TODO: trying out a 'deep forest' overlay

        // TODO: probably a better way
        if (FogEffect.refresh) {
            this.pixmap.fill();
            this.prevPos.set(game.player.position.x, game.player.position.y);
            FogEffect.refresh = false;
            this.fogPositions1.clear();
            this.fogPositions2.clear();
            //
            this.startPos = game.player.position.cpy().add(-64*6, -64*4);
            this.startPos.x -= this.startPos.x % 16;
            this.startPos.y -= this.startPos.y % 16;
            this.endPos = game.player.position.cpy().add(64*6, 64*4);
            this.endPos.x -= this.endPos.x % 16;
            this.endPos.y -= this.endPos.y % 16;
            int scale = 16;
            Vector2 rotate;
            int offsetX, offsetY, offset1, offset2;
            //
            for (Vector2 currPos = this.startPos.cpy(); currPos.y <= this.endPos.y;) {
                // 
                if (currPos.x/8 % 2 == currPos.y/16 % 2) {
                    rotate = currPos.cpy().sub(this.currOffset).rotate(0);
                    offsetY = ((int)Math.abs(rotate.y/scale) %14) -7;
                    offsetX = (((int)Math.abs(rotate.x/scale - rotate.y/scale)) %14) -7;
                    offsetX = Math.abs(offsetX);
                    offsetY = Math.abs(offsetY);
                    offset1 = offsetX +offsetY;
                    offsetY = Math.abs(((int)Math.abs(rotate.y/scale) %20) -10);
                    offsetX = Math.abs((((int)Math.abs(rotate.x/scale)) %20) -10);
                    offset2 = offsetX +offsetY;
                    offset2 = offset2/5;
                    if (offset1*offset2 > 9) {
                        this.fogPositions2.add(currPos.cpy().add((Game.rand.nextInt(3)-1), (Game.rand.nextInt(3)-1)));
                    }
                    if (offset1*offset2 > 11) {
                        this.fogPositions1.add(currPos.cpy().add((Game.rand.nextInt(3)-1), (Game.rand.nextInt(3)-1)));
                    }
                    if (offset1*offset2 > 13) {
                        this.fogPositions1.add(currPos.cpy().add((Game.rand.nextInt(3)-1), (Game.rand.nextInt(3)-1)));
                        this.fogPositions1.add(currPos.cpy().add((Game.rand.nextInt(3)-1), 4));
                    }
                }

                currPos.x += 4;
                if (currPos.x > endPos.x) {
                    currPos.x = startPos.x;
                    currPos.y += 8;
                }
            }

            for (Vector2 currPos : this.fogPositions1) {
                this.pixmap.drawPixmap(this.shadePixmap1, (int)(currPos.x -this.startPos.x), (int)(currPos.y -this.startPos.y));
            }
            for (Vector2 currPos : this.fogPositions2) {
                this.pixmap.drawPixmap(this.shadePixmap2, (int)(currPos.x -this.startPos.x), (int)(currPos.y -this.startPos.y));
            }
        }
        //
        Color tempColor = game.mapBatch.getColor();
        game.mapBatch.setColor(new Color(1f, 1f, 1f, .4f));
        this.texture.draw(this.pixmap, 0, 0);
        game.mapBatch.draw(this.texture, this.prevPos.x +8 -320, this.prevPos.y +8 -216);
        game.mapBatch.setColor(tempColor);
    }

    public void coolSmokeEffect(Game game) {
        if (!FogEffect.active ||
            game.map.timeOfDay.equals("night") ||
            game.map.tiles != game.map.overworldTiles) {
            return;
        }
        
        // TODO: trying out a 'deep forest' overlay

        // TODO: probably a better way
        if (FogEffect.refresh) {
            FogEffect.refresh = false;
            this.fogPositions1.clear();
            this.fogPositions2.clear();
            //
            this.startPos = game.player.position.cpy().add(-64*6, -64*4);
            this.startPos.x -= this.startPos.x % 16;
            this.startPos.y -= this.startPos.y % 16;
            this.endPos = game.player.position.cpy().add(64*6, 64*4);
            this.endPos.x -= this.endPos.x % 16;
            this.endPos.y -= this.endPos.y % 16;
            int scale = 16;
            Vector2 rotate;
            int offsetX, offsetY, offset1, offset2;
            //
            for (Vector2 currPos = this.startPos.cpy(); currPos.y <= this.endPos.y;) {
                // 
                if (currPos.x/8 % 2 == currPos.y/16 % 2) {
                    rotate = currPos.cpy().sub(this.currOffset).rotate(0);
                    offsetY = ((int)Math.abs(rotate.y/scale) %14) -7;
                    offsetX = (((int)Math.abs(rotate.x/scale - rotate.y/scale)) %14) -7;
                    offsetX = Math.abs(offsetX);
                    offsetY = Math.abs(offsetY);
                    offset1 = offsetX +offsetY;
                    offsetY = Math.abs(((int)Math.abs(rotate.y/scale) %20) -10);
                    offsetX = Math.abs((((int)Math.abs(rotate.x/scale)) %20) -10);
                    offset2 = offsetX +offsetY;
                    offset2 = offset2/5;
                    if (offset1*offset2 > 9) {
                        this.fogPositions2.add(currPos.cpy().add((Game.rand.nextInt(3)-1), (Game.rand.nextInt(3)-1)));
                    }
                    if (offset1*offset2 > 11) {
                        this.fogPositions1.add(currPos.cpy().add((Game.rand.nextInt(3)-1), (Game.rand.nextInt(3)-1)));
                    }
                    if (offset1*offset2 > 13) {
                        this.fogPositions1.add(currPos.cpy().add((Game.rand.nextInt(3)-1), (Game.rand.nextInt(3)-1)));
                        this.fogPositions1.add(currPos.cpy().add((Game.rand.nextInt(3)-1), 4));
                    }
                }

                currPos.x += 4;
                if (currPos.x > endPos.x) {
                    currPos.x = startPos.x;
                    currPos.y += 8;
                }
            }
        }

        this.prevColor = game.mapBatch.getColor();
        game.mapBatch.setColor(1f, 1f, 1f, .2f);
        this.timer++;
        for (Vector2 currPos : this.fogPositions1) {
            game.mapBatch.draw(this.shadeTexture1, currPos.x +this.currOffset.x, currPos.y +this.currOffset.y);
        }
        for (Vector2 currPos : this.fogPositions2) {
            game.mapBatch.draw(this.shadeTexture2, currPos.x +this.currOffset.x, currPos.y +this.currOffset.y);
        }
        if (this.timer >= 151) {
            this.currOffset.add(-2, 0);
            this.timer = 0;
        }
        game.mapBatch.setColor(this.prevColor);
    }
    

    /**
     * I'm sure there's an opengl way to do this but I couldn't figure it out.
     */
    class LightenScreen extends Action {
        Texture texture;
        Color prevColor;
        
        public LightenScreen() {
            this.texture = TextureCache.get(Gdx.files.internal("battle/intro_frame6.png"));
        }

        @Override
        public String getCamera() {
            return "gui";
        }

        @Override
        public int getLayer() {
            return 0;
        }
        
        @Override
        public void step(Game game) {
            if (!FogEffect.active || game.map.timeOfDay.equals("night")) {
                return;
            }
            this.prevColor = game.mapBatch.getColor();
            game.uiBatch.setColor(1f, 1f, 1f, .2f);
            for (int i = -1; i < 1; i+= 1) {
                for (int j = -1; j < 1; j+= 1) {
                    game.uiBatch.draw(this.texture, 256*i, 256*j);
                }
            }
            game.uiBatch.setColor(this.prevColor);
        }
    }
}





