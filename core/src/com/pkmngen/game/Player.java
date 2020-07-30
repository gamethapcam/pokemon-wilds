package com.pkmngen.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.DrawPokemonMenu.SelectedMenu;
import com.pkmngen.game.DrawPokemonMenu.SelectedMenu.Switch;
import com.pkmngen.game.util.LoadingZone;

/*
 * TODO: remove this, not using.
 */
class AnimationContainer<E> {
    ArrayList<E> animateThese;
    ArrayList<Integer> numFrames;
    int index;

    public AnimationContainer() {
        this.animateThese = new ArrayList<E>();
        this.numFrames = new ArrayList<Integer>();
        this.index = 0;
    }

    public void add(E thing, int numFrames) {
        animateThese.add(thing);
        this.numFrames.add(numFrames);
    }

    public int currentFrame() {
        return this.numFrames.get(this.index);
    }

    public E currentThing() {
        return this.animateThese.get(this.index);
    }
}

/**
 * Animation of player cutting tree in overworld.
 */
class CutTreeAnim extends Action {
    int index = 0;
    Tile tile;
    Sprite left, right;

    public CutTreeAnim(Game game, Tile tile, Action nextAction) {
        this.tile = tile;
        this.nextAction = nextAction;
    }

    @Override
    public void step(Game game) {
        if (this.index < 13) {
        }
        else if (this.index == 13) {
            game.insertAction(new PlaySound("cut1", null));
        }
        else if (this.index < 19) {
        }
        else if (this.index == 19) {
            // TODO: map tiles to anim tiles
            // TODO: somehow handle at tile-level?
            game.map.tiles.remove(this.tile.position);
//            if (this.tile.name.equals("bush1")) {
////                game.map.tiles.put(this.tile.position.cpy(), new Tile("bush2", this.tile.position.cpy()));
//                this.tile = new Tile("bush2", this.tile.position.cpy());
//            }
            String name = this.tile.name;
            if (name.equals("grass2")) {
                name = "green1";
            }
            else if (name.equals("grass3")) {
                name = "snow1";
            }
            else if (name.equals("grass_sand1")) {
                name = "sand1";
            }
            game.map.tiles.put(this.tile.position.cpy(),
                               new Tile(name, this.tile.position.cpy(),
                                        true, this.tile.routeBelongsTo));

            game.mapBatch.draw(this.tile.sprite, this.tile.sprite.getX(), this.tile.sprite.getY());
            game.mapBatch.draw(this.tile.overSprite, this.tile.overSprite.getX(), this.tile.overSprite.getY());
        }
        else if (this.index == 20) {
            // slice overSprite down middle, sep by 4 px
            Sprite temp = new Sprite(this.tile.overSprite);
            TextureRegion[][] tempRegion = temp.split((int)this.tile.overSprite.getWidth()/2, (int)this.tile.overSprite.getHeight());
            this.left = new Sprite(tempRegion[0][0]);
            this.left.setPosition(this.tile.position.x-2-4+4, this.tile.position.y);
            this.right = new Sprite(tempRegion[0][1]);
            this.right.setPosition(this.tile.position.x+2+4+4, this.tile.position.y);
            game.mapBatch.draw(this.left, this.left.getX(), this.left.getY());
            game.mapBatch.draw(this.right, this.right.getX(), this.right.getY());
        }
        else if (this.index < 20+17) {
            game.mapBatch.draw(this.left, this.left.getX(), this.left.getY());
            game.mapBatch.draw(this.right, this.right.getX(), this.right.getY());
        }
        else if (this.index < 20+17+2) {
        }
        else if (this.index == 20+17+2) {
            this.left.translateX(-2);
            this.right.translateX(2);
            game.mapBatch.draw(this.left, this.left.getX(), this.left.getY());
            game.mapBatch.draw(this.right, this.right.getX(), this.right.getY());
        }
        else if (this.index < 20+17+2+2) {
            game.mapBatch.draw(this.left, this.left.getX(), this.left.getY());
            game.mapBatch.draw(this.right, this.right.getX(), this.right.getY());
        }
        else if (this.index < 20+17+2+2+2) {
        }
        else if (this.index == 20+17+2+2+2) {
            this.left.translateX(-2);
            this.right.translateX(2);
            game.mapBatch.draw(this.left, this.left.getX(), this.left.getY());
            game.mapBatch.draw(this.right, this.right.getX(), this.right.getY());
        }
        else if (this.index < 20+17+2+2+2+2) {
            game.mapBatch.draw(this.left, this.left.getX(), this.left.getY());
            game.mapBatch.draw(this.right, this.right.getX(), this.right.getY());
        }
        else if (this.index < 20+17+2+2+2+2+2) {
        }
        else if (this.index == 20+17+2+2+2+2+2) {
            this.left.translateX(-2);
            this.right.translateX(2);
            game.mapBatch.draw(this.left, this.left.getX(), this.left.getY());
            game.mapBatch.draw(this.right, this.right.getX(), this.right.getY());
        }
        else if (this.index < 20+17+2+2+2+2+2+2) {
        }
        else {
//            game.map.tiles.put(this.tile.position.cpy(), this.tile);
//            game.playerCanMove = true;
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
        }
        this.index++;
    }
}

/**
 * Keeps track of current time of day, handles day/night transition animation.
 * Keeps track of when ghosts should spawn.
 */
class CycleDayNight extends Action {
    public static int dayTimer;
    public int layer = 114;

    int[] currFrames;;

    boolean fadeToDay;

    boolean fadeToNight;
    int animIndex;

    AnimationContainer<Color> animContainer;

    AnimationContainer<Color> fadeToDayAnim;
    Random rand;

    int countDownToGhost;
    Sprite bgSprite;
    String text;

    int signCounter;
    int day, night; // number of days/nights that has passed
    
    Vector2 startPos;
    Vector2 endPos;

    public CycleDayNight(Game game) {
        this.day = 1;
        this.night = 0;

        this.animContainer = new AnimationContainer<Color>();
        animContainer.add(new Color(0.8f, 0.8f, 0.8f, 1.0f), 80);
        animContainer.add(new Color(0.5f, 0.5f, 0.6f, 1.0f), 80);
        animContainer.add(new Color(0.2f, 0.2f, 0.4f, 1.0f), 80);
        animContainer.add(new Color(0.08f, 0.08f, 0.3f, 1.0f), 80);
        // this was too dark for color overworld
        // maybe use this in forest biomes, require player to have 'flash' pokemon (charmander walking, etc?)
//        animContainer.add(new Color(0.01f, 0.01f, 0.2f, 1.0f), 80);

        this.fadeToDayAnim = new AnimationContainer<Color>();
//        fadeToDayAnim.add(new Color(0.01f, 0.01f, 0.2f, 1.0f), 80);
        fadeToDayAnim.add(new Color(0.08f, 0.08f, 0.3f, 1.0f), 80);
        fadeToDayAnim.add(new Color(0.2f, 0.2f, 0.4f, 1.0f), 80);
        fadeToDayAnim.add(new Color(0.8f, 0.8f, 0.8f, 1.0f), 80);
        fadeToDayAnim.add(Color.WHITE, 80);

        this.fadeToDay = false;
        this.fadeToNight = false;
        this.animIndex = 0;

        this.rand = new Random();
        CycleDayNight.dayTimer = 18000; // 100; //- debug // 10000;

        Texture text = new Texture(Gdx.files.internal("text2.png"));
        this.bgSprite = new Sprite(text, 0, 0, 160, 144);
        this.bgSprite.setPosition(0,24);

        this.text = game.map.timeOfDay+": ";
    }
    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        // TODO: remove this (required by network play)
        // TODO: test
//        if (game.playerCanMove) {
//            dayTimer--;
//        }
        CycleDayNight.dayTimer--;

        if (CycleDayNight.dayTimer <= 0) {
            if (game.map.timeOfDay.equals("Day")) {
                this.fadeToNight = true;
                CycleDayNight.dayTimer = 5000; // debug  // was 10000
            }
            else if (game.map.timeOfDay.equals("Night")) {
                this.fadeToDay = true;
                CycleDayNight.dayTimer = 18000; // 10000 // 1000 - debug
            }
        }

        if (this.fadeToDay) {
            game.mapBatch.setColor(this.fadeToDayAnim.currentThing());
            animIndex++;

            if (animIndex >= this.fadeToDayAnim.currentFrame()) {
                this.fadeToDayAnim.index++;
                animIndex = 0;
            }

            if (this.fadeToDayAnim.index >= this.fadeToDayAnim.animateThese.size()) {
                fadeToDay = false;
                game.map.timeOfDay = "Day";
                this.fadeToDayAnim.index = 0;

//                // TODO - fade day music
//                game.currMusic.pause();
//                // start night music
//                Music music = Gdx.audio.newMusic(Gdx.files.internal("route1_1.ogg"));
//                music.setLooping(true);
//                music.setVolume(.7f);
//                game.currMusic = music;
//                game.map.currRoute.music = music; // TODO - how to switch to normal after defeating
//                game.currMusic.play();

                if (game.type != Game.Type.SERVER) {
                    String nextMusicName = game.map.currRoute.getNextMusic(true);
                    BattleFadeOutMusic.playerFainted = true;  // TODO: this is just a hack around issues with FadeMusic
                    Action nextMusic = new BattleFadeOutMusic(game,
                                       new WaitFrames(game, 360,
                                       new FadeMusic(nextMusicName, "in", "", 0.2f, true, 1f, game.musicCompletionListener,
                                       null)));
                    game.fadeMusicAction = nextMusic;
                    game.insertAction(nextMusic);
                }

                // state which day it is
                day++;
                signCounter = 300;
                this.bgSprite.setPosition(0, 24);

                // All planted trees become full size trees
                if (game.type != Game.Type.CLIENT) {
                    for (Tile tile : game.map.overworldTiles.values()) {
                        if (tile.nameUpper.equals("tree_planted")) {
                            game.map.overworldTiles.put(tile.position, new Tile("bush1", "", tile.position.cpy(), true, tile.routeBelongsTo));
                        }
                    }
                }
            }
        }

        if (this.fadeToNight) {
            game.mapBatch.setColor(this.animContainer.currentThing());
            animIndex++;

            if (animIndex >= this.animContainer.currentFrame()) {
                this.animContainer.index++;
                animIndex = 0;
            }

            if (this.animContainer.index >= this.animContainer.animateThese.size()) {
                fadeToNight = false;
                game.map.timeOfDay = "Night";
                this.countDownToGhost = 150; // this.rand.nextInt(5000) + 150;  // debug: 150;
                this.animContainer.index = 0;

                // TODO test
//                game.currMusic.pause();
                if (game.type != Game.Type.SERVER) {
                    game.currMusic.stop();
                    game.actionStack.remove(game.fadeMusicAction);
                    // start night music
                    if (!game.loadedMusic.containsKey("night1")) {
                        game.loadedMusic.put("night1", Gdx.audio.newMusic(Gdx.files.internal("night1.ogg")));
                    }
                    Music music = game.loadedMusic.get("night1");
                    music.setLooping(true);
                    music.setVolume(.7f);
                    game.currMusic = music;
                    game.map.currRoute.music = music;
                    game.currMusic.play();
                }
                // state which night it is
                night++;
                signCounter = 150;
                this.bgSprite.setPosition(0,24);
            }
        }

        // Check player can move so don't spawn in middle of battle or when looking at ghost
        // If player is near a campfire, don't deduct from ghost spawn timer
        if (game.map.timeOfDay.equals("Night") && game.playerCanMove == true && !game.player.isNearCampfire && game.map.currBiome.equals("deep_forest")) {
            countDownToGhost--;
            if (game.player.currState != "Running") {
                countDownToGhost--;
            }

            if (countDownToGhost <= 0) {
                Vector2 randPos = game.player.position.cpy().add(this.rand.nextInt(5)*16 - 48, this.rand.nextInt(5)*16 - 48);
                game.insertAction(new SpawnGhost(game, new Vector2(randPos)) );
                // TODO: mess with this.
//                this.countDownToGhost = this.rand.nextInt(4000) + 1000; // debug: 1000;
                this.countDownToGhost = this.rand.nextInt(2000) + 1000; // debug: 1000;
                
            }
        }

        // TODO: move to unused code
//        if (signCounter > 0) {
//            signCounter--;
//
//            if (signCounter > 100) {
//            }
//            else if (signCounter > 78) {
//                this.bgSprite.setPosition(this.bgSprite.getX(), this.bgSprite.getY()-1);
//            }
//            else if (signCounter > 22) {
//            }
//            else {
//                this.bgSprite.setPosition(this.bgSprite.getX(), this.bgSprite.getY()+1);
//            }
//
//            this.bgSprite.draw(game.uiBatch);
//            String temp="";
//            if (game.map.timeOfDay == "Day") {
//                    temp = String.valueOf(this.day);
//            }
//            else {
//                    temp = String.valueOf(this.night);
//            }
//            game.font.draw(game.uiBatch, game.map.timeOfDay+": "+temp, 60, this.bgSprite.getY()+134); // Gdx.graphics.getHeight()-
//        }

    }
}

class DespawnGhost extends Action {
    public int layer = 114;
    Sprite[] sprites;
    int part1;
    int part2;
    int part3;
    Vector2 position;
    Sprite sprite;

    public DespawnGhost(Vector2 position) {
        this.part1 = 80;

        this.position = position;

        Texture ghostTexture1 = new Texture(Gdx.files.internal("ghost_spawn1.png"));
        this.sprite = new Sprite(ghostTexture1, 0, 0, 40, 40);
        this.sprite.setPosition(position.x-4, position.y-4);
    }

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        if (part1 > 0) {
            if (part1 % 4 >=2) {
                this.sprite.draw(game.mapBatch);
            }

            part1--;
            return;
        }

        game.actionStack.remove(this);
    }
}

/**
 * Draws the requirements for building an object if game.player.isBuilding.
 */
class DrawBuildRequirements extends Action {
    Sprite textBoxTop;
    Sprite textBoxMiddle;
    Sprite textBoxBottom;
    ArrayList<String> words = new ArrayList<String>(); // menu items
    ArrayList<Color> wordColors = new ArrayList<Color>(); // menu items
    Vector2 topLeft = new Vector2(89, 144);
    String currBuilding = "";

    public DrawBuildRequirements() {
        // text box background
        Texture text = new Texture(Gdx.files.internal("pokemon_menu/selected_menu_top.png"));
        this.textBoxTop = new Sprite(text, 0,0, 71, 19);
        text = new Texture(Gdx.files.internal("pokemon_menu/selected_menu_middle.png"));
        this.textBoxMiddle = new Sprite(text, 0,0, 71, 16);
        text = new Texture(Gdx.files.internal("pokemon_menu/selected_menu_bottom.png"));
        this.textBoxBottom = new Sprite(text, 0,0, 71, 19);
    }

    public String getCamera() {return "gui";}

    @Override
    public void firstStep(Game game) {}

    @Override
    public void step(Game game) {
        // If player isn't building, don't draw anything
        if (!game.player.isBuilding) {
            return;
        }
        if (game.player.isBuilding) {
            if (!game.playerCanMove) {
                return;
            }
            String curr = game.player.buildTiles.get(game.player.buildTileIndex).name;
            if (curr != null && !this.currBuilding.equals(curr)) {
                this.currBuilding = curr;
                this.words.clear();
                this.wordColors.clear();
                this.words.add("Need");
                this.wordColors.add(new Color(1,1,1,1));
                for (String req : game.player.buildTileRequirements.get(curr).keySet()) {
                    String text = req.toUpperCase();
                    int numNeeded = game.player.buildTileRequirements.get(curr).get(req);
                    for (int i=0; i < 5-req.length(); i++) {
                        text += " ";
                    }
                    text += "x";
                    text += String.valueOf(numNeeded);
                    if (game.player.itemsDict.get(req) == null || game.player.itemsDict.get(req) < numNeeded) {
                        this.wordColors.add(new Color(1, 1, 1, 0.5f));
                    }
                    else {
                        this.wordColors.add(new Color(1,1,1,1));
                    }
                    this.words.add(text);
                }
            }
        }
        // TODO: remove if unused
//        else if (game.player.isCrafting) {
//            String curr = Player.crafts.get(DrawCraftsMenu.lastCurrIndex).name;
//            if (curr != null && !this.currBuilding.equals(curr)) {
//                this.currBuilding = curr;
//                this.words.clear();
//                this.words.add("Need");
//                this.wordColors.add(new Color(1,1,1,1));
//                for (Player.Craft craft : Player.crafts.get(DrawCraftsMenu.lastCurrIndex).requirements) {
//                    String text = craft.name.toUpperCase();
//                    int numNeeded = craft.amount;
//                    for (int i=0; i < 5-craft.name.length(); i++) {
//                        text += " ";
//                    }
//                    text += "x";
//                    text += String.valueOf(numNeeded);
//                    if (game.player.itemsDict.get(craft.name) == null || game.player.itemsDict.get(craft.name) < numNeeded) {
//                        this.wordColors.add(new Color(1, 1, 1, 0.5f));
//                    }
//                    else {
//                        this.wordColors.add(new Color(1,1,1,1));
//                    }
//                    this.words.add(text);
//                }
//            }
//        }
        // draw requirements of current item being built
        Sprite letterSprite;
        for (int i=0; i < this.words.size(); i++) {
            // Draw appropriate part of textBox
            if (i == 0) {
                game.uiBatch.draw(this.textBoxTop, this.topLeft.x, this.topLeft.y-19);
            }
            else if (i == this.words.size()-1) {
                game.uiBatch.draw(this.textBoxBottom, this.topLeft.x, this.topLeft.y-38 -16*(this.words.size()-2));
            }
            else {
                game.uiBatch.draw(this.textBoxMiddle, this.topLeft.x, this.topLeft.y-19 -16*(this.words.size()-3+i));
            }

            String word = this.words.get(i);
            for (int j=0; j < word.length(); j++) {
                char letter = word.charAt(j);
                // convert string to text
                letterSprite = new Sprite(game.textDict.get(letter));
//                game.uiBatch.draw(letterSprite, this.topLeft.x +8 +8*j, this.topLeft.y -16*(this.words.size()-2+i));
                letterSprite.setPosition(this.topLeft.x +8 +8*j, this.topLeft.y -16*(this.words.size()-2+i));
                letterSprite.setColor(this.wordColors.get(i));
                letterSprite.draw(game.uiBatch);
            }
        }
    }
}

/**
 * Draw item menu, used in overworld and battle.
 */
class DrawCraftsMenu extends MenuAction {
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
    ArrayList<String> craftsList;
    Sprite downArrow;
    int downArrowTimer;
    Sprite craftReqsTextbox;
    Sprite titleTextbox;
    String currCraft = "";
    ArrayList<String> craftReqs = new ArrayList<String>();
    ArrayList<Color> craftReqColors = new ArrayList<Color>();
    Vector2 topLeft = new Vector2(0, 26);

    public DrawCraftsMenu(Game game, Action nextAction) {
        this(game, null);
        this.nextAction = nextAction;
    }

    public DrawCraftsMenu(Game game, MenuAction prevMenu) {
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

        // craft requirements text box bg
        text = new Texture(Gdx.files.internal("textbox_bg1.png"));
        this.craftReqsTextbox = new Sprite(text, 0,0, 16*10, 16*9);
        text = new Texture(Gdx.files.internal("title_bg1.png"));
        this.titleTextbox = new Sprite(text, 0,0, 16*10, 16*9);

        // down arrow for items menu
        text = new Texture(Gdx.files.internal("arrow_down.png"));
        this.downArrow = new Sprite(text, 0, 0, 7, 5);
        this.downArrow.setPosition(144, 50);
        this.downArrowTimer = 0;

        this.currIndex = DrawCraftsMenu.lastCurrIndex;
        this.cursorPos = DrawCraftsMenu.lastCursorPos;

        // finite amount of cursor coordinates (4)
        // this.arrowCoords.put(1, new Vector2(89, 72+16+16)); // example
        this.arrowCoords.put(0, new Vector2(41, 104 - 16*0));
        this.arrowCoords.put(1, new Vector2(41, 104 - 16*1));
        this.arrowCoords.put(2, new Vector2(41, 104 - 16*2));
        this.arrowCoords.put(3, new Vector2(41, 104 - 16*3));

        newPos = arrowCoords.get(cursorPos);
        this.arrow.setPosition(newPos.x, newPos.y);

        this.craftsList = new ArrayList<String>();
        for (Player.Craft craft : Player.crafts) {
            this.craftsList.add(craft.name);
        }
        this.craftsList.add("Cancel");
        // convert player item list to sprites
        for (String entry : this.craftsList) {
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

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        if (this.prevMenu != null) {
            this.prevMenu.step(game);
        }

        // draw text box
        this.titleTextbox.draw(game.uiBatch);
        this.craftReqsTextbox.draw(game.uiBatch);
        this.textBox.draw(game.uiBatch);

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
        int index = DrawCraftsMenu.lastCurrIndex+DrawCraftsMenu.lastCursorPos;
        if (index >= Player.crafts.size()) {  // happens when 'cancel' is selected
            this.craftReqs.clear();
            this.craftReqColors.clear();
            this.currCraft = "";
        }
        else {
            String curr = Player.crafts.get(index).name;
            if (curr != null && !this.currCraft.equals(curr)) {
                this.currCraft = curr;
                this.craftReqs.clear();
                this.craftReqColors.clear();
//                this.words.add("Need");
//                this.wordColors.add(new Color(1,1,1,1));
                for (Player.Craft craft : Player.crafts.get(index).requirements) {
                    String text = craft.name.toUpperCase();
                    int numNeeded = craft.amount;
                    for (int i=0; i < 16-craft.name.length(); i++) {
                        text += " ";
                    }
                    text += "x";
                    text += String.valueOf(numNeeded);
                    if (game.player.itemsDict.get(craft.name) == null || game.player.itemsDict.get(craft.name) < numNeeded) {
                        this.craftReqColors.add(new Color(1, 1, 1, 0.5f));
                    }
                    else {
                        this.craftReqColors.add(new Color(1,1,1,1));
                    }
                    this.craftReqs.add(text);
                }
            }
        }
        // draw requirements of current item being crafted
        char[] textArray = "CRAFTING MENU".toCharArray(); // iterate elements
        j=0;
        for (char letter : textArray) {
            Sprite letterSprite = game.textDict.get((char)letter);
            if (letterSprite == null) {
                letterSprite = game.textDict.get(null);
            }
            game.uiBatch.draw(letterSprite, 8 +8*j, 144-16);
            j++;
        }
        Sprite letterSprite;
        for (int i=0; i < this.craftReqs.size(); i++) {
            String word = this.craftReqs.get(i);
            for (int k=0; k < word.length(); k++) {
                char letter = word.charAt(k);
                // convert string to text
                letterSprite = new Sprite(game.textDict.get(letter));
//                game.uiBatch.draw(letterSprite, this.topLeft.x +8 +8*j, this.topLeft.y -16*(this.words.size()-2+i));
                letterSprite.setPosition(this.topLeft.x +8 +8*k, this.topLeft.y -16*i);
                letterSprite.setColor(this.craftReqColors.get(i));
                letterSprite.draw(game.uiBatch);
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
            DrawCraftsMenu.lastCurrIndex = this.currIndex;
            DrawCraftsMenu.lastCursorPos = this.cursorPos;
        }
        else if (InputProcessor.downJustPressed) {
            if (cursorPos < 2 && cursorPos+1 < this.craftsList.size()) {
                cursorPos += 1;
                newPos = arrowCoords.get(cursorPos);
            }
            else if (currIndex < this.craftsList.size() - 3) {
                currIndex += 1;
            }
            DrawCraftsMenu.lastCurrIndex = this.currIndex;
            DrawCraftsMenu.lastCursorPos = this.cursorPos;
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
        if ( (this.craftsList.size() - this.currIndex) > 4 ) {
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

        // if press a, draw craft/cancel for item
        if (InputProcessor.aJustPressed) {
            game.insertAction(new PlaySound("click1", null));
            String name = this.craftsList.get(currIndex + cursorPos);
            if ("Cancel".equals(name)) {
                if (this.prevMenu != null) {
                    this.prevMenu.disabled = false;
                }
                game.insertAction(this.prevMenu);
                game.insertAction(this.nextAction);
                game.actionStack.remove(this);
                return;
            }
            else {
                this.disabled = true;
                // Draw 'select amount' box
                game.insertAction(new DrawCraftsMenu.SelectAmount(this));
                game.actionStack.remove(this);
                return;
            }
        }
        // player presses b, ie wants to go back
        else if (InputProcessor.bJustPressed) {
            DrawCraftsMenu.lastCurrIndex = this.currIndex;
            DrawCraftsMenu.lastCursorPos = this.cursorPos;
            if (this.prevMenu != null) {
                this.prevMenu.disabled = false;
            }
            game.actionStack.remove(this);
            game.insertAction(this.prevMenu);
            game.insertAction(this.nextAction);
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

    /**
     * Draw a box where the user selects the amount of an item to craft.
     */
    static class SelectAmount extends MenuAction {
        public int layer = 106;
        Sprite textbox;
        public static int amount = 0;
        int maxAmount = 1;

        public SelectAmount(MenuAction prevMenu) {
//            this.maxAmount = maxAmount;
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

            if (this.disabled == true) {
                return;
            }

            if (InputProcessor.upJustPressed) {
                int index = DrawCraftsMenu.lastCurrIndex+DrawCraftsMenu.lastCursorPos;
                if (game.player.hasCraftRequirements(index, SelectAmount.amount+1)) {
                    SelectAmount.amount++;
                }
            }
            else if (InputProcessor.downJustPressed) {
                if (SelectAmount.amount > 1) {
                    SelectAmount.amount--;
                }
            }
            else if (InputProcessor.rightJustPressed) {
                int index = DrawCraftsMenu.lastCurrIndex+DrawCraftsMenu.lastCursorPos;
                for (int i = 0; i < 10; i++) {
                    if (game.player.hasCraftRequirements(index, SelectAmount.amount+1)) {
                        SelectAmount.amount++;
                    }
                }
                if (SelectAmount.amount >= 99) {
                    SelectAmount.amount = 99;
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
                this.disabled = true;
                // Draw CRAFT/CANCEL
                game.actionStack.remove(this);
                game.insertAction(new DrawCraftsMenu.Selected(this));
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

    /**
     * Draw a box where the user confirms whether or not to craft an item.
     */
    static class Selected extends MenuAction {
        Sprite arrow;
        Sprite textBoxTop;
        Sprite textBoxMiddle;
        Sprite textBoxBottom;
        public int layer = 106;
        Map<Integer, Vector2> getCoords;
        int curr;
        Vector2 newPos;
        Sprite helperSprite;
        ArrayList<String> words; // menu items
        int textboxDelay = 0; // this is just extra detail. text box has 1 frame delay before appearing

        public Selected(MenuAction prevMenu) {
            this.prevMenu = prevMenu;

            this.getCoords = new HashMap<Integer, Vector2>();
            this.words = new ArrayList<String>();
            this.words.add("CRAFT");
            this.words.add("CANCEL");

            this.getCoords.put(0, new Vector2(97, 40 +16));
            this.getCoords.put(1, new Vector2(97, 40-16 +16));

            Texture text = new Texture(Gdx.files.internal("battle/arrow_right1.png"));
            this.arrow = new Sprite(text, 0, 0, 5, 7);

            // text box background
            text = new Texture(Gdx.files.internal("pokemon_menu/selected_menu_top.png"));
            this.textBoxTop = new Sprite(text, 0,0, 71, 19);
            text = new Texture(Gdx.files.internal("pokemon_menu/selected_menu_middle.png"));
            this.textBoxMiddle = new Sprite(text, 0,0, 71, 16);
            text = new Texture(Gdx.files.internal("pokemon_menu/selected_menu_bottom.png"));
            this.textBoxBottom = new Sprite(text, 0,0, 71, 19);

            // this.newPos =  new Vector2(32, 79); // post scaling change
            this.newPos =  this.getCoords.get(0);
            this.arrow.setPosition(newPos.x, newPos.y);
            this.curr = 0;
            // If you want to customize menu text, add to this.spritesToDraw here
        }

        public String getCamera() {return "gui";}

        public int getLayer(){return this.layer;}

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
                    game.uiBatch.draw(this.textBoxTop, 89, 35 +33 +16*(this.words.size()-3));
                }
                else if (i == this.words.size()-1) {
                    game.uiBatch.draw(this.textBoxBottom, 89, 33);
                }
                else {
                    game.uiBatch.draw(this.textBoxMiddle, 89, 19 +33 +16*(this.words.size()-i-2));
                }

                String word = this.words.get(i);
                for (int j=0; j < word.length(); j++) {
                    char letter = word.charAt(j);
                    // convert string to text
                    letterSprite = game.textDict.get(letter);
                    game.uiBatch.draw(letterSprite, 104 +8*j, 40 +32 -16*(i-this.words.size()+3));
                }
            }

            // draw arrow sprite
            this.arrow.setPosition(newPos.x, newPos.y);
            this.arrow.draw(game.uiBatch);

            if (InputProcessor.aJustPressed) {
                String word = this.words.get(this.curr);
                if ("CANCEL".equals(word)) {
                    game.insertAction(new PlaySound("click1", null));
                    game.actionStack.remove(this);
                    this.prevMenu.disabled = false;
                    game.insertAction(this.prevMenu);
                    return;
                }
                else if ("CRAFT".equals(word)) {
                    int index = DrawCraftsMenu.lastCurrIndex+DrawCraftsMenu.lastCursorPos;
                    if (game.type == Game.Type.CLIENT) {
                        game.client.sendTCP(new com.pkmngen.game.Network.Craft(game.player.network.id,
                                                                               index,
                                                                               SelectAmount.amount));
                    }
                    // Check craft requirements
                    if (game.player.hasCraftRequirements(index, SelectAmount.amount)) {
                        // Craft the item if the item requirements are met
                        game.player.craftItem(index, SelectAmount.amount);
                        // Display 'you crafted' text
                        game.actionStack.remove(this);
                        game.insertAction(this.prevMenu.prevMenu);
                        String plural = "";
                        if (SelectAmount.amount > 1) {
                            plural += "S";
                        }
                        game.insertAction(new DisplayText(game, "Crafted "+String.valueOf(SelectAmount.amount)+" "+Player.crafts.get(index).name.toUpperCase()+plural+"!",
                                                          "fanfare1.ogg", true, true,
                                          new DisplayText.Clear(game,
                                          new SetField(this.prevMenu.prevMenu, "disabled", false,
                                          null))));
                    }
                }
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
 * Draws the ghost that chases the player.
 */
class DrawGhost extends Action {
    public int layer = 114;
    Sprite currSprite;
    Vector2 basePos;  // need this to make ghost 'float'
    float velX;
    float velY;
    float maxVel;
    float sineTimer;
    String dirFacing;
    Map<String, Sprite[]> sprites;
    int animIndex;
    int animFrame;
    Pokemon pokemon;
    boolean inBattle = false;
    int noEncounterTimer = 0;
    Vector2 startPos;
    Vector2 endPos;

    public DrawGhost(Game game, Vector2 position) {
        this.basePos = position;
        this.sineTimer = 0;
        this.dirFacing = "down";
        this.sprites = new HashMap<String, Sprite[]>();
        this.animIndex = 0;
        this.animFrame = 0;
        this.maxVel = 1.2f; // can scale to make ghost harder

        // need to store which pokemon this actually will be (i think)
//        this.pokemon = new Pokemon("Sableye", 21);
//        this.pokemon = new Pokemon("Sableye", 21, Pokemon.Generation.CRYSTAL);
        this.pokemon = new Pokemon("Ghost", 21, Pokemon.Generation.CRYSTAL);

        Texture ghostTexture = new Texture(Gdx.files.internal("ghost_sheet1.png"));
        this.sprites.put("down", new Sprite[]{
                                    new Sprite(ghostTexture, 32*0, 0, 32, 32),
                                    new Sprite(ghostTexture, 32*1, 0, 32, 32),
                                    new Sprite(ghostTexture, 32*0, 0, 32, 32),
                                    new Sprite(ghostTexture, 32*1, 0, 32, 32),
                                    new Sprite(ghostTexture, 32*0, 0, 32, 32),
                                    new Sprite(ghostTexture, 32*1, 0, 32, 32),
                                    new Sprite(ghostTexture, 32*2, 0, 32, 32),
                                    new Sprite(ghostTexture, 32*3, 0, 32, 32)} );

        this.sprites.put("left", new Sprite[]{new Sprite(ghostTexture, 32*4, 0, 32, 32),
                                                new Sprite(ghostTexture, 32*5, 0, 32, 32),
                                                new Sprite(ghostTexture, 32*4, 0, 32, 32),
                                                new Sprite(ghostTexture, 32*5, 0, 32, 32),
                                                new Sprite(ghostTexture, 32*4, 0, 32, 32),
                                                new Sprite(ghostTexture, 32*5, 0, 32, 32),
                                                new Sprite(ghostTexture, 32*6, 0, 32, 32),
                                                new Sprite(ghostTexture, 32*7, 0, 32, 32)} );

        this.sprites.put("up", new Sprite[]{ new Sprite(ghostTexture, 32*8, 0, 32, 32),
                                                new Sprite(ghostTexture, 32*9, 0, 32, 32),
                                                new Sprite(ghostTexture, 32*8, 0, 32, 32),
                                                new Sprite(ghostTexture, 32*9, 0, 32, 32),
                                                new Sprite(ghostTexture, 32*8, 0, 32, 32),
                                                new Sprite(ghostTexture, 32*9, 0, 32, 32),
                                                new Sprite(ghostTexture, 32*10, 0, 32, 32),
                                                new Sprite(ghostTexture, 32*11, 0, 30, 32)} );
        Sprite temp = new Sprite(ghostTexture, 32*4, 0, 32, 32);
        temp.flip(true, false);
        Sprite temp2 = new Sprite(ghostTexture, 32*5, 0, 32, 32);
        temp2.flip(true, false);
        Sprite temp3 = new Sprite(ghostTexture, 32*6, 0, 32, 32);
        temp3.flip(true, false);
        Sprite temp4 = new Sprite(ghostTexture, 32*7, 0, 32, 32);
        temp4.flip(true, false);
        this.sprites.put("right", new Sprite[]{temp, temp2, temp, temp2, temp, temp2, temp3, temp4});
        this.currSprite = this.sprites.get(this.dirFacing)[this.animIndex];
        this.currSprite.setPosition(position.x, position.y);
    }

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        // check if it's day or not. if not Night, despawn the ghost
        if (game.map.timeOfDay != "Night") {
            this.currSprite.draw(game.mapBatch);
            game.actionStack.remove(this);
            game.insertAction(new DespawnGhost(this.basePos.cpy()));
            return;
        }

        // if too near to a fire, despawn
        this.startPos = this.basePos.cpy().add(-64, -64);
        this.startPos.x = (int)this.startPos.x - (int)this.startPos.x % 16;
        this.startPos.y = (int)this.startPos.y - (int)this.startPos.y % 16;
        this.endPos = this.basePos.cpy().add(64, 64);
        this.endPos.x = (int)this.endPos.x - (int)this.endPos.x % 16;
        this.endPos.y = (int)this.endPos.y - (int)this.endPos.y % 16;
        for (Vector2 currPos = new Vector2(startPos.x, startPos.y); currPos.y < endPos.y;) {
            Tile tile = game.map.tiles.get(currPos);
            currPos.x += 16;
            if (currPos.x > endPos.x) {
                currPos.x = this.startPos.x;
                currPos.y += 16;
            }
            if (tile == null) {
                continue;
            }
            if (tile.nameUpper.contains("campfire")) {
                this.currSprite.draw(game.mapBatch);
                game.actionStack.remove(this);
                game.insertAction(new DespawnGhost(this.basePos.cpy()));
                // TODO: test
//                game.currMusic.pause();
                game.currMusic.stop();
                game.currMusic.dispose();
                game.currMusic = game.map.currRoute.music;
                game.currMusic.play();
                return;
            }
        }

        // Check if player's party has fainted. if yes, remove this from AS
        boolean hasAlivePokemon = false;
        for (Pokemon pokemon : game.player.pokemon) {
            if (pokemon.currentStats.get("hp") > 0) {
                hasAlivePokemon = true;
                break;
            }
        }

        // Check if ghost pokemon is dead. if yes, remove this from AS
        if (!hasAlivePokemon || (this.pokemon.currentStats.get("hp") <= 0 && !this.inBattle)) {
            this.currSprite.draw(game.mapBatch);
            game.actionStack.remove(this);
            game.insertAction(new DespawnGhost(this.basePos.cpy()));
            // TODO: check if this works.
            boolean foundGhost = false;
            for (Action action : game.actionStack) {
                if (DrawGhost.class.isInstance(action)) {
                    foundGhost = true;
                    break;
                }
            }
            if (!foundGhost && hasAlivePokemon) {
//                game.currMusic.pause();
                game.currMusic.stop();
                game.currMusic.dispose();
                game.currMusic = game.map.currRoute.music;
                game.currMusic.play();
            }
            return;
        }

        // check whether player is in battle or not
        // if not, don't move the ghost at all (subject to change)
        if (game.battle.drawAction != null) {
            this.currSprite.draw(game.mapBatch);
            this.inBattle = true;
            this.noEncounterTimer = 0;
            return;
        }

        // Wait for a while if you just exited battle
        if (inBattle == true) {
            if (this.noEncounterTimer % 4 >= 2) {
                this.currSprite.draw(game.mapBatch);
            }
            this.noEncounterTimer++;
            if (this.noEncounterTimer < 128) {
                return;
            }
            this.inBattle = false;
        }

        // Pause if player can't move
        if (game.playerCanMove == false) {
            this.currSprite.draw(game.mapBatch);
            return;
        }

        // Calculate direction to player, face that direction
        float dx = this.basePos.x - game.player.position.x;
        float dy = this.basePos.y - game.player.position.y;
        if (dx < dy) {
            if (game.player.position.y < this.basePos.y) {
                this.dirFacing = "down";
            }
            else {
                this.dirFacing = "right";
            }
        }
        else {
            if (game.player.position.x < this.basePos.x) {
                this.dirFacing = "left";
            }
            else {
                this.dirFacing = "up";
            }
        }

        // Set ghost sprite to dirFacing
        this.currSprite = this.sprites.get(this.dirFacing)[this.animIndex];

        // game.player.position.x < this.basePos.x

        // Modify base pos to chase player (accelerate in player direction)
        if (this.dirFacing.equals("left")) {
            if (velX > -maxVel) {
                this.velX -= .1f;
            }
            this.velY -= this.velY/16.0f;
        }
        else if (this.dirFacing.equals("right")) {
            if (velX < maxVel) {
                this.velX += .1f;
            }
            this.velY -= this.velY/16.0f;
        }
        else if (this.dirFacing.equals("down")) {
            if (velY > -maxVel) {
                this.velY -= .1f;
            }
            this.velX -= this.velX/16.0f;
        }
        else if (this.dirFacing.equals("up")) {
            if (velY < maxVel) {
                this.velY += .1f;
            }
            this.velX -= this.velX/16.0f;
        }
        // apply position
        this.basePos.add(velX, velY);

        // x and y are sine function
        float shiftPosX = (float)Math.sin(sineTimer);
        float shiftPosY = (float)Math.sin(2*sineTimer);
        this.currSprite.setPosition(basePos.x+shiftPosX, basePos.y+shiftPosY);
        this.currSprite.draw(game.mapBatch);

        sineTimer+=.125f;
        if (sineTimer >= 3.14*2f) {
            sineTimer = 0.0f;
        }

        // Play ghost animation
        animFrame++;
        if (animFrame > 10) {
            animFrame = 0;
            animIndex++;
        }
        if (animIndex >= this.sprites.get(this.dirFacing).length ) {
            animIndex = 0;
        }

        // Need to make ghost rectangle bounds smaller
        Rectangle rect = this.currSprite.getBoundingRectangle();
        rect.x +=16;
        rect.y +=16;
        rect.width -=2*16;
        rect.height -=2*16;

        // Check collision. if collision, start battle with ghost pokemon
        if (rect.overlaps(game.player.currSprite.getBoundingRectangle())) {
            game.battle.oppPokemon = this.pokemon;
            game.playerCanMove = false;
            game.insertAction(Battle.getIntroAction(game));
            if (game.type == Game.Type.CLIENT) {
                game.client.sendTCP(new Network.BattleData(this.pokemon, game.player.network.id));
            }
        }
    }
}

class DrawPlayerLower extends Action {
    public int layer = 130;
    Sprite spritePart;

    public DrawPlayerLower(Game game) {
    }

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        if (game.player.isSleeping) {
            return;
        }

        this.spritePart = new Sprite(game.player.currSprite);

        // this.spritePart.setRegion(0,8,16,8);
        this.spritePart.setRegionY(8);
        this.spritePart.setRegionHeight(8);
        // this.spritePart.setSize(this.spritePart.getWidth(), 8);

        game.mapBatch.draw(this.spritePart, game.player.position.x, game.player.position.y+4);
    }
}

class DrawPlayerUpper extends Action {
    public int layer = 115;
    Sprite spritePart;

    public DrawPlayerUpper(Game game) {}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        if (game.player.isSleeping) {
            game.player.zSprite.draw(game.mapBatch);
            game.mapBatch.draw(game.player.sleepingSprite, game.player.position.x, game.player.position.y);
            return;
        }
        // Have draw sleeping bag on ground separately for 'getting into sleeping bag' animation.
        if (game.player.drawSleepingBag) {
            game.mapBatch.draw(game.player.sleepingBagSprite,
                               game.player.sleepingBagSprite.getX(),
                               game.player.sleepingBagSprite.getY());
        }

        // draw building tile if building
        if (game.player.isBuilding) {
            // get direction facing
            Vector2 pos = new Vector2(0,0);
            if (game.player.dirFacing.equals("right")) {
                pos = new Vector2(game.player.position.cpy().add(16,0));
            }
            else if (game.player.dirFacing.equals("left")) {
                pos = new Vector2(game.player.position.cpy().add(-16,0));
            }
            else if (game.player.dirFacing.equals("up")) {
                pos = new Vector2(game.player.position.cpy().add(0,16));
            }
            else if (game.player.dirFacing.equals("down")) {
                pos = new Vector2(game.player.position.cpy().add(0,-16));
            }
            // get game.player.currBuildTile and draw it at position
            Sprite sprite = new Sprite(game.player.currBuildTile.sprite);
            sprite.setAlpha(.8f);
            sprite.setPosition(pos.x, pos.y);
            Tile nextTile = game.map.tiles.get(pos);
            if (nextTile != null && nextTile.attrs.get("solid")) {
                sprite.setColor(1f, .7f, .7f, .8f);
            }
            boolean requirementsMet = true;
            for (String reqName : game.player.buildTileRequirements.get(game.player.currBuildTile.name).keySet()) {
                if (!game.player.itemsDict.containsKey(reqName)) {
                    requirementsMet = false;
                    break;
                }
                int playerOwns = game.player.itemsDict.get(reqName);
                if (playerOwns < game.player.buildTileRequirements.get(game.player.currBuildTile.name).get(reqName)) {
                    requirementsMet = false;
                    break;
                }
            }
            if (!requirementsMet) {
                sprite.setColor(1f, .7f, .7f, .8f);
            }
            sprite.draw(game.mapBatch);
//            game.batch.draw(sprite, pos.x, pos.y);
            if (game.player.currBuildTile.overSprite != null) {
//                game.player.currBuildTile.overSprite.draw(game.batch, .7f);
                sprite = new Sprite(game.player.currBuildTile.overSprite);
                sprite.setAlpha(.8f);
                sprite.setPosition(pos.x, pos.y);
                if (nextTile != null && nextTile.attrs.get("solid")) {
                    sprite.setColor(1f, .7f, .7f, .8f);
                }
                sprite.draw(game.mapBatch);
//                game.batch.draw(sprite, pos.x, pos.y);
            }
        }

        this.spritePart = new Sprite(game.player.currSprite);

        // this.spritePart.setRegion(0,0,16,8);
        // this.spritePart.setRegionHeight(4);
        // this.spritePart.setSize(this.spritePart.getWidth(), 4);
        this.spritePart.setRegionY(0);
        this.spritePart.setRegionHeight(8);

        game.mapBatch.draw(this.spritePart, game.player.position.x, game.player.position.y+12);

        // this needs to be set to detect collision
        game.player.currSprite.setPosition(game.player.position.x, game.player.position.y);
    }
}

// demo of drawing sprites for ea pokemon caught
class DrawPokemonCaught extends Action {
    public int layer = 110;
    Sprite pokeball;

    public DrawPokemonCaught(Game game) {
        Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball1.png"));
        this.pokeball = new Sprite(text, 0, 0, 12, 12);
        this.pokeball.setScale(2);
    };

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        for (int i=0; i < game.player.pokemon.size(); i++) {
            game.uiBatch.draw(pokeball, i*16*3, 144*3-12);
        }
//
//        this.pokeball.setPosition(0*16*3+12, 144*3-12*3);
//        this.pokeball.draw(game.floatingBatch);
    }
}

class HeadbuttTreeAnim extends Action {
    Sprite originalSprite;
    Sprite rightSprite;
    Sprite leftSprite;
    int index = 0;
    Tile tile;
    Action nextAction;
    Sprite left, right;

    public HeadbuttTreeAnim(Game game, Tile tile, Action nextAction) {
        this.tile = tile;
        this.nextAction = nextAction;
    }

    @Override
    public void step(Game game) {
        if (this.index == 0) {
            this.originalSprite = this.tile.overSprite;
            TextureData temp = this.tile.overSprite.getTexture().getTextureData();
//            TextureRegion[][] tempRegion = temp.split((int)originalSprite.getWidth(), (int)originalSprite.getHeight()); // should be 7x7
//            this.rightSprite = new Sprite[(int)originalSprite.getWidth()][(int)originalSprite.getHeight()];
            int width = (int)originalSprite.getWidth();
            int height = (int)originalSprite.getHeight();
            if (!temp.isPrepared()) {
                temp.prepare();
            }
            Pixmap pixmap = temp.consumePixmap();
            // transparent pixmap to draw to
            Color clearColor = new Color(0, 0, 0, 0);
            // pixmap of right-leaning sprite
            Pixmap rightPixmap = new Pixmap(width+4, height, Pixmap.Format.RGBA8888);
            rightPixmap.setColor(clearColor);
            rightPixmap.fill();
            // pixmap of left-leaning sprite
            Pixmap leftPixmap = new Pixmap(width+4, height, Pixmap.Format.RGBA8888);
            leftPixmap.setColor(clearColor);
            leftPixmap.fill();
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    int additionalX = 0;
                    if (height-j < height/4) {
                    }
                    else if (height-j < (5*height)/8) {
                        additionalX = 1;
                    }
                    else if (height-j < (7*height)/8) {
                        additionalX = 2;
                    }
                    else if (height-j < (15*height)/16) {
                        additionalX = 3;
                    }
                    else {
                        additionalX = 4;
                    }
                    Color color = new Color(pixmap.getPixel(i, j));
                    rightPixmap.drawPixel(i+additionalX, j, Color.rgba8888(color));
                    leftPixmap.drawPixel(i-additionalX+4, j, Color.rgba8888(color));
                }
            }
            this.rightSprite = new Sprite(new Texture(rightPixmap));
            this.rightSprite.setPosition(this.originalSprite.getX(), this.originalSprite.getY());
            this.leftSprite = new Sprite(new Texture(leftPixmap));
            this.leftSprite.setPosition((int)this.originalSprite.getX()-4, this.originalSprite.getY());
        }
        else if (this.index == 14) {
            game.insertAction(new PlaySound("headbutt1", null));
        }
        else if (this.index == 18+3) {
            this.tile.overSprite = this.rightSprite;
        }
        else if (this.index == 18+3+3) {
            this.tile.overSprite = this.originalSprite;
        }
        else if (this.index == 18+3+3+3) {
            this.tile.overSprite = this.leftSprite;
        }
        else if (this.index == 18+3+3+3+3) {
            this.tile.overSprite = this.originalSprite;
        }
        else if (this.index == 18+3+3+3+3+3) {
            this.tile.overSprite = this.rightSprite;
        }
        else if (this.index == 18+3+3+3+3+3+3) {
            this.tile.overSprite = this.originalSprite;
        }
        else if (this.index == 18+3+3+3+3+3+3+3) {
            this.tile.overSprite = this.leftSprite;
        }
        else if (this.index == 18+3+3+3+3+3+3+3+3) {
            this.tile.overSprite = this.originalSprite;
        }
        else if (this.index == 18+3+3+3+3+3+3+3+3+3) {
            this.tile.overSprite = this.rightSprite;
        }
        else if (this.index == 18+3+3+3+3+3+3+3+3+3+3) {
            this.tile.overSprite = this.originalSprite;
        }
        else if (this.index == 18+3+3+3+3+3+3+3+3+3+3+11) {
//            game.map.tiles.put(this.tile.position.cpy(), this.tile);
//            game.playerCanMove = true; // TODO: remove
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
        }
        this.index++;
    }
}

class ItemPickupNotify extends Action {
    public int layer = 114;
    Sprite bgSprite;
    int signCounter = 150;;

    String itemName;
    int quantity;

    public ItemPickupNotify(Game game, String itemName, int quantity) {
        this.itemName = itemName;
        this.quantity = quantity;
        Texture text = new Texture(Gdx.files.internal("text2.png"));
        this.bgSprite = new Sprite(text, 0, 0, 160, 144);
        this.bgSprite.setPosition(0, -144);
    }
    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        if (signCounter > 0) {
            signCounter--;
            if (signCounter > 100) {
            }
            else if (signCounter > 78) {
                this.bgSprite.setPosition(this.bgSprite.getX(), this.bgSprite.getY()+1);
            }
            else if (signCounter > 22) {
            }
            else {
                this.bgSprite.setPosition(this.bgSprite.getX(), this.bgSprite.getY()-1);
            }
            this.bgSprite.draw(game.uiBatch);
            game.font.draw(game.uiBatch, "Picked up "+this.itemName+" x"+this.quantity+".", 42, this.bgSprite.getY()+134);
        }
    }
}

/**
 * Plants a tree at the tile specified by pos.
 * Note: based off of leech seed anim, which uses five frames per sprite.
 */
class PlantTree extends Action {
    public int layer = 0;
    Vector2 pos;
    ArrayList<Sprite> sprites = new ArrayList<Sprite>();
    int timer = 0;
    Tile currTile;

    public PlantTree(Vector2 pos, Action nextAction) {
        this.pos = pos;
        this.nextAction = nextAction;
        Texture text = new Texture(Gdx.files.internal("sprout_sheet2.png"));
        for (int i = 0; i < 5; i++) {
            Sprite sprite = new Sprite(text, 16*i, 0, 16, 16);
            sprite.setPosition(pos.x, pos.y+2);
            this.sprites.add(sprite);
        }
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void firstStep(Game game) {
        this.currTile = game.map.tiles.get(this.pos);
    }

    @Override
    public void step(Game game) {
        if (this.timer == 0) {
            this.currTile.overSprite = this.sprites.get(0);
        }
        else if (this.timer == 12) {
            this.currTile.overSprite = this.sprites.get(1);
        }
        else if (this.timer == 15) {
            this.currTile.overSprite = this.sprites.get(2);
        }
        else if (this.timer == 18) {
            this.currTile.overSprite = this.sprites.get(3);
        }
        else if (this.timer == 21) {
            this.currTile.overSprite = this.sprites.get(4);
        }
        else if (this.timer == 24) {
            game.map.tiles.put(this.pos.cpy(), new Tile(currTile.name, "tree_planted2",
                                                        this.pos.cpy(), true, currTile.routeBelongsTo));
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
        }
        this.timer++;
    }
}

/**
 * Self-explanatory Player class.
 */
public class Player {
    // current player direction
    public String dirFacing = "down";

    // movement sprites
    Map<String, Sprite> standingSprites = new HashMap<String, Sprite>();
    Map<String, Sprite> movingSprites = new HashMap<String, Sprite>();
    Map<String, Sprite> altMovingSprites = new HashMap<String, Sprite>();

    public Vector2 position;
    public Sprite currSprite = new Sprite();

    Sprite battleSprite;
    Sprite sleepingSprite;
    Sprite sleepingBagSprite;

    String name;  // Player display name
    Color color = new Color(0.9137255f, 0.5294118f, 0.1764706f, 1f);  // Used to colorize the player sprite

    // players pokemon
     // functions as pokemon storage in demo
    ArrayList<Pokemon> pokemon;
    Pokemon currPokemon; // needed for displaying current pokemon

    // items that player currently possesses
    ArrayList<String> itemsList;
    Map<String, Integer> itemsDict; // had idea to make this an array of 'ItemActions' instead; would have .name attribute for printing name in menus
     // each action would perform action; would still need to initialize the action when adding tho
     // decided this way was less intuitive.

    int adrenaline;  // was 'streak'
    // demo mechanic - catching in a row without oppPokemon fleeing builds streak
     // displayed as 'adrenaline'
     // based off of catch rate

    // this would be if doing android
     // action would set these
    // Map<String, Integer> buttonPressed = new HashMap<String, Integer>();

    String currState; // need for ghost spawn
    boolean isBuilding = false; // player has building tile in front of them
    boolean isCutting = false; // player will try to cut tree on A press
    boolean isHeadbutting = false; // player will try to headbutt tree on A press
    boolean isJumping = false; // player will jump up ledges using 'fast' ledge jump animation.
    public boolean isCrafting = false;
    public boolean isSleeping = false;
    public boolean drawSleepingBag = false;
    public boolean acceptInput = true;
    boolean isNearCampfire = false;
    Tile currBuildTile; // which tile will be built next
    int buildTileIndex = 0;
    ArrayList<Tile> buildTiles = new ArrayList<Tile>();
    HashMap<String, HashMap<String, Integer>> buildTileRequirements = new HashMap<String, HashMap<String, Integer>>();
    boolean canMove = true;  // TODO: migrate to start using this
    int numFlees = 0;  // used in battle run away mechanic
    Sprite zSprite;
    int zsTimer = 0;
    Vector2 spawnLoc = new Vector2(0, 0);
    PlayerStanding standingAction;
    public boolean displayedMaxPartyText = false;

    public Type type;
    Network network;

    static class Craft {
        String name;
        int amount;
        ArrayList<Craft> requirements = new ArrayList<Craft>();
        public Craft(String name, int amount) {
            this.name = name;
            this.amount = amount;
        }
    }
    // Things that the player can craft
    public static ArrayList<Craft> crafts = new ArrayList<Craft>();
    static {
        Craft craft = new Craft("heavy ball", 1);
        craft.requirements.add(new Craft("black apricorn", 1));
        Player.crafts.add(craft);
        craft = new Craft("lure ball", 1);
        craft.requirements.add(new Craft("blue apricorn", 1));
        Player.crafts.add(craft);
        craft = new Craft("friend ball", 1);
        craft.requirements.add(new Craft("green apricorn", 1));
        Player.crafts.add(craft);
        craft = new Craft("love ball", 1);
        craft.requirements.add(new Craft("pink apricorn", 1));
        Player.crafts.add(craft);
        craft = new Craft("level ball", 1);
        craft.requirements.add(new Craft("red apricorn", 1));
        Player.crafts.add(craft);
        craft = new Craft("fast ball", 1);
        craft.requirements.add(new Craft("white apricorn", 1));
        Player.crafts.add(craft);
        craft = new Craft("moon ball", 1);
        craft.requirements.add(new Craft("yellow apricorn", 1));
        Player.crafts.add(craft);
    }

    public Player() {
        Texture text = new Texture(Gdx.files.internal("tiles/zs1.png"));
        this.zSprite = new Sprite(text, 0, 0, 16, 16);
        // Set player standing/moving sprites
//        Texture playerText = new Texture(Gdx.files.internal("player1_sheet1.png"));
        Texture playerText = new Texture(Gdx.files.internal("player1_sheet1_color.png"));
        this.standingSprites.put("down", new Sprite(playerText, 0, 0, 16, 16));
        this.standingSprites.put("up", new Sprite(playerText, 16, 0, 16, 16));
        this.standingSprites.put("left", new Sprite(playerText, 32, 0, 16, 16));
        this.standingSprites.put("right", new Sprite(playerText, 48, 0, 16, 16));
        this.movingSprites.put("down", new Sprite(playerText, 64, 0, 16, 16));
        this.movingSprites.put("up", new Sprite(playerText, 80, 0, 16, 16));
        this.movingSprites.put("left", new Sprite(playerText, 96, 0, 16, 16));
        this.movingSprites.put("right", new Sprite(playerText, 112, 0, 16, 16));
        this.altMovingSprites.put("down", new Sprite(playerText, 64, 0, 16, 16));
        this.altMovingSprites.get("down").flip(true, false);
        this.altMovingSprites.put("up", new Sprite(playerText, 80, 0, 16, 16));
        this.altMovingSprites.get("up").flip(true, false);
        this.altMovingSprites.put("left", new Sprite(playerText, 96, 0, 16, 16));
        this.altMovingSprites.put("right", new Sprite(playerText, 112, 0, 16, 16));

        // Running sprites
//        playerText = new Texture(Gdx.files.internal("player1_sheet2.png"));
        playerText = new Texture(Gdx.files.internal("player1_sheet2_color.png"));
        this.standingSprites.put("down_running", new Sprite(playerText, 0, 0, 16, 16));
        this.standingSprites.put("up_running", new Sprite(playerText, 16, 0, 16, 16));
        this.standingSprites.put("left_running", new Sprite(playerText, 32, 0, 16, 16));
        this.standingSprites.put("right_running", new Sprite(playerText, 48, 0, 16, 16));
        this.movingSprites.put("down_running", new Sprite(playerText, 64, 0, 16, 16));
        this.movingSprites.put("up_running", new Sprite(playerText, 80, 0, 16, 16));
        this.movingSprites.put("left_running", new Sprite(playerText, 96, 0, 16, 16));
        this.movingSprites.put("right_running", new Sprite(playerText, 112, 0, 16, 16));
        this.altMovingSprites.put("down_running", new Sprite(playerText, 64, 0, 16, 16));
        this.altMovingSprites.get("down_running").flip(true, false);
        this.altMovingSprites.put("up_running", new Sprite(playerText, 80, 0, 16, 16));
        this.altMovingSprites.get("up_running").flip(true, false);
        this.altMovingSprites.put("left_running", new Sprite(playerText, 96, 0, 16, 16));
        this.altMovingSprites.put("right_running", new Sprite(playerText, 112, 0, 16, 16));

        // battle portrait sprite
//        Texture text = new Texture(Gdx.files.internal("battle/player_back1.png"));
//        this.battleSprite = new Sprite(text, 0, 0, 28, 28);
        text = new Texture(Gdx.files.internal("battle/player_back_color1.png"));
        this.battleSprite = new Sprite(text, 0, 0, 45, 46);
        text = new Texture(Gdx.files.internal("tiles/sleeping_bag1_using.png"));
        this.sleepingSprite = new Sprite(text, 0, 0, 24, 16);
        text = new Texture(Gdx.files.internal("tiles/sleeping_bag1.png"));
        this.sleepingBagSprite = new Sprite(text, 0, 0, 24, 16);

        // set initial position
        this.position = new Vector2(0,0);
        this.currSprite = new Sprite(this.standingSprites.get(this.dirFacing));

        // TODO: default name - change later
        this.name = "AAAA";
        this.pokemon = new ArrayList<Pokemon>();

        this.adrenaline = 0;
        this.currState = "";

        // TODO: remove unused
        // currBuildTile
//        this.buildTiles.add(new Tile("house1_middle1", new Vector2(0,0)));
//        this.buildTiles.add(new Tile("house1_left1", new Vector2(0,0)));
//        this.buildTiles.add(new Tile("house1_right1", new Vector2(0,0)));
//        this.buildTiles.add(new Tile("house1_door1_dark", new Vector2(0,0)));
//        this.buildTiles.add(new Tile("house1_roof_middle1", new Vector2(0,0)));
//        this.buildTiles.add(new Tile("house1_roof_left1", new Vector2(0,0)));
//        this.buildTiles.add(new Tile("house1_roof_right1", new Vector2(0,0)));
//        // couldn't get these to look right - roof middle looks funny
////        this.buildTiles.add(new Tile("house2_middle1", new Vector2(0,0)));
//        this.buildTiles.add(new Tile("house2_door1_dark", new Vector2(0,0)));
////        this.buildTiles.add(new Tile("house2_roof_middle1", new Vector2(0,0)));
////        this.buildTiles.add(new Tile("house2_roof_left1", new Vector2(0,0)));
////        this.buildTiles.add(new Tile("house2_roof_right1", new Vector2(0,0)));
//        this.buildTiles.add(new Tile("house3_left1", new Vector2(0,0)));
//        this.buildTiles.add(new Tile("house3_middle1", new Vector2(0,0)));
//        this.buildTiles.add(new Tile("house3_right1", new Vector2(0,0)));
//        this.buildTiles.add(new Tile("house3_roof_middle1", new Vector2(0,0)));
//        this.buildTiles.add(new Tile("house3_roof_left1", new Vector2(0,0)));
//        this.buildTiles.add(new Tile("house3_roof_right1", new Vector2(0,0)));

//        this.buildTiles.add(new Tile("house4_middle1", new Vector2(0,0)));
        this.buildTiles.add(new Tile("house5_door1", new Vector2(0,0)));
//        this.buildTiles.add(new Tile("house5_left1", new Vector2(0,0)));
        this.buildTiles.add(new Tile("house5_middle1", new Vector2(0,0)));
//        this.buildTiles.add(new Tile("house5_right1", new Vector2(0,0)));
        this.buildTiles.add(new Tile("house5_roof_middle1", new Vector2(0,0)));
//        this.buildTiles.add(new Tile("house5_roof_left1", new Vector2(0,0)));
//        this.buildTiles.add(new Tile("house5_roof_right1", new Vector2(0,0)));
        this.buildTiles.add(new Tile("campfire1", new Vector2(0,0)));
        this.buildTiles.add(new Tile("fence1", new Vector2(0,0)));
        // TODO: remove
//        this.buildTiles.add(new Tile("sleeping_bag1", new Vector2(0,0)));
        this.currBuildTile = this.buildTiles.get(this.buildTileIndex);

        // for now, everything requires same amt
        // TODO: convert this to static Player var.
        for (Tile tile : this.buildTiles) {
            if (tile.nameUpper.equals("sleeping_bag1")) {
                continue;
            }
            this.buildTileRequirements.put(tile.name, new HashMap<String, Integer>());
            if (tile.name.contains("campfire")) {
                this.buildTileRequirements.get(tile.name).put("log", 4);
                this.buildTileRequirements.get(tile.name).put("grass", 2);
                continue;
            }
            this.buildTileRequirements.get(tile.name).put("grass", 1);
            this.buildTileRequirements.get(tile.name).put("log", 1);
        }
        // can pick up sleeping bag and put back in inventory
        this.buildTileRequirements.put("sleeping_bag1", new HashMap<String, Integer>());
        this.buildTileRequirements.get("sleeping_bag1").put("Sleeping Bag", 1);

        // TODO: debug - initialize items list
        // TODO: not using itemsList anymore, using itemsDict
        this.itemsList = new ArrayList<String>();
        this.itemsList.add("Master Ball");
        this.itemsList.add("Ultra Ball");
        this.itemsList.add("Great Ball");
        this.itemsList.add("Safari Ball");
        this.itemsList.add("Safari Ball2");
        this.itemsList.add("Safari Ball3");
        this.itemsList.add("Safari Ball4");
        this.itemsDict = new HashMap<String, Integer>();
        this.itemsDict.put("Sleeping Bag", 1);
//        this.itemsDict.put("Safari Ball", 99);
        // TODO: debug, remove
//        this.itemsDict.put("grass", 99);
//        this.itemsDict.put("log", 99);
//        this.itemsDict.put("blue apricorn", 99);
//        this.itemsDict.put("ultra ball", 99);
//        this.itemsDict.put("blue apricorn", 99);
//        this.itemsDict.put("Poké Ball", 99);

        this.network = new Network(this.position);
        this.type = Type.LOCAL;
    }

    /**
     * Constructor to load from serialized class (ie data sent over network or loaded from file).
     */
    public Player(com.pkmngen.game.Network.PlayerData playerData) {
        this();
        this.spawnLoc = playerData.spawnLoc;
        this.dirFacing = playerData.dirFacing;
        this.position = playerData.position;
        this.name = playerData.name;
        this.pokemon = new ArrayList<Pokemon>();
        for (com.pkmngen.game.Network.PokemonData pokemonData : playerData.pokemon) {
            this.pokemon.add(new Pokemon(pokemonData));
        }
        // TODO: remove
        if (this.pokemon.size() > 0) {
            this.currPokemon = this.pokemon.get(0);
        }
        this.itemsDict = playerData.itemsDict;
        this.network = new Network(this.position.cpy());  // re-initialize loading zone boundaries based off of current position.
        this.network.id = playerData.id;
        this.network.number = playerData.number;
        this.setColor(playerData.color);  // TODO: when server re-loads player, doesn't seem like this is being rendered.
        this.displayedMaxPartyText = playerData.displayedMaxPartyText;
//        this.network.isInterior = playerData.isInterior;
        if (playerData.isInterior) {
            this.network.tiles = Game.staticGame.map.interiorTiles.get(Game.staticGame.map.interiorTilesIndex);
        }
        else {
            this.network.tiles = Game.staticGame.map.overworldTiles;
        }
    }

    /**
     * Check if player has required materials for a craft.
     */
    public boolean hasCraftRequirements(int craftIndex, int amount) {
        boolean hasRequirements = true;
        for (Player.Craft req : Player.crafts.get(craftIndex).requirements) {
            hasRequirements = false;
            for (String item : this.itemsDict.keySet()) {
                if (item.equals(req.name) && this.itemsDict.get(item) >= req.amount*amount) {
                    hasRequirements = true;
                    break;
                }
            }
            if (!hasRequirements) {
                break;
            }
        }
        return hasRequirements;
    }

    /**
     * Check if player is near campfire (needed to know if ghost can spawn or not).
     */
    public boolean checkNearCampfire() {
        Vector2 startPos = this.position.cpy().add(-64, -64);
        startPos.x = (int)startPos.x - (int)startPos.x % 16;
        startPos.y = (int)startPos.y - (int)startPos.y % 16;
        Vector2 endPos = this.position.cpy().add(64, 64);
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
            if (tile.nameUpper.contains("campfire")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Deduct required materials from player inventory and add crafted item to inventory.
     */
    public void craftItem(int craftIndex, int amount) {
        // Remove required materials from player inventory
        Craft craft = Player.crafts.get(craftIndex);
        for (Player.Craft req : craft.requirements) {
            // TODO: remove
//            for (String item : this.itemsDict.keySet()) {
//                if (item.equals(req.name)) {
//                    int newAmt = this.itemsDict.get(item)-(req.amount*amount);
//                    this.itemsDict.put(item, newAmt);
//                    break;
//                }
//            }
            int newAmt = this.itemsDict.get(req.name)-(req.amount*amount);
            this.itemsDict.put(req.name, newAmt);
            if (newAmt <= 0) {
                this.itemsDict.remove(req.name);
            }
        }
        int newAmt = amount;
        if (this.itemsDict.containsKey(craft.name)) {
            newAmt = this.itemsDict.get(craft.name) + newAmt;
        }
        this.itemsDict.put(craft.name, newAmt);
    }

    /**
     * Get position that player is facing towards.
     */
    public Vector2 facingPos() {
        return this.facingPos(this.dirFacing);
    }

    /**
     * Get position that player is facing towards.
     */
    public Vector2 facingPos(String dirFacing) {
        Vector2 pos = null;
        if (dirFacing.equals("right")) {
            pos = this.position.cpy().add(16, 0);
        }
        else if (dirFacing.equals("left")) {
            pos = this.position.cpy().add(-16, 0);
        }
        else if (dirFacing.equals("up")) {
            pos = this.position.cpy().add(0, 16);
        }
        else if (dirFacing.equals("down")) {
            pos = this.position.cpy().add(0, -16);
        }
        return pos;
    }

    public void setColor(Color newColor) {
        this.color = newColor;

        // Colorize the sheet based on this.color
        Texture playerText = new Texture(Gdx.files.internal("player1_sheet1_color.png"));
        if (!playerText.getTextureData().isPrepared()) {
            playerText.getTextureData().prepare();
        }
        Pixmap pixmap = playerText.getTextureData().consumePixmap();
        Color clearColor = new Color(0, 0, 0, 0);
        Pixmap coloredPixmap = new Pixmap(playerText.getWidth(), playerText.getHeight(), Pixmap.Format.RGBA8888);
        coloredPixmap.setColor(clearColor);
        coloredPixmap.fill();
        for (int i = 0; i < playerText.getWidth(); i++) {
            for (int j = 0; j < playerText.getHeight(); j++) {
                Color color = new Color(pixmap.getPixel(i, j));
                if (color.equals(new Color(0.9137255f, 0.5294118f, 0.1764706f, 1f))) {
                    color = this.color;
                }
                coloredPixmap.drawPixel(i, j, Color.rgba8888(color));
            }
        }
        playerText = new Texture(coloredPixmap);
        this.standingSprites.put("down", new Sprite(playerText, 0, 0, 16, 16));
        this.standingSprites.put("up", new Sprite(playerText, 16, 0, 16, 16));
        this.standingSprites.put("left", new Sprite(playerText, 32, 0, 16, 16));
        this.standingSprites.put("right", new Sprite(playerText, 48, 0, 16, 16));
        this.movingSprites.put("down", new Sprite(playerText, 64, 0, 16, 16));
        this.movingSprites.put("up", new Sprite(playerText, 80, 0, 16, 16));
        this.movingSprites.put("left", new Sprite(playerText, 96, 0, 16, 16));
        this.movingSprites.put("right", new Sprite(playerText, 112, 0, 16, 16));
        this.altMovingSprites.put("down", new Sprite(playerText, 64, 0, 16, 16));
        this.altMovingSprites.get("down").flip(true, false);
        this.altMovingSprites.put("up", new Sprite(playerText, 80, 0, 16, 16));
        this.altMovingSprites.get("up").flip(true, false);
        this.altMovingSprites.put("left", new Sprite(playerText, 96, 0, 16, 16));
        this.altMovingSprites.put("right", new Sprite(playerText, 112, 0, 16, 16));

        // running sprites
//        playerText = new Texture(Gdx.files.internal("player1_sheet2.png"));
        playerText = new Texture(Gdx.files.internal("player1_sheet2_color.png"));
        if (!playerText.getTextureData().isPrepared()) {
            playerText.getTextureData().prepare();
        }
        pixmap = playerText.getTextureData().consumePixmap();
        clearColor = new Color(0, 0, 0, 0);
        coloredPixmap = new Pixmap(playerText.getWidth(), playerText.getHeight(), Pixmap.Format.RGBA8888);
        coloredPixmap.setColor(clearColor);
        coloredPixmap.fill();
        for (int i = 0; i < playerText.getWidth(); i++) {
            for (int j = 0; j < playerText.getHeight(); j++) {
                Color color = new Color(pixmap.getPixel(i, j));
                if (color.equals(new Color(0.9137255f, 0.5294118f, 0.1764706f, 1f))) {
                    color = this.color;
                }
                coloredPixmap.drawPixel(i, j, Color.rgba8888(color));

            }
        }
        playerText = new Texture(coloredPixmap);
        this.standingSprites.put("down_running", new Sprite(playerText, 0, 0, 16, 16));
        this.standingSprites.put("up_running", new Sprite(playerText, 16, 0, 16, 16));
        this.standingSprites.put("left_running", new Sprite(playerText, 32, 0, 16, 16));
        this.standingSprites.put("right_running", new Sprite(playerText, 48, 0, 16, 16));
        this.movingSprites.put("down_running", new Sprite(playerText, 64, 0, 16, 16));
        this.movingSprites.put("up_running", new Sprite(playerText, 80, 0, 16, 16));
        this.movingSprites.put("left_running", new Sprite(playerText, 96, 0, 16, 16));
        this.movingSprites.put("right_running", new Sprite(playerText, 112, 0, 16, 16));
        this.altMovingSprites.put("down_running", new Sprite(playerText, 64, 0, 16, 16));
        this.altMovingSprites.get("down_running").flip(true, false);
        this.altMovingSprites.put("up_running", new Sprite(playerText, 80, 0, 16, 16));
        this.altMovingSprites.get("up_running").flip(true, false);
        this.altMovingSprites.put("left_running", new Sprite(playerText, 96, 0, 16, 16));
        this.altMovingSprites.put("right_running", new Sprite(playerText, 112, 0, 16, 16));

        playerText = new Texture(Gdx.files.internal("battle/player_back_color1.png"));
        if (!playerText.getTextureData().isPrepared()) {
            playerText.getTextureData().prepare();
        }
        pixmap = playerText.getTextureData().consumePixmap();
        clearColor = new Color(0, 0, 0, 0);
        coloredPixmap = new Pixmap(playerText.getWidth(), playerText.getHeight(), Pixmap.Format.RGBA8888);
        coloredPixmap.setColor(clearColor);
        coloredPixmap.fill();
        for (int i = 0; i < playerText.getWidth(); i++) {
            for (int j = 0; j < playerText.getHeight(); j++) {
                Color color = new Color(pixmap.getPixel(i, j));
                if (color.equals(new Color(0.6901961f, 0.28235295f, 0.15686275f, 1f))) {
                    color = this.color;
                }
                coloredPixmap.drawPixel(i, j, Color.rgba8888(color));
            }
        }
        playerText = new Texture(coloredPixmap);
        this.battleSprite = new Sprite(playerText, 0, 0, 45, 46);

        // recolor sleeping bag sprite
        playerText = new Texture(Gdx.files.internal("tiles/sleeping_bag1_using.png"));
        if (!playerText.getTextureData().isPrepared()) {
            playerText.getTextureData().prepare();
        }
        pixmap = playerText.getTextureData().consumePixmap();
        clearColor = new Color(0, 0, 0, 0);
        coloredPixmap = new Pixmap(playerText.getWidth(), playerText.getHeight(), Pixmap.Format.RGBA8888);
        coloredPixmap.setColor(clearColor);
        coloredPixmap.fill();
        for (int i = 0; i < playerText.getWidth(); i++) {
            for (int j = 0; j < playerText.getHeight(); j++) {
                Color color = new Color(pixmap.getPixel(i, j));
                if (color.equals(new Color(0.9137255f, 0.5294118f, 0.1764706f, 1f))) {
                    color = this.color;
                }
                coloredPixmap.drawPixel(i, j, Color.rgba8888(color));
            }
        }
        playerText = new Texture(coloredPixmap);
        this.sleepingSprite = new Sprite(playerText, 0, 0, 24, 16);
    }

    class Network {
//        Vector2 loadingZoneBL = new Vector2();
//        Vector2 loadingZoneTR = new Vector2();
        LoadingZone loadingZone = new LoadingZone();
        String id;  // when this is a remote player, this is an index number
        boolean shouldMove = false;
        String dirFacing = "down";
        boolean isRunning = false;
        int connectionId;  // kryonet connection id
        String number;
        com.pkmngen.game.Network.BattleData doEncounter;  // acts as flag that battle has been entered

        int syncTimer = 0;  // used to know when to re-send player location to client
        Map<Vector2, Tile> tiles;  // where player is located, ie interior, exterior etc.
        boolean isInterior;

        public Network(Vector2 position) {
//            this.loadingZone.setSize(128*4, 128*4);
            this.loadingZone.setSize(128*6, 128*6);
            this.loadingZone.setCenter(position);
            this.loadingZone.inner = new LoadingZone();
            this.loadingZone.inner.setSize(224, 224);
            this.loadingZone.inner.setCenter(position);
        }
    }

    enum Type {
        LOCAL,   // local, ie accepting keyboard input
        REMOTE;  // being synced from remote machine
    }
}

class PlayerBump extends Action {
    public int layer = 130;
    int timer = 0;

    int maxTime = 10; // 20 reminded me of gold version I think
    boolean alternate = false;
    public static boolean alternate2 = false;

    Player player;
    public PlayerBump(Game game) {
        this(game, game.player);
    }

    public PlayerBump(Game game, Player player) {
        this.player = player;
        game.insertAction(new PlaySound("bump2", null));
    }

    public int getLayer(){return this.layer;}

    public void localStep(Game game) {
        timer++;

        if (this.timer >= 2*maxTime ) {
            this.alternate = !this.alternate;
            this.timer = 0;
            game.insertAction(new PlaySound("bump2", null));
        }

        if (this.timer < maxTime) {
            if (this.alternate == true) {
                // game.batch.draw(game.player.altMovingSprites.get(game.player.dirFacing), game.player.position.x, game.player.position.y);
                game.player.currSprite = game.player.altMovingSprites.get(game.player.dirFacing);
            }
            else {
                // game.batch.draw(game.player.movingSprites.get(game.player.dirFacing), game.player.position.x, game.player.position.y);
                game.player.currSprite = game.player.movingSprites.get(game.player.dirFacing);
            }
        }
        else {
            // game.batch.draw(game.player.standingSprites.get(game.player.dirFacing), game.player.position.x, game.player.position.y);
            game.player.currSprite = game.player.standingSprites.get(game.player.dirFacing);
        }

        // when facingDir key is released, go to playerStanding
        if (!InputProcessor.upPressed && game.player.dirFacing.equals("up")) {
            game.insertAction(new PlayerStanding(game));
            game.actionStack.remove(this);
        }
        else if (!InputProcessor.downPressed && game.player.dirFacing.equals("down")) {
            game.insertAction(new PlayerStanding(game));
            game.actionStack.remove(this);
        }
        else if (!InputProcessor.leftPressed && game.player.dirFacing.equals("left")) {
            game.insertAction(new PlayerStanding(game));
            game.actionStack.remove(this);
        }
        else if (!InputProcessor.rightPressed && game.player.dirFacing.equals("right")) {
            game.insertAction(new PlayerStanding(game));
            game.actionStack.remove(this);
        }
    }

    public void remoteStep(Game game) {
        this.player.network.syncTimer++;
        timer++;

//        if (this.timer >= 2*maxTime ) {
//            this.alternate = !this.alternate;
//            this.timer = 0;
//            game.insertAction(new PlaySound("bump2", null));
//        }

        if (this.timer < maxTime) {
            if (this.alternate) {
                this.player.currSprite = this.player.altMovingSprites.get(this.player.dirFacing);
            }
            else {
                this.player.currSprite = this.player.movingSprites.get(this.player.dirFacing);
            }
        }
        else {
            this.player.currSprite = this.player.standingSprites.get(this.player.dirFacing);
        }

        if (this.timer >= 2*maxTime || this.player.network.shouldMove) {
            this.player.standingAction.alternate = true;
            this.player.standingAction.isRunning = false;
            game.insertAction(this.player.standingAction);
            game.actionStack.remove(this);
        }
    }

    @Override
    public void step(Game game) {
        if (this.player.type == Player.Type.LOCAL) {
            this.localStep(game);
        }
        else if (this.player.type == Player.Type.REMOTE) {
            this.remoteStep(game);
        }
    }
}

class PlayerCanMove extends Action {
    Action nextAction;

    public PlayerCanMove(Game game, Action nextAction) {
        this.nextAction = nextAction;
    }

    @Override
    public void step(Game game) {
        game.playerCanMove = true;

        game.insertAction(this.nextAction);
        game.actionStack.remove(this);
    }
}

// made up/right/left anim same as down
 // possible that length that shadow stays isn't perfect
class PlayerLedgeJump extends Action {
    public int layer = 131;
    float xDist, yDist;

    Vector2 initialPos, targetPos;

    Sprite shadow;

    int timer1 = 0;

    ArrayList<Integer> yMovesList = new ArrayList<Integer>();

    ArrayList<Map<String, Sprite>> spriteAnim = new ArrayList<Map<String, Sprite>>();
    // Map<String, ArrayList<Sprite>> spritesAnimList = new HashMap<String, ArrayList<Sprite>>();
    Player player;
    public PlayerLedgeJump(Game game) {
        this(game, game.player);
    }

    public PlayerLedgeJump(Game game, Player player) {
        this.player = player;
        this.initialPos = new Vector2(this.player.position);
        if (this.player.dirFacing.equals("up")) {
            this.targetPos = new Vector2(this.player.position.x, this.player.position.y+32);
        }
        else if (this.player.dirFacing.equals("down")) {
            this.targetPos = new Vector2(this.player.position.x, this.player.position.y-32);
        }
        else if (this.player.dirFacing.equals("left")) {
            this.targetPos = new Vector2(this.player.position.x-32, this.player.position.y);
        }
        else if (this.player.dirFacing.equals("right")) {
            this.targetPos = new Vector2(this.player.position.x+32, this.player.position.y);
        }

        // shadow sprite
        Texture shadowText = new Texture(Gdx.files.internal("shadow1.png"));
        this.shadow = new Sprite(shadowText, 0, 0, 16, 16);

        // play sound
        game.insertAction(new PlaySound("ledge1", null));

        // below two lists are used to get exact sprite and
         // y movement on every other frame
        this.yMovesList.add(4);
        this.yMovesList.add(2);
        this.yMovesList.add(2);
        this.yMovesList.add(2);
        this.yMovesList.add(1);
        this.yMovesList.add(1);
        this.yMovesList.add(0);
        this.yMovesList.add(0);
        this.yMovesList.add(-1);
        this.yMovesList.add(-1);
        this.yMovesList.add(-1);
        this.yMovesList.add(-1);
        this.yMovesList.add(-2);
        this.yMovesList.add(-3);
        this.yMovesList.add(-3);
        this.yMovesList.add(0);

        // sprites to use (according to frame-by-frame)
        this.spriteAnim.add(this.player.standingSprites);
        this.spriteAnim.add(this.player.standingSprites);
        this.spriteAnim.add(this.player.movingSprites);
        this.spriteAnim.add(this.player.movingSprites);
        this.spriteAnim.add(this.player.movingSprites);
        this.spriteAnim.add(this.player.movingSprites);
        this.spriteAnim.add(this.player.standingSprites);
        this.spriteAnim.add(this.player.standingSprites);
        this.spriteAnim.add(this.player.standingSprites);
        this.spriteAnim.add(this.player.standingSprites);
        this.spriteAnim.add(this.player.altMovingSprites);
        this.spriteAnim.add(this.player.altMovingSprites);
        this.spriteAnim.add(this.player.altMovingSprites);
        this.spriteAnim.add(this.player.altMovingSprites);
        this.spriteAnim.add(this.player.standingSprites);
        this.spriteAnim.add(this.player.standingSprites);
    }

    public int getLayer(){return this.layer;}

    public void localStep(Game game) {
        // gb does a weird anim here
         // looked at frame-by-frame (ledge_anim_notes.txt text file)

//        if (this.timer1 % 2 == 0 && this.timer1 < 32) {
//            game.cam.position.y -=2;
//            game.player.position.y -=2;
//        }

        if ( this.timer1 < 32) {
            if (game.player.dirFacing.equals("up")) {
                game.player.position.y +=1;
                game.cam.position.y +=1;
            }
            else if (game.player.dirFacing.equals("down")) {
                game.cam.position.y -=1;
                game.player.position.y -=1;
            }
            else if (game.player.dirFacing.equals("left")) {
                game.player.position.x -=1;
                game.cam.position.x -=1;
            }
            else if (game.player.dirFacing.equals("right")) {
                game.player.position.x +=1;
                game.cam.position.x +=1;
            }

            if (this.timer1 % 2 == 1) {
                game.player.position.y += this.yMovesList.get(0);
                this.yMovesList.remove(0);

                // use next sprite in list
                game.player.currSprite = this.spriteAnim.get(0).get(game.player.dirFacing);
                this.spriteAnim.remove(0);
            }

            // this is needed for batch to draw according to cam
             // always call this after updating camera
            game.cam.update();
            game.mapBatch.setProjectionMatrix(game.cam.combined);

            // old sprite anim code was here

        }
        else {
            game.player.currSprite = game.player.standingSprites.get(game.player.dirFacing);
        }

        // draw shadow
        game.mapBatch.draw(this.shadow, game.cam.position.x-16, game.cam.position.y-4);

        if (this.timer1 >= 38) {
            game.player.position.set(this.targetPos);
            game.cam.position.set(this.targetPos.x+16, this.targetPos.y,0);
            game.player.isNearCampfire = game.player.checkNearCampfire();

            Action playerStanding = new PlayerStanding(game);
            game.insertAction(playerStanding);
            // playerStanding.step(game); // step to detect movement right away
            game.actionStack.remove(this);
        }

        this.timer1++;
    }

    public void remoteStep(Game game) {
        this.player.network.syncTimer++;
        if ( this.timer1 < 32) {
            if (this.player.dirFacing.equals("up")) {
                this.player.position.y +=1;
            }
            else if (this.player.dirFacing.equals("down")) {
                this.player.position.y -=1;
            }
            else if (this.player.dirFacing.equals("left")) {
                this.player.position.x -=1;
            }
            else if (this.player.dirFacing.equals("right")) {
                this.player.position.x +=1;
            }

            if (this.timer1 % 2 == 1) {
                this.player.position.y += this.yMovesList.get(0);
                this.yMovesList.remove(0);
                // use next sprite in list
                this.player.currSprite = this.spriteAnim.get(0).get(this.player.dirFacing);
                this.spriteAnim.remove(0);
            }
        }
        else {
            this.player.currSprite = this.player.standingSprites.get(this.player.dirFacing);
        }
        // draw shadow
        game.mapBatch.draw(this.shadow, this.player.position.x-16, this.player.position.y-4);
        if (this.timer1 >= 38) {
            this.player.position.set(this.targetPos);
//            Action playerStanding = new playerStanding(game, this.player, true, false);
            this.player.standingAction.alternate = true;
            this.player.standingAction.isRunning = false;
            game.insertAction(this.player.standingAction);
            game.actionStack.remove(this);
        }
        this.timer1++;
    }

    @Override
    public void step(Game game) {
        if (this.player.type == Player.Type.LOCAL) {
            this.localStep(game);
        }
        else if (this.player.type == Player.Type.REMOTE) {
            this.remoteStep(game);
        }
    }
}

class PlayerLedgeJumpFast extends Action {
    public int layer = 131;
    float xDist, yDist;

    Vector2 initialPos, targetPos;

    Sprite shadow;

    int timer1 = 0;

    ArrayList<Integer> yMovesList = new ArrayList<Integer>();

    ArrayList<Map<String, Sprite>> spriteAnim = new ArrayList<Map<String, Sprite>>();
    // Map<String, ArrayList<Sprite>> spritesAnimList = new HashMap<String, ArrayList<Sprite>>();
    Player player;
    public PlayerLedgeJumpFast(Game game) {
        this(game, game.player);
    }

    public PlayerLedgeJumpFast(Game game, Player player) {
        this.player = player;
        this.initialPos = new Vector2(this.player.position);
        if (this.player.dirFacing.equals("up")) {
            this.targetPos = new Vector2(this.player.position.x, this.player.position.y+16);
        }
        else if (this.player.dirFacing.equals("down")) {
            this.targetPos = new Vector2(this.player.position.x, this.player.position.y-16);
        }
        else if (this.player.dirFacing.equals("left")) {
            this.targetPos = new Vector2(this.player.position.x-16, this.player.position.y);
        }
        else if (this.player.dirFacing.equals("right")) {
            this.targetPos = new Vector2(this.player.position.x+16, this.player.position.y);
        }

        // shadow sprite
        Texture shadowText = new Texture(Gdx.files.internal("shadow1.png"));
        this.shadow = new Sprite(shadowText, 0, 0, 16, 16);

        // Play ledge jumping sound
        game.insertAction(new PlaySound("ledge2", null));

        // below two lists are used to get exact sprite and
         // y movement on every other frame
        this.yMovesList.add(4);
//        this.yMovesList.add(2);
        this.yMovesList.add(2);
//        this.yMovesList.add(2);
        this.yMovesList.add(1);
//        this.yMovesList.add(1);
        this.yMovesList.add(0);
//        this.yMovesList.add(0);
        this.yMovesList.add(-1);
//        this.yMovesList.add(-1);
        this.yMovesList.add(-1);
//        this.yMovesList.add(-1);
        this.yMovesList.add(-2);
//        this.yMovesList.add(-3);
        this.yMovesList.add(-3);
//        this.yMovesList.add(0);

        // sprites to use (according to frame-by-frame)
        this.spriteAnim.add(this.player.standingSprites);
//        this.spriteAnim.add(this.player.standingSprites);
        this.spriteAnim.add(this.player.movingSprites);
//        this.spriteAnim.add(this.player.movingSprites);
        this.spriteAnim.add(this.player.movingSprites);
//        this.spriteAnim.add(this.player.movingSprites);
        this.spriteAnim.add(this.player.standingSprites);
//        this.spriteAnim.add(this.player.standingSprites);
        this.spriteAnim.add(this.player.standingSprites);
//        this.spriteAnim.add(this.player.standingSprites);
        this.spriteAnim.add(this.player.altMovingSprites);
//        this.spriteAnim.add(this.player.altMovingSprites);
        this.spriteAnim.add(this.player.altMovingSprites);
//        this.spriteAnim.add(this.player.altMovingSprites);
        this.spriteAnim.add(this.player.standingSprites);
//        this.spriteAnim.add(this.player.standingSprites);
    }

    public int getLayer(){return this.layer;}

    public void localStep(Game game) {
//        if (this.timer1 % 2 == 0 && this.timer1 < 32) {
//            game.cam.position.y -=2;
//            game.player.position.y -=2;
//        }

        if ( this.timer1 < 16) {
            if (game.player.dirFacing.equals("up")) {
                game.player.position.y +=1;
                game.cam.position.y +=1;
            }
            else if (game.player.dirFacing.equals("down")) {
                game.cam.position.y -=1;
                game.player.position.y -=1;
            }
            else if (game.player.dirFacing.equals("left")) {
                game.player.position.x -=1;
                game.cam.position.x -=1;
            }
            else if (game.player.dirFacing.equals("right")) {
                game.player.position.x +=1;
                game.cam.position.x +=1;
            }
            if (this.timer1 % 2 == 1) {
                game.player.position.y += this.yMovesList.get(0);
                this.yMovesList.remove(0);

                // use next sprite in list
                game.player.currSprite = this.spriteAnim.get(0).get(game.player.dirFacing);
                this.spriteAnim.remove(0);
            }

            // this is needed for batch to draw according to cam
             // always call this after updating camera
            game.cam.update();
            game.mapBatch.setProjectionMatrix(game.cam.combined);

            // old sprite anim code was here

        }
        else {
            game.player.currSprite = game.player.standingSprites.get(game.player.dirFacing);
        }
        // Draw shadow
        game.mapBatch.draw(this.shadow, game.cam.position.x-16, game.cam.position.y-4);

        if (this.timer1 >= 19) {
//            Vector2 diff = this.targetPos.cpy().sub(game.player.position);
//            Tile currTile = game.map.tiles.get(this.targetPos);
//            Tile temp = game.map.tiles.get(this.targetPos.cpy().add(diff));

            game.player.position.set(this.targetPos);
            game.cam.position.set(this.targetPos.x+16, this.targetPos.y, 0);
            game.player.isNearCampfire = game.player.checkNearCampfire();

//            if (currTile != null && temp != null && currTile.attrs.get("ledge")) {
//                game.insertAction(new PlayerLedgeJumpFast(game, this.player));
//            }
//            else {
//                Action playerStanding = new playerStanding(game);
//                game.insertAction(playerStanding);
//            }
            Action playerStanding = new PlayerStanding(game);
            game.insertAction(playerStanding);
            game.actionStack.remove(this);

        }

        this.timer1++;
    }

    public void remoteStep(Game game) {
        this.player.network.syncTimer++;
        if ( this.timer1 < 16) {
            if (this.player.dirFacing.equals("up")) {
                this.player.position.y +=1;
            }
            else if (this.player.dirFacing.equals("down")) {
                this.player.position.y -=1;
            }
            else if (this.player.dirFacing.equals("left")) {
                this.player.position.x -=1;
            }
            else if (this.player.dirFacing.equals("right")) {
                this.player.position.x +=1;
            }

            if (this.timer1 % 2 == 1) {
                this.player.position.y += this.yMovesList.get(0);
                this.yMovesList.remove(0);
                // use next sprite in list
                this.player.currSprite = this.spriteAnim.get(0).get(this.player.dirFacing);
                this.spriteAnim.remove(0);
            }
        }
        else {
            this.player.currSprite = this.player.standingSprites.get(this.player.dirFacing);
        }
        // draw shadow
        game.mapBatch.draw(this.shadow, this.player.position.x-16, this.player.position.y-4);
        if (this.timer1 >= 19) {
//            Vector2 diff = this.targetPos.cpy().sub(game.player.position);
//            Tile currTile = game.map.tiles.get(this.targetPos);
//            Tile temp = game.map.tiles.get(this.targetPos.cpy().add(diff));
            this.player.position.set(this.targetPos);
//            if (currTile != null && temp != null && currTile.attrs.get("ledge")) {
//                game.insertAction(new PlayerLedgeJumpFast(game, this.player));
//            }
//            else {
//                this.player.standingAction.alternate = true;
//                this.player.standingAction.isRunning = false;
//                game.insertAction(this.player.standingAction);
//            }
            this.player.standingAction.alternate = true;
            this.player.standingAction.isRunning = false;
            game.insertAction(this.player.standingAction);
            game.actionStack.remove(this);
        }
        this.timer1++;
    }

    @Override
    public void step(Game game) {
        if (this.player.type == Player.Type.LOCAL) {
            this.localStep(game);
        }
        else if (this.player.type == Player.Type.REMOTE) {
            this.remoteStep(game);
        }
    }
}

// draw character action
// need to build in 'button press delay'
class PlayerMoving extends Action {
    public int layer = 150;
    Vector2 initialPos; // track distance of movement

    Vector2 targetPos;
    float xDist, yDist;
    // float speed = 50.0f;

    boolean alternate = false;

    Player player;
    public PlayerMoving(Game game, boolean alternate) {
        this(game, game.player, alternate);
    }

    public PlayerMoving(Game game, Player player, boolean alternate) {
        this(game, player, alternate, new PlayerStanding(game, !alternate));
    }

    public PlayerMoving(Game game, Player player, boolean alternate, Action nextAction) {
        this.nextAction = nextAction;
        this.alternate = alternate;
        this.player = player;
    }

    @Override
    public void firstStep(Game game) {
        this.initialPos = new Vector2(this.player.position);
        if (this.player.dirFacing.equals("up")) {
            this.targetPos = new Vector2(this.player.position.x, this.player.position.y+16);
        }
        else if (this.player.dirFacing.equals("down")) {
            this.targetPos = new Vector2(this.player.position.x, this.player.position.y-16);
        }
        else if (this.player.dirFacing.equals("left")) {
            this.targetPos = new Vector2(this.player.position.x-16, this.player.position.y);
        }
        else if (this.player.dirFacing.equals("right")) {
            this.targetPos = new Vector2(this.player.position.x+16, this.player.position.y);
        }
    }

    // changed, was 130
    // alternative is to call cam.update(blah) each draw thingy, but
    // i think that's less optimal. this action needs to happen before everything else
    public int getLayer(){return this.layer;}

    public void localStep(Game game) {
        // allows game to pause in middle of run
        if (game.playerCanMove == false) {
            return;
        }
        // can consider doing skipping here if I need to slow down animation
        // bug - have to add 1 to cam position at beginning of each iteration.
         // probably related to occasionaly shakiness, which is probably related to floats

        // while you haven't moved 16 pixels,
         // move in facing direction

        if (game.player.dirFacing.equals("up")) {
            game.player.position.y +=1;
            game.cam.position.y +=1;
        }
        else if (game.player.dirFacing.equals("down")) {
            game.player.position.y -=1;
            game.cam.position.y -=1;
        }
        else if (game.player.dirFacing.equals("left")) {
            game.player.position.x -=1;
            game.cam.position.x -=1;
        }
        else if (game.player.dirFacing.equals("right")) {
            game.player.position.x +=1;
            game.cam.position.x +=1;
        }

        this.xDist = Math.abs(this.initialPos.x - game.player.position.x);
        this.yDist = Math.abs(this.initialPos.y - game.player.position.y);

        // this is needed for batch to draw according to cam
        game.cam.update();
        game.mapBatch.setProjectionMatrix(game.cam.combined);

        // if u remove the below check, youll notice that there's a bit of
         // movement that you don't want
        if ((this.yDist < 13 && this.yDist > 2)
            || (this.xDist < 13 && this.xDist > 2)) {
            if (this.alternate == true) {
                // game.batch.draw(game.player.altMovingSprites.get(game.player.dirFacing), game.player.position.x, game.player.position.y);
                game.player.currSprite = game.player.altMovingSprites.get(game.player.dirFacing);
            }
            else {
                // game.batch.draw(game.player.movingSprites.get(game.player.dirFacing), game.player.position.x, game.player.position.y);
                game.player.currSprite = game.player.movingSprites.get(game.player.dirFacing);
            }
        }
        else {
            // game.batch.draw(game.player.standingSprites.get(game.player.dirFacing), game.player.position.x, game.player.position.y);
            game.player.currSprite = game.player.standingSprites.get(game.player.dirFacing);
        }

        // System.out.println("Debug: " + String.valueOf(game.player.position.y));

        // when we've moved 16 pixels
        // if button pressed, change dir and move that direction
        // else, stand still again
        if (this.xDist >= 16 || this.yDist >= 16) {
            // TODO: routes are persistent, ie never regenerate
            // TBD if that needs to change
            // TODO: that will be a problem if game has to load all pokemon sprites on the map
            if (game.map.tiles.get(this.targetPos) != null && game.map.tiles.get(this.targetPos).routeBelongsTo != null) {
                Route newRoute = game.map.tiles.get(this.targetPos).routeBelongsTo;
                String newBiome = game.map.tiles.get(this.targetPos).biome;
                if (game.map.currRoute != newRoute) {
                    // TODO: testing
                    // only fade to new music if route is different name
                    // ie, don't fade if going from forest1->forest1, but
                    // do fade if going from forest1->snow1
                    if (game.map.currRoute.name.equals(newRoute.name)) {
                        newRoute.music = game.map.currRoute.music;
                    }
                    else {
                        // load new music
                        // fade music
                        // TODO: this will switch music each time moving to new route
                        //  didn't really like, although might be necessary for some routes
                        //  ie, mountains, beach, forest etc.
//                        String nextMusicName = newRoute.getNextMusic(true);
//                        Action nextMusic = new FadeMusic("currMusic", "out", "", .025f,
//                                           new WaitFrames(Game.staticGame, 10,
//                                           new FadeMusic(nextMusicName, "in", "", .2f, true, 1f, game.musicCompletionListener, null)));
//                        game.insertAction(nextMusic);
//                        nextMusic.step(game);
                        newRoute.music = game.map.currRoute.music;  // TODO: comment this if reverting the above
                        System.out.println("New Route: " + newRoute.name);
                    }
                    game.map.currRoute = newRoute;
                }
                game.map.currBiome = newBiome;
//                System.out.println("New Biome: " + newBiome);
            }
            game.player.position.set(this.targetPos);
            game.player.isNearCampfire = game.player.checkNearCampfire();
            game.cam.position.set(this.targetPos.x+16, this.targetPos.y,0);
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
            this.nextAction.step(game); // decide where to move // doesn't actually seem to do much
        }
    }

    public void remoteStep(Game game) {
        this.player.network.syncTimer++;
        // allows game to pause in middle of run
        if (this.player.dirFacing.equals("up")) {
            this.player.position.y +=1;
        }
        else if (this.player.dirFacing.equals("down")) {
            this.player.position.y -=1;
        }
        else if (this.player.dirFacing.equals("left")) {
            this.player.position.x -=1;
        }
        else if (this.player.dirFacing.equals("right")) {
            this.player.position.x +=1;
        }
        this.xDist = Math.abs(this.initialPos.x - this.player.position.x);
        this.yDist = Math.abs(this.initialPos.y - this.player.position.y);
        // if u remove the below check, youll notice that there's a bit of
         // movement that you don't want
        if (    (this.yDist < 13 && this.yDist > 2)
            || (this.xDist < 13 && this.xDist > 2)) {
            if (this.alternate) {
                this.player.currSprite = this.player.altMovingSprites.get(this.player.dirFacing);
            }
            else {
                this.player.currSprite = this.player.movingSprites.get(this.player.dirFacing);
            }
        }
        else {
            this.player.currSprite = this.player.standingSprites.get(this.player.dirFacing);
        }
        if (this.xDist >= 16 || this.yDist >= 16) {
//            // this.player.currRoute keeps track of which route
//            if (game.map.tiles.get(this.targetPos) != null && game.map.tiles.get(this.targetPos).routeBelongsTo != null) {

            this.player.position.set(this.targetPos);
//            Action standingAction = new playerStanding(game, this.player, !this.alternate, false);
            this.player.standingAction.alternate = !this.alternate;
            this.player.standingAction.isRunning = false;
            game.insertAction(this.player.standingAction);
            this.player.standingAction.step(game);
            game.actionStack.remove(this);
        }
    }

    @Override
    public void step(Game game) {
        if (this.player.type == Player.Type.LOCAL) {
            this.localStep(game);
        }
        else if (this.player.type == Player.Type.REMOTE) {
            this.remoteStep(game);
        }
    }
}

// note - this action is nearly identical to playerMoving. Keeping separate though,
 // for simplicity
 // differences are - player sprites, movement speed
class PlayerRunning extends Action {
    public int layer = 150;
    Vector2 initialPos; // track distance of movement

    Vector2 targetPos;
    float xDist, yDist;
    // float speed = 50.0f;

    boolean alternate = false;

    Player player;
    public PlayerRunning(Game game, boolean alternate) {
        this(game, game.player, alternate);
    }

    public PlayerRunning(Game game, Player player, boolean alternate) {
        this.alternate = alternate;
        this.player = player;

        this.initialPos = new Vector2(this.player.position);
        if (this.player.dirFacing.equals("up")) {
            this.targetPos = new Vector2(this.player.position.x, this.player.position.y+16);
        }
        else if (this.player.dirFacing.equals("down")) {
            this.targetPos = new Vector2(this.player.position.x, this.player.position.y-16);
        }
        else if (this.player.dirFacing.equals("left")) {
            this.targetPos = new Vector2(this.player.position.x-16, this.player.position.y);
        }
        else if (this.player.dirFacing.equals("right")) {
            this.targetPos = new Vector2(this.player.position.x+16, this.player.position.y);
        }
        this.player.currState = "Running";
    }

    // changed, was 130
    // alternative is to call cam.update(blah) each draw thingy, but
    // i think that's less optimal. this action needs to happen before everything else
    public int getLayer(){return this.layer;}

    public void localStep(Game game) {
        // allows game to pause in middle of run
        if (game.playerCanMove == false) {
            return;
        }
        // can consider doing skipping here if I need to slow down animation
        // bug - have to add 1 to cam position at beginning of each iteration.
         // probably related to occasionaly shakiness, which is probably related to floats

        // while you haven't moved 16 pixels,
         // move in facing direction

        float speed = 1.6f; // this needs to add up to 16 for smoothness?

        if (game.player.dirFacing.equals("up")) {
            this.player.position.y +=speed;
            game.cam.position.y +=speed;
        }
        else if (game.player.dirFacing.equals("down")) {
            game.player.position.y -=speed;
            game.cam.position.y -=speed;
        }
        else if (game.player.dirFacing.equals("left")) {
            game.player.position.x -=speed;
            game.cam.position.x -=speed;
        }
        else if (game.player.dirFacing.equals("right")) {
            game.player.position.x +=speed;
            game.cam.position.x +=speed;
        }

        this.xDist = Math.abs(this.initialPos.x - game.player.position.x);
        this.yDist = Math.abs(this.initialPos.y - game.player.position.y);

        // this is needed for batch to draw according to cam
        game.cam.update();
        game.mapBatch.setProjectionMatrix(game.cam.combined);

        // if u remove the below check, youll notice that there's a bit of
         // movement that you don't want
        String spriteString = String.valueOf(game.player.dirFacing+"_running");
        // System.out.println("spriteString: " + String.valueOf(spriteString)); // debug
        if (    (this.yDist < 13 && this.yDist > 2)
            || (this.xDist < 13 && this.xDist > 2)) {
            if (this.alternate == true) {
                // game.batch.draw(game.player.altMovingSprites.get(game.player.dirFacing), game.player.position.x, game.player.position.y);
                game.player.currSprite = game.player.altMovingSprites.get(spriteString);
            }
            else {
                // game.batch.draw(game.player.movingSprites.get(game.player.dirFacing), game.player.position.x, game.player.position.y);
                game.player.currSprite = game.player.movingSprites.get(spriteString);
            }
        }
        else {
            // game.batch.draw(game.player.standingSprites.get(game.player.dirFacing), game.player.position.x, game.player.position.y);
            game.player.currSprite = game.player.standingSprites.get(spriteString);
        }

        // when we've moved 16 pixels
        // if button pressed, change dir and move that direction
        // else, stand still again
        if (this.xDist >= 16 || this.yDist >= 16) {
            if (game.map.tiles.get(this.targetPos) != null && game.map.tiles.get(this.targetPos).routeBelongsTo != null) {
                Route newRoute = game.map.tiles.get(this.targetPos).routeBelongsTo;
                String newBiome = game.map.tiles.get(this.targetPos).biome;
                if (game.map.currRoute != newRoute) {
                    // TODO: testing
                    // only fade to new music if route is different name
                    // ie, don't fade if going from forest1->forest1, but
                    // do fade if going from forest1->snow1
                    if (game.map.currRoute.name.equals(newRoute.name)) {
                        newRoute.music = game.map.currRoute.music;
                    }
                    else {
                        // load new music
                        // fade music
//                        String nextMusicName = newRoute.getNextMusic(true);
//                        Action nextMusic = new FadeMusic("currMusic", "out", "", .025f,
//                                           new WaitFrames(Game.staticGame, 10,
//                                           new FadeMusic(nextMusicName, "in", "", .2f, true, 1f, game.musicCompletionListener, null)));
//                        game.insertAction(nextMusic);
//                        nextMusic.step(game);
                        newRoute.music = game.map.currRoute.music;  // TODO: comment this if reverting the above
                    }
                    game.map.currRoute = newRoute;
                }
                game.map.currBiome = newBiome;
            }

            game.player.position.set(this.targetPos);
            game.cam.position.set(this.targetPos.x+16, this.targetPos.y,0);
            game.cam.update();                                     // this line fixes jittering bug
            game.mapBatch.setProjectionMatrix(game.cam.combined);    // same

            game.player.isNearCampfire = game.player.checkNearCampfire();
            Action standingAction = new PlayerStanding(game, !this.alternate, true); // pass true to keep running animation going
            game.insertAction(standingAction);
            standingAction.step(game); // decide where to move // doesn't actually seem to do much
            game.actionStack.remove(this);
        }
    }

    public void remoteStep(Game game) {
        this.player.network.syncTimer++;
        // allows player to pause in middle of run
        float speed = 1.6f; // this needs to add up to 16 for smoothness?

        if (this.player.dirFacing.equals("up")) {
            this.player.position.y += speed;
        }
        else if (this.player.dirFacing.equals("down")) {
            this.player.position.y -= speed;
        }
        else if (this.player.dirFacing.equals("left")) {
            this.player.position.x -= speed;
        }
        else if (this.player.dirFacing.equals("right")) {
            this.player.position.x += speed;
        }

        this.xDist = Math.abs(this.initialPos.x - this.player.position.x);
        this.yDist = Math.abs(this.initialPos.y - this.player.position.y);

        // if u remove the below check, you'll notice that there's a bit of
         // movement that you don't want
        String spriteString = String.valueOf(this.player.dirFacing+"_running");
        // System.out.println("spriteString: " + String.valueOf(spriteString)); // debug
        if (    (this.yDist < 13 && this.yDist > 2)
            || (this.xDist < 13 && this.xDist > 2)) {
            if (this.alternate) {
                this.player.currSprite = this.player.altMovingSprites.get(spriteString);
            }
            else {
                this.player.currSprite = this.player.movingSprites.get(spriteString);
            }
        }
        else {
            this.player.currSprite = this.player.standingSprites.get(spriteString);
        }
        if (this.xDist >= 16 || this.yDist >= 16) {
            this.player.position.set(this.targetPos);
//            Action standingAction = new playerStanding(game, this.player, !this.alternate, true); // pass true to keep running animation going
            this.player.standingAction.alternate = !this.alternate;
            this.player.standingAction.isRunning = true;
            game.insertAction(this.player.standingAction);
            this.player.standingAction.step(game); // decide where to move // doesn't actually seem to do much
            game.actionStack.remove(this);
        }
    }

    @Override
    public void step(Game game) {
        if (this.player.type == Player.Type.LOCAL) {
            this.localStep(game);
        }
        else if (this.player.type == Player.Type.REMOTE) {
            this.remoteStep(game);
        }
    }
}

/**
 * This action is basically the decision-maker for
 * what to do next when a player presses a button.
 * All moving Actions come back to here.
 */
class PlayerStanding extends Action {
    public int layer = 130;
    // TODO: is this used.
    public float initialWait; // might use this to wait before moving
    boolean alternate;
    boolean checkWildEncounter = true; // TODO - remove when playWait is implemented
    boolean isRunning;
    Player player;
    // TODO: doesn't need to be public-static when local PlayerStanding keeps track of this.player
    public static int moveTimer = 0;  // used to prevent player from moving until held down key long enough

    /*
     * TODO: correct constructors, ie this one should call this(game, something); instead.
     */
    public PlayerStanding(Game game) {
        // could snap cam, and have playerMoving come here after 15 pixels. saves a little code
         // problems - timer before moving, alternating sprites
        this.alternate = true;
        this.checkWildEncounter = false;
        this.isRunning = false;
        this.player = game.player;
    }

    public PlayerStanding(Game game, boolean alternate) {
        // only used by playerMoving atm
        this.alternate = alternate;
        this.isRunning = false;
        this.player = game.player;
        // todo - might be able to remove above alternate code, should work atm. after 1 iter this.alternate = false, init to true
    }

    public PlayerStanding(Game game, boolean alternate, boolean isRunning) {
        // only used by playerMoving atm
        this.alternate = alternate;
        this.isRunning = isRunning;
        this.player = game.player;
        // todo - might be able to remove above alternate code, should work atm. after 1 iter this.alternate = false, init to true
    }

    public PlayerStanding(Game game, Player player, boolean alternate, boolean isRunning) {
        this.alternate = alternate;
        this.isRunning = isRunning;
        this.checkWildEncounter = false;
        this.player = player;
    }

    boolean checkWildEncounter(Game game) {
        Pokemon pokemon = this.checkWildEncounter(game, game.player.position);
        if (pokemon != null) {
            game.battle.oppPokemon = pokemon;
        }
        return pokemon != null;
    }

    // when moving to tile, no chance of encounter unless continuing to move
     // i think the real game uses an encounter table or something
    Pokemon checkWildEncounter(Game game, Vector2 position) {
        // no encounters at night (subject to change)
        // TODO: need to enable this. shaders shouldn't be active on floatingbatch so should be fine. not sure abt ghost
        // timer though.
        // TODO: test without.
//        if (game.map.timeOfDay == "Night") {
//            return null;
//        }

        Tile currTile = game.map.tiles.get(position);

        if (currTile != null && currTile.routeBelongsTo != null) {
            // if currently on grass
            if (currTile.attrs.get("grass") == true) {
                // chance wild encounter
                int randomNum = game.map.rand.nextInt(100) + 1; // rate determine by player? // 1 - 100
                if (randomNum < 20) { // encounterRate // was <= 50
                    // disable player movement
                    // game.actionStack.remove(this); // using flag now, delete this

                    // get list of pokemon not in battle
                    ArrayList<Pokemon> notInBattle = new ArrayList<Pokemon>();
//                    for (Pokemon pokemon : game.map.currRoute.pokemon) {  // TODO: remove
                    for (Pokemon pokemon : currTile.routeBelongsTo.pokemon) {
                        if (!pokemon.inBattle) {
                            notInBattle.add(pokemon);
                        }
                    }
                    // if all pokemon are in battle, return null for now.
                    if (notInBattle.size() <= 0) {
                        return null;
                    }
                    // select new pokemon to encounter, put it in battle struct
                    int index = game.map.rand.nextInt(notInBattle.size());
                    // assume pokemon is getting put into a battle
                    Pokemon pokemon = notInBattle.get(index);
                    pokemon.inBattle = true;
                    return pokemon;
                }
            }
        }
        return null;
    }

    /**
     * Check all tiles next to currTile for a completed house.
     * If house is complete, change door to solid=false so that player can enter.
     */
    public void detectIsHouseBuilt(Game game, Tile currTile) {
        Vector2 pos = currTile.position.cpy();
        // detect if house is fully built or not
        if (currTile.nameUpper.contains("house")) {
            Tile startTile = currTile;
            Tile nextTile = currTile;
            Tile doorTile = null;
            // detect if there are house tiles in between
            boolean found = true;
            while (true) {
                if (nextTile.nameUpper.contains("middle") && !nextTile.nameUpper.contains("roof")) {
                    currTile = game.map.tiles.get(pos.add(0, 16));
                    while (!currTile.nameUpper.contains("middle")) {
                        if (!currTile.nameUpper.contains("house")) {
                            found = false;
                            break;
                        }
                        currTile = game.map.tiles.get(pos.add(0, 16));
                    }
                }
                if (nextTile.nameUpper.contains("door")) {
                    doorTile = nextTile;
                }
                // TODO: remove
//                if (nextTile.name.contains("middle") && !nextTile.name.contains("roof") && doorTile == null) {
//                    doorTile = nextTile;
//                }
                if (!found || !nextTile.nameUpper.contains("house")) {
                    found = false;
                    break;
                }
                if (nextTile.nameUpper.contains("roof")) {
                    if (nextTile.nameUpper.contains("right")) {
                        nextTile = game.map.tiles.get(nextTile.position.cpy().add(0, -16));
                    }
                    else {
                        nextTile = game.map.tiles.get(nextTile.position.cpy().add(16, 0));
                    }
                }
                else {
                    if (nextTile.nameUpper.contains("left")) {
                        nextTile = game.map.tiles.get(nextTile.position.cpy().add(0, 16));
                    }
                    else {
                        nextTile = game.map.tiles.get(nextTile.position.cpy().add(-16, 0));
                    }
                }
                if (nextTile.equals(startTile)) {
                    break;
                }
            }
            // if solid house, apply interior
            if (found) {
                if (doorTile != null) {
                    if (doorTile.nameUpper.contains("house1")) {
                        game.map.tiles.put(doorTile.position, new Tile("house1_door1", doorTile.position.cpy()));
                    }
                }
            }
            else if (doorTile != null){
                // replace door tile with dark door tile (?)
                game.map.tiles.put(doorTile.position, new Tile("house1_door1_dark", doorTile.position.cpy()));
            }
        }
    }

    /**
     * TODO: move this to map, non-static function.
     * Fix surrounding tiles based on the tile just added
     * Ex: fence tile touching another fence above and below
     */
    public static void adjustSurroundingTiles(Tile currTile) {
        Game game = Game.staticGame;
        Vector2 pos = new Vector2();
        Tile tile;
        for (int i = -16; i < 17; i+=16) {
            for (int j = -16; j < 17; j+=16) {
                pos.set(currTile.position.cpy().add(i, j));
                tile = game.map.tiles.get(pos);
                PlayerStanding.adjustTile(tile);
            }
        }
    }

    /**
     * Find which sides surrounded on. Replace overSprite based on that.
     * Ex: fence surrounded by 4 fence posts becomes a 4-cross fence.
     */
    public static void adjustTile(Tile currTile) {
        Game game = Game.staticGame;
        Tile up = game.map.tiles.get(currTile.position.cpy().add(0, 16));
        Tile down = game.map.tiles.get(currTile.position.cpy().add(0, -16));
        Tile right = game.map.tiles.get(currTile.position.cpy().add(16, 0));
        Tile left = game.map.tiles.get(currTile.position.cpy().add(-16, 0));
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
        boolean touchUp = up.nameUpper.contains(name);
        boolean touchDown = down.nameUpper.contains(name);
        boolean touchRight = right.nameUpper.contains(name);
        boolean touchLeft = left.nameUpper.contains(name);
        String ext = "_";
        if (currTile.nameUpper.contains("fence") && !currTile.nameUpper.contains("house")) {
            // bend fences towards houses?
            if (up.nameUpper.contains("house") && !up.nameUpper.contains("roof")) {
                touchUp = true;
            }
            if (down.nameUpper.contains("house") && !down.nameUpper.contains("roof")) {
                touchDown = true;
            }
            if (left.nameUpper.contains("house") && !left.nameUpper.contains("roof")) {
                touchLeft = true;
            }
            if (right.nameUpper.contains("house") && !right.nameUpper.contains("roof")) {
                touchRight = true;
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
            game.map.tiles.put(currTile.position.cpy(),
                               new Tile(currTile.name, name+ext, 
                                        currTile.position.cpy(), true, currTile.routeBelongsTo));
    //        text = TextureCache.get(Gdx.files.internal(path+currTile.nameUpper+ext+".png"));
    //        Vector2 pos = new Vector2(currTile.overSprite.getX(), currTile.overSprite.getY());
    //        currTile.overSprite = new Sprite(text, 0, 0, 16, 16);
    //        currTile.overSprite.setPosition(pos.x, pos.y);
        }
        else if (currTile.nameUpper.contains("house")
                 && !currTile.nameUpper.contains("roof")
                 && !currTile.nameUpper.contains("door")) {
            if (currTile.nameUpper.equals("house5_middle1") && down.nameUpper.contains("fence1") &&
                !(touchUp ^ touchDown)) {
                game.map.tiles.put(currTile.position.cpy(),
                                   new Tile(currTile.name, "house5_middle1_fence1", 
                                            currTile.position.cpy(), true, currTile.routeBelongsTo));
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
            else if (touchLeft &&touchDown) {
                ext += "W";
            }
            else if (touchRight) {
                ext += "left1";
            }
            else if (touchLeft) {
                ext += "right1";
            }
            game.map.tiles.put(currTile.position.cpy(),
                               new Tile(currTile.name, name+ext, 
                                        currTile.position.cpy(), true, currTile.routeBelongsTo));
            // Add to interiors
            Tile interiorTile = new Tile("house5_floor1", currTile.position.cpy());
            game.map.interiorTiles.get(game.map.interiorTilesIndex).put(currTile.position.cpy(), interiorTile);
        }
        else if (currTile.nameUpper.contains("house") && currTile.nameUpper.contains("roof")) {
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
            game.map.tiles.put(currTile.position.cpy(),
                               new Tile(currTile.name, name+ext, 
                                        currTile.position.cpy(), true, currTile.routeBelongsTo));
            // Add to interiors
            HashMap<Vector2, Tile> currInterior = game.map.interiorTiles.get(game.map.interiorTilesIndex);
            Tile interiorTile = new Tile("house5_floor1", currTile.position.cpy());
            currInterior.put(currTile.position.cpy(), interiorTile);
            if (!currInterior.containsKey(up.position) || currInterior.get(up.position) == null) {
                currInterior.put(up.position.cpy(), new Tile("house5_wall1", up.position.cpy()));
            }
        }
        else if (currTile.nameUpper.contains("door")) {
            // handle case where player puts a door on the roof, which should
            // make a 'back door' (red carpet on back of house that acts as door).
            if ((left.nameUpper.contains("roof") || left.nameUpper.contains("door")) &&
                (right.nameUpper.contains("roof") || right.nameUpper.contains("door"))) {
                game.map.tiles.put(currTile.position.cpy(),
                                   new Tile(currTile.name, "house5_roof_middle1", 
                                            currTile.position.cpy(), true, currTile.routeBelongsTo));
                PlayerStanding.adjustSurroundingTiles(currTile);
                if (!up.attrs.get("solid")) {
                    game.map.tiles.put(up.position.cpy(),
                                       new Tile("rug2", "", up.position.cpy(), true, up.routeBelongsTo));
                    // Add to interiors
                    Tile interiorTile = new Tile("house5_door1", up.position.cpy());
                    game.map.interiorTiles.get(game.map.interiorTilesIndex).put(up.position.cpy(), interiorTile);
                }
                return;
            }
            // Add to interiors
            Tile interiorTile = new Tile("house5_floor_rug1", currTile.position.cpy());
            game.map.interiorTiles.get(game.map.interiorTilesIndex).put(currTile.position.cpy(), interiorTile);
        }
    }

    public void localStep(Game game) {
        if (!game.playerCanMove) {
            return;
        }

        boolean shouldMove = false;
        Vector2 newPos = new Vector2();

        // Check wild encounter
        if (this.checkWildEncounter && game.type != Game.Type.CLIENT) {
            if (this.checkWildEncounter(game) == true) {
                game.playerCanMove = false;
                if (game.map.unownSpawn == null) {
                    // if night, no music transition
                    if (!game.map.timeOfDay.equals("Night")) {
                        game.currMusic.pause();
                        game.currMusic = game.battle.music; 
//                        game.battle.music2.stop();
//                        game.battle.music2.setVolume(0.3f);
                        game.battle.music.stop();
                        game.battle.music.setVolume(0.3f);
//                        game.battle.music.setVolume(0.3f);
                        BattleFadeOutMusic.stop = true;
                        FadeMusic.pause = true;
                        // TODO: debug, remove
//                        game.currMusic.play();
//                        game.currMusic.pause();
//                        game.currMusic.setPosition(11f);  
                        game.currMusic.play();
//                        game.insertAction(new FadeMusic("currMusic", "in", "", 1f, false, game.battle.music.getVolume(), null));
                    }
                }
                else {
                    String unownLetter = game.map.unownUsed.get(game.map.rand.nextInt(game.map.unownUsed.size()));
                    game.map.unownUsed.remove(unownLetter);
                    game.battle.oppPokemon = new Pokemon("unown_"+unownLetter, 13, Pokemon.Generation.CRYSTAL);
                }
                // The first Pokemon the player sends out in battle should
                // have >0 hp.
                for (Pokemon currPokemon : this.player.pokemon) {
                    if (currPokemon.currentStats.get("hp") > 0) {
                        this.player.currPokemon = currPokemon;
                        break;
                    }
                }
                game.insertAction(Battle.getIntroAction(game));
                this.checkWildEncounter = false;
                return;
            }
            this.checkWildEncounter = false;
        }
        // else, check if the server sent an encounter
        else if (this.player.network.doEncounter != null) {
            // The first Pokemon the player sends out in battle should
            // have >0 hp.
            for (Pokemon currPokemon : this.player.pokemon) {
                if (currPokemon.currentStats.get("hp") > 0) {
                    this.player.currPokemon = currPokemon;
                    break;
                }
            }
            game.playerCanMove = false;
            Network.PokemonData pokemonData = this.player.network.doEncounter.pokemonData;
            game.battle.oppPokemon = new Pokemon(pokemonData.name,
                                                 pokemonData.level,
                                                 pokemonData.generation);
            game.battle.oppPokemon.currentStats.put("hp", pokemonData.hp);
            game.insertAction(Battle.getIntroAction(game));
            if (!game.map.timeOfDay.equals("Night")) {
                game.currMusic.pause();
                game.currMusic = game.battle.music;
                game.battle.music.stop();
                game.battle.music.setVolume(0.3f);
                game.currMusic.play();
                BattleFadeOutMusic.stop = true;
                FadeMusic.pause = true;
            }
            this.checkWildEncounter = false;
            this.player.network.doEncounter = null;
            return;
        }
        // If player is standing on a door, stop moving and move player to interior.
        if (game.map.tiles.get(game.player.position) != null &&
            (game.map.tiles.get(game.player.position).nameUpper.contains("door") ||
             game.map.tiles.get(game.player.position).name.contains("door"))) {
            game.playerCanMove = false;
            if (game.map.tiles == game.map.overworldTiles) {
                game.insertAction(new EnterBuilding(game, 
                                  new SetField(game, "playerCanMove", true,
                                  null)));
            }
            else {
                game.insertAction(new EnterBuilding(game, "exit",
                                  new SetField(game, "playerCanMove", true,
                                  null)));
            }
            return;
        }
        // Check if player wants to access the menu
        if (InputProcessor.startJustPressed) {
            game.insertAction(new DrawPlayerMenu.Intro(game,
                              new DrawPlayerMenu(game,
                              new WaitFrames(game, 1,
                              new PlayerCanMove(game,
                              null)))));
            // game.actionStack.remove(this);
            game.playerCanMove = false;
            return;
        }
        // If player sleeping, then don't accept movement-related inputs.
        if (game.player.isSleeping) {
            if (InputProcessor.bJustPressed) {
                if (game.type == Game.Type.CLIENT) {
                    game.client.sendTCP(new com.pkmngen.game.Network.Sleep(game.player.network.id, false));
                }
                game.player.isSleeping = false;
                game.player.dirFacing = "left";
                game.insertAction(new WaitFrames(game, 24,
                                  new SetField(game.player, "dirFacing", "right",
                                  new PlayerMoving(game, game.player, false,
                                  new SetField(game.player, "dirFacing", "left",
                                  new SetField(game.player, "currSprite", game.player.standingSprites.get("left"),
                                  new WaitFrames(game, 24,
                                  new SetField(game.player, "drawSleepingBag", false,
                                  new WaitFrames(game, 24,
                                  new PlayerMoving(game, game.player, true,
                                  new SetField(game.player, "acceptInput", true,
                                  new SetField(game.player, "currSprite", game.player.standingSprites.get("down"),
                                  null))))))))))));

                // TODO: remove
//                Tile currTile = game.map.tiles.get(game.player.position);
//                Texture text = new Texture(Gdx.files.internal("tiles/sleeping_bag1.png"));
//                Sprite temp = new Sprite(text, 0, 0, 24, 16);
//                temp.setPosition(currTile.overSprite.getX(), currTile.overSprite.getY());
//                currTile.overSprite = temp;
            }
            return;
        }
        if (!game.player.acceptInput) {
            return;
        }

        // check user input
        if (InputProcessor.upPressed) {
            // if the block below isn't solid,
            // exec down move
            game.player.dirFacing = "up";
            newPos = new Vector2(game.player.position.x, game.player.position.y+16);
            if (PlayerStanding.moveTimer > 2) {
                shouldMove = true;
            }
            PlayerStanding.moveTimer++;
        }
        else if (InputProcessor.downPressed) {
            game.player.dirFacing = "down";
            newPos = new Vector2(game.player.position.x, game.player.position.y-16);
            if (PlayerStanding.moveTimer > 2) {
                shouldMove = true;
            }
            PlayerStanding.moveTimer++;
        }
        else if (InputProcessor.leftPressed) {
            game.player.dirFacing = "left";
            newPos = new Vector2(game.player.position.x-16, game.player.position.y);
            if (PlayerStanding.moveTimer > 2) {
                shouldMove = true;
            }
            PlayerStanding.moveTimer++;
        }
        else if (InputProcessor.rightPressed) {
            game.player.dirFacing = "right";
            newPos = new Vector2(game.player.position.x+16, game.player.position.y);
            if (PlayerStanding.moveTimer > 2) {
                shouldMove = true;
            }
            PlayerStanding.moveTimer++;
        }
        else {
            PlayerStanding.moveTimer = 0;
        }

        // check if input pressed
        if (Gdx.input.isKeyJustPressed(Input.Keys.C) && game.player.isBuilding) {
            game.player.buildTileIndex -= 1;
            if (game.player.buildTileIndex < 0) {
                game.player.buildTileIndex = game.player.buildTiles.size() - 1;
            }
            game.player.currBuildTile = game.player.buildTiles.get(game.player.buildTileIndex);
            // TODO: remove
//            game.insertAction(new RequirementNotify(game, game.player.currBuildTile.name));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.V) && game.player.isBuilding) {
            game.player.buildTileIndex += 1;
            if (game.player.buildTileIndex >= game.player.buildTiles.size()) {
                game.player.buildTileIndex = 0;
            }
            game.player.currBuildTile = game.player.buildTiles.get(game.player.buildTileIndex);
            // TODO: remove
//            game.insertAction(new RequirementNotify(game, game.player.currBuildTile.name));
        }

        if (InputProcessor.bJustPressed) {
            // TODO: this is broken, no idea what's going on.
            if (game.type == Game.Type.CLIENT && (game.player.isBuilding || game.player.isCutting || game.player.isHeadbutting || game.player.isJumping)) {
                game.client.sendTCP(new com.pkmngen.game.Network.UseHM(game.player.network.id,
                                                                       DrawPokemonMenu.currIndex,
                                                                       "STOP"));
            }
            if (game.player.isBuilding) {
                // player wants to stop building
                game.player.isBuilding = false;
            }
            if (game.player.isCutting) {
                // player wants to stop building
                game.player.isCutting = false;
            }
            if (game.player.isHeadbutting) {
                // player wants to stop building
                game.player.isHeadbutting = false;
            }
            if (game.player.isJumping) {
                // player wants to stop building
                game.player.isJumping = false;
            }
        }

        if (InputProcessor.aJustPressed) {
            // place the built tile
            Vector2 pos = new Vector2(0,0);
            if (game.player.dirFacing.equals("right")) {
                pos = new Vector2(game.player.position.cpy().add(16,0));
            }
            else if (game.player.dirFacing.equals("left")) {
                pos = new Vector2(game.player.position.cpy().add(-16,0));
            }
            else if (game.player.dirFacing.equals("up")) {
                pos = new Vector2(game.player.position.cpy().add(0,16));
            }
            else if (game.player.dirFacing.equals("down")) {
                pos = new Vector2(game.player.position.cpy().add(0,-16));
            }
            Tile currTile = game.map.tiles.get(pos);
            if (currTile == null) {

            }
            else if (currTile.nameUpper.equals("sleeping_bag1")) {
                // TODO: yes/no confirm dialogue
                game.player.isSleeping = true;
                Texture playerText = new Texture(Gdx.files.internal("tiles/sleeping_bag1_using.png"));
                Sprite temp = new Sprite(playerText, 0, 0, 24, 16);
                temp.setPosition(currTile.overSprite.getX(), currTile.overSprite.getY());
                currTile.overSprite = temp;
                game.player.position.set(currTile.overSprite.getX(), currTile.overSprite.getY());
            }
            else if (game.player.isBuilding) {
//                Tile newTile = new Tile(game.player.currBuildTile.name, pos.cpy());
//                game.map.tiles.remove(pos);
//                game.map.tiles.put(pos.cpy(), newTile);
                // TODO: two tiles at some loc, so that you can remove building tiles and under tile remains?
                //  or keep track of 'underTile' somewhere
                // TODO: this won't work for doors
                boolean requirementsMet = true;
                for (String reqName : game.player.buildTileRequirements.get(game.player.currBuildTile.name).keySet()) {
                    if (!game.player.itemsDict.containsKey(reqName)) {
                        requirementsMet = false;
                        break;
                    }
                    int playerOwns = game.player.itemsDict.get(reqName);
                    if (playerOwns < game.player.buildTileRequirements.get(game.player.currBuildTile.name).get(reqName)) {
                        requirementsMet = false;
                        break;
                    }
                }
                if (!currTile.attrs.get("solid") && requirementsMet) {
                    // TODO: remove commented lines
//                    currTile.overSprite = new Sprite(game.player.currBuildTile.sprite);
//                    currTile.name = game.player.currBuildTile.name;
//                    currTile.overSprite.setPosition(pos.x, pos.y);
//                    currTile.attrs.put("cuttable", true); // test this
//                    currTile.attrs.put("solid", true);

                    Tile newTile = new Tile(currTile.name, game.player.currBuildTile.name,
                                            currTile.position.cpy(), true, currTile.routeBelongsTo);
                    // Tile may change orientation depending on surrounding tiles
                    // ie, fence will rotate, house piece might be corner, etc
                    if (game.type != Game.Type.CLIENT) {
                        game.map.tiles.remove(currTile.position.cpy());
                        game.map.tiles.put(currTile.position.cpy(), newTile);
                        PlayerStanding.adjustSurroundingTiles(newTile);
                    }
                    else {
                        // Send request to build new tile to server
                        // Don't update locally yet, server will send back TileData if it succeeds.
                        game.client.sendTCP(new Network.TileData(newTile));
                    }
                    game.insertAction(new PlaySound("strength1", null));
                    game.playerCanMove = false;
                    game.insertAction(new WaitFrames(game, 30,
                                      new SetField(game, "playerCanMove", true,
                                      null)));

                    // TODO: server also needs to do this
                    // Add 'opening' to interiors if the player is building a house
                    if (game.player.currBuildTile.name.contains("house")) {
                        // TODO: place window(s) and chimney
                        this.detectIsHouseBuilt(game, currTile);
                    }
                    // Deduct required materials
                    for (String name : game.player.buildTileRequirements.get(game.player.currBuildTile.name).keySet()) {
                        int value = game.player.buildTileRequirements.get(game.player.currBuildTile.name).get(name);
                        value = game.player.itemsDict.get(name)-value;
                        game.player.itemsDict.put(name, value);
                        if (value <= 0) {
                            game.player.itemsDict.remove(name);
                        }
                    }
                }
            }
            else if (game.player.isHeadbutting) {
                if (currTile.attrs.containsKey("headbuttable") && currTile.attrs.get("headbuttable")) {
                    // Choose if found anything
                    Action nextAction = new PlayerCanMove(game, null);
//                    int randInt = game.map.rand.nextInt(4);
                    if (currTile.routeBelongsTo != null && !currTile.routeBelongsTo.pokemon.isEmpty()) {
                        // TODO: need to be static per-tree somehow.
                        game.playerCanMove = false;
                        game.battle.oppPokemon = currTile.routeBelongsTo.pokemon.get(0);
//                        game.insertAction(Battle_Actions.get(game));
                        // new DisplayText(game, "A wild pokémon attacked!", null, null,
                        nextAction = new WaitFrames(game, 16, new BattleIntroMusic(Battle.getIntroAction(game)));
                        this.checkWildEncounter = false;
                        shouldMove = false;  // for safety

                    }
                    game.insertAction(new HeadbuttTreeAnim(game, game.map.tiles.get(pos),
                                                     nextAction));
                    game.playerCanMove = false;
                }
            }
            else if (game.player.isCutting) {
                if (currTile.attrs.containsKey("cuttable") && currTile.attrs.get("cuttable")) {
                    Action action = new CutTreeAnim(game, game.map.tiles.get(pos), null);
                    // place black tile at location
                    // TODO: if there are ever interior objects, how do you preserve those?
//                    Tile interiorTile = new Tile("black1", currTile.position.cpy());
//                    game.map.interiorTiles.get(game.map.interiorTilesIndex).put(currTile.position.cpy(), interiorTile);
                    game.map.interiorTiles.get(game.map.interiorTilesIndex).remove(currTile.position.cpy());
                    game.playerCanMove = false;
                    // Get items from tile
                    if (!currTile.items.isEmpty()) {
                        for (String item : currTile.items.keySet()) {
                            System.out.println(item);
//                            game.insertAction(new ItemPickupNotify(game, item, currTile.items.get(item)));
                            String plural = "";
                            if (currTile.items.get(item) > 1) {
                                plural = "s";
                            }
                            action.append(new DisplayText(game, "Picked up "+currTile.items.get(item)+" "+item.toUpperCase()+plural+".", null, null,
                                          null));
                            if (game.player.itemsDict.containsKey(item)) {
                                int currQuantity = game.player.itemsDict.get(item);
                                game.player.itemsDict.put(item, currQuantity+currTile.items.get(item));
                            }
                            else {
                                game.player.itemsDict.put(item, currTile.items.get(item));
                            }
                        }
                        currTile.items.clear();
                    }
                    action.append(new SetField(game, "playerCanMove", true, null));
                    game.insertAction(action);
                    if (game.type == Game.Type.CLIENT) {
                        game.client.sendTCP(new Network.UseHM(game.player.network.id, 0, "CUT", game.player.dirFacing));
                    }
                }
                this.detectIsHouseBuilt(game, currTile);
            }
            else if (currTile.nameUpper.contains("campfire") && game.playerCanMove) {
                game.playerCanMove = false;
                game.player.isCrafting = true;
                game.insertAction(new DrawCraftsMenu.Intro(null, 9,
                                  new DrawCraftsMenu(game,
                                  new SetField(game, "playerCanMove", true,
                                  new SetField(game.player, "isCrafting", false,
                                  null)))));
            }
            else {
                // TODO: onPressA is deprecated since there are no unique Tile subclasses
                // because it facilitates serialization. Deprecate this.
                // calling this will trigger the tiles' onPressA function
                 // might be sign (text), character, etc.
                Tile temp = game.map.tiles.get(pos);
                if (temp != null) {
                    temp.onPressA(game);
                }
            }
        }

        // TODO: test that moving this is ok
        // Draw the sprite corresponding to player direction
        if (this.isRunning == true) {
            game.player.currSprite = new Sprite(game.player.standingSprites.get(game.player.dirFacing+"_running"));
        }
        else {
            game.player.currSprite = new Sprite(game.player.standingSprites.get(game.player.dirFacing));
        }

        if (shouldMove) {
            game.actionStack.remove(this);
            // If client, send move command to server
            if (game.type == Game.Type.CLIENT) {
                game.client.sendTCP(new Network.MovePlayer(game.player.network.id,
                                                           game.player.dirFacing,
                                                           InputProcessor.bPressed));
            }
            Tile currTile = game.map.tiles.get(game.player.position);
            Tile temp = game.map.tiles.get(newPos);
            String oppDir = "";
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
            // Check if traveling through interior door.
            if (game.player.dirFacing.equals("down") && currTile.name.contains("rug")) {
                if (game.map.tiles == game.map.overworldTiles) {
                    // do enter building anim, then player travels down one space
                    game.insertAction(new EnterBuilding(game, "enter", game.map.interiorTiles.get(game.map.interiorTilesIndex),
                                      new PlayerMoving(game, this.alternate)));
                }
                else {
                    // do leave building anim, then player travels down one space
                    game.insertAction(new EnterBuilding(game, "exit", game.map.overworldTiles,
                                      new PlayerMoving(game, this.alternate)));
                }
                return;
            }
            // Check if moving into empty space to avoid temp.attr checks afterwards
            // If player is pressing space key (debug mode), just move through the object.
            if (temp == null) {
                game.insertAction(new PlayerBump(game));
                return;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && game.debugInputEnabled) {
                // No tile here, so just move normally
                if (InputProcessor.bPressed) { // check if player should be running
                    game.insertAction(new PlayerRunning(game, this.alternate));
                }
                else {
                    game.insertAction(new PlayerMoving(game, this.alternate));
                }
                return;
            }
            if (temp.attrs.get("solid")) {
                game.insertAction(new PlayerBump(game));
                return;
            }
            if (game.map.pokemon.containsKey(newPos)) {
                game.insertAction(new PlayerBump(game));
                return;
            }
            if (game.player.isJumping) {
                if (temp.attrs.get("ledge")){
                    if (temp.ledgeDir.equals("up") && !game.player.dirFacing.equals("down")) {
                    }
                    else if (temp.ledgeDir.equals(game.player.dirFacing) || temp.ledgeDir.equals(oppDir)) {
                        // Jump up the ledge
                        game.insertAction(new PlayerLedgeJumpFast(game));
                        return;
                    }
                    else {
                        // bump into ledge
                        game.insertAction(new PlayerBump(game));
                        return;
                    }
                }
                // If player is currently on a ledge
                if (currTile != null && currTile.attrs.get("ledge") && !currTile.ledgeDir.equals("up")) {
                    if (currTile.ledgeDir.equals(game.player.dirFacing) || currTile.ledgeDir.equals(oppDir)) {
                        // Jump off the ledge
                        game.insertAction(new PlayerLedgeJumpFast(game));
                        return;
                    }
                }
            }
            // Handle ledge jump upward case
            if (currTile.attrs.get("ledge") && currTile.ledgeDir.equals("up") && game.player.dirFacing.equals("up")) {
                // jump over ledge
                game.insertAction(new PlayerLedgeJumpFast(game));
                return;
            }
            if (temp.attrs.get("ledge") && temp.ledgeDir.equals("up") && game.player.dirFacing.equals("down")) {
                game.insertAction(new PlayerBump(game));
                return;
            }
            if (temp.attrs.get("ledge") && !temp.ledgeDir.equals("up")) {
                // check that the tile the player will jump into isn't solid
                Vector2 diff = newPos.cpy().sub(game.player.position);
                Tile fartherOutTile = game.map.tiles.get(newPos.cpy().add(diff));
                if (temp.ledgeDir.equals(game.player.dirFacing) && (fartherOutTile == null || (!fartherOutTile.attrs.get("solid") && !fartherOutTile.attrs.get("ledge")))) {
                    // jump over ledge
                    game.insertAction(new PlayerLedgeJump(game));
                }
                else {
                    // bump into ledge
                    game.insertAction(new PlayerBump(game));
                }
                return;
            }
            // Check if player should be running
            if (InputProcessor.bPressed) {
                game.insertAction(new PlayerRunning(game, this.alternate));
                return;
            }
            game.insertAction(new PlayerMoving(game, this.alternate));
            return;
        }

        // If we got to this point, player didn't press any movement key.
        this.alternate = false;
        this.isRunning = false;
    }

    public void remoteStep(Game game) {
        if (!this.player.canMove) {
            return;
        }
        // TODO: not sure what to do with this
//        if (game.playerCanMove == false || game.player.isSleeping) {
//            if (InputProcessor.bJustPressed && game.player.isSleeping) {
//                game.player.isSleeping = false;
//                Tile currTile = game.map.tiles.get(game.player.position);
//                Texture text = new Texture(Gdx.files.internal("tiles/sleeping_bag1.png"));
//                Sprite temp = new Sprite(text, 0, 0, 24, 16);
//                temp.setPosition(currTile.overSprite.getX(), currTile.overSprite.getY());
//                currTile.overSprite = temp;
//            }
//            return;
//        }

        Vector2 newPos = new Vector2();

        // check wild encounter
         // TODO - in future, this action will jump to a waiting action after one iteration
//        if (this.checkWildEncounter == true) {
//            if (checkWildEncounter(game) == true) {
//                this.player.canMove = false;
//                game.insertAction(Battle_Actions.get(game));
//                game.currMusic.pause();
//                game.currMusic = game.battle.music;
//                game.currMusic.stop();
//                game.currMusic.play();
//                // game.battle.music.play(); // would rather have an action that does this?
//                this.checkWildEncounter = false;
//                return;
//            }
//
//            // TODO: idk where the right place for this is.
//            if (game.map.tiles.get(game.player.position) != null && game.map.tiles.get(game.player.position).name.contains("door")) {
//                game.insertAction(new EnterBuilding(game, null));
//            }
//            this.checkWildEncounter = false;
//        }

        if (game.type == Game.Type.SERVER) {
            // timer to re-sync client-side position
            // only re-sync once, then leave it alone until the player moves.
            if (this.player.network.syncTimer > 0) {
                this.player.network.syncTimer++;
            }
            if (this.player.network.syncTimer > 240) {
                this.player.network.syncTimer = 0;
                // TODO: need to be able to test this over real internet connection
                // two pc's using hamachi?
//                game.server.sendToTCP(this.player.network.connectionId, new Network.RelocatePlayer(this.player.position));
            }

            // TODO: probably move this to end of playermoving? or it's own function.
            if (!this.player.network.loadingZone.inner.contains(this.player.position)) {
                LoadingZone oldZone = new LoadingZone(this.player.network.loadingZone);
                oldZone.inner = new LoadingZone(this.player.network.loadingZone.inner);
                if (this.player.position.x < oldZone.inner.x) {
                    this.player.network.loadingZone.translate(-112, 0);
                }
                else if (this.player.position.x >= oldZone.inner.topRight().x) {
                    this.player.network.loadingZone.translate(112, 0);
                }
                if (this.player.position.y < oldZone.inner.y) {
                    this.player.network.loadingZone.translate(0, -112);
                }
                else if (this.player.position.y >= oldZone.inner.topRight().y) {
                    this.player.network.loadingZone.translate(0, 112);
                }
                Network.MapTiles mapTiles = new Network.MapTiles();
                for (Vector2 position : this.player.network.loadingZone.diff(oldZone)) {
                    Tile tile = game.map.tiles.get(position);
                    if (tile == null) {
                        continue;
                    }
                    mapTiles.tiles.add(new Network.TileData(tile));
                    // TODO: test
                    tile = game.map.interiorTiles.get(game.map.interiorTilesIndex).get(position);
                    if (tile != null) {
                        mapTiles.tiles.add(new Network.TileData(tile, game.map.interiorTilesIndex));
                    }
                    // the larger the number, the more the client hangs when receiving.
                    // 16 seemed to cause little hangup.
                    if (mapTiles.tiles.size() >= 14) {  // buffer overflow at 16?
                        game.server.sendToTCP(player.network.connectionId, mapTiles);
                        mapTiles.tiles.clear();
                    }
                    // TODO: test
                    if (game.map.pokemon.containsKey(position)) {
                        Pokemon pokemon = game.map.pokemon.get(position);
                        game.server.sendToTCP(player.network.connectionId,
                                              new Network.OverworldPokemonData(pokemon, position));
                    }
                }
                game.server.sendToTCP(player.network.connectionId, mapTiles);
                for (Player otherPlayer : game.players.values()) {
                    if (otherPlayer == this.player) {
                        continue;
                    }
                    if (this.player.network.loadingZone.contains(otherPlayer.position)) {
                        Network.ServerPlayerData serverPlayerData = new Network.ServerPlayerData(otherPlayer);
                        game.server.sendToTCP(this.player.network.connectionId, serverPlayerData);
                    }
                }
                // TODO: test without
//                for (Vector2 pos : game.map.pokemon.keySet()) {
//                    // TODO: this seems wrong, should remove the '!'?
//                    if (!this.player.network.loadingZone.contains(pos)) {
//                        Pokemon pokemon = game.map.pokemon.get(pos);
//                        game.server.sendToTCP(this.player.network.connectionId,
//                                              new Network.OverworldPokemonData(pokemon, pos));
//                    }
//                }
            }

        }

        // Draw the sprite corresponding to player direction
        if (this.player.network.isRunning == true) {
            this.player.currSprite = new Sprite(this.player.standingSprites.get(this.player.dirFacing+"_running"));
        }
        else {
            this.player.currSprite = new Sprite(this.player.standingSprites.get(this.player.dirFacing));
        }

        // If player is standing on a door, stop moving and move player to interior.
        if (this.player.network.tiles.get(this.player.position) != null &&
            (this.player.network.tiles.get(this.player.position).nameUpper.contains("door") ||
             this.player.network.tiles.get(this.player.position).name.contains("door"))) {
            if (this.player.network.tiles == game.map.overworldTiles) {
                this.player.network.tiles = game.map.interiorTiles.get(game.map.interiorTilesIndex);
            }
            else {
                this.player.network.tiles = game.map.overworldTiles;
            }
            return;
        }

        if (this.player.network.shouldMove) {
            game.actionStack.remove(this);
            this.player.network.shouldMove = false;
            if (this.player.network.dirFacing.equals("up")) {
                this.player.dirFacing = "up";
                newPos = new Vector2(this.player.position.x, this.player.position.y+16);
            }
            else if (this.player.network.dirFacing.equals("down")) {
                this.player.dirFacing = "down";
                newPos = new Vector2(this.player.position.x, this.player.position.y-16);
            }
            else if (this.player.network.dirFacing.equals("left")) {
                this.player.dirFacing = "left";
                newPos = new Vector2(this.player.position.x-16, this.player.position.y);
            }
            else if (this.player.network.dirFacing.equals("right")) {
                this.player.dirFacing = "right";
                newPos = new Vector2(this.player.position.x+16, this.player.position.y);
            }
            Tile currTile = this.player.network.tiles.get(this.player.position);
            Tile temp = this.player.network.tiles.get(newPos);

            // Check if traveling through interior door.
            if (this.player.dirFacing.equals("down") && currTile != null && currTile.name.contains("rug")) {
                if (this.player.network.tiles == game.map.overworldTiles) {
                    this.player.network.tiles = game.map.interiorTiles.get(game.map.interiorTilesIndex);
                    game.insertAction(new PlayerMoving(game, this.player, this.alternate));
                }
                else {
                    this.player.network.tiles = game.map.overworldTiles;
                    game.insertAction(new PlayerMoving(game, this.player, this.alternate));
                }
                return;
            }

            String oppDir = "";
            if (this.player.dirFacing.equals("up")) {
                oppDir = "down";
            }
            else if (this.player.dirFacing.equals("down")) {
                oppDir = "up";
            }
            else if (this.player.dirFacing.equals("right")) {
                oppDir = "left";
            }
            else if (this.player.dirFacing.equals("left")) {
                oppDir = "right";
            }
            // Check if moving into empty space to avoid temp.attr checks afterwards
            if (temp == null) {
                // TODO: remove if unused
//                // No tile here, so just move normally
//                if (this.player.network.isRunning) {
//                    game.insertAction(new PlayerRunning(game, this.player, this.alternate));
//                }
//                else {
//                    game.insertAction(new PlayerMoving(game, this.player, this.alternate));
//                }
                game.insertAction(new PlayerBump(game, this.player));
                return;
            }
            if (temp.attrs.get("solid")) {
                game.insertAction(new PlayerBump(game, this.player));
                return;
            }
            if (game.map.pokemon.containsKey(newPos)) {
                game.insertAction(new PlayerBump(game, this.player));
                return;
            }
            if (this.player.isJumping) {
                if (temp.attrs.get("ledge")){
                    if (temp.ledgeDir.equals("up") && !this.player.dirFacing.equals("down")) {
                    }
                    else if (temp.ledgeDir.equals(this.player.dirFacing) || temp.ledgeDir.equals(oppDir)) {
                        // Jump up the ledge
                        game.insertAction(new PlayerLedgeJumpFast(game, this.player));
                        return;
                    }
                    else {
                        // bump into ledge
                        game.insertAction(new PlayerBump(game, this.player));
                        return;
                    }
                }
                // If player is currently on a ledge
                if (currTile != null && currTile.attrs.get("ledge") && !currTile.ledgeDir.equals("up")) {
                    if (currTile.ledgeDir.equals(this.player.dirFacing) || currTile.ledgeDir.equals(oppDir)) {
                        // Jump off the ledge
                        game.insertAction(new PlayerLedgeJumpFast(game, this.player));
                        return;
                    }
                }
            }
            // Handle ledge jump upward case
            if (currTile != null && currTile.attrs.get("ledge") && currTile.ledgeDir.equals("up") && this.player.dirFacing.equals("up")) {
                // jump over ledge
                game.insertAction(new PlayerLedgeJumpFast(game, this.player));
                return;
            }
            if (temp.attrs.get("ledge") && temp.ledgeDir.equals("up") && this.player.dirFacing.equals("down")) {
                game.insertAction(new PlayerBump(game, this.player));
                return;
            }
            if (temp.attrs.get("ledge") && !temp.ledgeDir.equals("up")) {
                // Check that the tile the player will jump into isn't solid
                Vector2 diff = newPos.cpy().sub(this.player.position);
                Tile fartherOutTile = game.map.tiles.get(newPos.cpy().add(diff));
                if (temp.ledgeDir.equals(this.player.dirFacing) && (fartherOutTile == null || (!fartherOutTile.attrs.get("solid") && !fartherOutTile.attrs.get("ledge")))) {
                    // jump over ledge
                    game.insertAction(new PlayerLedgeJump(game, this.player));
                }
                else {
                    // bump into ledge
                    game.insertAction(new PlayerBump(game, this.player));
                }
                return;
            }
            // Check if player should be running
            if (this.player.network.isRunning) {
                game.insertAction(new PlayerRunning(game, this.player, this.alternate));
            }
            else {
                game.insertAction(new PlayerMoving(game, this.player, this.alternate));
            }
            if (game.type == Game.Type.SERVER) {
                // check wild encounter on next position, send back if yes
                Pokemon pokemon = checkWildEncounter(game, newPos);
                if (pokemon != null) {
                    // The first Pokemon the player sends out in battle should
                    // have >0 hp.
                    for (Pokemon currPokemon : this.player.pokemon) {
                        if (currPokemon.currentStats.get("hp") > 0) {
                            this.player.currPokemon = currPokemon;
                            break;
                        }
                    }
                    this.player.canMove = false;
                    game.server.sendToTCP(this.player.network.connectionId,
                                          new Network.BattleData(pokemon));
                    // set up battle server-side, so server can keep track of move outcomes
                    game.battles.put(this.player.network.id, new Battle());
                    game.battles.get(this.player.network.id).oppPokemon = pokemon;
                }
            }
            return;

            // TODO: make sure the above changes work
//            // first check if traveling through interior door
//            if (this.player.dirFacing.equals("down") && game.map.tiles.containsKey(this.player.position) && game.map.tiles.get(this.player.position).name.contains("rug")) {
//                // do leave building anim, then player travels down one space
//                game.insertAction(new EnterBuilding(game, "exit",
//                                                 new playerMoving(game, this.alternate)));
//                game.actionStack.remove(this);
//            }
//            else if (temp == null) { // need this check to avoid attr checks after this if null
//                // no tile here, so just move normally
//                if (this.player.network.isRunning) { // check if player should be running
//                    game.insertAction(new playerRunning(game, this.player, this.alternate));
//                }
//                else {
//                    game.insertAction(new playerMoving(game, this.player, this.alternate));
//                }
//                game.actionStack.remove(this);
//            }
//            // TODO: 'up' ledges not handled correctly.
//            else if (temp.attrs.get("solid") == true) {
//                    game.insertAction(new playerBump(game, this.player));
//                    game.actionStack.remove(this);
//            }
//            else if (temp.attrs.get("ledge")) {
//                String oppDir = "";
//                if (this.player.dirFacing.equals("up")) {
//                    oppDir = "down";
//                }
//                else if (this.player.dirFacing.equals("down")) {
//                    oppDir = "up";
//                }
//                else if (this.player.dirFacing.equals("right")) {
//                    oppDir = "left";
//                }
//                else if (this.player.dirFacing.equals("left")) {
//                    oppDir = "right";
//                }
//                // Jump up the ledge if player is jumping
////                System.out.println(this.player.isJumping);
////                System.out.println(temp.ledgeDir.equals(this.player.dirFacing));
////                System.out.println(temp.ledgeDir.equals(oppDir));
////                System.out.println(oppDir);
//                if (this.player.isJumping && (temp.ledgeDir.equals(this.player.dirFacing) || temp.ledgeDir.equals(oppDir))) {
//                    game.insertAction(new PlayerLedgeJumpFast(game, this.player));
//                    game.actionStack.remove(this);
//                }
//                else if (temp.ledgeDir == this.player.dirFacing) {
//                    // jump over ledge
//                    game.insertAction(new playerLedgeJump(game, this.player));
//                    game.actionStack.remove(this);
//                }
//                else {
//                    // bump into ledge
//                    game.insertAction(new playerBump(game, this.player));
//                    game.actionStack.remove(this);
//                }
//            }
//            else {
//                if (this.player.network.isRunning) { // check if player should be running
//                    game.insertAction(new playerRunning(game, this.player, this.alternate));
//                }
//                else {
//                    game.insertAction(new playerMoving(game, this.player, this.alternate));
//                }
//                if (game.type == Game.Type.SERVER) {
//                    // check wild encounter on next position, send back if yes
//                    Pokemon pokemon = checkWildEncounter(game, newPos);
//                    if (pokemon != null) {
//                        // TODO: this may stop player from moving server-side
//                        this.player.canMove = false;
//                        game.server.sendToTCP(this.player.network.connectionId,
//                                              new Network.BattleData(pokemon));
//                        // set up battle server-side, so server can keep track of move outcomes
//                        game.battles.put(this.player.network.id, new Battle());
//                        game.battles.get(this.player.network.id).oppPokemon = pokemon;
//                    }
//                }
//                game.actionStack.remove(this);
//            }
        }
//        this.alternate = false;
        this.player.network.isRunning = false;
    }

    @Override
    public void step(Game game) {
        if (this.player.isSleeping) {
            if (this.player.zsTimer < 64) {
                this.player.zSprite.setPosition(this.player.position.x+8, this.player.position.y+18);
            }
            else {
                this.player.zSprite.setPosition(this.player.position.x+16, this.player.position.y+18);
            }
            this.player.zsTimer++;
            if (this.player.zsTimer >= 128 ) {
                // Restore pokemon hp
                // TODO: restore player hp
                // TODO: this needs to go in remoteStep or something for remote player
                for (Pokemon pokemon : this.player.pokemon) {
                    boolean indoors = (this.player.network.tiles == game.map.overworldTiles);
                    if (game.type == Game.Type.CLIENT) {
                        indoors = (game.map.tiles == game.map.overworldTiles);
                    }
                    if (indoors) {
                        pokemon.currentStats.put("hp", pokemon.currentStats.get("hp")+1);
                    }
                    else {
                        pokemon.currentStats.put("hp", pokemon.currentStats.get("hp")+2);
                    }
                    if (pokemon.currentStats.get("hp") >= pokemon.maxStats.get("hp")) {
                        pokemon.currentStats.put("hp", pokemon.maxStats.get("hp"));
                    }
                }
                this.player.zsTimer = 0;
            }
        }

        if (this.player.type == Player.Type.LOCAL) {
            this.localStep(game);
        }
        else if (this.player.type == Player.Type.REMOTE) {
            this.remoteStep(game);
        }
    }

}

class RequirementNotify extends Action {
  public int layer = 114;
  Sprite bgSprite;
  int signCounter = 100;;

  String tileName;
  String text = "";
  public RequirementNotify(Game game, String tileName) {
      this.tileName = tileName;
      Texture text = new Texture(Gdx.files.internal("text2.png"));
      this.bgSprite = new Sprite(text, 0, 0, 160, 144);
      this.bgSprite.setPosition(0, -144);
      for (String name : game.player.buildTileRequirements.get(tileName).keySet()) {
          this.text += name + ": " + String.valueOf(game.player.buildTileRequirements.get(tileName).get(name)) + " ";
      }
  }
  public String getCamera() {return "gui";}

  public int getLayer(){return this.layer;}

  @Override
  public void step(Game game) {
      if (signCounter > 0) {
          signCounter--;
          if (signCounter > 100) {
          }
          else if (signCounter > 78) {
              this.bgSprite.setPosition(this.bgSprite.getX(), this.bgSprite.getY()+1);
          }
          else if (signCounter > 22) {
          }
          else {
              this.bgSprite.setPosition(this.bgSprite.getX(), this.bgSprite.getY()-1);
          }
          this.bgSprite.draw(game.uiBatch);

          game.font.draw(game.uiBatch, "Requires: " + this.text, 10, this.bgSprite.getY()+134);
      }
  }
}

// for spawning ghost, playing effects etc
class SpawnGhost extends Action {
    public int layer = 114;
    Sprite[] sprites;
    int part1;
    int part2;
    int part3;
    Vector2 position;

    public SpawnGhost(Game game, Vector2 position) {
        this.part1 = 80;
        this.part2 = 40;
        this.part3 = 50;
        this.position = position;

        Texture ghostTexture1 = new Texture(Gdx.files.internal("ghost_spawn1.png"));
        Texture ghostTexture2 = new Texture(Gdx.files.internal("ghost_sheet1.png"));

        this.sprites = new Sprite[]{
                new Sprite(ghostTexture1, 0, 0, 40, 40),
                new Sprite(ghostTexture2, 0, 0, 32, 32),
                new Sprite(ghostTexture2, 32, 0, 32, 32),
        };

        this.sprites[0].setPosition(position.x-4, position.y-4);
        this.sprites[1].setPosition(position.x, position.y);
        this.sprites[2].setPosition(position.x, position.y);
    }

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        if (part1 == 80) { // do once
            game.playerCanMove = false;
            // set player to face ghost
            float dx = this.position.x - game.player.position.x;
            float dy = this.position.y - game.player.position.y;
            if (dx < dy) {
                if (game.player.position.y < this.position.y) {
                    game.player.currSprite = game.player.standingSprites.get("up");
                }
                else {
                    game.player.currSprite = game.player.standingSprites.get("left");
                }
            }
            else {
                if (game.player.position.x < this.position.x) {
                    game.player.currSprite = game.player.standingSprites.get("right");
                }
                else {
                    game.player.currSprite = game.player.standingSprites.get("down");
                }
            }

            // play alert music
            game.currMusic.pause();
            Music music = Gdx.audio.newMusic(Gdx.files.internal("night1_alert1.ogg"));
            music.setLooping(true);
            music.setVolume(.7f);
            game.currMusic = music;
            game.currMusic.play();

            // TODO - insert ! mark action
        }

        // play anim frames one by one
        if (part1 > 0) {
            if (part1 % 4 >= 2) {
                this.sprites[0].draw(game.mapBatch);
            }
            part1--;
            return;
        }

        if (part2 == 40) {
            game.insertAction(new PlaySound("ghost1", null));
        }
        if (part2 == 32) {
            game.insertAction(new PlaySound("ghost1", null));
        }

        if (part2 > 0) {
            if (part2 % 8 >= 4) {
                this.sprites[1].draw(game.mapBatch);
            }
            else {
                this.sprites[2].draw(game.mapBatch);
            }
            part2--;
            return;
        }

        if (part3 > 0) {
            this.sprites[2].draw(game.mapBatch);
            part3--;
            return;
        }

        // TODO - start frantic music (? best place? better in ghost draw?)
//        game.currMusic.pause();
        // TODO: test
        // this should stop and dispose the night1_alert1 music.
        // was having issues where the game would not play sounds because too many were loaded.
        game.currMusic.stop();  
        game.currMusic.dispose();
        Music music = Gdx.audio.newMusic(Gdx.files.internal("night1_chase1.ogg"));
        music.setLooping(true);
        music.setVolume(.7f);
        game.currMusic = music;
//        game.map.currRoute.music = music; // TODO - how to switch to normal after defeating
        game.currMusic.play();

        game.playerCanMove = true;
        game.actionStack.remove(this);
        game.insertAction(new DrawGhost(game, this.position));
    }
}
