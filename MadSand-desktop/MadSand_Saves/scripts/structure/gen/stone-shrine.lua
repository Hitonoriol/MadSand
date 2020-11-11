local origin, width, height = assert(loadfile(structure_header)(..., math.random(3, 4), math.random(3, 4)));

-- A stone rectangle without corners & with loot in the middle

local map = world:getCurLoc();
map:drawObjectRectangle(origin.x, origin.y, width, height, 13);	-- Stone walls
map:fillTile(origin.x, origin.y, width, height, 12);		-- Stone floor

-- Remove edges:
map:delObject(origin.x, origin.y);			map:delTile(origin.x, origin.y);
map:delObject(origin.x + width, origin.y);		map:delTile(origin.x + width, origin.y);
map:delObject(origin.x, origin.y + height);		map:delTile(origin.x, origin.y + height);
map:delObject(origin.x + width, origin.y + height);	map:delTile(origin.x + width, origin.y + height);

map:addObject(origin.x + width/2, origin.y, 39);	-- Stone cross
map:putLoot(origin.x + width/2, origin.y + height/2, utils:lootCategory(val_utils:oneOfStrings("Weapons,Armor")), 1);

return true;