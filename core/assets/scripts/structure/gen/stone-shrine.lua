local structure = assert(utils:loadScript(structure_header)(..., 3, 3));

-- A stone rectangle without corners & with loot in the middle

local map = world:getCurLoc();
map:drawObjectRectangle(structure.x, structure.y, structure.width - 1, structure.height - 1, 13);	-- Stone walls
map:fillTile(structure.x, structure.y, structure.width - 1, structure.height - 1, 12);		-- Stone floor

-- Remove edges:
map:delObject(structure.x, structure.y);				map:delTile(structure.x, structure.y);
map:delObject(structure.x + structure.width - 1, structure.y);			map:delTile(structure.x + structure.width - 1, structure.y);
map:delObject(structure.x, structure.y + structure.height - 1);			map:delTile(structure.x, structure.y + structure.height - 1);
map:delObject(structure.x + structure.width - 1, structure.y + structure.height - 1);	map:delTile(structure.x + structure.width - 1, structure.y + structure.height - 1);

map:addObject(structure.x + structure.width/2, structure.y, 39);	-- Stone cross
map:putLoot(structure.x + structure.width/2, structure.y + structure.height/2, utils:lootCategory(val_utils:oneOfStrings("Weapons,Armor")), 1);

return true;