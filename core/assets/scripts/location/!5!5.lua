world:getWorldGenerator():initPosition():setBiome(0):skipLandPropGen():generate();

local map = world:getCurLoc();
local player = world.player;

for i = 1,4 do
	map:spawnMobs(true, true);
end

local structure = map:addStructure("plain-camp");	-- Create starting location

player:teleport(structure.x, structure.y);		-- Teleport player to the origin of the structure
map:spawnMobs("12", 1, 7, 15);				-- Spawn Old farmer NPC
map:spawnMobs("4", 1, 10, 15);				-- Spawn Pirate NPC

local free_tile = map:getFreeTileNear(map:locateObject(82));
map:spawnNpc(9, free_tile);	-- Spawn Miner NPC near mine entrance

utils:print("Welcome to MadSand!");