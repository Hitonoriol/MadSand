local structure = assert(loadfile(structure_header)(..., 4 + math.random(3), 7 + math.random(2)));
local map = world:getCurLoc();

structure:fillTile(23);
map:drawObjectLine(structure.x, structure.y, structure.xMax, structure.y, 173);
map:drawObjectLine(structure.x, structure.yMax, structure.xMax, structure.yMax, 173);
map:drawObjectLine(structure.x, structure.y, structure.x, structure.yMax, 174);
map:drawObjectLine(structure.xMax, structure.y, structure.xMax, structure.yMax, 174);

for i = 0, math.random(8) do
	map:spawnNpc(val_utils:oneOf("37,38,39"), structure:getFreeTile());
end

for i = 0, math.random(12) do
	map:addObject(structure:getFreeTile(), val_utils:oneOf("169,170"));
end

return true;