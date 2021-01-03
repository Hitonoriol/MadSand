-- This script gets executed on every player.doAction() call

show_tutorial(stamina_tutorial, function() return stats:getStaminaPercent() <= 25 end);
show_tutorial(travel_tutorial, function() return player:hasItem(130) end);
show_tutorial(dungeon_tutorial, function() return player:hasItem(100) end);