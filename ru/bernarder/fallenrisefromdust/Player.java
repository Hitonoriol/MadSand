package ru.bernarder.fallenrisefromdust;

import ru.bernarder.fallenrisefromdust.enums.*;

public class Player
{
	int hp,mhp,exp,nexp,lvl;
	float speed, splim;	//moves/actions per world tick
	String name;
	boolean isMain;
	Faction faction;
	Direction direction;

	public Player(String name) {
		this.name = name;
	}
}