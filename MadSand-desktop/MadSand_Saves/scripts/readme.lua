--[
Available bindings:

	* Everything from World class (lua instance name: world)
	* Tutorial.show(name) - shows dialog chain from tutorial.json ( lua usage: tutorial:show(name) )

	* Print & Dialog utils:
		* utils:print() -- print to in-game log
		* utils:notice() -- print to in-game log with noticeColor
		* utils:showDialog(query) -- generates dialog chain from string query ( #Title#Dialog text[button text]=>#Next title#next dialog text[btn]=>.... )

	* Map editing:
		local map = world:getCurLoc();
		* map:addTile(x, y, tile_id) -- place tile
		* map:addObject(x, y, object_id) -- place map object
		* map:delObject(x, y)	-- delete map object

	* Misc Map-related stuff:
		* utils:locateTile(id) -- returns a pair(x, y) of coordinates of specified tile
		* utils:ascend() -- change current location layer to ++layer (move a floor up)
		* utils:descend() -- change current location layer to --layer (move a floor down)

If you want to do a specific one-time thing (e.g. show a dialog when player level ups a certain skill to a certain level,
	you can use player:registerLuaAction(name) and then check if this action has been done before with player:luaActionDone(name))

--]