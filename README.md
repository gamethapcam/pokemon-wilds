# Pokemon Wilds

![Pokemon Wilds](https://github.com/SheerSt/pokemon-wilds/blob/master/android/assets/8.png)

## How to Play
 - Download /builds/latest/pokemon-wilds.exe and run.
 - Java is required in order to run (it's required by libGDX, the engine this is built on). If you don't have java installed, a popup may appear pointing you to java installation instructions.
 - **NOTE:** this is currently in Alpha. Things are getting there, but still not fully complete. If you find a bug, please create an issue here on github describing it (that would help me out a ton).

## v0.1 gameplay video

https://www.youtube.com/watch?v=ndQ0d0hhIRQ

## Full-resolution images of procedurally generated maps (along with various screenshots)

https://imgur.com/a/Pq2Ht2p

# About

Pokemon Wilds is a Gen 2 Pokemon game/engine built using libGDX. It uses procedural generation to create large worlds with different biomes, each with their own unique pokemon. The levels of wild pokemon increase the further you explore, meaning that some biomes with rare pokemon are difficult to get to. Your Pokemon can help you explore the world by usual means (cut, fly, surf etc), but there are also be new HMs that allow you to interact with the world in new ways, like building structures/houses, jumping up ledges, starting fires, and more.

I have been using a set of techniques to 'rip' sprites and animations from the console game very accurately - as such, the game is intended to look and feel just like the console games, with some extra capabilities (see core/assets/attacks/). The benefit from using libGDX is that the engine can support cross-platform play, multiplayer, huge maps, fullscreen mode, and more pokemon.

In the future I hope that the game can also support graphics from other generations, as well as a map editor mode that could serve as a platform for custom games.

# v0.2 (Latest)
 - Pokefarms - you can build fences, let your pokemon out to walk around, and grow trees by planting Apricorns.
 - Crafting - you can craft Apricorns into Pokeballs at a campfire.
 - Smart building - tiles that you build will 'snap' together and form the structure as you build it.
 - Host mode - option to host a server. Allows you to zoom around the map like an observer.

# v0.1
 - Procedurally generated beach, forest, mountain and snow biomes with their own unique pokemon.
 - All Gen 2 Pokemon + Crystal intro animations.
 - Support for the most battle mechanics (wild battle, level up, evolution, attacks, running, catching, etc.)
 - Rips of some attack animations.
 - Building stuff.
 - Multiplayer support.
 - Saving/Loading worlds.
 - Currently desktop and Android support only.

# Future aims and goals for the project
 - Map editor.
 - Support for other platforms (controls, screen resizing).
 - Larger proc-gen worlds with more biomes (desert, graveyard, dungeons, ...)
 - Pokemon breeding / shinies
 - Full Gen 2 engine replication (battle mechanics, attacks, etc).
 - Other Generations.
 
# Contributing

This project is still in the early stages. If you are interested in joining the project, please pm me and we can work out the details of what currently needs done. Loosely, here is the current list:
 - Rips of Gen 2 attack animations
 - Battle system mechanics and animations (trainer battles, status effects, temporary stat changes, etc)
 - Network code for anything added



