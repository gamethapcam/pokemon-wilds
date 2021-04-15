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
import com.pkmngen.game.Pokemon.Generation;
import com.pkmngen.game.util.SpriteProxy;
import com.pkmngen.game.util.TextureCache;

public class Specie {
    // Contains all loaded pkmn textures, so that only one is used for each pkmn. ie don't load duplicate textures.
    public static HashMap<String, Texture> textures = new HashMap<String, Texture>();
    // Add to this when loading pokemon
    public static HashMap<String, Map<String, String>> gen2Evos = new HashMap<String, Map<String, String>>();
    public static HashMap<String, Map<Integer, String[]>> gen2Attacks = new HashMap<String, Map<Integer, String[]>>();
    public static HashMap<String, ArrayList<String>> eggMoves = new HashMap<String, ArrayList<String>>();
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
                                     "darmanitan", "darmanitanzen",
                                     "elgyem", "beheeyem",  // Goose on discord
                                     "sandile", "krokorok", "krookodile",  // Goose and Sadfish on discord
                                     "cutiefly", "ribombee",  // TerraTerraCotta on discord
                                     "combee", "vespiquen",  // TerraTerraCotta on discord
                                     "nosepass",  // nuuk, ow sadfish on discord
                                     "sigilyph",  // sadfish on discord, ow dustman on discord
                                     "snover"};  // TODO: sep loading method
        for (String t : temp) {
            nuukPokemon.add(t);
        }
    }

    public static HashMap<String, Specie> species = new HashMap<String, Specie>();

    String name;
    String dexNumber;
    String genderRatio;
    String[] eggGroups = new String[2];
    int baseHappiness = 70; //base is 70 for all pokemon in Gen II
    ArrayList<String> types;
    //	String eggHatchInto = null;
    String growthRateGroup = "";
    Map<Integer, String[]> learnSet= new HashMap<Integer, String[]>();
    ArrayList<String> hms = new ArrayList<String>();
    Map<String, Integer> baseStats = new HashMap<String, Integer>();

    Generation generation;

    SpriteProxy sprite;
    SpriteProxy backSprite;
    SpriteProxy spriteShiny;
    SpriteProxy backSpriteShiny;
    static SpriteProxy spriteGhost;
    static SpriteProxy spriteEgg;
    static SpriteProxy backSpriteEgg;
    ArrayList<SpriteProxy> introAnim;
    ArrayList<SpriteProxy> introAnimShiny;
    static ArrayList<SpriteProxy> introAnimGhost;
    static ArrayList<SpriteProxy> introAnimEgg;
    static {
        Texture text = null;

        // Load sprite and animation data (cached)
        if (!Specie.textures.containsKey("egg_front")) {
            // Load front sprite
            text = TextureCache.get(Gdx.files.internal("crystal_pokemon/pokemon/egg/front.png"));
            Specie.textures.put("egg_front", text);
            // back sprites
            text = TextureCache.get(Gdx.files.internal("crystal_pokemon/pokemon/egg/back.png"));
            Specie.textures.put("egg_back", text);

        }

        // Load sprite and animation data (cached)
        if (!Specie.textures.containsKey("ghost_front")) {
            // Load front sprite
            text = TextureCache.get(Gdx.files.internal("crystal_pokemon/prism/pics/ghost/front.png"));
            Specie.textures.put("ghost_front", text);
        }

        Texture pokemonText = Specie.textures.get("egg_front");
        // height and width are the same for these sprites
        int height = pokemonText.getWidth();
        spriteEgg = new SpriteProxy(pokemonText, 0, 0, height, height);
        pokemonText = Specie.textures.get("egg_back");
        backSpriteEgg = new SpriteProxy(pokemonText, 0, 0, 48, 48);

        pokemonText = Specie.textures.get("ghost_front");
        height = pokemonText.getWidth();
        spriteGhost = new SpriteProxy(pokemonText, 0, 0, height, height);


        //Ghost & egg intro animation
        // Load animation(s) from file
        introAnimGhost = new ArrayList<SpriteProxy>();
        introAnimEgg = new ArrayList<SpriteProxy>();
        try {
            FileHandle file = Gdx.files.internal("crystal_pokemon/prism/pics/ghost/anim0.asm");
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
                        pokemonText = Specie.textures.get("ghost_front");
                        SpriteProxy sprite = new SpriteProxy(pokemonText, 0, height*frame, height, height);
                        introAnimGhost.add(sprite);
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
            //Why yes, this *IS* kinda stupid
            file = Gdx.files.internal("crystal_pokemon/pokemon/egg/anim.asm");
            reader = file.reader();
            br = new BufferedReader(reader);
            setrepeat = 0;
            lines = new ArrayList<String>();
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
                        pokemonText = Specie.textures.get("egg_front");
                        height = pokemonText.getWidth();
                        SpriteProxy sprite = new SpriteProxy(pokemonText, 0, height*frame, height, height);
                        introAnimEgg.add(sprite);
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


    public int spriteOffsetY = 0;

    // overworld movement sprites
    public Map<String, Sprite> standingSprites = new HashMap<String, Sprite>();
    public Map<String, Sprite> movingSprites = new HashMap<String, Sprite>();
    public Map<String, Sprite> altMovingSprites = new HashMap<String, Sprite>();
    ArrayList<Sprite> avatarSprites = new ArrayList<Sprite>();

    public Map<String, Sprite> standingSpritesEgg = new HashMap<String, Sprite>();
    public Map<String, Sprite> movingSpritesEgg = new HashMap<String, Sprite>();
    public Map<String, Sprite> altMovingSpritesEgg = new HashMap<String, Sprite>();
    ArrayList<Sprite> avatarSpritesEgg = new ArrayList<Sprite>();

    public int harvestTimerMax = (int)(3600f*2.5f);
    public ArrayList<String> harvestables = new ArrayList<String>();
    {
        harvestables.add("manure");
    }
    public ArrayList<String> habitats = new ArrayList<String>();
    {
        habitats.add("green");
    }

    Specie(String name){
        this(name, Generation.CRYSTAL);
    }

    Specie(String name, Generation gen){
        this.init(name, gen);
    }

    public void init(String n, Generation generation) {

        this.name = n.toLowerCase();

        //        this.eggHatchInto = eggHatchInto;


        if (this.name.equals("unown")) {  // TODO: this was to fix a bug, remove
            this.name = "unown_w";
        }

        this.generation = generation;

        this.types = new ArrayList<String>();

        this.learnSet = new HashMap<Integer, String[]>();

        // TODO: individual avatars
        // TODO: remove if unused
        //        Texture avatarText = new Texture(Gdx.files.internal("pokemon_menu/avatars1.png"));
        //        this.avatarSprites = new ArrayList<Sprite>();
        //        this.avatarSprites.add(new Sprite(avatarText, 16*0, 16*0, 16, 16));
        //        this.avatarSprites.add(new Sprite(avatarText, 16*1, 16*0, 16, 16));

        // if generation is crystal, load from file
        if (generation.equals(Generation.CRYSTAL)) {
            this.dexNumber = Pokemon.nameToIndex(this.name);
            // if it is in original 251, load from crystal
            if (Specie.nuukPokemon.contains(this.name.toLowerCase()) ||
                    (Integer.valueOf(this.dexNumber) <= 251 && Integer.valueOf(this.dexNumber) > 0)) {
                this.loadCrystalPokemon();
                // else try loading from prism
            } else {
                System.out.println(this.dexNumber + ", " + Integer.valueOf(this.dexNumber));
                this.loadPrismPokemon();
            }
            this.loadOverworldSprites();

            // Custom attributes - better way to handle this?
            //            if (name.equals("machop")) {
            //                this.hms.add("CUT");  // TODO: debug, remove
            ////                this.hms.add("BUILD");
            //            }

            // Custom attributes - better way to handle this?
            if (name.equals("sneasel") || 
                    name.equals("scyther") ||
                    name.equals("pinsir")) {
                this.hms.add("CUT");
            }

            //
            if (this.types.contains("GROUND")) {
                this.hms.add("DIG");
            }
            if (this.types.contains("ELECTRIC")) {
                this.hms.add("POWER");
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

        else {
            return;
        }


        this.initHabitatValues();

        
    }



    /**
     * Load pokemon sprites and data from crystal_pokemon files.
     */
    void loadCrystalPokemon() {


        String newName = name;
        if (name.contains("unown")) {
            newName = "unown";
        }
        //        if (name.equals("egg")) {
        //            newName = this.eggHatchInto;
        //        }
        String path = "";
        if (Specie.nuukPokemon.contains(newName)) {
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
                    String prevType = "NORMAL";
                    for (String type : types) {
                        // TODO: pure fairy types won't be supported here.
                        if (!Game.fairyTypeEnabled && type.equals("FAIRY")) {
                            continue;
                        }
                        prevType = type;
                        this.types.add(type);
                    }
                    // This adds support for pure fairy types
                    // and fills in empty types with same type
                    if (this.types.isEmpty()) {
                        this.types.add("NORMAL");
                    }
                    if (this.types.size() < 2) {
                        this.types.add(prevType);
                    }
//                    this.types.add(types[0]);  // TODO: remove
//                    this.types.add(types[1]);
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
                    this.genderRatio = line.split("db ")[1].split(" ;")[0];
                // Egg cycles to hatch
                } else if (lineNum == 11) {
                    // TODO: I think need to have sep variable this.eggCycles here.
                    String eggCycles = line.split("db ")[1].split(" ;")[0];
                    this.baseHappiness = Integer.valueOf(eggCycles);
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

        //        if (name.equals("egg")) {
        //            path = "";  // all egg sprites loaded from crystal
        //        }

        // Load sprite and animation data (cached)
        if (!Specie.textures.containsKey(name+"_front")) {
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
            Specie.textures.put(name+"_front", text);

            // Swap palette for shiny texture (load by appending '_shiny' to the texture name)
            // Load shiny palette from file
            Color normalColor1 = null;
            Color normalColor2 = new Color();
            Color shinyColor1 = null;
            Color shinyColor2 = new Color();
            // TODO: this is just a hack workaround
            if (Specie.nuukPokemon.contains(newName)) {
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

                // TODO: there were occasoinally bugs with this (charizard, charmander)
                // TODO: remove
//                if (name.contains("unown")) {
//                    file = Gdx.files.internal("crystal_pokemon/"+path+"pokemon/" + newName + "/normal.pal");
//                }
//                else {
//                    file = Gdx.files.internal("crystal_pokemon/"+path+"pokemon/" + name + "/front.pal");
//                }
//                // TODO: don't really need anymore b/c SpriteProxy knows color1/color2
//                if (file.exists()) {
//                    reader = file.reader();
//                    br = new BufferedReader(reader);
//                    while ((line = br.readLine()) != null)   {
//                        if (line.contains("RGB")) {
//                            String[] vals = line.split("\tRGB ")[1].split(", ");
//                            if (normalColor1 == null) {
//                                // TODO: this is wrong, these values range from 0-32 but .r .g .b should be floats.
//                                normalColor1 = new Color();
//                                normalColor1.r = Integer.valueOf(vals[0]);
//                                normalColor1.g = Integer.valueOf(vals[1]);
//                                normalColor1.b = Integer.valueOf(vals[2]);
//                            }
//                            else {
//                                normalColor2.r = Integer.valueOf(vals[0]);
//                                normalColor2.g = Integer.valueOf(vals[1]);
//                                normalColor2.b = Integer.valueOf(vals[2]);
//                            }
//                        }
//                    }
//                    reader.close();
//                }
//                else {
                    SpriteProxy tempSprite = new SpriteProxy(Specie.textures.get(name+"_front"),
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
//                }
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
            Specie.textures.put(name+"_front_shiny", text);

            // back sprites
            text = TextureCache.get(Gdx.files.internal("crystal_pokemon/"+path+"pokemon/" + name + "/back.png"));
            Specie.textures.put(name+"_back", text);
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
            Specie.textures.put(name+"_back_shiny", text);
        }

        Texture pokemonText = Specie.textures.get(name+"_front");
        // height and width are the same for these sprites
        int height = pokemonText.getWidth();
        this.sprite = new SpriteProxy(pokemonText, 0, 0, height, height);
        pokemonText = Specie.textures.get(name+"_back");
        this.backSprite = new SpriteProxy(pokemonText, 0, 0, 48, 48);
        //shiny sprites
        pokemonText = Specie.textures.get(name+"_front_shiny");
        height = pokemonText.getWidth();
        this.spriteShiny = new SpriteProxy(pokemonText, 0, 0, height, height);
        pokemonText = Specie.textures.get(name+"_back_shiny");
        this.backSpriteShiny = new SpriteProxy(pokemonText, 0, 0, 48, 48);

        // Load animation(s) from file
        this.introAnim = new ArrayList<SpriteProxy>();
        this.introAnimShiny = new ArrayList<SpriteProxy>();
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
                        pokemonText = Specie.textures.get(name+"_front");
                        SpriteProxy sprite = new SpriteProxy(pokemonText, 0, height*frame, height, height);
                        this.introAnim.add(sprite);
                        pokemonText = Specie.textures.get(name+"_front_shiny");
                        sprite = new SpriteProxy(pokemonText, 0, height*frame, height, height);
                        this.introAnimShiny.add(sprite);
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
        if (Specie.nuukPokemon.contains(newName)) {
            path = "nuuk/";
        }

        // Load attacks from file
        // TODO: just load all of this statically in static {} block (?)
        if (!Specie.gen2Attacks.containsKey(newName) || !Specie.gen2Evos.containsKey(newName)) {
            Map<Integer, String[]> attacks = new HashMap<Integer, String[]>();
            Map<String, String> evos = new HashMap<String, String>();

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
                    else if (line.contains("EVOLVE_ITEM") || line.contains("EVOLVE_TRADE") || line.contains("EVOLVE_HAPPINESS") || line.contains("EVOLVE_MOVE") || line.contains("EVOLVE_STAT")) {
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
                        if (Pokemon.attacksImplemented.contains(attack)) {
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
            Specie.gen2Attacks.put(newName, attacks);
            Specie.gen2Evos.put(newName, evos);
        }
        this.learnSet = Specie.gen2Attacks.get(newName);
    }


    void loadOverworldSprites() {


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

                line = lines.get(i);

                // TODO: no overworld sprites for some pokemon
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
                else if (name.equals("darmanitan")) {
                    i = 334;
                    found = true;
                    flip = false;
                }
                else if (name.equals("darmanitanzen")) {
                    i = 335;
                    found = true;
                    flip = false;
                }
                else if (name.equals("cacturne")) {
                    i = 336;
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
                else if (name.equals("sigilyph")) {
                    i = 343;
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
                else if (name.equals("nosepass")) {
                    i = 347;
                    found = true;
                    flip = false;
                }
                else if (name.equals("vespiquen")) {
                    i = 348;
                    found = true;
                    flip = false;
                }
                else if (name.equals("beheeyem")) {
                    i = 349;
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


            // If this is an egg, load special texture for the overworld sprite
            // If cached egg texture doesn't exist in TextureCache.eggTextures, make
            // a new pixmap with colors replaced.
            String path = "";
            if (Specie.nuukPokemon.contains(this.name)) {
                path = "nuuk/";
            }

            loadEggTextures("crystal_pokemon/"+path+"pokemon/" + this.name + "/back.png");
            //            }
            if (found) {
                return;
            }
            // else, load from crystal overworld sprite sheet
            //            else {
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
            //            }

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

    void loadPrismPokemon() {


        //        if (this.name.equals("egg")) {
        //            name = this.eggHatchInto;
        //        }

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
                    int numericRatio = Integer.valueOf(genderRatio);
                    if (numericRatio == 31) {
                        this.genderRatio = "GENDER_F12_5";
                    }
                    else if (numericRatio == 63) {
                        this.genderRatio = "GENDER_F25";
                    }
                    else if (numericRatio == 127) {
                        this.genderRatio = "GENDER_F50";
                    }
                    else if (numericRatio == 191) {
                        this.genderRatio = "GENDER_F75";
                    }
                    // Ex: illumise
                    else if (numericRatio == 254) {
                        this.genderRatio = "GENDER_F100";
                    }
                    // For some reason 255 denotes genderless
                    else if (numericRatio == 255) {
                        this.genderRatio = "GENDER_UNKNOWN";
                    }

                    // Egg cycles to hatch
                } else if (lineNum == 9) {
                    String eggCycles = line.split("db ")[1].split(" ;")[0];
                    this.baseHappiness = Integer.valueOf(eggCycles);
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
        if (!Specie.textures.containsKey(name+"_front")) {
            Texture text = TextureCache.get(Gdx.files.internal("crystal_pokemon/prism/pics/" + name + "/front.png"));
            Specie.textures.put(name+"_front", text);

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
            Specie.textures.put(name+"_front_shiny", text);

            // Back sprites
            text = TextureCache.get(Gdx.files.internal("crystal_pokemon/prism/pics/" + name + "/back.png"));

            //also try to load Egg textures
            loadEggTextures("crystal_pokemon/prism/pics/" + name + "/back.png");


            Specie.textures.put(name+"_back_shiny", text);
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
            Specie.textures.put(name+"_back", text);
        }
        //        Texture pokemonText = new Texture(Gdx.files.internal("crystal_pokemon/prism/pics/" + name + "/front.png"));  // TODO: remove

        Texture pokemonText = Specie.textures.get(name+"_front");
        // height and width are the same for these sprites
        int height = pokemonText.getWidth();
        this.sprite = new SpriteProxy(pokemonText, 0, 0, height, height);
        pokemonText = Specie.textures.get(name+"_front_shiny");
        System.out.println(name);
        height = pokemonText.getWidth();
        this.spriteShiny = new SpriteProxy(pokemonText, 0, 0, height, height);
        //        if (!Specie.textures.containsKey(name+"_back")) {
        //            Specie.textures.put(name+"_back", new Texture(Gdx.files.internal("crystal_pokemon/prism/pics/" + name + "/back.png")));
        //        }
        //        pokemonText = new Texture(Gdx.files.internal("crystal_pokemon/prism/pics/" + name + "/back.png"));  // TODO: remove
        pokemonText = Specie.textures.get(name+"_back");
        this.backSprite = new SpriteProxy(pokemonText, 0, 0, 48, 48);
        pokemonText = Specie.textures.get(name+"_back");
        this.backSpriteShiny = new SpriteProxy(pokemonText, 0, 0, 48, 48);

        // Load animation from file
        this.introAnim = new ArrayList<SpriteProxy>();
        this.introAnimShiny = new ArrayList<SpriteProxy>();
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
                        pokemonText = Specie.textures.get(name+"_front");
                        SpriteProxy sprite = new SpriteProxy(pokemonText, 0, height*frame, height, height);
                        this.introAnim.add(sprite);
                        pokemonText = Specie.textures.get(name+"_front_shiny");
                        sprite = new SpriteProxy(pokemonText, 0, height*frame, height, height);
                        this.introAnimShiny.add(sprite);
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
        if (!Specie.gen2Attacks.containsKey(name) || !Specie.gen2Evos.containsKey(name)) {
            Map<Integer, String[]> attacks = new HashMap<Integer, String[]>();
            Map<String, String> evos = new HashMap<String, String>();

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
            Specie.gen2Attacks.put(name, attacks);
            Specie.gen2Evos.put(name, evos);
        }
        this.learnSet = Specie.gen2Attacks.get(name);
    }

    void loadEggTextures(String path) {
        Texture text;
        if (!TextureCache.eggTextures.containsKey(this.name)) {
            // TODO: won't work for prism pokemon
            text = TextureCache.get(Gdx.files.internal(path));
            int height = text.getWidth();
            SpriteProxy tempSprite = new SpriteProxy(text, 0, 0, height, height);
            //            System.out.println(tempSprite.color1);
            //            System.out.println(tempSprite.color2);
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
            TextureCache.eggTextures.put(this.name, TextureCache.get(newPixmap));
        }
        text = TextureCache.eggTextures.get(this.name);
        for (String dir : new String[]{"up", "down", "left", "right"}) {
            this.standingSpritesEgg.put(dir, new Sprite(text, 0, 0, 16, 16));
            this.movingSpritesEgg.put(dir, new Sprite(text, 0, 32, 16, 16));
            this.altMovingSpritesEgg.put(dir, new Sprite(text, 0, 48, 16, 16));
        }
        Sprite hopSprite = new Sprite(text, 0, 16, 16, 16);
        this.avatarSpritesEgg.add(this.standingSpritesEgg.get("down"));
        this.avatarSpritesEgg.add(hopSprite);
        this.avatarSpritesEgg.add(this.standingSpritesEgg.get("down"));
        this.avatarSpritesEgg.add(hopSprite);
    }

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
            this.habitats.add("mountain|sand|desert");
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
}
