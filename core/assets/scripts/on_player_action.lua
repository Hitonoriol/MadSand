-- This script gets executed on every player.doAction() call

-- Poll tutorial trigger conditions
show_tutorial("LowStamina", function() return stats:getStaminaPercent() <= 50 end);
show_tutorial("Travel", function() return player:hasItem(130) end);
show_tutorial("DungeonKey", function() return player:hasItem(100) end);