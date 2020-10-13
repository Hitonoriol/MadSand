world.worldGen:initPosition():setBiome(0):generate();

local map = world:getCurLoc();
local player = world.player;

for i = 1,4 do
	map:spawnMobs(true, true);
end

map:fillTile(50, 50, 10, 10, 23);
map:erodeTileRectangle(50, 50, 10, 10, 1, 0);
map:spawnNpc(5, 55, 55);
map:spawnNpc(36, 51, 58);
map:addObject(55, 54, 6);

map:spawnMobs("12", 1, 10);	--Spawn "Old farmer" NPC somewhere on the map

map:delObject(50, 50);
player:teleport(50, 50);
world:updateLight();

utils:print("Welcome to MadSand!");