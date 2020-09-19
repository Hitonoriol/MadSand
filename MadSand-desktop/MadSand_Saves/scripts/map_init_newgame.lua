local map = world:getCurLoc();
local player = world.player;

player:teleport(50, 50);
world:updateLight();
map:spawnNpc(5, 55, 55);
utils:addObject(55, 54, 6);

utils:print("Welcome to MadSand!");