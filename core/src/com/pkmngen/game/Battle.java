package com.pkmngen.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class Battle {

	//opposing pokemon
	Pokemon oppPokemon;
	
	//your pokemon //probly use this
	//Pokemon yourPokemon;
	
	//action that is drawing the battle
	 //this reference is used to stop drawing battle once it's complete
	DrawBattle drawAction;
	
	Music music;
	
	public Battle() {

		this.music = Gdx.audio.newMusic(Gdx.files.internal("battle/battle-vs-wild-pokemon.ogg"));
		this.music.setLooping(true);
		this.music.setVolume(.3f);
	}
	
}


//TODO - remove all 'post scaling change' commented lines.

//TODO - bug where a caught pokemon will still be in the wild

class BattleIntro extends Action {
	
	ArrayList<Sprite> frames;
	Sprite frame;
	
	Action nextAction;

	public int layer = 139;
	public int getLayer(){return this.layer;}

	public String getCamera() {return "gui";};
	
	@Override
	public void step(PkmnGen game) {
		
		//if done with anim, do nextAction
		if (frames.isEmpty()) {
			PublicFunctions.insertToAS(game, this.nextAction);
			game.actionStack.remove(this);
			return;
		}
		
		//get next frame
		this.frame = frames.get(0);
		
		if (this.frame != null) {
			this.frame.setScale(3); //scale doesn't work in batch.draw
			this.frame.setPosition(0,0);
			this.frame.draw(game.floatingBatch);
			//game.batch.draw(this.frame, 0, -20);
		}
		
		frames.remove(0);
	}
	
	public BattleIntro(Action nextAction) {
		
		this.nextAction = nextAction;
		
		this.frames = new ArrayList<Sprite>();
		
		//animation to play
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
		//this.frames.addAll(cpyFrames);
		frames.add(null);
		frames.add(null);
		
	}
	
}


class BattleIntroMusic extends Action {
	
	
	Action nextAction;

	public int layer = 139;
	public int getLayer(){return this.layer;}

	public String getCamera() {return "gui";};
	
	@Override
	public void step(PkmnGen game) {
		

		game.currMusic.pause();
		game.currMusic = game.battle.music;
		game.currMusic.play();
		
		PublicFunctions.insertToAS(game, this.nextAction);
		game.actionStack.remove(this);
		
	}
	
	public BattleIntroMusic(Action nextAction) {
		this.nextAction = nextAction;
	}
}
		


//TODO - i think this needs to call .step() for next b/c there is a missing frame before battle
class BattleIntro_anim1 extends Action {
	
	ArrayList<Sprite> frames;
	Sprite frame;
	
	Action nextAction;

	public int layer = 139;
	public int getLayer(){return this.layer;}

	public String getCamera() {return "gui";};
	
	@Override
	public void step(PkmnGen game) {
		
		//if done with anim, do nextAction
		if (frames.isEmpty()) {
			PublicFunctions.insertToAS(game, this.nextAction);
			game.actionStack.remove(this);
			return;
		}
		
		//get next frame
		this.frame = frames.get(0);
		
		if (this.frame != null) {
			//gui version
				//this.frame.setScale(3); //scale doesn't work in batch.draw //used these when scaling mattered
				//this.frame.setPosition(16*10,16*9);
			this.frame.draw(game.floatingBatch);
			//map version
			//game.batch.draw(this.frame, 16, -16);
		}
		
		frames.remove(0);
	}
	
	public BattleIntro_anim1(Action nextAction) {
		
		this.nextAction = nextAction;
		
		this.frames = new ArrayList<Sprite>();
		
		Texture text1 = new Texture(Gdx.files.internal("battle/battle_intro_anim1_sheet1.png"));
		
		//animation to play
		for (int i = 0; i < 28; i++) {
			frames.add(new Sprite(text1, i*160, 0, 160, 144));
		}

		for (int i = 0; i < 42; i++) {
			frames.add(new Sprite(text1, 27*160, 0, 160, 144));
		}
		
	}
	
}


//scroll both players into view
class BattleAnim_positionPlayers extends Action {
	
	ArrayList<Vector2> moves_relative;
	Vector2 move;
	
	Action nextAction;

	public int layer = 140;
	public int getLayer(){return this.layer;}

	public String getCamera() {return "gui";};
	
	@Override
	public void step(PkmnGen game) {
		
		
		//if done with anim, do nextAction
		if (moves_relative.isEmpty()) {
			PublicFunctions.insertToAS(game, this.nextAction);
			game.actionStack.remove(this);
			return;
		}
		
		//get next frame
		this.move = moves_relative.get(0);

		float xPos = game.player.battleSprite.getX() - move.x;//*3;
		game.player.battleSprite.setX(xPos);

		xPos = game.battle.oppPokemon.sprite.getX() + move.x;//*3;
		game.battle.oppPokemon.sprite.setX(xPos);
				
		moves_relative.remove(0);
	}
	
	public BattleAnim_positionPlayers(PkmnGen game, Action nextAction) {
		
		this.nextAction = nextAction;
		
		this.moves_relative = new ArrayList<Vector2>();
		
		//animation to play
		for (int i = 0; i < 72; i++) {
			moves_relative.add(new Vector2(2,0));
		}
		
		game.player.battleSprite.setPosition(175+1-8-2,71+1-10);//(3*175+1,3*71+1);
		//game.player.battleSprite.setScale(6);
		game.player.battleSprite.setScale(2);

		game.battle.oppPokemon.sprite.setPosition(-30-4-1-14,106+2-5-15);//(3*-30,3*106+2); //TODO - x and y pos not correct...
		//note - i think my previous x was off by 1/3 a pixel, b/c val wasn't divisible by 3.
		 //I am sticking with new x pos, which is really close
		//game.battle.oppPokemon.sprite.setScale(3);
		
		
	}
	
}


//scroll both players into view
class MovePlayerOffScreen extends Action {
	
	ArrayList<Vector2> positions;
	ArrayList<Integer> repeats;
	Vector2 move;
	
	Vector2 position;
	Sprite sprite;
	
	Action nextAction;

	public int layer = 140;
	public int getLayer(){return this.layer;}

	public String getCamera() {return "gui";};
	
	@Override
	public void step(PkmnGen game) {

		
		//if done with anim, do nextAction
		if (positions.isEmpty() || repeats.isEmpty()) {
			PublicFunctions.insertToAS(game, this.nextAction);
			game.actionStack.remove(this);
			return;
		}

		

		if (this.repeats.get(0) > 1) {
			this.repeats.set(0, this.repeats.get(0) - 1);
		}
		else {
			//since position is relative, only update once each time period
			this.position = new Vector2(game.player.battleSprite.getX(), game.player.battleSprite.getY());
			this.position.add(positions.get(0));
			this.sprite.setPosition(position.x, position.y);
			positions.remove(0);
			repeats.remove(0);
		}
		
	}
	
	public MovePlayerOffScreen(PkmnGen game, Action nextAction) {
		
		this.nextAction = nextAction;
		
		this.position = null;
		
		this.positions = new ArrayList<Vector2>();
		
		//animation to play
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
	
}


//i think this is outdated
//draw menu buttons (fight, run, etc)
class DrawBattleMenu1 extends Action {

	Sprite arrow;

	public int layer = 129;
	public int getLayer(){return this.layer;}

	public String getCamera() {return "gui";};

	
	Map<String, Vector2> getCoords = new HashMap<String, Vector2>();
	String curr;
	Vector2 newPos;
	
	Action nextAction;
	
	@Override
	public void step(PkmnGen game) {

		
		//check user input
		 //'tl' = top left, etc. 
		 //modify position by modifying curr to tl, tr, bl or br
		if(Gdx.input.isKeyPressed(Input.Keys.UP)) {
			if (curr.equals("bl") || curr.equals("br")) {
				curr = "t"+String.valueOf(curr.charAt(1));
				newPos = getCoords.get(curr);
			}
			
		}
		else if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
			if (curr.equals("tl") || curr.equals("tr")) {
				curr = "b"+String.valueOf(curr.charAt(1));
				newPos = getCoords.get(curr);
			}
		}
		else if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			if (curr.equals("tr") || curr.equals("br")) {
				curr = String.valueOf(curr.charAt(0))+"l";
				newPos = getCoords.get(curr);
			}
		}
		else if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			if (curr.equals("tl") || curr.equals("bl")) {
				curr = String.valueOf(curr.charAt(0))+"r";
				newPos = getCoords.get(curr);
			}
		}
		
		//if button press, do something
		if(Gdx.input.isKeyPressed(Input.Keys.Z)) {
			
			//user selected 'fight'
			 //for now this is 'rock', I am going to draw the safari menu later
			if (curr.equals("tl")) {
				//throw a rock, then resume battle menu
				PublicFunctions.insertToAS(game, new ThrowRock(game, this));
				game.actionStack.remove(this);
			}
			
			//user selected 'run'
			if (curr.equals("br")) {
				 //also need a 'stop playing music' thing here
				 //also need 'stop drawing battle' here
				PublicFunctions.insertToAS(game, new BattleFadeOut(game,
													new WaitFrames(game, 18, 
														new DisplayText(game, "Got away safely!", null, null,
															new DoneAction()//new playerStanding(game)
														)
													)
												)
											);
				PublicFunctions.insertToAS(game, new BattleFadeOutMusic(game, new DoneAction()));
				PublicFunctions.insertToAS(game, new PlaySound("click1", new PlaySound("run1", new DoneAction())));
				game.actionStack.remove(this);
				game.actionStack.remove(game.battle.drawAction); //stop drawing the battle
				game.battle.drawAction = null;

			}
		}

		//System.out.println("curr: "+curr);

		//draw arrow
		this.arrow.setPosition(newPos.x, newPos.y);
		this.arrow.draw(game.floatingBatch);
	}
	
	public DrawBattleMenu1(PkmnGen game, Action nextAction) {

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
}





//draw menu buttons (fight, run, etc)
class DrawBattleMenu_SafariZone extends Action {

	Sprite arrow;
	Sprite textBox;
	
	public int layer = 129;
	public int getLayer(){return this.layer;}

	public String getCamera() {return "gui";};

	
	Map<String, Vector2> getCoords = new HashMap<String, Vector2>();
	String curr;
	Vector2 newPos;
	
	Action nextAction;
	
	@Override
	public void step(PkmnGen game) {

		
		//check user input
		 //'tl' = top left, etc. 
		 //modify position by modifying curr to tl, tr, bl or br
		if(Gdx.input.isKeyPressed(Input.Keys.UP)) {
			if (curr.equals("bl") || curr.equals("br")) {
				curr = "t"+String.valueOf(curr.charAt(1));
				newPos = getCoords.get(curr);
			}
			
		}
		else if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
			if (curr.equals("tl") || curr.equals("tr")) {
				curr = "b"+String.valueOf(curr.charAt(1));
				newPos = getCoords.get(curr);
			}
		}
		else if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			if (curr.equals("tr") || curr.equals("br")) {
				curr = String.valueOf(curr.charAt(0))+"l";
				newPos = getCoords.get(curr);
			}
		}
		else if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			if (curr.equals("tl") || curr.equals("bl")) {
				curr = String.valueOf(curr.charAt(0))+"r";
				newPos = getCoords.get(curr);
			}
		}
		
		//if button press, do something
		if(Gdx.input.isKeyJustPressed(Input.Keys.Z)) { //using isKeyJustPressed rather than isKeyPressed
			
			//user selected 'pokeball'
			if (curr.equals("tl")) {
				
				//decide if caught or not, use corresponding action
				 //this is a trigger action for displayText_triggered
				//Action catchAction = new catchPokemon_miss(game, this); 
				Action catchAction = calcIfCaught(game);
				
				//display text, throw animation, catch or not
				String textString = game.player.name+" used SAFARI BALL!";
				//demo code
				if (game.player.adrenaline < 5) {
					PublicFunctions.insertToAS(game, new DisplayText(game, textString, null, catchAction,
							new ThrowPokeball(game, catchAction)
						)
					);
				}
				else if (game.player.adrenaline < 15){
					PublicFunctions.insertToAS(game, new DisplayText(game, textString, null, catchAction,
							new ThrowFastPokeball(game, catchAction)
						)
					);
				}
				else {
					PublicFunctions.insertToAS(game, new DisplayText(game, textString, null, catchAction,
							new ThrowHyperPokeball(game, catchAction)
						)
					);
				}
				
				
				//throw a pokeball, then resume battle menu (maybe)
				//PublicFunctions.insertToAS(game, new ThrowPokeball(game, this));
				game.actionStack.remove(this);
			}
			
			//user selected 'throw rock'
			else if (curr.equals("bl")) {
				//throw a rock, then resume battle menu
				
				
				Action throwRockAction = new ThrowRock(game, new PrintAngryEating(game, new ChanceToRun(game, this) ) );
				String textString = game.player.name+" threw a ROCK.";
				PublicFunctions.insertToAS(game, new DisplayText(game, textString, null, throwRockAction,
						throwRockAction
					)
				);
				
				
				//PublicFunctions.insertToAS(game, new ThrowRock(game, this));
				game.actionStack.remove(this);
			}
			
			//user selected 'throw bait'
			else if (curr.equals("tr")) {
				
				Action throwBaitAction = new ThrowBait(game, new PrintAngryEating(game, new ChanceToRun(game, this) ) );
				String textString = game.player.name+" threw some BAIT.";
				PublicFunctions.insertToAS(game, new DisplayText(game, textString, null, throwBaitAction,
						throwBaitAction
					)
				);
				
				game.actionStack.remove(this);
			}
			
			//user selected 'run'
			else if (curr.equals("br")) {
				 //also need a 'stop playing music' thing here
				 //also need 'stop drawing battle' here
				PublicFunctions.insertToAS(game, 
												new WaitFrames(game, 18, 
													new DisplayText(game, "Got away safely!", null, null,
														new SplitAction(
															new BattleFadeOut(game,
																new DoneAction()), //new playerStanding(game)),
															new BattleFadeOutMusic(game, new DoneAction())
														)
													)
												)
											);
				PublicFunctions.insertToAS(game, new PlaySound("click1", new PlaySound("run1", new DoneAction())));
				game.actionStack.remove(this);
			}
		}

		//System.out.println("curr: " + curr);

		//draw text box
		this.textBox.draw(game.floatingBatch);
		
		//draw arrow
		this.arrow.setPosition(newPos.x, newPos.y);
		this.arrow.draw(game.floatingBatch);
	}
	
	public DrawBattleMenu_SafariZone(PkmnGen game, Action nextAction) {

		this.nextAction = nextAction;
		
		Texture text = new Texture(Gdx.files.internal("battle/arrow_right1.png"));
		this.arrow = new Sprite(text, 0, 0, 5, 7);
		//this.arrow.setScale(3); //post scaling change
		
		//text box bg
		text = new Texture(Gdx.files.internal("battle/battle_text_safarizone.png"));
		this.textBox = new Sprite(text, 0,0, 16*10, 16*9);
		//this.textBox.setScale(3); //post scaling change
		//this.textBox.setPosition(16*10,16*9); //post scaling change
		
		this.getCoords.put("tr", new Vector2(105, 24));
		this.getCoords.put("tl", new Vector2(9, 24));
		this.getCoords.put("br", new Vector2(105, 8));
		this.getCoords.put("bl", new Vector2(9, 8));
		
		//this.newPos =  new Vector2(32, 79); //post scaling change
		this.newPos =  new Vector2(9, 24);
		this.arrow.setPosition(newPos.x, newPos.y);
		this.curr = "tl";
	}
	
	
	Action calcIfCaught(PkmnGen game) {
		
		//using http://bulbapedia.bulbagarden.net/wiki/Catch_rate#Capture_method_.28Generation_I.29
		 //also use http://www.dragonflycave.com/safarizone.aspx
		//not sure where 'ball used' will be stored. probly some inventory location, like currItem (in inventory)
		
		int maxRand = 150; //different per-ball
		int randomNum = game.map.rand.nextInt(maxRand+1); //+1 to include upper bound
		int statusValue = 0; //different depending on oppPokemon's status
		boolean breaksFree = false;


		int ball = 15; //8 if great ball
		//demo code
		int adrenaline = game.player.adrenaline;
		if (adrenaline > 25) {
			adrenaline = 25;
		}
		//ball = ball - adrenaline; 
		int modFactor = 100;//128; - want 5 adr to catch all easy, but not medium or hard.
		int f = (int)Math.floor((game.battle.oppPokemon.currentStats.get("catchRate") * 255 * 4) / (modFactor*ball)); //modify 128 to make game harder
		//
		
		//int f = (int)Math.floor((game.battle.oppPokemon.maxStats.get("hp") * 255 * 4) / (game.battle.oppPokemon.currentStats.get("hp") * ball));
		
		//left out calculation here based on status values
		 //demo - leave out status value
		//notes - adr seems to take effec too fast. also, pkmn in general are too hard to catch 
		 //at beginning. shift factor down, and make adr*10
		if (randomNum - statusValue > game.battle.oppPokemon.currentStats.get("catchRate") && false) {
			breaksFree = true;
			System.out.println("(randomNum - statusValue / catchRate): ("+String.valueOf(randomNum - statusValue)+" / "+String.valueOf(game.battle.oppPokemon.currentStats.get("catchRate"))+")");
		}
		else {
			int randomNum_M = game.map.rand.nextInt(255+1);
			
			
			//randomNum_M = randomNum_M - adrenaline*20;


			if (f+(adrenaline*10) >= randomNum_M) { //demo code
				breaksFree = false;
			}
			else {
				breaksFree = true;
			}
			System.out.println("(randomNum_M / f / adr): ("+String.valueOf(randomNum_M)+" / "+String.valueOf(f)+" / +"+String.valueOf(adrenaline*10)+")");
		}
		

		//simplify and put above
		if (breaksFree == false) { //ie was caught 
			return new catchPokemon_wigglesThenCatch(game, this);
		}

		//else, ie breaksFree = true
		
		int d = game.battle.oppPokemon.currentStats.get("catchRate") * 100 / maxRand;
				//, where the value of Ball is 255 for the Poké Ball, 200 for the Great Ball, or 150 for other balls
		if (d >= 256) {
			//shake 3 times before breaking free
			return new catchPokemon_wiggles3Times(game, new PrintAngryEating(game, new ChanceToRun(game, this) ) );
		}
				
		int s = 0;//status thing again
		int x = d * f / 255 + s;
		
		if (x < 10) {
			//ball misses
			return new catchPokemon_miss(game, new PrintAngryEating(game, new ChanceToRun(game, this) ) );
		}
		else if (x < 30) {
			//ball shakes once
			return new catchPokemon_wiggles1Time(game, new PrintAngryEating(game, new ChanceToRun(game, this) ) );
		}
		else if (x < 70) {
			//ball shakes twice
			return new catchPokemon_wiggles2Times(game, new PrintAngryEating(game, new ChanceToRun(game, this) ) );
		}
		//ball shakes three times before pkmn gets free
		
		//System.out.println("x: "+String.valueOf(x));
		//System.out.println("Shake three times: "+String.valueOf(x));
		
		return new catchPokemon_wiggles3Times(game, new PrintAngryEating(game, new ChanceToRun(game, this) ) );
	}
}




//draw menu buttons (fight, run, etc)
class DrawBattleMenu_Normal extends Action {

	Sprite arrow;
	Sprite textBox;
	
	public int layer = 129;
	public int getLayer(){return this.layer;}

	public String getCamera() {return "gui";};

	
	Map<String, Vector2> getCoords = new HashMap<String, Vector2>();
	String curr;
	Vector2 newPos;
	Sprite helperSprite;
	
	Action nextAction;
	
	@Override
	public void step(PkmnGen game) {

		
		//check user input
		 //'tl' = top left, etc. 
		 //modify position by modifying curr to tl, tr, bl or br
		if(Gdx.input.isKeyPressed(Input.Keys.UP)) {
			if (curr.equals("bl") || curr.equals("br")) {
				curr = "t"+String.valueOf(curr.charAt(1));
				newPos = getCoords.get(curr);
			}
			
		}
		else if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
			if (curr.equals("tl") || curr.equals("tr")) {
				curr = "b"+String.valueOf(curr.charAt(1));
				newPos = getCoords.get(curr);
			}
		}
		else if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			if (curr.equals("tr") || curr.equals("br")) {
				curr = String.valueOf(curr.charAt(0))+"l";
				newPos = getCoords.get(curr);
			}
		}
		else if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			if (curr.equals("tl") || curr.equals("bl")) {
				curr = String.valueOf(curr.charAt(0))+"r";
				newPos = getCoords.get(curr);
			}
		}
		
		//if button press, do something
		if(Gdx.input.isKeyJustPressed(Input.Keys.Z)) { //using isKeyJustPressed rather than isKeyPressed
			
			//user selected 'fight'
			if (curr.equals("tl")) {

				//play select sound
				PublicFunctions.insertToAS(game, new PlaySound("click1", new DoneAction()));
				
				//remove this action, new menu that selects between pokemon attacks
				PublicFunctions.insertToAS(game, new WaitFrames(game, 4, new DrawAttacksMenu(game, new WaitFrames(game, 4, this))));
				
				//attacks stored in String[4] in pkmn
								
				game.actionStack.remove(this);
			}
			
			//user selected 'throw rock'
			else if (curr.equals("bl")) {
				//throw a rock, then resume battle menu
				
				
				Action throwRockAction = new ThrowRock(game, new PrintAngryEating(game, new ChanceToRun(game, this) ) );
				String textString = game.player.name+" threw a ROCK.";
				PublicFunctions.insertToAS(game, new DisplayText(game, textString, null, throwRockAction,
						throwRockAction
					)
				);
				
				
				//PublicFunctions.insertToAS(game, new ThrowRock(game, this));
				game.actionStack.remove(this);
			}
			
			//user selected 'throw bait'
			else if (curr.equals("tr")) {
				
				Action throwBaitAction = new ThrowBait(game, new PrintAngryEating(game, new ChanceToRun(game, this) ) );
				String textString = game.player.name+" threw some BAIT.";
				PublicFunctions.insertToAS(game, new DisplayText(game, textString, null, throwBaitAction,
						throwBaitAction
					)
				);
				
				game.actionStack.remove(this);
			}
			
			//user selected 'run'
			else if (curr.equals("br")) {
				 //also need a 'stop playing music' thing here
				 //also need 'stop drawing battle' here
				PublicFunctions.insertToAS(game, 
												new WaitFrames(game, 18, 
													new DisplayText(game, "Got away safely!", null, null,
														new SplitAction(
															new BattleFadeOut(game,
																new DoneAction()), //new playerStanding(game)),
															new BattleFadeOutMusic(game, new DoneAction())
														)
													)
												)
											);
				PublicFunctions.insertToAS(game, new PlaySound("click1", new PlaySound("run1", new DoneAction())));
				game.actionStack.remove(this);
			}
		}

		//System.out.println("curr: " + curr);

		//draw text box
		this.textBox.draw(game.floatingBatch);

		//debug
//		helperSprite.draw(game.floatingBatch);
		
		//draw arrow
		this.arrow.setPosition(newPos.x, newPos.y);
		this.arrow.draw(game.floatingBatch);
	}
	
	public DrawBattleMenu_Normal(PkmnGen game, Action nextAction) {

		this.nextAction = nextAction;
		
		Texture text = new Texture(Gdx.files.internal("battle/arrow_right1.png"));
		this.arrow = new Sprite(text, 0, 0, 5, 7);
		//this.arrow.setScale(3); //post scaling change
		
		//text box bg
		text = new Texture(Gdx.files.internal("battle/battle_menu1.png"));
		this.textBox = new Sprite(text, 0,0, 16*10, 16*9);
		
		this.getCoords.put("tr", new Vector2(121, 24));
		this.getCoords.put("tl", new Vector2(73, 24));
		this.getCoords.put("br", new Vector2(121, 8));
		this.getCoords.put("bl", new Vector2(73, 8));
		
		//this.newPos =  new Vector2(32, 79); //post scaling change
		this.newPos =  new Vector2(73, 24);
		this.arrow.setPosition(newPos.x, newPos.y);
		this.curr = "tl";
		
		
		//helper sprite
		text = new Texture(Gdx.files.internal("pokemon_throw_out_anim/helper7.png"));
		this.helperSprite = new Sprite(text, 0,0, 16*10, 16*9);
	}
	
}




//draw menu buttons (fight, run, etc)
class DrawAttacksMenu extends Action { 

	Sprite arrow;
	Sprite textBox;
	
	public int layer = 108;
	public int getLayer(){return this.layer;}

	public String getCamera() {return "gui";};

	
	Map<Integer, Vector2> getCoords;
	int curr;
	Vector2 newPos;
	Sprite helperSprite;
	ArrayList<ArrayList<Sprite>> spritesToDraw;
	int cursorDelay; //this is just extra detail. cursor has 2 frame delay before showing in R/B
	
	Action nextAction;
	
	@Override
	public void step(PkmnGen game) {

		
		
		//check user input
		 //'tl' = top left, etc. 
		 //modify position by modifying curr to tl, tr, bl or br
		if(Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
			if (curr != 0) {
				curr -= 1;
				newPos = getCoords.get(curr);
			}
			
		}
		else if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
			if (curr != 3) {
				curr += 1;
				newPos = getCoords.get(curr);
			}
		}
		
		//if button press, do something
		if(Gdx.input.isKeyJustPressed(Input.Keys.Z)) { //using isKeyJustPressed rather than isKeyPressed
			
			//play select sound
			PublicFunctions.insertToAS(game, new PlaySound("click1", new DoneAction()));
			
			//if that selection isn't null, get that attack via
			 //Battle_Actions.getAttackAction(String attackName, Action nextAction);
			Action trigger = new WaitFrames(game, 13, 
									new WaitFrames(game, 3, this.nextAction)
								);
			Action attack = Battle_Actions.getAttackAction(game, game.player.currPokemon.attacks[curr], 
																	new DepleteEnemyHealth(game, trigger));
			PublicFunctions.insertToAS(game, new DisplayText(game, game.player.currPokemon.name.toUpperCase()+" used "+game.player.currPokemon.attacks[curr].toUpperCase()+"!",
					 											null, trigger, attack
								 							));
			game.actionStack.remove(this);
			return;
		}
		//player presses b, ie wants to go back
		else if(Gdx.input.isKeyJustPressed(Input.Keys.X)) { 
			game.actionStack.remove(this);
			PublicFunctions.insertToAS(game, this.nextAction);
		}

		//System.out.println("curr: " + curr);

		//draw text box
		this.textBox.draw(game.floatingBatch);

		//debug
//		helperSprite.draw(game.floatingBatch);
		
		//draw the attack strings 
		int j = 0;
		for (ArrayList<Sprite> word : this.spritesToDraw) {
			for (Sprite sprite : word) {
				//convert string to text 
				game.floatingBatch.draw(sprite, sprite.getX() + 40, sprite.getY() - j*8 + 8);
			}
			j+=1;
		}
		
		
		//draw arrow
		if (this.cursorDelay >= 2) {
			this.arrow.setPosition(newPos.x, newPos.y);
			this.arrow.draw(game.floatingBatch);
		}
		else {
			this.cursorDelay+=1;
		}
	}
	
	public DrawAttacksMenu(PkmnGen game, Action nextAction) {

		this.nextAction = nextAction;
		
		this.cursorDelay = 0;
		
		this.getCoords = new HashMap<Integer, Vector2>();
		this.spritesToDraw = new ArrayList<ArrayList<Sprite>>();
		
		Texture text = new Texture(Gdx.files.internal("battle/arrow_right1.png"));
		this.arrow = new Sprite(text, 0, 0, 5, 7);
		//this.arrow.setScale(3); //post scaling change
		
		//text box bg
		text = new Texture(Gdx.files.internal("attack_menu/attack_screen1.png"));
		this.textBox = new Sprite(text, 0,0, 16*10, 16*9);
		
		this.getCoords.put(0, new Vector2(41, 32));
		this.getCoords.put(1, new Vector2(41, 24));
		this.getCoords.put(2, new Vector2(41, 16));
		this.getCoords.put(3, new Vector2(41, 8));
		
		//this.newPos =  new Vector2(32, 79); //post scaling change
		this.newPos =  new Vector2(41, 32);
		this.arrow.setPosition(newPos.x, newPos.y);
		this.curr = 0;
		
		//convert pokemon attacks to sprites
		for (String attack : game.player.currPokemon.attacks) {
			char[] textArray = attack.toUpperCase().toCharArray(); //iterate elements
			Sprite currSprite;
			int i = 0;
			int j = 0;
			ArrayList<Sprite> word = new ArrayList<Sprite>();
			for (char letter : textArray) {
				//offsetNext += spriteWidth*3+2 //how to do this?
				Sprite letterSprite = game.textDict.get((char)letter);
				if (letterSprite == null) {
					letterSprite = game.textDict.get(null);
				}
				currSprite = new Sprite(letterSprite); //copy sprite from char-to-Sprite dictionary
				
				//currSprite.setPosition(10*3+8*i*3 +2, 26*3-16*j*3 +2); //offset x=8, y=25, spacing x=8, y=8(?)
				currSprite.setPosition(10+8*i +2-4, 26-16*j +2-4); //post scaling change
				//currSprite.setScale(3); //post scaling change
				
				word.add(currSprite);
				//go down a line if needed
				 //TODO - do this for words, not chars. split on space, array
				if (i >= 17) { 
					i = 0; j++;
				}
				else {
					i++;
				}
			}		
			spritesToDraw.add(word);	
		}
		
		
		//helper sprite
		text = new Texture(Gdx.files.internal("attack_menu/helper2.png"));
		this.helperSprite = new Sprite(text, 0,0, 16*10, 16*9);
	}
	
}





//fade out of battle to white
//fade out music too
class BattleFadeOut extends Action {
	
	ArrayList<Sprite> frames;
	Sprite frame;
	
	Action nextAction;

	public int layer = 129;
	public int getLayer(){return this.layer;}

	public String getCamera() {return "gui";};
	
	@Override
	public void step(PkmnGen game) {
		
		//should only happen once. not sure that repeat matters
		game.actionStack.remove(game.battle.drawAction); //stop drawing the battle
		game.battle.drawAction = null; //this is used as a flag in ThrowOutPokemon
		
		//if done with anim, do nextAction
		if (frames.isEmpty()) {
			PublicFunctions.insertToAS(game, this.nextAction);
			game.actionStack.remove(this);
			game.playerCanMove = true;
			return;
		}
		
		//get next frame
		this.frame = frames.get(0);
		
		if (this.frame != null) {
			//gui version
			this.frame.setScale(3); //scale doesn't work in batch.draw
			this.frame.setPosition(16*10,16*9);
			this.frame.draw(game.floatingBatch);
			//map version
			//game.batch.draw(this.frame, 16, -16);
		}
		
		frames.remove(0);

	}
	
	public BattleFadeOut(PkmnGen game, Action nextAction) {
		
		this.nextAction = nextAction;

		this.frames = new ArrayList<Sprite>();
		
		//fade out from white anim
		Texture text1 = new Texture(Gdx.files.internal("battle/intro_frame6.png"));
		Sprite sprite1 = new Sprite(text1);
		for (int i=0; i < 14; i++) {
			this.frames.add(sprite1);
		}
		text1 = new Texture(Gdx.files.internal("battle/intro_frame5.png"));
		sprite1 = new Sprite(text1);
		for (int i=0; i < 8; i++) {
			this.frames.add(sprite1);
		}
		text1 = new Texture(Gdx.files.internal("battle/intro_frame4.png"));
		sprite1 = new Sprite(text1);
		for (int i=0; i < 8; i++) {
			this.frames.add(sprite1);
		}
	}
}




class BattleFadeOutMusic extends Action {
	
	ArrayList<Float> frames;
	Float frame;
	
	Action nextAction;

	public int layer = 129;
	public int getLayer(){return this.layer;}

	public String getCamera() {return "gui";};
	
	@Override
	public void step(PkmnGen game) {


		//if done with anim, do nextAction
		if (frames.isEmpty()) {
			PublicFunctions.insertToAS(game, this.nextAction);
			game.actionStack.remove(this);
			game.battle.music.stop();
			game.battle.music.setVolume(.3f);
			//game.map.music.start();
			game.currMusic = game.map.currRoute.music;
			game.currMusic.play();
			return;
		}
		
		//get next frame
		this.frame = frames.get(0);
		
		game.battle.music.setVolume(frame);
		
		frames.remove(0);

	}
	
	public BattleFadeOutMusic(PkmnGen game, Action nextAction) {
		
		this.nextAction = nextAction;

		this.frames = new ArrayList<Float>();
		
		//fade out from white anim
		for (int i=0; i < 14; i++) {
			this.frames.add(.3f);
		}
		for (int i=0; i < 14; i++) {
			this.frames.add(.25f);
		}
		for (int i=0; i < 14; i++) {
			this.frames.add(.2f);
		}
		for (int i=0; i < 14; i++) {
			this.frames.add(.15f);
		}
		for (int i=0; i < 14; i++) {
			this.frames.add(.1f);
		}
		for (int i=0; i < 7; i++) {
			this.frames.add(.05f);
		}
		for (int i=0; i < 7; i++) {
			this.frames.add(.025f);
		}
	}
}


//draw battle elements
 //TODO - don't draw the options frame (ie fight, run etc)
 //TODO - on destroy, set drawAction to null?
class DrawBattle extends Action {


	Sprite bgSprite;
	
	DrawEnemyHealth drawEnemyHealthAction;

	public int layer = 130;
	public int getLayer(){return this.layer;}

	public String getCamera() {return "gui";};

	
	@Override
	public void step(PkmnGen game) {

		//draw helper sprite
		 //probly remove
		this.bgSprite.draw(game.floatingBatch);		
		
		game.battle.oppPokemon.sprite.draw(game.floatingBatch);
		game.player.battleSprite.draw(game.floatingBatch);
		
		
		//todo - remove
		//this gets assigned at some point. manually stepping here
		 //rather than inserting in AS b/c simpler
//		if (this.drawEnemyHealthAction != null) {
//			this.drawEnemyHealthAction.step(game);
//		}
		
		//todo - remove
//		//draw pkmn level bars
//		int tensPlace = game.battle.oppPokemon.level/10;
//		//System.out.println("level: "+String.valueOf(tensPlace));
//		Sprite tensPlaceSprite = game.textDict.get(Character.forDigit(tensPlace,10));
//		game.floatingBatch.draw(tensPlaceSprite, 40, 128);
//
//		int onesPlace = game.battle.oppPokemon.level % 10;
//		Sprite onesPlaceSprite = game.textDict.get(Character.forDigit(onesPlace,10));
//		game.floatingBatch.draw(onesPlaceSprite, 48, 128);
//
//		char[] textArray = game.battle.oppPokemon.name.toUpperCase().toCharArray();
//		Sprite letterSprite;
//		for (int i=0; i < textArray.length; i++) {
//			letterSprite = game.textDict.get(textArray[i]);
//			game.floatingBatch.draw(letterSprite, 8+8*i, 136);
//		}
		
	}
	
	public DrawBattle(PkmnGen game) {

		Texture text = new Texture(Gdx.files.internal("battle/battle_bg2.png"));
		//Texture text = new Texture(Gdx.files.internal("battle/helper1.png"));
		this.bgSprite = new Sprite(text, 0, 0, 160, 144);
		//this.bgSprite.setPosition(16*10,16*9);
		//this.bgSprite.setScale(3);
		//TODO - make bg the text box.(?)
		
		game.battle.drawAction = this;
	}
}



//instance of this assigned to DrawBattle.drawEnemyHealthAction
class DrawEnemyHealth extends Action {

	Sprite bgSprite;
	public ArrayList<Sprite> healthBar;
	
	public int layer = 129;
	public int getLayer(){return this.layer;}

	public String getCamera() {return "gui";};

	
	@Override
	public void step(PkmnGen game) {

		//draw helper sprite
		 //probly remove
		this.bgSprite.draw(game.floatingBatch);		
		
		//draw pkmn level bars
		int tensPlace = game.battle.oppPokemon.level/10;
		//System.out.println("level: "+String.valueOf(tensPlace));
		Sprite tensPlaceSprite = game.textDict.get(Character.forDigit(tensPlace,10));
		game.floatingBatch.draw(tensPlaceSprite, 40, 128);

		int onesPlace = game.battle.oppPokemon.level % 10;
		Sprite onesPlaceSprite = game.textDict.get(Character.forDigit(onesPlace,10));
		game.floatingBatch.draw(onesPlaceSprite, 48, 128);

		char[] textArray = game.battle.oppPokemon.name.toUpperCase().toCharArray();
		Sprite letterSprite;
		for (int i=0; i < textArray.length; i++) {
			letterSprite = game.textDict.get(textArray[i]);
			game.floatingBatch.draw(letterSprite, 8+8*i, 136);
		}
		
		//draw health bar
		for (Sprite bar : this.healthBar) {
			bar.draw(game.floatingBatch);
		}
		
		//detect when battle is over, 
		//object will remove itself from AS
		if (game.battle.drawAction == null) {
			game.actionStack.remove(this);
		}
		
	}
	
	public DrawEnemyHealth(PkmnGen game) {

		Texture text = new Texture(Gdx.files.internal("battle/enemy_healthbar1.png"));
		this.bgSprite = new Sprite(text, 0, 0, 160, 144);

		//this could be dangerous?
		//if drawAction is null. this may happen at some point.
		game.battle.drawAction.drawEnemyHealthAction = this; 
		
		//fill sprite array according to enemy health
		 //healthbar is 48 pixels long
		 //round up when counting
		this.healthBar = new ArrayList<Sprite>();
		int numElements = (int)Math.ceil( (game.battle.oppPokemon.currentStats.get("hp")*48f) / game.battle.oppPokemon.maxStats.get("hp") );
		
		//System.out.println("numElements: "+String.valueOf(numElements)); //debug
		
		text = new Texture(Gdx.files.internal("battle/health1.png"));
		Sprite temp = new Sprite(text, 0,0,1,2);
		for (int i = 0; i < numElements; i++) {
			Sprite temp2 = new Sprite(temp); //to avoid long loading
			temp2.setPosition(32 + i, 123);
			this.healthBar.add(temp2);
		}
		
	}
}



//instance of this assigned to DrawBattle.drawEnemyHealthAction
class DepleteEnemyHealth extends Action {

	
	public int layer = 129;
	public int getLayer(){return this.layer;}

	public String getCamera() {return "gui";};

	
	boolean firstStep;
	int timer;
	int removeNumber;
	int targetSize; //reduce enemy health bar to this number
	Action nextAction;
	
	@Override
	public void step(PkmnGen game) {
	
		if (this.firstStep == true) {
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
			PublicFunctions.insertToAS(game, this.nextAction);
		}
		
	}
	
	public DepleteEnemyHealth(PkmnGen game, Action nextAction) {
		
		this.nextAction = nextAction;
		
		this.firstStep = true;
		this.timer = 3;
		this.removeNumber = 2;
		
		
	}
}



//draws rock flying through air and hitting opposing pkmn
class ThrowRock extends Action {

	//remove
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
	public int getLayer(){return this.layer;}

	public String getCamera() {return "gui";};

	Action nextAction;
	
	Sprite helperSprite; //just for helping me position the animation. delete later.
	
	@Override
	public void step(PkmnGen game) {


		//set sprite position
		//if done with anim, do nextAction
		if (positions.isEmpty() || sprites.isEmpty()) {
			//modify opp pokemon's 'angry' level
			//"every time a rock is thrown, the catch rate C is doubled" - http://www.dragonflycave.com/safarizone.aspx
			int currCatchRate = game.battle.oppPokemon.currentStats.get("catchRate");
			game.battle.oppPokemon.currentStats.put("catchRate", currCatchRate*2);
			if (game.battle.oppPokemon.currentStats.get("catchRate") > 255) { //cap at 255
				game.battle.oppPokemon.currentStats.put("catchRate", 255);
			}
			
			//random between 1 and 5, add to angry counter
			int randomNum = game.map.rand.nextInt(5)+1;
			game.battle.oppPokemon.angry += randomNum;
			if (game.battle.oppPokemon.angry > 255) { //cap at 255
				game.battle.oppPokemon.angry = 255;
			}
			
			//set eating counter to 0 
			game.battle.oppPokemon.eating = 0;
			
			//debug
			System.out.println("angry counter: "+String.valueOf(game.battle.oppPokemon.angry));
			System.out.println("Catch Rate: "+String.valueOf(game.battle.oppPokemon.currentStats.get("catchRate")));
			
			//wait 3 frames before menu draw
			 //allows text box to be blank for 3 frames (drawText is already blank 1 frame by default)
			PublicFunctions.insertToAS(game, new WaitFrames(game, 2, this.nextAction));
			game.actionStack.remove(this);
			return;
		}

		//get next sound, play it
		this.sound = this.sounds.get(0);
		if (this.sound != null) {
			PublicFunctions.insertToAS(game, new PlaySound(this.sound, new DoneAction()));
			this.sounds.set(0, null); //don't play same sound over again
		}
		
		//get next frame
		this.sprite = sprites.get(0);
		
		//debug
		//this.helperSprite.draw(game.floatingBatch); //debug
		
		if (this.sprite != null) {
			//this.sprite.setScale(3); //post scaling change
			this.sprite.setPosition(position.x, position.y);
			this.sprite.draw(game.floatingBatch);
		}
		
//		if (this.frames.size() == 1) { //debug
//			return;
//		}
		
		//repeat sprite/pos for current object for 'frames[0]' number of frames.
		if (this.frames.get(0) > 0) {
			this.frames.set(0, this.frames.get(0) - 1);
		}
		else {
			//since position is relative, only update once each time period
			this.position = this.position.add(positions.get(0));
			positions.remove(0);
			sprites.remove(0);
			frames.remove(0);
			sounds.remove(0);
		}
		
		
		
	}
	
	public ThrowRock(PkmnGen game, Action nextAction) {

		this.nextAction = nextAction;
		
		Texture text = new Texture(Gdx.files.internal("throw_rock_anim/hit1.png"));
		this.hitSprite = new Sprite(text, 0, 0, 24, 24);
		
		text = new Texture(Gdx.files.internal("throw_rock_anim/rock_small1.png"));
		this.rockSprite = new Sprite(text, 0, 0, 8, 8);
		
		//positions is added to position every so often
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
		this.positions.add(new Vector2(0,0)); //72 frames of nothing
		this.positions.add(new Vector2(0,0)); //need dummy pos
		//13 events total
		
		this.sprites =  new ArrayList<Sprite>();
		for (int i = 0; i < 11; i++) {
			this.sprites.add(this.rockSprite);
		}
		this.sprites.add(this.hitSprite);
		this.sprites.add(null);
		//13 events total

		this.frames = new ArrayList<Integer>();
		for (int i = 0; i < 12; i++) {
			this.frames.add(3);
		}
		this.frames.add(72-1); //72 frames of nothing at end
		//13 events total

		text = new Texture(Gdx.files.internal("throw_rock_anim/helper12.png"));
		this.helperSprite = new Sprite(text, 0, 0, 160, 144);
		//this.helperSprite.setPosition(16*10,16*9); //post scaling change
		//this.helperSprite.setScale(3); //post scaling change
		
		
		//PublicFunctions.insertToAS(game, new PlaySound("throw_rock1", new DoneAction()));
		//sounds to play
		this.sounds = new ArrayList<String>();
		this.sounds.add("throw_rock1");
		for (int i = 0; i < 12; i++) { 
			this.sounds.add(null);
		}
		//13 events total
		
	}
}


//draws rock flying through air and hitting opposing pkmn
class ThrowBait extends Action {

	//remove
	Sprite baitSprite;
	
	ArrayList<Vector2> positions;
	Vector2 position;
	ArrayList<Sprite> sprites;
	Sprite sprite;
	ArrayList<Integer> frames;
	String sound;
	ArrayList<String> sounds;

	public int layer = 120;
	public int getLayer(){return this.layer;}

	public String getCamera() {return "gui";};

	Action nextAction;
	
	Sprite helperSprite; //just for helping me position the animation. delete later.
	
	@Override
	public void step(PkmnGen game) {


		//set sprite position
		//if done with anim, do nextAction
		if (positions.isEmpty() || sprites.isEmpty()) {
			//modify opp pokemon's 'angry' level
			//"every time a rock is thrown, the catch rate C is doubled" - http://www.dragonflycave.com/safarizone.aspx
			int currCatchRate = game.battle.oppPokemon.currentStats.get("catchRate");
			game.battle.oppPokemon.currentStats.put("catchRate", currCatchRate/2);
			
			//random between 1 and 5, add to eating counter
			int randomNum = game.map.rand.nextInt(5)+1;
			game.battle.oppPokemon.eating += randomNum;
			if (game.battle.oppPokemon.eating > 255) { //cap at 255
				game.battle.oppPokemon.eating = 255;
			}
			
			//set angry counter to 0 
			game.battle.oppPokemon.angry = 0;
			
			//debug
			System.out.println("eating counter: "+String.valueOf(game.battle.oppPokemon.eating));
			System.out.println("Catch Rate: "+String.valueOf(game.battle.oppPokemon.currentStats.get("catchRate")));
			
			//wait 3 frames before menu draw
			 //allows text box to be blank for 3 frames (drawText is already blank 1 frame by default)
			PublicFunctions.insertToAS(game, new WaitFrames(game, 2, this.nextAction));
			game.actionStack.remove(this);
			return;
		}

		//get next sound, play it
		this.sound = this.sounds.get(0);
		if (this.sound != null) {
			PublicFunctions.insertToAS(game, new PlaySound(this.sound, new DoneAction()));
			this.sounds.set(0, null); //don't play same sound over again
		}
		
		//get next frame
		this.sprite = sprites.get(0);
		
		//debug
		//this.helperSprite.draw(game.floatingBatch); //debug
		
		if (this.sprite != null) {
			//this.sprite.setScale(3); //post scaling change
			this.sprite.setPosition(position.x, position.y);
			this.sprite.draw(game.floatingBatch);
		}
		
//		if (this.frames.size() == 1) { //debug
//			return;
//		}
		
		//repeat sprite/pos for current object for 'frames[0]' number of frames.
		if (this.frames.get(0) > 0) {
			this.frames.set(0, this.frames.get(0) - 1);
		}
		else {
			//since position is relative, only update once each time period
			this.position = this.position.add(positions.get(0));
			positions.remove(0);
			sprites.remove(0);
			frames.remove(0);
			sounds.remove(0);
		}
		
		
		
	}
	
	public ThrowBait(PkmnGen game, Action nextAction) {

		this.nextAction = nextAction;
				
		Texture text = new Texture(Gdx.files.internal("throw_rock_anim/bait_small1.png"));
		this.baitSprite = new Sprite(text, 0, 0, 8, 8);
		
		//positions is added to position every so often
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
		this.positions.add(new Vector2(0,0)); //72 frames of nothing
		this.positions.add(new Vector2(0,0)); //need dummy pos
		//12 events total
		
		this.sprites =  new ArrayList<Sprite>();
		for (int i = 0; i < 11; i++) {
			this.sprites.add(this.baitSprite);
		}
		this.sprites.add(null);
		//12 events total

		this.frames = new ArrayList<Integer>();
		for (int i = 0; i < 11; i++) {
			this.frames.add(3);
		}
		this.frames.add(73-1); //73 frames of nothing at end
		//12 events total

		//text = new Texture(Gdx.files.internal("throw_rock_anim/helper12.png"));
		//this.helperSprite = new Sprite(text, 0, 0, 160, 144);
		//this.helperSprite.setPosition(16*10,16*9); //post scaling change
		//this.helperSprite.setScale(3); //post scaling change
		
		
		//PublicFunctions.insertToAS(game, new PlaySound("throw_rock1", new DoneAction()));
		//sounds to play
		this.sounds = new ArrayList<String>();
		this.sounds.add("throw_rock1");
		for (int i = 0; i < 11; i++) { 
			this.sounds.add(null);
		}
		//12 events total
		
	}
}




//draws pokeball flying through air and hitting opposing pkmn
 //whether to catch or not is decided at end of this action
 //pass nextAction to the new action (probably a draw menu action, for example DrawBattleMenu_SafariZone)
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
	public int getLayer(){return this.layer;}

	public String getCamera() {return "gui";};

	Action nextAction;
	
	Sprite helperSprite; //just for helping me position the animation. delete later.
	
	@Override
	public void step(PkmnGen game) {

		//set sprite position
		//if done with anim, do nextAction
		if (positions.isEmpty() || sprites.isEmpty()) {
			//note - it's possible that calling Action could take care of the below instead
			 //nextAction is likely a menu draw Action, ie safariMenuDraw
			//PublicFunctions.insertToAS(game, new catchPokemon_wigglesThenCatch(game, this.nextAction)); //before passed from menu
			//PublicFunctions.insertToAS(game, new catchPkmn_oneWiggle(this.nextAction)); //later
			PublicFunctions.insertToAS(game, this.nextAction);
			game.actionStack.remove(this);
			return;
		}
		
		//get next sound, play it
		this.sound = this.sounds.get(0);
		if (this.sound != null) {
			PublicFunctions.insertToAS(game, new PlaySound(this.sound, new DoneAction()));
			this.sounds.set(0, null); //don't play same sound over again
		}
		
		
		//get next frame
		this.sprite = sprites.get(0);
		
		//debug
		//this.helperSprite.draw(game.floatingBatch); 
		
		//draw current sprite
		if (this.sprite != null) {
			//this.sprite.setScale(3);  //post scaling change
			this.sprite.setPosition(position.x, position.y);
			this.sprite.draw(game.floatingBatch);			
		}
		
		//debug
//		if (this.repeats.size() == 5) { 
//			return;
//		}
		
		//repeat sprite/pos for current object for 'frames[0]' number of frames.
		if (this.repeats.get(0) > 0) {
			this.repeats.set(0, this.repeats.get(0) - 1);
		}
		else {
			//since position is relative, only update once each time period
			this.position = this.position.add(positions.get(0));
			positions.remove(0);
			sprites.remove(0);
			repeats.remove(0);
			sounds.remove(0);
		}
	}
	
	public ThrowPokeball(PkmnGen game, Action nextAction) {

		this.nextAction = nextAction;

		Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball1.png"));
		this.pokeballSprite = new Sprite(text, 0, 0, 12, 12);
		
		//consider doing relative positions from now on
		//this.position = new Vector2(104+4*3-2,200-6*3-2); //post scaling change
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
		this.positions.add(new Vector2(-6*3,-10*3+8));//first of poof anim
		this.positions.add(new Vector2(0,0));
		this.positions.add(new Vector2(0,0));
		this.positions.add(new Vector2(0,0));
		this.positions.add(new Vector2(0,0));
		//this.positions.add(new Vector2(0,0)); //wait frames before pokeball appears  //handled in next action now //delete
		this.positions.add(new Vector2(0,0)); //last is always dummy pos 
		
		this.sprites =  new ArrayList<Sprite>();
		for (int i = 0; i < 11; i++) {
			this.sprites.add(this.pokeballSprite);
		}
		this.sprites.add(null); //draw nothing for 10 frames
		
		//'poof' animation 
		text = new Texture(Gdx.files.internal("throw_pokeball_anim/poof_sheet1.png"));
		this.sprites.add(new Sprite(text, 48*0, 0, 48, 48)); 
		this.sprites.add(new Sprite(text, 48*1, 0, 48, 48)); 
		this.sprites.add(new Sprite(text, 48*2, 0, 48, 48)); 
		this.sprites.add(new Sprite(text, 48*3, 0, 48, 48)); 
		this.sprites.add(new Sprite(text, 48*4, 0, 48, 48)); 

		//this.sprites.add(null); //draw nothing for 13 frames //handled in next action now //delete

		
		this.repeats = new ArrayList<Integer>();
		for (int i = 0; i < 11; i++) {
			this.repeats.add(2);
		}
		this.repeats.add(10-1); //wait 10 frames
		
		for (int i = 0; i < 4; i++) { //4 poof frames
			this.repeats.add(4);
		}
		this.repeats.add(10-1); //wait 10 frames for last poof frame
		
		//this.repeats.add(13-1); //13 frames before pokeball appears on ground //handled in next action now //delete
		
		
		//sounds to play
		this.sounds = new ArrayList<String>();
		this.sounds.add("throw_pokeball1");
		for (int i = 0; i < 11; i++) { 
			this.sounds.add(null);
		}
		this.sounds.add("poof1");
		for (int i = 0; i < 11; i++) { 
			this.sounds.add(null);
		}
		
		text = new Texture(Gdx.files.internal("throw_pokeball_anim/helper13.png"));
		this.helperSprite = new Sprite(text, 0, 0, 160, 144);
		//this.helperSprite.setPosition(16*10,16*9); //post scaling change
		//this.helperSprite.setScale(3); //post scaling change

		//play 'throw pokeball' sound
		//PublicFunctions.insertToAS(game, new PlaySound("throw_pokeball1", new DoneAction()));
		
		
	}
}


//demo code
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
	public int getLayer(){return this.layer;}

	public String getCamera() {return "gui";};

	Action nextAction;
	
	Sprite helperSprite; //just for helping me position the animation. delete later.
	
	@Override
	public void step(PkmnGen game) {

		//set sprite position
		//if done with anim, do nextAction
		if (positions.isEmpty() || sprites.isEmpty()) {
			//note - it's possible that calling Action could take care of the below instead
			 //nextAction is likely a menu draw Action, ie safariMenuDraw
			//PublicFunctions.insertToAS(game, new catchPokemon_wigglesThenCatch(game, this.nextAction)); //before passed from menu
			//PublicFunctions.insertToAS(game, new catchPkmn_oneWiggle(this.nextAction)); //later
			PublicFunctions.insertToAS(game, this.nextAction);
			game.actionStack.remove(this);
			return;
		}
		
		//get next sound, play it
		this.sound = this.sounds.get(0);
		if (this.sound != null) {
			PublicFunctions.insertToAS(game, new PlaySound(this.sound, new DoneAction()));
			this.sounds.set(0, null); //don't play same sound over again
		}
		
		
		//get next frame
		this.sprite = sprites.get(0);
		
		//debug
		//this.helperSprite.draw(game.floatingBatch); 
		
		//draw current sprite
		if (this.sprite != null) {
			//this.sprite.setScale(3);  //post scaling change
			this.sprite.setPosition(position.x, position.y);
			this.sprite.draw(game.floatingBatch);			
		}
		
		//debug
//		if (this.repeats.size() == 16) { 
//			return;
//		}
		
		//repeat sprite/pos for current object for 'frames[0]' number of frames.
		if (this.repeats.get(0) > 0) {
			this.repeats.set(0, this.repeats.get(0) - 1);
		}
		else {
			//since position is relative, only update once each time period
			this.position = this.position.add(positions.get(0));
			positions.remove(0);
			sprites.remove(0);
			repeats.remove(0);
			sounds.remove(0);
		}
	}
	
	public ThrowFastPokeball(PkmnGen game, Action nextAction) {

		this.nextAction = nextAction;

		Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball1.png"));
		this.pokeballSprite = new Sprite(text, 0, 0, 12, 12);
		
		//consider doing relative positions from now on
		//this.position = new Vector2(104+4*3-2,200-6*3-2); //post scaling change
		this.position = new Vector2(34-16,56-16);
		
		this.positions = new ArrayList<Vector2>();
		//harden frames
		this.positions.add(new Vector2(0,0));
		this.positions.add(new Vector2(0,0));
		this.positions.add(new Vector2(0,0));
		this.positions.add(new Vector2(0+16,0+16));//move to pokeball first spot
		
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
		this.positions.add(new Vector2(-6*3,-10*3+8));//first of poof anim
		this.positions.add(new Vector2(0,0));
		this.positions.add(new Vector2(0,0));
		this.positions.add(new Vector2(0,0));
		this.positions.add(new Vector2(0,0));
		//this.positions.add(new Vector2(0,0)); //wait frames before pokeball appears  //handled in next action now //delete
		this.positions.add(new Vector2(0,0)); //last is always dummy pos 
		//20 events total
		
		this.sprites =  new ArrayList<Sprite>();
		text = new Texture(Gdx.files.internal("attacks/harden_sheet1.png"));
		this.sprites.add(null); //draw nothing for 7 frames
		this.sprites.add(new Sprite(text, 56*0, 0, 56, 56));
		this.sprites.add(new Sprite(text, 56*1, 0, 56, 56));
		this.sprites.add(new Sprite(text, 56*2, 0, 56, 56));
		
		for (int i = 0; i < 11; i++) {
			this.sprites.add(this.pokeballSprite);
		}
		this.sprites.add(null); //draw nothing for 10 frames
		
		//'poof' animation 
		text = new Texture(Gdx.files.internal("throw_pokeball_anim/poof_sheet1.png"));
		this.sprites.add(new Sprite(text, 48*0, 0, 48, 48)); 
		this.sprites.add(new Sprite(text, 48*1, 0, 48, 48)); 
		this.sprites.add(new Sprite(text, 48*2, 0, 48, 48)); 
		this.sprites.add(new Sprite(text, 48*3, 0, 48, 48)); 
		this.sprites.add(new Sprite(text, 48*4, 0, 48, 48)); 
		//21 events total

		//this.sprites.add(null); //draw nothing for 13 frames //handled in next action now //delete

		
		this.repeats = new ArrayList<Integer>();
		this.repeats.add(7); //
		this.repeats.add(7); //
		this.repeats.add(7); //
		this.repeats.add(7); //
		for (int i = 0; i < 11; i++) {
			this.repeats.add(1);
		}
		this.repeats.add(10-1); //wait 10 frames
		
		for (int i = 0; i < 4; i++) { //4 poof frames
			this.repeats.add(4);
		}
		this.repeats.add(10-1); //wait 10 frames for last poof frame
		//21 events total
		
		//this.repeats.add(13-1); //13 frames before pokeball appears on ground //handled in next action now //delete
		
		
		//sounds to play
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
		
		text = new Texture(Gdx.files.internal("throw_pokeball_anim/helper13.png"));
		this.helperSprite = new Sprite(text, 0, 0, 160, 144);
		//this.helperSprite.setPosition(16*10,16*9); //post scaling change
		//this.helperSprite.setScale(3); //post scaling change

		//play 'throw pokeball' sound
		//PublicFunctions.insertToAS(game, new PlaySound("throw_pokeball1", new DoneAction()));
		
		
	}
}


//demo code
 //pokeball throw that looks like hyperbeam
 //used when adr > 10 atm
class ThrowHyperPokeball extends Action {
	
	ArrayList<Vector2> positions;
	Vector2 position;
	ArrayList<Sprite> sprites;
	Sprite sprite;
	ArrayList<Integer> repeats;
	String sound;
	ArrayList<String> sounds;
	
	public int layer = 120;
	public int getLayer(){return this.layer;}

	public String getCamera() {return "gui";};

	Action nextAction;
	
	Sprite helperSprite; //just for helping me position the animation. delete later.
	
	@Override
	public void step(PkmnGen game) {

		//set sprite position
		//if done with anim, do nextAction
		if (positions.isEmpty() || sprites.isEmpty()) {
			//note - it's possible that calling Action could take care of the below instead
			 //nextAction is likely a menu draw Action, ie safariMenuDraw
			//PublicFunctions.insertToAS(game, new catchPokemon_wigglesThenCatch(game, this.nextAction)); //before passed from menu
			//PublicFunctions.insertToAS(game, new catchPkmn_oneWiggle(this.nextAction)); //later
			PublicFunctions.insertToAS(game, this.nextAction);
			game.actionStack.remove(this);
			return;
		}
		
		//get next sound, play it
		this.sound = this.sounds.get(0);
		if (this.sound != null) {
			PublicFunctions.insertToAS(game, new PlaySound(this.sound, new DoneAction()));
			this.sounds.set(0, null); //don't play same sound over again
		}
		
		
		//get next frame
		this.sprite = sprites.get(0);
		
		//debug
//		this.helperSprite.draw(game.floatingBatch); 
		
		//draw current sprite
		if (this.sprite != null) {
			//this.sprite.setScale(3);  //post scaling change
			this.sprite.setPosition(position.x, position.y);
			this.sprite.draw(game.floatingBatch);			
		}
		
		//debug
//		if (this.repeats.size() == 5) { 
//			return;
//		}
		
		//repeat sprite/pos for current object for 'frames[0]' number of frames.
		if (this.repeats.get(0) > 0) {
			this.repeats.set(0, this.repeats.get(0) - 1);
		}
		else {
			//since position is relative, only update once each time period
			this.position = this.position.add(positions.get(0));
			positions.remove(0);
			sprites.remove(0);
			repeats.remove(0);
			sounds.remove(0);
		}
	}
	
	public ThrowHyperPokeball(PkmnGen game, Action nextAction) {

		this.nextAction = nextAction;

		
		//consider doing relative positions from now on
		//this.position = new Vector2(104+4*3-2,200-6*3-2); //post scaling change
		this.position = new Vector2(50,72); //
		
		this.positions = new ArrayList<Vector2>();
		this.positions.add(new Vector2(0,0));
		this.positions.add(new Vector2(0,0));
		this.positions.add(new Vector2(0,0));
		this.positions.add(new Vector2(0,0));
		this.positions.add(new Vector2(0,0));
		this.positions.add(new Vector2(0,0));
		this.positions.add(new Vector2(0,0));
		this.positions.add(new Vector2(0+38+8,0)); //poof1
		this.positions.add(new Vector2(0,0));
		this.positions.add(new Vector2(0,0));
		this.positions.add(new Vector2(0,0));
		this.positions.add(new Vector2(0,0));
		this.positions.add(new Vector2(0,0));
		//this.positions.add(new Vector2(0,0)); //last is always dummy pos 
		//13 total events

		Texture text = new Texture(Gdx.files.internal("hyper_beam_anim/hyperbeam_sheet2.png"));
		
		this.sprites =  new ArrayList<Sprite>();
		this.sprites.add(null); //draw nothing for 14 frames
		for (int i = 0; i < 6; i++) {
			this.sprites.add(new Sprite(text, i*72, 0, 72, 40));
		}
		this.sprites.add(null); //draw nothing for 10 frames
		
		//'poof' animation 
		//TODO - actually missing beam frames
		text = new Texture(Gdx.files.internal("throw_pokeball_anim/poof_sheet1.png"));
		this.sprites.add(new Sprite(text, 48*0, 0, 48, 48)); 
		this.sprites.add(new Sprite(text, 48*1, 0, 48, 48)); 
		this.sprites.add(new Sprite(text, 48*2, 0, 48, 48)); 
		this.sprites.add(new Sprite(text, 48*3, 0, 48, 48)); 
		this.sprites.add(new Sprite(text, 48*4, 0, 48, 48)); 

		//13 total events


		
		this.repeats = new ArrayList<Integer>();
		this.repeats.add(14-1); //wait 14 frames
		for (int i = 0; i < 3; i++) {
			this.repeats.add(4); //6
			this.repeats.add(3); //2
		}
		this.repeats.add(3-1); //wait 10 frames
		
		for (int i = 0; i < 4; i++) { //4 poof frames
			this.repeats.add(4);
		}
		this.repeats.add(10-1); //wait 10 frames for last poof frame
		//13 total events
		
		
		//sounds to play
		this.sounds = new ArrayList<String>();
		this.sounds.add("hyperbeam1");
		for (int i = 0; i < 7; i++) { 
			this.sounds.add(null);
		}
		this.sounds.add("poof1");
		for (int i = 0; i < 4; i++) { 
			this.sounds.add(null);
		}
		//13 total events
		
		//text = new Texture(Gdx.files.internal("hyper_beam_anim/helper1.png"));
		text = new Texture(Gdx.files.internal("throw_pokeball_anim/helper13.png"));
		this.helperSprite = new Sprite(text, 0, 0, 160, 144);

		//play 'throw pokeball' sound
		//PublicFunctions.insertToAS(game, new PlaySound("throw_pokeball1", new DoneAction()));
		
		
	}
}


//draws pokeball wiggling once then releasing pokemon
 //note - maybe this should be a chain of actions? depending on how probabilities work?
class catchPokemon_wigglesThenCatch extends Action {

	//Sprite pokeballSprite;
	
	ArrayList<Vector2> positions;
	Vector2 position;
	ArrayList<Sprite> sprites;
	Sprite sprite;
	ArrayList<Integer> repeats;
	String sound;
	ArrayList<String> sounds;
	
	public int layer = 120;
	public int getLayer(){return this.layer;}

	public String getCamera() {return "gui";};

	Action nextAction;
	
	Sprite helperSprite; //just for helping me position the animation. delete later.
	
	@Override
	public void step(PkmnGen game) {

		//set opp pokemon sprite alpha
		game.battle.oppPokemon.sprite.setAlpha(0);
		
		//set sprite position
		//if done with anim, do nextAction
		if (positions.isEmpty() || sprites.isEmpty()) {

			

			//demo code - add to player adrenaline
			int adrenaline = (int)Math.ceil((256 - game.battle.oppPokemon.baseStats.get("catchRate"))/100.0f);
			game.player.adrenaline += adrenaline;
			System.out.println("adrenaline: " + String.valueOf(adrenaline));
			
			//
			
			//PokemonCaught_Events - sprite and text
			//new DisplayText(game, string)
			Action newAction = new PokemonCaught_Events(
									game, adrenaline, new SplitAction( //demo code
										new BattleFadeOut(game, 
												new DoneAction() //new playerStanding(game)
										),
										new BattleFadeOutMusic(game, new DoneAction())
									)
								);
										
			PublicFunctions.insertToAS(game, newAction);
			newAction.step(game);//need to draw the pokeball
			
			//PublicFunctions.insertToAS(game, new BattleFadeOut(game, new playerStanding(game)));
			//PublicFunctions.insertToAS(game, new BattleFadeOutMusic(game, new DoneAction()));
			
			//since pkmn was caught, add to players pokemon
			game.player.pokemon.add(game.battle.oppPokemon);
			//remove this pokemon from the map
			game.map.currRoute.pokemon.remove(game.battle.oppPokemon);
			//TODO - bug if all pokemon removed from route
			 //is this a good way to handle it?
			//game.map.currRoute.pokemon.add(new Pokemon(game.battle.oppPokemon.name, game.battle.oppPokemon.level));

			//demo code - add random pkmn with less or equal catch rate from route.
			/* TODO - remove this
			String pokemonName = null;
			while (pokemonName == null) { //should eventually resolve, b/c can always at least find same pkmn
				int index = game.map.rand.nextInt(game.map.currRoute.allowedPokemon.size());
				Pokemon temp = new Pokemon(game.map.currRoute.allowedPokemon.get(index), 1);
				if ( temp.baseStats.get("catchRate") <= game.battle.oppPokemon.baseStats.get("catchRate")) {
					pokemonName = game.map.currRoute.allowedPokemon.get(index);
				}
			}
			//TODO -level?
			game.map.currRoute.pokemon.add(new Pokemon(pokemonName, game.battle.oppPokemon.level));
			System.out.println("New pkmn: " + pokemonName);
			*/
			//brings pokemon count back up to max
			 
			game.map.currRoute.genPokemon(game.battle.oppPokemon.baseStats.get("catchRate"));
			

			
			game.actionStack.remove(this);
			return;
		}
		
		//get next sound, play it
		this.sound = this.sounds.get(0);
		if (this.sound != null) {
			PublicFunctions.insertToAS(game, new PlaySound(this.sound, new DoneAction()));
			this.sounds.set(0, null); //don't play same sound over again
		}
		
		
		//get next frame
		this.sprite = sprites.get(0);
		
		//remove later
		//this.helperSprite.draw(game.floatingBatch); //debug
		
		//draw current sprite
		if (this.sprite != null) {
			//this.sprite.setScale(3); //post scaling change
			this.sprite.setPosition(position.x, position.y);
			this.sprite.draw(game.floatingBatch);			
		}
		
//		if (this.repeats.size() == 18) { //debug 
//			return;
//		}
		
		//repeat sprite/pos for current object for 'frames[0]' number of frames.
		if (this.repeats.get(0) > 0) {
			this.repeats.set(0, this.repeats.get(0) - 1);
		}
		else {
			//since position is relative, only update once each time period
			this.position = this.position.add(positions.get(0));
			positions.remove(0);
			sprites.remove(0);
			repeats.remove(0);
			sounds.remove(0);
		}
	}
	
	public catchPokemon_wigglesThenCatch(PkmnGen game, Action nextAction) {

		this.nextAction = nextAction;
		
		Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball1.png"));
		
		//initial sprite position
		this.position = new Vector2(114,88); //post scaling change
		
		this.positions = new ArrayList<Vector2>();
		this.positions.add(new Vector2(0,0)); //wait 13
		for (int i = 0; i < 3; i++) {
			this.positions.add(new Vector2(-1,0));
			this.positions.add(new Vector2(1,0));
			this.positions.add(new Vector2(1,0));
			this.positions.add(new Vector2(-1,0));
		}
		//13 total
		
		//wiggle anim
		this.sprites =  new ArrayList<Sprite>();
		this.sprites.add(null); //draw nothing for 13 frames
		
		text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball_wiggleSheet1.png"));
		this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
		this.sprites.add(new Sprite(text, 12*1, 0, 12, 12)); //left tilt
		this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
		this.sprites.add(new Sprite(text, 12*2, 0, 12, 12)); //right tilt
		this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
		this.sprites.add(new Sprite(text, 12*1, 0, 12, 12)); //left tilt
		this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
		this.sprites.add(new Sprite(text, 12*2, 0, 12, 12)); //right tilt
		this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
		this.sprites.add(new Sprite(text, 12*1, 0, 12, 12)); //left tilt
		this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
		this.sprites.add(new Sprite(text, 12*2, 0, 12, 12)); //right tilt //done at this point //13 total events

		
		this.repeats = new ArrayList<Integer>();
		
		this.repeats.add(13-1); //13 frames before pokeball appears on ground

		this.repeats.add(44-1); //first middle sprite has 44 frames
		for (int i = 0; i < 3; i++) { //left-middle-right tilt all have 4 frames
			this.repeats.add(4-1);
		}
		this.repeats.add(44-1); //first middle sprite has 44 frames
		for (int i = 0; i < 3; i++) { //left-middle-right tilt all have 4 frames
			this.repeats.add(4-1);
		}
		this.repeats.add(44-1); //first middle sprite has 44 frames
		for (int i = 0; i < 2; i++) { //left-middle tilt both have 4 frames
			this.repeats.add(4-1);
		}
		this.repeats.add(6-1); //final right tilt has 6 frames
		//13 total
		
		//sounds to play
		this.sounds = new ArrayList<String>();
		//this.sounds.add(null); //wait 13 no sound
		this.sounds.add("pokeball_wiggle1"); //wiggle as ball appears (doing as white appears, so it's uniform)
		this.sounds.add(null);
		this.sounds.add("pokeball_wiggle1");
		for (int i = 0; i < 3; i++) {
			this.sounds.add(null); 
		}
		this.sounds.add("pokeball_wiggle1");
		for (int i = 0; i < 6; i++) {
			this.sounds.add(null); 
		}
		//this.sounds.add("pokeball_wiggle1");
//		for (int i = 0; i < 3; i++) {
//			this.sounds.add(null); 
//		}
		
		text = new Texture(Gdx.files.internal("throw_pokeball_anim/helper18.png"));
		this.helperSprite = new Sprite(text, 0, 0, 160, 144);
		//this.helperSprite.setPosition(16*10,16*9); //post scaling change
		//this.helperSprite.setScale(3); //post scaling change
		
	}
}



class catchPokemon_miss extends Action {

	ArrayList<Float> alphas;
	
	public int layer = 120;
	public int getLayer(){return this.layer;}

	public String getCamera() {return "gui";};

	Action nextAction;
	
	Sprite helperSprite; 
	
	@Override
	public void step(PkmnGen game) {

		// - 1 frame then text dissappears
		// - 3 frames nothing, then text appears
		
		String textString = "You missed the POKéMON!"; 
		PublicFunctions.insertToAS(game, new WaitFrames(game, 3,
												new DisplayText(game, textString, null, null, this.nextAction)
											)
										);
		
		game.actionStack.remove(this);
		return;
		
	}
	
	public catchPokemon_miss(PkmnGen game, Action nextAction) {

		this.nextAction = nextAction;
		/*
		this.alphas = new ArrayList<Float>();
		for (int i = 0; i < 3; i++) {
			this.alphas.add(1f); 
		}
		//3 total events
		*/
		Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/helper13.png"));
		this.helperSprite = new Sprite(text, 0, 0, 160, 144);
		
	}
}


class catchPokemon_wiggles1Time extends Action {
	
	ArrayList<Vector2> positions;
	Vector2 position;
	ArrayList<Sprite> sprites;
	Sprite sprite;
	ArrayList<Integer> repeats;
	String sound;
	ArrayList<String> sounds;

	ArrayList<Float> alphas;
	
	public int layer = 120;
	public int getLayer(){return this.layer;}

	public String getCamera() {return "gui";};

	Action nextAction;
	
	Sprite helperSprite; //just for helping me position the animation. delete later.
	
	@Override
	public void step(PkmnGen game) {

		//set sprite position
		//if done with anim, do nextAction
		if (positions.isEmpty() || sprites.isEmpty()) {
				
			String textString = "Darn! The POKéMON broke free!";
			//nextAction will be a battle menu draw action here
			PublicFunctions.insertToAS(game, new DisplayText(game, textString, null, null, this.nextAction));
			//newAction.step(game);//need to draw the pokeball
			
			game.actionStack.remove(this);
			return;
		}
		
		//control alpha of opposing pkmn
		float currAlpha = this.alphas.get(0);
		game.battle.oppPokemon.sprite.setAlpha(currAlpha);	
		
		//get next sound, play it
		this.sound = this.sounds.get(0);
		if (this.sound != null) {
			PublicFunctions.insertToAS(game, new PlaySound(this.sound, new DoneAction()));
			this.sounds.set(0, null); //don't play same sound over again
		}
		
		
		//get next frame
		this.sprite = sprites.get(0);
		
		//debug
		//this.helperSprite.draw(game.floatingBatch); //debug
		
		//draw current sprite
		if (this.sprite != null) {
			//this.sprite.setScale(3); //post scaling change
			this.sprite.setPosition(position.x, position.y);
			this.sprite.draw(game.floatingBatch);			
		}
		
		//debug
//		if (this.repeats.size() == 1) { //debug
//			return;
//		}
		
		//repeat sprite/pos for current object for 'frames[0]' number of frames.
		if (this.repeats.get(0) > 0) {
			this.repeats.set(0, this.repeats.get(0) - 1);
		}
		else {
			//since position is relative, only update once each time period
			this.position = this.position.add(positions.get(0));
			positions.remove(0);
			sprites.remove(0);
			repeats.remove(0);
			sounds.remove(0);
			alphas.remove(0);
		}
	}
	
	public catchPokemon_wiggles1Time(PkmnGen game, Action nextAction) {

		this.nextAction = nextAction;
		
		Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball1.png"));
		
		//initial sprite position
		this.position = new Vector2(114,88); //post scaling change
		
		this.positions = new ArrayList<Vector2>();
		this.positions.add(new Vector2(0,0)); //wait 13
		this.positions.add(new Vector2(-1,0));
		this.positions.add(new Vector2(1,0));
		this.positions.add(new Vector2(1,0));
		this.positions.add(new Vector2(-19,-16));
		for (int i = 0; i < 6; i++) {
			this.positions.add(new Vector2(0,0)); //filler
		}
		//11 total
		
		//wiggle anim
		this.sprites =  new ArrayList<Sprite>();
		this.sprites.add(null); //draw nothing for 13 frames
		
		text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball_wiggleSheet1.png"));
		this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
		this.sprites.add(new Sprite(text, 12*1, 0, 12, 12)); //left tilt
		this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
		this.sprites.add(new Sprite(text, 12*2, 0, 12, 12)); //right tilt
		text = new Texture(Gdx.files.internal("throw_pokeball_anim/poof_sheet1.png"));
		this.sprites.add(new Sprite(text, 48*0, 0, 48, 48)); //poof1 
		this.sprites.add(new Sprite(text, 48*1, 0, 48, 48)); //poof2
		this.sprites.add(new Sprite(text, 48*2, 0, 48, 48)); //poof3
		this.sprites.add(new Sprite(text, 48*3, 0, 48, 48)); //poof4
		this.sprites.add(new Sprite(text, 48*4, 0, 48, 48)); //poof5 
		this.sprites.add(null); //draw nothing for 4 frames //done at this point 
		//11 total events
		
		
		
		this.repeats = new ArrayList<Integer>();
		
		this.repeats.add(13-1); //13 frames before pokeball appears on ground

		this.repeats.add(44-1); //first middle sprite has 44 frames
		for (int i = 0; i < 2; i++) { //left-middle-right tilt all have 4 frames
			this.repeats.add(4-1);
		}
		this.repeats.add(14-1); //final right tilt has 14 frames
		for (int i = 0; i < 4; i++) { //each poof has 5 frames
			this.repeats.add(5-1);
		}
		this.repeats.add(10-1); //final poof has 10 frames
		this.repeats.add(4-1); //4 frames of nothing (before text box change)
		//11 total
		
		//sounds to play
		this.sounds = new ArrayList<String>();
		this.sounds.add("pokeball_wiggle1"); //wiggle as ball appears (doing as white appears, so it's uniform)
		for (int i = 0; i < 4; i++) {
			this.sounds.add(null); 
		}
		this.sounds.add("poof1");
		for (int i = 0; i < 5; i++) {
			this.sounds.add(null); 
		}
		//11 total events
		
		//opposing pkmn sprite alphas
		this.alphas = new ArrayList<Float>();
		for (int i = 0; i < 10; i++) {
			this.alphas.add(0f); 
		}
		this.alphas.add(1f); 
		//11 total events
		
		text = new Texture(Gdx.files.internal("throw_pokeball_anim/helper13.png"));
		this.helperSprite = new Sprite(text, 0, 0, 160, 144);
		//this.helperSprite.setPosition(16*10,16*9); //post scaling change
		//this.helperSprite.setScale(3);
		
	}
}


class catchPokemon_wiggles2Times extends Action {

	//Sprite pokeballSprite;
	
	ArrayList<Vector2> positions;
	Vector2 position;
	ArrayList<Sprite> sprites;
	Sprite sprite;
	ArrayList<Integer> repeats;
	String sound;
	ArrayList<String> sounds;

	ArrayList<Float> alphas;
	
	public int layer = 120;
	public int getLayer(){return this.layer;}

	public String getCamera() {return "gui";};

	Action nextAction;
	
	Sprite helperSprite; //just for helping me position the animation. delete later.
	
	@Override
	public void step(PkmnGen game) {

		//set opp pokemon sprite alpha
		//game.battle.oppPokemon.sprite.setAlpha(0); //delete at some point
		
		//set sprite position
		//if done with anim, do nextAction
		if (positions.isEmpty() || sprites.isEmpty()) {
				
			String textString = "Aww! It appeared to be caught!";
			//nextAction will be a battle menu draw action here
			PublicFunctions.insertToAS(game, new DisplayText(game, textString, null, null, this.nextAction));
			//newAction.step(game);//need to draw the pokeball
			
			game.actionStack.remove(this);
			return;
		}
		
		//control alpha of opposing pkmn
		float currAlpha = this.alphas.get(0);
		game.battle.oppPokemon.sprite.setAlpha(currAlpha);	
		
		//get next sound, play it
		this.sound = this.sounds.get(0);
		if (this.sound != null) {
			PublicFunctions.insertToAS(game, new PlaySound(this.sound, new DoneAction()));
			this.sounds.set(0, null); //don't play same sound over again
		}
		
		
		//get next frame
		this.sprite = sprites.get(0);
		
		//debug
		//this.helperSprite.draw(game.floatingBatch); //debug
		
		//draw current sprite
		if (this.sprite != null) {
			//this.sprite.setScale(3); //post scaling change
			this.sprite.setPosition(position.x, position.y);
			this.sprite.draw(game.floatingBatch);			
		}
		
		//debug
//		if (this.repeats.size() == 1) { //debug
//			return;
//		}
		
		//repeat sprite/pos for current object for 'frames[0]' number of frames.
		if (this.repeats.get(0) > 0) {
			this.repeats.set(0, this.repeats.get(0) - 1);
		}
		else {
			//since position is relative, only update once each time period
			this.position = this.position.add(positions.get(0));
			positions.remove(0);
			sprites.remove(0);
			repeats.remove(0);
			sounds.remove(0);
			alphas.remove(0);
		}
	}
	
	public catchPokemon_wiggles2Times(PkmnGen game, Action nextAction) {

		this.nextAction = nextAction;
		
		Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball1.png"));
		
		//initial sprite position
		this.position = new Vector2(114,88); //post scaling change
		
		this.positions = new ArrayList<Vector2>();
		this.positions.add(new Vector2(0,0)); //wait 13
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
			this.positions.add(new Vector2(0,0)); //filler
		}
		//15 total
		
		//wiggle anim
		this.sprites =  new ArrayList<Sprite>();
		this.sprites.add(null); //draw nothing for 13 frames
		
		text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball_wiggleSheet1.png"));
		this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
		this.sprites.add(new Sprite(text, 12*1, 0, 12, 12)); //left tilt
		this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
		this.sprites.add(new Sprite(text, 12*2, 0, 12, 12)); //right tilt
		this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
		this.sprites.add(new Sprite(text, 12*1, 0, 12, 12)); //left tilt
		this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
		this.sprites.add(new Sprite(text, 12*2, 0, 12, 12)); //right tilt
		text = new Texture(Gdx.files.internal("throw_pokeball_anim/poof_sheet1.png"));
		this.sprites.add(new Sprite(text, 48*0, 0, 48, 48)); //poof1 
		this.sprites.add(new Sprite(text, 48*1, 0, 48, 48)); //poof2
		this.sprites.add(new Sprite(text, 48*2, 0, 48, 48)); //poof3
		this.sprites.add(new Sprite(text, 48*3, 0, 48, 48)); //poof4
		this.sprites.add(new Sprite(text, 48*4, 0, 48, 48)); //poof5 
		this.sprites.add(null); //draw nothing for 4 frames //done at this point 
		//15 total events
		
		
		
		this.repeats = new ArrayList<Integer>();
		
		this.repeats.add(13-1); //13 frames before pokeball appears on ground

		this.repeats.add(44-1); //first middle sprite has 44 frames
		for (int i = 0; i < 3; i++) { //left-middle-right tilt all have 4 frames
			this.repeats.add(4-1);
		}
		this.repeats.add(44-1); //first middle sprite has 44 frames
		for (int i = 0; i < 2; i++) { //left-middle-right tilt all have 4 frames
			this.repeats.add(4-1);
		}
		this.repeats.add(14-1); //final right tilt has 14 frames
		for (int i = 0; i < 4; i++) { //each poof has 5 frames
			this.repeats.add(5-1);
		}
		this.repeats.add(10-1); //final poof has 10 frames
		this.repeats.add(4-1); //4 frames of nothing (before text box change)
		//15 total
		
		//sounds to play
		this.sounds = new ArrayList<String>();
		//this.sounds.add(null); //wait 13 no sound
		this.sounds.add("pokeball_wiggle1"); //wiggle as ball appears (doing as white appears, so it's uniform)
		this.sounds.add(null);
		this.sounds.add("pokeball_wiggle1");
		for (int i = 0; i < 6; i++) {
			this.sounds.add(null); 
		}
		this.sounds.add("poof1");
		for (int i = 0; i < 5; i++) {
			this.sounds.add(null); 
		}
		//15 total events
		
		//opposing pkmn sprite alphas
		this.alphas = new ArrayList<Float>();
		for (int i = 0; i < 14; i++) {
			this.alphas.add(0f); 
		}
		this.alphas.add(1f); 
		//15 total events
		
		text = new Texture(Gdx.files.internal("throw_pokeball_anim/helper13.png"));
		this.helperSprite = new Sprite(text, 0, 0, 160, 144);
		//this.helperSprite.setPosition(16*10,16*9); //post scaling change
		//this.helperSprite.setScale(3);
		
	}
}



//draws pokeball wiggling3 times and then releasing pokemon
 //still need to get wiggle animation done here
class catchPokemon_wiggles3Times extends Action {

	//Sprite pokeballSprite;
	
	ArrayList<Vector2> positions;
	Vector2 position;
	ArrayList<Sprite> sprites;
	Sprite sprite;
	ArrayList<Integer> repeats;
	String sound;
	ArrayList<String> sounds;

	ArrayList<Float> alphas;
	
	public int layer = 120;
	public int getLayer(){return this.layer;}

	public String getCamera() {return "gui";};

	Action nextAction;
	
	Sprite helperSprite; //just for helping me position the animation. delete later.
	
	@Override
	public void step(PkmnGen game) {

		//set opp pokemon sprite alpha
		//game.battle.oppPokemon.sprite.setAlpha(0); //delete at some point
		
		//set sprite position
		//if done with anim, do nextAction
		if (positions.isEmpty() || sprites.isEmpty()) {

			//PokemonCaught_Events - sprite and text
			//new DisplayText(game, string)
//			Action newAction = new PokemonCaught_Events(
//									game, new SplitAction(
//										new BattleFadeOut(game, 
//												new playerStanding(game)
//										),
//										new BattleFadeOutMusic(game, new DoneAction())
//									)
//								);

				
			//String textString = "Darn! The POKeMON broke free!";
			String textString = "Shoot! It was so close, too!";
			//nextAction will be a battle menu draw action here
			PublicFunctions.insertToAS(game, new DisplayText(game, textString, null, null, this.nextAction));
			//newAction.step(game);//need to draw the pokeball

			//since pkmn was caught, add to players pokemon
			//game.player.pokemon.add(game.battle.oppPokemon);
			
			game.actionStack.remove(this);
			return;
		}
		
		//control alpha of opposing pkmn
		float currAlpha = this.alphas.get(0);
		game.battle.oppPokemon.sprite.setAlpha(currAlpha);	
		
		//get next sound, play it
		this.sound = this.sounds.get(0);
		if (this.sound != null) {
			PublicFunctions.insertToAS(game, new PlaySound(this.sound, new DoneAction()));
			this.sounds.set(0, null); //don't play same sound over again
		}
		
		
		//get next frame
		this.sprite = sprites.get(0);
		
		//debug
		//this.helperSprite.draw(game.floatingBatch); //debug
		
		//draw current sprite
		if (this.sprite != null) {
			//this.sprite.setScale(3); //post scaling change
			this.sprite.setPosition(position.x, position.y);
			this.sprite.draw(game.floatingBatch);			
		}
		
		//debug
//		if (this.repeats.size() == 6) { //debug
//			return;
//		}
		
		//repeat sprite/pos for current object for 'frames[0]' number of frames.
		if (this.repeats.get(0) > 0) {
			this.repeats.set(0, this.repeats.get(0) - 1);
		}
		else {
			//since position is relative, only update once each time period
			this.position = this.position.add(positions.get(0));
			positions.remove(0);
			sprites.remove(0);
			repeats.remove(0);
			sounds.remove(0);
			alphas.remove(0);
		}
	}
	
	public catchPokemon_wiggles3Times(PkmnGen game, Action nextAction) {

		//TODO - would be nice to confirm this version with a recording
		
		this.nextAction = nextAction;
		
		Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball1.png"));
		
		//initial sprite position
		this.position = new Vector2(114,88); //post scaling change
		
		this.positions = new ArrayList<Vector2>();
		this.positions.add(new Vector2(0,0)); //wait 13
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
			this.positions.add(new Vector2(0,0)); //filler
		}
		//19 total
		
		//wiggle anim
		this.sprites =  new ArrayList<Sprite>();
		this.sprites.add(null); //draw nothing for 13 frames
		
		text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball_wiggleSheet1.png"));
		this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
		this.sprites.add(new Sprite(text, 12*1, 0, 12, 12)); //left tilt
		this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
		this.sprites.add(new Sprite(text, 12*2, 0, 12, 12)); //right tilt
		this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
		this.sprites.add(new Sprite(text, 12*1, 0, 12, 12)); //left tilt
		this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
		this.sprites.add(new Sprite(text, 12*2, 0, 12, 12)); //right tilt
		this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
		this.sprites.add(new Sprite(text, 12*1, 0, 12, 12)); //left tilt
		this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
		this.sprites.add(new Sprite(text, 12*2, 0, 12, 12)); //right tilt 
		text = new Texture(Gdx.files.internal("throw_pokeball_anim/poof_sheet1.png"));
		this.sprites.add(new Sprite(text, 48*0, 0, 48, 48)); //poof1 
		this.sprites.add(new Sprite(text, 48*1, 0, 48, 48)); //poof2
		this.sprites.add(new Sprite(text, 48*2, 0, 48, 48)); //poof3
		this.sprites.add(new Sprite(text, 48*3, 0, 48, 48)); //poof4
		this.sprites.add(new Sprite(text, 48*4, 0, 48, 48)); //poof5 
		this.sprites.add(null); //draw nothing for 4 frames //done at this point //18 total events
		
		
		this.repeats = new ArrayList<Integer>();
		
		this.repeats.add(13-1); //13 frames before pokeball appears on ground

		this.repeats.add(44-1); //first middle sprite has 44 frames
		for (int i = 0; i < 3; i++) { //left-middle-right tilt all have 4 frames
			this.repeats.add(4-1);
		}
		this.repeats.add(44-1); //first middle sprite has 44 frames
		for (int i = 0; i < 3; i++) { //left-middle-right tilt all have 4 frames
			this.repeats.add(4-1);
		}
		this.repeats.add(44-1); //first middle sprite has 44 frames
		for (int i = 0; i < 2; i++) { //left-middle tilt both have 4 frames
			this.repeats.add(4-1);
		}
		this.repeats.add(14-1); //final right tilt has 14 frames
		for (int i = 0; i < 4; i++) { //each poof has 5 frames
			this.repeats.add(5-1);
		}
		this.repeats.add(10-1); //final poof has 10 frames
		this.repeats.add(4-1); //4 frames of nothing (before text box change)
		//19 total
		
		//sounds to play
		 //TODO - wiggle sound
		this.sounds = new ArrayList<String>();
		//this.sounds.add(null); //wait 13 no sound
		this.sounds.add("pokeball_wiggle1"); //wiggle as ball appears (doing as white appears, so it's uniform)
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
		//this.sounds.add("pokeball_wiggle1"); //this is in 3 wiggle?
//		for (int i = 0; i < 3; i++) {
//			this.sounds.add(null); 
//		}
		
		//opposing pkmn sprite alphas
		this.alphas = new ArrayList<Float>();
		for (int i = 0; i < 18; i++) {
			this.alphas.add(0f); 
		}
		this.alphas.add(1f); 
		//19 total events
		
		text = new Texture(Gdx.files.internal("throw_pokeball_anim/helper13.png"));
		this.helperSprite = new Sprite(text, 0, 0, 160, 144);
		//this.helperSprite.setPosition(16*10,16*9); //post scaling change
		//this.helperSprite.setScale(3);
		
	}
}





class PrintAngryEating extends Action {

	public int layer = 120;
	public int getLayer(){return this.layer;}

	public String getCamera() {return "gui";};
	
	Action nextAction;
	
	@Override
	public void step(PkmnGen game) {
		
		if (game.battle.oppPokemon.angry > 0) {
			String textString = "Wild " + game.battle.oppPokemon.name.toUpperCase() + " is angry!";
			PublicFunctions.insertToAS(game, new DisplayText(game, textString, null, null, this.nextAction));
			game.actionStack.remove(this);
			
			game.battle.oppPokemon.angry--;
			//If the angry counter is decreased to zero this way, 
			 //the Pokémon's catch rate will also be reset to its initial catch rate,
			 //regardless of how it has been modified in the battle before this point;
			if (game.battle.oppPokemon.angry <= 0) {
				int baseCatchRate = game.battle.oppPokemon.baseStats.get("catchRate"); //or maxStats?
				game.battle.oppPokemon.currentStats.put("catchRate", baseCatchRate);
			}
			return;
		}
		else if (game.battle.oppPokemon.eating > 0) {
			String textString = "Wild " + game.battle.oppPokemon.name.toUpperCase() + " is eating!";
			PublicFunctions.insertToAS(game, new DisplayText(game, textString, null, null, this.nextAction));
			game.actionStack.remove(this);
			
			game.battle.oppPokemon.eating--;
			return;
		}

		//made it here, so not eating or angry
		PublicFunctions.insertToAS(game, this.nextAction);
		game.actionStack.remove(this);
		return;
	}

	public PrintAngryEating(PkmnGen game, Action nextAction) {
		
		this.nextAction = nextAction;
		
	}
	
}


class ChanceToRun extends Action {

	public int layer = 120;
	public int getLayer(){return this.layer;}

	public String getCamera() {return "gui";};
	
	Action nextAction;
	

	@Override
	public void step(PkmnGen game) {
	
		//TODO - uses up one frame here when it shouldn't.
		 //call nextAction step? but layer will be wrong
		 //ignoring for now

		
		//can't make this a function in drawMenu, because
		 //i need this calculation to happen after angry counters have been modified
		int x = game.battle.oppPokemon.currentStats.get("speed") % 256; //current speed or max speed?
		x = x*2;

		System.out.println("Chance to run, x: " + String.valueOf(x));
		
		if (x > 255) {
			//pokemon runs
			String textString = "Wild " + game.battle.oppPokemon.name.toUpperCase() + " ran!";
			PublicFunctions.insertToAS(game, 
						new DisplayText(game, textString, null, null,
							new SplitAction(
								new OppPokemonFlee(game, 
									new SplitAction(
										new BattleFadeOut(game,
											new DoneAction()), //new playerStanding(game)),
										new BattleFadeOutMusic(game, new DoneAction())
									)
								),
								new WaitFrames(game, 8, new PlaySound("run1", new DoneAction())) //8 frames seems right
							)
						)
					);
			//demo code - reset adrenaline
			game.player.adrenaline = 0;
			//
			game.actionStack.remove(this);
			return;
		}
		if (game.battle.oppPokemon.angry > 0) {
			x = x*2;
			if (x > 255) { //capped at 255
				x = 255;
			}
		}
		else if (game.battle.oppPokemon.eating > 0) {
			x = x/4;
		}
		int r = game.map.rand.nextInt(255+1); //+1 to include upper bound
		if (r < x) {
			//pokemon runs
			String textString = "Wild " + game.battle.oppPokemon.name.toUpperCase() + " ran!";
			PublicFunctions.insertToAS(game, 
						new DisplayText(game, textString, null, null,
							new SplitAction(
								new OppPokemonFlee(game, 
									new SplitAction(
										new BattleFadeOut(game,
											new DoneAction()), //new playerStanding(game)),
										new BattleFadeOutMusic(game, new DoneAction())
									)
								),
								new WaitFrames(game, 8, new PlaySound("run1", new DoneAction())) 
							)
						)
					);
			//demo code - reset adrenaline
			game.player.adrenaline = 0;
			//
			//PublicFunctions.insertToAS(game, new PlaySound("click1", new PlaySound("run1", new DoneAction())));
			game.actionStack.remove(this);
			return;
		}
				
		//pokemon doesn't run
		//insert nextAction to actionstack
		PublicFunctions.insertToAS(game, this.nextAction); //usually menu draw action
		game.actionStack.remove(this);
		
	}
	

	public ChanceToRun(PkmnGen game, Action nextAction) {

		this.nextAction = nextAction;
		
		//  - if run chance succeeds, then print 'ran away' text and exit battle actions
		//  - if failed, go to menuAction (nextAction)
		
		//experiment - add angry/eating print to AS?
		//automatically adds 'print angry/eating to AS'
		/*
		PublicFunctions.insertToAS(game, new PrintAngryEating(game, this)); //usually menu draw action
		game.actionStack.remove(this);
		*/ //didn't do b/c confusing. can use this in place of series' tho
		
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
	public int getLayer(){return this.layer;}

	public String getCamera() {return "gui";};

	Action nextAction;
	
	Sprite helperSprite; //just for helping me position the animation. delete later.
	
	@Override
	public void step(PkmnGen game) {

		//set opp pokemon sprite alpha
		//game.battle.oppPokemon.sprite.setAlpha(0); //delete at some point
		
		//set sprite position
		//if done with anim, do nextAction
		if (positions.isEmpty()) {
				
			PublicFunctions.insertToAS(game, this.nextAction);			
			game.actionStack.remove(this);
			return;
		}
		
		//get next sound, play it
		this.sound = this.sounds.get(0);
		if (this.sound != null) {
			PublicFunctions.insertToAS(game, new PlaySound(this.sound, new DoneAction()));
			this.sounds.set(0, null); //don't play same sound over again
		}
		
		
		//get next frame
		//this.sprite = sprites.get(0); //not using
		
		//debug
		//this.helperSprite.draw(game.floatingBatch); //debug


		//this.sprite.setScale(3); //post scaling change
		game.battle.oppPokemon.sprite.setPosition(position.x, position.y);
		game.battle.oppPokemon.sprite.draw(game.floatingBatch);	
		
		
		//debug
//		if (this.repeats.size() == 1) { //debug
//			return;
//		}
		
		//repeat sprite/pos for current object for 'frames[0]' number of frames.
		if (this.repeats.get(0) > 0) {
			this.repeats.set(0, this.repeats.get(0) - 1);
		}
		else {
			//since position is relative, only update once each time period
			this.position = this.position.add(positions.get(0));
			positions.remove(0);
			repeats.remove(0);
			sounds.remove(0);
		}
	}
	
	public OppPokemonFlee(PkmnGen game, Action nextAction) {

		this.nextAction = nextAction;
		
		//Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball1.png"));
		
		//initial oppPokemon sprite position
		this.position = new Vector2(game.battle.oppPokemon.sprite.getX(),game.battle.oppPokemon.sprite.getY()); 
		
		this.positions = new ArrayList<Vector2>();
		this.positions.add(new Vector2(0,0)); //wait 16 frames
		for (int i = 0; i < 13; i++) { //move 8 pixels to the right every 3 frames
			this.positions.add(new Vector2(8,0));
		}
		//14 total


		this.repeats = new ArrayList<Integer>();
		
		this.repeats.add(16-1); //16 frames of nothing
		for (int i = 0; i < 13; i++) {
			this.repeats.add(3-1); //3 frames each movement
		}
		//14 total
		
		//sounds to play //nothing?
		this.sounds = new ArrayList<String>();
		for (int i = 0; i < 14; i++) {
			this.sounds.add(null); 
		}
		//14 total events


		//text = new Texture(Gdx.files.internal("throw_pokeball_anim/helper13.png"));
		//this.helperSprite = new Sprite(text, 0, 0, 160, 144);
		//this.helperSprite.setPosition(16*10,16*9); //post scaling change
		//this.helperSprite.setScale(3);
		
	}
}


//displays caught pkmn text
//plays victory fanfare
//continues to draw pokeball on screen
class PokemonCaught_Events extends Action {

	Action displayTextAction;
	
	Sprite pokeballSprite;
	
	public int layer = 120;
	public int getLayer(){return this.layer;}

	public String getCamera() {return "gui";};
	
	Action nextAction;
	
	boolean startLooking;
	
	//what to do at each iteration
	public void step(PkmnGen game) {


		this.pokeballSprite.draw(game.floatingBatch);
		
		//when text action first appears, start checking for when it leaves AS
		if (this.startLooking == false && game.actionStack.contains(this.displayTextAction)) {
			this.startLooking = true;
			return; //won't have left AS on this first iteration, might as well return
		}
		
		//for now, if displayTextAction not in AS, remove and return
		if (!game.actionStack.contains(this.displayTextAction) && this.startLooking == true) {
			//set oppPokemon alpha back to normal
			game.battle.oppPokemon.sprite.setAlpha(1);
			
			PublicFunctions.insertToAS(game, this.nextAction);
			game.actionStack.remove(this);
			//stop drawing the battle
			game.actionStack.remove(game.battle.drawAction); //stop drawing the battle
			game.battle.drawAction = null; //essentially using this like a flag

			//alternative is to have drawAction check for boolean to not exist, and remove itself if flag not set
			return;
		}
		
	}

	public PokemonCaught_Events(PkmnGen game, int adrenaline, Action nextAction) {
		
		this.nextAction = nextAction;
		
		this.startLooking = false;

		//step through DisplayText until finished 
		//String string1 = game.battle.oppPokemon.name.toUpperCase()+" was    caught!";
		String string1 = "All right! "+game.battle.oppPokemon.name.toUpperCase()+" was caught!";
		//demo code
		String string2 = ""+game.player.name+" gained "+Character.forDigit(adrenaline,10)+" ADRENALINE!"; 

		this.displayTextAction  = new DisplayText(game, string2,  null, null, new DoneAction());
		Action firstTextAction = new DisplayText(game, string1, "fanfare1", null, this.displayTextAction); //trigger
				
		PublicFunctions.insertToAS(game, firstTextAction);
		//
		
		//
		Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball_wiggleSheet1.png"));
		this.pokeballSprite = new Sprite(text, 12*2, 0, 12, 12); //right tilt
		this.pokeballSprite.setPosition(115,88);
		//this.pokeballSprite.setScale(3); //post scaling change

		
		
		
	}
	
}



//poof animation 
//and algorithm that will show pkmn extending outwards
class ThrowOutPokemon extends Action {

	Sprite pokeballSprite;
	
	ArrayList<Vector2> positions;
	Vector2 position;
	ArrayList<Sprite[][]> sprites;
	Sprite[][] sprite;
	ArrayList<Integer> repeats;
	ArrayList<String> sounds;
	String sound;
	
	public int layer = 109;
	public int getLayer(){return this.layer;}

	public String getCamera() {return "gui";};

	Action nextAction;
	
	Sprite helperSprite; //just for helping me position the animation. delete later.
	
	boolean doneYet;
	
	@Override
	public void step(PkmnGen game) {

		//get next frame
		this.sprite = sprites.get(0);

		//debug
//		this.helperSprite.draw(game.floatingBatch); 
		
		//set sprite position
		//if done with anim, do nextAction
		if (positions.isEmpty() || sprites.isEmpty()) {


			//debug
//			if (this.repeats.size() == 0) { 
//				return;
//			}
			
			if (!this.doneYet) {
				PublicFunctions.insertToAS(game, this.nextAction);
				for (int i = 0; i < this.sprite.length; i++) {
					for (int j = 0; j < this.sprite[i].length; j++) {
						this.sprite[i][j].setPosition(position.x, position.y);
					}
				}
				this.doneYet = true;
			}
			

			for (int i = 0; i < this.sprite.length; i++) {
				for (int j = 0; j < this.sprite[i].length; j++) {
					this.sprite[i][j].draw(game.floatingBatch);
				}
			}
			
			
			if (game.battle.drawAction == null) {
				game.actionStack.remove(this);
			}
			return;
		}
				
		
		
		//draw current sprite
		if (this.sprite != null) {
			for (int i = 0; i < this.sprite.length; i++) {
				for (int j = 0; j < this.sprite[i].length; j++) {
					if (this.sprite[i][j] != null) {
						this.sprite[i][j].setPosition(position.x+8*i, position.y+8*j);
						this.sprite[i][j].draw(game.floatingBatch);	
					}
				}
			}
		}
		
		//debug
//		if (this.repeats.size() == 6) { 
//			return;
//		}

		//get next sound, play it
		this.sound = this.sounds.get(0);
		if (this.sound != null) {
			PublicFunctions.insertToAS(game, new PlaySound(this.sound, new DoneAction()));
			this.sounds.set(0, null); //don't play same sound over again
		}
		
		
		//repeat sprite/pos for current object for 'frames[0]' number of frames.
		if (this.repeats.get(0) > 1) {
			this.repeats.set(0, this.repeats.get(0) - 1);
		}
		else {
			//since position is relative, only update once each time period
			this.position = this.position.add(positions.get(0));
			positions.remove(0);
			sprites.remove(0);
			repeats.remove(0);
			sounds.remove(0);
		}
	}
	
	public ThrowOutPokemon(PkmnGen game, Action nextAction) {

		
		this.doneYet = false;
		this.nextAction = nextAction;

		//consider doing relative positions from now on
		//this.position = new Vector2(104+4*3-2,200-6*3-2); //post scaling change
		this.position = new Vector2(16,32);
		
		this.positions = new ArrayList<Vector2>();
		this.positions.add(new Vector2(0,0));//first of poof anim
		this.positions.add(new Vector2(0,0));
		this.positions.add(new Vector2(0,0));
		this.positions.add(new Vector2(0,0));
		this.positions.add(new Vector2(0,0));
		this.positions.add(new Vector2(0,0)); //1 empty frame
		this.positions.add(new Vector2(18-24,18)); //first of pokemon expand anim
		this.positions.add(new Vector2(0,0));
		this.positions.add(new Vector2(0,0));
		this.positions.add(new Vector2(24-18+6,18-6)); //last is always dummy pos (?need anymore?)
		
		
		this.sprites =  new ArrayList<Sprite[][]>();
		this.sprites.add(null); //draw nothing for 40 frames
		//'poof' animation 
		Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/poof_sheet1.png"));
		this.sprites.add(new Sprite[][]{new Sprite[]{new Sprite(text, 48*0, 0, 48, 48)}}); 
		this.sprites.add(new Sprite[][]{new Sprite[]{new Sprite(text, 48*1, 0, 48, 48)}}); 
		this.sprites.add(new Sprite[][]{new Sprite[]{new Sprite(text, 48*2, 0, 48, 48)}}); 
		this.sprites.add(new Sprite[][]{new Sprite[]{new Sprite(text, 48*3, 0, 48, 48)}}); 
		this.sprites.add(new Sprite[][]{new Sprite[]{new Sprite(text, 48*4, 0, 48, 48)}}); 

		this.sprites.add(null); //draw nothing for 1 frame
		
		//below code will splice up image to create 'expanding' effect
		
//		 7 blocks of 4 sprites each, 4th block is 'middle'
//		 working backwards
//		 frame 4to3 - x: remove cols 3 and 5 , y: remove rows 3 and 5
//		 frame 3to2: x: remove cols 3 and 5  , y: remove rows 2 and 4
//		 frame 2to1: x: remove cols 3 and 5  , y: remove rows 1 and 2 - no
//		  - cloyster ends up with 5,7 as initial sprite
		
		//3 frames release1
		Sprite temp = new Sprite(game.player.currPokemon.backSprite);
		TextureRegion[][] tempRegion = temp.split(4, 4); //should be 7x7
		
		//
		Sprite[][] temp2 = new Sprite[7][7];
		for (int i = 0; i < tempRegion.length; i++) {
			for (int j = 0; j < tempRegion[i].length; j++) {
				temp2[i][j] = new Sprite(tempRegion[6-j][i]);
				temp2[i][j].setScale(2);
//				System.out.println("sprite size: "+String.valueOf(temp2[i][j].getHeight())+"  "+String.valueOf(temp2[i][j].getHeight()));
			}
		}
		
		//remove rows 3 and 5
		for (int i = 0; i < temp2.length; i++) {
			temp2[i][2] = temp2[i][3];
			temp2[i][3] = temp2[i][5];
			temp2[i][4] = temp2[i][6];
			temp2[i][5] = null;
			temp2[i][6] = null;
		}
		//remove cols 3 and 5
		temp2[2] = temp2[1];
		temp2[1] = temp2[0];
		temp2[0] = new Sprite[]{};
		temp2[4] = temp2[5];
		temp2[5] = temp2[6];
		temp2[6] = new Sprite[]{};
		//i=0, j=6 -> i=6, j=0
		//i=0, j=0 -> i=6, j=6
		
		//copy temp 2
		Sprite[][] temp3 = new Sprite[7][7]; //.clone();
		for (int i = 0; i < temp2.length; i++) {
			for (int j = 0; j < temp2[i].length; j++) {
				temp3[i][j] = temp2[i][j];
			}
		}
		
		//remove rows 2 and 4
		for (int i = 0; i < temp3.length; i++) {
			if (temp3[i].length <= 0) {
				continue;
			}
			temp3[i][1] = temp3[i][2];
			temp3[i][2] = temp3[i][4];
			temp3[i][3] = null;
			temp3[i][4] = null;
		}
		//remove cols 3 and 5
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
		//3 frames release2
		//6 frames release3
		
		// -1?
		this.repeats = new ArrayList<Integer>();
		this.repeats.add(40);
		for (int i = 0; i < 4; i++) {
			this.repeats.add(5);
		}
		this.repeats.add(10); //last is 10 frames
		this.repeats.add(1); //last is 10 frames
		this.repeats.add(3);
		this.repeats.add(3);
		this.repeats.add(6);


		

		this.sounds = new ArrayList<String>();
		this.sounds.add(null); //
		this.sounds.add("poof1");
		for (int i = 0; i < 9; i++) {
			this.sounds.add(null);
		}
		
		
		text = new Texture(Gdx.files.internal("pokemon_throw_out_anim/helper5.png"));
		this.helperSprite = new Sprite(text, 0, 0, 160, 144);
		//this.helperSprite.setPosition(16*10,16*9); //post scaling change
		//this.helperSprite.setScale(3); //post scaling change

		//play 'throw pokeball' sound
		//PublicFunctions.insertToAS(game, new PlaySound("throw_pokeball1", new DoneAction()));
		
		
	}
}



//return the type of battle the the player must enter
 //safari battle (no pokemon),
 //throw out first pkmn

class Battle_Actions extends Action {
	
	
	public static Action getAttackAction(PkmnGen game, String attackName, Action nextAction) {
		
		//construct default attack?
		
		
		int power = 40;
		int accuracy = 100;
		
		if (attackName.equals("Aurora Beam")) {
			//normally return new attack here
			power = 65; accuracy = 100;
		}
		else if (attackName.equals("Clamp")) {
			//normally return new attack here
			power = 35; accuracy = 85;
		}
		else if (attackName.equals("Supersonic")) {
			//normally return new attack here
			power = 0; accuracy = 55;
		}
		else if (attackName.equals("Withdraw")) {
			//normally return new attack here
			power = 20; accuracy = 100;
		}
		
		
		
		return new DefaultAttack(game, power, accuracy, nextAction);
		
	}
	
	
	

	public static Action get(PkmnGen game) {
		
		//if player has no pokemon, encounter is safari zone style
		if (game.player.pokemon.isEmpty()) {

			return new SplitAction(
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
				);
		}
		//throw out first pokemon in player.pokemon
		else {
			
			Action triggerAction = new PlaySound(game.player.currPokemon.name, 
													new WaitFrames(game, 6,
														new DrawBattleMenu_Normal(game, new DoneAction())
													)
												);
			
			return new BattleIntro(
						new BattleIntro_anim1(
							new SplitAction(
								new DrawBattle(game),
								new BattleAnim_positionPlayers(game, 
									new PlaySound(game.battle.oppPokemon.name, 
										new DisplayText(game, "Wild "+game.battle.oppPokemon.name.toUpperCase()+" appeared!", null, null, 
											new SplitAction(
												new WaitFrames(game, 1,
														new DrawEnemyHealth(game)
												),
												new WaitFrames(game, 39,
													new MovePlayerOffScreen(game, 
														new DisplayText(game, "Go! "+game.player.currPokemon.name.toUpperCase()+"!", null, triggerAction, 
															new ThrowOutPokemon(game, //this draws pkmn sprite permanently until battle ends, ie until game.battle.drawAction == null
																triggerAction
															)
														)
													)
												)
											)
										)
									)
								)
							)
						)
					);
		}
		
		//game.actionStack.remove(this);
		
	}
		
}


//basically 'tackle' attack, with modified power/accuracy
class DefaultAttack extends Action {
	

	ArrayList<Vector2> positions;
	Vector2 position;
	ArrayList<Sprite> sprites;
	Sprite sprite;
	ArrayList<Integer> repeats;
	ArrayList<Float> alphas;
	ArrayList<String> sounds;
	String sound;
	
	public int layer = 120;
	public int getLayer(){return this.layer;}

	public String getCamera() {return "gui";};

	Action nextAction;
	
	Sprite helperSprite; 
	
	boolean doneYet; //unused
	int power;
	int accuracy;
	

	@Override
	public void step(PkmnGen game) {

		//set sprite position
		//if done with anim, do nextAction
		if (positions.isEmpty() || sprites.isEmpty()) {

			//assign damage to opposing pkmn
			int currHealth = game.battle.oppPokemon.currentStats.get("hp");
			//TODO - correct damage calculation
			int finalHealth = currHealth - this.power;
			if (finalHealth < 0) {finalHealth = 0;} //make sure finalHealth isn't negative
			game.battle.oppPokemon.currentStats.put("hp", finalHealth);
			
			PublicFunctions.insertToAS(game, this.nextAction);
			game.actionStack.remove(this);
			return;
		}
				

		//get next frame
		this.sprite = sprites.get(0);

		//debug
//		this.helperSprite.draw(game.floatingBatch); 
		
		
		//draw current sprite
//		if (this.sprite != null) {
//			this.sprite.setPosition(position.x, position.y);
//			this.sprite.draw(game.floatingBatch);	
//		}
		
		float currAlpha = this.alphas.get(0);
		game.battle.oppPokemon.sprite.setAlpha(currAlpha);
		
		//debug
//		if (this.repeats.size() == 14) { 
//			return;
//		}

		//get next sound, play it
		this.sound = this.sounds.get(0);
		if (this.sound != null) {
			PublicFunctions.insertToAS(game, new PlaySound(this.sound, new DoneAction()));
			this.sounds.set(0, null); //don't play same sound over again
		}
		
		
		//repeat sprite/pos for current object for 'frames[0]' number of frames.
		if (this.repeats.get(0) > 1) {
			this.repeats.set(0, this.repeats.get(0) - 1);
		}
		else {
			//since position is relative, only update once each time period
			this.position = this.position.add(positions.get(0));
			game.player.currPokemon.backSprite.setPosition(this.position.x, this.position.y);
			positions.remove(0);
			sprites.remove(0);
			repeats.remove(0);
			sounds.remove(0);
			alphas.remove(0);
		}
	}
	
	
	public DefaultAttack(PkmnGen game, int power, int accuracy, Action nextAction) {		

		this.power = power;
		this.accuracy = accuracy;
		
		this.doneYet = false; //unused
		this.nextAction = nextAction;

		//consider doing relative positions from now on
		//this.position = new Vector2(104+4*3-2,200-6*3-2); //post scaling change
		this.position = new Vector2(game.player.currPokemon.backSprite.getX(), game.player.currPokemon.backSprite.getY());
		
		this.positions = new ArrayList<Vector2>();
		this.positions.add(new Vector2(8,0));//move forward 1
		this.positions.add(new Vector2(-8,0));//move back 1
		for (int i = 0; i < 13; i++) {
			this.positions.add(new Vector2(0,0));
		}
		
		
		
		this.sprites = new ArrayList<Sprite>(); //may use this in future
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
		
		
		Texture text = new Texture(Gdx.files.internal("attack_menu/helper3.png"));
		this.helperSprite = new Sprite(text, 0, 0, 160, 144);
	}
}





/*//unused code
 * 
 * //old safari menu rock throw pre-run code
String textString = "Wild "+game.battle.oppPokemon.name+" is angry!";
Action catchAction = new ThrowRock(game, new DisplayText(game, textString, null, null,
		this ));
textString = game.player.name+" threw a rock.";
PublicFunctions.insertToAS(game, new DisplayText(game, textString, null, catchAction,
		catchAction
	)
);


*/
