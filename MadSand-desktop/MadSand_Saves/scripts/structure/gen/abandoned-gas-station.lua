local structure, width, height = assert(loadfile(structure_header)(..., 8 + math.random(0, 2), 4 + math.random(0, 2)));
local map = world:getCurLoc();
structure:clear();
map:fillTile(structure.x, structure.y, width, height, 46);
map:erodeTileRectangle(structure.x, structure.y, width, height, 1, 0);

for i = 0, 3 do
	map:addObject(structure:getFreeTile(), 159);
end

if (val_utils:percentRoll(30)) then	-- chance to spawn a vending machine
	map:spawnNpc(36, structure:getFreeTile());
end

return true;