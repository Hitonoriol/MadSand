local structure = assert(loadfile(structure_header)(..., 7 + math.random(0, 2), 6));
local map = world:getCurLoc();

local p1 = pair:make(structure.x, structure.y);
local p2 = pair:make(structure.xMax, structure.y);
local p3 = pair:make(structure.x + structure.width/2, structure.yMax);

map:drawObjectTriangle(p1, p2, p3, 168);
map:fillTileTriangle(p1, p2, p3, val_utils:oneOf("28,35"));

local rolls = 1 + math.random(0, 2);
for i = 1, rolls do
	map:randPlaceLootInTriangle(
		utils:rollLoot(utils:lootCategory(val_utils:oneOfStrings("Weapons,Armor,Food,Materials"))),
		p1, p2, p3
	);
end

return true;