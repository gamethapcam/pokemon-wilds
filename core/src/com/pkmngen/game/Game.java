package com.pkmngen.game;

// import gme_debug.VGMPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import box2dLight.RayHandler;

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
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Game extends ApplicationAdapter {
    

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
    public OrthographicCamera cam2;
    
    public Viewport viewport;
    
    //demo code - num objectives finished
    public int numObjectivesFinished = 0;
    //
    
    boolean playerCanMove;
    Action displayTextAction;
    
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

    //when want to play a music file, put in here. call dispose and remove elements when done
    HashMap<String, Music> loadedMusic =  new HashMap<String, Music>();

    //char-to-Sprite text dictionary
    Map<Character, Sprite> textDict;

    public static Game staticGame;

    // test box2d lights
    public World b2World;
    RayHandler rayHandler;
    float timeIncrement = 1.f / 60.f;
    float accumulator = 0;
    int velocityIterations = 6;
    int positionIterations = 2;

    @Override
    public void create() {

        //annoying - used for music completion listener
        this.staticGame = this;
        // Gdx.app.getApplicationListener() <-- ??

        //map handles unit positions
        //map = new GagMap(this, "Frost_Zone_v01");

        // test box2d lights
        this.b2World = new World(new Vector2(0, 0), true);
        this.rayHandler = new RayHandler(this.b2World);
        
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
//        float[] mtrx = {1,0,0,0,
//                        0,1,0,0,
//                        0,0,1,-1,
//                        0,0,0,1};
//        this.cam2 = new OrthographicCamera(); //160, 144
//        this.cam2.position.set(0, 2, 0);
//        this.cam2.translate(0f, 2f);
////        cam2.position.set(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, 0);
////        cam2.zoom = 1;
////        floatingBatch.setProjectionMatrix(cam2.projection);
//        System.out.println("mtrx: \n" + String.valueOf(new Matrix4(new Vector3(0,2,0), new Quaternion(), new Vector3())));
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
        //draw tops of trees over player
        startActions.add(new DrawMapTrees(this));
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
        
        // TODO: remove if unused
        //start playing music?
//        this.currMusic = this.map.currRoute.music;
        //this.currMusic.setVolume(.0f);
        //this.currMusic.play(); //debug TODO - enable this

        this.currMusic = Gdx.audio.newMusic(Gdx.files.internal("music/nature1_render.ogg"));
//        this.currMusic = Gdx.audio.newMusic(Gdx.files.internal("music/route_42.ogg"));
        this.map.currRoute.music = this.currMusic;
        this.currMusic.setLooping(false);
        this.currMusic.setVolume(1f);
        this.currMusic.play();
//        this.currMusic.setPosition(141); // TODO: debug, delete
//        this.currMusic.setPosition(93); // TODO: debug, delete
        // this music should 'radio' through a selection of musics for the route
        this.currMusic.setOnCompletionListener(new Music.OnCompletionListener() {
                @Override
                public void onCompletion(Music aMusic) {
                    String nextMusicName = Game.staticGame.map.currRoute.getNextMusic(true);
                    // TODO: would it be good to also accept music instance here?
                    // TODO: these fade-ins don't even work. will need for route musics
                    Action nextMusic = new FadeMusic("currMusic", "out", "", .025f,
                                       // TODO: there's def a bug here if you run into a wild
                                       // pokemon while waiting frames, the next music will start anyway
                                       new WaitFrames(Game.staticGame, 360,
                                       new FadeMusic(nextMusicName, "in", "", .2f, true, 1f, this, new DoneAction())));
                    PublicFunctions.insertToAS(Game.staticGame, nextMusic);
                    nextMusic.step(Game.staticGame);
                }
            }
        );
        
        this.playerCanMove = true;

        //extra stuff
//        PublicFunctions.insertToAS(this, new DrawObjectives(this));
//        PublicFunctions.insertToAS(this, new GenForest1(this, new Vector2(-64,-64), new Vector2(128,128)));
//        PublicFunctions.insertToAS(this, new GenForest2(this, new Vector2(-64,-48), new Vector2(320,336)));


        // was using this as default map
//        PublicFunctions.insertToAS(this, new GenForest2(this, new Vector2(-64,-48), new Vector2(800,800))); //this is the size I want

        // gen island map 
        // old size = 16*20
//        PublicFunctions.insertToAS(this, new GenIsland1(this, new Vector2(0,0), 20*40)); //16*15 //30*40 // 20*40  //16*18 //20*30
        // generates a mountain now.
        PublicFunctions.insertToAS(this, new GenIsland1(this, new Vector2(0,0), 20*30)); //20*30 //60*100 //100*120

        // TODO - mega gengar battle debug in genforest2, remove that

        // this is the special mewtwo debug map
        // comment out the genforest to use this
//        this.map = new PkmnMap("SpecialMewtwo");
//        PublicFunctions.insertToAS(this, new DrawSpecialMewtwoBg());
        
        // debug
        //PublicFunctions.insertToAS(this, new spawnGhost(this, new Vector2(32, 0))); //debug
        

        //TODO - remove
        //batch.enableBlending();
        //batch.setColor(new Color(0.01f, 0.01f, 0.2f, 1.0f));
        //batch.setBlendFunction(Gdx.gl.GL_MAX_TEXTURE_UNITS, Gdx.gl.GL_FUNC_ADD);
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
                    + "    gl_Position =  u_projTrans * a_position;\n"
                    + "}\n";
        String fragmentShader = "precision mediump float;\n"

                    + "varying vec4 v_color;\n"
                    + "varying vec2 v_texCoords;\n"
                    + "uniform sampler2D u_texture;\n"
                    + "uniform mat4 u_projTrans;\n"
                    
                    + "bool equals(float a, float b) {\n"
                    + "    return abs(a-b) < 0.0001;\n"
                    + "}\n"
                    
                    + "bool isWhiteShade(vec4 color) {\n"
                    + "    return equals(color.r, color.g) && equals(color.r, color.b);\n"
                    + "}\n"
                    
                    + "void main() {\n"
                    + "    vec4 color = texture2D (u_texture, v_texCoords) * v_color;\n"
                    + "    //if(isWhiteShade(color)) {\n"
                    + "    if(color.r == 1 && color.g == 1 && color.b == 1) {\n"
                    + "color *= vec4(0, 0, 1, 1);\n"
                    + "    }\n"
                    + "    else {\n"
                    + "        color *= vec4(0, 0, 0, 1);\n"
                    + "    }\n"
                    + "    gl_FragColor = color;\n"
                    + "}\n";
        
        ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
        //batch.setShader(shader);
        
        
        PublicFunctions.insertToAS(this, new cycleDayNight(this));
        
        //debug
        this.player.currPokemon = new Pokemon("Cloyster", 70);
//        this.player.currPokemon.name = "AA"; //debug
        this.player.pokemon.add(this.player.currPokemon); 
//        this.player.currPokemon.currentStats.put("hp", 0); //debug

        //TODO: debug, delete
//        this.player.currPokemon.currentStats.put("hp", 12);
        this.player.pokemon.add(new Pokemon("Machop", 50, Pokemon.Generation.CRYSTAL)); 
        this.player.pokemon.add(new Pokemon("sneasel", 50, Pokemon.Generation.CRYSTAL)); 
        this.player.pokemon.get(2).attacks[0] = "Bubblebeam";  // TODO: debug, remove
//        this.player.pokemon.add(new Pokemon("Zubat", 40)); 
//        this.player.pokemon.add(new Pokemon("Spinarak", 30)); 
//        this.player.pokemon.add(new Pokemon("Zubat", 20)); 
//        this.player.pokemon.add(new Pokemon("Zubat", 10)); 

        // TODO: delete
//        this.currMusic = this.battle.music;
//        this.currMusic.stop();
//        this.currMusic.play();
//        this.playerCanMove = false;
//        this.battle.oppPokemon = new Pokemon("mamoswine", 22, Pokemon.Generation.CRYSTAL);
//        PublicFunctions.insertToAS(this, Battle_Actions.get(this)); 
        
        // Example debug battle
//        this.currMusic = this.battle.music;
//        this.currMusic.stop();
//        this.currMusic.play();
//        this.playerCanMove = false;
//        this.battle.oppPokemon = new Pokemon("Cloyster", 40);
//        PublicFunctions.insertToAS(this, Battle_Actions.get(this)); 

        // debug for SpecialMewtwo battle
//        this.playerCanMove = false;
//        PublicFunctions.insertToAS(this, new SpecialBattleMewtwo(this));
        
        
//        System.out.println("color r:"+String.valueOf(Color.TEAL.r)); //debug
//        System.out.println("color b:"+String.valueOf(Color.TEAL.b));
//        System.out.println("color g:"+String.valueOf(Color.TEAL.g));
        
        //debug //these below work
//        String string1 = "AAAA used         SAFARI BALL!";
//        String string1 = "AAAA threw a      ROCK.";
//        String string1 = "All right!        PARASECT was      caught!";
//        String string1 = "All right! PARASECT was caught!";
//        String string1 = "Hey, what?";
//        String string1 = "testing long long long long long long long long long long long long long long";
//        String string1 = "AAAA has ADRENALINE 5!";
//        PublicFunctions.insertToAS(this, new DisplayText(this, string1, "fanfare1", null, new DoneAction()));
//        PublicFunctions.insertToAS(this, new DisplayText(this, string1, null, null, new DoneAction()));
        
//        // trying out gme stuff
//        //trying the vgmplayer route
//        int sampleRate = 44100;
//        VGMPlayer gbsPlayer = new VGMPlayer(sampleRate);
//        try {
//            //if url=path for now it won't try to do http get on url
//            // and instead load file
//            // dumb I know
//            gbsPlayer.loadFile("C:/Users/Evan/Desktop/pokemon_gbs/Pokemon Gold zophar/DMG-AAUJ-JPN.gbs",
//                               "C:/Users/Evan/Desktop/pokemon_gbs/Pokemon Gold zophar/DMG-AAUJ-JPN.gbs");
//
////            gbsPlayer.loadFile("C:/Users/Evan/Desktop/pokemon_gbs/PM_Y_C_Stereo_GBS/Pokemon Crystal (2001)(Game Freak, Nintendo).gbs",
////                               "C:/Users/Evan/Desktop/pokemon_gbs/PM_Y_C_Stereo_GBS/Pokemon Crystal (2001)(Game Freak, Nintendo).gbs");
//            //don't trust file number in directory - look inside m3u file for GBS,<track number>
//            gbsPlayer.startTrack(88, 150);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        // draw android controls if on android device
        // TODO: didnt finish
//        PublicFunctions.insertToAS(this, new DrawAndroidControls());

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
                // TODO: debug, remove timeit data
                if (Gdx.input.isKeyPressed(Input.Keys.P)) {
                    System.out.println(action);
                    System.out.println(java.time.LocalTime.now());  
                }
                action.step(this);
                if (Gdx.input.isKeyPressed(Input.Keys.P)) { 
                    System.out.println(action);
                    System.out.println(java.time.LocalTime.now()); 
                }
            }
        }
        batch.end();
        

        // TODO: debug box2d lights
//        this.accumulator += Gdx.graphics.getDeltaTime();
//        while (this.accumulator >= this.timeIncrement) {
//            this.b2World.step(this.timeIncrement, this.velocityIterations, this.positionIterations);
//            this.accumulator -= this.timeIncrement;
//        }
//        this.rayHandler.setCombinedMatrix(this.cam.combined.cpy().scale(32.f, 32.f, 32.f));
////        this.rayHandler.useCustomViewport(Gdx.graphics.getWidth() / 2, 0, Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight());
//        this.rayHandler.setAmbientLight(new Color(0f, 0f, 0f, 1f));
//        this.rayHandler.updateAndRender();

        floatingBatch.begin();
        //iterate through action stack
        for (Action action : new ArrayList<Action>(this.actionStack)) { //iterate copy
            if (action.getCamera() == "gui") { //only gui actions
                action.step(this);
            }
        }
        //disabled as per html5
        //font.draw(floatingBatch, "Cam zoom: "+String.valueOf(cam.zoom), 130, 40);
//        font.draw(floatingBatch, "Cam x pos: "+String.valueOf(cam.position.x), 130, 30);
//        font.draw(floatingBatch, "Cam y pos: "+String.valueOf(cam.position.y), 130, 20); 
//        font.draw(floatingBatch, "Mouse x pos: "+String.valueOf(Gdx.input.getX()), 130, 50);
//        font.draw(floatingBatch, "Mouse y pos: "+String.valueOf(Gdx.graphics.getHeight() -  Gdx.input.getY()), 130, 40); 

//        font.draw(floatingBatch, "Mouse x pos: "+String.valueOf((Gdx.input.getX())/3), 0, 20);
//        font.draw(floatingBatch, "Mouse y pos: "+String.valueOf((Gdx.graphics.getHeight() -  Gdx.input.getY())/3), 0, 10); 
//        font.draw(floatingBatch, "Debug: "+PkmnGen.debugString, 10, 20);

//        font.draw(floatingBatch, "CTRL = Slide", 130, 20); 
//        font.draw(floatingBatch, "WASD = Move", 130, 40); 
//        font.draw(floatingBatch, "Q/E = Zoom", 130, 60); 
//        font.draw(floatingBatch, "FPS... = " + String.valueOf(Gdx.graphics.getFramesPerSecond()), 10, 20); 
        
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
        textDict.put('É', new Sprite(text, 10+16*9, 5+12+12+12, 8, 8)); //same as lower case é, used in menus (ie POKéBALL, etc)
        textDict.put('-', new Sprite(text, 10+16*10, 5+12+12+12, 8, 8)); 
        textDict.put('\'', new Sprite(text, 10+16*11, 5+12+12+12, 8, 8)); 
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
