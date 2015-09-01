package com.pkmngen.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Pokemon {

	String name;
	int level;

	Map<String, Integer> baseStats = new HashMap<String, Integer>();
	Map<String, Integer> currentStats = new HashMap<String, Integer>();
	Map<String, Integer> maxStats = new HashMap<String, Integer>(); //needed for various calculations

	Map<String, Integer> IVs = new HashMap<String, Integer>();
	
	//note - this doesn't go in 'maxStats' map
	//int catchRate; //may put into some other map later
	
	Sprite sprite;
	
	ArrayList<String> types;
	
	int angry, eating; //nonzero if angry or eating. safari zone mechanic
	
	//Music cry; //unused atm, using PlaySound
	
	//this reference is used when needing to stop drawing pokemon in battle screen
	 //could also just be oppPokemonDrawAction in battle, I think
	//Action drawAction; //doesn't work. also, using sprite alpha for now
	
	public Pokemon (String name, int level) {
		
		this.name = name;
		this.level = level;
		
		this.types = new ArrayList<String>();
		
		//init vars
		this.angry = 0;
		this.eating = 0;
		

		if (name == "Zubat") { //gen I properties
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
}
