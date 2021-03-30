package com.pkmngen.game;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.EndPoint;
import com.esotericsoftware.kryonet.Listener;
import com.pkmngen.game.Pokemon.AddToInventory;
import com.pkmngen.game.Pokemon.Emote;
import com.pkmngen.game.Pokemon.Standing;

/**
 * Adds Listener to game.client to receive and handle incoming server
 * connections.
 */
class ClientBroadcast extends Action {
//    HeroPlayer player;
    int playerIndex;

    int timeStep = 0;

    public int layer = 1;

    /**
     * Will choke if it encounters a reference, like game.player, game.map, etc.
     */
    public Action deserializeAction(Network.ActionData actionData) {
        Action newAction = null;
        try {
            Class cl = Class.forName(actionData.className);
            Class[] paramTypes = new Class[actionData.params.length];
            for (int i = 0; i < actionData.params.length; i++) {
                if (actionData.params[i] instanceof Network.ActionData) {
                    actionData.params[i] = this.deserializeAction((Network.ActionData)actionData.params[i]);
                }
                paramTypes[i] = actionData.params[i].getClass();  // not sure if this works
            }
            Constructor con = cl.getConstructor(paramTypes);
            newAction = (Action)con.newInstance(actionData.params);
            
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return newAction;
    }

    public ClientBroadcast(final Game game) {
//        this.player = game.players[1];
        this.playerIndex = 1;

        // game has final keyword because registering listeners below requires it

        // register listeners - trigger whenever client receives message from server
//      this.client.addListener(new ThreadedListener(new Listener() {
        game.client.addListener(new Listener() {
            public void connected(Connection connection) {
            }

            public void received(Connection connection, final Object object) {


                if (object instanceof Network.ActionData) {
                    Network.ActionData actionData = (Network.ActionData) object;
                    game.insertAction(ClientBroadcast.this.deserializeAction(actionData));
                }
                
                // Annoying, but need to handle the received object in the Gdx thread
                // because only the Gdx thread can make OpenGL calls.
                Runnable runnable = new Runnable() {
                    public void run() {
                        try {
                            if (object instanceof Network.PlayerData) {
                                Network.PlayerData playerData = (Network.PlayerData) object;
//                                game.player.position = playerData.position;
//                                game.player.name = playerData.name;
//                                game.player.pokemon = new ArrayList<Pokemon>();
//                                for (Network.PokemonData pokemonData : playerData.pokemon) {
//                                    game.player.pokemon.add(new Pokemon(pokemonData));
//                                }
//                                if (game.player.pokemon.size() > 0) {
//                                    game.player.currPokemon = game.player.pokemon.get(0);
//                                }
//                                game.player.itemsDict = playerData.itemsDict;
//                                game.player.network = new Player.Network(playerData.position.cpy());  // re-initialize loading zone boundaries based off of current position.
//                                game.player.setColor(playerData.color);
                                // TODO: verify nothing is lost here.
                                game.player = new Player(playerData);
                                game.cam.position.set(game.player.position.x+16, game.player.position.y, 0);
                            }

                            else if (object instanceof Network.RelocatePlayer) {
                                Network.RelocatePlayer relocatePlayer = (Network.RelocatePlayer) object;
                                game.player.position = relocatePlayer.position.cpy();
                                game.cam.position.set(relocatePlayer.position.cpy().add(16,0), 0f);
                            }
                            else if (object instanceof Network.Logout) {
                                Network.Logout logout = (Network.Logout) object;
                                if (!game.players.containsKey(logout.playerId)) {
                                    System.out.println("Logout: Invalid player ID " + logout.playerId + ", sent by server");
                                    throw new Exception();
                                }
                                Player player = game.players.get(logout.playerId);
                                game.actionStack.remove(player.standingAction);
                                game.players.remove(logout.playerId);
                            }
                            else if (object instanceof Network.MapTiles) {
                                Network.MapTiles mapTiles = (Network.MapTiles) object;
                                for (Network.TileData tileData : mapTiles.tiles) {
                                    // TODO: remove
//                                    Tile tile = new Tile(tileData.tileName, tileData.tileNameUpper, tileData.pos.cpy(), true, null);
                                    Tile tile = Tile.get(tileData, null);
                                    if (tileData.interiorIndex != 0) {
                                        game.map.interiorTiles.get(tileData.interiorIndex).put(tileData.pos.cpy(), tile);
                                    }
                                    else {
                                        game.map.overworldTiles.put(tileData.pos.cpy(), tile);
                                    }
                                }
                                if (mapTiles.timeOfDay != null) {
                                    CycleDayNight.dayTimer = mapTiles.dayTimer;
                                    game.map.timeOfDay = mapTiles.timeOfDay;
                                }
                            }
                            else if (object instanceof Network.ServerPlayerData) {
                                Network.ServerPlayerData serverPlayerData = (Network.ServerPlayerData) object;
                                if (!game.players.containsKey(serverPlayerData.number)) {
                                    Player player = new Player();
                                    player.name = serverPlayerData.name;
                                    player.position = serverPlayerData.position.cpy();
                                    player.type = Player.Type.REMOTE;
                                    player.network.id = serverPlayerData.number;  // on client side, just index player by number
                                    if (serverPlayerData.isInterior) {
                                        player.network.tiles = game.map.interiorTiles.get(game.map.interiorTilesIndex);
                                    }
                                    else {
                                        player.network.tiles = game.map.overworldTiles;
                                    }
                                    player.setColor(serverPlayerData.color);
                                    game.players.put(serverPlayerData.number, player);
                                }
                                Player player = game.players.get(serverPlayerData.number);
                                if (game.actionStack.contains(player.standingAction)) {
                                    player.standingAction = new PlayerStanding(game, player, false, true);
                                    game.insertAction(player.standingAction);
                                }
                            }
                            // Server is notifying client that a player has moved
                            else if (object instanceof Network.MovePlayer) {
                                Network.MovePlayer movePlayer = (Network.MovePlayer) object;
                                if (!game.players.containsKey(movePlayer.playerId)) {
                                    System.out.println("MovePlayer: Invalid player ID " + movePlayer.playerId + ", sent by server");
                                    throw new Exception();
                                }
                                Player player = game.players.get(movePlayer.playerId);
                                player.network.shouldMove = true;
                                player.network.dirFacing = movePlayer.dirFacing;
                                player.network.isRunning = movePlayer.isRunning;
                            }
                            // Server is notifying client that a pokemon has moved
                            else if (object instanceof Network.MovePokemon) {
                                Network.MovePokemon movePokemon = (Network.MovePokemon) object;
                                if (!game.map.pokemon.containsKey(movePokemon.position)) {
                                    System.out.println("MovePokemon: Invalid pokemon position " + movePokemon.position.toString() + ", sent by server");
                                    throw new Exception();
                                }
                                Pokemon pokemon = game.map.pokemon.get(movePokemon.position);
                                pokemon.dirFacing = movePokemon.dirFacing;
                                pokemon.shouldMove = true;
                            }
                            // server is notifying client that the local player has entered a battle
                            else if (object instanceof Network.BattleData) {
                                Network.BattleData battleData = (Network.BattleData) object;
                                game.player.network.doEncounter = battleData;
                            }
                            // server is updating client with the result of a battle turn
                            else if (object instanceof Network.BattleTurnData) {
                                Network.BattleTurnData turnData = (Network.BattleTurnData) object;
                                game.battle.network.turnData = turnData;
//                                System.out.println("Recieved turn data.");
                            }

                            // A server is changing a tile (ie player built or cut something within this player's loading zone)
                            else if (object instanceof Network.TileData) {
                                Network.TileData tileData = (Network.TileData) object;
                                Tile newTile = new Tile(tileData.tileName, tileData.tileNameUpper,
                                                        tileData.pos.cpy(), true, null);
                                newTile.hasItem = tileData.hasItem;
                                newTile.hasItemAmount = tileData.hasItemAmount;
                                game.map.tiles.put(tileData.pos.cpy(), newTile);
                                PlayerStanding.adjustSurroundingTiles(newTile);
                            }
                            else if (object instanceof Network.OverworldPokemonData) {
                                Network.OverworldPokemonData pokemonData = (Network.OverworldPokemonData) object;
                                if (pokemonData.remove) {
                                    Pokemon pokemon = game.map.pokemon.get(pokemonData.overworldPos);
                                    if (pokemon == null) {
                                        System.out.println("OverworldPokemonData: Invalid position " + pokemonData.overworldPos.toString() + ", sent by server");
                                        throw new Exception();
                                    }
                                    game.map.pokemon.remove(pokemonData.overworldPos);
                                    game.actionStack.remove(pokemon.standingAction);
                                }
                                else {
                                    Pokemon pokemon = new Pokemon(pokemonData);
                                    pokemon.position = pokemonData.overworldPos.cpy();
                                    game.map.pokemon.put(pokemonData.overworldPos, pokemon);
                                    pokemon.type = Player.Type.REMOTE;
                                    // Create standing action in the overworld
                                    // TODO: remove
//                                    System.out.println(String.valueOf(pokemon.mapTiles == game.map.overworldTiles));
                                    game.insertAction(pokemon.new Standing());
                                }
                            }
                            else if (object instanceof Network.PokemonData) {
                                Network.PokemonData pokemonData = (Network.PokemonData) object;
                                game.player.pokemon.set(pokemonData.index, new Pokemon(pokemonData));
                            }
                            else if (object instanceof Network.PickupItem) {
                                Network.PickupItem pickupItem = (Network.PickupItem) object;
                                Tile tile = game.map.tiles.get(pickupItem.pos);
                                game.playerCanMove = false;
                                String number = "a";
                                String plural = "";
                                if (tile.hasItemAmount > 1) {
                                    number = String.valueOf(tile.hasItemAmount);
                                    plural = "S";
                                    if (tile.hasItem.endsWith("s")) {
                                        plural = "ES";
                                    }
                                }
                                game.insertAction(new DisplayText(game, "Found "+number+" "+tile.hasItem.toUpperCase()+plural+"!", "fanfare1.ogg", null,
                                                                  new SetField(game, "playerCanMove", true,
                                                                  null)));
                                tile.pickUpItem(game.player);
                            }
                            else if (object instanceof Network.UseHM) {
                                Network.UseHM useHM = (Network.UseHM) object;
                                if (!game.players.containsKey(useHM.playerId)) {
                                    System.out.println("UseHM: Invalid player id " + useHM.playerId + ", sent by: server");
                                    throw new Exception();
                                }
                                Player player = game.players.get(useHM.playerId);
                                if (useHM.hm.equals("STOP")) {
                                    player.isBuilding = false;
                                    player.isCutting = false;
                                    player.isHeadbutting = false;
                                    player.isJumping = false;
                                }
                                else if (useHM.hm.equals("CUT")) {
                                    Vector2 pos = player.facingPos(useHM.dirFacing);
                                    Tile currTile = player.network.tiles.get(pos);
                                    if (currTile.attrs.containsKey("cuttable") && currTile.attrs.get("cuttable")) {
                                        Action action = new CutTreeAnim(game, game.map.overworldTiles.get(pos), null);
                                        game.map.interiorTiles.get(game.map.interiorTilesIndex).remove(currTile.position.cpy());
                                        game.insertAction(action);
                                    }
                                }
                                else if (useHM.hm.equals("JUMP")) {
                                    player.isHeadbutting = false;
                                    player.isBuilding = false;
                                    player.isCutting = false;
                                    player.isJumping = true;
                                }
                            }
                            else if (object instanceof Network.UseItem) {
                                Network.UseItem useItem = (Network.UseItem) object;
                                if (!game.players.containsKey(useItem.playerId)) {
                                    System.out.println("UseHM: Invalid player id " + useItem.playerId + ", sent by: server");
                                    throw new Exception();
                                }
                                Player player = game.players.get(useItem.playerId);
                                Vector2 pos = player.facingPos(useItem.dirFacing);
//                                System.out.println(pos);
                                if (useItem.item.contains("apricorn")) {
                                    game.insertAction(new PlantTree(pos, null));
                                }
                            }
                            else if (object instanceof Network.PausePokemon) {
                                Network.PausePokemon pausePokemon = (Network.PausePokemon) object;
                                Pokemon pokemon = game.map.pokemon.get(pausePokemon.position);
                                pokemon.canMove = false;
                                game.playerCanMove = false;
                                String oppDir = "down";
                                if (game.player.dirFacing.equals("up")) {
                                    oppDir = "down";
                                }
                                else if (game.player.dirFacing.equals("down")) {
                                    oppDir = "up";
                                }
                                else if (game.player.dirFacing.equals("right")) {
                                    oppDir = "left";
                                }
                                else if (game.player.dirFacing.equals("left")) {
                                    oppDir = "right";
                                }
                                Action nextAction = new SetField(pokemon, "dirFacing", oppDir,
                                                    new WaitFrames(game, 20,
                                                    new SplitAction(pokemon.new Emote("happy", null),
                                                    new WaitFrames(game, 20,
                                                    new PlaySound(pokemon,
                                                    null)))));
                                if (pokemon.previousOwner != game.player) {
                                    nextAction.append(new DisplayText(game, pokemon.name.toUpperCase()+" seems friendly. ", null, false, true, null));
                                }
                                nextAction.append(new DisplayText(game, "Add "+pokemon.name.toUpperCase()+" to your party?", null, true, false,
                                                  new DrawYesNoMenu(null,
                                                      new DisplayText.Clear(game,
                                                      new WaitFrames(game, 3,
                                                      pokemon.new AddToInventory(
                                                      new SetField(game, "playerCanMove", true,
                                                      new SetField(pokemon, "canMove", true,
                                                      null))))),
                                                  new DisplayText.Clear(game,
                                                  new WaitFrames(game, 3,
                                                  new SetField(game, "playerCanMove", true, 
                                                  new SetField(pokemon, "canMove", true,
                                                  pokemon.new UnPause(
                                                  ))))))));
                                game.insertAction(nextAction);
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        synchronized (this) {
                            this.notify();
                        }
                    }
                };
                Gdx.app.postRunnable(runnable);
                try {
                    synchronized (runnable) {
                        runnable.wait();
                    }
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    public int getLayer(){return this.layer;}
}

public class Network {
    static public final int port = 54555;

    // This registers objects that are going to be sent over the network.
    static public void register (EndPoint endPoint) {
        Kryo kryo = endPoint.getKryo();
        kryo.register(com.badlogic.gdx.math.Vector2.class);
        kryo.register(com.badlogic.gdx.physics.box2d.Filter.class);
        kryo.register(java.util.ArrayList.class);
        kryo.register(java.util.HashMap.class);
        kryo.register(String[].class);
        kryo.register(Color.class);
        kryo.register(MapTiles.class);
        kryo.register(TileData.class);
        kryo.register(Login.class);
        kryo.register(Logout.class);
        kryo.register(PlayerDataBase.class);
        kryo.register(PokemonDataBase.class);
        kryo.register(MovePlayer.class);
        kryo.register(Pokemon.Generation.class);
        kryo.register(ServerPlayerData.class);
        kryo.register(BattleData.class);
        kryo.register(BattleTurnData.class);
        kryo.register(Attack.class);
        kryo.register(DoBattleAction.class);
        kryo.register(Battle.DoTurn.Type.class);
        kryo.register(RelocatePlayer.class);
        kryo.register(RouteData.class);
        kryo.register(UseHM.class);
        kryo.register(UseItem.class);
        kryo.register(Sleep.class);
        kryo.register(Craft.class);
        kryo.register(DropItem.class);
        kryo.register(DropPokemon.class);
        kryo.register(PausePokemon.class);
        kryo.register(SaveData.class);
        kryo.register(PickupItem.class);
        kryo.register(LearnMove.class);
        kryo.register(MovePokemon.class);
        kryo.register(OverworldPokemonData.class);
        
        // Any new 'versioned' classes need to be added at bottom (I think)
        kryo.register(PlayerData.class);  // rename to PlayerDataV05 when moving to v0.6
        kryo.register(PokemonDataV04.class);
        kryo.register(PokemonData.class);  // rename to PokemonDataV05 when moving to v0.6
    }

    static public class ActionData {
        String className;
        String objectId;
//        Class[] paramTypes;
//        String classId;
        Object[] params;
        ActionData nextAction;

        public ActionData(Action action){
            // ex: com.pkmngen.game.Game@85a978
//            this.con = ;
//            cls.cast(object).getClass().getEnclosingConstructor();
            this.className = action.getClass().toString();
//            this.objectId = action.toString();
            this.params = action.params;
//            this.classID = 
//            if (action.nextAction != null) {
//                this.nextAction = new ActionData(action.nextAction);
//            }
            // 
            for (int i = 0; i < this.params.length; i++) {
                Object param = this.params[i];
                if (param instanceof Action) {
                    this.params[i] = new ActionData((Action)param);
                }
            }
        }

//        public ActionData() {
//        }
    }

    static public class BattleData {
        PokemonData pokemonData;
        String playerId = null;

        public BattleData(){}

        public BattleData(Pokemon pokemon) {
            this.pokemonData = new PokemonData(pokemon);
        }

        public BattleData(Pokemon pokemon, String playerId) {
           this(pokemon);
           this.playerId = playerId;
        }
    }

    static public class BattleTurnData {
        boolean oppFirst;
        Attack playerAttack;
        String playerTrappedBy = null;
        int playerTrapCounter = 0;
        Attack enemyAttack;
        String enemyTrappedBy = null;
        int enemyTrapCounter = 0;
        // Item-related data
        String itemName;
        int numWobbles;
        // If player chose 'run', whether or not it was successful.
        boolean runSuccessful;

        public BattleTurnData(){}
    }

    /**
     * Sent client->server to notify that the player has crafted an item.
     */
    static public class Craft {
        String playerId;
        int craftIndex;
        int amount;

        public Craft(){}

        public Craft(String playerId, int craftIndex, int amount) {
            this.playerId = playerId;
            this.craftIndex = craftIndex;
            this.amount = amount;
        }
    }

    static public class DoBattleAction {
        String playerId;
        String attack;
        String itemName;
        int pokemonIndex;
        Battle.DoTurn.Type type = Battle.DoTurn.Type.ATTACK;

        public DoBattleAction(){}

        public DoBattleAction(String playerId, Battle.DoTurn.Type type, int pokemonIndex) {
            this(playerId, type);
            this.pokemonIndex = pokemonIndex;
        }

        public DoBattleAction(String playerId, Battle.DoTurn.Type type, String action) {
            this(playerId, type);
            this.attack = action;
            this.itemName = action;
        }

        public DoBattleAction(String playerId, Battle.DoTurn.Type type) {
            this.playerId = playerId;
            this.type = type;
        }
    }

    /**
     * Sent client->server to notify that the player has dropped an item.
     */
    static public class DropItem {
        String playerId;
        String itemName;
        int amount;
        Vector2 pos;

        public DropItem(){}

        public DropItem(String playerId, String itemName, int amount, Vector2 pos) {
            this.playerId = playerId;
            this.itemName = itemName;
            this.amount = amount;
            this.pos = pos;
        }
    }

    /**
     * Sent client->server to notify that the player has dropped a pokemon.
     */
    static public class DropPokemon {
        String playerId;
        int index;
        String dirFacing;
        boolean pickingUp;
        boolean pausing = false;
        Vector2 pos;

        public DropPokemon(){}
        
        public DropPokemon(String playerId, Vector2 pos) {
            this.playerId = playerId;
            this.pos = pos;
            this.pickingUp = true;
        }

        /**
         * TODO: remove if unused.
         */
        public DropPokemon(String playerId, String dirFacing, boolean pausing) {
            this.playerId = playerId;
            this.dirFacing = dirFacing;
            this.pausing = pausing;
        }

        public DropPokemon(String playerId, int index, String dirFacing) {
            this.playerId = playerId;
            this.index = index;
            this.dirFacing = dirFacing;
            this.pickingUp = false;
        }
    }

    /**
     * Sent client->server to notify that the player is making a pokemon learn a move.
     */
    static public class LearnMove {
        String playerId;
        int pokemonIndex;
        int replaceIndex;
        String moveName;

        public LearnMove(){}

        public LearnMove(String playerId, int pokemonIndex, int replaceIndex, String moveName) {
            this.playerId = playerId;
            this.pokemonIndex = pokemonIndex;
            this.replaceIndex = replaceIndex;
            this.moveName = moveName;
        }
    }

    static public class Login {
        public String playerId;
        Color color;

        public Login(){}

        public Login(String playerId, Color color) {
            this.playerId = playerId;
            this.color = color;
        }
    }

    static public class Logout {
        public String playerId;

        public Logout(){}

        public Logout(String playerId) {
            this.playerId = playerId;
        }
    }

    static public class MapTiles {
        public ArrayList<TileData> tiles = new ArrayList<TileData>();
        // store routes as classId->Route
        public HashMap<String, RouteData> routes = new HashMap<String, RouteData>();
        public ArrayList<Vector2> edges = new ArrayList<Vector2>();
        
        public ArrayList<HashMap<Vector2, TileData>> interiorTiles = new ArrayList<HashMap<Vector2, TileData>>();
        int interiorTilesIndex;

        // Used to sync time with server
        String timeOfDay = null;
        int dayTimer;

        public MapTiles(){}
    }

    static public class MovePlayer {
        String playerId;
        public String dirFacing;
        boolean isRunning;

        public MovePlayer(){}

        public MovePlayer(String playerId, String dirFacing, boolean isRunning) {
            this.playerId = playerId;
            this.dirFacing = dirFacing;
            this.isRunning = isRunning;
        }
    }

    /**
     * Sent server->client to notify that Pokemon has moved.
     */
    static public class MovePokemon {
        Vector2 position;
        public String dirFacing;

        public MovePokemon(){}

        public MovePokemon(Pokemon pokemon) {
            this.position = pokemon.position;
            this.dirFacing = pokemon.dirFacing;
        }
    }

    /**
     * For now just a separate class to communicate to client that this isn't a Player's pokemon,
     * but exists on the overworld.
     */
    static public class OverworldPokemonData extends PokemonData {
        Vector2 overworldPos;
        boolean remove = false;

        public OverworldPokemonData() {}

        public OverworldPokemonData(Pokemon pokemon, Vector2 position, boolean remove) {
            this(pokemon, position);
            this.remove = remove;
        }

        public OverworldPokemonData(Pokemon pokemon, Vector2 position) {
            super(pokemon);
            this.overworldPos = position;
       }
    }

    /**
     * For now just a separate class to communicate to client that this isn't a Player's pokemon,
     * but exists on the overworld.
     */
    static public class PausePokemon {
        String playerId;
        Vector2 position;
        boolean shouldPause;

        public PausePokemon() {}

        public PausePokemon(String playerId, Vector2 position, boolean shouldPause) {
            this.playerId = playerId;
            this.position = position;
            this.shouldPause = shouldPause;
        }
    }

    /**
     * Sent client->server to notify that the player is picking up an item.
     */
    static public class PickupItem {
        String playerId;
        String dirFacing;  // direction player is facing as they pick up the item
        Vector2 pos;  // ignored server-side

        public PickupItem(){}

        public PickupItem(String playerId, String dirFacing) {
            this.playerId = playerId;
            this.dirFacing = dirFacing;
        }
    }

    static public class PlayerDataBase {
        public Vector2 position;
        public String name;
        ArrayList<PokemonDataBase> pokemon;
        PokemonDataBase currPokemon;
        Map<String, Integer> itemsDict;
        String id;
        String number;
        Color color;
        String dirFacing;
        public Vector2 spawnLoc;
        boolean isInterior;
        boolean displayedMaxPartyText;
        
        boolean isFlying;
        int flyingIndex = 0;

        public PlayerDataBase(){}

        public PlayerDataBase(PlayerDataBase base) {
            this.position = base.position;
            this.name = base.name;
            this.pokemon = base.pokemon;
            this.currPokemon = base.currPokemon;
            this.itemsDict = base.itemsDict;
            this.id = base.id;
            this.number = base.number;
            this.color = base.color;
            this.dirFacing = base.dirFacing;
            this.spawnLoc = base.spawnLoc;
            this.isInterior = base.isInterior;
            this.displayedMaxPartyText = base.displayedMaxPartyText;
            this.isFlying = base.isFlying;
            this.flyingIndex = base.flyingIndex;
        }

        public PlayerDataBase(Player player) {
            this.position = player.position.cpy();
            this.position.x = this.position.x - (this.position.x % 16);
            this.position.y = this.position.y - (this.position.y % 16);
            this.name = player.name;
            this.pokemon = new ArrayList<PokemonDataBase>();
            for (Pokemon pokemon : player.pokemon) {
                this.pokemon.add(new PokemonData(pokemon));
            }
            if (player.currPokemon != null) {
                this.currPokemon = new PokemonData(player.currPokemon);
            }
            this.itemsDict = player.itemsDict;
            this.id = player.network.id;
            this.number = player.network.number;
            this.color = player.color;
            this.dirFacing = player.dirFacing;
            this.spawnLoc = player.spawnLoc;
            this.displayedMaxPartyText = player.displayedMaxPartyText;
            this.isFlying = player.isFlying;
            if (player.flyingAction != null) {
                this.flyingIndex = player.pokemon.indexOf(player.flyingAction.pokemon);
            }
        }
    }

    /**
     * Data associated with the Player to be sent over the network.
     *
     * Used to initialize a Player instance.
     */
    static public class PlayerData extends PlayerDataBase {
        public int spawnIndex = -1;

        public static PlayerData get(PlayerDataBase base) {
            if (PlayerData.class.isInstance(base)) {
                return (PlayerData)base;
            }
            // else, assume it's PlayerDataBase (?)
            return new PlayerData(base);
        }

        public PlayerData(PlayerDataBase base) {
            super(base);
        }

        public PlayerData() {
            // Note - kyro deserilization uses the no arg constructor,
            // so if you need to init anything can put it here?
            super();
        }

        public PlayerData(Player player) {
            super(player);
            this.spawnIndex = player.spawnIndex;
        }
    }

    /**
     * These fields are from pokemon wilds version 0.4.
     */
    static public class PokemonDataBase {
        String name;
        int level;
        Pokemon.Generation generation;
        int hp;
        boolean isShiny;
        String[] attacks = new String[4];
        int index;  // index in player inventory
        String status = null;
        String previousOwnerName = null;
        
        // overworld-related
        Vector2 position;
        boolean isInterior = false;
        int harvestTimer = 0;

        public PokemonDataBase() {}

        public PokemonDataBase(Pokemon pokemon) {
            this.name = pokemon.name;
            this.level = pokemon.level;
            this.generation = pokemon.generation;
            this.isShiny = pokemon.isShiny;
            this.hp = pokemon.currentStats.get("hp");
            this.attacks[0] = pokemon.attacks[0];
            this.attacks[1] = pokemon.attacks[1];
            this.attacks[2] = pokemon.attacks[2];
            this.attacks[3] = pokemon.attacks[3];
            // TODO: psn, para etc status

            this.position = pokemon.position;
            if (pokemon.mapTiles != null) {
                this.isInterior = (pokemon.mapTiles != Game.staticGame.map.overworldTiles);
            }
            this.status = pokemon.status;
            this.harvestTimer = pokemon.harvestTimer;
            if (pokemon.previousOwner != null) {
                this.previousOwnerName = pokemon.previousOwner.name;
            }
        }

        public PokemonDataBase(Pokemon pokemon, int index) {
            this(pokemon);
            this.index = index;
        }
    }

    /**
     * This is just for testing, no fields were added here.
     */
    static public class PokemonDataV04 extends PokemonDataBase {
        public boolean test = false;
        
        public PokemonDataV04() {
            super();
        }

        public PokemonDataV04(Pokemon pokemon) {
            super(pokemon);
        }

        public PokemonDataV04(Pokemon pokemon, int index) {
            super(pokemon, index);
        }
    }
    
    /**
     * Fields added in v0.5
     */
    static public class PokemonData extends PokemonDataV04 {
        public String gender = null;
        public String eggHatchInto = null;
        public int friendliness = 0;  // mistakenly didn't include this in v0.4
        public boolean aggroPlayer = false;

        public PokemonData() {
            super();
        }

        public PokemonData(Pokemon pokemon) {
            super(pokemon);
            this.gender = pokemon.gender;
            this.eggHatchInto = pokemon.eggHatchInto;
            this.friendliness = pokemon.happiness;
            this.aggroPlayer = pokemon.aggroPlayer;
        }

        public PokemonData(Pokemon pokemon, int index) {
            super(pokemon, index);
            this.gender = pokemon.gender;
            this.eggHatchInto = pokemon.eggHatchInto;
            this.friendliness = pokemon.happiness;
            this.aggroPlayer = pokemon.aggroPlayer;
        }
    }

    /*
     * Sent by server, tells client to move player to given location
     */
    static public class RelocatePlayer {
        Vector2 position;

        public RelocatePlayer(){}

        public RelocatePlayer(Vector2 position) {
            this.position = position.cpy();
        }
    }

    static public class RouteData {
        String classId;
        String name;
        int level;
        ArrayList<PokemonDataBase> pokemon = new ArrayList<PokemonDataBase>();
        ArrayList<String> allowedPokemon;
        ArrayList<String> musics;
        int musicsIndex = 0;

        public RouteData(){}

        public RouteData(Route route) {
            this.classId = route.toString();
            this.name = route.name;
            this.level = route.level;
            this.allowedPokemon = new ArrayList<String>(route.allowedPokemon);
            this.musics = new ArrayList<String>(route.musics);
            this.musicsIndex = route.musicsIndex;
            for (Pokemon pokemon : route.pokemon) {
                this.pokemon.add(new PokemonData(pokemon));
            }
        }
    }

    /**
     * Used when serializing all game data to file (saving the game).
     */
    static public class SaveData {
        // TODO
        MapTiles mapTiles = new Network.MapTiles();
        ArrayList<Network.PlayerDataBase> players = new ArrayList<Network.PlayerDataBase>();
        Network.PlayerDataBase playerData;
        HashMap<Vector2, PokemonDataBase> overworldPokemon = new HashMap<Vector2, PokemonDataBase>();

        public SaveData() {}

        public SaveData(Game game) {
            for (Tile tile : game.map.overworldTiles.values()) {
                // store unique routes as hashmap ClassID->Route
                if (tile.routeBelongsTo != null && !this.mapTiles.routes.containsKey(tile.routeBelongsTo.toString())) {
                    this.mapTiles.routes.put(tile.routeBelongsTo.toString(), new RouteData(tile.routeBelongsTo));
                }
                this.mapTiles.tiles.add(new TileData(tile));
            }
            // Save interior tiles
            for (HashMap<Vector2, Tile> tiles : game.map.interiorTiles) {
                HashMap<Vector2, TileData> tileDatas = null;
                if (tiles != null) {
                    tileDatas  = new HashMap<Vector2, TileData>();
                    for (Tile tile : tiles.values()) {
                        // store unique routes as hashmap ClassID->Route
                        if (tile.routeBelongsTo != null && !this.mapTiles.routes.containsKey(tile.routeBelongsTo.toString())) {
                            this.mapTiles.routes.put(tile.routeBelongsTo.toString(), new RouteData(tile.routeBelongsTo));
                        }
                        tileDatas.put(tile.position, new TileData(tile));
                    }
                }
                this.mapTiles.interiorTiles.add(tileDatas);
            }
            // Save time of day, edges.
            this.mapTiles.interiorTilesIndex = game.map.interiorTilesIndex;
            this.mapTiles.edges = game.map.edges;
            this.mapTiles.timeOfDay = game.map.timeOfDay;
            this.mapTiles.dayTimer = CycleDayNight.dayTimer;

            // Save players to file
            for (Player player : game.players.values()) {
                this.players.add(new PlayerData(player));
            }
            this.playerData =  new PlayerData(game.player);
            this.playerData.isInterior = (game.map.tiles != game.map.overworldTiles);
            Pokemon currPokemon;
            for (Vector2 pos : game.map.pokemon.keySet()) {
                currPokemon = game.map.pokemon.get(pos);
                this.overworldPokemon.put(pos, new PokemonData(currPokemon));
            }
        }
    }


//    static public class SaveDataV04 extends SaveData {
//
//        Network.PlayerDataV04 playerData;
//        
//        
//        public SaveDataV04(Game game) {
//            super(game);
//        }
//    }

    static public class ServerPlayerData {
        public Vector2 position;
        public String name;
        String number;
        Color color;
        boolean isInterior;

        public ServerPlayerData(){}

        public ServerPlayerData(Player player) {
            this.position = player.position;
            this.name = player.name;
            this.number = player.network.number;
            this.color = player.color;
            this.isInterior = (player.network.tiles != Game.staticGame.map.overworldTiles);
        }
    }

    /**
     * Sent client->server to notify that player is using the sleeping bag.
     */
    static public class Sleep {
        String playerId;
        boolean isSleeping;

        public Sleep(){}

        public Sleep(String playerId, boolean isSleeping) {
            this.playerId = playerId;
            this.isSleeping = isSleeping;
        }
    }

    static public class TileData {
        public Vector2 pos;
        public String tileName;
        public String tileNameUpper;
        String routeBelongsTo;  // this is a string of the route's class id
        HashMap<String, Integer> items;
        public String hasItem;
        public int hasItemAmount;
        String biome;

        // TrainerTipsTile stuff
        boolean isUnown;
        String message = "";
        int interiorIndex;
        ArrayList<Vector2> doorTiles;

        public TileData(){}

        public TileData(Tile tile, int interiorIndex) {
            this(tile);
            this.interiorIndex = interiorIndex;
        }

        public TileData(Tile tile) {
            this.pos = tile.position.cpy();
            this.tileName = tile.name;
            this.tileNameUpper = tile.nameUpper;
            if (tile.routeBelongsTo != null) {
                this.routeBelongsTo = tile.routeBelongsTo.toString();
            }
            if (TrainerTipsTile.class.isInstance(tile)) {
                TrainerTipsTile tTile = (TrainerTipsTile)tile;
                this.isUnown = tTile.isUnown;
                this.message = tTile.message;
            }
            this.items = tile.items;
            this.hasItem = tile.hasItem;
            this.hasItemAmount = tile.hasItemAmount;
            this.doorTiles = tile.doorTiles;
            this.biome = tile.biome;
        }
    }

    /**
     * Sent client->server to notify that the player has used an HM.
     */
    static public class UseHM {
        String playerId;
        int pokemonIndex;
        String hm;
        String dirFacing;
        int movePos;  // used for 'SWITCH'

        public UseHM(){}

        public UseHM(String playerId, int pokemonIndex, String hm, int movePos) {
            this(playerId, pokemonIndex, hm);
            this.movePos = movePos;
        }

        public UseHM(String playerId, int pokemonIndex, String hm, String dirFacing) {
            this(playerId, pokemonIndex, hm);
            this.dirFacing = dirFacing;
        }

        public UseHM(String playerId, int pokemonIndex, String hm) {
            this.playerId = playerId;
            this.pokemonIndex = pokemonIndex;
            this.hm = hm;
        }
    }

    /**
     * Sent client->server to notify that the player used an item.
     */
    static public class UseItem {
        String playerId;
        String item;
        String dirFacing;

        public UseItem(){}

        public UseItem(String playerId, String item, String dirFacing) {
            this.playerId = playerId;
            this.item = item;
            this.dirFacing = dirFacing;
        }
    }

    // TODO: remove if unused
    static public class SyncedHashMap {
        String name;
        String operation;
        Object putgetMe;

        public SyncedHashMap(){}

        public SyncedHashMap(String name, String operation, Object putgetMe) {
            this.name = name;
            this.operation = operation;
            this.putgetMe = putgetMe;
        }
    }
    
}

/**
 * Adds Listener to game.server to receive and handle incoming client
 * connections.
 */
class ServerBroadcast extends Action {
    int timeStep = 0;

    public int layer = 1;
    public ServerBroadcast(final Game game) {
        game.server.addListener(new Listener() {
            public void received(final Connection connection, final Object object) {
                // debug code
//                try {
//                    Thread.sleep(game.map.rand.nextInt(400));
//                } catch (InterruptedException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//                if (object instanceof Network.SyncedHashMap) {
//                    Network.SyncedHashMap synced = (Network.SyncedHashMap) object;
//                    if (synced.operation.equals("get")) {
//                        try {
//                            Object sendMe = game.map.getClass().getField(synced.name).get(synced.putgetMe);
//                            connection.sendTCP(sendMe);
//                        } catch (NoSuchFieldException e) {
//                            e.printStackTrace();
//                        } catch (SecurityException e) {
//                            e.printStackTrace();
//                        } catch (IllegalArgumentException e) {
//                            e.printStackTrace();
//                        } catch (IllegalAccessException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    if (synced.operation.equals("put")) {
//                        game.map.getClass().getField(synced.name).set(synced.putgetMe, synced.putgetMe);
//                    }
//                }
                
                // annoying, but need to handle the received object in the Gdx thread
                // because only Gdx thread can make OpenGL calls
                Runnable runnable = new Runnable() {
                    public void run() {
                        try {
                            // Client is notifying server that it wants to load map tiles
                            if (object instanceof Network.Login) {
                                Network.Login login = (Network.Login) object;

                                // If player doesn't exist, add them to game.players
                                // TODO: obviously need diff handling, need to require password
                                if (!game.players.containsKey(login.playerId)) {
                                    String playerId = login.playerId;
                                    Player player = new Player();
                                    // Spawn player at random edge location
                                    // NOTE: Comment to start players in middle (like for debug)
                                    Vector2 startLoc = game.map.edges.get(game.map.rand.nextInt(game.map.edges.size()));
                                    player.position.set(startLoc);
                                    player.spawnLoc.set(startLoc);
                                    //
                                    player.type = Player.Type.REMOTE;
                                    player.network = player.new Network(player.position);
                                    player.network.connectionId = connection.getID();
                                    player.network.id = playerId;
                                    player.network.number = String.valueOf(game.players.keySet().size());
                                    player.setColor(login.color);
                                    player.network.tiles = game.map.overworldTiles;

                                    // TODO: Debug, remove
                                    // TODO: A bunch of animations and code required for having no pokemon.
                                    player.currPokemon = new Pokemon("machop", 6);
//                                    player.currPokemon = new Pokemon("machop", 2, Pokemon.Generation.CRYSTAL);
//                                    player.currPokemon.attacks[0] = "Ice Beam";
//                                    player.currPokemon.attacks[1] = "Hydro Pump";
                                    player.pokemon.add(player.currPokemon);
//                                    player.currPokemon.currentStats.put("hp", 2); // TODO: debug, remove
//                                    player.pokemon.add(new Pokemon("stantler", 6, Pokemon.Generation.CRYSTAL));
//                                    player.pokemon.add(new Pokemon("lunatone", 6, Pokemon.Generation.CRYSTAL));
//                                    player.pokemon.add(new Pokemon("mareep", 6, Pokemon.Generation.CRYSTAL));
                                    // ---
                                    game.players.put(playerId, player);
                                }
                                Player player = game.players.get(login.playerId);
                                player.network.connectionId = connection.getID();
                                // Send over player data
                                Network.PlayerData playerData = new Network.PlayerData(player);
                                connection.sendTCP(playerData);

                                // If player is in battle, then tell client.
                                if (game.battles.containsKey(player.network.id)) {
                                    Battle battle = game.battles.get(player.network.id);
                                    Pokemon oppPokemon = battle.oppPokemon;
                                    connection.sendTCP(new Network.BattleData(oppPokemon));
                                }

                                // Add standing action to actionStack
                                if (player.standingAction == null) {
                                    player.standingAction = new PlayerStanding(game, player, false, true);
                                    game.insertAction(player.standingAction);
                                }
                                // TODO: remove
//                                if (!game.actionStack.contains(player.standingAction)) {
//                                    game.insertAction(player.standingAction);
//                                }
//                                // TODO: the runnable didn't seem like it helped.
//                                Thread thread = new Thread(new Runnable() {
//                                    public void run() {
                                Network.MapTiles mapTiles = new Network.MapTiles();
                                // Get map.tiles in square around player and send them back
//                                        for (Vector2 position = player.network.loadingZone.bottomLeft();
//                                             position.y < player.network.loadingZone.topRight().y; position.add(16, 0)) {
                                for (Vector2 position : player.network.loadingZone.allPositions()) {
                                    Tile tile = game.map.overworldTiles.get(position);
                                    if (tile == null) {
                                        continue;
                                    }
                                    mapTiles.tiles.add(new Network.TileData(tile));
                                    // Send interior tiles
                                    tile = game.map.interiorTiles.get(game.map.interiorTilesIndex).get(position);
                                    if (tile != null) {
                                        mapTiles.tiles.add(new Network.TileData(tile, game.map.interiorTilesIndex));
                                    }
                                    // TODO: test
                                    if (game.map.pokemon.containsKey(position)) {
                                        Pokemon pokemon = game.map.pokemon.get(position);
                                        connection.sendTCP(new Network.OverworldPokemonData(pokemon, position));
                                    }
                                    // how many can we send without hitting buffer limit?
                                    //                              if (mapTiles.tiles.size() >= 16) {
                                    if (mapTiles.tiles.size() >= 16) {
                                        connection.sendTCP(mapTiles);
                                        mapTiles.tiles.clear();
                                    }
                                }
                                // Time of day sync
                                mapTiles.dayTimer = CycleDayNight.dayTimer;
                                mapTiles.timeOfDay = game.map.timeOfDay;
                                connection.sendTCP(mapTiles);
                                
                                // Send players that are within loading zone
                                // TODO: may have to have a HashMap<Vector2, Player> for performance
                                for (Player otherPlayer : game.players.values()) {
                                    if (otherPlayer == player) {
                                        continue;
                                    }
                                    if (player.network.loadingZone.contains(otherPlayer.position)) {
                                        Network.ServerPlayerData serverPlayerData = new Network.ServerPlayerData(otherPlayer);
                                        connection.sendTCP(serverPlayerData);
                                        serverPlayerData = new Network.ServerPlayerData(player);
                                        game.server.sendToTCP(otherPlayer.network.connectionId, serverPlayerData);
                                    }
                                }
                            }
                            else if (object instanceof Network.Logout) {
                                Network.Logout logout = (Network.Logout) object;
                                if (!game.players.containsKey(logout.playerId)) {
                                    System.out.println("Logout: Invalid player ID " + logout.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                    throw new Exception();
                                }
                                Player player = game.players.get(logout.playerId);
                                
                                // TODO: temporarily gets around issue where if player quits while in battle,
                                // they can't do anything afterward
//                                player.canMove = true;
//                                game.battles.remove(logout.playerId);
                                
//                                game.actionStack.remove(player.standingAction);
                                for (Player otherPlayer : game.players.values()) {
                                    if (otherPlayer == player) {
                                        continue;
                                    }
                                    if (otherPlayer.network.loadingZone.contains(player.position)) {
                                        Network.Logout logoutPlayer = new Network.Logout(player.network.number);
                                        game.server.sendToTCP(otherPlayer.network.connectionId, logoutPlayer);
                                    }
                                }
                            }
                            // Client is notifying server that player has moved
                            else if (object instanceof Network.MovePlayer) {
                                Network.MovePlayer movePlayer = (Network.MovePlayer) object;
                                if (!game.players.containsKey(movePlayer.playerId)) {
                                    System.out.println("MovePlayer: Invalid player ID " + movePlayer.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                    throw new Exception();
                                }
                                Player player = game.players.get(movePlayer.playerId);
                                player.network.shouldMove = true;
                                player.network.dirFacing = movePlayer.dirFacing;
                                player.network.isRunning = movePlayer.isRunning;
                                Vector2 nextPos = player.position.cpy();
                                if (movePlayer.dirFacing.equals("up")) {
                                    nextPos.add(0, 16);
                                }
                                else if (movePlayer.dirFacing.equals("down")) {
                                    nextPos.add(0, -16);
                                }
                                else if (movePlayer.dirFacing.equals("right")) {
                                    nextPos.add(16, 0);
                                }
                                else if (movePlayer.dirFacing.equals("left")) {
                                    nextPos.add(-16, 0);
                                }
                                // Send movement to all other clients if it's in that client's loading zone
                                for (Player otherPlayer : game.players.values()) {
                                    if (otherPlayer == player) {
                                        continue;
                                    }
                                    if (otherPlayer.network.loadingZone.contains(nextPos)) {
                                        if (!otherPlayer.network.loadingZone.contains(player.position)) {
                                            Network.ServerPlayerData serverPlayerData = new Network.ServerPlayerData(player);
                                            game.server.sendToTCP(otherPlayer.network.connectionId, serverPlayerData);
                                            serverPlayerData = new Network.ServerPlayerData(otherPlayer);
                                            game.server.sendToTCP(player.network.connectionId, serverPlayerData);
                                        }
                                        game.server.sendToTCP(otherPlayer.network.connectionId,
                                                              new Network.MovePlayer(player.network.number,
                                                                                     player.network.dirFacing,
                                                                                     player.network.isRunning));
                                    }
                                }
                            }
                            // client is notifying server that it's doing a battle action (fight, run, item, switch)
                            else if (object instanceof Network.DoBattleAction) {
                                Network.DoBattleAction battleAction = (Network.DoBattleAction) object;
                                if (!game.players.containsKey(battleAction.playerId)) {
                                    System.out.println("DoAttack: Invalid player id " + battleAction.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                    throw new Exception();
                                }
                                Player player = game.players.get(battleAction.playerId);
                                if (!game.battles.containsKey(battleAction.playerId)) {
                                    System.out.println("DoAttack: player ID not currently in battle " + battleAction.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                    throw new Exception();
                                }
                                Battle battle = game.battles.get(battleAction.playerId);
                                // Apply the effects of the attack, and send back to client
                                // TODO: calculations here for hit/miss, crit, effect hit, etc.
                                Network.BattleTurnData turnData = new Network.BattleTurnData();

                                if (battleAction.type == Battle.DoTurn.Type.SWITCH) {
                                    turnData.oppFirst = false;
                                    player.numFlees = 0;
                                    player.currPokemon = player.pokemon.get(battleAction.pokemonIndex);
                                }
                                else if (battleAction.type == Battle.DoTurn.Type.ITEM) {
                                    turnData.oppFirst = false;
                                    player.numFlees = 0;
                                    turnData.itemName = battleAction.itemName;
                                    // deduct item from inventory
                                    player.itemsDict.put(turnData.itemName, player.itemsDict.get(turnData.itemName)-1);
                                    if (player.itemsDict.get(turnData.itemName) <= 0) {
                                        player.itemsDict.remove(turnData.itemName);
                                    }
                                    // For now, just have a bunch of cases for each item
                                    if (turnData.itemName.contains("ball")) {
                                        turnData.numWobbles = Battle.gen2CalcIfCaught(game, battle.oppPokemon,
                                                                                      battleAction.itemName);
                                        if (turnData.numWobbles == -1) {
                                            player.pokemon.add(battle.oppPokemon);
                                            Route currRoute = game.map.tiles.get(player.position).routeBelongsTo;
                                            currRoute.pokemon.remove(battle.oppPokemon);
                                            currRoute.genPokemon(256);
                                        }
                                    }
                                }
                                else if (battleAction.type == Battle.DoTurn.Type.RUN) {
                                    turnData.oppFirst = false;
                                    turnData.runSuccessful = battle.calcIfRunSuccessful(game, player);
                                    if (turnData.runSuccessful) {
                                        player.numFlees = 0;
                                    }
                                    else {
                                        player.numFlees++;
                                    }
                                }
                                // Determine attack outcome
                                else {
                                    player.numFlees = 0;
                                    boolean found = false;
                                    for (int i=0; i < player.currPokemon.attacks.length; i++) {
                                        if (player.currPokemon.attacks[i] == null) {
                                            continue;
                                        }
                                        if (player.currPokemon.attacks[i].toLowerCase().equals(battleAction.attack.toLowerCase())) {
                                            found = true;
                                            break;
                                        }
                                    }
                                    if (!found) {
                                        System.out.println("DoAttack: Invalid attack choice " + battleAction.attack + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                        throw new Exception();
                                    }
                                    turnData.playerAttack = battle.attacks.get(battleAction.attack.toLowerCase());

                                    // TODO: fix
                                    int yourSpeed = player.currPokemon.currentStats.get("speed");
                                    int oppSpeed = battle.oppPokemon.currentStats.get("speed");
                                    if (yourSpeed > oppSpeed) {
                                        turnData.oppFirst = false;
                                    }
                                    else if (yourSpeed < oppSpeed) {
                                        turnData.oppFirst = true;
                                    }
                                    else {
                                        if (game.map.rand.nextInt(2) == 0) {
                                            turnData.oppFirst = true;
                                        }
                                    }
                                }
                                // If player's previous pokemon had fainted, we're expecting the player to
                                // switch pokemon
                                if (battle.network.expectPlayerSwitch) {
                                    if (battleAction.type == Battle.DoTurn.Type.SWITCH) {
                                        battle.network.expectPlayerSwitch = false;
                                    }
                                    System.out.println("battle.network.expectPlayerSwitch check");
                                    throw new Exception();
                                }
                                // decide enemy attack choice
                                String attackChoice = battle.oppPokemon.attacks[game.map.rand.nextInt(battle.oppPokemon.attacks.length)];
                                if (attackChoice == null) {
                                    attackChoice = "Struggle";
                                }
                                // update enemy pokemon locally
                                turnData.enemyAttack = battle.attacks.get(attackChoice.toLowerCase());
                                if (!turnData.oppFirst) {
                                    int finalHealth = battle.oppPokemon.currentStats.get("hp");
                                    if (battleAction.type == Battle.DoTurn.Type.ATTACK) {
                                        int damage = Battle.gen2CalcDamage(player.currPokemon, turnData.playerAttack, battle.oppPokemon);
                                        int currHealth = battle.oppPokemon.currentStats.get("hp");
                                        finalHealth = currHealth - damage > 0 ? currHealth - damage : 0;
                                        battle.oppPokemon.currentStats.put("hp", finalHealth);
                                        turnData.playerAttack.damage = damage;
                                        if (finalHealth <= 0) {
                                            // TODO: currRoute == null will cause crash, which would happen if player isn't on
                                            // grass (ie trainer battle?)
                                            Route currRoute = game.map.tiles.get(player.position).routeBelongsTo;
                                            currRoute.pokemon.remove(battle.oppPokemon);
                                            currRoute.genPokemon(256);
                                            player.currPokemon.exp += battle.calcFaintExp(1);
                                            while (player.currPokemon.level < 100 && player.currPokemon.gen2CalcExpForLevel(player.currPokemon.level+1) <= player.currPokemon.exp) {
                                                player.currPokemon.level += 1;
                                            }
                                            // Check if pokemon evolves or not
                                            // TODO: handle when player cancels evolution
                                            for (int i=1; i <= player.currPokemon.level; i++) {
                                                if (Specie.gen2Evos.get(player.currPokemon.name.toLowerCase()).containsKey(String.valueOf(i))) {
                                                    String evolveTo = Specie.gen2Evos.get(player.currPokemon.name.toLowerCase()).get(String.valueOf(i));
                                                    player.currPokemon.evolveTo(evolveTo);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    // If enemy is still alive after attack, or player failed to run, or player failed
                                    // to catch pokemon, then enemy pokemon attacks player pokemon.
                                    if ((battleAction.type == Battle.DoTurn.Type.ATTACK && finalHealth > 0) ||
                                        (battleAction.type == Battle.DoTurn.Type.RUN && !turnData.runSuccessful) ||
                                        (battleAction.type == Battle.DoTurn.Type.ITEM && turnData.numWobbles != -1)) {
                                        int damage = Battle.gen2CalcDamage(battle.oppPokemon, turnData.enemyAttack, player.currPokemon);
                                        int currHealth = player.currPokemon.currentStats.get("hp");
                                        finalHealth = currHealth - damage > 0 ? currHealth - damage : 0;
                                        player.currPokemon.currentStats.put("hp", finalHealth);
                                        turnData.enemyAttack.damage = damage;
                                        if (finalHealth <= 0) {
                                            battle.network.expectPlayerSwitch = true;
                                        }
                                    }
                                }
                                else {
                                    int damage = Battle.gen2CalcDamage(battle.oppPokemon, turnData.enemyAttack, player.currPokemon);
                                    int currHealth = player.currPokemon.currentStats.get("hp");
                                    int finalHealth = currHealth - damage > 0 ? currHealth - damage : 0;
                                    player.currPokemon.currentStats.put("hp", finalHealth);
                                    turnData.enemyAttack.damage = damage;
                                    if (finalHealth > 0) {
                                        damage = Battle.gen2CalcDamage(player.currPokemon, turnData.playerAttack, battle.oppPokemon);
                                        currHealth = battle.oppPokemon.currentStats.get("hp");
                                        finalHealth = currHealth - damage > 0 ? currHealth - damage : 0;
                                        battle.oppPokemon.currentStats.put("hp", finalHealth);
                                        turnData.playerAttack.damage = damage;
                                        if (finalHealth <= 0) {
                                            // TODO: this is some duplicate code with an if block above
                                            Route currRoute = game.map.tiles.get(player.position).routeBelongsTo;
                                            currRoute.pokemon.remove(battle.oppPokemon);
                                            currRoute.genPokemon(256);
                                            player.currPokemon.exp += battle.calcFaintExp(1);
                                            while (player.currPokemon.level < 100 && player.currPokemon.gen2CalcExpForLevel(player.currPokemon.level+1) <= player.currPokemon.exp) {
                                                player.currPokemon.level += 1;
                                            }
                                            // Check if pokemon evolves or not
                                            // TODO: handle when player cancels evolution
                                            for (int i=1; i <= player.currPokemon.level; i++) {
                                                if (Specie.gen2Evos.get(player.currPokemon.name.toLowerCase()).containsKey(String.valueOf(i))) {
                                                    String evolveTo = Specie.gen2Evos.get(player.currPokemon.name.toLowerCase()).get(String.valueOf(i));
                                                    player.currPokemon.evolveTo(evolveTo);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    else {
                                        battle.network.expectPlayerSwitch = true;
                                    }
                                }
                                // TODO: won't work for trainer battles
                                //  might be something like battle.oppPokemons or battle.pokemon
                                if (battle.oppPokemon.currentStats.get("hp") <= 0 || 
                                    (battleAction.type == Battle.DoTurn.Type.RUN && turnData.runSuccessful) ||
                                    (battleAction.type == Battle.DoTurn.Type.ITEM && turnData.numWobbles == -1)) {
                                    battle.oppPokemon.inBattle = false;
                                    player.canMove = true;
                                    player.numFlees = 0;
                                    // TODO: gameboy game handles this differently
                                    // TODO: remove
//                                    player.currPokemon = game.player.pokemon.get(0);
                                    game.battles.remove(player.network.id);
                                }
                                // If player out of pokemon, move them to last spawn location
                                // ie where they initially spawned, or where they last used a sleeping bag
                                boolean hasAlivePokemon = false;
                                for (Pokemon pokemon : player.pokemon) {
                                    if (pokemon.currentStats.get("hp") > 0) {
                                        hasAlivePokemon = true;
                                        break;
                                    }
                                }
                                if (!hasAlivePokemon) {
                                    battle.oppPokemon.inBattle = false;
                                    player.canMove = true;
                                    player.numFlees = 0;
                                    // TODO: gameboy game handles this differently
                                    // TODO: remove
//                                    player.currPokemon = game.player.pokemon.get(0);
                                    game.battles.remove(player.network.id);
                                    player.position.set(player.spawnLoc);
                                    // Restore hp to half
                                    for (Pokemon pokemon : player.pokemon) {
                                        pokemon.currentStats.put("hp", pokemon.maxStats.get("hp")/2);
                                    }
                                    // Send location update to relevant players
                                    for (Player otherPlayer : game.players.values()) {
                                        if (otherPlayer == player) {
                                            continue;
                                        }
                                        if (otherPlayer.network.loadingZone.contains(player.position)) {
                                            Network.ServerPlayerData serverPlayerData = new Network.ServerPlayerData(player);
                                            game.server.sendToTCP(otherPlayer.network.connectionId, serverPlayerData);
                                        }
                                    }
                                }

                                // debug code
    //                            try {
    //                                Thread.sleep(5000);
    //                            } catch (InterruptedException e) {
    //                                // TODO Auto-generated catch block
    //                                e.printStackTrace();
    //                            }
                                System.out.println("Sending turn data.");
                                game.server.sendToTCP(player.network.connectionId, turnData);
                            }
                            // A client is requesting to build this tile
                            else if (object instanceof Network.TileData) {
                                // TODO: player can theoretically build anywhere.
                                // TODO: need to check if player has enough wood/grass/etc to build this item.
                                // as it currently is, player can arbitrarily change tiles.
                                Network.TileData tileData = (Network.TileData) object;
                                if (!game.map.tiles.containsKey(tileData.pos)) {
                                    System.out.println("TileData: Invalid tile position " + tileData.pos.toString() + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                    throw new Exception();
                                }
                                if (game.map.tiles.get(tileData.pos).attrs.get("solid")) {
                                    throw new Exception();  // may be trying to build on a tile that was just built on.
                                }
                                for (Player player : game.players.values()) {
                                    if (player.position.equals(tileData.pos)) {
                                        throw new Exception();
                                    }
                                }
                                Tile oldTile = game.map.tiles.get(tileData.pos);
                                // preserve route from previous tile here.
                                Tile newTile = new Tile(tileData.tileName, tileData.tileNameUpper,
                                                        tileData.pos.cpy(), true, oldTile.routeBelongsTo);
                                newTile.hasItem = tileData.hasItem;
                                newTile.hasItemAmount = tileData.hasItemAmount;
                                game.map.tiles.put(tileData.pos.cpy(), newTile);
                                PlayerStanding.adjustSurroundingTiles(newTile);  // TODO: test
                                // Send the change to clients (including the one that requested the change.)
                                // Only send to clients that have this tile in their loading zone

                                // TODO: may have to use a HashMap<Vector2, Player> for performance
                                for (Player player : game.players.values()) {
//                                    if (tileData.pos.x <= player.network.loadingZoneTR.x &&
//                                        tileData.pos.x >= player.network.loadingZoneBL.x &&
//                                        tileData.pos.y <= player.network.loadingZoneTR.y &&
//                                        tileData.pos.y >= player.network.loadingZoneBL.y) {
                                    if (player.network.loadingZone.contains(tileData.pos)) {
                                        game.server.sendToTCP(player.network.connectionId, tileData);
                                    }
                                }
                            }
                            else if (object instanceof Network.UseHM) {
                                Network.UseHM useHM = (Network.UseHM) object;  // TODO: fix name
//                                System.out.println(useHM.hm);
                                if (!game.players.containsKey(useHM.playerId)) {
                                    System.out.println("UseHM: Invalid player id " + useHM.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                    throw new Exception();
                                }
                                Player player = game.players.get(useHM.playerId);
                                if (!player.pokemon.get(useHM.pokemonIndex).hms.contains(useHM.hm)) {
                                    System.out.println("UseHM: Invalid HM " + useHM.hm + " for index " + useHM.pokemonIndex + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                }
                                // Set appropriate flags for player
                                if (useHM.hm.equals("STOP")) {
                                    player.isBuilding = false;
                                    player.isCutting = false;
                                    player.isHeadbutting = false;
                                    player.isJumping = false;
                                }
                                else if (useHM.hm.equals("SWITCH")) {
                                    Pokemon movePokemon = player.pokemon.get(useHM.pokemonIndex);
                                    Pokemon movePokemon2 = player.pokemon.get(useHM.movePos);
                                    player.pokemon.remove(useHM.pokemonIndex);
                                    player.pokemon.add(useHM.pokemonIndex, movePokemon2);
                                    player.pokemon.remove(useHM.movePos);
                                    player.pokemon.add(useHM.movePos, movePokemon);
                                    // Replace currPokemon if applicable
                                    if (useHM.pokemonIndex == 0) {
                                        player.currPokemon = movePokemon2;
                                    }
                                    if (useHM.movePos == 0) {
                                        player.currPokemon = movePokemon;
                                    }
                                }
                                else if (useHM.hm.equals("BUILD")) {
//                                    player.isBuilding = true;
//                                    player.isCutting = false;
//                                    player.isHeadbutting = false;
//                                    player.isJumping = false;
                                }
                                else if (useHM.hm.equals("CUT")) {
                                    Vector2 pos = player.facingPos(useHM.dirFacing);
                                    Tile currTile = player.network.tiles.get(pos);
                                    if (currTile.attrs.containsKey("cuttable") && currTile.attrs.get("cuttable")) {
                                        Action action = new CutTreeAnim(game, game.map.overworldTiles.get(pos), null);
                                        game.map.interiorTiles.get(game.map.interiorTilesIndex).remove(currTile.position.cpy());
                                        // Get items from tile
                                        if (!currTile.items().isEmpty()) {
                                            for (String item : currTile.items().keySet()) {
                                                if (player.itemsDict.containsKey(item)) {
                                                    int currQuantity = player.itemsDict.get(item);
                                                    player.itemsDict.put(item, currQuantity+currTile.items().get(item));
                                                }
                                                else {
                                                    player.itemsDict.put(item, currTile.items().get(item));
                                                }
                                            }
                                            currTile.items().clear();
                                        }
                                        game.insertAction(action);
                                    }
                                }
                                else if (useHM.hm.equals("HEADBUTT")) {
                                    player.isHeadbutting = true;
                                    player.isBuilding = false;
                                    player.isCutting = false;
                                    player.isJumping = false;
                                }
                                else if (useHM.hm.equals("JUMP")) {
                                    player.isHeadbutting = false;
                                    player.isBuilding = false;
                                    player.isCutting = false;
                                    player.isJumping = true;
                                }
                                for (Player otherPlayer : game.players.values()) {
                                    if (player == otherPlayer) {
                                        continue;
                                    }
                                    if (otherPlayer.network.loadingZone.contains(player.position)) {
                                        useHM.playerId = player.network.number;
                                        game.server.sendToTCP(otherPlayer.network.connectionId, useHM);
                                    }
                                }
                            }
                            else if (object instanceof Network.UseItem) {
                                Network.UseItem useItem = (Network.UseItem) object;
                                if (!game.players.containsKey(useItem.playerId)) {
                                    System.out.println("UseItem: Invalid player id " + useItem.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                    throw new Exception();
                                }
                                Player player = game.players.get(useItem.playerId);
                                if (!player.itemsDict.containsKey(useItem.item)) {
                                    System.out.println("UseItem: None of this item in inventory: " + useItem.item + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                    throw new Exception();
                                }
                                Vector2 pos = player.facingPos(useItem.dirFacing);
                                if (useItem.item.contains("apricorn")) {
                                    game.insertAction(new PlantTree(pos, null));
                                }
                                // Deduct from inventory
                                player.itemsDict.put(useItem.item, player.itemsDict.get(useItem.item)-1);
                                if (player.itemsDict.get(useItem.item) <= 0) {
                                    player.itemsDict.remove(useItem.item);
                                }
                                for (Player otherPlayer : game.players.values()) {
                                    if (player == otherPlayer) {
                                        continue;
                                    }
                                    if (otherPlayer.network.loadingZone.contains(player.position)) {
                                        useItem.playerId = player.network.number;
                                        game.server.sendToTCP(otherPlayer.network.connectionId, useItem);
                                    }
                                }
                            }
                            else if (object instanceof Network.Sleep) {
                                Network.Sleep sleep = (Network.Sleep) object;
                                if (!game.players.containsKey(sleep.playerId)) {
                                    System.out.println("UseHM: Invalid player id " + sleep.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                    throw new Exception();
                                }
                                Player player = game.players.get(sleep.playerId);
                                player.isSleeping = sleep.isSleeping;
                                player.canMove = !sleep.isSleeping;
                                if (!player.isSleeping) {
                                    player.spawnLoc = player.position.cpy();
                                    // Sync pokemon health with remote player
                                    int i = 0;
                                    for (Pokemon pokemon : player.pokemon) {
//                                        System.out.println(pokemon.name);  // TODO: remove
//                                        System.out.println(pokemon.name);  
                                        Network.PokemonData pokemonData = new Network.PokemonData(pokemon, i);
                                        connection.sendTCP(pokemonData);
                                        i++;
                                    }
                                }
                            }
                            else if (object instanceof Network.Craft) {
                                // TODO: test if inventory is really updated.
                                Network.Craft craft = (Network.Craft) object;
                                if (!game.players.containsKey(craft.playerId)) {
                                    System.out.println("Craft: Invalid player id " + craft.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                    throw new Exception();
                                }
                                Player player = game.players.get(craft.playerId);
                                // check requirements
                                if (player.hasCraftRequirements(Player.crafts, craft.craftIndex, craft.amount)) {
                                    // if passed, update inventory
                                    player.craftItem(Player.crafts, craft.craftIndex, craft.amount);
                                }
//                                // TODO: debug, delete
//                                for (String item : player.itemsDict.keySet()) {
//                                    System.out.println(player.itemsDict.get(item).toString() + " " + item);
//                                }
                            }
                            else if (object instanceof Network.DropItem) {
                                // TODO: test if inventory is really updated.
                                Network.DropItem dropItem = (Network.DropItem) object;
                                if (!game.players.containsKey(dropItem.playerId)) {
                                    System.out.println("DropItem: Invalid player id " + dropItem.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                    throw new Exception();
                                }
                                Player player = game.players.get(dropItem.playerId);
                                // deduct from inventory. if player has 0 or less, remove.
                                player.itemsDict.put(dropItem.itemName, player.itemsDict.get(dropItem.itemName)-dropItem.amount);
                                if (player.itemsDict.get(dropItem.itemName) <= 0) {
                                    player.itemsDict.remove(dropItem.itemName);
                                }
//                              // TODO: debug, delete
//                                for (String item : player.itemsDict.keySet()) {
//                                    System.out.println(player.itemsDict.get(item).toString() + " " + item);
//                                }
                            }
                            else if (object instanceof Network.DropPokemon) {
                                Network.DropPokemon dropPokemon = (Network.DropPokemon) object;
                                if (!game.players.containsKey(dropPokemon.playerId)) {
                                    System.out.println("DropPokemon: Invalid player id " + dropPokemon.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                    throw new Exception();
                                }
                                Player player = game.players.get(dropPokemon.playerId);
                                if (dropPokemon.pickingUp) {
                                    Vector2 pos = dropPokemon.pos;
                                    Pokemon pokemon = game.map.pokemon.get(pos);
                                    // Make sure pokemon exists and is in range of player before picking up.
                                    if (player.pokemon.size() < 6 && pokemon != null &&
                                        player.network.loadingZone.contains(pos)) {
                                            player.pokemon.add(pokemon);
                                            game.map.pokemon.remove(pos);
                                            game.actionStack.remove(pokemon.standingAction);
                                    }
                                    // For each other player, remove the overworld pokemon
                                    for (Player otherPlayer : game.players.values()) {
                                        if (otherPlayer == player) {
                                            continue;
                                        }
                                        if (player.network.loadingZone.contains(pos)) {
                                            game.server.sendToTCP(otherPlayer.network.connectionId, new Network.OverworldPokemonData(pokemon, pos, true));
                                        }
                                    }
                                }
                                else {
                                    Vector2 pos = player.facingPos(dropPokemon.dirFacing);
                                    Pokemon pokemon = player.pokemon.get(dropPokemon.index);
                                    pokemon.position = pos;
                                    player.pokemon.remove(dropPokemon.index);
                                    if (dropPokemon.index == 0) {
                                        for (Pokemon currPokemon : player.pokemon) {
                                            if (currPokemon.currentStats.get("hp") > 0) {
                                                player.currPokemon = currPokemon;
                                                break;
                                            }
                                        }
                                    }
                                    pokemon.mapTiles = player.network.tiles;
                                    pokemon.canMove = true;
                                    game.insertAction(pokemon.new Standing());

                                    // For each other player, add the overworld pokemon
                                    for (Player otherPlayer : game.players.values()) {
                                        if (otherPlayer == player) {
                                            continue;
                                        }
                                        if (player.network.loadingZone.contains(pos)) {
                                            game.server.sendToTCP(otherPlayer.network.connectionId, new Network.OverworldPokemonData(pokemon, pos));
                                        }
                                    }
                                }
                            }
                            else if (object instanceof Network.PausePokemon) {
                                Network.PausePokemon pausePokemon = (Network.PausePokemon) object;
                                if (!game.players.containsKey(pausePokemon.playerId)) {
                                    System.out.println("DropPokemon: Invalid player id " + pausePokemon.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                    throw new Exception();
                                }
                                Player player = game.players.get(pausePokemon.playerId);
                                if (player.network.loadingZone.contains(pausePokemon.position) && 
                                        game.map.pokemon.containsKey(pausePokemon.position)) {
                                    Pokemon pokemon = game.map.pokemon.get(pausePokemon.position);
                                    pokemon.canMove = !pausePokemon.shouldPause;
                                }
                                if (pausePokemon.shouldPause) {
                                    connection.sendTCP(pausePokemon);
                                }
                            }
                            // TODO: there is an issue here if two players try to pick up at same time.
                            else if (object instanceof Network.PickupItem) {
                                Network.PickupItem pickupItem = (Network.PickupItem) object;
                                if (!game.players.containsKey(pickupItem.playerId)) {
                                    System.out.println("PickupItem: Invalid player id " + pickupItem.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                    throw new Exception();
                                }
                                Player player = game.players.get(pickupItem.playerId);
                                Vector2 pos = new Vector2(0,0);
                                if (pickupItem.dirFacing.equals("right")) {
                                    pos = new Vector2(player.position.cpy().add(16,0));
                                }
                                else if (pickupItem.dirFacing.equals("left")) {
                                    pos = new Vector2(player.position.cpy().add(-16,0));
                                }
                                else if (pickupItem.dirFacing.equals("up")) {
                                    pos = new Vector2(player.position.cpy().add(0,16));
                                }
                                else if (pickupItem.dirFacing.equals("down")) {
                                    pos = new Vector2(player.position.cpy().add(0,-16));
                                }
                                Tile tile = game.map.tiles.get(pos);
                                // tile can == null here b/c player position on server-side
                                // might still be transitioning between tiles.
                                if (tile != null && tile.hasItem != null) {
                                    tile.pickUpItem(player);
                                    // confirm to the client that item was picked up
                                    pickupItem.pos = pos;
                                    connection.sendTCP(pickupItem);
                                    // Update the tile for everyone.
                                    Network.TileData tileData = new Network.TileData(tile);
                                    for (Player otherPlayer : game.players.values()) {
                                        if (player == otherPlayer) {
                                            continue;
                                        }
                                        // TODO: remove
//                                        if (tileData.pos.x <= player.network.loadingZoneTR.x &&
//                                            tileData.pos.x >= player.network.loadingZoneBL.x &&
//                                            tileData.pos.y <= player.network.loadingZoneTR.y &&
//                                            tileData.pos.y >= player.network.loadingZoneBL.y) {
                                        if (player.network.loadingZone.contains(tileData.pos)) {
                                            game.server.sendToTCP(player.network.connectionId, tileData);
                                        }
                                    }
                                }
//                              // TODO: debug, delete
//                                for (String item : player.itemsDict.keySet()) {
//                                    System.out.println(player.itemsDict.get(item).toString() + " " + item);
//                                }
                            }
                            else if (object instanceof Network.LearnMove) {
                                // Check if move is in current level learnmoves, or less.
                                Network.LearnMove learnMove = (Network.LearnMove) object;
                                if (!game.players.containsKey(learnMove.playerId)) {
                                    System.out.println("DropItem: Invalid player id " + learnMove.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                    throw new Exception();
                                }
                                Player player = game.players.get(learnMove.playerId);
                                Pokemon pokemon = player.pokemon.get(learnMove.pokemonIndex);
                                learnMove.moveName = learnMove.moveName.toLowerCase();
                                boolean foundMove = false;
                                for (int i = 0; i <= pokemon.level; i++) {
                                    if (!pokemon.learnSet.containsKey(i)) {
                                        continue;
                                    }
                                    for (String attack : pokemon.learnSet.get(i)) {
                                        if (attack.toLowerCase().equals(learnMove.moveName)) {
                                            foundMove = true;
                                            break;
                                        }
                                    }
                                    if (foundMove) {
                                        break;
                                    }
                                }
                                if (foundMove) {
                                    pokemon.attacks[learnMove.replaceIndex] = learnMove.moveName;
                                }
////                              // TODO: debug, delete
//                                for (int i = 0; i < 4; i++) {
//                                    System.out.println(String.valueOf(pokemon.attacks[i]));
//                                }
                            }
                            // TODO: temporarily being used for ghost encounter
                            // ideally, need to handle ghost spawning from the server side.
                            else if (object instanceof Network.BattleData) {
                                Network.BattleData battleData = (Network.BattleData) object;
                                if (!battleData.pokemonData.name.toLowerCase().equals("ghost")) {
                                    System.out.println("BattleData: Invalid encounter for " + battleData.pokemonData.name + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                    throw new Exception();
                                }
                                if (!game.players.containsKey(battleData.playerId)) {
                                    System.out.println("BattleData: Invalid player id " + battleData.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                    throw new Exception();
                                }
                                Player player = game.players.get(battleData.playerId);
//                                System.out.println(battleData.pokemonData.name);
//                                System.out.println(battleData.playerId);
                                // TODO: remove
                                // The first Pokemon the player sends out in battle should
                                // have >0 hp.
//                                for (Pokemon currPokemon : player.pokemon) {
//                                    if (currPokemon.currentStats.get("hp") > 0) {
//                                        player.currPokemon = currPokemon;
//                                        break;
//                                    }
//                                }
                                player.setCurrPokemon();
                                player.canMove = false;
                                game.battles.put(player.network.id, new Battle());
                                game.battles.get(player.network.id).oppPokemon = new Pokemon(battleData.pokemonData);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        synchronized (this) {
                            this.notify();
                        }
                    }
                };
                Gdx.app.postRunnable(runnable);
                try {
                    synchronized (runnable) {
                        runnable.wait();
                    }
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    public int getLayer(){return this.layer;}
}

