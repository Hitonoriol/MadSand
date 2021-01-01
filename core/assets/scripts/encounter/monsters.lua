utils:showOkDialog("You've been ambushed by a swarm of monsters!");
local map = world:getCurLoc();
map:purge();
map:rollSize(35, 45);
map.defTile = utils:oneOf('10,23,24,34,36,40,5,11,12');
map:fillTile();
world.player:teleport(map:getWidth() / 2, map:getHeight() / 2);
map:spawnMobs("2,16,17,18,19,15,32", math.random(10, 20), 8);
for i = 1,50 do
	map:randPlaceTile(26);
	map:randPlaceTile(27);
	map:randPlaceTile(18);
end