package ru.bernarder.fallenrisefromdust;

import ru.bernarder.fallenrisefromdust.strings.Objects;

public class Object {
	int id, hp;
	String name;

	public Object(int id) {
		this.id = id;
		this.name = Objects.name.get(id);
	}

	void destroy() {
		this.id = 0;
	}

	boolean verify() {
		if (this.hp > 0)
			return true;
		else {
			destroy();
			return false;
		}
	}

	int takeDamage() {
		this.hp--;
		this.verify();
		return this.hp;
	}
}
