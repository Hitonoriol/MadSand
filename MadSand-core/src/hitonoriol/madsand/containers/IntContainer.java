package hitonoriol.madsand.containers;

public class IntContainer {
	public int value;
	public String name;

	public IntContainer(String name, int value) {
		set(name, value);
	}

	public IntContainer() {
		this("", 0);
	}

	public IntContainer set(String name, int value) {
		this.name = name;
		this.value = value;
		return this;
	}
}
