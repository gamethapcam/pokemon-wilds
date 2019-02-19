package com.pkmngen.game;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;


//includes player global structure (the class)
 //as well as all actions associated with player class

//no problems with just snapping camera to player for now
 //might have issues later

//not using deltatime
 //would be nice if libgdx let me throttle the game somehow, i don't think it will tho

//code in playerStanding class needs to be made easier to read

//player sprite needs to be moved up a little

public class Player {

    //current player direction
    public String dirFacing = "down";
    
    //movement sprites
    Map<String, Sprite> standingSprites = new HashMap<String, Sprite>();
    Map<String, Sprite> movingSprites = new HashMap<String, Sprite>();
    Map<String, Sprite> altMovingSprites = new HashMap<String, Sprite>();
    
    Vector2 position;
    
    Sprite currSprite = new Sprite();
    
    Sprite battleSprite;
    
    String name; //player name
    
    //players pokemon
     //functions as pokemon storage in demo
    ArrayList<Pokemon> pokemon; 
    Pokemon currPokemon; //needed for displaying current pokemon
    
    //items that player currently possesses
    ArrayList<String> itemsList;
    Map<String, Integer> itemsDict; //had idea to make this an array of 'ItemActions' instead; would have .name attribute for printing name in menus
     //each action would perform action; would still need to initialize the action when adding tho
     //decided this way was less intuitive.
    
    int adrenaline;  //was 'streak'
    //demo mechanic - catching in a row without oppPokemon fleeing builds streak
     //displayed as 'adrenaline'
     //based off of catch rate
    
    //this would be if doing android
     //action would set these
    //Map<String, Integer> buttonPressed = new HashMap<String, Integer>();
    
    String currState; //need for ghost spawn
    boolean isBuilding = false; // player has building tile in front of them
    Tile currBuildTile; // which tile will be built next
    int buildTileIndex = 0;
    ArrayList<Tile> buildTiles = new ArrayList<Tile>();
    
    public Player() {
        
        //set player standing/moving sprites
        Texture playerText = new Texture(Gdx.files.internal("player1_sheet1.png"));
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
        
        //running sprites
        playerText = new Texture(Gdx.files.internal("player1_sheet2.png"));
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
        
        
        //battle portrait sprite
        Texture text = new Texture(Gdx.files.internal("battle/player_back1.png"));
        this.battleSprite = new Sprite(text, 0, 0, 28, 28);
        
        //set initial position
        this.position = new Vector2(0,0);

        this.currSprite = new Sprite(this.standingSprites.get(this.dirFacing));
        //currSprite.setPosition(0, 0);
        
        //default name - change later
        this.name = "AAAA";
        this.pokemon = new ArrayList<Pokemon>();
        
        this.adrenaline = 0;
        this.currState = "";
        
        // currBuildTile
        this.buildTiles.add(new Tile("house1_middle1", new Vector2(0,0)));
        this.buildTiles.add(new Tile("house1_left1", new Vector2(0,0)));
        this.buildTiles.add(new Tile("house1_right1", new Vector2(0,0)));
        this.buildTiles.add(new Tile("house1_door1", new Vector2(0,0)));
        this.buildTiles.add(new Tile("house1_roof_middle1", new Vector2(0,0)));
        this.buildTiles.add(new Tile("house1_roof_left1", new Vector2(0,0)));
        this.buildTiles.add(new Tile("house1_roof_right1", new Vector2(0,0)));
        this.currBuildTile = this.buildTiles.get(this.buildTileIndex);
        
        // debug - initialize items list
        this.itemsList = new ArrayList<String>();
        this.itemsList.add("Master Ball");
        this.itemsList.add("Ultra Ball");
        this.itemsList.add("Great Ball");
        this.itemsList.add("Safari Ball");
        this.itemsList.add("Safari Ball2");
        this.itemsList.add("Safari Ball3");
        this.itemsList.add("Safari Ball4");
        this.itemsDict = new HashMap<String, Integer>();
        this.itemsDict.put("Safari Ball", 99);
        
    }
    
}



//draw character action
 //need to build in 'button press delay'

//this action is basically the decision-maker for
 //what to do next when a player presses a button 
 //all moving states come back to here
//TODO - game code currently uses 'playerCanMove' flag,
 //and relies on removing from AS. will switch when 
 //grass has it's own 'onWalkOver' function
class playerStanding extends Action {

    public int layer = 130;
    public int getLayer(){return this.layer;}
    
    public float initialWait; //might use this to wait before moving
    
    boolean alternate;
    
    boolean checkWildEncounter = true; //TODO - remove when playWait is implemented
    
    boolean isRunning;
    
    @Override
    public void step(PkmnGen game) {
        
        if (game.playerCanMove == false) {
            return;
        }
        
        
        boolean shouldMove = false;
        Vector2 newPos = new Vector2();
        
        //check wild encounter
         //TODO - in future, this action will jump to a waiting action after one iteration
        if (this.checkWildEncounter == true) {
            if (checkWildEncounter(game) == true) {
                //game.actionStack.remove(this); //now using playerCanMove flag
                
                game.playerCanMove = false;
                PublicFunctions.insertToAS(game, Battle_Actions.get(game)); 
                game.currMusic.pause();
                game.currMusic = game.battle.music;
                game.currMusic.stop();
                game.currMusic.play();
                //game.battle.music.play(); //would rather have an action that does this?
                return;
            }
            this.checkWildEncounter = false;
        }
        
        //check if player wants to access the menu
        if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            PublicFunctions.insertToAS(game,
                                       new DrawPlayerMenu.Intro(game,
                                       new DrawPlayerMenu(game,
                                       new WaitFrames(game, 1,
                                       new PlayerCanMove(game,
                                       new DoneAction())))));
            //game.actionStack.remove(this); 
            game.playerCanMove = false;
            return;
        }
        
        
        //check user input
        if(Gdx.input.isKeyPressed(Input.Keys.UP)) {
            //if the block below isn't solid,
            //exec down move
            game.player.dirFacing = "up";
            newPos = new Vector2(game.player.position.x, game.player.position.y+16);
            shouldMove = true;
        }
        else if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            game.player.dirFacing = "down";
            newPos = new Vector2(game.player.position.x, game.player.position.y-16);
            shouldMove = true;
        }
        else if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            game.player.dirFacing = "left";
            newPos = new Vector2(game.player.position.x-16, game.player.position.y);
            shouldMove = true;
        }
        else if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            game.player.dirFacing = "right";
            newPos = new Vector2(game.player.position.x+16, game.player.position.y);
            shouldMove = true;
        }
        
        //check if input pressed
        if(Gdx.input.isKeyJustPressed(Input.Keys.C) && game.player.isBuilding) {
            game.player.buildTileIndex -= 1;
            if (game.player.buildTileIndex <= 0) {
                game.player.buildTileIndex = game.player.buildTiles.size() - 1;
            }
            game.player.currBuildTile = game.player.buildTiles.get(game.player.buildTileIndex);
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.V) && game.player.isBuilding) {
            game.player.buildTileIndex += 1;
            if (game.player.buildTileIndex >= game.player.buildTiles.size()) {
                game.player.buildTileIndex = 0;
            }
            game.player.currBuildTile = game.player.buildTiles.get(game.player.buildTileIndex);
        }
        
        if(Gdx.input.isKeyJustPressed(Input.Keys.X)) {
            if (game.player.isBuilding) {
                // player wants to stop building
                game.player.isBuilding = false;
            }
        }
        
        if(Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            if (game.player.isBuilding) {
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
//                Tile newTile = new Tile(game.player.currBuildTile.name, pos.cpy());
//                game.map.tiles.remove(pos);
//                game.map.tiles.put(pos.cpy(), newTile);
                // TODO: two tiles at some loc, so that you can remove building tiles and under tile remains?
                //  or keep track of 'underTile' somewhere
                // TODO: this won't work for doors
                Tile currTile = game.map.tiles.get(pos);
                currTile.overSprite = new Sprite(game.player.currBuildTile.sprite);
                currTile.overSprite.setPosition(pos.x, pos.y);
                currTile.attrs.put("solid", true);
            }
            else {
                //look at diff tile depending on where facing
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
                //calling this will trigger the tiles' onPressA function
                 //might be sign (text), character, etc.
                Tile temp = game.map.tiles.get(pos);
                if (temp != null) {
                    temp.onPressA(game);
                }
            }
        }

        if (shouldMove == true) {
            Tile temp = game.map.tiles.get(newPos);
            if (temp == null) { //need this check to avoid attr checks after this if null
                //no tile here, so just move normally
                if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) { //check if player should be running
                    PublicFunctions.insertToAS(game, new playerRunning(game, this.alternate));
                }
                else {
                    PublicFunctions.insertToAS(game, new playerMoving(game, this.alternate));
                }
                game.actionStack.remove(this);
            }
            else if (temp.attrs.get("solid") == true) {
                    PublicFunctions.insertToAS(game, new playerBump(game));
                    game.actionStack.remove(this);
            }
            else if (temp.attrs.get("ledge") == true) {
                if (temp.ledgeDir == game.player.dirFacing) {
                    //jump over ledge
                    PublicFunctions.insertToAS(game, new playerLedgeJump(game));
                    game.actionStack.remove(this);
                }
                else {
                    //bump into ledge
                    PublicFunctions.insertToAS(game, new playerBump(game));
                    game.actionStack.remove(this);
                }
            }
            else {
                if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) { //check if player should be running
                    PublicFunctions.insertToAS(game, new playerRunning(game, this.alternate));
                }
                else {
                    PublicFunctions.insertToAS(game, new playerMoving(game, this.alternate));
                }
                game.actionStack.remove(this);
            }
        }
        
        //draw the sprite corresponding to player direction
        
        if (this.isRunning == true) {  //check running
            game.player.currSprite = new Sprite(game.player.standingSprites.get(game.player.dirFacing+"_running"));
        }
        else {
            game.player.currSprite = new Sprite(game.player.standingSprites.get(game.player.dirFacing));
        }
        
        this.alternate = false;
        this.isRunning = false;
        
    }
    
    public playerStanding(PkmnGen game) {
        
        //could snap cam, and have playerMoving come here after 15 pixels. saves a little code
         //problems - timer before moving, alternating sprites
        this.alternate = true;
        this.checkWildEncounter = false;
        this.isRunning = false;
    }
    public playerStanding(PkmnGen game, boolean alternate) {

        //only used by playerMoving atm
        this.alternate = alternate;
        this.isRunning = false;
        //todo - might be able to remove above alternate code, should work atm. after 1 iter this.alternate = false, init to true
    }
    public playerStanding(PkmnGen game, boolean alternate, boolean isRunning) {

        //only used by playerMoving atm
        this.alternate = alternate;
        this.isRunning = isRunning;
        //todo - might be able to remove above alternate code, should work atm. after 1 iter this.alternate = false, init to true
    }
    
    //when moving to tile, no chance of encounter unless continuing to move
     //i think the real game uses an encounter table or something
    boolean checkWildEncounter(PkmnGen game) {
        
        //no encounters at night (subject to change)
        if (game.map.timeOfDay == "Night") {
            return false;
        }
        
        Tile currTile = game.map.tiles.get(game.player.position);
        
        if (currTile != null) {
            
            //if currently on grass
            if (currTile.attrs.get("grass") == true) {
                //chance wild encounter
                int randomNum = game.map.rand.nextInt(100) + 1; //rate determine by player? //1 - 100
                if (randomNum < 20) { //encounterRate //was <= 50
                    //disable player movement 
                    //game.actionStack.remove(this); //using flag now, delete this
                    
                    
                    //select new pokemon to encounter, put it in battle struct
                    int index = game.map.rand.nextInt(game.map.currRoute.pokemon.size());
                    game.battle.oppPokemon = game.map.currRoute.pokemon.get(index);
                    
                    //System.out.println("Wild encounter.");
                    
                    return true;
                }
            }
        }
        return false;
    }

}


//draw character action
//need to build in 'button press delay'
class playerMoving extends Action {
    

    public int layer = 150;
    //changed, was 130
    //alternative is to call cam.update(blah) each draw thingy, but
    //i think that's less optimal. this action needs to happen before everything else
    public int getLayer(){return this.layer;}
    
    Vector2 initialPos; //track distance of movement
    Vector2 targetPos; 
    
    float xDist, yDist;
    //float speed = 50.0f;
    
    boolean alternate = false;
    
    @Override
    public void step(PkmnGen game) {
        

        //allows game to pause in middle of run
        if (game.playerCanMove == false) {
            return;
        }
        
        
        //can consider doing skipping here if I need to slow down animation
        //bug - have to add 1 to cam position at beginning of each iteration.
         //probably related to occasionaly shakiness, which is probably related to floats
        
        //while you haven't moved 16 pixels, 
         //move in facing direction
        
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
        
        //this is needed for batch to draw according to cam
        game.cam.update();
        game.batch.setProjectionMatrix(game.cam.combined);
        
        //if u remove the below check, youll notice that there's a bit of 
         //movement that you don't want
        if(    (this.yDist < 13 && this.yDist > 2)
            || (this.xDist < 13 && this.xDist > 2)) {
            if (this.alternate == true) {
                //game.batch.draw(game.player.altMovingSprites.get(game.player.dirFacing), game.player.position.x, game.player.position.y);
                game.player.currSprite = game.player.altMovingSprites.get(game.player.dirFacing);
            }
            else {
                //game.batch.draw(game.player.movingSprites.get(game.player.dirFacing), game.player.position.x, game.player.position.y);
                game.player.currSprite = game.player.movingSprites.get(game.player.dirFacing);
            }
        }
        else {
            //game.batch.draw(game.player.standingSprites.get(game.player.dirFacing), game.player.position.x, game.player.position.y);
            game.player.currSprite = game.player.standingSprites.get(game.player.dirFacing);
        }
        
        //System.out.println("Debug: " + String.valueOf(game.player.position.y));
        
        //when we've moved 16 pixels
        //if button pressed, change dir and move that direction
        //else, stand still again
        if (this.xDist >= 16 || this.yDist >= 16) {

            game.player.position.set(this.targetPos);
            game.cam.position.set(this.targetPos.x+16, this.targetPos.y,0);
            
            Action standingAction = new playerStanding(game, !this.alternate);
            PublicFunctions.insertToAS(game, standingAction);
            standingAction.step(game); //decide where to move //doesn't actually seem to do much
            game.actionStack.remove(this);
            
        }
    }
    
    public playerMoving(PkmnGen game, boolean alternate) {
        
        this.alternate = alternate;
        
        this.initialPos = new Vector2(game.player.position);
        if (game.player.dirFacing == "up") {
            this.targetPos = new Vector2(game.player.position.x, game.player.position.y+16);
        }
        else if (game.player.dirFacing == "down") {
            this.targetPos = new Vector2(game.player.position.x, game.player.position.y-16);
        }
        else if (game.player.dirFacing == "left") {
            this.targetPos = new Vector2(game.player.position.x-16, game.player.position.y);
        }
        else if (game.player.dirFacing == "right") {
            this.targetPos = new Vector2(game.player.position.x+16, game.player.position.y);
        }
    }
}


//note - this action is nearly identical to playerMoving. Keeping separate though, 
 //for simplicity
 //differences are - player sprites, movement speed
class playerRunning extends Action {
    

    public int layer = 150;
    //changed, was 130
    //alternative is to call cam.update(blah) each draw thingy, but
    //i think that's less optimal. this action needs to happen before everything else
    public int getLayer(){return this.layer;}
    
    Vector2 initialPos; //track distance of movement
    Vector2 targetPos; 
    
    float xDist, yDist;
    //float speed = 50.0f;
    
    boolean alternate = false;
    
    @Override
    public void step(PkmnGen game) {
        
        //allows game to pause in middle of run
        if (game.playerCanMove == false) {
            return;
        }
        
        
        //can consider doing skipping here if I need to slow down animation
        //bug - have to add 1 to cam position at beginning of each iteration.
         //probably related to occasionaly shakiness, which is probably related to floats
        
        //while you haven't moved 16 pixels, 
         //move in facing direction
        
        float speed = 1.6f; //this needs to add up to 16 for smoothness?
        
        if (game.player.dirFacing == "up") {
            game.player.position.y +=speed;
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
        
        //this is needed for batch to draw according to cam
        game.cam.update();
        game.batch.setProjectionMatrix(game.cam.combined);
        
        //if u remove the below check, youll notice that there's a bit of 
         //movement that you don't want
        String spriteString = String.valueOf(game.player.dirFacing+"_running");
        //System.out.println("spriteString: " + String.valueOf(spriteString)); //debug
        if(    (this.yDist < 13 && this.yDist > 2)
            || (this.xDist < 13 && this.xDist > 2)) {
            if (this.alternate == true) {
                //game.batch.draw(game.player.altMovingSprites.get(game.player.dirFacing), game.player.position.x, game.player.position.y);
                game.player.currSprite = game.player.altMovingSprites.get(spriteString);
            }
            else {
                //game.batch.draw(game.player.movingSprites.get(game.player.dirFacing), game.player.position.x, game.player.position.y);
                game.player.currSprite = game.player.movingSprites.get(spriteString);
            }
        }
        else {
            //game.batch.draw(game.player.standingSprites.get(game.player.dirFacing), game.player.position.x, game.player.position.y);
            game.player.currSprite = game.player.standingSprites.get(spriteString);
        }
        
        //System.out.println("Debug: " + String.valueOf(game.player.position.y));
        
        //when we've moved 16 pixels
        //if button pressed, change dir and move that direction
        //else, stand still again
        if (this.xDist >= 16 || this.yDist >= 16) {

            game.player.position.set(this.targetPos);
            game.cam.position.set(this.targetPos.x+16, this.targetPos.y,0);
            game.cam.update();                                     //this line fixes jittering bug
            game.batch.setProjectionMatrix(game.cam.combined);    //same
            
            Action standingAction = new playerStanding(game, !this.alternate, true); //pass true to keep running animation going
            PublicFunctions.insertToAS(game, standingAction);
            standingAction.step(game); //decide where to move //doesn't actually seem to do much
            game.actionStack.remove(this);
            
        }
    }
    
    public playerRunning(PkmnGen game, boolean alternate) {
        
        this.alternate = alternate;
        
        this.initialPos = new Vector2(game.player.position);
        if (game.player.dirFacing == "up") {
            this.targetPos = new Vector2(game.player.position.x, game.player.position.y+16);
        }
        else if (game.player.dirFacing == "down") {
            this.targetPos = new Vector2(game.player.position.x, game.player.position.y-16);
        }
        else if (game.player.dirFacing == "left") {
            this.targetPos = new Vector2(game.player.position.x-16, game.player.position.y);
        }
        else if (game.player.dirFacing == "right") {
            this.targetPos = new Vector2(game.player.position.x+16, game.player.position.y);
        }
        game.player.currState = "Running";
    }
}


class playerBump extends Action {

    public int layer = 130;
    public int getLayer(){return this.layer;}
    
    
    int timer = 0;
    int maxTime = 10; //20 reminded me of gold version I think
    
    
    boolean alternate = false;
    
    @Override
    public void step(PkmnGen game) {
        
        timer++;
        
        if (this.timer >= 2*maxTime ) {
            this.alternate = !this.alternate;
            this.timer = 0;
            PublicFunctions.insertToAS(game, new PlaySound("bump2", new DoneAction()));
        }
        
        if (this.timer < maxTime) {
            if (this.alternate == true) {
                //game.batch.draw(game.player.altMovingSprites.get(game.player.dirFacing), game.player.position.x, game.player.position.y);
                game.player.currSprite = game.player.altMovingSprites.get(game.player.dirFacing);
            }
            else {
                //game.batch.draw(game.player.movingSprites.get(game.player.dirFacing), game.player.position.x, game.player.position.y);
                game.player.currSprite = game.player.movingSprites.get(game.player.dirFacing);
            }
        }
        else {
            //game.batch.draw(game.player.standingSprites.get(game.player.dirFacing), game.player.position.x, game.player.position.y);
            game.player.currSprite = game.player.standingSprites.get(game.player.dirFacing);
        }
        
        //when facingDir key is released, go to playerStanding


        if(!Gdx.input.isKeyPressed(Input.Keys.UP) && game.player.dirFacing == "up") {
            PublicFunctions.insertToAS(game, new playerStanding(game));
            game.actionStack.remove(this);
        }
        else if(!Gdx.input.isKeyPressed(Input.Keys.DOWN) && game.player.dirFacing == "down") {
            PublicFunctions.insertToAS(game, new playerStanding(game));
            game.actionStack.remove(this);
        }
        else if(!Gdx.input.isKeyPressed(Input.Keys.LEFT) && game.player.dirFacing == "left") {
            PublicFunctions.insertToAS(game, new playerStanding(game));
            game.actionStack.remove(this);
        }
        else if(!Gdx.input.isKeyPressed(Input.Keys.RIGHT) && game.player.dirFacing == "right") {
            PublicFunctions.insertToAS(game, new playerStanding(game));
            game.actionStack.remove(this);
        }
    }
    
    
    
    public playerBump(PkmnGen game) {
        
        PublicFunctions.insertToAS(game, new PlaySound("bump2", new DoneAction()));
        
    }
}



//made up/right/left anim same as down
 //possible that length that shadow stays isn't perfect
class playerLedgeJump extends Action {


    public int layer = 131;
    public int getLayer(){return this.layer;}
    
    float xDist, yDist;

    Vector2 initialPos, targetPos;
    
    Sprite shadow;
    
    int timer1 = 0;
    
    ArrayList<Integer> yMovesList = new ArrayList<Integer>();
    ArrayList<Map<String, Sprite>> spriteAnim = new ArrayList<Map<String, Sprite>>();
    //Map<String, ArrayList<Sprite>> spritesAnimList = new HashMap<String, ArrayList<Sprite>>();
    
    @Override
    public void step(PkmnGen game) {
        
        //gb does a weird anim here
         //looked at frame-by-frame (ledge_anim_notes.txt text file)
        
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
                
                //use next sprite in list
                game.player.currSprite = this.spriteAnim.get(0).get(game.player.dirFacing);
                this.spriteAnim.remove(0);
            }
            
            //this is needed for batch to draw according to cam
             //always call this after updating camera
            game.cam.update();
            game.batch.setProjectionMatrix(game.cam.combined);
            
            //old sprite anim code was here
                
        }
        else {
            game.player.currSprite = game.player.standingSprites.get(game.player.dirFacing);
        }

                
        //draw shadow
        game.batch.draw(this.shadow, game.cam.position.x-16, game.cam.position.y-4);
        
        if (this.timer1 >= 38) {
            game.player.position.set(this.targetPos);
            game.cam.position.set(this.targetPos.x+16, this.targetPos.y,0);

            Action playerStanding = new playerStanding(game);
            PublicFunctions.insertToAS(game, playerStanding);
            //playerStanding.step(game); //step to detect movement right away
            game.actionStack.remove(this);
        }
        
        this.timer1++;
    }
    
    public playerLedgeJump(PkmnGen game) {
        
        this.initialPos = new Vector2(game.player.position);
        if (game.player.dirFacing == "up") {
            this.targetPos = new Vector2(game.player.position.x, game.player.position.y+32);
        }
        else if (game.player.dirFacing == "down") {
            this.targetPos = new Vector2(game.player.position.x, game.player.position.y-32);
        }
        else if (game.player.dirFacing == "left") {
            this.targetPos = new Vector2(game.player.position.x-32, game.player.position.y);
        }
        else if (game.player.dirFacing == "right") {
            this.targetPos = new Vector2(game.player.position.x+32, game.player.position.y);
        }
        
        //shadow sprite
        Texture shadowText = new Texture(Gdx.files.internal("shadow1.png"));
        this.shadow = new Sprite(shadowText, 0, 0, 16, 16);
        
        //play sound
        PublicFunctions.insertToAS(game, new PlaySound("ledge1", new DoneAction()));
        
        //below two lists are used to get exact sprite and 
         //y movement on every other frame
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
        
        //sprites to use (according to frame-by-frame)
        this.spriteAnim.add(game.player.standingSprites);
        this.spriteAnim.add(game.player.standingSprites);
        this.spriteAnim.add(game.player.movingSprites);
        this.spriteAnim.add(game.player.movingSprites);
        this.spriteAnim.add(game.player.movingSprites);
        this.spriteAnim.add(game.player.movingSprites);
        this.spriteAnim.add(game.player.standingSprites);
        this.spriteAnim.add(game.player.standingSprites);
        this.spriteAnim.add(game.player.standingSprites);
        this.spriteAnim.add(game.player.standingSprites);
        this.spriteAnim.add(game.player.altMovingSprites);
        this.spriteAnim.add(game.player.altMovingSprites);
        this.spriteAnim.add(game.player.altMovingSprites);
        this.spriteAnim.add(game.player.altMovingSprites);
        this.spriteAnim.add(game.player.standingSprites);
        this.spriteAnim.add(game.player.standingSprites);
    }
}



class drawPlayer_upper extends Action {
    

    public int layer = 115;
    public int getLayer(){return this.layer;}
    
    Sprite spritePart;

    @Override
    public void step(PkmnGen game) {

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
            game.batch.draw(game.player.currBuildTile.sprite, pos.x, pos.y);
            if (game.player.currBuildTile.overSprite != null) {
                game.batch.draw(game.player.currBuildTile.overSprite, pos.x, pos.y);
            }
        }

        this.spritePart = new Sprite(game.player.currSprite);

        //this.spritePart.setRegion(0,0,16,8);
        //this.spritePart.setRegionHeight(4);
        //this.spritePart.setSize(this.spritePart.getWidth(), 4);
        this.spritePart.setRegionY(0);
        this.spritePart.setRegionHeight(8);
        
        game.batch.draw(this.spritePart, game.player.position.x, game.player.position.y+12);

        //this needs to be set to detect collision
        game.player.currSprite.setPosition(game.player.position.x, game.player.position.y);
    }
            

    public drawPlayer_upper(PkmnGen game) {


    }
}

class drawPlayer_lower extends Action {
    

    public int layer = 130;
    public int getLayer(){return this.layer;}
    
    Sprite spritePart;

    @Override
    public void step(PkmnGen game) {
        
        this.spritePart = new Sprite(game.player.currSprite);

        //this.spritePart.setRegion(0,8,16,8);
        this.spritePart.setRegionY(8);
        this.spritePart.setRegionHeight(8);
        //this.spritePart.setSize(this.spritePart.getWidth(), 8);
        
        game.batch.draw(this.spritePart, game.player.position.x, game.player.position.y+4);
        
        
    }
            

    public drawPlayer_lower(PkmnGen game) {


    }
}



//demo of drawing sprites for ea pokemon caught
class DrawPokemonCaught extends Action {
    

    public int layer = 110;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};
    
    Sprite pokeball;

    @Override
    public void step(PkmnGen game) {

        int i = 0;
        for (Pokemon pokemon : game.player.pokemon) {
            game.floatingBatch.draw(pokeball, i*16*3, 144*3-12);
            i++;
        }
//        
//        this.pokeball.setPosition(0*16*3+12, 144*3-12*3);
//        this.pokeball.draw(game.floatingBatch);
        
    }
            

    public DrawPokemonCaught(PkmnGen game) {

        Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball1.png"));
        this.pokeball = new Sprite(text, 0, 0, 12, 12);
        this.pokeball.setScale(2);
    }
}



class PlayerCanMove extends Action {

    Action nextAction;

    @Override
    public void step(PkmnGen game) {

        game.playerCanMove = true;
        
        PublicFunctions.insertToAS(game, this.nextAction);
        game.actionStack.remove(this);
    }
    
    
    public PlayerCanMove(PkmnGen game, Action nextAction) {
        this.nextAction = nextAction;
    }
}



//for drawing the ghost that chases the player
class drawGhost extends Action {
    

    public int layer = 114;
    public int getLayer(){return this.layer;}
    
    Sprite currSprite;
    Vector2 basePos; //need this to make ghost 'float'

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
    
    @Override
    public void step(PkmnGen game) {
            
        //check if it's day or not. if not Night, despawn the ghost
        if (game.map.timeOfDay != "Night") {
            this.currSprite.draw(game.batch);
            game.actionStack.remove(this);
            PublicFunctions.insertToAS(game, new despawnGhost(this.basePos.cpy()));
            return;
        }
        
        //check if ghost pokemon is dead. if yes, remove this from AS
        
        
        //check whether player is in battle or not
        //if not, don't move the ghost at all (subject to change)
        if (game.battle.drawAction != null) { 
            this.currSprite.draw(game.batch);
            this.inBattle = true;
            this.noEncounterTimer = 0;
            return;
        }
        

        
        //wait for a while if you just exited battle
        if (inBattle == true) {
            if (noEncounterTimer % 4 >= 2) {
                this.currSprite.draw(game.batch);
            }
            this.noEncounterTimer++;
            if (this.noEncounterTimer < 128) {
                return;
            }
            inBattle = false;
        }

        //pause if player can't move
        if (game.playerCanMove == false) { 
            this.currSprite.draw(game.batch);
            return;
        }
        
        //calculate direction to player, face that direction
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
        
        //set ghost sprite to dirFacing
        this.currSprite = this.sprites.get(this.dirFacing)[this.animIndex];
        
        //game.player.position.x < this.basePos.x
        
        //modify base pos to chase player (accelerate in player direction)
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
        //apply position
        this.basePos.add(velX, velY);
        
        //x and y are sine function
        float shiftPosX = (float)Math.sin(sineTimer);
        float shiftPosY = (float)Math.sin(2*sineTimer);
        this.currSprite.setPosition(basePos.x+shiftPosX, basePos.y+shiftPosY);
        
        this.currSprite.draw(game.batch); 
        //game.batch.draw(this.currSprite, this.currSprite.getX(), this.currSprite.getY()); //TODO - remove
        
        sineTimer+=.125f;
        if (sineTimer >= 3.14*2f) {
            sineTimer = 0.0f;
        }
        
        //run ghost animation
        animFrame++;
        if (animFrame > 10) {
            animFrame = 0;
            animIndex++;
        }
        if (animIndex >= this.sprites.get(this.dirFacing).length ) {
            animIndex = 0;
        }
        
        //need to make ghost rectangle bounds smaller
        Rectangle rect = this.currSprite.getBoundingRectangle(); 
        rect.x +=16;
        rect.y +=16;
        rect.width -=2*16;
        rect.height -=2*16;
        
        //check collision. if collision, start battle with pokemon
        if (rect.overlaps(game.player.currSprite.getBoundingRectangle())) {
            //System.out.println("collision x: " + String.valueOf(this.currSprite.getBoundingRectangle().x)); //debug
            //System.out.println("collision x: " + String.valueOf(game.player.currSprite.getBoundingRectangle().x)); //debug

            game.battle.oppPokemon = this.pokemon;
            game.playerCanMove = false; 
            //todo - remove
//            PublicFunctions.insertToAS(game, new SplitAction(
//                                                new BattleIntro(
//                                                    new BattleIntro_anim1(
//                                                        new SplitAction(
//                                                            new DrawBattle(game),
//                                                            new BattleAnim_positionPlayers(game, 
//                                                                new PlaySound(game.battle.oppPokemon.name, 
//                                                                    new DisplayText(game, "A Ghost appeared!", null, null, 
//                                                                        new WaitFrames(game, 39,
//                                                                            //demo code - wildly confusing, but i don't want to write another if statement
//                                                                                game.player.adrenaline > 0 ? 
//                                                                                new DisplayText(game, ""+game.player.name+" has ADRENALINE "+Integer.toString(game.player.adrenaline)+"!", null, null,
//                                                                                    new PrintAngryEating(game, //for demo mode, normally left out
//                                                                                            new DrawBattleMenu_SafariZone(game, new DoneAction())
//                                                                                        )
//                                                                                    )
//                                                                                : 
//                                                                                new PrintAngryEating(game, //for demo mode, normally left out
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
            PublicFunctions.insertToAS(game, Battle_Actions.get(game)); 
        }
    }
            

    public drawGhost(PkmnGen game, Vector2 position) {

        this.basePos = position;
        this.sineTimer = 0;
        this.dirFacing = "down";
        this.sprites = new HashMap<String, Sprite[]>();
        this.animIndex = 0;
        this.animFrame = 0;
        this.maxVel = 1.2f; //can scale to make ghost harder
        
        //need to store which pokemon this actually will be (i think)
        this.pokemon = new Pokemon("Sableye", 21);
        
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
}




//for spawning ghost, playing effects etc
class spawnGhost extends Action {
    

    public int layer = 114;
    public int getLayer(){return this.layer;}

    Sprite[] sprites;
    
    int part1;
    int part2;
    int part3;
    
    Vector2 position;
    
    @Override
    public void step(PkmnGen game) {
        
        if (part1 == 80) { //do once
            game.playerCanMove = false;
            //set player to face ghost
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
            
            //play alert music
            game.currMusic.pause();
            Music music = Gdx.audio.newMusic(Gdx.files.internal("night1_alert1.ogg"));
            music.setLooping(true);
            music.setVolume(.7f);
            game.currMusic = music;
            game.currMusic.play();
            
            //TODO - insert ! mark action
        }
        
        //play anim frames one by one
        if (part1 > 0) {
            
            if (part1 % 4 >= 2) {
                this.sprites[0].draw(game.batch);
            }
            part1--;
            return;
        }
        
        if (part2 == 40) {
            PublicFunctions.insertToAS(game, new PlaySound("ghost1", new DoneAction()));
        }
        if (part2 == 32) {
            PublicFunctions.insertToAS(game, new PlaySound("ghost1", new DoneAction()));
        }
        
        if (part2 > 0) {
            if (part2 % 8 >= 4) {
                this.sprites[1].draw(game.batch);
            }
            else {
                this.sprites[2].draw(game.batch);
            }
            part2--;
            return;
        }
        
        if (part3 > 0) {
            this.sprites[2].draw(game.batch);
            part3--;
            return;
        }
        
        //TODO - start frantic music (? best place? better in ghost draw?)
        game.currMusic.pause();
        Music music = Gdx.audio.newMusic(Gdx.files.internal("night1_chase1.ogg"));
        music.setLooping(true);
        music.setVolume(.7f);
        game.currMusic = music;
        game.map.currRoute.music = music; //TODO - how to switch to normal after defeating
        game.currMusic.play();
        
        
        game.playerCanMove = true;
        game.actionStack.remove(this);
        PublicFunctions.insertToAS(game, new drawGhost(game, this.position));
        
    }
    
    
    public spawnGhost(PkmnGen game, Vector2 position) {

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
        
}



class despawnGhost extends Action {
    

    public int layer = 114;
    public int getLayer(){return this.layer;}

    Sprite[] sprites;
    
    int part1;
    int part2;
    int part3;
    
    Vector2 position;
    Sprite sprite;
    
    @Override
    public void step(PkmnGen game) {
        
        if (part1 > 0) {
            
            if (part1 % 4 >=2) {
                this.sprite.draw(game.batch);
            }
            
            part1--;
            return;            
        }
        
        game.actionStack.remove(this);
        
    }
    
    public despawnGhost(Vector2 position) {

        this.part1 = 80;
        
        this.position = position;
        
        Texture ghostTexture1 = new Texture(Gdx.files.internal("ghost_spawn1.png"));
        this.sprite = new Sprite(ghostTexture1, 0, 0, 40, 40);
        this.sprite.setPosition(position.x-4, position.y-4);
    }
}




//for keeping track of day night
 //pops up cycle change notif, changes shader, etc
class cycleDayNight extends Action {
    

    public int layer = 114;
    public int getLayer(){return this.layer;}
    
    public String getCamera() {return "gui";};
    
    int[] currFrames;

    boolean fadeToDay;
    boolean fadeToNight;
    
    int animIndex;
    
    AnimationContainer<Color> animContainer;
    AnimationContainer<Color> fadeToDayAnim;
    
    Random rand;
    int countDownToGhost;
    int dayTimer;
    
    Sprite bgSprite;
    String text;
    
    int signCounter;
    int day, night; //number of days/nights that has passed
    
    @Override
    public void step(PkmnGen game) {
        
        if (game.playerCanMove == true) {
            dayTimer--;
        }
        
        if (dayTimer <= 0) {
            //TODO - time of day is part of map
             //
            if (game.map.timeOfDay == "Day") {
                this.fadeToNight = true;
                dayTimer = 10000; //debug
            }
            else if (game.map.timeOfDay == "Night") {
                this.fadeToDay = true;
                dayTimer = 10000; //1000 - debug
            }
        }
        

        if (fadeToDay == true) {
            
            game.batch.setColor(this.fadeToDayAnim.currentThing());
            
            animIndex++;

            if (animIndex >= this.fadeToDayAnim.currentFrame()) {
                this.fadeToDayAnim.index++;
                animIndex = 0;
            }
            
            
            if (this.fadeToDayAnim.index >= this.fadeToDayAnim.animateThese.size()) {
                fadeToDay = false;
                game.map.timeOfDay = "Day";
                this.fadeToDayAnim.index = 0;
                
                //TODO - fade day music
                game.currMusic.pause();
                //start night music
                Music music = Gdx.audio.newMusic(Gdx.files.internal("route1_1.ogg"));
                music.setLooping(true);
                music.setVolume(.7f);
                game.currMusic = music;
                game.map.currRoute.music = music; //TODO - how to switch to normal after defeating
                game.currMusic.play();
                
                //state which day it is
                day++;
                signCounter = 300;
                this.bgSprite.setPosition(0,24);
            }
        }
        
        
        if (fadeToNight == true) {
            
            game.batch.setColor(this.animContainer.currentThing());
            
            animIndex++;

            if (animIndex >= this.animContainer.currentFrame()) {
                this.animContainer.index++;
                animIndex = 0;
            }
            
            
            if (this.animContainer.index >= this.animContainer.animateThese.size()) {
                fadeToNight = false;
                game.map.timeOfDay = "Night";
                this.countDownToGhost = 150;//this.rand.nextInt(5000);
                this.animContainer.index = 0;
                
                //TODO - fade day music
                game.currMusic.pause();
                //start night music
                Music music = Gdx.audio.newMusic(Gdx.files.internal("night1.ogg"));
                music.setLooping(true);
                music.setVolume(.7f);
                game.currMusic = music;
                game.map.currRoute.music = music; //TODO - how to switch to normal after defeating
                game.currMusic.play();
                
                //state which night it is
                night++;
                signCounter = 150;
                this.bgSprite.setPosition(0,24);
            }
        }
        
        
        //check player can move so don't spawn in middle of battle or when looking at ghost
        if (game.map.timeOfDay == "Night" && game.playerCanMove == true) { 
            //chance to spawn ghost
            countDownToGhost--;
            if (game.player.currState != "Running") {
                countDownToGhost--;
            }
            
            if (countDownToGhost == 0) {
                Vector2 randPos = game.player.position.cpy().add(this.rand.nextInt(5)*16 - 48, this.rand.nextInt(5)*16 - 48);
                PublicFunctions.insertToAS(game, new spawnGhost(game, new Vector2(randPos)) );
                this.countDownToGhost = 1000;//this.rand.nextInt(5000);
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
            
            this.bgSprite.draw(game.floatingBatch);
            String temp="";
            if (game.map.timeOfDay == "Day") {
                    temp = String.valueOf(this.day);
            }
            else {
                    temp = String.valueOf(this.night);
            }
            game.font.draw(game.floatingBatch, game.map.timeOfDay+": "+temp, 60, this.bgSprite.getY()+134); //Gdx.graphics.getHeight()-
        }
        
    }
    
    public cycleDayNight(PkmnGen game) {
        
        this.day = 1;
        this.night = 0;
        
        this.animContainer = new AnimationContainer<Color>();
        animContainer.add(new Color(0.8f, 0.8f, 0.8f, 1.0f), 80);
        animContainer.add(new Color(0.5f, 0.5f, 0.6f, 1.0f), 80);
        animContainer.add(new Color(0.2f, 0.2f, 0.4f, 1.0f), 80);
        animContainer.add(new Color(0.08f, 0.08f, 0.3f, 1.0f), 80);
        animContainer.add(new Color(0.01f, 0.01f, 0.2f, 1.0f), 80);

        this.fadeToDayAnim = new AnimationContainer<Color>();
        fadeToDayAnim.add(new Color(0.01f, 0.01f, 0.2f, 1.0f), 80);
        fadeToDayAnim.add(new Color(0.08f, 0.08f, 0.3f, 1.0f), 80);
        fadeToDayAnim.add(new Color(0.2f, 0.2f, 0.4f, 1.0f), 80);
        fadeToDayAnim.add(new Color(0.8f, 0.8f, 0.8f, 1.0f), 80);
        fadeToDayAnim.add(Color.WHITE, 80);

        this.fadeToDay = false;
        this.fadeToNight = false;
        this.animIndex = 0;
        
        this.rand = new Random();
        this.dayTimer = 10000; //100; //- debug // 10000; //
        

        Texture text = new Texture(Gdx.files.internal("text2.png"));
        this.bgSprite = new Sprite(text, 0, 0, 160, 144);
        this.bgSprite.setPosition(0,24);

        this.text = game.map.timeOfDay+": "; //String.valueOf(this.numMain)
    }
        
}



// Robot package not available on android
// would need to restructure the PlayerStanding/Moving classes
// maybe they could also check some global booleans?
 // hmm, that might work
class DrawAndroidControls extends Action {
    
    public int layer = 101;  // hopefully above most things?
    public int getLayer(){return this.layer;}
    
    public String getCamera() {return "gui";};
    
    Sprite leftArrowSprite;
    Sprite rightArrowSprite;
//    Robot robot;

    @Override
    public void step(PkmnGen game) {

        this.leftArrowSprite.draw(game.floatingBatch);
        

        if (Gdx.input.justTouched()) {

            Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            game.cam.unproject(touchPos);
            
            if (this.leftArrowSprite.getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
                int keyCode = KeyEvent.VK_LEFT; // left arrow
//                this.robot.keyPress(keyCode);
            }
        }
        
        
    }

    public DrawAndroidControls() {
        
        Texture text = new Texture(Gdx.files.internal("battle/player_back1.png"));
        this.leftArrowSprite = new Sprite(text, 0, 0, 28, 28);
        this.leftArrowSprite.setPosition(0,0);
        this.rightArrowSprite = new Sprite(text, 0, 0, 28, 28);
        
//        try {
//            this.robot = new Robot();
//        } catch (AWTException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }
}    



//contains thing to cycle, number of frames for thing
//add more in future, like sounds
class AnimationContainer<E> {
    
    ArrayList<E> animateThese;
    ArrayList<Integer> numFrames;
    
    int index;
    
    public void add(E thing, int numFrames) {
        animateThese.add(thing);;
        this.numFrames.add(numFrames);
    
    }

    public E currentThing() {
        return this.animateThese.get(this.index);
    }
    
    public int currentFrame() {
        return this.numFrames.get(this.index);
    }
    
    public AnimationContainer() {
        this.animateThese = new ArrayList<E>();
        this.numFrames = new ArrayList<Integer>();
        this.index = 0;
    }
}



/*
 * 
 *      Tried to used deltatime for movement, proved bad because pixels are so visible

        //draw the sprite corresponding to player direction
        //game.player.standingSprites.get(game.player.dirFacing).draw(game.batch);


        //game.batch.draw(game.player.standingSprites.get(game.player.dirFacing), game.player.position.x, game.player.position.y);
        
        //game.player.currSprite = new Sprite(game.player.standingSprites.get(game.player.dirFacing));
        
        

        //game.player.currSprite.translate(0,-this.speed*Gdx.graphics.getDeltaTime());
        //snap camera
        //game.cam.translate(0,-this.speed*Gdx.graphics.getDeltaTime());
        
        //game.player.currSprite.draw(game.batch);

        //System.out.println("Timer: " + String.valueOf(Math.abs(this.initialPos.y)));
         */

        /* old sprite anim in playerLedge that looked pretty good
        //sprite anim
        if (this.timer1 < 8) {
            game.player.currSprite = game.player.standingSprites.get(game.player.dirFacing);
        }
        else if (this.timer1 < 16) {
            game.player.currSprite = game.player.movingSprites.get(game.player.dirFacing);
        }
        else if (this.timer1 < 24) {
            game.player.currSprite = game.player.standingSprites.get(game.player.dirFacing);
        }
        else if (this.timer1 < 32) {
            game.player.currSprite = game.player.altMovingSprites.get(game.player.dirFacing);
        }
        */


        //old playerMoving code to see where to move next
        /* remove this if chaining the standing action works
        this.initialPos = new Vector2(game.player.position);
        
        if(Gdx.input.isKeyPressed(Input.Keys.UP)) {
            game.player.dirFacing = "up";
            this.alternate = !this.alternate; //flip sprite for up/down movement
            this.targetPos = new Vector2(game.player.position.x, game.player.position.y+16);
        }
        else if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            game.player.dirFacing = "down";
            this.alternate = !this.alternate; //flip sprite for up/down movement
            this.targetPos = new Vector2(game.player.position.x, game.player.position.y-16);
        }
        else if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            game.player.dirFacing = "left";
            this.alternate = false;
            this.targetPos = new Vector2(game.player.position.x-16, game.player.position.y);
        }
        else if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            game.player.dirFacing = "right";
            this.alternate = false;
            this.targetPos = new Vector2(game.player.position.x+16, game.player.position.y);
        }
        else {
            PublicFunctions.insertToAS(game, new playerStanding(game));
            game.actionStack.remove(this);
        }
        
        //if the targetPos is a solid tile, instead go to 'bump' action
        Tile temp = game.map.tiles.get(this.targetPos);
        if (temp != null) {
            if (temp.attrs.get("solid") == true) {
                PublicFunctions.insertToAS(game, new playerBump(game));
                game.actionStack.remove(this);
            }
        }
        */



        /* detect encounters code in playerStanding
        //when moving to tile, no chance of encounter unless continuing to move
        boolean checkWildEncounter(PkmnGen game) {
            
            Tile currTile = game.map.tiles.get(game.player.position);
            
            if (currTile != null) {
                
                //if currently on grass
                if (currTile.attrs.get("grass") == true) {
                    //chance wild encounter
                    int randomNum = this.rand.nextInt(100) + 1; //rate determine by player? //1 - 100
                    if (randomNum <= 50) { //encounterRate
                        //disable player movement 
                        game.actionStack.remove(this);
                        
                        //select new pokemon to encounter, put it in battle struct
                        int index = rand.nextInt(game.map.currRoute.pokemon.size());
                        //game.battle.currPokemon = game.map.currRoute.pokemon.get(index);
                        
                        //start battle anim
                        //PublicFunctions.insertToAS(game, new battleIntro(game));
                        
                        System.out.println("Wild encounter.");
                        
                        return true;
                    }
                }
                
            }
            
            return false;
        }
        */

