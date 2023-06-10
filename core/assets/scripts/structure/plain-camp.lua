local structure = assert(lua:loadScript(structure_header)(..., 10, 10));
local map = world:getCurLoc();

structure:clear();
map:fillTile(structure.x, structure.y, structure.width, structure.height, 23);	-- Fill & erode camp tiles
map:erodeTileRectangle(structure.x, structure.y, structure.width, structure.height, 1, 0);
map:addObject(structure.x + 5, structure.y + 4, 6);	-- Add campfire

local nx, ny = structure.x + 5, structure.y + 5;
map:spawnNpc(5, nx, ny); -- Spawn tutorial questline NPC
map:addWaypoint(nx, ny):setDestinationName("Stranger");

return true;