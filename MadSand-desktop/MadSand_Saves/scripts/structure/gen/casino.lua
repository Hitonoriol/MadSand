local structure = assert(loadfile(structure_header)(..., 4 + math.random(5), 4 + math.random(5)));
local map = world:getCurLoc();
structure:clear();
local first_tile, second_tile = 49, 50;

local tile = first_tile;
local switch_tile = function() return (tile == first_tile) and second_tile or first_tile; end
for y = 0, structure.height do
	for x = 0, structure.width do
		tile = switch_tile();
		map:addTile(structure.x + x, structure.y + y, tile);
	end
	tile = switch_tile();
end

map:drawObjectRectangle(structure.x, structure.y, structure.width, structure.height, 104);
local door_coords = pair:make(structure.x + structure.width/2, structure.y);
map:delObject(door_coords);
map:addObject(door_coords, val_utils:oneOf("11,12"));

for i = 0, math.random(4) do
	map:addObject(structure:getFreeTile(), val_utils:oneOf("5,172"));
end

return true;