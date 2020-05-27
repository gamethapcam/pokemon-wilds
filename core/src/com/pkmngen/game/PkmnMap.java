package com.pkmngen.game;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.kryo.io.Output;
import com.pkmngen.game.Network.PokemonData;

import box2dLight.PointLight;

//grass - 
//part of player's head is above grass,
//part is beneath
//just use two draw actions
//likely need to mark tile as 'grass',
// for wild encounters (not positive tho)

class Tile {

    Map<String, Boolean> attrs;

    // temp fix to ledge direction thing
    // can't think of another place to put this.
    String ledgeDir;

    Vector2 position;

    Sprite sprite;
    Sprite overSprite; // ledges

    // route that this tile belongs to
    // used to signal movement to new routes
    Route routeBelongsTo;

    // this is just my idea for tiles that do something when player walks over
    // it,
    // like grass, warp tiles.
    // would probly rely on setting a game.map.player.canMove flag to false.
    // likely that playerMove function would call onWalkOver when player steps
    // onto new tile
    // tall grass wild encounter is currently in-lined in playerMoving action

    // if you need to see what type of tile this is, use name variable
    String name;
    // Needed to store which object is above the 'terrain'. Like a tree, rock, house piece, etc.
    // This must be stored because it needs to be sent over the network, and saved locally.
    String nameUpper = "";
    
    // for when you collect items from this tile
    HashMap<String, Integer> items = new HashMap<String, Integer>();

    public void onWalkOver() {

    }

    public void onPressA(Game game) {

    }
    public Tile(String tileName, Vector2 pos) {
        this(tileName, "", pos);
    }
    public Tile(String tileName, String nameUpper, Vector2 pos) {
        this(tileName, nameUpper, pos, false);
    }
    public Tile(String tileName, String nameUpper, Vector2 pos, boolean color) {
        this(tileName, nameUpper, pos, color, null);
    }
    public Tile(String tileName, Vector2 pos, boolean color) {
        this(tileName, "", pos, color, null);
    }
    public Tile(String tileName, Vector2 pos, boolean color, Route routeBelongsTo) {
        this(tileName, "", pos, color, routeBelongsTo);
    }
    public Tile(String tileName, String nameUpper, Vector2 pos, boolean color, Route routeBelongsTo) {

        // initialize attributes of tile
        this.attrs = new HashMap<String, Boolean>();
        this.attrs.put("solid", false);
        this.attrs.put("ledge", false);
        this.attrs.put("water", false);
        this.attrs.put("grass", false);
        this.attrs.put("tree",  false);

        this.name = tileName;
        this.nameUpper = nameUpper;  // object above the terrain, ie house, tree, rock etc.

        // this.attrs.put("qmark", false); //TODO - delete if unused//tile that
        // is available to be changed to another tile later

        this.position = pos;
        this.routeBelongsTo = routeBelongsTo;

        if (tileName.equals("ground1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("ground1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.equals("ground2")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("ground2.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.equals("block1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("block1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true); // block is solid
        } else if (tileName.equals("grass1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/blank2.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(Gdx.files.internal("grass1.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("grass", true);

            // this.sprite.setColor(new Color(.1f, .1f, .1f, 1f)); //debug
        } else if (tileName.equals("grass2")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/grass2_under.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(Gdx.files.internal("tiles/grass2_over.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("grass", true);
            this.attrs.put("cuttable", true);
            this.items.put("grass", 1);
            // this.sprite.setColor(new Color(.1f, .1f, .1f, 1f)); //debug
        } else if (tileName.equals("grass3")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/grass3_under.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(Gdx.files.internal("tiles/grass3_over.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("grass", true);

        } else if (tileName.equals("grass_sand1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/sand1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(Gdx.files.internal("tiles/grass2_over.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("grass", true);
        } else if (tileName.equals("flower1")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/flower1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.equals("ground3")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/ground3.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        
        } else if (tileName.equals("mountain1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/mountain1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.equals("mountain2")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/mountain2.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.equals("mountain3")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/mountain3.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.contains("mountain")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/"+tileName+".png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            if (tileName.contains("left")) {
                this.attrs.put("ledge", true);
                this.ledgeDir = "left";
            }
            else if (tileName.contains("right")) {
                this.attrs.put("ledge", true);
                this.ledgeDir = "right";
            }
            else if (tileName.contains("top")) {
                this.attrs.put("ledge", true);
                this.ledgeDir = "up";
            }
            else if (tileName.contains("bottom")) {
                this.attrs.put("ledge", true);
                this.ledgeDir = "down";
            }
            else if (!tileName.contains("inner")) {
                this.attrs.put("solid", true);
            }
        }

        else if (tileName.equals("tree_large1")) { //
            Texture playerText;
            if (color) {
                playerText = TextureCache.get(Gdx.files.internal("tiles/tree_large1_color.png"));
            }
            else {
                playerText = TextureCache.get(Gdx.files.internal("tiles/tree_large1.png"));
            }
            this.sprite = new Sprite(playerText, 0, 0, 32, 32);
            this.attrs.put("solid", true);
        } else if (tileName.equals("tree_large1_noSprite")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/blank.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        }

        else if (tileName.equals("ledge1_down")) {
            Texture playerText;
            if (color) {
                playerText = TextureCache.get(Gdx.files.internal("ledge1_down_color.png"));
            }
            else {
                playerText = TextureCache.get(Gdx.files.internal("ledge1_down.png"));
            }
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("ledge", true);
            this.ledgeDir = "down";

            // this.sprite.setColor(new Color(1f, 1f, 1f, 1f)); //debug
        } else if (tileName.equals("ledge1_left")) {
            Texture playerText;
            if (color) {
                playerText = TextureCache.get(Gdx.files.internal("ledge1_left_color.png"));
            }
            else {
                playerText = TextureCache.get(Gdx.files.internal("ledge1_left.png"));
            }
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("ledge", true);
            this.ledgeDir = "left";

            // this.sprite.setColor(new Color(.1f, .1f, .1f, 1f)); //debug
        } else if (tileName.equals("ledge1_right")) {
            Texture playerText;
            if (color) {
                playerText = TextureCache.get( Gdx.files.internal("ledge1_right_color.png"));
            }
            else {
                playerText = TextureCache.get( Gdx.files.internal("ledge1_right.png"));
            }
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("ledge", true);
            this.ledgeDir = "right";
        } else if (tileName.equals("ground2_top")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("ground2_top.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("ledge", true);
        } else if (tileName.equals("ledge1_corner_bl")) {
            Texture playerText;
            if (color) {
                playerText = TextureCache.get(Gdx.files.internal("ledge1_corner_bl_color.png"));
            }
            else {
                playerText = TextureCache.get(Gdx.files.internal("ledge1_corner_bl.png"));
            }
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        } else if (tileName.equals("ledge1_corner_br")) {
            Texture playerText;
            if (color) {
                playerText = TextureCache.get(Gdx.files.internal("ledge1_corner_br_color.png"));
            }
            else {
                playerText = TextureCache.get(Gdx.files.internal("ledge1_corner_br.png"));
            }
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        } else if (tileName.equals("ledge2_corner_tl")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("ledge2_corner_tl.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        } else if (tileName.equals("ledge2_corner_tr")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("ledge2_corner_tr.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        }

        else if (tileName.equals("ledge_grass_ramp")) {
            Texture playerText;
            if (color) {
                playerText = TextureCache.get(Gdx.files.internal("tiles/ledge_grass_ramp_color.png"));
            }
            else {
                playerText = TextureCache.get(Gdx.files.internal("tiles/ledge_grass_ramp.png"));
            }
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.equals("ledge_grass_safari_up")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/ledge_grass_safari_up.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("ledge", true); // TODO - need ledgeDir variable?
            this.ledgeDir = "up";
        } else if (tileName.equals("ledge_grass_down")) {
            Texture playerText;
            if (color) {
                playerText = TextureCache.get(Gdx.files.internal("tiles/ledge1_down_color.png"));
            }
            else {
                playerText = TextureCache.get(Gdx.files.internal("tiles/ledge_grass_down.png"));
            }
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("ledge", true);
            this.ledgeDir = "down";
        } else if (tileName.equals("ledge_grass_left")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/ledge_grass_left.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("ledge", true);
            this.ledgeDir = "left";
        } else if (tileName.equals("ledge_grass_right")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/ledge_grass_right.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("ledge", true);
            this.ledgeDir = "right";
        } else if (tileName.equals("ledge_grass_inside_tl")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/ledge_grass_inside_tl.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        } else if (tileName.equals("ledge_grass_inside_tr")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/ledge_grass_inside_tr.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        }

        // water
        else if (tileName.equals("water1")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/water1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
            this.attrs.put("water", true);
        } else if (tileName.equals("water1_ledge1_left")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/water1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
            this.attrs.put("water", true);
            playerText = TextureCache.get(
                    Gdx.files.internal("tiles/water1_ledge1_left.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.equals("water1_ledge1_right")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/water1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
            this.attrs.put("water", true);
            playerText = TextureCache.get(
                    Gdx.files.internal("tiles/water1_ledge1_right.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.equals("water1_ledge1_tl")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/water1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
            this.attrs.put("water", true);
            playerText = TextureCache.get(
                    Gdx.files.internal("tiles/water1_ledge1_tl.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.equals("water1_ledge1_top")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/water1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
            this.attrs.put("water", true);
            playerText = TextureCache.get(
                    Gdx.files.internal("tiles/water1_ledge1_top.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.equals("water1_ledge1_tr")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/water1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
            this.attrs.put("water", true);
            playerText = TextureCache.get(
                    Gdx.files.internal("tiles/water1_ledge1_tr.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
        }

        else if (tileName.equals("grass_short2")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/grass_short2.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.equals("grass_short3")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/grass_short3.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.equals("warp1_greyed")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/warp1_greyed.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.equals("warp1")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/warp1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        }

        // overw pokemon
        else if (tileName.equals("raikou_overw1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("ground1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(
                    Gdx.files.internal("pokemon/raikou_overw1.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        } else if (tileName.equals("entei_overw1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("ground1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(
                    Gdx.files.internal("pokemon/entei_overw1.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        } else if (tileName.equals("suicune_overw1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("ground1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(
                    Gdx.files.internal("pokemon/suicune_overw1.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        } else if (tileName.equals("mewtwo_overw1")) {
//            Texture playerText = TextureCache.get(Gdx.files.internal("ground1.png"));
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/blank.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(
                    Gdx.files.internal("pokemon/mewtwo_overworld1.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        } else if (tileName.equals("mega_gengar_overworld1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/blank.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(
                    Gdx.files.internal("pokemon/mgengar_overworld1.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        } else if (tileName.equals("solid")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/qmark_tile1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            // this.attrs.put("qmark", true); //TODO - remove if unused
            this.attrs.put("solid", true); // solid in case I need to put
                                            // foliage near 'solid' objects
        } else if (tileName.equals("bush1")) {
            Texture playerText;
            if (color) {
                playerText = TextureCache.get(Gdx.files.internal("tiles/bush2_color.png"));
                this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
                this.nameUpper = "bush2_color";
            }
            else {
                playerText = TextureCache.get(Gdx.files.internal("tiles/bush1.png"));
            }
            this.name = "green1";
            playerText = TextureCache.get(Gdx.files.internal("tiles/green1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
            this.attrs.put("cuttable", true);
            this.attrs.put("headbuttable", true);
            this.items.put("logs", 1);
        } else if (tileName.equals("bush2")) {
            Texture playerText;
            playerText = TextureCache.get(Gdx.files.internal("tiles/bush2_color.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(Gdx.files.internal("tiles/green1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        } else if (tileName.equals("tree_small1")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/tree_small1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        } else if (tileName.equals("rock1")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/rock1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        } else if (tileName.equals("rock2")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/rock2.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        } else if (tileName.equals("rock3")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/rock3.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        } else if (tileName.equals("sand1")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/sand1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        // grass-like ground
        } else if (tileName.contains("green")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/"+tileName+".png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        } else if (tileName.contains("snow")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/"+tileName+".png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
        // tall tree
        } else if (tileName.equals("tree1")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/tree1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 32);
            this.attrs.put("solid", true);
        // tall tree - one sprite over, one under
        } else if (tileName.equals("tree2")) {
//            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/tree2_under.png"));
//            this.sprite = new Sprite(playerText, 0, 0, 16, 32);
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/tree3_under.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(Gdx.files.internal("tiles/tree3_over.png"));
            this.overSprite = new Sprite(playerText, 0, 0, 16, 32);
            this.attrs.put("solid", true);
            this.attrs.put("tree", true);
        } else if (tileName.equals("tree4")) {
          Texture playerText = TextureCache.get(Gdx.files.internal("tiles/tree4_under.png"));
          this.sprite = new Sprite(playerText, 0, 0, 16, 16);
          playerText = TextureCache.get(Gdx.files.internal("tiles/tree4_over.png"));
          this.overSprite = new Sprite(playerText, 0, 0, 16, 32);
          this.attrs.put("solid", true);
          this.attrs.put("tree", true);
        } else if (tileName.equals("tree5")) {
//          Texture playerText = TextureCache.get(Gdx.files.internal("tiles/tree5_under.png"));
          Texture playerText = TextureCache.get(Gdx.files.internal("tiles/green1.png"));
          this.sprite = new Sprite(playerText, 0, 0, 16, 16);
//          playerText = TextureCache.get(Gdx.files.internal("tiles/tree5_over.png"));
          playerText = TextureCache.get(Gdx.files.internal("tiles/tree6.png"));
          this.overSprite = new Sprite(playerText, 0, 0, 16, 32);
          this.attrs.put("solid", true);
          this.attrs.put("tree", true);
          this.attrs.put("headbuttable", true);
        // colored water
        } else if (tileName.equals("water2")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/water2.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
            this.attrs.put("water", true);
        }
        else if (tileName.equals("water3")) {
            Texture playerText = TextureCache.get(
                    Gdx.files.internal("tiles/water3.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
            this.attrs.put("water", true);
        }
        else if (tileName.equals("black1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/blank3.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            this.attrs.put("solid", true);
        }
        else if (tileName.equals("campfire1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/campfire1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 20);
            this.attrs.put("solid", true);
        }
        else if (tileName.equals("sleeping_bag1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/sleeping_bag1.png"));
            this.sprite = new Sprite(playerText, 0, 0, 24, 16);
        }
        else {
            // just load from image file
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/buildings/"+tileName+".png"));
            this.sprite = new Sprite(playerText, 0, 0, 16, 16);
            if (!tileName.contains("door") && !tileName.contains("floor")) {
                this.attrs.put("solid", true);
            }
        }
        // TODO: refactors
        //  - just load file based on tile name. Names can probably contain slashes.
        //  - remove references above to setting overSprite, and just have whatever calls Tile() pass in nameUpper.
        //  - remove attrs, just do booleans isCuttable, isLedge, etc.
        // TODO: it would be even better if this.name was just in the format "lower:upper". 
        //  - that way code can just check tile.name once without checking both. Also keeps backwards compatibility.
        
        // if there is an 'upper' (above terrain' object, load it and set oversprite to it.
        if (!this.nameUpper.equals("")){
            // load from image file based on the name
            Texture text;
            if (this.nameUpper.contains("house")) {
                text = TextureCache.get(Gdx.files.internal("tiles/buildings/"+this.nameUpper+".png"));
            }
            else {
                text = TextureCache.get(Gdx.files.internal("tiles/"+this.nameUpper+".png"));
            }
            this.overSprite = new Sprite(text, 0, 0, 16, 16);
            if (!this.nameUpper.contains("door") && !this.nameUpper.contains("floor")) {
                this.attrs.put("solid", true);
            }
            if (!this.nameUpper.contains("floor")) {
                this.attrs.put("cuttable", true); 
            }
            if (this.nameUpper.equals("bush2_color")) {
                this.attrs.put("headbuttable", true);
            }
        }
        
        this.sprite.setPosition(pos.x, pos.y);
        if (this.overSprite != null) {
            this.overSprite.setPosition(pos.x, pos.y);
        }
    }

}

class SpecialMewtwoTile extends Tile {

    public SpecialMewtwoTile(Vector2 pos) {
        super("mewtwo_overw1", pos);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onPressA(Game game) {
        game.playerCanMove = false;
        PublicFunctions.insertToAS(game, new SpecialBattleMewtwo(game));
    }
}


class MegaGengarTile extends Tile {

    public MegaGengarTile(Vector2 pos) {
        super("mega_gengar_overworld1", pos);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onPressA(Game game) {
        game.playerCanMove = false;

        PublicFunctions.insertToAS(game, new SpecialBattleMegaGengar(game));

    }
}

// TODO - in future, will not handle these as tiles but as
// overworld pkmn. need easy way to access tile and overworld
// pkmn functions all at once
class Suicune_Tile extends Tile {

    public Suicune_Tile(Vector2 pos) {
        super("suicune_overw1", pos);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onPressA(Game game) {
        game.playerCanMove = false;

        // TODO - player can't move when returning from this
        game.battle.oppPokemon = new Pokemon("Suicune", 50);

        Action encounterAction = new SplitAction(
                new BattleIntro(
                        new BattleIntro_anim1(
                                new SplitAction(
                                        new DrawBattle(game),
                                        new BattleAnim_positionPlayers(
                                                game,
                                                new PlaySound(
                                                        game.battle.oppPokemon.name,
                                                        new DisplayText(
                                                                game,
                                                                "Wild "
                                                                        + game.battle.oppPokemon.name
                                                                                .toUpperCase()
                                                                        + " appeared!",
                                                                null,
                                                                null,
                                                                new WaitFrames(
                                                                        game,
                                                                        39,
                                                                        // demo
                                                                        // code
                                                                        // -
                                                                        // wildly
                                                                        // confusing,
                                                                        // but i
                                                                        // don't
                                                                        // want
                                                                        // to
                                                                        // write
                                                                        // another
                                                                        // if
                                                                        // statement
                                                                        game.player.adrenaline > 0 ? new DisplayText(
                                                                                game,
                                                                                ""
                                                                                        + game.player.name
                                                                                        + " has ADRENALINE "
                                                                                        + Integer
                                                                                                .toString(game.player.adrenaline)
                                                                                        + "!",
                                                                                null,
                                                                                null,
                                                                                new PrintAngryEating(
                                                                                        game, // for
                                                                                                // demo
                                                                                                // mode,
                                                                                                // normally
                                                                                                // left
                                                                                                // out
                                                                                        new DrawBattleMenu_SafariZone(
                                                                                                game,
                                                                                                new DoneAction())))
                                                                                : new PrintAngryEating(
                                                                                        game, // for
                                                                                                // demo
                                                                                                // mode,
                                                                                                // normally
                                                                                                // left
                                                                                                // out
                                                                                        new DrawBattleMenu_SafariZone(
                                                                                                game,
                                                                                                new DoneAction()))
                                                                //
                                                                ))))))),
                new WaitFrames(game, 10, new BattleIntroMusic(new WaitFrames(
                        game, 100, new DoneWithDemo(game)))));

        PublicFunctions.insertToAS(game, new DisplayText(game, "GROWL!!",
                "Suicune", null,
                // new PlayerCanMove(game,
                encounterAction));

    }
}

class Raikou_Tile extends Tile {

    public Raikou_Tile(Vector2 pos) {
        super("raikou_overw1", pos);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onPressA(Game game) {
        game.playerCanMove = false;

        // TODO - player can't move when returning from this
        game.battle.oppPokemon = new Pokemon("Raikou", 50);

        Action encounterAction = new SplitAction(
                new BattleIntro(
                        new BattleIntro_anim1(
                                new SplitAction(
                                        new DrawBattle(game),
                                        new BattleAnim_positionPlayers(
                                                game,
                                                new PlaySound(
                                                        game.battle.oppPokemon.name,
                                                        new DisplayText(
                                                                game,
                                                                "Wild "
                                                                        + game.battle.oppPokemon.name
                                                                                .toUpperCase()
                                                                        + " appeared!",
                                                                null,
                                                                null,
                                                                new WaitFrames(
                                                                        game,
                                                                        39,
                                                                        // demo
                                                                        // code
                                                                        // -
                                                                        // wildly
                                                                        // confusing,
                                                                        // but i
                                                                        // don't
                                                                        // want
                                                                        // to
                                                                        // write
                                                                        // another
                                                                        // if
                                                                        // statement
                                                                        game.player.adrenaline > 0 ? new DisplayText(
                                                                                game,
                                                                                ""
                                                                                        + game.player.name
                                                                                        + " has ADRENALINE "
                                                                                        + Integer
                                                                                                .toString(game.player.adrenaline)
                                                                                        + "!",
                                                                                null,
                                                                                null,
                                                                                new PrintAngryEating(
                                                                                        game, // for
                                                                                                // demo
                                                                                                // mode,
                                                                                                // normally
                                                                                                // left
                                                                                                // out
                                                                                        new DrawBattleMenu_SafariZone(
                                                                                                game,
                                                                                                new DoneAction())))
                                                                                : new PrintAngryEating(
                                                                                        game, // for
                                                                                                // demo
                                                                                                // mode,
                                                                                                // normally
                                                                                                // left
                                                                                                // out
                                                                                        new DrawBattleMenu_SafariZone(
                                                                                                game,
                                                                                                new DoneAction()))
                                                                //
                                                                ))))))),
                new WaitFrames(game, 10, new BattleIntroMusic(new WaitFrames(
                        game, 100, new DoneWithDemo(game)))));

        PublicFunctions.insertToAS(game, new DisplayText(game, "GROWL!!",
                "Raikou", null,
                // new PlayerCanMove(game,
                encounterAction));

    }
}

class Entei_Tile extends Tile {

    public Entei_Tile(Vector2 pos) {
        super("entei_overw1", pos);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onPressA(Game game) {
        game.playerCanMove = false;

        // TODO - player can't move when returning from this
        game.battle.oppPokemon = new Pokemon("Entei", 50);

        Action encounterAction = new SplitAction(
                new BattleIntro(
                        new BattleIntro_anim1(
                                new SplitAction(
                                        new DrawBattle(game),
                                        new BattleAnim_positionPlayers(
                                                game,
                                                new PlaySound(
                                                        game.battle.oppPokemon.name,
                                                        new DisplayText(
                                                                game,
                                                                "Wild "
                                                                        + game.battle.oppPokemon.name
                                                                                .toUpperCase()
                                                                        + " appeared!",
                                                                null,
                                                                null,
                                                                new WaitFrames(
                                                                        game,
                                                                        39,
                                                                        // demo
                                                                        // code
                                                                        // -
                                                                        // wildly
                                                                        // confusing,
                                                                        // but i
                                                                        // don't
                                                                        // want
                                                                        // to
                                                                        // write
                                                                        // another
                                                                        // if
                                                                        // statement
                                                                        game.player.adrenaline > 0 ? new DisplayText(
                                                                                game,
                                                                                ""
                                                                                        + game.player.name
                                                                                        + " has ADRENALINE "
                                                                                        + Integer
                                                                                                .toString(game.player.adrenaline)
                                                                                        + "!",
                                                                                null,
                                                                                null,
                                                                                new PrintAngryEating(
                                                                                        game, // for
                                                                                                // demo
                                                                                                // mode,
                                                                                                // normally
                                                                                                // left
                                                                                                // out
                                                                                        new DrawBattleMenu_SafariZone(
                                                                                                game,
                                                                                                new DoneAction())))
                                                                                : new PrintAngryEating(
                                                                                        game, // for
                                                                                                // demo
                                                                                                // mode,
                                                                                                // normally
                                                                                                // left
                                                                                                // out
                                                                                        new DrawBattleMenu_SafariZone(
                                                                                                game,
                                                                                                new DoneAction()))
                                                                //
                                                                ))))))),
                new WaitFrames(game, 10, new BattleIntroMusic(new WaitFrames(
                        game, 100, new DoneWithDemo(game)))));

        PublicFunctions.insertToAS(game, new DisplayText(game, "GROWL!!",
                "Entei", null,
                // new PlayerCanMove(game,
                encounterAction));

    }
}


class Route {

    String name;
    int level;

    // multiple entries to increase encounter rate
    ArrayList<Pokemon> pokemon = new ArrayList<Pokemon>();

    // pokemon list to pick from when populating route
    ArrayList<String> allowedPokemon;

    // try this for now?
    Music music;
    ArrayList<String> musics = new ArrayList<String>();
    int musicsIndex = 0;

    Random rand;

    /*
     * Constructor for loading from file or loading over network.
     */
    public Route(Network.RouteData routeData) {
//        this.rand = new Random();  // TODO: delete, should just use single random num generator which can be seeded.
        this.name = routeData.name;
        this.level = routeData.level;
        this.allowedPokemon = new ArrayList<String>(routeData.allowedPokemon);
        this.musics = new ArrayList<String>(routeData.musics);
        this.musicsIndex = routeData.musicsIndex;
        for (Network.PokemonData pokemonData : routeData.pokemon) {
            this.pokemon.add(new Pokemon(pokemonData));
        }
    }

    public Route(String name, int level) {

        this.name = name;
        this.level = level;

        this.pokemon = new ArrayList<Pokemon>();
        this.allowedPokemon = new ArrayList<String>();

        this.rand = new Random();

        if (name == "Route 1") {

            this.allowedPokemon.add("Electabuzz");
            this.allowedPokemon.add("Steelix");
            this.allowedPokemon.add("Tauros");
            this.allowedPokemon.add("Makuhita");
            this.allowedPokemon.add("Hariyama");
            this.allowedPokemon.add("Mareep");
            this.allowedPokemon.add("Scyther");

            // TODO - generate random pokemon based off of this.allowedPokemon
            // factor in route level
            // this.pokemon.add(new Pokemon("Electabuzz", 20)); //2
            // this.pokemon.add(new Pokemon("Steelix", 21)); //3
            // this.pokemon.add(new Pokemon("Tauros", 22));

            // TODO: remove
//            this.music = Gdx.audio.newMusic(Gdx.files.internal("route1_1.ogg"));
//            this.music.setLooping(true);
//            this.music.setVolume(.3f);
        }
        else if (name == "Demo_DarkRoute") {

            // ie sneasel, etc

            this.allowedPokemon.add("Shuppet");
            this.allowedPokemon.add("Cacnea");
            this.allowedPokemon.add("Spinarak");
            this.allowedPokemon.add("Oddish");
            this.allowedPokemon.add("Gloom");
            this.allowedPokemon.add("Sneasel");
            this.allowedPokemon.add("Sableye");

            // TODO: remove
//            this.music = Gdx.audio.newMusic(Gdx.files.internal("route1_1.ogg"));
//            this.music.setLooping(true);
//            this.music.setVolume(.3f);
        }
        else if (name == "Demo_SteelRoute") {

            // ie metagross, etc

            this.allowedPokemon.add("Makuhita");
            this.allowedPokemon.add("Hariyama");
            this.allowedPokemon.add("Shinx");
//            this.allowedPokemon.add("Starly");
            this.allowedPokemon.add("Zubat");
            this.allowedPokemon.add("Lairon");
            this.allowedPokemon.add("Steelix");

            // TODO: remove
//            this.music = Gdx.audio.newMusic(Gdx.files.internal("route1_1.ogg"));
//            this.music.setLooping(true);
//            this.music.setVolume(.3f);
        }
        else if (name == "Demo_PsychicRoute") {

            // ie metagross, etc

            this.allowedPokemon.add("Wurmple");
            this.allowedPokemon.add("Mareep");
            this.allowedPokemon.add("Flaaffy");
            this.allowedPokemon.add("Shinx");
            this.allowedPokemon.add("Starly");
            this.allowedPokemon.add("Gardevoir");
            this.allowedPokemon.add("Claydol");

            // TODO: remove
//            this.music = Gdx.audio.newMusic(Gdx.files.internal("route1_1.ogg"));
//            this.music.setLooping(true);
//            this.music.setVolume(.3f);
        }
        else if (name.equals("mountain1")) {
            this.allowedPokemon.add("rhydon");
            this.allowedPokemon.add("rhyhorn");
            this.allowedPokemon.add("onix");
            this.allowedPokemon.add("machop");
            this.allowedPokemon.add("solrock");
            // TODO: remove
//            this.music = Gdx.audio.newMusic(Gdx.files.internal("route1_1.ogg"));
//            this.music.setLooping(true);
//            this.music.setVolume(.3f);
        }
        else if (name.equals("forest1")) {
            this.allowedPokemon.add("oddish");
            this.allowedPokemon.add("gloom");
            this.allowedPokemon.add("pidgey");
            this.allowedPokemon.add("hoppip");
            this.allowedPokemon.add("machop");
            this.allowedPokemon.add("stantler");
            this.allowedPokemon.add("tauros");
            this.allowedPokemon.add("bulbasaur");
            this.allowedPokemon.add("charmander");
            this.allowedPokemon.add("chikorita");
            this.allowedPokemon.add("paras");
            this.allowedPokemon.add("pikachu");
            this.allowedPokemon.add("weedle");
            this.allowedPokemon.add("caterpie");
            this.allowedPokemon.add("spinarak");
            this.allowedPokemon.add("ledyba");
            // TODO: remove
//            this.music = Gdx.audio.newMusic(Gdx.files.internal("route1_1.ogg"));
//            this.music.setLooping(true);
//            this.music.setVolume(.3f);
        }
        else if (name.equals("beach1")) {
            this.allowedPokemon.add("squirtle");
            this.allowedPokemon.add("krabby");
            this.allowedPokemon.add("totodile");
            this.allowedPokemon.add("shellder");
            this.allowedPokemon.add("wooper");
            this.allowedPokemon.add("shuckle");
            this.allowedPokemon.add("staryu");
        }
        else if (name.equals("desert1")) {
            // TODO: these are just some ideas
            this.allowedPokemon.add("gible");
            this.allowedPokemon.add("gabite");
            // TODO: would be sweet if fully evolved forms were hard to escape from and catch
            //  ie, they were scary to run into.
            //  probably just make them high level.
            this.allowedPokemon.add("garchomp");   // high-level
            this.allowedPokemon.add("shieldon");
            this.allowedPokemon.add("cacnea");
            this.allowedPokemon.add("sandshrew");
            this.allowedPokemon.add("kangaskan");
            this.allowedPokemon.add("rhyhorn");
            this.allowedPokemon.add("shieldon");
            this.allowedPokemon.add("bastiodon");  // high-level
            this.allowedPokemon.add("skorupi");
            this.allowedPokemon.add("drapion");    // high-level
            this.allowedPokemon.add("trapinch");
        }
        else if (name.equals("snow1")) {
            this.allowedPokemon.add("larvitar");
            this.allowedPokemon.add("sneasel");
            this.allowedPokemon.add("weavile");
            this.allowedPokemon.add("snorunt");
            this.allowedPokemon.add("glalie");
            this.allowedPokemon.add("lairon");
            this.allowedPokemon.add("swinub");
            this.allowedPokemon.add("piloswine");
            // TODO: remove
//            this.music = Gdx.audio.newMusic(Gdx.files.internal("route1_1.ogg"));
//            this.music.setLooping(true);
//            this.music.setVolume(.3f);
        }
        else if (name.equals("")) {
            this.allowedPokemon.clear();
            // TODO: remove
//            this.music = Gdx.audio.newMusic(Gdx.files.internal("route1_1.ogg"));
//            this.music.setLooping(true);
//            this.music.setVolume(.3f);
        }
        else {
            this.allowedPokemon.clear();
//            this.pokemon.add(new Pokemon("kangaskhan", 22, Pokemon.Generation.CRYSTAL));
//            this.pokemon.add(new Pokemon("togekiss", 22, Pokemon.Generation.CRYSTAL));
            this.pokemon.add(new Pokemon("rhydon", 22, Pokemon.Generation.CRYSTAL));
            this.pokemon.add(new Pokemon("rhyhorn", 22, Pokemon.Generation.CRYSTAL));
            this.pokemon.add(new Pokemon("onix", 22, Pokemon.Generation.CRYSTAL));
            this.pokemon.add(new Pokemon("machop", 22, Pokemon.Generation.CRYSTAL));
            this.allowedPokemon.add("mamoswine");
            this.allowedPokemon.add("weavile");
            this.allowedPokemon.add("magmortar");
            this.allowedPokemon.add("electivire");
            this.allowedPokemon.add("metagross");
            // TODO: remove
//            this.music = Gdx.audio.newMusic(Gdx.files.internal("route1_1.ogg"));
//            this.music.setLooping(true);
//            this.music.setVolume(.3f);
        }
        genPokemon(256);
        
        // TODO: possibly different per-route
        this.musics.add("nature1_render");
//        this.musics.add("overw2");
        this.musics.add("route_42");
        this.musics.add("national_park1");
        this.musics.add("viridian_forest_gs");
        this.musics.add("route_3_gs");
        this.musics.add("route_1");
        this.musics.add("route_idk1");
        // TODO: victory road theme thing
        
        // TODO: mountain musics
        //  - ruins of alps theme?
        
        // TODO: debug, delete
//        this.pokemon.clear();
//        Pokemon debug = new Pokemon("Rhydon", 70, Pokemon.Generation.CRYSTAL);  // 22
//        debug.attacks[0] = "Whirlpool";
//        debug.attacks[1] = "Whirlpool";
//        debug.attacks[2] = "Whirlpool";
//        debug.attacks[3] = "Whirlpool";
//        this.pokemon.add(debug);

        /*
         * //below will add all from allowed pkmn for (String pokemonName :
         * this.allowedPokemon) { randomNum = rand.nextInt(3); //0, 1, 2
         * this.pokemon.add(new Pokemon(pokemonName, this.level+randomNum)); }
         */
    }
    
    // get next music. if random == true, get random one that isn't same as current
    public String getNextMusic(boolean random) {
        // for now, do nature sounds between each song
        if (this.musicsIndex != 0) {
            this.musicsIndex = 0;
            return this.musics.get(this.musicsIndex);
        }
        // TODO: this doesn't work given the above code
        int nextIndex = 0;
        if (random) {
            nextIndex = this.musicsIndex;
            while (nextIndex == this.musicsIndex || nextIndex == 0) {
                nextIndex = Game.staticGame.map.rand.nextInt(this.musics.size());
            }
        }
        else {
            nextIndex = (this.musicsIndex + 1) % this.musics.size();
        }
        this.musicsIndex = nextIndex;
        return this.musics.get(this.musicsIndex);
    }

    /*
     * This will bring the route pkmn count back to 4.
     * 
     * TODO: probably should have more than 4 pokemon.
     */
    public void genPokemon(int maxCatchRate) {

        // TODO - bug if maxed out on catch rates, and need to repeat a pkmn

        // if less than needed pokemon for randomization, just use them all.
        if (this.allowedPokemon.size() < 4) {
            for (String pokemonName : this.allowedPokemon) {
                this.pokemon.add(new Pokemon(pokemonName, this.level + rand.nextInt(3), Pokemon.Generation.CRYSTAL));
            }
            return;
        }
        
        int randomNum;
        int randomLevel;
        String pokemonName;
        ArrayList<String> usedPokemon = new ArrayList<String>();
        for (Pokemon pkmn : this.pokemon) {
            usedPokemon.add(pkmn.name);
        }

        // below will add from allowed pkmn based on catchRate
        while (this.pokemon.size() < 4) { // 4 total pokemon in route
            randomNum = rand.nextInt(this.allowedPokemon.size()); // 0, 1, 2
            randomLevel = rand.nextInt(3); // 0, 1, 2
            pokemonName = this.allowedPokemon.get(randomNum);
            // this breaks if less than 5 available pokemon in route
            if (usedPokemon.contains(pokemonName) && this.allowedPokemon.size() > 4) {
                continue;
            }
            Pokemon tempPokemon = new Pokemon(pokemonName, this.level + randomLevel, Pokemon.Generation.CRYSTAL);
            usedPokemon.add(pokemonName);
            this.pokemon.add(tempPokemon);

            // no idea what this stuff did - comment for now
//            if (tempPokemon.baseStats.get("catchRate") > maxCatchRate) {
//                continue;
//            }
//            // add it based on pokemon catchRate
//            randomNum = rand.nextInt(256);
//            if (randomNum < tempPokemon.baseStats.get("catchRate")) {
//                usedPokemon.add(pokemonName);
//                this.pokemon.add(tempPokemon);
//            }
        }
        for (Pokemon pokemon : this.pokemon) {
            System.out.println("curr route pokemon: "
                    + String.valueOf(pokemon.name));
        }
    }

}

public class PkmnMap {

    // it looks like .equals() is called here, so this method is valid.
    // ie, passing a new vector to search for a tile is fine
    Map<Vector2, Tile> overworldTiles = new HashMap<Vector2, Tile>();
    Map<Vector2, Tile> tiles = overworldTiles;

    ArrayList<HashMap<Vector2, Tile>> interiorTiles = new ArrayList<HashMap<Vector2, Tile>>();
    int interiorTilesIndex = 100;

    // use this to drop the tops of trees over the player
    //  hopefully makes drawing take less time
//    Map<Vector2, Tile> trees = new HashMap<Vector2, Tile>();
    
    // routes on map
    ArrayList<Route> routes;
    // debating whether I should just make tiles have references
    // to route objects, and not have currRoute here
    Route currRoute;

    // needed for wild encounters etc
    Random rand;

    String timeOfDay = "Day";  // used by cycleDayNight
    
    String id;  // needed for saving to file

    public PkmnMap(String mapName) {

        this.id = mapName;
        Vector2 pos;
        this.rand = new Random();
        
        // init interior tiles
        // I couldn't figure out how to make this a normal array
        // higher numbers == higher layers in y direction
        for (int i=0; i < 100; i++) {
            this.interiorTiles.add(null);
        }
        this.interiorTiles.add(new HashMap<Vector2, Tile>());

        if (mapName == "default") {

            /*
             * 
             * //for now, just load this manually for (int i = 0; i < 10; i++) {
             * for (int j = 0; j < 10; j++) { pos = new Vector2(i*16, j*16);
             * this.tiles.put(pos, new Tile("ground1", pos)); } }
             * 
             * for (int i = 0; i < 10; i++) { pos = new Vector2(i*16, 10*16);
             * this.tiles.put(pos, new Tile("block1", pos));
             * 
             * pos = new Vector2(i*16, -1*16); this.tiles.put(pos, new
             * Tile("block1", pos)); }
             * 
             * for (int j = 0; j < 8; j++) { pos = new Vector2(-1*16, j*16);
             * this.tiles.put(pos, new Tile("block1", pos));
             * 
             * pos = new Vector2(16*16, j*16); this.tiles.put(pos, new
             * Tile("block1", pos)); }
             * 
             * //grass in middle for (int i = 4; i < 6; i++) { for (int j = 4; j
             * < 6; j++) { //hopefully will overwrite? pos = new Vector2(i*16,
             * j*16); this.tiles.put(pos, new Tile("grass1", pos)); } }
             * 
             * //ledge pos = new Vector2(4*16, 2*16); this.tiles.put(pos, new
             * Tile("ledge1_down", pos)); //ledge pos = new Vector2(3*16, 3*16);
             * this.tiles.put(pos, new Tile("ledge1_right", pos)); //ledge pos =
             * new Vector2(4*16, 4*16); this.tiles.put(pos, new
             * Tile("ledge1_left", pos));
             */

            // pos = new Vector2(16, 16);
            // this.tiles.put(pos, new Tile("ground2", pos));

            // set route
            // in future, need tiles to change route
            // this.currRoute = new Route("Route 1", 20);
            this.currRoute = new Route("Demo_SteelRoute", 20); // handled in
                                                                // other actions

        }

        else if (mapName == "Demo_Legendary1") {

            for (int i = -3; i < 8; i++) {
                pos = new Vector2(-32, i * 32 - 16);
                this.tiles.put(pos.cpy(), new Tile("tree_large1", pos.cpy()));
                this.tiles.put(pos.cpy().add(16, 0), new Tile(
                        "tree_large1_noSprite", pos.cpy().add(16, 0)));
                this.tiles.put(pos.cpy().add(0, 16), new Tile(
                        "tree_large1_noSprite", pos.cpy().add(0, 16)));
                this.tiles.put(pos.cpy().add(16, 16), new Tile(
                        "tree_large1_noSprite", pos.cpy().add(16, 16)));
                pos = new Vector2(-64, i * 32 - 16);
                this.tiles.put(pos.cpy(), new Tile("tree_large1", pos.cpy()));
                this.tiles.put(pos.cpy().add(16, 0), new Tile(
                        "tree_large1_noSprite", pos.cpy().add(16, 0)));
                this.tiles.put(pos.cpy().add(0, 16), new Tile(
                        "tree_large1_noSprite", pos.cpy().add(0, 16)));
                this.tiles.put(pos.cpy().add(16, 16), new Tile(
                        "tree_large1_noSprite", pos.cpy().add(16, 16)));
                pos = new Vector2(16, i * 32 - 16);
                this.tiles.put(pos.cpy(), new Tile("tree_large1", pos.cpy()));
                this.tiles.put(pos.cpy().add(16, 0), new Tile(
                        "tree_large1_noSprite", pos.cpy().add(16, 0)));
                this.tiles.put(pos.cpy().add(0, 16), new Tile(
                        "tree_large1_noSprite", pos.cpy().add(0, 16)));
                this.tiles.put(pos.cpy().add(16, 16), new Tile(
                        "tree_large1_noSprite", pos.cpy().add(16, 16)));
                pos = new Vector2(48, i * 32 - 16);
                this.tiles.put(pos.cpy(), new Tile("tree_large1", pos.cpy()));
                this.tiles.put(pos.cpy().add(16, 0), new Tile(
                        "tree_large1_noSprite", pos.cpy().add(16, 0)));
                this.tiles.put(pos.cpy().add(0, 16), new Tile(
                        "tree_large1_noSprite", pos.cpy().add(0, 16)));
                this.tiles.put(pos.cpy().add(16, 16), new Tile(
                        "tree_large1_noSprite", pos.cpy().add(16, 16)));
                pos = new Vector2(80, i * 32 - 16);
                this.tiles.put(pos.cpy(), new Tile("tree_large1", pos.cpy()));
                this.tiles.put(pos.cpy().add(16, 0), new Tile(
                        "tree_large1_noSprite", pos.cpy().add(16, 0)));
                this.tiles.put(pos.cpy().add(0, 16), new Tile(
                        "tree_large1_noSprite", pos.cpy().add(0, 16)));
                this.tiles.put(pos.cpy().add(16, 16), new Tile(
                        "tree_large1_noSprite", pos.cpy().add(16, 16)));
            }

            pos = new Vector2(0, -16);
            this.tiles.put(pos, new Tile("block1", pos));
            pos = new Vector2(0, 32 * 6 - 16);
            this.tiles.put(pos, new Tile("block1", pos));

            pos = new Vector2(0, 32 * 5);

            // choose randomly between raikou, entei and suicune as legendary
            // pkmn to appear
            int randNum = this.rand.nextInt(3); // 0,1,2
            if (randNum == 0) {
                this.tiles.put(pos, new Raikou_Tile(pos));
            } else if (randNum == 1) {
                this.tiles.put(pos, new Entei_Tile(pos));
            } else {
                this.tiles.put(pos, new Suicune_Tile(pos));
            }

            this.currRoute = new Route("Route 1", 20);
        }

        else if (mapName.equals("SpecialMewtwo")) {

//            for (int i = -3; i < 8; i++) {
//                pos = new Vector2(-32, i * 32 - 16);
//                this.tiles.put(pos.cpy(), new Tile("tree_large1", pos.cpy()));
//                this.tiles.put(pos.cpy().add(16, 0), new Tile(
//                        "tree_large1_noSprite", pos.cpy().add(16, 0)));
//                this.tiles.put(pos.cpy().add(0, 16), new Tile(
//                        "tree_large1_noSprite", pos.cpy().add(0, 16)));
//                this.tiles.put(pos.cpy().add(16, 16), new Tile(
//                        "tree_large1_noSprite", pos.cpy().add(16, 16)));
//                pos = new Vector2(-64, i * 32 - 16);
//                this.tiles.put(pos.cpy(), new Tile("tree_large1", pos.cpy()));
//                this.tiles.put(pos.cpy().add(16, 0), new Tile(
//                        "tree_large1_noSprite", pos.cpy().add(16, 0)));
//                this.tiles.put(pos.cpy().add(0, 16), new Tile(
//                        "tree_large1_noSprite", pos.cpy().add(0, 16)));
//                this.tiles.put(pos.cpy().add(16, 16), new Tile(
//                        "tree_large1_noSprite", pos.cpy().add(16, 16)));
//                pos = new Vector2(16, i * 32 - 16);
//                this.tiles.put(pos.cpy(), new Tile("tree_large1", pos.cpy()));
//                this.tiles.put(pos.cpy().add(16, 0), new Tile(
//                        "tree_large1_noSprite", pos.cpy().add(16, 0)));
//                this.tiles.put(pos.cpy().add(0, 16), new Tile(
//                        "tree_large1_noSprite", pos.cpy().add(0, 16)));
//                this.tiles.put(pos.cpy().add(16, 16), new Tile(
//                        "tree_large1_noSprite", pos.cpy().add(16, 16)));
//                pos = new Vector2(48, i * 32 - 16);
//                this.tiles.put(pos.cpy(), new Tile("tree_large1", pos.cpy()));
//                this.tiles.put(pos.cpy().add(16, 0), new Tile(
//                        "tree_large1_noSprite", pos.cpy().add(16, 0)));
//                this.tiles.put(pos.cpy().add(0, 16), new Tile(
//                        "tree_large1_noSprite", pos.cpy().add(0, 16)));
//                this.tiles.put(pos.cpy().add(16, 16), new Tile(
//                        "tree_large1_noSprite", pos.cpy().add(16, 16)));
//                pos = new Vector2(80, i * 32 - 16);
//                this.tiles.put(pos.cpy(), new Tile("tree_large1", pos.cpy()));
//                this.tiles.put(pos.cpy().add(16, 0), new Tile(
//                        "tree_large1_noSprite", pos.cpy().add(16, 0)));
//                this.tiles.put(pos.cpy().add(0, 16), new Tile(
//                        "tree_large1_noSprite", pos.cpy().add(0, 16)));
//                this.tiles.put(pos.cpy().add(16, 16), new Tile(
//                        "tree_large1_noSprite", pos.cpy().add(16, 16)));
//            }
//
//            pos = new Vector2(0, -16);
//            this.tiles.put(pos, new Tile("block1", pos));
//            pos = new Vector2(0, 32 * 6 - 16);
//            this.tiles.put(pos, new Tile("block1", pos));

//            pos = new Vector2(0, 32 * 5);
            pos = new Vector2(0, 16 * 5);

            // tile to trigger special mewtwo battle
            this.tiles.put(pos, new SpecialMewtwoTile(pos));

            this.currRoute = new Route("Route 1", 20);
        }
    }
    
    public void loadFromFile(Game game) {
        // If map exists as file, load it
        try {
            // TODO: tile Route is not saved here.
            // Should be able to serialize route pokemon and save. Although might be ineffecient to save all
            //  pokemon details.
            InputStream inputStream = new InflaterInputStream(new FileInputStream(this.id + ".sav"));
            com.esotericsoftware.kryo.io.Input input = new com.esotericsoftware.kryo.io.Input(inputStream);
//            com.esotericsoftware.kryo.io.Input input = new com.esotericsoftware.kryo.io.Input(new FileInputStream(this.id + ".sav"));  // uncompressed
            Network.MapTiles mapTiles = game.server.getKryo().readObject(input, Network.MapTiles.class);
            input.close();
            this.tiles.clear();
            HashMap<String, Route> loadedRoutes = new HashMap<String, Route>();
            for (Network.TileData tileData : mapTiles.tiles) {
                // store unique routes as hashmap ClassID->Route
                if (tileData.routeBelongsTo != null && !loadedRoutes.containsKey(tileData.routeBelongsTo)) {
                    loadedRoutes.put(tileData.routeBelongsTo, new Route(mapTiles.routes.get(tileData.routeBelongsTo)));
                }
                Route tempRoute = loadedRoutes.get(tileData.routeBelongsTo);
                this.tiles.put(tileData.pos.cpy(), new Tile(tileData.tileName,
                                                            tileData.tileNameUpper,
                                                            tileData.pos.cpy(),
                                                            true,
                                                            tempRoute));
            }
            // load time of day
            game.map.timeOfDay = mapTiles.timeOfDay;
            cycleDayNight.dayTimer = mapTiles.dayTimer;
            
            // load players
            inputStream = new InflaterInputStream(new FileInputStream(this.id + ".players.sav"));
            input = new com.esotericsoftware.kryo.io.Input(inputStream);
            ArrayList<Network.PlayerData> players = game.server.getKryo().readObject(input, ArrayList.class);
            for (Network.PlayerData playerData : players) {
                Player player = new Player(playerData);
                player.type = Player.Type.REMOTE;  // TODO: store in playerData?
                game.players.put(playerData.id, player);
            }
            
            
        } catch (FileNotFoundException e) {
            System.out.println("No save file found for map: " + this.id);
        }
    }
    
    /*
     * TODO: for large maps, will likely need to split into multiple files corresponding to 
     * different map regions. Then save regions periodically. Ideally only when cpu cycles
     * are available.
     */
    public static class PeriodicSave extends Action {

        float timeDelta = 60;
        float saveInterval = 10; //TODO: debug, was 60  // Every minute for now
        
        // TODO: map id's?
        OutputStream outputStream;
        Output output;
        
        public int getLayer() { return 500;}

        @Override
        public void step(Game game) {
            // TODO: this only works for server atm, because I'm using game.server.getKryo() below.
            // Could change to also use game.server.getKryo() if I need for client.
            if (game.type != Game.Type.SERVER) {
                return;
            }
            this.timeDelta += Gdx.graphics.getDeltaTime();
            if (this.timeDelta >= this.saveInterval) {
                this.timeDelta = 0f;
                System.out.println("Saving map tiles to file...");
                try {
                    this.outputStream = new DeflaterOutputStream(new FileOutputStream(game.map.id + ".sav"));
//                    this.output = new Output(new FileOutputStream(game.map.id + ".sav"));  // uncompressed
                    this.output = new Output(this.outputStream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Network.MapTiles mapTiles = new Network.MapTiles();
                for (Tile tile : game.map.tiles.values()) {
                    // store unique routes as hashmap ClassID->Route
                    if (tile.routeBelongsTo != null && !mapTiles.routes.containsKey(tile.routeBelongsTo.toString())) {
                        mapTiles.routes.put(tile.routeBelongsTo.toString(), new Network.RouteData(tile.routeBelongsTo));
                    }
                    mapTiles.tiles.add(new Network.TileData(tile));
                }
                mapTiles.timeOfDay = game.map.timeOfDay;
                mapTiles.dayTimer = cycleDayNight.dayTimer;
                game.server.getKryo().writeObject(this.output, mapTiles);
                this.output.close();
                
                // Save players to separate file
                System.out.println("Saving players to file...");
                try {
                    this.outputStream = new DeflaterOutputStream(new FileOutputStream(game.map.id + ".players.sav"));
                    this.output = new Output(this.outputStream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                ArrayList<Network.PlayerData> players = new ArrayList<Network.PlayerData>();
                for (Player player : game.players.values()) {
                    players.add(new Network.PlayerData(player));
                }
                game.server.getKryo().writeObject(this.output, players);
                this.output.close();
                System.out.println("Done.");
            }
        }
        
        public PeriodicSave(Game game) {
            // Ends up under ...\pokemon_world_gen\Workspace\desktop\<game.map.id>.sav
            // DeflaterOutputStream <- output compression.
//            this.outputStream = new DeflaterOutputStream(new FileOutputStream(game.map.id + ".sav"));
        }
    }

}

// debug for drawing lab floor 1 bg
class DrawSpecialMewtwoBg extends Action {
    

    public int layer = 141;

    public int getLayer() {
        return this.layer;
    }
    
    Sprite bgSprite;

    @Override
    public void step(Game game) {
        
        this.bgSprite.draw(game.mapBatch);
        
    }
    
    public DrawSpecialMewtwoBg() {
        Texture text = TextureCache.get(Gdx.files.internal("lab1_fl1.png"));
        this.bgSprite = new Sprite(text, 0, 0, 479, 448);
        
        this.bgSprite.setPosition(-80 +1, -242 +1 +16*4);
    }
    
}

// TODO - bug where grass tiles move out of sync with normal
// probably because drawmap is before a camera update, drawGrass is after

class DrawMap extends Action { // /

    public int layer = 140;

    public int getLayer() {
        return this.layer;
    }

    Sprite blankSprite;
    Pixmap pixels;
    Texture texture;
    Vector3 startPos;
    Vector3 endPos;
    Vector3 worldCoordsTL;
    Vector3 worldCoordsBR;
    Tile tile;

    Sprite spritePart;
    Sprite zSprite;
    int zsTimer = 0;

    @Override
    public void step(Game game) {

        if (Gdx.input.isKeyPressed(Input.Keys.P)) {
            System.out.println("Camera unproject");
            System.out.println(java.time.LocalTime.now());  
        }
//        System.out.println(game.currScreen.x);
//        System.out.println(game.currScreen.y);

        // draw every sprite in the map
//        for (Tile tile : new ArrayList<Tile>(game.map.tiles.values())) {
        // TODO: this screws up when screen resizes
        // trying to draw tl to br of screen, so that sprites layer on top of each other
        worldCoordsTL = game.cam.unproject(new Vector3(-256, 0, 0f));
        worldCoordsBR = game.cam.unproject(new Vector3(game.currScreen.x, game.currScreen.y+128, 0f));
        worldCoordsTL.x = (int)worldCoordsTL.x - (int)worldCoordsTL.x % 16;
        worldCoordsTL.y = (int)worldCoordsTL.y - (int)worldCoordsTL.y % 16;
        worldCoordsBR.x = (int)worldCoordsBR.x - (int)worldCoordsBR.x % 16;
        worldCoordsBR.y = (int)worldCoordsBR.y - (int)worldCoordsBR.y % 16;
//        System.out.println(game.viewport.getScreenWidth());
//        System.out.println(game.viewport.getScreenHeight());
        this.startPos = worldCoordsTL; //new Vector3(worldCoordsTL.x, worldCoordsTL.y, 0f);
        this.endPos = worldCoordsBR; // new Vector3(worldCoordsBR.x, worldCoordsBR.y, 0f);
        
        // debug, remove
        int numTiles = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.P)) {
            System.out.println("Start draw tiles");
            System.out.println(java.time.LocalTime.now());  
        }
        
        for (Vector2 currPos = new Vector2(startPos.x, startPos.y); currPos.y > endPos.y;) {
            tile = game.map.tiles.get(currPos);
            currPos.x += 16;
            if (currPos.x > endPos.x) {
                currPos.x = startPos.x;
                currPos.y -= 16;
            }
            if (tile == null) {
                continue;
            }
            //debug, remove
            numTiles++;
//          for (Tile tile : game.map.tiles.values()) {
//            // Don't draw sprites if not in camera view
//            // note - when zoomed out, game will lag
            if (!game.cam.frustum.pointInFrustum(tile.position.x, tile.position.y, game.cam.position.z) &&
                    !game.cam.frustum.pointInFrustum(tile.position.x+tile.sprite.getWidth(), tile.position.y+tile.sprite.getHeight(), game.cam.position.z) &&
                    !game.cam.frustum.pointInFrustum(tile.position.x+tile.sprite.getWidth(), tile.position.y, game.cam.position.z) &&
                    !game.cam.frustum.pointInFrustum(tile.position.x, tile.position.y+tile.sprite.getHeight(), game.cam.position.z)) {
               continue;
            }
            
            // TODO: this attempted to create one bg sprite out of map tiles, couldn't get pixmap resizing to work (ie it just
            // remained 16x16 at all times
            // removing from map tiles removed collision data
            // if I were to do this in the future, just put this login in the Tile() constructor
            // draw to game.map.bgsprite on construction. 
//            Texture text = tile.sprite.getTexture();
//            if (!text.getTextureData().isPrepared()) {
//                text.getTextureData().prepare();
//            }
//            Pixmap pixmap = text.getTextureData().consumePixmap();
//            this.pixels.drawPixmap(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight(),
//                                   (int)tile.position.x, (int)tile.position.y,
//                                   (int)tile.sprite.getWidth(), (int)tile.sprite.getHeight());
////            this.texture.draw(pixmap, (int)tile.position.x, (int)tile.position.y);
//
//            if (tile.overSprite != null) {
//                text = tile.overSprite.getTexture();
//                if (!text.getTextureData().isPrepared()) {
//                    text.getTextureData().prepare();
//                }
//                pixmap = text.getTextureData().consumePixmap();
////                this.texture.draw(pixmap, (int)tile.position.x, (int)tile.position.y);
//                this.pixels.drawPixmap(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight(),
//                                       (int)tile.position.x, (int)tile.position.y,
//                                       (int)tile.sprite.getWidth(), (int)tile.sprite.getHeight());
//            }
//            game.map.tiles.remove(tile.position);
//            this.texture = TextureCache.get(this.pixels);

            game.mapBatch.draw(tile.sprite, tile.sprite.getX(), tile.sprite.getY());
            // tile.sprite.draw(game.batch);

            // oversprite is often ledges
            if (tile.overSprite != null) {
                game.mapBatch.draw(tile.overSprite, tile.overSprite.getX(), tile.overSprite.getY());
                // tile.overSprite.draw(game.batch); //doesn't allow
                // coloring via batch //TODO - remove
            }
            
            /*
             * I regret everything, shouldn't have tried to do this at all.
             * TODO: remove if unused.
             */
//            // if this is same position as player, draw player
//            if (game.player.position.y >= currPos.y-16 && game.player.position.y < currPos.y+16 && game.player.position.x >= currPos.x-32 && game.player.position.x < currPos.x) {
//                // draw player lower
//                if (!game.player.isSleeping) {
//                    this.spritePart = new Sprite(game.player.currSprite);
//                    this.spritePart.setRegionY(8);
//                    this.spritePart.setRegionHeight(8);
//                    game.batch.draw(this.spritePart, game.player.position.x, game.player.position.y+4);
//                }
//                // draw grass over lower part of player
//                if (tile.attrs.get("grass") == true) {
//                    game.batch.draw(tile.overSprite, tile.sprite.getX(), tile.sprite.getY());
//                }
//                // TODO: shouldn't be doing this here, need to refactor map draw action
//                if (tile.nameUpper.contains("sleeping_bag")) {
//                    tile.overSprite.draw(game.batch);
//                }
//                
//                // draw player upper
//                if (game.player.isSleeping) {
//                    if (this.zsTimer < 64) {
//                        this.zSprite.setPosition(game.player.position.x+8, game.player.position.y+18);
//                    }
//                    else {
//                        this.zSprite.setPosition(game.player.position.x+16, game.player.position.y+18);
//                    }
//                    this.zsTimer++;
//                    if (this.zsTimer >= 128) {
//                        this.zsTimer = 0;
//                    }
//                    this.zSprite.draw(game.batch);
//                }
//                else {
//                    // TODO: this is broken
//                    // draw building tile if building
//                    if (game.player.isBuilding) {
//                        // get direction facing 
//                        Vector2 pos = new Vector2(0,0);
//                        if (game.player.dirFacing == "right") {
//                            pos = new Vector2(game.player.position.cpy().add(16,0));
//                        }
//                        else if (game.player.dirFacing == "left") {
//                            pos = new Vector2(game.player.position.cpy().add(-16,0));
//                        }
//                        else if (game.player.dirFacing == "up") {
//                            pos = new Vector2(game.player.position.cpy().add(0,16));
//                        }
//                        else if (game.player.dirFacing == "down") {
//                            pos = new Vector2(game.player.position.cpy().add(0,-16));
//                        }
//                        // get game.player.currBuildTile and draw it at position
//                        Sprite sprite = new Sprite(game.player.currBuildTile.sprite);
//                        sprite.setAlpha(.8f);
//                        sprite.setPosition(pos.x, pos.y);
//                        Tile nextTile = game.map.tiles.get(pos);
//                        if (nextTile != null && nextTile.attrs.get("solid")) {
//                            sprite.setColor(1f, .7f, .7f, .8f);
//                        }
//                        boolean requirementsMet = true;
//                        for (String reqName : game.player.buildTileRequirements.get(game.player.currBuildTile.name).keySet()) {
//                            if (!game.player.itemsDict.containsKey(reqName)) {
//                                requirementsMet = false;
//                                break;
//                            }
//                            int playerOwns = game.player.itemsDict.get(reqName);
//                            if (playerOwns < game.player.buildTileRequirements.get(game.player.currBuildTile.name).get(reqName)) {
//                                requirementsMet = false;
//                                break;
//                            }
//                        }
//                        if (!requirementsMet) {
//                            sprite.setColor(1f, .7f, .7f, .8f);
//                        }
//                        sprite.draw(game.batch);
//                        if (game.player.currBuildTile.overSprite != null) {
//                            sprite = new Sprite(game.player.currBuildTile.overSprite);
//                            sprite.setAlpha(.8f);
//                            sprite.setPosition(pos.x, pos.y);
//                            if (nextTile != null && nextTile.attrs.get("solid")) {
//                                sprite.setColor(1f, .7f, .7f, .8f);
//                            }
//                            sprite.draw(game.batch);
//                        }
//                    }
//                    this.spritePart = new Sprite(game.player.currSprite);
//                    this.spritePart.setRegionY(0);
//                    this.spritePart.setRegionHeight(8);
//                    game.batch.draw(this.spritePart, game.player.position.x, game.player.position.y+12);
//                    game.player.currSprite.setPosition(game.player.position.x, game.player.position.y);  // this needs to be set to detect collision
//                }
//            }
        }

        // Draw other players
        for (Player player : game.players.values()) {
            // TODO: could check for player in frustum, not checking for now
            player.currSprite.setPosition(player.position.x, player.position.y+4);
//            game.batch.draw(player.currSprite, player.position.x, player.position.y);
            player.currSprite.draw(game.mapBatch);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.P)) {
            System.out.println("Drew tiles");
            System.out.println(numTiles);
            System.out.println(java.time.LocalTime.now());  
        }
//        game.batch.draw(this.texture, 5, 5);
    }

    public DrawMap(Game game) {
        this.pixels = new Pixmap(Gdx.files.internal("tiles/blank2.png"));
        this.texture = new Texture(this.pixels);
        this.blankSprite = new Sprite(TextureCache.get(Gdx.files.internal("tiles/blank2.png")), 0, 0, 16, 16);
        this.zSprite = new Sprite(TextureCache.get(Gdx.files.internal("tiles/zs1.png")), 0, 0, 16, 16);
    }

}

// TODO: this is doubling as animating campfire
// moves water back and forth
// also - I am going to add a sprite below each grass here
// needed for sprite coloring
class MoveWater extends Action {

    public int layer = 110;

    public int getLayer() {
        return this.layer;
    }

    ArrayList<Vector2> positions;
    Vector2 position;
    ArrayList<Integer> repeats;

    Sprite blankSprite;
    Pixmap pixels;
    Texture texture;
    Vector3 startPos;
    Vector3 endPos;
    Vector3 worldCoordsTL;
    Vector3 worldCoordsBR;
    Tile tile;
    Sprite[] campfireSprites;
    int campfireTimer = 0;
    PointLight pointLight;

    @Override
    public void step(Game game) {
        worldCoordsTL = game.cam.unproject(new Vector3(-256, -256, 0f));
        worldCoordsBR = game.cam.unproject(new Vector3(game.currScreen.x+256, game.currScreen.y+256, 0f));
        worldCoordsTL.x = (int)worldCoordsTL.x - (int)worldCoordsTL.x % 16;
        worldCoordsTL.y = (int)worldCoordsTL.y - (int)worldCoordsTL.y % 16;
        worldCoordsBR.x = (int)worldCoordsBR.x - (int)worldCoordsBR.x % 16;
        worldCoordsBR.y = (int)worldCoordsBR.y - (int)worldCoordsBR.y % 16;
        this.startPos = worldCoordsTL; //new Vector3(worldCoordsTL.x, worldCoordsTL.y, 0f);
        this.endPos = worldCoordsBR; // new Vector3(worldCoordsBR.x, worldCoordsBR.y, 0f);

        // set sprite position
        // if done with anim, do nextAction
        if (positions.isEmpty()) {
            resetVars();
            // game.actionStack.remove(this);
            // return;
        }

        this.position = positions.get(0);
        // positions.set(0,new Vector2(0,0));

        // draw every sprite in the map
//        for (Tile tile : game.map.tiles.values()) {
        for (Vector2 currPos = new Vector2(startPos.x, startPos.y); currPos.y > endPos.y;) {
            tile = game.map.tiles.get(currPos);
            currPos.x += 16;
            if (currPos.x > endPos.x) {
                currPos.x = startPos.x;
                currPos.y -= 16;
            }
            if (tile == null) {
                continue;
            }
//            if (!game.cam.frustum.pointInFrustum(tile.position.x, tile.position.y, game.cam.position.z) &&
//                    !game.cam.frustum.pointInFrustum(tile.position.x+tile.sprite.getWidth(), tile.position.y+tile.sprite.getHeight(), game.cam.position.z) &&
//                    !game.cam.frustum.pointInFrustum(tile.position.x+tile.sprite.getWidth(), tile.position.y, game.cam.position.z) &&
//                    !game.cam.frustum.pointInFrustum(tile.position.x, tile.position.y+tile.sprite.getHeight(), game.cam.position.z)) {
//               continue;
//            }
            // Texture text = new
            // Texture(Gdx.files.internal("tiles/water1_2.png"));
            if (tile.attrs.get("water") == true) {
                // tile.sprite.setPosition(tile.sprite.getX()+position.x,
                // tile.sprite.getY()+position.y);
                // tile.overSprite.draw(game.batch);
                tile.sprite.setRegionX((int) this.position.x);
                tile.sprite.setRegionWidth((int) tile.sprite.getWidth());
            }

            //animate campfires
            if (tile.nameUpper.equals("campfire1")) {
                if (this.campfireTimer == 0) {
                    Sprite newSprite = new Sprite(this.campfireSprites[0]);
                    newSprite.setPosition(tile.overSprite.getX(), tile.overSprite.getY());
                    tile.overSprite = newSprite;
                }
                else if (this.campfireTimer == 40) {
                    Sprite newSprite = new Sprite(this.campfireSprites[1]);
                    newSprite.setPosition(tile.overSprite.getX(), tile.overSprite.getY());
                    tile.overSprite = newSprite;
                }

                if (this.campfireTimer % 20 < 10 && game.mapBatch.getColor().r < .5f) {
                    Color tempColor = game.mapBatch.getColor();
                    game.mapBatch.setColor(new Color(1f, 1f, 1f, 1f));
                    // no idea how/why this works
                    int temp1 = game.mapBatch.getBlendSrcFunc();
                    int temp2 = game.mapBatch.getBlendDstFunc();
                    game.mapBatch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_SRC_ALPHA);
                    this.campfireSprites[2].setPosition(tile.overSprite.getX()+(tile.overSprite.getWidth()/2f)-(this.campfireSprites[2].getWidth()/2f), tile.overSprite.getY()+(tile.overSprite.getHeight()/2f)-(this.campfireSprites[2].getHeight()/2f));
                    // looked better with 'double-exposure'
                    game.mapBatch.draw(this.campfireSprites[2], this.campfireSprites[2].getX(), this.campfireSprites[2].getY());
                    game.mapBatch.draw(this.campfireSprites[2], this.campfireSprites[2].getX(), this.campfireSprites[2].getY());
                    game.mapBatch.draw(this.campfireSprites[2], this.campfireSprites[2].getX(), this.campfireSprites[2].getY());
                    game.mapBatch.setBlendFunction(temp1, temp2);
                    tile.overSprite.draw(game.mapBatch);
                    game.mapBatch.setColor(tempColor);
                }
                else if (this.campfireTimer % 20 < 20 && game.mapBatch.getColor().r < .5f) {
                    Color tempColor = game.mapBatch.getColor();
                    game.mapBatch.setColor(new Color(1f, 1f, 1f, 1f));
                    // no idea how/why this works
                    int temp1 = game.mapBatch.getBlendSrcFunc();
                    int temp2 = game.mapBatch.getBlendDstFunc();
                    game.mapBatch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_SRC_ALPHA);
                    this.campfireSprites[3].setPosition(tile.overSprite.getX()+(tile.overSprite.getWidth()/2f)-(this.campfireSprites[3].getWidth()/2f), tile.overSprite.getY()+(tile.overSprite.getHeight()/2f)-(this.campfireSprites[3].getHeight()/2f));
                    // looked better with 'double-exposure'
                    game.mapBatch.draw(this.campfireSprites[3], this.campfireSprites[3].getX(), this.campfireSprites[3].getY());
                    game.mapBatch.draw(this.campfireSprites[3], this.campfireSprites[3].getX(), this.campfireSprites[3].getY());
                    game.mapBatch.draw(this.campfireSprites[3], this.campfireSprites[3].getX(), this.campfireSprites[3].getY());
                    game.mapBatch.setBlendFunction(temp1, temp2);
                    tile.overSprite.draw(game.mapBatch);
                    game.mapBatch.setColor(tempColor);
                }
                
            }
        }
        if (this.campfireTimer < 79) {
            this.campfireTimer++;
        }
        else {
            this.campfireTimer = 0;
        }
        // repeat sprite/pos for current object for 'frames[0]' number of
        // frames.
        if (this.repeats.get(0) > 0) {
            this.repeats.set(0, this.repeats.get(0) - 1);
        } else {
            // since position is relative, only update once each time period
            // this.position = this.position.add(positions.get(0));
            positions.remove(0);
            repeats.remove(0);
        }
    }

    public MoveWater(Game game) {
        this.positions = new ArrayList<Vector2>();
        resetVars();
        Texture text = TextureCache.get(Gdx.files.internal("tiles/campfire1.png"));
        this.campfireSprites = new Sprite[4];
        this.campfireSprites[0] = new Sprite(text, 0,  0, 16, 20);
        this.campfireSprites[1] = new Sprite(text, 16, 0, 16, 20);
        text = TextureCache.get(Gdx.files.internal("fire_mask1.png"));
        this.campfireSprites[2] = new Sprite(text, 0,  0, 160, 144);
        text = TextureCache.get(Gdx.files.internal("fire_mask2.png"));
        this.campfireSprites[3] = new Sprite(text, 0, 0, 160, 144);
        
//        this.pointLight = new PointLight(game.rayHandler, 20, new Color(.3f,.2f,.1f,1), 2, -0, 0);
//        this.pointLight = new PointLight(game.rayHandler, 8, new Color(1f, .9f, .7f, 1), 5f, -0, 0);
//        this.pointLight = new PointLight(game.rayHandler, 16, new Color(.8f, .7f, .6f, 1), 5f, -0, 0);
//        this.pointLight.setPosition(0f, 0f);
        
    }

    public void resetVars() {
        this.position = new Vector2(0, 0);
        this.positions.add(new Vector2(0, 0));
        this.positions.add(new Vector2(1, 0));
        this.positions.add(new Vector2(2, 0));
        this.positions.add(new Vector2(3, 0));
        this.positions.add(new Vector2(2, 0));
        this.positions.add(new Vector2(1, 0));

        this.repeats = new ArrayList<Integer>();
        this.repeats.add(20 - 1); // 20 frames each
        this.repeats.add(20 - 1); // 20 frames each
        this.repeats.add(20 - 1); // 20 frames each
        this.repeats.add(20 - 1); // 20 frames each
        this.repeats.add(20 - 1); // 20 frames each
        this.repeats.add(20 - 1); // 20 frames each
    }

}

// action is separate because grass in on different layer
class DrawMapGrass extends Action {

    public int layer = 120;

    public int getLayer() {
        return this.layer;
    }
    
    Sprite blankSprite;
    Pixmap pixels;
    Texture texture;
    Vector3 startPos;
    Vector3 endPos;
    Vector3 worldCoordsTL;
    Vector3 worldCoordsBR;
    Tile tile;

    @Override
    public void step(Game game) {

        worldCoordsTL = game.cam.unproject(new Vector3(-128, 0, 0f));
        worldCoordsBR = game.cam.unproject(new Vector3(game.currScreen.x, game.currScreen.y+128, 0f));
        worldCoordsTL.x = (int)worldCoordsTL.x - (int)worldCoordsTL.x % 16;
        worldCoordsTL.y = (int)worldCoordsTL.y - (int)worldCoordsTL.y % 16;
        worldCoordsBR.x = (int)worldCoordsBR.x - (int)worldCoordsBR.x % 16;
        worldCoordsBR.y = (int)worldCoordsBR.y - (int)worldCoordsBR.y % 16;
        this.startPos = worldCoordsTL; //new Vector3(worldCoordsTL.x, worldCoordsTL.y, 0f);
        this.endPos = worldCoordsBR; // new Vector3(worldCoordsBR.x, worldCoordsBR.y, 0f);
        
        // TODO - bug i can't fix where grass sprites lag when moving

        // game.cam.update(); //doesn't work
        // game.batch.setProjectionMatrix(game.cam.combined);

        // draw every sprite in the map
//        for (Tile tile : game.map.tiles.values()) {
        for (Vector2 currPos = new Vector2(startPos.x, startPos.y); currPos.y > endPos.y;) {
            tile = game.map.tiles.get(currPos);
            currPos.x += 16;
            if (currPos.x > endPos.x) {
                currPos.x = startPos.x;
                currPos.y -= 16;
            }
            if (tile == null) {
                continue;
            }
            // Don't draw sprites if not in camera view
            // note - when zoomed out, game will lag
            if (!game.cam.frustum.pointInFrustum(tile.position.x, tile.position.y, game.cam.position.z) &&
                    !game.cam.frustum.pointInFrustum(tile.position.x+tile.sprite.getWidth(), tile.position.y+tile.sprite.getHeight(), game.cam.position.z) &&
                    !game.cam.frustum.pointInFrustum(tile.position.x+tile.sprite.getWidth(), tile.position.y, game.cam.position.z) &&
                    !game.cam.frustum.pointInFrustum(tile.position.x, tile.position.y+tile.sprite.getHeight(), game.cam.position.z)) {
               continue;
            }
            if (tile.attrs.get("grass")) {
                // tile.sprite.draw(game.batch); doesn't allow coloring
                game.mapBatch.draw(tile.overSprite, tile.sprite.getX(), tile.sprite.getY());
            }

            // TODO: shouldn't be doing this here, need to refactor map draw action
            if (tile.nameUpper.contains("sleeping_bag")) {
                tile.overSprite.draw(game.mapBatch);
            }
        }

    }

    public DrawMapGrass(Game game) {

    }

}


/*
 * Draw tops of some trees over the player.
 * 
 * TODO: this isn't working for some reason.
 */
class DrawMapTrees extends Action {
    public int layer = 110;
    public int getLayer() {
        return this.layer;
    }
    Vector3 startPos;
    Vector3 endPos;
    Vector3 worldCoordsTL;
    Vector3 worldCoordsBR;
    Tile tile;
    
    @Override
    public void step(Game game) {
        if (game.map.tiles != game.map.overworldTiles) {
            return;
        }

        worldCoordsTL = game.cam.unproject(new Vector3(-256, 0, 0f));
        worldCoordsBR = game.cam.unproject(new Vector3(game.currScreen.x, game.currScreen.y+128, 0f));
        worldCoordsTL.x = (int)worldCoordsTL.x - (int)worldCoordsTL.x % 16;
        worldCoordsTL.y = (int)worldCoordsTL.y - (int)worldCoordsTL.y % 16;
        worldCoordsBR.x = (int)worldCoordsBR.x - (int)worldCoordsBR.x % 16;
        worldCoordsBR.y = (int)worldCoordsBR.y - (int)worldCoordsBR.y % 16;
        this.startPos = worldCoordsTL;
        this.endPos = worldCoordsBR;
        for (Vector2 currPos = new Vector2(startPos.x, startPos.y); currPos.y > endPos.y;) {
            tile = game.map.tiles.get(currPos);
            currPos.x += 16;
            if (currPos.x > endPos.x) {
                currPos.x = startPos.x;
                currPos.y -= 16;
            }
            if (tile == null) {
                continue;
            }
            if (!game.cam.frustum.pointInFrustum(tile.position.x, tile.position.y, game.cam.position.z) &&
                !game.cam.frustum.pointInFrustum(tile.position.x+tile.sprite.getWidth(), tile.position.y+tile.sprite.getHeight(), game.cam.position.z) &&
                !game.cam.frustum.pointInFrustum(tile.position.x+tile.sprite.getWidth(), tile.position.y, game.cam.position.z) &&
                !game.cam.frustum.pointInFrustum(tile.position.x, tile.position.y+tile.sprite.getHeight(), game.cam.position.z)) {
                continue;
            }
            // If this tree is supposed to be behind the player, don't re-draw it. 
            if (tile.position.y > game.player.position.y) {
                continue;
            }
            // TODO: only do for subset of trees?
            if ((tile.name.contains("tree") || tile.nameUpper.contains("tree")) && tile.overSprite != null) {
                game.mapBatch.draw(tile.overSprite, tile.sprite.getX(), tile.sprite.getY());
            }
        }
    }
    public DrawMapTrees(Game game) {

    }
}

class detectWildEncounter extends Action {

    public int layer = 0;

    public int getLayer() {
        return this.layer;
    }

    Tile currTile;

    Random rand;

    @Override
    public void step(Game game) {

        // when the player moves to a new tile, chance of wild encounter
        Tile newTile = game.map.tiles.get(new Vector2(game.cam.position.x,
                game.cam.position.y));

        // beginning of game
        if (currTile == null) {
            currTile = newTile;
        }
        // if moved to a new tile
        if (newTile != null && newTile != this.currTile) {
            //
            this.currTile = newTile;

            // if stepped into grass,
            if (newTile.attrs.get("grass") == true) {
                // chance wild encounter
                int randomNum = this.rand.nextInt(100) + 1; // rate determine by
                                                            // player? //1 - 100
                if (randomNum <= 75) { // encounterRate
                    // disable player movement (how?) (flag in player, move
                    // actions self-destruct when seen?)

                    // select new pokemon to encounter, put it in battle struct
                    int index = rand.nextInt(game.map.currRoute.pokemon.size());
                    // game.battle.currPokemon =
                    // game.map.currRoute.pokemon.get(index);

                    // start battle anim
                    // PublicFunctions.insertToAS(game, new battleIntro(game));

                    System.out.println("Wild encounter.");
                }
            }

        }

    }

    public detectWildEncounter(Game game) {

        // get player's current tile
        // this.currTile = game.map.tiles.get(game.player.position);

        this.rand = new Random();
    }

}

// action to generate mountain

class genMountain_1 extends Action {

    public int layer = 120;

    public int getLayer() {
        return this.layer;
    }

    ArrayList<Tile> tilesToAdd = new ArrayList<Tile>();

    Vector2 seed;
    Vector2 topLeft, bottomRight;

    Random rand;

    int height = 5;

    @Override
    public void step(Game game) {

        if (tilesToAdd.isEmpty()) {

            // if done with mountain, return and remove
            if (this.height <= 0) {
                game.actionStack.remove(this);
                return;
            }

            // increment corners

            // add tiles on each increment

            // int randomNum = rand.nextInt((max - min) + 1) + min;
            int randomNum = this.rand.nextInt(5);
            // 0=left, 1=bottom, 2=right, 3=top, 4=ledge

            // left
            if (randomNum == 0) {
                topLeft.x -= 16;
                for (int i = (int) this.topLeft.y; i >= (int) this.bottomRight.y; i -= 16) {
                    this.tilesToAdd.add(new Tile("ground2", new Vector2(
                            this.topLeft.x, i)));
                }
            }
            // bottom
            else if (randomNum == 1) {
                bottomRight.y -= 16;
                for (int i = (int) this.topLeft.x; i <= (int) this.bottomRight.x; i += 16) {
                    this.tilesToAdd.add(new Tile("ground2", new Vector2(i,
                            bottomRight.y)));
                }
            }
            // right
            else if (randomNum == 2) {
                bottomRight.x += 16;
                for (int i = (int) this.bottomRight.y; i <= (int) this.topLeft.y; i += 16) {
                    this.tilesToAdd.add(new Tile("ground2", new Vector2(
                            this.bottomRight.x, i)));
                }
            }
            // top
            else if (randomNum == 3) {
                topLeft.y += 16;
                for (int i = (int) this.bottomRight.x; i >= (int) this.topLeft.x; i -= 16) {
                    this.tilesToAdd.add(new Tile("ground2", new Vector2(i,
                            topLeft.y)));
                }
            } else if (randomNum == 4) {
                topLeft.x -= 16;
                topLeft.y += 16;
                bottomRight.x += 16;
                bottomRight.y -= 16;

                // corners
                this.tilesToAdd.add(new Tile("ledge1_corner_bl", new Vector2(
                        this.topLeft.x, this.bottomRight.y)));
                this.tilesToAdd.add(new Tile("ledge1_corner_br", new Vector2(
                        this.bottomRight.x, this.bottomRight.y)));
                this.tilesToAdd.add(new Tile("ledge2_corner_tl", new Vector2(
                        this.topLeft.x, this.topLeft.y)));
                this.tilesToAdd.add(new Tile("ledge2_corner_tr", new Vector2(
                        this.bottomRight.x, this.topLeft.y)));

                for (int i = (int) this.topLeft.y - 16; i > (int) this.bottomRight.y; i -= 16) {
                    this.tilesToAdd.add(new Tile("ledge1_left", new Vector2(
                            this.topLeft.x, i)));
                }
                for (int i = (int) this.topLeft.x + 16; i < (int) this.bottomRight.x; i += 16) {
                    this.tilesToAdd.add(new Tile("ledge1_down", new Vector2(i,
                            bottomRight.y)));
                }
                for (int i = (int) this.bottomRight.y + 16; i < (int) this.topLeft.y; i += 16) {
                    this.tilesToAdd.add(new Tile("ledge1_right", new Vector2(
                            this.bottomRight.x, i)));
                }
                for (int i = (int) this.bottomRight.x - 16; i > (int) this.topLeft.x; i -= 16) {
                    this.tilesToAdd.add(new Tile("ground2_top", new Vector2(i,
                            topLeft.y)));
                }

                //
                this.height -= 1;
            }
            // game.actionStack.remove(this);

            // System.out.println("done");
        }

        Tile currTile = this.tilesToAdd.get(0);
        game.map.tiles.put(currTile.position.cpy(), currTile);
        this.tilesToAdd.remove(0);

        // System.out.println("pos: "+String.valueOf(currTile.position));

    }

    public genMountain_1(Game game, Vector2 seed, int variance, int steepness) {

        this.seed = seed;
        this.topLeft = new Vector2(seed.x, seed.y);
        this.bottomRight = new Vector2(seed.x + 16, seed.y - 16);

        // fill in initial tilesToAdd
        this.tilesToAdd.add(new Tile("ground2", new Vector2(this.topLeft)));
        this.tilesToAdd.add(new Tile("ground2", new Vector2(this.topLeft.x,
                this.bottomRight.y)));
        this.tilesToAdd.add(new Tile("ground2", new Vector2(this.bottomRight)));
        this.tilesToAdd.add(new Tile("ground2", new Vector2(this.bottomRight.x,
                this.topLeft.y)));

        this.rand = new Random();

    }

}

// TODO: shader method
class EnterBuilding extends Action {
  
  Sprite sprite;
  String action;
  Action nextAction;

  public int layer = 114;  // TODO: check
  public int getLayer(){return this.layer;}

  public String getCamera() {return "gui";};

  int timer = 0;
  
  @Override
  public void step(Game game) {
      
      if (this.timer < 2) {
          if (this.timer == 0 && this.action.equals("enter") || this.action.equals("exit")) {
              PublicFunctions.insertToAS(game, new PlaySound(this.action+"1", new DoneAction()));
          }
      }
      else if (this.timer < 4) {
          this.sprite.draw(game.uiBatch, .25f);
      }
      else if (this.timer < 6) {
          this.sprite.draw(game.uiBatch, .50f);
      }
      else if (this.timer < 12) {
          if (this.timer == 6) {
              if (this.action.equals("enter")) {
                  game.map.tiles = game.map.interiorTiles.get(game.map.interiorTilesIndex);
              }
              else if (this.action.equals("exit")){
                  game.map.tiles = game.map.overworldTiles;
              }
          }
          this.sprite.draw(game.uiBatch, 1f);
      }
      else if (this.timer < 14) {
          this.sprite.draw(game.uiBatch, .75f);
      }
      else if (this.timer < 16) {
          this.sprite.draw(game.uiBatch, .50f);
      }
      else if (this.timer < 18) {
          this.sprite.draw(game.uiBatch, .25f);
      }
      else {
          game.actionStack.remove(this);
          PublicFunctions.insertToAS(game, this.nextAction);
      }
          
      this.timer++;

  }
  
  public EnterBuilding(Game game, Action nextAction) {
      this(game, "enter", nextAction);
  }
  
  public EnterBuilding(Game game, String action, Action nextAction) {
      this.nextAction = nextAction;
      this.action = action;
      //fade out from white anim
      Texture text1 = TextureCache.get(Gdx.files.internal("battle/intro_frame6.png"));
      this.sprite = new Sprite(text1);
      this.sprite.setPosition(0,0);
  }
}




/*
 * Unused code
 * 
 * //will potentially highlight batch (couldn't get to work)
 * game.batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE);
 * game.batch.setColor(0.4f, 0.4f, 0.4f, 1f);
 * game.batch.draw(tile.sprite.getTexture(), tile.sprite.getX(),
 * tile.sprite.getY()); //only works with batch.draw game.batch.setColor(1f, 1f,
 * 1f, 1f); game.batch.setBlendFunction(GL20.GL_SRC_ALPHA,
 * GL20.GL_ONE_MINUS_SRC_ALPHA); //does this cause problems?
 */

