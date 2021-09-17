package hitonoriol.madsand.tests;

import org.junit.jupiter.api.Test;

import hitonoriol.madsand.dialog.TextSubstitutor;
import hitonoriol.madsand.util.Utils;

public class TextSubstitutorTest {
	@Test
	void test() {
		Utils.out("-----Substitutor test-----");
		Utils.out(TextSubstitutor.replace("Player: {PLAYER}"));
		Utils.out(TextSubstitutor.replace("Rand name F: {RANDOM_NAME_F}"));
		Utils.out(TextSubstitutor.replace("Rand name M: {RANDOM_NAME_M}"));
		Utils.out(TextSubstitutor.replace("Tile: Gravel -> @(tile: gravel)"));
		Utils.out(TextSubstitutor.replace("Item: Raw Poultry -> @(item: poultry)"));
		Utils.out(TextSubstitutor.replace("Object: Wooden Door -> @(object: door)"));
		Utils.out(TextSubstitutor.replace("Lua expr: Player's HP -> $(player.stats.hp)"));
		Utils.out("--------------------------");
	}
}
