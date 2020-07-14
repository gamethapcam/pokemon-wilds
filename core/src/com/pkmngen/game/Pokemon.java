package com.pkmngen.game;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.TextureCache;

public class Pokemon {
    // contains all loaded pkmn textures, so that only one is used for each pkmn. ie don't load duplicate textures.
    public static HashMap<String, Texture> textures = new HashMap<String, Texture>();
    // add to this when loading pokemon
    public static HashMap<String, Map<String, String>> gen2Evos = new HashMap<String, Map<String, String>>();
    public static HashMap<String, Map<Integer, String[]>> gen2Attacks = new HashMap<String, Map<Integer, String[]>>();

    public static String nameToIndex(String name) {
        name = name.toLowerCase();
        if (name.contains("unown")) {
            name = "unown";
        }
        if (name.equals("ghost")) {
            return "000";
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
    // keep all pkmn textures in an array to be loaded
    // that way whenever a pkmn is created, it doesn't re-load the texture
    // takes up a lot of memory
    Texture[] pokemonTextures = new Texture[650]; // 650 for now since that's how many cries in folder

    Map<String, Integer> baseStats = new HashMap<String, Integer>();

    Map<String, Integer> currentStats = new HashMap<String, Integer>();

    // note - this doesn't go in 'maxStats' map
    // int catchRate; // may put into some other map later

    Map<String, Integer> maxStats = new HashMap<String, Integer>(); // needed for various calculations
    ArrayList<String> hms = new ArrayList<String>();
    Map<String, Integer> IVs = new HashMap<String, Integer>();

    String growthRateGroup = "";

    Sprite sprite;
    Sprite backSprite;

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

    String trappedBy = null;  // whirlpool, wrap, bind, etc
    int trapCounter = 0;  // number turns remaining for trap
    Map<Integer, String[]> learnSet;

    boolean inBattle = false;

    Generation generation;
    ArrayList<Sprite> introAnim;

    public Pokemon (Network.PokemonData pokemonData) {
        this(pokemonData.name, pokemonData.level, pokemonData.generation);
        this.currentStats.put("hp", pokemonData.hp);
        this.attacks[0] = pokemonData.attacks[0];
        this.attacks[1] = pokemonData.attacks[1];
        this.attacks[2] = pokemonData.attacks[2];
        this.attacks[3] = pokemonData.attacks[3];
        this.position = pokemonData.position;
    }

    public Pokemon (String name, int level) {
        // generation defaults to RED
        this(name, level, Generation.RED);
    }

    public Pokemon (String name, int level, Generation generation) {
        name = name.toLowerCase();
        this.name = name;
        if (name.contains("unown")) {
            this.name = "unown";
        }
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
            if (Integer.valueOf(this.dexNumber) <= 251 && Integer.valueOf(this.dexNumber) > 0) {
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
            if (name.equals("sneasel") || name.equals("scyther")) {
                this.hms.add("CUT");
            }

            // TODO: for now, all grass types can cut
            if (this.types.contains("GRASS")) {
                this.hms.add("CUT");
            }
            if (this.types.contains("FIGHTING")) {
                this.hms.add("BUILD");
            }

            // TODO: different pokemon than machop
            // Custom attributes - better way to handle this?
//            if (name.equals("machop")) {
//                this.hms.add("HEADBUTT");
//            }

            // Custom attributes - better way to handle this?
            if (name.equals("stantler") ||
                name.equals("ponyta") ||
                name.equals("arcanine") ||
                name.equals("donphan") ||
                name.equals("girafarig") ||
                name.equals("houndoom") ||
                name.equals("rapidash") ||
                name.equals("tauros") ||
                name.equals("ninetails") ||
                name.equals("mamomswine") ||
                name.equals("luxray")) {
                // TODO: change to 'RIDE' later. Making it 'JUMP' for now so that it's not confusing.
                // Later, once there (hopefully) are riding sprites, this can be changed to ride.
                // My current idea is that RIDE increases movement speed and can perform jumps up ledges.
                this.hms.add("JUMP");
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
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
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
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
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
            this.sprite = new Sprite(pokemonText, 56*28, 56*2, 56, 56);
            this.sprite.flip(true, false); // my sprites are backwards

            // back sprite
            pokemonText = new Texture(Gdx.files.internal("pokemon/back_sheet1.png"));
            this.backSprite = new Sprite(pokemonText, 30*2+1, 29*8+1, 28, 28); // sheet is a little messed up, hard to change
            this.backSprite.setScale(2);
//            pokemonText = new Texture(Gdx.files.internal("pokemon/back_sheet1.png")); // debug - change to charmander sprite
//            this.backSprite = new Sprite(pokemonText, 30*3+1, 29*0+1, 28, 28); //
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
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
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
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
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
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
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
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
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
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
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
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
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
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
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
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
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
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
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
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
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
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
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
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
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
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
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
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
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
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
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
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
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
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
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
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
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
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
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
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
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
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
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
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
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
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
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
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
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
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
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

        // hp = (((IV + Base + (sqrt(EV)/8) + 50)*Level)/50 + 10
        // other stat = (((IV + Base + (sqrt(EV)/8))*Level)/50 + 5
    }

    /*
     * Compute changes required by evolution.
     */
    void evolveTo(String targetName) {
        this.name = targetName;
        this.dexNumber = Pokemon.nameToIndex(this.name);
        // Update base stats and various values.
        // Don't modify attacks, current hp, etc.
        // TODO: current hp is probably compensated in the real game
        if (Integer.valueOf(this.dexNumber) <= 251 && Integer.valueOf(this.dexNumber) > 0) {
            this.loadCrystalPokemon(this.name);
        }
        else {
            this.loadPrismPokemon(this.name);
        }
        this.calcMaxStats();
        // restore hp to full
        this.currentStats.put("hp", this.maxStats.get("hp"));

        // TODO: probably move this to function somewhere
        if (name.equals("machop")) {
            this.hms.add("BUILD");
            this.hms.add("HEADBUTT");
        }
        else if (name.equals("sneasel")) {
            this.hms.add("CUT");
        }
        else if (name.equals("stantler") ||
            name.equals("ponyta") ||
            name.equals("arcanine") ||
            name.equals("donphan") ||
            name.equals("girafarig") ||
            name.equals("houndoom") ||
            name.equals("rapidash") ||
            name.equals("tauros") ||
            name.equals("ninetails") ||
            name.equals("mamomswine") ||
            name.equals("luxray")) {
            // TODO: change to 'RIDE' later. Making it 'JUMP' for now so that it's not confusing.
            // Later, once there (hopefully) are riding sprites, this can be changed to ride.
            // My current idea is that RIDE increases movement speed and can perform jumps up ledges.
            this.hms.add("JUMP");
        }
        // TODO: for now, all grass types can cut
        if (this.types.contains("GRASS")) {
            this.hms.add("CUT");
        }
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
        System.out.println("Error: invalid growth group for " + this.name + ", group: "+String.valueOf(this.growthRateGroup));
        return 0;
    }

    // when generating a pokemon, this will select which attacks it knows
     // by default
    void getCurrentAttacks() {
        int i = 0;
        for (Integer level : this.learnSet.keySet()) {
            for (String attack : this.learnSet.get(level)) {
                if (level < this.level) {
                    this.attacks[i] = attack;
                    i += 1;
                    if (i >= this.attacks.length) {
                        i = 0;
                    }
//                    System.out.println("attack: " + attack);
                }
            }
        }

    }

    void loadCrystalPokemon(String name) {
        name = name.toLowerCase();

        // load base stats
        try {
            String newName = name;
            if (name.contains("unown")) {
                newName = "unown";
            }
            FileHandle file = Gdx.files.internal("crystal_pokemon/base_stats/" + newName + ".asm");
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
                } else if (lineNum == 15) {
                    this.growthRateGroup = line.split("db ")[1].split(" ;")[0];
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

        // load sprite and animation data
        // load front sprite
        if (!Pokemon.textures.containsKey(name+"_front")) {
            Texture text = new Texture(Gdx.files.internal("crystal_pokemon/pokemon/" + name + "/front.png"));
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
        }
//        Texture pokemonText = new Texture(Gdx.files.internal("crystal_pokemon/pokemon/" + name + "/front.png"));
        Texture pokemonText = Pokemon.textures.get(name+"_front");
        // height and width are the same for these sprites
        int height = pokemonText.getWidth();
        this.sprite = new Sprite(pokemonText, 0, 0, height, height);
        if (!Pokemon.textures.containsKey(name+"_back")) {
            Pokemon.textures.put(name+"_back", new Texture(Gdx.files.internal("crystal_pokemon/pokemon/" + name + "/back.png")));
        }
//      pokemonText = new Texture(Gdx.files.internal("crystal_pokemon/pokemon/" + name + "/back.png"));
        pokemonText = Pokemon.textures.get(name+"_back");
//        height = pokemonText.getWidth();
        this.backSprite = new Sprite(pokemonText, 0, 0, 48, 48);

        // load animation from file
        this.introAnim = new ArrayList<Sprite>();
        try {
            FileHandle file = Gdx.files.internal("crystal_pokemon/pokemon/" + name + "/anim.asm");
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
//                        pokemonText = new Texture(Gdx.files.internal("crystal_pokemon/pokemon/" + name + "/front.png"));
                        pokemonText = Pokemon.textures.get(name+"_front");
                        Sprite sprite = new Sprite(pokemonText, 0, height*frame, height, height);
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

        // load attacks from file
        if (!Pokemon.gen2Attacks.containsKey(name) || !Pokemon.gen2Evos.containsKey(name)) {
            Map<Integer, String[]> attacks = new HashMap<Integer, String[]>();
            Map<String, String> evos = new HashMap<String, String>();
            Pokemon.gen2Attacks.put(name, attacks);
            Pokemon.gen2Evos.put(name, evos);
            try {
                FileHandle file = Gdx.files.internal("crystal_pokemon/evos_attacks.asm");
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
                    if (line.toLowerCase().contains(name)) {
                        inSection = true;
                        continue;
                    }
                    if (!inSection) {
                        continue;
                    }
                    if (line.contains("EVOLVE_LEVEL")) {
                        String vals[] = line.split(", ");
                        evos.put(vals[1], vals[2].toLowerCase());
                    }
                    else if (line.contains("EVOLVE_ITEM") || line.contains("EVOLVE_TRADE") || line.contains("EVOLVE_HAPPINESS")) {
                        // TODO
                    }
                    else if (!line.contains("\t")) {
                        inSection = false;
                    }
                    else if (!line.contains("db 0")) {
                        String vals[] = line.split(", ");
                        String attack = vals[1].toLowerCase().replace('_', ' ');
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

    void loadOverworldSprites(String name) {
        name = name.toLowerCase();
        // load overworld sprites from file
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
            for (int i=0; i < lines.size(); ) {
                line = lines.get(i);
                String currName = line.split("db \"")[1].split("\"")[0].toLowerCase();
                if (currName.contains(name)) {
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
                    this.altMovingSprites.get("up").flip(true, false);
                    this.standingSprites.put("up", new Sprite(text, col*16 +48, row*16, 16, 16));

                    this.standingSprites.put("down", new Sprite(text, col*16 +64, row*16, 16, 16));
                    this.movingSprites.put("down", new Sprite(text, col*16 +80, row*16, 16, 16));
                    this.altMovingSprites.put("down", new Sprite(text, col*16 +80, row*16, 16, 16));
                    this.altMovingSprites.get("down").flip(true, false);
                    
                    this.avatarSprites.add(this.standingSprites.get("down"));
                    this.avatarSprites.add(this.movingSprites.get("down"));
                    this.avatarSprites.add(this.standingSprites.get("down"));
                    this.avatarSprites.add(this.altMovingSprites.get("down"));
                    break;
                }
                i++;
            }
            reader.close();

            if (found) {
                return;
            }
            // If failed to load from prism animations, load from crystal
            Texture text = TextureCache.get(Gdx.files.internal("crystal_pokemon/crystal-overworld-sprites1.png"));
            int dexNumber = Integer.valueOf(this.dexNumber)-1;
//            if (dexNumber > 123) {
//                dexNumber += 3;
//            }
            int col = (dexNumber % 15) * 2;
            int row = (int)((dexNumber) / 15);
            this.spriteOffsetY = row*16;
            for (String dir : new String[]{"up", "down", "left", "right"}) {
                this.standingSprites.put(dir, new Sprite(text, 1 +col*17, 31 +row*25, 16, 16));
                this.movingSprites.put(dir, new Sprite(text, 1 +col*17 +17, 31 +row*25, 16, 16));
                this.altMovingSprites.put(dir, new Sprite(text, 1 +col*17 +17, 31 +row*25, 16, 16));
            }
            this.avatarSprites.add(this.standingSprites.get("down"));
            this.avatarSprites.add(this.movingSprites.get("down"));
            this.avatarSprites.add(this.standingSprites.get("down"));
            this.avatarSprites.add(this.movingSprites.get("down"));

            // TODO: load ghost overworld sprite from ghost sheet.
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    void loadPrismPokemon(String name) {
        name = name.toLowerCase();

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
                } else if (lineNum == 14) {
                    this.growthRateGroup = line.split("db ")[1].split(" ;")[0];
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

        // load sprite and animation data
        // load front sprite
        if (!Pokemon.textures.containsKey(name+"_front")) {
            Pokemon.textures.put(name+"_front", new Texture(Gdx.files.internal("crystal_pokemon/prism/pics/" + name + "/front.png")));
        }
//        Texture pokemonText = new Texture(Gdx.files.internal("crystal_pokemon/prism/pics/" + name + "/front.png"));  // TODO: remove
        Texture pokemonText = Pokemon.textures.get(name+"_front");
        // height and width are the same for these sprites
        int height = pokemonText.getWidth();
        this.sprite = new Sprite(pokemonText, 0, 0, height, height);
        if (!Pokemon.textures.containsKey(name+"_back")) {
            Pokemon.textures.put(name+"_back", new Texture(Gdx.files.internal("crystal_pokemon/prism/pics/" + name + "/back.png")));
        }
//        pokemonText = new Texture(Gdx.files.internal("crystal_pokemon/prism/pics/" + name + "/back.png"));  // TODO: remove
        pokemonText = Pokemon.textures.get(name+"_back");
//        height = pokemonText.getWidth();
        this.backSprite = new Sprite(pokemonText, 0, 0, 48, 48);

        // load animation from file
        this.introAnim = new ArrayList<Sprite>();
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
                        pokemonText = Pokemon.textures.get(name+"_front");
                        Sprite sprite = new Sprite(pokemonText, 0, height*frame, height, height);
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

        // load attacks from file
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
     * Add pokemon to inventory, remove standingAction from actionStack,
     * and remove pokemon from game.map.pokemon.
     */
    public class AddToInventory extends Action {
        public int layer = 130;
        int moveTimer = 0;
        
        public AddToInventory(Action nextAction) {
            this.nextAction = nextAction;
        }

        public int getLayer(){
            return this.layer;
        }

        @Override
        public void firstStep(Game game) {
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
            
            game.actionStack.remove(this);
            game.insertAction(new PlaySound("seed1", this.nextAction));
        }
    }

    /**
     * Draw pokemon lower (below grass).
     */
    public class DrawLower extends Action {
        public int layer = 130;
        Sprite spritePart;

        public int getLayer(){return this.layer;}

        @Override
        public void step(Game game) {
            if (!game.actionStack.contains(Pokemon.this.standingAction)) {
                game.actionStack.remove(this);
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

        public int getLayer(){return this.layer;}

        @Override
        public void step(Game game) {
            if (!game.actionStack.contains(Pokemon.this.standingAction)) {
                game.actionStack.remove(this);
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
        }
    }

    /**
     * Draw emote animation above pokemon.
     */
    public class Emote extends Action {
        public int layer = 114;
        String type;
        Sprite sprite;
        HashMap<String, Sprite> sprites = new HashMap<String, Sprite>();
        int timer = 0;

        public int getLayer(){return this.layer;}
        
        public Emote(String type, Action nextAction) {
            this.type = type;
            Texture text = TextureCache.get(Gdx.files.internal("emotes.png"));
            int i = 0;
            for (String name : new String[]{"!", "?", "happy", "skull", "heart", "bolt", "sleep", "fish"}) {
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
            game.mapBatch.draw(this.sprite, Pokemon.this.position.x, Pokemon.this.position.y +4 +16);
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
        int numMove = 1;
        
        public Moving(int delay, int numMove, boolean alternate, Action nextAction) {
            this.delay = delay;
            this.numMove = numMove;
            this.alternate = alternate;
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
            if (Pokemon.this.dirFacing == "up") {
                this.targetPos = new Vector2(Pokemon.this.position.x, Pokemon.this.position.y+16);
            }
            else if (Pokemon.this.dirFacing == "down") {
                this.targetPos = new Vector2(Pokemon.this.position.x, Pokemon.this.position.y-16);
            }
            else if (Pokemon.this.dirFacing == "left") {
                this.targetPos = new Vector2(Pokemon.this.position.x-16, Pokemon.this.position.y);
            }
            else if (Pokemon.this.dirFacing == "right") {
                this.targetPos = new Vector2(Pokemon.this.position.x+16, Pokemon.this.position.y);
            }
            game.map.pokemon.remove(Pokemon.this.position);
            game.map.pokemon.put(this.targetPos.cpy(), Pokemon.this);
            Pokemon.this.canMove = true;
        }

        @Override
        public void step(Game game) {
            if (!Pokemon.this.canMove) {
                return;
            }
            this.timer--;
            if (this.timer <= 0) {
                this.timer = this.delay;
                if (Pokemon.this.dirFacing == "up") {
                    Pokemon.this.position.y += this.numMove;
                }
                else if (Pokemon.this.dirFacing == "down") {
                    Pokemon.this.position.y -= this.numMove;
                }
                else if (Pokemon.this.dirFacing == "left") {
                    Pokemon.this.position.x -= this.numMove;
                }
                else if (Pokemon.this.dirFacing == "right") {
                    Pokemon.this.position.x += this.numMove;
                }
            }
            this.xDist = Math.abs(this.initialPos.x - Pokemon.this.position.x);
            this.yDist = Math.abs(this.initialPos.y - Pokemon.this.position.y);

            if ((this.yDist < 13 && this.yDist > 2)
                || (this.xDist < 13 && this.xDist > 2)) {
                if (this.alternate == true) {
                    Pokemon.this.currOwSprite = Pokemon.this.altMovingSprites.get(Pokemon.this.dirFacing);
                }
                else {
                    Pokemon.this.currOwSprite = Pokemon.this.movingSprites.get(Pokemon.this.dirFacing);
                }
            }
            else {
                Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
            }
            if (this.xDist >= 16 || this.yDist >= 16) {
                Pokemon.this.position.set(this.targetPos);
                game.actionStack.remove(this);
                game.insertAction(this.nextAction);
                Pokemon.this.standingAction = this.nextAction;
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
            game.player.pokemon.remove(Pokemon.this);
            if (game.player.currPokemon == Pokemon.this) {
                game.player.currPokemon = game.player.pokemon.get(0);
            }
            game.actionStack.remove(this);
            game.insertAction(Pokemon.this.new Standing());
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
        boolean alternate = true;
        
        public Standing() {}

        public String getCamera() {
            return "map";
        }

        public int getLayer(){
            return this.layer;
        }

        @Override
        public void firstStep(Game game) {
            Pokemon.this.mapTiles = game.map.tiles;
            this.moveTimer = game.map.rand.nextInt(180) + 60;
            game.map.pokemon.put(Pokemon.this.position.cpy(), Pokemon.this);
            Pokemon.this.standingAction = this;
            Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
            game.insertAction(Pokemon.this.new DrawLower());
            game.insertAction(Pokemon.this.new DrawUpper());
            // If pokemon has low happiness, it starts running animation
            if (Pokemon.this.happiness <= 0) {
                this.runTimer = 180;
            }
        }

        @Override
        public void step(Game game) {
            Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);

            if (this.runTimer > 0 && !Pokemon.this.isRunning) {
                if (this.runTimer == 170) {
                    game.insertAction(Pokemon.this.new Emote("!", null));
                }
                if (this.runTimer == 110) {
                    Pokemon.this.dirFacing = "left";
                }
                if (this.runTimer == 80) {
                    Pokemon.this.dirFacing = "right";
                }
                if (this.runTimer == 50) {
                    Pokemon.this.dirFacing = "left";
                }
                if (this.runTimer == 20) {
                    Pokemon.this.dirFacing = "left";
                }
                if (this.runTimer == 1) {
                    Pokemon.this.isRunning = true;
                }
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

            if (!Pokemon.this.canMove) {
                return;
            }
//            game.mapBatch.draw(Pokemon.this.standingSprites.get(Pokemon.this.dirFacing),
//                               Pokemon.this.position.x, Pokemon.this.position.y+4);
            if (this.moveTimer <= 0) {
                this.moveTimer = game.map.rand.nextInt(180) + 60;
                int randDir = game.map.rand.nextInt(4);
                Vector2 newPos;
                if (randDir == 0) {
                    Pokemon.this.dirFacing = "up";
                    newPos = new Vector2(Pokemon.this.position.x, Pokemon.this.position.y+16);
                }
                else if (randDir == 1) {
                    Pokemon.this.dirFacing = "down";
                    newPos = new Vector2(Pokemon.this.position.x, Pokemon.this.position.y-16);
                }
                else if (randDir == 2) {
                    Pokemon.this.dirFacing = "right";
                    newPos = new Vector2(Pokemon.this.position.x+16, Pokemon.this.position.y);
                }
                else {
                    Pokemon.this.dirFacing = "left";
                    newPos = new Vector2(Pokemon.this.position.x-16, Pokemon.this.position.y);
                }
                // Just checking overworldTiles for now, that way it will still
                // move around while player is indoors
                Tile temp = Pokemon.this.mapTiles.get(newPos);
                if (temp == null ||
                    temp.attrs.get("solid") || 
                    temp.attrs.get("ledge") || 
                    temp.name.contains("door") || 
                    game.map.pokemon.containsKey(newPos) ||
                    // TODO: an indoor player will mess with an outdoor pokemon here
                    // technically doesn't matter for now b/c indoor tiles won't
                    // overlap with non-solid overworld areas.
                    game.player.position.equals(newPos) ||
                    game.players.containsKey(newPos)) {
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
        Texture pokemonText = new Texture(Gdx.files.internal("pokemon/mgengar_base1.png"));
        this.breathingSprite = new Sprite(pokemonText, 0, 0, 56, 56);

        pokemonText = new Texture(Gdx.files.internal("pokemon/mgengar_over1.png"));
        this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);

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
    }
}

class SpecialMewtwo1 extends Pokemon {
    Sprite breathingSprite;

    public SpecialMewtwo1(int level) {
        // initialize variables
        super("Mewtwo", level);

        // gen I properties
        this.baseStats.put("hp", 106);
        this.baseStats.put("attack", 110);
        this.baseStats.put("defense", 90);
        this.baseStats.put("specialAtk", 154);
        this.baseStats.put("specialDef", 154);
        this.baseStats.put("speed", 130);
        this.baseStats.put("catchRate", 3);

        // sprite
        Texture pokemonText = new Texture(Gdx.files.internal("pokemon/mewtwo_special1.png"));
        this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);

        pokemonText = new Texture(Gdx.files.internal("pokemon/mewtwo_special2.png"));
        this.breathingSprite = new Sprite(pokemonText, 0, 0, 56, 56);

//        this.learnSet.put(1, new String[]{"Confusion", "Disable", "Psychic", "Swift"});
        this.learnSet.put(1, new String[]{"Psychic", "Psychic", "Psychic", "Psychic"});
        this.types.add("Psychic");

        getCurrentAttacks(); // fill this.attacks with what it can currently know

        // stats formulas here
        calcMaxStats();
        this.currentStats = new HashMap<String, Integer>(this.maxStats); // copy maxStats
    }
}