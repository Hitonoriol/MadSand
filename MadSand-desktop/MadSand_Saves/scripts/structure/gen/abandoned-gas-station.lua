local structure, width, height = assert(loadfile(structure_header)(..., 8 + math.random(0, 2), 4 + math.random(0, 2)));
local map = world:getCurLoc();
structure:clear();
map:fillTile(structure.x, structure.y, width, height, 46);
map:erodeTileRectangle(structure.x, structure.y, width, height, 1, 0);
local free_tile;

for i = 0, 3 do
	free_tile = structure:getFreeTile();
	map:addObject(free_tile.x, free_tile.y, 159);
end

free_tile = structure:getFreeTile();
map:spawnNpc(36, free_tile.x, free_tile.y);

return true;