local structure = assert(loadfile(structure_header)(..., math.random(1, 3), math.random(1, 3)));
local map = world:getCurLoc();

local crop_list = "20,43,47,51,55";

map:fillTile(structure.x, structure.y, structure.width - 1, structure.height - 1, 15);
map:fillCrop(structure.x, structure.y, structure.width - 1, structure.height - 1, val_utils:oneOf(crop_list));

return true;