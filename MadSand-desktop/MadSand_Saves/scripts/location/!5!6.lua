local mapID = world:getCurMapID();
print(mapID.worldxy.x .. ', ' .. mapID.worldxy.y .. ' :: ' .. mapID.layer .. ' -- ' .. mapID.id);
world.worldGen:initPosition():setSize(65, 65):generate();

utils:notice("The key should be somewhere around here...");
local map = world:getCurLoc();
map:randPlaceLoot(100);