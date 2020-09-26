local mapID = world:getCurMapID();
print(mapID.worldxy.x .. ', ' .. mapID.worldxy.y .. ' :: ' .. mapID.layer .. ' -- ' .. mapID.id);
world.worldGen:generate(mapID, -1, 65, 65);

utils:notice("The key should be somewhere around here...");
local map = world:getCurLoc();
map:randPlaceLoot(100);