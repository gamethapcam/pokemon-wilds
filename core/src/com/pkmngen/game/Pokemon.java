package com.pkmngen.game;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Pokemon {

    String name;
    int level;
    String dexNumber;

    Map<String, Integer> baseStats = new HashMap<String, Integer>();
    Map<String, Integer> currentStats = new HashMap<String, Integer>();
    Map<String, Integer> maxStats = new HashMap<String, Integer>(); //needed for various calculations
    ArrayList<String> hms = new ArrayList<String>();

    Map<String, Integer> IVs = new HashMap<String, Integer>();

    //note - this doesn't go in 'maxStats' map
    //int catchRate; //may put into some other map later

    Sprite sprite;
    Sprite backSprite;
    ArrayList<Sprite> avatarSprites; 

    // need to be able to manipulate this for 
    // normal pkmn don't use this - so far only specialmewtwo and mega gengar
    Sprite breathingSprite = null;

    ArrayList<String> types;

    int angry, eating; //nonzero if angry or eating. safari zone mechanic

    //Music cry; //unused atm, using PlaySound

    //this reference is used when needing to stop drawing pokemon in battle screen
     //could also just be oppPokemonDrawAction in battle, I think
    //Action drawAction; //doesn't work. also, using sprite alpha for now
    
    String[] attacks;
    Map<Integer, String[]> learnSet;
    
    // specific, ie GOLD, RED, CRYSTAL
    // sprites, stats, attacks etc differ depending
    public enum Generation {
        RED,
        CRYSTAL
    }
    Generation generation;
    ArrayList<Sprite> introAnim;
    

    
    String nameToIndex(String name) {
        name = name.toLowerCase();
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
    
    void loadCrystalPokemon(String name) {
        name = name.toLowerCase();

        // load base stats
        try {
            FileHandle file = Gdx.files.internal("crystal_pokemon/base_stats/" + name + ".asm");
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
                    String types[] = line.split("db ")[1].split(", ");
                    this.types.add(types[0]);
                    this.types.add(types[1]);
                } else if (lineNum == 6) {
                    String catchRate = line.split("db ")[1].split(" ;")[0];
                    this.baseStats.put("catchRate", Integer.valueOf(catchRate));
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
        Texture pokemonText = new Texture(Gdx.files.internal("crystal_pokemon/pokemon/" + name + "/front.png"));
        // height and width are the same for these sprites
        int height = pokemonText.getWidth();
        this.sprite = new Sprite(pokemonText, 0, 0, height, height);
        pokemonText = new Texture(Gdx.files.internal("crystal_pokemon/pokemon/" + name + "/back.png"));  
//        height = pokemonText.getWidth();  
        this.backSprite = new Sprite(pokemonText, 0, 0, height, height);
        
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
                        pokemonText = new Texture(Gdx.files.internal("crystal_pokemon/pokemon/" + name + "/front.png"));
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
                    String types[] = line.split("db ")[1].split(", ");
                    this.types.add(types[0]);
                    this.types.add(types[1]);
                } else if (lineNum == 3) {
                    String catchRate = line.split("db ")[1].split(" ;")[0];
                    this.baseStats.put("catchRate", Integer.valueOf(catchRate));
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
        Texture pokemonText = new Texture(Gdx.files.internal("crystal_pokemon/prism/pics/" + name + "/front.png"));
        // height and width are the same for these sprites
        int height = pokemonText.getWidth();
        this.sprite = new Sprite(pokemonText, 0, 0, height, height);
        pokemonText = new Texture(Gdx.files.internal("crystal_pokemon/prism/pics/" + name + "/back.png")); 
//        height = pokemonText.getWidth();   
        this.backSprite = new Sprite(pokemonText, 0, 0, height, height);
        
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
                        pokemonText = new Texture(Gdx.files.internal("crystal_pokemon/prism/pics/" + name + "/front.png"));
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
    }
    
    public Pokemon (String name, int level) {
        // generation defaults to RED
        this(name, level, Generation.RED);
    }
    
    public Pokemon (String name, int level, Generation generation) {
        
        this.name = name;
        this.level = level;
        this.generation = generation;

        this.types = new ArrayList<String>();

        //init vars
        this.angry = 0;
        this.eating = 0;

        this.attacks = new String[]{"-","-","-","-"};
        this.learnSet = new HashMap<Integer, String[]>();
        
        //TODO: individual avatars
        Texture avatarText = new Texture(Gdx.files.internal("pokemon_menu/avatars1.png"));
        this.avatarSprites = new ArrayList<Sprite>();
        this.avatarSprites.add(new Sprite(avatarText, 16*0, 16*0, 16, 16));
        this.avatarSprites.add(new Sprite(avatarText, 16*1, 16*0, 16, 16));
        
        // if generation is crystal, load from file
        if (generation.equals(Generation.CRYSTAL)) {
            this.dexNumber = this.nameToIndex(name);
            // if it is in original 251, load from crystal
            if (Integer.valueOf(this.dexNumber) <= 251) {
                this.loadCrystalPokemon(name);
            // else try loading from prism
            } else {
                this.loadPrismPokemon(name);
            }
            
            // Custom attributes - better way to handle this?
            if (name.toLowerCase().equals("machop")) {
                this.hms.add("BUILD");
            }
            
        }

        else if (name == "Zubat") { //gen I properties
            this.baseStats.put("hp",40);
            this.baseStats.put("attack",45);
            this.baseStats.put("defense",35);
            this.baseStats.put("specialAtk",30);
            this.baseStats.put("specialDef",40);
            this.baseStats.put("speed",55);
            this.baseStats.put("catchRate", 255);
            //sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/zubat.png"));
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); //my sprites are backwards

            this.types.add("Poison");
            this.types.add("Flying");
        }
        else if (name.equals("Rattata")) { //gen I properties
            this.baseStats.put("hp",30);
            this.baseStats.put("attack",56);
            this.baseStats.put("defense",35);
            this.baseStats.put("specialAtk",25);
            this.baseStats.put("specialDef",25);
            this.baseStats.put("speed",72);
            this.baseStats.put("catchRate", 255);
            //sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/rattata.png"));
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
//            this.sprite.flip(true, false); //my sprites are backwards

            this.types.add("Normal");
        }
        else if (name == "Cloyster") { //gen I properties
//            this.baseStats.put("hp",50);
            this.baseStats.put("hp",500);
            this.baseStats.put("attack",95);
            this.baseStats.put("defense",180);
            this.baseStats.put("specialAtk",85);
            this.baseStats.put("specialDef",85);
            this.baseStats.put("speed",70);
            this.baseStats.put("catchRate", 60);
            //sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/pokemon_sheet1.png"));
            this.sprite = new Sprite(pokemonText, 56*28, 56*2, 56, 56);
            this.sprite.flip(true, false); //my sprites are backwards

            //back sprite
            pokemonText = new Texture(Gdx.files.internal("pokemon/back_sheet1.png"));
            this.backSprite = new Sprite(pokemonText, 30*2+1, 29*8+1, 28, 28); //sheet is a little messed up, hard to change
            this.backSprite.setScale(2);
//            pokemonText = new Texture(Gdx.files.internal("pokemon/back_sheet1.png")); //debug - change to charmander sprite
//            this.backSprite = new Sprite(pokemonText, 30*3+1, 29*0+1, 28, 28); //
//            this.backSprite.setScale(2);
            
            //moves that cloyster can learn
            // TODO: was this, removed for demo
//            learnSet.put(1, new String[]{"Aurora Beam", "Clamp", "Supersonic", "Withdraw"});
            learnSet.put(1, new String[]{"Clamp"});
//            learnSet.put(1, new String[]{"Fly", "Growl", "Hyper Beam"});
            //learnSet.put(20, new String[]{"Harden", "Harden", "Harden", "Harden"}); //debug
//            learnSet.put(50, new String[]{"Spike Cannon"});  // TODO: re-enable
            
            this.types.add("Water");
            this.types.add("Ice");
        }
        else if (name == "Spinarak") { //gen I properties
            this.baseStats.put("hp",45);
            this.baseStats.put("attack",50);
            this.baseStats.put("defense",55);
            this.baseStats.put("specialAtk",75);
            this.baseStats.put("specialDef",75);
            this.baseStats.put("speed",30);
            this.baseStats.put("catchRate", 255);
            //sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/spinarak.png"));
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); //my sprites are backwards
            
            this.types.add("Bug");
            this.types.add("Poison");
        }
        else if (name == "Oddish") { //gen I properties
            this.baseStats.put("hp",45);
            this.baseStats.put("attack",50);
            this.baseStats.put("defense",55);
            this.baseStats.put("specialAtk",75);
            this.baseStats.put("specialDef",75);
            this.baseStats.put("speed",30);
            this.baseStats.put("catchRate", 255);
            //sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/oddish.png"));
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); //my sprites are backwards
            
            this.types.add("Grass");
            this.types.add("Poison");
        }
        else if (name == "Gloom") { //gen I properties
            this.baseStats.put("hp",60);
            this.baseStats.put("attack",65);
            this.baseStats.put("defense",70);
            this.baseStats.put("specialAtk",85);
            this.baseStats.put("specialDef",85);
            this.baseStats.put("speed",40);
            this.baseStats.put("catchRate", 120);
            //sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/gloom.png"));
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); //my sprites are backwards
            
            this.types.add("Grass");
            this.types.add("Poison");
        }
        else if (name == "Electabuzz") { //gen I properties
            this.baseStats.put("hp",65);
            this.baseStats.put("attack",83);
            this.baseStats.put("defense",57);
            this.baseStats.put("specialAtk",85);
            this.baseStats.put("specialDef",85);
            this.baseStats.put("speed",105);
            this.baseStats.put("catchRate", 45);
            //sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/electabuzz.png"));
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); //my sprites are backwards
            
            this.types.add("Electric");
        }

        else if (name == "Scyther") { //gen I properties
            this.baseStats.put("hp",70);
            this.baseStats.put("attack",110);
            this.baseStats.put("defense",80);
            this.baseStats.put("specialAtk",55);
            this.baseStats.put("specialDef",55);
            this.baseStats.put("speed",105);
            this.baseStats.put("catchRate", 45);
            //sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/scyther.png"));
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); //my sprites are backwards

            this.types.add("Bug");
        }

        else if (name == "Tauros") { //gen I properties
            this.baseStats.put("hp",75);
            this.baseStats.put("attack",100);
            this.baseStats.put("defense",95);
            this.baseStats.put("specialAtk",70);
            this.baseStats.put("specialDef",70);
            this.baseStats.put("speed",110);
            this.baseStats.put("catchRate", 45);
            //sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/tauros.png"));
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); //my sprites are backwards

            this.types.add("Normal");
        }

        else if (name == "Mareep") { //gen I properties
            this.baseStats.put("hp",55);
            this.baseStats.put("attack",40);
            this.baseStats.put("defense",40);
            this.baseStats.put("specialAtk",65);
            this.baseStats.put("specialDef",45);
            this.baseStats.put("speed",35);
            this.baseStats.put("catchRate", 235);
            //sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/mareep.png"));
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); 

            this.types.add("Electric");
        }
        else if (name == "Flaaffy") { //gen I properties
            this.baseStats.put("hp",70);
            this.baseStats.put("attack",55);
            this.baseStats.put("defense",55);
            this.baseStats.put("specialAtk",80);
            this.baseStats.put("specialDef",60);
            this.baseStats.put("speed",45);
            this.baseStats.put("catchRate", 255);
            //sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/flaaffy.png"));
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); //my sprites are backwards
            
            this.types.add("Electric");
        }

        else if (name == "Steelix") { //gen II properties
            this.baseStats.put("hp",75);
            this.baseStats.put("attack",85);
            this.baseStats.put("defense",200);
            this.baseStats.put("specialAtk",55);
            this.baseStats.put("specialDef",65);
            this.baseStats.put("speed",30);
            this.baseStats.put("catchRate", 25);
            //sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/steelix.png"));
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); //my sprites are backwards

            this.types.add("Steel");
            this.types.add("Ground");
        }
        else if (name == "Sneasel") { //gen I properties
            this.baseStats.put("hp",55);
            this.baseStats.put("attack",95);
            this.baseStats.put("defense",55);
            this.baseStats.put("specialAtk",35);
            this.baseStats.put("specialDef",75);
            this.baseStats.put("speed",115);
            this.baseStats.put("catchRate", 60);
            //sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/sneasel.png"));
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); 

            this.types.add("Dark");
            this.types.add("Ice");
        }

        else if (name == "Suicune") { //gen II properties
            this.baseStats.put("hp",100);
            this.baseStats.put("attack",75);
            this.baseStats.put("defense",115);
            this.baseStats.put("specialAtk",90);
            this.baseStats.put("specialDef",115);
            this.baseStats.put("speed",85);
            this.baseStats.put("catchRate", 3);
            //sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/suicune.png"));
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); //my sprites are backwards

            this.types.add("Water");
        }
        else if (name == "Raikou") { //gen II properties
            this.baseStats.put("hp",90);
            this.baseStats.put("attack",85);
            this.baseStats.put("defense",75);
            this.baseStats.put("specialAtk",115);
            this.baseStats.put("specialDef",100);
            this.baseStats.put("speed",115);
            this.baseStats.put("catchRate", 3);
            //sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/raikou.png"));
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); //my sprites are backwards

            this.types.add("Electric");
        }
        else if (name == "Entei") { //gen II properties
            this.baseStats.put("hp",115);
            this.baseStats.put("attack",115);
            this.baseStats.put("defense",85);
            this.baseStats.put("specialAtk",90);
            this.baseStats.put("specialDef",75);
            this.baseStats.put("speed",100);
            this.baseStats.put("catchRate", 3);
            //sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/entei.png"));
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); //my sprites are backwards

            this.types.add("Electric");
        }
        else if (name == "Wurmple") { //gen IV properties
            this.baseStats.put("hp",45);
            this.baseStats.put("attack",45);
            this.baseStats.put("defense",35);
            this.baseStats.put("specialAtk",20);
            this.baseStats.put("specialDef",30);
            this.baseStats.put("speed",20);
            this.baseStats.put("catchRate", 255);
            //sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/wurmple.png"));
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); 

            this.types.add("Bug");
        }
        else if (name == "Makuhita") { //gen I properties
            this.baseStats.put("hp",72);
            this.baseStats.put("attack",60);
            this.baseStats.put("defense",30);
            this.baseStats.put("specialAtk",20);
            this.baseStats.put("specialDef",30);
            this.baseStats.put("speed",25);
            this.baseStats.put("catchRate", 180);
            //sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/makuhita.png"));
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); //my sprites are backwards

            this.types.add("Fighting");
        }
        
        else if (name == "Hariyama") { //gen I properties
            this.baseStats.put("hp",144);
            this.baseStats.put("attack",120);
            this.baseStats.put("defense",60);
            this.baseStats.put("specialAtk",40);
            this.baseStats.put("specialDef",60);
            this.baseStats.put("speed",50);
            this.baseStats.put("catchRate", 200);
            //sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/hariyama.png"));
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
            //this.sprite.flip(true, false); //this one looks better flipped

            this.types.add("Fighting");
        }
        else if (name == "Skitty") { //gen I properties
            this.baseStats.put("hp",50);
            this.baseStats.put("attack",45);
            this.baseStats.put("defense",45);
            this.baseStats.put("specialAtk",35);
            this.baseStats.put("specialDef",35);
            this.baseStats.put("speed",50);
            this.baseStats.put("catchRate", 255);
            //sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/skitty.png"));
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); //this one looks better flipped

            this.types.add("Normal");
        }
        else if (name == "Sableye") { //gen I properties
            this.baseStats.put("hp",50);
            this.baseStats.put("attack",75);
            this.baseStats.put("defense",75);
            this.baseStats.put("specialAtk",65);
            this.baseStats.put("specialDef",65);
            this.baseStats.put("speed",50);
            this.baseStats.put("catchRate", 45);
            //sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/sableye.png"));
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
            //this.sprite.flip(true, false); //this one looks better flipped

            this.types.add("Dark");
            this.types.add("Ghost");
        }
        else if (name == "Gardevoir") { //gen IV properties
            this.baseStats.put("hp",68);
            this.baseStats.put("attack",65);
            this.baseStats.put("defense",65);
            this.baseStats.put("specialAtk",125);
            this.baseStats.put("specialDef",115);
            this.baseStats.put("speed",80);
            this.baseStats.put("catchRate", 45);
            //sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/gardevoir.png"));
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); //this one looks better flipped

            this.types.add("Psychic");
            //this.types.add("Fairy"); //gen  IV doesn't include
        }
        else if (name == "Claydol") { //gen IV properties
            this.baseStats.put("hp",60);
            this.baseStats.put("attack",70);
            this.baseStats.put("defense",105);
            this.baseStats.put("specialAtk",70);
            this.baseStats.put("specialDef",120);
            this.baseStats.put("speed",75);
            this.baseStats.put("catchRate", 90);
            //sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/claydol.png"));
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); //this one looks better flipped

            this.types.add("Ground");
            this.types.add("Psychic");
            //this.types.add("Fairy"); //gen  IV doesn't include
        }
        else if (name == "Lairon") { //gen IV properties
            this.baseStats.put("hp",60);
            this.baseStats.put("attack",90);
            this.baseStats.put("defense",140);
            this.baseStats.put("specialAtk",50);
            this.baseStats.put("specialDef",50);
            this.baseStats.put("speed",40);
            this.baseStats.put("catchRate", 90);
            //sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/lairon.png"));
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); 

            this.types.add("Steel");
            this.types.add("Rock");
        }
        else if (name == "Cacnea") { //gen IV properties
            this.baseStats.put("hp",50);
            this.baseStats.put("attack",85);
            this.baseStats.put("defense",40);
            this.baseStats.put("specialAtk",85);
            this.baseStats.put("specialDef",40);
            this.baseStats.put("speed",35);
            this.baseStats.put("catchRate", 190);
            //sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/cacnea.png"));
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); //my sprites are backwards

            this.types.add("Grass");
        }
        else if (name == "Shuppet") { //gen IV properties
            this.baseStats.put("hp",44);
            this.baseStats.put("attack",75);
            this.baseStats.put("defense",35);
            this.baseStats.put("specialAtk",63);
            this.baseStats.put("specialDef",33);
            this.baseStats.put("speed",45);
            this.baseStats.put("catchRate", 225);
            //sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/shuppet.png"));
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); //my sprites are backwards

            this.types.add("Grass");
        }
        else if (name == "Starly") { //gen IV properties
            this.baseStats.put("hp",40);
            this.baseStats.put("attack",55);
            this.baseStats.put("defense",30);
            this.baseStats.put("specialAtk",30);
            this.baseStats.put("specialDef",30);
            this.baseStats.put("speed",60);
            this.baseStats.put("catchRate", 255);
            //sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/starly.png"));
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); 

            this.types.add("Normal");
            this.types.add("Flying");
        }
        else if (name == "Shinx") { //gen IV properties
            this.baseStats.put("hp",45);
            this.baseStats.put("attack",65);
            this.baseStats.put("defense",34);
            this.baseStats.put("specialAtk",40);
            this.baseStats.put("specialDef",34);
            this.baseStats.put("speed",45);
            this.baseStats.put("catchRate", 235);
            //sprite
            Texture pokemonText = new Texture(Gdx.files.internal("pokemon/shinx.png"));
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
            this.sprite.flip(true, false); 

            this.types.add("Electric");
        }
        else if (name == "Machop") { // todo: stats are wrong
            this.baseStats.put("hp",45);
            this.baseStats.put("attack",65);
            this.baseStats.put("defense",34);
            this.baseStats.put("specialAtk",40);
            this.baseStats.put("specialDef",34);
            this.baseStats.put("speed",45);
            this.baseStats.put("catchRate", 235);
            //sprite
            Texture pokemonText = new Texture(Gdx.files.internal("crystal_pokemon/machop_front2.png"));
            this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
//            this.sprite.flip(true, false); 

            this.types.add("Fighting");

            this.introAnim = new ArrayList<Sprite>();
            // 23 frames do nothing
            Sprite sprite = new Sprite(pokemonText, 56*0, 0, 56, 56);
//            sprite.flip(true, false);
            for (int i=0; i < 24; i++) {
                this.introAnim.add(sprite);
            }
            // 22 frames mouth open
            sprite = new Sprite(pokemonText, 56*2, 0, 56, 56);
//            sprite.flip(true, false);
            for (int i=0; i < 23; i++) {
                this.introAnim.add(sprite);
            }
            // 13 frames normal
            sprite = new Sprite(pokemonText, 56*0, 0, 56, 56);
//            sprite.flip(true, false);
            for (int i=0; i < 14; i++) {
                this.introAnim.add(sprite);
            }
            // 12 frames fists up
            sprite = new Sprite(pokemonText, 56*1, 0, 56, 56);
//            sprite.flip(true, false);
            for (int i=0; i < 13; i++) {
                this.introAnim.add(sprite);
            }
            // 13 frames normal
            sprite = new Sprite(pokemonText, 56*0, 0, 56, 56);
//            sprite.flip(true, false);
            for (int i=0; i < 14; i++) {
                this.introAnim.add(sprite);
            }
            // 13 frames fists up
            sprite = new Sprite(pokemonText, 56*1, 0, 56, 56);
//            sprite.flip(true, false);
            for (int i=0; i < 14; i++) {
                this.introAnim.add(sprite);
            }
            // 11 frames normal
            sprite = new Sprite(pokemonText, 56*0, 0, 56, 56);
//            sprite.flip(true, false);
            for (int i=0; i < 14; i++) {
                this.introAnim.add(sprite);
            }
        }
        else {
            return;
        }
        
        getCurrentAttacks(); //fill this.attacks with what it can currently know
        
        //stats formulas here
        calcMaxStats();
        this.currentStats = new HashMap<String, Integer>(this.maxStats); //copy maxStats 
        
        
    }
    
    //TODO - this doesn't take IV's or EV's into account.
     //for EV's - I think they only get factored in on pokemon level up. So only call calcMaxStats on level up.
     //if you ever need to reset currentStats, just make a copy of maxStats - like after battle, mist attack, etc
    void calcMaxStats() {

        this.maxStats.put("hp", (((this.baseStats.get("hp") + 50) * this.level) / 50) + 10);
        this.maxStats.put("attack", (((this.baseStats.get("attack")) * this.level) / 50) + 10);
        this.maxStats.put("defense", (((this.baseStats.get("defense")) * this.level) / 50) + 10);
        this.maxStats.put("specialAtk", (((this.baseStats.get("specialAtk")) * this.level) / 50) + 10);
        this.maxStats.put("specialDef", (((this.baseStats.get("specialDef")) * this.level) / 50) + 10);
        this.maxStats.put("speed", (((this.baseStats.get("speed")) * this.level) / 50) + 10);
        
        //catchRate for the sake of including everything
        this.maxStats.put("catchRate", this.baseStats.get("catchRate"));

        //hp = (((IV + Base + (sqrt(EV)/8) + 50)*Level)/50 + 10
        //other stat = (((IV + Base + (sqrt(EV)/8))*Level)/50 + 5
    }

    //when generating a pokemon, this will select which attacks it knows
     //by default
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
}

class SpecialMewtwo1 extends Pokemon {

    Sprite breathingSprite;

    public SpecialMewtwo1(int level) {
        
        // initialize variables
        super("Mewtwo", level);

        //gen I properties
        this.baseStats.put("hp", 106);
        this.baseStats.put("attack", 110);
        this.baseStats.put("defense", 90);
        this.baseStats.put("specialAtk", 154);
        this.baseStats.put("specialDef", 154);
        this.baseStats.put("speed", 130);
        this.baseStats.put("catchRate", 3);
        
        //sprite
        Texture pokemonText = new Texture(Gdx.files.internal("pokemon/mewtwo_special1.png"));
        this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);
        
        pokemonText = new Texture(Gdx.files.internal("pokemon/mewtwo_special2.png"));
        this.breathingSprite = new Sprite(pokemonText, 0, 0, 56, 56);

//        this.learnSet.put(1, new String[]{"Confusion", "Disable", "Psychic", "Swift"});
        this.learnSet.put(1, new String[]{"Psychic", "Psychic", "Psychic", "Psychic"});
        this.types.add("Psychic");

        getCurrentAttacks(); //fill this.attacks with what it can currently know
        
        //stats formulas here
        calcMaxStats();
        this.currentStats = new HashMap<String, Integer>(this.maxStats); //copy maxStats 
    }
}


class SpecialMegaGengar1 extends Pokemon {

    // TODO: remove, in base class
//    Sprite breathingSprite;

    public SpecialMegaGengar1(int level) {
        
        // initialize variables
        super("Mega Gengar", level);

        //gen 7 stats
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
        
        //sprite
        Texture pokemonText = new Texture(Gdx.files.internal("pokemon/mgengar_base1.png"));
        this.breathingSprite = new Sprite(pokemonText, 0, 0, 56, 56);
        
        pokemonText = new Texture(Gdx.files.internal("pokemon/mgengar_over1.png"));
        this.sprite = new Sprite(pokemonText, 0, 0, 56, 56);

//        this.learnSet.put(1, new String[]{"Confusion", "Disable", "Psychic", "Swift"});
//        this.learnSet.put(1, new String[]{"Psychic", "Psychic", "Psychic", "Psychic"});
//        this.learnSet.put(1, new String[]{"Night Shade", "Night Shade", "Night Shade", "Night Shade"}); //, "Lick"
        this.learnSet.put(1, new String[]{"Shadow Claw", "Night Shade", "Lick", "-"}); //, "Lick"
        this.types.add("Ghost");
        this.types.add("Poison");

        getCurrentAttacks(); //fill this.attacks with what it can currently know
        
        //stats formulas here
        calcMaxStats();
        this.currentStats = new HashMap<String, Integer>(this.maxStats); //copy maxStats 
    }
}