-- Teleport anywhere in [<ability_lvl> + <base_radius>] cell radius

local ability = this(...);
local base_radius = 4;

mouse:setClickAction(
	lambda:intBiConsumer(
		function(x, y)
			print("Teleporting to " .. x .. ", " .. y);
			player:teleport(x, y);
			change_stamina(ability);
		end
	)
, base_radius + ability.lvl);