local structure = assert(loadfile(structure_header)(..., 5 + math.random(0, 2), 4 + math.random(0, 2)));
local map = world:getCurLoc();

map:fillTile(structure.x, structure.y, structure.width - 1, structure.height - 1, val_utils:oneOf("47,48"));

map:addObject(structure.x, structure.y, 166);
map:addObject(structure.xMax, structure.y, 166);

for i = 1, structure.height - 1, 2 do
	map:drawObjectLine(structure.x, structure.y + i, structure.xMax, structure.y + i, val_utils:oneOf("163,164,165"));
end

map:drawObjectLine(structure.x + structure.width/2, structure.y, structure.x + structure.width/2, structure.yMax, 0);

return true;