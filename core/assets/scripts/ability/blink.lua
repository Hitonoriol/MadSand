local ability = this(...);
mouse:setClickAction(
	lambda:intBiConsumer(
		function(x, y)
			print("Teleporting to " .. x .. ", " .. y);
			player:teleport(x, y);
			change_stamina(ability);
		end
	)
);