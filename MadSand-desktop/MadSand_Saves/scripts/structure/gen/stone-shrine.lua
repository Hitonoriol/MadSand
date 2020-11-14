local origin, width, height = assert(loadfile(structure_header)(..., 3, 3));

-- A stone rectangle without corners & with loot in the middle

local map = world:getCurLoc();
map:drawObjectRectangle(origin.x, origin.y, width - 1, height - 1, 13);	-- Stone walls
map:fillTile(origin.x, origin.y, width - 1, height - 1, 12);		-- Stone floor

-- Remove edges:
map:delObject(origin.x, origin.y);				map:delTile(origin.x, origin.y);
map:delObject(origin.x + width - 1, origin.y);			map:delTile(origin.x + width - 1, origin.y);
map:delObject(origin.x, origin.y + height - 1);			map:delTile(origin.x, origin.y + height - 1);
map:delObject(origin.x + width - 1, origin.y + height - 1);	map:delTile(origin.x + width - 1, origin.y + height - 1);

map:addObject(origin.x + width/2, origin.y, 39);	-- Stone cross
map:putLoot(origin.x + width/2, origin.y + height/2, utils:lootCategory(val_utils:oneOfStrings("Weapons,Armor")), 1);

return true;