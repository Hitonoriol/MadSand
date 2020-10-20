--Is executed once to init all global vars and functions that can be used by any script executed after this one

player = world.player;
stats = player.stats;

stamina_tutorial = "LowStamina";
travel_tutorial = "Travel";

show_tutorial = function(name, condition)
	if (condition() and not player:luaActionDone(name)) then
		tutorial:show(name);
		player:registerLuaAction(name);
	end
end