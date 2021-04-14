package com.pkmngen.game;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import com.pkmngen.game.PkmnMap.PeriodicSave;
import com.pkmngen.game.util.ProxyBatch;
import com.pkmngen.game.util.SpriteProxy;

/**
 * Standard libGDX Game boilerplate extended for Pokemon Wilds.
 * 
 * @author  SheerSt on github
 * @version 0.1
 * @since   2020-06-11
 */
public class Game extends ApplicationAdapter {
    public static Game staticGame;  // Annoying - used by music completion listener
    public ArrayList<Action> actionStack = new ArrayList<Action>();
    public ProxyBatch mapBatch;
    public ProxyBatch uiBatch;
    public BitmapFont font;
    public OrthographicCamera cam;
    Vector3 touchLoc = new Vector3();
    public boolean playerCanMove;  // TODO: migrate to game.player.canMove
    public Action displayTextAction;
    Vector2 currScreen;
    boolean debugInputEnabled = false;  // pass 'dev' arg to enable this.
    Player player;
    PkmnMap map;
    Battle battle;
    // Try this for overworld music, etc
    // May have to replace this with a string
    public Music currMusic;
//    Music.OnCompletionListener musicCompletionListener;  // TODO: remove
    MusicController musicController = null;
    // When want to play a music file, put in here. Call dispose and remove elements when done.
    HashMap<String, Music> loadedMusic =  new HashMap<String, Music>();
    // Char-to-Sprite text dictionary
    Map<Character, SpriteProxy> textDict;
    HashMap<Character, SpriteProxy> transparentDict = new HashMap<Character, SpriteProxy>();
    // Server uses this to keep track of all players currently in the game.
    // playerId->Player
    HashMap<String, Player> players = new HashMap<String, Player>();
    // Server determines outcome of all actions done in battle
    HashMap<String, Battle> battles = new HashMap<String, Battle>();
    FileWriter logFile;
//    Action fadeMusicAction = null;
    // Network
    public Client client;
    public Server server;
    Type type;
    public static Random rand = new Random();
    public Thread gameThread;
    //
    DrawCampfireAuras drawCampfireAuras;
    Texture shadow;
    public SpriteBatch lightingBatch;
    public FrameBuffer frameBuffer;  // TODO: I don't think this is used.
    // TODO: in the future, advanced option to toggle this (?) idk.
    //       would have to be saved with the map I think
    public boolean levelScalingEnabled = true; 
    public static boolean fairyTypeEnabled = false; 

    HashMap<Character, SpriteProxy> brailleDict = new HashMap<Character, SpriteProxy>();

    public Game() {
        super();
    }

    public Game(String[] args) {
        this();
        for (int i=0; i < args.length; i++) {
            if (args[i].equals("dev")) {
                this.debugInputEnabled = true;
            }
        }
    }

    @Override
    public void create() {
        // Annoying - used for music completion listener
        Game.staticGame = this;
        this.gameThread = Thread.currentThread();

        this.mapBatch = new ProxyBatch(); //new SpriteBatch();  <-- does this need to be a proxyBatch? not sure
        this.uiBatch = new ProxyBatch();
        this.frameBuffer = new FrameBuffer(Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        this.lightingBatch = new SpriteBatch();
//        this.lightingBatch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_SRC_ALPHA);  // TODO: mess with this
//        this.lightingBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_BLEND_COLOR); 
//        this.lightingBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
//        this.lightingBatch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
//        this.lightingBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_DST_COLOR);
        // TODO: debug
//        this.lightingBatch.setColor(new Color(0.08f, 0.08f, 0.3f, 1.0f));
//        this.lightingBatch.setColor(new Color(0f, 0f, 0f, 1.0f));
        this.drawCampfireAuras = new DrawCampfireAuras(this);
        this.shadow = new Texture(Gdx.files.internal("shadow2.png"));

        // This will force window to contain gameboy-equivalent pixels
        this.cam = new OrthographicCamera(160, 144);
        this.cam.position.set(16, 0, 0);
        this.cam.zoom = 1;

        // TODO: should be disabled since it doesn't work with html5/webgl...
        // I'm still using it in Player.java tho.
        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = 10;
        this.font = gen.generateFont(parameter);
        this.font.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
        this.font.setColor(Color.BLACK);
        this.font.setScale(0.5f, 0.5f);

        this.player = new Player();
        this.battle = new Battle();
        // TODO: remove
//        this.map = new PkmnMap("default");
        this.textDict = initTextDict();
        this.initTransparentDict();
        this.initBrailleDict();

        this.insertAction(new InputProcessor());  // Mux keyboard/mobile input to common button presses
        if (Gdx.app.getType() == ApplicationType.Android) {
            this.insertAction(new DrawMobileControls(this));
        }
        this.insertAction(new DrawSetupMenu(this, null));

        Gdx.gl.glClearColor(1, 1, 1, 1);
//        Gdx.gl.glClearColor(0, 0, 0, 1);
    }

    @Override
    public void dispose() {
        if (this.logFile != null) {
            // These constant try/catches are such a joy
            try {
                this.logFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mapBatch.dispose();
        uiBatch.dispose();
        lightingBatch.dispose();
        for (Music music : this.loadedMusic.values()){
            music.dispose();
        }
        // Save game
        if (this.map != null) {
            // TODO: maybe remove, not sure
//            new PkmnMap.PeriodicSave(this).step(this);

            // Display option to save if it's been 20 seconds or more
            // Trying to allow shiny hunters to reset and not be badgered
            if (PeriodicSave.timeSinceLastSave >= 60*20) {
//                JFrame frame = new JFrame("Save");
//                JOptionPane.showMessageDialog(frame, "There was an error loading this file - it may be too old for this build.\nPlease create a bug at github.com/SheerSt/pokemon-wilds.");
                if (JOptionPane.showConfirmDialog(null, "Save your progress?", "WARNING",
                                                  JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    new PkmnMap.PeriodicSave(this).step(this);
                }
            }
        }
        if (this.server != null) {
            this.server.close();
        }
        if (this.type == Game.Type.CLIENT) {
            Network.Logout logoutPlayer = new Network.Logout(this.player.network.id);
            this.client.sendTCP(logoutPlayer);
        }
    }

    /**
     * Handle keyboard input to be used for debug-related things (screenshot, move/zoom camera, print actionStack).
     */
    private void handleDebuggingInput() {
        if(Gdx.input.isKeyPressed(Input.Keys.Q)) {
            if (cam.zoom < 8) {
                cam.zoom += 0.5;
            }
        }
        if(Gdx.input.isKeyPressed(Input.Keys.E)) {
            if (cam.zoom > 1) {
                cam.zoom -= 0.5;
            }
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
        if(Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            CycleDayNight.dayTimer = 100;
        }
        // Print action stack in layer-order
        if(Gdx.input.isKeyPressed(Input.Keys.P)) {
            System.out.println("Layer, Name");
            for (Action action : this.actionStack) {
                System.out.println(String.valueOf(action.getLayer()) + "  " + action.getClass().getName());
            }
            System.out.println("Time of day: " + this.map.timeOfDay + " " + String.valueOf(CycleDayNight.dayTimer));
//            System.out.println("player pokemon");
//            for (Player player : this.players.values()) {
//                System.out.println("curr pokemon:" + player.currPokemon.name);
//                for (Pokemon pokemon : player.pokemon) {
//                    System.out.println(pokemon.name + " hp: " + pokemon.currentStats.get("hp"));
//                    for (String type : pokemon.types) {
//                        System.out.println(type);
//                    }
//                }
//            }
            for (Pokemon pokemon : this.player.pokemon) {
                System.out.println(pokemon.previousOwner.name);
            }
//            System.out.println("oppPokemon attacks:");
//            for (String attack : this.battle.oppPokemon.attacks) {
//                System.out.println(attack);
//            }

//            // Debug related to stat stages
//            if (this.battle.oppPokemon != null) {
//                System.out.println("oppPokemon stats:");
//                System.out.println("attack");
//                System.out.println(this.battle.oppPokemon.statStages.get("attack"));
//                System.out.println(this.battle.oppPokemon.currentStats.get("attack"));
//                System.out.println("defense");
//                System.out.println(this.battle.oppPokemon.statStages.get("defense"));
//                System.out.println(this.battle.oppPokemon.currentStats.get("defense"));
//                System.out.println("specialAtk");
//                System.out.println(this.battle.oppPokemon.statStages.get("specialAtk"));
//                System.out.println(this.battle.oppPokemon.currentStats.get("specialAtk"));
//                System.out.println("specialDef");
//                System.out.println(this.battle.oppPokemon.statStages.get("specialDef"));
//                System.out.println(this.battle.oppPokemon.currentStats.get("specialDef"));
//                System.out.println("speed");
//                System.out.println(this.battle.oppPokemon.statStages.get("speed"));
//                System.out.println(this.battle.oppPokemon.currentStats.get("speed"));
//                System.out.println("accuracy");
//                System.out.println(this.battle.oppPokemon.statStages.get("accuracy"));
//                System.out.println(this.battle.oppPokemon.currentStats.get("accuracy"));
//                System.out.println("evasion");
//                System.out.println(this.battle.oppPokemon.statStages.get("evasion"));
//                System.out.println(this.battle.oppPokemon.currentStats.get("evasion"));
//            }
//            if (this.player.currPokemon != null) {
//                System.out.println("currPokemon stats:");
//                System.out.println("attack");
//                System.out.println(this.player.currPokemon.statStages.get("attack"));
//                System.out.println(this.player.currPokemon.currentStats.get("attack"));
//                System.out.println("defense");
//                System.out.println(this.player.currPokemon.statStages.get("defense"));
//                System.out.println(this.player.currPokemon.currentStats.get("defense"));
//                System.out.println("specialAtk");
//                System.out.println(this.player.currPokemon.statStages.get("specialAtk"));
//                System.out.println(this.player.currPokemon.currentStats.get("specialAtk"));
//                System.out.println("specialDef");
//                System.out.println(this.player.currPokemon.statStages.get("specialDef"));
//                System.out.println(this.player.currPokemon.currentStats.get("specialDef"));
//                System.out.println("speed");
//                System.out.println(this.player.currPokemon.statStages.get("speed"));
//                System.out.println(this.player.currPokemon.currentStats.get("speed"));
//                System.out.println("accuracy");
//                System.out.println(this.player.currPokemon.statStages.get("accuracy"));
//                System.out.println(this.player.currPokemon.currentStats.get("accuracy"));
//                System.out.println("evasion");
//                System.out.println(this.player.currPokemon.statStages.get("evasion"));
//                System.out.println(this.player.currPokemon.currentStats.get("evasion"));
//            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.PAGE_UP)) {
            this.map.interiorTilesIndex += 1;
            this.map.tiles = this.map.interiorTiles.get(this.map.interiorTilesIndex);
        }
        else if (Gdx.input.isKeyJustPressed(Input.Keys.PAGE_DOWN)) {
            this.map.interiorTilesIndex -= 1;
            this.map.tiles = this.map.interiorTiles.get(this.map.interiorTilesIndex);
        }
        // Check network type (reset when pressed)
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            for (Action action : new ArrayList<Action>(this.actionStack)) {
                if (DrawSetupMenu.class.isInstance(action)) {
                    this.actionStack.remove(action);
                }
            }
            this.start();
            // set up networking
            try {
                this.initServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.map.loadFromFile(this);  // load map if it exists already
            this.insertAction(new ServerBroadcast(this));
            this.insertAction(new PkmnMap.PeriodicSave(this));
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.N)) {
            for (Action action : new ArrayList<Action>(this.actionStack)) {
                if (DrawSetupMenu.class.isInstance(action)) {
                    this.actionStack.remove(action);
                }
            }
            this.start();
            // set up networking
            try {
                this.initClient("127.0.0.1");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            for (Tile tile : this.map.overworldTiles.values()) {
                if (tile.nameUpper.equals("pokemon_mansion_key")) {
                    System.out.println(this.cam.position);
                    System.out.println(tile.position);
                    this.cam.position.set(tile.position.x, tile.position.y, 1.0f);
                    break;
                }
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.O)) {
            for (Tile tile : this.map.overworldTiles.values()) {
//                if (tile.nameUpper.equals("pokemon_mansion_key")) {
//                    System.out.println(this.cam.position);
//                    System.out.println(tile.position);
//                    this.cam.position.set(tile.position.x, tile.position.y, 1.0f);
//                    break;
//                }
                if (tile.name.equals("cave1_door1")) {
                    System.out.println(this.cam.position);
                    System.out.println(tile.position);
                    this.cam.position.set(tile.position.x, tile.position.y, 1.0f);
                    break;
                }
            }
        }
        // Screenshot
        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            try {
                // Find map corners
                Vector2 tl = null;
                Vector2 br = null;
                for (Tile tile : this.map.tiles.values()) {
                    if (tl == null) {
                        tl = tile.position.cpy();
                    }
                    if (br == null) {
                        br = tile.position.cpy();
                    }
                    if (tile.position.x < tl.x) {
                        tl.x = tile.position.x;
                    }
                    else if (tile.position.x > br.x) {
                        br.x = tile.position.x;
                    }
                    if (tile.position.y < br.y) {
                        br.y = tile.position.y;
                    }
                    else if (tile.position.y > tl.y) {
                        tl.y = tile.position.y;
                    }
                }
                System.out.println("Creating screenshot of full map...");
                System.out.println((int)(br.x-tl.x)+16);
                System.out.println((int)(tl.y-br.y)+16);
                Pixmap pixmap;
                while (true) {
                    try {
                        pixmap = new Pixmap((int)(br.x-tl.x)+16, (int)(tl.y-br.y)+16, Pixmap.Format.RGBA8888);
                        break;
                    }
                    catch(Exception e) {
                        br.x -= 16;
                        br.y += 16;
                        tl.x += 16;
                        tl.y -= 16;
                    }
                }
                // Draw all tiles onto the pixmap from top-left to bottom-right
                for (Vector2 currPos = tl.cpy(); currPos.y >= br.y-16; currPos.x += 16) {
                    if (currPos.x > br.x+16) {
                        currPos.x = tl.x-16;
                        currPos.y -= 16;
                        continue;
                    }
                    Tile currTile = this.map.tiles.get(currPos);
                    if (currTile == null) {
                        continue;
                    }
                    // Draw current tile onto the pixmap
                    TextureData temp = currTile.sprite.getTexture().getTextureData();
                    if (!temp.isPrepared()) {
                        temp.prepare();
                    }
                    Pixmap currPixmap = temp.consumePixmap();
                    pixmap.drawPixmap(currPixmap, (int)(currPos.x-tl.x), (int)(tl.y-currPos.y)+(16-(int)currPixmap.getHeight()));
                }

                // Draw all tiles onto the pixmap from top-left to bottom-right
                for (Vector2 currPos = tl.cpy(); currPos.y >= br.y-16; currPos.x += 16) {
                    if (currPos.x > br.x+16) {
                        currPos.x = tl.x-16;
                        currPos.y -= 16;
                        continue;
                    }
                    Tile currTile = this.map.tiles.get(currPos);
                    if (currTile == null) {
                        continue;
                    }
                    if (currTile.nameUpper.equals("solid")) {  // this gets drawn over oversprites
                        continue;
                    }
                    if (currTile.overSprite == null) {
                        continue;
                    }
                    TextureData temp = currTile.overSprite.getTexture().getTextureData();
                    if (!temp.isPrepared()) {
                        temp.prepare();
                    }
                    Pixmap currPixmap = temp.consumePixmap();
                    pixmap.drawPixmap(currPixmap, (int)(currPos.x-tl.x), (int)(tl.y-currPos.y)+(16-(int)currPixmap.getHeight()));
                }

                FileHandle file = new FileHandle("screenshot1.png");
                PixmapIO.writePNG(file, pixmap);
                System.out.println("Done.");
            } catch (GdxRuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Initialize Client and start behaving like one.
     * @throws IOException
     */
    public void initClient(String ip) throws IOException {
        if (this.client != null) {
            this.client.close();
        }
        this.client = new Client();
        this.type = Game.Type.CLIENT;
        Network.register(client);
        this.client.start();

        try {
            this.client.connect(5000, ip, Network.port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // TODO: handle login differently.
//        if (this.player.name == "") {
//            // why did I do this?
//        }
//        else {
        this.player.network.id = this.player.name;
//        }
        this.player.type = Player.Type.LOCAL;
        // Clear map tiles because the Server is going to send over tiles from it's map.
        this.map.tiles.clear();
        this.client.sendTCP(new Network.Login(this.player.network.id,
                                              this.player.color));  // Specify which color player chose during setup.
        this.insertAction(new ClientBroadcast(this));
    }

    /**
     * Initialize Server and start accepting connections from Clients.
     * @throws IOException
     */
    public void initServer() throws IOException {
        // TODO: CharacterConnection probably not necessary, remove.
        this.server = new Server();
        Network.register(this.server);
        this.type = Game.Type.SERVER;
        this.server.bind(Network.port);
        this.server.start();
        while (this.server.getUpdateThread() == null) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // TODO: remove if unused
//        this.map.loadFromFile(this);  // load map if it exists already
//        this.insertAction(new ServerBroadcast(this));
//        this.insertAction(new PkmnMap.PeriodicSave(this));
    }

    public void initTransparentDict() {
        // This sheet starts with an offset of x=5, y=1
        Texture text = new Texture(Gdx.files.internal("text_sheet1_transparent.png"));
        char[] alphabet_upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        char[] alphabet_lower = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        // Uppercase letters
        for (int i = 0; i < 26; i++) {
            this.transparentDict.put(alphabet_upper[i], new SpriteProxy(text, 10+16*i, 5, 8, 8));
        }
        // Lowercase letters
        for (int i = 0; i < 26; i++) {
            this.transparentDict.put(alphabet_lower[i], new SpriteProxy(text, 10+16*i, 5+12, 8, 8));
        }
        // Numbers
        for (int i = 0; i < 10; i++) {
            this.transparentDict.put(Character.forDigit(i, 10), new SpriteProxy(text, 10+16*i, 5+12+12, 8, 8));
        }
        // Special chars
        this.transparentDict.put(' ', new SpriteProxy(text, 10+16*10, 5+12+12, 8, 8));
        this.transparentDict.put('_', new SpriteProxy(text, 10+16*2, 5+12+12+12, 8, 8));
        this.transparentDict.put('?', new SpriteProxy(text, 10+16*3, 5+12+12+12, 8, 8));
        this.transparentDict.put('!', new SpriteProxy(text, 10+16*4, 5+12+12+12, 8, 8));
        this.transparentDict.put('.', new SpriteProxy(text, 10+16*7, 5+12+12+12, 8, 8));
        this.transparentDict.put(',', new SpriteProxy(text, 10+16*8, 5+12+12+12, 8, 8));
        this.transparentDict.put('é', new SpriteProxy(text, 10+16*9, 5+12+12+12, 8, 8));
        this.transparentDict.put('É', new SpriteProxy(text, 10+16*9, 5+12+12+12, 8, 8));  // same as lower case é, used in menus (ie POKéBALL, etc)
        this.transparentDict.put('-', new SpriteProxy(text, 10+16*10, 5+12+12+12, 8, 8));
        this.transparentDict.put('\'', new SpriteProxy(text, 10+16*11, 5+12+12+12, 8, 8));
        this.transparentDict.put('ì', new SpriteProxy(text, 10+16*12, 5+12+12+12, 8, 8));
        this.transparentDict.put(null, new SpriteProxy(text, 10+16*0, 5+12+12+12+12, 8, 8));  // use when no char found
    }

    public void initBrailleDict() {
            Texture text = new Texture(Gdx.files.internal("braille_sheet1.png"));
            char[] alphabet_upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
            char[] alphabet_lower = "abcdefghijklmnopqrstuvwxyz".toCharArray();
            // Uppercase letters
            for (int i = 0; i < 26; i++) {
                brailleDict.put(alphabet_upper[i], new SpriteProxy(text, 10+16*i, 5, 8, 8));
            }
            // Lowercase letters
            for (int i = 0; i < 26; i++) {
                brailleDict.put(alphabet_lower[i], new SpriteProxy(text, 10+16*i, 5, 8, 8));
            }
            // TODO: probably remove
//            // Numbers
//            for (int i = 0; i < 10; i++) {
//                brailleDict.put(Character.forDigit(i, 10), new SpriteProxy(text, 10+16*i, 5+12+12, 8, 8));
//            }
//            // Special chars
//            brailleDict.put(' ', new SpriteProxy(text, 10+16*10, 5+12+12, 8, 8));
//            brailleDict.put('_', new SpriteProxy(text, 10+16*2, 5+12+12+12, 8, 8));
//            brailleDict.put('?', new SpriteProxy(text, 10+16*3, 5+12+12+12, 8, 8));
//            brailleDict.put('!', new SpriteProxy(text, 10+16*4, 5+12+12+12, 8, 8));
//            brailleDict.put('.', new SpriteProxy(text, 10+16*7, 5+12+12+12, 8, 8));
//            brailleDict.put(',', new SpriteProxy(text, 10+16*8, 5+12+12+12, 8, 8));
//            brailleDict.put('é', new SpriteProxy(text, 10+16*9, 5+12+12+12, 8, 8));
//            brailleDict.put('É', new SpriteProxy(text, 10+16*9, 5+12+12+12, 8, 8));  // same as lower case é, used in menus (ie POKéBALL, etc)
//            brailleDict.put('-', new SpriteProxy(text, 10+16*10, 5+12+12+12, 8, 8));
//            brailleDict.put('\'', new SpriteProxy(text, 10+16*11, 5+12+12+12, 8, 8));
//            brailleDict.put('ì', new SpriteProxy(text, 10+16*12, 5+12+12+12, 8, 8));
//            brailleDict.put(null, new SpriteProxy(text, 10+16*0, 5+12+12+12+12, 8, 8));  // use when no char found
    }

    /**
     * Create a Map<Character, Sprite> to be used when drawing text to the screen.
     * @return Map<Character, Sprite>
     * @see DisplayText
     */
    public Map<Character, SpriteProxy> initTextDict() {
        Map<Character, SpriteProxy> textDict = new HashMap<Character, SpriteProxy>();
        // This sheet starts with an offset of x=5, y=1
        Texture text = new Texture(Gdx.files.internal("text_sheet1.png"));
        char[] alphabet_upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        char[] alphabet_lower = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        // Uppercase letters
        for (int i = 0; i < 26; i++) {
            textDict.put(alphabet_upper[i], new SpriteProxy(text, 10+16*i, 5, 8, 8));
        }
        // Lowercase letters
        for (int i = 0; i < 26; i++) {
            textDict.put(alphabet_lower[i], new SpriteProxy(text, 10+16*i, 5+12, 8, 8));
        }
        // Numbers
        for (int i = 0; i < 10; i++) {
            textDict.put(Character.forDigit(i, 10), new SpriteProxy(text, 10+16*i, 5+12+12, 8, 8));
        }
        // Special chars
        textDict.put(' ', new SpriteProxy(text, 10+16*10, 5+12+12, 8, 8));
        textDict.put('_', new SpriteProxy(text, 10+16*2, 5+12+12+12, 8, 8));
        textDict.put('?', new SpriteProxy(text, 10+16*3, 5+12+12+12, 8, 8));
        textDict.put('!', new SpriteProxy(text, 10+16*4, 5+12+12+12, 8, 8));
        textDict.put('.', new SpriteProxy(text, 10+16*7, 5+12+12+12, 8, 8));
        textDict.put(',', new SpriteProxy(text, 10+16*8, 5+12+12+12, 8, 8));
        textDict.put('é', new SpriteProxy(text, 10+16*9, 5+12+12+12, 8, 8));
        textDict.put('É', new SpriteProxy(text, 10+16*9, 5+12+12+12, 8, 8));  // same as lower case é, used in menus (ie POKéBALL, etc)
        textDict.put('-', new SpriteProxy(text, 10+16*10, 5+12+12+12, 8, 8));
        textDict.put('\'', new SpriteProxy(text, 10+16*11, 5+12+12+12, 8, 8));
        textDict.put('ì', new SpriteProxy(text, 10+16*12, 5+12+12+12, 8, 8));
        textDict.put(null, new SpriteProxy(text, 10+16*0, 5+12+12+12+12, 8, 8));  // use when no char found
        return textDict;
    }

    /**
     * Insert action into this.actionStack at the layer defined by actionToInsert.getLayer().
     */
    public void insertAction(Action actionToInsert) {
        // Handle null entry by skipping
        if (actionToInsert == null) {
            return;
        }
        for (int i = 0; i < this.actionStack.size(); i++) {
            // If actionToInsert layer is <=  action layer, insert before element
            if (actionToInsert.getLayer() >= this.actionStack.get(i).getLayer()) {
                this.actionStack.add(i, actionToInsert);
                return;
            }
        }
        // Layer is smallest in stack, so add to the end.
        this.actionStack.add(actionToInsert);
    }

    @Override
    public void pause() {
    }

    @Override
    public void render() {
        if (this.debugInputEnabled) {
            this.handleDebuggingInput();
        }
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        this.cam.update();

//        // Handles time of day lighting and campfire 'aura' effect
//        // TODO: no idea if this can 'darken' everything or not. You'd think it should be
//        //       able to.
//        this.frameBuffer.begin();
//        this.lightingBatch.begin();
//        this.lightingBatch.setProjectionMatrix(new Matrix4());
//        this.lightingBatch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
//        this.lightingBatch.draw(this.shadow, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//        this.lightingBatch.setProjectionMatrix(cam.combined);
//        this.lightingBatch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_SRC_ALPHA); 
//        this.drawCampfireAuras.step(this);
//        this.lightingBatch.end();
//        this.frameBuffer.end();
        

        this.mapBatch.setProjectionMatrix(cam.combined);
        // Iterate through this.actionStack twice, once for this.batch (map objects), once for this.uiBatch (ui objects)
        this.mapBatch.begin();
        // Iterate through the action stack and call the step() fn of each Action.
        for (Action action : new ArrayList<Action>(this.actionStack)) {
            // TODO: what is causing this?
            /// Something to do with displaytext.
            if (action == null) {
                continue;
            }
            if (action.getCamera().equals("map")) {
                try {
                    if (action.firstStep) {
                        action.firstStep(this);
                        action.firstStep = false;
                    }
                    action.step(this);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    if (this.logFile != null) {
                        try {
                            this.logFile.append(e.toString());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }
        this.mapBatch.end();

        this.uiBatch.begin();
//        this.uiBatch.draw(this.frameBuffer.getColorBufferTexture(), 0, 0);  // TODO: test
        // Iterate through the action stack and call the step() fn of each Action.
        for (Action action : new ArrayList<Action>(this.actionStack)) {
            // TODO: what is causing this?
            if (action == null) {
                continue;
            }
            try {
                if (action.getCamera().equals("gui")) {
                    if (action.firstStep) {
                        action.firstStep(this);
                        action.firstStep = false;
                    }
                    action.step(this);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                System.out.println(action);
                if (this.logFile != null) {
                    try {
                        this.logFile.append(e.toString());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
//        font.draw(this.uiBatch, "input: " + new Vector2(Gdx.input.getX(), Gdx.input.getY()), 0, 20);
//        font.draw(this.uiBatch, "curr dims: " + this.currScreen, 0, 30);
//        font.draw(this.uiBatch, "ex rect: " + DrawMobileControls.upArrowSprite.getBoundingRectangle(), 0, 40);
//        font.draw(this.uiBatch, "touchLoc: " + InputProcessor.touchLoc, 0, 10);

        this.uiBatch.end();

        // Just do this manually after the frame
        if (PeriodicSave.timeSinceLastSave <= 20*60) {
            PeriodicSave.timeSinceLastSave++;
        }
    }

    @Override
    public void resize(int width, int height) {
        // TODO: probably just check if width > height instead of application type
        if (Gdx.app.getType() == ApplicationType.Android) {
            // TODO: haven't tested the android resizing yet
            int menuHeight = (160*height)/width;  // height/width = menuHeight/160
            int offsetY = (menuHeight-144)/2;
            this.uiBatch.getProjectionMatrix().setToOrtho2D(0, -offsetY, 160, menuHeight);
//            Gdx.input.setOnscreenKeyboardVisible(true);
            this.cam.viewportWidth = 160;
            this.cam.viewportHeight = menuHeight;
        }
        else {
            // Below will scale floatingBatch regardless of current screen size
            // This might not be technically the best method; works for now.
            int menuWidth = (144*width)/height;  // height/width = 144/menuWidth
            int offsetX = (menuWidth-160)/2;
            this.uiBatch.getProjectionMatrix().setToOrtho2D(-offsetX, 0, menuWidth, 144);
            this.cam.viewportWidth = width/3;
            this.cam.viewportHeight = height/3;
        }
        this.currScreen = new Vector2(width, height);
    }

    @Override
    public void resume() {
    }

    /**
     * Do things required at the start of the game (draw map, draw player, start music, etc).
     */
    public void start() {
        this.insertAction(new DrawMap(this));
        this.insertAction(new DrawMapGrass(this));
        // TODO: test
        // if player is hosting, no moving player
        if (this.type != Game.Type.SERVER) {
            this.insertAction(new PlayerStanding(this));
            this.insertAction(new DrawPlayerLower(this));
            this.insertAction(new DrawPlayerUpper(this));
            this.insertAction(new DrawBuildRequirements());
        }
        this.insertAction(new MusicController(this));
        this.insertAction(new DrawMapTrees(this));  // Draw tops of trees over player
        this.insertAction(new MoveWater(this));  // Move water tiles around

//        if (this.type != Game.Type.SERVER) {
//            // This will 'radio' through a selection of musics for the map (based on current route)
//            this.currMusic = Gdx.audio.newMusic(Gdx.files.internal("music/nature1_render.ogg"));
////            this.currMusic = Gdx.audio.newMusic(Gdx.files.internal("music/overw3.ogg"));
//            this.map.currRoute.music = this.currMusic;
//            this.currMusic.setLooping(false);
//            this.currMusic.setVolume(1f);
//            // TODO: not sure if deleting or not
//            this.currMusic.play();
//            this.currMusic.pause();
//            this.currMusic.setPosition(130f);  
//            this.currMusic.play();
//            this.musicCompletionListener = new Music.OnCompletionListener() {
//                @Override
//                public void onCompletion(Music aMusic) {
//                    String nextMusicName = Game.staticGame.map.currRoute.getNextMusic(true);
//                    // TODO: would it be good to also accept music instance here?
//                    // TODO: These fade-ins don't even work. Will need for route musics
//                    // TODO: there's def a bug here if you run into a wild
//                    // pokemon while waiting frames, the next music will start anyway
//                    Action nextMusic = new FadeMusic("currMusic", "out", "", 0.025f,
//                                       new WaitFrames(Game.staticGame, 360,
//                                       new FadeMusic(nextMusicName, "in", "", 0.2f, true, 1f, this,
//                                       null)));
//                    Game.staticGame.insertAction(nextMusic);
//                    nextMusic.step(Game.staticGame);
//                    Game.staticGame.fadeMusicAction = nextMusic;
//                }
//            };
//            this.currMusic.setOnCompletionListener(this.musicCompletionListener);
//        }
        this.playerCanMove = true;

        // This is the special mewtwo battle debug map
        // Comment out the GenIsland1 above to use this
//        this.map = new PkmnMap("SpecialMewtwo");
//        this.insertAction(new DrawSpecialMewtwoBg());

        this.insertAction(new CycleDayNight(this));

        // If you join a game as a Client, these go away, so only affects local play.
        this.player.pokemon.add(new Pokemon("Machop", 6));
        if (this.debugInputEnabled) {
            // Some starting pokemon used for debugging
//            this.player.pokemon.get(0).currentStats.put("hp", 1);
//            this.player.pokemon.get(0).attacks[2] = "recover";
//            this.player.pokemon.get(0).attacks[3] = "slash";
            this.player.pokemon.add(new Pokemon("rapidash", 60));
//            this.player.pokemon.get(1).attacks[0] = "crush grip";
//            this.player.pokemon.get(1).attacks[1] = "dragon energy";
//            this.player.pokemon.get(1).attacks[2] = "thunder cage";
//            this.player.pokemon.get(1).attacks[1] = "confuse ray";
//            this.player.pokemon.get(1).attacks[2] = "toxic";
//            this.player.pokemon.get(1).attacks[3] = "sweet scent";
//            this.player.pokemon.add(new Pokemon("pidgeot", 60, Pokemon.Generation.CRYSTAL));
//            this.player.pokemon.add(new Pokemon("meganium", 60, Pokemon.Generation.CRYSTAL));
//            this.player.pokemon.add(new Pokemon("ursaring", 60, Pokemon.Generation.CRYSTAL));
//            this.player.pokemon.add(new Pokemon("golem", 60, Pokemon.Generation.CRYSTAL));
//            this.player.pokemon.get(1).currentStats.put("hp", 0);
//            this.player.pokemon.get(2).currentStats.put("hp", 0);
//            this.player.pokemon.get(3).currentStats.put("hp", 0);

//            this.player.pokemon.add(new Pokemon("loudred", 60, Pokemon.Generation.CRYSTAL));
//            this.player.pokemon.add(new Pokemon("loudred", 60, Pokemon.Generation.CRYSTAL));
//            this.player.pokemon.get(3).gender = "male";
//            this.player.pokemon.get(4).gender = "female";
//            this.player.pokemon.add(new Pokemon("houndoom", 66, Pokemon.Generation.CRYSTAL));
//            this.player.pokemon.add(new Pokemon("registeel", 40, Pokemon.Generation.CRYSTAL));
//            this.player.pokemon.add(new Pokemon("egg", 1, Pokemon.Generation.CRYSTAL, false, "skarmory"));
//            this.player.pokemon.add(new Pokemon("registeel", 40, Pokemon.Generation.CRYSTAL, true));
//            this.player.pokemon.add(new Pokemon("masquerain", 60, Pokemon.Generation.CRYSTAL));
            
            
            this.player.pokemon.add(new Pokemon("ampharos", 70, Pokemon.Generation.CRYSTAL));
            this.player.pokemon.add(new Pokemon("rhydon", 46, Pokemon.Generation.CRYSTAL));
            this.player.pokemon.add(new Pokemon("meganium", 46, Pokemon.Generation.CRYSTAL));
//            this.player.pokemon.add(new Pokemon("ursaring", 46, Pokemon.Generation.CRYSTAL));
            
//            this.player.pokemon.add(new Pokemon("mimikyu", 46, Pokemon.Generation.CRYSTAL, true, false));
//            this.player.pokemon.add(new Pokemon("ribombee", 46, Pokemon.Generation.CRYSTAL, true, false));
            this.player.pokemon.add(new Pokemon("sigilyph", 46, Pokemon.Generation.CRYSTAL, true, false));
            this.player.pokemon.get(2).attacks[0] = "false swipe";

            // TODO: remove
//            this.player.pokemon.add(new Pokemon("charizard", 60, Pokemon.Generation.CRYSTAL));
//            this.player.pokemon.add(new Pokemon("charizard", 60, Pokemon.Generation.CRYSTAL));
//            this.player.pokemon.add(new Pokemon("unown", 60, Pokemon.Generation.CRYSTAL));
//            this.player.pokemon.get(4).gender = "male";
//            this.player.pokemon.get(5).gender = "female";

//            this.player.pokemon.add(new Pokemon("egg", 1, Pokemon.Generation.CRYSTAL, false, "skarmory"));
//            this.player.pokemon.add(new Pokemon("egg", 1, Pokemon.Generation.CRYSTAL, true, "skarmory"));
//            this.player.pokemon.get(4).happiness = 1;  // test egg cycle hatching works
//            this.player.pokemon.add(new Pokemon("egg", 1, Pokemon.Generation.CRYSTAL, false, "skarmory"));
//            this.player.pokemon.get(5).happiness = 1;  // test egg cycle hatching works
            Log.set(Log.LEVEL_DEBUG);
        }
        for (Pokemon pokemon : this.player.pokemon) {
            pokemon.previousOwner = this.player;
        }

//        this.player.pokemon.add(new Pokemon("Celebi", 6, Pokemon.Generation.CRYSTAL));
//        this.player.pokemon.add(new Pokemon("Mareep", 6, Pokemon.Generation.CRYSTAL));
        // TODO: remove
//        this.player.currPokemon = this.player.pokemon.get(0);
        // The first Pokemon the player sends out in battle should
        // have >0 hp.
        for (Pokemon currPokemon : this.player.pokemon) {
            if (currPokemon.currentStats.get("hp") > 0) {
                this.player.currPokemon = currPokemon;
                break;
            }
        }

        // TODO: debug, remove
//        this.player.currPokemon.currentStats.put("hp", 10);
    }

    public static class SetCamPos extends Action {
        Vector2 pos;

        @Override
        public void step(Game game) {
            game.cam.position.set(this.pos.x, this.pos.y, 0);
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
        }
        
        public SetCamPos(Vector2 pos, Action nextAction) {
            this.pos = pos;
            this.nextAction = nextAction;
        }
    }

    enum Type {
        CLIENT,
        SERVER
    }
}
