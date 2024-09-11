# DeadSouls
### A simple tombstone system inspired by Dark Souls.
This project would not be possible without [Darkyenus](https://github.com/Darkyenus/DeadSouls). He did all the heavy lifting, I have simply updated the plugin for my personal needs.
<br /><br />

## How does the plugin work?
When a player dies, they will leave behind a soul in the last safe location (outside a lava pool or at the top of a tall cliff) that contains their items and experience. The soul will play sounds as the player approaches to help them locate the exact location of the soul. Upon walking into the soul, the player regains these items and some of the lost experience.

The soul is interactable only to the player who initially died for two hours. After two hours have passed, other players can collect the soul. Alternatively, players can release their souls for their friends to retrieve the items and experience inside immediately.

If the player does not have enough inventory space to carry all items within the soul, the soul will remain so that the player can retrieve the rest of the contents at a later time.

The plugin is highly configurable, and each player's experience can be customized by using permissions. The explanation above applies when utilizing the default configuration file, but many behaviors of the plugin can be changed easily by editing the configuration.
<br /><br />

## Features
* ##### Provides players with a way to safely retrieve their items and experience without fear of items despawning.

* ##### Includes a souls list (`/souls`) to easily see soul information, release souls, or teleport to souls.

* ##### More appealing than spawning a player chest and potentially losing items if the player has a full inventory.

* ##### Perfectly supports custom items.

* ##### Prevents server lag caused by items dropping and rendering.

* ##### Customizable behavior.


## Configuration
Many aspects of the plugin can be configured. You can view the default configuration [here](https://github.com/Marotheit/DeadSouls/blob/master/src/main/resources/config.yml).
<br /><br />

## Permissions
### Default
---
* ##### `deadsouls.souls.spawn` allows the player to spawn a soul upon death. This permission, in conjunction with the permissions that allow the soul to store items and experience, provides the core experience of DeadSouls.

* ##### `deadsouls.souls.save.items` allows the soul to store the player's items when it is spawned.

* ##### `deadsouls.souls.save.experience` allows the soul to store a portion of the player's experience when it is spawned.

* ##### `deadsouls.souls.list` lists applicable player souls when using the `/souls` command. Players can perform other functions from this list as well, such as releasing their soul or teleporting to their soul, should they have the applicable permissions.

* ##### `deadsouls.souls.release` allows the player to release their soul when using the `/souls` command. After a player has released their soul, other players will be able to retrieve the contents of the soul.

* ##### `deadsouls.souls.spectator` allows the player to see all souls in the world if they are set to the Spectator gamemode. They will not be able to retrieve the souls contents.
<br />

### Optional
---
* ##### `deadsouls.souls.coordinates` displays the location of each applicable soul when using the `/souls` command. This will also display the location of the soul upon death. This is not recommended for normal players as it breaks immersion.

* ##### `deadsouls.souls.distance` displays the distance from each applicable soul when using the `/souls` command. This is not recommended for normal players as it breaks immersion, but can be a better alternative than displaying coordinates.

* ##### `deadsouls.souls.teleport` allows the player to teleport to their soul. This is not recommended for normal players as it breaks immersion.
<br />

### Administrator
---
* ##### `deadsouls.souls.list.all` lists all player souls when using the `/souls` command, even those that do not belong to you.

* ##### `deadsouls.souls.release.all` allows the player to release any soul when using the `/souls` command, even those that do not belong to you.

* ##### `deadsouls.souls.teleport.all` allows the player to teleport to any soul when using the `/souls` command, even those that do not belong to you.
<br /><br />

## How to Compile

```bash
# Test gradle to ensure your environment is acceptable.
./gradlew test
```
```bash
# Build plugin jar file; The destination directory for the plugin file will be `<Project Location>\build\libs\`.
./gradlew build
```

This plugin was designed to be reliable and lightweight. It does not use any non-public APIs.
Please report any issues, and I will see what I can remedy.