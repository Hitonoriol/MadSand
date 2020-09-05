--[
Available bindings:
	* Everything from World class (lua instance name: world)
	* Tutorial.show(name) - shows dialog chain from tutorial.json (lua usage: tutorial:show())
	* Stuff from LuaUtils:
		* utils:print() -- print to in-game log
		* utils:notice() -- print to in-game log with noticeColor
		* utils:showDialog(query) -- generates dialog chain from string query ( #Title#Dialog text[button text]=>#Next title#next dialog text[btn]=>.... )

If you want to do a specific one-time thing (e.g. show a dialog when player level ups a certain skill to a certain level,
	you can use player:registerLuaAction(name) and then check if this action has been done before with player:luaActionDone(name))
--]