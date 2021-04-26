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
import com.pkmngen.game.Network.PlayerDataBase;
import com.pkmngen.game.Network.PlayerDataV06;
import com.pkmngen.game.Pokemon.Burrowed;
import com.pkmngen.game.Pokemon.LedgeJump;
import com.pkmngen.game.Pokemon.Moving;
import com.pkmngen.game.Pokemon.Standing;
import com.pkmngen.game.util.LoadingZone;
import com.pkmngen.game.util.SpriteProxy;
import com.pkmngen.game.util.TextureCache;
import com.pkmngen.game.util.Direction;
import com.sun.xml.internal.bind.v2.runtime.Name;

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
            // Do nothing
        }
        else if (this.index == 13) {
            game.insertAction(new PlaySound("cut1", null));
        }
        else if (this.index < 19) {
            // Do nothing
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
            else if (name.equals("grass4")) {
                name = "green1";
            }
            else if (name.contains("bridge")) {
                name = name.split("_")[1];
                this.tile.overSprite = this.tile.sprite;  // hack to make anim work
                // If tile below is drawing bridge supports, make it stop
                Tile down = game.map.tiles.get(this.tile.position.cpy().add(0, -16));
                if (down != null && down.belowBridge) {
                    String[] tokens = down.name.split("_");
                    String name2 = tokens[1];
//                    for (int i=1; i < tokens.length-1; i++) {
//                        name2 += "_"+tokens[i];
//                    }
                    down.name = name2;
                    down.init(down.name, down.nameUpper, down.position, true, down.routeBelongsTo);
                }
                Tile up = game.map.tiles.get(this.tile.position.cpy().add(0, 16));
                if (up != null && up.name.contains("bridge") && !up.belowBridge) {
                    name = tile.name+"_lower";
                }
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

    int[] currFrames;

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

    int countDownToCacturne = 200;
    
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

        this.rand = new Random();  // TODO: use Game.rand instead
        CycleDayNight.dayTimer = 18000;

        Texture text = TextureCache.get(Gdx.files.internal("text2.png"));
        this.bgSprite = new Sprite(text, 0, 0, 160, 144);
        this.bgSprite.setPosition(0, 24);
        this.text = game.map.timeOfDay + ": ";
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
            if (game.map.timeOfDay.equals("day")) {
                this.fadeToNight = true;
                CycleDayNight.dayTimer = 5000;
            }
            else if (game.map.timeOfDay.equals("night")) {
                this.fadeToDay = true;
                CycleDayNight.dayTimer = 18000;
            }
        }

        if (this.fadeToDay) {
            if (!game.map.currRoute.isDungeon) {
                game.mapBatch.setColor(this.fadeToDayAnim.currentThing());
            }
            animIndex++;

            if (animIndex >= this.fadeToDayAnim.currentFrame()) {
                this.fadeToDayAnim.index++;
                animIndex = 0;
            }

            if (this.fadeToDayAnim.index >= this.fadeToDayAnim.animateThese.size()) {
                fadeToDay = false;
                game.map.timeOfDay = "day";
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

//                if (game.type != Game.Type.SERVER) {
//                    if (game.battle.oppPokemon == null || !SpecialMewtwo1.class.isInstance(game.battle.oppPokemon)) {
//                        String nextMusicName = game.map.currRoute.getNextMusic(true);
//                        BattleFadeOutMusic.playerFainted = true;  // TODO: this is just a hack around issues with FadeMusic
//                        Action nextMusic = new BattleFadeOutMusic(game,
//                                           new WaitFrames(game, 360,
//                                           new FadeMusic(nextMusicName, "in", "", 0.2f, true, 1f, game.musicCompletionListener,
//                                           null)));
//                        game.fadeMusicAction = nextMusic;
//                        game.insertAction(nextMusic);
//                    }
//                    // set brightness according to player location (ie, if in pokemon mansion, set to dim light.)
//                    // TODO: enable once player position doesn't vary like this.
////                    Route currRoute = game.map.tiles.get(game.player.position).routeBelongsTo;
////                    if (currRoute != null) {
////                        if (currRoute.name.contains("pkmnmansion")) {
////                            game.mapBatch.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
////                        }
////                    }
//                }

                day++;
                signCounter = 300;
                this.bgSprite.setPosition(0, 24);

                // Set batch shading if in pokemon mansion
                // TODO: test, remove
//                if (game.map.currRoute.name.contains("pkmnmansion")) {
//                    game.mapBatch.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
//                }
//                else {
//                    game.musicController.startTimeOfDay = "day";
//                    game.mapBatch.setColor(new Color(1f, 1f, 1f, 1f));
//                }
                
                game.musicController.startTimeOfDay = "day";
                if (!game.map.currRoute.isDungeon) {
                    game.mapBatch.setColor(new Color(1f, 1f, 1f, 1f));
                }

                if (game.type != Game.Type.CLIENT) {
                    int numRocks = 0;
                    // TODO maybe remove, not sure
//                    int maxRocks = game.map.edges.size()/10;
//                    System.out.println(maxRocks);
                    // All planted trees become full size trees
                    
                    // Used to respawn fossils
                    // TODO: do smthng similar for respawning rocks
                    ArrayList<Tile> desertTiles = new ArrayList<Tile>();
                    int numFossils = 0;

                    ArrayList<Vector2> desertEdges = new ArrayList<Vector2>();
                    
                    for (Tile tile : game.map.overworldTiles.values()) {
                        if (tile.nameUpper.contains("tree_planted")) {
                            Tile tree;
                            if (tile.name.contains("desert")) {
                                tree = new Tile(tile.name, "tree7", tile.position.cpy(), true, tile.routeBelongsTo);
                            }
                            else {
                                tree = new Tile("bush1", "", tile.position.cpy(), true, tile.routeBelongsTo);
                            }
                            game.map.overworldTiles.put(tile.position, tree);
                            // Just making planted trees yield 2 apricorns by default. 3 if fertilized.
                            // If fertilized, double existing apricorns. If no apricorns, place 1.
                            int amt = 1;
                            if (tile.nameUpper.contains("fertilized")) {
                                amt = 2;
                            }
                            String[] items = {"black apricorn", "blue apricorn", "green apricorn", "pink apricorn",
                                              "red apricorn", "white apricorn", "yellow apricorn"};
                            boolean found = false;
                            for (String item : items) {
                                if (tree.items().containsKey(item)) {
                                    tree.items().put(item, tree.items().get(item)+amt);
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                tree.items().put(items[game.map.rand.nextInt(items.length)], 2);
                            }
                        }
                        else if (tile.name.equals("grass_planted")) {
                            Tile grass = new Tile("grass2", tile.position.cpy(), true, tile.routeBelongsTo);
                            game.map.overworldTiles.put(tile.position, grass);
                        }
                        else if (tile.nameUpper.equals("cactus2_cacturne")) {
                            tile.nameUpper = "";
                            tile.overSprite = null;
                            tile.attrs.put("solid", false);
                        }
                        else if (tile.name.equals("desert2_trapinch_spawn")) {

                            Vector2 startPos = tile.position.cpy().add(-16*4, -16*4);
                            startPos.x = (int)startPos.x - (int)startPos.x % 16;
                            startPos.y = (int)startPos.y - (int)startPos.y % 16;
                            Vector2 endPos = tile.position.cpy().add(16*4, 16*4);
                            endPos.x = (int)endPos.x - (int)endPos.x % 16;
                            endPos.y = (int)endPos.y - (int)endPos.y % 16;
                            int numFound = 0;

                            for (Vector2 currPos = new Vector2(startPos.x, startPos.y); currPos.y < endPos.y;) {
                                Tile nextTile = game.map.overworldTiles.get(currPos);
                                currPos.x += 16;
                                if (currPos.x > endPos.x) {
                                    currPos.x = startPos.x;
                                    currPos.y += 16;
                                }
                                if (nextTile == null) {
                                    continue;
                                }
                                if (!nextTile.name.contains("desert2")) {
                                    continue;
                                }
                                if (nextTile.items().containsKey("trapinch")) {
                                    numFound++;
                                }
                            }
                            // Place the trapinch
                            for (Vector2 currPos = new Vector2(startPos.x, startPos.y); currPos.y < endPos.y && numFound < 3;) {
                                Tile nextTile = game.map.overworldTiles.get(currPos);
                                currPos.x += 16;
                                if (currPos.x > endPos.x) {
                                    currPos.x = startPos.x;
                                    currPos.y += 16;
                                }
                                if (nextTile == null) {
                                    continue;
                                }
                                if (!nextTile.name.contains("desert2")) {
                                    continue;
                                }
                                if (!nextTile.items().containsKey("trapinch") && Game.rand.nextInt(256) < 32) {
                                    numFound++;
                                    Pokemon trapinch = new Pokemon("trapinch", 22, Pokemon.Generation.CRYSTAL);
                                    trapinch.isTrapping = true;
                                    trapinch.position = nextTile.position.cpy();
                                    game.insertAction(trapinch.new Burrowed());
                                    trapinch.mapTiles = game.map.overworldTiles;
                                    nextTile.items().put("trapinch", 1);
                                }
                            }
                        }
                        else if (tile.nameUpper.equals("rock1_color")) {
                            numRocks++;
                        }
                        else if (tile.name.equals("sand3_desertEdge")) {
                            desertEdges.add(tile.position.cpy());
                        }
                        else if (tile.name.contains("desert")) {
                            if (tile.nameUpper.equals("")) {
                                desertTiles.add(tile);
                            }
                            else if (tile.nameUpper.equals("desert4_cracked")) {
                                numFossils++;
                            }
                        }
                        // Respawn rocks randomly
                        else if (numRocks < 7 &&
                                 Game.rand.nextInt(200) >= 197 &&
                                 !game.map.pokemon.containsKey(tile.position) &&
                                 tile.name.contains("mountain") &&
                                 !tile.attrs.get("ledge") &&
                                 !tile.attrs.get("solid") &&
                                 tile.nameUpper.equals("") &&
                                 tile.items().isEmpty()) {
                            int level = 22;
                            if (tile.routeBelongsTo != null) {
                                level = tile.routeBelongsTo.level;
                            }
                            Route tempRoute = new Route("rock_smash1", level);
                            Tile rockTile = new Tile(tile.name, "rock1_color", tile.position.cpy(), true, tempRoute);
                            game.map.overworldTiles.put(tile.position, rockTile);
                            numRocks++;
                        }
                    }
                    // Respawns fossils
                    for (; numFossils < 6 && desertTiles.size() > 0; numFossils++) {
                        Tile fossilTile = desertTiles.remove(Game.rand.nextInt(desertTiles.size()));
                        fossilTile.nameUpper = "desert4_cracked";
                        Texture text = TextureCache.get(Gdx.files.internal("tiles/desert4_cracked.png"));
                        fossilTile.overSprite = new Sprite(text, 0, 0, 16, 16);
                        fossilTile.overSprite.setPosition(fossilTile.position.x, fossilTile.position.y);
                        System.out.println("placed fossil");
                        System.out.println(numFossils);
                    }
                    desertTiles.clear();
                    // Mewtwo respawns
                    for (HashMap<Vector2, Tile> interiorTiles : game.map.interiorTiles) {
                        if (interiorTiles == null) {
                            continue;
                        }
                        for (Tile tile : interiorTiles.values()) {
                            if (tile.nameUpper.equals("mewtwo_overworld_hidden")) {
                                tile.attrs.put("solid", true);
                                tile.nameUpper = "mewtwo_overworld";
                                Texture text = TextureCache.get(Gdx.files.internal("tiles/mewtwo_overworld.png"));
                                tile.overSprite = new Sprite(text, 0, 0, 16, 16);
                            }
                        }
                    }
                    // Respawn dragonites if necessary
                    // TODO: eventually move dragonite to a biome
                    int numDragonites = 0;
                    int numGarchomps = 0;
                    String dragoniteGender = null;
                    String garchompGender = null;
                    for (Vector2 pos : game.map.overworldTiles.keySet()) {
                        if (game.map.pokemon.containsKey(pos)) {
                            Pokemon pokemon = game.map.pokemon.get(pos);
                            if (pokemon.specie.name.equals("dragonite")) {
                                dragoniteGender = pokemon.gender;
                                numDragonites++;
                            }
                            else if (pokemon.specie.name.equals("garchomp")) {
                                garchompGender = pokemon.gender;
                                numGarchomps++;
                            }
                            if (numDragonites >= 2 && numGarchomps >= 2) {
                                break;
                            }
                        }
                    }
                    int index = game.map.rand.nextInt(game.map.edges.size()-1);
                    for (String gender : new String[]{"male", "female"}) {
                        if (numDragonites >= 2) {
                            break;
                        }
                        if (gender.equals(dragoniteGender)) {
                            continue;
                        }
                        System.out.println("numDragonites");
                        System.out.println(numDragonites);
                        Vector2 edge = game.map.edges.get(index);
                        Pokemon pokemon = new Pokemon("dragonite", 55);
                        pokemon.gender = gender;
                        pokemon.position = edge.cpy();
                        pokemon.mapTiles = game.map.overworldTiles;
                        pokemon.standingAction = pokemon.new Standing();
//                            this.pokemonToAdd.put(pokemon.position.cpy(), pokemon);
                        game.insertAction(pokemon.standingAction);
                        game.map.pokemon.put(pokemon.position.cpy(), pokemon);
                        numDragonites++;
                    }
                    // Spawn two opposite-gender garchomp on a random desert edge
                    index = Game.rand.nextInt(desertEdges.size()-1);
                    for (String gender : new String[]{"male", "female"}) {
                        if (desertEdges.size() <= 0) {
                            break;
                        }
                        if (numGarchomps >= 2) {
                            break;
                        }
                        if (gender.equals(garchompGender)) {
                            continue;
                        }
                        System.out.println("numGarchomps");
                        System.out.println(numGarchomps);
                        Vector2 edge = desertEdges.get(index);
                        Pokemon pokemon = new Pokemon("garchomp", 50);
                        pokemon.gender = gender;
                        pokemon.position = edge.cpy();
                        pokemon.mapTiles = game.map.overworldTiles;
                        pokemon.standingAction = pokemon.new Standing();
                        game.insertAction(pokemon.standingAction);
                        game.map.pokemon.put(pokemon.position.cpy(), pokemon);
                    }
                }
            }
        }

        if (this.fadeToNight) {
            if (!game.map.currRoute.isDungeon) {
                game.mapBatch.setColor(this.animContainer.currentThing());
            }
            animIndex++;

            if (animIndex >= this.animContainer.currentFrame()) {
                this.animContainer.index++;
                animIndex = 0;
            }

            if (this.animContainer.index >= this.animContainer.animateThese.size()) {
                fadeToNight = false;
                game.map.timeOfDay = "night";
                this.countDownToGhost = 150; // this.rand.nextInt(5000) + 150;  // debug: 150;
                this.animContainer.index = 0;
                this.countDownToCacturne = 200;
                

                for (Tile tile : game.map.overworldTiles.values()) {
                    // Chance to spawn cacturne
                    if (tile.name.equals("desert4") && tile.nameUpper.equals("") && Game.rand.nextInt(512) < 2) {
                        if (game.cam.frustum.pointInFrustum(tile.position.x, tile.position.y-32, game.cam.position.z) ||
                            game.cam.frustum.pointInFrustum(tile.position.x+tile.sprite.getWidth(), tile.position.y+tile.sprite.getHeight()+64, game.cam.position.z) ||
                            game.cam.frustum.pointInFrustum(tile.position.x+tile.sprite.getWidth(), tile.position.y-32, game.cam.position.z) ||
                            game.cam.frustum.pointInFrustum(tile.position.x, tile.position.y+tile.sprite.getHeight()+64, game.cam.position.z)) {
                            continue;
                        }
                        Vector2 startPos = tile.position.cpy().add(-16*4, -16*4);
                        startPos.x = (int)startPos.x - (int)startPos.x % 16;
                        startPos.y = (int)startPos.y - (int)startPos.y % 16;
                        Vector2 endPos = tile.position.cpy().add(16*4, 16*4);
                        endPos.x = (int)endPos.x - (int)endPos.x % 16;
                        endPos.y = (int)endPos.y - (int)endPos.y % 16;
                        int numPlaced = 0;
                        for (Vector2 currPos = new Vector2(startPos.x, startPos.y); currPos.y < endPos.y;) {
                            Tile nextTile = game.map.overworldTiles.get(currPos);
                            currPos.x += 16;
                            if (currPos.x > endPos.x) {
                                currPos.x = startPos.x;
                                currPos.y += 16;
                            }
                            if (nextTile == null) {
                                continue;
                            }
                            if (!nextTile.name.equals("desert4") || !nextTile.nameUpper.equals("")) {
                                continue;
                            }
                            if (nextTile.attrs.get("solid")) {
                                continue;
                            }
                            if (Game.rand.nextInt(256) < 32) {
                                Tile newTile = new Tile("desert4", "cactus2_cacturne", currPos.cpy(), true, nextTile.routeBelongsTo);
                                game.map.overworldTiles.put(currPos, newTile);
                                numPlaced++;
                                if (numPlaced > 4) {
                                    break;
                                }
                            }
                        }
                    }
                }
                

                // TODO test
//                game.currMusic.pause();
//                if (game.type != Game.Type.SERVER && (game.battle.oppPokemon == null || !SpecialMewtwo1.class.isInstance(game.battle.oppPokemon))) {
//                    game.currMusic.stop();
//                    game.actionStack.remove(game.fadeMusicAction);
//                    // start night music
//                    if (!game.loadedMusic.containsKey("night1")) {
//                        game.loadedMusic.put("night1", Gdx.audio.newMusic(Gdx.files.internal("night1.ogg")));
//                    }
//                    Music music = game.loadedMusic.get("night1");
//                    music.setLooping(true);
//                    music.setVolume(.7f);
//                    game.currMusic = music;
//                    game.map.currRoute.music = music;
//                    game.currMusic.play();
//                }
                if (!game.map.currRoute.name.contains("pkmnmansion")) {
                    game.musicController.startTimeOfDay = "night";
                }
                // State which night it is
                night++;
                signCounter = 150;
                this.bgSprite.setPosition(0,24);
            }
        }

        // Check player can move so don't spawn in middle of battle or when looking at ghost
        // If player is near a campfire, don't deduct from ghost spawn timer
        if (game.map.timeOfDay.equals("night") && game.playerCanMove && !game.player.isNearCampfire) {
            if (game.map.currBiome.equals("deep_forest")) {
                countDownToGhost--;
                // TODO: not working?
                System.out.println(this.countDownToGhost);
                if (game.player.currState != "Running") {
                    countDownToGhost--;
                }

                if (countDownToGhost <= 0) {
                    Vector2 randPos = game.player.position.cpy().add(this.rand.nextInt(5)*16 - 48, this.rand.nextInt(5)*16 - 48);
                    game.insertAction(new SpawnGhost(game, new Vector2(randPos)) );
                    // TODO: mess with this.
//                    this.countDownToGhost = this.rand.nextInt(4000) + 1000; // debug: 1000;
                    this.countDownToGhost = this.rand.nextInt(2000) + 1000;
                }
            }
            if (game.player.nearCacturne) {
                this.countDownToCacturne--;
                

                System.out.println(this.countDownToCacturne);
                if (countDownToCacturne <= 0) {
                    this.countDownToCacturne = this.rand.nextInt(500) + 100;
                    
                    System.out.println("spawn cacturne");
                    Pokemon cacturne = null;
                    Vector2 startPos = game.player.position.cpy().add(-16*5, -16*5);
                    startPos.x = (int)startPos.x - (int)startPos.x % 16;
                    startPos.y = (int)startPos.y - (int)startPos.y % 16;
                    Vector2 endPos = game.player.position.cpy().add(16*5, 16*5);
                    endPos.x = (int)endPos.x - (int)endPos.x % 16;
                    endPos.y = (int)endPos.y - (int)endPos.y % 16;

                    for (Vector2 currPos = new Vector2(startPos.x, startPos.y); currPos.y < endPos.y;) {
                        Tile nextTile = game.map.overworldTiles.get(currPos);
                        currPos.x += 16;
                        if (currPos.x > endPos.x) {
                            currPos.x = startPos.x;
                            currPos.y += 16;
                        }
                        if (nextTile == null) {
                            continue;
                        }
                        if (nextTile.nameUpper.equals("cactus2_cacturne")) {
                            Tile newTile = new Tile("desert4", nextTile.position.cpy(), true, nextTile.routeBelongsTo);
                            game.map.tiles.put(newTile.position, newTile);

                            cacturne = new Pokemon("cacturne", 22, Pokemon.Generation.CRYSTAL);
                            cacturne.position = nextTile.position.cpy();
                            cacturne.mapTiles = game.map.overworldTiles;
                            cacturne.aggroPlayer = true;
                            game.playerCanMove = false;
                            game.insertAction(cacturne.new Cacturnt(null));
                            game.insertAction(game.player.new Emote("!", null));
                            game.insertAction(cacturne.new CactusSpawn(nextTile,
                                              cacturne.new Emote("skull",
                                              new WaitFrames(game, 60,
                                              new SetField(game, "playerCanMove", true, 
                                              new SetField(game.musicController, "startNightAlert", "night1_chase1", null))))));
                        }
                    }

                    if (cacturne != null) {
                        game.insertAction(new WaitFrames(game, 60,
                                          new PlaySound(cacturne, null)));
                        game.musicController.startNightAlert = "night1_alert1";
                    }
                }
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
    HashMap<String, String> namesMap = new HashMap<String, String>();

    public DrawBuildRequirements() {
        // text box background
        Texture text = TextureCache.get(Gdx.files.internal("pokemon_menu/selected_menu_top.png"));
        this.textBoxTop = new Sprite(text, 0,0, 71, 19);
        text = TextureCache.get(Gdx.files.internal("pokemon_menu/selected_menu_middle.png"));
        this.textBoxMiddle = new Sprite(text, 0,0, 71, 16);
        text = TextureCache.get(Gdx.files.internal("pokemon_menu/selected_menu_bottom.png"));
        this.textBoxBottom = new Sprite(text, 0,0, 71, 19);
        namesMap.put("house5_roof_middle1", "Roof");
        namesMap.put("campfire1", "Fire");
        namesMap.put("fence1", "Fence");
        namesMap.put("house_bed1", "Bed");
        namesMap.put("house5_door1", "Door");
        namesMap.put("house5_middle1", "Wall");
        namesMap.put("bridge1", "Bridge");
        // Desert structures
        namesMap.put("house6_NEW", "Wall");
        namesMap.put("house6_roof_middle1", "Roof");
        namesMap.put("house6_door1", "Door");
        namesMap.put("fence2", "Fence");
        namesMap.put("potted1", "Flower");
        namesMap.put("potted2", "Flower");
        //
        namesMap.put("torch1", "Torch");
        //
        namesMap.put("house_plant1", "Plant");
        namesMap.put("house_plant2", "Plant");
        namesMap.put("house_gym1", "Statue");
        namesMap.put("house_shelf1", "Shelf");
        namesMap.put("house_wardrobe1", "Dresser");
        namesMap.put("house_stool1", "Stool");
        namesMap.put("house_table1_default", "Table");
        namesMap.put("house_carpet1", "Carpet");
        namesMap.put("house_carpet2", "Carpet");
        namesMap.put("house_carpet3", "Carpet");
        namesMap.put("house_carpet4", "Carpet");
        namesMap.put("house_carpet5", "Carpet");
        namesMap.put("house_carpet6", "Carpet");
        namesMap.put("house_carpet7", "Carpet");
        namesMap.put("house_carpet8", "Carpet");
        namesMap.put("house_clock1", "Clock");
        namesMap.put("house_picture1", "Picture");
        namesMap.put("house_picture2", "Picture");
        namesMap.put("house_picture3", "Picture");
        namesMap.put("house_picture4", "Picture");
        namesMap.put("house_picture5", "Picture");
        namesMap.put("house_picture6", "Picture");
        namesMap.put("house_picture7", "Picture");
        namesMap.put("house_picture8", "Picture");
        namesMap.put("house_picture9", "Picture");
        namesMap.put("house_picture10", "Picture");
        namesMap.put("house_picture11", "Picture");
        namesMap.put("house_picture12", "Picture");
        namesMap.put("house_picture13", "Picture");
        namesMap.put("house_picture14", "Picture");
        namesMap.put("house_picture15", "Picture");
        namesMap.put("house_picture16", "Picture");
        namesMap.put("house_picture17", "Picture");

        // Terrain
        namesMap.put("sand1", "Sand");
        namesMap.put("desert4", "Desert");
        namesMap.put("snow1", "Snow");
        namesMap.put("mountain1", "Clay");
        namesMap.put("green1", "Grass");
        namesMap.put("flower4", "Flowers");
        
    }

    public String getCamera() {return "gui";}

    @Override
    public void firstStep(Game game) {}

    @Override
    public void step(Game game) {
        // If player isn't building, don't draw anything
        if (!game.player.currFieldMove.equals("BUILD") && !game.player.currFieldMove.equals("DIG")) {
            return;
        }
        if (!game.playerCanMove) {
            return;
        }
        // Don't display build requirements if player is just digging a hole
        if (game.player.currFieldMove.equals("DIG") && game.player.currBuildTile.name.contains("hole")) {
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
//                String text = req.toUpperCase();
                int numNeeded = game.player.buildTileRequirements.get(curr).get(req);
                String newReq = req.toUpperCase();
                if (newReq.equals("NEVERMELTICE")) {
                    newReq = "NVRMLT ICE";
                }
                String[] texts = newReq.split(" ");
                for (int j=0; j < texts.length; j++) {
                    String text = texts[j];
                    if (text.equals("BEDDING")) {
                        text = "BED";
                    }
                    if (j >= texts.length-1) {
                        int length = text.length();
                        for (int i=0; i < 5-length; i++) {
                            text += " ";
                        }
                        text += "x";
                        text += String.valueOf(numNeeded);
                    }
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
        // Draw requirements of current item being built
        Sprite letterSprite;
        Color prevColor;
        for (int i=0; i < this.words.size(); i++) {

            // Draw appropriate part of textBox
            if (i == 0) {
                game.uiBatch.draw(this.textBoxTop, this.topLeft.x, this.topLeft.y-19);
            }
            else if (i == this.words.size()-1) {
                game.uiBatch.draw(this.textBoxBottom, this.topLeft.x, this.topLeft.y-19 -16*(i));
            }
            else {
                game.uiBatch.draw(this.textBoxMiddle, this.topLeft.x, this.topLeft.y-19 -16*(i));
            }
            Color color = game.uiBatch.getColor();
            if (i < this.words.size()) {
                String word = this.words.get(i);
                for (int j=0; j < word.length(); j++) {
                    char letter = word.charAt(j);
                    letterSprite = new Sprite(game.textDict.get(letter));
                    letterSprite.setPosition(this.topLeft.x +8 +8*j, this.topLeft.y -14 -16*(i));
                    game.uiBatch.setColor(color.r, color.g, color.b, this.wordColors.get(i).a);
                    game.uiBatch.draw(letterSprite, letterSprite.getX(), letterSprite.getY());
                }
            }
            game.uiBatch.setColor(color);

            /// TODO: remove if unused
//            // Draw appropriate part of textBox
//            if (i == 0) {
//                game.uiBatch.draw(this.textBoxTop, this.topLeft.x, this.topLeft.y-19);
//            }
//            else if (i == this.words.size()-1) {
//                game.uiBatch.draw(this.textBoxBottom, this.topLeft.x, this.topLeft.y-38 -16*(this.words.size()-2));
//            }
//            else {
//                game.uiBatch.draw(this.textBoxMiddle, this.topLeft.x, this.topLeft.y-19 -16*(this.words.size()-3+i));
//            }
//            String word = this.words.get(i);
//            for (int j=0; j < word.length(); j++) {
//                int offsetY = 0;
//                // TODO: not sure what is the issue here
//                if (this.words.size() <= 2) {
//                    offsetY = 1;
//                }
//                char letter = word.charAt(j);
//                // convert string to text
//                letterSprite = new Sprite(game.textDict.get(letter));
////                game.uiBatch.draw(letterSprite, this.topLeft.x +8 +8*j, this.topLeft.y -16*(this.words.size()-2+i));
//                letterSprite.setPosition(this.topLeft.x +8 +8*j, this.topLeft.y -16*(this.words.size()-2+i+offsetY));
////                letterSprite.setColor(this.wordColors.get(i));
//
//                prevColor = game.uiBatch.getColor();
//                game.uiBatch.setColor(prevColor.r, prevColor.g, prevColor.b, this.wordColors.get(i).a);
//                game.uiBatch.draw(letterSprite, letterSprite.getX(), letterSprite.getY());
//                game.uiBatch.setColor(prevColor);
////                game.uiBatch.draw(letterSprite, letterSprite.getX(), letterSprite.getY());  // TODO: remove, not sure why this was here.
//            }
        }
        // Draw the name of what is being built top-left
        game.uiBatch.draw(this.textBoxTop, 0, 144-19);
        game.uiBatch.draw(this.textBoxBottom, 0, 144-19-6);
        // Turns weird internal names of objects to 'nice' names
        String name = this.namesMap.get(this.currBuilding);
        if (name == null) {
            name = "null";
        }
        for (int j=0; j < name.length(); j++) {
            char letter = name.charAt(j);
            // convert string to text
            letterSprite = new Sprite(game.textDict.get(letter));
            letterSprite.setPosition(0 +8 +8*j, 144-16);
//            letterSprite.draw(game.uiBatch);
            game.uiBatch.draw(letterSprite, letterSprite.getX(), letterSprite.getY());
        }
    }
}


/**
 * Draws the requirements for building an object if game.player.isBuilding.
 */
class DrawItemPickup extends Action {
    Sprite textBoxTop;
    Sprite textBoxMiddle;
    Sprite textBoxBottom;
    ArrayList<String> words = new ArrayList<String>(); // menu items
    ArrayList<Color> wordColors = new ArrayList<Color>(); // menu items
    Vector2 topLeft = new Vector2(89, 144);
    int timer = 0;
    HashMap<String, Integer> items;

    public DrawItemPickup(HashMap<String, Integer> items, Action nextAction) {
        this.items = items;
        this.nextAction = nextAction;
        // Text box background
        Texture texture = new Texture(Gdx.files.internal("pokemon_menu/selected_menu_top.png"));
        this.textBoxTop = new Sprite(texture, 0,0, 71, 19);
        texture = new Texture(Gdx.files.internal("pokemon_menu/selected_menu_middle.png"));
        this.textBoxMiddle = new Sprite(texture, 0,0, 71, 16);
        texture = new Texture(Gdx.files.internal("pokemon_menu/selected_menu_bottom.png"));
        this.textBoxBottom = new Sprite(texture, 0,0, 71, 19);

        this.words.clear();
        this.wordColors.clear();
        this.words.add("GOT");
        this.wordColors.add(new Color(1,1,1,1));
        for (String item : this.items.keySet()) {
//            String text = item.toUpperCase();
            String[] texts = item.toUpperCase().split(" ");
            for (int i=0; i < texts.length; i++) {
                String text = texts[i];
                if (text.contains("APRICORN")) {
                    text = "APRCN";
                }
                else if (text.equals("BEDDING")) {
                    text = "BED";
                }
                // This will split long words into two lines
                if (i >= texts.length-1) {
                    int numGot = this.items.get(item);
                    int length = text.length();
                    for (int j=0; j < 5-length; j++) {
                        text += " ";
                    }
                    text += "x";
                    text += String.valueOf(numGot);
                }
                this.wordColors.add(new Color(1,1,1,1));
                this.words.add(text);
            }
        }
    }

    public String getCamera() {return "gui";}

    @Override
    public void firstStep(Game game) {}

    @Override
    public void step(Game game) {
        if (this.timer > 90) {
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
        }
        this.timer++;

        // Draw requirements of current item being built
        Sprite letterSprite;
        int height = this.words.size();
        if (height < 2) {
            height = 2;
        }
        for (int i=0; i < height; i++) {
            // Draw appropriate part of textBox
            if (i == 0) {
                game.uiBatch.draw(this.textBoxTop, this.topLeft.x, this.topLeft.y-19);
            }
            else if (i == height-1) {
                game.uiBatch.draw(this.textBoxBottom, this.topLeft.x, this.topLeft.y-19 -16*(i));
            }
            else {
                game.uiBatch.draw(this.textBoxMiddle, this.topLeft.x, this.topLeft.y-19 -16*(i));
            }
            Color color = game.uiBatch.getColor();
            if (i < this.words.size()) {
                String word = this.words.get(i);
                for (int j=0; j < word.length(); j++) {
                    char letter = word.charAt(j);
                    letterSprite = new Sprite(game.textDict.get(letter));
                    letterSprite.setPosition(this.topLeft.x +8 +8*j, this.topLeft.y -14 -16*(i));
//                    letterSprite.setColor(this.wordColors.get(i));
//                    letterSprite.draw(game.uiBatch);
                    game.uiBatch.setColor(color.r, color.g, color.b, this.wordColors.get(i).a);
                    game.uiBatch.draw(letterSprite, letterSprite.getX(), letterSprite.getY());
                }
            }
            game.uiBatch.setColor(color);
        }
    }
}

/**
 * Draw crafts menu, used in overworld (currently when player interacts with a campfire).
 */
class DrawCraftsMenu extends MenuAction {
    public static int lastCurrIndex = 0;  // Which item the player was viewing last
    public static int lastCursorPos = 0;
    Sprite arrow;
    Sprite arrowWhite;
    Sprite textBox;
    public int layer = 107;
    Map<Integer, Vector2> arrowCoords;
    Vector2 newPos;
    Sprite helperSprite;
    ArrayList<ArrayList<Sprite>> spritesToDraw;
    int cursorDelay;  // This is just extra detail. cursor has 2 frame delay before showing in R/B
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
    boolean refresh = false;
    ArrayList<Player.Craft> crafts;

    public DrawCraftsMenu(Game game, Action nextAction) {
        this(game, Player.crafts, null);
        this.nextAction = nextAction;
    }

    public DrawCraftsMenu(Game game, ArrayList<Player.Craft> crafts, Action nextAction) {
        this(game, crafts, null);
        this.nextAction = nextAction;
    }

    public DrawCraftsMenu(Game game, MenuAction prevMenu) {
        this(game, Player.crafts, prevMenu);
    }

    public DrawCraftsMenu(Game game, ArrayList<Player.Craft> crafts, MenuAction prevMenu) {
        this.crafts = crafts;
        this.prevMenu = prevMenu;
        this.disabled = false;
        this.cursorDelay = 0;
        this.arrowCoords = new HashMap<Integer, Vector2>();
        this.spritesToDraw = new ArrayList<ArrayList<Sprite>>();

        Texture text = new Texture(Gdx.files.internal("battle/arrow_right1.png"));
        this.arrow = new Sprite(text, 0, 0, 5, 7);
        text = new Texture(Gdx.files.internal("battle/arrow_right_white.png"));
        this.arrowWhite = new Sprite(text, 0, 0, 5, 7);

        // Text box bg
        text = new Texture(Gdx.files.internal("attack_menu/item_menu1.png"));
        this.textBox = new Sprite(text, 0,0, 16*10, 16*9);

        // Craft requirements text box bg
        text = new Texture(Gdx.files.internal("textbox_bg1.png"));
        this.craftReqsTextbox = new Sprite(text, 0,0, 16*10, 16*9);
        text = new Texture(Gdx.files.internal("title_bg1.png"));
        this.titleTextbox = new Sprite(text, 0,0, 16*10, 16*9);

        // Down arrow for items menu
        text = new Texture(Gdx.files.internal("arrow_down.png"));
        this.downArrow = new Sprite(text, 0, 0, 7, 5);
        this.downArrow.setPosition(144, 50);
        this.downArrowTimer = 0;

        this.currIndex = DrawCraftsMenu.lastCurrIndex;
        this.cursorPos = DrawCraftsMenu.lastCursorPos;

        // Finite amount of cursor coordinates (4)
        // this.arrowCoords.put(1, new Vector2(89, 72+16+16)); // example
        this.arrowCoords.put(0, new Vector2(41, 104 - 16*0));
        this.arrowCoords.put(1, new Vector2(41, 104 - 16*1));
        this.arrowCoords.put(2, new Vector2(41, 104 - 16*2));
        this.arrowCoords.put(3, new Vector2(41, 104 - 16*3));

        newPos = arrowCoords.get(cursorPos);
        this.arrow.setPosition(newPos.x, newPos.y);

        this.craftsList = new ArrayList<String>();
        for (Player.Craft craft : this.crafts) {
            this.craftsList.add(craft.name);
        }
        this.craftsList.add("Cancel");
        // Convert player item list to sprites
        for (String entry : this.craftsList) {
            char[] textArray = entry.toUpperCase().toCharArray();
            Sprite currSprite;
            int i = 0;
            ArrayList<Sprite> word = new ArrayList<Sprite>();
            for (char letter : textArray) {
                Sprite letterSprite = game.textDict.get((char)letter);
                if (letterSprite == null) {
                    letterSprite = game.textDict.get(null);
                }
                if (this.crafts == game.player.regiCrafts) {
                    letterSprite = game.brailleDict.get((char)letter);
                    if (letterSprite == null) {
                        letterSprite = game.textDict.get(' ');
                    }
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
                    currSprite.setPosition(48 +8*i, 104);
                    word.add(currSprite);
                }
            }
            spritesToDraw.add(word);

            if (this.crafts == game.player.regiCrafts) {
                this.textBox.setPosition(0, 16);
                text = new Texture(Gdx.files.internal("textbox_bg2.png"));
                this.craftReqsTextbox = new Sprite(text, 0,0, 16*10, 16*9);
                this.downArrow.setPosition(144, 50 +16);
//                for (Vector2 coord : this.arrowCoords.values()) {
//                    coord.add(0, 16);
//                }
//                for (ArrayList<Sprite> sprites : this.spritesToDraw) {
//                    for (Sprite sprite : sprites) {
//                        sprite.setPosition(sprite.getX(), sprite.getY()+16);
//                    }
//                }
            }
            
        }
    }

    public String getCamera() {return "gui";}

    public int getLayer(){return this.layer;}

    public void refresh(Game game, int index) {
        this.craftReqs.clear();
        this.craftReqColors.clear();
//        this.words.add("Need");
//        this.wordColors.add(new Color(1,1,1,1));
        for (Player.Craft craft : this.crafts.get(index).requirements) {
            String text = craft.name.toUpperCase();
            int numNeeded = craft.amount;
            int offset = 0;
            if (numNeeded >= 100) {
                offset = 2;
            }
            else if (numNeeded >= 10) {
                offset = 1;
            }
            for (int i=0; i < (16-craft.name.length()) -offset; i++) {
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

    @Override
    public void firstStep(Game game) {
        // Fixes issue if exited one craft menu and opened
        // another with less items in it (ex: regi craft menu)
        while (this.currIndex + this.cursorPos > this.crafts.size()-2) {
            if (this.currIndex > 0) {
                this.currIndex--;
            }
            else if (this.cursorPos > 0) {
                this.cursorPos--;
            }
            else {
                break;
            }
        }
        DrawCraftsMenu.lastCurrIndex = this.currIndex;
        DrawCraftsMenu.lastCursorPos = this.cursorPos;
        newPos = arrowCoords.get(cursorPos);
        this.arrow.setPosition(newPos.x, newPos.y);
    }

    @Override
    public void step(Game game) {
        if (this.prevMenu != null) {
            this.prevMenu.step(game);
        }

        // Draw text box
        if (this.crafts != game.player.regiCrafts) {
//            this.titleTextbox.draw(game.uiBatch);
            game.uiBatch.draw(this.titleTextbox, this.titleTextbox.getX(), this.titleTextbox.getY());
        }

//        this.craftReqsTextbox.draw(game.uiBatch);
//      this.textBox.draw(game.uiBatch);
        game.uiBatch.draw(this.craftReqsTextbox, this.craftReqsTextbox.getX(), this.craftReqsTextbox.getY());
        game.uiBatch.draw(this.textBox, this.textBox.getX(), this.textBox.getY());

        // Draw the menu items
        int j = 0;
        for (int i = 0; i < this.spritesToDraw.size(); i++) {
            if (i >= this.currIndex && i < this.currIndex +4) { // only draw range of 4 starting at currIndex
                ArrayList<Sprite> word = this.spritesToDraw.get(i);
                for (Sprite sprite : word) {
                    // Draw this string as text on the screen
                    int extra = 0;
                    if (this.crafts == game.player.regiCrafts) {
                        extra = 16;
                    }
                    game.uiBatch.draw(sprite, sprite.getX(), sprite.getY() - j*16 +extra);
                }
                j++;
            }
        }
        int index = DrawCraftsMenu.lastCurrIndex+DrawCraftsMenu.lastCursorPos;
        if (index >= this.crafts.size()) {  // Happens when 'cancel' is selected
            this.craftReqs.clear();
            this.craftReqColors.clear();
            this.currCraft = "";
        }
        else {
            String curr = this.crafts.get(index).name;
            if (curr != null && !this.currCraft.equals(curr)) {
                this.currCraft = curr;
                this.refresh(game, index);
            }
        }
        // Draw requirements of current item being crafted
        char[] textArray = "CRAFTING MENU".toCharArray();
        if (this.crafts == game.player.fossilCrafts) {
            textArray = "FOSSIL MENU".toCharArray();
        }
        Sprite letterSprite;
        j=0;
        for (char letter : textArray) {
            letterSprite = game.textDict.get((char)letter);
            if (letterSprite == null) {
                letterSprite = game.textDict.get(null);
            }
            if (this.crafts == game.player.regiCrafts) {
                break;
                // TODO: just not drawing for now
//                letterSprite = game.brailleDict.get((char)letter);
//                if (letterSprite == null) {
//                    letterSprite = game.textDict.get(' ');
//                }
            }
            game.uiBatch.draw(letterSprite, 8 +8*j, 144-16);
            j++;
        }
        Color prevColor;
        for (int i=0; i < this.craftReqs.size(); i++) {
            String word = this.craftReqs.get(i);
            for (int k=0; k < word.length(); k++) {
                char letter = word.charAt(k);
                int extra = 0;
                if (this.crafts == game.player.regiCrafts) {
                    extra = 16;
                }
                // Convert string to text
                letterSprite = new Sprite(game.textDict.get(letter));
//                game.uiBatch.draw(letterSprite, this.topLeft.x +8 +8*j, this.topLeft.y -16*(this.words.size()-2+i));
                letterSprite.setPosition(this.topLeft.x +8 +8*k, this.topLeft.y -16*i +extra);
//                letterSprite.setColor(this.craftReqColors.get(i));
//                letterSprite.draw(game.uiBatch);
                prevColor = game.uiBatch.getColor();
                game.uiBatch.setColor(prevColor.r, prevColor.g, prevColor.b, this.craftReqColors.get(i).a);
                game.uiBatch.draw(letterSprite, letterSprite.getX(), letterSprite.getY());
                game.uiBatch.setColor(prevColor);
            }
        }
        // Return at this point if this menu is disabled
        if (this.disabled) {
            int extra = 0;
            if (this.crafts == game.player.regiCrafts) {
                extra = 16;
            }
            this.arrowWhite.setPosition(newPos.x, newPos.y +extra);
//            this.arrowWhite.draw(game.uiBatch);
            game.uiBatch.draw(this.arrowWhite, this.arrowWhite.getX(), this.arrowWhite.getY());
            return;
        }
        if (this.refresh) {
            this.refresh(game, index);
            this.refresh = false;
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

        // Draw arrow
        if (this.cursorDelay >= 5) {
            int extra = 0;
            if (this.crafts == game.player.regiCrafts) {
                extra = 16;
            }
            this.arrow.setPosition(newPos.x, newPos.y +extra);
//            this.arrow.draw(game.uiBatch);
            game.uiBatch.draw(this.arrow, this.arrow.getX(), this.arrow.getY());
        }
        else {
            this.cursorDelay+=1;
        }

        // Draw down arrow if applicable
        if ((this.craftsList.size() - this.currIndex) > 4) {
            if (this.downArrowTimer < 22) {
//                this.downArrow.draw(game.uiBatch);
                game.uiBatch.draw(this.downArrow, this.downArrow.getX(), this.downArrow.getY());
            }
            this.downArrowTimer++;
        }
        else {
            this.downArrowTimer = 0;  // Force arrow to start over when scroll up
        }

        if (this.downArrowTimer > 41) {
            this.downArrowTimer = 0;
        }

        // If press A, draw craft/cancel for item
        if (InputProcessor.aJustPressed) {
            game.actionStack.remove(this);
            game.insertAction(new PlaySound("click1", null));
            String name = this.craftsList.get(currIndex + cursorPos);
            if ("Cancel".equals(name)) {
                DrawCraftsMenu.lastCurrIndex = this.currIndex;
                DrawCraftsMenu.lastCursorPos = this.cursorPos;
                if (this.prevMenu != null) {
                    this.prevMenu.disabled = false;
                }
                game.insertAction(new WaitFrames(game, 3, this.prevMenu));
                game.insertAction(new WaitFrames(game, 3, this.nextAction));
            }
            else {
                this.refresh = true;
                this.disabled = true;
                // Draw 'select amount' box
                if (this.crafts == game.player.regiCrafts || this.crafts == game.player.fossilCrafts) {
                    SelectAmount.amount = 1;  // Can only craft one regi
                    game.insertAction(new DrawCraftsMenu.Selected(this.crafts, this));
                }
                else {
                    game.insertAction(new DrawCraftsMenu.SelectAmount(this.crafts, this));
                }
            }
            return;
        }
        // Player presses b, ie wants to go back
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
        ArrayList<Player.Craft> crafts;

        public SelectAmount(ArrayList<Player.Craft> crafts, MenuAction prevMenu) {
//            this.maxAmount = maxAmount;
            this.crafts = crafts;
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
                if (game.player.hasCraftRequirements(this.crafts, index, SelectAmount.amount+1)) {
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
                    if (game.player.hasCraftRequirements(this.crafts, index, SelectAmount.amount+1)) {
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
                game.insertAction(new DrawCraftsMenu.Selected(this.crafts, this));
            }
            // player presses B, ie wants to go back
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
        Sprite arrowWhite;
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
        ArrayList<Player.Craft> crafts;

        public Selected(ArrayList<Player.Craft> crafts, MenuAction prevMenu) {
            this.crafts = crafts;
            this.prevMenu = prevMenu;

            this.getCoords = new HashMap<Integer, Vector2>();
            this.words = new ArrayList<String>();
            if (crafts == Game.staticGame.player.fossilCrafts) {
                this.words.add("CREATE");
            }
            else {
                this.words.add("CRAFT");
            }
            this.words.add("CANCEL");

            this.getCoords.put(0, new Vector2(97, 40 +16));
            this.getCoords.put(1, new Vector2(97, 40-16 +16));

            Texture text = new Texture(Gdx.files.internal("battle/arrow_right1.png"));
            this.arrow = new Sprite(text, 0, 0, 5, 7);
            text = new Texture(Gdx.files.internal("battle/arrow_right_white.png"));
            this.arrowWhite = new Sprite(text, 0, 0, 5, 7);

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
            // If there is a previous menu, step through it to display text
            if (prevMenu != null) {
                prevMenu.step(game);
            }
            // White arrow only for one frame, then box appears
            if (this.textboxDelay < 1) {
                this.textboxDelay+=1;
                return;
            }
            // Draw the menu items (CRAFT, CANCEL)
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
            // Draw arrow sprite
            this.arrowWhite.setPosition(newPos.x, newPos.y);
            this.arrowWhite.draw(game.uiBatch);
            if (this.disabled) {
                return;
            }
            // Draw arrow sprite
            this.arrow.setPosition(newPos.x, newPos.y);
//            this.arrow.draw(game.uiBatch);
            game.uiBatch.draw(this.arrow, this.arrow.getX(), this.arrow.getY());
            // Check user input
            // 'tl' = top left, etc.
            // Modify position by modifying curr to tl, tr, bl or br
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

            if (InputProcessor.aJustPressed) {
                String word = this.words.get(this.curr);
                if ("CANCEL".equals(word)) {
                    game.insertAction(new PlaySound("click1", null));
                    game.actionStack.remove(this);
                    this.prevMenu.disabled = false;
                    game.insertAction(this.prevMenu);
                    return;
                }
                else if ("CRAFT".equals(word) || "CREATE".equals(word)) {
                    int index = DrawCraftsMenu.lastCurrIndex+DrawCraftsMenu.lastCursorPos;
                    if (game.type == Game.Type.CLIENT) {
                        game.client.sendTCP(new com.pkmngen.game.Network.Craft(game.player.network.id,
                                                                               index,
                                                                               SelectAmount.amount));
                    }
                    // Check craft requirements
                    if (game.player.hasCraftRequirements(this.crafts, index, SelectAmount.amount)) {

                        if (this.crafts == game.player.regiCrafts) {
                            // Remove required items from player inventory
                            // TODO: test
                            Player.Craft craft = this.crafts.get(index);
                            for (Player.Craft req : craft.requirements) {
                                int newAmt = game.player.itemsDict.get(req.name)-(req.amount*SelectAmount.amount);
                                game.player.itemsDict.put(req.name, newAmt);
                                if (newAmt <= 0) {
                                    game.player.itemsDict.remove(req.name);
                                }
                            }
                            
                            // Remove that regicraft from the player (can only ever craft once)
                            Player.Craft regiCraft = this.crafts.remove(index);  // TODO: this doesn't do anything anymore, remove (I think)
                            game.actionStack.remove(this);
                            game.actionStack.remove(this.prevMenu);
                            String dirFacing = game.player.dirFacing;
                            game.player.dirFacing = "up";
                            // Display player emote
                            // Probably one action that does eq anim and regi sprite fade in
                            game.insertAction(new WaitFrames(game, 20,
                                              game.player.new Emote("!",
                                              new WaitFrames(game, 30,
                                              new SetField(game.player, "currSprite", game.player.standingSprites.get(game.player.dirFacing),
                                              new RegigigasIntroAnim(regiCraft.name, dirFacing, null))))));
                            return;
                        }

                        // Special animation for reviving a fossil
                        if (this.crafts == game.player.fossilCrafts) {
                            Player.Craft craft = this.crafts.get(index);
                            for (Player.Craft req : craft.requirements) {
                                int newAmt = game.player.itemsDict.get(req.name)-(req.amount*SelectAmount.amount);
                                game.player.itemsDict.put(req.name, newAmt);
                                if (newAmt <= 0) {
                                    game.player.itemsDict.remove(req.name);
                                }
                            }
                            Player.Craft fossilCraft = this.crafts.get(index);
                            game.actionStack.remove(this);
                            game.actionStack.remove(this.prevMenu);
                            game.player.dirFacing = "right";
                            Action newAction = new WaitFrames(game, 20,
                                               game.player.new Emote("!",
                                               new WaitFrames(game, 30,
                                               new SetField(game.player, "currSprite", game.player.standingSprites.get(game.player.dirFacing),
                                                            null))));
                            if (game.player.hmPokemon != null) {
                                newAction.append(new SetField(game.player.hmPokemon, "dirFacing", "right", null));
                            }
                            game.insertAction(new WaitFrames(game, 20,
                                              new PlaySound("sounds/computer1", .5f, false,
                                              null)));
                            game.insertAction(new FossilMachinePowerUp.DoRevive(fossilCraft.name, null));
                            game.insertAction(newAction);
                            return;
                        }
                        
                        // Craft the item if the item requirements are met
                        game.player.craftItem(this.crafts, index, SelectAmount.amount);
                        // Display 'you crafted' text
                        game.actionStack.remove(this);
                        game.insertAction(this.prevMenu.prevMenu);
                        String plural = "";
                        if (SelectAmount.amount > 1 && !this.crafts.get(index).name.toLowerCase().endsWith("s")) {
                            plural += "S";
                        }
                        game.insertAction(new DisplayText(game, "Crafted "+String.valueOf(SelectAmount.amount)+" "+this.crafts.get(index).name.toUpperCase()+plural+"!",
                                                          "fanfare1.ogg", true, true,
                                          new DisplayText.Clear(game,
                                          new SetField(this.prevMenu.prevMenu, "disabled", false,
                                          null))));
                    }
                    else {
                        this.disabled = true;
                        game.insertAction(new PlaySound("error1",
                                          new SetField(this, "disabled", false,
                                          null)));         
                    }
                }
            }
            // Player presses b, ie wants to go back
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

        String[] pokemon = new String[]{"litwick", "lampent", "chandelure",
                "mimikyu", "misdreavus", "sableye",
                "gastly", "haunter", "gengar"};
        
        this.pokemon = new Pokemon(pokemon[Game.rand.nextInt(pokemon.length)], 21);
        this.pokemon.spookify();

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
        if (!game.map.timeOfDay.equals("night")) {
            this.currSprite.draw(game.mapBatch);
            game.actionStack.remove(this);
            game.insertAction(new DespawnGhost(this.basePos.cpy()));
            return;
        }
        // Check if player's party has fainted. if yes, remove this from AS
        boolean hasAlivePokemon = false;
        for (Pokemon pokemon : game.player.pokemon) {
            if (pokemon.currentStats.get("hp") > 0 && !pokemon.isEgg) {
                hasAlivePokemon = true;
                break;
            }
        }
        // Check if ghost pokemon is dead. if yes, remove this from AS
        if (!hasAlivePokemon ||
            (!this.inBattle && this.pokemon.currentStats.get("hp") <= 0) ||
            this.pokemon.previousOwner != null) {
            this.currSprite.draw(game.mapBatch);
            game.actionStack.remove(this);
            game.insertAction(new DespawnGhost(this.basePos.cpy()));
            boolean foundGhost = false;
            for (Action action : game.actionStack) {
                if (DrawGhost.class.isInstance(action)) {
                    foundGhost = true;
                    break;
                }
            }
            if (!foundGhost && hasAlivePokemon) {
                // TODO: remove
//                game.currMusic.stop();
//                game.currMusic.dispose();
//                game.currMusic = game.loadedMusic.get(game.musicController.currOverworldMusic);//game.map.currRoute.music;
//                game.currMusic.play();

                game.musicController.nightAlert = false;
                game.musicController.resumeOverworldMusic = true;
            }
            return;
        }
        // TODO: remove
        // Check whether player is in battle or not
        // if not, don't move the ghost at all (subject to change)
//        if (game.battle.drawAction != null) {
//            this.currSprite.draw(game.mapBatch);
//            this.inBattle = true;
//            this.noEncounterTimer = 0;
//            return;
//        }

        // Pause if player can't move
        if (!game.playerCanMove) {
            this.currSprite.draw(game.mapBatch);
            return;
        }

        // Wait for a while if you just exited battle
        if (this.inBattle) {
            // Do this b/c player may have used silph scope
            this.pokemon = game.battle.oppPokemon;
            if (this.noEncounterTimer % 4 >= 2) {
                this.currSprite.draw(game.mapBatch);
            }
            if (this.noEncounterTimer < 128) {
                this.noEncounterTimer++;
                return;
            }
            this.inBattle = false;
            return;
        }

        // If too near to a fire, despawn
        boolean foundCampfire = false;
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
                foundCampfire = true;
                break;
            }
            else if (tile.items().containsKey("torch") &&
                     this.basePos.dst2(tile.position) < 1024) {
                foundCampfire = true;
                break;
            }
            Pokemon pokemon = game.map.pokemon.get(currPos);
            if (pokemon != null && pokemon.hms.contains("FLASH")) {
                foundCampfire = true;
                break;
            }
        }
        // Check if player is using an hm pokemon with flash
        if (game.player.hmPokemon != null && 
            this.basePos.dst2(game.player.position) < 4096 &&
            game.player.hmPokemon.hms.contains("FLASH")) {
            foundCampfire = true;
        }
        if (foundCampfire) {
            this.currSprite.draw(game.mapBatch);
            game.actionStack.remove(this);
            game.insertAction(new DespawnGhost(this.basePos.cpy()));
            // TODO: test
//            game.currMusic.pause();
//            game.currMusic.stop();
//            game.currMusic.dispose();
//            game.currMusic = game.loadedMusic.get(game.musicController.currOverworldMusic);
//            game.currMusic.play();
            game.musicController.nightAlert = false;
            game.musicController.resumeOverworldMusic = true;
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
            this.inBattle = true;
            this.noEncounterTimer = 0;
            game.battle.oppPokemon = this.pokemon;
            game.playerCanMove = false;
            game.musicController.inBattle = true;
            game.player.setCurrPokemon();
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
        if (game.player.isSleeping || game.player.currFieldMove.equals("FLY")) {
            return;
        }

        this.spritePart = new Sprite(game.player.currSprite);

        // this.spritePart.setRegion(0,8,16,8);
        this.spritePart.setRegionY(game.player.spriteOffsetY+8);
        this.spritePart.setRegionHeight(8);
        // this.spritePart.setSize(this.spritePart.getWidth(), 8);

        game.mapBatch.draw(this.spritePart, game.player.position.x, game.player.position.y+4+DrawPlayerUpper.pokemonOffsetY);
    }
}

class DrawPlayerUpper extends Action {
    public int layer = 115;
    Sprite spritePart;
    public static int offsetY = 0;
    public static int pokemonOffsetY = 0;
    public static boolean desertGrass = false;
    public static int timer = 0;
    // Prevents terrain being built from immediately appearing, 
    // because it makes the ui cluttered.
    public int terrainTimer = 0;

    public DrawPlayerUpper(Game game) {}

    public int getLayer(){return this.layer;}


    @Override
    public void step(Game game) {
        if (game.player.isSleeping) {
            if (game.player.sleepingDir == null) {
                game.player.zSprite.draw(game.mapBatch);
                game.mapBatch.draw(game.player.sleepingSprite, game.player.position.x, game.player.position.y);
            }
            else {
                Vector2 pos = game.player.sleepingDir.cpy().sub(game.player.position);
                game.mapBatch.draw(game.player.zSprite, game.player.zSprite.getX() + pos.x -12,
                                                        game.player.zSprite.getY() + pos.y +16);
                this.spritePart = new Sprite(game.player.currSprite);
                this.spritePart.setRegionY(game.player.spriteOffsetY);
                this.spritePart.setRegionHeight(6);
                game.mapBatch.draw(this.spritePart, game.player.sleepingDir.x, game.player.sleepingDir.y+12 +8);
            }
            return;
        }
        if (game.player.currFieldMove.equals("FLY")) {
            return;
        }
        // Have draw sleeping bag on ground separately for 'getting into sleeping bag' animation.
        if (game.player.drawSleepingBag) {
            game.mapBatch.draw(game.player.sleepingBagSprite,
                               game.player.sleepingBagSprite.getX(),
                               game.player.sleepingBagSprite.getY());
        }
        // Draw building tile if building
        if (game.player.currFieldMove.equals("BUILD")) {
            // Get direction facing
            Vector2 pos = game.player.facingPos();
            // Get game.player.currBuildTile and draw it in front of player
            Sprite sprite = new Sprite(game.player.currBuildTile.sprite);
            sprite.setAlpha(0.8f);
            sprite.setPosition(pos.x, pos.y);
            Tile nextTile = game.map.tiles.get(pos);
            boolean isTorch = nextTile != null && (game.player.currBuildTile.name.contains("torch") ||
                                                   game.player.currBuildTile.name.equals("house_clock1") ||
                                                   game.player.currBuildTile.name.contains("picture"));
            if ((isTorch && !nextTile.attrs.get("solid")) ||
                (nextTile != null && nextTile.attrs.get("solid"))) {
                sprite.setColor(1f, 0.7f, 0.7f, 0.8f);
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
            if (nextTile != null && game.player.currBuildTile.name.contains("house")) {
                Tile upTile = game.map.interiorTiles.get(game.map.interiorTilesIndex).get(nextTile.position.cpy().add(0, 16));
                requirementsMet = requirementsMet && (upTile == null || upTile.name.contains("house"));
            }
            if (isTorch) {
                requirementsMet = requirementsMet &&
                                  !nextTile.items().containsKey("torch") &&
                                  nextTile.attrs.get("solid") &&
                                  !nextTile.nameUpper.contains("roof");
                // Bunch of tiles that aren't allowed
                String[] notAllowedTiles = new String[]{"regi", "tree", "bush", "table",
                                                        "bed", "shelf", "plant"};
                for (String name : notAllowedTiles) {
                    requirementsMet = requirementsMet && !(nextTile.name.contains(name) || nextTile.nameUpper.contains(name));
                }
            }
            //
            boolean isBridge = game.player.currBuildTile.name.contains("bridge");
            if (isBridge) {
                requirementsMet = requirementsMet && nextTile != null && nextTile.name.contains("water") && !nextTile.name.contains("bridge");
            }

            if (!requirementsMet) {
                sprite.setColor(1f, 0.7f, 0.7f, 0.8f);
            }
            // TODO: instead of coloring sprite here, could color the batch.
            //       Have to also set batch alpha at this time.
            // Might have unintended effects, tho.
            // If batch has a color effect (like at night), ignore it
//            Color color = game.mapBatch.getColor();
//            game.mapBatch.setColor(1f, 1f, 1f, 1f);
            sprite.draw(game.mapBatch);
//            game.batch.draw(sprite, pos.x, pos.y);
            if (game.player.currBuildTile.overSprite != null) {
//                game.player.currBuildTile.overSprite.draw(game.batch, .7f);
                sprite = new Sprite(game.player.currBuildTile.overSprite);
                sprite.setAlpha(.8f);
                sprite.setPosition(pos.x, pos.y);
                if (nextTile != null && (nextTile.attrs.get("solid") || nextTile.nameUpper.contains("door"))) {
                    sprite.setColor(1f, .7f, .7f, .8f);
                }
                sprite.draw(game.mapBatch);
//                game.batch.draw(sprite, pos.x, pos.y);
            }
        }
        
        // Draw building tile if building
        else if (game.player.currFieldMove.equals("DIG")) {
            // Get direction facing
            Vector2 pos = game.player.facingPos();
            Tile nextTile = game.map.tiles.get(pos);
            if (nextTile != null) {
                if (nextTile.nameUpper.contains("hole") ||
                    nextTile.name.contains("water5")) {
                    if (this.terrainTimer < 25) {
                        this.terrainTimer++;
                    }
                    else {
                        game.player.currBuildTile = game.player.buildTiles.get(game.player.buildTileIndex);
                    }
                }
                else {
                    this.terrainTimer = 0;
                    game.player.currBuildTile = game.player.currDigTile;
                }
            }
            Sprite sprite = new Sprite(game.player.currBuildTile.sprite);
            // Get game.player.currDigTile and draw it in front of player
            sprite.setAlpha(0.8f);
            sprite.setPosition(pos.x, pos.y);
            sprite.draw(game.mapBatch);
        }

        // Draw plant if planting apricorn
        //  && game.playerCanMove ?
        if (game.player.currPlanting != null) {
            Vector2 pos = game.player.facingPos();
            Player.sproutSprite.setAlpha(.7f);
            Player.sproutSprite.setPosition(pos.x, pos.y+2);
            Player.sproutSprite2.setPosition(pos.x, pos.y);
            Tile nextTile = game.map.tiles.get(pos);
            if (game.player.currPlanting.contains("apricorn")) {
                if (nextTile != null &&
                    (!(nextTile.name.equals("green1") || 
                       nextTile.name.contains("flower") || 
                       nextTile.name.contains("desert")) ||
                     nextTile.nameUpper.contains("tree") ||
                     nextTile.attrs.get("solid"))) {
                    Player.sproutSprite.setColor(1f, .7f, .7f, .7f);
                    Player.sproutSprite2.setColor(1f, .7f, .7f, .7f);
                }
                else if (nextTile != null) {
                    Player.sproutSprite2.setColor(.7f, 1f, .7f, .7f);
                }
            }
            else if (game.player.currPlanting.equals("manure")) {
                if (nextTile != null &&
                    !nextTile.nameUpper.contains("fertilized") &&
                    nextTile.nameUpper.contains("tree_planted")) {
                    Player.sproutSprite2.setColor(.7f, 1f, .7f, .7f);
                }
                else if (nextTile != null) {
                    Player.sproutSprite2.setColor(1f, .7f, .7f, .7f);
                }
            }
            else if (game.player.currPlanting.equals("miracle seed")) {
                if (nextTile != null &&
                    !nextTile.name.contains("grass_planted") &&
                    (nextTile.name.equals("green1") || nextTile.name.contains("flower"))) {
                    Player.sproutSprite2.setColor(.7f, 1f, .7f, .7f);
                }
                else if (nextTile != null) {
                    Player.sproutSprite2.setColor(1f, .7f, .7f, .7f);
                }
            }
            if (Player.drawSproutSprite) {
                Player.sproutSprite2.draw(game.mapBatch);
                if (game.player.currPlanting.contains("apricorn")) {
                    Player.sproutSprite.draw(game.mapBatch);
                }
            }
            // TODO: If not any more in inventory, then should just stop using
        }

        this.spritePart = new Sprite(game.player.currSprite);
        

        // this.spritePart.setRegion(0,0,16,8);
        // this.spritePart.setRegionHeight(4);
        // this.spritePart.setSize(this.spritePart.getWidth(), 4);
        if (DrawPlayerUpper.desertGrass) {
            this.spritePart.setRegionY(game.player.spriteOffsetY);
            this.spritePart.setRegionHeight(13);
            game.mapBatch.draw(this.spritePart, game.player.position.x, game.player.position.y+7+DrawPlayerUpper.pokemonOffsetY);
        }
        else {
            this.spritePart.setRegionY(game.player.spriteOffsetY);
            this.spritePart.setRegionHeight(8);
            game.mapBatch.draw(this.spritePart, game.player.position.x, game.player.position.y+12+DrawPlayerUpper.pokemonOffsetY);
        }

        if (game.player.currFieldMove.equals("RIDE") && game.player.hmPokemon != null) {
            int offsetX = 0;
            int offsetY = 0;
            if (game.player.dirFacing.equals("up")) {
                offsetY += -2;
                if (game.player.hmPokemon.specie.name.equals("mamoswine")) {
                    offsetY += 5;
                }
            }
            if (game.player.dirFacing.equals("down")) {
                offsetY += 3;
                if (game.player.hmPokemon.specie.name.equals("ponyta") ||
                    game.player.hmPokemon.specie.name.equals("rapidash")) {
                    offsetY -= 1;
                }
                if (game.player.hmPokemon.specie.name.equals("mamoswine")) {
                    offsetY += 2;
                }
                if (game.player.hmPokemon.specie.name.equals("rhyhorn")) {
                    offsetY += 1;
                }
            }
            else if (game.player.dirFacing.equals("right")) {
                offsetX += -4;
                if (game.player.hmPokemon.specie.name.equals("ninetales") ||
                    game.player.hmPokemon.specie.name.equals("arcanine")) {
                    offsetX += 1;
                }
                if (game.player.hmPokemon.specie.name.equals("ponyta") ||
                    game.player.hmPokemon.specie.name.equals("rapidash")) {
                    offsetX += 2;
                }
                if (game.player.hmPokemon.specie.name.equals("mamoswine")) {
                    offsetY += 5;
                    offsetX += 2;
                }
                if (game.player.hmPokemon.specie.name.equals("donphan")) {
                    offsetY += 2;
                    offsetX += 2;
                }
                if (game.player.hmPokemon.specie.name.equals("rhyhorn")) {
                    offsetY += 1;
                    offsetX += 1;
                }
            }
            else if (game.player.dirFacing.equals("left")) {
                offsetX += 4;
                if (game.player.hmPokemon.specie.name.equals("ninetales") ||
                    game.player.hmPokemon.specie.name.equals("arcanine")) {
                    offsetX -= 1;
                }
                if (game.player.hmPokemon.specie.name.equals("ponyta") ||
                    game.player.hmPokemon.specie.name.equals("rapidash")) {
                    offsetX -= 2;
                }
                if (game.player.hmPokemon.specie.name.equals("mamoswine")) {
                    offsetY += 5;
                    offsetX -= 2;
                }
                if (game.player.hmPokemon.specie.name.equals("donphan")) {
                    offsetY += 2;
                    offsetX -= 2;
                }
                if (game.player.hmPokemon.specie.name.equals("rhyhorn")) {
                    offsetY += 1;
                    offsetX -= 1;
                }
            }
            game.player.hmPokemon.currOwSprite = game.player.hmPokemon.standingSprites.get(game.player.dirFacing);
            this.spritePart = new Sprite(game.player.hmPokemon.currOwSprite);
            this.spritePart.setRegionY(game.player.hmPokemon.spriteOffsetY);
            this.spritePart.setRegionHeight(14);
            game.mapBatch.draw(this.spritePart, game.player.position.x+offsetX, game.player.position.y+10+offsetY+DrawPlayerUpper.offsetY+DrawPlayerUpper.pokemonOffsetY);

            if (game.player.dirFacing.equals("down") && 
                !game.player.hmPokemon.specie.name.equals("rhyhorn")) {
                this.spritePart = new Sprite(game.player.currSprite);
                this.spritePart.setRegionY(game.player.spriteOffsetY);
                this.spritePart.setRegionHeight(8);
                game.mapBatch.draw(this.spritePart, game.player.position.x, game.player.position.y+12+DrawPlayerUpper.pokemonOffsetY);
            }
        }
        
        
        // Draw grass above player if applicable
        if (DrawPlayerUpper.desertGrass) {
            if (DrawPlayerUpper.timer < 6) {
                if (DrawPlayerUpper.timer == 0) {
                    Player.desertGrassSprite.setRegion(16, 0, 16, 16);
//                    game.insertAction(new PlaySound("seed1", .5f, true, null));
                    game.insertAction(new PlaySound("sounds/sand2", .4f, true, null));
                }
                game.mapBatch.draw(Player.desertGrassSprite,
                                   game.player.position.x,
                                   game.player.position.y +7);
            }
            else if (DrawPlayerUpper.timer < 12) {
                if (DrawPlayerUpper.timer == 6) {
                    Player.desertGrassSprite.setRegion(0, 0, 16, 16);
                }
                game.mapBatch.draw(Player.desertGrassSprite,
                                   game.player.position.x,
                                   game.player.position.y +7);
            }
            else if (DrawPlayerUpper.timer < 16) {
                if (DrawPlayerUpper.timer == 12) {
                    Player.desertGrassSprite.setRegion(16, 0, 16, 16);
                }
                game.mapBatch.draw(Player.desertGrassSprite,
                                   game.player.position.x,
                                   game.player.position.y +7);
            }
            if (DrawPlayerUpper.timer < 16) {
                DrawPlayerUpper.timer++;
            }
        }

        // This needs to be set to detect collision
        game.player.currSprite.setPosition(game.player.position.x, game.player.position.y);
    }
}

// Demo of drawing sprites for ea pokemon caught
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
        // This to prevent player from re-planting on this tile
        this.currTile.nameUpper = "tree_planted2";
    }

    @Override
    public void step(Game game) {
        Player.drawSproutSprite = false;
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
            Player.drawSproutSprite = true;
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
 * 
 * TODO: need some class OverworldThing that player and pokemon extend from
 *  - facingPos(), pokemon.prevOwner is type OverworldThing so pokemon-parents can own egg (eh?)
 *  - Emote() likely the same for both.
 */
public class Player {
    // current player direction
    public String dirFacing = "down";
    
//    public Direction dirFace = Direction.UP;  // TODO: replace dirFacing and checks

    // Movement sprites
    Map<String, Sprite> standingSprites = new HashMap<String, Sprite>();
    Map<String, Sprite> movingSprites = new HashMap<String, Sprite>();
    Map<String, Sprite> altMovingSprites = new HashMap<String, Sprite>();
    
    public Pokemon hmPokemon = null;  // Pokemon currently using a Field Move
    public int spriteOffsetY = 0;

    public Vector2 position;
    public Sprite currSprite = new Sprite();

    SpriteProxy battleSprite;
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

    // TODO: remove after refactor
//    boolean isBuilding = false; // player has building tile in front of them
//    boolean isCutting = false; // player will try to cut tree on A press
//    boolean isSmashing = false; // rock smash
//    boolean isHeadbutting = false; // player will try to headbutt tree on A press
//    boolean isJumping = false; // player will jump up ledges using 'fast' ledge jump animation.
//    boolean isAttacking = false;
    public String currFieldMove = "";
    Tile currDigTile;
    {
        currDigTile = new Tile("hole1", new Vector2(0,0));
    }
    //

    public boolean isCrafting = false;
    public boolean isSleeping = false;
    public Vector2 sleepingDir = null;
//    public boolean isFlying = false;
    public Flying flyingAction = null;
    public boolean drawSleepingBag = false;
    public boolean acceptInput = true;
    boolean isNearCampfire = false;
    Tile currBuildTile; // which tile will be built next
    int buildTileIndex = 0;
//    Tile currTerrainTile; // which tile will be built next
//    int terrainTileIndex = 0;
    ArrayList<Tile> terrainTiles = new ArrayList<Tile>();
    // Points to indoor/outdoor building tiles
    ArrayList<Tile> buildTiles = new ArrayList<Tile>();
    // Anything that can be built indoor and outdoor needs to be at
    // the beginning of these lists, in the same order
    ArrayList<Tile> outdoorBuildTiles = new ArrayList<Tile>();
    ArrayList<Tile> indoorBuildTiles = new ArrayList<Tile>();
    ArrayList<Tile> desertBuildTiles = new ArrayList<Tile>();
    HashMap<String, HashMap<String, Integer>> buildTileRequirements = new HashMap<String, HashMap<String, Integer>>();
    public boolean canMove = true;  // TODO: migrate to start using this
    int numFlees = 0;  // used in battle run away mechanic
    Sprite zSprite;
    int zsTimer = 0;
    public int repelCounter = 0;  // counts down, item sets at 100/200.
    Vector2 spawnLoc = new Vector2(0, 0);
    int spawnIndex = -1;  // -1 == overworld, anything else is the interiorTilesIndex
    PlayerStanding standingAction;
    public boolean displayedMaxPartyText = false;
    
    public int eggStepTimer = 0;
    public boolean nearAggroPokemon = false;
    public boolean nearCacturne = false;

    // gold, kris, red, green
    String character = "gold";
    Color skinColor = new Color(1f, 0.8078431372549019607843137254902f, 0.28235294117647058823529411764706f, 1f);

    public String currPlanting = null;  // Which field move player is currently using
    public static Sprite sproutSprite;
    public static Sprite sproutSprite2;
    public static boolean drawSproutSprite = true;  // briefly false after planting something
    static {
        Texture text = new Texture(Gdx.files.internal("sprout_sheet2.png"));
        sproutSprite = new Sprite(text, 16*4, 0, 16, 16);
        text = new Texture(Gdx.files.internal("tiles/place_something1.png"));
        sproutSprite2 = new Sprite(text, 0, 0, 16, 16);
    }

    public static Sprite desertGrassSprite;
    static {
        Texture text = new Texture(Gdx.files.internal("grass_over_sheet3.png"));
        desertGrassSprite = new Sprite(text, 0, 0, 16, 16);
    }

    ArrayList<String> alreadyDoneHarvestables = new ArrayList<String>();
    // up=down, left=right etc.
    public static HashMap<String, String> oppDirs = new HashMap<String, String>();
    static {
        Player.oppDirs.put("up", "down");
        Player.oppDirs.put("down", "up");
        Player.oppDirs.put("right", "left");
        Player.oppDirs.put("left", "right");
    }

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
//        Craft craft = new Craft("REGISTEEL", 1);
//        craft.requirements.add(new Craft("black apricorn", 1));
//        Player.crafts.add(craft);
        
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

        //
        craft = new Craft("soft bedding", 1);
        craft.requirements.add(new Craft("soft feather", 3));
        craft.requirements.add(new Craft("silky thread", 3));
        Player.crafts.add(craft);
//        craft = new Craft("HQ bedding", 1);
//        craft.requirements.add(new Craft("soft bedding", 1));
//        craft.requirements.add(new Craft("soft wool", 3));
//        Player.crafts.add(craft);
        craft = new Craft("repel", 1);
        craft.requirements.add(new Craft("manure", 2));  // + charcoal? moomoo milk?
        Player.crafts.add(craft);  // max repel? gloom - foul ooze?
        // charcoal makes sense b/c it's like it makes it go farther by making it a 'powder'
        craft = new Craft("max repel", 1);
        craft.requirements.add(new Craft("charcoal", 2));
        craft.requirements.add(new Craft("repel", 1));
        Player.crafts.add(craft);
        // TODO: make harder to get, maybe add gold berry to reqs
        // mysticpowder? stardust? ancientpowder (unowns?)?
        craft = new Craft("rare candy", 1);
        craft.requirements.add(new Craft("berry juice", 3));
        craft.requirements.add(new Craft("ancientpowder", 1));
        Player.crafts.add(craft);
        //
        craft = new Craft("poké ball", 1);
        craft.requirements.add(new Craft("magnet", 1));
        craft.requirements.add(new Craft("hard shell", 1));
        Player.crafts.add(craft);
        craft = new Craft("great ball", 1);
        craft.requirements.add(new Craft("poké ball", 1));
        craft.requirements.add(new Craft("metal coat", 1));
        Player.crafts.add(craft);
        craft = new Craft("ultra ball", 1);
        craft.requirements.add(new Craft("great ball", 1));
        craft.requirements.add(new Craft("psi energy", 1));  // dusk ball? nugget + dark energy? idk.
        Player.crafts.add(craft);
        craft = new Craft("dusk ball", 1);
        craft.requirements.add(new Craft("ultra ball", 1));
        craft.requirements.add(new Craft("dark energy", 1));  // dusk ball? nugget + dark energy? idk.
        Player.crafts.add(craft);
        //
        craft = new Craft("clear glass", 1);
        craft.requirements.add(new Craft("soft sand", 3));
        Player.crafts.add(craft);
        craft = new Craft("silph scope", 1);
        craft.requirements.add(new Craft("clear glass", 2));
        craft.requirements.add(new Craft("metal coat", 2));
        Player.crafts.add(craft);
        //
        craft = new Craft("binding band", 1);
        craft.requirements.add(new Craft("grass", 3));
        Player.crafts.add(craft);
        //
        craft = new Craft("thin paper", 1);
        craft.requirements.add(new Craft("log", 1));
        Player.crafts.add(craft);
        //
        craft = new Craft("ragecandybar", 1);
        craft.requirements.add(new Craft("honey", 2));
//        craft.requirements.add(new Craft("miracle seed", 1));
        craft.requirements.add(new Craft("charcoal", 1));
        Player.crafts.add(craft);
    }

    public ArrayList<Craft> fossilCrafts = new ArrayList<Craft>();
    {
        Craft craft = new Craft("AERODACTYL", 1);
        craft.requirements.add(new Craft("old amber", 1));
        fossilCrafts.add(craft);
        craft = new Craft("OMANYTE", 1);
        craft.requirements.add(new Craft("helix fossil", 1));
        fossilCrafts.add(craft);
        craft = new Craft("KABUTO", 1);
        craft.requirements.add(new Craft("dome fossil", 1));
        fossilCrafts.add(craft);
        craft = new Craft("ANORITH", 1);
        craft.requirements.add(new Craft("claw fossil", 1));
        fossilCrafts.add(craft);
        craft = new Craft("LILEEP", 1);
        craft.requirements.add(new Craft("root fossil", 1));
        fossilCrafts.add(craft);
        craft = new Craft("SHIELDON", 1);
        craft.requirements.add(new Craft("shield fossil", 1));
        fossilCrafts.add(craft);
        craft = new Craft("CRANIDOS", 1);
        craft.requirements.add(new Craft("skull fossil", 1));
        fossilCrafts.add(craft);
    }

    // TODO: Needs to be loaded from save data.
    public ArrayList<Craft> regiCrafts = new ArrayList<Craft>();
    {
        Craft craft = new Craft("REGISTEEL", 1);
        craft.requirements.add(new Craft("metal coat", 70));
        craft.requirements.add(new Craft("spell tag", 1));
        regiCrafts.add(craft);
        craft = new Craft("REGIROCK", 1);
        craft.requirements.add(new Craft("hard stone", 70));
        craft.requirements.add(new Craft("spell tag", 1));
        regiCrafts.add(craft);
        craft = new Craft("REGICE", 1);
        craft.requirements.add(new Craft("nevermeltice", 70));
        craft.requirements.add(new Craft("spell tag", 1));
        regiCrafts.add(craft);
        craft = new Craft("REGIDRAGO", 1);
        craft.requirements.add(new Craft("dragon scale", 70));
        craft.requirements.add(new Craft("dragon fang", 2));
        craft.requirements.add(new Craft("spell tag", 1));
        regiCrafts.add(craft);
        craft = new Craft("REGIELEKI", 1);
        craft.requirements.add(new Craft("magnet", 70));
        craft.requirements.add(new Craft("binding band", 2));
        craft.requirements.add(new Craft("spell tag", 1));
        regiCrafts.add(craft);
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

        // Battle portrait sprite
//        Texture text = new Texture(Gdx.files.internal("battle/player_back1.png"));
//        this.battleSprite = new Sprite(text, 0, 0, 28, 28);
        text = new Texture(Gdx.files.internal("battle/player_back_color1.png"));
        this.battleSprite = new SpriteProxy(text, 0, 0, 45, 46);
        text = new Texture(Gdx.files.internal("tiles/sleeping_bag1_using.png"));
        this.sleepingSprite = new Sprite(text, 0, 0, 24, 16);
        text = new Texture(Gdx.files.internal("tiles/sleeping_bag1.png"));
        this.sleepingBagSprite = new Sprite(text, 0, 0, 24, 16);

        // Set initial position
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
        this.outdoorBuildTiles.add(new Tile("torch1", new Vector2(0,0)));
        this.outdoorBuildTiles.add(new Tile("house5_door1", new Vector2(0,0)));
//        this.buildTiles.add(new Tile("house5_left1", new Vector2(0,0)));
        this.outdoorBuildTiles.add(new Tile("house5_middle1", new Vector2(0,0)));
//        this.buildTiles.add(new Tile("house5_right1", new Vector2(0,0)));
        this.outdoorBuildTiles.add(new Tile("house5_roof_middle1", new Vector2(0,0)));
//        this.buildTiles.add(new Tile("house5_roof_left1", new Vector2(0,0)));
//        this.buildTiles.add(new Tile("house5_roof_right1", new Vector2(0,0)));
        this.outdoorBuildTiles.add(new Tile("campfire1", new Vector2(0,0)));
        this.outdoorBuildTiles.add(new Tile("fence1", new Vector2(0,0)));
        this.outdoorBuildTiles.add(new Tile("bridge1", new Vector2(0,0)));

        // 
        this.desertBuildTiles.add(new Tile("torch1", new Vector2(0,0)));
        this.desertBuildTiles.add(new Tile("house6_door1", new Vector2(0,0)));
        this.desertBuildTiles.add(new Tile("house6_NEW", new Vector2(0,0)));
        this.desertBuildTiles.add(new Tile("house6_roof_middle1", new Vector2(0,0)));
        this.desertBuildTiles.add(new Tile("campfire1", new Vector2(0,0)));
        this.desertBuildTiles.add(new Tile("fence2", new Vector2(0,0)));
        this.desertBuildTiles.add(new Tile("potted1", new Vector2(0,0)));
        this.desertBuildTiles.add(new Tile("potted2", new Vector2(0,0)));
        this.desertBuildTiles.add(new Tile("bridge1", new Vector2(0,0)));
        
        // 
        this.indoorBuildTiles.add(new Tile("torch1", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_plant1", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_plant2", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_gym1", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_shelf1", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_wardrobe1", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_stool1", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_bed1", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_table1_default", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_carpet1", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_carpet2", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_carpet3", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_carpet4", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_carpet5", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_carpet6", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_carpet7", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_carpet8", new Vector2(0,0)));
        
        // 
        this.indoorBuildTiles.add(new Tile("house_clock1", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_picture1", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_picture2", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_picture3", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_picture4", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_picture5", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_picture6", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_picture7", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_picture8", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_picture9", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_picture10", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_picture11", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_picture12", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_picture13", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_picture14", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_picture15", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_picture16", new Vector2(0,0)));
        this.indoorBuildTiles.add(new Tile("house_picture17", new Vector2(0,0)));

        // TODO: remove
//        this.buildTiles.add(new Tile("sleeping_bag1", new Vector2(0,0)));

        this.buildTiles = this.outdoorBuildTiles;
        this.currBuildTile = this.buildTiles.get(this.buildTileIndex);
        

        this.terrainTiles.add(new Tile("sand1", new Vector2(0,0)));
        this.terrainTiles.add(new Tile("desert4", new Vector2(0,0)));
        this.terrainTiles.add(new Tile("mountain1", new Vector2(0,0)));
        this.terrainTiles.add(new Tile("snow1", new Vector2(0,0)));
        this.terrainTiles.add(new Tile("green1", new Vector2(0,0)));
        this.terrainTiles.add(new Tile("flower4", new Vector2(0,0)));

        // for now, everything requires same amt
        // TODO: convert this to static Player var.
        ArrayList<Tile> allTiles = new ArrayList<Tile>(this.outdoorBuildTiles);
        allTiles.addAll(this.indoorBuildTiles);
        allTiles.addAll(this.desertBuildTiles);
        allTiles.addAll(this.terrainTiles);
        for (Tile tile : allTiles) {
            if (tile.nameUpper.equals("sleeping_bag1")) {
                continue;
            }
            HashMap<String, Integer> requirements = new HashMap<String, Integer>();
            this.buildTileRequirements.put(tile.name, requirements);
            if (tile.name.contains("campfire")) {
                requirements.put("log", 4);
                requirements.put("grass", 2);
                continue;
            }
//            else if (tile.name.equals("hq bed")) {
//                this.buildTileRequirements.get(tile.name).put("log", 4);
//                this.buildTileRequirements.get(tile.name).put("hq bedding", 1);
//                continue;
//            }
            else if (tile.name.contains("bridge")) {
                requirements.put("log", 1);
                requirements.put("hard stone", 1);
                continue;
            }
            else if (tile.name.equals("house_bed1")) {
                requirements.put("log", 4);
                requirements.put("soft bedding", 1);
                continue;
            }
            else if (tile.name.equals("house_gym1")) {
                requirements.put("hard stone", 2);
                continue;
            }
            else if (tile.name.contains("house_plant")) {
                requirements.put("log", 1);
                requirements.put("miracle seed", 1);
                continue;
            }
            else if (tile.name.contains("carpet")) {
                requirements.put("grass", 1);
                continue;
            }
            else if (tile.name.contains("picture")) {
                requirements.put("thin paper", 1);
                continue;
            }
            else if (tile.name.contains("clock")) {
                requirements.put("log", 1);
                requirements.put("clear glass", 1);
                continue;
            }
            else if (tile.name.contains("house6_")) {
                requirements.put("hard stone", 1);
                continue;
            }
            else if (tile.name.contains("potted")) {
                requirements.put("hard stone", 1);
                requirements.put("miracle seed", 1);
                continue;
            }
            else if (tile.name.contains("house_")) {
                requirements.put("log", 1);
                continue;
            }
            else if (tile.name.equals("sand1")) {
                requirements.put("soft sand", 1);
                continue;
            }
            else if (tile.name.equals("desert4")) {
                requirements.put("dry sand", 1);
                continue;
            }
            else if (tile.name.equals("mountain1")) {
                requirements.put("light clay", 1);
                continue;
            }
            else if (tile.name.equals("snow1")) {
                requirements.put("nevermeltice", 1);
                continue;
            }
            else if (tile.name.equals("green1")) {
                requirements.put("grass", 1);
                continue;
            }
            else if (tile.name.equals("flower4")) {
                requirements.put("flowers", 1);
                continue;
            }
            requirements.put("grass", 1);
            requirements.put("log", 1);
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
        this.itemsDict.put("Sleeping Bag", 1);  // TODO: remove caps on this
        this.itemsDict.put("escape rope", 1);
//        this.itemsDict.put("Safari Ball", 99);
        // TODO: debug, remove
//        this.itemsDict.put("grass", 99);
//        this.itemsDict.put("log", 99);
//        this.itemsDict.put("blue apricorn", 99);
//        this.itemsDict.put("ultra ball", 99);
//        this.itemsDict.put("blue apricorn", 99);
//        this.itemsDict.put("Poké Ball", 99);
        if (Game.staticGame.debugInputEnabled) {
            this.itemsDict.put("secret key", 1);
            this.itemsDict.put("master ball", 99);
            this.itemsDict.put("grass", 99);
            this.itemsDict.put("log", 99);
            this.itemsDict.put("black apricorn", 99);
            this.itemsDict.put("green apricorn", 3);
            this.itemsDict.put("pink apricorn", 2);
            this.itemsDict.put("manure", 99);
            this.itemsDict.put("berry juice", 99);
            this.itemsDict.put("moomoo milk", 99);
            this.itemsDict.put("ancientpowder", 99);
            this.itemsDict.put("silph scope", 1);
            this.itemsDict.put("soft sand", 18);
            this.itemsDict.put("metal coat", 99);
            this.itemsDict.put("hard stone", 99);
            this.itemsDict.put("nevermeltice", 99);
            this.itemsDict.put("dragon scale", 99);
            this.itemsDict.put("dragon fang", 99);
            this.itemsDict.put("magnet", 99);
            this.itemsDict.put("binding band", 2);
            this.itemsDict.put("spell tag", 88);
            this.itemsDict.put("moon ball", 99);
            this.itemsDict.put("love ball", 99);
            this.itemsDict.put("heavy ball", 99);
            this.itemsDict.put("level ball", 99);
            this.itemsDict.put("soft bedding", 99);;
            this.itemsDict.put("miracle seed", 99);
            this.itemsDict.put("great ball", 99);
            this.itemsDict.put("ultra ball", 99);
            this.itemsDict.put("dark energy", 99);
            this.itemsDict.put("thin paper", 99);
            this.itemsDict.put("hard stone", 99);
            this.itemsDict.put("old amber", 99);
            this.itemsDict.put("helix fossil", 99);
            this.itemsDict.put("dome fossil", 99);
            this.itemsDict.put("root fossil", 99);
            this.itemsDict.put("claw fossil", 99);
            this.itemsDict.put("shield fossil", 99);
            this.itemsDict.put("skull fossil", 99);
            this.itemsDict.put("flowers", 99);
            this.itemsDict.put("light clay", 99);
            this.itemsDict.put("ragecandybar", 99);
        }

        this.network = new Network(this.position);
        this.type = Type.LOCAL;
    }

    /**
     * Constructor to load from serialized class (ie data sent over network or loaded from file).
     */
    public Player(PlayerDataBase playerData) {
        this();
        if (playerData instanceof PlayerDataV06) {
            this.spawnIndex = ((PlayerDataV06)playerData).spawnIndex;
        }
        this.spawnLoc = playerData.spawnLoc;
        this.dirFacing = playerData.dirFacing;
        this.position = playerData.position;
        this.name = playerData.name;
        this.pokemon = new ArrayList<Pokemon>();
        for (com.pkmngen.game.Network.PokemonDataBase pokemonData : playerData.pokemon) {
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
            this.buildTiles = this.indoorBuildTiles;
        }
        else {
            this.network.tiles = Game.staticGame.map.overworldTiles;
        }
        if (playerData.isFlying) {
            this.currFieldMove = "FLY";
        }
    }

    /**
     * Check if player has required materials for a craft.
     */
    public boolean hasCraftRequirements(ArrayList<Craft> crafts, int craftIndex, int amount) {
        boolean hasRequirements = true;
        for (Player.Craft req : crafts.get(craftIndex).requirements) {
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
     * TODO: this is going to become a generic check called after each time
     * the player moves to fill in various values.
     * 
     * Check if player is near campfire (needed to know if ghost can spawn or not).
     */
    public boolean checkNearCampfire() {
        // true if player is near an aggro-d Pokemon
        // player can't use campfire in this case.
        this.nearAggroPokemon = false;
        this.nearCacturne = false;
        // Automatically near a campfire if the pokemon following the player knows flash
        boolean foundCampfire = this.hmPokemon != null && this.hmPokemon.hms.contains("FLASH");
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
                foundCampfire = true;
            }
            else if (tile.items().containsKey("torch")) {
                // TODO: probably only check if within small radius
                foundCampfire = true;
            }
            else if (tile.nameUpper.equals("cactus2_cacturne")) {
                this.nearCacturne = true;
            }
            else if (Game.staticGame.map.pokemon.containsKey(currPos)) {
                Pokemon pokemon = Game.staticGame.map.pokemon.get(currPos);
                if (pokemon.aggroPlayer) {
                    this.nearAggroPokemon = true;
                }
                if (pokemon.hms.contains("FLASH")) {
                    foundCampfire = true;
                }
            }
                    
            // TODO: remove
//            else if (Game.staticGame.map.pokemon.containsKey(currPos) &&
//                     Game.staticGame.map.pokemon.get(currPos).name.equals("egg")) {
//                this.nearbyEggs.put(Game.staticGame.map.pokemon.get(currPos), Math.abs(i) + Math.abs(j));
//            }
        }
        return foundCampfire;
    }

    /**
     * Deduct required materials from player inventory and add crafted item to inventory.
     */
    public void craftItem(ArrayList<Player.Craft> crafts, int craftIndex, int amount) {
        // Remove required materials from player inventory
        Craft craft = crafts.get(craftIndex);
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

    /**
     * Used for hms where player controls pokemon.
     */
    public void swapSprites(Pokemon pokemon) {
        Map<String, Sprite> tempSprites = this.standingSprites;
        this.standingSprites = pokemon.standingSprites;
        pokemon.standingSprites = tempSprites;
        tempSprites = this.movingSprites;
        this.movingSprites = pokemon.movingSprites;
        pokemon.movingSprites = tempSprites;
        tempSprites = this.altMovingSprites;
        this.altMovingSprites = pokemon.altMovingSprites;
        pokemon.altMovingSprites = tempSprites;
        int temp = this.spriteOffsetY;
        this.spriteOffsetY = pokemon.spriteOffsetY;
        pokemon.spriteOffsetY = temp;
        this.hmPokemon = pokemon;  // TODO: probably shouldn't be in here
    }

    /**
     * Draw emote animation above the player.
     */
    public class Emote extends Action {
        public int layer = 109;
        String type;
        Sprite sprite;
        HashMap<String, Sprite> sprites = new HashMap<String, Sprite>();
        int timer = 0;

        public int getLayer(){return this.layer;}
        
        public Emote(String type, Action nextAction) {
            this.type = type;
            Texture text = TextureCache.get(Gdx.files.internal("emotes.png"));
            int i = 0;
            for (String name : new String[]{"!", "?", "happy", "skull", "heart", "bolt", "sleep", "fish", "uncomfortable", "..."}) {
                sprites.put(name, new Sprite(text, 16*i, 0, 16, 16));
                i++;
            }
            this.nextAction = nextAction;
        }

        @Override
        public void firstStep(Game game) {
            this.sprite = this.sprites.get(this.type);
        }

        @Override
        public void step(Game game) {
            game.mapBatch.draw(this.sprite, Player.this.position.x, Player.this.position.y +4 +16);
            if (this.timer >= 60) {
                game.actionStack.remove(this);
                game.insertAction(this.nextAction);
            }
            this.timer++;
        }
    }
    

    public class Flying extends Action {
        Pokemon pokemon;
        Sprite shadow;
        int animIndex = 0;
        int xOffset = 0;
        int yOffset = 32;
        int yOffsetCounter = 0;
        boolean acceptInput = true;
        Sprite spritePart;
        boolean takingOff = false;
        public Color batchColor;

        public Flying(Pokemon pokemon, boolean takingOff, Action nextAction) {
            this.pokemon = pokemon;
            this.takingOff = takingOff;
            Texture text = TextureCache.get(Gdx.files.internal("shadow1.png"));
            this.shadow = new Sprite(text, 0, 0, 16, 16);
            this.nextAction = nextAction;
        }

        @Override
        public void firstStep(Game game) {
            game.insertAction(this.nextAction);
            Player.this.flyingAction = this;
            // When loading from save, this.takingOff = false to avoid doing the animation.
            if (this.takingOff) {
                game.insertAction(this.new TakingOff(true, null));
                this.yOffset = 0;
            }
            game.player.hmPokemon = this.pokemon; // TODO: test this
        }

        @Override
        public void step(Game game) {
            // Draw drop shadow
            game.mapBatch.draw(this.shadow, Player.this.position.x, Player.this.position.y);

            // Draw flying Pokemon sprite.
            int otherOffsetY = 0;
            if (this.animIndex < 80-12) {
                this.pokemon.currOwSprite = this.pokemon.standingSprites.get(Player.this.dirFacing);
            }
            else if (this.animIndex < 80-8) {
                this.pokemon.currOwSprite = this.pokemon.movingSprites.get(Player.this.dirFacing);
                otherOffsetY = -2;
            }
            else if (this.animIndex < 80-4) {
                this.pokemon.currOwSprite = this.pokemon.standingSprites.get(Player.this.dirFacing);
            }
            else if (this.animIndex < 80) {
                this.pokemon.currOwSprite = this.pokemon.movingSprites.get(Player.this.dirFacing);
                otherOffsetY = -2;
            }

            // Draw player riding
            int offsetX = 0;
            int offsetY = 0;
            if (game.player.dirFacing.equals("up")) {
                offsetY += 1;
            }
            if (game.player.dirFacing.equals("down")) {
                offsetY += 2;
            }
            else if (game.player.dirFacing.equals("right")) {
                offsetX += -2;
            }
            else if (game.player.dirFacing.equals("left")) {
                offsetX += 2;
            }
            Player.this.currSprite = new Sprite(Player.this.standingSprites.get(Player.this.dirFacing));
            this.spritePart = new Sprite(game.player.currSprite);
            this.spritePart.setRegionY(0);
            this.spritePart.setRegionHeight(14);
            this.batchColor = game.mapBatch.getColor();
            if (this.pokemon.hms.contains("FLASH")) {
                game.mapBatch.setColor(1f, 1f, 1f, 1f);
            }
            if (Player.this.dirFacing.equals("down")) {
                game.mapBatch.draw(this.spritePart, game.player.position.x+offsetX+this.xOffset, game.player.position.y+12+9+offsetY+this.yOffset);
            }
            // TODO: probably use +otherOffsetY for sprites of specific pokemon.
            this.pokemon.position.set(Player.this.position.x+this.xOffset, Player.this.position.y+(16+this.yOffset)/2);  // used for drawing campfire aura
            game.mapBatch.draw(this.pokemon.currOwSprite, Player.this.position.x+this.xOffset, Player.this.position.y+16+this.yOffset);  //+otherOffsetY

            if (!Player.this.dirFacing.equals("down")) {
                game.mapBatch.draw(this.spritePart, game.player.position.x+offsetX+this.xOffset, game.player.position.y+12+9+offsetY+this.yOffset);
            }
            game.mapBatch.setColor(this.batchColor);

            if (this.takingOff) {
                return;
            }

            this.animIndex = (this.animIndex + 1) % 80;
            this.yOffsetCounter++;
            if (this.yOffsetCounter % 4 == 0) {
                if ((this.yOffsetCounter % 32) > 15) {
                    this.yOffset++;
                }
                else {
                    this.yOffset--;
                }
            }
            // Didn't quite like the way this looked.
            // Added a 'swaying' motion.
//            if (this.yOffsetCounter % 64 == 0) {
//                if (this.yOffsetCounter > 95 && this.yOffsetCounter <= 192) {
//                    this.xOffset--;
//                }
//                else {
//                    this.xOffset++;
//                }
//            }
            if (this.yOffsetCounter % 32 >= 31) {
                this.yOffset = 32;
            }
            if (this.yOffsetCounter >= 255) {
                this.yOffsetCounter = 0;
                this.xOffset = 0;
            }
            if (!Player.this.canMove) {
                return;
            }
            // Check if player wants to access the menu
            if (InputProcessor.startJustPressed) {
                game.insertAction(new DrawPlayerMenu.Intro(game,
                                  new DrawPlayerMenu(game,
                                  new WaitFrames(game, 1,
                                  new SetField(Player.this, "canMove", true,
                                  null)))));
                Player.this.canMove = false;
                return;
            }

            if (!this.acceptInput) {
                return;
            }

            boolean shouldMove = false;
            // Accept player input, change direction depending
            if (InputProcessor.upPressed) {
                game.player.dirFacing = "up";
                if (Player.this.position.y < game.map.topRight.y-32) {
                    shouldMove = true;
                }
            }
            else  if (InputProcessor.downPressed) {
                game.player.dirFacing = "down";
                if (Player.this.position.y > game.map.bottomLeft.y+32) {
                    shouldMove = true;
                }
            }
            else  if (InputProcessor.leftPressed) {
                game.player.dirFacing = "left";
                if (Player.this.position.x > game.map.bottomLeft.x+32) {
                    shouldMove = true;
                }
            }
            else  if (InputProcessor.rightPressed) {
                game.player.dirFacing = "right";
                if (Player.this.position.x < game.map.topRight.x-32) {
                    shouldMove = true;
                }
            }
            // If player presses B, stop flying.
            else if (InputProcessor.bJustPressed) {
                if (!game.map.tiles.get(game.player.position).attrs.get("solid") &&
                    !game.map.tiles.get(game.player.position).attrs.get("ledge")) {
//                    Player.this.isFlying = false;  // TODO: do this after outro anim (setfield)
//                    game.cam.translate(0f, -16f);  // TODO: do this after outro anim (setfield?)
//                    game.actionStack.remove(this);
//                    game.insertAction(Player.this.standingAction);  // TODO: enable once standingAction works.
                    this.takingOff = true;
                    game.insertAction(this.new TakingOff(false,
                                      new PlayerStanding(game)));
                    return;
                }
            }
            if (shouldMove) {
                this.acceptInput = false;
                game.insertAction(this.new Moving());
            }
        }

        public class Moving extends Action {
            int timer = 0;

            @Override
            public void step(Game game) {
                if (Player.this.dirFacing.equals("up")) {
                    Player.this.position.y += 2;
                }
                else if (Player.this.dirFacing.equals("down")) {
                    Player.this.position.y -= 2;
                }
                else if (Player.this.dirFacing.equals("right")) {
                    Player.this.position.x += 2;
                }
                else if (Player.this.dirFacing.equals("left")) {
                    Player.this.position.x -= 2;
                }
                game.cam.position.set(Player.this.position.x+16, Player.this.position.y+16, 0);
                this.timer++;
                if (this.timer >= 8) {
                    game.actionStack.remove(this);
                    Flying.this.acceptInput = true;
                }
            }
        }

        public class TakingOff extends Action {
            int timer = 0;
            boolean takingOff = true;
            
            public TakingOff(boolean takingOff, Action nextAction) {
                this.takingOff = takingOff;
                this.nextAction = nextAction;
            }

            @Override
            public void firstStep(Game game) {
                if (this.takingOff) {
                    game.insertAction(new PlaySound("fly_takingoff1", null));
                }
                else {
                    game.insertAction(new PlaySound("fly_landing1", null));
                }
            }

//            @Override
//            public int getLayer() {
//                return Flying.this.getLayer()-1;
//            }

            @Override
            public void step(Game game) {
                int extra = 0;
                if (this.takingOff && this.timer >= 64) {
                    extra = 8;
                }
                else if (this.timer % 4 == 0) {
                    if (this.takingOff) {
                        Flying.this.yOffset+=2;
                    }
                    else {
                        Flying.this.yOffset-=2;
                    }
                }
                // Flap the wings
                Flying.this.animIndex = ((Flying.this.animIndex-80 + 1) % (8+extra)) +80;
                if (Flying.this.animIndex < 84+(extra/2)) {
                    Flying.this.pokemon.currOwSprite = Flying.this.pokemon.standingSprites.get(Player.this.dirFacing);
                }
                else {
                    Flying.this.pokemon.currOwSprite = Flying.this.pokemon.movingSprites.get(Player.this.dirFacing);
                }

                this.timer++;
                if (!this.takingOff && this.timer < 80 && this.timer >= 64) {
                    game.cam.translate(0f, -1f);
                }
                if (this.takingOff && this.timer < 17 && this.timer >= 0) {
                    game.cam.translate(0f, 1f);
                }
                if (!this.takingOff && this.timer >= 80) {
//                    Flying.this.takingOff = false; // this causes issue where player will move once at bottom.
                    game.actionStack.remove(this);
                    game.insertAction(this.nextAction);
                    Player.this.currFieldMove = "";
                    game.player.hmPokemon = null;  // TODO: test this
//                    game.cam.translate(0f, -16f);
                    game.actionStack.remove(Flying.this);
                    return;
                }
                if (this.takingOff && this.timer >= 96) {
                    Flying.this.takingOff = false;
                    game.actionStack.remove(this);
                    game.insertAction(this.nextAction);
                    return;
                }
            }
        }

    }

    public class RemoveFromInventory extends Action {
        String itemName;
        int amount;

        public RemoveFromInventory(String itemName, int amount, Action nextAction) {
            this.itemName = itemName;
            this.amount = amount;
            this.nextAction = nextAction;
        }

        @Override
        public void step(Game game) {
            Player.this.itemsDict.put(itemName, Player.this.itemsDict.get(itemName)-amount);
            if (game.player.itemsDict.get(itemName) <= 0) {
                game.player.itemsDict.remove(itemName);
            }
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
        }
    }

    public void setColor(Color newColor) {
        this.setColor(newColor, false);
    }

    public void setColor(Color newColor, boolean skinColor) {
        if (skinColor) {
            this.skinColor = newColor;
        }
        else {
            this.color = newColor;
        }

        // Colorize the sheet based on this.color
        Texture playerText = new Texture(Gdx.files.internal("player1_sheet1_color.png"));
        if (!playerText.getTextureData().isPrepared()) {
            playerText.getTextureData().prepare();
        }
        Pixmap pixmap = playerText.getTextureData().consumePixmap();
        Color clearColor = new Color(0, 0, 0, 0);
        Pixmap coloredPixmap = new Pixmap(playerText.getWidth(),
                                          playerText.getHeight(),
                                          Pixmap.Format.RGBA8888);
        coloredPixmap.setColor(clearColor);
        coloredPixmap.fill();
        Color replaceColor = new Color(0.9137255f, 0.5294118f, 0.1764706f, 1f);
        Color replaceWith = this.color;
        if (skinColor) {
            replaceColor = new Color(1f, 0.8078431372549019607843137254902f, 0.28235294117647058823529411764706f, 1f);
            replaceWith = this.skinColor;
        }
        for (int i = 0; i < playerText.getWidth(); i++) {
            for (int j = 0; j < playerText.getHeight(); j++) {
                Color color = new Color(pixmap.getPixel(i, j));
                if (color.equals(replaceColor)) {
                    color = replaceWith;
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
                if (color.equals(replaceColor)) {
                    color = replaceWith;
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

        // Recolor sleeping bag sprite
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
                if (color.equals(replaceColor)) {
                    color = replaceWith;
                }
                coloredPixmap.drawPixel(i, j, Color.rgba8888(color));
            }
        }
        playerText = new Texture(coloredPixmap);
        this.sleepingSprite = new Sprite(playerText, 0, 0, 24, 16);

        // Recolor battle sprite
        // TODO: this probably looks bad
        playerText = new Texture(Gdx.files.internal("battle/player_back_color1.png"));
        if (!playerText.getTextureData().isPrepared()) {
            playerText.getTextureData().prepare();
        }
        pixmap = playerText.getTextureData().consumePixmap();
        clearColor = new Color(0, 0, 0, 0);
        coloredPixmap = new Pixmap(playerText.getWidth(), playerText.getHeight(), Pixmap.Format.RGBA8888);
        coloredPixmap.setColor(clearColor);
        coloredPixmap.fill();

        replaceColor = new Color(0.6901961f, 0.28235295f, 0.15686275f, 1f);
        if (skinColor) {
            replaceColor = new Color(0.97254901960784313725490196078431f, 0.97254901960784313725490196078431f, 0.97254901960784313725490196078431f, 1f);
        }
        for (int i = 0; i < playerText.getWidth(); i++) {
            for (int j = 0; j < playerText.getHeight(); j++) {
                Color color = new Color(pixmap.getPixel(i, j));
                if (color.equals(replaceColor)) {
                    color = replaceWith;
                }
                coloredPixmap.drawPixel(i, j, Color.rgba8888(color));
            }
        }
        playerText = new Texture(coloredPixmap);
        this.battleSprite = new SpriteProxy(playerText, 0, 0, 45, 46);
    }
    
    /**
     * Set which Pokemon to send out first in battle.
     * 
     * Send out player.hmPokemon if it's eligible.
     * 
     * If player isn't using a field move, send out first
     * non-egg pokemon with hp > 0.
     */
    public void setCurrPokemon() {
        for (Pokemon pokemon : this.pokemon) {
            pokemon.participatedInBattle = false;
        }
        if (this.hmPokemon != null &&
            this.hmPokemon.currentStats.get("hp") > 0 &&
            !this.hmPokemon.isEgg) {
            this.currPokemon = this.hmPokemon;
            return;
        }
        for (Pokemon currPokemon : this.pokemon) {
            if (currPokemon.currentStats.get("hp") > 0 &&
                !currPokemon.isEgg) {
                this.currPokemon = currPokemon;
                return;
            }
        }
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
        this.timer++;

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
        Texture shadowText = TextureCache.get(Gdx.files.internal("shadow1.png"));
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

    @Override
    public void firstStep(Game game) {
        if (this.player.hmPokemon != null) {
            this.player.hmPokemon.moveDirs.add(this.player.dirFacing);
            this.player.hmPokemon.numMoves.add(1f);
        }
    }

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
            // If player is following a pokemon using an hm, then
            // the player needs to do a ledge jump after the pokemon
            if (this.player.hmPokemon != null) {
                this.player.hmPokemon.ledgeJumps.add(1);
            }
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



class PlayerKnockedBack extends Action {
    public int layer = 131;
    float xDist, yDist;
    Vector2 initialPos, targetPos;
    Sprite shadow;
    int timer1 = 0;
    ArrayList<Integer> yMovesList = new ArrayList<Integer>();
    ArrayList<Map<String, Sprite>> spriteAnim = new ArrayList<Map<String, Sprite>>();
    Player player;
    String dir;

    public PlayerKnockedBack(Game game, Player player, String dir) {
        this.player = player;
        this.dir = dir;
        this.initialPos = new Vector2(this.player.position);
        if (dir.equals("up")) {
            this.targetPos = new Vector2(this.player.position.x, this.player.position.y+32);
        }
        else if (dir.equals("down")) {
            this.targetPos = new Vector2(this.player.position.x, this.player.position.y-32);
        }
        else if (dir.equals("left")) {
            this.targetPos = new Vector2(this.player.position.x-32, this.player.position.y);
        }
        else if (dir.equals("right")) {
            this.targetPos = new Vector2(this.player.position.x+32, this.player.position.y);
        }
        // Shadow sprite
        Texture shadowText = TextureCache.get(Gdx.files.internal("shadow1.png"));
        this.shadow = new Sprite(shadowText, 0, 0, 16, 16);

        // Below two lists are used to get exact sprite and
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

    @Override
    public void firstStep(Game game) {
        if (this.player.hmPokemon != null) {
            this.player.hmPokemon.moveDirs.add(this.player.dirFacing);
            this.player.hmPokemon.numMoves.add(1f);
        }
    }

    @Override
    public void step(Game game) {

        if ( this.timer1 < 32) {
            if (dir.equals("up")) {
                game.player.position.y +=1;
                game.cam.position.y +=1;
            }
            else if (dir.equals("down")) {
                game.cam.position.y -=1;
                game.player.position.y -=1;
            }
            else if (dir.equals("left")) {
                game.player.position.x -=1;
                game.cam.position.x -=1;
            }
            else if (dir.equals("right")) {
                game.player.position.x +=1;
                game.cam.position.x +=1;
            }

            if (this.timer1 % 2 == 1) {
                game.player.position.y += this.yMovesList.get(0);
                this.yMovesList.remove(0);
                // Use next sprite in list
                game.player.currSprite = this.spriteAnim.get(0).get(game.player.dirFacing);
                this.spriteAnim.remove(0);
            }

            // this is needed for batch to draw according to cam
            // always call this after updating camera
            game.cam.update();
            game.mapBatch.setProjectionMatrix(game.cam.combined);
        }
        else {
            game.player.currSprite = game.player.standingSprites.get(game.player.dirFacing);
        }
        // Draw shadow
        game.mapBatch.draw(this.shadow, game.cam.position.x-16, game.cam.position.y-4);
        if (this.timer1 >= 38) {
            game.player.position.set(this.targetPos);
            game.cam.position.set(this.targetPos.x+16, this.targetPos.y,0);
            game.player.isNearCampfire = game.player.checkNearCampfire();
            game.actionStack.remove(this);
            // If player is following a pokemon using an hm, then
            // the player needs to do a ledge jump after the pokemon
            if (this.player.hmPokemon != null) {
                this.player.hmPokemon.ledgeJumps.add(1);
            }
        }
        this.timer1++;
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
        Texture shadowText = TextureCache.get(Gdx.files.internal("shadow1.png"));
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
    
    @Override
    public void firstStep(Game game) {
        if (this.player.hmPokemon != null) {
            this.player.hmPokemon.moveDirs.add(this.player.dirFacing);
            this.player.hmPokemon.numMoves.add(1f);
        }
    }

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
            if (this.player.hmPokemon != null) {
                this.player.hmPokemon.ledgeJumps.add(2);
            }
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
        if (this.player.hmPokemon != null) {
            this.player.hmPokemon.moveDirs.add(this.player.dirFacing);
            this.player.hmPokemon.numMoves.add(1f);
        }
        
        if (game.map.tiles.containsKey(this.targetPos) && game.map.tiles.get(this.targetPos).name.contains("desert2")) {
            DrawPlayerUpper.desertGrass = true;
            DrawPlayerUpper.timer = 0;
        }
        else if (game.map.tiles.containsKey(this.initialPos) && game.map.tiles.get(this.initialPos).name.contains("desert2")) {
            DrawPlayerUpper.desertGrass = true;
        }
        else {
            DrawPlayerUpper.desertGrass = false;
        }
    }

    // changed, was 130
    // alternative is to call cam.update(blah) each draw thingy, but
    // i think that's less optimal. this action needs to happen before everything else
    public int getLayer(){return this.layer;}

    public void localStep(Game game) {
        // allows game to pause in middle of run
        if (!game.playerCanMove) {
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
            if (this.alternate) {
                // game.batch.draw(game.player.altMovingSprites.get(game.player.dirFacing), game.player.position.x, game.player.position.y);
                game.player.currSprite = game.player.altMovingSprites.get(game.player.dirFacing);
                DrawPlayerUpper.offsetY = -1;
            }
            else {
                // game.batch.draw(game.player.movingSprites.get(game.player.dirFacing), game.player.position.x, game.player.position.y);
                game.player.currSprite = game.player.movingSprites.get(game.player.dirFacing);
                DrawPlayerUpper.offsetY = -1;
            }
        }
        else {
            // game.batch.draw(game.player.standingSprites.get(game.player.dirFacing), game.player.position.x, game.player.position.y);
            game.player.currSprite = game.player.standingSprites.get(game.player.dirFacing);
            DrawPlayerUpper.offsetY = 0;
        }

        // System.out.println("Debug: " + String.valueOf(game.player.position.y));

        // when we've moved 16 pixels
        // if button pressed, change dir and move that direction
        // else, stand still again
        if (this.xDist >= 16 || this.yDist >= 16) {
            // TODO: routes are persistent, ie never regenerate
            // TBD if that needs to change
            // TODO: that will be a problem if game has to load all pokemon sprites on the map
            Tile nextTile = game.map.tiles.get(this.targetPos);
            if (nextTile != null && nextTile.routeBelongsTo != null) {
                Route newRoute = nextTile.routeBelongsTo;
                String newBiome = nextTile.biome;

                // TODO: some flag to denote music transition
                if (!game.map.currRoute.type.equals(newRoute.type) && !game.map.timeOfDay.equals("night")) {
                    game.musicController.fadeToDungeon = true;
                }

                if (game.map.currRoute != newRoute) {
                    // TODO: try removing this stuff, don't think it does anything
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
            // Change player building tiles depending on where they are located
            // outdoors - outdoorBuildTiles, indoors - indoorBuildTiles, desert - desertBuildTiles
            if (nextTile != null) {
                // TODO: fix this to look at facingPos
                if (game.player.currFieldMove.equals("BUILD")) {
                    if (game.map.tiles == game.map.overworldTiles) {
                        if (nextTile.name.contains("desert")) {
                            game.player.buildTiles = game.player.desertBuildTiles;
                        }
                        else {
                            game.player.buildTiles = game.player.outdoorBuildTiles;
                        }
                    }
                    else {
                        game.player.buildTiles = game.player.indoorBuildTiles;
                    }
                }
                else if (game.player.currFieldMove.equals("DIG")) {
                    game.player.buildTiles = game.player.terrainTiles;
                }
                while (game.player.buildTileIndex > 0 && game.player.buildTileIndex >= game.player.buildTiles.size()) {
                    game.player.buildTileIndex--;
                }
                game.player.currBuildTile = game.player.buildTiles.get(game.player.buildTileIndex);
            }
            game.player.position.set(this.targetPos);
            game.player.isNearCampfire = game.player.checkNearCampfire();
            game.cam.position.set(this.targetPos.x+16, this.targetPos.y, 0);
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
    int timer = 0;
    Vector2 targetPos;
    float xDist, yDist;
    // float speed = 50.0f;

    boolean alternate = false;
    boolean firstStep = true;

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

        if (this.player.hmPokemon != null) {
            this.player.hmPokemon.moveDirs.add(this.player.dirFacing);
            this.player.hmPokemon.numMoves.add(1.6f);
        }
    }

    // changed, was 130
    // alternative is to call cam.update(blah) each draw thingy, but
    // i think that's less optimal. this action needs to happen before everything else
    public int getLayer(){return this.layer;}

    public void localStep(Game game) {
        // allows game to pause in middle of run
        if (!game.playerCanMove) {
            return;
        }
        // can consider doing skipping here if I need to slow down animation
        // bug - have to add 1 to cam position at beginning of each iteration.
         // probably related to occasionaly shakiness, which is probably related to floats

        // while you haven't moved 16 pixels,
         // move in facing direction

        float speed = 1.6f; // this needs to add up to 16 for smoothness?
        if (game.player.currFieldMove.equals("RIDE")) {
            speed = 2f;
//            speed = 1.6f;
        }
//        if (!game.player.isJumping) {
            this.timer = 20;
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
//        }
          // This looks slightly better but caused me motion sickness
//        else {
//            if (this.timer > 3 && this.timer < 12) {
//                if (game.player.dirFacing.equals("up")) {
//                    this.player.position.y +=2;
//                    game.cam.position.y +=2;
//                }
//                else if (game.player.dirFacing.equals("down")) {
//                    game.player.position.y -=2;
//                    game.cam.position.y -=2;
//                }
//                else if (game.player.dirFacing.equals("left")) {
//                    game.player.position.x -=2;
//                    game.cam.position.x -=2;
//                }
//                else if (game.player.dirFacing.equals("right")) {
//                    game.player.position.x +=2;
//                    game.cam.position.x +=2;
//                }
//            }
//            this.timer++;
//        }

        this.xDist = Math.abs(this.initialPos.x - game.player.position.x);
        this.yDist = Math.abs(this.initialPos.y - game.player.position.y);

        // this is needed for batch to draw according to cam
        game.cam.update();
        game.mapBatch.setProjectionMatrix(game.cam.combined);

        // if u remove the below check, youll notice that there's a bit of
         // movement that you don't want
        String spriteString = String.valueOf(game.player.dirFacing+"_running");
        // System.out.println("spriteString: " + String.valueOf(spriteString)); // debug
        if (!game.player.currFieldMove.equals("RIDE")) {
            if ((this.yDist < 13 && this.yDist > 2)
                || (this.xDist < 13 && this.xDist > 2)) {
                if (this.alternate) {
                    // game.batch.draw(game.player.altMovingSprites.get(game.player.dirFacing), game.player.position.x, game.player.position.y);
                    game.player.currSprite = game.player.altMovingSprites.get(spriteString);
                    if (game.player.currFieldMove.equals("RIDE")) {
                        DrawPlayerUpper.offsetY = -1;
                        DrawPlayerUpper.pokemonOffsetY = 2;
                    }
                }
                else {
                    // game.batch.draw(game.player.movingSprites.get(game.player.dirFacing), game.player.position.x, game.player.position.y);
                    game.player.currSprite = game.player.movingSprites.get(spriteString);
                    if (game.player.currFieldMove.equals("RIDE")) {
                        DrawPlayerUpper.offsetY = -1;
                        DrawPlayerUpper.pokemonOffsetY = 2;
                    }
                }
            }
            else {
                // game.batch.draw(game.player.standingSprites.get(game.player.dirFacing), game.player.position.x, game.player.position.y);
                game.player.currSprite = game.player.standingSprites.get(spriteString);
                DrawPlayerUpper.offsetY = 0;
                DrawPlayerUpper.pokemonOffsetY = 0;
            }
        }
        // Like the way this looks, keeping for now.
        else {
            if (!this.alternate) {
                if (this.firstStep) {
                    game.insertAction(new PlaySound("ride1", 1f, true, null));
                    this.firstStep = false;
                }
                game.player.currSprite = game.player.altMovingSprites.get(spriteString);
                DrawPlayerUpper.offsetY = -1;
                DrawPlayerUpper.pokemonOffsetY = 2;

                if ((this.yDist > 10)
                    || (this.xDist > 10)) {
                    DrawPlayerUpper.offsetY = 0;
                }
            }
            else {
                game.player.currSprite = game.player.standingSprites.get(spriteString);
                DrawPlayerUpper.offsetY = 0;
                DrawPlayerUpper.pokemonOffsetY = 0;
            }
        }

        // when we've moved 16 pixels
        // if button pressed, change dir and move that direction
        // else, stand still again
        if ((this.xDist >= 16 || this.yDist >= 16)
            && this.timer >= 14) {
            DrawPlayerUpper.offsetY = 0;
            DrawPlayerUpper.pokemonOffsetY = 0;
            if (game.map.tiles.get(this.targetPos) != null && game.map.tiles.get(this.targetPos).routeBelongsTo != null) {
                Route newRoute = game.map.tiles.get(this.targetPos).routeBelongsTo;
                String newBiome = game.map.tiles.get(this.targetPos).biome;

                // TODO: some flag to denote music transition
                if (!game.map.currRoute.type.equals(newRoute.type) && !game.map.timeOfDay.equals("night")) {
                    game.musicController.fadeToDungeon = true;
                }
                
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
    public static int bTimer = 0;
    public int cTimer = 0;
    public int vTimer = 0;

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
            // If currently on grass
            if (currTile.attrs.get("grass")) {
                // Burrowed trapinch tiles have no wild encounters
                if (currTile.items().containsKey("trapinch")) {
                    return null;
                }
                // Chance wild encounter
                int randomNum = game.map.rand.nextInt(100) + 1; // rate determine by player? // 1 - 100
                int rate = 10;
                if (game.map.tiles == game.map.interiorTiles.get(game.map.interiorTilesIndex)) {
                    rate = 4;
                }
                // Sand pits
                else if (currTile.name.contains("desert2")) {
                    rate = 7;
                }
                if (randomNum < rate) { //  < 20
                    // disable player movement
                    // game.actionStack.remove(this); // using flag now, delete this

                    

                    // get list of pokemon not in battle
                    ArrayList<Pokemon> notInBattle = new ArrayList<Pokemon>();
//                    for (Pokemon pokemon : game.map.currRoute.pokemon) {  // TODO: remove
                    for (Pokemon pokemon : currTile.routeBelongsTo.pokemon) {
                        if (!pokemon.inBattle) {
                            notInBattle.add(pokemon);
                        }
//                        System.out.println(pokemon.nickname);  // TODO: debug, remove
                    }
                    // If all pokemon are in battle, return null for now.
                    if (notInBattle.size() <= 0) {
                        return null;
                    }
                    // select new pokemon to encounter, put it in battle struct
                    int index = game.map.rand.nextInt(notInBattle.size());
                    Pokemon pokemon = notInBattle.get(index);
                    
                    // TODO: this breaks a lot of things, refactor in
                    //       the future. I don't think this is set back
                    //       to false in most cases.
                    // Also breaks if player is repelling the Pokemon
//                    pokemon.inBattle = true;

                    // Scale level if that's enabled
                    // Don't scale levels for dungeons
                    if (game.levelScalingEnabled && 
                        !currTile.routeBelongsTo.isDungeon) {
                        int averageLevel = 0;
                        int numberPokemon = 0;
                        for (Pokemon mon : game.player.pokemon) {
                            // Eggs don't count towards level scaling
                            if (mon.isEgg) {
                                continue;
                            }
                            averageLevel += mon.level;
                            numberPokemon++;
                        }
                        // This shouldn't happen, but just in case.
                        if (numberPokemon <= 0) {
                            System.out.println("WARNING: this should never happen. Might want to look into it.");
                            numberPokemon = 1;
                        }
                        averageLevel = averageLevel/numberPokemon;
                        averageLevel = averageLevel -3 + Game.rand.nextInt(3);
                        if (averageLevel > 50) {
                            averageLevel = 50;
                        }
                        // Don't allow anything below level 2
                        if (averageLevel < 2) {
                            averageLevel = 2;
                        }
                        // Reset all of the Pokemon's stat values (including health).
                        // TODO: not sure if I'm evolving mons here or not
                        //       would be cool in the beach biome.
                        pokemon.level = averageLevel;
                        pokemon.exp = pokemon.gen2CalcExpForLevel(pokemon.level);
                        pokemon.calcMaxStats();
                        pokemon.currentStats.clear();
                        pokemon.currentStats.putAll(pokemon.maxStats);
                        // Re-learn all attacks
                        pokemon.getCurrentAttacks();
                    }
                    // TODO: debug, remove
                    pokemon = new Pokemon("numel", 60, Pokemon.Generation.CRYSTAL);
                    // TODO: debug, remove
                    pokemon.attacks[0] = "feint attack";
                    pokemon.attacks[1] = "faint attack";
                    pokemon.attacks[2] = "vital throw";
                    pokemon.attacks[3] = "aerial ace";
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
        // TODO
        if (game.map.tiles != game.map.overworldTiles) {
            return;
        }
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
                    // TODO: these were screwing things up
//                    if (doorTile.nameUpper.contains("house1")) {
//                        game.map.tiles.put(doorTile.position, new Tile("house1_door1", doorTile.position.cpy()));
//                    }
                }
            }
            else if (doorTile != null){
                // TODO: these were screwing things up
                // replace door tile with dark door tile (?)
//                game.map.tiles.put(doorTile.position, new Tile("house1_door1_dark", doorTile.position.cpy()));
            }
        }
    }


    public void localStep(Game game) {
        if (!game.playerCanMove) {
            return;
        }

        boolean shouldMove = false;
        Vector2 newPos = new Vector2();

        if (this.checkWildEncounter) {
            Tile tile = game.map.tiles.get(game.player.position);
            // If player is standing on stairs, stop moving and change interior index.
            if (tile != null && (tile.nameUpper.contains("stairs") || tile.name.contains("stairs"))) {
                int downUp = 0;
                if (tile.nameUpper.contains("up") || tile.name.contains("up")) {
                    downUp = +1;
                }
                else {
                    downUp = -1;
                }
                game.playerCanMove = false;
                Map<Vector2, Tile> whichTiles = game.map.interiorTiles.get(game.map.interiorTilesIndex+downUp);
                if (tile.name.contains("exit") || tile.nameUpper.contains("exit")) {
                    whichTiles = game.map.overworldTiles;
                }
                Action action = new EnterBuilding(game, "enter", whichTiles, game.map.interiorTilesIndex+downUp,
                                new SetField(game.map, "interiorTilesIndex", game.map.interiorTilesIndex+downUp,
                                new SetField(game, "playerCanMove", true,
                                null)));
                // not working
//                if (game.player.hmPokemon != null) {
//                    action.append(new SetField(game.player.hmPokemon, "position", game.player.position.cpy(), null));
//                }
                game.insertAction(action);
            }
            // Check if any eggs hatch this step
            game.player.eggStepTimer++;
            // NOTE: vgc makes this check every 256 steps but that is way too high
            if (game.player.eggStepTimer >= 16) {  //either 16 or 32, not sure which  //16) {  // 256
                for (Pokemon pokemon : game.player.pokemon) {
                    if (pokemon.isEgg) {
                        pokemon.happiness -= 1;
                        System.out.println(pokemon.specie.name);
                        System.out.println(pokemon.happiness);
                        if (pokemon.happiness <= 0) {
                            game.playerCanMove = false;
                            // This plays the hatch animation
                            String[] huhs = new String[]{"Huh?", "Oh?"};
                            Action hatchAnimation = new WaitFrames(game, 61,
                                                    new WaitFrames(game, 3,
                                                    new DisplayText(game, huhs[Game.rand.nextInt(huhs.length)], null, true, false,
                                                    new WaitFrames(game, 51,
                                                    new DisplayText.Clear(game,
                                                    new EggHatchAnim(pokemon,
                                                    new SplitAction(
                                                        new SetField(game.musicController, "startEvolveMusic", true, null),
                                                    new Battle.LoadAndPlayAnimation(game, "egg_hatch", null,
                                                    new SplitAction(new WaitFrames(game, 4,
                                                                    new PlaySound(new Pokemon(pokemon.specie.name, 10),
                                                                    null)),
                                                    new PokemonIntroAnim(
                                                    new WaitFrames(game, 4,
                                                    new DisplayText(game, pokemon.specie.name.toUpperCase()+" came out of its EGG!",
                                                                    "fanfare2.ogg", false, true,
                                                    // Commented block is more accurate to the game, but feels weird
                                                    // b/c fadeout is so quick
                                                    // uses music fadeout speed -0.05
//                                                    new SplitAction(
//                                                        new EggHatchAnim.Done(13, null),
//                                                    new EnterBuilding(game, "",
//                                                    new SetField(game.musicController, "evolveMusicFadeout", true,
//                                                    new SetField(game, "playerCanMove", true,
                                                    new SplitAction(
                                                        new EggHatchAnim.Done(13,
                                                        new SetField(game.musicController, "evolveMusicFadeout", true,
                                                        null)),
                                                    new WaitFrames(game, 13,
                                                    new EnterBuilding(game, "", 8,
                                                    new SetField(game, "playerCanMove", true,
                                                    null))))))))))))))));
//                            // TODO: remove
                              // This was just the evolve anim that functioned like egg hatch as debug
//                            Action hatchAnimation = new WaitFrames(game, 61,
//                                                   new WaitFrames(game, 3,
//                                                   new DisplayText(game, "Oh?", null, true, false,
//                                                   new WaitFrames(game, 51,
//                                                   new EvolutionAnim(pokemon, pokemon.eggHatchInto,
//                                                   new PlaySound(pokemon,
//                                                   new SplitAction(
//                                                       new SetField(game.musicController, "startEvolveMusic", true, null),
////                                                       new EvolutionAnim.StartMusic(),  // TODO: remove
//                                                   new Battle.LoadAndPlayAnimation(game, "evolve", null,
//                                                   new WaitFrames(game, 30,  // about 30 frames after bubble anim until pokemon cry is heard
//                                                   new PlaySound(new Pokemon(pokemon.eggHatchInto, 10, Pokemon.Generation.CRYSTAL),
//                                                   new DisplayText.Clear(game,
//                                                   new WaitFrames(game, 3,
//                                                   new DisplayText(game, "Congratulations! Your "+pokemon.name.toUpperCase(),
//                                                                   null, true, true,
//                                                   new DisplayText.Clear(game,
//                                                   new WaitFrames(game, 3,
//                                                   new DisplayText(game, "hatched into "+pokemon.eggHatchInto.toUpperCase()+"!",
//                                                                   "fanfare2.ogg", true, false,
//                                                   new DisplayText.Clear(game,
//                                                   new WaitFrames(game, 2,
//                                                   new EvolutionAnim.Done(
//                                                   new SetField(game.musicController, "evolveMusicFadeout", true,
//                                                   new SetField(game, "playerCanMove", true,
//                                                   null)))))))))))))))))))));
                            game.insertAction(hatchAnimation);
                            this.checkWildEncounter = false;
                            return;
                        }
                    }
                }
                game.player.eggStepTimer = 0;
            }
        }
        // TODO: remove
        // This doesn't work atm because can't collide with pokemon
        // If player is using Attack, check if pokemon on this tile
//        if ((game.player.isAttacking || (game.player.isJumping && game.player.hmPokemon.types.contains("DARK")))
//            && this.checkWildEncounter
//            && game.map.pokemon.containsKey(game.player.position)) {
//            Pokemon pokemon = game.map.pokemon.get(game.player.position);
//            if (pokemon.mapTiles == game.map.tiles) {
//                // Enter battle with pokemon
////                pokemon.canMove = false;  // TODO: this won't get un-set after battle currently
//                game.playerCanMove = false;
//                game.musicController.startBattle = "wild";
//                game.battle.oppPokemon = pokemon;
//                game.player.setCurrPokemon();
//                game.insertAction(Battle.getIntroAction(game));
//                this.checkWildEncounter = false;
//                return;
//            }
//        }
        
        // Check wild encounter
        if (this.checkWildEncounter && game.type != Game.Type.CLIENT) {
            if (this.checkWildEncounter(game) == true) {
                // TODO: remove
//                for (Pokemon currPokemon : this.player.pokemon) {
//                    if (currPokemon.currentStats.get("hp") > 0 && !currPokemon.name.equals("egg")) {
//                        this.player.currPokemon = currPokemon;
//                        break;
//                    }
//                }
                // The first Pokemon the player sends out in battle should
                // have > 0 hp.
                this.player.setCurrPokemon();
                
                // TODO: should this just go in checkWildEncounter?
                boolean repelling = game.player.repelCounter > 0 && game.battle.oppPokemon.level < game.player.currPokemon.level;
                if (!repelling) {
                    game.playerCanMove = false;
                    
                    game.musicController.startBattle = "wild";
                    // TODO: remove
//                    if (game.map.unownSpawn == null) {
//                        // if night, no music transition
//                        if (!game.map.timeOfDay.equals("night")) {
//                            game.currMusic.pause();
//                            game.currMusic = game.battle.music; 
////                            game.battle.music2.stop();
////                            game.battle.music2.setVolume(0.3f);
//                            game.battle.music.stop();
//                            game.battle.music.setVolume(0.3f);
////                            game.battle.music.setVolume(0.3f);
//                            BattleFadeOutMusic.stop = true;
//                            FadeMusic.pause = true;
//                            // TODO: debug, remove
////                            game.currMusic.play();
////                            game.currMusic.pause();
////                            game.currMusic.setPosition(11f);  
//                            game.currMusic.play();
////                            game.insertAction(new FadeMusic("currMusic", "in", "", 1f, false, game.battle.music.getVolume(), null));
//                        }
//                    }
                    if (game.musicController.unownMusic) {
                        String unownLetter = game.map.unownUsed.get(game.map.rand.nextInt(game.map.unownUsed.size()));
//                        game.map.unownUsed.remove(unownLetter);
                        game.battle.oppPokemon = new Pokemon("unown_"+unownLetter, 13);
                    }
                    game.insertAction(Battle.getIntroAction(game));
                    this.checkWildEncounter = false;
                    return;
                }
            }
            if (game.player.repelCounter > 0) {
                game.player.repelCounter--;
                if (game.player.repelCounter == 0) {
                    game.playerCanMove = false;
                    game.insertAction(new DisplayText(game, "The effects of REPEL wore off.", null, null,
                                      new WaitFrames(game, 3,  // TODO: see if this fixes issue where grass text is triggered.
                                      new SetField(game, "playerCanMove", true, null))));
                    return;
                }
            }
            this.checkWildEncounter = false;
        }
        // else, check if the server sent an encounter
        else if (this.player.network.doEncounter != null) {
            // TODO: remove
//            for (Pokemon currPokemon : this.player.pokemon) {
//                if (currPokemon.currentStats.get("hp") > 0 && !currPokemon.name.equals("egg")) {
//                    this.player.currPokemon = currPokemon;
//                    break;
//                }
//            }
            // The first Pokemon the player sends out in battle should
            // have >0 hp.
            this.player.setCurrPokemon();
            game.playerCanMove = false;
            Network.PokemonData pokemonData = this.player.network.doEncounter.pokemonData;
            game.battle.oppPokemon = new Pokemon(pokemonData.name,
                                                 pokemonData.level);
            game.battle.oppPokemon.currentStats.put("hp", pokemonData.hp);
            game.musicController.startBattle = "wild";
            game.insertAction(Battle.getIntroAction(game));
//            if (!game.map.timeOfDay.equals("night")) {
//                game.currMusic.pause();
//                game.currMusic = game.battle.music;
//                game.battle.music.stop();
//                game.battle.music.setVolume(0.3f);
//                game.currMusic.play();
//                BattleFadeOutMusic.stop = true;
////                FadeMusic.pause = true;  // TODO: remove
//            }
            this.checkWildEncounter = false;
            this.player.network.doEncounter = null;
            return;
        }
        // If player is standing on a door, stop moving and move player to interior.
        if (game.map.tiles.get(game.player.position) != null &&
            (game.map.tiles.get(game.player.position).nameUpper.contains("door") ||
             game.map.tiles.get(game.player.position).name.contains("door"))) {
            game.playerCanMove = false;
            Action action;
            if (game.map.tiles == game.map.overworldTiles) {
                action = new EnterBuilding(game, 
                          new SetField(game, "playerCanMove", true,
                          null));
            }
            else {
                action = new EnterBuilding(game, "exit",
                          new SetField(game, "playerCanMove", true,
                          null));
            }
            // not working
//            if (game.player.hmPokemon != null) {
//                action.append(new SetField(game.player.hmPokemon, "position", game.player.position.cpy(), null));
//            }
            game.insertAction(action);
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
                if (game.player.sleepingDir == null) {
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
                }
                else {
                    game.player.sleepingDir = null;
                }

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

        // Hold c/v for 1 sec to scroll rapidly
        if (game.player.currFieldMove.equals("BUILD") ||
            game.player.currFieldMove.equals("DIG")) {
            if (Gdx.input.isKeyPressed(Input.Keys.C)) {
                if (this.cTimer < 30) {
                    this.cTimer++;
                }
            }
            else {
                this.cTimer = 0;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.V)) {
                if (this.vTimer < 30) {
                    this.vTimer++;
                }
            }
            else {
                this.vTimer = 0;
            }
        }

        // Check if input pressed
        if (Gdx.input.isKeyJustPressed(Input.Keys.C) || this.cTimer >= 30) {
            if (game.player.currFieldMove.equals("BUILD")) {
                game.player.buildTileIndex -= 1;
                if (game.player.buildTileIndex < 0) {
                    game.player.buildTileIndex = game.player.buildTiles.size() - 1;
                }
                game.player.currBuildTile = game.player.buildTiles.get(game.player.buildTileIndex);
            }
            else if (game.player.currFieldMove.equals("DIG") && !game.player.currBuildTile.name.contains("hole")) {
                // Don't select terrains that player doesn't have requirements for
                do {
                    game.player.buildTileIndex -= 1;
                    if (game.player.buildTileIndex < 0) {
                        game.player.buildTileIndex = game.player.terrainTiles.size() - 1;
                    }
                    game.player.currBuildTile = game.player.terrainTiles.get(game.player.buildTileIndex);
                    // TODO: this should be a generic function
                    boolean requirementsMet = true;
                    System.out.println(game.player.currBuildTile.name);
                    System.out.println(game.player.buildTileRequirements.get(game.player.currBuildTile.name).keySet());
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
                    if (requirementsMet) {
                        break;
                    }
                }
                while (game.player.buildTileIndex != 0);
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.V) || this.vTimer >= 30) {
            if (game.player.currFieldMove.equals("BUILD")) {
                game.player.buildTileIndex += 1;
                if (game.player.buildTileIndex >= game.player.buildTiles.size()) {
                    game.player.buildTileIndex = 0;
                }
                game.player.currBuildTile = game.player.buildTiles.get(game.player.buildTileIndex);
            }
            else if (game.player.currFieldMove.equals("DIG") && !game.player.currBuildTile.name.contains("hole")) {
                // Don't select terrains that player doesn't have requirements for
                do {
                    game.player.buildTileIndex += 1;
                    if (game.player.buildTileIndex >= game.player.terrainTiles.size()) {
                        game.player.buildTileIndex = 0;
                    }
                    game.player.currBuildTile = game.player.terrainTiles.get(game.player.buildTileIndex);
                    // TODO: this should be a generic function
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
                    if (requirementsMet) {
                        break;
                    }
                }
                while (game.player.buildTileIndex != 0);
            }
        }

        if (InputProcessor.bJustPressed) {
            PlayerStanding.bTimer = 0;
        }
        // If player moves, they need to re-press B for it to register
        if (shouldMove) {
            PlayerStanding.bTimer = 20;
        }
        if (InputProcessor.bPressed) {
            if (PlayerStanding.bTimer == 19) {
                // TODO: this is broken, no idea what's going on.
                if (game.type == Game.Type.CLIENT && !game.player.currFieldMove.equals("")) {
                    game.client.sendTCP(new com.pkmngen.game.Network.UseHM(game.player.network.id,
                                                                           DrawPokemonMenu.currIndex,
                                                                           "STOP"));
                }
                // TODO: this is incomplete
                if (game.player.currPlanting != null) {
                    game.player.currPlanting = null;
                    PlayerStanding.bTimer = 0;
                    return;
                }
                // Player wants to stop using field move
                if (game.player.hmPokemon != null) {
                    if (game.player.currFieldMove.equals("RIDE")) {
                        game.player.swapSprites(game.player.hmPokemon);
                        game.player.hmPokemon = null;
                        game.player.currFieldMove = "";
                    }
                    else {
                        game.playerCanMove = false;
                        game.actionStack.remove(game.player.hmPokemon.standingAction);
                        if (game.player.hmPokemon.ledgeJumps.size() > 0) {
                            game.player.hmPokemon.standingAction = game.player.hmPokemon.new LedgeJump(game.player.hmPokemon.dirFacing, game.player.hmPokemon.ledgeJumps.remove(0),
                                                                   null);
                        }
                        else {
                            game.player.hmPokemon.standingAction = game.player.hmPokemon.new Moving(game.player.hmPokemon.dirFacing, 1, 1f, true, true,
                                                                   null);
                        }
                        // If not doing any of these, assume Pokemon is using FOLLOW
                        // in which case you shouldn't swap sprites
                        if (!game.player.currFieldMove.equals("")) {
                            game.player.hmPokemon.standingAction.append(new CallMethod(game.player, "swapSprites", new Object[]{game.player.hmPokemon}, null));
                        }
                        game.player.hmPokemon.standingAction.append(new SetField(game.player, "hmPokemon", null,
                                                                    new SetField(game, "playerCanMove", true,
                                                                    null)));
                        game.player.currFieldMove = "";
                        game.insertAction(game.player.hmPokemon.standingAction);
                        return;
                    }
                }
                // TODO: remove
                // TODO: test
//                if (game.player.isBuilding) {
//                    // Player wants to stop building
//                    game.player.isBuilding = false;
//                    game.actionStack.remove(game.player.hmPokemon.standingAction);
//                    if (game.player.hmPokemon.ledgeJumps.size() > 0) {
//                        game.player.hmPokemon.standingAction = game.player.hmPokemon.new LedgeJump(game.player.hmPokemon.dirFacing, game.player.hmPokemon.ledgeJumps.remove(0),
//                                                               new CallMethod(game.player, "swapSprites", new Object[]{game.player.hmPokemon}, 
//                                                               new SetField(game, "playerCanMove", true,
//                                                               null)));
//                    }
//                    else {
//                        game.player.hmPokemon.standingAction = game.player.hmPokemon.new Moving(game.player.hmPokemon.dirFacing, 1, 1f, true, true,
//                                                                new CallMethod(game.player, "swapSprites", new Object[]{game.player.hmPokemon}, 
//                                                                new SetField(game, "playerCanMove", true,
//                                                                null)));
//                    }
//                    game.insertAction(game.player.hmPokemon.standingAction);
//                    
//                }
//                if (game.player.isCutting) {
//                    // player wants to stop building
//                    game.player.isCutting = false;
//                    game.playerCanMove = false;
//                    game.actionStack.remove(game.player.hmPokemon.standingAction);
////                    game.player.hmPokemon.moveDir = game.player.dirFacing;  // move again in this direction
////                  game.player.swapSprites(game.player.hmPokemon);
//                    if (game.player.hmPokemon.ledgeJumps.size() > 0) {
//                        game.player.hmPokemon.standingAction = game.player.hmPokemon.new LedgeJump(game.player.hmPokemon.dirFacing, game.player.hmPokemon.ledgeJumps.remove(0),
//                                                               new CallMethod(game.player, "swapSprites", new Object[]{game.player.hmPokemon}, 
//                                                               new SetField(game, "playerCanMove", true,
//                                                               null)));
//                    }
//                    else {
//                        game.player.hmPokemon.standingAction = game.player.hmPokemon.new Moving(game.player.hmPokemon.dirFacing, 1, 1f, true, true,
//                                                                new CallMethod(game.player, "swapSprites", new Object[]{game.player.hmPokemon}, 
//                                                                new SetField(game, "playerCanMove", true,
//                                                                null)));
//                    }
//                    game.insertAction(game.player.hmPokemon.standingAction);
//                    return;  // why return here?
//                }
//                if (game.player.isHeadbutting) {
//                    // player wants to stop building
//                    game.player.isHeadbutting = false;
////                    game.player.swapSprites(game.player.hmPokemon);
//                    game.actionStack.remove(game.player.hmPokemon.standingAction);
//                    if (game.player.hmPokemon.ledgeJumps.size() > 0) {
//                        game.player.hmPokemon.standingAction = game.player.hmPokemon.new LedgeJump(game.player.hmPokemon.dirFacing, game.player.hmPokemon.ledgeJumps.remove(0),
//                                                               new CallMethod(game.player, "swapSprites", new Object[]{game.player.hmPokemon}, 
//                                                               new SetField(game, "playerCanMove", true,
//                                                               null)));
//                    }
//                    else {
//                        game.player.hmPokemon.standingAction = game.player.hmPokemon.new Moving(game.player.hmPokemon.dirFacing, 1, 1f, true, true,
//                                                                new CallMethod(game.player, "swapSprites", new Object[]{game.player.hmPokemon}, 
//                                                                new SetField(game, "playerCanMove", true,
//                                                                null)));
//                    }
//                    game.insertAction(game.player.hmPokemon.standingAction);
//                }
//                if (game.player.isJumping) {
//                    // player wants to stop jumping
//                    game.player.isJumping = false;
//                    game.player.swapSprites(game.player.hmPokemon);
//                }
//                if (game.player.isSmashing) {
//                    game.player.isSmashing = false;
////                    game.player.swapSprites(game.player.hmPokemon);
//                    game.actionStack.remove(game.player.hmPokemon.standingAction);
//                    if (game.player.hmPokemon.ledgeJumps.size() > 0) {
//                        game.player.hmPokemon.standingAction = game.player.hmPokemon.new LedgeJump(game.player.hmPokemon.dirFacing, game.player.hmPokemon.ledgeJumps.remove(0),
//                                                               new CallMethod(game.player, "swapSprites", new Object[]{game.player.hmPokemon}, 
//                                                               new SetField(game, "playerCanMove", true,
//                                                               null)));
//                    }
//                    else {
//                        game.player.hmPokemon.standingAction = game.player.hmPokemon.new Moving(game.player.hmPokemon.dirFacing, 1, 1f, true, true,
//                                                                new CallMethod(game.player, "swapSprites", new Object[]{game.player.hmPokemon}, 
//                                                                new SetField(game, "playerCanMove", true,
//                                                                null)));
//                    }
//                    game.insertAction(game.player.hmPokemon.standingAction);
//                }
            }
            else {
                PlayerStanding.bTimer++;
            }
        }

        if (InputProcessor.aJustPressed) {
            // TODO: remove once tested
//            Vector2 pos = new Vector2(0,0);
//            if (game.player.dirFacing.equals("right")) {
//                pos = new Vector2(game.player.position.cpy().add(16,0));
//            }
//            else if (game.player.dirFacing.equals("left")) {
//                pos = new Vector2(game.player.position.cpy().add(-16,0));
//            }
//            else if (game.player.dirFacing.equals("up")) {
//                pos = new Vector2(game.player.position.cpy().add(0,16));
//            }
//            else if (game.player.dirFacing.equals("down")) {
//                pos = new Vector2(game.player.position.cpy().add(0,-16));
//            }
            Vector2 pos = game.player.facingPos();
            Tile currTile = game.map.tiles.get(pos);
            if (currTile == null) {

            }
            else if (currTile.nameUpper.equals("sleeping_bag1")) {
                game.player.isSleeping = true;
                Texture playerText = new Texture(Gdx.files.internal("tiles/sleeping_bag1_using.png"));
                Sprite temp = new Sprite(playerText, 0, 0, 24, 16);
                temp.setPosition(currTile.overSprite.getX(), currTile.overSprite.getY());
                currTile.overSprite = temp;
                game.player.position.set(currTile.overSprite.getX(), currTile.overSprite.getY());
            }
            else if (game.player.currPlanting != null) {
                if (game.player.currPlanting.equals("manure")) {
                    if (!currTile.nameUpper.contains("fertilized") &&
                        currTile.nameUpper.contains("tree_planted")) {
                        currTile.nameUpper = currTile.nameUpper+"_fertilized";
                        // Change currTile lower appearance
                        game.map.tiles.put(currTile.position, new Tile("mountain1", currTile.nameUpper, currTile.position.cpy(), true, currTile.routeBelongsTo));
                        game.insertAction(new PlaySound("seed1", null));
                    }
                }
                else if (game.player.currPlanting.equals("miracle seed")) {
                    if (!currTile.name.equals("grass_planted") &&
                        (currTile.name.equals("green1") || currTile.name.contains("flower"))) {
                        currTile.nameUpper = "grass_planted";
                        // Change currTile appearance
                        game.map.tiles.put(currTile.position, new Tile("grass_planted", currTile.position.cpy(), true, currTile.routeBelongsTo));
                        game.insertAction(new PlaySound("seed1", null));
                    }
                }
                else if (game.player.currPlanting.contains("apricorn")) {
                    if (!(currTile.name.equals("green1") || 
                            currTile.name.contains("flower") ||
                            currTile.name.contains("desert")) ||  // TODO: unsure
                        currTile.nameUpper.contains("tree") ||
                        currTile.attrs.get("solid")) {
                        // TODO: used to display hint text
//                        game.insertAction(new DisplayText(game, "Seeds must be planted in good soil!", null, false, true,
//                                          new WaitFrames(game, 10,
//                                          new SetField(game, "playerCanMove", true,
//                                          null))));
                        return;
                    }
                    game.playerCanMove = false;
                    game.insertAction(new WaitFrames(game, 10,
                                      new SplitAction(new PlantTree(pos,
                                                      null),
                                      new PlaySound("seed1",
                                      new SetField(game, "playerCanMove", true,
                                      new WaitFrames(game, 4,
                                      new PlaySound("ledge2",
//                                      new WaitFrames(game, 10,
//                                      new SetField(game, "playerCanMove", true,
                                      null)))))));
                }
                // Deduct from inventory
                game.player.itemsDict.put(game.player.currPlanting, game.player.itemsDict.get(game.player.currPlanting)-1);
                if (game.player.itemsDict.get(game.player.currPlanting) <= 0) {
                    game.player.itemsDict.remove(game.player.currPlanting);
                    game.player.currPlanting = null;  // Player isn't planting this anymore
                }
                return;
            }
            else if (game.player.currFieldMove.equals("BUILD")) {
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
                // Prevent player from cheesing into the pokemon mansion,
                // make sure the interior tile above is also either nothing or a house.
                if (game.player.currBuildTile.name.contains("house")) {
                    Tile upTile = game.map.interiorTiles.get(game.map.interiorTilesIndex).get(currTile.position.cpy().add(0, 16));
                    requirementsMet = requirementsMet && (upTile == null || upTile.name.contains("house"));
                }
                boolean isTorch = game.player.currBuildTile.name.contains("torch") ||
                                  game.player.currBuildTile.name.equals("house_clock1") ||
                                  game.player.currBuildTile.name.contains("picture");
                if (isTorch) {
                    requirementsMet = requirementsMet &&
                                      !currTile.items().containsKey("torch") &&
                                      currTile.attrs.get("solid") &&
                                      !currTile.nameUpper.contains("roof");
                    String[] notAllowedTiles = new String[]{"regi", "tree", "bush", "table",
                                                            "bed", "shelf", "plant"};
                    for (String name : notAllowedTiles) {
                        requirementsMet = requirementsMet && !(currTile.name.contains(name) || currTile.nameUpper.contains(name));
                    }
                }
                boolean isCarpet = game.player.currBuildTile.name.contains("carpet");
                if (isCarpet) {
                    // Don't allow building over the house rug (which lets you out of the house)
                    requirementsMet = requirementsMet && !(currTile.name.contains("rug"));
                }
                boolean isBridge = game.player.currBuildTile.name.contains("bridge");
                if (isBridge) {
                    // Can only be built on/over water
                    requirementsMet = requirementsMet && currTile.name.contains("water") && (!currTile.name.contains("bridge") || currTile.name.contains("_lower"));
                }

                if ((isTorch || isCarpet || isBridge || !currTile.attrs.get("solid")) && !currTile.nameUpper.contains("door") && requirementsMet) {
                    // TODO: remove commented lines
//                    currTile.overSprite = new Sprite(game.player.currBuildTile.sprite);
//                    currTile.name = game.player.currBuildTile.name;
//                    currTile.overSprite.setPosition(pos.x, pos.y);
//                    currTile.attrs.put("cuttable", true); // test this
//                    currTile.attrs.put("solid", true);

                    Tile newTile = currTile;
                    if (game.player.currBuildTile.name.contains("torch")) {
                        // Just add 'torch' to items
                        currTile.items().put("torch", 1);
                        
//                        // TODO: Remove?
//                        // Add 1 grass 1 log so player gets that back later.
//                        int numGrass = 1;
//                        if (currTile.items().containsKey("grass")) {
//                            numGrass += currTile.items().get("grass");
//                        }
//                        currTile.items().put("grass", numGrass);
//                        int numLogs = 1;
//                        if (currTile.items().containsKey("log")) {
//                            numLogs += currTile.items().get("log");
//                        }
//                        currTile.items().put("log", numLogs);
                    }
                    else {
                        // Carpet replaces the lower tile
                        if (isCarpet) {
                            newTile = new Tile(game.player.currBuildTile.name, currTile.nameUpper,
                                               currTile.position.cpy(), true, currTile.routeBelongsTo);
                        }
                        else if (isBridge) {
                            // Store water type on the end of name.
                            String waterType = currTile.name;
                            String[] tokens = currTile.name.split("_");
                            if (tokens.length > 1) {
                                waterType = tokens[1];
                            }
                            String name = game.player.currBuildTile.name + "_" + waterType;
                            newTile = new Tile(name, currTile.nameUpper,
                                               currTile.position.cpy(), true, currTile.routeBelongsTo);
                            // If tile below is water, then make it a 'below bridge' water tile
                            // used for drawing bridge supports
                            Tile down = game.map.tiles.get(currTile.position.cpy().add(0, -16));
                            if (down != null && down.name.contains("water") && !down.name.contains("bridge")) {
                                down.name = name + "_lower";
                                down.init(down.name, down.nameUpper, down.position, true, down.routeBelongsTo);
                            }
                        }
                        else {
                            newTile = new Tile(currTile.name, game.player.currBuildTile.name,
                                               currTile.position.cpy(), true, currTile.routeBelongsTo);
                        }
                        // Transfer items over
                        newTile.items = currTile.items;
                        // Tile may change orientation depending on surrounding tiles
                        // ie, fence will rotate, house piece might be corner, etc
                        if (game.type != Game.Type.CLIENT) {
                            game.map.tiles.remove(currTile.position.cpy());
                            game.map.tiles.put(currTile.position.cpy(), newTile);
                            game.map.adjustSurroundingTiles(newTile);
                            if (game.player.currBuildTile.name.contains("bed")) {
//                                game.map.tiles.remove(currTile.position.cpy().add(0, 16));
//                                Tile upTile = new Tile("black1", currTile.position.cpy().add(0, 16));
//                                game.map.tiles.put(currTile.position.cpy().add(0, 16), upTile);
                                Tile upTile = game.map.tiles.get(currTile.position.cpy().add(0, 16));
                                upTile.nameUpper = "solid";
                                upTile.attrs.put("solid", true);
                            }
                        }
                        else {
                            // Send request to build new tile to server
                            // Don't update locally yet, server will send back TileData if it succeeds.
                            game.client.sendTCP(new Network.TileData(newTile));
                        }
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
                        // TODO: this doesn't even work atm, I don't think
                        this.detectIsHouseBuilt(game, currTile);
                    }
                    // Deduct required materials
                    // Also put all materials used for building into the tile
                    // that you built.
                    for (String name : game.player.buildTileRequirements.get(game.player.currBuildTile.name).keySet()) {
                        int value = game.player.buildTileRequirements.get(game.player.currBuildTile.name).get(name);
                        int newValue = game.player.itemsDict.get(name)-value;
                        game.player.itemsDict.put(name, newValue);
                        if (newValue <= 0) {
                            game.player.itemsDict.remove(name);
                        }
                        // Put materials into the tile that was built.
                        if (newTile.items().containsKey(name)) {
                            value += newTile.items().get(name);
                        }
                        newTile.items().put(name, value);
                    }
                }
            }
            else if (game.player.currFieldMove.equals("DIG")) {
//                if (currTile.nameUpper.contains("hole")) {
                if (currTile.hasItem != null) {
                    currTile.onPressA(game);
                    return;
                }
                else if (!game.player.currBuildTile.name.contains("hole")) {
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
                    if (requirementsMet) {
                        currTile.name = game.player.currBuildTile.name;
                        currTile.nameUpper = "";  // Not a hole anymore
                        currTile.overSprite = null; // TODO: do this in init()
                        currTile.init(currTile.name, currTile.nameUpper, currTile.position, true, currTile.routeBelongsTo);
                        // TODO: remove this if you don't want holes to 'snap' to correct shape
                        //       by default.
                        game.map.adjustSurroundingTiles(currTile);
                    }
                    // Deduct required materials from player inventory
                    for (String name : game.player.buildTileRequirements.get(game.player.currBuildTile.name).keySet()) {
                        int value = game.player.buildTileRequirements.get(game.player.currBuildTile.name).get(name);
                        int newValue = game.player.itemsDict.get(name)-value;
                        game.player.itemsDict.put(name, newValue);
                        if (newValue <= 0) {
                            game.player.itemsDict.remove(name);
                        }
                    }
                    game.playerCanMove = false;
                    game.player.currFieldMove = "";  // Stop drawing hole
                    // 
                    game.player.currSprite = game.player.altMovingSprites.get(game.player.dirFacing);
                    game.insertAction(new WaitFrames(game, 16,
                                      new SetField(game.player, "currFieldMove", "DIG",
                                      new SetField(game, "playerCanMove", true,
                                      null))));
                    game.insertAction(new WaitFrames(game, 10,
                                      new SetField(game.player, "currSprite", game.player.standingSprites.get(game.player.dirFacing),
                                      null)));
//                    game.insertAction(new PlaySound("seed1", null));
//                    game.insertAction(new PlaySound("sounds/dig1", 1f, true, null));
                    game.insertAction(new PlaySound("sounds/ap1", 1f, true, null));
                    return;
                }
                else {
                    boolean requirementsMet = currTile.nameUpper.equals("") || currTile.nameUpper.equals("desert4_cracked");
                    requirementsMet = requirementsMet && !currTile.attrs.get("solid") && !currTile.name.contains("door");
                    if (requirementsMet) {
                        Tile newTile = new Tile(currTile.name, game.player.currDigTile.name,
                                                currTile.position.cpy(), true, currTile.routeBelongsTo);
                        if (currTile.nameUpper.equals("desert4_cracked")) {
                            // Different types of fossils, probably randomly pick here
                            // TODO: technically player can save scum for fossils here. Not a huge deal tho.
                            String[] fossilTypes = {"old amber", "dome fossil", "helix fossil",
                                                    "claw fossil", "root fossil", "shield fossil", "skull fossil"};
                            newTile.hasItem = fossilTypes[Game.rand.nextInt(fossilTypes.length)];
                            newTile.hasItemAmount = 1;
                        }
                        newTile.items = currTile.items;
                        game.map.tiles.remove(currTile.position.cpy());
                        game.map.tiles.put(currTile.position.cpy(), newTile);
                        game.map.adjustSurroundingTiles(newTile);

                        game.insertAction(new PlaySound("sounds/dig1", 1f, true, null));
                        game.insertAction(new OverworldAnimation(game, "dig_overworld_gsc", currTile.position.cpy().add(-40 -64, -48 -4), true, null));
//                        game.insertAction(new OverworldAnimation(game, "dig_overworld_gsc", currTile.position.cpy().add(-40 -64, -44 -6), true, null));
                        game.insertAction(new OverworldAnimation(game, "dig_overworld_gsc", currTile.position.cpy().add(-40, -48 -4), false, null));
//                        game.insertAction(new OverworldAnimation(game, "dig_overworld_gsc", currTile.position.cpy().add(-40, -44 -6), false, null));
                        game.playerCanMove = false;
                        game.player.currFieldMove = "";  // stop drawing hole
                        // 
                        game.player.currSprite = game.player.altMovingSprites.get(game.player.dirFacing);
                        game.insertAction(new WaitFrames(game, 8,
//                                          new PutTile(game, newTile,
                                          new WaitFrames(game, 8,
                                          new SetField(game.player, "currFieldMove", "DIG",
                                          new SetField(game, "playerCanMove", true,                                
//                                          new CallMethod(PlayerStanding, "adjustSurroundingTiles", new Object[]{newTile},
                                          null)))));
                        game.insertAction(new WaitFrames(game, 10,
                                          new SetField(game.player, "currSprite", game.player.standingSprites.get(game.player.dirFacing),
                                          null)));
                        String gotTerrain = "grass";
                        if (currTile.name.contains("sand")) {
                            gotTerrain = "soft sand";
                        }
                        else if (currTile.name.contains("snow")) {
                            gotTerrain = "nevermeltice";
                        }
                        else if (currTile.name.contains("mountain")) {
                            gotTerrain = "light clay";
                        }
                        else if (currTile.name.contains("desert")) {
                            gotTerrain = "dry sand";
                        }
                        else if (currTile.name.contains("flower")) {
                            gotTerrain = "flowers";
                        }
                        // TODO: not sure if will ever get more than one terrain.
                        HashMap<String, Integer> items = new HashMap<String, Integer>();
                        items.put(gotTerrain, 1);
                        game.insertAction(new DrawItemPickup(items, null));
                        if (game.player.itemsDict.containsKey(gotTerrain)) {
                            int currQuantity = game.player.itemsDict.get(gotTerrain);
                            game.player.itemsDict.put(gotTerrain, currQuantity+1);
                        }
                        else {
                            game.player.itemsDict.put(gotTerrain, 1);
                        }
                        // TODO: deduct terrain materials
                        // TODO: start terrain at either previously dug thing, or
                        //       the name of the currTile.name
                        return;
                    }
                    // TODO: potentially remove
                    else {
                        // Interact with tile (?)
                        currTile.onPressA(game);
                        return;
                    }
                }
            }
            else if (game.player.currFieldMove.equals("HEADBUTT")) {
                if (currTile.attrs.containsKey("headbuttable") && currTile.attrs.get("headbuttable")) {
                    // Choose if found anything
                    Action nextAction = new PlayerCanMove(game, null);  // TODO: use SetField
//                    int randInt = game.map.rand.nextInt(4);
                    if (currTile.routeBelongsTo != null && !currTile.routeBelongsTo.pokemon.isEmpty()) {
                        // TODO: need to be static per-tree somehow.
                        game.playerCanMove = false;
                        game.battle.oppPokemon = currTile.routeBelongsTo.pokemon.get(0);
                        game.player.setCurrPokemon();
                        // Required by EnemyFaint and others, so they can remove the caught/fainted pokemon.
                        // Route name == "" in this case, so no new Pokemon are added when route.genPokemon() is called.
                        game.map.currRoute = currTile.routeBelongsTo;
                        nextAction = new WaitFrames(game, 16,
                                     new SetField(game.musicController, "startBattle", "wild",
                                     Battle.getIntroAction(game)));
                        this.checkWildEncounter = false;
                        shouldMove = false;  // for safety
                    }
                    game.insertAction(new HeadbuttTreeAnim(game, game.map.tiles.get(pos),
                                      nextAction));
                    game.playerCanMove = false;
                }
            }
            else if (game.player.currFieldMove.equals("CUT")) {
                if (currTile.attrs.containsKey("cuttable") && currTile.attrs.get("cuttable")) {
                    Action action = new CutTreeAnim(game, game.map.tiles.get(pos), null);
//                    Tile interiorTile = new Tile("black1", currTile.position.cpy());
//                    game.map.interiorTiles.get(game.map.interiorTilesIndex).put(currTile.position.cpy(), interiorTile);
                    Tile upTile = game.map.tiles.get(pos.cpy().add(0, 16));
                    if (upTile != null && upTile.name.contains("rug") && currTile.nameUpper.contains("roof")) {
                        game.map.tiles.put(upTile.position.cpy(), new Tile("green1", upTile.position.cpy(),
                                                                           true, upTile.routeBelongsTo));
                    }
                    // If cutting bed, remove the solid tile above
                    if (upTile != null && currTile.nameUpper.contains("bed")) {
                        upTile.nameUpper = "";
                        upTile.attrs.put("solid", false);
                    }
                    
                    if (game.map.tiles == game.map.overworldTiles) {
                        HashMap<Vector2, Tile> interiorTiles = game.map.interiorTiles.get(game.map.interiorTilesIndex);
                        Tile interiorTile = interiorTiles.remove(currTile.position);
                        if (interiorTile != null) {
                            upTile = interiorTiles.get(interiorTile.position.cpy().add(0, 16));
                            if (upTile != null && (upTile.name.contains("wall") || upTile.name.contains("door"))) {
                                interiorTiles.remove(upTile.position);
                            }
                            // Gain all of the items contained in the tile.
                            // Just move them all to the overworld tile
                            for (String name : interiorTile.items().keySet()) {
                                int value = interiorTile.items().get(name);
                                if (currTile.items().containsKey(name)) {
                                    value += currTile.items().get(name);
                                }
                                currTile.items().put(name, value);
                            }
                        }
                    }
                    game.playerCanMove = false;
                    // Get items from tile
                    if (!currTile.items().isEmpty()) {
                        // Torches are just there to denote to draw a torch
                        // on top of the tile.
                        currTile.items().remove("torch");
                        action.append(new SplitAction(
                                          new DrawItemPickup(currTile.items(), null),
                                      null));
                        for (String item : currTile.items().keySet()) {

                            // TODO: debug, remove
                            System.out.println(item);
                            
                            // TODO: this was a text box popup for each item recieved
                            // Still functional but deprecated for now.
////                            System.out.println(item);
////                            game.insertAction(new ItemPickupNotify(game, item, currTile.items().get(item)));
//                            String plural = "";
//                            if (currTile.items().get(item) > 1) {
//                                plural = "s";
//                            }
//                            action.append(new DisplayText(game, "Picked up "+currTile.items().get(item)+" "+item.toUpperCase()+plural+".", null, null,
//                                          null));
                            if (game.player.itemsDict.containsKey(item)) {
                                int currQuantity = game.player.itemsDict.get(item);
                                game.player.itemsDict.put(item, currQuantity+currTile.items().get(item));
                            }
                            else {
                                game.player.itemsDict.put(item, currTile.items().get(item));
                            }
                        }
                        currTile.items().clear();
                    }
                    action.append(new SetField(game, "playerCanMove", true, null));
                    game.insertAction(action);
                    if (game.type == Game.Type.CLIENT) {
                        game.client.sendTCP(new Network.UseHM(game.player.network.id, 0, "CUT", game.player.dirFacing));
                    }
                }
                this.detectIsHouseBuilt(game, currTile);
            }
            else if (game.player.currFieldMove.equals("SMASH")) {
                if (currTile.attrs.get("smashable")) {
                    Action action = new CutTreeAnim(game, game.map.tiles.get(pos), null);
                    game.playerCanMove = false;
                    // Get items from tile
                    currTile.items().remove("torch");  // if torch is on it, don't pick it up.
                    if (!currTile.items().isEmpty()) {
                        action.append(new SplitAction(new DrawItemPickup(currTile.items(), null),
                                      null));
                        for (String item : currTile.items().keySet()) {
                            // TODO: remove
//                            String plural = "";
//                            if (currTile.items().get(item) > 1) {
//                                plural = "s";
//                            }
//                            action.append(new DisplayText(game, "Picked up "+currTile.items().get(item)+" "+item.toUpperCase()+plural+".", null, null,
//                                          null));
                            if (game.player.itemsDict.containsKey(item)) {
                                int currQuantity = game.player.itemsDict.get(item);
                                game.player.itemsDict.put(item, currQuantity+currTile.items().get(item));
                            }
                            else {
                                game.player.itemsDict.put(item, currTile.items().get(item));
                            }
                        }
                        currTile.items().clear();
                    }
                    // TODO: why the rand check? should just make the route empty, or null
                    if (currTile.routeBelongsTo != null &&
                        !currTile.routeBelongsTo.pokemon.isEmpty()) {
                        game.player.setCurrPokemon();
                        game.playerCanMove = false;
//                        game.battle.oppPokemon = currTile.routeBelongsTo.pokemon.get(game.map.rand.nextInt(currTile.routeBelongsTo.pokemon.size()));  // TODO: remove
                        game.battle.oppPokemon = currTile.routeBelongsTo.pokemon.get(0);
//                        game.insertAction(Battle_Actions.get(game));
                        // new DisplayText(game, "A wild pokémon attacked!", null, null,
//                        game.musicController.startBattle = "wild";  // TODO: remove
                        action.append(new WaitFrames(game, 16,
                                      new SetField(game.musicController, "startBattle", "wild",
                                      Battle.getIntroAction(game))));
                        this.checkWildEncounter = false;
                        shouldMove = false;  // for safety
                    }
                    action.append(new SetField(game, "playerCanMove", true, null));
                    game.insertAction(action);
                    // TODO: for rock smash, depending on how network works in the future.
//                    if (game.type == Game.Type.CLIENT) {
//                        game.client.sendTCP(new Network.UseHM(game.player.network.id, 0, "CUT", game.player.dirFacing));
//                    }
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
                // TODO: remove
//                Tile temp = game.map.tiles.get(pos);
//                if (temp != null) {
//                    temp.onPressA(game);
//                }
                currTile.onPressA(game);
            }
        }

        // TODO: test that moving this is ok
        // Draw the sprite corresponding to player direction
        if (this.isRunning) {
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
            if (game.player.dirFacing.equals("down") && 
                (currTile.name.contains("rug") || (temp != null && temp.name.contains("entrance")))) {
                if (game.map.tiles == game.map.overworldTiles) {
                    // Do enter building anim, then player travels down one space
                    game.insertAction(new EnterBuilding(game, "enter", game.map.interiorTiles.get(game.map.interiorTilesIndex),
                                      new PlayerMoving(game, this.alternate)));
                }
                else {
                    // Do leave building anim, then player travels down one space
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
            if (game.map.pokemon.containsKey(newPos) && game.map.pokemon.get(newPos).mapTiles == game.map.tiles) {
                game.insertAction(new PlayerBump(game));
                return;
            }
            if (game.player.currFieldMove.equals("RIDE")) {
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
            // Can't run thru desert 'sand pit' tile
            if (InputProcessor.bPressed && !temp.name.contains("desert2")) {
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
            if (game.map.pokemon.containsKey(newPos) && game.map.pokemon.get(newPos).mapTiles == game.map.tiles) {
                game.insertAction(new PlayerBump(game, this.player));
                return;
            }
            if (game.player.currFieldMove.equals("RIDE")) {
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
                // Check wild encounter on next position, send back if yes
                Pokemon pokemon = checkWildEncounter(game, newPos);
                if (pokemon != null) {
                    // The first Pokemon the player sends out in battle should
                    // have > 0 hp.
                    // TODO: this really needs to go in an action somewhere in the action chain (DrawBattle?)
//                    for (Pokemon currPokemon : this.player.pokemon) {
//                        if (currPokemon.currentStats.get("hp") > 0 && !currPokemon.name.equals("egg")) {
//                            this.player.currPokemon = currPokemon;
//                            break;
//                        }
//                    }
                    this.player.setCurrPokemon();
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
                boolean outdoors = (this.player.network.tiles == game.map.overworldTiles);
//                if (game.type == Game.Type.CLIENT) {
                if (this.player.type == Player.Type.LOCAL) {
                    outdoors = (game.map.tiles == game.map.overworldTiles);
                }
                // Restore pokemon hp
                // TODO: restore player hp
                // TODO: this needs to go in remoteStep or something for remote player
                for (Pokemon pokemon : this.player.pokemon) {
                    if (pokemon.isEgg) {
                        continue;
                    }
                    if (outdoors) {
                        pokemon.currentStats.put("hp", pokemon.currentStats.get("hp")+1);
                    }
                    else {
                        pokemon.currentStats.put("hp", pokemon.currentStats.get("hp")+2);
                    }
                    if (this.player.sleepingDir != null) {  // If using bed, not sleeping bag
                        pokemon.currentStats.put("hp", pokemon.currentStats.get("hp")+2);
                    }
                    if (pokemon.currentStats.get("hp") >= pokemon.maxStats.get("hp")) {
                        pokemon.currentStats.put("hp", pokemon.maxStats.get("hp"));
                        if (this.player.sleepingDir != null) {  // If using bed, not sleeping bag
                            pokemon.status = null;
                        }
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
        if (part1 == 80) {
            game.playerCanMove = false;
            // Set player to face ghost
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
            game.musicController.startNightAlert = "night1_alert1";
            

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
//        game.currMusic.stop();  
//        game.currMusic.dispose();
//        Music music = Gdx.audio.newMusic(Gdx.files.internal("night1_chase1.ogg"));
//        music.setLooping(true);
//        music.setVolume(.7f);
//        game.currMusic = music;
////        game.musicController.currOverworldMusic = "night1_chase1";
////        game.map.currRoute.music = music; // TODO - how to switch to normal after defeating
//        game.currMusic.play();
        game.musicController.startNightAlert = "night1_chase1";

        game.playerCanMove = true;
        game.actionStack.remove(this);
        game.insertAction(new DrawGhost(game, this.position));
    }
}
