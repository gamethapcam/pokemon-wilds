package com.pkmngen.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class PkmnGen extends ApplicationAdapter {
	

	Vector3 touchLoc;
	
	//put alarm-type things in here
	 //see create() for example of action chaining
	public ArrayList<Action> actionStack;
	
	
	//for debug
	public static String debugString;
	
	//for non-floating elements
	public SpriteBatch batch;
	
	//for floating ui elements
	public SpriteBatch floatingBatch;
	
	public BitmapFont font;
	
	public OrthographicCamera cam;
	
	public Viewport viewport;
	
	//demo code - num objectives finished
	public int numObjectivesFinished = 0;
	//
	
	boolean playerCanMove;
	
	//box2d
	//World world;
	//Box2DDebugRenderer debugRenderer;
	
	//global info structures
	Player player;
	PkmnMap map;
	Battle battle;
	
	//try this for overworld music, etc
	 //may have to replace this with a string
	Music currMusic;
	
	//char-to-Sprite text dictionary
	Map<Character, Sprite> textDict;
	
	@Override
	public void create() {	

		//map handles unit positions
		//map = new GagMap(this, "Frost_Zone_v01");

		batch = new SpriteBatch();
		floatingBatch = new SpriteBatch();
			
		//have the map return a camera to use
		//some borrowed camera dimensions
		//this.cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		this.cam = new OrthographicCamera(160, 144);//this will force window to contain gb-equiv pixels
		this.cam.position.set(16, 0, 0); //544, 300, 0);
		this.cam.zoom = 1;//10.0f; //comfortable initial zoom.
		
		//do this to scale floating batch?
		 //seems like it might have worked
		//TODO - delete this stuff. have setToOrtho in resize fn
//		float[] mtrx = {1,0,0,0,
//		                0,1,0,0,
//		                0,0,1,-1,
//		                0,0,0,1};
//		OrthographicCamera cam2 = new OrthographicCamera(160, 144);
//		cam2.position.set(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, 0);
//		cam2.zoom = 1;
//		floatingBatch.setProjectionMatrix(cam2.projection);
//		System.out.println("mtrx: " + String.valueOf(cam2.projection));
		
		//fit viewport?
		this.viewport = new FitViewport(160, 144, this.cam);
		
		//set the font //disabled as per html5
		FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = 10;
		font = gen.generateFont(parameter);
		font.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		//font.setColor(Color.RED);
		font.setColor(Color.BLACK);
		//font.setScale(1,1); //used this when floatingBatch was 3x size
		//font.setScale(.4f,.4f);
		font.setScale(.5f,.5f);
		
		//stores touch location
		touchLoc = new Vector3();
		
		//add start actions
		ArrayList<Action> startActions = new ArrayList<Action>();
		//draw bg
		//startActions.add(new drawAction_map1());
		//control_ship1 ship1
		//startActions.add(new control_ship1(this));
		//draw_crosshair
		//startActions.add(new draw_crosshair(this));
		//draw map
		startActions.add(new drawMap(this));
		//set player
		startActions.add(new playerStanding(this));
		//lower player draw action
		startActions.add(new drawPlayer_lower(this));
		//draw map grass
		startActions.add(new drawMap_grass(this));
		//upper player draw action
		startActions.add(new drawPlayer_upper(this));
		//move water tiles around
		startActions.add(new MoveWater(this));

		
		
		//debug - generate mountain at 10,10
		//startActions.add(new genMountain_1(this, new Vector2(32,32), 1, 1));
				
		//draw pokeballs top screen for each one caught (demo)
		//startActions.add(new DrawPokemonCaught(this));
		
		//debug
		//startActions.add(new GenForest1(this, new Vector2(-64,-64), new Vector2(128,128))); //doesn't work b/c AS reference in init
		//startActions.add(new GenForest2(this, new Vector2(-64,-64), new Vector2(128,128)));
		
		//initialize action_stack
		this.actionStack  = new ArrayList<Action>();
		this.actionStack.addAll(startActions); 
		
		
		//init player structure (store global vars)
		this.player = new Player();
		//init map structure
		this.map = new PkmnMap("default");
		//init battle structure
		this.battle = new Battle();
		
		this.textDict = initTextDict();
		
		//start playing music?
		this.currMusic = this.map.currRoute.music;
		//this.currMusic.setVolume(.0f);
		//this.currMusic.play(); //debug TODO - enable this
		
		this.playerCanMove = true;

		//extra stuff
		//PublicFunctions.insertToAS(this, new DrawObjectives(this));
		//PublicFunctions.insertToAS(this, new GenForest1(this, new Vector2(-64,-64), new Vector2(128,128)));
		//PublicFunctions.insertToAS(this, new GenForest2(this, new Vector2(-64,-48), new Vector2(320,336)));
		PublicFunctions.insertToAS(this, new GenForest2(this, new Vector2(-64,-48), new Vector2(800,800)));
		
		//debug //these below work
//		String string1 = "AAAA used         SAFARI BALL!";
//		String string1 = "AAAA threw a      ROCK.";
//		String string1 = "All right!        PARASECT was      caught!";
//		String string1 = "All right! PARASECT was caught!";
//		String string1 = "Hey, what?";
//		String string1 = "testing long long long long long long long long long long long long long long";
//		String string1 = "AAAA has ADRENALINE 5!";
//		PublicFunctions.insertToAS(this, new DisplayText(this, string1, "fanfare1", null, new DoneAction()));
//		PublicFunctions.insertToAS(this, new DisplayText(this, string1, null, null, new DoneAction()));
		
	}

	@Override
	public void dispose() {
		batch.dispose();
		floatingBatch.dispose();
	}

	@Override
	public void render() {	

		//TODO 
		 //...
		
		
		handleInput();
		
		Gdx.gl.glClearColor(1, 1, 1, 1);
		//Gdx.gl.glClearColor(0, 0, 0, 1); //black bg
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		cam.update();
		batch.setProjectionMatrix(cam.combined);
		
		//iterate twice because batch drawing happens separately
		
		batch.begin();
		//iterate through action stack
		for (Action action : new ArrayList<Action>(this.actionStack)) { //iterate copy
			if (action.getCamera() == "map") { //only map actions
				action.step(this);
			}
		}
		batch.end();
		
		floatingBatch.begin();
		//iterate through action stack
		for (Action action : new ArrayList<Action>(this.actionStack)) { //iterate copy
			if (action.getCamera() == "gui") { //only gui actions
				action.step(this);
			}
		}
		//disabled as per html5
		//font.draw(floatingBatch, "Cam zoom: "+String.valueOf(cam.zoom), 130, 40);
//		font.draw(floatingBatch, "Cam x pos: "+String.valueOf(cam.position.x), 130, 30);
//		font.draw(floatingBatch, "Cam y pos: "+String.valueOf(cam.position.y), 130, 20); 
//		font.draw(floatingBatch, "Mouse x pos: "+String.valueOf(Gdx.input.getX()), 130, 50);
//		font.draw(floatingBatch, "Mouse y pos: "+String.valueOf(Gdx.graphics.getHeight() -  Gdx.input.getY()), 130, 40); 

//		font.draw(floatingBatch, "Mouse x pos: "+String.valueOf((Gdx.input.getX())/3), 0, 20);
//		font.draw(floatingBatch, "Mouse y pos: "+String.valueOf((Gdx.graphics.getHeight() -  Gdx.input.getY())/3), 0, 10); 
//		font.draw(floatingBatch, "Debug: "+PkmnGen.debugString, 10, 20);

//		font.draw(floatingBatch, "CTRL = Slide", 130, 20); 
//		font.draw(floatingBatch, "WASD = Move", 130, 40); 
//		font.draw(floatingBatch, "Q/E = Zoom", 130, 60); 
//		font.draw(floatingBatch, "FPS... = " + String.valueOf(Gdx.graphics.getFramesPerSecond()), 10, 20); 
		
		floatingBatch.end();
		
		//box2d
		//this.world.step(1/60f, 6, 2);
		//this.debugRenderer.render(world, this.cam.combined);
		
	}
	
	private void handleInput() {
		
		
		if(Gdx.input.isKeyPressed(Input.Keys.Q)) {
			cam.zoom += 0.5;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.E)) {
			cam.zoom -= 0.5;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.A)) {
			if (cam.position.x > -10000)
				cam.translate(-20, 0, 0);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.D)) {
			if (cam.position.x < 10000)
				cam.translate(20, 0, 0);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.S)) {
			if (cam.position.y > -10000)
				cam.translate(0, -20, 0);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.W)) {
			if (cam.position.y < 10000)
				cam.translate(0, 20, 0);
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.F)) {
			this.cam.zoom = 1;
		}
		//print action stack in order if p key is pressed
		if(Gdx.input.isKeyPressed(Input.Keys.P)) {
			System.out.println("Layer, Name");
			for (Action action : this.actionStack) {
				System.out.println(String.valueOf(action.getLayer()) + "  " + action.getClass().getName());
			}
		}
		
	}
	
	//used when drawing in-game text boxes
	//this will load the map from characters to Sprites
	public Map<Character, Sprite> initTextDict() {
		
		//modify spritesheet if there are problems
		
		Map<Character, Sprite> textDict = new HashMap<Character, Sprite>();
		
		//this sheet happens to start with an offset of x=5, y=1
		Texture text = new Texture(Gdx.files.internal("text_sheet1.png"));

		char[] alphabet_upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
		char[] alphabet_lower = "abcdefghijklmnopqrstuvwxyz".toCharArray();
		
		//uppercase letters
		for (int i = 0; i < 26; i++) {
			textDict.put(alphabet_upper[i], new Sprite(text, 10+16*i, 5, 8, 8));
		}
		//lowercase letters
		for (int i = 0; i < 26; i++) {
			textDict.put(alphabet_lower[i], new Sprite(text, 10+16*i, 5+12, 8, 8));
		}
		//numbers
		for (int i = 0; i < 10; i++) {
			//textDict.put(Character.toChars(i)[0], new Sprite(text, 10+16*i, 5+12+12, 8, 8));
			//textDict.put((char)i, new Sprite(text, 10+16*i, 5+12+12, 8, 8));
			textDict.put(Character.forDigit(i,10), new Sprite(text, 10+16*i, 5+12+12, 8, 8));
		}
		//special chars
		//char[] special_chars = " ".toCharArray();
		textDict.put(' ', new Sprite(text, 10+16*10, 5+12+12, 8, 8)); //blank spot
		textDict.put('?', new Sprite(text, 10+16*3, 5+12+12+12, 8, 8)); 
		textDict.put('!', new Sprite(text, 10+16*4, 5+12+12+12, 8, 8)); 
		textDict.put('.', new Sprite(text, 10+16*7, 5+12+12+12, 8, 8)); 
		textDict.put(',', new Sprite(text, 10+16*8, 5+12+12+12, 8, 8)); 
		textDict.put('é', new Sprite(text, 10+16*9, 5+12+12+12, 8, 8)); 
		textDict.put(null, new Sprite(text, 10+16*0, 5+12+12+12+12, 8, 8)); //use when no char found
		
		return textDict;
		
	}

	@Override
	public void resize(int width, int height) {
		
		
		 //this sort of works. it will essentially change the height and width of camera to equal viewport 
		 //size when screen is resized, and at beginning of program.
		 //doesn't create letterbox bars on android like i want. mebe pass in fixed heigh/width?
		//this.viewport.update(width, height, true);
		//scaling viewport here doesn't affect screen pixel size. 
		 //this might be the right way to resize
		//not sure if pixels are right or wrong
		
		//below will scale floatingBatch regardless of current screen size
		this.floatingBatch.getProjectionMatrix().setToOrtho2D(0, 0, 160, 144);
		//width/3, height/3);
		

		//if (Gdx.app.getType() == ApplicationType.Android) {
			//below is basically prototype.\
			 //this isn't perfect atm, because requires scale variable
			//also, restricts drawing to inside viewport, which isn't right. 
			//need to draw controls outside
			int left = (width - 160*3)/2;
			int bottom = (height - 144*3)/2;
			this.viewport.setScreenBounds(left,bottom, 160*3, 144*3);
			//Gdx.graphics.
			this.viewport.apply(); //provides more control. same as update
		//}
		//this.viewport.update(160*3, 144*3, false);
		//int newWidth = (height/144)*160; - didn't work right. should have?
		//this.viewport.update(width, height, false); //doesn't work anymore?
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
	
	
	
	
	// EXAMPLE CODE - delete sometime
	/*
	SpriteBatch batch;
	Texture img;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(img, 0, 0);
		batch.end();
	}
	
	
	//old way to resize screen and retain correct floatingBatch coordinates
	//this will cause floatingBatch to work right
	//Gdx.graphics.setDisplayMode(160*3, 144*3, false); //decided not to use this
	
	*/
}
