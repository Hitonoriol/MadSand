package hitonoriol.madsand;

import com.fasterxml.jackson.annotation.JsonGetter;

public interface Enumerable {
	void setId(int id);
	@JsonGetter("id")
	int id();

	default boolean idEquals(Enumerable enumerableObj) {
		return id() == enumerableObj.id();
	}
}
