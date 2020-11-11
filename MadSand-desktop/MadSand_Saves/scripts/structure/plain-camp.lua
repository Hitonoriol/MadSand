local origin, width, height = assert(loadfile(structure_header)(..., 10, 10));

local map = world:getCurLoc();
map:fillTile(origin.x, origin.y, width, height, 23);	-- Fill & erode camp tiles
map:erodeTileRectangle(origin.x, origin.y, width, height, 1, 0);
map:spawnNpc(5, origin.x + 5, origin.y + 5);		-- Spawn tutorial quest branch NPC
map:spawnNpc(36, origin.x + 1, origin.y + 8);		-- Vending machine? Why is it here?
map:addObject(origin.x + 5, origin.y + 4, 6);		-- Add campfire
map:delObject(origin.x, origin.y);

return true;