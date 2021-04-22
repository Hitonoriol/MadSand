package hitonoriol.madsand.gui.dialogs;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.ability.Ability;
import hitonoriol.madsand.entities.ability.ActiveAbility;
import hitonoriol.madsand.entities.ability.PassiveAbility;
import hitonoriol.madsand.properties.Prefs;
import hitonoriol.madsand.util.Functional;
import hitonoriol.madsand.util.Utils;

public class AbilityDialog extends GameDialog {

	private List<Ability> abilities;
	static float PAD = 30;

	public AbilityDialog(List<Ability> abilities) {
		super.setTitle("Abilities");
		super.skipLine();
		this.abilities = abilities;

		if (abilities.isEmpty())
			super.add("You don't have any abilities").height(250).align(Align.center).row();
		else {
			super.skipLine().padBottom(PAD);
			createAbilityBlock("Active", ActiveAbility.class);
			createAbilityBlock("Passive", PassiveAbility.class);
		}

		super.row();
		super.skipLine().padTop(PAD);
		super.addCloseButton();
	}

	private <T extends Ability> void createAbilityBlock(String title, Class<T> abilityType) {
		if (!hasAbilities(abilityType))
			return;

		Gui.setFontSize(super.add(title).padBottom(7).getActor(), Gui.FONT_M);
		super.row();
		createAbilityEntries(abilityType, ability -> addAbilityButton(ability.as(abilityType).get()));

	}

	private <T extends Ability> boolean hasAbilities(Class<T> abilityType) {
		return filterAbilities(abilityType).findAny().isPresent();
	}

	private <T extends Ability> Stream<Ability> filterAbilities(Class<T> abilityType) {
		return abilities.stream()
				.filter(ability -> ability.is(abilityType));
	}

	private <T extends Ability> void createAbilityEntries(Class<T> abilityType, Consumer<Ability> action) {
		filterAbilities(abilityType)
				.forEach(action);
	}

	private void addAbilityButton(Ability abstrAbility) {
		String btnText = abstrAbility.name + " Lvl. " + abstrAbility.lvl;
		abstrAbility.as(ActiveAbility.class)
				.ifPresent(
						ability -> {
							Prefs prefs = Prefs.values();

							addButton(btnText + Resources.LINEBREAK
									+ "[" + ability.staminaCost + " stamina]",
									() -> {
										ability.apply();
										Utils.scheduleTask(() -> remove(), 0.1f);
									}).padRight(5);

							String bindText = Optional.of(prefs.getAbilityKey(ability.id))
									.map(key -> Keys.toString(key))
									.orElse("No Key");

							addButton(bindText, () -> new KeyDialog(key -> prefs.bindAbility(key, ability.id))).row();
						});

		abstrAbility.as(PassiveAbility.class)
				.ifPresent(ability -> Functional.with(addButton(btnText, () -> {}), cell -> {
					cell.getActor().setDisabled(true);
					cell.row();
				}));
	}

	private Cell<TextButton> addButton(String text, Runnable action) {
		Cell<TextButton> cell = super.add(new TextButton(text, Gui.skin)).width(300).height(45);
		Gui.setAction(cell.getActor(), action);
		return cell;
	}

}
