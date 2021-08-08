--Is executed once to init all global vars and functions that can be used by any script executed after this one

script_dir = ...;
structure_header = "structure/structure.lua";
root_package = "hitonoriol.madsand";

stats = player.stats;

this = function(value)
	return value;
end

show_tutorial = function(name, condition)
	if (not player:luaActionDone(name) and condition()) then
		tutorial:show(name);
		player:registerLuaAction(name);
	end
end

obj_delete_looked_at = function()
	local coords = player:lookingAt();
	local dx = coords.x; local dy = coords.y;
	world:getCurLoc():delObject(dx, dy);
end

verify_structure = function(x, y, w, h)
	local map = world:getCurLoc();
	
	if (x + w > map:getWidth()) or (y + h > map:getHeight()) then
		return false;
	end
	
	return true;
end

change_stamina = function(ability)
	player:changeStamina(-ability:getStaminaCost());
end

-- Append root package name to the beginning of the provided full class name
package = function(name)
	return root_package .. "." .. name;
end

-- Get access to all public static methods from class <name>
static = function(name)
	return luajava.bindClass(package(name))
end

-- Create a new instance of class <name>, calling its constructor with the provided varargs
new = function(name, ...)
	return luajava.newInstance(package(name), ...)
end