-- Pathfind to selected tile and put calculated path to player's movementQueue, then start movement with increased speed, consuming [<ability.staminaCost> * path length] stamina

local ability = ...;

mouse:setClickAction(
	lambda:intBiConsumer(
		function(x, y)
			local path = mouse:getPathToCursor();
			local path_cost = (path:getCount() - 1) * ability.staminaCost;
			
			if (path_cost + ability.staminaCost > player:getStats().stamina) then
				utils:warn("You don't have enough stamina to run such distance!");
				return;
			end
			
			change_stamina(ability:addBonusCost(path_cost));
			player:run(path);
		end
	)
, 5 + ability.lvl);

