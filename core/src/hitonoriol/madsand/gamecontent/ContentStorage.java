package hitonoriol.madsand.gamecontent;

import hitonoriol.madsand.Enumerable;

public abstract class ContentStorage<T extends Enumerable> extends EnumerableStorage<Integer, T> {
	protected ContentStorage(T defaultValue) {
		super(defaultValue);
	}
}
