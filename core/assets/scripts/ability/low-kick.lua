-- Hit currently targeted enemy (or map object), dealing [125 + 2 * (<ability_lvl>)]% of baseAttack

local ability = this(...);
local enemyPos = player:lookingAt();

if not world:getCurLoc():isFreeTile(enemyPos) then
	local dmg = player:getStats():getBaseAttack() * (1.25 + ability.lvl * 0.02);
	player:attack(enemyPos, dmg);
	change_stamina(...);
else
	utils:warn("You are not fighting anyone right now");
end