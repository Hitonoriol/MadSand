-- Called on game load
local offline_time = ...;

local timeflux_bonus = math.floor(offline_time / 240);
world.player:addItem(101, timeflux_bonus);

print('Offline for ' .. offline_time .. '; Bonus: ' .. timeflux_bonus);