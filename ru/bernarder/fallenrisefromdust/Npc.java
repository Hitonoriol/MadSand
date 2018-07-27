package ru.bernarder.fallenrisefromdust;

import ru.bernarder.fallenrisefromdust.enums.Faction;

public class Npc
{
	int id,hp,mhp,atk,qid;
	String loot, name;
	Faction faction;

	public Npc(int id) {
		this.id = id;
	}
}