utils:showOkDialog("You see a wandering trader passing by...");
local map = world:getCurLoc();
map:purge();
map:rollSize(10, 15);
map.defTile = utils:oneOf('32,30,37,38,13,14');
map:fillTile();
world.player:teleport(map:getWidth() / 2, map:getHeight() / 2);
map:spawnMobs("8,1", 1, 2);
for i = 1,35 do
	map:randPlaceTile(29);
	map:randPlaceTile(26);
end