package com.pkmngen.game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.pkmngen.game.DrawPlayerMenu.Intro;


class Attack {
    
    String name;
    int power;
    String type;
    int accuracy;
    int pp;
    int effect_chance; // chance to paralyze, lower speed, poison, etc.
    
    public Attack(String name, int power, String type, int accuracy, int pp, int effect_chance) {
        this.name = name;
        this.power = power;
        this.type = type;
        this.accuracy = accuracy;
        this.pp = pp;
        this.effect_chance = effect_chance;
    }
}

public class Battle {

    //opposing pokemon
    Pokemon oppPokemon;
    
    //your pokemon //probly use this
    //Pokemon yourPokemon;
    
    //action that is drawing the battle
     //this reference is used to stop drawing battle once it's complete
    DrawBattle drawAction;
    
    Music music;
    Music victoryFanfare;
    
    // TODO: remove
//    public enum Effectiveness {
//        Super,
//        Neutral,
//        Not_Very,
//        No_Effect;
//    }
    
    // HashMap<Generation, ... ?
    HashMap<String, HashMap<String, Float>> gen2TypeEffectiveness;
    HashMap<String, Attack> attacks = new HashMap<String, Attack>();

    public Battle() {

        this.music = Gdx.audio.newMusic(Gdx.files.internal("battle/battle-vs-wild-pokemon.ogg"));
        this.music.setLooping(true);
        this.music.setVolume(.3f);
        
        this.victoryFanfare = Gdx.audio.newMusic(Gdx.files.internal("victory_fanfare2.ogg"));
        this.victoryFanfare.setLooping(true);
        this.victoryFanfare.setVolume(.3f);
        
        this.gen2TypeEffectiveness = new HashMap<String, HashMap<String, Float>>();
        this.gen2TypeEffectiveness.put("normal", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("normal").put("normal", 1f);
        this.gen2TypeEffectiveness.get("normal").put("fire", 1f);
        this.gen2TypeEffectiveness.get("normal").put("water", 1f);
        this.gen2TypeEffectiveness.get("normal").put("electric", 1f);
        this.gen2TypeEffectiveness.get("normal").put("grass", 1f);
        this.gen2TypeEffectiveness.get("normal").put("ice", 1f);
        this.gen2TypeEffectiveness.get("normal").put("fighting", 1f);
        this.gen2TypeEffectiveness.get("normal").put("poison", 1f);
        this.gen2TypeEffectiveness.get("normal").put("ground", 1f);
        this.gen2TypeEffectiveness.get("normal").put("flying", 1f);
        this.gen2TypeEffectiveness.get("normal").put("psychic", 1f);
        this.gen2TypeEffectiveness.get("normal").put("bug", 1f);
        this.gen2TypeEffectiveness.get("normal").put("rock", .5f);
        this.gen2TypeEffectiveness.get("normal").put("ghost", 0f);
        this.gen2TypeEffectiveness.get("normal").put("dragon", 1f);
        this.gen2TypeEffectiveness.get("normal").put("dark", 1f);
        this.gen2TypeEffectiveness.get("normal").put("steel", .5f);
        this.gen2TypeEffectiveness.get("normal").put("fairy", 1f);
        this.gen2TypeEffectiveness.put("fire", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("fire").put("normal", 1f);
        this.gen2TypeEffectiveness.get("fire").put("fire", .5f);
        this.gen2TypeEffectiveness.get("fire").put("water", .5f);
        this.gen2TypeEffectiveness.get("fire").put("electric", 1f);
        this.gen2TypeEffectiveness.get("fire").put("grass", 2f);
        this.gen2TypeEffectiveness.get("fire").put("ice", 2f);
        this.gen2TypeEffectiveness.get("fire").put("fighting", 1f);
        this.gen2TypeEffectiveness.get("fire").put("poison", 1f);
        this.gen2TypeEffectiveness.get("fire").put("ground", 1f);
        this.gen2TypeEffectiveness.get("fire").put("flying", 1f);
        this.gen2TypeEffectiveness.get("fire").put("psychic", 1f);
        this.gen2TypeEffectiveness.get("fire").put("bug", 2f);
        this.gen2TypeEffectiveness.get("fire").put("rock", .5f);
        this.gen2TypeEffectiveness.get("fire").put("ghost", 1f);
        this.gen2TypeEffectiveness.get("fire").put("dragon", .5f);
        this.gen2TypeEffectiveness.get("fire").put("dark", 1f);
        this.gen2TypeEffectiveness.get("fire").put("steel", .5f);
        this.gen2TypeEffectiveness.get("fire").put("fairy", 1f);
        this.gen2TypeEffectiveness.put("water", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("water").put("normal", 1f);
        this.gen2TypeEffectiveness.get("water").put("fire", 2f);
        this.gen2TypeEffectiveness.get("water").put("water", .5f);
        this.gen2TypeEffectiveness.get("water").put("electric", 1f);
        this.gen2TypeEffectiveness.get("water").put("grass", .5f);
        this.gen2TypeEffectiveness.get("water").put("ice", 1f);
        this.gen2TypeEffectiveness.get("water").put("fighting", 1f);
        this.gen2TypeEffectiveness.get("water").put("poison", 1f);
        this.gen2TypeEffectiveness.get("water").put("ground", 2f);
        this.gen2TypeEffectiveness.get("water").put("flying", 1f);
        this.gen2TypeEffectiveness.get("water").put("psychic", 1f);
        this.gen2TypeEffectiveness.get("water").put("bug", 1f);
        this.gen2TypeEffectiveness.get("water").put("rock", 2f);
        this.gen2TypeEffectiveness.get("water").put("ghost", 1f);
        this.gen2TypeEffectiveness.get("water").put("dragon", .5f);
        this.gen2TypeEffectiveness.get("water").put("dark", 1f);
        this.gen2TypeEffectiveness.get("water").put("steel", 1f);
        this.gen2TypeEffectiveness.get("water").put("fairy", 1f);
        this.gen2TypeEffectiveness.put("electric", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("electric").put("normal", 1f);
        this.gen2TypeEffectiveness.get("electric").put("fire", 1f);
        this.gen2TypeEffectiveness.get("electric").put("water", 2f);
        this.gen2TypeEffectiveness.get("electric").put("electric", .5f);
        this.gen2TypeEffectiveness.get("electric").put("grass", .5f);
        this.gen2TypeEffectiveness.get("electric").put("ice", 1f);
        this.gen2TypeEffectiveness.get("electric").put("fighting", 1f);
        this.gen2TypeEffectiveness.get("electric").put("poison", 1f);
        this.gen2TypeEffectiveness.get("electric").put("ground", 0f);
        this.gen2TypeEffectiveness.get("electric").put("flying", 2f);
        this.gen2TypeEffectiveness.get("electric").put("psychic", 1f);
        this.gen2TypeEffectiveness.get("electric").put("bug", 1f);
        this.gen2TypeEffectiveness.get("electric").put("rock", 1f);
        this.gen2TypeEffectiveness.get("electric").put("ghost", 1f);
        this.gen2TypeEffectiveness.get("electric").put("dragon", .5f);
        this.gen2TypeEffectiveness.get("electric").put("dark", 1f);
        this.gen2TypeEffectiveness.get("electric").put("steel", 1f);
        this.gen2TypeEffectiveness.get("electric").put("fairy", 1f);
        this.gen2TypeEffectiveness.put("grass", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("grass").put("normal", 1f);
        this.gen2TypeEffectiveness.get("grass").put("fire", .5f);
        this.gen2TypeEffectiveness.get("grass").put("water", 2f);
        this.gen2TypeEffectiveness.get("grass").put("electric", 1f);
        this.gen2TypeEffectiveness.get("grass").put("grass", .5f);
        this.gen2TypeEffectiveness.get("grass").put("ice", 1f);
        this.gen2TypeEffectiveness.get("grass").put("fighting", 1f);
        this.gen2TypeEffectiveness.get("grass").put("poison", .5f);
        this.gen2TypeEffectiveness.get("grass").put("ground", 2f);
        this.gen2TypeEffectiveness.get("grass").put("flying", .5f);
        this.gen2TypeEffectiveness.get("grass").put("psychic", 1f);
        this.gen2TypeEffectiveness.get("grass").put("bug", .5f);
        this.gen2TypeEffectiveness.get("grass").put("rock", 2f);
        this.gen2TypeEffectiveness.get("grass").put("ghost", 1f);
        this.gen2TypeEffectiveness.get("grass").put("dragon", .5f);
        this.gen2TypeEffectiveness.get("grass").put("dark", 1f);
        this.gen2TypeEffectiveness.get("grass").put("steel", .5f);
        this.gen2TypeEffectiveness.get("grass").put("fairy", 1f);
        this.gen2TypeEffectiveness.put("ice", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("ice").put("normal", 1f);
        this.gen2TypeEffectiveness.get("ice").put("fire", .5f);
        this.gen2TypeEffectiveness.get("ice").put("water", .5f);
        this.gen2TypeEffectiveness.get("ice").put("electric", 1f);
        this.gen2TypeEffectiveness.get("ice").put("grass", 2f);
        this.gen2TypeEffectiveness.get("ice").put("ice", .5f);
        this.gen2TypeEffectiveness.get("ice").put("fighting", 1f);
        this.gen2TypeEffectiveness.get("ice").put("poison", 1f);
        this.gen2TypeEffectiveness.get("ice").put("ground", 2f);
        this.gen2TypeEffectiveness.get("ice").put("flying", 2f);
        this.gen2TypeEffectiveness.get("ice").put("psychic", 1f);
        this.gen2TypeEffectiveness.get("ice").put("bug", 1f);
        this.gen2TypeEffectiveness.get("ice").put("rock", 1f);
        this.gen2TypeEffectiveness.get("ice").put("ghost", 1f);
        this.gen2TypeEffectiveness.get("ice").put("dragon", 2f);
        this.gen2TypeEffectiveness.get("ice").put("dark", 1f);
        this.gen2TypeEffectiveness.get("ice").put("steel", .5f);
        this.gen2TypeEffectiveness.get("ice").put("fairy", 1f);
        this.gen2TypeEffectiveness.put("fighting", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("fighting").put("normal", 2f);
        this.gen2TypeEffectiveness.get("fighting").put("fire", 1f);
        this.gen2TypeEffectiveness.get("fighting").put("water", 1f);
        this.gen2TypeEffectiveness.get("fighting").put("electric", 1f);
        this.gen2TypeEffectiveness.get("fighting").put("grass", 1f);
        this.gen2TypeEffectiveness.get("fighting").put("ice", 2f);
        this.gen2TypeEffectiveness.get("fighting").put("fighting", 1f);
        this.gen2TypeEffectiveness.get("fighting").put("poison", .5f);
        this.gen2TypeEffectiveness.get("fighting").put("ground", 1f);
        this.gen2TypeEffectiveness.get("fighting").put("flying", .5f);
        this.gen2TypeEffectiveness.get("fighting").put("psychic", .5f);
        this.gen2TypeEffectiveness.get("fighting").put("bug", .5f);
        this.gen2TypeEffectiveness.get("fighting").put("rock", 2f);
        this.gen2TypeEffectiveness.get("fighting").put("ghost", 0f);
        this.gen2TypeEffectiveness.get("fighting").put("dragon", 1f);
        this.gen2TypeEffectiveness.get("fighting").put("dark", 2f);
        this.gen2TypeEffectiveness.get("fighting").put("steel", 2f);
        this.gen2TypeEffectiveness.get("fighting").put("fairy", .5f);
        this.gen2TypeEffectiveness.put("poison", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("poison").put("normal", 1f);
        this.gen2TypeEffectiveness.get("poison").put("fire", 1f);
        this.gen2TypeEffectiveness.get("poison").put("water", 1f);
        this.gen2TypeEffectiveness.get("poison").put("electric", 1f);
        this.gen2TypeEffectiveness.get("poison").put("grass", 2f);
        this.gen2TypeEffectiveness.get("poison").put("ice", 1f);
        this.gen2TypeEffectiveness.get("poison").put("fighting", 1f);
        this.gen2TypeEffectiveness.get("poison").put("poison", .5f);
        this.gen2TypeEffectiveness.get("poison").put("ground", .5f);
        this.gen2TypeEffectiveness.get("poison").put("flying", 1f);
        this.gen2TypeEffectiveness.get("poison").put("psychic", 1f);
        this.gen2TypeEffectiveness.get("poison").put("bug", 1f);
        this.gen2TypeEffectiveness.get("poison").put("rock", .5f);
        this.gen2TypeEffectiveness.get("poison").put("ghost", .5f);
        this.gen2TypeEffectiveness.get("poison").put("dragon", 1f);
        this.gen2TypeEffectiveness.get("poison").put("dark", 1f);
        this.gen2TypeEffectiveness.get("poison").put("steel", 0f);
        this.gen2TypeEffectiveness.get("poison").put("fairy", 2f);
        this.gen2TypeEffectiveness.put("ground", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("ground").put("normal", 1f);
        this.gen2TypeEffectiveness.get("ground").put("fire", 2f);
        this.gen2TypeEffectiveness.get("ground").put("water", 1f);
        this.gen2TypeEffectiveness.get("ground").put("electric", 2f);
        this.gen2TypeEffectiveness.get("ground").put("grass", .5f);
        this.gen2TypeEffectiveness.get("ground").put("ice", 1f);
        this.gen2TypeEffectiveness.get("ground").put("fighting", 1f);
        this.gen2TypeEffectiveness.get("ground").put("poison", 2f);
        this.gen2TypeEffectiveness.get("ground").put("ground", 1f);
        this.gen2TypeEffectiveness.get("ground").put("flying", 0f);
        this.gen2TypeEffectiveness.get("ground").put("psychic", 1f);
        this.gen2TypeEffectiveness.get("ground").put("bug", .5f);
        this.gen2TypeEffectiveness.get("ground").put("rock", 2f);
        this.gen2TypeEffectiveness.get("ground").put("ghost", 1f);
        this.gen2TypeEffectiveness.get("ground").put("dragon", 1f);
        this.gen2TypeEffectiveness.get("ground").put("dark", 1f);
        this.gen2TypeEffectiveness.get("ground").put("steel", 2f);
        this.gen2TypeEffectiveness.get("ground").put("fairy", 1f);
        this.gen2TypeEffectiveness.put("flying", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("flying").put("normal", 1f);
        this.gen2TypeEffectiveness.get("flying").put("fire", 1f);
        this.gen2TypeEffectiveness.get("flying").put("water", 1f);
        this.gen2TypeEffectiveness.get("flying").put("electric", .5f);
        this.gen2TypeEffectiveness.get("flying").put("grass", 2f);
        this.gen2TypeEffectiveness.get("flying").put("ice", 1f);
        this.gen2TypeEffectiveness.get("flying").put("fighting", 2f);
        this.gen2TypeEffectiveness.get("flying").put("poison", 1f);
        this.gen2TypeEffectiveness.get("flying").put("ground", 1f);
        this.gen2TypeEffectiveness.get("flying").put("flying", 1f);
        this.gen2TypeEffectiveness.get("flying").put("psychic", 1f);
        this.gen2TypeEffectiveness.get("flying").put("bug", 2f);
        this.gen2TypeEffectiveness.get("flying").put("rock", .5f);
        this.gen2TypeEffectiveness.get("flying").put("ghost", 1f);
        this.gen2TypeEffectiveness.get("flying").put("dragon", 1f);
        this.gen2TypeEffectiveness.get("flying").put("dark", 1f);
        this.gen2TypeEffectiveness.get("flying").put("steel", .5f);
        this.gen2TypeEffectiveness.get("flying").put("fairy", 1f);
        this.gen2TypeEffectiveness.put("psychic", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("psychic").put("normal", 1f);
        this.gen2TypeEffectiveness.get("psychic").put("fire", 1f);
        this.gen2TypeEffectiveness.get("psychic").put("water", 1f);
        this.gen2TypeEffectiveness.get("psychic").put("electric", 1f);
        this.gen2TypeEffectiveness.get("psychic").put("grass", 1f);
        this.gen2TypeEffectiveness.get("psychic").put("ice", 1f);
        this.gen2TypeEffectiveness.get("psychic").put("fighting", 2f);
        this.gen2TypeEffectiveness.get("psychic").put("poison", 2f);
        this.gen2TypeEffectiveness.get("psychic").put("ground", 1f);
        this.gen2TypeEffectiveness.get("psychic").put("flying", 1f);
        this.gen2TypeEffectiveness.get("psychic").put("psychic", .5f);
        this.gen2TypeEffectiveness.get("psychic").put("bug", 1f);
        this.gen2TypeEffectiveness.get("psychic").put("rock", 1f);
        this.gen2TypeEffectiveness.get("psychic").put("ghost", 1f);
        this.gen2TypeEffectiveness.get("psychic").put("dragon", 1f);
        this.gen2TypeEffectiveness.get("psychic").put("dark", 0f);
        this.gen2TypeEffectiveness.get("psychic").put("steel", .5f);
        this.gen2TypeEffectiveness.get("psychic").put("fairy", 1f);
        this.gen2TypeEffectiveness.put("bug", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("bug").put("normal", 1f);
        this.gen2TypeEffectiveness.get("bug").put("fire", .5f);
        this.gen2TypeEffectiveness.get("bug").put("water", 1f);
        this.gen2TypeEffectiveness.get("bug").put("electric", 1f);
        this.gen2TypeEffectiveness.get("bug").put("grass", 2f);
        this.gen2TypeEffectiveness.get("bug").put("ice", 1f);
        this.gen2TypeEffectiveness.get("bug").put("fighting", .5f);
        this.gen2TypeEffectiveness.get("bug").put("poison", .5f);
        this.gen2TypeEffectiveness.get("bug").put("ground", 1f);
        this.gen2TypeEffectiveness.get("bug").put("flying", .5f);
        this.gen2TypeEffectiveness.get("bug").put("psychic", 2f);
        this.gen2TypeEffectiveness.get("bug").put("bug", 1f);
        this.gen2TypeEffectiveness.get("bug").put("rock", 1f);
        this.gen2TypeEffectiveness.get("bug").put("ghost", .5f);
        this.gen2TypeEffectiveness.get("bug").put("dragon", 1f);
        this.gen2TypeEffectiveness.get("bug").put("dark", 2f);
        this.gen2TypeEffectiveness.get("bug").put("steel", .5f);
        this.gen2TypeEffectiveness.get("bug").put("fairy", .5f);
        this.gen2TypeEffectiveness.put("rock", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("rock").put("normal", 1f);
        this.gen2TypeEffectiveness.get("rock").put("fire", 2f);
        this.gen2TypeEffectiveness.get("rock").put("water", 1f);
        this.gen2TypeEffectiveness.get("rock").put("electric", 1f);
        this.gen2TypeEffectiveness.get("rock").put("grass", 1f);
        this.gen2TypeEffectiveness.get("rock").put("ice", 2f);
        this.gen2TypeEffectiveness.get("rock").put("fighting", .5f);
        this.gen2TypeEffectiveness.get("rock").put("poison", 1f);
        this.gen2TypeEffectiveness.get("rock").put("ground", .5f);
        this.gen2TypeEffectiveness.get("rock").put("flying", 2f);
        this.gen2TypeEffectiveness.get("rock").put("psychic", 1f);
        this.gen2TypeEffectiveness.get("rock").put("bug", 2f);
        this.gen2TypeEffectiveness.get("rock").put("rock", 1f);
        this.gen2TypeEffectiveness.get("rock").put("ghost", 1f);
        this.gen2TypeEffectiveness.get("rock").put("dragon", 1f);
        this.gen2TypeEffectiveness.get("rock").put("dark", 1f);
        this.gen2TypeEffectiveness.get("rock").put("steel", .5f);
        this.gen2TypeEffectiveness.get("rock").put("fairy", 1f);
        this.gen2TypeEffectiveness.put("ghost", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("ghost").put("normal", 0f);
        this.gen2TypeEffectiveness.get("ghost").put("fire", 1f);
        this.gen2TypeEffectiveness.get("ghost").put("water", 1f);
        this.gen2TypeEffectiveness.get("ghost").put("electric", 1f);
        this.gen2TypeEffectiveness.get("ghost").put("grass", 1f);
        this.gen2TypeEffectiveness.get("ghost").put("ice", 1f);
        this.gen2TypeEffectiveness.get("ghost").put("fighting", 1f);
        this.gen2TypeEffectiveness.get("ghost").put("poison", 1f);
        this.gen2TypeEffectiveness.get("ghost").put("ground", 1f);
        this.gen2TypeEffectiveness.get("ghost").put("flying", 1f);
        this.gen2TypeEffectiveness.get("ghost").put("psychic", 2f);
        this.gen2TypeEffectiveness.get("ghost").put("bug", 1f);
        this.gen2TypeEffectiveness.get("ghost").put("rock", 1f);
        this.gen2TypeEffectiveness.get("ghost").put("ghost", 2f);
        this.gen2TypeEffectiveness.get("ghost").put("dragon", 1f);
        this.gen2TypeEffectiveness.get("ghost").put("dark", .5f);
        this.gen2TypeEffectiveness.get("ghost").put("steel", 1f);
        this.gen2TypeEffectiveness.get("ghost").put("fairy", 1f);
        this.gen2TypeEffectiveness.put("dragon", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("dragon").put("normal", 1f);
        this.gen2TypeEffectiveness.get("dragon").put("fire", 1f);
        this.gen2TypeEffectiveness.get("dragon").put("water", 1f);
        this.gen2TypeEffectiveness.get("dragon").put("electric", 1f);
        this.gen2TypeEffectiveness.get("dragon").put("grass", 1f);
        this.gen2TypeEffectiveness.get("dragon").put("ice", 1f);
        this.gen2TypeEffectiveness.get("dragon").put("fighting", 1f);
        this.gen2TypeEffectiveness.get("dragon").put("poison", 1f);
        this.gen2TypeEffectiveness.get("dragon").put("ground", 1f);
        this.gen2TypeEffectiveness.get("dragon").put("flying", 1f);
        this.gen2TypeEffectiveness.get("dragon").put("psychic", 1f);
        this.gen2TypeEffectiveness.get("dragon").put("bug", 1f);
        this.gen2TypeEffectiveness.get("dragon").put("rock", 1f);
        this.gen2TypeEffectiveness.get("dragon").put("ghost", 1f);
        this.gen2TypeEffectiveness.get("dragon").put("dragon", 2f);
        this.gen2TypeEffectiveness.get("dragon").put("dark", 1f);
        this.gen2TypeEffectiveness.get("dragon").put("steel", .5f);
        this.gen2TypeEffectiveness.get("dragon").put("fairy", 0f);
        this.gen2TypeEffectiveness.put("dark", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("dark").put("normal", 1f);
        this.gen2TypeEffectiveness.get("dark").put("fire", 1f);
        this.gen2TypeEffectiveness.get("dark").put("water", 1f);
        this.gen2TypeEffectiveness.get("dark").put("electric", 1f);
        this.gen2TypeEffectiveness.get("dark").put("grass", 1f);
        this.gen2TypeEffectiveness.get("dark").put("ice", 1f);
        this.gen2TypeEffectiveness.get("dark").put("fighting", .5f);
        this.gen2TypeEffectiveness.get("dark").put("poison", 1f);
        this.gen2TypeEffectiveness.get("dark").put("ground", 1f);
        this.gen2TypeEffectiveness.get("dark").put("flying", 1f);
        this.gen2TypeEffectiveness.get("dark").put("psychic", 2f);
        this.gen2TypeEffectiveness.get("dark").put("bug", 1f);
        this.gen2TypeEffectiveness.get("dark").put("rock", 1f);
        this.gen2TypeEffectiveness.get("dark").put("ghost", 2f);
        this.gen2TypeEffectiveness.get("dark").put("dragon", 1f);
        this.gen2TypeEffectiveness.get("dark").put("dark", .5f);
        this.gen2TypeEffectiveness.get("dark").put("steel", 1f);
        this.gen2TypeEffectiveness.get("dark").put("fairy", .5f);
        this.gen2TypeEffectiveness.put("steel", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("steel").put("normal", 1f);
        this.gen2TypeEffectiveness.get("steel").put("fire", .5f);
        this.gen2TypeEffectiveness.get("steel").put("water", .5f);
        this.gen2TypeEffectiveness.get("steel").put("electric", .5f);
        this.gen2TypeEffectiveness.get("steel").put("grass", 1f);
        this.gen2TypeEffectiveness.get("steel").put("ice", 2f);
        this.gen2TypeEffectiveness.get("steel").put("fighting", 1f);
        this.gen2TypeEffectiveness.get("steel").put("poison", 1f);
        this.gen2TypeEffectiveness.get("steel").put("ground", 1f);
        this.gen2TypeEffectiveness.get("steel").put("flying", 1f);
        this.gen2TypeEffectiveness.get("steel").put("psychic", 1f);
        this.gen2TypeEffectiveness.get("steel").put("bug", 1f);
        this.gen2TypeEffectiveness.get("steel").put("rock", 2f);
        this.gen2TypeEffectiveness.get("steel").put("ghost", 1f);
        this.gen2TypeEffectiveness.get("steel").put("dragon", 1f);
        this.gen2TypeEffectiveness.get("steel").put("dark", 1f);
        this.gen2TypeEffectiveness.get("steel").put("steel", .5f);
        this.gen2TypeEffectiveness.get("steel").put("fairy", 2f);
        this.gen2TypeEffectiveness.put("fairy", new HashMap<String, Float>());
        this.gen2TypeEffectiveness.get("fairy").put("normal", 1f);
        this.gen2TypeEffectiveness.get("fairy").put("fire", .5f);
        this.gen2TypeEffectiveness.get("fairy").put("water", 1f);
        this.gen2TypeEffectiveness.get("fairy").put("electric", 1f);
        this.gen2TypeEffectiveness.get("fairy").put("grass", 1f);
        this.gen2TypeEffectiveness.get("fairy").put("ice", 1f);
        this.gen2TypeEffectiveness.get("fairy").put("fighting", 2f);
        this.gen2TypeEffectiveness.get("fairy").put("poison", .5f);
        this.gen2TypeEffectiveness.get("fairy").put("ground", 1f);
        this.gen2TypeEffectiveness.get("fairy").put("flying", 1f);
        this.gen2TypeEffectiveness.get("fairy").put("psychic", 1f);
        this.gen2TypeEffectiveness.get("fairy").put("bug", 1f);
        this.gen2TypeEffectiveness.get("fairy").put("rock", 1f);
        this.gen2TypeEffectiveness.get("fairy").put("ghost", 1f);
        this.gen2TypeEffectiveness.get("fairy").put("dragon", 2f);
        this.gen2TypeEffectiveness.get("fairy").put("dark", 2f);
        this.gen2TypeEffectiveness.get("fairy").put("steel", .5f);
        this.gen2TypeEffectiveness.get("fairy").put("fairy", 1f);
        
        // load all attacks and attributes
        
        try {
            FileHandle file = Gdx.files.internal("crystal_pokemon/moves.asm");
            Reader reader = file.reader();
            BufferedReader br = new BufferedReader(reader);
            String line;
            int lineNum = 1;
            while ((line = br.readLine()) != null)   {
                // TODO: using table to look up number now
//                if (lineNum == 0) {
//                    this.dexNumber = line.split(" ; ")[1];
//                } else 
                if (lineNum > 14 && lineNum < 266) {
                    String attrs[] = line.split("\tmove ")[1].split(",\\s+");
                    Attack attack = new Attack(attrs[0].toLowerCase().replace('_', ' '), Integer.valueOf(attrs[2]), 
                                               attrs[3].toLowerCase(), Integer.valueOf(attrs[4]),
                                               Integer.valueOf(attrs[5]), Integer.valueOf(attrs[6]));
                    this.attacks.put(attack.name, attack);
//                    System.out.println(attack.name + " " + attack.type);
                } 
                lineNum++;
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    class CheckTrapped extends Action {
        public int layer = 500;
        public int getLayer(){return this.layer;}
        
        @Override
        public void step(Game game) {
            this.step();
        }
        
        public void step() {
            // TODO: trying out method where can only reference parent battle object, using Battle.this
            // probably revert at some point
            // TODO: if keeping, refactor to remove references to Game.staticGame
            //  likely need global actionStack or something
            
            // always goes you, then opponent
            Game.staticGame.actionStack.remove(this);
            if (Battle.this.oppPokemon.trappedBy != null) {
                this.nextAction = new Battle_Actions.LoadAndPlayAttackAnimation(Game.staticGame, Battle.this.oppPokemon.trappedBy, Battle.this.oppPokemon,
                                  new DisplayText.Clear(Game.staticGame,
                                  new WaitFrames(Game.staticGame, 3,
                                  new DisplayText(Game.staticGame, 
                                                  Battle.this.oppPokemon.name.toUpperCase()+"' hurt by "+Battle.this.oppPokemon.trappedBy.toUpperCase()+"!",
                                                  null,
                                                  true,
                                  new DepleteEnemyHealth(Game.staticGame,
                                  new WaitFrames(Game.staticGame, 13,
                                  this.nextAction))))));
                Battle.this.oppPokemon.trapCounter -= 1;
                if (Battle.this.oppPokemon.trapCounter <= 0) {
                    Battle.this.oppPokemon.trappedBy = null;
                }
            }
            if (Game.staticGame.player.currPokemon.trappedBy != null) {
                this.nextAction = new Battle_Actions.LoadAndPlayAttackAnimation(Game.staticGame, Game.staticGame.player.currPokemon.trappedBy, Game.staticGame.player.currPokemon,
                                  new DisplayText.Clear(Game.staticGame,
                                  new WaitFrames(Game.staticGame, 3,
                                  new DisplayText(Game.staticGame, 
                                                  Game.staticGame.player.currPokemon.name.toUpperCase()+"' hurt by "+Game.staticGame.player.currPokemon.trappedBy.toUpperCase()+"!",
                                                  null, 
                                                  true,
                                  new DepleteFriendlyHealth(Game.staticGame.player.currPokemon,
                                  new WaitFrames(Game.staticGame, 13,
                                  this.nextAction))))));
                Game.staticGame.player.currPokemon.trapCounter -= 1;
                if (Battle.this.oppPokemon.trapCounter <= 0) {
                    Battle.this.oppPokemon.trappedBy = null;
                }
            }
            PublicFunctions.insertToAS(Game.staticGame, this.nextAction);
        }

        public CheckTrapped(Game game, Action nextAction) {
            this.nextAction = nextAction;
        }
    }
}


//TODO - remove all 'post scaling change' commented lines.

//TODO - bug where a caught pokemon will still be in the wild

class BattleIntro extends Action {
    
    ArrayList<Sprite> frames;
    Sprite frame;
    
    

    public int layer = 139;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};
    
    @Override
    public void step(Game game) {

        //get next frame
        this.frame = frames.get(0);

        if (this.frame != null) {
            this.frame.setScale(3); //scale doesn't work in batch.draw
            this.frame.setPosition(0,0);
            this.frame.draw(game.floatingBatch);
            //game.batch.draw(this.frame, 0, -20);
        }

        frames.remove(0);

        //if done with anim, do nextAction
        if (frames.isEmpty()) {
            PublicFunctions.insertToAS(game, this.nextAction);
            game.actionStack.remove(this);
//            if (this.nextAction.getLayer() >= this.getLayer()) {
//                this.nextAction.step(game);  //don't skip a frame
//            }
            return;
        }
    }
    
    public BattleIntro(Action nextAction) {

        this.nextAction = nextAction;

        this.frames = new ArrayList<Sprite>();

        //animation to play
        frames.add(null);
        frames.add(null);
        Texture text1 = new Texture(Gdx.files.internal("battle/intro_frame1.png"));
        Sprite sprite1 = new Sprite(text1);
        frames.add(sprite1);
        frames.add(sprite1);
        Texture text2 = new Texture(Gdx.files.internal("battle/intro_frame2.png"));
        Sprite sprite2 = new Sprite(text2);
        frames.add(sprite2);
        frames.add(sprite2);
        Texture text3 = new Texture(Gdx.files.internal("battle/intro_frame3.png"));
        Sprite sprite3 = new Sprite(text3);
        frames.add(sprite3);
        frames.add(sprite3);
        frames.add(sprite2);
        frames.add(sprite2);
        frames.add(sprite1);
        frames.add(sprite1);
        frames.add(null);
        frames.add(null);
        Texture text4 = new Texture(Gdx.files.internal("battle/intro_frame4.png"));
        Sprite sprite4 = new Sprite(text4);
        frames.add(sprite4);
        frames.add(sprite4);
        Texture text5 = new Texture(Gdx.files.internal("battle/intro_frame5.png"));
        Sprite sprite5 = new Sprite(text5);
        frames.add(sprite5);
        frames.add(sprite5);
        Texture text6 = new Texture(Gdx.files.internal("battle/intro_frame6.png"));
        Sprite sprite6 = new Sprite(text6);
        frames.add(sprite6);
        frames.add(sprite6);
        frames.add(sprite5);
        frames.add(sprite5);
        frames.add(sprite4);
        frames.add(sprite4);
        
        ArrayList<Sprite> cpyFrames = new ArrayList<Sprite> (this.frames);
        this.frames.addAll(cpyFrames);
        this.frames.addAll(cpyFrames);
        //this.frames.addAll(cpyFrames);
        frames.add(null);
        frames.add(null);
        
    }
    
}


class BattleIntroMusic extends Action {
    
    
    

    public int layer = 139;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};
    
    @Override
    public void step(Game game) {
        

        game.currMusic.pause();
        game.currMusic = game.battle.music;
        game.currMusic.play();
        
        PublicFunctions.insertToAS(game, this.nextAction);
        game.actionStack.remove(this);
        
    }
    
    public BattleIntroMusic(Action nextAction) {
        this.nextAction = nextAction;
    }
}
        


//TODO - i think this needs to call .step() for next b/c there is a missing frame before battle
class BattleIntro_anim1 extends Action {
    
    ArrayList<Sprite> frames;
    Sprite frame;
    
    

    public int layer = 139;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};
    
    @Override
    public void step(Game game) {
        
        //if done with anim, do nextAction
        if (frames.isEmpty()) {
            PublicFunctions.insertToAS(game, this.nextAction);
            game.actionStack.remove(this);
            //avoid lag
            nextAction.step(game);
            return;
        }
        
        //get next frame
        this.frame = frames.get(0);
        
        if (this.frame != null) {
            //gui version
                //this.frame.setScale(3); //scale doesn't work in batch.draw //used these when scaling mattered
                //this.frame.setPosition(16*10,16*9);
            this.frame.draw(game.floatingBatch);
            //map version
            //game.batch.draw(this.frame, 16, -16);
        }
        
        frames.remove(0);
    }
    
    public BattleIntro_anim1(Action nextAction) {
        
        this.nextAction = nextAction;
        
        this.frames = new ArrayList<Sprite>();
        
        Texture text1 = new Texture(Gdx.files.internal("battle/battle_intro_anim1_sheet1.png"));
        
        //animation to play
        for (int i = 0; i < 28; i++) {
            frames.add(new Sprite(text1, i*160, 0, 160, 144));
        }

        for (int i = 0; i < 42; i++) {
            frames.add(new Sprite(text1, 27*160, 0, 160, 144));
        }
        
    }
    
}


//scroll both players into view
class BattleAnim_positionPlayers extends Action {
    
    ArrayList<Vector2> moves_relative;
    Vector2 move;
    
    

    public int layer = 140;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};
    
    @Override
    public void step(Game game) {
        
        
        //if done with anim, do nextAction
        if (moves_relative.isEmpty()) {
            PublicFunctions.insertToAS(game, this.nextAction);
            game.actionStack.remove(this);
            return;
        }
        
        //get next frame
        this.move = moves_relative.get(0);

        float xPos = game.player.battleSprite.getX() - move.x;//*3;
        game.player.battleSprite.setX(xPos);

        xPos = game.battle.oppPokemon.sprite.getX() + move.x;//*3;
        game.battle.oppPokemon.sprite.setX(xPos);
                
        moves_relative.remove(0);
    }
    
    public BattleAnim_positionPlayers(Game game, Action nextAction) {
        
        this.nextAction = nextAction;
        
        this.moves_relative = new ArrayList<Vector2>();
        
        //animation to play
        for (int i = 0; i < 72; i++) {
            moves_relative.add(new Vector2(2,0));
        }
        
        game.player.battleSprite.setPosition(175+1-8-2,71+1-10);//(3*175+1,3*71+1);
        //game.player.battleSprite.setScale(6);
        game.player.battleSprite.setScale(2);

        game.battle.oppPokemon.sprite.setPosition(-30-4-1-14,106+2-5-15);//(3*-30,3*106+2); //TODO - x and y pos not correct...
        //note - i think my previous x was off by 1/3 a pixel, b/c val wasn't divisible by 3.
         //I am sticking with new x pos, which is really close
        //game.battle.oppPokemon.sprite.setScale(3);
        
        
    }
    
}


//scroll both players into view
class MovePlayerOffScreen extends Action {
    
    ArrayList<Vector2> positions;
    ArrayList<Integer> repeats;
    Vector2 move;
    
    Vector2 position;
    Sprite sprite;
    
    

    public int layer = 140;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};
    
    @Override
    public void step(Game game) {

        
        //if done with anim, do nextAction
        if (positions.isEmpty() || repeats.isEmpty()) {
            PublicFunctions.insertToAS(game, this.nextAction);
            game.actionStack.remove(this);
            return;
        }

        

        if (this.repeats.get(0) > 1) {
            this.repeats.set(0, this.repeats.get(0) - 1);
        }
        else {
            //since position is relative, only update once each time period
            this.position = new Vector2(game.player.battleSprite.getX(), game.player.battleSprite.getY());
            this.position.add(positions.get(0));
            this.sprite.setPosition(position.x, position.y);
            positions.remove(0);
            repeats.remove(0);
        }
        
    }
    
    public MovePlayerOffScreen(Game game, Action nextAction) {
        
        this.nextAction = nextAction;
        
        this.position = null;
        
        this.positions = new ArrayList<Vector2>();
        
        //animation to play
        positions.add(new Vector2(0,0));
        for (int i = 0; i < 3; i++) {
            positions.add(new Vector2(-8,0));
            positions.add(new Vector2(-16,0));
            positions.add(new Vector2(-8,0));
            positions.add(new Vector2(-16,0));
            positions.add(new Vector2(-8,0));
            positions.add(new Vector2(-16,0));
        }
        
        this.repeats = new ArrayList<Integer>();
        this.repeats.add(38);
        this.repeats.add(2);
        for (int i = 0; i < 5; i++) {
            repeats.add(3);
        }
        
        this.sprite = game.player.battleSprite;
        
        
    }
    
}


//scroll enemy pkmn off screen
class EnemyFaint extends Action {
    
    ArrayList<Vector2> positions;
    ArrayList<Integer> repeats;
    ArrayList<Boolean> playSound;
    Vector2 move;
    
    Vector2 position;
    Sprite sprite;
    
    

    public int layer = 120;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};
    
    boolean firstStep;
    
    Sprite helperSprite;
    
    @Override
    public void step(Game game) {

        if (this.firstStep == true) {
            //TODO - because drawing enemy sprite will likely be an
             //action later, this flag will need to instead be like
             //'FriendlyFaint' , ie remove drawAction for pokemon
            
            //stop drawing enemy sprite
            game.battle.drawAction.shouldDrawOppPokemon = false;
            
            this.firstStep = false;
        }
        
        //if done with anim, do nextAction
        if (positions.isEmpty() || repeats.isEmpty()) {
            
            //stop drawing enemy healthbar
            game.actionStack.remove(game.battle.drawAction.drawEnemyHealthAction);
            game.battle.drawAction.drawEnemyHealthAction = null;
            
            PublicFunctions.insertToAS(game, this.nextAction);
            game.actionStack.remove(this);
            return;
        }
        
        //debug
//        this.helperSprite.draw(game.floatingBatch);

        this.sprite.draw(game.floatingBatch);
        
        //debug
//        if (this.repeats.size() == 1) {
//            return;
//        }
        
        
        
        if (this.repeats.get(0) > 1) {
            this.repeats.set(0, this.repeats.get(0) - 1);
        }
        else {
            //todo - remove
//            this.position = new Vector2(this.sprite.getX(), this.sprite.getY());
//            this.position.add(positions.get(0));
//            this.sprite.setPosition(position.x, position.y);
            
            //this.sprite.setRegionY(this.sprite.getRegionY() + (int)positions.get(0).y);
            this.sprite.setRegionHeight(this.sprite.getRegionHeight() + (int)positions.get(0).y);
            this.sprite.setSize(this.sprite.getWidth(), this.sprite.getHeight() + (int)positions.get(0).y);

            if (this.playSound.get(0) == true) {

                //play victory fanfare
                game.currMusic.pause();
                game.currMusic = game.battle.victoryFanfare;
                game.currMusic.stop();
                game.currMusic.play();
            }
            
            positions.remove(0);
            repeats.remove(0);
            playSound.remove(0);
        }
        
    }
    
    public EnemyFaint(Game game, Action nextAction) {
        
        this.nextAction = nextAction;
        this.firstStep = true;
        
        this.position = null;
        
        this.positions = new ArrayList<Vector2>();
        
        //animation to play
//        for (int i = 0; i < 14; i++) {
//            positions.add(new Vector2(0,-4));
//        }
//        positions.add(new Vector2(0,0));
//        positions.add(new Vector2(0,0));
//        
//        this.repeats = new ArrayList<Integer>();
//        this.repeats.add(24);
//        for (int i = 0; i < 16; i++) {
//            repeats.add(1);
//        }
//        repeats.add(2);

        //try moving sprite iterations of 2
         //this version looks more natural
        //animation to play
        for (int i = 0; i < 7; i++) {
            positions.add(new Vector2(0,-8));
        }
        positions.add(new Vector2(0,0));
        positions.add(new Vector2(0,0));
        
        this.repeats = new ArrayList<Integer>();
        this.repeats.add(24);
        for (int i = 0; i < 8; i++) {
            repeats.add(2);
        }
//        repeats.add(2);
        
        
        
        this.playSound = new ArrayList<Boolean>();
        if (!game.map.timeOfDay.equals("Night")) {
            this.playSound.add(true);
        }
        else {
            this.playSound.add(false);
        }
        for (int i = 0; i < 15; i++) {
            this.playSound.add(false);
        }
        
        this.sprite = new Sprite(game.battle.oppPokemon.sprite);

        Texture text = new Texture(Gdx.files.internal("attack_menu/helper5.png"));
        this.helperSprite = new Sprite(text, 0,0, 16*10, 16*9);
    }
    
}


//scroll enemy pkmn off screen
class FriendlyFaint extends Action {
    
    ArrayList<Vector2> positions;
    ArrayList<Integer> repeats;
    ArrayList<Boolean> playSound;
    Vector2 move;
    
    Vector2 position;
    Sprite sprite;
    
    

    public int layer = 120;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};
    
    boolean firstStep;
    
    Sprite helperSprite;
    
    @Override
    public void step(Game game) {

        if (this.firstStep == true) {

            //stop drawing friendly healthbar
            game.actionStack.remove(game.battle.drawAction.drawFriendlyHealthAction); 
            game.battle.drawAction.drawFriendlyHealthAction = null;
            
            //stop drawing friendly sprite
            game.actionStack.remove(game.battle.drawAction.drawFriendlyPokemonAction);
            game.battle.drawAction.drawFriendlyPokemonAction = null; 
            
            this.firstStep = false;
        }
        
        //if done with anim, do nextAction
        if (positions.isEmpty() || repeats.isEmpty()) {
            
            //PublicFunctions.insertToAS(game, this.nextAction); //doing after sound is played instead; remove this
            game.actionStack.remove(this);
            return;
        }
        
        //debug
//        this.helperSprite.draw(game.floatingBatch);

        this.sprite.draw(game.floatingBatch);
        
        //debug
//        if (this.repeats.size() == 1) {
//            return;
//        }
        
        
        
        if (this.repeats.get(0) > 1) {
            this.repeats.set(0, this.repeats.get(0) - 1);
        }
        else {
            
            //this.sprite.setRegionY(this.sprite.getRegionY() + (int)positions.get(0).y);
            this.sprite.setRegionHeight(this.sprite.getRegionHeight() + (int)positions.get(0).y / 2);
            this.sprite.setSize(this.sprite.getWidth(), this.sprite.getHeight() + (int)positions.get(0).y / 2);

            if (this.playSound.get(0) == true) {

                //TODO - remove
                //play victory fanfare
//                game.currMusic.pause();
//                game.currMusic = game.battle.victoryFanfare;
//                game.currMusic.stop();
//                game.currMusic.play();
                //TODO - right timing?
                PublicFunctions.insertToAS(game, new PlaySound(game.player.currPokemon.name, this.nextAction));
            }
            
            positions.remove(0);
            repeats.remove(0);
            playSound.remove(0);
        }
        
    }
    
    public FriendlyFaint(Game game, Action nextAction) {
        
        this.nextAction = nextAction;
        this.firstStep = true;
        
        this.position = null;
        
        this.positions = new ArrayList<Vector2>();
        
        //animation to play
//        for (int i = 0; i < 14; i++) {
//            positions.add(new Vector2(0,-4));
//        }
//        positions.add(new Vector2(0,0));
//        positions.add(new Vector2(0,0));
//        
//        this.repeats = new ArrayList<Integer>();
//        this.repeats.add(24);
//        for (int i = 0; i < 16; i++) {
//            repeats.add(1);
//        }
//        repeats.add(2);

        //try moving sprite iterations of 2
         //this version looks more natural
        //animation to play
        for (int i = 0; i < 7; i++) {
            positions.add(new Vector2(0,-8));
        }
        positions.add(new Vector2(0,0));
        positions.add(new Vector2(0,0));
        
        this.repeats = new ArrayList<Integer>();
        this.repeats.add(24);
        for (int i = 0; i < 8; i++) {
            repeats.add(2);
        }
//        repeats.add(2);
        
        
        
        this.playSound = new ArrayList<Boolean>();
        if (!game.map.timeOfDay.equals("Night")) {
            this.playSound.add(true);
        }
        else {
            this.playSound.add(false);
        }
        for (int i = 0; i < 15; i++) {
            this.playSound.add(false);
        }
        
        this.sprite = new Sprite(game.player.currPokemon.backSprite);
        
        Texture text = new Texture(Gdx.files.internal("attack_menu/helper5.png"));
        this.helperSprite = new Sprite(text, 0,0, 16*10, 16*9);
    }
    
}





// TODO: i think this is outdated
//draw menu buttons (fight, run, etc)
class DrawBattleMenu1 extends Action {

    Sprite arrow;

    public int layer = 129;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};

    
    Map<String, Vector2> getCoords = new HashMap<String, Vector2>();
    String curr;
    Vector2 newPos;
    
    
    
    @Override
    public void step(Game game) {

        
        //check user input
         //'tl' = top left, etc. 
         //modify position by modifying curr to tl, tr, bl or br
        if(Gdx.input.isKeyPressed(Input.Keys.UP)) {
            if (curr.equals("bl") || curr.equals("br")) {
                curr = "t"+String.valueOf(curr.charAt(1));
                newPos = getCoords.get(curr);
            }
            
        }
        else if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            if (curr.equals("tl") || curr.equals("tr")) {
                curr = "b"+String.valueOf(curr.charAt(1));
                newPos = getCoords.get(curr);
            }
        }
        else if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            if (curr.equals("tr") || curr.equals("br")) {
                curr = String.valueOf(curr.charAt(0))+"l";
                newPos = getCoords.get(curr);
            }
        }
        else if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            if (curr.equals("tl") || curr.equals("bl")) {
                curr = String.valueOf(curr.charAt(0))+"r";
                newPos = getCoords.get(curr);
            }
        }
        
        //if button press, do something
        if(Gdx.input.isKeyPressed(Input.Keys.Z)) {
            
            //user selected 'fight'
             //for now this is 'rock', I am going to draw the safari menu later
            if (curr.equals("tl")) {
                //throw a rock, then resume battle menu
                PublicFunctions.insertToAS(game, new ThrowRock(game, this));
                game.actionStack.remove(this);
            }
            
            //user selected 'run'
            if (curr.equals("br")) {
                 //also need a 'stop playing music' thing here
                 //also need 'stop drawing battle' here
                PublicFunctions.insertToAS(game, new BattleFadeOut(game,
                                                 new WaitFrames(game, 18, 
                                                 new DisplayText(game, "Got away safely!", null, null,
                                                 new DoneAction()//new playerStanding(game)
                                                 ))));
                PublicFunctions.insertToAS(game, new BattleFadeOutMusic(game, new DoneAction()));
                PublicFunctions.insertToAS(game, new PlaySound("click1", new PlaySound("run1", new DoneAction())));
                game.actionStack.remove(this);
                game.actionStack.remove(game.battle.drawAction); //stop drawing the battle
                game.battle.drawAction = null;

            }
        }

        //System.out.println("curr: "+curr);

        //draw arrow
        this.arrow.setPosition(newPos.x, newPos.y);
        this.arrow.draw(game.floatingBatch);
    }
    
    public DrawBattleMenu1(Game game, Action nextAction) {

        this.nextAction = nextAction;
        
        Texture text = new Texture(Gdx.files.internal("battle/arrow_right1.png"));
        this.arrow = new Sprite(text, 0, 0, 5, 7);
        this.arrow.setScale(3);
        
        this.getCoords.put("tr", new Vector2(368, 79));
        this.getCoords.put("tl", new Vector2(224, 79));
        this.getCoords.put("br", new Vector2(368, 31));
        this.getCoords.put("bl", new Vector2(224, 31));
        
        this.newPos =  new Vector2(224, 79);
        this.arrow.setPosition(newPos.x, newPos.y);
        this.curr = "tl";
    }
}





//draw menu buttons (fight, run, etc)
class DrawBattleMenu_SafariZone extends Action {

    Sprite arrow;
    Sprite textBox;
    
    public int layer = 129;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};

    
    Map<String, Vector2> getCoords = new HashMap<String, Vector2>();
    String curr;
    Vector2 newPos;
    
    
    
    @Override
    public void step(Game game) {

        //check user input
         //'tl' = top left, etc. 
         //modify position by modifying curr to tl, tr, bl or br
        if(Gdx.input.isKeyPressed(Input.Keys.UP)) {
            if (curr.equals("bl") || curr.equals("br")) {
                curr = "t"+String.valueOf(curr.charAt(1));
                newPos = getCoords.get(curr);
            }
            
        }
        else if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            if (curr.equals("tl") || curr.equals("tr")) {
                curr = "b"+String.valueOf(curr.charAt(1));
                newPos = getCoords.get(curr);
            }
        }
        else if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            if (curr.equals("tr") || curr.equals("br")) {
                curr = String.valueOf(curr.charAt(0))+"l";
                newPos = getCoords.get(curr);
            }
        }
        else if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            if (curr.equals("tl") || curr.equals("bl")) {
                curr = String.valueOf(curr.charAt(0))+"r";
                newPos = getCoords.get(curr);
            }
        }
        
        //if button press, do something
        if(Gdx.input.isKeyJustPressed(Input.Keys.Z)) { //using isKeyJustPressed rather than isKeyPressed
            
            //user selected 'pokeball'
            if (curr.equals("tl")) {
                
                //decide if caught or not, use corresponding action
                 //this is a trigger action for displayText_triggered
                //Action catchAction = new catchPokemon_miss(game, this); 
                Action catchAction = calcIfCaught(game);
                
                //display text, throw animation, catch or not
                String textString = game.player.name+" used SAFARI BALL!";
                //demo code
                if (game.player.adrenaline < 5) {
                    PublicFunctions.insertToAS(game, new DisplayText(game, textString, null, catchAction,
                            new ThrowPokeball(game, catchAction)
                        )
                    );
                }
                else if (game.player.adrenaline < 15){
                    PublicFunctions.insertToAS(game, new DisplayText(game, textString, null, catchAction,
                            new ThrowFastPokeball(game, catchAction)
                        )
                    );
                }
                else {
                    PublicFunctions.insertToAS(game, new DisplayText(game, textString, null, catchAction,
                            new ThrowHyperPokeball(game, catchAction)
                        )
                    );
                }
                
                
                //throw a pokeball, then resume battle menu (maybe)
                //PublicFunctions.insertToAS(game, new ThrowPokeball(game, this));
                game.actionStack.remove(this);
            }
            
            //user selected 'throw rock'
            else if (curr.equals("bl")) {
                //throw a rock, then resume battle menu
                
                
                Action throwRockAction = new ThrowRock(game, new PrintAngryEating(game, new ChanceToRun(game, this) ) );
                String textString = game.player.name+" threw a ROCK.";
                PublicFunctions.insertToAS(game, new DisplayText(game, textString, null, throwRockAction,
                        throwRockAction
                    )
                );
                
                
                //PublicFunctions.insertToAS(game, new ThrowRock(game, this));
                game.actionStack.remove(this);
            }
            
            //user selected 'throw bait'
            else if (curr.equals("tr")) {
                
                Action throwBaitAction = new ThrowBait(game, new PrintAngryEating(game, new ChanceToRun(game, this) ) );
                String textString = game.player.name+" threw some BAIT.";
                PublicFunctions.insertToAS(game, new DisplayText(game, textString, null, throwBaitAction,
                        throwBaitAction
                    )
                );
                
                game.actionStack.remove(this);
            }
            
            //user selected 'run'
            else if (curr.equals("br")) {
                 //also need a 'stop playing music' thing here
                 //also need 'stop drawing battle' here
                PublicFunctions.insertToAS(game, new WaitFrames(game, 18, 
                                                 new DisplayText(game, "Got away safely!", null, null,
                                                 new SplitAction(
                                                         new BattleFadeOut(game,
                                                         new DoneAction()), //new playerStanding(game)),
                                                 new BattleFadeOutMusic(game,
                                                 new DoneAction())))));

                PublicFunctions.insertToAS(game, new PlaySound("click1",
                                                 new PlaySound("run1",
                                                 new DoneAction())));
                game.actionStack.remove(this);
            }
        }

        //System.out.println("curr: " + curr);

        //draw text box
        this.textBox.draw(game.floatingBatch);
        
        //draw arrow
        this.arrow.setPosition(newPos.x, newPos.y);
        this.arrow.draw(game.floatingBatch);
    }
    
    public DrawBattleMenu_SafariZone(Game game, Action nextAction) {

        this.nextAction = nextAction;
        
        Texture text = new Texture(Gdx.files.internal("battle/arrow_right1.png"));
        this.arrow = new Sprite(text, 0, 0, 5, 7);
        //this.arrow.setScale(3); //post scaling change
        
        //text box bg
        text = new Texture(Gdx.files.internal("battle/battle_text_safarizone.png"));
        this.textBox = new Sprite(text, 0,0, 16*10, 16*9);
        //this.textBox.setScale(3); //post scaling change
        //this.textBox.setPosition(16*10,16*9); //post scaling change
        
        this.getCoords.put("tr", new Vector2(105, 24));
        this.getCoords.put("tl", new Vector2(9, 24));
        this.getCoords.put("br", new Vector2(105, 8));
        this.getCoords.put("bl", new Vector2(9, 8));
        
        //this.newPos =  new Vector2(32, 79); //post scaling change
        this.newPos =  new Vector2(9, 24);
        this.arrow.setPosition(newPos.x, newPos.y);
        this.curr = "tl";
    }

    Action calcIfCaught(Game game) {
        
        //using http://bulbapedia.bulbagarden.net/wiki/Catch_rate#Capture_method_.28Generation_I.29
         //also use http://www.dragonflycave.com/safarizone.aspx
        //not sure where 'ball used' will be stored. probly some inventory location, like currItem (in inventory)
        
        int maxRand = 150; //different per-ball
        int randomNum = game.map.rand.nextInt(maxRand+1); //+1 to include upper bound
        int statusValue = 0; //different depending on oppPokemon's status
        boolean breaksFree = false;


        int ball = 15; //8 if great ball
        //demo code
        int adrenaline = game.player.adrenaline;
        if (adrenaline > 25) {
            adrenaline = 25;
        }
        //ball = ball - adrenaline; 
        int modFactor = 100;//128; - want 5 adr to catch all easy, but not medium or hard.
        int f = (int)Math.floor((game.battle.oppPokemon.currentStats.get("catchRate") * 255 * 4) / (modFactor*ball)); //modify 128 to make game harder
        //
        
        //int f = (int)Math.floor((game.battle.oppPokemon.maxStats.get("hp") * 255 * 4) / (game.battle.oppPokemon.currentStats.get("hp") * ball));
        
        //left out calculation here based on status values
         //demo - leave out status value
        //notes - adr seems to take effec too fast. also, pkmn in general are too hard to catch 
         //at beginning. shift factor down, and make adr*10
        if (randomNum - statusValue > game.battle.oppPokemon.currentStats.get("catchRate") && false) {
            breaksFree = true;
            System.out.println("(randomNum - statusValue / catchRate): ("+String.valueOf(randomNum - statusValue)+" / "+String.valueOf(game.battle.oppPokemon.currentStats.get("catchRate"))+")");
        }
        else {
            int randomNum_M = game.map.rand.nextInt(255+1);
            
            
            //randomNum_M = randomNum_M - adrenaline*20;


            if (f+(adrenaline*10) >= randomNum_M) { //demo code
                breaksFree = false;
            }
            else {
                breaksFree = true;
            }
            System.out.println("(randomNum_M / f / adr): ("+String.valueOf(randomNum_M)+" / "+String.valueOf(f)+" / +"+String.valueOf(adrenaline*10)+")");
        }
        

        //simplify and put above
        if (breaksFree == false) { //ie was caught 
            return new catchPokemon_wigglesThenCatch(game, this);
        }

        //else, ie breaksFree = true
        
        int d = game.battle.oppPokemon.currentStats.get("catchRate") * 100 / maxRand;
                //, where the value of Ball is 255 for the PokÈ Ball, 200 for the Great Ball, or 150 for other balls
        if (d >= 256) {
            //shake 3 times before breaking free
            return new catchPokemon_wiggles3Times(game, new PrintAngryEating(game, new ChanceToRun(game, this) ) );
        }
                
        int s = 0;//status thing again
        int x = d * f / 255 + s;
        
        if (x < 10) {
            //ball misses
            return new catchPokemon_miss(game, new PrintAngryEating(game, new ChanceToRun(game, this) ) );
        }
        else if (x < 30) {
            //ball shakes once
            return new catchPokemon_wiggles1Time(game, new PrintAngryEating(game, new ChanceToRun(game, this) ) );
        }
        else if (x < 70) {
            //ball shakes twice
            return new catchPokemon_wiggles2Times(game, new PrintAngryEating(game, new ChanceToRun(game, this) ) );
        }
        //ball shakes three times before pkmn gets free
        
        //System.out.println("x: "+String.valueOf(x));
        //System.out.println("Shake three times: "+String.valueOf(x));
        
        return new catchPokemon_wiggles3Times(game, new PrintAngryEating(game, new ChanceToRun(game, this) ) );
    }
}




//draw menu buttons (fight, run, etc)
class DrawBattleMenu_Normal extends MenuAction {

    Sprite arrow;
    Sprite textBox;
    
//    public int layer = 129; // TODO: verify working
    public int layer = 109; 
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};

    
    Map<String, Vector2> getCoords = new HashMap<String, Vector2>();
    String curr;
    Vector2 newPos;
    Sprite helperSprite;
    
    
    
    @Override
    public void step(Game game) {
        
        // doesn't draw any arrow if disabled
        if (this.disabled) {
            return;
        }
        
        //check user input
         //'tl' = top left, etc. 
         //modify position by modifying curr to tl, tr, bl or br
        if(Gdx.input.isKeyPressed(Input.Keys.UP)) {
            if (curr.equals("bl") || curr.equals("br")) {
                curr = "t"+String.valueOf(curr.charAt(1));
                newPos = getCoords.get(curr);
            }
            
        }
        else if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            if (curr.equals("tl") || curr.equals("tr")) {
                curr = "b"+String.valueOf(curr.charAt(1));
                newPos = getCoords.get(curr);
            }
        }
        else if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            if (curr.equals("tr") || curr.equals("br")) {
                curr = String.valueOf(curr.charAt(0))+"l";
                newPos = getCoords.get(curr);
            }
        }
        else if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            if (curr.equals("tl") || curr.equals("bl")) {
                curr = String.valueOf(curr.charAt(0))+"r";
                newPos = getCoords.get(curr);
            }
        }
        
        //if button press, do something
        if(Gdx.input.isKeyJustPressed(Input.Keys.Z)) { //using isKeyJustPressed rather than isKeyPressed
            
            //user selected 'fight'
            if (curr.equals("tl")) {

                //play select sound
                PublicFunctions.insertToAS(game, new PlaySound("click1", new DoneAction()));
                
                //remove this action, new menu that selects between pokemon attacks
                PublicFunctions.insertToAS(game, new WaitFrames(game, 4, new DrawAttacksMenu(new WaitFrames(game, 4, this))));
                
                //attacks stored in String[4] in pkmn    
                game.actionStack.remove(this);
            }

            //user selected 'item'
            else if (curr.equals("bl")) {
                this.disabled = true;
                // play select sound
                PublicFunctions.insertToAS(game, new PlaySound("click1", new DoneAction()));
                // new menu that selects between pokemon attacks
                PublicFunctions.insertToAS(game, new DrawItemMenu.Intro(this, 1,
                                                 new DrawItemMenu(game, this)));
                // remove this action,
                game.actionStack.remove(this);
            }
            
            //user selected 'throw bait'
            else if (curr.equals("tr")) {
                
                Action throwBaitAction = new ThrowBait(game, new PrintAngryEating(game, new ChanceToRun(game, this) ) );
                String textString = game.player.name+" threw some BAIT.";
                PublicFunctions.insertToAS(game, new DisplayText(game, textString, null, throwBaitAction,
                        throwBaitAction
                    )
                );
                
                game.actionStack.remove(this);
            }
            
            //user selected 'run'
            else if (curr.equals("br")) {
                 //also need a 'stop playing music' thing here
                 //also need 'stop drawing battle' here
                PublicFunctions.insertToAS(game, new WaitFrames(game, 18, 
                                                 new DisplayText(game, "Got away safely!", null, null,
                                                 new SplitAction(new BattleFadeOut(game,
                                                                   new DoneAction()), //new playerStanding(game)),
                                                 new BattleFadeOutMusic(game,
                                                 new DoneAction())
                                                 ))));
                PublicFunctions.insertToAS(game, new PlaySound("click1",
                                                 new PlaySound("run1",
                                                 new DoneAction())));
                game.actionStack.remove(this);
            }
        }

        //System.out.println("curr: " + curr);

        //draw text box
        this.textBox.draw(game.floatingBatch);

        //debug
//        helperSprite.draw(game.floatingBatch);
        
        //draw arrow
        this.arrow.setPosition(newPos.x, newPos.y);
        this.arrow.draw(game.floatingBatch);
    }
    
    public DrawBattleMenu_Normal(Game game, Action nextAction) {

        this.nextAction = nextAction;
        
        Texture text = new Texture(Gdx.files.internal("battle/arrow_right1.png"));
        this.arrow = new Sprite(text, 0, 0, 5, 7);
        //this.arrow.setScale(3); //post scaling change
        
        //text box bg
        text = new Texture(Gdx.files.internal("battle/battle_menu1.png"));
        this.textBox = new Sprite(text, 0,0, 16*10, 16*9);
        
        this.getCoords.put("tr", new Vector2(121, 24));
        this.getCoords.put("tl", new Vector2(73, 24));
        this.getCoords.put("br", new Vector2(121, 8));
        this.getCoords.put("bl", new Vector2(73, 8));
        
        //this.newPos =  new Vector2(32, 79); //post scaling change
        this.newPos =  new Vector2(73, 24);
        this.arrow.setPosition(newPos.x, newPos.y);
        this.curr = "tl";
        
        
        //helper sprite
        text = new Texture(Gdx.files.internal("attack_menu/helper4.png"));
        this.helperSprite = new Sprite(text, 0,0, 16*10, 16*9);
    }
    
}




//draw menu buttons (fight, run, etc)
class DrawAttacksMenu extends Action { 

    Sprite arrow;
    Sprite arrowWhite;
    Sprite textBox;
    
    public int layer = 108;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};

    
    Map<Integer, Vector2> getCoords;
    int curr;
    Vector2 newPos;
    Sprite helperSprite;
    ArrayList<ArrayList<Sprite>> spritesToDraw;
    int cursorDelay; //this is just extra detail. cursor has 2 frame delay before showing in R/B
    
    
    
    @Override
    public void step(Game game) {
        //check user input
         //'tl' = top left, etc. 
         //modify position by modifying curr to tl, tr, bl or br
        if(Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            if (curr != 0) {
                curr -= 1;
                newPos = getCoords.get(curr);
            }
            
        }
        else if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            if (curr != 3) {
                curr += 1;
                newPos = getCoords.get(curr);
            }
        }
        
        //if press a, do attack
        if(Gdx.input.isKeyJustPressed(Input.Keys.Z)) { //using isKeyJustPressed rather than isKeyPressed
            
            //explanation of speed move priority: http://bulbapedia.bulbagarden.net/wiki/Stats#Speed
             // pkmn with higher speed moves first
            
            //find which pokemon is first
            
            int yourSpeed = game.player.currPokemon.currentStats.get("speed");
            int oppSpeed = game.battle.oppPokemon.currentStats.get("speed");
            
            boolean oppFirst = false;
            if (yourSpeed > oppSpeed) {
                oppFirst = false;
            }
            else if (yourSpeed < oppSpeed) {
                oppFirst = true;
            }
            else {
                int randNum = game.map.rand.nextInt(2);
                if (randNum == 0) {
                    oppFirst = true;
                }
            }
            // TODO: probably have Action ClearDisplayText instead of using triggers; too confusing atm

            boolean isFriendly = true;
            //set up enemy attack
            String attackChoice = game.battle.oppPokemon.attacks[game.map.rand.nextInt(game.battle.oppPokemon.attacks.length)];
            if (attackChoice.equals("-")) {
                attackChoice = "Struggle";
            }
            //play select sound
            PublicFunctions.insertToAS(game, new PlaySound("click1", null));
            Action attack;
            if (!oppFirst) {
                attack = new DisplayText(game,
                                         game.player.currPokemon.name.toUpperCase()+" used "+game.player.currPokemon.attacks[this.curr].toUpperCase()+"!",
                                         null,
                                         true,
                                         false,
                         new AttackAnim(game, game.player.currPokemon.attacks[curr], isFriendly,
                         new DisplayText.Clear(game,
                         new WaitFrames(game, 3,
                         new DisplayText(game, 
                                         "Enemy "+game.battle.oppPokemon.name.toUpperCase()+" used "+attackChoice.toUpperCase()+"!",
                                         null, 
                                         true,
                                         false,
                         new AttackAnim(game, attackChoice, !isFriendly,
                         null))))));
            }
            else{
                attack = new DisplayText(game,
                                         "Enemy "+game.battle.oppPokemon.name.toUpperCase()+" used "+attackChoice.toUpperCase()+"!",
                                         null,
                                         true,
                                         false,
                         new AttackAnim(game, attackChoice, !isFriendly,
                         new DisplayText.Clear(game,
                         new WaitFrames(game, 3,
                         new DisplayText(game, 
                                         game.player.currPokemon.name.toUpperCase()+" used "+game.player.currPokemon.attacks[curr].toUpperCase()+"!",
                                         null, 
                                         true,
                                         false,
                         new AttackAnim(game, game.player.currPokemon.attacks[curr], isFriendly,
                         null))))));
            }
            // TODO: Battle.CheckTrapped is annoying, b/c we can't check if pkmn is trapped now
            // because it's determined in AttackAnim
            attack.appendAction(game.battle.new CheckTrapped(game,
                                new DisplayText.Clear(game,
                                new WaitFrames(game, 3,
                                this.nextAction))));
            game.actionStack.remove(this);
            PublicFunctions.insertToAS(game, attack);
            return;
        }
        //player presses b, ie wants to go back
        else if(Gdx.input.isKeyJustPressed(Input.Keys.X)) { 
            game.actionStack.remove(this);
            PublicFunctions.insertToAS(game, this.nextAction);
        }

        //System.out.println("curr: " + curr);

        //draw text box
        this.textBox.draw(game.floatingBatch);

        //debug
//        helperSprite.draw(game.floatingBatch);
        
        //draw the attack strings 
        int j = 0;
        for (ArrayList<Sprite> word : this.spritesToDraw) {
            for (Sprite sprite : word) {
                //convert string to text 
                game.floatingBatch.draw(sprite, sprite.getX() + 40, sprite.getY() - j*8 + 8);
            }
            j+=1;
        }
        
        
        //draw arrow
        if (this.cursorDelay >= 2) {
            this.arrow.setPosition(newPos.x, newPos.y);
            this.arrow.draw(game.floatingBatch);
        }
        else {
            this.cursorDelay+=1;
        }
    }
    
    public DrawAttacksMenu(Action nextAction) {

        this.nextAction = nextAction;
        this.cursorDelay = 0;
        
        this.getCoords = new HashMap<Integer, Vector2>();
        this.spritesToDraw = new ArrayList<ArrayList<Sprite>>();
        
        Texture text = new Texture(Gdx.files.internal("battle/arrow_right1.png"));
        this.arrow = new Sprite(text, 0, 0, 5, 7);
        //this.arrow.setScale(3); //post scaling change
        
        //text box bg
        text = new Texture(Gdx.files.internal("attack_menu/attack_screen1.png"));
        this.textBox = new Sprite(text, 0,0, 16*10, 16*9);
        
        this.getCoords.put(0, new Vector2(41, 32));
        this.getCoords.put(1, new Vector2(41, 24));
        this.getCoords.put(2, new Vector2(41, 16));
        this.getCoords.put(3, new Vector2(41, 8));
        
        //this.newPos =  new Vector2(32, 79); //post scaling change
        this.newPos =  new Vector2(41, 32);
        this.arrow.setPosition(newPos.x, newPos.y);
        this.curr = 0;
        
        //convert pokemon attacks to sprites
        for (String attack : Game.staticGame.player.currPokemon.attacks) {
            char[] textArray = attack.toUpperCase().toCharArray(); //iterate elements
            Sprite currSprite;
            int i = 0;
            int j = 0;
            ArrayList<Sprite> word = new ArrayList<Sprite>();
            for (char letter : textArray) {
                //offsetNext += spriteWidth*3+2 //how to do this?
                Sprite letterSprite = Game.staticGame.textDict.get((char)letter);
                if (letterSprite == null) {
                    letterSprite = Game.staticGame.textDict.get(null);
                }
                currSprite = new Sprite(letterSprite); //copy sprite from char-to-Sprite dictionary
                
                //currSprite.setPosition(10*3+8*i*3 +2, 26*3-16*j*3 +2); //offset x=8, y=25, spacing x=8, y=8(?)
                currSprite.setPosition(10+8*i +2-4, 26-16*j +2-4); //post scaling change
                //currSprite.setScale(3); //post scaling change
                
                word.add(currSprite);
                //go down a line if needed
                 //TODO - do this for words, not chars. split on space, array
                if (i >= 17) { 
                    i = 0; j++;
                }
                else {
                    i++;
                }
            }        
            spritesToDraw.add(word);    
        }
        
        
        //helper sprite
        text = new Texture(Gdx.files.internal("attack_menu/helper2.png"));
        this.helperSprite = new Sprite(text, 0,0, 16*10, 16*9);
    }
    
}


//draw player menu, ie pokedex, pokemon, items, etc. only appears in overworld, ie not a battle menu
class DrawPlayerMenu extends MenuAction { 

    Sprite arrow;
    Sprite arrowWhite;
    Sprite textBox;
    
    public int layer = 108;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};

    
    Map<Integer, Vector2> arrowCoords;
    Vector2 newPos;
    Sprite helperSprite;
    ArrayList<ArrayList<Sprite>> spritesToDraw;
    int cursorDelay; //this is just extra detail. cursor has 2 frame delay before showing in R/B
    String[] entries; //pokemon, items etc
    public static int lastIndex = 0;
    
    

    
    @Override
    public void step(Game game) {

        //System.out.println("curr: " + curr);

        //draw text box
        this.textBox.draw(game.floatingBatch);

        //debug
//        helperSprite.draw(game.floatingBatch);
        
        //draw the menu items
        int j = 0;
        for (ArrayList<Sprite> word : this.spritesToDraw) {
            for (Sprite sprite : word) {
                //convert string to text 
                game.floatingBatch.draw(sprite, sprite.getX(), sprite.getY() - j*16);
            }
            j+=1;
        }
        
        if (this.disabled == true) {
            if (this.drawArrowWhite == true) {
                this.arrowWhite.setPosition(newPos.x, newPos.y);
                this.arrowWhite.draw(game.floatingBatch);
            }
            return;
        }

        //check user input
         //'tl' = top left, etc. 
         //modify position by modifying curr to tl, tr, bl or br
        if(Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            if (this.currIndex > 0) {
                this.currIndex -= 1;
                newPos = arrowCoords.get(this.currIndex);
            }
            
        }
        else if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            if (this.currIndex < 1) {
                this.currIndex += 1;
                newPos = arrowCoords.get(this.currIndex);
            }
        }
        
        //if press a, do attack
        if(Gdx.input.isKeyJustPressed(Input.Keys.Z)) { //using isKeyJustPressed rather than isKeyPressed
            
            PublicFunctions.insertToAS(game, new PlaySound("click1", new DoneAction()));
            
            String currEntry = this.entries[this.currIndex];
            
            //we also need to create an 'action' that each of these items goes to
            if (currEntry.equals("POKÈMON")) {
                PublicFunctions.insertToAS(game, new DrawPokemonMenu.Intro(
                                                 new DrawPokemonMenu(game, this)));
            }
            else if (currEntry.equals("ITEM")) {
                PublicFunctions.insertToAS(game, new DrawItemMenu.Intro(this, 9,
                                                 new DrawItemMenu(game, this)));
            }
            
            game.actionStack.remove(this);
            this.disabled = true;
            
        }
        //player presses b, ie wants to go back
        else if(Gdx.input.isKeyJustPressed(Input.Keys.X) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) { 
            DrawPlayerMenu.lastIndex = this.currIndex;
            game.actionStack.remove(this);
            PublicFunctions.insertToAS(game, this.nextAction); 
            PublicFunctions.insertToAS(game, new PlaySound("click1", new DoneAction()));
        }

        
        //draw arrow
        if (this.cursorDelay >= 2) {
            this.arrow.setPosition(newPos.x, newPos.y);
            this.arrow.draw(game.floatingBatch);
        }
        else {
            this.cursorDelay+=1;
        }
    }
    
    public DrawPlayerMenu(Game game, Action nextAction) {

        this.disabled = false;
        this.drawArrowWhite = false;
        
        this.nextAction = nextAction;
        
        this.cursorDelay = 0;
        
        this.arrowCoords = new HashMap<Integer, Vector2>();
        this.spritesToDraw = new ArrayList<ArrayList<Sprite>>();
        
        Texture text = new Texture(Gdx.files.internal("battle/arrow_right1.png"));
        this.arrow = new Sprite(text, 0, 0, 5, 7);
        
        text = new Texture(Gdx.files.internal("battle/arrow_right_white.png"));
        this.arrowWhite = new Sprite(text, 0, 0, 5, 7);
        
        //text box bg
        text = new Texture(Gdx.files.internal("attack_menu/menu3_smaller.png"));
        this.textBox = new Sprite(text, 0,0, 16*10, 16*9);
        
        this.arrowCoords.put(0, new Vector2(89, 72+32+16));
        this.arrowCoords.put(1, new Vector2(89, 72+16+16));
        
        
        //this.newPos =  new Vector2(32, 79); //post scaling change
        this.currIndex = DrawPlayerMenu.lastIndex;
        this.newPos = this.arrowCoords.get(this.currIndex); // new Vector2(89, 72+32+16);
        this.arrow.setPosition(newPos.x, newPos.y);

//        this.menuActions = new ArrayList<Action>();  //TODO: remove

        //populate sprites for entries in menu
        this.entries = new String[]{"POKÈMON", "ITEM"};
        for (String entry : this.entries) {
            char[] textArray = entry.toCharArray(); //iterate elements
            Sprite currSprite;
            int i = 0;
            int j = 0;
            ArrayList<Sprite> word = new ArrayList<Sprite>();
            for (char letter : textArray) {

                Sprite letterSprite = game.textDict.get((char)letter);
                if (letterSprite == null) {
                    letterSprite = game.textDict.get(null);
                }
                currSprite = new Sprite(letterSprite); //copy sprite from char-to-Sprite dictionary
                
                currSprite.setPosition(96+8*i, 120-8*j); 
                word.add(currSprite);
                //go down a line if needed
                 //TODO - do this for words, not chars. split on space, array
                if (i >= 17) { 
                    i = 0; j++;
                }
                else {
                    i++;
                }
            }        
            spritesToDraw.add(word);
        }
        
        //helper sprite
        text = new Texture(Gdx.files.internal("attack_menu/helper6.png"));
        this.helperSprite = new Sprite(text, 0,0, 16*10, 16*9);
    }
    
    

    public static class Intro extends Action {
        
        ArrayList<Sprite> sprites;
        Sprite sprite;
        ArrayList<Integer> repeats;
        ArrayList<String> sounds;
        String sound;
        
        public int layer = 120;
        public int getLayer(){return this.layer;}

        public String getCamera() {return "gui";};

        
        
        Sprite helperSprite; 
        

        @Override
        public void step(Game game) {


            //get next frame
            this.sprite.draw(game.floatingBatch);
            
            //set sprite position
            //if done with anim, do nextAction
            if (this.repeats.isEmpty()) {

                PublicFunctions.insertToAS(game, new PlaySound("menu_open1", new DoneAction()));
                
                PublicFunctions.insertToAS(game, this.nextAction);
                game.actionStack.remove(this);
                return;
            }
                    
            
            //debug
//            this.helperSprite.draw(game.floatingBatch); 
            
            //debug
//            if (this.repeats.size() == 14) { 
//                return;
//            }

            //get next sound, play it
            this.sound = this.sounds.get(0);
            if (this.sound != null) {
                PublicFunctions.insertToAS(game, new PlaySound("menu_open1", new DoneAction()));
                this.sounds.set(0, null); //don't play same sound over again
            }
            
            
            //repeat sprite/pos for current object for 'frames[0]' number of frames.
            if (this.repeats.get(0) > 1) {
                this.repeats.set(0, this.repeats.get(0) - 1);
            }
            else {
                //since position is relative, only update once each time period
                repeats.remove(0);
                sounds.remove(0);
            }
        }
        
        
        public Intro(Game game, Action nextAction) {        

            this.nextAction = nextAction;

            Texture text = new Texture(Gdx.files.internal("attack_menu/menu3_smaller.png"));
            
            this.sprites = new ArrayList<Sprite>(); //may use this in future
            this.sprite = new Sprite(text, 0, 0, 160, 144);
            
            this.repeats = new ArrayList<Integer>();
            this.repeats.add(17);

            this.sounds = new ArrayList<String>();
            this.sounds.add(null);
            
            text = new Texture(Gdx.files.internal("attack_menu/helper3.png"));
            this.helperSprite = new Sprite(text, 0, 0, 160, 144);
        }
    }
    
    
    
}



//draw item menu, used in overworld and battle
 //current version is just for overworld
class DrawItemMenu extends MenuAction { 

    Sprite arrow;
    Sprite arrowWhite;
    Sprite textBox;
    
    public int layer = 107;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};

    
    Map<Integer, Vector2> arrowCoords;
    Vector2 newPos;
    Sprite helperSprite;
    ArrayList<ArrayList<Sprite>> spritesToDraw;
    int cursorDelay; //this is just extra detail. cursor has 2 frame delay before showing in R/B
    
    int cursorPos;
    int currIndex;
    ArrayList<String> itemsList;
    
    Sprite downArrow;
    int downArrowTimer;
    
    // which item the player was viewing last
    public static int lastCurrIndex = 0;
    public static int lastCursorPos = 0;
    
    // don't do this - idk why, but will occasionally overwrite prevMenu
//    MenuAction prevMenu;  
    
    @Override
    public void step(Game game) {

        if (this.prevMenu != null) {
            this.prevMenu.step(game);
        }

        //System.out.println("curr: " + curr);

        //draw text box
        this.textBox.draw(game.floatingBatch);

        //debug
//        helperSprite.draw(game.floatingBatch);
        
        //draw the menu items
        int j = 0;
        for (int i = 0; i < this.spritesToDraw.size(); i++) {
            if (i >= currIndex && i < currIndex +4) { //only draw range of 4 starting at currIndex
                ArrayList<Sprite> word = this.spritesToDraw.get(i);
                for (Sprite sprite : word) {
                    //draw this string as text on the screen
                    game.floatingBatch.draw(sprite, sprite.getX(), sprite.getY() - j*16);
                }
                j+=1;
            }
        }
        
        //return at this point if this menu is disabled
        if (this.disabled == true) {
            this.arrowWhite.setPosition(newPos.x, newPos.y);
            this.arrowWhite.draw(game.floatingBatch);
            return;
        }
        //check user input
         //'tl' = top left, etc. 
         //modify position by modifying curr to tl, tr, bl or br
        if(Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            if (cursorPos > 0) {
                cursorPos -= 1;
                newPos = arrowCoords.get(cursorPos);
            }
            else if (currIndex > 0) {
                currIndex -= 1;
            }
            
        }
        else if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            if (cursorPos < 2 && cursorPos+1 < this.itemsList.size()) {
                cursorPos += 1;
                newPos = arrowCoords.get(cursorPos);
            }
            else if (currIndex < this.itemsList.size() - 3) {
                currIndex += 1;
            }
        }
        
        //draw arrow
        if (this.cursorDelay >= 5) {
            this.arrow.setPosition(newPos.x, newPos.y);
            this.arrow.draw(game.floatingBatch);
        }
        else {
            this.cursorDelay+=1;
        }
        
        //draw downarrow if applicable
        if ( (this.itemsList.size() - this.currIndex) > 4 ) {
            if (this.downArrowTimer < 22) {
                this.downArrow.draw(game.floatingBatch);
            }
            this.downArrowTimer++;
        }
        else {
            this.downArrowTimer = 0; //force arrow to start over when scroll up
        }

        if (this.downArrowTimer > 41) {
            this.downArrowTimer = 0;
        }

        //button interaction is below drawing b/c I want to be able to return here
        //if press a, draw use/toss for item
        if(Gdx.input.isKeyJustPressed(Input.Keys.Z)) { //using isKeyJustPressed rather than isKeyPressed

            PublicFunctions.insertToAS(game, new PlaySound("click1", new DoneAction()));

            //if name == 'cancel', go back. else, get action for selected name
            String name = this.itemsList.get(currIndex + cursorPos);

            if ("Cancel".equals(name)) {
                this.prevMenu.disabled = false;
                PublicFunctions.insertToAS(game, this.prevMenu); 
                game.actionStack.remove(this);
                return;
            }
            else {
                this.disabled = true;
                System.out.println(String.valueOf(this.prevMenu));
                PublicFunctions.insertToAS(game, new DrawUseTossMenu(game, this, name)); 
                game.actionStack.remove(this);
                return;
            }
            
            //selected name = currPos + currIndex
            
        }
        //player presses b, ie wants to go back
        else if(Gdx.input.isKeyJustPressed(Input.Keys.X)) { 
            DrawItemMenu.lastCurrIndex = this.currIndex;  // save last position
            DrawItemMenu.lastCursorPos = this.cursorPos; 
            this.prevMenu.disabled = false;
            game.actionStack.remove(this);
            PublicFunctions.insertToAS(game, this.prevMenu); 
            PublicFunctions.insertToAS(game, new PlaySound("click1", new DoneAction()));
            return;
        }
        
        
    }
    
    public DrawItemMenu(Game game, MenuAction prevMenu) {

        this.prevMenu = prevMenu;
        
        this.disabled = false;
        
        this.cursorDelay = 0;
        
        this.arrowCoords = new HashMap<Integer, Vector2>();
        this.spritesToDraw = new ArrayList<ArrayList<Sprite>>();
        
        Texture text = new Texture(Gdx.files.internal("battle/arrow_right1.png"));
        this.arrow = new Sprite(text, 0, 0, 5, 7);
        
        text = new Texture(Gdx.files.internal("battle/arrow_right_white.png"));
        this.arrowWhite = new Sprite(text, 0, 0, 5, 7);
        
        //text box bg
        text = new Texture(Gdx.files.internal("attack_menu/item_menu1.png"));
        this.textBox = new Sprite(text, 0,0, 16*10, 16*9);
        
        //down arrow for items menu
        text = new Texture(Gdx.files.internal("arrow_down.png"));
        this.downArrow = new Sprite(text, 0, 0, 7, 5);
        this.downArrow.setPosition(144, 50);
        this.downArrowTimer = 0;
        
        this.currIndex = DrawItemMenu.lastCurrIndex; //this is what range of items gets displayed (4 at a time)
        this.cursorPos = DrawItemMenu.lastCursorPos; //this is where cursor is displayed

        //finite amount of cursor coordinates (4)
        //this.arrowCoords.put(1, new Vector2(89, 72+16+16)); //example
        this.arrowCoords.put(0, new Vector2(41, 104 - 16*0));
        this.arrowCoords.put(1, new Vector2(41, 104 - 16*1));
        this.arrowCoords.put(2, new Vector2(41, 104 - 16*2));
        this.arrowCoords.put(3, new Vector2(41, 104 - 16*3));

        newPos = arrowCoords.get(cursorPos);
        this.arrow.setPosition(newPos.x, newPos.y);
        
        int j = 0;
        //add 'cancel' to the items list
//        this.itemsList = new ArrayList<String>(game.player.itemsList); // TODO: delete
        this.itemsList = new ArrayList<String>(game.player.itemsDict.keySet()); // TODO: delete
        this.itemsList.add("Cancel");
        //convert player item list to sprites
        for (String entry : this.itemsList) {

            char[] textArray = entry.toUpperCase().toCharArray(); //iterate elements
            Sprite currSprite;
            int i = 0;
            ArrayList<Sprite> word = new ArrayList<Sprite>();
            for (char letter : textArray) {

                Sprite letterSprite = game.textDict.get((char)letter);
                if (letterSprite == null) {
                    letterSprite = game.textDict.get(null);
                }
                currSprite = new Sprite(letterSprite); //copy sprite from char-to-Sprite dictionary
                
                currSprite.setPosition(48+8*i, 104); //was *j 
                word.add(currSprite);
                //go down a line if needed
                 //TODO - do this for words, not chars. split on space, array
                
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
                    currSprite.setPosition(48+8*i, 104);
                    word.add(currSprite);
                }
            }
            spritesToDraw.add(word);    
            j++;
        }
        
        
        //helper sprite
//        text = new Texture(Gdx.files.internal("attack_menu/helper7.png"));
        text = new Texture(Gdx.files.internal("attack_menu/helper9.png"));
        this.helperSprite = new Sprite(text, 0,0, 16*10, 16*9);
    }
    
    

    static class Intro extends Action {

        int length;
        
        public int layer = 110;
        public int getLayer(){return this.layer;}

        public String getCamera() {return "gui";};
        
        
        
        MenuAction prevMenu;

        @Override
        public void step(Game game) {
            if (this.prevMenu != null) {
                this.prevMenu.step(game);
            }
            this.length--;
            if (this.length <= 0) {
                PublicFunctions.insertToAS(game, this.nextAction);
                game.actionStack.remove(this);
            }
                    
        }
    
        public Intro(MenuAction prevMenu, int length, Action nextAction) {
            this.prevMenu = prevMenu;
            this.nextAction = nextAction;
            this.length = length;
        }
        
    }
    
    
}



//menu displays 'use' and 'toss'
class DrawUseTossMenu extends MenuAction { 

    Sprite arrow;
    Sprite textBox;
    
    public int layer = 106;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};

    Map<Integer, Vector2> getCoords;
    int curr;
    Vector2 newPos;
    Sprite helperSprite;
    ArrayList<ArrayList<Sprite>> spritesToDraw;
    int cursorDelay; //this is just extra detail. cursor has 2 frame delay before showing in R/B
    
    String itemName;

    MenuAction prevMenu;
    
    @Override
    public void step(Game game) {

        //if there is a previous menu, step through it to display text
        if (prevMenu != null) {
            prevMenu.step(game);
        }
        
        //check user input
         //'tl' = top left, etc. 
         //modify position by modifying curr to tl, tr, bl or br
        if(Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            if (curr > 0) {
                curr -= 1;
                newPos = getCoords.get(curr);
            }
            
        }
        else if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            if (curr < 1) {
                curr += 1;
                newPos = getCoords.get(curr);
            }
        }
        
        //System.out.println("curr: " + curr);

        //draw text box
        this.textBox.draw(game.floatingBatch);

        //debug
//        helperSprite.draw(game.floatingBatch);
        
        //draw the attack strings 
        int j = 0;
        for (ArrayList<Sprite> word : this.spritesToDraw) {
            for (Sprite sprite : word) {
                //convert string to text 
                game.floatingBatch.draw(sprite, sprite.getX() + 40, sprite.getY() - j*8 + 8);
            }
            j+=1;
        }
        
        
        //draw arrow
        if (this.cursorDelay >= 2) {
            this.arrow.setPosition(newPos.x, newPos.y);
            this.arrow.draw(game.floatingBatch);
        }
        else {
            this.cursorDelay+=1;
        }

        //if press a, do attack
        if(Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            //perform the action based on which item selected
            game.actionStack.remove(this);
            useItem(game, this.itemName);
            return;
        }
        //player presses b, ie wants to go back
        else if(Gdx.input.isKeyJustPressed(Input.Keys.X)) { 
            this.prevMenu.disabled = false;
            game.actionStack.remove(this);
            PublicFunctions.insertToAS(game, this.prevMenu);
            return;
        }
        
    }
    
    //constructor for when this was called by the item menu
     //probably will create separate constructor for other cases
    public DrawUseTossMenu(Game game, MenuAction prevMenu, String itemName) {

        this.prevMenu = prevMenu; //previously visiting menu
        this.itemName = itemName; //which item was selected from previous menu
        
        this.cursorDelay = 0;
        
        this.getCoords = new HashMap<Integer, Vector2>();
        this.spritesToDraw = new ArrayList<ArrayList<Sprite>>();
        
        Texture text = new Texture(Gdx.files.internal("battle/arrow_right1.png"));
        this.arrow = new Sprite(text, 0, 0, 5, 7);
        //this.arrow.setScale(3); //post scaling change
        
        //text box bg
        text = new Texture(Gdx.files.internal("attack_menu/usetoss_menu1.png"));
        this.textBox = new Sprite(text, 0,0, 16*10, 16*9);
        
        this.getCoords.put(0, new Vector2(113, 48));
        this.getCoords.put(1, new Vector2(113, 48-16));
        
        //this.newPos =  new Vector2(32, 79); //post scaling change
        this.newPos =  new Vector2(113, 48);
        this.arrow.setPosition(newPos.x, newPos.y);
        this.curr = 0;
        
        //if you want to customize menu text, add to this.spritesToDraw here
        
        //helper sprite
        text = new Texture(Gdx.files.internal("attack_menu/helper8.png"));
        this.helperSprite = new Sprite(text, 0,0, 16*10, 16*9);
    }
    
    public void useItem(Game game, String itemName) {
        // 
        if (itemName.toLowerCase().equals("ultra ball")) {
            // calculate if pokemon was caught
            this.prevMenu.prevMenu.disabled = false;  // menu won't get drawn until catch anim is over
            Action catchAction = Battle_Actions.calcIfCaught(game, this.prevMenu.prevMenu);
            // display text, throw animation, catch or not
            String textString = game.player.name+" used "+itemName.toUpperCase()+"!";
            PublicFunctions.insertToAS(game, new DisplayText(game, textString, null, catchAction,
                                             new ThrowPokeball(game, catchAction
                                             )));
        }
    }
}


//pokemon menu, used in battle and overworld
class DrawPokemonMenu extends MenuAction { 

    public int layer = 107;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};
    
    Sprite bgSprite;
    Sprite helperSprite;
    Sprite arrow;
    Sprite arrowWhite;
    Sprite healthBar;
    Sprite healthSprite;
    Vector2 newPos;
    Map<Integer, Vector2> arrowCoords;
    public static boolean drawChoosePokemonText = true;
    public static int avatarAnimCounter = 12;
    public static int currIndex = 0; //currently selected pokemon
    public static int lastIndex = 0;

    @Override
    public void step(Game game) {

        if (this.prevMenu != null) {
            this.prevMenu.step(game);
        }

        this.bgSprite.draw(game.floatingBatch);    
        
        //draw helper sprite
        //debug
//        this.helperSprite.draw(game.floatingBatch);

        // 1 frame delay - delay when switching to new avatar
        // 6 frames first, 6 second
        //draw health bars
        for (int i=0; i < game.player.pokemon.size(); i++) {
            Pokemon currPokemon = game.player.pokemon.get(i);

            //animate current pokemon avatar
            if (i == this.currIndex) {
                if (this.avatarAnimCounter >= 6) {
                    game.floatingBatch.draw(currPokemon.avatarSprites.get(0), 8, 128 -16*i);
                }
                else {
                    game.floatingBatch.draw(currPokemon.avatarSprites.get(1), 8, 128 -16*i);
                }
            }
            else {
                game.floatingBatch.draw(currPokemon.avatarSprites.get(0), 8, 128 -16*i);
            }

            //draw status bar
            game.floatingBatch.draw(this.healthBar, 0, 128 -16*i);

            //draw pkmn level text
            int tensPlace = currPokemon.level/10;
            Sprite tensPlaceSprite = game.textDict.get(Character.forDigit(tensPlace,10));
            game.floatingBatch.draw(tensPlaceSprite, 112, 136 -16*i);
            int offset = 0;
            if (currPokemon.level >= 10) {
                offset = 8;
            }
            int onesPlace = currPokemon.level % 10;
            Sprite onesPlaceSprite = game.textDict.get(Character.forDigit(onesPlace,10));
            game.floatingBatch.draw(onesPlaceSprite, 112 +offset, 136 -16*i);

            //draw pkmn max health text
            int maxHealth = currPokemon.maxStats.get("hp");
            int hundredsPlace = maxHealth/100;
            if (hundredsPlace > 0) {
                Sprite hudredsPlaceSprite = game.textDict.get(Character.forDigit(hundredsPlace,10));
                game.floatingBatch.draw(hudredsPlaceSprite, 136, 128 -16*i);
            }
            tensPlace = (maxHealth % 100) / 10;
            if (tensPlace > 0 || hundredsPlace > 0) {
                tensPlaceSprite = game.textDict.get(Character.forDigit(tensPlace,10));
                game.floatingBatch.draw(tensPlaceSprite, 136 +8, 128 -16*i);
            }
            onesPlace = maxHealth % 10;
            onesPlaceSprite = game.textDict.get(Character.forDigit(onesPlace,10));
            game.floatingBatch.draw(onesPlaceSprite, 136 +16, 128 -16*i);

            //draw pkmn current health text
            int currHealth = currPokemon.currentStats.get("hp");
            hundredsPlace = currHealth/100;
            if (hundredsPlace > 0) {
                Sprite hudredsPlaceSprite = game.textDict.get(Character.forDigit(hundredsPlace,10));
                game.floatingBatch.draw(hudredsPlaceSprite, 104, 128 -16*i);
            }
            tensPlace = (currHealth % 100) / 10;
            if (tensPlace > 0 || hundredsPlace > 0) {
                tensPlaceSprite = game.textDict.get(Character.forDigit(tensPlace,10));
                game.floatingBatch.draw(tensPlaceSprite, 104 +8, 128 -16*i);
            }
            onesPlace = currHealth % 10;
            onesPlaceSprite = game.textDict.get(Character.forDigit(onesPlace,10));
            game.floatingBatch.draw(onesPlaceSprite, 104 +16, 128 -16*i);

            //draw pokemon name
            char[] textArray = currPokemon.name.toUpperCase().toCharArray();
            Sprite letterSprite;
            for (int j=0; j < textArray.length; j++) {
                letterSprite = game.textDict.get(textArray[j]);
                game.floatingBatch.draw(letterSprite, 24 +8*j, 136 -16*i);
            }

            //draw health bar
            int targetSize = (int)Math.ceil( (currPokemon.currentStats.get("hp")*48) / currPokemon.maxStats.get("hp"));
            for (int j=0; j < targetSize; j++) {
                game.floatingBatch.draw(this.healthSprite, 48 +1*j, 131 -16*i);
            }
        }

        //draw 'Choose a pokemon' text
        if (this.drawChoosePokemonText == true) {
            char[] textArray = "Choose a POKÈMON.".toCharArray();
            Sprite letterSprite;
            for (int j=0; j < textArray.length; j++) {
                letterSprite = game.textDict.get(textArray[j]);
                game.floatingBatch.draw(letterSprite, 8 +8*j, 24);
            }
        }
        
        if (this.drawArrowWhite == true) {
            //draw white arrow
            this.arrowWhite.setPosition(this.newPos.x, this.newPos.y);
            this.arrowWhite.draw(game.floatingBatch);
        }

        //return at this point if this menu is disabled
        if (this.disabled == true) {
            return;
        }

        //decrement avatar anim counter
        this.avatarAnimCounter--;
        if (this.avatarAnimCounter <= 0) {
            this.avatarAnimCounter = 11;
        }
        
        //handle arrow input
        if(Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            if (this.currIndex > 0) {
                this.currIndex -= 1;
                this.avatarAnimCounter = 12; //reset to 12 for 1 extra frame of first frame for avatar anim
            }
        }
        else if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            if (this.currIndex < game.player.pokemon.size()-1) {
                this.currIndex += 1;
                this.avatarAnimCounter = 12; //reset to 12 for 1 extra frame of first frame for avatar anim
            }
        }
        newPos = this.arrowCoords.get(this.currIndex);

        //draw the arrow sprite
        this.arrow.setPosition(this.newPos.x, this.newPos.y);
        this.arrow.draw(game.floatingBatch);

        //button interaction is below drawing b/c I want to be able to return here
        //if press a, draw use/toss for item
        if(Gdx.input.isKeyJustPressed(Input.Keys.Z)) { //using isKeyJustPressed rather than isKeyPressed
            PublicFunctions.insertToAS(game, new PlaySound("click1", new DoneAction()));

            this.disabled = true;
            this.drawArrowWhite = true;
            //once player hits b in selected menu, avatar anim starts over
            Pokemon currPokemon = game.player.pokemon.get(this.currIndex);
            PublicFunctions.insertToAS(game, new DrawPokemonMenu.SelectedMenu(this, currPokemon));
            game.actionStack.remove(this);
            return;
        }
        //player presses b, ie wants to go back
        if(Gdx.input.isKeyJustPressed(Input.Keys.X)) {
            DrawPokemonMenu.lastIndex = this.currIndex;
            game.actionStack.remove(this);
            PublicFunctions.insertToAS(game, new DrawPokemonMenu.Outro(
                                             this.prevMenu));
            return;
        }
    }

    public DrawPokemonMenu(Game game, MenuAction prevMenu) {

        this.prevMenu = prevMenu;
        
        Texture text = new Texture(Gdx.files.internal("battle/arrow_right_white.png"));
        this.arrowWhite = new Sprite(text, 0, 0, 5, 7);

        text = new Texture(Gdx.files.internal("battle/arrow_right1.png"));
        this.arrow = new Sprite(text, 0, 0, 5, 7);

        text = new Texture(Gdx.files.internal("pokemon_menu/health_bar.png"));
        this.healthBar = new Sprite(text, 0, 0, 160, 16);

        this.currIndex = DrawPokemonMenu.lastIndex;

        this.arrowCoords = new HashMap<Integer, Vector2>();
        for (int i=0; i < 6; i++) {
            this.arrowCoords.put(i, new Vector2(1, 128 - 16*i));
        }
        //cursor position based on lastIndex
        this.newPos = this.arrowCoords.get(this.currIndex);
        
        //TODO: debug, delete
//        for (int i=0; i < game.player.pokemon.size(); i++) {
//            System.out.println(game.player.pokemon.get(i).maxStats.get("hp"));
//        }

        text = new Texture(Gdx.files.internal("battle/health1.png"));
        this.healthSprite = new Sprite(text, 0,0,1,2);
        
        text = new Texture(Gdx.files.internal("battle/battle_bg3.png"));
        this.bgSprite = new Sprite(text, 0, 0, 16*10, 16*9);
        
        //helper sprite
//        text = new Texture(Gdx.files.internal("pokemon_menu/helper1.png"));
//        Texture text = new Texture(Gdx.files.internal("pokemon_menu/helper2.png"));
//        this.helperSprite = new Sprite(text, 0,0, 16*10, 16*9);
    }

    static class Intro extends Action {

        MenuAction prevMenu;

        public int layer = 110;
        public int getLayer(){return this.layer;}

        public String getCamera() {return "gui";};

        int duration = 18;
        Sprite bgSprite;

        @Override
        public void step(Game game) {

            this.bgSprite.draw(game.floatingBatch);
            
            //draw a white bg for 18 frames
            this.duration--;
            
            if (this.duration <= 0) {
                PublicFunctions.insertToAS(game, this.prevMenu);
                game.actionStack.remove(this);
            }    
        }

        public Intro(MenuAction prevMenu) {

            this.prevMenu = prevMenu;

            Texture text = new Texture(Gdx.files.internal("battle/intro_frame6.png"));
            this.bgSprite = new Sprite(text, 0, 0, 16*10, 16*9);
        }
    }

    static class Outro extends Action {

        // 33 frames white
        // 1 frame where cursor is still white and can't move cursor (people aren't visible)
        // 1 frame people aren't visible (not implementing)

        MenuAction prevMenu;

        public int layer = 110;
        public int getLayer(){return this.layer;}

        public String getCamera() {return "gui";};

        int duration = 34;
        Sprite bgSprite;

        @Override
        public void step(Game game) {

            if (this.prevMenu != null) {
                this.prevMenu.step(game);
            }

            this.duration--;

            //last from no white bg
            if (this.duration > 0) {
                this.bgSprite.draw(game.floatingBatch);
            }

            if (this.duration <= 0) {
                if (this.prevMenu != null) {
                    PublicFunctions.insertToAS(game, this.prevMenu);
                    this.prevMenu.disabled = false;
                    this.prevMenu.drawArrowWhite = false;
                }
                game.actionStack.remove(this);
            }
        }

        public Outro(MenuAction prevMenu) {

            this.prevMenu = prevMenu;
            if (this.prevMenu != null) {
                this.prevMenu.drawArrowWhite = true;
            }

            Texture text = new Texture(Gdx.files.internal("battle/intro_frame6.png"));
            this.bgSprite = new Sprite(text, 0, 0, 16*10, 16*9);
        }
    }

    static class SelectedMenu extends MenuAction {

        Sprite arrow;
        Sprite textBox;
        Pokemon pokemon;
        
        public int layer = 106;
        public int getLayer(){return this.layer;}

        public String getCamera() {return "gui";};

        Map<Integer, Vector2> getCoords;
        int curr;
        Vector2 newPos;
        Sprite helperSprite;
        ArrayList<String> words; //menu items
        int textboxDelay = 0; //this is just extra detail. text box has 1 frame delay before appearing
        
        // maps menu selection to an action
        public Action getAction(Game game, String word, MenuAction prevMenu) {
            if (word.equals("STATS")) {
                return new SelectedMenu.Switch(prevMenu); //TODO - Stats menu
            }
            else if (word.equals("SWITCH")) {
                return new SelectedMenu.Switch(prevMenu);
            }
            // generate actions for HMs
            else if (word.equals("BUILD")) {
                // text box '__ used BUILD!'
                // outro anim to overworld
                // then swap player with pokemon anim
                //  hmm where does player go if no space? how about pokemon is super-imposed
                //  or maybe player walks away if there is an available space, default down
                //  just replace player sprite with pkmn
                //  need overworld sprite
                // then move like normal, with following player
                //  
                // TODO: can't return this as a new action, so just inserting here
                // 
//                PublicFunctions.insertToAS(game, this.prevMenu);  // keep drawing but don't enable
//                DrawPokemonMenu.lastIndex = prevMenu.currIndex;
                game.player.isBuilding = true;  // tile to build appears in front of player
                game.player.isCutting = false;
                game.player.isHeadbutting = false;
                return new SelectedMenu.ExitAfterActions(this.prevMenu,
                       new DisplayText(game, game.player.pokemon.get(DrawPokemonMenu.currIndex).name.toUpperCase()+" used BUILD! Press C and V to select tiles.", null, null, 
                       new DoneAction()
                       ));
            }
            else if (word.equals("CUT")) {
                game.player.isCutting = true;
                game.player.isBuilding = false;
                game.player.isHeadbutting = false;
                return new SelectedMenu.ExitAfterActions(this.prevMenu,
                       new DisplayText(game, game.player.pokemon.get(DrawPokemonMenu.currIndex).name.toUpperCase()+" is using CUT!", null, null, 
                       new DoneAction()));
            }
            else if (word.equals("HEADBUTT")) {
                game.player.isHeadbutting = true;
                game.player.isBuilding = false;
                game.player.isCutting = false;
                return new SelectedMenu.ExitAfterActions(this.prevMenu,
                       new DisplayText(game, game.player.pokemon.get(DrawPokemonMenu.currIndex).name.toUpperCase()+" is using HEADBUTT!", null, null, 
                       new DoneAction()));
            }
            return null;
        }
        
        @Override
        public void step(Game game) {

            //if there is a previous menu, step through it to display text
            if (prevMenu != null) {
                prevMenu.step(game);
            }

            //white arrow only for one frame, then box appears
            if (this.textboxDelay < 1) {
                this.textboxDelay+=1;
                return;
            }

            //check user input
             //'tl' = top left, etc. 
             //modify position by modifying curr to tl, tr, bl or br
            if(Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                if (curr > 0) {
                    curr -= 1;
                    newPos = getCoords.get(curr);
                }
            }
            else if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                if (curr < 2) {
                    curr += 1;
                    newPos = getCoords.get(curr);
                }
            }

            //draw text box
            this.textBox.draw(game.floatingBatch);

            //debug
//            helperSprite.draw(game.floatingBatch);

            //draw the menu items (stats, switch, cancel)
            Sprite letterSprite;
            for (int i=0; i < this.words.size(); i++) {
                String word = this.words.get(i);
                for (int j=0; j < word.length(); j++) {
                    char letter = word.charAt(j);
                    //convert string to text 
                    letterSprite = game.textDict.get(letter);
                    game.floatingBatch.draw(letterSprite, 104 +8*j, 40 -16*i);
                    // todo: need to modify to shift words up if there are hms
                }
            }

            //draw arrow sprite
            this.arrow.setPosition(newPos.x, newPos.y);
            this.arrow.draw(game.floatingBatch);

            if(Gdx.input.isKeyJustPressed(Input.Keys.Z)) { 
                
                //get action for this item

                //perform the action
                 //actually this will probably be performed in 'getAction'

                String word = this.words.get(this.curr);
                if ("CANCEL".equals(word)) {
                    DrawPokemonMenu.avatarAnimCounter = 12;
                    PublicFunctions.insertToAS(game, new PlaySound("click1", new DoneAction()));

                    game.actionStack.remove(this);
                    PublicFunctions.insertToAS(game, new DrawPokemonMenu.Outro(
                                                     this.prevMenu.prevMenu));
                    return;
                }
                else {
                    Action action = getAction(game, word, this.prevMenu);
                    game.actionStack.remove(this);
                    PublicFunctions.insertToAS(game, action);
                    return;
                }
                
            }
            //player presses b, ie wants to go back
            else if(Gdx.input.isKeyJustPressed(Input.Keys.X)) {
                PublicFunctions.insertToAS(game, new PlaySound("click1", new DoneAction()));
                //reset avatar anim
                DrawPokemonMenu.avatarAnimCounter = 12;
                game.actionStack.remove(this);
                PublicFunctions.insertToAS(game, new SelectedMenu.Outro(this.prevMenu));
                return;
            }
        }

        public SelectedMenu(MenuAction prevMenu, Pokemon pokemon) {

            this.prevMenu = prevMenu; //previously visiting menu
            this.pokemon = pokemon;

            this.getCoords = new HashMap<Integer, Vector2>();
            this.words = new ArrayList<String>();

            int numHms = pokemon.hms.size();
            this.getCoords.put(0, new Vector2(97, 40 +16*numHms));
            this.getCoords.put(1, new Vector2(97, 40-16 +16*numHms));
            this.getCoords.put(2, new Vector2(97, 40-32 +16*numHms));
            
            int i = 3;
            // add HMs from selected pokemon
            for (String hm : pokemon.hms) {
                this.words.add(hm);
                this.getCoords.put(i, new Vector2(97, 40-32-16 +16*i));
                i++;
            }
            
            this.words.add("STATS");
            this.words.add("SWITCH");
            this.words.add("CANCEL");

            Texture text = new Texture(Gdx.files.internal("battle/arrow_right1.png"));
            this.arrow = new Sprite(text, 0, 0, 5, 7);
            //this.arrow.setScale(3); //post scaling change

            //text box bg
            text = new Texture(Gdx.files.internal("pokemon_menu/selected_menu1.png"));
            this.textBox = new Sprite(text, 0,0, 16*10, 16*9);

            //this.newPos =  new Vector2(32, 79); //post scaling change
            this.newPos =  new Vector2(97, 40);
            this.arrow.setPosition(newPos.x, newPos.y);
            this.curr = 0;

            //if you want to customize menu text, add to this.spritesToDraw here

            //helper sprite
            text = new Texture(Gdx.files.internal("pokemon_menu/helper3.png"));
            this.helperSprite = new Sprite(text, 0,0, 16*10, 16*9);
        }

        static class Outro extends Action {

            //9 frames no arrow no bg (appears on 10th frame)
            //'Choose pokemon' text disappears on 3th frame, appears on 6th frame
            
            MenuAction prevMenu;

            public int layer = 110;
            public int getLayer(){return this.layer;}

            public String getCamera() {return "gui";};

            int duration = 9;

            @Override
            public void step(Game game) {

                this.prevMenu.drawArrowWhite = false;
                
                if (prevMenu != null) {
                    prevMenu.step(game);
                }
                
                this.duration--;

                if (this.duration == 6) {
                    DrawPokemonMenu.drawChoosePokemonText = false;
                }    
                else if (this.duration == 3) {
                    DrawPokemonMenu.drawChoosePokemonText = true;
                }    
                else if (this.duration <= 0) {
                    PublicFunctions.insertToAS(game, this.prevMenu);
                    this.prevMenu.disabled = false;
                    game.actionStack.remove(this);
                }    
            }

            public Outro(MenuAction prevMenu) {
                this.prevMenu = prevMenu;
            }
        }

        static class ExitAfterActions extends MenuAction {
            public int layer = 107;
            public int getLayer(){return this.layer;}
            public String getCamera() {return "gui";};
            
            boolean firstStep = true;
            
            @Override
            public void step(Game game) {
                if (this.firstStep) {
                    PublicFunctions.insertToAS(game, this.nextAction);
                    this.firstStep = false;
                }
                //if there is a previous menu, step through it to display text
                if (prevMenu != null) {
                    prevMenu.step(game);
                }
                // if there are no more actions in the nextActions chain, 
                //  then exit DrawPokemonMenu
                Action action = this.nextAction;
                while (action != null) {
                    if (game.actionStack.contains(action)) {
                        break;
                    }
                    action = action.nextAction;
                }
                if (action == null) {
                    DrawPokemonMenu.lastIndex = prevMenu.currIndex;
                    DrawPokemonMenu.lastIndex = prevMenu.currIndex;
                    game.actionStack.remove(this);
                    PublicFunctions.insertToAS(game, new DrawPokemonMenu.Outro(null));
//                    game.playerCanMove = true;
                    PublicFunctions.insertToAS(game, new WaitFrames(game, 30,
                                                     new PlayerCanMove(game,
                                                     new DoneAction())));
                    return;
                }
            }

            public ExitAfterActions(MenuAction prevMenu, Action nextAction) {
                this.prevMenu = prevMenu; //previously visiting menu
                this.nextAction = nextAction;
            }
        }

        static class Switch extends MenuAction {

            // 1 frame menu disappears
            // 1 frame 'Choose a pokemon' text disappears
            // 2 frames nothing 
            // 4 frames 'Move where?'
            // 1 frame arrow turns black (white arrow is drawn under)
            //  - assume allowed to move cursor here

            Sprite arrow;

            public int layer = 106;
            public int getLayer(){return this.layer;}

            public String getCamera() {return "gui";};

            Map<Integer, Vector2> arrowCoords;
            int curr;
            int startPosition;
            Vector2 newPos;
//            Sprite helperSprite;
            int timer = 0; //used for various intro anim timings

            @Override
            public void step(Game game) {

                //if there is a previous menu, step through it to display text
                if (prevMenu != null) {
                    prevMenu.step(game);
                }

                //draw arrow sprite
                this.arrow.setPosition(newPos.x, newPos.y);
                this.arrow.draw(game.floatingBatch);

                if (this.disabled == true) {
                    return;
                }
                
                if (this.timer < 10) {
                    this.timer++;
                }

                //decrement avatar anim counter
                DrawPokemonMenu.avatarAnimCounter--;
                if (DrawPokemonMenu.avatarAnimCounter <= 0) {
                    DrawPokemonMenu.avatarAnimCounter = 11;
                }

                if (this.timer == 2) {
                    DrawPokemonMenu.drawChoosePokemonText = false;
                }
                else if (this.timer > 4) {
                    char[] textArray = "Move POKÈMON".toCharArray();
                    Sprite letterSprite;
                    for (int j=0; j < textArray.length; j++) {
                        letterSprite = game.textDict.get(textArray[j]);
                        game.floatingBatch.draw(letterSprite, 8 +8*j, 24);
                    }
                    textArray = "where?".toCharArray();
                    for (int j=0; j < textArray.length; j++) {
                        letterSprite = game.textDict.get(textArray[j]);
                        game.floatingBatch.draw(letterSprite, 8 +8*j, 8);
                    }
                }
                if (this.timer > 8) {
                    if(Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                        if (this.curr > 0) {
                            this.curr -= 1;
                            DrawPokemonMenu.currIndex = this.curr; //DrawPokemonMenu animates the avatars
                            newPos = this.arrowCoords.get(this.curr);
                        }
                    }
                    else if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                        if (this.curr < game.player.pokemon.size()-1) {
                            this.curr += 1;
                            DrawPokemonMenu.currIndex = this.curr; //DrawPokemonMenu animates the avatars
                            newPos = this.arrowCoords.get(this.curr);
                        }
                    }

                    //debug
//                    helperSprite.draw(game.floatingBatch);

                    if(Gdx.input.isKeyJustPressed(Input.Keys.Z)) { 
                        
                        //switch
                        // TODO: anim?
                        // TODO: probably won't refresh
                        Pokemon movePokemon = game.player.pokemon.get(this.startPosition);
                        Pokemon movePokemon2 = game.player.pokemon.get(this.curr);
                        game.player.pokemon.remove(this.startPosition);
                        game.player.pokemon.add(this.startPosition, movePokemon2);
                        game.player.pokemon.remove(this.curr);
                        game.player.pokemon.add(this.curr, movePokemon);
                        
                        // replace currPokemon if applicable
                        if (this.startPosition == 0) {
                            game.player.currPokemon = movePokemon2;
                        }
                        if (this.curr == 0) {
                            game.player.currPokemon = movePokemon;
                        }
                        
                        this.disabled = true;
                        PublicFunctions.insertToAS(game, new PlaySound("click1", new DoneAction()));
                        //reset avatar anim
                        DrawPokemonMenu.avatarAnimCounter = 12;
                        game.actionStack.remove(this);
                        PublicFunctions.insertToAS(game, new Switch.Outro(this));
                        return;
                    }
                    //player presses b, ie wants to go back
                    else if(Gdx.input.isKeyJustPressed(Input.Keys.X)) {

                        this.disabled = true;
                        PublicFunctions.insertToAS(game, new PlaySound("click1", new DoneAction()));
                        //reset avatar anim
                        DrawPokemonMenu.avatarAnimCounter = 12;
                        game.actionStack.remove(this);
                        PublicFunctions.insertToAS(game, new Switch.Outro(this));
                        return;
                    }
                }
            }
    
            public Switch(MenuAction prevMenu) {
                this.prevMenu = prevMenu; //previously visiting menu

                Texture text = new Texture(Gdx.files.internal("battle/arrow_right1.png"));
                this.arrow = new Sprite(text, 0, 0, 5, 7);

                this.arrowCoords = new HashMap<Integer, Vector2>();
                for (int i=0; i < 6; i++) {
                    this.arrowCoords.put(i, new Vector2(1, 128 - 16*i));
                }

                this.startPosition = this.curr = DrawPokemonMenu.currIndex;
                this.newPos =  this.arrowCoords.get(this.curr);
                this.arrow.setPosition(newPos.x, newPos.y);

                //helper sprite
//                text = new Texture(Gdx.files.internal("pokemon_menu/helper3.png"));
//                this.helperSprite = new Sprite(text, 0,0, 16*10, 16*9);
            }

            static class Outro extends MenuAction {

                // 1 frame 'move where' disappears (black and white arrow still visible)
                // 1 frame black and white cursor disappear
                // 1 nothing
                // 4 'choose a pokemon' appears
                // 1 black cursor appears, assume can move cursor at this point
                // 1 frame avatar anim at 12 here (1 extra frame for avatar anim)
                public int layer = 110;
                public int getLayer(){return this.layer;}
                public String getCamera() {return "gui";};
                int timer = 0; //timer counting up

                @Override
                public void step(Game game) {

                    if (prevMenu != null) {
                        prevMenu.step(game);
                    }
                    
                    this.timer++;
                    if (this.timer == 1) {
                        this.prevMenu = this.prevMenu.prevMenu;
                        this.prevMenu.drawArrowWhite = false;
                    }
                    else if (this.timer == 3) {
                        DrawPokemonMenu.drawChoosePokemonText = true;
                    }    
                    else if (this.timer >= 7) {
                        DrawPokemonMenu.avatarAnimCounter = 13; //one extra avatar frame
                        game.actionStack.remove(this);
                        PublicFunctions.insertToAS(game, this.prevMenu);
                        this.prevMenu.disabled = false;
                    }
                }

                public Outro(MenuAction prevMenu) {
                    this.prevMenu = prevMenu;
                }
            }
        }
    }
}



//fade out of battle to white
//fade out music too
class BattleFadeOut extends Action {
    
    ArrayList<Sprite> frames;
    Sprite frame;
    
    

    public int layer = 129;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};
    
    @Override
    public void step(Game game) {
        
        //should only happen once. not sure that repeat matters
        game.actionStack.remove(game.battle.drawAction); //stop drawing the battle
        game.battle.drawAction = null; //this is used as a flag in ThrowOutPokemon
        
        //if done with anim, do nextAction
        if (frames.isEmpty()) {
            PublicFunctions.insertToAS(game, this.nextAction);
            game.actionStack.remove(this);
            game.playerCanMove = true;
            return;
        }
        
        //get next frame
        this.frame = frames.get(0);
        
        if (this.frame != null) {
            //gui version
            this.frame.setScale(3); //scale doesn't work in batch.draw
            this.frame.setPosition(16*10,16*9);
            this.frame.draw(game.floatingBatch);
            //map version
            //game.batch.draw(this.frame, 16, -16);
        }
        
        frames.remove(0);

    }
    
    public BattleFadeOut(Game game, Action nextAction) {
        
        this.nextAction = nextAction;

        this.frames = new ArrayList<Sprite>();
        
        //fade out from white anim
        Texture text1 = new Texture(Gdx.files.internal("battle/intro_frame6.png"));
        Sprite sprite1 = new Sprite(text1);
        for (int i=0; i < 14; i++) {
            this.frames.add(sprite1);
        }
        text1 = new Texture(Gdx.files.internal("battle/intro_frame5.png"));
        sprite1 = new Sprite(text1);
        for (int i=0; i < 8; i++) {
            this.frames.add(sprite1);
        }
        text1 = new Texture(Gdx.files.internal("battle/intro_frame4.png"));
        sprite1 = new Sprite(text1);
        for (int i=0; i < 8; i++) {
            this.frames.add(sprite1);
        }
    }
}




class BattleFadeOutMusic extends Action {
    
    ArrayList<Float> frames;
    Float frame;
    float originalVolume;
    
    
    Music music;
    boolean firstStep;
    boolean isNight;

    public int layer = 129;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};
    
    @Override
    public void step(Game game) {

        if (this.isNight == true) {
            game.actionStack.remove(this);
            PublicFunctions.insertToAS(game, this.nextAction);
            return;
        }
        
        if (this.firstStep == true) {
            this.originalVolume = game.currMusic.getVolume();
            this.music = game.currMusic;
            this.firstStep = false;
        }

        //if done with anim, do nextAction
        if (frames.isEmpty()) {
            PublicFunctions.insertToAS(game, this.nextAction);
            game.actionStack.remove(this);
            this.music.stop();
            this.music.setVolume(this.originalVolume);
            //game.map.music.start();
            //make sure we are out of battle before resetting map music
            if (game.battle.drawAction == null) {
                game.currMusic = game.map.currRoute.music;
                game.currMusic.play();
            }
            return;
        }
        
        //get next frame
        this.frame = frames.get(0);
        
        this.music.setVolume(frame);
        
        frames.remove(0);

    }
    
    public BattleFadeOutMusic(Game game, Action nextAction) {
        
        this.nextAction = nextAction;
        
        this.firstStep = true;

        this.frames = new ArrayList<Float>();
        
        this.isNight = false;
        if (game.map.timeOfDay.equals("Night")) {
            this.isNight = true;
        }
        
        
        //fade out from white anim
        for (int i=0; i < 14; i++) {
            this.frames.add(.3f);
        }
        for (int i=0; i < 14; i++) {
            this.frames.add(.25f);
        }
        for (int i=0; i < 14; i++) {
            this.frames.add(.2f);
        }
        for (int i=0; i < 14; i++) {
            this.frames.add(.15f);
        }
        for (int i=0; i < 14; i++) {
            this.frames.add(.1f);
        }
        for (int i=0; i < 7; i++) {
            this.frames.add(.05f);
        }
        for (int i=0; i < 7; i++) {
            this.frames.add(.025f);
        }
    }
}


//draw battle elements
 //TODO - don't draw the options frame (ie fight, run etc)
 //TODO - on destroy, set drawAction to null?
class DrawBattle extends Action {

    Sprite bgSprite;

    DrawFriendlyHealth drawFriendlyHealthAction;
    DrawEnemyHealth drawEnemyHealthAction;
    Action drawFriendlyPokemonAction;

    public int layer = 130;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};

    Sprite helperSprite;
    
    public static boolean shouldDrawOppPokemon = true;
    
    @Override
    public void step(Game game) {

        
        this.bgSprite.draw(game.floatingBatch);
//        game.floatingBatch.draw(this.bgSprite,
//                                this.bgSprite.getX(),
//                                this.bgSprite.getY());

        //debug
//        this.helperSprite.draw(game.floatingBatch);

        if (this.shouldDrawOppPokemon) {
            game.battle.oppPokemon.sprite.draw(game.floatingBatch);
//            game.floatingBatch.draw(game.battle.oppPokemon.sprite, 
//                                    game.battle.oppPokemon.sprite.getX(),
//                                    game.battle.oppPokemon.sprite.getY());
        }
        game.player.battleSprite.draw(game.floatingBatch);
//        game.floatingBatch.draw(game.player.battleSprite, game.player.battleSprite.getX(), game.player.battleSprite.getY());
        
        //todo - remove
        //this gets assigned at some point. manually stepping here
         //rather than inserting in AS b/c simpler
//        if (this.drawEnemyHealthAction != null) {
//            this.drawEnemyHealthAction.step(game);
//        }
        
        //todo - remove
//        //draw pkmn level bars
//        int tensPlace = game.battle.oppPokemon.level/10;
//        //System.out.println("level: "+String.valueOf(tensPlace));
//        Sprite tensPlaceSprite = game.textDict.get(Character.forDigit(tensPlace,10));
//        game.floatingBatch.draw(tensPlaceSprite, 40, 128);
//
//        int onesPlace = game.battle.oppPokemon.level % 10;
//        Sprite onesPlaceSprite = game.textDict.get(Character.forDigit(onesPlace,10));
//        game.floatingBatch.draw(onesPlaceSprite, 48, 128);
//
//        char[] textArray = game.battle.oppPokemon.name.toUpperCase().toCharArray();
//        Sprite letterSprite;
//        for (int i=0; i < textArray.length; i++) {
//            letterSprite = game.textDict.get(textArray[i]);
//            game.floatingBatch.draw(letterSprite, 8+8*i, 136);
//        }
        
    }
    
    public DrawBattle(Game game) {
        
        Texture text = new Texture(Gdx.files.internal("battle/battle_bg3.png"));
        //Texture text = new Texture(Gdx.files.internal("battle/helper1.png"));
        this.bgSprite = new Sprite(text, 0, 0, 176, 160);
        //this.bgSprite.setPosition(16*10,16*9);
        //this.bgSprite.setScale(3);
        //TODO - make bg the text box.(?)
        
        game.battle.drawAction = this;

//        text = new Texture(Gdx.files.internal("attack_menu/helper4.png"));
//        this.helperSprite = new Sprite(text, 0, 0, 160, 144);
        
        // sprite is bigger than bg, needed for screen shake
        this.bgSprite.setPosition(-8, -8);
        
    }
}


// TODO: should either step into this in DrawBattle, or just
// move this code to DrawBattle. 
//instance of this assigned to DrawBattle.drawEnemyHealthAction
class DrawEnemyHealth extends Action {

    Sprite bgSprite;
    public ArrayList<Sprite> healthBar;
    
    public int layer = 129;
    public int getLayer(){return this.layer;}
    public String getCamera() {return "gui";};
    public static boolean shouldDraw = true;
    
    @Override
    public void step(Game game) {

        if (this.shouldDraw) {
            
            //draw helper sprite
             //probly remove
            this.bgSprite.draw(game.floatingBatch);        
            
            //draw pkmn level bars
            int tensPlace = game.battle.oppPokemon.level/10;
            //System.out.println("level: "+String.valueOf(tensPlace));
            Sprite tensPlaceSprite = game.textDict.get(Character.forDigit(tensPlace,10));
            game.floatingBatch.draw(tensPlaceSprite, 40, 128);
    
            int offset = 0;
            if (game.battle.oppPokemon.level < 10) {
                offset = -8;
            }
            
            int onesPlace = game.battle.oppPokemon.level % 10;
            Sprite onesPlaceSprite = game.textDict.get(Character.forDigit(onesPlace,10));
            game.floatingBatch.draw(onesPlaceSprite, 48+offset, 128);
    
            char[] textArray = game.battle.oppPokemon.name.toUpperCase().toCharArray();
            Sprite letterSprite;
            for (int i=0; i < textArray.length; i++) {
                letterSprite = game.textDict.get(textArray[i]);
                game.floatingBatch.draw(letterSprite, 8+8*i, 136);
            }
            
            //draw health bar
            for (Sprite bar : this.healthBar) {
                bar.draw(game.floatingBatch);
            }
        }
        //detect when battle is over, 
        //object will remove itself from AS
        if (game.battle.drawAction == null) {
            game.actionStack.remove(this);
        }
    }
    
    public DrawEnemyHealth(Game game) {

        Texture text = new Texture(Gdx.files.internal("battle/enemy_healthbar1.png"));
        this.bgSprite = new Sprite(text, 0, 0, 160, 144);

        //this could be dangerous?
        //if drawAction is null. this may happen at some point.
        game.battle.drawAction.drawEnemyHealthAction = this; 
        
        //fill sprite array according to enemy health
         //healthbar is 48 pixels long
         //round up when counting
        this.healthBar = new ArrayList<Sprite>();
        int numElements = (int)Math.ceil( (game.battle.oppPokemon.currentStats.get("hp")*48f) / game.battle.oppPokemon.maxStats.get("hp") );
        
        //System.out.println("numElements: "+String.valueOf(numElements)); //debug
        
        text = new Texture(Gdx.files.internal("battle/health1.png"));
        Sprite temp = new Sprite(text, 0,0,1,2);
        for (int i = 0; i < numElements; i++) {
            Sprite temp2 = new Sprite(temp); //to avoid long loading
            temp2.setPosition(32 + i, 123);
            this.healthBar.add(temp2);
        }
        
    }
}



//TODO: should either step into this in DrawBattle, or just
//move this code to DrawBattle. 
//instance of this assigned to DrawBattle.drawEnemyHealthAction
class DrawFriendlyHealth extends Action {

    Sprite bgSprite;
    Sprite helperSprite;
    public ArrayList<Sprite> healthBar;
    
    public int layer = 129;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};
    boolean firstStep = true;
    public static boolean shouldDraw = true;
    
    @Override
    public void step(Game game) {

        //offset is 96, 80
        if (this.firstStep) {
            game.battle.drawAction.drawFriendlyHealthAction = this; 
            this.firstStep = false;
        }
        
        if (this.nextAction != null) {
            PublicFunctions.insertToAS(game, this.nextAction);
            this.nextAction = null;
        }
        if (this.shouldDraw) {
            
            //draw helper sprite
             //probly remove
            this.bgSprite.draw(game.floatingBatch);        
            
    
    //        this.helperSprite.draw(game.floatingBatch);    //debug
            
            //draw pkmn level bars
            int tensPlace = game.player.currPokemon.level/10;
            //System.out.println("level: "+String.valueOf(tensPlace));
            Sprite tensPlaceSprite = game.textDict.get(Character.forDigit(tensPlace,10));
            game.floatingBatch.draw(tensPlaceSprite, 120, 72);
    
            int offset = 0;
            if (game.player.currPokemon.level < 10) {
                offset = -8;
            }
            
            int onesPlace = game.player.currPokemon.level % 10;
            Sprite onesPlaceSprite = game.textDict.get(Character.forDigit(onesPlace,10));
            game.floatingBatch.draw(onesPlaceSprite, 128+offset, 72);
    
            char[] textArray = game.player.currPokemon.name.toUpperCase().toCharArray();
            Sprite letterSprite;
            offset = 0;
            if (textArray.length > 5) {
                offset = -16;
            }
            else if(textArray.length > 2) {
                offset = -8;
            }
            for (int i=0; i < textArray.length; i++) {
                letterSprite = game.textDict.get(textArray[i]);
                game.floatingBatch.draw(letterSprite, offset+96+8*i, 80);
            }
            
            //draw health bar
            for (Sprite bar : this.healthBar) {
                bar.draw(game.floatingBatch);
            }
        }
        //detect when battle is over, 
        //object will remove itself from AS
        if (game.battle.drawAction == null) {
            game.actionStack.remove(this);
        }
    }
    public DrawFriendlyHealth(Game game) {
        this(game, null);
    }
    
    public DrawFriendlyHealth(Game game, Action nextAction) {
        
        this.nextAction = nextAction;

        Texture text = new Texture(Gdx.files.internal("battle/friendly_healthbar1.png"));
        this.bgSprite = new Sprite(text, 0, 0, 160, 144);

        //this.bgSprite.setPosition(0,4); ;//debug
        

        text = new Texture(Gdx.files.internal("attack_menu/helper3.png"));
        this.helperSprite = new Sprite(text, 0, 0, 160, 144);
        
        //fill sprite array according to enemy health
         //healthbar is 48 pixels long
         //round up when counting
        this.healthBar = new ArrayList<Sprite>();
        int numElements = (int)Math.ceil( (game.player.currPokemon.currentStats.get("hp")*48f) / game.player.currPokemon.maxStats.get("hp") );
        
        //System.out.println("numElements: "+String.valueOf(numElements)); //debug
        
        text = new Texture(Gdx.files.internal("battle/health1.png"));
        Sprite temp = new Sprite(text, 0,0,1,2);
        for (int i = 0; i < numElements; i++) {
            Sprite temp2 = new Sprite(temp); //to avoid long loading
            temp2.setPosition(96 + i, 67);
            this.healthBar.add(temp2);
        }
        
    }
}




//instance of this assigned to DrawBattle.drawEnemyHealthAction
class DepleteEnemyHealth extends Action {

    
    public int layer = 129;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};

    
    boolean firstStep;
    int timer;
    int removeNumber;
    int targetSize; //reduce enemy health bar to this number
    
    
    @Override
    public void step(Game game) {
    
        if (this.firstStep == true) {
            this.targetSize = (int)Math.ceil( (game.battle.oppPokemon.currentStats.get("hp")*48) / game.battle.oppPokemon.maxStats.get("hp"));
            this.firstStep = false;
        }
        
        this.timer++;
        if (this.timer < 4) {
            return;
        }
        this.timer = 0;

        int size = 0;
        for (int i = 0; i < this.removeNumber; i++) {
            size = game.battle.drawAction.drawEnemyHealthAction.healthBar.size();
            if (size <= targetSize) {
                break;
            }
            game.battle.drawAction.drawEnemyHealthAction.healthBar.remove(size-1);
        }
        
        if (this.removeNumber == 2) {
            this.removeNumber = 1;
        }
        else {
            this.removeNumber = 2;
        }
        
        if (size <= targetSize) {

            game.actionStack.remove(this);
            
            //if health is 0, run EnemyFaint
            if (game.battle.oppPokemon.currentStats.get("hp") <= 0) {
                
                //TODO - play victory music here(?)
                PublicFunctions.insertToAS(game, new EnemyFaint(game, 
                                                 new RemoveDisplayText(  // TODO: refactor to stop using this
                                                 new DisplayText.Clear(game,
                                                 new WaitFrames(game, 3,
                                                 new DisplayText(game, "Enemy "+game.battle.oppPokemon.name.toUpperCase()+" fainted!",
                                                                 null, null, 
                                                 new SplitAction(
                                                     new BattleFadeOut(game,
                                                     new DoneAction()), 
                                                 new BattleFadeOutMusic(game, new DoneAction())
                                                 )))))));
            }
            //else, insert nextAction
            else {
                PublicFunctions.insertToAS(game, this.nextAction);
            }
        }
        
    }
    
    public DepleteEnemyHealth(Game game, Action nextAction) {
        
        this.nextAction = nextAction;
        
        this.firstStep = true;
        this.timer = 3;
        this.removeNumber = 2;
        
        
    }
}


//deplete friendly health bar



//instance of this assigned to DrawBattle.drawEnemyHealthAction
class DepleteFriendlyHealth extends Action {

    
    public int layer = 129;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};

    
    Pokemon pokemon;
    
    boolean firstStep;
    int timer;
    int removeNumber;
    int targetSize; //reduce enemy health bar to this number
    
    
    @Override
    public void step(Game game) {
    
        if (this.firstStep == true) {
            this.targetSize = (int)Math.ceil( (this.pokemon.currentStats.get("hp")*48) / this.pokemon.maxStats.get("hp"));
            this.firstStep = false;
        }
        
        this.timer++;
        if (this.timer < 4) {
            return;
        }
        this.timer = 0;
        
        
        int size = 0;
        for (int i = 0; i < this.removeNumber; i++) {
            size = game.battle.drawAction.drawFriendlyHealthAction.healthBar.size();
            if (size <= targetSize) {
                break;
            }
            game.battle.drawAction.drawFriendlyHealthAction.healthBar.remove(size-1);
        }
        
        //alternate between removing 2 and removing 1
        if (this.removeNumber == 2) {
            this.removeNumber = 1;
        }
        else {
            this.removeNumber = 2;
        }
        
        if (size <= targetSize) {

            game.actionStack.remove(this);
            
            //if health is 0, this pokemon should faint
            if (this.pokemon.currentStats.get("hp") <= 0) {


                //TODO - your pokemon should faint

                PublicFunctions.insertToAS(game, new FriendlyFaint(game,  
                                                 new RemoveDisplayText(
                                                 new WaitFrames(game, 3, 
                                                 new DisplayText(game, ""+this.pokemon.name.toUpperCase()+" fainted!",
                                                                 null, null, 
                                                 //decide whether to switch pkmn, send out player, or end game
                                                 new AfterFriendlyFaint()
                                                 )))));
            }
            //else, insert nextAction
            else {
                PublicFunctions.insertToAS(game, this.nextAction);
            }
        }
        
    }
    
    public DepleteFriendlyHealth(Pokemon friendlyPokemon, Action nextAction) {
        
        this.pokemon = friendlyPokemon;
        
        this.nextAction = nextAction;
        
        this.firstStep = true;
        this.timer = 3;
        this.removeNumber = 2;
        
        
    }
}


class AfterFriendlyFaint extends Action {

    
    public int layer = 129;
    public int getLayer(){return this.layer;}

    
    
    

    @Override
    public void step(Game game) {
    
        //for now, insert two text actions
        
        PublicFunctions.insertToAS(game, new DisplayText(game, ""+game.player.name.toUpperCase()+" is out of useable POKÈMON!", null, null, 
                                          new DisplayText(game, ""+game.player.name.toUpperCase()+" whited out!", null, null, 
                                         new SplitAction(
                                             new BattleFadeOut(game,
                                             new DoneWithDemo(game)),
//                                         new BattleFadeOutMusic(game,  // TODO: re-enable
                                         new DoneAction()
                                         ))));
        
        game.actionStack.remove(this);
    }
    

    public AfterFriendlyFaint() {
        
    }
    
}



//draws rock flying through air and hitting opposing pkmn
class ThrowRock extends Action {

    //remove
    Sprite hitSprite;
    Sprite rockSprite;
    
    ArrayList<Vector2> positions;
    Vector2 position;
    ArrayList<Sprite> sprites;
    Sprite sprite;
    ArrayList<Integer> frames;
    String sound;
    ArrayList<String> sounds;

    public int layer = 120;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};

    
    
    Sprite helperSprite; //just for helping me position the animation. delete later.
    
    @Override
    public void step(Game game) {


        //set sprite position
        //if done with anim, do nextAction
        if (positions.isEmpty() || sprites.isEmpty()) {
            //modify opp pokemon's 'angry' level
            //"every time a rock is thrown, the catch rate C is doubled" - http://www.dragonflycave.com/safarizone.aspx
            int currCatchRate = game.battle.oppPokemon.currentStats.get("catchRate");
            game.battle.oppPokemon.currentStats.put("catchRate", currCatchRate*2);
            if (game.battle.oppPokemon.currentStats.get("catchRate") > 255) { //cap at 255
                game.battle.oppPokemon.currentStats.put("catchRate", 255);
            }
            
            //random between 1 and 5, add to angry counter
            int randomNum = game.map.rand.nextInt(5)+1;
            game.battle.oppPokemon.angry += randomNum;
            if (game.battle.oppPokemon.angry > 255) { //cap at 255
                game.battle.oppPokemon.angry = 255;
            }
            
            //set eating counter to 0 
            game.battle.oppPokemon.eating = 0;
            
            //debug
            System.out.println("angry counter: "+String.valueOf(game.battle.oppPokemon.angry));
            System.out.println("Catch Rate: "+String.valueOf(game.battle.oppPokemon.currentStats.get("catchRate")));
            
            //wait 3 frames before menu draw
             //allows text box to be blank for 3 frames (drawText is already blank 1 frame by default)
            PublicFunctions.insertToAS(game, new WaitFrames(game, 2, this.nextAction));
            game.actionStack.remove(this);
            return;
        }

        //get next sound, play it
        this.sound = this.sounds.get(0);
        if (this.sound != null) {
            PublicFunctions.insertToAS(game, new PlaySound(this.sound, new DoneAction()));
            this.sounds.set(0, null); //don't play same sound over again
        }
        
        //get next frame
        this.sprite = sprites.get(0);
        
        //debug
        //this.helperSprite.draw(game.floatingBatch); //debug
        
        if (this.sprite != null) {
            //this.sprite.setScale(3); //post scaling change
            this.sprite.setPosition(position.x, position.y);
            this.sprite.draw(game.floatingBatch);
        }
        
//        if (this.frames.size() == 1) { //debug
//            return;
//        }
        
        //repeat sprite/pos for current object for 'frames[0]' number of frames.
        if (this.frames.get(0) > 0) {
            this.frames.set(0, this.frames.get(0) - 1);
        }
        else {
            //since position is relative, only update once each time period
            this.position = this.position.add(positions.get(0));
            positions.remove(0);
            sprites.remove(0);
            frames.remove(0);
            sounds.remove(0);
        }
        
        
        
    }
    
    public ThrowRock(Game game, Action nextAction) {

        this.nextAction = nextAction;
        
        Texture text = new Texture(Gdx.files.internal("throw_rock_anim/hit1.png"));
        this.hitSprite = new Sprite(text, 0, 0, 24, 24);
        
        text = new Texture(Gdx.files.internal("throw_rock_anim/rock_small1.png"));
        this.rockSprite = new Sprite(text, 0, 0, 8, 8);
        
        //positions is added to position every so often
        this.positions = new ArrayList<Vector2>();
        this.position = new Vector2(32,64);
        this.positions.add(new Vector2(8,12));
        this.positions.add(new Vector2(8,12));
        this.positions.add(new Vector2(8,8));
        this.positions.add(new Vector2(8,8));
        this.positions.add(new Vector2(8,8));
        this.positions.add(new Vector2(8,8));
        this.positions.add(new Vector2(8,2));
        this.positions.add(new Vector2(8,-2));
        this.positions.add(new Vector2(8,-9));
        this.positions.add(new Vector2(8,-9));
        this.positions.add(new Vector2(-4,-12));
        this.positions.add(new Vector2(0,0)); //72 frames of nothing
        this.positions.add(new Vector2(0,0)); //need dummy pos
        //13 events total
        
        this.sprites =  new ArrayList<Sprite>();
        for (int i = 0; i < 11; i++) {
            this.sprites.add(this.rockSprite);
        }
        this.sprites.add(this.hitSprite);
        this.sprites.add(null);
        //13 events total

        this.frames = new ArrayList<Integer>();
        for (int i = 0; i < 12; i++) {
            this.frames.add(3);
        }
        this.frames.add(72-1); //72 frames of nothing at end
        //13 events total

        text = new Texture(Gdx.files.internal("throw_rock_anim/helper12.png"));
        this.helperSprite = new Sprite(text, 0, 0, 160, 144);
        //this.helperSprite.setPosition(16*10,16*9); //post scaling change
        //this.helperSprite.setScale(3); //post scaling change
        
        
        //PublicFunctions.insertToAS(game, new PlaySound("throw_rock1", new DoneAction()));
        //sounds to play
        this.sounds = new ArrayList<String>();
        this.sounds.add("throw_rock1");
        for (int i = 0; i < 12; i++) { 
            this.sounds.add(null);
        }
        //13 events total
        
    }
}


//draws rock flying through air and hitting opposing pkmn
class ThrowBait extends Action {

    //remove
    Sprite baitSprite;
    
    ArrayList<Vector2> positions;
    Vector2 position;
    ArrayList<Sprite> sprites;
    Sprite sprite;
    ArrayList<Integer> frames;
    String sound;
    ArrayList<String> sounds;

    public int layer = 120;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};

    
    
    Sprite helperSprite; //just for helping me position the animation. delete later.
    
    @Override
    public void step(Game game) {


        //set sprite position
        //if done with anim, do nextAction
        if (positions.isEmpty() || sprites.isEmpty()) {
            //modify opp pokemon's 'angry' level
            //"every time a rock is thrown, the catch rate C is doubled" - http://www.dragonflycave.com/safarizone.aspx
            int currCatchRate = game.battle.oppPokemon.currentStats.get("catchRate");
            game.battle.oppPokemon.currentStats.put("catchRate", currCatchRate/2);
            
            //random between 1 and 5, add to eating counter
            int randomNum = game.map.rand.nextInt(5)+1;
            game.battle.oppPokemon.eating += randomNum;
            if (game.battle.oppPokemon.eating > 255) { //cap at 255
                game.battle.oppPokemon.eating = 255;
            }
            
            //set angry counter to 0 
            game.battle.oppPokemon.angry = 0;
            
            //debug
            System.out.println("eating counter: "+String.valueOf(game.battle.oppPokemon.eating));
            System.out.println("Catch Rate: "+String.valueOf(game.battle.oppPokemon.currentStats.get("catchRate")));
            
            //wait 3 frames before menu draw
             //allows text box to be blank for 3 frames (drawText is already blank 1 frame by default)
            PublicFunctions.insertToAS(game, new WaitFrames(game, 2, this.nextAction));
            game.actionStack.remove(this);
            return;
        }

        //get next sound, play it
        this.sound = this.sounds.get(0);
        if (this.sound != null) {
            PublicFunctions.insertToAS(game, new PlaySound(this.sound, new DoneAction()));
            this.sounds.set(0, null); //don't play same sound over again
        }
        
        //get next frame
        this.sprite = sprites.get(0);
        
        //debug
        //this.helperSprite.draw(game.floatingBatch); //debug
        
        if (this.sprite != null) {
            //this.sprite.setScale(3); //post scaling change
            this.sprite.setPosition(position.x, position.y);
            this.sprite.draw(game.floatingBatch);
        }
        
//        if (this.frames.size() == 1) { //debug
//            return;
//        }
        
        //repeat sprite/pos for current object for 'frames[0]' number of frames.
        if (this.frames.get(0) > 0) {
            this.frames.set(0, this.frames.get(0) - 1);
        }
        else {
            //since position is relative, only update once each time period
            this.position = this.position.add(positions.get(0));
            positions.remove(0);
            sprites.remove(0);
            frames.remove(0);
            sounds.remove(0);
        }
        
        
        
    }
    
    public ThrowBait(Game game, Action nextAction) {

        this.nextAction = nextAction;
                
        Texture text = new Texture(Gdx.files.internal("throw_rock_anim/bait_small1.png"));
        this.baitSprite = new Sprite(text, 0, 0, 8, 8);
        
        //positions is added to position every so often
        this.positions = new ArrayList<Vector2>();
        this.position = new Vector2(32,64);
        this.positions.add(new Vector2(8,12));
        this.positions.add(new Vector2(8,12));
        this.positions.add(new Vector2(8,8));
        this.positions.add(new Vector2(8,8));
        this.positions.add(new Vector2(8,8));
        this.positions.add(new Vector2(8,8));
        this.positions.add(new Vector2(8,2));
        this.positions.add(new Vector2(8,-2));
        this.positions.add(new Vector2(8,-9));
        this.positions.add(new Vector2(8,-9));
        this.positions.add(new Vector2(0,0)); //72 frames of nothing
        this.positions.add(new Vector2(0,0)); //need dummy pos
        //12 events total
        
        this.sprites =  new ArrayList<Sprite>();
        for (int i = 0; i < 11; i++) {
            this.sprites.add(this.baitSprite);
        }
        this.sprites.add(null);
        //12 events total

        this.frames = new ArrayList<Integer>();
        for (int i = 0; i < 11; i++) {
            this.frames.add(3);
        }
        this.frames.add(73-1); //73 frames of nothing at end
        //12 events total

        //text = new Texture(Gdx.files.internal("throw_rock_anim/helper12.png"));
        //this.helperSprite = new Sprite(text, 0, 0, 160, 144);
        //this.helperSprite.setPosition(16*10,16*9); //post scaling change
        //this.helperSprite.setScale(3); //post scaling change
        
        
        //PublicFunctions.insertToAS(game, new PlaySound("throw_rock1", new DoneAction()));
        //sounds to play
        this.sounds = new ArrayList<String>();
        this.sounds.add("throw_rock1");
        for (int i = 0; i < 11; i++) { 
            this.sounds.add(null);
        }
        //12 events total
        
    }
}




//draws pokeball flying through air and hitting opposing pkmn
 //whether to catch or not is decided at end of this action
 //pass nextAction to the new action (probably a draw menu action, for example DrawBattleMenu_SafariZone)
class ThrowPokeball extends Action {

    Sprite pokeballSprite;
    
    ArrayList<Vector2> positions;
    Vector2 position;
    ArrayList<Sprite> sprites;
    Sprite sprite;
    ArrayList<Integer> repeats;
    String sound;
    ArrayList<String> sounds;
    
    public int layer = 120;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};

    
    
    Sprite helperSprite; //just for helping me position the animation. delete later.
    
    @Override
    public void step(Game game) {

        //set sprite position
        //if done with anim, do nextAction
        if (positions.isEmpty() || sprites.isEmpty()) {
            //note - it's possible that calling Action could take care of the below instead
             //nextAction is likely a menu draw Action, ie safariMenuDraw
            //PublicFunctions.insertToAS(game, new catchPokemon_wigglesThenCatch(game, this.nextAction)); //before passed from menu
            //PublicFunctions.insertToAS(game, new catchPkmn_oneWiggle(this.nextAction)); //later
            PublicFunctions.insertToAS(game, this.nextAction);
            game.actionStack.remove(this);
            return;
        }
        
        //get next sound, play it
        this.sound = this.sounds.get(0);
        if (this.sound != null) {
            PublicFunctions.insertToAS(game, new PlaySound(this.sound, new DoneAction()));
            this.sounds.set(0, null); //don't play same sound over again
        }
        
        
        //get next frame
        this.sprite = sprites.get(0);
        
        //debug
        //this.helperSprite.draw(game.floatingBatch); 
        
        //draw current sprite
        if (this.sprite != null) {
            //this.sprite.setScale(3);  //post scaling change
            this.sprite.setPosition(position.x, position.y);
            this.sprite.draw(game.floatingBatch);            
        }
        
        //debug
//        if (this.repeats.size() == 5) { 
//            return;
//        }
        
        //repeat sprite/pos for current object for 'frames[0]' number of frames.
        if (this.repeats.get(0) > 0) {
            this.repeats.set(0, this.repeats.get(0) - 1);
        }
        else {
            //since position is relative, only update once each time period
            this.position = this.position.add(positions.get(0));
            positions.remove(0);
            sprites.remove(0);
            repeats.remove(0);
            sounds.remove(0);
        }
    }
    
    public ThrowPokeball(Game game, Action nextAction) {

        this.nextAction = nextAction;

        Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball1.png"));
        this.pokeballSprite = new Sprite(text, 0, 0, 12, 12);
        
        //consider doing relative positions from now on
        //this.position = new Vector2(104+4*3-2,200-6*3-2); //post scaling change
        this.position = new Vector2(34,56);
        
        this.positions = new ArrayList<Vector2>();
        this.positions.add(new Vector2(8,8));
        this.positions.add(new Vector2(8,8));
        this.positions.add(new Vector2(8,8));
        this.positions.add(new Vector2(8,8));
        this.positions.add(new Vector2(8,4));
        this.positions.add(new Vector2(8,4));
        this.positions.add(new Vector2(8,1));
        this.positions.add(new Vector2(8,-1));
        this.positions.add(new Vector2(8,-1));
        this.positions.add(new Vector2(8,-1));
        this.positions.add(new Vector2(0,0));// 12 accessed - done with throw at this point (blank screen)
        this.positions.add(new Vector2(-6*3,-10*3+8));//first of poof anim
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        //this.positions.add(new Vector2(0,0)); //wait frames before pokeball appears  //handled in next action now //delete
        this.positions.add(new Vector2(0,0)); //last is always dummy pos 
        
        this.sprites =  new ArrayList<Sprite>();
        for (int i = 0; i < 11; i++) {
            this.sprites.add(this.pokeballSprite);
        }
        this.sprites.add(null); //draw nothing for 10 frames
        
        //'poof' animation 
        text = new Texture(Gdx.files.internal("throw_pokeball_anim/poof_sheet1.png"));
        this.sprites.add(new Sprite(text, 48*0, 0, 48, 48)); 
        this.sprites.add(new Sprite(text, 48*1, 0, 48, 48)); 
        this.sprites.add(new Sprite(text, 48*2, 0, 48, 48)); 
        this.sprites.add(new Sprite(text, 48*3, 0, 48, 48)); 
        this.sprites.add(new Sprite(text, 48*4, 0, 48, 48)); 

        //this.sprites.add(null); //draw nothing for 13 frames //handled in next action now //delete

        
        this.repeats = new ArrayList<Integer>();
        for (int i = 0; i < 11; i++) {
            this.repeats.add(2);
        }
        this.repeats.add(10-1); //wait 10 frames
        
        for (int i = 0; i < 4; i++) { //4 poof frames
            this.repeats.add(4);
        }
        this.repeats.add(10-1); //wait 10 frames for last poof frame
        
        //this.repeats.add(13-1); //13 frames before pokeball appears on ground //handled in next action now //delete
        
        
        //sounds to play
        this.sounds = new ArrayList<String>();
        this.sounds.add("throw_pokeball1");
        for (int i = 0; i < 11; i++) { 
            this.sounds.add(null);
        }
        this.sounds.add("poof1");
        for (int i = 0; i < 11; i++) { 
            this.sounds.add(null);
        }
        
        text = new Texture(Gdx.files.internal("throw_pokeball_anim/helper13.png"));
        this.helperSprite = new Sprite(text, 0, 0, 160, 144);
        //this.helperSprite.setPosition(16*10,16*9); //post scaling change
        //this.helperSprite.setScale(3); //post scaling change

        //play 'throw pokeball' sound
        //PublicFunctions.insertToAS(game, new PlaySound("throw_pokeball1", new DoneAction()));
        
        
    }
}


//demo code
class ThrowFastPokeball extends Action {

    Sprite pokeballSprite;
    
    ArrayList<Vector2> positions;
    Vector2 position;
    ArrayList<Sprite> sprites;
    Sprite sprite;
    ArrayList<Integer> repeats;
    String sound;
    ArrayList<String> sounds;
    
    public int layer = 120;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};

    
    
    Sprite helperSprite; //just for helping me position the animation. delete later.
    
    @Override
    public void step(Game game) {

        //set sprite position
        //if done with anim, do nextAction
        if (positions.isEmpty() || sprites.isEmpty()) {
            //note - it's possible that calling Action could take care of the below instead
             //nextAction is likely a menu draw Action, ie safariMenuDraw
            //PublicFunctions.insertToAS(game, new catchPokemon_wigglesThenCatch(game, this.nextAction)); //before passed from menu
            //PublicFunctions.insertToAS(game, new catchPkmn_oneWiggle(this.nextAction)); //later
            PublicFunctions.insertToAS(game, this.nextAction);
            game.actionStack.remove(this);
            return;
        }
        
        //get next sound, play it
        this.sound = this.sounds.get(0);
        if (this.sound != null) {
            PublicFunctions.insertToAS(game, new PlaySound(this.sound, new DoneAction()));
            this.sounds.set(0, null); //don't play same sound over again
        }
        
        
        //get next frame
        this.sprite = sprites.get(0);
        
        //debug
        //this.helperSprite.draw(game.floatingBatch); 
        
        //draw current sprite
        if (this.sprite != null) {
            //this.sprite.setScale(3);  //post scaling change
            this.sprite.setPosition(position.x, position.y);
            this.sprite.draw(game.floatingBatch);            
        }
        
        //debug
//        if (this.repeats.size() == 16) { 
//            return;
//        }
        
        //repeat sprite/pos for current object for 'frames[0]' number of frames.
        if (this.repeats.get(0) > 0) {
            this.repeats.set(0, this.repeats.get(0) - 1);
        }
        else {
            //since position is relative, only update once each time period
            this.position = this.position.add(positions.get(0));
            positions.remove(0);
            sprites.remove(0);
            repeats.remove(0);
            sounds.remove(0);
        }
    }
    
    public ThrowFastPokeball(Game game, Action nextAction) {

        this.nextAction = nextAction;

        Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball1.png"));
        this.pokeballSprite = new Sprite(text, 0, 0, 12, 12);
        
        //consider doing relative positions from now on
        //this.position = new Vector2(104+4*3-2,200-6*3-2); //post scaling change
        this.position = new Vector2(34-16,56-16);
        
        this.positions = new ArrayList<Vector2>();
        //harden frames
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0+16,0+16));//move to pokeball first spot
        
        this.positions.add(new Vector2(8,8));
        this.positions.add(new Vector2(8,8));
        this.positions.add(new Vector2(8,8));
        this.positions.add(new Vector2(8,8));
        this.positions.add(new Vector2(8,4));
        this.positions.add(new Vector2(8,4));
        this.positions.add(new Vector2(8,1));
        this.positions.add(new Vector2(8,-1));
        this.positions.add(new Vector2(8,-1));
        this.positions.add(new Vector2(8,-1));
        this.positions.add(new Vector2(0,0));// 12 accessed - done with throw at this point (blank screen)
        this.positions.add(new Vector2(-6*3,-10*3+8));//first of poof anim
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        //this.positions.add(new Vector2(0,0)); //wait frames before pokeball appears  //handled in next action now //delete
        this.positions.add(new Vector2(0,0)); //last is always dummy pos 
        //20 events total
        
        this.sprites =  new ArrayList<Sprite>();
        text = new Texture(Gdx.files.internal("attacks/harden_sheet1.png"));
        this.sprites.add(null); //draw nothing for 7 frames
        this.sprites.add(new Sprite(text, 56*0, 0, 56, 56));
        this.sprites.add(new Sprite(text, 56*1, 0, 56, 56));
        this.sprites.add(new Sprite(text, 56*2, 0, 56, 56));
        
        for (int i = 0; i < 11; i++) {
            this.sprites.add(this.pokeballSprite);
        }
        this.sprites.add(null); //draw nothing for 10 frames
        
        //'poof' animation 
        text = new Texture(Gdx.files.internal("throw_pokeball_anim/poof_sheet1.png"));
        this.sprites.add(new Sprite(text, 48*0, 0, 48, 48)); 
        this.sprites.add(new Sprite(text, 48*1, 0, 48, 48)); 
        this.sprites.add(new Sprite(text, 48*2, 0, 48, 48)); 
        this.sprites.add(new Sprite(text, 48*3, 0, 48, 48)); 
        this.sprites.add(new Sprite(text, 48*4, 0, 48, 48)); 
        //21 events total

        //this.sprites.add(null); //draw nothing for 13 frames //handled in next action now //delete

        
        this.repeats = new ArrayList<Integer>();
        this.repeats.add(7); //
        this.repeats.add(7); //
        this.repeats.add(7); //
        this.repeats.add(7); //
        for (int i = 0; i < 11; i++) {
            this.repeats.add(1);
        }
        this.repeats.add(10-1); //wait 10 frames
        
        for (int i = 0; i < 4; i++) { //4 poof frames
            this.repeats.add(4);
        }
        this.repeats.add(10-1); //wait 10 frames for last poof frame
        //21 events total
        
        //this.repeats.add(13-1); //13 frames before pokeball appears on ground //handled in next action now //delete
        
        
        //sounds to play
        this.sounds = new ArrayList<String>();
        this.sounds.add("harden1");
        this.sounds.add(null);
        this.sounds.add(null);
        this.sounds.add(null);
        this.sounds.add("throw_pokeball1");
        for (int i = 0; i < 11; i++) { 
            this.sounds.add(null);
        }
        this.sounds.add("poof1");
        for (int i = 0; i < 11; i++) { 
            this.sounds.add(null);
        }
        //
        
        text = new Texture(Gdx.files.internal("throw_pokeball_anim/helper13.png"));
        this.helperSprite = new Sprite(text, 0, 0, 160, 144);
        //this.helperSprite.setPosition(16*10,16*9); //post scaling change
        //this.helperSprite.setScale(3); //post scaling change

        //play 'throw pokeball' sound
        //PublicFunctions.insertToAS(game, new PlaySound("throw_pokeball1", new DoneAction()));
        
        
    }
}


//demo code
 //pokeball throw that looks like hyperbeam
 //used when adr > 10 atm
class ThrowHyperPokeball extends Action {
    
    ArrayList<Vector2> positions;
    Vector2 position;
    ArrayList<Sprite> sprites;
    Sprite sprite;
    ArrayList<Integer> repeats;
    String sound;
    ArrayList<String> sounds;
    
    public int layer = 120;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};

    
    
    Sprite helperSprite; //just for helping me position the animation. delete later.
    
    @Override
    public void step(Game game) {

        //set sprite position
        //if done with anim, do nextAction
        if (positions.isEmpty() || sprites.isEmpty()) {
            //note - it's possible that calling Action could take care of the below instead
             //nextAction is likely a menu draw Action, ie safariMenuDraw
            //PublicFunctions.insertToAS(game, new catchPokemon_wigglesThenCatch(game, this.nextAction)); //before passed from menu
            //PublicFunctions.insertToAS(game, new catchPkmn_oneWiggle(this.nextAction)); //later
            PublicFunctions.insertToAS(game, this.nextAction);
            game.actionStack.remove(this);
            return;
        }
        
        //get next sound, play it
        this.sound = this.sounds.get(0);
        if (this.sound != null) {
            PublicFunctions.insertToAS(game, new PlaySound(this.sound, new DoneAction()));
            this.sounds.set(0, null); //don't play same sound over again
        }
        
        
        //get next frame
        this.sprite = sprites.get(0);
        
        //debug
//        this.helperSprite.draw(game.floatingBatch); 
        
        //draw current sprite
        if (this.sprite != null) {
            //this.sprite.setScale(3);  //post scaling change
            this.sprite.setPosition(position.x, position.y);
            this.sprite.draw(game.floatingBatch);            
        }
        
        //debug
//        if (this.repeats.size() == 5) { 
//            return;
//        }
        
        //repeat sprite/pos for current object for 'frames[0]' number of frames.
        if (this.repeats.get(0) > 0) {
            this.repeats.set(0, this.repeats.get(0) - 1);
        }
        else {
            //since position is relative, only update once each time period
            this.position = this.position.add(positions.get(0));
            positions.remove(0);
            sprites.remove(0);
            repeats.remove(0);
            sounds.remove(0);
        }
    }
    
    public ThrowHyperPokeball(Game game, Action nextAction) {

        this.nextAction = nextAction;

        
        //consider doing relative positions from now on
        //this.position = new Vector2(104+4*3-2,200-6*3-2); //post scaling change
        this.position = new Vector2(50,72); //
        
        this.positions = new ArrayList<Vector2>();
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0+38+8,0)); //poof1
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        //this.positions.add(new Vector2(0,0)); //last is always dummy pos 
        //13 total events

        Texture text = new Texture(Gdx.files.internal("hyper_beam_anim/hyperbeam_sheet2.png"));
        
        this.sprites =  new ArrayList<Sprite>();
        this.sprites.add(null); //draw nothing for 14 frames
        for (int i = 0; i < 6; i++) {
            this.sprites.add(new Sprite(text, i*72, 0, 72, 40));
        }
        this.sprites.add(null); //draw nothing for 10 frames
        
        //'poof' animation 
        //TODO - actually missing beam frames
        text = new Texture(Gdx.files.internal("throw_pokeball_anim/poof_sheet1.png"));
        this.sprites.add(new Sprite(text, 48*0, 0, 48, 48)); 
        this.sprites.add(new Sprite(text, 48*1, 0, 48, 48)); 
        this.sprites.add(new Sprite(text, 48*2, 0, 48, 48)); 
        this.sprites.add(new Sprite(text, 48*3, 0, 48, 48)); 
        this.sprites.add(new Sprite(text, 48*4, 0, 48, 48)); 

        //13 total events


        
        this.repeats = new ArrayList<Integer>();
        this.repeats.add(14-1); //wait 14 frames
        for (int i = 0; i < 3; i++) {
            this.repeats.add(4); //6
            this.repeats.add(3); //2
        }
        this.repeats.add(3-1); //wait 10 frames
        
        for (int i = 0; i < 4; i++) { //4 poof frames
            this.repeats.add(4);
        }
        this.repeats.add(10-1); //wait 10 frames for last poof frame
        //13 total events
        
        
        //sounds to play
        this.sounds = new ArrayList<String>();
        this.sounds.add("hyperbeam1");
        for (int i = 0; i < 7; i++) { 
            this.sounds.add(null);
        }
        this.sounds.add("poof1");
        for (int i = 0; i < 4; i++) { 
            this.sounds.add(null);
        }
        //13 total events
        
        //text = new Texture(Gdx.files.internal("hyper_beam_anim/helper1.png"));
        text = new Texture(Gdx.files.internal("throw_pokeball_anim/helper13.png"));
        this.helperSprite = new Sprite(text, 0, 0, 160, 144);

        //play 'throw pokeball' sound
        //PublicFunctions.insertToAS(game, new PlaySound("throw_pokeball1", new DoneAction()));
        
        
    }
}


//draws pokeball wiggling once then releasing pokemon
 //note - maybe this should be a chain of actions? depending on how probabilities work?
class catchPokemon_wigglesThenCatch extends Action {

    //Sprite pokeballSprite;
    
    ArrayList<Vector2> positions;
    Vector2 position;
    ArrayList<Sprite> sprites;
    Sprite sprite;
    ArrayList<Integer> repeats;
    String sound;
    ArrayList<String> sounds;
    
    public int layer = 120;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};

    
    
    Sprite helperSprite; //just for helping me position the animation. delete later.
    
    @Override
    public void step(Game game) {

        //set opp pokemon sprite alpha
        game.battle.oppPokemon.sprite.setAlpha(0);
        
        //set sprite position
        //if done with anim, do nextAction
        if (positions.isEmpty() || sprites.isEmpty()) {

            

            //demo code - add to player adrenaline
            int adrenaline = (int)Math.ceil((256 - game.battle.oppPokemon.baseStats.get("catchRate"))/100.0f);
            game.player.adrenaline += adrenaline;
            System.out.println("adrenaline: " + String.valueOf(adrenaline));
            
            //
            
            //PokemonCaught_Events - sprite and text
            //new DisplayText(game, string)
            Action newAction = new PokemonCaught_Events(
                                    game, adrenaline, new SplitAction( //demo code
                                        new BattleFadeOut(game, 
                                                new DoneAction() //new playerStanding(game)
                                        ),
                                        new BattleFadeOutMusic(game, new DoneAction())
                                    )
                                );
                                        
            PublicFunctions.insertToAS(game, newAction);
            newAction.step(game);//need to draw the pokeball
            
            //PublicFunctions.insertToAS(game, new BattleFadeOut(game, new playerStanding(game)));
            //PublicFunctions.insertToAS(game, new BattleFadeOutMusic(game, new DoneAction()));
            
            //since pkmn was caught, add to players pokemon
            game.player.pokemon.add(game.battle.oppPokemon);
            //remove this pokemon from the map
            game.map.currRoute.pokemon.remove(game.battle.oppPokemon);
            //TODO - bug if all pokemon removed from route
             //is this a good way to handle it?
            //game.map.currRoute.pokemon.add(new Pokemon(game.battle.oppPokemon.name, game.battle.oppPokemon.level));

            //demo code - add random pkmn with less or equal catch rate from route.
            /* TODO - remove this
            String pokemonName = null;
            while (pokemonName == null) { //should eventually resolve, b/c can always at least find same pkmn
                int index = game.map.rand.nextInt(game.map.currRoute.allowedPokemon.size());
                Pokemon temp = new Pokemon(game.map.currRoute.allowedPokemon.get(index), 1);
                if ( temp.baseStats.get("catchRate") <= game.battle.oppPokemon.baseStats.get("catchRate")) {
                    pokemonName = game.map.currRoute.allowedPokemon.get(index);
                }
            }
            //TODO -level?
            game.map.currRoute.pokemon.add(new Pokemon(pokemonName, game.battle.oppPokemon.level));
            System.out.println("New pkmn: " + pokemonName);
            */
            //brings pokemon count back up to max
             
            game.map.currRoute.genPokemon(game.battle.oppPokemon.baseStats.get("catchRate"));
            

            
            game.actionStack.remove(this);
            return;
        }
        
        //get next sound, play it
        this.sound = this.sounds.get(0);
        if (this.sound != null) {
            PublicFunctions.insertToAS(game, new PlaySound(this.sound, new DoneAction()));
            this.sounds.set(0, null); //don't play same sound over again
        }
        
        
        //get next frame
        this.sprite = sprites.get(0);
        
        //remove later
        //this.helperSprite.draw(game.floatingBatch); //debug
        
        //draw current sprite
        if (this.sprite != null) {
            //this.sprite.setScale(3); //post scaling change
            this.sprite.setPosition(position.x, position.y);
            this.sprite.draw(game.floatingBatch);            
        }
        
//        if (this.repeats.size() == 18) { //debug 
//            return;
//        }
        
        //repeat sprite/pos for current object for 'frames[0]' number of frames.
        if (this.repeats.get(0) > 0) {
            this.repeats.set(0, this.repeats.get(0) - 1);
        }
        else {
            //since position is relative, only update once each time period
            this.position = this.position.add(positions.get(0));
            positions.remove(0);
            sprites.remove(0);
            repeats.remove(0);
            sounds.remove(0);
        }
    }
    
    public catchPokemon_wigglesThenCatch(Game game, Action nextAction) {

        this.nextAction = nextAction;
        
        Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball1.png"));
        
        //initial sprite position
        this.position = new Vector2(114,88); //post scaling change
        
        this.positions = new ArrayList<Vector2>();
        this.positions.add(new Vector2(0,0)); //wait 13
        for (int i = 0; i < 3; i++) {
            this.positions.add(new Vector2(-1,0));
            this.positions.add(new Vector2(1,0));
            this.positions.add(new Vector2(1,0));
            this.positions.add(new Vector2(-1,0));
        }
        //13 total
        
        //wiggle anim
        this.sprites =  new ArrayList<Sprite>();
        this.sprites.add(null); //draw nothing for 13 frames
        
        text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball_wiggleSheet1.png"));
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
        this.sprites.add(new Sprite(text, 12*1, 0, 12, 12)); //left tilt
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
        this.sprites.add(new Sprite(text, 12*2, 0, 12, 12)); //right tilt
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
        this.sprites.add(new Sprite(text, 12*1, 0, 12, 12)); //left tilt
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
        this.sprites.add(new Sprite(text, 12*2, 0, 12, 12)); //right tilt
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
        this.sprites.add(new Sprite(text, 12*1, 0, 12, 12)); //left tilt
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
        this.sprites.add(new Sprite(text, 12*2, 0, 12, 12)); //right tilt //done at this point //13 total events

        
        this.repeats = new ArrayList<Integer>();
        
        this.repeats.add(13-1); //13 frames before pokeball appears on ground

        this.repeats.add(44-1); //first middle sprite has 44 frames
        for (int i = 0; i < 3; i++) { //left-middle-right tilt all have 4 frames
            this.repeats.add(4-1);
        }
        this.repeats.add(44-1); //first middle sprite has 44 frames
        for (int i = 0; i < 3; i++) { //left-middle-right tilt all have 4 frames
            this.repeats.add(4-1);
        }
        this.repeats.add(44-1); //first middle sprite has 44 frames
        for (int i = 0; i < 2; i++) { //left-middle tilt both have 4 frames
            this.repeats.add(4-1);
        }
        this.repeats.add(6-1); //final right tilt has 6 frames
        //13 total
        
        //sounds to play
        this.sounds = new ArrayList<String>();
        //this.sounds.add(null); //wait 13 no sound
        this.sounds.add("pokeball_wiggle1"); //wiggle as ball appears (doing as white appears, so it's uniform)
        this.sounds.add(null);
        this.sounds.add("pokeball_wiggle1");
        for (int i = 0; i < 3; i++) {
            this.sounds.add(null); 
        }
        this.sounds.add("pokeball_wiggle1");
        for (int i = 0; i < 6; i++) {
            this.sounds.add(null); 
        }
        //this.sounds.add("pokeball_wiggle1");
//        for (int i = 0; i < 3; i++) {
//            this.sounds.add(null); 
//        }
        
        text = new Texture(Gdx.files.internal("throw_pokeball_anim/helper18.png"));
        this.helperSprite = new Sprite(text, 0, 0, 160, 144);
        //this.helperSprite.setPosition(16*10,16*9); //post scaling change
        //this.helperSprite.setScale(3); //post scaling change
        
    }
}



class catchPokemon_miss extends Action {

    ArrayList<Float> alphas;
    
    public int layer = 120;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};

    
    
    Sprite helperSprite; 
    
    @Override
    public void step(Game game) {

        // - 1 frame then text dissappears
        // - 3 frames nothing, then text appears
        
        String textString = "You missed the POKÈMON!"; 
        PublicFunctions.insertToAS(game, new WaitFrames(game, 3,
                                                new DisplayText(game, textString, null, null, this.nextAction)
                                            )
                                        );
        
        game.actionStack.remove(this);
        return;
        
    }
    
    public catchPokemon_miss(Game game, Action nextAction) {

        this.nextAction = nextAction;
        /*
        this.alphas = new ArrayList<Float>();
        for (int i = 0; i < 3; i++) {
            this.alphas.add(1f); 
        }
        //3 total events
        */
        Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/helper13.png"));
        this.helperSprite = new Sprite(text, 0, 0, 160, 144);
        
    }
}


class catchPokemon_wiggles1Time extends Action {
    
    ArrayList<Vector2> positions;
    Vector2 position;
    ArrayList<Sprite> sprites;
    Sprite sprite;
    ArrayList<Integer> repeats;
    String sound;
    ArrayList<String> sounds;

    ArrayList<Float> alphas;
    
    public int layer = 120;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};

    
    
    Sprite helperSprite; //just for helping me position the animation. delete later.
    
    @Override
    public void step(Game game) {

        //set sprite position
        //if done with anim, do nextAction
        if (positions.isEmpty() || sprites.isEmpty()) {
                
            String textString = "Darn! The POKÈMON broke free!";
            //nextAction will be a battle menu draw action here
            PublicFunctions.insertToAS(game, new DisplayText(game, textString, null, null, this.nextAction));
            //newAction.step(game);//need to draw the pokeball
            
            game.actionStack.remove(this);
            return;
        }
        
        //control alpha of opposing pkmn
        float currAlpha = this.alphas.get(0);
        game.battle.oppPokemon.sprite.setAlpha(currAlpha);    
        
        //get next sound, play it
        this.sound = this.sounds.get(0);
        if (this.sound != null) {
            PublicFunctions.insertToAS(game, new PlaySound(this.sound, new DoneAction()));
            this.sounds.set(0, null); //don't play same sound over again
        }
        
        
        //get next frame
        this.sprite = sprites.get(0);
        
        //debug
        //this.helperSprite.draw(game.floatingBatch); //debug
        
        //draw current sprite
        if (this.sprite != null) {
            //this.sprite.setScale(3); //post scaling change
            this.sprite.setPosition(position.x, position.y);
            this.sprite.draw(game.floatingBatch);            
        }
        
        //debug
//        if (this.repeats.size() == 1) { //debug
//            return;
//        }
        
        //repeat sprite/pos for current object for 'frames[0]' number of frames.
        if (this.repeats.get(0) > 0) {
            this.repeats.set(0, this.repeats.get(0) - 1);
        }
        else {
            //since position is relative, only update once each time period
            this.position = this.position.add(positions.get(0));
            positions.remove(0);
            sprites.remove(0);
            repeats.remove(0);
            sounds.remove(0);
            alphas.remove(0);
        }
    }
    
    public catchPokemon_wiggles1Time(Game game, Action nextAction) {

        this.nextAction = nextAction;
        
        Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball1.png"));
        
        //initial sprite position
        this.position = new Vector2(114,88); //post scaling change
        
        this.positions = new ArrayList<Vector2>();
        this.positions.add(new Vector2(0,0)); //wait 13
        this.positions.add(new Vector2(-1,0));
        this.positions.add(new Vector2(1,0));
        this.positions.add(new Vector2(1,0));
        this.positions.add(new Vector2(-19,-16));
        for (int i = 0; i < 6; i++) {
            this.positions.add(new Vector2(0,0)); //filler
        }
        //11 total
        
        //wiggle anim
        this.sprites =  new ArrayList<Sprite>();
        this.sprites.add(null); //draw nothing for 13 frames
        
        text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball_wiggleSheet1.png"));
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
        this.sprites.add(new Sprite(text, 12*1, 0, 12, 12)); //left tilt
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
        this.sprites.add(new Sprite(text, 12*2, 0, 12, 12)); //right tilt
        text = new Texture(Gdx.files.internal("throw_pokeball_anim/poof_sheet1.png"));
        this.sprites.add(new Sprite(text, 48*0, 0, 48, 48)); //poof1 
        this.sprites.add(new Sprite(text, 48*1, 0, 48, 48)); //poof2
        this.sprites.add(new Sprite(text, 48*2, 0, 48, 48)); //poof3
        this.sprites.add(new Sprite(text, 48*3, 0, 48, 48)); //poof4
        this.sprites.add(new Sprite(text, 48*4, 0, 48, 48)); //poof5 
        this.sprites.add(null); //draw nothing for 4 frames //done at this point 
        //11 total events
        
        
        
        this.repeats = new ArrayList<Integer>();
        
        this.repeats.add(13-1); //13 frames before pokeball appears on ground

        this.repeats.add(44-1); //first middle sprite has 44 frames
        for (int i = 0; i < 2; i++) { //left-middle-right tilt all have 4 frames
            this.repeats.add(4-1);
        }
        this.repeats.add(14-1); //final right tilt has 14 frames
        for (int i = 0; i < 4; i++) { //each poof has 5 frames
            this.repeats.add(5-1);
        }
        this.repeats.add(10-1); //final poof has 10 frames
        this.repeats.add(4-1); //4 frames of nothing (before text box change)
        //11 total
        
        //sounds to play
        this.sounds = new ArrayList<String>();
        this.sounds.add("pokeball_wiggle1"); //wiggle as ball appears (doing as white appears, so it's uniform)
        for (int i = 0; i < 4; i++) {
            this.sounds.add(null); 
        }
        this.sounds.add("poof1");
        for (int i = 0; i < 5; i++) {
            this.sounds.add(null); 
        }
        //11 total events
        
        //opposing pkmn sprite alphas
        this.alphas = new ArrayList<Float>();
        for (int i = 0; i < 10; i++) {
            this.alphas.add(0f); 
        }
        this.alphas.add(1f); 
        //11 total events
        
        text = new Texture(Gdx.files.internal("throw_pokeball_anim/helper13.png"));
        this.helperSprite = new Sprite(text, 0, 0, 160, 144);
        //this.helperSprite.setPosition(16*10,16*9); //post scaling change
        //this.helperSprite.setScale(3);
        
    }
}


class catchPokemon_wiggles2Times extends Action {

    //Sprite pokeballSprite;
    
    ArrayList<Vector2> positions;
    Vector2 position;
    ArrayList<Sprite> sprites;
    Sprite sprite;
    ArrayList<Integer> repeats;
    String sound;
    ArrayList<String> sounds;

    ArrayList<Float> alphas;
    
    public int layer = 120;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};

    
    
    Sprite helperSprite; //just for helping me position the animation. delete later.
    
    @Override
    public void step(Game game) {

        //set opp pokemon sprite alpha
        //game.battle.oppPokemon.sprite.setAlpha(0); //delete at some point
        
        //set sprite position
        //if done with anim, do nextAction
        if (positions.isEmpty() || sprites.isEmpty()) {
                
            String textString = "Aww! It appeared to be caught!";
            //nextAction will be a battle menu draw action here
            PublicFunctions.insertToAS(game, new DisplayText(game, textString, null, null, this.nextAction));
            //newAction.step(game);//need to draw the pokeball
            
            game.actionStack.remove(this);
            return;
        }
        
        //control alpha of opposing pkmn
        float currAlpha = this.alphas.get(0);
        game.battle.oppPokemon.sprite.setAlpha(currAlpha);    
        
        //get next sound, play it
        this.sound = this.sounds.get(0);
        if (this.sound != null) {
            PublicFunctions.insertToAS(game, new PlaySound(this.sound, new DoneAction()));
            this.sounds.set(0, null); //don't play same sound over again
        }
        
        
        //get next frame
        this.sprite = sprites.get(0);
        
        //debug
        //this.helperSprite.draw(game.floatingBatch); //debug
        
        //draw current sprite
        if (this.sprite != null) {
            //this.sprite.setScale(3); //post scaling change
            this.sprite.setPosition(position.x, position.y);
            this.sprite.draw(game.floatingBatch);            
        }
        
        //debug
//        if (this.repeats.size() == 1) { //debug
//            return;
//        }
        
        //repeat sprite/pos for current object for 'frames[0]' number of frames.
        if (this.repeats.get(0) > 0) {
            this.repeats.set(0, this.repeats.get(0) - 1);
        }
        else {
            //since position is relative, only update once each time period
            this.position = this.position.add(positions.get(0));
            positions.remove(0);
            sprites.remove(0);
            repeats.remove(0);
            sounds.remove(0);
            alphas.remove(0);
        }
    }
    
    public catchPokemon_wiggles2Times(Game game, Action nextAction) {

        this.nextAction = nextAction;
        
        Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball1.png"));
        
        //initial sprite position
        this.position = new Vector2(114,88); //post scaling change
        
        this.positions = new ArrayList<Vector2>();
        this.positions.add(new Vector2(0,0)); //wait 13
        this.positions.add(new Vector2(-1,0));
        this.positions.add(new Vector2(1,0));
        this.positions.add(new Vector2(1,0));
        for (int i = 0; i < 1; i++) {
            this.positions.add(new Vector2(-1,0));
            this.positions.add(new Vector2(-1,0));
            this.positions.add(new Vector2(1,0));
            this.positions.add(new Vector2(1,0));
        }
        this.positions.add(new Vector2(-19,-16));
        for (int i = 0; i < 6; i++) {
            this.positions.add(new Vector2(0,0)); //filler
        }
        //15 total
        
        //wiggle anim
        this.sprites =  new ArrayList<Sprite>();
        this.sprites.add(null); //draw nothing for 13 frames
        
        text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball_wiggleSheet1.png"));
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
        this.sprites.add(new Sprite(text, 12*1, 0, 12, 12)); //left tilt
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
        this.sprites.add(new Sprite(text, 12*2, 0, 12, 12)); //right tilt
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
        this.sprites.add(new Sprite(text, 12*1, 0, 12, 12)); //left tilt
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
        this.sprites.add(new Sprite(text, 12*2, 0, 12, 12)); //right tilt
        text = new Texture(Gdx.files.internal("throw_pokeball_anim/poof_sheet1.png"));
        this.sprites.add(new Sprite(text, 48*0, 0, 48, 48)); //poof1 
        this.sprites.add(new Sprite(text, 48*1, 0, 48, 48)); //poof2
        this.sprites.add(new Sprite(text, 48*2, 0, 48, 48)); //poof3
        this.sprites.add(new Sprite(text, 48*3, 0, 48, 48)); //poof4
        this.sprites.add(new Sprite(text, 48*4, 0, 48, 48)); //poof5 
        this.sprites.add(null); //draw nothing for 4 frames //done at this point 
        //15 total events
        
        
        
        this.repeats = new ArrayList<Integer>();
        
        this.repeats.add(13-1); //13 frames before pokeball appears on ground

        this.repeats.add(44-1); //first middle sprite has 44 frames
        for (int i = 0; i < 3; i++) { //left-middle-right tilt all have 4 frames
            this.repeats.add(4-1);
        }
        this.repeats.add(44-1); //first middle sprite has 44 frames
        for (int i = 0; i < 2; i++) { //left-middle-right tilt all have 4 frames
            this.repeats.add(4-1);
        }
        this.repeats.add(14-1); //final right tilt has 14 frames
        for (int i = 0; i < 4; i++) { //each poof has 5 frames
            this.repeats.add(5-1);
        }
        this.repeats.add(10-1); //final poof has 10 frames
        this.repeats.add(4-1); //4 frames of nothing (before text box change)
        //15 total
        
        //sounds to play
        this.sounds = new ArrayList<String>();
        //this.sounds.add(null); //wait 13 no sound
        this.sounds.add("pokeball_wiggle1"); //wiggle as ball appears (doing as white appears, so it's uniform)
        this.sounds.add(null);
        this.sounds.add("pokeball_wiggle1");
        for (int i = 0; i < 6; i++) {
            this.sounds.add(null); 
        }
        this.sounds.add("poof1");
        for (int i = 0; i < 5; i++) {
            this.sounds.add(null); 
        }
        //15 total events
        
        //opposing pkmn sprite alphas
        this.alphas = new ArrayList<Float>();
        for (int i = 0; i < 14; i++) {
            this.alphas.add(0f); 
        }
        this.alphas.add(1f); 
        //15 total events
        
        text = new Texture(Gdx.files.internal("throw_pokeball_anim/helper13.png"));
        this.helperSprite = new Sprite(text, 0, 0, 160, 144);
        //this.helperSprite.setPosition(16*10,16*9); //post scaling change
        //this.helperSprite.setScale(3);
        
    }
}



//draws pokeball wiggling3 times and then releasing pokemon
 //still need to get wiggle animation done here
class catchPokemon_wiggles3Times extends Action {

    //Sprite pokeballSprite;
    
    ArrayList<Vector2> positions;
    Vector2 position;
    ArrayList<Sprite> sprites;
    Sprite sprite;
    ArrayList<Integer> repeats;
    String sound;
    ArrayList<String> sounds;

    ArrayList<Float> alphas;
    
    public int layer = 120;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};

    
    
    Sprite helperSprite; //just for helping me position the animation. delete later.
    
    @Override
    public void step(Game game) {

        //set opp pokemon sprite alpha
        //game.battle.oppPokemon.sprite.setAlpha(0); //delete at some point
        
        //set sprite position
        //if done with anim, do nextAction
        if (positions.isEmpty() || sprites.isEmpty()) {

            //PokemonCaught_Events - sprite and text
            //new DisplayText(game, string)
//            Action newAction = new PokemonCaught_Events(
//                                    game, new SplitAction(
//                                        new BattleFadeOut(game, 
//                                                new playerStanding(game)
//                                        ),
//                                        new BattleFadeOutMusic(game, new DoneAction())
//                                    )
//                                );

                
            //String textString = "Darn! The POKeMON broke free!";
            String textString = "Shoot! It was so close, too!";
            //nextAction will be a battle menu draw action here
            PublicFunctions.insertToAS(game, new DisplayText(game, textString, null, null, this.nextAction));
            //newAction.step(game);//need to draw the pokeball

            //since pkmn was caught, add to players pokemon
            //game.player.pokemon.add(game.battle.oppPokemon);
            
            game.actionStack.remove(this);
            return;
        }
        
        //control alpha of opposing pkmn
        float currAlpha = this.alphas.get(0);
        game.battle.oppPokemon.sprite.setAlpha(currAlpha);    
        
        //get next sound, play it
        this.sound = this.sounds.get(0);
        if (this.sound != null) {
            PublicFunctions.insertToAS(game, new PlaySound(this.sound, new DoneAction()));
            this.sounds.set(0, null); //don't play same sound over again
        }
        
        
        //get next frame
        this.sprite = sprites.get(0);
        
        //debug
        //this.helperSprite.draw(game.floatingBatch); //debug
        
        //draw current sprite
        if (this.sprite != null) {
            //this.sprite.setScale(3); //post scaling change
            this.sprite.setPosition(position.x, position.y);
            this.sprite.draw(game.floatingBatch);            
        }
        
        //debug
//        if (this.repeats.size() == 6) { //debug
//            return;
//        }
        
        //repeat sprite/pos for current object for 'frames[0]' number of frames.
        if (this.repeats.get(0) > 0) {
            this.repeats.set(0, this.repeats.get(0) - 1);
        }
        else {
            //since position is relative, only update once each time period
            this.position = this.position.add(positions.get(0));
            positions.remove(0);
            sprites.remove(0);
            repeats.remove(0);
            sounds.remove(0);
            alphas.remove(0);
        }
    }
    
    public catchPokemon_wiggles3Times(Game game, Action nextAction) {

        //TODO - would be nice to confirm this version with a recording
        
        this.nextAction = nextAction;
        
        Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball1.png"));
        
        //initial sprite position
        this.position = new Vector2(114,88); //post scaling change
        
        this.positions = new ArrayList<Vector2>();
        this.positions.add(new Vector2(0,0)); //wait 13
        this.positions.add(new Vector2(-1,0));
        this.positions.add(new Vector2(1,0));
        this.positions.add(new Vector2(1,0));
        for (int i = 0; i < 2; i++) {
            this.positions.add(new Vector2(-1,0));
            this.positions.add(new Vector2(-1,0));
            this.positions.add(new Vector2(1,0));
            this.positions.add(new Vector2(1,0));
        }
        this.positions.add(new Vector2(-19,-16));
        for (int i = 0; i < 6; i++) {
            this.positions.add(new Vector2(0,0)); //filler
        }
        //19 total
        
        //wiggle anim
        this.sprites =  new ArrayList<Sprite>();
        this.sprites.add(null); //draw nothing for 13 frames
        
        text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball_wiggleSheet1.png"));
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
        this.sprites.add(new Sprite(text, 12*1, 0, 12, 12)); //left tilt
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
        this.sprites.add(new Sprite(text, 12*2, 0, 12, 12)); //right tilt
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
        this.sprites.add(new Sprite(text, 12*1, 0, 12, 12)); //left tilt
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
        this.sprites.add(new Sprite(text, 12*2, 0, 12, 12)); //right tilt
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
        this.sprites.add(new Sprite(text, 12*1, 0, 12, 12)); //left tilt
        this.sprites.add(new Sprite(text, 12*0, 0, 12, 12)); //middle
        this.sprites.add(new Sprite(text, 12*2, 0, 12, 12)); //right tilt 
        text = new Texture(Gdx.files.internal("throw_pokeball_anim/poof_sheet1.png"));
        this.sprites.add(new Sprite(text, 48*0, 0, 48, 48)); //poof1 
        this.sprites.add(new Sprite(text, 48*1, 0, 48, 48)); //poof2
        this.sprites.add(new Sprite(text, 48*2, 0, 48, 48)); //poof3
        this.sprites.add(new Sprite(text, 48*3, 0, 48, 48)); //poof4
        this.sprites.add(new Sprite(text, 48*4, 0, 48, 48)); //poof5 
        this.sprites.add(null); //draw nothing for 4 frames //done at this point //18 total events
        
        
        this.repeats = new ArrayList<Integer>();
        
        this.repeats.add(13-1); //13 frames before pokeball appears on ground

        this.repeats.add(44-1); //first middle sprite has 44 frames
        for (int i = 0; i < 3; i++) { //left-middle-right tilt all have 4 frames
            this.repeats.add(4-1);
        }
        this.repeats.add(44-1); //first middle sprite has 44 frames
        for (int i = 0; i < 3; i++) { //left-middle-right tilt all have 4 frames
            this.repeats.add(4-1);
        }
        this.repeats.add(44-1); //first middle sprite has 44 frames
        for (int i = 0; i < 2; i++) { //left-middle tilt both have 4 frames
            this.repeats.add(4-1);
        }
        this.repeats.add(14-1); //final right tilt has 14 frames
        for (int i = 0; i < 4; i++) { //each poof has 5 frames
            this.repeats.add(5-1);
        }
        this.repeats.add(10-1); //final poof has 10 frames
        this.repeats.add(4-1); //4 frames of nothing (before text box change)
        //19 total
        
        //sounds to play
         //TODO - wiggle sound
        this.sounds = new ArrayList<String>();
        //this.sounds.add(null); //wait 13 no sound
        this.sounds.add("pokeball_wiggle1"); //wiggle as ball appears (doing as white appears, so it's uniform)
        this.sounds.add(null);
        this.sounds.add("pokeball_wiggle1");
        for (int i = 0; i < 3; i++) {
            this.sounds.add(null); 
        }
        this.sounds.add("pokeball_wiggle1");
        for (int i = 0; i < 6; i++) {
            this.sounds.add(null); 
        }
        this.sounds.add("poof1");
        for (int i = 0; i < 5; i++) {
            this.sounds.add(null); 
        }
        //this.sounds.add("pokeball_wiggle1"); //this is in 3 wiggle?
//        for (int i = 0; i < 3; i++) {
//            this.sounds.add(null); 
//        }
        
        //opposing pkmn sprite alphas
        this.alphas = new ArrayList<Float>();
        for (int i = 0; i < 18; i++) {
            this.alphas.add(0f); 
        }
        this.alphas.add(1f); 
        //19 total events
        
        text = new Texture(Gdx.files.internal("throw_pokeball_anim/helper13.png"));
        this.helperSprite = new Sprite(text, 0, 0, 160, 144);
        //this.helperSprite.setPosition(16*10,16*9); //post scaling change
        //this.helperSprite.setScale(3);
        
    }
}





class PrintAngryEating extends Action {

    public int layer = 120;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};
    
    
    
    @Override
    public void step(Game game) {
        
        if (game.battle.oppPokemon.angry > 0) {
            String textString = "Wild " + game.battle.oppPokemon.name.toUpperCase() + " is angry!";
            PublicFunctions.insertToAS(game, new DisplayText(game, textString, null, null, this.nextAction));
            game.actionStack.remove(this);
            
            game.battle.oppPokemon.angry--;
            //If the angry counter is decreased to zero this way, 
             //the PokÈmon's catch rate will also be reset to its initial catch rate,
             //regardless of how it has been modified in the battle before this point;
            if (game.battle.oppPokemon.angry <= 0) {
                int baseCatchRate = game.battle.oppPokemon.baseStats.get("catchRate"); //or maxStats?
                game.battle.oppPokemon.currentStats.put("catchRate", baseCatchRate);
            }
            return;
        }
        else if (game.battle.oppPokemon.eating > 0) {
            String textString = "Wild " + game.battle.oppPokemon.name.toUpperCase() + " is eating!";
            PublicFunctions.insertToAS(game, new DisplayText(game, textString, null, null, this.nextAction));
            game.actionStack.remove(this);
            
            game.battle.oppPokemon.eating--;
            return;
        }

        //made it here, so not eating or angry
        PublicFunctions.insertToAS(game, this.nextAction);
        game.actionStack.remove(this);
        return;
    }

    public PrintAngryEating(Game game, Action nextAction) {
        
        this.nextAction = nextAction;
        
    }
    
}


class ChanceToRun extends Action {

    public int layer = 120;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};
    
    
    

    @Override
    public void step(Game game) {
    
        //TODO - uses up one frame here when it shouldn't.
         //call nextAction step? but layer will be wrong
         //ignoring for now

        //can't make this a function in drawMenu, because
         //i need this calculation to happen after angry counters have been modified
        int x = game.battle.oppPokemon.currentStats.get("speed") % 256; //current speed or max speed?
        x = x*2;

        System.out.println("Chance to run, x: " + String.valueOf(x));
        
        if (x > 255) {
            //pokemon runs
            String textString = "Wild " + game.battle.oppPokemon.name.toUpperCase() + " ran!";
            PublicFunctions.insertToAS(game, new DisplayText(game, textString, null, null,
                                             new SplitAction(new OppPokemonFlee(game, 
                                                             new SplitAction(new BattleFadeOut(game,
                                                                             new DoneAction()), //new playerStanding(game)),
                                                             new BattleFadeOutMusic(game, new DoneAction()))),
                                             new WaitFrames(game, 8, new PlaySound("run1", new DoneAction())) //8 frames seems right
                                             )));
            //demo code - reset adrenaline
            game.player.adrenaline = 0;
            //
            game.actionStack.remove(this);
            return;
        }
        if (game.battle.oppPokemon.angry > 0) {
            x = x*2;
            if (x > 255) { //capped at 255
                x = 255;
            }
        }
        else if (game.battle.oppPokemon.eating > 0) {
            x = x/4;
        }

        int r = game.map.rand.nextInt(255+1); //+1 to include upper bound
        if (r < x) {
            //pokemon runs
            String textString = "Wild " + game.battle.oppPokemon.name.toUpperCase() + " ran!";
            PublicFunctions.insertToAS(game, new DisplayText(game, textString, null, null,
                                             new SplitAction(new OppPokemonFlee(game, 
                                                             new SplitAction(new BattleFadeOut(game,
                                                                             new DoneAction()), //new playerStanding(game)),
                                                             new BattleFadeOutMusic(game, new DoneAction()))),
                                             new WaitFrames(game, 8,
                                             new PlaySound("run1",
                                             new DoneAction()
                                             )))));
            //demo code - reset adrenaline
            game.player.adrenaline = 0;
            //
            //PublicFunctions.insertToAS(game, new PlaySound("click1", new PlaySound("run1", new DoneAction())));
            game.actionStack.remove(this);
            return;
        }
                
        //pokemon doesn't run
        //insert nextAction to actionstack
        PublicFunctions.insertToAS(game, this.nextAction); //usually menu draw action
        game.actionStack.remove(this);
        
    }
    

    public ChanceToRun(Game game, Action nextAction) {

        this.nextAction = nextAction;
        
        //  - if run chance succeeds, then print 'ran away' text and exit battle actions
        //  - if failed, go to menuAction (nextAction)
        
        //experiment - add angry/eating print to AS?
        //automatically adds 'print angry/eating to AS'
        /*
        PublicFunctions.insertToAS(game, new PrintAngryEating(game, this)); //usually menu draw action
        game.actionStack.remove(this);
        */ //didn't do b/c confusing. can use this in place of series' tho
        
    }
}



class OppPokemonFlee extends Action {

    ArrayList<Vector2> positions;
    Vector2 position;
    Sprite sprite;
    ArrayList<Integer> repeats;
    String sound;
    ArrayList<String> sounds;

    
    public int layer = 120;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};

    
    
    Sprite helperSprite; //just for helping me position the animation. delete later.
    
    @Override
    public void step(Game game) {

        //set opp pokemon sprite alpha
        //game.battle.oppPokemon.sprite.setAlpha(0); //delete at some point
        
        //set sprite position
        //if done with anim, do nextAction
        if (positions.isEmpty()) {
                
            PublicFunctions.insertToAS(game, this.nextAction);            
            game.actionStack.remove(this);
            return;
        }
        
        //get next sound, play it
        this.sound = this.sounds.get(0);
        if (this.sound != null) {
            PublicFunctions.insertToAS(game, new PlaySound(this.sound, new DoneAction()));
            this.sounds.set(0, null); //don't play same sound over again
        }
        
        
        //get next frame
        //this.sprite = sprites.get(0); //not using
        
        //debug
        //this.helperSprite.draw(game.floatingBatch); //debug


        //this.sprite.setScale(3); //post scaling change
        game.battle.oppPokemon.sprite.setPosition(position.x, position.y);
        game.battle.oppPokemon.sprite.draw(game.floatingBatch);    
        
        
        //debug
//        if (this.repeats.size() == 1) { //debug
//            return;
//        }
        
        //repeat sprite/pos for current object for 'frames[0]' number of frames.
        if (this.repeats.get(0) > 0) {
            this.repeats.set(0, this.repeats.get(0) - 1);
        }
        else {
            //since position is relative, only update once each time period
            this.position = this.position.add(positions.get(0));
            positions.remove(0);
            repeats.remove(0);
            sounds.remove(0);
        }
    }
    
    public OppPokemonFlee(Game game, Action nextAction) {

        this.nextAction = nextAction;
        
        //Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball1.png"));
        
        //initial oppPokemon sprite position
        this.position = new Vector2(game.battle.oppPokemon.sprite.getX(),game.battle.oppPokemon.sprite.getY()); 
        
        this.positions = new ArrayList<Vector2>();
        this.positions.add(new Vector2(0,0)); //wait 16 frames
        for (int i = 0; i < 13; i++) { //move 8 pixels to the right every 3 frames
            this.positions.add(new Vector2(8,0));
        }
        //14 total


        this.repeats = new ArrayList<Integer>();
        
        this.repeats.add(16-1); //16 frames of nothing
        for (int i = 0; i < 13; i++) {
            this.repeats.add(3-1); //3 frames each movement
        }
        //14 total
        
        //sounds to play //nothing?
        this.sounds = new ArrayList<String>();
        for (int i = 0; i < 14; i++) {
            this.sounds.add(null); 
        }
        //14 total events


        //text = new Texture(Gdx.files.internal("throw_pokeball_anim/helper13.png"));
        //this.helperSprite = new Sprite(text, 0, 0, 160, 144);
        //this.helperSprite.setPosition(16*10,16*9); //post scaling change
        //this.helperSprite.setScale(3);
        
    }
}


//displays caught pkmn text
//plays victory fanfare
//continues to draw pokeball on screen
class PokemonCaught_Events extends Action {

    Action displayTextAction;
    
    Sprite pokeballSprite;
    
    public int layer = 120;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};
    
    
    
    boolean startLooking;
    
    //what to do at each iteration
    public void step(Game game) {


        this.pokeballSprite.draw(game.floatingBatch);
        
        //when text action first appears, start checking for when it leaves AS
        if (this.startLooking == false && game.actionStack.contains(this.displayTextAction)) {
            this.startLooking = true;
            return; //won't have left AS on this first iteration, might as well return
        }
        
        //for now, if displayTextAction not in AS, remove and return
        if (!game.actionStack.contains(this.displayTextAction) && this.startLooking == true) {
            //set oppPokemon alpha back to normal
            game.battle.oppPokemon.sprite.setAlpha(1);
            
            PublicFunctions.insertToAS(game, this.nextAction);
            game.actionStack.remove(this);
            //stop drawing the battle
            game.actionStack.remove(game.battle.drawAction); //stop drawing the battle
            game.battle.drawAction = null; //essentially using this like a flag

            //alternative is to have drawAction check for boolean to not exist, and remove itself if flag not set
            return;
        }
        
    }

    public PokemonCaught_Events(Game game, int adrenaline, Action nextAction) {
        
        this.nextAction = nextAction;
        
        this.startLooking = false;

        //step through DisplayText until finished 
        //String string1 = game.battle.oppPokemon.name.toUpperCase()+" was    caught!";
        String string1 = "All right! "+game.battle.oppPokemon.name.toUpperCase()+" was caught!";
        //demo code
        String string2 = ""+game.player.name+" gained "+Character.forDigit(adrenaline,10)+" ADRENALINE!"; 

        this.displayTextAction  = new DisplayText(game, string2,  null, null, new DoneAction());
        Action firstTextAction = new DisplayText(game, string1, "fanfare1", null, this.displayTextAction); //trigger
                
        PublicFunctions.insertToAS(game, firstTextAction);
        //
        
        //
        Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/pokeball_wiggleSheet1.png"));
        this.pokeballSprite = new Sprite(text, 12*2, 0, 12, 12); //right tilt
        this.pokeballSprite.setPosition(115,88);
        //this.pokeballSprite.setScale(3); //post scaling change

        
        
        
    }
    
}


//poof animation 
//and algorithm that will show pkmn extending outwards
class ThrowOutPokemon extends Action {

    Sprite pokeballSprite;
    
    ArrayList<Vector2> positions;
    Vector2 position;
    ArrayList<Sprite[][]> sprites;
    Sprite[][] sprite;
    ArrayList<Integer> repeats;
    ArrayList<String> sounds;
    String sound;
    
    // night shade didn't work when this was layer 110, 
    //  but worked when it was 114
    //  but shaders worked when it was 110 so weird
    // note - technically needs to be above displaytext, which is 106
    //  but i think nightshade/mewtwo battle had issues when it was 105
//    public int layer = 114;  // tested
    // issue - 105 is above battlemenu and itemsmenu
    public int layer = 105;  // TODO: test
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};

    
    
    Sprite helperSprite; //just for helping me position the animation. delete later.
    
    boolean doneYet;
    boolean firstStep;
    
    @Override
    public void step(Game game) {

        if (this.firstStep == true) {
            game.battle.drawAction.drawFriendlyPokemonAction = this;
            this.firstStep = false;            
        }
        
        //get next frame
        this.sprite = sprites.get(0);

        //debug
//        this.helperSprite.draw(game.floatingBatch); 
        
        //set sprite position
        //if done with anim, do nextAction
        if (positions.isEmpty() || sprites.isEmpty()) {


            //debug
//            if (this.repeats.size() == 0) { 
//                return;
//            }
            
            if (!this.doneYet) {
                PublicFunctions.insertToAS(game, this.nextAction);

                //set real sprite to correct position
                game.player.currPokemon.backSprite.setPosition(position.x, position.y);
                
                for (int i = 0; i < this.sprite.length; i++) {
                    for (int j = 0; j < this.sprite[i].length; j++) {
                        this.sprite[i][j].setPosition(position.x, position.y);
                    }
                }
                this.doneYet = true;
                
                // modify layer of this action
                this.layer = 114;
                game.actionStack.remove(this);
                PublicFunctions.insertToAS(game, this);
            }
            
            // No performance different here either way
//            System.out.println("here1");
//            game.player.currPokemon.backSprite.draw(game.floatingBatch);
            // TODO: not sure why I was doing this, assuming it was for a reason
            for (int i = 0; i < this.sprite.length; i++) {
                for (int j = 0; j < this.sprite[i].length; j++) {
                    this.sprite[i][j].draw(game.floatingBatch);
                }
            }
            
            
            if (game.battle.drawAction == null) {
                game.actionStack.remove(this);
            }
            return;
        }
                
        
        
        //draw current sprite
        if (this.sprite != null) {
            for (int i = 0; i < this.sprite.length; i++) {
                for (int j = 0; j < this.sprite[i].length; j++) {
                    if (this.sprite[i][j] != null) {
                        this.sprite[i][j].setPosition(position.x+8*i, position.y+8*j);
                        this.sprite[i][j].draw(game.floatingBatch);    
                    }
                }
            }
        }
        
        //debug
//        if (this.repeats.size() == 6) { 
//            return;
//        }

        //get next sound, play it
        this.sound = this.sounds.get(0);
        if (this.sound != null) {
            PublicFunctions.insertToAS(game, new PlaySound(this.sound, new DoneAction()));
            this.sounds.set(0, null); //don't play same sound over again
        }
        
        
        //repeat sprite/pos for current object for 'frames[0]' number of frames.
        if (this.repeats.get(0) > 1) {
            this.repeats.set(0, this.repeats.get(0) - 1);
        }
        else {
            //since position is relative, only update once each time period
            this.position = this.position.add(positions.get(0));
            positions.remove(0);
            sprites.remove(0);
            repeats.remove(0);
            sounds.remove(0);
        }
    }
    
    public ThrowOutPokemon(Game game, Action nextAction) {
        this.firstStep = true;
        
        this.doneYet = false;
        this.nextAction = nextAction;

        //consider doing relative positions from now on
        //this.position = new Vector2(104+4*3-2,200-6*3-2); //post scaling change
        this.position = new Vector2(16,32);
        
        this.positions = new ArrayList<Vector2>();
        this.positions.add(new Vector2(0,0));//first of poof anim
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0)); //1 empty frame
        this.positions.add(new Vector2(18-24,18)); //first of pokemon expand anim
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(0,0));
        this.positions.add(new Vector2(24-18+6,18-6)); //last is always dummy pos (?need anymore?)
        
        
        this.sprites =  new ArrayList<Sprite[][]>();
        this.sprites.add(null); //draw nothing for 40 frames
        //'poof' animation 
        Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/poof_sheet1.png"));
        this.sprites.add(new Sprite[][]{new Sprite[]{new Sprite(text, 48*0, 0, 48, 48)}}); 
        this.sprites.add(new Sprite[][]{new Sprite[]{new Sprite(text, 48*1, 0, 48, 48)}}); 
        this.sprites.add(new Sprite[][]{new Sprite[]{new Sprite(text, 48*2, 0, 48, 48)}}); 
        this.sprites.add(new Sprite[][]{new Sprite[]{new Sprite(text, 48*3, 0, 48, 48)}}); 
        this.sprites.add(new Sprite[][]{new Sprite[]{new Sprite(text, 48*4, 0, 48, 48)}}); 

        this.sprites.add(null); //draw nothing for 1 frame
        
        //below code will splice up image to create 'expanding' effect
        
//         7 blocks of 4 sprites each, 4th block is 'middle'
//         working backwards
//         frame 4to3 - x: remove cols 3 and 5 , y: remove rows 3 and 5
//         frame 3to2: x: remove cols 3 and 5  , y: remove rows 2 and 4
//         frame 2to1: x: remove cols 3 and 5  , y: remove rows 1 and 2 - no
//          - cloyster ends up with 5,7 as initial sprite
        
        // TODO: this doesn't work for gen2+ because sprites are twice as big
        //3 frames release1
        Sprite temp = new Sprite(game.player.currPokemon.backSprite);
        TextureRegion[][] tempRegion = temp.split(4, 4); //should be 7x7
        
        //
        Sprite[][] temp2 = new Sprite[7][7];
        for (int i = 0; i < tempRegion.length; i++) {
            for (int j = 0; j < tempRegion[i].length; j++) {
                temp2[i][j] = new Sprite(tempRegion[6-j][i]);
                temp2[i][j].setScale(2);
//                System.out.println("sprite size: "+String.valueOf(temp2[i][j].getHeight())+"  "+String.valueOf(temp2[i][j].getHeight()));
            }
        }
        
        //remove rows 3 and 5
        for (int i = 0; i < temp2.length; i++) {
            temp2[i][2] = temp2[i][3];
            temp2[i][3] = temp2[i][5];
            temp2[i][4] = temp2[i][6];
            temp2[i][5] = null;
            temp2[i][6] = null;
        }
        //remove cols 3 and 5
        temp2[2] = temp2[1];
        temp2[1] = temp2[0];
        temp2[0] = new Sprite[]{};
        temp2[4] = temp2[5];
        temp2[5] = temp2[6];
        temp2[6] = new Sprite[]{};
        //i=0, j=6 -> i=6, j=0
        //i=0, j=0 -> i=6, j=6
        
        //copy temp 2
        Sprite[][] temp3 = new Sprite[7][7]; //.clone();
        for (int i = 0; i < temp2.length; i++) {
            for (int j = 0; j < temp2[i].length; j++) {
                temp3[i][j] = temp2[i][j];
            }
        }
        
        //remove rows 2 and 4
        for (int i = 0; i < temp3.length; i++) {
            if (temp3[i].length <= 0) {
                continue;
            }
            temp3[i][1] = temp3[i][2];
            temp3[i][2] = temp3[i][4];
            temp3[i][3] = null;
            temp3[i][4] = null;
        }
        //remove cols 3 and 5
        temp3[2] = temp3[1];
        temp3[1] = new Sprite[]{};
        temp3[4] = temp3[5];
        temp3[5] = new Sprite[]{};

        Sprite temp4 = new Sprite(tempRegion[0][4]);
        temp4.setScale(2);        
        
        
        this.sprites.add(new Sprite[][]{new Sprite[]{}, new Sprite[]{}, new Sprite[]{}, new Sprite[]{temp4}});
        this.sprites.add(temp3);
        this.sprites.add(temp2);
        this.sprites.add(new Sprite[][]{new Sprite[]{game.player.currPokemon.backSprite}});
        //3 frames release2
        //6 frames release3
        
        // -1?
        this.repeats = new ArrayList<Integer>();
        this.repeats.add(40);
        for (int i = 0; i < 4; i++) {
            this.repeats.add(5);
        }
        this.repeats.add(10); //last is 10 frames
        this.repeats.add(1); //last is 10 frames
        this.repeats.add(3);
        this.repeats.add(3);
        this.repeats.add(6);


        

        this.sounds = new ArrayList<String>();
        this.sounds.add(null); //
        this.sounds.add("poof1");
        for (int i = 0; i < 9; i++) {
            this.sounds.add(null);
        }
        
        
//        text = new Texture(Gdx.files.internal("pokemon_throw_out_anim/helper5.png"));
//        this.helperSprite = new Sprite(text, 0, 0, 160, 144);
        //this.helperSprite.setPosition(16*10,16*9); //post scaling change
        //this.helperSprite.setScale(3); //post scaling change

        //play 'throw pokeball' sound
        //PublicFunctions.insertToAS(game, new PlaySound("throw_pokeball1", new DoneAction()));
        
        
    }
}


//poof animation 
//and algorithm that will show pkmn extending outwards
class ThrowOutPokemonCrystal extends Action {

  Sprite pokeballSprite;
  
  ArrayList<Vector2> positions;
  Vector2 position;
  ArrayList<Sprite[][][]> sprites;
  Sprite[][][] sprite;
  ArrayList<Integer> repeats;
  ArrayList<String> sounds;
  String sound;
  
  // night shade didn't work when this was layer 110, 
  //  but worked when it was 114
  //  but shaders worked when it was 110 so weird
//  public int layer = 114; 
  public int layer = 105; 
  public int getLayer(){return this.layer;}

  public String getCamera() {return "gui";};

  
  
  Sprite helperSprite; //just for helping me position the animation. delete later.
  
  boolean doneYet;
  boolean firstStep;
  
  @Override
  public void step(Game game) {

      if (this.firstStep == true) {
          game.battle.drawAction.drawFriendlyPokemonAction = this;
          this.firstStep = false;            
      }

      //get next frame
      this.sprite = sprites.get(0);

      //debug
//      if (!Gdx.input.isKeyPressed(Input.Keys.N)) {
//          this.helperSprite.draw(game.floatingBatch); 
//      }

      //set sprite position
      //if done with anim, do nextAction
      if (sprites.size() <= 1) {  // positions.isEmpty() || 

          //debug
//          if (this.repeats.size() == 0) { 
//              return;
//          }

          if (!this.doneYet) {
              PublicFunctions.insertToAS(game, this.nextAction);

              //set real sprite to correct position
              game.player.currPokemon.backSprite.setPosition(position.x, position.y);

              for (int k = 0; k < this.sprite.length; k++) {
                  if (this.sprite[k] == null) {
                      continue;
                  }
                  for (int i = 0; i < this.sprite[k].length; i++) {
                      for (int j = 0; j < this.sprite[k][i].length; j++) {
                          this.sprite[k][i][j].setPosition(position.x, position.y);
                      }
                  }
              }
              this.doneYet = true;

              // modify layer of this action
              this.layer = 114;
              game.actionStack.remove(this);
              PublicFunctions.insertToAS(game, this);
          }

          // No performance difference here either way
//          System.out.println("here1");
//          game.player.currPokemon.backSprite.draw(game.floatingBatch);
          // TODO: not sure why I was doing this, assuming it was for a reason
          for (int k = 0; k < this.sprite.length; k++) {
              for (int i = 0; i < this.sprite[k].length; i++) {
                  for (int j = 0; j < this.sprite[k][i].length; j++) {
                      this.sprite[k][i][j].draw(game.floatingBatch);
                  }
              }
          }

          if (game.battle.drawAction == null) {
              game.actionStack.remove(this);
          }
          return;
      }

      //draw current sprite
      if (this.sprite != null) {
          for (int k = 0; k < this.sprite.length; k++) {
              if (this.sprite[k] == null) {
                  continue;
              }
              for (int i = 0; i < this.sprite[k].length; i++) {
                  for (int j = 0; j < this.sprite[k][i].length; j++) {
                      if (this.sprite[k][i][j] != null) {
                          this.sprite[k][i][j].setPosition(position.x +8*i -4*k, position.y +8*j -8*k);
                          this.sprite[k][i][j].draw(game.floatingBatch);    
                      }
                  }
              }
          }
      }

      //debug
//      if (this.repeats.size() == 2) {
//          return;
//      }

      //get next sound, play it
      this.sound = this.sounds.get(0);
      if (this.sound != null) {
          PublicFunctions.insertToAS(game, new PlaySound(this.sound, new DoneAction()));
          this.sounds.set(0, null); //don't play same sound over again
      }

      //repeat sprite/pos for current object for 'frames[0]' number of frames.
      if (this.repeats.get(0) > 1) {
          this.repeats.set(0, this.repeats.get(0) - 1);
      }
      else {
          //since position is relative, only update once each time period
//          this.position = this.position.add(positions.get(0));
//          positions.remove(0);
          sprites.remove(0);
          repeats.remove(0);
          sounds.remove(0);
      }
  }
  
  public ThrowOutPokemonCrystal(Game game, Action nextAction) {
      this.firstStep = true;
      
      this.doneYet = false;
      this.nextAction = nextAction;

      //consider doing relative positions from now on
      //this.position = new Vector2(104+4*3-2,200-6*3-2); //post scaling change
      this.position = new Vector2(16, 48);

//      this.positions = new ArrayList<Vector2>();
//      this.positions.add(new Vector2(0,0));//first of poof anim
//      this.positions.add(new Vector2(0,0));
//      this.positions.add(new Vector2(0,0));
//      this.positions.add(new Vector2(0,0));
//      this.positions.add(new Vector2(0,0));
//      this.positions.add(new Vector2(0,0)); //1 empty frame
//      this.positions.add(new Vector2(18-24,18)); //first of pokemon expand anim
//      this.positions.add(new Vector2(0,0));
//      this.positions.add(new Vector2(0,0));
//      this.positions.add(new Vector2(24-18+6,18-6)); //last is always dummy pos (?need anymore?)

      
      //below code will splice up image to create 'expanding' effect

//       7 blocks of 4 sprites each, 4th block is 'middle'
//       working backwards
//       frame 4to3 - x: remove cols 3 and 5 , y: remove rows 3 and 5
//       frame 3to2: x: remove cols 3 and 5  , y: remove rows 2 and 4
//       frame 2to1: x: remove cols 3 and 5  , y: remove rows 1 and 2 - no
//        - cloyster ends up with 5,7 as initial sprite

      // TODO: this doesn't work for gen2+ because sprites are twice as big
      //3 frames release1
      Sprite temp = new Sprite(game.player.currPokemon.backSprite);
      // sprite width is 48, need 8x8 blocks
      TextureRegion[][] tempRegion = temp.split(8, 8); //should be 8x8 blocks

      Sprite[][] temp2 = new Sprite[6][6];
      for (int i = 0; i < tempRegion.length; i++) {
          for (int j = 0; j < tempRegion[i].length; j++) {
              temp2[i][j] = new Sprite(tempRegion[5-j][i]);
//              temp2[i][j].setScale(2);  // TODO: remove
//              System.out.println("sprite size: "+String.valueOf(temp2[i][j].getHeight())+"  "+String.valueOf(temp2[i][j].getHeight()));
          }
      }

      // remove rows 1 and 4
      for (int i = 0; i < temp2.length; i++) {
          temp2[i][1] = temp2[i][2];
          temp2[i][2] = temp2[i][3];
          temp2[i][3] = temp2[i][5];
          temp2[i][4] = null;
          temp2[i][5] = null;
      }
      // remove cols 1 and 4
      temp2[1] = temp2[0];
      temp2[0] = new Sprite[]{};
      temp2[4] = temp2[5];
      temp2[5] = new Sprite[]{};
      //i=0, j=6 -> i=6, j=0
      //i=0, j=0 -> i=6, j=6

      //copy temp 2
      Sprite[][] temp3 = new Sprite[6][6];
      for (int i = 0; i < temp2.length; i++) {
          for (int j = 0; j < temp2[i].length; j++) {
              temp3[i][j] = temp2[i][j];
          }
      }

      //remove rows 1 and 2
      for (int i = 0; i < temp3.length; i++) {
          if (temp3[i].length <= 0) {
              continue;
          }
          temp3[i][1] = temp3[i][3];
          temp3[i][2] = null;
          temp3[i][3] = null;
          temp3[i][4] = null;
          temp3[i][5] = null;
      }
      //remove cols 2 and 3
      temp3[2] = temp3[1];
      temp3[1] = new Sprite[]{};
      temp3[3] = temp3[4];
      temp3[4] = new Sprite[]{};
      temp3[5] = new Sprite[]{};

//      Sprite temp4 = new Sprite(tempRegion[0][4]);
//      temp4.setScale(2);

      // TODO: need 'frame' class
      //  sprites, repeat, positions, etc

      //'poof' animation 
      Texture text = new Texture(Gdx.files.internal("throw_pokeball_anim/poof_sheet2.png"));
      this.sprites =  new ArrayList<Sprite[][][]>();
      // draw nothing for 34 frames
      this.sprites.add(null); 
      // poof sprites are mixed with pokemon sprites
      // 4 frames poof1
      this.sprites.add(new Sprite[][][]{null, new Sprite[][]{new Sprite[]{new Sprite(text, 48*0, 0, 48, 48)}}});
      // 2 frames poof2
      this.sprites.add(new Sprite[][][]{null, new Sprite[][]{new Sprite[]{new Sprite(text, 48*1, 0, 48, 48)}}});
      // 2 frames poof2, small sprite appears
      this.sprites.add(new Sprite[][][]{temp3, new Sprite[][]{new Sprite[]{new Sprite(text, 48*1, 0, 48, 48)}}});
      // 2 frames poof3, small sprite
      this.sprites.add(new Sprite[][][]{temp3, new Sprite[][]{ new Sprite[]{new Sprite(text, 48*2, 0, 48, 48)}}});
      // 2 frames poof3, middle sprite appears
      this.sprites.add(new Sprite[][][]{temp2, new Sprite[][]{ new Sprite[]{new Sprite(text, 48*2, 0, 48, 48)}}});
      // 2 frames poof4, middle sprite visible
      this.sprites.add(new Sprite[][][]{temp2, new Sprite[][]{ new Sprite[]{new Sprite(text, 48*3, 0, 48, 48)}}});
      // 2 frames poof4, sprite enlarge to normal size
      this.sprites.add(new Sprite[][][]{new Sprite[][]{ new Sprite[]{game.player.currPokemon.backSprite}}, new Sprite[][]{ new Sprite[]{new Sprite(text, 48*3, 0, 48, 48)}}});
      this.sprites.add(new Sprite[][][]{new Sprite[][]{ new Sprite[]{game.player.currPokemon.backSprite}}}); // wait frames before pokemon cry
      this.sprites.add(new Sprite[][][]{new Sprite[][]{ new Sprite[]{game.player.currPokemon.backSprite}}}); // dummy frame

//      this.sprites.add(new Sprite[][]{new Sprite[]{}, new Sprite[]{}, new Sprite[]{}, new Sprite[]{temp4}});
//      this.sprites.add(temp3);
//      this.sprites.add(temp2);
//      this.sprites.add(new Sprite[][]{new Sprite[]{game.player.currPokemon.backSprite}});
      //3 frames release2
      //6 frames release3

      // -1?
      this.repeats = new ArrayList<Integer>();
      this.repeats.add(34);
      this.repeats.add(4);
      this.repeats.add(2);
      this.repeats.add(2);
      this.repeats.add(2);
      this.repeats.add(2);
      this.repeats.add(2);
      this.repeats.add(2);
      this.repeats.add(26); // wait 12 frames before pokemon cry. TODO: test this.

      this.sounds = new ArrayList<String>();
      this.sounds.add(null); //
      this.sounds.add("poof1");
      for (int i = 0; i < 9; i++) {
          this.sounds.add(null);
      }
//      text = new Texture(Gdx.files.internal("pokemon_throw_out_anim/throwanim_helper4.png"));
//      this.helperSprite = new Sprite(text, 0, 0, 160, 144);
  }
}




class SpecialBattleMewtwo extends Action {

    public int layer = 107;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};
    
    Music music;
    boolean firstStep = true;
//    int timer = 0;
    int timer = 1670; // TODO: debug, remove

    @Override
    public void step(Game game) {

        if (this.firstStep) {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("battle/pokemon_mansion_remix_eq.ogg"));
            this.music.setLooping(true);
            this.music.setVolume(.9f);
            
            game.currMusic = this.music;
            game.currMusic.stop();
            game.currMusic.play();
            game.currMusic.setPosition(28); // TODO: debug, remove
            
            this.firstStep = false;
        }

        if (this.timer == 0) {
            
            // remove the text box
            game.actionStack.remove(game.displayTextAction);
            game.displayTextAction = null;
            PublicFunctions.insertToAS(game, new DisplayTextIntro(game, "...",
                                                                  null, null, false, new DoneAction()));
        }
        else if (this.timer == 100) {
            
            // remove the text box
            game.actionStack.remove(game.displayTextAction);
            game.displayTextAction = null;
            PublicFunctions.insertToAS(game, new DisplayTextIntro(game, "Humans...",
                                                                  null, null, false, new DoneAction()));
        }
        else if (this.timer == 300 -20) {
            
            // remove the text box
            game.actionStack.remove(game.displayTextAction);
            game.displayTextAction = null;
            PublicFunctions.insertToAS(game, new DisplayTextIntro(game, "These humans cared nothing for me...",
                                                                  null, null, false, new DoneAction()));
        }
        else if (this.timer == 600 -20 -20) {
            
            // remove the text box
            game.actionStack.remove(game.displayTextAction);
            game.displayTextAction = null;
            PublicFunctions.insertToAS(game, new DisplayTextIntro(game, "From the moment I first opened my eyes, humans have sought to control me.",
                                                                  null, null, false, new DoneAction()));
        }
        else if (this.timer == 1000 -20 -20) {
            
            // remove the text box
            game.actionStack.remove(game.displayTextAction);
            game.displayTextAction = null;
            PublicFunctions.insertToAS(game, new DisplayTextIntro(game, "But no more.",
                                                                  null, null, false, new DoneAction()));
        }
        else if (this.timer == 1200 -20 -20) {
            
            // remove the text box
            game.actionStack.remove(game.displayTextAction);
            game.displayTextAction = null;
            PublicFunctions.insertToAS(game, new DisplayTextIntro(game, "Why are you here?",
                                                                  null, null, false, new DoneAction()));
        }
        else if (this.timer == 1350 -20 -20) {
            
            // remove the text box
            game.actionStack.remove(game.displayTextAction);
            game.displayTextAction = null;
            PublicFunctions.insertToAS(game, new DisplayTextIntro(game, "You desire to control me, just like the others.",
                                                                  null, null, false, new DoneAction()));
        }
        else if (this.timer == 1580) {
            
            // remove the text box
            game.actionStack.remove(game.displayTextAction);
            game.displayTextAction = null;
            PublicFunctions.insertToAS(game, new DisplayTextIntro(game, "...",
                                                                  null, null, false, new DoneAction()));
        }
        
        if (this.timer >= 1685) {

            game.actionStack.remove(game.displayTextAction);
            game.displayTextAction = null;
            
            SpecialMewtwo1 mewtwo = new SpecialMewtwo1(70);
            game.battle.oppPokemon = mewtwo;
            
            Action triggerAction = new PlaySound(game.player.currPokemon.name, 
                                   new WaitFrames(game, 6,
                                   new DrawBattleMenu_Normal(game, new DoneAction())
                                   ));

            
//            Action nextAction =  new WaitFrames(game, 15, 
            Action nextAction =  new BattleIntro(
                                 new SpecialBattleMewtwo.BattleIntro1(
                                 new SplitAction(
                                         new SpecialBattleMewtwo.DrawBattle1(game),
                                 new SplitAction(
                                         new SpecialBattleMewtwo.DrawBreathingSprite(mewtwo),
                                 new SplitAction(
                                         new SpecialBattleMewtwo.RocksEffect2(),
                                 new SpecialBattleMewtwo.IntroAnim(game,
                                 new SplitAction(
                                         new SpecialBattleMewtwo.RocksEffect1(),
                                 new SplitAction(
                                         new SpecialBattleMewtwo.RippleEffect1(),
                                 new PlaySound(game.battle.oppPokemon.name, 
                                 new DisplayText(game, "Wild "+game.battle.oppPokemon.name.toUpperCase()+" appeared!", null, null, 
                                 new SplitAction(
                                         new WaitFrames(game, 1,
                                         new DrawEnemyHealth(game)
                                 ),
                                 new WaitFrames(game, 39,
                                 new MovePlayerOffScreen(game, 
                                 new DisplayText(game, "Go! "+game.player.currPokemon.name.toUpperCase()+"!", null, triggerAction, 
                                 new SplitAction(
                                         new DrawFriendlyHealth(game),
                                 new ThrowOutPokemon(game, //this draws pkmn sprite permanently until battle ends, ie until game.battle.drawAction == null
                                 triggerAction
                                 ))))))))))))))));
            game.actionStack.remove(this);
            PublicFunctions.insertToAS(game, nextAction);
        }
        this.timer++;
    }

    public SpecialBattleMewtwo(Game game) {

        
    }
    
    // draw sprite and move it up and down
    static class DrawBreathingSprite extends Action {

        public int layer = 129;
        public int getLayer(){return this.layer;}

        public String getCamera() {return "gui";};
        
        SpecialMewtwo1 mewtwo;
        
        // set true when should start breathing
        static boolean shouldBreathe = false;
        int timer = 300;
        int offsetY = 0;

        @Override
        public void step(Game game) {
            
            // TODO: debug, delete
//            this.shouldBreathe = true;
            if (this.shouldBreathe) {
                this.timer--;
                if (this.timer == 149) {
                    this.offsetY = -1;
                }
                else if (this.timer == 0){
                    this.timer = 300;
                    this.offsetY = 0;
                }
            }
            
            // TODO: this needs to be above draw enemy sprite
            //
            
            // always draw sprite relative to other sprite
//            game.floatingBatch.draw(this.mewtwo.breathingSprite,
//                                    this.mewtwo.sprite.getX(),
//                                    this.mewtwo.sprite.getY() + this.offsetY);
            this.mewtwo.breathingSprite.setPosition(this.mewtwo.sprite.getX(), this.mewtwo.sprite.getY() + this.offsetY);
            this.mewtwo.breathingSprite.draw(game.floatingBatch);
            
            if (game.battle.drawAction == null) {
                game.actionStack.remove(this);
                return;
            }
        }
        
        public DrawBreathingSprite(SpecialMewtwo1 mewtwo) {
            this.mewtwo = mewtwo;
        }
    }

    // scroll both players into view
    // shader animation
    // set shouldBreathe
    static class IntroAnim extends Action {
        
        ArrayList<Vector2> moves_relative;
        Vector2 move;
        
        

        public int layer = 140;
        public int getLayer(){return this.layer;}

        public String getCamera() {return "gui";};
        
        int timer = 0;
        String vertexShader;

        @Override
        public void step(Game game) {


//            if (this.timer < 60 || this.timer > 700) {
//            }
            if (!moves_relative.isEmpty()) {
                //get next frame
                this.move = moves_relative.get(0);

                float xPos = game.player.battleSprite.getX() - move.x;//*3;
                game.player.battleSprite.setX(xPos);

                xPos = game.battle.oppPokemon.sprite.getX() + move.x;//*3;
                game.battle.oppPokemon.sprite.setX(xPos);
                        
                moves_relative.remove(0);
                
                if (moves_relative.isEmpty()) {
                    PublicFunctions.insertToAS(game, new DisplayTextIntro(game, "An oppressive force surrounds you...",
                                                                          null, null, false, new DoneAction()));
                }
            }
            
            if (this.timer == 380) {
                // remove the text box
                game.actionStack.remove(game.displayTextAction);
                game.displayTextAction = null;
                PublicFunctions.insertToAS(game, new DisplayTextIntro(game, "MEWTWO unleashes its full power!",
                                                                      null, null, false, new DoneAction()));
            }
            
            
            if (this.timer == 0) {
                // TODO: will fail if WebGL (maybe LibGDX has fixed this?)
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(.8f));
                game.floatingBatch.setShader(shader);
//                game.battle.oppPokemon.sprite.setColor(.2f, .2f, .2f, .2f);
            }
            
            
            if (this.timer == 490) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(.6f));
                game.floatingBatch.setShader(shader);
            }
            else if (this.timer == 490 + 6) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(.4f));
                game.floatingBatch.setShader(shader);
            }
            else if (this.timer == 490 + 6*2) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(.2f));
                game.floatingBatch.setShader(shader);
            }
            else if (this.timer == 490 + 6*3) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(0f));
                game.floatingBatch.setShader(shader);
            }
            else if (this.timer == 490 + 6*4) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-.2f));
                game.floatingBatch.setShader(shader);
            }
            else if (this.timer == 490 + 6*5) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-.4f));
                game.floatingBatch.setShader(shader);
            }
            else if (this.timer == 490 + 6*6) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-.6f));
                game.floatingBatch.setShader(shader);
            }
            else if (this.timer == 490 + 6*7) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-.8f));
                game.floatingBatch.setShader(shader);
            }
            else if (this.timer == 490 + 6*8) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-.85f));
                game.floatingBatch.setShader(shader);
            }
            else if (this.timer == 490 + 6*9) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-.90f));
                game.floatingBatch.setShader(shader);
            }
            else if (this.timer == 490 + 6*10) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-.95f));
                game.floatingBatch.setShader(shader);
            }
            else if (this.timer == 490 + 6*11) {
                // stop displaying text box
                game.actionStack.remove(game.displayTextAction);
                game.displayTextAction = null;

                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-1f));
                game.floatingBatch.setShader(shader);
            }
            else if (this.timer == 490 + 6*19 + 3) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-.5f));
                game.floatingBatch.setShader(shader);
            }
            else if (this.timer == 490 + 6*19 + 5) {
                game.floatingBatch.setShader(null);
            }
            else if (this.timer == 620) {
                SpecialBattleMewtwo.DrawBreathingSprite.shouldBreathe = true;
            }
            
            
            if (this.timer >= 640) {
                
                game.actionStack.remove(this);
                PublicFunctions.insertToAS(game, this.nextAction);
            }
            this.timer++;
        }
        
        public IntroAnim(Game game, Action nextAction) {
            
            this.nextAction = nextAction;
            this.moves_relative = new ArrayList<Vector2>();

            //animation to play
            for (int i = 0; i < 72*2; i++) {
                moves_relative.add(new Vector2(1,0));
            }

            game.player.battleSprite.setPosition(175+1-8-2,71+1-10);
            game.player.battleSprite.setScale(2);
            game.battle.oppPokemon.sprite.setPosition(-30-4-1-14,106+2-5-15);
            
            this.vertexShader = "attribute vec4 a_position;\n"
                    + "attribute vec4 a_color;\n"
                    + "attribute vec2 a_texCoord;\n"
                    + "attribute vec2 a_texCoord0;\n"
                        
                    + "uniform mat4 u_projTrans;\n"
                                            
                    + "varying vec4 v_color;\n"
                    + "varying vec2 v_texCoords;\n"
                                            
                    + "void main()\n"
                    + "{\n"
                    + "    v_color = a_color;\n"
                    + "    v_texCoords = a_texCoord0;\n"
                    + "    gl_Position =  u_projTrans * a_position;\n"
                    // below can be used to translate screen pixels (for attacks, etc
//                    + "    gl_Position =  u_projTrans * vec4(a_position.x, a_position.y, a_position.z, 1.0);\n"
                    + "}\n";
        }

        String getShader(float level) {
            
            String shader = "precision mediump float;\n"

                            + "varying vec4 v_color;\n"
                            + "varying vec2 v_texCoords;\n"
                            + "uniform sampler2D u_texture;\n"
                            + "uniform mat4 u_projTrans;\n"
                            
                            + "bool equals(float a, float b) {\n"
                            + "    return abs(a-b) < 0.0001;\n"
                            + "}\n"
                            
                            + "void main() {\n"
                            + "    vec4 color = texture2D (u_texture, v_texCoords) * v_color;\n"

                            + "    float level = "+level+";\n" // can't do +- or -+ inline
                            + "    if(color.r >= .9 && color.g >= .9 && color.b >= .9) {\n"
                            + "           color = vec4(color.r, color.g, color.b, color.a);\n"
                            + "    }\n"
                            + "    else {\n"
                            + "        color = vec4(color.r-level, color.g-level, color.b-level, color.a);\n"
                            + "    }\n"
                            + "    gl_FragColor = color;\n"
                            + "}\n";

            // version that keeps everything black/white (didnt like as much)
//            String shader = "precision mediump float;\n"
//
//                            + "varying vec4 v_color;\n"
//                            + "varying vec2 v_texCoords;\n"
//                            + "uniform sampler2D u_texture;\n"
//                            + "uniform mat4 u_projTrans;\n"
//                            
//                            + "bool equals(float a, float b) {\n"
//                            + "    return abs(a-b) < 0.0001;\n"
//                            + "}\n"
//                            
//                            + "void main() {\n"
//                            + "    vec4 color = texture2D (u_texture, v_texCoords) * v_color;\n"
//
//                            + "    float level = ("+level+"+1.0)/2.0;\n" // can't do +- or -+ inline
//                            + "    if(color.r >= .9 && color.g >= .9 && color.b >= .9) {\n"
//                            + "           color = vec4(color.r, color.g, color.b, color.a);\n"
//                            + "    }\n"
//                            + "    else if(color.r < level || color.g < level || color.b < level) {\n"
//                            + "        color = vec4(0, 0, 0, color.a);\n"
//                            + "    }\n"
//                            + "    else {\n"
//                            + "        color = vec4(1, 1, 1, color.a);\n"
//                            + "    }\n"
//                            + "    gl_FragColor = color;\n"
//                            + "}\n";
            return shader;
        }
    }
    
    class BattleIntro1 extends Action {
        
        ArrayList<Sprite> frames;
        Sprite frame;
        
        

        public int layer = 139;
        public int getLayer(){return this.layer;}

        public String getCamera() {return "gui";};
        
        @Override
        public void step(Game game) {
            
            //if done with anim, do nextAction
            if (frames.isEmpty()) {
                PublicFunctions.insertToAS(game, this.nextAction);
                game.actionStack.remove(this);
                //avoid lag
                nextAction.step(game);
                return;
            }
            
            //get next frame
            this.frame = frames.get(0);
            
            if (this.frame != null) {
                //gui version
                    //this.frame.setScale(3); //scale doesn't work in batch.draw //used these when scaling mattered
                    //this.frame.setPosition(16*10,16*9);
                this.frame.draw(game.floatingBatch);
                //map version
                //game.batch.draw(this.frame, 16, -16);
            }
            
            frames.remove(0);
        }
        
        public BattleIntro1(Action nextAction) {
            
            this.nextAction = nextAction;
            this.frames = new ArrayList<Sprite>();
            Texture text1 = new Texture(Gdx.files.internal("battle/battle_intro_anim1_sheet1.png"));
            
            //animation to play
            for (int i = 0; i < 28; i++) {
                frames.add(new Sprite(text1, i*160, 0, 160, 144));
            }

            for (int i = 0; i < 42*3; i++) {
                frames.add(new Sprite(text1, 27*160, 0, 160, 144));
            }
        }
    }
    
    class RippleEffect1 extends Action {

        public int layer = 109;
        public int getLayer(){return this.layer;}

        public String getCamera() {return "gui";};
        
        ShaderProgram shader;
        String fragShader;
        int yPos = -16;
        
        Pixmap pixmap;
        Sprite sprite;
//        int[] offsets = new int[] {2, 1, 0, -1, -2, -1, 0, 1};
        int[] offsets = new int[] {0, 0, 0, 1, 2, 2, 2, 3, 4, 4, 4, 3, 2, 2, 2, 1};
        
        @Override
        public void step(Game game) {
            
            //TODO: why isn't breathing sprite drawn?
            
            if (this.yPos <= 144) {
//                this.pixmap = ScreenUtils.getFrameBufferPixmap(0, this.yPos, 160, 16); //looked neat
                
                //TODO: *3 b/c screen is scaled, alternative approach would be nice
                this.pixmap = ScreenUtils.getFrameBufferPixmap(0, this.yPos*3, 160*3, 16*3);
                for (int j=0; j < 16; j++) {
                    for (int i=0; i < 160; i++) {
//                        this.sprite.setColor(this.pixmap.getPixel(i*3, j*3)); //trippy colors
                        this.sprite.setColor(new Color(this.pixmap.getPixel(i*3, j*3)));
                        this.sprite.setPosition(i + this.offsets[j], j + this.yPos);
                        this.sprite.draw(game.floatingBatch);
                    }
                }
            }
            
//            this.shader = new ShaderProgram(this.getVertexShader(this.yPos), this.fragShader);
//            game.floatingBatch.setShader(shader);
            
            if (this.yPos > 144*4) {
                this.yPos = 0;
            }
            if (this.yPos % 2 == 0) {
                this.yPos+=3;
            }
            else {
                this.yPos+=4;
            }

            if (game.battle.drawAction == null) {
                game.actionStack.remove(this);
                return;
            }
        }

        public RippleEffect1() {

            Texture text = new Texture(Gdx.files.internal("battle/pixel1.png"));
            this.sprite = new Sprite(text, 0, 0, 1, 1);
            
            //TODO: didn't use
            this.fragShader = "precision mediump float;\n"
                            + "varying vec4 v_color;\n"
                            + "varying vec2 v_texCoords;\n"
                            + "uniform sampler2D u_texture;\n"
                            + "uniform mat4 u_projTrans;\n"
                            
                            + "bool equals(float a, float b) {\n"
                            + "    return abs(a-b) < 0.0001;\n"
                            + "}\n"
                            
                            + "void main() {\n"
                            + "    vec4 color = texture2D (u_texture, v_texCoords) * v_color;\n"
                            + "    gl_FragColor = color;\n"
                            + "}\n";
        }
        
        String getVertexShader(int timer) {

            // used to translate pixels over
            String vertexShader = "attribute vec4 a_position;\n"
                    + "attribute vec4 a_color;\n"
                    + "attribute vec2 a_texCoord;\n"
                    + "attribute vec2 a_texCoord0;\n"
                        
                    + "uniform mat4 u_projTrans;\n"
                                            
                    + "varying vec4 v_color;\n"
                    + "varying vec2 v_texCoords;\n"
                                            
                    + "void main()\n"
                    + "{\n"
                    + "    v_color = a_color;\n"
                    + "    v_texCoords = a_texCoord0;\n"
                    + "    int timer = "+timer+";\n"
                    + "    int offset = (timer + int(a_position.y)) % 16;\n"
//                    + "    offset = offset/2;\n"
//                    + "    if (int(a_position.y) >= timer && int(a_position.y) < timer + 16) {\n"
                    + "        if (offset == 0 || offset == 4) {offset = 0;}\n"
                    + "        else if (offset == 1 || offset == 3) {offset = 1;}\n"
                    + "        else if (offset == 2) {offset = 2;}\n"
                    + "        else if (offset == 5 || offset == 7) {offset = -1;}\n"
                    + "        else if (offset == 6) {offset = -2;}\n"
//                    + "    }\n"
                    // below can be used to translate screen pixels (for attacks, etc
                    + "    gl_Position =  u_projTrans * vec4(a_position.x + offset, a_position.y, a_position.z, 1.0);\n"
                    + "}\n";
            
            
            return vertexShader;
        }
    }
    
    static class RocksEffect1 extends Action {

        public int layer = 111;
        public int getLayer(){return this.layer;}

        public String getCamera() {return "gui";};

        Sprite textboxSprite;
        Sprite[] sprites = new Sprite[10];
        //flip-flop between these two velocities
        int[] velocities = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        int[] velocities2 = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        public static int velocityX = 0;
        int velocity = 1;
        int whichVelocity = 0;
        Random rand = new Random();
        boolean firstStep = true;
        public static boolean shouldMoveX = false;
        public static boolean shouldMoveY = true;

        @Override
        public void step(Game game) {

            if (this.firstStep) {
                SpecialBattleMewtwo.RocksEffect2.drawRocks = true;
                this.firstStep = false;
            }

            for (int i=0; i < 10; i++) {
                if (this.whichVelocity == 0) {
                    this.velocity = this.velocities[i];
                }
                else {
                    this.velocity = this.velocities2[i];
                }
                if (!this.shouldMoveY) {
                    this.velocity = 0;
                }
                
                this.sprites[i].setPosition(this.sprites[i].getX() + this.velocityX,
                                            this.sprites[i].getY() + this.velocity);
                if (this.sprites[i].getY() > 144) {
                    this.sprites[i].setPosition(rand.nextInt(160-32), rand.nextInt(144) - 144);
//                    this.sprites[i].setRotation(rand.nextInt(4) * 90);
                    this.velocities[i] = rand.nextInt(2) + 1;
                    this.velocities2[i] = this.velocities[i] -1 +rand.nextInt(2);
                }
                if (this.sprites[i].getX() < 0) {
                    this.sprites[i].setPosition(160, rand.nextInt(144) - 32);
                }
                this.sprites[i].draw(game.floatingBatch);

            }
            this.whichVelocity = (this.whichVelocity + 1) % 2;

            //need textbox over animation
            this.textboxSprite.draw(game.floatingBatch);
            
            if (game.battle.drawAction == null) {
                game.actionStack.remove(this);
                return;
            }
        }
        
        public RocksEffect1() {

            Texture text = new Texture(Gdx.files.internal("battle/battle_bg4.png"));
            this.textboxSprite = new Sprite(text, 0, 0, 160, 144);
            
            text = new Texture(Gdx.files.internal("battle/rock1.png"));
            this.sprites[0] = new Sprite(text, 0*32, 0, 32, 32);
            this.sprites[0].setColor(1f, 1f, 1f, 1f);
            this.sprites[1] = new Sprite(text, 1*32, 0, 32, 32);
            this.sprites[2] = new Sprite(text, 2*32, 0, 32, 32);
            this.sprites[3] = new Sprite(text, 5*32, 0, 32, 32);
            this.sprites[4] = new Sprite(text, 6*32, 0, 32, 32);
            this.sprites[5] = new Sprite(text, 1*32, 0, 32, 32);
            this.sprites[6] = new Sprite(text, 2*32, 0, 32, 32);
            this.sprites[7] = new Sprite(text, 5*32, 0, 32, 32);
            this.sprites[8] = new Sprite(text, 6*32, 0, 32, 32);
            this.sprites[9] = new Sprite(text, 0*32, 0, 32, 32);
            this.sprites[9].setColor(1f, 1f, 1f, 1f);
            
            for (int i=0; i < 10; i++) {
                this.sprites[i].setPosition(rand.nextInt(160-32), rand.nextInt(144) - 144);
                this.velocities[i] = rand.nextInt(2) + 1;
                this.velocities2[i] = this.velocities[i] -1 +rand.nextInt(2);
//                this.sprites[i].setRotation(rand.nextInt(4) * 90);
            }
        }
    }
    static class RocksEffect2 extends Action {

        public int layer = 131;
        public int getLayer(){return this.layer;}

        public String getCamera() {return "gui";};
        Sprite bgSprite;
        Sprite bgSprite2;
        Sprite[] sprites = new Sprite[10];
        //flip-flop between these two velocities
        int[] velocities = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        int[] velocities2 = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        public static int velocityX = 0;
        int velocity = 1;
        int whichVelocity = 0;
        Random rand = new Random();
        public static boolean shouldMoveX = false;
        public static boolean shouldMoveY = true;
        
        float[] bg_values = new float[]{0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 08f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 08f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
                                        0.9f, 0.9f, 0.9f, 0.9f, 0.9f, 0.9f,
                                        0.95f, 0.95f, 0.95f, 0.95f, 0.95f, 0.95f,
                                        1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 
                                        1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 
                                        1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 
                                        1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 
                                        1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 
                                        1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 
                                        1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 
                                        1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 
                                        0.95f, 0.95f, 0.95f, 0.95f, 0.95f, 0.95f,
                                        0.9f, 0.9f, 0.9f, 0.9f, 0.9f, 0.9f,};
        int bg_values_idx = 0;
        float curr_value = 0f;
        
        static boolean drawRocks = false;

        @Override
        public void step(Game game) {

            // didn't end up liking this
//            //need bg under the animation
//            this.curr_value = this.bg_values[this.bg_values_idx];
////            System.out.println(curr_value);
//            this.bg_values_idx += 1;
//            if (this.bg_values_idx >= this.bg_values.length) {
//                this.bg_values_idx = 0;
//            }
//
//            this.bgSprite.setColor(this.curr_value, this.curr_value, this.curr_value, 1f);
//            this.bgSprite2.setColor(this.curr_value, this.curr_value, this.curr_value, 1f);
            this.bgSprite.draw(game.floatingBatch);
//            game.floatingBatch.draw(this.bgSprite, 0, 0);

            if (this.drawRocks) {
                for (int i=0; i < 10; i++) {
                    if (this.velocities2[i] <= 0) {
                        this.velocity = 1;
                        this.velocities2[i] = this.velocities[i];
                    }
                    else {
                        this.velocity = 0;
                    }
                    this.velocities2[i]--;

                    if (!this.shouldMoveY) {
                        this.velocity = 0;
                    }
                    
                    this.sprites[i].setPosition(this.sprites[i].getX() + this.velocityX,
                                                this.sprites[i].getY() + this.velocity);
                    if (this.sprites[i].getY() > 144) {
                        this.sprites[i].setPosition(rand.nextInt(160-32), rand.nextInt(46) + (144-46-32-46));
                        this.sprites[i].setRotation(rand.nextInt(4) * 90);
                        this.velocities[i] = rand.nextInt(5) + 4;
                        this.velocities2[i] = this.velocities[i];
                    }
                    if (this.sprites[i].getX() < 0) {
                        this.sprites[i].setPosition(160, rand.nextInt(144-32));
                        this.sprites[i].setRotation(rand.nextInt(4) * 90);
                    }
                    this.sprites[i].draw(game.floatingBatch);

                }
                this.whichVelocity = (this.whichVelocity + 1) % 2;
                this.bgSprite2.draw(game.floatingBatch);
            }

            
            if (game.battle.drawAction == null) {
                game.actionStack.remove(this);
                return;
            }
        }
        
        public RocksEffect2() {

            Texture text = new Texture(Gdx.files.internal("battle/battle_bg3.png"));
            this.bgSprite = new Sprite(text, 0, 0, 160, 144);
            
            text = new Texture(Gdx.files.internal("battle/battle_bg5.png"));
            this.bgSprite2 = new Sprite(text, 0, 0, 160, 144);
            
            text = new Texture(Gdx.files.internal("battle/rock1.png"));
            this.sprites[0] = new Sprite(text, 0*32, 0, 32, 32);
            this.sprites[0].setColor(0f, 0f, 0f, 1f);
            this.sprites[1] = new Sprite(text, 1*32, 0, 32, 32);
            this.sprites[1].setColor(0f, 0f, 0f, 1f);
            this.sprites[2] = new Sprite(text, 2*32, 0, 32, 32);
            this.sprites[2].setColor(0f, 0f, 0f, 1f);
            this.sprites[3] = new Sprite(text, 5*32, 0, 32, 32);
            this.sprites[3].setColor(0f, 0f, 0f, 1f);
            this.sprites[4] = new Sprite(text, 0*32, 0, 32, 32);
            this.sprites[4].setColor(0f, 0f, 0f, 1f);
            this.sprites[5] = new Sprite(text, 1*32, 0, 32, 32);
            this.sprites[5].setColor(0f, 0f, 0f, 1f);
            this.sprites[6] = new Sprite(text, 2*32, 0, 32, 32);
            this.sprites[6].setColor(0f, 0f, 0f, 1f);
            this.sprites[7] = new Sprite(text, 5*32, 0, 32, 32);
            this.sprites[7].setColor(0f, 0f, 0f, 1f);
            this.sprites[8] = new Sprite(text, 0*32, 0, 32, 32);
            this.sprites[8].setColor(0f, 0f, 0f, 1f);
            this.sprites[9] = new Sprite(text, 0*32, 0, 32, 32);
            this.sprites[9].setColor(0f, 0f, 0f, 1f);
            
            for (int i=0; i < 10; i++) {
                this.sprites[i].setPosition(rand.nextInt(160-32), rand.nextInt(46) + (144-46-32-46));
                this.velocities[i] = rand.nextInt(5) + 4;
                this.velocities2[i] = this.velocities[i];
                this.sprites[i].setRotation(rand.nextInt(4) * 90);
            }
        }
    }
    
    class DrawBattle1 extends DrawBattle {


        public int layer = 130;
        public int getLayer(){return this.layer;}

        public String getCamera() {return "gui";};
        
        @Override
        public void step(Game game) {
//            this.bgSprite.draw(game.floatingBatch);
            //debug
//            this.helperSprite.draw(game.floatingBatch);

            if (shouldDrawOppPokemon) {
                game.battle.oppPokemon.sprite.draw(game.floatingBatch);
            }
            game.player.battleSprite.draw(game.floatingBatch);
        }
        
        public DrawBattle1(Game game) {
            super(game);
        }
    }
}



class SpecialBattleMegaGengar extends Action {

    public int layer = 107;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};
    
    Music music;
    boolean firstStep = true;
    int timer = 0;
    Music temp;
//    int timer = 1670; // TODO: debug, remove

    @Override
    public void step(Game game) {

        if (this.firstStep) {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("battle/mgengar_battle_intro1.wav"));
            this.music.setLooping(false);
            // trying to balance this the attack sound effects mainly - they are pretty quiet
            this.music.setVolume(.2f);
//            this.music.setVolume(.0f); // TODO: debug, remove

            // TODO: sound manager object needs to do this
            game.currMusic.stop();
            game.currMusic.dispose();

            game.currMusic = this.music;
            game.currMusic.stop();
            game.currMusic.play();
//            game.currMusic.setPosition(28); // TODO: debug, remove

            game.currMusic.setOnCompletionListener(new Music.OnCompletionListener() {
                @Override
                public void onCompletion(Music aMusic) {
                    //fade normal battle music in
//                    Action nextMusic = new PlaySound("mgengar_battle1", new DoneAction());
////                    PublicFunctions.insertToAS(PkmnGen.staticGame, nextMusic);
//                    nextMusic.step(PkmnGen.staticGame);
////                    nextMusic.step(PkmnGen.staticGame);

//                    synchronized (PkmnGen.staticGame) {
//                        try {
//                            PkmnGen.staticGame.wait(1);
//                        } catch (InterruptedException e) {
//                            // TODO Auto-generated catch block
//                            e.printStackTrace();
//                        }
                    // still a little bit of lag when playing
                    // note: lowering sample rate of songs didn't help
                    //  tried lowering for both songs
                    // looked at source, didn't see any better way than below
                    //  load from file, play(), pause(), play later
                    // still occasionally lags...
                    //  note: got more consistent after using .wav
                    //  now it's just a very slight lag
                    //  tried mp3 - slower than ogg and wav
//                    System.out.println(System.nanoTime());
//                    PkmnGen.staticGame.currMusic.stop(); // didn't help
//                    System.out.println(System.nanoTime());
//                    PkmnGen.staticGame.currMusic.dispose();
//                    System.out.println(System.nanoTime());
//                    System.out.println(System.nanoTime());
                    temp.play(); //timed this, takes the longest
//                    PkmnGen.staticGame.currMusic = temp;
//                    //lower lag, but still noticeable
//                    PkmnGen.staticGame.currMusic.setPosition(0f);
//                    PkmnGen.staticGame.currMusic.setVolume(.9f);
//                    System.out.println(System.nanoTime());
//                    PkmnGen.staticGame.notify();
//                    }
                }
            });
            
            // pre-load battle music
            temp = Gdx.audio.newMusic(Gdx.files.internal("battle/mgengar_battle1.wav"));
            temp.setLooping(true);
            temp.setVolume(.2f);
            temp.play();
            temp.pause();
            game.loadedMusic.put("mgengar_battle1", temp);
            
            this.firstStep = false;
        }
        
        // test
//        if (this.timer == 1) {
//            temp.pause();
////            temp.setPosition(0f);
//            temp.setVolume(.9f);
//        }

        if (this.timer == 0) {

            game.actionStack.remove(game.displayTextAction);
            game.displayTextAction = null;

            SpecialMegaGengar1 gengar = new SpecialMegaGengar1(70);
            game.battle.oppPokemon = gengar;
            
            Action triggerAction = new PlaySound(game.player.currPokemon.name, 
                                   new WaitFrames(game, 6,
                                   new DrawBattleMenu_Normal(game, new DoneAction())
                                   ));

//            Action nextAction =  new WaitFrames(game, 15, 
            Action nextAction =  new BattleIntro(
                                 new SpecialBattleMegaGengar.BattleIntro1(
                                 new SplitAction(
                                         new SpecialBattleMegaGengar.DrawBattle1(game),
                                 new SplitAction(
                                         new SpecialBattleMegaGengar.DrawBreathingSprite(gengar),
                                 new SpecialBattleMegaGengar.IntroAnim(game,
//                                 new PlaySound(game.battle.oppPokemon.name,
//                                 new DisplayText(game, ""+game.battle.oppPokemon.name.toUpperCase()+" attacked!", null, null, 
                                 new SplitAction(
                                         new WaitFrames(game, 1,
                                         new DrawEnemyHealth(game)
                                 ),
                                 new WaitFrames(game, 39,
                                 new MovePlayerOffScreen(game, 
                                 new DisplayText(game, "Go! "+game.player.currPokemon.name.toUpperCase()+"!", null, triggerAction, 
                                 new SplitAction(
                                         new DrawFriendlyHealth(game),
                                 new ThrowOutPokemon(game, //this draws pkmn sprite permanently until battle ends, ie until game.battle.drawAction == null
                                 triggerAction
                                 )))))))))));

            PublicFunctions.insertToAS(game, nextAction);
        }
        // TODO: remove
//        else if (this.timer >= 771) {
//
//            game.currMusic.stop();
//
//            this.music = Gdx.audio.newMusic(Gdx.files.internal("battle/mgengar_battle1.wav"));
//            this.music.setLooping(true);
//            this.music.setVolume(.9f);
////            this.music.setVolume(.0f);  // TODO: debug, remove
//
//            game.currMusic = this.music;
//            game.currMusic.stop();
//            game.currMusic.play();
//
//            game.actionStack.remove(this);
//        }
        this.timer++;
    }

    public SpecialBattleMegaGengar(Game game) {

        
    }
    
    // draw sprite and move it up and down
    static class DrawBreathingSprite extends Action {

        public int layer = 131;
        public int getLayer(){return this.layer;}

        public String getCamera() {return "gui";};
        
        SpecialMegaGengar1 gengar;
        
        // set true when should start breathing
        static boolean shouldBreathe = false;
        int timer = 300;
        int offsetY = 0;
        int offsetY2 = 0;
        Sprite bgSprite;

        @Override
        public void step(Game game) {
            
            // TODO: debug, delete
//            this.shouldBreathe = true;
            if (this.shouldBreathe) {
                this.timer--;
                if (this.timer == 74) {
                    this.offsetY2 = -1;
                }
                else if (this.timer == 274){
                    this.offsetY2 = 0;
                }
                if (this.timer == 149) {
                    this.offsetY = -1;
                }
                else if (this.timer == 0){
                    this.timer = 300;
                    this.offsetY = 0;
                }
            }

            //need bg under the animation
            this.bgSprite.draw(game.floatingBatch);
            
            // TODO: this needs to be above draw enemy sprite
            //
            
            // always draw sprite relative to other sprite
//            game.floatingBatch.draw(this.mewtwo.breathingSprite,
//                                    this.mewtwo.sprite.getX(),
//                                    this.mewtwo.sprite.getY() + this.offsetY);
            //breathing sprite is on top of base in this instance
            this.gengar.breathingSprite.setPosition(this.gengar.sprite.getX(), this.gengar.sprite.getY() - this.offsetY);
            this.gengar.breathingSprite.draw(game.floatingBatch);
            // annoying workaround
            this.gengar.sprite.setPosition(this.gengar.sprite.getX(), this.gengar.sprite.getY() - this.offsetY2);
            this.gengar.sprite.draw(game.floatingBatch);
            this.gengar.sprite.setPosition(this.gengar.sprite.getX(), this.gengar.sprite.getY() + this.offsetY2);
            
            if (game.battle.drawAction == null) {
                game.actionStack.remove(this);
                return;
            }
        }

        public DrawBreathingSprite(SpecialMegaGengar1 gengar) {
            this.gengar = gengar;
            Texture text = new Texture(Gdx.files.internal("battle/battle_bg3.png"));
            this.bgSprite = new Sprite(text, 0, 0, 160, 144);
        }
    }

    // scroll both players into view
    // shader animation
    // set shouldBreathe
    static class IntroAnim extends Action {

        public int layer = 140;
        public int getLayer(){return this.layer;}
        public String getCamera() {return "gui";};

        ArrayList<Vector2> moves_relative;
        Vector2 move;
        int timer = 0;
        String vertexShader;
        boolean firstStep = true;
        

        @Override
        public void step(Game game) {

            if (this.firstStep) {
                // TODO: will fail if WebGL (maybe LibGDX has fixed this?)
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(.8f));
                game.floatingBatch.setShader(shader);
                this.firstStep = false;
            }

            if (!moves_relative.isEmpty()) {
                //get next frame
                this.move = moves_relative.get(0);

                float xPos = game.player.battleSprite.getX() - move.x;//*3;
                game.player.battleSprite.setX(xPos);

                xPos = game.battle.oppPokemon.sprite.getX() + move.x;//*3;
                game.battle.oppPokemon.sprite.setX(xPos);
                        
                moves_relative.remove(0);
                
                // TODO: remove
//                if (moves_relative.isEmpty()) {
////                    PublicFunctions.insertToAS(game, new DisplayTextIntro(game, "An oppressive force fills the air...",
////                                                                          null, null, false, new DoneAction()));
//
//                }
                return;
            }

//            if (this.timer == 0) {
//                // white screen 'flash'
//                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-1f));
//                game.floatingBatch.setShader(shader);
//            }
//            if (this.timer == 2) {
//                // white screen 'flash'
//                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(0f));
//                game.floatingBatch.setShader(shader);
//            }

            // start next action (pokemon cry) but continue animation
            if (this.timer == 30) {
                // mega gengar cry
                PublicFunctions.insertToAS(game, new PlaySound(game.battle.oppPokemon.name,
                                                 new DoneAction()));
            }

            // TODO: remove
//            if (this.timer == 380) {
//                // remove the text box
//                game.actionStack.remove(game.displayTextAction);
//                game.displayTextAction = null;
//                PublicFunctions.insertToAS(game, new DisplayTextIntro(game, "MEWTWO unleashes its full power!",
//                                                                      null, null, false, new DoneAction()));
//            }

            //start of flash anim
            else if (this.timer == 20) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(.7f));
                game.floatingBatch.setShader(shader);
            }
            else if (this.timer == 20 + 24) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(.6f));
                game.floatingBatch.setShader(shader);
            }
            else if (this.timer == 20 + 24*2) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(.4f));
                game.floatingBatch.setShader(shader);
            }
            else if (this.timer == 20 + 24*3) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(.2f));
                game.floatingBatch.setShader(shader);
            }
            else if (this.timer == 20 + 24*4) {
                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(0f));
                game.floatingBatch.setShader(shader);
            }
//            else if (this.timer == 490 + 6*4) {
//                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-.2f));
//                game.floatingBatch.setShader(shader);
//            }
//            else if (this.timer == 490 + 6*5) {
//                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-.4f));
//                game.floatingBatch.setShader(shader);
//            }
//            else if (this.timer == 490 + 6*6) {
//                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-.6f));
//                game.floatingBatch.setShader(shader);
//            }
//            else if (this.timer == 490 + 6*7) {
//                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-.8f));
//                game.floatingBatch.setShader(shader);
//            }
//            else if (this.timer == 490 + 6*8) {
//                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-.85f));
//                game.floatingBatch.setShader(shader);
//            }
//            else if (this.timer == 490 + 6*9) {
//                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-.90f));
//                game.floatingBatch.setShader(shader);
//            }
//            else if (this.timer == 490 + 6*10) {
//                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-.95f));
//                game.floatingBatch.setShader(shader);
//            }
//            else if (this.timer == 490 + 6*11) {
//                // stop displaying text box
//                game.actionStack.remove(game.displayTextAction);
//                game.displayTextAction = null;
//
//                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-1f));
//                game.floatingBatch.setShader(shader);
//            }
//            else if (this.timer == 490 + 6*19 + 3) {
//                ShaderProgram shader = new ShaderProgram(this.vertexShader, this.getShader(-.5f));
//                game.floatingBatch.setShader(shader);
//            }
//            else if (this.timer == 490 + 6*19 + 5) {
//                game.floatingBatch.setShader(null);
//            }
//            else if (this.timer == 620) {
//                SpecialBattleMegaGengar.DrawBreathingSprite.shouldBreathe = true;
//            }
            

            // battle text
            if (this.timer == 20) {
                PublicFunctions.insertToAS(game, new DisplayTextIntro(game, "MEGA GENGAR attacked!",
                                                                      null, null, false, new DoneAction()));
            }

            //start of switch to pokemon
            if (this.timer == 120 + 24*4) {
                PublicFunctions.insertToAS(game, this.nextAction);
            }

            if (this.timer >= 150 + 24*4) {
    //            // remove the text box
                game.actionStack.remove(game.displayTextAction);
                game.displayTextAction = null;
                game.actionStack.remove(this);
                SpecialBattleMegaGengar.DrawBreathingSprite.shouldBreathe = true;
            }
            this.timer++;
        }
        
        public IntroAnim(Game game, Action nextAction) {
            
            this.nextAction = nextAction;
            this.moves_relative = new ArrayList<Vector2>();

            //animation to play
            for (int i = 0; i < 72*2; i++) {
                moves_relative.add(new Vector2(1,0));
            }

            game.player.battleSprite.setPosition(175+1-8-2,71+1-10);
            game.player.battleSprite.setScale(2);
            game.battle.oppPokemon.sprite.setPosition(-30-4-1-14,106+2-5-15);
            
            this.vertexShader = "attribute vec4 a_position;\n"
                    + "attribute vec4 a_color;\n"
                    + "attribute vec2 a_texCoord;\n"
                    + "attribute vec2 a_texCoord0;\n"
                        
                    + "uniform mat4 u_projTrans;\n"
                                            
                    + "varying vec4 v_color;\n"
                    + "varying vec2 v_texCoords;\n"
                                            
                    + "void main()\n"
                    + "{\n"
                    + "    v_color = a_color;\n"
                    + "    v_texCoords = a_texCoord0;\n"
                    + "    gl_Position =  u_projTrans * a_position;\n"
                    // below can be used to translate screen pixels (for attacks, etc
//                    + "    gl_Position =  u_projTrans * vec4(a_position.x, a_position.y, a_position.z, 1.0);\n"
                    + "}\n";
        }

        String getShader(float level) {
            
            String shader = "precision mediump float;\n"

                            + "varying vec4 v_color;\n"
                            + "varying vec2 v_texCoords;\n"
                            + "uniform sampler2D u_texture;\n"
                            + "uniform mat4 u_projTrans;\n"
                            
                            + "bool equals(float a, float b) {\n"
                            + "    return abs(a-b) < 0.0001;\n"
                            + "}\n"
                            
                            + "void main() {\n"
                            + "    vec4 color = texture2D (u_texture, v_texCoords) * v_color;\n"

                            + "    float level = "+level+";\n" // can't do +- or -+ inline
                            + "    if(color.r >= .9 && color.g >= .9 && color.b >= .9) {\n"
                            + "           color = vec4(color.r, color.g, color.b, color.a);\n"
                            + "    }\n"
                            + "    else {\n"
                            + "        color = vec4(color.r-level, color.g-level, color.b-level, color.a);\n"
                            + "    }\n"
                            + "    gl_FragColor = color;\n"
                            + "}\n";

            // version that keeps everything black/white (didnt like as much)
//            String shader = "precision mediump float;\n"
//
//                            + "varying vec4 v_color;\n"
//                            + "varying vec2 v_texCoords;\n"
//                            + "uniform sampler2D u_texture;\n"
//                            + "uniform mat4 u_projTrans;\n"
//                            
//                            + "bool equals(float a, float b) {\n"
//                            + "    return abs(a-b) < 0.0001;\n"
//                            + "}\n"
//                            
//                            + "void main() {\n"
//                            + "    vec4 color = texture2D (u_texture, v_texCoords) * v_color;\n"
//
//                            + "    float level = ("+level+"+1.0)/2.0;\n" // can't do +- or -+ inline
//                            + "    if(color.r >= .9 && color.g >= .9 && color.b >= .9) {\n"
//                            + "           color = vec4(color.r, color.g, color.b, color.a);\n"
//                            + "    }\n"
//                            + "    else if(color.r < level || color.g < level || color.b < level) {\n"
//                            + "        color = vec4(0, 0, 0, color.a);\n"
//                            + "    }\n"
//                            + "    else {\n"
//                            + "        color = vec4(1, 1, 1, color.a);\n"
//                            + "    }\n"
//                            + "    gl_FragColor = color;\n"
//                            + "}\n";
            return shader;
        }
    }
    
    class BattleIntro1 extends Action {
        
        ArrayList<Sprite> frames;
        Sprite frame;
        
        

        public int layer = 139;
        public int getLayer(){return this.layer;}

        public String getCamera() {return "gui";};
        
        @Override
        public void step(Game game) {
            
            //get next frame
            this.frame = frames.get(0);
            
            if (this.frame != null) {
                //gui version
                    //this.frame.setScale(3); //scale doesn't work in batch.draw //used these when scaling mattered
                    //this.frame.setPosition(16*10,16*9);
                this.frame.draw(game.floatingBatch);
                //map version
                //game.batch.draw(this.frame, 16, -16);
            }
            
            frames.remove(0);
            
            //if done with anim, do nextAction
            if (frames.isEmpty()) {
                PublicFunctions.insertToAS(game, this.nextAction);
                game.actionStack.remove(this);
                //avoid lag
                this.nextAction.step(game);
                return;
            }
        }
        
        public BattleIntro1(Action nextAction) {
            
            this.nextAction = nextAction;
            this.frames = new ArrayList<Sprite>();
            Texture text1 = new Texture(Gdx.files.internal("battle/battle_intro_anim1_sheet1.png"));
            
            //animation to play
            for (int i = 0; i < 28; i++) {
                frames.add(new Sprite(text1, i*160, 0, 160, 144));
            }

            for (int i = 0; i < 42*3; i++) {
                frames.add(new Sprite(text1, 27*160, 0, 160, 144));
            }
        }
    }

    class DrawBattle1 extends DrawBattle {

        public int layer = 130;
        public int getLayer(){return this.layer;}
        public String getCamera() {return "gui";};

        Sprite bgSprite;
        
        @Override
        public void step(Game game) {
            //debug
//            this.helperSprite.draw(game.floatingBatch);

            //need bg under the animation
//            this.bgSprite.draw(game.floatingBatch);
            
            // TODO: remove
//            if (shouldDrawOppPokemon) {
//                game.battle.oppPokemon.sprite.draw(game.floatingBatch);
//            }
            game.player.battleSprite.draw(game.floatingBatch);
        }
        
        public DrawBattle1(Game game) {
            super(game);
//            Texture text = new Texture(Gdx.files.internal("battle/battle_bg3.png"));
//            this.bgSprite = new Sprite(text, 0, 0, 160, 144);
        }
    }
}

class PokemonIntroAnim extends Action {
    public int layer = 140;
    public int getLayer(){return this.layer;}
    public String getCamera() {return "gui";};
    
    
    
    int currFrame = 0;
    Sprite originalSprite;
    boolean firstStep = true;

    @Override
    public void step(Game game) {
        if (this.firstStep) {
            this.originalSprite = game.battle.oppPokemon.sprite;
            this.firstStep = false;
        }
        if (this.currFrame >= game.battle.oppPokemon.introAnim.size()) {
            game.battle.oppPokemon.sprite = this.originalSprite;
            game.actionStack.remove(this);
            PublicFunctions.insertToAS(game, this.nextAction);
            return;
        }
        // play intro anim frame by frame
        game.battle.oppPokemon.sprite = game.battle.oppPokemon.introAnim.get(this.currFrame);
        game.battle.oppPokemon.sprite.setPosition(this.originalSprite.getX(), this.originalSprite.getY());
        this.currFrame++;
    }
    
    public PokemonIntroAnim(Action nextAction) {
        this.nextAction = nextAction;
    }
}



class AttackAnim extends Action {

    public int layer = 500;  // ensure that this triggers before other actions
    public int getLayer(){return this.layer;};

    String attackName;
    boolean isFriendly;

    @Override
    public void step(Game game) {
        // TODO: move the Battle_Actions.getAttackAction code here
        game.actionStack.remove(this);
        PublicFunctions.insertToAS(game, Battle_Actions.getAttackAction(game, attackName, isFriendly, nextAction));
    }
    
    public AttackAnim(Game game, String attackName, boolean isFriendly, Action nextAction) {
        this.nextAction = nextAction;
        this.attackName = attackName;
        this.isFriendly = isFriendly;
    }
}


//return the type of battle the the player must enter
 //safari battle (no pokemon),
 //throw out first pkmn

class Battle_Actions extends Action {

    // TODO: this catch calculator is for gen1 only, also want gen2
    // dupe of a fn in draw safari menu action, put here bc the one in safari menu has demo code
    public static Action calcIfCaught(Game game, Action nextAction) {

        //using http://bulbapedia.bulbagarden.net/wiki/Catch_rate#Capture_method_.28Generation_I.29
         //also use http://www.dragonflycave.com/safarizone.aspx
        //not sure where 'ball used' will be stored. probly some inventory location, like currItem (in inventory)

        int maxRand = 150; //different per-ball
        int randomNum = game.map.rand.nextInt(maxRand+1); //+1 to include upper bound
        int statusValue = 0; //different depending on oppPokemon's status
        boolean breaksFree = false;

        int ball = 15; //8 if great ball
        //demo code
        int adrenaline = game.player.adrenaline;
        if (adrenaline > 25) {
            adrenaline = 25;
        }
        //ball = ball - adrenaline; 
        int modFactor = 100;//128; - want 5 adr to catch all easy, but not medium or hard.
        int f = (int)Math.floor((game.battle.oppPokemon.currentStats.get("catchRate") * 255 * 4) / (modFactor*ball)); //modify 128 to make game harder

        //int f = (int)Math.floor((game.battle.oppPokemon.maxStats.get("hp") * 255 * 4) / (game.battle.oppPokemon.currentStats.get("hp") * ball));
        
        //left out calculation here based on status values
         //demo - leave out status value
        //notes - adr seems to take effec too fast. also, pkmn in general are too hard to catch 
         //at beginning. shift factor down, and make adr*10
        if (randomNum - statusValue > game.battle.oppPokemon.currentStats.get("catchRate") && false) {
            breaksFree = true;
            System.out.println("(randomNum - statusValue / catchRate): ("+String.valueOf(randomNum - statusValue)+" / "+String.valueOf(game.battle.oppPokemon.currentStats.get("catchRate"))+")");
        }
        else {
            int randomNum_M = game.map.rand.nextInt(255+1);
            
            
            //randomNum_M = randomNum_M - adrenaline*20;


            if (f+(adrenaline*10) >= randomNum_M) { //demo code
                breaksFree = false;
            }
            else {
                breaksFree = true;
            }
            System.out.println("(randomNum_M / f / adr): ("+String.valueOf(randomNum_M)+" / "+String.valueOf(f)+" / +"+String.valueOf(adrenaline*10)+")");
        }
        

        //simplify and put above
        if (breaksFree == false) { //ie was caught 
            return new catchPokemon_wigglesThenCatch(game, nextAction);
        }

        //else, ie breaksFree = true
        
        int d = game.battle.oppPokemon.currentStats.get("catchRate") * 100 / maxRand;
                //, where the value of Ball is 255 for the PokÈ Ball, 200 for the Great Ball, or 150 for other balls
        if (d >= 256) {
            //shake 3 times before breaking free
            return new catchPokemon_wiggles3Times(game, new PrintAngryEating(game, new ChanceToRun(game, nextAction) ) );
        }

        int s = 0;//status thing again
        int x = d * f / 255 + s;
        
        if (x < 10) {
            //ball misses
            return new catchPokemon_miss(game, new PrintAngryEating(game, new ChanceToRun(game, nextAction) ) );
        }
        else if (x < 30) {
            //ball shakes once
            return new catchPokemon_wiggles1Time(game, new PrintAngryEating(game, new ChanceToRun(game, nextAction) ) );
        }
        else if (x < 70) {
            //ball shakes twice
            return new catchPokemon_wiggles2Times(game, new PrintAngryEating(game, new ChanceToRun(game, nextAction) ) );
        }
        //ball shakes three times before pkmn gets free
        
        //System.out.println("x: "+String.valueOf(x));
        //System.out.println("Shake three times: "+String.valueOf(x));
        
        return new catchPokemon_wiggles3Times(game, new PrintAngryEating(game, new ChanceToRun(game, nextAction) ) );
    }
    
    public static Action getAttackAction(Game game, String attackName, boolean isFriendly, Action nextAction) {
        
        //construct default attack?
        // TODO: the non-loaded ones are broken now, need to do DisplayText.Clear()
        
        
        int power = 40;
        int accuracy = 100;
        
        if (attackName.equals("Aurora Beam")) {
            //normally return new attack here
            power = 65; accuracy = 100;
        }
        else if (attackName.equals("Clamp")) {
            //normally return new attack here
            power = 35; accuracy = 85;
        }
        else if (attackName.equals("Supersonic")) {
            //normally return new attack here
            power = 0; accuracy = 55;
        }
        else if (attackName.equals("Withdraw")) {
            //normally return new attack here
            power = 20; accuracy = 100;
        }
        else if (attackName.equals("Struggle")) {
            //normally return new attack here
            power = 50; accuracy = 100;
        }
        else if (attackName.equals("Psychic")) {
            if (isFriendly) {
                return new Battle_Actions.Psychic(game,
                                                  game.player.currPokemon,
                                                  game.battle.oppPokemon,
                                                  false,
                                                  nextAction);
            }
            else {
                return new Battle_Actions.Psychic(game,
                                                  game.battle.oppPokemon,
                                                  game.player.currPokemon,
                                                  false,
                                                  nextAction);
            }
        }
        else if (attackName.equals("Mewtwo_Special1")) {
            return new Battle_Actions.Mewtwo_Special1(game,
                                                      game.battle.oppPokemon,
                                                      game.player.currPokemon,
                                                      nextAction);
        }
        else if (attackName.equals("Night Shade")) {
            if (isFriendly) {
                return new Battle_Actions.Psychic(game,
                                                  game.player.currPokemon,
                                                  game.battle.oppPokemon,
                                                  true,
                                                  nextAction);
            }
            else {
                return new Battle_Actions.Psychic(game,
                                                  game.battle.oppPokemon,
                                                  game.player.currPokemon,
                                                  true,
                                                  nextAction);
            }
        }
        else if (attackName.equals("Slash")) {
            if (isFriendly) {
                // TODO
                return new DefaultAttack(game, power, accuracy, nextAction);
            }
            else {
                return new Battle_Actions.Slash(game, game.battle.oppPokemon, game.player.currPokemon, nextAction);
            }
        }
        else if (attackName.equals("Shadow Claw")) {
            if (isFriendly) {
                // TODO
                return new DefaultAttack(game, power, accuracy, nextAction);
            }
            else {
                return new Battle_Actions.ShadowClaw(game, game.battle.oppPokemon, game.player.currPokemon, nextAction);
            }
        }
        else if (attackName.equals("Lick")) {
            if (isFriendly) {
                // TODO
                return new DefaultAttack(game, power, accuracy, nextAction);
            }
            else {
                return new Battle_Actions.Lick(game, game.battle.oppPokemon, game.player.currPokemon, nextAction);
            }
        }
        else {
            if (game.battle.oppPokemon.name.equals("Mewtwo")) {
                nextAction = new DisplayText(game, "A wave of psychic power unleashes!", null, true, 
                             Battle_Actions.getAttackAction(game, "Mewtwo_Special1", !isFriendly,
                             new DepleteFriendlyHealth(game.player.currPokemon, 
                             new WaitFrames(game, 30, 
                             new DisplayText.Clear(game,
                             new WaitFrames(game, 3, nextAction))))));
            }
            String effectiveness;
            String text_string = "";
            Action attack;
            if (isFriendly) {
                // TODO: string based on effectiveness
                // TODO: 'no effect' attacks
                // attack data loaded from Crystal
                String attackType = game.battle.attacks.get(attackName.toLowerCase()).type;
                float multiplier = game.battle.gen2TypeEffectiveness.get(attackType).get(game.battle.oppPokemon.types.get(0).toLowerCase());
                if (game.battle.oppPokemon.types.size() > 1) {
                    multiplier *= game.battle.gen2TypeEffectiveness.get(attackType).get(game.battle.oppPokemon.types.get(1).toLowerCase());
                }
                if (multiplier > 1f) {
                    effectiveness = "super_effective";
                    text_string = "It' super- effective!";
                }
                else if (multiplier == 1f) {
                    effectiveness = "neutral_effective";
                }
                else {
                    effectiveness = "not_very_effective";
                    text_string = "It' not very effective...";
                }
                
                attack =  new LoadAndPlayAttackAnimation(game, attackName, game.battle.oppPokemon, 
                          new LoadAndPlayAttackAnimation(game, effectiveness, game.battle.oppPokemon,
                          new DepleteEnemyHealth(game,
                          new WaitFrames(game, 13,
                          !effectiveness.equals("neutral_effective") ?
                              new DisplayText.Clear(game,
                              new WaitFrames(game, 3,
                              new DisplayText(game,
                                              text_string,
                                              null,
                                              true,
                                              true,
                              null)))
                          :
                              null))));
                // check if attack traps target pokemon
                if (game.battle.oppPokemon.trappedBy == null &&
                    attackName.toLowerCase().equals("whirlpool") ||
                    attackName.toLowerCase().equals("fire spin") ||
                    attackName.toLowerCase().equals("wrap") ||
                    attackName.toLowerCase().equals("clamp")) {
                    // 2-5 turns for trap
                    game.battle.oppPokemon.trappedBy = attackName.toLowerCase();
                    game.battle.oppPokemon.trapCounter = game.map.rand.nextInt(4) + 2;
                    attack.appendAction(new DisplayText.Clear(game,
                                        new WaitFrames(game, 3,
                                        new DisplayText(game,
                                                        game.battle.oppPokemon.name.toUpperCase()+" was trapped!",
                                                        null,
                                                        true,
                                                        true,
                                        null))));
                }
            }
            else {
                String attackType = game.battle.attacks.get(attackName.toLowerCase()).type;
                float multiplier = game.battle.gen2TypeEffectiveness.get(attackType).get(game.player.currPokemon.types.get(0).toLowerCase());
                if (game.player.currPokemon.types.size() > 1) {
                    multiplier *= game.battle.gen2TypeEffectiveness.get(attackType).get(game.player.currPokemon.types.get(1).toLowerCase());
                }
                if (multiplier > 1f) {
                    effectiveness = "super_effective";
                    text_string = "It' super- effective!";
                }
                else if (multiplier == 1f) {
                    effectiveness = "neutral_effective";
                }
                else {
                    effectiveness = "not_very_effective";
                    text_string = "It' not very effective...";
                }
                attack = new LoadAndPlayAttackAnimation(game, attackName, game.player.currPokemon,
                         new LoadAndPlayAttackAnimation(game, effectiveness, game.player.currPokemon,
                         new DepleteFriendlyHealth(game.player.currPokemon,
                         new WaitFrames(game, 13,
                         !effectiveness.equals("neutral_effective") ?
                             new DisplayText.Clear(game,
                             new WaitFrames(game, 3,
                             new DisplayText(game,
                                             text_string,
                                             null,
                                             true,
                                             true,
                             new WaitFrames(game, 3,
                             null))))
                         :
                             null))));
                // check if attack traps target pokemon
                if (game.player.currPokemon.trappedBy == null &&
                    attackName.toLowerCase().equals("whirlpool") ||
                    attackName.toLowerCase().equals("fire spin") ||
                    attackName.toLowerCase().equals("wrap") ||
                    attackName.toLowerCase().equals("clamp")) {
                    // 2-5 turns for trap
                    game.player.currPokemon.trappedBy = attackName.toLowerCase();
                    game.player.currPokemon.trapCounter = game.map.rand.nextInt(4) + 2;
                    attack.appendAction(new DisplayText.Clear(game,
                                        new WaitFrames(game, 3,
                                        new DisplayText(game,
                                                        game.player.currPokemon.name.toUpperCase()+" was trapped!",
                                                        null,
                                                        true,
                                                        true,
                                        null))));
                }
            }
            attack.appendAction(nextAction);
            return attack;
        }

        
        if (isFriendly) {
            return new DefaultAttack(game, power, accuracy, nextAction);
        }
        else {
            return new DefaultEnemyAttack(game.battle.oppPokemon, game.player.currPokemon, power, accuracy, nextAction);
        }
        
    }

    public static Action get(Game game) {
        
        //if player has no pokemon, encounter is safari zone style
        if (game.player.pokemon.isEmpty()) {

            return new SplitAction(new BattleIntro(
                                   new BattleIntro_anim1(
                                   new SplitAction(new DrawBattle(game),
                                                   new BattleAnim_positionPlayers(game, 
                                                   new PlaySound(game.battle.oppPokemon.name, 
                                                   new DisplayText(game, "Wild "+game.battle.oppPokemon.name.toUpperCase()+" appeared!", null, null, 
                                                   new WaitFrames(game, 39,
                                                   //demo code - wildly confusing, but i don't want to write another if statement
                                                    game.player.adrenaline > 0 ? new DisplayText(game, ""+game.player.name+" has ADRENALINE "+Integer.toString(game.player.adrenaline)+"!", null, null,
                                                        new PrintAngryEating(game, //for demo mode, normally left out
                                                                new DrawBattleMenu_SafariZone(game, new DoneAction())
                                                            )
                                                        )
                                                    : 
                                                   new PrintAngryEating(game, //for demo mode, normally left out
                                                   new DrawBattleMenu_SafariZone(game, new DoneAction())    
                                   )))))))),
                   new DoneAction()
                   );
        }
        //throw out first pokemon in player.pokemon
        // play intro anim if pokemon crystal
        else if (game.battle.oppPokemon.generation == Pokemon.Generation.CRYSTAL) {
            Action triggerAction;
            if (game.player.currPokemon.generation == Pokemon.Generation.RED) {
                triggerAction = new PlaySound(game.player.currPokemon.name, 
                                new WaitFrames(game, 6,
                                new DrawBattleMenu_Normal(game, new DoneAction())
                                ));
            }
            else {
                Action afterTrigger = new WaitFrames(game, 15,
                                      new DrawBattleMenu_Normal(game,
                                      new DoneAction()
                                      ));
                triggerAction = new PlaySound(game.player.currPokemon, 
                                new WaitFrames(game, 6,
                                new DrawFriendlyHealth(game,
                                afterTrigger)));
            }
            return new BattleIntro(
                   new BattleIntro_anim1(
                   new SplitAction(new DrawBattle(game),
                   new BattleAnim_positionPlayers(game, 
                   new SplitAction(new WaitFrames(game, 4,
                                      new PlaySound(game.battle.oppPokemon,
                                      new DoneAction())), 
                   new PokemonIntroAnim(
                   new WaitFrames(game, 11,
                   new DisplayText(game, "Wild "+game.battle.oppPokemon.name.toUpperCase()+" appeared!", null, null, 
                   new SplitAction(new WaitFrames(game, 1,
                                   new DrawEnemyHealth(game)),
                   new WaitFrames(game, 39,
                   new MovePlayerOffScreen(game, 
                   new DisplayText(game, "Go! "+game.player.currPokemon.name.toUpperCase()+"!", null, triggerAction,
                   game.player.currPokemon.generation == Pokemon.Generation.RED ?  // basically an if block
                       new SplitAction(new DrawFriendlyHealth(game),
                       new ThrowOutPokemon(game, //this draws pkmn sprite permanently until battle ends, ie until game.battle.drawAction == null
                       triggerAction
                       ))
                   :
                       new ThrowOutPokemonCrystal(game, //this draws pkmn sprite permanently until battle ends, ie until game.battle.drawAction == null
                       triggerAction
                       )
                   ))))))))))));
        }
        // below is red/blue intro anim
        // TODO: what if opp pokemon is crystal, and yours is red? or vice-versa
        else {
            Action triggerAction = new PlaySound(game.player.currPokemon.name, 
                                   new WaitFrames(game, 6,
                                   new DrawBattleMenu_Normal(game, new DoneAction())
                                   ));
            return new BattleIntro(
                   new BattleIntro_anim1(
                   new SplitAction(
                   new DrawBattle(game),
                   new BattleAnim_positionPlayers(game,
                   new PlaySound(game.battle.oppPokemon.name,
                   new DisplayText(game, "Wild "+game.battle.oppPokemon.name.toUpperCase()+" appeared!", null, null,
                   new SplitAction(new WaitFrames(game, 1,
                                   new DrawEnemyHealth(game)),
                   new WaitFrames(game, 39,
                   new MovePlayerOffScreen(game,
                   new DisplayText(game, "Go! "+game.player.currPokemon.name.toUpperCase()+"!", null, triggerAction, 
                   new SplitAction(new DrawFriendlyHealth(game),
                   new ThrowOutPokemon(game, //this draws pkmn sprite permanently until battle ends, ie until game.battle.drawAction == null
                                       triggerAction
                   ))))))))))));
        }
        
        //game.actionStack.remove(this);
        
    }

    

    // Lick attack animation
    static class Lick extends Action {

        ArrayList<Vector2> positions;
        Vector2 position;
        ArrayList<Vector2> screenPositions;
        Vector2 currPosition;
        ArrayList<Sprite> sprites;
        Sprite sprite;
        ArrayList<Integer> repeats;
        String sound;
        ArrayList<String> sounds;

        Sprite blockSprite;

        // TODO: has to be higher layer than ThrowOutPokemon()
        // why is ThrowOutPokemon in such a high layer?
        public int layer = 108;
        public int getLayer(){return this.layer;}

        public String getCamera() {return "gui";};

        

        Sprite helperSprite; //just for helping me position the animation. delete later.
        
        Pokemon attacker;
        Pokemon target;

        Pixmap pixmap;
                
        // gen 7 properties
        int power = 30;
        int accuracy = 100;
        
        @Override
        public void step(Game game) {

            //set sprite position
            //if done with anim, do nextAction
            if (this.repeats.isEmpty()) {
                if (!this.screenPositions.isEmpty()) {
                    this.currPosition = this.screenPositions.get(0);
                    // screen wiggle
                    //TODO: *3 b/c screen is scaled, alternative approach would be nice
                    this.pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, 160*3, 144*3);
                    for (int j=0; j < 144; j++) {
                        for (int i=0; i < 160; i++) {
                            this.blockSprite.setColor(new Color(this.pixmap.getPixel(i*3, j*3)));
                            // not using this.currPosition.y here, but may in future animations
                            this.blockSprite.setPosition(i + this.currPosition.x, j + this.currPosition.y);
                            this.blockSprite.draw(game.floatingBatch);
                        }
                    }
                    this.screenPositions.remove(0);
                }
                else {
                    //assign damage to target pkmn
                    int currHealth = this.target.currentStats.get("hp");
                    //TODO - correct damage calculation
                    int finalHealth = currHealth - this.power;
                    if (finalHealth < 0) {finalHealth = 0;} //make sure finalHealth isn't negative
                    this.target.currentStats.put("hp", finalHealth);

                    game.actionStack.remove(this);
                    PublicFunctions.insertToAS(game, this.nextAction);
                }
                return;
            }
            
            //get next sound, play it
            this.sound = this.sounds.get(0);
            if (this.sound != null) {
                PublicFunctions.insertToAS(game, new PlaySound(this.sound, new DoneAction()));
                this.sounds.set(0, null); //don't play same sound over again
            }

            //get next frame
            this.sprite = sprites.get(0);

            //debug
//            this.helperSprite.draw(game.floatingBatch); 

            //draw current sprite
            if (this.sprite != null) {
                this.sprite.setPosition(position.x, position.y);
                this.sprite.draw(game.floatingBatch);            
            }

            //debug
//            if (this.repeats.size() == 5) { 
//                return;
//            }

            //repeat sprite/pos for current object for 'frames[0]' number of frames.
            if (this.repeats.get(0) > 0) {
                this.repeats.set(0, this.repeats.get(0) - 1);
            }
            else {
                //since position is relative, only update once each time period
                this.position = this.position.add(positions.get(0));
                positions.remove(0);
                sprites.remove(0);
                repeats.remove(0);
                sounds.remove(0);
            }
        }

        public Lick(Game game,
                     Pokemon attacker,
                     Pokemon target,
                     Action nextAction) {

            this.attacker = attacker;
            this.target = target;
            this.nextAction = nextAction;

            // single pixel sprite used for drawing the effect
            Texture text = new Texture(Gdx.files.internal("battle/pixel1.png"));
            this.blockSprite = new Sprite(text, 0, 0, 1, 1);

            this.repeats = new ArrayList<Integer>();
            this.repeats.add(19-1); //wait 19 frames
            this.repeats.add(13-1); //orb appears 13 frames
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    this.repeats.add(6-1);
                }
                this.repeats.add(7-1);
            }
            this.repeats.add(23-1); //wait 23 frames

            this.position = new Vector2(0, 0);
            this.positions = new ArrayList<Vector2>();
            for (int i = 0; i < 9; i++) {
                this.positions.add(new Vector2(0,0));
            }

            // screen movement after attack
            this.screenPositions = new ArrayList<Vector2>();
            // 9 frames nothing
            for (int i = 0; i < 9; i++) {
                this.screenPositions.add(new Vector2(0,0));
            }
            // 5 frame move 2 right
            for (int i = 0; i < 5; i++) {
                this.screenPositions.add(new Vector2(2,0));
            }
            // 9 frame move back
            for (int i = 0; i < 9; i++) {
                this.screenPositions.add(new Vector2(0,0));
            }
            // 4 frame move 1 right
            for (int i = 0; i < 4; i++) {
                this.screenPositions.add(new Vector2(1,0));
            }
            // 19 frame move back
            for (int i = 0; i < 19; i++) {
                this.screenPositions.add(new Vector2(0,0));
            }

            text = new Texture(Gdx.files.internal("attacks/enemy_lick_sheet1.png"));

            this.sprites =  new ArrayList<Sprite>();
            this.sprites.add(null); //draw nothing
            for (int i = 0; i < 7; i++) {
                this.sprites.add(new Sprite(text, 160*i, 0, 160, 144));
            }
            this.sprites.add(null); //draw nothing

            //sounds to play
            this.sounds = new ArrayList<String>();
            this.sounds.add("lick1");
            for (int i = 0; i < 7; i++) { 
                this.sounds.add(null);
            }
            this.sounds.add("hit_normal1");

//            text = new Texture(Gdx.files.internal("attacks/enemy_slash/helper1.png"));
//            this.helperSprite = new Sprite(text, 0, 0, 160, 144);
        }
    }
    
    // Slash attack animation
    static class Slash extends Action {

        ArrayList<Vector2> positions;
        Vector2 position;
        ArrayList<Vector2> screenPositions;
        Vector2 currPosition;
        ArrayList<Sprite> sprites;
        Sprite sprite;
        ArrayList<Integer> repeats;
        String sound;
        ArrayList<String> sounds;

        Sprite blockSprite;

        // TODO: has to be higher layer than ThrowOutPokemon()
        // why is ThrowOutPokemon in such a high layer?
        public int layer = 108;
        public int getLayer(){return this.layer;}

        public String getCamera() {return "gui";};

        

        Sprite helperSprite; //just for helping me position the animation. delete later.
        
        Pokemon attacker;
        Pokemon target;

        Pixmap pixmap;
                
        // gen 7 properties
        int power = 70;
        int accuracy = 100;
        
        @Override
        public void step(Game game) {

            //set sprite position
            //if done with anim, do nextAction
            if (this.repeats.isEmpty()) {
                if (!this.screenPositions.isEmpty()) {
                    this.currPosition = this.screenPositions.get(0);
                    // screen wiggle
                    //TODO: *3 b/c screen is scaled, alternative approach would be nice
                    this.pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, 160*3, 144*3);
                    for (int j=0; j < 144; j++) {
                        for (int i=0; i < 160; i++) {
                            this.blockSprite.setColor(new Color(this.pixmap.getPixel(i*3, j*3)));
                            // not using this.currPosition.y here, but may in future animations
                            this.blockSprite.setPosition(i + this.currPosition.x, j + this.currPosition.y);
                            this.blockSprite.draw(game.floatingBatch);
                        }
                    }
                    this.screenPositions.remove(0);
                }
                else {
                    //assign damage to target pkmn
                    int currHealth = this.target.currentStats.get("hp");
                    //TODO - correct damage calculation
                    int finalHealth = currHealth - this.power;
                    if (finalHealth < 0) {finalHealth = 0;} //make sure finalHealth isn't negative
                    this.target.currentStats.put("hp", finalHealth);

                    game.actionStack.remove(this);
                    PublicFunctions.insertToAS(game, this.nextAction);
                }
                return;
            }
            
            //get next sound, play it
            this.sound = this.sounds.get(0);
            if (this.sound != null) {
                PublicFunctions.insertToAS(game, new PlaySound(this.sound, new DoneAction()));
                this.sounds.set(0, null); //don't play same sound over again
            }

            //get next frame
            this.sprite = sprites.get(0);
            
            //debug
//            this.helperSprite.draw(game.floatingBatch); 

            //draw current sprite
            if (this.sprite != null) {
                this.sprite.setPosition(position.x, position.y);
                this.sprite.draw(game.floatingBatch);            
            }
            
            //debug
//            if (this.repeats.size() == 5) { 
//                return;
//            }

            
            
            //repeat sprite/pos for current object for 'frames[0]' number of frames.
            if (this.repeats.get(0) > 0) {
                this.repeats.set(0, this.repeats.get(0) - 1);
            }
            else {
                //since position is relative, only update once each time period
                this.position = this.position.add(positions.get(0));
                positions.remove(0);
                sprites.remove(0);
                repeats.remove(0);
                sounds.remove(0);
                
            }
        }

        public Slash(Game game,
                     Pokemon attacker,
                     Pokemon target,
                     Action nextAction) {

            this.attacker = attacker;
            this.target = target;
            this.nextAction = nextAction;

            // single pixel sprite used for drawing the effect
            Texture text = new Texture(Gdx.files.internal("battle/pixel1.png"));
            this.blockSprite = new Sprite(text, 0, 0, 1, 1);
    
            text = new Texture(Gdx.files.internal("attacks/enemy_slash_sheet1.png"));
            
            //consider doing relative positions from now on
            this.position = new Vector2(16, 40);
            this.positions = new ArrayList<Vector2>();
            for (int i = 0; i < 6; i++) {
                this.positions.add(new Vector2(0,0));
            }

            // screen movement after attack
            this.screenPositions = new ArrayList<Vector2>();
            // 9 frames nothing
            for (int i = 0; i < 9; i++) {
                this.screenPositions.add(new Vector2(0,0));
            }
            // 5 frame move 2 right
            for (int i = 0; i < 5; i++) {
                this.screenPositions.add(new Vector2(2,0));
            }
            // 9 frame move back
            for (int i = 0; i < 9; i++) {
                this.screenPositions.add(new Vector2(0,0));
            }
            // 4 frame move 1 right
            for (int i = 0; i < 4; i++) {
                this.screenPositions.add(new Vector2(1,0));
            }
            // 19 frame move back
            for (int i = 0; i < 19; i++) {
                this.screenPositions.add(new Vector2(0,0));
            }
            
            
            this.sprites =  new ArrayList<Sprite>();
            this.sprites.add(null); //draw nothing
            for (int i = 0; i < 4; i++) {
                this.sprites.add(new Sprite(text, 48*i, 0, 48, 48));
            }
            this.sprites.add(null); //draw nothing

            this.repeats = new ArrayList<Integer>();
            // TODO: is triggerAction working like it should?
            this.repeats.add(20-1); //wait 20 frames
            for (int i = 0; i < 4; i++) {
                //7 frames per image
                this.repeats.add(7-1);
            }
            this.repeats.add(6-1); //wait 6 frames

            //sounds to play
            this.sounds = new ArrayList<String>();
            this.sounds.add("slash1");
            for (int i = 0; i < 5; i++) { 
                this.sounds.add(null);
            }

//            text = new Texture(Gdx.files.internal("attacks/enemy_slash/helper1.png"));
//            this.helperSprite = new Sprite(text, 0, 0, 160, 144);

        }
    }
    

    // ShadowClaw attack animation
    // gen1-ifying this attack anim
    static class ShadowClaw extends Action {

        ArrayList<Vector2> positions;
        Vector2 position;
        ArrayList<Vector2> screenPositions;
        Vector2 currPosition;
        ArrayList<Sprite> sprites;
        Sprite sprite;
        ArrayList<Integer> repeats;
        String sound;
        ArrayList<String> sounds;
        String currShaderVal;
        ArrayList<String> shaderVals = new ArrayList<String>();
        ShaderProgram currShader;
        String vertexShader;

        Sprite blockSprite;

        // TODO: has to be higher layer than ThrowOutPokemon()
        // why is ThrowOutPokemon in such a high layer?
        public int layer = 100;
        public int getLayer(){return this.layer;}

        public String getCamera() {return "gui";};

        

        Sprite helperSprite; //just for helping me position the animation. delete later.
        
        Pokemon attacker;
        Pokemon target;

        Pixmap pixmap;
                
        // gen 7 properties
        // TODO: 1/8 critical hit chance
        int power = 70;
        int accuracy = 100;
        
        @Override
        public void step(Game game) {

            //set sprite position
            //if done with anim, do nextAction
            if (this.repeats.isEmpty()) {
                if (!this.screenPositions.isEmpty()) {
                    this.currPosition = this.screenPositions.get(0);
                    // screen wiggle
                    //TODO: *3 b/c screen is scaled, alternative approach would be nice
                    this.pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, 160*3, 144*3);
                    for (int j=0; j < 144; j++) {
                        for (int i=0; i < 160; i++) {
                            this.blockSprite.setColor(new Color(this.pixmap.getPixel(i*3, j*3)));
                            // not using this.currPosition.y here, but may in future animations
                            this.blockSprite.setPosition(i + this.currPosition.x, j + this.currPosition.y);
                            this.blockSprite.draw(game.floatingBatch);
                        }
                    }
                    this.screenPositions.remove(0);
                }
                else {
                    //assign damage to target pkmn
                    int currHealth = this.target.currentStats.get("hp");
                    //TODO - correct damage calculation
                    int finalHealth = currHealth - this.power;
                    if (finalHealth < 0) {finalHealth = 0;} //make sure finalHealth isn't negative
                    this.target.currentStats.put("hp", finalHealth);

                    game.actionStack.remove(this);
                    PublicFunctions.insertToAS(game, this.nextAction);
                }
                return;
            }
            
            //get next sound, play it
            this.sound = this.sounds.get(0);
            if (this.sound != null) {
                PublicFunctions.insertToAS(game, new PlaySound(this.sound, new DoneAction()));
                this.sounds.set(0, null); //don't play same sound over again
            }

            //get next frame
            this.sprite = sprites.get(0);
            
            //debug
//            this.helperSprite.draw(game.floatingBatch); 

            //draw current sprite
            if (this.sprite != null) {
                this.sprite.setPosition(position.x, position.y);
                this.sprite.draw(game.floatingBatch);            
            }
            
            //debug
//            if (this.repeats.size() == 5) { 
//                return;
//            }

            //repeat sprite/pos for current object for 'frames[0]' number of frames.
            if (this.repeats.get(0) > 0) {
                this.repeats.set(0, this.repeats.get(0) - 1);
            }
            else {
                //since position is relative, only update once each time period
                this.position = this.position.add(positions.get(0));
                positions.remove(0);
                sprites.remove(0);
                repeats.remove(0);
                sounds.remove(0);

                this.currShaderVal = this.shaderVals.get(0);
                this.currShader = new ShaderProgram(this.vertexShader,
                                                    this.currShaderVal);
                game.floatingBatch.setShader(this.currShader);
                this.shaderVals.remove(0);
            }
        }

        public ShadowClaw(Game game,
                          Pokemon attacker,
                          Pokemon target,
                          Action nextAction) {

            this.attacker = attacker;
            this.target = target;
            this.nextAction = nextAction;

            // single pixel sprite used for drawing the effect
            Texture text = new Texture(Gdx.files.internal("battle/pixel1.png"));
            this.blockSprite = new Sprite(text, 0, 0, 1, 1);

            text = new Texture(Gdx.files.internal("attacks/enemy_slash_sheet1.png"));

            //consider doing relative positions from now on
            this.position = new Vector2(16, 40);
            this.positions = new ArrayList<Vector2>();
            for (int i = 0; i < 7; i++) {
                this.positions.add(new Vector2(0,0));
            }

            // screen movement after attack
            this.screenPositions = new ArrayList<Vector2>();
            // 9 frames nothing
            for (int i = 0; i < 9; i++) {
                this.screenPositions.add(new Vector2(0,0));
            }
            // 5 frame move 2 right
            for (int i = 0; i < 5; i++) {
                this.screenPositions.add(new Vector2(2,0));
            }
            // 9 frame move back
            for (int i = 0; i < 9; i++) {
                this.screenPositions.add(new Vector2(0,0));
            }
            // 4 frame move 1 right
            for (int i = 0; i < 4; i++) {
                this.screenPositions.add(new Vector2(1,0));
            }
            // 19 frame move back
            for (int i = 0; i < 19; i++) {
                this.screenPositions.add(new Vector2(0,0));
            }
            
            
            this.sprites =  new ArrayList<Sprite>();
            this.sprites.add(null); //draw nothing
            this.sprites.add(null); //draw nothing
            for (int i = 0; i < 4; i++) {
                this.sprites.add(new Sprite(text, 48*i, 0, 48, 48));
            }
            this.sprites.add(null); //draw nothing

            this.repeats = new ArrayList<Integer>();
            // TODO: is triggerAction working like it should?
            this.repeats.add(20-1); //wait 20 frames
            this.repeats.add(40-1); //wait 20 frames
            for (int i = 0; i < 4; i++) {
                //7 frames per image
                this.repeats.add(7-1);
            }
            this.repeats.add(6-1); //wait 6 frames

            //sounds to play
            this.sounds = new ArrayList<String>();
            this.sounds.add(null);
            this.sounds.add(null);
            this.sounds.add("slash1");
            for (int i = 0; i < 4; i++) {
                this.sounds.add(null);
            }

            this.vertexShader = "attribute vec4 a_position;\n"
                    + "attribute vec4 a_color;\n"
                    + "attribute vec2 a_texCoord;\n"
                    + "attribute vec2 a_texCoord0;\n"
                        
                    + "uniform mat4 u_projTrans;\n"
                                            
                    + "varying vec4 v_color;\n"
                    + "varying vec2 v_texCoords;\n"
                                            
                    + "void main()\n"
                    + "{\n"
                    + "    v_color = a_color;\n"
                    + "    v_texCoords = a_texCoord0;\n"
                    + "    gl_Position =  u_projTrans * a_position;\n"
                    // below can be used to translate screen pixels (for attacks, etc
//                    + "    gl_Position =  u_projTrans * vec4(a_position.x, a_position.y, a_position.z, 1.0);\n"
                    + "}\n";

            float level = 0f;
            String normalShader = "precision mediump float;\n"
                            + "varying vec4 v_color;\n"
                            + "varying vec2 v_texCoords;\n"
                            + "uniform sampler2D u_texture;\n"
                            + "uniform mat4 u_projTrans;\n"
                            
                            + "bool equals(float a, float b) {\n"
                            + "    return abs(a-b) < 0.0001;\n"
                            + "}\n"
                            
                            + "void main() {\n"
                            + "    vec4 color = texture2D (u_texture, v_texCoords) * v_color;\n"

                            + "    float level = "+level+";\n" // can't do +- or -+ inline
                            + "    color = vec4(color.r+level, color.g+level, color.b+level, color.a);\n"
                            + "    gl_FragColor = color;\n"
                            + "}\n";
            String inverseShader = "precision mediump float;\n"
                            + "varying vec4 v_color;\n"
                            + "varying vec2 v_texCoords;\n"
                            + "uniform sampler2D u_texture;\n"
                            + "uniform mat4 u_projTrans;\n"
                            
                            + "bool equals(float a, float b) {\n"
                            + "    return abs(a-b) < 0.0001;\n"
                            + "}\n"
                            
                            + "void main() {\n"
                            + "    vec4 color = texture2D (u_texture, v_texCoords) * v_color;\n"

                            + "    color = vec4(1-color.r, 1-color.g, 1-color.b, color.a);\n"
                            + "    gl_FragColor = color;\n"
                            + "}\n";
            this.shaderVals.add(inverseShader);
            for (int i = 0; i < 5; i++) {
                this.shaderVals.add(inverseShader);
            }
            this.shaderVals.add(normalShader);

//            text = new Texture(Gdx.files.internal("attacks/enemy_slash/helper1.png"));
//            this.helperSprite = new Sprite(text, 0, 0, 160, 144);

        }
    }
    
    // this is also the Night Shade attack
    // TODO: this attack lags, you can tell b/c
    // the night shade sound effect dmg sound happens too early, but
    // the animation has the correct number of frames
    static class Psychic extends Action {

        public int layer = 109;
        public int getLayer(){return this.layer;}

        public String getCamera() {return "gui";};

        // TODO: set these
        int power = 100;
        int accuracy = 100;

        Pokemon attacker;
        Pokemon target;
        Pixmap pixmap = null;
        Sprite sprite;
        ArrayList<int[]> offsets = new ArrayList<int[]>();
        int[] currOffsets;
        ArrayList<Vector2> positions = new ArrayList<Vector2>();
        Vector2 currPosition;
        ArrayList<String> shaderVals = new ArrayList<String>();
        String currShaderVal;
        ShaderProgram currShader;
        String vertexShader;
        boolean firstStep = true;
        boolean isNightShade = false;  // if true, use different sound effect
        
        boolean hitSound = true;

        @Override
        public void step(Game game) {

            if (this.firstStep) {
                if (this.isNightShade) {
                    PublicFunctions.insertToAS(game, new PlaySound("night_shade1", new DoneAction()));
                }
                else {
                    PublicFunctions.insertToAS(game, new PlaySound("psychic1", new DoneAction()));
                }
                this.firstStep = false;
            }

            // run through shaders, then offsets, then positions
            if (!this.shaderVals.isEmpty()) {
                this.currShaderVal = this.shaderVals.get(0);
                this.currShader = new ShaderProgram(this.vertexShader,
                                                    this.currShaderVal);
                game.floatingBatch.setShader(this.currShader);
                this.shaderVals.remove(0);
            }
            else if (!this.offsets.isEmpty()) {
                this.currOffsets = this.offsets.get(0);
                // setting pixmap only once b/c otherwise get bad lag
                // would be nice to do every frame tho since there are moving rocks etc
                if (this.pixmap == null) {
                    //TODO: *3 b/c screen is scaled, alternative approach would be nice
                    this.pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, 160*3, 144*3);
                }
                for (int j=0; j < 144-3; j++) { //3 pixels from top
                    for (int i=0; i < 160; i++) {
                        // note: got better performance when creating a new Color here
                        // rather than using Color.set() on existing Color
                        this.sprite.setColor(new Color(this.pixmap.getPixel(i*3, j*3)));
                        this.sprite.setPosition(i + this.currOffsets[j%16], j);
                        this.sprite.draw(game.floatingBatch);
                    }
                }
                this.offsets.remove(0);
            }
            else if (!this.positions.isEmpty()) {
                if (this.hitSound) {
                    // play hit sound
                    PublicFunctions.insertToAS(game, new PlaySound("hit_normal1", new DoneAction()));
                    this.hitSound = false;
                }
                this.currPosition = this.positions.get(0);
                //TODO: *3 b/c screen is scaled, alternative approach would be nice
                this.pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, 160*3, 144*3);
                for (int j=0; j < 144; j++) {
                    for (int i=0; i < 160; i++) {
                        this.sprite.setColor(new Color(this.pixmap.getPixel(i*3, j*3)));
                        // not using this.currPosition.y here, but may in future animations
                        this.sprite.setPosition(i + this.currPosition.x, j + this.currPosition.y);
                        this.sprite.draw(game.floatingBatch);
                    }
                }
                this.positions.remove(0);
            }
            else {
                //assign damage to target pkmn
                int currHealth = this.target.currentStats.get("hp");
                //TODO - correct damage calculation
                int finalHealth = currHealth - this.power;
                if (finalHealth < 0) {finalHealth = 0;} //make sure finalHealth isn't negative
                this.target.currentStats.put("hp", finalHealth);
                
                game.actionStack.remove(this);
                PublicFunctions.insertToAS(game, this.nextAction);
            }
            
        }

        public Psychic(Game game,
                       Pokemon attacker,
                       Pokemon target,
                       boolean isNightShade,
                       Action nextAction) {

            this.isNightShade = isNightShade;
            this.target = target;
            this.attacker = attacker;
            this.nextAction = nextAction;

            // if night shade, power == users level
            if (this.isNightShade) {
                this.power = this.attacker.level;
            }

            // single pixel sprite used for drawing the effect
            Texture text = new Texture(Gdx.files.internal("battle/pixel1.png"));
            this.sprite = new Sprite(text, 0, 0, 1, 1);

            this.vertexShader = "attribute vec4 a_position;\n"
                    + "attribute vec4 a_color;\n"
                    + "attribute vec2 a_texCoord;\n"
                    + "attribute vec2 a_texCoord0;\n"
                        
                    + "uniform mat4 u_projTrans;\n"
                                            
                    + "varying vec4 v_color;\n"
                    + "varying vec2 v_texCoords;\n"
                                            
                    + "void main()\n"
                    + "{\n"
                    + "    v_color = a_color;\n"
                    + "    v_texCoords = a_texCoord0;\n"
                    + "    gl_Position =  u_projTrans * a_position;\n"
                    // below can be used to translate screen pixels (for attacks, etc
//                    + "    gl_Position =  u_projTrans * vec4(a_position.x, a_position.y, a_position.z, 1.0);\n"
                    + "}\n";

            // shader 'flash' anim at beginning
            // 0, .33, .66, 1 ?
            String darken1 = this.getShader(-.33f);
            String darken2 = this.getShader(-.66f);
            String darken3 = this.getShader(-1f);
            String lighten1 = this.getShader(.33f);
            String lighten2 = this.getShader(.66f);
            String lighten3 = this.getShader(1f);
            String normal = this.getShader(0f);
            this.shaderVals.add(darken1);
            this.shaderVals.add(darken1);
            this.shaderVals.add(darken2);
            this.shaderVals.add(darken2);
            this.shaderVals.add(darken3);
            this.shaderVals.add(darken3);
            this.shaderVals.add(darken2);
            this.shaderVals.add(darken2);
            this.shaderVals.add(darken1);
            this.shaderVals.add(darken1);
            this.shaderVals.add(normal);
            this.shaderVals.add(normal);
            this.shaderVals.add(lighten1);
            this.shaderVals.add(lighten1);
            this.shaderVals.add(lighten2);
            this.shaderVals.add(lighten2);
            this.shaderVals.add(lighten3);
            this.shaderVals.add(lighten3);
            this.shaderVals.add(lighten2);
            this.shaderVals.add(lighten2);
            this.shaderVals.add(lighten1);
            this.shaderVals.add(lighten1);
            this.shaderVals.add(normal);
            this.shaderVals.add(normal);
            this.shaderVals.add(darken1);
            this.shaderVals.add(darken2);
            this.shaderVals.add(darken3);
            this.shaderVals.add(darken2);
            this.shaderVals.add(darken1);
            this.shaderVals.add(normal);
            this.shaderVals.add(lighten1);
            this.shaderVals.add(lighten2);
            this.shaderVals.add(lighten3);
            this.shaderVals.add(lighten2);
            this.shaderVals.add(lighten1);
            this.shaderVals.add(normal);
            this.shaderVals.add(darken1);
            this.shaderVals.add(darken2);
            this.shaderVals.add(darken3);
            this.shaderVals.add(darken2);
            this.shaderVals.add(darken1);
            this.shaderVals.add(normal);
            this.shaderVals.add(lighten1);
            this.shaderVals.add(lighten2);
            this.shaderVals.add(lighten3);
            this.shaderVals.add(lighten2);
            this.shaderVals.add(lighten1);
            for (int i = 0; i < 8; i++) {
                this.shaderVals.add(normal);
            }

            // screen movement after attack
            // 9 frames nothing
            for (int i = 0; i < 9; i++) {
                this.positions.add(new Vector2(0,0));
            }
            // 5 frame move 2 right
            for (int i = 0; i < 5; i++) {
                this.positions.add(new Vector2(2,0));
            }
            // 9 frame move back
            for (int i = 0; i < 9; i++) {
                this.positions.add(new Vector2(0,0));
            }
            // 4 frame move 1 right
            for (int i = 0; i < 4; i++) {
                this.positions.add(new Vector2(1,0));
            }
            // 19 frame move back
            for (int i = 0; i < 19; i++) {
                this.positions.add(new Vector2(0,0));
            }
            // on 20th, start health bar subtract

            // TODO: probably just have two other arrays - positions and 'shaders', or shader intensity vals

            // offsets determine the ripple effect - sequence of 16 sprites
            // starts at bottom-left of screen
            // 127 total frames (back to normal on 28th)
            // animation is weird and doesn't seem to have repeating pattern
            this.offsets.add(new int[] {2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2}); // frame 1
            this.offsets.add(new int[] {2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1});
            this.offsets.add(new int[] {1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1});
            this.offsets.add(new int[] {1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0});
            this.offsets.add(new int[] {0, 0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0});
            this.offsets.add(new int[] {0, 0, 0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1});
            this.offsets.add(new int[] {-1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1});
            this.offsets.add(new int[] {-2, -1, 0, 0, 0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2});
            this.offsets.add(new int[] {2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1});
            this.offsets.add(new int[] {-2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1});
            this.offsets.add(new int[] {0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0});
            this.offsets.add(new int[] {-1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0});
            this.offsets.add(new int[] {0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2, 2, 2, 1, 0});
            this.offsets.add(new int[] {0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2, 2, 2, 1});
            this.offsets.add(new int[] {-1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0});
            this.offsets.add(new int[] {2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2, 2});
            this.offsets.add(new int[] {2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2});
            this.offsets.add(new int[] {2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1});
            this.offsets.add(new int[] {-1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0});
            this.offsets.add(new int[] {0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0}); //20
            this.offsets.add(new int[] {0, 0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0});
            this.offsets.add(new int[] {0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1});
            this.offsets.add(new int[] {-1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1});
            this.offsets.add(new int[] {-1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1});
            this.offsets.add(new int[] {-2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2}); //25
            this.offsets.add(new int[] {-2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1});
            this.offsets.add(new int[] {1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0});
            this.offsets.add(new int[] {-1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0});
            this.offsets.add(new int[] {0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1});
            this.offsets.add(new int[] {0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2, 2, 2, 1});
            this.offsets.add(new int[] {1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2, 2, 2});
            this.offsets.add(new int[] {2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2, 2});
            this.offsets.add(new int[] {2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2});
            this.offsets.add(new int[] {2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1});
            this.offsets.add(new int[] {1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0});
            this.offsets.add(new int[] {1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0});
            this.offsets.add(new int[] {0, 0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0});
            this.offsets.add(new int[] {0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2, 2, 2, 1});
            this.offsets.add(new int[] {0, 0, 0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1});
            this.offsets.add(new int[] {-1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2}); //40
            this.offsets.add(new int[] {-2, -1, 0, 0, 0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2});
            this.offsets.add(new int[] {-2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1});
            this.offsets.add(new int[] {-2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1});
            this.offsets.add(new int[] {0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0});
            this.offsets.add(new int[] {0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0});
            this.offsets.add(new int[] {0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1});
            this.offsets.add(new int[] {1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2});
            this.offsets.add(new int[] {1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1});
            this.offsets.add(new int[] {2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1});
            this.offsets.add(new int[] {1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1});
            this.offsets.add(new int[] {1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0});
            this.offsets.add(new int[] {0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0});
            this.offsets.add(new int[] {0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1});
            this.offsets.add(new int[] {-1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1});
            this.offsets.add(new int[] {-1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2}); //55
            this.offsets.add(new int[] {-2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2});
            this.offsets.add(new int[] {-2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2});
            this.offsets.add(new int[] {2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1});
            this.offsets.add(new int[] {-1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0});
            this.offsets.add(new int[] {0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0});
            this.offsets.add(new int[] {0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1});
            this.offsets.add(new int[] {1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1});
            this.offsets.add(new int[] {0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2, 2, 2, 1});
            this.offsets.add(new int[] {2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2});
            this.offsets.add(new int[] {2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1});
            this.offsets.add(new int[] {1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1});
            this.offsets.add(new int[] {1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0});
            this.offsets.add(new int[] {1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2}); //68
            this.offsets.add(new int[] {0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1});
            this.offsets.add(new int[] {-1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1});
            this.offsets.add(new int[] {-1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2});
            this.offsets.add(new int[] {-2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2});
            this.offsets.add(new int[] {-2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1});
            this.offsets.add(new int[] {-1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1});
            this.offsets.add(new int[] {-1, -2, -2, -2, -1, 0, 0, 0, 1, 2, 2, 2, 1, 0, 0, 0});
            this.offsets.add(new int[] {0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0});
            this.offsets.add(new int[] {0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1});
            this.offsets.add(new int[] {0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1});
            this.offsets.add(new int[] {1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2});
            this.offsets.add(new int[] {1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2}); //80
            this.offsets.add(new int[] {2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1}); // stopped here
            this.offsets.add(new int[] {2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1});
            this.offsets.add(new int[] {1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0});
            this.offsets.add(new int[] {1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0});
            this.offsets.add(new int[] {0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0});
            this.offsets.add(new int[] {-1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1});
            this.offsets.add(new int[] {-2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1});
            this.offsets.add(new int[] {-2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2});
            this.offsets.add(new int[] {-2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1});
            this.offsets.add(new int[] {-2, -2, -2, -1, 0, 0, 0, 1, 2, 2, 2, 1, 0, 0, 0, -1}); //90
            this.offsets.add(new int[] {-1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0});
            this.offsets.add(new int[] {0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0});
            this.offsets.add(new int[] {0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1});
            this.offsets.add(new int[] {1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1});
            this.offsets.add(new int[] {1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2});
            this.offsets.add(new int[] {1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2});
            this.offsets.add(new int[] {-2, -2, -1, 0, 0, 0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2});
            this.offsets.add(new int[] {1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1});
            this.offsets.add(new int[] {1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0});
            this.offsets.add(new int[] {0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0}); //100
            this.offsets.add(new int[] {0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1});
            this.offsets.add(new int[] {0, 0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0});
            this.offsets.add(new int[] {-1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2});
            this.offsets.add(new int[] {-2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2});
            this.offsets.add(new int[] {-2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1});
            this.offsets.add(new int[] {-1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1});
            this.offsets.add(new int[] {0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1});
            this.offsets.add(new int[] {0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0});
            this.offsets.add(new int[] {0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1});
            this.offsets.add(new int[] {1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1}); //110
            this.offsets.add(new int[] {1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2});
            this.offsets.add(new int[] {2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2});
            this.offsets.add(new int[] {2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1});
            this.offsets.add(new int[] {2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1});
            this.offsets.add(new int[] {1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0});
            this.offsets.add(new int[] {0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0});
            this.offsets.add(new int[] {0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0});
            this.offsets.add(new int[] {-1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1});
            this.offsets.add(new int[] {-1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1});
            this.offsets.add(new int[] {-2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2}); //120
            this.offsets.add(new int[] {-2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1});
            this.offsets.add(new int[] {-1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1});
            this.offsets.add(new int[] {-1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1});
            this.offsets.add(new int[] {-1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0});
            this.offsets.add(new int[] {0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1});
            this.offsets.add(new int[] {-1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1});
            this.offsets.add(new int[] {1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2}); //127
        }

        String getShader(float level) {
            
            String shader = "precision mediump float;\n"

                            + "varying vec4 v_color;\n"
                            + "varying vec2 v_texCoords;\n"
                            + "uniform sampler2D u_texture;\n"
                            + "uniform mat4 u_projTrans;\n"
                            
                            + "bool equals(float a, float b) {\n"
                            + "    return abs(a-b) < 0.0001;\n"
                            + "}\n"
                            
                            + "void main() {\n"
                            + "    vec4 color = texture2D (u_texture, v_texCoords) * v_color;\n"

                            + "    float level = "+level+";\n" // can't do +- or -+ inline
                            + "    color = vec4(color.r+level, color.g+level, color.b+level, color.a);\n"
                            + "    gl_FragColor = color;\n"
                            + "}\n";
            return shader;
        }
    }
    

    static class Mewtwo_Special1 extends Action {

        public int layer = 109;
        public int getLayer(){return this.layer;}

        public String getCamera() {return "gui";};

        // TODO: set these
        int power = 100;
        int accuracy = 100;

        Pokemon attacker;
        Pokemon target;
        Pixmap pixmap = null;
        Sprite sprite;
        ArrayList<int[]> offsets = new ArrayList<int[]>();
        int[] currOffsets;
        ArrayList<Vector2> positions = new ArrayList<Vector2>();
        Vector2 currPosition;
        ArrayList<String> shaderVals = new ArrayList<String>();
        String currShaderVal;
        ShaderProgram currShader;
        String vertexShader;
        boolean firstStep = true;
        boolean isNightShade = false;  // if true, use different sound effect
        
        boolean hitSound = true;
        int timer = 0;

        @Override
        public void step(Game game) {

            if (this.timer == 0) {

                this.currShader = new ShaderProgram(this.vertexShader,
                                                    this.getShader(.8f));
                game.floatingBatch.setShader(this.currShader);
                // pause rock anim
                SpecialBattleMewtwo.RocksEffect1.shouldMoveY = false;
                SpecialBattleMewtwo.RocksEffect2.shouldMoveY = false;
            }
            else if (this.timer < 50) {
                
            }
            else if (this.timer == 60) {
                PublicFunctions.insertToAS(game, new PlaySound("Mewtwo_Special1", new DoneAction()));
                SpecialBattleMewtwo.RocksEffect1.velocityX = -8;
                SpecialBattleMewtwo.RocksEffect2.velocityX = -2;  // TODO: is this doing anything?
                this.currShader = new ShaderProgram(this.vertexShader, this.getShader(.6f));
                game.floatingBatch.setShader(this.currShader);
            }
            else if (this.timer == 120) {
                SpecialBattleMewtwo.RocksEffect1.velocityX = -12;
                SpecialBattleMewtwo.RocksEffect2.velocityX = -3;
                this.currShader = new ShaderProgram(this.vertexShader, this.getShader(.4f));
                game.floatingBatch.setShader(this.currShader);
            }
            else if (this.timer == 180) {
                SpecialBattleMewtwo.RocksEffect1.velocityX = -16;
                SpecialBattleMewtwo.RocksEffect2.velocityX = -4;
                this.currShader = new ShaderProgram(this.vertexShader, this.getShader(.2f));
                game.floatingBatch.setShader(this.currShader);
            }
            else if (this.timer == 190) {
                this.currShader = new ShaderProgram(this.vertexShader, this.getShader(.3f));
                game.floatingBatch.setShader(this.currShader);
            }
            else if (this.timer == 200) {
                this.currShader = new ShaderProgram(this.vertexShader, this.getShader(0f));
                game.floatingBatch.setShader(this.currShader);
            }
            else if (this.timer == 210) {
                this.currShader = new ShaderProgram(this.vertexShader, this.getShader(.1f));
                game.floatingBatch.setShader(this.currShader);
            }
            else if (this.timer == 220) {
                this.currShader = new ShaderProgram(this.vertexShader, this.getShader(-.2f));
                game.floatingBatch.setShader(this.currShader);
            }
            else if (this.timer == 230) {
                this.currShader = new ShaderProgram(this.vertexShader, this.getShader(-.1f));
                game.floatingBatch.setShader(this.currShader);
            }
            else if (this.timer == 240) {
                this.currShader = new ShaderProgram(this.vertexShader, this.getShader(-.4f));
                game.floatingBatch.setShader(this.currShader);
            }
            else if (this.timer == 250) {
                this.currShader = new ShaderProgram(this.vertexShader, this.getShader(-.3f));
                game.floatingBatch.setShader(this.currShader);
            }
            else if (this.timer == 260) {
                this.currShader = new ShaderProgram(this.vertexShader, this.getShader(-.6f));
                game.floatingBatch.setShader(this.currShader);
            }
            else if (this.timer == 270) {
                this.currShader = new ShaderProgram(this.vertexShader, this.getShader(-.5f));
                game.floatingBatch.setShader(this.currShader);
            }
            else if (this.timer == 280) {
                this.currShader = new ShaderProgram(this.vertexShader, this.getShader(-.8f));
                game.floatingBatch.setShader(this.currShader);
            }
            else if (this.timer < 300) {
                
            }
            else if (this.timer < 400 +150) {
                if (this.timer % 2 == 0) {
                    game.player.currPokemon.backSprite.setAlpha(1);
                }
                else {
                    game.player.currPokemon.backSprite.setAlpha(0);
                }

                if (this.timer == 340) {
                    this.currShader = new ShaderProgram(this.vertexShader, this.getShader(-.6f));
                    game.floatingBatch.setShader(this.currShader);
                }
                if (this.timer == 400) {
                    this.currShader = new ShaderProgram(this.vertexShader, this.getShader(-.4f));
                    game.floatingBatch.setShader(this.currShader);
                }
                if (this.timer == 460) {
                    this.currShader = new ShaderProgram(this.vertexShader, this.getShader(-.2f));
                    game.floatingBatch.setShader(this.currShader);
                }
                
                if (this.timer == 305 +150) {
                    SpecialBattleMewtwo.RocksEffect1.velocityX = -14;
                    SpecialBattleMewtwo.RocksEffect2.velocityX = -3;
                }
                else if (this.timer == 315 +150) {
                    SpecialBattleMewtwo.RocksEffect1.velocityX = -12;
                }
                else if (this.timer == 325 +150) {
                    SpecialBattleMewtwo.RocksEffect1.velocityX = -10;
                }
                else if (this.timer == 335 +150) {
                    SpecialBattleMewtwo.RocksEffect1.velocityX = -8;
                    SpecialBattleMewtwo.RocksEffect2.velocityX = -2;
                }
                else if (this.timer == 345 +150) {
                    SpecialBattleMewtwo.RocksEffect1.velocityX = -6;
                }
                else if (this.timer == 355 +150) {
                    SpecialBattleMewtwo.RocksEffect1.velocityX = -6;
                }
                else if (this.timer == 365 +150) {
                    SpecialBattleMewtwo.RocksEffect1.velocityX = -4;
                    SpecialBattleMewtwo.RocksEffect2.velocityX = -1;
                }
                else if (this.timer == 375 +150) {
                    SpecialBattleMewtwo.RocksEffect1.velocityX = -2;
                }
                else if (this.timer == 385 +150) {
                    SpecialBattleMewtwo.RocksEffect1.velocityX = 0;
                    SpecialBattleMewtwo.RocksEffect2.velocityX = 0;
                }
            }
            else if (this.timer == 550) {
                game.player.currPokemon.backSprite.setAlpha(1);
                this.currShader = new ShaderProgram(this.vertexShader, this.getShader(0f));
                game.floatingBatch.setShader(this.currShader);
                SpecialBattleMewtwo.RocksEffect1.shouldMoveY = true;
                SpecialBattleMewtwo.RocksEffect2.shouldMoveY = true;
            }
            else if (this.timer < 600) {
                
            }
            else {
                    
                //assign damage to target pkmn
                int currHealth = this.target.currentStats.get("hp");
                //TODO - correct damage calculation
                int finalHealth = currHealth - this.power;
                if (finalHealth < 0) {finalHealth = 0;} //make sure finalHealth isn't negative
                this.target.currentStats.put("hp", finalHealth);
                  
                game.actionStack.remove(this);
                PublicFunctions.insertToAS(game, this.nextAction);
            }
            this.timer++;

//            // run through shaders, then offsets, then positions
//            if (!this.shaderVals.isEmpty()) {
//                this.currShaderVal = this.shaderVals.get(0);
//                this.currShader = new ShaderProgram(this.vertexShader,
//                                                    this.currShaderVal);
//                game.floatingBatch.setShader(this.currShader);
//                this.shaderVals.remove(0);
//            }
//            else if (!this.offsets.isEmpty()) {
//                this.currOffsets = this.offsets.get(0);
//                // setting pixmap only once b/c otherwise get bad lag
//                // would be nice to do every frame tho since there are moving rocks etc
//                if (this.pixmap == null) {
//                    //TODO: *3 b/c screen is scaled, alternative approach would be nice
//                    this.pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, 160*3, 144*3);
//                }
//                for (int j=0; j < 144-3; j++) { //3 pixels from top
//                    for (int i=0; i < 160; i++) {
//                        // note: got better performance when creating a new Color here
//                        // rather than using Color.set() on existing Color
//                        this.sprite.setColor(new Color(this.pixmap.getPixel(i*3, j*3)));
//                        this.sprite.setPosition(i + this.currOffsets[j%16], j);
//                        this.sprite.draw(game.floatingBatch);
//                    }
//                }
//                this.offsets.remove(0);
//            }
//            else if (!this.positions.isEmpty()) {
//                if (this.hitSound) {
//                    // play hit sound
//                    PublicFunctions.insertToAS(game, new PlaySound("hit_normal1", new DoneAction()));
//                    this.hitSound = false;
//                }
//                this.currPosition = this.positions.get(0);
//                //TODO: *3 b/c screen is scaled, alternative approach would be nice
//                this.pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, 160*3, 144*3);
//                for (int j=0; j < 144; j++) {
//                    for (int i=0; i < 160; i++) {
//                        this.sprite.setColor(new Color(this.pixmap.getPixel(i*3, j*3)));
//                        // not using this.currPosition.y here, but may in future animations
//                        this.sprite.setPosition(i + this.currPosition.x, j + this.currPosition.y);
//                        this.sprite.draw(game.floatingBatch);
//                    }
//                }
//                this.positions.remove(0);
//            }
//            else {
//                //assign damage to target pkmn
//                int currHealth = this.target.currentStats.get("hp");
//                //TODO - correct damage calculation
//                int finalHealth = currHealth - this.power;
//                if (finalHealth < 0) {finalHealth = 0;} //make sure finalHealth isn't negative
//                this.target.currentStats.put("hp", finalHealth);
//                
//                game.actionStack.remove(this);
//                PublicFunctions.insertToAS(game, this.nextAction);
//            }
            
        }

        public Mewtwo_Special1(Game game,
                               Pokemon attacker,
                               Pokemon target,
                               Action nextAction) {

            this.target = target;
            this.attacker = attacker;
            this.nextAction = nextAction;

            // single pixel sprite used for drawing the effect
            Texture text = new Texture(Gdx.files.internal("battle/pixel1.png"));
            this.sprite = new Sprite(text, 0, 0, 1, 1);

            this.vertexShader = "attribute vec4 a_position;\n"
                    + "attribute vec4 a_color;\n"
                    + "attribute vec2 a_texCoord;\n"
                    + "attribute vec2 a_texCoord0;\n"
                        
                    + "uniform mat4 u_projTrans;\n"
                                            
                    + "varying vec4 v_color;\n"
                    + "varying vec2 v_texCoords;\n"
                                            
                    + "void main()\n"
                    + "{\n"
                    + "    v_color = a_color;\n"
                    + "    v_texCoords = a_texCoord0;\n"
                    + "    gl_Position =  u_projTrans * a_position;\n"
                    // below can be used to translate screen pixels (for attacks, etc
//                    + "    gl_Position =  u_projTrans * vec4(a_position.x, a_position.y, a_position.z, 1.0);\n"
                    + "}\n";

            // shader 'flash' anim at beginning
            // 0, .33, .66, 1 ?
            String darken1 = this.getShader(-.33f);
            String darken2 = this.getShader(-.66f);
            String darken3 = this.getShader(-1f);
            String lighten1 = this.getShader(.33f);
            String lighten2 = this.getShader(.66f);
            String lighten3 = this.getShader(1f);
            String normal = this.getShader(0f);
            this.shaderVals.add(darken1);
            this.shaderVals.add(darken1);
            this.shaderVals.add(darken2);
            this.shaderVals.add(darken2);
            this.shaderVals.add(darken3);
            this.shaderVals.add(darken3);
            this.shaderVals.add(darken2);
            this.shaderVals.add(darken2);
            this.shaderVals.add(darken1);
            this.shaderVals.add(darken1);
            this.shaderVals.add(normal);
            this.shaderVals.add(normal);
            this.shaderVals.add(lighten1);
            this.shaderVals.add(lighten1);
            this.shaderVals.add(lighten2);
            this.shaderVals.add(lighten2);
            this.shaderVals.add(lighten3);
            this.shaderVals.add(lighten3);
            this.shaderVals.add(lighten2);
            this.shaderVals.add(lighten2);
            this.shaderVals.add(lighten1);
            this.shaderVals.add(lighten1);
            this.shaderVals.add(normal);
            this.shaderVals.add(normal);
            this.shaderVals.add(darken1);
            this.shaderVals.add(darken2);
            this.shaderVals.add(darken3);
            this.shaderVals.add(darken2);
            this.shaderVals.add(darken1);
            this.shaderVals.add(normal);
            this.shaderVals.add(lighten1);
            this.shaderVals.add(lighten2);
            this.shaderVals.add(lighten3);
            this.shaderVals.add(lighten2);
            this.shaderVals.add(lighten1);
            this.shaderVals.add(normal);
            this.shaderVals.add(darken1);
            this.shaderVals.add(darken2);
            this.shaderVals.add(darken3);
            this.shaderVals.add(darken2);
            this.shaderVals.add(darken1);
            this.shaderVals.add(normal);
            this.shaderVals.add(lighten1);
            this.shaderVals.add(lighten2);
            this.shaderVals.add(lighten3);
            this.shaderVals.add(lighten2);
            this.shaderVals.add(lighten1);
            for (int i = 0; i < 8; i++) {
                this.shaderVals.add(normal);
            }

            // screen movement after attack
            // 9 frames nothing
            for (int i = 0; i < 9; i++) {
                this.positions.add(new Vector2(0,0));
            }
            // 5 frame move 2 right
            for (int i = 0; i < 5; i++) {
                this.positions.add(new Vector2(2,0));
            }
            // 9 frame move back
            for (int i = 0; i < 9; i++) {
                this.positions.add(new Vector2(0,0));
            }
            // 4 frame move 1 right
            for (int i = 0; i < 4; i++) {
                this.positions.add(new Vector2(1,0));
            }
            // 19 frame move back
            for (int i = 0; i < 19; i++) {
                this.positions.add(new Vector2(0,0));
            }
            // on 20th, start health bar subtract

        }

        String getShader(float level) {
            String shader = "precision mediump float;\n"

                            + "varying vec4 v_color;\n"
                            + "varying vec2 v_texCoords;\n"
                            + "uniform sampler2D u_texture;\n"
                            + "uniform mat4 u_projTrans;\n"
                            
                            + "bool equals(float a, float b) {\n"
                            + "    return abs(a-b) < 0.0001;\n"
                            + "}\n"
                            
                            + "void main() {\n"
                            + "    vec4 color = texture2D (u_texture, v_texCoords) * v_color;\n"

                            + "    float level = "+level+";\n" // can't do +- or -+ inline
                            + "    if(color.r >= .9 && color.g >= .9 && color.b >= .9) {\n"
                            + "           color = vec4(color.r, color.g, color.b, color.a);\n"
                            + "    }\n"
                            + "    else {\n"
                            + "        color = vec4(color.r-level, color.g-level, color.b-level, color.a);\n"
                            + "    }\n"
                            + "    gl_FragColor = color;\n"
                            + "}\n";
            return shader;
        }
    }
    

    // Lick attack animation
    static class LoadAndPlayAttackAnimation extends Action {

        public int layer = 110;
        public int getLayer(){return this.layer;}
        public String getCamera() {return "gui";};

        int power = 10;
        int accuracy = 10;
        String name;
        HashMap<Integer, String> metadata = new HashMap<Integer, String>();
        Music sound;
        int frameNum = 1;
        Texture currText;
        Sprite currFrame;
        Pokemon target;
        boolean firstStep = true;
        Matrix4 translation;
        Vector2 playerSpriteOrigin;
        Vector2 enemySpriteOrigin;
        Pixmap pixmap;
        int pixmapX, pixmapY;

        @Override
        public void step(Game game) {
            // TODO: load attack power/accuracy
            if (this.firstStep) {
                // load metadata for each frame
                // ex: player_healthbar_gone -> means to make player's healthbar transparent during this frame
                try {
                    FileHandle file = Gdx.files.internal("attacks/" + this.name + "/metadata.out");
                    Reader reader = file.reader();
                    BufferedReader br = new BufferedReader(reader);
                    String line;
//                    int lineNum = 0;
                    while ((line = br.readLine()) != null)   {
                        int frameNum = Integer.valueOf(line.split(", ")[0]);
                        String properties = line.split(", ")[1];
                        this.metadata.put(frameNum, properties);
//                        lineNum++;
                    }
                    reader.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                // load sound to play and play it
                this.sound = Gdx.audio.newMusic(Gdx.files.internal("attacks/" + this.name + "/sound.ogg"));
                this.sound.play();
                this.firstStep = false;
                
                this.playerSpriteOrigin = new Vector2(game.player.currPokemon.backSprite.getX(), 
                                                      game.player.currPokemon.backSprite.getY());
                this.enemySpriteOrigin = new Vector2(game.battle.oppPokemon.sprite.getX(), 
                                                     game.battle.oppPokemon.sprite.getY());
            }

            // reset vars at beginning
            DrawEnemyHealth.shouldDraw = true;
            DrawFriendlyHealth.shouldDraw = true;
            DrawBattle.shouldDrawOppPokemon = true;
            game.player.currPokemon.backSprite.setPosition(this.playerSpriteOrigin.x, this.playerSpriteOrigin.y);
            game.battle.oppPokemon.sprite.setPosition(this.enemySpriteOrigin.x, this.enemySpriteOrigin.y);
            game.floatingBatch.setTransformMatrix(new Matrix4(new Vector3(0,0,0), new Quaternion(), new Vector3(1,1,1)));
            
            // if next frame doesn't exist in animation, return
            FileHandle filehandle = Gdx.files.internal("attacks/" + this.name + "/output/frame-" + String.format("%03d", this.frameNum) + ".png"); 
            if (!filehandle.exists()) {
                //assign damage to target pkmn
                int currHealth = this.target.currentStats.get("hp");
                //TODO - correct damage calculation
                int finalHealth = currHealth - this.power;
                if (finalHealth < 0) {finalHealth = 0;} //make sure finalHealth isn't negative
                this.target.currentStats.put("hp", finalHealth);
                game.actionStack.remove(this);
                PublicFunctions.insertToAS(game, this.nextAction);
                
                // TODO: need new actions not v effective, super eff, etc
//                if (this.target == game.player.currPokemon) {
//                    PublicFunctions.insertToAS(game, new DefaultEnemyAttack(game.battle.oppPokemon, game.player.currPokemon, this.power, this.accuracy, this.nextAction));
//                }
//                else {
//                    PublicFunctions.insertToAS(game, new DefaultAttack(game, this.power, this.accuracy, this.nextAction));
//                }
                return;
            }
            
            // draw water effect if present
            if (this.metadata.containsKey(this.frameNum)) {
                String properties = this.metadata.get(this.frameNum);
                if (properties.contains("screenshot")) {
                    String[] values = properties.split("screenshot:")[1].split(" ")[0].split(",");
                    this.pixmapX = Integer.valueOf(values[0]);
                    this.pixmapY = Integer.valueOf(values[1]);
                    int width = Integer.valueOf(values[2]);
                    int height = Integer.valueOf(values[3]);
                    this.pixmap = ScreenUtils.getFrameBufferPixmap(this.pixmapX*3, this.pixmapY*3, width*3, height*3);
                }
                if (properties.contains("row_copy")) {
                    // copy the screenshotted pixmap
                    Pixmap newPixmap = new Pixmap((int)(this.pixmap.getWidth()/3f), (int)(this.pixmap.getHeight()/3f), Pixmap.Format.RGBA8888);
//                    Pixmap newPixmap = new Pixmap(this.pixmap.getWidth(), this.pixmap.getHeight(), Pixmap.Format.RGBA8888);
                    newPixmap.setColor(new Color(0, 0, 0, 0));
                    newPixmap.fill();
                    // drawPixmap didn't work for some reason, copying manually
                    for (int i = 0; i < newPixmap.getWidth(); i++) {
                        for (int j = 0; j < newPixmap.getHeight(); j++) {
                            Color color = new Color(this.pixmap.getPixel(i*3, j*3));
                            newPixmap.drawPixel(i, j, Color.rgba8888(color));
                        }
                    }
//                    newPixmap.drawPixmap(this.pixmap, 0, 0, this.pixmap.getWidth(), this.pixmap.getHeight(), 0, 0, (int)(this.pixmap.getWidth()/3f), (int)(this.pixmap.getHeight()/3f));
//                    newPixmap.drawPixmap(this.pixmap, 0, 0);
                    // don't apply water ripple effect to player sprite
                    boolean playerSpriteIgnore = this.name.contains("player") && !this.name.contains("surf") && !this.name.contains("whirlpool");
                    boolean enemySpriteIgnore = this.name.contains("enemy") && !this.name.contains("surf") && !this.name.contains("whirlpool");
                    // syntax - row_copy:to_y,from_y
                    String[] copies = properties.split(" row_copy:");
                    int i=0;
                    for (String copy : copies) {
                        // skip first element
                        i++;
                        if (i==1) {
                            continue;
                        }
                        int targetY = Integer.valueOf(copy.split(",")[0]);
                        int sourceY = Integer.valueOf(copy.split(",")[1]);
//                        System.out.println(String.valueOf(this.frameNum));
                        // copy row to new location (from the original pixmap)
                        for (int x = 0; x < newPixmap.getWidth(); x++) {
                            if (playerSpriteIgnore && x < 86 && (144-targetY) < 112) {
                                continue;
                            }
                            if (enemySpriteIgnore && x >= 96 && (144-targetY) >= 88) {
                                continue;
                            }
                            Color color = new Color(this.pixmap.getPixel(x*3, (144-sourceY)*3));
                            newPixmap.drawPixel(x, 144-targetY, Color.rgba8888(color));
                        }
                    }
                    
                    Sprite drawSprite = new Sprite(new Texture(newPixmap));
                    drawSprite.flip(false, true); // pixmaps are flipped for some reason
//                    Sprite drawSprite = new Sprite(new Texture(this.pixmap));
//                    drawSprite.scale(.3f);
//                    drawSprite.setPosition(this.pixmapX, this.pixmapY);
                    game.floatingBatch.draw(drawSprite, this.pixmapX, this.pixmapY);
//                    drawSprite.draw(game.floatingBatch);
                }
            }

            // draw current frame
            this.currText = new Texture(filehandle);
            this.currFrame = new Sprite(this.currText, 0, 0, 160, 144);
            this.currFrame.draw(game.floatingBatch);
            
            // handle metadata
            if (this.metadata.containsKey(this.frameNum)) {
                String properties = this.metadata.get(this.frameNum);
                if (properties.contains("enemy_healthbar_gone")) {
                    DrawEnemyHealth.shouldDraw = false;
                }
                if (properties.contains("player_healthbar_gone")) {
                    DrawFriendlyHealth.shouldDraw = false;
                }
                if (properties.contains("enemy_sprite_gone")) {
                    DrawBattle.shouldDrawOppPokemon = false;
                }
                if (properties.contains("screen_translate_y")) {
                    int translateAmt = Integer.valueOf(properties.split("screen_translate_y:")[1].split(" ")[0]);
                    game.floatingBatch.setTransformMatrix(new Matrix4(new Vector3(0,translateAmt,0), new Quaternion(), new Vector3(1,1,1)));
                }
                if (properties.contains("screen_translate_x")) {
                    int translateAmt = Integer.valueOf(properties.split("screen_translate_x:")[1].split(" ")[0]);
                    game.floatingBatch.setTransformMatrix(new Matrix4(new Vector3(translateAmt,0,0), new Quaternion(), new Vector3(1,1,1)));
                }
                if (properties.contains("player_translate_x")) {
                    int translateAmt = Integer.valueOf(properties.split("player_translate_x:")[1].split(" ")[0]);
                    game.player.currPokemon.backSprite.setPosition(game.player.currPokemon.backSprite.getX()+translateAmt, game.player.currPokemon.backSprite.getY());
                }
                if (properties.contains("player_translate_y")) {
                    int translateAmt = Integer.valueOf(properties.split("player_translate_y:")[1].split(" ")[0]);
                    game.player.currPokemon.backSprite.setPosition(game.player.currPokemon.backSprite.getX(), game.player.currPokemon.backSprite.getY()+translateAmt);
                }
                if (properties.contains("enemy_translate_x")) {
                    int translateAmt = Integer.valueOf(properties.split("enemy_translate_x:")[1].split(" ")[0]);
                    game.battle.oppPokemon.sprite.setPosition(game.battle.oppPokemon.sprite.getX()+translateAmt, game.battle.oppPokemon.sprite.getY());
                }
                if (properties.contains("enemy_translate_y")) {
                    int translateAmt = Integer.valueOf(properties.split("enemy_translate_y:")[1].split(" ")[0]);
                    game.battle.oppPokemon.sprite.setPosition(game.battle.oppPokemon.sprite.getX(), game.battle.oppPokemon.sprite.getY()+translateAmt);
                }
            }
            
            this.frameNum++;
        }
        
        public LoadAndPlayAttackAnimation(Game game, String name, Pokemon target, Action nextAction) {
            this.name = name.toLowerCase().replace(' ', '_');
            if (target == game.player.currPokemon) {
                this.name = this.name+"_enemy_gsc";
            }
            else {
                this.name = this.name+"_player_gsc";
            }
            this.target = target;
            this.nextAction = nextAction;
        }
        
    }
    
}


//basically 'tackle' attack, with modified power/accuracy
class DefaultAttack extends Action {
    

    ArrayList<Vector2> positions;
    Vector2 position;
    ArrayList<Sprite> sprites;
    Sprite sprite;
    ArrayList<Integer> repeats;
    ArrayList<Float> alphas;
    ArrayList<String> sounds;
    String sound;
    
    public int layer = 120;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};

    
    
    Sprite helperSprite; 
    
    boolean doneYet; //unused
    int power;
    int accuracy;
    

    @Override
    public void step(Game game) {

        //set sprite position
        //if done with anim, do nextAction
        if (positions.isEmpty() || sprites.isEmpty()) {

            //assign damage to opposing pkmn
            int currHealth = game.battle.oppPokemon.currentStats.get("hp");
            //TODO - correct damage calculation
            int finalHealth = currHealth - this.power;
            if (finalHealth < 0) {finalHealth = 0;} //make sure finalHealth isn't negative
            game.battle.oppPokemon.currentStats.put("hp", finalHealth);
            
            
            
            PublicFunctions.insertToAS(game, this.nextAction);
            game.actionStack.remove(this);
            return;
        }
                

        //get next frame
        this.sprite = sprites.get(0);

        //debug
//        this.helperSprite.draw(game.floatingBatch); 


        //draw current sprite
//        if (this.sprite != null) {
//            this.sprite.setPosition(position.x, position.y);
//            this.sprite.draw(game.floatingBatch);    
//        }

        float currAlpha = this.alphas.get(0);
        game.battle.oppPokemon.sprite.setAlpha(currAlpha);
        // special battles require this
        if (game.battle.oppPokemon.breathingSprite != null) {
            game.battle.oppPokemon.breathingSprite.setAlpha(currAlpha);
        }
        

        //debug
//        if (this.repeats.size() == 14) { 
//            return;
//        }

        //get next sound, play it
        this.sound = this.sounds.get(0);
        if (this.sound != null) {
            PublicFunctions.insertToAS(game, new PlaySound(this.sound, new DoneAction()));
            this.sounds.set(0, null); //don't play same sound over again
        }


        //repeat sprite/pos for current object for 'frames[0]' number of frames.
        if (this.repeats.get(0) > 1) {
            this.repeats.set(0, this.repeats.get(0) - 1);
        }
        else {
            //since position is relative, only update once each time period
            this.position = this.position.add(positions.get(0));
            game.player.currPokemon.backSprite.setPosition(this.position.x, this.position.y);
            positions.remove(0);
            sprites.remove(0);
            repeats.remove(0);
            sounds.remove(0);
            alphas.remove(0);
        }
    }
    
    
    public DefaultAttack(Game game, int power, int accuracy, Action nextAction) {        

        this.power = power;
        this.accuracy = accuracy;
        
        this.doneYet = false; //unused
        this.nextAction = nextAction;

        //consider doing relative positions from now on
        //this.position = new Vector2(104+4*3-2,200-6*3-2); //post scaling change
        this.position = new Vector2(game.player.currPokemon.backSprite.getX(), game.player.currPokemon.backSprite.getY());

        this.positions = new ArrayList<Vector2>();
        this.positions.add(new Vector2(8,0));//move forward 1
        this.positions.add(new Vector2(-8,0));//move back 1
        for (int i = 0; i < 13; i++) {
            this.positions.add(new Vector2(0,0));
        }

        this.sprites = new ArrayList<Sprite>(); //may use this in future
        for (int i = 0; i < 15; i++) {
            this.sprites.add(null);
        }
        
        
        this.repeats = new ArrayList<Integer>();
        this.repeats.add(7);
        this.repeats.add(3);
        this.repeats.add(17);
        this.repeats.add(6); 
        this.repeats.add(7); 
        this.repeats.add(5);
        this.repeats.add(9);
        this.repeats.add(4);
        this.repeats.add(8);
        this.repeats.add(6); 
        this.repeats.add(7); 
        this.repeats.add(5);
        this.repeats.add(9);
        this.repeats.add(4);
        this.repeats.add(11);


        this.alphas = new ArrayList<Float>();
        this.alphas.add(1f);
        this.alphas.add(1f);
        this.alphas.add(1f);
        for (int i = 0; i < 6; i++) {
            this.alphas.add(0f);
            this.alphas.add(1f);
        }
        

        this.sounds = new ArrayList<String>();
        this.sounds.add(null); //
        this.sounds.add("tackle1");
        for (int i = 0; i < 13; i++) {
            this.sounds.add(null);
        }
        
        
        Texture text = new Texture(Gdx.files.internal("attack_menu/helper3.png"));
        this.helperSprite = new Sprite(text, 0, 0, 160, 144);
    }
}




//basically 'tackle' attack, with modified power/accuracy
class DefaultEnemyAttack extends Action {
    
    Pokemon pokemon;
    Pokemon oppPokemon; //pokemon being hit
    
    ArrayList<Vector2> positions;
    Vector2 position;
    ArrayList<Sprite> sprites;
    Sprite sprite;
    ArrayList<Integer> repeats;
    ArrayList<Float> alphas;
    ArrayList<String> sounds;
    String sound;
    
    public int layer = 120;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};

    Sprite helperSprite; 
    
    boolean doneYet; //unused
    int power;
    int accuracy;
    

    @Override
    public void step(Game game) {

        //set sprite position
        //if done with anim, do nextAction
        if (positions.isEmpty() || sprites.isEmpty()) {

            //assign damage to opposing pkmn
            int currHealth = this.oppPokemon.currentStats.get("hp");
            //TODO - correct damage calculation
            int finalHealth = currHealth - this.power;
            if (finalHealth < 0) {finalHealth = 0;} //make sure finalHealth isn't negative
            this.oppPokemon.currentStats.put("hp", finalHealth);
                        
            PublicFunctions.insertToAS(game, this.nextAction);
            game.actionStack.remove(this);
            return;
        }
                

        //get next frame
        this.sprite = sprites.get(0);

        //debug
//        this.helperSprite.draw(game.floatingBatch); 
        
        
        //draw current sprite
//        if (this.sprite != null) {
//            this.sprite.setPosition(position.x, position.y);
//            this.sprite.draw(game.floatingBatch);    
//        }
        
        float currAlpha = this.alphas.get(0);
        this.oppPokemon.backSprite.setAlpha(currAlpha);
        
        //debug
//        if (this.repeats.size() == 14) { 
//            return;
//        }

        //get next sound, play it
        this.sound = this.sounds.get(0);
        if (this.sound != null) {
            PublicFunctions.insertToAS(game, new PlaySound(this.sound, new DoneAction()));
            this.sounds.set(0, null); //don't play same sound over again
        }
        
        
        //repeat sprite/pos for current object for 'frames[0]' number of frames.
        if (this.repeats.get(0) > 1) {
            this.repeats.set(0, this.repeats.get(0) - 1);
        }
        else {
            //since position is relative, only update once each time period
            this.position = this.position.add(positions.get(0));
            this.pokemon.sprite.setPosition(this.position.x, this.position.y);
            positions.remove(0);
            sprites.remove(0);
            repeats.remove(0);
            sounds.remove(0);
            alphas.remove(0);
        }
    }
    
    
    public DefaultEnemyAttack(Pokemon attackingPokemon, Pokemon oppPokemon, int power, int accuracy, Action nextAction) {        

        this.pokemon = attackingPokemon;
        this.oppPokemon = oppPokemon;
        
        this.power = power;
        this.accuracy = accuracy;
        
        this.doneYet = false; //unused
        this.nextAction = nextAction;

        //consider doing relative positions from now on
        //this.position = new Vector2(104+4*3-2,200-6*3-2); //post scaling change
        this.position = new Vector2(this.pokemon.sprite.getX(), this.pokemon.sprite.getY());
        
        this.positions = new ArrayList<Vector2>();
        this.positions.add(new Vector2(-8,0));//move forward 1
        this.positions.add(new Vector2(8,0));//move back 1
        for (int i = 0; i < 13; i++) {
            this.positions.add(new Vector2(0,0));
        }
        
        
        
        this.sprites = new ArrayList<Sprite>(); //may use this in future
        for (int i = 0; i < 15; i++) {
            this.sprites.add(null);
        }
        
        
        this.repeats = new ArrayList<Integer>();
        this.repeats.add(7);
        this.repeats.add(3);
        this.repeats.add(17);
        this.repeats.add(6); 
        this.repeats.add(7); 
        this.repeats.add(5);
        this.repeats.add(9);
        this.repeats.add(4);
        this.repeats.add(8);
        this.repeats.add(6); 
        this.repeats.add(7); 
        this.repeats.add(5);
        this.repeats.add(9);
        this.repeats.add(4);
        this.repeats.add(11);


        this.alphas = new ArrayList<Float>();
        this.alphas.add(1f);
        this.alphas.add(1f);
        this.alphas.add(1f);
        for (int i = 0; i < 6; i++) {
            this.alphas.add(0f);
            this.alphas.add(1f);
        }
        

        this.sounds = new ArrayList<String>();
        this.sounds.add(null); //
        this.sounds.add("tackle1");
        for (int i = 0; i < 13; i++) {
            this.sounds.add(null);
        }
        
//        Texture text = new Texture(Gdx.files.internal("attack_menu/helper3.png"));
//        this.helperSprite = new Sprite(text, 0, 0, 160, 144);
    }
}






/*//unused code
 * 
 * //old safari menu rock throw pre-run code
String textString = "Wild "+game.battle.oppPokemon.name+" is angry!";
Action catchAction = new ThrowRock(game, new DisplayText(game, textString, null, null,
        this ));
textString = game.player.name+" threw a rock.";
PublicFunctions.insertToAS(game, new DisplayText(game, textString, null, catchAction,
        catchAction
    )
);


*/
