package com.pkmngen.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;


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
	
	int adrenaline;  //was 'streak'
	//demo mechanic - catching in a row without oppPokemon fleeing builds streak
	 //displayed as 'adrenaline'
	 //based off of catch rate
	
	//this would be if doing android
	 //action would set these
	//Map<String, Integer> buttonPressed = new HashMap<String, Integer>();
	
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
				game.actionStack.remove(this);
				//plan to insert a series here instead
				 //
				PublicFunctions.insertToAS(game, new SplitAction(
													new BattleIntro(
														new BattleIntro_anim1(
															new SplitAction(
																new DrawBattle(game),
																new BattleAnim_positionPlayers(game, 
																	new PlaySound(game.battle.oppPokemon.name, 
																		new DisplayText(game, "Wild "+game.battle.oppPokemon.name.toUpperCase()+" appeared!", null, null, 
																			new WaitFrames(game, 39,
																				//demo code - wildly confusing, but i don't want to write another if statement
																					game.player.adrenaline > 0 ? new DisplayText(game, ""+game.player.name+" has ADRENALINE "+Integer.toString(game.player.adrenaline)+"!", null, null,
																						new PrintAngryEating(game, //for demo mode, normally left out
																								new DrawBattleMenu_SafariZone(game, new DoneAction())
																							)
																						)
																					: 
																					new PrintAngryEating(game, //for demo mode, normally left out
																							new DrawBattleMenu_SafariZone(game, new DoneAction())	
																				)
																				//
																			)
																		)
																	)
																)
															)
														)
													),
													new DoneAction()
												)
											);
				
				game.currMusic.pause();
				game.currMusic = game.battle.music;
				game.currMusic.play();
				//game.battle.music.play(); //would rather have an action that does this?
				return;
			}
			this.checkWildEncounter = false;
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
		if(Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
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
		
		Tile currTile = game.map.tiles.get(game.player.position);
		
		if (currTile != null) {
			
			//if currently on grass
			if (currTile.attrs.get("grass") == true) {
				//chance wild encounter
				int randomNum = game.map.rand.nextInt(100) + 1; //rate determine by player? //1 - 100
				if (randomNum < 20) { //encounterRate //was <= 50
					//disable player movement 
					game.actionStack.remove(this);
					
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
			game.cam.update(); 									//this line fixes jittering bug
			game.batch.setProjectionMatrix(game.cam.combined);	//same
			
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
		
//		if (this.timer1 % 2 == 0 && this.timer1 < 32) {
//			game.cam.position.y -=2;
//			game.player.position.y -=2;
//		}
		
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
		
		this.spritePart = new Sprite(game.player.currSprite);

		//this.spritePart.setRegion(0,0,16,8);
		//this.spritePart.setRegionHeight(4);
		//this.spritePart.setSize(this.spritePart.getWidth(), 4);
		this.spritePart.setRegionY(0);
		this.spritePart.setRegionHeight(8);
		
		game.batch.draw(this.spritePart, game.player.position.x, game.player.position.y+12);
		
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
//		this.pokeball.setPosition(0*16*3+12, 144*3-12*3);
//		this.pokeball.draw(game.floatingBatch);
		
	}
			

	public DrawPokemonCaught(PkmnGen game) {

		Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball1.png"));
		this.pokeball = new Sprite(text, 0, 0, 12, 12);
		this.pokeball.setScale(2);
	}
}



//demo of drawing sprites for ea pokemon caught
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

