--[
When a sector is created for the first time,
	game calls a script named by template: !x!y.lua (where x & y are sector's global coordinates) from this directory (if the script exists),
	if init script doesn't exist, the game randomly generates sector's contents with WorldGen.
This means that if you want to add some things "on top" of a generated map, you have to call worldGen instance from world yourself ( e.g. world.worldGen:generate(world:getCurMapID()) ).
Otherwise, you can just initialize an empty map using map's methods.
--]