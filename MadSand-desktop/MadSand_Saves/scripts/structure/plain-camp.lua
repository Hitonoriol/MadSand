local structure, width, height = assert(loadfile(structure_header)(..., 10, 10));

local map = world:getCurLoc();
map:fillTile(structure.x, structure.y, width, height, 23);	-- Fill & erode camp tiles
map:erodeTileRectangle(structure.x, structure.y, width, height, 1, 0);
map:spawnNpc(5, structure.x + 5, structure.y + 5);		-- Spawn tutorial quest branch NPC
map:addObject(structure.x + 5, structure.y + 4, 6);		-- Add campfire
map:delObject(structure.x, structure.y);

return true;