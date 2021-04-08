package com.pkmngen.game;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import com.pkmngen.game.util.Direction;
import com.pkmngen.game.util.LinkedMusic;

// TODO: enums
// layers
// 1-100: reserved
// 101-110: sky
// 111-115: player(upper)
// 116-120: grass
// 121-130: player(lower)
// 131-140: tiles

// ui layers
// 1-100: reserved
// 101-110: text
// 111-120: attacks
// 121-130: pkmn/trainers
// 131-140: backgrounds

/**
 * Insert Actions into game.actionStack and their step() function will get
 * called once per frame. The Action calls game.actionStack.remove(this) once
 * it's completed. Actions will usually insert this.nextAction into
 * game.actionStack once they are done (to move to the next Action).
 *
 * Convention for chaining Actions:
 * Action newAction = new Action1(game, ...,
 *                    new Action2(game, ...,
 *                    ...
 *                    null));
 * game.insertAction(newAction);
 */
public class Action {
    // Keep reference to Game.staticGame to prevent having to pass `Game game`
    // to init functions and step function.
    // Game game = Game.staticGame;
    Action nextAction = null;
    boolean firstStep = true;

    Object[] params;  // unused for now

    public Action(Object... parameters) {
//        for (Object param : parameters) {
//            System.out.println(param);
//        }
        this.params = parameters;
    }

    /**
     * Append an Action to the end of this chain of Actions.
     * NOTE: if you declare a duplicate nextAction in a child class, it messes
     * this up. So don't I guess.
     * @param action to append to chain of actions
     */
    public void append(Action action) {
        Action currAction = this;
        while (currAction.nextAction != null) {
            currAction = currAction.nextAction;
        }
        currAction.nextAction = action;
    }

    /**
     * Useful to override if you want to do something the first time this
     * action is step'd (like initialize vars).
     */
    public void firstStep(Game game){}

    // TODO: refactor to this.batch
    public String getCamera() {
        return "map";
    }

    // TODO: refactor to this.layer
    public int getLayer() {
        return 0;
    }

    /**
     * Define what do to at each step (this is called once per frame).
     */
    public void step(Game game) {
        game.actionStack.remove(this);
        game.insertAction(this.nextAction);
        if (this.nextAction != null) {
            this.nextAction.step(game);
        }
    }
    
    // Idea: print self name if debug enabled when firststep called.
}

/**
 * Draw text on screen (Gen 1/2 style).
 * TODO: Right now you have to insert spaces to do newline.
 */
class DisplayText extends Action {
    public static boolean textPersist = false;
    ArrayList<Sprite> spritesNotDrawn;
    ArrayList<Sprite> spritesBeingDrawn;

    Sprite arrowSprite;
    Action playSoundAction;
    boolean playSound;

    Action scrollUpAction; // keeps track of if text is currently scrolling up
    public int layer = 106;

    Sprite helperSprite;

    Sprite bgSprite;
    int timer;
    int speedTimer;

    int speed;
    Action triggerAction;

    boolean foundTrigger;
    boolean checkTrigger;
    boolean persist = false;  // if true, don't clear text after text is finished
    boolean waitInput = true;  // if true, wait for user to press A after text is complete
    public boolean braillify = false;  // used for regigigas text
    boolean firstStep;
    public static boolean unownText = false;
    Texture unownTiles1;
    Texture unownTiles2;
    Texture unownTiles3;

    public DisplayText(Game game, String textString, String playSound, Action triggerAction, Action nextAction) {
        this(game, textString, playSound, triggerAction, false, nextAction);
    }

    public DisplayText(Game game, String textString, String playSound, Action triggerAction, boolean braillify, Action nextAction) {
        this.nextAction = nextAction;
        this.firstStep = true;
        // Set end trigger action
        this.triggerAction = triggerAction;
        this.foundTrigger = false;
        this.checkTrigger = false;
        this.braillify = braillify;
        this.spritesNotDrawn = new ArrayList<Sprite>();
        this.spritesBeingDrawn = new ArrayList<Sprite>();

        if (playSound != null) {
            this.playSoundAction = new DisplayText.PlaySoundText(playSound, null);
            this.playSound = true;
        }
        else {
            this.playSound = false;
        }

        this.speed = 2; this.speedTimer = this.speed;

        // here we make sure each line wraps by word, not by char
         // could be better, but works
        String line = "";
        String lines = "";
        String[] words = textString.split(" ");
        for (String word : words) {
            if (line.length() + word.length() < 19) {
                line += word;
                if (line.length() != 18) { // possible bug // don't add space to end of max length line
                    line += " ";
                }
            }
            else {
                while (line.length() < 18) {
                    line += " ";
                }
                lines += line;
                if (this.braillify) {
                    lines+=line;  // lines are repeated
                }
                line = word + " ";
            }
        }
        if (this.braillify) {
            while (line.length() < 18) {
                line += " ";
            }
            lines+=line;
            lines+=line;  // lines are repeated
        }
        else {
            lines+=line;
        }
        char[] textArray = lines.toCharArray(); // iterate elements
        this.unownTiles1 = new Texture(Gdx.files.internal("unownTiles1.png"));
        this.unownTiles2 = new Texture(Gdx.files.internal("unownTiles2.png"));
        this.unownTiles3 = new Texture(Gdx.files.internal("unown_font2.png"));

        int i = 0;
        int j = 0;  ///, offsetNext = 0; // offsetNext if char sizes are ever different. atm it works.
        Sprite currSprite;
        Sprite letterSprite;
//        boolean doBraille = true;
        for (char letter : textArray) {
            if (!DisplayText.unownText) {
                letterSprite = game.textDict.get((char)letter);
                if (letterSprite == null) {
                    letterSprite = game.textDict.get(null);
                }
            }
            else {
                int randX = game.map.rand.nextInt(128/8);
                int randY;
                int rand = game.map.rand.nextInt(3);
                if (letter == ' ') {
                    letterSprite = game.textDict.get(' ');
                }
                else if (rand == 0) {
                    randY = game.map.rand.nextInt(80/8) + 48/8;
                    letterSprite = new Sprite(this.unownTiles1, randX*8, randY*8, 8, 8);
                }
                else if (rand == 1) {
                    if (game.map.rand.nextInt(2) == 0) {
                        randY = game.map.rand.nextInt(24/8) + 64/8;
                    }
                    else {
                        randY = game.map.rand.nextInt(16/8) + 112/8;
                    }
                    letterSprite = new Sprite(this.unownTiles2, randX*8, randY*8, 8, 8);
                }
                else {
                    randX = game.map.rand.nextInt(26);
                    letterSprite = new Sprite(this.unownTiles3, randX*8, 0, 8, 9);
                }
            }
            currSprite = new Sprite(letterSprite);  // Copy sprite from char-to-Sprite dictionary
            
            // If braille effect (regi dialogue), then every other line is braille
            if (this.braillify && j % 2 == 0) {
                letterSprite = game.brailleDict.get((char)letter);
                if (letterSprite == null) {
                    letterSprite = game.textDict.get(' ');
                }
                currSprite = new Sprite(letterSprite);
            }

            // currSprite.setPosition(10*3+8*i*3 +2, 26*3-16*j*3 +2); // offset x=8, y=25, spacing x=8, y=8(?)
            currSprite.setPosition(10+8*i +2-4, 26-16*j +2-4);
            spritesNotDrawn.add(currSprite);
            // Go down a line if needed
            // TODO - do this for words, not chars. split on space, array
            if (i >= 17) {  // TODO: test
                i = 0; j++;
            }
            else {
                i++;
            }
        }
        Texture text = new Texture(Gdx.files.internal("arrow_down.png"));
        this.arrowSprite = new Sprite(text, 0, 0, 7, 5);
        this.arrowSprite.setPosition(147-2-1,12-1-1);

//        text = new Texture(Gdx.files.internal("text_helper1.png")); // battle_bg1
        // text = new Texture(Gdx.files.internal("battle/battle_bg1.png"));
        // Texture text = new Texture(Gdx.files.internal("throw_rock_anim/helper12.png"));
//        this.helperSprite = new Sprite(text, 0, 0, 160, 144);
        // this.helperSprite.setPosition(16*10,16*9);
        // this.helperSprite.setScale(3);
        this.timer = 0;

        text = new Texture(Gdx.files.internal("textbox_bg1.png"));
        this.bgSprite = new Sprite(text, 0, 0, 160, 144);
    }

    public DisplayText(Game game,
                       String textString,
                       String playSound,
                       boolean textPersist,
                       Action nextAction) {
        this(game, textString, playSound, textPersist, true, nextAction);
    }

    // TODO: migrate to using this only
    public DisplayText(Game game,
                       String textString,
                       String playSound,
                       boolean textPersist,
                       boolean waitInput,
                       boolean braillify,
                       Action nextAction) {
        this(game, textString, playSound, null, braillify, nextAction);
        this.persist = textPersist;
        this.waitInput = waitInput;
    }

    // TODO: migrate to using this only
    public DisplayText(Game game,
                       String textString,
                       String playSound,
                       boolean textPersist,
                       boolean waitInput,
                       Action nextAction) {
        this(game, textString, playSound, textPersist, waitInput, false, nextAction);
    }

    public String getCamera() {return "gui";}

    public int getLayer() {return this.layer;}

    // what to do at each iteration
    public void step(Game game) {
        if (this.firstStep) {
            // if you ever just pass 'null' to triggerAction, and
             // then remove game.displayTextAction from actionStack later,
             // text will stop displaying
             // needed when enemy pkmn faints (displayText doesn't wait for user input)
            game.displayTextAction = this;
            this.firstStep = false;
            if (this.persist) {
                DisplayText.textPersist = true;
            }
        }

        // debug
        // this.helperSprite.draw(game.floatingBatch);
        this.bgSprite.draw(game.uiBatch);

        // debug // flash on and off
//        this.timer--;
//        if (this.timer <= 0){
//            if (this.timer <= -19){
//                this.timer = 20;
//            }
//            return;
//        }

        // note - draws 0 letters on first step

        // draw all drawable
        for (Sprite sprite : spritesBeingDrawn) {
            // Happens with regigigas braille text that scrolls up 
            // really high each time it scrolls.
            if (sprite.getY() > 46-8) {
                continue;
            }
            // Sprites will appear below screen when scrolling,
            // don't draw in this case.
            if (sprite.getY() < 6) {
                continue;
            }
            sprite.draw(game.uiBatch);
        }

        // don't do anything if scrolling text up
        if (game.actionStack.contains(this.scrollUpAction)) {
            return;
        }

        // TODO - don't do anything if waiting for user to press A

        // don't do anything if playing sound (example fanfare, etc)
        if (game.actionStack.contains(this.playSoundAction)) {
            return;
        }

        // TODO: deprecate this
        // don't do anything if trigger action is in actionStack
        if (this.checkTrigger == true || (this.persist && !DisplayText.textPersist)) {
            if (this.triggerAction == null && !DisplayText.textPersist) {
                game.actionStack.remove(this);
                return;
            }
            // TODO: remove the below two blocks once migrated to using textPersist
            if (game.actionStack.contains(this.triggerAction)) {
                this.foundTrigger = true;
                return;
            }
            // once the trigger is found once, exit after that trigger is finished
            if (this.foundTrigger == true) {
                game.actionStack.remove(this);
                return;
            }
            return;
        }

        // debug
//        if (spritesBeingDrawn.size() == 30) {
//            return;
//        }

        //

        // if no sprites left in spritesNotDrawn, wait for player to hit A
//        int max = 37;
//        if (this.braillify) {
//            max *= 2;
//        }
        if (spritesBeingDrawn.size() >= 37 || spritesNotDrawn.isEmpty()) { // 18 characters per line allowed
            // If at the end of text and need to play sound, do that
            if (this.playSound && spritesNotDrawn.isEmpty()) {
                game.insertAction(this.playSoundAction);
                this.playSoundAction.step(game); // avoid latency
                this.playSound = false;
                return;
            }

            // if we need to wait on a trigger
            if (this.triggerAction != null || (spritesNotDrawn.isEmpty() &&
                                               DisplayText.textPersist &&
                                               !this.waitInput)) {
                game.insertAction(this.nextAction);
                this.checkTrigger = true;
                return;
            }

            // Draw arrow
             // flash on and off
            if (this.timer <= 0){
                if (this.timer <= -35){
                    this.timer = 33;
                }
                else {
                    this.arrowSprite.draw(game.uiBatch);
                }
            }
            this.timer--;

            if (InputProcessor.aJustPressed) {
                Action playSound = new PlaySound("click1", null);
                game.insertAction(playSound);
                playSound.step(game); // prevent latency

                if (spritesNotDrawn.isEmpty()) {
                    if (DisplayText.textPersist) {
                        game.insertAction(this.nextAction);
                        this.checkTrigger = true;
                        return;
                    }
                    game.insertAction(this.nextAction);
                    game.actionStack.remove(this);
                }
                else {
                    this.scrollUpAction = new DisplayText.ScrollTextUp(game, this.spritesBeingDrawn, this.spritesNotDrawn, this.braillify);
                    game.insertAction(this.scrollUpAction);
                }
            }
            return;
        }

        // only extract sprite every 'speed' number of frames
        if (this.speedTimer > 0) {
            this.speedTimer--;
            return;
        }
        else {
            this.speedTimer = this.speed;
        }

        // get next sprite, remove from spritesNotDrawn
        // text speed - if pressing A or B, add 3 sprites instead of 1
        if (InputProcessor.aPressed || InputProcessor.bPressed) {
            // if would take too many, stop
            for (int i=0; i < 3 && !spritesNotDrawn.isEmpty(); i++) {
                spritesBeingDrawn.add(spritesNotDrawn.remove(0));
                if (this.braillify && !spritesNotDrawn.isEmpty() && 35-spritesBeingDrawn.size() >= 0) {
                    spritesBeingDrawn.add(spritesNotDrawn.remove(35-spritesBeingDrawn.size()));
                }
            }
        }
        else {
            spritesBeingDrawn.add(spritesNotDrawn.remove(0));
            if (this.braillify && !spritesNotDrawn.isEmpty() && 35-spritesBeingDrawn.size() >= 0) {
                spritesBeingDrawn.add(spritesNotDrawn.remove(35-spritesBeingDrawn.size()));
            }
        }

    }

    /**
     * Clear any text persisting on screen.
     * TODO: refactor to always insert WaitFrames(3) before nextAction.
     */
    static class Clear extends Action {
        public Clear(Game game, Action nextAction) {
            this.nextAction = nextAction;
        }

        @Override
        public void step(Game game) {
            DisplayText.textPersist = false;
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
        }
    }

    // play a sound, ie victory fanfare
     // unique b/c need to mute battle music
    class PlaySoundText extends Action {
        Music music;
        float initialVolume;  // Different tracks have diff volume
        boolean playedYet;  // Do music.play on first step

        public PlaySoundText(String sound, Action nextAction) {
            this.nextAction = nextAction;
            this.playedYet = false;
            if (sound.equals("fanfare1")) {
                this.music = Gdx.audio.newMusic(Gdx.files.internal("catch_fanfare.mp3")); // use this
                this.music.setLooping(false);
            }
            else if (sound == "Raikou") {
                this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/243Cry.ogg")); // use this
                this.music.setLooping(false);
            }
            else if (sound == "Entei") {
                this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/244Cry.ogg")); // use this
                this.music.setLooping(false);
            }
            else if (sound == "Suicune") {
                this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/245Cry.ogg"));
                this.music.setLooping(false);
            }
            else {
                this.music = Gdx.audio.newMusic(Gdx.files.internal(sound)); // use this
                this.music.setLooping(false);
                if (sound.contains("cries")) {
                    this.music.setVolume(0.5f);
                }
            }
        }

        @Override
        public void step(Game game) {
            // play the sound
            if (this.music != null && !this.playedYet) {
                // game.battle.music.setVolume(0f); // TODO - use this?
                this.initialVolume = game.currMusic.getVolume();
                game.currMusic.setVolume(0f);
                this.music.play();
                this.playedYet = true;
            }

            if (!this.music.isPlaying()) {
                // game.battle.music.setVolume(0.3f);
                game.currMusic.setVolume(this.initialVolume);
                game.actionStack.remove(this);
                game.insertAction(this.nextAction);
            }
        }
    }

    // Move text up in text box
    // Works b/c DisplayText will begin drawing new chars once SpritesBeingDrawn is small enough
    class ScrollTextUp extends Action {
        ArrayList<Vector2> positions;
        Vector2 position;

        ArrayList<Sprite> text;
        ArrayList<Sprite> otherText;
        boolean braillify = false;
        int numRemove = 18;

        public int layer = 110;

        public ScrollTextUp(Game game, ArrayList<Sprite> text, ArrayList<Sprite> otherText) {
            this(game, text, otherText, false);
        }

        public ScrollTextUp(Game game, ArrayList<Sprite> text, ArrayList<Sprite> otherText, boolean braillify) {
            this.text = text;
            this.otherText = otherText;
            this.braillify = braillify;

            this.positions = new ArrayList<Vector2>();
            for (int i=0; i < 5; i++) {
                this.positions.add(new Vector2(0,0));
            }
            this.positions.add(new Vector2(0,8));
            for (int i=0; i < 5; i++) {
                this.positions.add(new Vector2(0,0));
            }
            this.positions.add(new Vector2(0,8));
            
            if (this.braillify) {
                this.numRemove = 36;
            }
        }

        public String getCamera() {return "gui";}

        public int getLayer() {return this.layer;}

        // what to do at each iteration
        public void step(Game game) {
            this.position = this.positions.remove(0);

            for (Sprite sprite : this.text) {
                sprite.setPosition(sprite.getX()+this.position.x, sprite.getY()+this.position.y);
            }
            for (Sprite sprite : this.otherText) {
                sprite.setPosition(sprite.getX()+this.position.x, sprite.getY()+this.position.y);
            }

            // if done, remove first 18 elements
            // frees up DisplayText's text array, which will get filled with new sprites
            if (this.positions.isEmpty()) {
                if (this.braillify) {
                    this.braillify = false;
                    for (int i=0; i < 5; i++) {
                        this.positions.add(new Vector2(0,0));
                    }
                    this.positions.add(new Vector2(0,8));
                    for (int i=0; i < 5; i++) {
                        this.positions.add(new Vector2(0,0));
                    }
                    this.positions.add(new Vector2(0,8));
                    return;
                }
                for (int i=0; i<this.numRemove; i++) {
                    this.text.remove(0);
                }
                game.actionStack.remove(this);
                return;
            }
        }
    }
}

/**
 * Scrolls text at a fixed speed and doesn't require any user input to proceed.
 * (Used in special mewtwo battle intro).
 */
class DisplayTextIntro extends Action {
    ArrayList<Sprite> spritesNotDrawn;
    ArrayList<Sprite> spritesBeingDrawn;

    Sprite arrowSprite; // TODO - remove this
    Sprite arrowSprite2;

    Action playSoundAction;
    boolean playSound;
    Action scrollUpAction; // keeps track of if text is currently scrolling up

    int charsPerLine = 32; // number of characters allowed in one line
    int spacing = 6; // how far apart to space characters

    public int layer = 110;
    Sprite helperSprite;
    Sprite bgSprite;

    int timer;
    int speedTimer;
    int speed;

    // when we need to stop after trigger action
    Action triggerAction;
    boolean foundTrigger;

    boolean checkTrigger;
    Vector3 touchLoc = new Vector3();
    Vector2 touchLoc2d = new Vector2();

    boolean firstStep = true;
    boolean exitWhenDone = true;
    boolean waitingOnExit = false;

    public DisplayTextIntro(Game game, String textString, String playSound, Action triggerAction, boolean exitWhenDone, Action nextAction) {
        this.nextAction = nextAction;

        this.exitWhenDone = exitWhenDone;

        // TODO - need separate triggerAction and clickComplete modes
         // when both are passed, clicks complete but still waits on triggerAction

        // set end trigger action
        this.triggerAction = triggerAction;
        this.foundTrigger = false;
        this.checkTrigger = false;

        this.spritesNotDrawn = new ArrayList<Sprite>();
        this.spritesBeingDrawn = new ArrayList<Sprite>();

        if (playSound != null) {
            this.playSoundAction = new DisplayTextIntro.PlaySound_Text(playSound, null);
            this.playSound = true;
        }
        else {
            this.playSound = false;
        }

        this.speed = 3; //= 2;
        this.speedTimer = this.speed;

        // 26 chars per line

        // here we make sure each line wraps by word, not by char
         // could be better, but works
        String line = "";
        String lines = "";
        String[] words = textString.split(" ");
        for (String word : words) {
            if (line.length() + word.length() < 18) {
                line += word;
                if (line.length() != 17) { // possible bug // don't add space to end of max length line
                    line += " ";
                }
            }
            else {
                while (line.length() < 18) {
                    line += " ";
                }
                lines += line;
                line = word + " ";
            }
        }
        lines+=line;

        char[] textArray = lines.toCharArray(); // iterate elements

        int i = 0;
        int j = 0;  ///, offsetNext = 0; // offsetNext if char sizes are ever different. atm it works.
        Sprite currSprite;
        for (char letter : textArray) {
            // offsetNext += spriteWidth*3+2 // how to do this?
            Sprite letterSprite = game.textDict.get((char)letter);
            // System.out.println(String.valueOf(letter));
            if (letterSprite == null) {
                letterSprite = game.textDict.get(null);
            }
            currSprite = new Sprite(letterSprite); // copy sprite from char-to-Sprite dictionary

            // currSprite.setPosition(10*3+8*i*3 +2, 26*3-16*j*3 +2); // offset x=8, y=25, spacing x=8, y=8(?)
            currSprite.setPosition(10+8*i +2-4, 26-16*j +2-4); // post scaling change
//            currSprite.setPosition(((game.cam.viewportWidth*game.cam.zoom)-144)/2 +10+5*i +2-4, 26-16*j +2-4 -1); // new font offset of 6
            // currSprite.setScale(3); // post scaling change

            spritesNotDrawn.add(currSprite);
            // go down a line if needed
             // TODO - do this for words, not chars. split on space, array
            if (i >= 17) {
                i = 0; j++;
            }
            else {
                i++;
            }
        }

        // why not just every frame draw a new sprite, pop off the sprites list?
         // when list is empty, display the arrow and wait for user input

        Texture text = new Texture(Gdx.files.internal("arrow_down.png"));
        this.arrowSprite = new Sprite(text, 0, 0, 7, 5);
        // this.arrowSprite.setPosition(147*3-2,12*3-1);
        this.arrowSprite.setPosition(147-2-1,12-1-1); // post scaling change
        // this.arrowSprite.setScale(3); // post scaling change

//        text = new Texture(Gdx.files.internal("text_helper1.png")); // battle_bg1
        // text = new Texture(Gdx.files.internal("battle/battle_bg1.png"));
        // Texture text = new Texture(Gdx.files.internal("throw_rock_anim/helper12.png"));
//        this.helperSprite = new Sprite(text, 0, 0, 160, 144);
        // this.helperSprite.setPosition(16*10,16*9);
        // this.helperSprite.setScale(3);
        this.timer = 0;

        text = new Texture(Gdx.files.internal("textbox_bg1.png")); // textbox bg1
        this.bgSprite = new Sprite(text, 0, 0, 160, 144);
    }
    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    // what to do at each iteration
    public void step(Game game) {
        if (this.firstStep == true) {
            // if you ever just pass 'null' to triggerAction, and
             // then remove game.displayTextAction from actionStack later,
             // text will stop displaying
             // needed when enemy pkmn faints (displayText doesn't wait for user input)
            game.displayTextAction = this;
            this.firstStep = false;
        }

        // debug
        // this.helperSprite.draw(game.floatingBatch);
        this.bgSprite.draw(game.uiBatch);

        // debug // flash on and off
//        this.timer--;
//        if (this.timer <= 0){
//            if (this.timer <= -19){
//                this.timer = 20;
//            }
//            return;
//        }

        // note - draws 0 letters on first step

        // draw all drawable
        for (Sprite sprite : spritesBeingDrawn) {
            sprite.draw(game.uiBatch);
        }

        // don't do if waiting for something else to exit this
        if (this.waitingOnExit == true) {
            return;
        }

        // don't do anything if scrolling text up
        if (game.actionStack.contains(this.scrollUpAction)) {
            return;
        }

        // TODO - don't do anything if waiting for user to press A

        // don't do anything if playing sound (example fanfare, etc)
        if (game.actionStack.contains(this.playSoundAction)) {
            return;
        }

        // don't do anything if trigger action is in actionStack
        if (this.checkTrigger) {
            if (game.actionStack.contains(this.triggerAction)) {
                this.foundTrigger = true;
                return;
            }
            // once the trigger is found once, exit after that trigger is finished
            if (this.foundTrigger == true) {
                game.actionStack.remove(this);
                return;
            }
            return;
        }

        // debug
//        if (spritesBeingDrawn.size() == 30) {
//            return;
//        }

        //

        // if no sprites left in spritesNotDrawn, wait for player to hit A
        if (spritesBeingDrawn.size() >= 36 || spritesNotDrawn.isEmpty()) { // 24 characters per line allowed

            // if at the end of text and need to play sound, do that
            if (this.playSound == true && spritesNotDrawn.isEmpty()) {
                game.insertAction(this.playSoundAction);
                this.playSoundAction.step(game); // avoid latency
                this.playSound = false;
                return;
            }

            // if we need to wait on a trigger
            if (this.triggerAction != null) {
                game.insertAction(this.nextAction);
                this.checkTrigger = true;
                return;
            }

            // draw arrow
             // flash on and off
//            if (this.timer <= 0){
//                if (this.timer <= -35){
//                    this.timer = 33;
//                }
//                else {
//                    this.arrowSprite2.draw(game.floatingBatch);
//                }
//            }
//            this.timer--;

            // Intro - Always go to next line
            // z button still enabled for skipping text
//            if (InputProcessor.aJustPressed) {
                if (spritesNotDrawn.isEmpty()) {
                    game.insertAction(this.nextAction);

                    if (this.exitWhenDone == true) {
                        game.actionStack.remove(this);
                    }
                    else {
                        this.waitingOnExit = true;
                    }
                }
                else {
                    this.scrollUpAction = new DisplayTextIntro.ScrollTextUp(game, this.spritesBeingDrawn, this.spritesNotDrawn);
                    game.insertAction(this.scrollUpAction);
                }

            return;
        }

        // only extract sprite every 'speed' number of frames
        if (this.speedTimer > 0) {
            this.speedTimer--;
            return;
        }
        else {
            this.speedTimer = this.speed;
        }

        // If would take too many, stop
        for (int i=0; i < 1 && !spritesNotDrawn.isEmpty() && spritesBeingDrawn.size() < 36; i++) {
//            spritesBeingDrawn.add(spritesNotDrawn.get(0));
//            spritesNotDrawn.remove(0);  // TODO: remove
            spritesBeingDrawn.add(spritesNotDrawn.remove(0));
        }
    }

    // play a sound, ie victory fanfare
     // unique b/c need to mute battle music
    class PlaySound_Text extends Action {
        Music music;
        float initialVolume; // different tracks have diff volume

        boolean playedYet; // do music.play on first step
        public PlaySound_Text(String sound, Action nextAction) {
            this.nextAction = nextAction;
            this.playedYet = false;

            if (sound == "fanfare1") {
                this.music = Gdx.audio.newMusic(Gdx.files.internal("catch_fanfare.mp3")); // use this
                this.music.setLooping(false);
            }

            else if (sound == "Raikou") {
                this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/243Cry.ogg")); // use this
                this.music.setLooping(false);
            }
            else if (sound == "Entei") {
                this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/244Cry.ogg")); // use this
                this.music.setLooping(false);
            }
            else if (sound == "Suicune") {
                this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/245Cry.ogg"));
                this.music.setLooping(false);
            }
        }

        @Override
        public void step(Game game) {
            // play the sound
            if (this.music != null && !this.playedYet) {
                // TODO - use this probably in the future
//                this.initialVolume = game.currMusic.getVolume();
//                game.currMusic.setVolume(0f);

                this.music.play();
                this.playedYet = true;
            }

            if (!this.music.isPlaying()) {
//                game.currMusic.setVolume(this.initialVolume);
                game.actionStack.remove(this);

                game.insertAction(this.nextAction);
            }
        }
    }

    // move text up in text box
     // works b/c DisplayText will begin drawing new chars once SpritesBeingDrawn is small enough
    // parent might need to call step() for frame-correctness, not sure
    class ScrollTextUp extends Action {
        ArrayList<Vector2> positions;
        Vector2 position;

        ArrayList<Sprite> text;
        ArrayList<Sprite> otherText;

        public int layer = 110;

        public ScrollTextUp(Game game, ArrayList<Sprite> text, ArrayList<Sprite> otherText) {
            // TODO - bug, fails if scrolling up twice
            // TODO - get comma to work (what text contains comma?)

            this.text = text;
            this.otherText = otherText;

            this.positions = new ArrayList<Vector2>();
            for (int i=0; i < 5; i++) {
                this.positions.add(new Vector2(0,0));
            }
            this.positions.add(new Vector2(0,8));
            for (int i=0; i < 5; i++) {
                this.positions.add(new Vector2(0,0));
            }
            this.positions.add(new Vector2(0,8));

        }
        public String getCamera() {return "gui";}

        public int getLayer(){return this.layer;}

        // what to do at each iteration
        public void step(Game game) {
            this.position = this.positions.get(0);
            this.positions.remove(0);

            for (Sprite sprite : this.text) {
                sprite.setPosition(sprite.getX()+this.position.x, sprite.getY()+this.position.y);
            }
            for (Sprite sprite : this.otherText) {
                sprite.setPosition(sprite.getX()+this.position.x, sprite.getY()+this.position.y);
            }

            // if done, remove first 24 elements
            // frees up DisplayText's text array, which will get filled with new sprites
            if (this.positions.isEmpty()) {
                for (int i=0; i < 18; i++) {
                    this.text.remove(0);
                }
                game.actionStack.remove(this);

                return;
            }
        }
    }
}

// TODO: remove
class DoneAction extends Action {
    @Override
    public void step(Game game) {
        game.actionStack.remove(this);
    }
}

/**
 * Display "Thanks for playing!" text and white bg (for demo purposes).
 */
class DoneWithDemo extends Action {
    public int layer = 140;
    Sprite bgSprite;
    public DoneWithDemo(Game game) {
        Texture text = new Texture(Gdx.files.internal("battle/intro_frame6.png"));
        this.bgSprite = new Sprite(text);
    }
    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        this.bgSprite.draw(game.uiBatch);
        game.font.draw(game.uiBatch, "Thanks for playing!",44,80);
    }
}

/**
 * Draw Mobile controls on screen.
 * TODO: likely want setting where all controls are on right side of phone screen.
 */
class DrawMobileControls extends Action {
    public static Sprite upArrowSprite = new Sprite();
    public static Sprite downArrowSprite = new Sprite();
    public static Sprite leftArrowSprite = new Sprite();
    public static Sprite rightArrowSprite = new Sprite();
    public static Sprite aSprite = new Sprite();
    public static Sprite bSprite = new Sprite();
    public static Sprite startSprite = new Sprite();

    public DrawMobileControls(Game game) {
        Texture text = new Texture(Gdx.files.internal("gb_arrow2.png"));
        DrawMobileControls.upArrowSprite = new Sprite(text, 0, 0, 24, 24);
        DrawMobileControls.downArrowSprite = new Sprite(text, 0, 0, 24, 24);
        DrawMobileControls.downArrowSprite.flip(false, true);
        DrawMobileControls.rightArrowSprite = new Sprite(text, 0, 0, 24, 24);
        DrawMobileControls.rightArrowSprite.rotate90(true);
        DrawMobileControls.leftArrowSprite = new Sprite(text, 0, 0, 24, 24);
        DrawMobileControls.leftArrowSprite.rotate90(false);
        // TODO: a and b button sprites
        DrawMobileControls.aSprite = new Sprite(text, 0, 0, 24, 24);
        DrawMobileControls.bSprite = new Sprite(text, 0, 0, 24, 24);
        DrawMobileControls.startSprite = new Sprite(text, 0, 0, 24, 24);
    }

    @Override
    public void firstStep(Game game) {
        float scaleX = 160/game.currScreen.x;
        int offsetY = (int)(((game.currScreen.y-144/scaleX)/2)*scaleX);
        DrawMobileControls.upArrowSprite.setPosition(30, 90 - offsetY - 35);
        DrawMobileControls.downArrowSprite.setPosition(30, 40 - offsetY - 35);
        DrawMobileControls.leftArrowSprite.setPosition(5, 65 - offsetY - 35);
        DrawMobileControls.rightArrowSprite.setPosition(55, 65 - offsetY - 35);
        DrawMobileControls.bSprite.setPosition(100, 75 - offsetY - 35);
        DrawMobileControls.aSprite.setPosition(125, 85 - offsetY - 35);
        DrawMobileControls.startSprite.setPosition(100, 45 - offsetY - 35);
    }

    @Override
    public void step(Game game) {
        DrawMobileControls.upArrowSprite.draw(game.uiBatch);
        DrawMobileControls.downArrowSprite.draw(game.uiBatch);
        DrawMobileControls.leftArrowSprite.draw(game.uiBatch);
        DrawMobileControls.rightArrowSprite.draw(game.uiBatch);
        DrawMobileControls.aSprite.draw(game.uiBatch);
        DrawMobileControls.bSprite.draw(game.uiBatch);
        DrawMobileControls.startSprite.draw(game.uiBatch);
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return 0;}
}


//interface TestAction {
//    
//    
//    public TestAction(Object... params) {
//        for (Object param : params) {
//            System.out.println(param);
//        }
//    }
//}

/**
 * Displayed at the beginning of the game to show player how to play the game.
 * 
 * TODO: change displayed options for android, controlling input, etc.
 */
class DrawControls extends Action {
    public boolean remove = false;
    boolean displayControls = true;
    int timer = 0;
    int timerPadding = 60*4;
    float alpha = 1f;
    String currTrainerTip = "";
    int prevIndex = -1;
    ArrayList<String> messages;
    int randomIndex = 0;
    
    public DrawControls() {
        this.messages = new ArrayList<String>(TrainerTipsTile.messages);
        this.randomIndex = Game.rand.nextInt(this.messages.size());
    }

    public String getCamera() {return "gui";}

    @Override 
    public void firstStep(Game game) {}
    
    @Override
    public void step(Game game) {
        if (this.remove) {
            game.actionStack.remove(this);
        }
        if (this.timer <= 60) {
            if (this.timer % 10 == 0) {
                this.alpha = (float)this.timer/60f;
            }
        }
        else if (this.timer >= 5*60 + this.timerPadding) {
            if (this.timer % 10 == 0) {
                this.alpha = 1f - (float)(this.timer % 60)/60f;
            }
        }
        else if (InputProcessor.aJustPressed) {
            this.timer = 5*60 + this.timerPadding;
        }
        if (++this.timer >= 6*60 + this.timerPadding) {
            this.timer = 0;
            this.displayControls = false;
            this.timerPadding = 1*60;
//            this.randomIndex = Game.rand.nextInt(this.messages.size());
            this.randomIndex++;
            if (randomIndex >= this.messages.size()) {
                this.randomIndex = 0;
            }
            this.currTrainerTip = "   TRAINER TIPS!  "+this.messages.get(this.randomIndex);
            // Don't pick a tip that is too long or is same as previous tip.
            int tries = 0;
            while (tries < 4 && (this.randomIndex == this.prevIndex || this.currTrainerTip.length() > 105)) {
                this.randomIndex++; // = Game.rand.nextInt(this.messages.size());
                if (randomIndex >= this.messages.size()) {
                    this.randomIndex = 0;
                }
                this.currTrainerTip = "   TRAINER TIPS!  "+this.messages.get(this.randomIndex);
                tries++;
            }
            this.prevIndex = this.randomIndex;
        }

        if (!this.displayControls) {
            char[] textArray = this.currTrainerTip.toCharArray();
            int i = 0;
            int j = 0;
            Sprite letterSprite;
            for (int k=0; k < textArray.length; k++) {
                char character = textArray[k];
                if (character == ' ' && k+1 < textArray.length) {
                    int length = 1;
                    char nextChar = textArray[k+length];
                    while (nextChar != ' ' && k+length < textArray.length) {
                        nextChar = textArray[k+length++];
                    }
                    if (i+length > 20) {
                        i = 0;
                        j++;
                    }
                }
                letterSprite = game.textDict.get(character);
                letterSprite.setPosition(8*i, 128 -16*j);
                letterSprite.draw(game.uiBatch, this.alpha);
                i++;
                if (i > 20) {
                    i = 0;
                    j++;
                }
            }
            return;
        }
        
        for (int j=0; j < 8; j++) {
            if (j == 0) {
                char[] textArray = "   - Controls -".toCharArray();
                Sprite letterSprite;
                for (int i=0; i < textArray.length; i++) {
                    letterSprite = game.textDict.get(textArray[i]);
                    letterSprite.setPosition(8 +8*i, 128 -16*j);
                    letterSprite.draw(game.uiBatch, this.alpha);
//                    game.uiBatch.draw(letterSprite, 8 +8*i, 128 -16*j);
                }
            }
            else if (j == 1) {
                char[] textArray = "Arrows  - Movement".toCharArray();
                Sprite letterSprite;
                for (int i=0; i < textArray.length; i++) {
                    letterSprite = game.textDict.get(textArray[i]);
                    letterSprite.setPosition(8 +8*i, 128 -16*j);
                    letterSprite.draw(game.uiBatch, this.alpha);
//                    game.uiBatch.draw(letterSprite, 8 +8*i, 128 -16*j);
                }
            }
            else if (j == 2) {
                char[] textArray = "Z       - A button".toCharArray();
                Sprite letterSprite;
                for (int i=0; i < textArray.length; i++) {
                    letterSprite = game.textDict.get(textArray[i]);
                    letterSprite.setPosition(8 +8*i, 128 -16*j);
                    letterSprite.draw(game.uiBatch, this.alpha);
//                    game.uiBatch.draw(letterSprite, 8 +8*i, 128 -16*j);
                }
            }
            else if (j == 3) {
                char[] textArray = "X       - B button".toCharArray();
                Sprite letterSprite;
                for (int i=0; i < textArray.length; i++) {
                    letterSprite = game.textDict.get(textArray[i]);
                    letterSprite.setPosition(8 +8*i, 128 -16*j);
                    letterSprite.draw(game.uiBatch, this.alpha);
//                    game.uiBatch.draw(letterSprite, 8 +8*i, 128 -16*j);
                }
            }
            else if (j == 4) {
                char[] textArray = "Enter   - Menu".toCharArray();
                Sprite letterSprite;
                for (int i=0; i < textArray.length; i++) {
                    letterSprite = game.textDict.get(textArray[i]);
                    letterSprite.setPosition(8 +8*i, 128 -16*j);
                    letterSprite.draw(game.uiBatch, this.alpha);
//                    game.uiBatch.draw(letterSprite, 8 +8*i, 128 -16*j);
                }
            }
            else if (j == 5) {
                char[] textArray = "Hold X to run".toCharArray();
                Sprite letterSprite;
                for (int i=0; i < textArray.length; i++) {
                    letterSprite = game.textDict.get(textArray[i]);
                    letterSprite.setPosition(8 +8*i, 128 -16*j);
                    letterSprite.draw(game.uiBatch, this.alpha);
//                    game.uiBatch.draw(letterSprite, 8 +8*i, 128 -16*j);
                }
            }
        }
    }
}

/**
 * Displayed at the beginning of the game so that the player can specify setup options.
 */
class DrawSetupMenu extends Action {
    public static boolean drawChoosePokemonText = true;
    public static int avatarAnimCounter = 24;
    public static int currIndex = 0;  // Currently selected menu item
    public static int lastIndex = 0;
    public int layer = 5000;
    Sprite bgSprite;
    Sprite helperSprite;
    Sprite arrow;
    Sprite arrowFlipped;
    Sprite arrowWhite;
    ArrayList<Sprite> avatarSprites = new ArrayList<Sprite>();
    int avatarColorIndex = 0;
    int localHostJoinIndex = 0;
    int newLoadIndex = 0;
    int sizeIndex = 0;
    int fileIndex = 0;
    int offset = 0;
    int offset2 = 0;
    int offset3 = 0;
    Vector2 newPos;
    Map<Integer, Vector2> arrowCoords;
    ArrayList<Character> name = new ArrayList<Character>();
    ArrayList<Character> mapName = new ArrayList<Character>();
    ArrayList<Character> serverIp = new ArrayList<Character>();

    HashMap<Integer, Character> alphanumericKeys = new HashMap<Integer, Character>();
    HashMap<Integer, Character> alphanumericKeysShift = new HashMap<Integer, Character>();

    HashMap<Integer, Character> numberKeys = new HashMap<Integer, Character>();
    ArrayList<Color> colors = new ArrayList<Color>();
    ArrayList<String> fileNames = new ArrayList<String>();

    public DrawSetupMenu(Game game, Action nextAction) {
        super(game, nextAction);
        this.nextAction = nextAction;
        Texture text = new Texture(Gdx.files.internal("battle/arrow_right_white.png"));
        this.arrowWhite = new Sprite(text, 0, 0, 5, 7);
        text = new Texture(Gdx.files.internal("battle/arrow_right1.png"));
        this.arrow = new Sprite(text, 0, 0, 5, 7);
        this.arrowFlipped = new Sprite(this.arrow);
        this.arrowFlipped.flip(true, false);
        this.avatarSprites.add(game.player.standingSprites.get("down"));
        this.avatarSprites.add(game.player.movingSprites.get("down"));
        this.avatarSprites.add(game.player.altMovingSprites.get("down"));
        DrawSetupMenu.currIndex = DrawSetupMenu.lastIndex;

        this.arrowCoords = new HashMap<Integer, Vector2>();
//        for (int i=0; i < 3; i++) {
//            this.arrowCoords.put(i, new Vector2(1 +4, 128 - 16*i));
//        }
//        for (int i=3; i < 4; i++) {
//            this.arrowCoords.put(i, new Vector2(1 +4, 128 -16 -16*i));
//        }
        for (int i=0; i < 8; i++) {
            this.arrowCoords.put(i, new Vector2(1, 128 - 16*i));
        }
        // Cursor position based on lastIndex
        this.newPos = this.arrowCoords.get(DrawSetupMenu.currIndex);
        text = new Texture(Gdx.files.internal("battle/battle_bg3.png"));
        this.bgSprite = new Sprite(text, 0, 0, 176, 160);
        this.bgSprite.setPosition(-8, -8);

        char[] textArray = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        for (int i=0; i < textArray.length; i++) {
            this.alphanumericKeys.put(Input.Keys.valueOf(String.valueOf(textArray[i])),
                                      String.valueOf(textArray[i]).toLowerCase().charAt(0));
            this.alphanumericKeysShift.put(Input.Keys.valueOf(String.valueOf(textArray[i])),textArray[i]);
        }
        textArray = "1234567890".toCharArray();
        for (int i=0; i < textArray.length; i++) {
            this.numberKeys.put(Input.Keys.valueOf(String.valueOf(textArray[i])), textArray[i]);
            this.alphanumericKeys.put(Input.Keys.valueOf(String.valueOf(textArray[i])), textArray[i]);
            this.alphanumericKeysShift.put(Input.Keys.valueOf(String.valueOf(textArray[i])), textArray[i]);
        }
        this.numberKeys.put(Input.Keys.PERIOD, '.');

        this.colors.add(new Color(0.9137255f, 0.5294118f, 0.1764706f, 1f));  // Original texture color.
//        this.colors.add(Color.WHITE);
//        this.colors.add(Color.BLACK);
        this.colors.add(new Color(46f/255f, 113f/255f, 1f, 1f));  // blue
        this.colors.add(Color.CYAN);
        this.colors.add(new Color(47f/255f, 229f/255f, 53f/255f, 1f));  // green
        this.colors.add(Color.MAGENTA);
        this.colors.add(Color.MAROON);
        this.colors.add(Color.YELLOW);
//        this.colors.add(new Color(245f/255f, 250f/255f, 90f/255f, 1f));  // yellow
        this.colors.add(Color.OLIVE);
        this.colors.add(Color.TEAL);
        this.colors.add(Color.RED);
        this.colors.add(Color.PURPLE);
        this.colors.add(new Color(255f/255f, 115f/255f, 200f/255f, 1f));  // pink

        // Get a list of all saved maps
        File directory = new File("./");
        File[] files = directory.listFiles();
        for (File file : files) {
            String filename = file.getName();
            if (filename.endsWith(".sav") && !filename.endsWith("players.sav") && !filename.endsWith("player.sav")) {
                this.fileNames.add(filename);
            }
        }
    }
    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void firstStep(Game game) {
        // TODO: needs work
//        game.currMusic = Gdx.audio.newMusic(Gdx.files.internal("music/shore3.ogg"));
//        game.currMusic.setLooping(true);
//        game.currMusic.setVolume(0.6f);
//        game.currMusic.play();
        game.currMusic = new LinkedMusic("music/follow_me1_intro", "music/follow_me1");
        game.currMusic.setVolume(0.8f);
        game.currMusic.play();
    }

    @Override
    public void step(Game game) {
//        this.bgSprite.draw(game.uiBatch);  // TODO: remove
        newPos = this.arrowCoords.get(DrawSetupMenu.currIndex);

        for (int j=0; j < 8; j++) {
            if (j == 0) {
//                char[] textArray = "LOCAL  HOST   JOIN".toCharArray();  // TODO: re-enable once working
                char[] textArray = "LOCAL  ".toCharArray();
                Sprite letterSprite;
                for (int i=0; i < textArray.length; i++) {
                    letterSprite = game.textDict.get(textArray[i]);
                    game.uiBatch.draw(letterSprite, 8 +8*i, 128 -16*j);
                }
                int offsetX = 0;
                this.offset = 0;
                if (this.localHostJoinIndex == 1) {
                    offsetX = 64-8;
                }
                if (this.localHostJoinIndex == 2) {
                    offsetX = 112;
                    this.offset = -1;
                    this.offset2 = 0;
                    this.newLoadIndex = 0;
                }

                if (this.localHostJoinIndex == 1 && this.newLoadIndex == 0) {
                    this.offset3 = -2;
                }
                else {
                    this.offset3 = 0;
                }

                if (j == DrawSetupMenu.currIndex) {
                      // TODO: re-enable once working
//                    if (InputProcessor.leftJustPressed && this.localHostJoinIndex > 0) {
//                        this.localHostJoinIndex--;
//                    }
//                    if (InputProcessor.rightJustPressed && this.localHostJoinIndex < 2) {
//                        this.localHostJoinIndex++;
//                    }
                    newPos = newPos.cpy().add(offsetX, 0);
                }
                game.uiBatch.draw(this.arrowWhite, this.arrowCoords.get(j).x+offsetX, this.arrowCoords.get(j).y);
            }
            else if (j == 1+this.offset) {
                char[] textArray = "NEW    LOAD".toCharArray();
                Sprite letterSprite;
                for (int i=0; i < textArray.length; i++) {
                    letterSprite = game.textDict.get(textArray[i]);
                    game.uiBatch.draw(letterSprite, 8 +8*i, 128 -16*j);
                }
                int offsetX = 0;
                if (this.newLoadIndex == 1) {
                    offsetX = 64-24+16;
                    this.offset2 = -3;
                }
                else {
                    this.offset2 = 0;
                }
                if (j == DrawSetupMenu.currIndex) {
                    if (InputProcessor.leftJustPressed && this.newLoadIndex > 0) {
                        this.newLoadIndex--;
                    }
                    if (InputProcessor.rightJustPressed && this.newLoadIndex < 1) {
                        this.newLoadIndex++;
                    }
                    newPos = newPos.cpy().add(offsetX, 0);
                }
                game.uiBatch.draw(this.arrowWhite, this.arrowCoords.get(j).x+offsetX, this.arrowCoords.get(j).y);
            }
            // enter map name
            else if (j == 2+this.offset+this.offset2) {
                // Server IP address _ (enter ip)
                if (this.localHostJoinIndex == 2) {
                    char[] textArray = "Server IP".toCharArray();
                    Sprite letterSprite;
                    for (int i=0; i < textArray.length; i++) {
                        letterSprite = game.textDict.get(textArray[i]);
                        game.uiBatch.draw(letterSprite, 8 +8*i, 128 -16*j);
                    }
                    for (int i=0; i < this.serverIp.size(); i++) {
                        letterSprite = game.textDict.get(this.serverIp.get(i));
                        game.uiBatch.draw(letterSprite, 8 +8*i, 128 -16*(j+1));
                    }
                    if (j == DrawSetupMenu.currIndex+this.offset) {
                        if (this.serverIp.size() < 15) {
                            for (Integer key : this.numberKeys.keySet()) {
                                if (Gdx.input.isKeyJustPressed(key)) {
                                    this.serverIp.add(this.numberKeys.get(key));
                                }
                            }
                        }
                        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE) && this.serverIp.size() > 0) {
                            this.serverIp.remove(this.serverIp.size()-1);
                        }
                        if (DrawSetupMenu.avatarAnimCounter >= 12) {
                            letterSprite = game.textDict.get('_');
                            game.uiBatch.draw(letterSprite, 8 + 8*this.serverIp.size(), 128 -16 -16*j);
                        }
                    }
                    else if (this.serverIp.size() <= 0) {
                        textArray = "127.0.0.1".toCharArray();
                        for (int i=0; i < textArray.length; i++) {
                            letterSprite = game.textDict.get(textArray[i]);
                            game.uiBatch.draw(letterSprite, 8 +8*i, 128 -16*(j+1));
                        }
                    }
                }
                else {
                    char[] textArray = "File".toCharArray();
                    Sprite letterSprite;
                    for (int i=0; i < textArray.length; i++) {
                        letterSprite = game.textDict.get(textArray[i]);
                        game.uiBatch.draw(letterSprite, 8 +8*i, 128 -16*j);
                    }
                    if (j == DrawSetupMenu.currIndex+this.offset) {
                        if (this.mapName.size() < 11) {
                            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                                for (Integer key : this.alphanumericKeysShift.keySet()) {
                                    if (Gdx.input.isKeyJustPressed(key)) {
                                        this.mapName.add(this.alphanumericKeysShift.get(key));
                                    }
                                }
                            }
                            else {
                                for (Integer key : this.alphanumericKeys.keySet()) {
                                    if (Gdx.input.isKeyJustPressed(key)) {
                                        this.mapName.add(this.alphanumericKeys.get(key));
                                    }
                                }
                            }
                        }
                        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE) && this.mapName.size() > 0) {
                            this.mapName.remove(this.mapName.size()-1);
                        }
                        if (DrawSetupMenu.avatarAnimCounter >= 12) {
                            letterSprite = game.textDict.get('_');
                            game.uiBatch.draw(letterSprite, 8 +5*8 + 8*this.mapName.size(), 128 -16*j);
                        }
                    }
                    else if (this.mapName.size() <=0) {
                        textArray = "default".toCharArray();
                        for (int i=0; i < textArray.length; i++) {
                            letterSprite = game.textDict.get(textArray[i]);
                            game.uiBatch.draw(letterSprite, 8 +5*8 +8*i, 128 -16*j);
                        }
                    }
                    for (int i=0; i < this.mapName.size(); i++) {
                        letterSprite = game.textDict.get(this.mapName.get(i));
                        game.uiBatch.draw(letterSprite, 8 +5*8 +8*i, 128 -16*j);
                    }
                }
            }
            // Enter map size
            else if (j == 3+this.offset+this.offset2 && this.localHostJoinIndex != 2) {
//                char[] textArray = "Size  S M L XL XXL".toCharArray();  // TODO: remove
                char[] textArray = "World S M L XL XXL".toCharArray();
                Sprite letterSprite;
                for (int i=0; i < textArray.length; i++) {
                    letterSprite = game.textDict.get(textArray[i]);
                    game.uiBatch.draw(letterSprite, 8 +8*i, 128 -16*j);
                }
                int offsetX = 0;
                if (this.sizeIndex == 0) {
                    offsetX = 0;
                }
                else if (this.sizeIndex < 5) {
                    offsetX = 32 + 16*this.sizeIndex;
                }
                else  {
                    offsetX = 40 + 16*this.sizeIndex;
                }
                if (j == DrawSetupMenu.currIndex+this.offset) {
                    if (InputProcessor.leftJustPressed && this.sizeIndex > 0) {
                        this.sizeIndex--;
                    }
                    if (InputProcessor.rightJustPressed && this.sizeIndex < 5) {
                        this.sizeIndex++;
                    }
                    newPos = newPos.cpy().add(offsetX, 0);
                }
                game.uiBatch.draw(this.arrowWhite, this.arrowCoords.get(j).x+offsetX, this.arrowCoords.get(j).y);
            }
            // Name: _ (enter name)
            else if (j == 4+this.offset+this.offset2+this.offset3) {
                char[] textArray = "Name".toCharArray();
                Sprite letterSprite;
                for (int i=0; i < textArray.length; i++) {
                    letterSprite = game.textDict.get(textArray[i]);
                    game.uiBatch.draw(letterSprite, 8 +8*i, 128 -16*j);
                }
                // Let the player type their name in via keyboard
                if (j == DrawSetupMenu.currIndex) {
                    if (this.name.size() < 11) {
                        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                            for (Integer key : this.alphanumericKeysShift.keySet()) {
                                if (Gdx.input.isKeyJustPressed(key)) {
                                    this.name.add(this.alphanumericKeysShift.get(key));
                                }
                            }
                        }
                        else {
                            for (Integer key : this.alphanumericKeys.keySet()) {
                                if (Gdx.input.isKeyJustPressed(key)) {
                                    this.name.add(this.alphanumericKeys.get(key));
                                }
                            }
                        }
                    }
                    if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE) && this.name.size() > 0) {
                        this.name.remove(this.name.size()-1);
                    }
                    if (DrawSetupMenu.avatarAnimCounter >= 12) {
                        letterSprite = game.textDict.get('_');
                        game.uiBatch.draw(letterSprite, 8 +5*8 + 8*this.name.size(), 128 -16*j);
                    }
                }
                for (int i=0; i < this.name.size(); i++) {
                    letterSprite = game.textDict.get(this.name.get(i));
                    game.uiBatch.draw(letterSprite, 8 +5*8 +8*i, 128 -16*j);
                }
            }

            // < (character sprite) >
            else if (j == 5+this.offset+this.offset2+this.offset3 && this.newLoadIndex != 1) {
                Sprite avatarSprite;
                // Animate player avatar
                if (j == DrawSetupMenu.currIndex) {
                    if (DrawSetupMenu.avatarAnimCounter >= 18) {
                        avatarSprite = new Sprite(this.avatarSprites.get(0));
                    }
                    else if (DrawSetupMenu.avatarAnimCounter >= 12) {
                        avatarSprite = new Sprite(this.avatarSprites.get(1));
                    }
                    else if (DrawSetupMenu.avatarAnimCounter >= 6) {
                        avatarSprite = new Sprite(this.avatarSprites.get(0));
                    }
                    else {
                        avatarSprite = new Sprite(this.avatarSprites.get(2));
                    }

                    if (InputProcessor.leftJustPressed) {
                        this.avatarColorIndex--;
                        if (this.avatarColorIndex < 0) {
                            this.avatarColorIndex = this.colors.size()-1;
                        }
                        game.player.setColor(this.colors.get(this.avatarColorIndex));
                        this.avatarSprites.clear();
                        this.avatarSprites.add(game.player.standingSprites.get("down"));
                        this.avatarSprites.add(game.player.movingSprites.get("down"));
                        this.avatarSprites.add(game.player.altMovingSprites.get("down"));
                    }
                    if (InputProcessor.rightJustPressed) {
                        this.avatarColorIndex++;
                        if (this.avatarColorIndex >= this.colors.size()) {
                            this.avatarColorIndex = 0;
                        }
                        game.player.setColor(this.colors.get(this.avatarColorIndex));
                        this.avatarSprites.clear();
                        this.avatarSprites.add(game.player.standingSprites.get("down"));
                        this.avatarSprites.add(game.player.movingSprites.get("down"));
                        this.avatarSprites.add(game.player.altMovingSprites.get("down"));
                    }

                }
                else {
                    avatarSprite = new Sprite(this.avatarSprites.get(0));
                }
                avatarSprite.setPosition(24, 124 -16*j);
                avatarSprite.draw(game.uiBatch);
                game.uiBatch.draw(this.arrowFlipped, 16, 124 -16*j);
                game.uiBatch.draw(this.arrow, 43, 124 -16*j);
            }
            // or file selector < (file name) >
            else if (j == 5+this.offset+this.offset2+this.offset3) {
                if (!this.fileNames.isEmpty()) {
                    char[] textArray = this.fileNames.get(this.fileIndex).toCharArray();
                    Sprite letterSprite;
                    for (int i=0; i < textArray.length; i++) {
                        letterSprite = game.textDict.get(textArray[i]);
                        game.uiBatch.draw(letterSprite, 8 +24 +8*i, 124 -16*(j+this.offset));
                    }
                    game.uiBatch.draw(this.arrowFlipped, 16, 124 -16*(j+this.offset));
                    game.uiBatch.draw(this.arrow, 43 + 8*this.fileNames.get(this.fileIndex).length(), 124 -16*j);
                }
                if (j == DrawSetupMenu.currIndex) {
                    if (InputProcessor.leftJustPressed && this.fileIndex > 0) {
                        this.fileIndex--;
                    }
                    if (InputProcessor.rightJustPressed && this.fileIndex < this.fileNames.size()-1) {
                        this.fileIndex++;
                    }
                }
            }
            // Go!
            else if (j == 6+this.offset+this.offset2+this.offset3) {
                char[] textArray = "Go!".toCharArray();
                Sprite letterSprite;
                for (int i=0; i < textArray.length; i++) {
                    letterSprite = game.textDict.get(textArray[i]);
                    game.uiBatch.draw(letterSprite, 8 +8*i, 128 -16*j);
                }
                if (j == DrawSetupMenu.currIndex) {
                    // Set up the map.
                    if (InputProcessor.startJustPressed || InputProcessor.aJustPressed) {
//                        game.currMusic.stop();
//                        game.currMusic.dispose();
//                        game.playerCanMove = true;  // TODO: ideally not here, but required to make this
                        // fademusic work.
//                        Action fadeMusic = new FadeMusic("currMusic", "out", "stop", .0125f, null);
                        Action fadeMusic = new FadeMusic(game.currMusic, -0.0125f,
                                           new CallMethod(game.currMusic, "stop", new Object[]{},
                                           new CallMethod(game.currMusic, "dispose", new Object[]{},
                                           null)));
                        // TODO: remove, idk what these were for.
//                        fadeMusic.firstStep(game);
//                        fadeMusic.firstStep = false;
//                        fadeMusic.step(game);
                        game.insertAction(fadeMusic);
                        if (this.localHostJoinIndex != 2) {
                            String mapName = "";
                            if (this.newLoadIndex == 0) {
                                for (int i=0; i < this.mapName.size(); i++) {
                                    mapName += this.mapName.get(i).toString();
                                }
                                if (mapName.equals("")) {
                                    mapName = "default";
                                }
                            }
                            else {
                                mapName = this.fileNames.get(this.fileIndex).split(".sav")[0];
                            }

                            // if hosting, then start server
                            Action hostAction = null;
                            if (this.localHostJoinIndex == 1) {
                                try {
                                    game.initServer();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                game.insertAction(new ServerBroadcast(game));
                                game.debugInputEnabled = true;  // allow player to look around
                                hostAction = new DisplayText(game, "Welcome to host mode!           WASD moves camera     Q and E zooms",
                                                             null, null, 
                                                             null);
//                                game.insertAction(new WaitFrames(game, 340,
//                                                  new SetField(game, "displayTextAction", null,
//                                                  null)));
                            }
                            else {
                                // required for loading map (getKryo())
                                game.server = new Server();
                                Network.register(game.server);
                            }
                            final Action hostActionFinal = hostAction;
                            final Action drawControls = new DrawControls();
                            game.map = new PkmnMap(mapName);
                            // player chose new game
                            if (this.newLoadIndex == 0) {
                                // TODO: warning text about this
                                // TODO: uncomment
//                                if (this.fileNames.contains(mapName+".sav")) {
//                                    return;
//                                }
                                game.actionStack.remove(this);

                                // World size
                                if (this.sizeIndex == 0) {
                                    if (!game.debugInputEnabled) {
                                        this.sizeIndex = 1;
                                    }
                                    else {
                                        this.sizeIndex = -1;
                                    }
                                }
                                
                                // TODO: remove somethign and make 100 always.
                                // Largest worlds were causing issues.
                                // TODO: took too long to load XXL and peoples laptops were having issues
                                // TODO: map sometimes lags (likely due to large num overworld pokemon)
                                // TODO: saving game (maybe) takes too long
                                int something = 100;
                                if (this.sizeIndex > 4) {
                                    something = 80;
                                }
                                else if (this.sizeIndex > 3) {
                                    something = 90;  //  attempt to shrink larger worlds.
                                }
                                // TODO: would ideally be bigger, like 100*200*this.sizeIndex
                                final int size = 100*something*(this.sizeIndex+2);
                                
                                // TODO: debug, comment or removee
//                                size = 100*100;  // 100*300 // 100*500 // 100*180 // 100*100 // 20*30 // 60*100 // 100*120

                                // Fun fact - this probably wouldn't/won't work for html5
                                // according to libGDX docs multithreading doesn't work
                                // for html5.
                                // Possible that I could have the thread run on the gdx thread,
                                // and just keep stop/starting it after it took slightly less time
                                // than one frame.
                                
                                
                                // attempt at threaded create - doesn't work b/c 
                                // needs opengl context when generating tiles
                                // might be able to make texturecache post runnable to fix this.
                                // 
                                Thread thread = new Thread(new Runnable() {
                                    public void run() {
                                        try {
                                            System.out.println("Generating map...");
                                            System.out.println(java.time.LocalTime.now());
                                            final Action genIsland = new GenIsland1(Game.staticGame, new Vector2(0, 0), size);
                                            System.out.println("Done.");
                                            System.out.println(java.time.LocalTime.now());
                                            Runnable runnable = new Runnable() {
                                                public void run() {
                                                    Game.staticGame.start();
                                                    Game.staticGame.insertAction(genIsland);
                                                    genIsland.step(Game.staticGame);
                                                    EnterBuilding enterBuilding = new EnterBuilding(Game.staticGame, "", null);
                                                    enterBuilding.slow = 8;
                                                    Game.staticGame.insertAction(enterBuilding);
                                                    Game.staticGame.insertAction(new DisplayText.Clear(Game.staticGame,
                                                                                 new SetField(drawControls, "remove", true,
                                                                                 hostActionFinal)));
                                                }
                                            };
                                            Gdx.app.postRunnable(runnable);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                thread.setPriority(Thread.MIN_PRIORITY);
                                thread.start();

//                                System.out.println("Generating map...");
//                                System.out.println(java.time.LocalTime.now());
//
//                                // Note reg. full-res screenshot using pixmap:
//                                //  Tried 100*800, pixmap save didn't work.
//                                //  Tried 100*700, pixmap error loading tiles/water2.png
//                                game.start();  // TODO: test
//                                Action genIsland = new GenIsland1(game, new Vector2(0, 0), size);
//                                genIsland.step(game);  // fixes issue where player would try to move into tiles that didn't exist yet
//                                game.insertAction(genIsland);
//
//                                System.out.println("Done.");
//                                System.out.println(java.time.LocalTime.now());
                                
                                // TODO: uncomment
                                game.insertAction(new DisplayText(game, "Generating... please wait...", null, true, false, null));
                                game.insertAction(drawControls);
                                
                                //
//                                game.map = new PkmnMap("default"); // TODO: ideally shouldn't have to do this.
//                                game.start();
//                                game.insertAction(new TileEditor());  // TODO: formally handle somehow, maybe command line arg

                                // Set player name
                                String name = "";
                                for (int i=0; i < this.name.size(); i++) {
                                    name += this.name.get(i).toString();
                                }
                                game.player.name = name;


                                // TODO: debug, remove
    //                            if (Gdx.app.getType() != ApplicationType.Android) {
    //                                try {
    //                                    game.initClient("25.8.66.159");
    //                                } catch (IOException e) {
    //                                    e.printStackTrace();
    //                                }
    //                            }
    //                            else {
    //                                Log.set(Log.LEVEL_DEBUG);
    //                                try {
    //                                    game.initServer();
    //                                } catch (IOException e) {
    //                                    e.printStackTrace();
    //                                } catch (Exception e) {
    //                                    e.printStackTrace();
    //                                    Log.debug("pkmngen", e.getStackTrace().toString());
    //                                }
    //                            }
                            }
                            else {
                                game.actionStack.remove(this);
                                game.start();
                                try {
                                    game.map.loadFromFile(game);
                                    InputProcessor.aJustPressed = false;  // Hack to prevent interaction after loading
//                                    throw new Exception();
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                    JFrame frame = new JFrame("Error");
                                    JOptionPane.showMessageDialog(frame, "There was an error loading this file - it may be too old for this build.\nPlease create a bug at github.com/SheerSt/pokemon-wilds.");
                                    System.exit(0);
                                }
                                // Reload game logfile
                                try {
                                    game.logFile = new FileWriter(game.map.id+".log");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                EnterBuilding enterBuilding = new EnterBuilding(game, "", null);
                                enterBuilding.slow = 8;
                                game.insertAction(enterBuilding);
                                
                                // TODO: remove
                                game.insertAction(new TileEditor());  // TODO: formally handle somehow, maybe command line arg
                            }
                            // periodically save game in both host/local cases
                            // TODO: this lags too much for large maps, which causes server desync.
                            // not sure what to do.
//                            game.insertAction(new PkmnMap.PeriodicSave(game));
                        }
                        // join existing server
                        else {
                            // Set player name
                            String name = "";
                            for (int i=0; i < this.name.size(); i++) {
                                name += this.name.get(i).toString();
                            }
                            game.player.name = name;
                            // Set ip addr
                            String ipAddr = "";
                            for (int i=0; i < this.serverIp.size(); i++) {
                                ipAddr += this.serverIp.get(i).toString();
                            }
                            if (ipAddr.equals("")) {
                                ipAddr = "127.0.0.1";
                            }
                            game.actionStack.remove(this);
                            game.map = new PkmnMap("default"); // TODO: ideally shouldn't have to do this.
                            game.start();
                            // set up networking
                            try {
                                game.initClient(ipAddr);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            EnterBuilding enterBuilding = new EnterBuilding(game, "", null);
                            enterBuilding.slow = 8;
                            game.insertAction(enterBuilding);
                        }
//                        EnterBuilding enterBuilding = new EnterBuilding(game, "", null);
//                        enterBuilding.slow = 8;
//                        game.insertAction(enterBuilding);
//                        for (int i=0; i < 12*enterBuilding.slow; i++) {
//                            enterBuilding.step(game);
//                        }
                    }
                }
            }
        }

        // Decrement avatar anim counter
        DrawSetupMenu.avatarAnimCounter--;
        if (DrawSetupMenu.avatarAnimCounter <= 0) {
            DrawSetupMenu.avatarAnimCounter = 24;
        }

        // Handle arrow input
        int max = 6;
        if (this.localHostJoinIndex == 2) {
            max = 5;
        }
        if (this.localHostJoinIndex == 1) {
            max = 4;
        }
        if (this.newLoadIndex == 1) {
            max = 3;
        }
        if (InputProcessor.upJustPressed) {
            if (DrawSetupMenu.currIndex > 0) {
                DrawSetupMenu.currIndex -= 1;
                if (this.localHostJoinIndex == 2 && DrawSetupMenu.currIndex == 1) {
                    DrawSetupMenu.currIndex -= 1;
                }
                DrawSetupMenu.avatarAnimCounter = 24; // reset to 12 for 1 extra frame of first frame for avatar anim
            }
            else {
                DrawSetupMenu.currIndex = max;
            }
        }
        else if (InputProcessor.downJustPressed) {
            if (DrawSetupMenu.currIndex < max) {
                DrawSetupMenu.currIndex += 1;
                if (this.localHostJoinIndex == 2 && DrawSetupMenu.currIndex == 1) {
                    DrawSetupMenu.currIndex += 1;
                }
                DrawSetupMenu.avatarAnimCounter = 24; // reset to 12 for 1 extra frame of first frame for avatar anim
            }
            else {
                DrawSetupMenu.currIndex = 0;
            }
        }
        // Draw the arrow sprite
        this.arrow.setPosition(this.newPos.x, this.newPos.y);
        this.arrow.draw(game.uiBatch);
    }
}

/**
 * Fade the music in or out.
 */
class FadeMusic extends Action {
    Music music;
    float rate =  -0.05f;
    float maxVol = 1f;
//    float maxVol = 1f;
    public static Action currFadeMusic;

//    public FadeMusic(Game game, String musicName, float rate, Action nextAction) {
//        if (!game.loadedMusic.containsKey(musicName)) {
//            game.loadedMusic.put(musicName, new LinkedMusic("music/"+musicName, ""));
//        }
//        this(game.loadedMusic.get(musicName), rate, nextAction);
//    }

    public FadeMusic(Music music, float rate, float maxVol, Action nextAction) {
        this.music = music;
        this.rate = rate;
        this.maxVol = maxVol;
        this.nextAction = nextAction;
    }
    
    public FadeMusic(Music music, float rate, Action nextAction) {
        this(music, rate, 1f, nextAction);
    }

//    public FadeMusic(String musicName, String direction, String shouldPause, float rate, Action nextAction) {
//        this.musicName = musicName;
//        this.shouldPause = shouldPause;
//
//        this.direction = direction;
//        if (direction.equals("out")) {
//            this.amt = -rate;
//        }
//        else {
//            this.amt = rate;
//        }
//
//        this.nextAction = nextAction;
//    }
//
//    public FadeMusic(String musicName, String direction, String shouldPause, float rate, boolean switchCurrMusic, Action nextAction) {
//        this.musicName = musicName;
//
//        this.shouldPause = shouldPause;
//
//        this.switchCurrMusic = switchCurrMusic;
//
//        this.direction = direction;
//        if (direction.equals("out")) {
//            this.amt = -rate;
//        }
//        else {
//            this.amt = rate;
//        }
//        this.nextAction = nextAction;
//    }
//
//    public FadeMusic(String musicName, String direction, String shouldPause, float rate, boolean switchCurrMusic, float maxVol, Action nextAction) {
//        this.maxVol = maxVol;
//        this.musicName = musicName;
//        this.shouldPause = shouldPause;
//        this.switchCurrMusic = switchCurrMusic;
//        this.direction = direction;
//        if (direction.equals("out")) {
//            this.amt = -rate;
//        }
//        else {
//            this.amt = rate;
//        }
//        this.nextAction = nextAction;
//    }
//
//    public FadeMusic(String musicName, String direction, String shouldPause, float rate, boolean switchCurrMusic, float maxVol, Music.OnCompletionListener onCompleteListener, Action nextAction) {
//        this(musicName, direction, shouldPause, rate, switchCurrMusic, maxVol, nextAction);
//        this.onCompleteListener = onCompleteListener;
//    }

    @Override
    public void firstStep(Game game) {
        FadeMusic.currFadeMusic = this;
    }
    
    @Override
    public void step(Game game) {
        if (FadeMusic.currFadeMusic == this) {
            // Only allow one FadeMusic to be happening at a time.
            this.music.setVolume(this.music.getVolume()+this.rate);
//            System.out.println(this.music.getVolume());
        }
        // Bug - for some reason this can't be .0f - no idea why. 'failed to allocate' issue if play() (on any Music) ever called in future
        if (FadeMusic.currFadeMusic != this || this.music.getVolume() <= 0.1f || this.music.getVolume() >= this.maxVol) {
//            System.out.println(FadeMusic.currFadeMusic == this);
//            System.out.println(this.music.getVolume());
            if (this.music.getVolume() < this.maxVol) {
                this.music.pause();
            }
            this.music.setVolume(this.maxVol);
            game.actionStack.remove(this);
            if (FadeMusic.currFadeMusic == this) {
                game.insertAction(this.nextAction);
            }
        }
    }

//    public void oldStep(Game game) {
////        if (!game.playerCanMove) {
////            return;
////        }
//        // If player enters battle, pause any music that's currently fading.
//        if (FadeMusic.pause) {
//            return;
//        }
//
//        if (this.firstStep) {
//            FadeMusic.currFadeMusic = this;
//            if (this.direction.equals("in")) {
//                if (this.musicName.equals("currMusic")) {
//                    game.currMusic.play();
//                }
//                else {
//                    // Load music if it isn't loaded already
//                    if (!game.loadedMusic.containsKey(this.musicName)) {
////                        String extension = ".ogg";
////                        Music temp = Gdx.audio.newMusic(Gdx.files.internal("music/"+this.musicName+extension));
//                        Music temp = new LinkedMusic("music/"+this.musicName, "");
//                        temp.setVolume(0.1f); // will always fade in (for now, have option to fade in fast) //.1f because of 'failed to allocate' bug
//                        if (this.onCompleteListener != null) {
//                            temp.setOnCompletionListener(this.onCompleteListener);
//                        }
//                        game.loadedMusic.put(this.musicName, temp);
//                    }
//                    game.loadedMusic.get(this.musicName).play();
//                }
//            }
//            if (this.switchCurrMusic) {
//                // if currmusic still playing, fade it out
//                if (game.currMusic != null) {
//                    if (game.currMusic.isPlaying()) {
////                      game.insertAction(new FadeMusic("currMusic", "out", "pause", .05f, null));
//                        game.currMusic.pause();
//                    }
//                }
//                game.currMusic = game.loadedMusic.get(this.musicName);
//                game.map.currRoute.music = game.currMusic; // TODO: shouldn't be doing this probably
//            }
//
//            // set this.music for reference
//            if (this.musicName.equals("currMusic")) {
//                // TODO: issue here if game.currMusic changes after this point, won't fade anymore.
//                this.music = game.currMusic;
//            }
//            else {
//                this.music = game.loadedMusic.get(this.musicName);
//            }
//
//            // Shouldn't ever be the case, occasionally is in testing tho.
//            if (this.music == null) {
//                game.actionStack.remove(this);
//                game.insertAction(this.nextAction);
//                return;
//            }
//
//            this.firstStep = false;
//        }
//
//        // The reason that this is here is to prevent a fadeout->fadein chain from overwriting battle music
//        // ie, music starts transitioning in overworld, player enters battle (changes game.currMusic), then fademusic(in)
//        // overwrites the battle music.
//        // This should be handled better somehow, not sure how though.
//        //  - still want the fademusic to continue after the battle (which this accomplishes).
//        // TODO: test
//        // if another FadeMusic got inserted, then stop doing stuff.
//        if (FadeMusic.currFadeMusic != this) {
//            if (this.direction.equals("in")) {
//                this.music.setVolume(this.maxVol);
//            }
//            else if (this.direction.equals("out")) {
//                this.music.setVolume(0f);
//            }
////            game.actionStack.remove(this);
////            return;
//        }
//        else {
//            this.music.setVolume(this.music.getVolume()+this.amt);
////            System.out.println(this.music.getVolume());
////            System.out.println(this.amt);
//        }
//
//        if (this.direction.equals("out")) {
//            if (music.getVolume() <= 0.1f) {  // bug - for some reason this can't be .0f - no idea why. 'failed to allocate' issue if play() (on any Music) ever called in future
//                if (this.shouldPause.equals("pause")) {
//                    this.music.setVolume(0.1f);
//                    this.music.pause();
//                }
//                else if (this.shouldPause.equals("stop")) {
//                    // dispose and remove from loadedMusic
//                    this.music.setVolume(0.1f);
//                    this.music.stop();
//                    this.music.dispose();
//                    game.loadedMusic.remove(this.musicName);
//
//                }
//                game.actionStack.remove(this);
//                game.insertAction(this.nextAction);
//            }
//        }
//        else {
//            if (music.getVolume() >= this.maxVol) {
//                this.music.setVolume(this.maxVol);
//                game.actionStack.remove(this);
//                game.insertAction(this.nextAction);
//            }
//        }
//    }
}

/**
 * Handle input events in a less annoying fashion than libGDX input processors.
 * Note: this is called before all Actions, so functionally behaves (almost) exactly
 * the same as an InputProcessor.
 */
class InputProcessor extends Action {

    public static boolean acceptInput = true;
    public static boolean upPressed = false;
    public static boolean downPressed = false;
    public static boolean leftPressed = false;
    public static boolean rightPressed = false;
    public static boolean aPressed = false;
    public static boolean bPressed = false;
    public static boolean startPressed = false;
    public static boolean upJustPressed = false;
    public static boolean downJustPressed = false;
    public static boolean leftJustPressed = false;
    public static boolean rightJustPressed = false;
    public static boolean aJustPressed = false;
    public static boolean bJustPressed = false;
    public static boolean startJustPressed = false;
    Vector2 touchLoc = new Vector2();

    @Override
    public void step(Game game) {
        // Use this when orchestrating button presses (like when auto-moving player)
        if (!InputProcessor.acceptInput) {
            return;
        }
        if (Gdx.input.isTouched()) {
//            int menuHeight = (int)((160*game.currScreen.y)/game.currScreen.x);  // height/width = menuHeight/160
            float scaleX = 160/game.currScreen.x;
//            float scaleY = 144/game.currScreen.y;
            int offsetY = (int)(((game.currScreen.y-144/scaleX)/2)*scaleX);
            // height/width = menuHeight/160
            this.touchLoc.set(Gdx.input.getX()*scaleX, (game.currScreen.y-Gdx.input.getY())*scaleX - offsetY); // game.currScreen.y - 
        }
        else {
            this.touchLoc.set(-1, -1);
        }
        // Up
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || DrawMobileControls.upArrowSprite.getBoundingRectangle().contains(this.touchLoc)) {
            InputProcessor.upJustPressed = false;
            if (!InputProcessor.upPressed) {
                InputProcessor.upJustPressed = true;
            }
            InputProcessor.upPressed = true;
        }
        else {
            InputProcessor.upJustPressed = false;
            InputProcessor.upPressed = false;
        }
        // Down
//        System.out.println(DrawMobileControls.downArrowSprite.getBoundingRectangle().contains(this.touchLoc));
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || DrawMobileControls.downArrowSprite.getBoundingRectangle().contains(this.touchLoc)) {
            InputProcessor.downJustPressed = false;
            if (!InputProcessor.downPressed) {
                InputProcessor.downJustPressed = true;
            }
            InputProcessor.downPressed = true;
        }
        else {
            InputProcessor.downJustPressed = false;
            InputProcessor.downPressed = false;
        }
        // Left
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || DrawMobileControls.leftArrowSprite.getBoundingRectangle().contains(this.touchLoc)) {
            InputProcessor.leftJustPressed = false;
            if (!InputProcessor.leftPressed) {
                InputProcessor.leftJustPressed = true;
            }
            InputProcessor.leftPressed = true;
        }
        else {
            InputProcessor.leftJustPressed = false;
            InputProcessor.leftPressed = false;
        }
        // Right
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || DrawMobileControls.rightArrowSprite.getBoundingRectangle().contains(this.touchLoc)) {
            InputProcessor.rightJustPressed = false;
            if (!InputProcessor.rightPressed) {
                InputProcessor.rightJustPressed = true;
            }
            InputProcessor.rightPressed = true;
        }
        else {
            InputProcessor.rightJustPressed = false;
            InputProcessor.rightPressed = false;
        }
        // A
        if (Gdx.input.isKeyPressed(Input.Keys.Z) || DrawMobileControls.aSprite.getBoundingRectangle().contains(this.touchLoc)) {
            InputProcessor.aJustPressed = false;
            if (!InputProcessor.aPressed) {
                InputProcessor.aJustPressed = true;
            }
            InputProcessor.aPressed = true;
        }
        else {
            InputProcessor.aJustPressed = false;
            InputProcessor.aPressed = false;
        }
        // B
        if (Gdx.input.isKeyPressed(Input.Keys.X) || DrawMobileControls.bSprite.getBoundingRectangle().contains(this.touchLoc)) {
            InputProcessor.bJustPressed = false;
            if (!InputProcessor.bPressed) {
                InputProcessor.bJustPressed = true;
            }
            InputProcessor.bPressed = true;
        }
        else {
            InputProcessor.bJustPressed = false;
            InputProcessor.bPressed = false;
        }
        // Start
        if (Gdx.input.isKeyPressed(Input.Keys.ENTER) || DrawMobileControls.startSprite.getBoundingRectangle().contains(this.touchLoc)) {
            InputProcessor.startJustPressed = false;
            if (!InputProcessor.startPressed) {
                InputProcessor.startJustPressed = true;
            }
            InputProcessor.startPressed = true;
        }
        else {
            InputProcessor.startJustPressed = false;
            InputProcessor.startPressed = false;
        }
    }

    public String getCamera() {
        return "map";
    }

    public int getLayer(){
        return -1;
    }
}

/**
 * TODO: migrate away from this.
 * This has the extra 'disabled' variable.
 */
class MenuAction extends Action {
    public boolean disabled;
    boolean drawArrowWhite; // for DrawPlayerMenu
    public boolean goAway = false;

    MenuAction prevMenu;
    int currIndex;
}



/**
 * Handles music and transitions between tracks.
 */
class MusicController extends Action {
    boolean firstStep = true;
    public String startBattle = null;  // Triggered when entering battle
    public boolean battleVictoryFanfare = false;  // Trigger victory fanfare music
    public boolean inBattle = false;
    public boolean battleFadeOut = false;
    public boolean playerFainted = false;

    public String startTimeOfDay = null;  // Triggered when time of day transitions
    public boolean unownMusic = false;

    public String currOverworldMusic = null;
    public String startOverworldMusic = null;  // next overworld music queued to be played
    public String startDungeonMusic = null;
    public boolean fadeToDungeon = false;
    public boolean resumeOverworldMusic = false;
    public boolean inTransition = false;

    public boolean startEvolveMusic = false;
    public boolean evolving = false;
    public boolean evolveMusicFadeout = false;

    public String startNightAlert = null;
    public boolean nightAlert = false;

    String currDayTime = null;

    Music.OnCompletionListener musicCompletionListener;

    public MusicController(Game game) {
        game.musicController = this;
    }

    @Override
    public void firstStep(Game game) {
        this.startTimeOfDay = game.map.timeOfDay;
    }

    @Override
    public void step(Game game) {
        // Code will attempt to set fields on game.musicController, so this object needs
        // to exist even in the case of a Server.
        if (game.type == Game.Type.SERVER) {
            return;
        }

        boolean isDungeon = game.map.currRoute != null && game.map.currRoute.isDungeon;

        if (this.startBattle != null && !this.inBattle) {
            System.out.println("startBattle");
            // TODO: needs to trigger in regi cave even if it's night
            // probably check currRoute or something
            if (isDungeon || (!this.currDayTime.equals("night") && !this.unownMusic)) {

                game.currMusic.pause();
                if (this.startBattle.equals("wild")) {
                    game.currMusic = game.battle.music;
                    game.currMusic.stop();
                    game.currMusic.setVolume(0.3f);
//                    game.currMusic.setVolume(0f); // TODO: debug, remove
//                    BattleFadeOutMusic.stop = true;  // TODO: remove
//                    FadeMusic.pause = true;  // TODO: remove
                }
                else {
                    if (!game.loadedMusic.containsKey(this.startBattle)) {
                        game.loadedMusic.put(this.startBattle, new LinkedMusic("music/"+this.startBattle+"_intro", "music/"+this.startBattle));
                    }
                    game.currMusic = game.loadedMusic.get(this.startBattle);
                    game.currMusic.stop();
                    game.currMusic.setVolume(0.4f);
                    if (this.startBattle.contains("regi")) {
                        game.currMusic.setVolume(0.3f);
                    }
                }
                game.currMusic.play();
            }
            this.inBattle = true;
            this.startBattle = null;
        }
        if (this.battleVictoryFanfare && this.inBattle) {
            if (!SpecialMewtwo1.class.isInstance(game.battle.oppPokemon) && !this.unownMusic) {
                game.currMusic.pause();
                game.currMusic = game.battle.victoryFanfare;
                game.currMusic.stop();
                game.currMusic.play();
            }
            this.battleVictoryFanfare = false;
        }
        if (this.battleFadeOut && this.inBattle) {
            System.out.println("battleFadeOut");
            if (isDungeon ||
                !this.currDayTime.equals("night") ||
                game.map.timeOfDay.equals("day") ||
                this.playerFainted ||
                this.unownMusic) {
//                game.currMusic.setVolume(0.3f);  // Emulates previous behavior
                float currVol = game.currMusic.getVolume();
                currVol = 0.004f*(currVol/0.3f);  // Sounds slightly better for fanfare fade out.
                System.out.println(currVol);
                game.insertAction(new FadeMusic(game.currMusic, -currVol, null));
//                                  new SetField(this, "inBattle", false, null)));  // BattleFadeMusic does this for now.
            }
            this.unownMusic = false;
            this.battleFadeOut = false;
        }
        if (this.startEvolveMusic) {
            game.currMusic.pause();
            game.currMusic = Gdx.audio.newMusic(Gdx.files.internal("evolution1.ogg"));
            game.currMusic.play();
            this.evolving = true;
            this.startEvolveMusic = false;
        }
        if (this.evolveMusicFadeout && this.evolving) {
            game.insertAction(new FadeMusic(game.currMusic, -0.02f,
                              new CallMethod(game.currMusic, "stop", new Object[]{},
                              new CallMethod(game.currMusic, "dispose", new Object[]{},
                              new SetField(this, "evolving", false,
                              new SetField(this, "resumeOverworldMusic", true,
                              null))))));
            this.evolveMusicFadeout = false;
        }
        if (this.resumeOverworldMusic && !this.inBattle && !this.nightAlert) {
            System.out.println("resume overworld music");
            System.out.println(game.map.timeOfDay.equals("night"));
            System.out.println(this.inBattle);
//            if (!this.inTransition) {  // TODO: remove once tested
                game.currMusic.pause();
                if (isDungeon) {
                    String musicName = game.map.currRoute.getNextMusic(true);
                    if (!game.loadedMusic.containsKey(musicName)) {
                        Music music = Gdx.audio.newMusic(Gdx.files.internal("music/"+musicName+".ogg"));
                        music.setLooping(true);
                        game.loadedMusic.put(musicName, music);
                    }
                    game.currMusic = game.loadedMusic.get(musicName);
                    // This didn't work when I set it while adding to game.loadedMusic
                    // maybe FadeMusic re-sets the volume to 1f.
                    // TODO: testing
                    // sound pretty good
                    game.currMusic.stop(); 
                    if (musicName.contains("sealed_chamber")) {
                        game.currMusic.setVolume(0.25f);
                    }
                }
                else if (this.startTimeOfDay == null) {
                    System.out.println(this.currOverworldMusic);
                    System.out.println(game.currMusic.getVolume());
                    if (!game.loadedMusic.containsKey(this.currOverworldMusic)) {
                        game.loadedMusic.put(this.currOverworldMusic, Gdx.audio.newMusic(Gdx.files.internal("music/"+this.currOverworldMusic+".ogg")));
                    }
                    game.currMusic = game.loadedMusic.get(this.currOverworldMusic);
                }
                if (!game.currMusic.isPlaying()) {
                    game.currMusic.play();
                }
//            }  // TODO: remove once tested
            this.resumeOverworldMusic = false;
            this.playerFainted = false;
        }
        if (this.fadeToDungeon) {
            System.out.println("fadeToDungeon");
            // TODO: remove
//            if (!this.currDayTime.equals("night")) {
                String nextMusicName = this.currOverworldMusic;
                // If previous overworld music was night, and it's not currently night, need to refresh.
                if (isDungeon || nextMusicName.contains("night")) {
                    nextMusicName = game.map.currRoute.getNextMusic(true);  // was false
                }
                System.out.println("nextMusicName");
                System.out.println(nextMusicName);
                // TODO: this was the previous behavior
//                Action action = new FadeMusic(game.currMusic, -0.025f,
//                                new WaitFrames(game, 10, 
//                                null));
                // This will accomodate musics that are at a different volume (hopefully)
                float rate = game.currMusic.getVolume() * -0.025f;
                Action action = new FadeMusic(game.currMusic, rate, null);
                // TODO: potentially remove
//                if (game.map.currRoute.isDungeon) {
//                    action.append(new SetField(this, "startDungeonMusic", nextMusicName, null));
//                }
//                else {
//                    action.append(new SetField(this, "startOverworldMusic", nextMusicName, null));
//                }
                action.append(new SetField(this, "resumeOverworldMusic", true, null));
                game.insertAction(action);
//            }
            this.fadeToDungeon = false;
        }
        // TODO: potentially remove
//        // Had to make this sep b/c overworld music fade was overriding the dungeon music
//        if (this.startDungeonMusic != null && !this.inBattle && !this.evolving) {
//            if (!this.unownMusic) {
//                game.currMusic.pause();
//                if (!game.loadedMusic.containsKey(this.startDungeonMusic)) {
//                    game.loadedMusic.put(this.startDungeonMusic, Gdx.audio.newMusic(Gdx.files.internal("music/"+this.startDungeonMusic+".ogg")));
//                }
//                game.currMusic = game.loadedMusic.get(this.startDungeonMusic);
//                game.currMusic.setLooping(true);
//                // TODO: volume control some other way
//                if (this.startDungeonMusic.contains("sealed_chamber")) {
//                    game.currMusic.setVolume(0.25f);
//                }
//                game.currMusic.play();
//            }
//            this.startDungeonMusic = null;
//        }
        if (this.startNightAlert != null) {
            game.currMusic.pause();
            if (!game.loadedMusic.containsKey(this.startNightAlert)) {
                game.loadedMusic.put(this.startNightAlert, Gdx.audio.newMusic(Gdx.files.internal(this.startNightAlert+".ogg")));
            }
            Music music = game.loadedMusic.get(this.startNightAlert);
            music.stop();
//            music.setVolume(.7f);  // was
            music.setVolume(0.5f);
            music.setLooping(true);
            game.currMusic = music;
            game.currMusic.play();
            this.nightAlert = true;
            this.startNightAlert = null;
        }
        if (this.startOverworldMusic != null && !this.inBattle && !this.evolving) {
            System.out.println("startOverworldMusic");
            System.out.println(this.startOverworldMusic);
            // Was a fade in, just starting music now.
            if (!isDungeon && !this.currDayTime.equals("night") && !this.unownMusic) {
                game.currMusic.pause();
                if (!game.loadedMusic.containsKey(this.startOverworldMusic)) {
                    game.loadedMusic.put(this.startOverworldMusic, Gdx.audio.newMusic(Gdx.files.internal("music/"+this.startOverworldMusic+".ogg")));
                }
                game.currMusic = game.loadedMusic.get(this.startOverworldMusic);
//                if (!this.startOverworldMusic.contains("pkmnmansion") &&
//                    !this.startOverworldMusic.contains("sealed_chamber")) {
//                if (game.map.currRoute.isDungeon) {
//                    game.currMusic.setLooping(true);
//                }
//                else {
                game.currMusic.setOnCompletionListener(this.musicCompletionListener);
                this.currOverworldMusic = this.startOverworldMusic;
//                }
//                // TODO: remove
//                if (this.startOverworldMusic.contains("sealed_chamber")) {
//                    game.currMusic.setVolume(0.25f);
//                    System.out.println("here3");
//                }
                game.currMusic.play();
            }
            this.startOverworldMusic = null;
        }
        if (this.startTimeOfDay != null && !this.inBattle &&
            !this.playerFainted && !this.unownMusic &&
            !this.evolving && !isDungeon) {
            this.currDayTime = this.startTimeOfDay;
            System.out.println("startTimeOfDay");
            System.out.println(this.startTimeOfDay);
            // TODO: test
//            if (game.battle.oppPokemon == null || !SpecialMewtwo1.class.isInstance(game.battle.oppPokemon)) {
            if (this.startTimeOfDay.equals("day")) {
                this.musicCompletionListener = new Music.OnCompletionListener() {
                    @Override
                    public void onCompletion(Music aMusic) {
                        String nextMusicName = Game.staticGame.map.currRoute.getNextMusic(true);
//                            System.out.println(nextMusicName);  // debug
//                            String nextMusicName = Game.staticGame.musicController.currOverworldMusic;  // debug
                        
//                            Action nextMusic = new FadeMusic("currMusic", "out", "", 0.025f,
//                                               new WaitFrames(Game.staticGame, 360,
//                                               new FadeMusic(nextMusicName, "in", "", 0.2f, true, 1f, this,
//                                               null)));

                        Game.staticGame.loadedMusic.remove(MusicController.this.currOverworldMusic);  // TODO: test
                        Action nextMusic = new FadeMusic(aMusic, -0.0125f,  // used for fading night music out
                                           new WaitFrames(Game.staticGame, 360,
                                           new SetField(Game.staticGame.musicController,
                                                        "startOverworldMusic", nextMusicName,
                                           new CallMethod(aMusic, "dispose", new Object[]{},
                                           null))));
                        Game.staticGame.insertAction(nextMusic);

                        // TODO: remove
                        System.out.println("loaded musics");
                        for (String name : Game.staticGame.loadedMusic.keySet()) {
                            System.out.println(name);
                        }
                        
//                            nextMusic.step(Game.staticGame);  // messes up firstStep if done
//                            Game.staticGame.fadeMusicAction = nextMusic;  // TODO: remove
//                            MusicController.currOverworldMusic = nextMusicName;  // TODO remove
                    }
                };
                if (!this.firstStep) {
                    // TODO: maybe move this elsewhere.
                    // Felt like this was a safe ish place to do it.
                    // TODO: this feels too dangerous
//                    for (String name : new ArrayList<String>(game.loadedMusic.keySet())) {
//                        Music music = game.loadedMusic.get(name);
//                        if (!music.isPlaying()) {
//                            game.loadedMusic.remove(name);
//                            music.dispose();
//                        }
//                    }

                    String nextMusicName = game.map.currRoute.getNextMusic(true);
//                        BattleFadeOutMusic.playerFainted = true;  // TODO: this is just a hack around issues with FadeMusic
//                        Action nextMusic = new BattleFadeOutMusic(game,
//                                           new WaitFrames(game, 360,
//                                           new FadeMusic(nextMusicName, "in", "", 0.2f, true, 1f, musicCompletionListener,
//                                           null)));
                    // TODO: this could cause problems if resumeoverworldmusic is set true
                    // but inTransition is somehow also still true.
//                        this.inTransition = true;  // TODO: test without
                    Action nextMusic = new FadeMusic(game.currMusic, -0.025f,  // used for fading night music out
                                       new WaitFrames(game, 360,
                                       new SetField(game.musicController, "startOverworldMusic", nextMusicName,
                                       new SetField(game.musicController, "inTransition", false,
                                       null))));
//                        game.fadeMusicAction = nextMusic; // TODO: is this being used?
//                        MusicController.currOverworldMusic = nextMusicName;
                    game.insertAction(nextMusic);
                    this.currOverworldMusic = nextMusicName;
                }
                else {
                    game.currMusic = Gdx.audio.newMusic(Gdx.files.internal("music/nature1_render.ogg"));
                    if (!game.loadedMusic.containsKey("nature1_render")) {
                        game.loadedMusic.put("nature1_render", game.currMusic);
                    }
                    game.currMusic.setLooping(false);
                    game.currMusic.setVolume(1f);
                    game.currMusic.play();
                    game.currMusic.pause();
                    game.currMusic.setPosition(130f);  
                    game.currMusic.play();
                    game.currMusic.setOnCompletionListener(this.musicCompletionListener);
                    this.currOverworldMusic = "nature1_render";
                }
            }
            else {
                System.out.println("start night music");
                game.currMusic.stop();
//                    game.actionStack.remove(game.fadeMusicAction);
                if (!game.loadedMusic.containsKey("night1")) {
                    game.loadedMusic.put("night1", Gdx.audio.newMusic(Gdx.files.internal("night1.ogg")));
                }
                Music music = game.loadedMusic.get("night1");
                music.setLooping(true);
                music.setVolume(.4f);  // TODO: adjust
                game.currMusic = music;
//                    game.map.currRoute.music = music;  // TODO: remove
                this.currOverworldMusic = "night1";
                game.currMusic.play();
            }
            this.startTimeOfDay = null;
        }
        this.firstStep = false;
    }
}

/**
 * Plays a Sound and then calls sound.dispose().
 */
class PlaySound extends Action {
    Music music;
    String sound;
    boolean cached = false;

    boolean playedYet; // do music.play on first step

    public PlaySound(Pokemon pokemon, Action nextAction) {
        this(pokemon, false, nextAction);
    }
    // for playing pokemon cry
    public PlaySound(Pokemon pokemon, boolean cached, Action nextAction) {
        this.nextAction = nextAction;
        this.playedYet = false;
        this.sound = pokemon.name;
        this.music = null;
        this.cached = cached;
        // Don't play cry if this is an egg
        if (pokemon.isEgg) {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("egg_noise1.ogg"));
            this.music.setLooping(false);
        }
        // if it's crystal pokemon, load from crystal dir
        else if (pokemon.generation == Pokemon.Generation.CRYSTAL) {
        	if(pokemon.isGhost)
        	{
        		this.music = Gdx.audio.newMusic(Gdx.files.internal("crystal_pokemon/cries/000.ogg"));
        		this.music.setVolume(0.9f);
        	}
        	else {
        		this.music = Gdx.audio.newMusic(Gdx.files.internal("crystal_pokemon/cries/" + pokemon.dexNumber + ".ogg"));
        		this.music.setVolume(0.5f);        		
        	}

            if (cached) {
                if (!Game.staticGame.loadedMusic.containsKey(sound)) {
                    Game.staticGame.loadedMusic.put(sound, Gdx.audio.newMusic(Gdx.files.internal("crystal_pokemon/cries/" + pokemon.dexNumber + ".ogg")));
                }
                this.music = Game.staticGame.loadedMusic.get(sound);
                this.music.stop();
                this.music.setVolume(0.5f);
                this.music.setLooping(false);
                return;
            }

            this.music = Gdx.audio.newMusic(Gdx.files.internal("crystal_pokemon/cries/" + pokemon.dexNumber + ".ogg"));
            if(pokemon.isGhost)
            {
                this.music = Gdx.audio.newMusic(Gdx.files.internal("crystal_pokemon/cries/000.ogg"));
                this.music.setVolume(0.9f);
            }
            else {
                this.music = Gdx.audio.newMusic(Gdx.files.internal("crystal_pokemon/cries/" + pokemon.dexNumber + ".ogg"));
                this.music.setVolume(0.5f);             
            }
            this.music.setLooping(false);
        }
    }

    public PlaySound(String sound, Action nextAction) {
        this(sound, 1f, nextAction);
    }

    public PlaySound(String sound, float volume, Action nextAction) {
        this(sound, volume, false, nextAction);
    }

    public PlaySound(String sound, float volume, boolean cached, Action nextAction) {
        this.nextAction = nextAction;
        this.playedYet = false;
        this.sound = sound;
        this.music = null;
        this.cached = cached;

        if (cached) {
            if (!Game.staticGame.loadedMusic.containsKey(sound)) {
                Game.staticGame.loadedMusic.put(sound, Gdx.audio.newMusic(Gdx.files.internal(sound+".ogg")));
            }
            this.music = Game.staticGame.loadedMusic.get(sound);
            this.music.stop();
            this.music.setVolume(volume);
            this.music.setLooping(false);
            return;
        }

        if (sound == "laser1") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("data/laser1.wav")); // use this
            this.music.setLooping(false);
        }

        else if (sound == "laser2") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("data/laser2.wav")); // use this
            this.music.setLooping(false);
        }

        else if (sound == "laser3") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("data/laser3.wav")); // use this
            this.music.setLooping(false);
        }

        else if (sound == "laser4") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("data/laser4.wav")); // use this
            this.music.setLooping(false);
        }

        else if (sound == "laser5") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("data/laser6.mp3")); // use this
            this.music.setLooping(false);
        }

        else if (sound == "bump2") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("bump2.wav")); // use this
            this.music.setLooping(false);
        }
        else if (sound == "ledge1") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("ledge1.wav")); // use this
            this.music.setLooping(false);
        }
        else if ("menu_open1".equals(sound)) {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("attack_menu/menu_open1.ogg")); // use this
            this.music.setLooping(false);
        }

        else if (sound == "wild_battle") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("battle/battle-vs-wild-pokemon.wav")); // use this
            this.music.setLooping(false);
            this.music.setVolume(0.3f);
        }

        // pkmn
        else if (sound == "Zubat") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/041Cry.ogg"));
            this.music.setLooping(false);
        }
        else if (sound == "Oddish") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/043Cry.ogg"));
            this.music.setLooping(false);
        }
        else if (sound == "Gloom") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/044Cry.ogg"));
            this.music.setLooping(false);
        }
        else if (sound == "Cloyster") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/091Cry.ogg"));
            this.music.setLooping(false);
            this.music.setVolume(0.6f);
        }
        else if (sound == "Electabuzz") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/electabuzz.mp3")); // use this
            this.music.setLooping(false);
        }
        else if (sound == "Scyther") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/123Cry.ogg"));
            this.music.setLooping(false);
        }
        else if (sound == "Tauros") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/128Cry.mp3"));
            this.music.setLooping(false);
        }
        else if (sound.equals("mewtwo")) {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/150Cry.ogg"));
            this.music.setLooping(false);
            this.music.setVolume(0.8f);
        }
        else if (sound.equals("Mega Gengar")) {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/m_gengar_cry1.ogg"));
            this.music.setLooping(false);
            this.music.setVolume(0.5f);
        }
        else if (sound == "Spinarak") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/167Cry.ogg"));
            this.music.setLooping(false);
        }
        else if (sound == "Mareep") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/179Cry.wav"));
            this.music.setVolume(0.7f);
            this.music.setLooping(false);
        }
        else if (sound == "Flaaffy") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/180Cry.ogg"));
            this.music.setLooping(false);
        }
        else if (sound == "Steelix") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/208Cry.mp3")); // use this
            this.music.setLooping(false);
        }
        else if (sound == "Sneasel") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/215Cry.ogg"));
            this.music.setVolume(0.7f);
            this.music.setLooping(false);
        }
        else if (sound == "Raikou") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/243Cry.ogg")); // use this
            this.music.setLooping(false);
        }
        else if (sound == "Entei") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/244Cry.ogg")); // use this
            this.music.setLooping(false);
        }
        else if (sound == "Suicune") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/245Cry.ogg")); // use this
            this.music.setLooping(false);
        }

        else if (sound == "Wurmple") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/265Cry.ogg"));
            this.music.setLooping(false);
        }
        else if (sound == "Makuhita") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/296Cry.mp3"));
            this.music.setLooping(false);
        }
        else if (sound == "Hariyama") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/297Cry.mp3"));
            this.music.setLooping(false);
        }
        else if (sound == "Skitty") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/300Cry.ogg"));
            this.music.setLooping(false);
        }
        else if (sound == "Sableye") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/302Cry.ogg"));
            this.music.setLooping(false);
        }
        else if (sound == "Lairon") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/305Cry.ogg"));
            this.music.setLooping(false);
        }
        else if (sound == "Cacnea") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/331Cry.ogg"));
            this.music.setLooping(false);
        }
        else if (sound == "Shuppet") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/353Cry.ogg"));
            this.music.setLooping(false);
        }
        else if (sound == "Starly") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/396Cry.ogg"));
            this.music.setLooping(false);
        }
        else if (sound == "Shinx") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/403Cry.ogg"));
            this.music.setLooping(false);
        }
        else if (sound == "Gardevoir") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/282Cry.ogg"));
            this.music.setLooping(false);
        }
        else if (sound == "Claydol") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/344Cry.ogg"));
            this.music.setLooping(false);
        }
        else if (sound == "Machop") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/066Cry.ogg"));
            this.music.setLooping(false);
            this.music.setVolume(0.5f);
        }

        else if (sound == "throw_rock1") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("throw_rock_anim/rock_throw1.mp3"));
            this.music.setLooping(false);
        }
        else if (sound == "throw_pokeball1") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("throw_pokeball_anim/pkball1.mp3"));
            this.music.setLooping(false);
        }
        else if (sound == "poof1") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("throw_pokeball_anim/poof1.mp3"));
            this.music.setLooping(false);
        }
        else if (sound == "pokeball_wiggle1") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("throw_pokeball_anim/wiggle1.mp3"));
            this.music.setLooping(false);
        }
        else if (sound == "click1") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("click1.mp3"));
            this.music.setLooping(false);
        }
        else if (sound == "fanfare1") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("catch_fanfare.mp3"));
            this.music.setLooping(false);
        }
        else if (sound == "run1") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("run1.mp3"));
            this.music.setLooping(false);
        }

        // attacks
        else if (sound == "hyperbeam1") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("hyper_beam_anim/hyperbeam1.ogg"));
            this.music.setLooping(false);
        }
        else if (sound == "harden1") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("attacks/harden1.ogg"));
            this.music.setLooping(false);
        }
        else if (sound == "tackle1") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("attacks/tackle1.ogg"));
            this.music.setLooping(false);
        }
        else if (sound.equals("psychic1")) {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("attacks/psychic1.ogg"));
            this.music.setLooping(false);
        }
        else if (sound.equals("night_shade1")) {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("attacks/night_shade1.ogg"));
            this.music.setLooping(false);
        }
        else if (sound.equals("lick1")) {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("attacks/lick1.ogg"));
            this.music.setLooping(false);
        }
        else if (sound.equals("slash1")) {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("attacks/slash1.ogg"));
            this.music.setLooping(false);
        }
        else if (sound.equals("hit_normal1")) {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("attacks/hit_normal1.ogg"));
            this.music.setLooping(false);
        }
        else if (sound == "Mewtwo_Special1") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("attacks/Mewtwo_Special1.ogg"));
            this.music.setLooping(false);
            this.music.setVolume(1.5f);
        }
        else if (sound == "ghost1") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("ghost1.ogg"));
            this.music.setLooping(false);
        }
        else if (sound == "cut1") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("cut1.ogg"));
            this.music.setLooping(false);
        }
        else if (sound == "strength1") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("strength1.ogg"));
            this.music.setLooping(false);
        }
        else {
            this.music = Gdx.audio.newMusic(Gdx.files.internal(sound+".ogg"));
            this.music.setLooping(false);
        }
        this.music.setVolume(volume);
    }

    @Override
    public void step(Game game) {
        if (this.sound.equals("mgengar_battle1")) {
//            game.currMusic.stop();
            game.loadedMusic.get("mgengar_battle1").play();
            game.currMusic = game.loadedMusic.get("mgengar_battle1");
//            game.currMusic.play();
//            game.actionStack.remove(this);
//            game.insertAction(this.nextAction);
            return;
        }

        if (this.music == null) {
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
            return;
        }
        // play the sound
        if (!this.playedYet) {
            this.music.play();
            this.playedYet = true;
        }
        if (!this.music.isPlaying()) {
            // TODO: getting stuck here occasionally?
            if (!this.cached) {
                this.music.dispose();
            }
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
        }
    }
}

/**
 * TODO: migrating to using DisplayText.Clear instead.
 */
class RemoveDisplayText extends Action {
    public RemoveDisplayText(Action nextAction) {
        this.nextAction = nextAction;
    }

    @Override
    public void step(Game game) {
        game.actionStack.remove(game.displayTextAction);
        game.displayTextAction = null;
        game.actionStack.remove(this);
        game.insertAction(this.nextAction);
    }
}

/**
 * Set an arbitrary field of an object to a value when step() is called.
 * 
 * Example: SetField(game.player, <-- Set a field on this object.
 *                   "dirFacing", <-- Set this field.
 *                   "left",      <-- Set the field to this value.
 *                   null)        <-- (nextAction, inserted immediately after setting field)
 */
class SetField extends Action {
    Object object;
    String field;
    Object setTo;

    public SetField(Object object, String field, Object setTo, Action nextAction) {
        this.nextAction = nextAction;
        this.object = object;
        this.field = field;
        this.setTo = setTo;
    }

    @Override
    public void step(Game game) {
        if (this.object != null) {
            try {
                this.object.getClass().getField(this.field).set(this.object, this.setTo);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        game.actionStack.remove(this);
        game.insertAction(this.nextAction);
    }
}



class SetArrayAtIndex extends Action {
    Object[] object;
    int index;
    Object setTo;

    public SetArrayAtIndex(Object[] object, int index, Object setTo, Action nextAction) {
        this.nextAction = nextAction;
        this.object = object;
        this.index = index;
        this.setTo = setTo;
    }

    @Override
    public void step(Game game) {
        if (this.object != null) {
            try {
                this.object[this.index] = this.setTo;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        game.actionStack.remove(this);
        game.insertAction(this.nextAction);
    }
}

/**
 * Ideally don't do this.
 */
class ChangePlayerColor extends Action {
    
//    public static int colorIndex = 0;
    int index = 0;
    
    ArrayList<Color> colors = new ArrayList<Color>();

    public ChangePlayerColor(Action nextAction) {
        this.nextAction = nextAction;
        this.colors.add(new Color(0.9137255f, 0.5294118f, 0.1764706f, 1f));  // Original texture color.
        this.colors.add(new Color(46f/255f, 113f/255f, 1f, 1f));  // blue
        this.colors.add(Color.CYAN);
        this.colors.add(new Color(47f/255f, 229f/255f, 53f/255f, 1f));  // green
        this.colors.add(Color.MAGENTA);
        this.colors.add(Color.MAROON);
        this.colors.add(Color.YELLOW);
        this.colors.add(Color.OLIVE);
        this.colors.add(Color.TEAL);
        this.colors.add(Color.RED);
        this.colors.add(Color.PURPLE);
        this.colors.add(new Color(255f/255f, 115f/255f, 200f/255f, 1f));  // pink
    }

    @Override
    public void step(Game game) {

        if (InputProcessor.leftJustPressed) {
            this.index--;
            if (this.index < 0) {
                this.index = this.colors.size()-1;
            }
            game.player.setColor(this.colors.get(this.index));
            game.player.currSprite = game.player.standingSprites.get(game.player.dirFacing);
        }
        else if (InputProcessor.rightJustPressed) {
            this.index++;
            if (this.index >= this.colors.size()) {
                this.index = 0;
            }
            game.player.setColor(this.colors.get(this.index));
            game.player.currSprite = game.player.standingSprites.get(game.player.dirFacing);
        }

        if (InputProcessor.bJustPressed) {
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
        }
        
    }
}

/**
 * Just an experiment for drawing tiles.
 */
class TileEditor extends Action {
    
    Vector3 touchPos = new Vector3();
    boolean justTouched = false;
    public int timer = 0;
    public int brushSize = 1;
    
    // List of pokemon, list of overtiles, list of undertiles
    ArrayList<String> overTiles = new ArrayList<String>();
    {
        overTiles.add("cactus7");
        overTiles.add("rock1_color");
        overTiles.add("tree7");
        overTiles.add("aloe_large1");
    }
    
    ArrayList<String> underTiles = new ArrayList<String>();
    {
        underTiles.add("green1");
        underTiles.add("sand1");
        underTiles.add("sand2");
        underTiles.add("sand3");
        underTiles.add("flower2");
        underTiles.add("flower3");
        underTiles.add("flower4");
        underTiles.add("grass2");
        underTiles.add("grass3");
        underTiles.add("grass4");
        underTiles.add("water2");

        underTiles.add("grass_sand2");
        underTiles.add("grass_sand3");
        underTiles.add("desert1");
        underTiles.add("desert2");
        underTiles.add("desert3");
        underTiles.add("desert4");
        

        underTiles.add("ruins1_NE");
        underTiles.add("ruins1_NSEW");
        underTiles.add("ruins1_SE");
        underTiles.add("ruins1_NW");
        underTiles.add("ruins1_SW");
        underTiles.add("ruins1_pillar1");

        // Ruins
        underTiles.add("ruins2_floor");
        underTiles.add("ruins2_door");
        underTiles.add("ruins2_wall1");
        underTiles.add("black1");
//        underTiles.add("cave1_floor1");
//        underTiles.add("cave1_right1");
        
        // Trees
        overTiles.add("bush1");
        overTiles.add("tree2");
        overTiles.add("tree4");
        overTiles.add("tree6");
        overTiles.add("cactus1");
        overTiles.add("cactus2");
        overTiles.add("cactus3");
        overTiles.add("cactus4");
        overTiles.add("cactus5");
        overTiles.add("cactus6");
        

        underTiles.addAll(this.overTiles);
    }
    
    Tile currTile;
    int currIndex = 0;

    public TileEditor() {
        
    }

    @Override
    public void firstStep(Game game) {
        this.currTile = new Tile(this.underTiles.get(0), new Vector2(0, 0), true, null);
        this.currTile.sprite.setAlpha(0.7f);
        if (this.currTile.overSprite != null) {
            this.currTile.overSprite.setAlpha(0.7f);
        }
    }

    @Override
    public void step(Game game) {
        if (this.timer < 60) {
            this.timer++;
        }
        else if (this.timer == 60) {
//            Gdx.gl.glClearColor(1, 1, 1, 1);  // TODO: why?
        }
        

//        newTile = new Tile(currTile.name, game.player.currBuildTile.name,
//                           currTile.position.cpy(), true, currTile.routeBelongsTo);
//        // Carpet replaces the lower tile
//        if (game.player.currBuildTile.name.contains("carpet")) {
//            newTile = new Tile(game.player.currBuildTile.name, currTile.nameUpper,
//                               currTile.position.cpy(), true, currTile.routeBelongsTo);
//        }

        // Draw currently selected tile or pokemon at mouse cursor.
        
        if (this.currTile != null) {

            this.touchPos.x = Gdx.input.getX();
            this.touchPos.y = Gdx.input.getY();
            game.cam.unproject(this.touchPos);
            
            int offsetX = 0;
            int offsetY = 0;
            if (this.touchPos.x < 0) {
                offsetX = -16;
            }
            if (this.touchPos.y < 0) {
                offsetY = -16;
            }
            for (int i = 0; i < this.brushSize; i++) {
                for (int j = 0; j < this.brushSize; j++) {
                    this.currTile.sprite.setPosition(this.touchPos.x - (this.touchPos.x % 16) +offsetX +16*i,
                                                     this.touchPos.y - (this.touchPos.y % 16) +offsetY +16*j);
                    this.currTile.sprite.draw(game.mapBatch);
                    
                    if (this.currTile.overSprite != null) {
                        this.currTile.overSprite.setPosition(this.touchPos.x - (this.touchPos.x % 16) +offsetX +16*i,
                                                             this.touchPos.y - (this.touchPos.y % 16) +offsetY +16*j);
                        this.currTile.overSprite.draw(game.mapBatch);
                    }
                }
            }
            
            
        }
        

        if (Gdx.input.isTouched()) {
//            this.justTouched = true;
            Vector2 position = new Vector2();
            for (int i = 0; i < this.brushSize; i++) {
                for (int j = 0; j < this.brushSize; j++) {
                    position.set(this.currTile.sprite.getX() -16*i, this.currTile.sprite.getY() -16*j);
                    Tile currTile = game.map.tiles.remove(position);
                    Tile newTile = new Tile(this.underTiles.get(this.currIndex), position.cpy(), true, null);
                    if (this.overTiles.contains(this.currTile.name)) {
                        newTile = new Tile(currTile.name, this.underTiles.get(this.currIndex), position.cpy(), true, null);
                    }
                    
                    game.map.tiles.put(newTile.position.cpy(), newTile);
                }
            }
        }
        else {
//            if (this.justTouched) {
//                PlayerStanding.adjustSurroundingTiles(newTile);
//            }
            this.justTouched = false;
        }
        


        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            this.currIndex--;
            if (this.currIndex < 0) {
                this.currIndex = this.underTiles.size()-1;
            }
            this.currTile = new Tile(this.underTiles.get(this.currIndex), new Vector2(0, 0), true, null);
            this.currTile.sprite.setAlpha(0.7f);
            if (this.currTile.overSprite != null) {
                this.currTile.overSprite.setAlpha(0.7f);
            }
        }
        else if (Gdx.input.isKeyJustPressed(Input.Keys.V)) {
            this.currIndex++;
            if (this.currIndex >= this.underTiles.size()) {
                this.currIndex = 0;
            }
            this.currTile = new Tile(this.underTiles.get(this.currIndex), new Vector2(0, 0), true, null);
            this.currTile.sprite.setAlpha(0.7f);
            if (this.currTile.overSprite != null) {
                this.currTile.overSprite.setAlpha(0.7f);
            }
        }
        

        if (Gdx.input.isKeyJustPressed(Input.Keys.CONTROL_LEFT)) {
            if (this.brushSize > 0) {
                this.brushSize--;
            }
        }
        else if (Gdx.input.isKeyJustPressed(Input.Keys.ALT_LEFT)) {
            if (this.brushSize < 10) {
                this.brushSize++;
            }
        }
        
    }
    
}


/**
 * Ideally don't do this.
 */
class PickupItem extends Action {
    HashMap<String, Integer> items;
    String whichItem;
    
    public PickupItem(HashMap<String, Integer> items, String whichItem, Action nextAction) {
        this.items = items;
        this.whichItem = whichItem;
        this.nextAction = nextAction;
    }

    @Override
    public void step(Game game) {
        game.insertAction(new PlaySound("seed1", null));
        HashMap<String, Integer> items = new HashMap<String, Integer>();
        this.items.remove(this.whichItem);
        if (whichItem.equals("torch")) {
            items.put("log", 1);
            items.put("grass", 1);
            game.insertAction(new DrawItemPickup(items, null));
        }
        game.actionStack.remove(this);
        game.insertAction(this.nextAction);
    }
}


/**
 * Call an arbitrary method with params when step() is called. Useful if you don't
 * want to call a method right away in an action chain, but want it to be called
 * at a certain step.
 * 
 * Note: you can't instantiate this on an object that will change down the road.
 * Ex: CallMethod(game.currMusic, ...) if game.currMusic changes, this will still
 * use what it was previously pointing to.
 *
 * @see Tile.onPressA()
 *
 * Example: CallMethod(game.currMusic,        <-- Call method on this object.
 *                     "setVolume",           <-- Call this method.
 *                     new Object[]{"0.1f"},  <-- Call with these parameters
 *                     null)                  <-- (nextAction, inserted immediately after calling the method)
 */
class CallMethod extends Action {
    Object object;
    String method;
    Object[] params;
    Class<?>[] paramTypes;

    /**
     * Use this constructor if you want to pass null to a function (type can't be inferred from null, 
     * so you need to pass the type manually).
     */
    public CallMethod(Object object, String method, Class<?>[] paramTypes, Object[] params, Action nextAction) {
        this.object = object;
        this.method = method;
        this.params = params;
        this.paramTypes = paramTypes;
        this.nextAction = nextAction;
    }

    public CallMethod(Object object, String method, Object[] params, Action nextAction) {
        this.object = object;
        this.method = method;
        this.params = params;
        this.nextAction = nextAction;
        this.paramTypes = new Class[params.length];
        for (int i = 0; i < params.length; i++) {
            this.paramTypes[i] = params[i].getClass();
            // For some reason generic types need to be converted.
            // Can't figure out how to do this generically.
            if (this.paramTypes[i].getName().equals("java.lang.Float")) {
                this.paramTypes[i] = float.class;  // not sure how to do generically.
            }
            else if (this.paramTypes[i].getName().equals("java.lang.Integer")) {
                this.paramTypes[i] = int.class;
            }
            else if (this.paramTypes[i].getName().equals("java.lang.Boolean")) {
                this.paramTypes[i] = boolean.class;
            }
            else if (this.paramTypes[i].getName().equals("java.lang.Long")) {
                this.paramTypes[i] = long.class;
            }
            else if (this.paramTypes[i].getName().equals("java.lang.Double")) {
                this.paramTypes[i] = double.class;
            }
        }
    }

    @Override
    public void step(Game game) {
        try {
            Method method = this.object.getClass().getMethod(this.method, this.paramTypes);
            method.invoke(this.object, this.params);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        game.actionStack.remove(this);
        game.insertAction(this.nextAction);
    }
}

/**
 * Requires two actions, this will add both to the action stack.
 */
class SplitAction extends Action {
    private Action nextAction2;

    String camera = "map";

    public SplitAction(Action nextAction1, Action nextAction2) {
        // put two new actions in the actionStack
        this.nextAction2 = nextAction1;
        this.nextAction = nextAction2;
    }
    public String getCamera() {return camera;}

    // TODO: shouldn't this have layer == LARGE_NUMBER?
    public int getLayer(){
        return 500;
    }

    @Override
    public void step(Game game) {
        game.actionStack.remove(this);
        game.insertAction(nextAction2);
        game.insertAction(nextAction);
    }
}

/**
 * Wait a number of frames before performing the next Action.
 */
class WaitFrames extends Action {
    int length;
    public int layer = 110;

    public WaitFrames(Game game, int length, Action nextAction) {
        this.nextAction = nextAction;
        this.length = length;
    }

    public String getCamera() {return "gui";}

    public int getLayer() {return this.layer;}

    @Override
    public void step(Game game) {
        this.length--;
        if (this.length <= 0) {
            game.insertAction(this.nextAction);
            game.actionStack.remove(this);
        }
    }
}

class RemoveAction extends Action {
    Action action;

    public RemoveAction(Action action, Action nextAction) {
        this.nextAction = nextAction;
        this.action = action;
    }

    @Override
    public void step(Game game) {
        game.actionStack.remove(this);
        game.actionStack.remove(this.action);
        game.insertAction(this.nextAction);
    }
}

