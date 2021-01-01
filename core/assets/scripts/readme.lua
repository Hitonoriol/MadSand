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
		Basic editing:
			* map:addTile/addObject(x, y, id) -- place tile/object
			* map:delTile/delObject(x, y)	-- delete object/tile
			* map:fillTile/fillObject([id]) -- fill the whole map with specified tile/object or with default tile/object if id is not specified
			* map:erodeTileRectangle(x, y, width, height, [depth], id) -- replace tiles on the border of a rectangle area with specified tile with 30% chance
		Shapes:
			* map:drawTileRectangle/drawObjectRectangle(x, y, width, height, id)
			* map:fillTile/fillObject(x, y, width, height, id) -- fills rectangle region of map with tile/object
			* map:drawObjectLine(x1, y1, x2, y2, id)
			* map:drawTileCircle/drawObjectCircle(x0, y0, radius, id)
			* map:fillTileCircle/fillObjectCircle(x0, y0, radius, id)
			* map:drawTileTriangle/drawObjectTriangle(p1, p2, p3, id) -- Points p1, p2, p3 must be of type Pair (use pair:make(x, y) to create one from lua)
		Structures:
			* map:addStructure(name) -- place specified structure from scripts/structure/ somewhere on map. Returns Pair with structure's coords.
			* map:addStructure(name, x, y) -- place structure on specified map coords. Returns false if structure didn't fit into the map and true otherwise.

	* Misc Map-related stuff:
		* utils:locateTile(id) -- returns a pair(x, y) of coordinates of specified tile
		* utils:ascend() -- change current location layer to ++layer (move a floor up)
		* utils:descend() -- change current location layer to --layer (move a floor down)

If you want to do a specific one-time thing (e.g. show a dialog when player level ups a certain skill to a certain level,
	you can use player:registerLuaAction(name) and then check if this action has been done before with player:luaActionDone(name))

--]