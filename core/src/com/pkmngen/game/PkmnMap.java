package com.pkmngen.game;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.kryo.io.Output;
import com.pkmngen.game.util.LinkedMusic;
import com.pkmngen.game.util.TextureCache;

import box2dLight.PointLight;

// grass -
// part of player's head is above grass,
// part is beneath
// just use two draw actions
// likely need to mark tile as 'grass',
// for wild encounters (not positive tho)
class DrawMap extends Action {
    public int layer = 140;
    Sprite blankSprite;
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

    public DrawMap(Game game) {
        this.pixels = new Pixmap(Gdx.files.internal("tiles/blank2.png"));
        this.texture = new Texture(this.pixels);
        this.blankSprite = new Sprite(TextureCache.get(Gdx.files.internal("tiles/blank2.png")), 0, 0, 16, 16);
        this.zSprite = new Sprite(TextureCache.get(Gdx.files.internal("tiles/zs1.png")), 0, 0, 16, 16);
    }

    public int getLayer() {
        return this.layer;
    }

    @Override
    public void step(Game game) {
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
        worldCoordsBR = game.cam.unproject(new Vector3(game.currScreen.x, game.currScreen.y+128, 0f));
        worldCoordsTL.x = (int)worldCoordsTL.x - (int)worldCoordsTL.x % 16;
        worldCoordsTL.y = (int)worldCoordsTL.y - (int)worldCoordsTL.y % 16;
        worldCoordsBR.x = (int)worldCoordsBR.x - (int)worldCoordsBR.x % 16;
        worldCoordsBR.y = (int)worldCoordsBR.y - (int)worldCoordsBR.y % 16;
//        System.out.println(game.viewport.getScreenWidth());
//        System.out.println(game.viewport.getScreenHeight());
        this.startPos = worldCoordsTL; // new Vector3(worldCoordsTL.x, worldCoordsTL.y, 0f);
        this.endPos = worldCoordsBR; // new Vector3(worldCoordsBR.x, worldCoordsBR.y, 0f);

        int numTiles = 0;
        // TODO: debug, remove
//        if (Gdx.input.isKeyPressed(Input.Keys.P)) {
//            System.out.println("Start draw tiles");
//            System.out.println(java.time.LocalTime.now());
//        }

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
            numTiles++;
//          for (Tile tile : game.map.tiles.values()) {
//            // Don't draw sprites if not in camera view
//            // note - when zoomed out, game will lag
            if (!game.cam.frustum.pointInFrustum(tile.position.x, tile.position.y, game.cam.position.z) &&
                    !game.cam.frustum.pointInFrustum(tile.position.x+tile.sprite.getWidth(), tile.position.y+tile.sprite.getHeight()+16, game.cam.position.z) &&
                    !game.cam.frustum.pointInFrustum(tile.position.x+tile.sprite.getWidth(), tile.position.y, game.cam.position.z) &&
                    !game.cam.frustum.pointInFrustum(tile.position.x, tile.position.y+tile.sprite.getHeight()+16, game.cam.position.z)) {
               continue;
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

            game.mapBatch.draw(tile.sprite, tile.sprite.getX(), tile.sprite.getY());
            // TODO: might cause performance issue
            if (tile.nameUpper.contains("stairs")) {
                game.mapBatch.draw(tile.overSprite, tile.overSprite.getX(), tile.overSprite.getY());
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
    Tile tile;

    public DrawMapGrass(Game game) {}

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
                    !game.cam.frustum.pointInFrustum(tile.position.x+tile.sprite.getWidth(), tile.position.y+tile.sprite.getHeight()+16, game.cam.position.z) &&
                    !game.cam.frustum.pointInFrustum(tile.position.x+tile.sprite.getWidth(), tile.position.y, game.cam.position.z) &&
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

            if (tile.overSprite != null && !tile.nameUpper.contains("stairs")) {
                game.mapBatch.draw(tile.overSprite, tile.overSprite.getX(), tile.overSprite.getY());
            }
        }

    }

}

/*
 * Draw tops of some trees over the player.
 *
 * TODO: this isn't working for some reason.
 */
class DrawMapTrees extends Action {
    public int layer = 110;
    Vector3 startPos;
    Vector3 endPos;
    Vector3 worldCoordsTL;
    Vector3 worldCoordsBR;
    Tile tile;
    public DrawMapTrees(Game game) {
    }

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
            // TODO: only do for subset of trees?
            if ((tile.name.contains("tree") || tile.nameUpper.contains("tree")) && tile.overSprite != null) {
                game.mapBatch.draw(tile.overSprite, tile.sprite.getX(), tile.sprite.getY());
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
                                     new PlaySound(game.battle.oppPokemon.name,
                                     new DisplayText(game, "Wild "+ game.battle.oppPokemon.name.toUpperCase()+ " appeared!",
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

// TODO: shader method
class EnterBuilding extends Action {
    Sprite sprite;
    String action;
    Action nextAction;
    Map<Vector2, Tile> whichTiles;

    public int layer = 114;  // TODO: check
    int timer = 0;

    int slow = 1;  // TODO: remove, use some sort of into anim;

    public EnterBuilding(Game game, Action nextAction) {
        this(game, "enter", nextAction);
    }

    public EnterBuilding(Game game, String action, Action nextAction) {
        this(game, action, null, nextAction);
    }
    public EnterBuilding(Game game, String action, Map<Vector2, Tile> whichTiles, Action nextAction) {
        this.whichTiles = whichTiles;
        this.nextAction = nextAction;
        this.action = action;
        // fade out from white anim
        Texture text1 = TextureCache.get(Gdx.files.internal("battle/intro_frame6.png"));
        this.sprite = new Sprite(text1);
        this.sprite.setPosition(0,0);
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
                if (this.whichTiles != null) {
                    game.map.tiles = this.whichTiles;
                }
                else if (this.action.equals("enter")) {
                    game.map.tiles = game.map.interiorTiles.get(game.map.interiorTilesIndex);
                }
                else if (this.action.equals("exit")){
                    game.map.tiles = game.map.overworldTiles;
                }
                if (this.action.equals("enter")) {
                    Gdx.gl.glClearColor(0, 0, 0, 1);
                }
                else {
                    Gdx.gl.glClearColor(1, 1, 1, 1);
                }

                // Fade music if required
                Route newRoute = game.map.tiles.get(game.player.position).routeBelongsTo;
                if (newRoute != null && !newRoute.name.equals(game.map.currRoute.name) && (newRoute.transitionMusic != game.map.currRoute.transitionMusic)) {
                    String nextMusicName = newRoute.getNextMusic(false);
                    Action nextMusic = new FadeMusic("currMusic", "out", "", .025f,
                                       new WaitFrames(game, 10,
                                       null));
                    if (newRoute.name.contains("pkmnmansion")) {
                        game.mapBatch.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
                        if (!game.loadedMusic.containsKey(nextMusicName)) {
                            Music temp = new LinkedMusic("music/"+nextMusicName, "");
                            temp.setVolume(0.1f);
                            game.loadedMusic.put(nextMusicName, temp);
                        }
                        game.loadedMusic.get(nextMusicName).stop();
                        nextMusic.append(// TODO: this didn't really work, still doesn't loop
                                         new FadeMusic(nextMusicName, "in", "", .2f, true, 1f, null,
                                         new CallMethod(game.loadedMusic.get(nextMusicName), "setLooping", new Object[]{true},
                                         null)));
                    }
                    else {
                        game.mapBatch.setColor(new Color(1f, 1f, 1f, 1f));
                        nextMusic.append(new FadeMusic(nextMusicName, "in", "", .2f, true, 1f, game.musicCompletionListener, null));
                    }
                    game.insertAction(nextMusic);
                    nextMusic.step(game);
                    game.fadeMusicAction = nextMusic;
                    game.map.currRoute = newRoute;
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

// TODO - bug where grass tiles move out of sync with normal
// probably because drawmap is before a camera update, drawGrass is after

// TODO: this is doubling as animating campfire
// moves water back and forth
// also - I am going to add a sprite below each grass here
// needed for sprite coloring
class MoveWater extends Action {
    public int layer = 110;

    ArrayList<Vector2> positions;

    Vector2 position;
    ArrayList<Integer> repeats;
    Sprite blankSprite;

    Pixmap pixels;
    Texture texture;
    Vector3 startPos;
    Vector3 endPos;
    Vector3 worldCoordsTL;
    Vector3 worldCoordsBR;
    Tile tile;
    Sprite[] campfireSprites;
    int campfireTimer = 0;
    PointLight pointLight;

    public MoveWater(Game game) {
        this.positions = new ArrayList<Vector2>();
        resetVars();
        Texture text = TextureCache.get(Gdx.files.internal("tiles/campfire1.png"));
        this.campfireSprites = new Sprite[4];
        this.campfireSprites[0] = new Sprite(text, 0,  0, 16, 20);
        this.campfireSprites[1] = new Sprite(text, 16, 0, 16, 20);
        text = TextureCache.get(Gdx.files.internal("fire_mask1.png"));
        this.campfireSprites[2] = new Sprite(text, 0,  0, 160, 144);
        text = TextureCache.get(Gdx.files.internal("fire_mask2.png"));
        this.campfireSprites[3] = new Sprite(text, 0, 0, 160, 144);

//        this.pointLight = new PointLight(game.rayHandler, 20, new Color(.3f,.2f,.1f,1), 2, -0, 0);
//        this.pointLight = new PointLight(game.rayHandler, 8, new Color(1f, .9f, .7f, 1), 5f, -0, 0);
//        this.pointLight = new PointLight(game.rayHandler, 16, new Color(.8f, .7f, .6f, 1), 5f, -0, 0);
//        this.pointLight.setPosition(0f, 0f);
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
        worldCoordsTL = game.cam.unproject(new Vector3(-256, -256, 0f));
        worldCoordsBR = game.cam.unproject(new Vector3(game.currScreen.x+256, game.currScreen.y+256, 0f));
        worldCoordsTL.x = (int)worldCoordsTL.x - (int)worldCoordsTL.x % 16;
        worldCoordsTL.y = (int)worldCoordsTL.y - (int)worldCoordsTL.y % 16;
        worldCoordsBR.x = (int)worldCoordsBR.x - (int)worldCoordsBR.x % 16;
        worldCoordsBR.y = (int)worldCoordsBR.y - (int)worldCoordsBR.y % 16;
        this.startPos = worldCoordsTL; // new Vector3(worldCoordsTL.x, worldCoordsTL.y, 0f);
        this.endPos = worldCoordsBR; // new Vector3(worldCoordsBR.x, worldCoordsBR.y, 0f);

        // set sprite position
        // if done with anim, do nextAction
        if (positions.isEmpty()) {
            resetVars();
            // game.actionStack.remove(this);
            // return;
        }

        this.position = positions.get(0);
        // positions.set(0,new Vector2(0,0));

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
//            if (!game.cam.frustum.pointInFrustum(tile.position.x, tile.position.y, game.cam.position.z) &&
//                    !game.cam.frustum.pointInFrustum(tile.position.x+tile.sprite.getWidth(), tile.position.y+tile.sprite.getHeight(), game.cam.position.z) &&
//                    !game.cam.frustum.pointInFrustum(tile.position.x+tile.sprite.getWidth(), tile.position.y, game.cam.position.z) &&
//                    !game.cam.frustum.pointInFrustum(tile.position.x, tile.position.y+tile.sprite.getHeight(), game.cam.position.z)) {
//               continue;
//            }
            // Texture text = new
            // Texture(Gdx.files.internal("tiles/water1_2.png"));
            if (tile.attrs.get("water") == true) {
                // tile.sprite.setPosition(tile.sprite.getX()+position.x,
                // tile.sprite.getY()+position.y);
                // tile.overSprite.draw(game.batch);
                tile.sprite.setRegionX((int) this.position.x);
                tile.sprite.setRegionWidth((int) tile.sprite.getWidth());
            }

            // animate campfires
            if (tile.nameUpper.equals("campfire1")) {
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

                if (this.campfireTimer % 20 < 10 && game.mapBatch.getColor().r < .5f) {
                    Color tempColor = game.mapBatch.getColor();
                    game.mapBatch.setColor(new Color(1f, 1f, 1f, 1f));
                    // no idea how/why this works
                    int temp1 = game.mapBatch.getBlendSrcFunc();
                    int temp2 = game.mapBatch.getBlendDstFunc();
                    game.mapBatch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_SRC_ALPHA);
                    this.campfireSprites[2].setPosition(tile.overSprite.getX()+(tile.overSprite.getWidth()/2f)-(this.campfireSprites[2].getWidth()/2f), tile.overSprite.getY()+(tile.overSprite.getHeight()/2f)-(this.campfireSprites[2].getHeight()/2f));
                    // looked better with 'double-exposure'
                    game.mapBatch.draw(this.campfireSprites[2], this.campfireSprites[2].getX(), this.campfireSprites[2].getY());
                    game.mapBatch.draw(this.campfireSprites[2], this.campfireSprites[2].getX(), this.campfireSprites[2].getY());
                    game.mapBatch.draw(this.campfireSprites[2], this.campfireSprites[2].getX(), this.campfireSprites[2].getY());
                    game.mapBatch.setBlendFunction(temp1, temp2);
                    tile.overSprite.draw(game.mapBatch);
                    game.mapBatch.setColor(tempColor);
                }
                else if (this.campfireTimer % 20 < 20 && game.mapBatch.getColor().r < .5f) {
                    Color tempColor = game.mapBatch.getColor();
                    game.mapBatch.setColor(new Color(1f, 1f, 1f, 1f));
                    // no idea how/why this works
                    int temp1 = game.mapBatch.getBlendSrcFunc();
                    int temp2 = game.mapBatch.getBlendDstFunc();
                    game.mapBatch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_SRC_ALPHA);
                    this.campfireSprites[3].setPosition(tile.overSprite.getX()+(tile.overSprite.getWidth()/2f)-(this.campfireSprites[3].getWidth()/2f), tile.overSprite.getY()+(tile.overSprite.getHeight()/2f)-(this.campfireSprites[3].getHeight()/2f));
                    // looked better with 'double-exposure'
                    game.mapBatch.draw(this.campfireSprites[3], this.campfireSprites[3].getX(), this.campfireSprites[3].getY());
                    game.mapBatch.draw(this.campfireSprites[3], this.campfireSprites[3].getX(), this.campfireSprites[3].getY());
                    game.mapBatch.draw(this.campfireSprites[3], this.campfireSprites[3].getX(), this.campfireSprites[3].getY());
                    game.mapBatch.setBlendFunction(temp1, temp2);
                    tile.overSprite.draw(game.mapBatch);
                    game.mapBatch.setColor(tempColor);
                }

            }
        }
        if (this.campfireTimer < 79) {
            this.campfireTimer++;
        }
        else {
            this.campfireTimer = 0;
        }
        // repeat sprite/pos for current object for 'frames[0]' number of
        // frames.
        if (this.repeats.get(0) > 0) {
            this.repeats.set(0, this.repeats.get(0) - 1);
        } else {
            // since position is relative, only update once each time period
            // this.position = this.position.add(positions.get(0));
            positions.remove(0);
            repeats.remove(0);
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

    // use this to drop the tops of trees over the player
    //  hopefully makes drawing take less time
//    Map<Vector2, Tile> trees = new HashMap<Vector2, Tile>();

    // Used to know where to spawn new players
    ArrayList<Vector2> edges = new ArrayList<Vector2>();
    // routes on map
    ArrayList<Route> routes;
    // debating whether I should just make tiles have references
    // to route objects, and not have currRoute here
    Route currRoute;
    String currBiome = "";
    // needed for wild encounters etc
    Random rand;
    String timeOfDay = "Day";  // used by cycleDayNight
    String id;  // needed for saving to file
    TrainerTipsTile unownSpawn;
    ArrayList<String> unownUsed = new ArrayList<String>();
    {
        char[] textArray = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        for (int i=0; i < textArray.length; i++) {
            unownUsed.add(String.valueOf(textArray[i]));
        }
    }

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

    public void loadFromFile(Game game) {
        // If map exists as file, load it
        try {
            // TODO: tile Route is not saved here.
            // Should be able to serialize route pokemon and save. Although might be ineffecient to save all
            //  pokemon details.
            InputStream inputStream = new InflaterInputStream(new FileInputStream(this.id + ".sav"));
            com.esotericsoftware.kryo.io.Input input = new com.esotericsoftware.kryo.io.Input(inputStream);
//            com.esotericsoftware.kryo.io.Input input = new com.esotericsoftware.kryo.io.Input(new FileInputStream(this.id + ".sav"));  // uncompressed
            
            Network.SaveData saveData = game.server.getKryo().readObject(input, Network.SaveData.class);
            input.close();
            this.tiles.clear();
            HashMap<String, Route> loadedRoutes = new HashMap<String, Route>();
            for (Network.TileData tileData : saveData.mapTiles.tiles) {
                // store unique routes as hashmap ClassID->Route
                if (tileData.routeBelongsTo != null && !loadedRoutes.containsKey(tileData.routeBelongsTo)) {
                    loadedRoutes.put(tileData.routeBelongsTo, new Route(saveData.mapTiles.routes.get(tileData.routeBelongsTo)));
                }
                Route tempRoute = loadedRoutes.get(tileData.routeBelongsTo);
                this.tiles.put(tileData.pos.cpy(), Tile.get(tileData, tempRoute));
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
            for (Network.PlayerData playerData : saveData.players) {
                Player player = new Player(playerData);
                player.type = Player.Type.REMOTE;  // TODO: store in playerData?
                game.players.put(playerData.id, player);
            }
            // Load game.player
            game.player = new Player(saveData.playerData);
            game.cam.position.set(game.player.position.x+16, game.player.position.y, 0);
            game.player.type = Player.Type.LOCAL;
            if (saveData.playerData.isInterior) {
                game.map.tiles = game.map.interiorTiles.get(game.map.interiorTilesIndex);
            }
            // Load overworld pokemon
            game.map.pokemon.clear();
            for (Vector2 pos : saveData.overworldPokemon.keySet()) {
                Network.PokemonData pokemonData = saveData.overworldPokemon.get(pos);
                Pokemon pokemon = new Pokemon(pokemonData);
                System.out.println(pokemon.mapTiles == game.map.overworldTiles);
                pokemon.position = pos.cpy();
                game.map.pokemon.put(pos, pokemon);
                game.insertAction(pokemon.new Standing());
            }
            
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

        } catch (FileNotFoundException e) {
            System.out.println("No save file found for map: " + this.id);
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
            // TODO: this only works for server atm, because I'm using game.server.getKryo() below.
            // Could change to also use game.server.getKryo() if I need for client.
            if (game.type == Game.Type.CLIENT) {
                return;
            }
            this.timeDelta += Gdx.graphics.getDeltaTime();
            if (this.timeDelta >= this.saveInterval) {
                this.timeDelta = 0f;
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
                                                        game.battle.oppPokemon.name,
                                                        new DisplayText(
                                                                game,
                                                                "Wild "
                                                                        + game.battle.oppPokemon.name
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

    // multiple entries to increase encounter rate
    ArrayList<Pokemon> pokemon = new ArrayList<Pokemon>();

    // pokemon list to pick from when populating route
    ArrayList<String> allowedPokemon;

    // try this for now?
    Music music;
    ArrayList<String> musics = new ArrayList<String>();
    int musicsIndex = 0;
    
    boolean transitionMusic = false;  // some routes require music transition. ie, cave, pokemon mansion

    // TODO: remove
//    Random rand;

    /*
     * Constructor for loading from file or loading over network.
     */
    public Route(Network.RouteData routeData) {
//        this.rand = new Random();  // TODO: delete, should just use single random num generator which can be seeded.
        this.name = routeData.name;
        this.level = routeData.level;
        this.allowedPokemon = new ArrayList<String>(routeData.allowedPokemon);
        this.musics = new ArrayList<String>(routeData.musics);
        this.musicsIndex = routeData.musicsIndex;
        for (Network.PokemonData pokemonData : routeData.pokemon) {
            this.pokemon.add(new Pokemon(pokemonData));
        }
        if (name.equals("pkmnmansion1")) {
            this.transitionMusic = true;
        }
    }

    public Route(String name, int level) {
        this.name = name;
        this.level = level;

        this.pokemon = new ArrayList<Pokemon>();
        this.allowedPokemon = new ArrayList<String>();
        
        // TODO: possibly different per-route
        this.musics.add("nature1_render");
        this.musics.add("overw3");
        this.musics.add("route_42");
        this.musics.add("national_park1");
        this.musics.add("viridian_forest_gs");
        this.musics.add("route_3_gs");
        this.musics.add("route_1");
        this.musics.add("route_idk1");

//        this.rand = new Random();

        if (name == "Route 1") {
            this.allowedPokemon.add("Electabuzz");
            this.allowedPokemon.add("Steelix");
            this.allowedPokemon.add("Tauros");
            this.allowedPokemon.add("Makuhita");
            this.allowedPokemon.add("Hariyama");
            this.allowedPokemon.add("Mareep");
            this.allowedPokemon.add("Scyther");

            // TODO - generate random pokemon based off of this.allowedPokemon
            // factor in route level
            // this.pokemon.add(new Pokemon("Electabuzz", 20)); // 2
            // this.pokemon.add(new Pokemon("Steelix", 21)); // 3
            // this.pokemon.add(new Pokemon("Tauros", 22));

            // TODO: remove
//            this.music = Gdx.audio.newMusic(Gdx.files.internal("route1_1.ogg"));
//            this.music.setLooping(true);
//            this.music.setVolume(.3f);
        }
        else if (name == "Demo_DarkRoute") {
            // ie sneasel, etc

            this.allowedPokemon.add("Shuppet");
            this.allowedPokemon.add("Cacnea");
            this.allowedPokemon.add("Spinarak");
            this.allowedPokemon.add("Oddish");
            this.allowedPokemon.add("Gloom");
            this.allowedPokemon.add("Sneasel");
            this.allowedPokemon.add("Sableye");

            // TODO: remove
//            this.music = Gdx.audio.newMusic(Gdx.files.internal("route1_1.ogg"));
//            this.music.setLooping(true);
//            this.music.setVolume(.3f);
        }
        else if (name == "Demo_SteelRoute") {
            // ie metagross, etc

            this.allowedPokemon.add("Makuhita");
            this.allowedPokemon.add("Hariyama");
            this.allowedPokemon.add("Shinx");
//            this.allowedPokemon.add("Starly");
            this.allowedPokemon.add("Zubat");
            this.allowedPokemon.add("Lairon");
            this.allowedPokemon.add("Steelix");

            // TODO: remove
//            this.music = Gdx.audio.newMusic(Gdx.files.internal("route1_1.ogg"));
//            this.music.setLooping(true);
//            this.music.setVolume(.3f);
        }
        else if (name == "Demo_PsychicRoute") {
            // ie metagross, etc

            this.allowedPokemon.add("Wurmple");
            this.allowedPokemon.add("Mareep");
            this.allowedPokemon.add("Flaaffy");
            this.allowedPokemon.add("Shinx");
            this.allowedPokemon.add("Starly");
            this.allowedPokemon.add("Gardevoir");
            this.allowedPokemon.add("Claydol");

            // TODO: remove
//            this.music = Gdx.audio.newMusic(Gdx.files.internal("route1_1.ogg"));
//            this.music.setLooping(true);
//            this.music.setVolume(.3f);
        }
        else if (name.equals("mountain1")) {
            this.allowedPokemon.add("rhydon");
            this.allowedPokemon.add("rhyhorn");
            this.allowedPokemon.add("onix");
            this.allowedPokemon.add("machop");
            this.allowedPokemon.add("solrock");
            // TODO: remove
//            this.music = Gdx.audio.newMusic(Gdx.files.internal("route1_1.ogg"));
//            this.music.setLooping(true);
//            this.music.setVolume(.3f);
        }
        else if (name.equals("deep_forest")) {
            // TODO: this is just an idea
            // zubat, houndour, murkrow, nidorana/o, scyther, pinsir, stantler, tangela, teddiursa, weepinbell, 
            // absol, growlithe, vulpix,
          this.allowedPokemon.add("zubat");
          this.allowedPokemon.add("houndour");
          this.allowedPokemon.add("stantler");
          this.allowedPokemon.add("murkrow");
          this.allowedPokemon.add("nidorina");
          this.allowedPokemon.add("nidorino");
          this.allowedPokemon.add("scyther");
          this.allowedPokemon.add("pinsir");
          this.allowedPokemon.add("growlithe");
          this.allowedPokemon.add("vulpix");
        }
        else if (name.equals("forest1")) {
            this.allowedPokemon.add("oddish");
//            this.allowedPokemon.add("gloom");  // TODO: remove, no evos.
            this.allowedPokemon.add("pidgey");
            this.allowedPokemon.add("spearow");
            this.allowedPokemon.add("hoppip");
//            this.allowedPokemon.add("machop");
            this.allowedPokemon.add("bulbasaur");
            this.allowedPokemon.add("charmander");
            this.allowedPokemon.add("chikorita");
            this.allowedPokemon.add("paras");
            this.allowedPokemon.add("pikachu");
            this.allowedPokemon.add("weedle");
            this.allowedPokemon.add("caterpie");
            this.allowedPokemon.add("spinarak");
            this.allowedPokemon.add("ledyba");
            this.allowedPokemon.add("hoothoot");
            this.allowedPokemon.add("mankey");
            for (int i = 0; i < 2; i++) {
                this.allowedPokemon.add("bulbasaur");
                this.allowedPokemon.add("charmander");
                this.allowedPokemon.add("chikorita");
            }
            // TODO: remove
//            this.music = Gdx.audio.newMusic(Gdx.files.internal("route1_1.ogg"));
//            this.music.setLooping(true);
//            this.music.setVolume(.3f);
        }
        else if (name.equals("savanna1")) {
            this.allowedPokemon.add("tauros");
            this.allowedPokemon.add("miltank");
            for (int i = 0; i < 2; i++) {
                this.allowedPokemon.add("oddish");
                this.allowedPokemon.add("chikorita");
                this.allowedPokemon.add("bulbasaur");
                this.allowedPokemon.add("hoppip");
                this.allowedPokemon.add("pidgey");
//                for (int j = 0; j < 2; j++) {
                this.allowedPokemon.add("charmander");
                this.allowedPokemon.add("cyndaquil");
//                }
                this.allowedPokemon.add("mareep");
                this.allowedPokemon.add("ekans");
                this.allowedPokemon.add("doduo");
                this.allowedPokemon.add("sentret");
                this.allowedPokemon.add("rattata");
            }
            // feels like it's getting too diluted
//            this.allowedPokemon.add("snubbul");
//            this.allowedPokemon.add("meowth");
            // nidoran, murkro, natu, 

            // TODO: might need savanna2 biome, which has it's own dedicated blotch area
//            this.allowedPokemon.add("kangaskan");  // only if high-level?
//            this.allowedPokemon.add("scyther");  // only if high-level?
//            this.allowedPokemon.add("pinsir");  // only if high-level?
//            this.allowedPokemon.add("lickitung");  // only if high-level?
//            this.allowedPokemon.add("drowzee");
//            this.allowedPokemon.add("eevee");  // TODO: after evolutionary stones added
        }
        else if (name.equals("beach1")) {
            this.allowedPokemon.add("squirtle");
            this.allowedPokemon.add("krabby");
            this.allowedPokemon.add("totodile");
            this.allowedPokemon.add("shellder");
            this.allowedPokemon.add("wooper");
            this.allowedPokemon.add("shuckle");
            this.allowedPokemon.add("staryu");
            this.allowedPokemon.add("marill");
        }
        else if (name.equals("desert1")) {
            // TODO: these are just some ideas
            this.allowedPokemon.add("gible");
            this.allowedPokemon.add("gabite");
            // TODO: would be sweet if fully evolved forms were hard to escape from and catch
            //  ie, they were scary to run into.
            //  probably just make them high level.
            this.allowedPokemon.add("garchomp");   // high-level
            this.allowedPokemon.add("shieldon");
            this.allowedPokemon.add("cacnea");
            this.allowedPokemon.add("sandshrew");
            this.allowedPokemon.add("kangaskan");
            this.allowedPokemon.add("rhyhorn");
            this.allowedPokemon.add("shieldon");
            this.allowedPokemon.add("bastiodon");  // high-level
            this.allowedPokemon.add("skorupi");
            this.allowedPokemon.add("drapion");    // high-level
            this.allowedPokemon.add("trapinch");
        }
        else if (name.equals("snow1")) {
            this.allowedPokemon.add("larvitar");
            this.allowedPokemon.add("sneasel");
            this.allowedPokemon.add("weavile");
            this.allowedPokemon.add("snorunt");
            this.allowedPokemon.add("glalie");
            this.allowedPokemon.add("lairon");
            this.allowedPokemon.add("swinub");
            this.allowedPokemon.add("piloswine");
            // TODO: remove
//            this.music = Gdx.audio.newMusic(Gdx.files.internal("route1_1.ogg"));
//            this.music.setLooping(true);
//            this.music.setVolume(.3f);
        }
        else if (this.name.equals("pkmnmansion1")) {
            this.allowedPokemon.add("magmar");
            this.allowedPokemon.add("grimer");
            this.allowedPokemon.add("rattata");
            this.allowedPokemon.add("koffing");
            this.allowedPokemon.add("vulpix");
            this.allowedPokemon.add("growlithe");
            this.allowedPokemon.add("ponyta");
            this.allowedPokemon.add("ditto");
            this.transitionMusic = true;
            this.musics.clear();
//            this.musics.add("pkmnmansion1");  // gbs version (didn't work)
            this.musics.add("pkmnmansion");
            this.musics.add("pkmnmansion");
        }
        else if (name.equals("")) {
            this.allowedPokemon.clear();
            // TODO: remove
//            this.music = Gdx.audio.newMusic(Gdx.files.internal("route1_1.ogg"));
//            this.music.setLooping(true);
//            this.music.setVolume(.3f);
        }
        else {
            this.allowedPokemon.clear();
//            this.pokemon.add(new Pokemon("kangaskhan", 22, Pokemon.Generation.CRYSTAL));
//            this.pokemon.add(new Pokemon("togekiss", 22, Pokemon.Generation.CRYSTAL));
            this.pokemon.add(new Pokemon("rhydon", 22, Pokemon.Generation.CRYSTAL));
            this.pokemon.add(new Pokemon("rhyhorn", 22, Pokemon.Generation.CRYSTAL));
            this.pokemon.add(new Pokemon("onix", 22, Pokemon.Generation.CRYSTAL));
            this.pokemon.add(new Pokemon("machop", 22, Pokemon.Generation.CRYSTAL));
            this.allowedPokemon.add("mamoswine");
            this.allowedPokemon.add("weavile");
            this.allowedPokemon.add("magmortar");
            this.allowedPokemon.add("electivire");
            this.allowedPokemon.add("metagross");
            // TODO: remove
//            this.music = Gdx.audio.newMusic(Gdx.files.internal("route1_1.ogg"));
//            this.music.setLooping(true);
//            this.music.setVolume(.3f);
        }
        genPokemon(256);

        // TODO: victory road theme thing

        // TODO: mountain musics
        //  - ruins of alps theme?

        // TODO: debug, delete
//        this.pokemon.clear();
//        Pokemon debug = new Pokemon("machamp", 22, Pokemon.Generation.CRYSTAL);  // 22
//        Pokemon debug = new Pokemon("garchompbeta", 70, Pokemon.Generation.CRYSTAL);  // 22
//        debug.attacks[0] = "toxic";
//        debug.attacks[1] = "sweet scent";
//        debug.attacks[2] = "thunder wave";
//        debug.attacks[3] = "sing";
//        this.pokemon.add(debug);

        /*
         * // below will add all from allowed pkmn for (String pokemonName :
         * this.allowedPokemon) { randomNum = rand.nextInt(3); // 0, 1, 2
         * this.pokemon.add(new Pokemon(pokemonName, this.level+randomNum)); }
         */
    }

    /*
     * This will bring the route pkmn count back to 4.
     *
     * TODO: probably should have more than 4 pokemon.
     */
    public void genPokemon(int maxCatchRate) {
        // TODO - bug if maxed out on catch rates, and need to repeat a pkmn

        // if less than needed pokemon for randomization, just use them all.
        if (this.allowedPokemon.size() < 4) {
            for (String pokemonName : this.allowedPokemon) {
                this.pokemon.add(new Pokemon(pokemonName,
                                             this.level + Game.rand.nextInt(3),
                                             Pokemon.Generation.CRYSTAL));
            }
            return;
        }

        int randomNum;
        int randomLevel;
        String pokemonName;
        ArrayList<String> usedPokemon = new ArrayList<String>();
        for (Pokemon pkmn : this.pokemon) {
            usedPokemon.add(pkmn.name);
        }
        // below will add from allowed pkmn based on catchRate
        while (this.pokemon.size() < 4) { // 4 total pokemon in route
            // TODO: debug, remove
//            System.out.println("debug:");
//            System.out.println(this.allowedPokemon);
//            System.out.println(Game.staticGame.map.rand);
            randomNum = Game.rand.nextInt(this.allowedPokemon.size()); // 0, 1, 2
            randomLevel = Game.rand.nextInt(3); // 0, 1, 2
            pokemonName = this.allowedPokemon.get(randomNum);
            // this breaks if less than 5 available pokemon in route
            if (usedPokemon.contains(pokemonName) && this.allowedPokemon.size() > 4) {
                continue;
            }
            Pokemon tempPokemon = new Pokemon(pokemonName, this.level + randomLevel, Pokemon.Generation.CRYSTAL);
            // Evolve as high as possible. Notch level up by 10 whenever evolved.
            // (this makes it so that fully evolved pokemon are 'hazards')
            String evolveTo = null;
            int timesEvolved = 0;
            for (int i=1; i <= tempPokemon.level; i++) {
                if (Pokemon.gen2Evos.get(tempPokemon.name.toLowerCase()).containsKey(String.valueOf(i)) && 
                    Game.rand.nextInt(2) == 0) {
                    evolveTo = Pokemon.gen2Evos.get(tempPokemon.name.toLowerCase()).get(String.valueOf(i));
                    tempPokemon.evolveTo(evolveTo);
                    timesEvolved++;
                }
            }
            if (evolveTo != null) {
                tempPokemon.level += 10*timesEvolved;
                tempPokemon.exp = tempPokemon.gen2CalcExpForLevel(tempPokemon.level);
            }
            usedPokemon.add(pokemonName);
            this.pokemon.add(tempPokemon);

            // no idea what this stuff did - comment for now
//            if (tempPokemon.baseStats.get("catchRate") > maxCatchRate) {
//                continue;
//            }
//            // add it based on pokemon catchRate
//            randomNum = rand.nextInt(256);
//            if (randomNum < tempPokemon.baseStats.get("catchRate")) {
//                usedPokemon.add(pokemonName);
//                this.pokemon.add(tempPokemon);
//            }
        }
        // TODO: debug, remove
//        for (Pokemon pokemon : this.pokemon) {
//            System.out.println("curr route pokemon: "
//                    + String.valueOf(pokemon.name));
//        }
    }

    // get next music. if random == true, get random one that isn't same as current
    public String getNextMusic(boolean random) {
        // for now, do nature sounds between each song
        if (this.musicsIndex != 0) {
            this.musicsIndex = 0;
            return this.musics.get(this.musicsIndex);
        }
        // TODO: this doesn't work given the above code
        int nextIndex = 0;
        if (random) {
            nextIndex = this.musicsIndex;
            while (nextIndex == this.musicsIndex || nextIndex == 0) {
                nextIndex = Game.staticGame.map.rand.nextInt(this.musics.size());
            }
        }
        else {
            nextIndex = (this.musicsIndex + 1) % this.musics.size();
        }
        this.musicsIndex = nextIndex;
        return this.musics.get(this.musicsIndex);
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
                                                        game.battle.oppPokemon.name,
                                                        new DisplayText(
                                                                game,
                                                                "Wild "
                                                                        + game.battle.oppPokemon.name
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
    Map<String, Boolean> attrs;

    // temp fix to ledge direction thing
    // can't think of another place to put this.
    String ledgeDir;
    Vector2 position;
    Sprite sprite;
    Sprite overSprite; // ledges
    // if not null, has item on this square that can be pick up by pressing A on the tile
    String hasItem;  
    int hasItemAmount = 0;

    // route that this tile belongs to
    // used to signal movement to new routes
    Route routeBelongsTo;

    // this is just my idea for tiles that do something when player walks over
    // it,
    // like grass, warp tiles.
    // would probly rely on setting a game.map.player.canMove flag to false.
    // likely that playerMove function would call onWalkOver when player steps
    // onto new tile
    // tall grass wild encounter is currently in-lined in playerMoving action

    // if you need to see what type of tile this is, use name variable
    String name;
    // Needed to store which object is above the 'terrain'. Like a tree, rock, house piece, etc.
    // This must be stored because it needs to be sent over the network, and saved locally.
    String nameUpper = "";

    String biome = "";

    // Used by hidden switches (so it knows which door tiles to flip)
    // I'm storing Vector2 so I can serialize these values. Unfortunately the switch
    // tile will need to be smart enough to know if it should flip switches on same
    // layer, layer below, above, etc.
    //
    // This has to be initialized manually. Defaulting to null to save space.
    ArrayList<Vector2> doorTiles;

    // for when you collect items from this tile
    HashMap<String, Integer> items = new HashMap<String, Integer>();

    /**
     * Factory method. Useful when loading from file, and need to create Tile subclasses for some Tiles.
     * @see TrainerTipsTile
     * 
     * TODO: I kind of regret doing it this way. Should have just made an 'if' block in Tile's onPressA,
     * added TrainerTipsTile fields here and called it good.
     */
    public static Tile get(Network.TileData tileData, Route routeBelongsTo) {
        if (tileData.tileNameUpper.contains("sign")) {
            return new TrainerTipsTile(tileData.pos.cpy(), routeBelongsTo, tileData.isUnown, tileData.message);
        }
        Tile tile = new Tile(tileData.tileName, tileData.tileNameUpper, tileData.pos.cpy(), true, routeBelongsTo);
        tile.items = tileData.items;
        tile.hasItem = tileData.hasItem;
        tile.hasItemAmount = tileData.hasItemAmount;
        tile.doorTiles = tileData.doorTiles;
        return tile;
    }

    public Tile(String tileName, String nameUpper, Vector2 pos) {
        this(tileName, nameUpper, pos, false);
    }

    public Tile(String tileName, String nameUpper, Vector2 pos, boolean color) {
        this(tileName, nameUpper, pos, color, null);
    }
    public Tile(String tileName, String nameUpper, Vector2 pos, boolean color, Route routeBelongsTo) {
        // initialize attributes of tile
        this.attrs = new HashMap<String, Boolean>();
        this.attrs.put("solid", false);
        this.attrs.put("ledge", false);
        this.attrs.put("water", false);
        this.attrs.put("grass", false);
        this.attrs.put("tree",  false);

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
        } else if (tileName.equals("grass2")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/grass2_under.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(Gdx.files.internal("tiles/grass2_over.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("grass", true);
            this.attrs.put("cuttable", true);
            this.items.put("grass", 1);
            // this.sprite.setColor(new Color(.1f, .1f, .1f, 1f)); // debug
        } else if (tileName.equals("grass3")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/grass3_under.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(Gdx.files.internal("tiles/grass3_over.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("grass", true);
            this.attrs.put("cuttable", true);
            this.items.put("grass", 1);
        } else if (tileName.equals("grass_sand1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/sand1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(Gdx.files.internal("tiles/grass2_over.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("grass", true);
            this.attrs.put("cuttable", true);
            this.items.put("grass", 1);
        } else if (tileName.equals("flower1")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/flower1.png"));
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
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/blank.png"));
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
        } else if (tileName.equals("ledge1_left")) {
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
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/qmark_tile1.png"));
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
            this.items.put("log", 1);
            String[] items = {"black apricorn", "blue apricorn", "green apricorn", "pink apricorn",
                              "red apricorn", "white apricorn", "yellow apricorn"};
            if (Game.staticGame.map.rand.nextInt(2) == 0) {
                this.items.put(items[Game.staticGame.map.rand.nextInt(items.length)], 1);
            }
        } else if (tileName.equals("bush2")) {
            Texture playerText;
            playerText = TextureCache.get(Gdx.files.internal("tiles/bush2_color.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(Gdx.files.internal("tiles/green1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        } else if (tileName.equals("tree_small1")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/tree_small1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        } else if (tileName.equals("rock1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/rock1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        } else if (tileName.equals("rock2")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/rock2.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        } else if (tileName.equals("rock3")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/rock3.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        } else if (tileName.equals("sand1")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/sand1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.equals("path1")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/path1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        // grass-like ground
        } else if (tileName.contains("green")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/"+tileName+".png"));
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
//            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/tree2_under.png"));
//            this.sprite = new Sprite(playerText, 0, 0, 16, 32);
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/tree3_under.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(Gdx.files.internal("tiles/tree3_over.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 32);
            this.attrs.put("solid", true);
            this.attrs.put("tree", true);
        } else if (tileName.equals("tree4")) {
          Texture playerText = TextureCache.get(Gdx.files.internal("tiles/tree4_under.png"));
          this.sprite = new Sprite(playerText, 0, 0, 16, 16);
          playerText = TextureCache.get(Gdx.files.internal("tiles/tree4_over.png"));
          this.overSprite = new Sprite(playerText, 0, 0, 16, 32);
          this.attrs.put("solid", true);
          this.attrs.put("tree", true);
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
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/water2.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
            this.attrs.put("water", true);
        }
        else if (tileName.equals("water3")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/water3.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
            this.attrs.put("water", true);
        }
        else if (tileName.equals("black1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/blank3.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        }
        else if (tileName.equals("campfire1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/campfire1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 20);
            this.attrs.put("solid", true);
        }
        else if (tileName.equals("fence1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/fence1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 20);
            this.attrs.put("solid", true);
        }
        else if (tileName.equals("sleeping_bag1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/sleeping_bag1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 24, 16);
        }
        else {
            // just load from image file
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/buildings/"+tileName+".png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            if (!tileName.contains("door") && !tileName.contains("floor") && 
                !tileName.contains("rug") && !tileName.contains("__off")) {
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
            // load from image file based on the name
            Texture text;
            if (this.nameUpper.contains("house")) {
                text = TextureCache.get(Gdx.files.internal("tiles/buildings/"+this.nameUpper+".png"));
            }
            else {
                text = TextureCache.get(Gdx.files.internal("tiles/"+this.nameUpper+".png"));
            }
            this.overSprite = new Sprite(text, 0, 0, 16, 16);
            if (!this.nameUpper.contains("door") &&
                !this.nameUpper.contains("floor") &&
                !this.nameUpper.contains("tree_planted")) {
                this.attrs.put("solid", true);
            }
            if (!this.nameUpper.contains("floor") && !this.nameUpper.contains("pokeball") && !this.nameUpper.contains("bush")) {
                this.attrs.put("cuttable", true);
                this.items.put("grass", 1);
                this.items.put("log", 1);
            }
            if (this.nameUpper.equals("bush2_color")) {
                this.attrs.put("headbuttable", true);
                this.attrs.put("cuttable", true);
                this.items.put("log", 1);
            }
            if (this.nameUpper.equals("pokeball1")) {
                this.attrs.put("solid", true);
                this.hasItem = "pokÈ ball";
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
     * Remove upper part of tile and put pokeball in inventory.
     * @param player
     */
    public void pickUpItem(Player player) {
        this.overSprite = null;
        this.nameUpper = "";
        this.attrs.put("solid", false);
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

    public void onPressA(Game game) {
        if (this.hasItem != null) {
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
                                  new SetField(game, "playerCanMove", true,
                                  null)));
                this.pickUpItem(game.player);
            }
        }
        else if (game.map.pokemon.containsKey(this.position)) {
            Pokemon pokemon = game.map.pokemon.get(this.position);
            if (game.type == Game.Type.CLIENT) {
                // Server will send a PausePokemon back if this succeeds.
                game.client.sendTCP(new Network.PausePokemon(game.player.network.id,
                                                             pokemon.position,
                                                             true));
                return;
            }

//            game.map.pokemon.get(this.position)
            // TODO: play animation
            game.playerCanMove = false;
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
            Action nextAction = new SetField(pokemon, "dirFacing", oppDir,
                                new WaitFrames(game, 20,
                                new SplitAction(pokemon.new Emote("happy", null),
                                new WaitFrames(game, 20,
                                new PlaySound(pokemon,
                                null)))));
            if (pokemon.previousOwner != game.player) {
                nextAction.append(new DisplayText(game, pokemon.name.toUpperCase()+" seems friendly. ", null, false, true, null));
            }
//                              new DisplayText(game, "Put "+pokemon.name.toUpperCase()+" in it' POKÈBALL?", null, true, false,
            nextAction.append(new DisplayText(game, "Add "+pokemon.name.toUpperCase()+" to your party?", null, true, false,
                              new DrawYesNoMenu(null,
                                  new DisplayText.Clear(game,
                                  new WaitFrames(game, 3,
                                  pokemon.new AddToInventory(
                                  new SetField(game, "playerCanMove", true,
                                  new SetField(pokemon, "canMove", true,
                                  null))))),
                              new DisplayText.Clear(game,
                              new WaitFrames(game, 3,
                              new SetField(game, "playerCanMove", true, 
                              new SetField(pokemon, "canMove", true,
                              null)))))));
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
        else if (this.name.equals("pkmnmansion_statue1") && this.doorTiles != null && game.player.dirFacing.equals("up")) {
            // TODO: secret switch text
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
        else if (this.nameUpper.equals("mewtwo_overworld")) {
            game.playerCanMove = false;
            SpecialMewtwo1 mewtwo = new SpecialMewtwo1(50, this);
            game.battle.oppPokemon = mewtwo;
            // The first Pokemon the player sends out in battle should have > 0 hp.
            for (Pokemon currPokemon : game.player.pokemon) {
                if (currPokemon.currentStats.get("hp") > 0) {
                    game.player.currPokemon = currPokemon;
                    break;
                }
            }
            Action fadeMusic = new FadeMusic("currMusic", "out", "pause", 0.025f, 
                               new CallMethod(game.currMusic, "setVolume", new Object[]{1f}, null));
            game.insertAction(new SplitAction(fadeMusic,
                              new WaitFrames(game, 20,
                              new DisplayText(game, "...", null, false, true,
                              new WaitFrames(game, 100,
                              new SpecialBattleMewtwo(game, mewtwo))))));
//            game.fadeMusicAction = fadeMusic;
        }
        else if (this.nameUpper.contains("bush") || this.name.contains("grass")) {  // && !this.name.contains("large")
            game.playerCanMove = false;
            game.insertAction(new DisplayText(game, "A Grass-type POKÈMON can CUT this.", null, null,
                              new WaitFrames(game, 3, 
                              new SetField(game, "playerCanMove", true,
                              null))));
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

    public void onWalkOver() {
    }
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
        TrainerTipsTile.messages.add("You can craft POKÈBALLS out of Apricorns at a campfire.");
        TrainerTipsTile.messages.add("If you white out during battle, you will return to the last place you used a sleeping bag.");
        TrainerTipsTile.messages.add("Using a sleeping bag will slowly restore your party' hp.");
//        TrainerTipsTile.messages.add("Ghosts will chase you at night, but remember! A campfire will deter them.");
        TrainerTipsTile.messages.add("Ghosts may appear in the woods at night. A campfire will ward them off.");
        TrainerTipsTile.messages.add("Use CUT on trees and tall grass to get building materials.");
//        TrainerTipsTile.messages.add("Build fences to prevent your pokemon from running away when you let them out of their POKÈBALL.");
        TrainerTipsTile.messages.add("Build fences to prevent your pokemon from wandering off when you let them out of their POKÈBALL.");
        TrainerTipsTile.messages.add("You can build a door between two roof tiles to build a back door to your house.");
        TrainerTipsTile.messages.add("Sleeping indoors will restore hp twice as fast as sleeping outdoors.");
        TrainerTipsTile.messages.add("Use CUT on buildings to remove them.");
    }

    @Override
    public void onPressA(Game game) {
        game.playerCanMove = false;
        if (this.isUnown) {
            DisplayText.unownText = true;  // makes text look glitchy
            game.map.unownSpawn = this;  // TODO: probably don't need this anymore.
            game.currMusic.pause();
            Music music = Gdx.audio.newMusic(Gdx.files.internal("music/unown1.ogg"));
            music.setLooping(true);
            music.setVolume(1f);
            game.currMusic = music;
            game.currMusic.play();
            this.isUnown = false;
        }
        game.insertAction(new DisplayText(game, "TRAINER TIPS!    ", null, null,
                          new DisplayText.Clear(game,
                          new DisplayText(game, this.message, null, null,
                          new WaitFrames(game, 3,  // this fixes issue where final A press will re-trigger text.
                          new SetField(game, "playerCanMove", true,
                          null))))));
    }
}