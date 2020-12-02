local structure, width, height = assert(loadfile(structure_header)(..., 3, 3));

-- A stone rectangle without corners & with loot in the middle

local map = world:getCurLoc();
map:drawObjectRectangle(structure.x, structure.y, width - 1, height - 1, 13);	-- Stone walls
map:fillTile(structure.x, structure.y, width - 1, height - 1, 12);		-- Stone floor

-- Remove edges:
map:delObject(structure.x, structure.y);				map:delTile(structure.x, structure.y);
map:delObject(structure.x + width - 1, structure.y);			map:delTile(structure.x + width - 1, structure.y);
map:delObject(structure.x, structure.y + height - 1);			map:delTile(structure.x, structure.y + height - 1);
map:delObject(structure.x + width - 1, structure.y + height - 1);	map:delTile(structure.x + width - 1, structure.y + height - 1);

map:addObject(structure.x + width/2, structure.y, 39);	-- Stone cross
map:putLoot(structure.x + width/2, structure.y + height/2, utils:lootCategory(val_utils:oneOfStrings("Weapons,Armor")), 1);

return true;