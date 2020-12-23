local structure = assert(loadfile(structure_header)(..., 9, 9));
local map = world:getCurLoc();
local x0, y0 = structure.x + structure.width/2, structure.y + structure.height/2;

map:fillTile(structure.x, structure.y, structure.width, structure.height, 30);
map:erodeTileRectangle(structure.x, structure.y, structure.width, structure.height, 2, map.defTile);

map:addObject(x0, y0, 15);
map:putLoot(x0, y0, 100);

local tiles = { 29, 26, 27 };
for i = 0, 3 do
	map:drawTileCircle(x0, y0, i + 1, tiles[i]);
end

return true;