-- This script gets executed on new game start (starting location init script)
-- (TODO: Make init script map for scripted locations)

local map = world:getCurLoc();
local player = world.player;

map:spawnNpc(5, 55, 55);
map:addObject(55, 54, 6);

player:teleport(50, 50);
world:updateLight();

utils:print("Welcome to MadSand!");