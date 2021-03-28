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

public class Specie{
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
                                     "dwebble",  // overworld 
                                     "crustle",  // overworld 
                                     "litwick", "lampent", "chandelure",  // overworld Goose (discord)
                                     "corphish",  // sir-feralipogchamp (discord)
                                     "crawdaunt",  // sir-feralipogchamp, Mr Dustman, Goose (discord)
                                     "mimikyu",  // boomtox-the-boombox (discord)
                                     "scorbunny",  // Internet_Goblin on discord
                                     "raboot",  // Internet_Goblin on discord
                                     "regieleki", "regidrago", "registeel", "regirock", "regice", "regigigas", // Mr Dustman and Sadfish on discord
                                     "snover"};  // TODO: sep loading method
        for (String t : temp) {
            nuukPokemon.add(t);
        }
    }
	
	
	public static HashMap<String,Specie> species = new HashMap<String,Specie>();
    static ArrayList<String> specieNames = new ArrayList<String>();
    
    static {

    	specieNames.add("Bulbasaur");
    	specieNames.add("Ivysaur");
    	specieNames.add("Venusaur");
    	specieNames.add("Charmander");
    	specieNames.add("Charmeleon");
    	specieNames.add("Charizard");
    	specieNames.add("Squirtle");
    	specieNames.add("Wartortle");
    	specieNames.add("Blastoise");
    	specieNames.add("Caterpie");
    	specieNames.add("Metapod");
    	specieNames.add("Butterfree");
    	specieNames.add("Weedle");
    	specieNames.add("Kakuna");
    	specieNames.add("Beedrill");
    	specieNames.add("Pidgey");
    	specieNames.add("Pidgeotto");
    	specieNames.add("Pidgeot");
    	specieNames.add("Rattata");
    	specieNames.add("Raticate");
    	specieNames.add("Spearow");
    	specieNames.add("Fearow");
    	specieNames.add("Ekans");
    	specieNames.add("Arbok");
    	specieNames.add("Pikachu");
    	specieNames.add("Raichu");
    	specieNames.add("Sandshrew");
    	specieNames.add("Sandslash");
    	specieNames.add("Nidoran_m");
    	specieNames.add("Nidorina");
    	specieNames.add("Nidoqueen");
    	specieNames.add("Nidoran_f");
    	specieNames.add("Nidorino");
    	specieNames.add("Nidoking");
    	specieNames.add("Clefairy");
    	specieNames.add("Clefable");
    	specieNames.add("Vulpix");
    	specieNames.add("Ninetales");
    	specieNames.add("Jigglypuff");
    	specieNames.add("Wigglytuff");
    	specieNames.add("Zubat");
    	specieNames.add("Golbat");
    	specieNames.add("Oddish");
    	specieNames.add("Gloom");
    	specieNames.add("Vileplume");
    	specieNames.add("Paras");
    	specieNames.add("Parasect");
    	specieNames.add("Venonat");
    	specieNames.add("Venomoth");
    	specieNames.add("Diglett");
    	specieNames.add("Dugtrio");
    	specieNames.add("Meowth");
    	specieNames.add("Persian");
    	specieNames.add("Psyduck");
    	specieNames.add("Golduck");
    	specieNames.add("Mankey");
    	specieNames.add("Primeape");
    	specieNames.add("Growlithe");
    	specieNames.add("Arcanine");
    	specieNames.add("Poliwag");
    	specieNames.add("Poliwhirl");
    	specieNames.add("Poliwrath");
    	specieNames.add("Abra");
    	specieNames.add("Kadabra");
    	specieNames.add("Alakazam");
    	specieNames.add("Machop");
    	specieNames.add("Machoke");
    	specieNames.add("Machamp");
    	specieNames.add("Bellsprout");
    	specieNames.add("Weepinbell");
    	specieNames.add("Victreebel");
    	specieNames.add("Tentacool");
    	specieNames.add("Tentacruel");
    	specieNames.add("Geodude");
    	specieNames.add("Graveler");
    	specieNames.add("Golem");
    	specieNames.add("Ponyta");
    	specieNames.add("Rapidash");
    	specieNames.add("Slowpoke");
    	specieNames.add("Slowbro");
    	specieNames.add("Magnemite");
    	specieNames.add("Magneton");
    	specieNames.add("Farfetch_d");
    	specieNames.add("Doduo");
    	specieNames.add("Dodrio");
    	specieNames.add("Seel");
    	specieNames.add("Dewgong");
    	specieNames.add("Grimer");
    	specieNames.add("Muk");
    	specieNames.add("Shellder");
    	specieNames.add("Cloyster");
    	specieNames.add("Gastly");
    	specieNames.add("Haunter");
    	specieNames.add("Gengar");
    	specieNames.add("Onix");
    	specieNames.add("Drowzee");
    	specieNames.add("Hypno");
    	specieNames.add("Krabby");
    	specieNames.add("Kingler");
    	specieNames.add("Voltorb");
    	specieNames.add("Electrode");
    	specieNames.add("Exeggcute");
    	specieNames.add("Exeggutor");
    	specieNames.add("Cubone");
    	specieNames.add("Marowak");
    	specieNames.add("Hitmonlee");
    	specieNames.add("Hitmonchan");
    	specieNames.add("Lickitung");
    	specieNames.add("Koffing");
    	specieNames.add("Weezing");
    	specieNames.add("Rhyhorn");
    	specieNames.add("Rhydon");
    	specieNames.add("Chansey");
    	specieNames.add("Tangela");
    	specieNames.add("Kangaskhan");
    	specieNames.add("Horsea");
    	specieNames.add("Seadra");
    	specieNames.add("Goldeen");
    	specieNames.add("Seaking");
    	specieNames.add("Staryu");
    	specieNames.add("Starmie");
    	specieNames.add("Mr__Mime");
    	specieNames.add("Scyther");
    	specieNames.add("Jynx");
    	specieNames.add("Electabuzz");
    	specieNames.add("Magmar");
    	specieNames.add("Pinsir");
    	specieNames.add("Tauros");
    	specieNames.add("Magikarp");
    	specieNames.add("Gyarados");
    	specieNames.add("Lapras");
    	specieNames.add("Ditto");
    	specieNames.add("Eevee");
    	specieNames.add("Vaporeon");
    	specieNames.add("Jolteon");
    	specieNames.add("Flareon");
    	specieNames.add("Porygon");
    	specieNames.add("Omanyte");
    	specieNames.add("Omastar");
    	specieNames.add("Kabuto");
    	specieNames.add("Kabutops");
    	specieNames.add("Aerodactyl");
    	specieNames.add("Snorlax");
    	specieNames.add("Articuno");
    	specieNames.add("Zapdos");
    	specieNames.add("Moltres");
    	specieNames.add("Dratini");
    	specieNames.add("Dragonair");
    	specieNames.add("Dragonite");
    	specieNames.add("Mewtwo");
    	specieNames.add("Mew");
    	specieNames.add("Chikorita");
    	specieNames.add("Bayleef");
    	specieNames.add("Meganium");
    	specieNames.add("Cyndaquil");
    	specieNames.add("Quilava");
    	specieNames.add("Typhlosion");
    	specieNames.add("Totodile");
    	specieNames.add("Croconaw");
    	specieNames.add("Feraligatr");
    	specieNames.add("Sentret");
    	specieNames.add("Furret");
    	specieNames.add("Hoothoot");
    	specieNames.add("Noctowl");
    	specieNames.add("Ledyba");
    	specieNames.add("Ledian");
    	specieNames.add("Spinarak");
    	specieNames.add("Ariados");
    	specieNames.add("Crobat");
    	specieNames.add("Chinchou");
    	specieNames.add("Lanturn");
    	specieNames.add("Pichu");
    	specieNames.add("Cleffa");
    	specieNames.add("Igglybuff");
    	specieNames.add("Togepi");
    	specieNames.add("Togetic");
    	specieNames.add("Natu");
    	specieNames.add("Xatu");
    	specieNames.add("Mareep");
    	specieNames.add("Flaaffy");
    	specieNames.add("Ampharos");
    	specieNames.add("Bellossom");
    	specieNames.add("Marill");
    	specieNames.add("Azumarill");
    	specieNames.add("Sudowoodo");
    	specieNames.add("Politoed");
    	specieNames.add("Hoppip");
    	specieNames.add("Skiploom");
    	specieNames.add("Jumpluff");
    	specieNames.add("Aipom");
    	specieNames.add("Sunkern");
    	specieNames.add("Sunflora");
    	specieNames.add("Yanma");
    	specieNames.add("Wooper");
    	specieNames.add("Quagsire");
    	specieNames.add("Espeon");
    	specieNames.add("Umbreon");
    	specieNames.add("Murkrow");
    	specieNames.add("Slowking");
    	specieNames.add("Misdreavus");
    	specieNames.add("Unown");
    	specieNames.add("Wobbuffet");
    	specieNames.add("Girafarig");
    	specieNames.add("Pineco");
    	specieNames.add("Forretress");
    	specieNames.add("Dunsparce");
    	specieNames.add("Gligar");
    	specieNames.add("Steelix");
    	specieNames.add("Snubbull");
    	specieNames.add("Granbull");
    	specieNames.add("Qwilfish");
    	specieNames.add("Scizor");
    	specieNames.add("Shuckle");
    	specieNames.add("Heracross");
    	specieNames.add("Sneasel");
    	specieNames.add("Teddiursa");
    	specieNames.add("Ursaring");
    	specieNames.add("Slugma");
    	specieNames.add("Magcargo");
    	specieNames.add("Swinub");
    	specieNames.add("Piloswine");
    	specieNames.add("Corsola");
    	specieNames.add("Remoraid");
    	specieNames.add("Octillery");
    	specieNames.add("Delibird");
    	specieNames.add("Mantine");
    	specieNames.add("Skarmory");
    	specieNames.add("Houndour");
    	specieNames.add("Houndoom");
    	specieNames.add("Kingdra");
    	specieNames.add("Phanpy");
    	specieNames.add("Donphan");
    	specieNames.add("Porygon2");
    	specieNames.add("Stantler");
    	specieNames.add("Smeargle");
    	specieNames.add("Tyrogue");
    	specieNames.add("Hitmontop");
    	specieNames.add("Smoochum");
    	specieNames.add("Elekid");
    	specieNames.add("Magby");
    	specieNames.add("Miltank");
    	specieNames.add("Blissey");
    	specieNames.add("Raikou");
    	specieNames.add("Entei");
    	specieNames.add("Suicune");
    	specieNames.add("Larvitar");
    	specieNames.add("Pupitar");
    	specieNames.add("Tyranitar");
    	specieNames.add("Lugia");
    	specieNames.add("Ho_Oh");
    	specieNames.add("Celebi");
//    	specieNames.add("Treecko");
//    	specieNames.add("Grovyle");
//    	specieNames.add("Sceptile");
//    	specieNames.add("Torchic");
//    	specieNames.add("Combusken");
//    	specieNames.add("Blaziken");
//    	specieNames.add("Mudkip");
//    	specieNames.add("Marshtomp");
//    	specieNames.add("Swampert");
    	specieNames.add("Poochyena");
    	specieNames.add("Mightyena");
//    	specieNames.add("Zigzagoon");
//    	specieNames.add("Linoone");
//    	specieNames.add("Wurmple");
//    	specieNames.add("Silcoon");
//    	specieNames.add("Beautifly");
//    	specieNames.add("Cascoon");
//    	specieNames.add("Dustox");
    	specieNames.add("Lotad");
    	specieNames.add("Lombre");
    	specieNames.add("Ludicolo");
//    	specieNames.add("Seedot");
//    	specieNames.add("Nuzleaf");
//    	specieNames.add("Shiftry");
    	specieNames.add("Taillow");
    	specieNames.add("Swellow");
    	specieNames.add("Wingull");
    	specieNames.add("Pelipper");
    	specieNames.add("Ralts");
    	specieNames.add("Kirlia");
    	specieNames.add("Gardevoir");
    	specieNames.add("Surskit");
    	specieNames.add("Masquerain");
    	specieNames.add("Shroomish");
    	specieNames.add("Breloom");
//    	specieNames.add("Slakoth");
//    	specieNames.add("Vigoroth");
//    	specieNames.add("Slaking");
//    	specieNames.add("Nincada");
//    	specieNames.add("Ninjask");
//    	specieNames.add("Shedinja");
    	specieNames.add("Whismur");
    	specieNames.add("Loudred");
    	specieNames.add("Exploud");
    	specieNames.add("Makuhita");
    	specieNames.add("Hariyama");
//    	specieNames.add("Azurill");
//    	specieNames.add("Nosepass");
//    	specieNames.add("Skitty");
//    	specieNames.add("Delcatty");
    	specieNames.add("Sableye");
//    	specieNames.add("Mawile");
    	specieNames.add("Aron");
    	specieNames.add("Lairon");
    	specieNames.add("Aggron");
//    	specieNames.add("Meditite");
//    	specieNames.add("Medicham");
//    	specieNames.add("Electrike");
//    	specieNames.add("Manectric");
//    	specieNames.add("Plusle");
//    	specieNames.add("Minun");
//    	specieNames.add("Volbeat");
//    	specieNames.add("Illumise");
//    	specieNames.add("Roselia");
//    	specieNames.add("Gulpin");
//    	specieNames.add("Swalot");
//    	specieNames.add("Carvanha");
//    	specieNames.add("Sharpedo");
//    	specieNames.add("Wailmer");
//    	specieNames.add("Wailord");
//    	specieNames.add("Numel");
//    	specieNames.add("Camerupt");
//    	specieNames.add("Torkoal");
//    	specieNames.add("Spoink");
//    	specieNames.add("Grumpig");
//    	specieNames.add("Spinda");
//    	specieNames.add("Trapinch");
//    	specieNames.add("Vibrava");
//    	specieNames.add("Flygon");
//    	specieNames.add("Cacnea");
//    	specieNames.add("Cacturne");
//    	specieNames.add("Swablu");
//    	specieNames.add("Altaria");
//    	specieNames.add("Zangoose");
//    	specieNames.add("Seviper");
//    	specieNames.add("Lunatone");
//    	specieNames.add("Solrock");
//    	specieNames.add("Barboach");
//    	specieNames.add("Whiscash");
    	specieNames.add("Corphish");
    	specieNames.add("Crawdaunt");
//    	specieNames.add("Baltoy");
//    	specieNames.add("Claydol");
//    	specieNames.add("Lileep");
//    	specieNames.add("Cradily");
//    	specieNames.add("Anorith");
//    	specieNames.add("Armaldo");
//    	specieNames.add("Feebas");
//    	specieNames.add("Milotic");
//    	specieNames.add("Castform");
//    	specieNames.add("Kecleon");
//    	specieNames.add("Shuppet");
//    	specieNames.add("Banette");
//    	specieNames.add("Duskull");
//    	specieNames.add("Dusclops");
//    	specieNames.add("Tropius");
//    	specieNames.add("Chimecho");
//    	specieNames.add("Absol");
//    	specieNames.add("Wynaut");
//    	specieNames.add("Snorunt");
//    	specieNames.add("Glalie");
//    	specieNames.add("Spheal");
//    	specieNames.add("Sealeo");
//    	specieNames.add("Walrein");
//    	specieNames.add("Clamperl");
//    	specieNames.add("Huntail");
//    	specieNames.add("Gorebyss");
//    	specieNames.add("Relicanth");
//    	specieNames.add("Luvdisc");
//    	specieNames.add("Bagon");
//    	specieNames.add("Shelgon");
//    	specieNames.add("Salamence");
//    	specieNames.add("Beldum");
//    	specieNames.add("Metang");
//    	specieNames.add("Metagross");
    	specieNames.add("Regirock");
    	specieNames.add("Regice");
    	specieNames.add("Registeel");
//    	specieNames.add("Latias");
//    	specieNames.add("Latios");
//    	specieNames.add("Kyogre");
//    	specieNames.add("Groudon");
//    	specieNames.add("Rayquaza");
//    	specieNames.add("Jirachi");
//    	specieNames.add("Deoxys");
//    	specieNames.add("Turtwig");
//    	specieNames.add("Grotle");
//    	specieNames.add("Torterra");
//    	specieNames.add("Chimchar");
//    	specieNames.add("Monferno");
//    	specieNames.add("Infernape");
//    	specieNames.add("Piplup");
//    	specieNames.add("Prinplup");
//    	specieNames.add("Empoleon");
//    	specieNames.add("Starly");
//    	specieNames.add("Staravia");
//    	specieNames.add("Staraptor");
//    	specieNames.add("Bidoof");
//    	specieNames.add("Bibarel");
//    	specieNames.add("Kricketot");
//    	specieNames.add("Kricketune");
    	specieNames.add("Shinx");
    	specieNames.add("Luxio");
    	specieNames.add("Luxray");
//    	specieNames.add("Budew");
//    	specieNames.add("Roserade");
//    	specieNames.add("Cranidos");
//    	specieNames.add("Rampardos");
//    	specieNames.add("Shieldon");
//    	specieNames.add("Bastiodon");
//    	specieNames.add("Burmy");
//    	specieNames.add("Wormadam");
//    	specieNames.add("Mothim");
//    	specieNames.add("Combee");
//    	specieNames.add("Vespiquen");
//    	specieNames.add("Pachirisu");
//    	specieNames.add("Buizel");
//    	specieNames.add("Floatzel");
//    	specieNames.add("Cherubi");
//    	specieNames.add("Cherrim");
//    	specieNames.add("Shellos");
//    	specieNames.add("Gastrodon");
//    	specieNames.add("Ambipom");
//    	specieNames.add("Drifloon");
//    	specieNames.add("Drifblim");
//    	specieNames.add("Buneary");
//    	specieNames.add("Lopunny");
//    	specieNames.add("Mismagius");
//    	specieNames.add("Honchkrow");
//    	specieNames.add("Glameow");
//    	specieNames.add("Purugly");
//    	specieNames.add("Chingling");
//    	specieNames.add("Stunky");
//    	specieNames.add("Skuntank");
//    	specieNames.add("Bronzor");
//    	specieNames.add("Bronzong");
//    	specieNames.add("Bonsly");
//    	specieNames.add("MimeJr.");
//    	specieNames.add("Happiny");
//    	specieNames.add("Chatot");
//    	specieNames.add("Spiritomb");
//    	specieNames.add("Gible");
//    	specieNames.add("Gabite");
//    	specieNames.add("Garchomp");
//    	specieNames.add("Munchlax");
//    	specieNames.add("Riolu");
//    	specieNames.add("Lucario");
//    	specieNames.add("Hippopotas");
//    	specieNames.add("Hippowdon");
//    	specieNames.add("Skorupi");
//    	specieNames.add("Drapion");
//    	specieNames.add("Croagunk");
//    	specieNames.add("Toxicroak");
//    	specieNames.add("Carnivine");
//    	specieNames.add("Finneon");
//    	specieNames.add("Lumineon");
//    	specieNames.add("Mantyke");
    	specieNames.add("Snover");
//    	specieNames.add("Abomasnow");
//    	specieNames.add("Weavile");
//    	specieNames.add("Magnezone");
//    	specieNames.add("Lickilicky");
//    	specieNames.add("Rhyperior");
//    	specieNames.add("Tangrowth");
//    	specieNames.add("Electivire");
//    	specieNames.add("Magmortar");
//    	specieNames.add("Togekiss");
//    	specieNames.add("Yanmega");
//    	specieNames.add("Leafeon");
//    	specieNames.add("Glaceon");
//    	specieNames.add("Gliscor");
//    	specieNames.add("Mamoswine");
//    	specieNames.add("Porygon-Z");
    	specieNames.add("Gallade");
//    	specieNames.add("Probopass");
//    	specieNames.add("Dusknoir");
//    	specieNames.add("Froslass");
//    	specieNames.add("Rotom");
//    	specieNames.add("Uxie");
//    	specieNames.add("Mesprit");
//    	specieNames.add("Azelf");
//    	specieNames.add("Dialga");
//    	specieNames.add("Palkia");
//    	specieNames.add("Heatran");
    	specieNames.add("Regigigas");
//    	specieNames.add("Giratina");
//    	specieNames.add("Cresselia");
//    	specieNames.add("Phione");
//    	specieNames.add("Manaphy");
//    	specieNames.add("Darkrai");
//    	specieNames.add("Shaymin");
//    	specieNames.add("Arceus");
//    	specieNames.add("Victini");
//    	specieNames.add("Snivy");
//    	specieNames.add("Servine");
//    	specieNames.add("Serperior");
//    	specieNames.add("Tepig");
//    	specieNames.add("Pignite");
//    	specieNames.add("Emboar");
//    	specieNames.add("Oshawott");
//    	specieNames.add("Dewott");
//    	specieNames.add("Samurott");
//    	specieNames.add("Patrat");
//    	specieNames.add("Watchog");
//    	specieNames.add("Lillipup");
//    	specieNames.add("Herdier");
//    	specieNames.add("Stoutland");
//    	specieNames.add("Purrloin");
//    	specieNames.add("Liepard");
//    	specieNames.add("Pansage");
//    	specieNames.add("Simisage");
//    	specieNames.add("Pansear");
//    	specieNames.add("Simisear");
//    	specieNames.add("Panpour");
//    	specieNames.add("Simipour");
//    	specieNames.add("Munna");
//    	specieNames.add("Musharna");
//    	specieNames.add("Pidove");
//    	specieNames.add("Tranquill");
//    	specieNames.add("Unfezant");
//    	specieNames.add("Blitzle");
//    	specieNames.add("Zebstrika");
//    	specieNames.add("Roggenrola");
//    	specieNames.add("Boldore");
//    	specieNames.add("Gigalith");
//    	specieNames.add("Woobat");
//    	specieNames.add("Swoobat");
//    	specieNames.add("Drilbur");
//    	specieNames.add("Excadrill");
//    	specieNames.add("Audino");
//    	specieNames.add("Timburr");
//    	specieNames.add("Gurdurr");
//    	specieNames.add("Conkeldurr");
//    	specieNames.add("Tympole");
//    	specieNames.add("Palpitoad");
//    	specieNames.add("Seismitoad");
//    	specieNames.add("Throh");
//    	specieNames.add("Sawk");
//    	specieNames.add("Sewaddle");
//    	specieNames.add("Swadloon");
//    	specieNames.add("Leavanny");
//    	specieNames.add("Venipede");
//    	specieNames.add("Whirlipede");
//    	specieNames.add("Scolipede");
//    	specieNames.add("Cottonee");
//    	specieNames.add("Whimsicott");
//    	specieNames.add("Petilil");
//    	specieNames.add("Lilligant");
//    	specieNames.add("Basculin");
//    	specieNames.add("Sandile");
//    	specieNames.add("Krokorok");
//    	specieNames.add("Krookodile");
//    	specieNames.add("Darumaka");
//    	specieNames.add("Darmanitan");
//    	specieNames.add("Maractus");
    	specieNames.add("Dwebble");
    	specieNames.add("Crustle");
//    	specieNames.add("Scraggy");
//    	specieNames.add("Scrafty");
//    	specieNames.add("Sigilyph");
//    	specieNames.add("Yamask");
//    	specieNames.add("Cofagrigus");
//    	specieNames.add("Tirtouga");
//    	specieNames.add("Carracosta");
//    	specieNames.add("Archen");
//    	specieNames.add("Archeops");
//    	specieNames.add("Trubbish");
//    	specieNames.add("Garbodor");
//    	specieNames.add("Zorua");
//    	specieNames.add("Zoroark");
//    	specieNames.add("Minccino");
//    	specieNames.add("Cinccino");
//    	specieNames.add("Gothita");
//    	specieNames.add("Gothorita");
//    	specieNames.add("Gothitelle");
//    	specieNames.add("Solosis");
//    	specieNames.add("Duosion");
//    	specieNames.add("Reuniclus");
//    	specieNames.add("Ducklett");
//    	specieNames.add("Swanna");
//    	specieNames.add("Vanillite");
//    	specieNames.add("Vanillish");
//    	specieNames.add("Vanilluxe");
//    	specieNames.add("Deerling");
//    	specieNames.add("Sawsbuck");
//    	specieNames.add("Emolga");
//    	specieNames.add("Karrablast");
//    	specieNames.add("Escavalier");
//    	specieNames.add("Foongus");
//    	specieNames.add("Amoonguss");
//    	specieNames.add("Frillish");
//    	specieNames.add("Jellicent");
//    	specieNames.add("Alomomola");
//    	specieNames.add("Joltik");
//    	specieNames.add("Galvantula");
//    	specieNames.add("Ferroseed");
//    	specieNames.add("Ferrothorn");
//    	specieNames.add("Klink");
//    	specieNames.add("Klang");
//    	specieNames.add("Klinklang");
//    	specieNames.add("Tynamo");
//    	specieNames.add("Eelektrik");
//    	specieNames.add("Eelektross");
//    	specieNames.add("Elgyem");
//    	specieNames.add("Beheeyem");
    	specieNames.add("Litwick");
    	specieNames.add("Lampent");
    	specieNames.add("Chandelure");
//    	specieNames.add("Axew");
//    	specieNames.add("Fraxure");
//    	specieNames.add("Haxorus");
//    	specieNames.add("Cubchoo");
//    	specieNames.add("Beartic");
//    	specieNames.add("Cryogonal");
//    	specieNames.add("Shelmet");
//    	specieNames.add("Accelgor");
//    	specieNames.add("Stunfisk");
//    	specieNames.add("Mienfoo");
//    	specieNames.add("Mienshao");
//    	specieNames.add("Druddigon");
//    	specieNames.add("Golett");
//    	specieNames.add("Golurk");
//    	specieNames.add("Pawniard");
//    	specieNames.add("Bisharp");
//    	specieNames.add("Bouffalant");
//    	specieNames.add("Rufflet");
//    	specieNames.add("Braviary");
//    	specieNames.add("Vullaby");
//    	specieNames.add("Mandibuzz");
//    	specieNames.add("Heatmor");
//    	specieNames.add("Durant");
//    	specieNames.add("Deino");
//    	specieNames.add("Zweilous");
//    	specieNames.add("Hydreigon");
//    	specieNames.add("Larvesta");
//    	specieNames.add("Volcarona");
//    	specieNames.add("Cobalion");
//    	specieNames.add("Terrakion");
//    	specieNames.add("Virizion");
//    	specieNames.add("Tornadus");
//    	specieNames.add("Thundurus");
//    	specieNames.add("Reshiram");
//    	specieNames.add("Zekrom");
//    	specieNames.add("Landorus");
//    	specieNames.add("Kyurem");
//    	specieNames.add("Keldeo");
//    	specieNames.add("Meloetta");
//    	specieNames.add("Genesect");
//    	specieNames.add("Chespin");
//    	specieNames.add("Quilladin");
//    	specieNames.add("Chesnaught");
//    	specieNames.add("Fennekin");
//    	specieNames.add("Braixen");
//    	specieNames.add("Delphox");
//    	specieNames.add("Froakie");
//    	specieNames.add("Frogadier");
//    	specieNames.add("Greninja");
//    	specieNames.add("Bunnelby");
//    	specieNames.add("Diggersby");
//    	specieNames.add("Fletchling");
//    	specieNames.add("Fletchinder");
//    	specieNames.add("Talonflame");
//    	specieNames.add("Scatterbug");
//    	specieNames.add("Spewpa");
//    	specieNames.add("Vivillon");
//    	specieNames.add("Litleo");
//    	specieNames.add("Pyroar");
//    	specieNames.add("Flabébé");
//    	specieNames.add("Floette");
//    	specieNames.add("Florges");
//    	specieNames.add("Skiddo");
//    	specieNames.add("Gogoat");
//    	specieNames.add("Pancham");
//    	specieNames.add("Pangoro");
//    	specieNames.add("Furfrou");
//    	specieNames.add("Espurr");
//    	specieNames.add("Meowstic");
//    	specieNames.add("Honedge");
//    	specieNames.add("Doublade");
//    	specieNames.add("Aegislash");
//    	specieNames.add("Spritzee");
//    	specieNames.add("Aromatisse");
//    	specieNames.add("Swirlix");
//    	specieNames.add("Slurpuff");
//    	specieNames.add("Inkay");
//    	specieNames.add("Malamar");
//    	specieNames.add("Binacle");
//    	specieNames.add("Barbaracle");
//    	specieNames.add("Skrelp");
//    	specieNames.add("Dragalge");
//    	specieNames.add("Clauncher");
//    	specieNames.add("Clawitzer");
//    	specieNames.add("Helioptile");
//    	specieNames.add("Heliolisk");
//    	specieNames.add("Tyrunt");
//    	specieNames.add("Tyrantrum");
//    	specieNames.add("Amaura");
//    	specieNames.add("Aurorus");
//    	specieNames.add("Sylveon");
//    	specieNames.add("Hawlucha");
//    	specieNames.add("Dedenne");
//    	specieNames.add("Carbink");
//    	specieNames.add("Goomy");
//    	specieNames.add("Sliggoo");
//    	specieNames.add("Goodra");
//    	specieNames.add("Klefki");
//    	specieNames.add("Phantump");
//    	specieNames.add("Trevenant");
//    	specieNames.add("Pumpkaboo");
//    	specieNames.add("Gourgeist");
//    	specieNames.add("Bergmite");
//    	specieNames.add("Avalugg");
//    	specieNames.add("Noibat");
//    	specieNames.add("Noivern");
//    	specieNames.add("Xerneas");
//    	specieNames.add("Yveltal");
//    	specieNames.add("Zygarde");

    	specieNames.add("Mimikyu");
    	specieNames.add("Scorbunny");
    	specieNames.add("Raboot");
    	specieNames.add("Regieleki");
    	specieNames.add("Regidrago");
    	
    	for(String name : specieNames) {
    		species.put(name.toLowerCase(),new Specie(name.toLowerCase()));
    	}
    	
    }
	
	

    
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
        
        
        //Ghost intro animation
        // Load animation(s) from file
        introAnimGhost = new ArrayList<SpriteProxy>();
     
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
//                        pokemonText = new Texture(Gdx.files.internal("crystal_pokemon/pokemon/" + name + "/front.png"));
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
    	//Defaults to generation RED 	
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

        else if (name.equalsIgnoreCase("Zubat")) { // gen I properties
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
        else if (name.equalsIgnoreCase("Rattata")) { // gen I properties
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

        else if (name.equalsIgnoreCase("Steelix")) { // gen II properties
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
                    genderRatio = line.split("db ")[1].split(" ;")[0];
                    
                // Egg cycles to hatch
                } else if (lineNum == 11) {
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
                if (name.equals("poochyena")) {
                    i = 312;
                    found = true;
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
                    else if (numericRatio == 155) {
                        this.genderRatio = "GENDER_F75";
                    }
                    else if (numericRatio == 255) {
                        this.genderRatio = "GENDER_F100";
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
}
