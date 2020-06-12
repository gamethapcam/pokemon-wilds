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

// contains thing to cycle, number of frames for thing
// add more in future, like sounds
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
        animateThese.add(thing);;
        this.numFrames.add(numFrames);
    }

    public int currentFrame() {
        return this.numFrames.get(this.index);
    }

    public E currentThing() {
        return this.animateThese.get(this.index);
    }
}

// draw character action
//  need to build in 'button press delay'

class CutTreeAnim extends Action {
    int index = 0;
    Tile tile;
    Action nextAction;
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
            game.insertAction(new PlaySound("cut1", new DoneAction()));
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
            game.map.tiles.put(this.tile.position.cpy(),
                               new Tile(this.tile.name, this.tile.position.cpy(),
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
            game.playerCanMove = true;
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
        }
        this.index++;
    }
}

// for keeping track of day night
 // pops up cycle change notif, changes shader, etc
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
        CycleDayNight.dayTimer = 10000; // 100; //- debug // 10000;

        Texture text = new Texture(Gdx.files.internal("text2.png"));
        this.bgSprite = new Sprite(text, 0, 0, 160, 144);
        this.bgSprite.setPosition(0,24);

        this.text = game.map.timeOfDay+": "; // String.valueOf(this.numMain)
    }
    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        if (game.playerCanMove == true) {
            dayTimer--;
        }

        if (dayTimer <= 0) {
            // TODO - time of day is part of map
             //
            if (game.map.timeOfDay == "Day") {
                this.fadeToNight = true;
                dayTimer = 10000; // debug
            }
            else if (game.map.timeOfDay == "Night") {
                this.fadeToDay = true;
                dayTimer = 10000; // 1000 - debug
            }
        }

        if (fadeToDay == true) {
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

                // TODO - fade day music
                game.currMusic.pause();
                // start night music
                Music music = Gdx.audio.newMusic(Gdx.files.internal("route1_1.ogg"));
                music.setLooping(true);
                music.setVolume(.7f);
                game.currMusic = music;
                game.map.currRoute.music = music; // TODO - how to switch to normal after defeating
                game.currMusic.play();

                // state which day it is
                day++;
                signCounter = 300;
                this.bgSprite.setPosition(0,24);
            }
        }

        if (fadeToNight == true) {
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

                // TODO - fade day music
                game.currMusic.pause();
                // start night music
                Music music = Gdx.audio.newMusic(Gdx.files.internal("night1.ogg"));
                music.setLooping(true);
                music.setVolume(.7f);
                game.currMusic = music;
                game.map.currRoute.music = music; // TODO - how to switch to normal after defeating
                game.currMusic.play();

                // state which night it is
                night++;
                signCounter = 150;
                this.bgSprite.setPosition(0,24);
            }
        }

        // check player can move so don't spawn in middle of battle or when looking at ghost
        if (game.map.timeOfDay == "Night" && game.playerCanMove == true) {
            // chance to spawn ghost
            countDownToGhost--;
            if (game.player.currState != "Running") {
                countDownToGhost--;
            }

            if (countDownToGhost == 0) {
                Vector2 randPos = game.player.position.cpy().add(this.rand.nextInt(5)*16 - 48, this.rand.nextInt(5)*16 - 48);
                game.insertAction(new spawnGhost(game, new Vector2(randPos)) );
                this.countDownToGhost = this.rand.nextInt(4000) + 1000; // debug: 1000;
            }

        }

        if (signCounter > 0) {
            signCounter--;

            if (signCounter > 100) {
            }
            else if (signCounter > 78) {
                this.bgSprite.setPosition(this.bgSprite.getX(), this.bgSprite.getY()-1);
            }
            else if (signCounter > 22) {
            }
            else {
                this.bgSprite.setPosition(this.bgSprite.getX(), this.bgSprite.getY()+1);
            }

            this.bgSprite.draw(game.uiBatch);
            String temp="";
            if (game.map.timeOfDay == "Day") {
                    temp = String.valueOf(this.day);
            }
            else {
                    temp = String.valueOf(this.night);
            }
            game.font.draw(game.uiBatch, game.map.timeOfDay+": "+temp, 60, this.bgSprite.getY()+134); // Gdx.graphics.getHeight()-
        }

    }
}

class despawnGhost extends Action {
    public int layer = 114;
    Sprite[] sprites;

    int part1;

    int part2;
    int part3;
    Vector2 position;

    Sprite sprite;
    public despawnGhost(Vector2 position) {
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

// for drawing the ghost that chases the player
class drawGhost extends Action {
    public int layer = 114;
    Sprite currSprite;

    Vector2 basePos; // need this to make ghost 'float'
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
    public drawGhost(Game game, Vector2 position) {
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
            game.insertAction(new despawnGhost(this.basePos.cpy()));
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
                System.out.println("hey");
                continue;
            }
            if (tile.nameUpper.contains("campfire")) {
                this.currSprite.draw(game.mapBatch);
                game.actionStack.remove(this);
                game.insertAction(new despawnGhost(this.basePos.cpy()));
                game.currMusic.pause();
                game.currMusic = game.map.currRoute.music;
                game.currMusic.play();
                return;
            }
        }

        // check if ghost pokemon is dead. if yes, remove this from AS

        // check whether player is in battle or not
        // if not, don't move the ghost at all (subject to change)
        if (game.battle.drawAction != null) {
            this.currSprite.draw(game.mapBatch);
            this.inBattle = true;
            this.noEncounterTimer = 0;
            return;
        }

        // wait for a while if you just exited battle
        if (inBattle == true) {
            if (noEncounterTimer % 4 >= 2) {
                this.currSprite.draw(game.mapBatch);
            }
            this.noEncounterTimer++;
            if (this.noEncounterTimer < 128) {
                return;
            }
            inBattle = false;
        }

        // pause if player can't move
        if (game.playerCanMove == false) {
            this.currSprite.draw(game.mapBatch);
            return;
        }

        // calculate direction to player, face that direction
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

        // set ghost sprite to dirFacing
        this.currSprite = this.sprites.get(this.dirFacing)[this.animIndex];

        // game.player.position.x < this.basePos.x

        // modify base pos to chase player (accelerate in player direction)
        if (this.dirFacing == "left") {
            if (velX > -maxVel) {
                this.velX -= .1f;
            }
            this.velY -= this.velY/16.0f;
        }
        else if (this.dirFacing == "right") {
            if (velX < maxVel) {
                this.velX += .1f;
            }
            this.velY -= this.velY/16.0f;
        }
        else if (this.dirFacing == "down") {
            if (velY > -maxVel) {
                this.velY -= .1f;
            }
            this.velX -= this.velX/16.0f;
        }
        else if (this.dirFacing == "up") {
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
        // game.batch.draw(this.currSprite, this.currSprite.getX(), this.currSprite.getY()); // TODO - remove

        sineTimer+=.125f;
        if (sineTimer >= 3.14*2f) {
            sineTimer = 0.0f;
        }

        // run ghost animation
        animFrame++;
        if (animFrame > 10) {
            animFrame = 0;
            animIndex++;
        }
        if (animIndex >= this.sprites.get(this.dirFacing).length ) {
            animIndex = 0;
        }

        // need to make ghost rectangle bounds smaller
        Rectangle rect = this.currSprite.getBoundingRectangle();
        rect.x +=16;
        rect.y +=16;
        rect.width -=2*16;
        rect.height -=2*16;

        // check collision. if collision, start battle with pokemon
        if (rect.overlaps(game.player.currSprite.getBoundingRectangle())) {
            // System.out.println("collision x: " + String.valueOf(this.currSprite.getBoundingRectangle().x)); // debug
            // System.out.println("collision x: " + String.valueOf(game.player.currSprite.getBoundingRectangle().x)); // debug

            game.battle.oppPokemon = this.pokemon;
            game.playerCanMove = false;
            // todo - remove
//            game.insertAction(new SplitAction(
//                                                new BattleIntro(
//                                                    new BattleIntro_anim1(
//                                                        new SplitAction(
//                                                            new DrawBattle(game),
//                                                            new BattleAnim_positionPlayers(game,
//                                                                new PlaySound(game.battle.oppPokemon.name,
//                                                                    new DisplayText(game, "A Ghost appeared!", null, null,
//                                                                        new WaitFrames(game, 39,
//                                                                            // demo code - wildly confusing, but i don't want to write another if statement
//                                                                                game.player.adrenaline > 0 ?
//                                                                                new DisplayText(game, ""+game.player.name+" has ADRENALINE "+Integer.toString(game.player.adrenaline)+"!", null, null,
//                                                                                    new PrintAngryEating(game, // for demo mode, normally left out
//                                                                                            new DrawBattleMenu_SafariZone(game, new DoneAction())
//                                                                                        )
//                                                                                    )
//                                                                                :
//                                                                                new PrintAngryEating(game, // for demo mode, normally left out
//                                                                                        new DrawBattleMenu_SafariZone(game, new DoneAction())
//                                                                            )
//                                                                            //
//                                                                        )
//                                                                    )
//                                                                )
//                                                            )
//                                                        )
//                                                    )
//                                                ),
//                                                new DoneAction()
//                                            )
//                                        );
            game.insertAction(Battle.getIntroAction(game));
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

    Sprite zSprite;

    int zsTimer = 0;
    public DrawPlayerUpper(Game game) {
        Texture text = new Texture(Gdx.files.internal("tiles/zs1.png"));
        this.zSprite = new Sprite(text, 0, 0, 16, 16);
    }

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        if (game.player.isSleeping) {
            if (this.zsTimer < 64) {
                this.zSprite.setPosition(game.player.position.x+8, game.player.position.y+18);
            }
            else {
                this.zSprite.setPosition(game.player.position.x+16, game.player.position.y+18);
            }
            this.zsTimer++;
            if (this.zsTimer >= 128) {
                this.zsTimer = 0;
            }
            this.zSprite.draw(game.mapBatch);
            return;
        }

        // draw building tile if building
        if (game.player.isBuilding) {
            // get direction facing
            Vector2 pos = new Vector2(0,0);
            if (game.player.dirFacing == "right") {
                pos = new Vector2(game.player.position.cpy().add(16,0));
            }
            else if (game.player.dirFacing == "left") {
                pos = new Vector2(game.player.position.cpy().add(-16,0));
            }
            else if (game.player.dirFacing == "up") {
                pos = new Vector2(game.player.position.cpy().add(0,16));
            }
            else if (game.player.dirFacing == "down") {
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
            game.insertAction(new PlaySound("headbutt1", new DoneAction()));
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

class itemPickupNotify extends Action {
  public int layer = 114;
  Sprite bgSprite;
  int signCounter = 150;;

  String itemName;
  int quantity;
  public itemPickupNotify(Game game, String itemName, int quantity) {
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
 * Self-explanatory Player class.
 */
public class Player {
    // current player direction
    public String dirFacing = "down";

    // movement sprites
    Map<String, Sprite> standingSprites = new HashMap<String, Sprite>();
    Map<String, Sprite> movingSprites = new HashMap<String, Sprite>();
    Map<String, Sprite> altMovingSprites = new HashMap<String, Sprite>();

    Vector2 position;

    Sprite currSprite = new Sprite();

    Sprite battleSprite;

    String name;  // Player display name
    Color color;  // Used to colorize the player sprite

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
    boolean isSleeping = false;
    Tile currBuildTile; // which tile will be built next
    int buildTileIndex = 0;
    ArrayList<Tile> buildTiles = new ArrayList<Tile>();
    HashMap<String, HashMap<String, Integer>> buildTileRequirements = new HashMap<String, HashMap<String, Integer>>();
    boolean canMove = true;  // TODO: migrate to start using this
    int numFlees = 0;  // used in battle run away mechanic
    playerStanding standingAction;

    public Type type;
    Network network;

    public Player() {
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
        Texture text = new Texture(Gdx.files.internal("battle/player_back1.png"));
        this.battleSprite = new Sprite(text, 0, 0, 28, 28);

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
        this.buildTiles.add(new Tile("house1_door1_dark", new Vector2(0,0)));
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
        this.buildTiles.add(new Tile("house5_left1", new Vector2(0,0)));
        this.buildTiles.add(new Tile("house5_middle1", new Vector2(0,0)));
        this.buildTiles.add(new Tile("house5_right1", new Vector2(0,0)));
        this.buildTiles.add(new Tile("house5_roof_middle1", new Vector2(0,0)));
        this.buildTiles.add(new Tile("house5_roof_left1", new Vector2(0,0)));
        this.buildTiles.add(new Tile("house5_roof_right1", new Vector2(0,0)));
        this.buildTiles.add(new Tile("campfire1", new Vector2(0,0)));
        this.buildTiles.add(new Tile("sleeping_bag1", new Vector2(0,0)));
        this.currBuildTile = this.buildTiles.get(this.buildTileIndex);

        // for now, everything requires same amt
        for (Tile tile : this.buildTiles) {
            if (tile.nameUpper.equals("sleeping_bag1")) {
                continue;
            }
            this.buildTileRequirements.put(tile.name, new HashMap<String, Integer>());
//            this.buildTileRequirements.get(tile.name).put("grass", 1);
//            this.buildTileRequirements.get(tile.name).put("logs", 1);
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
//        this.itemsDict.put("Safari Ball", 99);
        this.itemsDict.put("Ultra Ball", 99);
        this.itemsDict.put("Poké Ball", 99);
        this.itemsDict.put("Sleeping Bag", 1);

        this.network = new Network(this.position);
        this.type = Type.LOCAL;
    }
    /**
     * Constructor to load from serialized class (ie data sent over network or loaded from file).
     */
    public Player(com.pkmngen.game.Network.PlayerData playerData) {
        this();
        this.position = playerData.position;
        this.name = playerData.name;
        this.pokemon = new ArrayList<Pokemon>();
        for (com.pkmngen.game.Network.PokemonData pokemonData : playerData.pokemon) {
            this.pokemon.add(new Pokemon(pokemonData));
        }
        if (this.pokemon.size() > 0) {
            this.currPokemon = this.pokemon.get(0);
        }
        this.itemsDict = playerData.itemsDict;
        this.network = new Network(this.position.cpy());  // re-initialize loading zone boundaries based off of current position.
        this.network.id = playerData.id;
        this.network.number = playerData.number;
        this.setColor(playerData.color);  // TODO: when server re-loads player, doesn't seem like this is being rendered.
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
        // pixmap of right-leaning sprite
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
        // pixmap of right-leaning sprite
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
    }

    class Network {
        Vector2 loadingZoneBL = new Vector2();
        Vector2 loadingZoneTR = new Vector2();
        String id;  // when this is a remote player, this is an index number
        boolean shouldMove = false;
        String dirFacing = "down";
        boolean isRunning = false;
        int connectionId;  // kryonet connection id
        String number;
        com.pkmngen.game.Network.BattleData doEncounter;  // acts as flag that battle has been entered

        int syncTimer = 0;  // used to know when to re-send player location to client

        public Network(Vector2 position) {
            loadingZoneBL = position.cpy().add(-128*2, -128*2);
            loadingZoneTR = position.cpy().add(128*2, 128*2);
        }
    }

    enum Type {
        LOCAL,   // local, ie accepting keyboard input
        REMOTE;  // being synced from remote machine
    }
}

class playerBump extends Action {
    public int layer = 130;
    int timer = 0;

    int maxTime = 10; // 20 reminded me of gold version I think
    boolean alternate = false;

    Player player;
    public playerBump(Game game) {
        this(game, game.player);
    }

    public playerBump(Game game, Player player) {
        this.player = player;
        game.insertAction(new PlaySound("bump2", new DoneAction()));
    }

    public int getLayer(){return this.layer;}

    public void localStep(Game game) {
        timer++;

        if (this.timer >= 2*maxTime ) {
            this.alternate = !this.alternate;
            this.timer = 0;
            game.insertAction(new PlaySound("bump2", new DoneAction()));
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

        if (!Gdx.input.isKeyPressed(Input.Keys.UP) && game.player.dirFacing == "up") {
            game.insertAction(new playerStanding(game));
            game.actionStack.remove(this);
        }
        else if (!Gdx.input.isKeyPressed(Input.Keys.DOWN) && game.player.dirFacing == "down") {
            game.insertAction(new playerStanding(game));
            game.actionStack.remove(this);
        }
        else if (!Gdx.input.isKeyPressed(Input.Keys.LEFT) && game.player.dirFacing == "left") {
            game.insertAction(new playerStanding(game));
            game.actionStack.remove(this);
        }
        else if (!Gdx.input.isKeyPressed(Input.Keys.RIGHT) && game.player.dirFacing == "right") {
            game.insertAction(new playerStanding(game));
            game.actionStack.remove(this);
        }
    }

    public void remoteStep(Game game) {
        this.player.network.syncTimer++;
        timer++;

        if (this.timer >= 2*maxTime ) {
            this.alternate = !this.alternate;
            this.timer = 0;
            game.insertAction(new PlaySound("bump2", new DoneAction()));
        }

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

        // when facingDir key is released, go to playerStanding
        if (!this.player.network.shouldMove) {
//            game.insertAction(new playerStanding(game, this.player, true, false));
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
class playerLedgeJump extends Action {
    public int layer = 131;
    float xDist, yDist;

    Vector2 initialPos, targetPos;

    Sprite shadow;

    int timer1 = 0;

    ArrayList<Integer> yMovesList = new ArrayList<Integer>();

    ArrayList<Map<String, Sprite>> spriteAnim = new ArrayList<Map<String, Sprite>>();
    // Map<String, ArrayList<Sprite>> spritesAnimList = new HashMap<String, ArrayList<Sprite>>();
    Player player;
    public playerLedgeJump(Game game) {
        this(game, game.player);
    }

    public playerLedgeJump(Game game, Player player) {
        this.player = player;
        this.initialPos = new Vector2(this.player.position);
        if (this.player.dirFacing == "up") {
            this.targetPos = new Vector2(this.player.position.x, this.player.position.y+32);
        }
        else if (this.player.dirFacing == "down") {
            this.targetPos = new Vector2(this.player.position.x, this.player.position.y-32);
        }
        else if (this.player.dirFacing == "left") {
            this.targetPos = new Vector2(this.player.position.x-32, this.player.position.y);
        }
        else if (this.player.dirFacing == "right") {
            this.targetPos = new Vector2(this.player.position.x+32, this.player.position.y);
        }

        // shadow sprite
        Texture shadowText = new Texture(Gdx.files.internal("shadow1.png"));
        this.shadow = new Sprite(shadowText, 0, 0, 16, 16);

        // play sound
        game.insertAction(new PlaySound("ledge1", new DoneAction()));

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
            if (game.player.dirFacing == "up") {
                game.player.position.y +=1;
                game.cam.position.y +=1;
            }
            else if (game.player.dirFacing == "down") {
                game.cam.position.y -=1;
                game.player.position.y -=1;
            }
            else if (game.player.dirFacing == "left") {
                game.player.position.x -=1;
                game.cam.position.x -=1;
            }
            else if (game.player.dirFacing == "right") {
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

            Action playerStanding = new playerStanding(game);
            game.insertAction(playerStanding);
            // playerStanding.step(game); // step to detect movement right away
            game.actionStack.remove(this);
        }

        this.timer1++;
    }

    public void remoteStep(Game game) {
        this.player.network.syncTimer++;
        if ( this.timer1 < 32) {
            if (this.player.dirFacing == "up") {
                this.player.position.y +=1;
            }
            else if (this.player.dirFacing == "down") {
                this.player.position.y -=1;
            }
            else if (this.player.dirFacing == "left") {
                this.player.position.x -=1;
            }
            else if (this.player.dirFacing == "right") {
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
        if (this.player.dirFacing == "up") {
            this.targetPos = new Vector2(this.player.position.x, this.player.position.y+16);
        }
        else if (this.player.dirFacing == "down") {
            this.targetPos = new Vector2(this.player.position.x, this.player.position.y-16);
        }
        else if (this.player.dirFacing == "left") {
            this.targetPos = new Vector2(this.player.position.x-16, this.player.position.y);
        }
        else if (this.player.dirFacing == "right") {
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
            if (game.player.dirFacing == "up") {
                game.player.position.y +=1;
                game.cam.position.y +=1;
            }
            else if (game.player.dirFacing == "down") {
                game.cam.position.y -=1;
                game.player.position.y -=1;
            }
            else if (game.player.dirFacing == "left") {
                game.player.position.x -=1;
                game.cam.position.x -=1;
            }
            else if (game.player.dirFacing == "right") {
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

//            if (currTile != null && temp != null && currTile.attrs.get("ledge")) {
//                game.insertAction(new PlayerLedgeJumpFast(game, this.player));
//            }
//            else {
//                Action playerStanding = new playerStanding(game);
//                game.insertAction(playerStanding);
//            }
            Action playerStanding = new playerStanding(game);
            game.insertAction(playerStanding);
            game.actionStack.remove(this);

        }

        this.timer1++;
    }

    public void remoteStep(Game game) {
        this.player.network.syncTimer++;
        if ( this.timer1 < 16) {
            if (this.player.dirFacing == "up") {
                this.player.position.y +=1;
            }
            else if (this.player.dirFacing == "down") {
                this.player.position.y -=1;
            }
            else if (this.player.dirFacing == "left") {
                this.player.position.x -=1;
            }
            else if (this.player.dirFacing == "right") {
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
class playerMoving extends Action {
    public int layer = 150;
    Vector2 initialPos; // track distance of movement

    Vector2 targetPos;
    float xDist, yDist;
    // float speed = 50.0f;

    boolean alternate = false;

    Player player;
    public playerMoving(Game game, boolean alternate) {
        this(game, game.player, alternate);
    }

    public playerMoving(Game game, Player player, boolean alternate) {
        this.alternate = alternate;
        this.player = player;
        this.initialPos = new Vector2(this.player.position);
        if (this.player.dirFacing == "up") {
            this.targetPos = new Vector2(this.player.position.x, this.player.position.y+16);
        }
        else if (this.player.dirFacing == "down") {
            this.targetPos = new Vector2(this.player.position.x, this.player.position.y-16);
        }
        else if (this.player.dirFacing == "left") {
            this.targetPos = new Vector2(this.player.position.x-16, this.player.position.y);
        }
        else if (this.player.dirFacing == "right") {
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

        if (game.player.dirFacing == "up") {
            game.player.position.y +=1;
            game.cam.position.y +=1;
        }
        else if (game.player.dirFacing == "down") {
            game.player.position.y -=1;
            game.cam.position.y -=1;
        }
        else if (game.player.dirFacing == "left") {
            game.player.position.x -=1;
            game.cam.position.x -=1;
        }
        else if (game.player.dirFacing == "right") {
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
        if (    (this.yDist < 13 && this.yDist > 2)
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
            }

            game.player.position.set(this.targetPos);
            game.cam.position.set(this.targetPos.x+16, this.targetPos.y,0);

            Action standingAction = new playerStanding(game, !this.alternate);
            game.insertAction(standingAction);
            standingAction.step(game); // decide where to move // doesn't actually seem to do much
            game.actionStack.remove(this);
        }
    }

    public void remoteStep(Game game) {
        this.player.network.syncTimer++;
        // allows game to pause in middle of run
        if (this.player.dirFacing == "up") {
            this.player.position.y +=1;
        }
        else if (this.player.dirFacing == "down") {
            this.player.position.y -=1;
        }
        else if (this.player.dirFacing == "left") {
            this.player.position.x -=1;
        }
        else if (this.player.dirFacing == "right") {
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
class playerRunning extends Action {
    public int layer = 150;
    Vector2 initialPos; // track distance of movement

    Vector2 targetPos;
    float xDist, yDist;
    // float speed = 50.0f;

    boolean alternate = false;

    Player player;
    public playerRunning(Game game, boolean alternate) {
        this(game, game.player, alternate);
    }

    public playerRunning(Game game, Player player, boolean alternate) {
        this.alternate = alternate;
        this.player = player;

        this.initialPos = new Vector2(this.player.position);
        if (this.player.dirFacing == "up") {
            this.targetPos = new Vector2(this.player.position.x, this.player.position.y+16);
        }
        else if (this.player.dirFacing == "down") {
            this.targetPos = new Vector2(this.player.position.x, this.player.position.y-16);
        }
        else if (this.player.dirFacing == "left") {
            this.targetPos = new Vector2(this.player.position.x-16, this.player.position.y);
        }
        else if (this.player.dirFacing == "right") {
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

        if (game.player.dirFacing == "up") {
            this.player.position.y +=speed;
            game.cam.position.y +=speed;
        }
        else if (game.player.dirFacing == "down") {
            game.player.position.y -=speed;
            game.cam.position.y -=speed;
        }
        else if (game.player.dirFacing == "left") {
            game.player.position.x -=speed;
            game.cam.position.x -=speed;
        }
        else if (game.player.dirFacing == "right") {
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

        // System.out.println("Debug: " + String.valueOf(game.player.position.y));

        // when we've moved 16 pixels
        // if button pressed, change dir and move that direction
        // else, stand still again
        if (this.xDist >= 16 || this.yDist >= 16) {
            if (game.map.tiles.get(this.targetPos) != null && game.map.tiles.get(this.targetPos).routeBelongsTo != null) {
                Route newRoute = game.map.tiles.get(this.targetPos).routeBelongsTo;
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
            }

            game.player.position.set(this.targetPos);
            game.cam.position.set(this.targetPos.x+16, this.targetPos.y,0);
            game.cam.update();                                     // this line fixes jittering bug
            game.mapBatch.setProjectionMatrix(game.cam.combined);    // same

            Action standingAction = new playerStanding(game, !this.alternate, true); // pass true to keep running animation going
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
// this action is basically the decision-maker for
//  what to do next when a player presses a button
//  all moving states come back to here
class playerStanding extends Action {
    public int layer = 130;
    public float initialWait; // might use this to wait before moving

    boolean alternate;

    boolean checkWildEncounter = true; // TODO - remove when playWait is implemented

    boolean isRunning;

    Player player;

    /*
     * TODO: correct constructors, ie this one should call this(game, something); instead.
     */
    public playerStanding(Game game) {
        // could snap cam, and have playerMoving come here after 15 pixels. saves a little code
         // problems - timer before moving, alternating sprites
        this.alternate = true;
        this.checkWildEncounter = false;
        this.isRunning = false;
        this.player = game.player;
    }

    public playerStanding(Game game, boolean alternate) {
        // only used by playerMoving atm
        this.alternate = alternate;
        this.isRunning = false;
        this.player = game.player;
        // todo - might be able to remove above alternate code, should work atm. after 1 iter this.alternate = false, init to true
    }

    public playerStanding(Game game, boolean alternate, boolean isRunning) {
        // only used by playerMoving atm
        this.alternate = alternate;
        this.isRunning = isRunning;
        this.player = game.player;
        // todo - might be able to remove above alternate code, should work atm. after 1 iter this.alternate = false, init to true
    }

    public playerStanding(Game game, Player player, boolean alternate, boolean isRunning) {
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
        if (game.map.timeOfDay == "Night") {
            return null;
        }

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
    public int getLayer(){return this.layer;}
    public void localStep(Game game) {
        if (game.playerCanMove == false || game.player.isSleeping) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.X) && game.player.isSleeping) {
                game.player.isSleeping = false;
                Tile currTile = game.map.tiles.get(game.player.position);
                Texture text = new Texture(Gdx.files.internal("tiles/sleeping_bag1.png"));
                Sprite temp = new Sprite(text, 0, 0, 24, 16);
                temp.setPosition(currTile.overSprite.getX(), currTile.overSprite.getY());
                currTile.overSprite = temp;
            }
            return;
        }

        boolean shouldMove = false;
        Vector2 newPos = new Vector2();

        // check wild encounter
         // TODO - in future, this action will jump to a waiting action after one iteration
        if (this.checkWildEncounter == true && game.type != Game.Type.CLIENT) {
            if (checkWildEncounter(game) == true) {
                // game.actionStack.remove(this); // now using playerCanMove flag

                game.playerCanMove = false;
                game.insertAction(Battle.getIntroAction(game));
                game.currMusic.pause();
                game.currMusic = game.battle.music;
                game.currMusic.stop();
                game.currMusic.play();
                // game.battle.music.play(); // would rather have an action that does this?
                this.checkWildEncounter = false;
                return;
            }

            // TODO: idk where the right place for this is.
            if (game.map.tiles.get(game.player.position) != null && game.map.tiles.get(game.player.position).nameUpper.contains("door")) {
                game.insertAction(new EnterBuilding(game, new DoneAction()));
            }

            this.checkWildEncounter = false;
        }
        // else, check if the server sent an encounter
        else if (this.player.network.doEncounter != null) {
            game.playerCanMove = false;
            Network.PokemonData pokemonData = this.player.network.doEncounter.pokemonData;
            game.battle.oppPokemon = new Pokemon(pokemonData.name,
                                                 pokemonData.level,
                                                 pokemonData.generation);
            game.battle.oppPokemon.currentStats.put("hp", pokemonData.hp);
            game.insertAction(Battle.getIntroAction(game));
            game.currMusic.pause();
            game.currMusic = game.battle.music;
            game.currMusic.stop();
            game.currMusic.play();
            this.checkWildEncounter = false;
            this.player.network.doEncounter = null;
            return;
        }

        // Check if player wants to access the menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.insertAction(new DrawPlayerMenu.Intro(game,
                              new DrawPlayerMenu(game,
                              new WaitFrames(game, 1,
                              new PlayerCanMove(game,
                              new DoneAction())))));
            // game.actionStack.remove(this);
            game.playerCanMove = false;
            return;
        }

        // check user input
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            // if the block below isn't solid,
            // exec down move
            game.player.dirFacing = "up";
            newPos = new Vector2(game.player.position.x, game.player.position.y+16);
            shouldMove = true;
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            game.player.dirFacing = "down";
            newPos = new Vector2(game.player.position.x, game.player.position.y-16);
            shouldMove = true;
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            game.player.dirFacing = "left";
            newPos = new Vector2(game.player.position.x-16, game.player.position.y);
            shouldMove = true;
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            game.player.dirFacing = "right";
            newPos = new Vector2(game.player.position.x+16, game.player.position.y);
            shouldMove = true;
        }

        // check if input pressed
        if (Gdx.input.isKeyJustPressed(Input.Keys.C) && game.player.isBuilding) {
            game.player.buildTileIndex -= 1;
            if (game.player.buildTileIndex < 0) {
                game.player.buildTileIndex = game.player.buildTiles.size() - 1;
            }
            game.player.currBuildTile = game.player.buildTiles.get(game.player.buildTileIndex);
            game.insertAction(new requirementNotify(game, game.player.currBuildTile.name));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.V) && game.player.isBuilding) {
            game.player.buildTileIndex += 1;
            if (game.player.buildTileIndex >= game.player.buildTiles.size()) {
                game.player.buildTileIndex = 0;
            }
            game.player.currBuildTile = game.player.buildTiles.get(game.player.buildTileIndex);
            game.insertAction(new requirementNotify(game, game.player.currBuildTile.name));
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            // place the built tile
            Vector2 pos = new Vector2(0,0);
            if (game.player.dirFacing == "right") {
                pos = new Vector2(game.player.position.cpy().add(16,0));
            }
            else if (game.player.dirFacing == "left") {
                pos = new Vector2(game.player.position.cpy().add(-16,0));
            }
            else if (game.player.dirFacing == "up") {
                pos = new Vector2(game.player.position.cpy().add(0,16));
            }
            else if (game.player.dirFacing == "down") {
                pos = new Vector2(game.player.position.cpy().add(0,-16));
            }
            Tile currTile = game.map.tiles.get(pos);
            if (currTile.nameUpper.equals("sleeping_bag1")) {
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
                if (currTile.attrs.containsKey("solid") && !currTile.attrs.get("solid") && requirementsMet) {
                    // TODO: remove commented lines
//                    currTile.overSprite = new Sprite(game.player.currBuildTile.sprite);
//                    currTile.name = game.player.currBuildTile.name;
//                    currTile.overSprite.setPosition(pos.x, pos.y);
//                    currTile.attrs.put("cuttable", true); // test this
//                    currTile.attrs.put("solid", true);
                    Tile newTile = new Tile(currTile.name, game.player.currBuildTile.name,
                                            currTile.position.cpy(), true, currTile.routeBelongsTo);
                    if (game.type != Game.Type.CLIENT) {
                        game.map.tiles.remove(currTile.position.cpy());
                        game.map.tiles.put(currTile.position.cpy(), newTile);
                    }
                    else {
                        // Send request to build new tile to server
                        // Don't update locally yet, server will send back TileData if it succeeds.
                        game.client.sendTCP(new Network.TileData(newTile));
                    }
                    game.insertAction(new PlaySound("strength1",
                                                     new DoneAction()));
                    game.playerCanMove = false;
                    game.insertAction(new WaitFrames(game, 30,
                                                     new PlayerCanMove(game,
                                                     new DoneAction())));
                    this.detectIsHouseBuilt(game, currTile);
                    // add tile to interiors
                    if (!game.player.currBuildTile.nameUpper.contains("campfire") && !game.player.currBuildTile.nameUpper.contains("sleeping_bag")) {
                        Tile interiorTile;
                        if (currTile.nameUpper.contains("door")) {
                            interiorTile = new Tile("house5_floor_rug1", currTile.position.cpy());
                        }
                        else {
                            interiorTile = new Tile("house5_floor1", currTile.position.cpy());
                        }
                        game.map.interiorTiles.get(game.map.interiorTilesIndex).put(currTile.position.cpy(), interiorTile);
                    }

                    // deduct required materials
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
                    // choose if found anything
                    Action nextAction = new PlayerCanMove(game, new DoneAction());
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
                    game.insertAction(new CutTreeAnim(game, game.map.tiles.get(pos),
                                                     new DoneAction()));
                    // place black tile at location
                    // TODO: if there are ever interior objects, how do you preserve those?
                    Tile interiorTile = new Tile("black1", currTile.position.cpy());
                    game.map.interiorTiles.get(game.map.interiorTilesIndex).put(currTile.position.cpy(), interiorTile);
                    game.playerCanMove = false;

                    // get items from tile
                    if (!currTile.items.isEmpty()) {
                        for (String item : currTile.items.keySet()) {
                            game.insertAction(new itemPickupNotify(game, item, currTile.items.get(item)));
                            if (game.player.itemsDict.containsKey(item)) {
                                int currQuantity = game.player.itemsDict.get(item);
                                game.player.itemsDict.put(item, currQuantity+currTile.items.get(item));
                            }
                            else {
                                game.player.itemsDict.put(item, currTile.items.get(item));
                            }
                        }
                    }
                }
                this.detectIsHouseBuilt(game, currTile);
            }
            else {
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
                                                           Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)));
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
                // do leave building anim, then player travels down one space
                game.insertAction(new EnterBuilding(game, "exit",
                                  new playerMoving(game, this.alternate)));
                return;
            }
            // Check if moving into empty space to avoid temp.attr checks afterwards
            // If player is pressing space key (debug mode), just move through the object.
            if (temp == null || Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                // No tile here, so just move normally
                if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) { // check if player should be running
                    game.insertAction(new playerRunning(game, this.alternate));
                }
                else {
                    game.insertAction(new playerMoving(game, this.alternate));
                }
                return;
            }
            if (temp.attrs.get("solid")) {
                game.insertAction(new playerBump(game));
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
                        game.insertAction(new playerBump(game));
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
                game.insertAction(new playerBump(game));
                return;
            }
            if (temp.attrs.get("ledge") && !temp.ledgeDir.equals("up")) {
                // check that the tile the player will jump into isn't solid
                Vector2 diff = newPos.cpy().sub(game.player.position);
                Tile fartherOutTile = game.map.tiles.get(newPos.cpy().add(diff));
                if (temp.ledgeDir.equals(game.player.dirFacing) && (fartherOutTile == null || (!fartherOutTile.attrs.get("solid") && !fartherOutTile.attrs.get("ledge")))) {
                    // jump over ledge
                    game.insertAction(new playerLedgeJump(game));
                }
                else {
                    // bump into ledge
                    game.insertAction(new playerBump(game));
                }
                return;
            }
            // Check if player should be running
            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                game.insertAction(new playerRunning(game, this.alternate));
                return;
            }
            game.insertAction(new playerMoving(game, this.alternate));
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
//            if (Gdx.input.isKeyJustPressed(Input.Keys.X) && game.player.isSleeping) {
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
//                game.insertAction(new EnterBuilding(game, new DoneAction()));
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

            // if player is at edge of loading zone, send more tiles to client
            if (this.player.position.x <= this.player.network.loadingZoneBL.x+96) {
                this.player.network.loadingZoneBL.add(-128, 0);
                this.player.network.loadingZoneTR.add(-128, 0);
                Network.MapTiles mapTiles = new Network.MapTiles();
                for (Vector2 position = this.player.network.loadingZoneBL.cpy();
                     position.y < this.player.network.loadingZoneTR.y; position.add(16, 0)) {
                    Tile tile = game.map.tiles.get(position);
                    if (tile == null) {
                        continue;
                    }
                    mapTiles.tiles.add(new Network.TileData(tile));
                    if (position.x >= player.position.x) {
                        position.add(0, 16);
                        position.x = player.network.loadingZoneBL.x;
                    }
                    // the larger the number, the more the client hangs when receiving.
                    // 16 seemed to cause little hangup.
                    if (mapTiles.tiles.size() >= 16) {
                        game.server.sendToTCP(this.player.network.connectionId, mapTiles);
                        mapTiles.tiles.clear();
                    }
                }
                game.server.sendToTCP(this.player.network.connectionId, mapTiles);
            }
            if (this.player.position.x >= this.player.network.loadingZoneTR.x-96) {
                this.player.network.loadingZoneBL.add(128, 0);
                this.player.network.loadingZoneTR.add(128, 0);
                Network.MapTiles mapTiles = new Network.MapTiles();
                for (Vector2 position = new Vector2(this.player.position.x, this.player.network.loadingZoneBL.y);
                     position.y < this.player.network.loadingZoneTR.y; position.add(16, 0)) {
                    Tile tile = game.map.tiles.get(position);
                    if (tile == null) {
                        continue;
                    }
                    mapTiles.tiles.add(new Network.TileData(tile));
                    if (position.x >= this.player.network.loadingZoneTR.x) {
                        position.add(0, 16);
                        position.x = this.player.position.x;
                    }
                    if (mapTiles.tiles.size() >= 16) {
                        game.server.sendToTCP(this.player.network.connectionId, mapTiles);
                        mapTiles.tiles.clear();
                    }
                }
                game.server.sendToTCP(this.player.network.connectionId, mapTiles);
            }
            if (this.player.position.y <= this.player.network.loadingZoneBL.y+96) {
                this.player.network.loadingZoneBL.add(0, -128);
                this.player.network.loadingZoneTR.add(0, -128);
                Network.MapTiles mapTiles = new Network.MapTiles();
                for (Vector2 position = this.player.network.loadingZoneBL.cpy();
                     position.y < this.player.position.y; position.add(16, 0)) {
                    Tile tile = game.map.tiles.get(position);
                    if (tile == null) {
                        continue;
                    }
                    mapTiles.tiles.add(new Network.TileData(tile));
                    if (position.x >= this.player.network.loadingZoneTR.x) {
                        position.add(0, 16);
                        position.x = this.player.network.loadingZoneBL.x;
                    }
                    if (mapTiles.tiles.size() >= 16) {
                        game.server.sendToTCP(this.player.network.connectionId, mapTiles);
                        mapTiles.tiles.clear();
                    }
                }
                game.server.sendToTCP(this.player.network.connectionId, mapTiles);
            }
            if (this.player.position.y >= this.player.network.loadingZoneTR.y-96) {
                this.player.network.loadingZoneBL.add(0, 128);
                this.player.network.loadingZoneTR.add(0, 128);
                Network.MapTiles mapTiles = new Network.MapTiles();
                for (Vector2 position = new Vector2(this.player.network.loadingZoneBL.x, this.player.position.y);
                     position.y < this.player.network.loadingZoneTR.y; position.add(16, 0)) {
                    Tile tile = game.map.tiles.get(position);
                    if (tile == null) {
                        continue;
                    }
                    mapTiles.tiles.add(new Network.TileData(tile));
                    if (position.x >= this.player.network.loadingZoneTR.x) {
                        position.add(0, 16);
                        position.x = this.player.network.loadingZoneBL.x;
                    }
                    if (mapTiles.tiles.size() >= 16) {
                        game.server.sendToTCP(this.player.network.connectionId, mapTiles);
                        mapTiles.tiles.clear();
                    }
                }
                game.server.sendToTCP(this.player.network.connectionId, mapTiles);
            }
        }

        // draw the sprite corresponding to player direction
        if (this.player.network.isRunning == true) {
            this.player.currSprite = new Sprite(this.player.standingSprites.get(this.player.dirFacing+"_running"));
        }
        else {
            this.player.currSprite = new Sprite(this.player.standingSprites.get(this.player.dirFacing));
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
            Tile currTile = game.map.tiles.get(this.player.position);
            Tile temp = game.map.tiles.get(newPos);

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
                // No tile here, so just move normally
                if (this.player.network.isRunning) {
                    game.insertAction(new playerRunning(game, this.player, this.alternate));
                }
                else {
                    game.insertAction(new playerMoving(game, this.player, this.alternate));
                }
                return;
            }
            if (temp.attrs.get("solid")) {
                game.insertAction(new playerBump(game, this.player));
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
                        game.insertAction(new playerBump(game, this.player));
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
            if (currTile.attrs.get("ledge") && currTile.ledgeDir.equals("up") && this.player.dirFacing.equals("up")) {
                // jump over ledge
                game.insertAction(new PlayerLedgeJumpFast(game, this.player));
                return;
            }
            if (temp.attrs.get("ledge") && temp.ledgeDir.equals("up") && this.player.dirFacing.equals("down")) {
                game.insertAction(new playerBump(game, this.player));
                return;
            }
            if (temp.attrs.get("ledge") && !temp.ledgeDir.equals("up")) {
                if (temp.ledgeDir.equals(this.player.dirFacing)) {
                    // jump over ledge
                    game.insertAction(new playerLedgeJump(game, this.player));
                }
                else {
                    // bump into ledge
                    game.insertAction(new playerBump(game, this.player));
                }
                return;
            }
            // Check if player should be running
            if (this.player.network.isRunning) {
                game.insertAction(new playerRunning(game, this.player, this.alternate));
            }
            else {
                game.insertAction(new playerMoving(game, this.player, this.alternate));
            }
            if (game.type == Game.Type.SERVER) {
                // check wild encounter on next position, send back if yes
                Pokemon pokemon = checkWildEncounter(game, newPos);
                if (pokemon != null) {
                    // TODO: this may stop player from moving server-side
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
        if (this.player.type == Player.Type.LOCAL) {
            this.localStep(game);
        }
        else if (this.player.type == Player.Type.REMOTE) {
            this.remoteStep(game);
        }
    }

}

class requirementNotify extends Action {
  public int layer = 114;
  Sprite bgSprite;
  int signCounter = 100;;

  String tileName;
  String text = "";
  public requirementNotify(Game game, String tileName) {
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
class spawnGhost extends Action {
    public int layer = 114;
    Sprite[] sprites;

    int part1;

    int part2;
    int part3;
    Vector2 position;

    public spawnGhost(Game game, Vector2 position) {
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
            game.insertAction(new PlaySound("ghost1", new DoneAction()));
        }
        if (part2 == 32) {
            game.insertAction(new PlaySound("ghost1", new DoneAction()));
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
        game.currMusic.pause();
        Music music = Gdx.audio.newMusic(Gdx.files.internal("night1_chase1.ogg"));
        music.setLooping(true);
        music.setVolume(.7f);
        game.currMusic = music;
//        game.map.currRoute.music = music; // TODO - how to switch to normal after defeating
        game.currMusic.play();

        game.playerCanMove = true;
        game.actionStack.remove(this);
        game.insertAction(new drawGhost(game, this.position));
    }

}
