package com.pkmngen.game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Server;

/**
 * Standard libGDX Game biolerplate extended for Pokemon Wilds.
 * 
 * @author  SheerSt on github
 * @version 0.1
 * @since   2020-06-11
 */
public class Game extends ApplicationAdapter {
    public static Game staticGame;  // Annoying - used by music completion listener
    public ArrayList<Action> actionStack = new ArrayList<Action>();
    public SpriteBatch mapBatch;
    public SpriteBatch uiBatch;
    public BitmapFont font;
    public OrthographicCamera cam;
    Vector3 touchLoc = new Vector3();
    boolean playerCanMove;  // TODO: migrate to game.player.canMove
    Action displayTextAction;
    Vector2 currScreen;
    Player player;
    PkmnMap map;
    Battle battle;
    // Try this for overworld music, etc
    // May have to replace this with a string
    Music currMusic;
    Music.OnCompletionListener musicCompletionListener;
    // When want to play a music file, put in here. Call dispose and remove elements when done.
    HashMap<String, Music> loadedMusic =  new HashMap<String, Music>();
    // Char-to-Sprite text dictionary
    Map<Character, Sprite> textDict;
    // Server uses this to keep track of all players currently in the game.
    // playerId->Player
    HashMap<String, Player> players = new HashMap<String, Player>();
    // Server determines outcome of all actions done in battle
    HashMap<String, Battle> battles = new HashMap<String, Battle>();
    // Network
    public Client client;
    public Server server;
    Type type;

    @Override
    public void create() {
        // Annoying - used for music completion listener
        Game.staticGame = this;

        this.mapBatch = new SpriteBatch();
        this.uiBatch = new SpriteBatch();

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
        this.map = new PkmnMap("default");

        this.textDict = initTextDict();
        this.insertAction(new DrawSetupMenu(this, null));
    }

    @Override
    public void dispose() {
        mapBatch.dispose();
        uiBatch.dispose();
        for (Music music : this.loadedMusic.values()){
            music.dispose();
        }
    }

    /**
     * Handle keyboard input to be used for debug-related things (screenshot, move/zoom camera, print actionStack).
     */
    private void handleDebuggingInput() {
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
        // Print action stack in layer-order
        if(Gdx.input.isKeyPressed(Input.Keys.P)) {
            System.out.println("Layer, Name");
            for (Action action : this.actionStack) {
                System.out.println(String.valueOf(action.getLayer()) + "  " + action.getClass().getName());
            }
        }
        // Check network type (reset when pressed)
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
                initClient("127.0.0.1");
            } catch (IOException e) {
                e.printStackTrace();
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
                Pixmap pixmap = new Pixmap((int)(br.x-tl.x), (int)(tl.y-br.y), Pixmap.Format.RGBA8888);
                // Draw all tiles onto the pixmap from top-left to bottom-right
                for (Vector2 currPos = tl.cpy(); currPos.y >= br.y; currPos.x += 16) {
                    if (currPos.x > br.x) {
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
                    if (currTile.overSprite == null) {
                        continue;
                    }
                    temp = currTile.overSprite.getTexture().getTextureData();
                    if (!temp.isPrepared()) {
                        temp.prepare();
                    }
                    currPixmap = temp.consumePixmap();
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
        if (this.player.name == "") {
        }
        else {
            this.player.network.id = this.player.name;
        }
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
        this.map.loadFromFile(this);  // load map if it exists already
        this.insertAction(new ServerBroadcast(this));
        this.insertAction(new PkmnMap.PeriodicSave(this));
    }

    /**
     * Create a Map<Character, Sprite> to be used when drawing text to the screen.
     * @return Map<Character, Sprite>
     * @see DisplayText
     */
    public Map<Character, Sprite> initTextDict() {
        Map<Character, Sprite> textDict = new HashMap<Character, Sprite>();
        // This sheet starts with an offset of x=5, y=1
        Texture text = new Texture(Gdx.files.internal("text_sheet1.png"));
        char[] alphabet_upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        char[] alphabet_lower = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        // Uppercase letters
        for (int i = 0; i < 26; i++) {
            textDict.put(alphabet_upper[i], new Sprite(text, 10+16*i, 5, 8, 8));
        }
        // Lowercase letters
        for (int i = 0; i < 26; i++) {
            textDict.put(alphabet_lower[i], new Sprite(text, 10+16*i, 5+12, 8, 8));
        }
        // Numbers
        for (int i = 0; i < 10; i++) {
            textDict.put(Character.forDigit(i,10), new Sprite(text, 10+16*i, 5+12+12, 8, 8));
        }
        // Special chars
        textDict.put(' ', new Sprite(text, 10+16*10, 5+12+12, 8, 8));
        textDict.put('_', new Sprite(text, 10+16*2, 5+12+12+12, 8, 8));
        textDict.put('?', new Sprite(text, 10+16*3, 5+12+12+12, 8, 8));
        textDict.put('!', new Sprite(text, 10+16*4, 5+12+12+12, 8, 8));
        textDict.put('.', new Sprite(text, 10+16*7, 5+12+12+12, 8, 8));
        textDict.put(',', new Sprite(text, 10+16*8, 5+12+12+12, 8, 8));
        textDict.put('é', new Sprite(text, 10+16*9, 5+12+12+12, 8, 8));
        textDict.put('É', new Sprite(text, 10+16*9, 5+12+12+12, 8, 8));  // same as lower case é, used in menus (ie POKéBALL, etc)
        textDict.put('-', new Sprite(text, 10+16*10, 5+12+12+12, 8, 8));
        textDict.put('\'', new Sprite(text, 10+16*11, 5+12+12+12, 8, 8));
        textDict.put('ì', new Sprite(text, 10+16*12, 5+12+12+12, 8, 8));
        textDict.put(null, new Sprite(text, 10+16*0, 5+12+12+12+12, 8, 8));  // use when no char found
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
        this.handleDebuggingInput();
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        this.cam.update();
        this.mapBatch.setProjectionMatrix(cam.combined);
        // Iterate through this.actionStack twice, once for this.batch (map objects), once for this.uiBatch (ui objects)
        this.mapBatch.begin();
        // Iterate through the action stack and call the step() fn of each Action.
        for (Action action : new ArrayList<Action>(this.actionStack)) {
            if (action.getCamera().equals("map")) {
                if (action.firstStep) {
                    action.firstStep(this);
                    action.firstStep = false;
                }
                action.step(this);
            }
        }
        this.mapBatch.end();
        this.uiBatch.begin();
        // Iterate through the action stack and call the step() fn of each Action.
        for (Action action : new ArrayList<Action>(this.actionStack)) {
            if (action.getCamera().equals("gui")) {
                if (action.firstStep) {
                    action.firstStep(this);
                    action.firstStep = false;
                }
                action.step(this);
            }
        }
        this.uiBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        if (Gdx.app.getType() == ApplicationType.Android) {
            // TODO: haven't tested the android resizing yet
            int menuHeight = (160*height)/width;  // height/width = menuHeight/160
            int offsetY = (menuHeight-144)/2;
            this.uiBatch.getProjectionMatrix().setToOrtho2D(0, -offsetY, 160, menuHeight);
        }
        else {
            // Below will scale floatingBatch regardless of current screen size
            int menuWidth = (144*width)/height;  // height/width = 144/menuWidth
            int offsetX = (menuWidth-160)/2;
            this.uiBatch.getProjectionMatrix().setToOrtho2D(-offsetX, 0, menuWidth, 144);
            // This might not be technically the best method; works for now.
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
        ArrayList<Action> startActions = new ArrayList<Action>();
        startActions.add(new DrawMap(this));
        startActions.add(new playerStanding(this));
        startActions.add(new DrawPlayerLower(this));
        startActions.add(new DrawMapGrass(this));
        startActions.add(new DrawPlayerUpper(this));
        startActions.add(new DrawMapTrees(this));  // Draw tops of trees over player
        startActions.add(new MoveWater(this));  // Move water tiles around
        this.actionStack.addAll(startActions);

        // This will 'radio' through a selection of musics for the map (based on current route)
        this.currMusic = Gdx.audio.newMusic(Gdx.files.internal("music/nature1_render.ogg"));
        this.map.currRoute.music = this.currMusic;
        this.currMusic.setLooping(false);
        this.currMusic.setVolume(1f);
        this.currMusic.play();
        this.currMusic.pause();
//        this.currMusic.setPosition(130f);  // TODO: debug, delete
        this.currMusic.play();
        this.musicCompletionListener = new Music.OnCompletionListener() {
            @Override
            public void onCompletion(Music aMusic) {
                String nextMusicName = Game.staticGame.map.currRoute.getNextMusic(true);
                // TODO: would it be good to also accept music instance here?
                // TODO: These fade-ins don't even work. Will need for route musics
                // TODO: there's def a bug here if you run into a wild
                // pokemon while waiting frames, the next music will start anyway
                Action nextMusic = new FadeMusic("currMusic", "out", "", 0.025f,
                                   new WaitFrames(Game.staticGame, 360,
                                   new FadeMusic(nextMusicName, "in", "", 0.2f, true, 1f, this,
                                   null)));
                Game.staticGame.insertAction(nextMusic);
                nextMusic.step(Game.staticGame);
            }
        };
        this.currMusic.setOnCompletionListener(this.musicCompletionListener);
        this.playerCanMove = true;

        // Note reg. full-res screenshot using pixmap:
        //  Tried 100*800, pixmap save didn't work.
        //  Tried 100*700, pixmap error loading tiles/water2.png
        this.insertAction(new GenIsland1(this, new Vector2(0, 0), 100*300)); // 100*500 // 100*180 // 100*100 // 20*30 // 60*100 // 100*120

        // This is the special mewtwo battle debug map
        // Comment out the GenIsland1 above to use this
//        this.map = new PkmnMap("SpecialMewtwo");
//        this.insertAction(new DrawSpecialMewtwoBg());

        this.insertAction(new CycleDayNight(this));

        // TODO: Some starting pokemon used for debugging
        // If you join a game as a Client, these go away, so only affects local play.
        this.player.pokemon.add(new Pokemon("Machop", 50, Pokemon.Generation.CRYSTAL));
//        this.battle.attacks.get("karate chop").power = 200;  // TODO: debug, remove
//        this.player.pokemon.add(new Pokemon("Cyndaquil", 50, Pokemon.Generation.CRYSTAL));
//        this.battle.attacks.get("flamethrower").power = 200;  // TODO: debug, remove
        this.player.pokemon.add(new Pokemon("sneasel", 50, Pokemon.Generation.CRYSTAL));
//        this.player.pokemon.get(1).attacks[0] = "Bubblebeam";  // TODO: debug, remove
//        this.player.pokemon.get(1).attacks[0] = "Ice Beam";  // TODO: debug, remove
        this.player.pokemon.add(new Pokemon("stantler", 50, Pokemon.Generation.CRYSTAL));
        this.player.currPokemon = this.player.pokemon.get(0);
    }

    enum Type {
        CLIENT,
        SERVER
    }
}
