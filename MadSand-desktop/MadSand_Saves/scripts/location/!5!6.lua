world.worldGen:generate(world:getCurMapID(), -1, 50, 50);

utils:notice("The key should be somewhere around here...");
local map = world:getCurLoc();
map:randPlaceLoot(100);