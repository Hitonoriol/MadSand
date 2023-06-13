package hitonoriol.madsand;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonGetter;

import me.xdrop.fuzzywuzzy.FuzzySearch;

public interface Enumerable {
	void setId(int id);
	@JsonGetter("id")
	int id();
	String name();

	default boolean idEquals(Enumerable enumerableObj) {
		return id() == enumerableObj.id();
	}

	static <T extends Enumerable> T find(Map<Integer, T> items, String partialName) {
		return FuzzySearch.extractOne(
			partialName,
			items.values(),
			item -> item.name() == null ? "" : item.name(),
			FuzzySearch::weightedRatio
		)
			.getReferent();
	}

	static <T extends Enumerable> int findId(Map<Integer, T> items, String partialName) {
		return find(items, partialName).id();
	}

	Class<Integer> idType = Integer.class;

	public static class EnumerableMap<T extends Enumerable> extends HashMap<Integer, T> {}
}
