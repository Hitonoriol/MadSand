package hitonoriol.madsand.lua;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.luaj.vm2.LuaValue;

public class LuaLambda {
	public static Runnable runnable(LuaValue chunk) {
		return () -> Lua.callChunk(chunk);
	}

	public static LuaConsumer consumer(LuaValue chunk) {
		return arg -> Lua.callChunk(chunk, arg);
	}

	public static LuaBiConsumer biConsumer(LuaValue chunk) {
		return (arg1, arg2) -> Lua.callChunk(chunk, arg1, arg2);
	}

	public static LuaIntBiConsumer intBiConsumer(LuaValue chunk) {
		return (arg1, arg2) -> Lua.callChunk(chunk, arg1, arg2);
	}

	public static LuaPredicate predicate(LuaValue chunk) {
		return arg -> Lua.callChunk(chunk, arg).toboolean();
	}

	public interface LuaPredicate extends Predicate<Object> {}
	public interface LuaConsumer extends Consumer<Object> {}
	public interface LuaBiConsumer extends BiConsumer<Object, Object> {}
	public interface LuaIntBiConsumer extends BiConsumer<Integer, Integer> {}
}
