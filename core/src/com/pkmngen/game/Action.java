package com.pkmngen.game;

import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;

//to change layer, change var, remove from AS, re-insert.

//the 'Action' object referred to in the action_stack
 //completely empty
public class Action {

    // order to draw this action
    // 0 is top (last element in actionStack), and it's step() function is executed last
    public int getLayer(){return 0;};
    //draw using which camera?
    public String getCamera() {return "map";};
    
    // Keep reference to Game.staticGame to prevent having to pass `Game game` to
    // init functions and step function.
//    Game game = Game.staticGame;
    
    
    //what to do at each iteration
    boolean firstStep = true;
    public void firstStep(Game game){}
    
    //what to do at each iteration
    public void step(Game game){}
    
    Action nextAction = null;
    
    // search for nextAction == null, then set to action
    // NOTE: if you declare a duplicate nextAction in a child class, it messes this up.
    //  so don't I guess.
    public void appendAction(Action action) {
        Action currAction = this;
        while (currAction.nextAction != null) {
            currAction = currAction.nextAction;
        }
        currAction.nextAction = action;
    }
}


// TODO: migrate away from this
//this has the extra 'disabled' variable
class MenuAction extends Action {

    boolean disabled;
    boolean drawArrowWhite; // for DrawPlayerMenu
    
    MenuAction prevMenu;
    int currIndex;
}



//always called at end of action chain
class DoneAction extends Action {
    @Override
    public void step(Game game) {
        game.actionStack.remove(this);
    }
}


//pass in two actions. Will add both to action stack.
class SplitAction extends Action {
    
    private Action nextAction2;
    
    // TODO: shouldn't this have layer == LARGE_NUMBER?
    public int getLayer(){return 500;};

    String camera = "map";
    public String getCamera() {return camera;};
    
    public SplitAction(Action nextAction1, Action nextAction2) {
        //put two new actions in the actionStack
        this.nextAction2 = nextAction1;
        this.nextAction = nextAction2;
    }
    
    @Override
    public void step(Game game) {
        game.actionStack.remove(this);
        PublicFunctions.insertToAS(game, nextAction2);
        PublicFunctions.insertToAS(game, nextAction);
        
        // TODO: test this - didn't seem to have an effect
//        if (this.camera.equals(this.nextAction1.getCamera())) {
//            this.nextAction1.step(game);
//        }
//        if (this.camera.equals(this.nextAction2.getCamera())) {
//            this.nextAction2.step(game);
//        }
//
//        //flip flop cameras
//        if (this.camera.equals("map")) {
//            this.camera = "gui";
//        }
//        else if (this.camera.equals("gui")) {
//            this.camera = "map";
//        }
    }
}



//layers
//1-100: reserved
//101-110: sky
//111-115: player(upper)
//116-120: grass
//121-130: player(lower)
//131-140: tiles


//ui layers
//1-100: reserved
//101-110: text
//111-120: attacks
//121-130: pkmn/trainers
//131-140: backgrounds


class draw_laser1 extends Action {

    Sprite sprite;
    
    int timer = 0;
    Vector2 velocityXY;
    
    float velocity = 500f;
    
    public int layer = 121;
    public int getLayer(){return this.layer;}
    
    //what to do at each iteration
    public void step(Game game) {
        

        this.sprite.translate(this.velocityXY.x, this.velocityXY.y);
        
        this.sprite.draw(game.batch);
        
        timer += 1;
        if (timer >= 100) {
            game.actionStack.remove(this);
        }
    }
    
    public draw_laser1(Game game, Vector2 origin, float angle) {
        

        Texture laser_text = new Texture(Gdx.files.internal("data/laser1.png"));
        this.sprite = new Sprite(laser_text, 0, 0, 2048, 128);
        this.sprite.setOrigin(600, this.sprite.getOriginY());
        this.sprite.setPosition(origin.x - this.sprite.getOriginX(), origin.y - this.sprite.getOriginY());
        
        //angle of hypotenuse
        this.sprite.setRotation(angle);
        
        //calc x and y velocity based on angle and this.velocity
        this.velocityXY = new Vector2(this.velocity * (float)Math.cos(Math.toRadians(angle)), this.velocity * (float)Math.sin(Math.toRadians(angle)));
        
        
    }
}
        
        

class draw_crosshair extends Action {
    
    public Sprite crosshair;
    
    public Vector3 mousePos;
    

    @Override
    public String getCamera(){return "map";}

    //what to do at each iteration
    public void step(Game game) {
        
        

        this.mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        game.cam.unproject(this.mousePos);
        
        //snap to cursor
        this.crosshair.setPosition(this.mousePos.x - crosshair.getWidth()/2, this.mousePos.y - crosshair.getHeight()/2);
        
        
        //transform //if using floatingBatch
        //this.crosshair.setScale(1/game.cam.zoom);
        
        //draw
        this.crosshair.draw(game.batch);
                
    }
    

    public draw_crosshair(Game game) {

        Texture crosshair_text = new Texture(Gdx.files.internal("data/crosshair1.png"));
        this.crosshair = new Sprite(crosshair_text, 0, 0, 429, 569);
        
        //Gdx.input.setCursorCatched(true);
        //Gdx.input.setCursorPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
        
        //didn't work
        //this.crosshair = new Pixmap(Gdx.files.internal("data/crosshair1.png"));
        //Gdx.input.setCursorImage(this.crosshair, 0, 0);
        
    }
    
}

//ship1 object draw action
class control_ship1 extends Action {


    public int layer = 120;
    public int getLayer(){return this.layer;}
    
    public ArrayList<Sprite> tiles;
    //tiles map
    private HashMap<Sprite, Vector2> tilesVecs = new HashMap<Sprite, Vector2>();
    
    Music bgSound = null;
    
    public Sprite hullOuter;
    
    //box2d
    //public Body body;
    
    int startX = 544;
    int startY = 300;
    
    float velX = 0;
    float velY = 0;
    
    float vel = 0;
    int maxVel = 120;
    float velIncreaseAmt = .7f;
    
    //0 points to the right
    float torque = 0;
    float maxTorque = 2f;
    float torqueIncreaseAmt = .1f;
    
    float angle = 0;
    float currAngle = 0;
    float driftAmt = 1f;
    
    float driftDecel = .3f;
    float driftStationary = 20f;
    
    public boolean newTouch = true;
    
    //what to do at each iteration
    public void step(Game game) {
        
        if (this.bgSound == null) {
            this.bgSound = Gdx.audio.newMusic(Gdx.files.internal("data/drone1.wav")); //use this
            this.bgSound.setLooping(true);
            this.bgSound.play();
        }
        
        boolean keyIsPressed = false;
        
        //torque
            if(Gdx.input.isKeyPressed(Input.Keys.A)) {
                if (torque < maxTorque)
                    torque += torqueIncreaseAmt;
                keyIsPressed = true;
            }
            if(Gdx.input.isKeyPressed(Input.Keys.D)) {
                if (torque > -maxTorque)
                    torque -= torqueIncreaseAmt;
                keyIsPressed = true;
            }
        //velocity
        if (!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            if(Gdx.input.isKeyPressed(Input.Keys.W)) {
                if (vel < maxVel)
                    vel += velIncreaseAmt;
                keyIsPressed = true;
            }
            if(Gdx.input.isKeyPressed(Input.Keys.S)) {
                if (vel > -maxVel)
                    vel -= velIncreaseAmt;
                keyIsPressed = true;
            }
            //slow down if no movement key is pressed
            if (!Gdx.input.isKeyPressed(Input.Keys.W) && !Gdx.input.isKeyPressed(Input.Keys.S))
            {
                if (vel > driftStationary) {
                    vel -= driftDecel;
                }
                else if (vel < -driftStationary) {
                    vel += driftDecel;
                }
                else {
                    //vel = 0;
                }
            }
        }
        else {
            if (vel > driftStationary) {
                vel -= driftDecel;
            }
            else if (vel < -driftStationary) {
                vel += driftDecel;
            }
            else {
                //vel = 0;
            }
        }
        
        if (!Gdx.input.isKeyPressed(Input.Keys.A) && !Gdx.input.isKeyPressed(Input.Keys.D))
        {
            if (torque > 0.1f) {
                torque -= torqueIncreaseAmt;
            }
            else if (torque < -0.1f) {
                torque += torqueIncreaseAmt;
            }
            else {
                torque = 0;
            }
        }
        
        //draw ship hull
        this.hullOuter.rotate(torque);
        this.hullOuter.draw(game.batch);
        
        //angle of 'where ship _should_ be going'
        this.angle = this.hullOuter.getRotation(); 
        //responsible for drift
        if (!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            
            // this is having issues
            if (Math.abs(currAngle - this.angle) > 180) {
                System.out.println("here");
                currAngle = this.angle + (currAngle % 360) - (this.angle % 360);
            }
//            float drift = this.driftAmt;
//            if (currAngle > this.angle + 100) {
//                currAngle = this.angle + 100;
//            }
//            if (currAngle < this.angle - 100) {
//                currAngle = this.angle - 100;
//            }
//            
            //would prefer something that doesn't change vector, but starts 'thrusting' in new direction
              //when change x vel, add to vel until matches? would be disproportioned diagonally
            float drift = this.driftAmt;
            if (currAngle > this.angle + 100 || currAngle < this.angle - 100) {
                drift *= 6;
            }
            
            if (currAngle > this.angle + 0.1) {
                currAngle -= drift;
            }
            else if (currAngle < this.angle - 0.1) {
                currAngle += drift;
            }
            else {
                currAngle = this.angle;
            }
            
        }
        
        //calc velX and velY based on vel and torque
        this.velX = (float)Math.cos(Math.toRadians(this.currAngle)) * vel;
        this.velY = (float)Math.sin(Math.toRadians(this.currAngle)) * vel;

        this.hullOuter.translate(velX, velY);
        //translate cam 
        game.cam.translate(velX, velY);
        
        
        //snap each tile
        //draw each tile
        for (Sprite tile : this.tiles) {
            //add velocity
            //tile.translate(velX, velY);
            //draw the tile
            tile.draw(game.batch);
            
            Vector2 tileVec = new Vector2(this.tilesVecs.get(tile));
            
            tileVec.rotate(this.angle);
            tile.setRotation(this.angle);
            float offsetX = this.hullOuter.getX() + this.hullOuter.getOriginX() + tileVec.x;
            float offsetY = this.hullOuter.getY() + this.hullOuter.getOriginY() + tileVec.y;
            tile.setPosition(offsetX, offsetY);
        }
        
        
        //update this box2d body
        //this.body.setLinearVelocity(velX*100, velY*100);//hitting some sort of max vel
        
        
        
        if (Gdx.input.isTouched()) {
            game.touchLoc.set(Gdx.input.getX(), Gdx.graphics.getHeight()-Gdx.input.getY(), 0);
            
            if ( this.newTouch == true ) {
                
                Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                game.cam.unproject(mousePos);

                Vector2 laserOrigin = new Vector2(this.hullOuter.getX() + this.hullOuter.getOriginX(), this.hullOuter.getY() + this.hullOuter.getOriginY());
                Vector2 laser1Offset = new Vector2(520, -1600);
                laser1Offset.rotate(this.hullOuter.getRotation());
                Vector2 laser2Offset = new Vector2(520, 1600);
                laser2Offset.rotate(this.hullOuter.getRotation());
                
                Vector2 laser1Origin = new Vector2(laserOrigin.x + laser1Offset.x, laserOrigin.y + laser1Offset.y);
                Vector2 laser2Origin = new Vector2(laserOrigin.x + laser2Offset.x, laserOrigin.y + laser2Offset.y);
                
                Vector2 laser1Vec =  new Vector2(mousePos.x - laser1Origin.x, mousePos.y - laser1Origin.y);
                Vector2 laser2Vec =  new Vector2(mousePos.x - laser2Origin.x, mousePos.y - laser2Origin.y);
                                
                PublicFunctions.insertToAS(game, new draw_laser1(game, laser1Origin, laser1Vec.angle()));
                PublicFunctions.insertToAS(game, new draw_laser1(game, laser2Origin, laser2Vec.angle()));
                PublicFunctions.insertToAS(game, new PlaySound("laser5", new DoneAction()));
                
            }
            this.newTouch = false;
        }
        else {
            this.newTouch = true;
        }
    }
    
    public control_ship1(Game game) {
        
        Texture tileset = new Texture(Gdx.files.internal("data/sheet1.png"));
        Sprite tempSprite;
        
        //set up ship tiles
        this.tiles = new ArrayList<Sprite>();
        
        tempSprite = new Sprite(tileset, 0, 0, 128, 128);
        tempSprite.setPosition(startX+0, startY+0);
        tempSprite.setOrigin(0, 0);
        tiles.add(tempSprite);
        tilesVecs.put(tempSprite, new Vector2(0,-128));

        tempSprite = new Sprite(tileset, 128, 0, 128, 128);
        tempSprite.setPosition(startX+128, startY+0);
        tempSprite.setOrigin(0, 0);
        tiles.add(tempSprite);
        tilesVecs.put(tempSprite, new Vector2(0,128));

        tempSprite = new Sprite(tileset, 256, 0, 128, 128);
        tempSprite.setPosition(startX+0, startY+128);
        tempSprite.setOrigin(0, 0);
        tiles.add(tempSprite);
        tilesVecs.put(tempSprite, new Vector2(0,256));
        
        tempSprite = new Sprite(tileset, 384, 0, 128, 128);
        tempSprite.setPosition(startX+128, startY+128);
        tempSprite.setOrigin(0, 0);
        tiles.add(tempSprite);
        tilesVecs.put(tempSprite, new Vector2(0,0));
        
        
        //hull sprite
        Texture hullTexture = new Texture(Gdx.files.internal("data/titan1.png"));
        this.hullOuter = new Sprite(hullTexture, 0, 0, 2454, 3275);
        this.hullOuter.setOrigin(this.hullOuter.getWidth()/3, this.hullOuter.getHeight()/2);
        
        //temp
        game.cam.position.set(this.hullOuter.getX() + this.hullOuter.getOriginX(), this.hullOuter.getY() + this.hullOuter.getOriginY(), 0);
                
        //box2d vv
        //create box2d body
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.KinematicBody;
        bodyDef.position.set(startX, startY);

        // Create our body in the world using our body definition
        //this.body = game.world.createBody(bodyDef);

        // Create a circle shape and set its radius to 6
        CircleShape circle = new CircleShape();
        circle.setRadius(6f);

        // Create a fixture definition to apply our shape to
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 0.5f; 
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.6f; // Make it bounce a little bit

        // Create our fixture and attach it to the body
        //Fixture fixture = this.body.createFixture(fixtureDef);

        // Remember to dispose of any shapes after you're done with them!
        // BodyDef and FixtureDef don't need disposing, but shapes do.
        circle.dispose();
        
    }
}

//tiling background
class drawAction_map1 extends Action {

    public Texture bgmapText;
    public Sprite bgmapSprite;

    public int layer = 140;
    public int getLayer(){return this.layer;}
    
    //what to do at each iteration
    public void step(Game game) {

        
        //get screen lower left (0,0)
        //turn into world coords
        //divide by 600, round down
        //i is this
        Vector3 topLeft = new Vector3(0,0,0);
        game.cam.unproject(topLeft);
        //maxI is screen.width coord / 600, round up
        Vector3 bottomRight = new Vector3(Gdx.graphics.getWidth(),Gdx.graphics.getHeight(),0);
        game.cam.unproject(bottomRight);
        
        int min_i = (int)Math.floor((topLeft.x / (bgmapSprite.getWidth()*bgmapSprite.getScaleX())));
        int min_j = (int)Math.floor((bottomRight.y / (bgmapSprite.getHeight()*bgmapSprite.getScaleY())));
        
        int max_i = (int)Math.ceil((bottomRight.x / (bgmapSprite.getWidth()*bgmapSprite.getScaleX())));
        int max_j = (int)Math.ceil((topLeft.y / (bgmapSprite.getHeight()*bgmapSprite.getScaleY())));

        for (int i=min_i; i <= max_i; i++) {
            for (int j=min_j; j <= max_j; j++) {
                this.bgmapSprite.setPosition(i*bgmapSprite.getWidth()*bgmapSprite.getScaleX(), j*bgmapSprite.getHeight()*bgmapSprite.getScaleY());
                this.bgmapSprite.draw(game.batch);
            }
        }
    }
    
    public drawAction_map1() {
//        this.bgmapText = new Texture(Gdx.files.internal("bg2.png"));
//        this.bgmapSprite = new Sprite(this.bgmapText, 0, 0, 600, 600);

        this.bgmapText = new Texture(Gdx.files.internal("block1.png"));
        this.bgmapSprite = new Sprite(this.bgmapText, 0, 0, 16, 16);
        
        
        //this.bgmapSprite.setScale(10);
    }
}


//demo code - basically the whole switch map mechanic
//draw objectives
class DrawObjectives extends Action {


    Sprite bgSprite;
    Sprite tileSprite1;
    Sprite tileSprite2;
    
    Tile tile;
    
    String mainObj;
    String type;
    int numMain;
    String sideObj;
    int numSide;
    
    int timer;
    
    Tile newTile;
    Tile currTile;
    

    public int layer = 140;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};

    
    @Override
    public void step(Game game) {

        //when the player moves to a new tile, chance of wild encounter
        this.newTile = game.map.tiles.get(new Vector2(game.player.position));
        
        //beginning of game
        if (this.currTile == null) {
            this.currTile = this.newTile;
        }
        
        //draw helper sprite
         //probly remove
        //this.bgSprite.draw(game.floatingBatch);    
        
        int numMainDone = 0;
        int numSideDone = 0;
        //numMainDone=5; //debug
        for (Pokemon pokemon : game.player.pokemon) {
            if (pokemon.types.contains(this.type)) {
                numMainDone+=1;
            }
            numSideDone+=1;
        }
        if (numMainDone >= numMain) {
            numMainDone = numMain;
            if (this.timer < 4) {
                this.tile.sprite = this.tileSprite1;
            }
            else if (this.timer < 8) {
                this.tile.sprite = this.tileSprite2;
            }
        }
        if (numSideDone > numSide) {
            numSideDone = numSide;
        }
        
        this.bgSprite.draw(game.floatingBatch);
        game.font.draw(game.floatingBatch, "Objective: "+numMainDone+mainObj, 18, 136);
        game.font.draw(game.floatingBatch, "Extra: "+numSideDone+sideObj, 30, 130); 
        
        
        if (this.timer < 8) {
            this.timer += 1;
        }
        else {
            this.timer = 0;
        }
        

        //if moved to a new tile
        if (newTile != null && newTile != this.currTile) {
            //
            this.currTile = newTile;
            
            //check if should warp (on tile and objective done
             //could check if tile == tile too, same
            if (this.currTile.position.equals(this.tile.position)
                    && numMainDone >= numMain) {
                //warp
                game.numObjectivesFinished+=1;
                if (game.numObjectivesFinished < 2) {
                    PublicFunctions.insertToAS(game, new SplitAction( 
                            new BattleFadeOut(game,
                                new GenForest1(game, new Vector2(-64,-64), new Vector2(128,128)) //inserts a drawObjective automatically
                            ),
                            new PlaySound("harden1", new DoneAction())
                    ));
                    //remove player pkmn
                    game.player.pokemon.clear();
                }
                else {
                    game.map = new PkmnMap("Demo_Legendary1");
                    PublicFunctions.insertToAS(game, new SplitAction( 
                            new BattleFadeOut(game, new DoneAction() 
                            ),
                            new PlaySound("harden1", new DoneAction())
                    ));
                }
                //TODO - change route
                //
                
                game.actionStack.remove(this);
            }
        }
        
    }
    
    public DrawObjectives(Game game, Tile tile) {

        this.tile = tile;
        
        Texture text = new Texture(Gdx.files.internal("text2.png"));
        this.bgSprite = new Sprite(text, 0, 0, 160, 144);
        
        //how many?
        this.numMain = 2;
        //what type?
        ArrayList<String> types = new ArrayList<String>();
        ArrayList<String> strings = new ArrayList<String>();
        types.add("Steel"); strings.add("Demo_SteelRoute");
        types.add("Dark"); strings.add("Demo_DarkRoute");
        types.add("Psychic"); strings.add("Demo_PsychicRoute");
        int randomNum = game.map.rand.nextInt(types.size());
        this.type = types.get(randomNum);
        
        //set route according to type selected
        game.map.currRoute = new Route(strings.get(randomNum), 20);
        
        this.mainObj = "/"+String.valueOf(this.numMain)+" "+this.type+" Pokémon.";
        
        //side objectives:
        //3 of diff type, 5 total, etc
        this.numSide = 5;
        this.sideObj = "/"+String.valueOf(this.numSide)+" total Pokémon.";

        text = new Texture(Gdx.files.internal("tiles/warp1_greyed.png"));
        this.tileSprite1 = new Sprite(text, 0, 0, 16, 16);
        this.tileSprite1.setPosition(tile.sprite.getX(), tile.sprite.getY());
        text = new Texture(Gdx.files.internal("tiles/warp1.png"));
        this.tileSprite2 = new Sprite(text, 0, 0, 16, 16);
        this.tileSprite2.setPosition(tile.sprite.getX(), tile.sprite.getY());
        
        this.timer = 0;
        
    }
}



class DoneWithDemo extends Action {
    

    public int layer = 140;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};

    Sprite bgSprite;
    
    
    @Override
    public void step(Game game) {
        
        this.bgSprite.draw(game.floatingBatch);
        game.font.draw(game.floatingBatch, "Thanks for playing!",44,80);
        
    }


    public DoneWithDemo(Game game) {

        Texture text = new Texture(Gdx.files.internal("battle/intro_frame6.png"));
        this.bgSprite = new Sprite(text);
        
    }

}



//draw text on screen
 //right now you have to insert spaces to do newline
 //pkmns logic for choosing new lines. (probly just not enough room)
 //i think arrow is first displayed after text is done (rather than blank)
//problem - this will render spaces even though it shouldn't
 //solution is to make it 'correct', ie nested arrays
class DisplayText extends Action {

    ArrayList<Sprite> spritesNotDrawn;
    ArrayList<Sprite> spritesBeingDrawn;

    Sprite arrowSprite;
    
    Action playSoundAction;
    boolean playSound;
    Action scrollUpAction; //keeps track of if text is currently scrolling up
    
    public int layer = 106;
    public int getLayer(){return this.layer;}

    public String getCamera() {return "gui";};
    
    Sprite helperSprite;
    Sprite bgSprite;
    int timer;
    
    int speedTimer, speed;

    Action triggerAction;
    boolean foundTrigger;
    boolean checkTrigger;
    public static boolean textPersist = false;
    boolean persist = false;  // if true, don't clear text after text is finished
    boolean waitInput = true;  // if true, wait for user to press A after text is complete

    boolean firstStep;
    
    //what to do at each iteration
    public void step(Game game) {
        
        if (this.firstStep == true) {
            //if you ever just pass 'new DoneAction()' to triggerAction, and 
             //then remove game.displayTextAction from actionStack later,
             //text will stop displaying
             //needed when enemy pkmn faints (displayText doesn't wait for user input)
            game.displayTextAction = this;
            this.firstStep = false;
            if (this.persist) {
                DisplayText.textPersist = true;
            }
        }
        
        //debug
        //this.helperSprite.draw(game.floatingBatch);
        this.bgSprite.draw(game.floatingBatch);
        
        //debug //flash on and off
//        this.timer--;
//        if (this.timer <= 0){
//            if (this.timer <= -19){
//                this.timer = 20;
//            }
//            return;
//        }
        
        //note - draws 0 letters on first step
        
        //draw all drawable
        for (Sprite sprite : spritesBeingDrawn) {
            sprite.draw(game.floatingBatch);    
        }

        
        //don't do anything if scrolling text up
        if (game.actionStack.contains(this.scrollUpAction)) {
            return;
        }
        
        //TODO - don't do anything if waiting for user to press A
        
        //don't do anything if playing sound (example fanfare, etc)
        if (game.actionStack.contains(this.playSoundAction)) {
            return;
        }
        
        // TODO: deprecate this
        //don't do anything if trigger action is in actionStack
        if (this.checkTrigger == true || (this.persist && !DisplayText.textPersist)) {
            if (this.triggerAction == null && !DisplayText.textPersist) {
                game.actionStack.remove(this);
                return;
            }
            // TODO: remove the below two blocks once migrated to using textPersist
            if (game.actionStack.contains(this.triggerAction)) {
                this.foundTrigger = true;
                return;
            }
            //once the trigger is found once, exit after that trigger is finished
            if (this.foundTrigger == true) {
                game.actionStack.remove(this);
                return;
            }
            return;
        }
        
        //debug
//        if (spritesBeingDrawn.size() == 30) {
//            return;
//        }
        
        //
        
        //if no sprites left in spritesNotDrawn, wait for player to hit A
        if (spritesBeingDrawn.size() >= 36 || spritesNotDrawn.isEmpty()) { //18 characters per line allowed

            //if at the end of text and need to play sound, do that
            if (this.playSound == true && spritesNotDrawn.isEmpty()) {
                PublicFunctions.insertToAS(game, this.playSoundAction);
                this.playSoundAction.step(game); //avoid latency
                this.playSound = false;
                return;
            }
            
            //if we need to wait on a trigger
            if (this.triggerAction != null || (DisplayText.textPersist && !this.waitInput)) {
                PublicFunctions.insertToAS(game, this.nextAction);
                this.checkTrigger = true;
                return;
            }
            
            //draw arrow
             //flash on and off
            if (this.timer <= 0){
                if (this.timer <= -35){
                    this.timer = 33;
                }
                else {
                    this.arrowSprite.draw(game.floatingBatch);
                }
            }
            this.timer--;
            
            if(Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
                
                Action playSound = new PlaySound("click1", new DoneAction());
                PublicFunctions.insertToAS(game, playSound);
                playSound.step(game); //prevent latency
                
                if (spritesNotDrawn.isEmpty()) {
                    if (DisplayText.textPersist) {
                        PublicFunctions.insertToAS(game, this.nextAction);
                        this.checkTrigger = true;
                        return;
                    }
                    PublicFunctions.insertToAS(game, this.nextAction);
                    game.actionStack.remove(this);
                }
                else {
                    this.scrollUpAction = new DisplayText.ScrollTextUp(game, this.spritesBeingDrawn, this.spritesNotDrawn);
                    PublicFunctions.insertToAS(game, this.scrollUpAction);
                }
            }
            return;
        }
        

        //only extract sprite every 'speed' number of frames
        if (this.speedTimer > 0) {
            this.speedTimer--;
            return;
        }
        else {
            this.speedTimer = this.speed;
        }
        
        //get next sprite, remove from spritesNotDrawn
        //text speed - if pressing A or B, add 3 sprites instead of 1
        if (Gdx.input.isKeyPressed(Input.Keys.Z) || Gdx.input.isKeyPressed(Input.Keys.X)) {
            //if would take too many, stop
            for (int i=0; i < 3 && !spritesNotDrawn.isEmpty(); i++) {
                spritesBeingDrawn.add(spritesNotDrawn.get(0));
                spritesNotDrawn.remove(0);
            }
        }
        else {
            spritesBeingDrawn.add(spritesNotDrawn.get(0));
            spritesNotDrawn.remove(0);
        }
        
    }
    
    public DisplayText(Game game,
                       String textString,
                       String playSound,
                       boolean textPersist,
                       Action nextAction) {
        this(game, textString, playSound, textPersist, true, nextAction);
    }
    
    // TODO: migrate to using this only
    public DisplayText(Game game,
                       String textString,
                       String playSound,
                       boolean textPersist,
                       boolean waitInput,
                       Action nextAction) {
        this(game, textString, playSound, null, nextAction);
        this.persist = textPersist;
        this.waitInput = waitInput;
    }
    
    public DisplayText(Game game, String textString, String playSound, Action triggerAction, Action nextAction) {
        
        this.nextAction = nextAction;

        this.firstStep = true;
        
        //TODO - need separate triggerAction and clickComplete modes
         //when both are passed, clicks complete but still waits on triggerAction
        
        //set end trigger action
        this.triggerAction = triggerAction;
        this.foundTrigger = false;
        this.checkTrigger = false;
        
        this.spritesNotDrawn = new ArrayList<Sprite>();
        this.spritesBeingDrawn = new ArrayList<Sprite>();

        if (playSound != null) {
            this.playSoundAction = new DisplayText.PlaySound_Text(playSound, new DoneAction());
            this.playSound = true;
        }
        else {
            this.playSound = false;
        }
        
        this.speed = 2; this.speedTimer = this.speed;
        
        //here we make sure each line wraps by word, not by char
         //could be better, but works
        String line = "";
        String lines = "";
        String[] words = textString.split(" ");
        for (String word : words) {
            
            if (line.length() + word.length() < 18) {
                line += word;
                if (line.length() != 17) { //possible bug //don't add space to end of max length line
                    line += " ";
                }
            }
            else {
                while (line.length() < 18) {
                    line += " ";
                }
                lines += line;
                line = word + " ";
            }
        }
        lines+=line;
        
        char[] textArray = lines.toCharArray(); //iterate elements
        
        int i = 0, j = 0; ///, offsetNext = 0; //offsetNext if char sizes are ever different. atm it works.
        Sprite currSprite;
        for (char letter : textArray) {
            //offsetNext += spriteWidth*3+2 //how to do this?
            Sprite letterSprite = game.textDict.get((char)letter);
            //System.out.println(String.valueOf(letter));
            if (letterSprite == null) {
                letterSprite = game.textDict.get(null);
            }
            currSprite = new Sprite(letterSprite); //copy sprite from char-to-Sprite dictionary
            
            //currSprite.setPosition(10*3+8*i*3 +2, 26*3-16*j*3 +2); //offset x=8, y=25, spacing x=8, y=8(?)
            currSprite.setPosition(10+8*i +2-4, 26-16*j +2-4); //post scaling change
            //currSprite.setScale(3); //post scaling change
            
            spritesNotDrawn.add(currSprite);
            //go down a line if needed
             //TODO - do this for words, not chars. split on space, array
            if (i >= 17) { 
                i = 0; j++;
            }
            else {
                i++;
            }
        }
        
        //why not just every frame draw a new sprite, pop off the sprites list?
         //when list is empty, display the arrow and wait for user input

        Texture text = new Texture(Gdx.files.internal("arrow_down.png"));
        this.arrowSprite = new Sprite(text, 0, 0, 7, 5);
        //this.arrowSprite.setPosition(147*3-2,12*3-1);
        this.arrowSprite.setPosition(147-2-1,12-1-1); //post scaling change
        //this.arrowSprite.setScale(3); //post scaling change
        
        text = new Texture(Gdx.files.internal("text_helper1.png")); //battle_bg1
        //text = new Texture(Gdx.files.internal("battle/battle_bg1.png"));
        //Texture text = new Texture(Gdx.files.internal("throw_rock_anim/helper12.png"));
        this.helperSprite = new Sprite(text, 0, 0, 160, 144);
        //this.helperSprite.setPosition(16*10,16*9);
        //this.helperSprite.setScale(3);
        this.timer = 0;
        

        text = new Texture(Gdx.files.internal("textbox_bg1.png")); //textbox bg1
        this.bgSprite = new Sprite(text, 0, 0, 160, 144);
        
    }
    
    //move text up in text box
     //works b/c DisplayText will begin drawing new chars once SpritesBeingDrawn is small enough
    //parent might need to call step() for frame-correctness, not sure
    class ScrollTextUp extends Action {

        ArrayList<Vector2> positions;
        Vector2 position;
        
        ArrayList<Sprite> text;
        ArrayList<Sprite> otherText;

        public int layer = 110;
        public int getLayer(){return this.layer;}

        public String getCamera() {return "gui";};
        
        
        //what to do at each iteration
        public void step(Game game) {
            
            this.position = this.positions.get(0);
            this.positions.remove(0);
            
            for (Sprite sprite : this.text) {
                sprite.setPosition(sprite.getX()+this.position.x, sprite.getY()+this.position.y);
            }
            for (Sprite sprite : this.otherText) {
                sprite.setPosition(sprite.getX()+this.position.x, sprite.getY()+this.position.y);
            }
            
            //if done, remove first 18 elements
            //frees up DisplayText's text array, which will get filled with new sprites
            if (this.positions.isEmpty()) {
                for (int i=0; i<18; i++) {
                    this.text.remove(0);
                }
                game.actionStack.remove(this);

                return;
            }
        }
        
        public ScrollTextUp(Game game, ArrayList<Sprite> text, ArrayList<Sprite> otherText) {
            
            //TODO - bug, fails if scrolling up twice
            //TODO - get comma to work (what text contains comma?)
            
            this.text = text;
            this.otherText = otherText;
            
            
            this.positions = new ArrayList<Vector2>();
            for (int i=0; i < 5; i++) {
                this.positions.add(new Vector2(0,0));
            }
            this.positions.add(new Vector2(0,8));
            for (int i=0; i < 5; i++) {
                this.positions.add(new Vector2(0,0));
            }
            this.positions.add(new Vector2(0,8));
            
        }
    }

    /*
     * Clear any text persisting on screen.
     */
    static class Clear extends Action {

        @Override
        public void step(Game game) {
            DisplayText.textPersist = false;
            game.actionStack.remove(this);
            PublicFunctions.insertToAS(game, this.nextAction);
        }
        
        public Clear(Game game, Action nextAction) {
            this.nextAction = nextAction;
        }
    }

    //play a sound, ie victory fanfare
     //unique b/c need to mute battle music
    class PlaySound_Text extends Action {
            
        Music music;
        
        
        float initialVolume; //different tracks have diff volume

        public PlaySound_Text(String sound, Action nextAction) {
            
            this.nextAction = nextAction;
            this.playedYet = false;
            
            if (sound == "fanfare1") {
                this.music = Gdx.audio.newMusic(Gdx.files.internal("catch_fanfare.mp3")); //use this
                this.music.setLooping(false);
            }

            else if (sound == "Raikou") {
                this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/243Cry.ogg")); //use this
                this.music.setLooping(false);
            }
            else if (sound == "Entei") {
                this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/244Cry.ogg")); //use this
                this.music.setLooping(false);
            }
            else if (sound == "Suicune") {
                this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/245Cry.ogg"));
                this.music.setLooping(false);
            }
        }
        boolean playedYet; //do music.play on first step
        
        @Override
        public void step(Game game) {
            //play the sound
            if (this.music != null && !this.playedYet) {
                //game.battle.music.setVolume(0f); //TODO - use this?
                this.initialVolume = game.currMusic.getVolume();
                game.currMusic.setVolume(0f);
                this.music.play();
                this.playedYet = true;
            }
            
            if (!this.music.isPlaying()) {
                //game.battle.music.setVolume(.3f);
                game.currMusic.setVolume(this.initialVolume);
                game.actionStack.remove(this);
                PublicFunctions.insertToAS(game, this.nextAction);
            }
        }
    }
}


class RemoveDisplayText extends Action {
    
    
    
    @Override
    public void step(Game game) {
        game.actionStack.remove(game.displayTextAction);
        game.displayTextAction = null;
        game.actionStack.remove(this);
        PublicFunctions.insertToAS(game, this.nextAction);
    }
    
    public RemoveDisplayText(Action nextAction) {
        this.nextAction = nextAction;
    }
}


class WaitFrames extends Action {
    int length;
    public int layer = 110;
    public int getLayer(){return this.layer;}
    public String getCamera() {return "gui";};
    
    
    @Override
    public void step(Game game) {
        this.length--;
        if (this.length <= 0) {
            PublicFunctions.insertToAS(game, this.nextAction);
            game.actionStack.remove(this);
        }
    }
    
    public WaitFrames(Game game, int length, Action nextAction) {
        this.nextAction = nextAction;
        this.length = length;
    }
}

class PublicFunctions {
    //this is for inserting into the AS at proper layer
    public static void insertToAS(Game game, Action actionToInsert) {
        // handle null entry by skipping
        if (actionToInsert == null) {
            return;
        }
        //for item in as
        for (int i = 0; i < game.actionStack.size(); i++) {
            //if actionToInsert layer is <=  action layer, insert before element
            if (actionToInsert.getLayer() >= game.actionStack.get(i).getLayer()) {
                game.actionStack.add(i, actionToInsert);
                return;
            }
        }
        //layer is smallest in stack, add to end
        game.actionStack.add(actionToInsert);
    }
}


// pulled from wizard-duels code
class DisplayTextIntro extends Action {

    ArrayList<Sprite> spritesNotDrawn;
    ArrayList<Sprite> spritesBeingDrawn;

    Sprite arrowSprite; //TODO - remove this
    Sprite arrowSprite2;
    
    Action playSoundAction;
    boolean playSound;
    Action scrollUpAction; //keeps track of if text is currently scrolling up
    
    int charsPerLine = 32; //number of characters allowed in one line
    int spacing = 6; //how far apart to space characters
    
    public int layer = 110;
    public int getLayer(){return this.layer;}
    public String getCamera() {return "gui";};
    
    
    
    Sprite helperSprite;
    Sprite bgSprite;
    int timer;
    
    int speedTimer, speed;
    
    //when we need to stop after trigger action
    Action triggerAction;
    boolean foundTrigger;
    boolean checkTrigger;
    
    Vector3 touchLoc = new Vector3();
    Vector2 touchLoc2d = new Vector2();
    
    boolean firstStep = true;
    boolean exitWhenDone = true;
    boolean waitingOnExit = false;
    
    //what to do at each iteration
    public void step(Game game) {

        
        
        if (this.firstStep == true) {
            //if you ever just pass 'new DoneAction()' to triggerAction, and 
             //then remove game.displayTextAction from actionStack later,
             //text will stop displaying
             //needed when enemy pkmn faints (displayText doesn't wait for user input)
            game.displayTextAction = this;
            this.firstStep = false;
        }
        
        //debug
        //this.helperSprite.draw(game.floatingBatch);
        this.bgSprite.draw(game.floatingBatch);
        
        //debug //flash on and off
//        this.timer--;
//        if (this.timer <= 0){
//            if (this.timer <= -19){
//                this.timer = 20;
//            }
//            return;
//        }
        
        //note - draws 0 letters on first step
        
        //draw all drawable
        for (Sprite sprite : spritesBeingDrawn) {
            sprite.draw(game.floatingBatch);    
        }

        

        //don't do if waiting for something else to exit this
        if (this.waitingOnExit == true) {
            return;
        }
        
        //don't do anything if scrolling text up
        if (game.actionStack.contains(this.scrollUpAction)) {
            return;
        }
        
        //TODO - don't do anything if waiting for user to press A
        
        //don't do anything if playing sound (example fanfare, etc)
        if (game.actionStack.contains(this.playSoundAction)) {
            return;
        }
        
        //don't do anything if trigger action is in actionStack
        if (this.checkTrigger == true) {
            if (game.actionStack.contains(this.triggerAction)) {
                this.foundTrigger = true;
                return;
            }
            //once the trigger is found once, exit after that trigger is finished
            if (this.foundTrigger == true) {
                game.actionStack.remove(this);
                return;
            }
            return;
        }
        
        //debug
//        if (spritesBeingDrawn.size() == 30) {
//            return;
//        }
        
        //
        
        //if no sprites left in spritesNotDrawn, wait for player to hit A
        if (spritesBeingDrawn.size() >= 36 || spritesNotDrawn.isEmpty()) { //24 characters per line allowed
            
            //if at the end of text and need to play sound, do that
            if (this.playSound == true && spritesNotDrawn.isEmpty()) {
                PublicFunctions.insertToAS(game, this.playSoundAction);
                this.playSoundAction.step(game); //avoid latency
                this.playSound = false;
                return;
            }
            
            //if we need to wait on a trigger
            if (this.triggerAction != null) {
                PublicFunctions.insertToAS(game, this.nextAction);
                this.checkTrigger = true;
                return;
            }
            
            //draw arrow
             //flash on and off
//            if (this.timer <= 0){
//                if (this.timer <= -35){
//                    this.timer = 33;
//                }
//                else {
//                    this.arrowSprite2.draw(game.floatingBatch);
//                }
//            }
//            this.timer--;
            
            //Intro - Always go to next line
            //z button still enabled for skipping text
//            if(Gdx.input.isKeyJustPressed(Input.Keys.Z)) {

                if (spritesNotDrawn.isEmpty()) {
                    PublicFunctions.insertToAS(game, this.nextAction);
                    
                    if (this.exitWhenDone == true) {
                        game.actionStack.remove(this);
                    }
                    else {
                        this.waitingOnExit = true;
                    }
                }
                else {
                    this.scrollUpAction = new DisplayTextIntro.ScrollTextUp(game, this.spritesBeingDrawn, this.spritesNotDrawn);
                    PublicFunctions.insertToAS(game, this.scrollUpAction);
                }
            
            return;
        }
        
        //only extract sprite every 'speed' number of frames
        if (this.speedTimer > 0) {
            this.speedTimer--;
            return;
        }
        else {
            this.speedTimer = this.speed;
        }
        
        //if would take too many, stop
        for (int i=0; i < 1 && !spritesNotDrawn.isEmpty() && spritesBeingDrawn.size() < 36; i++) {
            spritesBeingDrawn.add(spritesNotDrawn.get(0));
            spritesNotDrawn.remove(0);
        }
        
    }
    
    public DisplayTextIntro(Game game, String textString, String playSound, Action triggerAction, boolean exitWhenDone, Action nextAction) {
        
        this.nextAction = nextAction;

        this.exitWhenDone = exitWhenDone;
        
        //TODO - need separate triggerAction and clickComplete modes
         //when both are passed, clicks complete but still waits on triggerAction
        
        //set end trigger action
        this.triggerAction = triggerAction;
        this.foundTrigger = false;
        this.checkTrigger = false;
        
        this.spritesNotDrawn = new ArrayList<Sprite>();
        this.spritesBeingDrawn = new ArrayList<Sprite>();

        if (playSound != null) {
            this.playSoundAction = new DisplayTextIntro.PlaySound_Text(playSound, new DoneAction());
            this.playSound = true;
        }
        else {
            this.playSound = false;
        }
        
        this.speed = 3; //= 2; 
        this.speedTimer = this.speed;
        
        //26 chars per line
        
        //here we make sure each line wraps by word, not by char
         //could be better, but works
        String line = "";
        String lines = "";
        String[] words = textString.split(" ");
        for (String word : words) {
            
            if (line.length() + word.length() < 18) {
                line += word;
                if (line.length() != 17) { //possible bug //don't add space to end of max length line
                    line += " ";
                }
            }
            else {
                while (line.length() < 18) {
                    line += " ";
                }
                lines += line;
                line = word + " ";
            }
        }
        lines+=line;
        
        
        char[] textArray = lines.toCharArray(); //iterate elements
        
        int i = 0, j = 0; ///, offsetNext = 0; //offsetNext if char sizes are ever different. atm it works.
        Sprite currSprite;
        for (char letter : textArray) {
            //offsetNext += spriteWidth*3+2 //how to do this?
            Sprite letterSprite = game.textDict.get((char)letter);
            //System.out.println(String.valueOf(letter));
            if (letterSprite == null) {
                letterSprite = game.textDict.get(null);
            }
            currSprite = new Sprite(letterSprite); //copy sprite from char-to-Sprite dictionary
            
            //currSprite.setPosition(10*3+8*i*3 +2, 26*3-16*j*3 +2); //offset x=8, y=25, spacing x=8, y=8(?)
            currSprite.setPosition(10+8*i +2-4, 26-16*j +2-4); //post scaling change
//            currSprite.setPosition(((game.cam.viewportWidth*game.cam.zoom)-144)/2 +10+5*i +2-4, 26-16*j +2-4 -1); //new font offset of 6
            //currSprite.setScale(3); //post scaling change

            
            spritesNotDrawn.add(currSprite);
            //go down a line if needed
             //TODO - do this for words, not chars. split on space, array
            if (i >= 17) { 
                i = 0; j++;
            }
            else {
                i++;
            }
        }
        
        //why not just every frame draw a new sprite, pop off the sprites list?
         //when list is empty, display the arrow and wait for user input
        
        Texture text = new Texture(Gdx.files.internal("arrow_down.png"));
        this.arrowSprite = new Sprite(text, 0, 0, 7, 5);
        //this.arrowSprite.setPosition(147*3-2,12*3-1);
        this.arrowSprite.setPosition(147-2-1,12-1-1); //post scaling change
        //this.arrowSprite.setScale(3); //post scaling change
        
        text = new Texture(Gdx.files.internal("text_helper1.png")); //battle_bg1
        //text = new Texture(Gdx.files.internal("battle/battle_bg1.png"));
        //Texture text = new Texture(Gdx.files.internal("throw_rock_anim/helper12.png"));
        this.helperSprite = new Sprite(text, 0, 0, 160, 144);
        //this.helperSprite.setPosition(16*10,16*9);
        //this.helperSprite.setScale(3);
        this.timer = 0;
        

        text = new Texture(Gdx.files.internal("textbox_bg1.png")); //textbox bg1
        this.bgSprite = new Sprite(text, 0, 0, 160, 144);
        
    }
    
    //move text up in text box
     //works b/c DisplayText will begin drawing new chars once SpritesBeingDrawn is small enough
    //parent might need to call step() for frame-correctness, not sure
    class ScrollTextUp extends Action {

        ArrayList<Vector2> positions;
        Vector2 position;
        
        ArrayList<Sprite> text;
        ArrayList<Sprite> otherText;

        public int layer = 110;
        public int getLayer(){return this.layer;}
        public String getCamera() {return "gui";};
        
        //what to do at each iteration
        public void step(Game game) {
            
            this.position = this.positions.get(0);
            this.positions.remove(0);
            
            for (Sprite sprite : this.text) {
                sprite.setPosition(sprite.getX()+this.position.x, sprite.getY()+this.position.y);
            }
            for (Sprite sprite : this.otherText) {
                sprite.setPosition(sprite.getX()+this.position.x, sprite.getY()+this.position.y);
            }
            
            //if done, remove first 24 elements
            //frees up DisplayText's text array, which will get filled with new sprites
            if (this.positions.isEmpty()) {
                for (int i=0; i<18; i++) {
                    this.text.remove(0);
                }
                game.actionStack.remove(this);

                return;
            }
        }
        
        public ScrollTextUp(Game game, ArrayList<Sprite> text, ArrayList<Sprite> otherText) {
            
            //TODO - bug, fails if scrolling up twice
            //TODO - get comma to work (what text contains comma?)
            
            this.text = text;
            this.otherText = otherText;
            
            
            this.positions = new ArrayList<Vector2>();
            for (int i=0; i < 5; i++) {
                this.positions.add(new Vector2(0,0));
            }
            this.positions.add(new Vector2(0,8));
            for (int i=0; i < 5; i++) {
                this.positions.add(new Vector2(0,0));
            }
            this.positions.add(new Vector2(0,8));
            
        }
    }

    //play a sound, ie victory fanfare
     //unique b/c need to mute battle music
    class PlaySound_Text extends Action {
            
        Music music;
        
        
        float initialVolume; //different tracks have diff volume

        public PlaySound_Text(String sound, Action nextAction) {
            
            this.nextAction = nextAction;
            this.playedYet = false;
            
            if (sound == "fanfare1") {
                this.music = Gdx.audio.newMusic(Gdx.files.internal("catch_fanfare.mp3")); //use this
                this.music.setLooping(false);
            }

            else if (sound == "Raikou") {
                this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/243Cry.ogg")); //use this
                this.music.setLooping(false);
            }
            else if (sound == "Entei") {
                this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/244Cry.ogg")); //use this
                this.music.setLooping(false);
            }
            else if (sound == "Suicune") {
                this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/245Cry.ogg"));
                this.music.setLooping(false);
            }
        }
        boolean playedYet; //do music.play on first step
        
        @Override
        public void step(Game game) {
            //play the sound
            if (this.music != null && !this.playedYet) {

                //TODO - use this probably in the future
//                this.initialVolume = game.currMusic.getVolume();
//                game.currMusic.setVolume(0f);
                
                this.music.play();
                this.playedYet = true;
            }
            
            if (!this.music.isPlaying()) {

//                game.currMusic.setVolume(this.initialVolume);
                game.actionStack.remove(this);
                
                PublicFunctions.insertToAS(game, this.nextAction);
            }
        }
    }
}



//put this in map constructor - doesn't need an action.
class PlaySound extends Action {
        
    Music music;
    
    String sound;

    // for playing pokemon cry
    public PlaySound(Pokemon pokemon, Action nextAction) {
        this.nextAction = nextAction;
        this.playedYet = false;
        this.sound = pokemon.name;
        this.music = null;

        // if it's crystal pokemon, load from crystal dir
        if (pokemon.generation == Pokemon.Generation.CRYSTAL) {
            if (pokemon.dexNumber.equals("000")) {
                this.music = Gdx.audio.newMusic(Gdx.files.internal("crystal_pokemon/cries/" + pokemon.dexNumber + ".ogg"));
                this.music.setVolume(.9f);
            }
            else {
                this.music = Gdx.audio.newMusic(Gdx.files.internal("crystal_pokemon/cries/" + pokemon.dexNumber + ".wav"));
                this.music.setVolume(.5f);
            }
            this.music.setLooping(false);
        }
    }

    public PlaySound(String sound, Action nextAction) {
        
        this.nextAction = nextAction;
        this.playedYet = false;
        this.sound = sound;
        
        this.music = null;
        
        if (sound == "laser1") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("data/laser1.wav")); //use this
            this.music.setLooping(false);
        }
        
        else if (sound == "laser2") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("data/laser2.wav")); //use this
            this.music.setLooping(false);
        }

        else if (sound == "laser3") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("data/laser3.wav")); //use this
            this.music.setLooping(false);
        }
        
        else if (sound == "laser4") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("data/laser4.wav")); //use this
            this.music.setLooping(false);
        }
        
        else if (sound == "laser5") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("data/laser6.mp3")); //use this
            this.music.setLooping(false);
        }
        

        else if (sound == "bump2") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("bump2.wav")); //use this
            this.music.setLooping(false);
        }
        else if (sound == "ledge1") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("ledge1.wav")); //use this
            this.music.setLooping(false);
        }
        else if ("menu_open1".equals(sound)) {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("attack_menu/menu_open1.ogg")); //use this
            this.music.setLooping(false);
        }
        
        
        
        else if (sound == "wild_battle") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("battle/battle-vs-wild-pokemon.wav")); //use this
            this.music.setLooping(false);
            this.music.setVolume(.3f);
            
        }
        
        //pkmn
        else if (sound == "Zubat") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/041Cry.ogg"));
            this.music.setLooping(false);
        }
        else if (sound == "Oddish") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/043Cry.ogg"));
            this.music.setLooping(false);
        }
        else if (sound == "Gloom") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/044Cry.ogg"));
            this.music.setLooping(false);
        }
        else if (sound == "Cloyster") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/091Cry.ogg"));
            this.music.setLooping(false);
            this.music.setVolume(.6f);
        }
        else if (sound == "Electabuzz") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/electabuzz.mp3")); //use this
            this.music.setLooping(false);
        }
        else if (sound == "Scyther") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/123Cry.ogg")); 
            this.music.setLooping(false);
        }
        else if (sound == "Tauros") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/128Cry.mp3")); 
            this.music.setLooping(false);
        }
        else if (sound == "Mewtwo") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/150Cry.ogg"));
            this.music.setLooping(false);
        }
        else if (sound.equals("Mega Gengar")) {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/m_gengar_cry1.ogg"));
            this.music.setLooping(false);
            this.music.setVolume(.5f);
        }
        else if (sound == "Spinarak") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/167Cry.ogg"));
            this.music.setLooping(false);
        }
        else if (sound == "Mareep") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/179Cry.wav")); 
            this.music.setVolume(.7f);
            this.music.setLooping(false);
        }
        else if (sound == "Flaaffy") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/180Cry.ogg")); 
            this.music.setLooping(false);
        }
        else if (sound == "Steelix") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/208Cry.mp3")); //use this
            this.music.setLooping(false);
        }
        else if (sound == "Sneasel") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/215Cry.ogg")); 
            this.music.setVolume(.7f);
            this.music.setLooping(false);
        }
        else if (sound == "Raikou") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/243Cry.ogg")); //use this
            this.music.setLooping(false);
        }
        else if (sound == "Entei") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/244Cry.ogg")); //use this
            this.music.setLooping(false);
        }
        else if (sound == "Suicune") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/245Cry.ogg")); //use this
            this.music.setLooping(false);
        }

        else if (sound == "Wurmple") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/265Cry.ogg")); 
            this.music.setLooping(false);
        }
        else if (sound == "Makuhita") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/296Cry.mp3")); 
            this.music.setLooping(false);
        }
        else if (sound == "Hariyama") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/297Cry.mp3")); 
            this.music.setLooping(false);
        }
        else if (sound == "Skitty") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/300Cry.ogg")); 
            this.music.setLooping(false);
        }
        else if (sound == "Sableye") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/302Cry.ogg")); 
            this.music.setLooping(false);
        }
        else if (sound == "Lairon") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/305Cry.ogg")); 
            this.music.setLooping(false);
        }
        else if (sound == "Cacnea") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/331Cry.ogg")); 
            this.music.setLooping(false);
        }
        else if (sound == "Shuppet") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/353Cry.ogg")); 
            this.music.setLooping(false);
        }
        else if (sound == "Starly") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/396Cry.ogg")); 
            this.music.setLooping(false);
        }
        else if (sound == "Shinx") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/403Cry.ogg")); 
            this.music.setLooping(false);
        }
        else if (sound == "Gardevoir") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/282Cry.ogg")); 
            this.music.setLooping(false);
        }
        else if (sound == "Claydol") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/344Cry.ogg")); 
            this.music.setLooping(false);
        }
        else if (sound == "Machop") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("pokemon/cries/066Cry.ogg")); 
            this.music.setLooping(false);
            this.music.setVolume(.5f);
        }
        
        else if (sound == "throw_rock1") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("throw_rock_anim/rock_throw1.mp3"));
            this.music.setLooping(false);
        }
        else if (sound == "throw_pokeball1") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("throw_pokeball_anim/pkball1.mp3"));
            this.music.setLooping(false);
        }
        else if (sound == "poof1") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("throw_pokeball_anim/poof1.mp3"));
            this.music.setLooping(false);
        }
        else if (sound == "pokeball_wiggle1") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("throw_pokeball_anim/wiggle1.mp3"));
            this.music.setLooping(false);
        }
        else if (sound == "click1") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("click1.mp3"));
            this.music.setLooping(false);
        }
        else if (sound == "fanfare1") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("catch_fanfare.mp3"));
            this.music.setLooping(false);
        }
        else if (sound == "run1") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("run1.mp3"));
            this.music.setLooping(false);
        }
        
        //attacks
        else if (sound == "hyperbeam1") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("hyper_beam_anim/hyperbeam1.ogg"));
            this.music.setLooping(false);
        }
        else if (sound == "harden1") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("attacks/harden1.ogg"));
            this.music.setLooping(false);
        }
        else if (sound == "tackle1") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("attacks/tackle1.ogg"));
            this.music.setLooping(false);
        }
        else if (sound.equals("psychic1")) {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("attacks/psychic1.ogg"));
            this.music.setLooping(false);
        }
        else if (sound.equals("night_shade1")) {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("attacks/night_shade1.ogg"));
            this.music.setLooping(false);
        }
        else if (sound.equals("lick1")) {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("attacks/lick1.ogg"));
            this.music.setLooping(false);
        }
        else if (sound.equals("slash1")) {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("attacks/slash1.ogg"));
            this.music.setLooping(false);
        }
        else if (sound.equals("hit_normal1")) {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("attacks/hit_normal1.ogg"));
            this.music.setLooping(false);
        }
        else if (sound == "Mewtwo_Special1") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("attacks/Mewtwo_Special1.ogg"));
            this.music.setLooping(false);
            this.music.setVolume(1.5f);
        }
        else if (sound == "ghost1") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("ghost1.ogg"));
            this.music.setLooping(false);
        }
        else if (sound == "cut1") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("cut1.ogg"));
            this.music.setLooping(false);
        }
        else if (sound == "strength1") {
            this.music = Gdx.audio.newMusic(Gdx.files.internal("strength1.ogg"));
            this.music.setLooping(false);
        }
        else {
            this.music = Gdx.audio.newMusic(Gdx.files.internal(sound+".ogg"));
            this.music.setLooping(false);
        }
    }
    
    boolean playedYet; //do music.play on first step
    
    @Override
    public void step(Game game) {
        
        if (this.sound.equals("mgengar_battle1")) {
//            game.currMusic.stop();
            game.loadedMusic.get("mgengar_battle1").play();
            game.currMusic = game.loadedMusic.get("mgengar_battle1");
//            game.currMusic.play();
//            game.actionStack.remove(this);
//            PublicFunctions.insertToAS(game, this.nextAction);
            return;
        }
        
        if (this.music == null) {
            game.actionStack.remove(this);
            PublicFunctions.insertToAS(game, this.nextAction);
            return;
        }
        //play the sound
        if (!this.playedYet) {
            this.music.play();
            this.playedYet = true;
        }
        
        if (!this.music.isPlaying()) {
            this.music.dispose();
            game.actionStack.remove(this);
            PublicFunctions.insertToAS(game, this.nextAction);
        }
    }
}


class FadeMusic extends Action {
    
    String musicName;
    Music music;
    
    
    
    String direction;
    String shouldPause;
    
    float amt =  -.05f;
    
    float maxVol = 1f;
    
    boolean firstStep = true;
    
    boolean switchCurrMusic = false;
    
    Music.OnCompletionListener onCompleteListener;
    
    public void step(Game game) {
        
        if (this.firstStep == true) {
            
            if (this.direction.equals("in")) {
                
                if (this.musicName.equals("currMusic")) {
                    game.currMusic.play();
                }
                else {
                    //load music if it isnt loaded already
                    if (!game.loadedMusic.containsKey(this.musicName)) {
                        
                        String extension = ".ogg";
                        //test - wav file for android, to enable looping
//                      if (this.musicName.equals("bg_rumble1")) {
//                          extension = ".mp3";
//                          System.out.println("using mp3 extension");
//                      }
                        
                        Music temp = Gdx.audio.newMusic(Gdx.files.internal("music/"+this.musicName+extension)); //danger1.ogg
//                        temp.setLooping(true);  // TODO: don't think I need this
                        temp.setVolume(.1f); //will always fade in (for now, have option to fade in fast) //.1f because of 'failed to allocate' bug
                        if (this.onCompleteListener != null) {
                            temp.setOnCompletionListener(this.onCompleteListener);
                        }
                        game.loadedMusic.put(this.musicName, temp);
                        
                    }

//                  System.out.println("loaded music: "+this.musicName);
//                  System.out.println(game.loadedMusic);
                    
                    //this.music.play(); //TODO - remove
                    game.loadedMusic.get(this.musicName).play();
                }
                
                //TODO - remove
//              //set max volumes
//              if (this.musicName.equals("s_and_c3-2")) {
//                  this.maxVol = .9f;
//              }
            }
            
            if (this.switchCurrMusic == true) {
//              game.currMusic = this.music; //TODO - remove
                
                //if currmusic still playing, fade it out
                if (game.currMusic != null) {
                    if (game.currMusic.isPlaying()) {
//                      game.insertAction(new FadeMusic("currMusic", "out", "pause", .05f, new DoneAction())); 
                        game.currMusic.pause();
                    }
                }
                
                game.currMusic = game.loadedMusic.get(this.musicName);
                game.map.currRoute.music = game.currMusic; // TODO: shouldn't be doing this probably
            }
            
            //set this.music for reference
            if (this.musicName.equals("currMusic")) {
                this.music = game.currMusic;
            }
            else {
                this.music = game.loadedMusic.get(this.musicName);
            }
            
            //shouldn't ever be the case, occasionally is in testing tho.
            if (this.music == null) {
                game.actionStack.remove(this);
                PublicFunctions.insertToAS(game, this.nextAction);
                return;
            }
            
            this.firstStep = false;
        }
        
        this.music.setVolume(this.music.getVolume()+this.amt);
        

        if (this.direction.equals("out")) {
            if (music.getVolume() <= .1f) { //bug - for some reason this can't be .0f - no idea why. 'failed to allocate' issue if play() (on any Music) ever called in future

                
                if (this.shouldPause.equals("pause")) {
                    this.music.setVolume(.1f);
                    this.music.pause();
                }
                else if (this.shouldPause.equals("stop")) {
//                  this.music.setPosition(0); //TODO - remove
//                  this.music.pause();
                    
                    //dispose and remove from loadedMusic
                    this.music.setVolume(.1f);
                    this.music.stop();
                    this.music.dispose();
                    game.loadedMusic.remove(this.musicName);
                    
                }
                game.actionStack.remove(this);
                PublicFunctions.insertToAS(game, this.nextAction);
            }
        }
        else { 
            if (music.getVolume() >= this.maxVol) {

                this.music.setVolume(this.maxVol);
                game.actionStack.remove(this);
                PublicFunctions.insertToAS(game, this.nextAction);
            }
        }
        
    }
    
    
    public FadeMusic(String musicName, String direction, String shouldPause, float rate, Action nextAction) {
        this.musicName = musicName;
        
        this.shouldPause = shouldPause;
        
        
        this.direction = direction;
        if (direction.equals("out")) {
            this.amt = -rate;
        }
        else {
            this.amt = rate;
        }
        
        this.nextAction = nextAction;
    }

    public FadeMusic(String musicName, String direction, String shouldPause, float rate, boolean switchCurrMusic, Action nextAction) {
        this.musicName = musicName;
        
        this.shouldPause = shouldPause;
        
        this.switchCurrMusic = switchCurrMusic;
        
        this.direction = direction;
        if (direction.equals("out")) {
            this.amt = -rate;
        }
        else {
            this.amt = rate;
        }
        
        this.nextAction = nextAction;
    }

    public FadeMusic(String musicName, String direction, String shouldPause, float rate, boolean switchCurrMusic, float maxVol, Action nextAction) {
        
        this.maxVol = maxVol;
        
        this.musicName = musicName;
        
        this.shouldPause = shouldPause;
        
        this.switchCurrMusic = switchCurrMusic;
        
        this.direction = direction;
        if (direction.equals("out")) {
            this.amt = -rate;
        }
        else {
            this.amt = rate;
        }
        
        this.nextAction = nextAction;
    }

    public FadeMusic(String musicName, String direction, String shouldPause, float rate, boolean switchCurrMusic, float maxVol, Music.OnCompletionListener onCompleteListener, Action nextAction) {
        this(musicName, direction, shouldPause, rate, switchCurrMusic, maxVol, nextAction);
        this.onCompleteListener = onCompleteListener;
    }
}
