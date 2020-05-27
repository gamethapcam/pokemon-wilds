package com.pkmngen.game;

import java.awt.List;
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
import com.oracle.jrockit.jfr.InvalidValueException;
import com.pkmngen.game.DrawPokemonMenu.SelectedMenu;
import com.pkmngen.game.Game.CharacterConnection;
import com.pkmngen.game.Pokemon.Generation;

public class Network {

    static public final int port = 54555;

    // This registers objects that are going to be sent over the network.
    static public void register (EndPoint endPoint) {
        Kryo kryo = endPoint.getKryo();
        kryo.register(UpdatePlayer.class);
        kryo.register(UpdateFireball.class);
        kryo.register(UpdateGhostData.class);
        kryo.register(AllGhosts.class);
        kryo.register(UpdateScore.class);
        
        kryo.register(com.badlogic.gdx.math.Vector2.class);
        kryo.register(com.badlogic.gdx.physics.box2d.Filter.class);
        kryo.register(java.util.ArrayList.class);
        kryo.register(java.util.HashMap.class);
        kryo.register(String[].class);
        kryo.register(Color.class);
        
        //
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
    }
    

    static public class UpdatePlayer {
        public String dirFacing;
        public Vector2 position;
        
        public int health;
//      public boolean isDead; //TODO - remove
        
        public int damageCooldown;
        public int swordSwingCooldown;
        public Vector2 swordPos;
//        public Filter swordFilter;
    }

    static public class UpdateFireball {
        public Vector2 position;
    }
    
    static public class UpdateGhostData {
        public Vector2 position;
        public String dirFacing;
        public int id;
        public int target;
        
        public int damageCooldown;
        public int isDying;
        public int attackingCooldown;
        public int damageBoxCooldown;
    }

    static public class UpdateScore {
        public int player;
        public int score;
    }
    
    static public class AllGhosts {
        public ArrayList<UpdateGhostData> ghosts;
    }
    


    
    static public class PokemonData {
        String name;
        int level;
        Pokemon.Generation generation;
        int hp;
        String[] attacks = new String[4];

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
        }
    }
    
    /*
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

    static public class Login {
        public String playerId = "";
        Color color;

        public Login(){}

        public Login(String playerId, Color color) {
            this.playerId = playerId;
            this.color = color;
        }
    }
    
    static public class TileData {
        public Vector2 pos;
        public String tileName;
        public String tileNameUpper;
        String routeBelongsTo;  // this is a string of the route's class id

        public TileData(){}

        public TileData(Tile tile) {
            this.pos = tile.position.cpy();
            this.tileName = tile.name;
            this.tileNameUpper = tile.nameUpper;
            if (tile.routeBelongsTo != null) {
                this.routeBelongsTo = tile.routeBelongsTo.toString();
            }
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

    static public class MapTiles {
        public ArrayList<TileData> tiles = new ArrayList<TileData>();
        // store routes as classId->Route
        public HashMap<String, RouteData> routes = new HashMap<String, RouteData>();
        public ArrayList<Vector2> edges = new ArrayList<Vector2>();
        
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
    
    static public class BattleData {
        PokemonData pokemonData;

        public BattleData(){}

        public BattleData(Pokemon pokemon) {
            this.pokemonData = new PokemonData(pokemon);
        }
    }
    
    static public class DoBattleAction {
        String playerId;
        String attack;
        Battle.DoTurn.Type type = Battle.DoTurn.Type.ATTACK;

        public DoBattleAction(){}

        public DoBattleAction(String playerId, Battle.DoTurn.Type type, String attack) {
            this.playerId = playerId;
            this.type = type;
            this.attack = attack;
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
        
        boolean runSuccessful;

        public BattleTurnData(){}
    }
}


class ClientBroadcast extends Action {

//    HeroPlayer player;
    int playerIndex;
    
    int timeStep = 0;

    public int layer = 1;
    public int getLayer(){return this.layer;}

    public ClientBroadcast(final Game game) {
//        this.player = game.players[1];
        this.playerIndex = 1;
        
        //game has final keyword because registering listeners below requires it
        
        //register listeners - trigger whenever client receives message from server
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
                                    Tile tile = new Tile(tileData.tileName, tileData.tileNameUpper, tileData.pos.cpy(), true, null);
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
                                game.insertAction(new playerStanding(game, player, false, true));
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
                                game.map.tiles.put(tileData.pos.cpy(),
                                                   new Tile(tileData.tileName, tileData.tileNameUpper,
                                                            tileData.pos.cpy(), true, null));
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
    
    @Override
    public void step(Game game) {

        //send to server the location of current player
         //right now it's location of wasd player
         //in future will need to pass the player index as well

        this.timeStep++;
        
        //TODO - timestep of 1 will not work - why? some sort of interference?
        
        if (this.timeStep >= 1) { 

            Network.UpdatePlayer updatePlayer = new Network.UpdatePlayer();
            
//            updatePlayer.position = this.player.physics.getPosition();
//            updatePlayer.dirFacing = this.player.dirFacing;
//            
//            updatePlayer.health = this.player.health;
//            updatePlayer.swordSwingCooldown = this.player.swordSwingCooldown;
//            updatePlayer.damageCooldown = this.player.damageCooldown;
//            
//            updatePlayer.swordPos = this.player.sword.physics.getPosition();
//            updatePlayer.swordFilter = this.player.sword.physics.fixture.getFilterData();
            
//            game.client.sendTCP(updatePlayer);
            this.timeStep = 0;
        }
    }
}



//this class will broadcast positions that client must update periodically
class ServerBroadcast extends Action {

    int timeStep = 0;

    public int layer = 1;
    public int getLayer(){return this.layer;}

    public ServerBroadcast(final Game game) {

        //register server listeners
        game.server.addListener(new Listener() {
            public void received(Connection c, final Object object) {

                // debug code
//                try {
//                    Thread.sleep(game.map.rand.nextInt(400));
//                } catch (InterruptedException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
                
                // We know all connections for this server are actually
                // CharacterConnections.
                final CharacterConnection connection = (CharacterConnection) c;
                Vector2 character = connection.character;

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
                                    Vector2 startLoc = game.map.edges.get(game.map.rand.nextInt(game.map.edges.size()));
                                    player.position.set(startLoc);
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
                                    player.standingAction = new playerStanding(game, player, false, true);
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
                                
                                // apply the effects of the attack, and send back to client
                                // TODO: calculations here for hit/miss, crit, effect hit, etc.
                                Network.BattleTurnData turnData = new Network.BattleTurnData();
                                if (battleAction.type == Battle.DoTurn.Type.RUN) {
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
                                            game.map.currRoute.pokemon.remove(game.battle.oppPokemon);
                                            game.map.currRoute.genPokemon(256);
                                            player.currPokemon.exp += battle.calcFaintExp();
                                            while (player.currPokemon.level < 100 && player.currPokemon.gen2CalcExpForLevel(player.currPokemon.level+1) <= player.currPokemon.exp) {
                                                player.currPokemon.level += 1;
                                            }
                                            // Check if pokemon evolves or not
                                            // TODO: handle when player cancels evolution
                                            for (int i=1; i <= player.currPokemon.level; i++) {
                                                if (Pokemon.gen2Evos.get(player.currPokemon.name.toLowerCase()).containsKey(String.valueOf(i))) {
                                                    String evolveTo = Pokemon.gen2Evos.get(player.currPokemon.name.toLowerCase()).get(String.valueOf(i));
                                                    player.currPokemon.Evolve(evolveTo);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    if (finalHealth > 0 || (battleAction.type == Battle.DoTurn.Type.RUN && !turnData.runSuccessful)) {
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
                                            game.map.currRoute.pokemon.remove(game.battle.oppPokemon);
                                            game.map.currRoute.genPokemon(256);
                                            player.currPokemon.exp += battle.calcFaintExp();
                                            while (player.currPokemon.level < 100 && player.currPokemon.gen2CalcExpForLevel(player.currPokemon.level+1) <= player.currPokemon.exp) {
                                                player.currPokemon.level += 1;
                                            }
                                            // Check if pokemon evolves or not
                                            // TODO: handle when player cancels evolution
                                            for (int i=1; i <= player.currPokemon.level; i++) {
                                                if (Pokemon.gen2Evos.get(player.currPokemon.name.toLowerCase()).containsKey(String.valueOf(i))) {
                                                    String evolveTo = Pokemon.gen2Evos.get(player.currPokemon.name.toLowerCase()).get(String.valueOf(i));
                                                    player.currPokemon.Evolve(evolveTo);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                                // TODO: won't work for trainer battles
                                //  might be something like battle.oppPokemons or battle.pokemon
                                if (battle.oppPokemon.currentStats.get("hp") <= 0 || (battleAction.type == Battle.DoTurn.Type.RUN && turnData.runSuccessful)) {
                                    battle.oppPokemon.inBattle = false;
                                    player.canMove = true;
                                    player.numFlees = 0;
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
                                Network.TileData tileData = (Network.TileData) object;
                                if (!game.map.tiles.containsKey(tileData.pos)) {
                                    System.out.println("TileData: Invalid tile position " + tileData.pos.toString() + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                    throw new Exception();
                                }
                                if (game.map.tiles.get(tileData.pos).attrs.containsKey("solid") && game.map.tiles.get(tileData.pos).attrs.get("solid")) {
                                    throw new Exception();  // may be trying to build on a tile that was just built on.
                                }
                                for (Player player : game.players.values()) { 
                                    if (player.position.equals(tileData.pos)) {
                                        throw new Exception();
                                    }
                                }
                                Tile oldTile = game.map.tiles.get(tileData.pos);
                                // preserve route from previous tile here.
                                game.map.tiles.put(tileData.pos.cpy(),
                                                   new Tile(tileData.tileName, tileData.tileNameUpper,
                                                            tileData.pos.cpy(), true, oldTile.routeBelongsTo));
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
                                Network.UseHM useHM = (Network.UseHM) object;
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

    @Override
    public void step(Game game) {

        this.timeStep++;

        //note - 1 WILL NOT work if you are using ServerReceive to receive client requests (use 2)
        //not sure why this is, but happens when stopping the thread in game.initNetwork and then updating via ServerReceive every frame
        if (this.timeStep >= 1) {

            Network.UpdatePlayer updatePlayer = new Network.UpdatePlayer();

            //          updatePlayer.position = game.player.physics.getPosition();
            //          updatePlayer.dirFacing = game.player.dirFacing;
            //          
            //          updatePlayer.health = game.player.health;
            //          updatePlayer.swordSwingCooldown = game.player.swordSwingCooldown;
            //          updatePlayer.damageCooldown = game.player.damageCooldown;
            //          
            //          updatePlayer.swordPos = game.player.sword.physics.getPosition();
            //          updatePlayer.swordFilter = game.player.sword.physics.fixture.getFilterData();

            //          game.server.sendToAllTCP(updatePlayer);

            //        System.out.println("Server - update player  "+String.valueOf(1));


            //          //update fireball
            //          Network.UpdateFireball updateFireball = new Network.UpdateFireball();
            ////          updateFireball.position = game.map.fireball.physics.getPosition();
            //
            //          game.server.sendToAllTCP(updateFireball);


            //update ghosts
            //          int i = 0;
            //          for (Ghost ghost : game.map.enemies) {
            //              Network.UpdateGhostData updateGhost = new Network.UpdateGhostData();
            //              updateGhost.position = ghost.physics.getPosition();
            //              updateGhost.dirFacing = ghost.dirFacing;
            //              updateGhost.id = ghost.id;
            //
            //              updateGhost.damageCooldown = ghost.damageCooldown;
            //              updateGhost.isDying = ghost.isDying;
            //              updateGhost.attackingCooldown = ghost.attackingCooldown;
            //              updateGhost.damageBoxCooldown = ghost.damageBoxCooldown;
            //
            //              updateGhost.target = ghost.targetIndex;
            //              
            //              game.server.sendToAllTCP(updateGhost);
            //              
            //              i++; //unused
            //          }

            //update game.map score for both players
            //only occurs when score changes
            //          if (game.map.network.prevPlayer1Score != game.map.player1Score) {
            //
            //              Network.UpdateScore updateScore = new Network.UpdateScore();
            //              updateScore.player = 0;
            //              updateScore.score = game.map.player1Score;
            //              game.server.sendToAllTCP(updateScore);
            //              
            //              game.map.network.prevPlayer1Score = game.map.player1Score;
            ////            System.out.println("server - sent player 1 score: " + String.valueOf(updateScore.score)); //debug
            //          }
            //
            //          if (game.map.network.prevPlayer2Score != game.map.player2Score) {
            //
            //              Network.UpdateScore updateScore = new Network.UpdateScore();
            //              updateScore.player = 1;
            //              updateScore.score = game.map.player2Score;
            //              game.server.sendToAllTCP(updateScore);
            //              
            //              game.map.network.prevPlayer2Score = game.map.player2Score;
            //          }
            //        System.out.println("Num ghosts:  "+String.valueOf(i));
            this.timeStep = 0;
        }
    }
}

