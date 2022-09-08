# MadSand

> Open-world survival sandbox RPG

### [Download Launcher](https://github.com/Hitonoriol/MadSand/releases/download/launcher/MadSandLauncher.jar)

### Screenshots
![screenshot](https://raw.githubusercontent.com/Hitonoriol/MadSand/master/screenshots/title%20screen.png)
![screenshot](https://raw.githubusercontent.com/Hitonoriol/MadSand/master/screenshots/big%20dungeon%20room.png)
![screenshot](https://raw.githubusercontent.com/Hitonoriol/MadSand/master/screenshots/desert.png)
![screenshot](https://raw.githubusercontent.com/Hitonoriol/MadSand/master/screenshots/dead%20lands.png)
![screenshot](https://raw.githubusercontent.com/Hitonoriol/MadSand/master/screenshots/snowy%20biome.png)
![screenshot](https://raw.githubusercontent.com/Hitonoriol/MadSand/master/screenshots/plains.png)
![screenshot](https://raw.githubusercontent.com/Hitonoriol/MadSand/master/screenshots/starting%20location.png)
![screenshot](https://raw.githubusercontent.com/Hitonoriol/MadSand/master/screenshots/inventory.png)
![screenshot](https://raw.githubusercontent.com/Hitonoriol/MadSand/master/screenshots/light%20system.png)
![screenshot](https://raw.githubusercontent.com/Hitonoriol/MadSand/master/screenshots/character%20creation.png)
![screenshot](https://raw.githubusercontent.com/Hitonoriol/MadSand/master/screenshots/trade%20ui.png)
![screenshot](https://raw.githubusercontent.com/Hitonoriol/MadSand/master/screenshots/equipment%20sidebar.png)
![screenshot](https://raw.githubusercontent.com/Hitonoriol/MadSand/master/screenshots/quest%20dialog.png)
![screenshot](https://raw.githubusercontent.com/Hitonoriol/MadSand/master/screenshots/crafting%20menu.png)
![screenshot](https://raw.githubusercontent.com/Hitonoriol/MadSand/master/screenshots/quest%20journal.png)

### Building

**`JDK 18+` is required**  

There are two subprojects that can be built:  
* `desktop` -- the game itself;
* `launcher` -- a lightweight launcher that accesses this repository's release section via GitHub API to keep the game up-to-date and fetch changelogs.  

\
**Building jars:**

Invoke `dist` task of the desired project, for example:

```
./gradlew desktop:dist
```

Resulting files will be located in `<project-name>/build/libs`.  

\
**Building self-contained native packages:**  

Invoke `jpackage` task of the desired project, for example:

```
./gradlew desktop:jpackage
```

or `nativeDist` to create the package as a zip archive.  

Resulting files will be located in: `<project-name>/build/distribution`  