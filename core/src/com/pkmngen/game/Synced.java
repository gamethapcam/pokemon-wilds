package com.pkmngen.game;

/*
 * TODO: remove if unused.
 */
public class Synced {

    static class HashMap<T, U> {
        // Player required so server knows which connection to send to.
        public Player player;
        String objectName;
        java.util.HashMap<T, U> object = new java.util.HashMap<T, U>();
        U response;

        public HashMap(String objectName) {
            this.objectName = objectName;
        }

        public U get(T key) {
            Game game = Game.staticGame;
            if (game.type == Game.Type.SERVER) {
                return this.object.get(key);
            }

            // If client, request object to be sent over network.
            Game.staticGame.client.sendTCP(new Network.SyncedHashMap(this.objectName, "get", null));
            while (this.response == null) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            U response = this.response;
            this.response = null;
            return response;
        }

        public void put (T key, U val) {
            this.object.put(key, val);
        }
    }
}
