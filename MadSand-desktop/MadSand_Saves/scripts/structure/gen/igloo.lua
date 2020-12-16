local radius = 4
local structure = assert(loadfile(structure_header)(..., radius * 2, radius * 2));
local map = world:getCurLoc();

map:drawObjectCircle(structure.x + radius, structure.y + radius, radius, 167);
map:putLoot(structure.x + radius + math.random(-2, 2),
	structure.y + radius + math.random(-2, 2),
	utils:lootCategory(val_utils:oneOfStrings("Weapons,Armor")),
	math.random(1,2));

return true;