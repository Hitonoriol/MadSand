-- Map Structure header
-- Called by structures to set their position and width/height and check if they can be built

local s_origin, s_width, s_height = ...;
if not verify_structure(s_origin.x, s_origin.y, s_width, s_height) then
	return false;
end

s_origin:setSize(s_width, s_height);

return s_origin, s_width, s_height;