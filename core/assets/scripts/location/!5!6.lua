world.worldGen:initPosition():friendlyOnly():setBiome(1):setSize(65, 65):generate();

utils:notice("The key should be somewhere around here...");
local map = world:getCurLoc();
map:randPlaceLoot(100);