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
import com.pkmngen.game.util.LinkedMusic;

class AfterFriendlyFaint extends Action {
    public int layer = 129;

    public AfterFriendlyFaint() {}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        game.actionStack.remove(this);
        boolean hasAlivePokemon = false;
        for (Pokemon pokemon : game.player.pokemon) {
            if (pokemon.currentStats.get("hp") > 0) {
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
            pokemon.currentStats.put("hp", pokemon.maxStats.get("hp")/2);
        }
        BattleFadeOut.whiteScreen = true;
        BattleFadeOutMusic.playerFainted = true;
        game.insertAction(new DisplayText.Clear(game,
                          new WaitFrames(game, 3,new DisplayText(game, ""+game.player.name.toUpperCase()+" is out of useable POKÈMON!", null, null,
                          new DisplayText(game, ""+game.player.name.toUpperCase()+" whited out!", null, null,
                          // TODO: remove
//                          new SplitAction(
//                              new BattleFadeOut(game,
//                              new DoneWithDemo(game)),
                          // TODO: this puts the player in the overworld
                          // will eventually want to be able to respawn indoors
                          // probably something like game.player.spawnLocTiles
                          new SetField(game.player, "position", game.player.spawnLoc.cpy(),
                          new SetField(game.map, "tiles", game.map.overworldTiles,
                          new SetField(game.map, "interiorTilesIndex", 100,
                          new SetField(game.player, "dirFacing", "down",
                          new SetField(game.player, "currSprite", game.player.standingSprites.get("down"),
                          new Game.SetCamPos(game.player.spawnLoc.cpy().add(16, 0),
                          new SplitAction(new BattleFadeOut(game, 4, null),
                          new BattleFadeOutMusic(game,
                          new DisplayText(game, "Weary from battle, you flee to the last known safe place...", null, null,
                          new BattleFadeOut.WhiteScreen(false,
                          new SplitAction(new SetField(game, "currMusic", game.map.currRoute.music,
                                          new FadeMusic("currMusic", "in", "", 0.2f, false, 1f,
                                          null)),
//                          new SplitAction(new FadeIn(),
                          null))))))))))))))));
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
    int power;
    String type;
    int accuracy;
    int pp;
    int effectChance; // chance to paralyze, lower speed, poison, etc.
    boolean isPhysical = false;

    // Network-related things
    int damage;

    public Attack() {}

    public Attack(String name, int power, String type, int accuracy, int pp, int effectChance) {
        this.name = name;
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
        // TODO: move the Battle_Actions.getAttackAction code here
        game.actionStack.remove(this);
        game.insertAction(Battle.getAttackAction(game, attack, isFriendly, nextAction));
    }
}

public class Battle {
    /*
     * Calculate an attack's damage.
     *
     * Note: below is taken based off of Gen 2.
     */
    static int calcDamage(Pokemon source, Attack attack, Pokemon target) {
        if (attack.name.equals("Mewtwo_Special1")) {
            return 30;
        }
        int attackStat = attack.isPhysical ? source.currentStats.get("attack") : source.currentStats.get("specialAtk");
        int defenseStat = attack.isPhysical ? target.currentStats.get("defense") : target.currentStats.get("specialDef");
        int damage = (int)Math.floor(Math.floor(Math.floor(2 * source.level / 5 + 2) * attackStat * attack.power / defenseStat) / 50) + 2;
        if (source.types.contains(attack.type)) {damage = (int)(damage * 1.5f);}  // STAB
        // factor in type effectiveness
        float multiplier = 1f;
        for (String type : target.types){
            multiplier *= Game.staticGame.battle.gen2TypeEffectiveness.get(attack.type).get(type.toLowerCase());
        }
        damage = (int)(damage * multiplier);
        return damage;
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
                //, where the value of Ball is 255 for the PokÈ Ball, 200 for the Great Ball, or 150 for other balls
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
        // TODO: other pokeballs
//      else if (ballUsed.equals("heavy ball")) {
//      }
        else if (ballUsed.equals("fast ball") && (pokemon.name.equals("tangela") || 
                                                  pokemon.name.equals("grimer")  || 
                                                  pokemon.name.equals("magnemite"))) {
            rateModified *= 4;
        }
        if (rateModified > 255){
            rateModified = 255;
        }
        int bonusStatus = 0;  // TODO: sleep/frozen bonus 
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
        System.out.println(b);
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
                    mewtwoSpecial1.damage = Battle.calcDamage(game.battle.oppPokemon, mewtwoSpecial1, game.player.currPokemon);
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
            String effectiveness;
            String text_string = "";
            Action attackAction;
            if (isFriendly) {
                // TODO: string based on effectiveness
                // TODO: 'no effect' attacks
                // attack data loaded from Crystal
                String attackType = attack.type;
                float multiplier = game.battle.gen2TypeEffectiveness.get(attackType).get(game.battle.oppPokemon.types.get(0).toLowerCase());
                if (game.battle.oppPokemon.types.size() > 1) {
                    multiplier *= game.battle.gen2TypeEffectiveness.get(attackType).get(game.battle.oppPokemon.types.get(1).toLowerCase());
                }
                if (multiplier > 1f) {
                    effectiveness = "super_effective";
                    text_string = "It' super- effective!";
                }
                else if (multiplier == 1f) {
                    effectiveness = "neutral_effective";
                }
                else {
                    effectiveness = "not_very_effective";
                    text_string = "It' not very effective...";
                }
                attackAction =  new LoadAndPlayAnimation(game, attack.name, game.battle.oppPokemon,
                                new LoadAndPlayAnimation(game, effectiveness, game.battle.oppPokemon,
                                new DepleteEnemyHealth(game, attack.damage,
                                new WaitFrames(game, 13,
                                !effectiveness.equals("neutral_effective") ?
                                    new DisplayText.Clear(game,
                                    new WaitFrames(game, 3,
                                    new DisplayText(game,
                                                    text_string,
                                                    null, true, true,
                                    null)))
                                :
                                    null))));
                // check if attack traps target pokemon
                if (game.battle.oppPokemon.trappedBy != null) {
                    attackAction.append(new DisplayText.Clear(game,
                                        new WaitFrames(game, 3,
                                        new DisplayText(game, game.battle.oppPokemon.name.toUpperCase()+" was trapped!",
                                                        null, true, true,
                                        null))));
                }
            }
            else {
                String attackType = game.battle.attacks.get(attack.name.toLowerCase()).type;
                float multiplier = game.battle.gen2TypeEffectiveness.get(attackType).get(game.player.currPokemon.types.get(0).toLowerCase());
                if (game.player.currPokemon.types.size() > 1) {
                    multiplier *= game.battle.gen2TypeEffectiveness.get(attackType).get(game.player.currPokemon.types.get(1).toLowerCase());
                }
                if (multiplier > 1f) {
                    effectiveness = "super_effective";
                    text_string = "It' super- effective!";
                }
                else if (multiplier == 1f) {
                    effectiveness = "neutral_effective";
                }
                else {
                    effectiveness = "not_very_effective";
                    text_string = "It' not very effective...";
                }
                attackAction = new LoadAndPlayAnimation(game, attack.name, game.player.currPokemon,
                               new LoadAndPlayAnimation(game, effectiveness, game.player.currPokemon,
                               new DepleteFriendlyHealth(game.player.currPokemon, attack.damage,
                               new WaitFrames(game, 13,
                               !effectiveness.equals("neutral_effective") ?
                                   new DisplayText.Clear(game,
                                   new WaitFrames(game, 3,
                                   new DisplayText(game, text_string,
                                                   null, true, true,
                                   new WaitFrames(game, 3,
                                   null))))
                               :
                                   null))));
                // Check if attack traps target pokemon
                if (game.player.currPokemon.trappedBy != null) {
                    attackAction.append(new DisplayText.Clear(game,
                                          new WaitFrames(game, 3,
                                          new DisplayText(game,
                                                          game.player.currPokemon.name.toUpperCase()+" was trapped!",
                                                          null,
                                                          true,
                                                          true,
                                          null))));
                }
            }
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
                   new DisplayText(game, "Wild "+game.battle.oppPokemon.name.toUpperCase()+" appeared!", null, null,
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
                   )))))))));
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
    Pokemon oppPokemon;

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
        this.music = new LinkedMusic("battle/battle-vs-wild-pokemon3", "battle/battle-vs-wild-pokemon2");
        this.music.setVolume(0.3f);

//        this.victoryFanfare = Gdx.audio.newMusic(Gdx.files.internal("victory_fanfare2.ogg"));
//        this.victoryFanfare.setLooping(true);
//        this.victoryFanfare.setVolume(0.3f);
        this.victoryFanfare = new LinkedMusic("victory_fanfare1_intro", "victory_fanfare1");
        this.victoryFanfare.setVolume(1f);

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
        this.gen2TypeEffectiveness.get("fire").put("steel", 0.5f);
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

        // load all attacks and attributes
        try {
            FileHandle file = Gdx.files.internal("crystal_pokemon/moves.asm");
            Reader reader = file.reader();
            BufferedReader br = new BufferedReader(reader);
            String line;
            int lineNum = 1;
            while ((line = br.readLine()) != null)   {
                // TODO: using table to look up number now
//                if (lineNum == 0) {
//                    this.dexNumber = line.split(" ; ")[1];
//                } else
                if (lineNum > 14 && lineNum < 266) {
                    String[] attrs = line.split("\tmove ")[1].split(",\\s+");
                    Attack attack = new Attack(attrs[0].toLowerCase().replace('_', ' '), Integer.valueOf(attrs[2]),
                                               attrs[3].toLowerCase(), Integer.valueOf(attrs[4]),
                                               Integer.valueOf(attrs[5]), Integer.valueOf(attrs[6]));
                    if (gen2PhysicalTypes.contains(attack.type.toLowerCase())) {
                        attack.isPhysical = true;
                    }
                    this.attacks.put(attack.name, attack);
//                    System.out.println(attack.name + " " + attack.type);
                }
                lineNum++;
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
                    Attack attack = new Attack(attrs[0].toLowerCase().replace('_', ' '), Integer.valueOf(attrs[2]),
                                               attrs[3].toLowerCase(), Integer.valueOf(attrs[5]),
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
        Attack attack = new Attack("Mewtwo_Special1", 0, "psychic", 100, 1, 100);
        this.attacks.put(attack.name, attack);
    }

    /*
     * Reference - https:// bulbapedia.bulbagarden.net/wiki/Experience#Example_.28Generation_II_to_IV.29
     */
    int calcFaintExp() {
        int a = 1; // TODO: 1.5 if owned by trainer
        int t = 1; // TODO: 1.5 if traded
        int b = this.oppPokemon.baseStats.get("baseExp");
        int e = 1; // TODO: 1.5 if curr pokemon holding lucky egg
        int l = this.oppPokemon.level;
        int s = 1; // TODO: equals number of pokemon participated in battle that didn't faint
        int exp = (a*t*b*e*l)/(7*s);
        // TODO: leveling takes too long, doing exp*2 for now. May remove this in the future if way
        // to get more exp is added.
        return exp*2;
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

    /*
     * TODO: remove if unused
     */
    class CheckTrapped extends Action {
        public int layer = 500;

        public CheckTrapped(Game game, Action nextAction) {
            this.nextAction = nextAction;
        }

        public int getLayer(){return this.layer;}

        public void step() {
            // TODO: trying out method where can only reference parent battle object, using Battle.this
            // probably revert at some point
            // TODO: if keeping, refactor to remove references to Game.staticGame
            //  likely need global actionStack or something

            // always goes you, then opponent
            Game.staticGame.actionStack.remove(this);
            if (Battle.this.oppPokemon.trappedBy != null) {
                this.nextAction = new Battle.LoadAndPlayAnimation(Game.staticGame, Battle.this.oppPokemon.trappedBy, Battle.this.oppPokemon,
                                  new DisplayText.Clear(Game.staticGame,
                                  new WaitFrames(Game.staticGame, 3,
                                  new DisplayText(Game.staticGame,
                                                  Battle.this.oppPokemon.name.toUpperCase()+"' hurt by "+Battle.this.oppPokemon.trappedBy.toUpperCase()+"!",
                                                  null,
                                                  true,
                                  new DepleteEnemyHealth(Game.staticGame,
                                  new WaitFrames(Game.staticGame, 13,
                                  this.nextAction))))));
                Battle.this.oppPokemon.trapCounter -= 1;
                if (Battle.this.oppPokemon.trapCounter <= 0) {
                    Battle.this.oppPokemon.trappedBy = null;
                }
            }
            if (Game.staticGame.player.currPokemon.trappedBy != null) {
                this.nextAction = new Battle.LoadAndPlayAnimation(Game.staticGame, Game.staticGame.player.currPokemon.trappedBy, Game.staticGame.player.currPokemon,
                                  new DisplayText.Clear(Game.staticGame,
                                  new WaitFrames(Game.staticGame, 3,
                                  new DisplayText(Game.staticGame,
                                                  Game.staticGame.player.currPokemon.name.toUpperCase()+"' hurt by "+Game.staticGame.player.currPokemon.trappedBy.toUpperCase()+"!",
                                                  null, true,
                                  new DepleteFriendlyHealth(Game.staticGame.player.currPokemon,
                                  new WaitFrames(Game.staticGame, 13,
                                  this.nextAction))))));
                Game.staticGame.player.currPokemon.trapCounter -= 1;
                if (Battle.this.oppPokemon.trapCounter <= 0) {
                    Battle.this.oppPokemon.trappedBy = null;
                }
            }
            Game.staticGame.insertAction(this.nextAction);
        }

        @Override
        public void step(Game game) {
            this.step();
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
                        // Get run result from server
                        numWobbles = turnData.numWobbles;
                    }
                    // TODO: remove
//                    System.out.println("numWobbles");
//                    System.out.println(numWobbles);
                    // TODO: refactors, stop using catchAction, catchPokemon_wigglesThenCatch needs to insert nextAction
                    // currently it just ignores nextAction.
                    Action catchAction;
                    if (numWobbles == 0) {
                        catchAction = new CatchPokemonMiss(game,
//                                      new PrintAngryEating(game,
//                                      new ChanceToRun(game,
                                      null);
                    }
                    else if (numWobbles == 1) {
                        catchAction = new CatchPokemonWobbles1Time(game,
//                                      new PrintAngryEating(game,
//                                      new ChanceToRun(game,
                                      null);
                    }
                    else if (numWobbles == 2) {
                        catchAction = new CatchPokemonWobbles2Times(game,
//                                      new PrintAngryEating(game,
//                                      new ChanceToRun(game,
                                      null);
                    }
                    else if (numWobbles == 3) {
                        catchAction = new CatchPokemonWobbles3Times(game,
//                                      new PrintAngryEating(game,
//                                      new ChanceToRun(game,
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
                else {
                    playerAction =  new DisplayText(game, "Dev note - Invalid item.",
                                                    null, null,
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
                                                      null),
                                      new BattleFadeOutMusic(game,
                                      null)))));
                    game.insertAction(new PlaySound("run1", null));
                    game.battle.network.turnData = null;
                    return;
                }
                // Failed to run away if we got here.
                game.player.numFlees++;
                playerAction = new DisplayText(game, "CanÏ escape!",
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
                    // TODO: determine if hit/miss, crit, effect hit, etc
                    playerAttack = game.battle.attacks.get(game.player.currPokemon.attacks[DrawAttacksMenu.curr].toLowerCase());
                    playerAttack.damage = Battle.calcDamage(game.player.currPokemon, playerAttack, game.battle.oppPokemon);

                    if (game.battle.oppPokemon.trappedBy == null &&
                        playerAttack.name.toLowerCase().equals("whirlpool") ||
                        playerAttack.name.toLowerCase().equals("fire spin") ||
                        playerAttack.name.toLowerCase().equals("wrap") ||
                        playerAttack.name.toLowerCase().equals("clamp")) {
                        // 2-5 turns for trap
                        game.battle.oppPokemon.trappedBy = playerAttack.name.toLowerCase();
                        game.battle.oppPokemon.trapCounter = game.map.rand.nextInt(4) + 2;
                    }
                }
                // if this is a CLIENT, get all outcomes of attacks etc from the server.
                else {
                    BattleTurnData turnData = game.battle.network.turnData;
                    oppFirst = turnData.oppFirst;
                    playerAttack = turnData.playerAttack;
                    game.player.currPokemon.trappedBy = turnData.playerTrappedBy;
                    game.player.currPokemon.trapCounter = turnData.playerTrapCounter;
                }
                playerAction =  new DisplayText(game,
                                                game.player.currPokemon.name.toUpperCase()+" used "+playerAttack.name.toUpperCase()+"!",
                                                null, true, false,
                                new AttackAnim(game, playerAttack, isFriendly,
                                null));
            }

            // If expecting player to switch, enemy does nothing
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
                // set up enemy attack
                String attackChoice = game.battle.oppPokemon.attacks[game.map.rand.nextInt(game.battle.oppPokemon.attacks.length)];
                if (attackChoice == null) {
                    attackChoice = "Struggle";
                }
                enemyAttack = game.battle.attacks.get(attackChoice.toLowerCase());
                // TODO: debug, remove
//                for (int i=0; i<game.battle.oppPokemon.attacks.length;i++) {
//                    System.out.println(game.battle.oppPokemon.attacks[i]);
//                }
//                System.out.println(attackChoice);
//                System.out.println(enemyAttack.name);
                enemyAttack.damage = Battle.calcDamage(game.battle.oppPokemon, enemyAttack, game.player.currPokemon);

                // check if attack traps player pokemon
                if (game.player.currPokemon.trappedBy == null &&
                    enemyAttack.name.toLowerCase().equals("whirlpool") ||
                    enemyAttack.name.toLowerCase().equals("fire spin") ||
                    enemyAttack.name.toLowerCase().equals("wrap") ||
                    enemyAttack.name.toLowerCase().equals("clamp")) {
                    // 2-5 turns for trap
                    game.player.currPokemon.trappedBy = enemyAttack.name.toLowerCase();
                    game.player.currPokemon.trapCounter = game.map.rand.nextInt(4) + 2;
                }
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

            Action enemyAction =  new DisplayText(game, "Enemy "+game.battle.oppPokemon.name.toUpperCase()+" used "+enemyAttack.name.toUpperCase()+"!",
                                                  null, true, false,
                                  new AttackAnim(game, enemyAttack, !isFriendly,
                                  null));
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
            // Always goes you, then opponent for trap check
            if (game.player.currPokemon.trappedBy != null) {
                Attack trap = game.battle.attacks.get(game.player.currPokemon.trappedBy);
                trap.damage = Battle.calcDamage(game.battle.oppPokemon, trap, game.player.currPokemon);  // TODO: does STAB apply to traps?
                doTurn.append(new Battle.LoadAndPlayAnimation(game, game.player.currPokemon.trappedBy, game.player.currPokemon,
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
                trap.damage = Battle.calcDamage(game.battle.oppPokemon, trap, game.player.currPokemon);  // TODO: does STAB apply to traps?
                doTurn.append(new Battle.LoadAndPlayAnimation(game, game.battle.oppPokemon.trappedBy, game.battle.oppPokemon,
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

    // Lick attack animation
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
        Vector2 enemySpriteOrigin;
        Pixmap pixmap;
        int pixmapX;
        int pixmapY;

        public LoadAndPlayAnimation(Game game, String name, Pokemon target, Action nextAction) {
            this.name = name.toLowerCase().replace(' ', '_');
            if (target == game.player.currPokemon) {
                this.name = this.name+"_enemy_gsc";
            }
            else if (target == game.battle.oppPokemon) {
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
                    game.insertAction(new Attack.Psychic(game, this.target,
                                                         false, this.nextAction));
                    return;
                }

                // load metadata for each frame
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
                this.playerSpriteOrigin = new Vector2(game.player.currPokemon.backSprite.getX(),
                                                      game.player.currPokemon.backSprite.getY());
                this.enemySpriteOrigin = new Vector2(game.battle.oppPokemon.sprite.getX(),
                                                     game.battle.oppPokemon.sprite.getY());
            }
 
            // Reset vars at beginning
            DrawEnemyHealth.shouldDraw = true;
            DrawFriendlyHealth.shouldDraw = true;
            DrawBattle.shouldDrawOppPokemon = true;
            EvolutionAnim.isGreyscale = false;
   //            EvolutionAnim.playStarterCry = false;  // TODO: remove
   //            EvolutionAnim.playEvoCry = false;
            game.player.currPokemon.backSprite.setPosition(this.playerSpriteOrigin.x, this.playerSpriteOrigin.y);
            game.battle.oppPokemon.sprite.setPosition(this.enemySpriteOrigin.x, this.enemySpriteOrigin.y);
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
 
            // draw current frame
            this.currText = new Texture(filehandle);
            this.currFrame = new Sprite(this.currText, 0, 0, 160, 144);
            this.currFrame.draw(game.uiBatch);
 
            // handle metadata
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
                if (properties.contains("sprite_greyscale")) {
                    EvolutionAnim.isGreyscale = true;
                }
                if (properties.contains("play_evo_fanfare")) {
                    EvolutionAnim.playSound = true;
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

        game.battle.oppPokemon.sprite.setPosition(-30-4-1-14,106+2-5-15);//(3*-30,3*106+2); // TODO - x and y pos not correct...
//        game.battle.oppPokemon.sprite.setPosition(-30-4-1-14-24,106+2-5-15-16);  // used for saving video of pokemon sprite
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
        // should only happen once. not sure that repeat matters
        game.actionStack.remove(game.battle.drawAction); // stop drawing the battle
        game.battle.drawAction = null;

        // if done with anim, do nextAction
        if (frames.isEmpty()) {
            game.insertAction(this.nextAction);
            game.actionStack.remove(this);
            game.playerCanMove = true;
            DrawBattle.shouldDrawOppPokemon = true;
            // TODO: gameboy game handles this differently
            // TODO: remove
//            game.player.currPokemon = game.player.pokemon.get(0);
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
    boolean firstStep = true;
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
    public void step(Game game) {
        if (game.map.timeOfDay.equals("Night") && !BattleFadeOutMusic.playerFainted) {
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
            return;
        }

        if (this.firstStep == true) {
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
            FadeMusic.pause = false;
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

        // animation to play
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
        for (int i = 0; i < 42; i++) {
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

        String textString = "You missed the POKÈMON!";
        game.insertAction(new WaitFrames(game, 3,
                                                new DisplayText(game, textString, null, null, this.nextAction)
                                            )
                                        );

        game.actionStack.remove(this);
        return;
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
            String textString = "Darn! The POKÈMON broke free!";
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
    public void step(Game game) {
        // set opp pokemon sprite alpha
//        game.battle.oppPokemon.sprite.setAlpha(0);
        DrawBattle.shouldDrawOppPokemon = false;

        // Set sprite position
        // if done with anim, do nextAction
        if (positions.isEmpty() || sprites.isEmpty()) {
            // Since pkmn was caught, add to players pokemon
            game.player.pokemon.add(game.battle.oppPokemon);
            if (SpecialMewtwo1.class.isInstance(game.battle.oppPokemon)) {
                // Remove mewtwo from it's tile
                Tile tile = ((SpecialMewtwo1)game.battle.oppPokemon).tile;
                tile.nameUpper = "";
                tile.overSprite = null;
                tile.attrs.put("solid", false);
            }
            else {
                // Remove this pokemon from the map
                game.map.currRoute.pokemon.remove(game.battle.oppPokemon);
                game.map.currRoute.genPokemon(game.battle.oppPokemon.baseStats.get("catchRate"));
            }
            Action newAction = new PokemonCaughtEvents(game,
                               new SplitAction(new BattleFadeOut(game, null),
                               new BattleFadeOutMusic(game,
                               null)));
            if (game.player.pokemon.size() >= 6 && !game.player.displayedMaxPartyText) {
                game.player.displayedMaxPartyText = true;
                newAction.append(new SetField(game, "playerCanMove", false,
                                 new DisplayText(game, "Your party is full! You will need to DROP some of them in order to catch more.", null, null,
                                 new SetField(game, "playerCanMove", true,
                                 null))));
            }
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
        if (this.firstStep == true) {
            // apply damage if there is any (otherwise assume it's already applied)
            if (this.damage > 0) {
                int currHealth = game.battle.oppPokemon.currentStats.get("hp");
                int finalHealth = currHealth - this.damage > 0 ? currHealth - this.damage : 0;
                game.battle.oppPokemon.currentStats.put("hp", finalHealth);
            }
            this.targetSize = (int)Math.ceil( (game.battle.oppPokemon.currentStats.get("hp")*48) / game.battle.oppPokemon.maxStats.get("hp"));
            this.firstStep = false;
        }

        this.timer++;
        if (this.timer < 4) {
            return;
        }
        this.timer = 0;

        int size = 0;
        for (int i = 0; i < this.removeNumber; i++) {
            size = game.battle.drawAction.drawEnemyHealthAction.healthBar.size();
            if (size <= targetSize) {
                break;
            }
            game.battle.drawAction.drawEnemyHealthAction.healthBar.remove(size-1);
        }

        if (this.removeNumber == 2) {
            this.removeNumber = 1;
        }
        else {
            this.removeNumber = 2;
        }

        if (size <= targetSize) {
            game.actionStack.remove(this);
            // If enemy health is 0, do EnemyFaint
            if (game.battle.oppPokemon.currentStats.get("hp") <= 0) {
                int exp = game.battle.calcFaintExp();
                game.player.currPokemon.exp += exp;
                Action nextAction = new EnemyFaint(game,
                                    new RemoveDisplayText(  // TODO: refactor to stop using this
                                    new DisplayText.Clear(game,
                                    new WaitFrames(game, 3,
                                    new DisplayText(game, "Enemy "+game.battle.oppPokemon.name.toUpperCase()+" fainted!",
                                                    null, null,
                                    new DisplayText(game, game.player.currPokemon.name.toUpperCase()+" gained "+String.valueOf(exp)+" EXP. Points!",
                                                    null, true, true,
                                    new GainExpAnimation(
                                    null)))))));
                nextAction.append(new SplitAction(new BattleFadeOut(game,
                                                  null),
                                  new BattleFadeOutMusic(game,
                                  null)));
                // If player made mewtwo faint, display message that it may return
                // Also don't draw upper sprite, make unsolid.
                if (SpecialMewtwo1.class.isInstance(game.battle.oppPokemon)) {
                    SpecialMewtwo1 mewtwo = (SpecialMewtwo1)game.battle.oppPokemon;
                    mewtwo.tile.attrs.put("solid", false);
                    mewtwo.tile.nameUpper = "mewtwo_overworld_hidden";
                    mewtwo.tile.overSprite = null;
                    nextAction.append(new SetField(game, "playerCanMove", false,
                                      new DisplayText(game, "MEWTWO fled... it may return when it' had time to recover.",
                                                      null, null,
                                      new SetField(game, "playerCanMove", true,
                                      null))));
                }
                game.insertAction(nextAction);
            }
            // else, insert nextAction
            else {
                game.insertAction(this.nextAction);
            }
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
        if (this.firstStep == true) {
            // apply damage if there is any (otherwise assume it's already applied)
            if (this.damage > 0) {
                int currHealth = game.player.currPokemon.currentStats.get("hp");
                int finalHealth = currHealth - this.damage > 0 ? currHealth - this.damage : 0;
                game.player.currPokemon.currentStats.put("hp", finalHealth);
            }
            this.targetSize = (int)Math.ceil( (this.pokemon.currentStats.get("hp")*48) / this.pokemon.maxStats.get("hp"));
            this.firstStep = false;
        }

        this.timer++;
        if (this.timer < 4) {
            return;
        }
        this.timer = 0;

        int size = 0;
        for (int i = 0; i < this.removeNumber; i++) {
            size = game.battle.drawAction.drawFriendlyHealthAction.healthBar.size();
            if (size <= targetSize) {
                break;
            }
            game.battle.drawAction.drawFriendlyHealthAction.healthBar.remove(size-1);
        }

        // alternate between removing 2 and removing 1
        if (this.removeNumber == 2) {
            this.removeNumber = 1;
        }
        else {
            this.removeNumber = 2;
        }

        // If health is 0, this pokemon should faint
        if (size <= targetSize) {
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

    public DrawAttacksMenu(Action nextAction) {
        this(null, nextAction);
    }

    public DrawAttacksMenu(String attackLearning, Action nextAction) {
        this.attackLearning = attackLearning;
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
        for (String attack : Game.staticGame.player.currPokemon.attacks) {
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
            if (DrawAttacksMenu.curr+1 < game.player.currPokemon.attacks.length &&
                game.player.currPokemon.attacks[DrawAttacksMenu.curr+1] != null) {
                DrawAttacksMenu.curr += 1;
                newPos = coords.get(DrawAttacksMenu.curr);
            }
        }

        // if press a, do attack
        if (InputProcessor.aJustPressed) {
            if (this.attackLearning != null) {
                game.actionStack.remove(this);
                game.insertAction(new DisplayText.Clear(game,
                                  new WaitFrames(game, 3,
                                  new DisplayText(game, "And one... two... three... POOF!",
                                                  null, null,
                                  new DisplayText(game, game.player.currPokemon.name.toUpperCase() + " forgot " + game.player.currPokemon.attacks[DrawAttacksMenu.curr].toUpperCase()+" and...",
                                                  null, null,
                                  new DisplayText(game, game.player.currPokemon.name.toUpperCase() + " learned " + this.attackLearning.toUpperCase()+"!",
                                                  "fanfare1.ogg", true, true,
                                  this.nextAction))))));
                game.player.currPokemon.attacks[DrawAttacksMenu.curr] = this.attackLearning;
                if (game.type == Game.Type.CLIENT) {
                    game.client.sendTCP(new Network.LearnMove(game.player.network.id, 0, DrawAttacksMenu.curr, this.attackLearning));
                }
                return;
            }
            // Reset counter keeping track of number flees done by player this battle (used in run away mechanic).
            game.player.numFlees = 0;

            // explanation of speed move priority: http:// bulbapedia.bulbagarden.net/wiki/Stats#Speed
             // pkmn with higher speed moves first

            // play select sound
            Action attack = new SplitAction(new PlaySound("click1", null), null);
            if (game.type == Game.Type.CLIENT) {
                // send move to server, wait response
                String attackName = game.player.currPokemon.attacks[DrawAttacksMenu.curr];
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
                game.insertAction(new DisplayText.Clear(game,
                                  new WaitFrames(game, 3,
                                  new DisplayText(game, game.player.currPokemon.name.toUpperCase()+" did not learn " + this.attackLearning.toUpperCase()+".",
                                                  null, null,
                                  this.nextAction))));
                return;
            }
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
        }

        // System.out.println("curr: " + curr);

        // draw text box
        this.textBox.draw(game.uiBatch);

        // debug
//        helperSprite.draw(game.floatingBatch);

        // draw the attack strings
        int j = 0;
        for (ArrayList<Sprite> word : this.spritesToDraw) {
            for (Sprite sprite : word) {
                // convert string to text
                game.uiBatch.draw(sprite, sprite.getX() + 40, sprite.getY() - j*8 + 8);
            }
            j+=1;
        }

        // draw arrow
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
 // TODO - on destroy, set drawAction to null?
class DrawBattle extends Action {
    public static boolean shouldDrawOppPokemon = true;
    boolean doneNightOverlay = false;
    Sprite bgSprite;
    Sprite bgSprite2;
    DrawFriendlyHealth drawFriendlyHealthAction;
    DrawEnemyHealth drawEnemyHealthAction;

    Action drawFriendlyPokemonAction;
    public int layer = 130;

    public DrawBattle(Game game) {
        Texture text = new Texture(Gdx.files.internal("battle/battle_bg3.png"));
        this.bgSprite = new Sprite(text, 0, 0, 176, 160);
        game.battle.drawAction = this;
        this.bgSprite.setPosition(-8, -8);
        text = new Texture(Gdx.files.internal("battle/intro_frame6.png"));
        this.bgSprite2 = new Sprite(text, 0, 0, 160, 144);
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        if (!this.doneNightOverlay) {
            if (game.map.timeOfDay.equals("Night") && (game.battle.oppPokemon == null || !SpecialMewtwo1.class.isInstance(game.battle.oppPokemon))) {
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
            if (!game.actionStack.contains(DrawBattle.this) || game.map.timeOfDay.equals("Day")) {
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

            // user selected 'run'
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
//                                                 new DisplayText(game, "CanÏ Escape!", null, null,
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
                //, where the value of Ball is 255 for the PokÈ Ball, 200 for the Great Ball, or 150 for other balls
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
    Sprite bgSprite;

    public ArrayList<Sprite> healthBar;
    public int layer = 129;
    public DrawEnemyHealth(Game game) {
        Texture text = new Texture(Gdx.files.internal("battle/enemy_healthbar1.png"));
        this.bgSprite = new Sprite(text, 0, 0, 160, 144);

        // this could be dangerous?
        // if drawAction is null. this may happen at some point.
        game.battle.drawAction.drawEnemyHealthAction = this;

        // fill sprite array according to enemy health
         // healthbar is 48 pixels long
         // round up when counting
        this.healthBar = new ArrayList<Sprite>();
        int numElements = (int)Math.ceil( (game.battle.oppPokemon.currentStats.get("hp")*48f) / game.battle.oppPokemon.maxStats.get("hp") );

        // System.out.println("numElements: "+String.valueOf(numElements)); // debug

        text = new Texture(Gdx.files.internal("battle/health1.png"));
        Sprite temp = new Sprite(text, 0,0,1,2);
        for (int i = 0; i < numElements; i++) {
            Sprite temp2 = new Sprite(temp); // to avoid long loading
            temp2.setPosition(32 + i, 123);
            this.healthBar.add(temp2);
        }

    }
    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        if (DrawEnemyHealth.shouldDraw) {
            // draw helper sprite
             // probly remove
            this.bgSprite.draw(game.uiBatch);

            // draw pkmn level bars
            int tensPlace = game.battle.oppPokemon.level/10;
            // System.out.println("level: "+String.valueOf(tensPlace));
            Sprite tensPlaceSprite = game.textDict.get(Character.forDigit(tensPlace, 10));
            game.uiBatch.draw(tensPlaceSprite, 40, 128);

            int offset = 0;
            if (game.battle.oppPokemon.level < 10) {
                offset = -8;
            }

            int onesPlace = game.battle.oppPokemon.level % 10;
            Sprite onesPlaceSprite = game.textDict.get(Character.forDigit(onesPlace, 10));
            game.uiBatch.draw(onesPlaceSprite, 48+offset, 128);

            char[] textArray = game.battle.oppPokemon.name.toUpperCase().toCharArray();
            Sprite letterSprite;
            for (int i=0; i < textArray.length; i++) {
                letterSprite = game.textDict.get(textArray[i]);
                game.uiBatch.draw(letterSprite, 8+8*i, 136);
            }

            // draw health bar
            for (Sprite bar : this.healthBar) {
                bar.draw(game.uiBatch);
            }
        }
        // detect when battle is over,
        // object will remove itself from AS
        if (game.battle.drawAction == null) {
            game.actionStack.remove(this);
        }
    }
}

// TODO: should either step into this in DrawBattle, or just
// move this code to DrawBattle.
// instance of this assigned to DrawBattle.drawEnemyHealthAction
class DrawFriendlyHealth extends Action {
    public static boolean shouldDraw = true;
    Sprite bgSprite;
    Sprite helperSprite;

    public ArrayList<Sprite> healthBar;
    public int layer = 129;

    boolean firstStep = true;
    public DrawFriendlyHealth(Game game) {
        this(game, null);
    }
    public DrawFriendlyHealth(Game game, Action nextAction) {
        this.nextAction = nextAction;

        Texture text = new Texture(Gdx.files.internal("battle/friendly_healthbar1.png"));
        this.bgSprite = new Sprite(text, 0, 0, 160, 144);

        // this.bgSprite.setPosition(0,4); ;// debug

//        text = new Texture(Gdx.files.internal("attack_menu/helper3.png"));
//        this.helperSprite = new Sprite(text, 0, 0, 160, 144);

        // fill sprite array according to enemy health
         // healthbar is 48 pixels long
         // round up when counting
        this.healthBar = new ArrayList<Sprite>();
        int numElements = (int)Math.ceil( (game.player.currPokemon.currentStats.get("hp")*48f) / game.player.currPokemon.maxStats.get("hp") );

        // System.out.println("numElements: "+String.valueOf(numElements)); // debug

        text = new Texture(Gdx.files.internal("battle/health1.png"));
        Sprite temp = new Sprite(text, 0,0,1,2);
        for (int i = 0; i < numElements; i++) {
            Sprite temp2 = new Sprite(temp); // to avoid long loading
            temp2.setPosition(96 + i, 67);
            this.healthBar.add(temp2);
        }
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

            // draw pkmn level bars
            int tensPlace = game.player.currPokemon.level/10;
            // System.out.println("level: "+String.valueOf(tensPlace));
            Sprite tensPlaceSprite = game.textDict.get(Character.forDigit(tensPlace,10));
            game.uiBatch.draw(tensPlaceSprite, 120, 72);

            int offset = 0;
            if (game.player.currPokemon.level < 10) {
                offset = -8;
            }

            int onesPlace = game.player.currPokemon.level % 10;
            Sprite onesPlaceSprite = game.textDict.get(Character.forDigit(onesPlace,10));
            game.uiBatch.draw(onesPlaceSprite, 128+offset, 72);

            char[] textArray = game.player.currPokemon.name.toUpperCase().toCharArray();
            Sprite letterSprite;
            offset = 0;
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

            // draw health bar
            for (Sprite bar : this.healthBar) {
                bar.draw(game.uiBatch);
            }
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
    ArrayList<ArrayList<Sprite>> spritesToDraw;
    int cursorDelay; // this is just extra detail. cursor has 2 frame delay before showing in R/B

    int cursorPos;
    int currIndex;
    ArrayList<String> itemsList;

    Sprite downArrow;
    int downArrowTimer;

    public DrawItemMenu(Game game, MenuAction prevMenu) {
        this.prevMenu = prevMenu;
        this.disabled = false;
        this.cursorDelay = 0;
        this.arrowCoords = new HashMap<Integer, Vector2>();
        this.spritesToDraw = new ArrayList<ArrayList<Sprite>>();
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

        // add 'cancel' to the items list
//        this.itemsList = new ArrayList<String>(game.player.itemsList); // TODO: delete
        this.itemsList = new ArrayList<String>(game.player.itemsDict.keySet());
        this.itemsList.add("Cancel");
        // convert player item list to sprites
        for (String entry : this.itemsList) {
            char[] textArray = entry.toUpperCase().toCharArray(); // iterate elements
            Sprite currSprite;
            int i = 0;
            ArrayList<Sprite> word = new ArrayList<Sprite>();
            for (char letter : textArray) {
                Sprite letterSprite = game.textDict.get((char)letter);
                if (letterSprite == null) {
                    letterSprite = game.textDict.get(null);
                }
                currSprite = new Sprite(letterSprite); // copy sprite from char-to-Sprite dictionary

                currSprite.setPosition(48+8*i, 104); // was *j
                word.add(currSprite);
                // go down a line if needed
                 // TODO - do this for words, not chars. split on space, array

                i++;
            }
            if (game.player.itemsDict.containsKey(entry)) {
                char[] numItemsArray = String.format("%02d", game.player.itemsDict.get(entry)).toCharArray();
                i = 10;
                for (char letter : numItemsArray) {
                    i += 1;
                    if (letter == '0') {
                        continue;
                    }
                    Sprite letterSprite = game.textDict.get((char)letter);
                    currSprite = new Sprite(letterSprite);
                    currSprite.setPosition(48+8*i, 104);
                    word.add(currSprite);
                }
            }
            spritesToDraw.add(word);
        }
    }
    public String getCamera() {return "gui";}

    // don't do this - idk why, but will occasionally overwrite prevMenu
//    MenuAction prevMenu;

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        if (this.prevMenu != null) {
            this.prevMenu.step(game);
        }

        // System.out.println("curr: " + curr);

        // draw text box
        this.textBox.draw(game.uiBatch);

        // debug
//        helperSprite.draw(game.floatingBatch);

        // draw the menu items
        int j = 0;
        for (int i = 0; i < this.spritesToDraw.size(); i++) {
            if (i >= currIndex && i < currIndex +4) { // only draw range of 4 starting at currIndex
                ArrayList<Sprite> word = this.spritesToDraw.get(i);
                for (Sprite sprite : word) {
                    // draw this string as text on the screen
                    game.uiBatch.draw(sprite, sprite.getX(), sprite.getY() - j*16);
                }
                j+=1;
            }
        }

        // return at this point if this menu is disabled
        if (this.disabled == true) {
            this.arrowWhite.setPosition(newPos.x, newPos.y);
            this.arrowWhite.draw(game.uiBatch);
            return;
        }
        // check user input
         //'tl' = top left, etc.
         // modify position by modifying curr to tl, tr, bl or br
        if (InputProcessor.upJustPressed) {
            if (cursorPos > 0) {
                cursorPos -= 1;
                newPos = arrowCoords.get(cursorPos);
            }
            else if (currIndex > 0) {
                currIndex -= 1;
            }

        }
        else if (InputProcessor.downJustPressed) {
            if (cursorPos < 2 && cursorPos+1 < this.itemsList.size()) {
                cursorPos += 1;
                newPos = arrowCoords.get(cursorPos);
            }
            else if (currIndex < this.itemsList.size() - 3) {
                currIndex += 1;
            }
        }

        // draw arrow
        if (this.cursorDelay >= 5) {
            this.arrow.setPosition(newPos.x, newPos.y);
            this.arrow.draw(game.uiBatch);
        }
        else {
            this.cursorDelay+=1;
        }

        // draw downarrow if applicable
        if ( (this.itemsList.size() - this.currIndex) > 4 ) {
            if (this.downArrowTimer < 22) {
                this.downArrow.draw(game.uiBatch);
            }
            this.downArrowTimer++;
        }
        else {
            this.downArrowTimer = 0; // force arrow to start over when scroll up
        }

        if (this.downArrowTimer > 41) {
            this.downArrowTimer = 0;
        }

        // button interaction is below drawing b/c I want to be able to return here
        // if press a, draw use/toss for item
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
//                System.out.println(String.valueOf(this.prevMenu));
                game.insertAction(new DrawUseTossMenu(game, this, name));
                game.actionStack.remove(this);
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
 * Draw player menu, ie pokedex, pokemon, items, etc. only appears in overworld, ie not a battle menu
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
        text = new Texture(Gdx.files.internal("attack_menu/menu3_smaller.png"));
        this.textBox = new Sprite(text, 0,0, 16*10, 16*9);

        this.arrowCoords.put(0, new Vector2(89, 72+32+16));
        this.arrowCoords.put(1, new Vector2(89, 72+16+16));

        // this.newPos =  new Vector2(32, 79); // post scaling change
        this.currIndex = DrawPlayerMenu.lastIndex;
        this.newPos = this.arrowCoords.get(this.currIndex); // new Vector2(89, 72+32+16);
        this.arrow.setPosition(newPos.x, newPos.y);

//        this.menuActions = new ArrayList<Action>();  // TODO: remove

        // populate sprites for entries in menu
        this.entries = new String[]{"POKÈMON", "ITEM"};
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
        this.textBox.draw(game.uiBatch);

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
            if (this.currIndex < 1) {
                this.currIndex += 1;
                newPos = arrowCoords.get(this.currIndex);
            }
        }

        // if press a, do attack
        if (InputProcessor.aJustPressed) { // using isKeyJustPressed rather than isKeyPressed

            game.insertAction(new PlaySound("click1", null));

            String currEntry = this.entries[this.currIndex];

            // we also need to create an 'action' that each of these items goes to
            if (currEntry.equals("POKÈMON")) {
                game.insertAction(new DrawPokemonMenu.Intro(
                                  new DrawPokemonMenu(game,
                                  this)));
            }
            else if (currEntry.equals("ITEM")) {
                game.insertAction(new DrawItemMenu.Intro(this, 9,
                                  new DrawItemMenu(game,
                                  this)));
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
            this.arrow.setPosition(newPos.x, newPos.y);
            this.arrow.draw(game.uiBatch);
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

            Texture text = new Texture(Gdx.files.internal("attack_menu/menu3_smaller.png"));

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
            this.sprite.draw(game.uiBatch);

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

// pokemon menu, used in battle and overworld
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
        this.bgSprite.draw(game.uiBatch);
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

            // draw status bar
            game.uiBatch.draw(this.healthBar, 0, 128 -16*i);

            // draw pkmn level text
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

            // draw pkmn max health text
            int maxHealth = currPokemon.maxStats.get("hp");
            int hundredsPlace = maxHealth/100;
            if (hundredsPlace > 0) {
                Sprite hudredsPlaceSprite = game.textDict.get(Character.forDigit(hundredsPlace,10));
                game.uiBatch.draw(hudredsPlaceSprite, 136, 128 -16*i);
            }
            tensPlace = (maxHealth % 100) / 10;
            if (tensPlace > 0 || hundredsPlace > 0) {
                tensPlaceSprite = game.textDict.get(Character.forDigit(tensPlace,10));
                game.uiBatch.draw(tensPlaceSprite, 136 +8, 128 -16*i);
            }
            onesPlace = maxHealth % 10;
            onesPlaceSprite = game.textDict.get(Character.forDigit(onesPlace,10));
            game.uiBatch.draw(onesPlaceSprite, 136 +16, 128 -16*i);

            // draw pkmn current health text
            int currHealth = currPokemon.currentStats.get("hp");
            hundredsPlace = currHealth/100;
            if (hundredsPlace > 0) {
                Sprite hudredsPlaceSprite = game.textDict.get(Character.forDigit(hundredsPlace,10));
                game.uiBatch.draw(hudredsPlaceSprite, 104, 128 -16*i);
            }
            tensPlace = (currHealth % 100) / 10;
            if (tensPlace > 0 || hundredsPlace > 0) {
                tensPlaceSprite = game.textDict.get(Character.forDigit(tensPlace,10));
                game.uiBatch.draw(tensPlaceSprite, 104 +8, 128 -16*i);
            }
            onesPlace = currHealth % 10;
            onesPlaceSprite = game.textDict.get(Character.forDigit(onesPlace,10));
            game.uiBatch.draw(onesPlaceSprite, 104 +16, 128 -16*i);

            // draw pokemon name
            char[] textArray = currPokemon.name.toUpperCase().toCharArray();
            Sprite letterSprite;
            for (int j=0; j < textArray.length; j++) {
                letterSprite = game.textDict.get(textArray[j]);
                game.uiBatch.draw(letterSprite, 24 +8*j, 136 -16*i);
            }

            // draw health bar
            int targetSize = (int)Math.ceil( (currPokemon.currentStats.get("hp")*48) / currPokemon.maxStats.get("hp"));
            for (int j=0; j < targetSize; j++) {
                game.uiBatch.draw(this.healthSprite, 48 +1*j, 131 -16*i);
            }
        }

        // draw 'Choose a pokemon' text
        if (DrawPokemonMenu.drawChoosePokemonText == true) {
            char[] textArray = "Choose a POKÈMON.".toCharArray();
            Sprite letterSprite;
            for (int j=0; j < textArray.length; j++) {
                letterSprite = game.textDict.get(textArray[j]);
                game.uiBatch.draw(letterSprite, 8 +8*j, 24);
            }
        }

        if (this.drawArrowWhite == true) {
            // draw white arrow
            this.arrowWhite.setPosition(this.newPos.x, this.newPos.y);
            this.arrowWhite.draw(game.uiBatch);
        }

        // return at this point if this menu is disabled
        if (this.disabled == true) {
            return;
        }

        // decrement avatar anim counter
        DrawPokemonMenu.avatarAnimCounter--;
        if (DrawPokemonMenu.avatarAnimCounter <= 0) {
            DrawPokemonMenu.avatarAnimCounter = 23;
        }

        // handle arrow input
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
        this.arrow.setPosition(this.newPos.x, this.newPos.y);
        this.arrow.draw(game.uiBatch);

        // button interaction is below drawing b/c I want to be able to return here
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
            // once player hits b in selected menu, avatar anim starts over
            Pokemon currPokemon = game.player.pokemon.get(DrawPokemonMenu.currIndex);
            game.insertAction(new DrawPokemonMenu.SelectedMenu(this, currPokemon));
            game.actionStack.remove(this);
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
//            if (word.equals("STATS")) {
//                return new SelectedMenu.Switch(prevMenu); // TODO - Stats menu
//            }
            if (word.equals("DROP")) {
                Vector2 pos = new Vector2(0,0);
                if (game.player.dirFacing.equals("right")) {
                    pos = game.player.position.cpy().add(16, 0);
                }
                else if (game.player.dirFacing.equals("left")) {
                    pos = game.player.position.cpy().add(-16, 0);
                }
                else if (game.player.dirFacing.equals("up")) {
                    pos = game.player.position.cpy().add(0, 16);
                }
                else if (game.player.dirFacing.equals("down")) {
                    pos = game.player.position.cpy().add(0, -16);
                }
                else {
                    pos = game.player.position.cpy().add(0, -16);
                }
                if (game.map.tiles.get(pos).attrs.get("solid") || game.map.tiles.get(pos).attrs.get("ledge")) {
                    this.disabled = true;
                    return new DisplayText(game, "CanÏ place here - something is in the way.", null, null,
                               new SetField(this.prevMenu, "disabled", false,
                               this.prevMenu));
                }
                if (game.player.pokemon.size() <= 1) {
                    this.disabled = true;
                    game.insertAction(this.prevMenu);
                    return new PlaySound("error1",
                           new DisplayText(game, "You need at least 1 POKÈMON in your party.", null, null,
                           new SetField(this.prevMenu, "disabled", false,
                           null)));
                }
                Pokemon pokemon = game.player.pokemon.get(DrawPokemonMenu.currIndex);
                pokemon.position = pos;
                pokemon.mapTiles = game.map.tiles;
                return new SelectedMenu.ExitAfterActions(this.prevMenu,
                       new PlaySound(pokemon,
                       pokemon.new RemoveFromInventory()));
            }
            else if (word.equals("SWITCH")) {
                if (game.battle.drawAction == null) {
                    return new SelectedMenu.Switch(prevMenu);
                }
                else if (game.player.pokemon.get(DrawPokemonMenu.currIndex).currentStats.get("hp") <= 0) {
                    this.disabled = true;
                    game.insertAction(this.prevMenu);
                    return new PlaySound("error1",
                           new SetField(this.prevMenu, "disabled", false, null));
                }
                else if (game.player.pokemon.get(DrawPokemonMenu.currIndex) == game.player.currPokemon) {
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
                game.player.isBuilding = true;  // tile to build appears in front of player
                game.player.isCutting = false;
                game.player.isHeadbutting = false;
                game.player.isJumping = false;
                return new SelectedMenu.ExitAfterActions(this.prevMenu,
                       new DisplayText(game, game.player.pokemon.get(DrawPokemonMenu.currIndex).name.toUpperCase()+" used BUILD! Press C and V to select tiles.", null, null,
                       null
                       ));
            }
            else if (word.equals("CUT")) {
                // TODO: only send when cutting tile
//                if (game.type == Game.Type.CLIENT) {
//                    game.client.sendTCP(new com.pkmngen.game.Network.UseHM(game.player.network.id,
//                                                                           DrawPokemonMenu.currIndex,
//                                                                           word));
//                }
                game.player.isCutting = true;
                game.player.isBuilding = false;
                game.player.isHeadbutting = false;
                game.player.isJumping = false;
                return new SelectedMenu.ExitAfterActions(this.prevMenu,
                       new DisplayText(game, game.player.pokemon.get(DrawPokemonMenu.currIndex).name.toUpperCase()+" is using CUT!", null, null,
                       null));
            }
            else if (word.equals("HEADBUTT")) {
                if (game.type == Game.Type.CLIENT) {
                    game.client.sendTCP(new com.pkmngen.game.Network.UseHM(game.player.network.id,
                                                                           DrawPokemonMenu.currIndex,
                                                                           word));
                }
                game.player.isHeadbutting = true;
                game.player.isBuilding = false;
                game.player.isCutting = false;
                game.player.isJumping = false;
                return new SelectedMenu.ExitAfterActions(this.prevMenu,
                       new DisplayText(game, game.player.pokemon.get(DrawPokemonMenu.currIndex).name.toUpperCase()+" is using HEADBUTT!", null, null,
                       null));
            }
            else if (word.equals("JUMP")) {
                if (game.type == Game.Type.CLIENT) {
                    game.client.sendTCP(new com.pkmngen.game.Network.UseHM(game.player.network.id,
                                                                           DrawPokemonMenu.currIndex,
                                                                           word));
                }
                game.player.isHeadbutting = false;
                game.player.isBuilding = false;
                game.player.isCutting = false;
                game.player.isJumping = true;
                return new SelectedMenu.ExitAfterActions(this.prevMenu,
                       new DisplayText(game, game.player.pokemon.get(DrawPokemonMenu.currIndex).name.toUpperCase()+" is using JUMP!", null, null,
                       null));
            }
            return null;
        }

        public String getCamera() {return "gui";}

        public int getLayer(){return this.layer;}

        @Override
        public void firstStep(Game game) {
            int numHms = -1;
            if (game.battle.drawAction == null) {
                numHms = pokemon.hms.size();
            }
            this.getCoords.put(0, new Vector2(97, 40 +16*numHms));
            this.getCoords.put(1, new Vector2(97, 40-16 +16*numHms));
            this.getCoords.put(2, new Vector2(97, 40-32 +16*numHms));

            if (game.battle.drawAction == null) {
                int i = 3;
                // Add HMs from selected pokemon
                for (String hm : this.pokemon.hms) {
                    this.words.add(hm);
                    this.getCoords.put(i, new Vector2(97, 40-32 -16*(i-3)));
                    i++;
                }
            }

//            this.words.add("STATS");
            this.words.add("SWITCH");
            if (game.battle.drawAction == null) {
                this.words.add("DROP");  // ideas: OPEN, FREE, DROP,
            }
            this.words.add("CANCEL");

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
            this.arrow.setPosition(newPos.x, newPos.y);
            this.arrow.draw(game.uiBatch);

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

                // draw arrow sprite
                this.arrow.setPosition(newPos.x, newPos.y);
                this.arrow.draw(game.uiBatch);

                if (this.disabled == true) {
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
                    char[] textArray = "Move POKÈMON".toCharArray();
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
        // draw text box
        this.textBox.draw(game.uiBatch);

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
            this.arrow.setPosition(newPos.x, newPos.y);
            this.arrow.draw(game.uiBatch);
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
            if (itemName.equals("sleeping bag")) {
                // TODO: 'cant use this' text while in battle.
                if (game.map.tiles.get(game.player.position.cpy().add(16,0)).attrs.get("solid") ||
                    game.map.tiles.get(game.player.position.cpy().add(16,0)).attrs.get("ledge")) {
                    this.disabled = true;
                    game.actionStack.remove(this);
                    game.insertAction(new DisplayText(game, "Not enough room!", null, false, true,
                                      new SetField(this.prevMenu, "disabled", false,
                                      this.prevMenu)));
                    return;
                }
                if (game.map.tiles.get(game.player.position.cpy().add(0,0)).attrs.get("grass")) {
                    this.disabled = true;
                    game.actionStack.remove(this);
                    // CanÏ use this while in tall grass!
                    game.insertAction(new DisplayText(game, "CanÏ use this while PokÈmon are nearby!", null, false, true,
                                      new SetField(this.prevMenu, "disabled", false,
                                      this.prevMenu)));
                    return;
                }
                if (game.type == Game.Type.CLIENT) {
                    game.client.sendTCP(new com.pkmngen.game.Network.Sleep(game.player.network.id, true));
                }
                // Save this spot as next place to go to if player blacks out
                // TODO: should require a house for this (probably)
                game.player.spawnLoc.set(game.player.position);
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
            if (itemName.contains("apricorn")) {
                // Perform this action on the tile in from of the player.
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
                if (!game.map.tiles.get(pos).name.equals("green1")) {
                    this.disabled = true;
                    game.actionStack.remove(this);
                    game.insertAction(new DisplayText(game, "Seeds must be planted in good soil!", null, false, true,  //CanÏ plant here!
                                      new SetField(this.prevMenu, "disabled", false,
                                      this.prevMenu)));
                    return;
                }
                game.playerCanMove = false;
                game.insertAction(new WaitFrames(game, 10,
                                  new SplitAction(new PlantTree(pos, null),
                                  new PlaySound("seed1", //new SplitAction(null),
                                  new WaitFrames(game, 4,
                                  new PlaySound("ledge2",
                                  new WaitFrames(game, 10,
                                  new SetField(game, "playerCanMove", true,
                                  null))))))));
                // Deduct from inventory
                game.player.itemsDict.put(itemName, game.player.itemsDict.get(itemName)-1);
                if (game.player.itemsDict.get(itemName) <= 0) {
                    game.player.itemsDict.remove(itemName);
                }
                // Tell server.
                if (game.type == Game.Type.CLIENT) {
                    game.client.sendTCP(new Network.UseItem(game.player.network.id, itemName,
                                                            game.player.dirFacing));
                }
                return;
            }
            this.prevMenu.disabled = false;
            game.insertAction(this.prevMenu);
            return;
        }
        if (itemName.contains("ball") && game.player.pokemon.size() >= 6) {
            game.insertAction(this.prevMenu);
            game.insertAction(new DisplayText(game, "Not enough room in your party!", null, false, true,
                              new SetField(this.prevMenu, "disabled", false,
                              null)));
            return;
        }
        // if this item can't be used in battle, player error noise.
        if (!itemName.contains("ball") && !itemName.contains("berry")) {  // TODO: more items
            game.insertAction(this.prevMenu);
            game.insertAction(new PlaySound("error1",
                              new SetField(this.prevMenu, "disabled", false,
                              null)));
            return;
        }

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
        // deduct item from inventory
        game.player.itemsDict.put(itemName, game.player.itemsDict.get(itemName)-1);
        if (game.player.itemsDict.get(itemName) <= 0) {
            game.player.itemsDict.remove(itemName);
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
        this.textBox.draw(game.uiBatch);

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
            this.arrow.draw(game.uiBatch);
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
        if (!game.map.timeOfDay.equals("Night")) {
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

            if (this.playSound.get(0) == true && !SpecialMewtwo1.class.isInstance(game.battle.oppPokemon)) {
                // play victory fanfare
                game.currMusic.pause();
                game.currMusic = game.battle.victoryFanfare;
                game.currMusic.stop();
                game.currMusic.play();
            }

            positions.remove(0);
            repeats.remove(0);
            playSound.remove(0);
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
    /*
     * Source: https:// stackoverflow.com/questions/17516177/texture-grayscale-in-libgdx
     */
    static String vertexShader = "attribute vec4 a_position;\n" +
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
    static String fragmentShader = "#ifdef GL_ES\n" +
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
                                  new PlayerCanMove(game,
                                  null)));
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
//        if (!game.map.timeOfDay.equals("Night")) {
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

            this.firstStep = false;
        }
        // if done with anim, do nextAction
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

/*
 * TODO: not doing 'blue bar' animation for now, do at some point.
 */
class GainExpAnimation extends Action {
    boolean gainedLevel = false;

    public GainExpAnimation(Action nextAction) {
        this.nextAction = nextAction;
    }

    @Override
    public void step(Game game) {
        if (game.player.currPokemon.level < 100 && game.player.currPokemon.gen2CalcExpForLevel(game.player.currPokemon.level+1) <= game.player.currPokemon.exp) {
            // TODO: debug, remove
//            System.out.println("Curr exp: " + String.valueOf(game.player.currPokemon.exp));
//            System.out.println("Needed for next level: " + String.valueOf(game.player.currPokemon.gen2CalcExpForLevel(game.player.currPokemon.level+1)));
            game.player.currPokemon.level += 1;
            game.actionStack.remove(this);
            Action action = new DisplayText.Clear(game,
                            new WaitFrames(game, 3,
                            new DisplayText(game, game.player.currPokemon.name.toUpperCase() + " grew to level " + game.player.currPokemon.level+"!",
                                            "fanfare1.ogg", true, true,
                            null)));
            // Check if any moves learned
            if (game.player.currPokemon.learnSet.containsKey(game.player.currPokemon.level)) {
                for (String attack : game.player.currPokemon.learnSet.get(game.player.currPokemon.level)) {
                    boolean learned = false;
                    for (int i = 0; i < 4; i++) {
                        if (game.player.currPokemon.attacks[i] == null) {
                            action.append(new DisplayText.Clear(game,
                                          new WaitFrames(game, 3,
                                          new DisplayText(game, game.player.currPokemon.name.toUpperCase() + " learned " + attack.toUpperCase()+"!",
                                                          "fanfare1.ogg", true, true,
                                          null))));
                            game.player.currPokemon.attacks[i] = attack;
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
                                      new DisplayText(game, game.player.currPokemon.name.toUpperCase() + " is trying to learn " + attack.toUpperCase()+".",
                                                      null, null,
                                      new DisplayText(game, "Which move should be forgotten?",
                                                      null, true, false,
                                      new DrawAttacksMenu(attack,
                                      null))))));
                    }
                }
            }
            action.append(this);
            game.insertAction(action);
            this.gainedLevel = true;
            return;
        }

        // Check if the Pokemon should evolve
        if (this.gainedLevel) {
            for (int i=1; i <= game.player.currPokemon.level; i++) {
                if (Pokemon.gen2Evos.get(game.player.currPokemon.name.toLowerCase()).containsKey(String.valueOf(i))) {
                    String evolveTo = Pokemon.gen2Evos.get(game.player.currPokemon.name.toLowerCase()).get(String.valueOf(i));
                    this.nextAction = new WaitFrames(game, 61,
                                    new WaitFrames(game, 3,  // NOTE: this is in case I add 3 frame delay to DisplayText, in that case
                                                             // remove this line.
                                    new DisplayText(game, "What? "+game.player.currPokemon.name.toUpperCase()+" is evolving!",
                                                    null, true, false,
                                    new WaitFrames(game, 51,
                                    new EvolutionAnim(game.player.currPokemon, evolveTo,
                                    new PlaySound(game.player.currPokemon,
                                    new SplitAction(
                                        new EvolutionAnim.StartMusic(),
                                    new Battle.LoadAndPlayAnimation(game, "evolve", null,
                                    new WaitFrames(game, 30,  // about 30 frames after bubble anim until pokemon cry is heard
                                    new PlaySound(new Pokemon(evolveTo.toLowerCase(), 10, Pokemon.Generation.CRYSTAL),
                                    new DisplayText.Clear(game,
                                    new WaitFrames(game, 3,
                                    new DisplayText(game, "Congratulations! Your "+game.player.currPokemon.name.toUpperCase(),
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

        // for now, if displayTextAction not in AS, remove and return
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
    Sprite originalSprite;

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
             // the PokÈmon's catch rate will also be reset to its initial catch rate,
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
                                 new SplitAction(
                                         new DrawFriendlyHealth(game),
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
      if (this.firstStep == true) {
          game.battle.drawAction.drawFriendlyPokemonAction = this;
          this.firstStep = false;
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
