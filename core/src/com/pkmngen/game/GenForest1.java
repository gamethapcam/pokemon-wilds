package com.pkmngen.game;

import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.math.Vector2;



//generate small forest area
//start by creating square of arbitrary size, surrounded by large trees


class GenForest1 extends Action {
	
	public int layer = 120;
	public int getLayer(){return this.layer;}
	
	
	ArrayList<Vector2> allowedPositions;
	
	ArrayList<Tile> tilesToAdd;

	ArrayList<Action> doActions;
	
	Vector2 topLeft, bottomRight;

	Random rand;
	
	@Override
	public void step(Game game) {


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
		if (!this.tilesToAdd.isEmpty()) {
			currTile = this.tilesToAdd.get(0);
			game.map.tiles.put(currTile.position.cpy(), currTile);
			this.tilesToAdd.remove(0);
		}
		
		//System.out.println("pos: "+String.valueOf(currTile.position));
		
	}
	
	public GenForest1(Game game, Vector2 startLoc, Vector2 endLoc) {

		//clear the current map
		game.map.tiles.clear();
		
		this.rand = new Random();
		
		//note assuming startLoc and endLoc dist are divisible by 32 for now
		 //bc surrounding with large trees.
		
		//this.tilesNotUsed = new HashMap<Vector2, Tile>(game.map.tiles);
		
		this.topLeft = new Vector2();
		this.bottomRight = new Vector2();

		int maxTotalOffset = 256; //max offset entrance/exit from edge
		//subtract from this
		
		//set top-left and bottom-right
		
		//randomly choose left-right or up-down twice
		 //
		int horizontalToEntrance = this.rand.nextInt(2); //0 or 1

		int randomNum = this.rand.nextInt(maxTotalOffset/32) * 32; //guarantees blocks of 32
		if (horizontalToEntrance == 1) {
			//get offset
			this.topLeft.x = startLoc.x - randomNum;
			this.bottomRight.y = startLoc.y;
		}
		else {
			this.topLeft.x = startLoc.x;
			this.bottomRight.y = startLoc.y - randomNum;
		}
		maxTotalOffset -= randomNum; //arbitrary

		int horizontalToExit = this.rand.nextInt(2); //0 or 1
		randomNum = this.rand.nextInt(maxTotalOffset/32) * 32; //guarantees blocks of 32
		if (horizontalToExit == 1) {
			//get offset
			this.topLeft.y = endLoc.y;
			this.bottomRight.x = endLoc.x + randomNum;
		}
		else {
			this.topLeft.y = endLoc.y + randomNum;
			this.bottomRight.x = endLoc.x;
		}
		
		//height >= startLoc-endLoc
		
		this.tilesToAdd = new ArrayList<Tile>();
		//left top to bottom
		for (int i = (int)this.topLeft.y; i > this.bottomRight.y; i -= 32) {
			if (i == startLoc.y && startLoc.x == topLeft.x) {
				//continue;
			}
			this.tilesToAdd.add(new Tile("tree_large1", new Vector2(this.topLeft.x, i))); 
			this.tilesToAdd.add(new Tile("tree_large1_noSprite", new Vector2(this.topLeft.x, i+16))); 
			this.tilesToAdd.add(new Tile("tree_large1_noSprite", new Vector2(this.topLeft.x+16, i)));
			this.tilesToAdd.add(new Tile("tree_large1_noSprite", new Vector2(this.topLeft.x+16, i+16)));
		}
		//bottom left to right
		for (int i = (int)this.topLeft.x; i < this.bottomRight.x; i += 32) {
			if (i == startLoc.x && startLoc.y == bottomRight.y) {
				//continue;
			}
			this.tilesToAdd.add(new Tile("tree_large1", new Vector2(i, bottomRight.y))); 
			this.tilesToAdd.add(new Tile("tree_large1_noSprite", new Vector2(i+16, bottomRight.y))); 
			this.tilesToAdd.add(new Tile("tree_large1_noSprite", new Vector2(i, bottomRight.y+16)));
			this.tilesToAdd.add(new Tile("tree_large1_noSprite", new Vector2(i+16, bottomRight.y+16)));
		}
		//right bottom to top
		for (int i = (int)this.bottomRight.y; i < this.topLeft.y; i += 32) {
			if (i == endLoc.y && endLoc.x == bottomRight.x) {
				//continue;
			}
			this.tilesToAdd.add(new Tile("tree_large1", new Vector2(this.bottomRight.x, i))); 
			this.tilesToAdd.add(new Tile("tree_large1_noSprite", new Vector2(this.bottomRight.x, i+16))); 
			this.tilesToAdd.add(new Tile("tree_large1_noSprite", new Vector2(this.bottomRight.x+16, i)));
			this.tilesToAdd.add(new Tile("tree_large1_noSprite", new Vector2(this.bottomRight.x+16, i+16)));
		}
		//top right to left
		for (int i = (int)this.bottomRight.x; i > this.topLeft.x; i -= 32) {
			if (i == endLoc.x && endLoc.y == topLeft.y) {
				//continue;
			}
			this.tilesToAdd.add(new Tile("tree_large1", new Vector2(i, topLeft.y))); 
			this.tilesToAdd.add(new Tile("tree_large1_noSprite", new Vector2(i+16, topLeft.y))); 
			this.tilesToAdd.add(new Tile("tree_large1_noSprite", new Vector2(i, topLeft.y+16)));
			this.tilesToAdd.add(new Tile("tree_large1_noSprite", new Vector2(i+16, topLeft.y+16)));
		}
		
		this.allowedPositions = new ArrayList<Vector2>();
		
		//add blank space
		for (int i= (int)this.bottomRight.y+32; i < this.topLeft.y; i+=16) {
			for (int j= (int)this.topLeft.x+32; j < this.bottomRight.x; j+=16) {
				//add ground tiles
				this.allowedPositions.add(new Vector2(j, i));
			}
		}
		
		//place start location for player
		 //atm just remove 0,0 tile from allowedPositions
		 //important note - remove will use implementation of .equals() method
		this.allowedPositions.remove(new Vector2(0,0));
		Tile newTile = new Tile("warp1_greyed", new Vector2(0,0));
		PublicFunctions.insertToAS(game, new DrawObjectives(game, newTile));
		this.tilesToAdd.add(newTile);

		this.doActions = new ArrayList<Action>();
		
		//place end warp tile
		 //probably a platform of fixed size
		
		//plant large trees in corners
		this.doActions.add(new PlantTreesLarge(game, this.allowedPositions, this.tilesToAdd, new Vector2(this.topLeft.x+32,this.topLeft.y-32), this));
		this.doActions.add(new PlantTreesLarge(game, this.allowedPositions, this.tilesToAdd, new Vector2(this.topLeft.x+32,this.bottomRight.y+32), this));
		this.doActions.add(new PlantTreesLarge(game, this.allowedPositions, this.tilesToAdd, new Vector2(this.bottomRight.x-32,this.bottomRight.y+32), this));
		this.doActions.add(new PlantTreesLarge(game, this.allowedPositions, this.tilesToAdd, new Vector2(this.bottomRight.x-32,this.topLeft.y-32), this));
		
		
		//plant large tree randomly
		//scale based on map size
		 //need 3 trees for 192 height, 416 width 
		 //3 - 352.0 height, 192 width
		 //2 - width: 192.0 height: 352.0
		int numTrees = (int)((this.topLeft.y-this.bottomRight.y)*(this.bottomRight.x-this.topLeft.x)/(192*192));
		//System.out.println("trees: "+String.valueOf(numTrees)); //debug 
		for (int i=0; i < numTrees; i++ ) {
			this.doActions.add(new PlantTreesLarge(game, this.allowedPositions, this.tilesToAdd, null, this));
		}
		

		//create pond
		this.doActions.add(new AddPond(game, this.allowedPositions, this.tilesToAdd, null, this));
		
		System.out.println("width: "+String.valueOf(this.bottomRight.x-this.topLeft.x)); //debug 
		System.out.println("height: "+String.valueOf(this.topLeft.y-this.bottomRight.y)); //debug 
		
		//create platform
		this.doActions.add(new AddPlatform(game, this.allowedPositions, this.tilesToAdd, null, this));
		
		//create random patches of grass
		this.doActions.add(new PlantGrassLarge(game, this.allowedPositions, this.tilesToAdd, null, this));
		for (int i=0; i < numTrees; i++ ) {
			this.doActions.add(new PlantGrassLarge(game, this.allowedPositions, this.tilesToAdd, null, this));
		}
		
		//TODO - make diagonal path and remove from allowed
		 //doesn't really fix it tho, still chance to get stuck
		 //mebe fill some 'garunteed path' with ground1?
		
		//fill empty space
		this.doActions.add(new FillEmpty(game, this.allowedPositions, this.tilesToAdd, "ground1", this));
		
		
	}

	public class PlantGrassLarge extends Action {

		public int layer = 120;
		public int getLayer(){return this.layer;}

		ArrayList<Vector2> allowedPositions;
		//ArrayList<Vector2> allowedPositions;

		ArrayList<Tile> tilesToAdd;
		
		boolean firstStep;
		
		Vector2 startPosition;
		
		int minNumPatches;
		int maxNumPatches; //max number of trees planted
		
		Action nextAction;
		
		boolean preferCorners;
		
		@Override
		public void step(Game game) {

			//try to put grass in corners
			if (this.preferCorners == true) {
				
			}
			
			//if no start position specified, 
			 //get random position to plant tree
			if (this.startPosition == null) {
				//needs to be divisible by 32?
				int randomIndex;
				Vector2 randomPos;
				while (this.startPosition == null ) {
					randomIndex = game.map.rand.nextInt(this.allowedPositions.size());
					randomPos = this.allowedPositions.get(randomIndex);
					if (!this.allowedPositions.contains(randomPos.cpy().add(16,0))) {
						continue;
					}
					if (!this.allowedPositions.contains(randomPos.cpy().add(0,16))) {
						continue;
					}
					if (!this.allowedPositions.contains(randomPos.cpy().add(16,16))) {
						continue;
					}
					this.startPosition = this.allowedPositions.get(randomIndex).cpy();
				}
			}
			
			
			this.tilesToAdd.add(new Tile("grass1", this.startPosition.cpy()));
			this.tilesToAdd.add(new Tile("grass1", this.startPosition.cpy().add(16,0))); 
			this.tilesToAdd.add(new Tile("grass1", this.startPosition.cpy().add(0,16)));
			this.tilesToAdd.add(new Tile("grass1", this.startPosition.cpy().add(16,16)));
			
			this.allowedPositions.remove(this.startPosition.cpy());
			this.allowedPositions.remove(this.startPosition.cpy().add(16,0));
			this.allowedPositions.remove(this.startPosition.cpy().add(0,16));
			this.allowedPositions.remove(this.startPosition.cpy().add(16,16));

			Vector2 currPos = this.startPosition.cpy();
			
			//get number of grass patches to plant
			
			int randomNum = game.map.rand.nextInt(this.maxNumPatches-this.minNumPatches) + this.minNumPatches; //1 - 4

			System.out.println("new grass: "+String.valueOf(startPosition.x)+", "+String.valueOf(startPosition.y)); //debug 
			System.out.println("rand: "+String.valueOf(randomNum)); //debug 
			
			for (int i=0; i < randomNum; i++) {
				
				ArrayList<Vector2> nextPositions = new ArrayList<Vector2>();
				//
				
				//left
				if (this.allowedPositions.contains(currPos.cpy().add(-16,0))) {
					if (this.allowedPositions.contains(currPos.cpy().add(-16,+16))) {
						nextPositions.add(currPos.cpy().add(-16,0));
					}
				}
				//right
				if (this.allowedPositions.contains(currPos.cpy().add(+32,0))) {
					if (this.allowedPositions.contains(currPos.cpy().add(+32,+16))) {
						nextPositions.add(currPos.cpy().add(+16,0));
					}
				}
				//up
				if (this.allowedPositions.contains(currPos.cpy().add(0,32))) {
					if (this.allowedPositions.contains(currPos.cpy().add(+16,+32))) {
						nextPositions.add(currPos.cpy().add(0,32));
					}
				}
				//down
				if (this.allowedPositions.contains(currPos.cpy().add(0,-16))) {
					if (this.allowedPositions.contains(currPos.cpy().add(+16,-16))) {
						nextPositions.add(currPos.cpy().add(0,-16));
					}
				}
								
				//if you can't go anywhere, stop trying to add grass
				if (nextPositions.isEmpty()) {
					break;
				}
				//choose one randomly from nextPositions
				int randomIndex = game.map.rand.nextInt(nextPositions.size());
				Vector2 newPos = nextPositions.get(randomIndex);
				//put it in tilesToAdd
				this.tilesToAdd.add(new Tile("grass1", newPos.cpy()));
				this.tilesToAdd.add(new Tile("grass1", newPos.cpy().add(16,0))); 
				this.tilesToAdd.add(new Tile("grass1", newPos.cpy().add(0,16)));
				this.tilesToAdd.add(new Tile("grass1", newPos.cpy().add(16,16)));
				
				//remove this from allowedTiles	
				this.allowedPositions.remove(newPos.cpy());
				this.allowedPositions.remove(newPos.cpy().add(16,0));
				this.allowedPositions.remove(newPos.cpy().add(0,16));
				this.allowedPositions.remove(newPos.cpy().add(16,16));
								
				currPos = newPos;
			}
			
			

			PublicFunctions.insertToAS(game, this.nextAction);
			game.actionStack.remove(this);
			return;
			
		}
		public PlantGrassLarge(Game game, ArrayList<Vector2> allowedPositions, ArrayList<Tile> tilesToAdd, Vector2 startPosition, Action nextAction) {
			
			//allowed tiles isn't up to date at this point, so don't use it.
			this.allowedPositions = allowedPositions;
			this.tilesToAdd = tilesToAdd;
			
			this.nextAction = nextAction;
			
			//
			this.firstStep = true;
			
			this.startPosition = startPosition;
			
			this.maxNumPatches = 6;//5;
			this.minNumPatches = 2;//1;
		}
	}
	
	
	public class PlantTreesLarge extends Action {

		public int layer = 120;
		public int getLayer(){return this.layer;}

		ArrayList<Vector2> allowedPositions;
		//ArrayList<Vector2> allowedPositions;

		ArrayList<Tile> tilesToAdd;
		
		boolean firstStep;
		
		Vector2 startPosition;
		
		int minNumTrees;
		int maxNumTrees; //max number of trees planted
		
		Action nextAction;
		
		@Override
		public void step(Game game) {
			
			//if no start position specified, 
			 //get random position to plant tree
			if (this.startPosition == null) {
				//needs to be divisible by 32?
				int randomIndex;
				while (this.startPosition == null) {
					randomIndex = game.map.rand.nextInt(this.allowedPositions.size());
					if ( this.allowedPositions.get(randomIndex).x % 32 == 0 && this.allowedPositions.get(randomIndex).y % 32 == 0 ) {
						this.startPosition = this.allowedPositions.get(randomIndex).cpy();
					}
				}
			}
			
			Vector2 currPos = this.startPosition.cpy();
			
			//get number of trees to plant
			
			int randomNum = game.map.rand.nextInt(this.maxNumTrees-this.minNumTrees) + this.minNumTrees; //1 - 4
			
			this.tilesToAdd.add(new Tile("tree_large1", this.startPosition.cpy()));
			this.tilesToAdd.add(new Tile("tree_large1_noSprite", this.startPosition.cpy().add(16,0))); 
			this.tilesToAdd.add(new Tile("tree_large1_noSprite", this.startPosition.cpy().add(0,16)));
			this.tilesToAdd.add(new Tile("tree_large1_noSprite", this.startPosition.cpy().add(16,16)));
			
			this.allowedPositions.remove(new Vector2(this.startPosition));
			this.allowedPositions.remove(new Vector2(this.startPosition.x+16, this.startPosition.y));
			this.allowedPositions.remove(new Vector2(this.startPosition.x, this.startPosition.y+16));
			this.allowedPositions.remove(new Vector2(this.startPosition.x+16, this.startPosition.y+16));
			/* TODO - remove this sometime
			for (Vector2 pos : new ArrayList<Vector2>(this.allowedPositions)) {
				if (pos.equals(this.startPosition)
					|| pos.equals(this.startPosition.cpy().add(16,0))
					|| pos.equals(this.startPosition.cpy().add(0,16))
					|| pos.equals(this.startPosition.cpy().add(16,16))) {
					for (Vector2 pos2 : this.allowedPositions) {
						if (pos2.equals(pos)) {
							this.allowedPositions.remove(pos);
							break;
						}
					}
				}
			}
			*/
			
			
			for (int i=0; i < randomNum; i++) {
				//

				//System.out.println("new tree"); //debug
				
				
				ArrayList<Vector2> nextPositions = new ArrayList<Vector2>();
				//
				for (Vector2 pos : this.allowedPositions) {
					//left
					Vector2 left = new Vector2(currPos.x-32, currPos.y);
					if (pos.equals(left)) {
						nextPositions.add(left);
					}
					//right
					Vector2 right = new Vector2(currPos.x+32, currPos.y);
					if (pos.equals(right)) {
						nextPositions.add(right);
					}
					//up
					Vector2 up = new Vector2(currPos.x, currPos.y+32);
					if (pos.equals(up)) {
						nextPositions.add(up);
					}
					//down
					Vector2 down = new Vector2(currPos.x, currPos.y-32);
					if (pos.equals(down)) {
						nextPositions.add(down);
					}
				}
				
				//if you can't go anywhere, stop trying to add trees
				if (nextPositions.isEmpty()) {
					break;
				}
				//choose one randomly from nextPositions
				int randomIndex = game.map.rand.nextInt(nextPositions.size());
				Vector2 newPos = nextPositions.get(randomIndex);
				//put it in tilesToAdd
				this.tilesToAdd.add(new Tile("tree_large1", newPos.cpy()));
				this.tilesToAdd.add(new Tile("tree_large1_noSprite", newPos.cpy().add(16,0))); 
				this.tilesToAdd.add(new Tile("tree_large1_noSprite", newPos.cpy().add(0,16)));
				this.tilesToAdd.add(new Tile("tree_large1_noSprite", newPos.cpy().add(16,16)));
				
				//remove this from allowedTiles	
				this.allowedPositions.remove(newPos.cpy());
				this.allowedPositions.remove(newPos.cpy().add(16,0));
				this.allowedPositions.remove(newPos.cpy().add(0,16));
				this.allowedPositions.remove(newPos.cpy().add(16,16));
				
				/* TODO - remove this sometime
				for (Vector2 pos : new ArrayList<Vector2>(this.allowedPositions)) {
					if (pos.equals(newPos)
						|| pos.equals(newPos.cpy().add(16,0))
						|| pos.equals(newPos.cpy().add(0,16))
						|| pos.equals(newPos.cpy().add(16,16))) {
						for (Vector2 pos2 : this.allowedPositions) {
							if (pos2.equals(pos)) {
								this.allowedPositions.remove(pos);
								break;
							}
						}	
					}
				}
				*/
				
				currPos = newPos;
			}
			
			

			PublicFunctions.insertToAS(game, this.nextAction);
			game.actionStack.remove(this);
			return;
		}
		

		public PlantTreesLarge(Game game, ArrayList<Vector2> allowedPositions, ArrayList<Tile> tilesToAdd, Vector2 startPosition, Action nextAction) {
			
			//allowed tiles isn't up to date at this point, so don't use it.
			this.allowedPositions = allowedPositions;
			this.tilesToAdd = tilesToAdd;
			
			this.nextAction = nextAction;
			
			//
			this.firstStep = true;
			
			this.startPosition = startPosition;
			
			this.maxNumTrees = 5;
			this.minNumTrees = 0;
		}
	}
	

	public class AddPlatform extends Action {

		public int layer = 120;
		public int getLayer(){return this.layer;}

		ArrayList<Vector2> allowedPositions;
		//ArrayList<Vector2> allowedPositions;

		ArrayList<Tile> tilesToAdd;

		Vector2 startPosition; //top-left corner
		
		Action nextAction;
		
		int numLevels; 
		//which sprite to use, ie two level mtn or 1 level
		 //can potentially trace inside of raised area later
		
		int minHeight, maxHeight;
		int minWidth, maxWidth;
		
		@Override
		public void step(Game game) {
						
			//
			int randomWidth = (game.map.rand.nextInt(this.maxWidth-this.minWidth+1) + this.minWidth)*16; //3 - 7
			int randomHeight = (game.map.rand.nextInt(this.maxHeight-this.minHeight+1) + this.minHeight)*16; //3 - 7

			//if no start position specified, 
			 //get random top left corner
			if (this.startPosition == null) {
				int randomIndex;
				randomIndex = game.map.rand.nextInt(this.allowedPositions.size());
				this.startPosition = this.allowedPositions.get(randomIndex).cpy();		
			}
			
			//used when iterating
			int bottom = (int)this.startPosition.y-randomHeight; //start with bottom b/c need an empty line here
			int left = (int)this.startPosition.x;
			int top = (int)this.startPosition.y;
			int right = (int)this.startPosition.x+randomWidth;

			
			//go through and finalize square dimensions
			 //need left or right to be out of the trees
			
			//TODO - bug - don't break at currpos
			
			//bottom
			for (int i = left; i <= right; i += 16) {
				//always need to be above empty
				boolean aboveEmpty = false;
				for (Vector2 vec: this.allowedPositions) {
					if (i == vec.x && bottom-16 == vec.y) {
						aboveEmpty = true;
					}
				}
				//if next to tree/thing, start over
				if (aboveEmpty == false) {
					i = left;
					bottom+=16;
					randomHeight-=16; //bugish - if width 2*16 now, can't enter
					if (randomHeight <=2*16) {
						break;
					}
				}
			}
			//left
			for (int i = top; i >= bottom-16; i -= 16) {
				//always need to be above empty
				boolean rightOfEmpty = false;
				for (Vector2 vec: this.allowedPositions) {
					if (left-16 == vec.x && i == vec.y) {
						rightOfEmpty = true;
					}
				}
				//if next to tree/thing, start over
				if (rightOfEmpty == false) {
					i = top;
					left+=16;
					randomWidth-=16; 
					if (randomWidth <=2*16) {
						break;
					}
				}
			}
			//top
			for (int i = left-16; i <= right; i += 16) {
				//always need to be above empty
				boolean belowEmpty = false;
				for (Vector2 vec: this.allowedPositions) {
					if (i == vec.x && top+16 == vec.y) {
						belowEmpty = true;
					}
				}
				//if next to tree/thing, start over
				if (belowEmpty == false) {
					i = left-16;
					top-=16;
					randomHeight-=16; 
					if (randomHeight <=2*16) {
						break;
					}
				}
			}
			//right
			for (int i = top+16; i >= bottom-16; i -= 16) {
				//always need to be above empty
				boolean leftOfEmpty = false;
				for (Vector2 vec: this.allowedPositions) {
					if (right+16 == vec.x && i == vec.y) {
						leftOfEmpty = true;
					}
				}
				//if next to tree/thing, start over
				if (leftOfEmpty == false) {
					i = top+16;
					top-=16;
					randomHeight-=16;
					if (randomHeight <=2*16) {
						break;
					}
				}
			}
			
			System.out.println("left/right: "+String.valueOf(left)+", "+String.valueOf(right));
			System.out.println("top/bottom: "+String.valueOf(top)+", "+String.valueOf(bottom));
			//
			
			//place tiles

			//bottom
			for (int i = left; i < right; i += 16) {

				int maxRand = (right-left)/16 - 2; //length 3, then maxRand == 1
				
				Vector2 currPos = null;
				for (Vector2 vec: this.allowedPositions) {
					if (i == vec.x && bottom== vec.y) {
						currPos = vec;
						break;
					}
				}
				if (currPos == null) {
					continue;
				}
				if (i == left) {
					this.tilesToAdd.add(new Tile("ledge1_corner_bl", currPos.cpy()));  
				}
				else {
					//chance to add entrance
					if (maxRand != 0) {
						int randNum = game.map.rand.nextInt(maxRand);
						if (randNum == 0) {
							//add ramp tile
							this.tilesToAdd.add(new Tile("ledge_grass_ramp", currPos.cpy())); 
							maxRand = 0;
						}
						else {
							this.tilesToAdd.add(new Tile("ledge_grass_down", currPos.cpy())); 
							maxRand--;
						}
					}
					else {
						this.tilesToAdd.add(new Tile("ledge_grass_down", currPos.cpy()));  
					}
				}
				this.allowedPositions.remove(currPos);
			}
			//left
			for (int i = top; i > bottom; i -= 16) {

				Vector2 currPos = null;
				for (Vector2 vec: this.allowedPositions) {
					if (left == vec.x && i == vec.y) {
						currPos = vec;
						break;
					}
				}
				if (currPos == null) {
					continue;
				}
				if (i == top) {
					this.tilesToAdd.add(new Tile("ledge2_corner_tl", currPos.cpy()));  
				}
				else {
					this.tilesToAdd.add(new Tile("ledge_grass_left", currPos.cpy()));  
				}
				this.allowedPositions.remove(currPos);
			}
			//top
			for (int i = right; i > left; i -= 16) {

				Vector2 currPos = null;
				for (Vector2 vec: this.allowedPositions) {
					if (i == vec.x && top == vec.y) {
						currPos = vec;
						break;
					}
				}
				if (currPos == null) {
					continue;
				}
				if (i == right) {
					this.tilesToAdd.add(new Tile("ledge2_corner_tr", currPos.cpy()));  
				}
				else {
					this.tilesToAdd.add(new Tile("ledge_grass_safari_up", currPos.cpy()));  
				}
				this.allowedPositions.remove(currPos);
			}
			//right
			for (int i = bottom; i < top; i += 16) {

				Vector2 currPos = null;
				for (Vector2 vec: this.allowedPositions) {
					if (right == vec.x && i == vec.y) {
						currPos = vec;
						break;
					}
				}
				if (currPos == null) {
					continue;
				}
				if (i == bottom) {
					this.tilesToAdd.add(new Tile("ledge1_corner_br", currPos.cpy()));  
				}
				else {
					this.tilesToAdd.add(new Tile("ledge_grass_right", currPos.cpy()));  
				}
				this.allowedPositions.remove(currPos);
			}
			
			

			PublicFunctions.insertToAS(game, this.nextAction);
			game.actionStack.remove(this);
			return; 
			
		}

		public AddPlatform(Game game, ArrayList<Vector2> allowedPositions, ArrayList<Tile> tilesToAdd, Vector2 startPosition, Action nextAction) {
					
			//TODO - make sure all sides are at least 1 away from trees or in trees (not next to). 
			 //solves getting stuck problem, ledge jump problem, door problem
			
			//allowed tiles isn't up to date at this point, so don't use it.
			this.allowedPositions = allowedPositions;
			this.tilesToAdd = tilesToAdd;
			
			this.nextAction = nextAction;

			this.startPosition = startPosition;
			
			this.minHeight = this.minWidth = 3;
			this.maxHeight = this.maxWidth = 10;
		}
	}
	

	public class AddPond extends Action {

		public int layer = 120;
		public int getLayer(){return this.layer;}

		ArrayList<Vector2> allowedPositions;
		//ArrayList<Vector2> allowedPositions;

		ArrayList<Tile> tilesToAdd;

		Vector2 startPosition; //top-left corner
		
		Action nextAction;
		
		int numLevels; 
		//which sprite to use, ie two level mtn or 1 level
		 //can potentially trace inside of raised area later
		
		int minHeight, maxHeight;
		int minWidth, maxWidth;
		
		@Override
		public void step(Game game) {
						
			//
			int randomWidth = (game.map.rand.nextInt(this.maxWidth-this.minWidth+1) + this.minWidth)*16; //3 - 7
			int randomHeight = (game.map.rand.nextInt(this.maxHeight-this.minHeight+1) + this.minHeight)*16; //3 - 7

			//if no start position specified, 
			 //get random top left corner
			if (this.startPosition == null) {
				int randomIndex;
				randomIndex = game.map.rand.nextInt(this.allowedPositions.size());
				this.startPosition = this.allowedPositions.get(randomIndex).cpy();		
			}
			
			//used when iterating
			int bottom = (int)this.startPosition.y-randomHeight; //start with bottom b/c need an empty line here
			int left = (int)this.startPosition.x;
			int top = (int)this.startPosition.y;
			int right = (int)this.startPosition.x+randomWidth;

			
			//go through and finalize square dimensions
			 //need left or right to be out of the trees
			
			//TODO - bug - don't break at currpos
			/*
			//bottom
			for (int i = left; i <= right; i += 16) {
				//always need to be above empty
				boolean aboveEmpty = false;
				for (Vector2 vec: this.allowedPositions) {
					if (i == vec.x && bottom-16 == vec.y) {
						aboveEmpty = true;
					}
				}
				//if next to tree/thing, start over
				if (aboveEmpty == false) {
					i = left;
					bottom+=16;
					randomHeight-=16; //bugish - if width 2*16 now, can't enter
					if (randomHeight <=2*16) {
						break;
					}
				}
			}
			//left
			for (int i = top; i >= bottom-16; i -= 16) {
				//always need to be above empty
				boolean rightOfEmpty = false;
				for (Vector2 vec: this.allowedPositions) {
					if (left-16 == vec.x && i == vec.y) {
						rightOfEmpty = true;
					}
				}
				//if next to tree/thing, start over
				if (rightOfEmpty == false) {
					i = top;
					left+=16;
					randomWidth-=16; 
					if (randomWidth <=2*16) {
						break;
					}
				}
			}
			//top
			for (int i = left-16; i <= right; i += 16) {
				//always need to be above empty
				boolean belowEmpty = false;
				for (Vector2 vec: this.allowedPositions) {
					if (i == vec.x && top+16 == vec.y) {
						belowEmpty = true;
					}
				}
				//if next to tree/thing, start over
				if (belowEmpty == false) {
					i = left-16;
					top-=16;
					randomHeight-=16; 
					if (randomHeight <=2*16) {
						break;
					}
				}
			}
			//right
			for (int i = top+16; i >= bottom-16; i -= 16) {
				//always need to be above empty
				boolean leftOfEmpty = false;
				for (Vector2 vec: this.allowedPositions) {
					if (right+16 == vec.x && i == vec.y) {
						leftOfEmpty = true;
					}
				}
				//if next to tree/thing, start over
				if (leftOfEmpty == false) {
					i = top+16;
					top-=16;
					randomHeight-=16;
					if (randomHeight <=2*16) {
						break;
					}
				}
			}
			*/
			
			//System.out.println("left/right: "+String.valueOf(left)+", "+String.valueOf(right));
			//System.out.println("top/bottom: "+String.valueOf(top)+", "+String.valueOf(bottom));
			//
			
			//place tiles
			

			for (int i = left; i <= right; i += 16) {

				for (int j = bottom; j <= top; j += 16) {
					Vector2 currPos = new Vector2(i,j);
					
					if (!this.allowedPositions.contains(currPos)) {
						continue;
					}
					//corners
					if (i == left && j == top) {
						this.tilesToAdd.add(new Tile("water1_ledge1_tl", currPos)); 
					}
					else if (i == right && j == top) {
						this.tilesToAdd.add(new Tile("water1_ledge1_tr", currPos)); 
					}
					//top
					else if (j == top) {
						this.tilesToAdd.add(new Tile("water1_ledge1_top", currPos)); 
					}
					//left side
					else if (i == left) {
						this.tilesToAdd.add(new Tile("water1_ledge1_left", currPos));  
					}
					//right side
					else if (i == right) {
						this.tilesToAdd.add(new Tile("water1_ledge1_right", currPos)); 
					}
					else {
						this.tilesToAdd.add(new Tile("water1", currPos));  
					}
					this.allowedPositions.remove(currPos);
				}
			}
			/* TODO - remove this
			//bottom
			for (int i = left; i < right; i += 16) {

				//int maxRand = (right-left)/16 - 2; //length 3, then maxRand == 1
				
				Vector2 currPos = null;
				for (Vector2 vec: this.allowedPositions) {
					if (i == vec.x && bottom== vec.y) {
						currPos = vec;
						break;
					}
				}
				if (currPos == null) {
					continue;
				}
				//no corner for bottom
				this.tilesToAdd.add(new Tile("water1", currPos.cpy()));  
					
				this.allowedPositions.remove(currPos);
			}
			//left
			for (int i = top; i > bottom; i -= 16) {

				Vector2 currPos = null;
				for (Vector2 vec: this.allowedPositions) {
					if (left == vec.x && i == vec.y) {
						currPos = vec;
						break;
					}
				}
				if (currPos == null) {
					continue;
				}
				if (i == top) {
					this.tilesToAdd.add(new Tile("water1_ledge1_tl", currPos.cpy()));  
				}
				else {
					this.tilesToAdd.add(new Tile("water1_ledge1_left", currPos.cpy()));  
				}
				this.allowedPositions.remove(currPos);
			}
			//top
			for (int i = right; i > left; i -= 16) {

				Vector2 currPos = null;
				for (Vector2 vec: this.allowedPositions) {
					if (i == vec.x && top == vec.y) {
						currPos = vec;
						break;
					}
				}
				if (currPos == null) {
					continue;
				}
				if (i == right) {
					this.tilesToAdd.add(new Tile("water1_ledge1_tr", currPos.cpy()));  
				}
				else {
					this.tilesToAdd.add(new Tile("water1_ledge1_top", currPos.cpy()));  
				}
				this.allowedPositions.remove(currPos);
			}
			//right
			for (int i = bottom; i < top; i += 16) {

				Vector2 currPos = null;
				for (Vector2 vec: this.allowedPositions) {
					if (right == vec.x && i == vec.y) {
						currPos = vec;
						break;
					}
				}
				if (currPos == null) {
					continue;
				}
				
				this.tilesToAdd.add(new Tile("water1_ledge1_top", currPos.cpy()));  
				this.allowedPositions.remove(currPos);
			}
			*/
			

			PublicFunctions.insertToAS(game, this.nextAction);
			game.actionStack.remove(this);
			return; 
			
		}

		public AddPond(Game game, ArrayList<Vector2> allowedPositions, ArrayList<Tile> tilesToAdd, Vector2 startPosition, Action nextAction) {
					
			//TODO - take away 'one side away from tree' logic
			
			//allowed tiles isn't up to date at this point, so don't use it.
			this.allowedPositions = allowedPositions;
			this.tilesToAdd = tilesToAdd;
			
			this.nextAction = nextAction;

			this.startPosition = startPosition;
			
			this.minHeight = this.minWidth = 2;
			this.maxHeight = this.maxWidth = 6;
		}
	}

	public class FillEmpty extends Action {

		public int layer = 120;
		public int getLayer(){return this.layer;}

		ArrayList<Vector2> allowedPositions;
		//ArrayList<Vector2> allowedPositions;

		ArrayList<Tile> tilesToAdd;

		Vector2 startPosition; //top-left corner
		
		Action nextAction;
		
		String tileName;
		
		
		@Override
		public void step(Game game) {

			int randomNum;
			for (Vector2 position : new ArrayList<Vector2> (this.allowedPositions)) {
				//add a grass tile
				randomNum = game.map.rand.nextInt(4); //0 - 3
				if (randomNum == 0) {
					tilesToAdd.add(new Tile("grass_short3", position.cpy())); 
				}
				else {
					tilesToAdd.add(new Tile(tileName, position.cpy())); 
				}
				this.allowedPositions.remove(position);
			}

			PublicFunctions.insertToAS(game, this.nextAction);
			game.actionStack.remove(this);
			return; 
			
		}

		public FillEmpty(Game game, ArrayList<Vector2> allowedPositions, ArrayList<Tile> tilesToAdd, String tileName, Action nextAction) {

			this.allowedPositions = allowedPositions;
			this.tilesToAdd = tilesToAdd;
			
			this.tileName = tileName;
			
			this.nextAction = nextAction;

		}	
	}
}
