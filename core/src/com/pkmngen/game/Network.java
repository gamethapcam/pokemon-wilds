package com.pkmngen.game;

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

/**
 * Adds Listener to game.client to receive and handle incoming server
 * connections.
 */
class ClientBroadcast extends Action {
//    HeroPlayer player;
    int playerIndex;

    int timeStep = 0;

    public int layer = 1;
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

                            if (object instanceof Network.RelocatePlayer) {
                                Network.RelocatePlayer relocatePlayer = (Network.RelocatePlayer) object;
                                game.player.position = relocatePlayer.position.cpy();
                                game.cam.position.set(relocatePlayer.position.cpy().add(16,0), 0f);
                            }

                            if (object instanceof Network.MapTiles) {
                                Network.MapTiles mapTiles = (Network.MapTiles) object;
                                for (Network.TileData tileData : mapTiles.tiles) {
                                    // TODO: remove
//                                    Tile tile = new Tile(tileData.tileName, tileData.tileNameUpper, tileData.pos.cpy(), true, null);
                                    Tile tile = Tile.get(tileData, null);
                                    game.map.tiles.put(tileData.pos.cpy(), tile);
                                }
                                if (mapTiles.timeOfDay != null) {
                                    CycleDayNight.dayTimer = mapTiles.dayTimer;
                                    game.map.timeOfDay = mapTiles.timeOfDay;
                                }
                            }

                            if (object instanceof Network.ServerPlayerData) {
                                Network.ServerPlayerData serverPlayerData = (Network.ServerPlayerData) object;
                                Player player = new Player();
                                player.name = serverPlayerData.name;
                                player.position = serverPlayerData.position.cpy();
                                player.type = Player.Type.REMOTE;
                                player.network.id = serverPlayerData.number;  // on client side, just index player by number
                                player.setColor(serverPlayerData.color);
                                game.players.put(serverPlayerData.number, player);
                                player.standingAction = new PlayerStanding(game, player, false, true);
                                game.insertAction(player.standingAction);
                            }

                            // server is notifying client that a player has moved
                            if (object instanceof Network.MovePlayer) {
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

                            // server is notifying client that the local player has entered a battle
                            if (object instanceof Network.BattleData) {
                                Network.BattleData battleData = (Network.BattleData) object;
                                game.player.network.doEncounter = battleData;
                            }

                            // server is updating client with the result of a battle turn
                            if (object instanceof Network.BattleTurnData) {
                                Network.BattleTurnData turnData = (Network.BattleTurnData) object;
                                game.battle.network.turnData = turnData;
                                System.out.println("Recieved turn data.");
                            }

                            // A server is changing a tile (ie player built or cut something within this player's loading zone)
                            if (object instanceof Network.TileData) {
                                Network.TileData tileData = (Network.TileData) object;
                                Tile newTile = new Tile(tileData.tileName, tileData.tileNameUpper,
                                                        tileData.pos.cpy(), true, null);
                                newTile.hasItem = tileData.hasItem;
                                newTile.hasItemAmount = tileData.hasItemAmount;
                                game.map.tiles.put(tileData.pos.cpy(), newTile);
                                PlayerStanding.adjustSurroundingTiles(newTile);
                            }
                            if (object instanceof Network.PokemonData) {
                                Network.PokemonData pokemonData = (Network.PokemonData) object;
                                game.player.pokemon.set(pokemonData.index, new Pokemon(pokemonData));
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
        kryo.register(PlayerData.class);
        kryo.register(PokemonData.class);
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
        kryo.register(Sleep.class);
        kryo.register(Craft.class);
        kryo.register(DropItem.class);
        kryo.register(SaveData.class);
    }

    static public class BattleData {
        PokemonData pokemonData;

        public BattleData(){}

        public BattleData(Pokemon pokemon) {
            this.pokemonData = new PokemonData(pokemon);
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
        Battle.DoTurn.Type type = Battle.DoTurn.Type.ATTACK;

        public DoBattleAction(){}

        public DoBattleAction(String playerId, Battle.DoTurn.Type type, String action) {
            this.playerId = playerId;
            this.type = type;
            this.attack = action;
            this.itemName = action;
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

    static public class Login {
        public String playerId = "";
        Color color;

        public Login(){}

        public Login(String playerId, Color color) {
            this.playerId = playerId;
            this.color = color;
        }
    }

    static public class MapTiles {
        public ArrayList<TileData> tiles = new ArrayList<TileData>();
        // store routes as classId->Route
        public HashMap<String, RouteData> routes = new HashMap<String, RouteData>();
        public ArrayList<Vector2> edges = new ArrayList<Vector2>();
        
        public ArrayList<HashMap<Vector2, TileData>> interiorTiles = new ArrayList<HashMap<Vector2, TileData>>();
        int interiorTilesIndex;

        // used to sync time with server
        String timeOfDay;
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
     * Data associated with the Player to be sent over the network.
     *
     * Used to initialize a Player instance.
     */
    static public class PlayerData {
        public Vector2 position;
        public String name;
        ArrayList<PokemonData> pokemon;
        PokemonData currPokemon;
        Map<String, Integer> itemsDict;
        String id;
        String number;
        Color color;
        String dirFacing;
        public Vector2 spawnLoc;
        boolean isInterior;
        boolean displayedMaxPartyText;

        public PlayerData(){}

        public PlayerData(Player player) {
            this.position = player.position.cpy();
            this.position.x = this.position.x - (this.position.x % 16);
            this.position.y = this.position.y - (this.position.y % 16);
            this.name = player.name;
            this.pokemon = new ArrayList<PokemonData>();
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
        }
    }

    static public class PokemonData {
        String name;
        int level;
        Pokemon.Generation generation;
        int hp;
        String[] attacks = new String[4];
        int index;  // index in player inventory
        
        // overworld-related
        Vector2 position;

        public PokemonData() {}

        public PokemonData(Pokemon pokemon) {
            this.name = pokemon.name;
            this.level = pokemon.level;
            this.generation = pokemon.generation;
            this.hp = pokemon.currentStats.get("hp");
            this.attacks[0] = pokemon.attacks[0];
            this.attacks[1] = pokemon.attacks[1];
            this.attacks[2] = pokemon.attacks[2];
            this.attacks[3] = pokemon.attacks[3];
            // TODO: psn, para etc status

            this.position = pokemon.position;
        }

        public PokemonData(Pokemon pokemon, int index) {
            this(pokemon);
            this.index = index;
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
        ArrayList<PokemonData> pokemon = new ArrayList<PokemonData>();
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
        ArrayList<Network.PlayerData> players = new ArrayList<Network.PlayerData>();
        Network.PlayerData playerData;
        HashMap<Vector2, PokemonData> overworldPokemon = new HashMap<Vector2, PokemonData>();

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

    static public class ServerPlayerData {
        public Vector2 position;
        public String name;
        String number;
        Color color;

        public ServerPlayerData(){}

        public ServerPlayerData(Player player) {
            this.position = player.position;
            this.name = player.name;
            this.number = player.network.number;
            this.color = player.color;
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

        // TrainerTipsTile stuff
        boolean isUnown;
        String message = "";

        public TileData(){}

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
        }
    }

    /**
     * Sent client->server to notify that the player has used an HM.
     */
    static public class UseHM {
        String playerId;
        int pokemonIndex;
        String hm;

        public UseHM(){}

        public UseHM(String playerId, int pokemonIndex, String hm) {
            this.playerId = playerId;
            this.pokemonIndex = pokemonIndex;
            this.hm = hm;
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
        // register server listeners
        game.server.addListener(new Listener() {
            public void received(final Connection connection, final Object object) {
                // debug code
//                try {
//                    Thread.sleep(game.map.rand.nextInt(400));
//                } catch (InterruptedException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//                if (object instanceof String) {
//                    String getData = (String) object;
//                }
                
                // annoying, but need to handle the received object in the Gdx thread
                // because only Gdx thread can make OpenGL calls
                Runnable runnable = new Runnable() {
                    public void run() {
                        try {
                            // client is notifying server that it wants to load map tiles
                            if (object instanceof Network.Login) {
                                Network.Login login = (Network.Login) object;
                                Network.MapTiles mapTiles = new Network.MapTiles();

                                // If player doesn't exist, add them to game.players
                                // TODO: obviously need diff handling, need to require password
                                if (!game.players.containsKey(login.playerId)) {
                                    final String playerId = login.playerId;
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

                                    // TODO: Debug, remove
                                    // TODO: A bunch of animations and code required for having no pokemon.
                                    player.currPokemon = new Pokemon("machop", 4, Pokemon.Generation.CRYSTAL);
//                                    player.currPokemon.attacks[0] = "Ice Beam";
//                                    player.currPokemon.attacks[1] = "Hydro Pump";
                                    player.pokemon.add(player.currPokemon);
                                    player.currPokemon.currentStats.put("hp", 2); // TODO: debug, remove
//                                    Pokemon pokemon = new Pokemon("stantler", 50, Pokemon.Generation.CRYSTAL);
//                                    player.pokemon.add(pokemon);
                                    // ---
                                    game.players.put(playerId, player);
                                }
                                Player player = game.players.get(login.playerId);
                                player.network.connectionId = connection.getID();
                                // send over player data
                                Network.PlayerData playerData = new Network.PlayerData(player);
                                connection.sendTCP(playerData);

                                // Add standing action to actionStack
                                if (player.standingAction == null) {
                                    player.standingAction = new PlayerStanding(game, player, false, true);
                                }
                                if (!game.actionStack.contains(player.standingAction)) {
                                    game.insertAction(player.standingAction);
                                }

                                // get map.tiles in square around player and send them back
                                for (Vector2 position = player.network.loadingZoneBL.cpy();
                                     position.y < player.network.loadingZoneTR.y; position.add(16, 0)) {
                                    Tile tile = game.map.tiles.get(position);
                                    if (tile == null) {
                                        continue;
                                    }
                                    mapTiles.tiles.add(new Network.TileData(tile));
                                    if (position.x >= player.network.loadingZoneTR.x) {
                                        position.add(0, 16);
                                        position.x = player.network.loadingZoneBL.x;
                                    }
                                    // how many can we send without hitting buffer limit?
                                    //                              if (mapTiles.tiles.size() >= 16) {
                                    if (mapTiles.tiles.size() >= 32) {
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
                                    if (otherPlayer.position.x <= player.network.loadingZoneTR.x &&
                                            otherPlayer.position.x >= player.network.loadingZoneBL.x &&
                                            otherPlayer.position.y <= player.network.loadingZoneTR.y &&
                                            otherPlayer.position.y >= player.network.loadingZoneBL.y) {
                                        Network.ServerPlayerData serverPlayerData = new Network.ServerPlayerData(otherPlayer);
                                        connection.sendTCP(serverPlayerData);
                                        serverPlayerData = new Network.ServerPlayerData(player);
                                        game.server.sendToTCP(otherPlayer.network.connectionId, serverPlayerData);

                                        System.out.println("Sent FROMSERVER player: " + serverPlayerData.number);
                                    }
                                }
                            }

                            // Client is notifying server that player has moved
                            if (object instanceof Network.MovePlayer) {
                                Network.MovePlayer movePlayer = (Network.MovePlayer) object;
                                if (!game.players.containsKey(movePlayer.playerId)) {
                                    System.out.println("MovePlayer: Invalid player ID " + movePlayer.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                    throw new Exception();
                                }
                                Player player = game.players.get(movePlayer.playerId);
                                player.network.shouldMove = true;
                                player.network.dirFacing = movePlayer.dirFacing;
                                player.network.isRunning = movePlayer.isRunning;
                                // send movement to all other clients if it's in that client's loading zone
                                for (Player p : game.players.values()) {
                                    if (p == player) {
                                        continue;
                                    }
                                    if (player.position.x <= p.network.loadingZoneTR.x &&
                                        player.position.x >= p.network.loadingZoneBL.x &&
                                        player.position.y <= p.network.loadingZoneTR.y &&
                                        player.position.y >= p.network.loadingZoneBL.y) {
                                        game.server.sendToTCP(p.network.connectionId,
                                                              new Network.MovePlayer(player.network.number,
                                                                                     player.network.dirFacing,
                                                                                     player.network.isRunning));
                                    }
                                }
                            }

                            // client is notifying server that it's doing a battle action (fight, run, item, switch)
                            if (object instanceof Network.DoBattleAction) {
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

                                // TODO: don't determine player attack outcome if run, item, or switch.

                                // Apply the effects of the attack, and send back to client
                                // TODO: calculations here for hit/miss, crit, effect hit, etc.
                                Network.BattleTurnData turnData = new Network.BattleTurnData();
                                if (battleAction.type == Battle.DoTurn.Type.ITEM) {
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
                                // determine attack outcome
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
                                // decide enemy attack choice
                                String attackChoice = battle.oppPokemon.attacks[game.map.rand.nextInt(battle.oppPokemon.attacks.length)];
                                if (attackChoice.equals("-")) {
                                    attackChoice = "Struggle";
                                }

                                // update enemy pokemon locally
                                turnData.enemyAttack = battle.attacks.get(attackChoice.toLowerCase());
                                if (!turnData.oppFirst) {
                                    int finalHealth = 0;
                                    if (battleAction.type == Battle.DoTurn.Type.ATTACK) {
                                        int damage = Battle.calcDamage(player.currPokemon, turnData.playerAttack, battle.oppPokemon);
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
                                            player.currPokemon.exp += battle.calcFaintExp();
                                            while (player.currPokemon.level < 100 && player.currPokemon.gen2CalcExpForLevel(player.currPokemon.level+1) <= player.currPokemon.exp) {
                                                player.currPokemon.level += 1;
                                            }
                                            // Check if pokemon evolves or not
                                            // TODO: handle when player cancels evolution
                                            for (int i=1; i <= player.currPokemon.level; i++) {
                                                if (Pokemon.gen2Evos.get(player.currPokemon.name.toLowerCase()).containsKey(String.valueOf(i))) {
                                                    String evolveTo = Pokemon.gen2Evos.get(player.currPokemon.name.toLowerCase()).get(String.valueOf(i));
                                                    player.currPokemon.evolveTo(evolveTo);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    // If enemy is still alive after attack, or player failed to run, or player failed
                                    // to catch pokemon, then enemy pokemon attacks player pokemon.
                                    if (finalHealth > 0 || 
                                        (battleAction.type == Battle.DoTurn.Type.RUN && !turnData.runSuccessful) ||
                                        (battleAction.type == Battle.DoTurn.Type.ITEM && turnData.numWobbles != -1)) {
                                        int damage = Battle.calcDamage(battle.oppPokemon, turnData.enemyAttack, player.currPokemon);
                                        int currHealth = player.currPokemon.currentStats.get("hp");
                                        finalHealth = currHealth - damage > 0 ? currHealth - damage : 0;
                                        player.currPokemon.currentStats.put("hp", finalHealth);
                                        turnData.enemyAttack.damage = damage;
                                    }
                                }
                                else {
                                    int damage = Battle.calcDamage(battle.oppPokemon, turnData.enemyAttack, player.currPokemon);
                                    int currHealth = player.currPokemon.currentStats.get("hp");
                                    int finalHealth = currHealth - damage > 0 ? currHealth - damage : 0;
                                    player.currPokemon.currentStats.put("hp", finalHealth);
                                    turnData.enemyAttack.damage = damage;
                                    if (finalHealth > 0) {
                                        damage = Battle.calcDamage(player.currPokemon, turnData.playerAttack, battle.oppPokemon);
                                        currHealth = battle.oppPokemon.currentStats.get("hp");
                                        finalHealth = currHealth - damage > 0 ? currHealth - damage : 0;
                                        battle.oppPokemon.currentStats.put("hp", finalHealth);
                                        turnData.playerAttack.damage = damage;
                                        if (finalHealth <= 0) {
                                            // TODO: this is some duplicate code with an if block above
                                            Route currRoute = game.map.tiles.get(player.position).routeBelongsTo;
                                            currRoute.pokemon.remove(battle.oppPokemon);
                                            currRoute.genPokemon(256);
                                            player.currPokemon.exp += battle.calcFaintExp();
                                            while (player.currPokemon.level < 100 && player.currPokemon.gen2CalcExpForLevel(player.currPokemon.level+1) <= player.currPokemon.exp) {
                                                player.currPokemon.level += 1;
                                            }
                                            // Check if pokemon evolves or not
                                            // TODO: handle when player cancels evolution
                                            for (int i=1; i <= player.currPokemon.level; i++) {
                                                if (Pokemon.gen2Evos.get(player.currPokemon.name.toLowerCase()).containsKey(String.valueOf(i))) {
                                                    String evolveTo = Pokemon.gen2Evos.get(player.currPokemon.name.toLowerCase()).get(String.valueOf(i));
                                                    player.currPokemon.evolveTo(evolveTo);
                                                    break;
                                                }
                                            }
                                        }
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
                                    player.position.set(player.spawnLoc);
                                    // Restore hp to half
                                    for (Pokemon pokemon : player.pokemon) {
                                        pokemon.currentStats.put("hp", pokemon.maxStats.get("hp")/2);
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
                            // a client is requesting to change this tile
                            if (object instanceof Network.TileData) {
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
                                // Send the change to clients (including the one that requested the change.)
                                // Only send to clients that have this tile in their loading zone

                                // TODO: may have to use a HashMap<Vector2, Player> for performance
                                for (Player player : game.players.values()) {
                                    if (tileData.pos.x <= player.network.loadingZoneTR.x &&
                                        tileData.pos.x >= player.network.loadingZoneBL.x &&
                                        tileData.pos.y <= player.network.loadingZoneTR.y &&
                                        tileData.pos.y >= player.network.loadingZoneBL.y) {
                                        game.server.sendToTCP(player.network.connectionId, tileData);
                                    }
                                }
                            }
                            if (object instanceof Network.UseHM) {
                                Network.UseHM useHM = (Network.UseHM) object;  // TODO: fix name
                                System.out.println(useHM.hm);
                                if (!game.players.containsKey(useHM.playerId)) {
                                    System.out.println("UseHM: Invalid player id " + useHM.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                    throw new Exception();
                                }
                                Player player = game.players.get(useHM.playerId);
                                // TODO: probably should handle switch action here. maybe.
                                if (useHM.hm.equals("STATS") || useHM.hm.equals("SWITCH")) {
                                    throw new Exception();
                                }
                                if (useHM.hm.equals("STOP")) {
                                    player.isBuilding = false;
                                    player.isCutting = false;
                                    player.isHeadbutting = false;
                                    player.isJumping = false;
                                    throw new Exception();
                                }
                                if (!player.pokemon.get(useHM.pokemonIndex).hms.contains(useHM.hm)) {
                                    System.out.println("UseHM: Invalid HM " + useHM.hm + " for index " + useHM.pokemonIndex + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                    throw new Exception();
                                }
                                // Set appropriate flags for player
                                if (useHM.hm.equals("BUILD")) {
                                    player.isBuilding = true;
                                    player.isCutting = false;
                                    player.isHeadbutting = false;
                                    player.isJumping = false;
                                }
                                else if (useHM.hm.equals("CUT")) {
                                    player.isCutting = true;
                                    player.isBuilding = false;
                                    player.isHeadbutting = false;
                                    player.isJumping = false;
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
                            }
                            if (object instanceof Network.Sleep) {
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
                                    // sync pokemon health with remote player
                                    for (Pokemon pokemon : player.pokemon) {
                                        Network.PokemonData pokemonData = new Network.PokemonData(pokemon);
                                        connection.sendTCP(pokemonData);
                                    }
                                }
                            }
                            if (object instanceof Network.Craft) {
                                // TODO: test if inventory is really updated.
                                Network.Craft craft = (Network.Craft) object;
                                if (!game.players.containsKey(craft.playerId)) {
                                    System.out.println("Craft: Invalid player id " + craft.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                    throw new Exception();
                                }
                                Player player = game.players.get(craft.playerId);
                                // check requirements
                                if (player.hasCraftRequirements(craft.craftIndex, craft.amount)) {
                                    // if passed, update inventory
                                    player.craftItem(craft.craftIndex, craft.amount);
                                }
//                                // TODO: debug, delete
//                                for (String item : player.itemsDict.keySet()) {
//                                    System.out.println(player.itemsDict.get(item).toString() + " " + item);
//                                }
                            }
                            if (object instanceof Network.DropItem) {
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
                                for (String item : player.itemsDict.keySet()) {
                                    System.out.println(player.itemsDict.get(item).toString() + " " + item);
                                }
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

