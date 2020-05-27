package com.pkmngen.game;

// import gme_debug.VGMPlayer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import box2dLight.RayHandler;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
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
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;

public class Game extends ApplicationAdapter {
    
    Vector3 touchLoc;
    
    public ArrayList<Action> actionStack;
    
    //for debug
    public static String debugString;
    
    // For non-floating elements
    public SpriteBatch mapBatch;
    
    // For floating ui elements
    public SpriteBatch uiBatch;
    
    public BitmapFont font;
    
    public OrthographicCamera cam;
    public OrthographicCamera cam2;
    
    // TODO: remove if unused
//    public Viewport viewport;
    
    //demo code - num objectives finished
    public int numObjectivesFinished = 0;

    boolean playerCanMove;
    Action displayTextAction;

    // Network
    public Client client;
    public Server server;
    
    //box2d
    //World world;
    //Box2DDebugRenderer debugRenderer;

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

    // Annoying - used for music completion listener
    public static Game staticGame;

    // test box2d lights
    public World b2World;
    RayHandler rayHandler;
    float timeIncrement = 1.f / 60.f;
    float accumulator = 0;
    int velocityIterations = 6;
    int positionIterations = 2;
    Vector2 currScreen;
    
    // Network
    // ID: Player
    HashMap<String, Player> players = new HashMap<String, Player>();
    // server determines outcome of all actions done in battle
    HashMap<String, Battle> battles = new HashMap<String, Battle>();
    
    enum Type {
        CLIENT,
        SERVER;
    }
    Type type;
    
    Music.OnCompletionListener musicCompletionListener;

    @Override
    public void create() {

        // Annoying - used for music completion listener
        this.staticGame = this;
        // Gdx.app.getApplicationListener() <-- ??

        //map handles unit positions
        //map = new GagMap(this, "Frost_Zone_v01");

        // test box2d lights
        this.b2World = new World(new Vector2(0, 0), true);
        this.rayHandler = new RayHandler(this.b2World);
        
        this.mapBatch = new SpriteBatch();
        this.uiBatch = new SpriteBatch();
            
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
//        this.viewport = new FitViewport(160, 144, this.cam);
        
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

        //init player structure (store global vars)
        this.player = new Player();
        //init battle structure
        this.battle = new Battle();
        //init map structure
        this.map = new PkmnMap("default");
        
        this.textDict = initTextDict();

        this.actionStack  = new ArrayList<Action>();
        this.insertAction(new DrawSetupMenu(this, null));
    }
    
    public void start() {
        // TODO: there's a bunch of junk here, need to go through.
        
        //add start actions
        ArrayList<Action> startActions = new ArrayList<Action>();
        //draw map
        startActions.add(new DrawMap(this));
        //set player
        startActions.add(new playerStanding(this));
        //lower player draw action
        startActions.add(new DrawPlayerLower(this));
        //draw map grass
        startActions.add(new DrawMapGrass(this));
        //upper player draw action
        startActions.add(new DrawPlayerUpper(this));
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
        this.actionStack.addAll(startActions);
        
        // TODO: remove if unused
        //start playing music?
//                    this.currMusic = this.map.currRoute.music;
        //this.currMusic.setVolume(.0f);
        //this.currMusic.play(); //debug TODO - enable this

        this.currMusic = Gdx.audio.newMusic(Gdx.files.internal("music/nature1_render.ogg"));
//                    this.currMusic = Gdx.audio.newMusic(Gdx.files.internal("music/route_42.ogg"));
        this.map.currRoute.music = this.currMusic;
        this.currMusic.setLooping(false);
        this.currMusic.setVolume(1f);
        this.currMusic.play();
        this.currMusic.pause();
//                    this.currMusic.setPosition(130f);
        this.currMusic.play();
//                    this.currMusic.setPosition(141); // TODO: debug, delete
//                    this.currMusic.setPosition(93); // TODO: debug, delete
        // this music should 'radio' through a selection of musics for the route
        this.musicCompletionListener = new Music.OnCompletionListener() {
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
        };
        this.currMusic.setOnCompletionListener(this.musicCompletionListener);
        
        this.playerCanMove = true;

        //extra stuff
//                    PublicFunctions.insertToAS(this, new DrawObjectives(this));
//                    PublicFunctions.insertToAS(this, new GenForest1(this, new Vector2(-64,-64), new Vector2(128,128)));
//                    PublicFunctions.insertToAS(this, new GenForest2(this, new Vector2(-64,-48), new Vector2(320,336)));


        // was using this as default map
//                    PublicFunctions.insertToAS(this, new GenForest2(this, new Vector2(-64,-48), new Vector2(800,800))); //this is the size I want

        // gen island map 
        // old size = 16*20
//                    PublicFunctions.insertToAS(this, new GenIsland1(this, new Vector2(0,0), 20*40)); //16*15 //30*40 // 20*40  //16*18 //20*30
        // generates a mountain now.
        this.insertAction(new GenIsland1(this, new Vector2(0,0), 100*100)); //100*100 //20*30 //60*100 //100*120

        // TODO - mega gengar battle debug in genforest2, remove that

        // this is the special mewtwo debug map
        // comment out the genforest to use this
//                    this.map = new PkmnMap("SpecialMewtwo");
//                    PublicFunctions.insertToAS(this, new DrawSpecialMewtwoBg());
        
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
        
        
        this.insertAction(new cycleDayNight(this));
        
        //debug
//                    this.player.currPokemon = new Pokemon("Cloyster", 70);
//                    this.player.pokemon.add(this.player.currPokemon); 

        //TODO: debug, delete
//                    this.player.currPokemon.currentStats.put("hp", 12);
//                    this.player.pokemon.add(new Pokemon("Machop", 50, Pokemon.Generation.CRYSTAL)); 
//                    this.battle.attacks.get("karate chop").power = 200;  // TODO: debug, remove
        this.player.pokemon.add(new Pokemon("Cyndaquil", 50, Pokemon.Generation.CRYSTAL)); 
        this.battle.attacks.get("flamethrower").power = 200;  // TODO: debug, remove
        this.player.pokemon.add(new Pokemon("sneasel", 50, Pokemon.Generation.CRYSTAL)); 
        this.player.pokemon.get(1).attacks[0] = "Bubblebeam";  // TODO: debug, remove
        this.player.pokemon.add(new Pokemon("stantler", 50, Pokemon.Generation.CRYSTAL));
        this.player.currPokemon = this.player.pokemon.get(0);

        
        // TODO: delete
//                    this.currMusic = this.battle.music;
//                    this.currMusic.stop();
//                    this.currMusic.play();
//                    this.playerCanMove = false;
//                    this.battle.oppPokemon = new Pokemon("mamoswine", 22, Pokemon.Generation.CRYSTAL);
//                    PublicFunctions.insertToAS(this, Battle_Actions.get(this)); 
        
        // Example debug battle
//                    this.currMusic = this.battle.music;
//                    this.currMusic.stop();
//                    this.currMusic.play();
//                    this.playerCanMove = false;
//                    this.battle.oppPokemon = new Pokemon("Cloyster", 40);
//                    PublicFunctions.insertToAS(this, Battle_Actions.get(this)); 

        // debug for SpecialMewtwo battle
//                    this.playerCanMove = false;
//                    PublicFunctions.insertToAS(this, new SpecialBattleMewtwo(this));
        
        
//                    System.out.println("color r:"+String.valueOf(Color.TEAL.r)); //debug
//                    System.out.println("color b:"+String.valueOf(Color.TEAL.b));
//                    System.out.println("color g:"+String.valueOf(Color.TEAL.g));
        
        //debug //these below work
//                    String string1 = "AAAA used         SAFARI BALL!";
//                    String string1 = "AAAA threw a      ROCK.";
//                    String string1 = "All right!        PARASECT was      caught!";
//                    String string1 = "All right! PARASECT was caught!";
//                    String string1 = "Hey, what?";
//                    String string1 = "testing long long long long long long long long long long long long long long";
//                    String string1 = "AAAA has ADRENALINE 5!";
//                    PublicFunctions.insertToAS(this, new DisplayText(this, string1, "fanfare1", null, new DoneAction()));
//                    PublicFunctions.insertToAS(this, new DisplayText(this, string1, null, null, new DoneAction()));
        
//                    // trying out gme stuff
//                    //trying the vgmplayer route
//                    int sampleRate = 44100;
//                    VGMPlayer gbsPlayer = new VGMPlayer(sampleRate);
//                    try {
//                        //if url=path for now it won't try to do http get on url
//                        // and instead load file
//                        // dumb I know
//                        gbsPlayer.loadFile("C:/Users/Evan/Desktop/pokemon_gbs/Pokemon Gold zophar/DMG-AAUJ-JPN.gbs",
//                                           "C:/Users/Evan/Desktop/pokemon_gbs/Pokemon Gold zophar/DMG-AAUJ-JPN.gbs");
//
////                        gbsPlayer.loadFile("C:/Users/Evan/Desktop/pokemon_gbs/PM_Y_C_Stereo_GBS/Pokemon Crystal (2001)(Game Freak, Nintendo).gbs",
////                                           "C:/Users/Evan/Desktop/pokemon_gbs/PM_Y_C_Stereo_GBS/Pokemon Crystal (2001)(Game Freak, Nintendo).gbs");
//                        //don't trust file number in directory - look inside m3u file for GBS,<track number>
//                        gbsPlayer.startTrack(88, 150);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }

        // draw android controls if on android device
        // TODO: didnt finish
//                    PublicFunctions.insertToAS(this, new DrawAndroidControls());
    }

    @Override
    public void dispose() {
        mapBatch.dispose();
        uiBatch.dispose();
    }

    @Override
    public void render() {    

        handleInput();
        
        Gdx.gl.glClearColor(1, 1, 1, 1);
        // Gdx.gl.glClearColor(0, 0, 0, 1);  // was black bg
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        this.cam.update();
        this.mapBatch.setProjectionMatrix(cam.combined);
        
        // Iterate twice, once for this.batch (map objects), once for this.uiBatch (ui objects)
        this.mapBatch.begin();
        // Iterate through the action stack and call the step() fn of each Action.
        for (Action action : new ArrayList<Action>(this.actionStack)) {
            if (action.getCamera() == "map") {
                // TODO: debug, remove timeit data
                if (Gdx.input.isKeyPressed(Input.Keys.P)) {
                    System.out.println(action);
                    System.out.println(java.time.LocalTime.now());  
                }
                if (action.firstStep) {
                    action.firstStep(this);
                    action.firstStep = false;
                }
                action.step(this);
                if (Gdx.input.isKeyPressed(Input.Keys.P)) { 
                    System.out.println(action);
                    System.out.println(java.time.LocalTime.now()); 
                }
            }
        }
        this.mapBatch.end();
        
        // TODO: Debug box2d lights
        // TODO: Didn't end up using this, using shaders for lighting because
        // it looks more natural to the Gen1/Gen2 graphics.
//        this.accumulator += Gdx.graphics.getDeltaTime();
//        while (this.accumulator >= this.timeIncrement) {
//            this.b2World.step(this.timeIncrement, this.velocityIterations, this.positionIterations);
//            this.accumulator -= this.timeIncrement;
//        }
//        this.rayHandler.setCombinedMatrix(this.cam.combined.cpy().scale(32.f, 32.f, 32.f));
////        this.rayHandler.useCustomViewport(Gdx.graphics.getWidth() / 2, 0, Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight());
//        this.rayHandler.setAmbientLight(new Color(0f, 0f, 0f, 1f));
//        this.rayHandler.updateAndRender();

        this.uiBatch.begin();
        //iterate through action stack
        for (Action action : new ArrayList<Action>(this.actionStack)) { //iterate copy
            if (action.getCamera().equals("gui")) { //only gui actions
                if (action.firstStep) {
                    action.firstStep(this);
                    action.firstStep = false;
                }
                action.step(this);
            }
        }
        // TODO: remove if unused.
        // Disabled as per html5
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
        this.uiBatch.end();
        
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
            // TODO: debug, remove
//            System.out.println("Musics: " + String.valueOf(this.map.currRoute.musics.size()));
            System.out.println("Test:  ");
            System.out.println(3 + (3 << 2) + (0 << 4));
        }
        // check network type (reset when pressed)
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            // set up networking
            try {
                initServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.N)) {
            // set up networking
            try {
                initClient();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /*
     * Used when drawing in-game text boxes. Creates a character->Sprite map.
     */
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
        textDict.put('ì', new Sprite(text, 10+16*12, 5+12+12+12, 8, 8)); 
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

        if (Gdx.app.getType() == ApplicationType.Android) {
            // TODO: haven't tested the android resizing yet
            int menuHeight = (160*height)/width;  // height/width = menuHeight/160
            int offsetY = (menuHeight-144)/2;
            this.uiBatch.getProjectionMatrix().setToOrtho2D(0, -offsetY, 160, menuHeight);
        }
        else {
            // below will scale floatingBatch regardless of current screen size
            int menuWidth = (144*width)/height;  // height/width = 144/menuWidth
//            System.out.println(menuWidth);
            int offsetX = (menuWidth-160)/2;
//            System.out.println(offsetX);
            
            this.uiBatch.getProjectionMatrix().setToOrtho2D(-offsetX, 0, menuWidth, 144);
            // this might not be technically the best method; works for now.
            this.cam.viewportWidth = width/3;
            this.cam.viewportHeight = height/3;
        }
        this.currScreen = new Vector2(width, height);
        // width/3, height/3);;
//        System.out.println(width);
//        System.out.println(height);

        //if (Gdx.app.getType() == ApplicationType.Android) {
            //below is basically prototype.\
             //this isn't perfect atm, because requires scale variable
            //also, restricts drawing to inside viewport, which isn't right. 
            //need to draw controls outside
        
//            int left = (width - 160*3)/2;
//            int bottom = (height - 144*3)/2;
//            this.viewport.setScreenBounds(left, bottom, 160*3, 144*3);
//            //Gdx.graphics.
//            this.viewport.apply(); //provides more control. same as update
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

    public void initServer() throws IOException {

        //make screen smaller (not split screen)
//        Gdx.graphics.setDisplayMode(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight(), false);
    
        //resize window
//          this.viewport.setScreenBounds(0,0, 144, 160); // Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight());
//          this.viewport.apply(); //provides more control. same as update
        
        this.server = new Server() {
            protected Connection newConnection() {
                // By providing our own connection implementation, we can
                // store per
                // connection state without a connection ID to state look
                // up.
                return new CharacterConnection();
            }
        };

        Network.register(this.server);
        this.type = Game.Type.SERVER;

        this.server.bind(Network.port);
        this.server.start();
        while (this.server.getUpdateThread() == null) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        //was previously using these lines to restrict when server can receive messages (b/c of threading issues)
         //may need in future
//          this.server.getUpdateThread().stop();
//          this.insertAction(new ServerReceive()); 
        
        
//          this.server.update(0);
//          this.updateThread = new Thread(this.server, "Server");
//          updateThread.start();

        //change player 2's type
//        this.player2.type = HeroPlayer.Type.NETWORK;
//        this.player2.cam = this.cam2; //have to switch cam, or else causes input errors
//        Filter filter = new Filter();
//        filter.maskBits = Mask.None.getValue();
//        filter.categoryBits = Category.None.getValue();
//        this.player2.physics.fixture.setFilterData(filter);
        
        // add action that will continually update client of positions
        
        this.map.loadFromFile(this);  // load map if it exists already
        PublicFunctions.insertToAS(this, new ServerBroadcast(this));
        PublicFunctions.insertToAS(this, new PkmnMap.PeriodicSave(this));
    }

    public void initClient() throws IOException {
        if (this.client != null) {
            this.client.close();
        }
        this.client = new Client();
        this.type = Game.Type.CLIENT;

        Network.register(client);

        this.client.start();
        
        try {
            this.client.connect(5000, "127.0.0.1", Network.port);
//            this.client.connect(5000, "25.10.89.3", Network.port);  // hamachi?
//              this.client.connect(5000, "192.168.101", Network.port); 
            // Server communication after connection can go here, or in
            // Listener#connected().
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
//          this.client.stop(); //attempting to manually update
        //didn't work - it seems like this will prevent future updates from working
        
        //these lines were used to avoid threading issues (see notes in server above)
//          this.client.getUpdateThread().stop(); //stop thread and update manually in future
//          insertAction(new ClientReceive());
        
//        Log.set(Log.LEVEL_DEBUG);

        //TODO - put this somewhere?
//        this.player.type = HeroPlayer.Type.NETWORK;
//        Filter filter = new Filter();
//        filter.maskBits = Mask.None.getValue();
//        filter.categoryBits = Category.None.getValue();
//        this.player.physics.fixture.setFilterData(filter);
//        WaitTurnData
//        //register fireball as networked
//        this.map.fireball.type = Fireball.Type.NETWORK;
//        //register map as networked
//        this.map.type = GameMap.Type.NETWORK;
//        
//        //remove ghosts and spawner from map
//        this.actionStack.remove(this.spawnGhosts);
//        for (Ghost ghost : map.enemies) {
//            b2World.destroyBody(ghost.physics.body);
//            actionStack.remove(ghost.physics);
//            actionStack.remove(ghost.graphics);
//            actionStack.remove(ghost);
//        }
//        this.map.enemies.clear();
//        this.map.networkGhosts = new SpawnNetworkGhosts(this);
//
//        System.out.println("client requesting all ghosts");
        
        //request ghosts from server
//        this.client.sendTCP(new Network.AllGhosts());

        this.player.network.id = "dummy_id4";
        this.player.type = Player.Type.LOCAL;
        this.client.sendTCP(new Network.Login(this.player.network.id));

        // server won't say when to clear tiles, so do it now.
        this.map.tiles.clear();
//        this.map.trees.clear(); // TODO: needs to be handled differently
        
        PublicFunctions.insertToAS(this, new ClientBroadcast(this));
    }

    // TODO - keep? remove?
    // This holds per connection state.
    static class CharacterConnection extends Connection {
        public Vector2 character;
    }
    
    /*
     * Insert action to Game.ActionStack at the layer defined by Action.getLayer().
     */
    public void insertAction(Action actionToInsert) {
        // handle null entry by skipping
        if (actionToInsert == null) {
            return;
        }
        for (int i = 0; i < this.actionStack.size(); i++) {
            //if actionToInsert layer is <=  action layer, insert before element
            if (actionToInsert.getLayer() >= this.actionStack.get(i).getLayer()) {
                this.actionStack.add(i, actionToInsert);
                return;
            }
        }
        //layer is smallest in stack, so add to the end.
        this.actionStack.add(actionToInsert);
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
