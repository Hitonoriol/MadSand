-- Called on game load
local offline_time = ...;

function offline_bonus()
	local timeflux_bonus = math.floor(offline_time / 240);
	player:addItem(101, timeflux_bonus);

	print('Offline for ' .. offline_time .. '; Bonus: ' .. timeflux_bonus);
end

function daily_bonus()
	local date_field = 'last_played';
	
	local grabbag_list = '128,129';
	
	local full_date = os.date("*t");
	local current_date = full_date.day .. '/' .. full_date.month .. '/' .. full_date.year;
	local previous_date = world:getStorageValue(date_field);

	if (current_date ~= previous_date) then
		player:addItem(utils:oneOf(grabbag_list), 1);
		world:setStorageValue(date_field, current_date);
		utils:notice('You receive today\'s daily bonus!');
	end
end

offline_bonus();
daily_bonus();