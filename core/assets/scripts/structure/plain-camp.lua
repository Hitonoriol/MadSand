local structure = assert(utils:loadScript(structure_header)(..., 10, 10));
local map = world:getCurLoc();

structure:clear();
map:fillTile(structure.x, structure.y, structure.width, structure.height, 23);	-- Fill & erode camp tiles
map:erodeTileRectangle(structure.x, structure.y, structure.width, structure.height, 1, 0);
map:spawnNpc(5, structure.x + 5, structure.y + 5);		-- Spawn tutorial quest branch NPC
map:addObject(structure.x + 5, structure.y + 4, 6);		-- Add campfire

return true;