package com.pkmngen.game;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.Pokemon.Burrowed;
import com.pkmngen.game.Pokemon.Standing;

public class GenForest2 extends Action {
    public static void FlipFormat(String[][] format){
        int i=0;
        for (String[] sub : format) {
            int j=0;
            for (String tileName : sub) {
                if (i >= 5-j) {
                    break;
                }
                String temp = format[i][j];
                format[i][j] = format[5-j][5-i];
                format[5-j][5-i] = temp;

                j++;
            }
            i++;
        }
    }
    public static ArrayList<Tile> GenTiles(String[][] format, int offSetX, int offSetY, Random rand) {
        return GenForest2.GenTiles(format, offSetX, offSetY, rand, false);
    }

    // given a formatted input, ouput the equivalent list of tiles with appropriate x and y positions set
    public static ArrayList<Tile> GenTiles(String[][] format, int offSetX, int offSetY, Random rand, boolean color) {
        ArrayList<Tile> tiles = new ArrayList<Tile>();

        // need to start in top-left corner, b/c that's how inline formatting does it (can't be helped)

//        System.out.println("offSetX: "+String.valueOf(offSetX));
//        System.out.println("offSetY: "+String.valueOf(offSetY));
        String[] emptyOrSolid = {"solid", ""};

        int i=5;
        for (String[] sub : format) {
            int j=0;
            for (String tileName : sub) {
                // if tile can be 'either', pick 'solid' or ''
                if (tileName == "either") {
                    int randomNum = rand.nextInt(emptyOrSolid.length);
                    tileName = emptyOrSolid[randomNum];
                }
                if (tileName != "") {
                    // System.out.println("tileName: "+String.valueOf(tileName)); // debug
                    tiles.add(new Tile(tileName, new Vector2(0+j*16+offSetX, 0+i*16+offSetY), color));
                }
                j++;
            }
            i--;
        }

        return tiles;
    }

    public static ArrayList<Tile> getTileSquare(MazeNode node, Vector2 startLoc, Random rand) {
        return GenForest2.getTileSquare(node, startLoc, rand, false);
    }

    public static ArrayList<Tile> getTileSquare(MazeNode node, Vector2 startLoc, Random rand, boolean color) {
        // note - using 'solid' and 'nonsolid'(empty string) tile method now
         //"solid" - solid denotes that the tile can be used later for solid object. for example, water or trees
         //"" - empty string denotest that tile can be used for nonsolid objects. for example, grass
         //"either" - can be used for solid or nonsolid. this will return 'solid' or '' tile at random

        // README - when creating these, DONT block the middle two tiles of walls that should be open.
         // ex: bottom open
//        1 1 X X 0 0
//        1 1 0 0 0 0
//        1 1 0 0 0 0
//        1 1 0 0 0 0
//        1 1 0 0 0 0
//        1 1 X X 0 0
        // don't block the X's - if two of the below ever break this rule, and are put adjacent, system doesn't work

        // list1.equals(list2)
        // Arrays.equals(a, b)

        int offSetx = node.size*node.x +(int)startLoc.x;
        int offSetY = node.size*node.y +(int)startLoc.y;

        ArrayList<ArrayList<Tile>> squares = new ArrayList<ArrayList<Tile>>();
        ArrayList<Tile> square = new ArrayList<Tile>();

        String[][] format = new String[][]{};

        // random ledge for use later

        // TODO - unused
        String[] ledge = {"ledge_grass_down","ledge_grass_down","ledge_grass_down",    "ledge_grass_down",    "ledge_grass_down",    "ledge_grass_down"};
        ledge[rand.nextInt(ledge.length)] = "ledge_grass_ramp";

        // bl corner
        if (Arrays.equals(node.isOpen, new boolean[]{false, false})) {
            // 1

            format = new String[][]{
                                   new String[]{"solid",     "solid",        "",             "",             "either",         "either"},

                                   new String[]{"solid",     "solid",        "solid",         "",             "",             "either"},

                                   new String[]{"solid",     "solid",        "solid",         "solid",        "",             ""},

                                   new String[]{"solid",     "solid",        "solid",         "solid",         "solid",         ""},

                                   new String[]{"solid",     "solid",        "solid",         "solid",        "solid",         "solid",},

                                   new String[]{"solid",     "solid",        "solid",         "solid",        "solid",         "solid",},
                                 };
            square = GenTiles(format, offSetx, offSetY, rand, color);

            squares.add(square);

            // 2
            format = new String[][]{
                       new String[]{"solid",     "solid",        "",             "",             "solid",         "solid"},

                       new String[]{"solid",     "solid",        "",             "",             "solid",         "solid"},

                       new String[]{"solid",     "solid",        "either",         "",                "",             ""},

                       new String[]{"solid",     "solid",        "either",         "either",         "",             ""},

                       new String[]{"solid",     "solid",        "solid",         "solid",        "solid",         "solid",},

                       new String[]{"solid",     "solid",        "solid",         "solid",        "solid",         "solid",},
             };

            square = GenTiles(format, offSetx, offSetY, rand, color);

            squares.add(square);

        }
        // maze node left or down is open (but not both)
        else if ( node.leftOpen ^ node.downOpen ) {
            // 1
            format = new String[][]{
                       new String[]{"either",                 "either",                         "",                        "",                         "either",                 "either"},

                       new String[]{"",                         "",                                "",                     "",                             "",                     ""},

                       new String[]{"",                         "",                                "",                     "",                             "",                     ""},

                       new String[]{"",                         "either",                        "",                     "either",                         "",                     ""},

                       new String[]{"solid",                 "solid",                        "solid",                "solid",                        "solid",                 "solid"},

                       new String[]{"solid",                 "solid",                        "solid",                 "solid",                        "solid",                 "solid"},
                     };
            if (node.downOpen == true) {
                FlipFormat(format);
                // debug - FlipFormat mirrors about x = y line
//                for (String[] sub : format) {
//                    for (String tileName : sub) {
//                        System.out.print(""+String.valueOf(tileName)+"\t\t\t\t");
//                    }
//                    System.out.print("\n");
//                }
                // randomly insert random ledge
                if (rand.nextInt(3) == 1) {
                    int randRowIndex = rand.nextInt(format.length-2)+1;
                    String[] randRow = format[randRowIndex];
                    boolean onGround = false;
                    ArrayList<Integer> ledgesGoHere = new ArrayList<Integer>();
                    for (int i=0; i < randRow.length; i++) {
                        if (randRow[i] != "solid") {
                            onGround = true;
                            ledgesGoHere.add(i);
                        }
                        else if (onGround == true) {
                            break;
                        }
                    }
                    int randIndexForRamp = ledgesGoHere.get(rand.nextInt(ledgesGoHere.size()));
                    for (Integer index : ledgesGoHere) {
                        randRow[index] = "ledge_grass_down";
                        if (index == randIndexForRamp) {
                            randRow[index] = "ledge_grass_ramp";
                            format[randRowIndex-1][index] = ""; // prevent solid object from blocking ramp
                            format[randRowIndex+1][index] = ""; // same
                        }
                    }
                }
            }
            square = GenTiles(format, offSetx, offSetY, rand, color);
            squares.add(square);

            // 2
            format = new String[][]{
                       new String[]{"",                         "",                                "",                     "",                             "solid",                 "solid"},

                       new String[]{"",                         "",                                "",                     "solid",                         "solid",                 "solid"},

                       new String[]{"",                         "",                                "",                     "",                             "",                     ""},

                       new String[]{"",                         "",                                "",                     "",                             "",                     ""},

                       new String[]{"solid",                 "solid",                        "solid",                 "solid",                        "solid",                 "solid",},

                       new String[]{"solid",                 "solid",                        "solid",                 "solid",                        "solid",                 "solid",},
                     };
            if (node.downOpen == true) {
                FlipFormat(format);
                // insert random ledge
                if (rand.nextInt(3) == 1) {
                    String[] randRow = format[rand.nextInt(format.length-2)+1];
                    boolean onGround = false;
                    ArrayList<Integer> ledgesGoHere = new ArrayList<Integer>();
                    for (int i=0; i < randRow.length; i++) {
                        if (randRow[i] != "solid") {
                            onGround = true;
                            ledgesGoHere.add(i);
                        }
                        else if (onGround == true) {
                            break;
                        }
                    }
                    int randIndexForRamp = ledgesGoHere.get(rand.nextInt(ledgesGoHere.size()));
                    for (Integer index : ledgesGoHere) {
                        randRow[index] = "ledge_grass_down";
                        if (index == randIndexForRamp) {
                            randRow[index] = "ledge_grass_ramp";
                        }
                    }
                }
            }
            square = GenTiles(format, offSetx, offSetY, rand, color);
            squares.add(square);

        }
        // maze node bottom and left is open
        else {
            // 1
//            square = new ArrayList<Tile>(); // no tiles
//            squares.add(square);

            // 2
            format = new String[][]{
                       new String[]{"",                         "",                                "",                         "",                             "solid",             "solid"},

                       new String[]{"",                         "",                                "",                         "either",                        "solid",             "solid"},

                       new String[]{"",                         "",                                "",                         "",                             "either",             ""},

                       new String[]{"",                         "either",                        "",                         "",                             "",                 ""},

                       new String[]{"solid",                 "solid",                        "either",                    "",                                "",                 "",},

                       new String[]{"solid",                 "solid",                        "",                         "",                                "",                 "",},
                     };
            square = GenTiles(format, offSetx, offSetY, rand, color);
            squares.add(square);

            // 3 - all qmark.
            format = new String[][]{
                       new String[]{"",     "",            "",                         "",                             "",             ""},

                       new String[]{"",     "",            "",                         "",                             "",             ""},

                       new String[]{"",     "",            "",                         "",                             "",             ""},

                       new String[]{"",     "",            "",                         "",                             "",             ""},

                       new String[]{"solid",     "solid",            "",                         "",                                "",             "",},

                       new String[]{"solid",     "solid",            "",                         "",                                "",             "",},
                     };
            square = GenTiles(format, offSetx, offSetY, rand, color);
            squares.add(square);
        }

        // choose randomly from ArrayList of squares
        int randomNum = rand.nextInt(squares.size());
        // randomNum = 0; // debug
        return squares.get(randomNum);
    }

    // TODO - this still returns forest biome tiles
    public static ArrayList<Tile> getTileSquarePlatform1(MazeNode node, Vector2 startLoc, Random rand) {
        // README - when creating these, DONT block the middle two tiles of walls that should be open.
         // ex: bottom open
//        1 1 X X 0 0
//        1 1 0 0 0 0
//        1 1 0 0 0 0
//        1 1 0 0 0 0
//        1 1 0 0 0 0
//        1 1 X X 0 0
        // don't block the X's - if two of the below ever break this rule, and are put adjacent, system doesn't work

        // list1.equals(list2)
        // Arrays.equals(a, b)

        int offSetx = node.size*node.x +(int)startLoc.x;
        int offSetY = node.size*node.y +(int)startLoc.y;

        ArrayList<ArrayList<Tile>> squares = new ArrayList<ArrayList<Tile>>();
        ArrayList<Tile> square = new ArrayList<Tile>();

        String[][] format = new String[][]{};

//        String[] rn = {"ground1", "qmark", "grass1"}; // random non-solid objects
         // bug - qmark can potentially be solid

        // choose from temp because I want more uniformity
        String[] temp = {"bush1", "tree_small1"};
        String[] rs = {temp[rand.nextInt(temp.length)], "ground1", "ground1"}; // random solid or non-solid objects

        // bl corner
        if (!node.leftOpen && !node.downOpen) {
            if (node.rampLoc == "down") {
                // 1
                format = new String[][]{
                           new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",            "ledge_grass_left",         "",                             "",                                 "ledge_grass_right"},

                           new String[]{"tree_large1",             "tree_large1_noSprite",            "ledge_grass_left",         "",                             "",                                 "ledge_grass_right"},

                           new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",            "ledge_grass_left",         "ledge_grass_inside_tl",        "ledge_grass_ramp",                 "ledge1_corner_br"},

                           new String[]{"tree_large1",             "tree_large1_noSprite",            "ledge1_corner_bl",         "ledge1_corner_br",             "",                                 ""},

                           new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",            "tree_large1_noSprite",     "tree_large1_noSprite",            "tree_large1_noSprite",             "tree_large1_noSprite",},

                           new String[]{"tree_large1",             "tree_large1_noSprite",            "tree_large1",                 "tree_large1_noSprite",            "tree_large1",                         "tree_large1_noSprite",},
                         };
                square = GenTiles(format, offSetx, offSetY, rand);

                squares.add(square);
            }
            else {
                // 1
                format = new String[][]{
                           new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",            "",                         "ledge2_corner_tl",             "ledge_grass_safari_up",             "ledge_grass_safari_up"},

                           new String[]{"tree_large1",             "tree_large1_noSprite",            "",                         "ledge_grass_left",             "",                                 ""},

                           new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",            "",                         "ledge1_corner_bl",                "ledge_grass_ramp",                 "ledge_grass_inside_tr"},

                           new String[]{"tree_large1",             "tree_large1_noSprite",            "",                         "",                             "",                                 "ledge1_corner_bl"},

                           new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",            "tree_large1_noSprite",     "tree_large1_noSprite",            "tree_large1_noSprite",             "tree_large1_noSprite",},

                           new String[]{"tree_large1",             "tree_large1_noSprite",            "tree_large1",                 "tree_large1_noSprite",            "tree_large1",                         "tree_large1_noSprite",},
                         };
                square = GenTiles(format, offSetx, offSetY, rand);

                squares.add(square);
            }
            // TODO - handle case of both false (small platform in middle i guess

        }
        // maze node left or down is open (but not both)
        else if ( node.leftOpen == true && node.downOpen == false ) {
            if (node.rampLoc == "down") {
                // 1
                format = new String[][]{
                           new String[]{rs[rand.nextInt(rs.length)], rs[rand.nextInt(rs.length)], rs[rand.nextInt(rs.length)], rs[rand.nextInt(rs.length)], rs[rand.nextInt(rs.length)], rs[rand.nextInt(rs.length)]},

                           new String[]{"ledge_grass_left",                         "",                                "",                     "",             "",                     "ledge_grass_right"},

                           new String[]{"ledge1_corner_bl",         "ledge_grass_down",                "ledge_grass_down",     "ledge_grass_ramp",             "ledge_grass_down",     "ledge1_corner_br"},

                           new String[]{"",                         "",                                "",                     "",                             "",                     ""},

                           new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",            "tree_large1_noSprite", "tree_large1_noSprite",            "tree_large1_noSprite", "tree_large1_noSprite",},

                           new String[]{"tree_large1",             "tree_large1_noSprite",            "tree_large1",             "tree_large1_noSprite",            "tree_large1",             "tree_large1_noSprite",},
                         };
                square = GenTiles(format, offSetx, offSetY, rand);
                squares.add(square);
            }
            else if (node.rampLoc == "left") {
                // 1
                format = new String[][]{
                           new String[]{"",                         "",                                "",                     "ledge2_corner_tl",             "ledge_grass_safari_up","ledge_grass_safari_up"},

                           new String[]{"",                         "",                                "",                     "ledge_grass_left",             "",                     ""},

                           new String[]{"",                         "",                                "",                     "ledge1_corner_bl",             "ledge_grass_ramp",     "ledge_grass_inside_tr"},

                           new String[]{"",                         "",                                "",                     "",                             "",                     "ledge1_corner_bl"},

                           new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",            "tree_large1_noSprite", "tree_large1_noSprite",            "tree_large1_noSprite", "tree_large1_noSprite",},

                           new String[]{"tree_large1",             "tree_large1_noSprite",            "tree_large1",             "tree_large1_noSprite",            "tree_large1",             "tree_large1_noSprite",},
                         };
                square = GenTiles(format, offSetx, offSetY, rand);
                squares.add(square);
            }
            else {
                // 1
                format = new String[][]{
                           new String[]{"ledge_grass_safari_up", "ledge_grass_safari_up",        "ledge_grass_safari_up","ledge2_corner_tr",             "",                     ""},

                           new String[]{"",                         "",                                "",                     "ledge_grass_right",             "",                     ""},

                           new String[]{"",                         "ledge_grass_inside_tl",        "ledge_grass_ramp",     "ledge1_corner_br",             "",                     ""},

                           new String[]{"ledge_grass_down",         "ledge1_corner_br",                "",                     "",                             "",                     ""},

                           new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",            "tree_large1_noSprite", "tree_large1_noSprite",            "tree_large1_noSprite", "tree_large1_noSprite",},

                           new String[]{"tree_large1",             "tree_large1_noSprite",            "tree_large1",             "tree_large1_noSprite",            "tree_large1",             "tree_large1_noSprite",},
                         };
                square = GenTiles(format, offSetx, offSetY, rand);
                squares.add(square);

            }
        }
        // maze node left or down is open (but not both)
        else if ( node.leftOpen == false && node.downOpen == true ) {
            if (node.rampLoc == "down") {
                // 1
                format = new String[][]{
                           new String[]{"tree_large1_noSprite", "tree_large1_noSprite",     "ledge_grass_left",     "",                             "",                     "ledge_grass_right"},

                           new String[]{"tree_large1",             "tree_large1_noSprite",    "ledge_grass_left",     "",                             "",                     "ledge_grass_right"},

                           new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",    "ledge1_corner_bl",     "ledge_grass_ramp",             "ledge_grass_down",     "ledge1_corner_br"},

                           new String[]{"tree_large1",             "tree_large1_noSprite",    "",                     "",                             "",                     ""},

                           new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",    "",                     "",                                "",                     ""},

                           new String[]{"tree_large1",             "tree_large1_noSprite",    "",                     "",                                "",                     ""},
                         };
                square = GenTiles(format, offSetx, offSetY, rand);
                squares.add(square);
            }
            else if (node.rampLoc == "left") {
                // 1
                format = new String[][]{
                           new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",                                "",                     "ledge2_corner_tl",             "ledge_grass_safari_up","ledge_grass_safari_up"},

                           new String[]{"tree_large1",             "tree_large1_noSprite",                                "",                     "ledge_grass_left",             "",                     ""},

                           new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",                                "",                     "ledge1_corner_bl",             "ledge_grass_ramp",     "ledge_grass_inside_tr"},

                           new String[]{"tree_large1",             "tree_large1_noSprite",                                "",                     "",                             "",                     "ledge1_corner_bl"},

                           new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",            "", "",            "", ""},

                           new String[]{"tree_large1",             "tree_large1_noSprite",            "",             "",            "",             ""},
                         };
                square = GenTiles(format, offSetx, offSetY, rand);
                squares.add(square);
            }
            else {
                // 1
                format = new String[][]{
                          new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",                "",                         "",                             "",                                 ""},

                           new String[]{"tree_large1",             "tree_large1_noSprite",                "ledge_grass_safari_up",     "ledge_grass_safari_up",         "ledge_grass_safari_up",             "ledge2_corner_tr"},

                           new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",                "",                         "",                             "",                                 "ledge_grass_right"},

                           new String[]{"tree_large1",             "tree_large1_noSprite",                "",                         "ledge_grass_inside_tl",         "ledge_grass_ramp",                 "ledge1_corner_br"},

                           new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",                "",                            "ledge_grass_right",            "",                                 "",},

                           new String[]{"tree_large1",             "tree_large1_noSprite",                "",                         "ledge_grass_safari_up",        "ledge_grass_safari_up",             "ledge2_corner_tr",},
                         };
                square = GenTiles(format, offSetx, offSetY, rand);
                squares.add(square);

            }
        }
        // maze node bottom and left is open
        else {
            // 1
//            square = new ArrayList<Tile>(); // no tiles
//            squares.add(square);

            // bottom ramp
            if (node.rampLoc == "down") {
                // 1
                format = new String[][]{
                          new String[]{"",                         "",                                "ledge_grass_left",                     "",             "",                     "ledge_grass_right"},

                           new String[]{"",                         "",                                "ledge1_corner_bl",         "ledge_grass_ramp",             "ledge_grass_down",     "ledge1_corner_br"},

                           new String[]{"",                         "",                                "",                         "",                             rs[rand.nextInt(rs.length)],     ""},

                           new String[]{"",                         rs[rand.nextInt(rs.length)],"",                         "",                             "",                                 ""},

                           new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",            rs[rand.nextInt(rs.length)],"",                        "",                                 "",},

                           new String[]{"tree_large1",             "tree_large1_noSprite",            "",                         "",                                "",                                 "",},
                         };
                square = GenTiles(format, offSetx, offSetY, rand);
                squares.add(square);
            }
            // top ramp
            else if (node.rampLoc == "up") {
                // 2
                format = new String[][]{
                          new String[]{"",                         "",                                "",                         "",                             "",                     ""},

                           new String[]{"",                         "ledge2_corner_tl",                "ledge_grass_safari_up",     "ledge_grass_safari_up",         "ledge_grass_safari_up",             "ledge2_corner_tr"},

                           new String[]{"",                         "ledge_grass_left",                "",                         "",                             "",                                 "ledge_grass_right"},

                           new String[]{"",                         "ledge1_corner_bl",                "ledge_grass_ramp",         "ledge_grass_inside_tr",         "",                                 "ledge_grass_right"},

                           new String[]{"",                         "",                                "",                            "ledge_grass_left",                "",                                 "ledge_grass_right",},

                           new String[]{"",                         "",                                "ledge2_corner_tl",         "ledge_grass_safari_up",        "",                                 "ledge_grass_right",},
                         };
                square = GenTiles(format, offSetx, offSetY, rand);
                squares.add(square);
            }
            // left ramp
            else if (node.rampLoc == "left") {
                // 1
                format = new String[][]{
                           new String[]{"",                         "",                                "ledge_grass_left",     "",             "",                     ""},

                           new String[]{"",                         "",                                "ledge_grass_left",     "",             "",                     ""},

                           new String[]{"",                         "",                                "ledge1_corner_br",     "ledge_grass_ramp",             "ledge_grass_inside_tr",                 ""},

                           new String[]{"",                         "",                                "",                     "",                             "ledge_grass_left",                     ""},

                           new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",            "",                     "",                                "ledge_grass_left",                     "",},

                           new String[]{"tree_large1",             "tree_large1_noSprite",            "",                     "",                                "ledge1_corner_br",                     "ledge_grass_down",},
                         };
                if (node.downOpen == true) {
                    FlipFormat(format);
                }
                square = GenTiles(format, offSetx, offSetY, rand);
                squares.add(square);
            }
            // right ramp
            else {
                // 1
                format = new String[][]{
                           new String[]{"",                         "",                                "",                     "ledge_grass_right",             "",                     ""},

                           new String[]{"",                         "",                                "",                     "ledge_grass_right",             "",                     ""},

                           new String[]{"",                         "ledge_grass_inside_tl",        "ledge_grass_ramp",     "ledge1_corner_br",             "",                     ""},

                           new String[]{"",                         "ledge_grass_right",                "",                     "",                             "",                     ""},

                           new String[]{"",                         "ledge_grass_right",            "",                        "",                                "tree_large1_noSprite", "tree_large1_noSprite",},

                           new String[]{"ledge_grass_down",         "ledge1_corner_br",            "",                     "",                                "tree_large1",             "tree_large1_noSprite",},
                         };
                if (node.downOpen == true) {
                    FlipFormat(format);
                }
                square = GenTiles(format, offSetx, offSetY, rand);
                squares.add(square);
            }

        }

        // choose randomly from ArrayList of squares
        int randomNum = rand.nextInt(squares.size());
        // randomNum = 0; // debug
        return squares.get(randomNum);
    }

    public static boolean[][] Maze_Algo2(int width, int height, float complexity, float density, Random rand) {
        width = (int)(width / 2) * 2 + 2;
        height = (int)(height / 2) * 2 + 2;

        complexity = (int)(complexity * (5 * (height + width)));
        density    = (int)(density * ( (int)(height / 2) * (int)(width / 2) ));

        boolean[][] Z = new boolean[ width ][ height ];

        // Fill borders
        for (int i=0; i < width; i++) {
            for (int j=0; j < height; j++) {
                if ( i == 0 || j == 0 || i == width-2 || j == height-2) {
                    Z[i][j] = Boolean.TRUE;
                }
            }
        }
        for (int i=0; i < density; i++) {
            // Get even number between width and height
            int x = rand.nextInt( (int)(width / 2)  ) * 2;
            int y = rand.nextInt( (int)(height / 2)  ) * 2;
            Z[x][y] = Boolean.TRUE;

            for (int j=0; j < complexity; j++) {
                // Compile list of neighboring cells
                ArrayList<int[]> neighbours = new ArrayList<int[]>();
                if (x > 1) { neighbours.add(new int[]{x - 2, y}); }
                if (x < width - 2) { neighbours.add(new int[]{x + 2, y}); }
                if (y > 1) { neighbours.add(new int[]{x, y - 2}); }
                if (y < height - 2) { neighbours.add(new int[]{x, y + 2}); }

                if (!neighbours.isEmpty()) {
                    int randomNum = rand.nextInt(neighbours.size()); //-1? no, python includes, java excludes this number
                    int x_ = neighbours.get(randomNum)[0];
                    int y_ = neighbours.get(randomNum)[1];

                    if (Z[x_][y_] == Boolean.FALSE) {
                        Z[x_][y_] = Boolean.TRUE;
                        Z[x_ + (int)((x - x_) / 2)][y_ + (int)((y - y_) / 2)] = Boolean.TRUE;
                        x = x_;
                        y = y_;
                    }
                }
            }
        }
        // Debug - print maze
        for (int i=height-1; i >= 0; i--) {
            for (int j=0; j < width; j++) {
                System.out.print(String.valueOf(Z[j][i] ? 1 : 0)+" ");
            }
            System.out.print("\n");
        }
        return Z;
    }
    

    /**
     * Simplify density and complexity as an integer. Accept startLocs to override maze wall origin points.
     * 
     * startLocs is modified and turns into endpoints.
     */
    public static boolean[][] Maze_Algo3(int width, int height, float complexity, float density, Random rand, ArrayList<Vector2> startLocs) {
        width = (int)(width / 2) * 2 + 2;
        height = (int)(height / 2) * 2 + 2;
        
        ArrayList<Vector2> endpoints = new ArrayList<Vector2>();

        boolean[][] Z = new boolean[ width ][ height ];

        // Fill borders
//        for (int i=0; i < width; i++) {
//            for (int j=0; j < height; j++) {
//                if ( i == 0 || j == 0 || i == width-2 || j == height-2) {
//                    Z[i][j] = Boolean.TRUE;
//                }
//            }
//        }
        // Don't add to endpoints unless placed at least one wall.
        boolean placedOneWall = false;
        for (int i=0; i < density; i++) {
            // Get even number between width and height
            int x;
            int y;
            if (!startLocs.isEmpty()) {
                Vector2 newLoc = startLocs.remove(0);
                x = (int)newLoc.x;
                y = (int)newLoc.y;
            }
            else {
                x = rand.nextInt( (int)(width / 2)  ) * 2;
                y = rand.nextInt( (int)(height / 2)  ) * 2;
            }
            Z[x][y] = Boolean.TRUE;
            placedOneWall = false;

            for (int j=0; j < complexity; j++) {
                // Compile list of neighboring cells
                ArrayList<int[]> neighbours = new ArrayList<int[]>();
                if (x > 1) { neighbours.add(new int[]{x - 2, y}); }
                if (x < width - 2) { neighbours.add(new int[]{x + 2, y}); }
                if (y > 1) { neighbours.add(new int[]{x, y - 2}); }
                if (y < height - 2) { neighbours.add(new int[]{x, y + 2}); }

                if (!neighbours.isEmpty()) {
                    int randomNum = rand.nextInt(neighbours.size()); //-1? no, python includes, java excludes this number
                    int x_ = neighbours.get(randomNum)[0];
                    int y_ = neighbours.get(randomNum)[1];

                    if (Z[x_][y_] == Boolean.FALSE) {
                        Z[x_][y_] = Boolean.TRUE;
                        Z[x_ + (int)((x - x_) / 2)][y_ + (int)((y - y_) / 2)] = Boolean.TRUE;
                        x = x_;
                        y = y_;
                        placedOneWall = true;
                    }
                }
            }
            // Done adding to wall at this point, so save this point as an endpoint.
            if (placedOneWall) {
                endpoints.add(new Vector2(x, y));
            }
        }
        // Debug - print maze
        for (int i=height-1; i >= 0; i--) {
            for (int j=0; j < width; j++) {
                System.out.print(String.valueOf(Z[j][i] ? 1 : 0)+" ");
            }
            System.out.print("\n");
        }

        startLocs.clear();
        for (Vector2 point : endpoints) {
            startLocs.add(point);
        }

        return Z;
    }
    

    
    public static HashMap<Vector2, MazeNode> Maze_Algo1(int width, int height, float complexity, float density, int squareSize, Random rand) {
        // Only odd shapes
        // int[] shape = { (int)(height / 2) * 2 + 1, (int)(width / 2) * 2 + 1 };

        // TODO - +1 only if you want to handle edges differently
        // in the future, will likely surround all mazes with trees anyway. change to +1 when you do that.
        width = (int)(width / 2) * 2 + 2;
        height = (int)(height / 2) * 2 + 2;

//        System.out.println("width: "+String.valueOf(width));
//        System.out.println("height: "+String.valueOf(height));

        // Adjust complexity and density relative to maze size
         // density - num 'long walls'. long walls is a single connected wall
         // complexity - length of 'long walls'
        complexity = (int)(complexity * (5 * (height + width)));
        density    = (int)(density * ( (int)(height / 2) * (int)(width / 2) ));

        // maze starts as an array of booleans (two layers)
         // all values default to 'false'
        boolean[][] Z = new boolean[ width ][ height ];

        // Fill borders
        for (int i=0; i < width; i++) {
            for (int j=0; j < height; j++) {
                if ( i == 0 || i == (width-2) || j == 0 || j == (height-2)) {
                    Z[i][j] = Boolean.TRUE;
                }

            }
        }

        // Make aisles
        for (int i=0; i < density; i++) {
            // get even number between width and height
            int x = rand.nextInt( (int)(width / 2)  ) * 2; //+1 b/c java excludes this number
            int y = rand.nextInt( (int)(height / 2)  ) * 2;
            Z[x][y] = Boolean.TRUE;

            for (int j=0; j < complexity; j++) {
                // compile list of neighboring cells
                ArrayList<int[]> neighbours = new ArrayList<int[]>();
                if (x > 1) { neighbours.add(new int[]{x - 2, y}); }
                if (x < width - 2) { neighbours.add(new int[]{x + 2, y}); }
                if (y > 1) { neighbours.add(new int[]{x, y - 2}); }
                if (y < height - 2) { neighbours.add(new int[]{x, y + 2}); }

                if (!neighbours.isEmpty()) {
                    int randomNum = rand.nextInt(neighbours.size()); //-1? no, python includes, java excludes this number
                    int x_ = neighbours.get(randomNum)[0];
                    int y_ = neighbours.get(randomNum)[1];

                    if (Z[x_][y_] == Boolean.FALSE) {
                        Z[x_][y_] = Boolean.TRUE;
                        Z[x_ + (int)((x - x_) / 2)][y_ + (int)((y - y_) / 2)] = Boolean.TRUE;
                        x = x_;
                        y = y_;
                    }
                }
            }
        }

        // debug - print maze
        for (int i=height-1; i >= 0; i--) {
            for (int j=0; j < width; j++) {
                System.out.print(String.valueOf(Z[j][i] ? 1 : 0)+" ");
            }
            System.out.print("\n");
        }

        HashMap<Vector2, MazeNode> nodes = new HashMap<Vector2, MazeNode>();

        // convert array of booleans to mazenodes
         // need mazenodes so that we can choose templated tile
         // interpreting Z[0][0] as bottom-left corner
        for (int i=0; i < (width)/2; i++) {
            for (int j=0; j < (height)/2; j++) {
                boolean leftOpen = !Z[i*2][j*2+1];
                boolean downOpen = !Z[i*2+1][j*2];

                nodes.put(new Vector2(i, j), new MazeNode(i, j, new boolean[]{leftOpen, downOpen}, squareSize));

            }
        }

        return nodes;
    }

    public int layer = 120;

    ArrayList<Vector2> freePositions;

    // ArrayList<Tile> tilesToAdd;
    HashMap<Vector2, Tile> tilesToAdd;

    ArrayList<Action> doActions;

    Vector2 topLeft;

    Vector2 bottomRight;

    Random rand;
    
    public static HashMap<String, String> mates = new HashMap<String, String>();
    static {
        mates.put("nidoqueen", "nidoking");
        mates.put("nidoking", "nidoqueen");
        mates.put("charizard", "charizard");
        mates.put("venusaur", "venusaur");
//        mates.put("blastoise", "blastoise");
        mates.put("meganium", "meganium");
//        mates.put("typhlosion", "typhlosion");
        mates.put("nidorina", "nidorino");
        mates.put("nidorino", "nidorina");
//        mates.put("tauros", "miltank");
//        mates.put("miltank", "tauros");
    }
    public static HashMap<String, String> mates2 = new HashMap<String, String>();
    static {
        mates2.put("tauros", "miltank");
        mates2.put("miltank", "tauros");
    }

    public GenForest2(Game game, Vector2 startLoc, Vector2 endLoc) {
        // TODO -
         // get some ideas from GenForest1 for templates to make
         // somehow raised platforms can't cut off areas for access

        this.rand = new Random();
        this.tilesToAdd = new HashMap<Vector2, Tile>();
        this.freePositions = new ArrayList<Vector2>();
        this.doActions = new ArrayList<Action>();

        // a bunch of arraylists of Vector2/Tile Maps
         // these represent the 3x3 pieces
         // maybe have 'generate' function that will return a random of certain orientation?

        // random maze height/width

        // for now, just return a maze that is a square

        // for node in maze, get a mazeTile that has correct num openings
         // pass in node, whatever kind of struct that is
        int squareSize = 3*32;

        // TODO - delete below code, or hardcode a preset maze to test
//        ArrayList<MazeNode> nodes = new ArrayList<MazeNode>();
//        nodes.add(new MazeNode(0,0,new boolean[]{true,false,false,true}, squareSize)); // upOpen, downOpen, leftOpen, rightOpen, x, y
//        nodes.add(new MazeNode(1,0,new boolean[]{true,false,true,false}, squareSize));
//        nodes.add(new MazeNode(0,1,new boolean[]{false,true,false,true}, squareSize));
//        nodes.add(new MazeNode(1,1,new boolean[]{false,true,true,false}, squareSize));

        // Maze_Algo1 uses python example code from maze algo wiki
         // using because it can generate patches that are 'fully open', ie no walls
         // also is pretty adjustable
         // there are other good algo's - 'Recursive backtracker' with horizontal bias looks fun. could adapt algo1 with similar idea.
        int width = (int)((endLoc.x - startLoc.x)/squareSize)*2; // 8; //
        int height = (int)((endLoc.y - startLoc.y)/squareSize)*2; // 10; //
        // below settings allow for a few loops to be present on average, which is what I want
        float density = 0.1f; // num times a 'long wall' is created. algo works by creating a long wall at random  //.2f //.1f for 500 width
        float complexity = 0.9f; // length of that 'long wall' //.2f
        HashMap<Vector2, MazeNode> nodes = Maze_Algo1(width, height, density, complexity, squareSize, this.rand);

        // for each node, get a template tile that matches it's number of openings
         // nodes contains the nodes of the maze
        ArrayList<Tile> tileSquare;
        for (MazeNode node : nodes.values()) {
            // get tiles depending on type of mazenode
            if (node.type == "platform1") {
                tileSquare = getTileSquarePlatform1(node, startLoc, this.rand);
            }
            else {
                tileSquare = getTileSquare(node, startLoc, this.rand);
            }

            // need to account for offset
            // add these to tilesToAdd
            for (Tile tile : tileSquare) {
                // System.out.println("here");
                // translate tile to be in line with map
                 // bottom left corner node.x == 0, node.y == 0
                this.tilesToAdd.put(tile.position.cpy(), tile);
            }
        }

        // this.doActions.add(new ApplyForestBiome(game, this.tilesToAdd, startLoc, endLoc, this)); // if you want to apply later
        // for now:
        Action temp = new ApplyForestBiome(this.tilesToAdd, startLoc, endLoc.cpy(), this);
        temp.step(game); // this way map is rendered in pretty way, but apply biome is still an action
         // contains placement of water and grass

        // add platform - didn't end up liking this method
        // this.doActions.add(new AddPlatform(game, this.tilesToAdd, this.freePositions, startLoc, endLoc, this));

        // set start point

        // randomly plant grass/tree/flowers

        // fill in platform or pond

        // do more (until <= certain amt of freePositions left?)

        // fill in rest with grass

    }

    public int getLayer(){return this.layer;}

    @Override
    public void step(Game game) {
        if (this.tilesToAdd.isEmpty()) {
            if (this.doActions.isEmpty()) {
                game.actionStack.remove(this);

                // debug
                // spawn mega-gengar, interact to start battle
                game.map.tiles.put(new Vector2(0, 16 +48), new MegaGengarTile(new Vector2(0, 16 +48)));
                return;
            }
            Action currAction = this.doActions.get(0);
            this.doActions.remove(0);
            game.insertAction(currAction);
            game.actionStack.remove(this);
            return;
        }

        Tile currTile = this.tilesToAdd.values().iterator().next();
        game.map.tiles.put(currTile.position.cpy(), currTile);
        this.tilesToAdd.remove(currTile.position.cpy());

        // do i  more times (to speed up)
        for (int i=0; i < 10; i++) {
            if (!this.tilesToAdd.isEmpty()) {
                currTile = this.tilesToAdd.values().iterator().next();
                game.map.tiles.put(currTile.position.cpy(), currTile);
                this.tilesToAdd.remove(currTile.position.cpy());
            }
            else {
                break;
            }
        }

        // System.out.println("print: "+String.valueOf(currTile.name));
    }
    // unused - switched to templated tile method
    public class AddPlatform extends Action {
        public int layer = 120;
        Random rand;

        ArrayList<Tile> tilesToAdd;

        ArrayList<Vector2> freePositions;
        Vector2 startPosition; // top-left corner

        Action nextAction;

        int numLevels;
        // which sprite to use, ie two level mtn or 1 level
         // can potentially trace inside of raised area later

        Vector2 bottomLeft;

        Vector2 topRight;

        public AddPlatform(Game game, ArrayList<Tile> tilesToAdd, ArrayList<Vector2> freePositions, Vector2 startLoc, Vector2 endLoc,  Action nextAction) {
            this.nextAction = nextAction;
            this.rand = new Random();
            this.freePositions = freePositions;
            this.tilesToAdd = tilesToAdd;

            // choose a random start position within boundaries
            // x and y are midpoints of square (to make the square end up in middle on average)
            // assuming startLoc is bottom left
            int x = this.rand.nextInt((int)(endLoc.x - startLoc.x)) + (int)startLoc.x;
            int y = this.rand.nextInt((int)(endLoc.y - startLoc.y)) + (int)startLoc.y;
            // make variable width about 3/6 of range, +/- 1/6 of range for now
            int width = (int) (0.5f * (endLoc.x - startLoc.x) + ((this.rand.nextInt(10)+1)/10f) * (2f/6f) * (endLoc.x - startLoc.x) - (1f/6f) * (endLoc.x - startLoc.x) );
            int height = (int) (0.5f * (endLoc.y - startLoc.y) + ((this.rand.nextInt(10)+1)/10f) * (2f/6f) * (endLoc.y - startLoc.y) - (1f/6f) * (endLoc.y - startLoc.y) );

            this.bottomLeft = new Vector2(x - width/2, y - height/2);
            this.topRight = new Vector2(x + width/2, y + height/2);

        }

        public int getLayer(){return this.layer;}

        @Override
        public void step(Game game) {
            // this step gets executed only once

            // TODO - need to approach this differently
             // it cuts off too many routes.
            // would prefer if entrances were positioned only when there is a maze path it needs to complete.
            //

            ArrayList<Vector2> platformGoesHere = new ArrayList<Vector2>();

            // assemble a list of qmark tiles in region
             // TODO - may need to trim out qmarks next to solid block here
            for (Vector2 pos : this.freePositions) {
                if (pos.x >= this.bottomLeft.x && pos.x <= this.topRight.x
                    && pos.y >= this.bottomLeft.y && pos.y <= this.topRight.y) {
                    platformGoesHere.add(pos.cpy());
                }
            }

            // if couldn't find anything, return
            if (platformGoesHere.isEmpty()) {
                game.insertAction(this.nextAction);
                game.actionStack.remove(this);
                return;
            }

            // keep track of bottom ledges for later when you need to make ramp
            ArrayList<Vector2> bottomLedges = new ArrayList<Vector2>();

            // for each qmark_tile
            for (Vector2 pos : platformGoesHere) {
                // decide when type of platform it should be
                // Tile currTile;

                boolean upPlatform = false;
                boolean leftPlatform = false;
                boolean rightPlatform = false;
                boolean downPlatform = false;
                // check if right platform
                if (platformGoesHere.contains(new Vector2(pos.x+16, pos.y))) {
                    rightPlatform = true;
                }
                // check if left platform
                if (platformGoesHere.contains(new Vector2(pos.x-16, pos.y))) {
                    leftPlatform = true;
                }
                // check if up platform
                if (platformGoesHere.contains(new Vector2(pos.x, pos.y+16))) {
                    upPlatform = true;
                }
                // check if down platform
                if (platformGoesHere.contains(new Vector2(pos.x, pos.y-16))) {
                    downPlatform = true;
                }

                // check if left edge
                if (rightPlatform && !leftPlatform && upPlatform && downPlatform) {
                    this.tilesToAdd.add(new Tile("ledge_grass_left", new Vector2(pos.x, pos.y) ));
                }
                // check if right edge
                else if (!rightPlatform && leftPlatform && upPlatform && downPlatform) {
                    this.tilesToAdd.add(new Tile("ledge_grass_right", new Vector2(pos.x, pos.y) ));
                }
                // check if up ledge
                else if (rightPlatform && leftPlatform && !upPlatform && downPlatform) {
                    this.tilesToAdd.add(new Tile("ledge_grass_safari_up", new Vector2(pos.x, pos.y) ));
                }
                // check if down ledge
                else if (rightPlatform && leftPlatform && upPlatform && !downPlatform) {
                    this.tilesToAdd.add(new Tile("ledge_grass_down", new Vector2(pos.x, pos.y) ));
                    bottomLedges.add(pos);
                }
                // check if bl corner
                else if (rightPlatform && !leftPlatform && upPlatform && !downPlatform) {
                    this.tilesToAdd.add(new Tile("ledge1_corner_bl", new Vector2(pos.x, pos.y) ));
                }
                // check if br corner
                else if (!rightPlatform && leftPlatform && upPlatform && !downPlatform) {
                    this.tilesToAdd.add(new Tile("ledge1_corner_br", new Vector2(pos.x, pos.y) ));
                }
                // check if tl corner
                else if (rightPlatform && !leftPlatform && !upPlatform && downPlatform) {
                    this.tilesToAdd.add(new Tile("ledge2_corner_tl", new Vector2(pos.x, pos.y) ));
                }
                // check if tr corner
                else if (!rightPlatform && leftPlatform && !upPlatform && downPlatform) {
                    this.tilesToAdd.add(new Tile("ledge2_corner_tr", new Vector2(pos.x, pos.y) ));
                }
                // check if lone rock // TODO - rock2 as well?
                else if (!rightPlatform && !leftPlatform && !upPlatform && !downPlatform) {
                    this.tilesToAdd.add(new Tile("rock1", new Vector2(pos.x, pos.y) ));
                }
                // else, it's a regular platform top
                else {
                    this.tilesToAdd.add(new Tile("ground1", new Vector2(pos.x, pos.y) ));
                }

                // add that to tile to add to tilesToAdd

                // remove from free positions
                this.freePositions.remove(pos);

            }

            // System.out.println("array size: "+String.valueOf(bottomLedges.size()));
            if (!bottomLedges.isEmpty()) {
                // for ledge in bottomLedges // remove one
                this.tilesToAdd.add( new Tile("ledge_grass_ramp", bottomLedges.get(this.rand.nextInt(bottomLedges.size())).cpy()) );
                // chance to remove a second one
                if (this.rand.nextInt(2) == 1) {
                    this.tilesToAdd.add( new Tile("ledge_grass_ramp", bottomLedges.get(this.rand.nextInt(bottomLedges.size())).cpy()) );
                }
            }

            // put nextAction on stack, remove this.
            game.insertAction(this.nextAction);
            game.actionStack.remove(this);

        }

    }

    // unused - switched to templated tile method
    public static class ApplyForestBiome extends Action {
        public int layer = 120;
        Random rand;

        HashMap<Vector2, Tile> tilesToAdd;

        ArrayList<Vector2> freePositions; // TODO - remove if unused
        Action nextAction;

        Vector2 bottomLeft;

        Vector2 topRight;
        boolean color;
        Route currRoute;

        public ApplyForestBiome(HashMap<Vector2, Tile> tilesToAdd, Vector2 bottomLeft, Vector2 topRight,  Action nextAction) {
            this(tilesToAdd, bottomLeft, topRight, false, nextAction);
        }

        public ApplyForestBiome(HashMap<Vector2, Tile> tilesToAdd, Vector2 bottomLeft, Vector2 topRight, boolean color, Action nextAction) {
            this.color = color;
            this.nextAction = nextAction;
            this.rand = new Random();
            this.tilesToAdd = tilesToAdd;

            this.bottomLeft = bottomLeft;
            this.topRight = topRight;
            this.currRoute = new Route("forest1", 22);

        }

        public void addPond() {
            int maxSize = 12;
            ArrayList<Vector2> keySet = new ArrayList<Vector2>(this.tilesToAdd.keySet());
            Vector2 randBLCorner = keySet.get(this.rand.nextInt(keySet.size())).cpy();
            Vector2 randTRCorner = randBLCorner.cpy().add(this.rand.nextInt(maxSize)*16 + 32, this.rand.nextInt(maxSize)*16 + 32);
            ArrayList<Vector2> pondGoesHere = new ArrayList<Vector2>();

            for (float i=randBLCorner.x; i < randTRCorner.x; i += 16) {
                for (float j=randBLCorner.y; j < randTRCorner.y; j += 16) {
                    int surroundedBy = 0;
                    Tile temp2 = this.tilesToAdd.get(new Vector2(i+16,j));
                    if (temp2 != null) {
                        if (temp2.name == "solid") {
                            surroundedBy++;
                        }
                    }
                    temp2 = this.tilesToAdd.get(new Vector2(i-16,j));
                    if (temp2 != null) {
                        if (temp2.name == "solid") {
                            surroundedBy++;
                        }
                    }
                    temp2 = this.tilesToAdd.get(new Vector2(i,j+16));
                    if (temp2 != null) {
                        if (temp2.name == "solid") {
                            surroundedBy++;
                        }
                    }
                    temp2 = this.tilesToAdd.get(new Vector2(i,j-16));
                    if (temp2 != null) {
                        if (temp2.name == "solid") {
                            surroundedBy++;
                        }
                    }
                    Tile temp = this.tilesToAdd.get(new Vector2(i,j));
                    if (temp != null && surroundedBy > 1) {
                        if (temp.name == "solid" ) {
                            pondGoesHere.add(temp.position.cpy());
                        }
                    }
                }
            }

            // for each qmark_tile
            for (Vector2 pos : pondGoesHere) {
                // decide when type of platform it should be
                // Tile currTile;

                boolean upPond = false;
                boolean leftPond = false;
                boolean rightPond = false;
                boolean downPond = false;
                // check if right platform
                if (pondGoesHere.contains(new Vector2(pos.x+16, pos.y))) {
                    rightPond = true;
                }
                // check if left platform
                if (pondGoesHere.contains(new Vector2(pos.x-16, pos.y))) {
                    leftPond = true;
                }
                // check if up platform
                if (pondGoesHere.contains(new Vector2(pos.x, pos.y+16))) {
                    upPond = true;
                }
                // check if down platform
                if (pondGoesHere.contains(new Vector2(pos.x, pos.y-16))) {
                    downPond = true;
                }

                // check if left edge
                if (rightPond && !leftPond && upPond && downPond) {
                    this.tilesToAdd.put(pos.cpy(), new Tile("water1_ledge1_left", new Vector2(pos.x, pos.y) ));
                }
                // check if right edge
                else if (!rightPond && leftPond && upPond && downPond) {
                    this.tilesToAdd.put(pos.cpy(), new Tile("water1_ledge1_right", new Vector2(pos.x, pos.y) ));
                }
                // check if up ledge
                else if (rightPond && leftPond && !upPond && downPond) {
                    this.tilesToAdd.put(pos.cpy(), new Tile("water1_ledge1_top", new Vector2(pos.x, pos.y) ));
                }
                // check if tl corner
                else if (rightPond && !leftPond && !upPond && downPond) {
                    this.tilesToAdd.put(pos.cpy(), new Tile("water1_ledge1_tl", new Vector2(pos.x, pos.y) ));
                }
                // check if tr corner
                else if (!rightPond && leftPond && !upPond && downPond) {
                    this.tilesToAdd.put(pos.cpy(), new Tile("water1_ledge1_tr", new Vector2(pos.x, pos.y) ));
                }
                // check if bl corner
                else if (rightPond && !leftPond && upPond && !downPond) {
                    this.tilesToAdd.put(pos.cpy(), new Tile("water1_ledge1_left", new Vector2(pos.x, pos.y) ));
                }
                // check if br corner
                else if (!rightPond && leftPond && upPond && !downPond) {
                    this.tilesToAdd.put(pos.cpy(), new Tile("water1_ledge1_right", new Vector2(pos.x, pos.y) ));
                }
                // else, it's a regular platform top
                else if ((rightPond ? 1 : 0) + (leftPond ? 1 : 0) + (upPond ? 1 : 0) + (downPond ? 1 : 0) > 1){
                    this.tilesToAdd.put(pos.cpy(), new Tile("water1", new Vector2(pos.x, pos.y) ));
                }

            }

        }

        public void fillAllEmptyTiles() {
            String[] randomTile = {"ground3", "ground1", "ground3", "ground1", "ground3", "ground1", "ground3", "ground1", "flower1"};

            for (float i=this.bottomLeft.x; i < this.topRight.x; i += 16) { // + 64 + 64
                for (float j=this.bottomLeft.y; j < this.topRight.y; j += 16) { // + 64 + 64 + 64
                    Tile temp = this.tilesToAdd.get(new Vector2(i,j));
                    // check if there is nothing there yet
                    String tileName = randomTile[this.rand.nextInt(randomTile.length)];
                    if (temp == null && tileName != "") {
                        // add random element
                        this.tilesToAdd.put(new Vector2(i,j), new Tile(tileName, new Vector2(i,j)));
                    }
                }
            }
        }

        public int getLayer(){return this.layer;}

        public void plantGrass() {
            int minNumPatches = 2;
            int maxNumPatches = 12;
            // if no start position specified,
             // get random position to plant tree
            Vector2 startPosition = null;

            int randomIndex;
            Vector2 randomPos;
            int iter = 0;
            while (startPosition == null && iter < 20) {
                if ((int)topRight.x-(int)bottomLeft.x - 64 == 0) {
                    break;
                }
//                ArrayList<Vector2> keySet = new ArrayList<Vector2>(this.tilesToAdd.keySet());
//                randomPos = keySet.get(this.rand.nextInt(keySet.size())).cpy();
                iter++; // prevents infinite loop
                int randX = Math.abs((int)topRight.x-(int)bottomLeft.x - 64) + (int)bottomLeft.x;
                randX = (int)((this.rand.nextInt(randX <= 0 ? 1 : randX))/16) * 16;
                int randY = Math.abs((int)topRight.y-(int)bottomLeft.y - 64) + (int)bottomLeft.y;
                randY = (int)(this.rand.nextInt((randY <= 0 ? 1 : randY))/16) * 16;
                randomPos = new Vector2(randX, randY);

                if (this.tilesToAdd.get(randomPos.cpy().add(0,0)) != null) {
                    continue;
                }
                if (this.tilesToAdd.get(randomPos.cpy().add(16,0)) != null) {
                    continue;
                }
                if (this.tilesToAdd.get(randomPos.cpy().add(0,16)) != null) {
                    continue;
                }
                if (this.tilesToAdd.get(randomPos.cpy().add(16,16)) != null) {
                    continue;
                }
                startPosition = randomPos;
            }

            if (startPosition == null) { // return if we failed to find a startPos
                return;
            }
            String name = "grass1";
            if (this.color) {
                name = "grass2";
            }
            this.tilesToAdd.put(startPosition.cpy(), new Tile(name, startPosition.cpy(), this.color, this.currRoute));
            this.tilesToAdd.put(startPosition.cpy().cpy().add(16,0), new Tile(name, startPosition.cpy().add(16,0), this.color, this.currRoute));
            this.tilesToAdd.put(startPosition.cpy().cpy().add(0,16), new Tile(name, startPosition.cpy().add(0,16), this.color, this.currRoute));
            this.tilesToAdd.put(startPosition.cpy().cpy().add(16,16), new Tile(name, startPosition.cpy().add(16,16), this.color, this.currRoute));

            Vector2 currPos = startPosition.cpy();

            // get number of grass patches to plant

            int randomNum = this.rand.nextInt(maxNumPatches-minNumPatches) + minNumPatches; // 1 - 4

//            System.out.println("new grass: "+String.valueOf(startPosition.x)+", "+String.valueOf(startPosition.y)); // debug
//            System.out.println("rand: "+String.valueOf(randomNum)); // debug
//
//            System.out.println("randomNum: "+String.valueOf(randomNum));
            for (int i=0; i < randomNum; i++) {
                ArrayList<Vector2> nextPositions = new ArrayList<Vector2>();
                //

                // left
                if (this.tilesToAdd.get(currPos.cpy().add(-16,0)) == null) {
                    if (this.tilesToAdd.get(currPos.cpy().add(-16,+16)) == null) {
                        nextPositions.add(currPos.cpy().add(-16,0));
                    }
                }
                // right
                if (this.tilesToAdd.get(currPos.cpy().add(+32,0)) == null) {
                    if (this.tilesToAdd.get(currPos.cpy().add(+32,+16)) == null) {
                        nextPositions.add(currPos.cpy().add(+16,0));
                    }
                }
                // up
                if (this.tilesToAdd.get(currPos.cpy().add(0,32)) == null) {
                    if (this.tilesToAdd.get(currPos.cpy().add(+16,+32)) == null) {
                        nextPositions.add(currPos.cpy().add(0,16));
                    }
                }
                // down
                if (this.tilesToAdd.get(currPos.cpy().add(0,-16)) == null) {
                    if (this.tilesToAdd.get(currPos.cpy().add(+16,-16)) == null) {
                        nextPositions.add(currPos.cpy().add(0,-16));
                    }
                }

                // if you can't go anywhere, stop trying to add grass
                if (nextPositions.isEmpty()) {
                    break;
                }
                // choose one randomly from nextPositions
                randomIndex = this.rand.nextInt(nextPositions.size());

                Vector2 newPos = nextPositions.get(randomIndex);
                // put it in tilesToAdd
                this.tilesToAdd.put(newPos.cpy(), new Tile(name, newPos.cpy()));
                this.tilesToAdd.put(newPos.cpy().add(16,0), new Tile(name, newPos.cpy().add(16,0), this.color, this.currRoute));
                this.tilesToAdd.put(newPos.cpy().add(0,16), new Tile(name, newPos.cpy().add(0,16), this.color, this.currRoute));
                this.tilesToAdd.put(newPos.cpy().add(16,16), new Tile(name, newPos.cpy().add(16,16), this.color, this.currRoute));

                currPos = newPos;
            }
            return;
        }

        @Override
        public void step(Game game) {
            // add ponds
             // just make this a square

            // TODO: scale with size
            // TODO: fix this and re-enable at some point.
//            addPond();
//            addPond();

            // for everything between bottomLeft and topRight
            // add solid sprites
             // TODO -
            for (float i=this.bottomLeft.x; i < this.topRight.x + 64 + 64; i += 16) {
                for (float j=this.bottomLeft.y; j < this.topRight.y + 64 + 64 + 64; j += 16) {
                    Tile temp = this.tilesToAdd.get(new Vector2(i,j));
                    if (temp != null) {
                        if (temp.name == "solid") {
                            boolean noTileYet = true;
                            Tile temp2 = this.tilesToAdd.get(new Vector2(i+16,j));
                            Tile temp3 = this.tilesToAdd.get(new Vector2(i,j+16));
                            Tile temp4 = this.tilesToAdd.get(new Vector2(i+16,j+16));
                            if (Math.abs(temp.position.x - this.bottomLeft.x) % 32 == 0 && Math.abs(temp.position.y - this.bottomLeft.y) % 32 == 0
                                && temp2 != null && temp3 != null && temp4 != null) {
                                // put tree there
                                if (temp2.name == "solid" && temp3.name == "solid" && temp4.name == "solid") {
                                    this.tilesToAdd.put(temp.position.cpy().add(0,0), new Tile("tree_large1", temp.position.cpy().add(0,0), this.color));
                                    this.tilesToAdd.put(temp.position.cpy().add(16,0), new Tile("tree_large1_noSprite", temp.position.cpy().add(16,0)));
                                    this.tilesToAdd.put(temp.position.cpy().add(0,16), new Tile("tree_large1_noSprite", temp.position.cpy().add(0,16)));
                                    this.tilesToAdd.put(temp.position.cpy().add(16,16), new Tile("tree_large1_noSprite", temp.position.cpy().add(16,16)));
                                    noTileYet = false;
                                }
                            }
                            if (noTileYet){
                                // TODO - add small tree here
                                // each bush gets it's own route consisting of one pokemon
                                Route tempRoute = new Route("", 11);
                                String[] pokemon = new String[]{"pineco", "aipom", "kakuna", "metapod", "spinarak", "heracross",
                                                                "ledyba", "hoothoot", "zubat", "pidgey", "spearow", "forretress"};
                                // 1 in 3 ish bushes has a pokemon
                                int randInt = this.rand.nextInt(3);
                                if (randInt == 2) {
                                    randInt = this.rand.nextInt(pokemon.length);
                                    tempRoute.storedPokemon.add(new Pokemon(pokemon[randInt], 10+this.rand.nextInt(4)));
                                }
                                this.tilesToAdd.put(temp.position.cpy().add(0,0), new Tile("bush1", temp.position.cpy().add(0,0), this.color, tempRoute));
                            }
                        }
                    }
                }
            }

            // plant grass in random areas
            // TODO: scale with size
            // TODO: not sure what to do here.
//            plantGrass();
//            plantGrass();
//            plantGrass();
//            plantGrass();
//            plantGrass();
//            plantGrass();
//            plantGrass();

            // TODO - put berry tree

            // fill with nothing, ground or flowers
            // TODO: was used for gen1 maze... not sure what to do with it.
            // Causes bug when using with applyblotch(), basically adds a bunch of unneeded tiles on edges.
//            fillAllEmptyTiles();

        }

    }

}

// idea - when creating a maze that 'has to go to one spot',
 // as more nodes are added to 'visited nodes', increase the odds
 // of choosing node in right direction slightly. that way maze
 // will be 'geared around' that area. note that if u generate
 // a maze filling a solid square, you can put entrance and exit anywhere.
 // ie point is to create a maze that isn't too complex, and heads generally
 // from entrance to exit

class GenIsland1 extends Action {
    public int layer = 0;
    ArrayList<Vector2> freePositions;

    // ArrayList<Tile> tilesToAdd;
    HashMap<Vector2, Tile> tilesToAdd;
    ArrayList<HashMap<Vector2, Tile>> interiorTilesToAdd = new ArrayList<HashMap<Vector2, Tile>>();
    {
        for (int i=0; i < 102; i++) {
            interiorTilesToAdd.add(null);
        }
    }
//    HashMap<Vector2, Tile> edgeTiles;

    HashMap<Vector2, Pokemon> pokemonToAdd = new HashMap<Vector2, Pokemon>();
    
    ArrayList<Action> doActions;

    Vector2 bottomLeft = null;
    Vector2 topRight = null;
    Vector2 origin;
    int radius;
    // Edges where players can spawn
    ArrayList<Tile> edges = new ArrayList<Tile>();

    Random rand;
    
    // used to know if this has spawned the pokemon mansion yet or not (only spawn one)
    public static boolean donePkmnMansion = false;
    public static boolean donePkmnMansionKey = false;
    public static boolean doneRegiDungeon = false;
    public static boolean doneFossilBuilding = false;
    public static int unownCounter = 0;
    

    ArrayList<Vector2> keystoneSpawns = new ArrayList<Vector2>();

    public HashMap<Vector2, Tile> mtnTiles = new HashMap<Vector2, Tile>();

    public static int doneDesert = 0;

    public HashMap<Pokemon, Integer> interiorPokemon = new HashMap<Pokemon, Integer>();
    // Candidate locations for where to place trapinch in the desert
    public ArrayList<Vector2> trapinchSpawns = new ArrayList<Vector2>();

    public GenIsland1(Game game, Vector2 origin, int radius) {
        this.radius = radius;
        this.origin = origin;
        this.rand = new Random();
        this.tilesToAdd = new HashMap<Vector2, Tile>();
        this.doActions = new ArrayList<Action>();

        // for each tile in edges
        //  for each open edge, decide if you will put tile
        //   check that it doesn't exist already
        //  chance decreases as you move outward
        //  add those tiles to list, then remove self
        int maxDist = this.radius;  // 16*10;
        Tile originTile = new Tile("sand1", this.origin.cpy());
        this.tilesToAdd.put(originTile.position.cpy(), originTile);
        
        // TODO: debug, remove
        // Generates a standalone biome
//        if (true) {
//            originTile = new Tile("sand1", this.origin.cpy().add(-256*4, -256*4));
//            this.tilesToAdd.put(originTile.position.cpy(), originTile);
//            Route blotchRoute2 = new Route("graveyard1", 22);
//            ApplyBlotch(game, "graveyard1", originTile, 500, this.tilesToAdd, 0, false, blotchRoute2);
//            this.edges.add(originTile);
//            this.bottomLeft = originTile.position.cpy().add(-128, -128);
//            this.topRight = originTile.position.cpy().add(128, 128);
//            return;
//        }

//        Route blotchRoute = new Route("desert1", 40);
//        ApplyBlotch(game, "desert", originTile, maxDist/36, this.tilesToAdd, 1, false, blotchRoute);

        // TODO: uncomment this for just giant island
//        ApplyBlotch(game, "island", originTile, maxDist, this.tilesToAdd, 1, false, new Route("forest1", 22));

        // TODO: debug, revert
        ArrayList<Tile> endPoints = ApplyBlotchMountain(game, originTile, maxDist, this.mtnTiles);
        System.out.println("endPoints size");
        System.out.println(endPoints.size());
        for (Tile tile : endPoints) {
            if (GenIsland1.doneDesert == 1) {
                // TODO: seems to be happening near middle of mountain, would like it to be elsewhere.
                Route blotchRoute = new Route("desert1", 40);
                ApplyBlotch(game, "desert", tile, maxDist/18 +maxDist/4, this.tilesToAdd, 1, true, blotchRoute);
                continue;
            }
            else {
                GenIsland1.doneDesert++;
            }
            
              // TODO: this might be fixed, test
            Route blotchRoute = new Route("forest1", 40);
            // TODO: maxDist/6 is too big I think for some islands.
            // maxDist/6 for 100x100 island, it looked pretty good.
            // maxDist/10 for 100x180 island
            // maxDist/14 for 100x350 island
            // maxDist/18 for 100x500 island
            // TODO: could try adding more layers to mountains for larger islands.
            // TODO: maxDist/18 is actually working pretty well for most sizes.
            ApplyBlotch(game, "island", tile, maxDist/18, this.tilesToAdd, 1, true, blotchRoute); 
        }

        // TODO: this probably will tack on even more processing time.
        // TODO: doesn't really work, bleeds into upper mountain area
        long startTime = System.currentTimeMillis();
        for (Tile tile : this.mtnTiles.values()) {
            if (this.tilesToAdd.containsKey(tile.position)) {
                Tile currTile = this.tilesToAdd.get(tile.position);
                if (currTile.name.contains("desert") &&
                    tile.isBottomMtnLayer) {
                    continue;
                }
            }
            this.tilesToAdd.put(tile.position, tile);
        }
        System.out.println("End mountain tile dither: " + String.valueOf(System.currentTimeMillis()-startTime));
//        this.tilesToAdd.putAll(this.mtnTiles);  // TODO: remove

        // Remove 'stray' overworld pokemon
        for (Vector2 pos : this.mtnTiles.keySet()) {
            if (this.pokemonToAdd.containsKey(pos)) {
                this.pokemonToAdd.remove(pos);
            }
        }

        // Find max/min x and y tiles, add padding and add water tiles
        ArrayList<Tile> desertTiles = new ArrayList<Tile>();
        ArrayList<Tile> deepForestTiles = new ArrayList<Tile>();
        Vector2 maxPos = this.origin.cpy();
        Vector2 minPos = this.origin.cpy();
        for (Tile tile : new ArrayList<Tile>(this.tilesToAdd.values())) {
            if (maxPos.x < tile.position.x) {
                maxPos.x = tile.position.x;
            }
            if (maxPos.y < tile.position.y) {
                maxPos.y = tile.position.y;
            }
            if (minPos.x > tile.position.x) {
                minPos.x = tile.position.x;
            }
            if (minPos.y > tile.position.y) {
                minPos.y = tile.position.y;
            }

            // TODO: use deep_forest tiles for regi/pokemon mansion placement.
            if (tile.name.contains("desert")) {
                desertTiles.add(tile);
            }
            //tile.routeBelongsTo != null && tile.routeBelongsTo.name.equals("deep_forest") 
            else if (tile.biome.equals("deep_forest")) {
                deepForestTiles.add(tile);
            }
        }
        // TODO: test. Needed more water tile padding.
//        maxPos.add(16*14, 16*14);
//        minPos.sub(16*14, 16*14);
        maxPos.add(16*56, 16*56);
        minPos.sub(16*56, 16*56);
        Vector2 pos;
//        Route seaRoute = new Route("sea1", 10);  // TODO: remove, unused (?)
        for (float i=minPos.x; i < maxPos.x; i+=16) {
            for (float j=minPos.y; j < maxPos.y; j+=16) {
                pos = new Vector2(i, j);
                if (!this.tilesToAdd.containsKey(pos)) {
                    this.tilesToAdd.put(pos.cpy(), new Tile("water2", pos.cpy()));
                }
                // TODO: remove if unused
                // add black tiles to interior
//                game.map.interiorTiles.get(game.map.interiorTilesIndex).put(pos.cpy(), new Tile("black1", pos.cpy()));
            }
        }
        // TODO: test
        this.bottomLeft = minPos.cpy();
        this.topRight = maxPos.cpy();

        // Place graveyard biome(s) based on conditions
        // Needs to happen before regi dungeon and mansion
        int placeNum = this.rand.nextInt(7) +1;
        int tries3 = 20;
        while (deepForestTiles.size() > 0) {
            System.out.println("hi");
            Tile originTile2 = deepForestTiles.get(this.rand.nextInt(deepForestTiles.size()));
            boolean isInForest = originTile2.routeBelongsTo != null && originTile2.routeBelongsTo.name.equals("forest1");
            boolean nearWater = false;
            for (Tile newTile : tilesToAdd.values()) {
                if (newTile.name.contains("water") && newTile.position.dst2(originTile2.position) < 230400) {
                    nearWater = true;
                    break;
                }
            }
            if (this.tilesToAdd.get(originTile2.position).biome.equals("graveyard") ||
                !isInForest ||
                nearWater) {
                tries3--;
                if (tries3 <= 0) {
                    break;
                }
                continue;
            }
            HashMap<Vector2, Tile> newTiles = new HashMap<Vector2, Tile>();
            Route blotchRoute = new Route("graveyard1", 22);
            ApplyBlotch(game, "graveyard1", originTile2, 300, newTiles, 0, false, blotchRoute);
            // TODO: large trees issue, maybe
            for (Vector2 position : newTiles.keySet()) {
                if (this.mtnTiles.containsKey(position)) {
                    continue;
                }
                this.tilesToAdd.put(position, newTiles.get(position));
            }
            // TODO: debug, remove
            this.tilesToAdd.putAll(newTiles);
            placeNum--;
            if (placeNum <= 0) {
                break;
            }
        }
        // Spawn keystone somewhere
        Vector2 edge = this.keystoneSpawns.get(this.rand.nextInt(this.keystoneSpawns.size()));
        Tile newTile4 = tilesToAdd.get(edge);
        newTile4 = new Tile("green12", "odd_keystone2", edge.cpy(), true, newTile4.routeBelongsTo);
        newTile4.biome = "graveyard";
        tilesToAdd.put(edge, newTile4);

        // Remove 'stray' overworld pokemon (again, this time from desert)
        // Do this before placing Darmanitan, otherwise it will remove them.
        for (Tile tile : desertTiles) {
            if (this.pokemonToAdd.containsKey(tile.position)) {
                this.pokemonToAdd.remove(tile.position);
            }
        }
        
        // TODO: add pokemon mansion, regi dungeon and mansion key here
        Rectangle[] hitBoxes = new Rectangle[3];
        hitBoxes[0] = new Rectangle(0, 0, 1056, 1056);  // Desert ruins; 1056 = 22*3*16
        // TODO: did I mean 40*16 here?
        // Likely works because the ruins hitbox is so big
        hitBoxes[1] = new Rectangle(0, 0, 40, 40);  // Oasis
        // TODO: placing dungeon origin but not using it atm.
        hitBoxes[2] = new Rectangle(0, 0, 13*16, 10*16);  // Regi dungeon

        Vector2[] origins = new Vector2[3];
        origins[0] = null;  // Desert ruins
        origins[1] = null;  // Oasis
        origins[2] = null;  // Regi dungeon

        //
        Vector2 mansionPos = new Vector2(0, 16*14);
        Rectangle mansionBox = new Rectangle(0, 0, 33*16, 31*16);
        mansionBox.setCenter(mansionPos);
         
        for (int i=0, tries=0; i < hitBoxes.length;) {
            // Pick start point
            if (i < 2) {
                origins[i] = desertTiles.get(this.rand.nextInt(desertTiles.size())).position;
            }
            else {
                origins[i] = deepForestTiles.get(this.rand.nextInt(deepForestTiles.size())).position;
            }
            
            // Special rules for Desert Ruins origin point
            if (i == 0) {
                int tries2 = 0;
                while (tries2 < 80 && desertTiles.size() > 0) {
                    // newTile.position.x % 64 == 0 looked neat
//                     newTile.position.x % 64 == 32
                    if (origins[i].x % 32 == 0 && origins[i].y % 32 == 16) {
                        break;
                    }
                    origins[i] = desertTiles.get(this.rand.nextInt(desertTiles.size())).position;
                    tries2++;
                }
            }
            // Adjust hitbox to that position
            hitBoxes[i].setCenter(origins[i]);
            // Avoid mansion if desert ruins
            if (i == 0) {
                if (hitBoxes[i].overlaps(mansionBox)) {
                    continue;
                }
            }
            // Regi dungeon avoids mountains
            if (i > 1) {
                boolean hitsMountain = false;
                for (Vector2 position : this.mtnTiles.keySet()) {
                    if (hitBoxes[i].contains(position)) {
                        hitsMountain = true;
                        break;
                    }
                }
                if (hitsMountain) {
                    continue;
                }
            }
            // Check that all previous hitboxes don't collide with the chosen point.
            boolean worked = true;
            for (int j=0; j < i; j++) {
                if (hitBoxes[i].overlaps(hitBoxes[j])) {
                    worked = false;
                    break;
                }
            }
            if (worked || tries > 10) {
                i++;
                tries = 0;
                if (tries > 10) {
                    // TODO: debug, remove
                    System.out.println("Gave up.");
                    System.out.println(i);
                }
            }
            else {
                tries++;
            }
        }

        // Place desert ruins exterior and interior
        this.generateDesertRuins(game, origins[0]);

        // Place oasis exterior and interior
        this.generateOasis(game, origins[1]);

        // Place regi dungeon
        // TODO: was not able to get this fully working (mansion spawned over it a lot)
        // TODO: various issues with hitbox method
        //  - Mansion has weird hitbox for mountains. Basically it needs 2 hitboxes
        //   - possible that all dungeons could use separate mountains/non-mountains hitbox
//        this.generateRegiDungeon(game, origins[2]);

        // post-process - remove stray trees
        // Might need a better way to store tiles bigger than 16x16
        // Timed this part, took 42 milliseconds for large map.
//        for (int i=0; i < 1; i++) {
        boolean complete = false;
        Vector2 keyLoc = null;
        HashMap<Vector2, Tile> mansionExteriorTiles = new HashMap<Vector2, Tile>();
        ArrayList<HashMap<Vector2, Tile>> mansionInteriorTiles = new ArrayList<HashMap<Vector2, Tile>>();
        while (!complete) {
            complete = true;
            Vector2 tl;
            Vector2 tr;
            Vector2 bl;
            Vector2 br;
            Vector2 left;
            Vector2 right;
            Vector2 up;
            Vector2 down;
            for (Tile tile : new ArrayList<Tile>(tilesToAdd.values())) {
                // Helped but introduced some issues
//                if (tile.name.equals("tree_large1")) {
////                    tl = tile.position.cpy().add(-16*1,16*1);
//                    br = tile.position.cpy().add(-16*0+16,16*0-16);
////                    boolean foundTl = this.tilesToAdd.containsKey(tl) && this.tilesToAdd.get(tl).name.equals("tree_large1");
//                    boolean foundBr = this.tilesToAdd.containsKey(br) && this.tilesToAdd.get(br).name.equals("tree_large1");
//                    if (foundBr) {
//                        this.tilesToAdd.put(tile.position, new Tile("bush1", tile.position.cpy(), true, tile.routeBelongsTo));
//                    }
//                }
//                else
                if (tile.name.contains("tree_large1")) {
                    boolean found = false;
                    for (int j=0; j < 2; j++) {
                        for (int k=0; k < 2; k++) {
                            tl = tile.position.cpy().add(-16*j,16*k);
                            tr = tile.position.cpy().add(-16*j+16,16*k);
                            bl = tile.position.cpy().add(-16*j,16*k-16);
                            br = tile.position.cpy().add(-16*j+16,16*k-16);
                            boolean foundTl = this.tilesToAdd.containsKey(tl) && this.tilesToAdd.get(tl).name.equals("tree_large1_noSprite");
                            boolean foundTr = this.tilesToAdd.containsKey(tr) && this.tilesToAdd.get(tr).name.equals("tree_large1_noSprite");
                            boolean foundBl = this.tilesToAdd.containsKey(bl) && this.tilesToAdd.get(bl).name.equals("tree_large1");
                            boolean foundBr = this.tilesToAdd.containsKey(br) && this.tilesToAdd.get(br).name.equals("tree_large1_noSprite");
                            if (foundTl && foundTr && foundBl && foundBr) {
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            break;
                        }
                    }
                    if (!found) {
                        this.tilesToAdd.put(tile.position, new Tile("bush1", tile.position.cpy(), true, tile.routeBelongsTo));
                    }
                }
                // Remove rocks that block in areas around the edges.
                else if (tile.name.equals("rock1")) {
                    left = tile.position.cpy().add(-16f, 0f);
                    tl = tile.position.cpy().add(-16f, 16f);
                    bl = tile.position.cpy().add(-16f, -16f);
                    right = tile.position.cpy().add(16f, 0f);
                    tr = tile.position.cpy().add(16f, 16f);
                    br = tile.position.cpy().add(16f, -16f);
                    up = tile.position.cpy().add(0f, 16f);
                    down = tile.position.cpy().add(0f, -16f);
                    boolean touchLeft = (tilesToAdd.containsKey(left) && tilesToAdd.get(left).attrs.get("solid")) ||
                                        (tilesToAdd.containsKey(tl) && tilesToAdd.get(tl).attrs.get("solid")) ||
                                        (tilesToAdd.containsKey(bl) && tilesToAdd.get(bl).attrs.get("solid"));
                    boolean touchRight = (tilesToAdd.containsKey(right) && tilesToAdd.get(right).attrs.get("solid")) ||
                                        (tilesToAdd.containsKey(tr) && tilesToAdd.get(tr).attrs.get("solid")) ||
                                        (tilesToAdd.containsKey(br) && tilesToAdd.get(br).attrs.get("solid"));
                    boolean touchUp = (tilesToAdd.containsKey(up) && tilesToAdd.get(up).attrs.get("solid")) || 
                                        (tilesToAdd.containsKey(tl) && tilesToAdd.get(tl).attrs.get("solid")) || 
                                        (tilesToAdd.containsKey(tr) && tilesToAdd.get(tr).attrs.get("solid"));
                    boolean touchDown = (tilesToAdd.containsKey(down) && tilesToAdd.get(down).attrs.get("solid")) ||
                                        (tilesToAdd.containsKey(bl) && tilesToAdd.get(bl).attrs.get("solid")) ||
                                        (tilesToAdd.containsKey(br) && tilesToAdd.get(br).attrs.get("solid"));
                    if ((touchLeft && touchRight) || (touchUp && touchDown)) {
                        this.tilesToAdd.put(tile.position, new Tile("sand1", tile.position.cpy(), true, tile.routeBelongsTo));
                    }
                }
                else if (tile.nameUpper.equals("cactus10")) {
                    // TODO: not working.
                    tilesToAdd.put(tile.position.cpy().add(16,0), new Tile("desert4", "solid", tile.position.cpy().add(16, 0), true, tile.routeBelongsTo));
                }


                
                // TODO: should probably just move all pokemon spawning logic
                // to the route-yeeting section. That way pokemon don't spawn 
                // overtop of stuff. Would need special rules to allow
                // non-evolved pokemon to spawn.
                
                // Remove overworld pokemon that are currently inside something solid
                //  Hard to remove them when they are being placed initially, this issue
                //  happens when groups of tiles get merged together (I think).
                if (this.pokemonToAdd.containsKey(tile.position) && 
                    ((tile.attrs.get("solid") && !tile.name.contains("water")) ||
                     tile.name.contains("ledge") ||
                     // Pokemon could spawn on top of regi dungeon
                     tile.name.contains("door") ||
                     tile.nameUpper.contains("door"))) {
                    Pokemon pokemon = this.pokemonToAdd.remove(tile.position);
                }

                // Generate the pokemon mansion dungeon
                if (tile.biome.equals("deep_forest") && !GenIsland1.donePkmnMansion
                    && !this.mtnTiles.containsKey(tile.position.cpy().add(0, -16*23))
                    && !this.mtnTiles.containsKey(tile.position.cpy().add(-16*14, 0))
                    && !this.mtnTiles.containsKey(tile.position.cpy().add(16*9, -16*15))) {
                    mansionPos = tile.position.cpy();
                    if (this.radius < 100*100*(4)) {
                        mansionPos = new Vector2(0, 16*14);
                    }
                    // If overlaps ruins, retry.
                    mansionBox.setCenter(mansionPos);
                    if (mansionBox.overlaps(hitBoxes[0])) {
                        continue;
                    }
                    GenIsland1.donePkmnMansion = true;
                    complete = false;
                    // TODO: occasional bug with mansion generation when it
                    // fails to find enough endpoint tiles for statues/stairs
                    int tries = 0;
                    // only thing I know to do is retry
                    while (tries < 4) {
                        try {
                            this.generateMansion(game, mansionExteriorTiles, mansionInteriorTiles, mansionPos);
                            tilesToAdd.putAll(mansionExteriorTiles);
//                            this.interiorTilesToAdd.addAll(mansionInteriorTiles);  // TODO: remove
                            // TODO: generic algo for this.
                            for (int i=0; i < mansionInteriorTiles.size(); i++) {
                                HashMap<Vector2, Tile> currLayer = mansionInteriorTiles.get(i);
                                if (i >= this.interiorTilesToAdd.size()) {
                                    this.interiorTilesToAdd.add(currLayer);
                                    continue;
                                }
                                if (currLayer == null) {
                                    continue;
                                }
                                if (this.interiorTilesToAdd.get(i) == null) {
                                    this.interiorTilesToAdd.remove(i);
                                    this.interiorTilesToAdd.add(i, currLayer);
                                    continue;
                                }
                                for (Vector2 key : currLayer.keySet()) {
                                    this.interiorTilesToAdd.get(i).put(key, currLayer.get(key));
                                }
                            }
                            break;
                        } catch (Exception e) {
                            System.out.println("Failed to generate mansion: " + e.getMessage());
                            System.out.println("Retrying...");
                            mansionExteriorTiles.clear();
                            mansionInteriorTiles.clear();
                            tries++;
                            e.printStackTrace();
                        }
                    }
                    break;
                }
                if (tile.biome.equals("deep_forest") && tile.name.contains("green") && !GenIsland1.donePkmnMansionKey) {
                    // make sure that it's not trying to spawn the secret key above a tree (makes it really hard to find)
                    down = tile.position.cpy().add(0f, -16f);
                    if (tilesToAdd.containsKey(down) &&
//                        !tilesToAdd.get(down).name.contains("tree") &&  // TODO: test, possible remove
                        !tilesToAdd.get(down).nameUpper.contains("tree")) {
                        GenIsland1.donePkmnMansionKey = true;
//                        i = -1;  // start over. it's iterating on a copy of tilesToAdd right now.
                        complete = false;
                        tilesToAdd.put(tile.position, new Tile(tile.name, "pokemon_mansion_key", tile.position.cpy(), true, null));
                        keyLoc = tile.position;
                        break;
                    }
                }

                // Generate the regi dungeon dungeon
                // TODO: ideally nestled somewhere. not sure.
                if (tile.biome.equals("deep_forest") && !GenIsland1.doneRegiDungeon
                     // TODO: test
                     // might have been fine if cave is just on lower layer
//                    && (keyLoc == null || !keyLoc.equals(tile.position))  
                    && !this.mtnTiles.containsKey(tile.position.cpy().add(0, -16*23))
                    && !this.mtnTiles.containsKey(tile.position.cpy().add(-16*14, 0))
                    && !this.mtnTiles.containsKey(tile.position.cpy().add(16*9, -16*15))
                    && !this.mtnTiles.containsKey(tile.position.cpy().add(32, 32))
                    && !this.mtnTiles.containsKey(tile.position.cpy().add(-32, 32))
                    && !this.mtnTiles.containsKey(tile.position.cpy().add(32, -32))
                    && !this.mtnTiles.containsKey(tile.position.cpy().add(-32, -32))
                    && this.rand.nextInt(3) == 2) {
                    Vector2 regiPos = tile.position.cpy();
                    boolean collidesWithMansion = false;
                    hitBoxes[2].setCenter(regiPos);
                    for (Tile mansionTile : mansionExteriorTiles.values()) {
                        if (hitBoxes[2].contains(mansionTile.position)) {
                            collidesWithMansion = true;
                            break;
                        }
                    }
                    if (collidesWithMansion) {
                        continue;
                    }
                    GenIsland1.doneRegiDungeon = true;
                    complete = false;
                    this.generateRegiDungeon(game, regiPos);
                    break;  // if you don't do this, then dungeon gets replaced by trees.
                }
            }
        }

        // Yeet pokemon out of routes and into the overworld (using various criteria)
        for (Tile tile : this.tilesToAdd.values()) {
            if (tile.attrs.get("solid")) {
                continue;
            }
            if (tile.routeBelongsTo == null) {
                continue;
            }
            if (tile.routeBelongsTo.name.equals("")) {
                continue;
            }
            if (tile.routeBelongsTo.allowedPokemon().size() <= 0) {
                continue;
            }
            // Should be handled by "solid" check now
//            boolean headbuttable = tile.attrs.containsKey("headbuttable") && tile.attrs.get("headbuttable");
//            if (headbuttable) {
//                continue;
//            }
            // Small chance to yeet
            // If succeeded, evolve pokemon all the way (depending on some rules)
            //  -- that will result in more 3-stages than usual.
            // Not sure what to do in case of beach spawns.
            //  -- probably just evo once at random. even tho that results in starmie
            // ruins1_upper - evo at random?

            if (tile.routeBelongsTo.name.equals("desert1")) {
                // Odds probably have to be increased *4
//                if (this.rand.nextInt(2048) >= 2047) {  // TODO: remove
//                if (this.rand.nextInt(256) >= 255) {
                if (this.rand.nextInt(512) >= 511) {
                    String name = tile.routeBelongsTo.allowedPokemon().get(this.rand.nextInt(tile.routeBelongsTo.allowedPokemon().size()));
                    int level = tile.routeBelongsTo.level +Game.rand.nextInt(3);
                    // If the Pokemon is 'join-able' then nerf it's level.
                    if (name.equals("numel") ||
                        name.equals("kangaskhan") ||
                        name.equals("cubone")) {
                        level = 10;
                    }
                    Pokemon pokemon = new Pokemon(name, level, Pokemon.Generation.CRYSTAL);
                    pokemon.position = tile.position.cpy();
                    pokemon.mapTiles = game.map.overworldTiles;
                    pokemon.standingAction = pokemon.new Standing();
                    this.pokemonToAdd.put(tile.position.cpy(), pokemon);
                    if (pokemon.specie.name.equals("drapion")) {
                        pokemon.aggroPlayer = true;
                    }

                }
                continue;
            }

            // TODO: headbuttable may have messed with yeeting beach pokemon, because
            // they seem rarer now.
//            int baseChance = 506;
            int baseChance = 509;  // mountain and snow
            if (tile.routeBelongsTo.name.contains("forest")) {
                baseChance = 509;
            }
            else if (tile.routeBelongsTo.name.contains("oasis")) {
                baseChance = 460;
            }
            else if (tile.routeBelongsTo.name.contains("ruins")) {
                baseChance = 480;
            }
            else if (tile.routeBelongsTo.name.contains("beach")) {
                baseChance = 480;
            }
            else if (tile.routeBelongsTo.name.contains("graveyard")) {
                baseChance = 506;
            }

         if (this.rand.nextInt(512) >= baseChance) {
                // Grab a pokemon and evo it at random.
                // TODO: this is technically somewhat inefficient but preserves previous behavior.
                String name = tile.routeBelongsTo.allowedPokemon().get(this.rand.nextInt(tile.routeBelongsTo.allowedPokemon().size()));
                int level = tile.routeBelongsTo.level +Game.rand.nextInt(3);

                Pokemon pokemon = new Pokemon(name, level, Pokemon.Generation.CRYSTAL);
                String evolveTo = null;
                int timesEvolved = 0;
                Map<String, String> evos;
                boolean failed = false;
                while (!failed) {
                    failed = true;
//                    System.out.println(pokemon.specie.name);
                    evos = Specie.gen2Evos.get(pokemon.specie.name);
                    for (String evo : evos.keySet()) {
                        try {
                            int evoLevel = Integer.valueOf(evo);
                            if (evoLevel <= pokemon.level +(10*(timesEvolved+1)) && Game.rand.nextInt(256) >= 128) {
                                evolveTo = evos.get(evo);
                                pokemon.evolveTo(evolveTo);
                                timesEvolved++;
                                failed = false;
                                break;
                            }
                        }
                        catch (NumberFormatException e) {
                            // Item-based or other type of evo, so just do it regardless of requirement
                            if (Game.rand.nextInt(256) >= 192) {  // TODO: adjust
//                            if (Game.rand.nextInt(256) >= 128) {
                                evolveTo = evos.get(evo);
                                pokemon.evolveTo(evolveTo);
                                timesEvolved++;
                                failed = false;
                                break;
                            }
                        }
                    }
                }
                if (evolveTo != null) {
                    pokemon.level += 10*timesEvolved;
                    pokemon.exp = pokemon.gen2CalcExpForLevel(pokemon.level);
                    pokemon.calcMaxStats();
                    pokemon.currentStats.put("hp", pokemon.maxStats.get("hp"));
                }

                boolean isBaseSpecies = Pokemon.baseSpecies.get(pokemon.specie.name.toLowerCase()).equalsIgnoreCase(pokemon.specie.name);
                if (isBaseSpecies) {
                    if (pokemon.level > 10) {
                        pokemon.level = 10;
                        pokemon.exp = pokemon.gen2CalcExpForLevel(pokemon.level);
                        pokemon.calcMaxStats();
                        pokemon.currentStats.put("hp", pokemon.maxStats.get("hp"));
                    }
                }
                else if (Pokemon.dontAggro.contains(pokemon.specie.name) && pokemon.level > 30) {
                    pokemon.level = 30;
                    pokemon.exp = pokemon.gen2CalcExpForLevel(pokemon.level);
                    pokemon.calcMaxStats();
                    pokemon.currentStats.put("hp", pokemon.maxStats.get("hp"));
                }
                boolean hasEvo = !Specie.gen2Evos.get(pokemon.specie.name).isEmpty();

                // TODO: debug, remove
//                if (tile.routeBelongsTo.name.equals("sand_pit1")) {
//                    System.out.println(pokemon.nickname);
//                    System.out.println(pokemon.level);
//                    System.out.println(timesEvolved);
//                    System.out.println(isBaseSpecies);
//                    System.out.println(hasEvo);
//                }

                boolean requirementsMet = !hasEvo;
                if (tile.routeBelongsTo.name.contains("beach")) {
                    requirementsMet = hasEvo;  // want wartortle, croconaw, etc to walk around
                }
                if (tile.routeBelongsTo.name.contains("ruins")) {
                    requirementsMet = true;  // yeet everything
                }
                if (tile.routeBelongsTo.name.contains("graveyard")) {
                    requirementsMet = true;  // yeet everything
                }
                if (tile.routeBelongsTo.name.contains("mountain")) {
                    // TODO: ideally would have better yeeting rules
                    //          Maybe level-based.
                    requirementsMet = !hasEvo ||
                                      pokemon.specie.name.equals("machoke") ||
                                      pokemon.specie.name.equals("rhydon") ||
                                      pokemon.specie.name.equals("onix");
                }
                if (tile.routeBelongsTo.name.contains("snow")) {
                    requirementsMet = !hasEvo ||
                                          pokemon.specie.name.equals("sneasel") ||
                                      pokemon.specie.name.equals("piloswine");
                }

                if (requirementsMet) {
                    pokemon.position = tile.position.cpy();
                    pokemon.mapTiles = game.map.overworldTiles;
                    pokemon.standingAction = pokemon.new Standing();
                    this.pokemonToAdd.put(tile.position.cpy(), pokemon);
                }
            }
            
            
            // TODO: old behavior, remove
//            for (Pokemon pokemon : new ArrayList<Pokemon>(tile.routeBelongsTo.pokemon)) {
//                //
//                boolean isBaseSpecies = Pokemon.baseSpecies.get(pokemon.specie.name.toLowerCase()).equalsIgnoreCase(pokemon.specie.name);
//                boolean hasEvo = !Specie.gen2Evos.get(pokemon.specie.name).isEmpty();
//
//                if (tile.routeBelongsTo.name.contains("beach")) {
//                    hasEvo = !hasEvo;  // want wartortle, croconaw, etc to walk around
//                }
//                if (tile.routeBelongsTo.name.contains("ruins")) {
//                    isBaseSpecies = false;
//                    hasEvo = false;  // yeet everything
//                }
//                if (tile.routeBelongsTo.name.equals("desert1")) {
//                    if (this.rand.nextInt(2048) >= 2047) {
//                        tile.routeBelongsTo.pokemon.remove(pokemon);
//                        pokemon.position = tile.position.cpy();
//                        pokemon.mapTiles = game.map.overworldTiles;
//                        pokemon.standingAction = pokemon.new Standing();
//                        this.pokemonToAdd.put(tile.position.cpy(), pokemon);
//                        if (pokemon.specie.name.equals("drapion")) {
//                            pokemon.aggroPlayer = true;
//                        }
//                        break;
//                    }
//                    continue;
//                }
//                // Don't yeet pokemon out of the routes contained in headbutt trees.
//                boolean headbuttable = tile.attrs.containsKey("headbuttable") && tile.attrs.get("headbuttable");
//                if (!headbuttable && !isBaseSpecies && !hasEvo) {
//                    tile.routeBelongsTo.pokemon.remove(pokemon);
//                    int baseChance = 192;
//                    // TODO: headbuttable may have messed with yeeting beach pokemon, because
//                    // they seem rarer now.
//                    
//                    if (tile.routeBelongsTo.name.contains("forest")) {
//                        baseChance = 224;  // 1/8 chance if it's forest biome
//                    }
//                    if (tile.routeBelongsTo.name.contains("oasis")) {
////                        baseChance = 248;
//                        baseChance = 100;  // Had to change this after some refactors
//                    }
//                    if (tile.routeBelongsTo.name.contains("ruins")) {
//                        baseChance = 253;
//                    }
//                    // 25% chance to yeet it out to the overworld
//                    // TODO: Jynx and Piloswine are very prolific.. maybe have to
//                    // introduce special rule
//                    if (this.rand.nextInt(256) >= baseChance) {
//                        pokemon.position = tile.position.cpy();
//                        pokemon.mapTiles = game.map.overworldTiles;
//                        pokemon.standingAction = pokemon.new Standing();
//                        this.pokemonToAdd.put(tile.position.cpy(), pokemon);
//                    }
//                }
//            }
//            tile.routeBelongsTo.genPokemon(256, false);
        }


//        // TODO: this part takes a long time (6 seconds for small map, ~20 for larger map)
//        long startTime = System.currentTimeMillis();
//        // Post-processing - find all outer water tiles. Any adjacent land is an edge.
//        // TODO: if any water on land isn't in this group, make into a puddle. (not ponds tho...)
//        ArrayList<Tile> currTiles = new ArrayList<Tile>();
////        ArrayList<Tile> waterTiles = new ArrayList<Tile>();  // TODO: not sure if using or not.
//        ArrayList<Vector2> alreadyChecked = new ArrayList<Vector2>();
//        Tile currTile = this.tilesToAdd.get(minPos);
//        currTiles.add(currTile);
//        alreadyChecked.add(minPos);
//        Tile nextTile;
//        Vector2 newPos;
//        while (currTiles.size() > 0) {
//            currTile = currTiles.remove(0);
//            for (int i=-1; i < 2; i++){
//                for (int j=-1; j < 2; j++){
//                    if (i == j || i == -j || (i == 0 && j == 0)) {
//                        continue;
//                    }
//                    newPos = currTile.position.cpy().add(i*16, j*16);
//                    if (alreadyChecked.contains(newPos)) {
//                        continue;
//                    }
//                    alreadyChecked.add(newPos);
//                    nextTile = this.tilesToAdd.get(newPos);
//                    if (nextTile == null) {
//                        continue;
//                    }
//                    if (nextTile.name.contains("water")) {
////                        waterTiles.add(nextTile);
//                        currTiles.add(nextTile);
//                    }
//                    else if (!nextTile.attrs.get("solid")) {
//                        this.edges.add(nextTile);
//                    }
//                }
//            }
//        }
//        System.out.println("End post-process: " + String.valueOf(System.currentTimeMillis()-startTime));
        
        
        // TODO: potentially remove if other thing workig
//        long startTime = System.currentTimeMillis();
//        Vector2[] positions = new Vector2[]{new Vector2(-16, 0), new Vector2(16, 0),
//                                            new Vector2(0, -16), new Vector2(0, 16)};
//        Tile nextTile;
//        for (Tile tile : new ArrayList<Tile>(this.tilesToAdd.values())) {
//            for (Vector2 position : positions) {
//                nextTile = tilesToAdd.get(tile.position.cpy().add(position));
//                if (nextTile == null) {
//                    continue;
//                }
////                if (nextTile.name.contains("water") && !tile.attrs.get("solid")) {
////                    this.edges.add(tile);
////                }
//                if (nextTile.name.contains("water")) {
//                    if (!tile.attrs.get("solid")) {
//                        this.edges.add(tile);
//                    }
//                    // Issues with this. Removes pieces of pkmn mansion. also, rocks are too sparse.
////                    else if (!tile.name.contains("water")) {
////                        // Remove if solid and touching water. Doing this in an effort to remove blocked-in spawns.
////                        // Have to replace with sand b/c rock1 and tree5 both don't use nameUpper
////                        tilesToAdd.put(tile.position, new Tile("green1", tile.position, true, tile.routeBelongsTo));
////                    }
//                    break;
//                }
//            }
//        }
//        System.out.println("End post-process: " + String.valueOf(System.currentTimeMillis()-startTime));



        startTime = System.currentTimeMillis();
        Vector2[] positions = new Vector2[]{new Vector2(-16, 0), new Vector2(16, 0),
                                            new Vector2(-16, -16), new Vector2(16, 16),
                                            new Vector2(16, -16), new Vector2(-16, 16),
                                            new Vector2(0, -16), new Vector2(0, 16)};
        Tile nextTile;
        Route tempRoute = new Route("", 2);
        for (Tile tile : new ArrayList<Tile>(this.tilesToAdd.values())) {
//            if (tile.attrs.get("solid")) {  // 
//            if (!tile.name.equals("sand1")) {
//                continue;
//            }
            if (tile.name.equals("water2")) {
                continue;
            }
            for (Vector2 position : positions) {
                nextTile = tilesToAdd.get(tile.position.cpy().add(position));
                if (nextTile == null) {
                    continue;
                }
                if (nextTile.name.equals("water2")) {
//                    this.edges.add(tile);
                    // Add a 'ring' of sand around the island
                    String name = "sand3";
                    if (tile.name.contains("desert")) {
                        name = "sand3_desertEdge";
                    }

//                    Tile newTile = new Tile(name, nextTile.position.cpy(), true, tempRoute);  // TODO: test changes
                    Tile newTile = new Tile(name, nextTile.position.cpy(), true, tile.routeBelongsTo);
                    this.tilesToAdd.put(newTile.position.cpy(), newTile);
                    // Don't place edges next to desert or in oasis

                    // TODO: re-enable
                    if (!tile.name.contains("desert") &&
                        (tile.routeBelongsTo == null || (!tile.routeBelongsTo.name.equals("oasis1") &&
                                                         !tile.routeBelongsTo.name.equals("ruins1_outer")))) {
                        this.edges.add(newTile);
                    }
                }
            }
        }
        System.out.println("End post-process: " + String.valueOf(System.currentTimeMillis()-startTime));

//        this.edges.add(this.tilesToAdd.get(new Vector2(0,0)));
        
//        // place pokemon mansion
//        while (true) {
//            Vector2 edge = this.edges.get(this.rand.nextInt(this.edges.size())).position.cpy();
//            boolean touchRight = this.tilesToAdd.get(edge.cpy().add(15*16, 0)).name.contains("water");
//            boolean touchLeft = this.tilesToAdd.get(edge.cpy().add(-15*16, 0)).name.contains("water");
//            boolean touchUp = this.tilesToAdd.get(edge.cpy().add(0, 14*16)).name.contains("water");
//            boolean touchDown = this.tilesToAdd.get(edge.cpy().add(0, -14*16)).name.contains("water");
//            if (touchRight && touchLeft) {
//                continue;
//            }
//            if (touchUp && touchDown) {
//                continue;
//            }
//            Vector2 bl = edge;
//            if (touchRight) {
//                bl.add(-15*16 -32, 0);
//            }
//            else {
//                bl.add(32, 0);
//            }
//            if (touchUp) {
//                bl.add(0, -14*16 -32);
//            }
//            else {
//                bl.add(0, 32);
//            }
//            HashMap<Vector2, Tile> mansionExteriorTiles = new HashMap<Vector2, Tile>();
//            ArrayList<HashMap<Vector2, Tile>> mansionInteriorTiles = new ArrayList<HashMap<Vector2, Tile>>();
//            this.generateMansion(game, mansionExteriorTiles, mansionInteriorTiles, edge);
//            tilesToAdd.putAll(mansionExteriorTiles);
//            break;
//        }

        // TODO: remove if not using
//        for (Tile tile : new ArrayList<Tile>(this.tilesToAdd.values())) {
//            // show tops of trees
//            if (tile.name.equals("tree1")) {
//                this.tilesToAdd.remove(tile.position.cpy().add(0, 16));
//            }
//        }

        // TODO: remove
        // debug - put grass tile next to player
//        this.tilesToAdd.put(new Vector2(16, 00), new Tile("grass1", new Vector2(16, 00)));
//        this.tilesToAdd.put(new Vector2(16, 16), new Tile("grass1", new Vector2(16, 16)));
        
        ArrayList<TrainerTipsTile> signTiles = new ArrayList<TrainerTipsTile>();
        for (Tile tile : this.tilesToAdd.values()) {
            if (tile.nameUpper.equals("sign1")) {
                signTiles.add((TrainerTipsTile)tile);
            }
        }
        if (signTiles.size() > 0) {
            TrainerTipsTile tile = signTiles.get(game.map.rand.nextInt(signTiles.size()));
            tile.isUnown = true;
//            game.cam.position.set(tile.position, game.cam.position.z);
//            System.out.println(tile.position);
        }

        // TODO: revisit
//        try {
//            game.logFile = new FileWriter(game.map.id+".log");
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }

    public void AddMtnLayer(HashMap<Vector2, Tile> levelTiles,
                            HashMap<Tile, Integer> tileLevels,
                            HashMap<Vector2, Tile> mtnTiles,
                            int newLevel,
                            String name,
                            Route route) {
        for (Tile tile : new ArrayList<Tile>(levelTiles.values())) {
            Tile nextTile;
            Vector2 pos = new Vector2(tile.position.x+16, tile.position.y);
            if (!mtnTiles.containsKey(pos)) {
                nextTile = new Tile(name, "", pos, true, route);
                levelTiles.put(nextTile.position.cpy(), nextTile);
                tileLevels.put(nextTile, newLevel);
                mtnTiles.put(nextTile.position.cpy(), nextTile);
            }
            pos = new Vector2(tile.position.x-16, tile.position.y);
            if (!mtnTiles.containsKey(pos)) {
                nextTile = new Tile(name, "", pos, true, route);
                levelTiles.put(nextTile.position.cpy(), nextTile);
                tileLevels.put(nextTile, newLevel);
                mtnTiles.put(nextTile.position.cpy(), nextTile);
            }
            pos = new Vector2(tile.position.x, tile.position.y+16);
            if (!mtnTiles.containsKey(pos)) {
                nextTile = new Tile(name, "", pos, true, route);
                levelTiles.put(nextTile.position.cpy(), nextTile);
                tileLevels.put(nextTile, newLevel);
                mtnTiles.put(nextTile.position.cpy(), nextTile);
            }
            pos = new Vector2(tile.position.x, tile.position.y-16);
            if (!mtnTiles.containsKey(pos)) {
                nextTile = new Tile(name, "", pos, true, route);
                levelTiles.put(nextTile.position.cpy(), nextTile);
                tileLevels.put(nextTile, newLevel);
                mtnTiles.put(nextTile.position.cpy(), nextTile);
            }
            pos = new Vector2(tile.position.x+16, tile.position.y+16);
            if (!mtnTiles.containsKey(pos)) {
                nextTile = new Tile(name, "", pos, true, route);
                levelTiles.put(nextTile.position.cpy(), nextTile);
                tileLevels.put(nextTile, newLevel);
                mtnTiles.put(nextTile.position.cpy(), nextTile);
            }
            pos = new Vector2(tile.position.x-16, tile.position.y+16);
            if (!mtnTiles.containsKey(pos)) {
                nextTile = new Tile(name, "", pos, true, route);
                levelTiles.put(nextTile.position.cpy(), nextTile);
                tileLevels.put(nextTile, newLevel);
                mtnTiles.put(nextTile.position.cpy(), nextTile);
            }
            pos = new Vector2(tile.position.x-16, tile.position.y-16);
            if (!mtnTiles.containsKey(pos)) {
                nextTile = new Tile(name, "", pos, true, route);
                levelTiles.put(nextTile.position.cpy(), nextTile);
                tileLevels.put(nextTile, newLevel);
                mtnTiles.put(nextTile.position.cpy(), nextTile);
            }
            pos = new Vector2(tile.position.x+16, tile.position.y-16);
            if (!mtnTiles.containsKey(pos)) {
                nextTile = new Tile(name, "", pos, true, route);
                levelTiles.put(nextTile.position.cpy(), nextTile);
                tileLevels.put(nextTile, newLevel);
                mtnTiles.put(nextTile.position.cpy(), nextTile);
            }
            levelTiles.remove(tile.position);
        }
    }

    public void ApplyBlotch(Game game, String type, Tile originTile, int maxDist, HashMap<Vector2, Tile> tilesToAdd) {
        ApplyBlotch(game, type, originTile, maxDist, tilesToAdd, 0, true);
    }

    public void ApplyBlotch(Game game, String type, Tile originTile, int maxDist, HashMap<Vector2, Tile> tilesToAdd, int isMaze, boolean doNext) {
        ApplyBlotch(game, type, originTile, maxDist, tilesToAdd, isMaze, doNext, null);
    }
    
    public static int numSandPits = 0;

    public void ApplyBlotch(Game game, String type, Tile originTile, int maxDist, HashMap<Vector2, Tile> tilesToAdd, int isMaze, boolean doNext, Route currRoute) {
        HashMap<Vector2, Tile> edgeTiles = new HashMap<Vector2, Tile>();
        ArrayList<Tile> prevTiles = new ArrayList<Tile>();
        Tile prevTile = originTile;
        edgeTiles.put(originTile.position.cpy(), originTile);
        HashMap<Vector2, Tile> grassTiles = new HashMap<Vector2, Tile>();
        // whether or not this blotch has a maze in the middle
//        int isMaze = 1; // this.rand.nextInt(2);
        isMaze = 1; // this.rand.nextInt(2);
        HashMap<Vector2, Tile> forestMazeTiles = new HashMap<Vector2, Tile>();
        ArrayList<Vector2> fenceSpawns = new ArrayList<Vector2>();

        //      int newSize = this.rand.nextInt(maxDist + (int)(maxDist/1.5f));  // size varies a lot, but looks good
          // has a tendency to snake, but looks good
        //  int newSize = maxDist - this.rand.nextInt((int)Math.ceil(maxDist/4f)); // was doing
        int newSize = maxDist - this.rand.nextInt((int)Math.ceil(maxDist/4f)) - maxDist/4;
        if (type.equals("sand_pit1")) {
            newSize = maxDist;
        }
        // TODO: this will have to be done in post like the mansion, etc.
        // Picking a random spot should be okay, tiles are concetrated near center (?)
        int doneOasis = maxDist;  // TODO: remove
        int numDarms = 10;  // TODO: would like this to only apply to desert ruins
//        int invulnTimer = 0;  // TODO: remove

        Route beachRoute = null;
        if (type.equals("island")) {
            beachRoute = new Route("", 2);
//            beachRoute = new Route("beach1", 3);  // had no visible effect
        }

        while (!edgeTiles.isEmpty()) {
            for (Tile tile : new ArrayList<Tile>(edgeTiles.values())) {
                for (Vector2 edge : new Vector2[]{tile.position.cpy().add(-16f, 0f),
                                                  tile.position.cpy().add(16f, 0f),
                                                  tile.position.cpy().add(0f, 16f),
                                                  tile.position.cpy().add(0f, -16f)}) {
                    float distance = edge.dst(originTile.position);
                    if (!tilesToAdd.containsKey(edge)) {
                        int putTile = this.rand.nextInt(maxDist) + (int)distance;
    //                    System.out.println(putTile);
                        // ((int)distance < 7*maxDist/16 will ensure tiles near middle always get added, ie
                        // that there aren't any random gaps in the middle of an island.
                        boolean shouldPut = ((int)distance < 7*maxDist/16);
                        if (type.equals("desert")) {
                            // Prevents desert from bleeding 'through' mountains
                            // TODO: test that this was causing issue
//                            if (invulnTimer >= 120 && this.mtnTiles.containsKey(edge)) {
//                                continue;
//                            }
//                            if (invulnTimer < 120) {
//                                invulnTimer++;
//                            }
                            // Looked nice but tiles from other biomes were 'bleeding' into the
                            // desert too much.
//                            shouldPut = ((int)distance < 8*maxDist/16);
                            shouldPut = ((int)distance < 10*maxDist/16);
                        }
                        else if (type.equals("sand_pit1")) {
                            shouldPut = ((int)distance < 10*maxDist/16);
                        }
                        else if (type.equals("graveyard1")) {
                            shouldPut = ((int)distance < 10*maxDist/16);
                        }

                        // TODO: remove
//                        if (putTile < maxDist) {  // TODO: remove
//
//                            // TODO: perf test, remove
//                            Tile aTile = new Tile("desert4", edge);
//                            aTile.biome = "desert";
//                            tilesToAdd.put(aTile.position.cpy(), aTile);
//                            edgeTiles.put(aTile.position.cpy(), aTile);
//                        }
//                        else if (putTile < maxDist && isMaze != 1) {  // always fails
                        //

                        if (putTile < maxDist || shouldPut) {

                            if (type.equals("desert")) {
//                                Tile newTile = new Tile("desert4", edge);
                                Tile newTile = new Tile("desert4", edge, true, currRoute);
                                newTile.biome = "desert";

                                if (this.rand.nextInt(maxDist) < 6) {
                                    int nextSize = 200;
                                    ApplyBlotch(game, "desert_cacti2", newTile, nextSize, grassTiles, 0, false, currRoute);

                                    // 
                                    nextSize = this.rand.nextInt(40) + 20;  //30;
                                    HashMap<Vector2, Tile> newTiles = new HashMap<Vector2, Tile>();
                                    ApplyBlotch(game, "desert_cacti1", newTile, nextSize, newTiles, 0, false, currRoute);
                                    grassTiles.putAll(newTiles);
                                }
                                else if (this.rand.nextInt(maxDist) < 3) {
                                    int nextSize = 30 +this.rand.nextInt(20);
                                    HashMap<Vector2, Tile> newTiles = new HashMap<Vector2, Tile>();
                                    ApplyBlotch(game, "desert_cacti3", newTile, nextSize, newTiles, 0, false, currRoute);
                                    grassTiles.putAll(newTiles);
                                }
                                else if (this.rand.nextInt(maxDist) < 3) {
                                    newTile = new Tile("desert4", "cactus10", edge, true, currRoute);
                                }
//                                else if (this.rand.nextInt(maxDist) < 1) {
                                else if (this.rand.nextInt(maxDist) < 3) {
                                    newTile = new Tile("desert4", "desert4_cracked", edge, true, currRoute);
                                }

                                // TODO: remove
                                // TODO: not sure if should limit to 1. definitely want minimum of one
//                                else if (distance > maxDist/8 && doneOasis > 0 && this.rand.nextInt(doneOasis) < 1) {
//                                    
////                                    int nextSize = 300 + this.rand.nextInt(100);
//                                    int nextSize = 250 + this.rand.nextInt(50);
//                                    HashMap<Vector2, Tile> newTiles = new HashMap<Vector2, Tile>();
//                                    ApplyBlotch(game, "oasis1", newTile, nextSize, newTiles, 0, true, currRoute);
//                                    HashMap<Vector2, Tile> newTiles2 = new HashMap<Vector2, Tile>();
//                                    nextSize = nextSize/2;
//                                    ApplyBlotch(game, "pond1", newTile, nextSize, newTiles2, 0, true, currRoute);
//                                    newTiles.putAll(newTiles2);
//                                    
//
//                                    for (Tile tile2 : new ArrayList<Tile>(newTiles.values())) {
//                                        if (tile2.nameUpper.equals("aloe_large1")) {
//                                            newTiles.put(tile2.position.cpy().add(16, 0), new Tile("green1", "solid", tile2.position.cpy().add(16, 0), true, tile2.routeBelongsTo));
//                                            newTiles.put(tile2.position.cpy().add(0, 16), new Tile("green1", "solid", tile2.position.cpy().add(0, 16), true, tile2.routeBelongsTo));
//                                            newTiles.put(tile2.position.cpy().add(16,16), new Tile("green1", "solid", tile2.position.cpy().add(16,16), true, tile2.routeBelongsTo));
//                                        }
//                                    }
//
//                                    grassTiles.putAll(newTiles);
//                                    tilesToAdd.putAll(newTiles);
//                                    doneOasis= 0;
//                                }
                                // TODO: remove
//                                else if (distance > maxDist/8 && doneRuins > 0 && this.rand.nextInt(doneRuins) < 1) {
//                                    int nextSize = 150;
//                                    HashMap<Vector2, Tile> newTiles = new HashMap<Vector2, Tile>();
//                                    ApplyBlotch(game, "ruins1_upper", newTile, nextSize, newTiles, 0, false, currRoute);
//                                    grassTiles.putAll(newTiles);
//                                    tilesToAdd.putAll(newTiles);
//                                    doneRuins= 0;
//                                }
                                
//                                else if (this.rand.nextInt(10) > 8) {
                                else if (this.rand.nextInt(maxDist) < 3) {
                                    GenIsland1.numSandPits = 0;
                                    int nextSize = 45; // +this.rand.nextInt(20);
                                    newTile = new Tile("desert2_trapinch_spawn", edge, true, currRoute);
                                    HashMap<Vector2, Tile> newTiles = new HashMap<Vector2, Tile>();
                                    Route tempRoute = new Route("sand_pit1", 44);
                                    ApplyBlotch(game, "sand_pit1", newTile, nextSize, newTiles, 0, true, tempRoute);
                                    grassTiles.putAll(newTiles);
                                }

                                tilesToAdd.put(newTile.position.cpy(), newTile);
                                edgeTiles.put(newTile.position.cpy(), newTile);
                                if (GenIsland1.doneDesert < 2) {
                                    GenIsland1.doneDesert = 2;
                                }
                                if (doneOasis > 0) {
                                    doneOasis--;
                                }
                                // TODO: remove
//                                if (doneRuins > 0) {
//                                    doneRuins--;
//                                }
                            }

                            // Trees in middle, grass near middle, sand and rock on edges
                            else if (type.equals("island")) {
                                Tile newTile = new Tile("sand1", edge, true, beachRoute);
                                int isRock = this.rand.nextInt(maxDist) + (int)distance;
                                if (isRock > maxDist + maxDist/2) {
//                                    Vector2 left = edge.cpy().add(-16f, 0f);
//                                    Vector2 tl = edge.cpy().add(-16f, 16f);
//                                    Vector2 bl = edge.cpy().add(-16f, -16f);
//                                    Vector2 right = edge.cpy().add(16f, 0f);
//                                    Vector2 tr = edge.cpy().add(16f, 16f);
//                                    Vector2 br = edge.cpy().add(16f, -16f);
//                                    Vector2 up = edge.cpy().add(0f, 16f);
//                                    Vector2 down = edge.cpy().add(0f, -16f);
//                                    
//                                    boolean touchLeft = (tilesToAdd.containsKey(left) && tilesToAdd.get(left).attrs.get("solid")) ||
//                                                        (tilesToAdd.containsKey(tl) && tilesToAdd.get(tl).attrs.get("solid")) ||
//                                                        (tilesToAdd.containsKey(bl) && tilesToAdd.get(bl).attrs.get("solid"));
//                                    boolean touchRight = (tilesToAdd.containsKey(right) && tilesToAdd.get(right).attrs.get("solid")) ||
//                                                         (tilesToAdd.containsKey(tr) && tilesToAdd.get(tr).attrs.get("solid")) ||
//                                                         (tilesToAdd.containsKey(br) && tilesToAdd.get(br).attrs.get("solid"));
//                                    boolean touchUp = (tilesToAdd.containsKey(up) && tilesToAdd.get(up).attrs.get("solid")) || 
//                                                      (tilesToAdd.containsKey(tl) && tilesToAdd.get(tl).attrs.get("solid")) || 
//                                                      (tilesToAdd.containsKey(tr) && tilesToAdd.get(tr).attrs.get("solid"));
//                                    boolean touchDown = (tilesToAdd.containsKey(down) && tilesToAdd.get(down).attrs.get("solid")) ||
//                                                        (tilesToAdd.containsKey(bl) && tilesToAdd.get(bl).attrs.get("solid")) ||
//                                                        (tilesToAdd.containsKey(br) && tilesToAdd.get(br).attrs.get("solid"));
//                                    if (!(touchLeft && touchRight) && !(touchUp && touchDown)) {
//                                        newTile = new Tile("rock1", edge);
//                                    }
                                    Route tempRoute = null;
                                    int randInt = this.rand.nextInt(3);
                                    if (randInt == 2) {
                                        tempRoute = new Route("", 11);
                                        String[] pokemon = new String[]{"shellder", "krabby", "staryu", "dwebble"};
                                        randInt = this.rand.nextInt(pokemon.length);
                                        tempRoute.storedPokemon.add(new Pokemon(pokemon[randInt], 11+this.rand.nextInt(3)));
                                        // TODO: remove
//                                        tempRoute.allowedPokemon.add(pokemon[randInt]);
//                                        tempRoute.genPokemon(256);
//                                        tempRoute.pokemon.add(new Pokemon(pokemon[randInt], 20+this.rand.nextInt(4), Pokemon.Generation.CRYSTAL));
                                    }
                                    newTile = new Tile("rock1", edge, true, tempRoute);
                                }
                                // grass isn't as solid as i want
                                int isGrass = this.rand.nextInt(maxDist/8) + (int)distance;
                                if (isGrass < 1*maxDist/2) {
                                    if ((int)distance < 3*maxDist/8) {
                                        // TODO: remove if unused
//                                        if (this.rand.nextInt(8) == 0) {
//                                            if (this.rand.nextInt(2) == 0) {
//                                                newTile = new Tile("green6", edge);
//                                            }
//                                            else {
//                                                newTile = new Tile("green7", edge);
//                                            }
//                                        }
//                                        if (this.rand.nextInt(12) == 0) {
//                                            newTile = new Tile("green5", edge);
//                                        }
                                        // TODO: this is one of the few uses of currRoute that I see
                                        // Would be nice to replace this route with a route that's
                                        // actually used and level-adjusted.
                                        if (this.rand.nextInt(24) == 0) {
                                            newTile = new Tile("flower4", edge, true, currRoute);
                                        }
                                        else {
                                            newTile = new Tile("green1", edge, true, currRoute);
                                        }
                                    }
                                    else {
                                        newTile = new Tile("green1", edge, true, currRoute);
                                    }
                                }
                                else if (isRock <= maxDist + maxDist/2 && isGrass < maxDist - maxDist/4 && this.rand.nextInt(40) == 0) {
                                    // Palm tree has it's own route with exeggcute or exeggutor in it.
                                    Route tempRoute = new Route("", 11);
                                    int randInt = this.rand.nextInt(5);
                                    if (randInt == 4) {
                                        tempRoute.storedPokemon.add(new Pokemon("Exeggutor", 10+this.rand.nextInt(4)));
                                    }
                                    else if (randInt > 1) {
                                        tempRoute.storedPokemon.add(new Pokemon("Exeggcute", 10+this.rand.nextInt(4)));
                                    }
                                    // TODO: probably will just make puddles spawn pokemon, not sure
                                    if (this.rand.nextInt(2) == 0) {
                                        // TODO: remove this comment. was 5 ; changed to 3 as per feedback
                                        ApplyBlotch(game, "grass_sand", newTile, 35, grassTiles, 0, false, new Route("beach1", 3));
                                    }
                                    // small chance this becomes a trainer-tips sign
                                    // TODO: they are a little too prolific on large maps
                                    if (this.rand.nextInt(10) == 0) {
                                        newTile = new TrainerTipsTile(edge, tempRoute, GenIsland1.unownCounter++ % 64 == 0, "");
                                    }
                                    else {
                                        newTile = new Tile("tree5", edge, true, tempRoute);
                                    }
                                }
                                // spawn pokeball items
                                else if (isRock <= maxDist + maxDist/2 && isGrass < maxDist - maxDist/4 && this.rand.nextInt(200) == 0) {
                                    newTile = new Tile(newTile.name, "pokeball1", edge, true, currRoute);
                                }
                                // Spawn a friendly overworld pokemon
                                else if (isRock <= maxDist + maxDist/2 && isGrass < maxDist - maxDist/4 && this.rand.nextInt(450) == 0) {
                                    int centerLevel;
                                    if (maxDist > 300) {
                                        centerLevel = 30;
                                    }
                                    else {
                                        centerLevel = 15;
                                    }
                                    int level = (int)(centerLevel*(1-(distance / (2*maxDist/5))));
                                    if (level < 4) {
                                        level = 4;
                                    }
                                    Route blotchRoute;
                                    if ((int)distance < 2*maxDist/8 && maxDist > 600) {
                                        blotchRoute = new Route("deep_forest", level);
                                    }
                                    else if ((int)distance < 3*maxDist/8) {
                                        blotchRoute = new Route("forest1", level);
                                    }
                                    else {
                                        blotchRoute = new Route("savanna1", level);
                                    }
//                                    Pokemon pokemon = blotchRoute.pokemon.remove(0);  // TODO: previous behavior, remove
                                    Pokemon pokemon = new Pokemon(blotchRoute.allowedPokemon().get(this.rand.nextInt(blotchRoute.allowedPokemon().size())),
                                                                  blotchRoute.level +this.rand.nextInt(3), Pokemon.Generation.CRYSTAL);
                                    pokemon.position = edge.cpy();
//                                    pokemon.mapTiles = game.map.tiles;  // TODO: test
                                    pokemon.mapTiles = game.map.overworldTiles;
                                    //pokemon.name.toLowerCase().equals("tauros") || 
                                    if (pokemon.specie.name.toLowerCase().equals("ekans") ||
                                        pokemon.specie.name.toLowerCase().equals("pidgey") ||
                                        pokemon.specie.name.toLowerCase().equals("spearow") ||
                                        pokemon.specie.name.toLowerCase().equals("rattata")) {
                                        pokemon.happiness = 0;
                                    }
                                    pokemon.standingAction = pokemon.new Standing();
//                                    game.insertAction(pokemon.standingAction);
                                    this.pokemonToAdd.put(pokemon.position.cpy(), pokemon);

                                    // Add mates for some pokemon (at same position)
                                    if (GenForest2.mates2.containsKey(pokemon.specie.name)) {
                                        String oppGender = pokemon.gender.equals("male") ? "female" : "male";
                                        Pokemon mate = new Pokemon(GenForest2.mates2.get(pokemon.specie.name), pokemon.level);
                                        mate.gender = oppGender;
                                        mate.position = pokemon.position.cpy().add(16, 0);
                                        mate.mapTiles = game.map.overworldTiles;
                                        mate.standingAction = mate.new Standing();
                                        this.pokemonToAdd.put(mate.position.cpy(), mate);
                                    }
                                }
                                // Chance to spawn poke-parents in deep forest
                                if (distance < maxDist/4 && maxDist > 300 && this.rand.nextInt(750) == 0) {  //800
                                    int centerLevel;
                                    if (maxDist > 300) {
                                        centerLevel = 50;
                                    }
                                    else {
                                        centerLevel = 15;
                                    }
                                    int level = (int)(centerLevel*(1-(distance / (2*maxDist/5))));
                                    if (level < 4) {
                                        level = 4;
                                    }
                                    String name = new ArrayList<String>(GenForest2.mates.keySet()).get(Game.rand.nextInt(GenForest2.mates.keySet().size()));
                                    Pokemon pokemon = new Pokemon(name, level);
                                    pokemon.position = edge.cpy();
                                    pokemon.mapTiles = game.map.overworldTiles;
                                    pokemon.standingAction = pokemon.new Standing();
                                    this.pokemonToAdd.put(pokemon.position.cpy(), pokemon);
                                    String oppGender = pokemon.gender.equals("male") ? "female" : "male";
                                    Pokemon mate = new Pokemon(GenForest2.mates.get(pokemon.specie.name), pokemon.level);
                                    mate.gender = oppGender;
                                    mate.position = pokemon.position.cpy().add(16, 0);
                                    mate.mapTiles = game.map.overworldTiles;
                                    mate.standingAction = mate.new Standing();
                                    this.pokemonToAdd.put(mate.position.cpy(), mate);
                                }
                                if (isMaze == 0) {
                                    int isTree = this.rand.nextInt(maxDist/4) + (int)distance;
                                    if (isTree < 1*maxDist/3 && newTile.position.y % 32 == 0) {
                                            newTile = new Tile("tree2", edge);
                                    }
                                }
                                else if ((int)distance + this.rand.nextInt(maxDist/16 +1) < 3*maxDist/8) {  // note: remove randint part for smoother forest edge
                                    forestMazeTiles.put(newTile.position.cpy(), newTile);
                                }
                                int grassBlotchHere = this.rand.nextInt(maxDist/4) + (int)distance;
                                int grassBlotchHere2 = this.rand.nextInt(maxDist);
                                int grassBlotchHere3 = this.rand.nextInt(maxDist);
                                if ( //(int)distance > 1*maxDist/4 &&  // TODO: what was this for? test.
                                     // TODO: test. was causing grass in middle of water
                                    (int)distance < maxDist - maxDist/5  
//                                    (int)distance < 3*maxDist/5
                                    && grassBlotchHere < 3*maxDist/7
                                    && grassBlotchHere2 < maxDist/4
                                    && (maxDist < 300 || grassBlotchHere3 < maxDist/8)) {
                                    int nextSize = (int)Math.ceil(maxDist/6f);
                                    if (maxDist > 300) {
                                        nextSize = (int)Math.ceil(maxDist/12f);
                                    }
                                    if (maxDist > 1000) {
//                                        nextSize = (int)Math.ceil(maxDist/24f);
                                        nextSize = (int)Math.ceil(maxDist/32f);
                                    }
                                    // TODO: debug, remove
//                                    System.out.println(newTile.position.dst(0, 0));
//                                    System.out.println(this.radius);
//                                    int centerLevel = (int)(60*(1-(newTile.position.dst(this.origin) / (this.radius/10))));
                                    int centerLevel;
                                    if (maxDist > 300) {
                                        centerLevel = 30;
                                    }
                                    else {
                                        centerLevel = 15;
                                    }
                                    int level = (int)(centerLevel*(1-(distance / (2*maxDist/5))));
                                    if (level < 4) {
                                        level = 4;
                                    }
//                                    System.out.println("origin dist: "+String.valueOf(newTile.position.dst(this.origin)));
//                                    System.out.println("dist: "+String.valueOf(distance));
//                                    System.out.println("radius/10: "+String.valueOf(this.radius/10));
//                                    System.out.println("maxDist/2: "+String.valueOf(maxDist/2));
//                                    System.out.println("centerLevel: "+String.valueOf(centerLevel));
//                                    System.out.println("level: "+String.valueOf(level));
                                    // TODO: test without
                                    // If this is right next to the shore, 'nerf' the level
//                                    if (newSize <= 0) {
//                                        level -= 20;
//                                        if (level <= 10) {
//                                            level = 10;
//                                        }
//                                    }
//                                    System.out.println((1-(newTile.position.dst(this.origin) / (this.radius/12))));
                                    Route blotchRoute;
                                    if ((int)distance < 2*maxDist/8 && maxDist > 600) {
                                        blotchRoute = new Route("deep_forest", level);
                                    }
                                    else if ((int)distance < 3*maxDist/8) {
                                        blotchRoute = new Route("forest1", level);
                                    }
                                    else {
                                        blotchRoute = new Route("savanna1", level);
                                    }
                                    ApplyBlotch(game, "grass", newTile, nextSize, grassTiles, 0, false, blotchRoute);
                                }
                                if ((int)distance < 2*maxDist/8 && maxDist > 300) {
                                    newTile.biome = "deep_forest";
                                }
                                tilesToAdd.put(newTile.position.cpy(), newTile);
                                edgeTiles.put(newTile.position.cpy(), newTile);
                            // grass blotch
                            } else if (type.equals("grass")) {
                                String name = "grass2";
                                if (currRoute.name.contains("savanna") || currRoute.name.contains("oasis")) {
                                    name = "grass4";
                                }
                                Tile newTile = new Tile(name, edge, false, currRoute);
                                if ((int)distance < 2*maxDist/8 && maxDist > 300) {
                                    newTile.biome = "deep_forest";
                                }
                                tilesToAdd.put(newTile.position.cpy(), newTile);
                                edgeTiles.put(newTile.position.cpy(), newTile);
                            }
                            else if (type.equals("grass_sand")) {
                                Tile newTile = new Tile("grass_sand1", edge, false, currRoute);
                                tilesToAdd.put(newTile.position.cpy(), newTile);
                                edgeTiles.put(newTile.position.cpy(), newTile);
                            }
                            else if (type.equals("grass_desert1")) {
                                Tile newTile = new Tile("desert4", edge);
                                if (distance < 2*maxDist/3) {
                                    newTile = new Tile("grass_sand3", edge, true, currRoute);
                                }
                                tilesToAdd.put(newTile.position.cpy(), newTile);
                                edgeTiles.put(newTile.position.cpy(), newTile);
                            }
                            else if (type.contains("grass_")) {
                                Tile newTile = new Tile(type, edge, false, currRoute);
                                tilesToAdd.put(newTile.position.cpy(), newTile);
                                edgeTiles.put(newTile.position.cpy(), newTile);
                            }
                            else if (type.equals("sand_pit1")) {
                                Tile newTile = new Tile("desert4", edge);
                                if (distance < 2*maxDist/3) {
                                    newTile = new Tile("desert2", edge, true, currRoute);
                                    // Chance to put trapinch in this tile
                                    if (this.rand.nextInt(256) < 8) {  // 32
                                        this.trapinchSpawns.add(edge.cpy());
                                    }
                                }
                                tilesToAdd.put(newTile.position.cpy(), newTile);
                                edgeTiles.put(newTile.position.cpy(), newTile);
                            }
                            else if (type.equals("ruins1_upper")) {
                                Tile newTile = new Tile("desert4", edge);
                                if (distance < 2*maxDist/5) {
                                    int putTile2 = this.rand.nextInt(maxDist) + (int)distance;
                                    if (putTile2 < maxDist) {
                                        newTile = new Tile("ruins1", edge, true, currRoute);
//                                        if (this.rand.nextInt(20) > 12 && edge.y % 32 == 0 && edge.x % 32 == 0) { 
//                                            Pokemon darmanitan = new Pokemon("darmanitan", 44, Pokemon.Generation.CRYSTAL);
//                                            darmanitan.position = newTile.position.cpy();
//                                            darmanitan.mapTiles = game.map.overworldTiles;
//                                            darmanitan.standingAction = darmanitan.new Standing();
//                                            this.pokemonToAdd.put(darmanitan.position.cpy(), darmanitan);
//                                        }
                                        
                                    }
                                    else if (edge.y % 32 == 0 && edge.x % 64 == 0) {  // this.rand.nextInt(20) > 1 && 
                                        newTile = new Tile("ruins1_pillar1", edge, true, currRoute);
                                    }
                                }
                                else if (distance < 3*maxDist/5) {
                                    if (this.rand.nextInt(20) > 10 && edge.y % 32 == 0 && edge.x % 64 == 0) {  //
                                        if (this.rand.nextInt(20) > 9) {
                                            newTile = new Tile("ruins1_pillar1_broken", edge, true, currRoute);
                                        }
                                        else {
                                            newTile = new Tile("ruins1_pillar1", edge, true, currRoute);
                                        }
                                    }
                                    else if (this.rand.nextInt(20) > numDarms && edge.y % 32 == 0 && edge.x % 32 == 0) {  //
                                        // Darm?
                                        // TODO: try
                                        Pokemon darmanitan = new Pokemon("darmanitanzen", 35, Pokemon.Generation.CRYSTAL);
                                        darmanitan.position = newTile.position.cpy();
                                        darmanitan.mapTiles = game.map.overworldTiles;
                                        darmanitan.standingAction = darmanitan.new Standing();
                                        this.pokemonToAdd.put(darmanitan.position.cpy(), darmanitan);
                                        numDarms++;
                                    }
                                }
                                else {
                                    if (this.rand.nextInt(10) > 4) {
                                        
                                        int nextSize = 95;
                                        newTile = new Tile("grass_sand3", edge, true, currRoute);
//                                        if (this.rand.nextInt(10) > 3) {
//                                            newTile = new Tile("ruins1_pillar1", edge, true, currRoute);
//                                        }
                                        HashMap<Vector2, Tile> newTiles = new HashMap<Vector2, Tile>();
                                        ApplyBlotch(game, "grass_desert1", newTile, nextSize, newTiles, 0, true, currRoute);
                                        grassTiles.putAll(newTiles);
//                                        grassTiles.put(newTile.position.cpy(), newTile);

                                        if (this.rand.nextInt(2) > 0) {
                                            newTile = new Tile("grass_sand3", edge, true, currRoute);
                                            newTiles = new HashMap<Vector2, Tile>();
                                            if (this.rand.nextInt(2) > 0) {
                                                nextSize = this.rand.nextInt(40) + 20;
                                                ApplyBlotch(game, "desert_cacti1", newTile, nextSize, newTiles, 0, true, currRoute);
                                            }
                                            else {
                                                nextSize = 30 +this.rand.nextInt(20);
                                                ApplyBlotch(game, "desert_cacti3", newTile, nextSize, newTiles, 0, false, currRoute);
                                            }
                                            grassTiles.putAll(newTiles);
                                        }
                                    }
                                    
                                }
                                tilesToAdd.put(newTile.position.cpy(), newTile);
                                edgeTiles.put(newTile.position.cpy(), newTile);
                            }
                            else if (type.equals("ruins1_upper2")) {
                                Tile newTile = new Tile("desert4", edge);
                                if (this.rand.nextInt(3) > 0) {
                                    newTile = new Tile("ruins1_NSEW", edge, true, currRoute);
                                }
                                tilesToAdd.put(newTile.position.cpy(), newTile);
                                edgeTiles.put(newTile.position.cpy(), newTile);
                            }
                            else if (type.equals("pond1")) {
                                Tile newTile = new Tile("water2", edge, true, currRoute);
                                tilesToAdd.put(newTile.position.cpy(), newTile);
                                edgeTiles.put(newTile.position.cpy(), newTile);
                            }
                            else if (type.equals("oasis1")) {

                                // TODO: remove
//                                Route blotchRoute = new Route("oasis1", 30);
//                                blotchRoute.genPokemon(0, false);
                                
                                Tile newTile = new Tile("sand1", "", edge, true, currRoute);
                                int isGrass = this.rand.nextInt(maxDist/8) + (int)distance;
                                if (isGrass < 1*maxDist/2) {
                                    if ((int)distance < 3*maxDist/8) {
                                        if (this.rand.nextInt(24) == 0) {
                                            newTile = new Tile("flower4", edge, true, currRoute);
                                        }
                                        else {
                                            newTile = new Tile("green1", edge, true, currRoute);
                                        }
                                    }
                                    else {
                                        newTile = new Tile("green1", edge, true, currRoute);
                                    }
                                }
//                                if (this.rand.nextInt(110) < 1) {
                                boolean putGrass = this.rand.nextInt(90) < 1;
                                if (distance < maxDist/2) {
                                    putGrass = this.rand.nextInt(40) < 1;
                                }
                                boolean putRock = false;
                                if (distance > maxDist/2) {
                                    putRock = this.rand.nextInt(10) < 1;
                                }
                                
                                if (putGrass) {
                                    int nextSize = 40;
                                    // New route technically not required however I liked the
                                    // overworld diversity created by having a separate route here.
                                    Route blotchRoute = new Route("oasis1", 30);
                                    ApplyBlotch(game, "grass", newTile, nextSize, grassTiles, 0, false, blotchRoute);
                                }
                                else if (this.rand.nextInt(10) < 1) {
                                    Route tempRoute = new Route("", 22);
                                    tempRoute.name = "oasis1";
                                    int randInt = this.rand.nextInt(2);
                                    if (randInt == 0) {
                                        String[] pokemon = new String[]{"aexeggutor"};
                                        randInt = this.rand.nextInt(pokemon.length);
                                        tempRoute.storedPokemon.add(new Pokemon(pokemon[randInt],
                                                                                tempRoute.level,
                                                                                Pokemon.Generation.CRYSTAL));
                                    }
                                    newTile = new Tile("tree5", edge, true, tempRoute);
                                }
                                else if (this.rand.nextInt(110) < 1) {
                                    newTile = new Tile("green1", "aloe_large1", edge, true, currRoute);
                                }
                                else if (putRock) {
                                    Route tempRoute = new Route("", 22);
                                    tempRoute.name = "oasis1";
                                    int randInt = this.rand.nextInt(2);
                                    if (randInt == 0) {
                                        String[] pokemon = new String[]{"shellder", "krabby", "staryu", "dwebble"};
                                        randInt = this.rand.nextInt(pokemon.length);
                                        tempRoute.storedPokemon.add(new Pokemon(pokemon[randInt],
                                                                                tempRoute.level,
                                                                                Pokemon.Generation.CRYSTAL));
                                    }
                                    newTile = new Tile(newTile.name, "rock1_color", edge.cpy(), true, tempRoute);
                                }
                                tilesToAdd.put(newTile.position.cpy(), newTile);
                                edgeTiles.put(newTile.position.cpy(), newTile);
                            }
                            // Cactus and/or deadbrush
                            else if (type.equals("desert_cacti1")) {
//                                Tile newTile = new Tile("sand1", edge);
                                Tile newTile = new Tile("desert4", edge);
                                boolean doCactus = true;
                                if (distance > 0) {
                                    doCactus = this.rand.nextInt((int)distance) < maxDist/8;
                                }
                                if (doCactus) {
                                    Route tempRoute = null;
                                    int randInt = this.rand.nextInt(2);
                                    if (randInt == 0) {
                                        String[] pokemon = new String[]{"maractus"};
                                        randInt = this.rand.nextInt(pokemon.length);
                                        tempRoute = new Route("", 22);
                                        tempRoute.storedPokemon.add(new Pokemon(pokemon[randInt], tempRoute.level,
                                                                                Pokemon.Generation.CRYSTAL));
                                    }
                                    if (distance < maxDist/4) {  //  && this.rand.nextInt(2) == 1
                                        newTile = new Tile("cactus2", edge, true, tempRoute);
                                    }
                                    // Don't place small cactus right above the center, looks weird.
                                    else if (!(newTile.position.x == originTile.position.x &&
                                               newTile.position.y == originTile.position.y+16)) {
                                        newTile = new Tile("cactus1", edge, true, tempRoute);
                                    }
                                }
                                tilesToAdd.put(newTile.position.cpy(), newTile);
                                edgeTiles.put(newTile.position.cpy(), newTile);
                            }
                            else if (type.equals("desert_cacti3")) {
                                Tile newTile = new Tile("desert4", edge, true, currRoute);
                                boolean doCactus = true;
                                if (distance > 0) {
                                    doCactus = this.rand.nextInt((int)distance) < maxDist/8;
                                }
                                if (doCactus) {
                                    Route tempRoute = null;
                                    if (distance < maxDist/4) {
                                        newTile = new Tile("desert6", "cactus9", edge, true, tempRoute);
                                    }
                                    else {
                                        newTile = new Tile("desert6", "cactus8", edge, true, tempRoute);
                                    }
                                }
                                tilesToAdd.put(newTile.position.cpy(), newTile);
                                edgeTiles.put(newTile.position.cpy(), newTile);
                            }
                            else if (type.equals("desert_cacti4")) {
                                Tile newTile = new Tile("desert4", edge, true, currRoute);
                                if (this.rand.nextInt((int)(maxDist-distance)) < maxDist) {
                                    Route tempRoute = null;
                                    if (distance < maxDist/4) {
                                        if (this.rand.nextInt(2) == 1) {
                                            newTile = new Tile("desert6", "cactus7", edge, true, tempRoute);  //desert4
                                        }
                                    }
                                    else {
                                        newTile = new Tile("desert6", "cactus7", edge, true, tempRoute);
                                    }
                                }
                                tilesToAdd.put(newTile.position.cpy(), newTile);
                                edgeTiles.put(newTile.position.cpy(), newTile);
                            }
                            else if (type.equals("desert_cacti2")) {
                                Tile newTile = new Tile("desert4", edge, true, currRoute);

                                if (distance < 2*maxDist/7) {
                                    
                                }
//                                else if (this.rand.nextInt(100) > 98) {
////                                    int nextSize = 25;
//                                    int nextSize = 50;
//                                    ApplyBlotch(game, "grass_desert1", newTile, nextSize, grassTiles, 0, false, currRoute);
//                                }
//                                else if (this.rand.nextInt(140) > 138) {
//                                    int nextSize = 20;
////                                    newTile = new Tile("desert4", "cactus7", edge);
//                                    HashMap<Vector2, Tile> newTiles = new HashMap<Vector2, Tile>();
//                                    ApplyBlotch(game, "desert_cacti4", newTile, nextSize, newTiles, 0, false, currRoute);
//                                    tilesToAdd.putAll(newTiles);
//                                }
                                else if (distance < 5*maxDist/7) {
                                    
                                }
//                                else if (this.rand.nextInt(10) > 8) {
////                                    int nextSize = 40 +this.rand.nextInt(80);
////                                    int nextSize = 40 +this.rand.nextInt(40);
//                                    int nextSize = 40; // +this.rand.nextInt(5);
//                                    newTile = new Tile("desert2_trapinch_spawn", edge, true, currRoute);
//                                    HashMap<Vector2, Tile> newTiles = new HashMap<Vector2, Tile>();
//                                    ApplyBlotch(game, "sand_pit1", newTile, nextSize, newTiles, 0, true, currRoute);
//                                    tilesToAdd.putAll(newTiles);
//                                }

                                tilesToAdd.put(newTile.position.cpy(), newTile);
                                edgeTiles.put(newTile.position.cpy(), newTile);
                            }
                            // graveyard
                            else if (type.equals("graveyard1")) {
                                String name = "green12";
                                String nameUpper = "";

                                // TODO/idea: make a generic 'striped patterns' algorithm
                                // - could have parameters for skew, rotation etc.
//                                int offsetY = Math.abs(((int)Math.abs(edge.y/16) %10) -5);  // zigzag pattern
//                                int offsetX = Math.abs((((int)Math.abs(edge.x/16) - offsetY) %10) -5);  // zigzag pattern
                                Vector2 rotate = edge.cpy().rotate(-15);
                                int offsetY = ((int)Math.abs(rotate.y/8) %14) -7;
                                int offsetX = (((int)Math.abs(rotate.x/8) -offsetY*2) %14) -7;  //  -offsetY
                                offsetX = Math.abs(offsetX);
                                offsetY = Math.abs(offsetY);
                                int offset1 = offsetX +offsetY;
                                offsetY = Math.abs(((int)Math.abs(rotate.y/8) %20) -10);
                                offsetX = Math.abs((((int)Math.abs(rotate.x/8)) %20) -10);
                                int offset2 = offsetX +offsetY;
                                offset2 = offset2/5;
//                                offset2 = Math.abs((offset -20) % 10);

//                                if (this.rand.nextInt(offset+1) +offset > 4+offsetY) {  // very even stripes
//                                if (this.rand.nextInt(offset+1) +offset > 6) {  // old ver
//                                int offset = offsetX*offsetX +offsetY*offsetY;  // made edges 'circular'
                                if (offset1*offset2 > 6) {  // this.rand.nextInt(offset*2 +1)
                                    name = "green11";
                                    if (this.rand.nextInt(5) == 0) {
                                        if (this.rand.nextBoolean()) {
                                            name = "green10";
                                        }
                                        else {
                                            name = "green9";
                                        }
                                    }
                                }
                                else {
                                    if (offset1 < 2) {
                                        if (this.rand.nextInt(256) < 80) {
                                            name = "flower5";
                                        }
                                    }
                                    else if (this.rand.nextInt(15) == 0) {
                                        name = "flower5";
                                    }
                                }
                                // Chance to put withered tree
                                // TODO: remove, didn't look that good
                                // Needs to be a 32x32 tree
//                                if (!name.equals("flower5") && this.rand.nextInt(110) == 0) {
//                                    nameUpper = "cactus11";
//                                }

                                // If this is a gravestone, follow new rules
                                if (edge.y/16 % 2  == (int)((edge.y/32 % 11)/5) &&
                                    // 11)/3 - slightly thinner
                                    // (edge.x/16) % 2  == (int)((((edge.x/16)+(edge.y/((edge.y/32 % 5)/3 +1))) % 11)/4)) { <-- prev ver, very good
                                    (edge.x/16) % 2  == (int)((((edge.x/16)+(edge.y/2)) % 13)/5)) {
                                    int scale = 16;
                                    rotate = edge.cpy().rotate(0);
                                    offsetY = ((int)Math.abs(rotate.y/scale) %14) -7;
                                    offsetX = (((int)Math.abs(rotate.x/scale - rotate.y/scale)) %14) -7;
                                    offsetX = Math.abs(offsetX);
                                    offsetY = Math.abs(offsetY);
                                    offset1 = offsetX +offsetY;
                                    offsetY = Math.abs(((int)Math.abs(rotate.y/scale) %20) -10);
                                    offsetX = Math.abs((((int)Math.abs(rotate.x/scale)) %20) -10);
                                    offset2 = offsetX +offsetY;
                                    offset2 = offset2/5;
                                    if (offset1*offset2 > 9) {
                                        name = "green12";
                                        nameUpper = "gravestone2";
                                    }
                                }
                                Tile newTile = new Tile(name, nameUpper, edge, true, currRoute);
                                // Chance to spawn grass
                                boolean putGrass = this.rand.nextInt(90) < 1;
                                if (distance > maxDist/2) {
                                    putGrass = this.rand.nextInt(40) < 1;
                                }
                                if (putGrass) {
                                    int nextSize = 40;
                                    ApplyBlotch(game, "grass_graveyard1", newTile, nextSize, grassTiles, 0, false, currRoute);
                                }
                                //
                                newTile.biome = "graveyard";
                                tilesToAdd.put(newTile.position.cpy(), newTile);
                                edgeTiles.put(newTile.position.cpy(), newTile);

                                // Save potential fence locations
                                if (offset1 < 1) {
                                    fenceSpawns.add(edge.cpy());
                                }
                            }
                            // Foresty blotch
                            else if (type.equals("mtn_green1") || type.equals("mtn_snow1")) {
                                Tile newTile;
                                if (this.rand.nextInt(5) == 0) {
                                    // TODO: Trevenant and Abamosnow encounters
                                    Route tempRoute = null;
                                    if (type.equals("mtn_snow1")) {
                                        // 1 in 3 ish trees has a pokemon
                                        int randInt = this.rand.nextInt(3);
                                        if (randInt == 2) {
                                            tempRoute = new Route("", 22);
                                            String[] pokemon = new String[]{
//                                                                            "pineco", "aipom", "kakuna", "metapod", "spinarak", "heracross",
//                                                                            "ledyba", "hoothoot", "zubat", "pidgey", "spearow", "forretress",
                                                                            "snover"};
                                            randInt = this.rand.nextInt(pokemon.length);
                                            tempRoute.storedPokemon.add(new Pokemon(pokemon[randInt], 20+this.rand.nextInt(4)));
                                        }
                                        newTile = new Tile("tree4", edge, true, tempRoute);
                                    }
                                    else {
                                        // 1 in 3 ish trees has a pokemon
                                        int randInt = this.rand.nextInt(3);
                                        if (randInt == 2) {
                                            tempRoute = new Route("", 22);
                                            String[] pokemon = new String[]{"pineco", "aipom", "kakuna", "metapod", "spinarak", "heracross",
                                                                            "ledyba", "hoothoot", "zubat", "pidgey", "spearow", "forretress"};
                                            randInt = this.rand.nextInt(pokemon.length);
                                            tempRoute.storedPokemon.add(new Pokemon(pokemon[randInt], 20+this.rand.nextInt(4)));
                                        }
                                        newTile = new Tile("tree2", edge, true, tempRoute);
                                    }
                                }
                                else if (this.rand.nextInt(5) == 0) {
                                    if (type.equals("mtn_green1")) {
                                        newTile = new Tile("grass2", edge, false, currRoute);
                                    }
                                    else {
                                        newTile = new Tile("grass3", edge, false, currRoute);
                                    }

                                }
                                // Rock smash rock
                                else if (this.rand.nextInt(10) == 0) {
//                                    Route tempRoute = new Route("rock_smash1", currRoute.level);  // TODO: remove
                                    Route tempRoute = null;
                                    int randInt = this.rand.nextInt(2);
                                    if (randInt == 0) {
                                        tempRoute = new Route("", 22);
                                        String[] pokemon = new String[]{"slugma", "geodude", "shuckle"};
                                        randInt = this.rand.nextInt(pokemon.length);
                                        tempRoute.storedPokemon.add(new Pokemon(pokemon[randInt], tempRoute.level+this.rand.nextInt(3)));
                                        // TODO: remove
//                                        tempRoute.allowedPokemon.add(pokemon[randInt]);
//                                        tempRoute.genPokemon(256);
//                                        tempRoute.pokemon.add(new Pokemon(pokemon[randInt], currRoute.level+this.rand.nextInt(4), Pokemon.Generation.CRYSTAL));
                                    }
                                    if (type.equals("mtn_snow1")) {
                                        newTile = new Tile("snow1", "rock1_color", edge.cpy(), true, tempRoute);
                                    }
                                    else {
                                        newTile = new Tile("green1", "rock1_color", edge.cpy(), true, tempRoute);
                                    }
                                }
                                // flowers for mtn_green
                                else if (this.rand.nextInt(8) == 0) {
                                    if (type.equals("mtn_green1")) {
                                        if (this.rand.nextInt(2) == 0) {
                                            // flowery green
                                            newTile = new Tile("flower2", edge, false, currRoute);
                                        }
                                        else {
                                            // flowery green
                                            newTile = new Tile("flower3", edge, false, currRoute);
                                        }
                                    }
                                    else {
                                        newTile = new Tile("snow1", edge, false, currRoute);
                                    }
                                }
                                else {
                                    if (type.equals("mtn_green1")) {
                                        newTile = new Tile("green1", edge, false, currRoute);
                                    }
                                    else {
                                        newTile = new Tile("snow1", edge, false, currRoute);
                                    }
                                }
                                if (type.equals("mtn_green1")) {
                                    newTile.biome = "deep_forest";
                                }
                                if (currRoute.level <= 10) {
                                    newTile.isBottomMtnLayer = true;
                                }
                                tilesToAdd.put(newTile.position.cpy(), newTile);
                                edgeTiles.put(newTile.position.cpy(), newTile);
                            }
                        }
                    }
                }

                // only select edge tiles for new 'blotches'
                // note - don't select sand/rock tiles, b/c I want forests to blend together
                // ie not be too far apart
                if (tile.position.dst(originTile.position) > prevTile.position.dst(originTile.position) &&
                        !tile.name.equals("sand1") && !tile.name.equals("rock1") && !tile.name.equals("tree5")) {
                    prevTile = tile;
                    // use a random number of these later
                    // add beginning of list
                    prevTiles.add(0, tile);
                }
                // numAdded checks if we put a lot of tiles around this tile. if so,
                // it's probably not an edge
//                if (numAdded < 1 && tile.position != originTile.position) {
//                    prevTiles.add(0, tile);
//                }
                edgeTiles.remove(tile.position);
            }
        }
        for (Tile tile : grassTiles.values()) {
            if (tilesToAdd.containsKey(tile.position) &&
                !tilesToAdd.get(tile.position).attrs.get("solid")) {
                tilesToAdd.put(tile.position.cpy(), tile);
            }
        }

        // Create forest maze if applicable
        // get bl/tr
        if (isMaze != 0 && forestMazeTiles.size() > 0) {
            Vector2 bl = null;
            Vector2 tr = null;
            for (Tile tile : forestMazeTiles.values()) {
                if (bl == null) {
                    bl = tile.position.cpy();
                }
                if (tr == null) {
                    tr = tile.position.cpy();
                }
                if (tile.position.x < bl.x) {
                    bl.x = tile.position.x;
                }
                else if (tile.position.x > tr.x) {
                    tr.x = tile.position.x;
                }
                if (tile.position.y < bl.y) {
                    bl.y = tile.position.y;
                }
                else if (tile.position.y > tr.y) {
                    tr.y = tile.position.y;
                }
            }
            // this didn't seem to fix anything
            tr.x = tr.x - (tr.x % 16);
            bl.x = bl.x - (bl.x % 16);
            tr.y = tr.y - (tr.y % 16);
            bl.y = bl.y - (bl.y % 16);
            // TODO: density/complexity appear to not matter at large sizes, maze is always the same size.
            // Algorithm needs to be fixed.
            float density = 0.1f;
            float complexity = 0.9f;
            int squareSize = 3*32;
            HashMap<Vector2, MazeNode> nodes = GenForest2.Maze_Algo1((int)(tr.x-bl.x)*2/squareSize, (int)(tr.y-bl.y)*2/squareSize, density, complexity, squareSize, this.rand);
            // for each node, get a template tile that matches it's number of openings
             // nodes contains the nodes of the maze
//            HashMap<Vector2, Tile> newTiles = new HashMap<Vector2, Tile>();
            ArrayList<Tile> tileSquare;
            for (MazeNode node : nodes.values()) {
                // get tiles depending on type of mazenode
                if (node.type == "platform1") {
                    tileSquare = GenForest2.getTileSquarePlatform1(node, bl, this.rand);
                }
                else {
                    tileSquare = GenForest2.getTileSquare(node, bl, this.rand, true);
                }
                for (Tile tile : tileSquare) {
                    if (forestMazeTiles.containsKey(tile.position)) {
                        tilesToAdd.put(tile.position.cpy(), tile);
                    }
                }
            }
            // TODO: this still isn't working
            // it keeps creating larger and larger mazes
            if (tilesToAdd.size() > 0) {
                Action temp = new GenForest2.ApplyForestBiome(tilesToAdd, bl.cpy(), tr.cpy(), true, null);
                temp.step(game);
            }

            // Post-process - add continuation to ledges
            for (Tile tile : new ArrayList<Tile>(tilesToAdd.values())) {
                if ((tile.name.equals("ledge_grass_down") || tile.name.equals("ledge_ramp_down")) &&
                    tilesToAdd.containsKey(tile.position.cpy().add(16,0)) && !tilesToAdd.get(tile.position.cpy().add(16,0)).attrs.get("solid") && !tilesToAdd.get(tile.position.cpy().add(16,0)).attrs.get("ledge")) {
                    HashMap<Vector2, Tile> upTiles = new HashMap<Vector2, Tile>();
                    HashMap<Vector2, Tile> rightTiles = new HashMap<Vector2, Tile>();
                    upTiles.put(tile.position.cpy().add(16, 0), new Tile("ledge1_corner_br", tile.position.cpy().add(16, 0), true));
                    rightTiles.put(tile.position.cpy().add(16, 0), new Tile("ledge_grass_down", tile.position.cpy().add(16, 0), true));
                    for (int i=16; i < 5*16; i+=16) {
                        if (tilesToAdd.containsKey(tile.position.cpy().add(16, i)) && (tilesToAdd.get(tile.position.cpy().add(16, i)).attrs.get("solid") || tilesToAdd.get(tile.position.cpy().add(16, i)).attrs.get("ledge"))) {
                            tilesToAdd.putAll(upTiles);
                            break;
                        }
                        if (tilesToAdd.containsKey(tile.position.cpy().add(16+i, 0)) && (tilesToAdd.get(tile.position.cpy().add(16+i, 0)).attrs.get("solid") || tilesToAdd.get(tile.position.cpy().add(16+i, 0)).attrs.get("ledge"))) {
                            tilesToAdd.putAll(rightTiles);
                            break;
                        }
                        upTiles.put(tile.position.cpy().add(16, i), new Tile("ledge1_right", tile.position.cpy().add(16, i), true));
                        rightTiles.put(tile.position.cpy().add(16+i, 0), new Tile("ledge_grass_down", tile.position.cpy().add(16+i, 0), true));
                    }
                }
                if ((tile.name.equals("ledge_grass_down") || tile.name.equals("ledge_ramp_down")) &&
                        tilesToAdd.containsKey(tile.position.cpy().add(-16,0)) && !tilesToAdd.get(tile.position.cpy().add(-16,0)).attrs.get("solid") && !tilesToAdd.get(tile.position.cpy().add(-16,0)).attrs.get("ledge")) {
                    HashMap<Vector2, Tile> upTiles = new HashMap<Vector2, Tile>();
                    HashMap<Vector2, Tile> leftTiles = new HashMap<Vector2, Tile>();
                    upTiles.put(tile.position.cpy().add(-16, 0), new Tile("ledge1_corner_bl", tile.position.cpy().add(-16, 0), true));
                    leftTiles.put(tile.position.cpy().add(-16, 0), new Tile("ledge_grass_down", tile.position.cpy().add(-16, 0), true));
                    for (int i=16; i < 5*16; i+=16) {
                        if (tilesToAdd.containsKey(tile.position.cpy().add(-16, i)) && (tilesToAdd.get(tile.position.cpy().add(-16, i)).attrs.get("solid") || tilesToAdd.get(tile.position.cpy().add(-16, i)).attrs.get("ledge"))) {
                            tilesToAdd.putAll(upTiles);
                            break;
                        }
                        if (tilesToAdd.containsKey(tile.position.cpy().add(-16-i, 0)) && (tilesToAdd.get(tile.position.cpy().add(-16-i, 0)).attrs.get("solid") || tilesToAdd.get(tile.position.cpy().add(-16-i, 0)).attrs.get("ledge"))) {
                            tilesToAdd.putAll(leftTiles);
                            break;
                        }
                        upTiles.put(tile.position.cpy().add(-16, i), new Tile("ledge1_left", tile.position.cpy().add(-16, i), true));
                        leftTiles.put(tile.position.cpy().add(-16-i, 0), new Tile("ledge_grass_down", tile.position.cpy().add(-16-i, 0), true));
                    }
                }
            }
        }
        
        // Add fenced-in areas in graveyards
        if (type.equals("graveyard1")) {
            String name, nameUpper;
            Tile newTile;
            Vector2 edge;
            for (int numFences = this.rand.nextInt(4) +1; numFences > 0 && !fenceSpawns.isEmpty(); numFences--) {
                boolean doneDoll = false;
                edge = fenceSpawns.remove(this.rand.nextInt(fenceSpawns.size()));
                int size = 4 +this.rand.nextInt(3);
                int left = this.rand.nextInt(size);
                int doorOffset = this.rand.nextInt(2)*16;
                int numDoors = this.rand.nextInt(4) +1;
//                boolean hasGravestones = this.rand.nextBoolean();
                Vector2 bl = edge.cpy().add(-16*(left), -16*(left));
                //edge.y/16 % 2 == ((int)((edge.y/32 % 11)/5)
                bl.x += bl.x % 32;
                bl.y += ((bl.y/16 % 2) +((int)((bl.y/32 % 11)/5))) *16;  // forces row to fall in line with gravestones

                Vector2 tr = bl.cpy().add(16*size, 16*size);
                tr.x += tr.x % 32;
                tr.y += ((tr.y/16 % 2) +((int)((tr.y/32 % 11)/5))) *16;  // forces row to fall in line with gravestones
                for (Vector2 currPos = bl.cpy(); currPos.y <= tr.y;) {
                    name = "flower5";
                    nameUpper = "";
                    boolean replace = true;
                    if (currPos.x == bl.x && currPos.y == bl.y) {
                        name = "green12";
                        nameUpper = "fence1_NE";
                    }
                    else if (currPos.x == tr.x && currPos.y == tr.y) {
                        name = "green12";
                        nameUpper = "fence1_SW";
                    }
                    else if (currPos.x == bl.x && currPos.y == tr.y) {
                        name = "green12";
                        nameUpper = "fence1_SE";
                    }
                    else if (currPos.x == tr.x && currPos.y == bl.y) {
                        name = "green12";
                        nameUpper = "fence1_NW";
                    }
                    else if (currPos.y == bl.y || currPos.y == tr.y) {
                        name = "green12";
                        nameUpper = "fence1";
                        if (numDoors > 0 && currPos.x == bl.x + (size/2)*16 +doorOffset) {
                            nameUpper = "house5_door1";
                            numDoors--;
                        }
                    }
                    else if (currPos.x == bl.x || currPos.x == tr.x) {
                        name = "green12";
                        nameUpper = "fence1_NS";
                        if (this.rand.nextBoolean() && numDoors > 0 && currPos.y == bl.y + (size/2)*16 +doorOffset) {
                            nameUpper = "house5_door1";
                            numDoors--;
                        }
                    }
                    else  {
                        if (tilesToAdd.get(currPos) != null) {
                            if (!tilesToAdd.get(currPos).nameUpper.equals("") &&
                                currPos.y != bl.y+16) {
                                replace = false;
                            }
                            if (tilesToAdd.get(currPos).nameUpper.contains("fence")) {
                                replace = true;
                            }
                        }
                        //
                        if (replace &&
//                            hasGravestones &&
//                            currPos.y/16 % 2 == ((int)((currPos.y/32 % 11)/5)) &&
                            currPos.y % 32 == 0 &&
                            currPos.x != bl.x +16 &&
                            currPos.x != tr.x -16 &&
                            currPos.y != bl.y +16
//                            currPos.y != tr.y -16
//                            currPos.y != bl.y + (size/2)*16 +doorOffset
//                            currPos.x != bl.x + (size/2)*16 +doorOffset
                            ) {
                            keystoneSpawns.add(currPos.cpy());
                            if (this.rand.nextInt(3) == 0) {
                                name = "green12";
                                nameUpper = "gravestone2";
                            }

                            // TODO: test
                            if (!doneDoll && (size <= 5 || this.rand.nextInt(3) == 0)) {
                                doneDoll = true;
                                int randInt = this.rand.nextInt(3);
                                nameUpper = "pokedoll1";
                                if (randInt == 0) {
                                    nameUpper = "pokedoll1_mimikyu";
                                }
                                else if (randInt == 1) {
                                    nameUpper = "pokedoll1_banette";
                                }
                            }
                            // 
                        }
                    }
                    if (replace) {
                        newTile = new Tile(name, nameUpper, currPos.cpy(), true, currRoute);
                        newTile.biome = "graveyard";
                        tilesToAdd.put(currPos.cpy(), newTile);
                        game.map.adjustSurroundingTiles(newTile, tilesToAdd, true);
                    }
                    //
                    currPos.x += 16;
                    if (currPos.x > tr.x) {
                        currPos.x = bl.x;
                        currPos.y += 16;
                    }
                }
            }
        }

        // origin changes to new spot
//        System.out.println(newSize);
        if (newSize <= 0) {
            return;
        }

        // todo: param
        int randInt = 1;  // previous behavior, keeping for now
//        int randInt = this.rand.nextInt(3) + 1;
        // TODO: test
        int[] vals = {1, 1, 1, 1, 2};  //   1, 1,  // TODO: not sure about this.
        randInt = vals[this.rand.nextInt(vals.length)];
        if (prevTiles.size() < randInt) {
            randInt = prevTiles.size();
        }
        
        if (type.equals("oasis1")) {
            vals = new int[]{3, 3, 4, 4, 5, 5};
            randInt = vals[this.rand.nextInt(vals.length)];
            if (prevTiles.size() < randInt) {
                randInt = prevTiles.size();
            }
            newSize += maxDist/10;
        }
        else if (type.equals("sand_pit1")) {
//            if (this.rand.nextInt(3) < 2) {
////                newSize = maxDist;
//                newSize -= maxDist/4;
//            }
//            else {
//                newSize -= maxDist/5;
//            }
//            if (this.rand.nextInt(3) < 1) {
//                newSize -= this.rand.nextInt((int)Math.ceil(maxDist/6));
//            }
//            if (GenIsland1.numSandPits < 2) {
//                GenIsland1.numSandPits++;
//            }
//            else 
            if (randInt > 1) {
                randInt = 1;
            }
            if (GenIsland1.numSandPits >= 20) {   // this.rand.nextInt(30) < 1 || 
//                newSize -= maxDist/3;  
                newSize = 1;
            }
            else {
                GenIsland1.numSandPits++;
            }
        }

//        remove/fix this logic
        HashMap<Vector2, Tile> nextIslandTiles = new HashMap<Vector2, Tile>();
        int next=0;
        if ((type.equals("island") || type.equals("desert") || type.equals("oasis1") || type.equals("sand_pit1")) && doNext) {
            for (int i=0; i < randInt; i++) {

                if (GenIsland1.doneDesert == 1) {
                    tilesToAdd.put(prevTiles.get(next).position.cpy(), prevTiles.get(next));
                    Route blotchRoute = new Route("desert1", 40);
                    // newSize +80  -- desert slightly bigger
                    ApplyBlotch(game, "desert", prevTiles.get(next), newSize +(maxDist/4), nextIslandTiles, 1, true, blotchRoute);
                    continue;
                }

                tilesToAdd.put(prevTiles.get(next).position.cpy(), prevTiles.get(next));
                // TODO: this code is weird.
                String newType = type;
                if (type.equals("desert")) {
                    newType = "island";
                    // As far as I can tell, this route's level isn't used.
                    // It is applied to flower and green tiles.
                    currRoute = new Route("forest1", 40);
                }
                ApplyBlotch(game, newType, prevTiles.get(next), newSize, nextIslandTiles, 1, true, currRoute);
//                int level = currRoute.level - (int)(10*(1f-(newSize/maxDist)));
//                if (level <= 5) {
//                    level = 5;
//                }
//                Route newRoute = new Route("forest1", currRoute.level-10);
//                ApplyBlotch(game, "island", prevTiles.get(next), newSize, nextIslandTiles, 1, true, newRoute);
                next += this.rand.nextInt(prevTiles.size() -next) + next;
                if (next >= prevTiles.size()) {
                    break;
                }
            }
        }

        // manually 'interlace'?
        // other option is tilesToAdd is only beach tiles, idk.
        for (Tile tile : nextIslandTiles.values()) {
            if (tilesToAdd.containsKey(tile.position)) {
                Tile currTile = tilesToAdd.get(tile.position);
                
                // TODO: should probably have different rules depending on blotch type
                // Diff rules for sand pits
                if (type.equals("sand_pit1")) {
                    if (tile.name.equals("desert2")) {
                        tilesToAdd.put(tile.position.cpy(), tile);
                        continue;
                    }
                    continue;
                }
                
                // Desert tiles take priority
                if (tile.name.contains("desert")) {
                    tilesToAdd.put(tile.position.cpy(), tile);
                    continue;
                }
                // Desert tiles take priority
                if (currTile.name.contains("desert")) {
                    continue;
                }
                if ((currTile.name.equals("sand1") ||
                     currTile.name.equals("rock1") ||
                     currTile.name.equals("tree5") ||
                     currTile.name.equals("grass_sand1")) &&
                    !tile.name.equals("sand1")) {
                    tilesToAdd.put(tile.position.cpy(), tile);
                }
                // Works decently well, still some blank areas
                if (tile.name.equals("tree_large1")) {
                    tilesToAdd.put(tile.position.cpy(), tile);
                    tilesToAdd.put(tile.position.cpy().add(16,0), new Tile("tree_large1_noSprite", tile.position.cpy().add(16,0)));
                    tilesToAdd.put(tile.position.cpy().add(0,16), new Tile("tree_large1_noSprite", tile.position.cpy().add(0,16)));
                    tilesToAdd.put(tile.position.cpy().add(16,16), new Tile("tree_large1_noSprite", tile.position.cpy().add(16,16)));
                }

            }
            else {
                tilesToAdd.put(tile.position.cpy(), tile);
            }
        }
        
        // TODO: remove
//        if (type.equals("oasis1")) {
//            for (Tile tile2 : tilesToAdd.values()) {
//                if (tile2.nameUpper.equals("aloe_large1")) {
//                    tilesToAdd.put(tile2.position.cpy().add(16, 0), new Tile("green1", "solid", tile2.position.cpy().add(16, 0)));
//                    tilesToAdd.put(tile2.position.cpy().add(0, 16), new Tile("green1", "solid", tile2.position.cpy().add(0, 16)));
//                    tilesToAdd.put(tile2.position.cpy().add(16,16), new Tile("green1", "solid", tile2.position.cpy().add(16,16)));
//                }
//            }
//        }

    }

    public ArrayList<Tile> ApplyBlotchMountain(Game game, Tile originTile, int maxDist, HashMap<Vector2, Tile> tilesToAdd) {
//        maxDist = 120*140; // TODO: debug, remove
//        maxDist = 100*120;
//        maxDist = 60*100;
//        maxDist = 150*140;
//        maxDist = 200*150;
        ArrayList<Tile> edgeTiles = new ArrayList<Tile>();
        HashMap<Tile, Vector2> edgeDirs = new HashMap<Tile, Vector2>();
        HashMap<Vector2, Tile> mtnTiles = new HashMap<Vector2, Tile>();
        Tile copyTile = new Tile(originTile.name, originTile.position.cpy()); // .add(16, 16)
        edgeTiles.add(originTile);
        edgeTiles.add(copyTile);
        Vector2 dir1 = new Vector2(this.rand.nextInt(200)-100, this.rand.nextInt(200)-100);
        Vector2 dir2 = dir1.cpy().rotate(180);
        // TODO: debug, remove
//        System.out.println(dir1);
//        System.out.println(dir2);
        edgeDirs.put(originTile, dir1);
        edgeDirs.put(copyTile, dir2);

        HashMap<Tile, Vector2> origins = new HashMap<Tile, Vector2>();
        HashMap<Tile, Integer> originDists = new HashMap<Tile, Integer>();
        origins.put(originTile, originTile.position.cpy());
        origins.put(copyTile, originTile.position.cpy());
        originDists.put(originTile, maxDist);
        originDists.put(copyTile, maxDist);

        Tile copyTile2 = new Tile(originTile.name, originTile.position.cpy());
        Tile copyTile3 = new Tile(originTile.name, originTile.position.cpy());
        edgeTiles.add(copyTile2);
        edgeTiles.add(copyTile3);
        edgeDirs.put(copyTile2, dir1.cpy().rotate(90));
        edgeDirs.put(copyTile3, dir2.cpy().rotate(90));
        origins.put(copyTile2, originTile.position.cpy());
        origins.put(copyTile3, originTile.position.cpy());
        originDists.put(copyTile2, (int)(2f*Math.abs(maxDist)/8f));
        originDists.put(copyTile3, (int)(2f*Math.abs(maxDist)/8f));

        ArrayList<Tile> endPoints = new ArrayList<Tile>();

        int currLevel = 0;
        int newLevel = 0;
        // todo: this needs to factor in maxDist
//        int[] levels = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1};
        // TODO: these don't scale with maxDist, needs to
        int[] levels2 = new int[]{0, 0, -1, -1, -1};
//        int[] levels = new int[]{0, 0, 0, 0, 0, 0, 0, 0, -1};
//        int levelIndex = 0;
        HashMap<Vector2, Tile> levelTiles = new HashMap<Vector2, Tile>();
        HashMap<Tile, Integer> tileLevels = new HashMap<Tile, Integer>();
        levelTiles.put(originTile.position.cpy(), originTile);
        levelTiles.put(copyTile.position.cpy(), copyTile);
        tileLevels.put(originTile, currLevel);
        tileLevels.put(copyTile, currLevel);
        int maxDist2;
        Route mtnRoute = new Route("mountain1", 33);
        Route snowRoute = new Route("snow1", 44);

//        while (!edgeTiles.isEmpty() || currLevel > -Math.ceil(maxDist/2000)) {
        while (!edgeTiles.isEmpty() || currLevel > -Math.ceil(maxDist/4000)) {
//            newLevel = currLevel + levels[this.rand.nextInt(levels.length)];
//            newLevel = currLevel + levels[levelIndex++ % levels.length];
            System.out.println(newLevel);

            for (Tile tile : new ArrayList<Tile>(edgeTiles)) {
                edgeTiles.remove(tile);
                Vector2 currDir = edgeDirs.get(tile);
                edgeDirs.remove(tile);
                float distance = tile.position.dst(origins.get(tile));
//                float distance = tile.position.dst(originTile.position);
                maxDist2 = originDists.get(tile);
                int putTile = this.rand.nextInt(maxDist2+1) + (int)distance - (maxDist2/32);  // maxDist/32 functions as min distance
                if (putTile < maxDist2) {
                    Vector2 newDir = new Vector2();
                    putTile = this.rand.nextInt(Math.abs((int)currDir.x)+1)+ this.rand.nextInt(Math.abs((int)currDir.y)+1);
                    if (putTile < Math.abs(currDir.y)+2) {
                        // put y
                        if (currDir.y < 0) {
                            // negative y
                            newDir.x = 0;
                            newDir.y = -1;
                        }
                        else {
                            // positive y
                            newDir.x = 0;
                            newDir.y = 1;
                        }
                    }
                    else {
                        // put x
                        if (currDir.x < 0) {
                            // negative x
                            newDir.x = -1;
                            newDir.y = 0;
                        }
                        else {
                            // positive x
                            newDir.x = 1;
                            newDir.y = 0;
                        }
                    }
                    // TODO: variance/trees here? yeah, then do ledges later
                    Tile nextTile = new Tile("mountain1", "", tile.position.cpy().add(16*newDir.x, 16*newDir.y), true, mtnRoute);
                    // TODO: variate the dirs
                    mtnTiles.put(nextTile.position.cpy(), nextTile);
                    edgeTiles.add(nextTile);
                    edgeDirs.put(nextTile, currDir);
                    origins.put(nextTile, origins.get(tile));
                    originDists.put(nextTile, originDists.get(tile));

                    // trace around these
                    levelTiles.put(nextTile.position.cpy(), nextTile);
                    tileLevels.put(nextTile, newLevel);

                    // TODO: decide if splitting off tree
//                    if (this.rand.nextInt(maxDist*edgeTiles.size()) < maxDist/2) { // maxDist/18
//                    System.out.println(Math.ceil(1f/(4f/maxDist)));
                    if (this.rand.nextInt((int)Math.ceil(1f/(4f/((maxDist+maxDist2)/350f)))) == 1) {
                        int degrees = this.rand.nextInt(80) + 10;
                        Vector2 branchDir = currDir.cpy().rotate(degrees);
                        nextTile = new Tile("mountain1", "", tile.position.cpy(), true, mtnRoute);
                        edgeTiles.add(nextTile);
                        edgeDirs.put(nextTile, branchDir);
                        origins.put(nextTile, tile.position.cpy());
//                        originDists.put(nextTile, (int)Math.abs(maxDist2-(distance*3))/12); // maxDist2/12
                        originDists.put(nextTile, (int)(1f*Math.abs(maxDist2-distance)/8f)); // maxDist2/12

                        branchDir = currDir.cpy().rotate(-degrees);
                        nextTile = new Tile("mountain1", "", tile.position.cpy(), true, mtnRoute);
                        edgeTiles.add(nextTile);
                        edgeDirs.put(nextTile, branchDir);
                        origins.put(nextTile, tile.position.cpy());
//                        originDists.put(nextTile, (int)Math.abs(maxDist2-(distance*3))/12); // maxDist2/12
                        originDists.put(nextTile, (int)(1f*Math.abs(maxDist2-distance)/8f)); // maxDist2/12
                    }
                }
                else {
                    endPoints.add(tile);
                }
                origins.remove(tile);
                originDists.remove(tile);
            }

//            if (newLevel != currLevel) {
            if (this.rand.nextInt((int)Math.ceil(1f/(1f/((maxDist)/1000f)))) == 1) {
                newLevel = currLevel + levels2[this.rand.nextInt(levels2.length)];
                AddMtnLayer(levelTiles, tileLevels, mtnTiles, newLevel, newLevel <= -1 ? "mountain3" : "snow1", newLevel <= -1 ? mtnRoute : snowRoute);
                currLevel = newLevel;
            }
        }
        AddMtnLayer(levelTiles, tileLevels, mtnTiles, currLevel-1, "mountain3", mtnRoute);
        AddMtnLayer(levelTiles, tileLevels, mtnTiles, currLevel-2, "mountain3", mtnRoute);
        // for each tile,
        // if bott 3 are lower, make edge
        // if corner 3 are lower, make corner
        // ... else, do nothing for now (maybe make it a new sprite
//        Tile nextTile;
        HashMap<Vector2, Tile> mtnTiles2 = new HashMap<Vector2, Tile>();
        boolean done = false;
        while (!done) {
            done = true;
            for (Tile tile : mtnTiles.values()) {
                Tile bl = mtnTiles.get(tile.position.cpy().add(-16, -16));
                Tile bot = mtnTiles.get(tile.position.cpy().add(0, -16));
                Tile br = mtnTiles.get(tile.position.cpy().add(16, -16));
                Tile left = mtnTiles.get(tile.position.cpy().add(-16, 0));
                Tile tl = mtnTiles.get(tile.position.cpy().add(-16, 16));
                Tile top = mtnTiles.get(tile.position.cpy().add(0, 16));
                Tile tr = mtnTiles.get(tile.position.cpy().add(16, 16));
                Tile right = mtnTiles.get(tile.position.cpy().add(16, 0));
    //            int numLower = 0;
    //            ArrayList<Tile> values = new ArrayList<Tile>();
    //            values.add(bot);
    //            values.add(left);
    //            values.add(top);
    //            values.add(right);
    //            for (Tile tile2 : values) {
    //                if (tileLevels.containsKey(tile2) && tileLevels.get(tile2) == tileLevels.get(tile)-1) {
    //                    numLower++;
    //                }
    //            }
                if (tileLevels.containsKey(left) && tileLevels.get(left) == tileLevels.get(tile)-1 &&
                    tileLevels.containsKey(right) && tileLevels.get(right) == tileLevels.get(tile)-1) {
                    tileLevels.put(tile, tileLevels.get(tile)-1);
                    done = false;
//                    break;
                }
                if (tileLevels.containsKey(top) && tileLevels.get(top) == tileLevels.get(tile)-1 &&
                    tileLevels.containsKey(bot) && tileLevels.get(bot) == tileLevels.get(tile)-1) {
                    tileLevels.put(tile, tileLevels.get(tile)-1);
                    done = false;
//                    break;
                }
                if (tileLevels.containsKey(top) && tileLevels.get(top) == tileLevels.get(tile)-1 &&
                    tileLevels.containsKey(left) && tileLevels.get(left) == tileLevels.get(tile)-1 &&
                    tileLevels.containsKey(br) && tileLevels.get(br) == tileLevels.get(tile)-1) {
                    tileLevels.put(tile, tileLevels.get(tile)-1);
                    done = false;
//                    break;
                }
                if (tileLevels.containsKey(top) && tileLevels.get(top) == tileLevels.get(tile)-1 &&
                        tileLevels.containsKey(right) && tileLevels.get(right) == tileLevels.get(tile)-1 &&
                        tileLevels.containsKey(bl) && tileLevels.get(bl) == tileLevels.get(tile)-1) {
                   tileLevels.put(tile, tileLevels.get(tile)-1);
                   done = false;
//                   break;
                }
                if (tileLevels.containsKey(bot) && tileLevels.get(bot) == tileLevels.get(tile)-1 &&
                        tileLevels.containsKey(right) && tileLevels.get(right) == tileLevels.get(tile)-1 &&
                        tileLevels.containsKey(tl) && tileLevels.get(tl) == tileLevels.get(tile)-1) {
                   tileLevels.put(tile, tileLevels.get(tile)-1);
                   done = false;
                }
                if (tileLevels.containsKey(bot) && tileLevels.get(bot) == tileLevels.get(tile)-1 &&
                        tileLevels.containsKey(left) && tileLevels.get(left) == tileLevels.get(tile)-1 &&
                        tileLevels.containsKey(tr) && tileLevels.get(tr) == tileLevels.get(tile)-1) {
                   tileLevels.put(tile, tileLevels.get(tile)-1);
                   done = false;
                }
                if (tileLevels.containsKey(bot) && tileLevels.get(bot) == tileLevels.get(tile) &&
                        tileLevels.containsKey(left) && tileLevels.get(left) == tileLevels.get(tile)-1 &&
                        tileLevels.containsKey(br) && tileLevels.get(br) == tileLevels.get(tile)-1) {
                   tileLevels.put(tile, tileLevels.get(tile)-1);
                   done = false;
                }
                if (tileLevels.containsKey(bot) && tileLevels.get(bot) == tileLevels.get(tile) &&
                        tileLevels.containsKey(right) && tileLevels.get(right) == tileLevels.get(tile)-1 &&
                        tileLevels.containsKey(bl) && tileLevels.get(bl) == tileLevels.get(tile)-1) {
                   tileLevels.put(tile, tileLevels.get(tile)-1);
                   done = false;
                }
                if (tileLevels.containsKey(top) && tileLevels.get(top) == tileLevels.get(tile) &&
                        tileLevels.containsKey(left) && tileLevels.get(left) == tileLevels.get(tile)-1 &&
                        tileLevels.containsKey(tr) && tileLevels.get(tr) == tileLevels.get(tile)-1) {
                   tileLevels.put(tile, tileLevels.get(tile)-1);
                   done = false;
                }
                if (tileLevels.containsKey(top) && tileLevels.get(top) == tileLevels.get(tile) &&
                        tileLevels.containsKey(right) && tileLevels.get(right) == tileLevels.get(tile)-1 &&
                        tileLevels.containsKey(tl) && tileLevels.get(tl) == tileLevels.get(tile)-1) {
                   tileLevels.put(tile, tileLevels.get(tile)-1);
                   done = false;
                }
                if (tileLevels.containsKey(right) && tileLevels.get(right) == tileLevels.get(tile) &&
                        tileLevels.containsKey(top) && tileLevels.get(top) == tileLevels.get(tile)-1 &&
                        tileLevels.containsKey(br) && tileLevels.get(br) == tileLevels.get(tile)-1) {
                   tileLevels.put(tile, tileLevels.get(tile)-1);
                   done = false;
                }
                if (tileLevels.containsKey(right) && tileLevels.get(right) == tileLevels.get(tile) &&
                        tileLevels.containsKey(bot) && tileLevels.get(bot) == tileLevels.get(tile)-1 &&
                        tileLevels.containsKey(tr) && tileLevels.get(tr) == tileLevels.get(tile)-1) {
                   tileLevels.put(tile, tileLevels.get(tile)-1);
                   done = false;
                }
                if (tileLevels.containsKey(left) && tileLevels.get(left) == tileLevels.get(tile) &&
                        tileLevels.containsKey(bot) && tileLevels.get(bot) == tileLevels.get(tile)-1 &&
                        tileLevels.containsKey(tl) && tileLevels.get(tl) == tileLevels.get(tile)-1) {
                   tileLevels.put(tile, tileLevels.get(tile)-1);
                   done = false;
                }
                if (tileLevels.containsKey(left) && tileLevels.get(left) == tileLevels.get(tile) &&
                        tileLevels.containsKey(top) && tileLevels.get(top) == tileLevels.get(tile)-1 &&
                        tileLevels.containsKey(bl) && tileLevels.get(bl) == tileLevels.get(tile)-1) {
                   tileLevels.put(tile, tileLevels.get(tile)-1);
                   done = false;
                }
            }
        }

        for (Tile tile : mtnTiles.values()) {
            Tile bl = mtnTiles.get(tile.position.cpy().add(-16, -16));
            Tile bot = mtnTiles.get(tile.position.cpy().add(0, -16));
            Tile br = mtnTiles.get(tile.position.cpy().add(16, -16));
            Tile left = mtnTiles.get(tile.position.cpy().add(-16, 0));
            Tile tl = mtnTiles.get(tile.position.cpy().add(-16, 16));
            Tile top = mtnTiles.get(tile.position.cpy().add(0, 16));
            Tile tr = mtnTiles.get(tile.position.cpy().add(16, 16));
            Tile right = mtnTiles.get(tile.position.cpy().add(16, 0));
            if (tileLevels.containsKey(left) && tileLevels.get(left) == tileLevels.get(tile) &&
                tileLevels.containsKey(bot) && tileLevels.get(bot) == tileLevels.get(tile)-1 &&
                tileLevels.containsKey(right) && tileLevels.get(right) == tileLevels.get(tile)) {
                mtnTiles2.put(tile.position.cpy(), new Tile("mountain1_bottom1", tile.position.cpy()));
            }
            else if (tileLevels.containsKey(top) && tileLevels.get(top) == tileLevels.get(tile) &&
                     tileLevels.containsKey(right) && tileLevels.get(right) == tileLevels.get(tile) &&
                     tileLevels.containsKey(bot) && tileLevels.get(bot) == tileLevels.get(tile)-1 &&
                     tileLevels.containsKey(left) && tileLevels.get(left) == tileLevels.get(tile)-1) {
                mtnTiles2.put(tile.position.cpy(), new Tile("mountain1_bl1", tile.position.cpy()));
            }
            else if (tileLevels.containsKey(top) && tileLevels.get(top) == tileLevels.get(tile) &&
                     tileLevels.containsKey(left) && tileLevels.get(left) == tileLevels.get(tile)-1 &&
                     tileLevels.containsKey(bot) && tileLevels.get(bot) == tileLevels.get(tile)) {
                mtnTiles2.put(tile.position.cpy(), new Tile("mountain1_left1", tile.position.cpy()));
            }
            else if (tileLevels.containsKey(top) && tileLevels.get(top) == tileLevels.get(tile)-1 &&
                     tileLevels.containsKey(left) && tileLevels.get(left) == tileLevels.get(tile)-1 &&
                     tileLevels.containsKey(right) && tileLevels.get(right) == tileLevels.get(tile) &&
                     tileLevels.containsKey(bot) && tileLevels.get(bot) == tileLevels.get(tile)) {
                mtnTiles2.put(tile.position.cpy(), new Tile("mountain1_tl1", tile.position.cpy()));
            }
            else if (tileLevels.containsKey(top) && tileLevels.get(top) == tileLevels.get(tile)-1 &&
                     tileLevels.containsKey(left) && tileLevels.get(left) == tileLevels.get(tile) &&
                     tileLevels.containsKey(right) && tileLevels.get(right) == tileLevels.get(tile)) {
                mtnTiles2.put(tile.position.cpy(), new Tile("mountain1_top1", tile.position.cpy()));
            }
            else if (tileLevels.containsKey(top) && tileLevels.get(top) == tileLevels.get(tile)-1 &&
                     tileLevels.containsKey(bot) && tileLevels.get(bot) == tileLevels.get(tile) &&
                     tileLevels.containsKey(left) && tileLevels.get(left) == tileLevels.get(tile) &&
                     tileLevels.containsKey(right) && tileLevels.get(right) == tileLevels.get(tile)-1) {
                mtnTiles2.put(tile.position.cpy(), new Tile("mountain1_tr1", tile.position.cpy()));
            }
            else if (tileLevels.containsKey(top) && tileLevels.get(top) == tileLevels.get(tile) &&
                     tileLevels.containsKey(bot) && tileLevels.get(bot) == tileLevels.get(tile) &&
                     tileLevels.containsKey(right) && tileLevels.get(right) == tileLevels.get(tile)-1) {
                mtnTiles2.put(tile.position.cpy(), new Tile("mountain1_right1", tile.position.cpy()));
            }
            else if (tileLevels.containsKey(top) && tileLevels.get(top) == tileLevels.get(tile) &&
                     tileLevels.containsKey(left) && tileLevels.get(left) == tileLevels.get(tile) &&
                     tileLevels.containsKey(bot) && tileLevels.get(bot) == tileLevels.get(tile)-1 &&
                     tileLevels.containsKey(right) && tileLevels.get(right) == tileLevels.get(tile)-1) {
                mtnTiles2.put(tile.position.cpy(), new Tile("mountain1_br1", tile.position.cpy()));
            }
            else if (tileLevels.containsKey(br) && tileLevels.get(br) == tileLevels.get(tile)-1 &&
                     tileLevels.containsKey(bot) && tileLevels.get(bot) == tileLevels.get(tile) &&
                     tileLevels.containsKey(right) && tileLevels.get(right) == tileLevels.get(tile)) {
                mtnTiles2.put(tile.position.cpy(), new Tile("mountain1_br_inner1", tile.position.cpy()));
            }
            else if (tileLevels.containsKey(bl) && tileLevels.get(bl) == tileLevels.get(tile)-1 &&
                     tileLevels.containsKey(bot) && tileLevels.get(bot) == tileLevels.get(tile) &&
                     tileLevels.containsKey(left) && tileLevels.get(left) == tileLevels.get(tile)) {
                mtnTiles2.put(tile.position.cpy(), new Tile("mountain1_bl_inner1", tile.position.cpy()));
            }
            else if (tileLevels.containsKey(tr) && tileLevels.get(tr) == tileLevels.get(tile)-1 &&
                    tileLevels.containsKey(top) && tileLevels.get(top) == tileLevels.get(tile) &&
                    tileLevels.containsKey(right) && tileLevels.get(right) == tileLevels.get(tile)) {
                mtnTiles2.put(tile.position.cpy(), new Tile("mountain3", tile.position.cpy()));
            }
            else if (tileLevels.containsKey(tl) && tileLevels.get(tl) == tileLevels.get(tile)-1 &&
                    tileLevels.containsKey(top) && tileLevels.get(top) == tileLevels.get(tile) &&
                    tileLevels.containsKey(left) && tileLevels.get(left) == tileLevels.get(tile)) {
                mtnTiles2.put(tile.position.cpy(), new Tile("mountain3", tile.position.cpy()));
            }
//            else {
//                mtnTiles2.put(tile.position.cpy(), new Tile("mountain3", tile.position.cpy()));
//            }
        }
        for (Tile tile : new ArrayList<Tile>(mtnTiles.values())) {
            // randomly place grass blotches
            if (this.rand.nextInt((int)Math.ceil(1f/(1f/((maxDist)/500f)))) == 1) {
                float distance = tile.position.dst(originTile.position);
//                int level = (int)(60*(1-(tile.position.dst(this.origin) / (this.radius/12))));
                int level = (int)(30*(1-(tile.position.dst(this.origin) / (this.radius/12))));
                if (level < 4) {
                    level = 4;
                }
                // If this is at base of the mountain,
                // then set level = 10 so player doesn't get
                // super high level encounters
                if (tileLevels.get(tile) <= currLevel) {
                    level = 10;
                }
                else if (level > 30) {
                    level = 30;
                }
                if (distance < 1*maxDist/40) {
                    Route blotchRoute = new Route("snow1", level);
                    ApplyBlotch(game, "mtn_snow1", tile, maxDist/200, mtnTiles2, 0, false, blotchRoute);
                }
                else {
                    Route blotchRoute = new Route("mountain1", level);
                    ApplyBlotch(game, "mtn_green1", tile, maxDist/200, mtnTiles2, 0, false, blotchRoute);
                }

            }
        }
        for (Tile tile : new ArrayList<Tile>(mtnTiles.values())) {
            // Randomly place rocks
            if (this.rand.nextInt((int)Math.ceil(1f/(1f/((maxDist)/500f)))) == 1 && !mtnTiles2.containsKey(tile.position)) {
//                Route tempRoute = new Route("rock_smash1", tile.routeBelongsTo.level);  // TODO: remove
                Route tempRoute = null;
                int randInt = this.rand.nextInt(2);
                if (randInt == 0) {
                    tempRoute = new Route("", 22);
                    String[] pokemon = new String[]{"slugma", "geodude", "shuckle"};
                    randInt = this.rand.nextInt(pokemon.length);
                    tempRoute.storedPokemon.add(new Pokemon(pokemon[randInt], tempRoute.level+this.rand.nextInt(3)));
                    // TODO: remove
//                    tempRoute.allowedPokemon.add(pokemon[randInt]);
//                    tempRoute.genPokemon(256);
//                    tempRoute.pokemon.add(new Pokemon(pokemon[randInt], tile.routeBelongsTo.level+this.rand.nextInt(4), Pokemon.Generation.CRYSTAL));
                }
                Tile newTile = new Tile(tile.name, "rock1_color", tile.position.cpy(), true, tempRoute);
                mtnTiles2.put(tile.position.cpy(), newTile);
            }
        }
        // add all mtnTiles in bulk
        tilesToAdd.putAll(mtnTiles);
        tilesToAdd.putAll(mtnTiles2);

        return endPoints;
    }

    /**
     * Generate regi dungeon.
     */
    public void generateRegiDungeon(Game game, Vector2 origin) {
        HashMap<Vector2, Tile> exteriorTiles = new HashMap<Vector2, Tile>();
        ArrayList<HashMap<Vector2, Tile>> interiorTiles = new ArrayList<HashMap<Vector2, Tile>>();
        String[][] names = new String[][]{{null, null, null, "cave2_tl_alternate", "cave2_tr_alternate", null, null, null, "cave2_tl_alternate", "cave2_tr_alternate", null, null, null},
                                          {null, null, null, "cave2_bl", "cave2_br", null, null, null, "cave2_bl", "cave2_br", null, null, null},
                                          {null, null, null, null, null, "cave2_tl", "cave2_up", "cave2_tr", null, null, null, null, null},
                                          {null, null, null, null, "cave2_tl", "cave2_left", "cave2_floor", "cave2_right", "cave2_tr", null, null, null, null},
                                          {"cave2_tl_alternate", "cave2_tr_alternate", null, "sand1", "cave2_left", "cave2_bl", "cave2_down", "cave2_br", "cave2_right", "sand1", null, "cave2_tl_alternate", "cave2_tr_alternate"},
                                          {"cave2_bl", "cave2_br", null, "sand1", "cave2_bl", "cave2_down", "cave1_door1", "cave2_down", "cave2_br", "sand1", null, "cave2_bl", "cave2_br"},
                                          {null, null, null, null, "sand1", "sand2", "sand2", "sand2", "sand1", null, null, null, null},
                                          {null, null, null, null, null, "sand1", "sand1", "sand1", null, null, null, null, null},
                                          {null, null, null, "cave2_tl_alternate", "cave2_tr_alternate", null, null, null, "cave2_tl_alternate", "cave2_tr_alternate", null, null, null},
                                          {null, null, null, "cave2_bl", "cave2_br", null, null, null, "cave2_bl", "cave2_br", null, null, null}
                                          };
        Vector2 pos;
        Route currRoute = new Route("", 2);
        for (int i=0; i < names.length; i++) {
            for (int j=0; j < names[i].length; j++) {
                if (names[i][j] == null) {
                    continue;
                }
                pos = new Vector2(origin.x -6*16 +j*16, origin.y +5*16 -i*16);
                exteriorTiles.put(pos, new Tile(names[i][j], pos, true, currRoute));
            }
        }
        origin.add(-19*16 +16, +11*16 +6*16 );
        for (int i=0; i < 99-9; i++) {
            interiorTiles.add(null);
        }
        HashMap<Vector2, Tile> currLayer = new HashMap<Vector2, Tile>();
        names = new String[][]{//Padding
                              {"cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2"},
                              {"cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2"},
                              {"cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2"},
                              {"cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2"},
                              {"cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2"},
                              {"cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2"},
                              {"cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2"},
                              //1
                              {"cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_floor2", "cave1_br_inner", "cave1_down1", "cave1_down1", "cave1_down1", "cave1_down1", "cave1_down1", "cave1_down1", "cave1_down1", "cave1_down1", "cave1_down1", "cave1_down1", "cave1_bl_inner", "cave1_floor2", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2"},
                              //2
                              {"cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_floor2", "cave1_right1", "cave1_br_inner", "cave1_down1", "cave1_down1", "cave1_down1", "cave1_down1", "cave1_down1", "cave1_down1", "cave1_down1", "cave1_down1", "cave1_bl_inner", "cave1_left1", "cave1_floor2", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2"},
                              //3
                              {"cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_br_inner", "cave1_br1", "cave1_right1", "cave1_floor1", "cave1_floor1", "cave1_floor1", "cave1_down1_dark", "cave1_down1_dark", "cave1_floor1", "cave1_floor1", "cave1_floor1", "cave1_left1", "cave1_bl1", "cave1_bl_inner", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2"},
                              //4
                              {"cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_br_inner", "cave1_br1", "cave1_floor1", "cave1_floor1", "cave1_stone1", null, null, "cave1_stone1", "cave1_floor1", "cave1_floor1", "cave1_bl1", "cave1_bl_inner", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2"},
                              //5
                              {"cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_right1", "cave1_floor1", "cave1_floor1", "cave1_floor1", "cave1_floor1", "cave1_regi1", "cave1_regi5", "cave1_floor1", "cave1_floor1", "cave1_floor1", "cave1_floor1", "cave1_left1", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2"},
                              //6
                              {"cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_right1", "cave1_floor1", "cave1_floor1", "cave1_floor1", "cave1_tl1", "cave1_up1", "cave1_regipedistal1", "cave1_tr1", "cave1_floor1", "cave1_floor1", "cave1_floor1", "cave1_left1", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2"},
                              //7
                              {"cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_right1", "cave1_floor1", "cave1_stone1", "cave1_floor1", "cave1_left1", "cave1_floor2", "cave1_floor2", "cave1_right1", "cave1_floor1", "cave1_stone1", "cave1_floor1", "cave1_left1", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2"},
                              //8
                              {"cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_right1", "cave1_floor1", "cave1_floor1", "cave1_floor1", "cave1_bl1", "cave1_ramp1", "cave1_ramp1", "cave1_br1", "cave1_floor1", "cave1_floor1", "cave1_floor1", "cave1_left1", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2"},
                              //9
                              {"cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_right1", "cave1_floor1", "cave1_floor1", "cave1_floor1", "cave1_floor1", "cave1_floor1", "cave1_floor1", "cave1_floor1", "cave1_floor1", "cave1_floor1", "cave1_floor1", "cave1_left1", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2"},
                              //10
                              {"cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_tr1", "cave1_floor1", "cave1_floor1", "cave1_stone1", "cave1_floor1", "cave1_floor1", "cave1_stone1", "cave1_floor1", "cave1_floor1", "cave1_tl1", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2"},
                              //11
                              {"cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_right1", "cave1_floor1", "cave1_floor1", "cave1_floor1", "cave1_floor1", "cave1_floor1", "cave1_floor1", "cave1_floor1", "cave1_floor1", "cave1_left1", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2"},
                              //12
                              {"cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_up1", "cave1_up1", "cave1_up1", "cave1_up1", "cave1_entrance1", "cave1_up1", "cave1_up1", "cave1_up1", "cave1_up1", "cave1_up1", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2"},
                              //Padding
                              {"cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2"},
                              {"cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2"},
                              {"cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2"},
                              {"cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2"},
                              {"cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2"},
                              {"cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2"},
                              {"cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_right1", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2", "cave1_left1", "cave1_floor2"},
                          };
        currRoute = new Route("regi_cave1", 44);
        for (int i=0; i < names.length; i++) {
            for (int j=0; j < names[i].length; j++) {
                if (names[i][j] == null) {
                    continue;
                }
                pos = new Vector2(origin.x +j*16, origin.y -i*16);
                Tile tile = new Tile(names[i][j], pos, true, currRoute);
                // These are the regis that haven't been crafted yet.
                if (names[i][j].equals("cave1_regipedistal1")) {
                    tile.items().put("REGISTEEL", 1);
                    tile.items().put("REGICE", 1);
                    tile.items().put("REGIROCK", 1);
                    tile.items().put("REGIELEKI", 1);
                    tile.items().put("REGIDRAGO", 1);
                }
                currLayer.put(pos, tile);
            }
        }
        interiorTiles.add(currLayer);

        // TODO: not sure if needed
        for (Tile tile : exteriorTiles.values()) {
            Tile currTile = this.tilesToAdd.get(tile.position);
            if (currTile != null && currTile.nameUpper.equals("pokemon_mansion_key")) {
                this.tilesToAdd.put(tile.position.cpy(), new Tile(tile.name, currTile.nameUpper, currTile.position, true, currTile.routeBelongsTo));
                continue;
            }
            this.tilesToAdd.put(tile.position.cpy(), tile);
        }

        for (int i=0; i < interiorTiles.size(); i++) {
            currLayer = interiorTiles.get(i);
            if (i >= this.interiorTilesToAdd.size()) {
                this.interiorTilesToAdd.add(currLayer);
                continue;
            }
            if (currLayer == null) {
                continue;
            }
            if (this.interiorTilesToAdd.get(i) == null) {
                this.interiorTilesToAdd.remove(i);
                this.interiorTilesToAdd.add(i, currLayer);
                continue;
            }
            for (Vector2 key : currLayer.keySet()) {
                this.interiorTilesToAdd.get(i).put(key, currLayer.get(key));
            }
        }
    }

    /**
     * Desert oasis.
     */
    public void generateOasis(Game game, Vector2 origin) {
        Route blotchRoute = new Route("oasis1", 30);
//        Tile newTile = desertTiles.remove(this.rand.nextInt(desertTiles.size()));  // TODO: remove
        Tile newTile = this.tilesToAdd.get(origin);
        newTile.biome = "desert";
        int nextSize = 250 + this.rand.nextInt(50);
        HashMap<Vector2, Tile> newTiles = new HashMap<Vector2, Tile>();
        ApplyBlotch(game, "oasis1", newTile, nextSize, newTiles, 0, true, blotchRoute);
        HashMap<Vector2, Tile> newTiles2 = new HashMap<Vector2, Tile>();
        nextSize = nextSize/2;
        ApplyBlotch(game, "pond1", newTile, nextSize, newTiles2, 0, true, blotchRoute);
        // Milotic in middle of pond.
        Pokemon pokemon = new Pokemon("milotic", 44);
        pokemon.position = newTile.position.cpy();
        pokemon.mapTiles = game.map.overworldTiles;
        pokemon.standingAction = pokemon.new Standing();
        this.pokemonToAdd.put(pokemon.position.cpy(), pokemon);
        //
        newTiles.putAll(newTiles2);
        for (Tile tile2 : new ArrayList<Tile>(newTiles.values())) {
            if (tile2.nameUpper.equals("aloe_large1")) {
                newTiles.put(tile2.position.cpy().add(16, 0), new Tile("green1", "solid", tile2.position.cpy().add(16, 0), true, tile2.routeBelongsTo));
                newTiles.put(tile2.position.cpy().add(0, 16), new Tile("green1", "solid", tile2.position.cpy().add(0, 16), true, tile2.routeBelongsTo));
                newTiles.put(tile2.position.cpy().add(16,16), new Tile("green1", "solid", tile2.position.cpy().add(16,16), true, tile2.routeBelongsTo));
            }
        }
        // Don't overwrite mountains.
        ArrayList<Tile> oasisTiles = new ArrayList<Tile>();
        for (Tile tile : newTiles.values()) {
//            Tile currTile = this.tilesToAdd.get(tile.position);
//            if (currTile != null) {
//                if (currTile.name.contains("mountain") || currTile.nameUpper.contains("mountain")) {
//                    continue;
//                }
//            }
            if (this.mtnTiles.containsKey(tile.position)) {
                continue;
            }
            if (!tile.name.contains("water")) {
                oasisTiles.add(tile);
            }
            this.tilesToAdd.put(tile.position.cpy(), tile);
        }

        // Start fossil building stuff
        newTile = oasisTiles.remove(this.rand.nextInt(oasisTiles.size()));
        HashMap<Vector2, Tile> exteriorTiles = new HashMap<Vector2, Tile>();
        ArrayList<HashMap<Vector2, Tile>> interiorTiles = new ArrayList<HashMap<Vector2, Tile>>();
        for (int i=0; i < 100; i++) {
            interiorTiles.add(null);
        }
        HashMap<Vector2, Tile> currLayer = new HashMap<Vector2, Tile>();
        Vector2 tl2 = newTile.position.cpy();
        String[][] names = new String[][]{{"building1_wall1", "building1_wall1", "building1_wall1", "building1_wall1", "building1_wall1", "building1_wall1"},
                                          {"building1_machine2", null, null, null, null, null},
                                          {"building1_fossilreviver1", "building1_cables1", "building1_cables1", "building1_cables1", "building1_pokecenter1", "building1_pokecenter1_right"},
                                          {"building1_floor2", "building1_floor1", "rug2", "building1_floor1", "building1_floor2", "building1_floor2"}
                                          };
        Route interiorRoute = new Route("fossil_lab1", 11);
        Vector2 pos2;
        for (int i=0; i < names.length; i++) {
            for (int j=0; j < names[i].length; j++) {
                if (names[i][j] == null) {
                    continue;
                }
                pos2 = new Vector2(tl2.x -6*16 +j*16, tl2.y +5*16 -i*16);
                currLayer.put(pos2, new Tile(names[i][j], pos2, true, interiorRoute));
            }
        }
        interiorTiles.add(currLayer);
        names = new String[][]{{"pkmnmansion_roof_NW", "pkmnmansion_roof_N", "pkmnmansion_roof_N", "pkmnmansion_roof_N_damaged", "pkmnmansion_roof_N", "pkmnmansion_roof_NE"},
                               {"pkmnmansion_roof_SW", "pkmnmansion_roof_S_damaged", "pkmnmansion_roof_S", "pkmnmansion_roof_S", "pkmnmansion_roof_S", "pkmnmansion_roof_SE"},
                               {"pkmnmansion_ext_W_window", "pkmnmansion_ext_windows", "pkmnmansion_ext_windows_W_damaged", "pkmnmansion_ext_windows_E_damaged", "pkmnmansion_ext_windows", "pkmnmansion_ext_E_window_damaged"},
                               {"pkmnmansion_ext_SW", "pkmnmansion_ext_S", "pkmnmansion_ext_door", "pkmnmansion_ext_S_windows_damaged", "pkmnmansion_ext_S", "pkmnmansion_ext_SE_damaged"},
                               {"sand1", "sand1", "sand1", "sand1", "sand1", "sand1"},
                               };
        for (int i=0; i < names.length; i++) {
            for (int j=0; j < names[i].length; j++) {
                if (names[i][j] == null) {
                    continue;
                }
                pos2 = new Vector2(tl2.x -6*16 +j*16, tl2.y +5*16 -i*16);

                // TODO: I think this is broken. Did I intend to do this.tilesToAdd.get(pos2) instead?
                // Below block is to get rid of aloe plants
                Tile currTile = exteriorTiles.get(pos2);
                if (currTile != null && currTile.name.equals("aloe_large1")) {
                    Vector2[] nexts = new Vector2[]{new Vector2(16, 0), new Vector2(0, 16), new Vector2(16, 16)};
                    for (Vector2 next : nexts) {
                        Vector2 nextPos = pos2.cpy().add(next);
                        Tile nextTile = exteriorTiles.get(nextPos);
                        if (nextTile.nameUpper.equals("solid")) {
                            nextTile.nameUpper = "";
                            nextTile.attrs.put("solid", false);
                        }
                    }
                }
                exteriorTiles.put(pos2, new Tile(names[i][j], pos2, true, newTile.routeBelongsTo));
            }
        }
        // TODO: I don't think the secret key dodging stuff is used anymore
        // Actually it might be
        for (Tile tile2 : exteriorTiles.values()) {
            Tile currTile = this.tilesToAdd.get(tile2.position);
            if (currTile != null && currTile.nameUpper.equals("pokemon_mansion_key")) {
                this.tilesToAdd.put(tile2.position.cpy(), new Tile(tile2.name, currTile.nameUpper, currTile.position, true, currTile.routeBelongsTo));
                continue;
            }
            this.tilesToAdd.put(tile2.position.cpy(), tile2);
        }
        for (int i=0; i < interiorTiles.size(); i++) {
            currLayer = interiorTiles.get(i);
            if (i >= this.interiorTilesToAdd.size()) {
                this.interiorTilesToAdd.add(currLayer);
                continue;
            }
            if (currLayer == null) {
                continue;
            }
            if (this.interiorTilesToAdd.get(i) == null) {
                this.interiorTilesToAdd.remove(i);
                this.interiorTilesToAdd.add(i, currLayer);
                continue;
            }
            for (Vector2 key : currLayer.keySet()) {
                this.interiorTilesToAdd.get(i).put(key, currLayer.get(key));
            }
        }
    }

    /**
     * Ruins exterior.
     */
    public void generateDesertRuins(Game game, Vector2 origin) {
        // Start ruins upper/outer placement
        int nextSize = 230;
        Route currRoute = new Route("ruins1_outer", 25);  // 44

        // TODO: remove
//        int tries = 0;
//        Tile newTile = desertTiles.remove(this.rand.nextInt(desertTiles.size()));
//        while (tries < 80 && desertTiles.size() > 0) {
//            // newTile.position.x % 64 == 0 looked neat
////             newTile.position.x % 64 == 32
//            if (newTile.position.x % 32 == 0 && newTile.position.y % 32 == 16) {  
//                break;
//            }
//            newTile = desertTiles.remove(this.rand.nextInt(desertTiles.size()));
//            tries++;
//        }
        //
        Tile newTile = this.tilesToAdd.get(origin);
        HashMap<Vector2, Tile> newTiles = new HashMap<Vector2, Tile>();
        HashMap<Vector2, Tile> newTiles2 = new HashMap<Vector2, Tile>();
        ApplyBlotch(game, "ruins1_upper2", newTile, nextSize, newTiles2, 0, false, currRoute);
        newTiles.putAll(newTiles2);
        newTiles2.clear();
        nextSize = 240;
        ApplyBlotch(game, "ruins1_upper", newTile, nextSize, newTiles2, 0, false, currRoute);
        newTiles.putAll(newTiles2);
        // Ruins tiles need to be adjusted for surrounding tiles.
        for (Tile tile : newTiles.values()) {
            if (!tile.name.contains("ruins1")) {
                continue;
            }
            game.map.adjustSurroundingTiles(tile, newTiles);
        }
        // Don't overwrite mountains
        for (Tile tile : newTiles.values()) {
            if (this.mtnTiles.containsKey(tile.position)) {
                continue;
            }
            this.tilesToAdd.put(tile.position.cpy(), tile);
        }
        // Add middle stairs area.
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                Tile currTile = this.tilesToAdd.get(newTile.position.cpy().add(16*i, 16*j));
                if (!currTile.name.equals("desert4")) {
                    this.tilesToAdd.put(currTile.position.cpy(), new Tile("ruins_floor2", currTile.position.cpy(), true, null));
                }
            }
        }
        Vector2[] positions2 = new Vector2[]{newTile.position.cpy().add(-32, 16),
                                             newTile.position.cpy().add(32, 16),
                                             newTile.position.cpy().add(-32, -16),
                                             newTile.position.cpy().add(32, -16)};
        for (Vector2 position : positions2) {
            if (this.rand.nextInt(3) > 0) {
                this.tilesToAdd.put(position.cpy(), new Tile("ruins1_pillar1", position.cpy(), true, null));
            }
        }
        this.tilesToAdd.put(newTile.position.cpy(), new Tile("ruins1_NSEW", "stairs_down2", newTile.position.cpy(), true, currRoute));

        // Generate the ruins interior
        this.generateRuinsInterior(newTile.position.cpy());
    }

    /**
     * Ruins interior. Currently 5 levels.
     * 
     * Levels 1-2 -> Wild encounters enabled, no dusclops.
     * Levels 3-4 -> Wild encounters disabled, roaming dusclops.
     * Level 5 - Volcarona room.
     */
    public void generateRuinsInterior(Vector2 origin) {
        // Array representation of the volcarona room.
        String[][] names = new String[][]{{"ruins2_wall1", "ruins2_wall1", "ruins2_wall1", "ruins2_wall1", "ruins2_wall1", "ruins2_wall1", "ruins2_wall1", "ruins2_wall1", "ruins2_wall1"},
                                          {"ruins2_wall1", "ruins2_wall1", "ruins2_wall1", "ruins2_volcarona_picture1", null, null, "ruins2_wall1", "ruins2_wall1", "ruins2_wall1"},
                                          {"ruins_floor2", "ruins_floor2", "ruins_floor2", "ruins2_path1", "blank1", "blank1",  "ruins_floor2", "ruins_floor2", "ruins_floor2"},
                                          {"ruins_floor2", "pedistal1", "ruins_floor2", "ruins2_path1", "blank1", "blank1",  "ruins_floor2", "pedistal1", "ruins_floor2"},
                                          {"ruins_floor2", "ruins_floor2", "pedistal1", "ruins2_path1", "blank1", "blank1",  "pedistal1", "ruins_floor2", "ruins_floor2"},
                                          {"ruins_floor2", "ruins_floor2", "ruins_floor2", "ruins2_path1", "blank1", "blank1",  "ruins_floor2", "ruins_floor2", "ruins_floor2"},
                                          {"ruins_floor2", "ruins1_pillar1", "ruins_floor2", "ruins2_path1", "blank1", "blank1", "ruins_floor2", "ruins1_pillar1", "ruins_floor2"},
                                          {"ruins_floor2", "ruins_floor2", "ruins_floor2", "ruins2_path1", "blank1", "blank1",  "ruins_floor2", "ruins_floor2", "ruins_floor2"},
                                          {"ruins_floor2", "ruins_floor2", "ruins_floor2", "ruins2_path1", "blank1", "blank1",  "ruins_floor2", "ruins_floor2", "ruins_floor2"},
                                          {"ruins_floor2", "ruins_floor2", "ruins_floor2", "ruins2_path1", "blank1", "blank1",  "ruins_floor2", "ruins_floor2", "ruins_floor2"},
                                          {"ruins_floor2", "ruins_floor2", "ruins1_pillar1", "ruins2_path1", "blank1", "blank1", "ruins1_pillar1", "ruins_floor2", "ruins_floor2"},
                                          {"ruins_floor2", "ruins_floor2", "ruins_floor2", "ruins2_path1", "blank1", "blank1",  "ruins_floor2", "ruins_floor2", "ruins_floor2"},
                                          {"ruins_floor2", "ruins_floor2", "ruins_floor2", "ruins2_path1", "blank1", "blank1",  "ruins_floor2", "ruins_floor2", "ruins_floor2"},
                                          {"ruins_floor2", "ruins_floor2", "ruins_floor2", "ruins2_path1", "blank1", "blank1",  "ruins_floor2", "ruins_floor2", "ruins_floor2"},
                                          {"ruins_floor2", "ruins1_pillar1", "ruins_floor2", "ruins2_path1", "blank1", "blank1", "ruins_floor2", "ruins1_pillar1", "ruins_floor2"},
                                          {"ruins_floor2", "ruins_floor2", "ruins_floor2", "ruins2_path1", "blank1", "blank1",  "ruins_floor2", "ruins_floor2", "ruins_floor2"},
                                          {null, null, null, "ruins2_path1", "ruins_floor2_stairs", "blank1", null, null, null, null}};
        // Generate the ruins interior
        ArrayList<HashMap<Vector2, Tile>> interiorTiles = new ArrayList<HashMap<Vector2, Tile>>();
        for (int i=0; i < 5; i++) {
            interiorTiles.add(new HashMap<Vector2, Tile>());
        }
        HashMap<Vector2, Tile> currLayer;
        Vector2 startLoc = new Vector2(10, 10);
        Vector2 bl = origin.add((-10*3-1)*16, (-10*3-1)*16);
        ArrayList<Vector2> startLocs = new ArrayList<Vector2>();
        ArrayList<Vector2> endPoints = new ArrayList<Vector2>();
        ArrayList<Vector2> dusclopsPositions = new ArrayList<Vector2>();
        endPoints.add(startLoc);
        boolean[][] maze;
        Route currRoute = new Route("ruins1_inner", 22);

        // TODO: remove
//        currRoute.genPokemon(256, false);  // Don't evolve pokemon in the route
        // Start from top level, work downward
        for (int levelNum=interiorTiles.size()-1; levelNum >= 0; levelNum--) {
            currLayer = interiorTiles.get(levelNum);

            // Final layer so make volcarona room
            if (levelNum == 0) {
                for (int i=0; i < names.length; i++) {
                    for (int j=0; j < names[i].length; j++) {
                        if (names[i][j] == null) {
                            continue;
                        }
                        Vector2 startPos = endPoints.get(0);
                        Vector2 position = bl.cpy().add( (startPos.x*3 -3 +j)*16, (startPos.y*3 +17 -i)*16 );
                        Tile newTile;
                        if (names[i][j].equals("ruins_floor2_stairs")) {
                            newTile = new Tile("blank1", "stairs_up1", position.cpy(), true, currRoute);
                        }
                        else if (names[i][j].equals("pedistal1")) {
                            newTile = new Tile("ruins_floor2", "pedistal1", position.cpy(), true, currRoute);
                        }
                        else {
                            newTile = new Tile(names[i][j], position.cpy(), true, currRoute);
                        }
                        if (names[i][j].contains("pillar")) {
                            newTile.items().put("torch", 1);
                        }
                        if (i == 2 && j == 4) {
                            newTile.nameUpper = "volcarona";
                        }
                        currLayer.put(position, newTile);
                    }
                }
                continue;
            }

            // 3 start locations to branch from
            float complexity = 15;
            for (Vector2 position : endPoints) {
                String nameUpper = "stairs_up1";
                if (levelNum == interiorTiles.size()-1) {
                    // _exit denotes that player should switch to overworldTiles after going through.
                    nameUpper = "stairs_up1_exit";
                }
                currLayer.put(bl.cpy().add((position.x*3+1)*16, (position.y*3+1)*16), new Tile("desert4", nameUpper, bl.cpy().add((position.x*3+1)*16, (position.y*3+1)*16), true, currRoute));
                startLocs.add(position);
                startLocs.add(position);
                if (this.rand.nextInt(2) == 0 || levelNum == interiorTiles.size()-1) {
                    startLocs.add(position);
                    if (levelNum != interiorTiles.size()-1) {
                        complexity = 10;
                    }
                }
            }
            endPoints.clear();
            
            // Generate a maze
            int width = 20;
            int height = 20;
            float density = startLocs.size();
            maze = GenForest2.Maze_Algo3(width, height, complexity, density, this.rand, startLocs);
//            System.out.println("startLocs.size()");
//            System.out.println(startLocs.size());
            // Debug - remove
            System.out.print("\n");
            System.out.print("\n");

            for (int j=maze.length-1; j >= 0; j--) {
                for (int i=0; i < maze[j].length; i++) {
//                    maze[j][i] = !maze[j][i];
                    System.out.print(String.valueOf(maze[i][j] ? 1 : " ")+" ");
                }
                System.out.print("\n");
            }
            
            dusclopsPositions.clear();

            // 
            for (int i=0; i < maze.length; i++) {
                for (int j=0; j < maze[i].length; j++) {
                    if (maze[i][j]) {
                        // This will overwrite stairs going up otherwise.
                        // 3x3 grid of stuff
                        for (int k=0; k < 3; k++) {
                            for (int l=0; l < 4; l++) {
                                Vector2 position = bl.cpy().add((i*3+k)*16, (j*3+l)*16);
                                Tile currTile = currLayer.get(position);
                                if (currTile == null || currTile.name.contains("wall")) {
                                    if (l == 3) {
                                        // ruins2_wall2
                                        Tile newTile = new Tile("ruins2_wall1", position.cpy(), true, null);
                                        if (levelNum != interiorTiles.size()-1 && i % 2 == 0 && k == 1 && this.rand.nextInt(4)> 0) {
                                            newTile.items().put("torch", 1);
                                        }
                                        currLayer.put(position, newTile);
                                    }
                                    else {
                                        String name = "desert4";
                                        // TODO: remove
                                        // Mod function was an attempt to make it 'splotch'
                                        // this.rand.nextInt(6 +2*Math.abs((i%8)-4)) > 5 -- floor 2
                                        if (levelNum == interiorTiles.size()-1) {
                                            name = "desert4_isGrass";
                                            // Just desert for now
//                                            if (this.rand.nextInt(26) == 0) { // old method
                                            int offset = +Math.abs(((i*3+k)%12)-6) +Math.abs(((j*3+l)%12)-6);
                                            if (this.rand.nextInt(offset+1)+offset > 14) {
                                                name = "ruins_floor2";
                                            }
                                        }
                                        else if (levelNum == interiorTiles.size()-2){
                                            name = "desert4_isGrass";
//                                            if (this.rand.nextInt(6 +2*Math.abs((i%8)-4)) > 5) {
                                            int offset = +Math.abs(((i*3+k)%12)-6) +Math.abs(((j*3+l)%12)-6);
                                            if (this.rand.nextInt(offset+1)+offset > 10) {
                                                name = "ruins_floor2";
                                            }
                                        }
                                        else if (levelNum == interiorTiles.size()-3){
//                                            if (this.rand.nextInt(6 +2*Math.abs((i%8)-4)) > 1) {
                                            int offset = +Math.abs(((i*3+k)%12)-6) +Math.abs(((j*3+l)%12)-6);
                                            if (this.rand.nextInt(offset+1)+offset > 7) {
                                                name = "ruins_floor2";
                                            }
                                        }
                                        else { 
                                            // TODO: remove, no chance for desert tiles anymore
//                                            if (this.rand.nextInt(26) > 0) {
//                                                name = "ruins_floor2";
//                                            }
                                            name = "ruins_floor2";
                                        }
                                        currLayer.put(position, new Tile(name, position.cpy(), true, currRoute));
                                        
                                    }
                                    // Only on bottom two layers (before volcarona room)
                                    // && this.rand.nextInt(4) == 0
                                    if (levelNum < 3 && k == 1 && l == 1 && i % 3 == 0 && j % 3 == 0) {

                                        dusclopsPositions.add(position);
                                    }
                                }
                            }
                        }

                        // Jar placement
                        if (!startLocs.contains(new Vector2(i, j)) && i % 2 == 0 && j % 2 == 0 && this.rand.nextInt(3) == 0) {
                            ArrayList<Vector2> potPositions = new ArrayList<Vector2>();
                            // down closed
                            if (j-1 >= 0 && !maze[i][j-1]) {
                                potPositions.add(bl.cpy().add((i*3+0)*16, (j*3+0)*16));
                                potPositions.add(bl.cpy().add((i*3+1)*16, (j*3+0)*16));
                                potPositions.add(bl.cpy().add((i*3+2)*16, (j*3+0)*16));
                            }
                            // up closed
                            if (j+1 < height && !maze[i][j+1]) {
                                potPositions.add(bl.cpy().add((i*3+0)*16, (j*3+2)*16));
                                potPositions.add(bl.cpy().add((i*3+1)*16, (j*3+2)*16));
                                potPositions.add(bl.cpy().add((i*3+2)*16, (j*3+2)*16));
                            }
                            // right closed
                            if (i+1 < width && !maze[i+1][j]) {
                                potPositions.add(bl.cpy().add((i*3+2)*16, (j*3+0)*16));
                                potPositions.add(bl.cpy().add((i*3+2)*16, (j*3+1)*16));
                                potPositions.add(bl.cpy().add((i*3+2)*16, (j*3+2)*16));
                            }
                            // left closed
                            if (i-1 >= 0 && !maze[i-1][j]) {
                                potPositions.add(bl.cpy().add((i*3+0)*16, (j*3+0)*16));
                                potPositions.add(bl.cpy().add((i*3+0)*16, (j*3+1)*16));
                                potPositions.add(bl.cpy().add((i*3+0)*16, (j*3+2)*16));
                            }
                            for (Vector2 position : potPositions) {
                                if (this.rand.nextInt(7) > 3) {
                                    Tile tile = currLayer.get(position);
                                    tile.nameUpper = "pot1";
                                    tile.init(tile.name, tile.nameUpper, tile.position, true, tile.routeBelongsTo);
                                }
                            }
                        }
                        // Statues
                        else if (levelNum == 1 && i % 2 == 0 && j % 2 == 0 && this.rand.nextInt(2) == 0) {
                            Vector2 position = bl.cpy().add((i*3+1)*16, (j*3+1)*16);
                            Tile tile = currLayer.get(position);
                            // Prevent it from going over-top a staircase
                            if (tile.nameUpper.equals("")) {
                                tile.nameUpper = "ruins_statue1";
                                tile.init(tile.name, tile.nameUpper, tile.position, true, tile.routeBelongsTo);
                            }
                        }
                    }
                }
            }
            
            int size = dusclopsPositions.size();
            for (int i=0; i < 2 + this.rand.nextInt(3) && i < size; i++) {
                Vector2 position = dusclopsPositions.remove(this.rand.nextInt(dusclopsPositions.size()));
                int interiorIndex = levelNum +100 -5;
                Pokemon pokemon = new Pokemon("dusclops", 44, Pokemon.Generation.CRYSTAL);
                pokemon.position = position.cpy();
                pokemon.standingAction = pokemon.new Standing();
                pokemon.aggroPlayer = true;
//                pokemon.mapTiles = game.map.interiorTiles.get(interiorIndex);  // TODO: this doesn't work either.
//                game.insertAction(pokemon.standingAction);  // TODO: probably doesn't work
                // TODO: what if pokemon position overlaps with another?
//              game.map.pokemon.put(position.cpy(), pokemon);
                this.interiorPokemon.put(pokemon, interiorIndex);
            }

            // Pick two endpoints to be stairs
            size = startLocs.size();
            int numStairs = 2;
            // Only place 1 stairs on final level
            if (levelNum == 1) {
                numStairs = 1;
            }
            for (int i=0; i < size; i++) {
                startLoc = startLocs.remove(this.rand.nextInt(startLocs.size()));
                System.out.println(startLoc);
                if (i < numStairs) {
                    for (int k=0; k < 3; k++) {
                        for (int l=0; l < 3; l++) {
                            Vector2 position = bl.cpy().add((startLoc.x*3+k)*16, (startLoc.y*3+l)*16);
                            if (k == 1 && l == 1) {
                                currLayer.put(position.cpy(), new Tile("ruins_floor2", "stairs_down2", position.cpy(), true, currRoute));
                            }
                            else if (this.rand.nextInt(5) > 1) {
                                currLayer.put(position.cpy(), new Tile("ruins_floor2", position.cpy(), true, currRoute));
                            }
                            else {
                                currLayer.put(position.cpy(), new Tile("desert4", position.cpy(), true, currRoute));
                                
                            }
                        }
                    }
                    endPoints.add(startLoc);
                }
                else {
                    int numPots = 0;
                    for (int k=0; k < 3; k++) {
                        for (int l=0; l < 3; l++) {
                            Vector2 position = bl.cpy().add((startLoc.x*3+k)*16, (startLoc.y*3+l)*16);
                            Tile tile = currLayer.get(position);
                            if (k == 1 && l == 1) {
                                // Don't put over a stairscase
                                // But should put over a statue
                                if (tile != null && !tile.nameUpper.contains("stairs")) {
                                    tile.nameUpper = "pokeball1";
                                    tile.init(tile.name, tile.nameUpper, tile.position, true, tile.routeBelongsTo);
                                    if (this.rand.nextInt(2) == 0) {
                                        tile.hasItem = "ultra ball";
                                        tile.hasItemAmount = 2;
                                    }
                                    else {
                                        tile.hasItem = "ancientpowder";
                                        tile.hasItemAmount = this.rand.nextInt(3) +2;
                                    }
                                }
                            }
                            // TODO: had chance to block path, remove
                            else if (this.rand.nextInt(7) > 3 && numPots < 2) {
                                currLayer.put(position.cpy(), new Tile(tile.name, "pot1", position.cpy(), true, currRoute));
                                numPots++;
                            }
                        }
                    }
                }
            }
            startLocs.clear();
        }
        // Do this because stairs will go down one level
        interiorTiles.add(new HashMap<Vector2, Tile>());

        // Add all generated interior layers to map interior tiles.
        for (int i=0; i < interiorTiles.size(); i++) {
            currLayer = interiorTiles.get(i);
            int interiorIndex = i +100 -5;
            if (interiorIndex >= this.interiorTilesToAdd.size()) {
                this.interiorTilesToAdd.add(currLayer);
                continue;
            }
            if (this.interiorTilesToAdd.get(interiorIndex) == null) {
                this.interiorTilesToAdd.remove(interiorIndex);
                this.interiorTilesToAdd.add(interiorIndex, currLayer);
                continue;
            }
            for (Vector2 key : currLayer.keySet()) {
                this.interiorTilesToAdd.get(interiorIndex).put(key, currLayer.get(key));
            }
        }
    }

    /**
     * Generate pokemon mansion dungeon to be placed on the map:
     *  - Generate the exterior (including locked door)
     *  - Generate 5 interior levels: 2F, 1F, 1B, 2B, 3B, 4B.
     *  - Starting with the top level, add stairs, statues and doors.
     *  - Add the 5B level which contains armored mewtwo.
     */
    public void generateMansion(Game game,
                                HashMap<Vector2, Tile> mansionExteriorTiles,
                                ArrayList<HashMap<Vector2, Tile>> mansionInteriorTiles,
                                Vector2 bl) {
        int height = 28;
        int width = 30;
        int doWindows = 0; //this.rand.nextInt(3);
        bl.add(-(width*16)/2, -(height*16)/2);  // centered
        Tile prevStatue = null;
        Route currRoute = new Route("snow1", 40);

        // 6 levels
        for (int i=0; i < 100-5; i++) {  // 100
            mansionInteriorTiles.add(null);
        }
        mansionInteriorTiles.add(new HashMap<Vector2, Tile>());  // basement thing

        // Create exterior
        for (int i = -8; i <= height; i++) {
            doWindows = this.rand.nextInt(2) + 1;
            for (int j = 0; j <= width; j++) {
                if (j == width-1) {
                    doWindows = 1;
                }
                if (i < 0 && (j == width/2 || j == width/2 +1)) {
                    String name = "path1";
                    if (this.rand.nextInt(6) <= 1) {
                        name = "sand1";
                    }
                    mansionExteriorTiles.put(bl.cpy().add(j*16, i*16), new Tile(name, bl.cpy().add(j*16, i*16), true, null));
                }
                else if (i == -2) {
                    String name = "green1";
                    if (this.rand.nextInt(24) == 0) {
                        name = "flower4";
                    }
                    mansionExteriorTiles.put(bl.cpy().add(j*16, i*16), new Tile(name, bl.cpy().add(j*16, i*16), true, null));
                }
                else if (i == -1) {
                    mansionExteriorTiles.put(bl.cpy().add(j*16, i*16), new Tile("sand1", bl.cpy().add(j*16, i*16), true, null));
                }
                else if (i == 0) {
                    if (j == 0) {
                        mansionExteriorTiles.put(bl.cpy().add(j*16, i*16), new Tile("pkmnmansion_ext_SW", bl.cpy().add(j*16, i*16), true, null));
                    }
                    else if (j == width) {
                        mansionExteriorTiles.put(bl.cpy().add(j*16, i*16), new Tile("pkmnmansion_ext_SE", bl.cpy().add(j*16, i*16), true, null));
                    }
                    else if (j == (width/2)) {
                        mansionExteriorTiles.put(bl.cpy().add(j*16, i*16), new Tile("pkmnmansion_ext_locked", bl.cpy().add(j*16, i*16), true, currRoute));
                    }
                    else {
                        String name = "pkmnmansion_ext_S";
                        if (doWindows != 0) {
                            name = "pkmnmansion_ext_S_windows";
                        }
                        mansionExteriorTiles.put(bl.cpy().add(j*16, i*16), new Tile(name,  bl.cpy().add(j*16, i*16), true, null));
                    }
                }
                else if (i == 1 || i == 2) {
                    if (j == 0) {
                        mansionExteriorTiles.put(bl.cpy().add(j*16, i*16), new Tile("pkmnmansion_ext_W", bl.cpy().add(j*16, i*16), true, null));
                    }
                    else if (j == width) {
                        mansionExteriorTiles.put(bl.cpy().add(j*16, i*16), new Tile("pkmnmansion_ext_E", bl.cpy().add(j*16, i*16), true, null));
                    }
                    else {
                        String name = "pkmnmansion_ext";
                        if (doWindows != 0) {
                            name = "pkmnmansion_ext_windows";
                        }
                        mansionExteriorTiles.put(bl.cpy().add(j*16, i*16), new Tile(name,  bl.cpy().add(j*16, i*16), true, null));
                    }
                }
                else if (i == 3) {
                    if (j == 0) {
                        mansionExteriorTiles.put(bl.cpy().add(j*16, i*16), new Tile("pkmnmansion_roof_SW", bl.cpy().add(j*16, i*16), true, null));
                    }
                    else if (j == width) {
                        mansionExteriorTiles.put(bl.cpy().add(j*16, i*16), new Tile("pkmnmansion_roof_SE", bl.cpy().add(j*16, i*16), true, null));
                    }
                    else {
                        mansionExteriorTiles.put(bl.cpy().add(j*16, i*16), new Tile("pkmnmansion_roof_S",  bl.cpy().add(j*16, i*16), true, null));
                    }
                }
                else if (i == height) {
                    if (j == 0) {
                        mansionExteriorTiles.put(bl.cpy().add(j*16, i*16), new Tile("pkmnmansion_roof_NW", bl.cpy().add(j*16, i*16), true, null));
                    }
                    else if (j == width) {
                        mansionExteriorTiles.put(bl.cpy().add(j*16, i*16), new Tile("pkmnmansion_roof_NE", bl.cpy().add(j*16, i*16), true, null));
                    }
                    else {
                        mansionExteriorTiles.put(bl.cpy().add(j*16, i*16), new Tile("pkmnmansion_roof_N",  bl.cpy().add(j*16, i*16), true, null));
                    }
                }
                else if (i > 0) {
                    if (j == 0) {
                        mansionExteriorTiles.put(bl.cpy().add(j*16, i*16), new Tile("pkmnmansion_roof_W", bl.cpy().add(j*16, i*16), true, null));
                    }
                    else if (j == width) {
                        mansionExteriorTiles.put(bl.cpy().add(j*16, i*16), new Tile("pkmnmansion_roof_E", bl.cpy().add(j*16, i*16), true, null));
                    }
                    else {
                        mansionExteriorTiles.put(bl.cpy().add(j*16, i*16), new Tile("pkmnmansion_roof",  bl.cpy().add(j*16, i*16), true, null));
                    }
                }

                if (this.rand.nextInt(1+(doWindows)) == 0) {
                    doWindows = (doWindows+1) % 2;
                }
            }
        }

        ArrayList<Vector2> alreadyChecked = new ArrayList<Vector2>();
        Tile stairsUp = null;  // Stairs tile going upstairs. Top floor won't have this.
        Tile stairsDown = null; 
        Tile statue = null; 
        // bl vector2 needs to move
        bl.add(-4*16, -4*16);
        // start from bottom basement level
        for (int levelNum=0; levelNum < 6; levelNum++) {
            // Always assume there is a wall
            // Needs to fit into 30x28
            // height: 28, -2 for top/bottom walls. 26 spaces. 27 since maze generates bottom wall
            // 1. then 1-5. 
            // should it just be true/false? yeah. true == solid
            // 
            currRoute = new Route("pkmnmansion1", 40 -levelNum*2);  // TODO: up level later
            stairsUp = null;  // Stairs tile going upstairs. Top floor won't have this.
            stairsDown = null; 
            statue = null; 
            ArrayList<Boolean> rowIsSolid = new ArrayList<Boolean>();
            ArrayList<Integer> sizes = new ArrayList<Integer>();
            sizes.add(1);
            for (int k = 0; k < 2; k++) {
                sizes.add(2);
    //            sizes.add(3);
                sizes.add(4);
                sizes.add(5);
            }
            if (levelNum < 4) {
                sizes.add(4);
                sizes.add(2);
                sizes.add(4);
                sizes.add(2);
            }
            int numRows = 0;
            int counter = 0;
            for (int i = 0; i < 27 +(4-levelNum)*2; i++) {
                if (i == 26 +(4-levelNum)*2) {
                    rowIsSolid.add(false);
                    continue;
                }
                if (counter == 0) {
                    rowIsSolid.add(true);
    //                int min = 1;
    //                if (i < 8) {
    //                    min = 3;
    //                }
    //                counter = this.rand.nextInt(5) + min;  // 5 == max open area height
                    counter = sizes.remove(this.rand.nextInt(sizes.size()));
                    numRows+=2;
                    continue;
                }
                else {
                    rowIsSolid.add(false);
                }
                counter--;
            }
            rowIsSolid.add(true);  // TODO: needed? idk
    //        System.out.println(rowIsSolid.size()); // check that this is 28
    
            ArrayList<Boolean> columnIsSolid = new ArrayList<Boolean>();
            sizes = new ArrayList<Integer>();
            sizes.add(1);
            for (int k = 0; k < 2; k++) {
                sizes.add(2);
    //            sizes.add(3);
                sizes.add(4);
                sizes.add(5);
            }
            if (levelNum < 4) {
                sizes.add(4);
                sizes.add(2);
                sizes.add(4);
                sizes.add(2);
            }
            int numCols = 0;
            counter = 0;
            for (int i = 0; i < 29 +(4-levelNum)*2; i++) {
                if (i == 28 +(4-levelNum)*2) {
                    columnIsSolid.add(false);
                    continue;
                }
                if (counter == 0) {
                    columnIsSolid.add(true);
    //                counter = this.rand.nextInt(5) + 2;  // 5 == max open area width
                    counter = sizes.remove(this.rand.nextInt(sizes.size()));
                    numCols += 2;
                    continue;
                }
                else {
                    columnIsSolid.add(false);
                }
                counter--;
            }
            columnIsSolid.add(true);
            
            // TODO: don't use mazenodes for now. might be useful later tho (probably not)
    
            // get maze use numRows and numCols
            // TODO: mess with those one values
    
    //        float density = 0.1f;
    //        float complexity = 0.9f;
    
            float density = 0.05f;
            float complexity = 0.7f;
            boolean[][] maze = GenForest2.Maze_Algo2(numCols, numRows, density, complexity, this.rand);
            HashMap<Vector2, Tile> currLayer = new HashMap<Vector2, Tile>();
            mansionInteriorTiles.add(currLayer);
    
            int i = 0;
            for (int k=0; k < rowIsSolid.size(); k++) {
                boolean rowSolid = rowIsSolid.get(k);
                int j = 0;
                for (int l=0; l < columnIsSolid.size(); l++) {
                    boolean columnSolid = columnIsSolid.get(l);
                    if (maze[j][i]) {
                        currLayer.put(bl.cpy().add(l*16, k*16), new Tile("pkmnmansion_wall",  bl.cpy().add(l*16, k*16), true, null));
                    }
                    else {
                        currLayer.put(bl.cpy().add(l*16, k*16), new Tile("pkmnmansion_floor1",  bl.cpy().add(l*16, k*16), true, currRoute));
                    }
                    if (columnSolid || (l+1 < columnIsSolid.size() && columnIsSolid.get(l+1))) {
                        j += 1;
                    }
                }
                if (rowSolid || (k+1 < rowIsSolid.size() && rowIsSolid.get(k+1))) {
                    i += 1;
                }
            }

            // Place plants
            alreadyChecked.clear();
            for (Vector2 pos : currLayer.keySet()) {
                if (alreadyChecked.contains(pos)) {
                    continue;
                }
                if (currLayer.get(pos).attrs.get("solid")) {
                    alreadyChecked.add(pos);
                    int size = 0;
                    ArrayList<Vector2> checkThese = new ArrayList<Vector2>();
                    ArrayList<Vector2> found = new ArrayList<Vector2>();
                    found.add(pos);
                    checkThese.add(pos.cpy().add(-16,0));
                    checkThese.add(pos.cpy().add(16,0));
                    checkThese.add(pos.cpy().add(0,16));
                    checkThese.add(pos.cpy().add(0,-16));
                    while (checkThese.size() > 0) {
                        Vector2 pos2 = checkThese.remove(0);
                        if (alreadyChecked.contains(pos2)) {
                            continue;
                        }
                        alreadyChecked.add(pos2);
                        if (!currLayer.containsKey(pos2)) {
                            continue;
                        }
                        if (!currLayer.get(pos2).attrs.get("solid")) {
                            continue;
                        }
                        size++;
                        found.add(pos2);
                        checkThese.add(pos2.cpy().add(-16,0));
                        checkThese.add(pos2.cpy().add(16,0));
                        checkThese.add(pos2.cpy().add(0,16));
                        checkThese.add(pos2.cpy().add(0,-16));
                    }
                    if (size < 10) {
                        for (Vector2 pos3 : found) {
                            currLayer.put(pos3.cpy(), new Tile("tree_plant1",  pos3.cpy(), true, null));
                        }
                    }
                }
            }

            // Fix walls
            for (Vector2 pos : currLayer.keySet()) {
                if (!currLayer.get(pos).name.contains("pkmnmansion_wall")) {
                    continue;
                }
    //            Tile tile = currLayer.get(pos);
                Vector2 left = pos.cpy().add(-16f, 0f);
                Vector2 right = pos.cpy().add(16f, 0f);
                Vector2 up = pos.cpy().add(0f, 16f);
                Vector2 down = pos.cpy().add(0f, -16f);
                boolean touchLeft = currLayer.containsKey(left) && currLayer.get(left).name.contains("pkmnmansion_wall");
                boolean touchRight = currLayer.containsKey(right) && currLayer.get(right).name.contains("pkmnmansion_wall");
                boolean touchUp = currLayer.containsKey(up) && currLayer.get(up).name.contains("pkmnmansion_wall");
                boolean touchDown = currLayer.containsKey(down) && currLayer.get(down).name.contains("pkmnmansion_wall");
                // TODO: choose some random
                // TODO: if touching solid up and down, do
                if (touchDown && touchUp) {
                    currLayer.put(pos.cpy(), new Tile("pkmnmansion_wall_NS", pos.cpy(), true, null));
                }
                else if (touchDown && touchLeft) {
                    if (this.rand.nextInt(2) == 0) {
                        currLayer.put(pos.cpy(), new Tile("pkmnmansion_wall_N", pos.cpy(), true, null));
                    }
                    else {
                        currLayer.put(pos.cpy(), new Tile("pkmnmansion_wall_NE", pos.cpy(), true, null));
                    }
                }
                else if (touchDown && touchRight) {
                    if (this.rand.nextInt(2) == 0) {
                        currLayer.put(pos.cpy(), new Tile("pkmnmansion_wall_N", pos.cpy(), true, null));
                    }
                    else {
                        currLayer.put(pos.cpy(), new Tile("pkmnmansion_wall_NW", pos.cpy(), true, null));
                    }
                }
                else if (touchDown) {
                    currLayer.put(pos.cpy(), new Tile("pkmnmansion_wall_N", pos.cpy(), true, null));
                }
                else if (touchUp && touchLeft) {
                    if (this.rand.nextInt(2) == 0) {
                        currLayer.put(pos.cpy(), new Tile("pkmnmansion_wall_S", pos.cpy(), true, null));
                    }
                    else {
                        currLayer.put(pos.cpy(), new Tile("pkmnmansion_wall_E", pos.cpy(), true, null));
                    }
                }
                else if (touchUp && touchRight) {
                    if (this.rand.nextInt(2) == 0) {
                        currLayer.put(pos.cpy(), new Tile("pkmnmansion_wall_S", pos.cpy(), true, null));
                    }
                    else {
                        currLayer.put(pos.cpy(), new Tile("pkmnmansion_wall_W", pos.cpy(), true, null));
                    }
                }
                else if (touchUp) {
                    currLayer.put(pos.cpy(), new Tile("pkmnmansion_wall_S", pos.cpy(), true, null));
                }
                else if (touchLeft && touchRight) {
                    // nothing
                }
                else if (touchLeft) {
                    currLayer.put(pos.cpy(), new Tile("pkmnmansion_wall_E", pos.cpy(), true, null));
                }
                else if (touchRight) {
                    currLayer.put(pos.cpy(), new Tile("pkmnmansion_wall_W", pos.cpy(), true, null));
                }
            }
    
            // Place debris
            alreadyChecked.clear();
            for (Vector2 pos : currLayer.keySet()) {
                if (alreadyChecked.contains(pos)) {
                    continue;
                }
                alreadyChecked.add(pos);
                if (currLayer.get(pos).name.contains("pkmnmansion_wall") && this.rand.nextInt(13) == 0) {  //13
                    ArrayList<Vector2> checkThese = new ArrayList<Vector2>();
                    ArrayList<Vector2> found = new ArrayList<Vector2>();
                    int size = 0;
                    found.add(pos);
                    checkThese.add(pos.cpy().add(-16,0));
                    checkThese.add(pos.cpy().add(16,0));
                    checkThese.add(pos.cpy().add(0,16));
                    checkThese.add(pos.cpy().add(0,-16));
                    while (checkThese.size() > 0) {
                        Vector2 pos2 = checkThese.remove(0);
                        if (alreadyChecked.contains(pos2)) {
                            continue;
                        }
                        alreadyChecked.add(pos2);
                        if (!currLayer.containsKey(pos2)) {
                            continue;
                        }
                        if (!currLayer.get(pos2).attrs.get("solid")) {
                            Vector2 left = pos2.cpy().add(-16f, 0f);
                            Vector2 tl = pos2.cpy().add(-16f, 16f);
                            Vector2 ble = pos2.cpy().add(-16f, -16f);
                            Vector2 right = pos2.cpy().add(16f, 0f);
                            Vector2 tr = pos2.cpy().add(16f, 16f);
                            Vector2 br = pos2.cpy().add(16f, -16f);
                            Vector2 up = pos2.cpy().add(0f, 16f);
                            Vector2 down = pos2.cpy().add(0f, -16f);
                            boolean touchLeft = currLayer.containsKey(left) && currLayer.get(left).attrs.get("solid");
                            boolean touchTl = currLayer.containsKey(tl) && currLayer.get(tl).attrs.get("solid");
                            boolean touchBle = currLayer.containsKey(ble) && currLayer.get(ble).attrs.get("solid");
                            boolean touchRight = currLayer.containsKey(right) && currLayer.get(right).attrs.get("solid");
                            boolean touchTr = currLayer.containsKey(tr) && currLayer.get(tr).attrs.get("solid");
                            boolean touchBr = currLayer.containsKey(br) && currLayer.get(br).attrs.get("solid");
                            boolean touchUp = currLayer.containsKey(up) && currLayer.get(up).attrs.get("solid");
                            boolean touchDown = currLayer.containsKey(down) && currLayer.get(down).attrs.get("solid");
    
                            // Use this one if you ever want there to be a lot of size=1 pathways, like in rocket hideout, cave, etc.
    //                        if ((touchLeft && (touchTr || touchBr)) || 
    //                            (touchUp && (touchBle || touchBr)) || 
    //                            (touchRight && (touchTl || touchBle)) || 
    //                            (touchDown && (touchTl || touchTr))) {
    //                            continue;
    //                        }
                            
                            if ((touchLeft && ((touchTr && !touchUp) || (touchBr && !touchDown))) || 
                                (touchUp && ((touchBle && !touchLeft) || (touchBr && !touchRight))) || 
                                (touchRight && ((touchTl && !touchUp) || (touchBle && !touchDown))) || 
                                (touchDown && ((touchTl && !touchLeft) || (touchTr && !touchRight)))) {
                                continue;
                            }
                        }
                        else if (!currLayer.get(pos2).name.contains("pkmnmansion_wall")) {
                            continue;
                        }
                        if (size > 10 && this.rand.nextInt(35) -size <= 0) {
                            continue;
                        }
                        size++;
                        currLayer.put(pos2.cpy(), new Tile("pkmnmansion_floor1", "rubble1", pos2.cpy(), true, null));
                        checkThese.add(pos2.cpy().add(-16,0));
                        checkThese.add(pos2.cpy().add(16,0));
                        checkThese.add(pos2.cpy().add(0,16));
                        checkThese.add(pos2.cpy().add(0,-16));
                    }
                }
            }
    
            
            // Place shelf things
            alreadyChecked.clear();
            for (Vector2 pos : currLayer.keySet()) {
                if (alreadyChecked.contains(pos)) {
                    continue;
                }
                alreadyChecked.add(pos);
                int numShelves = 0;
                HashMap<Vector2, Tile> currSet = new HashMap<Vector2, Tile>();
                if (!currLayer.get(pos).attrs.get("solid") 
                    && (currLayer.get(pos.cpy().add(0, 16)).name.contains("pkmnmansion_wall") 
                        || currLayer.get(pos.cpy().add(-16, 0)).name.contains("pkmnmansion_wall")
                        || currLayer.get(pos.cpy().add(16, 0)).name.contains("pkmnmansion_wall"))
                    && this.rand.nextInt(5+numShelves) == 0) {
                    ArrayList<Vector2> checkThese = new ArrayList<Vector2>();
                    ArrayList<Vector2> found = new ArrayList<Vector2>();
                    int size = 0;
                    found.add(pos);
                    checkThese.add(pos.cpy().add(-16,0));
                    checkThese.add(pos.cpy().add(16,0));
                    checkThese.add(pos.cpy().add(0,16));
                    checkThese.add(pos.cpy().add(0,-16));
                    numShelves++;
                    while (checkThese.size() > 0) {
                        Vector2 pos2 = checkThese.remove(0);
                        if (alreadyChecked.contains(pos2)) {
                            continue;
                        }
                        alreadyChecked.add(pos2);
                        if (!currLayer.containsKey(pos2)) {
                            continue;
                        }
                        if (currLayer.get(pos2).attrs.get("solid")) {
                            continue;
                        }
                        Vector2 left = pos2.cpy().add(-16f, 0f);
                        Vector2 right = pos2.cpy().add(16f, 0f);
                        Vector2 up = pos2.cpy().add(0f, 16f);
                        Vector2 down = pos2.cpy().add(0f, -16f);
                        boolean touchLeft = currLayer.get(left).name.contains("pkmnmansion_wall");
                        boolean touchRight = currLayer.get(right).name.contains("pkmnmansion_wall");
                        boolean touchUp = currLayer.get(up).name.contains("pkmnmansion_wall");
                        boolean touchDown = currLayer.get(down).name.contains("pkmnmansion_wall");
    
                        if (touchDown) {
                            continue;
                        }
                        else if ((touchLeft && touchRight) ||
                                 (!touchLeft && !touchRight && !touchUp)) {
                            continue;
                        }
    
                        Vector2 tl = pos2.cpy().add(-16f, 16f);
                        Vector2 ble = pos2.cpy().add(-16f, -16f);
                        Vector2 tr = pos2.cpy().add(16f, 16f);
                        Vector2 br = pos2.cpy().add(16f, -16f);
                        boolean touchTl = currLayer.get(tl).attrs.get("solid");
                        boolean touchBle = currLayer.get(ble).attrs.get("solid");
                        boolean touchTr = currLayer.get(tr).attrs.get("solid");
                        boolean touchBr = currLayer.get(br).attrs.get("solid");
                        touchLeft = currLayer.get(left).attrs.get("solid");
                        touchRight = currLayer.get(right).attrs.get("solid");
                        touchUp = currLayer.get(up).attrs.get("solid");
                        touchDown = currLayer.get(down).attrs.get("solid");
    
                        if ((touchLeft && ((touchTr && !touchUp) || (touchBr && !touchDown))) || 
                            (touchUp && ((touchBle && !touchLeft) || (touchBr && !touchRight))) || 
                            (touchRight && ((touchTl && !touchUp) || (touchBle && !touchDown))) || 
                            (touchDown && ((touchTl && !touchLeft) || (touchTr && !touchRight)))) {
                            continue;
                        }
    
                        if (size > 10 && this.rand.nextInt(35) -size <= 0) {
                            continue;
                        }
                        size++;
                        currSet.put(pos2.cpy(), new Tile("pkmnmansion_shelf1", pos2.cpy(), true, null));
                        checkThese.add(pos2.cpy().add(-16,0));
                        checkThese.add(pos2.cpy().add(16,0));
                        checkThese.add(pos2.cpy().add(0,16));
                        checkThese.add(pos2.cpy().add(0,-16));
                    }
                    if (currSet.keySet().size() > 3) {
                        for (Vector2 pos2 : currSet.keySet()) {
                            Vector2 up = pos2.cpy().add(0f, 16f);
                            boolean touchUp = currSet.containsKey(up);
                            if (touchUp) {
                                currLayer.put(pos2.cpy(), new Tile("pkmnmansion_shelf1_NS", pos2.cpy(), true, null));
                            }
                            else {
                                currLayer.put(pos2.cpy(), new Tile("pkmnmansion_shelf1", pos2.cpy(), true, null));
                            }
                        }
                    }
                }
            }

            if (levelNum == 4) {
            }

            // Place tables
            alreadyChecked.clear();
            ArrayList<HashMap<Vector2, Tile>> possibleSpots = new ArrayList<HashMap<Vector2, Tile>>();
    //        for (Vector2 pos : currLayer.keySet()) {
            for (Vector2 pos = bl.cpy(); pos.y < (bl.y + 28*16); pos.x += 16) {
                if (pos.x > bl.x + 30*16) {
                    pos.x = bl.x-16;
                    pos.y += 16;
                    continue;
                }
                if (!currLayer.containsKey(pos)) {
                    continue;
                }
                if (alreadyChecked.contains(pos)) {
                    continue;
                }
    //            alreadyChecked.add(pos);
    //            if (!currLayer.get(pos).attrs.get("solid")) {
                if (currLayer.get(pos).name.contains("pkmnmansion_floor1")) {
    //                ArrayList<Vector2> checkThese = new ArrayList<Vector2>();
                    int h = 4;
                    int w = 4;
                    Vector2 lastWorking = null;
                    boolean yDir = false;
                    while (true) {
                        boolean works = true;
                        for (int x = 0; x < w; x++) {
                            for (int y = 0; y < h; y++) {
    //                            if (alreadyChecked.contains(pos.cpy().add(x*16, y*16))) {
    //                                works = false;
    //                                break;
    //                            }
                                if (currLayer.get(pos.cpy().add(x*16, y*16)).attrs.get("solid")) {
                                    works = false;
                                    break;
                                }
                            }
                            if (!works) {
                                break;
                            }
                        }
    //                    if (w > 7) {
    //                        works = false;
    //                    }
    //                    if (h > 7) {
    //                        works = false;
    //                    }
    //                    if (lastWorking != null && works && this.rand.nextInt(4) == 0) {
    //                        works = false;
    //                    }
                        if (works) {
                            lastWorking = new Vector2(w, h);
                            if (yDir) {
                                h += 1;
                            }
                            else {
                                w += 1;
                            }
                        }
                        else {
                            if (!yDir) {
                                yDir = true;
                                continue;
                            }
                            else if (lastWorking != null) {  // && this.rand.nextInt(3) != 0
                                // place table
                                // TODO: probably place beds, or something.
                                if (lastWorking.x < 7 && lastWorking.y < 7) {
                                    HashMap<Vector2, Tile> putThese = new HashMap<Vector2, Tile>();
                                    for (int x = 1; x < lastWorking.x-1; x++) {
                                        for (int y = 1; y < lastWorking.y-1; y++) {
                                            if (x == 1 && y == 1) {
                                                putThese.put(pos.cpy().add(x*16, y*16), new Tile("table_SW", pos.cpy().add(x*16, y*16), true, null));
                                            }
                                            else if (x == lastWorking.x-2 && y == lastWorking.y-2) {
                                                putThese.put(pos.cpy().add(x*16, y*16), new Tile("table_NE", pos.cpy().add(x*16, y*16), true, null));
                                            }
                                            else if (x == 1 && y == lastWorking.y-2) {
                                                putThese.put(pos.cpy().add(x*16, y*16), new Tile("table_NW", pos.cpy().add(x*16, y*16), true, null));
                                            }
                                            else if (x == lastWorking.x-2 && y == 1) {
                                                putThese.put(pos.cpy().add(x*16, y*16), new Tile("table_SE", pos.cpy().add(x*16, y*16), true, null));
                                            }
                                            else if (x == 1) {
                                                putThese.put(pos.cpy().add(x*16, y*16), new Tile("table_W", pos.cpy().add(x*16, y*16), true, null));
                                            }
                                            else if (x == lastWorking.x-2) {
                                                putThese.put(pos.cpy().add(x*16, y*16), new Tile("table_E", pos.cpy().add(x*16, y*16), true, null));
                                            }
                                            else if (y == 1) {
                                                putThese.put(pos.cpy().add(x*16, y*16), new Tile("table_S", pos.cpy().add(x*16, y*16), true, null));
                                            }
                                            else if (y == lastWorking.y-2) {
                                                putThese.put(pos.cpy().add(x*16, y*16), new Tile("table_N", pos.cpy().add(x*16, y*16), true, null));
                                            }
                                            else {
                                                putThese.put(pos.cpy().add(x*16, y*16), new Tile("table", pos.cpy().add(x*16, y*16), true, null));
                                            }
                                        }
                                    }
                                    possibleSpots.add(putThese);
                                }
                                for (int x = 0; x < lastWorking.x; x++) {
                                    for (int y = 0; y < lastWorking.y+1; y++) {
                                        alreadyChecked.add(pos.cpy().add(x*16, y*16));
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
            for (HashMap<Vector2, Tile> putThese : possibleSpots) {
                boolean works = true;
                for (Vector2 pos : putThese.keySet()) {
                    if (currLayer.get(pos).attrs.get("solid")) {
                        works = false;
                        break;
                    }
                }
                if (works) {
                    for (Vector2 pos : putThese.keySet()) {
                        currLayer.put(pos, putThese.get(pos));
                    }
                }
            }
            bl.add(16, 16);
        }

        bl.add(-32, -32);
        Tile nextStairsUp = null;
        HashMap<Vector2, Tile> currLayer = mansionInteriorTiles.get(100);
        HashMap<Vector2, Tile> layerAbove = mansionInteriorTiles.get(101);
        currRoute = new Route("pkmnmansion1", 30);  // TODO: up level later
        // Entrance area
        int yMax = this.rand.nextInt(4)*2 +6;  // from 6 to 18  // this.rand.nextInt(7)*2 +6
        while (true) {
            Vector2 pos = bl.cpy().add((5+10)*16, (yMax-1)*16);
            if (layerAbove.get(pos) != null && layerAbove.get(pos).attrs.get("solid")) {
//                System.out.println(yMax);
                yMax++;
            }
            else {
                break;
            }
        }
        for (int x = 0; x < 10; x++) {  //  x pos
            for (int y = 0; y < yMax; y++) {  //  y pos  
                Vector2 pos = bl.cpy().add((x+10)*16, y*16);
                if (y == yMax-1 && x == 5) {
                    nextStairsUp = new Tile("pkmnmansion_floor2", "stairs_up1", pos.cpy(), true, null);
                    currLayer.put(pos.cpy(), nextStairsUp);
                    stairsUp = new Tile("pkmnmansion_floor2", "stairs_down1", pos.cpy(), true, null);
                    layerAbove.put(pos.cpy(), stairsUp);
                }
                else if (y == 0 && x == 5) {
                    currLayer.put(pos.cpy(), new Tile("pkmnmansion_rug", pos.cpy(), true, currRoute));
                }
                else if ((x == 2 || x == 7) && (y % 2) == 1 && y != 1) {
                    currLayer.put(pos.cpy(), new Tile("pkmnmansion_block1", pos.cpy(), true, null));
                }
                else if (x == 3) {
                    currLayer.put(pos.cpy(), new Tile("pkmnmansion_floor2_W", pos.cpy(), true, currRoute));
                }
                else if (x == 6) {
                    currLayer.put(pos.cpy(), new Tile("pkmnmansion_floor2_E", pos.cpy(), true, currRoute));
                }
                else if (x > 3 && x < 6) {
                    currLayer.put(pos.cpy(), new Tile("pkmnmansion_floor2", pos.cpy(), true, currRoute));
                }
                else if (y != 0) {
                    currLayer.put(pos.cpy(), new Tile("pkmnmansion_floor1", pos.cpy(), true, currRoute));
                }
            }
        }

        // Starting at top level, place statue, stairs and walls.
        HashMap<Vector2, Tile> layerBelow = null;
        for (int levelNum = 6; levelNum > 0; levelNum--) {
            currLayer = mansionInteriorTiles.get(95+levelNum);
            layerBelow = mansionInteriorTiles.get(94+levelNum);

            // Place downstairs
            if (stairsUp != null) {
                alreadyChecked.clear();
                ArrayList<Vector2> checkThese = new ArrayList<Vector2>();
                ArrayList<Vector2> shuffleThese = new ArrayList<Vector2>();
                ArrayList<Vector2> endPoints = new ArrayList<Vector2>();
                checkThese.add(stairsUp.position.cpy());
                alreadyChecked.add(stairsUp.position.cpy());
                while (checkThese.size() > 0) {
                    for (Vector2 pos : new ArrayList<Vector2>(checkThese)) {
                        boolean addedAnother = false;
                        boolean touchTable = false;
                        boolean touchEndpoint = false;
                        checkThese.remove(pos);
                        shuffleThese.clear();
                        shuffleThese.add(pos.cpy().add(16, 0));
                        shuffleThese.add(pos.cpy().add(-16, 0));
                        shuffleThese.add(pos.cpy().add(0, 16));
                        shuffleThese.add(pos.cpy().add(0, -16));
                        Vector2 pos2;
                        while (true) {
                            pos2 = shuffleThese.remove(this.rand.nextInt(shuffleThese.size()));
                            if (!alreadyChecked.contains(pos2) && currLayer.containsKey(pos2) && !currLayer.get(pos2).attrs.get("solid")&& !currLayer.get(pos2).nameUpper.contains("stairs")) {
                                checkThese.add(pos2);
                                alreadyChecked.add(pos2);
                                addedAnother = true;
                            }
                            if (currLayer.containsKey(pos2) && currLayer.get(pos2).name.contains("table")) {
                                touchTable = true;
                            }
                            if (endPoints.contains(pos2)) {
                                touchEndpoint = true;
                            }
                            if (shuffleThese.size() <= 0) {
                                break;
                            }
                        }
                        if (!touchTable && !touchEndpoint && !addedAnother && !currLayer.get(pos).name.contains("floor2") && !currLayer.get(pos).name.contains("rug")) {
                            endPoints.add(pos);
                        }
                    }
                }
                // TODO: hidden items
                // TODO: sometimes there aren't enough endpoints, crashes.
                Vector2 pos3 = endPoints.get(endPoints.size()-1); // = endPoints.remove(endPoints.size()-1);
                while (endPoints.size() > 0) {
                    if (endPoints.size() > 4) {
                        pos3 = endPoints.remove(this.rand.nextInt(5)+endPoints.size()-5);
                    }
                    else {
                        pos3 = endPoints.remove(endPoints.size()-1);
                    }
                    if (layerBelow.get(pos3) != null && layerBelow.get(pos3).attrs.get("solid")) {
                        continue;
                    }
                    // TODO: check
                    stairsDown = new Tile("pkmnmansion_floor2", "stairs_down1", pos3.cpy(), true, null);
                    break;
                }
                if (levelNum != 6) {
                    currLayer.put(pos3.cpy(), stairsDown);
                    nextStairsUp = new Tile("pkmnmansion_floor1", "stairs_up1", pos3.cpy(), true, null);
                    layerBelow.put(pos3.cpy(), nextStairsUp);
                }

                // Place pokeballs
                int maxJ = this.rand.nextInt(3)+3;
                for (int j = 0; j < maxJ*3 && endPoints.size() > 5; j++) {
    //                pos = endPoints.remove(this.rand.nextInt(5)+endPoints.size()-5);
    //                pos = endPoints.remove(this.rand.nextInt(endPoints.size()));
                    pos3 = endPoints.remove(endPoints.size()-1);
                    if (j % 3 == 0) {  // && this.rand.nextInt(2) == 0
//                        currLayer.put(pos3.cpy(), new Tile("pkmnmansion_floor1", "pokeball1", pos3.cpy(), true, null));
                        currLayer.put(pos3.cpy(), new Tile("pkmnmansion_floor1", "ultraball1", pos3.cpy(), true, null));
                    }
    //                if (this.rand.nextInt(2) == 0) {
    //                    currLayer.put(pos3.cpy(), new Tile("pkmnmansion_floor1", "pokeball1", pos3.cpy(), true, null));
    //                }
    //                else {
    //                    j--;
    //                }
                    
                }
    //            System.out.println("Num Endpoints:");
    //            System.out.println(endPoints.size());
    //            for (int j = 0; j < 5 && j < endPoints.size(); j++) {
    //                Vector2 pos = endPoints.get(endPoints.size()-1-j);
    ////                Vector2 pos = endPoints.get(j);
    //                stairsDown = new Tile("pkmnmansion_floor2", "stairs_down1", pos.cpy(), true, null);
    //                currLayer.put(pos.cpy(), stairsDown);
    //            }

                // Place the hidden-switch statue
                alreadyChecked.clear();
                checkThese.clear();
                shuffleThese.clear();
                endPoints.clear();
                checkThese.add(stairsUp.position.cpy());
                alreadyChecked.add(stairsUp.position.cpy());
                checkThese.add(stairsDown.position.cpy());
                alreadyChecked.add(stairsUp.position.cpy());
                while (checkThese.size() > 0) {
                    for (Vector2 pos : new ArrayList<Vector2>(checkThese)) {
                        boolean addedAnother = false;
                        boolean touchTable = false;
                        boolean touchEndpoint = false;
                        // TODO: remove
    //                    boolean touchDown;
    //                    boolean touchLeft;
    //                    boolean touchRight;
                        int numTouch = 0;
                        checkThese.remove(pos);
                        shuffleThese.clear();
                        shuffleThese.add(pos.cpy().add(16, 0));
                        shuffleThese.add(pos.cpy().add(-16, 0));
                        shuffleThese.add(pos.cpy().add(0, 16));
                        shuffleThese.add(pos.cpy().add(0, -16));
                        Vector2 pos2;
                        while (true) {
                            pos2 = shuffleThese.remove(this.rand.nextInt(shuffleThese.size()));
                            // TODO: remove
    //                        boolean works = true;
    //                        touchDown = currLayer.containsKey(pos2.cpy().add(0, -16)) && currLayer.get(pos2.cpy().add(0, -16)).attrs.get("solid");
    //                        touchLeft = currLayer.containsKey(pos2.cpy().add(-16, 0)) && currLayer.get(pos2.cpy().add(-16, 0)).attrs.get("solid");
    //                        touchRight = currLayer.containsKey(pos2.cpy().add(16, 0)) && currLayer.get(pos2.cpy().add(16, 0)).attrs.get("solid");
    //                        if (pos.y == pos2.y && touchDown) {
    //                            works = false;
    //                        }
    //                        else if (touchLeft && touchRight) {
    //                            works = false;
    //                        }
                            if (currLayer.containsKey(pos2) && currLayer.get(pos2).attrs.get("solid")) {
                                numTouch++;
                            }
                            if (!alreadyChecked.contains(pos2) && currLayer.containsKey(pos2) && !currLayer.get(pos2).attrs.get("solid") && !currLayer.get(pos2).nameUpper.contains("stairs")) {
                                checkThese.add(pos2);
                                alreadyChecked.add(pos2);
                                addedAnother = true;
                            }
                            if (currLayer.containsKey(pos2) && currLayer.get(pos2).name.contains("table")) {
                                touchTable = true;
                            }
                            if (endPoints.contains(pos2)) {
                                touchEndpoint = true;
                            }
                            if (shuffleThese.size() <= 0) {
                                break;
                            }
                        }
                        boolean touchDown = currLayer.containsKey(pos.cpy().add(0, -16)) && currLayer.get(pos.cpy().add(0, -16)).attrs.get("solid");
                        boolean touchBl = currLayer.containsKey(pos.cpy().add(-16, -16)) && currLayer.get(pos.cpy().add(-16, -16)).attrs.get("solid");
                        boolean touchBr = currLayer.containsKey(pos.cpy().add(16, -16)) && currLayer.get(pos.cpy().add(16, -16)).attrs.get("solid");
                        boolean touchTl = currLayer.containsKey(pos.cpy().add(-16, 16)) && currLayer.get(pos.cpy().add(-16, 16)).attrs.get("solid");
                        boolean touchTr = currLayer.containsKey(pos.cpy().add(16, 16)) && currLayer.get(pos.cpy().add(16, 16)).attrs.get("solid");
                        if (!touchDown && !(touchBl && touchBr) && !(touchTl && touchTr) && numTouch < 2 && !touchTable && !touchEndpoint && !addedAnother && !currLayer.get(pos).name.contains("floor2") && !currLayer.get(pos).name.contains("rug")) {
                            endPoints.add(pos);
                        }
                    }
                }
                //
                if (endPoints.size() > 4) {
                    pos3 = endPoints.remove(this.rand.nextInt(5)+endPoints.size()-5);
                }
                else {
                    pos3 = endPoints.remove(endPoints.size()-1);
                }
                statue = new Tile("pkmnmansion_statue1", pos3.cpy(), true, null);
                if (levelNum != 1) {
                    currLayer.put(pos3.cpy(), statue);
                }
    //            pos3 = endPoints.remove(endPoints.size()-1);
    //            statue = new Tile("pkmnmansion_statue1", pos3.cpy(), true, null);
    //            currLayer.put(pos3.cpy(), statue);
    
    //            // TODO: debug, remove
    //            for (int j = 0; j < 10; j++) {  // j <endPoints.size()
    //                pos3 = endPoints.get(endPoints.size()-1-j);
    ////                statue = new Tile("pkmnmansion_statue1", pos3.cpy(), true, null);
    //                statue = new Tile("pkmnmansion_roof", pos3.cpy(), true, null);
    //                currLayer.put(pos3.cpy(), statue);
    //            }
                
                // Get list of all possible doorways
                ArrayList<ArrayList<Tile>> allDoors = new ArrayList<ArrayList<Tile>>();
                alreadyChecked.clear();
                for (Vector2 pos : currLayer.keySet()) {
                    Tile currTile = currLayer.get(pos);
                    if (!currTile.attrs.get("solid")) {
                        continue;
                    }
                    Tile nextTile = currTile;
                    int length = 0;
                    ArrayList<Vector2> checkDirs = new ArrayList<Vector2>();
                    if (currTile.name.equals("pkmnmansion_wall_S")) {
                        checkDirs.add(new Vector2(-16, 0));
                        checkDirs.add(new Vector2(0, -16));
                        checkDirs.add(new Vector2(16, 0));
                    }
                    else if (currTile.name.equals("pkmnmansion_wall_N")) {
                        checkDirs.add(new Vector2(-16, 0));
                        checkDirs.add(new Vector2(0, 16));
                        checkDirs.add(new Vector2(16, 0));
                    }
                    else if (currTile.name.equals("pkmnmansion_wall_W")) {
                        checkDirs.add(new Vector2(-16, 0));
                        checkDirs.add(new Vector2(0, 16));
                        checkDirs.add(new Vector2(0, -16));
                    }
                    else if (currTile.name.equals("pkmnmansion_wall_NW")) {
                        checkDirs.add(new Vector2(-16, 0));
                        checkDirs.add(new Vector2(0, 16));
                    } 
                    else if (currTile.name.equals("pkmnmansion_wall_E")) {
                        checkDirs.add(new Vector2(16, 0));
                        checkDirs.add(new Vector2(0, 16));
                        checkDirs.add(new Vector2(0, -16));
                    }
                    else if (currTile.name.equals("pkmnmansion_wall_NE")) {
                        checkDirs.add(new Vector2(16, 0));
                        checkDirs.add(new Vector2(0, 16));
                    }
                    else {
                        continue;
                    }
                    for (Vector2 dir : checkDirs) {
                        ArrayList<Tile> door = new ArrayList<Tile>();
                        Vector2 initialDir = dir.cpy();
                        while (true) {
                            nextTile = currLayer.get(currTile.position.cpy().add(dir));
                            if (nextTile != currTile && (nextTile == null || nextTile.name.contains("rug") || nextTile.name.contains("stairs") || nextTile.nameUpper.contains("stairs") || (nextTile.attrs.get("solid") && !nextTile.nameUpper.contains("pokeball")))) {
                                if (nextTile != null && ((!nextTile.name.contains("pkmnmansion_wall") && !nextTile.nameUpper.contains("rubble")) || alreadyChecked.contains(nextTile.position))) {
                                    nextTile = null;
                                }
        //                        if (nextTile != null) {
        //                            alreadyChecked.add(nextTile.position.cpy());
        //                            alreadyChecked.add(currTile.position.cpy());
        //                        }
    //                            if (door.size() > 0) {
    //                                door.remove(door.size()-1);
    //                            }
                                break;
                            }
                            String name = "pkmnmansion_gate_EW__on";
                            if (dir.x == 0) {
                                name = "pkmnmansion_gate_NS__on";
                            }
                            door.add(new Tile(name, nextTile.position.cpy(), true, null));
        //                    if (currTile.name.equals("pkmnmansion_wall_S")) {
        //                        nextTile = currLayer.get(nextTile.position.cpy().add(0, -16));
        //                        if (nextTile != null) {
        //                            door.add(new Tile("pkmnmansion_gate_NS__on", nextTile.position.cpy(), true, null));
        //                        }
        //                    }
        //                    else if (currTile.name.equals("pkmnmansion_wall_N")) {
        //                        nextTile = currLayer.get(nextTile.position.cpy().add(0, 16));
        //                        if (nextTile != null) {
        //                            door.add(new Tile("pkmnmansion_gate_NS__on", nextTile.position.cpy(), true, null));
        //                        }
        //                    }
        //                    else if (currTile.name.equals("pkmnmansion_wall_W") || currTile.name.equals("pkmnmansion_wall_NW")) {
        //                        nextTile = currLayer.get(nextTile.position.cpy().add(-16, 0));
        //                        if (nextTile != null) {
        //                            door.add(new Tile("pkmnmansion_gate_EW__on", nextTile.position.cpy(), true, null));
        //                        }
        //                    }
        //                    else if (currTile.name.equals("pkmnmansion_wall_E") || currTile.name.equals("pkmnmansion_wall_NE")) {
        //                        nextTile = currLayer.get(nextTile.position.cpy().add(16, 0));
        //                        if (nextTile != null) {
        //                            door.add(new Tile("pkmnmansion_gate_EW__on", nextTile.position.cpy(), true, null));
        //                        }
        //                    }
        //                    else  {
        //                        break;
        //                    }
                            dir = dir.add(initialDir);
                            length++;
                        }
                        if (nextTile != null && length < 7) {
                            allDoors.add(door);
                        }
                    }
                }
                
                // TODO: debug, remove
//                prevStatue = statue;
    
                if (prevStatue != null) {
                    
                    prevStatue.doorTiles = new ArrayList<Vector2>();
                    int j = 0;
                    for (ArrayList<Tile> door : allDoors) {
        //                boolean flip = this.rand.nextInt(2) == 0;
                        for (Tile tile : door) {
                            currLayer.put(tile.position.cpy(), tile);
                            if (!prevStatue.doorTiles.contains(tile.position)) {
                                prevStatue.doorTiles.add(tile.position.cpy());
                            }
        //                    if (flip) {
        //                        tile.flipDoorTile();
        //                    }
        //                    if (j % 2 == 0) {
        //                        tile.flipDoorTile();
        //                    }
                        }
                        j++;
                    }
                    
                    ArrayList<ArrayList<Tile>> stairsDownDoors = new ArrayList<ArrayList<Tile>>();
                    for (ArrayList<Tile> door : allDoors) {
                        stairsDownDoors.add(door);
                    }
                    int numRemoved = 0;
                    boolean changedDoor = true;
        //            while(!this.isPathBetween(currLayer, stairsUp, statue) || this.isPathBetween(currLayer, stairsUp, stairsDown)) {
                    while(changedDoor) {
                        boolean pathToStairs = this.isPathBetween(currLayer, stairsUp, stairsDown);
                        boolean pathToStatue = this.isPathBetween(currLayer, stairsUp, statue);
                        changedDoor = false;
        //                for (ArrayList<Tile> door : new ArrayList<ArrayList<Tile>>(stairsDownDoors)) {
                        for (int k = 0; k < stairsDownDoors.size(); k++) {
                            ArrayList<Tile> door = stairsDownDoors.get(this.rand.nextInt(stairsDownDoors.size()));
                            for (Tile tile : door) {
                                tile.flipDoorTile();
                            }
                            // If stairsDown is now reachable, add the door back
                            if (!pathToStairs && this.isPathBetween(currLayer, stairsUp, stairsDown)) {
                                for (Tile tile : door) {
                                    tile.flipDoorTile();
                                }
                            }
                            // If statue is no longer reachable, remove the door
                            else if (pathToStatue && !this.isPathBetween(currLayer, stairsUp, statue)) {
                                for (Tile tile : door) {
                                    tile.flipDoorTile();
                                }
                            }
                            // Else, set removedDoor
                            else {
                                stairsDownDoors.remove(door);
                                changedDoor = true;
                                numRemoved++;
                            }
                        }
        //                System.out.println(stairsDownDoors.size());
        //                System.out.println(numRemoved);
        //                if (!removedDoor) {
        //                    break;
        //                }
                    }
        
                    // stairsDownDoors should only contain closed doors
        //            System.out.println("removed:");
        //            System.out.println(numRemoved);
                    stairsDownDoors.clear();
        //            if (numRemoved != 0) {
                    for (ArrayList<Tile> door : allDoors) {
                        for (Tile tile : door) {
                            if (tile.name.contains("__on")) {
                                stairsDownDoors.add(door);
                                break;
                            }
                        }
                    }
        //            }
        //            
        //            
                    // Flip everything to __on
                    for (Vector2 pos : prevStatue.doorTiles) {
                        if (currLayer.get(pos).name.contains("__off")) {
                            currLayer.get(pos).flipDoorTile();
                        }
                    }
        
                    j = 0;
                    for (ArrayList<Tile> door : allDoors) {
                        for (Tile tile : door) {
                            if (tile.name.contains("__off")) {  //__off
                                tile.flipDoorTile();
                            }
        //                    if (j % 2 == 1) {
        //                        tile.flipDoorTile();
        //                    }
                        }
                        j++;
                    }
        
                    //
                    ArrayList<ArrayList<Tile>> statueDoors = new ArrayList<ArrayList<Tile>>();
                    for (ArrayList<Tile> door : allDoors) {
                        if (!stairsDownDoors.contains(door)) {
                            statueDoors.add(door);
                        }
                    }
                    numRemoved = 0;
        //            int giveUp = 0;
                    changedDoor = true;
                    while(changedDoor) {  // && giveUp < statueDoors.size()
        //          while(!this.isPathBetween(currLayer, stairsUp, stairsDown) || this.isPathBetween(currLayer, stairsUp, statue)) {
                        boolean pathToStairs = this.isPathBetween(currLayer, stairsUp, stairsDown);
                        boolean pathToStatue = this.isPathBetween(currLayer, stairsUp, statue);
        //                boolean removedDoor = false;
        //                for (ArrayList<Tile> door : new ArrayList<ArrayList<Tile>>(statueDoors)) {
                        changedDoor = false;
                        for (int k = 0; k < statueDoors.size(); k++) {
                            ArrayList<Tile> door = statueDoors.get(this.rand.nextInt(statueDoors.size()));
                            for (Tile tile : door) {
                                tile.flipDoorTile();
                            }
        
                            if (!pathToStatue && this.isPathBetween(currLayer, stairsUp, statue)) {
                                for (Tile tile : door) {
                                    tile.flipDoorTile();
                                }
                            }
                            // If stairs aren't reachable, remove the door
                            else if (pathToStairs && !this.isPathBetween(currLayer, stairsUp, stairsDown)) {
                                for (Tile tile : door) {
                                    tile.flipDoorTile();
                                }
                            }
                            // Else, set removedDoor
                            else {
        //                        for (Tile tile : door) {
        //                            tile.flipDoorTile();
        //                        }
                                statueDoors.remove(door);
                                changedDoor = true;
                                numRemoved++;
                            }
                        }
        //                giveUp++;
        //                if (!removedDoor) {
        ////                    if (numRemoved == 0) {
        ////                        for (ArrayList<Tile> door : statueDoors) {
        ////                            for (Tile tile : door) {
        ////                                tile.flipDoorTile();
        ////                            }
        ////                        }
        ////                        statueDoors.clear();
        ////                    }
        //                    break;
        //                }
                    }
        
                    // statueDoors should only contain closed doors
                    statueDoors.clear();
        //            if (numRemoved != 0) {
        //            if (this.isPathBetween(currLayer, stairsUp, stairsDown)) {
                        for (ArrayList<Tile> door : allDoors) {
                            for (Tile tile : door) {
                                if (tile.name.contains("__on")) {
                                    statueDoors.add(door);
                                    break;
                                }
                            }
                        }
        //            }
        //            }
                    
        
                    for (ArrayList<Tile> door : allDoors) {
        //                if (stairsDownDoors.contains(door) && statueDoors.contains(door)) {
        //                    for (Tile tile : door) {
        //                        prevStatue.doorTiles.remove(tile.position);
        //                    }
        //                    System.out.println("both");
        //                }
                        // TODO: i don't think this check works.
        //                else if (!stairsDownDoors.contains(door) && !statueDoors.contains(door)) {
                            for (Tile tile : door) {
//                                if (tile.nameUpper.contains("stairs")) {
//                                    System.out.println(tile.nameUpper);
//                                }
                                currLayer.put(tile.position.cpy(), new Tile("pkmnmansion_floor1", tile.position.cpy(), true, null));
                                prevStatue.doorTiles.remove(tile.position);
                            }
        //                    System.out.println("neither");
        //                }
        
        //                for (Tile tile : door) {
        //                    if (tile.name.contains("__off")) {
        //                        tile.flipDoorTile();
        //                    }
        //                }
                    }
                    // TODO: probably don't add to this until now.
                    prevStatue.doorTiles.clear();
                    for (ArrayList<Tile> door : statueDoors) {
                        for (Tile tile : door) {
        //                    currLayer.put(tile.position.cpy(), new Tile(tile.name, tile.position.cpy(), true, null));
                            currLayer.put(tile.position.cpy(), tile);
                            prevStatue.doorTiles.add(tile.position.cpy());
                            if (tile.name.contains("__off")) {
                                tile.flipDoorTile();
                            }
                        }
                    }
                    if (!this.isPathBetween(currLayer, stairsUp, stairsDown)) {
                        for (ArrayList<Tile> door : statueDoors) {
                            for (Tile tile : door) {
                                currLayer.put(tile.position.cpy(), new Tile("pkmnmansion_floor1", tile.position.cpy(), true, null));
                                prevStatue.doorTiles.remove(tile.position);
                            }
                            if (this.isPathBetween(currLayer, stairsUp, stairsDown)) {
                                break;
                            }
                        }
                    }
                    for (ArrayList<Tile> door : stairsDownDoors) {
                        for (Tile tile : door) {
        //                    currLayer.put(tile.position.cpy(), new Tile(tile.name, tile.position.cpy(), true, null));
                            currLayer.put(tile.position.cpy(), tile);
                            if (!prevStatue.doorTiles.contains(tile.position)) {
                                prevStatue.doorTiles.add(tile.position.cpy());
                            }
//                            else {
//                                System.out.println("duplicate");
//                            }
                            if (tile.name.contains("__on")) {
                                tile.flipDoorTile();
                            }
                        }
                    }
                    
                    
        
        //            while(!this.isPathBetween(currLayer, stairsUp, stairsDown) && allDoors.size() > 0) {
        //                ArrayList<Tile> door = allDoors.remove(this.rand.nextInt(allDoors.size()));
        //                // Remove the door
        //                for (Tile tile : door) {
        //                    currLayer.put(tile.position.cpy(), new Tile("pkmnmansion_floor1", tile.position.cpy(), true, null));
        //                    prevStatue.doorTiles.remove(tile.position);
        //                }
        //            }
        
        //            tempDoors = new ArrayList<ArrayList<Tile>>(allDoors);
        //            boolean giveUp = false;
        //            while(!this.isPathBetween(currLayer, stairsUp, stairsDown)) {
        //                boolean removedDoor = false;
        //                for (ArrayList<Tile> door : new ArrayList<ArrayList<Tile>>(tempDoors)) {
        //                    for (Tile tile : door) {
        //                        currLayer.put(tile.position.cpy(), new Tile("pkmnmansion_floor1", tile.position.cpy(), true, null));
        //                        prevStatue.doorTiles.remove(tile.position);
        //                    }
        ////                    System.out.println(door.size());
        //                    // If statue is now reachable, add the door back
        //                    if (this.isPathBetween(currLayer, stairsUp, statue)) {
        //                        for (Tile tile : door) {
        //                            currLayer.put(tile.position.cpy(), tile);
        //                            if (giveUp) {
        //                                tile.flipDoorTile();
        //                            }
        //                            else {
        //                                prevStatue.doorTiles.add(tile.position);
        //                            }
        //                        }
        ////                        System.out.println("added back");
        //                    }
        //                    // Else, set removedDoor
        //                    else {
        //                        tempDoors.remove(door);
        //                        removedDoor = true;
        //                    }
        //                }
        //                if (!removedDoor) {
        //                    giveUp = true;
        //                    System.out.println("gave up.");
        //                }
        //            }
        
                    // At end, randomly flip statue switch
                    if (this.rand.nextInt(2) == 0) {
                        for (Vector2 pos : prevStatue.doorTiles) {
                            currLayer.get(pos).flipDoorTile();
                        }
                    }
                }
                
                // Place mewtwo
                if (levelNum == 1) {
//                    alreadyChecked.clear();
//                    checkThese.clear();
//                    Tile currTile = stairsDown;
//                    Vector2 pos = stairsDown.position.cpy();
//                    checkThese.add(pos.cpy());
////                    checkThese.add(pos.cpy().add(-16,0));
////                    checkThese.add(pos.cpy().add(16,0));
////                    checkThese.add(pos.cpy().add(0,16));
////                    checkThese.add(pos.cpy().add(0,-16));
//                    while (checkThese.size() > 0) {
//                        Vector2 pos2 = checkThese.remove(0);
//                        if (alreadyChecked.contains(pos2)) {
//                            continue;
//                        }
//                        alreadyChecked.add(pos2);
//                        if (pos.dst(pos2) > 128) {
//                            continue;
//                        }
//                        if (!currLayer.containsKey(pos2)) {
//                            continue;
//                        }
//                        if (currLayer.get(pos2).name.contains("wall") || currLayer.get(pos2).name.contains("gate") || currLayer.get(pos2).nameUpper.contains("pokeball")) {
//                            continue;
//                        }
////                        Route currRoute = currLayer.get(pos2).routeBelongsTo;
//                        currTile = new Tile("pkmnmansion_floor1", pos2.cpy(), true, null);
//                        currLayer.put(pos2.cpy(), currTile);
//                        // Depth-first search, try to get middle tile.
//                        checkThese.add(0, pos2.cpy().add(-16,0));
//                        checkThese.add(0, pos2.cpy().add(16,0));
//                        checkThese.add(0, pos2.cpy().add(0,16));
//                        checkThese.add(0, pos2.cpy().add(0,-16));
//                    }
//                    currLayer.put(currTile.position.cpy(), new SpecialMewtwoTile(currTile.position.cpy()));

                    bl = stairsDown.position.cpy().add(-16*5, -16*1);
                    mansionInteriorTiles.remove(94+levelNum);
                    mansionInteriorTiles.add(94+levelNum, new HashMap<Vector2, Tile>());
                    layerBelow = mansionInteriorTiles.get(94+levelNum); // TODO: init this beginning
                    Tile currTile = null;
                    for (int x = 0; x < 10; x++) {  //  x pos
                        for (int y = 0; y < 11; y++) {  //  y pos  
                            Vector2 pos = bl.cpy().add(x*16, y*16);
                            if ((y == 2 || y == 6) && (x == 2 || x == 3 || x == 6 || x == 7)) {
                                currTile = new Tile("tree_plant1", pos.cpy(), true, null);
                            }
                            else if (x == 0|| y == 0 || y == 10 || x == 9) {
                                currTile = new Tile("pkmnmansion_wall", pos.cpy(), true, null);
                            }
                            else if (x == 4 && y == 9) {
                                currTile = new Tile("pkmnmansion_struct1", pos.cpy(), true, null);
                            }
                            else if (x == 5 && y == 9) {
                                // place nothing here. test if causes issues or not.
                            }
                            else if (x == 5 && y == 8) {
//                                currTile = new SpecialMewtwoTile(pos.cpy());
                                currTile = new Tile("pkmnmansion_floor1", "mewtwo_overworld", pos.cpy(), true, null);
                            }
                            else if (x == 5 && y == 1) {
                                currTile = new Tile("pkmnmansion_floor1", "stairs_up1", pos.cpy(), true, null);
                            }
                            else {
                                currTile = new Tile("pkmnmansion_floor1", pos.cpy(), true, null);
                            }
                            layerBelow.put(pos.cpy(), currTile);
                        }
                    }
                    for (Vector2 pos : layerBelow.keySet()) {
                        if (!layerBelow.get(pos).name.contains("pkmnmansion_wall")) {
                            continue;
                        }
            //            Tile tile = currLayer.get(pos);
                        Vector2 left = pos.cpy().add(-16f, 0f);
                        Vector2 right = pos.cpy().add(16f, 0f);
                        Vector2 up = pos.cpy().add(0f, 16f);
                        Vector2 down = pos.cpy().add(0f, -16f);
                        boolean touchLeft = layerBelow.containsKey(left) && layerBelow.get(left).name.contains("pkmnmansion_wall");
                        boolean touchRight = layerBelow.containsKey(right) && layerBelow.get(right).name.contains("pkmnmansion_wall");
                        boolean touchUp = layerBelow.containsKey(up) && layerBelow.get(up).name.contains("pkmnmansion_wall");
                        boolean touchDown = layerBelow.containsKey(down) && layerBelow.get(down).name.contains("pkmnmansion_wall");
                        // TODO: choose some random
                        // TODO: if touching solid up and down, do
                        if (touchDown && touchUp) {
                            layerBelow.put(pos.cpy(), new Tile("pkmnmansion_wall_NS", pos.cpy(), true, null));
                        }
                        else if (touchDown && touchLeft) {
                            if (this.rand.nextInt(2) == 0) {
                                layerBelow.put(pos.cpy(), new Tile("pkmnmansion_wall_N", pos.cpy(), true, null));
                            }
                            else {
                                layerBelow.put(pos.cpy(), new Tile("pkmnmansion_wall_NE", pos.cpy(), true, null));
                            }
                        }
                        else if (touchDown && touchRight) {
                            if (this.rand.nextInt(2) == 0) {
                                layerBelow.put(pos.cpy(), new Tile("pkmnmansion_wall_N", pos.cpy(), true, null));
                            }
                            else {
                                layerBelow.put(pos.cpy(), new Tile("pkmnmansion_wall_NW", pos.cpy(), true, null));
                            }
                        }
                        else if (touchDown) {
                            layerBelow.put(pos.cpy(), new Tile("pkmnmansion_wall_N", pos.cpy(), true, null));
                        }
                        else if (touchUp && touchLeft) {
                            if (this.rand.nextInt(2) == 0) {
                                layerBelow.put(pos.cpy(), new Tile("pkmnmansion_wall_S", pos.cpy(), true, null));
                            }
                            else {
                                layerBelow.put(pos.cpy(), new Tile("pkmnmansion_wall_E", pos.cpy(), true, null));
                            }
                        }
                        else if (touchUp && touchRight) {
                            if (this.rand.nextInt(2) == 0) {
                                layerBelow.put(pos.cpy(), new Tile("pkmnmansion_wall_S", pos.cpy(), true, null));
                            }
                            else {
                                layerBelow.put(pos.cpy(), new Tile("pkmnmansion_wall_W", pos.cpy(), true, null));
                            }
                        }
                        else if (touchUp) {
                            layerBelow.put(pos.cpy(), new Tile("pkmnmansion_wall_S", pos.cpy(), true, null));
                        }
                        else if (touchLeft && touchRight) {
                            // nothing
                        }
                        else if (touchLeft) {
                            layerBelow.put(pos.cpy(), new Tile("pkmnmansion_wall_E", pos.cpy(), true, null));
                        }
                        else if (touchRight) {
                            layerBelow.put(pos.cpy(), new Tile("pkmnmansion_wall_W", pos.cpy(), true, null));
                        }
                    }
                }
                
                prevStatue = statue;
                stairsUp = nextStairsUp;
            }
        }
    }
    

    //
    public boolean isPathBetween(HashMap<Vector2, Tile> currLayer, Tile tileA, Tile tileB) {
        // TODO: test
        ArrayList<Tile> checkTiles = new ArrayList<Tile>();
        ArrayList<Vector2> alreadyChecked = new ArrayList<Vector2>();
        checkTiles.add(tileA);
        while (checkTiles.size() > 0) {
            Tile tile = checkTiles.remove(0);
            if (tile != null && tile.position.equals(tileB.position)) {
                return true;
            }
            if (tile != tileA && (tile == null || (tile.attrs.get("solid") && !tile.nameUpper.contains("pokeball")) || alreadyChecked.contains(tile.position))) {
                continue;
            }
            // TODO: debug, remove
//            if (tile != tileA) {
//                tile = new Tile("pkmnmansion_floor2", tile.position.cpy(), true, null);
//                currLayer.put(tile.position.cpy(), tile);
//            }
            alreadyChecked.add(tile.position);
            checkTiles.add(currLayer.get(tile.position.cpy().add(-16, 0)));
            checkTiles.add(currLayer.get(tile.position.cpy().add(16, 0)));
            checkTiles.add(currLayer.get(tile.position.cpy().add(0, -16)));
            checkTiles.add(currLayer.get(tile.position.cpy().add(0, 16)));
//            System.out.println(checkTiles.size());
        }
        return false;
    }

    public int getLayer(){
        return this.layer;
    }

    @Override
    public void step(Game game) {
        // TODO: remove if you want to stagger adding the tiles.
        game.map.tiles.putAll(this.tilesToAdd);
        this.tilesToAdd.clear();

        for (int i = 0; i < this.interiorTilesToAdd.size(); i++) {
            HashMap<Vector2, Tile> tiles = this.interiorTilesToAdd.get(i);
            if (tiles == null) {
                continue;
            }
            if (game.map.interiorTiles.get(i) == null) {
                game.map.interiorTiles.remove(i);
                game.map.interiorTiles.add(i, new HashMap<Vector2, Tile>());
            }
            System.out.println(i);
            game.map.interiorTiles.get(i).putAll(this.interiorTilesToAdd.get(i));
        }
        this.interiorTilesToAdd.clear();

        for (Pokemon pokemon : this.pokemonToAdd.values()) {
            game.insertAction(pokemon.standingAction);
            // TODO: ideally wouldn't do this but not sure what to do
            // Nido family requires 'ground' habitat.
            if (pokemon.specie.name.contains("nido") && pokemon.gender.equals("female")) {
                game.map.tiles.put(pokemon.position.cpy(), new Tile("mountain3", pokemon.position.cpy(), true));
            }
            else if (pokemon.specie.name.equals("charizard") && pokemon.gender.equals("female")) {
                game.map.tiles.put(pokemon.position.cpy(), new Tile("green1", "campfire1", pokemon.position.cpy(), true));
            }
        }
        this.pokemonToAdd.clear();

        for (Pokemon pokemon : this.interiorPokemon.keySet()) {
            if (!game.map.pokemon.containsKey(pokemon.position)) {
                int interiorIndex = this.interiorPokemon.get(pokemon);
                game.insertAction(pokemon.standingAction);
                pokemon.mapTiles = game.map.interiorTiles.get(interiorIndex);
                pokemon.interiorIndex = interiorIndex;
                game.map.pokemon.put(pokemon.position.cpy(), pokemon);
            }
        }
        this.interiorPokemon.clear();
        

        for (Vector2 position : this.trapinchSpawns) {
            Tile tile = game.map.overworldTiles.get(position);
            if (tile == null) {
                continue;
            }
            // Only place trapinch on 'sand pit' tiles
            if (!tile.name.equals("desert2")) {
                continue;
            }
            Pokemon trapinch = new Pokemon("trapinch", 22, Pokemon.Generation.CRYSTAL);
            trapinch.mapTiles = game.map.overworldTiles;
            trapinch.position = position.cpy();
            trapinch.isTrapping = true;
            // NOTE: Doing game.insertAction on a thread other than
            // the Gdx thread was causing issues. Calling
            // game.insertAction() will sometimes cause concurrent
            // modification of the actionStack (I think), since
            // this is happening in separate thread from game
            // thread. If this needs to be called from a different
            // thread, need to post a runnable to the gdx thread.
            game.insertAction(trapinch.new Burrowed());
            tile.items().put("trapinch", 1);
        }
        this.trapinchSpawns.clear();

        if (this.tilesToAdd.isEmpty()) {
            if (this.doActions.isEmpty()) {
                game.actionStack.remove(this);

                // TODO: probably needs to be handled outside of this action
                for (Tile edgeTile : this.edges) {
                    game.map.edges.add(edgeTile.position.cpy());
                }

                Vector2 startLoc = this.edges.get(game.map.rand.nextInt(this.edges.size())).position;
//                System.out.println("startLoc");
//                System.out.println(startLoc);

                game.map.bottomLeft = this.bottomLeft;
                game.map.topRight = this.topRight;

                // TODO: uncomment
                game.player.position.set(startLoc);
                game.player.spawnLoc.set(startLoc);
                game.cam.position.set(startLoc.x+16, startLoc.y, 0);
                return;
            }
            Action currAction = this.doActions.get(0);
            this.doActions.remove(0);
            game.insertAction(currAction);
            game.actionStack.remove(this);
            return;
        }

        // TODO: this causes bugs when player tries to move
        // may revisit
//        Tile currTile = this.tilesToAdd.values().iterator().next();
//        game.map.tiles.put(currTile.position.cpy(), currTile);
//        this.tilesToAdd.remove(currTile.position.cpy());
//
//        // do i  more times (to speed up)
//        for (int i=0; i < 1000; i++) {
//            if (!this.tilesToAdd.isEmpty()) {
//                currTile = this.tilesToAdd.values().iterator().next();
//                game.map.tiles.put(currTile.position.cpy(), currTile);
////                // TODO: handle this better?
////                if (currTile.attrs.get("tree")) {
////                    game.map.trees.put(currTile.position.cpy(), currTile);
////                }
//                this.tilesToAdd.remove(currTile.position.cpy());
//            }
//            else {
//                break;
//            }
//        }
    }
}

class MazeNode {
    int x;
    int y;
    boolean leftOpen;
    boolean downOpen;
    boolean isOpen[];
    int size;

    String type; // forest, platform1, ...
    String rampLoc; // ramp location for platforms
    // boolean bottomRamp, leftRamp; // for platforms, if ramp should be on bottom or left (false if top or right)

    // have to pass in array to specify if dirs open or not
    public MazeNode(int x, int y, boolean isOpen[], int size) {
        this.x = x;
        this.y = y;

        leftOpen = isOpen[0];
        downOpen = isOpen[1];

        this.isOpen = isOpen;

        this.size = size;
        this.type = "";
        this.rampLoc = "";
    }

}

/* unused code

// make two mazenodes 'platform' nodes
 *
 *
        // make a random pair (for now) of maze nodes to be platforms
        List<Vector2> keysAsArray = new ArrayList<Vector2>(nodes.keySet());
        Vector2 platform1 = keysAsArray.get(this.rand.nextInt(keysAsArray.size()));

        // if there is a val to the right, and right wall is open, add to otherNodes
        ArrayList<Vector2> otherNodes = new ArrayList<Vector2>();
        Vector2 checkNode = platform1.cpy().add(1, 0);
        if (nodes.get(checkNode) != null) {
            if (nodes.get(checkNode).leftOpen)
                otherNodes.add(checkNode);
        }
        // if node to the left and this leftopen, add
        checkNode = platform1.cpy().add(-1, 0);
        if (nodes.get(checkNode) != null) {
            if (nodes.get(platform1).leftOpen)
                otherNodes.add(checkNode);
        }
        // if node above and above is downOpen, add
        checkNode = platform1.cpy().add(0, 1);
        if (nodes.get(checkNode) != null) {
            if (nodes.get(checkNode).downOpen)
                otherNodes.add(checkNode);
        }
        // if node below and this is downOpen, add
        checkNode = platform1.cpy().add(0, -1);
        if (nodes.get(checkNode) != null) {
            if (nodes.get(platform1).downOpen)
                otherNodes.add(checkNode);
        }

        // should be guaranteed to have two.
        if (!otherNodes.isEmpty()) {
            nodes.get(platform1).type = "platform1";
            Vector2 platform2 = otherNodes.get(this.rand.nextInt(otherNodes.size()));
            nodes.get(platform2).type = "platform1";
            if (platform1.x < platform2.x) {
                nodes.get(platform1).rampLoc = "left";
                nodes.get(platform2).rampLoc = "right";
            }
            else if (platform1.x > platform2.x) {
                nodes.get(platform1).rampLoc = "right";
                nodes.get(platform2).rampLoc = "left";
            }
            else if (platform1.y < platform2.y) {
                nodes.get(platform1).rampLoc = "down";
                nodes.get(platform2).rampLoc = "up";
            }
            else if (platform1.y > platform2.y) {
                nodes.get(platform1).rampLoc = "up";
                nodes.get(platform2).rampLoc = "down";
            }
        }

// unused getTile code - now these groups are marked 'solid' and 'nonsolid'(empty string)
 // then, biome is applied

        // bl corner
        if (Arrays.equals(node.isOpen, new boolean[]{false, false})) {
            // 1

            format = new String[][]{
                                   new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",            "",                         "",                             rn[this.rand.nextInt(rn.length)],     rn[this.rand.nextInt(rn.length)]},

                                   new String[]{"tree_large1",             "tree_large1_noSprite",            "bush1",                     "",                             rn[this.rand.nextInt(rn.length)],     rn[this.rand.nextInt(rn.length)]},

                                   new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",            "tree_large1_noSprite",     "tree_large1_noSprite",            "",                                 ""},

                                   new String[]{"tree_large1",             "tree_large1_noSprite",            "tree_large1",                 "tree_large1_noSprite",         "bush1",                             ""},

                                   new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",            "tree_large1_noSprite",     "tree_large1_noSprite",            "tree_large1_noSprite",             "tree_large1_noSprite",},

                                   new String[]{"tree_large1",             "tree_large1_noSprite",            "tree_large1",                 "tree_large1_noSprite",            "tree_large1",                         "tree_large1_noSprite",},
                                 };
            square = GenTiles(format, offSetx, offSetY);

            squares.add(square);

            // 2
            format = new String[][]{
                       new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",            "",                     "",                             "tree_large1_noSprite", "tree_large1_noSprite"},

                       new String[]{"tree_large1",             "tree_large1_noSprite",            "",                     "",                             "tree_large1",             "tree_large1_noSprite"},

                       new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",            rs[this.rand.nextInt(rs.length)], "",                     "",                     ""},

                       new String[]{"tree_large1",             "tree_large1_noSprite",            "bush1",                 rs[this.rand.nextInt(rs.length)], "",                     ""},

                       new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",            "tree_large1_noSprite", "tree_large1_noSprite",            "tree_large1_noSprite", "tree_large1_noSprite",},

                       new String[]{"tree_large1",             "tree_large1_noSprite",            "tree_large1",             "tree_large1_noSprite",            "tree_large1",             "tree_large1_noSprite",},
                     };
            square = GenTiles(format, offSetx, offSetY);

            squares.add(square);

        }
        // maze node left or down is open (but not both)
        else if ( node.leftOpen ^ node.downOpen ) {
            // 1
            format = new String[][]{
                       new String[]{rs[this.rand.nextInt(rs.length)], rs[this.rand.nextInt(rs.length)], rs[this.rand.nextInt(rs.length)], rs[this.rand.nextInt(rs.length)], rs[this.rand.nextInt(rs.length)], rs[this.rand.nextInt(rs.length)]},

                       new String[]{"",                         "",                                "",                     "",                             "",                     ""},

                       new String[]{"",                         "",                                "",                     "",                             "",                     ""},

                       new String[]{"",                         "",                                "",                     "",                             "",                     ""},

                       new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",            "tree_large1_noSprite", "tree_large1_noSprite",            "tree_large1_noSprite", "tree_large1_noSprite",},

                       new String[]{"tree_large1",             "tree_large1_noSprite",            "tree_large1",             "tree_large1_noSprite",            "tree_large1",             "tree_large1_noSprite",},
                     };
            if (node.downOpen == true) {
                FlipFormat(format);
                // debug - FlipFormat mirrors about x = y line
//                for (String[] sub : format) {
//                    for (String tileName : sub) {
//                        System.out.print(""+String.valueOf(tileName)+"\t\t\t\t");
//                    }
//                    System.out.print("\n");
//                }

            }
            square = GenTiles(format, offSetx, offSetY);
            squares.add(square);

            // 2
            format = new String[][]{
                       new String[]{"",                         "",                                "",                     "",                             "tree_large1_noSprite", "tree_large1_noSprite"},

                       new String[]{"",                         "",                                "",                     "bush1",                         "tree_large1",             "tree_large1_noSprite"},

                       new String[]{"",                         "",                                "",                     "",                             "",                     ""},

                       new String[]{"",                         "",                                "",                     "",                             "",                     ""},

                       new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",            "tree_large1_noSprite", "tree_large1_noSprite",            "tree_large1_noSprite", "tree_large1_noSprite",},

                       new String[]{"tree_large1",             "tree_large1_noSprite",            "tree_large1",             "tree_large1_noSprite",            "tree_large1",             "tree_large1_noSprite",},
                     };
            if (node.downOpen == true) {
                FlipFormat(format);
            }
            square = GenTiles(format, offSetx, offSetY);
            squares.add(square);

        }
        // maze node bottom and left is open
        else {
            // 1
//            square = new ArrayList<Tile>(); // no tiles
//            squares.add(square);

            // 2
            format = new String[][]{
                       new String[]{"",                         "",                                "",                         "",                             "tree_large1_noSprite",             "tree_large1_noSprite"},

                       new String[]{"",                         "",                                "",                         rs[this.rand.nextInt(rs.length)], "tree_large1",                     "tree_large1_noSprite"},

                       new String[]{"",                         "",                                "",                         "",                             rs[this.rand.nextInt(rs.length)],     ""},

                       new String[]{"",                         rs[this.rand.nextInt(rs.length)],"",                         "",                             "",                                 ""},

                       new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",            rs[this.rand.nextInt(rs.length)],"",                        "",                                 "",},

                       new String[]{"tree_large1",             "tree_large1_noSprite",            "",                         "",                                "",                                 "",},
                     };
            square = GenTiles(format, offSetx, offSetY);
            squares.add(square);

            // 3 - all qmark.
            format = new String[][]{
                       new String[]{"qmark",     "qmark",            "qmark",                         "qmark",                             "qmark",             "qmark"},

                       new String[]{"qmark",     "qmark",            "qmark",                         "qmark",                             "qmark",             "qmark"},

                       new String[]{"qmark",     "qmark",            "qmark",                         "qmark",                             "qmark",             "qmark"},

                       new String[]{"qmark",     "qmark",            "qmark",                         "qmark",                             "qmark",             "qmark"},

                       new String[]{"qmark",     "qmark",            "qmark",                         "qmark",                            "qmark",             "qmark",},

                       new String[]{"qmark",     "qmark",            "qmark",                         "qmark",                            "qmark",             "qmark",},
                     };
            square = GenTiles(format, offSetx, offSetY);
            squares.add(square);
        }

// unused maze node down code (mirrored now)
// maze node bottom is open
else if (Arrays.equals(node.isOpen, new boolean[]{false, true})) {
    // TODO - flip the 'left open' tiles for this.

    // 1
    format = new String[][]{
               new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",            "",                         "",                             rt[this.rand.nextInt(rt.length)],     rt[this.rand.nextInt(rt.length)]},

               new String[]{"tree_large1",             "tree_large1_noSprite",            "",                         "",                             rt[this.rand.nextInt(rt.length)],     rt[this.rand.nextInt(rt.length)]},

               new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",            "",                         "",                             "",                                 ""},

               new String[]{"tree_large1",             "tree_large1_noSprite",            "",                         "",                             "",                                 ""},

               new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",            "",                         "",                                "",                                 "",},

               new String[]{"tree_large1",             "tree_large1_noSprite",            "",                         "",                                "",                                 "",},
             };
    square = GenTiles(format, offSetx, offSetY);
    squares.add(square);

    // 2
    format = new String[][]{
               new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",            "",                         "",                             "tree_large1_noSprite",             "tree_large1_noSprite"},

               new String[]{"tree_large1",             "tree_large1_noSprite",            "",                         "",                             "tree_large1",                         "tree_large1_noSprite"},

               new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",            "",                         "",                             "",                                 ""},

               new String[]{"tree_large1",             "tree_large1_noSprite",            "",                         "",                             "",                                 ""},

               new String[]{"tree_large1_noSprite",     "tree_large1_noSprite",            "",                         "",                                "",                                 "",},

               new String[]{"tree_large1",             "tree_large1_noSprite",            "",                         "",                                "",                                 "",},
             };
    square = GenTiles(format, offSetx, offSetY);
    squares.add(square);

}*/
