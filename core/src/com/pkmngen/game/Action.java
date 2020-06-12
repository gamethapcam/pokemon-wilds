package com.pkmngen.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

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
    public void step(Game game) {}
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
    boolean firstStep;

    public DisplayText(Game game, String textString, String playSound, Action triggerAction, Action nextAction) {
        this.nextAction = nextAction;

        this.firstStep = true;

        // Set end trigger action
        this.triggerAction = triggerAction;
        this.foundTrigger = false;
        this.checkTrigger = false;

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
                       Action nextAction) {
        this(game, textString, playSound, null, nextAction);
        this.persist = textPersist;
        this.waitInput = waitInput;
    }

    public String getCamera() {return "gui";}

    public int getLayer() {return this.layer;}

    // what to do at each iteration
    public void step(Game game) {
        if (this.firstStep == true) {
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
        if (spritesBeingDrawn.size() >= 36 || spritesNotDrawn.isEmpty()) { // 18 characters per line allowed

            // if at the end of text and need to play sound, do that
            if (this.playSound == true && spritesNotDrawn.isEmpty()) {
                game.insertAction(this.playSoundAction);
                this.playSoundAction.step(game); // avoid latency
                this.playSound = false;
                return;
            }

            // if we need to wait on a trigger
            if (this.triggerAction != null || (DisplayText.textPersist && !this.waitInput)) {
                game.insertAction(this.nextAction);
                this.checkTrigger = true;
                return;
            }

            // draw arrow
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

            if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
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
                    this.scrollUpAction = new DisplayText.ScrollTextUp(game, this.spritesBeingDrawn, this.spritesNotDrawn);
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
        if (Gdx.input.isKeyPressed(Input.Keys.Z) || Gdx.input.isKeyPressed(Input.Keys.X)) {
            // if would take too many, stop
            for (int i=0; i < 3 && !spritesNotDrawn.isEmpty(); i++) {
                spritesBeingDrawn.add(spritesNotDrawn.get(0));
                spritesNotDrawn.remove(0);
            }
        }
        else {
            spritesBeingDrawn.add(spritesNotDrawn.get(0));
            spritesNotDrawn.remove(0);
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

        public int layer = 110;

        public ScrollTextUp(Game game, ArrayList<Sprite> text, ArrayList<Sprite> otherText) {
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

        public int getLayer() {return this.layer;}

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

            // if done, remove first 18 elements
            // frees up DisplayText's text array, which will get filled with new sprites
            if (this.positions.isEmpty()) {
                for (int i=0; i<18; i++) {
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
        if (this.checkTrigger == true) {
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
//            if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
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

        // if would take too many, stop
        for (int i=0; i < 1 && !spritesNotDrawn.isEmpty() && spritesBeingDrawn.size() < 36; i++) {
            spritesBeingDrawn.add(spritesNotDrawn.get(0));
            spritesNotDrawn.remove(0);
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
                for (int i=0; i<18; i++) {
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
 * Displayed at the beginning of the game so that the player can specify setup options.
 */
class DrawSetupMenu extends Action {
    public static boolean drawChoosePokemonText = true;
    public static int avatarAnimCounter = 24;
    public static int currIndex = 0;  // Currently selected menu item

    public static int lastIndex = 0;
    public int layer = 107;
    Sprite bgSprite;
    Sprite helperSprite;
    Sprite arrow;
    Sprite arrowFlipped;
    Sprite arrowWhite;
    ArrayList<Sprite> avatarSprites = new ArrayList<Sprite>();
    int avatarColorIndex = 0;
    Vector2 newPos;
    Map<Integer, Vector2> arrowCoords;
    ArrayList<Character> name = new ArrayList<Character>();
    ArrayList<Character> serverIp = new ArrayList<Character>();

    HashMap<Integer, Character> alphanumericKeys = new HashMap<Integer, Character>();
    HashMap<Integer, Character> alphanumericKeysShift = new HashMap<Integer, Character>();

    HashMap<Integer, Character> numberKeys = new HashMap<Integer, Character>();
    ArrayList<Color> colors = new ArrayList<Color>();
    public DrawSetupMenu(Game game, Action nextAction) {
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
        for (int i=0; i < 3; i++) {
            this.arrowCoords.put(i, new Vector2(1, 128 - 16*i));
        }
        for (int i=3; i < 4; i++) {
            this.arrowCoords.put(i, new Vector2(1, 128 -16 -16*i));
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
        }
        this.numberKeys.put(Input.Keys.PERIOD, '.');

        this.colors.add(new Color(0.9137255f, 0.5294118f, 0.1764706f, 1f));  // Original texture color.
        this.colors.add(Color.WHITE);
        this.colors.add(Color.BLACK);
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
    }
    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        this.bgSprite.draw(game.uiBatch);

        for (int j=0; j < 4; j++) {
            // Name: _ (enter name)
            if (j == 0) {
                char[] textArray = "Name".toCharArray();
                Sprite letterSprite;
                for (int i=0; i < textArray.length; i++) {
                    letterSprite = game.textDict.get(textArray[i]);
                    game.uiBatch.draw(letterSprite, 16 +8*i, 128 -16*j);
                }

                // Let the player type their name in via keyboard
                if (j == DrawSetupMenu.currIndex) {
                    if (this.name.size() < 11) {
                        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
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
                        game.uiBatch.draw(letterSprite, 16 +5*8 + 8*this.name.size(), 128 -16*j);
                    }
                }

                for (int i=0; i < this.name.size(); i++) {
                    letterSprite = game.textDict.get(this.name.get(i));
                    game.uiBatch.draw(letterSprite, 16 +5*8 +8*i, 128 -16*j);
                }

            }

            // < (character sprite) >
            if (j == 1) {
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

                    if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
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
                    if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
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

            // Server IP address (or "Local") _ (enter ip)
            if (j == 2) {
                char[] textArray = "Server IP".toCharArray();
                Sprite letterSprite;
                for (int i=0; i < textArray.length; i++) {
                    letterSprite = game.textDict.get(textArray[i]);
                    game.uiBatch.draw(letterSprite, 16 +8*i, 128 -16*j);
                }
                for (int i=0; i < this.serverIp.size(); i++) {
                    letterSprite = game.textDict.get(this.serverIp.get(i));
                    game.uiBatch.draw(letterSprite, 16 +8*i, 128 -16*(j+1));
                }

                if (j == DrawSetupMenu.currIndex) {
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
                        game.uiBatch.draw(letterSprite, 16 + 8*this.serverIp.size(), 128 -16 -16*j);
                    }
                }

            }
            // Go!
            if (j == 3) {
                char[] textArray = "Go!".toCharArray();
                Sprite letterSprite;
                for (int i=0; i < textArray.length; i++) {
                    letterSprite = game.textDict.get(textArray[i]);
                    game.uiBatch.draw(letterSprite, 16 +8*i, 128 -16 -16*j);
                }
                if (j == DrawSetupMenu.currIndex) {
                    // Set up the map.
                    if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                        game.actionStack.remove(this);
                        game.start();

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
                        // TODO: enable
//                        if (ipAddr.equals("")) {
//                            try {
//                                game.initServer();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        else {
//                            try {
//                                game.initClient(ipAddr);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }

                        EnterBuilding enterBuilding = new EnterBuilding(game, "", null);
                        enterBuilding.slow = 8;
                        game.insertAction(enterBuilding);
                        for (int i=0; i < 12*enterBuilding.slow; i++) {
                            enterBuilding.step(game);
                        }
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            if (DrawSetupMenu.currIndex > 0) {
                DrawSetupMenu.currIndex -= 1;
                DrawSetupMenu.avatarAnimCounter = 24; // reset to 12 for 1 extra frame of first frame for avatar anim
            }
        }
        else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            if (DrawSetupMenu.currIndex < 3) {
                DrawSetupMenu.currIndex += 1;
                DrawSetupMenu.avatarAnimCounter = 24; // reset to 12 for 1 extra frame of first frame for avatar anim
            }
        }
        newPos = this.arrowCoords.get(DrawSetupMenu.currIndex);
        // Draw the arrow sprite
        this.arrow.setPosition(this.newPos.x, this.newPos.y);
        this.arrow.draw(game.uiBatch);
    }
}

class FadeMusic extends Action {
    String musicName;
    Music music;
    String direction;
    String shouldPause;
    float amt =  -0.05f;
    float maxVol = 1f;
    boolean firstStep = true;
    boolean switchCurrMusic = false;
    Music.OnCompletionListener onCompleteListener;

    public FadeMusic(String musicName, String direction, String shouldPause, float rate, Action nextAction) {
        this.musicName = musicName;

        this.shouldPause = shouldPause;

        this.direction = direction;
        if (direction.equals("out")) {
            this.amt = -rate;
        }
        else {
            this.amt = rate;
        }

        this.nextAction = nextAction;
    }

    public FadeMusic(String musicName, String direction, String shouldPause, float rate, boolean switchCurrMusic, Action nextAction) {
        this.musicName = musicName;

        this.shouldPause = shouldPause;

        this.switchCurrMusic = switchCurrMusic;

        this.direction = direction;
        if (direction.equals("out")) {
            this.amt = -rate;
        }
        else {
            this.amt = rate;
        }

        this.nextAction = nextAction;
    }

    public FadeMusic(String musicName, String direction, String shouldPause, float rate, boolean switchCurrMusic, float maxVol, Action nextAction) {
        this.maxVol = maxVol;

        this.musicName = musicName;

        this.shouldPause = shouldPause;

        this.switchCurrMusic = switchCurrMusic;

        this.direction = direction;
        if (direction.equals("out")) {
            this.amt = -rate;
        }
        else {
            this.amt = rate;
        }

        this.nextAction = nextAction;
    }

    public FadeMusic(String musicName, String direction, String shouldPause, float rate, boolean switchCurrMusic, float maxVol, Music.OnCompletionListener onCompleteListener, Action nextAction) {
        this(musicName, direction, shouldPause, rate, switchCurrMusic, maxVol, nextAction);
        this.onCompleteListener = onCompleteListener;
    }

    public void step(Game game) {
        if (!game.playerCanMove) {
            return;
        }

        if (this.firstStep == true) {
            if (this.direction.equals("in")) {
                if (this.musicName.equals("currMusic")) {
                    game.currMusic.play();
                }
                else {
                    // load music if it isnt loaded already
                    if (!game.loadedMusic.containsKey(this.musicName)) {
                        String extension = ".ogg";
                        // test - wav file for android, to enable looping
//                      if (this.musicName.equals("bg_rumble1")) {
//                          extension = ".mp3";
//                          System.out.println("using mp3 extension");
//                      }

                        Music temp = Gdx.audio.newMusic(Gdx.files.internal("music/"+this.musicName+extension)); // danger1.ogg
//                        temp.setLooping(true);  // TODO: don't think I need this
                        temp.setVolume(0.1f); // will always fade in (for now, have option to fade in fast) //.1f because of 'failed to allocate' bug
                        if (this.onCompleteListener != null) {
                            temp.setOnCompletionListener(this.onCompleteListener);
                        }
                        game.loadedMusic.put(this.musicName, temp);

                    }

//                  System.out.println("loaded music: "+this.musicName);
//                  System.out.println(game.loadedMusic);

                    // this.music.play(); // TODO - remove
                    game.loadedMusic.get(this.musicName).play();
                }

                // TODO - remove
//              // set max volumes
//              if (this.musicName.equals("s_and_c3-2")) {
//                  this.maxVol = .9f;
//              }
            }

            if (this.switchCurrMusic == true) {
//              game.currMusic = this.music; // TODO - remove

                // if currmusic still playing, fade it out
                if (game.currMusic != null) {
                    if (game.currMusic.isPlaying()) {
//                      game.insertAction(new FadeMusic("currMusic", "out", "pause", .05f, null));
                        game.currMusic.pause();
                    }
                }

                game.currMusic = game.loadedMusic.get(this.musicName);
                game.map.currRoute.music = game.currMusic; // TODO: shouldn't be doing this probably
            }

            // set this.music for reference
            if (this.musicName.equals("currMusic")) {
                this.music = game.currMusic;
            }
            else {
                this.music = game.loadedMusic.get(this.musicName);
            }

            // shouldn't ever be the case, occasionally is in testing tho.
            if (this.music == null) {
                game.actionStack.remove(this);
                game.insertAction(this.nextAction);
                return;
            }

            this.firstStep = false;
        }

        this.music.setVolume(this.music.getVolume()+this.amt);

        if (this.direction.equals("out")) {
            if (music.getVolume() <= 0.1f) { // bug - for some reason this can't be .0f - no idea why. 'failed to allocate' issue if play() (on any Music) ever called in future

                if (this.shouldPause.equals("pause")) {
                    this.music.setVolume(0.1f);
                    this.music.pause();
                }
                else if (this.shouldPause.equals("stop")) {
//                  this.music.setPosition(0); // TODO - remove
//                  this.music.pause();

                    // dispose and remove from loadedMusic
                    this.music.setVolume(0.1f);
                    this.music.stop();
                    this.music.dispose();
                    game.loadedMusic.remove(this.musicName);

                }
                game.actionStack.remove(this);
                game.insertAction(this.nextAction);
            }
        }
        else {
            if (music.getVolume() >= this.maxVol) {
                this.music.setVolume(this.maxVol);
                game.actionStack.remove(this);
                game.insertAction(this.nextAction);
            }
        }

    }
}

/**
 * TODO: migrate away from this.
 * This has the extra 'disabled' variable.
 */
class MenuAction extends Action {
    boolean disabled;
    boolean drawArrowWhite; // for DrawPlayerMenu

    MenuAction prevMenu;
    int currIndex;
}

/**
 * Plays a Sound and then calls sound.dispose().
 */
class PlaySound extends Action {
    Music music;
    String sound;

    boolean playedYet; // do music.play on first step

    // for playing pokemon cry
    public PlaySound(Pokemon pokemon, Action nextAction) {
        this.nextAction = nextAction;
        this.playedYet = false;
        this.sound = pokemon.name;
        this.music = null;

        // if it's crystal pokemon, load from crystal dir
        if (pokemon.generation == Pokemon.Generation.CRYSTAL) {
            if (pokemon.dexNumber.equals("000")) {
                this.music = Gdx.audio.newMusic(Gdx.files.internal("crystal_pokemon/cries/" + pokemon.dexNumber + ".ogg"));
                this.music.setVolume(0.9f);
            }
            else {
                this.music = Gdx.audio.newMusic(Gdx.files.internal("crystal_pokemon/cries/" + pokemon.dexNumber + ".wav"));
                this.music.setVolume(0.5f);
            }
            this.music.setLooping(false);
        }
    }

    public PlaySound(String sound, Action nextAction) {
        this.nextAction = nextAction;
        this.playedYet = false;
        this.sound = sound;

        this.music = null;

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
        else if (sound == "Mewtwo") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/150Cry.ogg"));
            this.music.setLooping(false);
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
            this.music.dispose();
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
