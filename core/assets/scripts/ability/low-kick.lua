local player = world.player;
local enemy = world:getCurLoc():getNpc(player:lookingAt());
player:attack(enemy, player:getBaseAttack() * 1.25);
