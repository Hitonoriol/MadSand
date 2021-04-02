package hitonoriol.madsand.gui.dialogs;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.ability.Ability;
import hitonoriol.madsand.entities.ability.ActiveAbility;
import hitonoriol.madsand.entities.ability.PassiveAbility;

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
						ability -> addButton(btnText + Resources.LINEBREAK
								+ "[" + ability.staminaCost + " stamina]",
								() -> {
									ability.apply();
									Utils.scheduleTask(() -> remove(), 0.1f);
								}));

		abstrAbility.as(PassiveAbility.class)
				.ifPresent(ability -> addButton(btnText, () -> {}).setDisabled(true));
	}

	private TextButton addButton(String text, Runnable action) {
		TextButton button = new TextButton(text, Gui.skin);
		super.add(button).width(300).height(45).row();
		Gui.setAction(button, action);
		return button;
	}

}
