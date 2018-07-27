package ru.bernarder.fallenrisefromdust;

public class World {
	private int xsz, ysz;

	// number of layers-fixed?
	public World(int xsz, int ysz) {
		this.xsz = xsz;
		this.ysz = ysz;
	}

	int getWidth() {
		return xsz;
	}

	int getHeight() {
		return ysz;
	}

	void timeTick() {// on successful movement or action
		// moblogic, croplogic, playerstats update
	}
}