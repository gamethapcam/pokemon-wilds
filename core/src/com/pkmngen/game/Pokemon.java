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
    public static ArrayList<String> attacksImplemented = new ArrayList<String>();
    static {
        attacksImplemented.add("absorb");
        attacksImplemented.add("acid");
        attacksImplemented.add("acid armor");
        attacksImplemented.add("aeroblast");
        attacksImplemented.add("amnesia");
        attacksImplemented.add("ancientpower");
        attacksImplemented.add("attract");
        attacksImplemented.add("aurora beam");
        attacksImplemented.add("barrier");
        attacksImplemented.add("bind");
        attacksImplemented.add("bite");
        attacksImplemented.add("blizzard");
        attacksImplemented.add("body slam");
        attacksImplemented.add("bone club");
        attacksImplemented.add("bubble");
        attacksImplemented.add("bubblebeam");
        attacksImplemented.add("clamp");
        attacksImplemented.add("confuse ray");
        attacksImplemented.add("constrict");
        attacksImplemented.add("cotton spore");
        attacksImplemented.add("crabhammer");
        attacksImplemented.add("cross chop");
        attacksImplemented.add("crunch");
        attacksImplemented.add("cut");
        attacksImplemented.add("defense curl");
        attacksImplemented.add("disable");
        attacksImplemented.add("dizzy punch");
        attacksImplemented.add("double team");
        attacksImplemented.add("dragonbreath");
        attacksImplemented.add("drill peck");
        attacksImplemented.add("dynamicpunch");
        attacksImplemented.add("earthquake");
        attacksImplemented.add("egg bomb");
        attacksImplemented.add("ember");
        attacksImplemented.add("explosion");
        attacksImplemented.add("extreme speed");
        attacksImplemented.add("faint attack");
        attacksImplemented.add("false swipe");
        attacksImplemented.add("fire blast");
        attacksImplemented.add("fire punch");
        attacksImplemented.add("fire spin");
        attacksImplemented.add("flamethrower");
        attacksImplemented.add("flash");
        attacksImplemented.add("giga drain");
        attacksImplemented.add("glare");
        attacksImplemented.add("growl");
        attacksImplemented.add("growth");
        attacksImplemented.add("gust");
        attacksImplemented.add("harden");
        attacksImplemented.add("headbutt");
        attacksImplemented.add("hi jump kick");
        attacksImplemented.add("hidden power");
        attacksImplemented.add("horn attack");
        attacksImplemented.add("hydro pump");
        attacksImplemented.add("hyper beam");
        attacksImplemented.add("hyper fang");
        attacksImplemented.add("hypnosis");
        attacksImplemented.add("ice beam");
        attacksImplemented.add("ice punch");
        attacksImplemented.add("icy wind");
        attacksImplemented.add("iron tail");
        attacksImplemented.add("jump kick");
        attacksImplemented.add("karate chop");
        attacksImplemented.add("kinesis");
        attacksImplemented.add("leech life");
        attacksImplemented.add("leer");
        attacksImplemented.add("lick");
        attacksImplemented.add("lovely kiss");
        attacksImplemented.add("low kick");
        attacksImplemented.add("mach punch");
        attacksImplemented.add("meditate");
        attacksImplemented.add("mega drain");
        attacksImplemented.add("mega horn");
        attacksImplemented.add("mega kick");
        attacksImplemented.add("mega punch");
        attacksImplemented.add("metal claw");
        attacksImplemented.add("milk drink");
        attacksImplemented.add("minimize");
        attacksImplemented.add("moonlight");
        attacksImplemented.add("morning sun");
        attacksImplemented.add("mud slap");
        attacksImplemented.add("night shade");
        attacksImplemented.add("octazooka");
        attacksImplemented.add("outrage");
        attacksImplemented.add("payday");
        attacksImplemented.add("peck");
        attacksImplemented.add("poison gas");
        attacksImplemented.add("poison powder");
        attacksImplemented.add("poison sting");
        attacksImplemented.add("pound");
        attacksImplemented.add("powder snow");
        attacksImplemented.add("psybeam");
        attacksImplemented.add("psych up");
        attacksImplemented.add("psychic");
        attacksImplemented.add("psywave");
        attacksImplemented.add("pursuit");
        attacksImplemented.add("quick attack");
        attacksImplemented.add("rapid spin");
        attacksImplemented.add("razor leaf");
        attacksImplemented.add("razor wind");
        attacksImplemented.add("recover");
        attacksImplemented.add("return");
        attacksImplemented.add("rock slide");
        attacksImplemented.add("rock smash");
        attacksImplemented.add("rock throw");
        attacksImplemented.add("rolling kick");
        attacksImplemented.add("sacred fire");
        attacksImplemented.add("sand attack");
        attacksImplemented.add("scary face");
        attacksImplemented.add("scratch");
        attacksImplemented.add("screech");
        attacksImplemented.add("selfdestruct");
        attacksImplemented.add("shadow ball");
        attacksImplemented.add("sharpen");
        attacksImplemented.add("sing");
        attacksImplemented.add("slam");
        attacksImplemented.add("slash");
        attacksImplemented.add("sleep powder");
        attacksImplemented.add("sludge");
        attacksImplemented.add("sludge bomb");
        attacksImplemented.add("smog");
        attacksImplemented.add("smokescreen");
        attacksImplemented.add("softboiled");
        attacksImplemented.add("sonicboom");
        attacksImplemented.add("spark");
        attacksImplemented.add("splash");
        attacksImplemented.add("spore");
        attacksImplemented.add("steel wing");
        attacksImplemented.add("stomp");
        attacksImplemented.add("strength");
        attacksImplemented.add("string shot");
        attacksImplemented.add("struggle");
        attacksImplemented.add("stun spore");
        attacksImplemented.add("submission");
        attacksImplemented.add("super fang");
        attacksImplemented.add("supersonic");
        attacksImplemented.add("surf");
        attacksImplemented.add("sweet kiss");
        attacksImplemented.add("sweet scent");
        attacksImplemented.add("swift");
        attacksImplemented.add("swords dance");
        attacksImplemented.add("synthesis");
        attacksImplemented.add("tackle");
        attacksImplemented.add("tail whip");
        attacksImplemented.add("take down");
        attacksImplemented.add("thief");
        attacksImplemented.add("thunder");
        attacksImplemented.add("thunder punch");
        attacksImplemented.add("thunderbolt");
        attacksImplemented.add("thundershock");
        attacksImplemented.add("thunderwave");
        attacksImplemented.add("toxic");
        attacksImplemented.add("tri attack");
        attacksImplemented.add("twister");
        attacksImplemented.add("vice grip");
        attacksImplemented.add("vine whip");
        attacksImplemented.add("vital throw");
        attacksImplemented.add("water gun");
        attacksImplemented.add("waterfall");
        attacksImplemented.add("whirlpool");
        attacksImplemented.add("wing attack");
        attacksImplemented.add("withdraw");
        attacksImplemented.add("wrap");
        attacksImplemented.add("zap cannon");
        //
        attacksImplemented.add("dragon energy");
        attacksImplemented.add("thunder cage");
        attacksImplemented.add("crush grip");
        attacksImplemented.add("dazzling gleam");
        attacksImplemented.add("draining kiss");
        attacksImplemented.add("struggle bug");
        attacksImplemented.add("fairy wind");
        attacksImplemented.add("air cutter");

        // 'prism' attacks
        attacksImplemented.add("zen headbutt");
        attacksImplemented.add("iron defense");
        attacksImplemented.add("bug buzz");
        attacksImplemented.add("dragon pulse");
        attacksImplemented.add("drain punch");
        attacksImplemented.add("hyper voice");
        attacksImplemented.add("nasty plot");
        attacksImplemented.add("energy ball");
        attacksImplemented.add("astonish");
        attacksImplemented.add("flash cannon");
        attacksImplemented.add("will o wisp");
        attacksImplemented.add("seed bomb");
        attacksImplemented.add("night slash");
        attacksImplemented.add("poison jab");
        attacksImplemented.add("signal beam");
        attacksImplemented.add("meteor mash");
        attacksImplemented.add("dragon claw");
        attacksImplemented.add("iron head");
        attacksImplemented.add("power gem");
        attacksImplemented.add("air slash");
        attacksImplemented.add("dark pulse");
        attacksImplemented.add("earth power");
        attacksImplemented.add("psycho cut");
        attacksImplemented.add("shadow claw");
        attacksImplemented.add("play rough");
    }

    // TODO: remove
//    public static ArrayList<String> attacksNotImplemented = new ArrayList<String>();
//    static {
//        attacksNotImplemented.add("barrage");
//        attacksNotImplemented.add("baton pass");
//        attacksNotImplemented.add("beat Up");
//        attacksNotImplemented.add("belly drum");
//        attacksNotImplemented.add("bide");
//        attacksNotImplemented.add("bind");
//        attacksNotImplemented.add("bone rush");
//        attacksNotImplemented.add("bonemerang");
//        attacksNotImplemented.add("charm");
//        attacksNotImplemented.add("comet punch");
//        attacksNotImplemented.add("conversion");
//        attacksNotImplemented.add("conversion 2");
//        attacksNotImplemented.add("counter");
//        attacksNotImplemented.add("curse");
//        attacksNotImplemented.add("protect");
//        attacksNotImplemented.add("destiny bond");
//        attacksNotImplemented.add("detect");
//        attacksNotImplemented.add("dig");
//        attacksNotImplemented.add("double edge");
//        attacksNotImplemented.add("double kick");
//        attacksNotImplemented.add("double slap");
//        attacksNotImplemented.add("dragon rage");
//        attacksNotImplemented.add("dream eater");
//        attacksNotImplemented.add("encore");
//        attacksNotImplemented.add("endure");
//        attacksNotImplemented.add("fissure");
//        attacksNotImplemented.add("flail");
//        attacksNotImplemented.add("flame wheel");
//        attacksNotImplemented.add("fly");
//        attacksNotImplemented.add("focus energy");
//        attacksNotImplemented.add("foresight");
//        attacksNotImplemented.add("frustration");
//        attacksNotImplemented.add("fury attack");
//        attacksNotImplemented.add("fury swipes");
//        attacksNotImplemented.add("future sight");
//        attacksNotImplemented.add("guillotine");
//        attacksNotImplemented.add("haze");
//        attacksNotImplemented.add("heal bell");
//        attacksNotImplemented.add("horn drill");
//        attacksNotImplemented.add("leech seed");
//        attacksNotImplemented.add("light screen");
//        attacksNotImplemented.add("lock on");
//        attacksNotImplemented.add("magnitude");
//        attacksNotImplemented.add("mean look");
//        attacksNotImplemented.add("metronome");
//        attacksNotImplemented.add("mimic");
//        attacksNotImplemented.add("mind reader");
//        attacksNotImplemented.add("mirror coat");
//        attacksNotImplemented.add("mirror move");
//        attacksNotImplemented.add("mist");
//        attacksNotImplemented.add("nightmare");
//        attacksNotImplemented.add("pain split");
//        attacksNotImplemented.add("perish song");
//        attacksNotImplemented.add("petal dance");
//        attacksNotImplemented.add("pin missile");
//        attacksNotImplemented.add("present");
//        attacksNotImplemented.add("rage");
//        attacksNotImplemented.add("rain dance");
//        attacksNotImplemented.add("reflect");
//        attacksNotImplemented.add("rest");
//        attacksNotImplemented.add("reversal");
//        attacksNotImplemented.add("roar");
//        attacksNotImplemented.add("rollout");
//        attacksNotImplemented.add("safeguard");
//        attacksNotImplemented.add("sandstorm");
//        attacksNotImplemented.add("seismic toss");
//        attacksNotImplemented.add("sketch");
//        attacksNotImplemented.add("skull bash");
//        attacksNotImplemented.add("sky attack");
//        attacksNotImplemented.add("sleep talk");
//        attacksNotImplemented.add("snore");
//        attacksNotImplemented.add("solar beam");
//        attacksNotImplemented.add("spider web");
//        attacksNotImplemented.add("spike cannon");
//        attacksNotImplemented.add("spikes");
//        attacksNotImplemented.add("spite");
//        attacksNotImplemented.add("substitute");
//        attacksNotImplemented.add("sunny day");
//        attacksNotImplemented.add("swagger");
//        attacksNotImplemented.add("teleport");
//        attacksNotImplemented.add("thrash");
//        attacksNotImplemented.add("transform");
//        attacksNotImplemented.add("triple kick");
//        attacksNotImplemented.add("twineedle");
//        attacksNotImplemented.add("whirlwind");
//    }

    // Contains all loaded pkmn textures, so that only one is used for each pkmn. ie don't load duplicate textures.
    public static HashMap<String, Texture> textures = new HashMap<String, Texture>();
    // Add to this when loading pokemon
//    public static HashMap<String, Map<String, String>> gen2Evos = new HashMap<String, Map<String, String>>();
//    public static HashMap<String, Map<Integer, String[]>> gen2Attacks = new HashMap<String, Map<Integer, String[]>>();
    // Name->Base species name. Required by egg-laying.
    public static HashMap<String, String> baseSpecies = new HashMap<String, String>();
    public static HashMap<String, ArrayList<String>> eggMoves = new HashMap<String, ArrayList<String>>();
    static {
        try {
            // TODO: prism
            for (String path : new String[]{"prism/", "", "nuuk/"}) {
                if (path.equals("prism/")) {
                    FileHandle file2 = Gdx.files.internal("crystal_pokemon/prism/pokemon_names.asm");
                    Reader reader2 = file2.reader();
                    BufferedReader br2 = new BufferedReader(reader2);
                    String line2;
                    String currPokemonName = null;
                    int dexNumber = 0;
                    while ((line2 = br2.readLine()) != null)   {
                        
                        // No need to do lower case b/c file names use capital first letter.
                        currPokemonName = line2.split("db \"")[1].split("\"")[0].replace("@", "");
                        if (currPokemonName.equals("Egg") ||
                            currPokemonName.equals("?????") ||
                            currPokemonName.equals("Debug")) {
                            continue;
                        }
                        dexNumber = Integer.valueOf(Pokemon.nameToIndex(currPokemonName.toLowerCase()));
                        if (dexNumber <= 251) {
                            continue;
                        }
                        currPokemonName = currPokemonName.replace("-", "");
                        FileHandle file = Gdx.files.internal("crystal_pokemon/prism/movesets/"+currPokemonName+".asm");
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
                            if (line.contains("EVOLVE")) {
                                String vals[] = line.split(", ");
                                String baseMon = Pokemon.baseSpecies.get(currMon);
                                if (baseMon == null) {
                                    baseMon = currMon;
                                }
                                Pokemon.baseSpecies.put(vals[2].toLowerCase(), baseMon);
                            }
                            else if (line.contains("db 0")) {
                                // Base forms put self
                                String baseMon = Pokemon.baseSpecies.get(currMon);
                                if (baseMon == null) {
                                    Pokemon.baseSpecies.put(currMon, currMon);
                                }
                                currMon = null;
                            }
                        }
                        reader.close();
                    }
                }
                else {
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
                }
                
                // TODO: if abilities are ever implemented, this needs to be removed.
                // Right now Darminitan Zen form is a fully-separate pokemon (as 
                // requested by people on discord)
                Pokemon.baseSpecies.put("darmanitanzen", "darumaka");
                Pokemon.baseSpecies.put("aexeggutor", "exeggcute");
                // Female combee is separate species due to different front anim/sprites
                Pokemon.baseSpecies.put("combee_female", "combee");
                
                // Load egg moves
                FileHandle file = Gdx.files.internal("crystal_pokemon/"+path+"egg_moves.asm");
                Reader reader = file.reader();
                BufferedReader br = new BufferedReader(reader);
                String line;
                String currMon = null;
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
//        if (name.equals("ghost")) {
//            return "000";
//        }
//        if (name.equals("egg")) {
//            return "999";
//        }
        if (name.equals("farfetch_d")){
        	name = "farfetch’d";
        }
        if(name.equals("ho_oh")) {
        	name = "ho-oh";
        }
        // TODO: handle all alolan forms here
        if (name.equals("aexeggutor")) {
            name = "exeggutor";
        }
        else if (name.equals("darmanitanzen")) {
            name = "darmanitan";
        }
        else if (name.equals("combee_female")) {
            name = "combee";
        }

        int lineNum = 1;
        try {
            FileHandle file = Gdx.files.internal("crystal_pokemon/pokemon_to_index.txt");
            Reader reader = file.reader();
            BufferedReader br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null)   {
                if (line.equalsIgnoreCase(name)) {
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

    String nickname;
    Specie specie;
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
//    String eggHatchInto = null;  // Pokemon that egg hatches into. 
    boolean isEgg = false;
    boolean isGhost = false; //for Silph Scope. Set externally by the caller.
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
    public Tile onTile;
    
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
        if (pokemonData instanceof Network.PokemonDataV05) {
            this.isEgg = ((Network.PokemonDataV05)pokemonData).name.equals("egg");
            if (((Network.PokemonDataV05)pokemonData).eggHatchInto != null) {
                pokemonData.name = ((Network.PokemonDataV05)pokemonData).eggHatchInto;
            }
        }

        this.init(pokemonData.name, pokemonData.level, pokemonData.generation, pokemonData.isShiny, this.isEgg);
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
        if (this.specie.name.equals("tauros") || this.specie.name.equals("ekans")
                || this.specie.name.equals("pidgey") || this.specie.name.equals("spearow")
                || this.specie.name.equals("rattata")) {
            this.happiness = 0;
        }

        // Fields added in v0.5
        if (pokemonData instanceof Network.PokemonDataV05) {
            this.gender = ((Network.PokemonDataV05)pokemonData).gender;
            this.happiness = ((Network.PokemonDataV05)pokemonData).friendliness;
            this.aggroPlayer = ((Network.PokemonDataV05)pokemonData).aggroPlayer;
        }

        // TODO: I guess I will have to rename this to Network.PokemonDataV05 when moving to v0.6
        // TODO: if I don't rename this, it will introduce a bug. I can't think of a better way to do it though.
        if (pokemonData instanceof Network.PokemonData) {
            this.nickname = ((Network.PokemonData)pokemonData).nickname;
        }
    }

    public Pokemon (String name, int level) {
        this(name, level, Generation.CRYSTAL);
    }

    public Pokemon (String name, int level, Generation generation) {
        this(name, level, generation, Pokemon.rand.nextInt(256) == 0);
    }
    
    public Pokemon (String name, int level, Generation generation, boolean isShiny) {
        this(name, level, generation, isShiny, false);
    }

    public Pokemon (String name, int level, Generation generation, boolean isShiny, boolean isEgg) {
        this.init(name, level, generation, isShiny, isEgg);
        // This shouldn't be called when loading from PokemonData,
        // only when the pokemon is created for the first time.
        if (this.specie.name.equals("combee") && this.gender.equals("female")) {
            this.updateSpecieInfo("combee_female");
        }
    }

    public void updateSpecieInfo(String specieName) {
        this.nickname = specieName.toLowerCase();
        if (this.nickname.equals("aexeggutor")) {
            this.nickname = "exeggutor";
        }
        else if (this.nickname.equals("darmanitanzen")) {
            this.nickname = "darmanitan";
        }
        else if (this.nickname.equals("combee_female")) {
            this.nickname = "combee";
        }
        else if (this.nickname.contains("unown")) {
            this.nickname = "unown";
        }

        if (!Specie.species.containsKey(specieName)) {
            // Very clunky looking, but I don't know of a better way
            if (Thread.currentThread() != Game.staticGame.gameThread) {
                final String finalName = specieName;
                Runnable runnable = new Runnable() {
                    public void run() {
                        try {
                            Specie.species.put(finalName, new Specie(finalName));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        synchronized (this) {
                            this.notify();
                        }
                    }
                };
                Gdx.app.postRunnable(runnable);
                try {
                    synchronized (runnable) {
                        runnable.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else {
                Specie.species.put(specieName, new Specie(specieName));
            }
        }
        this.specie = Specie.species.get(specieName);
        if (this.specie == null) {
            System.out.println("No such specie exists: " + specieName);
        }

        // Set sprites, backsprites, & intro animations
        if (isEgg) {
            this.nickname = "egg";
            this.sprite = Specie.spriteEgg;
            this.backSprite = Specie.backSpriteEgg;
            this.introAnim = Specie.introAnimEgg;
        }
        else if (isShiny) {
            this.sprite = this.specie.spriteShiny;
            this.backSprite = this.specie.backSpriteShiny;
            this.introAnim = this.specie.introAnimShiny;
        }
        else {
            this.sprite = this.specie.sprite;
            this.backSprite = this.specie.backSprite;
            this.introAnim = this.specie.introAnim;
        }

        this.learnSet = this.specie.learnSet;
        this.dexNumber = this.specie.dexNumber;
        this.baseStats = this.specie.baseStats;
        this.types = this.specie.types;
        this.growthRateGroup = this.specie.growthRateGroup;
        this.eggGroups = this.specie.eggGroups;
        if (!this.isEgg) {
            this.hms = this.specie.hms;
        }
        this.loadOverworldSprites();
        // Stats formulas here
        this.calcMaxStats();
        this.initHabitatValues();
    }
    /**
     * TODO: remove isShiny and eggHatchInto params, just set before calling init.
     */
    public void init(String specieName, int level, Generation generation, boolean isShiny, boolean isEgg) {
        // levels have to be >= 1
        if (level <= 0) {
            System.out.println("Bad level: " + String.valueOf(level));
            level = 1;
        }
        this.generation = generation;
        this.level = level;
        this.isEgg = isEgg;
        this.isShiny = isShiny;  //Pokemon.rand.nextInt(256) == 0;
        this.updateSpecieInfo(specieName);
        this.currentStats = new HashMap<String, Integer>(this.maxStats); // copy maxStats   

        // init vars
        this.angry = 0;
        this.eating = 0;

        this.attacks = new String[]{null, null, null, null};


        // TODO: individual avatars
        // TODO: remove if unused
        //        Texture avatarText = new Texture(Gdx.files.internal("pokemon_menu/avatars1.png"));
        //        this.avatarSprites = new ArrayList<Sprite>();
        //        this.avatarSprites.add(new Sprite(avatarText, 16*0, 16*0, 16, 16));
        //        this.avatarSprites.add(new Sprite(avatarText, 16*1, 16*0, 16, 16));

        //set gender of this pokemon based on GenderRatio of the specie
        // source: https://github.com/pret/pokecrystal/wiki/Add-a-new-Pok%C3%A9mon
        // GENDER_F0: 100% male
        // GENDER_F12_5: 7/8 male, 1/8 female
        // GENDER_F25: 3/4 male, 1/4 female
        // GENDER_F50: 1/2 male, 1/2 female
        // GENDER_F75: 1/4 male, 3/4 female
        // GENDER_F100: 100% female
        // GENDER_UNKNOWN: genderless
        if (this.specie.genderRatio.equals("GENDER_UNKNOWN")) {
            this.gender = "unknown";
        }
        else {
            int percentFemale = 0;
            if (this.specie.genderRatio.equals("GENDER_F12_5")) {
                percentFemale = 125;
            }
            else if (this.specie.genderRatio.equals("GENDER_F25")) {
                percentFemale = 250;
            }
            else if (this.specie.genderRatio.equals("GENDER_F50")) {
                percentFemale = 500;
            }
            else if (this.specie.genderRatio.equals("GENDER_F75")) {
                percentFemale = 750;
            }
            else if (this.specie.genderRatio.equals("GENDER_F100")) {
                percentFemale = 1000;
            }
            if (Pokemon.rand.nextInt(1000) < percentFemale) {
                this.gender = "female";
            }
            else {
                this.gender = "male";
            }
        }

        
        // Set hapiness level
        this.happiness = this.specie.baseHappiness;

        //        // if it is in original 251, load from crystal
        //        if (this.name.equals("egg") ||
        //            Specie.nuukPokemon.contains(this.name.toLowerCase()) ||
        //            (Integer.valueOf(this.dexNumber) <= 251 && Integer.valueOf(this.dexNumber) > 0)) {
        ////            this.loadCrystalPokemon(name);
        //        // else try loading from prism
        //        } else {
        ////            this.loadPrismPokemon(name);
        //        }

        getCurrentAttacks(); // fill this.attacks with what it can currently know
        this.exp = gen2CalcExpForLevel(this.level);
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
        // TODO: should probably remove, don't need this.
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
            if (!this.isEgg &&
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
                if (!potentialMate.isEgg &&
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
    	int prevMaxHp = this.maxStats.get("hp");
        this.level += numLevels;
        this.calcMaxStats();
        // TODO: remove when getting rid of currentStats.
//        this.currentStats = new HashMap<String, Integer>(this.maxStats);
        for (String stat : this.maxStats.keySet()) {
            if (stat.equals("hp")) {
            	int prevCurrentHp = this.currentStats.get("hp");
                this.currentStats.put(stat, prevCurrentHp + (this.maxStats.get("hp")-prevMaxHp));
            }
            else
            	this.currentStats.put(stat, this.maxStats.get(stat));
        }
    }

    /*
     * Turn off egg flag & reset overworld textures
     * */
    void hatch() {
        this.isEgg = false;
        this.loadOverworldSprites();
        this.nickname = this.specie.name;
        this.hms = this.specie.hms;
        if (this.isShiny)
        {
            this.sprite = specie.spriteShiny;
            this.backSprite = specie.backSpriteShiny;
            this.introAnim = specie.introAnimShiny;
        }
        else {
            this.sprite = specie.sprite;
            this.backSprite = specie.backSprite;
            this.introAnim = specie.introAnim;
        }
    }
    /*
     * Called when setting up a Ghost encounter
     * */
    void spookify() {
        this.isGhost = true;
        this.nickname = "ghost";
        this.sprite = Specie.spriteGhost;
        this.backSprite = null;
        this.introAnim = Specie.introAnimGhost;
    }
    /*
     * Called when Silph Scope is used
     * */
    public Pokemon revealGhost() {
        this.isGhost = false;
        this.loadOverworldSprites();
        this.nickname = this.specie.name;
        //need to store these as the Sprite will be reset once the ghost is revealed
        float x = this.sprite.getX();
        float y = this.sprite.getY();
        if(isShiny)
        {
            this.sprite = specie.spriteShiny;
            this.backSprite = specie.backSpriteShiny;
            this.introAnim = specie.introAnimShiny;
        }
        else {
            this.sprite = specie.sprite;
            this.backSprite = specie.backSprite;
            this.introAnim = specie.introAnim;
        }
        //reset the new sprite to the correct place in the Battle
        this.sprite.setPosition(x, y);
        return this;
    }
    /**
     * Compute changes required by evolution.
     */
    void evolveTo(String targetName) {
    	int prevMaxHp = this.maxStats.get("hp");
    	int prevCurrentHp = this.currentStats.get("hp");
        updateSpecieInfo(targetName);

        // Update base stats and various values.
        // Don't modify attacks, current hp, etc.
        // TODO: current hp is probably compensated in the real game
        //        if (Specie.nuukPokemon.contains(this.name.toLowerCase()) ||
        //            (Integer.valueOf(this.dexNumber) <= 251 && Integer.valueOf(this.dexNumber) > 0)) {
        //            this.loadCrystalPokemon(this.name);
        //        }
        //        else {
        //            this.loadPrismPokemon(this.name);
        //        }

        //TODO *possibly* need to account for Shedinja here since it actually loses HP when it evolves, but depends how it gets implemented
        // add the HP difference to the pokmon's health
        this.currentStats.put("hp", prevCurrentHp + (this.maxStats.get("hp") - prevMaxHp));
        //clear status aliments
        this.status = null;
    }

    /**
     * .
     */
    void initHabitatValues() {
        this.harvestables = specie.harvestables;
        this.habitats = specie.habitats;
        this.harvestTimerMax = specie.harvestTimerMax;
        
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
        // Currently Ancientpower is the only attack that boosts all stats.
        if (stat.equals("all")) {
            boolean succeeded = false;
            String[] statNames = new String[]{"attack", "defense", "specialAtk", "specialDef", "speed"};
            for (String statName : statNames) {
                succeeded = succeeded || this.gen2ApplyStatStage(statName, stage, false);
            }
            return succeeded;
        }
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



    void loadOverworldSprites() {                
            // If this is an egg, load special texture for the overworld sprite
            if (this.isEgg) {
            	this.movingSprites = specie.movingSpritesEgg;
                this.altMovingSprites = specie.altMovingSpritesEgg;
                this.standingSprites = specie.standingSpritesEgg;
                this.avatarSprites = specie.avatarSpritesEgg;
            }
            else //otherwise, set normal overworld textures
            {
            	this.spriteOffsetY = specie.spriteOffsetY;
                this.movingSprites = specie.movingSprites;
                this.altMovingSprites = specie.altMovingSprites;
                this.standingSprites = specie.standingSprites;
                this.avatarSprites = specie.avatarSprites;
            }       
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
            // Aggro any (potential) nearby parents
            if (Pokemon.this.isEgg && Pokemon.this.previousOwner != game.player) {
                newAction = new DisplayText(game, game.player.name+" received an EGG.", "Berry_Get.ogg", null,
                            null);
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
                    if (pokemon.isEgg) {
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
            Pokemon.this.isRunning = false;  // if pokemon was trying to flee player, stop.
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
            this.isEgg = Pokemon.this.isEgg;
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
//            this.spritePart.setRegionY(Pokemon.this.spriteOffsetY+8);
            this.spritePart.setRegionY(Pokemon.this.currOwSprite.getRegionY()+8);
            this.spritePart.setRegionHeight(8);
            // TODO: the below didn't help
//            this.spritePart.setRegion(Pokemon.this.currOwSprite.getRegionX(),
//                                      Pokemon.this.currOwSprite.getRegionY()+8,
//                                      Pokemon.this.currOwSprite.getRegionWidth(), 8);
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
//            this.spritePart.setRegionY(Pokemon.this.spriteOffsetY);
            this.spritePart.setRegionY(Pokemon.this.currOwSprite.getRegionY());
            this.spritePart.setRegionHeight(8);
            // TODO: the below didn't help anything
//            this.spritePart.setRegion(Pokemon.this.currOwSprite.getRegionX(),
//                                      Pokemon.this.currOwSprite.getRegionY(),
//                                      Pokemon.this.currOwSprite.getRegionWidth(), 8);
            
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
                String baseSpecies = Pokemon.baseSpecies.get(Pokemon.this.specie.name.toLowerCase());
                if (Pokemon.this.specie.name.equals("ditto")) {
                    baseSpecies = Pokemon.baseSpecies.get(Pokemon.this.loveInterest.specie.name.toLowerCase());
                }
                // Nidoqueen will lay a nidoran_f egg b/c nidoran_f is it's base speicies
                // Add a 50 percent chance for it to lay nidoran_m egg instead.
                if (baseSpecies.equals("nidoran_f") && Game.rand.nextInt(256) < 128) {
                    baseSpecies = "nidoran_m";
                }
                Pokemon pokemonEgg = new Pokemon(baseSpecies, 5, Generation.CRYSTAL, Game.rand.nextInt(256) == 0, true);
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
                    System.out.println(Pokemon.this.specie.name);
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
            this.isEgg = Pokemon.this.isEgg;
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
                // Sigilyph moves in a square when wild
                if (Pokemon.this.previousOwner == null && Pokemon.this.specie.name.equals("sigilyph")) {
                    // Check modulus for new dir
//                    System.out.println("here");
//                    Vector2 newPos = Pokemon.this.facingPos();
                    if (Pokemon.this.dirFacing.equals("up") && Pokemon.this.position.y % 64 == 0) {
                        if (Pokemon.this.position.x % 128 == 0) {
                            Pokemon.this.dirFacing = "left";
                        }
                        else {
                            Pokemon.this.dirFacing = "right";
                        }
                    }
                    else if (Pokemon.this.dirFacing.equals("left") && Pokemon.this.position.x % 64 == 0) {
                        if (Pokemon.this.position.y % 128 == 64) {
                            Pokemon.this.dirFacing = "down";
                        }
                        else {
                            Pokemon.this.dirFacing = "up";
                        }
                    }
                    else if (Pokemon.this.dirFacing.equals("down") && Pokemon.this.position.y % 64 == 0) {
                        if (Pokemon.this.position.x % 128 == 0) {
                            Pokemon.this.dirFacing = "left";
                        }
                        else {
                            Pokemon.this.dirFacing = "right";
                        }
                    }
                    else if (Pokemon.this.dirFacing.equals("right") && Pokemon.this.position.x % 64 == 0) {
                        if (Pokemon.this.position.y % 128 == 64) {
                            Pokemon.this.dirFacing = "down";
                        }
                        else {
                            Pokemon.this.dirFacing = "up";
                        }
                    }
                    Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
                    Vector2 newPos = Pokemon.this.facingPos();
                    // If collides with something, head the opposite dir
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
                        if (Pokemon.this.dirFacing.equals("up")) {
                            Pokemon.this.dirFacing = "down";
                        }
                        else if (Pokemon.this.dirFacing.equals("left")) {
                            Pokemon.this.dirFacing = "right";
                        }
                        else if (Pokemon.this.dirFacing.equals("down")) {
                            Pokemon.this.dirFacing = "up";
                        }
                        else if (Pokemon.this.dirFacing.equals("right")) {
                            Pokemon.this.dirFacing = "left";
                        }
                        return;
                    }
                    game.actionStack.remove(this);
                    Action action = Pokemon.this.new Moving(2, 1, true, null);
                    action.append(this);
                    game.insertAction(action);
                    Pokemon.this.standingAction = action;
                    return;
                }
                else if (Pokemon.this.previousOwner == null && Pokemon.this.specie.name.equals("darmanitanzen")) {
                    return;
                }
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
        super("Mewtwo", level);
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