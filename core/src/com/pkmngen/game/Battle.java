package com.pkmngen.game;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;
import com.pkmngen.game.DrawCraftsMenu.SelectAmount;
import com.pkmngen.game.Network.BattleTurnData;
import com.pkmngen.game.Pokemon.Standing;
import com.pkmngen.game.util.LinkedMusic;
import com.pkmngen.game.util.SpriteProxy;
import com.pkmngen.game.util.TextureCache;

class AfterFriendlyFaint extends Action {
    public int layer = 129;

    public AfterFriendlyFaint() {}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        game.actionStack.remove(this);
        boolean hasAlivePokemon = false;
        for (Pokemon pokemon : game.player.pokemon) {
            if (pokemon.currentStats.get("hp") > 0 && !pokemon.isEgg) {
                hasAlivePokemon = true;
                break;
            }
        }
        if (hasAlivePokemon) {
            game.battle.network.expectPlayerSwitch = true;
            game.insertAction(new DisplayText.Clear(game,
                              new WaitFrames(game, 3,
                              new DrawPokemonMenu.Intro(
                              new DrawPokemonMenu(game,
                              null)))));
            return;
        }
        // Restore hp to half for each pokemon if player whites out.
        for (Pokemon pokemon : game.player.pokemon) {
            if (pokemon.isEgg) {
                continue;
            }
            pokemon.currentStats.put("hp", pokemon.maxStats.get("hp")/2);
        }
        BattleFadeOut.whiteScreen = true;
        game.musicController.playerFainted = true;
        int interiorTilesIndex = 100;
        game.battle.oppPokemon.aggroPlayer = false;  // stop aggro-ing if was aggroing
        Map<Vector2, Tile> tiles = game.map.overworldTiles;
        if (game.player.spawnIndex != -1) {
            tiles = game.map.interiorTiles.get(game.player.spawnIndex);
            interiorTilesIndex = game.player.spawnIndex;
        }
        Tile playerTile = tiles.get(game.player.spawnLoc);
        
        // reset batch colors
        Route newRoute = playerTile.routeBelongsTo;
        if (newRoute != null && newRoute.name.contains("pkmnmansion")) {
            game.mapBatch.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
        }
        else if (newRoute != null && newRoute.name.equals("regi_cave1")) {
        }
        else if (!game.map.timeOfDay.equals("night")) {
            game.mapBatch.setColor(new Color(1f, 1f, 1f, 1f));
        }
        else {
            game.mapBatch.setColor(new Color(0.08f, 0.08f, 0.3f, 1.0f));
        }
        // issues where map.currRoute == null causes bad things.
        if (newRoute == null) {
            newRoute = new Route("", 2);
        }

        game.insertAction(new DisplayText.Clear(game,
                          new WaitFrames(game, 3, new DisplayText(game, ""+game.player.name.toUpperCase()+" is out of useable POKéMON!", null, null,
                          new DisplayText(game, ""+game.player.name.toUpperCase()+" whited out!", null, null,
                          // TODO: remove
//                          new SplitAction(
//                              new BattleFadeOut(game,
//                              new DoneWithDemo(game)),
                          // TODO: this puts the player in the overworld
                          // will eventually want to be able to respawn indoors
                          // probably something like game.player.spawnLocTiles
                          new SetField(game.player, "position", game.player.spawnLoc.cpy(),
                          new SetField(game.map, "tiles", tiles,
                          new SetField(game.map, "interiorTilesIndex", interiorTilesIndex,
                          new SetField(game.player, "dirFacing", "down",
                          new SetField(game.player, "currSprite", game.player.standingSprites.get("down"),
                          // Required by musicController to know which song to play.
                          new SetField(game.map, "currRoute", newRoute,
                          new Game.SetCamPos(game.player.spawnLoc.cpy().add(16, 0),
                          new SplitAction(new BattleFadeOut(game, 4, null),
                          new BattleFadeOutMusic(game,
                          new DisplayText(game, "Weary from battle, you flee to the last known safe place...", null, null,
                          new BattleFadeOut.WhiteScreen(false,
                          new SplitAction(//new SetField(game, "currMusic", game.map.currRoute.music,
//                                          new WaitFrames(game, 100,
                                          // TODO: test
//                                          new CallMethod(game.loadedMusic.get(game.musicController.currOverworldMusic), "setVolume", new Object[]{0.1f},  // TODO: remove
                                          new SetField(game.musicController, "playerFainted", false,  // needs to be before resume = true is set.
                                          new SetField(game.musicController, "nightAlert", false,  // needs to be before resume = true is set.
//                                          new WaitFrames(game, 600,
                                          new SetField(game.musicController, "resumeOverworldMusic", true,
//                                          new FadeMusic("currMusic", "in", "", 0.2f, false, 1f,
//                                          new FadeMusic(game.loadedMusic.get(game.musicController.currOverworldMusic), 0.2f,  // TODO: remove
                                          null))),
//                          new SplitAction(new FadeIn(),
                          new SetField(game, "playerCanMove", true,
                          null))))))))))))))))));
    }
    // TODO: remove
//    static class FadeIn extends Action {
//        public int layer = 129;
//
//        public FadeIn() {}
//
//        public int getLayer(){return this.layer;}
//
//        @Override
//        public void step(Game game) {
//            System.out.println(game.map.currRoute.music);
//            System.out.println(game.currMusic);
//            System.out.println(game.map.currRoute.music.getVolume());
//            System.out.println(game.currMusic.getVolume());
//            game.currMusic = game.map.currRoute.music;
//            game.currMusic.play();
//            game.actionStack.remove(this);
//        }
//    }
}

class Attack {
    String name;
    String effect;
    int power;
    String type;
    int accuracy;
    int pp;
    int effectChance; // chance to paralyze, lower speed, poison, etc.
    boolean isPhysical = false;
    boolean isCrit = false;

    // Network-related things
    int damage;

    public Attack() {}

    public Attack(String name, String effect, int power, String type, int accuracy, int pp, int effectChance) {
        this.name = name;
        this.effect = effect;
        this.power = power;
        this.type = type;
        this.accuracy = accuracy;
        this.pp = pp;
        this.effectChance = effectChance;
    }

    // Basically 'tackle' attack, with modified power/accuracy
    public static class Default extends Action {
        ArrayList<Vector2> positions;
        Vector2 position;
        ArrayList<Sprite> sprites;
        Sprite sprite;
        ArrayList<Integer> repeats;
        ArrayList<Float> alphas;
        ArrayList<String> sounds;
        String sound;
        public int layer = 120;
        Sprite helperSprite;
        boolean doneYet; // unused
        int power;
        int accuracy;

        public Default(Game game, int power, int accuracy, Action nextAction) {
            this.power = power;
            this.accuracy = accuracy;

            this.doneYet = false; // unused
            this.nextAction = nextAction;

            this.position = new Vector2(game.player.currPokemon.backSprite.getX(),
                                        game.player.currPokemon.backSprite.getY());

            this.positions = new ArrayList<Vector2>();
            this.positions.add(new Vector2(8,0));// move forward 1
            this.positions.add(new Vector2(-8,0));// move back 1
            for (int i = 0; i < 13; i++) {
                this.positions.add(new Vector2(0,0));
            }

            this.sprites = new ArrayList<Sprite>(); // may use this in future
            for (int i = 0; i < 15; i++) {
                this.sprites.add(null);
            }

            this.repeats = new ArrayList<Integer>();
            this.repeats.add(7);
            this.repeats.add(3);
            this.repeats.add(17);
            this.repeats.add(6);
            this.repeats.add(7);
            this.repeats.add(5);
            this.repeats.add(9);
            this.repeats.add(4);
            this.repeats.add(8);
            this.repeats.add(6);
            this.repeats.add(7);
            this.repeats.add(5);
            this.repeats.add(9);
            this.repeats.add(4);
            this.repeats.add(11);

            this.alphas = new ArrayList<Float>();
            this.alphas.add(1f);
            this.alphas.add(1f);
            this.alphas.add(1f);
            for (int i = 0; i < 6; i++) {
                this.alphas.add(0f);
                this.alphas.add(1f);
            }

            this.sounds = new ArrayList<String>();
            this.sounds.add(null); //
            this.sounds.add("tackle1");
            for (int i = 0; i < 13; i++) {
                this.sounds.add(null);
            }
        }

        public String getCamera() {return "gui";}

        public int getLayer(){return this.layer;}

        @Override
        public void step(Game game) {
            // set sprite position
            // if done with anim, do nextAction
            if (positions.isEmpty() || sprites.isEmpty()) {
                // Assign damage to opposing pkmn
                int currHealth = game.battle.oppPokemon.currentStats.get("hp");
                // TODO: Correct damage calculation
                int finalHealth = currHealth - this.power;
                if (finalHealth < 0) {finalHealth = 0;} // Make sure finalHealth isn't negative
                game.battle.oppPokemon.currentStats.put("hp", finalHealth);

                game.insertAction(this.nextAction);
                game.actionStack.remove(this);
                return;
            }

            // get next frame
            this.sprite = sprites.get(0);

            // debug
            //        this.helperSprite.draw(game.floatingBatch);

            // draw current sprite
            //        if (this.sprite != null) {
            //            this.sprite.setPosition(position.x, position.y);
            //            this.sprite.draw(game.floatingBatch);
            //        }

            float currAlpha = this.alphas.get(0);
            game.battle.oppPokemon.sprite.setAlpha(currAlpha);
//            Color color = game.battle.oppPokemon.sprite.getColor();
//            game.battle.oppPokemon.sprite.setColor(color.r, color.g, color.b, currAlpha);
            // special battles require this
            if (game.battle.oppPokemon.breathingSprite != null) {
                game.battle.oppPokemon.breathingSprite.setAlpha(currAlpha);
            }

            // debug
            //        if (this.repeats.size() == 14) {
            //            return;
            //        }

            // get next sound, play it
            this.sound = this.sounds.get(0);
            if (this.sound != null) {
                game.insertAction(new PlaySound(this.sound, null));
                this.sounds.set(0, null); // don't play same sound over again
            }

            // repeat sprite/pos for current object for 'frames[0]' number of frames.
            if (this.repeats.get(0) > 1) {
                this.repeats.set(0, this.repeats.get(0) - 1);
            }
            else {
                // since position is relative, only update once each time period
                this.position = this.position.add(positions.get(0));
                game.player.currPokemon.backSprite.setPosition(this.position.x, this.position.y);
                positions.remove(0);
                sprites.remove(0);
                repeats.remove(0);
                sounds.remove(0);
                alphas.remove(0);
            }
        }
    }

    // Basically 'tackle' attack, with modified power/accuracy
    public static class DefaultEnemy extends Action {
        Pokemon pokemon;
        Pokemon oppPokemon; // pokemon being hit

        ArrayList<Vector2> positions;
        Vector2 position;
        ArrayList<Sprite> sprites;
        Sprite sprite;
        ArrayList<Integer> repeats;
        ArrayList<Float> alphas;
        ArrayList<String> sounds;
        String sound;

        public int layer = 120;
        Sprite helperSprite;

        boolean doneYet; // unused

        int power;

        int accuracy;

        public DefaultEnemy(Pokemon attackingPokemon, Pokemon oppPokemon, int power, int accuracy, Action nextAction) {
            this.pokemon = attackingPokemon;
            this.oppPokemon = oppPokemon;

            this.power = power;
            this.accuracy = accuracy;

            this.doneYet = false; // unused
            this.nextAction = nextAction;

            // consider doing relative positions from now on
            // this.position = new Vector2(104+4*3-2,200-6*3-2); // post scaling change
            this.position = new Vector2(this.pokemon.sprite.getX(), this.pokemon.sprite.getY());

            this.positions = new ArrayList<Vector2>();
            this.positions.add(new Vector2(-8,0));// move forward 1
            this.positions.add(new Vector2(8,0));// move back 1
            for (int i = 0; i < 13; i++) {
                this.positions.add(new Vector2(0,0));
            }

            this.sprites = new ArrayList<Sprite>(); // may use this in future
            for (int i = 0; i < 15; i++) {
                this.sprites.add(null);
            }

            this.repeats = new ArrayList<Integer>();
            this.repeats.add(7);
            this.repeats.add(3);
            this.repeats.add(17);
            this.repeats.add(6);
            this.repeats.add(7);
            this.repeats.add(5);
            this.repeats.add(9);
            this.repeats.add(4);
            this.repeats.add(8);
            this.repeats.add(6);
            this.repeats.add(7);
            this.repeats.add(5);
            this.repeats.add(9);
            this.repeats.add(4);
            this.repeats.add(11);

            this.alphas = new ArrayList<Float>();
            this.alphas.add(1f);
            this.alphas.add(1f);
            this.alphas.add(1f);
            for (int i = 0; i < 6; i++) {
                this.alphas.add(0f);
                this.alphas.add(1f);
            }

            this.sounds = new ArrayList<String>();
            this.sounds.add(null); //
            this.sounds.add("tackle1");
            for (int i = 0; i < 13; i++) {
                this.sounds.add(null);
            }

            //        Texture text = new Texture(Gdx.files.internal("attack_menu/helper3.png"));
            //        this.helperSprite = new Sprite(text, 0, 0, 160, 144);
        }
        public String getCamera() {return "gui";}

        public int getLayer(){return this.layer;}

        @Override
        public void step(Game game) {
            // set sprite position
            // if done with anim, do nextAction
            if (positions.isEmpty() || sprites.isEmpty()) {
                // assign damage to opposing pkmn
                int currHealth = this.oppPokemon.currentStats.get("hp");
                // TODO - correct damage calculation
                int finalHealth = currHealth - this.power;
                if (finalHealth < 0) {finalHealth = 0;} // make sure finalHealth isn't negative
                this.oppPokemon.currentStats.put("hp", finalHealth);

                game.insertAction(this.nextAction);
                game.actionStack.remove(this);
                return;
            }

            // get next frame
            this.sprite = sprites.get(0);

            // debug
            //        this.helperSprite.draw(game.floatingBatch);

            // draw current sprite
            //        if (this.sprite != null) {
            //            this.sprite.setPosition(position.x, position.y);
            //            this.sprite.draw(game.floatingBatch);
            //        }

            float currAlpha = this.alphas.get(0);
            this.oppPokemon.backSprite.setAlpha(currAlpha);

            // debug
            //        if (this.repeats.size() == 14) {
            //            return;
            //        }

            // get next sound, play it
            this.sound = this.sounds.get(0);
            if (this.sound != null) {
                game.insertAction(new PlaySound(this.sound, null));
                this.sounds.set(0, null); // don't play same sound over again
            }

            // repeat sprite/pos for current object for 'frames[0]' number of frames.
            if (this.repeats.get(0) > 1) {
                this.repeats.set(0, this.repeats.get(0) - 1);
            }
            else {
                // since position is relative, only update once each time period
                this.position = this.position.add(positions.get(0));
                this.pokemon.sprite.setPosition(this.position.x, this.position.y);
                positions.remove(0);
                sprites.remove(0);
                repeats.remove(0);
                sounds.remove(0);
                alphas.remove(0);
            }
        }
    }

    // Lick attack animation
    static class Lick extends Action {
        ArrayList<Vector2> positions;
        Vector2 position;
        ArrayList<Vector2> screenPositions;
        Vector2 currPosition;
        ArrayList<Sprite> sprites;
        Sprite sprite;
        ArrayList<Integer> repeats;
        String sound;
        ArrayList<String> sounds;

        Sprite blockSprite;

        // TODO: has to be higher layer than ThrowOutPokemon()
        // why is ThrowOutPokemon in such a high layer?
        public int layer = 108;
        Sprite helperSprite; // just for helping me position the animation. delete later.

        Pokemon attacker;

        Pokemon target;

        Pixmap pixmap;
        // gen 7 properties
        int power = 30;

        int accuracy = 100;

        public Lick(Game game,
                    Pokemon attacker,
                    Pokemon target,
                    Action nextAction) {
            this.attacker = attacker;
            this.target = target;
            this.nextAction = nextAction;

            // single pixel sprite used for drawing the effect
            Texture text = new Texture(Gdx.files.internal("battle/pixel1.png"));
            this.blockSprite = new Sprite(text, 0, 0, 1, 1);

            this.repeats = new ArrayList<Integer>();
            this.repeats.add(19-1); // wait 19 frames
            this.repeats.add(13-1); // orb appears 13 frames
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    this.repeats.add(6-1);
                }
                this.repeats.add(7-1);
            }
            this.repeats.add(23-1); // wait 23 frames

            this.position = new Vector2(0, 0);
            this.positions = new ArrayList<Vector2>();
            for (int i = 0; i < 9; i++) {
                this.positions.add(new Vector2(0,0));
            }

            // screen movement after attack
            this.screenPositions = new ArrayList<Vector2>();
            // 9 frames nothing
            for (int i = 0; i < 9; i++) {
                this.screenPositions.add(new Vector2(0,0));
            }
            // 5 frame move 2 right
            for (int i = 0; i < 5; i++) {
                this.screenPositions.add(new Vector2(2,0));
            }
            // 9 frame move back
            for (int i = 0; i < 9; i++) {
                this.screenPositions.add(new Vector2(0,0));
            }
            // 4 frame move 1 right
            for (int i = 0; i < 4; i++) {
                this.screenPositions.add(new Vector2(1,0));
            }
            // 19 frame move back
            for (int i = 0; i < 19; i++) {
                this.screenPositions.add(new Vector2(0,0));
            }

            text = new Texture(Gdx.files.internal("attacks/enemy_lick_sheet1.png"));

            this.sprites =  new ArrayList<Sprite>();
            this.sprites.add(null); // draw nothing
            for (int i = 0; i < 7; i++) {
                this.sprites.add(new Sprite(text, 160*i, 0, 160, 144));
            }
            this.sprites.add(null); // draw nothing

            // sounds to play
            this.sounds = new ArrayList<String>();
            this.sounds.add("lick1");
            for (int i = 0; i < 7; i++) {
                this.sounds.add(null);
            }
            this.sounds.add("hit_normal1");

            //            text = new Texture(Gdx.files.internal("attacks/enemy_slash/helper1.png"));
            //            this.helperSprite = new Sprite(text, 0, 0, 160, 144);
        }
        public String getCamera() {return "gui";}

        public int getLayer(){return this.layer;}

        @Override
        public void step(Game game) {
            // set sprite position
            // if done with anim, do nextAction
            if (this.repeats.isEmpty()) {
                if (!this.screenPositions.isEmpty()) {
                    this.currPosition = this.screenPositions.get(0);
                    // screen wiggle
                    // TODO: *3 b/c screen is scaled, alternative approach would be nice
                    this.pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, 160*3, 144*3);
                    for (int j=0; j < 144; j++) {
                        for (int i=0; i < 160; i++) {
                            this.blockSprite.setColor(new Color(this.pixmap.getPixel(i*3, j*3)));
                            // not using this.currPosition.y here, but may in future animations
                            this.blockSprite.setPosition(i + this.currPosition.x, j + this.currPosition.y);
                            this.blockSprite.draw(game.uiBatch);
                        }
                    }
                    this.screenPositions.remove(0);
                }
                else {
                    // assign damage to target pkmn
                    int currHealth = this.target.currentStats.get("hp");
                    // TODO - correct damage calculation
                    int finalHealth = currHealth - this.power;
                    if (finalHealth < 0) {finalHealth = 0;} // make sure finalHealth isn't negative
                    this.target.currentStats.put("hp", finalHealth);

                    game.actionStack.remove(this);
                    game.insertAction(this.nextAction);
                }
                return;
            }

            // get next sound, play it
            this.sound = this.sounds.get(0);
            if (this.sound != null) {
                game.insertAction(new PlaySound(this.sound, null));
                this.sounds.set(0, null); // don't play same sound over again
            }

            // get next frame
            this.sprite = sprites.get(0);

            // debug
            //            this.helperSprite.draw(game.floatingBatch);

            // draw current sprite
            if (this.sprite != null) {
                this.sprite.setPosition(position.x, position.y);
                this.sprite.draw(game.uiBatch);
            }

            // debug
            //            if (this.repeats.size() == 5) {
            //                return;
            //            }

            // repeat sprite/pos for current object for 'frames[0]' number of frames.
            if (this.repeats.get(0) > 0) {
                this.repeats.set(0, this.repeats.get(0) - 1);
            }
            else {
                // since position is relative, only update once each time period
                this.position = this.position.add(positions.get(0));
                positions.remove(0);
                sprites.remove(0);
                repeats.remove(0);
                sounds.remove(0);
            }
        }
    }

    static class Mewtwo_Special1 extends Action {
        public int layer = 109;
        // TODO: remove if unused
        int power = 100;
        int accuracy = 100;
        //

        Pokemon attacker;
        Pokemon target;

        Pixmap pixmap = null;
        Sprite sprite;
        ArrayList<int[]> offsets = new ArrayList<int[]>();
        int[] currOffsets;
        ArrayList<Vector2> positions = new ArrayList<Vector2>();
        Vector2 currPosition;
        ArrayList<String> shaderVals = new ArrayList<String>();
        String currShaderVal;
        ShaderProgram currShader;
        String vertexShader;
        boolean firstStep = true;
        boolean isNightShade = false;  // if true, use different sound effect
        boolean hitSound = true;
        int timer = 0;

        public Mewtwo_Special1(Game game,
                               Pokemon attacker,
                               Pokemon target,
                               Action nextAction) {
            this.target = target;
            this.attacker = attacker;
            this.nextAction = nextAction;

            // single pixel sprite used for drawing the effect
            Texture text = new Texture(Gdx.files.internal("battle/pixel1.png"));
            this.sprite = new Sprite(text, 0, 0, 1, 1);

            this.vertexShader = "attribute vec4 a_position;\n"
                    + "attribute vec4 a_color;\n"
                    + "attribute vec2 a_texCoord;\n"
                    + "attribute vec2 a_texCoord0;\n"

                            + "uniform mat4 u_projTrans;\n"

                            + "varying vec4 v_color;\n"
                            + "varying vec2 v_texCoords;\n"

                            + "void main()\n"
                            + "{\n"
                            + "    v_color = a_color;\n"
                            + "    v_texCoords = a_texCoord0;\n"
                            + "    gl_Position =  u_projTrans * a_position;\n"
                            // below can be used to translate screen pixels (for attacks, etc
                            //                    + "    gl_Position =  u_projTrans * vec4(a_position.x, a_position.y, a_position.z, 1.0);\n"
                            + "}\n";

            // shader 'flash' anim at beginning
            // 0, .33, .66, 1 ?
            String darken1 = this.getShader(-0.33f);
            String darken2 = this.getShader(-0.66f);
            String darken3 = this.getShader(-1f);
            String lighten1 = this.getShader(0.33f);
            String lighten2 = this.getShader(0.66f);
            String lighten3 = this.getShader(1f);
            String normal = this.getShader(0f);
            this.shaderVals.add(darken1);
            this.shaderVals.add(darken1);
            this.shaderVals.add(darken2);
            this.shaderVals.add(darken2);
            this.shaderVals.add(darken3);
            this.shaderVals.add(darken3);
            this.shaderVals.add(darken2);
            this.shaderVals.add(darken2);
            this.shaderVals.add(darken1);
            this.shaderVals.add(darken1);
            this.shaderVals.add(normal);
            this.shaderVals.add(normal);
            this.shaderVals.add(lighten1);
            this.shaderVals.add(lighten1);
            this.shaderVals.add(lighten2);
            this.shaderVals.add(lighten2);
            this.shaderVals.add(lighten3);
            this.shaderVals.add(lighten3);
            this.shaderVals.add(lighten2);
            this.shaderVals.add(lighten2);
            this.shaderVals.add(lighten1);
            this.shaderVals.add(lighten1);
            this.shaderVals.add(normal);
            this.shaderVals.add(normal);
            this.shaderVals.add(darken1);
            this.shaderVals.add(darken2);
            this.shaderVals.add(darken3);
            this.shaderVals.add(darken2);
            this.shaderVals.add(darken1);
            this.shaderVals.add(normal);
            this.shaderVals.add(lighten1);
            this.shaderVals.add(lighten2);
            this.shaderVals.add(lighten3);
            this.shaderVals.add(lighten2);
            this.shaderVals.add(lighten1);
            this.shaderVals.add(normal);
            this.shaderVals.add(darken1);
            this.shaderVals.add(darken2);
            this.shaderVals.add(darken3);
            this.shaderVals.add(darken2);
            this.shaderVals.add(darken1);
            this.shaderVals.add(normal);
            this.shaderVals.add(lighten1);
            this.shaderVals.add(lighten2);
            this.shaderVals.add(lighten3);
            this.shaderVals.add(lighten2);
            this.shaderVals.add(lighten1);
            for (int i = 0; i < 8; i++) {
                this.shaderVals.add(normal);
            }

            // screen movement after attack
            // 9 frames nothing
            for (int i = 0; i < 9; i++) {
                this.positions.add(new Vector2(0,0));
            }
            // 5 frame move 2 right
            for (int i = 0; i < 5; i++) {
                this.positions.add(new Vector2(2,0));
            }
            // 9 frame move back
            for (int i = 0; i < 9; i++) {
                this.positions.add(new Vector2(0,0));
            }
            // 4 frame move 1 right
            for (int i = 0; i < 4; i++) {
                this.positions.add(new Vector2(1,0));
            }
            // 19 frame move back
            for (int i = 0; i < 19; i++) {
                this.positions.add(new Vector2(0,0));
            }
            // on 20th, start health bar subtract

        }
        public String getCamera() {return "gui";}

        public int getLayer(){return this.layer;}

        String getShader(float level) {
            String shader = "precision mediump float;\n"

                                    + "varying vec4 v_color;\n"
                                    + "varying vec2 v_texCoords;\n"
                                    + "uniform sampler2D u_texture;\n"
                                    + "uniform mat4 u_projTrans;\n"

                                    + "bool equals(float a, float b) {\n"
                                    + "    return abs(a-b) < 0.0001;\n"
                                    + "}\n"

                                    + "void main() {\n"
                                    + "    vec4 color = texture2D (u_texture, v_texCoords) * v_color;\n"

                                    + "    float level = "+level+";\n" // can't do +- or -+ inline
                                    + "    if (color.r >= .9 && color.g >= .9 && color.b >= .9) {\n"
                                    + "           color = vec4(color.r, color.g, color.b, color.a);\n"
                                    + "    }\n"
                                    + "    else {\n"
                                    + "        color = vec4(color.r-level, color.g-level, color.b-level, color.a);\n"
                                    + "    }\n"
                                    + "    gl_FragColor = color;\n"
                                    + "}\n";
            return shader;
        }

        @Override
        public void step(Game game) {
            if (this.timer == 0) {
                this.currShader = new ShaderProgram(this.vertexShader,
                                                    this.getShader(0.8f));
                game.uiBatch.setShader(this.currShader);
                // pause rock anim
                SpecialBattleMewtwo.RocksEffect1.shouldMoveY = false;
                SpecialBattleMewtwo.RocksEffect2.shouldMoveY = false;
            }
            else if (this.timer < 50) {
            }
            else if (this.timer == 60) {
                game.insertAction(new PlaySound("Mewtwo_Special1", null));
                SpecialBattleMewtwo.RocksEffect1.velocityX = -8;
                SpecialBattleMewtwo.RocksEffect2.velocityX = -2;  // TODO: is this doing anything?
                this.currShader = new ShaderProgram(this.vertexShader, this.getShader(0.6f));
                game.uiBatch.setShader(this.currShader);
            }
            else if (this.timer == 120) {
                SpecialBattleMewtwo.RocksEffect1.velocityX = -12;
                SpecialBattleMewtwo.RocksEffect2.velocityX = -3;
                this.currShader = new ShaderProgram(this.vertexShader, this.getShader(0.4f));
                game.uiBatch.setShader(this.currShader);
            }
            else if (this.timer == 180) {
                SpecialBattleMewtwo.RocksEffect1.velocityX = -16;
                SpecialBattleMewtwo.RocksEffect2.velocityX = -4;
                this.currShader = new ShaderProgram(this.vertexShader, this.getShader(0.2f));
                game.uiBatch.setShader(this.currShader);
            }
            else if (this.timer == 190) {
                this.currShader = new ShaderProgram(this.vertexShader, this.getShader(0.3f));
                game.uiBatch.setShader(this.currShader);
            }
            else if (this.timer == 200) {
                this.currShader = new ShaderProgram(this.vertexShader, this.getShader(0f));
                game.uiBatch.setShader(this.currShader);
            }
            else if (this.timer == 210) {
                this.currShader = new ShaderProgram(this.vertexShader, this.getShader(0.1f));
                game.uiBatch.setShader(this.currShader);
            }
            else if (this.timer == 220) {
                this.currShader = new ShaderProgram(this.vertexShader, this.getShader(-0.2f));
                game.uiBatch.setShader(this.currShader);
            }
            else if (this.timer == 230) {
                this.currShader = new ShaderProgram(this.vertexShader, this.getShader(-0.1f));
                game.uiBatch.setShader(this.currShader);
            }
            else if (this.timer == 240) {
                this.currShader = new ShaderProgram(this.vertexShader, this.getShader(-0.4f));
                game.uiBatch.setShader(this.currShader);
            }
            else if (this.timer == 250) {
                this.currShader = new ShaderProgram(this.vertexShader, this.getShader(-0.3f));
                game.uiBatch.setShader(this.currShader);
            }
            else if (this.timer == 260) {
                this.currShader = new ShaderProgram(this.vertexShader, this.getShader(-0.6f));
                game.uiBatch.setShader(this.currShader);
            }
            else if (this.timer == 270) {
                this.currShader = new ShaderProgram(this.vertexShader, this.getShader(-0.5f));
                game.uiBatch.setShader(this.currShader);
            }
            else if (this.timer == 280) {
                this.currShader = new ShaderProgram(this.vertexShader, this.getShader(-0.8f));
                game.uiBatch.setShader(this.currShader);
            }
            else if (this.timer < 300) {
            }
            else if (this.timer < 400 +150) {
                if (this.timer % 2 == 0) {
                    game.player.currPokemon.backSprite.setAlpha(1);
                }
                else {
                    game.player.currPokemon.backSprite.setAlpha(0);
                }

                if (this.timer == 340) {
                    this.currShader = new ShaderProgram(this.vertexShader, this.getShader(-0.6f));
                    game.uiBatch.setShader(this.currShader);
                }
                if (this.timer == 400) {
                    this.currShader = new ShaderProgram(this.vertexShader, this.getShader(-0.4f));
                    game.uiBatch.setShader(this.currShader);
                }
                if (this.timer == 460) {
                    this.currShader = new ShaderProgram(this.vertexShader, this.getShader(-0.2f));
                    game.uiBatch.setShader(this.currShader);
                }

                if (this.timer == 305 +150) {
                    SpecialBattleMewtwo.RocksEffect1.velocityX = -14;
                    SpecialBattleMewtwo.RocksEffect2.velocityX = -3;
                }
                else if (this.timer == 315 +150) {
                    SpecialBattleMewtwo.RocksEffect1.velocityX = -12;
                }
                else if (this.timer == 325 +150) {
                    SpecialBattleMewtwo.RocksEffect1.velocityX = -10;
                }
                else if (this.timer == 335 +150) {
                    SpecialBattleMewtwo.RocksEffect1.velocityX = -8;
                    SpecialBattleMewtwo.RocksEffect2.velocityX = -2;
                }
                else if (this.timer == 345 +150) {
                    SpecialBattleMewtwo.RocksEffect1.velocityX = -6;
                }
                else if (this.timer == 355 +150) {
                    SpecialBattleMewtwo.RocksEffect1.velocityX = -6;
                }
                else if (this.timer == 365 +150) {
                    SpecialBattleMewtwo.RocksEffect1.velocityX = -4;
                    SpecialBattleMewtwo.RocksEffect2.velocityX = -1;
                }
                else if (this.timer == 375 +150) {
                    SpecialBattleMewtwo.RocksEffect1.velocityX = -2;
                }
                else if (this.timer == 385 +150) {
                    SpecialBattleMewtwo.RocksEffect1.velocityX = 0;
                    SpecialBattleMewtwo.RocksEffect2.velocityX = 0;
                }
            }
            else if (this.timer == 550) { //550
                game.player.currPokemon.backSprite.setAlpha(1);
                this.currShader = new ShaderProgram(this.vertexShader, this.getShader(0f));
                game.uiBatch.setShader(this.currShader);
                SpecialBattleMewtwo.RocksEffect1.shouldMoveY = true;
                SpecialBattleMewtwo.RocksEffect2.shouldMoveY = true;
            }
            else if (this.timer < 600) { //600
            }
            else {
                // TODO: remove if unused.
//                // assign damage to target pkmn
//                int currHealth = this.target.currentStats.get("hp");
//                // TODO - correct damage calculation
//                int finalHealth = currHealth - this.power;
//                if (finalHealth < 0) {finalHealth = 0;} // make sure finalHealth isn't negative
//                this.target.currentStats.put("hp", finalHealth);

                game.actionStack.remove(this);
                game.insertAction(this.nextAction);
            }
            this.timer++;

            //            // run through shaders, then offsets, then positions
            //            if (!this.shaderVals.isEmpty()) {
            //                this.currShaderVal = this.shaderVals.get(0);
            //                this.currShader = new ShaderProgram(this.vertexShader,
            //                                                    this.currShaderVal);
            //                game.floatingBatch.setShader(this.currShader);
            //                this.shaderVals.remove(0);
            //            }
            //            else if (!this.offsets.isEmpty()) {
            //                this.currOffsets = this.offsets.get(0);
            //                // setting pixmap only once b/c otherwise get bad lag
            //                // would be nice to do every frame tho since there are moving rocks etc
            //                if (this.pixmap == null) {
            //                    // TODO: *3 b/c screen is scaled, alternative approach would be nice
            //                    this.pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, 160*3, 144*3);
            //                }
            //                for (int j=0; j < 144-3; j++) { // 3 pixels from top
            //                    for (int i=0; i < 160; i++) {
            //                        // note: got better performance when creating a new Color here
            //                        // rather than using Color.set() on existing Color
            //                        this.sprite.setColor(new Color(this.pixmap.getPixel(i*3, j*3)));
            //                        this.sprite.setPosition(i + this.currOffsets[j%16], j);
            //                        this.sprite.draw(game.floatingBatch);
            //                    }
            //                }
            //                this.offsets.remove(0);
            //            }
            //            else if (!this.positions.isEmpty()) {
            //                if (this.hitSound) {
            //                    // play hit sound
            //                    game.insertAction(new PlaySound("hit_normal1", null));
            //                    this.hitSound = false;
            //                }
            //                this.currPosition = this.positions.get(0);
            //                // TODO: *3 b/c screen is scaled, alternative approach would be nice
            //                this.pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, 160*3, 144*3);
            //                for (int j=0; j < 144; j++) {
            //                    for (int i=0; i < 160; i++) {
            //                        this.sprite.setColor(new Color(this.pixmap.getPixel(i*3, j*3)));
            //                        // not using this.currPosition.y here, but may in future animations
            //                        this.sprite.setPosition(i + this.currPosition.x, j + this.currPosition.y);
            //                        this.sprite.draw(game.floatingBatch);
            //                    }
            //                }
            //                this.positions.remove(0);
            //            }
            //            else {
            //                // assign damage to target pkmn
            //                int currHealth = this.target.currentStats.get("hp");
            //                // TODO - correct damage calculation
            //                int finalHealth = currHealth - this.power;
            //                if (finalHealth < 0) {finalHealth = 0;} // make sure finalHealth isn't negative
            //                this.target.currentStats.put("hp", finalHealth);
            //
            //                game.actionStack.remove(this);
            //                game.insertAction(this.nextAction);
            //            }

        }
    }

    /**
     * This was (hopefully) easier than an attack animation.
     */
    static class CrushGrip extends Action {
        public int layer = 109;
        public int timer = -40;
        boolean drawFriendly = false;
        Sprite sprite;
        Sprite sprite2;
        Music soundEffect;
        Music soundEffect2;
        Vector2 pos;
        
        public CrushGrip(Game game, Pokemon target, Action nextAction) {
            this.nextAction = nextAction;
            Texture text = TextureCache.get(Gdx.files.internal("attacks/crush_grip1.png"));
            this.sprite = new Sprite(text, 224-32, 0, 32, 32);
            text = TextureCache.get(Gdx.files.internal("attacks/crush_grip1.png"));
            this.sprite2 = new Sprite(text, 64, 0, 32, 32);
            if (target == game.battle.oppPokemon) {
                this.pos = new Vector2(116-30, 92-10);
                this.drawFriendly = false;
            }
            else {
                this.pos = new Vector2(24+30, 52+10);
                this.drawFriendly = true;
            }
        }

        public String getCamera() {return "gui";}

        public int getLayer(){return this.layer;}

        @Override
        public void firstStep(Game game) {
//            this.soundEffect = Gdx.audio.newMusic(Gdx.files.internal("sounds/ap1.ogg"));
            this.soundEffect = Gdx.audio.newMusic(Gdx.files.internal("attacks/rolling_kick_player_gsc/sound.ogg"));
            this.soundEffect.setLooping(false);
            this.soundEffect.setVolume(1f);

            this.soundEffect2 = Gdx.audio.newMusic(Gdx.files.internal("attacks/rock_throw_player_gsc/sound.ogg"));
            this.soundEffect2.setLooping(false);
            this.soundEffect2.setVolume(1f);
            
        }

        @Override
        public void step(Game game) {
            int offset = 0;
            if (this.timer < -20) {
            }
            else if (this.timer < -10) {
                int friendly = 1;
                if (this.drawFriendly) {
                    friendly = -1;
                }
                this.pos.add(2*friendly, 1*friendly);
            }
            else if (this.timer < 20) {
            }
            else if (this.timer < 166) {
                if (this.timer % 2 == 0) {
                    offset = 2;
                }
                else {
                    offset = -2;
                }
                game.uiBatch.setTransformMatrix(new Matrix4(new Vector3(offset, 0, 0), new Quaternion(), new Vector3(1, 1, 1)));
            }
            else if (this.timer < 206) {
            }
            else if (this.timer < 260) {  //+60
                SpriteProxy.inverseColors = true;
            }
            else {
                SpriteProxy.inverseColors = false;
                this.soundEffect.stop();
                this.soundEffect.dispose();
                this.soundEffect2.stop();
                this.soundEffect2.dispose();
                DrawFriendlyHealth.shouldDraw = true;
                DrawEnemyHealth.shouldDraw = true;
                game.actionStack.remove(this);
                game.insertAction(this.nextAction);
            }

            if (this.timer < 90) {
            }
            else if (this.timer < 98) {
                game.uiBatch.draw(this.sprite2, this.pos.x-offset-8, this.pos.y+8);
            }
            else if (this.timer < 106) {
            }
            else if (this.timer < 114) {
                game.uiBatch.draw(this.sprite2, this.pos.x-offset+8, this.pos.y+8);
            }
            else if (this.timer < 122) {
            }
            else if (this.timer < 130) {
                game.uiBatch.draw(this.sprite2, this.pos.x-offset-8, this.pos.y-8);
            }
            else if (this.timer < 138) {
            }
            else if (this.timer < 146) {
                game.uiBatch.draw(this.sprite2, this.pos.x-offset+8, this.pos.y-8);
            }

            if (this.timer == -20) {
                this.soundEffect.play();
            }
            else if (this.timer == 20) {
                this.soundEffect.stop();
                this.soundEffect.dispose();
                this.soundEffect2.play();
                this.sprite.setRegion(224-64, 0, 32, 32);
            }
            else if (this.timer == 70) {
                this.soundEffect2.pause();
            }
            else if (this.timer == 90) {
                this.soundEffect = Gdx.audio.newMusic(Gdx.files.internal("sounds/ap1.ogg"));
                this.soundEffect.play();
//                this.sprite.setRegion(224-128, 0, 32, 32);
            }
            else if (this.timer == 98) {
                this.sprite.setRegion(224-64, 0, 32, 32);
            }
            else if (this.timer == 106) {
                this.soundEffect.stop();
                this.soundEffect.play();
//                this.sprite.setRegion(224-128, 0, 32, 32);
            }
            else if (this.timer == 114) {
                this.sprite.setRegion(224-64, 0, 32, 32);
            }
            else if (this.timer == 122) {
                this.soundEffect.stop();
                this.soundEffect.play();
//                this.sprite.setRegion(224-128, 0, 32, 32);
            }
            else if (this.timer == 130) {
                this.sprite.setRegion(224-64, 0, 32, 32);
            }
            else if (this.timer == 138) {
                this.soundEffect.stop();
                this.soundEffect.play();
//                this.sprite.setRegion(224-128, 0, 32, 32);
            }
            else if (this.timer == 166) {
                game.uiBatch.setTransformMatrix(new Matrix4(new Vector3(0,0,0), new Quaternion(), new Vector3(1,1,1)));
            }
            else if (this.timer == 206) {
                //
//                this.soundEffect2.stop();
//                this.soundEffect2.dispose();
//                this.soundEffect2 = Gdx.audio.newMusic(Gdx.files.internal("sounds/nani.ogg"));
                // TODO: remove

                this.soundEffect2.play();
//                this.sprite.setRegion(224-128, 0, 32, 32);
            }
//            else if (this.timer == 210) {
//                this.sprite.setRegion(224-64, 0, 32, 32);
//            }
            
            if (this.drawFriendly) {
                DrawEnemyHealth.shouldDraw = false;
            }
            else {
                DrawFriendlyHealth.shouldDraw = false;
            }
            
            game.uiBatch.draw(this.sprite, this.pos.x-offset, this.pos.y);

            this.timer++;
        }
    }
    

    // this is also the Night Shade attack
    // TODO: this attack lags, you can tell b/c
    // the night shade sound effect dmg sound happens too early, but
    // the animation has the correct number of frames
    static class Psychic extends Action {
        public int layer = 109;
        // TODO: remove if unused
        int power = 100;
        int accuracy = 100;

//        Pokemon attacker;
        Pokemon target;

        Pixmap pixmap = null;
        Sprite sprite;
        ArrayList<int[]> offsets = new ArrayList<int[]>();
        int[] currOffsets;
        ArrayList<Vector2> positions = new ArrayList<Vector2>();
        Vector2 currPosition;
        ArrayList<String> shaderVals = new ArrayList<String>();
        String currShaderVal;
        ShaderProgram currShader;
        String vertexShader;
        boolean firstStep = true;
        boolean isNightShade = false;  // if true, use different sound effect
        boolean hitSound = true;

        public Psychic(Game game,
//                       Pokemon attacker,
                       Pokemon target,
                       boolean isNightShade,
                       Action nextAction) {
            this.isNightShade = isNightShade;
            this.target = target;
//            this.attacker = attacker;
            this.nextAction = nextAction;

            // if night shade, power == users level
//            if (this.isNightShade) {
//                this.power = this.attacker.level;
//            }

            // single pixel sprite used for drawing the effect
            Texture text = new Texture(Gdx.files.internal("battle/pixel1.png"));
            this.sprite = new Sprite(text, 0, 0, 1, 1);

            this.vertexShader = "attribute vec4 a_position;\n"
                    + "attribute vec4 a_color;\n"
                    + "attribute vec2 a_texCoord;\n"
                    + "attribute vec2 a_texCoord0;\n"

                        + "uniform mat4 u_projTrans;\n"

                        + "varying vec4 v_color;\n"
                        + "varying vec2 v_texCoords;\n"

                        + "void main()\n"
                        + "{\n"
                        + "    v_color = a_color;\n"
                        + "    v_texCoords = a_texCoord0;\n"
                        + "    gl_Position =  u_projTrans * a_position;\n"
                        // below can be used to translate screen pixels (for attacks, etc
                        //                    + "    gl_Position =  u_projTrans * vec4(a_position.x, a_position.y, a_position.z, 1.0);\n"
                        + "}\n";

            // shader 'flash' anim at beginning
            // 0, .33, .66, 1 ?
            String darken1 = this.getShader(-0.33f);
            String darken2 = this.getShader(-0.66f);
            String darken3 = this.getShader(-1f);
            String lighten1 = this.getShader(0.33f);
            String lighten2 = this.getShader(0.66f);
            String lighten3 = this.getShader(1f);
            String normal = this.getShader(0f);
            this.shaderVals.add(darken1);
            this.shaderVals.add(darken1);
            this.shaderVals.add(darken2);
            this.shaderVals.add(darken2);
            this.shaderVals.add(darken3);
            this.shaderVals.add(darken3);
            this.shaderVals.add(darken2);
            this.shaderVals.add(darken2);
            this.shaderVals.add(darken1);
            this.shaderVals.add(darken1);
            this.shaderVals.add(normal);
            this.shaderVals.add(normal);
            this.shaderVals.add(lighten1);
            this.shaderVals.add(lighten1);
            this.shaderVals.add(lighten2);
            this.shaderVals.add(lighten2);
            this.shaderVals.add(lighten3);
            this.shaderVals.add(lighten3);
            this.shaderVals.add(lighten2);
            this.shaderVals.add(lighten2);
            this.shaderVals.add(lighten1);
            this.shaderVals.add(lighten1);
            this.shaderVals.add(normal);
            this.shaderVals.add(normal);
            this.shaderVals.add(darken1);
            this.shaderVals.add(darken2);
            this.shaderVals.add(darken3);
            this.shaderVals.add(darken2);
            this.shaderVals.add(darken1);
            this.shaderVals.add(normal);
            this.shaderVals.add(lighten1);
            this.shaderVals.add(lighten2);
            this.shaderVals.add(lighten3);
            this.shaderVals.add(lighten2);
            this.shaderVals.add(lighten1);
            this.shaderVals.add(normal);
            this.shaderVals.add(darken1);
            this.shaderVals.add(darken2);
            this.shaderVals.add(darken3);
            this.shaderVals.add(darken2);
            this.shaderVals.add(darken1);
            this.shaderVals.add(normal);
            this.shaderVals.add(lighten1);
            this.shaderVals.add(lighten2);
            this.shaderVals.add(lighten3);
            this.shaderVals.add(lighten2);
            this.shaderVals.add(lighten1);
            for (int i = 0; i < 8; i++) {
                this.shaderVals.add(normal);
            }

            // TODO: remove
            // screen movement after attack
            // 9 frames nothing
            for (int i = 0; i < 9; i++) {
                this.positions.add(new Vector2(0,0));
            }
            // 5 frame move 2 right
            for (int i = 0; i < 5; i++) {
                this.positions.add(new Vector2(2,0));
            }
            // 9 frame move back
            for (int i = 0; i < 9; i++) {
                this.positions.add(new Vector2(0,0));
            }
            // 4 frame move 1 right
            for (int i = 0; i < 4; i++) {
                this.positions.add(new Vector2(1,0));
            }
            // 19 frame move back
            for (int i = 0; i < 19; i++) {
                this.positions.add(new Vector2(0,0));
            }
            // on 20th, start health bar subtract

            // TODO: probably just have two other arrays - positions and 'shaders', or shader intensity vals

            // offsets determine the ripple effect - sequence of 16 sprites
            // starts at bottom-left of screen
            // 127 total frames (back to normal on 28th)
            // animation is weird and doesn't seem to have repeating pattern
            this.offsets.add(new int[] {2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2}); // frame 1
            this.offsets.add(new int[] {2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1});
            this.offsets.add(new int[] {1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1});
            this.offsets.add(new int[] {1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0});
            this.offsets.add(new int[] {0, 0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0});
            this.offsets.add(new int[] {0, 0, 0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1});
            this.offsets.add(new int[] {-1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1});
            this.offsets.add(new int[] {-2, -1, 0, 0, 0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2});
            this.offsets.add(new int[] {2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1});
            this.offsets.add(new int[] {-2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1});
            this.offsets.add(new int[] {0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0});
            this.offsets.add(new int[] {-1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0});
            this.offsets.add(new int[] {0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2, 2, 2, 1, 0});
            this.offsets.add(new int[] {0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2, 2, 2, 1});
            this.offsets.add(new int[] {-1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0});
            this.offsets.add(new int[] {2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2, 2});
            this.offsets.add(new int[] {2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2});
            this.offsets.add(new int[] {2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1});
            this.offsets.add(new int[] {-1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0});
            this.offsets.add(new int[] {0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0}); // 20
            this.offsets.add(new int[] {0, 0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0});
            this.offsets.add(new int[] {0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1});
            this.offsets.add(new int[] {-1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1});
            this.offsets.add(new int[] {-1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1});
            this.offsets.add(new int[] {-2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2}); // 25
            this.offsets.add(new int[] {-2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1});
            this.offsets.add(new int[] {1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0});
            this.offsets.add(new int[] {-1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0});
            this.offsets.add(new int[] {0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1});
            this.offsets.add(new int[] {0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2, 2, 2, 1});
            this.offsets.add(new int[] {1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2, 2, 2});
            this.offsets.add(new int[] {2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2, 2});
            this.offsets.add(new int[] {2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2});
            this.offsets.add(new int[] {2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1});
            this.offsets.add(new int[] {1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0});
            this.offsets.add(new int[] {1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0});
            this.offsets.add(new int[] {0, 0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0});
            this.offsets.add(new int[] {0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2, 2, 2, 1});
            this.offsets.add(new int[] {0, 0, 0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1});
            this.offsets.add(new int[] {-1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2}); // 40
            this.offsets.add(new int[] {-2, -1, 0, 0, 0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2});
            this.offsets.add(new int[] {-2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1});
            this.offsets.add(new int[] {-2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1});
            this.offsets.add(new int[] {0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0});
            this.offsets.add(new int[] {0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0});
            this.offsets.add(new int[] {0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1});
            this.offsets.add(new int[] {1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2});
            this.offsets.add(new int[] {1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1});
            this.offsets.add(new int[] {2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1});
            this.offsets.add(new int[] {1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1});
            this.offsets.add(new int[] {1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0});
            this.offsets.add(new int[] {0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0});
            this.offsets.add(new int[] {0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1});
            this.offsets.add(new int[] {-1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1});
            this.offsets.add(new int[] {-1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2}); // 55
            this.offsets.add(new int[] {-2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2});
            this.offsets.add(new int[] {-2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2});
            this.offsets.add(new int[] {2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1});
            this.offsets.add(new int[] {-1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0});
            this.offsets.add(new int[] {0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0});
            this.offsets.add(new int[] {0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1});
            this.offsets.add(new int[] {1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1});
            this.offsets.add(new int[] {0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2, 2, 2, 1});
            this.offsets.add(new int[] {2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2});
            this.offsets.add(new int[] {2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1});
            this.offsets.add(new int[] {1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1});
            this.offsets.add(new int[] {1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0});
            this.offsets.add(new int[] {1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2}); // 68
            this.offsets.add(new int[] {0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1});
            this.offsets.add(new int[] {-1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1});
            this.offsets.add(new int[] {-1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2});
            this.offsets.add(new int[] {-2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2});
            this.offsets.add(new int[] {-2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1});
            this.offsets.add(new int[] {-1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1});
            this.offsets.add(new int[] {-1, -2, -2, -2, -1, 0, 0, 0, 1, 2, 2, 2, 1, 0, 0, 0});
            this.offsets.add(new int[] {0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0});
            this.offsets.add(new int[] {0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1});
            this.offsets.add(new int[] {0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1});
            this.offsets.add(new int[] {1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2});
            this.offsets.add(new int[] {1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2}); // 80
            this.offsets.add(new int[] {2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1}); // stopped here
            this.offsets.add(new int[] {2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1});
            this.offsets.add(new int[] {1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0});
            this.offsets.add(new int[] {1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0});
            this.offsets.add(new int[] {0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0});
            this.offsets.add(new int[] {-1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1});
            this.offsets.add(new int[] {-2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1});
            this.offsets.add(new int[] {-2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2});
            this.offsets.add(new int[] {-2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1});
            this.offsets.add(new int[] {-2, -2, -2, -1, 0, 0, 0, 1, 2, 2, 2, 1, 0, 0, 0, -1}); // 90
            this.offsets.add(new int[] {-1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0});
            this.offsets.add(new int[] {0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0});
            this.offsets.add(new int[] {0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1});
            this.offsets.add(new int[] {1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1});
            this.offsets.add(new int[] {1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2});
            this.offsets.add(new int[] {1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2});
            this.offsets.add(new int[] {-2, -2, -1, 0, 0, 0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2});
            this.offsets.add(new int[] {1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1});
            this.offsets.add(new int[] {1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0});
            this.offsets.add(new int[] {0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0}); // 100
            this.offsets.add(new int[] {0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1});
            this.offsets.add(new int[] {0, 0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0});
            this.offsets.add(new int[] {-1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2});
            this.offsets.add(new int[] {-2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2});
            this.offsets.add(new int[] {-2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1});
            this.offsets.add(new int[] {-1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1});
            this.offsets.add(new int[] {0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1});
            this.offsets.add(new int[] {0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0});
            this.offsets.add(new int[] {0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1});
            this.offsets.add(new int[] {1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1}); // 110
            this.offsets.add(new int[] {1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2});
            this.offsets.add(new int[] {2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2});
            this.offsets.add(new int[] {2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1});
            this.offsets.add(new int[] {2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1});
            this.offsets.add(new int[] {1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0});
            this.offsets.add(new int[] {0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0});
            this.offsets.add(new int[] {0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0});
            this.offsets.add(new int[] {-1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1});
            this.offsets.add(new int[] {-1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1});
            this.offsets.add(new int[] {-2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2}); // 120
            this.offsets.add(new int[] {-2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1});
            this.offsets.add(new int[] {-1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1});
            this.offsets.add(new int[] {-1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1});
            this.offsets.add(new int[] {-1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0});
            this.offsets.add(new int[] {0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1});
            this.offsets.add(new int[] {-1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1});
            this.offsets.add(new int[] {1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2}); // 127
        }

        public String getCamera() {return "gui";}

        public int getLayer(){return this.layer;}

        String getShader(float level) {
            String shader = "precision mediump float;\n"

                                + "varying vec4 v_color;\n"
                                + "varying vec2 v_texCoords;\n"
                                + "uniform sampler2D u_texture;\n"
                                + "uniform mat4 u_projTrans;\n"

                                + "bool equals(float a, float b) {\n"
                                + "    return abs(a-b) < 0.0001;\n"
                                + "}\n"

                                + "void main() {\n"
                                + "    vec4 color = texture2D (u_texture, v_texCoords) * v_color;\n"

                                + "    float level = "+level+";\n" // can't do +- or -+ inline
                                + "    color = vec4(color.r+level, color.g+level, color.b+level, color.a);\n"
                                + "    gl_FragColor = color;\n"
                                + "}\n";
            return shader;
        }

        @Override
        public void step(Game game) {
            if (this.firstStep) {
                if (this.isNightShade) {
                    game.insertAction(new PlaySound("night_shade1", null));
                }
                else {
                    game.insertAction(new PlaySound("psychic1", null));
                }
                this.firstStep = false;
            }

            // Run through shaders, then offsets, then positions
            if (!this.shaderVals.isEmpty()) {
                this.currShaderVal = this.shaderVals.get(0);
                this.currShader = new ShaderProgram(this.vertexShader,
                                                    this.currShaderVal);
                game.uiBatch.setShader(this.currShader);
                this.shaderVals.remove(0);
            }
            else if (!this.offsets.isEmpty()) {
                this.currOffsets = this.offsets.get(0);
                // setting pixmap only once b/c otherwise get bad lag
                // would be nice to do every frame tho since there are moving rocks etc
                float heightM = (game.currScreen.y / 144); 
                if (this.pixmap == null) {
                    int offsetX = (int)((game.currScreen.x-((160*game.currScreen.y)/144))/2);  // x = (currWidth-160*currHeight/144)/2
                    this.pixmap = ScreenUtils.getFrameBufferPixmap(offsetX, 0, (int)game.currScreen.x-(offsetX*2), (int)game.currScreen.y);
                    // TODO: *3 b/c screen is scaled, alternative approach would be nice
//                    this.pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, 160*3, 144*3);
                }
                for (int j=0; j < 144-3; j++) { // 3 pixels from top
                    for (int i=0; i < 160; i++) {
                        // note: got better performance when creating a new Color here
                        // rather than using Color.set() on existing Color
                        this.sprite.setColor(new Color(this.pixmap.getPixel((int)(i*heightM), (int)(j*heightM))));
                        this.sprite.setPosition(i + this.currOffsets[j%16], j);
                        this.sprite.draw(game.uiBatch);
                    }
                }
                this.offsets.remove(0);
            }
            // TODO: remove
//            else if (!this.positions.isEmpty()) {
//                if (this.hitSound) {
//                    // play hit sound
//                    game.insertAction(new PlaySound("hit_normal1", null));
//                    this.hitSound = false;
//                }
//                this.currPosition = this.positions.get(0);
//                // TODO: *3 b/c screen is scaled, alternative approach would be nice
//                this.pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, 160*3, 144*3);
//                for (int j=0; j < 144; j++) {
//                    for (int i=0; i < 160; i++) {
//                        this.sprite.setColor(new Color(this.pixmap.getPixel(i*3, j*3)));
//                        // not using this.currPosition.y here, but may in future animations
//                        this.sprite.setPosition(i + this.currPosition.x, j + this.currPosition.y);
//                        this.sprite.draw(game.uiBatch);
//                    }
//                }
//                this.positions.remove(0);
//            }
            else {
                // TODO: remove if unused
//                // assign damage to target pkmn
//                int currHealth = this.target.currentStats.get("hp");
//                // TODO - correct damage calculation
//                int finalHealth = currHealth - this.power;
//                if (finalHealth < 0) {finalHealth = 0;} // make sure finalHealth isn't negative
//                this.target.currentStats.put("hp", finalHealth);

                game.actionStack.remove(this);
                game.insertAction(this.nextAction);
            }

        }
    }

    // ShadowClaw attack animation
    // gen1-ifying this attack anim
    static class ShadowClaw extends Action {
        ArrayList<Vector2> positions;
        Vector2 position;
        ArrayList<Vector2> screenPositions;
        Vector2 currPosition;
        ArrayList<Sprite> sprites;
        Sprite sprite;
        ArrayList<Integer> repeats;
        String sound;
        ArrayList<String> sounds;
        String currShaderVal;
        ArrayList<String> shaderVals = new ArrayList<String>();
        ShaderProgram currShader;
        String vertexShader;

        Sprite blockSprite;

        // TODO: has to be higher layer than ThrowOutPokemon()
        // why is ThrowOutPokemon in such a high layer?
        public int layer = 100;
        Sprite helperSprite; // just for helping me position the animation. delete later.

        Pokemon attacker;

        Pokemon target;

        Pixmap pixmap;
        // gen 7 properties
        // TODO: 1/8 critical hit chance
        int power = 70;

        int accuracy = 100;

        public ShadowClaw(Game game,
                          Pokemon attacker,
                          Pokemon target,
                          Action nextAction) {
            this.attacker = attacker;
            this.target = target;
            this.nextAction = nextAction;

            // single pixel sprite used for drawing the effect
            Texture text = new Texture(Gdx.files.internal("battle/pixel1.png"));
            this.blockSprite = new Sprite(text, 0, 0, 1, 1);

            text = new Texture(Gdx.files.internal("attacks/enemy_slash_sheet1.png"));

            // consider doing relative positions from now on
            this.position = new Vector2(16, 40);
            this.positions = new ArrayList<Vector2>();
            for (int i = 0; i < 7; i++) {
                this.positions.add(new Vector2(0,0));
            }

            // screen movement after attack
            this.screenPositions = new ArrayList<Vector2>();
            // 9 frames nothing
            for (int i = 0; i < 9; i++) {
                this.screenPositions.add(new Vector2(0,0));
            }
            // 5 frame move 2 right
            for (int i = 0; i < 5; i++) {
                this.screenPositions.add(new Vector2(2,0));
            }
            // 9 frame move back
            for (int i = 0; i < 9; i++) {
                this.screenPositions.add(new Vector2(0,0));
            }
            // 4 frame move 1 right
            for (int i = 0; i < 4; i++) {
                this.screenPositions.add(new Vector2(1,0));
            }
            // 19 frame move back
            for (int i = 0; i < 19; i++) {
                this.screenPositions.add(new Vector2(0,0));
            }

            this.sprites =  new ArrayList<Sprite>();
            this.sprites.add(null); // draw nothing
            this.sprites.add(null); // draw nothing
            for (int i = 0; i < 4; i++) {
                this.sprites.add(new Sprite(text, 48*i, 0, 48, 48));
            }
            this.sprites.add(null); // draw nothing

            this.repeats = new ArrayList<Integer>();
            // TODO: is triggerAction working like it should?
            this.repeats.add(20-1); // wait 20 frames
            this.repeats.add(40-1); // wait 20 frames
            for (int i = 0; i < 4; i++) {
                // 7 frames per image
                this.repeats.add(7-1);
            }
            this.repeats.add(6-1); // wait 6 frames

            // sounds to play
            this.sounds = new ArrayList<String>();
            this.sounds.add(null);
            this.sounds.add(null);
            this.sounds.add("slash1");
            for (int i = 0; i < 4; i++) {
                this.sounds.add(null);
            }

            this.vertexShader = "attribute vec4 a_position;\n"
                    + "attribute vec4 a_color;\n"
                    + "attribute vec2 a_texCoord;\n"
                    + "attribute vec2 a_texCoord0;\n"

                            + "uniform mat4 u_projTrans;\n"

                            + "varying vec4 v_color;\n"
                            + "varying vec2 v_texCoords;\n"

                            + "void main()\n"
                            + "{\n"
                            + "    v_color = a_color;\n"
                            + "    v_texCoords = a_texCoord0;\n"
                            + "    gl_Position =  u_projTrans * a_position;\n"
                            // below can be used to translate screen pixels (for attacks, etc
                            //                    + "    gl_Position =  u_projTrans * vec4(a_position.x, a_position.y, a_position.z, 1.0);\n"
                            + "}\n";

            float level = 0f;
            String normalShader = "precision mediump float;\n"
                    + "varying vec4 v_color;\n"
                    + "varying vec2 v_texCoords;\n"
                    + "uniform sampler2D u_texture;\n"
                    + "uniform mat4 u_projTrans;\n"

                                    + "bool equals(float a, float b) {\n"
                                    + "    return abs(a-b) < 0.0001;\n"
                                    + "}\n"

                                    + "void main() {\n"
                                    + "    vec4 color = texture2D (u_texture, v_texCoords) * v_color;\n"

                                    + "    float level = "+level+";\n" // can't do +- or -+ inline
                                    + "    color = vec4(color.r+level, color.g+level, color.b+level, color.a);\n"
                                    + "    gl_FragColor = color;\n"
                                    + "}\n";
            String inverseShader = "precision mediump float;\n"
                    + "varying vec4 v_color;\n"
                    + "varying vec2 v_texCoords;\n"
                    + "uniform sampler2D u_texture;\n"
                    + "uniform mat4 u_projTrans;\n"

                                    + "bool equals(float a, float b) {\n"
                                    + "    return abs(a-b) < 0.0001;\n"
                                    + "}\n"

                                    + "void main() {\n"
                                    + "    vec4 color = texture2D (u_texture, v_texCoords) * v_color;\n"

                                    + "    color = vec4(1-color.r, 1-color.g, 1-color.b, color.a);\n"
                                    + "    gl_FragColor = color;\n"
                                    + "}\n";
            this.shaderVals.add(inverseShader);
            for (int i = 0; i < 5; i++) {
                this.shaderVals.add(inverseShader);
            }
            this.shaderVals.add(normalShader);

            //            text = new Texture(Gdx.files.internal("attacks/enemy_slash/helper1.png"));
            //            this.helperSprite = new Sprite(text, 0, 0, 160, 144);

        }
        public String getCamera() {return "gui";}

        public int getLayer(){return this.layer;}

        @Override
        public void step(Game game) {
            // set sprite position
            // if done with anim, do nextAction
            if (this.repeats.isEmpty()) {
                if (!this.screenPositions.isEmpty()) {
                    this.currPosition = this.screenPositions.get(0);
                    // screen wiggle
                    // TODO: *3 b/c screen is scaled, alternative approach would be nice
                    this.pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, 160*3, 144*3);
                    for (int j=0; j < 144; j++) {
                        for (int i=0; i < 160; i++) {
                            this.blockSprite.setColor(new Color(this.pixmap.getPixel(i*3, j*3)));
                            // not using this.currPosition.y here, but may in future animations
                            this.blockSprite.setPosition(i + this.currPosition.x, j + this.currPosition.y);
                            this.blockSprite.draw(game.uiBatch);
                        }
                    }
                    this.screenPositions.remove(0);
                }
                else {
//                    // assign damage to target pkmn
//                    int currHealth = this.target.currentStats.get("hp");
//                    // TODO - correct damage calculation
//                    int finalHealth = currHealth - this.power;
//                    if (finalHealth < 0) {finalHealth = 0;} // make sure finalHealth isn't negative
//                    this.target.currentStats.put("hp", finalHealth);

                    game.actionStack.remove(this);
                    game.insertAction(this.nextAction);
                }
                return;
            }

            // get next sound, play it
            this.sound = this.sounds.get(0);
            if (this.sound != null) {
                game.insertAction(new PlaySound(this.sound, null));
                this.sounds.set(0, null); // don't play same sound over again
            }

            // get next frame
            this.sprite = sprites.get(0);

            // debug
            //            this.helperSprite.draw(game.floatingBatch);

            // draw current sprite
            if (this.sprite != null) {
                this.sprite.setPosition(position.x, position.y);
                this.sprite.draw(game.uiBatch);
            }

            // debug
            //            if (this.repeats.size() == 5) {
            //                return;
            //            }

            // repeat sprite/pos for current object for 'frames[0]' number of frames.
            if (this.repeats.get(0) > 0) {
                this.repeats.set(0, this.repeats.get(0) - 1);
            }
            else {
                // since position is relative, only update once each time period
                this.position = this.position.add(positions.get(0));
                positions.remove(0);
                sprites.remove(0);
                repeats.remove(0);
                sounds.remove(0);

                this.currShaderVal = this.shaderVals.get(0);
                this.currShader = new ShaderProgram(this.vertexShader,
                                                    this.currShaderVal);
                game.uiBatch.setShader(this.currShader);
                this.shaderVals.remove(0);
            }
        }
    }

    // Slash attack animation
    static class Slash extends Action {
        ArrayList<Vector2> positions;
        Vector2 position;
        ArrayList<Vector2> screenPositions;
        Vector2 currPosition;
        ArrayList<Sprite> sprites;
        Sprite sprite;
        ArrayList<Integer> repeats;
        String sound;
        ArrayList<String> sounds;

        Sprite blockSprite;

        // TODO: has to be higher layer than ThrowOutPokemon()
        // why is ThrowOutPokemon in such a high layer?
                                                       public int layer = 108;
                                                       Sprite helperSprite; // just for helping me position the animation. delete later.

                                                       Pokemon attacker;

                                                       Pokemon target;

                                                       Pixmap pixmap;
                                                       // gen 7 properties
                                                       int power = 70;

                                                       int accuracy = 100;

                                                       public Slash(Game game,
                                                                    Pokemon attacker,
                                                                    Pokemon target,
                                                                    Action nextAction) {
                                                           this.attacker = attacker;
                                                           this.target = target;
                                                           this.nextAction = nextAction;

                                                           // single pixel sprite used for drawing the effect
                                                           Texture text = new Texture(Gdx.files.internal("battle/pixel1.png"));
                                                           this.blockSprite = new Sprite(text, 0, 0, 1, 1);

                                                           text = new Texture(Gdx.files.internal("attacks/enemy_slash_sheet1.png"));

                                                           // consider doing relative positions from now on
                                                           this.position = new Vector2(16, 40);
                                                           this.positions = new ArrayList<Vector2>();
                                                           for (int i = 0; i < 6; i++) {
                                                               this.positions.add(new Vector2(0,0));
                                                           }

                                                           // screen movement after attack
                                                           this.screenPositions = new ArrayList<Vector2>();
                                                           // 9 frames nothing
                                                           for (int i = 0; i < 9; i++) {
                                                               this.screenPositions.add(new Vector2(0,0));
                                                           }
                                                           // 5 frame move 2 right
                                                           for (int i = 0; i < 5; i++) {
                                                               this.screenPositions.add(new Vector2(2,0));
                                                           }
                                                           // 9 frame move back
                                                           for (int i = 0; i < 9; i++) {
                                                               this.screenPositions.add(new Vector2(0,0));
                                                           }
                                                           // 4 frame move 1 right
                                                           for (int i = 0; i < 4; i++) {
                                                               this.screenPositions.add(new Vector2(1,0));
                                                           }
                                                           // 19 frame move back
                                                           for (int i = 0; i < 19; i++) {
                                                               this.screenPositions.add(new Vector2(0,0));
                                                           }

                                                           this.sprites =  new ArrayList<Sprite>();
                                                           this.sprites.add(null); // draw nothing
                                                           for (int i = 0; i < 4; i++) {
                                                               this.sprites.add(new Sprite(text, 48*i, 0, 48, 48));
                                                           }
                                                           this.sprites.add(null); // draw nothing

                                                           this.repeats = new ArrayList<Integer>();
                                                           // TODO: is triggerAction working like it should?
                                                           this.repeats.add(20-1); // wait 20 frames
                                                           for (int i = 0; i < 4; i++) {
                                                               // 7 frames per image
                                                               this.repeats.add(7-1);
                                                           }
                                                           this.repeats.add(6-1); // wait 6 frames

                                                           // sounds to play
                                                           this.sounds = new ArrayList<String>();
                                                           this.sounds.add("slash1");
                                                           for (int i = 0; i < 5; i++) {
                                                               this.sounds.add(null);
                                                           }

                                                           //            text = new Texture(Gdx.files.internal("attacks/enemy_slash/helper1.png"));
                                                           //            this.helperSprite = new Sprite(text, 0, 0, 160, 144);

                                                       }
                                                       public String getCamera() {return "gui";}

                                                       public int getLayer(){return this.layer;}

                                                       @Override
                                                       public void step(Game game) {
                                                           // set sprite position
                                                           // if done with anim, do nextAction
                                                           if (this.repeats.isEmpty()) {
                                                               if (!this.screenPositions.isEmpty()) {
                                                                   this.currPosition = this.screenPositions.get(0);
                                                                   // screen wiggle
                                                                   // TODO: *3 b/c screen is scaled, alternative approach would be nice
                                                                   this.pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, 160*3, 144*3);
                                                                   for (int j=0; j < 144; j++) {
                                                                       for (int i=0; i < 160; i++) {
                                                                           this.blockSprite.setColor(new Color(this.pixmap.getPixel(i*3, j*3)));
                                                                           // not using this.currPosition.y here, but may in future animations
                                                                           this.blockSprite.setPosition(i + this.currPosition.x, j + this.currPosition.y);
                                                                           this.blockSprite.draw(game.uiBatch);
                                                                       }
                                                                   }
                                                                   this.screenPositions.remove(0);
                                                               }
                                                               else {
                                                                   // assign damage to target pkmn
                                                                   int currHealth = this.target.currentStats.get("hp");
                                                                   // TODO - correct damage calculation
                                                                   int finalHealth = currHealth - this.power;
                                                                   if (finalHealth < 0) {finalHealth = 0;} // make sure finalHealth isn't negative
                                                                   this.target.currentStats.put("hp", finalHealth);

                                                                   game.actionStack.remove(this);
                                                                   game.insertAction(this.nextAction);
                                                               }
                                                               return;
                                                           }

                                                           // get next sound, play it
                                                           this.sound = this.sounds.get(0);
                                                           if (this.sound != null) {
                                                               game.insertAction(new PlaySound(this.sound, null));
                                                               this.sounds.set(0, null); // don't play same sound over again
                                                           }

                                                           // get next frame
                                                           this.sprite = sprites.get(0);

                                                           // debug
                                                           //            this.helperSprite.draw(game.floatingBatch);

                                                           // draw current sprite
                                                           if (this.sprite != null) {
                                                               this.sprite.setPosition(position.x, position.y);
                                                               this.sprite.draw(game.uiBatch);
                                                           }

                                                           // debug
                                                           //            if (this.repeats.size() == 5) {
                                                           //                return;
                                                           //            }

                                                           // repeat sprite/pos for current object for 'frames[0]' number of frames.
                                                           if (this.repeats.get(0) > 0) {
                                                               this.repeats.set(0, this.repeats.get(0) - 1);
                                                           }
                                                           else {
                                                               // since position is relative, only update once each time period
                                                               this.position = this.position.add(positions.get(0));
                                                               positions.remove(0);
                                                               sprites.remove(0);
                                                               repeats.remove(0);
                                                               sounds.remove(0);

                                                           }
                                                       }
    }
}

// TODO - remove all 'post scaling change' commented lines.

// TODO - bug where a caught pokemon will still be in the wild

class AttackAnim extends Action {
    public int layer = 500;  // ensure that this triggers before other actions
    Attack attack;

    boolean isFriendly;
    public AttackAnim(Game game, Attack attack, boolean isFriendly, Action nextAction) {
        this.nextAction = nextAction;
        this.attack = attack;
        this.isFriendly = isFriendly;
    }

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        game.actionStack.remove(this);
        Action action = Battle.getAttackAction(game, attack, isFriendly, null);
        
        // Resolve poison/burn
        // TODO: friendlyPokemon/enemyPokemon are duplicates of stuff in getAttackAction()
        Pokemon friendlyPokemon;
        Pokemon enemyPokemon;
        if (isFriendly) {
            friendlyPokemon = game.player.currPokemon;
            enemyPokemon = game.battle.oppPokemon;
        }
        else {
            friendlyPokemon = game.battle.oppPokemon;
            enemyPokemon = game.player.currPokemon;
        }
        String enemy = isFriendly ? "" : "Enemy ";
        if (friendlyPokemon.status != null) {
            if (friendlyPokemon.status.equals("poison")) {
                // Poison damage is 1/8 of hp
                int damage = friendlyPokemon.maxStats.get("hp")/8;
                if (damage < 1) {
                    damage = 1;
                }
                action.append(new DisplayText.Clear(game,
                              new WaitFrames(game, 3,
                              new DisplayText(game, enemy+friendlyPokemon.name.toUpperCase()+"' hurt by poison!", null, true, true,
                              new Battle.LoadAndPlayAnimation(game, "status_poison", friendlyPokemon,
                              Battle.depleteHealth(game, !isFriendly, damage,
                              new WaitFrames(game, 13,
                              null)))))));
            }
            else if (friendlyPokemon.status.equals("burn")) {
                // Burn damage is 1/8 of hp
                int damage = friendlyPokemon.maxStats.get("hp")/8;
                if (damage < 1) {
                    damage = 1;
                }
                action.append(new DisplayText.Clear(game,
                              new WaitFrames(game, 3,
                              new DisplayText(game, enemy+friendlyPokemon.name.toUpperCase()+"' hurt by it' burn!", null, true, false,
                              Battle.depleteHealth(game, !isFriendly, damage,
                              new WaitFrames(game, 13,
                              null))))));
            }
            else if (friendlyPokemon.status.equals("toxic")) {
                // TODO: toxic reverts to regular poison in gen2 if switched out
                // also reverts to regular poison after battle
                // Should I mimic this?
                int damage = (friendlyPokemon.maxStats.get("hp")*friendlyPokemon.statusCounter)/16;
                if (damage < 1) {
                    damage = 1;
                }
                action.append(new DisplayText.Clear(game,
                              new WaitFrames(game, 3,
                              new DisplayText(game, enemy+friendlyPokemon.name.toUpperCase()+"' hurt by poison!", null, true, true,
                                              new Battle.LoadAndPlayAnimation(game, "status_poison", friendlyPokemon,
                              Battle.depleteHealth(game, !isFriendly, damage,
                              new WaitFrames(game, 13,
                              null)))))));
                friendlyPokemon.statusCounter++;
            }
        }
        action.append(nextAction);
        game.insertAction(action);
    }
}

public class Battle {
    /**
     * Calculate an attack's damage. Based off of Gen 2 mechanics.
     */
    static int gen2CalcDamage(Pokemon source, Attack attack, Pokemon target) {
        if (attack.name.equals("Mewtwo_Special1")) {
            return 30;
        }
        // TODO: remove if unused
//        if (attack.power == 0) {
//            // TODO: not sure if there are corner cases or not
//            // 0 power attacks include defense curl, swords dance, etc.
//            return 0;
//        }
        int power = attack.power;
        if (attack.name.equals("dragon energy")) {
            power = (power * source.currentStats.get("hp"))/source.maxStats.get("hp");
            if (power < 1) {
                power = 1;
            }
        }
        // https://bulbapedia.bulbagarden.net/wiki/Crush_Grip_(move)#Generation_IV
        // Using gen 3 formula
        else if (attack.name.equals("crush grip")) {
            power = 1 + ((120 * target.currentStats.get("hp"))/target.maxStats.get("hp"));
            System.out.println(power);
        }
        int attackStat = attack.isPhysical ? source.currentStats.get("attack") : source.currentStats.get("specialAtk");
        int defenseStat = attack.isPhysical ? target.currentStats.get("defense") : target.currentStats.get("specialDef");
        int damage = (int)Math.floor(Math.floor(Math.floor(2 * source.level / 5 + 2) * attackStat * power / defenseStat) / 50) + 2;
        if (source.types.contains(attack.type.toUpperCase())) {
            damage = (int)(damage * 1.5f);
        }  // STAB
        // Factor in type effectiveness
        float multiplier = 1f;
        String prevType = "";
        for (String type : target.types){
            if (type.equals(prevType)) {
                continue;
            }
            prevType = type;
            multiplier *= Game.staticGame.battle.gen2TypeEffectiveness.get(attack.type).get(type.toLowerCase());
        }
        damage = (int)(damage * multiplier);
        if (attack.isCrit) {
            // TODO: ignore reflect, etc.
            // https://web.archive.org/web/20140712063943/http://www.upokecenter.com/content/pokemon-gold-version-silver-version-and-crystal-version-timing-notes
            damage *= 2;
        }
        return damage;
    }

    /**
     * Calculate an attack's damage. Based off of Gen 2 mechanics.
     * 
     * Source: https://web.archive.org/web/20140712063943/http://www.upokecenter.com/content/pokemon-gold-version-silver-version-and-crystal-version-timing-notes
     */
    static boolean gen2DetermineCrit(Pokemon source, Attack attack) {
        int c = 0;
        // TODO: focus energy (use boolean in pokemon class)
        // If attack is Aero Blast, Crab Hammer, Cross Chop, Karate Chop, Razor Leaf, or Slash, add 2 to C. 
        // TODO this results in a 1/4 crit ratio, which is different than other generations
        if (attack.name.equals("aero blast") || attack.name.equals("crabhammer") || attack.name.equals("cross chop") ||
            attack.name.equals("karate chop") || attack.name.equals("razor leaf") || attack.name.equals("slash")) {
//        if (attack != null && critAttacks.contains(attack));
            c += 2;
        }
        // TODO: using gen 8 mechanics here, I think
        else if (attack.name.equals("air cutter"))  {
            c += 1;
        } 
        if (c == 0) {
            c = 17;
        }
        else if (c == 1) {
            c = 32;
        }
        else if (c == 2) {
            c = 64;
        }
        else if (c == 3) {
            c = 85;
        }
        else {
            c = 128;
        }
        return Game.staticGame.map.rand.nextInt(256) < c;
    }

    // your pokemon // probly use this
    // Pokemon yourPokemon;

    /**
     * TODO: The below has modifications making it inaccurate (re: adrenaline mechanic)
     * Also, capture probability should be calculated separate from Actions.
     * 
     * Probably stash the below code, and make a new function gen1CalcIfCaught(). Returns number of wobbles.
     */
    // TODO: this catch calculator is for gen1 only, also want gen2
    // dupe of a fn in draw safari menu action, put here bc the one in safari menu has demo code
    @Deprecated
    public static Action calcIfCaught(Game game, Action nextAction) {

        // using http:// bulbapedia.bulbagarden.net/wiki/Catch_rate#Capture_method_.28Generation_I.29
         // also use http:// www.dragonflycave.com/safarizone.aspx
        // not sure where 'ball used' will be stored. probly some inventory location, like currItem (in inventory)

        int maxRand = 150; // different per-ball
        int randomNum = game.map.rand.nextInt(maxRand+1); //+1 to include upper bound
        int statusValue = 0; // different depending on oppPokemon's status
        boolean breaksFree = false;

        int ball = 15; // 8 if great ball
        // demo code
        int adrenaline = game.player.adrenaline;
        if (adrenaline > 25) {
            adrenaline = 25;
        }
        // ball = ball - adrenaline;
        int modFactor = 100;// 128; - want 5 adr to catch all easy, but not medium or hard.
        int f = (int)Math.floor((game.battle.oppPokemon.currentStats.get("catchRate") * 255 * 4) / (modFactor*ball)); // modify 128 to make game harder

        // int f = (int)Math.floor((game.battle.oppPokemon.maxStats.get("hp") * 255 * 4) / (game.battle.oppPokemon.currentStats.get("hp") * ball));

        // left out calculation here based on status values
         // demo - leave out status value
        // notes - adr seems to take effec too fast. also, pkmn in general are too hard to catch
         // at beginning. shift factor down, and make adr*10
        if (randomNum - statusValue > game.battle.oppPokemon.currentStats.get("catchRate") && false) {
            breaksFree = true;
            System.out.println("(randomNum - statusValue / catchRate): ("+String.valueOf(randomNum - statusValue)+" / "+String.valueOf(game.battle.oppPokemon.currentStats.get("catchRate"))+")");
        }
        else {
            int randomNum_M = game.map.rand.nextInt(255+1);


            // randomNum_M = randomNum_M - adrenaline*20;


            if (f+(adrenaline*10) >= randomNum_M) { // demo code
                breaksFree = false;
            }
            else {
                breaksFree = true;
            }
            System.out.println("(randomNum_M / f / adr): ("+String.valueOf(randomNum_M)+" / "+String.valueOf(f)+" / +"+String.valueOf(adrenaline*10)+")");
        }

        // simplify and put above
        if (breaksFree == false) { // ie was caught
            return new CatchPokemonWobblesThenCatch(game, nextAction);
        }

        // else, ie breaksFree = true
        int d = game.battle.oppPokemon.currentStats.get("catchRate") * 100 / maxRand;
                //, where the value of Ball is 255 for the Poké Ball, 200 for the Great Ball, or 150 for other balls
        if (d >= 256) {
            // shake 3 times before breaking free
            return new CatchPokemonWobbles3Times(game, new PrintAngryEating(game, new ChanceToRun(game, nextAction) ) );
        }

        int s = 0;// status thing again
        int x = d * f / 255 + s;

        if (x < 10) {
            // ball misses
            return new CatchPokemonMiss(game, new PrintAngryEating(game, new ChanceToRun(game, nextAction) ) );
        }
        else if (x < 30) {
            // ball shakes once
            return new CatchPokemonWobbles1Time(game, new PrintAngryEating(game, new ChanceToRun(game, nextAction) ) );
        }
        else if (x < 70) {
            // ball shakes twice
            return new CatchPokemonWobbles2Times(game, new PrintAngryEating(game, new ChanceToRun(game, nextAction) ) );
        }
        // ball shakes three times before pkmn gets free

        // System.out.println("x: "+String.valueOf(x));
        // System.out.println("Shake three times: "+String.valueOf(x));

        return new CatchPokemonWobbles3Times(game, new PrintAngryEating(game, new ChanceToRun(game, nextAction) ) );
    }

    /**
     * Determine if pokeball capture was successful or not (Gen 2).
     * 
     * Returns -1 if pokemon is caught.
     *
     * Sources:
     * https:// bulbapedia.bulbagarden.net/wiki/Catch_rate#Capture_method_.28Generation_II.29
     * https:// www.dragonflycave.com/mechanics/gen-ii-capturing
     */
    public static int gen2CalcIfCaught(Game game, Pokemon pokemon, String ballUsed) {
        ballUsed = ballUsed.toLowerCase();

        if (ballUsed.equals("master ball")) {
            return -1;
        }
        int rateModified = pokemon.maxStats.get("catchRate");
        if (ballUsed.equals("great ball") || ballUsed.equals("park ball")) {
            rateModified *= 1.5;
        }
        else if (ballUsed.equals("ultra ball")) {
            rateModified *= 2;
        }
        else if (ballUsed.equals("dusk ball")) {
            // 3x is the gen 8 effect, previous gens was 3.5
            // https://bulbapedia.bulbagarden.net/wiki/Dusk_Ball
            if (game.map.timeOfDay.equals("night") || game.map.tiles != game.map.overworldTiles) {
                rateModified *= 3.5;
                System.out.println("used dusk ball");  // TODO: remove
            }
        }
        else if (ballUsed.equals("fast ball") && (pokemon.name.equals("tangela") || 
                                                  pokemon.name.equals("grimer")  || 
                                                  pokemon.name.equals("regieleki")  ||  // TODO: not sure if approp
                                                  pokemon.name.equals("magnemite"))) {
            rateModified *= 4;
        }
        
        // TODO: lure ball
        // TODO: heavy ball is just a list of heavy pokemon for now (maybe check weight later)
        // TODO: what was friend ball supposed to do?
        else if (ballUsed.equals("heavy ball")) {
            String[] lbs903 = new String[]{"snorlax", "regigigas"};
            String[] lbs677 = new String[]{"steelix"};
            String[] lbs451 = new String[]{"golem",
                                           "registeel",
                                           "dragonite",
                                           "onix",
                                           "lugia",
                                           "lapras",
                                           "regirock",
                                           "gyarados",
                                           "hariyama",
                                           "rhyperior",
                                           "mamoswine"};
            String[] lbs225 = new String[]{"crustle",
                                           "regidrago",
                                           "tyranitaur",
                                           "ho-oh",
                                           "regice",
                                           "pupitar",
                                           "arcanine",
                                           "graveler",
                                           "rhyhorn",
                                           "scizor",
                                           "exeggutor",
                                           "mewtwo",
                                           "forretress",
                                           "ursaring",
                                           "machamp",
                                           "cloyster",
                                           "regieleki"};
            int foundRate = rateModified -20;
            for (String name : lbs903) {
                if (name.equals(pokemon.name)) {
                    foundRate = rateModified +40;
                    break;
                }
            }
            for (String name : lbs677) {
                if (name.equals(pokemon.name)) {
                    foundRate = rateModified +30;
                    break;
                }
            }
            for (String name : lbs451) {
                if (name.equals(pokemon.name)) {
                    foundRate = rateModified +20;
                    break;
                }
            }
            for (String name : lbs225) {
                if (name.equals(pokemon.name)) {
                    foundRate = rateModified;
                    break;
                }
            }
            rateModified = foundRate;
        }
        else if (ballUsed.equals("level ball")) {
            if (game.player.currPokemon.level / 4 > pokemon.level) {
                rateModified *= 8;
            }
            else if (game.player.currPokemon.level / 2 > pokemon.level) {
                rateModified *= 4;
            }
            else if (game.player.currPokemon.level > pokemon.level) {
                rateModified *= 2;
            }
        }
        else if (ballUsed.equals("love ball")) {
            // TODO: this is using the 'fixed' version, where pokemon
            //       have to be opposite genders.
            if (!pokemon.gender.equals("unknown") && 
                !game.player.currPokemon.gender.equals("unknown") &&
                !game.player.currPokemon.gender.equals(pokemon.gender)) {
                rateModified *= 8;
            }
        }
        else if (ballUsed.equals("moon ball")) {
            // TODO: can't remember any more
            String[] moonStoneMons = new String[]{"clefairy",
                                                  "jigglypuff",
                                                  "nidorina",
                                                  "nidorino"};
            for (String name : moonStoneMons) {
                if (pokemon.name.equals(name)) {
                    rateModified *= 4;
                    break;
                }
            }
        }

        if (rateModified > 255){
            rateModified = 255;
        }
        int bonusStatus = 0;
        if (pokemon.status != null) {
            if (pokemon.status.equals("sleep") || pokemon.status.equals("freeze")) {
                bonusStatus = 10;
            }
            // TODO: enable for para, poison etc if changed in advanced settings menu?
//            else {
//                bonusStatus = 5;
//            }
        }
        int m = 3*pokemon.maxStats.get("hp");
        int h = 2*pokemon.currentStats.get("hp");
        if (m > 255) {
            m = m/2;
            m = m/2;
            h = h/2;
            h = h/2;
        }
        int a = ((m - h)*rateModified)/m;
        if (a < 1) {
            a = 1;
        }
        a += bonusStatus;
        if (a > 255) {
            a = 255;
        }
        // Determine if pokemon is caught or not.
        System.out.println(a);
        if (game.map.rand.nextInt(256) <= a) {
            return -1;
        }
        int[] aLookup = new int[]{1, 2, 3, 4, 5, 7, 10, 15, 20, 30, 40, 50, 60, 80, 100, 120, 140, 160, 180, 200, 220, 240, 254, 255};
        int[] bLookup = new int[]{63, 75, 84, 90, 95, 103, 113, 126, 134, 149, 160, 169, 177, 191, 201, 211, 220, 227, 234, 240, 246, 251, 253, 255};
        int i = 0;
        int prevVal = -1;
        for (int val : aLookup) {
            if (a > prevVal && a <= val) {
                break;
            }
            prevVal = val;
            i++;
        }
        int b = bLookup[i];
        if (game.map.rand.nextInt(256) >= b) {
            return 0;  // 0 shakes
        }
        if (game.map.rand.nextInt(256) >= b) {
            return 1;  // 1 shakes
        }
        if (game.map.rand.nextInt(256) >= b) {
            return 2;  // 2 shakes
        }
        return 3;  // 3 shakes
    }

    public static Action getAttackAction(Game game, Attack attack, boolean isFriendly, Action nextAction) {
        // construct default attack?
        // TODO: the non-loaded ones are broken now, need to do DisplayText.Clear()
        int power = 40;
        int accuracy = 100;

        if (attack.name.equals("Aurora Beam")) {
            // normally return new attack here
            power = 65; accuracy = 100;
        }
        else if (attack.name.equals("Clamp")) {
            // normally return new attack here
            power = 35; accuracy = 85;
        }
        else if (attack.name.equals("Supersonic")) {
            // normally return new attack here
            power = 0; accuracy = 55;
        }
        else if (attack.name.equals("Withdraw")) {
            // normally return new attack here
            power = 20; accuracy = 100;
        }
        else if (attack.name.equals("Struggle")) {
            // normally return new attack here
            power = 50; accuracy = 100;
        }
        else if (attack.name.equals("Psychic")) {
            if (isFriendly) {
                return new Attack.Psychic(game,
//                                          game.player.currPokemon,
                                          game.battle.oppPokemon,
                                          false, nextAction);
            }
            else {
                return new Attack.Psychic(game,
//                                          game.battle.oppPokemon,
                                          game.player.currPokemon,
                                          false, nextAction);
            }
        }
        else if (attack.name.equals("Mewtwo_Special1")) {
            return new Attack.Mewtwo_Special1(game,
                                              game.battle.oppPokemon,
                                              game.player.currPokemon,
                                              nextAction);
        }
        else if (attack.name.equals("Night Shade")) {
            if (isFriendly) {
                return new Attack.Psychic(game,
//                                          game.player.currPokemon,
                                          game.battle.oppPokemon,
                                          true, nextAction);
            }
            else {
                return new Attack.Psychic(game,
//                                          game.battle.oppPokemon,
                                          game.player.currPokemon,
                                          true, nextAction);
            }
        }
        else if (attack.name.equals("Slash")) {
            if (isFriendly) {
                return new Attack.Default(game, power, accuracy, nextAction);
            }
            else {
                return new Attack.Slash(game, game.battle.oppPokemon, game.player.currPokemon, nextAction);
            }
        }
        else if (attack.name.equals("Shadow Claw")) {
            if (isFriendly) {
                // TODO
                return new Attack.Default(game, power, accuracy, nextAction);
            }
            else {
                return new Attack.ShadowClaw(game, game.battle.oppPokemon, game.player.currPokemon, nextAction);
            }
        }
        else if (attack.name.equals("Lick")) {
            if (isFriendly) {
                // TODO
                return new Attack.Default(game, power, accuracy, nextAction);
            }
            else {
                return new Attack.Lick(game, game.battle.oppPokemon, game.player.currPokemon, nextAction);
            }
        }
        else {
//            if (game.battle.oppPokemon.name.equals("Mewtwo")) {
            if (SpecialMewtwo1.class.isInstance(game.battle.oppPokemon) && !isFriendly) {
                SpecialBattleMewtwo.specialAttackCounter++;
                if (SpecialBattleMewtwo.specialAttackCounter >= 3) {
                    Attack mewtwoSpecial1 = game.battle.attacks.get("Mewtwo_Special1");
                    mewtwoSpecial1.damage = Battle.gen2CalcDamage(game.battle.oppPokemon, mewtwoSpecial1, game.player.currPokemon);
                    nextAction = new DisplayText.Clear(game,
                                 new WaitFrames(game, 3,
                                 new DisplayText(game, "A wave of psychic power unleashes!", null, true, false,
                                 Battle.getAttackAction(game, mewtwoSpecial1, !isFriendly,
                                 new DepleteFriendlyHealth(game.player.currPokemon, mewtwoSpecial1.damage,
                                 new WaitFrames(game, 30,
                                 new DisplayText.Clear(game,
                                 new WaitFrames(game, 3, nextAction))))))));
                    SpecialBattleMewtwo.specialAttackCounter = 0;
                }
            }
            Pokemon friendlyPokemon;
            Pokemon enemyPokemon;
            if (isFriendly) {
                friendlyPokemon = game.player.currPokemon;
                enemyPokemon = game.battle.oppPokemon;
            }
            else {
                friendlyPokemon = game.battle.oppPokemon;
                enemyPokemon = game.player.currPokemon;
            }
            // This is just so that I can append to it below.
            // Blank action will just immediately insert next Action and step()
            Action attackAction = new Action() {
                public String getCamera() {
                    return "gui";
                }
            };
            String enemy = isFriendly ? "" : "Enemy ";
            // Handle disable.
            if (friendlyPokemon.disabledCounter > 0) {
                friendlyPokemon.disabledCounter--;
            }
            if (friendlyPokemon.disabledCounter <= 0 && friendlyPokemon.disabledIndex != -1) {
                attackAction.append(new DisplayText.Clear(game,
                                    new WaitFrames(game, 3,
                                    new DisplayText(game, enemy+friendlyPokemon.name.toUpperCase()+"' "+friendlyPokemon.attacks[friendlyPokemon.disabledIndex]+" is no longer disabled!", null, true, false,
                                    null))));
                friendlyPokemon.disabledIndex = -1;
            }

            if (friendlyPokemon.flinched) {
                return new DisplayText.Clear(game,
                       new WaitFrames(game, 3,
                       new DisplayText(game, enemy+friendlyPokemon.name.toUpperCase()+" flinched!", null, true, true,
                       new WaitFrames(game, 10,
                       nextAction))));
            }

            // Status check - display appropriate text if asleep, confused, etc
            // If snap out of status (confusion, sleep), then proceed normally.
            if (friendlyPokemon.status != null) {
                if (friendlyPokemon.status.equals("sleep")) {
                    friendlyPokemon.statusCounter--;
                    if (friendlyPokemon.statusCounter > 0) {
                        return new DisplayText.Clear(game,
                               new WaitFrames(game, 3,
                               new DisplayText(game, enemy+friendlyPokemon.name.toUpperCase()+" is fast asleep!", null, true, true,
                               // used attack_anims\hypnosis_player_gsc\frames
                               new WaitFrames(game, 23,
                               new LoadAndPlayAnimation(game, "status_sleep", friendlyPokemon,
                               nextAction)))));
                    }
//                    friendlyPokemon.status = null;
                    attackAction.append(new DisplayText.Clear(game,
                                        new WaitFrames(game, 3,
                                        new DisplayText(game, enemy+friendlyPokemon.name.toUpperCase()+" woke up!", null, true, true,
                                        new SetField(friendlyPokemon, "status", null,  // Delay to emulate vgc
                                        null)))));
                }
                else if (friendlyPokemon.status.equals("paralyze")) {
                    if (game.map.rand.nextInt(256) < 128) {
                        return new DisplayText.Clear(game,
                               new WaitFrames(game, 3,
                               new DisplayText(game, enemy+friendlyPokemon.name.toUpperCase()+" is fully paralyzed!", null, true, true,
                               nextAction)));
                    }
                }
                else if (friendlyPokemon.status.equals("freeze")) {
                    // 20% chance to thaw each turn
                    // - Gen 1 is no thaw.
                    if (game.map.rand.nextInt(256) >= 51) {  // roughly 80 percent
                        return new DisplayText.Clear(game,
                               new WaitFrames(game, 3,
                               new DisplayText(game, enemy+friendlyPokemon.name.toUpperCase()+" is frozen solid!", null, true, true,
                               nextAction)));
                    }
//                    friendlyPokemon.status = null;
                    attackAction.append(new DisplayText.Clear(game,
                                        new WaitFrames(game, 3,
                                        new DisplayText(game, enemy+friendlyPokemon.name.toUpperCase()+" thawed out!", null, true, true,
                                        new SetField(friendlyPokemon, "status", null,
                                        null)))));
                }
                else if (friendlyPokemon.status.equals("confuse")) {
                    friendlyPokemon.statusCounter--;
                    if (friendlyPokemon.statusCounter > 0) {
                        attackAction.append(new DisplayText.Clear(game,
                                            new WaitFrames(game, 3,
                                            new DisplayText(game, enemy+friendlyPokemon.name.toUpperCase()+" is confused!", null, true, true,
                                            // Used attack_anims/supersonic_enemy_gsc/is_confused_test.avi
                                            new WaitFrames(game, 24,
                                            new LoadAndPlayAnimation(game, "status_confuse", friendlyPokemon,
                                            null))))));

                        if (game.map.rand.nextInt(256) < 128) {
                            // TODO: what is the damage calc here? Assuming stab, type effectiveness etc all apply.
                            // https://bulbapedia.bulbagarden.net/wiki/Status_condition
                            // "The damage is done as if the Pokémon attacked itself with a 40-power typeless physical attack"
                            // TODO: can't handle typeless. Probably need a 'typeless' attack specially stored.
                            Attack confustionAtk = game.battle.attacks.get("confusion_hit");
                            confustionAtk.damage = Battle.gen2CalcDamage(friendlyPokemon, confustionAtk, friendlyPokemon);
                            attackAction.append(new DisplayText.Clear(game,
                                                new WaitFrames(game, 3,
                                                new DisplayText(game, "It hurt itself in confusion!", null, true, false,
                                                Battle.depleteHealth(game, !isFriendly, confustionAtk.damage,
                                                new WaitFrames(game, 13,
                                                nextAction))))));
                            return attackAction;
                        }
                    }
                    else {
                        friendlyPokemon.status = null;
                        attackAction.append(new DisplayText.Clear(game,
                                            new WaitFrames(game, 3,
                                            new DisplayText(game, enemy+friendlyPokemon.name.toUpperCase()+" snapped out of confusion!", null, true, true,
                                            null))));
                    }
                }
                // TODO: remove
//                attackAction.append(new WaitFrames(game, 30, null));
            }
            
            // Set previous attack used (used by disable)
            int attackIndex = -1;
            for (int i=0; i < friendlyPokemon.attacks.length; i++) {
                if (friendlyPokemon.attacks[i] == null) {
                    continue;
                }
                if (friendlyPokemon.attacks[i].equals(attack.name)) {
                    attackIndex = i;
                    break;
                }
            }
            if (isFriendly) {
                DrawBattle.prevFriendlyAttackIndex = attackIndex;
            }
            else {
                DrawBattle.prevEnemyAttackIndex = attackIndex;
            }
            
            // Check if attack misses or not
            // If it does, just return 'Attack Missed!' text
            // Source - https://web.archive.org/web/20140712063943/http://www.upokecenter.com/content/pokemon-gold-version-silver-version-and-crystal-version-timing-notes
            // Percentages - https://github.com/pret/pokecrystal/blob/master/macros/data.asm
            // - best I can tell, formula to convert percentage to number out of 256 is (n * 255) / 100;
            int accuracyStage = friendlyPokemon.statStages.get("accuracy");
            float multiplier = (float)Math.max(3, 3 + accuracyStage)/(float)Math.max(3, 3 - accuracyStage);
            accuracy = (int)(((attack.accuracy*255)/100)*multiplier);  // convert from 0-100 percent to 0-255
            if (accuracy < 1) {
                accuracy = 1;
            }
            int evasionStage = enemyPokemon.statStages.get("evasion");
            multiplier = (float)Math.max(3, 3 - evasionStage)/(float)Math.max(3, 3 + evasionStage);  // this is inverse of accuracy formula
            accuracy = (int)(accuracy*multiplier);
            if (accuracy < 1) {
                accuracy = 1;
            }
            if (accuracy > 255) {
                accuracy = 255;
            }
            // TODO: debug, remove
//            System.out.println(attack.name);
//            System.out.println(attack.accuracy);
//            System.out.println(accuracy);
            boolean attackMisses = accuracy < 255 && game.map.rand.nextInt(256) >= accuracy;
            if (attack.name.equals("swift")) {
                attackMisses = false;
            }

//            if (isFriendly) {
            // TODO: 'no effect' attacks
            // Attack data loaded from Crystal
//                String attackType = attack.type;  // TODO: remove

            // if does any damage, do multiplier stuff and text.
            // if does any stat change (how to tell?) then add text.
            attackAction.append(new DisplayText.Clear(game,
                                new WaitFrames(game, 3,
                                new DisplayText(game, enemy+friendlyPokemon.name.toUpperCase()+" used "+attack.name.toUpperCase()+"!",
                                                null, true, false,
                                null))));

            // If it's an attack that does damage,
            // - play enemy 'hit' animation
            // - deplete enemy health
            // - if super or not very effect, display text.
            if (attack.power != 0) {
                if (attackMisses) {
                    attackAction.append(new WaitFrames(game, 30,
                                        new DisplayText.Clear(game,
                                        new WaitFrames(game, 3,
                                        new DisplayText(game, "The attack missed!", null, true, true,
                                        nextAction)))));
                    return attackAction;
                }
                // Attack didn't miss so continue with damage calc.
                // TODO: remove
//                multiplier = game.battle.gen2TypeEffectiveness.get(attack.type).get(enemyPokemon.types.get(0).toLowerCase());
//                if (enemyPokemon.types.size() > 1) {
//                    multiplier *= game.battle.gen2TypeEffectiveness.get(attack.type).get(enemyPokemon.types.get(1).toLowerCase());
//                }
                multiplier = 1f;
                String prevType = "";
                for (String type : enemyPokemon.types){
                    if (type.equals(prevType)) {
                        continue;
                    }
                    prevType = type;
                    multiplier *= game.battle.gen2TypeEffectiveness.get(attack.type).get(type.toLowerCase());
                }
                String effectiveness = "neutral_effective";
                String text_string = "";
                if (multiplier > 1f) {
                    effectiveness = "super_effective";
                    text_string = "It' super- effective!";
                }
                else if (multiplier == 1f) {
                    effectiveness = "neutral_effective";
                }
                else if (multiplier > 0f) {
                    effectiveness = "not_very_effective";
                    text_string = "It' not very effective...";
                }
                // If no effect, just return here, the attack did nothing
                // and no status/stat changes will get applied.
                else {
                    attackAction.append(new WaitFrames(game, 30,
                                        new DisplayText.Clear(game,
                                        new WaitFrames(game, 3,
                                        new DisplayText(game, "It doesnì effect "+enemyPokemon.name.toUpperCase()+"!", null, true, true,
                                        nextAction)))));
                    return attackAction;
                }
                
                // If the attack is false swipe, leave the enemy with at least 1 hp.
                if (attack.effect.equals("EFFECT_FALSE_SWIPE") &&
                    attack.damage >= enemyPokemon.currentStats.get("hp")) {
                    attack.damage = enemyPokemon.currentStats.get("hp")-1;
                    if (attack.damage < 1) {
                        attack.damage = 1;
                    }
                }

                attackAction.append(new LoadAndPlayAnimation(game, attack.name, enemyPokemon, null));
                attackAction.append(new LoadAndPlayAnimation(game, effectiveness, enemyPokemon,
                                    Battle.depleteHealth(game, isFriendly, attack.damage,  // TODO: generic DepleteHealth class
                                    new WaitFrames(game, 13,
                                    null))));
                // If crit, display text.
                if (attack.isCrit) {
                    attackAction.append(new DisplayText.Clear(game,
                                        new WaitFrames(game, 3,
                                        new DisplayText(game, "Critical hit!", null, null,
                                        null))));
                }
                if (!effectiveness.equals("neutral_effective")) {
                    attackAction.append(new DisplayText.Clear(game,
                                        new WaitFrames(game, 3,
                                        new DisplayText(game, text_string, null, true, true,
                                        null))));
                }
                // Determine flinch success
                if (attack.effect.contains("FLINCH_HIT")) {
                    accuracy = (attack.effectChance*255)/100;
                    attackMisses = accuracy < 255 && game.map.rand.nextInt(256) >= accuracy;
                    if (!attackMisses) {
                        enemyPokemon.flinched = true;
                    }
                }
            }
            // TODO: I'm probably going to have to move the effects with the additional HIT effects
            // - if opposing pokemon faints, that effect isn't supposed to trigger. it will currently.
            // - See: 'End of Attack' section, https://web.archive.org/web/20140712063943/http://www.upokecenter.com/content/pokemon-gold-version-silver-version-and-crystal-version-timing-notes
            // TODO: actually the effect may not apply. we'll see.
            
            // If this is a stat-change attack, then:
            // - attempt to apply stat change
            // - add appropriate text (<> went up!; <> can't go any higher!)
            String stat = null;
            if (attack.effect.contains("EFFECT_ATTACK")) {
                stat = "attack";
            }
            else if (attack.effect.contains("EFFECT_DEFENSE")) {
                stat = "defense";
            }
            else if (attack.effect.contains("EFFECT_SPEED")) {
                stat = "speed";
            }
            else if (attack.effect.contains("EFFECT_SP_ATK")) {
                stat = "specialAtk";
            }
            else if (attack.effect.contains("EFFECT_SP_DEF")) {
                stat = "specialDef";
            }
            else if (attack.effect.contains("EFFECT_ACCURACY")) {
                stat = "accuracy";
            }
            else if (attack.effect.contains("EFFECT_EVASION")) {
                stat = "evasion";
            }
            if (stat != null) {
                // If this is a side-effect of an attack, then use effect chance instead of accuracy.
                if (attack.effect.contains("HIT")) {
                    accuracy = (attack.effectChance*255)/100;
                    attackMisses = accuracy < 255 && game.map.rand.nextInt(256) >= accuracy;
                }
                if (attackMisses) {
                    if (!attack.effect.contains("HIT")) {
                        attackAction.append(new WaitFrames(game, 30,
                                            new DisplayText.Clear(game,
                                            new WaitFrames(game, 3,
                                            new DisplayText(game, "But it failed!", null, true, true,
                                            nextAction)))));
                        return attackAction;
                    }
                    // Fails silently if it's the result of a hit.
                }
                else {
                    int stage = 1;
                    if (attack.effect.contains("2")) {
                        stage = 2;
                    }
                    if (attack.effect.contains("DOWN")) {
                        stage *= -1;
                    }
                    Pokemon target = friendlyPokemon;
                    // If this comes as a side-effect to a damaging move,
                    // then the stat change is applied to the opposing pokemon.
                    // Or if it just targets the other pokemon.
                    if (attack.effect.contains("HIT") || 
                        attack.name.equals("growl") || attack.name.equals("leer") ||
                        attack.name.equals("screech") || attack.name.equals("spider web") ||
                        attack.name.equals("string shot") || attack.name.equals("tail whip") ||
                        attack.name.equals("sand attack") || attack.name.equals("smokescreen") ||
                        attack.name.equals("kinesis") || attack.name.equals("flash") ||
                        attack.name.equals("mud slap") || attack.name.equals("octazooka") ||
                        attack.name.equals("cotton spore") ||
                        attack.name.equals("sweet scent") || attack.name.equals("scary face")) {
                        target = enemyPokemon;
                    }
                    boolean worked = target.gen2ApplyStatStage(stat, stage);
                    String text = null;
                    // Only show failed text when applying to friendly.
                    if (!worked) {
                        if (!attack.effect.contains("HIT")) {
                            text = target.name.toUpperCase()+"' "+stat.toUpperCase()+" wonì go any "+(stage > 0 ? "higher!" : "lower!");
                        }
                    }
                    else {
                        if (stage == 2) {
                            text = target.name.toUpperCase()+"' "+stat.toUpperCase()+" went way up!";
                        }
                        else if (stage == 1) {
                            text = target.name.toUpperCase()+"' "+stat.toUpperCase()+" went up!";
                        }
                        else if (stage == -1) {
                            text = target.name.toUpperCase()+"' "+stat.toUpperCase()+" fell!";
                        }
                        else if (stage == -2) {
                            text = target.name.toUpperCase()+"' "+stat.toUpperCase()+" sharply fell!";
                        }
                    }
                    // Don't play animation if stat stage won't go any higher/lower
                    // Animation will have already played for 'HIT' attacks
                    if (worked && !attack.effect.contains("HIT")) {
                        attackAction.append(new LoadAndPlayAnimation(game, attack.name, enemyPokemon, null));
                    }
                    if (text != null) {
                        if (worked && !attack.effect.contains("HIT")) {
                            // TODO: there's like a 'hit' animation that goes here where screen shakes.
                            // TODO: check and see if the 30 frames are necessary here. Also, how many?
                            // They might not be - I remember that text might appear immediately after shake.
//                            attackAction.append(new WaitFrames(game, 30, null)); 
                            attackAction.append(new LoadAndPlayAnimation(game, "stat_stage_hit", enemyPokemon,
                                                null));
                        }
                        attackAction.append(new DisplayText.Clear(game,
                                            new WaitFrames(game, 3,
                                            new DisplayText(game, text, null, true, false,
                                            new WaitFrames(game, 30,
                                            null)))));
                    }
                }
            }
            // If this is a status-change attack, then:
            // - attempt to apply status change
            // - add appropriate text (<> fell to sleep! etc)
            // https://web.archive.org/web/20140712063943/http://www.upokecenter.com/content/pokemon-gold-version-silver-version-and-crystal-version-timing-notes
            // https://bulbapedia.bulbagarden.net/wiki/Status_condition
            // TODO: confuse actually stacks with all other statuses, just handle it differently
            // - ie pokemon.confuseStatus or something (also needs to denote attract? can pokemon be confused and attracted at same time?)
            String status = null;
            enemy = isFriendly ? "Enemy " : "";
            if (attack.effect.contains("EFFECT_PARALYZE")) {
                status = "paralyze";
            }
            else if (attack.effect.equals("EFFECT_SLEEP")) {
                // Fun fact - EFFECT_SLEEP_TALK exists so don't collide with that
                // also - there is no EFFECT_SLEEP_HIT so not an issue.
                status = "sleep";
            }
            else if (attack.effect.contains("EFFECT_POISON")) {
                status = "poison";
            }
            else if (attack.effect.contains("EFFECT_CONFUSE")) {
                status = "confuse";
            }
            else if (attack.effect.contains("EFFECT_BURN")) {
                status = "burn";
            }
            else if (attack.effect.contains("EFFECT_FREEZE")) {
                status = "freeze";
            }
            else if (attack.effect.contains("EFFECT_TOXIC")) {
                status = "toxic";
            }
            else if (attack.effect.contains("EFFECT_ATTRACT")) {
                status = "attract";
            }
            if (status != null) {
                // If this is a side-effect of an attack, then use effect chance instead of accuracy.
                if (attack.effect.contains("HIT")) {
                    accuracy = (attack.effectChance*255)/100;
                    attackMisses = accuracy < 255 && game.map.rand.nextInt(256) >= accuracy;
                }
                // TODO: Technically should also check that move type == electric I believe
                // Electric types cannot be paralyzed (this covers HIT and non-HIT moves)
                if (attack.type.equals("electric") && enemyPokemon.types.contains("ELECTRIC") && status.equals("paralyze")) {
                    attackMisses = true;
                }
                // Ground types not affected by electric moves
                if (attack.type.equals("electric") && enemyPokemon.types.contains("ELECTRIC")) {
                    attackMisses = true;
                }

                // Poison types cannot be poisoned
                if (attack.type.equals("poison") && enemyPokemon.types.contains("POISON") && (status.equals("poison") || status.equals("toxic"))) {
                    attackMisses = true;
                }
                // Ice types cannot be frozen
                // TODO: but they can by tri-attack (not sure if keeping this feature)
                if (attack.type.equals("ice") && enemyPokemon.types.contains("ICE") && status.equals("freeze")) {
                    attackMisses = true;
                }
                // Fire types cannot be burned
                if (attack.type.equals("fire") && enemyPokemon.types.contains("FIRE") && status.equals("burn")) {
                    attackMisses = true;
                }
                // Target is always enemy for status moves
                // Note: for moves like REST, game doesnt denote EFFECT_SLEEP for effect.
                Pokemon target = enemyPokemon;
                if ((attackMisses || target.status != null) && !attack.effect.contains("HIT")) {
//                    if (!attack.effect.contains("HIT") || target.status != null) {
                    attackAction.append(new WaitFrames(game, 30,
                                        new DisplayText.Clear(game,
                                        new WaitFrames(game, 3,
                                        new DisplayText(game, "But it failed!", null, true, true,
                                        nextAction)))));
                    return attackAction;
//                    }
                }
                if (!attackMisses && target.status == null) {
                    // Animation will have already played for 'HIT' attacks
                    if (!attack.effect.contains("HIT")) {
                        attackAction.append(new LoadAndPlayAnimation(game, attack.name, enemyPokemon, null));
                    }
                    else {
                        // Timed using attack_anims/psybeam_player_gsc/source-reference.avi
                        attackAction.append(new WaitFrames(game, 49, null));
                    }

                    // TODO: I think this only applies to confuse, freeze, burn
                    if (!status.equals("sleep") && !status.equals("poison") ) {
                        // 'birds' animation for confuse, etc.
                        attackAction.append(new LoadAndPlayAnimation(game, "status_"+status, target, null));
                    }

//                    target.status = status;  // TODO: remove if unused
                    attackAction.append(new WaitFrames(game, 30, null));
                    String text = null;

                    
                    // TODO: enemy+ isn't working
                    if (status.equals("paralyze")) {
                        // Reduce speed to 1/4
                        target.currentStats.put("speed", target.currentStats.get("speed")/4);
                        text = enemy+target.name.toUpperCase()+" was PARALYZED! It might not be able to attack!";
                    }
                    else if (status.equals("burn")) {
                        // Reduce attack to 1/4
                        target.currentStats.put("attack", target.currentStats.get("attack")/4);
                        text = enemy+target.name.toUpperCase()+" was burned!";
                    }
                    else if (status.equals("sleep")) {
                        // Add to statusCounter
                        target.statusCounter = game.map.rand.nextInt(5) + 1;
                        text = enemy+target.name.toUpperCase()+" fell asleep!";
                    }
                    else if (status.equals("poison")) {
                        text = enemy+target.name.toUpperCase()+" was poisoned!";
                    }
                    else if (status.equals("confuse")) {
                        target.statusCounter = game.map.rand.nextInt(5) + 1;
                        text = enemy+target.name.toUpperCase()+" is confused!";
                    }
                    else if (status.equals("freeze")) {
                        text = enemy+target.name.toUpperCase()+"' frozen solid!";
                    }
                    else if (status.equals("toxic")) {
                        // Use statusCounter to count how many turns toxic has been in effect.
                        target.statusCounter = 1;
                        text = enemy+target.name.toUpperCase()+"' badly poisoned!";
                    }
                    else if (status.equals("attract")) {
                        text = enemy+enemyPokemon.name.toUpperCase()+" became infatuated with "+friendlyPokemon.name.toUpperCase()+"!";
                    }
                    if (text != null) {
                        attackAction.append(new DisplayText.Clear(game,
                                            new WaitFrames(game, 3,
                                            new DisplayText(game, text, null, true, true,
                                            // TODO: why is this set here? seems like this happens too late
                                            //  based on recordings.
                                            new SetField(target, "status", status,  // Delay to emulate vgc
                                            null)))));
                    }
                }
            }
            if (attack.name.equals("disable")) {
                // Last attack
                attackIndex = DrawBattle.prevFriendlyAttackIndex;
                if (isFriendly) {
                    attackIndex = DrawBattle.prevEnemyAttackIndex;
                }
                if (attackIndex == -1 || enemyPokemon.disabledIndex >= 0) {
                    attackAction.append(new DisplayText.Clear(game,
                                        new WaitFrames(game, 3,
                                        new DisplayText(game, "But it failed!",
                                                        null, false, true,
                                        null))));
                }
//                else if (attackIndex == enemyPokemon.disabledIndex) {
//                    attackAction.append(new DisplayText.Clear(game,
//                                        new WaitFrames(game, 3,
//                                        new DisplayText(game, enemy+enemyPokemon.name.toUpperCase()+"' "+enemyPokemon.attacks[attackIndex]+" is already disabled!",
//                                                        null, false, true,
//                                        null))));
//                }
                else {
                    enemyPokemon.disabledIndex = attackIndex;
                    enemyPokemon.disabledCounter = game.map.rand.nextInt(7) + 2;
                    attackAction.append(new DisplayText.Clear(game,
                                        new WaitFrames(game, 3,
                                        new DisplayText(game, enemy+enemyPokemon.name.toUpperCase()+"' "+enemyPokemon.attacks[attackIndex].toUpperCase()+" was disabled!",
                                                        null, false, true,
                                        null))));
                }
            }
            // Recover/Heal effects
            if (attack.effect.equals("EFFECT_HEAL") ||
                attack.effect.equals("EFFECT_SYNTHESIS") ||
                attack.effect.equals("EFFECT_LEECH_HIT") ||
                attack.effect.equals("EFFECT_DRAINING_KISS")) {
                // Default value for Recover
                int amount = friendlyPokemon.maxStats.get("hp")/2;
                // Synthesis effect notes:
                // https://bulbapedia.bulbagarden.net/wiki/Synthesis_(move)#Generation_II
                // TODO: synthesis checks weather, restores more if harsh sunlight
                //  weather not in the game yet (probably game.map.weather)
                if (attack.effect.equals("EFFECT_SYNTHESIS")) {
                    amount = friendlyPokemon.maxStats.get("hp")/4;
                    // TODO: if harsh sunlight active, do hp/2
                    //  If other weather active (hail, rain, sandstorm, ...), do hp/8
                    // TODO: 'morning' time of day doesn't exist yet. Shouldn't require any change here tho.
                    if (game.map.timeOfDay.equals("day")) {
                        amount *= 2;
                    }
                }
                else if (attack.effect.equals("EFFECT_LEECH_HIT")) {
                    amount = attack.damage/2;
                    if (amount < 1) {
                        amount = 1;
                    }
                }
                else if (attack.effect.equals("EFFECT_DRAINING_KISS")) {
                    amount = (3*attack.damage)/4;
                    if (amount < 1) {
                        amount = 1;
                    }
                }
                enemy = isFriendly ? "" : "Enemy ";
                
                if (friendlyPokemon.currentStats.get("hp") >= friendlyPokemon.maxStats.get("hp")) {
                    attackAction.append(new DisplayText.Clear(game,
                                        new WaitFrames(game, 3,
                                        new DisplayText(game, enemy+friendlyPokemon.name.toUpperCase()+"' HP is full!",  // TODO: wrong text
                                                        null, false, true,
                                        null))));
                }
                else {
                    attackAction.append(new LoadAndPlayAnimation(game, attack.name, enemyPokemon,
                                        // TODO: probably need to wait 4/5 frames here
                                        // to sync with animation length
                                        new RestoreHealth(friendlyPokemon, -amount,
                                        new DisplayText.Clear(game,
                                        new WaitFrames(game, 3,
                                        new DisplayText(game, friendlyPokemon.name.toUpperCase()+" regained health!", null, false, true,
                                        null))))));
                }
            }

            // TODO: not sure if this effect is accurate
            if (attack.effect.equals("EFFECT_SELFDESTRUCT")) {
                friendlyPokemon.currentStats.put("hp", 0);
            }
            
//            System.out.println(attack.effect);
//            System.out.println(attackMisses);
//            System.out.println(enemyPokemon.trappedBy);

            // Check if attack traps target pokemon
            if (enemyPokemon.trappedBy == null &&
                !attackMisses &&
                (attack.effect.equals("EFFECT_TRAP_TARGET") ||
                 attack.effect.equals("EFFECT_BIND"))) {
                    // TODO: don't need this anymore
//                (attack.name.equals("whirlpool") ||
//                 attack.name.equals("fire spin") ||
//                 attack.name.equals("wrap") ||
//                 attack.name.equals("thunder cage") ||
//                 attack.name.equals("clamp"))) {
                enemy = isFriendly ? "Enemy " : "";  // TODO: probably could just base this on target
                attackAction.append(new DisplayText.Clear(game,
                                    new WaitFrames(game, 3,
                                    new DisplayText(game, enemy+enemyPokemon.name.toUpperCase()+" was trapped!",
                                                    null, true, true,
                                    null))));
//                System.out.println(enemyPokemon.name);
                // 2-5 turns for trap
                enemyPokemon.trappedBy = attack.name.toLowerCase();
                enemyPokemon.trapCounter = game.map.rand.nextInt(4) + 2;
                if (attack.name.toLowerCase().equals("thunder cage")) {
                    enemyPokemon.trapCounter = game.map.rand.nextInt(2) + 4; // 4-5 for thunder cage
                }
            }


//            }
//            else {
//                String attackType = game.battle.attacks.get(attack.name.toLowerCase()).type;
//                float multiplier = game.battle.gen2TypeEffectiveness.get(attackType).get(game.player.currPokemon.types.get(0).toLowerCase());
//                if (game.player.currPokemon.types.size() > 1) {
//                    multiplier *= game.battle.gen2TypeEffectiveness.get(attackType).get(game.player.currPokemon.types.get(1).toLowerCase());
//                }
//                if (multiplier > 1f) {
//                    effectiveness = "super_effective";
//                    text_string = "It' super- effective!";
//                }
//                else if (multiplier == 1f) {
//                    effectiveness = "neutral_effective";
//                }
//                else {
//                    effectiveness = "not_very_effective";
//                    text_string = "It' not very effective...";
//                }
//                attackAction = new LoadAndPlayAnimation(game, attack.name, game.player.currPokemon,
//                               new LoadAndPlayAnimation(game, effectiveness, game.player.currPokemon,
//                               new DepleteFriendlyHealth(game.player.currPokemon, attack.damage,
//                               new WaitFrames(game, 13,
//                               !effectiveness.equals("neutral_effective") ?
//                                   new DisplayText.Clear(game,
//                                   new WaitFrames(game, 3,
//                                   new DisplayText(game, text_string,
//                                                   null, true, true,
//                                   new WaitFrames(game, 3,
//                                   null))))
//                               :
//                                   null))));
//                // Check if attack traps target pokemon
//                if (game.player.currPokemon.trappedBy != null) {
//                    attackAction.append(new DisplayText.Clear(game,
//                                          new WaitFrames(game, 3,
//                                          new DisplayText(game,
//                                                          game.player.currPokemon.name.toUpperCase()+" was trapped!",
//                                                          null,
//                                                          true,
//                                                          true,
//                                          null))));
//                }
//            }
            attackAction.append(nextAction);
            return attackAction;
        }

        if (isFriendly) {
            return new Attack.Default(game, power, accuracy, nextAction);
        }
        else {
            return new Attack.DefaultEnemy(game.battle.oppPokemon, game.player.currPokemon, power, accuracy, nextAction);
        }
    }

    // TODO: remove
//    public enum Effectiveness {
//        Super,
//        Neutral,
//        Not_Very,
//        No_Effect;
//    }

    public static Action getIntroAction(Game game) {
        // If player has no pokemon, encounter is safari zone style
        if (game.player.pokemon.isEmpty()) {

            return new SplitAction(new BattleIntro(
                                   new BattleIntroAnim1(
                                   new SplitAction(new DrawBattle(game),
                                                   new BattleAnimPositionPlayers(game,
                                                   new PlaySound(game.battle.oppPokemon,
                                                   new DisplayText(game, "Wild "+game.battle.oppPokemon.name.toUpperCase()+" appeared!", null, null,
                                                   new WaitFrames(game, 39,
                                                   // Demo code - confusing, but I don't want to write another if statement
                                                   game.player.adrenaline > 0 ? new DisplayText(game, ""+game.player.name+" has ADRENALINE "+Integer.toString(game.player.adrenaline)+"!", null, null,
                                                       new PrintAngryEating(game, // for demo mode, normally left out
                                                               new DrawBattleMenuSafariZone(game, null)
                                                           )
                                                       )
                                                   :
                                                   new PrintAngryEating(game, // for demo mode, normally left out
                                                   new DrawBattleMenuSafariZone(game,
                                                   null)
                                   )))))))),
                   null
                   );
        }
        // Play Pokemon's intro anim if pokemon crystal
        // TODO: refactor to use appendAction
        else if (game.battle.oppPokemon.generation == Pokemon.Generation.CRYSTAL) {
            Action triggerAction;
            if (game.player.currPokemon.generation == Pokemon.Generation.RED) {
                triggerAction = new PlaySound(game.player.currPokemon.name,
                                new WaitFrames(game, 6,
                                new DrawBattleMenuNormal(game, null)
                                ));
            }
            else {
                Action afterTrigger = new WaitFrames(game, 15,
                                      new DrawBattleMenuNormal(game,
                                      null
                                      ));
                triggerAction = new PlaySound(game.player.currPokemon,
                                new WaitFrames(game, 6,
                                new DrawFriendlyHealth(game,
                                afterTrigger)));
            }
            Action introAction = 
                   new BattleIntro(
                   new BattleIntroAnim1(
                   new SplitAction(new DrawBattle(game),
                   new BattleAnimPositionPlayers(game,
                   null))));
            if (game.battle.oppPokemon.isShiny) {
                introAction.append(new Battle.LoadAndPlayAnimation(game, "shiny", game.player.currPokemon, null));
            }
            introAction.append(
                   new SplitAction(new WaitFrames(game, 4,
                                   new PlaySound(game.battle.oppPokemon,
                                   null)),
                   new PokemonIntroAnim(
                   new WaitFrames(game, 11,
                   null))));
            // If Pokemon aggro'd the player (ie player stole egg, future: unfriendly pokemon),
            // then increase it's attack stat
            if (game.battle.oppPokemon.aggroPlayer) {
                introAction.append(
                    new DisplayText(game, "Angry "+game.battle.oppPokemon.name.toUpperCase()+" attacked!", null, null,
                    new DisplayText(game, "Enemy "+game.battle.oppPokemon.name.toUpperCase()+"' attack went way up!", null, null,
                    null)));
                // TODO: stat stages are never reset for oppPokemon, I think
                // and they need to be
                game.battle.oppPokemon.gen2ApplyStatStage("attack", 2);
            }
            else {
                introAction.append(
                    new DisplayText(game, "Wild "+game.battle.oppPokemon.name.toUpperCase()+" appeared!", null, null,
                    null));
            }

            if (game.battle.oppPokemon.isTrapping) {
                introAction.append(new DisplayText(game, "It' holding your leg! You canì flee!", null, null, null));
            }

            introAction.append(
                   new SplitAction(new WaitFrames(game, 1,
                                   new DrawEnemyHealth(game)),
                   new WaitFrames(game, 39,
                   new MovePlayerOffScreen(game,
                   new DisplayText(game, "Go! "+game.player.currPokemon.name.toUpperCase()+"!", null, triggerAction,
                   game.player.currPokemon.generation == Pokemon.Generation.RED ?  // basically an if block
                       new SplitAction(new DrawFriendlyHealth(game),
                       new ThrowOutPokemon(game, // this draws pkmn sprite permanently until battle ends, ie until game.battle.drawAction == null
                       triggerAction
                       ))
                   :
                       new ThrowOutPokemonCrystal(game, // this draws pkmn sprite permanently until battle ends, ie until game.battle.drawAction == null
                       game.player.currPokemon.isShiny ?
                           new Battle.LoadAndPlayAnimation(game, "shiny", game.battle.oppPokemon,
                           triggerAction)
                       :
                           triggerAction
                       )
                   )))));
            return introAction;
        }
        // Below is red/blue intro anim
        // TODO: what if opp pokemon is crystal, and yours is red? or vice-versa
        else {
            Action triggerAction = new PlaySound(game.player.currPokemon.name,
                                   new WaitFrames(game, 6,
                                   new DrawBattleMenuNormal(game, null)
                                   ));
            return new BattleIntro(
                   new BattleIntroAnim1(
                   new SplitAction(
                   new DrawBattle(game),
                   new BattleAnimPositionPlayers(game,
                   new PlaySound(game.battle.oppPokemon.name,
                   new DisplayText(game, "Wild "+game.battle.oppPokemon.name.toUpperCase()+" appeared!", null, null,
                   new SplitAction(new WaitFrames(game, 1,
                                   new DrawEnemyHealth(game)),
                   new WaitFrames(game, 39,
                   new MovePlayerOffScreen(game,
                   new DisplayText(game, "Go! "+game.player.currPokemon.name.toUpperCase()+"!", null, triggerAction,
                   new SplitAction(new DrawFriendlyHealth(game),
                   new ThrowOutPokemon(game, // this draws pkmn sprite permanently until battle ends, ie until game.battle.drawAction == null
                                       triggerAction
                   ))))))))))));
        }
    }
    // opposing pokemon
    public Pokemon oppPokemon;

    // action that is drawing the battle
     // this reference is used to stop drawing battle once it's complete
    DrawBattle drawAction;
    Music music;
//    Music music2; // TODO: remove

    Music victoryFanfare;

    // HashMap<Generation, ... ?
    HashMap<String, HashMap<String, Float>> gen2TypeEffectiveness;

    HashMap<String, Attack> attacks = new HashMap<String, Attack>();

    Network network = new Network();
    Runnable runnable;

    public Battle() {
        
        // TODO: remove if unused
//        this.music = Gdx.audio.newMusic(Gdx.files.internal("battle/battle-vs-wild-pokemon3.ogg"));
//        this.music.setLooping(false);
//        this.music.setVolume(0.3f);
//
//        // This is required to pre-load the looped part of the audio
//        // best method of looping that I could find (all other methods cause hanging)
//        this.music2 = Gdx.audio.newMusic(Gdx.files.internal("battle/battle-vs-wild-pokemon2.ogg"));
//        this.music2.setLooping(true);
//        this.music2.setVolume(0.3f);
//        this.music2.play();
//        this.music2.pause();
//        this.music2.setPosition(0f);
//        this.music.setOnCompletionListener(new Music.OnCompletionListener() {
//            @Override
//            public void onCompletion(final Music music) {
//                // TODO: didn't end up needing this.
//                // Restarting the music in a separate thread will at least prevent game from hanging
////                Thread thread = new Thread(runnable);
////                thread.start();
//                music2.play();
//                Game.staticGame.currMusic = music2;
//            }
//        });
        // TODO: just doing for fun, remove
//        this.music = Gdx.audio.newMusic(Gdx.files.internal("music/wild_battle_johto_by_familyjules2.ogg"));
//        this.music.setLooping(true);
//        this.music.setVolume(0.3f);

        this.music = new LinkedMusic("battle/battle-vs-wild-pokemon3", "battle/battle-vs-wild-pokemon2");
        this.music.setVolume(0.3f);

//        this.victoryFanfare = Gdx.audio.newMusic(Gdx.files.internal("victory_fanfare2.ogg"));
//        this.victoryFanfare.setLooping(true);
//        this.victoryFanfare.setVolume(0.3f);
        this.victoryFanfare = new LinkedMusic("victory_fanfare1_intro", "victory_fanfare1");
        this.victoryFanfare.setVolume(1f);

        // TODO: make this static
        // TODO: this could have been a string table, converted to map
//                normal fire water ...
//        normal  1      1    1
//        fire    1      .5   .5
//        water   1      2    .5
//        ...                       ...
        this.gen2TypeEffectiveness = new HashMap<String, HashMap<String, Float>>();
        this.gen2TypeEffectiveness.put("normal", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("normal").put("normal", 1f);
        this.gen2TypeEffectiveness.get("normal").put("fire", 1f);
        this.gen2TypeEffectiveness.get("normal").put("water", 1f);
        this.gen2TypeEffectiveness.get("normal").put("electric", 1f);
        this.gen2TypeEffectiveness.get("normal").put("grass", 1f);
        this.gen2TypeEffectiveness.get("normal").put("ice", 1f);
        this.gen2TypeEffectiveness.get("normal").put("fighting", 1f);
        this.gen2TypeEffectiveness.get("normal").put("poison", 1f);
        this.gen2TypeEffectiveness.get("normal").put("ground", 1f);
        this.gen2TypeEffectiveness.get("normal").put("flying", 1f);
        this.gen2TypeEffectiveness.get("normal").put("psychic", 1f);
        this.gen2TypeEffectiveness.get("normal").put("bug", 1f);
        this.gen2TypeEffectiveness.get("normal").put("rock", 0.5f);
        this.gen2TypeEffectiveness.get("normal").put("ghost", 0f);
        this.gen2TypeEffectiveness.get("normal").put("dragon", 1f);
        this.gen2TypeEffectiveness.get("normal").put("dark", 1f);
        this.gen2TypeEffectiveness.get("normal").put("steel", 0.5f);
        this.gen2TypeEffectiveness.get("normal").put("fairy", 1f);
        this.gen2TypeEffectiveness.put("fire", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("fire").put("normal", 1f);
        this.gen2TypeEffectiveness.get("fire").put("fire", 0.5f);
        this.gen2TypeEffectiveness.get("fire").put("water", 0.5f);
        this.gen2TypeEffectiveness.get("fire").put("electric", 1f);
        this.gen2TypeEffectiveness.get("fire").put("grass", 2f);
        this.gen2TypeEffectiveness.get("fire").put("ice", 2f);
        this.gen2TypeEffectiveness.get("fire").put("fighting", 1f);
        this.gen2TypeEffectiveness.get("fire").put("poison", 1f);
        this.gen2TypeEffectiveness.get("fire").put("ground", 1f);
        this.gen2TypeEffectiveness.get("fire").put("flying", 1f);
        this.gen2TypeEffectiveness.get("fire").put("psychic", 1f);
        this.gen2TypeEffectiveness.get("fire").put("bug", 2f);
        this.gen2TypeEffectiveness.get("fire").put("rock", 0.5f);
        this.gen2TypeEffectiveness.get("fire").put("ghost", 1f);
        this.gen2TypeEffectiveness.get("fire").put("dragon", 0.5f);
        this.gen2TypeEffectiveness.get("fire").put("dark", 1f);
        this.gen2TypeEffectiveness.get("fire").put("steel", 2f);
        this.gen2TypeEffectiveness.get("fire").put("fairy", 1f);
        this.gen2TypeEffectiveness.put("water", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("water").put("normal", 1f);
        this.gen2TypeEffectiveness.get("water").put("fire", 2f);
        this.gen2TypeEffectiveness.get("water").put("water", 0.5f);
        this.gen2TypeEffectiveness.get("water").put("electric", 1f);
        this.gen2TypeEffectiveness.get("water").put("grass", 0.5f);
        this.gen2TypeEffectiveness.get("water").put("ice", 1f);
        this.gen2TypeEffectiveness.get("water").put("fighting", 1f);
        this.gen2TypeEffectiveness.get("water").put("poison", 1f);
        this.gen2TypeEffectiveness.get("water").put("ground", 2f);
        this.gen2TypeEffectiveness.get("water").put("flying", 1f);
        this.gen2TypeEffectiveness.get("water").put("psychic", 1f);
        this.gen2TypeEffectiveness.get("water").put("bug", 1f);
        this.gen2TypeEffectiveness.get("water").put("rock", 2f);
        this.gen2TypeEffectiveness.get("water").put("ghost", 1f);
        this.gen2TypeEffectiveness.get("water").put("dragon", 0.5f);
        this.gen2TypeEffectiveness.get("water").put("dark", 1f);
        this.gen2TypeEffectiveness.get("water").put("steel", 1f);
        this.gen2TypeEffectiveness.get("water").put("fairy", 1f);
        this.gen2TypeEffectiveness.put("electric", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("electric").put("normal", 1f);
        this.gen2TypeEffectiveness.get("electric").put("fire", 1f);
        this.gen2TypeEffectiveness.get("electric").put("water", 2f);
        this.gen2TypeEffectiveness.get("electric").put("electric", 0.5f);
        this.gen2TypeEffectiveness.get("electric").put("grass", 0.5f);
        this.gen2TypeEffectiveness.get("electric").put("ice", 1f);
        this.gen2TypeEffectiveness.get("electric").put("fighting", 1f);
        this.gen2TypeEffectiveness.get("electric").put("poison", 1f);
        this.gen2TypeEffectiveness.get("electric").put("ground", 0f);
        this.gen2TypeEffectiveness.get("electric").put("flying", 2f);
        this.gen2TypeEffectiveness.get("electric").put("psychic", 1f);
        this.gen2TypeEffectiveness.get("electric").put("bug", 1f);
        this.gen2TypeEffectiveness.get("electric").put("rock", 1f);
        this.gen2TypeEffectiveness.get("electric").put("ghost", 1f);
        this.gen2TypeEffectiveness.get("electric").put("dragon", 0.5f);
        this.gen2TypeEffectiveness.get("electric").put("dark", 1f);
        this.gen2TypeEffectiveness.get("electric").put("steel", 1f);
        this.gen2TypeEffectiveness.get("electric").put("fairy", 1f);
        this.gen2TypeEffectiveness.put("grass", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("grass").put("normal", 1f);
        this.gen2TypeEffectiveness.get("grass").put("fire", 0.5f);
        this.gen2TypeEffectiveness.get("grass").put("water", 2f);
        this.gen2TypeEffectiveness.get("grass").put("electric", 1f);
        this.gen2TypeEffectiveness.get("grass").put("grass", 0.5f);
        this.gen2TypeEffectiveness.get("grass").put("ice", 1f);
        this.gen2TypeEffectiveness.get("grass").put("fighting", 1f);
        this.gen2TypeEffectiveness.get("grass").put("poison", 0.5f);
        this.gen2TypeEffectiveness.get("grass").put("ground", 2f);
        this.gen2TypeEffectiveness.get("grass").put("flying", 0.5f);
        this.gen2TypeEffectiveness.get("grass").put("psychic", 1f);
        this.gen2TypeEffectiveness.get("grass").put("bug", 0.5f);
        this.gen2TypeEffectiveness.get("grass").put("rock", 2f);
        this.gen2TypeEffectiveness.get("grass").put("ghost", 1f);
        this.gen2TypeEffectiveness.get("grass").put("dragon", 0.5f);
        this.gen2TypeEffectiveness.get("grass").put("dark", 1f);
        this.gen2TypeEffectiveness.get("grass").put("steel", 0.5f);
        this.gen2TypeEffectiveness.get("grass").put("fairy", 1f);
        this.gen2TypeEffectiveness.put("ice", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("ice").put("normal", 1f);
        this.gen2TypeEffectiveness.get("ice").put("fire", 0.5f);
        this.gen2TypeEffectiveness.get("ice").put("water", 0.5f);
        this.gen2TypeEffectiveness.get("ice").put("electric", 1f);
        this.gen2TypeEffectiveness.get("ice").put("grass", 2f);
        this.gen2TypeEffectiveness.get("ice").put("ice", 0.5f);
        this.gen2TypeEffectiveness.get("ice").put("fighting", 1f);
        this.gen2TypeEffectiveness.get("ice").put("poison", 1f);
        this.gen2TypeEffectiveness.get("ice").put("ground", 2f);
        this.gen2TypeEffectiveness.get("ice").put("flying", 2f);
        this.gen2TypeEffectiveness.get("ice").put("psychic", 1f);
        this.gen2TypeEffectiveness.get("ice").put("bug", 1f);
        this.gen2TypeEffectiveness.get("ice").put("rock", 1f);
        this.gen2TypeEffectiveness.get("ice").put("ghost", 1f);
        this.gen2TypeEffectiveness.get("ice").put("dragon", 2f);
        this.gen2TypeEffectiveness.get("ice").put("dark", 1f);
        this.gen2TypeEffectiveness.get("ice").put("steel", 0.5f);
        this.gen2TypeEffectiveness.get("ice").put("fairy", 1f);
        this.gen2TypeEffectiveness.put("fighting", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("fighting").put("normal", 2f);
        this.gen2TypeEffectiveness.get("fighting").put("fire", 1f);
        this.gen2TypeEffectiveness.get("fighting").put("water", 1f);
        this.gen2TypeEffectiveness.get("fighting").put("electric", 1f);
        this.gen2TypeEffectiveness.get("fighting").put("grass", 1f);
        this.gen2TypeEffectiveness.get("fighting").put("ice", 2f);
        this.gen2TypeEffectiveness.get("fighting").put("fighting", 1f);
        this.gen2TypeEffectiveness.get("fighting").put("poison", 0.5f);
        this.gen2TypeEffectiveness.get("fighting").put("ground", 1f);
        this.gen2TypeEffectiveness.get("fighting").put("flying", 0.5f);
        this.gen2TypeEffectiveness.get("fighting").put("psychic", 0.5f);
        this.gen2TypeEffectiveness.get("fighting").put("bug", 0.5f);
        this.gen2TypeEffectiveness.get("fighting").put("rock", 2f);
        this.gen2TypeEffectiveness.get("fighting").put("ghost", 0f);
        this.gen2TypeEffectiveness.get("fighting").put("dragon", 1f);
        this.gen2TypeEffectiveness.get("fighting").put("dark", 2f);
        this.gen2TypeEffectiveness.get("fighting").put("steel", 2f);
        this.gen2TypeEffectiveness.get("fighting").put("fairy", 0.5f);
        this.gen2TypeEffectiveness.put("poison", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("poison").put("normal", 1f);
        this.gen2TypeEffectiveness.get("poison").put("fire", 1f);
        this.gen2TypeEffectiveness.get("poison").put("water", 1f);
        this.gen2TypeEffectiveness.get("poison").put("electric", 1f);
        this.gen2TypeEffectiveness.get("poison").put("grass", 2f);
        this.gen2TypeEffectiveness.get("poison").put("ice", 1f);
        this.gen2TypeEffectiveness.get("poison").put("fighting", 1f);
        this.gen2TypeEffectiveness.get("poison").put("poison", 0.5f);
        this.gen2TypeEffectiveness.get("poison").put("ground", 0.5f);
        this.gen2TypeEffectiveness.get("poison").put("flying", 1f);
        this.gen2TypeEffectiveness.get("poison").put("psychic", 1f);
        this.gen2TypeEffectiveness.get("poison").put("bug", 1f);
        this.gen2TypeEffectiveness.get("poison").put("rock", 0.5f);
        this.gen2TypeEffectiveness.get("poison").put("ghost", 0.5f);
        this.gen2TypeEffectiveness.get("poison").put("dragon", 1f);
        this.gen2TypeEffectiveness.get("poison").put("dark", 1f);
        this.gen2TypeEffectiveness.get("poison").put("steel", 0f);
        this.gen2TypeEffectiveness.get("poison").put("fairy", 2f);
        this.gen2TypeEffectiveness.put("ground", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("ground").put("normal", 1f);
        this.gen2TypeEffectiveness.get("ground").put("fire", 2f);
        this.gen2TypeEffectiveness.get("ground").put("water", 1f);
        this.gen2TypeEffectiveness.get("ground").put("electric", 2f);
        this.gen2TypeEffectiveness.get("ground").put("grass", 0.5f);
        this.gen2TypeEffectiveness.get("ground").put("ice", 1f);
        this.gen2TypeEffectiveness.get("ground").put("fighting", 1f);
        this.gen2TypeEffectiveness.get("ground").put("poison", 2f);
        this.gen2TypeEffectiveness.get("ground").put("ground", 1f);
        this.gen2TypeEffectiveness.get("ground").put("flying", 0f);
        this.gen2TypeEffectiveness.get("ground").put("psychic", 1f);
        this.gen2TypeEffectiveness.get("ground").put("bug", 0.5f);
        this.gen2TypeEffectiveness.get("ground").put("rock", 2f);
        this.gen2TypeEffectiveness.get("ground").put("ghost", 1f);
        this.gen2TypeEffectiveness.get("ground").put("dragon", 1f);
        this.gen2TypeEffectiveness.get("ground").put("dark", 1f);
        this.gen2TypeEffectiveness.get("ground").put("steel", 2f);
        this.gen2TypeEffectiveness.get("ground").put("fairy", 1f);
        this.gen2TypeEffectiveness.put("flying", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("flying").put("normal", 1f);
        this.gen2TypeEffectiveness.get("flying").put("fire", 1f);
        this.gen2TypeEffectiveness.get("flying").put("water", 1f);
        this.gen2TypeEffectiveness.get("flying").put("electric", 0.5f);
        this.gen2TypeEffectiveness.get("flying").put("grass", 2f);
        this.gen2TypeEffectiveness.get("flying").put("ice", 1f);
        this.gen2TypeEffectiveness.get("flying").put("fighting", 2f);
        this.gen2TypeEffectiveness.get("flying").put("poison", 1f);
        this.gen2TypeEffectiveness.get("flying").put("ground", 1f);
        this.gen2TypeEffectiveness.get("flying").put("flying", 1f);
        this.gen2TypeEffectiveness.get("flying").put("psychic", 1f);
        this.gen2TypeEffectiveness.get("flying").put("bug", 2f);
        this.gen2TypeEffectiveness.get("flying").put("rock", 0.5f);
        this.gen2TypeEffectiveness.get("flying").put("ghost", 1f);
        this.gen2TypeEffectiveness.get("flying").put("dragon", 1f);
        this.gen2TypeEffectiveness.get("flying").put("dark", 1f);
        this.gen2TypeEffectiveness.get("flying").put("steel", 0.5f);
        this.gen2TypeEffectiveness.get("flying").put("fairy", 1f);
        this.gen2TypeEffectiveness.put("psychic", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("psychic").put("normal", 1f);
        this.gen2TypeEffectiveness.get("psychic").put("fire", 1f);
        this.gen2TypeEffectiveness.get("psychic").put("water", 1f);
        this.gen2TypeEffectiveness.get("psychic").put("electric", 1f);
        this.gen2TypeEffectiveness.get("psychic").put("grass", 1f);
        this.gen2TypeEffectiveness.get("psychic").put("ice", 1f);
        this.gen2TypeEffectiveness.get("psychic").put("fighting", 2f);
        this.gen2TypeEffectiveness.get("psychic").put("poison", 2f);
        this.gen2TypeEffectiveness.get("psychic").put("ground", 1f);
        this.gen2TypeEffectiveness.get("psychic").put("flying", 1f);
        this.gen2TypeEffectiveness.get("psychic").put("psychic", 0.5f);
        this.gen2TypeEffectiveness.get("psychic").put("bug", 1f);
        this.gen2TypeEffectiveness.get("psychic").put("rock", 1f);
        this.gen2TypeEffectiveness.get("psychic").put("ghost", 1f);
        this.gen2TypeEffectiveness.get("psychic").put("dragon", 1f);
        this.gen2TypeEffectiveness.get("psychic").put("dark", 0f);
        this.gen2TypeEffectiveness.get("psychic").put("steel", 0.5f);
        this.gen2TypeEffectiveness.get("psychic").put("fairy", 1f);
        this.gen2TypeEffectiveness.put("bug", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("bug").put("normal", 1f);
        this.gen2TypeEffectiveness.get("bug").put("fire", 0.5f);
        this.gen2TypeEffectiveness.get("bug").put("water", 1f);
        this.gen2TypeEffectiveness.get("bug").put("electric", 1f);
        this.gen2TypeEffectiveness.get("bug").put("grass", 2f);
        this.gen2TypeEffectiveness.get("bug").put("ice", 1f);
        this.gen2TypeEffectiveness.get("bug").put("fighting", 0.5f);
        this.gen2TypeEffectiveness.get("bug").put("poison", 0.5f);
        this.gen2TypeEffectiveness.get("bug").put("ground", 1f);
        this.gen2TypeEffectiveness.get("bug").put("flying", 0.5f);
        this.gen2TypeEffectiveness.get("bug").put("psychic", 2f);
        this.gen2TypeEffectiveness.get("bug").put("bug", 1f);
        this.gen2TypeEffectiveness.get("bug").put("rock", 1f);
        this.gen2TypeEffectiveness.get("bug").put("ghost", 0.5f);
        this.gen2TypeEffectiveness.get("bug").put("dragon", 1f);
        this.gen2TypeEffectiveness.get("bug").put("dark", 2f);
        this.gen2TypeEffectiveness.get("bug").put("steel", 0.5f);
        this.gen2TypeEffectiveness.get("bug").put("fairy", 0.5f);
        this.gen2TypeEffectiveness.put("rock", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("rock").put("normal", 1f);
        this.gen2TypeEffectiveness.get("rock").put("fire", 2f);
        this.gen2TypeEffectiveness.get("rock").put("water", 1f);
        this.gen2TypeEffectiveness.get("rock").put("electric", 1f);
        this.gen2TypeEffectiveness.get("rock").put("grass", 1f);
        this.gen2TypeEffectiveness.get("rock").put("ice", 2f);
        this.gen2TypeEffectiveness.get("rock").put("fighting", 0.5f);
        this.gen2TypeEffectiveness.get("rock").put("poison", 1f);
        this.gen2TypeEffectiveness.get("rock").put("ground", 0.5f);
        this.gen2TypeEffectiveness.get("rock").put("flying", 2f);
        this.gen2TypeEffectiveness.get("rock").put("psychic", 1f);
        this.gen2TypeEffectiveness.get("rock").put("bug", 2f);
        this.gen2TypeEffectiveness.get("rock").put("rock", 1f);
        this.gen2TypeEffectiveness.get("rock").put("ghost", 1f);
        this.gen2TypeEffectiveness.get("rock").put("dragon", 1f);
        this.gen2TypeEffectiveness.get("rock").put("dark", 1f);
        this.gen2TypeEffectiveness.get("rock").put("steel", 0.5f);
        this.gen2TypeEffectiveness.get("rock").put("fairy", 1f);
        this.gen2TypeEffectiveness.put("ghost", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("ghost").put("normal", 0f);
        this.gen2TypeEffectiveness.get("ghost").put("fire", 1f);
        this.gen2TypeEffectiveness.get("ghost").put("water", 1f);
        this.gen2TypeEffectiveness.get("ghost").put("electric", 1f);
        this.gen2TypeEffectiveness.get("ghost").put("grass", 1f);
        this.gen2TypeEffectiveness.get("ghost").put("ice", 1f);
        this.gen2TypeEffectiveness.get("ghost").put("fighting", 1f);
        this.gen2TypeEffectiveness.get("ghost").put("poison", 1f);
        this.gen2TypeEffectiveness.get("ghost").put("ground", 1f);
        this.gen2TypeEffectiveness.get("ghost").put("flying", 1f);
        this.gen2TypeEffectiveness.get("ghost").put("psychic", 2f);
        this.gen2TypeEffectiveness.get("ghost").put("bug", 1f);
        this.gen2TypeEffectiveness.get("ghost").put("rock", 1f);
        this.gen2TypeEffectiveness.get("ghost").put("ghost", 2f);
        this.gen2TypeEffectiveness.get("ghost").put("dragon", 1f);
        this.gen2TypeEffectiveness.get("ghost").put("dark", 0.5f);
        this.gen2TypeEffectiveness.get("ghost").put("steel", 1f);
        this.gen2TypeEffectiveness.get("ghost").put("fairy", 1f);
        this.gen2TypeEffectiveness.put("dragon", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("dragon").put("normal", 1f);
        this.gen2TypeEffectiveness.get("dragon").put("fire", 1f);
        this.gen2TypeEffectiveness.get("dragon").put("water", 1f);
        this.gen2TypeEffectiveness.get("dragon").put("electric", 1f);
        this.gen2TypeEffectiveness.get("dragon").put("grass", 1f);
        this.gen2TypeEffectiveness.get("dragon").put("ice", 1f);
        this.gen2TypeEffectiveness.get("dragon").put("fighting", 1f);
        this.gen2TypeEffectiveness.get("dragon").put("poison", 1f);
        this.gen2TypeEffectiveness.get("dragon").put("ground", 1f);
        this.gen2TypeEffectiveness.get("dragon").put("flying", 1f);
        this.gen2TypeEffectiveness.get("dragon").put("psychic", 1f);
        this.gen2TypeEffectiveness.get("dragon").put("bug", 1f);
        this.gen2TypeEffectiveness.get("dragon").put("rock", 1f);
        this.gen2TypeEffectiveness.get("dragon").put("ghost", 1f);
        this.gen2TypeEffectiveness.get("dragon").put("dragon", 2f);
        this.gen2TypeEffectiveness.get("dragon").put("dark", 1f);
        this.gen2TypeEffectiveness.get("dragon").put("steel", 0.5f);
        this.gen2TypeEffectiveness.get("dragon").put("fairy", 0f);
        this.gen2TypeEffectiveness.put("dark", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("dark").put("normal", 1f);
        this.gen2TypeEffectiveness.get("dark").put("fire", 1f);
        this.gen2TypeEffectiveness.get("dark").put("water", 1f);
        this.gen2TypeEffectiveness.get("dark").put("electric", 1f);
        this.gen2TypeEffectiveness.get("dark").put("grass", 1f);
        this.gen2TypeEffectiveness.get("dark").put("ice", 1f);
        this.gen2TypeEffectiveness.get("dark").put("fighting", 0.5f);
        this.gen2TypeEffectiveness.get("dark").put("poison", 1f);
        this.gen2TypeEffectiveness.get("dark").put("ground", 1f);
        this.gen2TypeEffectiveness.get("dark").put("flying", 1f);
        this.gen2TypeEffectiveness.get("dark").put("psychic", 2f);
        this.gen2TypeEffectiveness.get("dark").put("bug", 1f);
        this.gen2TypeEffectiveness.get("dark").put("rock", 1f);
        this.gen2TypeEffectiveness.get("dark").put("ghost", 2f);
        this.gen2TypeEffectiveness.get("dark").put("dragon", 1f);
        this.gen2TypeEffectiveness.get("dark").put("dark", 0.5f);
        this.gen2TypeEffectiveness.get("dark").put("steel", 1f);
        this.gen2TypeEffectiveness.get("dark").put("fairy", 0.5f);
        this.gen2TypeEffectiveness.put("steel", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("steel").put("normal", 1f);
        this.gen2TypeEffectiveness.get("steel").put("fire", 0.5f);
        this.gen2TypeEffectiveness.get("steel").put("water", 0.5f);
        this.gen2TypeEffectiveness.get("steel").put("electric", 0.5f);
        this.gen2TypeEffectiveness.get("steel").put("grass", 1f);
        this.gen2TypeEffectiveness.get("steel").put("ice", 2f);
        this.gen2TypeEffectiveness.get("steel").put("fighting", 1f);
        this.gen2TypeEffectiveness.get("steel").put("poison", 1f);
        this.gen2TypeEffectiveness.get("steel").put("ground", 1f);
        this.gen2TypeEffectiveness.get("steel").put("flying", 1f);
        this.gen2TypeEffectiveness.get("steel").put("psychic", 1f);
        this.gen2TypeEffectiveness.get("steel").put("bug", 1f);
        this.gen2TypeEffectiveness.get("steel").put("rock", 2f);
        this.gen2TypeEffectiveness.get("steel").put("ghost", 1f);
        this.gen2TypeEffectiveness.get("steel").put("dragon", 1f);
        this.gen2TypeEffectiveness.get("steel").put("dark", 1f);
        this.gen2TypeEffectiveness.get("steel").put("steel", 0.5f);
        this.gen2TypeEffectiveness.get("steel").put("fairy", 2f);
        this.gen2TypeEffectiveness.put("fairy", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("fairy").put("normal", 1f);
        this.gen2TypeEffectiveness.get("fairy").put("fire", 0.5f);
        this.gen2TypeEffectiveness.get("fairy").put("water", 1f);
        this.gen2TypeEffectiveness.get("fairy").put("electric", 1f);
        this.gen2TypeEffectiveness.get("fairy").put("grass", 1f);
        this.gen2TypeEffectiveness.get("fairy").put("ice", 1f);
        this.gen2TypeEffectiveness.get("fairy").put("fighting", 2f);
        this.gen2TypeEffectiveness.get("fairy").put("poison", 0.5f);
        this.gen2TypeEffectiveness.get("fairy").put("ground", 1f);
        this.gen2TypeEffectiveness.get("fairy").put("flying", 1f);
        this.gen2TypeEffectiveness.get("fairy").put("psychic", 1f);
        this.gen2TypeEffectiveness.get("fairy").put("bug", 1f);
        this.gen2TypeEffectiveness.get("fairy").put("rock", 1f);
        this.gen2TypeEffectiveness.get("fairy").put("ghost", 1f);
        this.gen2TypeEffectiveness.get("fairy").put("dragon", 2f);
        this.gen2TypeEffectiveness.get("fairy").put("dark", 2f);
        this.gen2TypeEffectiveness.get("fairy").put("steel", 0.5f);
        this.gen2TypeEffectiveness.get("fairy").put("fairy", 1f);

        ArrayList<String> gen2PhysicalTypes = new ArrayList<String>();
        gen2PhysicalTypes.add("normal");
        gen2PhysicalTypes.add("fighting");
        gen2PhysicalTypes.add("poison");
        gen2PhysicalTypes.add("ground");
        gen2PhysicalTypes.add("flying");
        gen2PhysicalTypes.add("bug");
        gen2PhysicalTypes.add("rock");
        gen2PhysicalTypes.add("ghost");
        gen2PhysicalTypes.add("steel");

        // Load all attacks and attributes
        try {
            FileHandle file = Gdx.files.internal("crystal_pokemon/moves.asm");
            Reader reader = file.reader();
            BufferedReader br = new BufferedReader(reader);
            String line;
//            int lineNum = 1;  // TODO: remove
            while ((line = br.readLine()) != null)   {
                // TODO: using table to look up number now
//                if (lineNum == 0) {
//                    this.dexNumber = line.split(" ; ")[1];
//                } else
//                if (lineNum > 14 && lineNum < 269) {
                if (!line.contains("\tmove ")) {
                    continue;
                }
                if (line.contains(";")) {
                    continue;
                }
                String[] attrs = line.split("\tmove ")[1].split(",\\s+");
                String attackType = attrs[3].toLowerCase();
                if (!Game.fairyTypeEnabled && attackType.equals("FAIRY")) {
                    attackType = "NORMAL";
                }
                Attack attack = new Attack(attrs[0].toLowerCase().replace('_', ' '), attrs[1], 
                                           Integer.valueOf(attrs[2]), attackType, Integer.valueOf(attrs[4]),
                                           Integer.valueOf(attrs[5]), Integer.valueOf(attrs[6]));
                if (gen2PhysicalTypes.contains(attack.type.toLowerCase())) {
                    attack.isPhysical = true;
                }
                this.attacks.put(attack.name, attack);
//                lineNum++;  // TODO: remove
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // load prism moves
        try {
            FileHandle file = Gdx.files.internal("crystal_pokemon/prism/moves.asm");
            Reader reader = file.reader();
            BufferedReader br = new BufferedReader(reader);
            String line;
            int lineNum = 1;
            while ((line = br.readLine()) != null)   {
                if (lineNum > 13 && lineNum < 268) {
                    // Weird prism-exclusive types
                    if (line.contains("PRISM_T") || line.contains(", SOUND,") || line.contains("FAIRY_T") || line.contains("CURSE_T") || line.contains(", GAS,") || line.contains("TRI_T")) {
                        continue;
                    }
                    // TODO: prism moves.asm includes info about physical/special/status
                    // Not using 'STATUS' type for gen2, so ignoring for now.
                    String[] attrs = line.split("\tmove ")[1].split(",\\s+");
                    Attack attack = new Attack(attrs[0].toLowerCase().replace('_', ' '), attrs[1], 
                                               Integer.valueOf(attrs[2]), attrs[3].toLowerCase(), Integer.valueOf(attrs[5]),
                                               Integer.valueOf(attrs[6]), Integer.valueOf(attrs[7].split(" ")[0]));
                    if (gen2PhysicalTypes.contains(attack.type.toLowerCase())) {
                        attack.isPhysical = true;
                    }
                    this.attacks.put(attack.name, attack);
                }
                lineNum++;
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // power 0 because the attack will apply extra damage that ignores stats.
        Attack attack = new Attack("Mewtwo_Special1", "EFFECT_NORMAL_HIT", 0, "psychic", 100, 1, 100);
        this.attacks.put(attack.name, attack);
        attack = new Attack("confusion_hit", "EFFECT_NORMAL_HIT", 40, "normal", 100, 1, 100);  // TODO: typeless
        this.attacks.put(attack.name, attack);
    }

    /**
     * Reference - https:// bulbapedia.bulbagarden.net/wiki/Experience#Example_.28Generation_II_to_IV.29
     * 
     * numParticipated - number of Pokemon that participated in battle.
     */
    int calcFaintExp(int numParticipated) {
        int a = 1; // TODO: 1.5 if owned by trainer
        int t = 1; // TODO: 1.5 if traded
        int b = this.oppPokemon.baseStats.get("baseExp");
        int e = 1; // TODO: 1.5 if curr pokemon holding lucky egg
        int l = this.oppPokemon.level;
        int s = numParticipated;
        int exp = (a*t*b*e*l)/(7*s);
        // TODO: leveling takes too long, doing exp*2 for now. May remove this in the future if way
        // to get more exp is added.
        return exp*5;  // TODO: was 2
    }

    /**
     * Determine if run from battle is successful or not.
     *
     * https://web.archive.org/web/20140712063943/http://www.upokecenter.com/content/pokemon-gold-version-silver-version-and-crystal-version-timing-notes
     * Whether current run attempt is counted or not is contradicted here: https://pokemondb.net/pokebase/71124/how-is-cant-escape-determined
     * https://bulbapedia.bulbagarden.net/wiki/Escape#Generation_I_and_II
     */
    boolean calcIfRunSuccessful(Game game, Player player) {
        // special case for special mewtwo battle - can't run.
        // TODO: uncomment in the future.
//        if (SpecialMewtwo1.class.isInstance(this.oppPokemon)) {
//            return false;
//        }
        int currSpeed = player.currPokemon.currentStats.get("speed");
        int b = (int)(this.oppPokemon.currentStats.get("speed")/4) % 256;
//        System.out.println("opp speed:" + String.valueOf(this.oppPokemon.currentStats.get("speed")));
//        System.out.println("b:" + String.valueOf(b));
        if (b == 0) {
            return true;
        }
        int x = (int)(currSpeed*32/b)+(30*(player.numFlees+1));
        if (x > 255) {
            return true;
        }
        if (game.map.rand.nextInt(256) < x) {
            return true;
        }
        return false;
    }
    
    /**
     * TODO: just make DepleteHealth generic class
     */
    public static Action depleteHealth(Game game, boolean isFriendly, int damage, Action nextAction) {
        if (!isFriendly) {
            return new DepleteFriendlyHealth(game.player.currPokemon, damage, nextAction);
        }
        return new DepleteEnemyHealth(game, damage, nextAction);
    }

    class CheckTrapped extends Action {

        public CheckTrapped(Game game, Action nextAction) {
            this.nextAction = nextAction;
        }

        @Override
        public void step(Game game) {
            Action action = new Action() {
                public String getCamera() {
                    return "gui";
                }
            };
            if (game.player.currPokemon.trappedBy != null) {
                Attack trap = game.battle.attacks.get(game.player.currPokemon.trappedBy);
                trap.damage = Battle.gen2CalcDamage(game.battle.oppPokemon, trap, game.player.currPokemon);  // TODO: does STAB apply to traps?
                if (trap.name.equals("thunder cage")) {
                    trap.damage = game.player.currPokemon.maxStats.get("hp")/8;
                }
                action.append(new Battle.LoadAndPlayAnimation(game, game.player.currPokemon.trappedBy, game.player.currPokemon,
                              new DisplayText.Clear(game,
                              new WaitFrames(game, 3,
                              new DisplayText(game, game.player.currPokemon.name.toUpperCase()+"' hurt by "+game.player.currPokemon.trappedBy.toUpperCase()+"!",
                                              null, true,
                              new DepleteFriendlyHealth(game.player.currPokemon, trap.damage,
                              new WaitFrames(game, 13,
                              null)))))));
                game.player.currPokemon.trapCounter -= 1;
                if (game.player.currPokemon.trapCounter <= 0) {
                    game.player.currPokemon.trappedBy = null;
                }
            }
            if (game.battle.oppPokemon.trappedBy != null) {
                Attack trap = game.battle.attacks.get(game.battle.oppPokemon.trappedBy);
                trap.damage = Battle.gen2CalcDamage(game.battle.oppPokemon, trap, game.player.currPokemon);  // TODO: does STAB apply to traps?
                if (trap.name.equals("thunder cage")) {
                    trap.damage = game.battle.oppPokemon.maxStats.get("hp")/8;
                }
                action.append(new Battle.LoadAndPlayAnimation(game, game.battle.oppPokemon.trappedBy, game.battle.oppPokemon,
                              new DisplayText.Clear(game,
                              new WaitFrames(game, 3,
                              new DisplayText(game, game.battle.oppPokemon.name.toUpperCase()+"' hurt by "+game.battle.oppPokemon.trappedBy.toUpperCase()+"!",
                                              null, true,
                              new DepleteEnemyHealth(game, trap.damage,
                              new WaitFrames(game, 13,
                              null)))))));
                game.battle.oppPokemon.trapCounter -= 1;
                if (game.battle.oppPokemon.trapCounter <= 0) {
                    game.battle.oppPokemon.trappedBy = null;
                }
            }
            action.append(this.nextAction);
            game.actionStack.remove(this);
            game.insertAction(action);
            
            
            // TODO: trying out method where can only reference parent battle object, using Battle.this
            // probably revert at some point
            // TODO: if keeping, refactor to remove references to Game.staticGame
            //  likely need global actionStack or something

//            // Always goes you, then opponent
//            Game.staticGame.actionStack.remove(this);
//            if (Battle.this.oppPokemon.trappedBy != null) {
//                this.nextAction = new Battle.LoadAndPlayAnimation(Game.staticGame, Battle.this.oppPokemon.trappedBy, Battle.this.oppPokemon,
//                                  new DisplayText.Clear(Game.staticGame,
//                                  new WaitFrames(Game.staticGame, 3,
//                                  new DisplayText(Game.staticGame,
//                                                  Battle.this.oppPokemon.name.toUpperCase()+"' hurt by "+Battle.this.oppPokemon.trappedBy.toUpperCase()+"!",
//                                                  null, true,
//                                  new DepleteEnemyHealth(Game.staticGame,
//                                  new WaitFrames(Game.staticGame, 13,
//                                  this.nextAction))))));
//                Battle.this.oppPokemon.trapCounter -= 1;
//                if (Battle.this.oppPokemon.trapCounter <= 0) {
//                    Battle.this.oppPokemon.trappedBy = null;
//                }
//            }
//            if (Game.staticGame.player.currPokemon.trappedBy != null) {
//                this.nextAction = new Battle.LoadAndPlayAnimation(Game.staticGame, Game.staticGame.player.currPokemon.trappedBy, Game.staticGame.player.currPokemon,
//                                  new DisplayText.Clear(Game.staticGame,
//                                  new WaitFrames(Game.staticGame, 3,
//                                  new DisplayText(Game.staticGame,
//                                                  Game.staticGame.player.currPokemon.name.toUpperCase()+"' hurt by "+Game.staticGame.player.currPokemon.trappedBy.toUpperCase()+"!",
//                                                  null, true,
//                                  new DepleteFriendlyHealth(Game.staticGame.player.currPokemon,
//                                  new WaitFrames(Game.staticGame, 13,
//                                  this.nextAction))))));
//                Game.staticGame.player.currPokemon.trapCounter -= 1;
//                if (Battle.this.oppPokemon.trapCounter <= 0) {
//                    Battle.this.oppPokemon.trappedBy = null;
//                }
//            }
//            Game.staticGame.insertAction(this.nextAction);
        }
    }

    /**
     * Play turn animations. Includes attack, item, switch, run.
     *
     * If client, get player order and attack choices from game.battle.network. Else, determine manually.
     */
    static class DoTurn extends Action {
        Type type = Type.ATTACK;

        public DoTurn(Game game, Action nextAction) {
            this(game, Type.ATTACK, nextAction);
        }

        public DoTurn(Game game, Type type, Action nextAction) {
            this.type = type;
            this.nextAction = nextAction;
        }

        public int getLayer() {return 500;}

        @Override
        public void step(Game game) {
            // Reset flinched flags
            game.player.currPokemon.flinched = false;
            game.battle.oppPokemon.flinched = false;

            boolean oppFirst = false;  // opponent doesn't go first in case of run, item, or switch,
                                       // so default to false.
            boolean isFriendly = true;
            Attack enemyAttack;
            Action playerAction;
            Action doTurn;

            if (this.type == Type.SWITCH) {
                game.player.numFlees = 0;
                // stop drawing friendly healthbar
                game.actionStack.remove(game.battle.drawAction.drawFriendlyHealthAction);
                game.battle.drawAction.drawFriendlyHealthAction = null;
                // stop drawing friendly sprite
                game.actionStack.remove(game.battle.drawAction.drawFriendlyPokemonAction);
                game.battle.drawAction.drawFriendlyPokemonAction = null;

                game.player.currPokemon = game.player.pokemon.get(DrawPokemonMenu.currIndex);
                playerAction = new DrawPokemonMenu.Intro(13,
                               new DisplayText(game, "Go! "+game.player.currPokemon.name.toUpperCase()+"!",
                                               null, true, false,
                               new ThrowOutPokemonCrystal(game,
                               new PlaySound(game.player.currPokemon,
                               new WaitFrames(game, 6,
                               new DrawFriendlyHealth(game,
//                               new DisplayText.Clear(game,
//                               new WaitFrames(game, 3,
//                               new WaitFrames(game, 15,
//                               new DrawBattleMenuNormal(game,
                               null))))));
            }
            else if (this.type == Type.ITEM) {
                game.player.numFlees = 0;
                BattleTurnData turnData = game.battle.network.turnData;
                String itemName = turnData.itemName.toLowerCase();
                if (itemName.contains("ball")) {
                    int numWobbles;  // 0 wobbles == caught
                    if (game.type != Game.Type.CLIENT) {
                        numWobbles = Battle.gen2CalcIfCaught(game, game.battle.oppPokemon, itemName);
                    }
                    else {
                        // Get number of wobbles result from server
                        numWobbles = turnData.numWobbles;
                    }
                    // TODO: remove
//                    System.out.println("numWobbles");
//                    System.out.println(numWobbles);
                    // TODO: refactors, stop using catchAction, catchPokemon_wigglesThenCatch needs to insert nextAction
                    // currently it just ignores nextAction.
                    Action catchAction;
                    if (numWobbles == 0) {
//                        catchAction = new CatchPokemonMiss(game,
//                                      null);
                        catchAction = new CatchPokemonWobbles0Times(game,
                                      null);
                    }
                    else if (numWobbles == 1) {
                        catchAction = new CatchPokemonWobbles1Time(game,
                                      null);
                    }
                    else if (numWobbles == 2) {
                        catchAction = new CatchPokemonWobbles2Times(game,
                                      null);
                    }
                    else if (numWobbles == 3) {
                        catchAction = new CatchPokemonWobbles3Times(game,
                                      null);
                    }
                    else {
                        catchAction = new CatchPokemonWobblesThenCatch(game, null);
                    }
                    // Display text, throw animation, catch or not
                    playerAction = new DisplayText(game, game.player.name+" used "+itemName.toUpperCase()+"!",
                                                   null, catchAction,
                                   new ThrowPokeball(game,
                                   catchAction));
                    // If pokemon was caught, don't do anything else this turn.
                    if (numWobbles == -1) {
                        game.battle.oppPokemon.inBattle = false;
                        game.insertAction(playerAction);
                        game.actionStack.remove(this);
                        game.battle.network.turnData = null;
                        return;
                    }
                }
                else if (itemName.equals("silph scope")) {

                    playerAction = new DisplayText(game, game.player.name+" used "+itemName.toUpperCase()+"!",
                                                   null, true, false,
                                   new SplitAction(
                                       new WaitFrames(game, 96,
                                       new CallMethod(game.battle.oppPokemon, "revealGhost", new Object[] {},
                                       null)),
                                   new FadeAnim(game, 8,
                                   new SplitAction(new WaitFrames(game, 4,
                                                   new PlaySound(new Pokemon(game.battle.oppPokemon.specie.name,10),
                                                   null)),
                                   new PokemonIntroAnim(
                                   new DisplayText.Clear(game,
                                   new WaitFrames(game, 3,
                                   new DisplayText(game, "Enemy "+game.battle.oppPokemon.specie.name.toUpperCase()+" was revealed!",
                                                   null, null,
                                   null))))))));
                }
                else if (itemName.contains("berry") || itemName.equals("moomoo milk")) {
                    // Don't do anything, action was already performed.
                    playerAction = new Action() {
                        public String getCamera() {
                            return "gui";
                        }
                    };
                }
                else {
                    playerAction =  new DisplayText(game, "Dev note - Invalid item.", null, null,
                                    null);
                }
            }
            else if (this.type == Type.RUN) {
                boolean runSuccessful;
                if (game.type != Game.Type.CLIENT) {
                    runSuccessful = game.battle.calcIfRunSuccessful(game, game.player);
                }
                else {
                    // Get run result from server
                    BattleTurnData turnData = game.battle.network.turnData;
                    runSuccessful = turnData.runSuccessful;
                }
                if (runSuccessful) {
                    game.player.numFlees = 0;
                    game.battle.oppPokemon.inBattle = false;
                    game.actionStack.remove(this);
                    game.insertAction(new WaitFrames(game, 18,
                                      new DisplayText(game, "Got away safely!", null, null,
                                      new SplitAction(new BattleFadeOut(game,
                                                      new SetField(game, "playerCanMove", true, null)),
                                      new BattleFadeOutMusic(game,
                                      new SetField(game.musicController, "resumeOverworldMusic", true,
                                      null))))));
                    game.insertAction(new PlaySound("run1", null));
                    game.battle.network.turnData = null;
                    return;
                }
                // Failed to run away if we got here.
                game.player.numFlees++;
                playerAction = new DisplayText(game, "Canì escape!",
                                               null, null,
                               null);
            }
            // else, player selected attack
            else {
                game.player.numFlees = 0;  // reset this counter if attack is picked.
                Attack playerAttack;
                if (game.type != Game.Type.CLIENT) {
                    // Find which pokemon is first
                    int yourSpeed = game.player.currPokemon.currentStats.get("speed");
                    int oppSpeed = game.battle.oppPokemon.currentStats.get("speed");

                    if (yourSpeed > oppSpeed) {
                        oppFirst = false;
                    }
                    else if (yourSpeed < oppSpeed) {
                        oppFirst = true;
                    }
                    else {
                        int randNum = game.map.rand.nextInt(2);
                        if (randNum == 0) {
                            oppFirst = true;
                        }
                    }
                    String attackName = game.player.currPokemon.attacks[DrawAttacksMenu.curr];
                    if (attackName == null) {
                        attackName = "struggle";
                    }
                    playerAttack = game.battle.attacks.get(attackName.toLowerCase());
                    playerAttack.isCrit = Battle.gen2DetermineCrit(game.player.currPokemon, playerAttack);
                    playerAttack.damage = Battle.gen2CalcDamage(game.player.currPokemon, playerAttack, game.battle.oppPokemon);

                    // TODO: remove
//                    if (game.battle.oppPokemon.trappedBy == null &&
//                        playerAttack.name.toLowerCase().equals("whirlpool") ||
//                        playerAttack.name.toLowerCase().equals("fire spin") ||
//                        playerAttack.name.toLowerCase().equals("wrap") ||
//                        playerAttack.name.toLowerCase().equals("thunder cage") ||
//                        playerAttack.name.toLowerCase().equals("clamp")) {
//                        // 2-5 turns for trap
//                        game.battle.oppPokemon.trappedBy = playerAttack.name.toLowerCase();
//                        game.battle.oppPokemon.trapCounter = game.map.rand.nextInt(4) + 2;
//                        if (playerAttack.name.toLowerCase().equals("thunder cage")) {
//                            game.battle.oppPokemon.trapCounter = game.map.rand.nextInt(2) + 4;
//                        }
//                        //
//                    }
                }
                // if this is a CLIENT, get all outcomes of attacks etc from the server.
                else {
                    BattleTurnData turnData = game.battle.network.turnData;
                    oppFirst = turnData.oppFirst;
                    playerAttack = turnData.playerAttack;
                    game.player.currPokemon.trappedBy = turnData.playerTrappedBy;
                    game.player.currPokemon.trapCounter = turnData.playerTrapCounter;
                }
                playerAction =  //new DisplayText(game, game.player.currPokemon.name.toUpperCase()+" used "+playerAttack.name.toUpperCase()+"!",
                                //                null, true, false,
                                new AttackAnim(game, playerAttack, isFriendly,
                                null);
            }
            // If expecting player to switch (after friendly pokemon fainted), enemy does nothing
            if (game.battle.network.expectPlayerSwitch) {
                game.battle.network.expectPlayerSwitch = false;
                doTurn = playerAction;
                doTurn.append(new DisplayText.Clear(game,
                              new WaitFrames(game, 3,
                              this.nextAction)));
                game.actionStack.remove(this);
                game.insertAction(doTurn);
                game.battle.network.turnData = null;
                return;
            }
            // Select enemy attack
            if (game.type != Game.Type.CLIENT) {
                // TODO: remove
//                int attackIndex = game.map.rand.nextInt(game.battle.oppPokemon.attacks.length);
//                String attackChoice = game.battle.oppPokemon.attacks[attackIndex];
//                if (attackChoice == null) {
//                    attackChoice = "Struggle";
//                }

                // TODO: is enemy able to choose disabled move?
                // TODO: check disabled index
                ArrayList<String> validAttacks = new ArrayList<String>();
                int i = 0;
                for (String attack : game.battle.oppPokemon.attacks) {
                    if (attack != null && game.battle.oppPokemon.disabledIndex != i) {
                        validAttacks.add(attack);
                    }
                    i++;
                }
                String attackChoice = null;
                if (validAttacks.isEmpty()) {
                    attackChoice = "struggle";
                }
                else {
                    attackChoice = validAttacks.get(game.map.rand.nextInt(validAttacks.size()));
                }
                enemyAttack = game.battle.attacks.get(attackChoice.toLowerCase());
                // TODO: debug, remove
//                for (int i=0; i<game.battle.oppPokemon.attacks.length;i++) {
//                    System.out.println(game.battle.oppPokemon.attacks[i]);
//                }
//                System.out.println(attackChoice);
//                System.out.println(enemyAttack.name);
                enemyAttack.isCrit = Battle.gen2DetermineCrit(game.battle.oppPokemon, enemyAttack);
                enemyAttack.damage = Battle.gen2CalcDamage(game.battle.oppPokemon, enemyAttack, game.player.currPokemon);

                // TODO: remove
//                // check if attack traps player pokemon
//                if (game.player.currPokemon.trappedBy == null &&
//                    enemyAttack.name.toLowerCase().equals("whirlpool") ||
//                    enemyAttack.name.toLowerCase().equals("fire spin") ||
//                    enemyAttack.name.toLowerCase().equals("wrap") ||
//                    enemyAttack.name.toLowerCase().equals("thunder cage") ||
//                    enemyAttack.name.toLowerCase().equals("clamp")) {
//                    // 2-5 turns for trap
//                    game.player.currPokemon.trappedBy = enemyAttack.name.toLowerCase();
//                    game.player.currPokemon.trapCounter = game.map.rand.nextInt(4) + 2;
//                    // 4-5 turns for thunder cage
//                    if (enemyAttack.name.toLowerCase().equals("thunder cage")) {
//                        game.player.currPokemon.trapCounter = game.map.rand.nextInt(2) + 4;
//                    }
//                }
            }
            // If Client, get enemy attack that was sent from server.
            else {
                BattleTurnData turnData = game.battle.network.turnData;
                enemyAttack = turnData.enemyAttack;
                game.battle.oppPokemon.trappedBy = turnData.enemyTrappedBy;
                game.battle.oppPokemon.trapCounter = turnData.enemyTrapCounter;
                // TODO: debug, remove
//                System.out.println("enemyAttack.damage");
//                System.out.println(enemyAttack.damage);
            }
            Action enemyAction =  //new DisplayText(game, "Enemy "+game.battle.oppPokemon.name.toUpperCase()+" used "+enemyAttack.name.toUpperCase()+"!",
                                  //                null, true, false, // TODO: remove if unused
                                  new AttackAnim(game, enemyAttack, !isFriendly,
                                  null);
            if (!oppFirst) {
                doTurn = playerAction;
                doTurn.append(new DisplayText.Clear(game,
                              new WaitFrames(game, 3,
                              enemyAction)));

            }
            else{
                doTurn = enemyAction;
                doTurn.append(new DisplayText.Clear(game,
                              new WaitFrames(game, 3,
                              playerAction)));
            }
            
            // TODO: probably move this to checktrapped or something
            
            // Always goes you, then opponent for trap check
            // This has to be checked after hit/miss has been determined
            doTurn.append(game.battle.new CheckTrapped(game, null));

            doTurn.append(new DisplayText.Clear(game,
                          new WaitFrames(game, 3,
                          this.nextAction)));
            game.actionStack.remove(this);
            game.insertAction(doTurn);
            game.battle.network.turnData = null;
            return;
        }

        public enum Type {
            ATTACK,
            SWITCH,
            ITEM,
            RUN
        }
    }

    /**
     * I realized while recording confuse ray that attack anims weren't perfectly consistent every time
     * - I forced visualboy to run using gpu, and they started being perfectly consistent (attack_anims/hypnosis_player_gsc/frames-compare). Very important to use gpu from now on.
     * - Technically I would need to go back and redo everything before I recorded the hypnosis animation (ending with psybeam). In reality it's probably good enough, but worth noting.
     */
    static class LoadAndPlayAnimation extends Action {
        public int layer = 110;
        String name;
        HashMap<Integer, String> metadata = new HashMap<Integer, String>();
        Music sound;
        int frameNum = 1;
        Texture currText;
        Sprite currFrame;
        Pokemon target;
        boolean firstStep = true;
        Matrix4 translation;
        Vector2 playerSpriteOrigin;
        Vector2 enemySpriteOrigin = new Vector2();
        Pixmap pixmap;
        int pixmapX;
        int pixmapY;
        // TODO: remove
        ShaderProgram grayscaleShader = new ShaderProgram(EvolutionAnim.vertexShader,
                                                          EvolutionAnim.fragmentShader);

        public LoadAndPlayAnimation(Game game, String name, Pokemon target, Action nextAction) {
            this.name = name.toLowerCase().replace(' ', '_');
            if (target == game.player.currPokemon) {
                this.name = this.name+"_enemy_gsc";
            }
            else if (game.battle.oppPokemon != null && target == game.battle.oppPokemon) {
                this.name = this.name+"_player_gsc";
            }
            else {
                this.name = this.name+"_gsc";
            }
            this.target = target;
            this.nextAction = nextAction;
        }
        public String getCamera() {return "gui";}
 
        public int getLayer(){return this.layer;}
 
        @Override
        public void step(Game game) {
            if (this.firstStep) {
                // I did the Psychic animation manually before I ripped them and put them under attacks/
                if (this.name.contains("psychic")) {
                    game.actionStack.remove(this);
                    game.insertAction(new Attack.Psychic(game, this.target, false, this.nextAction));
                    return;
                }
                else if (this.name.contains("crush_grip")) {
                    game.actionStack.remove(this);
                    game.insertAction(new Attack.CrushGrip(game, this.target, this.nextAction));
                    return;
                }

                // Load metadata for each frame
                // ex: player_healthbar_gone -> means to make player's healthbar transparent during this frame
                System.out.println(this.name);
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
                this.playerSpriteOrigin = new Vector2(game.player.currPokemon.backSprite.getX(),
                                                      game.player.currPokemon.backSprite.getY());
                if (game.battle.oppPokemon != null) {
                    this.enemySpriteOrigin = new Vector2(game.battle.oppPokemon.sprite.getX(),
                                                         game.battle.oppPokemon.sprite.getY());
                }
//                this.enemyHealthbarOrigin = new Vector2(game.battle.drawAction.drawEnemyHealthAction.bgSprite.getX(),
//                                                        game.battle.drawAction.drawEnemyHealthAction.bgSprite.getY());
                
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
 
            // Reset vars at beginning
            DrawEnemyHealth.shouldDraw = true;
            DrawFriendlyHealth.shouldDraw = true;
            DrawBattle.shouldDrawOppPokemon = true;
            EvolutionAnim.isGreyscale = false;
            SpriteProxy.inverseColors = false;
            SpriteProxy.darkenColors1 = false;
            SpriteProxy.darkenColors2 = false;
            SpriteProxy.darkenColors3 = false;
            SpriteProxy.lightenAllColors1 = false;
            SpriteProxy.lightenAllColors2 = false;
            SpriteProxy.confuseRayColors1 = false;
            SpriteProxy.confuseRayColors2 = false;
            game.player.currPokemon.backSprite.lightenColors1 = false;
            game.player.currPokemon.backSprite.lightenColors2 = false;
            if (game.battle.oppPokemon != null) {
                game.battle.oppPokemon.sprite.lightenColors1 = false;
                game.battle.oppPokemon.sprite.lightenColors2 = false;
                game.battle.oppPokemon.sprite.setPosition(this.enemySpriteOrigin.x, this.enemySpriteOrigin.y);
            }
            game.player.currPokemon.backSprite.lightenColors2 = false;
   //            EvolutionAnim.playStarterCry = false;  // TODO: remove
   //            EvolutionAnim.playEvoCry = false;
            game.player.currPokemon.backSprite.setPosition(this.playerSpriteOrigin.x, this.playerSpriteOrigin.y);
//            game.battle.drawAction.drawEnemyHealthAction.bgSprite.setPosition(this.enemyHealthbarOrigin.x, this.enemyHealthbarOrigin.y);
            if (game.battle.drawAction != null) {
                game.battle.drawAction.drawEnemyHealthAction.translateAmt.set(0, 0);
            }
            game.uiBatch.setTransformMatrix(new Matrix4(new Vector3(0,0,0), new Quaternion(), new Vector3(1,1,1)));


            // if next frame doesn't exist in animation, return
            FileHandle filehandle = Gdx.files.internal("attacks/" + this.name + "/output/frame-" + String.format("%03d", this.frameNum) + ".png");
            if (!filehandle.exists()) {
                game.actionStack.remove(this);
                game.insertAction(this.nextAction);
 
                // TODO: need new actions not v effective, super eff, etc
   //                if (this.target == game.player.currPokemon) {
   //                    game.insertAction(new DefaultEnemyAttack(game.battle.oppPokemon, game.player.currPokemon, this.power, this.accuracy, this.nextAction));
   //                }
   //                else {
   //                    game.insertAction(new DefaultAttack(game, this.power, this.accuracy, this.nextAction));
   //                }
                return;
            }
            EvolutionAnim.drawPostEvoBottom = false;
            EvolutionAnim.drawPostEvoTop = false;
 
            // Draw water ripple effect if present
            if (this.metadata.containsKey(this.frameNum)) {
                String properties = this.metadata.get(this.frameNum);
                if (properties.contains("screenshot")) {
                    String[] values = properties.split("screenshot:")[1].split(" ")[0].split(",");
                    this.pixmapX = Integer.valueOf(values[0]);
                    this.pixmapY = Integer.valueOf(values[1]);
   //                    int width = Integer.valueOf(values[2]);
   //                    int height = Integer.valueOf(values[3]);
   //                    this.pixmap = ScreenUtils.getFrameBufferPixmap(this.pixmapX*3, this.pixmapY*3, width*3, height*3);
                    //
                    // TODO: just screenshotting full screen, since screen resizing will screw this up.
                    int offsetX = (int)((game.currScreen.x-((160*game.currScreen.y)/144))/2);  // x = (currWidth-160*currHeight/144)/2
                    this.pixmap = ScreenUtils.getFrameBufferPixmap(offsetX, 0, (int)game.currScreen.x-(offsetX*2), (int)game.currScreen.y);
                }
                if (properties.contains("row_copy")) {
                    // copy the screenshotted pixmap
                    float heightM = (game.currScreen.y / 144);  // 144*x = height
                    Pixmap newPixmap = new Pixmap((int)(this.pixmap.getWidth()/heightM), (int)(this.pixmap.getHeight()/heightM), Pixmap.Format.RGBA8888);
   //                    Pixmap newPixmap = new Pixmap(this.pixmap.getWidth(), this.pixmap.getHeight(), Pixmap.Format.RGBA8888);
                    newPixmap.setColor(new Color(0, 0, 0, 0));
                    newPixmap.fill();
                    // drawPixmap didn't work for some reason, copying manually
                    for (int i = 0; i < newPixmap.getWidth(); i++) {
                        for (int j = 0; j < newPixmap.getHeight(); j++) {
                            Color color = new Color(this.pixmap.getPixel((int)(i*heightM), (int)(j*heightM)));
                            newPixmap.drawPixel(i, j, Color.rgba8888(color));
                        }
                    }
   //                    newPixmap.drawPixmap(this.pixmap, 0, 0, this.pixmap.getWidth(), this.pixmap.getHeight(), 0, 0, (int)(this.pixmap.getWidth()/3f), (int)(this.pixmap.getHeight()/3f));
   //                    newPixmap.drawPixmap(this.pixmap, 0, 0);
                    // don't apply water ripple effect to player sprite
                    boolean playerSpriteIgnore = this.name.contains("player") && !this.name.contains("surf") && !this.name.contains("whirlpool");
                    boolean enemySpriteIgnore = this.name.contains("enemy") && !this.name.contains("surf") && !this.name.contains("whirlpool");
                    // syntax - row_copy:to_y,from_y
                    String[] copies = properties.split(" row_copy:");
                    int i=0;
                    for (String copy : copies) {
                        // skip first element
                        i++;
                        if (i==1) {
                            continue;
                        }
                        int targetY = Integer.valueOf(copy.split(",")[0]);
                        int sourceY = Integer.valueOf(copy.split(",")[1]);
   //                        System.out.println(String.valueOf(this.frameNum));
                        // copy row to new location (from the original pixmap)
                        for (int x = 0; x < newPixmap.getWidth(); x++) {
                            if (playerSpriteIgnore && x < 86 && (144-targetY) < 112) {
                                continue;
                            }
                            if (enemySpriteIgnore && x >= 96 && (144-targetY) >= 88) {
                                continue;
                            }
                            Color color = new Color(this.pixmap.getPixel((int)(x*heightM), (int)((144-sourceY)*heightM)));
                            newPixmap.drawPixel(x, 144-targetY, Color.rgba8888(color));
                        }
                    }
 
                    Sprite drawSprite = new Sprite(new Texture(newPixmap));
                    drawSprite.flip(false, true); // pixmaps are flipped for some reason
   //                    Sprite drawSprite = new Sprite(new Texture(this.pixmap));
   //                    drawSprite.scale(0.3f);
   //                    drawSprite.setPosition(this.pixmapX, this.pixmapY);
                    game.uiBatch.draw(drawSprite, this.pixmapX, this.pixmapY);
   //                    drawSprite.draw(game.floatingBatch);
                }
            }
 
            // Draw current frame
//            this.currText = new Texture(filehandle); // TODO: test
            this.currText = TextureCache.get(filehandle);
            this.currFrame = new Sprite(this.currText, 0, 0, 160, 144);
            this.currFrame.draw(game.uiBatch);
 
            // Handle metadata
            if (this.metadata.containsKey(this.frameNum)) {
                String properties = this.metadata.get(this.frameNum);
                if (properties.contains("enemy_healthbar_gone")) {
                    DrawEnemyHealth.shouldDraw = false;
                }
                if (properties.contains("player_healthbar_gone")) {
                    DrawFriendlyHealth.shouldDraw = false;
                }
                if (properties.contains("enemy_sprite_gone")) {
                    DrawBattle.shouldDrawOppPokemon = false;
                }
                if (properties.contains("screen_translate_y")) {
                    int translateAmt = Integer.valueOf(properties.split("screen_translate_y:")[1].split(" ")[0]);
                    game.uiBatch.setTransformMatrix(new Matrix4(new Vector3(0,translateAmt,0), new Quaternion(), new Vector3(1,1,1)));
                }
                if (properties.contains("screen_translate_x")) {
                    int translateAmt = Integer.valueOf(properties.split("screen_translate_x:")[1].split(" ")[0]);
                    game.uiBatch.setTransformMatrix(new Matrix4(new Vector3(translateAmt,0,0), new Quaternion(), new Vector3(1,1,1)));
                }
                if (properties.contains("player_translate_x")) {
                    int translateAmt = Integer.valueOf(properties.split("player_translate_x:")[1].split(" ")[0]);
                    game.player.currPokemon.backSprite.setPosition(game.player.currPokemon.backSprite.getX()+translateAmt, game.player.currPokemon.backSprite.getY());
                }
                if (properties.contains("player_translate_y")) {
                    int translateAmt = Integer.valueOf(properties.split("player_translate_y:")[1].split(" ")[0]);
                    game.player.currPokemon.backSprite.setPosition(game.player.currPokemon.backSprite.getX(), game.player.currPokemon.backSprite.getY()+translateAmt);
                }
                if (properties.contains("enemy_translate_x")) {
                    int translateAmt = Integer.valueOf(properties.split("enemy_translate_x:")[1].split(" ")[0]);
                    game.battle.oppPokemon.sprite.setPosition(game.battle.oppPokemon.sprite.getX()+translateAmt, game.battle.oppPokemon.sprite.getY());
//                    game.battle.drawAction.drawEnemyHealthAction.bgSprite.setPosition(this.enemyHealthbarOrigin.x+translateAmt, this.enemyHealthbarOrigin.y);
                    game.battle.drawAction.drawEnemyHealthAction.translateAmt.set(translateAmt, 0);
                }
                if (properties.contains("enemy_translate_y")) {
                    int translateAmt = Integer.valueOf(properties.split("enemy_translate_y:")[1].split(" ")[0]);
                    game.battle.oppPokemon.sprite.setPosition(game.battle.oppPokemon.sprite.getX(), game.battle.oppPokemon.sprite.getY()+translateAmt);
                }
                if (properties.contains("evo_top_sprite_changed")) {
                    EvolutionAnim.drawPostEvoTop = true;
                }
                if (properties.contains("evo_bottom_sprite_changed")) {
                    EvolutionAnim.drawPostEvoBottom = true;
                }
                if (properties.contains("draw_hatch_bottom_sprite")) {
                    EggHatchAnim.drawPostHatchBottom = true;
                }
                if (properties.contains("draw_hatch_top_sprite")) {
                    EggHatchAnim.drawPostHatchTop = true;
                }
                if (properties.contains("sprite_greyscale")) {
                    EvolutionAnim.isGreyscale = true;
                }
                if (properties.contains("darken_effect1")) {
                    SpriteProxy.darkenColors1 = true;
                }
                if (properties.contains("darken_effect2")) {
                    SpriteProxy.darkenColors2 = true;
                }
                if (properties.contains("darken_effect3")) {
                    SpriteProxy.darkenColors3 = true;
                }
                if (properties.contains("confuseray_effect1")) {
                    SpriteProxy.confuseRayColors1 = true;
                }
                if (properties.contains("confuseray_effect2")) {
                    SpriteProxy.confuseRayColors2 = true;
                }
                if (properties.contains(" lighten_effect1")) {
                    SpriteProxy.lightenAllColors1 = true;
                }
                if (properties.contains(" lighten_effect2")) {
                    SpriteProxy.lightenAllColors2 = true;
                }
                if (properties.contains("player_lighten_effect1")) {
                    game.player.currPokemon.backSprite.lightenColors1 = true;
                }
                if (properties.contains("player_lighten_effect2")) {
                    game.player.currPokemon.backSprite.lightenColors2 = true;
                }
                if (properties.contains("enemy_lighten_effect1")) {
                    game.battle.oppPokemon.sprite.lightenColors1 = true;
                }
                if (properties.contains("enemy_lighten_effect2")) {
                    game.battle.oppPokemon.sprite.lightenColors2 = true;
                }
                
                if (properties.contains("play_evo_fanfare")) {
                    EvolutionAnim.playSound = true;
                }
                if (properties.contains("inverse_colors")) {
                    SpriteProxy.inverseColors = true;
                }
            }
            this.frameNum++;
        }
    }

    class Network {
        BattleTurnData turnData;
        boolean expectPlayerSwitch = false;

        public Network() {}
    }

        /**
         * Wait for the server to send turn data back to client.
         *
         * Example: the player sends an attack, waits to see result of the attack.
         */
        static class WaitTurnData extends Action {
            Action text;

            public WaitTurnData(Game game, Action nextAction) {
                this.nextAction = nextAction;
            }

            @Override
            public void firstStep(Game game) {
                DisplayText.textPersist = false;  // Clear any displayed text
                this.text = new DisplayText(game, "Waiting for server...", null, true, false, null);
                game.insertAction(text);
                this.text.step(game);
            }

            public String getCamera() {return "gui";}

            public int getLayer() {return 500;}

            @Override
            public void step(Game game) {
                if (game.battle.network.turnData == null) {
                    return;
                }
                // There was an issue when I didn't have this line. Action order went like:
                // 1 - WaitTurnData.firstStep - created the DisplayText(game, "Waiting for server...", ...), and calls step,
                //     which will set DisplayText.textPersist = true
                // 2 - WaitTurnData.step - set DisplayText.textPersist = false and insert nextAction, in this case nextAction was
                //     a new DisplayText(game, "...", false, true, ...). This action was inserted before the "Waiting for server..."
                //     DisplayText Action because it is on the same layer.
                // 3 - The new DisplayText(game, "...", false, true, ...).step() is called, which re-sets DisplayText.textPersist = true
                // 4 - DisplayText(game, "Waiting for server...", ...).step() is called, sees that DisplayText.textPersist == true so it
                //     keeps drawing the text, and because it's called second it's displayed over the previous text.
                // So basically, it was sort of a race conditions that was obfuscated by the action chaining. Very hard to debug,
                // need to think about what should be changed here.
                game.actionStack.remove(this.text);  // this line.
                DisplayText.textPersist = false;  // Clear any displayed text
                game.actionStack.remove(this);
                game.insertAction(this.nextAction);
            }
        }
}

// scroll both players into view
class BattleAnimPositionPlayers extends Action {
    ArrayList<Vector2> moves_relative;
    Vector2 move;

    public int layer = 140;
    public BattleAnimPositionPlayers(Game game, Action nextAction) {
        this.nextAction = nextAction;

        this.moves_relative = new ArrayList<Vector2>();

        // animation to play
        for (int i = 0; i < 72; i++) {
            moves_relative.add(new Vector2(2,0));
        }


        // TODO: this worked for the gen1 backSprite
        // somehow preserve this if player is doing Gen 1 style battle?
//        game.player.battleSprite.setPosition(175+1-8-2, 71+1-10);//(3*175+1,3*71+1);
//        game.player.battleSprite.setScale(2);
        game.player.battleSprite.setPosition(162, 49);
//        game.player.battleSprite.setPosition(162-30, 49);  // used for saving video of pokemon sprite

        // TODO - x and y pos not correct...
        // Maybe this one was correct for red/blue, idk
//        game.battle.oppPokemon.sprite.setPosition(-30-4-1-14,106+2-5-15);//(3*-30,3*106+2); 
//        game.battle.oppPokemon.sprite.setPosition(-30-4-1-14-24,106+2-5-15-16);  // used for saving video of pokemon sprite

        // Measured using screenshots from crystal
        game.battle.oppPokemon.sprite.setPosition(92 -140, 88);
        if (game.battle.oppPokemon.sprite.getWidth() <= 48) {
            game.battle.oppPokemon.sprite.setPosition(98 -140, 88);
        }
        else if (game.battle.oppPokemon.sprite.getWidth() <= 40) {
            game.battle.oppPokemon.sprite.setPosition(104 -140, 88);
        }

        // note - i think my previous x was off by 1/3 a pixel, b/c val wasn't divisible by 3.
         // I am sticking with new x pos, which is really close
        // game.battle.oppPokemon.sprite.setScale(3);
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        // if done with anim, do nextAction
        if (moves_relative.isEmpty()) {
            game.insertAction(this.nextAction);
            game.actionStack.remove(this);
            return;
        }

        // get next frame
        this.move = moves_relative.get(0);

        float xPos = game.player.battleSprite.getX() - move.x;//*3;
        game.player.battleSprite.setX(xPos);

        xPos = game.battle.oppPokemon.sprite.getX() + move.x;//*3;
        game.battle.oppPokemon.sprite.setX(xPos);

        moves_relative.remove(0);
    }

}

// fade out of battle to white
// fade out music too
class BattleFadeOut extends Action {
    ArrayList<Sprite> frames;
    Sprite frame;
    int speed = 1;
    public static boolean whiteScreen = false;

    public int layer = 129;

    public BattleFadeOut(Game game, Action nextAction) {
        this(game, 1, nextAction);
    }

    public BattleFadeOut(Game game, int speed, Action nextAction) {
        this.nextAction = nextAction;
        this.speed = speed;
        this.frames = new ArrayList<Sprite>();

        // fade out from white anim
        Texture text1 = new Texture(Gdx.files.internal("battle/intro_frame6.png"));
        Sprite sprite1 = new Sprite(text1);
        for (int i=0; i < 14*this.speed; i++) {
            this.frames.add(sprite1);
        }
        text1 = new Texture(Gdx.files.internal("battle/intro_frame5.png"));
        sprite1 = new Sprite(text1);
        for (int i=0; i < 8*this.speed; i++) {
            this.frames.add(sprite1);
        }
        text1 = new Texture(Gdx.files.internal("battle/intro_frame4.png"));
        sprite1 = new Sprite(text1);
        for (int i=0; i < 8*this.speed; i++) {
            this.frames.add(sprite1);
        }
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        // Should only happen once. not sure that repeat matters
        game.actionStack.remove(game.battle.drawAction); // stop drawing the battle
        game.battle.drawAction = null;

        // if done with anim, do nextAction
        if (frames.isEmpty()) {
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
            
            // TODO: remove
            // TODO: test
//            game.playerCanMove = true;
            
            
            DrawBattle.shouldDrawOppPokemon = true;
            // TODO: gameboy game handles this differently
            // TODO: remove
//            game.player.currPokemon = game.player.pokemon.get(0);

            if (DisplayText.unownText) {
                ArrayList<TrainerTipsTile> signTiles = new ArrayList<TrainerTipsTile>();
                for (Tile tile : game.map.overworldTiles.values()) {
                    if (tile.nameUpper.contains("sign")) {
                        signTiles.add((TrainerTipsTile)tile);
                    }
                }
                if (signTiles.size() > 0) {
                    TrainerTipsTile tile = signTiles.get(game.map.rand.nextInt(signTiles.size()));
                    tile.isUnown = true;
//                    game.cam.position.set(tile.position, game.cam.position.z);
//                    System.out.println(tile.position);
                }
            }
            DisplayText.unownText = false;
            game.musicController.unownMusic = false;
//            game.map.unownSpawn = null;
            // Traps go away from player's current Pokemon
            game.player.currPokemon.trappedBy = null;
            game.player.currPokemon.trapCounter = 0;
            // TODO: test
            game.battle.oppPokemon.canMove = true;
            return;
        }

        // get next frame
        this.frame = frames.get(0);

        if (this.frame != null) {
            // gui version
            this.frame.setScale(3); // scale doesn't work in batch.draw
            this.frame.setPosition(16*10,16*9);
            this.frame.draw(game.uiBatch);
            // map version
            // game.batch.draw(this.frame, 16, -16);
        }
        // Used after player is out of pokemon, and want to draw white screen.
        if (BattleFadeOut.whiteScreen) {
            return;
        }

        frames.remove(0);
    }

    public static class WhiteScreen extends Action {
        boolean whiteScreen;

        public WhiteScreen(boolean whiteScreen, Action nextAction) {
            this.whiteScreen = whiteScreen;
            this.nextAction = nextAction;
        }

        @Override
        public void step(Game game) {
            BattleFadeOut.whiteScreen = this.whiteScreen;
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
        }
    }
}

class BattleFadeOutMusic extends Action {
    ArrayList<Float> frames;
    Float frame;
    float originalVolume;
    Music music;
    String timeOfDay;
    // flag this true if player fainted, required when player faints at night
    // and normally the fade out would be skipped.
    public static boolean playerFainted = false;
    public static boolean stop = false;
    public int layer = 129;

    public BattleFadeOutMusic(Game game, Action nextAction) {
        this.nextAction = nextAction;
        this.frames = new ArrayList<Float>();
        // fade out from white anim
        for (int i=0; i < 14; i++) {
            this.frames.add(0.3f);
        }
        for (int i=0; i < 14; i++) {
            this.frames.add(0.25f);
        }
        for (int i=0; i < 14; i++) {
            this.frames.add(0.2f);
        }
        for (int i=0; i < 14; i++) {
            this.frames.add(0.15f);
        }
        for (int i=0; i < 14; i++) {
            this.frames.add(0.1f);
        }
        for (int i=0; i < 7; i++) {
            this.frames.add(0.05f);
        }
        for (int i=0; i < 7; i++) {
            this.frames.add(0.025f);
        }
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void firstStep(Game game) {
        this.music = game.currMusic;
        this.timeOfDay = game.map.timeOfDay;
        game.musicController.battleFadeOut = true;
    }

    @Override
    public void step(Game game) {
        if (frames.isEmpty()) {
//            this.music.stop();  // TODO: remove
            game.musicController.inBattle = false;
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
            return;
        }
        // Get next frame
        this.frame = frames.get(0);
//        this.music.setVolume(frame);  // TODO: remove
        frames.remove(0);
    }

    // TODO: remove, unused now.
    public void oldStep(Game game) {
        // TODO: this interferes with fading out properly.
        if (game.map.timeOfDay.equals("night") && !BattleFadeOutMusic.playerFainted) {
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
            return;
        }

        if (this.firstStep) {
//            FadeMusic.currFadeMusic = this;  // prevents 2 fade musics from happening at same time
            this.originalVolume = game.currMusic.getVolume();
            this.music = game.currMusic;
            BattleFadeOutMusic.stop = false;
            this.firstStep = false;
        }
//        this.music = game.currMusic;

        // If done with anim, do nextAction
        if (frames.isEmpty() || BattleFadeOutMusic.stop) {  // || FadeMusic.currFadeMusic != this
            game.insertAction(this.nextAction);
            game.actionStack.remove(this);
            if (!BattleFadeOutMusic.stop) {
                this.music.stop();
            }
            this.music.setVolume(this.originalVolume);
            // Make sure we are out of battle before resetting map music
            if (game.battle.drawAction == null && !BattleFadeOutMusic.playerFainted) {
                game.currMusic = game.map.currRoute.music;
                game.currMusic.play();
            }
            BattleFadeOutMusic.playerFainted = false;
            BattleFadeOutMusic.stop = false;
//            FadeMusic.pause = false;
            return;
        }
        // get next frame
        this.frame = frames.get(0);
        this.music.setVolume(frame);
        frames.remove(0);
    }
}

class BattleIntro extends Action {
    ArrayList<Sprite> frames;
    Sprite frame;

    public int layer = 139;
    public BattleIntro(Action nextAction) {
        this.nextAction = nextAction;

        this.frames = new ArrayList<Sprite>();

        // Animation to play
        frames.add(null);
        frames.add(null);
        Texture text1 = new Texture(Gdx.files.internal("battle/intro_frame1.png"));
        Sprite sprite1 = new Sprite(text1);
        frames.add(sprite1);
        frames.add(sprite1);
        Texture text2 = new Texture(Gdx.files.internal("battle/intro_frame2.png"));
        Sprite sprite2 = new Sprite(text2);
        frames.add(sprite2);
        frames.add(sprite2);
        Texture text3 = new Texture(Gdx.files.internal("battle/intro_frame3.png"));
        Sprite sprite3 = new Sprite(text3);
        frames.add(sprite3);
        frames.add(sprite3);
        frames.add(sprite2);
        frames.add(sprite2);
        frames.add(sprite1);
        frames.add(sprite1);
        frames.add(null);
        frames.add(null);
        Texture text4 = new Texture(Gdx.files.internal("battle/intro_frame4.png"));
        Sprite sprite4 = new Sprite(text4);
        frames.add(sprite4);
        frames.add(sprite4);
        Texture text5 = new Texture(Gdx.files.internal("battle/intro_frame5.png"));
        Sprite sprite5 = new Sprite(text5);
        frames.add(sprite5);
        frames.add(sprite5);
        Texture text6 = new Texture(Gdx.files.internal("battle/intro_frame6.png"));
        Sprite sprite6 = new Sprite(text6);
        frames.add(sprite6);
        frames.add(sprite6);
        frames.add(sprite5);
        frames.add(sprite5);
        frames.add(sprite4);
        frames.add(sprite4);

        ArrayList<Sprite> cpyFrames = new ArrayList<Sprite> (this.frames);
        this.frames.addAll(cpyFrames);
        this.frames.addAll(cpyFrames);
        // this.frames.addAll(cpyFrames);
        frames.add(null);
        frames.add(null);
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        // get next frame
        this.frame = frames.get(0);

        if (this.frame != null) {
            this.frame.setScale(3); // scale doesn't work in batch.draw
            this.frame.setPosition(0,0);
            this.frame.draw(game.uiBatch);
            // game.batch.draw(this.frame, 0, -20);
        }

        frames.remove(0);

        // if done with anim, do nextAction
        if (frames.isEmpty()) {
            game.insertAction(this.nextAction);
            game.actionStack.remove(this);
//            if (this.nextAction.getLayer() >= this.getLayer()) {
//                this.nextAction.step(game);  // don't skip a frame
//            }
            return;
        }
    }

}

class BattleIntroAnim1 extends Action {
    ArrayList<Sprite> frames;
    Sprite frame;
    Sprite bgSprite;

    public int layer = 139;
    public BattleIntroAnim1(Action nextAction) {
        this.nextAction = nextAction;
        this.frames = new ArrayList<Sprite>();
        Texture text = new Texture(Gdx.files.internal("battle/battle_intro_anim1_sheet1.png"));
        // animation to play
        for (int i = 0; i < 28; i++) {
            frames.add(new Sprite(text, i*160, 0, 160, 144));
        }
        int numFrames = 42;
//        if (Game.staticGame.battle.oppPokemon != null && Game.staticGame.battle.oppPokemon.name.contains("regi")) {
//            numFrames = 42*3 +20;
//        }
        for (int i = 0; i < numFrames; i++) {
            frames.add(new Sprite(text, 27*160, 0, 160, 144));
        }
        text = new Texture(Gdx.files.internal("battle/intro_frame3.png"));
        this.bgSprite = new Sprite(text, 0, 0, 160, 144);
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        // if done with anim, do nextAction
        if (frames.isEmpty()) {
            game.insertAction(this.nextAction);
            game.actionStack.remove(this);
            // avoid lag
            nextAction.step(game);
            return;
        }

        // get next frame
        this.frame = frames.get(0);

        if (this.frame != null) {
            // gui version
                // this.frame.setScale(3); // scale doesn't work in batch.draw // used these when scaling mattered
                // this.frame.setPosition(16*10,16*9);
            this.frame.draw(game.uiBatch);
            // map version
            // game.batch.draw(this.frame, 16, -16);
        }
        frames.remove(0);
        
        // Needed for fullscreen
        game.uiBatch.draw(this.bgSprite, -160, 0);
        game.uiBatch.draw(this.bgSprite, 160, 0);
        game.uiBatch.draw(this.bgSprite, 0, -144);
        game.uiBatch.draw(this.bgSprite, 0, 144);
    }

}

class BattleIntroMusic extends Action {
    public int layer = 139;
    public BattleIntroMusic(Action nextAction) {
        this.nextAction = nextAction;
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        game.currMusic.pause();
        game.currMusic = game.battle.music;
        game.currMusic.play();
        game.insertAction(this.nextAction);
        game.actionStack.remove(this);
    }
}

class CatchPokemonMiss extends Action {
    ArrayList<Float> alphas;

    public int layer = 120;
    Sprite helperSprite;

    public CatchPokemonMiss(Game game, Action nextAction) {
        this.nextAction = nextAction;
        /*
        this.alphas = new ArrayList<Float>();
        for (int i = 0; i < 3; i++) {
            this.alphas.add(1f);
        }
        // 3 total events
        */
//        Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/helper13.png"));
//        this.helperSprite = new Sprite(text, 0, 0, 160, 144);
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        // - 1 frame then text dissappears
        // - 3 frames nothing, then text appears

        String textString = "You missed the POKéMON!";
        game.insertAction(new WaitFrames(game, 3,
                                                new DisplayText(game, textString, null, null, this.nextAction)
                                            )
                                        );

        game.actionStack.remove(this);
        return;
    }
}

class CatchPokemonWobbles0Times extends Action {
    ArrayList<Vector2> positions;
    Vector2 position;
    ArrayList<Sprite> sprites;
    Sprite sprite;
    ArrayList<Integer> repeats;
    String sound;
    ArrayList<String> sounds;
    ArrayList<Float> alphas;
    public int layer = 120;

    public CatchPokemonWobbles0Times(Game game, Action nextAction) {
        this.nextAction = nextAction;
      Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball1_color.png"));

      // initial sprite position
      this.position = new Vector2(114,88); // post scaling change
      this.positions = new ArrayList<Vector2>();
      this.positions.add(new Vector2(0, 0)); // wait 13
      this.positions.add(new Vector2(-19, -16));
      for (int i = 0; i < 6; i++) {
          this.positions.add(new Vector2(0,0)); // filler
      }
      // 11 total

      // Wiggle anim
      this.sprites =  new ArrayList<Sprite>();
      this.sprites.add(null); // draw nothing for 13 frames

      text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball_wiggleSheet1_color.png"));
      this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); // middle
      text = new Texture(Gdx.files.internal("throw_pokeball_anim/poof_sheet1.png"));
      this.sprites.add(new Sprite(text, 48*0, 0, 48, 48)); // poof1
      this.sprites.add(new Sprite(text, 48*1, 0, 48, 48)); // poof2
      this.sprites.add(new Sprite(text, 48*2, 0, 48, 48)); // poof3
      this.sprites.add(new Sprite(text, 48*3, 0, 48, 48)); // poof4
      this.sprites.add(new Sprite(text, 48*4, 0, 48, 48)); // poof5
      this.sprites.add(null); // draw nothing for 4 frames // done at this point

      // 11 total events
      this.repeats = new ArrayList<Integer>();

      this.repeats.add(13-1); // 13 frames before pokeball appears on ground
      this.repeats.add(44-1); // first middle sprite has 44 frames
      for (int i = 0; i < 4; i++) { // each poof has 5 frames
          this.repeats.add(5-1);
      }
      this.repeats.add(10-1); // final poof has 10 frames
      this.repeats.add(4-1); // 4 frames of nothing (before text box change)
      // 11 total

      // sounds to play
      this.sounds = new ArrayList<String>();
      this.sounds.add(null);
      this.sounds.add(null);
      this.sounds.add("poof1");
      for (int i = 0; i < 5; i++) {
          this.sounds.add(null);
      }
      // 11 total events

      // opposing pkmn sprite alphas
      this.alphas = new ArrayList<Float>();
      for (int i = 0; i < 7; i++) {
          this.alphas.add(0f);
      }
      this.alphas.add(1f);
      // 11 total events
    }


    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        // set sprite position
        // if done with anim, do nextAction
        if (positions.isEmpty() || sprites.isEmpty()) {
            String textString = "Darn! The POKéMON broke free!";
            // nextAction will be a battle menu draw action here
            game.insertAction(new DisplayText(game, textString, null, null, this.nextAction));
            // newAction.step(game);// need to draw the pokeball

            game.actionStack.remove(this);
            return;
        }

        // control alpha of opposing pkmn
        float currAlpha = this.alphas.get(0);
        if (currAlpha == 0f) {
            DrawBattle.shouldDrawOppPokemon = false;
        }
        else {
            DrawBattle.shouldDrawOppPokemon = true; 
        }

        // get next sound, play it
        this.sound = this.sounds.get(0);
        if (this.sound != null) {
            game.insertAction(new PlaySound(this.sound, null));
            this.sounds.set(0, null); // don't play same sound over again
        }

        // get next frame
        this.sprite = sprites.get(0);

        // debug
        // this.helperSprite.draw(game.floatingBatch); // debug

        // draw current sprite
        if (this.sprite != null) {
            // this.sprite.setScale(3); // post scaling change
            this.sprite.setPosition(position.x, position.y);
            this.sprite.draw(game.uiBatch);
        }

        // repeat sprite/pos for current object for 'frames[0]' number of frames.
        if (this.repeats.get(0) > 0) {
            this.repeats.set(0, this.repeats.get(0) - 1);
        }
        else {
            // since position is relative, only update once each time period
            this.position = this.position.add(positions.get(0));
            positions.remove(0);
            sprites.remove(0);
            repeats.remove(0);
            sounds.remove(0);
            alphas.remove(0);
        }
    }
}

class CatchPokemonWobbles1Time extends Action {
    ArrayList<Vector2> positions;
    Vector2 position;
    ArrayList<Sprite> sprites;
    Sprite sprite;
    ArrayList<Integer> repeats;
    String sound;
    ArrayList<String> sounds;

    ArrayList<Float> alphas;

    public int layer = 120;
    Sprite helperSprite; // just for helping me position the animation. delete later.

    public CatchPokemonWobbles1Time(Game game, Action nextAction) {
        this.nextAction = nextAction;

//        Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball1.png"));
        Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball1_color.png"));

        // initial sprite position
        this.position = new Vector2(114,88); // post scaling change

        this.positions = new ArrayList<Vector2>();
        this.positions.add(new Vector2(0,0)); // wait 13
        this.positions.add(new Vector2(-1,0));
        this.positions.add(new Vector2(1,0));
        this.positions.add(new Vector2(1,0));
        this.positions.add(new Vector2(-19,-16));
        for (int i = 0; i < 6; i++) {
            this.positions.add(new Vector2(0,0)); // filler
        }
        // 11 total

        // wiggle anim
        this.sprites =  new ArrayList<Sprite>();
        this.sprites.add(null); // draw nothing for 13 frames

//        text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball_wiggleSheet1.png"));
        text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball_wiggleSheet1_color.png"));
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); // middle
        this.sprites.add(new Sprite(text, 12*1, 0, 12, 12)); // left tilt
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); // middle
        this.sprites.add(new Sprite(text, 12*2, 0, 12, 12)); // right tilt
        text = new Texture(Gdx.files.internal("throw_pokeball_anim/poof_sheet1.png"));
        this.sprites.add(new Sprite(text, 48*0, 0, 48, 48)); // poof1
        this.sprites.add(new Sprite(text, 48*1, 0, 48, 48)); // poof2
        this.sprites.add(new Sprite(text, 48*2, 0, 48, 48)); // poof3
        this.sprites.add(new Sprite(text, 48*3, 0, 48, 48)); // poof4
        this.sprites.add(new Sprite(text, 48*4, 0, 48, 48)); // poof5
        this.sprites.add(null); // draw nothing for 4 frames // done at this point
        // 11 total events

        this.repeats = new ArrayList<Integer>();

        this.repeats.add(13-1); // 13 frames before pokeball appears on ground

        this.repeats.add(44-1); // first middle sprite has 44 frames
        for (int i = 0; i < 2; i++) { // left-middle-right tilt all have 4 frames
            this.repeats.add(4-1);
        }
        this.repeats.add(14-1); // final right tilt has 14 frames
        for (int i = 0; i < 4; i++) { // each poof has 5 frames
            this.repeats.add(5-1);
        }
        this.repeats.add(10-1); // final poof has 10 frames
        this.repeats.add(4-1); // 4 frames of nothing (before text box change)
        // 11 total

        // sounds to play
        this.sounds = new ArrayList<String>();
        this.sounds.add("pokeball_wiggle1"); // wiggle as ball appears (doing as white appears, so it's uniform)
        for (int i = 0; i < 4; i++) {
            this.sounds.add(null);
        }
        this.sounds.add("poof1");
        for (int i = 0; i < 5; i++) {
            this.sounds.add(null);
        }
        // 11 total events

        // opposing pkmn sprite alphas
        this.alphas = new ArrayList<Float>();
        for (int i = 0; i < 10; i++) {
            this.alphas.add(0f);
        }
        this.alphas.add(1f);
        // 11 total events

//        text = new Texture(Gdx.files.internal("throw_pokeball_anim/helper13.png"));
//        this.helperSprite = new Sprite(text, 0, 0, 160, 144);
        // this.helperSprite.setPosition(16*10,16*9); // post scaling change
        // this.helperSprite.setScale(3);
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        // set sprite position
        // if done with anim, do nextAction
        if (positions.isEmpty() || sprites.isEmpty()) {
            String textString = "Darn! The POKéMON broke free!";
            // nextAction will be a battle menu draw action here
            game.insertAction(new DisplayText(game, textString, null, null, this.nextAction));
            // newAction.step(game);// need to draw the pokeball

            game.actionStack.remove(this);
            return;
        }

        // control alpha of opposing pkmn
        float currAlpha = this.alphas.get(0);
        if (currAlpha == 0f) {
            DrawBattle.shouldDrawOppPokemon = false;
        }
        else {
            DrawBattle.shouldDrawOppPokemon = true; 
        }
//        game.battle.oppPokemon.sprite.setAlpha(currAlpha);

        // get next sound, play it
        this.sound = this.sounds.get(0);
        if (this.sound != null) {
            game.insertAction(new PlaySound(this.sound, null));
            this.sounds.set(0, null); // don't play same sound over again
        }

        // get next frame
        this.sprite = sprites.get(0);

        // debug
        // this.helperSprite.draw(game.floatingBatch); // debug

        // draw current sprite
        if (this.sprite != null) {
            // this.sprite.setScale(3); // post scaling change
            this.sprite.setPosition(position.x, position.y);
            this.sprite.draw(game.uiBatch);
        }

        // debug
//        if (this.repeats.size() == 1) { // debug
//            return;
//        }

        // repeat sprite/pos for current object for 'frames[0]' number of frames.
        if (this.repeats.get(0) > 0) {
            this.repeats.set(0, this.repeats.get(0) - 1);
        }
        else {
            // since position is relative, only update once each time period
            this.position = this.position.add(positions.get(0));
            positions.remove(0);
            sprites.remove(0);
            repeats.remove(0);
            sounds.remove(0);
            alphas.remove(0);
        }
    }
}

class CatchPokemonWobbles2Times extends Action {
    // Sprite pokeballSprite;

    ArrayList<Vector2> positions;
    Vector2 position;
    ArrayList<Sprite> sprites;
    Sprite sprite;
    ArrayList<Integer> repeats;
    String sound;
    ArrayList<String> sounds;
    ArrayList<Float> alphas;

    public int layer = 120;
    Sprite helperSprite; // just for helping me position the animation. delete later.

    public CatchPokemonWobbles2Times(Game game, Action nextAction) {
        this.nextAction = nextAction;

//        Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball1.png"));
        Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball1_color.png"));

        // initial sprite position
        this.position = new Vector2(114,88); // post scaling change

        this.positions = new ArrayList<Vector2>();
        this.positions.add(new Vector2(0,0)); // wait 13
        this.positions.add(new Vector2(-1,0));
        this.positions.add(new Vector2(1,0));
        this.positions.add(new Vector2(1,0));
        for (int i = 0; i < 1; i++) {
            this.positions.add(new Vector2(-1,0));
            this.positions.add(new Vector2(-1,0));
            this.positions.add(new Vector2(1,0));
            this.positions.add(new Vector2(1,0));
        }
        this.positions.add(new Vector2(-19,-16));
        for (int i = 0; i < 6; i++) {
            this.positions.add(new Vector2(0,0)); // filler
        }
        // 15 total

        // wiggle anim
        this.sprites =  new ArrayList<Sprite>();
        this.sprites.add(null); // draw nothing for 13 frames

//        text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball_wiggleSheet1.png"));
        text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball_wiggleSheet1_color.png"));
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); // middle
        this.sprites.add(new Sprite(text, 12*1, 0, 12, 12)); // left tilt
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); // middle
        this.sprites.add(new Sprite(text, 12*2, 0, 12, 12)); // right tilt
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); // middle
        this.sprites.add(new Sprite(text, 12*1, 0, 12, 12)); // left tilt
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); // middle
        this.sprites.add(new Sprite(text, 12*2, 0, 12, 12)); // right tilt
        text = new Texture(Gdx.files.internal("throw_pokeball_anim/poof_sheet1.png"));
        this.sprites.add(new Sprite(text, 48*0, 0, 48, 48)); // poof1
        this.sprites.add(new Sprite(text, 48*1, 0, 48, 48)); // poof2
        this.sprites.add(new Sprite(text, 48*2, 0, 48, 48)); // poof3
        this.sprites.add(new Sprite(text, 48*3, 0, 48, 48)); // poof4
        this.sprites.add(new Sprite(text, 48*4, 0, 48, 48)); // poof5
        this.sprites.add(null); // draw nothing for 4 frames // done at this point
        // 15 total events

        this.repeats = new ArrayList<Integer>();

        this.repeats.add(13-1); // 13 frames before pokeball appears on ground

        this.repeats.add(44-1); // first middle sprite has 44 frames
        for (int i = 0; i < 3; i++) { // left-middle-right tilt all have 4 frames
            this.repeats.add(4-1);
        }
        this.repeats.add(44-1); // first middle sprite has 44 frames
        for (int i = 0; i < 2; i++) { // left-middle-right tilt all have 4 frames
            this.repeats.add(4-1);
        }
        this.repeats.add(14-1); // final right tilt has 14 frames
        for (int i = 0; i < 4; i++) { // each poof has 5 frames
            this.repeats.add(5-1);
        }
        this.repeats.add(10-1); // final poof has 10 frames
        this.repeats.add(4-1); // 4 frames of nothing (before text box change)
        // 15 total

        // sounds to play
        this.sounds = new ArrayList<String>();
        // this.sounds.add(null); // wait 13 no sound
        this.sounds.add("pokeball_wiggle1"); // wiggle as ball appears (doing as white appears, so it's uniform)
        this.sounds.add(null);
        this.sounds.add("pokeball_wiggle1");
        for (int i = 0; i < 6; i++) {
            this.sounds.add(null);
        }
        this.sounds.add("poof1");
        for (int i = 0; i < 5; i++) {
            this.sounds.add(null);
        }
        // 15 total events

        // opposing pkmn sprite alphas
        this.alphas = new ArrayList<Float>();
        for (int i = 0; i < 14; i++) {
            this.alphas.add(0f);
        }
        this.alphas.add(1f);
        // 15 total events

//        text = new Texture(Gdx.files.internal("throw_pokeball_anim/helper13.png"));
//        this.helperSprite = new Sprite(text, 0, 0, 160, 144);
        // this.helperSprite.setPosition(16*10,16*9); // post scaling change
        // this.helperSprite.setScale(3);
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        // set opp pokemon sprite alpha
        // game.battle.oppPokemon.sprite.setAlpha(0); // delete at some point

        // set sprite position
        // if done with anim, do nextAction
        if (positions.isEmpty() || sprites.isEmpty()) {
            String textString = "Aww! It appeared to be caught!";
            // nextAction will be a battle menu draw action here
            game.insertAction(new DisplayText(game, textString, null, null, this.nextAction));
            // newAction.step(game);// need to draw the pokeball

            game.actionStack.remove(this);
            return;
        }

        // control alpha of opposing pkmn
        float currAlpha = this.alphas.get(0);
        if (currAlpha == 0f) {
            DrawBattle.shouldDrawOppPokemon = false;
        }
        else {
            DrawBattle.shouldDrawOppPokemon = true; 
        }
//        game.battle.oppPokemon.sprite.setAlpha(currAlpha);

        // get next sound, play it
        this.sound = this.sounds.get(0);
        if (this.sound != null) {
            game.insertAction(new PlaySound(this.sound, null));
            this.sounds.set(0, null); // don't play same sound over again
        }

        // get next frame
        this.sprite = sprites.get(0);

        // debug
        // this.helperSprite.draw(game.floatingBatch); // debug

        // draw current sprite
        if (this.sprite != null) {
            // this.sprite.setScale(3); // post scaling change
            this.sprite.setPosition(position.x, position.y);
            this.sprite.draw(game.uiBatch);
        }

        // debug
//        if (this.repeats.size() == 1) { // debug
//            return;
//        }

        // repeat sprite/pos for current object for 'frames[0]' number of frames.
        if (this.repeats.get(0) > 0) {
            this.repeats.set(0, this.repeats.get(0) - 1);
        }
        else {
            // since position is relative, only update once each time period
            this.position = this.position.add(positions.get(0));
            positions.remove(0);
            sprites.remove(0);
            repeats.remove(0);
            sounds.remove(0);
            alphas.remove(0);
        }
    }
}

// draws pokeball wiggling3 times and then releasing pokemon
 // still need to get wiggle animation done here
class CatchPokemonWobbles3Times extends Action {
    ArrayList<Vector2> positions;
    Vector2 position;
    ArrayList<Sprite> sprites;
    Sprite sprite;
    ArrayList<Integer> repeats;
    String sound;
    ArrayList<String> sounds;

    ArrayList<Float> alphas;

    public int layer = 120;
    public CatchPokemonWobbles3Times(Game game, Action nextAction) {
        // TODO - would be nice to confirm this version with a recording

        this.nextAction = nextAction;

//        Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball1.png"));
        Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball1_color.png"));

        // initial sprite position
        this.position = new Vector2(114,88); // post scaling change

        this.positions = new ArrayList<Vector2>();
        this.positions.add(new Vector2(0,0)); // wait 13
        this.positions.add(new Vector2(-1,0));
        this.positions.add(new Vector2(1,0));
        this.positions.add(new Vector2(1,0));
        for (int i = 0; i < 2; i++) {
            this.positions.add(new Vector2(-1,0));
            this.positions.add(new Vector2(-1,0));
            this.positions.add(new Vector2(1,0));
            this.positions.add(new Vector2(1,0));
        }
        this.positions.add(new Vector2(-19,-16));
        for (int i = 0; i < 6; i++) {
            this.positions.add(new Vector2(0,0)); // filler
        }
        // 19 total

        // wiggle anim
        this.sprites =  new ArrayList<Sprite>();
        this.sprites.add(null); // draw nothing for 13 frames

//        text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball_wiggleSheet1.png"));
        text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball_wiggleSheet1_color.png"));
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); // middle
        this.sprites.add(new Sprite(text, 12*1, 0, 12, 12)); // left tilt
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); // middle
        this.sprites.add(new Sprite(text, 12*2, 0, 12, 12)); // right tilt
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); // middle
        this.sprites.add(new Sprite(text, 12*1, 0, 12, 12)); // left tilt
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); // middle
        this.sprites.add(new Sprite(text, 12*2, 0, 12, 12)); // right tilt
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); // middle
        this.sprites.add(new Sprite(text, 12*1, 0, 12, 12)); // left tilt
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); // middle
        this.sprites.add(new Sprite(text, 12*2, 0, 12, 12)); // right tilt
        text = new Texture(Gdx.files.internal("throw_pokeball_anim/poof_sheet1.png"));
        this.sprites.add(new Sprite(text, 48*0, 0, 48, 48)); // poof1
        this.sprites.add(new Sprite(text, 48*1, 0, 48, 48)); // poof2
        this.sprites.add(new Sprite(text, 48*2, 0, 48, 48)); // poof3
        this.sprites.add(new Sprite(text, 48*3, 0, 48, 48)); // poof4
        this.sprites.add(new Sprite(text, 48*4, 0, 48, 48)); // poof5
        this.sprites.add(null); // draw nothing for 4 frames // done at this point // 18 total events

        this.repeats = new ArrayList<Integer>();

        this.repeats.add(13-1); // 13 frames before pokeball appears on ground

        this.repeats.add(44-1); // first middle sprite has 44 frames
        for (int i = 0; i < 3; i++) { // left-middle-right tilt all have 4 frames
            this.repeats.add(4-1);
        }
        this.repeats.add(44-1); // first middle sprite has 44 frames
        for (int i = 0; i < 3; i++) { // left-middle-right tilt all have 4 frames
            this.repeats.add(4-1);
        }
        this.repeats.add(44-1); // first middle sprite has 44 frames
        for (int i = 0; i < 2; i++) { // left-middle tilt both have 4 frames
            this.repeats.add(4-1);
        }
        this.repeats.add(14-1); // final right tilt has 14 frames
        for (int i = 0; i < 4; i++) { // each poof has 5 frames
            this.repeats.add(5-1);
        }
        this.repeats.add(10-1); // final poof has 10 frames
        this.repeats.add(4-1); // 4 frames of nothing (before text box change)
        // 19 total

        // sounds to play
         // TODO - wiggle sound
        this.sounds = new ArrayList<String>();
        // this.sounds.add(null); // wait 13 no sound
        this.sounds.add("pokeball_wiggle1"); // wiggle as ball appears (doing as white appears, so it's uniform)
        this.sounds.add(null);
        this.sounds.add("pokeball_wiggle1");
        for (int i = 0; i < 3; i++) {
            this.sounds.add(null);
        }
        this.sounds.add("pokeball_wiggle1");
        for (int i = 0; i < 6; i++) {
            this.sounds.add(null);
        }
        this.sounds.add("poof1");
        for (int i = 0; i < 5; i++) {
            this.sounds.add(null);
        }
        // this.sounds.add("pokeball_wiggle1"); // this is in 3 wiggle?
//        for (int i = 0; i < 3; i++) {
//            this.sounds.add(null);
//        }

        // opposing pkmn sprite alphas
        this.alphas = new ArrayList<Float>();
        for (int i = 0; i < 18; i++) {
            this.alphas.add(0f);
        }
        this.alphas.add(1f);
        // 19 total events

//        text = new Texture(Gdx.files.internal("throw_pokeball_anim/helper13.png"));
//        this.helperSprite = new Sprite(text, 0, 0, 160, 144);
        // this.helperSprite.setPosition(16*10,16*9); // post scaling change
        // this.helperSprite.setScale(3);
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        // set opp pokemon sprite alpha
        // game.battle.oppPokemon.sprite.setAlpha(0); // delete at some point

        // set sprite position
        // if done with anim, do nextAction
        if (positions.isEmpty() || sprites.isEmpty()) {
            // PokemonCaught_Events - sprite and text
            // new DisplayText(game, string)
//            Action newAction = new PokemonCaught_Events(
//                                    game, new SplitAction(
//                                        new BattleFadeOut(game,
//                                                new playerStanding(game)
//                                        ),
//                                        new BattleFadeOutMusic(game, null)
//                                    )
//                                );

            // String textString = "Darn! The POKeMON broke free!";
            String textString = "Shoot! It was so close, too!";
            // nextAction will be a battle menu draw action here
            game.insertAction(new DisplayText(game, textString, null, null, this.nextAction));
            // newAction.step(game);// need to draw the pokeball

            // since pkmn was caught, add to players pokemon
            // game.player.pokemon.add(game.battle.oppPokemon);

            game.actionStack.remove(this);
            return;
        }

        // control alpha of opposing pkmn
        float currAlpha = this.alphas.get(0);
        if (currAlpha == 0f) {
            DrawBattle.shouldDrawOppPokemon = false;
        }
        else {
            DrawBattle.shouldDrawOppPokemon = true; 
        }
//        game.battle.oppPokemon.sprite.setAlpha(currAlpha);

        // get next sound, play it
        this.sound = this.sounds.get(0);
        if (this.sound != null) {
            game.insertAction(new PlaySound(this.sound, null));
            this.sounds.set(0, null); // don't play same sound over again
        }

        // get next frame
        this.sprite = sprites.get(0);

        // debug
        // this.helperSprite.draw(game.floatingBatch); // debug

        // draw current sprite
        if (this.sprite != null) {
            // this.sprite.setScale(3); // post scaling change
            this.sprite.setPosition(position.x, position.y);
            this.sprite.draw(game.uiBatch);
        }

        // debug
//        if (this.repeats.size() == 6) { // debug
//            return;
//        }

        // repeat sprite/pos for current object for 'frames[0]' number of frames.
        if (this.repeats.get(0) > 0) {
            this.repeats.set(0, this.repeats.get(0) - 1);
        }
        else {
            // since position is relative, only update once each time period
            this.position = this.position.add(positions.get(0));
            positions.remove(0);
            sprites.remove(0);
            repeats.remove(0);
            sounds.remove(0);
            alphas.remove(0);
        }
    }
}

class CatchPokemonWobblesThenCatch extends Action {
    ArrayList<Vector2> positions;
    Vector2 position;
    ArrayList<Sprite> sprites;
    Sprite sprite;
    ArrayList<Integer> repeats;
    String sound;
    ArrayList<String> sounds;
    Tile pedistalTile;

    public int layer = 120;
    public CatchPokemonWobblesThenCatch(Game game, Action nextAction) {
        this.nextAction = nextAction;

//        Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball1.png"));
        Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball1_color.png"));

        // initial sprite position
        this.position = new Vector2(114,88); // post scaling change

        this.positions = new ArrayList<Vector2>();
        this.positions.add(new Vector2(0,0)); // wait 13
        for (int i = 0; i < 3; i++) {
            this.positions.add(new Vector2(-1,0));
            this.positions.add(new Vector2(1,0));
            this.positions.add(new Vector2(1,0));
            this.positions.add(new Vector2(-1,0));
        }
        // 13 total

        // wiggle anim
        this.sprites =  new ArrayList<Sprite>();
        this.sprites.add(null); // draw nothing for 13 frames

//        text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball_wiggleSheet1.png"));
        text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball_wiggleSheet1_color.png"));
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); // middle
        this.sprites.add(new Sprite(text, 12*1, 0, 12, 12)); // left tilt
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); // middle
        this.sprites.add(new Sprite(text, 12*2, 0, 12, 12)); // right tilt
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); // middle
        this.sprites.add(new Sprite(text, 12*1, 0, 12, 12)); // left tilt
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); // middle
        this.sprites.add(new Sprite(text, 12*2, 0, 12, 12)); // right tilt
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); // middle
        this.sprites.add(new Sprite(text, 12*1, 0, 12, 12)); // left tilt
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); // middle
        this.sprites.add(new Sprite(text, 12*2, 0, 12, 12)); // right tilt // done at this point // 13 total events

        this.repeats = new ArrayList<Integer>();

        this.repeats.add(13-1); // 13 frames before pokeball appears on ground

        this.repeats.add(44-1); // first middle sprite has 44 frames
        for (int i = 0; i < 3; i++) { // left-middle-right tilt all have 4 frames
            this.repeats.add(4-1);
        }
        this.repeats.add(44-1); // first middle sprite has 44 frames
        for (int i = 0; i < 3; i++) { // left-middle-right tilt all have 4 frames
            this.repeats.add(4-1);
        }
        this.repeats.add(44-1); // first middle sprite has 44 frames
        for (int i = 0; i < 2; i++) { // left-middle tilt both have 4 frames
            this.repeats.add(4-1);
        }
        this.repeats.add(6-1); // final right tilt has 6 frames
        // 13 total

        // sounds to play
        this.sounds = new ArrayList<String>();
        // this.sounds.add(null); // wait 13 no sound
        this.sounds.add("pokeball_wiggle1"); // wiggle as ball appears (doing as white appears, so it's uniform)
        this.sounds.add(null);
        this.sounds.add("pokeball_wiggle1");
        for (int i = 0; i < 3; i++) {
            this.sounds.add(null);
        }
        this.sounds.add("pokeball_wiggle1");
        for (int i = 0; i < 6; i++) {
            this.sounds.add(null);
        }
        // this.sounds.add("pokeball_wiggle1");
//        for (int i = 0; i < 3; i++) {
//            this.sounds.add(null);
//        }

//        text = new Texture(Gdx.files.internal("throw_pokeball_anim/helper18.png"));
//        this.helperSprite = new Sprite(text, 0, 0, 160, 144);
        // this.helperSprite.setPosition(16*10,16*9); // post scaling change
        // this.helperSprite.setScale(3); // post scaling change

    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void firstStep(Game game) {
        for (Tile tile : game.map.tiles.values()) {
            if (tile.name.contains("cave1_regipedistal1")) {
                this.pedistalTile = tile;
                break;
            }
        }
    }

    @Override
    public void step(Game game) {
        // set opp pokemon sprite alpha
//        game.battle.oppPokemon.sprite.setAlpha(0);
        DrawBattle.shouldDrawOppPokemon = false;

        // Set sprite position
        // if done with anim, do nextAction
        if (positions.isEmpty() || sprites.isEmpty()) {
            // If the player caught an overworld pokemon,
            // remove it from the overworld
            if (game.battle.oppPokemon.standingAction != null) {
                game.actionStack.remove(game.battle.oppPokemon.standingAction);
                game.battle.oppPokemon.standingAction = null;
                // ... and remove it from game.map.pokemon.
                // If pokemon was burrowed, remove it from it's tile's items
                // (burrowed mon is contained in items)
                if (game.battle.oppPokemon.isTrapping) {
                    Tile tile = game.map.tiles.get(game.battle.oppPokemon.position);
                    tile.items.remove(game.battle.oppPokemon.name.toLowerCase());
                }
                // else it's in the overworld so remove
                else {
                    game.map.pokemon.remove(game.battle.oppPokemon.position);  // TODO: may not work if pkmn is frozen
                }
            }
            // If pokemon was burrowed, remove it from it's tile's items
            // (burrowed mon is contained in items)
            if (game.battle.oppPokemon.isTrapping) {
                Tile tile = game.map.tiles.get(game.battle.oppPokemon.position);
                tile.items.remove(game.battle.oppPokemon.name.toLowerCase());
            }
            // Since pkmn was caught, add to players pokemon
            Action newAction = new PokemonCaughtEvents(game,
                               new SplitAction(new BattleFadeOut(game, null),
                               new BattleFadeOutMusic(game,
//                                new SetField(game.musicController, "resumeOverworldMusic", true,  // TODO: remove
                               null)));
            if (!game.battle.oppPokemon.name.contains("regi")) {
                newAction.append(new SetField(game.musicController, "resumeOverworldMusic", true, null));
            }
            game.battle.oppPokemon.previousOwner = game.player;
            game.battle.oppPokemon.aggroPlayer = false;
            if (game.player.pokemon.size() < 6) {
                game.player.pokemon.add(game.battle.oppPokemon);
            }
            else {
                // If player inventory is full, then send the pokemon to player's spawn location
                game.battle.oppPokemon.position = game.player.spawnLoc.cpy();
                game.battle.oppPokemon.mapTiles = game.map.overworldTiles;
                if (game.player.spawnIndex != -1) {
                    game.battle.oppPokemon.mapTiles = game.map.interiorTiles.get(game.player.spawnIndex);
                }
                newAction.append(// new SetField(game, "playerCanMove", false,  // TODO: remove these
                                 new DisplayText(game, "Your party is full! "+game.battle.oppPokemon.name.toUpperCase()+" was sent to the last known safe place.", null, null,
                                 // new SetField(game, "playerCanMove", true,
                                 null));
                // TODO: there will probably be bugs if pokemon overlaps in game.map.pokemon with another pokemon.
                game.insertAction(game.battle.oppPokemon.new Standing());
            }
            if (SpecialMewtwo1.class.isInstance(game.battle.oppPokemon)) {
                // Remove mewtwo from it's tile
                Tile tile = ((SpecialMewtwo1)game.battle.oppPokemon).tile;
                tile.nameUpper = "";
                tile.overSprite = null;
                tile.attrs.put("solid", false);
            }
            else if (game.battle.oppPokemon.onTile != null && game.battle.oppPokemon.onTile.nameUpper.contains("revived_")) {
                game.battle.oppPokemon.onTile.nameUpper = "";
            }
            else {
                // Remove this pokemon from the map
                game.map.currRoute.pokemon.remove(game.battle.oppPokemon);
                game.map.currRoute.genPokemon(game.battle.oppPokemon.baseStats.get("catchRate"));
            }
            if (game.player.pokemon.size() >= 6 && !game.player.displayedMaxPartyText) {
                game.player.displayedMaxPartyText = true;
                newAction.append(//new SetField(game, "playerCanMove", false,
                                 new DisplayText(game, "Your party is full! You will need to DROP some of them in order to catch more.", null, null,
                                 //new SetField(game, "playerCanMove", true,
                                 null));
            }

            // If this was the regi encounter, so remove from the pedistal
            if (game.battle.oppPokemon.name.equals("regigigas")) {
                Tile regiTile = null;
                for (Tile tile : game.map.tiles.values()) {
                    if (tile.name.contains("cave1_regi3")) {
                        regiTile = tile;
                        break;
                    }
                }
                System.out.println("hererererere");
                System.out.println(game.battle.oppPokemon.position);
                game.map.tiles.remove(regiTile.position);
                newAction.append(new SetField(game, "playerCanMove", true, null));
//                game.map.tiles.remove(game.battle.oppPokemon.position);
//                game.map.tiles.remove(game.battle.oppPokemon.position.cpy().add(16, 0));
            }
            else if (this.pedistalTile != null &&
                     game.battle.oppPokemon.name.contains("regi")) {
                String text = "";
                if (this.pedistalTile.nameUpper.equals("REGICE")) {
                    text = "Cold ... very cold ... too cold ... but ... even with heat ... it lasts forever ... ";
                }
                else if (this.pedistalTile.nameUpper.equals("REGIROCK")) {
                    text = "Simple stones ... such a creation ... each one so unique ... but only stones ... even in the head ... ";
                }
                else if (this.pedistalTile.nameUpper.equals("REGISTEEL")) {
                    text = "Metals ... from underground ... so tough ... so flexible ... yet it' so old ... it has an unearthly presence ... ";
                }
                else if (this.pedistalTile.nameUpper.equals("REGIDRAGO")) {
                    text = "Such a sad sight ... my child ... its my fault ... incomplete ... just a dragon' head ... but you are still ... so strong ... ";
                }
                else if (this.pedistalTile.nameUpper.equals("REGIELEKI")) {
                    text = "Such a sad sight ... my child ... your electricity ... restrained ... but ... you are still ... so strong ... ";
                }
                this.pedistalTile.nameUpper = "";
                this.pedistalTile.overSprite = null;
                this.pedistalTile.items().remove(game.battle.oppPokemon.name.toUpperCase());
                
                // TODO: remove
//                newAction.append(new SetField(game, "playerCanMove", false,  // TODO: refactor this, i think it's in battlefadeout
//                                 null));

                if (this.pedistalTile.items().isEmpty()) {
                    Tile regiTile = null;
                    for (Tile tile : game.map.tiles.values()) {
                        if (tile.name.contains("cave1_regi2")) {
                            regiTile = tile;
                            break;
                        }
                    }
                    Texture texture = TextureCache.get(Gdx.files.internal("tiles/cave1/cave1_floor2.png"));
                    this.pedistalTile.sprite = new Sprite(texture, 0, 0, 16, 16);
                    this.pedistalTile.sprite.setPosition(this.pedistalTile.position.x, this.pedistalTile.position.y);
                    this.pedistalTile.name = "cave1_floor2";
                    this.pedistalTile.attrs.put("solid", false);
                    text += "My work is ... complete...";
                    
//                    // TODO: remove
//                    texture = TextureCache.get(Gdx.files.internal("tiles/cave1/regi3.png"));
//                    regiTile.sprite = new Sprite(texture, 0, 0, 32, 32);
//                    regiTile.sprite.setPosition(this.pedistalTile.position.x-8, this.pedistalTile.position.y-9);
                    
                    newAction.append(new DisplayText(game, text, null, null, true, 
                                     new SetField(regiTile, "name", "cave1_regi3", null)));
                }
                else {
                    Texture texture = TextureCache.get(Gdx.files.internal("tiles/cave1/cave1_regipedistal1.png"));
                    this.pedistalTile.overSprite = new Sprite(texture, 0, 0, 16, 16);
                    // TODO: remove once DrawMap does game.mapBatch.draw(overSprite) instead
                    this.pedistalTile.overSprite.setPosition(this.pedistalTile.position.x, this.pedistalTile.position.y);
                    text += "I desire more ...";
                    newAction.append(new DisplayText(game, text, null, null, true, 
                                     new RegigigasOutroAnim(null)));
                }
                newAction.append(new SetField(game.musicController, "resumeOverworldMusic", true, null));
//                newAction.append(new SetField(game, "playerCanMove", true, null));  // TODO: remove
            }
            newAction.append(new WaitFrames(game, 6,
                             new SetField(game, "playerCanMove", true, null)));
            game.insertAction(newAction);
            newAction.step(game);  // need to draw the pokeball
            game.actionStack.remove(this);
            return;
        }

        // get next sound, play it
        this.sound = this.sounds.get(0);
        if (this.sound != null) {
            game.insertAction(new PlaySound(this.sound, null));
            this.sounds.set(0, null); // don't play same sound over again
        }

        // get next frame
        this.sprite = sprites.get(0);

        // remove later
        // this.helperSprite.draw(game.floatingBatch); // debug

        // draw current sprite
        if (this.sprite != null) {
            // this.sprite.setScale(3); // post scaling change
            this.sprite.setPosition(position.x, position.y);
            this.sprite.draw(game.uiBatch);
        }

//        if (this.repeats.size() == 18) { // debug
//            return;
//        }

        // repeat sprite/pos for current object for 'frames[0]' number of frames.
        if (this.repeats.get(0) > 0) {
            this.repeats.set(0, this.repeats.get(0) - 1);
        }
        else {
            // since position is relative, only update once each time period
            this.position = this.position.add(positions.get(0));
            positions.remove(0);
            sprites.remove(0);
            repeats.remove(0);
            sounds.remove(0);
        }
    }
}

class ChanceToRun extends Action {
    public int layer = 120;
    public ChanceToRun(Game game, Action nextAction) {
        this.nextAction = nextAction;

        //  - if run chance succeeds, then print 'ran away' text and exit battle actions
        //  - if failed, go to menuAction (nextAction)

        // experiment - add angry/eating print to AS?
        // automatically adds 'print angry/eating to AS'
        /*
        game.insertAction(new PrintAngryEating(game, this)); // usually menu draw action
        game.actionStack.remove(this);
        */ // didn't do b/c confusing. can use this in place of series' tho

    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        // TODO - uses up one frame here when it shouldn't.
         // call nextAction step? but layer will be wrong
         // ignoring for now

        // can't make this a function in drawMenu, because
         // i need this calculation to happen after angry counters have been modified
        int x = game.battle.oppPokemon.currentStats.get("speed") % 256; // current speed or max speed?
        x = x*2;

        System.out.println("Chance to run, x: " + String.valueOf(x));

        if (x > 255) {
            // pokemon runs
            String textString = "Wild " + game.battle.oppPokemon.name.toUpperCase() + " ran!";
            game.insertAction(new DisplayText(game, textString, null, null,
                                             new SplitAction(new OppPokemonFlee(game,
                                                             new SplitAction(new BattleFadeOut(game,
                                                                             null), // new playerStanding(game)),
                                                             new BattleFadeOutMusic(game, null))),
                                             new WaitFrames(game, 8, new PlaySound("run1", null)) // 8 frames seems right
                                             )));
            // demo code - reset adrenaline
            game.player.adrenaline = 0;
            //
            game.actionStack.remove(this);
            return;
        }
        if (game.battle.oppPokemon.angry > 0) {
            x = x*2;
            if (x > 255) { // capped at 255
                x = 255;
            }
        }
        else if (game.battle.oppPokemon.eating > 0) {
            x = x/4;
        }

        int r = game.map.rand.nextInt(255+1); //+1 to include upper bound
        if (r < x) {
            // pokemon runs
            String textString = "Wild " + game.battle.oppPokemon.name.toUpperCase() + " ran!";
            game.insertAction(new DisplayText(game, textString, null, null,
                                             new SplitAction(new OppPokemonFlee(game,
                                                             new SplitAction(new BattleFadeOut(game,
                                                                             null), // new playerStanding(game)),
                                                             new BattleFadeOutMusic(game, null))),
                                             new WaitFrames(game, 8,
                                             new PlaySound("run1",
                                             null
                                             )))));
            // demo code - reset adrenaline
            game.player.adrenaline = 0;
            //
            // game.insertAction(new PlaySound("click1", new PlaySound("run1", null)));
            game.actionStack.remove(this);
            return;
        }

        // pokemon doesn't run
        // insert nextAction to actionstack
        game.insertAction(this.nextAction); // usually menu draw action
        game.actionStack.remove(this);
    }
}

// instance of this assigned to DrawBattle.drawEnemyHealthAction
class DepleteEnemyHealth extends Action {
    public int layer = 129;
    boolean firstStep;

    int timer;

    int removeNumber;
    // TODO: remove if unused
    int targetSize; // reduce enemy health bar to this number
    int damage = 0;
    public DepleteEnemyHealth(Game game, Action nextAction) {
        this(game, 0, nextAction);
    }
    public DepleteEnemyHealth(Game game, int damage, Action nextAction) {
        this.nextAction = nextAction;

        this.firstStep = true;
        this.timer = 3;
        this.removeNumber = 2;
        this.damage = damage;
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        if (this.firstStep) {
            // Apply damage if there is any (otherwise assume it's already applied)
            if (this.damage > 0) {
                int currHealth = game.battle.oppPokemon.currentStats.get("hp");
                int finalHealth = currHealth - this.damage > 0 ? currHealth - this.damage : 0;
                game.battle.oppPokemon.currentStats.put("hp", finalHealth);
            }
            // TODO: remove if unused
            this.targetSize = (int)Math.ceil( ((float)game.battle.oppPokemon.currentStats.get("hp")*48f) / (float)game.battle.oppPokemon.maxStats.get("hp"));
            this.firstStep = false;
        }

        this.timer++;
        if (this.timer < 4) {
            return;
        }
        this.timer = 0;

//        int size = 0;
//        for (int i = 0; i < this.removeNumber; i++) {
//            size = game.battle.drawAction.drawEnemyHealthAction.healthBar.size();
//            if (size <= targetSize) {
//                break;
//            }
//            game.battle.drawAction.drawEnemyHealthAction.healthBar.remove(size-1);
//        }
        game.battle.drawAction.drawEnemyHealthAction.currHealth -= this.removeNumber;

        if (this.removeNumber == 2) {
            this.removeNumber = 1;
        }
        else {
            this.removeNumber = 2;
        }

//        if (size <= targetSize) {
        if (game.battle.drawAction.drawEnemyHealthAction.currHealth <= this.targetSize) {
            game.battle.drawAction.drawEnemyHealthAction.currHealth = this.targetSize;
            game.actionStack.remove(this);
            // If enemy health is 0, do EnemyFaint
            if (game.battle.oppPokemon.currentStats.get("hp") <= 0) {
                int numParticipated = 0;
                for (Pokemon pokemon : game.player.pokemon) {
                    if (pokemon.participatedInBattle && pokemon.currentStats.get("hp") > 0) {
                        numParticipated++;
                    }
                }
                if (numParticipated <= 0) {
                    numParticipated = 1;
                }
                // TODO: remove
                System.out.println(numParticipated);
                Action nextAction = new EnemyFaint(game,
                                    new RemoveDisplayText(  // TODO: refactor to stop using this
                                    new DisplayText.Clear(game,
                                    new WaitFrames(game, 3,
                                    new DisplayText(game, "Enemy "+game.battle.oppPokemon.name.toUpperCase()+" fainted!",
                                                    null, null,
                                    null)))));
                for (Pokemon pokemon : game.player.pokemon) {
                    if (pokemon.participatedInBattle && pokemon.currentStats.get("hp") > 0) {
                        int exp = game.battle.calcFaintExp(numParticipated);
                        pokemon.exp += exp;
                        nextAction.append(new DisplayText(game, pokemon.name.toUpperCase()+" gained "+String.valueOf(exp)+" EXP. Points!",
                                          null, true, true,
                                          new GainExpAnimation(pokemon,
                                          null)));
                    }
                }
                // For each Pokemon that gained a level, check if it should evolve
                // and play the evolve animation if it does.
                for (Pokemon pokemon : game.player.pokemon) {
                    // TODO: multiple evos in a row likely is broken
                    nextAction.append(new CheckEvo(pokemon, null));
                }
                // TODO: remove
//                int exp = game.battle.calcFaintExp();
//                game.player.currPokemon.exp += exp;

                // TODO: now for each Pokemon, check if evolved or not
                
                nextAction.append(new SplitAction(
                                      new BattleFadeOut(game, null),
                                  new SplitAction(
                                      new BattleFadeOutMusic(game, null),
                                  new WaitFrames(game, 30,
                                  new SetField(game.musicController, "resumeOverworldMusic", true,
                                  null)))));
                // If player made mewtwo faint, display message that it may return
                // Also don't draw upper sprite, make unsolid.
                if (SpecialMewtwo1.class.isInstance(game.battle.oppPokemon)) {
//                if (game.battle.oppPokemon instanceof SpecialMewtwo1) {  // TODO: enable if verified working
                    SpecialMewtwo1 mewtwo = (SpecialMewtwo1)game.battle.oppPokemon;
                    mewtwo.tile.attrs.put("solid", false);
                    mewtwo.tile.nameUpper = "mewtwo_overworld_hidden";
                    mewtwo.tile.overSprite = null;
                    nextAction.append(//new SetField(game, "playerCanMove", false,
                                      new DisplayText(game, "MEWTWO fled... it may return when it' had time to recover.",
                                                      null, null,
                                      //new SetField(game, "playerCanMove", true,
                                      null));
                }
                // If the player fainted an overworld pokemon,
                // then remove it's standing Action :(
                if (game.battle.oppPokemon.standingAction != null) {
                    // It still lives on in memory... until
                    // it gets garbage collected :O
                    game.actionStack.remove(game.battle.oppPokemon.standingAction);
                    game.battle.oppPokemon.standingAction = null;
                    // If pokemon was burrowed, remove it from it's tile's items
                    // (burrowed mon is contained in items)
                    if (game.battle.oppPokemon.isTrapping) {
                        Tile tile = game.map.tiles.get(game.battle.oppPokemon.position);
                        tile.items.remove(game.battle.oppPokemon.name.toLowerCase());
                    }
                    // else it's in the overworld so remove
                    else {
                        game.map.pokemon.remove(game.battle.oppPokemon.position);
                    }
                }
                nextAction.append(new SetField(game, "playerCanMove", true, null));
                game.insertAction(nextAction);
            }
            // else, insert nextAction
            else {
                game.insertAction(this.nextAction);
            }
        }

    }
}

/**
 * Restore health animation.
 *
 * Used in battle by Recover, Rest, etc. Used outside of battle by potion, moomoo milk, etc.
 */
class RestoreHealth extends Action {
    Pokemon pokemon;
    int damage = 0;  // amount hp gained/lost
    int finalHealth;
    int timer = 3;
    int removeNumber = 1;  // 2;
    int sign = 1;

    public RestoreHealth(Pokemon pokemon, int damage, Action nextAction) {
        this.pokemon = pokemon;
        this.damage = damage;
        this.nextAction = nextAction;
    }

    public String getCamera() {return "gui";}

    @Override
    public void firstStep(Game game) {
        int currHealth = this.pokemon.currentStats.get("hp");
        this.finalHealth = currHealth - this.damage;
        if (this.finalHealth > this.pokemon.maxStats.get("hp")) {
            this.finalHealth = this.pokemon.maxStats.get("hp");
        }
        else if (this.finalHealth < 0) {
            this.finalHealth = 0;
        }
        if (this.finalHealth > this.pokemon.currentStats.get("hp")) {
            this.sign = -1;
        }
        this.removeNumber *= this.sign;
    }

    @Override
    public void step(Game game) {
        this.timer++;
        if (this.timer < 2) {
            return;
        }
        this.timer = 0;
        int removeHealth = (int)Math.ceil((((float)this.removeNumber)/48f) * (float)this.pokemon.maxStats.get("hp"));
        if (removeHealth <= 0) {
            removeHealth = 1*this.sign;
        }
        this.pokemon.currentStats.put("hp", this.pokemon.currentStats.get("hp") - removeHealth);

        if (game.battle.drawAction != null) {
            if (this.pokemon == game.battle.oppPokemon) {
                game.battle.drawAction.drawEnemyHealthAction.currHealth = (int)Math.ceil( (this.pokemon.currentStats.get("hp")*48) / this.pokemon.maxStats.get("hp"));
            }
            else {
                game.battle.drawAction.drawFriendlyHealthAction.currHealth = (int)Math.ceil( (this.pokemon.currentStats.get("hp")*48) / this.pokemon.maxStats.get("hp"));
            }
        }

        // Alternate between removing 2 and removing 1
//        if (this.removeNumber % 2 == 0) {
//            this.removeNumber = 1;
//        }
//        else {
//            this.removeNumber = 2;
//        }
        this.removeNumber = 1;
        this.removeNumber *= this.sign;

        // If health is 0, this pokemon should faint
        if ((sign == 1 && this.pokemon.currentStats.get("hp") <= this.finalHealth) ||
            (sign == -1 && this.pokemon.currentStats.get("hp") >= this.finalHealth)) {
            this.pokemon.currentStats.put("hp", this.finalHealth);
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
        }
    }
}

// instance of this assigned to DrawBattle.drawEnemyHealthAction
class DepleteFriendlyHealth extends Action {
    public int layer = 129;
    Pokemon pokemon;
    boolean firstStep;
    int timer;
    int removeNumber;
    int targetSize; // reduce enemy health bar to this number
    int damage = 0;

    public DepleteFriendlyHealth(Pokemon friendlyPokemon, Action nextAction) {
        this(friendlyPokemon, 0, nextAction);
    }

    public DepleteFriendlyHealth(Pokemon friendlyPokemon, int damage, Action nextAction) {
        this.pokemon = friendlyPokemon;
        this.nextAction = nextAction;
        this.firstStep = true;
        this.timer = 3;
        this.removeNumber = 2;
        this.damage = damage;
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        if (this.firstStep) {
            // Apply damage if there is any (otherwise assume it's already applied)
            if (this.damage > 0) {
                int currHealth = game.player.currPokemon.currentStats.get("hp");
                int finalHealth = currHealth - this.damage > 0 ? currHealth - this.damage : 0;
                game.player.currPokemon.currentStats.put("hp", finalHealth);
            }
            // TODO: remove if unused
            this.targetSize = (int)Math.ceil( ((float)this.pokemon.currentStats.get("hp")*48f) / (float)this.pokemon.maxStats.get("hp"));
            this.firstStep = false;
        }
        this.timer++;
        if (this.timer < 4) {
            return;
        }
        this.timer = 0;

        // TODO: remove if unused
//        int size = 0;
//        for (int i = 0; i < this.removeNumber; i++) {
//            size = game.battle.drawAction.drawFriendlyHealthAction.healthBar.size();
//            if (size <= targetSize) {
//                break;
//            }
//            game.battle.drawAction.drawFriendlyHealthAction.healthBar.remove(size-1);
//        }
        game.battle.drawAction.drawFriendlyHealthAction.currHealth -= this.removeNumber;

        // alternate between removing 2 and removing 1
        if (this.removeNumber == 2) {
            this.removeNumber = 1;
        }
        else {
            this.removeNumber = 2;
        }

        // If health is 0, this pokemon should faint
//        if (size <= targetSize) {
        if (game.battle.drawAction.drawFriendlyHealthAction.currHealth <= this.targetSize) {
            game.battle.drawAction.drawFriendlyHealthAction.currHealth = this.targetSize;
            game.actionStack.remove(this);
            if (this.pokemon.currentStats.get("hp") <= 0) {
                game.insertAction(new FriendlyFaint(game,
                                  new RemoveDisplayText(
                                  new WaitFrames(game, 3,
                                  new DisplayText(game, this.pokemon.name.toUpperCase()+" fainted!",
                                                  null, null,
                                  // TODO: decide whether to switch pkmn, send out player, or end game
                                  new AfterFriendlyFaint())))));
            }
            // else, insert nextAction
            else {
                game.insertAction(this.nextAction);
            }
        }
    }
}

/**
 * Draw Pokemon attacks menu.
 */
class DrawAttacksMenu extends Action {
    public static int curr = 0;
    Sprite arrow;
    Sprite arrowWhite;
    Sprite textBox;
    public int layer = 108;
    Map<Integer, Vector2> coords = new HashMap<Integer, Vector2>();
    Vector2 newPos;
    Sprite helperSprite;
    ArrayList<ArrayList<Sprite>> spritesToDraw = new ArrayList<ArrayList<Sprite>>();
    int cursorDelay; // this is just extra detail. cursor has 2 frame delay before showing in R/B
    String attackLearning = null;  // if this has a value, this is just being used by player to select attack to forget.
    Vector2 offset = new Vector2(0, 0);
    Pokemon pokemon;  // used if a Pokemon is learning a move

    public DrawAttacksMenu(Action nextAction) {
        this(null, Game.staticGame.player.currPokemon, nextAction);
    }

    public DrawAttacksMenu(String attackLearning, Pokemon pokemon, Action nextAction) {
        this.attackLearning = attackLearning;
        this.pokemon = pokemon;
        if (this.attackLearning != null) {
            this.offset.add(0, 48);
        }
        this.nextAction = nextAction;
        this.cursorDelay = 0;
        Texture text = new Texture(Gdx.files.internal("battle/arrow_right1.png"));
        this.arrow = new Sprite(text, 0, 0, 5, 7);

        // text box bg
        text = new Texture(Gdx.files.internal("attack_menu/attack_screen1.png"));
        this.textBox = new Sprite(text, 0,0, 16*10, 16*9);
        this.textBox.setPosition(this.offset.x, this.offset.y);

        this.coords.put(0, new Vector2(41, 32));
        this.coords.put(1, new Vector2(41, 24));
        this.coords.put(2, new Vector2(41, 16));
        this.coords.put(3, new Vector2(41, 8));

        // TODO: remove
        // this.newPos =  new Vector2(32, 79); // post scaling change
//        this.newPos =  new Vector2(41, 32);
//        this.arrow.setPosition(newPos.x, newPos.y);

        // helper sprite
//        text = new Texture(Gdx.files.internal("attack_menu/helper2.png"));
//        this.helperSprite = new Sprite(text, 0,0, 16*10, 16*9);
    }

    @Override
    public void firstStep(Game game) {
        this.newPos = this.coords.get(DrawAttacksMenu.curr);
        this.arrow.setPosition(this.newPos.x, this.newPos.y);

        // Convert pokemon attacks to sprites
        for (String attack : this.pokemon.attacks) {
            if (attack == null) {
                attack = "-";
            }
            char[] textArray = attack.toUpperCase().toCharArray(); // iterate elements
            Sprite currSprite;
            int i = 0;
            int j = 0;
            ArrayList<Sprite> word = new ArrayList<Sprite>();
            for (char letter : textArray) {
                // offsetNext += spriteWidth*3+2 // how to do this?
                Sprite letterSprite = Game.staticGame.textDict.get((char)letter);
                if (letterSprite == null) {
                    letterSprite = Game.staticGame.textDict.get(null);
                }
                currSprite = new Sprite(letterSprite); // copy sprite from char-to-Sprite dictionary

                // currSprite.setPosition(10*3+8*i*3 +2, 26*3-16*j*3 +2); // offset x=8, y=25, spacing x=8, y=8(?)
                currSprite.setPosition(10+8*i +2-4, this.offset.y + 26-16*j +2-4); // post scaling change
                // currSprite.setScale(3); // post scaling change

                word.add(currSprite);
                // go down a line if needed
                 // TODO - do this for words, not chars. split on space, array
                if (i >= 17) {
                    i = 0; j++;
                }
                else {
                    i++;
                }
            }
            spritesToDraw.add(word);
        }
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        // check user input
         //'tl' = top left, etc.
         // modify position by modifying curr to tl, tr, bl or br
        if (InputProcessor.upJustPressed) {
            if (DrawAttacksMenu.curr != 0) {
                DrawAttacksMenu.curr -= 1;
                newPos = coords.get(DrawAttacksMenu.curr);
            }
        }
        else if (InputProcessor.downJustPressed) {
            if (DrawAttacksMenu.curr+1 < this.pokemon.attacks.length &&
                this.pokemon.attacks[DrawAttacksMenu.curr+1] != null) {
                DrawAttacksMenu.curr += 1;
                newPos = coords.get(DrawAttacksMenu.curr);
            }
        }

        // if press A, do attack
        if (InputProcessor.aJustPressed) {
            if (this.attackLearning != null) {
                game.actionStack.remove(this);
                
                // No confirmation
//                game.insertAction(new DisplayText.Clear(game,
//                                  new WaitFrames(game, 3,
//                                  new DisplayText(game, "And one... two... three... POOF!",
//                                                  null, null,
//                                  new DisplayText(game, this.pokemon.name.toUpperCase() + " forgot " + this.pokemon.attacks[DrawAttacksMenu.curr].toUpperCase()+" and...",
//                                                  null, null,
//                                  new DisplayText(game, this.pokemon.name.toUpperCase() + " learned " + this.attackLearning.toUpperCase()+"!",
//                                                  "fanfare1.ogg", true, true,
//                                  this.nextAction))))));
                // Confirmation
                game.insertAction(new DisplayText.Clear(game,
                                  new WaitFrames(game, 3,
                                  new DisplayText(game, "Replace "+this.pokemon.attacks[DrawAttacksMenu.curr].toUpperCase()+"?",
//                                                  null, true, false,
                                                  null, true, true,  // <-- this was a requested fix
                                  new DrawYesNoMenu(null,
                                      new DisplayText.Clear(game,
                                      new WaitFrames(game, 3,
                                      new DisplayText(game, "And one... two... three... POOF!",
                                                      null, null,
                                      new DisplayText(game, this.pokemon.name.toUpperCase() + " forgot " + this.pokemon.attacks[DrawAttacksMenu.curr].toUpperCase()+" and...",
                                                      null, null,
                                      new DisplayText(game, this.pokemon.name.toUpperCase() + " learned " + this.attackLearning.toUpperCase()+"!",
                                                      "fanfare1.ogg", true, true,
                                      new SetArrayAtIndex(this.pokemon.attacks, DrawAttacksMenu.curr, this.attackLearning,
                                      this.nextAction)))))),
                                  new DisplayText.Clear(game,
                                  new WaitFrames(game, 3,                                      
                                  new DisplayText(game, "Which move should be forgotten?",
//                                                  null, true, false,
                                                  null, true, true,
                                  this))))))));
//                this.pokemon.attacks[DrawAttacksMenu.curr] = this.attackLearning;
//                if (game.type == Game.Type.CLIENT) {
//                    game.client.sendTCP(new Network.LearnMove(game.player.network.id, 0, DrawAttacksMenu.curr, this.attackLearning));
//                }
                return;
            }
            // Warning text if attack is disabled.
            if (this.pokemon.disabledIndex == DrawAttacksMenu.curr) {
                game.actionStack.remove(this);
                game.insertAction(new DisplayText.Clear(game,
                                  new WaitFrames(game, 3,
                                  new DisplayText(game, this.pokemon.attacks[DrawAttacksMenu.curr].toUpperCase()+" is disabled!",
                                                  null, null,
                                  new WaitFrames(game, 3,
                                  this)))));
                return;
            }
            
            // Reset counter keeping track of number flees done by player this battle (used in run away mechanic).
            game.player.numFlees = 0;

            // explanation of speed move priority: http:// bulbapedia.bulbagarden.net/wiki/Stats#Speed
             // pkmn with higher speed moves first

            // Play select sound
            Action attack = new SplitAction(new PlaySound("click1", null), null);
            if (game.type == Game.Type.CLIENT) {
                // send move to server, wait response
                String attackName = this.pokemon.attacks[DrawAttacksMenu.curr];
                attack.append(new Battle.WaitTurnData(game, null));
                game.client.sendTCP(new com.pkmngen.game.Network.DoBattleAction(game.player.network.id, Battle.DoTurn.Type.ATTACK, attackName));
            }
            // We don't know the result of attack miss/crit etc checks yet whenever the game
            // is a client, so DoTurn will act accordingly once we get that from the server.
            attack.append(new Battle.DoTurn(game, Battle.DoTurn.Type.ATTACK, this.nextAction));
            game.actionStack.remove(this);
            game.insertAction(attack);
            return;
        }
        // Player presses b, ie wants to go back
        else if (InputProcessor.bJustPressed) {
            if (this.attackLearning != null) {
                game.actionStack.remove(this);
                // No confirmation version
//                game.insertAction(new DisplayText.Clear(game,
//                                  new WaitFrames(game, 3,
//                                  new DisplayText(game, this.pokemon.name.toUpperCase()+" did not learn " + this.attackLearning.toUpperCase()+".",
//                                                  null, null,
//                                  this.nextAction))));
                // With confirmation version
                game.insertAction(new DisplayText.Clear(game,
                                  new WaitFrames(game, 3,
                                  new DisplayText(game, "Give up on learning "+this.attackLearning.toUpperCase()+"?",
//                                                  null, true, false,
                                                  null, true, true,  // <-- this was a requested fix
                                  new DrawYesNoMenu(null,
                                      new DisplayText.Clear(game,
                                      new WaitFrames(game, 3,
                                      new DisplayText(game, this.pokemon.name.toUpperCase()+" did not learn " + this.attackLearning.toUpperCase()+".",
                                                      null, null,
                                      this.nextAction))),
                                  new DisplayText.Clear(game,
                                  new WaitFrames(game, 3,
                                  new DisplayText(game, "Which move should be forgotten?",
//                                                  null, true, false,
                                                  null, true, true,
                                  this))))))));
                return;
            }
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
        }
        // Draw text box
        this.textBox.draw(game.uiBatch);

        // debug
//        helperSprite.draw(game.floatingBatch);

        // Draw the attack strings
        int j = 0;
        for (ArrayList<Sprite> word : this.spritesToDraw) {
            for (Sprite sprite : word) {
                // Convert string to text
                game.uiBatch.draw(sprite, sprite.getX() + 40, sprite.getY() - j*8 + 8);
            }
            j+=1;
        }
        // Draw attack type
        // TODO: cache this
        char[] textArray = game.battle.attacks.get(this.pokemon.attacks[DrawAttacksMenu.curr]).type.toUpperCase().toCharArray();
        Sprite letterSprite;
        for (int m=0; m < textArray.length; m++) {
            letterSprite = game.textDict.get(textArray[m]);
            game.uiBatch.draw(letterSprite, 16 +8*m, this.offset.y+56);
            game.uiBatch.draw(letterSprite, this.offset.x+16 +8*m, this.offset.y+56);
        }
        // Draw arrow
        if (this.cursorDelay >= 2) {
            this.arrow.setPosition(newPos.x+this.offset.x, newPos.y+this.offset.y);
            this.arrow.draw(game.uiBatch);
        }
        else {
            this.cursorDelay+=1;
        }
    }
}

// draw battle elements
 // TODO - don't draw the options frame (ie fight, run etc)
class DrawBattle extends Action {
    public static boolean shouldDrawOppPokemon = true;
    boolean doneNightOverlay = false;
    SpriteProxy bgSprite;
    SpriteProxy bgSprite2;
    DrawFriendlyHealth drawFriendlyHealthAction;
    DrawEnemyHealth drawEnemyHealthAction;

    public static int prevFriendlyAttackIndex = -1;
    public static int prevEnemyAttackIndex = -1;

    Action drawFriendlyPokemonAction;
    public int layer = 130;

    public DrawBattle(Game game) {
        Texture text = new Texture(Gdx.files.internal("battle/battle_bg3.png"));
        this.bgSprite = new SpriteProxy(Color.WHITE, text, 0, 0, 176, 160);
        game.battle.drawAction = this;
        this.bgSprite.setPosition(-8, -8);
        text = new Texture(Gdx.files.internal("battle/intro_frame6.png"));
        this.bgSprite2 = new SpriteProxy(text, 0, 0, 160, 144);
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void firstStep(Game game) {
        DrawBattle.prevFriendlyAttackIndex = -1;
        DrawBattle.prevEnemyAttackIndex = -1;
    }

    @Override
    public void step(Game game) {
        if (!this.doneNightOverlay) {
            if (game.map.timeOfDay.equals("night") && (game.battle.oppPokemon == null || !SpecialMewtwo1.class.isInstance(game.battle.oppPokemon))) {
                game.insertAction(this.new DrawNightOverlay());
                this.doneNightOverlay = true;
            }
        }

        this.bgSprite.draw(game.uiBatch);

        if (DrawBattle.shouldDrawOppPokemon) {
            game.battle.oppPokemon.sprite.draw(game.uiBatch);
        }
        game.player.battleSprite.draw(game.uiBatch);

        for (int i = -1; i < 2; i+= 1) {
            for (int j = -1; j < 2; j+= 1) {
                if (i == 0 && j == 0) {
                    continue;
                }
                game.uiBatch.draw(this.bgSprite2, 160*i, 144*j);
            }
        }
    }
    
    class DrawNightOverlay extends Action {
        public int layer = 0;
        Sprite bgSprite;

        public DrawNightOverlay() {
            Texture text = new Texture(Gdx.files.internal("battle/intro_frame1.png"));
            this.bgSprite = new Sprite(text, 0, 0, 160, 144);
        }

        public String getCamera() {return "gui";}

        public int getLayer(){return this.layer;}

        @Override
        public void step(Game game) {
            if (!game.actionStack.contains(DrawBattle.this) ||
                game.map.timeOfDay.equals("day") ||
                game.map.currRoute.isDungeon) {
                game.actionStack.remove(this);
                return;
            }
            // draw 3x3 grid including outside of screen
            // needed for camera shake effects in ui batch (friendly pokemon damaged, etc)
            for (int i = -1; i < 2; i+= 1) {
                for (int j = -1; j < 2; j+= 1) {
                    game.uiBatch.draw(this.bgSprite, 160*i, 144*j);
                }
            }
        }
    }
}

// TODO: i think this is outdated
// draw menu buttons (fight, run, etc)
class DrawBattleMenu1 extends Action {
    Sprite arrow;

    public int layer = 129;
    Map<String, Vector2> getCoords = new HashMap<String, Vector2>();

    String curr;

    Vector2 newPos;
    public DrawBattleMenu1(Game game, Action nextAction) {
        this.nextAction = nextAction;

        Texture text = new Texture(Gdx.files.internal("battle/arrow_right1.png"));
        this.arrow = new Sprite(text, 0, 0, 5, 7);
        this.arrow.setScale(3);

        this.getCoords.put("tr", new Vector2(368, 79));
        this.getCoords.put("tl", new Vector2(224, 79));
        this.getCoords.put("br", new Vector2(368, 31));
        this.getCoords.put("bl", new Vector2(224, 31));

        this.newPos =  new Vector2(224, 79);
        this.arrow.setPosition(newPos.x, newPos.y);
        this.curr = "tl";
    }
    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        // check user input
         //'tl' = top left, etc.
         // modify position by modifying curr to tl, tr, bl or br
        if (InputProcessor.upPressed) {
            if (curr.equals("bl") || curr.equals("br")) {
                curr = "t"+String.valueOf(curr.charAt(1));
                newPos = getCoords.get(curr);
            }

        }
        else if (InputProcessor.downPressed) {
            if (curr.equals("tl") || curr.equals("tr")) {
                curr = "b"+String.valueOf(curr.charAt(1));
                newPos = getCoords.get(curr);
            }
        }
        else if (InputProcessor.leftPressed) {
            if (curr.equals("tr") || curr.equals("br")) {
                curr = String.valueOf(curr.charAt(0))+"l";
                newPos = getCoords.get(curr);
            }
        }
        else if (InputProcessor.rightPressed) {
            if (curr.equals("tl") || curr.equals("bl")) {
                curr = String.valueOf(curr.charAt(0))+"r";
                newPos = getCoords.get(curr);
            }
        }

        // if button press, do something
        if (Gdx.input.isKeyPressed(Input.Keys.Z)) {
            // user selected 'fight'
             // for now this is 'rock', I am going to draw the safari menu later
            if (curr.equals("tl")) {
                // throw a rock, then resume battle menu
                game.insertAction(new ThrowRock(game, this));
                game.actionStack.remove(this);
            }
            // User selected 'run'
            if (curr.equals("br")) {
                 // also need a 'stop playing music' thing here
                 // also need 'stop drawing battle' here
                game.insertAction(new BattleFadeOut(game,
                                  new WaitFrames(game, 18,
                                  new DisplayText(game, "Got away safely!", null, null,
                                  null))));
                game.insertAction(new BattleFadeOutMusic(game, null));
                game.insertAction(new PlaySound("click1", new PlaySound("run1", null)));
                game.actionStack.remove(this);
                game.actionStack.remove(game.battle.drawAction); // stop drawing the battle
                game.battle.drawAction = null;
                game.battle.oppPokemon.inBattle = false;
            }
        }

        // System.out.println("curr: "+curr);

        // draw arrow
        this.arrow.setPosition(newPos.x, newPos.y);
        this.arrow.draw(game.uiBatch);
    }
}

// draw menu buttons (fight, run, etc)
class DrawBattleMenuNormal extends MenuAction {
    Sprite arrow;
    Sprite textBox;

    public int layer = 109;
    Map<String, Vector2> getCoords = new HashMap<String, Vector2>();
    String curr;
    Vector2 newPos;
    Sprite helperSprite;

    public DrawBattleMenuNormal(Game game, Action nextAction) {
        this.nextAction = nextAction;

        Texture text = new Texture(Gdx.files.internal("battle/arrow_right1.png"));
        this.arrow = new Sprite(text, 0, 0, 5, 7);

        // text box bg
        text = new Texture(Gdx.files.internal("battle/battle_menu1.png"));
        this.textBox = new Sprite(text, 0,0, 16*10, 16*9);

        this.getCoords.put("tr", new Vector2(121, 24));
        this.getCoords.put("tl", new Vector2(73, 24));
        this.getCoords.put("br", new Vector2(121, 8));
        this.getCoords.put("bl", new Vector2(73, 8));

        this.newPos =  new Vector2(73, 24);
        this.arrow.setPosition(newPos.x, newPos.y);
        this.curr = "tl";
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        // doesn't draw any arrow if disabled
        if (this.disabled) {
            return;
        }

        // check user input
         //'tl' = top left, etc.
         // modify position by modifying curr to tl, tr, bl or br
        if (InputProcessor.upPressed) {
            if (this.curr.equals("bl") || this.curr.equals("br")) {
                this.curr = "t"+String.valueOf(this.curr.charAt(1));
                this.newPos = this.getCoords.get(this.curr);
            }

        }
        else if (InputProcessor.downPressed) {
            if (this.curr.equals("tl") || this.curr.equals("tr")) {
                this.curr = "b"+String.valueOf(this.curr.charAt(1));
                this.newPos = this.getCoords.get(this.curr);
            }
        }
        else if (InputProcessor.leftPressed) {
            if (this.curr.equals("tr") || this.curr.equals("br")) {
                this.curr = String.valueOf(this.curr.charAt(0))+"l";
                this.newPos = getCoords.get(this.curr);
            }
        }
        else if (InputProcessor.rightPressed) {
            if (this.curr.equals("tl") || this.curr.equals("bl")) {
                this.curr = String.valueOf(this.curr.charAt(0))+"r";
                this.newPos = getCoords.get(this.curr);
            }
        }

        // if button press, do something
        if (InputProcessor.aJustPressed) { // using isKeyJustPressed rather than isKeyPressed
            // user selected 'fight'
            if (this.curr.equals("tl")) {
                // play select sound
                game.insertAction(new PlaySound("click1", null));

                // remove this action, new menu that selects between pokemon attacks
                game.insertAction(new WaitFrames(game, 4, new DrawAttacksMenu(new WaitFrames(game, 4, this))));

                // attacks stored in String[4] in pkmn
                game.actionStack.remove(this);
            }
            // user selected 'item'
            else if (this.curr.equals("bl")) {
                this.disabled = true;
                // play select sound
                game.insertAction(new PlaySound("click1", null));
                // new menu that selects between pokemon attacks
                game.insertAction(new DrawItemMenu.Intro(this, 1,
                                  new DrawItemMenu(game, this)));
                // remove this action,
                game.actionStack.remove(this);
            }
            // TODO: draw pokemon menu
            // user selected 'throw bait'
            else if (this.curr.equals("tr")) {
                // TODO: remove
//                Action throwBaitAction = new ThrowBait(game, new PrintAngryEating(game, new ChanceToRun(game, this) ) );
//                String textString = game.player.name+" threw some BAIT.";
//                game.insertAction(new DisplayText(game, textString, null, throwBaitAction,
//                        throwBaitAction
//                    )
//                );
                this.disabled = true;
                game.insertAction(new DisplayText.Clear(game,
                                  new WaitFrames(game, 3,
                                  new DrawPokemonMenu.Intro(
                                  new DrawPokemonMenu(game,
                                  this)))));
                game.actionStack.remove(this);
            }

            // User selected 'RUN'
            else if (this.curr.equals("br")) {
//                Action runAction = new Battle.DoTurn(game, Battle.DoTurn.Type.RUN, this);
//                if (game.type == Game.Type.CLIENT) {
//                    runAction = new Battle.WaitTurnData(game, runAction);
//                    game.client.sendTCP(new com.pkmngen.game.Network.DoBattleAction(game.player.network.id, Battle.DoTurn.Type.RUN, ""));
//                }
//                game.actionStack.remove(this);
//                game.insertAction(new PlaySound("click1", null));
//                game.insertAction(runAction);
                Action runAction = new SplitAction(new PlaySound("click1", null), null);
                if (game.battle.oppPokemon.isTrapping) {
                    runAction.append(new DisplayText(game, "Canì escape!", null, null, this));
                    game.actionStack.remove(this);
                    game.insertAction(runAction);
                    return;
                }
                if (game.type == Game.Type.CLIENT) {
                    runAction.append(new Battle.WaitTurnData(game, null));
                    game.client.sendTCP(new com.pkmngen.game.Network.DoBattleAction(game.player.network.id, Battle.DoTurn.Type.RUN, ""));
                }
                runAction.append(new Battle.DoTurn(game, Battle.DoTurn.Type.RUN, this));
                game.actionStack.remove(this);
                game.insertAction(runAction);
            }
        }

        // System.out.println("curr: " + curr);

        // draw text box
        this.textBox.draw(game.uiBatch);

        // debug
//        helperSprite.draw(game.floatingBatch);

        // draw arrow
        this.arrow.setPosition(newPos.x, newPos.y);
        this.arrow.draw(game.uiBatch);
    }

    // TODO: remove if unused
//    static class DoRun extends Action {
//
//        public int getLayer() {return 500;}
//
//        @Override
//        public void step(Game game) {
//            // TODO: fail to run scenario
//            // TODO: do this while I'm here.
//            boolean runSuccessful;
//            if (game.type != Game.Type.CLIENT) {
//                runSuccessful = game.battle.calcIfRunSuccessful(game);
//            }
//            else {
//                // Get run result from server
//                com.pkmngen.game.Network.BattleTurnData turnData = game.battle.network.turnData;
//                game.battle.network.turnData = null;
//                runSuccessful = turnData.runSuccessful;
//            }
//            if (runSuccessful) {
//                game.player.numFlees = 0;
//                game.battle.oppPokemon.inBattle = false;
//                game.insertAction(new WaitFrames(game, 18,
//                                                 new DisplayText(game, "Got away safely!", null, null,
//                                                 new SplitAction(new BattleFadeOut(game,
//                                                                 null),
//                                                 new BattleFadeOutMusic(game,
//                                                 null)))));
//                game.insertAction(new PlaySound("run1",
//                                                 null));
//            }
//            else {
//                game.player.numFlees++;
//                // TODO: needs to go to attack here
//                // TODO: something is off about the text.
//                // TODO: timing is off too.
//                game.insertAction(new WaitFrames(game, 18,
//                                                 new DisplayText(game, "Canì Escape!", null, null,
//                                                 this.nextAction)));
//            }
//            game.actionStack.remove(this);
//        }
//
//        public DoRun(Game game, Action nextAction) {
//            this.nextAction = nextAction;
//        }
//    }

}

// Draw menu buttons (fight, run, etc)
class DrawBattleMenuSafariZone extends Action {
    Sprite arrow;
    Sprite textBox;

    public int layer = 129;
    Map<String, Vector2> getCoords = new HashMap<String, Vector2>();

    String curr;

    Vector2 newPos;
    public DrawBattleMenuSafariZone(Game game, Action nextAction) {
        this.nextAction = nextAction;

        Texture text = new Texture(Gdx.files.internal("battle/arrow_right1.png"));
        this.arrow = new Sprite(text, 0, 0, 5, 7);
        // this.arrow.setScale(3); // post scaling change

        // text box bg
        text = new Texture(Gdx.files.internal("battle/battle_text_safarizone.png"));
        this.textBox = new Sprite(text, 0,0, 16*10, 16*9);
        // this.textBox.setScale(3); // post scaling change
        // this.textBox.setPosition(16*10,16*9); // post scaling change

        this.getCoords.put("tr", new Vector2(105, 24));
        this.getCoords.put("tl", new Vector2(9, 24));
        this.getCoords.put("br", new Vector2(105, 8));
        this.getCoords.put("bl", new Vector2(9, 8));

        // this.newPos =  new Vector2(32, 79); // post scaling change
        this.newPos =  new Vector2(9, 24);
        this.arrow.setPosition(newPos.x, newPos.y);
        this.curr = "tl";
    }
    /**
     * TODO: based on Gen 1 mechanics with some modifications required by an old demo.
     * Need to remove or move it.
     */
    @Deprecated
    Action calcIfCaught(Game game) {
        // using http:// bulbapedia.bulbagarden.net/wiki/Catch_rate#Capture_method_.28Generation_I.29
         // also use http:// www.dragonflycave.com/safarizone.aspx
        // not sure where 'ball used' will be stored. probly some inventory location, like currItem (in inventory)

        int maxRand = 150; // different per-ball
        int randomNum = game.map.rand.nextInt(maxRand+1); //+1 to include upper bound
        int statusValue = 0; // different depending on oppPokemon's status
        boolean breaksFree = false;

        int ball = 15; // 8 if great ball
        // demo code
        int adrenaline = game.player.adrenaline;
        if (adrenaline > 25) {
            adrenaline = 25;
        }
        // ball = ball - adrenaline;
        int modFactor = 100;// 128; - want 5 adr to catch all easy, but not medium or hard.
        int f = (int)Math.floor((game.battle.oppPokemon.currentStats.get("catchRate") * 255 * 4) / (modFactor*ball)); // modify 128 to make game harder
        //

        // int f = (int)Math.floor((game.battle.oppPokemon.maxStats.get("hp") * 255 * 4) / (game.battle.oppPokemon.currentStats.get("hp") * ball));

        // left out calculation here based on status values
         // demo - leave out status value
        // notes - adr seems to take effec too fast. also, pkmn in general are too hard to catch
         // at beginning. shift factor down, and make adr*10
        if (randomNum - statusValue > game.battle.oppPokemon.currentStats.get("catchRate") && false) {
            breaksFree = true;
            System.out.println("(randomNum - statusValue / catchRate): ("+String.valueOf(randomNum - statusValue)+" / "+String.valueOf(game.battle.oppPokemon.currentStats.get("catchRate"))+")");
        }
        else {
            int randomNum_M = game.map.rand.nextInt(255+1);

            // randomNum_M = randomNum_M - adrenaline*20;

            if (f+(adrenaline*10) >= randomNum_M) { // demo code
                breaksFree = false;
            }
            else {
                breaksFree = true;
            }
            System.out.println("(randomNum_M / f / adr): ("+String.valueOf(randomNum_M)+" / "+String.valueOf(f)+" / +"+String.valueOf(adrenaline*10)+")");
        }

        // simplify and put above
        if (breaksFree == false) { // ie was caught
            return new CatchPokemonWobblesThenCatch(game, this);
        }

        // else, ie breaksFree = true

        int d = game.battle.oppPokemon.currentStats.get("catchRate") * 100 / maxRand;
                //, where the value of Ball is 255 for the Poké Ball, 200 for the Great Ball, or 150 for other balls
        if (d >= 256) {
            // shake 3 times before breaking free
            return new CatchPokemonWobbles3Times(game, new PrintAngryEating(game, new ChanceToRun(game, this) ) );
        }

        int s = 0;// status thing again
        int x = d * f / 255 + s;

        if (x < 10) {
            // ball misses
            return new CatchPokemonMiss(game, new PrintAngryEating(game, new ChanceToRun(game, this) ) );
        }
        else if (x < 30) {
            // ball shakes once
            return new CatchPokemonWobbles1Time(game, new PrintAngryEating(game, new ChanceToRun(game, this) ) );
        }
        else if (x < 70) {
            // ball shakes twice
            return new CatchPokemonWobbles2Times(game, new PrintAngryEating(game, new ChanceToRun(game, this) ) );
        }
        // ball shakes three times before pkmn gets free

        // System.out.println("x: "+String.valueOf(x));
        // System.out.println("Shake three times: "+String.valueOf(x));

        return new CatchPokemonWobbles3Times(game, new PrintAngryEating(game, new ChanceToRun(game, this) ) );
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        // check user input
         //'tl' = top left, etc.
         // modify position by modifying curr to tl, tr, bl or br
        if (InputProcessor.upPressed) {
            if (curr.equals("bl") || curr.equals("br")) {
                curr = "t"+String.valueOf(curr.charAt(1));
                newPos = getCoords.get(curr);
            }

        }
        else if (InputProcessor.downPressed) {
            if (curr.equals("tl") || curr.equals("tr")) {
                curr = "b"+String.valueOf(curr.charAt(1));
                newPos = getCoords.get(curr);
            }
        }
        else if (InputProcessor.leftPressed) {
            if (curr.equals("tr") || curr.equals("br")) {
                curr = String.valueOf(curr.charAt(0))+"l";
                newPos = getCoords.get(curr);
            }
        }
        else if (InputProcessor.rightPressed) {
            if (curr.equals("tl") || curr.equals("bl")) {
                curr = String.valueOf(curr.charAt(0))+"r";
                newPos = getCoords.get(curr);
            }
        }
        // If button press, do something
        if (InputProcessor.aJustPressed) {
            // User selected 'pokeball'
            if (curr.equals("tl")) {
                // decide if caught or not, use corresponding action
                 // this is a trigger action for displayText_triggered
                // Action catchAction = new catchPokemon_miss(game, this);
                Action catchAction = calcIfCaught(game);

                // display text, throw animation, catch or not
                String textString = game.player.name+" used SAFARI BALL!";
                // demo code
                if (game.player.adrenaline < 5) {
                    game.insertAction(new DisplayText(game, textString, null, catchAction,
                            new ThrowPokeball(game, catchAction)
                        )
                    );
                }
                else if (game.player.adrenaline < 15){
                    game.insertAction(new DisplayText(game, textString, null, catchAction,
                            new ThrowFastPokeball(game, catchAction)
                        )
                    );
                }
                else {
                    game.insertAction(new DisplayText(game, textString, null, catchAction,
                            new ThrowHyperPokeball(game, catchAction)
                        )
                    );
                }

                // throw a pokeball, then resume battle menu (maybe)
                // game.insertAction(new ThrowPokeball(game, this));
                game.actionStack.remove(this);
            }

            // user selected 'throw rock'
            else if (curr.equals("bl")) {
                // throw a rock, then resume battle menu

                Action throwRockAction = new ThrowRock(game, new PrintAngryEating(game, new ChanceToRun(game, this) ) );
                String textString = game.player.name+" threw a ROCK.";
                game.insertAction(new DisplayText(game, textString, null, throwRockAction,
                        throwRockAction
                    )
                );

                // game.insertAction(new ThrowRock(game, this));
                game.actionStack.remove(this);
            }

            // user selected 'throw bait'
            else if (curr.equals("tr")) {
                Action throwBaitAction = new ThrowBait(game, new PrintAngryEating(game, new ChanceToRun(game, this) ) );
                String textString = game.player.name+" threw some BAIT.";
                game.insertAction(new DisplayText(game, textString, null, throwBaitAction,
                        throwBaitAction
                    )
                );

                game.actionStack.remove(this);
            }
            // User selected 'run'
            else if (curr.equals("br")) {
                game.actionStack.remove(this);
                game.insertAction(new WaitFrames(game, 18,
                                  new DisplayText(game, "Got away safely!", null, null,
                                  new SplitAction(new BattleFadeOut(game,
                                                  null),
                                  new BattleFadeOutMusic(game,
                                  null)))));
                game.insertAction(new PlaySound("click1",
                                  new PlaySound("run1",
                                  null)));
            }
        }

        // System.out.println("curr: " + curr);

        // draw text box
        this.textBox.draw(game.uiBatch);

        // draw arrow
        this.arrow.setPosition(newPos.x, newPos.y);
        this.arrow.draw(game.uiBatch);
    }
}

// TODO: should either step into this in DrawBattle, or just
// move this code to DrawBattle.
// instance of this assigned to DrawBattle.drawEnemyHealthAction
class DrawEnemyHealth extends Action {
    public static boolean shouldDraw = true;
    SpriteProxy bgSprite;
    SpriteProxy healthSprite;
    int currHealth;
    public Vector2 translateAmt = new Vector2();  // used by some effects that shake health bar

//    public ArrayList<Sprite> healthBar;
    public int layer = 129;

    public DrawEnemyHealth(Game game) {
        Texture text = new Texture(Gdx.files.internal("battle/enemy_healthbar1.png"));
        this.bgSprite = new SpriteProxy(Color.WHITE, text, 0, 0, 160, 144);

        // this could be dangerous?
        // if drawAction is null. this may happen at some point.
        game.battle.drawAction.drawEnemyHealthAction = this;

        // fill sprite array according to enemy health
         // healthbar is 48 pixels long
         // round up when counting
//        this.healthBar = new ArrayList<Sprite>();
        this.currHealth = (int)Math.ceil( ((float)game.battle.oppPokemon.currentStats.get("hp")*48f) / (float)game.battle.oppPokemon.maxStats.get("hp") );

        // System.out.println("numElements: "+String.valueOf(numElements)); // debug

        text = new Texture(Gdx.files.internal("battle/health1.png"));
        this.healthSprite = new SpriteProxy(new Color(240.0f/256f, 208.0f/256f, 120.0f/256f, 1f), text, 0,0,1,2);
//        Sprite temp = new Sprite(text, 0,0,1,2);
//        for (int i = 0; i < numElements; i++) {
//            Sprite temp2 = new Sprite(temp); // to avoid long loading
//            temp2.setPosition(32 + i, 123);
//            this.healthBar.add(temp2);
//        }

    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        if (DrawEnemyHealth.shouldDraw) {
            // draw helper sprite
            // probly remove
            this.bgSprite.setPosition(this.translateAmt.x, this.translateAmt.y);
            this.bgSprite.draw(game.uiBatch);
            String name = game.battle.oppPokemon.name.toUpperCase();
            if (name.contains("UNOWN")) {
                name = "UNOWN";
            }
            char[] textArray = name.toCharArray();
            SpriteProxy letterSprite;
            for (int i=0; i < textArray.length; i++) {
                letterSprite = game.textDict.get(textArray[i]);
                game.uiBatch.draw(letterSprite, 8+8*i +this.translateAmt.x, 136);
            }

            // Draw pkmn level text
            if (game.battle.oppPokemon.status == null || 
                game.battle.oppPokemon.status.equals("confuse") || 
                game.battle.oppPokemon.status.equals("attract")) {
                int tensPlace = game.battle.oppPokemon.level/10;
                // System.out.println("level: "+String.valueOf(tensPlace));
                SpriteProxy tensPlaceSprite = game.textDict.get(Character.forDigit(tensPlace, 10));
                game.uiBatch.draw(tensPlaceSprite, 40 +this.translateAmt.x, 128);

                int offset = 0;
                if (game.battle.oppPokemon.level < 10) {
                    offset = -8;
                }

                int onesPlace = game.battle.oppPokemon.level % 10;
                SpriteProxy onesPlaceSprite = game.textDict.get(Character.forDigit(onesPlace, 10));
                game.uiBatch.draw(onesPlaceSprite, 48+offset +this.translateAmt.x, 128);
            }
            else {
                String text = "";
                if (game.battle.oppPokemon.status.equals("poison") || game.battle.oppPokemon.status.equals("toxic")) {
                    text = "PSN";
                }
                else if (game.battle.oppPokemon.status.equals("paralyze")) {
                    text = "PAR";
                }
                else if (game.battle.oppPokemon.status.equals("freeze")) {
                    text = "FRZ";
                }
                else if (game.battle.oppPokemon.status.equals("sleep")) {
                    text = "SLP";
                }
                else if (game.battle.oppPokemon.status.equals("burn")) {
                    text = "BRN";
                }
                textArray = text.toCharArray();
                for (int i=0; i < textArray.length; i++) {
                    letterSprite = game.textDict.get(textArray[i]);
                    game.uiBatch.draw(letterSprite, 48-8-8+8*i +this.translateAmt.x, 128);
                }
            }
            // Draw health bar
            // TODO: remove if unused
//            for (Sprite bar : this.healthBar) {
//                bar.draw(game.uiBatch);
//            }
            for (int i=0; i < this.currHealth; i++) {
                game.uiBatch.draw(this.healthSprite, 32 +i +this.translateAmt.x, 123);
            }
            // Draw gender icon if the enemy pokemon is male or female
//            int nameOffset = game.battle.oppPokemon.name.length() > 8 ? game.battle.oppPokemon.name.length()-8 : 0;
            if (!game.battle.oppPokemon.isGhost) {
                if (game.battle.oppPokemon.gender.equals("male")) {
                    game.uiBatch.draw(TextureCache.maleSymbol, 72 +this.translateAmt.x, 136 -8);
                }
                else if (game.battle.oppPokemon.gender.equals("female")) {
                    game.uiBatch.draw(TextureCache.femaleSymbol, 72 +this.translateAmt.x, 136 -8);
                }
            }
        }
        // Detect when battle is over,
        // Object will remove itself from AS
        if (game.battle.drawAction == null) {
            game.actionStack.remove(this);
        }
    }
}

/**
 * TODO: should either step into this in DrawBattle, or just
 * move this code to DrawBattle.
 * Instance of this assigned to DrawBattle.drawEnemyHealthAction
 */
class DrawFriendlyHealth extends Action {
    public static boolean shouldDraw = true;
    SpriteProxy bgSprite;
    SpriteProxy healthSprite;

//    public ArrayList<Sprite> healthBar;  // TODO: remove if unused
    int currHealth;
    public int layer = 129;

    boolean firstStep = true;
    public DrawFriendlyHealth(Game game) {
        this(game, null);
    }

    public DrawFriendlyHealth(Game game, Action nextAction) {
        this.nextAction = nextAction;

        Texture text = new Texture(Gdx.files.internal("battle/friendly_healthbar1.png"));
        this.bgSprite = new SpriteProxy(Color.WHITE, text, 0, 0, 160, 144);

        // this.bgSprite.setPosition(0,4); ;// debug

//        text = new Texture(Gdx.files.internal("attack_menu/helper3.png"));
//        this.helperSprite = new Sprite(text, 0, 0, 160, 144);

        // fill sprite array according to enemy health
         // healthbar is 48 pixels long
         // round up when counting
//        this.healthBar = new ArrayList<Sprite>();
        this.currHealth = (int)Math.ceil( ((float)game.player.currPokemon.currentStats.get("hp")*48f) / (float)game.player.currPokemon.maxStats.get("hp") );

        // System.out.println("numElements: "+String.valueOf(numElements)); // debug

        text = TextureCache.get(Gdx.files.internal("battle/health1.png"));
        this.healthSprite = new SpriteProxy(new Color(240.0f/256f, 208.0f/256f, 120.0f/256f, 1f), text, 0, 0, 1, 2);
        // TODO: remove if unused
//        Sprite temp = new Sprite(text, 0, 0, 1, 2);
//        for (int i = 0; i < numElements; i++) {
//            Sprite temp2 = new Sprite(temp); // to avoid long loading
//            temp2.setPosition(96 + i, 67);
//            this.healthBar.add(temp2);
//        }
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        // offset is 96, 80
        if (this.firstStep) {
            game.battle.drawAction.drawFriendlyHealthAction = this;
            this.firstStep = false;
        }

        if (this.nextAction != null) {
            game.insertAction(this.nextAction);
            this.nextAction = null;
        }
        if (DrawFriendlyHealth.shouldDraw) {
            // draw helper sprite
             // probly remove
            this.bgSprite.draw(game.uiBatch);

    //        this.helperSprite.draw(game.floatingBatch);    // debug

            String name = game.player.currPokemon.name.toUpperCase();
            if (name.contains("UNOWN")) {
                name = "UNOWN";
            }
            char[] textArray = name.toCharArray();
            SpriteProxy letterSprite;
            int offset = 0;
            if (textArray.length > 5) {
                offset = -16;
            }
            else if (textArray.length > 2) {
                offset = -8;
            }
            for (int i=0; i < textArray.length; i++) {
                letterSprite = game.textDict.get(textArray[i]);
                game.uiBatch.draw(letterSprite, offset+96+8*i, 80);
            }

            // Draw pkmn level text or current status ailment
            if (game.player.currPokemon.status == null || 
                    game.player.currPokemon.status.equals("confuse") || 
                    game.player.currPokemon.status.equals("attract")) {
                int tensPlace = game.player.currPokemon.level/10;
                // System.out.println("level: "+String.valueOf(tensPlace));
                SpriteProxy tensPlaceSprite = game.textDict.get(Character.forDigit(tensPlace, 10));
                game.uiBatch.draw(tensPlaceSprite, 120, 72);

                offset = 0;
                if (game.player.currPokemon.level < 10) {
                    offset = -8;
                }

                int onesPlace = game.player.currPokemon.level % 10;
                SpriteProxy onesPlaceSprite = game.textDict.get(Character.forDigit(onesPlace, 10));
                game.uiBatch.draw(onesPlaceSprite, 128+offset, 72);
            }
            else {
                String text = "";
                if (game.player.currPokemon.status.equals("poison") || game.player.currPokemon.status.equals("toxic")) {
                    text = "PSN";
                }
                else if (game.player.currPokemon.status.equals("paralyze")) {
                    text = "PAR";
                }
                else if (game.player.currPokemon.status.equals("freeze")) {
                    text = "FRZ";
                }
                else if (game.player.currPokemon.status.equals("sleep")) {
                    text = "SLP";
                }
                else if (game.player.currPokemon.status.equals("burn")) {
                    text = "BRN";
                }
                textArray = text.toCharArray();
                for (int i=0; i < textArray.length; i++) {
                    letterSprite = game.textDict.get(textArray[i]);
                    game.uiBatch.draw(letterSprite, 128-8-8+i*8, 72);
                }
            }
            // Draw health bar
            // TODO: remove if unused
//            for (Sprite bar : this.healthBar) {
//                bar.draw(game.uiBatch);
//            }
            for (int i=0; i < this.currHealth; i++) {
                game.uiBatch.draw(this.healthSprite, 96 +i, 67);
            }
            // Draw gender icon if the friendly pokemon is male or female
//            int nameOffset = game.battle.oppPokemon.name.length() > 8 ? game.battle.oppPokemon.name.length()-8 : 0;
            if (game.player.currPokemon.gender.equals("male")) {
                game.uiBatch.draw(TextureCache.maleSymbol, 136, 72);
            }
            else if (game.player.currPokemon.gender.equals("female")) {
                game.uiBatch.draw(TextureCache.femaleSymbol, 136, 72);
            }
            
         // Draw pkmn max health text 
            int maxHealth = game.player.currPokemon.maxStats.get("hp");
            int hundredsPlace = maxHealth/100;
            if (hundredsPlace > 0) {
                SpriteProxy hudredsPlaceSprite = game.textDict.get(Character.forDigit(hundredsPlace,10));
                game.uiBatch.draw(hudredsPlaceSprite, 120, 56);
            }
            int tensPlace = (maxHealth % 100) / 10;
            if (tensPlace > 0 || hundredsPlace > 0) {
            	SpriteProxy tensPlaceSprite = game.textDict.get(Character.forDigit(tensPlace,10));
                game.uiBatch.draw(tensPlaceSprite, 120 +8, 56);
            }
            int onesPlace = maxHealth % 10;
            SpriteProxy onesPlaceSprite = game.textDict.get(Character.forDigit(onesPlace,10));
            game.uiBatch.draw(onesPlaceSprite, 120 +16, 56);

            // Draw pkmn current health text
            int currHealthRemaining = (this.currHealth*maxHealth)/48;
            hundredsPlace = currHealthRemaining/100;
            if (hundredsPlace > 0) {
            	SpriteProxy hudredsPlaceSprite = game.textDict.get(Character.forDigit(hundredsPlace,10));
                game.uiBatch.draw(hudredsPlaceSprite, 88, 56);
            }
            tensPlace = (currHealthRemaining % 100) / 10;
            if (tensPlace > 0 || hundredsPlace > 0) {
            	SpriteProxy tensPlaceSprite = game.textDict.get(Character.forDigit(tensPlace,10));
                game.uiBatch.draw(tensPlaceSprite, 88 +8, 56);
            }
            onesPlace = currHealthRemaining % 10;
            onesPlaceSprite = game.textDict.get(Character.forDigit(onesPlace,10));
            game.uiBatch.draw(onesPlaceSprite, 88 +16, 56);
        }
        // detect when battle is over,
        // object will remove itself from AS
        if (game.battle.drawAction == null) {
            game.actionStack.remove(this);
        }
    }
}

/**
 * Draw item menu, used in overworld and battle.
 */
class DrawItemMenu extends MenuAction {
    // which item the player was viewing last
    public static int lastCurrIndex = 0;
    public static int lastCursorPos = 0;
    Sprite arrow;

    Sprite arrowWhite;
    Sprite textBox;

    public int layer = 107;

    Map<Integer, Vector2> arrowCoords;
    Vector2 newPos;
    Sprite helperSprite;
    ArrayList<ArrayList<Sprite>> spritesToDraw = new ArrayList<ArrayList<Sprite>>();
    int cursorDelay; // this is just extra detail. cursor has 2 frame delay before showing in R/B
    int cursorPos;
    int currIndex;
    ArrayList<String> itemsList;
    Sprite downArrow;
    int downArrowTimer;
    boolean refresh = false;
    public int upTimer = 0;
    public int downTimer = 0;

    public DrawItemMenu(Game game, MenuAction prevMenu) {
        this.prevMenu = prevMenu;
        this.disabled = false;
        this.cursorDelay = 0;
        this.arrowCoords = new HashMap<Integer, Vector2>();
        Texture text = new Texture(Gdx.files.internal("battle/arrow_right1.png"));
        this.arrow = new Sprite(text, 0, 0, 5, 7);
        text = new Texture(Gdx.files.internal("battle/arrow_right_white.png"));
        this.arrowWhite = new Sprite(text, 0, 0, 5, 7);
        // text box bg
        text = new Texture(Gdx.files.internal("attack_menu/item_menu1.png"));
        this.textBox = new Sprite(text, 0,0, 16*10, 16*9);

        // down arrow for items menu
        text = new Texture(Gdx.files.internal("arrow_down.png"));
        this.downArrow = new Sprite(text, 0, 0, 7, 5);
        this.downArrow.setPosition(144, 50);
        this.downArrowTimer = 0;

        this.currIndex = DrawItemMenu.lastCurrIndex; // this is what range of items gets displayed (4 at a time)
        this.cursorPos = DrawItemMenu.lastCursorPos; // this is where cursor is displayed

        // finite amount of cursor coordinates (4)
        // this.arrowCoords.put(1, new Vector2(89, 72+16+16)); // example
        this.arrowCoords.put(0, new Vector2(41, 104 - 16*0));
        this.arrowCoords.put(1, new Vector2(41, 104 - 16*1));
        this.arrowCoords.put(2, new Vector2(41, 104 - 16*2));
        this.arrowCoords.put(3, new Vector2(41, 104 - 16*3));

        newPos = arrowCoords.get(cursorPos);
        this.arrow.setPosition(newPos.x, newPos.y);
    }
    
    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void firstStep(Game game) {
        this.itemsList = new ArrayList<String>(game.player.itemsDict.keySet());
        this.itemsList.add("Cancel");
        if (this.currIndex + this.cursorPos > itemsList.size()) {
            if (this.currIndex > 0) {
                this.currIndex--;
            }
            else if (this.cursorPos > 0) {
                this.cursorPos--;
            }
        }
        this.spritesToDraw.clear();
        // Convert player item list to sprites
        for (String entry : this.itemsList) {
            // Shorten apricorn to aprcrn
            String text = entry;
            if (text.toLowerCase().contains("apricorn")) {
                text = text.toLowerCase().replace("apricorn", "aprcn");
            }
            char[] textArray = text.toUpperCase().toCharArray(); // iterate elements
            Sprite currSprite;
            int i = 0;
            ArrayList<Sprite> word = new ArrayList<Sprite>();
            for (char letter : textArray) {
                Sprite letterSprite = game.textDict.get((char)letter);
                if (letterSprite == null) {
                    letterSprite = game.textDict.get(null);
                }
                currSprite = new Sprite(letterSprite);
                currSprite.setPosition(48+8*i, 104);
                word.add(currSprite);
                i++;
            }
            if (game.player.itemsDict.containsKey(entry)) {
                char[] numItemsArray = String.format("%02d", game.player.itemsDict.get(entry)).toCharArray();
                i = 10;
                for (char letter : numItemsArray) {
                    i += 1;
                    if (letter == '0' && i == 11) {
                        continue;
                    }
                    Sprite letterSprite = game.textDict.get((char)letter);
                    currSprite = new Sprite(letterSprite);
                    currSprite.setPosition(48+8*i, 104);
                    word.add(currSprite);
                }
            }
            this.spritesToDraw.add(word);
        }
    }

    @Override
    public void step(Game game) {
        if (this.prevMenu != null) {
            this.prevMenu.step(game);
        }

        // draw text box
//        this.textBox.draw(game.uiBatch);
        game.uiBatch.draw(this.textBox, this.textBox.getX(), this.textBox.getY());

        // debug
//        helperSprite.draw(game.floatingBatch);

        // draw the menu items
        int j = 0;
        for (int i = 0; i < this.spritesToDraw.size(); i++) {
            if (i >= this.currIndex && i < this.currIndex +4) { // only draw range of 4 starting at currIndex
                ArrayList<Sprite> word = this.spritesToDraw.get(i);
                for (Sprite sprite : word) {
                    // draw this string as text on the screen
                    game.uiBatch.draw(sprite, sprite.getX(), sprite.getY() - j*16);
                }
                j+=1;
            }
        }

        // return at this point if this menu is disabled
        if (this.disabled) {
            this.refresh = true;
//            this.arrowWhite.setPosition(newPos.x, newPos.y);
//            this.arrowWhite.draw(game.uiBatch);
            game.uiBatch.draw(this.arrowWhite, newPos.x, newPos.y);
            return;
        }
        if (this.refresh) {
            this.firstStep(game);
            this.refresh = false;
        }

        // Scroll fast if button is held for a while.
        if (InputProcessor.upPressed) {
            if (this.upTimer < 20) {
                this.upTimer++;
            }
        }
        else if (InputProcessor.downPressed) {
            if (this.downTimer < 20) {
                this.downTimer++;
            }
        }
        else {
            this.upTimer = 0;
            this.downTimer = 0;
        }
        
        // Check user input
         //'tl' = top left, etc.
         // modify position by modifying curr to tl, tr, bl or br
        if (InputProcessor.upJustPressed || this.upTimer >= 20) {
            if (cursorPos > 0) {
                cursorPos -= 1;
                newPos = arrowCoords.get(cursorPos);
            }
            else if (this.currIndex > 0) {
                this.currIndex -= 1;
            }

        }
        else if (InputProcessor.downJustPressed || this.downTimer >= 20) {
            if (this.cursorPos < 2 && this.currIndex+this.cursorPos+1 < this.itemsList.size()) {
                this.cursorPos += 1;
                newPos = arrowCoords.get(this.cursorPos);
            }
            else if (this.currIndex < this.itemsList.size() - 3) {
                this.currIndex += 1;
            }
        }

        // Draw arrow
        if (this.cursorDelay >= 5) {
//            this.arrow.setPosition(newPos.x, newPos.y);
//            this.arrow.draw(game.uiBatch);
            game.uiBatch.draw(this.arrow, newPos.x, newPos.y);
        }
        else {
            this.cursorDelay+=1;
        }

        // Draw downarrow if applicable
        if ((this.itemsList.size() - this.currIndex) > 4 ) {
            if (this.downArrowTimer < 22) {
//                this.downArrow.draw(game.uiBatch);
                game.uiBatch.draw(this.downArrow, this.downArrow.getX(), this.downArrow.getY());
            }
            this.downArrowTimer++;
        }
        else {
            this.downArrowTimer = 0; // force arrow to start over when scroll up
        }

        if (this.downArrowTimer > 41) {
            this.downArrowTimer = 0;
        }

        // Button interaction is below drawing b/c I want to be able to return here
        // If press A, draw use/toss for item
        if (InputProcessor.aJustPressed) {
            game.insertAction(new PlaySound("click1", null));
            String name = this.itemsList.get(currIndex + cursorPos);
            // save last position
            DrawItemMenu.lastCurrIndex = this.currIndex;
            DrawItemMenu.lastCursorPos = this.cursorPos;

            if ("Cancel".equals(name)) {
                this.prevMenu.disabled = false;
                game.insertAction(this.prevMenu);
                game.actionStack.remove(this);
                return;
            }
            else {
                this.disabled = true;
                game.actionStack.remove(this);
                game.insertAction(new DrawUseTossMenu(game, this, name));
                return;
            }
        }
        // player presses b, ie wants to go back
        else if (InputProcessor.bJustPressed) {
            DrawItemMenu.lastCurrIndex = this.currIndex;  // save last position
            DrawItemMenu.lastCursorPos = this.cursorPos;
            this.prevMenu.disabled = false;
            game.actionStack.remove(this);
            game.insertAction(this.prevMenu);
            game.insertAction(new PlaySound("click1", null));
            return;
        }

    }

    static class Intro extends Action {
        int length;

        public int layer = 110;
        MenuAction prevMenu;

        public Intro(MenuAction prevMenu, int length, Action nextAction) {
            this.prevMenu = prevMenu;
            this.nextAction = nextAction;
            this.length = length;
        }

        public String getCamera() {return "gui";}

        public int getLayer(){return this.layer;}

        @Override
        public void step(Game game) {
            if (this.prevMenu != null) {
                this.prevMenu.step(game);
            }
            this.length--;
            if (this.length <= 0) {
                game.insertAction(this.nextAction);
                game.actionStack.remove(this);
            }

        }

    }

}

/**
 * Draw player menu, ie pokedex, pokemon, items, etc. only appears while in overworld, ie not a battle menu.
 */
class DrawPlayerMenu extends MenuAction {
    public static int lastIndex = 0;
    Sprite arrow;
    Sprite arrowWhite;
    Sprite textBox;
    public int layer = 108;
    Map<Integer, Vector2> arrowCoords;
    Vector2 newPos;
    Sprite helperSprite;
    ArrayList<ArrayList<Sprite>> spritesToDraw;
    int cursorDelay; // this is just extra detail. cursor has 2 frame delay before showing in R/B
    String[] entries; // pokemon, items etc

    public DrawPlayerMenu(Game game, Action nextAction) {
        this.disabled = false;
        this.drawArrowWhite = false;

        this.nextAction = nextAction;

        this.cursorDelay = 0;

        this.arrowCoords = new HashMap<Integer, Vector2>();
        this.spritesToDraw = new ArrayList<ArrayList<Sprite>>();

        Texture text = new Texture(Gdx.files.internal("battle/arrow_right1.png"));
        this.arrow = new Sprite(text, 0, 0, 5, 7);

        text = new Texture(Gdx.files.internal("battle/arrow_right_white.png"));
        this.arrowWhite = new Sprite(text, 0, 0, 5, 7);

        // text box bg
//        text = new Texture(Gdx.files.internal("attack_menu/menu3_smaller.png"));  // TODO: remove
        text = new Texture(Gdx.files.internal("attack_menu/menu4.png"));
        this.textBox = new Sprite(text, 0,0, 16*10, 16*9);

        this.arrowCoords.put(0, new Vector2(89, 72+32+16));
        this.arrowCoords.put(1, new Vector2(89, 72+16+16));
        this.arrowCoords.put(2, new Vector2(89, 72+16));

        // this.newPos =  new Vector2(32, 79); // post scaling change
        this.currIndex = DrawPlayerMenu.lastIndex;
        this.newPos = this.arrowCoords.get(this.currIndex); // new Vector2(89, 72+32+16);
        this.arrow.setPosition(newPos.x, newPos.y);

//        this.menuActions = new ArrayList<Action>();  // TODO: remove

        // populate sprites for entries in menu
        this.entries = new String[]{"POKéMON", "ITEM", "SAVE"};
        for (String entry : this.entries) {
            char[] textArray = entry.toCharArray(); // iterate elements
            Sprite currSprite;
            int i = 0;
            int j = 0;
            ArrayList<Sprite> word = new ArrayList<Sprite>();
            for (char letter : textArray) {
                Sprite letterSprite = game.textDict.get((char)letter);
                if (letterSprite == null) {
                    letterSprite = game.textDict.get(null);
                }
                currSprite = new Sprite(letterSprite); // copy sprite from char-to-Sprite dictionary

                currSprite.setPosition(96+8*i, 120-8*j);
                word.add(currSprite);
                // go down a line if needed
                 // TODO - do this for words, not chars. split on space, array
                if (i >= 17) {
                    i = 0; j++;
                }
                else {
                    i++;
                }
            }
            spritesToDraw.add(word);
        }

        // helper sprite
//        text = new Texture(Gdx.files.internal("attack_menu/helper6.png"));
//        this.helperSprite = new Sprite(text, 0,0, 16*10, 16*9);
    }
    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        // System.out.println("curr: " + curr);

        // draw text box
//        this.textBox.draw(game.uiBatch);
        game.uiBatch.draw(this.textBox, this.textBox.getX(), this.textBox.getY());

        // debug
//        helperSprite.draw(game.floatingBatch);

        // draw the menu items
        int j = 0;
        for (ArrayList<Sprite> word : this.spritesToDraw) {
            for (Sprite sprite : word) {
                // convert string to text
                game.uiBatch.draw(sprite, sprite.getX(), sprite.getY() - j*16);
            }
            j+=1;
        }

        if (this.disabled == true) {
            if (this.drawArrowWhite == true) {
                this.arrowWhite.setPosition(newPos.x, newPos.y);
                this.arrowWhite.draw(game.uiBatch);
            }
            return;
        }

        // check user input
         //'tl' = top left, etc.
         // modify position by modifying curr to tl, tr, bl or br
        if (InputProcessor.upJustPressed) {
            if (this.currIndex > 0) {
                this.currIndex -= 1;
                newPos = arrowCoords.get(this.currIndex);
            }

        }
        else if (InputProcessor.downJustPressed) {
            if (this.currIndex < 2) {
                this.currIndex += 1;
                newPos = arrowCoords.get(this.currIndex);
            }
        }

        if (InputProcessor.aJustPressed) {
            game.insertAction(new PlaySound("click1", null));

            String currEntry = this.entries[this.currIndex];
            // We also need to create an 'action' that each of these items goes to
            if (currEntry.equals("POKéMON")) {
                game.insertAction(new DrawPokemonMenu.Intro(
                                  new DrawPokemonMenu(game,
                                  this)));
            }
            else if (currEntry.equals("ITEM")) {
                game.insertAction(new DrawItemMenu.Intro(this, 9,
                                  new DrawItemMenu(game,
                                  this)));
            }
            else if (currEntry.equals("SAVE")) {
                Action saveAction = new PkmnMap.PeriodicSave(game);
                saveAction.step(game);
                game.insertAction(new DisplayText(game, game.player.name+" saved the game!", "save1.ogg", null,
                                  new WaitFrames(game, 6,
                                  new SetField(game, "playerCanMove", true,
                                  // TODO: migrate to just use this
                                  // Required if player is flying
                                  new SetField(game.player, "canMove", true,
                                  null)))));
            }

            game.actionStack.remove(this);
            this.disabled = true;

        }
        // player presses b, ie wants to go back
        else if (InputProcessor.bJustPressed || InputProcessor.startJustPressed) {
            DrawPlayerMenu.lastIndex = this.currIndex;
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
            game.insertAction(new PlaySound("click1", null));
        }

        // draw arrow
        if (this.cursorDelay >= 2) {
//            this.arrow.setPosition(newPos.x, newPos.y);
//            this.arrow.draw(game.uiBatch);
            game.uiBatch.draw(this.arrow, newPos.x, newPos.y);
        }
        else {
            this.cursorDelay+=1;
        }
    }

    public static class Intro extends Action {
        ArrayList<Sprite> sprites;
        Sprite sprite;
        ArrayList<Integer> repeats;
        ArrayList<String> sounds;
        String sound;

        public int layer = 120;
        Sprite helperSprite;

        public Intro(Game game, Action nextAction) {
            this.nextAction = nextAction;

//            Texture text = new Texture(Gdx.files.internal("attack_menu/menu3_smaller.png"));
            Texture text = new Texture(Gdx.files.internal("attack_menu/menu4.png"));

            this.sprites = new ArrayList<Sprite>(); // may use this in future
            this.sprite = new Sprite(text, 0, 0, 160, 144);

            this.repeats = new ArrayList<Integer>();
            this.repeats.add(17);

            this.sounds = new ArrayList<String>();
            this.sounds.add(null);

//            text = new Texture(Gdx.files.internal("attack_menu/helper3.png"));
//            this.helperSprite = new Sprite(text, 0, 0, 160, 144);
        }

        public String getCamera() {return "gui";}

        public int getLayer(){return this.layer;}

        @Override
        public void step(Game game) {
            // get next frame
//            this.sprite.draw(game.uiBatch);
            game.uiBatch.draw(this.sprite, this.sprite.getX(), this.sprite.getY());

            // set sprite position
            // if done with anim, do nextAction
            if (this.repeats.isEmpty()) {
                game.insertAction(new PlaySound("menu_open1", null));

                game.insertAction(this.nextAction);
                game.actionStack.remove(this);
                return;
            }

            // debug
//            this.helperSprite.draw(game.floatingBatch);

            // debug
//            if (this.repeats.size() == 14) {
//                return;
//            }

            // get next sound, play it
            this.sound = this.sounds.get(0);
            if (this.sound != null) {
                game.insertAction(new PlaySound("menu_open1", null));
                this.sounds.set(0, null); // don't play same sound over again
            }

            // repeat sprite/pos for current object for 'frames[0]' number of frames.
            if (this.repeats.get(0) > 1) {
                this.repeats.set(0, this.repeats.get(0) - 1);
            }
            else {
                // since position is relative, only update once each time period
                repeats.remove(0);
                sounds.remove(0);
            }
        }
    }

}

/**
 * Pokemon menu, used in battle and overworld.
 */
class DrawPokemonMenu extends MenuAction {
    public static boolean drawChoosePokemonText = true;
    public static int avatarAnimCounter = 24;
    public static int currIndex = 0; // currently selected pokemon
    public static int lastIndex = 0;
    public int layer = 107;
    Sprite bgSprite;
    Sprite helperSprite;
    Sprite arrow;
    Sprite arrowWhite;
    Sprite healthBar;
    Sprite healthSprite;
    Vector2 newPos;
    Map<Integer, Vector2> arrowCoords;
    String item = null;

    public DrawPokemonMenu(Game game, String item, MenuAction prevMenu) {
        this(game, prevMenu);
        this.item = item;
    }

    public DrawPokemonMenu(Game game, MenuAction prevMenu) {
        this.prevMenu = prevMenu;

        Texture text = new Texture(Gdx.files.internal("battle/arrow_right_white.png"));
        this.arrowWhite = new Sprite(text, 0, 0, 5, 7);

        text = new Texture(Gdx.files.internal("battle/arrow_right1.png"));
        this.arrow = new Sprite(text, 0, 0, 5, 7);

        text = new Texture(Gdx.files.internal("pokemon_menu/health_bar.png"));
        this.healthBar = new Sprite(text, 0, 0, 160, 16);

        DrawPokemonMenu.currIndex = DrawPokemonMenu.lastIndex;

        this.arrowCoords = new HashMap<Integer, Vector2>();
        for (int i=0; i < 6; i++) {
            this.arrowCoords.put(i, new Vector2(1, 128 - 16*i));
        }
        // cursor position based on lastIndex
        this.newPos = this.arrowCoords.get(DrawPokemonMenu.currIndex);

        text = new Texture(Gdx.files.internal("battle/health1.png"));
        this.healthSprite = new Sprite(text, 0,0,1,2);

        text = new Texture(Gdx.files.internal("battle/battle_bg3.png"));
        this.bgSprite = new Sprite(text, 8, 8, 16*10, 16*9);
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        if (this.prevMenu != null) {
            this.prevMenu.step(game);
        }
        game.uiBatch.draw(this.bgSprite, this.bgSprite.getX(), this.bgSprite.getY());
//        this.bgSprite.draw(game.uiBatch);  // TODO: remove
        // 1 frame delay - delay when switching to new avatar
        // 6 frames first, 6 second
        // draw health bars
        for (int i=0; i < game.player.pokemon.size(); i++) {
            Pokemon currPokemon = game.player.pokemon.get(i);
            // animate current pokemon avatar
            if (i == DrawPokemonMenu.currIndex) {
                if (DrawPokemonMenu.avatarAnimCounter >= 18) {
                    game.uiBatch.draw(currPokemon.avatarSprites.get(0), 8, 128 -16*i);
                }
                else if (DrawPokemonMenu.avatarAnimCounter >= 12) {
                    game.uiBatch.draw(currPokemon.avatarSprites.get(1), 8, 128 -16*i);
                }
                else if (DrawPokemonMenu.avatarAnimCounter >= 6) {
                    game.uiBatch.draw(currPokemon.avatarSprites.get(2), 8, 128 -16*i);
                }
                else {
                    game.uiBatch.draw(currPokemon.avatarSprites.get(3), 8, 128 -16*i);
                }
            }
            else {
                game.uiBatch.draw(currPokemon.avatarSprites.get(0), 8, 128 -16*i);
            }

            // Draw pokemon name
            // TODO: cache this somewhere
            // .split("_")[0] is to not display the _w in unown_w, for example
            // TODO: I just need to implement real vs displayed name, somehow
            //       Displayed is probably the nickname, whereas real probably
            //       should be an enum, maybe.
            char[] textArray = currPokemon.name.split("_")[0].toUpperCase().toCharArray();
            Sprite letterSprite;
            for (int j=0; j < textArray.length; j++) {
                letterSprite = game.textDict.get(textArray[j]);
                game.uiBatch.draw(letterSprite, 24 +8*j, 136 -16*i);
            }
            if (currPokemon.isEgg) {
                continue;
            }

            // Draw status bar
            game.uiBatch.draw(this.healthBar, 0, 128 -16*i);

            // Draw pkmn level text
            if (currPokemon.status == null ||
                    currPokemon.status.equals("confuse") || 
                    currPokemon.status.equals("attract")) {
                int tensPlace = currPokemon.level/10;
                Sprite tensPlaceSprite = game.textDict.get(Character.forDigit(tensPlace,10));
                game.uiBatch.draw(tensPlaceSprite, 112, 136 -16*i);
                int offset = 0;
                if (currPokemon.level >= 10) {
                    offset = 8;
                }
                int onesPlace = currPokemon.level % 10;
                Sprite onesPlaceSprite = game.textDict.get(Character.forDigit(onesPlace,10));
                game.uiBatch.draw(onesPlaceSprite, 112 +offset, 136 -16*i);
            }
            else {
                String text = "";
                if (currPokemon.status.equals("poison") || currPokemon.status.equals("toxic")) {
                    text = "PSN";
                }
                else if (currPokemon.status.equals("paralyze")) {
                    text = "PAR";
                }
                else if (currPokemon.status.equals("freeze")) {
                    text = "FRZ";
                }
                else if (currPokemon.status.equals("sleep")) {
                    text = "SLP";
                }
                else if (currPokemon.status.equals("burn")) {
                    text = "BRN";
                }
                textArray = text.toCharArray();
                for (int j=0; j < textArray.length; j++) {
                    letterSprite = game.textDict.get(textArray[j]);
                    game.uiBatch.draw(letterSprite, 112-8 +8*j, 136 -16*i);
                }
            }

            // Draw pkmn max health text
            int maxHealth = currPokemon.maxStats.get("hp");
            int hundredsPlace = maxHealth/100;
            if (hundredsPlace > 0) {
                Sprite hudredsPlaceSprite = game.textDict.get(Character.forDigit(hundredsPlace,10));
                game.uiBatch.draw(hudredsPlaceSprite, 136, 128 -16*i);
            }
            int tensPlace = (maxHealth % 100) / 10;
            if (tensPlace > 0 || hundredsPlace > 0) {
                Sprite tensPlaceSprite = game.textDict.get(Character.forDigit(tensPlace,10));
                game.uiBatch.draw(tensPlaceSprite, 136 +8, 128 -16*i);
            }
            int onesPlace = maxHealth % 10;
            Sprite onesPlaceSprite = game.textDict.get(Character.forDigit(onesPlace,10));
            game.uiBatch.draw(onesPlaceSprite, 136 +16, 128 -16*i);

            // Draw pkmn current health text
            int currHealth = currPokemon.currentStats.get("hp");
            hundredsPlace = currHealth/100;
            if (hundredsPlace > 0) {
                Sprite hudredsPlaceSprite = game.textDict.get(Character.forDigit(hundredsPlace,10));
                game.uiBatch.draw(hudredsPlaceSprite, 104, 128 -16*i);
            }
            tensPlace = (currHealth % 100) / 10;
            if (tensPlace > 0 || hundredsPlace > 0) {
                Sprite tensPlaceSprite = game.textDict.get(Character.forDigit(tensPlace,10));
                game.uiBatch.draw(tensPlaceSprite, 104 +8, 128 -16*i);
            }
            onesPlace = currHealth % 10;
            onesPlaceSprite = game.textDict.get(Character.forDigit(onesPlace,10));
            game.uiBatch.draw(onesPlaceSprite, 104 +16, 128 -16*i);

            // Draw health bar
            int targetSize = (int)Math.ceil( (currPokemon.currentStats.get("hp")*48) / currPokemon.maxStats.get("hp"));
            for (int j=0; j < targetSize; j++) {
                game.uiBatch.draw(this.healthSprite, 48 +1*j, 131 -16*i);
            }

            // Draw gender
            // TODO: this isn't present in gsc, but instead is shown in the stats
            // screen which is still TODO.
            // Draw gender icon if the enemy pokemon is male or female
            if (currPokemon.gender.equals("male")) {
                game.uiBatch.draw(TextureCache.maleSymbol, 136, 136 -16*i);  // x - 152
            }
            else if (currPokemon.gender.equals("female")) {
                game.uiBatch.draw(TextureCache.femaleSymbol, 136, 136 -16*i);
            }
        }

        // Draw 'Choose a pokemon' text
        if (DrawPokemonMenu.drawChoosePokemonText) {
            char[] textArray = "Choose a POKéMON.".toCharArray();
            Sprite letterSprite;
            for (int j=0; j < textArray.length; j++) {
                letterSprite = game.textDict.get(textArray[j]);
                game.uiBatch.draw(letterSprite, 8 +8*j, 24);
            }
        }

        if (this.drawArrowWhite) {
            // draw white arrow
//            this.arrowWhite.setPosition(this.newPos.x, this.newPos.y);
//            this.arrowWhite.draw(game.uiBatch);
            game.uiBatch.draw(this.arrowWhite, newPos.x, newPos.y);
        }
        

        if (this.goAway) {
            game.actionStack.remove(this);
            return;
        }

        // return at this point if this menu is disabled
        if (this.disabled) {
            return;
        }

        // Decrement avatar anim counter
        DrawPokemonMenu.avatarAnimCounter--;
        if (DrawPokemonMenu.avatarAnimCounter <= 0) {
            DrawPokemonMenu.avatarAnimCounter = 23;
        }

        // Handle arrow input
        if (InputProcessor.upJustPressed) {
            if (DrawPokemonMenu.currIndex > 0) {
                DrawPokemonMenu.currIndex -= 1;
                DrawPokemonMenu.avatarAnimCounter = 24; // reset to 12 for 1 extra frame of first frame for avatar anim
            }
        }
        else if (InputProcessor.downJustPressed) {
            if (DrawPokemonMenu.currIndex < game.player.pokemon.size()-1) {
                DrawPokemonMenu.currIndex += 1;
                DrawPokemonMenu.avatarAnimCounter = 24; // reset to 12 for 1 extra frame of first frame for avatar anim
            }
        }
        newPos = this.arrowCoords.get(DrawPokemonMenu.currIndex);

        // draw the arrow sprite
//        this.arrow.setPosition(this.newPos.x, this.newPos.y);
//        this.arrow.draw(game.uiBatch);
        game.uiBatch.draw(this.arrow, newPos.x, newPos.y);

        // Button interaction is below drawing b/c I want to be able to return here
        // if press a, draw use/toss for item
        if (InputProcessor.aJustPressed) { // using isKeyJustPressed rather than isKeyPressed
            game.insertAction(new PlaySound("click1", null));

            // This was done from battle screen, so send out the pokemon (Go! <pokemon name>!).
            // TODO: probably just have game.battle do drawing, so check game.battle != null.
//            if (game.battle.drawAction != null) {
//                if (game.type == Game.Type.CLIENT) {
//                    game.client.sendTCP(new com.pkmngen.game.Network.DoBattleAction(game.player.network.id,
//                                                                                    Battle.DoTurn.Type.SWITCH,
//                                                                                    DrawPokemonMenu.currIndex));
//                }
//                
////                // stop drawing friendly healthbar
////                game.actionStack.remove(game.battle.drawAction.drawFriendlyHealthAction);
////                game.battle.drawAction.drawFriendlyHealthAction = null;
////                // stop drawing friendly sprite
////                game.actionStack.remove(game.battle.drawAction.drawFriendlyPokemonAction);
////                game.battle.drawAction.drawFriendlyPokemonAction = null;
////
////                game.player.currPokemon = game.player.pokemon.get(DrawPokemonMenu.currIndex);
////                game.actionStack.remove(this);
////                game.insertAction(new DrawPokemonMenu.Intro(13,
////                                  new DisplayText(game, "Go! "+game.player.currPokemon.name.toUpperCase()+"!",
////                                                  null, true, false,
////                                  new ThrowOutPokemonCrystal(game,
////                                  new PlaySound(game.player.currPokemon,
////                                  new WaitFrames(game, 6,
////                                  new DrawFriendlyHealth(game,
////                                  new DisplayText.Clear(game,
////                                  new WaitFrames(game, 3,
////                                  new WaitFrames(game, 15,
////                                  new DrawBattleMenuNormal(game,
////                                  null)))))))))));
//                Action switchAction = new SplitAction(new PlaySound("click1", null),
//                                      new Battle.DoTurn(game, Battle.DoTurn.Type.SWITCH,
//                                      new WaitFrames(game, 15,
//                                      new DrawBattleMenuNormal(game,
//                                      null))));
//                game.actionStack.remove(this);
//                game.insertAction(switchAction);
//                return;
//            }
            this.disabled = true;
            this.drawArrowWhite = true;
            // Once player hits b in selected menu, avatar anim starts over
            Pokemon currPokemon = game.player.pokemon.get(DrawPokemonMenu.currIndex);
            // TODO: wouldn't need this if had yield, could insert yield in DrawUseTossMenu
            if (this.item != null) {
                // Items can't be used on eggs
                if (currPokemon.isEgg) {
                    game.insertAction(new DisplayText(game, "It wonì have any effect.", null, null,
                                      new SetField(this, "goAway", true,
                                      new DrawPokemonMenu.Outro(
                                      this.prevMenu))));
                    return;
                }
                if (this.item.equals("moomoo milk") || this.item.equals("berry juice")) {
                    int diff = currPokemon.maxStats.get("hp") - currPokemon.currentStats.get("hp");
                    if (diff <= 0 || currPokemon.currentStats.get("hp") <= 0) {
                        game.insertAction(new DisplayText(game, "It wonì have any effect.", null, null,
                                          new SetField(this, "goAway", true,
                                          new DrawPokemonMenu.Outro(
                                          this.prevMenu))));
                        return;
                    }
                    // Deduct from inventory
                    game.player.itemsDict.put(this.item, game.player.itemsDict.get(this.item)-1);
                    if (game.player.itemsDict.get(this.item) <= 0) {
                        game.player.itemsDict.remove(this.item);
                    }
                    int restoreAmt = 100;
                    if (this.item.equals("berry juice")) {
                        restoreAmt = 20;
                    }
                    if (diff < restoreAmt) {
                        restoreAmt = diff;
                    }
                    // TODO: yields would prevent from having to check if in battle or not here.
                    // TODO: remove
//                    game.insertAction(new RestoreHealth(currPokemon, -restoreAmt,
//                                      new DisplayText(game, currPokemon.name.toUpperCase()+" gained "+String.valueOf(restoreAmt)+" hp!", null, null,
//                                      new SetField(this, "goAway", true,
//                                      new DrawPokemonMenu.Outro(
//                                      this.prevMenu)))));
                    game.insertAction(new PlaySound("potion1", null));
                    Action nextAction = new RestoreHealth(currPokemon, -restoreAmt,
                                        new DisplayText(game, currPokemon.name.toUpperCase()+" gained "+String.valueOf(restoreAmt)+" hp!", null, null,
                                        new SetField(this, "goAway", true,
                                        null)));
                    if (game.battle.drawAction == null) {
                        nextAction.append(new DrawPokemonMenu.Outro(this.prevMenu));
                    }
                    else {
                        game.battle.network.turnData = new BattleTurnData();
                        game.battle.network.turnData.itemName = this.item;
                        // TODO: test
                        // TODO: instead of this prevMenu.prevMenu stuff, just
                        //       have Battle.DoTurn insert a new battle menu
                        //       action after each turn.
                        System.out.println(this.prevMenu.prevMenu);
                        this.prevMenu.prevMenu.disabled = false;
//                        game.actionStack.remove(this.prevMenu.prevMenu);  // No idea why this is needed.
                        nextAction.append(new DrawPokemonMenu.Outro(
                                          new Battle.DoTurn(game, Battle.DoTurn.Type.ITEM, this.prevMenu.prevMenu)));
                        // Do this or else it's step() will be called b/c this
                        // menu persists for a while.
                        this.prevMenu.prevMenu = null;  
                    }
                    game.insertAction(nextAction);
                    return;
                }
                if (this.item.equals("rare candy")) {
                    if (currPokemon.level >= 99) {
                        game.insertAction(new DisplayText(game, "It wonì have any effect.", null, null,
                                          new SetField(this, "goAway", true,
                                          new DrawPokemonMenu.Outro(
                                          this.prevMenu))));
                        return;
                    }
                    // Deduct from inventory
                    game.player.itemsDict.put(this.item, game.player.itemsDict.get(this.item)-1);
                    if (game.player.itemsDict.get(this.item) <= 0) {
                        game.player.itemsDict.remove(this.item);
                    }
                    currPokemon.gainLevel(1);
                    currPokemon.currentStats.put("hp", currPokemon.maxStats.get("hp"));
                    game.insertAction(new DisplayText(game, currPokemon.name.toUpperCase()+" grew to level "+String.valueOf(currPokemon.level)+"!", "fanfare1.ogg", null,
                                      new SetField(this, "goAway", true,
                                      new DrawPokemonMenu.Outro(
                                      this.prevMenu))));
                    return;
                }
                game.insertAction(new DisplayText(game, "Dev note - invalid item.", null, null,
                                                  new SetField(this, "goAway", true,
                                                  new DrawPokemonMenu.Outro(
                                                  this.prevMenu))));
                return;
            }
            game.actionStack.remove(this);
            game.insertAction(new DrawPokemonMenu.SelectedMenu(this, currPokemon));
            return;
        }
        // player presses b, ie wants to go back
        // if prevMenu == null that means player pokemon just fainted and player must
        // send out another
        // TODO: vgc plays error noise when this happens (attack_anims/test3.avi)
        else if (InputProcessor.bJustPressed && this.prevMenu != null) {
            DrawPokemonMenu.lastIndex = DrawPokemonMenu.currIndex;
            game.actionStack.remove(this);
            game.insertAction(new DrawPokemonMenu.Outro(this.prevMenu));
            return;
        }
    }

    static class Intro extends Action {
        public int layer = 110;
        int duration = 18;
        Sprite bgSprite;

        public Intro(Action nextAction) {
            this.nextAction = nextAction;
            Texture text = new Texture(Gdx.files.internal("battle/intro_frame6.png"));
            this.bgSprite = new Sprite(text, 0, 0, 16*10, 16*9);
        }

        public Intro(int duration, Action nextAction) {
            this(nextAction);
            this.duration = duration;
        }

        public String getCamera() {return "gui";}

        public int getLayer(){return this.layer;}

        @Override
        public void step(Game game) {
            this.bgSprite.draw(game.uiBatch);

            // draw a white bg for 18 frames
            this.duration--;

            if (this.duration <= 0) {
                game.insertAction(this.nextAction);
                game.actionStack.remove(this);
            }
        }
    }

    static class Outro extends Action {
        // 33 frames white
        // 1 frame where cursor is still white and can't move cursor (people aren't visible)
        // 1 frame people aren't visible (not implementing)

        MenuAction prevMenu;

        public int layer = 110;
        int duration = 34;

        Sprite bgSprite;

        public Outro(Action nextAction) {
            this(null);
            this.nextAction = nextAction;
        }

        public Outro(MenuAction prevMenu) {
            this.prevMenu = prevMenu;
            if (this.prevMenu != null) {
                this.prevMenu.drawArrowWhite = true;
            }

            Texture text = new Texture(Gdx.files.internal("battle/intro_frame6.png"));
            this.bgSprite = new Sprite(text, 0, 0, 16*10, 16*9);
        }
        public String getCamera() {return "gui";}

        public int getLayer(){return this.layer;}

        @Override
        public void step(Game game) {
            if (this.prevMenu != null) {
                this.prevMenu.step(game);
            }

            this.duration--;

            // last from no white bg
            if (this.duration > 0) {
                this.bgSprite.draw(game.uiBatch);
            }

            if (this.duration <= 0) {
                if (this.prevMenu != null) {
                    game.insertAction(this.prevMenu);
                    this.prevMenu.disabled = false;
                    this.prevMenu.drawArrowWhite = false;
                }
                game.insertAction(this.nextAction);
                game.actionStack.remove(this);
            }
        }
    }

    static class SelectedMenu extends MenuAction {
        Sprite arrow;
        Sprite textBoxTop;
        Sprite textBoxMiddle;
        Sprite textBoxBottom;
        Pokemon pokemon;
        public int layer = 106;
        Map<Integer, Vector2> getCoords = new HashMap<Integer, Vector2>();
        int curr;
        Vector2 newPos;
        Sprite helperSprite;
        ArrayList<String> words = new ArrayList<String>(); // menu items
        int textboxDelay = 0; // this is just extra detail. text box has 1 frame delay before appearing

        public SelectedMenu(MenuAction prevMenu, Pokemon pokemon) {
            this.prevMenu = prevMenu;
            this.pokemon = pokemon;
            Texture text = new Texture(Gdx.files.internal("battle/arrow_right1.png"));
            this.arrow = new Sprite(text, 0, 0, 5, 7);
            // text box background
            text = new Texture(Gdx.files.internal("pokemon_menu/selected_menu_top.png"));
            this.textBoxTop = new Sprite(text, 0,0, 71, 19);
            text = new Texture(Gdx.files.internal("pokemon_menu/selected_menu_middle.png"));
            this.textBoxMiddle = new Sprite(text, 0,0, 71, 16);
            text = new Texture(Gdx.files.internal("pokemon_menu/selected_menu_bottom.png"));
            this.textBoxBottom = new Sprite(text, 0,0, 71, 19);
        }

        // Maps menu selection to an action
        public Action getAction(Game game, String word, MenuAction prevMenu) {
            if (word.equals("STATS")) {
                Pokemon currPokemon = game.player.pokemon.get(DrawPokemonMenu.currIndex);
                return new DrawStatsScreen.Intro(
                       new DrawStatsScreen(game, currPokemon, prevMenu));
                
            }
            else if (word.equals("SWITCH")) {
                Pokemon currPokemon = game.player.pokemon.get(DrawPokemonMenu.currIndex);
                if (game.battle.drawAction == null) {
                    // TODO: this should check game.battle == null after refactor
                    // Player isn't in battle so swap pokemon ordering
                    return new SelectedMenu.Switch(prevMenu);
                }
                // Don't allow switch if currPokemon is trapped
                else if (game.player.currPokemon.currentStats.get("hp") > 0 && game.player.currPokemon.trappedBy != null) {
                    this.disabled = true;
                    game.insertAction(this.prevMenu);
                    return new PlaySound("error1",
                           new DisplayText(game, game.player.currPokemon.name.toUpperCase()+" is trapped!", null, null,
                           new SetField(this.prevMenu, "disabled", false,
                           null)));
                }
                // Don't allow switch to fainted pokemon or egg
                else if (currPokemon.currentStats.get("hp") <= 0 || currPokemon.isEgg) {
                    this.disabled = true;
                    game.insertAction(this.prevMenu);
                    return new PlaySound("error1",
                           new SetField(this.prevMenu, "disabled", false, null));
                }
                // Don't allow switch to Pokemon that is currently out
                else if (currPokemon == game.player.currPokemon) {
                    this.disabled = true;
                    game.insertAction(this.prevMenu);
                    return new PlaySound("error1",
                           new SetField(this.prevMenu, "disabled", false, null));
                }
                else {
                    if (game.type == Game.Type.CLIENT) {
                        game.client.sendTCP(new com.pkmngen.game.Network.DoBattleAction(game.player.network.id,
                                                                                        Battle.DoTurn.Type.SWITCH,
                                                                                        DrawPokemonMenu.currIndex));
                    }
                    return new SplitAction(new PlaySound("click1", null),
                           new Battle.DoTurn(game, Battle.DoTurn.Type.SWITCH,
                           new WaitFrames(game, 15,
                           new DrawBattleMenuNormal(game,
                           null))));
                }
            }
            // TODO: just use player.currHm
            if (game.player.isFlying) {
                this.disabled = true;
                game.insertAction(this.prevMenu);
                return new PlaySound("error1",
                       new SetField(this.prevMenu, "disabled", false,
                       null));
            }
            
            if (word.equals("DROP")) {
                Vector2 pos = game.player.facingPos();
                Tile currTile = game.map.tiles.get(pos);
                if (currTile == null ||
                    currTile.attrs.get("solid") ||
                    currTile.attrs.get("ledge") ||
                    game.map.pokemon.containsKey(pos)) {  // Can't place Pokemon on top of each other
                    this.disabled = true;
                    return new DisplayText(game, "Canì place here - something is in the way.", null, null,
                               new SetField(this.prevMenu, "disabled", false,
                               this.prevMenu));
                }
                if (game.player.pokemon.size() <= 1) {
                    this.disabled = true;
                    game.insertAction(this.prevMenu);
                    return new PlaySound("error1",
                           new DisplayText(game, "You need at least 1 POKéMON in your party.", null, null,
                           new SetField(this.prevMenu, "disabled", false,
                           null)));
                }
                if (currTile.routeBelongsTo != null && currTile.routeBelongsTo.isDungeon) {
                    this.disabled = true;
                    return new DisplayText(game, "Canì leave Pokemon in a dangerous area.", null, null,
                               new SetField(this.prevMenu, "disabled", false,
                               this.prevMenu));
                }
                Pokemon pokemon = game.player.pokemon.get(DrawPokemonMenu.currIndex);
                boolean hasOneHealthyPokemon = false;
                for (Pokemon otherPokemon : game.player.pokemon) {
                    if (pokemon == otherPokemon) {
                        continue;
                    }
                    if (otherPokemon.currentStats.get("hp") > 0 && !otherPokemon.isEgg) {
                        hasOneHealthyPokemon = true;
                        break;
                    }
                }
                if (!hasOneHealthyPokemon) {
                    this.disabled = true;
                    game.insertAction(this.prevMenu);
                    return new PlaySound("error1",
                           new DisplayText(game, "You need at least 1 healthy POKéMON in your party.", null, null,
                           new SetField(this.prevMenu, "disabled", false,
                           null)));
                }
                pokemon.position = pos;
                pokemon.mapTiles = game.map.tiles;
                // If player is dropping the pokemon using a Field Move,
                // just swap sprites back from the pokemon and remove
                // it's standing action.
                if (game.player.hmPokemon == pokemon) {
                    game.player.swapSprites(game.player.hmPokemon);
                    game.actionStack.remove(game.player.hmPokemon.standingAction);
                    game.player.hmPokemon = null;
                    game.player.currFieldMove = "";
                }
                return new SelectedMenu.ExitAfterActions(this.prevMenu,
                       new PlaySound(pokemon,
                       pokemon.new RemoveFromInventory()));
            }
            // Don't allow player to use same Field Move as previous
            // TODO: use game.player.currFieldMove for this
            if (game.player.hmPokemon == game.player.pokemon.get(DrawPokemonMenu.currIndex)) {
                if (word.equals(game.player.currFieldMove)) {
                    this.disabled = true;
                    game.insertAction(this.prevMenu);
                    return new PlaySound("error1",
                           new SetField(this.prevMenu, "disabled", false,
                           null));
                }
            }
            // If player is already using an hm, just swap sprites back from the pokemon
            // and remove it's standing action.
            if (game.player.hmPokemon != null) {
                if (!game.player.currFieldMove.equals("")) {
                    game.player.swapSprites(game.player.hmPokemon);
                    // TODO: this is currently just used to stop
                    //       player from planting or fertilizing
                    // No need to do this if pokemon is just following, though.
                    game.player.currPlanting = null;
                }
                game.actionStack.remove(game.player.hmPokemon.standingAction);
                game.player.hmPokemon = null;
                game.player.currFieldMove = "";
            }
            if (word.equals("FLY")) {
                if (game.map.tiles != game.map.overworldTiles) {
                    game.insertAction(this.prevMenu);
                    return new PlaySound("error1",
                           new DisplayText(game, "Canì do this here!", null, null,
                           new SetField(this.prevMenu, "disabled", false, null)));
                }
                // TODO: hack to make playerStanding work for now.
                PlayerStanding standingAction = null;
                for (Action action : game.actionStack) {
                    if (PlayerStanding.class.isInstance(action)) {
                        standingAction = (PlayerStanding)action;
                        break;
                    }
                }
//                game.player.standingAction = standingAction;
                game.actionStack.remove(standingAction);  // TODO: what if not?
//                game.cam.translate(0f, 16f);  // TODO: remove
                game.player.currFieldMove = "FLY";
                return new SelectedMenu.ExitAfterActions(this.prevMenu,
                       new PlaySound(pokemon,
                       // trick ExitAfterActions
                       new SplitAction(new WaitFrames(game, 40,
                                       game.player.new Flying(pokemon, true,
                                       null)),
                       null)));
                                         
            }

            else if (word.equals("DIG")) {
                Pokemon pokemon = game.player.pokemon.get(DrawPokemonMenu.currIndex);
                game.player.swapSprites(pokemon);
                game.insertAction(pokemon.new Follow(game.player));
                game.player.currFieldMove = "DIG";
                // Update which tiles currently building
                game.player.buildTiles = game.player.terrainTiles;
                while (game.player.buildTileIndex > 0 && game.player.buildTileIndex >= game.player.buildTiles.size()) {
                    game.player.buildTileIndex--;
                }
                game.player.currBuildTile = game.player.buildTiles.get(game.player.buildTileIndex);
                //
                return new SelectedMenu.ExitAfterActions(this.prevMenu,
                       new PlaySound(pokemon,
                       new DisplayText(game, pokemon.name.toUpperCase()+" used DIG! Press C and V to select terrain.", null, null,
                       null)));
            }
            
            // generate actions for HMs
            else if (word.equals("BUILD")) {
//                if (game.type == Game.Type.CLIENT) {
//                    game.client.sendTCP(new com.pkmngen.game.Network.UseHM(game.player.network.id,
//                                                                           DrawPokemonMenu.currIndex,
//                                                                           word));
//                }
                // text box '__ used BUILD!'
                // outro anim to overworld
                // then swap player with pokemon anim
                //  hmm where does player go if no space? how about pokemon is super-imposed
                //  or maybe player walks away if there is an available space, default down
                //  just replace player sprite with pkmn
                //  need overworld sprite
                // then move like normal, with following player
                //
                // TODO: can't return this as a new action, so just inserting here
                //
//                game.insertAction(this.prevMenu);  // keep drawing but don't enable
//                DrawPokemonMenu.lastIndex = prevMenu.currIndex;
                Pokemon pokemon = game.player.pokemon.get(DrawPokemonMenu.currIndex);
                game.player.swapSprites(pokemon);
                game.insertAction(pokemon.new Follow(game.player));
                game.player.currFieldMove = "BUILD";

                Vector2 pos = game.player.facingPos();
                Tile currTile = game.map.tiles.get(pos);
                if (game.map.tiles == game.map.overworldTiles) {
                    if (currTile != null && currTile.name.contains("desert")) {
                        game.player.buildTiles = game.player.desertBuildTiles;
                    }
                    else {
                        game.player.buildTiles = game.player.outdoorBuildTiles;
                    }
                }
                else {
                    game.player.buildTiles = game.player.indoorBuildTiles;
                }
                while (game.player.buildTileIndex > 0 && game.player.buildTileIndex >= game.player.buildTiles.size()) {
                    game.player.buildTileIndex--;
                }
                game.player.currBuildTile = game.player.buildTiles.get(game.player.buildTileIndex);
                
                
                //
                return new SelectedMenu.ExitAfterActions(this.prevMenu,
                       new PlaySound(pokemon,
                       new DisplayText(game, pokemon.name.toUpperCase()+" used BUILD! Press C and V to select tiles.", null, null,
                       null)));
            }
            else if (word.equals("CUT")) {
                // TODO: only send when cutting tile
//                if (game.type == Game.Type.CLIENT) {
//                    game.client.sendTCP(new com.pkmngen.game.Network.UseHM(game.player.network.id,
//                                                                           DrawPokemonMenu.currIndex,
//                                                                           word));
//                }
                Pokemon pokemon = game.player.pokemon.get(DrawPokemonMenu.currIndex);
                game.player.swapSprites(pokemon);
                game.insertAction(pokemon.new Follow(game.player));
                game.player.currFieldMove = "CUT";
                return new SelectedMenu.ExitAfterActions(this.prevMenu,
                       new PlaySound(pokemon,
                       new DisplayText(game, pokemon.name.toUpperCase()+" is using CUT!", null, null,
                       null)));
            }
            else if (word.equals("SMASH")) {
                Pokemon pokemon = game.player.pokemon.get(DrawPokemonMenu.currIndex);
                game.player.swapSprites(pokemon);
                game.insertAction(pokemon.new Follow(game.player));
                game.player.currFieldMove = "SMASH";
                return new SelectedMenu.ExitAfterActions(this.prevMenu,
                       new PlaySound(pokemon,
                       new DisplayText(game, pokemon.name.toUpperCase()+" is using ROCK SMASH!", null, null,
                       null)));
            }
            else if (word.equals("POWER")) {
                Pokemon pokemon = game.player.pokemon.get(DrawPokemonMenu.currIndex);
                game.player.swapSprites(pokemon);
                game.insertAction(pokemon.new Follow(game.player));
                game.player.currFieldMove = "POWER";
                return new SelectedMenu.ExitAfterActions(this.prevMenu,
                       new PlaySound(pokemon,
                       new DisplayText(game, pokemon.name.toUpperCase()+" is using POWER! Power machinery by pressing Z.", null, null,
                       null)));
            }
            else if (word.equals("HEADBUTT")) {
                if (game.type == Game.Type.CLIENT) {
                    game.client.sendTCP(new com.pkmngen.game.Network.UseHM(game.player.network.id,
                                                                           DrawPokemonMenu.currIndex,
                                                                           word));
                }
                Pokemon pokemon = game.player.pokemon.get(DrawPokemonMenu.currIndex);
                game.player.swapSprites(pokemon);
                game.insertAction(pokemon.new Follow(game.player));
                game.player.currFieldMove = "HEADBUTT";
                return new SelectedMenu.ExitAfterActions(this.prevMenu,
                       new PlaySound(pokemon,
                       new DisplayText(game, pokemon.name.toUpperCase()+" is using HEADBUTT!", null, null,
                       null)));
            }
            else if (word.equals("RIDE")) {
                if (game.type == Game.Type.CLIENT) {
                    game.client.sendTCP(new com.pkmngen.game.Network.UseHM(game.player.network.id,
                                                                           DrawPokemonMenu.currIndex,
                                                                           word));
                }
                Pokemon pokemon = game.player.pokemon.get(DrawPokemonMenu.currIndex);
                game.player.swapSprites(pokemon);
                game.player.currFieldMove = "RIDE";
                return new SelectedMenu.ExitAfterActions(this.prevMenu,
                       new PlaySound(pokemon,
                       new DisplayText(game, pokemon.name.toUpperCase()+" is using RIDE!", null, null,
                       null)));
            }
            else if (word.equals("ATTACK")) {
                Pokemon pokemon = game.player.pokemon.get(DrawPokemonMenu.currIndex);
                game.player.swapSprites(pokemon);
                game.insertAction(pokemon.new Follow(game.player));
                game.player.currFieldMove = "ATTACK";
                return new SelectedMenu.ExitAfterActions(this.prevMenu,
                       new PlaySound(pokemon,
                       new DisplayText(game, pokemon.name.toUpperCase()+" is using ATTACK!", null, null,
                       null)));
            }
            // TODO: how to make pokemon stop following
            // TODO: player will be able to use BUILD etc, which will
            //  mess with the following pokemon
            else if (word.equals("FOLLOW")) {
                Pokemon pokemon = game.player.pokemon.get(DrawPokemonMenu.currIndex);
                game.insertAction(pokemon.new Follow(game.player));
                game.player.hmPokemon = pokemon;
                return new SelectedMenu.ExitAfterActions(this.prevMenu,
                       new PlaySound(pokemon,
                       new DisplayText(game, pokemon.name.toUpperCase()+" is following you around!", null, null,
                       null)));
            }
            return null;
        }

        public String getCamera() {return "gui";}

        public int getLayer(){return this.layer;}

        @Override
        public void firstStep(Game game) {
//            int numHms = -1;
            ArrayList<String> fieldMoves = new ArrayList<String>(this.pokemon.hms);
            fieldMoves.remove("FLASH");  // TODO: just have FLASH pkmn follow for now
            fieldMoves.add("FOLLOW");
//            if (fieldMoves.isEmpty()) {  // TODO: potentially remove
//                fieldMoves.add("FOLLOW");
//            }
//            if (game.battle.drawAction == null) {
//                numHms = fieldMoves.size();
//            }
//            int coordsIndex = 0;
            if (game.battle.drawAction == null) {
//                this.getCoords.put(coordsIndex++, new Vector2(97, 40 +16*numHms +16));
//                this.getCoords.put(coordsIndex++, new Vector2(97, 40-16 +16*numHms +16));
//                this.getCoords.put(coordsIndex++, new Vector2(97, 40-32 +16*numHms +16));
                // Add HMs from selected pokemon
                for (String hm : fieldMoves) {
                    this.words.add(hm);
//                    this.getCoords.put(coordsIndex, new Vector2(97, 40 -16*coordsIndex +16*numHms));
//                    coordsIndex++;
                }
                this.words.add("STATS");
//                this.getCoords.put(coordsIndex++, new Vector2(97, 40-32 +16*numHms));
            }
            else {
//                this.getCoords.put(coordsIndex++, new Vector2(97, 40 +16*numHms));
//                this.getCoords.put(coordsIndex++, new Vector2(97, 40-16 +16*numHms));
//                this.getCoords.put(coordsIndex++, new Vector2(97, 40-32 +16*numHms));
            }
            this.words.add("SWITCH");
            if (game.battle.drawAction == null) {
                this.words.add("DROP");  // ideas: OPEN, FREE, DROP,
            }
            this.words.add("CANCEL");
            
            for (int i=0; i < this.words.size(); i++) {
                this.getCoords.put(i, new Vector2(97, -8 +16*(this.words.size()-i)));
            }

            this.newPos =  this.getCoords.get(0);
            this.arrow.setPosition(newPos.x, newPos.y);
            this.curr = 0;
        }

        @Override
        public void step(Game game) {
            // if there is a previous menu, step through it to display text
            if (prevMenu != null) {
                prevMenu.step(game);
            }

            // white arrow only for one frame, then box appears
            if (this.textboxDelay < 1) {
                this.textboxDelay+=1;
                return;
            }

            // check user input
            // 'tl' = top left, etc.
            // modify position by modifying curr to tl, tr, bl or br
            if (InputProcessor.upJustPressed) {
                if (curr > 0) {
                    curr -= 1;
                    newPos = this.getCoords.get(curr);
                }
            }
            else if (InputProcessor.downJustPressed) {
                if (curr < this.words.size()-1) {
                    curr += 1;
                    newPos = this.getCoords.get(curr);
                }
            }

            // draw the menu items (stats, switch, cancel)
            Sprite letterSprite;
            for (int i=0; i < this.words.size(); i++) {
                // Draw appropriate part of textBox
                if (i == 0) {
                    game.uiBatch.draw(this.textBoxTop, 89, 35 +16*(this.words.size()-3));
                }
                else if (i == this.words.size()-1) {
                    game.uiBatch.draw(this.textBoxBottom, 89, 0);
                }
                else {
                    game.uiBatch.draw(this.textBoxMiddle, 89, 19 +16*(this.words.size()-i-2));
                }

                String word = this.words.get(i);
                for (int j=0; j < word.length(); j++) {
                    char letter = word.charAt(j);
                    // convert string to text
                    letterSprite = game.textDict.get(letter);
                    game.uiBatch.draw(letterSprite, 104 +8*j, 40 -16*(i-this.words.size()+3));
                    // todo: need to modify to shift words up if there are hms
                }
            }

            // draw arrow sprite
//            this.arrow.setPosition(newPos.x, newPos.y);
//            this.arrow.draw(game.uiBatch);
            game.uiBatch.draw(this.arrow, newPos.x, newPos.y);

            if (InputProcessor.aJustPressed) {
                // get action for this item
                // perform the action
                 // actually this will probably be performed in 'getAction'
                String word = this.words.get(this.curr);
                if ("CANCEL".equals(word)) {
                    DrawPokemonMenu.avatarAnimCounter = 24;
                    game.insertAction(new PlaySound("click1", null));
                    game.actionStack.remove(this);
                    if (game.battle.drawAction == null) {
                        game.insertAction(new DrawPokemonMenu.Outro(
                                          this.prevMenu.prevMenu));
                    }
                    else {
                        this.prevMenu.disabled = false;
                        game.insertAction(this.prevMenu);
                    }
                    return;
                }
                else {
                    Action action = this.getAction(game, word, this.prevMenu);
                    game.actionStack.remove(this);
                    game.insertAction(action);
                    return;
                }
            }
            // player presses b, ie wants to go back
            else if (InputProcessor.bJustPressed) {
                game.insertAction(new PlaySound("click1", null));
                // reset avatar anim
                DrawPokemonMenu.avatarAnimCounter = 24;
                game.actionStack.remove(this);
                game.insertAction(new SelectedMenu.Outro(this.prevMenu));
                return;
            }
        }

        static class ExitAfterActions extends MenuAction {
            public int layer = 107;
            boolean firstStep = true;

            public ExitAfterActions(MenuAction prevMenu, Action nextAction) {
                this.prevMenu = prevMenu; // previously visiting menu
                this.nextAction = nextAction;
            }

            public String getCamera() {return "gui";}

            public int getLayer(){return this.layer;}

            @Override
            public void step(Game game) {
                if (this.firstStep) {
                    game.insertAction(this.nextAction);
                    this.firstStep = false;
                }
                // if there is a previous menu, step through it to display text
                if (prevMenu != null) {
                    prevMenu.step(game);
                }
                // if there are no more actions in the nextActions chain,
                //  then exit DrawPokemonMenu
                Action action = this.nextAction;
                while (action != null) {
                    if (game.actionStack.contains(action)) {
                        break;
                    }
                    action = action.nextAction;
                }
                if (action == null) {
                    DrawPokemonMenu.lastIndex = prevMenu.currIndex;
                    DrawPokemonMenu.lastIndex = prevMenu.currIndex;
                    game.actionStack.remove(this);
                    game.insertAction(new DrawPokemonMenu.Outro(null));
//                    game.playerCanMove = true;
                    game.insertAction(new WaitFrames(game, 30,
                                      new PlayerCanMove(game,
                                      null)));
                    return;
                }
            }
        }

        static class Outro extends Action {
            // 9 frames no arrow no bg (appears on 10th frame)
            //'Choose pokemon' text disappears on 3th frame, appears on 6th frame

            MenuAction prevMenu;

            public int layer = 110;
            int duration = 9;

            public Outro(MenuAction prevMenu) {
                this.prevMenu = prevMenu;
            }

            public String getCamera() {return "gui";}

            public int getLayer(){return this.layer;}

            @Override
            public void step(Game game) {
                this.prevMenu.drawArrowWhite = false;

                if (prevMenu != null) {
                    prevMenu.step(game);
                }

                this.duration--;

                if (this.duration == 6) {
                    DrawPokemonMenu.drawChoosePokemonText = false;
                }
                else if (this.duration == 3) {
                    DrawPokemonMenu.drawChoosePokemonText = true;
                }
                else if (this.duration <= 0) {
                    game.insertAction(this.prevMenu);
                    this.prevMenu.disabled = false;
                    game.actionStack.remove(this);
                }
            }
        }

        static class Switch extends MenuAction {
            // 1 frame menu disappears
            // 1 frame 'Choose a pokemon' text disappears
            // 2 frames nothing
            // 4 frames 'Move where?'
            // 1 frame arrow turns black (white arrow is drawn under)
            //  - assume allowed to move cursor here

            Sprite arrow;

            public int layer = 106;
            Map<Integer, Vector2> arrowCoords;

            int curr;

            int startPosition;
            Vector2 newPos;
            //            Sprite helperSprite;
            int timer = 0; // used for various intro anim timings
            public Switch(MenuAction prevMenu) {
                this.prevMenu = prevMenu; // previously visiting menu

                Texture text = new Texture(Gdx.files.internal("battle/arrow_right1.png"));
                this.arrow = new Sprite(text, 0, 0, 5, 7);

                this.arrowCoords = new HashMap<Integer, Vector2>();
                for (int i=0; i < 6; i++) {
                    this.arrowCoords.put(i, new Vector2(1, 128 - 16*i));
                }

                this.startPosition = this.curr = DrawPokemonMenu.currIndex;
                this.newPos =  this.arrowCoords.get(this.curr);
                this.arrow.setPosition(newPos.x, newPos.y);

                // helper sprite
//                text = new Texture(Gdx.files.internal("pokemon_menu/helper3.png"));
//                this.helperSprite = new Sprite(text, 0,0, 16*10, 16*9);
            }
            public String getCamera() {return "gui";}

            public int getLayer(){return this.layer;}

            @Override
            public void step(Game game) {
                // if there is a previous menu, step through it to display text
                if (prevMenu != null) {
                    prevMenu.step(game);
                }

                // Draw arrow sprite
                this.arrow.setPosition(newPos.x, newPos.y);
                game.uiBatch.draw(this.arrow, this.arrow.getX(), this.arrow.getY());

                if (this.disabled) {
                    return;
                }

                if (this.timer < 10) {
                    this.timer++;
                }

                // decrement avatar anim counter
                DrawPokemonMenu.avatarAnimCounter--;
                if (DrawPokemonMenu.avatarAnimCounter <= 0) {
                    DrawPokemonMenu.avatarAnimCounter = 11;
                }

                if (this.timer == 2) {
                    DrawPokemonMenu.drawChoosePokemonText = false;
                }
                else if (this.timer > 4) {
                    char[] textArray = "Move POKéMON".toCharArray();
                    Sprite letterSprite;
                    for (int j=0; j < textArray.length; j++) {
                        letterSprite = game.textDict.get(textArray[j]);
                        game.uiBatch.draw(letterSprite, 8 +8*j, 24);
                    }
                    textArray = "where?".toCharArray();
                    for (int j=0; j < textArray.length; j++) {
                        letterSprite = game.textDict.get(textArray[j]);
                        game.uiBatch.draw(letterSprite, 8 +8*j, 8);
                    }
                }
                if (this.timer > 8) {
                    if (InputProcessor.upJustPressed) {
                        if (this.curr > 0) {
                            this.curr -= 1;
                            DrawPokemonMenu.currIndex = this.curr; // DrawPokemonMenu animates the avatars
                            newPos = this.arrowCoords.get(this.curr);
                        }
                    }
                    else if (InputProcessor.downJustPressed) {
                        if (this.curr < game.player.pokemon.size()-1) {
                            this.curr += 1;
                            DrawPokemonMenu.currIndex = this.curr; // DrawPokemonMenu animates the avatars
                            newPos = this.arrowCoords.get(this.curr);
                        }
                    }

                    // debug
//                    helperSprite.draw(game.floatingBatch);

                    if (InputProcessor.aJustPressed) {
                        // switch
                        // TODO: anim?
                        // TODO: probably won't refresh
                        Pokemon movePokemon = game.player.pokemon.get(this.startPosition);
                        Pokemon movePokemon2 = game.player.pokemon.get(this.curr);
                        game.player.pokemon.remove(this.startPosition);
                        game.player.pokemon.add(this.startPosition, movePokemon2);
                        game.player.pokemon.remove(this.curr);
                        game.player.pokemon.add(this.curr, movePokemon);

                        // replace currPokemon if applicable
                        if (this.startPosition == 0) {
                            game.player.currPokemon = movePokemon2;
                        }
                        if (this.curr == 0) {
                            game.player.currPokemon = movePokemon;
                        }

                        this.disabled = true;
                        game.insertAction(new PlaySound("click1", null));
                        // reset avatar anim
                        DrawPokemonMenu.avatarAnimCounter = 24;
                        game.actionStack.remove(this);
                        game.insertAction(new Switch.Outro(this));
                        if (game.type == Game.Type.CLIENT) {
                            game.client.sendTCP(new com.pkmngen.game.Network.UseHM(game.player.network.id,
                                                                                   this.startPosition,
                                                                                   "SWITCH",
                                                                                   this.curr));
                        }
                        return;
                    }
                    // player presses b, ie wants to go back
                    else if (InputProcessor.bJustPressed) {
                        this.disabled = true;
                        game.insertAction(new PlaySound("click1", null));
                        // reset avatar anim
                        DrawPokemonMenu.avatarAnimCounter = 24;
                        game.actionStack.remove(this);
                        game.insertAction(new Switch.Outro(this));
                        return;
                    }
                }
            }

            static class Outro extends MenuAction {
                // 1 frame 'move where' disappears (black and white arrow still visible)
                // 1 frame black and white cursor disappear
                // 1 nothing
                // 4 'choose a pokemon' appears
                // 1 black cursor appears, assume can move cursor at this point
                // 1 frame avatar anim at 12 here (1 extra frame for avatar anim)
                public int layer = 110;
                int timer = 0; // timer counting up
                public Outro(MenuAction prevMenu) {
                    this.prevMenu = prevMenu;
                }
                public String getCamera() {return "gui";}

                public int getLayer(){return this.layer;}

                @Override
                public void step(Game game) {
                    if (prevMenu != null) {
                        prevMenu.step(game);
                    }

                    this.timer++;
                    if (this.timer == 1) {
                        this.prevMenu = this.prevMenu.prevMenu;
                        this.prevMenu.drawArrowWhite = false;
                    }
                    else if (this.timer == 3) {
                        DrawPokemonMenu.drawChoosePokemonText = true;
                    }
                    else if (this.timer >= 7) {
                        DrawPokemonMenu.avatarAnimCounter = 25; // one extra avatar frame
                        game.actionStack.remove(this);
                        game.insertAction(this.prevMenu);
                        this.prevMenu.disabled = false;
                    }
                }
            }
        }
    }
}

/**
 * Self-explanatory.
 */
class DrawUseTossMenu extends MenuAction {
    Sprite arrow;
    Sprite textBox;
    public int layer = 106;
    Map<Integer, Vector2> getCoords = new HashMap<Integer, Vector2>();
    int curr;
    Vector2 newPos;
    Sprite helperSprite;
    int cursorDelay; // this is just extra detail. cursor has 2 frame delay before showing in R/B
    String itemName;
    MenuAction prevMenu;
    ArrayList<String> words = new ArrayList<String>();

    // constructor for when this was called by the item menu
     // probably will create separate constructor for other cases
    public DrawUseTossMenu(Game game, MenuAction prevMenu, String itemName) {
        this.prevMenu = prevMenu; // previously visiting menu
        this.itemName = itemName; // which item was selected from previous menu
        this.cursorDelay = 0;

        Texture text = new Texture(Gdx.files.internal("battle/arrow_right1.png"));
        this.arrow = new Sprite(text, 0, 0, 5, 7);

        // text box bg
        text = new Texture(Gdx.files.internal("attack_menu/usetoss_menu2.png"));
        this.textBox = new Sprite(text, 0,0, 16*10, 16*9);

        this.getCoords.put(0, new Vector2(113, 48));
        this.getCoords.put(1, new Vector2(113, 48-16));

        // this.newPos =  new Vector2(32, 79); // post scaling change
        this.newPos =  new Vector2(113, 48);
        this.arrow.setPosition(newPos.x, newPos.y);
        this.curr = 0;
        this.words.add("USE");
        this.words.add("DROP");
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        // if there is a previous menu, step through it to display text
        if (prevMenu != null) {
            prevMenu.step(game);
        }
        // Draw text box
//        this.textBox.draw(game.uiBatch);
        game.uiBatch.draw(this.textBox, this.textBox.getX(), this.textBox.getY());

        // Draw use/drop
        for (int i=0; i < this.words.size(); i++) {
            String word = this.words.get(i);
            for (int j=0; j < word.length(); j++) {
                // Convert character to sprite and draw
                char letter = word.charAt(j);
                game.uiBatch.draw(game.textDict.get(letter), 120 +8*j, 48 -16*i);
            }
        }

        if (this.disabled){
            return;
        }

        // check user input
         //'tl' = top left, etc.
         // modify position by modifying curr to tl, tr, bl or br
        if (InputProcessor.upJustPressed) {
            if (this.curr > 0) {
                this.curr -= 1;
                newPos = getCoords.get(this.curr);
            }

        }
        else if (InputProcessor.downJustPressed) {
            if (this.curr < 1) {
                this.curr += 1;
                newPos = getCoords.get(this.curr);
            }
        }

        // draw arrow
        if (this.cursorDelay >= 2) {
//            this.arrow.setPosition(newPos.x, newPos.y);
//            this.arrow.draw(game.uiBatch);
            game.uiBatch.draw(this.arrow, newPos.x, newPos.y);
        }
        else {
            this.cursorDelay+=1;
        }

        // If player presses A, use the item
        if (InputProcessor.aJustPressed) {
            game.actionStack.remove(this);
            if (this.curr == 0) {
                // Perform the action based on which item selected
                this.useItem(game, this.itemName);
                return;
            }
            else if (game.battle.drawAction == null) {
                // Drop this item on the tile in front of the player.
                Vector2 pos = game.player.position.cpy();
                if (game.player.dirFacing.equals("up")) {
                    pos.add(0, 16);
                }
                else if (game.player.dirFacing.equals("down")) {
                    pos.add(0, -16);
                }
                else if (game.player.dirFacing.equals("right")) {
                    pos.add(16, 0);
                }
                else if (game.player.dirFacing.equals("left")) {
                    pos.add(-16, 0);
                }
                if (game.map.tiles.get(pos).attrs.get("solid")) {
                    this.disabled = true;
                    game.insertAction(new DisplayText(game, "Not enough room!", null, false, true,
                                      new SetField(this.prevMenu, "disabled", false,
                                      this.prevMenu)));
                    return;
                }
                this.disabled = true;
                game.insertAction(new DrawUseTossMenu.SelectAmount(this.itemName, pos, this));
                return;
            }
            else {
                game.insertAction(this.prevMenu);
                game.insertAction(new PlaySound("error1",
                                  new SetField(this.prevMenu, "disabled", false,
                                  null)));
                return;
            }
        }
        // player presses b, ie wants to go back
        else if (InputProcessor.bJustPressed) {
            this.prevMenu.disabled = false;
            game.actionStack.remove(this);
            game.insertAction(this.prevMenu);
            return;
        }

    }

    public void useItem(Game game, String itemName) {
        itemName = itemName.toLowerCase();
        // TODO: demo code, possibly remove.
//        if (itemName.toLowerCase().equals("ultra ball")) {
//            this.prevMenu.prevMenu.disabled = false;  // Menu won't get drawn until catch anim is over
//            // Calculate if pokemon was caught
//            Action catchAction = Battle_Actions.calcIfCaught(game, this.prevMenu.prevMenu);
//            // Display text, throw animation, catch or not
//            String textString = game.player.name+" used "+itemName.toUpperCase()+"!";
//            game.insertAction(new DisplayText(game, textString, null, catchAction,
//                              new ThrowPokeball(game,
//                              catchAction)));
//
//        }
        if (game.battle.drawAction == null) {
            if (game.player.isFlying) {
                game.insertAction(this.prevMenu);
                game.insertAction(new PlaySound("error1",
                                  new SetField(this.prevMenu, "disabled", false,
                                  null)));
                return;
            }
            if (itemName.equals("sleeping bag")) {
                // Can't use this while using a non-FOLLOW field move
                if (!game.player.currFieldMove.equals("")) {
                    this.disabled = true;
                    game.insertAction(this.prevMenu);
                    game.insertAction(new PlaySound("error1",
                                      new SetField(this.prevMenu, "disabled", false,
                                      null)));
                    return;
                }
                if (game.map.tiles.get(game.player.position.cpy().add(16,0)) == null ||
                    game.map.tiles.get(game.player.position.cpy().add(16,0)).attrs.get("solid") ||
                    game.map.tiles.get(game.player.position.cpy().add(16,0)).attrs.get("ledge")) {
                    this.disabled = true;
                    game.actionStack.remove(this);
                    game.insertAction(new DisplayText(game, "Not enough room!", null, false, true,
                                      new SetField(this.prevMenu, "disabled", false,
                                      this.prevMenu)));
                    return;
                }
                Tile currTile = game.map.tiles.get(game.player.position.cpy().add(0,0));
                // Don't allow sleeping bag in dungeons
                if (currTile.routeBelongsTo != null && currTile.routeBelongsTo.isDungeon) {
                    this.disabled = true;
                    game.actionStack.remove(this);
                    game.insertAction(new DisplayText(game, "Canì use this in a dangerous area!", null, false, true,
                                      new SetField(this.prevMenu, "disabled", false,
                                      this.prevMenu)));
                    return;
                }
                if (currTile.name.contains("stairs") ||
                    currTile.name.contains("mountain") ||
                    currTile.name.contains("snow") ||
                    currTile.name.contains("desert")) {
                    this.disabled = true;
                    game.actionStack.remove(this);
                    game.insertAction(new DisplayText(game, "Canì sleep on harsh terrain!", null, false, true,
                                      new SetField(this.prevMenu, "disabled", false,
                                      this.prevMenu)));
                    return;
                }
                if (currTile.attrs.get("grass") || game.player.nearAggroPokemon) {
                    this.disabled = true;
                    game.actionStack.remove(this);
                    // Canì use this while in tall grass!
                    game.insertAction(new DisplayText(game, "Canì use this while Pokémon are nearby!", null, false, true,
                                      new SetField(this.prevMenu, "disabled", false,
                                      this.prevMenu)));
                    return;
                }
                if (game.type == Game.Type.CLIENT) {
                    game.client.sendTCP(new com.pkmngen.game.Network.Sleep(game.player.network.id, true));
                }
                // Save this spot as next place to go to if player blacks out
//                game.player.spawnLoc.set(game.player.position);  // TODO: remove
                game.player.spawnLoc = game.player.position.cpy();
                game.player.spawnIndex = -1;
                if (game.map.tiles != game.map.overworldTiles) {
                    game.player.spawnIndex = game.map.interiorTilesIndex;
                }
                game.playerCanMove = true;
                game.player.acceptInput = false;
                game.player.dirFacing = "right";
                game.player.sleepingBagSprite.setPosition(game.player.position.x, game.player.position.y);
                game.insertAction(new PlayerMoving(game, game.player, false,
                                  new SetField(game.player, "dirFacing", "left",
                                  new SetField(game.player, "currSprite", game.player.standingSprites.get("left"),
                                  new WaitFrames(game, 24,
                                  new SetField(game.player, "drawSleepingBag", true,
                                  new WaitFrames(game, 24,
                                  new PlayerMoving(game, game.player, true,
                                  new SetField(game.player, "isSleeping", true,
                                  null)))))))));
                return;
            }
            // TODO: test
            if (itemName.equals("escape rope")) {
                // Can't use this while using a non-FOLLOW field move
                if (!game.player.currFieldMove.equals("")) {
                    this.disabled = true;
                    game.insertAction(this.prevMenu);
                    game.insertAction(new PlaySound("error1",
                                      new SetField(this.prevMenu, "disabled", false,
                                      null)));
                    return;
                }
                game.playerCanMove = false;
                // Find nearest edge tile
                Vector2 nearest = null;
                int dst2 = 0;
                Vector2[] positions = new Vector2[]{new Vector2(-16, 0), new Vector2(0, 16),
                                                    new Vector2(16, 0), new Vector2(0, -16)};
                for (Vector2 pos : game.map.edges) {
//                for (Tile tile : game.map.overworldTiles) {
                    Tile tile = game.map.overworldTiles.get(pos);
                    // For some reason there are a bunch of non-sand2 tiles in this.
                    if (!tile.name.equals("sand3")) {
                        continue;
                    }
                    if (tile.attrs.get("solid")) {
                        continue;
                    }
                    boolean found = false;
                    for (Vector2 position : positions) {
                        Tile otherTile = game.map.overworldTiles.get(pos.cpy().add(position));
                        if (otherTile == null) {
                            continue;
                        }
                        if (otherTile.name.equals("water2")) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        continue;
                    }
                    int dist = (int)game.player.position.dst2(pos);
                    if (nearest == null || (dist > 1024 && dist < dst2)) {
                        dst2 = dist;
                        nearest = pos;
                    }
                }
                System.out.println(nearest);
                System.out.println(dst2);
                // TODO: edgeTile route is always null?
//                Tile edgeTile = game.map.tiles.get(nearest);
//                System.out.println(edgeTile.routeBelongsTo);
//                Route route = game.map.tiles.get(game.player.position).routeBelongsTo;
//                if (edgeTile.route != null) {
//                    
//                }
                game.insertAction(new DisplayText(game, "Return to the shore?", null, true, false,
                                  new DrawYesNoMenu(null,
                                      new DisplayText.Clear(game,
                                      new WaitFrames(game, 3,
                                      new FadeMusic(game.currMusic, -0.0125f,
                                      new WaitFrames(game, 60,
                                      new SplitAction(
                                          new WaitFrames(game, 6*3,
                                          new Game.SetCamPos(nearest.cpy().add(16, 0),
                                          new SetField(game.player, "position", nearest,
                                          null))),
//                                      new EnterBuilding(game, "exit", //game.map.overworldTiles,
                                      new EscapeRope(
                                      new SetField(game.map, "currRoute", new Route("", 2),
                                      new SetField(game.map, "interiorTilesIndex", 100,
                                      new WaitFrames(game, 60,
                                      new SetField(game.musicController, "resumeOverworldMusic", true,
                                      new SetField(game, "playerCanMove", true,
                                      null))))))))))),
                                  new DisplayText.Clear(game,
                                  new WaitFrames(game, 3,
                                  new SetField(game, "playerCanMove", true,
                                  null))))));
                return;
            }
            if (itemName.contains("repel")) {
                // Deduct from inventory
                game.player.itemsDict.put(itemName, game.player.itemsDict.get(itemName)-1);
                if (game.player.itemsDict.get(itemName) <= 0) {
                    game.player.itemsDict.remove(itemName);
                }
                game.player.repelCounter = 100;
                if (itemName.contains("max")) {
                    game.player.repelCounter = 250;
                }
                game.insertAction(this.prevMenu);
                game.insertAction(new DisplayText(game, "Applied "+itemName.toUpperCase()+"!", "fanfare1.ogg", null,
                                  new SetField(this.prevMenu, "disabled", false,
                                  null)));
                return;
            }
            if (itemName.contains("apricorn") || itemName.equals("manure") || itemName.equals("miracle seed")) {
                // TODO: if using field move, then swap the field move out
                game.insertAction(this.prevMenu);
                game.player.currPlanting = itemName;
                // TODO: migrate to use currFieldMove
                // If player is using an hm, they aren't anymore
                if (game.player.hmPokemon != null) {
                    // Only remove hmPokemon if it's not following
                    // TODO: use currFieldMove
                    if (!game.player.currFieldMove.equals("")) {
                        game.player.swapSprites(game.player.hmPokemon);
                        game.actionStack.remove(game.player.hmPokemon.standingAction);
                        game.player.hmPokemon = null;
                        game.player.currFieldMove = "";
                    }
                }
                String text = "Press Z to plant seeds.";
                if (itemName.equals("manure")) {
                    text = "Press Z to fertilize saplings.";
                }
                game.insertAction(new DisplayText(game, text, null, false, true,
                                  new WaitFrames(game, 10,
                                  new RemoveAction(this.prevMenu,  // tried to use CallMethod but remove() isn't visible
                                  new SetField(game, "playerCanMove", true,
                                  null)))));

                // TODO: this code would plant the seed in front of the player
                // now it sets player.currFieldMove to the item
//                // Perform this action on the tile in from of the player.
//                Vector2 pos = game.player.position.cpy();
//                if (game.player.dirFacing.equals("up")) {
//                    pos.add(0, 16);
//                }
//                else if (game.player.dirFacing.equals("down")) {
//                    pos.add(0, -16);
//                }
//                else if (game.player.dirFacing.equals("right")) {
//                    pos.add(16, 0);
//                }
//                else if (game.player.dirFacing.equals("left")) {
//                    pos.add(-16, 0);
//                }
//                if (!game.map.tiles.get(pos).name.equals("green1") ||
//                    game.map.tiles.get(pos).nameUpper.contains("tree") ||
//                    game.map.tiles.get(pos).attrs.get("solid")) {
//                    this.disabled = true;
//                    game.actionStack.remove(this);
//                    game.insertAction(new DisplayText(game, "Seeds must be planted in good soil!", null, false, true,  //Canì plant here!
//                                      new SetField(this.prevMenu, "disabled", false,
//                                      this.prevMenu)));
//                    return;
//                }
//                game.playerCanMove = false;
//                game.insertAction(new WaitFrames(game, 10,
//                                  new SplitAction(new PlantTree(pos, null),
//                                  new PlaySound("seed1", //new SplitAction(null),
//                                  new WaitFrames(game, 4,
//                                  new PlaySound("ledge2",
//                                  new WaitFrames(game, 10,
//                                  new SetField(game, "playerCanMove", true,
//                                  null))))))));
//                // Deduct from inventory
//                game.player.itemsDict.put(itemName, game.player.itemsDict.get(itemName)-1);
//                if (game.player.itemsDict.get(itemName) <= 0) {
//                    game.player.itemsDict.remove(itemName);
//                }
//                // Tell server.
//                if (game.type == Game.Type.CLIENT) {
//                    game.client.sendTCP(new Network.UseItem(game.player.network.id, itemName,
//                                                            game.player.dirFacing));
//                }
                return;
            }
            // TODO: this code would place manure 'one at a time' without showing
            //       a build tile in front of the player
            // Probably stash this in unused_code.java
//            if (itemName.contains("manure")) {
//                // Perform this action on the tile in front of the player.
//                Vector2 pos = game.player.position.cpy();
//                if (game.player.dirFacing.equals("up")) {
//                    pos.add(0, 16);
//                }
//                else if (game.player.dirFacing.equals("down")) {
//                    pos.add(0, -16);
//                }
//                else if (game.player.dirFacing.equals("right")) {
//                    pos.add(16, 0);
//                }
//                else if (game.player.dirFacing.equals("left")) {
//                    pos.add(-16, 0);
//                }
//                Tile tile = game.map.tiles.get(pos);
//                if (tile.nameUpper.contains("fertilized")) {
//                    this.disabled = true;
//                    game.actionStack.remove(this);
//                    game.insertAction(new DisplayText(game, "Already fertilized!", null, false, true, 
//                                      new SetField(this.prevMenu, "disabled", false,
//                                      this.prevMenu)));
//                    return;
//                }
//                if (!tile.nameUpper.contains("tree_planted")) {
//                    this.disabled = true;
//                    game.actionStack.remove(this);
//                    game.insertAction(new DisplayText(game, "Nothing here to fertilize!", null, false, true, 
//                                      new SetField(this.prevMenu, "disabled", false,
//                                      this.prevMenu)));
//                    return;
//                }
//                tile.nameUpper = tile.nameUpper+"_fertilized";
//                // Deduct from inventory
//                game.player.itemsDict.put(itemName, game.player.itemsDict.get(itemName)-1);
//                if (game.player.itemsDict.get(itemName) <= 0) {
//                    game.player.itemsDict.remove(itemName);
//                }
//                // Change lower appearance
//                game.map.tiles.put(tile.position, new Tile("mountain1", tile.nameUpper, tile.position.cpy(), true, tile.routeBelongsTo));
//                this.disabled = true;
//                game.actionStack.remove(this);
//                game.insertAction(new DisplayText(game, "The manure fertilized the plant!", "fanfare1.ogg", false, true, 
//                                  new SetField(this.prevMenu, "disabled", false,
//                                  this.prevMenu)));
//                // TODO: Tell server (?)
//                return;
//            }
            if (itemName.equals("moomoo milk") || itemName.equals("berry juice") || itemName.equals("rare candy")) {
                this.disabled = true;
                game.insertAction(new DrawPokemonMenu.Intro(
                                  new DrawPokemonMenu(game, itemName,
                                  this.prevMenu)));
                return;
            }
            game.insertAction(this.prevMenu);
            game.insertAction(new PlaySound("error1",
                              new SetField(this.prevMenu, "disabled", false,
                              null)));
            return;
        }
        // TODO: enable this code to prevent pokeball from being thrown if not enough room
        // currently sending pokemon to last safe spot in case where pokemon doesn't fit.
//        if (itemName.contains("ball") && game.player.pokemon.size() >= 6) {
//            game.insertAction(this.prevMenu);
//            game.insertAction(new DisplayText(game, "Not enough room in your party!", null, false, true,
//                              new SetField(this.prevMenu, "disabled", false,
//                              null)));
//            return;
//        }
        // If this item can't be used in battle, player error noise.
        if (!itemName.contains("ball") &&
            !itemName.contains("berry") &&
            !itemName.equals("moomoo milk") &&
            !itemName.equals("silph scope")) {  // TODO: more items
            game.insertAction(this.prevMenu);
            game.insertAction(new PlaySound("error1",
                              new SetField(this.prevMenu, "disabled", false,
                              null)));
            return;
        }
        // Silph scope must be used on a ghost
        if (itemName.equals("silph scope") && !game.battle.oppPokemon.isGhost) {
            game.insertAction(this.prevMenu);
            game.insertAction(new PlaySound("error1",
                              new SetField(this.prevMenu, "disabled", false,
                              null)));
            return;
        }
        // Not allowed to catch ghosts (for now)
        if (itemName.contains("ball") && game.battle.oppPokemon.isGhost) {
            game.insertAction(new DisplayText(game, game.battle.oppPokemon.name.toUpperCase()+" canì be caught!", null, null,
                              new SetField(this.prevMenu, "disabled", false,
                              this.prevMenu)));
            return;
        }

        if (itemName.equals("moomoo milk") || itemName.equals("berry juice")) {
//            game.actionStack.remove(this);  // TODO: remove
            this.disabled = true;
            game.insertAction(new DrawPokemonMenu.Intro(
                              new DrawPokemonMenu(game, itemName,
                              this.prevMenu)));
            return;
        }
        
        // TODO 
//        // If item is heal item, allow player to select pokemon here
//        if (itemName.contains("berry") || itemName.equals("moomoo milk")) {
//            this.disabled = true;
//            game.insertAction(new DrawPokemonMenu.Intro(
//                              new DrawPokemonMenu(game, itemName,
//                              this.prevMenu)));
//            return;
//        }

        this.prevMenu.prevMenu.disabled = false;
        Action action = new SplitAction(new PlaySound("click1", null), null);
        if (game.type == Game.Type.CLIENT) {
            action.append(new Battle.WaitTurnData(game, null));
            game.client.sendTCP(new com.pkmngen.game.Network.DoBattleAction(game.player.network.id,
                                                                            Battle.DoTurn.Type.ITEM,
                                                                            itemName));
        }
        else {
            // Battle.DoTurn handles what to do with the item; it looks at game.battle.network.turnData.itemName
            // to know which item is being used.
            game.battle.network.turnData = new BattleTurnData();
            game.battle.network.turnData.itemName = itemName;
        }
        // Deduct item from inventory (except for key items, like the silph scope)
        if (!itemName.equals("silph scope")) {
            game.player.itemsDict.put(itemName, game.player.itemsDict.get(itemName)-1);
            if (game.player.itemsDict.get(itemName) <= 0) {
                game.player.itemsDict.remove(itemName);
            }
        }
        action.append(new Battle.DoTurn(game, Battle.DoTurn.Type.ITEM, this.prevMenu.prevMenu));
        game.actionStack.remove(this);
        game.insertAction(action);
    }

    /**
     * Drops a number of items on the Tile specified by pos.
     */
    public static class DropItem extends Action {
        public int layer = 0;
        Vector2 pos;
        Tile currTile;
        String item;
        int amount;

        public DropItem(String item, int amount, Vector2 pos, Action nextAction) {
            this.item = item;
            this.amount = amount;
            this.pos = pos;
            this.nextAction = nextAction;
        }

        public String getCamera() {return "gui";}

        public int getLayer(){return this.layer;}

        @Override
        public void firstStep(Game game) {
            this.currTile = game.map.tiles.get(this.pos);
        }

        @Override
        public void step(Game game) {
            // deduct items from player inventory
            game.player.itemsDict.put(this.item, game.player.itemsDict.get(this.item)-this.amount);
            if (game.player.itemsDict.get(this.item) <= 0) {
                game.player.itemsDict.remove(this.item);
            }
            Tile newTile = new Tile(this.currTile.name, "pokeball1", this.pos.cpy(), true, this.currTile.routeBelongsTo);
            newTile.hasItem = this.item;
            newTile.hasItemAmount = this.amount;
            if (game.type != Game.Type.CLIENT) {
                game.map.tiles.put(this.pos.cpy(), newTile);
            }
            else {
                game.client.sendTCP(new Network.TileData(newTile));
            }
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
        }
    }

    /**
     * Draw a box where the user selects the amount of an item to drop.
     */
    static class SelectAmount extends MenuAction {
        public int layer = 106;
        Sprite textbox;
        String itemName;
        public static int amount = 0;
        int maxAmount = 1;
        Vector2 pos;

        public SelectAmount(String itemName, Vector2 pos, MenuAction prevMenu) {
//            this.maxAmount = maxAmount;
            this.pos = pos;
            this.itemName = itemName;
            this.prevMenu = prevMenu;
            Texture text = new Texture(Gdx.files.internal("amount_bg1.png"));
            this.textbox = new Sprite(text, 0,0, 16*10, 16*9);
        }

        public String getCamera() {return "gui";}

        public int getLayer(){return this.layer;}

        @Override
        public void firstStep(Game game) {
            SelectAmount.amount = 1;
        }

        @Override
        public void step(Game game) {
            // if there is a previous menu, step through it to display text
            if (prevMenu != null) {
                prevMenu.step(game);
            }

            this.textbox.draw(game.uiBatch);

            if (this.disabled) {
                return;
            }

            if (InputProcessor.upJustPressed) {
                if (SelectAmount.amount+1 <= game.player.itemsDict.get(this.itemName)) {
                    SelectAmount.amount++;
                }
            }
            else if (InputProcessor.downJustPressed) {
                if (SelectAmount.amount > 1) {
                    SelectAmount.amount--;
                }
            }
            else if (InputProcessor.rightJustPressed) {
                SelectAmount.amount += 10;
                if (SelectAmount.amount > game.player.itemsDict.get(this.itemName)) {
                    SelectAmount.amount = game.player.itemsDict.get(this.itemName);
                }
            }
            else if (InputProcessor.leftJustPressed) {
                SelectAmount.amount -= 10;
                if (SelectAmount.amount <= 1) {
                    SelectAmount.amount = 1;
                }
            }

            Sprite letterSprite;
            String word = "x"+String.format("%02d", SelectAmount.amount);
            for (int i=0; i < word.length(); i++) {
                char letter = word.charAt(i);
                letterSprite = game.textDict.get(letter);
                game.uiBatch.draw(letterSprite, 130 +8*i, 56);
            }

            if (InputProcessor.aJustPressed) {
                if (game.type == Game.Type.CLIENT) {
                    game.client.sendTCP(new com.pkmngen.game.Network.DropItem(game.player.network.id,
                                                                              this.itemName,
                                                                              SelectAmount.amount,
                                                                              this.pos));
                }
                
                this.disabled = true;
                game.actionStack.remove(this);
                game.playerCanMove = false;
                game.insertAction(new WaitFrames(game, 10,
                                  new SplitAction(new DrawUseTossMenu.DropItem(this.itemName, SelectAmount.amount, this.pos, null),
                                  new PlaySound("seed1",
                                  new WaitFrames(game, 10,
                                  new SetField(game, "playerCanMove", true,
                                  null))))));
            }
            // player presses b, ie wants to go back
            else if (InputProcessor.bJustPressed) {
                game.insertAction(new PlaySound("click1", null));
                game.actionStack.remove(this);
                this.prevMenu.disabled = false;
                game.insertAction(this.prevMenu);
                return;
            }
        }
    }
}

/**
 * Self-explanatory.
 */
class DrawYesNoMenu extends MenuAction {
    Action nextAction2;
    Sprite arrow;
    Sprite textBox;
    public int layer = 105;
    Map<Integer, Vector2> getCoords = new HashMap<Integer, Vector2>();
    int curr;
    Vector2 newPos;
    Sprite helperSprite;
    int cursorDelay; // this is just extra detail. cursor has 2 frame delay before showing in R/B
    String itemName;
    MenuAction prevMenu;
    ArrayList<String> words = new ArrayList<String>();

    // constructor for when this was called by the item menu
     // probably will create separate constructor for other cases
    public DrawYesNoMenu(MenuAction prevMenu, Action yesAction, Action noAction) {
        this.prevMenu = prevMenu;
        this.nextAction = yesAction;
        this.nextAction2 = noAction;
        this.cursorDelay = 0;

        Texture text = new Texture(Gdx.files.internal("battle/arrow_right1.png"));
        this.arrow = new Sprite(text, 0, 0, 5, 7);

        // text box bg
        text = new Texture(Gdx.files.internal("attack_menu/yesno_bg1.png"));
        this.textBox = new Sprite(text, 0,0, 16*10, 16*9);

        this.getCoords.put(0, new Vector2(113+8, 48+24));
        this.getCoords.put(1, new Vector2(113+8, 48-16+24));

        // this.newPos =  new Vector2(32, 79); // post scaling change
        this.newPos =  this.getCoords.get(0);
        this.arrow.setPosition(newPos.x, newPos.y);
        this.curr = 0;
        this.words.add("YES");
        this.words.add("NO");
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        // if there is a previous menu, step through it to display text
        if (prevMenu != null) {
            prevMenu.step(game);
        }
        // draw text box
//        this.textBox.draw(game.uiBatch);
        game.uiBatch.draw(this.textBox, this.textBox.getX(), this.textBox.getY());

        // Draw use/drop
        for (int i=0; i < this.words.size(); i++) {
            String word = this.words.get(i);
            for (int j=0; j < word.length(); j++) {
                // Convert character to sprite and draw
                char letter = word.charAt(j);
                game.uiBatch.draw(game.textDict.get(letter), 120 +8 +8*j, 48 +23 -16*i);
            }
        }

        if (this.disabled){
            return;
        }

        // check user input
         //'tl' = top left, etc.
         // modify position by modifying curr to tl, tr, bl or br
        if (InputProcessor.upJustPressed) {
            if (this.curr > 0) {
                this.curr -= 1;
                newPos = getCoords.get(this.curr);
            }

        }
        else if (InputProcessor.downJustPressed) {
            if (this.curr < 1) {
                this.curr += 1;
                newPos = getCoords.get(this.curr);
            }
        }

        // draw arrow
        if (this.cursorDelay >= 2) {
            this.arrow.setPosition(newPos.x, newPos.y);
//            this.arrow.draw(game.uiBatch);
            game.uiBatch.draw(this.arrow, this.arrow.getX(), this.arrow.getY());
        }
        else {
            this.cursorDelay+=1;
        }

        if (InputProcessor.aJustPressed) {
            game.actionStack.remove(this);
            if (this.curr == 0) {
                game.insertAction(this.nextAction);
            }
            else {
                game.insertAction(this.nextAction2);
            }
        }
        // player presses b, ie wants to go back
        else if (InputProcessor.bJustPressed) {
            game.actionStack.remove(this);
            if (this.prevMenu != null) {
                this.prevMenu.disabled = false;
                game.insertAction(this.prevMenu);
            }
            else {
                game.insertAction(this.nextAction2);
            }
        }
    }
}

// scroll enemy pkmn off screen
class EnemyFaint extends Action {
    ArrayList<Vector2> positions;
    ArrayList<Integer> repeats;
    ArrayList<Boolean> playSound;
    Vector2 move;

    Vector2 position;
    Sprite sprite;
    Sprite breathingSprite = null;

    public int layer = 120;
    boolean firstStep;

    Sprite helperSprite;

    public EnemyFaint(Game game, Action nextAction) {
        this.nextAction = nextAction;
        this.firstStep = true;

        this.position = null;

        this.positions = new ArrayList<Vector2>();

        // animation to play
//        for (int i = 0; i < 14; i++) {
//            positions.add(new Vector2(0,-4));
//        }
//        positions.add(new Vector2(0,0));
//        positions.add(new Vector2(0,0));
//
//        this.repeats = new ArrayList<Integer>();
//        this.repeats.add(24);
//        for (int i = 0; i < 16; i++) {
//            repeats.add(1);
//        }
//        repeats.add(2);

        // try moving sprite iterations of 2
         // this version looks more natural
        // animation to play
        for (int i = 0; i < 7; i++) {
            positions.add(new Vector2(0,-8));
        }
        positions.add(new Vector2(0,0));
        positions.add(new Vector2(0,0));

        this.repeats = new ArrayList<Integer>();
        this.repeats.add(24);
        for (int i = 0; i < 8; i++) {
            repeats.add(2);
        }
//        repeats.add(2);

        this.playSound = new ArrayList<Boolean>();
        if (!game.map.timeOfDay.equals("night")) {
            this.playSound.add(true);
        }
        else {
            this.playSound.add(false);
        }
        for (int i = 0; i < 15; i++) {
            this.playSound.add(false);
        }

        this.sprite = new Sprite(game.battle.oppPokemon.sprite);
        if (game.battle.oppPokemon.breathingSprite != null) {
            this.breathingSprite = game.battle.oppPokemon.breathingSprite;
        }

//        Texture text = new Texture(Gdx.files.internal("attack_menu/helper5.png"));
//        this.helperSprite = new Sprite(text, 0,0, 16*10, 16*9);
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        if (this.firstStep == true) {
            // TODO - because drawing enemy sprite will likely be an
             // action later, this flag will need to instead be like
             //'FriendlyFaint' , ie remove drawAction for pokemon

            // stop drawing enemy sprite
            DrawBattle.shouldDrawOppPokemon = false;

            this.firstStep = false;
        }

        // if done with anim, do nextAction
        if (positions.isEmpty() || repeats.isEmpty()) {
            // remove enemy from route, and add a new pokemon in route
            if (game.type != Game.Type.CLIENT) {
                game.map.currRoute.pokemon.remove(game.battle.oppPokemon);
                game.map.currRoute.genPokemon(256);
                // TODO: debug, remove
                for (Pokemon pokemon : game.map.currRoute.pokemon) {
                    System.out.println(pokemon.name);
                }
            }

            // stop drawing enemy healthbar
            game.actionStack.remove(game.battle.drawAction.drawEnemyHealthAction);
            game.battle.drawAction.drawEnemyHealthAction = null;

            game.insertAction(this.nextAction);
            game.actionStack.remove(this);
            return;
        }

        // debug
//        this.helperSprite.draw(game.floatingBatch);

        this.sprite.draw(game.uiBatch);
        if (this.breathingSprite != null) {
            this.breathingSprite.draw(game.uiBatch);
        }

        // debug
//        if (this.repeats.size() == 1) {
//            return;
//        }

        if (this.repeats.get(0) > 1) {
            this.repeats.set(0, this.repeats.get(0) - 1);
        }
        else {
            // todo - remove
//            this.position = new Vector2(this.sprite.getX(), this.sprite.getY());
//            this.position.add(positions.get(0));
//            this.sprite.setPosition(position.x, position.y);

            // this.sprite.setRegionY(this.sprite.getRegionY() + (int)positions.get(0).y);
            this.sprite.setRegionHeight(this.sprite.getRegionHeight() + (int)positions.get(0).y);
            this.sprite.setSize(this.sprite.getWidth(), this.sprite.getHeight() + (int)positions.get(0).y);
            if (this.breathingSprite != null) {
                this.breathingSprite.setRegionHeight(this.breathingSprite.getRegionHeight() + (int)positions.get(0).y);
                this.breathingSprite.setSize(this.breathingSprite.getWidth(), this.breathingSprite.getHeight() + (int)positions.get(0).y);
            }
            // TODO: remove comments if unused.
            if (this.playSound.get(0) == true) {  // && !SpecialMewtwo1.class.isInstance(game.battle.oppPokemon)
                game.musicController.battleVictoryFanfare = true;
//                // play victory fanfare
//                game.currMusic.pause();
//                game.currMusic = game.battle.victoryFanfare;
//                game.currMusic.stop();
//                game.currMusic.play();
            }

            positions.remove(0);
            repeats.remove(0);
            playSound.remove(0);
        }
    }
}

class EggHatchAnim extends Action {
    public int layer = 130;
    public Sprite bgSprite;
    public static boolean drawPostHatchBottom = false;
    public static boolean drawPostHatchTop = false;
    public static boolean drawSprite = false;
    Pokemon pokemon;
    public static boolean isDone = false;

    public EggHatchAnim(Pokemon pokemon, Action nextAction) {
        Texture text = new Texture(Gdx.files.internal("battle/intro_frame6.png"));
        this.bgSprite = new Sprite(text, 0, 0, 160, 144);
        this.pokemon = pokemon;
        this.nextAction = nextAction;
    }

    @Override
    public void firstStep(Game game) {
        EggHatchAnim.drawPostHatchBottom = false;
        EggHatchAnim.drawPostHatchTop = false;
        EggHatchAnim.drawSprite = false;
        EggHatchAnim.isDone = false;

        this.pokemon.hatch();
        
        // set oppPokemon to hatching pokemon in order to play intro animation
        game.battle.oppPokemon = this.pokemon;

        // Cache one frame where egg palette changes to the hatching pokemon's palette
        FileHandle fileHandle = Gdx.files.internal("attacks/egg_hatch_gsc/output/frame-452.png");
        Texture text = new Texture(fileHandle);
        TextureData temp = text.getTextureData();
        if (!temp.isPrepared()) {
            temp.prepare();
        }
        Pixmap currPixmap = temp.consumePixmap();
        Pixmap newPixmap = new Pixmap(text.getWidth(), text.getHeight(), Pixmap.Format.RGBA8888);
        newPixmap.setColor(new Color(0, 0, 0, 0));
        newPixmap.fill();
        for (int i=0, j=0; j < text.getHeight(); i++) {
            if (i > text.getWidth()) {
                i=-1;
                j++;
                continue;
            }
            Color color = new Color(currPixmap.getPixel(i, j));
            // color 1
            if ((int)(color.r*255) == 240 && (int)(color.g*255) == 208 && (int)(color.b*255) == 88) {
                color.r = this.pokemon.sprite.color1.r;
                color.g = this.pokemon.sprite.color1.g;
                color.b = this.pokemon.sprite.color1.b;
            }
            // color 2
            else if ((int)(color.r*255) == 184 && (int)(color.g*255) == 128 && (int)(color.b*255) == 0) {
                color.r = this.pokemon.sprite.color2.r;
                color.g = this.pokemon.sprite.color2.g;
                color.b = this.pokemon.sprite.color2.b;
            }
            newPixmap.drawPixel(i, j, Color.rgba8888(color.r, color.g, color.b, color.a));
        }
        // Pre-cache that frame with the colors swapped
        TextureCache.textMap.put(fileHandle, TextureCache.get(newPixmap));
        game.insertAction(this.nextAction);
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        if (EggHatchAnim.isDone) {
            game.actionStack.remove(this);
            return;
        }

        game.uiBatch.draw(this.bgSprite, 0, 0);

        if (EggHatchAnim.drawPostHatchTop) {
            // Draw full sprite
            this.pokemon.sprite.setRegionY(0);
            this.pokemon.sprite.setRegionHeight((int)this.pokemon.sprite.getHeight());
            EggHatchAnim.drawSprite = true;
            EggHatchAnim.drawPostHatchTop = false;
        }
        if (EggHatchAnim.drawPostHatchBottom) {
            // Draw lower part of sprite
            this.pokemon.sprite.setRegionY((int)this.pokemon.sprite.getWidth()-(int)this.pokemon.sprite.getWidth()+(int)this.pokemon.sprite.getWidth()/2);
            this.pokemon.sprite.setRegionHeight((int)this.pokemon.sprite.getWidth()/2);
            EggHatchAnim.drawSprite = true;
            EggHatchAnim.drawPostHatchBottom = false;
        }
        if (EggHatchAnim.drawSprite) {
            game.uiBatch.draw(this.pokemon.sprite, (160/2)-((int)this.pokemon.sprite.getWidth()/2)+4, 64);  //was y=72+(int)this.pokemon.sprite.getWidth()/2
        }
    }

    /**
     * Apply changes to evolved pokemon.
     */
    public static class Done extends Action {
        public int timer = 13;
        
        public Done(int timer, Action nextAction) {
            this.timer = timer;
            this.nextAction = nextAction;
        }

        @Override
        public void step(Game game) {
            this.timer--;
            if (this.timer <=0) {
                EggHatchAnim.isDone = true;
                game.actionStack.remove(this);
                game.insertAction(this.nextAction);
            }
            EggHatchAnim.drawSprite = false;
        }
    }
}

class EvolutionAnim extends Action {
    public static Sprite bgSprite;
    public static boolean drawSprite = true;
    public static boolean drawPostEvoBottom = false;
    public static boolean drawPostEvoTop = false;
    public static boolean isGreyscale = false;
    public static boolean playSound = false;
    public static boolean isDone = false;
    // TODO: should make this public static somewhere.
    // Maybe under Battle? idk.
    /*
     * Source: https:// stackoverflow.com/questions/17516177/texture-grayscale-in-libgdx
     */
    public static String vertexShader = "attribute vec4 a_position;\n" +
            "attribute vec4 a_color;\n" +
            "attribute vec2 a_texCoord0;\n" +
            "\n" +
            "uniform mat4 u_projTrans;\n" +
            "\n" +
            "varying vec4 v_color;\n" +
            "varying vec2 v_texCoords;\n" +
            "\n" +
            "void main() {\n" +
            "    v_color = a_color;\n" +
            "    v_texCoords = a_texCoord0;\n" +
            "    gl_Position = u_projTrans * a_position;\n" +
            "}";
    public static String fragmentShader = "#ifdef GL_ES\n" +
            "    precision mediump float;\n" +
            "#endif\n" +
            "\n" +
            "varying vec4 v_color;\n" +
            "varying vec2 v_texCoords;\n" +
            "uniform sampler2D u_texture;\n" +
            "\n" +
            "void main() {\n" +
            "  vec4 c = v_color * texture2D(u_texture, v_texCoords);\n" +
//            "  float grey = (c.r + c.g + c.b) / 3.0;\n" +
            "  int r = int(c.r*31.0);\n" +
            "  int g = int(c.g*31.0);\n" +
            "  int b = int(c.b*31.0);\n" +
            "  int sum = r + g + b;\n" +
            // casting to int for the sake of trying to keep it close to gbc approximations.
            "  float grey = float(int(sum*sum*sum)/25947)/31.0;\n" +  // y = sum_c^3/25947
            "  gl_FragColor = vec4(grey, grey, grey, c.a);\n" +
            "}";

    public int layer = 130;
    Sprite spritePart;

    Sprite preEvoSprite;
    Sprite postEvoSprite;

    Sprite preEvoSpriteTop;

    Sprite postEvoSpriteTop;
    Sprite preEvoSpriteBottom;
    Sprite postEvoSpriteBottom;
    Music preEvoCry;
    Music postEvoCry;
    int timer = 0;

    Pokemon targetPokemon;
    String targetName;

    ShaderProgram grayscaleShader = new ShaderProgram(vertexShader, fragmentShader);

    public EvolutionAnim(Pokemon targetPokemon, String evolveTo, Action nextAction) {
        this.nextAction = nextAction;
        this.targetPokemon = targetPokemon;
        this.targetName = evolveTo;
        Texture text = new Texture(Gdx.files.internal("battle/battle_bg3.png"));
        EvolutionAnim.bgSprite = new Sprite(text, 0, 0, 176, 160);
        this.preEvoSprite = new Sprite(targetPokemon.sprite);
        this.preEvoSprite.flip(true, false);
//        // TODO: this might not work for prism pokemon
//        if (!Pokemon.textures.containsKey(evolveTo+"_front")) {
//            String dexNumber = Pokemon.nameToIndex(evolveTo);
//            if (Integer.valueOf(dexNumber) <= 251 && Integer.valueOf(dexNumber) > 0) {
//                Pokemon.textures.put(evolveTo+"_front", new Texture(Gdx.files.internal("crystal_pokemon/pokemon/" + evolveTo + "/front.png")));
//            }
//            // else assume prism pokemon
//            else {
//                Pokemon.textures.put(evolveTo+"_front", new Texture(Gdx.files.internal("crystal_pokemon/prism/pics/" + evolveTo + "/front.png")));
//            }
//        }

//        this.postEvoSprite = new Sprite(Pokemon.textures.get(evolveTo+"_front"));
        this.preEvoSpriteBottom = new Sprite(this.preEvoSprite);
        this.preEvoSpriteBottom.setRegionY((int)this.preEvoSprite.getWidth()/2);
        this.preEvoSpriteBottom.setRegionHeight((int)this.preEvoSprite.getWidth()/2);
        this.preEvoSpriteTop = new Sprite(this.preEvoSprite);
        this.preEvoSpriteTop.setRegionY(0);
        this.preEvoSpriteTop.setRegionHeight((int)this.preEvoSprite.getWidth()/2);
    }

    @Override
    public void firstStep(Game game) {
        EvolutionAnim.drawSprite = false;
        EvolutionAnim.drawPostEvoBottom = false;
        EvolutionAnim.drawPostEvoTop = false;
        EvolutionAnim.isGreyscale = false;
        EvolutionAnim.playSound = false;
        EvolutionAnim.isDone = false;
        // Evolve pokemon, use it's new sprite in evo animation.
        this.targetPokemon.evolveTo(this.targetName);
        this.postEvoSprite = new Sprite(this.targetPokemon.sprite);
        this.postEvoSprite.flip(true, false);
        this.postEvoSpriteBottom = new Sprite(this.postEvoSprite);
        this.postEvoSpriteBottom.setRegionY((int)this.postEvoSprite.getWidth()-(int)this.preEvoSprite.getWidth()+(int)this.preEvoSprite.getWidth()/2);
        this.postEvoSpriteBottom.setRegionHeight((int)this.preEvoSprite.getWidth()/2);
        this.postEvoSpriteTop = new Sprite(this.postEvoSprite);
        this.postEvoSpriteTop.setRegionY(0);
        this.postEvoSpriteTop.setRegionHeight((int)this.postEvoSprite.getWidth()-(int)this.preEvoSprite.getWidth()+(int)this.preEvoSprite.getWidth()/2);
        
        // TODO: refactor to using a flag in DrawBattle instead
        game.actionStack.remove(game.battle.drawAction); // stop drawing the battle
        game.battle.drawAction = null;
        game.currMusic.stop();
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        game.uiBatch.draw(EvolutionAnim.bgSprite, -8, -8);

        // Screen is blank for 33 frames, then sprite appears
        if (this.timer == 33) {
            EvolutionAnim.drawSprite = true;
            game.insertAction(this.nextAction);
        }

        // TODO: this won't always line up
        if (EvolutionAnim.playSound) {
            EvolutionAnim.playSound = false;
            Music sound = Gdx.audio.newMusic(Gdx.files.internal("evolve_fanfare1.ogg"));
            sound.play();
        }

        if (EvolutionAnim.drawSprite) {
            if (EvolutionAnim.isGreyscale) {
                /* Grayscale effect:
                 * A cubic function is at least close:
                 * y = Output grayscale intensity, x = sum of rgb intensities
                 * n*y = x^3
                 * n*31 = (31*3)^3; n = 25947
                 * 25947*y = x^3
                 * Sample values from cyndaquil evolve anim:
                 * 25947*y = (31 + 27 + 0)^3; y = 7 (correct)
                 * 25947*y = (31 + 7 + 5)^3;  y = 3 (correct, except r=2, g=3, b=3 in reality)
                 * note: I scaled this from 31 to 1f in the shader. 31 is gameboy rgb channel size.
                 */
                game.uiBatch.setShader(this.grayscaleShader);
            }

            if (EvolutionAnim.drawPostEvoBottom) {
                this.spritePart = this.postEvoSpriteBottom;
            }
            else {
                this.spritePart = this.preEvoSpriteBottom;
            }
            // 64/72
            game.uiBatch.draw(this.spritePart, (160/2)-((int)this.spritePart.getWidth()/2)+4, 72);

            if (EvolutionAnim.drawPostEvoTop) {
                this.spritePart = this.postEvoSpriteTop;
            }
            else {
                this.spritePart = this.preEvoSpriteTop;
            }
            // 64/72
            game.uiBatch.draw(this.spritePart, (160/2)-((int)this.spritePart.getWidth()/2)+4, 72+(int)this.preEvoSpriteTop.getWidth()/2);

            game.uiBatch.setShader(null);
        }
        if (EvolutionAnim.isDone) {
            game.actionStack.remove(this);
            // Actually evolve the pokemon
            // TODO: add this logic to network code
//            this.targetPokemon.evolveTo(this.targetName);
        }
        this.timer++;
    }

    /**
     * Apply changes to evolved pokemon.
     */
    public static class Done extends Action {
        int timer = 0;

        public Done(Action nextAction) {
            this.nextAction = nextAction;
        }

        @Override
        public void firstStep(Game game) {
            Texture text = new Texture(Gdx.files.internal("battle/intro_frame6.png"));
            EvolutionAnim.bgSprite = new Sprite(text, 0, 0, 176, 160);
        }

        @Override
        public void step(Game game) {
            if (this.timer == 2) {
                EvolutionAnim.drawSprite = false;
            }
            if (this.timer == 36) {
                game.insertAction(new EnterBuilding(game, "",
//                                  new PlayerCanMove(game,
                                  null));
            }
            if (this.timer == 45) {
                EvolutionAnim.isDone = true;
                game.actionStack.remove(this);
                game.insertAction(this.nextAction);  // TODO: unused realistically, probably should be a fade-out anim
            }

            this.timer++;
        }
    }

    /**
     * TODO: remove, use music controller
     * Start evolution music.
     */
    public static class StartMusic extends Action {
        @Override
        public void firstStep(Game game) {
            game.currMusic = Gdx.audio.newMusic(Gdx.files.internal("evolution1.ogg"));
            game.currMusic.play();
            game.actionStack.remove(this);
        }
    }
}

/*
 * Scroll friendly Pokemon off screen.
 */
class FriendlyFaint extends Action {
    ArrayList<Vector2> positions;
    ArrayList<Integer> repeats;
    ArrayList<Boolean> playSound;
    Vector2 move;
    Vector2 position;
    Sprite sprite;
    public int layer = 120;
    boolean firstStep;

    public FriendlyFaint(Game game, Action nextAction) {
        this.nextAction = nextAction;
        this.firstStep = true;

        this.position = null;

        this.positions = new ArrayList<Vector2>();

        // animation to play
//        for (int i = 0; i < 14; i++) {
//            positions.add(new Vector2(0,-4));
//        }
//        positions.add(new Vector2(0,0));
//        positions.add(new Vector2(0,0));
//
//        this.repeats = new ArrayList<Integer>();
//        this.repeats.add(24);
//        for (int i = 0; i < 16; i++) {
//            repeats.add(1);
//        }
//        repeats.add(2);

        // try moving sprite iterations of 2
         // this version looks more natural
        // animation to play
        for (int i = 0; i < 7; i++) {
            positions.add(new Vector2(0,-8));
        }
        positions.add(new Vector2(0,0));
        positions.add(new Vector2(0,0));

        this.repeats = new ArrayList<Integer>();
        this.repeats.add(24);
        for (int i = 0; i < 8; i++) {
            repeats.add(2);
        }
//        repeats.add(2);

        this.playSound = new ArrayList<Boolean>();
        // TODO: why was this here?
//        if (!game.map.timeOfDay.equals("night")) {
            this.playSound.add(true);
//        }
//        else {
//            this.playSound.add(false);
//        }
        for (int i = 0; i < 15; i++) {
            this.playSound.add(false);
        }
        this.sprite = new Sprite(game.player.currPokemon.backSprite);
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        if (this.firstStep) {
            // stop drawing friendly healthbar
            game.actionStack.remove(game.battle.drawAction.drawFriendlyHealthAction);
            game.battle.drawAction.drawFriendlyHealthAction = null;

            // stop drawing friendly sprite
            game.actionStack.remove(game.battle.drawAction.drawFriendlyPokemonAction);
            game.battle.drawAction.drawFriendlyPokemonAction = null;
            
            // Remove status ailments
            // TODO: this should probably just keep a concept of this.pokemon,
            // and move it's backsprite.
            game.player.currPokemon.status = null;
            this.firstStep = false;
        }
        // If done with anim, do nextAction
        if (positions.isEmpty() || repeats.isEmpty()) {
            // game.insertAction(this.nextAction); // doing after sound is played instead; remove this
            game.actionStack.remove(this);
            return;
        }

        this.sprite.draw(game.uiBatch);

        // debug
//        if (this.repeats.size() == 1) {
//            return;
//        }

        if (this.repeats.get(0) > 1) {
            this.repeats.set(0, this.repeats.get(0) - 1);
        }
        else {
            // this.sprite.setRegionY(this.sprite.getRegionY() + (int)positions.get(0).y);
            this.sprite.setRegionHeight(this.sprite.getRegionHeight() + (int)positions.get(0).y / 2);
            this.sprite.setSize(this.sprite.getWidth(), this.sprite.getHeight() + (int)positions.get(0).y / 2);

            if (this.playSound.get(0) == true) {
                // TODO - remove
                // play victory fanfare
//                game.currMusic.pause();
//                game.currMusic = game.battle.victoryFanfare;
//                game.currMusic.stop();
//                game.currMusic.play();
                // TODO - right timing?
//                game.insertAction(new PlaySound(game.player.currPokemon.name, this.nextAction));
                game.insertAction(new PlaySound(game.player.currPokemon, this.nextAction));
            }
            positions.remove(0);
            repeats.remove(0);
            playSound.remove(0);
        }
    }
}

/**
 * Check if the Pokemon should evolve. If so, play the evolve
 * animation.
 */
class CheckEvo extends Action {
    Pokemon pokemon;

    public CheckEvo(Pokemon pokemon, Action nextAction) {
        this.pokemon = pokemon;
        this.nextAction = nextAction;
    }

    @Override
    public void step(Game game) {
        // Check if the Pokemon should evolve
        if (this.pokemon.gainedLevel) {
            this.pokemon.gainedLevel = false;
            for (int i=1; i <= this.pokemon.level; i++) {
                if (Specie.gen2Evos.get(this.pokemon.name.toLowerCase()).containsKey(String.valueOf(i))) {
                    String evolveTo = Specie.gen2Evos.get(this.pokemon.name.toLowerCase()).get(String.valueOf(i));
                    this.nextAction = new WaitFrames(game, 61,
                                    new WaitFrames(game, 3,  // NOTE: this is in case I add 3 frame delay to DisplayText, in that case
                                                             // remove this line.
                                    new DisplayText(game, "What? "+this.pokemon.name.toUpperCase()+" is evolving!",
                                                    null, true, false,
                                    new WaitFrames(game, 51,
                                    new EvolutionAnim(this.pokemon, evolveTo,
                                    new PlaySound(this.pokemon,
                                    new SplitAction(
                                        new EvolutionAnim.StartMusic(),
                                    new Battle.LoadAndPlayAnimation(game, "evolve", null,
                                    new WaitFrames(game, 30,  // about 30 frames after bubble anim until pokemon cry is heard
                                    new PlaySound(new Pokemon(evolveTo.toLowerCase(), 10),
                                    new DisplayText.Clear(game,
                                    new WaitFrames(game, 3,
                                    new DisplayText(game, "Congratulations! Your "+this.pokemon.name.toUpperCase(),
                                                    null, true, true,
                                    new DisplayText.Clear(game,
                                    new WaitFrames(game, 3,
                                    new DisplayText(game, "evolved into "+evolveTo.toUpperCase()+"!",
                                                    "fanfare2.ogg", true, false,
//                                            new WaitFrames(game, 206,  // TODO: remove
                                    new DisplayText.Clear(game,
                                    new WaitFrames(game, 2,
                                    new EvolutionAnim.Done(
                                    this.nextAction)))))))))))))))))));
                    break;
                }
            }
        }
        game.actionStack.remove(this);
        game.insertAction(//new DisplayText.Clear(game,
                          //new WaitFrames(game, 3,
                          this.nextAction);
    }
}

/**
 * TODO: not doing 'blue bar' animation for now, do at some point.
 */
class GainExpAnimation extends Action {
//    boolean gainedLevel = false;
    Pokemon pokemon;

    public GainExpAnimation(Pokemon pokemon, Action nextAction) {
        this.pokemon = pokemon;
        this.nextAction = nextAction;
    }

    @Override
    public void step(Game game) {
        if (pokemon.level < 100 &&
            pokemon.gen2CalcExpForLevel(pokemon.level+1) <= pokemon.exp) {
            // TODO: debug, remove
//            System.out.println("Curr exp: " + String.valueOf(game.player.currPokemon.exp));
//            System.out.println("Needed for next level: " + String.valueOf(game.player.currPokemon.gen2CalcExpForLevel(game.player.currPokemon.level+1)));
//            game.player.currPokemon.level += 1;  // TODO: remove
            pokemon.gainLevel(1);
            game.actionStack.remove(this);
            Action action = new DisplayText.Clear(game,
                            new WaitFrames(game, 3,
                            new DisplayText(game, pokemon.name.toUpperCase() + " grew to level " + pokemon.level+"!",
                                            "fanfare1.ogg", true, true,
                            null)));
            // Check if any moves learned
            if (pokemon.learnSet.containsKey(pokemon.level)) {
                for (String attack : pokemon.learnSet.get(pokemon.level)) {
                    // TODO: eventually remove this check
                    if (Pokemon.attacksNotImplemented.contains(attack.toLowerCase())) {
                        continue;
                    }
                    boolean learned = false;
                    for (int i = 0; i < 4; i++) {
                        if (pokemon.attacks[i] == null) {
                            action.append(new DisplayText.Clear(game,
                                          new WaitFrames(game, 3,
                                          new DisplayText(game, pokemon.name.toUpperCase() + " learned " + attack.toUpperCase()+"!",
                                                          "fanfare1.ogg", true, true,
                                          null))));
                            pokemon.attacks[i] = attack;
                            if (game.type == Game.Type.CLIENT) {
                                game.client.sendTCP(new Network.LearnMove(game.player.network.id, 0, i, attack));
                            }
                            learned = true;
                            break;
                        }
                    }
                    if (!learned) {
                        action.append(new DisplayText.Clear(game,
                                      new WaitFrames(game, 3,
                                      new DisplayText(game, pokemon.name.toUpperCase() + " is trying to learn " + attack.toUpperCase()+".",
                                                      null, null,
                                      new DisplayText(game, "Which move should be forgotten?",
//                                                      null, true, false,
                                                      null, true, true,   // <-- this was a requested fix
                                      new DrawAttacksMenu(attack, pokemon,
                                      null))))));
                    }
                }
            }
            action.append(this);
            game.insertAction(action);
//            this.gainedLevel = true;
            this.pokemon.gainedLevel = true;
            return;
        }
        // TODO: remove
//        // Check if the Pokemon should evolve
//        if (this.gainedLevel) {
//            for (int i=1; i <= game.player.currPokemon.level; i++) {
//                if (Pokemon.gen2Evos.get(game.player.currPokemon.name.toLowerCase()).containsKey(String.valueOf(i))) {
//                    String evolveTo = Pokemon.gen2Evos.get(game.player.currPokemon.name.toLowerCase()).get(String.valueOf(i));
//                    this.nextAction = new WaitFrames(game, 61,
//                                    new WaitFrames(game, 3,  // NOTE: this is in case I add 3 frame delay to DisplayText, in that case
//                                                             // remove this line.
//                                    new DisplayText(game, "What? "+game.player.currPokemon.name.toUpperCase()+" is evolving!",
//                                                    null, true, false,
//                                    new WaitFrames(game, 51,
//                                    new EvolutionAnim(game.player.currPokemon, evolveTo,
//                                    new PlaySound(game.player.currPokemon,
//                                    new SplitAction(
//                                        new EvolutionAnim.StartMusic(),
//                                    new Battle.LoadAndPlayAnimation(game, "evolve", null,
//                                    new WaitFrames(game, 30,  // about 30 frames after bubble anim until pokemon cry is heard
//                                    new PlaySound(new Pokemon(evolveTo.toLowerCase(), 10, Pokemon.Generation.CRYSTAL),
//                                    new DisplayText.Clear(game,
//                                    new WaitFrames(game, 3,
//                                    new DisplayText(game, "Congratulations! Your "+game.player.currPokemon.name.toUpperCase(),
//                                                    null, true, true,
//                                    new DisplayText.Clear(game,
//                                    new WaitFrames(game, 3,
//                                    new DisplayText(game, "evolved into "+evolveTo.toUpperCase()+"!",
//                                                    "fanfare2.ogg", true, false,
////                                            new WaitFrames(game, 206,  // TODO: remove
//                                    new DisplayText.Clear(game,
//                                    new WaitFrames(game, 2,
//                                    new EvolutionAnim.Done(
//                                    this.nextAction)))))))))))))))))));
//                    break;
//                }
//            }
//        }
        // TODO: test that this is working.
        game.actionStack.remove(this);
        game.insertAction(new DisplayText.Clear(game,
                          new WaitFrames(game, 3,
                          this.nextAction)));
    }
}

// scroll both players into view
class MovePlayerOffScreen extends Action {
    ArrayList<Vector2> positions;
    ArrayList<Integer> repeats;
    Vector2 move;

    Vector2 position;
    Sprite sprite;

    public int layer = 140;
    public MovePlayerOffScreen(Game game, Action nextAction) {
        this.nextAction = nextAction;

        this.position = null;

        this.positions = new ArrayList<Vector2>();

        // animation to play
        positions.add(new Vector2(0,0));
        for (int i = 0; i < 3; i++) {
            positions.add(new Vector2(-8,0));
            positions.add(new Vector2(-16,0));
            positions.add(new Vector2(-8,0));
            positions.add(new Vector2(-16,0));
            positions.add(new Vector2(-8,0));
            positions.add(new Vector2(-16,0));
        }

        this.repeats = new ArrayList<Integer>();
        this.repeats.add(38);
        this.repeats.add(2);
        for (int i = 0; i < 5; i++) {
            repeats.add(3);
        }

        this.sprite = game.player.battleSprite;
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        // if done with anim, do nextAction
        if (positions.isEmpty() || repeats.isEmpty()) {
            game.insertAction(this.nextAction);
            game.actionStack.remove(this);
            return;
        }

        if (this.repeats.get(0) > 1) {
            this.repeats.set(0, this.repeats.get(0) - 1);
        }
        else {
            // since position is relative, only update once each time period
            this.position = new Vector2(game.player.battleSprite.getX(), game.player.battleSprite.getY());
            this.position.add(positions.get(0));
            this.sprite.setPosition(position.x, position.y);
            positions.remove(0);
            repeats.remove(0);
        }

    }

}

class OppPokemonFlee extends Action {
    ArrayList<Vector2> positions;
    Vector2 position;
    Sprite sprite;
    ArrayList<Integer> repeats;
    String sound;
    ArrayList<String> sounds;

    public int layer = 120;
    Sprite helperSprite; // just for helping me position the animation. delete later.

    public OppPokemonFlee(Game game, Action nextAction) {
        this.nextAction = nextAction;

        // Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball1.png"));

        // initial oppPokemon sprite position
        this.position = new Vector2(game.battle.oppPokemon.sprite.getX(),game.battle.oppPokemon.sprite.getY());

        this.positions = new ArrayList<Vector2>();
        this.positions.add(new Vector2(0,0)); // wait 16 frames
        for (int i = 0; i < 13; i++) { // move 8 pixels to the right every 3 frames
            this.positions.add(new Vector2(8,0));
        }
        // 14 total

        this.repeats = new ArrayList<Integer>();

        this.repeats.add(16-1); // 16 frames of nothing
        for (int i = 0; i < 13; i++) {
            this.repeats.add(3-1); // 3 frames each movement
        }
        // 14 total

        // sounds to play // nothing?
        this.sounds = new ArrayList<String>();
        for (int i = 0; i < 14; i++) {
            this.sounds.add(null);
        }
        // 14 total events

        // text = new Texture(Gdx.files.internal("throw_pokeball_anim/helper13.png"));
        // this.helperSprite = new Sprite(text, 0, 0, 160, 144);
        // this.helperSprite.setPosition(16*10,16*9); // post scaling change
        // this.helperSprite.setScale(3);
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        // set opp pokemon sprite alpha
        // game.battle.oppPokemon.sprite.setAlpha(0); // delete at some point

        // set sprite position
        // if done with anim, do nextAction
        if (positions.isEmpty()) {
            game.insertAction(this.nextAction);
            game.actionStack.remove(this);
            return;
        }

        // get next sound, play it
        this.sound = this.sounds.get(0);
        if (this.sound != null) {
            game.insertAction(new PlaySound(this.sound, null));
            this.sounds.set(0, null); // don't play same sound over again
        }

        // get next frame
        // this.sprite = sprites.get(0); // not using

        // debug
        // this.helperSprite.draw(game.floatingBatch); // debug

        // this.sprite.setScale(3); // post scaling change
        game.battle.oppPokemon.sprite.setPosition(position.x, position.y);
        game.battle.oppPokemon.sprite.draw(game.uiBatch);

        // debug
//        if (this.repeats.size() == 1) { // debug
//            return;
//        }

        // repeat sprite/pos for current object for 'frames[0]' number of frames.
        if (this.repeats.get(0) > 0) {
            this.repeats.set(0, this.repeats.get(0) - 1);
        }
        else {
            // since position is relative, only update once each time period
            this.position = this.position.add(positions.get(0));
            positions.remove(0);
            repeats.remove(0);
            sounds.remove(0);
        }
    }
}

/*
 * Display caught pkmn text.
 * Play victory fanfare.
 * Continue to draw pokeball on screen.
 */
class PokemonCaughtEvents extends Action {
    Action displayTextAction;
    Sprite pokeballSprite;
    public int layer = 120;
    boolean startLooking;

    public PokemonCaughtEvents(Game game, Action nextAction) {
        this.nextAction = nextAction;

        this.startLooking = false;

        // step through DisplayText until finished
        // String string1 = game.battle.oppPokemon.name.toUpperCase()+" was    caught!";
        String string1 = "All right! "+game.battle.oppPokemon.name.toUpperCase()+" was caught!";
        // demo code
//        String string2 = ""+game.player.name+" gained "+Character.forDigit(adrenaline,10)+" ADRENALINE!";

        // TODO: remove if unused
//        this.displayTextAction  = new DisplayText(game, string2,  null, null, null);
//        Action firstTextAction = new DisplayText(game, string1, "fanfare1", null, this.displayTextAction);
        Action firstTextAction = new DisplayText(game, string1, "fanfare1", null, null);
        this.displayTextAction = firstTextAction;

        game.insertAction(firstTextAction);

//        Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball_wiggleSheet1.png"));
        Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball_wiggleSheet1_color.png"));
        this.pokeballSprite = new Sprite(text, 12*2, 0, 12, 12); // right tilt
        this.pokeballSprite.setPosition(115,88);
    }
    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    // what to do at each iteration
    public void step(Game game) {
        this.pokeballSprite.draw(game.uiBatch);

        // When text action first appears, start checking for when it leaves AS
        if (this.startLooking == false && game.actionStack.contains(this.displayTextAction)) {
            this.startLooking = true;
            return; // won't have left AS on this first iteration, might as well return
        }

        // For now, if displayTextAction not in AS, remove and return
        if (!game.actionStack.contains(this.displayTextAction) && this.startLooking == true) {
            // set oppPokemon alpha back to normal
            game.battle.oppPokemon.sprite.setAlpha(1);

            game.insertAction(this.nextAction);
            game.actionStack.remove(this);
            // stop drawing the battle
            game.actionStack.remove(game.battle.drawAction); // stop drawing the battle
            game.battle.drawAction = null; // essentially using this like a flag

            // alternative is to have drawAction check for boolean to not exist, and remove itself if flag not set
            return;
        }

    }

}

class PokemonIntroAnim extends Action {
    public int layer = 140;
    int currFrame = 0;
    SpriteProxy originalSprite;

    boolean firstStep = true;
    public PokemonIntroAnim(Action nextAction) {
        this.nextAction = nextAction;
    }
    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        if (this.firstStep) {
            this.originalSprite = game.battle.oppPokemon.sprite;
            this.firstStep = false;
        }
        if (this.currFrame >= game.battle.oppPokemon.introAnim.size()) {
            game.battle.oppPokemon.sprite = this.originalSprite;
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
            return;
        }
        // play intro anim frame by frame
        game.battle.oppPokemon.sprite = game.battle.oppPokemon.introAnim.get(this.currFrame);
        game.battle.oppPokemon.sprite.setPosition(this.originalSprite.getX(), this.originalSprite.getY());
        this.currFrame++;
    }
}

class PrintAngryEating extends Action {
    public int layer = 120;
    public PrintAngryEating(Game game, Action nextAction) {
        this.nextAction = nextAction;
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        if (game.battle.oppPokemon.angry > 0) {
            String textString = "Wild " + game.battle.oppPokemon.name.toUpperCase() + " is angry!";
            game.insertAction(new DisplayText(game, textString, null, null, this.nextAction));
            game.actionStack.remove(this);

            game.battle.oppPokemon.angry--;
            // If the angry counter is decreased to zero this way,
             // the Pokémon's catch rate will also be reset to its initial catch rate,
             // regardless of how it has been modified in the battle before this point;
            if (game.battle.oppPokemon.angry <= 0) {
                int baseCatchRate = game.battle.oppPokemon.baseStats.get("catchRate"); // or maxStats?
                game.battle.oppPokemon.currentStats.put("catchRate", baseCatchRate);
            }
            return;
        }
        else if (game.battle.oppPokemon.eating > 0) {
            String textString = "Wild " + game.battle.oppPokemon.name.toUpperCase() + " is eating!";
            game.insertAction(new DisplayText(game, textString, null, null, this.nextAction));
            game.actionStack.remove(this);

            game.battle.oppPokemon.eating--;
            return;
        }

        // made it here, so not eating or angry
        game.insertAction(this.nextAction);
        game.actionStack.remove(this);
        return;
    }

}

class SpecialBattleMegaGengar extends Action {
    public int layer = 107;
    Music music;

    boolean firstStep = true;

    int timer = 0;
    Music temp;
//    int timer = 1670; // TODO: debug, remove
    public SpecialBattleMegaGengar(Game game) {}
    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        if (this.firstStep) {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("battle/mgengar_battle_intro1.wav"));
            this.music.setLooping(false);
            // trying to balance this the attack sound effects mainly - they are pretty quiet
            this.music.setVolume(0.2f);
//            this.music.setVolume(0.0f); // TODO: debug, remove

            // TODO: sound manager object needs to do this
            game.currMusic.stop();
            game.currMusic.dispose();

            game.currMusic = this.music;
            game.currMusic.stop();
            game.currMusic.play();
//            game.currMusic.setPosition(28); // TODO: debug, remove

            game.currMusic.setOnCompletionListener(new Music.OnCompletionListener() {
                @Override
                public void onCompletion(Music aMusic) {
                    // fade normal battle music in
//                    Action nextMusic = new PlaySound("mgengar_battle1", null);
////                    PublicFunctions.insertToAS(PkmnGen.staticGame, nextMusic);
//                    nextMusic.step(PkmnGen.staticGame);
////                    nextMusic.step(PkmnGen.staticGame);

//                    synchronized (PkmnGen.staticGame) {
//                        try {
//                            PkmnGen.staticGame.wait(1);
//                        } catch (InterruptedException e) {
//                            // TODO Auto-generated catch block
//                            e.printStackTrace();
//                        }
                    // still a little bit of lag when playing
                    // note: lowering sample rate of songs didn't help
                    //  tried lowering for both songs
                    // looked at source, didn't see any better way than below
                    //  load from file, play(), pause(), play later
                    // still occasionally lags...
                    //  note: got more consistent after using .wav
                    //  now it's just a very slight lag
                    //  tried mp3 - slower than ogg and wav
//                    System.out.println(System.nanoTime());
//                    PkmnGen.staticGame.currMusic.stop(); // didn't help
//                    System.out.println(System.nanoTime());
//                    PkmnGen.staticGame.currMusic.dispose();
//                    System.out.println(System.nanoTime());
//                    System.out.println(System.nanoTime());
                    temp.play(); // timed this, takes the longest
//                    PkmnGen.staticGame.currMusic = temp;
//                    // lower lag, but still noticeable
//                    PkmnGen.staticGame.currMusic.setPosition(0f);
//                    PkmnGen.staticGame.currMusic.setVolume(0.9f);
//                    System.out.println(System.nanoTime());
//                    PkmnGen.staticGame.notify();
//                    }
                }
            });

            // TODO: needs to be fixed to use LinkedMusic.
            // pre-load battle music
//            temp = Gdx.audio.newMusic(Gdx.files.internal("battle/mgengar_battle1.wav"));
//            temp.setLooping(true);
//            temp.setVolume(0.2f);
//            temp.play();
//            temp.pause();
//            game.loadedMusic.put("mgengar_battle1", temp);

            this.firstStep = false;
        }

        // test
//        if (this.timer == 1) {
//            temp.pause();
////            temp.setPosition(0f);
//            temp.setVolume(0.9f);
//        }

        if (this.timer == 0) {
            game.actionStack.remove(game.displayTextAction);
            game.displayTextAction = null;

            SpecialMegaGengar1 gengar = new SpecialMegaGengar1(70);
            game.battle.oppPokemon = gengar;

            Action triggerAction = new PlaySound(game.player.currPokemon.name,
                                   new WaitFrames(game, 6,
                                   new DrawBattleMenuNormal(game, null)
                                   ));

//            Action nextAction =  new WaitFrames(game, 15,
            Action nextAction =  new BattleIntro(
                                 new SpecialBattleMegaGengar.BattleIntro1(
                                 new SplitAction(
                                         new SpecialBattleMegaGengar.DrawBattle1(game),
                                 new SplitAction(
                                         new SpecialBattleMegaGengar.DrawBreathingSprite(gengar),
                                 new SpecialBattleMegaGengar.IntroAnim(game,
//                                 new PlaySound(game.battle.oppPokemon.name,
//                                 new DisplayText(game, ""+game.battle.oppPokemon.name.toUpperCase()+" attacked!", null, null,
                                 new SplitAction(
                                         new WaitFrames(game, 1,
                                         new DrawEnemyHealth(game)
                                 ),
                                 new WaitFrames(game, 39,
                                 new MovePlayerOffScreen(game,
                                 new DisplayText(game, "Go! "+game.player.currPokemon.name.toUpperCase()+"!", null, triggerAction,
                                 new SplitAction(new DrawFriendlyHealth(game),
                                 new ThrowOutPokemon(game, // this draws pkmn sprite permanently until battle ends, ie until game.battle.drawAction == null
                                 triggerAction
                                 )))))))))));

            game.insertAction(nextAction);
        }
        // TODO: remove
//        else if (this.timer >= 771) {
//
//            game.currMusic.stop();
//
//            this.music = Gdx.audio.newMusic(Gdx.files.internal("battle/mgengar_battle1.wav"));
//            this.music.setLooping(true);
//            this.music.setVolume(0.9f);
////            this.music.setVolume(0.0f);  // TODO: debug, remove
//
//            game.currMusic = this.music;
//            game.currMusic.stop();
//            game.currMusic.play();
//
//            game.actionStack.remove(this);
//        }
        this.timer++;
    }

    class BattleIntro1 extends Action {
        ArrayList<Sprite> frames;
        Sprite frame;

        public int layer = 139;

        public BattleIntro1(Action nextAction) {
            this.nextAction = nextAction;
            this.frames = new ArrayList<Sprite>();
            Texture text1 = new Texture(Gdx.files.internal("battle/battle_intro_anim1_sheet1.png"));

            // animation to play
            for (int i = 0; i < 28; i++) {
                frames.add(new Sprite(text1, i*160, 0, 160, 144));
            }

            for (int i = 0; i < 42*3; i++) {
                frames.add(new Sprite(text1, 27*160, 0, 160, 144));
            }
        }

        public String getCamera() {return "gui";}

        public int getLayer(){return this.layer;}

        @Override
        public void step(Game game) {
            // get next frame
            this.frame = frames.get(0);

            if (this.frame != null) {
                // gui version
                    // this.frame.setScale(3); // scale doesn't work in batch.draw // used these when scaling mattered
                    // this.frame.setPosition(16*10,16*9);
                this.frame.draw(game.uiBatch);
                // map version
                // game.batch.draw(this.frame, 16, -16);
            }

            frames.remove(0);

            // if done with anim, do nextAction
            if (frames.isEmpty()) {
                game.insertAction(this.nextAction);
                game.actionStack.remove(this);
                // avoid lag
                this.nextAction.step(game);
                return;
            }
        }
    }

    class DrawBattle1 extends DrawBattle {
        public int layer = 130;
        Sprite bgSprite;

        public DrawBattle1(Game game) {
            super(game);
//            Texture text = new Texture(Gdx.files.internal("battle/battle_bg3.png"));
//            this.bgSprite = new Sprite(text, 0, 0, 160, 144);
        }

        public String getCamera() {return "gui";}

        public int getLayer(){return this.layer;}

        @Override
        public void step(Game game) {
            // debug
//            this.helperSprite.draw(game.floatingBatch);

            // need bg under the animation
//            this.bgSprite.draw(game.floatingBatch);

            // TODO: remove
//            if (shouldDrawOppPokemon) {
//                game.battle.oppPokemon.sprite.draw(game.floatingBatch);
//            }
            game.player.battleSprite.draw(game.uiBatch);
        }
    }

    // draw sprite and move it up and down
    static class DrawBreathingSprite extends Action {
        // set true when should start breathing
        static boolean shouldBreathe = false;
        public int layer = 131;

        SpecialMegaGengar1 gengar;

        int timer = 300;

        int offsetY = 0;
        int offsetY2 = 0;
        Sprite bgSprite;

        public DrawBreathingSprite(SpecialMegaGengar1 gengar) {
            this.gengar = gengar;
            Texture text = new Texture(Gdx.files.internal("battle/battle_bg3.png"));
            this.bgSprite = new Sprite(text, 0, 0, 160, 144);
        }
        public String getCamera() {return "gui";}

        public int getLayer(){return this.layer;}

        @Override
        public void step(Game game) {
            // TODO: debug, delete
//            this.shouldBreathe = true;
            if (DrawBreathingSprite.shouldBreathe) {
                this.timer--;
                if (this.timer == 74) {
                    this.offsetY2 = -1;
                }
                else if (this.timer == 274){
                    this.offsetY2 = 0;
                }
                if (this.timer == 149) {
                    this.offsetY = -1;
                }
                else if (this.timer == 0){
                    this.timer = 300;
                    this.offsetY = 0;
                }
            }

            // need bg under the animation
            this.bgSprite.draw(game.uiBatch);

            // TODO: this needs to be above draw enemy sprite
            //

            // always draw sprite relative to other sprite
//            game.floatingBatch.draw(this.mewtwo.breathingSprite,
//                                    this.mewtwo.sprite.getX(),
//                                    this.mewtwo.sprite.getY() + this.offsetY);
            // breathing sprite is on top of base in this instance
            this.gengar.breathingSprite.setPosition(this.gengar.sprite.getX(), this.gengar.sprite.getY() - this.offsetY);
            this.gengar.breathingSprite.draw(game.uiBatch);
            // annoying workaround
            this.gengar.sprite.setPosition(this.gengar.sprite.getX(), this.gengar.sprite.getY() - this.offsetY2);
            this.gengar.sprite.draw(game.uiBatch);
            this.gengar.sprite.setPosition(this.gengar.sprite.getX(), this.gengar.sprite.getY() + this.offsetY2);

            if (game.battle.drawAction == null) {
                game.actionStack.remove(this);
                return;
            }
        }
    }

    // scroll both players into view
    // shader animation
    // set shouldBreathe
    static class IntroAnim extends Action {
        public int layer = 140;
        ArrayList<Vector2> moves_relative;
        Vector2 move;

        int timer = 0;
        String vertexShader;
        boolean firstStep = true;

        public IntroAnim(Game game, Action nextAction) {
            this.nextAction = nextAction;
            this.moves_relative = new ArrayList<Vector2>();

            // animation to play
            for (int i = 0; i < 72*2; i++) {
                moves_relative.add(new Vector2(1,0));
            }

            game.player.battleSprite.setPosition(175+1-8-2,71+1-10);
//            game.player.battleSprite.setScale(2);  // TODO: this was for black-white sprite... not sure what to do.
            game.battle.oppPokemon.sprite.setPosition(-30-4-1-14,106+2-5-15);

            this.vertexShader = "attribute vec4 a_position;\n"
                    + "attribute vec4 a_color;\n"
                    + "attribute vec2 a_texCoord;\n"
                    + "attribute vec2 a_texCoord0;\n"

                    + "uniform mat4 u_projTrans;\n"

                    + "varying vec4 v_color;\n"
                    + "varying vec2 v_texCoords;\n"

                    + "void main()\n"
                    + "{\n"
                    + "    v_color = a_color;\n"
                    + "    v_texCoords = a_texCoord0;\n"
                    + "    gl_Position =  u_projTrans * a_position;\n"
                    // below be used to translate screen pixels (for attacks, etc
//                    + "    gl_Position =  u_projTrans * vec4(a_position.x, a_position.y, a_position.z, 1.0);\n"
                    + "}\n";
        }
        public String getCamera() {return "gui";}

        public int getLayer(){return this.layer;}

        String getShader(float level) {
            String shader = "precision mediump float;\n"

                            + "varying vec4 v_color;\n"
                            + "varying vec2 v_texCoords;\n"
                            + "uniform sampler2D u_texture;\n"
                            + "uniform mat4 u_projTrans;\n"

                            + "bool equals(float a, float b) {\n"
                            + "    return abs(a-b) < 0.0001;\n"
                            + "}\n"

                            + "void main() {\n"
                            + "    vec4 color = texture2D (u_texture, v_texCoords) * v_color;\n"

                            + "    float level = "+level+";\n" // can't do +- or -+ inline
                            + "    if (color.r >= .9 && color.g >= .9 && color.b >= .9) {\n"
                            + "           color = vec4(color.r, color.g, color.b, color.a);\n"
                            + "    }\n"
                            + "    else {\n"
                            + "        color = vec4(color.r-level, color.g-level, color.b-level, color.a);\n"
                            + "    }\n"
                            + "    gl_FragColor = color;\n"
                            + "}\n";

            // version that keeps everything black/white (didnt like as much)
//            String shader = "precision mediump float;\n"
//
//                            + "varying vec4 v_color;\n"
//                            + "varying vec2 v_texCoords;\n"
//                            + "uniform sampler2D u_texture;\n"
//                            + "uniform mat4 u_projTrans;\n"
//
//                            + "bool equals(float a, float b) {\n"
//                            + "    return abs(a-b) < 0.0001;\n"
//                            + "}\n"
//
//                            + "void main() {\n"
//                            + "    vec4 color = texture2D (u_texture, v_texCoords) * v_color;\n"
//
//                            + "    float level = ("+level+"+1.0)/2.0;\n" // can't do +- or -+ inline
//                            + "    if (color.r >= .9 && color.g >= .9 && color.b >= .9) {\n"
//                            + "           color = vec4(color.r, color.g, color.b, color.a);\n"
//                            + "    }\n"
//                            + "    else if (color.r < level || color.g < level || color.b < level) {\n"
//                            + "        color = vec4(0, 0, 0, color.a);\n"
//                            + "    }\n"
//                            + "    else {\n"
//                            + "        color = vec4(1, 1, 1, color.a);\n"
//                            + "    }\n"
//                            + "    gl_FragColor = color;\n"
//                            + "}\n";
            return shader;
        }

        @Override
        public void step(Game game) {
            if (this.firstStep) {
                // TODO: will fail if WebGL (maybe LibGDX has fixed this?)
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(0.8f));
                game.uiBatch.setShader(shader);
                this.firstStep = false;
            }

            if (!moves_relative.isEmpty()) {
                // get next frame
                this.move = moves_relative.get(0);

                float xPos = game.player.battleSprite.getX() - move.x;//*3;
                game.player.battleSprite.setX(xPos);

                xPos = game.battle.oppPokemon.sprite.getX() + move.x;//*3;
                game.battle.oppPokemon.sprite.setX(xPos);

                moves_relative.remove(0);

                // TODO: remove
//                if (moves_relative.isEmpty()) {
////                    game.insertAction(new DisplayTextIntro(game, "An oppressive force fills the air...",
////                                                                          null, null, false, null));
//
//                }
                return;
            }

//            if (this.timer == 0) {
//                // white screen 'flash'
//                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-1f));
//                game.floatingBatch.setShader(shader);
//            }
//            if (this.timer == 2) {
//                // white screen 'flash'
//                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(0f));
//                game.floatingBatch.setShader(shader);
//            }

            // start next action (pokemon cry) but continue animation
            if (this.timer == 30) {
                // mega gengar cry
                game.insertAction(new PlaySound(game.battle.oppPokemon.name,
                                                 null));
            }

            // TODO: remove
//            if (this.timer == 380) {
//                // remove the text box
//                game.actionStack.remove(game.displayTextAction);
//                game.displayTextAction = null;
//                game.insertAction(new DisplayTextIntro(game, "MEWTWO unleashes its full power!",
//                                                                      null, null, false, null));
//            }

            // start of flash anim
            else if (this.timer == 20) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(0.7f));
                game.uiBatch.setShader(shader);
            }
            else if (this.timer == 20 + 24) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(0.6f));
                game.uiBatch.setShader(shader);
            }
            else if (this.timer == 20 + 24*2) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(0.4f));
                game.uiBatch.setShader(shader);
            }
            else if (this.timer == 20 + 24*3) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(0.2f));
                game.uiBatch.setShader(shader);
            }
            else if (this.timer == 20 + 24*4) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(0f));
                game.uiBatch.setShader(shader);
            }
//            else if (this.timer == 490 + 6*4) {
//                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-.2f));
//                game.floatingBatch.setShader(shader);
//            }
//            else if (this.timer == 490 + 6*5) {
//                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-.4f));
//                game.floatingBatch.setShader(shader);
//            }
//            else if (this.timer == 490 + 6*6) {
//                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-.6f));
//                game.floatingBatch.setShader(shader);
//            }
//            else if (this.timer == 490 + 6*7) {
//                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-.8f));
//                game.floatingBatch.setShader(shader);
//            }
//            else if (this.timer == 490 + 6*8) {
//                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-.85f));
//                game.floatingBatch.setShader(shader);
//            }
//            else if (this.timer == 490 + 6*9) {
//                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-.90f));
//                game.floatingBatch.setShader(shader);
//            }
//            else if (this.timer == 490 + 6*10) {
//                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-.95f));
//                game.floatingBatch.setShader(shader);
//            }
//            else if (this.timer == 490 + 6*11) {
//                // stop displaying text box
//                game.actionStack.remove(game.displayTextAction);
//                game.displayTextAction = null;
//
//                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-1f));
//                game.floatingBatch.setShader(shader);
//            }
//            else if (this.timer == 490 + 6*19 + 3) {
//                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-.5f));
//                game.floatingBatch.setShader(shader);
//            }
//            else if (this.timer == 490 + 6*19 + 5) {
//                game.floatingBatch.setShader(null);
//            }
//            else if (this.timer == 620) {
//                SpecialBattleMegaGengar.DrawBreathingSprite.shouldBreathe = true;
//            }

            // battle text
            if (this.timer == 20) {
                game.insertAction(new DisplayTextIntro(game, "MEGA GENGAR attacked!",
                                                                      null, null, false, null));
            }

            // start of switch to pokemon
            if (this.timer == 120 + 24*4) {
                game.insertAction(this.nextAction);
            }

            if (this.timer >= 150 + 24*4) {
    //            // remove the text box
                game.actionStack.remove(game.displayTextAction);
                game.displayTextAction = null;
                game.actionStack.remove(this);
                SpecialBattleMegaGengar.DrawBreathingSprite.shouldBreathe = true;
            }
            this.timer++;
        }
    }
}

class SpecialBattleMewtwo extends Action {
    public int layer = 107;
    Music music;
    SpecialMewtwo1 mewtwo;
    boolean firstStep = true;
    public static boolean doneYet = false;
    public static int specialAttackCounter = 0;  // wait N turns until doing special attack
    int timer = 0;

    public SpecialBattleMewtwo(Game game, SpecialMewtwo1 mewtwo) {
        this.mewtwo = mewtwo;
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        if (this.firstStep) {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("battle/pokemon_mansion_remix_eq.ogg"));
            this.music.setLooping(true);
//            this.music.setVolume(0.9f);
            this.music.setVolume(0.7f);

            game.currMusic = this.music;
//            game.currMusic.stop();  // needed? might mess w/ volume
            game.currMusic.play();
            
            // debug
            if (SpecialBattleMewtwo.doneYet) {
                this.timer = 1708;  //1670;  // TODO: this is probably off now
//                game.currMusic.setPosition(28); // TODO: debug, remove
                game.currMusic.setPosition(28.55f);
            }
            SpecialBattleMewtwo.doneYet = true;
            SpecialBattleMewtwo.specialAttackCounter = 0;
            this.firstStep = false;
        }

        if (this.timer == 0) {
            // remove the text box
//            game.actionStack.remove(game.displayTextAction);
//            game.displayTextAction = null;
//            game.insertAction(new DisplayTextIntro(game, "...",
//                                                                  null, null, false, null));
        }
        else if (this.timer == 100) {
            // remove the text box
            game.actionStack.remove(game.displayTextAction);
            game.displayTextAction = null;
            game.insertAction(new DisplayTextIntro(game, "Humans...",
                                                                  null, null, false, null));
        }
        else if (this.timer == 300 -20) {
            // remove the text box
            game.actionStack.remove(game.displayTextAction);
            game.displayTextAction = null;
            game.insertAction(new DisplayTextIntro(game, "They cared nothing for me...",  //These humans cared nothing for me
                                                   null, null, false, null));
        }
        else if (this.timer == 600 -20 -20) {
            // remove the text box
            game.actionStack.remove(game.displayTextAction);
            game.displayTextAction = null;
            game.insertAction(new DisplayTextIntro(game, "From the moment I first opened my eyes, they have sought to control me...",
                                                                  null, null, false, null));
        }
        else if (this.timer == 1000 -20 -20) {
            // remove the text box
            game.actionStack.remove(game.displayTextAction);
            game.displayTextAction = null;
            game.insertAction(new DisplayTextIntro(game, "But no more.",
                                                                  null, null, false, null));
        }
        else if (this.timer == 1200 -20 -20) {
            // remove the text box
            game.actionStack.remove(game.displayTextAction);
            game.displayTextAction = null;
            game.insertAction(new DisplayTextIntro(game, "Why are you here?",
                                                                  null, null, false, null));
        }
        else if (this.timer == 1350 -20 -20) {
            // Remove the text box
            game.actionStack.remove(game.displayTextAction);
            game.displayTextAction = null;
            game.insertAction(new DisplayTextIntro(game, "You seek to control me, just like the others.",  //desire
                                                   null, null, false, null));
        }
        else if (this.timer == 1580) {
            // Remove the text box
            game.actionStack.remove(game.displayTextAction);
            game.displayTextAction = null;
            game.insertAction(new DisplayTextIntro(game, "...", null, null, false, null));
        }

        if (this.timer >= 1708) {  // >= 1685  // was this value. TODO: remove after tested.
            game.actionStack.remove(game.displayTextAction);
            game.displayTextAction = null;

            // TODO: remove
//            SpecialMewtwo1 mewtwo = new SpecialMewtwo1(70);
//            SpecialMewtwo1 mewtwo = new SpecialMewtwo1(50);
//            game.battle.oppPokemon = mewtwo;

            // TODO: this was for gen1 pokemon
//            Action triggerAction = new PlaySound(game.player.currPokemon, //game.player.currPokemon.name,
//                                   new WaitFrames(game, 6,
//                                   new DrawBattleMenuNormal(game, null)
//                                   ));
            

            Action afterTrigger = new WaitFrames(game, 15,
                                  new DrawBattleMenuNormal(game,
                                  null
                                  ));
            Action triggerAction = new PlaySound(game.player.currPokemon,
                                   new WaitFrames(game, 6,
                                   new DrawFriendlyHealth(game,
                                   afterTrigger)));
//            Action nextAction =  new WaitFrames(game, 15,  // TODO: delete
            Action nextAction =  new BattleIntro(
                                 new SpecialBattleMewtwo.BattleIntro1(
                                 new SplitAction(
                                         new SpecialBattleMewtwo.DrawBattle1(game),
                                 new SplitAction(
                                         new SpecialBattleMewtwo.DrawBreathingSprite(this.mewtwo),
                                 new SplitAction(
                                         new SpecialBattleMewtwo.RocksEffect2(),
                                 new SpecialBattleMewtwo.IntroAnim(game,
                                 new SplitAction(
                                         new SpecialBattleMewtwo.RocksEffect1(),
                                 new SplitAction(
                                         new SpecialBattleMewtwo.RippleEffect1(),
                                 new PlaySound(game.battle.oppPokemon.name,  // TODO: cry not working for dex num 150
//                                 new PlaySound(game.battle.oppPokemon,  // TODO: remove
                                 new DisplayText(game, "Wild "+game.battle.oppPokemon.name.toUpperCase()+" appeared!", null, null,
                                 new SplitAction(
                                         new WaitFrames(game, 1,
                                         new DrawEnemyHealth(game)
                                 ),
                                 new WaitFrames(game, 39,
                                 new MovePlayerOffScreen(game,
                                 new DisplayText(game, "Go! "+game.player.currPokemon.name.toUpperCase()+"!", null, triggerAction,
//                                 new SplitAction(  // TODO: gen 1 animation. remove
//                                     new DrawFriendlyHealth(game),
                                 // TODO: this was for gen1
//                                 new ThrowOutPokemon(game,
//                                 triggerAction
                                 new ThrowOutPokemonCrystal(game,
                                 game.player.currPokemon.isShiny ?
                                     new Battle.LoadAndPlayAnimation(game, "shiny", game.battle.oppPokemon,
                                     triggerAction)
                                 :
                                     triggerAction
                                 )))))))))))))));
            game.actionStack.remove(this);
            game.insertAction(nextAction);
        }
        this.timer++;
    }

    class BattleIntro1 extends Action {
        ArrayList<Sprite> frames;
        Sprite frame;

        public int layer = 139;

        public BattleIntro1(Action nextAction) {
            this.nextAction = nextAction;
            this.frames = new ArrayList<Sprite>();
            Texture text1 = new Texture(Gdx.files.internal("battle/battle_intro_anim1_sheet1.png"));

            // animation to play
            for (int i = 0; i < 28; i++) {
                frames.add(new Sprite(text1, i*160, 0, 160, 144));
            }

            for (int i = 0; i < 42*3; i++) {
                frames.add(new Sprite(text1, 27*160, 0, 160, 144));
            }
        }

        public String getCamera() {return "gui";}

        public int getLayer(){return this.layer;}

        @Override
        public void step(Game game) {
            // if done with anim, do nextAction
            if (frames.isEmpty()) {
                game.insertAction(this.nextAction);
                game.actionStack.remove(this);
                // avoid lag
                nextAction.step(game);
                return;
            }

            // get next frame
            this.frame = frames.get(0);

            if (this.frame != null) {
                // gui version
                    // this.frame.setScale(3); // scale doesn't work in batch.draw // used these when scaling mattered
                    // this.frame.setPosition(16*10,16*9);
                this.frame.draw(game.uiBatch);
                // map version
                // game.batch.draw(this.frame, 16, -16);
            }

            frames.remove(0);
        }
    }

    class DrawBattle1 extends DrawBattle {
        public int layer = 130;

        public DrawBattle1(Game game) {
            super(game);
        }

        public String getCamera() {return "gui";}

        public int getLayer(){return this.layer;}

        @Override
        public void step(Game game) {
//            this.bgSprite.draw(game.floatingBatch);
            // debug
//            this.helperSprite.draw(game.floatingBatch);

            if (shouldDrawOppPokemon) {
                game.battle.oppPokemon.sprite.draw(game.uiBatch);
            }
            game.player.battleSprite.draw(game.uiBatch);
        }
    }

    // draw sprite and move it up and down
    static class DrawBreathingSprite extends Action {
        // Set true when should start breathing
        static boolean shouldBreathe = false;
        public int layer = 129;
        Sprite bgSprite2;
        SpecialMewtwo1 mewtwo;
        int timer = 300;
        int offsetY = 0;

        public DrawBreathingSprite(SpecialMewtwo1 mewtwo) {
            this.mewtwo = mewtwo;
            Texture text = new Texture(Gdx.files.internal("battle/intro_frame6.png"));
            this.bgSprite2 = new Sprite(text, 0, 0, 160, 144);
        }
        public String getCamera() {return "gui";}

        public int getLayer(){return this.layer;}

        @Override
        public void step(Game game) {
            // TODO: debug, delete
//            this.shouldBreathe = true;
            if (DrawBreathingSprite.shouldBreathe) {
                this.timer--;
                if (this.timer == 149) {
                    this.offsetY = -1;
                }
                else if (this.timer == 0){
                    this.timer = 300;
                    this.offsetY = 0;
                }
            }

            // TODO: this needs to be above draw enemy sprite
            //

            // always draw sprite relative to other sprite
//            game.floatingBatch.draw(this.mewtwo.breathingSprite,
//                                    this.mewtwo.sprite.getX(),
//                                    this.mewtwo.sprite.getY() + this.offsetY);
            // TODO: remove
//            float alpha = this.mewtwo.sprite.getColor().a;
//            System.out.println(alpha);
//            this.mewtwo.breathingSprite.setAlpha(alpha);
            if (DrawBattle.shouldDrawOppPokemon) {
                this.mewtwo.breathingSprite.setPosition(this.mewtwo.sprite.getX(), this.mewtwo.sprite.getY() + this.offsetY);
                this.mewtwo.breathingSprite.draw(game.uiBatch);
            }
            for (int i = -1; i < 2; i+= 1) {
                for (int j = -1; j < 2; j+= 1) {
                    if (i == 0 && j == 0) {
                        continue;
                    }
                    game.uiBatch.draw(this.bgSprite2, 160*i, 144*j);
                }
            }
            if (game.battle.drawAction == null) {
                game.actionStack.remove(this);
                return;
            }
        }
    }

    // scroll both players into view
    // shader animation
    // set shouldBreathe
    static class IntroAnim extends Action {
        ArrayList<Vector2> moves_relative;
        Vector2 move;

        public int layer = 140;
        int timer = 0;

        String vertexShader;

        public IntroAnim(Game game, Action nextAction) {
            this.nextAction = nextAction;
            this.moves_relative = new ArrayList<Vector2>();

            // animation to play
            for (int i = 0; i < 72*2; i++) {
                moves_relative.add(new Vector2(1,0));
            }

//            game.player.battleSprite.setPosition(175+1-8-2,71+1-10);  // TODO: this was gen1 backsprite
            game.player.battleSprite.setPosition(162, 49);
//            game.player.battleSprite.setScale(2);
            game.battle.oppPokemon.sprite.setPosition(-30-4-1-14,106+2-5-15);

            this.vertexShader = "attribute vec4 a_position;\n"
                    + "attribute vec4 a_color;\n"
                    + "attribute vec2 a_texCoord;\n"
                    + "attribute vec2 a_texCoord0;\n"

                    + "uniform mat4 u_projTrans;\n"

                    + "varying vec4 v_color;\n"
                    + "varying vec2 v_texCoords;\n"

                    + "void main()\n"
                    + "{\n"
                    + "    v_color = a_color;\n"
                    + "    v_texCoords = a_texCoord0;\n"
                    + "    gl_Position =  u_projTrans * a_position;\n"
                    // below can be used to translate screen pixels (for attacks, etc
//                    + "    gl_Position =  u_projTrans * vec4(a_position.x, a_position.y, a_position.z, 1.0);\n"
                    + "}\n";
        }
        public String getCamera() {return "gui";}

        public int getLayer(){return this.layer;}

        String getShader(float level) {
            String shader = "precision mediump float;\n"

                            + "varying vec4 v_color;\n"
                            + "varying vec2 v_texCoords;\n"
                            + "uniform sampler2D u_texture;\n"
                            + "uniform mat4 u_projTrans;\n"

                            + "bool equals(float a, float b) {\n"
                            + "    return abs(a-b) < 0.0001;\n"
                            + "}\n"

                            + "void main() {\n"
                            + "    vec4 color = texture2D (u_texture, v_texCoords) * v_color;\n"

                            + "    float level = "+level+";\n" // can't do +- or -+ inline
                            + "    if (color.r >= .9 && color.g >= .9 && color.b >= .9) {\n"
                            + "           color = vec4(color.r, color.g, color.b, color.a);\n"
                            + "    }\n"
                            + "    else {\n"
                            + "        color = vec4(color.r-level, color.g-level, color.b-level, color.a);\n"
                            + "    }\n"
                            + "    gl_FragColor = color;\n"
                            + "}\n";

            // version that keeps everything black/white (didnt like as much)
//            String shader = "precision mediump float;\n"
//
//                            + "varying vec4 v_color;\n"
//                            + "varying vec2 v_texCoords;\n"
//                            + "uniform sampler2D u_texture;\n"
//                            + "uniform mat4 u_projTrans;\n"
//
//                            + "bool equals(float a, float b) {\n"
//                            + "    return abs(a-b) < 0.0001;\n"
//                            + "}\n"
//
//                            + "void main() {\n"
//                            + "    vec4 color = texture2D (u_texture, v_texCoords) * v_color;\n"
//
//                            + "    float level = ("+level+"+1.0)/2.0;\n" // can't do +- or -+ inline
//                            + "    if (color.r >= .9 && color.g >= .9 && color.b >= .9) {\n"
//                            + "           color = vec4(color.r, color.g, color.b, color.a);\n"
//                            + "    }\n"
//                            + "    else if (color.r < level || color.g < level || color.b < level) {\n"
//                            + "        color = vec4(0, 0, 0, color.a);\n"
//                            + "    }\n"
//                            + "    else {\n"
//                            + "        color = vec4(1, 1, 1, color.a);\n"
//                            + "    }\n"
//                            + "    gl_FragColor = color;\n"
//                            + "}\n";
            return shader;
        }

        @Override
        public void step(Game game) {
//            if (this.timer < 60 || this.timer > 700) {
//            }
            if (!moves_relative.isEmpty()) {
                // get next frame
                this.move = moves_relative.get(0);

                float xPos = game.player.battleSprite.getX() - move.x;//*3;
                game.player.battleSprite.setX(xPos);

                xPos = game.battle.oppPokemon.sprite.getX() + move.x;//*3;
                game.battle.oppPokemon.sprite.setX(xPos);

                moves_relative.remove(0);

                if (moves_relative.isEmpty()) {
                    game.insertAction(new DisplayTextIntro(game, "An oppressive force surrounds you...",
                                                                          null, null, false, null));
                }
            }

            if (this.timer == 380) {
                // remove the text box
                game.actionStack.remove(game.displayTextAction);
                game.displayTextAction = null;
                game.insertAction(new DisplayTextIntro(game, "MEWTWO unleashes its full power!",
                                                                      null, null, false, null));
            }

            if (this.timer == 0) {
                // TODO: will fail if WebGL (maybe LibGDX has fixed this?)
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(0.8f));
                game.uiBatch.setShader(shader);
//                game.battle.oppPokemon.sprite.setColor(0.2f, .2f, .2f, .2f);
            }

            if (this.timer == 490) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(0.6f));
                game.uiBatch.setShader(shader);
            }
            else if (this.timer == 490 + 6) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(0.4f));
                game.uiBatch.setShader(shader);
            }
            else if (this.timer == 490 + 6*2) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(0.2f));
                game.uiBatch.setShader(shader);
            }
            else if (this.timer == 490 + 6*3) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(0f));
                game.uiBatch.setShader(shader);
            }
            else if (this.timer == 490 + 6*4) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-0.2f));
                game.uiBatch.setShader(shader);
            }
            else if (this.timer == 490 + 6*5) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-0.4f));
                game.uiBatch.setShader(shader);
            }
            else if (this.timer == 490 + 6*6) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-0.6f));
                game.uiBatch.setShader(shader);
            }
            else if (this.timer == 490 + 6*7) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-0.8f));
                game.uiBatch.setShader(shader);
            }
            else if (this.timer == 490 + 6*8) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-0.85f));
                game.uiBatch.setShader(shader);
            }
            else if (this.timer == 490 + 6*9) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-0.90f));
                game.uiBatch.setShader(shader);
            }
            else if (this.timer == 490 + 6*10) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-0.95f));
                game.uiBatch.setShader(shader);
            }
            else if (this.timer == 490 + 6*11) {
                // stop displaying text box
                game.actionStack.remove(game.displayTextAction);
                game.displayTextAction = null;

                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-1f));
                game.uiBatch.setShader(shader);
            }
            else if (this.timer == 490 + 6*19 + 3) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-0.5f));
                game.uiBatch.setShader(shader);
            }
            else if (this.timer == 490 + 6*19 + 5) {
                game.uiBatch.setShader(null);
            }
            else if (this.timer == 620) {
                SpecialBattleMewtwo.DrawBreathingSprite.shouldBreathe = true;
            }

            if (this.timer >= 640) {
                game.actionStack.remove(this);
                game.insertAction(this.nextAction);
            }
            this.timer++;
        }
    }

    class RippleEffect1 extends Action {
        public int layer = 109;
        ShaderProgram shader;

        String fragShader;

        int yPos = -16;
        Pixmap pixmap;
        Sprite sprite;

        //        int[] offsets = new int[] {2, 1, 0, -1, -2, -1, 0, 1};
        int[] offsets = new int[] {0, 0, 0, 1, 2, 2, 2, 3, 4, 4, 4, 3, 2, 2, 2, 1};

        public RippleEffect1() {
            Texture text = new Texture(Gdx.files.internal("battle/pixel1.png"));
            this.sprite = new Sprite(text, 0, 0, 1, 1);

            // TODO: didn't use
            this.fragShader = "precision mediump float;\n"
                            + "varying vec4 v_color;\n"
                            + "varying vec2 v_texCoords;\n"
                            + "uniform sampler2D u_texture;\n"
                            + "uniform mat4 u_projTrans;\n"

                            + "bool equals(float a, float b) {\n"
                            + "    return abs(a-b) < 0.0001;\n"
                            + "}\n"

                            + "void main() {\n"
                            + "    vec4 color = texture2D (u_texture, v_texCoords) * v_color;\n"
                            + "    gl_FragColor = color;\n"
                            + "}\n";
        }
public String getCamera() {return "gui";}

        public int getLayer(){return this.layer;}

        String getVertexShader(int timer) {
            // used to translate pixels over
            String vertexShader = "attribute vec4 a_position;\n"
                    + "attribute vec4 a_color;\n"
                    + "attribute vec2 a_texCoord;\n"
                    + "attribute vec2 a_texCoord0;\n"

                    + "uniform mat4 u_projTrans;\n"

                    + "varying vec4 v_color;\n"
                    + "varying vec2 v_texCoords;\n"

                    + "void main()\n"
                    + "{\n"
                    + "    v_color = a_color;\n"
                    + "    v_texCoords = a_texCoord0;\n"
                    + "    int timer = "+timer+";\n"
                    + "    int offset = (timer + int(a_position.y)) % 16;\n"
//                    + "    offset = offset/2;\n"
//                    + "    if (int(a_position.y) >= timer && int(a_position.y) < timer + 16) {\n"
                    + "        if (offset == 0 || offset == 4) {offset = 0;}\n"
                    + "        else if (offset == 1 || offset == 3) {offset = 1;}\n"
                    + "        else if (offset == 2) {offset = 2;}\n"
                    + "        else if (offset == 5 || offset == 7) {offset = -1;}\n"
                    + "        else if (offset == 6) {offset = -2;}\n"
//                    + "    }\n"
                    // below can be used to translate screen pixels (for attacks, etc
                    + "    gl_Position =  u_projTrans * vec4(a_position.x + offset, a_position.y, a_position.z, 1.0);\n"
                    + "}\n";

            return vertexShader;
        }

        @Override
        public void step(Game game) {
            // TODO: why isn't breathing sprite drawn?

            if (this.yPos <= 144) {
//                this.pixmap = ScreenUtils.getFrameBufferPixmap(0, this.yPos, 160, 16); // looked neat

                // TODO: *3 b/c screen is scaled, alternative approach would be nice
                this.pixmap = ScreenUtils.getFrameBufferPixmap(0, this.yPos*3, 160*3, 16*3);
                for (int j=0; j < 16; j++) {
                    for (int i=0; i < 160; i++) {
//                        this.sprite.setColor(this.pixmap.getPixel(i*3, j*3)); // trippy colors
                        this.sprite.setColor(new Color(this.pixmap.getPixel(i*3, j*3)));
                        this.sprite.setPosition(i + this.offsets[j], j + this.yPos);
                        this.sprite.draw(game.uiBatch);
                    }
                }
            }

//            this.shader = new ShaderProgram(this.getVertexShader(this.yPos), this.fragShader);
//            game.floatingBatch.setShader(shader);

            if (this.yPos > 144*4) {
                this.yPos = 0;
            }
            if (this.yPos % 2 == 0) {
                this.yPos+=3;
            }
            else {
                this.yPos+=4;
            }

            if (game.battle.drawAction == null) {
                game.actionStack.remove(this);
                return;
            }
        }
    }

    static class RocksEffect1 extends Action {
        public static int velocityX = 0;
        public static boolean shouldMoveX = false;

        public static boolean shouldMoveY = true;

        public int layer = 111;
        Sprite textboxSprite;
        Sprite[] sprites = new Sprite[10];
        // flip-flop between these two velocities
        int[] velocities = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        int[] velocities2 = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        int velocity = 1;
        int whichVelocity = 0;
        Random rand = new Random();
        boolean firstStep = true;

        public RocksEffect1() {
            Texture text = new Texture(Gdx.files.internal("battle/battle_bg4.png"));
            this.textboxSprite = new Sprite(text, 0, 0, 176, 160);
            this.textboxSprite.setPosition(-8, -8);

            text = new Texture(Gdx.files.internal("battle/rock1.png"));
            this.sprites[0] = new Sprite(text, 0*32, 0, 32, 32);
            this.sprites[0].setColor(1f, 1f, 1f, 1f);
            this.sprites[1] = new Sprite(text, 1*32, 0, 32, 32);
            this.sprites[2] = new Sprite(text, 2*32, 0, 32, 32);
            this.sprites[3] = new Sprite(text, 5*32, 0, 32, 32);
            this.sprites[4] = new Sprite(text, 6*32, 0, 32, 32);
            this.sprites[5] = new Sprite(text, 1*32, 0, 32, 32);
            this.sprites[6] = new Sprite(text, 2*32, 0, 32, 32);
            this.sprites[7] = new Sprite(text, 5*32, 0, 32, 32);
            this.sprites[8] = new Sprite(text, 6*32, 0, 32, 32);
            this.sprites[9] = new Sprite(text, 0*32, 0, 32, 32);
            this.sprites[9].setColor(1f, 1f, 1f, 1f);

            for (int i=0; i < 10; i++) {
                this.sprites[i].setPosition(rand.nextInt(160-32), rand.nextInt(144) - 144);
                this.velocities[i] = rand.nextInt(2) + 1;
                this.velocities2[i] = this.velocities[i] -1 +rand.nextInt(2);
//                this.sprites[i].setRotation(rand.nextInt(4) * 90);
            }
        }
        public String getCamera() {return "gui";}

        public int getLayer(){return this.layer;}

        @Override
        public void step(Game game) {
            if (this.firstStep) {
                SpecialBattleMewtwo.RocksEffect2.drawRocks = true;
                this.firstStep = false;
            }

            for (int i=0; i < 10; i++) {
                if (this.whichVelocity == 0) {
                    this.velocity = this.velocities[i];
                }
                else {
                    this.velocity = this.velocities2[i];
                }
                if (!RocksEffect1.shouldMoveY) {
                    this.velocity = 0;
                }

                this.sprites[i].setPosition(this.sprites[i].getX() + RocksEffect1.velocityX,
                                            this.sprites[i].getY() + this.velocity);
                if (this.sprites[i].getY() > 144) {
                    this.sprites[i].setPosition(rand.nextInt(160-32), rand.nextInt(144) - 144);
//                    this.sprites[i].setRotation(rand.nextInt(4) * 90);
                    this.velocities[i] = rand.nextInt(2) + 1;
                    this.velocities2[i] = this.velocities[i] -1 +rand.nextInt(2);
                }
                if (this.sprites[i].getX() < 0) {
                    this.sprites[i].setPosition(160, rand.nextInt(144) - 32);
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
}

// draws rock flying through air and hitting opposing pkmn
class ThrowBait extends Action {
    // remove
    Sprite baitSprite;

    ArrayList<Vector2> positions;
    Vector2 position;
    ArrayList<Sprite> sprites;
    Sprite sprite;
    ArrayList<Integer> frames;
    String sound;
    ArrayList<String> sounds;

    public int layer = 120;
    Sprite helperSprite; // just for helping me position the animation. delete later.

    public ThrowBait(Game game, Action nextAction) {
        this.nextAction = nextAction;

        Texture text = new Texture(Gdx.files.internal("throw_rock_anim/bait_small1.png"));
        this.baitSprite = new Sprite(text, 0, 0, 8, 8);

        // positions is added to position every so often
        this.positions = new ArrayList<Vector2>();
        this.position = new Vector2(32,64);
        this.positions.add(new Vector2(8,12));
        this.positions.add(new Vector2(8,12));
        this.positions.add(new Vector2(8,8));
        this.positions.add(new Vector2(8,8));
        this.positions.add(new Vector2(8,8));
        this.positions.add(new Vector2(8,8));
        this.positions.add(new Vector2(8,2));
        this.positions.add(new Vector2(8,-2));
        this.positions.add(new Vector2(8,-9));
        this.positions.add(new Vector2(8,-9));
        this.positions.add(new Vector2(0,0)); // 72 frames of nothing
        this.positions.add(new Vector2(0,0)); // need dummy pos
        // 12 events total

        this.sprites =  new ArrayList<Sprite>();
        for (int i = 0; i < 11; i++) {
            this.sprites.add(this.baitSprite);
        }
        this.sprites.add(null);
        // 12 events total

        this.frames = new ArrayList<Integer>();
        for (int i = 0; i < 11; i++) {
            this.frames.add(3);
        }
        this.frames.add(73-1); // 73 frames of nothing at end
        // 12 events total

        // text = new Texture(Gdx.files.internal("throw_rock_anim/helper12.png"));
        // this.helperSprite = new Sprite(text, 0, 0, 160, 144);
        // this.helperSprite.setPosition(16*10,16*9); // post scaling change
        // this.helperSprite.setScale(3); // post scaling change

        // game.insertAction(new PlaySound("throw_rock1", null));
        // sounds to play
        this.sounds = new ArrayList<String>();
        this.sounds.add("throw_rock1");
        for (int i = 0; i < 11; i++) {
            this.sounds.add(null);
        }
        // 12 events total
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        // set sprite position
        // if done with anim, do nextAction
        if (positions.isEmpty() || sprites.isEmpty()) {
            // modify opp pokemon's 'angry' level
            //"every time a rock is thrown, the catch rate C is doubled" - http:// www.dragonflycave.com/safarizone.aspx
            int currCatchRate = game.battle.oppPokemon.currentStats.get("catchRate");
            game.battle.oppPokemon.currentStats.put("catchRate", currCatchRate/2);

            // random between 1 and 5, add to eating counter
            int randomNum = game.map.rand.nextInt(5)+1;
            game.battle.oppPokemon.eating += randomNum;
            if (game.battle.oppPokemon.eating > 255) { // cap at 255
                game.battle.oppPokemon.eating = 255;
            }

            // set angry counter to 0
            game.battle.oppPokemon.angry = 0;

            // debug
            System.out.println("eating counter: "+String.valueOf(game.battle.oppPokemon.eating));
            System.out.println("Catch Rate: "+String.valueOf(game.battle.oppPokemon.currentStats.get("catchRate")));

            // wait 3 frames before menu draw
             // allows text box to be blank for 3 frames (drawText is already blank 1 frame by default)
            game.insertAction(new WaitFrames(game, 2, this.nextAction));
            game.actionStack.remove(this);
            return;
        }

        // get next sound, play it
        this.sound = this.sounds.get(0);
        if (this.sound != null) {
            game.insertAction(new PlaySound(this.sound, null));
            this.sounds.set(0, null); // don't play same sound over again
        }

        // get next frame
        this.sprite = sprites.get(0);

        // debug
        // this.helperSprite.draw(game.floatingBatch); // debug

        if (this.sprite != null) {
            // this.sprite.setScale(3); // post scaling change
            this.sprite.setPosition(position.x, position.y);
            this.sprite.draw(game.uiBatch);
        }

//        if (this.frames.size() == 1) { // debug
//            return;
//        }

        // repeat sprite/pos for current object for 'frames[0]' number of frames.
        if (this.frames.get(0) > 0) {
            this.frames.set(0, this.frames.get(0) - 1);
        }
        else {
            // since position is relative, only update once each time period
            this.position = this.position.add(positions.get(0));
            positions.remove(0);
            sprites.remove(0);
            frames.remove(0);
            sounds.remove(0);
        }

    }
}

// demo code
class ThrowFastPokeball extends Action {
    Sprite pokeballSprite;

    ArrayList<Vector2> positions;
    Vector2 position;
    ArrayList<Sprite> sprites;
    Sprite sprite;
    ArrayList<Integer> repeats;
    String sound;
    ArrayList<String> sounds;

    public int layer = 120;
    Sprite helperSprite; // just for helping me position the animation. delete later.

    public ThrowFastPokeball(Game game, Action nextAction) {
        this.nextAction = nextAction;

        Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball1.png"));
        this.pokeballSprite = new Sprite(text, 0, 0, 12, 12);

        // consider doing relative positions from now on
        // this.position = new Vector2(104+4*3-2,200-6*3-2); // post scaling change
        this.position = new Vector2(34-16,56-16);

        this.positions = new ArrayList<Vector2>();
        // harden frames
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0+16,0+16));// move to pokeball first spot

        this.positions.add(new Vector2(8,8));
        this.positions.add(new Vector2(8,8));
        this.positions.add(new Vector2(8,8));
        this.positions.add(new Vector2(8,8));
        this.positions.add(new Vector2(8,4));
        this.positions.add(new Vector2(8,4));
        this.positions.add(new Vector2(8,1));
        this.positions.add(new Vector2(8,-1));
        this.positions.add(new Vector2(8,-1));
        this.positions.add(new Vector2(8,-1));
        this.positions.add(new Vector2(0,0));// 12 accessed - done with throw at this point (blank screen)
        this.positions.add(new Vector2(-6*3,-10*3+8));// first of poof anim
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        // this.positions.add(new Vector2(0,0)); // wait frames before pokeball appears  // handled in next action now // delete
        this.positions.add(new Vector2(0,0)); // last is always dummy pos
        // 20 events total

        this.sprites =  new ArrayList<Sprite>();
        text = new Texture(Gdx.files.internal("attacks/harden_sheet1.png"));
        this.sprites.add(null); // draw nothing for 7 frames
        this.sprites.add(new Sprite(text, 56*0, 0, 56, 56));
        this.sprites.add(new Sprite(text, 56*1, 0, 56, 56));
        this.sprites.add(new Sprite(text, 56*2, 0, 56, 56));

        for (int i = 0; i < 11; i++) {
            this.sprites.add(this.pokeballSprite);
        }
        this.sprites.add(null); // draw nothing for 10 frames

        //'poof' animation
        text = new Texture(Gdx.files.internal("throw_pokeball_anim/poof_sheet1.png"));
        this.sprites.add(new Sprite(text, 48*0, 0, 48, 48));
        this.sprites.add(new Sprite(text, 48*1, 0, 48, 48));
        this.sprites.add(new Sprite(text, 48*2, 0, 48, 48));
        this.sprites.add(new Sprite(text, 48*3, 0, 48, 48));
        this.sprites.add(new Sprite(text, 48*4, 0, 48, 48));
        // 21 events total

        // this.sprites.add(null); // draw nothing for 13 frames // handled in next action now // delete

        this.repeats = new ArrayList<Integer>();
        this.repeats.add(7); //
        this.repeats.add(7); //
        this.repeats.add(7); //
        this.repeats.add(7); //
        for (int i = 0; i < 11; i++) {
            this.repeats.add(1);
        }
        this.repeats.add(10-1); // wait 10 frames

        for (int i = 0; i < 4; i++) { // 4 poof frames
            this.repeats.add(4);
        }
        this.repeats.add(10-1); // wait 10 frames for last poof frame
        // 21 events total

        // this.repeats.add(13-1); // 13 frames before pokeball appears on ground // handled in next action now // delete

        // sounds to play
        this.sounds = new ArrayList<String>();
        this.sounds.add("harden1");
        this.sounds.add(null);
        this.sounds.add(null);
        this.sounds.add(null);
        this.sounds.add("throw_pokeball1");
        for (int i = 0; i < 11; i++) {
            this.sounds.add(null);
        }
        this.sounds.add("poof1");
        for (int i = 0; i < 11; i++) {
            this.sounds.add(null);
        }
        //

//        text = new Texture(Gdx.files.internal("throw_pokeball_anim/helper13.png"));
//        this.helperSprite = new Sprite(text, 0, 0, 160, 144);
        // this.helperSprite.setPosition(16*10,16*9); // post scaling change
        // this.helperSprite.setScale(3); // post scaling change

        // play 'throw pokeball' sound
        // game.insertAction(new PlaySound("throw_pokeball1", null));
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        // set sprite position
        // if done with anim, do nextAction
        if (positions.isEmpty() || sprites.isEmpty()) {
            // note - it's possible that calling Action could take care of the below instead
             // nextAction is likely a menu draw Action, ie safariMenuDraw
            // game.insertAction(new catchPokemon_wigglesThenCatch(game, this.nextAction)); // before passed from menu
            // game.insertAction(new catchPkmn_oneWiggle(this.nextAction)); // later
            game.insertAction(this.nextAction);
            game.actionStack.remove(this);
            return;
        }

        // get next sound, play it
        this.sound = this.sounds.get(0);
        if (this.sound != null) {
            game.insertAction(new PlaySound(this.sound, null));
            this.sounds.set(0, null); // don't play same sound over again
        }

        // get next frame
        this.sprite = sprites.get(0);

        // debug
        // this.helperSprite.draw(game.floatingBatch);

        // draw current sprite
        if (this.sprite != null) {
            // this.sprite.setScale(3);  // post scaling change
            this.sprite.setPosition(position.x, position.y);
            this.sprite.draw(game.uiBatch);
        }

        // debug
//        if (this.repeats.size() == 16) {
//            return;
//        }

        // repeat sprite/pos for current object for 'frames[0]' number of frames.
        if (this.repeats.get(0) > 0) {
            this.repeats.set(0, this.repeats.get(0) - 1);
        }
        else {
            // since position is relative, only update once each time period
            this.position = this.position.add(positions.get(0));
            positions.remove(0);
            sprites.remove(0);
            repeats.remove(0);
            sounds.remove(0);
        }
    }
}

// demo code
 // pokeball throw that looks like hyperbeam
 // used when adr > 10 atm
class ThrowHyperPokeball extends Action {
    ArrayList<Vector2> positions;
    Vector2 position;
    ArrayList<Sprite> sprites;
    Sprite sprite;
    ArrayList<Integer> repeats;
    String sound;
    ArrayList<String> sounds;

    public int layer = 120;
    Sprite helperSprite; // just for helping me position the animation. delete later.

    public ThrowHyperPokeball(Game game, Action nextAction) {
        this.nextAction = nextAction;

        // consider doing relative positions from now on
        // this.position = new Vector2(104+4*3-2,200-6*3-2); // post scaling change
        this.position = new Vector2(50,72); //

        this.positions = new ArrayList<Vector2>();
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0+38+8,0)); // poof1
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        // this.positions.add(new Vector2(0,0)); // last is always dummy pos
        // 13 total events

        Texture text = new Texture(Gdx.files.internal("hyper_beam_anim/hyperbeam_sheet2.png"));

        this.sprites =  new ArrayList<Sprite>();
        this.sprites.add(null); // draw nothing for 14 frames
        for (int i = 0; i < 6; i++) {
            this.sprites.add(new Sprite(text, i*72, 0, 72, 40));
        }
        this.sprites.add(null); // draw nothing for 10 frames

        //'poof' animation
        // TODO - actually missing beam frames
        text = new Texture(Gdx.files.internal("throw_pokeball_anim/poof_sheet1.png"));
        this.sprites.add(new Sprite(text, 48*0, 0, 48, 48));
        this.sprites.add(new Sprite(text, 48*1, 0, 48, 48));
        this.sprites.add(new Sprite(text, 48*2, 0, 48, 48));
        this.sprites.add(new Sprite(text, 48*3, 0, 48, 48));
        this.sprites.add(new Sprite(text, 48*4, 0, 48, 48));

        // 13 total events

        this.repeats = new ArrayList<Integer>();
        this.repeats.add(14-1); // wait 14 frames
        for (int i = 0; i < 3; i++) {
            this.repeats.add(4); // 6
            this.repeats.add(3); // 2
        }
        this.repeats.add(3-1); // wait 10 frames

        for (int i = 0; i < 4; i++) { // 4 poof frames
            this.repeats.add(4);
        }
        this.repeats.add(10-1); // wait 10 frames for last poof frame
        // 13 total events

        // sounds to play
        this.sounds = new ArrayList<String>();
        this.sounds.add("hyperbeam1");
        for (int i = 0; i < 7; i++) {
            this.sounds.add(null);
        }
        this.sounds.add("poof1");
        for (int i = 0; i < 4; i++) {
            this.sounds.add(null);
        }
        // 13 total events

        // text = new Texture(Gdx.files.internal("hyper_beam_anim/helper1.png"));
//        text = new Texture(Gdx.files.internal("throw_pokeball_anim/helper13.png"));
//        this.helperSprite = new Sprite(text, 0, 0, 160, 144);

        // play 'throw pokeball' sound
        // game.insertAction(new PlaySound("throw_pokeball1", null));
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        // set sprite position
        // if done with anim, do nextAction
        if (positions.isEmpty() || sprites.isEmpty()) {
            // note - it's possible that calling Action could take care of the below instead
             // nextAction is likely a menu draw Action, ie safariMenuDraw
            // game.insertAction(new catchPokemon_wigglesThenCatch(game, this.nextAction)); // before passed from menu
            // game.insertAction(new catchPkmn_oneWiggle(this.nextAction)); // later
            game.insertAction(this.nextAction);
            game.actionStack.remove(this);
            return;
        }

        // get next sound, play it
        this.sound = this.sounds.get(0);
        if (this.sound != null) {
            game.insertAction(new PlaySound(this.sound, null));
            this.sounds.set(0, null); // don't play same sound over again
        }

        // get next frame
        this.sprite = sprites.get(0);

        // debug
//        this.helperSprite.draw(game.floatingBatch);

        // draw current sprite
        if (this.sprite != null) {
            // this.sprite.setScale(3);  // post scaling change
            this.sprite.setPosition(position.x, position.y);
            this.sprite.draw(game.uiBatch);
        }

        // debug
//        if (this.repeats.size() == 5) {
//            return;
//        }

        // repeat sprite/pos for current object for 'frames[0]' number of frames.
        if (this.repeats.get(0) > 0) {
            this.repeats.set(0, this.repeats.get(0) - 1);
        }
        else {
            // since position is relative, only update once each time period
            this.position = this.position.add(positions.get(0));
            positions.remove(0);
            sprites.remove(0);
            repeats.remove(0);
            sounds.remove(0);
        }
    }
}

// poof animation
// and algorithm that will show pkmn extending outwards
class ThrowOutPokemon extends Action {
    Sprite pokeballSprite;

    ArrayList<Vector2> positions;
    Vector2 position;
    ArrayList<Sprite[][]> sprites;
    Sprite[][] sprite;
    ArrayList<Integer> repeats;
    ArrayList<String> sounds;
    String sound;

    // night shade didn't work when this was layer 110,
    //  but worked when it was 114
    //  but shaders worked when it was 110 so weird
    // note - technically needs to be above displaytext, which is 106
    //  but i think nightshade/mewtwo battle had issues when it was 105
//    public int layer = 114;  // tested
    // issue - 105 is above battlemenu and itemsmenu
    public int layer = 105;  // TODO: test
    Sprite helperSprite; // just for helping me position the animation. delete later.

    boolean doneYet;

    boolean firstStep;

    public ThrowOutPokemon(Game game, Action nextAction) {
        this.firstStep = true;

        this.doneYet = false;
        this.nextAction = nextAction;

        // consider doing relative positions from now on
        // this.position = new Vector2(104+4*3-2,200-6*3-2); // post scaling change
        this.position = new Vector2(16,32);

        this.positions = new ArrayList<Vector2>();
        this.positions.add(new Vector2(0,0));// first of poof anim
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0)); // 1 empty frame
        this.positions.add(new Vector2(18-24,18)); // first of pokemon expand anim
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(24-18+6,18-6)); // last is always dummy pos (?need anymore?)

        this.sprites =  new ArrayList<Sprite[][]>();
        this.sprites.add(null); // draw nothing for 40 frames
        //'poof' animation
        Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/poof_sheet1.png"));
        this.sprites.add(new Sprite[][]{new Sprite[]{new Sprite(text, 48*0, 0, 48, 48)}});
        this.sprites.add(new Sprite[][]{new Sprite[]{new Sprite(text, 48*1, 0, 48, 48)}});
        this.sprites.add(new Sprite[][]{new Sprite[]{new Sprite(text, 48*2, 0, 48, 48)}});
        this.sprites.add(new Sprite[][]{new Sprite[]{new Sprite(text, 48*3, 0, 48, 48)}});
        this.sprites.add(new Sprite[][]{new Sprite[]{new Sprite(text, 48*4, 0, 48, 48)}});

        this.sprites.add(null); // draw nothing for 1 frame

        // below code will splice up image to create 'expanding' effect

//         7 blocks of 4 sprites each, 4th block is 'middle'
//         working backwards
//         frame 4to3 - x: remove cols 3 and 5 , y: remove rows 3 and 5
//         frame 3to2: x: remove cols 3 and 5  , y: remove rows 2 and 4
//         frame 2to1: x: remove cols 3 and 5  , y: remove rows 1 and 2 - no
//          - cloyster ends up with 5,7 as initial sprite

        // TODO: this doesn't work for gen2+ because sprites are twice as big
        // 3 frames release1
        Sprite temp = new Sprite(game.player.currPokemon.backSprite);
        TextureRegion[][] tempRegion = temp.split(4, 4); // should be 7x7


        Sprite[][] temp2 = new Sprite[7][7];
        for (int i = 0; i < tempRegion.length; i++) {
            for (int j = 0; j < tempRegion[i].length; j++) {
                temp2[i][j] = new Sprite(tempRegion[6-j][i]);
                temp2[i][j].setScale(2);
//                System.out.println("sprite size: "+String.valueOf(temp2[i][j].getHeight())+"  "+String.valueOf(temp2[i][j].getHeight()));
            }
        }

        // remove rows 3 and 5
        for (int i = 0; i < temp2.length; i++) {
            temp2[i][2] = temp2[i][3];
            temp2[i][3] = temp2[i][5];
            temp2[i][4] = temp2[i][6];
            temp2[i][5] = null;
            temp2[i][6] = null;
        }
        // remove cols 3 and 5
        temp2[2] = temp2[1];
        temp2[1] = temp2[0];
        temp2[0] = new Sprite[]{};
        temp2[4] = temp2[5];
        temp2[5] = temp2[6];
        temp2[6] = new Sprite[]{};
        // i=0, j=6 -> i=6, j=0
        // i=0, j=0 -> i=6, j=6

        // copy temp 2
        Sprite[][] temp3 = new Sprite[7][7]; //.clone();
        for (int i = 0; i < temp2.length; i++) {
            for (int j = 0; j < temp2[i].length; j++) {
                temp3[i][j] = temp2[i][j];
            }
        }

        // remove rows 2 and 4
        for (int i = 0; i < temp3.length; i++) {
            if (temp3[i].length <= 0) {
                continue;
            }
            temp3[i][1] = temp3[i][2];
            temp3[i][2] = temp3[i][4];
            temp3[i][3] = null;
            temp3[i][4] = null;
        }
        // remove cols 3 and 5
        temp3[2] = temp3[1];
        temp3[1] = new Sprite[]{};
        temp3[4] = temp3[5];
        temp3[5] = new Sprite[]{};

        Sprite temp4 = new Sprite(tempRegion[0][4]);
        temp4.setScale(2);

        this.sprites.add(new Sprite[][]{new Sprite[]{}, new Sprite[]{}, new Sprite[]{}, new Sprite[]{temp4}});
        this.sprites.add(temp3);
        this.sprites.add(temp2);
        this.sprites.add(new Sprite[][]{new Sprite[]{game.player.currPokemon.backSprite}});
        // 3 frames release2
        // 6 frames release3

        // -1?
        this.repeats = new ArrayList<Integer>();
        this.repeats.add(40);
        for (int i = 0; i < 4; i++) {
            this.repeats.add(5);
        }
        this.repeats.add(10); // last is 10 frames
        this.repeats.add(1); // last is 10 frames
        this.repeats.add(3);
        this.repeats.add(3);
        this.repeats.add(6);

        this.sounds = new ArrayList<String>();
        this.sounds.add(null); //
        this.sounds.add("poof1");
        for (int i = 0; i < 9; i++) {
            this.sounds.add(null);
        }

//        text = new Texture(Gdx.files.internal("pokemon_throw_out_anim/helper5.png"));
//        this.helperSprite = new Sprite(text, 0, 0, 160, 144);
        // this.helperSprite.setPosition(16*10,16*9); // post scaling change
        // this.helperSprite.setScale(3); // post scaling change

        // play 'throw pokeball' sound
        // game.insertAction(new PlaySound("throw_pokeball1", null));
    }
    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        if (this.firstStep == true) {
            game.battle.drawAction.drawFriendlyPokemonAction = this;
            this.firstStep = false;
        }

        // get next frame
        this.sprite = sprites.get(0);

        // debug
//        this.helperSprite.draw(game.floatingBatch);

        // set sprite position
        // if done with anim, do nextAction
        if (positions.isEmpty() || sprites.isEmpty()) {
            // debug
//            if (this.repeats.size() == 0) {
//                return;
//            }

            if (!this.doneYet) {
                game.insertAction(this.nextAction);

                // set real sprite to correct position
                game.player.currPokemon.backSprite.setPosition(position.x, position.y);

                for (int i = 0; i < this.sprite.length; i++) {
                    for (int j = 0; j < this.sprite[i].length; j++) {
                        this.sprite[i][j].setPosition(position.x, position.y);
                    }
                }
                this.doneYet = true;

                // modify layer of this action
                this.layer = 114;
                game.actionStack.remove(this);
                game.insertAction(this);
            }

            // No performance different here either way
//            System.out.println("here1");
//            game.player.currPokemon.backSprite.draw(game.floatingBatch);
            // TODO: not sure why I was doing this, assuming it was for a reason
            for (int i = 0; i < this.sprite.length; i++) {
                for (int j = 0; j < this.sprite[i].length; j++) {
                    this.sprite[i][j].draw(game.uiBatch);
                }
            }

            if (game.battle.drawAction == null) {
                game.actionStack.remove(this);
            }
            return;
        }

        // draw current sprite
        if (this.sprite != null) {
            for (int i = 0; i < this.sprite.length; i++) {
                for (int j = 0; j < this.sprite[i].length; j++) {
                    if (this.sprite[i][j] != null) {
                        this.sprite[i][j].setPosition(position.x+8*i, position.y+8*j);
                        this.sprite[i][j].draw(game.uiBatch);
                    }
                }
            }
        }

        // debug
//        if (this.repeats.size() == 6) {
//            return;
//        }

        // get next sound, play it
        this.sound = this.sounds.get(0);
        if (this.sound != null) {
            game.insertAction(new PlaySound(this.sound, null));
            this.sounds.set(0, null); // don't play same sound over again
        }

        // repeat sprite/pos for current object for 'frames[0]' number of frames.
        if (this.repeats.get(0) > 1) {
            this.repeats.set(0, this.repeats.get(0) - 1);
        }
        else {
            // since position is relative, only update once each time period
            this.position = this.position.add(positions.get(0));
            positions.remove(0);
            sprites.remove(0);
            repeats.remove(0);
            sounds.remove(0);
        }
    }
}

// poof animation
// and algorithm that will show pkmn extending outwards
class ThrowOutPokemonCrystal extends Action {
  Sprite pokeballSprite;

  ArrayList<Vector2> positions;
  Vector2 position;
  ArrayList<Sprite[][][]> sprites;
  Sprite[][][] sprite;
  ArrayList<Integer> repeats;
  ArrayList<String> sounds;
  String sound;

  // night shade didn't work when this was layer 110,
  //  but worked when it was 114
  //  but shaders worked when it was 110 so weird
//  public int layer = 114;
  public int layer = 105;
  Sprite helperSprite; // just for helping me position the animation. delete later.

  boolean doneYet;

  boolean firstStep;

  public ThrowOutPokemonCrystal(Game game, Action nextAction) {
      this.firstStep = true;

      this.doneYet = false;
      this.nextAction = nextAction;

      // consider doing relative positions from now on
      // this.position = new Vector2(104+4*3-2,200-6*3-2); // post scaling change
      this.position = new Vector2(16, 48);

//      this.positions = new ArrayList<Vector2>();
//      this.positions.add(new Vector2(0,0));// first of poof anim
//      this.positions.add(new Vector2(0,0));
//      this.positions.add(new Vector2(0,0));
//      this.positions.add(new Vector2(0,0));
//      this.positions.add(new Vector2(0,0));
//      this.positions.add(new Vector2(0,0)); // 1 empty frame
//      this.positions.add(new Vector2(18-24,18)); // first of pokemon expand anim
//      this.positions.add(new Vector2(0,0));
//      this.positions.add(new Vector2(0,0));
//      this.positions.add(new Vector2(24-18+6,18-6)); // last is always dummy pos (?need anymore?)

      // below code will splice up image to create 'expanding' effect

//       7 blocks of 4 sprites each, 4th block is 'middle'
//       working backwards
//       frame 4to3 - x: remove cols 3 and 5 , y: remove rows 3 and 5
//       frame 3to2: x: remove cols 3 and 5  , y: remove rows 2 and 4
//       frame 2to1: x: remove cols 3 and 5  , y: remove rows 1 and 2 - no
//        - cloyster ends up with 5,7 as initial sprite

      // TODO: this doesn't work for gen2+ because sprites are twice as big
      // 3 frames release1
      Sprite temp = new Sprite(game.player.currPokemon.backSprite);
      // sprite width is 48, need 8x8 blocks
      TextureRegion[][] tempRegion = temp.split(8, 8); // should be 8x8 blocks

      Sprite[][] temp2 = new Sprite[6][6];
      for (int i = 0; i < tempRegion.length; i++) {
          for (int j = 0; j < tempRegion[i].length; j++) {
              temp2[i][j] = new Sprite(tempRegion[5-j][i]);
//              temp2[i][j].setScale(2);  // TODO: remove
//              System.out.println("sprite size: "+String.valueOf(temp2[i][j].getHeight())+"  "+String.valueOf(temp2[i][j].getHeight()));
          }
      }

      // remove rows 1 and 4
      for (int i = 0; i < temp2.length; i++) {
          temp2[i][1] = temp2[i][2];
          temp2[i][2] = temp2[i][3];
          temp2[i][3] = temp2[i][5];
          temp2[i][4] = null;
          temp2[i][5] = null;
      }
      // remove cols 1 and 4
      temp2[1] = temp2[0];
      temp2[0] = new Sprite[]{};
      temp2[4] = temp2[5];
      temp2[5] = new Sprite[]{};
      // i=0, j=6 -> i=6, j=0
      // i=0, j=0 -> i=6, j=6

      // copy temp 2
      Sprite[][] temp3 = new Sprite[6][6];
      for (int i = 0; i < temp2.length; i++) {
          for (int j = 0; j < temp2[i].length; j++) {
              temp3[i][j] = temp2[i][j];
          }
      }

      // remove rows 1 and 2
      for (int i = 0; i < temp3.length; i++) {
          if (temp3[i].length <= 0) {
              continue;
          }
          temp3[i][1] = temp3[i][3];
          temp3[i][2] = null;
          temp3[i][3] = null;
          temp3[i][4] = null;
          temp3[i][5] = null;
      }
      // remove cols 2 and 3
      temp3[2] = temp3[1];
      temp3[1] = new Sprite[]{};
      temp3[3] = temp3[4];
      temp3[4] = new Sprite[]{};
      temp3[5] = new Sprite[]{};

//      Sprite temp4 = new Sprite(tempRegion[0][4]);
//      temp4.setScale(2);

      // TODO: need 'frame' class
      //  sprites, repeat, positions, etc

      //'poof' animation
      Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/poof_sheet2.png"));
      this.sprites =  new ArrayList<Sprite[][][]>();
      // draw nothing for 34 frames
      this.sprites.add(null);
      // poof sprites are mixed with pokemon sprites
      // 4 frames poof1
      this.sprites.add(new Sprite[][][]{null, new Sprite[][]{new Sprite[]{new Sprite(text, 48*0, 0, 48, 48)}}});
      // 2 frames poof2
      this.sprites.add(new Sprite[][][]{null, new Sprite[][]{new Sprite[]{new Sprite(text, 48*1, 0, 48, 48)}}});
      // 2 frames poof2, small sprite appears
      this.sprites.add(new Sprite[][][]{temp3, new Sprite[][]{new Sprite[]{new Sprite(text, 48*1, 0, 48, 48)}}});
      // 2 frames poof3, small sprite
      this.sprites.add(new Sprite[][][]{temp3, new Sprite[][]{ new Sprite[]{new Sprite(text, 48*2, 0, 48, 48)}}});
      // 2 frames poof3, middle sprite appears
      this.sprites.add(new Sprite[][][]{temp2, new Sprite[][]{ new Sprite[]{new Sprite(text, 48*2, 0, 48, 48)}}});
      // 2 frames poof4, middle sprite visible
      this.sprites.add(new Sprite[][][]{temp2, new Sprite[][]{ new Sprite[]{new Sprite(text, 48*3, 0, 48, 48)}}});
      // 2 frames poof4, sprite enlarge to normal size
      this.sprites.add(new Sprite[][][]{new Sprite[][]{ new Sprite[]{game.player.currPokemon.backSprite}}, new Sprite[][]{ new Sprite[]{new Sprite(text, 48*3, 0, 48, 48)}}});
      this.sprites.add(new Sprite[][][]{new Sprite[][]{ new Sprite[]{game.player.currPokemon.backSprite}}}); // wait frames before pokemon cry
      this.sprites.add(new Sprite[][][]{new Sprite[][]{ new Sprite[]{game.player.currPokemon.backSprite}}}); // dummy frame

//      this.sprites.add(new Sprite[][]{new Sprite[]{}, new Sprite[]{}, new Sprite[]{}, new Sprite[]{temp4}});
//      this.sprites.add(temp3);
//      this.sprites.add(temp2);
//      this.sprites.add(new Sprite[][]{new Sprite[]{game.player.currPokemon.backSprite}});
      // 3 frames release2
      // 6 frames release3

      // -1?
      this.repeats = new ArrayList<Integer>();
      this.repeats.add(34);
      this.repeats.add(4);
      this.repeats.add(2);
      this.repeats.add(2);
      this.repeats.add(2);
      this.repeats.add(2);
      this.repeats.add(2);
      this.repeats.add(2);
      this.repeats.add(26); // wait 12 frames before pokemon cry. TODO: test this.

      this.sounds = new ArrayList<String>();
      this.sounds.add(null); //
      this.sounds.add("poof1");
      for (int i = 0; i < 9; i++) {
          this.sounds.add(null);
      }
//      text = new Texture(Gdx.files.internal("pokemon_throw_out_anim/throwanim_helper4.png"));
//      this.helperSprite = new Sprite(text, 0, 0, 160, 144);
  }
  public String getCamera() {return "gui";}

  public int getLayer(){return this.layer;}

  @Override
  public void step(Game game) {
      if (this.firstStep) {
          // TODO: game.battle.drawAction has been null after battle beings
          //       somehow.
          game.battle.drawAction.drawFriendlyPokemonAction = this;
          this.firstStep = false;

          // Reset stat stages of Pokemon sending out.
          game.player.currPokemon.resetStatStages();

          // This pokemon participated in battle
          game.player.currPokemon.participatedInBattle = true;

          // TODO: test
          // TODO: remove
          // If oppPokemon is 10 levels higher than player.currPokemon,
          // silently raise speed stat.
//          if (game.player.currPokemon.level +10 <= game.battle.oppPokemon.level) {
//              System.out.println(game.player.currPokemon.currentStats.get("speed"));
//              game.player.currPokemon.gen2ApplyStatStage("speed", 12, true);
//              System.out.println(game.player.currPokemon.currentStats.get("speed"));
//          }

          // Clear confusion/attract
          // TODO: confusion needs to be handled outside of pokemon.status.
          if (game.player.currPokemon.status != null && (game.player.currPokemon.status.equals("confuse") || game.player.currPokemon.status.equals("attract"))) {
              game.player.currPokemon.status = null;
              game.player.currPokemon.statusCounter = 0;
          }
          // Reset attacks cursor to 0. 
          // If you don't do this, player can select null attack which causes softlock.
          DrawAttacksMenu.curr = 0;
      }

      // get next frame
      this.sprite = sprites.get(0);

      // debug
//      if (!Gdx.input.isKeyPressed(Input.Keys.N)) {
//          this.helperSprite.draw(game.floatingBatch);
//      }

      // set sprite position
      // if done with anim, do nextAction
      if (sprites.size() <= 1) {  // positions.isEmpty() ||

          // debug
//          if (this.repeats.size() == 0) {
//              return;
//          }

          if (!this.doneYet) {
              game.insertAction(this.nextAction);

              // set real sprite to correct position
              game.player.currPokemon.backSprite.setPosition(position.x, position.y);

              for (int k = 0; k < this.sprite.length; k++) {
                  if (this.sprite[k] == null) {
                      continue;
                  }
                  for (int i = 0; i < this.sprite[k].length; i++) {
                      for (int j = 0; j < this.sprite[k][i].length; j++) {
                          this.sprite[k][i][j].setPosition(position.x, position.y);
                      }
                  }
              }
              this.doneYet = true;

              // modify layer of this action
              this.layer = 114;
              game.actionStack.remove(this);
              game.insertAction(this);
          }

          // No performance difference here either way
//          System.out.println("here1");
//          game.player.currPokemon.backSprite.draw(game.floatingBatch);
          // TODO: not sure why I was doing this, assuming it was for a reason
          // - I think this is the only thing drawing the back sprite
          for (int k = 0; k < this.sprite.length; k++) {
              for (int i = 0; i < this.sprite[k].length; i++) {
                  for (int j = 0; j < this.sprite[k][i].length; j++) {
                      this.sprite[k][i][j].draw(game.uiBatch);
                  }
              }
          }

          if (game.battle.drawAction == null) {
              game.actionStack.remove(this);
          }
          return;
      }

      // draw current sprite
      if (this.sprite != null) {
          for (int k = 0; k < this.sprite.length; k++) {
              if (this.sprite[k] == null) {
                  continue;
              }
              for (int i = 0; i < this.sprite[k].length; i++) {
                  for (int j = 0; j < this.sprite[k][i].length; j++) {
                      if (this.sprite[k][i][j] != null) {
                          this.sprite[k][i][j].setPosition(position.x +8*i -4*k, position.y +8*j -8*k);
                          this.sprite[k][i][j].draw(game.uiBatch);
                      }
                  }
              }
          }
      }

      // debug
//      if (this.repeats.size() == 2) {
//          return;
//      }

      // get next sound, play it
      this.sound = this.sounds.get(0);
      if (this.sound != null) {
          game.insertAction(new PlaySound(this.sound, null));
          this.sounds.set(0, null); // don't play same sound over again
      }

      // repeat sprite/pos for current object for 'frames[0]' number of frames.
      if (this.repeats.get(0) > 1) {
          this.repeats.set(0, this.repeats.get(0) - 1);
      }
      else {
          // since position is relative, only update once each time period
//          this.position = this.position.add(positions.get(0));
//          positions.remove(0);
          sprites.remove(0);
          repeats.remove(0);
          sounds.remove(0);
      }
  }
}

// draws pokeball flying through air and hitting opposing pkmn
 // whether to catch or not is decided at end of this action
 // pass nextAction to the new action (probably a draw menu action, for example DrawBattleMenu_SafariZone)
class ThrowPokeball extends Action {
    Sprite pokeballSprite;

    ArrayList<Vector2> positions;
    Vector2 position;
    ArrayList<Sprite> sprites;
    Sprite sprite;
    ArrayList<Integer> repeats;
    String sound;
    ArrayList<String> sounds;

    public int layer = 120;
    public ThrowPokeball(Game game, Action nextAction) {
        this.nextAction = nextAction;

        // Note - throw_pokeball_anim/pokeball1.png is r/b pokeball
        // uncomment and use this for gen 1 pokeball throw animation
//        Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball1.png"));
        Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball1_color.png"));
        this.pokeballSprite = new Sprite(text, 0, 0, 12, 12);

        // consider doing relative positions from now on
        // this.position = new Vector2(104+4*3-2,200-6*3-2); // post scaling change
        this.position = new Vector2(34,56);

        this.positions = new ArrayList<Vector2>();
        this.positions.add(new Vector2(8,8));
        this.positions.add(new Vector2(8,8));
        this.positions.add(new Vector2(8,8));
        this.positions.add(new Vector2(8,8));
        this.positions.add(new Vector2(8,4));
        this.positions.add(new Vector2(8,4));
        this.positions.add(new Vector2(8,1));
        this.positions.add(new Vector2(8,-1));
        this.positions.add(new Vector2(8,-1));
        this.positions.add(new Vector2(8,-1));
        this.positions.add(new Vector2(0,0));// 12 accessed - done with throw at this point (blank screen)
        this.positions.add(new Vector2(-6*3,-10*3+8));// first of poof anim
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        // this.positions.add(new Vector2(0,0)); // wait frames before pokeball appears  // handled in next action now // delete
        this.positions.add(new Vector2(0,0)); // last is always dummy pos

        this.sprites =  new ArrayList<Sprite>();
        for (int i = 0; i < 11; i++) {
            this.sprites.add(this.pokeballSprite);
        }
        this.sprites.add(null); // draw nothing for 10 frames

        //'poof' animation
        text = new Texture(Gdx.files.internal("throw_pokeball_anim/poof_sheet1.png"));
        this.sprites.add(new Sprite(text, 48*0, 0, 48, 48));
        this.sprites.add(new Sprite(text, 48*1, 0, 48, 48));
        this.sprites.add(new Sprite(text, 48*2, 0, 48, 48));
        this.sprites.add(new Sprite(text, 48*3, 0, 48, 48));
        this.sprites.add(new Sprite(text, 48*4, 0, 48, 48));

        // this.sprites.add(null); // draw nothing for 13 frames // handled in next action now // delete

        this.repeats = new ArrayList<Integer>();
        for (int i = 0; i < 11; i++) {
            this.repeats.add(2);
        }
        this.repeats.add(10-1); // wait 10 frames

        for (int i = 0; i < 4; i++) { // 4 poof frames
            this.repeats.add(4);
        }
        this.repeats.add(10-1); // wait 10 frames for last poof frame

        // this.repeats.add(13-1); // 13 frames before pokeball appears on ground // handled in next action now // delete

        // sounds to play
        this.sounds = new ArrayList<String>();
        this.sounds.add("throw_pokeball1");
        for (int i = 0; i < 11; i++) {
            this.sounds.add(null);
        }
        this.sounds.add("poof1");
        for (int i = 0; i < 11; i++) {
            this.sounds.add(null);
        }

//        text = new Texture(Gdx.files.internal("throw_pokeball_anim/helper13.png"));
//        this.helperSprite = new Sprite(text, 0, 0, 160, 144);
        // this.helperSprite.setPosition(16*10,16*9); // post scaling change
        // this.helperSprite.setScale(3); // post scaling change

        // play 'throw pokeball' sound
        // game.insertAction(new PlaySound("throw_pokeball1", null));
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        // set sprite position
        // if done with anim, do nextAction
        if (positions.isEmpty() || sprites.isEmpty()) {
            // note - it's possible that calling Action could take care of the below instead
             // nextAction is likely a menu draw Action, ie safariMenuDraw
            // game.insertAction(new catchPokemon_wigglesThenCatch(game, this.nextAction)); // before passed from menu
            // game.insertAction(new catchPkmn_oneWiggle(this.nextAction)); // later
            game.insertAction(this.nextAction);
            game.actionStack.remove(this);
            return;
        }

        // get next sound, play it
        this.sound = this.sounds.get(0);
        if (this.sound != null) {
            game.insertAction(new PlaySound(this.sound, null));
            this.sounds.set(0, null); // don't play same sound over again
        }

        // get next frame
        this.sprite = sprites.get(0);

        // debug
        // this.helperSprite.draw(game.floatingBatch);

        // draw current sprite
        if (this.sprite != null) {
            // this.sprite.setScale(3);  // post scaling change
            this.sprite.setPosition(position.x, position.y);
            this.sprite.draw(game.uiBatch);
        }

        // debug
//        if (this.repeats.size() == 5) {
//            return;
//        }

        // repeat sprite/pos for current object for 'frames[0]' number of frames.
        if (this.repeats.get(0) > 0) {
            this.repeats.set(0, this.repeats.get(0) - 1);
        }
        else {
            // since position is relative, only update once each time period
            this.position = this.position.add(positions.get(0));
            positions.remove(0);
            sprites.remove(0);
            repeats.remove(0);
            sounds.remove(0);
        }
    }
}

// draws rock flying through air and hitting opposing pkmn
class ThrowRock extends Action {
    // remove
    Sprite hitSprite;
    Sprite rockSprite;

    ArrayList<Vector2> positions;
    Vector2 position;
    ArrayList<Sprite> sprites;
    Sprite sprite;
    ArrayList<Integer> frames;
    String sound;
    ArrayList<String> sounds;

    public int layer = 120;
    Sprite helperSprite; // just for helping me position the animation. delete later.

    public ThrowRock(Game game, Action nextAction) {
        this.nextAction = nextAction;

        Texture text = new Texture(Gdx.files.internal("throw_rock_anim/hit1.png"));
        this.hitSprite = new Sprite(text, 0, 0, 24, 24);

        text = new Texture(Gdx.files.internal("throw_rock_anim/rock_small1.png"));
        this.rockSprite = new Sprite(text, 0, 0, 8, 8);

        // positions is added to position every so often
        this.positions = new ArrayList<Vector2>();
        this.position = new Vector2(32,64);
        this.positions.add(new Vector2(8,12));
        this.positions.add(new Vector2(8,12));
        this.positions.add(new Vector2(8,8));
        this.positions.add(new Vector2(8,8));
        this.positions.add(new Vector2(8,8));
        this.positions.add(new Vector2(8,8));
        this.positions.add(new Vector2(8,2));
        this.positions.add(new Vector2(8,-2));
        this.positions.add(new Vector2(8,-9));
        this.positions.add(new Vector2(8,-9));
        this.positions.add(new Vector2(-4,-12));
        this.positions.add(new Vector2(0,0)); // 72 frames of nothing
        this.positions.add(new Vector2(0,0)); // need dummy pos
        // 13 events total

        this.sprites =  new ArrayList<Sprite>();
        for (int i = 0; i < 11; i++) {
            this.sprites.add(this.rockSprite);
        }
        this.sprites.add(this.hitSprite);
        this.sprites.add(null);
        // 13 events total

        this.frames = new ArrayList<Integer>();
        for (int i = 0; i < 12; i++) {
            this.frames.add(3);
        }
        this.frames.add(72-1); // 72 frames of nothing at end
        // 13 events total

//        text = new Texture(Gdx.files.internal("throw_rock_anim/helper12.png"));
//        this.helperSprite = new Sprite(text, 0, 0, 160, 144);
        // this.helperSprite.setPosition(16*10,16*9); // post scaling change
        // this.helperSprite.setScale(3); // post scaling change

        // game.insertAction(new PlaySound("throw_rock1", null));
        // sounds to play
        this.sounds = new ArrayList<String>();
        this.sounds.add("throw_rock1");
        for (int i = 0; i < 12; i++) {
            this.sounds.add(null);
        }
        // 13 events total

    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        // set sprite position
        // if done with anim, do nextAction
        if (positions.isEmpty() || sprites.isEmpty()) {
            // modify opp pokemon's 'angry' level
            //"every time a rock is thrown, the catch rate C is doubled" - http:// www.dragonflycave.com/safarizone.aspx
            int currCatchRate = game.battle.oppPokemon.currentStats.get("catchRate");
            game.battle.oppPokemon.currentStats.put("catchRate", currCatchRate*2);
            if (game.battle.oppPokemon.currentStats.get("catchRate") > 255) { // cap at 255
                game.battle.oppPokemon.currentStats.put("catchRate", 255);
            }
            // random between 1 and 5, add to angry counter
            int randomNum = game.map.rand.nextInt(5)+1;
            game.battle.oppPokemon.angry += randomNum;
            if (game.battle.oppPokemon.angry > 255) { // cap at 255
                game.battle.oppPokemon.angry = 255;
            }
            // set eating counter to 0
            game.battle.oppPokemon.eating = 0;

            // TODO: debug, remove
//            System.out.println("angry counter: "+String.valueOf(game.battle.oppPokemon.angry));
//            System.out.println("Catch Rate: "+String.valueOf(game.battle.oppPokemon.currentStats.get("catchRate")));

            // wait 3 frames before menu draw
             // allows text box to be blank for 3 frames (drawText is already blank 1 frame by default)
            game.insertAction(new WaitFrames(game, 2, this.nextAction));
            game.actionStack.remove(this);
            return;
        }
        // get next sound, play it
        this.sound = this.sounds.get(0);
        if (this.sound != null) {
            game.insertAction(new PlaySound(this.sound, null));
            this.sounds.set(0, null); // don't play same sound over again
        }
        // get next frame
        this.sprite = sprites.get(0);
        if (this.sprite != null) {
            // this.sprite.setScale(3); // post scaling change
            this.sprite.setPosition(position.x, position.y);
            this.sprite.draw(game.uiBatch);
        }
        // repeat sprite/pos for current object for 'frames[0]' number of frames.
        if (this.frames.get(0) > 0) {
            this.frames.set(0, this.frames.get(0) - 1);
        }
        else {
            // since position is relative, only update once each time period
            this.position = this.position.add(positions.get(0));
            positions.remove(0);
            sprites.remove(0);
            frames.remove(0);
            sounds.remove(0);
        }
    }
}



class FadeAnim extends Action {
    Sprite sprite;

    public int layer = 114;
    int timer = 0;
    int slow = 1;  // TODO: remove, use some sort of into anim;

    public FadeAnim(Game game, int slow, Action nextAction) {
        this.nextAction = nextAction;
        this.slow = slow;
        // fade out from white anim
        Texture text1 = TextureCache.get(Gdx.files.internal("battle/intro_frame6.png"));
        this.sprite = new Sprite(text1);
        this.sprite.setPosition(0,0);
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void firstStep(Game game) {
    }

    @Override
    public void step(Game game) {
        if (this.timer < 2*slow) {
        }
        else if (this.timer < 4*slow) {
            this.sprite.draw(game.uiBatch, .25f);
        }
        else if (this.timer < 6*slow) {
            this.sprite.draw(game.uiBatch, .50f);
        }
        else if (this.timer < 12*slow) {
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



/**
 * Gen 2 stats screen
 */
class DrawStatsScreen extends MenuAction {
    public int layer = 107;
    Sprite[] bgSprites = new Sprite[3];
    Sprite helperSprite;
    public int currIndex = 0;
    Pokemon pokemon;
    int pokeIndex;
    Sprite healthSprite;
    PokemonIntroAnim intro;

    public DrawStatsScreen(Game game, Pokemon pokemon, MenuAction prevMenu) {
        this.prevMenu = prevMenu;
        for (int i=0; i < 3; i++) {
            Texture text = new Texture(Gdx.files.internal("menu/stats_screen"+String.valueOf(i+1)+".png"));  // TODO: un-ref
            this.bgSprites[i] = new Sprite(text, 0, 0, 16*10, 16*9);
        }
        this.pokemon = pokemon;
        this.pokeIndex = game.player.pokemon.indexOf(pokemon);
        Texture text = new Texture(Gdx.files.internal("battle/health1.png"));
        this.healthSprite = new Sprite(text, 0,0,1,2);
        this.intro = new PokemonIntroAnim(null);
    }
    
    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void firstStep(Game game) {
        game.battle.oppPokemon = this.pokemon;
        game.insertAction(new WaitFrames(game, 4, new PlaySound(this.pokemon, null)));
        game.insertAction(intro);
    }

    @Override
    public void step(Game game) {
        game.uiBatch.draw(this.bgSprites[this.currIndex], 0, 0);
        // Draw pokemon sprite at 88, and center it
        Sprite sprite = new Sprite(this.pokemon.sprite);
        sprite.flip(true, false);
        game.uiBatch.draw(sprite, (56-this.pokemon.sprite.getWidth())/2, 88);
        
        // TODO: probably just init all this in firststep
        // Level at 120, 144-8
        int tensPlace = this.pokemon.level/10;
        Sprite tensPlaceSprite;
        if (tensPlace > 0) {
            tensPlaceSprite = game.transparentDict.get(Character.forDigit(tensPlace, 10));
            game.uiBatch.draw(tensPlaceSprite, 120, 144 -8);
        }
        int offset = 0;
        if (this.pokemon.level >= 10) {
            offset = 8;
        }
        int onesPlace = this.pokemon.level % 10;
        Sprite onesPlaceSprite = game.transparentDict.get(Character.forDigit(onesPlace, 10));
        game.uiBatch.draw(onesPlaceSprite, 120 +offset, 144 -8);
        // Gender
        if (this.pokemon.gender.equals("male")) {
            game.uiBatch.draw(TextureCache.maleSymbol, 144, 144 -8);
        }
        else if (this.pokemon.gender.equals("female")) {
            game.uiBatch.draw(TextureCache.femaleSymbol, 144, 144 -8);
        }
        // Name 64, 120
        char[] textArray = this.pokemon.name.toUpperCase().toCharArray();
        Sprite letterSprite;
        for (int j=0; j < textArray.length; j++) {
            letterSprite = game.transparentDict.get(textArray[j]);
            game.uiBatch.draw(letterSprite, 64 +8*j, 120);
        }
        
        if (this.currIndex == 0) {
            // max health 17, 57
            int maxHealth = this.pokemon.maxStats.get("hp");
            int hundredsPlace = maxHealth/100;
            if (hundredsPlace > 0) {
                Sprite hundredsPlaceSprite = game.transparentDict.get(Character.forDigit(hundredsPlace,10));
                game.uiBatch.draw(hundredsPlaceSprite, 16-8, 56);
            }
            tensPlace = (maxHealth % 100) / 10;
            if (tensPlace > 0 || hundredsPlace > 0) {
                tensPlaceSprite = game.transparentDict.get(Character.forDigit(tensPlace, 10));
                game.uiBatch.draw(tensPlaceSprite, 16-8 +8, 56);
            }
            onesPlace = maxHealth % 10;
            onesPlaceSprite = game.transparentDict.get(Character.forDigit(onesPlace,10));
            game.uiBatch.draw(onesPlaceSprite, 16-8 +16, 56);
            // Draw pkmn current health text
            int currHealth = this.pokemon.currentStats.get("hp");
            hundredsPlace = currHealth/100;
            if (hundredsPlace > 0) {
                Sprite hundredsPlaceSprite = game.transparentDict.get(Character.forDigit(hundredsPlace, 10));
                game.uiBatch.draw(hundredsPlaceSprite, 48-8, 56);
            }
            tensPlace = (currHealth % 100) / 10;
            if (tensPlace > 0 || hundredsPlace > 0) {
                tensPlaceSprite = game.transparentDict.get(Character.forDigit(tensPlace, 10));
                game.uiBatch.draw(tensPlaceSprite, 48-8 +8, 56);
            }
            onesPlace = currHealth % 10;
            onesPlaceSprite = game.transparentDict.get(Character.forDigit(onesPlace, 10));
            game.uiBatch.draw(onesPlaceSprite, 48-8 +16, 56);

            // Status 48, 33
            String text = "OK";
            if (this.pokemon.status != null) {
                if (this.pokemon.status.equals("poison") || this.pokemon.status.equals("toxic")) {
                    text = "PSN";
                }
                else if (this.pokemon.status.equals("paralyze")) {
                    text = "PAR";
                }
                else if (this.pokemon.status.equals("freeze")) {
                    text = "FRZ";
                }
                else if (this.pokemon.status.equals("sleep")) {
                    text = "SLP";
                }
                else if (this.pokemon.status.equals("burn")) {
                    text = "BRN";
                }
            }
            textArray = text.toCharArray();
            for (int j=0; j < textArray.length; j++) {
                letterSprite = game.transparentDict.get(textArray[j]);
                game.uiBatch.draw(letterSprite, 48 +8*j, 32);
            }
            
            // Type 1 - 8, 17
            textArray = this.pokemon.types.get(0).toCharArray();
            for (int j=0; j < textArray.length; j++) {
                letterSprite = game.transparentDict.get(textArray[j]);
                game.uiBatch.draw(letterSprite, 8 +8*j, 16);
            }
            // Type 2 - 8, 9
            textArray = this.pokemon.types.get(1).toCharArray();
            for (int j=0; j < textArray.length && !this.pokemon.types.get(0).equals(this.pokemon.types.get(1)); j++) {
                letterSprite = game.transparentDict.get(textArray[j]);
                game.uiBatch.draw(letterSprite, 8 +8*j, 8);
            }
            
            // Exp points 80, 57
            String exp = String.valueOf(this.pokemon.exp);
            text = "";
            for (int i=0; i < 10-exp.length(); i++) {
                text += " ";
            }
            textArray = (text+exp).toCharArray();
            for (int j=0; j < textArray.length; j++) {
                letterSprite = game.transparentDict.get(textArray[j]);
                game.uiBatch.draw(letterSprite, 80 +8*j, 56);
            }
            // Draw health bar
            int targetSize = (int)Math.ceil((this.pokemon.currentStats.get("hp")*48) / this.pokemon.maxStats.get("hp"));
            for (int j=0; j < targetSize; j++) {
                game.uiBatch.draw(this.healthSprite, 16 +1*j, 67);
            }
            // Exp to next level
            exp = String.valueOf(this.pokemon.gen2CalcExpForLevel(this.pokemon.level+1)-this.pokemon.exp);
            text = "";
            for (int i=0; i < 10-exp.length(); i++) {
                text += " ";
            }
            textArray = (text+exp).toCharArray();
            for (int j=0; j < textArray.length; j++) {
                letterSprite = game.transparentDict.get(textArray[j]);
                game.uiBatch.draw(letterSprite, 80 +8*j, 32);
            }
            // Draw next pokemon level
            textArray = String.valueOf(pokemon.level+1).toCharArray();
            for (int j=0; j < textArray.length; j++) {
                letterSprite = game.transparentDict.get(textArray[j]);
                game.uiBatch.draw(letterSprite, 144 +8*j, 24);
            }
        }
        else if (this.currIndex == 1) {
            // Draw moves
            for (int i=0; i < this.pokemon.attacks.length; i++) {
                // Attack name
                String attack = this.pokemon.attacks[i];
                if (attack == null) {
                    attack = "-";
                }
                textArray = attack.toUpperCase().toCharArray();
                for (int j=0; j < textArray.length; j++) {
                    letterSprite = game.transparentDict.get(textArray[j]);
                    game.uiBatch.draw(letterSprite, 64 +8*j, 56 -i*16);
                }
                // TODO: later draw power points
            }
        }
        else {
            // OT which is just trainer name for now
            if (this.pokemon.previousOwner != null) {
                textArray = this.pokemon.previousOwner.name.toUpperCase().toCharArray();
                for (int j=0; j < textArray.length; j++) {
                    letterSprite = game.transparentDict.get(textArray[j]);
                    game.uiBatch.draw(letterSprite, 16 +8*j, 32);
                }
            }
            // Draw all stats
            // 152-16, 64 -16*i
            String[] allStats = new String[]{"attack", "defense", "specialAtk", "specialDef", "speed"};
            for (int i=0; i < allStats.length; i++) {
                String val = String.valueOf(this.pokemon.maxStats.get(allStats[i]));
                String text = "";
                for (int k=0; k < 3-val.length(); k++) {
                    text += " ";
                }
                textArray = (text+val).toCharArray();
                for (int j=0; j < textArray.length; j++) {
                    letterSprite = game.transparentDict.get(textArray[j]);
                    game.uiBatch.draw(letterSprite, 152-16 +8*j, 64 -16*i);
                }
            }
        }
       
     // Handle arrow input
        if (InputProcessor.upJustPressed && this.prevMenu != null) {
            int newIndex = pokeIndex-1;
        	newIndex = newIndex < 0 ? game.player.pokemon.size()-1 : newIndex;
        	if(newIndex != pokeIndex)
        		this.scrollToNewPokemon(game, newIndex);
        }
        else if (InputProcessor.downJustPressed && this.prevMenu != null) {
        	int newIndex = pokeIndex+1;
        	newIndex = newIndex >= game.player.pokemon.size() ? 0 : newIndex;
        	if(newIndex != pokeIndex)
        		this.scrollToNewPokemon(game, newIndex);
        }
        else if (InputProcessor.leftJustPressed) {
            if (this.currIndex > 0) {
                this.currIndex -= 1;
            }
        }
        else if (InputProcessor.rightJustPressed) {
            if (this.currIndex < 2) {
                this.currIndex += 1;
            }
        }
        if (InputProcessor.bJustPressed && this.prevMenu != null) {
            game.actionStack.remove(this);
            game.insertAction(new DrawStatsScreen.Outro(this.prevMenu));
            return;
        }
    }
    
    //added to reduce code repetition
    private void scrollToNewPokemon(Game game, int newIndex) {	
		//reset the animation if it's still playing
		this.intro.currFrame = this.pokemon.introAnim.size();
		//set Party menu to point at current pokemon
		DrawPokemonMenu.currIndex = newIndex;
		game.actionStack.remove(this);
		DrawStatsScreen newScreen = new DrawStatsScreen(game,game.player.pokemon.get(newIndex),this.prevMenu);
		//set the new screen to be on the same tab
		newScreen.currIndex = this.currIndex;
		game.insertAction(new DrawStatsScreen.Intro(newScreen));    	    
    }
    
    static class Intro extends Action {
        public int layer = 110;
        int duration = 30;
        Sprite bgSprite;

        public Intro(Action nextAction) {
            this.nextAction = nextAction;
            Texture text = new Texture(Gdx.files.internal("battle/intro_frame6.png"));
            this.bgSprite = new Sprite(text, 0, 0, 16*10, 16*9);
        }

        public Intro(int duration, Action nextAction) {
            this(nextAction);
            this.duration = duration;
        }

        public String getCamera() {return "gui";}

        public int getLayer(){return this.layer;}

        @Override
        public void step(Game game) {
            this.bgSprite.draw(game.uiBatch);
            this.duration--;
            if (this.duration <= 0) {
                game.insertAction(this.nextAction);
                game.actionStack.remove(this);
            }
        }
    }

    static class Outro extends Action {
        // 33 frames white
        // 1 frame where cursor is still white and can't move cursor (people aren't visible)
        // 1 frame people aren't visible (not implementing)
        MenuAction prevMenu;
        public int layer = 110;
        int duration = 34;
        Sprite bgSprite;

        public Outro(MenuAction prevMenu) {
            this.prevMenu = prevMenu;
            if (this.prevMenu != null) {
                this.prevMenu.drawArrowWhite = true;
            }

            Texture text = new Texture(Gdx.files.internal("battle/intro_frame6.png"));
            this.bgSprite = new Sprite(text, 0, 0, 16*10, 16*9);
        }
        public String getCamera() {return "gui";}

        public int getLayer(){return this.layer;}

        @Override
        public void step(Game game) {
            if (this.prevMenu != null) {
                this.prevMenu.step(game);
            }

            this.duration--;

            // last from no white bg
            if (this.duration > 0) {
                this.bgSprite.draw(game.uiBatch);
            }

            if (this.duration <= 0) {
                if (this.prevMenu != null) {
                    game.insertAction(this.prevMenu);
                    this.prevMenu.disabled = false;
                    this.prevMenu.drawArrowWhite = false;
                }
                game.insertAction(this.nextAction);
                game.actionStack.remove(this);
            }
        }
    }
}




