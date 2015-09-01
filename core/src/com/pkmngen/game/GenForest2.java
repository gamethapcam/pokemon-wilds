package com.pkmngen.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.GenForest1.AddPlatform;



//idea - when creating a maze that 'has to go to one spot', 
 //as more nodes are added to 'visited nodes', increase the odds
 //of choosing node in right direction slightly. that way maze
 //will be 'geared around' that area. note that if u generate 
 //a maze filling a solid square, you can put entrance and exit anywhere.
 //ie point is to create a maze that isn't too complex, and heads generally
 //from entrance to exit

class MazeNode {
	
	int x, y;
	boolean leftOpen, downOpen;
	boolean isOpen[];
	int size; 
	
	String type; //forest, platform1, ...
	String rampLoc; //ramp location for platforms
	//boolean bottomRamp, leftRamp; //for platforms, if ramp should be on bottom or left (false if top or right)
	
	//have to pass in array to specify if dirs open or not
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


public class GenForest2 extends Action {


	public int layer = 120;
	public int getLayer(){return this.layer;}
	
	
	ArrayList<Vector2> freePositions;
	
	ArrayList<Tile> tilesToAdd;

	ArrayList<Action> doActions;
	
	Vector2 topLeft, bottomRight;

	Random rand;
	

	
	@Override
	public void step(PkmnGen game) {
		

		if (this.tilesToAdd.isEmpty()) {
			if (this.doActions.isEmpty()) {
				game.actionStack.remove(this);
				return;
			}
			Action currAction = this.doActions.get(0);
			this.doActions.remove(0);
			PublicFunctions.insertToAS(game, currAction);
			game.actionStack.remove(this);
			return;
		}

		Tile currTile = this.tilesToAdd.get(0);
		game.map.tiles.put(currTile.position.cpy(), currTile);
		this.tilesToAdd.remove(0);
		
		//do twice more (to speed up)
		if (!this.tilesToAdd.isEmpty()) {
			currTile = this.tilesToAdd.get(0);
			game.map.tiles.put(currTile.position.cpy(), currTile);
			this.tilesToAdd.remove(0);
		}
		if (!this.tilesToAdd.isEmpty()) {
			currTile = this.tilesToAdd.get(0);
			game.map.tiles.put(currTile.position.cpy(), currTile);
			this.tilesToAdd.remove(0);
		}
		
	}


	public GenForest2(PkmnGen game, Vector2 startLoc, Vector2 endLoc) {
		
		//TODO - 
		 //get some ideas from GenForest1 for templates to make
		 //somehow raised platforms can't cut off areas for access
		
		
		this.rand = new Random();
		this.tilesToAdd = new ArrayList<Tile>();
		this.freePositions = new ArrayList<Vector2>();
		this.doActions = new ArrayList<Action>();
		
		//a bunch of arraylists of Vector2/Tile Maps
		 //these represent the 3x3 pieces
		 //maybe have 'generate' function that will return a random of certain orientation?
		
		//random maze height/width
		
		//for now, just return a maze that is a square
		
		//for node in maze, get a mazeTile that has correct num openings
		 //pass in node, whatever kind of struct that is
		int squareSize = 3*32;
		
		//TODO - delete below code, or hardcode a preset maze to test
//		ArrayList<MazeNode> nodes = new ArrayList<MazeNode>();
//		nodes.add(new MazeNode(0,0,new boolean[]{true,false,false,true}, squareSize)); //upOpen, downOpen, leftOpen, rightOpen, x, y
//		nodes.add(new MazeNode(1,0,new boolean[]{true,false,true,false}, squareSize));
//		nodes.add(new MazeNode(0,1,new boolean[]{false,true,false,true}, squareSize));
//		nodes.add(new MazeNode(1,1,new boolean[]{false,true,true,false}, squareSize));
		
		//Maze_Algo1 uses python example code from maze algo wiki
		 //using because it can generate patches that are 'fully open', ie no walls
		 //also is pretty adjustable
		 //there are other good algo's - 'Recursive backtracker' with horizontal bias looks fun. could adapt algo1 with similar idea.
		int width = 8; //(int)((endLoc.x - startLoc.x)/squareSize);
		int height = 10; //(int)((endLoc.y - startLoc.y)/squareSize);
		float density = .2f; //num times a 'long wall' is created. algo works by creating a long wall at random
		float complexity = .3f; //length of that 'long wall'
		HashMap<Vector2, MazeNode> nodes = Maze_Algo1(width, height, density, complexity, squareSize);

		
		//make a random pair (for now) of maze nodes to be platforms
		List<Vector2> keysAsArray = new ArrayList<Vector2>(nodes.keySet());
		Vector2 platform1 = keysAsArray.get(this.rand.nextInt(keysAsArray.size()));

		//if there is a val to the right, and right wall is open, add to otherNodes
		ArrayList<Vector2> otherNodes = new ArrayList<Vector2>();
		Vector2 checkNode = platform1.cpy().add(1, 0);
		if (nodes.get(checkNode) != null) {
			if (nodes.get(checkNode).leftOpen)
				otherNodes.add(checkNode);
		}
		//if node to the left and this leftopen, add
		checkNode = platform1.cpy().add(-1, 0);
		if (nodes.get(checkNode) != null) {
			if (nodes.get(platform1).leftOpen)
				otherNodes.add(checkNode);
		}
		//if node above and above is downOpen, add
		checkNode = platform1.cpy().add(0, 1);
		if (nodes.get(checkNode) != null) {
			if (nodes.get(checkNode).downOpen)
				otherNodes.add(checkNode);
		}
		//if node below and this is downOpen, add
		checkNode = platform1.cpy().add(0, -1);
		if (nodes.get(checkNode) != null) {
			if (nodes.get(platform1).downOpen)
				otherNodes.add(checkNode);
		}
		
		//should be guaranteed to have two. 
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
		
		
		
		//for each node, get a template tile that matches it's number of openings
		 //nodes contains the nodes of the maze
		ArrayList<Tile> tileSquare;
		for (MazeNode node : nodes.values()) {
			//get tiles depending on type of mazenode
			if (node.type == "platform1") {
				tileSquare = getTileSquarePlatform1(node, startLoc);
			}
			else {
				tileSquare = getTileSquare(node, startLoc);
			}
			

			//need to account for offset
			//add these to tilesToAdd
			for (Tile tile : tileSquare) {
				
				//if it's a qmark_tile, add it to 'freePositions' as well
				if (tile.attrs.get("qmark") == true) {
					this.freePositions.add(tile.position.cpy()); 
				}
				
				//translate tile to be in line with map
				 //bottom left corner node.x == 0, node.y == 0
				this.tilesToAdd.add(tile);
			}
		}

		//TODO - apply forest biome
		//this.doActions.add(new ApplyForestBiome(game, this.tilesToAdd, startLoc, endLoc, this)); //if you want to apply later
		//actually just - Action temp = new ApplyForestBiome(game, this.tilesToAdd, startLoc, endLoc, this)
		//temp.step(game); //this way map is rendered in pretty way, but apply biome is still an action
		 //contains placement of water and grass


		//add platform - didn't end up liking this method
		//this.doActions.add(new AddPlatform(game, this.tilesToAdd, this.freePositions, startLoc, endLoc, this));

		
		//set start point 
		
		//randomly plant grass/tree/flowers
		
		//fill in platform or pond
		
		//do more (until <= certain amt of freePositions left?)
		
		//fill in rest with grass
		
		
	}
	

	public ArrayList<Tile> getTileSquarePlatform1(MazeNode node, Vector2 startLoc) {
		
		//README - when creating these, DONT block the middle two tiles of walls that should be open.
		 //ex: bottom open
//		1 1 X X 0 0
//		1 1 0 0 0 0
//		1 1 0 0 0 0
//		1 1 0 0 0 0
//		1 1 0 0 0 0
//		1 1 X X 0 0
		//don't block the X's - if two of the below ever break this rule, and are put adjacent, system doesn't work

		//list1.equals(list2)
		//Arrays.equals(a, b)

		int offSetx = node.size*node.x +(int)startLoc.x;
		int offSetY = node.size*node.y +(int)startLoc.y;
		
		ArrayList<ArrayList<Tile>> squares = new ArrayList<ArrayList<Tile>>();
		ArrayList<Tile> square = new ArrayList<Tile>();

		String[][] format = new String[][]{};
		
		String[] rn = {"ground1", "qmark", "grass1"}; //random non-solid objects
		 //bug - qmark can potentially be solid
		
		//choose from temp because I want more uniformity
		String[] temp = {"bush1", "tree_small1"};
		String[] rs = {temp[this.rand.nextInt(temp.length)], "ground1", "ground1"}; //random solid or non-solid objects


		 
		//bl corner
		if (!node.leftOpen && !node.downOpen) {
			
			if (node.rampLoc == "down") {
				//1
				format = new String[][]{
			 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",			"ledge_grass_left", 		"", 							"", 								"ledge_grass_right"},
			 			  
			 			  new String[]{"tree_large1", 			"tree_large1_noSprite",			"ledge_grass_left", 		"", 							"", 								"ledge_grass_right"},
			 			  
			 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",			"ledge_grass_left", 		"ledge_grass_inside_tl",		"ledge_grass_ramp", 				"ledge1_corner_br"},
			 			  
			 			  new String[]{"tree_large1", 			"tree_large1_noSprite",			"ledge1_corner_bl", 		"ledge1_corner_br", 			"", 								""},
			 			  
			 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",			"tree_large1_noSprite", 	"tree_large1_noSprite",			"tree_large1_noSprite", 			"tree_large1_noSprite",},
			 			  
			 			  new String[]{"tree_large1", 			"tree_large1_noSprite",			"tree_large1", 				"tree_large1_noSprite",			"tree_large1", 						"tree_large1_noSprite",},
						 };
				square = GenTiles(format, offSetx, offSetY);
				 
				squares.add(square);
			}
			else {
				//1
				format = new String[][]{
			 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",			"", 						"ledge2_corner_tl", 			"ledge_grass_safari_up", 			"ledge_grass_safari_up"},
			 			  
			 			  new String[]{"tree_large1", 			"tree_large1_noSprite",			"", 						"ledge_grass_left", 			"", 								""},
			 			  
			 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",			"", 						"ledge1_corner_bl",				"ledge_grass_ramp", 				"ledge_grass_inside_tr"},
			 			  
			 			  new String[]{"tree_large1", 			"tree_large1_noSprite",			"", 						"", 							"", 								"ledge1_corner_bl"},
			 			  
			 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",			"tree_large1_noSprite", 	"tree_large1_noSprite",			"tree_large1_noSprite", 			"tree_large1_noSprite",},
			 			  
			 			  new String[]{"tree_large1", 			"tree_large1_noSprite",			"tree_large1", 				"tree_large1_noSprite",			"tree_large1", 						"tree_large1_noSprite",},
						 };
				square = GenTiles(format, offSetx, offSetY);
				 
				squares.add(square);
			}
			//TODO - handle case of both false (small platform in middle i guess
			 
		}
		//maze node left or down is open (but not both)
		else if ( node.leftOpen == true && node.downOpen == false ) {

			if (node.rampLoc == "down") {
				//1
				format = new String[][]{
			 			  new String[]{rs[this.rand.nextInt(rs.length)], rs[this.rand.nextInt(rs.length)], rs[this.rand.nextInt(rs.length)], rs[this.rand.nextInt(rs.length)], rs[this.rand.nextInt(rs.length)], rs[this.rand.nextInt(rs.length)]},
			 			  
			 			  new String[]{"ledge_grass_left", 						"",								"", 					"", 			"", 					"ledge_grass_right"},
			 			  
			 			  new String[]{"ledge1_corner_bl", 		"ledge_grass_down",				"ledge_grass_down", 	"ledge_grass_ramp", 			"ledge_grass_down", 	"ledge1_corner_br"},
			 			  
			 			  new String[]{"", 						"",								"", 					"", 							"", 					""},
			 			  
			 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",			"tree_large1_noSprite", "tree_large1_noSprite",			"tree_large1_noSprite", "tree_large1_noSprite",},
			 			  
			 			  new String[]{"tree_large1", 			"tree_large1_noSprite",			"tree_large1", 			"tree_large1_noSprite",			"tree_large1", 			"tree_large1_noSprite",},
						 };
				square = GenTiles(format, offSetx, offSetY);
				squares.add(square);
			}
			else if (node.rampLoc == "left") {
				//1
				format = new String[][]{
			 			  new String[]{"", 						"",								"", 					"ledge2_corner_tl", 			"ledge_grass_safari_up","ledge_grass_safari_up"},
			 			  
			 			  new String[]{"", 						"",								"", 					"ledge_grass_left", 			"", 					""},
			 			  
			 			  new String[]{"", 						"",								"", 					"ledge1_corner_bl", 			"ledge_grass_ramp", 	"ledge_grass_inside_tr"},
			 			  
			 			  new String[]{"", 						"",								"", 					"", 							"", 					"ledge1_corner_bl"},
			 			  
			 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",			"tree_large1_noSprite", "tree_large1_noSprite",			"tree_large1_noSprite", "tree_large1_noSprite",},
			 			  
			 			  new String[]{"tree_large1", 			"tree_large1_noSprite",			"tree_large1", 			"tree_large1_noSprite",			"tree_large1", 			"tree_large1_noSprite",},
						 };
				square = GenTiles(format, offSetx, offSetY);
				squares.add(square);
			}
			else {
				//1
				format = new String[][]{
			 			  new String[]{"ledge_grass_safari_up", "ledge_grass_safari_up",		"ledge_grass_safari_up","ledge2_corner_tr", 			"", 					""},
			 			  
			 			  new String[]{"", 						"",								"", 					"ledge_grass_right", 			"", 					""},
			 			  
			 			  new String[]{"", 						"ledge_grass_inside_tl",		"ledge_grass_ramp", 	"ledge1_corner_br", 			"", 					""},
			 			  
			 			  new String[]{"ledge_grass_down", 		"ledge1_corner_br",				"", 					"", 							"", 					""},
			 			  
			 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",			"tree_large1_noSprite", "tree_large1_noSprite",			"tree_large1_noSprite", "tree_large1_noSprite",},
			 			  
			 			  new String[]{"tree_large1", 			"tree_large1_noSprite",			"tree_large1", 			"tree_large1_noSprite",			"tree_large1", 			"tree_large1_noSprite",},
						 };
				square = GenTiles(format, offSetx, offSetY);
				squares.add(square);
				
			}
		}
		//maze node left or down is open (but not both)
		else if ( node.leftOpen == false && node.downOpen == true ) {

			if (node.rampLoc == "down") {
				//1
				format = new String[][]{
			 			  new String[]{"tree_large1_noSprite", "tree_large1_noSprite", 	"ledge_grass_left", 	"", 							"", 					"ledge_grass_right"},
			 			  
			 			  new String[]{"tree_large1", 			"tree_large1_noSprite",	"ledge_grass_left", 	"", 							"", 					"ledge_grass_right"},
			 			  
			 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",	"ledge1_corner_bl", 	"ledge_grass_ramp", 			"ledge_grass_down", 	"ledge1_corner_br"},
			 			  
			 			  new String[]{"tree_large1", 			"tree_large1_noSprite",	"", 					"", 							"", 					""},
			 			  
			 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",	"", 					"",								"", 					""},
			 			  
			 			  new String[]{"tree_large1", 			"tree_large1_noSprite",	"", 					"",								"", 					""},
						 };
				square = GenTiles(format, offSetx, offSetY);
				squares.add(square);
			}
			else if (node.rampLoc == "left") {
				//1
				format = new String[][]{
			 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",								"", 					"ledge2_corner_tl", 			"ledge_grass_safari_up","ledge_grass_safari_up"},
			 			  
			 			  new String[]{"tree_large1", 			"tree_large1_noSprite",								"", 					"ledge_grass_left", 			"", 					""},
			 			  
			 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",								"", 					"ledge1_corner_bl", 			"ledge_grass_ramp", 	"ledge_grass_inside_tr"},
			 			  
			 			  new String[]{"tree_large1", 			"tree_large1_noSprite",								"", 					"", 							"", 					"ledge1_corner_bl"},
			 			  
			 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",			"", "",			"", ""},
			 			  
			 			  new String[]{"tree_large1", 			"tree_large1_noSprite",			"", 			"",			"", 			""},
						 };
				square = GenTiles(format, offSetx, offSetY);
				squares.add(square);
			}
			else {
				//1
				format = new String[][]{
						  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",				"", 						"", 							"", 								""},
			 			  
			 			  new String[]{"tree_large1", 			"tree_large1_noSprite",				"ledge_grass_safari_up", 	"ledge_grass_safari_up", 		"ledge_grass_safari_up", 			"ledge2_corner_tr"},
			 			  
			 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",				"", 						"", 							"", 								"ledge_grass_right"},
			 			  
			 			  new String[]{"tree_large1", 			"tree_large1_noSprite",				"", 						"ledge_grass_inside_tl", 		"ledge_grass_ramp", 				"ledge1_corner_br"},
			 			  
			 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",				"",							"ledge_grass_right",			"", 								"",},
			 			  
			 			  new String[]{"tree_large1", 			"tree_large1_noSprite",				"", 						"ledge_grass_safari_up",		"ledge_grass_safari_up", 			"ledge2_corner_tr",},
						 };
				square = GenTiles(format, offSetx, offSetY);
				squares.add(square);
				
			}
		}
		//maze node bottom and left is open
		else {
			//1
//			square = new ArrayList<Tile>(); //no tiles
//			squares.add(square);

			//bottom ramp
			if (node.rampLoc == "down") {
				//1
				format = new String[][]{
						  new String[]{"", 						"",								"ledge_grass_left", 					"", 			"", 					"ledge_grass_right"},
			 			  
			 			  new String[]{"", 						"",								"ledge1_corner_bl", 		"ledge_grass_ramp", 			"ledge_grass_down", 	"ledge1_corner_br"},
			 			  
			 			  new String[]{"", 						"",								"", 						"", 							rs[this.rand.nextInt(rs.length)], 	""},
			 			  
			 			  new String[]{"", 						rs[this.rand.nextInt(rs.length)],"", 						"", 							"", 								""},
			 			  
			 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",			rs[this.rand.nextInt(rs.length)],"",						"", 								"",},
			 			  
			 			  new String[]{"tree_large1", 			"tree_large1_noSprite",			"", 						"",								"", 								"",},
						 };
				square = GenTiles(format, offSetx, offSetY);
				squares.add(square);
			}
			//top ramp
			else if (node.rampLoc == "up") { 
				//2
				format = new String[][]{
						  new String[]{"", 						"",								"", 						"", 							"", 					""},
			 			  
			 			  new String[]{"", 						"ledge2_corner_tl",				"ledge_grass_safari_up", 	"ledge_grass_safari_up", 		"ledge_grass_safari_up", 			"ledge2_corner_tr"},
			 			  
			 			  new String[]{"", 						"ledge_grass_left",				"", 						"", 							"", 								"ledge_grass_right"},
			 			  
			 			  new String[]{"", 						"ledge1_corner_bl",				"ledge_grass_ramp", 		"ledge_grass_inside_tr", 		"", 								"ledge_grass_right"},
			 			  
			 			  new String[]{"", 						"",								"",							"ledge_grass_left",				"", 								"ledge_grass_right",},
			 			  
			 			  new String[]{"", 						"",								"ledge2_corner_tl", 		"ledge_grass_safari_up",		"", 								"ledge_grass_right",},
						 };
				square = GenTiles(format, offSetx, offSetY);
				squares.add(square);
			}
			//left ramp
			else if (node.rampLoc == "left") { 
				//1
				format = new String[][]{
			 			  new String[]{"", 						"",								"ledge_grass_left", 	"", 			"", 					""},
			 			  
			 			  new String[]{"", 						"",								"ledge_grass_left", 	"", 			"", 					""},
			 			  
			 			  new String[]{"", 						"",								"ledge1_corner_br", 	"ledge_grass_ramp", 			"ledge_grass_inside_tr", 				""},
			 			  
			 			  new String[]{"", 						"",								"", 					"", 							"ledge_grass_left", 					""},
			 			  
			 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",			"", 					"",								"ledge_grass_left", 					"",},
			 			  
			 			  new String[]{"tree_large1", 			"tree_large1_noSprite",			"", 					"",								"ledge1_corner_br", 					"ledge_grass_down",},
						 };
				if (node.downOpen == true) {
					FlipFormat(format);
				}
				square = GenTiles(format, offSetx, offSetY);
				squares.add(square);
			}
			//right ramp
			else {
				//1
				format = new String[][]{
			 			  new String[]{"", 						"",								"", 					"ledge_grass_right", 			"", 					""},
			 			  
			 			  new String[]{"", 						"",								"", 					"ledge_grass_right", 			"", 					""},
			 			  
			 			  new String[]{"", 						"ledge_grass_inside_tl",		"ledge_grass_ramp", 	"ledge1_corner_br", 			"", 					""},
			 			  
			 			  new String[]{"", 						"ledge_grass_right",				"", 					"", 							"", 					""},
			 			  
			 			  new String[]{"", 						"ledge_grass_right",			"",						"",								"tree_large1_noSprite", "tree_large1_noSprite",},
			 			  
			 			  new String[]{"ledge_grass_down", 		"ledge1_corner_br",			"", 					"",								"tree_large1", 			"tree_large1_noSprite",},
						 };
				if (node.downOpen == true) {
					FlipFormat(format);
				}
				square = GenTiles(format, offSetx, offSetY);
				squares.add(square);
			}
			
		}

		//choose randomly from ArrayList of squares
		int randomNum = this.rand.nextInt(squares.size());
		//randomNum = 0; //debug
		return squares.get(randomNum);
		
	}
	
	
	//unused - switched to templated tile method
	public class AddPlatform extends Action {

		public int layer = 120;
		public int getLayer(){return this.layer;}

		Random rand;

		ArrayList<Tile> tilesToAdd;
		ArrayList<Vector2> freePositions;

		Vector2 startPosition; //top-left corner
		
		Action nextAction;
		
		int numLevels; 
		//which sprite to use, ie two level mtn or 1 level
		 //can potentially trace inside of raised area later
		
		Vector2 bottomLeft;
		Vector2 topRight;
		

		@Override
		public void step(PkmnGen game) {
			
			//this step gets executed only once
			
			//TODO - need to approach this differently
			 //it cuts off too many routes.
			//would prefer if entrances were positioned only when there is a maze path it needs to complete.
			//

			ArrayList<Vector2> platformGoesHere = new ArrayList<Vector2>();
			
			//assemble a list of qmark tiles in region
			 //TODO - may need to trim out qmarks next to solid block here
			for (Vector2 pos : this.freePositions) {
				if (pos.x >= this.bottomLeft.x && pos.x <= this.topRight.x
					&& pos.y >= this.bottomLeft.y && pos.y <= this.topRight.y) {
					
					
					platformGoesHere.add(pos.cpy());
				}
			}
			
			//if couldn't find anything, return
			if (platformGoesHere.isEmpty()) {
				PublicFunctions.insertToAS(game, this.nextAction);
				game.actionStack.remove(this);
				return;
			}
			
			//keep track of bottom ledges for later when you need to make ramp
			ArrayList<Vector2> bottomLedges = new ArrayList<Vector2>();
			
			//for each qmark_tile
			for (Vector2 pos : platformGoesHere) {
				
				//decide when type of platform it should be
				//Tile currTile;

				boolean upPlatform = false;
				boolean leftPlatform = false;
				boolean rightPlatform = false;
				boolean downPlatform = false;
				//check if right platform
				if (platformGoesHere.contains(new Vector2(pos.x+16, pos.y))) {
					rightPlatform = true;
				}
				//check if left platform
				if (platformGoesHere.contains(new Vector2(pos.x-16, pos.y))) {
					leftPlatform = true;
				}
				//check if up platform
				if (platformGoesHere.contains(new Vector2(pos.x, pos.y+16))) {
					upPlatform = true;
				}
				//check if down platform
				if (platformGoesHere.contains(new Vector2(pos.x, pos.y-16))) {
					downPlatform = true;
				}
				
				//check if left edge
				if (rightPlatform && !leftPlatform && upPlatform && downPlatform) {
					this.tilesToAdd.add(new Tile("ledge_grass_left", new Vector2(pos.x, pos.y) ));
				}
				//check if right edge
				else if (!rightPlatform && leftPlatform && upPlatform && downPlatform) {
					this.tilesToAdd.add(new Tile("ledge_grass_right", new Vector2(pos.x, pos.y) ));
				}
				//check if up ledge
				else if (rightPlatform && leftPlatform && !upPlatform && downPlatform) {
					this.tilesToAdd.add(new Tile("ledge_grass_safari_up", new Vector2(pos.x, pos.y) ));
				}
				//check if down ledge
				else if (rightPlatform && leftPlatform && upPlatform && !downPlatform) {
					this.tilesToAdd.add(new Tile("ledge_grass_down", new Vector2(pos.x, pos.y) ));
					bottomLedges.add(pos);
				}
				//check if bl corner
				else if (rightPlatform && !leftPlatform && upPlatform && !downPlatform) {
					this.tilesToAdd.add(new Tile("ledge1_corner_bl", new Vector2(pos.x, pos.y) ));
				}
				//check if br corner
				else if (!rightPlatform && leftPlatform && upPlatform && !downPlatform) {
					this.tilesToAdd.add(new Tile("ledge1_corner_br", new Vector2(pos.x, pos.y) ));
				}
				//check if tl corner
				else if (rightPlatform && !leftPlatform && !upPlatform && downPlatform) {
					this.tilesToAdd.add(new Tile("ledge2_corner_tl", new Vector2(pos.x, pos.y) ));
				}
				//check if tr corner
				else if (!rightPlatform && leftPlatform && !upPlatform && downPlatform) {
					this.tilesToAdd.add(new Tile("ledge2_corner_tr", new Vector2(pos.x, pos.y) ));
				}
				//check if lone rock //TODO - rock2 as well?
				else if (!rightPlatform && !leftPlatform && !upPlatform && !downPlatform) {
					this.tilesToAdd.add(new Tile("rock1", new Vector2(pos.x, pos.y) ));
				}
				//else, it's a regular platform top
				else {
					this.tilesToAdd.add(new Tile("ground1", new Vector2(pos.x, pos.y) ));
				}
				
				//add that to tile to add to tilesToAdd
				
				//remove from free positions
				this.freePositions.remove(pos);
				
			}
			
			//System.out.println("array size: "+String.valueOf(bottomLedges.size()));
			if (!bottomLedges.isEmpty()) {
				//for ledge in bottomLedges //remove one
				this.tilesToAdd.add( new Tile("ledge_grass_ramp", bottomLedges.get(this.rand.nextInt(bottomLedges.size())).cpy()) );
				//chance to remove a second one
				if (this.rand.nextInt(2) == 1) {
					this.tilesToAdd.add( new Tile("ledge_grass_ramp", bottomLedges.get(this.rand.nextInt(bottomLedges.size())).cpy()) );
				}
			}
				
			//put nextAction on stack, remove this.
			PublicFunctions.insertToAS(game, this.nextAction);
			game.actionStack.remove(this);
			
		}

		public AddPlatform(PkmnGen game, ArrayList<Tile> tilesToAdd, ArrayList<Vector2> freePositions, Vector2 startLoc, Vector2 endLoc,  Action nextAction) {
			
			this.nextAction = nextAction;
			this.rand = new Random();
			this.freePositions = freePositions;
			this.tilesToAdd = tilesToAdd;
			
			
			//choose a random start position within boundaries
			//x and y are midpoints of square (to make the square end up in middle on average)
			//assuming startLoc is bottom left
			int x = this.rand.nextInt((int)(endLoc.x - startLoc.x)) + (int)startLoc.x;
			int y = this.rand.nextInt((int)(endLoc.y - startLoc.y)) + (int)startLoc.y;
			//make variable width about 3/6 of range, +/- 1/6 of range for now
			int width = (int) ( .5f * (endLoc.x - startLoc.x) + ((this.rand.nextInt(10)+1)/10f) * (2f/6f) * (endLoc.x - startLoc.x) - (1f/6f) * (endLoc.x - startLoc.x) );
			int height = (int) ( .5f * (endLoc.y - startLoc.y) + ((this.rand.nextInt(10)+1)/10f) * (2f/6f) * (endLoc.y - startLoc.y) - (1f/6f) * (endLoc.y - startLoc.y) );
			
			
			this.bottomLeft = new Vector2(x - width/2, y - height/2);
			this.topRight = new Vector2(x + width/2, y + height/2);
			
			
		}
		
	}
				

		
	public HashMap<Vector2, MazeNode> Maze_Algo1(int width, int height, float complexity, float density, int squareSize) {

	    // Only odd shapes
		//int[] shape = { (int)(height / 2) * 2 + 1, (int)(width / 2) * 2 + 1 };
		
		//TODO - +1 only if you want to handle edges differently
		//in the future, will likely surround all mazes with trees anyway. change to +1 when you do that.
		width = (int)(width / 2) * 2 + 2;
		height = (int)(height / 2) * 2 + 2;

//        System.out.println("width: "+String.valueOf(width));
//        System.out.println("height: "+String.valueOf(height));
		

	    // Adjust complexity and density relative to maze size
		 //density - num 'long walls'. long walls is a single connected wall
		 //complexity - length of 'long walls'
	    complexity = (int)(complexity * (5 * (height + width)));
	    density    = (int)(density * ( (int)(height / 2) * (int)(width / 2) ));

	    // maze starts as an array of booleans (two layers)
	     //all values default to 'false'
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
	    	//get even number between width and height
	        int x = this.rand.nextInt( (int)(width / 2)  ) * 2; //+1 b/c java excludes this number
	        int y = this.rand.nextInt( (int)(height / 2)  ) * 2;
	        Z[x][y] = Boolean.TRUE;

	        for (int j=0; j < complexity; j++) {

	        	//compile list of neighboring cells
	        	ArrayList<int[]> neighbours = new ArrayList<int[]>();
	            if (x > 1) { neighbours.add(new int[]{x - 2, y}); }
	            if (x < width - 2) { neighbours.add(new int[]{x + 2, y}); }
	            if (y > 1) { neighbours.add(new int[]{x, y - 2}); }
	            if (y < height - 2) { neighbours.add(new int[]{x, y + 2}); }
	        	
	            if (!neighbours.isEmpty()) {
	            	int randomNum = this.rand.nextInt(neighbours.size()); //-1? no, python includes, java excludes this number
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
	    
	    //debug - print maze
	    for (int i=height-1; i >= 0; i--) {
		    for (int j=0; j < width; j++) {
			    System.out.print(String.valueOf(Z[j][i] ? 1 : 0)+" ");
		    } 
		    System.out.print("\n");
	    }

	    HashMap<Vector2, MazeNode> nodes = new HashMap<Vector2, MazeNode>();
	    
	    //convert array of booleans to mazenodes 
	     //need mazenodes so that we can choose templated tile
	     //interpreting Z[0][0] as bottom-left corner
	    for (int i=0; i < (width)/2; i++) { 
	    	
		    for (int j=0; j < (height)/2; j++) { 

		    	boolean leftOpen = !Z[i*2][j*2+1];
		    	boolean downOpen = !Z[i*2+1][j*2];
		    	
		    	nodes.put(new Vector2(i, j), new MazeNode(i, j, new boolean[]{leftOpen, downOpen}, squareSize));
		    	
		    }
	    }
	    
		return nodes;
	}
	
	
	public ArrayList<Tile> getTileSquare(MazeNode node, Vector2 startLoc) {
		
		//note - using 'solid' and 'nonsolid'(empty string) tile method now
		 //"solid" - solid denotes that the tile can be used later for solid object. for example, water or trees
		 //"" - empty string denotest that tile can be used for nonsolid objects. for example, grass
		 //"either" - can be used for solid or nonsolid. this will return 'solid' or '' tile at random
		
		//README - when creating these, DONT block the middle two tiles of walls that should be open.
		 //ex: bottom open
//		1 1 X X 0 0
//		1 1 0 0 0 0
//		1 1 0 0 0 0
//		1 1 0 0 0 0
//		1 1 0 0 0 0
//		1 1 X X 0 0
		//don't block the X's - if two of the below ever break this rule, and are put adjacent, system doesn't work

		//list1.equals(list2)
		//Arrays.equals(a, b)

		int offSetx = node.size*node.x +(int)startLoc.x;
		int offSetY = node.size*node.y +(int)startLoc.y;
		
		ArrayList<ArrayList<Tile>> squares = new ArrayList<ArrayList<Tile>>();
		ArrayList<Tile> square = new ArrayList<Tile>();

		String[][] format = new String[][]{};
		
		String[] rn = {"ground1", "qmark", "grass1"}; //random non-solid objects
		 //bug - qmark can potentially be solid
		
		//choose from temp because I want more uniformity
		String[] temp = {"bush1", "tree_small1"};
		String[] rs = {temp[this.rand.nextInt(temp.length)], "ground1", "ground1"}; //random solid or non-solid objects
		
		 
		//bl corner
		if (Arrays.equals(node.isOpen, new boolean[]{false, false})) {
			
			//1
			 
			format = new String[][]{
					 			  new String[]{"solid", 	"solid",		"", 			"", 			"either", 		"either"},
					 			  
					 			  new String[]{"solid", 	"solid",		"solid", 		"", 			"", 			"either"},
					 			  
					 			  new String[]{"solid", 	"solid",		"solid", 		"solid",		"", 			""},
					 			  
					 			  new String[]{"solid", 	"solid",		"solid", 		"solid", 		"solid", 		""},
					 			 
					 			  new String[]{"solid", 	"solid",		"solid", 		"solid",		"solid", 		"solid",},
					 			  
					 			  new String[]{"solid", 	"solid",		"solid", 		"solid",		"solid", 		"solid",},
								 };
			square = GenTiles(format, offSetx, offSetY);
			 
			squares.add(square);

			//2
			format = new String[][]{
		 			  new String[]{"solid", 	"solid",		"", 			"", 			"solid", 		"solid"},
		 			  
		 			  new String[]{"solid", 	"solid",		"", 			"", 			"solid", 		"solid"},
		 			  
		 			  new String[]{"solid", 	"solid",		"either", 		"",				"", 			""},
		 			  
		 			  new String[]{"solid", 	"solid",		"either", 		"either", 		"", 			""},
		 			 
		 			  new String[]{"solid", 	"solid",		"solid", 		"solid",		"solid", 		"solid",},
		 			  
		 			  new String[]{"solid", 	"solid",		"solid", 		"solid",		"solid", 		"solid",},
			 };
			
			square = GenTiles(format, offSetx, offSetY);
			
			squares.add(square);
			 
			
			 
		}
		//maze node left or down is open (but not both)
		else if ( node.leftOpen ^ node.downOpen ) {

			//1
			format = new String[][]{
		 			  new String[]{"either", 				"either", 						"either",				"either", 						"either", 				"either"},
		 			  
		 			  new String[]{"", 						"",								"", 					"", 							"", 					""},
		 			  
		 			  new String[]{"", 						"",								"", 					"", 							"", 					""},
		 			  
		 			  new String[]{"", 						"either",						"", 					"either", 						"", 					""},
		 			  
		 			  new String[]{"solid", 				"solid",						"solid",				"solid",						"solid", 				"solid"},
		 			  
		 			  new String[]{"solid", 				"solid",						"solid", 				"solid",						"solid", 				"solid"},
					 };
			if (node.downOpen == true) {
				FlipFormat(format);
				//debug - FlipFormat mirrors about x = y line
//				for (String[] sub : format) {
//					for (String tileName : sub) {
//						System.out.print(""+String.valueOf(tileName)+"\t\t\t\t");
//					}
//					System.out.print("\n");
//				}
				
			}
			square = GenTiles(format, offSetx, offSetY);
			squares.add(square);

			//2
			format = new String[][]{
		 			  new String[]{"", 						"",								"", 					"", 							"solid", 				"solid"},
		 			  
		 			  new String[]{"", 						"",								"", 					"solid", 						"solid", 				"solid"},
		 			  
		 			  new String[]{"", 						"",								"", 					"", 							"", 					""},
		 			  
		 			  new String[]{"", 						"",								"", 					"", 							"", 					""},
		 			  
		 			  new String[]{"solid", 				"solid",						"solid", 				"solid",						"solid", 				"solid",},
		 			  
		 			  new String[]{"solid", 				"solid",						"solid", 				"solid",						"solid", 				"solid",},
					 };
			if (node.downOpen == true) {
				FlipFormat(format);
			}
			square = GenTiles(format, offSetx, offSetY);
			squares.add(square);
			
		}
		//maze node bottom and left is open
		else {
			//1
//			square = new ArrayList<Tile>(); //no tiles
//			squares.add(square);
			
			//2
			format = new String[][]{
		 			  new String[]{"", 						"",								"", 						"", 							"tree_large1_noSprite", 			"tree_large1_noSprite"},
		 			  
		 			  new String[]{"", 						"",								"", 						rs[this.rand.nextInt(rs.length)], "tree_large1", 					"tree_large1_noSprite"},
		 			  
		 			  new String[]{"", 						"",								"", 						"", 							rs[this.rand.nextInt(rs.length)], 	""},
		 			  
		 			  new String[]{"", 						rs[this.rand.nextInt(rs.length)],"", 						"", 							"", 								""},
		 			  
		 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",			rs[this.rand.nextInt(rs.length)],"",						"", 								"",},
		 			  
		 			  new String[]{"tree_large1", 			"tree_large1_noSprite",			"", 						"",								"", 								"",},
					 };
			square = GenTiles(format, offSetx, offSetY);
			squares.add(square);
			
			//3 - all qmark.
			format = new String[][]{
		 			  new String[]{"qmark", 	"qmark",			"qmark", 						"qmark", 							"qmark", 			"qmark"},
		 			  
		 			  new String[]{"qmark", 	"qmark",			"qmark", 						"qmark", 							"qmark", 			"qmark"},
		 			  
		 			  new String[]{"qmark", 	"qmark",			"qmark", 						"qmark", 							"qmark", 			"qmark"},
		 			  
		 			  new String[]{"qmark", 	"qmark",			"qmark", 						"qmark", 							"qmark", 			"qmark"},
		 			  
		 			  new String[]{"qmark", 	"qmark",			"qmark", 						"qmark",							"qmark", 			"qmark",},
		 			  
		 			  new String[]{"qmark", 	"qmark",			"qmark", 						"qmark",							"qmark", 			"qmark",},
					 };
			square = GenTiles(format, offSetx, offSetY);
			squares.add(square);
		}

		//choose randomly from ArrayList of squares
		int randomNum = this.rand.nextInt(squares.size());
		//randomNum = 0; //debug
		return squares.get(randomNum);
		
	}
	
	public void FlipFormat(String[][] format){

		
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
	
	//given a formatted input, ouput the equivalent list of tiles with appropriate x and y positions set
	public ArrayList<Tile> GenTiles(String[][] format, int offSetX, int offSetY) {
		
		ArrayList<Tile> tiles = new ArrayList<Tile>();
		
		//need to start in top-left corner, b/c that's how inline formatting does it (can't be helped)

//		System.out.println("offSetX: "+String.valueOf(offSetX));
//		System.out.println("offSetY: "+String.valueOf(offSetY));
		
		int i=5;
		for (String[] sub : format) {
			
			int j=0;
			for (String tileName : sub) {
				
				if (tileName != "") {
					System.out.println("tileName: "+String.valueOf(tileName));
					tiles.add(new Tile( tileName, new Vector2(0+j*16+offSetX, 0+i*16+offSetY) ));
				}
				j++;
			}
			i--;
		}
		
		return tiles;
	}

}



/* unused code

//unused getTile code - now these groups are marked 'solid' and 'nonsolid'(empty string)
 //then, biome is applied


		//bl corner
		if (Arrays.equals(node.isOpen, new boolean[]{false, false})) {
			
			//1
			 
			format = new String[][]{
					 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",			"", 						"", 							rn[this.rand.nextInt(rn.length)], 	rn[this.rand.nextInt(rn.length)]},
					 			  
					 			  new String[]{"tree_large1", 			"tree_large1_noSprite",			"bush1", 					"", 							rn[this.rand.nextInt(rn.length)], 	rn[this.rand.nextInt(rn.length)]},
					 			  
					 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",			"tree_large1_noSprite", 	"tree_large1_noSprite",			"", 								""},
					 			  
					 			  new String[]{"tree_large1", 			"tree_large1_noSprite",			"tree_large1", 				"tree_large1_noSprite", 		"bush1", 							""},
					 			  
					 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",			"tree_large1_noSprite", 	"tree_large1_noSprite",			"tree_large1_noSprite", 			"tree_large1_noSprite",},
					 			  
					 			  new String[]{"tree_large1", 			"tree_large1_noSprite",			"tree_large1", 				"tree_large1_noSprite",			"tree_large1", 						"tree_large1_noSprite",},
								 };
			square = GenTiles(format, offSetx, offSetY);
			 
			squares.add(square);

			//2
			format = new String[][]{
		 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",			"", 					"", 							"tree_large1_noSprite", "tree_large1_noSprite"},
		 			  
		 			  new String[]{"tree_large1", 			"tree_large1_noSprite",			"", 					"", 							"tree_large1", 			"tree_large1_noSprite"},
		 			  
		 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",			rs[this.rand.nextInt(rs.length)], "", 					"", 					""},
		 			  
		 			  new String[]{"tree_large1", 			"tree_large1_noSprite",			"bush1", 				rs[this.rand.nextInt(rs.length)], "", 					""},
		 			  
		 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",			"tree_large1_noSprite", "tree_large1_noSprite",			"tree_large1_noSprite", "tree_large1_noSprite",},
		 			  
		 			  new String[]{"tree_large1", 			"tree_large1_noSprite",			"tree_large1", 			"tree_large1_noSprite",			"tree_large1", 			"tree_large1_noSprite",},
					 };
			square = GenTiles(format, offSetx, offSetY);
			
			squares.add(square);
			 
			
			 
		}
		//maze node left or down is open (but not both)
		else if ( node.leftOpen ^ node.downOpen ) {

			//1
			format = new String[][]{
		 			  new String[]{rs[this.rand.nextInt(rs.length)], rs[this.rand.nextInt(rs.length)], rs[this.rand.nextInt(rs.length)], rs[this.rand.nextInt(rs.length)], rs[this.rand.nextInt(rs.length)], rs[this.rand.nextInt(rs.length)]},
		 			  
		 			  new String[]{"", 						"",								"", 					"", 							"", 					""},
		 			  
		 			  new String[]{"", 						"",								"", 					"", 							"", 					""},
		 			  
		 			  new String[]{"", 						"",								"", 					"", 							"", 					""},
		 			  
		 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",			"tree_large1_noSprite", "tree_large1_noSprite",			"tree_large1_noSprite", "tree_large1_noSprite",},
		 			  
		 			  new String[]{"tree_large1", 			"tree_large1_noSprite",			"tree_large1", 			"tree_large1_noSprite",			"tree_large1", 			"tree_large1_noSprite",},
					 };
			if (node.downOpen == true) {
				FlipFormat(format);
				//debug - FlipFormat mirrors about x = y line
//				for (String[] sub : format) {
//					for (String tileName : sub) {
//						System.out.print(""+String.valueOf(tileName)+"\t\t\t\t");
//					}
//					System.out.print("\n");
//				}
				
			}
			square = GenTiles(format, offSetx, offSetY);
			squares.add(square);

			//2
			format = new String[][]{
		 			  new String[]{"", 						"",								"", 					"", 							"tree_large1_noSprite", "tree_large1_noSprite"},
		 			  
		 			  new String[]{"", 						"",								"", 					"bush1", 						"tree_large1", 			"tree_large1_noSprite"},
		 			  
		 			  new String[]{"", 						"",								"", 					"", 							"", 					""},
		 			  
		 			  new String[]{"", 						"",								"", 					"", 							"", 					""},
		 			  
		 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",			"tree_large1_noSprite", "tree_large1_noSprite",			"tree_large1_noSprite", "tree_large1_noSprite",},
		 			  
		 			  new String[]{"tree_large1", 			"tree_large1_noSprite",			"tree_large1", 			"tree_large1_noSprite",			"tree_large1", 			"tree_large1_noSprite",},
					 };
			if (node.downOpen == true) {
				FlipFormat(format);
			}
			square = GenTiles(format, offSetx, offSetY);
			squares.add(square);
			
		}
		//maze node bottom and left is open
		else {
			//1
//			square = new ArrayList<Tile>(); //no tiles
//			squares.add(square);
			
			//2
			format = new String[][]{
		 			  new String[]{"", 						"",								"", 						"", 							"tree_large1_noSprite", 			"tree_large1_noSprite"},
		 			  
		 			  new String[]{"", 						"",								"", 						rs[this.rand.nextInt(rs.length)], "tree_large1", 					"tree_large1_noSprite"},
		 			  
		 			  new String[]{"", 						"",								"", 						"", 							rs[this.rand.nextInt(rs.length)], 	""},
		 			  
		 			  new String[]{"", 						rs[this.rand.nextInt(rs.length)],"", 						"", 							"", 								""},
		 			  
		 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",			rs[this.rand.nextInt(rs.length)],"",						"", 								"",},
		 			  
		 			  new String[]{"tree_large1", 			"tree_large1_noSprite",			"", 						"",								"", 								"",},
					 };
			square = GenTiles(format, offSetx, offSetY);
			squares.add(square);
			
			//3 - all qmark.
			format = new String[][]{
		 			  new String[]{"qmark", 	"qmark",			"qmark", 						"qmark", 							"qmark", 			"qmark"},
		 			  
		 			  new String[]{"qmark", 	"qmark",			"qmark", 						"qmark", 							"qmark", 			"qmark"},
		 			  
		 			  new String[]{"qmark", 	"qmark",			"qmark", 						"qmark", 							"qmark", 			"qmark"},
		 			  
		 			  new String[]{"qmark", 	"qmark",			"qmark", 						"qmark", 							"qmark", 			"qmark"},
		 			  
		 			  new String[]{"qmark", 	"qmark",			"qmark", 						"qmark",							"qmark", 			"qmark",},
		 			  
		 			  new String[]{"qmark", 	"qmark",			"qmark", 						"qmark",							"qmark", 			"qmark",},
					 };
			square = GenTiles(format, offSetx, offSetY);
			squares.add(square);
		}




//unused maze node down code (mirrored now)
//maze node bottom is open
else if (Arrays.equals(node.isOpen, new boolean[]{false, true})) {

	//TODO - flip the 'left open' tiles for this.
	
	//1
	format = new String[][]{
 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",			"", 						"", 							rt[this.rand.nextInt(rt.length)], 	rt[this.rand.nextInt(rt.length)]},
 			  
 			  new String[]{"tree_large1", 			"tree_large1_noSprite",			"", 						"", 							rt[this.rand.nextInt(rt.length)], 	rt[this.rand.nextInt(rt.length)]},
 			  
 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",			"", 						"", 							"", 								""},
 			  
 			  new String[]{"tree_large1", 			"tree_large1_noSprite",			"", 						"", 							"", 								""},
 			  
 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",			"", 						"",								"", 								"",},
 			  
 			  new String[]{"tree_large1", 			"tree_large1_noSprite",			"", 						"",								"", 								"",},
			 };
	square = GenTiles(format, offSetx, offSetY);
	squares.add(square);

	//2
	format = new String[][]{
 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",			"", 						"", 							"tree_large1_noSprite", 			"tree_large1_noSprite"},
 			  
 			  new String[]{"tree_large1", 			"tree_large1_noSprite",			"", 						"", 							"tree_large1", 						"tree_large1_noSprite"},
 			  
 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",			"", 						"", 							"", 								""},
 			  
 			  new String[]{"tree_large1", 			"tree_large1_noSprite",			"", 						"", 							"", 								""},
 			  
 			  new String[]{"tree_large1_noSprite", 	"tree_large1_noSprite",			"", 						"",								"", 								"",},
 			  
 			  new String[]{"tree_large1", 			"tree_large1_noSprite",			"", 						"",								"", 								"",},
			 };
	square = GenTiles(format, offSetx, offSetY);
	squares.add(square);
	
}*/
