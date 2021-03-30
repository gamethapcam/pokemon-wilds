package com.pkmngen.game;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.Network.PokemonData;
import com.pkmngen.game.util.Direction;
import com.pkmngen.game.util.SpriteProxy;
import com.pkmngen.game.util.TextureCache;

public class Pokemon {

    // TODO: eventually remove
    public static ArrayList<String> attacksNotImplemented = new ArrayList<String>();
    static {
//        attacksNotImplemented.add("absorb");  // TODO: remove
        attacksNotImplemented.add("barrage");
        attacksNotImplemented.add("baton pass");
        attacksNotImplemented.add("beat Up");
        attacksNotImplemented.add("belly drum");
        attacksNotImplemented.add("bide");
        attacksNotImplemented.add("bind");
        attacksNotImplemented.add("bone rush");
        attacksNotImplemented.add("bonemerang");
        attacksNotImplemented.add("charm");
        attacksNotImplemented.add("comet punch");
        attacksNotImplemented.add("conversion");
        attacksNotImplemented.add("conversion 2");
        attacksNotImplemented.add("counter");
        attacksNotImplemented.add("curse");
        attacksNotImplemented.add("protect");
        attacksNotImplemented.add("destiny bond");
        attacksNotImplemented.add("detect");
        attacksNotImplemented.add("dig");
        attacksNotImplemented.add("double edge");
        attacksNotImplemented.add("double kick");
        attacksNotImplemented.add("double slap");
        attacksNotImplemented.add("dragon rage");
        attacksNotImplemented.add("dream eater");
        attacksNotImplemented.add("encore");
        attacksNotImplemented.add("endure");
//        attacksNotImplemented.add("explosion");  // TODO: remove
        attacksNotImplemented.add("fissure");
        attacksNotImplemented.add("flail");
        attacksNotImplemented.add("flame wheel");
        attacksNotImplemented.add("fly");
        attacksNotImplemented.add("focus energy");
        attacksNotImplemented.add("foresight");
        attacksNotImplemented.add("frustration");
        attacksNotImplemented.add("fury attack");
        attacksNotImplemented.add("fury swipes");
        attacksNotImplemented.add("future sight");
//        attacksNotImplemented.add("giga drain");  // TODO: remove
        attacksNotImplemented.add("guillotine");
        attacksNotImplemented.add("haze");
        attacksNotImplemented.add("heal bell");
        attacksNotImplemented.add("horn drill");
//        attacksNotImplemented.add("leech life");  // TODO: remove
        attacksNotImplemented.add("leech seed");
        attacksNotImplemented.add("light screen");
        attacksNotImplemented.add("lock on");
        attacksNotImplemented.add("magnitude");
        attacksNotImplemented.add("mean look");
//        attacksNotImplemented.add("mega drain");  // TODO: remove
        attacksNotImplemented.add("metronome");
        attacksNotImplemented.add("mimic");
        attacksNotImplemented.add("mind reader");
        attacksNotImplemented.add("mirror coat");
        attacksNotImplemented.add("mirror move");
        attacksNotImplemented.add("mist");
        attacksNotImplemented.add("nightmare");
        attacksNotImplemented.add("pain split");
        attacksNotImplemented.add("perish song");
        attacksNotImplemented.add("petal dance");
        attacksNotImplemented.add("pin missile");
        attacksNotImplemented.add("present");
        attacksNotImplemented.add("rage");
        attacksNotImplemented.add("rain dance");
        attacksNotImplemented.add("reflect");
        attacksNotImplemented.add("rest");
        attacksNotImplemented.add("reversal");
        attacksNotImplemented.add("roar");
        attacksNotImplemented.add("rollout");
        attacksNotImplemented.add("safeguard");
        attacksNotImplemented.add("sandstorm");
        attacksNotImplemented.add("seismic toss");
//        attacksNotImplemented.add("selfdestruct");  // TODO: remove
        attacksNotImplemented.add("sketch");
        attacksNotImplemented.add("skull bash");
        attacksNotImplemented.add("sky attack");
        attacksNotImplemented.add("sleep talk");
        attacksNotImplemented.add("snore");
        attacksNotImplemented.add("solar beam");
        attacksNotImplemented.add("spider web");
        attacksNotImplemented.add("spike cannon");
        attacksNotImplemented.add("spikes");
        attacksNotImplemented.add("spite");
        attacksNotImplemented.add("substitute");
        attacksNotImplemented.add("sunny day");
        attacksNotImplemented.add("swagger");
        attacksNotImplemented.add("teleport");
        attacksNotImplemented.add("thrash");
        attacksNotImplemented.add("transform");
        attacksNotImplemented.add("triple kick");
        attacksNotImplemented.add("twineedle");
        attacksNotImplemented.add("whirlwind");
    }

    // TODO: don't do it this way
    public static ArrayList<String> nuukPokemon = new ArrayList<String>();
    static {
        String[] temp = new String[]{"aggron", "aron", "exploud", "gardevoir", "hariyama", "kirlia",
                                     "lairon", "lombre", "lotad", "loudred", "ludicolo", "makuhita",
                                     "ralts", "taillow", "swellow", "whismur", "poochyena", "mightyena",
                                     "wingull", "pelipper", "shroomish", "breloom", "surskit", "masquerain",
                                     "sableye",
                                     "aexeggutor",  // Gmerc
                                     "zigzagoon", "linoone", // TODO: need ow sprite
                                     "dwebble",  // overworld 
                                     "crustle",  // overworld 
                                     "litwick", "lampent", "chandelure",  // overworld Goose (discord)
                                     "corphish",  // sir-feralipogchamp (discord)
                                     "crawdaunt",  // sir-feralipogchamp, Mr Dustman, Goose (discord)
                                     "mimikyu",  // boomtox-the-boombox (discord)
                                     "scorbunny",  // Internet_Goblin on discord
                                     "raboot",  // Internet_Goblin on discord
                                     "regieleki", "regidrago", "registeel", "regirock", "regice", "regigigas", // Mr Dustman and Sadfish on discord
                                     "bronzor", "bronzong",  // SkwovetSquire on discord
                                     "darumaka",  // Goose on discord
                                     "elgyem", "beheeyem",  // Goose on discord
                                     "sandile", "krokorok", "krookodile",  // Goose and Sadfish on discord
                                     "cutiefly", "ribombee",  // TerraTerraCotta on discord
                                     "combee",  // TerraTerraCotta on discord
                                     "snover"};  // TODO: sep loading method
        for (String t : temp) {
            nuukPokemon.add(t);
        }
    }
    // Contains all loaded pkmn textures, so that only one is used for each pkmn. ie don't load duplicate textures.
    public static HashMap<String, Texture> textures = new HashMap<String, Texture>();
    // Add to this when loading pokemon
    public static HashMap<String, Map<String, String>> gen2Evos = new HashMap<String, Map<String, String>>();
    public static HashMap<String, Map<Integer, String[]>> gen2Attacks = new HashMap<String, Map<Integer, String[]>>();
    // Name->Base species name. Required by egg-laying.
    public static HashMap<String, String> baseSpecies = new HashMap<String, String>();
    public static HashMap<String, ArrayList<String>> eggMoves = new HashMap<String, ArrayList<String>>();
    static {
        try {
            // TODO: prism
            for (String path : new String[]{"", "nuuk/"}) {
                FileHandle file = Gdx.files.internal("crystal_pokemon/"+path+"evos_attacks.asm");
                Reader reader = file.reader();
                BufferedReader br = new BufferedReader(reader);
                String line;
                String currMon = null;
                while ((line = br.readLine()) != null)   {
                    if (line.contains("EvosAttacks:")) {
                        currMon = line.split("EvosAttacks:")[0].toLowerCase();
                        // Fix some formatting exceptions (evos_attacks entries are camelcase,
                        // everything else seems to use underscore formatting).
                        if (currMon.equals("nidoranf")) {
                            currMon = "nidoran_f";
                        }
                        else if (currMon.equals("nidoranm")) {
                            currMon = "nidoran_m";
                        }
                        continue;
                    }
                    if (currMon == null) {
                        continue;
                    }
                    if (line.contains(";")) {
                        // skip commented line
                        continue;
                    }
                    if (line.contains("EVOLVE")) {
                        String vals[] = line.split(", ");
                        String baseMon = Pokemon.baseSpecies.get(currMon);
                        if (baseMon == null) {
                            baseMon = currMon;
                        }
                        Pokemon.baseSpecies.put(vals[2].toLowerCase(), baseMon);
                    }
                    else if (!line.contains("\t")) {
                        // Base forms put self
                        String baseMon = Pokemon.baseSpecies.get(currMon);
                        if (baseMon == null) {
                            baseMon = Pokemon.baseSpecies.put(currMon, currMon);
                        }
                        currMon = null;
                    }
                }
                reader.close();
                // Load egg moves
                file = Gdx.files.internal("crystal_pokemon/"+path+"egg_moves.asm");
                reader = file.reader();
                br = new BufferedReader(reader);
                currMon = null;
                while ((line = br.readLine()) != null)   {
                    if (line.contains("EggMoves:")) {
                        currMon = line.split("EggMoves:")[0].toLowerCase();
                        // Fix some formatting exceptions (evos_attacks entries are camelcase,
                        // everything else seems to use underscore formatting).
                        if (currMon.equals("nidoranf")) {
                            currMon = "nidoran_f";
                        }
                        else if (currMon.equals("nidoranm")) {
                            currMon = "nidoran_m";
                        }
                        Pokemon.eggMoves.put(currMon, new ArrayList<String>());
                        continue;
                    }
                    if (currMon == null) {
                        continue;
                    }
                    if (line.contains(";")) {
                        // skip commented line
                        continue;
                    }
                    if (!line.contains("\t")) {
                        currMon = null;
                        continue;
                    }
                    Pokemon.eggMoves.get(currMon).add(line.split("db ")[1].trim().toLowerCase().replace("_", " "));
                }
                reader.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String nameToIndex(String name) {
        name = name.toLowerCase();
        if (name.contains("unown")) {
            name = "unown";
        }
        if (name.equals("ghost")) {
            return "000";
        }
        if (name.equals("egg")) {
            return "999";
        }
        // TODO: handle all alolan forms here
        if (name.equals("aexeggutor")) {
            name = "exeggutor";
        }

        int lineNum = 1;
        try {
            FileHandle file = Gdx.files.internal("crystal_pokemon/pokemon_to_index.txt");
            Reader reader = file.reader();
            BufferedReader br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null)   {
                if (line.toLowerCase().equals(name)) {
                    break;
                }
                lineNum++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return String.format("%03d", lineNum);
    }

    String name;
    int level;
    int exp;  // current total exp.
    String dexNumber;
    int happiness = 1; // TODO: replicate gen 2 mechanics for happiness.
    boolean isShiny = false;  // shiny or not
    String growthRateGroup = "";

    // Keep all pkmn textures in an array to be loaded
    // that way whenever a pkmn is created, it doesn't re-load the texture
    // takes up a lot of memory
    // TODO: migrate to use TextureCache
    Texture[] pokemonTextures = new Texture[650]; // 650 for now since that's how many cries in folder

    Map<String, Integer> baseStats = new HashMap<String, Integer>();
    Map<String, Integer> currentStats = new HashMap<String, Integer>();
    Map<String, Integer> statStages = new HashMap<String, Integer>();
    {
        this.statStages.put("attack", 0);
        this.statStages.put("defense", 0);
        this.statStages.put("specialAtk", 0);
        this.statStages.put("specialDef", 0);
        this.statStages.put("speed", 0);
        this.statStages.put("accuracy", 0);
        this.statStages.put("evasion", 0);
    }

    // note - this doesn't go in 'maxStats' map
    // int catchRate; // may put into some other map later

    Map<String, Integer> maxStats = new HashMap<String, Integer>();
    ArrayList<String> hms = new ArrayList<String>();
    Map<String, Integer> IVs = new HashMap<String, Integer>();

    public String status = null;  // Store current status (sleep, poison, paralyze etc) here.
    public int statusCounter = 0;  // Number of turns left with the current status condition.
    public int disabledIndex = -1;  // Index of disabled attack.
    public int disabledCounter = 0;  // Number of turns left disabled.
    public boolean flinched = false;

    SpriteProxy sprite;
    SpriteProxy backSprite;

    // overworld movement sprites
    public Map<String, Sprite> standingSprites = new HashMap<String, Sprite>();
    public Map<String, Sprite> movingSprites = new HashMap<String, Sprite>();
    public Map<String, Sprite> altMovingSprites = new HashMap<String, Sprite>();
    public String dirFacing = "down";
    public Vector2 position = new Vector2(); // position in overworld
    public Action standingAction = null;
    public Sprite currOwSprite = null;
    public int spriteOffsetY = 0;
    public boolean canMove = true;
    // Keeps track of if this was set loose in overworld, indoors, etc.
    Map<Vector2, Tile> mapTiles;
    boolean isRunning = false;
    public boolean shouldMove = false;  // used by client listener to trigger remote pokemon to move.
    Player.Type type = Player.Type.LOCAL;
    Player previousOwner = null;
    // Related to overworld habitat
    public boolean inHabitat = false;
    public boolean inShelter = false;
    public ArrayList<String> habitats = new ArrayList<String>();
    {
        habitats.add("green");
    }
    public String hasItem = null;  // Harvestable item that the pokemon is holding
    public int harvestTimer = 0;  // Counts up to when pokemon has harvestable item.
    public int harvestTimerMax = (int)(3600f*2.5f);  // Amount of time (number of frames) required to harvest. 2.5 irl minutes by default.
    public ArrayList<String> harvestables = new ArrayList<String>();
    {
        harvestables.add("manure");
    }

    // this reference is used when needing to stop drawing pokemon in battle screen
     // could also just be oppPokemonDrawAction in battle, I think
    // Action drawAction; // doesn't work. also, using sprite alpha for now

    ArrayList<Sprite> avatarSprites = new ArrayList<Sprite>();
    // need to be able to manipulate this for
    // normal pkmn don't use this - so far only specialmewtwo and mega gengar
    Sprite breathingSprite = null;
    ArrayList<String> types;
    int angry, eating; // nonzero if angry or eating. safari zone mechanic
    String[] attacks;

    // egg groups pokemon belongs to
    String gender = null;  // male, female, and unknown. null means it needs to be assigned.
    String[] eggGroups = new String[2];
    // Egg is going to contain ivs, moves, etc of pokemon it evolves to
    // this isn't changed anyway via evolveTo() so should be able to use
    // that (?)
    String eggHatchInto = null;  // Pokemon that egg hatches into. 
    Pokemon loveInterest = null;  // Which pokemon this pokemon may breed with.
    int layEggTimerMax = 3600*2;  // 3600*1
    int layEggTimer = layEggTimerMax;
    int nearbyEggs = 0;  // used to prevent pokemon from laying too many eggs

    String trappedBy = null;  // whirlpool, wrap, bind, etc
    int trapCounter = 0;  // number turns remaining for trap
    Map<Integer, String[]> learnSet;

    boolean inBattle = false;
    public boolean aggroPlayer = false;  // If true, pokemon chases the player.
    public boolean interactedWith = false;  // used for evolved overworld pokemon to aggro player
    public boolean drawThisFrame = false;
    
    // Whether or not player can flee.
    public boolean isTrapping = false;
    
    // TODO: if a pokemon is switched out in a trainer battle, does the game
    //       remember which Pokemon were used against the switched out pokemon?
    //       Doesn't matter atm but may matter down the road.
    public boolean participatedInBattle = false;  // whether or not the Pokemon participated in this battle.
    public boolean gainedLevel = false;  // whether or not the Pokemon gained a level this battle.

    Generation generation;
    ArrayList<SpriteProxy> introAnim;
    public ArrayList<String> moveDirs = new ArrayList<String>();
    public ArrayList<Integer> ledgeJumps = new ArrayList<Integer>();  // 1 or 2, 2 is faster.
    public ArrayList<Float> numMoves = new ArrayList<Float>();

    public static Random rand = new Random();

    // TODO: put these in OverworldThing
    // TODO: this is just for testing. Trying to make API
    //       cleaner and not use Direction.* everywhere.
    // TODO: enable and migrate to start using this. 
//    public Direction UP = Direction.UP;
//    public Direction DOWN = Direction.DOWN;
//    public Direction LEFT = Direction.LEFT;
//    public Direction RIGHT = Direction.RIGHT;

    public Pokemon(Network.PokemonDataBase pokemonData) {
        // Fields added in v0.5
        // TODO: I guess I will have to rename this to Network.PokemonDataV05 when moving to v0.6
        // TODO: if I don't rename this, it will introduce a bug. I can't think of a better way to do it tho.
        if (Network.PokemonData.class.isInstance(pokemonData)) {
            this.eggHatchInto = ((Network.PokemonData)pokemonData).eggHatchInto;
        }

        this.init(pokemonData.name, pokemonData.level, pokemonData.generation, pokemonData.isShiny, this.eggHatchInto);
//        this.isShiny = pokemonData.isShiny;
        this.currentStats.put("hp", pokemonData.hp);
        this.attacks[0] = pokemonData.attacks[0];
        this.attacks[1] = pokemonData.attacks[1];
        this.attacks[2] = pokemonData.attacks[2];
        this.attacks[3] = pokemonData.attacks[3];
        this.position = pokemonData.position;
        if (pokemonData.isInterior) {
            // TODO: ideally store pokemon layer, similar to player
//            this.mapTiles = Game.staticGame.map.interiorTiles.get(Game.staticGame.map.interiorTilesIndex);
            // TODO: will need this when adding different floors to buildings
            System.out.println("Game.staticGame.map.interiorTilesIndex");
            System.out.println(Game.staticGame.map.interiorTilesIndex);
            this.mapTiles = Game.staticGame.map.interiorTiles.get(100);
        }
        else {
            this.mapTiles = Game.staticGame.map.overworldTiles;
        }
        this.initHabitatValues();  // TODO: done in init(), shouldn't need this (although harmless probably)
        this.status = pokemonData.status;
        this.harvestTimer = pokemonData.harvestTimer;
        if (pokemonData.previousOwnerName != null) {
            this.previousOwner = Game.staticGame.player;  // TODO: this doesn't work b/c game.player is just a dummy player here.
            if (Game.staticGame.players.containsKey(pokemonData.previousOwnerName)) {
                // on Server, player.name == player.network.id, so can get from game.players
                // based on player.name.
                this.previousOwner = Game.staticGame.players.get(pokemonData.previousOwnerName);
            }
        }
        // TODO: load from file once happiness is tied to things other than if pokemon runs in overworld.
        if (this.name.toLowerCase().equals("tauros") || this.name.toLowerCase().equals("ekans")
                || this.name.toLowerCase().equals("pidgey") || this.name.toLowerCase().equals("spearow")
                || this.name.toLowerCase().equals("rattata")) {
            this.happiness = 0;
        }

        // Fields added in v0.5
        // TODO: I guess I will have to rename this to Network.PokemonDataV05 when moving to v0.6
        // TODO: if I don't rename this, it will introduce a bug. I can't think of a better way to do it tho.
        if (Network.PokemonData.class.isInstance(pokemonData)) {
            this.gender = ((Network.PokemonData)pokemonData).gender;
            this.happiness = ((Network.PokemonData)pokemonData).friendliness;
            this.aggroPlayer = ((Network.PokemonData)pokemonData).aggroPlayer;
        }
    }

    public Pokemon (String name, int level) {
        // generation defaults to RED
        this(name, level, Generation.RED);
    }
    
    public Pokemon (String name, int level, Generation generation) {
        this(name, level, generation, Pokemon.rand.nextInt(256) == 0);
//        System.out.println("here3");
    }

    public Pokemon (String name, int level, Generation generation, boolean isShiny) {
        this(name, level, generation, isShiny, null);
    }

    public Pokemon (String name, int level, Generation generation, boolean isShiny, String eggHatchInto) {
        this.init(name, level, generation, isShiny, eggHatchInto);
    }
    
    /**
     * TODO: remove isShiny and eggHatchInto params, just set before calling init.
     */
    public void init(String name, int level, Generation generation, boolean isShiny, String eggHatchInto) {
        this.isShiny = isShiny;  //Pokemon.rand.nextInt(256) == 0;
        this.eggHatchInto = eggHatchInto;

        name = name.toLowerCase();
        if (name.equals("unown")) {  // TODO: this was to fix a bug, remove
            name = "unown_w";
        }
        this.name = name;
        // levels have to be >= 1
        if (level <= 0) {
            System.out.println("Bad level: " + String.valueOf(level));
            level = 1;
        }
        this.level = level;
        this.generation = generation;

        this.types = new ArrayList<String>();

        // init vars
        this.angry = 0;
        this.eating = 0;

        this.attacks = new String[]{null, null, null, null};
        this.learnSet = new HashMap<Integer, String[]>();

        // TODO: individual avatars
        // TODO: remove if unused
//        Texture avatarText = new Texture(Gdx.files.internal("pokemon_menu/avatars1.png"));
//        this.avatarSprites = new ArrayList<Sprite>();
//        this.avatarSprites.add(new Sprite(avatarText, 16*0, 16*0, 16, 16));
//        this.avatarSprites.add(new Sprite(avatarText, 16*1, 16*0, 16, 16));

        // if generation is crystal, load from file
        if (generation.equals(Generation.CRYSTAL)) {
            this.dexNumber = Pokemon.nameToIndex(name);
            // if it is in original 251, load from crystal
            if (this.name.equals("egg") ||
                Pokemon.nuukPokemon.contains(this.name.toLowerCase()) ||
                (Integer.valueOf(this.dexNumber) <= 251 && Integer.valueOf(this.dexNumber) > 0)) {
                this.loadCrystalPokemon(name);
            // else try loading from prism
            } else {
                this.loadPrismPokemon(name);
            }
            this.loadOverworldSprites(name);

            // Custom attributes - better way to handle this?
//            if (name.equals("machop")) {
//                this.hms.add("CUT");  // TODO: debug, remove
////                this.hms.add("BUILD");
//            }

            // Custom attributes - better way to handle this?
            if (!this.name.equals("egg")) {
                if (name.equals("sneasel") || 
                    name.equals("scyther") ||
                    name.equals("pinsir")) {
                    this.hms.add("CUT");
                }
                // TODO: for now, all grass types can cut
                if (this.types.contains("GRASS")) {
                    this.hms.add("CUT");
                }
                if (this.types.contains("ROCK")) {
                    this.hms.add("SMASH");
                }
                if (this.types.contains("FIGHTING")) {
                    this.hms.add("BUILD");
                }
                if (this.types.contains("FIRE") ||
                    // TODO: pokemon that can light
                    name.equals("chinchou") ||
                    name.equals("lanturn") ||
                    name.equals("mareep") ||
                    name.equals("flaaffy") ||
                    name.equals("ampharos")) {
                    // Calling it FLASH for now, since that's what most people
                    // are familiar with.
                    this.hms.add("FLASH");  
                }

                // TODO: different pokemon than machop
                // Custom attributes - better way to handle this?
//                if (name.equals("machop")) {
//                    this.hms.add("HEADBUTT");
//                }
                if (name.equals("hypno") ||
                    name.equals("nidorina") ||
                    name.equals("nidoqueen") ||
                    name.equals("nidorino") ||
                    name.equals("nidoking") ||
                    name.equals("granbull") ||
                    name.equals("jynx") ||
                    name.equals("snorlax") ||
                    name.equals("ursaring")) {
                    this.hms.add("HEADBUTT");
                }

                // Custom attributes - better way to handle this?
                if (name.equals("stantler") ||
                    name.equals("ponyta") ||
                    name.equals("arcanine") ||
                    name.equals("donphan") ||
                    name.equals("girafarig") ||
                    name.equals("houndoom") ||
                    name.equals("rapidash") ||
                    name.equals("tauros") ||
                    name.equals("ninetales") ||
                    name.equals("mamoswine") ||
                    name.equals("dodrio") ||
                    name.equals("mightyena") ||
                    //
                    name.equals("persian") ||
                    name.equals("onix") ||
                    name.equals("steelix") ||
                    name.equals("haunter") ||
                    name.equals("rhyhorn") ||
                    name.equals("rhydon") ||
                    //
                    name.equals("luxray")) {
                    // TODO: change to 'RIDE' later. Making it 'JUMP' for now so that it's not confusing.
                    // Later, once there (hopefully) are riding sprites, this can be changed to ride.
                    // My current idea is that RIDE increases movement speed and can perform jumps up ledges.
                    this.hms.add("RIDE");
                }

                if (name.equals("pidgeot") ||
                    name.equals("aerodactyl") ||
                    name.equals("charizard") ||
                    name.equals("dragonair") ||
                    name.equals("dragonite") ||
                    name.equals("salamence") ||
                    name.equals("ho_oh") ||
                    name.equals("lugia") ||
                    name.equals("skarmory") ||
                    name.equals("articuno") ||
                    name.equals("zapdos") ||
                    name.equals("moltres") ||
                    name.equals("crobat") ||
                    name.equals("noctowl") ||
                    name.equals("xatu") ||
                    // the animations starting here fit pretty well
                    name.equals("flygon") ||
                    name.equals("togekiss") ||
                    name.equals("swellow") ||
                    name.equals("pelipper") ||
                    name.equals("altaria") ||
                    name.equals("rayquaza") ||
//                    name.equals("farfetch_d") ||  // TODO: removed
                    name.equals("drifblim") ||
                    name.equals("honchkrow") ||
                    name.equals("yanmega") ||
                    name.equals("fearow")) {
                    //
                    this.hms.add("FLY");
                }
                // Ability to aggro any mon
                if (this.types.contains("DARK") && !this.hms.contains("RIDE")) {
                    this.hms.add("ATTACK");
                }
            }
        }

        else if (name == "Zubat") { // gen I properties
            this.baseStats.put("hp",40);
            this.baseStats.put("attack",45);
            this.baseStats.put("defense",35);
            this.baseStats.put("specialAtk",30);
            this.baseStats.put("specialDef",40);
            this.baseStats.put("speed",55);
            this.baseStats.put("catchRate", 255);
            // sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/zubat.png"));
            this.sprite = new SpriteProxy(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); // my sprites are backwards

            this.types.add("Poison");
            this.types.add("Flying");
        }
        else if (name.equals("Rattata")) { // gen I properties
            this.baseStats.put("hp",30);
            this.baseStats.put("attack",56);
            this.baseStats.put("defense",35);
            this.baseStats.put("specialAtk",25);
            this.baseStats.put("specialDef",25);
            this.baseStats.put("speed",72);
            this.baseStats.put("catchRate", 255);
            // sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/rattata.png"));
            this.sprite = new SpriteProxy(pokemonText, 0, 0, 56, 56);
//            this.sprite.flip(true, false); // my sprites are backwards

            this.types.add("Normal");
        }
        else if (name == "Cloyster") { // gen I properties
//            this.baseStats.put("hp",50);
            this.baseStats.put("hp",500);
            this.baseStats.put("attack",95);
            this.baseStats.put("defense",180);
            this.baseStats.put("specialAtk",85);
            this.baseStats.put("specialDef",85);
            this.baseStats.put("speed",70);
            this.baseStats.put("catchRate", 60);
            // sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/pokemon_sheet1.png"));
            this.sprite = new SpriteProxy(pokemonText, 56*28, 56*2, 56, 56);
            this.sprite.flip(true, false); // my sprites are backwards

            // back sprite
            pokemonText = new Texture(Gdx.files.internal("pokemon/back_sheet1.png"));
            this.backSprite = new SpriteProxy(pokemonText, 30*2+1, 29*8+1, 28, 28); // sheet is a little messed up, hard to change
            this.backSprite.setScale(2);
//            pokemonText = new Texture(Gdx.files.internal("pokemon/back_sheet1.png")); // debug - change to charmander sprite
//            this.backSprite = new SpriteProxy(pokemonText, 30*3+1, 29*0+1, 28, 28); //
//            this.backSprite.setScale(2);

            // moves that cloyster can learn
            // TODO: was this, removed for demo
//            learnSet.put(1, new String[]{"Aurora Beam", "Clamp", "Supersonic", "Withdraw"});
            learnSet.put(1, new String[]{"Clamp"});
//            learnSet.put(1, new String[]{"Fly", "Growl", "Hyper Beam"});
            // learnSet.put(20, new String[]{"Harden", "Harden", "Harden", "Harden"}); // debug
//            learnSet.put(50, new String[]{"Spike Cannon"});  // TODO: re-enable

//            learnSet.put(1, new String[]{"Razor Leaf"});
//            learnSet.put(1, new String[]{"Fury Cutter"});
//            learnSet.put(1, new String[]{"Ember"});
            learnSet.put(1, new String[]{"Ice Beam"});
//            learnSet.put(2, new String[]{"Earthquake"});
//            learnSet.put(3, new String[]{"Rock Throw"});
            learnSet.put(2, new String[]{"Peck"});
//            learnSet.put(2, new String[]{"Surf"});
            learnSet.put(3, new String[]{"Whirlpool"});
            learnSet.put(4, new String[]{"Hydro Pump"});
            this.types.add("Water");
            this.types.add("Ice");
        }
        else if (name == "Spinarak") { // gen I properties
            this.baseStats.put("hp",45);
            this.baseStats.put("attack",50);
            this.baseStats.put("defense",55);
            this.baseStats.put("specialAtk",75);
            this.baseStats.put("specialDef",75);
            this.baseStats.put("speed",30);
            this.baseStats.put("catchRate", 255);
            // sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/spinarak.png"));
            this.sprite = new SpriteProxy(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); // my sprites are backwards

            this.types.add("Bug");
            this.types.add("Poison");
        }
        else if (name == "Oddish") { // gen I properties
            this.baseStats.put("hp",45);
            this.baseStats.put("attack",50);
            this.baseStats.put("defense",55);
            this.baseStats.put("specialAtk",75);
            this.baseStats.put("specialDef",75);
            this.baseStats.put("speed",30);
            this.baseStats.put("catchRate", 255);
            // sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/oddish.png"));
            this.sprite = new SpriteProxy(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); // my sprites are backwards

            this.types.add("Grass");
            this.types.add("Poison");
        }
        else if (name == "Gloom") { // gen I properties
            this.baseStats.put("hp",60);
            this.baseStats.put("attack",65);
            this.baseStats.put("defense",70);
            this.baseStats.put("specialAtk",85);
            this.baseStats.put("specialDef",85);
            this.baseStats.put("speed",40);
            this.baseStats.put("catchRate", 120);
            // sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/gloom.png"));
            this.sprite = new SpriteProxy(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); // my sprites are backwards

            this.types.add("Grass");
            this.types.add("Poison");
        }
        else if (name == "Electabuzz") { // gen I properties
            this.baseStats.put("hp",65);
            this.baseStats.put("attack",83);
            this.baseStats.put("defense",57);
            this.baseStats.put("specialAtk",85);
            this.baseStats.put("specialDef",85);
            this.baseStats.put("speed",105);
            this.baseStats.put("catchRate", 45);
            // sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/electabuzz.png"));
            this.sprite = new SpriteProxy(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); // my sprites are backwards

            this.types.add("Electric");
        }

        else if (name == "Scyther") { // gen I properties
            this.baseStats.put("hp",70);
            this.baseStats.put("attack",110);
            this.baseStats.put("defense",80);
            this.baseStats.put("specialAtk",55);
            this.baseStats.put("specialDef",55);
            this.baseStats.put("speed",105);
            this.baseStats.put("catchRate", 45);
            // sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/scyther.png"));
            this.sprite = new SpriteProxy(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); // my sprites are backwards

            this.types.add("Bug");
        }

        else if (name == "Tauros") { // gen I properties
            this.baseStats.put("hp",75);
            this.baseStats.put("attack",100);
            this.baseStats.put("defense",95);
            this.baseStats.put("specialAtk",70);
            this.baseStats.put("specialDef",70);
            this.baseStats.put("speed",110);
            this.baseStats.put("catchRate", 45);
            // sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/tauros.png"));
            this.sprite = new SpriteProxy(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); // my sprites are backwards

            this.types.add("Normal");
        }

        else if (name == "Mareep") { // gen I properties
            this.baseStats.put("hp",55);
            this.baseStats.put("attack",40);
            this.baseStats.put("defense",40);
            this.baseStats.put("specialAtk",65);
            this.baseStats.put("specialDef",45);
            this.baseStats.put("speed",35);
            this.baseStats.put("catchRate", 235);
            // sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/mareep.png"));
            this.sprite = new SpriteProxy(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false);

            this.types.add("Electric");
        }
        else if (name == "Flaaffy") { // gen I properties
            this.baseStats.put("hp",70);
            this.baseStats.put("attack",55);
            this.baseStats.put("defense",55);
            this.baseStats.put("specialAtk",80);
            this.baseStats.put("specialDef",60);
            this.baseStats.put("speed",45);
            this.baseStats.put("catchRate", 255);
            // sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/flaaffy.png"));
            this.sprite = new SpriteProxy(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); // my sprites are backwards

            this.types.add("Electric");
        }

        else if (name == "Steelix") { // gen II properties
            this.baseStats.put("hp",75);
            this.baseStats.put("attack",85);
            this.baseStats.put("defense",200);
            this.baseStats.put("specialAtk",55);
            this.baseStats.put("specialDef",65);
            this.baseStats.put("speed",30);
            this.baseStats.put("catchRate", 25);
            // sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/steelix.png"));
            this.sprite = new SpriteProxy(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); // my sprites are backwards

            this.types.add("Steel");
            this.types.add("Ground");
        }
        else if (name == "Sneasel") { // gen I properties
            this.baseStats.put("hp",55);
            this.baseStats.put("attack",95);
            this.baseStats.put("defense",55);
            this.baseStats.put("specialAtk",35);
            this.baseStats.put("specialDef",75);
            this.baseStats.put("speed",115);
            this.baseStats.put("catchRate", 60);
            // sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/sneasel.png"));
            this.sprite = new SpriteProxy(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false);

            this.types.add("Dark");
            this.types.add("Ice");
        }

        else if (name == "Suicune") { // gen II properties
            this.baseStats.put("hp",100);
            this.baseStats.put("attack",75);
            this.baseStats.put("defense",115);
            this.baseStats.put("specialAtk",90);
            this.baseStats.put("specialDef",115);
            this.baseStats.put("speed",85);
            this.baseStats.put("catchRate", 3);
            // sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/suicune.png"));
            this.sprite = new SpriteProxy(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); // my sprites are backwards

            this.types.add("Water");
        }
        else if (name == "Raikou") { // gen II properties
            this.baseStats.put("hp",90);
            this.baseStats.put("attack",85);
            this.baseStats.put("defense",75);
            this.baseStats.put("specialAtk",115);
            this.baseStats.put("specialDef",100);
            this.baseStats.put("speed",115);
            this.baseStats.put("catchRate", 3);
            // sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/raikou.png"));
            this.sprite = new SpriteProxy(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); // my sprites are backwards

            this.types.add("Electric");
        }
        else if (name == "Entei") { // gen II properties
            this.baseStats.put("hp",115);
            this.baseStats.put("attack",115);
            this.baseStats.put("defense",85);
            this.baseStats.put("specialAtk",90);
            this.baseStats.put("specialDef",75);
            this.baseStats.put("speed",100);
            this.baseStats.put("catchRate", 3);
            // sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/entei.png"));
            this.sprite = new SpriteProxy(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); // my sprites are backwards

            this.types.add("Electric");
        }
        else if (name == "Wurmple") { // gen IV properties
            this.baseStats.put("hp",45);
            this.baseStats.put("attack",45);
            this.baseStats.put("defense",35);
            this.baseStats.put("specialAtk",20);
            this.baseStats.put("specialDef",30);
            this.baseStats.put("speed",20);
            this.baseStats.put("catchRate", 255);
            // sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/wurmple.png"));
            this.sprite = new SpriteProxy(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false);

            this.types.add("Bug");
        }
        else if (name == "Makuhita") { // gen I properties
            this.baseStats.put("hp",72);
            this.baseStats.put("attack",60);
            this.baseStats.put("defense",30);
            this.baseStats.put("specialAtk",20);
            this.baseStats.put("specialDef",30);
            this.baseStats.put("speed",25);
            this.baseStats.put("catchRate", 180);
            // sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/makuhita.png"));
            this.sprite = new SpriteProxy(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); // my sprites are backwards

            this.types.add("Fighting");
        }

        else if (name == "Hariyama") { // gen I properties
            this.baseStats.put("hp",144);
            this.baseStats.put("attack",120);
            this.baseStats.put("defense",60);
            this.baseStats.put("specialAtk",40);
            this.baseStats.put("specialDef",60);
            this.baseStats.put("speed",50);
            this.baseStats.put("catchRate", 200);
            // sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/hariyama.png"));
            this.sprite = new SpriteProxy(pokemonText, 0, 0, 56, 56);
            // this.sprite.flip(true, false); // this one looks better flipped

            this.types.add("Fighting");
        }
        else if (name == "Skitty") { // gen I properties
            this.baseStats.put("hp",50);
            this.baseStats.put("attack",45);
            this.baseStats.put("defense",45);
            this.baseStats.put("specialAtk",35);
            this.baseStats.put("specialDef",35);
            this.baseStats.put("speed",50);
            this.baseStats.put("catchRate", 255);
            // sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/skitty.png"));
            this.sprite = new SpriteProxy(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); // this one looks better flipped

            this.types.add("Normal");
        }
        else if (name == "Sableye") { // gen I properties
            this.baseStats.put("hp",50);
            this.baseStats.put("attack",75);
            this.baseStats.put("defense",75);
            this.baseStats.put("specialAtk",65);
            this.baseStats.put("specialDef",65);
            this.baseStats.put("speed",50);
            this.baseStats.put("catchRate", 45);
            // sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/sableye.png"));
            this.sprite = new SpriteProxy(pokemonText, 0, 0, 56, 56);
            // this.sprite.flip(true, false); // this one looks better flipped

            this.types.add("Dark");
            this.types.add("Ghost");
        }
        else if (name == "Gardevoir") { // gen IV properties
            this.baseStats.put("hp",68);
            this.baseStats.put("attack",65);
            this.baseStats.put("defense",65);
            this.baseStats.put("specialAtk",125);
            this.baseStats.put("specialDef",115);
            this.baseStats.put("speed",80);
            this.baseStats.put("catchRate", 45);
            // sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/gardevoir.png"));
            this.sprite = new SpriteProxy(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); // this one looks better flipped

            this.types.add("Psychic");
            // this.types.add("Fairy"); // gen  IV doesn't include
        }
        else if (name == "Claydol") { // gen IV properties
            this.baseStats.put("hp",60);
            this.baseStats.put("attack",70);
            this.baseStats.put("defense",105);
            this.baseStats.put("specialAtk",70);
            this.baseStats.put("specialDef",120);
            this.baseStats.put("speed",75);
            this.baseStats.put("catchRate", 90);
            // sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/claydol.png"));
            this.sprite = new SpriteProxy(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); // this one looks better flipped

            this.types.add("Ground");
            this.types.add("Psychic");
            // this.types.add("Fairy"); // gen  IV doesn't include
        }
        else if (name == "Lairon") { // gen IV properties
            this.baseStats.put("hp",60);
            this.baseStats.put("attack",90);
            this.baseStats.put("defense",140);
            this.baseStats.put("specialAtk",50);
            this.baseStats.put("specialDef",50);
            this.baseStats.put("speed",40);
            this.baseStats.put("catchRate", 90);
            // sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/lairon.png"));
            this.sprite = new SpriteProxy(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false);

            this.types.add("Steel");
            this.types.add("Rock");
        }
        else if (name == "Cacnea") { // gen IV properties
            this.baseStats.put("hp",50);
            this.baseStats.put("attack",85);
            this.baseStats.put("defense",40);
            this.baseStats.put("specialAtk",85);
            this.baseStats.put("specialDef",40);
            this.baseStats.put("speed",35);
            this.baseStats.put("catchRate", 190);
            // sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/cacnea.png"));
            this.sprite = new SpriteProxy(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); // my sprites are backwards

            this.types.add("Grass");
        }
        else if (name == "Shuppet") { // gen IV properties
            this.baseStats.put("hp",44);
            this.baseStats.put("attack",75);
            this.baseStats.put("defense",35);
            this.baseStats.put("specialAtk",63);
            this.baseStats.put("specialDef",33);
            this.baseStats.put("speed",45);
            this.baseStats.put("catchRate", 225);
            // sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/shuppet.png"));
            this.sprite = new SpriteProxy(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); // my sprites are backwards

            this.types.add("Grass");
        }
        else if (name == "Starly") { // gen IV properties
            this.baseStats.put("hp",40);
            this.baseStats.put("attack",55);
            this.baseStats.put("defense",30);
            this.baseStats.put("specialAtk",30);
            this.baseStats.put("specialDef",30);
            this.baseStats.put("speed",60);
            this.baseStats.put("catchRate", 255);
            // sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/starly.png"));
            this.sprite = new SpriteProxy(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false);

            this.types.add("Normal");
            this.types.add("Flying");
        }
        else if (name == "Shinx") { // gen IV properties
            this.baseStats.put("hp",45);
            this.baseStats.put("attack",65);
            this.baseStats.put("defense",34);
            this.baseStats.put("specialAtk",40);
            this.baseStats.put("specialDef",34);
            this.baseStats.put("speed",45);
            this.baseStats.put("catchRate", 235);
            // sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/shinx.png"));
            this.sprite = new SpriteProxy(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false);

            this.types.add("Electric");
        }
        // TODO: remove
//        else if (name == "Machop") { // todo: stats are wrong
//            this.baseStats.put("hp",45);
//            this.baseStats.put("attack",65);
//            this.baseStats.put("defense",34);
//            this.baseStats.put("specialAtk",40);
//            this.baseStats.put("specialDef",34);
//            this.baseStats.put("speed",45);
//            this.baseStats.put("catchRate", 235);
//            // sprite
//            Texture pokemonText = new Texture(Gdx.files.internal("crystal_pokemon/machop_front2.png"));
//            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
////            this.sprite.flip(true, false);
//
//            this.types.add("Fighting");
//
//            this.introAnim = new ArrayList<Sprite>();
//            // 23 frames do nothing
//            Sprite sprite = new Sprite(pokemonText, 56*0, 0, 56, 56);
////            sprite.flip(true, false);
//            for (int i=0; i < 24; i++) {
//                this.introAnim.add(sprite);
//            }
//            // 22 frames mouth open
//            sprite = new Sprite(pokemonText, 56*2, 0, 56, 56);
////            sprite.flip(true, false);
//            for (int i=0; i < 23; i++) {
//                this.introAnim.add(sprite);
//            }
//            // 13 frames normal
//            sprite = new Sprite(pokemonText, 56*0, 0, 56, 56);
////            sprite.flip(true, false);
//            for (int i=0; i < 14; i++) {
//                this.introAnim.add(sprite);
//            }
//            // 12 frames fists up
//            sprite = new Sprite(pokemonText, 56*1, 0, 56, 56);
////            sprite.flip(true, false);
//            for (int i=0; i < 13; i++) {
//                this.introAnim.add(sprite);
//            }
//            // 13 frames normal
//            sprite = new Sprite(pokemonText, 56*0, 0, 56, 56);
////            sprite.flip(true, false);
//            for (int i=0; i < 14; i++) {
//                this.introAnim.add(sprite);
//            }
//            // 13 frames fists up
//            sprite = new Sprite(pokemonText, 56*1, 0, 56, 56);
////            sprite.flip(true, false);
//            for (int i=0; i < 14; i++) {
//                this.introAnim.add(sprite);
//            }
//            // 11 frames normal
//            sprite = new Sprite(pokemonText, 56*0, 0, 56, 56);
////            sprite.flip(true, false);
//            for (int i=0; i < 14; i++) {
//                this.introAnim.add(sprite);
//            }
//        }
        else {
            return;
        }

        getCurrentAttacks(); // fill this.attacks with what it can currently know
        this.exp = gen2CalcExpForLevel(this.level);

        // stats formulas here
        calcMaxStats();
        this.currentStats = new HashMap<String, Integer>(this.maxStats); // copy maxStats
        this.initHabitatValues();
    }

    // TODO - this doesn't take IV's or EV's into account.
     // for EV's - I think they only get factored in on pokemon level up. So only call calcMaxStats on level up.
     // if you ever need to reset currentStats, just make a copy of maxStats - like after battle, mist attack, etc
    void calcMaxStats() {
        this.maxStats.put("hp", (((this.baseStats.get("hp") + 50) * this.level) / 50) + 10);
        this.maxStats.put("attack", (((this.baseStats.get("attack")) * this.level) / 50) + 10);
        this.maxStats.put("defense", (((this.baseStats.get("defense")) * this.level) / 50) + 10);
        this.maxStats.put("specialAtk", (((this.baseStats.get("specialAtk")) * this.level) / 50) + 10);
        this.maxStats.put("specialDef", (((this.baseStats.get("specialDef")) * this.level) / 50) + 10);
        this.maxStats.put("speed", (((this.baseStats.get("speed")) * this.level) / 50) + 10);

        // catchRate for the sake of including everything
        this.maxStats.put("catchRate", this.baseStats.get("catchRate"));
        
        // TODO: remove
        // egg has 0 hp, which makes you unable to use it in battle
        // and you can't drop it unless there is another healthy
        // pokemon in your party.
//        if (this.name.equals("egg")) {
//            this.maxStats.put("hp", 0);
//        }

        // hp = (((IV + Base + (sqrt(EV)/8) + 50)*Level)/50 + 10
        // other stat = (((IV + Base + (sqrt(EV)/8))*Level)/50 + 5
    }

    /**
     * Determine if pokemon near farm structures and habitat.
     */
    void checkHabitat(Game game) {
        Pokemon prevLoveInterest = this.loveInterest;
        if (this.loveInterest != null) {
            this.loveInterest.loveInterest = null;
        }
        this.loveInterest = null;
        Vector2 startPos = Pokemon.this.position.cpy().add(-16*3, -16*3);
        startPos.x = (int)startPos.x - (int)startPos.x % 16;
        startPos.y = (int)startPos.y - (int)startPos.y % 16;
        Vector2 endPos = Pokemon.this.position.cpy().add(16*3, 16*3);
        endPos.x = (int)endPos.x - (int)endPos.x % 16;
        endPos.y = (int)endPos.y - (int)endPos.y % 16;
        int fenceCount = 0;
        int roofCount = 0;
        this.nearbyEggs = 0;
//        int habitatCount = 0;
        ArrayList<String> notFoundHabitats = new ArrayList<String>(Pokemon.this.habitats);
        for (Vector2 currPos = new Vector2(startPos.x, startPos.y); currPos.y < endPos.y;) {
//            Tile tile = game.map.tiles.get(currPos);  // TODO: remove
            Tile tile = Pokemon.this.mapTiles.get(currPos);
            currPos.x += 16;
            if (currPos.x > endPos.x) {
                currPos.x = startPos.x;
                currPos.y += 16;
            }
            if (tile == null) {
                continue;
            }
            // If found the player and aggro-ing, then set player.nearAggroPokemon = true
            if (game.map.tiles == Pokemon.this.mapTiles &&
                game.player.position.equals(currPos) &&
                this.aggroPlayer) {
                game.player.nearAggroPokemon = true;
            }
            if (tile.nameUpper.contains("fence")) {
                fenceCount++; 
            }
            else if (tile.nameUpper.contains("roof")) {
                roofCount++;
            }
            // Dual-types require multiple habitats.
            for (String habitat : Pokemon.this.habitats) {
                // | is used to basically say either-or
                for (String name : habitat.split("\\|")) {
                    if (tile.name.contains(name) || tile.nameUpper.contains(name)) {
                        notFoundHabitats.remove(habitat);
                        break;
                    }
                }
            }
            if (game.map.pokemon.containsKey(currPos)) {
                Pokemon pokemon = game.map.pokemon.get(currPos);
                if (pokemon != this &&
                    pokemon.mapTiles == this.mapTiles) {
//                    pokemon.name.equals("egg")) {  // TODO: just counting any pokemon in general
                    this.nearbyEggs++;
                }
            }

            // Check for potential 'mates' (pokemon breeding mechanic)
            if (!this.name.equals("egg") &&
                this.loveInterest == null &&
                game.map.pokemon.containsKey(currPos) &&
                game.map.pokemon.get(currPos) != this &&
                game.map.pokemon.get(currPos).mapTiles == this.mapTiles) {
                Pokemon potentialMate = game.map.pokemon.get(currPos);
                // oppGender handles the 'no gender' case as well
                String oppGender = this.gender.equals("male") ? "female" : "male";
                boolean genderCompatible = potentialMate.gender.equals(oppGender);
                // TODO: what are the rules for genderless pokemon? (gender = "", not gender = null i think)
//                boolean compatible = !this.gender.equals("") && potentialMate.gender.equals(oppGender);
                boolean sameEggGroup = false;
                for (String group1 : this.eggGroups) {
                    for (String group2 : potentialMate.eggGroups) {
                        if (group1.equals("EGG_NONE") || group2.equals("EGG_NONE")) {
                            sameEggGroup = false;
                            break;
                        }
                        if (group1.equals("EGG_DITTO") || group2.equals("EGG_DITTO")) {
                            genderCompatible = true;
                            sameEggGroup = true;
                            break;
                        }
                        if (group1.equals(group2)) {
                            sameEggGroup = true;
                            break;
                        }
                    }
                }
                if (!potentialMate.name.equals("egg") &&
                    genderCompatible && sameEggGroup &&
                    potentialMate.loveInterest == null) {
                    this.loveInterest = potentialMate;
                    potentialMate.loveInterest = this;
                    // Heart emote to show that they love each other
                    if (potentialMate != prevLoveInterest) {
                        if (!potentialMate.aggroPlayer) {
                            game.insertAction(potentialMate.new Emote("heart", null));
                        }
                        if (!this.aggroPlayer) {
                            game.insertAction(this.new Emote("heart", null));
                        }
                    }
                }
            }
        }
        // Dark types are only happy at night.
        if (Pokemon.this.types.contains("DARK") && !game.map.timeOfDay.equals("night")) {
//            habitatCount = 0;  // TODO: remove
            notFoundHabitats.add("night");
        }
//        Pokemon.this.inHabitat = habitatCount >= Pokemon.this.habitats.size();  // TODO: remove
        Pokemon.this.inHabitat = notFoundHabitats.size() <= 0;
        Pokemon.this.inShelter = roofCount >= 3 && fenceCount >= 2;
    }

    /**
     * Compute changes required by leveling up.
     */
    void gainLevel(int numLevels) {
        this.level += numLevels;
        this.calcMaxStats();
        // TODO: remove when getting rid of currentStats.
//        this.currentStats = new HashMap<String, Integer>(this.maxStats);
        for (String stat : this.maxStats.keySet()) {
            if (stat.equals("hp")) {
                continue;
            }
            this.currentStats.put(stat, this.maxStats.get(stat));
        }
    }

    /**
     * Compute changes required by evolution.
     */
    void evolveTo(String targetName) {
        this.name = targetName;
        this.dexNumber = Pokemon.nameToIndex(this.name);
        // Update base stats and various values.
        // Don't modify attacks, current hp, etc.
        // TODO: current hp is probably compensated in the real game
        if (Pokemon.nuukPokemon.contains(this.name.toLowerCase()) ||
            (Integer.valueOf(this.dexNumber) <= 251 && Integer.valueOf(this.dexNumber) > 0)) {
            this.loadCrystalPokemon(this.name);
        }
        else {
            this.loadPrismPokemon(this.name);
        }
        this.standingSprites.clear();
        this.movingSprites.clear();
        this.altMovingSprites.clear();
        this.avatarSprites.clear();
        this.loadOverworldSprites(this.name);
        this.calcMaxStats();
        // restore hp to full
        this.currentStats.put("hp", this.maxStats.get("hp"));

        // TODO: probably move this to function somewhere
        this.hms.clear();
        if (this.types.contains("FIGHTING")) {
            this.hms.add("BUILD");
        }
        if (name.equals("hypno") ||
            name.equals("nidorina") ||
            name.equals("nidoqueen") ||
            name.equals("nidorino") ||
            name.equals("nidoking") ||
            name.equals("granbull") ||
            name.equals("jynx") ||
            name.equals("snorlax") ||
            name.equals("ursaring")) {
            this.hms.add("HEADBUTT");
        }
        if (name.equals("sneasel") || 
            name.equals("scyther") ||
            name.equals("pinsir")) {
            this.hms.add("CUT");
        }
        if (name.equals("stantler") ||
            name.equals("ponyta") ||
            name.equals("arcanine") ||
            name.equals("donphan") ||
            name.equals("girafarig") ||
            name.equals("houndoom") ||
            name.equals("rapidash") ||
            name.equals("tauros") ||
            name.equals("ninetales") ||
            name.equals("mamoswine") ||
            name.equals("mightyena") ||
            //
            name.equals("persian") ||
            name.equals("onix") ||
            name.equals("steelix") ||
            name.equals("haunter") ||
            name.equals("rhyhorn") ||
            name.equals("rhydon") ||
            //
            name.equals("dodrio") ||
            name.equals("luxray")) {
            // TODO: change to 'RIDE' later. Making it 'JUMP' for now so that it's not confusing.
            // Later, once there (hopefully) are riding sprites, this can be changed to ride.
            // My current idea is that RIDE increases movement speed and can perform jumps up ledges.
            this.hms.add("RIDE");
        }
        if (name.equals("pidgeot") ||
            name.equals("aerodactyl") ||
            name.equals("charizard") ||
            name.equals("dragonair") ||
            name.equals("dragonite") ||
            name.equals("salamence") ||
            name.equals("ho_oh") ||
            name.equals("lugia") ||
            name.equals("skarmory") ||
            name.equals("articuno") ||
            name.equals("zapdos") ||
            name.equals("moltres") ||
            name.equals("crobat") ||
            name.equals("noctowl") ||
            name.equals("xatu") ||
            name.equals("flygon") ||
            name.equals("togekiss") ||
            name.equals("swellow") ||
            name.equals("pelipper") ||
            name.equals("altaria") ||
            name.equals("rayquaza") ||
//            name.equals("farfetch_d") ||  // TODO: removed
            name.equals("drifblim") ||
            name.equals("honchkrow") ||
            name.equals("yanmega") ||
            name.equals("fearow")) {
            this.hms.add("FLY");
        }
        // TODO: for now, all grass types can cut
        if (this.types.contains("GRASS")) {
            this.hms.add("CUT");
        }
        if (this.types.contains("ROCK")) {
            this.hms.add("SMASH");
        }
        if (this.types.contains("FIRE") ||
            name.equals("chinchou") ||
            name.equals("lanturn") ||
            name.equals("mareep") ||
            name.equals("flaaffy") ||
            name.equals("ampharos")) {
            // Calling it FLASH for now, since that's what most people
            // are familiar with.
            this.hms.add("FLASH");  
        }
        if (this.types.contains("DARK") && !this.hms.contains("RIDE")) {
            this.hms.add("ATTACK");
        }
        this.initHabitatValues();
    }

    /**
     * .
     */
    void initHabitatValues() {
        String name = this.name.toLowerCase();
        this.harvestables.clear();
        this.habitats.clear();
        // Type-specific
        if (this.types.contains("BUG")) {
            this.habitats.add("flower");
            this.harvestables.add("silky thread");
        }
        if (this.types.contains("WATER")) {
            this.habitats.add("water");
            this.harvestables.add("hard shell");
        }
        if (this.types.contains("FLYING")) {
            this.habitats.add("tree");
            this.harvestables.add("soft feather");
        }
        if (this.types.contains("ROCK")) {
            this.habitats.add("rock");
            this.harvestables.add("hard stone");
        }
        if (this.types.contains("FIRE")) {
            this.habitats.add("campfire");
            this.harvestables.add("charcoal");
        }
        // GRASS, grass, grass
        if (this.types.contains("GRASS")) {
            this.habitats.add("grass");
            // TODO: test changes
//            this.harvestables.add("grass");
//            this.harvestables.add("grass");
            this.harvestables.add("miracle seed");
        }
        if (this.types.contains("GROUND")) {
            this.habitats.add("mountain|sand");
//            this.habitats.add("sand");  // not sure. probably needs to be either-or.
//            this.harvestables.add("soft clay");
            // used to make 'glass', required for silph scope
            // for now.
            this.harvestables.add("soft sand");
        }
        if (this.types.contains("STEEL")) {
            this.habitats.add("mountain");
            this.harvestables.add("metal coat");
        }
        if (this.types.contains("PSYCHIC")) {
            this.harvestables.add("psi energy");
        }
        if (this.types.contains("DARK")) {
            this.harvestables.add("dark energy");
        }
        if (this.types.contains("GHOST")) {
            this.harvestables.add("spell tag");
        }
        if (this.types.contains("DRAGON")) {
            this.habitats.clear();
            this.habitats.add("water");  // TODO: potentially change for other dragon types
            this.harvestables.clear();  // TODO: disable this line once it's easier to get dragon types.
            this.harvestables.add("dragon fang");
            this.harvestables.add("dragon scale");
            this.harvestables.add("dragon scale");
            this.harvestables.add("dragon scale");
            this.harvestables.add("dragon scale");
        }
        if (this.types.contains("ICE")) {
            this.habitats.add("snow");  // TODO: this originally wasn't here, why?
            this.harvestables.add("nevermeltice");
        }
        if (this.types.contains("ELECTRIC")) {
            this.harvestables.add("magnet");
        }
        if (name.equals("mareep") || name.equals("flaaffy") || name.equals("ampharos")) {
            this.habitats.clear();
            this.habitats.add("grass");
            this.harvestables.clear();
            this.harvestables.add("soft wool");
        }
        // TODO: enable if used
//        else if (name.equals("beedrill") || name.equals("butterfree")) {
//            this.harvestables.clear();
//            this.harvestables.add("sweet nectar");
//        }
        else if (name.equals("miltank")) {
            this.harvestables.clear();
            this.harvestables.add("moomoo milk");
        }
        // Current idea is this can drop any item
        else if (name.equals("delibird")) {
            this.harvestables.clear();
            this.harvestables.add("ancientpowder");
            this.harvestables.add("nevermeltice");
            this.harvestables.add("magnet");
            this.harvestables.add("ancientpowder");
            this.harvestables.add("soft wool");
            this.harvestables.add("moomoo milk");
            this.harvestables.add("hard stone");
            this.harvestables.add("manure");
            this.harvestables.add("berry juice");
            this.harvestables.add("soft sand");
            this.harvestables.add("nevermeltice");
            this.harvestables.add("spell tag");
            this.harvestables.add("dragon fang");
            this.harvestables.add("dragon scale");
            this.harvestables.add("spell tag");
            this.harvestables.add("psi energy");
            this.harvestables.add("dark energy");
            this.harvestables.add("metal coat");
            this.harvestables.add("silky thread");
            this.harvestables.add("hard shell");
            this.harvestables.add("soft feather");
            this.harvestables.add("hard stone");
            this.harvestables.add("charcoal");
            this.harvestables.add("grass");
        }
        else if (name.contains("unown")) {
            this.harvestables.clear();
            this.harvestables.add("ancientpowder");
            this.harvestTimerMax = (int)(3600f*3.5f);
        }
        else if (name.contains("regi")) {
            this.harvestables.add("ancientpowder");
        }
        // just ideas
//        else if (name.equals("slowpoke")) {
//            this.harvestables.clear();
//            this.harvestables.add("slowpoketail");
//        }
//        else if (this.isShiny && (name.equals("onyx") || name.equals("steelix"))) {
//            this.harvestables.clear();
//            this.harvestables.add("gold nugget");
//        }
        else if (name.equals("shuckle")) {
            this.harvestables.clear();
            this.harvestables.add("berry juice");
        }
        // Fill in with defaults
        if (this.habitats.size() <= 0) {
            this.habitats.add("green");
        }
        if (this.harvestables.size() <= 0) {
            this.harvestables.add("manure");
        }
    }

    /**
     * Get position that pokemon is facing towards.
     */
    public Vector2 facingPos() {
        return this.facingPos(this.dirFacing);
    }

    /**
     * Get position that pokemon is facing towards.
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

    /** Attempt to apply stat change during battle.
     * Source - https://www.dragonflycave.com/mechanics/stat-stages
     * Also - https://web.archive.org/web/20140712063943/http://www.upokecenter.com/content/pokemon-gold-version-silver-version-and-crystal-version-timing-notes
     * 
     * return false if stat stage is already at max or min value.
     */
    boolean gen2ApplyStatStage(String stat, int stage) {
        return this.gen2ApplyStatStage(stat, stage, false);
    }

    boolean gen2ApplyStatStage(String stat, int stage, boolean override) {
        int newStage = this.statStages.get(stat)+stage;
        if (!override && (newStage < -6 || newStage > 6)) {
            // Would result in a stat stage that is too high/low, so fail.
            return false;
        }
        this.statStages.put(stat, newStage);
        // Modify currentStats to use the correct value.
        float multiplier;
        if (stat.equals("accuracy") || stat.equals("evasion")) {
            // Unique to gen 2; gen 1 uses same formula for all stats
//            multiplier = (float)Math.max(3, 3 + newStage)/(float)Math.max(3, 3 - newStage);
            return true;
        }
        multiplier = (float)Math.max(2, 2 + newStage)/(float)Math.max(2, 2 - newStage);
        int currStat = this.maxStats.get(stat);
        // Handle stat changes caused by status
        if (this.status != null) {
            if (stat.equals("speed") && this.status.equals("paralyze")) {
                currStat = currStat/4;
            }
            else if (stat.equals("attack") && this.status.equals("burn")) {
                currStat = currStat/4;
            }
        }
        int newStat = (int)(multiplier*currStat);
        // This is a Gen 2 mechanic - stat value is capped at 999.
        if (newStat < 1) {
            newStat = 1;
        }
        if (newStat > 999) {
            newStat = 999;
        }
        this.currentStats.put(stat, newStat);
        return true;
    }

    /*
     * Calculate exp based on current level (gen 2 formula).
     */
    int gen2CalcExpForLevel(int level) {
        // Have to use contains() for compatibility with prism pokemon, which omit the 'GROWTH' from the beginning.
        if (this.growthRateGroup.contains("FAST")) {
            return (4*(level*level*level))/5;
        }
        else if (this.growthRateGroup.contains("MEDIUM_FAST")) {
            return level*level*level;
        }
        else if (this.growthRateGroup.contains("MEDIUM_SLOW")) {
            return (((6*(level*level*level))/5) - (15*(level*level)) + (100*level) - 140);
        }
        else if (this.growthRateGroup.contains("SLOW")) {
            return (5*(level*level*level))/4;
        }
//        System.out.println("Error: invalid growth group for " + this.name + ", group: "+String.valueOf(this.growthRateGroup));
        return level*level*level;  // just default to medium fast
    }

    /**
     * When generating a pokemon, this will select which attacks it knows by default
     */
    void getCurrentAttacks() {
        int i = 0;
        for (Integer level : this.learnSet.keySet()) {
            for (String attack : this.learnSet.get(level)) {
                // TODO: debug, remove
//                if (attack.contains("whirlwind") && this.name.contains("pidgeot")) {
//                    System.out.println(this.learnSet.get(level));
//                }
                if (level <= this.level) {
                    this.attacks[i] = attack;
                    i += 1;
                    if (i >= this.attacks.length) {
                        i = 0;
                    }
                }
            }
        }
    }

    /**
     * Load pokemon sprites and data from crystal_pokemon files.
     */
    void loadCrystalPokemon(String name) {
        name = name.toLowerCase();

        String newName = name;
        if (name.contains("unown")) {
            newName = "unown";
        }
        if (name.equals("egg")) {
            newName = this.eggHatchInto;
        }
        String path = "";
        if (Pokemon.nuukPokemon.contains(newName)) {
            path = "nuuk/";
        }

        // Load base stats
        try {
            FileHandle file = Gdx.files.internal("crystal_pokemon/"+path+"base_stats/" + newName + ".asm");
            Reader reader = file.reader();
            BufferedReader br = new BufferedReader(reader);
            String line;
            int lineNum = 0;
            while ((line = br.readLine()) != null)   {
                // TODO: using table to look up number now
//                if (lineNum == 0) {
//                    this.dexNumber = line.split(" ; ")[1];
//                } else
                if (lineNum == 2) {
                    String stats[] = line.split("db")[1].split(",");
                    this.baseStats.put("hp", Integer.valueOf((stats[0].replace(" ", ""))));
                    this.baseStats.put("attack", Integer.valueOf((stats[1].replace(" ", ""))));
                    this.baseStats.put("defense", Integer.valueOf((stats[2].replace(" ", ""))));
                    this.baseStats.put("speed", Integer.valueOf((stats[3].replace(" ", ""))));
                    this.baseStats.put("specialAtk", Integer.valueOf((stats[4].replace(" ", ""))));
                    this.baseStats.put("specialDef", Integer.valueOf((stats[5].replace(" ", ""))));
                } else if (lineNum == 5) {
                    String types[] = line.split("db ")[1].split(" ; ")[0].split(", ");
                    this.types.add(types[0]);
                    this.types.add(types[1]);
                } else if (lineNum == 6) {
                    String catchRate = line.split("db ")[1].split(" ;")[0];
                    this.baseStats.put("catchRate", Integer.valueOf(catchRate));
                } else if (lineNum == 7) {
                    String baseExp = line.split("db ")[1].split(" ;")[0];
                    this.baseStats.put("baseExp", Integer.valueOf(baseExp));
                // If gender not assigned yet, assign it here based on gender ratio
                } else if (lineNum == 9) {
                    // source: https://github.com/pret/pokecrystal/wiki/Add-a-new-Pok%C3%A9mon
                    // GENDER_F0: 100% male
                    // GENDER_F12_5: 7/8 male, 1/8 female
                    // GENDER_F25: 3/4 male, 1/4 female
                    // GENDER_F50: 1/2 male, 1/2 female
                    // GENDER_F75: 1/4 male, 3/4 female
                    // GENDER_F100: 100% female
                    // GENDER_UNKNOWN: genderless
                    String genderRatio = line.split("db ")[1].split(" ;")[0];
                    if (genderRatio.equals("GENDER_UNKNOWN")) {
                        this.gender = "unknown";
                    }
                    else {
                        int percentFemale = 0;
                        if (genderRatio.equals("GENDER_F12_5")) {
                            percentFemale = 125;
                        }
                        else if (genderRatio.equals("GENDER_F25")) {
                            percentFemale = 250;
                        }
                        else if (genderRatio.equals("GENDER_F50")) {
                            percentFemale = 500;
                        }
                        else if (genderRatio.equals("GENDER_F75")) {
                            percentFemale = 750;
                        }
                        else if (genderRatio.equals("GENDER_F100")) {
                            percentFemale = 1000;
                        }
                        if (Pokemon.rand.nextInt(1000) < percentFemale) {
                            this.gender = "female";
                        }
                        else {
                            this.gender = "male";
                        }
                    }
                // Egg cycles to hatch
                } else if (lineNum == 11 && this.name.equals("egg")) {
                    String eggCycles = line.split("db ")[1].split(" ;")[0];
                    this.happiness = Integer.valueOf(eggCycles);
                } else if (lineNum == 15) {
                    this.growthRateGroup = line.split("db ")[1].split(" ;")[0];
                // Egg groups this pokemon belongs to
                } else if (lineNum == 16) {
                    String groups = line.split("dn ")[1].split(" ;")[0];
                    this.eggGroups = groups.split(", ");
                }
                // TODO: other stats
                lineNum++;
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (name.equals("egg")) {
            path = "";  // all egg sprites loaded from crystal
        }

        // Load sprite and animation data (cached)
        if (!Pokemon.textures.containsKey(name+"_front")) {
            // Load front sprite
            Texture text = TextureCache.get(Gdx.files.internal("crystal_pokemon/"+path+"pokemon/" + name + "/front.png"));
            // unown sprites have color data only stored in one channel (alpha)
            // convert this to regular texture
            if (name.contains("unown")) {
                TextureData temp = text.getTextureData();
                if (!temp.isPrepared()) {
                    temp.prepare();
                }
                Pixmap currPixmap = temp.consumePixmap();
                Pixmap newPixmap = new Pixmap(text.getWidth(), text.getHeight(), Pixmap.Format.RGBA8888);
                newPixmap.setColor(new Color(0, 0, 0, 0));
                newPixmap.fill();
                for (int i=0, j=0; j < text.getHeight(); i++) {
                    if (i > text.getWidth()) {
                        i=-1;
                        j++;
                        continue;
                    }
                    Color color = new Color(currPixmap.getPixel(i, j));
                    newPixmap.drawPixel(i, j, Color.rgba8888(color.a, color.a, color.a, 1f));
                }
                text = new Texture(newPixmap);
            }
            Pokemon.textures.put(name+"_front", text);

            // Swap palette for shiny texture (load by appending '_shiny' to the texture name)
            // Load shiny palette from file
            Color normalColor1 = null;
            Color normalColor2 = new Color();
            Color shinyColor1 = null;
            Color shinyColor2 = new Color();
            // TODO: this is just a hack workaround
            if (Pokemon.nuukPokemon.contains(newName)) {
                path = "nuuk/";
            }
            try {
                FileHandle file = Gdx.files.internal("crystal_pokemon/"+path+"pokemon/" + newName + "/shiny.pal");
                Reader reader = file.reader();
                BufferedReader br = new BufferedReader(reader);
                String line;
                while ((line = br.readLine()) != null)   {
                    if (line.contains("RGB")) {
                        String[] vals = line.split("\tRGB ")[1].split(", ");
                        if (shinyColor1 == null) {
                            shinyColor1 = new Color();
                            shinyColor1.r = (Float.valueOf(vals[0])*8f)/256f;
                            shinyColor1.g = (Float.valueOf(vals[1])*8f)/256f;
                            shinyColor1.b = (Float.valueOf(vals[2])*8f)/256f;
                        }
                        else {
                            shinyColor2.r = (Float.valueOf(vals[0])*8f)/256f;
                            shinyColor2.g = (Float.valueOf(vals[1])*8f)/256f;
                            shinyColor2.b = (Float.valueOf(vals[2])*8f)/256f;
                        }
                    }
                }
                reader.close();

                // TODO: hack workaround
                if (name.equals("egg")) {
                    path = "";  // all egg sprites loaded from crystal
                }

                if (name.contains("unown")) {
                    file = Gdx.files.internal("crystal_pokemon/"+path+"pokemon/" + newName + "/normal.pal");
                }
                else {
                    file = Gdx.files.internal("crystal_pokemon/"+path+"pokemon/" + name + "/front.pal");
                }
                // TODO: don't really need anymore b/c SpriteProxy knows color1/color2
                if (file.exists()) {
                    reader = file.reader();
                    br = new BufferedReader(reader);
                    while ((line = br.readLine()) != null)   {
                        if (line.contains("RGB")) {
                            String[] vals = line.split("\tRGB ")[1].split(", ");
                            if (normalColor1 == null) {
                                // TODO: this is wrong, these values range from 0-32 but .r .g .b should be floats.
                                normalColor1 = new Color();
                                normalColor1.r = Integer.valueOf(vals[0]);
                                normalColor1.g = Integer.valueOf(vals[1]);
                                normalColor1.b = Integer.valueOf(vals[2]);
                            }
                            else {
                                normalColor2.r = Integer.valueOf(vals[0]);
                                normalColor2.g = Integer.valueOf(vals[1]);
                                normalColor2.b = Integer.valueOf(vals[2]);
                            }
                        }
                    }
                    reader.close();
                }
                else {
                    SpriteProxy tempSprite = new SpriteProxy(Pokemon.textures.get(name+"_front"),
                                                             0, 0, text.getWidth(), text.getWidth());
//                    normalColor1 = tempSprite.color1;
//                    normalColor2 = tempSprite.color2;
                    // TODO: these should be floats from 0-1, not from 0-32
                    normalColor1 = new Color();
                    normalColor1.r = tempSprite.color1.r*32f;
                    normalColor1.g = tempSprite.color1.g*32f;
                    normalColor1.b = tempSprite.color1.b*32f;
                    normalColor2 = new Color();
                    normalColor2.r = tempSprite.color2.r*32f;
                    normalColor2.g = tempSprite.color2.g*32f;
                    normalColor2.b = tempSprite.color2.b*32f;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            TextureData temp = text.getTextureData();
            if (!temp.isPrepared()) {
                temp.prepare();
            }
            Pixmap currPixmap = temp.consumePixmap();
            Pixmap newPixmap = new Pixmap(text.getWidth(), text.getHeight(), Pixmap.Format.RGBA8888);
            newPixmap.setColor(new Color(0, 0, 0, 0));
            newPixmap.fill();
            for (int i=0, j=0; j < text.getHeight(); i++) {
                if (i > text.getWidth()) {
                    i=-1;
                    j++;
                    continue;
                }
                Color color = new Color(currPixmap.getPixel(i, j));
                if ((int)(color.r*32) == (int)normalColor1.r && (int)(color.g*32) == (int)normalColor1.g && (int)(color.b*32) == (int)normalColor1.b) {
                    color = shinyColor1;
                }
                else if ((int)(color.r*32) == (int)normalColor2.r && (int)(color.g*32) == (int)normalColor2.g && (int)(color.b*32) == (int)normalColor2.b) {
                    color = shinyColor2;
                }
                newPixmap.drawPixel(i, j, Color.rgba8888(color.r, color.g, color.b, 1f));
            }
            text = TextureCache.get(newPixmap);
            Pokemon.textures.put(name+"_front_shiny", text);

            // back sprites
            text = TextureCache.get(Gdx.files.internal("crystal_pokemon/"+path+"pokemon/" + name + "/back.png"));
            Pokemon.textures.put(name+"_back", text);
            temp = text.getTextureData();
            if (!temp.isPrepared()) {
                temp.prepare();
            }
            currPixmap = temp.consumePixmap();
            newPixmap = new Pixmap(text.getWidth(), text.getHeight(), Pixmap.Format.RGBA8888);
            newPixmap.setColor(new Color(0, 0, 0, 0));
            newPixmap.fill();
            for (int i=0, j=0; j < text.getHeight(); i++) {
                if (i > text.getWidth()) {
                    i=-1;
                    j++;
                    continue;
                }
                Color color = new Color(currPixmap.getPixel(i, j));
                if ((int)(color.r*32) == (int)normalColor1.r && (int)(color.g*32) == (int)normalColor1.g && (int)(color.b*32) == (int)normalColor1.b) {
                    color = shinyColor1;
                }
                else if ((int)(color.r*32) == (int)normalColor2.r && (int)(color.g*32) == (int)normalColor2.g && (int)(color.b*32) == (int)normalColor2.b) {
                    color = shinyColor2;
                }
                newPixmap.drawPixel(i, j, Color.rgba8888(color.r, color.g, color.b, 1f));
            }
            text = TextureCache.get(newPixmap);
            Pokemon.textures.put(name+"_back_shiny", text);
        }
        String isShiny = "";
        if (this.isShiny) {
            isShiny = "_shiny";
        }
        Texture pokemonText = Pokemon.textures.get(name+"_front"+isShiny);
        // height and width are the same for these sprites
        int height = pokemonText.getWidth();
//        this.sprite = new Sprite(pokemonText, 0, 0, height, height);  // TODO: test
        this.sprite = new SpriteProxy(pokemonText, 0, 0, height, height);

        // TODO: remove
//        if (!Pokemon.textures.containsKey(name+"_back")) {
//            Texture text = new Texture(Gdx.files.internal("crystal_pokemon/pokemon/" + name + "/back.png"));
//            Pokemon.textures.put(name+"_back", text);
//        }
//      pokemonText = new Texture(Gdx.files.internal("crystal_pokemon/pokemon/" + name + "/back.png"));
        pokemonText = Pokemon.textures.get(name+"_back"+isShiny);
//        height = pokemonText.getWidth();

//        this.backSprite = new Sprite(pokemonText, 0, 0, 48, 48); // TODO: test
        this.backSprite = new SpriteProxy(pokemonText, 0, 0, 48, 48);

        // Load animation from file
        this.introAnim = new ArrayList<SpriteProxy>();
        try {
            FileHandle file = Gdx.files.internal("crystal_pokemon/"+path+"pokemon/" + name + "/anim.asm");
            Reader reader = file.reader();
            BufferedReader br = new BufferedReader(reader);
            String line;
            int setrepeat = 0;
            ArrayList<String> lines = new ArrayList<String>();
            while ((line = br.readLine()) != null)   {
                lines.add(line);
            }
            for (int i=0; i < lines.size(); ) {
                line = lines.get(i);
                if (line.contains("setrepeat")) {
                    setrepeat = Integer.valueOf(line.split("setrepeat ")[1]);
                } else if (line.contains("frame")) {
                    String vals[] = line.split("frame ")[1].split(", ");
                    int numFrames = Integer.valueOf(vals[1].trim());
                    int frame = Integer.valueOf(vals[0]);
                    for (int j=0; j < numFrames; j++) {
//                        pokemonText = new Texture(Gdx.files.internal("crystal_pokemon/pokemon/" + name + "/front.png"));
                        pokemonText = Pokemon.textures.get(name+"_front"+isShiny);
                        SpriteProxy sprite = new SpriteProxy(pokemonText, 0, height*frame, height, height);
                        this.introAnim.add(sprite);
                    }
                } else if (line.contains("dorepeat")) {
                    if (setrepeat != 0) {
                        i = Integer.valueOf(line.split("dorepeat ")[1]);
                        setrepeat--;
                        continue;
                    }
                }
                i++;
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // TODO: this is just a hack workaround
        if (Pokemon.nuukPokemon.contains(newName)) {
            path = "nuuk/";
        }

        // Load attacks from file
        // TODO: just load all of this statically in static {} block (?)
        if (!Pokemon.gen2Attacks.containsKey(newName) || !Pokemon.gen2Evos.containsKey(newName)) {
            Map<Integer, String[]> attacks = new HashMap<Integer, String[]>();
            Map<String, String> evos = new HashMap<String, String>();
            Pokemon.gen2Attacks.put(newName, attacks);
            Pokemon.gen2Evos.put(newName, evos);
            try {
                FileHandle file = Gdx.files.internal("crystal_pokemon/"+path+"evos_attacks.asm");
                Reader reader = file.reader();
                BufferedReader br = new BufferedReader(reader);
                String line;
                boolean inSection = false;
                ArrayList<String> lines = new ArrayList<String>();
                while ((line = br.readLine()) != null)   {
                    lines.add(line);
                }

                for (int i=0; i < lines.size(); i++) {
                    line = lines.get(i);
                    if (line.toLowerCase().equals(newName+"evosattacks:")) {
                        inSection = true;
                        continue;
                    }
                    if (!inSection) {
                        continue;
                    }
                    if (line.contains(";")) {
                        // skip commented line
                        continue;
                    }
                    if (line.contains("EVOLVE_LEVEL")) {
                        String vals[] = line.split(", ");
                        evos.put(vals[1], vals[2].toLowerCase());
                    }
                    else if (line.contains("EVOLVE_ITEM") || line.contains("EVOLVE_TRADE") || line.contains("EVOLVE_HAPPINESS") || line.contains("EVOLVE_MOVE")) {
                        // TODO
                        String vals[] = line.split(", ");
                        evos.put(vals[1].toLowerCase().replace("_", " "), vals[2].toLowerCase());
                    }
                    else if (!line.contains("\t")) {
                        inSection = false;
                    }
                    else if (!line.contains("db 0")) {
                        String vals[] = line.split(", ");
                        String attack = vals[1].toLowerCase().replace('_', ' ');
                        // TODO: eventually remove attacksNotImplemented check
                        // Prevent loading moves like Metronome, Mimic etc that arent
                        // implemented
                        if (!Pokemon.attacksNotImplemented.contains(attack)) {
                            int level = Integer.valueOf(vals[0].split(" ")[1]);
                            String[] attacksArray = new String[]{attack};
                            if (attacks.containsKey(level)) {
                                attacksArray = new String[attacks.get(level).length+1];
                                for (int j=0; j<attacks.get(level).length; j++) {
                                    attacksArray[j] = attacks.get(level)[j];
                                }
                                attacksArray[attacks.get(level).length] = attack;
                            }
                            attacks.put(level, attacksArray);
                        }
                    }
                }
                reader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.learnSet = Pokemon.gen2Attacks.get(newName);
    }

    void loadOverworldSprites(String name) {
        name = name.toLowerCase();

        // Load overworld sprites from file
        try {
            FileHandle file = Gdx.files.internal("crystal_pokemon/prism/pokemon_names.asm");
            Reader reader = file.reader();
            BufferedReader br = new BufferedReader(reader);
            String line;
            ArrayList<String> lines = new ArrayList<String>();
            while ((line = br.readLine()) != null)   {
                lines.add(line);
            }
            boolean found = false;
            boolean flip = true;  // some up/down animations are 2 frame
            
            for (int i=0; i < lines.size(); ) {
                if (name.equals("egg")) {
                    break;
                }
                line = lines.get(i);

                // TODO: no overworld sprites for some pokemon
                if (name.equals("poochyena")) {
                    i = 312;
                    found = true;
                    flip = false;
                }
                else if (name.equals("mightyena")) {
                    i = 313;
                    found = true;
                }
                else if (name.equals("wingull")) {
                    i = 314;
                    found = true;
                }
                else if (name.equals("pelipper")) {
                    i = 315;
                    found = true;
                }
                else if (name.equals("snover")) {
                    i = 316;
                    found = true;
                }
                else if (name.equals("mimikyu")) {
                    i = 317;
                    found = true;
                }
                else if (name.equals("corphish")) {
                    i = 318;
                    found = true;
                }
                else if (name.equals("crawdaunt")) {
                    i = 319;
                    found = true;
                }
                else if (name.equals("litwick")) {
                    i = 320;
                    found = true;
                    flip = false;
                }
                else if (name.equals("lampent")) {
                    i = 321;
                    found = true;
                    flip = false;
                }
                else if (name.equals("chandelure")) {
                    i = 322;
                    found = true;
                    flip = false;
                }
                else if (name.equals("dwebble")) {
                    i = 323;
                    found = true;
                    flip = false;
                }
                else if (name.equals("crustle")) {
                    i = 324;
                    found = true;
                    flip = false;
                }
                else if (name.equals("scorbunny")) {
                    i = 325;
                    found = true;
                }
                else if (name.equals("raboot")) {
                    i = 326;
                    found = true;
                }
                else if (name.equals("cinderace")) {
                    i = 327;
                    found = true;
                }
                else if (name.equals("regidrago")) {
                    i = 328;
                    found = true;
                }
                else if (name.equals("regieleki")) {
                    i = 329;
                    found = true;
                }
                else if (name.equals("regice")) {
                    i = 330;
                    found = true;
                    flip = false;
                }
                else if (name.equals("regirock")) {
                    i = 331;
                    found = true;
                    flip = false;
                }
                else if (name.equals("registeel")) {
                    i = 332;
                    found = true;
                    flip = false;
                }
                else if (name.equals("regigigas")) {
                    i = 333;
                    found = true;
                }
                else if (name.equals("cacturne")) {
                    i = 336;
                    found = true;
                }
                else if (name.equals("elgyem")) {
                    i = 339;
                    found = true;
                }
                else if (name.equals("elgyem")) {
                    i = 339;
                    found = true;
                    flip = false;
                }
                else if (name.equals("hippopotas")) {
                    i = 340;
                    found = true;
                    flip = false;
                }
                else if (name.equals("cutiefly")) {
                    i = 341;
                    found = true;
                    flip = false;
                }
                else if (name.equals("ribombee")) {
                    i = 342;
                    found = true;
                    flip = false;
                }
                else if (name.equals("sandile")) {
                    i = 344;
                    found = true;
                    flip = false;
                }
                else if (name.equals("krokorok")) {
                    i = 345;
                    found = true;
                    flip = false;
                }
                else if (name.equals("krookodile")) {
                    i = 346;
                    found = true;
                    flip = false;
                }
                // TODO: debug, remove
                else if (name.equals("whismur")) {
                    i = 343;
                    found = true;
                    flip = false;
                }
                String currName = line.split("db \"")[1].split("\"")[0].toLowerCase().replace("@", "");
                if (currName.equals(name) || found) {
                    found = true;
                    // TODO: Credits to Megaman-Omega on Deviantart for prism overworld sprites
                    Texture text = TextureCache.get(Gdx.files.internal("crystal_pokemon/prism-overworld-sprites2.png"));
                    // These aren't consistent because sprite sheet is also inconsistent in ordering
                    int col = (i*6) % 156;
                    int row = (int)((i*6) / 156);
                    this.spriteOffsetY = row*16;
                    this.movingSprites.put("left", new Sprite(text, col*16, row*16, 16, 16));
                    this.altMovingSprites.put("left", new Sprite(text, col*16, row*16, 16, 16));
                    this.standingSprites.put("left", new Sprite(text, col*16 +16, row*16, 16, 16));

                    this.movingSprites.put("right", new Sprite(text, col*16, row*16, 16, 16));
                    this.movingSprites.get("right").flip(true, false);
                    this.altMovingSprites.put("right", new Sprite(text, col*16, row*16, 16, 16));
                    this.altMovingSprites.get("right").flip(true, false);
                    this.standingSprites.put("right", new Sprite(text, col*16 +16, row*16, 16, 16));
                    this.standingSprites.get("right").flip(true, false);

                    // TODO: some of these probably need to be flipped in the sprite sheet
                    this.movingSprites.put("up", new Sprite(text, col*16 +32, row*16, 16, 16));
                    this.altMovingSprites.put("up", new Sprite(text, col*16 +32, row*16, 16, 16));
                    if (flip) {
                        this.altMovingSprites.get("up").flip(true, false);
                    }
                    this.standingSprites.put("up", new Sprite(text, col*16 +48, row*16, 16, 16));

                    this.standingSprites.put("down", new Sprite(text, col*16 +64, row*16, 16, 16));
                    this.movingSprites.put("down", new Sprite(text, col*16 +80, row*16, 16, 16));
                    this.altMovingSprites.put("down", new Sprite(text, col*16 +80, row*16, 16, 16));
                    if (flip) {
                        this.altMovingSprites.get("down").flip(true, false);
                    }
                    
                    this.avatarSprites.add(this.standingSprites.get("down"));
                    this.avatarSprites.add(this.movingSprites.get("down"));
                    this.avatarSprites.add(this.standingSprites.get("down"));
                    this.avatarSprites.add(this.altMovingSprites.get("down"));
                    for (String key : new ArrayList<String>(this.standingSprites.keySet())) {
                        this.standingSprites.put(key+"_running", this.standingSprites.get(key));
                    }
                    for (String key : new ArrayList<String>(this.movingSprites.keySet())) {
                        this.movingSprites.put(key+"_running", this.movingSprites.get(key));
                    }
                    for (String key : new ArrayList<String>(this.altMovingSprites.keySet())) {
                        this.altMovingSprites.put(key+"_running", this.altMovingSprites.get(key));
                    }
                    break;
                }
                i++;
            }
            reader.close();

            if (found) {
                return;
            }

            // If this is an egg, load special texture for the overworld sprite
            if (this.name.equals("egg")) {
//                Texture text = TextureCache.get(Gdx.files.internal("crystal_pokemon/egg1.png"));
                // If cached egg texture doesn't exist in TextureCache.eggTextures, make
                // a new pixmap with colors replaced.
                String path = "";
                if (Pokemon.nuukPokemon.contains(this.eggHatchInto)) {
                    path = "nuuk/";
                }
                
                Texture text;
                if (!TextureCache.eggTextures.containsKey(this.eggHatchInto)) {
                    // TODO: won't work for prism pokemon
                    text = TextureCache.get(Gdx.files.internal("crystal_pokemon/"+path+"pokemon/" + this.eggHatchInto + "/back.png"));
                    int height = text.getWidth();
                    SpriteProxy tempSprite = new SpriteProxy(text, 0, 0, height, height);
//                    System.out.println(tempSprite.color1);
//                    System.out.println(tempSprite.color2);
                    // Replace colors in egg icon
                    text = TextureCache.get(Gdx.files.internal("crystal_pokemon/egg1.png"));
                    SpriteProxy tempEggSprite = new SpriteProxy(text, 0, 0, text.getWidth(), text.getHeight());
                    //
                    TextureData temp = text.getTextureData();
                    if (!temp.isPrepared()) {
                        temp.prepare();
                    }
                    Pixmap currPixmap = temp.consumePixmap();
                    Pixmap newPixmap = new Pixmap(text.getWidth(), text.getHeight(), Pixmap.Format.RGBA8888);
                    newPixmap.setColor(new Color(0, 0, 0, 0));
                    newPixmap.fill();
                    for (int i=0, j=0; j < text.getHeight(); i++) {
                        if (i > text.getWidth()) {
                            i=-1;
                            j++;
                            continue;
                        }
                        Color color = new Color(currPixmap.getPixel(i, j));
                        // color 1
                        if (color.r == tempEggSprite.color1.r && color.g == tempEggSprite.color1.g && color.b == tempEggSprite.color1.b) {
                            color.r = tempSprite.color1.r;
                            color.g = tempSprite.color1.g;
                            color.b = tempSprite.color1.b;
                        }
                        // color 2
                        else if (color.r == tempEggSprite.color2.r && color.g == tempEggSprite.color2.g && color.b == tempEggSprite.color2.b) {
                            color.r = tempSprite.color2.r;
                            color.g = tempSprite.color2.g;
                            color.b = tempSprite.color2.b;
                        }
                        newPixmap.drawPixel(i, j, Color.rgba8888(color.r, color.g, color.b, color.a));
                    }
                    TextureCache.eggTextures.put(this.eggHatchInto, TextureCache.get(newPixmap));
                }
                text = TextureCache.eggTextures.get(this.eggHatchInto);
                for (String dir : new String[]{"up", "down", "left", "right"}) {
                    this.standingSprites.put(dir, new Sprite(text, 0, 0, 16, 16));
                    this.movingSprites.put(dir, new Sprite(text, 0, 32, 16, 16));
                    this.altMovingSprites.put(dir, new Sprite(text, 0, 48, 16, 16));
                }
                Sprite hopSprite = new Sprite(text, 0, 16, 16, 16);
                this.avatarSprites.add(this.standingSprites.get("down"));
                this.avatarSprites.add(hopSprite);
                this.avatarSprites.add(this.standingSprites.get("down"));
                this.avatarSprites.add(hopSprite);
            }
            // else, load from crystal overworld sprite sheet
            else {
                // If failed to load from prism animations, load from crystal
                Texture text = TextureCache.get(Gdx.files.internal("crystal_pokemon/crystal-overworld-sprites1.png"));
                int dexNumber = Integer.valueOf(this.dexNumber)-1;
                // TODO: no honchkrow o/w sprite that I know of, just make it equal to murkrow's for now
                // TODO: could check  C:\cygwin64\home\Evan\polishedcrystal\gfx\icon if that exists
                // fun fact - polishedcrystal honchkrow uses identical pallete to houndoom.
                if (name.equals("honchkrow")) {
                    dexNumber = 197;
                }
//                if (dexNumber > 123) {
//                    dexNumber += 3;
//                }
                int col = (dexNumber % 15) * 2;
                int row = (int)((dexNumber) / 15);
                this.spriteOffsetY = 31 +row*25;
                for (String dir : new String[]{"up", "down", "left", "right"}) {
                    this.standingSprites.put(dir, new Sprite(text, 1 +col*17, 31 +row*25, 16, 16));
                    this.movingSprites.put(dir, new Sprite(text, 1 +col*17 +17, 31 +row*25, 16, 16));
                    this.altMovingSprites.put(dir, new Sprite(text, 1 +col*17 +17, 31 +row*25, 16, 16));
                }
                this.avatarSprites.add(this.standingSprites.get("down"));
                this.avatarSprites.add(this.movingSprites.get("down"));
                this.avatarSprites.add(this.standingSprites.get("down"));
                this.avatarSprites.add(this.movingSprites.get("down"));
            }

            for (String key : new ArrayList<String>(this.standingSprites.keySet())) {
                this.standingSprites.put(key+"_running", this.standingSprites.get(key));
            }
            for (String key : new ArrayList<String>(this.movingSprites.keySet())) {
                this.movingSprites.put(key+"_running", this.movingSprites.get(key));
            }
            for (String key : new ArrayList<String>(this.altMovingSprites.keySet())) {
                this.altMovingSprites.put(key+"_running", this.altMovingSprites.get(key));
            }

            // TODO: load ghost overworld sprite from ghost sheet.
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Load pokemon sprites and data from crystal_pokemon/prism files.
     */
    void loadPrismPokemon(String name) {
        name = name.toLowerCase();
        
        if (this.name.equals("egg")) {
            name = this.eggHatchInto;
        }

        // TODO: if can't find prism path, check other paths
//        String path = "crystal_pokemon/prism/";
//        FileHandle file = Gdx.files.internal(path+"base_stats/" + name + ".asm");
//        if (!file.exists()) {
//            path = "crystal_pokemon/nuuk/";
//        }

        // load base stats
        try {
            FileHandle file = Gdx.files.internal("crystal_pokemon/prism/base_stats/" + name + ".asm");
            Reader reader = file.reader();
            BufferedReader br = new BufferedReader(reader);
            String line;
            int lineNum = 0;
            while ((line = br.readLine()) != null)   {
                if (lineNum == 1) {
                    String stats[] = line.split("db")[1].split(",");
                    this.baseStats.put("hp", Integer.valueOf((stats[0].replace(" ", ""))));
                    this.baseStats.put("attack", Integer.valueOf((stats[1].replace(" ", ""))));
                    this.baseStats.put("defense", Integer.valueOf((stats[2].replace(" ", ""))));
                    this.baseStats.put("speed", Integer.valueOf((stats[3].replace(" ", ""))));
                    this.baseStats.put("specialAtk", Integer.valueOf((stats[4].replace(" ", ""))));
                    this.baseStats.put("specialDef", Integer.valueOf((stats[5].replace(" ", ""))));
                } else if (lineNum == 2) {
                    String types[] = line.split("db ")[1].split(" ; ")[0].split(", ");
                    this.types.add(types[0]);
                    this.types.add(types[1]);
                } else if (lineNum == 3) {
                    String catchRate = line.split("db ")[1].split(" ;")[0];
                    this.baseStats.put("catchRate", Integer.valueOf(catchRate));
                } else if (lineNum == 4) {
                    String baseExp = line.split("db ")[1].split(" ;")[0];
                    this.baseStats.put("baseExp", Integer.valueOf(baseExp));
                } else if (lineNum == 7) {
                    // prism seems to just use (number+1)/256 for percent female
                    String genderRatio = line.split("db ")[1].split(" ;")[0];
                    int percentFemale = Integer.valueOf(genderRatio);
                    if (Pokemon.rand.nextInt(256) < percentFemale) {
                        this.gender = "female";
                    }
                    else {
                        this.gender = "male";
                    }
                // Egg cycles to hatch
                } else if (lineNum == 9 && this.name.equals("egg")) {
                    String eggCycles = line.split("db ")[1].split(" ;")[0];
                    this.happiness = Integer.valueOf(eggCycles);
                } else if (lineNum == 14) {
                    this.growthRateGroup = line.split("db ")[1].split(" ;")[0];
                // Egg groups this pokemon belongs to
                } else if (lineNum == 15) {
                    String groups = line.split("dn ")[1].split(" ;")[0];
                    this.eggGroups = groups.split(", ");
                }
                // TODO: other stats
                lineNum++;
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Load sprite and animation data
        // Load front sprite
        if (!Pokemon.textures.containsKey(name+"_front")) {
            Texture text = TextureCache.get(Gdx.files.internal("crystal_pokemon/prism/pics/" + name + "/front.png"));
            Pokemon.textures.put(name+"_front", text);

            // Swap palette for shiny texture (load by appending '_shiny' to the texture name)
            // Load shiny palette from file
            Color normalColor1 = null;
            Color normalColor2 = new Color();
            Color shinyColor1 = null;
            Color shinyColor2 = new Color();
            try {
                FileHandle file = Gdx.files.internal("crystal_pokemon/prism/pics/" + name + "/shiny.pal");
                Reader reader = file.reader();
                BufferedReader br = new BufferedReader(reader);
                String line;
                while ((line = br.readLine()) != null)   {
                    if (line.contains("RGB")) {
                        String[] vals = line.split("\tRGB ")[1].split(", ");
                        if (shinyColor1 == null) {
                            shinyColor1 = new Color();
                            shinyColor1.r = Float.valueOf(vals[0]);
                            shinyColor1.g = Float.valueOf(vals[1]);
                            shinyColor1.b = Float.valueOf(vals[2]);
                        }
                        else {
                            shinyColor2.r = Float.valueOf(vals[0]);
                            shinyColor2.g = Float.valueOf(vals[1]);
                            shinyColor2.b = Float.valueOf(vals[2]);
                        }
                    }
                }
                reader.close();

                file = Gdx.files.internal("crystal_pokemon/prism/pics/" + name + "/normal.pal");
                reader = file.reader();
                br = new BufferedReader(reader);
                while ((line = br.readLine()) != null)   {
                    if (line.contains("RGB")) {
                        String[] vals = line.split("\tRGB ")[1].split(", ");
                        if (normalColor1 == null) {
                            normalColor1 = new Color();
                            normalColor1.r = Float.valueOf(vals[0]);
                            normalColor1.g = Float.valueOf(vals[1]);
                            normalColor1.b = Float.valueOf(vals[2]);
                        }
                        else {
                            normalColor2.r = Float.valueOf(vals[0]);
                            normalColor2.g = Float.valueOf(vals[1]);
                            normalColor2.b = Float.valueOf(vals[2]);
                        }
                    }
                }
                reader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            TextureData temp = text.getTextureData();
            if (!temp.isPrepared()) {
                temp.prepare();
            }
            Pixmap currPixmap = temp.consumePixmap();
            Pixmap newPixmap = new Pixmap(text.getWidth(), text.getHeight(), Pixmap.Format.RGBA8888);
            newPixmap.setColor(new Color(0, 0, 0, 0));
            newPixmap.fill();
            for (int i=0, j=0; j < text.getHeight(); i++) {
                if (i > text.getWidth()) {
                    i=-1;
                    j++;
                    continue;
                }
                Color color = new Color(currPixmap.getPixel(i, j));
                if ((int)(color.r*32) == (int)normalColor1.r && (int)(color.g*32) == (int)normalColor1.g && (int)(color.b*32) == (int)normalColor1.b) {
                    color.r = (shinyColor1.r*8f)/256f;
                    color.g = (shinyColor1.g*8f)/256f;
                    color.b = (shinyColor1.b*8f)/256f;
                }
                else if ((int)(color.r*32) == (int)normalColor2.r && (int)(color.g*32) == (int)normalColor2.g && (int)(color.b*32) == (int)normalColor2.b) {
                    color.r = (shinyColor2.r*8f)/256f;
                    color.g = (shinyColor2.g*8f)/256f;
                    color.b = (shinyColor2.b*8f)/256f;
//                    color = shinyColor2;
                }
                newPixmap.drawPixel(i, j, Color.rgba8888(color.r, color.g, color.b, 1f));
            }
            text = TextureCache.get(newPixmap);
            Pokemon.textures.put(name+"_front_shiny", text);

            // Back sprites
            text = TextureCache.get(Gdx.files.internal("crystal_pokemon/prism/pics/" + name + "/back.png"));
            Pokemon.textures.put(name+"_back_shiny", text);
            temp = text.getTextureData();
            if (!temp.isPrepared()) {
                temp.prepare();
            }
            currPixmap = temp.consumePixmap();
            newPixmap = new Pixmap(text.getWidth(), text.getHeight(), Pixmap.Format.RGBA8888);
            newPixmap.setColor(new Color(0, 0, 0, 0));
            newPixmap.fill();
            for (int i=0, j=0; j < text.getHeight(); i++) {
                if (i > text.getWidth()) {
                    i=-1;
                    j++;
                    continue;
                }
                Color color = new Color(currPixmap.getPixel(i, j));
                if ((int)(color.r*32) == (int)shinyColor1.r && (int)(color.g*32) == (int)shinyColor1.g && (int)(color.b*32) == (int)shinyColor1.b) {
                    color.r = (normalColor1.r*8f)/256f;
                    color.g = (normalColor1.g*8f)/256f;
                    color.b = (normalColor1.b*8f)/256f;
                }
                else if ((int)(color.r*32) == (int)shinyColor2.r && (int)(color.g*32) == (int)shinyColor2.g && (int)(color.b*32) == (int)shinyColor2.b) {
                    color.r = (normalColor2.r*8f)/256f;
                    color.g = (normalColor2.g*8f)/256f;
                    color.b = (normalColor2.b*8f)/256f;
                }
                newPixmap.drawPixel(i, j, Color.rgba8888(color.r, color.g, color.b, 1f));
            }
            text = TextureCache.get(newPixmap);
            Pokemon.textures.put(name+"_back", text);
        }
//        Texture pokemonText = new Texture(Gdx.files.internal("crystal_pokemon/prism/pics/" + name + "/front.png"));  // TODO: remove
        String isShiny = "";
        if (this.isShiny) {
            isShiny = "_shiny";
        }
        Texture pokemonText = Pokemon.textures.get(name+"_front"+isShiny);
        // height and width are the same for these sprites
        int height = pokemonText.getWidth();
        this.sprite = new SpriteProxy(pokemonText, 0, 0, height, height);
//        if (!Pokemon.textures.containsKey(name+"_back")) {
//            Pokemon.textures.put(name+"_back", new Texture(Gdx.files.internal("crystal_pokemon/prism/pics/" + name + "/back.png")));
//        }
//        pokemonText = new Texture(Gdx.files.internal("crystal_pokemon/prism/pics/" + name + "/back.png"));  // TODO: remove
        pokemonText = Pokemon.textures.get(name+"_back"+isShiny);
//        height = pokemonText.getWidth();
        this.backSprite = new SpriteProxy(pokemonText, 0, 0, 48, 48);

        // Load animation from file
        this.introAnim = new ArrayList<SpriteProxy>();
        try {
            FileHandle file = Gdx.files.internal("crystal_pokemon/prism/pics/" + name + "/anim0.asm");
            Reader reader = file.reader();
            BufferedReader br = new BufferedReader(reader);
            String line;
            int setrepeat = 0;
            ArrayList<String> lines = new ArrayList<String>();
            while ((line = br.readLine()) != null)   {
                lines.add(line);
            }
            for (int i=0; i < lines.size(); ) {
                line = lines.get(i);
                if (line.contains("setrepeat")) {
                    setrepeat = Integer.valueOf(line.split("setrepeat ")[1]);
                } else if (line.contains("frame")) {
                    String vals[] = line.split("frame ")[1].split(", ");
                    int numFrames = Integer.valueOf(vals[1]);
                    int frame = Integer.valueOf(vals[0]);
                    for (int j=0; j < numFrames; j++) {
//                        pokemonText = new Texture(Gdx.files.internal("crystal_pokemon/prism/pics/" + name + "/front.png")
                        pokemonText = Pokemon.textures.get(name+"_front"+isShiny);
                        SpriteProxy sprite = new SpriteProxy(pokemonText, 0, height*frame, height, height);
                        this.introAnim.add(sprite);
                    }
                } else if (line.contains("dorepeat")) {
                    if (setrepeat != 0) {
                        i = Integer.valueOf(line.split("dorepeat ")[1]);
                        setrepeat--;
                        continue;
                    }
                }
                i++;
            }
            reader.close();
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Load attacks from file
        if (!Pokemon.gen2Attacks.containsKey(name) || !Pokemon.gen2Evos.containsKey(name)) {
            Map<Integer, String[]> attacks = new HashMap<Integer, String[]>();
            Map<String, String> evos = new HashMap<String, String>();
            Pokemon.gen2Attacks.put(name, attacks);
            Pokemon.gen2Evos.put(name, evos);
            try {
                FileHandle file = Gdx.files.internal("crystal_pokemon/prism/movesets/"+name.substring(0,1).toUpperCase()+name.substring(1)+".asm");
                Reader reader = file.reader();
                BufferedReader br = new BufferedReader(reader);
                String line;
                ArrayList<String> lines = new ArrayList<String>();
                while ((line = br.readLine()) != null)   {
                    lines.add(line);
                }

                for (int i=0; i < lines.size(); i++) {
                    line = lines.get(i);
                    if (!line.contains("db")) {
                        continue;
                    }
                    if (line.contains("EVOLVE_LEVEL")) {
                        String vals[] = line.split(", ");
                        evos.put(vals[1], vals[2].toLowerCase());
                    }
                    else if (line.contains("EVOLVE_ITEM") || line.contains("EVOLVE_TRADE")) {
                        // TODO
                        String vals[] = line.split(", ");
                        evos.put(vals[1].toLowerCase().replace("_", " "), vals[2].toLowerCase());
                    }
                    else if (!line.contains("db 0")) {
                        String vals[] = line.split(", ");
                        String attack = vals[1].toLowerCase().replace('_', ' ');
                        // TODO: there are a bunch of prism attacks that can't be handled by the engine,
                        //  so check if the attack exists currently.
                        // Ie, attacks with different typings (SOUND, FAIRY etc).
                        // Would be ideal to update the prism pokemon from later generations to use
                        //  valid types and attacks (I'm undecided tho as to what exactly is the best
                        //  solution).
                        if (!Game.staticGame.battle.attacks.containsKey(attack)) {
                            continue;
                        }
                        int level = Integer.valueOf(vals[0].split(" ")[1]);
                        String[] attacksArray = new String[]{attack};
                        if (attacks.containsKey(level)) {
                            attacksArray = new String[attacks.get(level).length+1];
                            for (int j=0; j<attacks.get(level).length; j++) {
                                attacksArray[j] = attacks.get(level)[j];
                            }
                            attacksArray[attacks.get(level).length] = attack;
                        }
                        attacks.put(level, attacksArray);
                    }
                }
                reader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        this.learnSet = Pokemon.gen2Attacks.get(name);
    }

    /**
     * When you send out a pokemon, reset all of it's stat stages to 0 (and corresponding stats).
     */
    void resetStatStages() {
        this.statStages.put("attack", 0);
        this.statStages.put("defense", 0);
        this.statStages.put("specialAtk", 0);
        this.statStages.put("specialDef", 0);
        this.statStages.put("speed", 0);
        this.statStages.put("accuracy", 0);
        this.statStages.put("evasion", 0);
//        this.currentStats = new HashMap<String, Integer>(this.maxStats);
        for (String stat : this.maxStats.keySet()) {
            if (stat.equals("hp")) {
                continue;
            }
            this.currentStats.put(stat, this.maxStats.get(stat));
        }
        // Handle stat changes caused by status
        if (this.status != null) {
            if (status.equals("paralyze")) {
                // Reduce speed to 1/4
                this.currentStats.put("speed", this.currentStats.get("speed")/4);
            }
            else if (status.equals("burn")) {
                // Reduce attack to 1/4
                this.currentStats.put("attack", this.currentStats.get("attack")/4);
            }
        }
    }

    /**
     * Add pokemon to inventory, remove standingAction from actionStack,
     * and remove pokemon from game.map.pokemon.
     */
    public class AddToInventory extends Action {
        public int layer = 130;
        
        public AddToInventory(Action nextAction) {
            this.nextAction = nextAction;
        }

        public int getLayer(){
            return this.layer;
        }

        @Override
        public void firstStep(Game game) {
            if (Pokemon.this.type == Player.Type.REMOTE) {
                game.client.sendTCP(new Network.DropPokemon(game.player.network.id,
                                                            Pokemon.this.position));
            }
            if (game.player.pokemon.size() < 6) {
                game.player.pokemon.add(Pokemon.this);
                game.map.pokemon.remove(Pokemon.this.position);
                game.actionStack.remove(Pokemon.this.standingAction);
            }
            else {
                game.actionStack.remove(this);
                game.insertAction(new DisplayText.Clear(game,
                                  new WaitFrames(game, 3,
                                  new PlaySound("error1",
                                  new DisplayText(game, "Not enough room in your party!", null, null,
                                  this.nextAction)))));
                return;
            }
            Action newAction;
            if (Pokemon.this.name.equals("egg") && Pokemon.this.previousOwner != game.player) {
                newAction = //new SetField(game, "playerCanMove", false,
                            new DisplayText(game, game.player.name+" received an EGG.", "Berry_Get.ogg", null,
                            //new SetField(game, "playerCanMove", true,
                            null);
                // Aggro any (potential) nearby parents
                boolean playedSound = false;  // Only play the '!' sound once
                Vector2 startPos = Pokemon.this.position.cpy().add(-16*10, -16*10);
                startPos.x = (int)startPos.x - (int)startPos.x % 16;
                startPos.y = (int)startPos.y - (int)startPos.y % 16;
                Vector2 endPos = Pokemon.this.position.cpy().add(16*10, 16*10);
                endPos.x = (int)endPos.x - (int)endPos.x % 16;
                endPos.y = (int)endPos.y - (int)endPos.y % 16;
                int offSet = 0;  // Needed to prevent pokemon from moving into same tile
                for (Vector2 currPos = new Vector2(startPos.x, startPos.y); currPos.y < endPos.y;) {
                    Pokemon pokemon = game.map.pokemon.get(currPos);
                    currPos.x += 16;
                    if (currPos.x > endPos.x) {
                        currPos.x = startPos.x;
                        currPos.y += 16;
                    }
                    if (pokemon == null) {
                        continue;
                    }
                    if (pokemon.name.equals("egg")) {
                        continue;
                    }
                    // Pokemon that are friendly to the player don't aggro
                    if (pokemon.previousOwner == game.player) {
                        continue;
                    }
                    // For now, anything sharing an egg group with the egg
                    // will be assumed to be a parent.
                    for (String group1 : pokemon.eggGroups) {
                        for (String group2 : Pokemon.this.eggGroups) {
                            if (group1.equals(group2)) {
                                if (!playedSound) {
                                    game.insertAction(new PlaySound("ledge2", null));
                                    playedSound = true;
                                }
                                game.insertAction(new SetField(pokemon, "canMove", false,
                                                  pokemon.new Emote("!",
                                                  new SetField(pokemon, "canMove", true,
                                                  new SetField(pokemon, "aggroPlayer", true,
                                                  null)))));
                                if (pokemon.standingAction != null && pokemon.standingAction instanceof Pokemon.Standing) {
                                    ((Pokemon.Standing)pokemon.standingAction).aggroTimer = -2*offSet;
                                }
                                offSet++;
                                break;
                            }
                        }
                    }
                }
            }
            else {
                newAction = new PlaySound("seed1", null);
            }
            if (game.player.pokemon.size() >= 6 && !game.player.displayedMaxPartyText) {
                game.player.displayedMaxPartyText = true;
                newAction.append(new DisplayText(game, "Your party is full! You will need to DROP some of them in order to catch more.", null, false, true,
                                 null));
            }
            Pokemon.this.previousOwner = game.player;
            // Break up relationship between this Pokemon and overworld Pokemon :'(
            if (Pokemon.this.loveInterest != null) {
                Pokemon.this.loveInterest.loveInterest = null;  // Farewell, my love...
                Pokemon.this.loveInterest = null;  // Until we meet again...
            }
            newAction.append(this.nextAction);
            game.actionStack.remove(this);
            game.insertAction(newAction);
        }

        @Override
        public void step(Game game) {
            // so, if i don't override it will immediately insert nextAction...
            // that's annoying and warrants Action being an interface (?)
        }
    }

    /**
     * Draw pokemon lower (below grass).
     */
    public class DrawLower extends Action {
        public int layer = 130;
        Sprite spritePart;
        public boolean isEgg = false;
        
        public boolean following = false;

        public int getLayer(){return this.layer;}
        
        public DrawLower() {}
        
        public DrawLower(boolean following) {
            this.following = following;
        }

        @Override
        public void firstStep(Game game) {
            this.isEgg = Pokemon.this.name.equals("egg");
        }

        @Override
        public void step(Game game) {
            if (!game.actionStack.contains(Pokemon.this.standingAction)) {
                game.actionStack.remove(this);
                return;
            }
            // Count up timer to when harvest-able
            if (Pokemon.this.hasItem == null && !this.isEgg) {
                if (Pokemon.this.inHabitat) {
                    Pokemon.this.harvestTimer++;
                    if (Pokemon.this.inShelter) {
                        Pokemon.this.harvestTimer++;
                    }
                }
                // TODO: time required to harvest may vary per Pokemon
//                if (Pokemon.this.harvestTimer >= 3600*2) {  // 2 irl minutes, 1 if near shelter
                if (Pokemon.this.harvestTimer >= Pokemon.this.harvestTimerMax) {
                    Pokemon.this.hasItem = Pokemon.this.harvestables.get(game.map.rand.nextInt(Pokemon.this.harvestables.size()));
                }
            }
            // If pokemon is female and has a love interest, count down to lay egg
            if (Pokemon.this.loveInterest != null &&
                Pokemon.this.inHabitat &&
                Pokemon.this.nearbyEggs < 5 &&  // Don't lay eggs if too many are nearby
                (Pokemon.this.gender.equals("female") || 
                (Pokemon.this.eggGroups[0].equals("EGG_DITTO") && Pokemon.this.loveInterest.gender.equals("male"))) &&
                Pokemon.this.layEggTimer > 0) {
                Pokemon.this.layEggTimer--;
                // Lay eggs in standing action, not while moving
            }

            if (!Pokemon.this.drawThisFrame && !this.following) {
                return;
            }
            // Don't draw pokemon unless in same indoor/outdoor as player
            if (game.map.tiles != Pokemon.this.mapTiles) {
                return;
            }
            this.spritePart = new Sprite(Pokemon.this.currOwSprite);
            this.spritePart.setRegionY(Pokemon.this.spriteOffsetY+8);
            this.spritePart.setRegionHeight(8);
            game.mapBatch.draw(this.spritePart, Pokemon.this.position.x, Pokemon.this.position.y+4);
        }
    }

    /**
     * Draw pokemon upper (above grass).
     */
    public class DrawUpper extends Action {
        public int layer = 115;
        Sprite spritePart;
        
        public boolean following = false;

        public int getLayer(){return this.layer;}
        
        public DrawUpper() {}

        public DrawUpper(boolean following) {
            this.following = following;
        }

        @Override
        public void step(Game game) {
            if (!game.actionStack.contains(Pokemon.this.standingAction)) {
                game.actionStack.remove(this);
                return;
            }
            if (!Pokemon.this.drawThisFrame && !this.following) {
                return;
            }
            // Don't draw pokemon unless in same indoor/outdoor as player
            if (game.map.tiles != Pokemon.this.mapTiles) {
                return;
            }
            this.spritePart = new Sprite(Pokemon.this.currOwSprite);
            this.spritePart.setRegionY(Pokemon.this.spriteOffsetY);
            this.spritePart.setRegionHeight(8);
            game.mapBatch.draw(this.spritePart, Pokemon.this.position.x, Pokemon.this.position.y+12);
            // TODO: remove if unused
//            Pokemon.this.currOwSprite.setPosition(Pokemon.this.position.x, Pokemon.this.position.y);
            Pokemon.this.drawThisFrame = false;
        }
    }

    /**
     * Draw emote animation above pokemon.
     */
    public class Emote extends Action {
        public int layer = 109;  // 114
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
            if (game.map.tiles == Pokemon.this.mapTiles) {
                game.mapBatch.draw(this.sprite, Pokemon.this.position.x, Pokemon.this.position.y +4 +16);
            }
            if (this.timer >= 60) {
                game.actionStack.remove(this);
                game.insertAction(this.nextAction);
            }
            this.timer++;
        }
    }

    /**
     * Pokemon moving in overworld.
     */
    public class Moving extends Action {
        public int layer = 130;
        Vector2 initialPos; // track distance of movement
        Vector2 targetPos;
        float xDist, yDist;
        boolean alternate;
        int delay = 1;
        int timer = 1;
        float numMove = 1f;
        String dirFacing;
        boolean isFollowing = false;

        public Moving(int delay, float numMove, boolean alternate, Action nextAction) {
            this(Pokemon.this.dirFacing, delay, numMove, alternate, false, nextAction);
        }

        public Moving(String dirFacing, int delay, float numMove, boolean alternate, boolean isFollowing, Action nextAction) {
            this.dirFacing = dirFacing;
            this.delay = delay;
            this.numMove = numMove;
            this.alternate = alternate;
            this.isFollowing = isFollowing;
            this.nextAction = nextAction;
        }

        public String getCamera() {
            return "map";
        }

        public int getLayer(){
            return this.layer;
        }

        @Override
        public void firstStep(Game game) {
            this.initialPos = new Vector2(Pokemon.this.position);
            if (this.dirFacing.equals("up")) {
                this.targetPos = new Vector2(Pokemon.this.position.x, Pokemon.this.position.y+16);
            }
            else if (this.dirFacing.equals("down")) {
                this.targetPos = new Vector2(Pokemon.this.position.x, Pokemon.this.position.y-16);
            }
            else if (this.dirFacing.equals("left")) {
                this.targetPos = new Vector2(Pokemon.this.position.x-16, Pokemon.this.position.y);
            }
            else if (this.dirFacing.equals("right")) {
                this.targetPos = new Vector2(Pokemon.this.position.x+16, Pokemon.this.position.y);
            }
            if (!this.isFollowing) {
                if (game.map.pokemon.containsKey(Pokemon.this.position) && game.map.pokemon.get(Pokemon.this.position).equals(Pokemon.this)) {
                    game.map.pokemon.remove(Pokemon.this.position);
                }
                game.map.pokemon.put(this.targetPos.cpy(), Pokemon.this);

                // Play 'dash' sound if pokemon is moving fast
                if (this.numMove <= 1) {
                    // TODO: this will get played incessantly, should only play if near player
                    // probably needs to be generalized for PlaySound()
//                    game.insertAction(new PlaySound("dash1", 1f, null));
                }
                else if (this.numMove <= 1.5f) {
                    game.insertAction(new PlaySound("ride1", 1f, true, null));
                }
            }
            Pokemon.this.canMove = true;

            // Lay egg if able
            // TODO: pokemon following you may do this
            if (Pokemon.this.layEggTimer <= 0 &&
                Pokemon.this.loveInterest != null &&
//                game.map.pokemon.get(Pokemon.this.position) == Pokemon.this
                !game.map.pokemon.containsKey(this.initialPos)) {

                // Create egg, place on overworld
                Pokemon.this.layEggTimer = Pokemon.this.layEggTimerMax;  //3600*2;
                String baseSpecies = Pokemon.baseSpecies.get(Pokemon.this.name.toLowerCase());
                if (Pokemon.this.name.equals("ditto")) {
                    baseSpecies = Pokemon.baseSpecies.get(Pokemon.this.loveInterest.name.toLowerCase());
                }
                // Nidoqueen will lay a nidoran_f egg b/c nidoran_f is it's base speicies
                // Add a 50 percent chance for it to lay nidoran_m egg instead.
                if (baseSpecies.equals("nidoran_f") && Game.rand.nextInt(256) < 128) {
                    baseSpecies = "nidoran_m";
                }
                Pokemon pokemonEgg = new Pokemon("egg", 5, Pokemon.Generation.CRYSTAL, Game.rand.nextInt(256) == 0, baseSpecies);
                // Find first empty attack slot in pokemon egg (first egg move is put here)
                int currIndex = 0;
                for (int i=0; i < pokemonEgg.attacks.length; i++) {
                    if (pokemonEgg.attacks[i] == null) {
                        currIndex = i;
                        break;
                    }
                }
                // TODO: debug, remove
                if (Pokemon.eggMoves.get(baseSpecies) == null) {
                    System.out.println(Pokemon.this.name);
                    System.out.println(baseSpecies);
                }
                for (String move : Pokemon.eggMoves.get(baseSpecies)) {
                    // Love interest *should* be male since we should only be here
                    // if the pokemon is female
                    for (String attack : Pokemon.this.loveInterest.attacks) {
                        if (move.equals(attack)) {
                            pokemonEgg.attacks[currIndex] = move;
                            currIndex = (currIndex + 1) % 4;
                        }
                    }
                }
                // TODO: IV's (once they are implemented)
                pokemonEgg.mapTiles = Pokemon.this.mapTiles;
                pokemonEgg.position = this.initialPos.cpy();
//                game.map.pokemon.put(pokemonEgg.position.cpy(), pokemonEgg);  // TODO: not sure why this isn't done by Standing()
                game.insertAction(pokemonEgg.new Standing());
            }

            // For each player in range, move this pokemon
            // If the pokemon moves out of the player's loading zone, tell that client to remove it.
            if (game.type == Game.Type.SERVER) {
                for (Player player : game.players.values()) {
                    if (player.network.loadingZone.contains(this.targetPos)) {
                        game.server.sendToTCP(player.network.connectionId,
                                              new Network.MovePokemon(Pokemon.this));
                    }
                    else if (player.network.loadingZone.contains(Pokemon.this.position)) {
                        game.server.sendToTCP(player.network.connectionId,
                                              new Network.OverworldPokemonData(Pokemon.this, Pokemon.this.position, true));
                    }
                }
            }
        }

        @Override
        public void step(Game game) {
            if (!Pokemon.this.canMove) {
                return;
            }
            this.timer--;
            if (this.timer <= 0) {
                this.timer = this.delay;
                if (this.dirFacing.equals("up")) {
                    Pokemon.this.position.y += this.numMove;
                }
                else if (this.dirFacing.equals("down")) {
                    Pokemon.this.position.y -= this.numMove;
                }
                else if (this.dirFacing.equals("left")) {
                    Pokemon.this.position.x -= this.numMove;
                }
                else if (this.dirFacing.equals("right")) {
                    Pokemon.this.position.x += this.numMove;
                }
            }
            this.xDist = Math.abs(this.initialPos.x - Pokemon.this.position.x);
            this.yDist = Math.abs(this.initialPos.y - Pokemon.this.position.y);

            if ((this.yDist < 13 && this.yDist > 2)
                || (this.xDist < 13 && this.xDist > 2)) {
                if (this.alternate == true) {
                    Pokemon.this.currOwSprite = Pokemon.this.altMovingSprites.get(this.dirFacing);
                }
                else {
                    Pokemon.this.currOwSprite = Pokemon.this.movingSprites.get(this.dirFacing);
                }
            }
            else {
                Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(this.dirFacing);
            }
            if (this.xDist >= 16 || this.yDist >= 16) {
                Pokemon.this.position.set(this.targetPos);
                game.actionStack.remove(this);
                game.insertAction(this.nextAction);
                Pokemon.this.standingAction = this.nextAction;
                Pokemon.this.checkHabitat(game);
                if (this.isFollowing) {
                    this.nextAction.step(game);  // gets rid of 'jitter'
                }
            }
        }
    }

    /**
     * Remove from inventory and spawn Pokemon in the overworld.
     */
    public class RemoveFromInventory extends Action {
        public int layer = 130;
        int moveTimer = 0;

        public int getLayer(){
            return this.layer;
        }

        @Override
        public void firstStep(Game game) {
            if (game.type == Game.Type.CLIENT) {
                Pokemon.this.type = Player.Type.REMOTE;
                game.client.sendTCP(new Network.DropPokemon(game.player.network.id,
                                                            DrawPokemonMenu.currIndex,
                                                            game.player.dirFacing));
            }

            game.player.pokemon.remove(Pokemon.this);
            // TODO: shouldn't need this. may also set to pokemon with hp == 0
            if (game.player.currPokemon == Pokemon.this) {
                game.player.currPokemon = game.player.pokemon.get(0);
            }
            game.actionStack.remove(this);
            game.insertAction(Pokemon.this.new Standing());
        }
    }

    /**
     * Tell server to unpause the pokemon.
     */
    public class UnPause extends Action {
        public int layer = 130;
        int moveTimer = 0;

        public int getLayer(){
            return this.layer;
        }

        @Override
        public void firstStep(Game game) {
            if (game.type == Game.Type.CLIENT) {
                game.client.sendTCP(new Network.PausePokemon(game.player.network.id,
                                                             Pokemon.this.position,
                                                             false));
            }
            game.actionStack.remove(this);
        }
    }

    public class Follow extends Action {
        public int layer = 151;
        boolean alternate = true;
        boolean onPlayer = false;
        Player player;
        
        public Follow(Player player) {
            this.player = player;
        }

        @Override
        public void firstStep(Game game) {
            Pokemon.this.standingAction = this;
            Pokemon.this.dirFacing = this.player.dirFacing;
            String oppDir = Player.oppDirs.get(this.player.dirFacing);
            Vector2 pos = this.player.facingPos(oppDir);
            if (game.map.tiles.get(pos) != null && !game.map.tiles.get(pos).attrs.get("solid") && !game.map.tiles.get(pos).attrs.get("ledge")) {
                Pokemon.this.position = pos;
            }
            else {
                Pokemon.this.position = this.player.position.cpy();
                this.onPlayer = true;
            }
            Pokemon.this.mapTiles = game.map.tiles;
            Pokemon.this.moveDirs.clear();
            Pokemon.this.numMoves.clear();
            Pokemon.this.ledgeJumps.clear();
            game.insertAction(Pokemon.this.new DrawLower(true));
            game.insertAction(Pokemon.this.new DrawUpper(true));
        }

        @Override
        public void step(Game game) {
            Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
            Pokemon.this.mapTiles = game.map.tiles;
            if (Pokemon.this.moveDirs.size() > 0) {
                String moveDir = Pokemon.this.moveDirs.remove(0);
                float numMove = Pokemon.this.numMoves.remove(0);
                int ledgeJump = 0;
                // TODO: could break if multiple ledges in a row
                if (Pokemon.this.ledgeJumps.size() > 0) {
                    ledgeJump = Pokemon.this.ledgeJumps.remove(0);
                }
                if (!this.onPlayer) {
                    game.actionStack.remove(this);
                    Action action;
                    if (ledgeJump <= 0) {
                        action = Pokemon.this.new Moving(Pokemon.this.dirFacing, 1, numMove, this.alternate, true,
                                 this);
                    }
                    else {
                        action = Pokemon.this.new LedgeJump(Pokemon.this.dirFacing, ledgeJump, this);
                    }
                    this.alternate = !this.alternate;
                    game.insertAction(action);
                    Pokemon.this.standingAction = action;
                }
                this.onPlayer = false;
                Pokemon.this.dirFacing = moveDir;
//                Pokemon.this.moveDir = null;
//                Pokemon.this.numMove = 1f;
            }
        }
    }

    class LedgeJump extends Action {
        public int layer = 131;
        float xDist, yDist;
        Vector2 initialPos, targetPos;
        Sprite shadow;
        int timer1 = 0;
        ArrayList<Integer> yMovesList = new ArrayList<Integer>();
        ArrayList<Map<String, Sprite>> spriteAnim = new ArrayList<Map<String, Sprite>>();
        int speed = 1;  // 1 or 2, 2 is faster
        String dirFacing;

        public LedgeJump(String dirFacing, int speed, Action nextAction) {
            this.dirFacing = dirFacing;
            this.speed = speed;
            this.nextAction = nextAction;
            this.initialPos = new Vector2(Pokemon.this.position);
            if (Pokemon.this.dirFacing.equals("up")) {
                this.targetPos = new Vector2(Pokemon.this.position.x, Pokemon.this.position.y+32/this.speed);
            }
            else if (Pokemon.this.dirFacing.equals("down")) {
                this.targetPos = new Vector2(Pokemon.this.position.x, Pokemon.this.position.y-32/this.speed);
            }
            else if (Pokemon.this.dirFacing.equals("left")) {
                this.targetPos = new Vector2(Pokemon.this.position.x-32/this.speed, Pokemon.this.position.y);
            }
            else if (Pokemon.this.dirFacing.equals("right")) {
                this.targetPos = new Vector2(Pokemon.this.position.x+32/this.speed, Pokemon.this.position.y);
            }

            // Shadow sprite
            Texture shadowText = TextureCache.get(Gdx.files.internal("shadow1.png"));
            this.shadow = new Sprite(shadowText, 0, 0, 16, 16);

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
            this.spriteAnim.add(Pokemon.this.standingSprites);
            this.spriteAnim.add(Pokemon.this.standingSprites);
            this.spriteAnim.add(Pokemon.this.movingSprites);
            this.spriteAnim.add(Pokemon.this.movingSprites);
            this.spriteAnim.add(Pokemon.this.movingSprites);
            this.spriteAnim.add(Pokemon.this.movingSprites);
            this.spriteAnim.add(Pokemon.this.standingSprites);
            this.spriteAnim.add(Pokemon.this.standingSprites);
            this.spriteAnim.add(Pokemon.this.standingSprites);
            this.spriteAnim.add(Pokemon.this.standingSprites);
            this.spriteAnim.add(Pokemon.this.altMovingSprites);
            this.spriteAnim.add(Pokemon.this.altMovingSprites);
            this.spriteAnim.add(Pokemon.this.altMovingSprites);
            this.spriteAnim.add(Pokemon.this.altMovingSprites);
            this.spriteAnim.add(Pokemon.this.standingSprites);
            this.spriteAnim.add(Pokemon.this.standingSprites);
        }

        public int getLayer(){return this.layer;}

        @Override
        public void firstStep(Game game) {
            // Play ledge jumping sound
            if (this.speed == 2) {
                game.insertAction(new PlaySound("ledge2", null));
            }
            else {
                game.insertAction(new PlaySound("ledge1", null));
            }
        }

        @Override
        public void step(Game game) {
            if (this.timer1 < 32/this.speed) {
                if (this.dirFacing.equals("up")) {
                    Pokemon.this.position.y +=1;
                }
                else if (this.dirFacing.equals("down")) {
                    Pokemon.this.position.y -=1;
                }
                else if (this.dirFacing.equals("left")) {
                    Pokemon.this.position.x -=1;
                }
                else if (this.dirFacing.equals("right")) {
                    Pokemon.this.position.x +=1;
                }
                if (this.timer1 % 2 == 1) {
                    Pokemon.this.position.y += this.yMovesList.remove(0);
                    // Use next sprite in list
                    Pokemon.this.currOwSprite = this.spriteAnim.remove(0).get(this.dirFacing);
                    for (int i=0; i < this.speed-1; i++) {
                        this.yMovesList.remove(0);
                        this.spriteAnim.remove(0);
                    }
                }

            }
            else {
                Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(this.dirFacing);
            }
            // Draw shadow
            game.mapBatch.draw(this.shadow, Pokemon.this.position.x, Pokemon.this.position.y-6);
            if (this.timer1 >= 38/this.speed) {
                Pokemon.this.position.set(this.targetPos);
                game.actionStack.remove(this);
                game.insertAction(this.nextAction);
                Pokemon.this.standingAction = this.nextAction;
                Pokemon.this.checkHabitat(game);
//                if (this.isFollowing) {
                    this.nextAction.step(game);  // gets rid of 'jitter'
//                }
            }

            this.timer1++;
        }
    }

    /**
     * Cacturne when it's mad.
     */
    public class Cacturnt extends Action {
        public int layer = 108;
        public int aggroTimer = 0;
        public boolean alternate = false;
        public int campfireDespawn = 0;
        Sprite tornadoSprite;

        public Cacturnt(Action nextAction) {
            this.nextAction = nextAction;
            Texture text = TextureCache.get(Gdx.files.internal("tornado_sheet1.png"));
            this.tornadoSprite = new Sprite(text, 0, 0, 16, 16);
        }

        public String getCamera() {
            return "map";
        }

        public int getLayer(){
            return this.layer;
        }

        @Override
        public void firstStep(Game game) {
            game.map.pokemon.put(Pokemon.this.position.cpy(), Pokemon.this);
            Pokemon.this.standingAction = this;
            Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
            game.insertAction(Pokemon.this.new DrawLower());
            game.insertAction(Pokemon.this.new DrawUpper());
            // Determine if pokemon near farm structures / habitat.
//            Pokemon.this.checkHabitat(game);
        }


        @Override
        public void step(Game game) {
            if (!game.playerCanMove) {
                return;
            }
            
            // Despawn if cacturne was near a campfire
            if (this.campfireDespawn > 0) {
                if (this.campfireDespawn == 60) {
                    Pokemon.this.dirFacing = "down";
                    Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
                    game.insertAction(Pokemon.this.new CactusSpawn(null, null));
                }

                if (this.campfireDespawn < 40) {
                    if (this.campfireDespawn % 4 == 3) {
                        this.tornadoSprite.setRegion(0, 0, 18, 16);
                    }
                    else if (this.campfireDespawn % 4 == 1) {
                        this.tornadoSprite.setRegion(18, 0, 18, 16);
                    }
                    game.mapBatch.draw(this.tornadoSprite, Pokemon.this.position.x-2, Pokemon.this.position.y+2);
                }
                
                // Remove the cacturne
                if (this.campfireDespawn <= 1) {
                    game.actionStack.remove(this);
                    game.map.pokemon.remove(Pokemon.this.position);
                    game.actionStack.remove(Pokemon.this.standingAction);
                    
                    // If no other cacturne, then 
                    boolean foundCacturnt = false;
                    for (Action action : game.actionStack) {
                        if (action instanceof Cacturnt) {
                            foundCacturnt = true;
                            break;
                        }
                    }
                    if (!foundCacturnt) {
                        game.musicController.nightAlert = false;
                        game.musicController.resumeOverworldMusic = true;
                    }
                }

                this.campfireDespawn--;
                return;
            }
            

            if (game.map.timeOfDay.equals("day")) {
                this.campfireDespawn = 60;
                return;
            }
            
            if (this.aggroTimer > 240) {
//                this.aggroTimer = 32;
                this.aggroTimer = 0;
            }
            this.aggroTimer++;

            // Play skull emote + pokemon cry every so often
            if (this.aggroTimer == 1) {
                game.insertAction(Pokemon.this.new Emote("skull", null));
            }

            float dst2 = Pokemon.this.position.dst2(game.player.position);
            // Play skull emote + pokemon cry every so often
            if (this.aggroTimer == 1) {
                game.insertAction(new PlaySound(Pokemon.this, true, null));
            }
            // Wait before moving
            // Play sounds
            if (this.aggroTimer < 4) {
                if (this.aggroTimer == 0) {
                    game.insertAction(new PlaySound("ride1", 0.5f, true, null));
                }
                Pokemon.this.currOwSprite = Pokemon.this.altMovingSprites.get(Pokemon.this.dirFacing);
                return;
            }
            else if (this.aggroTimer < 8) {
                Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
                return;
            }
            else if (this.aggroTimer < 12) {
                if (this.aggroTimer == 8) {
                    game.insertAction(new PlaySound("ride1", 0.5f, true, null));
                }
                Pokemon.this.currOwSprite = Pokemon.this.movingSprites.get(Pokemon.this.dirFacing);
                return;
            }
            else if (this.aggroTimer < 16) {
                Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
                return;
            }
            else if (this.aggroTimer < 20) {
                if (this.aggroTimer == 16) {
                    game.insertAction(new PlaySound("ride1", 0.5f, true, null));
                }
                Pokemon.this.currOwSprite = Pokemon.this.altMovingSprites.get(Pokemon.this.dirFacing);
                return;
            }
            else if (this.aggroTimer < 24) {
                Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
                return;
            }
            else if (this.aggroTimer < 28) {
                if (this.aggroTimer == 24) {
                    game.insertAction(new PlaySound("ride1", 0.5f, true, null));
                }
                Pokemon.this.currOwSprite = Pokemon.this.movingSprites.get(Pokemon.this.dirFacing);
                return;
            }
            else if (this.aggroTimer < 32) {
                Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
                return;
            }

            // Get dir towards player
            ArrayList<String> preferredMoves = new ArrayList<String>();

            float dx = Pokemon.this.position.x - game.player.position.x;
            float dy = Pokemon.this.position.y - game.player.position.y;
            if (dx < dy) {
                if (game.player.position.y < Pokemon.this.position.y) {
                    preferredMoves.add("down");
                    preferredMoves.add("right");
                }
                else {
                    preferredMoves.add("right");
                    preferredMoves.add("up");
                }
            }
            else {
                if (game.player.position.x < Pokemon.this.position.x) {
                    preferredMoves.add("left");
                    preferredMoves.add("down");
                }
                else {
                    preferredMoves.add("up");
                    preferredMoves.add("left");
                }
            }
            
//            if (Pokemon.this.position.x < game.player.position.x) {
//                preferredMoves.add("right");
//                preferredMoves.add("up");
//            }
//            else if (Pokemon.this.position.y > game.player.position.y) {
//                preferredMoves.add("down");
//                preferredMoves.add("right");
//            }
//            else if (Pokemon.this.position.x > game.player.position.x){
//                preferredMoves.add("left");
//                preferredMoves.add("down");
//            }
//            else if (Pokemon.this.position.y < game.player.position.y) {
//                preferredMoves.add("up");
//                preferredMoves.add("left");
//            }
//            else {
//                if (Pokemon.this.position.y < game.player.position.y) {
//                    preferredMoves.add("up");
//                }
//                else { //if (Pokemon.this.position.y > game.player.position.y){
//                    preferredMoves.add("down");
//                }
//            }
//            preferredMoves.add("up");
//            if (preferredMoves.size() < 2 && Pokemon.this.position.y > game.player.position.y) {
//                preferredMoves.remove("up");
//                preferredMoves.add("down");
//            }
//            if (preferredMoves.isEmpty()) {
//                if (Pokemon.this.position.x < game.player.position.x) {
//                    preferredMoves.add("up");
//                }
//                if (Pokemon.this.position.y < game.player.position.y) {
//                    preferredMoves.add("right");
//                }
//            }
            // flip-flop elements randomly
//            if (!preferredMoves.isEmpty() && Game.rand.nextInt(256) < 128) {
//                preferredMoves.add(preferredMoves.remove(0));
//            }
            // If standing on top of player, then initiate battle.
            if (dst2 < 64 &&
                this.aggroTimer > 32 &&
                !game.player.isFlying &&
                game.player.acceptInput) {
                game.playerCanMove = false;
                this.aggroTimer = 0;
//                game.musicController.startBattle = "wild";
                game.battle.oppPokemon = Pokemon.this;
                game.player.setCurrPokemon();
                game.insertAction(Battle.getIntroAction(game));
                return;
            }
            for (String move : preferredMoves) {
                Vector2 newPos = Pokemon.this.facingPos(move);
                Tile facingTile = Pokemon.this.mapTiles.get(newPos);
                Tile currTile = Pokemon.this.mapTiles.get(Pokemon.this.position);
                boolean isLedge = (facingTile != null && facingTile.attrs.get("ledge")) ||
                                  (currTile != null && currTile.attrs.get("ledge") && currTile.ledgeDir.equals("up") && move.equals("up"));
                if (!facingTile.attrs.get("solid") &&
                    !isLedge &&
                    !facingTile.name.contains("door") &&
                    !facingTile.nameUpper.contains("door") &&
                    !game.map.pokemon.containsKey(newPos)) {
                    

                    // Find out if near campfire
                    Vector2 startPos = Pokemon.this.position.cpy().add(-80, -80);
                    startPos.x = (int)startPos.x - (int)startPos.x % 16;
                    startPos.y = (int)startPos.y - (int)startPos.y % 16;
                    Vector2 endPos = Pokemon.this.position.cpy().add(80, 80);
                    endPos.x = (int)endPos.x - (int)endPos.x % 16;
                    endPos.y = (int)endPos.y - (int)endPos.y % 16;
                    for (Vector2 currPos = new Vector2(startPos.x, startPos.y); currPos.y < endPos.y;) {
                        Tile tile = game.map.tiles.get(currPos);
                        currPos.x += 16;
                        if (currPos.x > endPos.x) {
                            currPos.x = startPos.x;
                            currPos.y += 16;
                        }
                        if (tile == null) {
                            continue;
                        }
                        if (tile.nameUpper.contains("campfire")) {
                            this.campfireDespawn = 200;
                            return;
                        }
                        else if (tile.items().containsKey("torch") &&
                                 Pokemon.this.position.dst2(tile.position) < 1024) {
                            // TODO: probably only check if within small radius
                            this.campfireDespawn = 200;
                            return;
                        }
                        else if (game.map.pokemon.containsKey(currPos)) {
                            Pokemon pokemon = game.map.pokemon.get(currPos);
                            if (pokemon.hms.contains("FLASH")) {
                                this.campfireDespawn = 200;
                                return;
                            }
                        }
                    }

                    // Avoid pokemon walking over each other
                    if (game.map.pokemon.containsKey(Pokemon.this.position) && game.map.pokemon.get(Pokemon.this.position).equals(Pokemon.this)) {
                        game.map.pokemon.remove(Pokemon.this.position);
                    }
                    game.map.pokemon.put(newPos.cpy(), Pokemon.this);

                    // Move like normal if didn't find a campfire
                    Pokemon.this.dirFacing = move;
                    game.actionStack.remove(this);
                    Action action = Pokemon.this.new Moving(1, 1.5f, this.alternate, this);
                    this.alternate = !this.alternate;
                    game.insertAction(action);
                    Pokemon.this.standingAction = action;
                    break;
                }
            }

            // TODO: if haven't moved in a while, choose a different dir
            // other than moveDir
            
            // TODO: give up if enough time passed or haven't moved in a while.

            return;
        }
    }

    /**
     */
    public class CactusSpawn extends Action {
        public int layer = 116;
        Tile tile;
        int timer = 0;

        public CactusSpawn(Tile tile, Action nextAction) {
            this.tile = tile;
            this.nextAction = nextAction;
        }

        public String getCamera() {
            return "map";
        }

        public int getLayer(){
            return this.layer;
        }
        
        @Override
        public void firstStep(Game game) {
        }
        
        @Override
        public void step(Game game) {
            if (this.timer % 4 < 2) {
                // TODO: remove maybe
//                game.mapBatch.draw(this.tile.overSprite, this.tile.overSprite.getX(), this.tile.overSprite.getY());
                Pokemon.this.drawThisFrame = false;
            }
            else {
//                Pokemon.this.drawThisFrame = true;
            }

            if (this.timer > 60) {
                game.actionStack.remove(this);
                game.insertAction(this.nextAction);
            }
            this.timer++;
        }
    }

    /**
     */
    public class Burrowed extends Action {
        public int layer = 119;
        
        Sprite whirlpoolSprite;
        int whirlTimer = 0;
        boolean popOut = false;
        int jumpTimer = 0;
        int offsetY = 0;
        Sprite spritePart;
        Sprite sandSprite;
        Sprite trapinchSprite;

        public Burrowed() {
            Texture text = TextureCache.get(Gdx.files.internal("whirlpool_desert2.png"));
            this.whirlpoolSprite = new Sprite(text, 0, 0, 16, 16);
            text = TextureCache.get(Gdx.files.internal("grass_over_sheet3.png"));
            this.sandSprite = new Sprite(text, 0, 0, 16, 16);

            text = TextureCache.get(Gdx.files.internal("trapinch_ow1.png"));
            this.trapinchSprite = new Sprite(text, 0, 0, 16, 16);
        }

        public String getCamera() {
            return "map";
        }

        public int getLayer(){
            return this.layer;
        }
        
        @Override
        public void firstStep(Game game) {
            Pokemon.this.dirFacing = "right";
//            Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
            Pokemon.this.currOwSprite = this.trapinchSprite;
            game.insertAction(this.new DrawUpper());
            Pokemon.this.standingAction = this;
        }
        
        @Override
        public void step(Game game) {

            if (game.player.position.equals(Pokemon.this.position)) {
                this.popOut = true;
            }
            if (this.popOut) {

                if (this.jumpTimer < 30 +20) {
                    this.jumpTimer++;

                    if (this.jumpTimer % 8 == 0) {
                        game.insertAction(new PlaySound("sounds/move_object", 0.6f, true, null));
                    }
                }
                else if (this.jumpTimer < 33 +20) {
                    this.jumpTimer++;
                    this.offsetY+=4;
                    if (this.jumpTimer % 8 < 4) {
//                        Pokemon.this.currOwSprite = Pokemon.this.movingSprites.get(Pokemon.this.dirFacing);
                        this.trapinchSprite.setRegion(0, 0, 16, 16);
                    }
                    else {
//                        Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
                        this.trapinchSprite.setRegion(16, 0, 16, 16);
                    }

                }
                else if (this.jumpTimer < 36 +30) {
                    this.jumpTimer++;
                }
                else if (!game.player.position.equals(Pokemon.this.position)) {
                    this.jumpTimer++;
                    if (this.jumpTimer < 120) {
                        
                    }
                    else if (this.jumpTimer < 96 +60) {
                        if (this.jumpTimer % 8 < 4) {
//                            Pokemon.this.currOwSprite = Pokemon.this.movingSprites.get(Pokemon.this.dirFacing);
                            this.trapinchSprite.setRegion(0, 0, 16, 16);
                        }
                        else {
//                            Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
                            this.trapinchSprite.setRegion(16, 0, 16, 16);
                        }
                        if (this.jumpTimer % 8 == 0) {
                            game.insertAction(new PlaySound("sounds/move_object", 0.6f, true, null));
                        }
                        if (this.jumpTimer >= 81+60) {
                            this.offsetY--;
                        }
                    }
                    
                    if (this.jumpTimer >= 92+60) {
                        this.popOut = false;
                        this.jumpTimer = 0;
                    }
                }
                
                
                if (this.jumpTimer >= 33 +30 &&
                    game.player.position.equals(Pokemon.this.position) &&
                    game.playerCanMove) {  // TODO: this will instantly retrigger battle
                    // initiate battle
                    game.playerCanMove = false;
                    game.musicController.startBattle = "wild";
                    game.battle.oppPokemon = Pokemon.this;
                    game.player.setCurrPokemon();
                    game.insertAction(Battle.getIntroAction(game));
                }

                if (this.jumpTimer == 34) {
                    game.insertAction(new PlaySound(Pokemon.this, null));
                }
            }

            if (this.jumpTimer > 0) {
                if (this.whirlTimer == 0) {
                    this.whirlpoolSprite.setRegion(0, 0, 16, 16);
                }
                else if (this.whirlTimer == 40) {
                    this.whirlpoolSprite.setRegion(16, 0, 16, 16);
                    
                }
                game.mapBatch.draw(this.whirlpoolSprite,
                                   Pokemon.this.position.x,
                                   Pokemon.this.position.y);
            }

            this.whirlTimer++;
            if (this.whirlTimer >= 80) {
                this.whirlTimer = 0;
            }
        }
        
        /**
         * Drawn above player unlike normal with pokemon.
         */
        class DrawUpper extends Action {

            public int layer = 114;
            Sprite spritePart;
            
            public String getCamera() {
                return "map";
            }

            public int getLayer(){
                return this.layer;
            }
            
            public DrawUpper() {
                
            }

            @Override
            public void step(Game game) {
                if (!game.actionStack.contains(Burrowed.this)) {
                    game.actionStack.remove(this);
                }
                this.spritePart = new Sprite(Pokemon.this.currOwSprite);
//                this.spritePart.setRegionY(Pokemon.this.spriteOffsetY);
                this.spritePart.setRegionHeight(Burrowed.this.offsetY);
                game.mapBatch.draw(this.spritePart, Pokemon.this.position.x, Pokemon.this.position.y+6);  //x+4
                

                // Small sand ripple animation
                if (Burrowed.this.jumpTimer < 30 +20) {
                    
                }
                else if (Burrowed.this.jumpTimer < 30 +36) {
                    if (Burrowed.this.jumpTimer == 30 +20) {
                        Burrowed.this.sandSprite.setRegion(0, 0, 16, 16);
                    }
                    else if (Burrowed.this.jumpTimer == 30 +28) {
                        Burrowed.this.sandSprite.setRegion(16, 0, 16, 16);
                    }
                    game.mapBatch.draw(Burrowed.this.sandSprite, Pokemon.this.position.x, Pokemon.this.position.y+6);  //x+4
                }
                
            }
        }
    }

    /**
     * Pokemon standing in place (overworld), moving periodically.
     * TODO: consider just making Pokemon extend Action, then move this stuff out one level.
     */
    public class Standing extends Action {
        public int layer = 130;
        int moveTimer = 0;
        // Time until run animation starts
        int runTimer = 0;
        int danceCounter = 240;  // don't make this = 0, game will crash trying to play hop sound effects
        boolean alternate = true;
        public int aggroTimer = 0;
        public boolean isEgg = false;

        // TODO: migrate to start using this. 
        // Ideally in 'class OverworldThing' but sep for
        // Pokemon / Player shouldn't be too big of a deal.
//        public Direction test = UP;  
        
        public Standing() {}

        public String getCamera() {
            return "map";
        }

        public int getLayer(){
            return this.layer;
        }

        @Override
        public void firstStep(Game game) {
            this.moveTimer = game.map.rand.nextInt(180) + 60;
            game.map.pokemon.put(Pokemon.this.position.cpy(), Pokemon.this);
            Pokemon.this.standingAction = this;
            Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
            game.insertAction(Pokemon.this.new DrawLower());
            game.insertAction(Pokemon.this.new DrawUpper());
            // Determine if pokemon near farm structures / habitat.
            Pokemon.this.checkHabitat(game);
            // TODO: this is to avoid computationally intensive check
            // ideally pokemon names should be enums. altho 
            // that makes it quite a bit clunkier.
            // Right now, if pokemon hatches from egg in overworld
            // this will remain frozen. But that's okay b/c they
            // can't hatch in the overworld.
            this.isEgg = Pokemon.this.name.equals("egg");
        }

        public void localStep(Game game) {
            // Eggs don't move
            if (this.isEgg) {
                // Play 'hop' animation if close to hatching
//                if (Pokemon.this.happiness <= 1) {
//                if (Pokemon.this.happiness <= 3) {  // TODO: not doing for now
                if (this.danceCounter < 4) {
                    Pokemon.this.spriteOffsetY = 16;
                    Pokemon.this.currOwSprite = Pokemon.this.avatarSprites.get(1);
                }
                else if (this.danceCounter < 16) {
                    Pokemon.this.spriteOffsetY = 0;
                    Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get("down");
                }
                else if (this.danceCounter < 20) {
                    Pokemon.this.spriteOffsetY = 16;
                    Pokemon.this.currOwSprite = Pokemon.this.avatarSprites.get(1);
                }
                else if (this.danceCounter < 70 +20) {
                    Pokemon.this.spriteOffsetY = 0;
                    Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get("down");
                }
                else if (this.danceCounter < 74 +20) {
                    Pokemon.this.spriteOffsetY = 32;
                    Pokemon.this.currOwSprite = Pokemon.this.movingSprites.get("down");
                }
                else if (this.danceCounter < 78 +20) {
                    Pokemon.this.spriteOffsetY = 0;
                    Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get("down");
                }
                else if (this.danceCounter < 82 +20) {
                    Pokemon.this.spriteOffsetY = 48;
                    Pokemon.this.currOwSprite = Pokemon.this.altMovingSprites.get("down");
                }
                else if (this.danceCounter < 86 +20) {
                    Pokemon.this.spriteOffsetY = 0;
                    Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get("down");
                }
                else if (this.danceCounter < 90 +20) {
                    Pokemon.this.spriteOffsetY = 32;
                    Pokemon.this.currOwSprite = Pokemon.this.movingSprites.get("down");
                }
                else if (this.danceCounter < 94 +20) {
                    Pokemon.this.spriteOffsetY = 0;
                    Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get("down");
                }
                else if (this.danceCounter < 98 +20) {
                    Pokemon.this.spriteOffsetY = 48;
                    Pokemon.this.currOwSprite = Pokemon.this.altMovingSprites.get("down");
                }
                else {
                    Pokemon.this.spriteOffsetY = 0;
                    Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get("down");
                }
                this.danceCounter++;
                // Hop more frequently the closer pokemon is to hatching
                if (this.danceCounter >= 240 +30*(Pokemon.this.happiness-1)) {
                    this.danceCounter = 0;
                }
//                }
                return;
            }
            Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);

            // If pokemon is aggro-ing the player, run towards the player
            if (Pokemon.this.aggroPlayer) {
                if (!game.playerCanMove) {
                    return;
                }
                if (this.aggroTimer > 240) {
//                    this.aggroTimer = 32;
                    this.aggroTimer = 0;
                }
                this.aggroTimer++;

                // Play skull emote + pokemon cry every so often
                if (this.aggroTimer == 1) {
                    game.insertAction(Pokemon.this.new Emote("skull", null));
                }

                float dst2 = Pokemon.this.position.dst2(game.player.position);
                if (dst2 < 16384) {  //12544) {  //9216) {  //4096) {
                    // Play skull emote + pokemon cry every so often
                    if (this.aggroTimer == 1) {
                        game.insertAction(new PlaySound(Pokemon.this, null));
                    }
                    // Wait before moving
                    // Play sounds
                    if (this.aggroTimer < 4) {
                        if (this.aggroTimer == 0) {
                            game.insertAction(new PlaySound("ride1", 0.5f, true, null));
                        }
                        Pokemon.this.currOwSprite = Pokemon.this.altMovingSprites.get(Pokemon.this.dirFacing);
                        return;
                    }
                    else if (this.aggroTimer < 8) {
                        Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
                        return;
                    }
                    else if (this.aggroTimer < 12) {
                        if (this.aggroTimer == 8) {
                            game.insertAction(new PlaySound("ride1", 0.5f, true, null));
                        }
                        Pokemon.this.currOwSprite = Pokemon.this.movingSprites.get(Pokemon.this.dirFacing);
                        return;
                    }
                    else if (this.aggroTimer < 16) {
                        Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
                        return;
                    }
                    else if (this.aggroTimer < 20) {
                        if (this.aggroTimer == 16) {
                            game.insertAction(new PlaySound("ride1", 0.5f, true, null));
                        }
                        Pokemon.this.currOwSprite = Pokemon.this.altMovingSprites.get(Pokemon.this.dirFacing);
                        return;
                    }
                    else if (this.aggroTimer < 24) {
                        Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
                        return;
                    }
                    else if (this.aggroTimer < 28) {
                        if (this.aggroTimer == 24) {
                            game.insertAction(new PlaySound("ride1", 0.5f, true, null));
                        }
                        Pokemon.this.currOwSprite = Pokemon.this.movingSprites.get(Pokemon.this.dirFacing);
                        return;
                    }
                    else if (this.aggroTimer < 32) {
                        Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
                        return;
                    }

                    // Get dir towards player
                    ArrayList<String> preferredMoves = new ArrayList<String>();
                    if (Pokemon.this.position.x < game.player.position.x) {
                        preferredMoves.add("right");
                    }
                    else if (Pokemon.this.position.x > game.player.position.x){
                        preferredMoves.add("left");
                    }
                    if (Pokemon.this.position.y < game.player.position.y) {
                        preferredMoves.add("up");
                    }
                    else if (Pokemon.this.position.y > game.player.position.y) {
                        preferredMoves.add("down");
                    }
                    // flip-flop elements randomly
                    if (!preferredMoves.isEmpty() && Game.rand.nextInt(256) < 128) {
                        preferredMoves.add(preferredMoves.remove(0));
                    }
                    // If standing on top of player, then initiate battle.
                    if (dst2 < 64 &&
                        this.aggroTimer > 32 &&
                        !game.player.isFlying &&
                        game.player.acceptInput) {
                        game.playerCanMove = false;
                        this.aggroTimer = 0;
                        game.musicController.startBattle = "wild";
                        game.battle.oppPokemon = Pokemon.this;
                        game.player.setCurrPokemon();
                        game.insertAction(Battle.getIntroAction(game));
                        return;
                    }
                    for (String move : preferredMoves) {
                        Vector2 newPos = Pokemon.this.facingPos(move);
                        Tile facingTile = Pokemon.this.mapTiles.get(newPos);
                        Tile currTile = Pokemon.this.mapTiles.get(Pokemon.this.position);
                        boolean isLedge = (facingTile != null && facingTile.attrs.get("ledge")) ||
                                          (currTile != null && currTile.attrs.get("ledge") && currTile.ledgeDir.equals("up") && move.equals("up"));
                        if (!facingTile.attrs.get("solid") &&
                            !isLedge &&
                            !facingTile.name.contains("door") &&
                            !facingTile.nameUpper.contains("door") &&
                            !game.map.pokemon.containsKey(newPos)) {
                            Pokemon.this.dirFacing = move;
                            game.actionStack.remove(this);
//                            Action action = Pokemon.this.new Moving(1, 2, this.alternate, this);  // pretty fast
                            Action action = Pokemon.this.new Moving(1, 1.5f, this.alternate, this);
                            this.alternate = !this.alternate;
                            game.insertAction(action);
                            Pokemon.this.standingAction = action;
                            break;
                        }
                    }

                    // TODO: if haven't moved in a while, choose a different dir
                    // other than moveDir
                    
                    // TODO: give up if enough time passed or haven't moved in a while.

                    return;
                }
            }

            if (this.runTimer <= 0 && !Pokemon.this.isRunning) {
                // If player is running nearby and pokemon happiness is low, have it run away.
                boolean nearPlayer = false;
                for (Vector2 currPos = Pokemon.this.position.cpy().add(-64, -64); currPos.y < Pokemon.this.position.y+65;) {
                    if (currPos.x > Pokemon.this.position.x+64) {
                        currPos.x = Pokemon.this.position.x-64;
                        currPos.y += 16;
                        continue;
                    }
                    if (game.player.position.equals(currPos)) {
                        nearPlayer = true;
                        break;
                    }
                    currPos.x += 16;
                }
                if (Pokemon.this.previousOwner != game.player && Pokemon.this.happiness <= 0 && nearPlayer) {
                    this.runTimer = 180;
                }
            }

            if (this.runTimer > 0 && !Pokemon.this.isRunning) {
                if (this.runTimer == 170) {
                    game.insertAction(Pokemon.this.new Emote("!", null));
                    game.insertAction(new PlaySound("ledge2", null));
                }
                if (this.runTimer == 110) {
                    Pokemon.this.dirFacing = "left";
                }
                if (this.runTimer == 100) {
                    Pokemon.this.dirFacing = "right";
                }
                if (this.runTimer == 90) {
                    Pokemon.this.dirFacing = "left";
                }
                if (this.runTimer == 80) {
                    Pokemon.this.dirFacing = "right";
                }
                if (this.runTimer == 70) {
                    Pokemon.this.isRunning = true;
                }
//                if (this.runTimer == 110) {
//                    Pokemon.this.dirFacing = "left";
//                }
//                if (this.runTimer == 80) {
//                    Pokemon.this.dirFacing = "right";
//                }
//                if (this.runTimer == 50) {
//                    Pokemon.this.dirFacing = "left";
//                }
//                if (this.runTimer == 20) {
//                    Pokemon.this.dirFacing = "left";
//                }
//                if (this.runTimer == 1) {
//                    Pokemon.this.isRunning = true;
//                }
                this.runTimer--;
                return;
            }
            if (Pokemon.this.isRunning) {
                // If the pokemon encounters grass, it 'hides' in the grass.
                if (Pokemon.this.mapTiles.containsKey(Pokemon.this.position)) {
                    Tile tile = Pokemon.this.mapTiles.get(Pokemon.this.position);
                    if (tile.attrs.get("grass")) {
                        Pokemon.this.isRunning = false;
                        tile.routeBelongsTo.pokemon.add(Pokemon.this);
                        game.map.pokemon.remove(Pokemon.this.position);
                        game.actionStack.remove(Pokemon.this.standingAction);
                        return;
                    }
                }
                // Calculate direction to player, face opposite of that direction
                float dx;
                float dy;
                ArrayList<String> preferMoves = new ArrayList<String>();
                String moveDir = null;
                for (Vector2 tl = Pokemon.this.position.cpy().add(-96, -96); tl.y < Pokemon.this.position.y+96; tl.x += 16) {
                    if (tl.x > Pokemon.this.position.x+96) {
                        tl.x = Pokemon.this.position.x-112;
                        tl.y += 16;
                        continue;
                    }
                    Tile tile = Pokemon.this.mapTiles.get(tl);
                    if (tile != null && tile.attrs.get("grass") && game.map.rand.nextInt(5) == 0) {
//                        System.out.println("grass");
                        dx = Pokemon.this.position.x - tile.position.x;
                        dy = Pokemon.this.position.y - tile.position.y;
                        if (dx < dy) {
                            if (tile.position.y < Pokemon.this.position.y) {
                                preferMoves.add("down");
                            }
                            else {
                                preferMoves.add("right");
                            }
                        }
                        else {
                            if (tile.position.x < Pokemon.this.position.x) {
                                preferMoves.add("left");
                            }
                            else {
                                preferMoves.add("up");
                            }
                        }
                        break;
                    }
                }
                dx = Pokemon.this.position.x - game.player.position.x;
                dy = Pokemon.this.position.y - game.player.position.y;
                if (dx < dy) {
                    Vector2 up = new Vector2(Pokemon.this.position.x, Pokemon.this.position.y+16);
                    Tile upTile = Pokemon.this.mapTiles.get(up);
                    Vector2 left = new Vector2(Pokemon.this.position.x-16, Pokemon.this.position.y);
                    Tile leftTile = Pokemon.this.mapTiles.get(left);
                    if (game.player.position.y < Pokemon.this.position.y) {
                        preferMoves.add("up");
                    }
                    else if (!leftTile.attrs.get("solid")) {
                        preferMoves.add("left");
                    }
                }
                Vector2 right = new Vector2(Pokemon.this.position.x+16, Pokemon.this.position.y);
                Tile rightTile = Pokemon.this.mapTiles.get(right);
                Vector2 down = new Vector2(Pokemon.this.position.x, Pokemon.this.position.y-16);
                Tile downTile = Pokemon.this.mapTiles.get(down);
                if (game.player.position.x < Pokemon.this.position.x) {
                    preferMoves.add("right");
                }
                else {
                    preferMoves.add("down");
                }
                Vector2 up = new Vector2(Pokemon.this.position.x, Pokemon.this.position.y+16);
                Tile upTile = Pokemon.this.mapTiles.get(up);
                Vector2 left = new Vector2(Pokemon.this.position.x-16, Pokemon.this.position.y);
                Tile leftTile = Pokemon.this.mapTiles.get(left);
                if (game.player.position.y < Pokemon.this.position.y) {
                    preferMoves.add("up");
                }
                else {
                    preferMoves.add("left");
                }
                for (String move : preferMoves) {
                    if (move.equals("up") && 
                        !upTile.attrs.get("solid") && 
                        !upTile.attrs.get("ledge") && 
                        !upTile.name.contains("door")) {
                        moveDir = move;
                        break;
                    }
                    else if (move.equals("right") && 
                             !rightTile.attrs.get("solid") && 
                             !rightTile.attrs.get("ledge") && 
                             !rightTile.name.contains("door")) {
                        moveDir = move;
                        break;
                    }
                    else if (move.equals("down") &&
                            !downTile.attrs.get("solid") && 
                            !downTile.attrs.get("ledge") && 
                            !downTile.name.contains("door")) {
                       moveDir = move;
                       break;
                    }
                    else if (move.equals("left") &&
                            !leftTile.attrs.get("solid") && 
                            !leftTile.attrs.get("ledge") && 
                            !leftTile.name.contains("door")) {
                       moveDir = move;
                       break;
                    }
                }
                if (moveDir != null) {
                    Pokemon.this.dirFacing = moveDir;
                    game.actionStack.remove(this);
                    Action action = Pokemon.this.new Moving(1, 2, this.alternate, this);
                    this.alternate = !this.alternate;
                    game.insertAction(action);
                    Pokemon.this.standingAction = action;
                }
                return;
            }

            // If pokemon has a harvestable item, then have it 'dance'
            if (Pokemon.this.hasItem != null) {
                this.danceCounter++;
                if (this.danceCounter <= 16) {
                    Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
                }
                else if (this.danceCounter <= 32) {
                    Pokemon.this.currOwSprite = Pokemon.this.movingSprites.get(Pokemon.this.dirFacing);
                }
                else if (this.danceCounter <= 48) {
                    Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
                }
                else if (this.danceCounter < 64) {
                    Pokemon.this.currOwSprite = Pokemon.this.altMovingSprites.get(Pokemon.this.dirFacing);
                }
                else {
                    this.danceCounter = 0;
                }
            }

            if (!Pokemon.this.canMove) {
                return;
            }
            // TODO: remove
//            game.mapBatch.draw(Pokemon.this.standingSprites.get(Pokemon.this.dirFacing),
//                               Pokemon.this.position.x, Pokemon.this.position.y+4);
            if (this.moveTimer <= 0) {
                this.moveTimer = game.map.rand.nextInt(180) + 60;
                String[] dirs = new String[]{"up", "down", "left", "right"};
                Pokemon.this.dirFacing = dirs[game.map.rand.nextInt(dirs.length)];
                Vector2 newPos = Pokemon.this.facingPos();
                // Just checking overworldTiles for now, that way it will still
                // move around while player is indoors
                Tile temp = Pokemon.this.mapTiles.get(newPos);
                boolean collidesWithPlayer = false;
                for (Player player : game.players.values()) {
                    if (newPos.equals(player.position)) {
                        collidesWithPlayer = true;
                        break;
                    }
                }
                Tile currTile = Pokemon.this.mapTiles.get(Pokemon.this.position);
                boolean isLedge = (temp != null && temp.attrs.get("ledge")) ||
                                  (currTile != null && currTile.attrs.get("ledge") && currTile.ledgeDir.equals("up") && Pokemon.this.dirFacing.equals("up"));
                if (temp == null ||
                    temp.attrs.get("solid") || 
                    isLedge ||
                    temp.name.contains("door") || 
                    temp.nameUpper.contains("door") || 
                    game.map.pokemon.containsKey(newPos) ||
                    // TODO: an indoor player will mess with an outdoor pokemon here
                    // technically doesn't matter for now b/c indoor tiles won't
                    // overlap with non-solid overworld areas.
                    game.player.position.equals(newPos) ||
                    collidesWithPlayer) {
                    return;
                }
                game.actionStack.remove(this);
                Action action = Pokemon.this.new Moving(2, 1, true, null);
//                if (game.map.rand.nextInt(2) == 0) {
//                    action.append(Pokemon.this.new Moving(false, null));
//                }
                action.append(this);
                game.insertAction(action);
                Pokemon.this.standingAction = action;
            }
            this.moveTimer--;
        }

        public void remoteStep(Game game) {
            Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
            if (Pokemon.this.shouldMove) {
                Pokemon.this.shouldMove = false;
                game.actionStack.remove(this);
                Action action = Pokemon.this.new Moving(2, 1, true, null);
                action.append(this);
                game.insertAction(action);
                Pokemon.this.standingAction = action;
            }
        }

        @Override
        public void step(Game game) {
            if (Pokemon.this.type == Player.Type.LOCAL) {
                this.localStep(game);
            }
            else {
                this.remoteStep(game);
            }
        }
    }

    /**
     * Specifies Generation from, ie RED, CRYSTAL (only those two currently).
     */
    // sprites, stats, attacks etc differ depending
    public enum Generation {
        RED,
        CRYSTAL
    }
}

class SpecialMegaGengar1 extends Pokemon {
    // TODO: remove, in base class
//    Sprite breathingSprite;

    public SpecialMegaGengar1(int level) {
        // initialize variables
        super("Mega Gengar", level);

        // gen 7 stats
//        this.baseStats.put("hp", 60);
        this.baseStats.put("hp", 300);
        this.baseStats.put("attack", 65);
        this.baseStats.put("defense", 80);
        this.baseStats.put("specialAtk", 170);
        this.baseStats.put("specialDef", 95);
        this.baseStats.put("speed", 130);
        // mega gengar doesn't have a catch rate, so leaving at 3
        // same as mewtwo
        this.baseStats.put("catchRate", 3);

        // sprite
        Texture pokemonText = TextureCache.get(Gdx.files.internal("pokemon/mgengar_base1.png"));
        this.breathingSprite = new Sprite(pokemonText, 0, 0, 56, 56);

        pokemonText = TextureCache.get(Gdx.files.internal("pokemon/mgengar_over1.png"));
        this.sprite = new SpriteProxy(pokemonText, 0, 0, 56, 56);

//        this.learnSet.put(1, new String[]{"Confusion", "Disable", "Psychic", "Swift"});
//        this.learnSet.put(1, new String[]{"Psychic", "Psychic", "Psychic", "Psychic"});
//        this.learnSet.put(1, new String[]{"Night Shade", "Night Shade", "Night Shade", "Night Shade"}); //, "Lick"
        this.learnSet.put(1, new String[]{"Shadow Claw", "Night Shade", "Lick", null}); //, "Lick"
        this.types.add("Ghost");
        this.types.add("Poison");

        getCurrentAttacks(); // fill this.attacks with what it can currently know

        // stats formulas here
        calcMaxStats();
        this.currentStats = new HashMap<String, Integer>(this.maxStats); // copy maxStats
        this.initHabitatValues();
    }
}

class SpecialMewtwo1 extends Pokemon {
    // Where the mewtwo is located on the map
    // Needed because catching mewtwo needs to despawn it, and fainting it needs to temporarily remove it.
    Tile tile;

    public SpecialMewtwo1(int level, Tile tile) {
        // initialize variables
//        super("Mewtwo", level);
        super("Mewtwo", level, Pokemon.Generation.CRYSTAL);
        this.tile = tile;
        // gen I properties
//        this.baseStats.put("hp", 106);
//        this.baseStats.put("attack", 110);
//        this.baseStats.put("defense", 90);
//        this.baseStats.put("specialAtk", 154);
//        this.baseStats.put("specialDef", 154);
//        this.baseStats.put("speed", 130);
//        this.baseStats.put("catchRate", 3);

        // sprite
        Texture pokemonText = TextureCache.get(Gdx.files.internal("pokemon/mewtwo_special1.png"));
        this.sprite = new SpriteProxy(pokemonText, 0, 0, 56, 56);

        pokemonText = TextureCache.get(Gdx.files.internal("pokemon/mewtwo_special2.png"));
        this.breathingSprite = new Sprite(pokemonText, 0, 0, 56, 56);

        // Gen 1 moves instead of Gen 2
        this.learnSet.clear();
        this.learnSet.put(1, new String[]{"disable", "psychic", "swift", "recover"});  //"confusion", 
        // Recover isn't in original moveset at lvl 1, but I want recover for a lvl 50 battle
//        this.learnSet.put(1, new String[]{"recover"});  
        this.learnSet.put(63, new String[]{"barrier"});
        this.learnSet.put(66, new String[]{"psychic"});
        this.learnSet.put(70, new String[]{"recover"});
        this.learnSet.put(75, new String[]{"mist"});
        this.learnSet.put(81, new String[]{"amnesia"});
//        this.learnSet.put(50, new String[]{"psychic", "psychic", "psychic", "psychic"});
//        this.types.add("Psychic");

        getCurrentAttacks(); // fill this.attacks with what it can currently know

        this.baseStats.put("catchRate", 55);
        this.initHabitatValues();

//        // stats formulas here
//        calcMaxStats();
//        this.currentStats = new HashMap<String, Integer>(this.maxStats); // copy maxStats
//        this.dexNumber = Pokemon.nameToIndex("mewtwo");
////        System.out.println(this.dexNumber);
    }
}